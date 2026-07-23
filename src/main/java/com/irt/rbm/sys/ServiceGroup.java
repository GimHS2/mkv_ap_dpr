/*
 *	File Name:	ServiceGroup.java
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
 *	kdmcom		2002/12/20				create
 *
**/

package com.irt.rbm.sys;

import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class ServiceGroup extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.SYS_SERVICEGRP_PACKAGE );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_SERVICEGRP_PACKAGE );

	public ServiceGroup( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String serviceGroupCode ) {
		return Record.createMap( "serviceGroupCode", serviceGroupCode );
	}

	public boolean deleteLink( String serviceGroupCode, String serviceGroupSubCode ) throws DataException, SQLException {
		return( SQLManager.executeStatement( handler,
			"DELETE SYS_SERVGRP_LINK WHERE SERVGRPCD = ? AND SERVGRPSUBCD = ?"
		, serviceGroupCode, serviceGroupSubCode ) > 0 );
	}

	public int getLinkCount( String serviceGroupCode ) throws SQLException {
		return SQLManager.getInt( handler, "SELECT COUNT(*) FROM SYS_SERVGRP_LINK WHERE SERVGRPCD = ?", serviceGroupCode );
	}

	public String getServiceGroupName( String serviceGroupCode ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT SERVGRP_NAME FROM SYS_SERVGRP WHERE SERVGRP_CD = ?", serviceGroupCode );
	}

	public boolean registLink( String serviceGroupCode, String serviceGroupSubCode ) throws DataException, SQLException {
		return( SQLManager.executeStatement( handler,
			"INSERT INTO SYS_SERVGRP_LINK( SERVGRPCD, SERVGRPSUBCD ) VALUES( ?, ? )"
		, serviceGroupCode, serviceGroupSubCode ) > 0 );
	}
}
