/*
 *	File Name:	GeneralCode.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	create
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
public class GeneralCode extends StandardCode {
	/**	OSS: 농협발주 에러코드 **/
	public final static String CODETYPE_NONGHYUP_ORDERERR		= "NHE";

	private final static Table table = Schema.findTable( Schema.SYS_CODE );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.SYS_CODE );

	public GeneralCode( SQLHandler handler ) {
		super( handler, table, factory );
		setSort( "displaySequence" );
	}

	public String getCodeType( String code ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT CODETYPECD FROM SYS_CODE WHERE CODE_CD = ?", code );
	}

	public int getRecordCount( String codeTypeCode ) throws SQLException {
		return getRecordCount( Record.createMap("codeTypeCode", codeTypeCode) );
	}

	public List<Map<String, Object>> getRecords( String codeTypeCode ) throws SQLException {
		return getRecords( Record.createMap("codeTypeCode", codeTypeCode) );
	}

	public List<Map<String, Object>> getRecords( String codeTypeCode, int skipRows, int maxRows ) throws SQLException {
		return getRecords( Record.createMap("codeTypeCode", codeTypeCode), skipRows, maxRows );
	}

	public boolean isValidCodeType( String codeTypeCode ) throws SQLException {
		return ( SQLManager.getObjectValue( handler, "SELECT 'x' FROM SYS_CODETYPE WHERE CODETYPE_CD = ?", codeTypeCode ) != null );
	}
}
