/*
 *	File Name:	ScenarioRunner.java
 *	Version:	2.2.11
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	soma		2015/05/31		2.2.11	executeKPI(): pkCPFRItem_wrk, pkCPFRKPI_wrk 삭제
 *	soma		2015/04/30		2.2.10	executeSFC(), executeOFC(): Store별 Thread 처리 추가
 *	stghr12		2011/06/30		2.2.9	InterruptedException 처리
 *	yjcha		2011/02/28		2.2.8	void execute() throws ProcessException -> boolean execute()
 *										executeKPI(): CPFR_SCENARIO_DLOG 기간 조회 조건 executingKPICheckingDays 처리
 *	stghr12		2010/12/31		2.2.7	execute()의 QUERY 속도이슈로 수정
 *										executeKPI(): 누락된 조건 추가
 *										pkCPFRItem.fMakeDailyItem() 호출 후 commit() 실행
 *	stghr12		2010/09/30		2.2.6	executeKPI(): NullPointerException 오류수정
 *	stghr12		2010/07/31		2.2.5	CPFR_KPI_ENV 사용
 *	stghr12		2010/05/31		2.2.4	static executeKPI(), reexecutingKPI 추가
 *	stghr12		2010/03/31		2.2.3	executeASPD() 추가
 *										CPFR_SCENARIO_OPT -> CPFR_BASIS
 *	stghr12		2009/06/30		2.2.2	executeKPI(): 실행여부를 선택하는 로직변경
 *										executingKPITime* 환경 추가
 *	stghr12		2008/05/31		2.2.1	executeKPI(): pkCPFRItem.fApplyToItemTP() 자동으로 되지 않는 버그 수정. 연속적으로 에러가 발생하면 실행중단
 *										executeSFC(), executeOFC(): error 처리방법 변경
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.data.DataException;
import com.irt.sql.QueryBuffer;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.system.SystemConfig;
import com.irt.util.MessageHandler;
import java.sql.*;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 */
public class ScenarioRunner extends ProcessRunner {
	private static boolean reexecutingKPI;

	SystemConfig systemConfig;
	Map<String, ScenarioRunner> scenarioMap;
	String commandOFC, commandOFCStore, commandSFC, commandSFCStore;
	boolean executingKPI, usingOFCThread, usingSFCThread;
	int consistentErrorCountOfKPI, maxOFCThreadCount, maxSFCThreadCount;
	long scenarioSleepMillis;
	int executingKPICheckingDays = 1;
	int executingKPITimeAfterINVOnly = 15, executingKPITimeAfterMSTOnly = 15, executingKPITimeAfterRecv = 5, executingKPITimeBuffer = 3;

	String scenarioCode, scenarioName, retailName, sellerName;

	public ScenarioRunner( SystemConfig systemConfig ) {
		this.systemConfig = systemConfig;
		this.scenarioMap = java.util.Collections.synchronizedMap( new java.util.HashMap<String, ScenarioRunner>() );
		this.maxOFCThreadCount = 20;
		this.maxSFCThreadCount = 20;
		this.executingKPI = true;
		this.scenarioSleepMillis = -1;
		this.usingOFCThread = false;
		this.usingSFCThread = false;
	}

	private ScenarioRunner( ScenarioRunner runner ) {
		super( runner );
		this.systemConfig = runner.systemConfig;
		this.scenarioMap = runner.scenarioMap;
		this.usingOFCThread = runner.usingOFCThread;
		this.usingSFCThread = runner.usingSFCThread;
		this.commandOFC = runner.commandOFC;
		this.commandOFCStore = runner.commandOFCStore;
		this.commandSFC = runner.commandSFC;
		this.commandSFCStore = runner.commandSFCStore;
		if( runner.maxOFCThreadCount > 0 ) this.maxOFCThreadCount = runner.maxOFCThreadCount;
		if( runner.maxSFCThreadCount > 0 ) this.maxSFCThreadCount = runner.maxSFCThreadCount;
		if( runner.scenarioSleepMillis > 0 ) this.sleepMillis = runner.scenarioSleepMillis;
	}

	public void close() {
		if( scenarioCode == null ) {
			Logger logger = getLogger();

			for( Thread thread: scenarioMap.values() ) thread.interrupt();
			for( Thread thread: scenarioMap.values() ) {
				try {
					thread.join();
				} catch( InterruptedException interruptEx ) {}
			}
		}
	}

