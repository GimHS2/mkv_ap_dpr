/*
 *	File Name:	RddIndicator.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2019/06/28		2.2.0		create
 *
**/

package com.irt.dpr;

import com.irt.data.DataLoader;
import com.irt.sql.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/*
	*
	*/
public class RddIndicator extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_RDD_IND );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_RDD_IND );
	public final static String[] notIncTransport = new String[] { "JEJU" };

	public RddIndicator( SQLHandler handler ) {
		super( handler, table, factory );
	}

	@Override
	public DataLoader.Loader createDataLoader( String[] fieldKeys, final Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
			, int statementType ) throws SQLException {
		PreparedStatement pstmt_del = null;
		try {
			Connection conn = handler.getConnection();
			pstmt_del = conn.prepareStatement( "DELETE FROM DPR_RDD_IND" );
			pstmt_del.executeUpdate();
		} finally { if( pstmt_del != null ) pstmt_del.close(); }

		return super.createDataLoader( fieldKeys, defaultMap, updateFieldKeys, statementType );
	}

	public static Map<String, Object> createPrimary( String organizationCode, String distributionChannelCode, String soldPartyCode, String shipPartyCode ) {
		Map<String, Object> primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );
		primaryMap.put( "partyCode", soldPartyCode );
		primaryMap.put( "shipPartyCode", shipPartyCode );

		return primaryMap;
	}
/*
	public boolean isPredefined( String organizationCode, String distributionChannelCode, String soldPartyCode, String shipPartyCode ) throws IllegalArgumentException, SQLException {
		Map<String, Object> primaryMap = new java.util.HashMap<String, Object> ();
		if( shipPartyCode == null || shipPartyCode.length() == 0 ) {
			shipPartyCode = "0";
		}
		primaryMap = createPrimary( organizationCode, distributionChannelCode, soldPartyCode, shipPartyCode );
		String usePredefined = (String)getFieldValue( primaryMap, "usePreDefined" );
		if( !"0".equals(shipPartyCode) && (usePredefined == null || usePredefined.length() == 0) ) {
			primaryMap = createPrimary( organizationCode, distributionChannelCode, soldPartyCode, "0" );
			usePredefined = (String)getFieldValue( primaryMap, "usePreDefined" );
		}

		return "Y".equals(usePredefined);
	}
*/
	public boolean isPredefined( String organizationCode, String distributionChannelCode, String divisionCode, String partyCode ) throws IllegalArgumentException, SQLException {
		Map<String, Object> primaryMap = Party.createPrimary( partyCode, organizationCode, distributionChannelCode, divisionCode );
		String transportZone = (String) new Party( handler ).getFieldValue( primaryMap, "transportZone" );
		transportZone = transportZone != null && transportZone.length() > 0 ? transportZone.toUpperCase() : transportZone;
		for( String s : notIncTransport ) {
			if( s.equals(transportZone) ) {
				return false;
			}
		}
		return true;
	}
}
