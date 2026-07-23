/*
 *	File Name:	PartyEnvironment.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class PartyEnvironment extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ECS_PARTY_ENV );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_PARTY_ENV );

	public PartyEnvironment( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String gln ) {
		return Record.createMap( "gln", gln );
	}

	public boolean usePartyCategory( String gln ) throws SQLException {
		return( SQLManager.getObjectValue( handler, "SELECT CATE_LEN FROM ECS_PARTY_ENV WHERE GLN = ?", gln ) != null );
	}
}
