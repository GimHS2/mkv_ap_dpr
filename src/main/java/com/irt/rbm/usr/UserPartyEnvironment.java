/*
 *	File Name:	UserPartyEnvironment.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.0	create
 *
**/

package com.irt.rbm.usr;

import com.irt.data.DataException;
import com.irt.sql.*;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 *
 */
public class UserPartyEnvironment extends UserEnvironment {
	SQLHandler handler;
	String partyId;

	public UserPartyEnvironment( SQLHandler handler, String partyId ) {
		super( handler );
		this.handler = handler;
		this.partyId = partyId;
	}

	public String handleGetValue( String systemCode, String key ) throws SQLException {
		CallableStatement cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkUSRData.fGetPartyEnv(?, ?, ?); END;" );
		try {
			cstmt.registerOutParameter( 1, Types.VARCHAR );
			cstmt.setString( 2, partyId );
			cstmt.setString( 3, systemCode );
			cstmt.setString( 4, key );
			cstmt.executeUpdate();

			return cstmt.getString(1);
		} finally {
			try { cstmt.close(); } catch( Exception ex ) {}
		}
	}

	public void removeValue( String systemCode, String key ) throws DataException, SQLException {
		SQLManager.callStatement( handler, "call pkUSRData.pRemovePartyEnv(?, ?, ?)", partyId, systemCode, key );
	}

	public void setValue( String systemCode, String key, String value ) throws DataException, SQLException {
		SQLManager.callStatement( handler, "call pkUSRData.pPutPartyEnv(?, ?, ?, ?)", partyId, systemCode, key, value );
	}
}
