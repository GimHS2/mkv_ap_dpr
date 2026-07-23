/*
 *	File Name:	BoardComment.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/07/31		2.2.1	version up
 *	lsinji		2009/10/25		2.2.0	create
 *
**/

package com.irt.ics;

import com.irt.data.DataException;
import com.irt.data.ValidableField;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class BoardComment extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ICS_BOARD_COMMENT );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ICS_BOARD_COMMENT );

	public BoardComment( SQLHandler handler ) {
		super( handler, table, factory );
		setSort( "commentGroupNumber#ASC", "commentGroupDisplaySeq#ASC" );
	}

	public static Map<String, Object> createPrimary( String boardClassCode, int boardNumber, int commentNumber ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "boardClassCode", boardClassCode );
		primaryMap.put( "boardNumber", new Integer(boardNumber) );
		primaryMap.put( "commentNumber", new Integer(commentNumber) );

		return primaryMap;
	}

	@Override
	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		ValidableField[] fields = new ValidableField[] {
					new ValidableField( false, "boardClassCode",			"ICS_BOARD_COMMENT_BOARDCLASSCODE",			0, 10 )
			, new ValidableField( false, "boardNumber",				"ICS_BOARD_COMMENT_BOARDNUMBER",			Schema.INTEGER )
			, new ValidableField( false, "commentNumber",			"ICS_BOARD_COMMENT_COMMENTNUMBER",			Schema.INTEGER )
		};

		return ( SQLManager.callStatementInt( handler, "BEGIN ? := pkICSBoard.fCommentDelete( ?, ?, ? ); END;", fields, primaryMap ) > 0 );
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		ValidableField[] fields = new ValidableField[] {
					new ValidableField( false, "boardClassCode",			"ICS_BOARD_COMMENT_BOARDCLASSCODE",			0, 10 )
			, new ValidableField( false, "boardNumber",				"ICS_BOARD_COMMENT_BOARDNUMBER",			Schema.INTEGER )
			, new ValidableField( false, "registUserId",			"ICS_BOARD_COMMENT_REGISTUSERID",			0, 30 )
			, new ValidableField( false, "content",					"ICS_BOARD_COMMENT_CONTENT",				0, 1024 )
			, new ValidableField( true,  "originalCommentNumber",	"ICS_BOARD_COMMENT_ORIGINALCOMMENTNUMBER",	Schema.INTEGER )
		};

		return ( SQLManager.callStatementInt( handler, "BEGIN ? := pkICSBoard.fCommentWrite( ?, ?, ?, ?, ? ); END;", fields, recordMap ) >= 0 );
	}
}
