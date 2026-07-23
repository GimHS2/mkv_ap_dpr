/*
 *	File Name:	ServletUtility.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2016/05/31		2.2.2	locale 파라미터 유효성 검사 추가
 *	hankalam	2015/10/30		2.2.1	웹취약성 수정. locale 파라미터 위험문자 검사
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										createMap(Object, Object) -> createMap(String, Object)
 *										java.util.Calendar.getInstance().getTimeInMillis() -> System.currentTimeMillis()
 *	stghr12		2007/10/31		2.1.2	getFieldKeys(), getLocale() 추가
 *	stghr12		2007/04/30		2.1.1	createMap(): java.util.HashMap() -> java.util.TreeMap()
 *	stghr12		2006/12/01		2.1.0	getSortKeys(), setSort() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.servlet;

import com.irt.data.cols.ColumnList;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 */
public class ServletUtility {
	private final static int DEFAULT_TEMPORARY_SAVETIME	= 20000;		// 20초

	private static class TemporaryObject {
		public long timemilli;
		public Object savedObj;

		public TemporaryObject( long timemilli, Object savedObj ) {
			this.timemilli = timemilli;
			this.savedObj = savedObj;
		}
	}

	public static <V> List<Map<String, V>> createList( Map<String, V> map ) {
		List<Map<String, V>> list = new java.util.ArrayList<Map<String, V>>();

		list.add( map );

		return list;
	}

	public static Map<String, Object> createMap( String key, Object value ) {
		Map<String, Object> map = new java.util.TreeMap<String, Object>();

		map.put( key, value );

		return map;
	}

	public static String[] getFieldKeys( Object columnListObject ) {
		String[] fieldKeys = null;
		if( columnListObject instanceof ColumnList )
			return ((ColumnList)columnListObject).getFieldKeys();
		else if( columnListObject instanceof ColumnList[] ) {
			ColumnList[] columnLists = (ColumnList[])columnListObject;

			fieldKeys = columnLists[0].getFieldKeys();
			for( int i = 1; i < columnLists.length; i++ )
				fieldKeys = columnLists[i].getFieldKeys( fieldKeys );
		}

		return fieldKeys;
	}

	public static String[] getFieldKeys( Object columnListObject, String... fieldKeys ) {
		if( columnListObject instanceof ColumnList )
			return ((ColumnList)columnListObject).getFieldKeys( fieldKeys );
		else if( columnListObject instanceof ColumnList[] ) {
			ColumnList[] columnLists = (ColumnList[])columnListObject;
			for( int i = 0; i < columnLists.length; i++ )
				fieldKeys = columnLists[i].getFieldKeys( fieldKeys );
		}

		return fieldKeys;
	}

	public static Locale getLocale( HttpServletRequest req ) {
		String locale = req.getParameter( ServletModel.PARAM_LOCALE );
		if( locale != null ) {
			String[] locales = locale.split( "_", 3 );

			if( locale.length() == 2 ) {
				switch( locales.length ) {
				case 3:
					return new Locale( locales[0], locales[1], locales[2] );
				case 2:
					return new Locale( locales[0], locales[1] );
				case 1:
					return new Locale( locales[0] );
				}
			}
		}

		return req.getLocale();
	}

	public static String[] getSortKeys( HttpServletRequest req, String... defaultSortKeys ) {
		String[] sortKeys = req.getParameterValues( ServletModel.PARAM_SORTKEY );
		if( sortKeys == null )
			return defaultSortKeys;
		else {
			for( int k = 0; k < sortKeys.length; k++ ) {
				if( sortKeys[k].length() == 0 ) {
					int count = k;
					for( ; k < sortKeys.length; k++ ) {
						if( sortKeys[k].length() > 0 )
							count++;
					}
					if( count == 0 ) return defaultSortKeys;

					defaultSortKeys = new String[ count ];

					count = 0;
					for( k = 0; k < sortKeys.length; k++ ) {
						if( sortKeys[k].length() > 0 )
							defaultSortKeys[count++] = sortKeys[k];
					}
				}
			}

			return sortKeys;
		}
	}

	public static Object popTemporaryObject( HttpServletRequest req, String saveKey ) throws java.util.NoSuchElementException {
		HttpSession session = req.getSession( false );
		if( session == null )
			throw new java.util.NoSuchElementException( saveKey );

		Object savedObj = session.getAttribute( saveKey );
		if( savedObj instanceof TemporaryObject ) {
			session.removeAttribute( saveKey );
			return ((TemporaryObject)savedObj).savedObj;
		}

		throw new java.util.NoSuchElementException( saveKey );
	}

	public static String pushTemporaryObject( HttpServletRequest req, String saveKeyPrefix, Object object ) {
		HttpSession session = req.getSession();

		String saveKey = null;
		long timemilli = System.currentTimeMillis();
		synchronized( session ) {
			for( int k = 0; true; k++ ) {
				Object savedObj = session.getAttribute( saveKey = (saveKeyPrefix +"_"+ k) );
				if( savedObj == null )
					break;
				else if( savedObj instanceof TemporaryObject ) {
					if( timemilli > ((TemporaryObject)savedObj).timemilli )
						break;
				}
			}
			session.setAttribute( saveKey, new TemporaryObject(timemilli+DEFAULT_TEMPORARY_SAVETIME, object) );
		}

		return saveKey;
	}

	public static <V> Map<String, V> putFirstMap( Map<String, V> destinationMap, Collection<Map<String, ? extends V>> collection ) {
		if( collection != null && collection.size() > 0 )
			return putMap( destinationMap, collection.iterator().next() );

		return destinationMap;
	}

	public static <V> Map<String, V> putMap( Map<String, V> destinationMap, Map<String, ? extends V> sourceMap ) {
		for( Map.Entry<String, ? extends V> entry : sourceMap.entrySet() ) {
			String key = entry.getKey();
			V value = destinationMap.put( key, entry.getValue() );
			if( value != null ) destinationMap.put( key, value );
		}

		return destinationMap;
	}

	public static void setSort( HttpServletRequest req, com.irt.data.QueryableManager db, String... defaultSortKeys ) {
		String[] sortKeys = req.getParameterValues( ServletModel.PARAM_SORTKEY );
		if( sortKeys == null )
			db.setSort( defaultSortKeys );
		else {
			for( int k = 0; k < sortKeys.length; k++ ) {
				if( sortKeys[k].length() == 0 ) {
					db.clearSort();

					int count = k;
					for( int i = 0; i < count; i++ )
						db.appendSort( sortKeys[i] );
					for( ; k < sortKeys.length; k++ )
						if( sortKeys[k].length() > 0 ) {
							count++;
							db.appendSort( sortKeys[k] );
						}

					if( count == 0 )
						db.setSort( defaultSortKeys );
					return;
				}
			}

			db.setSort( sortKeys );
		}
	}
}
