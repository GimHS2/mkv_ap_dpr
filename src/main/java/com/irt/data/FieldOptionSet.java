/*
 *	File Name:	FieldOptionSet.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	FieldOptionSet 생성자 protected에서 public으로 변경
 *										FieldOptionSet.Field: protected에서 public으로 변경
 *										FieldOptionSet.Field: 변수 public에서 default로 변경
 *										FieldOptionSet.Field: 생성자 추가, get...()함수 추가
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.data;

import java.util.Map;

/**
 *
 */
public class FieldOptionSet {
	Map<String, FieldOptionSet.Field> fieldMap;

	public FieldOptionSet( FieldOptionSet.Field[] fields ) {
		this.fieldMap = new java.util.HashMap<String, FieldOptionSet.Field>( fields.length );
		for( int f = 0; f < fields.length; f++ )
			fieldMap.put( fields[f].fieldKey, fields[f] );
	}

	public void applyOptionToRecordMap( Map<String, Object> recordMap, Map<String, Object> originalMap ) {
		for( FieldOptionSet.Field field : fieldMap.values() ) {
			if( field.hasManageAuthValue ) continue;
			if( originalMap != null )
				recordMap.put( field.fieldKey, originalMap.get(field.fieldKey) );
			else if( field.defaultValue != null )
				recordMap.put( field.fieldKey, field.defaultValue );
			else
				recordMap.remove( field.fieldKey );
		}
	}

	public Object getDefaultValue( String fieldKey ) {
		FieldOptionSet.Field field = getField( fieldKey );
		return ( field != null ? field.defaultValue : null );
	}

	protected FieldOptionSet.Field getField( String fieldKey ) {
		return fieldMap.get( fieldKey );
	}

	public String[] getFieldKeys() {
		return fieldMap.keySet().toArray( new String[fieldMap.size()]);
	}

	public boolean hasManageAuth( String fieldKey ) {
		FieldOptionSet.Field field = getField( fieldKey );
		return ( field != null && field.hasManageAuthValue );
	}

	public boolean using( String fieldKey ) {
		FieldOptionSet.Field field = getField( fieldKey );
		return ( field != null && field.usingValue );
	}

	/**
	 *
	 */
	public static class Field {
		String fieldKey;
		boolean usingValue;
		boolean hasManageAuthValue;
		Object defaultValue;

		public Field( String fieldKey, boolean using ) {
			this( fieldKey, using, true, null );
		}

		public Field( String fieldKey, boolean using, boolean hasManageAuth ) {
			this( fieldKey, using, hasManageAuth, null );
		}

		public Field( String fieldKey, boolean using, boolean hasManageAuth, Object defaultValue ) {
			this.fieldKey = fieldKey;
			this.usingValue = using;
			this.hasManageAuthValue = hasManageAuth;
			this.defaultValue = defaultValue;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public String getFieldKey() {
			return fieldKey;
		}

		public boolean hasManageAuth() {
			return hasManageAuthValue;
		}

		public boolean using() {
			return usingValue;
		}
	}
}
