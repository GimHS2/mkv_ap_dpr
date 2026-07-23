/*
 *	File Name:	SellerCode.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	createPrimary(): java.util.HashMap -> java.util.TreeMap
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.sql.*;
import java.util.Map;

/**
 *
 */
public class SellerCode extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ECS_TRADEPARTNER_SELLERCODE );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_TRADEPARTNER_SELLERCODE );

	public SellerCode( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String buyerGln, String sellerGln, String sellerPartyCode ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "buyerGln", buyerGln );
		primaryMap.put( "sellerGln", sellerGln );
		primaryMap.put( "sellerPartyCode", sellerPartyCode );

		return primaryMap;
	}
}
