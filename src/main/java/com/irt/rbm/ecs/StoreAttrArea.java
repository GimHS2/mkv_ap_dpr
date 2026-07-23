/*
 *	File Name:	StoreAttrArea.java
 *	Version:	2.1.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2006/12/01		2.1.0	delete(), regist(): StoreAttrMultiLevel로 이동
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.sql.*;

/**
 *
 */
public class StoreAttrArea extends StoreAttrMultiLevel {
	private final static Table table = Schema.findTable( Schema.ECS_ST_AREA );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_ST_AREA );
	private final static HierarchyCodeField codeField = (HierarchyCodeField)table.getField( "code" );

	public StoreAttrArea( SQLHandler handler ) {
		super( handler, table, factory, codeField );
	}
}
