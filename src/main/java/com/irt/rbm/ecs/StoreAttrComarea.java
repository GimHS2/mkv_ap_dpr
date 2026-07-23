/*
 *	File Name:	StoreAttrComarea.java
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

package com.irt.rbm.ecs;

import com.irt.sql.*;

/**
 *
 */
public class StoreAttrComarea extends StoreAttr {
	private final static Table table = Schema.findTable( Schema.ECS_ST_COMAREA );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_ST_COMAREA );

	public StoreAttrComarea( SQLHandler handler ) {
		super( handler, table, factory );
	}
}
