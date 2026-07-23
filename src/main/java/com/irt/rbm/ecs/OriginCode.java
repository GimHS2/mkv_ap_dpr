/*
 *	File Name:	OriginCode.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/02/28		2.0.0	create
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
public class OriginCode extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ECS_ORIGIN );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_ORIGIN );

	public OriginCode( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String code ) {
		return Record.createMap( "code", code );
	}

	public String getName( String code ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT ORIGIN_NAME FROM ECS_ORIGIN WHERE ORIGIN_CD = ?", code );
	}
}
