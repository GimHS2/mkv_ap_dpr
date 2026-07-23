/*
 *	File Name:	BoardHeadword.java
 *	Version:	2.2.1c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2019/07/31		2.2.1c	getHeadwordCode() 추가
 *	stghr12		2010/07/31		2.2.1	version up
 *	lsinji		2009/10/25		2.2.0	create
 *
**/

package com.irt.ics;

import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class BoardHeadword extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ICS_BOARD_HEADWORD );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ICS_BOARD_HEADWORD );

	public BoardHeadword( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String headwordCode ) {
		return com.irt.data.Record.createMap( "headwordCode", headwordCode );
	}

	public String generateHeadwordCode( String boardClassCode ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT ? || LPAD(seqICS_BOARD_HW.NEXTVAL, 5 ,0) FROM DUAL", boardClassCode );
	}

	public java.util.List<Map<String, Object>> getRecords( String boardClassCode, String[] fieldKeys ) throws SQLException {
		return getRecords( com.irt.data.Record.createMap("boardClassCode", boardClassCode), fieldKeys );
	}

	public String getHeadwordCode( String boardClassCode, String headwordName ) throws SQLException {
		Map<String, Object> conditionMap = com.irt.data.Record.createMap( "boardClassCode", boardClassCode );
		conditionMap.put( "headwordName", headwordName );
		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), new String[] { "headwordCode" } );
		return (String) SQLManager.getObjectValue( handler, querybuf );
	}
}
