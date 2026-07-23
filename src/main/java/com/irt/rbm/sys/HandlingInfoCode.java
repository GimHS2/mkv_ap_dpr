/*
 *	File Name:	HandlingInfoCode.java
 *	Version:	2.0.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	create
 *
**/

package com.irt.rbm.sys;

import com.irt.sql.*;

/**
 *
 */
public class HandlingInfoCode extends StandardCode {
	private final static Table table = Schema.findTable( Schema.SYS_HANDLINGINFO );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_HANDLINGINFO );

	public HandlingInfoCode( SQLHandler handler ) {
		super( handler, table, factory );
	}
}