	public boolean execute() {
		Logger logger = getLogger();

		SQLHandler handler = null;
		try {
			MessageHandler msghandler = systemConfig.getMessageHandler();
			handler = systemConfig.createSQLHandler( msghandler, DEFAULT_SQLHANDLER_CREATING_TRIALCOUNT, DEFAULT_SQLHANDLER_WAIT_MILLIS );
			if( handler == null )
				throw new ProcessException( msghandler.getMessage("ERR_CANNOT_GET_SQLHANDLER") );

			if( scenarioCode == null )
				execute( handler );
			else
				execute( handler, scenarioCode );

			return true;
		} catch( ProcessException processEx ) {
			logger.error( getDescription() +" error.", processEx.getCause() != null ? processEx.getCause() : processEx );
			return false;
		} catch( SQLException sqlEx ) {
			logger.error( getDescription() +" error.", sqlEx );
			return false;
		} finally {
			try { handler.rollback(); } catch( Exception ignored ) {}
			try { handler.close(); } catch( Exception ignored ) {}
		}
	}

	private void execute( SQLHandler handler ) throws ProcessException, SQLException {
		Logger logger = getLogger();

		if( SQLManager.getInt(handler, "SELECT COUNT(*) FROM CPFR_SCENARIO") > scenarioMap.size() ) {
			PreparedStatement pstmt = handler.getConnection().prepareStatement(
				"SELECT SNR.SCENARIO_CD, SNR.SCENARIO_NAME, PBY.COMPANY_NAME, PSL.COMPANY_NAME"
					+" FROM CPFR_SCENARIO SNR, ECS_PARTY PBY, ECS_PARTY PSL"
					+" WHERE PBY.GLN(+) = SNR.BUYERGLN AND PSL.GLN(+) = SNR.SELLERGLN"
			);
			ResultSet rset = null;
			try {
				rset = pstmt.executeQuery();
				while( rset.next() ) {
					if( isInterrupted() ) break;
					if( scenarioMap.containsKey(rset.getString(1)) ) continue;

					ScenarioRunner runner = new ScenarioRunner( this );
					runner.scenarioCode = rset.getString(1);
					runner.scenarioName = rset.getString(2);
					runner.retailName = rset.getString(3);
					runner.sellerName = rset.getString(4);
					logger.debug( "["+ runner.scenarioCode +"] "+ runner.sellerName +" / "+ runner.retailName +" created." );

					scenarioMap.put( runner.scenarioCode, runner );
					runner.start();
				}
			} finally {
				try { rset.close(); } catch( Exception ignored ) {}
				try { pstmt.close(); } catch( Exception ignored ) {}
			}
		}

		if( isInterrupted() ) return;
		if( executingKPI && !reexecutingKPI )
			executeKPI( handler );
	}

	private void execute( SQLHandler handler, String scenarioCode ) throws ProcessException, SQLException {
		PreparedStatement pstmt = handler.getConnection().prepareStatement(
			"SELECT /*+ NO_MERGE(SNR) */ (CASE WHEN ASPD_NEXTSCHDATETIME < SYSDATE THEN 'A' "
						+" WHEN SFC_NEXTSCHDATETIME < SYSDATE THEN 'S' "
						+" WHEN OFC_NEXTSCHDATETIME < SYSDATE THEN 'O' ELSE 'X' END)"
					+", (SELECT COUNT(*) FROM vwCPFR_SFC_ITEM SFI WHERE SFI.SCENARIOCD = SNR.SCENARIO_CD "
							+" AND SFI.STARTDATE <= CURRDATE AND SFI.NEXTSCHDATETIME < SYSDATE )"
					+", (SELECT COUNT(*) FROM vwCPFR_OFC_ITEM OFI WHERE OFI.SCENARIOCD = SNR.SCENARIO_CD "
							+" AND OFI.STARTDATE <= CURRDATE AND OFI.NEXTSCHDATETIME < SYSDATE )"
				+" FROM "
					+"(SELECT SCENARIO_CD, TRUNC(pkSYSDate.fCurrentDate(TIMEZONE)) CURRDATE"
							+", SFC_NEXTSCHDATETIME, OFC_NEXTSCHDATETIME, ASPD_NEXTSCHDATETIME"
						+" FROM CPFR_SCENARIO) SNR"
				+" WHERE SCENARIO_CD = ?"
		);
		ResultSet rset = null;
		try {
			pstmt.setString( 1, scenarioCode );
			rset = pstmt.executeQuery();
			if( rset.next() ) {
				if( "A".equals(rset.getString(1)) ) {
					executeASPD( handler );
				} else if( "S".equals(rset.getString(1)) ) {
					executeSFC( handler, true );
					executeProcess( handler );
				} else if( "O".equals(rset.getString(1)) ) {
					executeProcess( handler );
					executeOFC( handler, true );
				} else {
					if( rset.getInt(2) > 0 ) executeSFC( handler, false );
					if( isInterrupted() ) return;
					if( rset.getInt(3) > 0 ) executeOFC( handler, false );
					if( isInterrupted() ) return;
					executeProcess( handler );
				}
			} else {
				scenarioMap.remove( scenarioCode );
				interrupt();
			}
		} finally {
			try { rset.close(); } catch( Exception ignored ) {}
			try { pstmt.close(); } catch( Exception ignored ) {}
		}
	}

