/*
 *	File Name:	PartyOperation.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	PartyOper -> PartyOperation
 *	GimHS		2007/11/30		2.2.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.data.*;
import com.irt.rbm.SchedulePattern;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class PartyOperation extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ECS_PARTY_OPER );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_PARTY_OPER );

	public PartyOperation( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String gln, char patternType, int patternIndex, java.util.Date patternDate ) {
		Map<String, Object> primaryMap = SchedulePattern.createPatternMap( patternType, patternIndex, patternDate );

		primaryMap.put( "gln", gln );

		return primaryMap;
	}

	public static Map<String, Object> createPrimary( String gln, Object patternType, Object patternIndex, Object patternDate ) {
		Map<String, Object> primaryMap = SchedulePattern.createPatternMap( patternType, patternIndex, patternDate );

		primaryMap.put( "gln", gln );

		return primaryMap;
	}

	public int applyToChild( Map<String, Object> recordMap, boolean deleting ) throws DataException, SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( recordMap );

		querybuf.appendData( "GLN" );
		querybuf.appendTable( "ECS_PARTY" );
		querybuf.appendCondition( "PARENT_GLN = ?", querybuf.getConditionValue( "gln", Schema.STRING ) );
		querybuf.findConditionCode( "childPartyRole", "PARTY_ROLE" );

		String glns[] = SQLManager.getStringValues( handler, querybuf );
		if( glns == null ) return 0;

		int count = 0;
		recordMap = new java.util.HashMap<String, Object>( recordMap );
		for( int i = 0; i < glns.length; i++ ) {
			recordMap.put( "gln", glns[i] );

			if( deleting ) {
				if( delete(recordMap) ) count++;
			} else {
				if( modify(recordMap) || regist(recordMap) ) count++;
			}
		}

		return count;
	}

	public void updateOperday() throws DataException, SQLException {
		SQLManager.callStatement( handler, "call pkECSPartyOper.pUpdateOperday()" );
	}
}
