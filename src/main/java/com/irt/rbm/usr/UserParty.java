/*
 *	File Name:	UserParty.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										SQLManager 변경사항 적용: fieldMap 삭제
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/13		1.0.0	version 관리
 *	crystal		2002/10/01				create
 *
**/

package com.irt.rbm.usr;

import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class UserParty extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.USER_PARTY );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.USER_PARTY );

	public UserParty( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String partyId ) {
		return Record.createMap( "partyId", partyId );
	}

	private String getFieldValueByName( String partyId, String fieldName ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT "+ fieldName +" FROM USR_PARTY WHERE PARTY_ID = ?", partyId );
	}

	public String getPartyGln( String partyId ) throws SQLException {
		return getFieldValueByName( partyId, "GLN" );
	}

	public String getPartyId( String gln ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT PARTY_ID FROM USR_PARTY WHERE GLN = ?", gln );
	}

	public String getPartyName( String partyId ) throws SQLException {
		return getFieldValueByName( partyId, "PARTY_NAME" );
	}

	public Map<String, Object> getRecordByGln( String gln ) throws SQLException {
		QueryBuffer querybuf = new ConditionQueryBuffer( Record.createMap("gln", gln) );
		return SQLManager.getRecordMap( handler, null, factory.setQuery(querybuf) );
	}

	public Map<String, Object> getRecordByGln( String gln, String[] fieldKeys ) throws SQLException {
		QueryBuffer querybuf = new ConditionQueryBuffer( Record.createMap("gln", gln) );
		return SQLManager.getRecordMap( handler, null, factory.setQuery(querybuf, fieldKeys) );
	}

	public boolean updateStatus( String partyId, String status ) throws DataException, SQLException {
		return( SQLManager.executeStatement( handler,
			"UPDATE USR_PARTY SET STATUS = ?, UPGDATE = SYSDATE WHERE PARTY_ID = ?"
		, status, partyId ) > 0 );
	}
}
