/*
 *	File Name:	StockQueryManage.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/12/31		2.2.0	create
 *
**/

package com.irt.dpr;

import java.util.Map;

import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;

/**
 *
 */
public class StockQueryManage extends com.irt.rbm.ManipulableManagerImpl {//@formatter:on
	private final static Table table = Schema.findTable( Schema.DPR_STOCK_QUERY_MNG );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_STOCK_QUERY_MNG );

	public final static String DEFAULT_AUTO_RETRY		= "N";

	public StockQueryManage( SQLHandler handler ) {
		super(handler, table, factory);
	}

	public static Map<String, Object> createPrimary( String countryCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "countryCode", countryCode );

		return primaryMap;
	}

}
