/*
 *	File Name:	Schedule.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	create
 *
 **/

package com.irt.rbm.rbm;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.rbm.SchedulePattern;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Schedule extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.RBM_SCHEDULE );
	private final static Table table_dtl = Schema.findTable( Schema.RBM_SCHEDULE_PTN );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.RBM_SCHEDULE );
	private final static QueryFactory factory_dtl = Schema.findQueryFactory( Schema.RBM_SCHEDULE_PTN );

	public Schedule( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String code ) {
		return Record.createMap( "code", code );
	}

	public static Map<String, Object> createPrimary( String code, char patternType, int patternIndex, java.util.Date patternDate, String time ) {
		Map<String, Object> primaryMap = SchedulePattern.createPatternMap( patternType, patternIndex, patternDate );

		primaryMap.put( "code", code );
		primaryMap.put( "time", time );

		return primaryMap;
	}

	public static Map<String, Object> createPrimary( String code, Object patternType, Object patternIndex, Object patternDate, String time ) {
		Map<String, Object> primaryMap = SchedulePattern.createPatternMap( patternType, patternIndex, patternDate );

		primaryMap.put( "code", code );
		primaryMap.put( "time", time );

		return primaryMap;
	}

	public boolean deletePattern( Map<String, Object> primaryMap ) throws DataException, SQLException {
		return SQLManager.manageRecord( handler, table_dtl, primaryMap, Record.DELETE );
	}

	public int deletePatterns( Map<String, Object> primaryMap ) throws DataException, SQLException {
		return deletePatterns( Record.extractString(primaryMap, "code") );
	}

	public int deletePatterns( String code ) throws DataException, SQLException {
		return SQLManager.executeStatement( handler, "DELETE RBM_SCHEDULE_PTN WHERE SCHCD = ?", code );
	}

	public List<Map<String, Object>> getPatterns( Map<String, ? extends Object> primaryMap ) throws SQLException {
		return getPatterns( Record.extractString(primaryMap, "code") );
	}

	public List<Map<String, Object>> getPatterns( Map<String, ? extends Object> primaryMap, String[] fieldKeys ) throws SQLException {
		return getPatterns( Record.extractString(primaryMap, "code"), fieldKeys );
	}

	public List<Map<String, Object>> getPatterns( String code ) throws SQLException {
		if( code == null || code.length() == 0 ) return null;

		QueryBuffer querybuf = factory_dtl.setQuery( new ConditionQueryBuffer(Record.createMap("code", code)) );
		appendOrderBy( querybuf, factory_dtl );

		return SQLManager.getRecordList( handler, querybuf, 0, -1 );
	}

	public List<Map<String, Object>> getPatterns( String code, String[] fieldKeys ) throws SQLException {
		if( code == null || code.length() == 0 ) return null;

		QueryBuffer querybuf = factory_dtl.setQuery( new ConditionQueryBuffer(Record.createMap("code", code)), fieldKeys );
		appendOrderBy( querybuf, factory_dtl );

		return SQLManager.getRecordList( handler, querybuf, 0, -1 );
	}

	public boolean modifyPattern( Map<String, Object> recordMap ) throws DataException, SQLException {
		return SQLManager.manageRecord( handler, table_dtl, recordMap, Record.INSERT );
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		if( super.regist(recordMap) ) {
			if( recordMap.get("code") == null )
				recordMap.put( "code", SQLManager.getObjectValue( handler, "SELECT 'S' || seqRBM_SCHEDULE.CURRVAL FROM DUAL" ) );

			return true;
		}

		return false;
	}

	@Override
	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException {
		return registEach( records );
	}

	public boolean registPattern( Map<String, Object> recordMap ) throws DataException, SQLException {
		return SQLManager.manageRecord( handler, table_dtl, recordMap, Record.INSERT );
	}

	public int registPatterns( Collection<Map<String, Object>> patterns ) throws DataException, SQLException {
		DataResult result = SQLManager.manageRecordAll( handler, table_dtl, patterns, Record.INSERT );
		if( result.getErrorCount() > 0 )
			throw result.getException();

		return result.getSuccessCount();
	}

	public void updateNextScheduleDateTime( Map<String, Object> primaryMap ) throws DataException, SQLException {
		updateNextScheduleDateTime( Record.extractString(primaryMap, "code") );
	}

	public void updateNextScheduleDateTime( String code ) throws DataException, SQLException {
		SQLManager.callStatement( handler, "call pkRBMSchedule.pUpdateNextSchDateTime( ? )", code );
	}

	public void updateNextScheduleDateTimeToNull( Map<String, Object> primaryMap ) throws DataException, SQLException {
		updateNextScheduleDateTimeToNull( Record.extractString(primaryMap, "code") );
	}

	public void updateNextScheduleDateTimeToNull( String code ) throws DataException, SQLException {
		SQLManager.executeStatement( handler, "UPDATE RBM_SCHEDULE SET NEXTSCHDATETIME = NULL WHERE SCH_CD = ?", code );
	}
}
