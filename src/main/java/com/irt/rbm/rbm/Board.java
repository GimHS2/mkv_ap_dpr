/*
 *	File Name:	Board.java
 *	Version:	2.2.2c(dpr)
 *
 *	Description:
 *
 *	Note:
 *		getRecord(Map, int)에서 boardType이 'R', 'N'인 경우에 대한 고려 부족
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2c	언어별 공지사항 기능 추가	
 *	hankalam	2017/02/28		2.2.2c	regist(): noticeEndDate 데이터타입 DATETIME 으로 변경
 *	stghr12		2008/09/26		2.2.2	boardClassCode 길이 변경 ( 10 -> 15 )
 *	stghr12		2008/03/31		2.2.1	pkRBMBoard 변경사항 적용: noticeStartDate, noticeEndDate, extraValue 추가
 *										setPrimaryConditionQuery() 구현
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										SQLManager 변경사항 적용: fieldMap 삭제, getRecordMap() 변경
 *	stghr12		2007/10/31		2.1.2	IllegalArgumentException message 변경: invalid -> illegal
 *	stghr12		2007/04/30		2.1.1	createPrimary(): java.util.HashMap -> java.util.TreeMap
 *	stghr12		2006/12/01		2.1.0	RBMDataManager.appendOrderBy 변경사항 반영
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/11/11		1.0.0	create
 *
 **/

