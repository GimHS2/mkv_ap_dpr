/*
 *	File Name:	AbstractFieldSet.java
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
public interface AbstractFieldSet {
	public AbstractField getField( String fieldKey );

	public AbstractField[] getFieldArray();

	public String[] getFieldKeyArray();

	public Map<String, ? extends AbstractField> getFieldMap();
}
