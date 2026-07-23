/*
 *	File Name:	ParameterMap.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2024/07/31		2.2.1	checkXSS() 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										ParameterMap(): remove PARAM_LOCALE 추가
 *	stghr12		2007/10/31		2.1.0	ParameterMap(): remove PARAM_MODE, PARAM_CONDITION_KEY, PARAM_SAVEDOBJECT_KEY 추가
 *										ParameterMap( req, removingCondition ) 추가
 *										getTimeValue(), getTimeValues() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.servlet;

import com.irt.data.Condition;
import com.irt.html.HtmlUtility;

import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;

/**
 *
 */
public class ParameterMap extends java.util.HashMap<String, Object> {
	public ParameterMap( ServletRequest req ) {
		this( req, false );
	}

	public ParameterMap( ServletRequest req, boolean removingCondition ) {
		super( req.getParameterMap() );

		remove( ServletModel.PARAM_MODE );
		remove( ServletModel.PARAM_URL );
		remove( ServletModel.PARAM_FOCUS );
		remove( ServletModel.PARAM_LOCALE );
		remove( ServletModel.PARAM_MENU );
		remove( ServletModel.PARAM_WINTYPE );
		remove( ServletModel.PARAM_ALLROWS );
		remove( ServletModel.PARAM_MAXROWS );
		remove( ServletModel.PARAM_SKIPROWS );
		remove( ServletModel.PARAM_SORTKEY );
		remove( ServletModel.PARAM_MESSAGE_KEY );
		remove( ServletModel.PARAM_CONDITION_KEY );
		remove( ServletModel.PARAM_SAVEDOBJECT_KEY );

		if( removingCondition ) {
			remove( Condition.BASIS_CONDITIONKEY );
			remove( Condition.DISTINCT_CONDITIONKEY );
			remove( Condition.GROUPING_CONDITIONKEY );
		}

		for( java.util.Iterator iterator = entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String key = (String)entry.getKey();
			String[] values = (String[])entry.getValue();

			int count = values.length;
			while( count > 0 && values[count-1].length() == 0 ) count--;

			if( count == 0 )
				iterator.remove();
			else if( count == 1 )
				put( key, values[0] );
			else {
				String[] values_new = null;

				if( values.length != count ) {
					values_new = new String[count];
					System.arraycopy( values, 0, values_new, 0, count );
				}
				for( int v = 0; v < count; v++ ) {
					if( values[v].length() == 0 ) {
						if( values_new == null ) {
							values_new = new String[count];
							System.arraycopy( values, 0, values_new, 0, count );
						}
						values_new[v] = null;
					}
				}
				if( values_new != null ) put( key, values_new );
			}
		}

		String filterValue = removeParameter( ServletModel.PARAM_FILTER_VALUE );
		String filterType = removeParameter( ServletModel.PARAM_FILTER_TYPE );
		if( filterValue != null && filterType != null ) {
			put( filterType, filterValue );
			put( filterType + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_CONTAINS );
		}
	}

	public boolean checkXSS() {
		for( java.util.Iterator iterator = entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String key = (String)entry.getKey();
			Object values = entry.getValue();

			if( !(values instanceof Object[]) ) {
				values = new Object[] { values };
			}

			for( Object obj : (Object[]) values ) {
				if( HtmlUtility.checkXSS(obj.toString()).length() == 0 )
					return true;
			}
		}

		return false;
	}

	public String getParameter( String key ) {
		Object object = get( key );
		if( object instanceof String )
			return (String)object;
		else if( object instanceof String[] )
			return ((String[])object)[0];
		else
			return null;
	}

	public String[] getParameterValues( String key ) {
		Object object = get( key );
		if( object instanceof String )
			return new String[] { (String)object };
		else if( object instanceof String[] )
			return (String[])object;
		else
			return null;
	}

	public String getTimeValue( String key ) {
		String value = getParameter( key );
		if( value == null ) {
			String value_h = getParameter( key +"_h" );
			String value_m = getParameter( key +"_m" );
			if( value_h != null && value_m != null )
				value = value_h +":"+ value_m;
		}

		return value;
	}

	public String[] getTimeValues( String key ) {
		String[] values = getParameterValues( key );
		if( values == null ) {
			String[] values_h = getParameterValues( key +"_h" );
			String[] values_m = getParameterValues( key +"_m" );
			if( values_h != null && values_m != null ) {
				values = new String[ values_h.length < values_m.length ? values_h.length : values_m.length ];
				for( int i = 0; i < values.length; i++ ) {
					if( values_h[i] != null && values_m[i] != null )
						values[i] = values_h[i] +":"+ values_m[i];
					else
						values[i] = null;
				}
			}
		}

		return values;
	}

	public String removeParameter( String key ) {
		Object object = remove( key );
		if( object instanceof String )
			return (String)object;
		else if( object instanceof String[] )
			return ((String[])object)[0];
		else
			return null;
	}

	public String[] removeParameterValues( String key ) {
		Object object = remove( key );
		if( object instanceof String )
			return new String[] { (String)object };
		else if( object instanceof String[] )
			return (String[])object;
		else
			return null;
	}

	public List<Map<String, Object>> extractGroupList( String groupKey ) {
		String prefix = groupKey + "_";

		int count = 1;
		Map<String, Object> map = new java.util.HashMap<String, Object>();
		for( java.util.Iterator iterator = entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String key = (String)entry.getKey();

			if( key.startsWith(prefix) ) {
				Object object = entry.getValue();

				map.put( key.substring(prefix.length()), object );
				if( object instanceof Object[] ) {
					int length = ((Object[])object).length;
					if( length > count ) count = length;
				}
				iterator.remove();
			}
		}
		if( map.size() == 0 ) return null;

		List<Map<String, Object>> list = new java.util.ArrayList<Map<String, Object>>();
		list.add( map );
		if( count == 1 ) return list;

		for( int i = 1; i < count; i++ )
			list.add( new java.util.HashMap<String, Object>(map.size()) );

		for( Map.Entry<String, Object> entry : map.entrySet() ) {
			String key = entry.getKey();
			Object object = entry.getValue();

			if( object instanceof Object[] ) {
				String[] values = (String[])object;
				for( int v = 1; v < values.length; v++ )
					if( values[v] != null )
						list.get(v).put( key, values[v] );
				map.put( key, values[0] );
			}
		}

		return list;
	}

	public Map<String, Object> extractGroupMap( String groupKey ) {
		String prefix = groupKey + "_";

		Map<String, Object> map = new java.util.HashMap<String, Object>();
		for( java.util.Iterator iterator = entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String key = (String)entry.getKey();

			if( key.startsWith(prefix) ) {
				map.put( key.substring(prefix.length()), entry.getValue() );
				iterator.remove();
			}
		}
		return( map.size() == 0 ? null : map );
	}
}
