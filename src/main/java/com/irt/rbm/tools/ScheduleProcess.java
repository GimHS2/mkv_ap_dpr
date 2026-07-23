/*
 *	File Name:	ScheduleProcess.java
 *	Version:	2.2.9
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	soma		2015/04/30		2.2.9	CPFR_SFC_GROUP_UPDATE, CPFR_OFC_GROUP_UPDATE 추가
 *	GimHS		2011/12/30		2.2.8	PDS_ANAL_LOG_DELETE 추가
 *	stghr12		2011/02/28		2.2.7	void execute() throws ProcessException -> boolean execute()
 *	stghr12		2010/05/31		2.2.6	CUSTOM_SCHEDULE 동작방식 변경
 *										RBM_REMOVE_DELETEDBFILE 삭제
 *										CPFR_EVENT_UPDATE_RESULT 추가
 *										CPFR_KPI_REEXECUTE 추가
 *	stghr12		2010/03/31		2.2.5	OSS_DATA_DELETE, CPFR_KPI_DATA_DELETE 삭제
 *	stghr12		2009/10/31		2.2.4	Partition Schedule 코드 수정
 *	stghr12		2009/06/30		2.2.3	Partition Schedule 수정
 *	stghr12		2008/12/31		2.2.2	Custom Schedule Code 변경
 *	stghr12		2008/06/20		2.2.1	사용하지 않는 scheduleCode 정리
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.data.DataException;
import com.irt.rbm.RBMSystem;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 */
public class ScheduleProcess implements Process {
	public final static String CUSTOM_SCHEDULE					= "CUSTOM_";

	public final static String SYS_MAKE_PARTITION				= "SYS_MAK_PT";
	public final static String USR_USER_EXPIRE_PASSWORD			= "USR_PASSWD";
	public final static String USR_SESSION_HIST_DELETE			= "USR_SS_DEL";

	public final static String ECS_PARTY_OPERDAY_UPDATE			= "ECS_OPER_U";
	public final static String ECS_PARTY_OPERDAY_DELETE			= "ECS_OPER_D";
	public final static String ECS_ITEM_FILE_UPDATE				= "ECS_ITM_FL";
	public final static String ECS_ITEM_PASTDATA_DELETE			= "ECS_ITM_PS";
	public final static String ECS_ITEMTP_TRADEDATE_APPLY		= "ECS_ITP_DT";

	public final static String OSS_UNSENT_ORDERSTATUS_UPDATE	= "OSS_ORDS_U";

	public final static String CPFR_SCENARIO_LOG_DELETE			= "CPFR_SNR_L";
	public final static String CPFR_EVENT_UPDATE_STATUS			= "CPFR_EVT_U";
	public final static String CPFR_EVENT_UPDATE_RESULT			= "CPFR_EVT_R";
	public final static String CPFR_SFC_RLTD_DELETE				= "CPFR_SFC_D";
	public final static String CPFR_OFC_RLTD_DELETE				= "CPFR_OFC_D";
	public final static String CPFR_SFC_GROUP_UPDATE			= "CPFR_SFG_U";
	public final static String CPFR_OFC_GROUP_UPDATE			= "CPFR_OFG_U";
	public final static String CPFR_KPI_REEXECUTE				= "CPFR_KPI_E";

	public final static String PDS_ANAL_LOG_DELETE				= "PDS_ALOG_D";

	boolean executed;
	SQLHandler handler;
	Logger logger;
	String scheduleCode, scheduleName, extraValue;

	public ScheduleProcess( SQLHandler handler, String scheduleCode, String scheduleName, String extraValue ) {
		this.executed = false;
		this.handler = handler;
		this.scheduleCode = scheduleCode;
		this.scheduleName = scheduleName;
		this.extraValue = extraValue;
		this.logger = Logger.getLogger( "com.irt.rbm.tools.ScheduleProcess."+ scheduleCode );

		if(		SYS_MAKE_PARTITION.equals(scheduleCode)
			 || USR_USER_EXPIRE_PASSWORD.equals(scheduleCode)
			 || USR_SESSION_HIST_DELETE.equals(scheduleCode)
			 || scheduleCode != null && scheduleCode.startsWith(CUSTOM_SCHEDULE) ) return;

		if( RBMSystem.usingSystem("ECS") ) {
			if(		ECS_PARTY_OPERDAY_UPDATE.equals(scheduleCode)
				 || ECS_PARTY_OPERDAY_DELETE.equals(scheduleCode)
				 || ECS_ITEM_FILE_UPDATE.equals(scheduleCode)
				 || ECS_ITEM_PASTDATA_DELETE.equals(scheduleCode)
				 || ECS_ITEMTP_TRADEDATE_APPLY.equals(scheduleCode) ) return;
		}
		if( RBMSystem.usingSystem("OSS") ) {
			if(		OSS_UNSENT_ORDERSTATUS_UPDATE.equals(scheduleCode) ) return;
		}
		if( RBMSystem.usingSystem("CPFR") ) {
			if(		CPFR_SCENARIO_LOG_DELETE.equals(scheduleCode)
				 || CPFR_EVENT_UPDATE_STATUS.equals(scheduleCode)
				 || CPFR_EVENT_UPDATE_RESULT.equals(scheduleCode)
				 || CPFR_SFC_RLTD_DELETE.equals(scheduleCode)
				 || CPFR_OFC_RLTD_DELETE.equals(scheduleCode)
				 || CPFR_SFC_GROUP_UPDATE.equals(scheduleCode)
				 || CPFR_OFC_GROUP_UPDATE.equals(scheduleCode)
				 || CPFR_KPI_REEXECUTE.equals(scheduleCode) ) return;
		}
		if( RBMSystem.usingSystem("PDS") ) {
			if(		PDS_ANAL_LOG_DELETE.equals(scheduleCode) ) return;
		}

		throw new IllegalArgumentException( "illegal scheduleCode '"+ scheduleCode +"'." );
	}