	public void executeASPD( SQLHandler handler ) throws ProcessException {
		Logger logger = getLogger();

		try {
			logger.info( getDescription() +" ASPD calculating start." );

			int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRASPD.fCalculateASPD(?); END;", scenarioCode );
			handler.commit();

			logger.info( getDescription() +" "+ count +" items ASPD calculating completed." );
		} catch( DataException dataEx ) {
			throw new ProcessException( dataEx );
		} catch( SQLException sqlEx ) {
			throw new ProcessException( sqlEx );
		}
	}

	public int executeKPI( SQLHandler handler ) throws ProcessException {
		Logger logger = getLogger();

		List<Map<String, Object>> recordList = null;
		try {
			QueryBuffer querybuf = new QueryBuffer();
			querybuf.appendData( "SNR.BUYERGLN \"buyerGln\", SNR.SELLERGLN \"sellerGln\", SNDL.LOGDATE \"logDate\"" );
			querybuf.appendData( "SNR.SCENARIO_CD \"scenarioCode\"" );
			querybuf.appendData( "DECODE( SNDL.LOGDATE, TRUNC(pkSYSDate.fCurrentDate(SNR.TIMEZONE)), CBS.APPLY_MASTER_IND ) \"applyingMasterInd\"" );
			querybuf.appendData( "PBY.COMPANY_NAME \"buyerName\", PSL.COMPANY_NAME \"sellerName\"" );
			querybuf.appendData(
				"(CASE WHEN "
					+" SNDL.MST_EXECDATETIME IS NULL"
					+" OR SNDL.MST_EXECDATETIME < SNDL.MST_RECVDATETIME + (? / 1440)"
					+" OR SNDL.MST_EXECDATETIME < SNDL.INV_RECVDATETIME + (? / 1440)"
				+" THEN 'Y' ELSE 'N' END) \"executeMST\"" );
			querybuf.appendData(
				"(CASE WHEN "
					+" SNDL.KPI_EXECDATETIME IS NULL"
					+" OR SNDL.KPI_EXECDATETIME < SNDL.MST_EXECDATETIME"
					+" OR SNDL.KPI_EXECDATETIME < SNDL.INV_RECVDATETIME + (? / 1440)"
				+" THEN 'Y' ELSE 'N' END) \"executeKPI\"" );
			querybuf.addBindVariable( QueryBuffer.DATA_BINDVAR, new Double(executingKPITimeBuffer) );
			querybuf.addBindVariable( QueryBuffer.DATA_BINDVAR, new Double(executingKPITimeBuffer) );
			querybuf.addBindVariable( QueryBuffer.DATA_BINDVAR, new Double(executingKPITimeBuffer) );
			querybuf.appendTableWithAlias( "CPFR_SCENARIO", "SNR" );
			querybuf.appendTableWithAlias( "CPFR_SCENARIO_DLOG", "SNDL", "SNDL.SCENARIOCD = SNR.SCENARIO_CD" );
			querybuf.appendTableWithAlias( "CPFR_BASIS", "CBS", "CBS.BUYERGLN = SNR.BUYERGLN AND CBS.SELLERGLN = SNR.SELLERGLN" );
			querybuf.appendTableWithAlias( "ECS_PARTY", "PBY", "PBY.GLN(+) = SNR.BUYERGLN" );
			querybuf.appendTableWithAlias( "ECS_PARTY", "PSL", "PSL.GLN(+) = SNR.SELLERGLN" );
			querybuf.appendCondition( "SNDL.LOGDATE >= TRUNC(pkSYSDate.fCurrentDate(SNR.TIMEZONE) - ?)", new Integer(executingKPICheckingDays) );
			querybuf.appendCondition(
					"GREATEST("
						+" NVL(SNDL.MST_RECVDATETIME, SNDL.INV_RECVDATETIME + (? / 1440))"
						+", NVL(SNDL.INV_RECVDATETIME, SNDL.MST_RECVDATETIME + (? / 1440))"
					+") < SYSDATE - (? / 1440)"
					, new Double(executingKPITimeAfterINVOnly - executingKPITimeAfterRecv)
					, new Double(executingKPITimeAfterMSTOnly - executingKPITimeAfterRecv)
					, new Double(executingKPITimeAfterRecv)
			);
			querybuf.appendOrderBy( "1, 2, 3" );

			recordList = SQLManager.getRecordList( handler, querybuf );
			if( recordList == null ) return 0;
		} catch( SQLException sqlEx ) {
			throw new ProcessException( sqlEx );
		}

		Map<String, Object> scenarioMap = null;
		List<Map<String, Object>> scenarioList = new java.util.ArrayList<Map<String, Object>>();
		for( Map<String, Object> recordMap : recordList ) {
			if( !"Y".equals(recordMap.get("executeMST")) && !"Y".equals(recordMap.get("executeKPI")) ) continue;

			if( scenarioMap == null
					|| !scenarioMap.get("buyerGln").equals(recordMap.get("buyerGln"))
					|| !scenarioMap.get("sellerGln").equals(recordMap.get("sellerGln"))
					|| !scenarioMap.get("logDate").equals(recordMap.get("logDate")) ) {
				scenarioList.add( scenarioMap = recordMap );
			}

			String scenarioCode = (String)recordMap.remove( "scenarioCode" );
			if( recordMap.get("applyingMasterInd") != null ) {
				String applyingScenario = scenarioCode +"="+ recordMap.get( "applyingMasterInd" );
				if( scenarioMap.containsKey("applyingScenario") )
					applyingScenario = scenarioMap.get("applyingScenario") +","+ applyingScenario;
				scenarioMap.put( "applyingScenario", applyingScenario );
			}
		}

		for( Map<String, Object> recordMap : scenarioList ) {
			String buyerGln = (String)recordMap.get( "buyerGln" );
			String sellerGln = (String)recordMap.get( "sellerGln" );
			String description = "["+ sellerGln +" / "+ buyerGln +"] "+ recordMap.get("sellerName") +" / "+ recordMap.get("buyerName");
			com.irt.data.Date logDate = (com.irt.data.Date)recordMap.get( "logDate" );

			try {
				int count;
				Object[] bindVars;

				if( "Y".equals(recordMap.get("executeMST")) ) {
					logger.info( description +" MST start." );

					bindVars = new Object[] { logDate, buyerGln, sellerGln };
					count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRItem.fMakeDailyItem( ?, ?, ? ); END;", bindVars );
					logger.info( description +" "+ count +" daily items created." );
					handler.commit();
					if( isInterrupted() ) break;

					String applyingScenario = (String)recordMap.get( "applyingScenario" );
					if( applyingScenario != null ) {
						count = 0;
						String[] applyingScenarios = applyingScenario.split( "," );
						for( int i = 0; i < applyingScenarios.length; i++ ) {
							Object[] values = applyingScenarios[i].split( "=" );
							count += SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRItem.fApplyToItemTP( ?, ? ); END;", values );
						}
						logger.info( description +" "+ count +" items updated." );
					}
					handler.commit();

					logger.info( description +" MST completed." );
				}
				if( isInterrupted() ) break;

				logger.info( description +" KPI start." );

				bindVars = new Object[] { logDate.getDate(-1), buyerGln, sellerGln };
				count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRKPI.fMakeKPIData( ?, ?, ? ); END;", bindVars );
				logger.info( description +" "+ count +" KPI data created." );
				handler.commit();

				consistentErrorCountOfKPI = 0;
				logger.info( description +" KPI completed." );

				if( isInterrupted() ) break;
			} catch( DataException dataEx ) {
				consistentErrorCountOfKPI++;
				logger.error( description +" error.", dataEx );
			} catch( SQLException sqlEx ) {
				consistentErrorCountOfKPI++;
				logger.error( description +" error.", sqlEx );
			} finally {
				try { handler.rollback(); } catch( Exception ignored ) {}
			}
		}
		if( maxConsistentErrorCount > 0 && consistentErrorCountOfKPI >= maxConsistentErrorCount ) {
			executingKPI = false;
			logger.error( getDescription() +" executing KPI suspended("+ consistentErrorCountOfKPI +" consistent error)." );
		}

		return recordList.size();
	}

