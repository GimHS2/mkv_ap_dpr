/*
 *	File Name:	Schema.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.0	create
**/

package com.irt.cst;

import com.irt.sql.QueryFactory;
import com.irt.sql.Queryable;
import com.irt.sql.Table;

public class Schema extends com.irt.sql.Schema {//@formatter:off

	public final static String CST_MENU						= "CSTMenu";


	private final static Schema schema = new Schema();

	Schema() {
		Table table;
		Table.Field[] tfields;


		/***************************************************************************************************
		 *	CST_MENU
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "menuId",				"MENU_ID",				"CST_MENU_ID",					0, 50 )
			, createFD( PM, "menuLocale",			"MENU_LOCALE",			"CST_MENU_LOCALE",				STRING )
			, createFD( PM, "menuHrcy",				"MENU_HRCY",			"CST_MENU_HIERARCHY",			STRING )
			, createFD( MD, "menuLevel",			"MENU_LEVEL",			"CST_MENU_LEVEL",				INTEGER )
			, createFD( MD, "menuSeq",				"MENU_SEQ",				"CST_MENU_SEQ",					INTEGER )
			, createFD( MD, "menuMessage",			"MENU_MESSAGE",			"CST_MENU_MESSAGE",				STRING )
			, createFD( OP, "menuMessageKey",		"MENU_MESSAGE_KEY",		"CST_MENU_MESSAGEKEY",			STRING )
			, createFD( OP, "menuHref",				"MENU_HREF",			"CST_MENU_HREF",				STRING )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( CST_MENU, table = createTable("CST_MENU", "MNU", tfields, "UPGDATE = SYSDATE") );
	}
//@formatter:on

	public static Queryable findQueryable( String key ) {
		return schema.getQueryable(key);
	}

	public static QueryFactory findQueryFactory( String key ) {
		Queryable queryable = schema.getQueryable(key);
		return ( queryable == null ? null : new QueryFactory(queryable) );
	}

	public static Table findTable( String key ) {
		return schema.getTable(key);
	}
}
