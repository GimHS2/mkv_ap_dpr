/*
 *	File Name:	DataUtility.java
 *	Version:	2.2.1(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/10/31		2.2.1	getStringValueTrim() : 값 앞에 0을 제거하는 기능 추가
 *	GimHS		2025/09/30		2.2.0	create(copy WMataUtility.java)
 *
**/

package com.irt.dpr.tools;

import java.util.Map;

/**
 *
 */
class DataUtility {
	public static String getStringValue( Object obj ) {
		if( obj instanceof String )
			return (String)obj;
		else if( obj instanceof Number )
			return String.valueOf( obj );
		else if( obj instanceof Double )
			return String.valueOf( obj );
		else
			if( obj != null )
				return obj.toString();

		return null;
	}

	public static String getStringValueTrim( Map<String, Object> map, String key ) {
		String val = getStringValue( map.get(key) );
		if( val != null ) val = val.replaceFirst( "^0+", "" );
		return val;
	}

	public static String getStringValue( Map<String, Object> map, String key ) {
		return getStringValue( map.get(key) );
	}

	public static String getStringMoneyValue( Map<String, Object> map, String key, boolean isFractionCorrection ) {
		if( isFractionCorrection ) {
			return map.get(key) == null ? null : getStringValue( map.get(key) ).replace(".", "").replace(",", "");
		} else {
			return getStringValue( map.get(key) );
		}
	}

	public static Long getLongValue( String str ) {
		try {
			return ( str == null ? null : Long.valueOf(str) );
		} catch( NumberFormatException numEx ) {
			try {
				Number num = java.text.NumberFormat.getInstance().parse( str );
				return Long.valueOf( num.longValue() );
			} catch( java.text.ParseException ex ) {}
		}

		return null;
	}

	public static Long getLongValue( Map<String, Object> map, String key ) {
		Object obj = map.get(key);

		if( obj == null )
			return null;
		else if( obj instanceof Number )
			return Long.valueOf( ((Number)obj).longValue() );
		else if( obj instanceof String )
			return getLongValue( (String)obj );

		return null;
	}

	public static Double getDoubleValue( String str ) {
		try {
			return ( str == null ? null : Double.valueOf(str) );
		} catch( NumberFormatException numEx ) {
			try {
				java.lang.Number num = java.text.NumberFormat.getInstance().parse( str );

				return Double.valueOf( num.doubleValue() );
			} catch( java.text.ParseException ex ) {}
		}

		return null;
	}

	public static Double getDoubleMetricValue( Map<String, Object> map, String key ) {
		return getDoubleValue( getStringValue(map, key) );
	}

	public static Double getDoubleMoneyValue( Map<String, Object> map, String key ) {
		return getDoubleValue( getStringMoneyValue(map, key, false) );
	}

	public static Double getDoubleMoneyValue( Map<String, Object> map, String key, boolean isFractionCorrection ) {
		return getDoubleValue( getStringMoneyValue(map, key, isFractionCorrection) );
	}


	public static Double getDoubleValue( Map<String, Object> map, String key ) {
		return getDoubleValue( getStringValue(map, key) );
	}
}
