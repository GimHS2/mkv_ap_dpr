/*
 *	File Name:	FileClass.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	stghr12		2002/04/15				create
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
public class FileClass extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ECS_FILECLASS );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_FILECLASS );

	public FileClass( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String code ) {
		return Record.createMap( "code", code );
	}

	public String getName( String code ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT FILECLASS_NAME FROM ECS_FILECLASS WHERE FILECLASS_CD = ?", code );
	}
}
