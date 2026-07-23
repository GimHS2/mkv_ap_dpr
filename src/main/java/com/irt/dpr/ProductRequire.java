/*
 *	File Name:	ProductRequire.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2017/08/31		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.sql.*;

import java.sql.SQLException;
import java.util.Map;

/*
 *
 */
public class ProductRequire extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_PRODUCT_REQ );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_PRODUCT_REQ );

	public ProductRequire( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( Object productReqKey, String orderDate, String soldPartyCode, String shipPartyCode, String itemCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "productReqKey", productReqKey );
		primaryMap.put( "orderDate", orderDate );
		primaryMap.put( "partyCode", soldPartyCode );
		primaryMap.put( "shipPartyCode", shipPartyCode );
		primaryMap.put( "itemCode", itemCode );

		return primaryMap;
	}

	public Object createProductReqKey() throws SQLException {
		return SQLManager.getObjectValue( handler, "SELECT seqDPR_PRODUCTREQ.nextval from dual" );
	}
}