	public synchronized static void executeKPI( SQLHandler handler, Logger logger, String description, int executingDays )
						throws DataException, SQLException {
		String minDateValue = null, maxDateValue = null;
		com.irt.data.Date minDate = null, maxDate = null;
		List<Object[]> bindVarsList = new java.util.ArrayList<Object[]>();

		// minDate, minDateValue, maxDate, maxDateValue
		try {
			minDateValue = (String)SQLManager.getObjectValue( handler,
					"SELECT MIN(DATEVALUE) FROM CPFR_KPI_MNG WHERE TUNIT = '2' AND DATEVALUE >= TO_CHAR(TRUNC(SYSDATE - ?), 'YYYY-MM-DD')"
			, new Integer(executingDays) );
			if( minDate == null ) return;
			minDate = com.irt.data.Date.getInstance( minDateValue );

			maxDateValue = (String)SQLManager.getObjectValue( handler,
				"SELECT MAX(DATEVALUE) FROM CPFR_KPI_MNG WHERE TUNIT = '2' AND DATEVALUE >= TO_CHAR(TRUNC(SYSDATE - ?), 'YYYY-MM-DD')"
			, new Integer(executingDays) );
			maxDate = com.irt.data.Date.getInstance( maxDateValue );
		} catch( java.text.ParseException parseEx ) {}

		// bindVarsList
		{
			QueryBuffer querybuf = new QueryBuffer();

			querybuf.append( "MNG.TUNIT, MNG.DATEVALUE, MNG.BUYERGLN, MNG.SELLERGLN" );
			querybuf.appendTableWithAlias( "CPFR_KPI_MNG", "MNG" );
			querybuf.appendCondition( "MNG.TUNIT = '2'" );
			querybuf.appendCondition( "MNG.DATEVALUE BETWEEN ? AND ?", minDateValue, maxDateValue );
			querybuf.unionAll();
			querybuf.appendDistinct();
			querybuf.append( "MNG.TUNIT, MNG.DATEVALUE, MNG.BUYERGLN, MNG.SELLERGLN" );
			querybuf.appendTableWithAlias( "CPFR_KPI_MNG", "MNG_D" );
			querybuf.appendTableWithAlias( "CPFR_KPI_MNG", "MNG" );
			querybuf.appendTableWithAlias( "CPFR_KPI_ENV", "ENV" );
			querybuf.appendCondition( "MNG_D.TUNIT = '2'" );
			querybuf.appendCondition( "MNG_D.DATEVALUE BETWEEN ? AND ?", minDateValue, maxDateValue );
			querybuf.appendCondition( "MNG.TUNIT = '3'" );
			querybuf.appendCondition( "MNG.DATEVALUE = pkSYSDateValue.fGetDateValue(TO_DATE(MNG_D.DATEVALUE, 'YYYY-MM-DD'), '3', NULL, ENV.FIRSTDAY_OF_WEEK)" );
			querybuf.appendCondition( "MNG.BUYERGLN = MNG_D.BUYERGLN AND MNG.SELLERGLN = MNG_D.SELLERGLN" );
			querybuf.unionAll();
			querybuf.appendDistinct();
			querybuf.append( "MNG.TUNIT, MNG.DATEVALUE, MNG.BUYERGLN, MNG.SELLERGLN" );
			querybuf.appendTableWithAlias( "CPFR_KPI_MNG", "MNG_D" );
			querybuf.appendTableWithAlias( "CPFR_KPI_MNG", "MNG" );
			querybuf.appendTableWithAlias( "CPFR_BASIS", "CBS", "CBS.BUYERGLN = MNG_D.BUYERGLN AND CBS.SELLERGLN = MNG_D.SELLERGLN" );
			querybuf.appendCondition( "MNG_D.TUNIT = '2'" );
			querybuf.appendCondition( "MNG_D.DATEVALUE BETWEEN ? AND ?", minDateValue, maxDateValue );
			querybuf.appendCondition( "MNG.TUNIT = '4'" );
			querybuf.appendCondition( "MNG.DATEVALUE = pkSYSDateValue.fGetDateValue(TO_DATE(MNG_D.DATEVALUE,'YYYY-MM-DD'), '4', CBS.CALENDAR_TYPE)" );
			querybuf.appendCondition( "MNG.BUYERGLN = MNG_D.BUYERGLN AND MNG.SELLERGLN = MNG_D.SELLERGLN" );
			querybuf.unionAll();
			querybuf.appendDistinct();
			querybuf.append( "MNG.TUNIT, MNG.DATEVALUE, MNG.BUYERGLN, MNG.SELLERGLN" );
			querybuf.appendTableWithAlias( "CPFR_KPI_MNG", "MNG_D" );
			querybuf.appendTableWithAlias( "CPFR_KPI_MNG", "MNG" );
			querybuf.appendTableWithAlias( "CPFR_BASIS", "CBS", "CBS.BUYERGLN = MNG_D.BUYERGLN AND CBS.SELLERGLN = MNG_D.SELLERGLN" );
			querybuf.appendCondition( "MNG_D.TUNIT = '2'" );
			querybuf.appendCondition( "MNG_D.DATEVALUE BETWEEN ? AND ?", minDateValue, maxDateValue );
			querybuf.appendCondition( "MNG.TUNIT = '5'" );
			querybuf.appendCondition( "MNG.DATEVALUE = pkSYSDateValue.fGetDateValue(TO_DATE(MNG_D.DATEVALUE,'YYYY-MM-DD'), '5', CBS.CALENDAR_TYPE)" );
			querybuf.appendCondition( "MNG.BUYERGLN = MNG_D.BUYERGLN AND MNG.SELLERGLN = MNG_D.SELLERGLN" );
			querybuf.appendOrderBy( "1, 2, 3" );

			SQLManager.saveQuery( handler, querybuf );

			PreparedStatement pstmt = null;
			ResultSet rset = null;
			try {
				pstmt = handler.getConnection().prepareStatement( querybuf.getQuery() );
				SQLManager.bindVariables( pstmt, querybuf.getBindVariables() );

				rset = pstmt.executeQuery();
				while( rset.next() ) {
					Object[] bindVars = new Object[] { rset.getString(1), rset.getString(2), rset.getString(3), rset.getString(4) };
					bindVarsList.add( bindVars );
				}
			} finally {
				try { rset.close(); } catch( Exception ignored ) {}
				try { pstmt.close(); } catch( Exception ignored ) {}
			}
		}

		reexecutingKPI = true;
		try {
			executeKPI( handler, logger, description, minDate, maxDate, bindVarsList );
		} finally {
			reexecutingKPI = false;
		}
	}

