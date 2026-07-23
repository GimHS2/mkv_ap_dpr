/*
 *	File Name:	Record.java
 *	Version:	2.2.6
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2016/10/31		2.2.6	Java Document 주석 추가, Generic Type warning 수정
 *	stghr12		2011/02/28		2.2.5	MERGE 추가, varargs 사용([] -> ...)
 *	stghr12		2010/08/31		2.2.4	varargs 사용([] -> ...)
 *										decodeQueryToMap(), encodeMapToQuery(), extractRecord(list, keys, values), equals(), merge() 추가
 *	stghr12		2009/06/30		2.2.3	removeMap() 추가
 *	stghr12		2008/05/31		2.2.2	decodeValueToMap(): String[] 지원
 *	stghr12		2008/03/31		2.2.1	INSERT_OR_UPDATE_DELETE 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										createMap(Object, Object) -> createMap(String, Object)
 *	stghr12		2007/10/31		2.1.2	copyMap(), decodeValueToMap(), extractObjectArray( recordList, key ), removeUnEqualValue() 추가
 *	stghr12		2007/04/30		2.1.1	createMap(): java.util.HashMap() -> java.util.TreeMap()
 *										extractRecord(), extractString() 추가
 *	stghr12		2006/12/01		2.1.0	replaceValues() 추가
 *										DELETE, REGIST, INSERT, MODIFY, UPDATE 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Map, List 등의 자료 구조를 조작 하기 위한 static Method 들이 구현되어 있는 Utility Class.
 */
public class Record {
	/** 삭제. **/
	public final static int DELETE						= 0x0001;
	/** 등록. **/
	public final static int REGIST						= 0x0010;
	/** 등록({@link #REGIST}와 동일). **/
	public final static int INSERT						= REGIST;
	/** 수정. **/
	public final static int MODIFY						= 0x0100;
	/** 수정({@link #MODIFY}와 동일). **/
	public final static int UPDATE						= MODIFY;
	/** 등록 or 수정 or 삭제. **/
	public final static int INSERT_OR_UPDATE_OR_DELETE	= INSERT | UPDATE | DELETE;
	/** 소스의 테이블을 목적지 테이블로 merge(등록 or 수정 or 삭제). **/
	public final static int MERGE						= 0x0200;
	/** 쿼리(조회). **/
	public final static int QUERY						= 0x1000;

	/**
	 * sourceMap에 있는 내용을 destinationMap에 put
	 */
	public static <K, V> void copyMap( Map<K, ? extends V> sourceMap, Map<K, V> destinationMap, K... keys ) {
		for( int k = 0; k < keys.length; k++ )
			destinationMap.put( keys[k], sourceMap.get(keys[k]) );
	}

	/**
	 * key=value 를 가지는 Map을 생성하여 return.
	 */
	public static Map<String, Object> createMap( String key, Object value ) {
		Map<String, Object> map = new java.util.TreeMap<String, Object>();

		map.put( key, value );

		return map;
	}

	/**
	 * 파라미터(url, 예: "gtin=8801001&gln=8801") 형태로 되어 있는 value를 Map에 put 후 return.
	 * @param value 파라미터(url, 예: "gtin=8801001&gln=8801") 형태의 값.
	 */
	public static Map<String, Object> decodeQueryToMap( String value ) {
		if( value == null ) return null;

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		try {
			String[] values = value.split( "&" );

			for( int i = 0; i < values.length; i++ ) {
				String[] namevalues = values[i].split( "=" );
				if( namevalues.length == 2 ) {
					Object object = conditionMap.put( namevalues[0], value = java.net.URLDecoder.decode(namevalues[1], "UTF8") );
					if( object != null ) {
						if( object instanceof String )
							conditionMap.put( namevalues[0], new String[] { (String)object, value } );
						else
							conditionMap.put( namevalues[0], com.irt.util.Arrays.append( (String[])object, value ) );
					}
				}
			}
		} catch( java.io.UnsupportedEncodingException encodingEx ) {}

		return conditionMap;
	}

	/**
	 * 파라미터(예: "gtin=8801001;gln=8801") 형태로 되어 있는 value를 Map에 put 후 return.
	 * @param value 파라미터(예: "gtin=8801001;gln=8801") 형태의 값.
	 */
	public static Map<String, Object> decodeValueToMap( String value ) {
		Map<String, Object> map = new java.util.HashMap<String, Object>();

		int idx1 = 0, idx2 = 0;
		do {
			idx2 = value.indexOf( ';', idx1 );
			while( idx2 >= 0 && (idx2+1) < value.length() && value.charAt(idx2+1) == ';' )
				idx2 = value.indexOf( ';', idx2+2 );

			String[] values;
			if( idx2 < 0 )
				values = value.substring(idx1).replaceAll(";;", ";").split( "=", 2 );
			else
				values = value.substring(idx1, idx2).replaceAll(";;", ";").split( "=", 2 );
			if( values.length == 2 ) {
				Object previousValue = map.put( values[0], values[1] );
				if( previousValue instanceof String )
					map.put( values[0], new String[] { (String)previousValue, values[1] } );
				else if( previousValue instanceof String[] )
					map.put( values[0], com.irt.util.Arrays.append( (String[])previousValue, values[1] ) );
			}
			idx1 = idx2 + 1;
		} while( idx1 > 0 );

		return map;
	}

