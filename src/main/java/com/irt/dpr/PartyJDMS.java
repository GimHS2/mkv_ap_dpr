/*
 *	File Name:	PartyJDMS.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Collection;
import java.util.Map;

/*
 *
 */
public class PartyJDMS extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_PARTY_JDMS );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_PARTY_JDMS );

	public PartyJDMS( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary(
			String partyCode, String organizationCode, String distributionChannelCode, String divisionCode ) {

		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "partyCode", partyCode );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );
		primaryMap.put( "divisionCode", divisionCode );

		return primaryMap;
	}

	public DataResult deleteEachAll( Collection<Map<String, Object>> records ) throws SQLException {
		return deleteEach( records );
	}

	public DataResult registEachAll( Collection<Map<String, Object>> records ) throws SQLException {
		return registEach( records );
	}

	protected DataResult registEach( Collection<Map<String, Object>> records ) throws SQLException {
		DataResult result = new DataResult();

		int idx = 0;
		for( java.util.Iterator<Map<String, Object>> iterator = records.iterator(); iterator.hasNext(); idx++ ) {
			Map<String, Object> recordMap = iterator.next();

			try {
				if( regist(recordMap) )
					result.increaseRegistCount();
				else
					result.increaseErrorCount();
			} catch( DataException dataEx ) {
				result.appendError( new DataException(idx, dataEx) );
			}
		}

		return result;
	}
}