	private static void executeKPI( SQLHandler handler, Logger logger, String description, com.irt.data.Date minDate, com.irt.data.Date maxDate
						, List<Object[]> bindVarsList ) throws DataException, SQLException {
		int count;
		Object[] bindVars_prev;

		logger.info( description +" KPI re-executing start." );

		count = SQLManager.callStatementInt( handler, "BEGIN ? := pkOSSMaster.fUpdateUnknownEventStatus(?, ?); END;", minDate, maxDate );
		logger.info( description +" "+ count +" records Unknown Promotion Status updated." );
		handler.commit();

		bindVars_prev = null;
		for( Object[] bindVars : bindVarsList ) {
			if( !"2".equals(bindVars[0]) ) break;
			if( bindVars_prev == null || !bindVars[1].equals(bindVars_prev[1]) ) {
				if( bindVars_prev != null ) {
					logger.info( description +" "+ count +" daily items updated("+ bindVars_prev[1] +")." );
					handler.commit();
				}
				count = 0;
				bindVars_prev = bindVars;
			}

			count += SQLManager.callStatementInt( handler,
				"BEGIN ? := pkCPFRItem.fUpdateDailyItemDataType(TO_DATE(?, 'YYYY-MM-DD'), ?, ?); END;"
			, bindVars[1], bindVars[2], bindVars[3] );
		}
		if( bindVars_prev != null ) {
			logger.info( description +" "+ count +" daily items updated("+ bindVars_prev[1] +")." );
			handler.commit();
		}

		bindVars_prev = null;
		for( Object[] bindVars : bindVarsList ) {
			if( bindVars_prev == null || !bindVars[0].equals(bindVars_prev[0]) || !bindVars[1].equals(bindVars_prev[1]) ) {
				if( bindVars_prev != null ) {
					logger.info( description +" "+ count +" KPI data updated("+ bindVars_prev[0] +" - "+ bindVars_prev[1] +")." );
					handler.commit();
				}
				count = 0;
				bindVars_prev = bindVars;
			}

			count += SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRKPI.fMakeKPIData(?, ?, ?, ?); END;", bindVars );
		}
		if( bindVars_prev != null ) {
			logger.info( description +" "+ count +" KPI data updated("+ bindVars_prev[0] +" - "+ bindVars_prev[1] +")." );
			handler.commit();
		}

		logger.info( description +" KPI re-executing completed." );
	}

