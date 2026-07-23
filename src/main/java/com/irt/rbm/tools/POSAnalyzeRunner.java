/*
 *	File Name:	POSAnalyzeRunner.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/06/30		2.2.3	InterruptedException 처리
 *	stghr12		2011/02/28		2.2.2	void execute() throws ProcessException -> boolean execute()
 *	stghr12		2009/06/30		2.2.1	DATEINDEX -> DATEVALUE
 *	stghr12		2008/05/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.data.DataException;
import com.irt.rbm.RBMSystem;
import com.irt.sql.QueryBuffer;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.system.SystemConfig;
import com.irt.util.MessageHandler;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 */
public class POSAnalyzeRunner extends ProcessRunner {
	SystemConfig systemConfig;

	int convertingCheckDays, convertingTimeBegin, convertingTimeEnd;
	long convertingNextTimeMillis;
	int analyzingTimeBegin, analyzingTimeEnd;
	String analyzingCommand;

	public POSAnalyzeRunner( SystemConfig systemConfig ) {
		this.systemConfig = systemConfig;

		this.convertingCheckDays = -1;
		this.convertingTimeBegin = -1;
		this.convertingTimeEnd = -1;
		this.convertingNextTimeMillis = -1;

		this.analyzingTimeBegin = -1;
		this.analyzingTimeEnd = -1;
		this.analyzingCommand = null;
	}

	public void close() {
	}

	public boolean execute() {
		String HHmm = (new java.text.SimpleDateFormat("HHmm")).format( new java.util.Date() );
		int currentTime = ( Integer.parseInt(HHmm.substring(0, 2)) * 60 + Integer.parseInt(HHmm.substring(2, 4)) );

		boolean executingINVConvert = (
			convertingCheckDays > 0
			&& ( convertingTimeBegin < 0 || currentTime >= convertingTimeBegin )
			&& ( convertingTimeEnd < 0 || currentTime <= convertingTimeEnd )
			&& convertingNextTimeMillis < System.currentTimeMillis()
		);

		boolean executingPOSAnalyze = (
			analyzingCommand != null
			&& ( analyzingTimeBegin < 0 || currentTime >= analyzingTimeBegin )
			&& ( analyzingTimeEnd < 0 || currentTime <= analyzingTimeEnd )
		);

		if( executingINVConvert || executingPOSAnalyze ) {
			SQLHandler handler = null;

			try {
				MessageHandler msghandler = systemConfig.getMessageHandler();
				handler = systemConfig.createSQLHandler( msghandler, DEFAULT_SQLHANDLER_CREATING_TRIALCOUNT, DEFAULT_SQLHANDLER_WAIT_MILLIS );
				if( handler == null )
					throw new ProcessException( msghandler.getMessage("ERR_CANNOT_GET_SQLHANDLER") );

				if( !isInterrupted() && executingINVConvert ) executeINVConvert( handler );
				if( !isInterrupted() && executingPOSAnalyze ) executePOSAnalyze( handler );
			} catch( ProcessException processEx ) {
				getLogger().error( getDescription() +" error.", processEx.getCause() != null ? processEx.getCause() : processEx );
				return false;
			} catch( SQLException sqlEx ) {
				getLogger().error( getDescription() +" error.", sqlEx );
				return false;
			} finally {
				try { handler.rollback(); } catch( Exception ignored ) {}
				try { handler.close(); } catch( Exception ignored ) {}
			}
		}

		return true;
	}

	private void executeINVConvert( SQLHandler handler ) throws ProcessException, SQLException {
		Logger logger = getLogger();

		String systemDateTime = (String)SQLManager.getObjectValue( handler, "SELECT TO_CHAR(SYSDATE - 1/24/12, 'YYYY-MM-DD HH24:MI:SS') FROM DUAL" );
		String previousSystemDateTime = RBMSystem.getSystemEnv( "PDS", "POSData;LastINV2POSDDateTime", systemDateTime );

		int convertCount = 0;
		PreparedStatement pstmt = null;
		CallableStatement cstmt = null;
		try {
			pstmt = handler.getConnection().prepareStatement(
				"SELECT DISTINCT INVDATE FROM OSS_INV WHERE INVDATE >= TRUNC(SYSDATE) - ? AND UPGDATE >= TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')"
					+" ORDER BY 1"
			);
			cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkPDSData.fMakePOSDFromINV( ?, NULL, pkPDSData.conAUTO ); END;" );
			cstmt.registerOutParameter( 1, Types.INTEGER );

			pstmt.setInt( 1, convertingCheckDays );
			pstmt.setString( 2, previousSystemDateTime );
			ResultSet rset = pstmt.executeQuery();
			try {
				while( rset.next() ) {
					com.irt.data.Date posDate = com.irt.data.Date.getInstance( rset.getDate(1) );

					logger.info( getDescription() +" INV to POSD["+ posDate +"] start." );
					cstmt.setDate( 2, posDate );
					cstmt.executeUpdate();
					handler.commit();
					convertCount++;
					logger.info( getDescription() +" INV to POSD["+ posDate +"] completed." );
					if( isInterrupted() ) break;
				}
			} finally {
				try { rset.close(); } catch( Exception ignored ) {}
			}
		} finally {
			try { pstmt.close(); } catch( Exception ignored ) {}
			try { cstmt.close(); } catch( Exception ignored ) {}
		}

		if( convertCount > 0 ) {
			try {
				RBMSystem.setSystemEnv( handler, "PDS", "POSData;LastINV2POSDDateTime", systemDateTime );
				handler.commit();
			} catch( DataException dataEx ) {
				throw new ProcessException( dataEx );
			}
			this.convertingNextTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000;
		}
	}

