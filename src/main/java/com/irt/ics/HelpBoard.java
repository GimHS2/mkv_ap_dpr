/*
 *	File Name:	HelpBoard.java
 *	Version:	2.2.1c(dpr)
 *
 *	Description:
 *
 *	Note:
 *		getRecord(Map, int)에서 boardType이 'R', 'N'인 경우에 대한 고려 부족
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.1c	Revise Order Feature.
 *	hankalam	2019/07/31		2.2.1c	makeBoardClassCode() 추가
 *	hankalam	2017/02/28		2.2.0c	create
 *
 **/

package com.irt.ics;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class HelpBoard extends com.irt.rbm.QueryableManagerImpl {
	public final static String HELP_BOARDCLASS_PREFIX		= "HD";
	public final static String BOARDOPTION_TEXT				= "T";
	public final static String BOARDOPTION_HTML				= "H";

	public final static String BOARDTYPE_NORMAL				= "C";
	public final static String BOARDTYPE_REPLY				= "R";

	private final static Table table = Schema.findTable( Schema.ICS_HELP_BOARD );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ICS_HELP_BOARD );

	public HelpBoard( SQLHandler handler ) {
		super( handler, factory );
		setSort( "boardGroupNumber#DESC", "boardGroupDisplaySeq" );
	}

	public static Map<String, Object> createPrimary( String boardClassCode, int boardNumber ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "boardClassCode", boardClassCode );
		primaryMap.put( "boardNumber", new Integer(boardNumber) );

		return primaryMap;
	}

	public static Map<String, Object> createPrimary( String boardClassCode, int boardNumber, String userId ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "boardClassCode", boardClassCode );
		primaryMap.put( "boardNumber", new Integer(boardNumber) );
		primaryMap.put( "userId", userId );

		return primaryMap;
	}

	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		return delete( primaryMap, false );
	}

	public boolean delete( Map<String, Object> primaryMap, boolean deleteAll ) throws DataException, SQLException {
		try {
			table.extractPrimaryValues( primaryMap );
		} catch( FieldException fieldEx ) {
			return false;
		}

		Object[] bindVars = new Object[] { primaryMap.get("boardClassCode"), primaryMap.get("boardNumber"), deleteAll ? "Y" : "N" };
		return ( SQLManager.callStatementInt( handler, "BEGIN ? := pkICSBoard.fDeleteOnHelpBoard( ?, ?, ? ); END;", bindVars ) > 0 );
	}

	public Map<String, Object> getBoardClass( String boardClassCode ) throws SQLException {
		return (new BoardClass(handler)).getRecord( Record.createMap("boardClassCode", boardClassCode) );
	}

	public String makeBoardClassCode( String organizationCode ) throws SQLException {
		if( organizationCode == null ) {
			return null;
		}
		return HELP_BOARDCLASS_PREFIX + "." + organizationCode;
	}

	public AbstractFieldSet getFieldSet( boolean inserting ) {
		return table.getValidableFieldSet( inserting );
	}

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, int index ) throws SQLException {
		return getRecord( primaryMap, index, null, 0 );
	}

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, int index, String[] fieldKeys ) throws SQLException {
		return getRecord( primaryMap, index, fieldKeys, 1 );
	}

	private Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, int index, String[] fieldKeys, int type ) throws SQLException {
		if( index == 0 ) return getRecord( primaryMap );

		QueryBuffer querybuf;
		if( type == 0 )
			querybuf = factory.setDataQuery( new QueryBuffer() );
		else
			querybuf = factory.setDataQuery( new QueryBuffer(), fieldKeys );
		querybuf.appendCondition( "BRD.BOARDCLASSCD = ?", primaryMap.get("boardClassCode") );

		int skipRows = 0;
		if( index > 0 ) {
			skipRows = index - 1;
			querybuf.appendCondition( "BRD.SEQID > ? AND BRD.BOARD_TYPE = 'C'", primaryMap.get("boardNumber") );
			querybuf.appendOrderBy( "BRD.SEQID" );
		} else {
			skipRows = - index - 1;
			querybuf.appendCondition( "BRD.SEQID < ? AND BRD.BOARD_TYPE = 'C'", primaryMap.get("boardNumber") );
			querybuf.appendOrderBy( "BRD.SEQID DESC" );
		}

		PreparedStatement pstmt = handler.getConnection().prepareStatement( querybuf.getQuery() );
		ResultSet rset = null;
		try {
			SQLManager.bindVariables( pstmt, querybuf.getBindVariables() );
			rset = pstmt.executeQuery();
			while( skipRows-- > 0 && rset.next() );

			if( rset.next() )
				return SQLManager.getRecordMap( handler, null, querybuf );
			else
				return null;
		} finally {
			try { rset.close(); } catch( Exception ignored ) {}
			try { pstmt.close(); } catch( Exception ignored ) {}
		}
	}

	public List<Map<String, Object>> getRecords( String boardClassCode ) throws SQLException {
		return getRecords( Record.createMap("boardClassCode", boardClassCode), 0, -1 );
	}

	public List<Map<String, Object>> getRecords( String boardClassCode, int skipRows, int maxRows ) throws SQLException {
		return getRecords( Record.createMap("boardClassCode", boardClassCode), skipRows, maxRows );
	}

	public List<Map<String, Object>> getRecords( String boardClassCode, String[] fieldKeys ) throws SQLException {
		return getRecords( Record.createMap("boardClassCode", boardClassCode), fieldKeys, 0, -1 );
	}

	public List<Map<String, Object>> getRecords( String boardClassCode, String[] fieldKeys, int skipRows, int maxRows ) throws SQLException {
		return getRecords( Record.createMap("boardClassCode", boardClassCode), fieldKeys, skipRows, maxRows );
	}

	public Map<String, Object> getRecordWithLock( Map<String, ? extends Object> primaryMap, String[] fieldKeys ) throws DataException, SQLException {
		return getRecordWithLock( primaryMap, fieldKeys, true );
	}

	public Map<String, Object> getRecordWithLock( Map<String, ? extends Object> primaryMap, String[] fieldKeys, boolean waiting )
			throws DataException, SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( primaryMap );

		(new QueryFactory(table)).setDataQuery( querybuf.appendLock(waiting), fieldKeys );
		if( setPrimaryConditionQuery(querybuf) == null ) return null;

		try {
			return SQLManager.getRecordMap( handler, null, querybuf );
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx, primaryMap );
		}
	}

	public String[] getRecentlyBoardNumber( String boardClassCode, int recentlyDays ) throws SQLException, DataException {
		if( boardClassCode != null ) {
			QueryBuffer querybuf;

			querybuf = factory.setDataQuery( new QueryBuffer(), new String[]{ "boardNumber" } );
			querybuf.appendCondition( "BRD.BOARDCLASSCD = ? AND BRD.BOARD_TYPE = ?", boardClassCode, BOARDTYPE_NORMAL );
			querybuf.appendCondition( "BRD.REGDATE > TRUNC(SYSDATE - ?)",recentlyDays );
			querybuf.appendCondition( "NVL(BRD.NOTICE_STARTDATE,TRUNC(SYSDATE)) <= TRUNC(SYSDATE)");
			querybuf.appendCondition( "NVL(BRD.NOTICE_ENDDATE,TRUNC(SYSDATE)) >= TRUNC(SYSDATE)");
			querybuf.appendOrderBy( "BRD.SEQID" );

			return SQLManager.getStringValues( handler, querybuf.getQuery(), querybuf.getBindVariables() );
		} else
			return null;
	}

	public boolean increaseViewCount( String boardClassCode, int boardNumber, String userId ) throws DataException, SQLException {
		Object[] bindVars = new Object[] { boardClassCode, new Integer(boardNumber), userId };
		return ( SQLManager.callStatementInt( handler, "BEGIN ? := pkICSBoard.fRead( ?, ?, ? ); END;", bindVars ) > 0 );
	}

	public int modify( Map<String, Object> recordMap ) throws DataException, SQLException {
		ValidableField[] fields = new ValidableField[] {
				new ValidableField( false, "boardClassCode",			"ICS_BOARD_BOARDCLASSCODE",			0, 10 )
				, new ValidableField( false, "boardNumber",				"ICS_BOARD_BOARDNUMBER",			Schema.INTEGER )
				, new ValidableField( false, "boardType",				"ICS_BOARD_BOARDTYPE",				"ICS_BOARD_TYPE_", "C,N,R" )
				, new ValidableField( true,  "boardOption",				"ICS_BOARD_BOARDOPTION",			0, 10 )
				, new ValidableField( true,  "headwordCode",			"ICS_BOARD_HEADWORDCODE",			0, 15 )
				, new ValidableField( true,  "registUserId",			"ICS_BOARD_REGISTUSERID",			0, 30 )
				, new ValidableField( false, "userName",				"ICS_HELP_BOARD_USERNAME",			0, 128 )
				, new ValidableField( true,  "orderNumber",				"ICS_HELP_BOARD_ORDERNUMBER",		0, 35 )
				, new ValidableField( false, "tel",						"ICS_HELP_BOARD_TEL",				0, 20 )
				, new ValidableField( false, "email",					"ICS_HELP_BOARD_EMAIL",				0, 128 )
				, new ValidableField( false, "content",					"ICS_BOARD_CONTENT",				Schema.DESC )
		};

		return SQLManager.callStatementInt( handler, "BEGIN ? := pkICSBoard.fModifyOnHelpBoard( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ); END;", fields, recordMap );
	}

	public int modifyCompletedPost( Map<String, Object> recordMap ) throws DataException, SQLException {
		String statement = "UPDATE ICS_HELP_BOARD SET COMPLETED_IND = ? WHERE BOARDCLASSCD = ? AND SEQID = ?";
		ValidableField[] fields = new ValidableField[] {
				new ValidableField( false, "completedInd",			"ICS_HELPBOARD_COMPLETED_IND",			"ICS_HELP_BOARD_COMPLETED_IND_", "Y,N" )
				, new ValidableField( false, "boardClassCode",			"ICS_HELPBOARD_BOARDCLASSCODE",			0, 10 )
				, new ValidableField( false, "boardNumber",				"ICS_HELPBOARD_BOARDNUMBER",			Schema.INTEGER )
		};

		int modified = ( SQLManager.executeStatement(handler, statement, fields, recordMap) );
		if( modified > 0 ) {
			SQLManager.executeStatement(handler
					, "UPDATE DPR_ORDER ORD SET ORD.REV_STATUS = DECODE("
							+ " ORD.ORDER_NUMBER,NULL,DECODE(?,'Y','CP','N','CQ')"
							+ " , (SELECT DECODE(COUNT(DECODE(HBRD.COMPLETED_IND,'N',1,NULL)),0,NULL,'CQ') REV_P FROM ICS_HELP_BOARD HBRD WHERE HBRD.BOARDCLASSCD = ? AND HBRD.SEQID = ?) )"
							+ " WHERE ("
							+ " ORD.ORDER_KEY = (SELECT HBRD.ORDERKEY FROM ICS_HELP_BOARD HBRD WHERE HBRD.BOARDCLASSCD = ? AND HBRD.SEQID = ?)"
							+ " OR "
							+ " ORD.ORDER_NUMBER = (SELECT HBRD.ORDER_NUMBER FROM ICS_HELP_BOARD HBRD WHERE HBRD.BOARDCLASSCD = ? AND HBRD.SEQID = ?)"
							+ " )"
							, new Object[] {recordMap.get("completedInd")
									, recordMap.get("boardClassCode"), recordMap.get("boardNumber")
									, recordMap.get("boardClassCode"), recordMap.get("boardNumber")
									, recordMap.get("boardClassCode"), recordMap.get("boardNumber")});
		}
		return modified;
	}

	public int regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		ValidableField[] fields = new ValidableField[] {
				new ValidableField( false, "boardClassCode",			"ICS_BOARD_BOARDCLASSCODE",			0, 10 )
				, new ValidableField( false, "boardType",				"ICS_BOARD_BOARDTYPE",				"ICS_BOARD_TYPE_", "C,N,R" )
				, new ValidableField( true,  "boardOption",				"ICS_BOARD_BOARDOPTION",			0, 10 )
				, new ValidableField( true,  "registUserId",			"ICS_BOARD_REGISTUSERID",			0, 30 )
				, new ValidableField( false, "userName",				"ICS_HELP_BOARD_USERNAME",			0, 128 )
				, new ValidableField( true,  "orderNumber",				"ICS_HELP_BOARD_ORDERNUMBER",		0, 35 )
				, new ValidableField( false, "tel",						"ICS_HELP_BOARD_TEL",				0, 20 )
				, new ValidableField( false, "email",					"ICS_HELP_BOARD_EMAIL",				0, 128 )
				, new ValidableField( true,  "headwordCode",			"ICS_BOARD_HEADWORDCODE",			0, 15 )
				, new ValidableField( false, "content",					"ICS_BOARD_CONTENT",				Schema.DESC )
				, new ValidableField( true,  "originalBoardNumber",		"ICS_BOARD_ORIGINALBOARDNUMBER",	Schema.INTEGER )
				, new ValidableField( true,  "attachManageKey",			"ICS_BOARD_ATTACHMANAGEKEY",		0, 50 )
		};

		return SQLManager.callStatementInt( handler, "BEGIN ? := pkICSBoard.fWriteOnHelpBoard( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ); END;", fields, recordMap );
	}

	@Override
	protected QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) {
		return setPrimaryConditionQuery( table, querybuf );
	}
}
