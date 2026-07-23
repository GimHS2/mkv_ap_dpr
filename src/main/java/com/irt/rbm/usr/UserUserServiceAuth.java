/*
 *	File Name:	UserUserServiceAuth.javah
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	createPrimary(), getRecordCount(), getRecords(): java.util.HashMap -> java.util.TreeMap
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	kdmcom		2002/12/20				create
 *
**/

package com.irt.rbm.usr;

import com.irt.data.DataException;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class UserUserServiceAuth extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.USER_USER_SERVICEAUTH );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.USER_USER_SERVICEAUTH );

	public UserUserServiceAuth( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String partyId, String userId, String serviceGroupCode ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "partyId", partyId );
		primaryMap.put( "userId", userId );
		primaryMap.put( "serviceGroupCode", serviceGroupCode );

		return primaryMap;
	}

	public int delete( String partyId, String userId ) throws DataException, SQLException {
		return SQLManager.executeStatement( handler, "DELETE USR_USER_AUTH WHERE PARTYID = ? AND USERID = ?", partyId, userId );
	}

	public int getRecordCount( String partyId, String userId ) throws SQLException {
		Map<String, Object> conditionMap = new java.util.TreeMap<String, Object>();

		conditionMap.put( "partyId", partyId );
		conditionMap.put( "userId", userId );

		return getRecordCount( conditionMap );
	}

	public List<Map<String, Object>> getRecords( String partyId, String userId ) throws SQLException {
		Map<String, Object> conditionMap = new java.util.TreeMap<String, Object>();

		conditionMap.put( "partyId", partyId );
		conditionMap.put( "userId", userId );

		return getRecords( conditionMap );
	}

	public List<Map<String, Object>> getRecords( String partyId, String userId, String[] fieldKeys ) throws SQLException {
		Map<String, Object> conditionMap = new java.util.TreeMap<String, Object>();

		conditionMap.put( "partyId", partyId );
		conditionMap.put( "userId", userId );

		return getRecords( conditionMap, fieldKeys );
	}

	public int registAllService( String partyId, String userId ) throws DataException, SQLException {
		return SQLManager.executeStatement( handler,
			"INSERT INTO USR_USER_AUTH( PARTYID, USERID, SERVGRPCD ) ( SELECT PARTYID, ?, SERVGRPCD FROM USR_PARTY_SERV WHERE PARTYID = ? )"
		, userId, partyId );
	}
}