	private void executePOSAnalyze( SQLHandler handler ) throws ProcessException, SQLException {
		Logger logger = getLogger();

		if( analyzingCommand == null ) throw new ProcessException( "cannot find POSAnalyzing command." );

		List<Map<String, Object>> executeList = SQLManager.getRecordList( handler,
			"SELECT EXECID \"executeId\", TUNIT \"termUnit\", DATEVALUE \"dateValue\", EXEC_OPTION \"executeOption\""
				+" FROM PDS_EXEC_QUEUE WHERE EXEC_DATETIME < SYSDATE AND STATUS = 'RD' ORDER BY EXEC_DATETIME, EXECID"
		, (Object[])null );
		if( executeList == null ) return;

		for( Map<String, Object> executeMap : executeList ) {
			String executeId = (String)executeMap.get( "executeId" );
			String termUnit = (String)executeMap.get( "termUnit" );
			String dateValue = (String)executeMap.get( "dateValue" );
			String executeOption = (String)executeMap.get( "executeOption" );

			String description = getDescription() +" ["+ executeId +"] POSAnalyzing(unit="+ termUnit +", date="+ dateValue;
			if( executeOption.charAt(0) != '_' )
				description += ", sum="+ executeOption.charAt(0);
			if( executeOption.charAt(1) != '_' )
				description += ", anal="+ executeOption.charAt(1);
			description += ")";

			logger.info( description +" start." );
			try {
				java.lang.Process process = Runtime.getRuntime().exec( analyzingCommand +" "+ executeId );
				int returnValue = process.waitFor();

				if( returnValue == 0 )
					logger.info( description +" completed." );
				else
					logger.error( description +" completed("+ returnValue +")." );
			} catch( InterruptedException interruptEx ) {
				logger.error( description +" interrupted.", interruptEx );
			} catch( IOException ioEx ) {
				throw new ProcessException( ioEx );
			}
		}
	}

	public String getDescription() {
		return "POSAnalyzeRunner";
	}

	public Logger getLogger() {
		return Logger.getLogger( "com.irt.rbm.tools.POSAnalyzeRunner" );
	}

	public String getProcessName() {
		return "POSAnalyzeRunner";
	}

	public void setAnalyzingCommand( String analyzingCommand ) {
		this.analyzingCommand = analyzingCommand;
	}

	public void setAnalyzingTimeBegin( String HHmm ) {
		if( HHmm.charAt(2) == ':' ) HHmm = HHmm.substring(0, 2) + HHmm.substring(3, 5);
		this.analyzingTimeBegin = ( Integer.parseInt(HHmm.substring(0, 2)) * 60 + Integer.parseInt(HHmm.substring(2, 4)) );
	}

	public void setAnalyzingTimeEnd( String HHmm ) {
		if( HHmm.charAt(2) == ':' ) HHmm = HHmm.substring(0, 2) + HHmm.substring(3, 5);
		this.analyzingTimeEnd = ( Integer.parseInt(HHmm.substring(0, 2)) * 60 + Integer.parseInt(HHmm.substring(2, 4)) );
	}

	public void setConvertingCheckDays( int convertingCheckDays ) {
		this.convertingCheckDays = convertingCheckDays;
	}

	public void setConvertingTimeBegin( String HHmm ) {
		if( HHmm.charAt(2) == ':' ) HHmm = HHmm.substring(0, 2) + HHmm.substring(3, 5);
		this.convertingTimeBegin = ( Integer.parseInt(HHmm.substring(0, 2)) * 60 + Integer.parseInt(HHmm.substring(2, 4)) );
	}

	public void setConvertingTimeEnd( String HHmm ) {
		if( HHmm.charAt(2) == ':' ) HHmm = HHmm.substring(0, 2) + HHmm.substring(3, 5);
		this.convertingTimeEnd = ( Integer.parseInt(HHmm.substring(0, 2)) * 60 + Integer.parseInt(HHmm.substring(2, 4)) );
	}
}
