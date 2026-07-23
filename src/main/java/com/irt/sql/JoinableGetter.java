/*
 *	File Name:	JoinableGetter.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/08/31		2.2.0	create
 *
**/

package com.irt.sql;

/**
 * InnerQueryBuffer를 사용하는 Joinable Class
 */
public interface JoinableGetter {
	public Joinable getJoinable( String... fieldKeys );
}
