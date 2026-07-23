/*
 *	File Name:	ItemPrice.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2019/06/30		2.2.1	Distribution, Group, Party 적용
 *	song7981	2016/05/20		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.sql.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/*
	*
	*/
public class ItemPrice extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_ITEM_PRICE );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ITEM_PRICE );

	public ItemPrice( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public com.irt.data.DataLoader.Validator createValidator( Map<String, Object> defaultMap ) {
		return new OrderItem(handler).createSellingSkuValidator((Map<String, Object>)defaultMap);
	}

	@Override
	public DataLoader.Loader createDataLoader( String[] fieldKeys, final Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
			, int statementType ) throws SQLException {
		PreparedStatement pstmt_del = null;
		try {
			Connection conn = handler.getConnection();
			pstmt_del = conn.prepareStatement( "DELETE FROM DPR_ITEM_PRICE WHERE ORGANIZATIONCD = ?" );
			pstmt_del.setString( 1, (String) defaultMap.get("organizationCode") );
			pstmt_del.executeUpdate();
		} finally { if( pstmt_del != null ) pstmt_del.close(); }

		return new TableDataLoader( fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType ) {
			@Override
			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				Map<String,Object> processedMap = super.processLine(handler, recordMap);

				String distributionChannelCode = (String)processedMap.get( "distributionChannelCode" );
				String officeCode = (String)processedMap.get( "officeCode" );
				String groupCode = (String)processedMap.get( "groupCode" );
				String partyCode = (String)processedMap.get( "partyCode" );
				if( distributionChannelCode == null || distributionChannelCode.trim().length() < 1 ) {
					processedMap.put( "distributionChannelCode", "0" );
				}
				if( officeCode == null || officeCode.trim().length() < 1 ) {
					processedMap.put( "officeCode", "0" );
				}
				if( groupCode == null || groupCode.trim().length() < 1 ) {
					processedMap.put( "groupCode", "0" );
				}
				if( partyCode == null || partyCode.trim().length() < 1 ) {
					processedMap.put( "partyCode", "0" );
				}
				return processedMap;
			}
		};
	}

	public static Map<String, Object> createPrimary( String organizationCode, String distributionChannelCode
			, String officeCode, String groupCode, String partyCode, String itemCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );
		primaryMap.put( "officeCode", officeCode );
		primaryMap.put( "groupCode", groupCode );
		primaryMap.put( "partyCode", partyCode );
		primaryMap.put( "itemCode", itemCode );

		return primaryMap;
	}

	public int deleteAll( String organizationCode ) throws DataException, SQLException {
		int count = SQLManager.executeStatement( handler,
						"DELETE DPR_ITEM_PRICE WHERE ORGANIZATIONCD = ?", organizationCode );

		return count;
	}

}