	/**
	 * conditionMap에 들어가 있는 key=value 들을 파라미터(url, 예: "gtin=8801001&gln=8801") 형태로 변환하여 return.
	 */
	public static String encodeMapToQuery( Map<String, Object> conditionMap ) {
		StringBuffer sbuf = new StringBuffer();

		try {
			for( Iterator<Map.Entry<String, Object>> iterator = conditionMap.entrySet().iterator(); iterator.hasNext(); ) {
				Map.Entry<String, Object> entry = iterator.next();

				Object value = entry.getValue();
				if( value == null ) continue;

				Object[] values;
				if( value instanceof Object[] )
					values = (Object[])value;
				else
					values = new Object[] { value };

				for( int v = 0; v < values.length; v++ ) {
					if( values[v] == null ) values[v] = "";
					sbuf.append( "&" )
						.append( entry.getKey() )
						.append( "=" )
						.append( java.net.URLEncoder.encode(values[v].toString(), "UTF8") );
				}
			}

			return sbuf.substring( 1 );
		} catch( java.io.UnsupportedEncodingException encodingEx ) {
			return "";
		}
	}

	/**
	 * 두개의 Map(map1, map2)에 대해서 keys 들의 값(value)이 모두 동일한지를 return.
	 */
	public static boolean equals( Map<String, ? extends Object> map1, Map<String, ? extends Object> map2, String... keys ) {
		for( String key : keys ) {
			Object value = map1.get( key );
			if( value == null ) {
				if( map2.get(key) == null ) return false;
			} else {
				if( !value.equals(map2.get(key)) ) return false;
			}
		}

		return true;
	}

	/**
	 * Map(map)에 들어가 있는 keys들에 대한 값(value)들이 모두 values와 동일한지를 return.
	 */
	public static boolean equals( Map<String, ? extends Object> map, String[] keys, Object[] values ) {
		for( int k = 0; k < keys.length; k++ ) {
			if( values[k] == null ) {
				if( map.get(keys[k]) == null ) return false;
			} else {
				if( !values[k].equals(map.get(keys[k])) ) return false;
			}
		}

		return true;
	}

	/**
	 * recordMap에서 key에 해당하는 값을 추출하여 배열 형태로 return.
	 */
	@SuppressWarnings("unchecked")
	public static Object[] extractObjectArray( Map<String, ? extends Object> recordMap, String key ) {
		Object value = recordMap.get( key );

		if( value == null ) return null;
		if( value instanceof Object[] )
			return (Object[])value;
		else if( value instanceof Collection )
			return ((Collection<Object>)value).toArray();
		else
			return new Object[] { value };
	}

	/**
	 * recordList에서 각 Map의 key에 해당하는 값들을 추출하여 배열 형태로 return.
	 */
	public static Object[] extractObjectArray( List<? extends Map<String, ? extends Object>> recordList, String key ) {
		Object[] values = new Object[ recordList.size() ];

		int i = 0;
		for( Map<String, ? extends Object> recordMap : recordList )
			values[i++] = recordMap.get( key );

		return values;
	}

	/**
	 * recordList에서 각 Map의 key대한 값을 value와 비교하여 같은 값이 동일한 Map을 return(recordMap.get(key) == value 인 recordMap을 return).
	 */
	public static Map<String, Object> extractRecord( List<Map<String, Object>> recordList, String key, Object value ) {
		return extractRecord( recordList, new String[] { key }, new Object[] { value } );
	}

	/**
	 * recordList에서 각 Map의 keys들에 대한 값들을 values와 비교하여 모든 값이 동일한 Map을 return(recordMap.get(keys) == values 인 recordMap을 return).
	 */
	public static Map<String, Object> extractRecord( List<Map<String, Object>> recordList, String[] keys, Object[] values ) {
		for( Map<String, Object> recordMap : recordList )
			if( recordMap != null && equals(recordMap, keys, values) )
				return recordMap;

		return null;
	}

	/**
	 * recordMap에서 key에 해당하는 값을 추출하여 문자열 형태로 return (값이 배열일 경우 첫번째 값을 return).
	 */
	public static String extractString( Map<String, ? extends Object> recordMap, String key ) {
		Object object = recordMap.get( key );
		if( object instanceof Object[] ) object = ((Object[])object)[0];

		if( object == null )
			return null;
		else if( object instanceof String )
			return (String)object;
		else
			return object.toString();
	}

