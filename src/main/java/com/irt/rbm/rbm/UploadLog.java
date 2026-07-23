/*
 *	File Name:	UploadLog.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/05/31		2.2.1	completeLog(): resultMap에 "registCount", "modifyCount", "deleteCount", "ignoreCount" 추가
 *										createNullLogger() 추가
 *	stghr12		2008/03/31		2.2.0	create
 *
 **/

package com.irt.rbm.rbm;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.data.format.RecordFormat;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class UploadLog extends com.irt.rbm.QueryableManagerImpl {
	private final static Table table = Schema.findTable( Schema.RBM_UPLOADLOG );
	private final static Table table_dtl = Schema.findTable( Schema.RBM_UPLOADLOG_DETAIL );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.RBM_UPLOADLOG );
	private final static QueryFactory factory_dtl = Schema.findQueryFactory( Schema.RBM_UPLOADLOG_DETAIL );
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( UploadLog.class );

	public UploadLog( SQLHandler handler ) {
		super( handler, factory );
	}

	public UploadLog.Logger createLogger( Map<String, ? extends Object> logMap, RecordFormat messageFormat, RecordFormat lineNameFormat )
			throws SQLException {
		return new UploadLog.Logger( logMap, messageFormat, lineNameFormat, true );
	}

	public String createLogId() throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT 'LOG' || seqRBM_UPLOADLOG.NEXTVAL FROM DUAL" );
	}

	public UploadLog.Logger createNullLogger( Map<String, ? extends Object> logMap, RecordFormat messageFormat, RecordFormat lineNameFormat )
			throws SQLException {
		return new UploadLog.Logger( logMap, messageFormat, lineNameFormat, false );
	}

	public static Map<String, Object> createPrimary( String logId ) {
		return Record.createMap( "logId", logId );
	}

	public int getDetailCount( Map<String, ? extends Object> conditionMap ) throws SQLException {
		QueryBuffer querybuf = factory_dtl.setCountQuery( new ConditionQueryBuffer(conditionMap) );
		return SQLManager.getInt( handler, querybuf );
	}

	public List<Map<String, Object>> getDetails( Map<String, ? extends Object> conditionMap ) throws SQLException {
		return getDetails( conditionMap, 0, -1 );
	}

	public List<Map<String, Object>> getDetails( Map<String, ? extends Object> conditionMap, int skipRows, int maxRows ) throws SQLException {
		QueryBuffer querybuf = factory_dtl.setQuery( new ConditionQueryBuffer(conditionMap) );
		appendOrderBy( querybuf, factory_dtl );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	public List<Map<String, Object>> getDetails( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException {
		return getDetails( conditionMap, fieldKeys );
	}

	public List<Map<String, Object>> getDetails( Map<String, ? extends Object> conditionMap, String[] fieldKeys, int skipRows, int maxRows )
			throws SQLException {
		QueryBuffer querybuf = factory_dtl.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		appendOrderBy( querybuf, factory_dtl );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	public List<Map<String, Object>> getUploadTypes() throws SQLException {
		return SQLManager.getRecordList( handler,
				"SELECT SUBSTRB(PKG_CD, 19) \"code\", PKG_NAME \"name\""
						+" FROM SYS_SYSTEM_PKG WHERE SYSTEMCD = 'RBM' AND PKG_CD LIKE 'RBMUploadLog.TYPE.%'"
						+" ORDER BY 2"
						, null );
	}

	@Override
	protected QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) {
		try {
			return table.setPrimaryConditionQuery( querybuf );
		} catch( FieldException fieldEx ) {
			return null;
		}
	}

	/**
	 *
	 */
	public class Logger implements com.irt.data.DataLoader.Logger {
		String logId;
		boolean writing;
		Map<String, Object> resultMap;
		RecordFormat messageFormat, lineNameFormat;

		long elapsedMilliTime, startedMilliTime;
		com.irt.data.Timestamp startTimestamp;
		List<Map<String, Object>> resultList = new java.util.ArrayList<Map<String, Object>>();

		Logger( Map<String, ? extends Object> logMap, RecordFormat messageFormat, RecordFormat lineNameFormat, boolean writing ) throws SQLException {
			this.logId = createLogId();
			this.writing = writing;
			this.messageFormat = messageFormat;
			this.lineNameFormat = lineNameFormat;

			this.resultMap = new java.util.HashMap<String, Object>();
			this.resultMap.put( "logId", this.logId );
			this.resultMap.put( "systemCode", logMap.get("systemCode") );
			this.resultMap.put( "uploadType", logMap.get("uploadType") );
			this.resultMap.put( "uploadFileName", logMap.get("uploadFileName") );
			this.resultMap.put( "fileName", logMap.get("fileName") );
			this.resultMap.put( "fileType", logMap.get("fileType") );
			this.resultMap.put( "encoding", logMap.get("encoding") );
			this.resultMap.put( "userId", logMap.get("userId") );
		}

		@Override
		public void completeLog( DataResult result, String status, String errorMessage ) {
			try {
				if( writing && resultList.size() > 0 ) {
					DataResult dataResult = SQLManager.manageRecordAll( handler, table_dtl, resultList, Record.INSERT );
					if( dataResult.getException() != null )
						throw dataResult.getException();

					resultList.clear();
				}

				resultMap.put( "lineCount", new Integer(result.getCount()) );
				resultMap.put( "registCount", new Integer(result.getRegistCount()) );
				resultMap.put( "modifyCount", new Integer(result.getModifyCount()) );
				resultMap.put( "deleteCount", new Integer(result.getDeleteCount()) );
				resultMap.put( "successCount", new Integer(result.getSuccessCount()) );
				resultMap.put( "ignoreCount", new Integer(result.getIgnoreCount()) );
				resultMap.put( "warningCount", new Integer(result.getWarningCount()) );
				resultMap.put( "errorCount", new Integer(result.getErrorCount()) );
				resultMap.put( "endDateTime", new com.irt.data.Timestamp(handler.getTimeZone()) );
				resultMap.put( "status", status );
				if( errorMessage != null )
					resultMap.put( "message", errorMessage );
				else
					resultMap.put( "message", messageFormat.format(resultMap, handler.getMessageHandler()) );
				logger.debug( "UploadLog["+ logId +"] completeLog("+ resultMap +")." );

				if( writing ) {
					SQLManager.manageRecord( handler, table, resultMap, Record.UPDATE );
					handler.commit();
				}
			} catch( DataException dataEx ) {
				logger.error( "UploadLog["+ logId +"] error.", dataEx );
			} catch( SQLException sqlEx ) {
				logger.error( "UploadLog["+ logId +"] error.", sqlEx );
			}

			this.elapsedMilliTime = System.currentTimeMillis() - this.startedMilliTime;
		}

		@Override
		public long getElapsedMilliTime() {
			return elapsedMilliTime;
		}

		@Override
		public String getLogId() {
			return logId;
		}

		@Override
		public Map<String, Object> getResultMap() {
			return resultMap;
		}

		@Override
		public long getStartTimeMillis() {
			return startedMilliTime;
		}

		@Override
		public com.irt.data.Timestamp getStartTimestamp() {
			return startTimestamp;
		}

		private Map<String, Object> makeMap( Map<String, Object> recordMap, Map<String, Object> resultMap ) {
			resultMap = new java.util.HashMap<String, Object>( resultMap );
			resultMap.put( "logId", logId );
			if( recordMap != null && lineNameFormat != null )
				resultMap.put( "lineName", lineNameFormat.format(recordMap, handler.getMessageHandler()) );

			return resultMap;
		}

		@Override
		public void pushLog( Map<String, Object> recordMap, Map<String, Object> resultMap ) {
			if( writing ) resultList.add( recordMap = makeMap( recordMap, resultMap ) );
			logger.debug( "UploadLog["+ logId +"] pushLog("+ recordMap +")." );
		}

		@Override
		public void startLog() {
			this.startedMilliTime = System.currentTimeMillis();
			this.startTimestamp = new com.irt.data.Timestamp( handler.getTimeZone() );

			Map<String, Object> recordMap = new java.util.HashMap<String, Object>( resultMap );
			recordMap.put( "status", com.irt.data.DataLoader.RUNNING );
			recordMap.put( "startDateTime", this.startTimestamp );
			try {
				if( writing ) {
					SQLManager.manageRecord( handler, table, recordMap, Record.INSERT );
					handler.commit();
				}

				logger.debug( "UploadLog["+ logId +"] startLog()." );
			} catch( DataException dataEx ) {
				logger.error( "UploadLog["+ logId +"] error.", dataEx );
			} catch( SQLException sqlEx ) {
				logger.error( "UploadLog["+ logId +"] error.", sqlEx );
			}
		}

		@Override
		public void writeLog( Map<String, Object> recordMap, Map<String, Object> resultMap ) {
			try {
				recordMap = makeMap( recordMap, resultMap );
				logger.debug( "UploadLog["+ logId +"] writeLog("+ recordMap +")." );

				if( writing ) {
					SQLManager.manageRecord( handler, table_dtl, recordMap, Record.INSERT );
					handler.commit();
				}
			} catch( DataException dataEx ) {
				logger.error( "UploadLog["+ logId +"] error.", dataEx );
			} catch( SQLException sqlEx ) {
				logger.error( "UploadLog["+ logId +"] error.", sqlEx );
			}
		}
	}
}
