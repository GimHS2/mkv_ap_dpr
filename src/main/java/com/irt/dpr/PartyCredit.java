/*
 *	File Name:	PartyCredit.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/*
 *
 */
public class PartyCredit extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_PARTY_CREDIT );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_PARTY_CREDIT );

	public PartyCredit( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String soldPartyCode, String creditPartyCode, String organizationCode, String distributionChannelCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "soldPartyCode", soldPartyCode );
		primaryMap.put( "creditPartyCode", creditPartyCode );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );

		return primaryMap;
	}
}
