/*
 *	File Name:	Joinable.java
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
 * QueryBuffer.appendTable에 들어가는 테이블.
 */
public interface Joinable {
	/**
	 * querybuf에 테이블을 추가.
	 * @return 테이블을 추가하면 true return. (이미 추가되어 있는 경우 false)
	 */
	public boolean appendTable( QueryBuffer querybuf );

	/**
	 * querybuf에 테이블이 추가되었는지 검사.
	 * @return querybuf에 테이블이 포함되어 있는지 여부 return.
	 */
	public boolean existTable( QueryBuffer querybuf );
}
