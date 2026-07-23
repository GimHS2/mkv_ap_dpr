/*
 *	File Name:	SQLManager.java
 *	Version:	2.2.10c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2026/03/31		2.2.10c	getRecordMap(): DateTime 필드의 경우 "SYS", "TimeZone;DBTimeZone" 환경설정에 따라서 시간이 변환되도록 수정
 *	jbaek		2020/06/30		2.2.10c	org.apache.log4j->org.slf4j로 변경
 *	GimHS		2016/05/31		2.2.10	getRecordMap(): Map에 들어가는 columnKey들을 순서대로 가지고 있는 String 배열을 Map 셋팅
 *	GimHS		2016/02/29		2.2.9	write(): SSDataWriter 사용시 "nofgcolor" 값을 활용하여 데이터값에 따라 ForegroundColor를 셋팅
 *												 recordMap에 "rowNumber" 값 추가
 *	jbaek		2018/10/30		2.2.8c	getRecordMap(): DateTime 칼럼일때 "{columnName}Zone" 추가
 *	jbaek		2017/09/30		2.2.8c	SQLManager.getDBTimeZone() 추가
 *	stghr12		2011/06/30		2.2.8	getTimestampValue() 추가
 *	stghr12		2011/02/28		2.2.7	executeBatch() return값을 int[]로 변경, executeBatch(stmt, rowcount) 추가
 *	stghr12		2010/08/31		2.2.6	executeBatch() 추가
 *	stghr12		2010/02/28		2.2.5	read(): executeType "F" 지원
 *	stghr12		2009/03/31		2.2.4	saveQuery() 추가
 *	stghr12		2009/01/10		2.2.3	write( ..., maxRows ) 추가, writeTitle() 추가
 *	stghr12		2008/05/31		2.2.2	write( handler, out, fieldKeys, ... ) 추가
 *										write(): writingOption 추가(writingExecuteType 포함), recordMap.put("rownum") 추가
 *	stghr12		2008/03/31		2.2.1	getRecordMap(): logger.debug() 추가
 *										(Calendar.getInstance()).getTimeInMillis() -> System.currentTimeMillis()
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										log4j 사용
 *										fieldMap 사용하는 부분 삭제
 *										bindVariable(), bindVariables(): 86400000L 부분 제거
 *										getRecordList( handler, pstmt ) 추가
 *										getRecordMap( map, rset, meta ): Parameter에 TimeZone 추가
 *	stghr12		2007/07/31		2.1.2	bindVariable(), getRecordMap(): TIMESTAMP 처리
 *	stghr12		2007/04/30		2.1.1	read() 추가
 *										write( handler, out, columnList, query, bindVars, fieldMap, containsExecuteType ) 추가
 *										write( handler, out, columnList, querybuf, fieldMap, containsExecuteType ) 추가
 *										write( handler, out, formats, query, bindVars, fieldMap, containsExecuteType ) 추가
 *										write( handler, out, formats, querybuf, fieldMap, containsExecuteType ) 추가
 *										ORA-17070 오류 처리 추가
 *	stghr12		2006/12/01		2.1.0	bindVariable(), deleteRecord(), deleteRecordAll(), lockTable(), write() 추가
 *										Table.STATEMENT_* -> Record.*
 *										DEFAULT_LIST_INITCAPACITY, DEFAULT_BUFFER_SIZE, MAXIMUM_CLOB_SIZE private -> public 변경
 *										manageRecord(), manageRecordAll(): statementType에 (Record.INSERT | Record.UPDATE) 처리 추가
 *										manageRecordAll(SQLHandler, Table, Collection): manageRecordAll(SQLHandler, Table, Collection, int)로 통합
 *										manageRecordAll() return type 변경: Collection -> DataResult
 *										updateRecordAll() return type 변경: Collection -> DataResult
 *	stghr12		2006/09/01		2.0.1	DateFormat 동기화 오류 수정
 *	stghr12		2006/02/28		2.0.0	create
 *
 **/

package com.irt.sql;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;
import com.irt.data.cols.ColumnUtility;
import com.irt.data.format.RecordFormat;
import java.io.InputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 *
 */
public class SQLManager {
	public final static int DEFAULT_LIST_INITCAPACITY	= 50;
	public final static int DEFAULT_BUFFER_SIZE			= 10240;
	public final static int DEFAULT_BATCH_ROWS			= 2000;
	public final static int MAXIMUM_CLOB_SIZE			= 10240;

	private final static int OPT_WRITING_ROWNUMBER		= QueryableManager.OPT_WRITING_ROWNUMBER;
	private final static int OPT_WRITING_EXECUTETYPE	= QueryableManager.OPT_WRITING_EXECUTETYPE;

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger( SQLManager.class );

	/**
	 * bind Variable을 설정.
	 * @return 다음 BIND할 위치
	 */
	public static int bindVariable( PreparedStatement pstmt, int bindIdx, Object bindVar ) throws SQLException {
		if( bindVar == null )
			pstmt.setNull( bindIdx++, Types.VARCHAR );
		else if( bindVar instanceof java.util.Date ) {
			if( bindVar instanceof java.sql.Date )
				pstmt.setDate( bindIdx++, (java.sql.Date)bindVar );
			else if( bindVar instanceof java.sql.Timestamp )
				pstmt.setTimestamp( bindIdx++, (java.sql.Timestamp)bindVar );
			else
				pstmt.setTimestamp( bindIdx++, new java.sql.Timestamp(((java.util.Date)bindVar).getTime()) );
		} else {
			try {
				pstmt.setObject( bindIdx, bindVar );
			} catch( SQLException sqlEx ) {
				if( sqlEx.getErrorCode() == 17070 && bindVar instanceof String )		// ORA-17070 : VARCHAR2 최대크기 오류
					pstmt.setCharacterStream( bindIdx, new java.io.StringReader((String)bindVar), ((String)bindVar).length() );
				else
					throw sqlEx;
			}
			bindIdx++;
		}

		return bindIdx;
	}

	/**
	 * bind Variable을 설정.
	 * @return 다음 BIND할 위치
	 * @see #bindVariables(PreparedStatement, Object[], int) bindVariables
	 */
	public static int bindVariables( PreparedStatement pstmt, Object[] bindVars ) throws SQLException {
		return bindVariables( pstmt, bindVars, 1 );
	}

