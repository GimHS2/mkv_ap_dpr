/*
 *	File Name:	DataLoader.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/10/30		2.2.2	close(): S3 에 error 파일 업로드 추가
 *	stghr12		2008/05/31		2.2.1	testing 추가
 *										DataLoader.Loader: complete(), start() 추가
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.data;

import com.irt.sql.SQLHandler;
import com.irt.util.S3Service;

import java.io.*;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class DataLoader extends Thread {
	private static Map<String, DataLoader> loaderMap = java.util.Collections.synchronizedMap( new java.util.HashMap<String, DataLoader>() );

	public final static String CANCEL					= "CL";
	public final static String COMPLETED				= "CP";
	public final static String ERROR					= "ER";
	public final static String RUNNING					= "RU";
	public final static String WARNING					= "WN";

	SQLHandler handler;
	DataLoader.Loader loader;
	DataLoader.Logger loaderLogger;
	DataLoader.Validator validator;
	String loaderLogId;

	DataReader reader;
	File errorFile;
	PrintStream errorStream;

	long limitMillis;
	boolean commitByLine;
	boolean testing;

	DataResult result;
	String resultStatus;

	public DataLoader( SQLHandler handler, DataLoader.Loader loader, DataLoader.Logger loaderLogger, DataLoader.Validator validator
						, DataReader reader, boolean commitByLine ) {
		this( handler, loader, loaderLogger, validator, reader, commitByLine, (PrintStream)null );
	}

	public DataLoader( SQLHandler handler, DataLoader.Loader loader, DataLoader.Logger loaderLogger, DataLoader.Validator validator
						, DataReader reader, boolean commitByLine, File errorFile ) throws FileNotFoundException {
		this( handler, loader, loaderLogger, validator, reader, commitByLine, new PrintStream(errorFile) );
		this.errorFile = errorFile;
	}

	public DataLoader( SQLHandler handler, DataLoader.Loader loader, DataLoader.Logger loaderLogger, DataLoader.Validator validator
						, DataReader reader, boolean commitByLine, File errorFile, String encoding )
						throws FileNotFoundException, UnsupportedEncodingException {
		this( handler, loader, loaderLogger, validator, reader, commitByLine, new PrintStream(errorFile, encoding) );
		this.errorFile = errorFile;
	}

	public DataLoader( SQLHandler handler, DataLoader.Loader loader, DataLoader.Logger loaderLogger, DataLoader.Validator validator
						, DataReader reader, boolean commitByLine, PrintStream errorStream ) {
		this.handler = handler;
		this.loader = loader;
		this.loaderLogger = loaderLogger;
		this.loaderLogId = loaderLogger.getLogId();
		this.validator = validator;

		this.reader = reader;
		this.commitByLine = commitByLine;
		this.errorFile = null;
		this.errorStream = errorStream;
		this.limitMillis = -1;
	}

	public void close( boolean closingHandler ) {
		try { loader.close(); } catch( Exception ignored ) {}
		try { if( closingHandler ) handler.close(); } catch( Exception ignored ) {}
		try { reader.close(); } catch( Exception ignored ) {}
		try { if( validator != null ) validator.close(); } catch( Exception ignored ) {}

		if( errorStream != null ) {
			try { errorStream.close(); } catch( Exception ignored ) {}
			if( errorFile != null && (result == null || result.getErrorCount() == 0) )
				try { errorFile.delete(); } catch( Exception ignored ) {}
			else if( S3Service.s3Instance != null ) {
				S3Service.s3Instance.upload( errorFile );
				try { errorFile.delete(); } catch( Exception ignored ) {}
			}
		}
	}

	public void execute() throws IOException, SQLException {
		this.result = new DataResult();
		this.resultStatus = RUNNING;

		String errorMessage = null;
		try {
			loaderLogger.startLog();
			loader.start( handler );
			if( commitByLine ) handler.commit();
			do {
				Map<String, Object> recordMap = null, resultMap = null;
				try {
					recordMap = loader.readLine( handler, reader );
					if( recordMap == null ) continue;

					recordMap = loader.processLine( handler, recordMap );
					if( validator != null )
						validator.validateLine( handler, recordMap );

					resultMap = loader.loadLine( handler, recordMap );
					if( COMPLETED.equals(resultMap.get("status")) ) {
						Object executeType = resultMap.get( "executeType" );

						if( "A".equals(executeType) )
							result.increaseRegistCount();
						else if( "U".equals(executeType) )
							result.increaseModifyCount();
						else if( "D".equals(executeType) )
							result.increaseDeleteCount();
						else
							result.increaseModifyCount();
					} else
						result.increaseCount();

					resultMap.put( "lineNumber", new Integer(reader.getLineNumber()) );
					resultMap.put( "lineString", reader.getLineString() );

					if( testing )
						handler.rollback();
					else if( commitByLine )
						handler.commit();
				} catch( DataException dataEx ) {
					resultMap = new java.util.TreeMap<String, Object>();
					resultMap.put( "lineNumber", new Integer(reader.getLineNumber()) );
					resultMap.put( "lineString", reader.getLineString() );
					resultMap.put( "executeType", loader.getExecuteType() );
					resultMap.put( "status", ERROR );
					resultMap.put( "message", dataEx.getMessage() );

					result.appendError( reader.getLineNumber(), dataEx );
					if( this.errorStream != null ) this.errorStream.println( reader.getLineString() );
				} finally {
					if( commitByLine ) {
						handler.rollback();
						if( resultMap != null ) loaderLogger.writeLog( recordMap, resultMap );
						handler.commit();
					} else if( resultMap != null )
						loaderLogger.pushLog( recordMap, resultMap );
				}

				if( isInterrupted() ) {
					this.resultStatus = CANCEL;
					return;
				} else if( limitMillis > 0 && limitMillis < System.currentTimeMillis() - loaderLogger.getStartTimeMillis() ) {
					this.resultStatus = CANCEL;
					return;
				}
			} while( !reader.isEOF() );

			loader.complete( handler );

			this.resultStatus = COMPLETED;
			if( !testing && result.getErrorCount() == 0 ) handler.commit();
		} catch( IOException ioEx ) {
			this.resultStatus = ERROR;
			errorMessage = ioEx.getMessage();
			throw ioEx;
		} catch( RuntimeException runtimeEx ) {
			this.resultStatus = ERROR;
			errorMessage = runtimeEx.getMessage();
			throw runtimeEx;
		} catch( SQLException sqlEx ) {
			this.resultStatus = ERROR;
			errorMessage = sqlEx.getMessage();
			throw sqlEx;
		} finally {
			handler.rollback();
			loaderLogger.completeLog( result, resultStatus, errorMessage );
			handler.commit();
		}
	}

	public DataReader getDataReader() {
		return reader;
	}

	public DataResult getDataResult() {
		return result;
	}

	public PrintStream getErrorPrintStream() {
		return errorStream;
	}

	public String getLoaderLogId() {
		return loaderLogId;
	}

	public String getResultStatus() {
		return resultStatus;
	}

	public com.irt.data.Timestamp getStartTimestamp() {
		return loaderLogger.getStartTimestamp();
	}

	@Override
	public void run() {
		try {
			loaderMap.put( loaderLogId, this );
			try {
				execute();
			} finally {
				loaderMap.remove( loaderLogId );
			}
		} catch( IOException ioEx ) {
		} catch( SQLException sqlEx ) {
		} finally {
			close( true );
		}
	}

	public void setTesting( boolean testing ) {
		this.testing = testing;
	}

	public void setTimeLimit( long limitMillis ) {
		this.limitMillis = limitMillis;
	}

	/**
	 *
	 */
	public interface Loader {
		public void close();

		public String getExecuteType();

		public void complete( SQLHandler handler ) throws SQLException;

		public Map<String, Object> loadLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException;

		public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException;

		public Map<String, Object> readLine( SQLHandler handler, DataReader reader ) throws DataException, IOException;

		public boolean setLoaderOption( String key, Object value ) throws IllegalArgumentException;

		public void start( SQLHandler handler ) throws SQLException;
	}

	/**
	 *
	 */
	public interface Logger {
		public void completeLog( DataResult result, String status, String errorMessage );

		public long getElapsedMilliTime();

		public String getLogId();

		public Map<String, Object> getResultMap();

		public long getStartTimeMillis();

		public com.irt.data.Timestamp getStartTimestamp();

		public void pushLog( Map<String, Object> recordMap, Map<String, Object> resultMap );

		public void startLog();

		public void writeLog( Map<String, Object> recordMap, Map<String, Object> resultMap );
	}

	/**
	 *
	 */
	public interface Validator {
		public void close();

		public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException;
	}
}
