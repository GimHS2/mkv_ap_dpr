/*
 *	File Name:	ICategoryCode.java
 *	Version:	2.1.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/04/30		2.1.0	getCodeField_static(), useCategoryCode() 추가
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	create
 *
**/

package com.irt.rbm.sys;

import com.irt.sql.*;

/**
 *
 */
public class ICategoryCode extends ClassCode {
	private final static Table table = Schema.findTable( Schema.SYS_ICATEGORY );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_ICATEGORY );
	private final static HierarchyCodeField codeField = (HierarchyCodeField)table.getField( "code" );

	public ICategoryCode( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static HierarchyCodeField getCodeField_static() {
		return codeField;
	}

	public static boolean useCategoryCode() {
		return com.irt.rbm.RBMSystem.getSystemEnvBool( "SYS", "CateEnv;UseICate", true );
	}
}
