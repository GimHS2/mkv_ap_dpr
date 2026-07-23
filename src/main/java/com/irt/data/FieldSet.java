/*
 *	File Name:	FieldSet.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	getFieldKeyArray() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

import java.util.Map;

/**
 *
 */
public class FieldSet implements AbstractFieldSet {
	Map<String, ? extends AbstractField> fieldMap;

	public FieldSet( Map<String, ? extends AbstractField> fieldMap ) {
		this.fieldMap = java.util.Collections.unmodifiableMap( fieldMap );
	}

	public AbstractField getField( String key ) {
		return fieldMap.get( key );
	}

	public AbstractField[] getFieldArray() {
		return fieldMap.values().toArray( new AbstractField[fieldMap.size()] );
	}

	public String[] getFieldKeyArray() {
		String[] fieldKeys = new String[ fieldMap.size() ];

		int k = 0;
		for( AbstractField field : fieldMap.values() )
			fieldKeys[k++] = field.getFieldKey();

		return fieldKeys;
	}

	public Map<String, ? extends AbstractField> getFieldMap() {
		return fieldMap;
	}
}
