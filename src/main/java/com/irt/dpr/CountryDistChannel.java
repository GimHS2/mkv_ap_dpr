/*
 *	File Name:	CountryDistChannel.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2009/01/06		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class CountryDistChannel extends com.irt.rbm.ManipulableManagerImpl {
	public final static String CONDITION_INDICATOR_REGISTRED		= "00";
	public final static String CONDITION_INDICATOR_NONE				= "99";

	private final static Table table = Schema.findTable( Schema.DPR_COUNTRY_DIST );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_COUNTRY_DIST );
		
	public CountryDistChannel( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String countryCode, String organizationCode, String distributionChannelCode ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object> ();
		primaryMap.put( "countryCode", countryCode );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );

		return primaryMap;
	}

	public String[] getDistributionChannels( String countryCode, String organizationCode ) throws SQLException {
		return SQLManager.getStringValues( handler, 
				"SELECT DIST_CHANNELCD FROM DPR_COUNTRY_DIST CDIS WHERE COUNTRYCD = ? AND ORGANIZATIONCD = ?"
				, new Object[] { countryCode, organizationCode } );
	}
}
