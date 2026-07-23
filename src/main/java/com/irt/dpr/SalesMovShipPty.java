/*
 *	File Name:	SalesMovShipPty.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/03/31		2.2.0	create
 *
**/

package com.irt.dpr;

import java.util.Map;

import com.irt.rbm.ManipulableManagerImpl;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;

/*
	*
	*/
public class SalesMovShipPty extends ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_SALES_MOVSPTY );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_SALES_MOVSPTY );

	public SalesMovShipPty ( SQLHandler handler ) {
		super ( handler, table, factory );
	}

	public static Map<String, Object> createPrimary(
			String organizationCode, String distributionChannelCode, String divisionCode, String shipPartyCode, String dangerousInd ) {

		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );
		primaryMap.put( "divisionCode", divisionCode );
		primaryMap.put( "shipPartyCode", shipPartyCode );
		primaryMap.put( "dangerousInd", dangerousInd );

		return primaryMap;
	}
}
