/*
 *	File Name:	PostalCode.java
 *	Version:	2.0.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	version 관리
 *	juchul		2003/02/19				create
 *
**/

package com.irt.rbm.sys;

import com.irt.sql.*;

/**
 *
 */
public class PostalCode extends com.irt.rbm.QueryableManagerImpl {
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_POSTALCODE );

	public PostalCode( SQLHandler handler ) {
		super( handler, factory );
	}
}
