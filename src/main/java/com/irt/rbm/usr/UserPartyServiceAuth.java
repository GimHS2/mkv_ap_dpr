/*
 *	File Name:	UserPartyServiceAuth.javah
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	createPrimary(): java.util.HashMap -> java.util.TreeMap
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	kdmcom		2002/12/20				create
 *
**/

package com.irt.rbm.usr;

import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class UserPartyServiceAuth extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.USER_PARTY_SERVICEAUTH );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.USER_PARTY_SERVICEAUTH );

	public UserPartyServiceAuth( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String partyId, String serviceGroupCode ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "partyId", partyId );
		primaryMap.put( "serviceGroupCode", serviceGroupCode );

		return primaryMap;
	}

	public int delete( String partyId ) throws DataException, SQLException {
		return SQLManager.executeStatement( handler, "DELETE USR_PARTY_SERV WHERE PARTYID = ?", partyId );
	}

	public int getRecordCount( String partyId ) throws SQLException {
		return getRecordCount( Record.createMap("partyId", partyId) );
	}

	public List<Map<String, Object>> getRecords( String partyId ) throws SQLException {
		return getRecords( Record.createMap("partyId", partyId) );
	}

	public List<Map<String, Object>> getRecords( String partyId, String[] fieldKeys ) throws SQLException {
		return getRecords( Record.createMap("partyId", partyId), fieldKeys );
	}

	public boolean updateStatus( String partyId, String serviceGroupCode, String status ) throws DataException, SQLException {
		return( SQLManager.executeStatement( handler,
			"UPDATE USR_PARTY_SERV SET STATUS = ?, UPGDATE = SYSDATE WHERE PARTYID = ? AND SERVGRPCD = ?"
		, status, partyId, serviceGroupCode ) > 0 );
	}
}
