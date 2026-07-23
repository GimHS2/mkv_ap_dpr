/*
 *	File Name:	SalesMovPty.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/06/30		2.2.1	createPrimary(): dangerousInd 추가
 *	jbaek		2014/07/13		2.2.0	create
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
public class SalesMovPty extends ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_SALES_MOVPTY );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_SALES_MOVPTY );

	public SalesMovPty ( SQLHandler handler ) {
		super ( handler, table, factory );
	}

	public static Map<String, Object> createPrimary(
			String organizationCode, String distributionChannelCode, String divisionCode, String officeCode, String partyCode, String dangerousInd ) {

		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );
		primaryMap.put( "divisionCode", divisionCode );
		primaryMap.put( "officeCode", officeCode );
		primaryMap.put( "partyCode", partyCode );
		primaryMap.put( "dangerousInd", dangerousInd );

		return primaryMap;
	}
}
