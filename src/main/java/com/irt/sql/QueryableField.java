/*
 *	File Name:	QueryableField.java
 *	Version:	2.0.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

/**
 * QueryBuffer.appendData에 들어가는 Field.
 */
public interface QueryableField extends com.irt.data.AbstractField {
	/**
	 * ConditionQueryBuffer에서 필드에 해당하는 조건을 찾아서 querybuf에 조건 추가.
	 * @return 조건이 추가되었는지 여부를 return.
	 */
	public boolean appendCondition( ConditionQueryBuffer querybuf );

	/**
	 * querybuf에 데이터(필드) 추가.
	 * @return 데이터를 추가하면 true return. (이미 추가되어 있는 경우 false)
	 */
	public boolean appendData( QueryBuffer querybuf );
}
