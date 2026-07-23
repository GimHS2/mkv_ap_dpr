/*
 *	File Name:	SalesMov.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/06/30		2.2.2	createPrimary(): dangerousInd 추가
 * 	jbaek		2014/07/13		2.2.1	Sold-to Level 기능 개발
 *	song7981	2013/12/31		2.2.0	create
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
public class SalesMov extends ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_SALES_MOV );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_SALES_MOV );

	public SalesMov ( SQLHandler handler ) {
		super ( handler, table, factory );
	}

	public static Map<String, Object> createPrimary(
			String organizationCode, String distributionChannelCode, String divisionCode, String officeCode, String dangerousInd ) {

		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );
		primaryMap.put( "divisionCode", divisionCode );
		primaryMap.put( "officeCode", officeCode );
		primaryMap.put( "dangerousInd", dangerousInd);

		return primaryMap;
	}
}
