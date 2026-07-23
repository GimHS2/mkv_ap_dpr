/*
 *	File Name:	ItemStatus.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/12/31		2.2.1	delete() 추가
 *	hankalam	2017/04/28		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.sql.*;
import java.sql.*;
import java.util.Map;

/**
	*
	*/
public class ItemStatus extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.vwDPR_ORDER_ITEM );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.vwDPR_ORDER_ITEM );

	public ItemStatus( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String organizationCode, String distributionChannelCode, String divisionCode, String partyCode, String itemCode ) {
		Map<String, Object> map = new java.util.HashMap<String, Object>();

		map.put( "organizationCode", organizationCode );
		map.put( "distributionChannelCode", distributionChannelCode );
		map.put( "divisionCode", divisionCode );
		map.put( "partyCode", partyCode );
		map.put( "itemCode", itemCode );

		return map;
	}

	public int delete( String organizationCode, String distributionChannelCode, String divisionCode
					, String countryCode, String partyCode, String itemCode ) throws DataException, SQLException {

		SQLManager.callStatement( handler, "call pkCustom.pDeleteOrderItem( ?, ?, ?, ?, ?)"
				, organizationCode, distributionChannelCode, divisionCode, partyCode, itemCode );

		String orderItemQuery = "DELETE DPR_ORDER_ITEM WHERE PARTYCD = ? AND ORGANIZATIONCD = ? AND DIST_CHANNELCD = ? AND DIVISIONCD = ? AND ITEMCD = ?";
		Object[] orderItemBindVars = { partyCode, organizationCode, distributionChannelCode, divisionCode, itemCode };
		int delCount = SQLManager.executeStatement( handler, orderItemQuery, orderItemBindVars );

		boolean existItem = SQLManager.getInt( handler, "SELECT COUNT(*) FROM DPR_ORDER_ITEM WHERE ORGANIZATIONCD = ? AND ITEMCD = ?"
				, organizationCode, itemCode ) > 0;

		if( !existItem ) {
			String itemQuery = "DELETE DPR_ITEM WHERE COUNTRYCD = ? AND ORGANIZATIONCD = ? AND ITEMCD = ?";
			Object[] itemBindVars = { countryCode, organizationCode, itemCode };
			SQLManager.executeStatement( handler, itemQuery, itemBindVars );
		}

		return delCount;
	}

	public Map<String, Object> getRecordCountWithPlant( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException {
		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		factory.setDataQuery( querybuf, fieldKeys );
		//if( setPrimaryConditionQuery(querybuf) == null ) return null;

		return SQLManager.getRecordMap( handler, null, querybuf );
	}


	public int getSSLRecordCount( Map<String, ? extends Object> conditionMap ) throws SQLException {
		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), new String[] { "sslCount" } );
		return SQLManager.getInt( handler, querybuf );
	}
}
