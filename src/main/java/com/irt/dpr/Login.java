/*
 *	File Name:	Login.java
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
import java.util.Map;

/*
 *
 */
public class Login extends com.irt.rbm.RBMDataManager {
	public Login( SQLHandler handler ) {
		super( handler );
	}

	public int getPassChangePasswordDays( String uniqId ) throws SQLException {
		return SQLManager.getInt( handler
				, "SELECT NVL(SYSDATE - USR.PASSWORD_CHGDATETIME, 90) FROM USR_USER USR WHERE USR.UNIQID = ?", uniqId );
	}

	public String[] getUserPartyId( String userId ) throws SQLException {
		if( userId == null ) return null;

		List<String> parties = new java.util.ArrayList<String>();
		java.sql.PreparedStatement pstmt = null;
		java.sql.ResultSet rset = null;
		try {
			pstmt = handler.getConnection().prepareStatement( "SELECT * FROM vwUSR_USER WHERE USERID = ?" );
			pstmt.setString( 1, userId );
			rset = pstmt.executeQuery();

			while( rset.next() ) {
				parties.add( rset.getString("PARTYID") );
			}

			if( parties.size() > 0 ) {
				String[] partyId = new String[ parties.size() ];
				parties.toArray(  partyId );

				return partyId;
			}
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}

		return null;
	}
}