package com.irt.rbm.rbm;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class Board extends com.irt.rbm.QueryableManagerImpl {
	private final static Table table = Schema.findTable( Schema.RBM_BOARD );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.RBM_BOARD );

	public Board( SQLHandler handler ) {
		super( handler, factory );
	}

	@Override
	protected boolean appendOrderBy( QueryBuffer querybuf ) {
		return appendOrderBy( querybuf, factory );
	}

	@Override
	protected boolean appendOrderBy( QueryBuffer querybuf, QueryFactory factory ) {
		if( !super.appendOrderBy(querybuf, factory) )
			querybuf.appendOrderBy( "BRD.BOARDGRP_SEQID DESC, BRD.BOARDGRP_DISPLAYSEQ" );

		return true;
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
		return ( SQLManager.callStatementInt( handler, "BEGIN ? := pkRBMBoard.fDelete( ?, ?, ? ); END;", bindVars ) > 0 );
	}

	public Map<String, Object> getBoardClass( String boardClassCode ) throws SQLException {
		QueryBuffer querybuf = new QueryBuffer();
		QueryFactory factory = Schema.findQueryFactory( Schema.RBM_BOARD_CLASS );

		factory.setDataQuery( querybuf );
		querybuf.appendCondition( "BCL.BOARDCLASS_CD = ?", boardClassCode );

		return SQLManager.getRecordMap( handler, null, querybuf );
	}

	@Override
	public Object getFieldValue( Map<String, ? extends Object> primaryMap, String fieldKey ) throws SQLException {
		try {
			QueryBuffer querybuf = table.setPrimaryConditionQuery( new ConditionQueryBuffer(primaryMap) );
			if( !factory.appendData(querybuf, fieldKey) )
				throw new IllegalArgumentException( "illegal fieldKey '"+ fieldKey +"'" );

			return SQLManager.getObjectValue( handler, querybuf );
		} catch( FieldException fieldEx ) {
			return null;
		}
	}

	public Map<String, Object> getLocaleRecord( Map<String, Object> recordMap, Locale locale ) {
		if( recordMap == null )
			return null;

		String language = locale.getLanguage();
		String title = (String) recordMap.get( "title_" + language );
		if( title == null ) {
			title = (String) recordMap.get( "title_en" );
		}

		String content = (String) recordMap.get( "content_" + language );
		if( content == null ) {
			content = (String) recordMap.get( "content_en" );
		}

		recordMap.put( "title", title );
		recordMap.put( "content" , content );

		return recordMap;
	}

	public List<Map<String, Object>> convertLocaleRecordList( List<Map<String, Object>> recordList ) {
		if( recordList == null )
			return null;

		for( Map<String, Object> recordMap : recordList ) {
			convertLocaleRecord( recordMap );
		}

		return recordList;
	}

	public Map<String, Object> convertLocaleRecord( Map<String, Object> recordMap ) {
		if( recordMap == null )
			return null;

		String[] titles = ( (String)recordMap.get("title") ).split( "\\|\\|" );

		if( titles.length == 1 && !titles[0].matches("\\[[a-z]+\\].+") ) {
			recordMap.put( "title_en", titles[0] );
		}
		for( String title : titles ) {
			String locale = null;
			String titleValue = null;
			if( title.matches("^\\[[a-z]+\\].+") ) {
				locale = title.substring( title.indexOf("[") + 1, title.indexOf("]") );
				titleValue = title.replace( "[" + locale + "]", "" );
			}
			if( locale != null ) {
				recordMap.put( "title_" + locale, titleValue );
			}
		}
		recordMap.remove( "title" );

		String[] contents = ( (String)recordMap.get("content") ).split( "\\|\\|" );

		if( contents.length == 1 && !contents[0].matches("\\[[a-z]+\\].+") ) {
			recordMap.put( "content_en", contents[0] );
		}
		for( String content : contents ) {
			String locale = null;
			String contentValue = null;
			if( content.substring(0, 4).matches("^\\[[a-z]+\\].*") ) {
				locale = content.substring( content.indexOf("[") + 1, content.indexOf("]") );
				contentValue = content.replace( "[" + locale + "]", "" );
			}
			if( locale != null ) {
				recordMap.put( "content_" + locale, contentValue );
			}
		}
		recordMap.remove( "content" );

		return recordMap;
	}

	@Override
	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap ) throws SQLException {
		return getRecordMap( factory, table, primaryMap );
	}

	@Override
	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, String[] fieldKeys ) throws SQLException {
		return getRecordMap( factory, table, primaryMap, fieldKeys );
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
				return SQLManager.getRecordMap( null, rset, rset.getMetaData(), handler.getTimeZone() );
			else
				return null;
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
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

	public String getRegistUserId( String boardClassCode, int boardNumber ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler,
				"SELECT REGUSERID FROM RBM_BOARD WHERE BOARDCLASSCD = ? AND SEQID = ?"
				, boardClassCode, new Integer(boardNumber) );
	}

	public String getTitle( String boardClassCode, int boardNumber ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler,
				"SELECT TITLE FROM RBM_BOARD WHERE BOARDCLASSCD = ? AND SEQID = ?"
				, boardClassCode, new Integer(boardNumber) );
	}

	public AbstractFieldSet getFieldSet( boolean inserting ) {
		return table.getValidableFieldSet( inserting );
	}

	public boolean increaseViewCount( String boardClassCode, int boardNumber, String userId ) throws DataException, SQLException {
		Object[] bindVars = new Object[] { boardClassCode, new Integer(boardNumber), userId };
		return ( SQLManager.callStatementInt( handler, "BEGIN ? := pkRBMBoard.fRead( ?, ?, ? ); END;", bindVars ) > 0 );
	}

	public int modify( Map<String, Object> recordMap ) throws DataException, SQLException {
		ValidableField[] fields = new ValidableField[] {
				new ValidableField( false, "boardClassCode",			"RBM_BOARD_BOARDCLASSCODE",			0, 15 )
				, new ValidableField( false, "boardNumber",				"RBM_BOARD_BOARDNUMBER",			Schema.INTEGER )
				, new ValidableField( true,  "boardOption",				"RBM_BOARD_BOARDOPTION",			0, 10 )
				, new ValidableField( true,  "userId",					"RBM_BOARD_REGISTUSERID",			0, 30 )
				, new ValidableField( false, "title",					"RBM_BOARD_TITLE",					0, 640 )
				, new ValidableField( true,  "noticeStartDateTime",		"RBM_BOARD_NOTICESTARTDATE",		Schema.DATETIME )
				, new ValidableField( true,  "noticeEndDateTime",		"RBM_BOARD_NOTICEENDDATE",			Schema.DATETIME )
				, new ValidableField( true,  "extraValue",				"RBM_BOARD_EXTRAVALUE",				0, 128 )
				, new ValidableField( false, "content",					"RBM_BOARD_CONTENT",				Schema.DESC )
		};

		return SQLManager.callStatementInt( handler, "BEGIN ? := pkRBMBoard.fModify( ?, ?, ?, ?, ?, ?, ?, ?, ? ); END;", fields, recordMap );
	}

	public int regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		ValidableField[] fields = new ValidableField[] {
				new ValidableField( false, "boardClassCode",			"RBM_BOARD_BOARDCLASSCODE",			0, 15 )
				, new ValidableField( false, "boardType",				"RBM_BOARD_BOARDTYPE",				"RBM_BOARD_TYPE_", "C,N,R" )
				, new ValidableField( true,  "boardOption",				"RBM_BOARD_BOARDOPTION",			0, 10 )
				, new ValidableField( true,  "userId",					"RBM_BOARD_REGISTUSERID",			0, 30 )
				, new ValidableField( false, "title",					"RBM_BOARD_TITLE",					0, 640 )
				, new ValidableField( true,  "noticeStartDateTime",		"RBM_BOARD_NOTICESTARTDATE",		Schema.DATETIME )
				, new ValidableField( true,  "noticeEndDateTime",		"RBM_BOARD_NOTICEENDDATE",			Schema.DATETIME )
				, new ValidableField( true,  "extraValue",				"RBM_BOARD_EXTRAVALUE",				0, 128 )
				, new ValidableField( false, "content",					"RBM_BOARD_CONTENT",				Schema.DESC )
				, new ValidableField( true,  "originalBoardNumber",		"RBM_BOARD_ORIGINALBOARDNUMBER",	Schema.INTEGER )
		};

		return SQLManager.callStatementInt( handler, "BEGIN ? := pkRBMBoard.fWrite( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ); END;", fields, recordMap );
	}

	@Override
	protected QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) {
		try {
			return table.setPrimaryConditionQuery( querybuf );
		} catch( FieldException fieldEx ) {
			return null;
		}
	}
}
