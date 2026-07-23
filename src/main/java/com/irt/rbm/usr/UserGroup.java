/*
 *	File Name:	UserGroup.java
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
 *	stghr12		2004/05/04		1.0.0	create
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
public class UserGroup extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.USER_GROUP );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.USER_GROUP );

	public UserGroup( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String partyId, String groupId ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "partyId", partyId );
		primaryMap.put( "groupId", groupId );

		return primaryMap;
	}

	public int delete( String partyId ) throws DataException, SQLException {
		return SQLManager.executeStatement( handler, "DELETE USR_GROUP WHERE PARTYID = ?", partyId );
	}

	public String getGroupName( String partyId, String groupId ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT GROUP_NAME FROM USR_GROUP WHERE PARTYID = ? AND GROUP_ID = ?", partyId, groupId );
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
}