	public void executeOFC(SQLHandler handler, boolean executeScenario) throws ProcessException, SQLException {
		Logger logger = getLogger();
		final Map<String, Thread> OFCProcessMap = java.util.Collections.synchronizedMap( new java.util.HashMap<String, Thread>(100) );

		logger.info( getDescription() +" OFC start." );

		if( !usingOFCThread ) {
			if( commandOFC == null ) throw new ProcessException( "cannot find OFC command." );
	
			try {
				java.lang.Process process = Runtime.getRuntime().exec( commandOFC +" "+ scenarioCode );
				int returnValue = process.waitFor();
	
				if( returnValue == 0 )
					logger.info( getDescription() +" OFC executed." );
				else {
					logger.info( getDescription() +" OFC error(return = "+ returnValue +")." );
					throw new ProcessException( "process '"+ commandOFC +" "+ scenarioCode +"' returns "+ returnValue +"." );
				}
			} catch( Exception ex ) {
				throw new ProcessException( ex );
			}
		} else {
			String conditionSQL;

			if(executeScenario){
				conditionSQL = "WHERE SCENARIOCD = ?";
				logger.debug( getDescription() + ": " + "scenario exec");
			} else {
				conditionSQL = "WHERE SCENARIOCD = ? AND NEXTSCHDATETIME < SYSDATE";
				logger.debug( getDescription() + ": " + "Store exec");
			}

			if( commandOFCStore == null ) throw new ProcessException( "cannot find OFC Store command." );

			PreparedStatement pstmt = handler.getConnection().prepareStatement(
					"SELECT DISTINCT BUYERGLN FROM vwCPFR_OFC_ITEM " + conditionSQL
				);

			ResultSet rset = null;
			try {
				pstmt.setString( 1, scenarioCode );
				rset = pstmt.executeQuery();

				while( rset.next() ) {

					if( !OFCProcessMap.containsKey(rset.getString(1)) ) {
						while( maxOFCThreadCount > 0 && OFCProcessMap.size() >= maxOFCThreadCount )
							sleep( DEFAULT_THREAD_WAIT_MILLIS );

						OFCProcessMap.put( rset.getString(1), null );

						final String buyergln = rset.getString(1);

						Thread thread = new Thread() {
							public void run() {

								try {
									Logger logger = getLogger();

									java.lang.Process process = Runtime.getRuntime().exec( commandOFCStore + " "+ scenarioCode + " " + buyergln);
									int returnValue = process.waitFor();

									if( returnValue == 0 ){
										logger.info( getDescription() + ": " + buyergln + " - Store OFC executed." );
									} else {
										logger.info( getDescription() +" OFC error(return = "+ returnValue +")." );
										throw new ProcessException( "process '"+ commandOFCStore +" "+ scenarioCode +"' returns "+ returnValue +"." );
									}
								} catch( Exception ex ) {
								} finally {
									OFCProcessMap.remove( buyergln );
								}
							}
						};

						OFCProcessMap.put( buyergln, thread );
						thread.start();
					}
				}

				if( executeScenario ) {
					while( OFCProcessMap.size() > 0 )
						sleep( DEFAULT_THREAD_WAIT_MILLIS );

					java.util.Date ofcLastDate = (java.util.Date)SQLManager.getObjectValue( handler
							, "SELECT TRUNC(SYSDATE) + NVL(OFC_TERMS,1) -1 FROM CPFR_SCENARIO WHERE SCENARIO_CD= ?", scenarioCode);

					int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRScenario.fUpdateOFCLastDate(?, NULL, ? ); END;", scenarioCode, ofcLastDate );

					handler.commit();
					logger.info( getDescription()  +" OFC update next schdatetime." );
				}
				logger.info( getDescription() +" OFC executed." );

				} catch (DataException dataEx) {
					throw new ProcessException( dataEx );
				} catch( InterruptedException interruptEx ) {
					throw new ProcessException( interruptEx );
				} finally {
					try { rset.close(); } catch( Exception ignored ) {}
					try { pstmt.close(); } catch( Exception ignored ) {}
				}
			}
		}

