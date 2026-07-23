/*
 *	File Name:	BankCode.java
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
public class BankCode extends StandardCode {
	private final static Table table = Schema.findTable( Schema.SYS_BANK );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_BANK );

	public BankCode( SQLHandler handler ) {
		super( handler, table, factory );
	}
}
