/*
 *	File Name:	TimeZone.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.rbm.sys;

import com.irt.sql.*;

/**
 *
 */
public class TimeZone extends StandardCode {
	private final static Table table = Schema.findTable( Schema.SYS_TIMEZONE );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_TIMEZONE );

	public TimeZone( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public final static boolean usingTimeZone() {
		return com.irt.rbm.RBMSystem.getSystemEnvBool( "SYS", "TimeZone;UsingTimeZone", false );
	}
}
