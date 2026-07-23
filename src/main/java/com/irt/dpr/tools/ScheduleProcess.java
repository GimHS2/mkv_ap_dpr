/*
 *	File Name:	ScheduleProcess.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.3	DPR_SCHEDULE 스케쥴 패턴 변수 설정.
 *	jbaek		2015/06/30		2.2.2	void execute() throws ProcessException -> boolean execute()
 *	stghr12		2008/06/20		2.2.1	사용하지 않는 scheduleCode 정리
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.data.DataException;
import com.irt.sql.SQLHandler;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 */
public class ScheduleProcess implements com.irt.rbm.tools.Process {
	public final static String DPR_SCHEDULE					= "DPR_";
	public final static String DPR_MSTINF					= "DPR_MSTINF";

	boolean executed;
	SQLHandler handler, central_handler;
	Logger logger;
	String scheduleCode, scheduleName, extraValue;

	public ScheduleProcess( SQLHandler handler, SQLHandler central_handler, String scheduleCode, String scheduleName, String extraValue ) {
		this.executed = false;
		this.handler = handler;
		this.central_handler = central_handler;
		this.scheduleCode = scheduleCode;
		this.scheduleName = scheduleName;
		this.extraValue = extraValue;
		this.logger = Logger.getLogger( "com.irt.dpr.tools.ScheduleProcess."+ scheduleCode );

		if( DPR_MSTINF.equals( scheduleCode ) ) return;

		throw new IllegalArgumentException( "illegal scheduleCode '"+ scheduleCode +"'" );
	}

	@Override
	public void close() {
		if( central_handler != null )
			central_handler.close();
	}

	@Override
	public boolean continueProcessing() {
		return executed;
	}

	@Override
	public boolean execute() {
		try {
			executed = true;
			executeSchedule();
			return executed;
		} catch( DataException dataEx ) {
			logger.error( getDescription() +" error.", dataEx );
			return false;
		} catch( SQLException sqlEx ) {
			logger.error( getDescription() +" error.", sqlEx );
			return false;
		}
	}

	protected void executeSchedule() throws DataException, SQLException {
		logger.info( getDescription() +" started." );

		try {
			if( DPR_MSTINF.equals(scheduleCode )) {
				com.irt.dpr.tools.MasterInterface.masterInterface( handler, central_handler, logger );
			}

		} finally {
			try {
				handler.rollback();
				central_handler.rollback();
			} catch( Exception ignored ) {}
		}
	}

	@Override
	public String getDescription() {
		return "["+ scheduleCode +"]"+ scheduleName;
	}

	@Override
	public String getProcessName() {
		return "ScheduleProcess."+ scheduleCode;
	}
}