	public void close() {}

	public boolean continueProcessing() {
		return executed;
	}

	public boolean execute() {
		try {
			executed = true;
			executeSchedule();

			return true;
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
			/********************************************************************************
			 *	RBM
			********************************************************************************/
			if( SYS_MAKE_PARTITION.equals(scheduleCode) ) {
				SQLManager.callStatement( handler, "call pkSYSStandard.pMakePartition()" );
				handler.commit();
				logger.info( getDescription() +" completed." );
			} else if( USR_USER_EXPIRE_PASSWORD.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkUSRData.fExpirePassword(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" user-passwords expired." );
			} else if( USR_SESSION_HIST_DELETE.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkUSRSession.fDeleteSessionHist(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" user-session-history deleted." );

				count = SQLManager.callStatementInt( handler, "BEGIN ? := pkUSRSession.fDeleteSessionAccessLog(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" user-session-accesslog deleted." );

				count = SQLManager.callStatementInt( handler, "BEGIN ? := pkUSRSession.fDeleteSessionFailLog(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" user-session-faillog deleted." );
			} else if( scheduleCode.startsWith(CUSTOM_SCHEDULE) ) {
				SQLManager.callStatement( handler, "call pkCustom.pSchedule(?)", scheduleCode.substring(CUSTOM_SCHEDULE.length()) );
				handler.commit();
				logger.info( getDescription() +" completed." );
			}


			/********************************************************************************
			 *	ECS
			********************************************************************************/
			if( ECS_PARTY_OPERDAY_UPDATE.equals(scheduleCode) ) {
				SQLManager.callStatement( handler, "call pkECSPartyOper.pUpdateLastOperday()" );
				handler.commit();
				logger.info( getDescription() +" completed." );
			} else if( ECS_PARTY_OPERDAY_DELETE.equals(scheduleCode) ) {
				int count;

				count = SQLManager.callStatementInt( handler, "BEGIN ? := pkECSPartyOper.fDeleteOldOperday(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" operdays deleted." );

				count = SQLManager.callStatementInt( handler, "BEGIN ? := pkECSPartyOper.fDeleteOldDeliveryCalDtl(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" delivery-schedules deleted." );

				count = SQLManager.callStatementInt( handler, "BEGIN ? := pkECSPartyOper.fDeleteUnusedDeliveryCal(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" unused delivery-calendars deleted." );
			} else if( ECS_ITEM_PASTDATA_DELETE.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkECSItemManage.fDeletePastItem(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" items deleted." );
			} else if( ECS_ITEM_FILE_UPDATE.equals(scheduleCode) ) {
				// WORKING
			} else if( ECS_ITEMTP_TRADEDATE_APPLY.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkECSTradeItem.fApplyTradeDate(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" trade-items updated." );
			}


			/********************************************************************************
			 *	OSS
			********************************************************************************/
			if( OSS_UNSENT_ORDERSTATUS_UPDATE.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkOSSOrder.fUpdateUnSentOrderStatus(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" un-sent order status updated." );
			}


			/********************************************************************************
			 *	CPFR
			********************************************************************************/
			if( CPFR_SCENARIO_LOG_DELETE.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRScenario.fDeleteScenarioLog(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" records deleted." );
			} else if( CPFR_EVENT_UPDATE_STATUS.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFREvent.fUpdateStatus(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" event-status updated." );
			} else if( CPFR_EVENT_UPDATE_RESULT.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFREvent.fUpdateResult(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" event-result updated." );
			} else if( CPFR_SFC_RLTD_DELETE.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRSFC.fDeleteOldResult(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" records deleted." );
			} else if( CPFR_OFC_RLTD_DELETE.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFROFC.fDeleteOldResult(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" records deleted." );
			} else if( CPFR_SFC_GROUP_UPDATE.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRAutoProcess.fUpdateSFCGroup(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" records changed." );
			} else if( CPFR_OFC_GROUP_UPDATE.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkCPFRAutoProcess.fUpdateOFCGroup(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" records changed." );
			} else if( CPFR_KPI_REEXECUTE.equals(scheduleCode) ) {
				logger.info( getDescription() +" KPI re-executing start." );
				try {
					ScenarioRunner.executeKPI( handler, logger, getDescription(), Integer.parseInt(extraValue) );
				} catch( NumberFormatException numberEx ) {
					ScenarioRunner.executeKPI( handler, logger, getDescription(), 7 );
				}
				handler.commit();
				logger.info( getDescription() +" KPI re-executing completed." );
			}


			/********************************************************************************
			 *	PDS
			********************************************************************************/
			if( PDS_ANAL_LOG_DELETE.equals(scheduleCode) ) {
				int count = SQLManager.callStatementInt( handler, "BEGIN ? := pkPDSAnal.fDeleteAccesslog(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" PDS analysis accesslog deleted." );

				count = SQLManager.callStatementInt( handler, "BEGIN ? := pkPDSAnal.fDeleteExecuteQueue(); END;" );
				handler.commit();
				logger.info( getDescription() +" "+ count +" PDS analysis executequeue deleted." );
			}
		} finally {
			try { handler.rollback(); } catch( Exception ignored ) {}
		}
	}

	public String getDescription() {
		return "["+ scheduleCode +"]"+ scheduleName;
	}

	public String getProcessName() {
		return "ScheduleProcess."+ scheduleCode;
	}
}
