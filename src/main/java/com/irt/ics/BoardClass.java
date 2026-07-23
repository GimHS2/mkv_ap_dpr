/*
 *	File Name:	BoardClass.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/09/30		2.2.2	QueryableManagerImpl.setPrimaryConditionQuery() 사용
 *	stghr12		2010/07/31		2.2.1	version up
 *	lsinji		2009/10/25		2.2.0	create
 *
**/

package com.irt.ics;

import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class BoardClass extends com.irt.rbm.QueryableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ICS_BOARDCLASS );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ICS_BOARDCLASS );

	public BoardClass( SQLHandler handler ) {
		super( handler, factory );
	}

	public static Map<String, Object> createPrimary( String boardClassCode ) {
		return com.irt.data.Record.createMap( "boardClassCode", boardClassCode );
	}

	public String getName( String boardClassCode ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT BOARDCLASS_NAME FROM ICS_BOARDCLASS WHERE BOARDCLASS_CD = ?", boardClassCode );
	}

	@Override
	protected QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) {
		return setPrimaryConditionQuery( table, querybuf );
	}
}
