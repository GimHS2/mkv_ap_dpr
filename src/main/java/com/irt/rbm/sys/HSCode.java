/*
 *	File Name:	HSCode.java
 *	Version:	2.1.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/04/30		2.1.0	getCodeField_static() 추가
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	create
 *
**/

package com.irt.rbm.sys;

import com.irt.sql.*;

/**
 *
 */
public class HSCode extends ClassCode {
	private final static Table table = Schema.findTable( Schema.SYS_HS );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_HS );
	private final static HierarchyCodeField codeField = (HierarchyCodeField)table.getField( "code" );

	public HSCode( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static HierarchyCodeField getCodeField_static() {
		return codeField;
	}
}