	public void executeProcess( SQLHandler handler ) throws ProcessException {
		Logger logger = getLogger();

		CallableStatement cstmt = null;
		try {
			cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkCPFRScenario.fUpdateScenarioProcess( ? ); END;" );
			cstmt.registerOutParameter( 1, Types.VARCHAR );
			cstmt.setString( 2, scenarioCode );

			String executeStatus = null;
			do {
				cstmt.executeUpdate();
				handler.commit();

				executeStatus = cstmt.getString(1);
				if( "SFCFR".equals(executeStatus) )
					logger.info( getDescription() +" SFC frozing executed." );
				else if( "OFCMD".equals(executeStatus) )
					logger.info( getDescription() +" OFC modify closing executed." );
				else if( "OFCFR".equals(executeStatus) )
					logger.info( getDescription() +" OFC frozing executed." );
				else if( "PPO".equals(executeStatus) )
					logger.info( getDescription() +" PPO sending executed." );
				else if( "PO".equals(executeStatus) )
					logger.info( getDescription() +" PO sending executed." );
			} while( executeStatus != null );
		} catch( SQLException sqlEx ) {
			throw new ProcessException( sqlEx );
		} finally {
			try { cstmt.close(); } catch( Exception ex ) {}
		}
	}

	public void executeSFC( SQLHandler handler, boolean executeScenario ) throws ProcessException, SQLException {
		Logger logger = getLogger();
		final Map<String, Thread> SFCProcessMap = java.util.Collections.synchronizedMap( new java.util.HashMap<String, Thread>(100) );

		logger.info( getDescription() +" SFC start." );

		if( !usingSFCThread ) {
			if( commandSFC == null ) throw new ProcessException( "cannot find SFC command." );

			try {
				java.lang.Process process = Runtime.getRuntime().exec( commandSFC +" "+ scenarioCode );
				int returnValue = process.waitFor();

				if( returnValue == 0 ){
					logger.info( getDescription() +" SFC executed." );
				} else {
					logger.info( getDescription() +" SFC error(return = "+ returnValue +")." );
					throw new ProcessException( "process '"+ commandSFC +" "+ scenarioCode +"' returns "+ returnValue +"." );
				}
			} catch( Exception ex ) {
				throw new ProcessException( ex );
			}
		} else {
			String conditionSQL;

			if(executeScenario){
				conditionSQL = "WHERE SCENARIOCD = ?";
				logger.debug( getDescription() + ": " + "scenario exec");
			} else {
				conditionSQL = "WHERE SCENARIOCD = ? AND NEXTSCHDATETIME < SYSDATE";
				logger.debug( getDescription() + ": " + "Store exec");
			}

			if( commandSFCStore == null ) throw new ProcessException( "cannot find SFC Store command." );

			PreparedStatement pstmt = handler.getConnection().prepareStatement(
					"SELECT DISTINCT BUYERGLN FROM vwCPFR_SFC_ITEM " + conditionSQL
				);

			ResultSet rset = null;
			try {
				pstmt.setString( 1, scenarioCode );
				rset = pstmt.executeQuery();

				while( rset.next() ) {

					if( !SFCProcessMap.containsKey(rset.getString(1)) ) {
						while( maxSFCThreadCount > 0 && SFCProcessMap.size() >= maxSFCThreadCount )
							sleep( DEFAULT_THREAD_WAIT_MILLIS );

						SFCProcessMap.put( rset.getString(1), null );

						final String buyergln = rset.getString(1);

						Thread thread = new Thread() {
							public void run() {

								try {
									Logger logger = getLogger();

									java.lang.Process process = Runtime.getRuntime().exec( commandSFCStore + " "+ scenarioCode + " " + buyergln);
									int returnValue = process.waitFor();

									if( returnValue == 0 ){
										logger.info( getDescription() + ": " + buyergln + " - SFC executed (Store)." );
									} else {
										logger.info( getDescription() +" SFC error(return = "+ returnValue +")." );
										throw new ProcessException( "process '"+ commandSFCStore +" "+ scenarioCode +"' returns "+ returnValue +"." );
									}
								} catch( Exception ex ) {
								} finally {
									SFCProcessMap.remove( buyergln );
								}
							}
						};

						SFCProcessMap.put( buyergln, thread );
						thread.start();
					}
				}

				if( executeScenario ) {
					while( SFCProcessMap.size() > 0 )
						sleep( DEFAULT_THREAD_WAIT_MILLIS );

					java.util.Date sfcLastDate = (java.util.Date)SQLManager.getObjectValue( handler
							, "SELECT TRUNC(SYSDATE) + ( NVL(SFC_TERMS,0) * 7 ) + MOD( 8 - TO_CHAR(SYSDATE,'D'), 7) -1  FROM CPFR_SCENARIO WHERE SCENARIO_CD= ?", scenarioCode);

					int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRScenario.fUpdateSFCLastDate(?, NULL, ? ); END;", scenarioCode, sfcLastDate );

					handler.commit();
					logger.info( getDescription()  +" SFC update next schdatetime." );
				}
				logger.info( getDescription() +" SFC executed." );

			} catch (DataException dataEx) {
				throw new ProcessException( dataEx );
			} catch( InterruptedException interruptEx ) {
				throw new ProcessException( interruptEx );
			} finally {
				try { rset.close(); } catch( Exception ignored ) {}
				try { pstmt.close(); } catch( Exception ignored ) {}
			}
		}
	}

