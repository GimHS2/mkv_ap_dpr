/*
 *	File Name:	TableDataLoader.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/07/30		2.2.0c	initPrepareStatement(): table.makeUpdateStatement(updateFields, updateSetCaluseOnly) 사용
 *	stghr12		2008/03/31		2.2.0	create
 *
 **/

package com.irt.sql;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.data.format.RecordFormat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class TableDataLoader extends BasicDataLoader {
	final static RecordFormat failureMessageFormat = com.irt.data.format.PatternRecordFormat.getInstance( "%{MSG_NO_RECORD_FOUND}" );
	final static RecordFormat insertSuccessMessageFormat = com.irt.data.format.PatternRecordFormat.getInstance( "%{MSG_REGIST_SUCCESS}" );
	final static RecordFormat updateSuccessMessageFormat = com.irt.data.format.PatternRecordFormat.getInstance( "%{MSG_MODIFY_SUCCESS}" );
	final static RecordFormat deleteSuccessMessageFormat = com.irt.data.format.PatternRecordFormat.getInstance( "%{MSG_REMOVE_SUCCESS}" );

	PreparedStatement pstmt_ins, pstmt_upd, pstmt_del;
	ValidableFieldSet insertFieldSet, updateFieldSet, deleteFieldSet;

	String deleteCheckingKey;
	ValidableField fld_executeType;

	public TableDataLoader( String[] lineFieldKeys, Map<String, ? extends Object> lineDefaultMap, SQLHandler handler, Table table
			, int statementType ) throws SQLException {
		super( lineFieldKeys, lineDefaultMap, null );
		initPreparedStatement( handler, table, null, false, statementType );
	}

	public TableDataLoader( String[] lineFieldKeys, Map<String, ? extends Object> lineDefaultMap, SQLHandler handler, Table table
			, String[] updateFieldKeys, int statementType ) throws SQLException {
		super( lineFieldKeys, lineDefaultMap, null );
		initPreparedStatement( handler, table, updateFieldKeys, true, statementType );
	}

	@Override
	public void close() {
		try { if( pstmt_ins != null ) pstmt_ins.close(); } catch( Exception ignored ) {}
		try { if( pstmt_upd != null ) pstmt_upd.close(); } catch( Exception ignored ) {}
		try { if( pstmt_del != null ) pstmt_del.close(); } catch( Exception ignored ) {}
	}

	private void initPreparedStatement( SQLHandler handler, Table table, String[] updateFieldKeys, boolean usingFieldKeys, int statementType )
			throws SQLException {
		Connection conn = handler.getConnection();
		try {
			if( (statementType & Record.INSERT) > 0 ) {
				this.pstmt_ins = conn.prepareStatement( table.makeStatement(Record.INSERT) );
				this.insertFieldSet = new ValidableFieldSet( table.getBindFieldArray(Record.INSERT) );
			}

			if( (statementType & Record.UPDATE) > 0 ) {
				if( usingFieldKeys ) {
					Table.Field[] updateFields = table.getAlterableFieldArray( updateFieldKeys );
					Table.Field[] primaryFields = table.getBindFieldArray( Record.QUERY );
					Table.Field[] updateBindFields = new Table.Field[ updateFields.length + primaryFields.length ];

					System.arraycopy( updateFields, 0, updateBindFields, 0, updateFields.length );
					System.arraycopy( primaryFields, 0, updateBindFields, updateFields.length, primaryFields.length );

					this.pstmt_upd = conn.prepareStatement( table.makeUpdateStatement(updateFields, false) );
					this.updateFieldSet = new ValidableFieldSet( updateBindFields );
				} else {
					this.pstmt_upd = conn.prepareStatement( table.makeStatement(Record.UPDATE) );
					this.updateFieldSet = new ValidableFieldSet( table.getBindFieldArray(Record.UPDATE) );
				}
			}

			if( (statementType & Record.DELETE) > 0 ) {
				this.pstmt_del = conn.prepareStatement( table.makeStatement(Record.DELETE) );
				this.deleteFieldSet = new ValidableFieldSet( table.getBindFieldArray(Record.DELETE) );
			}

			conn = null;
		} finally {
			if( conn != null ) {
				try { if( pstmt_ins != null ) pstmt_ins.close(); } catch( Exception ignored ) {}
				try { if( pstmt_upd != null ) pstmt_upd.close(); } catch( Exception ignored ) {}
				try { if( pstmt_del != null ) pstmt_del.close(); } catch( Exception ignored ) {}
			}
		}
	}

	@Override
	public Map<String, Object> loadLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
		boolean deleting = ( pstmt_del != null );
		boolean updating = ( pstmt_upd != null );
		boolean inserting = ( pstmt_ins != null );

		Map<String, Object> resultMap = new java.util.TreeMap<String, Object>();
		try {
			String executeType = null;
			if( fld_executeType != null ) {
				executeType = (String)fld_executeType.validate( recordMap );

				deleting = "D".equals( executeType );
				updating = "U".equals( executeType );
				inserting = "A".equals( executeType );
			} else if( deleteCheckingKey != null ) {
				if( recordMap.get(deleteCheckingKey) == null )
					updating = inserting = false;
			}

			if( updating ) {
				SQLManager.bindVariables( pstmt_upd, updateFieldSet.validate(recordMap) );
				if( pstmt_upd.executeUpdate() > 0 ) {
					resultMap.put( "executeType", "U" );
					resultMap.put( "status", com.irt.data.DataLoader.COMPLETED );
					resultMap.put( "message", updateSuccessMessageFormat.format(recordMap, handler.getMessageHandler()) );

					return resultMap;
				}
			}

			if( inserting ) {
				SQLManager.bindVariables( pstmt_ins, insertFieldSet.validate(recordMap) );
				if( pstmt_ins.executeUpdate() > 0 ) {
					resultMap.put( "executeType", "A" );
					resultMap.put( "status", com.irt.data.DataLoader.COMPLETED );
					resultMap.put( "message", insertSuccessMessageFormat.format(recordMap, handler.getMessageHandler()) );

					return resultMap;
				}
			}

			if( deleting ) {
				SQLManager.bindVariables( pstmt_del, deleteFieldSet.validate(recordMap) );
				if( pstmt_del.executeUpdate() > 0 ) {
					resultMap.put( "executeType", "D" );
					resultMap.put( "status", com.irt.data.DataLoader.COMPLETED );
					resultMap.put( "message", deleteSuccessMessageFormat.format(recordMap, handler.getMessageHandler()) );

					return resultMap;
				}
			}

			resultMap.put( "executeType", executeType );
			resultMap.put( "status", com.irt.data.DataLoader.WARNING );
			resultMap.put( "message", failureMessageFormat.format(recordMap, handler.getMessageHandler()) );

			return resultMap;
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx, recordMap );
		}
	}

	public void setDeleteCheckingKey( String deleteCheckingKey ) {
		this.deleteCheckingKey = deleteCheckingKey;
	}

	@Override
	public boolean setLoaderOption( String key, Object value ) throws IllegalArgumentException {
		if( "usingExecuteType".equals(key) ) {
			setUsingExecuteType( Condition.isConditionTrue(value) );
			return true;
		} else if( "deleteCheckingKey".equals(key) ) {
			try {
				setDeleteCheckingKey( (String)value );
			} catch( ClassCastException castEx ) {
				throw new IllegalArgumentException( castEx.getMessage() );
			}
			return true;
		}

		return super.setLoaderOption( key, value );
	}

	public void setUsingExecuteType( boolean usingExecuteType ) {
		if( usingExecuteType ) {
			String executeTypeValue = "";
			if( pstmt_ins != null ) executeTypeValue += ",A";
			if( pstmt_upd != null ) executeTypeValue += ",U";
			if( pstmt_del != null ) executeTypeValue += ",D";

			this.fld_executeType = new ValidableField( false, "executeType", "EXECUTETYPE", "PUB_EXECUTETYPE_", executeTypeValue.substring(1) );
		} else
			this.fld_executeType = null;
	}
}
