/*
 *	File Name:	StoreAttrFactor1.java
 *	Version:	2.1.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/04/30		2.1.0	useStoreAttr() 추가
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.rbm.RBMSystem;
import com.irt.sql.*;

/**
 *
 */
public class StoreAttrFactor1 extends StoreAttr {
	private final static Table table = Schema.findTable( Schema.ECS_ST_FACTOR1 );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_ST_FACTOR1 );

	public StoreAttrFactor1( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static boolean useStoreAttr() {
		return ( RBMSystem.getSystemEnv("ECS", "StoreFactor;Name1") != null );
	}
}
