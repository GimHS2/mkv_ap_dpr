/*
 *	File Name:	Queryable.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	getQueryableFieldArray() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

import java.util.Map;

/**
 *
 */
public interface Queryable extends Joinable {
	/**
	 * querybuf에 조건 추가.
	 * @return 조건이 추가되었는지 여부를 return.
	 */
	public boolean appendCondition( ConditionQueryBuffer querybuf );

	public boolean appendTable( QueryBuffer querybuf );

	public boolean existTable( QueryBuffer querybuf );

	/**
	 * fieldKey에 해당하는 QueryableField return.
	 */
	public QueryableField getQueryableField( String fieldKey );

	/**
	 * fieldKeys에 해당하는 QueryableField Array를 return.
	 */
	public QueryableField[] getQueryableFieldArray( String... fieldKeys ) throws IllegalArgumentException;

	/**
	 * 모든 QueryableField이 포함된 Map return.
	 */
	public Map<String, ? extends QueryableField> getQueryableFieldMap();
}