	/**
	 * recordMap에서 key에 해당하는 값을 추출하여 Object 형태로 return (값이 배열일 경우 첫번째 값을 return).
	 */
	public static Object extractValue( Map<String, ? extends Object> recordMap, String key ) {
		Object object = recordMap.get( key );
		if( object instanceof Object[] )
			return ((Object[])object)[0];
		else
			return object;
	}

	/**
	 * recordMap에서 keys 들에 해당하는 값들을 추출하여 배열 형태로 return (각각의 값이 배열일 경우 첫번째 값을 return).
	 */
	public static Object[] extractValues( Map<String, ? extends Object> recordMap, String... keys ) {
		Object[] values = new Object[ keys.length ];
		for( int f = 0; f < keys.length; f++ ) {
			values[f] = recordMap.get( keys[f] );
			if( values[f] instanceof Object[] )
				values[f] = ((Object[])values[f])[0];
		}

		return values;
	}

	/**
	 * recordMap에서 fields 들에 해당하는 값들을 추출하여 배열 형태로 return..
	 */
	public static Object[] extractValues( Map<String, ? extends Object> recordMap, Field... fields ) throws FieldException {
		Object[] values = new Object[ fields.length ];
		for( int f = 0; f < fields.length; f++ )
			values[f] = fields[f].extractValue( recordMap );

		return values;
	}

	/**
	 * recordList의 각 Map 들을 keys 들에 대한 값들이 모두 동일한 recordList_tot의 Map에 merge 하여 recordList_tot를 return (recordList의 recordMap을 recordList_tot의 recordMap에 putAll 실행, keys로 같은 recordMap인지 확인).
	 */
	public static List<Map<String, Object>> merge( List<Map<String, Object>> recordList_tot, List<Map<String, Object>> recordList, String... keys ) {
		if( recordList == null ) return recordList_tot;

		Object[] values = new Object[ keys.length ];
		for( Map<String, Object> recordMap : recordList ) {
			for( int i = 0; i < keys.length; i++ ) values[i] = recordMap.get( keys[i] );

			Map<String, Object> recordMap_tot = extractRecord( recordList_tot, keys, values );
			if( recordMap_tot != null )
				recordMap_tot.putAll( recordMap );
		}

		return recordList_tot;
	}

	public static void removeMap( Map<String, Object> recordMap, String... keys ) {
		for( int k = 0; k < keys.length; k++ )
			recordMap.remove( keys[k] );
	}

	/**
	 * destinationMap에서 sourceMap에 없거나 값이 다른 내용을 삭제 후 destinationMap을 return.
	 */
	public static <K, V> Map<K, V> removeUnEqualValue( Map<K, V> destinationMap, Map<K, ? extends Object> sourceMap ) {
		for( Iterator<Map.Entry<K, V>> iterator = destinationMap.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<K, V> entry = iterator.next();
			Object value = sourceMap.get( entry.getKey() );

			if( entry.getValue() == null ) {
				if( value != null ) iterator.remove();
			} else if( !entry.getValue().equals(value) ) iterator.remove();
		}

		return destinationMap;
	}

	/**
	 * recordMap에서 fields 들에 해당하는 값들을 추출한 후 convert된 object(값)로 recordMap에 put 하고, 그 값(object)들을 배열 형태로 return.
	 */
	public static Object[] replaceValues( Map<String, Object> recordMap, Field... fields ) throws FieldException {
		Object[] values = new Object[ fields.length ];
		for( int f = 0; f < fields.length; f++ ) {
			values[f] = fields[f].extractValue( recordMap );
			if( values[f] != null ) recordMap.put( fields[f].getFieldKey(), values[f] );
		}

		return values;
	}

	/**
	 * recordMap에서 fields 들에 해당하는 값들을 validate한 후 convert된 object(값)로 recordMap에 put하고, 그 값(object)들을 배열 형태로 return.
	 */
	public static Object[] replaceValues( Map<String, Object> recordMap, ValidableField... fields ) throws FieldException {
		Object[] values = new Object[ fields.length ];
		for( int f = 0; f < fields.length; f++ ) {
			values[f] = fields[f].validate( recordMap );
			if( values[f] != null ) recordMap.put( fields[f].getFieldKey(), values[f] );
		}

		return values;
	}

	/**
	 * recordMap에서 fields 들에 해당하는 값들을 validate 한 후 convert된 object(값)들을 배열 형태로 return.
	 */
	public static Object[] validate( Map<String, ? extends Object> recordMap, ValidableField... fields ) throws FieldException {
		Object[] values = new Object[ fields.length ];
		for( int f = 0; f < fields.length; f++ )
			values[f] = fields[f].validate( recordMap );

		return values;
	}
}
