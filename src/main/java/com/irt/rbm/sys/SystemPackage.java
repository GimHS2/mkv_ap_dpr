/*
 *	File Name:	SystemPackage.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	RBMDataManager.appendOrderBy 변경사항 반영
 *	stghr12		2006/02/28		2.0.0	version up(SystemPkg -> SystemPackage)
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	crystal		2002/10/15				create
 *
**/

package com.irt.rbm.sys;

import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SystemPackage extends com.irt.rbm.QueryableManagerImpl {
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_SYSTEM_PACKAGE );

	public SystemPackage( SQLHandler handler ) {
		super( handler, factory );
	}

	protected boolean appendOrderBy( QueryBuffer querybuf ) {
		return appendOrderBy( querybuf, factory );
	}

	protected boolean appendOrderBy( QueryBuffer querybuf, QueryFactory factory ) {
		if( super.appendOrderBy(querybuf, factory) )
			return true;
		else if( querybuf.existTableAlias("PKG") ) {
			querybuf.appendOrderBy( "PKG.SYSTEMCD, PKG.PKG_CD" );
			return true;
		} else
			return false;
	}

	public int getRecordCount( String systemCode ) throws SQLException {
		return SQLManager.getInt( handler, "SELECT COUNT(*) FROM SYS_SYSTEM_PKG WHERE SYSTEMCD = ?", systemCode );
	}

	public List<Map<String, Object>> getRecords( String systemCode ) throws SQLException {
		return getRecords( Record.createMap("systemCode", systemCode), 0, -1 );
	}

	public List<Map<String, Object>> getRecords( String systemCode, int skipRows, int maxRows ) throws SQLException {
		return getRecords( Record.createMap("systemCode", systemCode), skipRows, maxRows );
	}

	public int getSystemCount() throws SQLException {
		return SQLManager.getInt( handler, "SELECT COUNT(*) FROM SYS_SYSTEM" );
	}

	public List<Map<String, Object>> getSystems() throws SQLException {
		QueryFactory factory = Schema.findQueryFactory( Schema.SYS_SYSTEM );
		QueryBuffer querybuf = factory.setQuery( new QueryBuffer() );
		if( !appendOrderBy(querybuf) )
			querybuf.appendOrderBy( "SYS.SYSTEM_CD" );

		return SQLManager.getRecordList( handler, querybuf, 0, -1 );
	}
}