	/**
	 * bind Variable을 설정.
	 * @param bindIdx	BIND를 시작할 위치(1부터 시작)
	 * @return 다음 BIND할 위치
	 */
	public static int bindVariables( PreparedStatement pstmt, Object[] bindVars, int bindIdx ) throws SQLException {
		if( bindVars != null ) {
			for( Object bindVar : bindVars ) {
				if( bindVar == null )
					pstmt.setNull( bindIdx++, Types.VARCHAR );
				else if( bindVar instanceof java.util.Date ) {
					if( bindVar instanceof java.sql.Date )
						pstmt.setDate( bindIdx++, (java.sql.Date)bindVar );
					else if( bindVar instanceof java.sql.Timestamp )
						pstmt.setTimestamp( bindIdx++, (java.sql.Timestamp)bindVar );
					else
						pstmt.setTimestamp( bindIdx++, new java.sql.Timestamp(((java.util.Date)bindVar).getTime()) );
				} else {
					try {
						pstmt.setObject( bindIdx, bindVar );
					} catch( SQLException sqlEx ) {
						if( sqlEx.getErrorCode() == 17070 && bindVar instanceof String )		// ORA-17070 : VARCHAR2 최대크기 오류
							pstmt.setCharacterStream( bindIdx, new java.io.StringReader((String)bindVar), ((String)bindVar).length() );
						else
							throw sqlEx;
					}
					bindIdx++;
				}
			}
		}

		return bindIdx;
	}

