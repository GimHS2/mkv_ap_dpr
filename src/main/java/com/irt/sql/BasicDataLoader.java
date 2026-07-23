/*
 *	File Name:	BasicDataLoader.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/05/31		2.2.1	complete(), start() 추가
 *	stghr12		2008/03/31		2.2.0	create
 *
 **/

package com.irt.sql;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.data.format.RecordFormat;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class BasicDataLoader implements com.irt.data.DataLoader.Loader {
	String[] lineFieldKeys;
	Map<String, ? extends Object> lineDefaultMap;

	PreparedStatement pstmt_data;
	String[] dataFieldKeys;

	PreparedStatement pstmt_exec;
	String executeType;
	String[] execFieldKeys;
	ValidableFieldSet execFieldSet;
	RecordFormat successMessageFormat, failureMessageFormat;

	DataLoader.Validator validator;

	public BasicDataLoader( String[] lineFieldKeys, Map<String, ? extends Object> lineDefaultMap, String executeType ) {
		this.lineFieldKeys = lineFieldKeys;
		this.lineDefaultMap = lineDefaultMap;
		this.executeType = executeType;
	}

	public BasicDataLoader( String[] lineFieldKeys, Map<String, ? extends Object> lineDefaultMap
			, String executeType, PreparedStatement pstmt, String[] execFieldKeys
			, RecordFormat successMessageFormat, RecordFormat failureMessageFormat ) {
		this( lineFieldKeys, lineDefaultMap, executeType, pstmt, execFieldKeys, null, successMessageFormat, failureMessageFormat );
	}

	public BasicDataLoader( String[] lineFieldKeys, Map<String, ? extends Object> lineDefaultMap
			, String executeType, PreparedStatement pstmt, ValidableField[] execFieldKeys
			, RecordFormat successMessageFormat, RecordFormat failureMessageFormat ) {
		this( lineFieldKeys, lineDefaultMap, executeType, pstmt, new ValidableFieldSet(execFieldKeys), successMessageFormat, failureMessageFormat );
	}

	public BasicDataLoader( String[] lineFieldKeys, Map<String, ? extends Object> lineDefaultMap
			, String executeType, PreparedStatement pstmt, ValidableFieldSet execFieldSet
			, RecordFormat successMessageFormat, RecordFormat failureMessageFormat ) {
		this( lineFieldKeys, lineDefaultMap, executeType, pstmt, null, execFieldSet, successMessageFormat, failureMessageFormat );
	}

	BasicDataLoader( String[] lineFieldKeys, Map<String, ? extends Object> lineDefaultMap
			, String executeType, PreparedStatement pstmt, String[] execFieldKeys, ValidableFieldSet execFieldSet
			, RecordFormat successMessageFormat, RecordFormat failureMessageFormat ) {
		this.lineFieldKeys = lineFieldKeys;
		this.lineDefaultMap = lineDefaultMap;

		this.pstmt_exec = pstmt;
		this.executeType = executeType;
		this.execFieldKeys = execFieldKeys;
		this.execFieldSet = execFieldSet;
		this.successMessageFormat = successMessageFormat;
		this.failureMessageFormat = failureMessageFormat;
	}

	@Override
	public void close() {
		try { if( pstmt_data != null ) pstmt_data.close(); } catch( Exception ignored ) {}
		try { if( pstmt_exec != null ) pstmt_exec.close(); } catch( Exception ignored ) {}
		try { if( validator != null ) validator.close(); } catch( Exception ignored ) {}
	}

	@Override
	public void complete( SQLHandler handler ) throws SQLException {
	}

	@Override
	public String getExecuteType() {
		return executeType;
	}

	@Override
	public Map<String, Object> loadLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
		if( pstmt_exec == null ) throw new IllegalStateException();

		try {
			if( execFieldSet != null )
				SQLManager.bindVariables( pstmt_exec, execFieldSet.validate(recordMap) );
			else
				SQLManager.bindVariables( pstmt_exec, Record.extractValues(recordMap, execFieldKeys) );
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		}

		Map<String, Object> resultMap = new java.util.TreeMap<String, Object>();
		resultMap.put( "executeType", executeType );
		try {
			if( pstmt_exec.executeUpdate() > 0 ) {
				resultMap.put( "status", com.irt.data.DataLoader.COMPLETED );
				resultMap.put( "message", successMessageFormat.format(recordMap, handler.getMessageHandler()) );
			} else {
				resultMap.put( "status", com.irt.data.DataLoader.WARNING );
				resultMap.put( "message", failureMessageFormat.format(recordMap, handler.getMessageHandler()) );
			}
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx, recordMap );
		}

		return resultMap;
	}

	@Override
	public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
		if( lineDefaultMap != null ) recordMap.putAll( lineDefaultMap );
		if( pstmt_data != null ) {
			SQLManager.bindVariables( pstmt_data, Record.extractValues(recordMap, dataFieldKeys) );
			ResultSet rset = pstmt_data.executeQuery();
			try {
				if( rset.next() )
					SQLManager.getRecordMap( recordMap, rset, rset.getMetaData(), handler.getTimeZone() );
			} finally {
				try { rset.close(); } catch( Exception ignored ) {}
			}
		}
		if( validator != null ) validator.validateLine( handler, recordMap );

		return recordMap;
	}

	@Override
	public Map<String, Object> readLine( SQLHandler handler, DataReader reader ) throws DataException, IOException {
		Map<String, Object> recordMap = null;
		do {
			try {
				recordMap = reader.readNext( lineFieldKeys );
			} catch( DataException dataEx ) {
				throw handler.createDataException( dataEx.getErrorKey() );
			}
		} while( !reader.isEOF() && reader.isBlankLine() && recordMap != null );

		return recordMap;
	}

	public void setDataPreparedStatement( PreparedStatement pstmt, String[] fieldKeys ) {
		this.pstmt_data = pstmt;
		this.dataFieldKeys = fieldKeys;
	}

	@Override
	public boolean setLoaderOption( String key, Object value ) throws IllegalArgumentException {
		try {
			if( "validator".equals(key) ) {
				setValidator( (DataLoader.Validator)value );
				return true;
			} else
				return false;
		} catch( ClassCastException castEx ) {
			throw new IllegalArgumentException( castEx.getMessage() );
		}
	}

	public void setValidator( DataLoader.Validator validator ) {
		this.validator = validator;
	}

	@Override
	public void start( SQLHandler handler ) throws SQLException {
	}
}
