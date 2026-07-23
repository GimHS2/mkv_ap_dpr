/*
 *	File Name:	PlantItem.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.3	Sales Office별 Plant Exclusion 기능 추가
 *	hankalam	2017/02/28		2.2.2	전체 Delete 하여 Insert 하는 기능 추가
 *	jbaek		2015/04/07		2.2.1	plantCode 를 PK에 추가
 *	jbaek		2014/02/16		2.2.0	create
**/

package com.irt.dpr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.irt.data.DataLoader;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.sql.*;

/*
	*
	*/
public class PlantItem extends ManipulableManagerImpl{
	private final static Table table = Schema.findTable( Schema.DPR_PLANT_ITEM );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_PLANT_ITEM );

	public final static String PLANTITEM_SHIPPARTY_ALL		= "000000";

	public final static String PLANTITEMSTATUS_NORMAL		= "00";
	public final static String PLANTITEMSTATUS_EXCLUDE		= "99";

	public PlantItem ( SQLHandler handler ) {
		super ( handler, table, factory );
	}

	public DataLoader.Loader createDataLoader( String[] fieldKeys, final Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
			, int statementType, String uploadType ) throws SQLException {
		PreparedStatement pstmt_del = null;
		if( "DEL".equals(uploadType) ) {
			try {
				Connection conn = handler.getConnection();
				pstmt_del = conn.prepareStatement( "DELETE FROM DPR_PLANT_ITEM" );
				pstmt_del.executeUpdate();
			} finally { if( pstmt_del != null ) pstmt_del.close(); }
		}

		return super.createDataLoader( fieldKeys, defaultMap, updateFieldKeys, statementType );
	}

	public static Map<String, Object> createPrimary( String plantCode, String shipPartyCode, String officeCode, String organizationCode, String distributionChannelCode, String itemCode ) {
		Map<String, Object>primaryMap = new java.util.TreeMap<String, Object> ();
		primaryMap.put( "plantCode", plantCode );
		primaryMap.put( "shipPartyCode", shipPartyCode );
		primaryMap.put( "officeCode", officeCode );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );
		primaryMap.put( "itemCode", itemCode );

		return primaryMap;
	}
}
