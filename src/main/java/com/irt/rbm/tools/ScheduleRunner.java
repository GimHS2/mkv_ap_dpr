/*
 *	File Name:	ScheduleRunner.java
 *	Version:	2.2.4c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/05/31		2.2.4c	DPR_MSTINF 스케쥴 처리안되도록 수정
 *	stghr12		2011/06/30		2.2.4	InterruptedException 처리
 *	stghr12		2011/03/31		2.2.3	getInstance()에서 throws ProcessException 제거
 *	stghr12		2011/02/28		2.2.2	void execute() -> boolean execute() throws ProcessException
 *	stghr12		2010/08/31		2.2.1	ScheduleProcessHandler 지원
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.sql.SQLHandler;
import com.irt.system.SystemConfig;
import com.irt.util.MessageHandler;
import java.sql.*;
import org.apache.log4j.Logger;

/**
 *
 */
public class ScheduleRunner extends ProcessRunner implements ScheduleProcessHandler {
	SystemConfig systemConfig;
	ScheduleProcessHandler processHandler;

	public ScheduleRunner( SystemConfig systemConfig ) {
		this.systemConfig = systemConfig;
		this.processHandler = this;
	}

	@Override
	public boolean execute() {
		Logger logger = getLogger();
		MessageHandler msghandler = systemConfig.getMessageHandler();

		PreparedStatement pstmt = null;
		ResultSet rset = null;

		SQLHandler handler = null;
		SQLHandler handler_thr = null;
		try {
			handler = systemConfig.createSQLHandler( msghandler, DEFAULT_SQLHANDLER_CREATING_TRIALCOUNT, DEFAULT_SQLHANDLER_WAIT_MILLIS );
			if( handler == null )
				throw new ProcessException( msghandler.getMessage("ERR_CANNOT_GET_SQLHANDLER") );

			pstmt = handler.getConnection().prepareStatement(
				"SELECT SCH_CD, SCH_NAME, SCH_CLASS, EXTRAVALUE FROM RBM_SCHEDULE WHERE NEXTSCHDATETIME < SYSDATE ORDER BY NEXTSCHDATETIME"
			);
			rset = pstmt.executeQuery();
			while( rset.next() ) {
				String scheduleCode = rset.getString(1).trim();
				String scheduleName = rset.getString(2);
				String scheduleClass = rset.getString(3);
				String extraValue = rset.getString(4);

				if( executing("Schedule."+ scheduleCode) ) continue;

				if( usingThread && handler_thr == null ) {
					handler_thr = systemConfig.createSQLHandler( msghandler, DEFAULT_SQLHANDLER_CREATING_TRIALCOUNT, DEFAULT_SQLHANDLER_WAIT_MILLIS );
					if( handler_thr == null )
						throw new ProcessException( msghandler.getMessage("ERR_CANNOT_GET_SQLHANDLER") );
				}

				Process process = processHandler.getInstance( handler_thr == null ? handler : handler_thr, scheduleCode, scheduleName, extraValue );
				if( process == null ) {
					updateSchedule( handler, scheduleCode );
					if( "PUB".equals(scheduleClass) )
						logger.info( "["+ scheduleCode +"] "+ scheduleName +" update next schdatetime." );
				} else if( process != ProcessRunner.EMPTY ) {
					final String final_scheduleCode = scheduleCode;
					final boolean final_closingSQLHandler = ( handler_thr != null );
					final SQLHandler final_handler = ( handler_thr == null ? handler : handler_thr );

					process = new ProcessWrapper( process, "Schedule."+ scheduleCode, "["+ scheduleCode +"]"+ scheduleName ) {
						String scheduleCode = final_scheduleCode;
						boolean closingSQLHandler = final_closingSQLHandler;
						SQLHandler handler = final_handler;

						@Override
						public void close() {
							if( closingSQLHandler ) handler.close();
							super.close();
						}

						@Override
						public boolean execute() throws InterruptedException {
							try {
								return super.execute();
							} finally {
								if( super.continueProcessing() ) {
									try {
										updateSchedule( handler, scheduleCode );
										getLogger().info( process.getDescription() +" update next schdatetime." );
									} catch( SQLException sqlEx ) {
										getLogger().error( process.getDescription() +" error.", sqlEx );
									}
								}
							}
						}
					};
					handler_thr = null;

					if( !execute(process) ) return false;
				}
				if( isInterrupted() ) break;
			}

			return true;
		} catch( ProcessException processEx ) {
			logger.error( getDescription() +" error.", processEx.getCause() != null ? processEx.getCause() : processEx );
			return false;
		} catch( SQLException sqlEx ) {
			logger.error( getDescription() +" error.", sqlEx );
			return false;
		} finally {
			try { rset.close(); } catch( Exception ignored ) {}
			try { pstmt.close(); } catch( Exception ignored ) {}
			try { handler.close(); } catch( Exception ignored ) {}
			try { if( handler_thr != null ) handler_thr.close(); } catch( Exception ignored ) {}
		}
	}

	@Override
	public String getDescription() {
		return "ScheduleRunner";
	}

	@Override
	public Process getInstance( SQLHandler handler, String scheduleCode, String scheduleName, String extraValue ) {
		if( scheduleCode.equals(com.irt.dpr.tools.ScheduleProcess.DPR_MSTINF) ) {
			return ProcessRunner.EMPTY;
		}

		try {
			return new ScheduleProcess( handler, scheduleCode, scheduleName, extraValue );
		} catch( IllegalArgumentException argEx ) {
			return null;
		}
	}

	@Override
	public Logger getLogger() {
		return Logger.getLogger( "com.irt.rbm.tools.ScheduleRunner" );
	}

	public ScheduleProcessHandler setProcessHandler() {
		return this.processHandler;
	}

	@Override
	public String getProcessName() {
		return "ScheduleRunner";
	}

	public void setProcessHandler( String processHandler ) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		this.processHandler = (ScheduleProcessHandler)ScheduleRunner.class.getClassLoader().loadClass(processHandler).newInstance();
	}

	public void updateSchedule( SQLHandler handler, String scheduleCode ) throws SQLException {
		CallableStatement cstmt = null;
		try {
			cstmt = handler.getConnection().prepareCall( "call pkRBMSchedule.pUpdateNextSchDateTime( ? )" );
			cstmt.setString( 1, scheduleCode );
			cstmt.executeUpdate();

			handler.commit();
		} finally {
			try { cstmt.close(); } catch( Exception ignored ) {}
			try { handler.rollback(); } catch( Exception ignored ) {}
		}
	}
}
