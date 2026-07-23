/*
 *	File Name:	ValidableFieldSet.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	getFieldKeyArray(), replaceValues() 추가
 *										생성자 ValidableFieldSet(Map) 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

import java.util.Map;

/**
 *
 */
public class ValidableFieldSet implements AbstractFieldSet {
	ValidableField[] fields;
	Map<String, ValidableField> fieldMap;

	public ValidableFieldSet( Map<String, ValidableField> fieldMap ) {
		this.fields = fieldMap.values().toArray( new ValidableField[fieldMap.size()] );
		this.fieldMap = java.util.Collections.unmodifiableMap( fieldMap );
	}

	public ValidableFieldSet( ValidableField[] fields ) {
		System.arraycopy( fields, 0, this.fields = new ValidableField[fields.length], 0, fields.length );
		this.fieldMap = new java.util.LinkedHashMap<String, ValidableField>( fields.length );
		for( int f = 0; f < fields.length; f++ )
			fieldMap.put( fields[f].fieldKey, fields[f] );
		this.fieldMap = java.util.Collections.unmodifiableMap( this.fieldMap );
	}

	public AbstractField getField( String key ) {
		return fieldMap.get( key );
	}

	public AbstractField[] getFieldArray() {
		ValidableField[] fields_new = new ValidableField[ fields.length ];
		System.arraycopy( fields, 0, fields_new, 0, fields.length );
		return fields_new;
	}

	public String[] getFieldKeyArray() {
		String[] fieldKeys = new String[ fields.length ];
		for( int k = 0; k < fieldKeys.length; k++ )
			fieldKeys[k] = fields[k].getFieldKey();

		return fieldKeys;
	}

	public Map<String, ? extends ValidableField> getFieldMap() {
		return fieldMap;
	}

	/**
	 * recordMap에 들어있는 field들을 validate한 후, convert된 object로 recordMap에 put한다.
	 */
	public Object[] replaceValues( Map<String, Object> recordMap ) throws FieldException {
		Object[] values = new Object[ fieldMap.size() ];

		int f = 0;
		for( ValidableField field : fieldMap.values() ) {
			values[f] = field.validate( recordMap );
			if( values[f] != null ) recordMap.put( field.getFieldKey(), values[f] );
			f++;
		}

		return values;
	}

	public Object[] validate( Map<String, ? extends Object> recordMap ) throws FieldException {
		Object[] values = new Object[ fieldMap.size() ];

		int f = 0;
		for( ValidableField field : fieldMap.values() )
			values[f++] = field.validate( recordMap );

		return values;
	}
}