	/**
	 * statement를 수행( CallableStatement 사용 )
	 */
	public static void callStatement( SQLHandler handler, String statement, Object... bindVars ) throws DataException, SQLException {
		CallableStatement cstmt = handler.getConnection().prepareCall( statement );
		try {
			bindVariables( cstmt, bindVars );
			cstmt.executeUpdate();
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx );
		} finally {
			try { cstmt.close(); } catch( Exception ex ) {}
		}
	}

	/**
	 * statement를 수행( CallableStatement 사용 )
	 */
	public static void callStatement( SQLHandler handler, String statement, ValidableField[] fields, Map recordMap )
			throws DataException, SQLException {
		CallableStatement cstmt = handler.getConnection().prepareCall( statement );
		try {
			bindVariables( cstmt, Record.validate(recordMap, fields) );
			cstmt.executeUpdate();
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx, recordMap );
		} finally {
			try { cstmt.close(); } catch( Exception ex ) {}
		}
	}

	/**
	 * statement를 수행( CallableStatement 사용 )
	 */
	public static int callStatementInt( SQLHandler handler, String statement, Object... bindVars ) throws DataException, SQLException {
		CallableStatement cstmt = handler.getConnection().prepareCall( statement );
		try {
			cstmt.registerOutParameter( 1, Types.INTEGER );
			bindVariables( cstmt, bindVars, 2 );
			cstmt.executeUpdate();

			return cstmt.getInt(1);
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx );
		} finally {
			try { cstmt.close(); } catch( Exception ex ) {}
		}
	}

	/**
	 * statement를 수행( CallableStatement 사용 )
	 */
	public static int callStatementInt( SQLHandler handler, String statement, ValidableField[] fields, Map recordMap )
			throws DataException, SQLException {
		CallableStatement cstmt = handler.getConnection().prepareCall( statement );
		try {
			cstmt.registerOutParameter( 1, Types.INTEGER );
			bindVariables( cstmt, Record.validate(recordMap, fields), 2 );
			cstmt.executeUpdate();

			return cstmt.getInt(1);
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx, recordMap );
		} finally {
			try { cstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static int deleteRecord( SQLHandler handler, HierarchyCodeField field, String code ) throws DataException, SQLException {
		String statement = "DELETE "+ field.getTable().getTableName() +" WHERE "+ field.getFieldName() +" LIKE ? || '%'";
		return executeStatement( handler, statement, code );
	}

	public static DataResult deleteRecordAll( SQLHandler handler, HierarchyCodeField field, Collection<? extends Map> records )
			throws SQLException {
		PreparedStatement pstmt = handler.getConnection().prepareStatement(
				"DELETE "+ field.getTable().getTableName() +" WHERE "+ field.getFieldName() +" LIKE ? || '%'"
				);
		try {
			DataResult result = new DataResult();

			int idx = 0;
			for( java.util.Iterator<? extends Map> iterator = records.iterator(); iterator.hasNext(); idx++ ) {
				Map primaryMap = iterator.next();

				try {
					try {
						pstmt.setObject( 1, field.validate(primaryMap) );
						if( pstmt.executeUpdate() == 0 )
							throw handler.createDataException( DataException.ERR_NO_RECORD_DELETE, primaryMap );
					} catch( FieldException fieldEx ) {
						throw handler.createDataException( DataException.ERR_NO_RECORD_DELETE, primaryMap );
					} catch( SQLException sqlEx ) {
						throw handler.createDataException( sqlEx, primaryMap );
					}
				} catch( DataException datEx ) {
					result.appendError( idx, datEx );
				}
			}

			return result;
		} finally {
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static int executeBatch( Statement stmt, int rowcount ) throws SQLException {
		int execcount = 0;
		int[] counts = stmt.executeBatch();
		for( int count : counts ) {
			if( count == Statement.SUCCESS_NO_INFO )
				return rowcount;
			execcount += count;
		}

		return execcount;
	}

	public static int[] executeBatch( SQLHandler handler, String query, SQLHandler to_handler, String statement ) throws SQLException {
		return executeBatch( handler, query, null, to_handler, statement );
	}

	public static int[] executeBatch( SQLHandler handler, String query, Object[] bindVars, SQLHandler to_handler, String statement )
			throws SQLException {
		int rowcount = 0;
		int execcount = 0;

		PreparedStatement pstmt = null, pstmt_upd = null;
		try {
			pstmt = handler.getConnection().prepareStatement( query );
			pstmt_upd = to_handler.getConnection().prepareStatement( statement );

			if( bindVars != null ) SQLManager.bindVariables( pstmt, bindVars );
			ResultSet rset = pstmt.executeQuery();
			int columnCount = rset.getMetaData().getColumnCount();
			try {
				bindVars = new Object[ columnCount ];

				while( rset.next() ) {
					for( int i = 0; i < columnCount; i++ )
						bindVars[i] = rset.getObject( i+1 );

					SQLManager.bindVariables( pstmt_upd, bindVars );
					pstmt_upd.addBatch();

					if( (++rowcount % DEFAULT_BATCH_ROWS) == 0 ) {
						int[] counts = pstmt_upd.executeBatch();

						if( execcount != Statement.SUCCESS_NO_INFO ) {
							for( int count : counts ) {
								if( count == Statement.SUCCESS_NO_INFO ) {
									execcount = Statement.SUCCESS_NO_INFO;
									break;
								} else if( count >= 0 )
									execcount += count;
							}
						}
					}
				}
				if( (rowcount % DEFAULT_BATCH_ROWS) > 0 ) {
					int[] counts = pstmt_upd.executeBatch();

					if( execcount != Statement.SUCCESS_NO_INFO ) {
						for( int count : counts ) {
							if( count == Statement.SUCCESS_NO_INFO ) {
								execcount = Statement.SUCCESS_NO_INFO;
								break;
							} else if( count >= 0 )
								execcount += count;
						}
					}
				}
			} finally {
				try { rset.close(); } catch( Exception ignored ) {}
			}
		} finally {
			try { pstmt.close(); } catch( Exception ignored ) {}
			try { pstmt_upd.close(); } catch( Exception ignored ) {}
		}

		return new int[] { rowcount, execcount };
	}

	/**
	 * statement를 수행( PreparedStatement 사용 )
	 */
	public static int executeStatement( SQLHandler handler, String statement, Object... bindVars ) throws DataException, SQLException {
		PreparedStatement pstmt = handler.getConnection().prepareStatement( statement );
		try {
			bindVariables( pstmt, bindVars );
			return pstmt.executeUpdate();
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx );
		} finally {
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	/**
	 * statement를 수행( PreparedStatement 사용 )
	 */
	public static int executeStatement( SQLHandler handler, String statement, ValidableField[] fields, Map recordMap )
			throws DataException, SQLException {
		PreparedStatement pstmt = handler.getConnection().prepareStatement( statement );
		try {
			bindVariables( pstmt, Record.validate(recordMap, fields) );
			return pstmt.executeUpdate();
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx, recordMap );
		} finally {
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	/**
	 * blob를 저장.
	 **/
	public static long fillBlob( SQLHandler handler, Blob blob, InputStream instream ) throws IOException, SQLException {
		return fillBlob( handler, blob, instream, DEFAULT_BUFFER_SIZE );
	}

	/**
	 * blob를 저장.
	 **/
	public static long fillBlob( SQLHandler handler, Blob blob, InputStream instream, int bufferSize ) throws IOException, SQLException {
		CallableStatement cstmt = handler.getConnection().prepareCall( "BEGIN DBMS_LOB.WRITE(?, ?, ?, ?); END;" );

		try {
			long bytes = 0;
			byte buf[] = new byte[bufferSize + 1];

			while( true ) {
				int chunk = instream.read( buf, 0, bufferSize );
				if( chunk <= 0 ) break;
				cstmt.setBlob( 1, blob );
				cstmt.setLong( 2, chunk );
				cstmt.setLong( 3, bytes+1 );
				cstmt.setBytes( 4, buf );
				cstmt.execute();
				bytes += chunk;
			}

			return bytes;
		} finally {
			try { cstmt.close(); } catch( Exception ex ) {}
		}
	}

	/**
	 * JVM 서버가 서울에 있고(JVM의 OS타임존이 서울일 경우), 데이터베이스가 싱가폴에 있으면,
	 * SQLManager는 TimeZone을 사용시 서울이라고 설정하게 된다.
	 * 이렇게 되면 시간이 맞지 않게되므로, 이러한 경우를 방지하기 위해서 이 메소드를 사용하여 irt가 공통으로 사용하는 DBTimeZone이라는 환경 설정을 우선시하여 설정을 사용하게 하기 위해서 필요하다.
	 *
	 * DBTimeZone value resolve Priority ( High to Low )<br/>
	 * if found from High then use the High Priority value;
	 * <pre>
	 * Web Application
	 * 1. RBMSystem.getSystemEnv("SYS", "TimeZone;DBTimeZone") in {@link com.irt.custom.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)}
	 * 2. TimeZone.getDefault() resolve Priority
	 * 2.1. "-Duser.timezone" on application loading
	 * 2.2. application host timezone
	 *
	 * Tool Application
	 * 1. RBMSystem.getSystemEnv("SYS", "TimeZone;DBTimeZone") in {@link com.irt.rbm.tools.RBMTools#init(Properties)}
	 * 2. TimeZone.getDefault() resolve Priority
	 * 2.1. "-Duser.timezone" on application loading
	 * 2.2. application host timezone
	 * </pre>
	 *
	 * <br/></br/>
	 * TODO: add SQLHandler.getDBTimeZone()
	 * @return
	 */
	public static TimeZone getDBTimeZone() {
		String dbTimeZone = System.getProperty("DBTimeZone");
		if(dbTimeZone != null && dbTimeZone.length() > 0 ) {
			return TimeZone.getTimeZone(dbTimeZone);
		} else {
			return TimeZone.getDefault();
		}
	}

	/**
	 * query를 수행해서 첫번째 값을 int형으로 return.(개수등을 구할 때 사용)
	 */
	public static int getInt( SQLHandler handler, QueryBuffer querybuf ) throws SQLException {
		if( handler.debugging() ) {
			long timemillis = System.currentTimeMillis();
			try {
				return getInt( handler, querybuf.getQuery(), querybuf.getBindVariables() );
			} finally {
				handler.saveQuery( querybuf, System.currentTimeMillis() - timemillis );
			}
		} else
			return getInt( handler, querybuf.getQuery(), querybuf.getBindVariables() );
	}

	/**
	 * query를 수행해서 첫번째 값을 int형으로 return.(개수등을 구할 때 사용)
	 */
	public static int getInt( SQLHandler handler, String query, Object... bindVars ) throws SQLException {
		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "getInt()", query, bindVars );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( query );
		ResultSet rset = null;
		try {
			bindVariables( pstmt, bindVars );
			rset = pstmt.executeQuery();
			return ( rset.next() ? rset.getInt(1) : -1 );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	/**
	 * query를 수행해서 첫번째 값을 Object형으로 return.
	 */
	public static Object getObjectValue( SQLHandler handler, QueryBuffer querybuf ) throws SQLException {
		if( handler.debugging() ) {
			long timemillis = System.currentTimeMillis();
			try {
				return getObjectValue( handler, querybuf.getQuery(), querybuf.getBindVariables() );
			} finally {
				handler.saveQuery( querybuf, System.currentTimeMillis() - timemillis );
			}
		} else
			return getObjectValue( handler, querybuf.getQuery(), querybuf.getBindVariables() );
	}

	/**
	 * query를 수행해서 첫번째 값을 Object형으로 return.
	 */
	public static Object getObjectValue( SQLHandler handler, String query, Object... bindVars ) throws SQLException {
		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "getObjectValue()", query, bindVars );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( query );
		ResultSet rset = null;
		try {
			bindVariables( pstmt, bindVars );
			rset = pstmt.executeQuery();
			return ( rset.next() ? rset.getObject(1) : null );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static List<Map<String, Object>> getRecordList( SQLHandler handler, QueryBuffer querybuf ) throws SQLException {
		return getRecordList( handler, querybuf, 0, -1 );
	}

	public static List<Map<String, Object>> getRecordList( SQLHandler handler, QueryBuffer querybuf, int skipRows, int maxRows ) throws SQLException {
		if( handler.debugging() ) {
			long timemillis = System.currentTimeMillis();
			try {
				return getRecordList( handler, querybuf.getQuery(), querybuf.getBindVariables(), skipRows, maxRows );
			} finally {
				handler.saveQuery( querybuf, System.currentTimeMillis() - timemillis );
			}
		} else
			return getRecordList( handler, querybuf.getQuery(), querybuf.getBindVariables(), skipRows, maxRows );
	}

	public static List<Map<String, Object>> getRecordList( SQLHandler handler, String query, Object[] bindVars ) throws SQLException {
		return getRecordList( handler, query, bindVars, 0, -1 );
	}

	public static List<Map<String, Object>> getRecordList( SQLHandler handler, String query, Object[] bindVars, int skipRows, int maxRows )
			throws SQLException {
		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "getRecordList()", query, bindVars );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( query );
		try {
			bindVariables( pstmt, bindVars );
			return getRecordList( handler, pstmt, skipRows, maxRows );
		} finally {
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static List<Map<String, Object>> getRecordList( SQLHandler handler, PreparedStatement pstmt ) throws SQLException {
		return getRecordList( handler, pstmt, 0, -1 );
	}

	public static List<Map<String, Object>> getRecordList( SQLHandler handler, PreparedStatement pstmt, int skipRows, int maxRows )
			throws SQLException {
		ResultSet rset = null;
		try {
			rset = pstmt.executeQuery();
			while( skipRows-- > 0 && rset.next() );

			List<Map<String, Object>> list;
			if( maxRows > DEFAULT_LIST_INITCAPACITY || maxRows <= 0 )
				list = new ArrayList<Map<String, Object>>( DEFAULT_LIST_INITCAPACITY );
			else
				list = new ArrayList<Map<String, Object>>( maxRows );

			ResultSetMetaData meta = rset.getMetaData();
			while( rset.next() && maxRows-- != 0 )
				list.add( getRecordMap(null, rset, meta, handler.getTimeZone()) );

			return( list.size() == 0 ? null : list );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
		}
	}

	public static Map<String, Object> getRecordMap( SQLHandler handler, Map<String, Object> map, QueryBuffer querybuf ) throws SQLException {
		if( handler.debugging() ) {
			long timemillis = System.currentTimeMillis();
			try {
				return getRecordMap( handler, map, querybuf.getQuery(), querybuf.getBindVariables() );
			} finally {
				handler.saveQuery( querybuf, System.currentTimeMillis() - timemillis );
			}
		} else
			return getRecordMap( handler, map, querybuf.getQuery(), querybuf.getBindVariables() );
	}

	/**
	 *
	 */
	public static Map<String, Object> getRecordMap( SQLHandler handler, Map<String, Object> map, String query, Object... bindVars )
			throws SQLException {
		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "getRecordMap()", query, bindVars );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( query );
		ResultSet rset = null;
		try {
			bindVariables( pstmt, bindVars );
			rset = pstmt.executeQuery();
			if( rset.next() )
				return getRecordMap( map, rset, rset.getMetaData(), handler.getTimeZone() );
			else
				return null;
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static Map<String, Object> getRecordMap( Map<String, Object> map, ResultSet rset, ResultSetMetaData meta, TimeZone zone )
			throws SQLException {
		String[] columnKeys = new String[ meta.getColumnCount() ];
		if( map == null ) map = new java.util.HashMap<String, Object>( meta.getColumnCount() );

		for( int i = 1; i <= meta.getColumnCount(); i++ ) {
			String columnName = meta.getColumnName(i);
			columnKeys[i - 1] = columnName;

			switch( meta.getColumnType(i) ) {
			case Types.CLOB:
				Clob clob = rset.getClob(i);
				if( clob != null ) {
					int bufferSize = DEFAULT_BUFFER_SIZE;
					StringBuffer sbuf = new StringBuffer();
					for( long pos = 1; true; pos += bufferSize ) {
						String substring = clob.getSubString( pos, bufferSize );
						if( "".equals(substring) || pos > MAXIMUM_CLOB_SIZE ) break;
						sbuf.append( substring );
					}
					map.put( columnName, sbuf.toString() );
				}
				break;
			default:
				Object obj = rset.getObject(i);
				if( obj == null ) continue;
				if( obj instanceof java.util.Date ) {
					if( columnName.endsWith("DateTime") ) {
						String dbTimeZone = com.irt.rbm.RBMSystem.getSystemEnv( "SYS", "TimeZone;DBTimeZone" );
						if( dbTimeZone != null ) {
							java.time.ZonedDateTime zdt = rset.getTimestamp(i).toLocalDateTime().atZone( java.time.ZoneId.of(dbTimeZone) );
							zdt = zdt.withZoneSameInstant( zone.toZoneId() );

							map.put( columnName, new com.irt.data.Timestamp( java.sql.Timestamp.from(zdt.toInstant()), zone ) );
						} else
							map.put( columnName, new com.irt.data.Timestamp( rset.getTimestamp(i), zone ) );
						map.put( columnName + "Zone", zone.getID() );
					} else
						map.put( columnName, com.irt.data.Date.getInstance( (java.util.Date)obj ) );
				} else
					map.put( columnName, obj );
			}
		}
		map.put( "_columnKeys_", columnKeys );

		return map;
	}

	public static String[] getStringValues( SQLHandler handler, QueryBuffer querybuf ) throws SQLException {
		if( handler.debugging() ) {
			long timemillis = System.currentTimeMillis();
			try {
				return getStringValues( handler, querybuf.getQuery(), querybuf.getBindVariables() );
			} finally {
				handler.saveQuery( querybuf, System.currentTimeMillis() - timemillis );
			}
		} else
			return getStringValues( handler, querybuf.getQuery(), querybuf.getBindVariables() );
	}

	/**
	 *
	 */
	public static String[] getStringValues( SQLHandler handler, String query, Object... bindVars ) throws SQLException {
		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "getStringValues()", query, bindVars );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( query );
		ResultSet rset = null;
		try {
			bindVariables( pstmt, bindVars );
			rset = pstmt.executeQuery();

			List<String> valueList = new ArrayList<String>();
			while( rset.next() ) {
				String value = rset.getString(1);
				if( value != null ) valueList.add( value );
			}
			if( valueList.size() == 0 )
				return null;
			else
				return valueList.toArray( new String[valueList.size()] );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	/**
	 * query를 수행해서 첫번째 값을 Timestamp형으로 return.
	 */
	public static com.irt.data.Timestamp getTimestampValue( SQLHandler handler, QueryBuffer querybuf ) throws SQLException {
		if( handler.debugging() ) {
			long timemillis = System.currentTimeMillis();
			try {
				return getTimestampValue( handler, querybuf.getQuery(), querybuf.getBindVariables() );
			} finally {
				handler.saveQuery( querybuf, System.currentTimeMillis() - timemillis );
			}
		} else
			return getTimestampValue( handler, querybuf.getQuery(), querybuf.getBindVariables() );
	}

	/**
	 * query를 수행해서 첫번째 값을 Timestamp형으로 return.
	 */
	public static com.irt.data.Timestamp getTimestampValue( SQLHandler handler, String query, Object... bindVars ) throws SQLException {
		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "getObjectValue()", query, bindVars );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( query );
		ResultSet rset = null;
		try {
			bindVariables( pstmt, bindVars );
			rset = pstmt.executeQuery();
			return ( rset.next() && rset.getTimestamp(1) != null ? new com.irt.data.Timestamp( rset.getTimestamp(1), handler.getTimeZone() ) : null );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	/**
	 * field의 상위 code를 lock(SELECT * FOR UPDATE WAIT)을 한다.
	 * @return lock을 할 필요가 없거나, lock을 성공하면 true를 return, lock할 record가 없으면 false를 return.
	 */
	public static boolean lockTable( SQLHandler handler, HierarchyCodeField field, String code ) throws DataException, SQLException {
		int level = field.getLevel( code );
		if( level > 1 ) {
			String[] codes = field.getUpperLevelCodes( code );

			StringBuffer sbuf = new StringBuffer( "SELECT " );
			sbuf.append( field.getFieldName() ).append( " FROM " ).append( field.getTable().getTableName() );
			sbuf.append( " WHERE CLASSCD = ? AND " ).append( field.getFieldName() ).append( " IN (" );
			for( int i = 0; i < codes.length; i++ ) {
				if( i > 0 ) sbuf.append( "," );
				sbuf.append( "?" );
			}
			sbuf.append( ") FOR UPDATE NOWAIT" );

			PreparedStatement pstmt = handler.getConnection().prepareStatement( sbuf.toString() );
			ResultSet rset = null;
			try {
				pstmt.setString( 1, String.valueOf(level-1) );
				SQLManager.bindVariables( pstmt, codes, 2 );

				rset = pstmt.executeQuery();
				return rset.next();
			} finally {
				try { rset.close(); } catch( Exception ex ) {}
				try { pstmt.close(); } catch( Exception ex ) {}
			}
		}

		return true;
	}

	public static boolean manageRecord( SQLHandler handler, Table table, Map recordMap, int statementType )
			throws DataException, SQLException {
		if( statementType == (Record.INSERT | Record.UPDATE) )
			return ( manageRecord(handler, table, recordMap, Record.UPDATE) || manageRecord(handler, table, recordMap, Record.INSERT) );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( table.makeStatement(statementType) );
		try {
			bindVariables( pstmt, table.validate(recordMap, statementType) );
			return( pstmt.executeUpdate() > 0 );
		} catch( FieldException fieldEx ) {
			if( statementType == Record.DELETE ) return false;
			throw handler.createDataException( fieldEx, recordMap );
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx, recordMap );
		} finally {
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static DataResult manageRecordAll( SQLHandler handler, Table table, Collection<? extends Map> records, int statementType )
			throws SQLException {
		PreparedStatement pstmt = null;
		PreparedStatement pstmt_ins = null;
		ValidableField[] fields = null;
		ValidableField[] fields_ins = null;
		try {
			if( statementType == (Record.INSERT | Record.UPDATE) ) {
				fields_ins = table.getBindFieldArray( Record.INSERT );
				pstmt_ins = handler.getConnection().prepareStatement( table.makeStatement(Record.INSERT) );
				statementType = Record.UPDATE;
			}
			fields = table.getBindFieldArray( statementType );
			pstmt = handler.getConnection().prepareStatement( table.makeStatement(statementType) );

			DataResult result = new DataResult();

			int idx = 0;
			for( java.util.Iterator<? extends Map> iterator = records.iterator(); iterator.hasNext(); idx++ ) {
				Map recordMap = iterator.next();

				try {
					try {
						bindVariables( pstmt, Record.validate(recordMap, fields) );
						if( pstmt.executeUpdate() > 0 )
							result.increaseSuccessCount( statementType );
						else {
							if( statementType == Record.DELETE )
								throw handler.createDataException( DataException.ERR_NO_RECORD_DELETE, recordMap );
							else {
								if( pstmt_ins == null )
									throw handler.createDataException( DataException.ERR_NO_RECORD_UPDATE, recordMap );
								else {
									bindVariables( pstmt_ins, Record.validate(recordMap, fields_ins) );
									pstmt_ins.executeUpdate();
									result.increaseRegistCount();
								}
							}
						}
					} catch( FieldException fieldEx ) {
						if( statementType == Record.DELETE )
							throw handler.createDataException( DataException.ERR_NO_RECORD_DELETE, recordMap );
						else
							throw handler.createDataException( fieldEx, recordMap );
					} catch( SQLException sqlEx ) {
						throw handler.createDataException( sqlEx, recordMap );
					}
				} catch( DataException datEx ) {
					result.appendError( idx, datEx );
				}
			}

			return result;
		} finally {
			try { pstmt.close(); } catch( Exception ex ) {}
			try { pstmt_ins.close(); } catch( Exception ex ) {}
		}
	}

	public static DataResult read( SQLHandler handler, DataReader reader, Table table, String[] fieldKeys, Map<String, ? extends Object> valueMap )
			throws IOException, SQLException {
		return read( handler, reader, table, fieldKeys, valueMap, "A,U,F,D" );
	}

	/**
	 *
	 */
	public static DataResult read( SQLHandler handler, DataReader reader, Table table, String[] fieldKeys, Map<String, ? extends Object> valueMap
			, String validExecuteType ) throws IOException, SQLException {
		DataResult result = new DataResult();

		ValidableField fld_executeType = new ValidableField( false, "executeType", "EXECUTETYPE", "PUB_EXECUTETYPE_", validExecuteType );

		PreparedStatement pstmt_ins = null, pstmt_upd = null, pstmt_del = null;
		try {
			Connection conn = handler.getConnection();
			pstmt_ins = conn.prepareStatement( table.makeStatement(Record.INSERT) );
			pstmt_upd = conn.prepareStatement( table.makeStatement(Record.UPDATE) );
			pstmt_del = conn.prepareStatement( table.makeStatement(Record.DELETE) );

			do {
				// recordMap 읽기
				Map<String, Object> recordMap = null;
				do {
					try {
						recordMap = reader.readNext( fieldKeys );
					} catch ( DataException dataEx ) {
						result.appendError( reader.getLineNumber(), handler.createDataException(dataEx.getErrorKey()) );
					}
				} while( !reader.isEOF() && reader.isBlankLine() );
				if( reader.isEOF() ) break;
				if( recordMap == null ) continue;
				if( valueMap != null ) recordMap.putAll( valueMap );

				// recordMap 처리
				try {
					String executeType = (String)fld_executeType.validate( recordMap );
					if( "A".equals(executeType) ) {
						bindVariables( pstmt_ins, table.validate(recordMap, Record.INSERT) );
						pstmt_ins.executeUpdate();

						result.increaseRegistCount();
					} else if( "U".equals(executeType) ) {
						bindVariables( pstmt_upd, table.validate(recordMap, Record.UPDATE) );
						if( pstmt_upd.executeUpdate() == 0 )
							throw handler.createDataException( DataException.ERR_NO_RECORD_UPDATE, recordMap );

						result.increaseModifyCount();
					} else if( "D".equals(executeType) ) {
						bindVariables( pstmt_del, table.validate(recordMap, Record.DELETE) );
						if( pstmt_del.executeUpdate() == 0 )
							throw handler.createDataException( DataException.ERR_NO_RECORD_DELETE, recordMap );

						result.increaseDeleteCount();
					}
				} catch( DataException dataEx ) {
					result.appendError( reader.getLineNumber(), dataEx );
				} catch( FieldException fieldEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(fieldEx, recordMap) );
				} catch( SQLException sqlEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(sqlEx, recordMap) );
				}
			} while( !reader.isEOF() );
		} finally {
			try { pstmt_ins.close(); } catch( Exception ex ) {}
			try { pstmt_upd.close(); } catch( Exception ex ) {}
			try { pstmt_del.close(); } catch( Exception ex ) {}
		}

		return result;
	}

	public static DataResult read( SQLHandler handler, DataReader reader, ManipulableManager db, String[] fieldKeys
			, Map<String, ? extends Object> valueMap ) throws IOException, SQLException {
		return read( handler, reader, db, fieldKeys, valueMap, "A,U,F,D" );
	}

	/**
	 *
	 */
	public static DataResult read( SQLHandler handler, DataReader reader, ManipulableManager db, String[] fieldKeys
			, Map<String, ? extends Object> valueMap, String validExecuteType ) throws IOException, SQLException {
		DataResult result = new DataResult();

		ValidableField fld_executeType = new ValidableField( false, "executeType", "EXECUTETYPE", "PUB_EXECUTETYPE_", validExecuteType );

		do {
			// recordMap 읽기
			Map<String, Object> recordMap = null;
			do {
				try {
					recordMap = reader.readNext( fieldKeys );
				} catch ( DataException dataEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(dataEx.getErrorKey()) );
				}
			} while( !reader.isEOF() && reader.isBlankLine() );
			if( reader.isEOF() ) break;
			if( recordMap == null ) continue;
			if( valueMap != null ) recordMap.putAll( valueMap );

			// recordMap 처리
			try {
				String executeType = (String)fld_executeType.validate( recordMap );
				if( "A".equals(executeType) ) {
					db.regist( recordMap );

					result.increaseRegistCount();
				} else if( "U".equals(executeType) ) {
					if( !db.modify(recordMap) )
						throw handler.createDataException( DataException.ERR_NO_RECORD_UPDATE, recordMap );

					result.increaseModifyCount();
				} else if( "D".equals(executeType) ) {
					if( !db.delete(recordMap) )
						throw handler.createDataException( DataException.ERR_NO_RECORD_DELETE, recordMap );

					result.increaseDeleteCount();
				}
			} catch( DataException dataEx ) {
				result.appendError( reader.getLineNumber(), dataEx );
			} catch( FieldException fieldEx ) {
				result.appendError( reader.getLineNumber(), handler.createDataException(fieldEx, recordMap) );
			} catch( SQLException sqlEx ) {
				result.appendError( reader.getLineNumber(), handler.createDataException(sqlEx, recordMap) );
			}
		} while( !reader.isEOF() );

		return result;
	}

	public static void saveQuery( SQLHandler handler, QueryBuffer querybuf ) {
		if( handler.debugging() ) handler.saveQuery( querybuf, -1 );
		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "saveQuery()", querybuf );
	}

	public static void saveQuery( SQLHandler handler, QueryBuffer querybuf, long timemillis ) {
		if( handler.debugging() ) handler.saveQuery( querybuf, timemillis );
		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "saveQuery()", querybuf );
	}

	public static boolean updateRecord( SQLHandler handler, Table table, Map recordMap, String[] fieldKeys ) throws DataException, SQLException {
		Table.Field[] tfields = table.getAlterableFieldArray( fieldKeys );
		PreparedStatement pstmt = handler.getConnection().prepareStatement( table.makeUpdateStatement(tfields, false) );
		try {
			int bindIdx = bindVariables( pstmt, Record.validate(recordMap, tfields) );
			try {
				bindVariables( pstmt, table.extractPrimaryValues(recordMap), bindIdx );
				return( pstmt.executeUpdate() > 0 );
			} catch( FieldException fieldEx ) {
				throw handler.createDataException( DataException.ERR_NO_RECORD_UPDATE, recordMap );
			}
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx, recordMap );
		} finally {
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static DataResult updateRecordAll( SQLHandler handler, Table table, Collection<? extends Map> records, String... fieldKeys )
			throws SQLException {
		Table.Field[] tfields = table.getAlterableFieldArray( fieldKeys );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( table.makeUpdateStatement(tfields, false) );
		try {
			DataResult result = new DataResult();

			int idx = 0;
			for( java.util.Iterator<? extends Map> iterator = records.iterator(); iterator.hasNext(); idx++ ) {
				Map recordMap = iterator.next();

				try {
					int bindIdx;
					try {
						bindIdx = SQLManager.bindVariables( pstmt, Record.validate(recordMap, tfields) );
					} catch( FieldException fieldEx ) {
						throw handler.createDataException( fieldEx, recordMap );
					}

					try {
						SQLManager.bindVariables( pstmt, table.extractPrimaryValues(recordMap), bindIdx );
						if( pstmt.executeUpdate() == 0 )
							throw handler.createDataException( DataException.ERR_NO_RECORD_UPDATE, recordMap );
						result.increaseModifyCount();
					} catch( FieldException fieldEx ) {
						throw handler.createDataException( DataException.ERR_NO_RECORD_UPDATE, recordMap );
					} catch( SQLException sqlEx ) {
						throw handler.createDataException( sqlEx, recordMap );
					}
				} catch( DataException datEx ) {
					result.appendError( idx, datEx );
				}
			}

			return result;
		} finally {
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static void write( SQLHandler handler, DataWriter out, ColumnList columnList, QueryBuffer querybuf ) throws IOException, SQLException {
		write( handler, out, columnList, querybuf.getQuery(), querybuf.getBindVariables(), 0, -1 );
	}

	public static void write( SQLHandler handler, DataWriter out, ColumnList columnList, QueryBuffer querybuf, int writingOption )
			throws IOException, SQLException {
		write( handler, out, columnList, querybuf.getQuery(), querybuf.getBindVariables(), writingOption, -1 );
	}

	public static void write( SQLHandler handler, DataWriter out, ColumnList columnList, QueryBuffer querybuf, int writingOption, int maxRows )
			throws IOException, SQLException {
		write( handler, out, columnList, querybuf.getQuery(), querybuf.getBindVariables(), writingOption, maxRows );
	}

	public static void write( SQLHandler handler, DataWriter out, ColumnList columnList, String query, Object[] bindVars )
			throws IOException, SQLException {
		write( handler, out, columnList, query, bindVars, 0, -1 );
	}

	public static void write( SQLHandler handler, DataWriter out, ColumnList columnList, String query, Object[] bindVars, int writingOption )
			throws IOException, SQLException {
		write( handler, out, columnList, query, bindVars, writingOption, -1 );
	}

	/**
	 *
	 */
	public static void write( SQLHandler handler, DataWriter out, ColumnList columnList, String query, Object[] bindVars, int writingOption
			, int maxRows ) throws IOException, SQLException {
		Column[] columns = columnList.getColumns();
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "write", query, bindVars );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( query );
		ResultSet rset = null;
		try {
			bindVariables( pstmt, bindVars );
			rset = pstmt.executeQuery();

			ResultSetMetaData meta = rset.getMetaData();
			for( int rownum = 1; rset.next(); rownum++ ) {
				if( maxRows > 0 && rownum > maxRows ) break;

				Map<String, Object> recordMap = getRecordMap( null, rset, meta, handler.getTimeZone() );
				recordMap.put( "rowNumber", rownum );
				if( (writingOption & OPT_WRITING_ROWNUMBER) > 0 ) out.print( rownum );
				if( (writingOption & OPT_WRITING_EXECUTETYPE) > 0 ) out.print( "U" );
				for( int c = 0; c < columns.length; c++ ) {
					if( out instanceof com.irt.util.SSDataWriter ) {
						com.irt.util.SS.ColumnStyle columnStyle = null;
						String nofgcolorKey = (String)recordMap.get( "nofgcolor" );
						if( nofgcolorKey != null ) {
							String[] keys = nofgcolorKey.split( ";" );
							for( String key : keys ) {
								if( key.equals(columns[c].getFieldKey()) ) {
									if( "Y".equals(recordMap.get(key +"_nofgcolor")) ) {
										columnStyle = ((com.irt.util.SS.ColumnStyle)columns[c].getColumnAttr()).clone();
										columnStyle.setForegroundColor( (short)-2 );
									}
									break;
								}
							}
						}
						if( columnStyle != null )
							((com.irt.util.SSDataWriter)out).print( columns[c].getColumnValue(recordMap, msghandler)
									, columns[c].getColumnSize(), columnStyle );
						else
							out.print( columns[c].getColumnValue(recordMap, msghandler), columns[c].getColumnSize() );
					} else
						out.print( columns[c].getColumnValue(recordMap, msghandler), columns[c].getColumnSize() );
				}
				out.println();
			}
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static void write( SQLHandler handler, DataWriter out, RecordFormat[] formats, QueryBuffer querybuf ) throws IOException, SQLException {
		write( handler, out, formats, querybuf.getQuery(), querybuf.getBindVariables(), 0, -1 );
	}

	public static void write( SQLHandler handler, DataWriter out, RecordFormat[] formats, QueryBuffer querybuf, int writingOption )
			throws IOException, SQLException {
		write( handler, out, formats, querybuf.getQuery(), querybuf.getBindVariables(), writingOption, -1 );
	}

	public static void write( SQLHandler handler, DataWriter out, RecordFormat[] formats, QueryBuffer querybuf, int writingOption, int maxRows )
			throws IOException, SQLException {
		write( handler, out, formats, querybuf.getQuery(), querybuf.getBindVariables(), writingOption, maxRows );
	}

	public static void write( SQLHandler handler, DataWriter out, RecordFormat[] formats, String query, Object[] bindVars )
			throws IOException, SQLException {
		write( handler, out, formats, query, bindVars, 0, -1 );
	}

	public static void write( SQLHandler handler, DataWriter out, RecordFormat[] formats, String query, Object[] bindVars, int writingOption )
			throws IOException, SQLException {
		write( handler, out, formats, query, bindVars, writingOption, -1 );
	}

	/**
	 *
	 */
	public static void write( SQLHandler handler, DataWriter out, RecordFormat[] formats, String query, Object[] bindVars, int writingOption
			, int maxRows ) throws IOException, SQLException {
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "write", query, bindVars );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( query );
		ResultSet rset = null;
		try {
			bindVariables( pstmt, bindVars );
			rset = pstmt.executeQuery();

			ResultSetMetaData meta = rset.getMetaData();
			for( int rownum = 1; rset.next(); rownum++ ) {
				if( maxRows > 0 && rownum > maxRows ) break;

				Map<String, Object> recordMap = getRecordMap( null, rset, meta, handler.getTimeZone() );
				if( (writingOption & OPT_WRITING_ROWNUMBER) > 0 ) out.print( rownum );
				if( (writingOption & OPT_WRITING_EXECUTETYPE) > 0 ) out.print( "U" );
				for( int f = 0; f < formats.length; f++ ) {
					if( formats[f] == null )
						out.printNull();
					else
						out.print( formats[f].format(recordMap, msghandler) );
				}
				out.println();
			}
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static void write( SQLHandler handler, DataWriter out, String[] fieldKeys, QueryBuffer querybuf ) throws IOException, SQLException {
		write( handler, out, fieldKeys, querybuf.getQuery(), querybuf.getBindVariables(), 0, -1 );
	}

	public static void write( SQLHandler handler, DataWriter out, String[] fieldKeys, QueryBuffer querybuf, int writingOption )
			throws IOException, SQLException {
		write( handler, out, fieldKeys, querybuf.getQuery(), querybuf.getBindVariables(), writingOption, -1 );
	}

	public static void write( SQLHandler handler, DataWriter out, String[] fieldKeys, QueryBuffer querybuf, int writingOption, int maxRows )
			throws IOException, SQLException {
		write( handler, out, fieldKeys, querybuf.getQuery(), querybuf.getBindVariables(), writingOption, maxRows );
	}

	public static void write( SQLHandler handler, DataWriter out, String[] fieldKeys, String query, Object[] bindVars )
			throws IOException, SQLException {
		write( handler, out, fieldKeys, query, bindVars, 0, -1 );
	}

	public static void write( SQLHandler handler, DataWriter out, String[] fieldKeys, String query, Object[] bindVars, int writingOption )
			throws IOException, SQLException {
		write( handler, out, fieldKeys, query, bindVars, writingOption, -1 );
	}

	/**
	 *
	 */
	public static void write( SQLHandler handler, DataWriter out, String[] fieldKeys, String query, Object[] bindVars, int writingOption
			, int maxRows ) throws IOException, SQLException {
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

		if( logger.isDebugEnabled() ) QueryUtility.printQuery( logger, "write", query, bindVars );

		PreparedStatement pstmt = handler.getConnection().prepareStatement( query );
		ResultSet rset = null;
		try {
			bindVariables( pstmt, bindVars );
			rset = pstmt.executeQuery();

			ResultSetMetaData meta = rset.getMetaData();
			for( int rownum = 1; rset.next(); rownum++ ) {
				if( maxRows > 0 && rownum > maxRows ) break;

				Map<String, Object> recordMap = getRecordMap( null, rset, meta, handler.getTimeZone() );
				if( (writingOption & OPT_WRITING_ROWNUMBER) > 0 ) out.print( rownum );
				if( (writingOption & OPT_WRITING_EXECUTETYPE) > 0 ) out.print( "U" );
				for( int f = 0; f < fieldKeys.length; f++ ) {
					if( fieldKeys[f] == null )
						out.printNull();
					else
						out.print( recordMap.get(fieldKeys[f]) );
				}
				out.println();
			}
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static void writeTitle( SQLHandler handler, DataWriter out, QueryableField[] fields, int writingOption ) throws IOException {
		char dataType = out.getDataType();
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

		out.setDataType( DataWriter.TITLE );
		if( (writingOption & QueryableManager.OPT_WRITING_ROWNUMBER) > 0 ) out.print( "No" );
		if( (writingOption & QueryableManager.OPT_WRITING_EXECUTETYPE) > 0 ) out.print( msghandler.getMessage("FIELD_EXECUTETYPE") );
		for( int f = 0; f < fields.length; f++ ) {
			if( fields[f] == null )
				out.printNull();
			else
				out.print( msghandler.getMessage(fields[f].getDescriptionKey()) );
		}
		out.println();
		out.setDataType( dataType );
	}

	public static void writeTitle( SQLHandler handler, DataWriter out, ColumnList columnList, int writingOption ) throws IOException {
		char dataType = out.getDataType();
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

		out.setDataType( DataWriter.TITLE );
		switch( (writingOption & (QueryableManager.OPT_WRITING_ROWNUMBER | QueryableManager.OPT_WRITING_EXECUTETYPE)) ) {
		case (QueryableManager.OPT_WRITING_ROWNUMBER | QueryableManager.OPT_WRITING_EXECUTETYPE):
			ColumnUtility.writeTitle( out, columnList, msghandler, "No", msghandler.getMessage("FIELD_EXECUTETYPE") );
		break;
		case QueryableManager.OPT_WRITING_ROWNUMBER:
			ColumnUtility.writeTitle( out, columnList, msghandler, "No" );
			break;
		case QueryableManager.OPT_WRITING_EXECUTETYPE:
			ColumnUtility.writeTitle( out, columnList, msghandler, msghandler.getMessage("FIELD_EXECUTETYPE") );
			break;
		default:
			ColumnUtility.writeTitle( out, columnList, msghandler );
		}
		out.setDataType( dataType );
	}
}
