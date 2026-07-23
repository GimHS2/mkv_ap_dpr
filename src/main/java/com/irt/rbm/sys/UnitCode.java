/*
 *	File Name:	UnitCode.java
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

package com.irt.rbm.sys;

import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public class UnitCode extends StandardCode {
	public final static String UNIT_LENGTH				= "LN";
	public final static String UNIT_AREA				= "AR";
	public final static String UNIT_WEIGHT				= "WG";
	public final static String UNIT_STRONG				= "ST";
	public final static String UNIT_TEMPERATURE			= "TP";
	public final static String UNIT_VOLUME				= "VL";
	public final static String UNIT_ETC					= "ET";

	private final static Table table = Schema.findTable( Schema.SYS_UNIT );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_UNIT );

	public UnitCode( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public String getClassCode( String code ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT UNIT_CLASSCD FROM SYS_UNIT WHERE UNIT_CD = ?", code );
	}

	public int getRecordCount( String classCode ) throws SQLException {
		return getRecordCount( Record.createMap("classCode", classCode) );
	}

	public List getRecords( String classCode ) throws SQLException {
		return getRecords( Record.createMap("classCode", classCode) );
	}

	public List getRecords( String classCode, int skipRows, int maxRows ) throws SQLException {
		return getRecords( Record.createMap("classCode", classCode), skipRows, maxRows );
	}
}
