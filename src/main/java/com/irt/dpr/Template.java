/*
 *	File Name:	Template.java
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
import java.sql.*;
import java.util.Map;

/*
 *
 */
public class Template extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_ORDER_TEMPLATE );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ORDER_TEMPLATE );

	public Template( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String templateKey ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "templateKey", templateKey );

		return primaryMap;
	}

	public Object createTemplateKey() throws SQLException {
		return SQLManager.getObjectValue( handler, "SELECT seqDPR_TEMPLATE.nextval from dual" );
	}

	public Object findOrderSoldTo( String orderKey ) throws SQLException {
		return SQLManager.getObjectValue( handler
				, "SELECT DPS.PARTYNAME FROM DPR_PARTY_SALES DPS, DPR_ORDER DOO WHERE DPS.PARTYCD = DOO.SOLD_PARTYCD AND DOO.ORDER_KEY = ?"
				, orderKey );
	}

	public String getName( String templateKey ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler,
			"SELECT TEMPLATE_NAME FROM DPR_TEMPLATE WHERE TEMPLATE_KEY = ?"
			, templateKey );
	}

	public String regist( Map primaryMap, String uniqId ) throws DataException, SQLException {
		CallableStatement cstmt = null;
		try {
			cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkDPRTemplate.fOrderToTemplate( ?, ?, ?, ?, ? ); END;" );
			cstmt.registerOutParameter( 1, Types.INTEGER );
			cstmt.registerOutParameter( 2, Types.VARCHAR );
			cstmt.setString( 3, (String)primaryMap.get("orderKey") );
			cstmt.setString( 4, (String)primaryMap.get("publicInd") );
			cstmt.setString( 5, (String)primaryMap.get("templateName") );
			cstmt.setString( 6, uniqId );
			cstmt.executeUpdate();

			if( cstmt.getInt(1) > 0 )
				return null;
			else
				return cstmt.getString(2);
		} finally {
			if( cstmt != null ) cstmt.close();
		}
    }
}