	public String getDescription() {
		if( scenarioCode == null )
			return "ScenarioRunner";
		else
			return "["+ scenarioCode +"] "+ sellerName +" / "+ retailName;
	}

	public Logger getLogger() {
		return Logger.getLogger( scenarioCode == null ? "com.irt.rbm.tools.ScenarioRunner" : "com.irt.rbm.tools.ScenarioRunner."+ scenarioCode );
	}

	public String getProcessName() {
		return "ScenarioRunner";
	}

	public void setCommandOFC( String commandOFC ) {
		this.commandOFC = commandOFC;
	}
	
	public void setCommandOFCStore( String commandOFCStore ) {
		this.commandOFCStore = commandOFCStore;
	}

	public void setCommandSFC( String commandSFC ) {
		this.commandSFC = commandSFC;
	}

	public void setCommandSFCStore( String commandSFCStore ) {
		this.commandSFCStore = commandSFCStore;
	}

	public void setExecutingKPI( boolean executingKPI ) {
		this.executingKPI = executingKPI;
	}

	public void setUsingOFCThread( boolean usingOFCThread ) {
		this.usingOFCThread = usingOFCThread;
	}

	public void setUsingSFCThread( boolean usingSFCThread ) {
		this.usingSFCThread = usingSFCThread;
	}

	public void setExecutingKPICheckingDays( int executingKPICheckingDays ) {
		this.executingKPICheckingDays = executingKPICheckingDays;
	}

	public void setExecutingKPITimeAfterINVOnly( int executingKPITimeAfterINVOnly ) {
		this.executingKPITimeAfterINVOnly = executingKPITimeAfterINVOnly;
	}

	public void setExecutingKPITimeAfterMSTOnly( int executingKPITimeAfterMSTOnly ) {
		this.executingKPITimeAfterMSTOnly = executingKPITimeAfterMSTOnly;
	}

	public void setExecutingKPITimeAfterRecv( int executingKPITimeAfterRecv ) {
		this.executingKPITimeAfterRecv = executingKPITimeAfterRecv;
	}

	public void setExecutingKPITimeBuffer( int executingKPITimeBuffer ) {
		this.executingKPITimeBuffer = executingKPITimeBuffer;
	}

	public void setScenarioSleepMillis( long scenarioSleepMillis ) {
		this.scenarioSleepMillis = scenarioSleepMillis;
	}

	public void setMaxOFCThreadCount( int maxOFCThreadCount ) {
		this.maxOFCThreadCount = maxOFCThreadCount;
	}

	public void setMaxSFCThreadCount( int maxSFCThreadCount ) {
		this.maxSFCThreadCount = maxSFCThreadCount;
	}
}
