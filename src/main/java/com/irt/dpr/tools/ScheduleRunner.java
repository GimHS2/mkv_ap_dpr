/*
 *	File Name:	ScheduleRunner.java
 *	Version:	2.2.6
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/12/30		2.2.6	getInstance(): APMAS_LOGGING 테이블의 Interface 시간과 실행시간 메일 전송 추가
 *	jbaek		2020/06/30		2.2.5	getInstance(): DPR Schedule만 실행. whileDays 변수 적용.
 *	jbaek		2018/09/30		2.2.4	getDescription(), getProgramName() 추가
 *	jbaek		2015/06/30		2.2.3	systemConfigTo사용하는 Constructor 추가
 *	lsinji		2010/05/28		2.2.2	Central DB의 사용자 명 제거
 *	lsinji		2009/08/21		2.2.1	CentralInterface만 체크 하도록 변경
 *	keehe		2008/01/02		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.rbm.tools.*;
import com.irt.rbm.tools.Process;
import com.irt.sql.SQLHandler;
import com.irt.system.SystemConfig;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 *
 */
public class ScheduleRunner extends com.irt.rbm.tools.ScheduleRunner {

	SystemConfig systemConfigTo;//DPR_MSTINF 의 Source systemConfig
	Map<String, Object> sendMailMap;
	ScheduleProcessHandler processHandler;

	public ScheduleRunner( SystemConfig systemConfig, SystemConfig systemConfigTo ) {
		super( systemConfig );
		this.systemConfigTo = systemConfigTo;
		this.processHandler = this;
		sendMailMap = new java.util.HashMap<String, Object>();
	}

	@Override
	public Process getInstance( SQLHandler handler, String scheduleCode, String scheduleName, String extraValue ) {
		if( !scheduleCode.startsWith(com.irt.dpr.tools.ScheduleProcess.DPR_SCHEDULE) ) {
			return ProcessRunner.EMPTY;
		}

		Logger logger = getLogger();

		SQLHandler central_handler = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;

		try {
			central_handler = DPRTools.central_systemConfig.createSQLHandler( DPRTools.central_systemConfig.getMessageHandler() );

			if( central_handler == null ) {
				logger.debug( "Not Connected to Central DB" );
			}

			Integer whileDays = 1;
			try {
				if( extraValue != null )
					whileDays = Integer.parseInt(extraValue);
			} catch( NumberFormatException ignored ) {}

			Map<String, Object> mailMap = new java.util.HashMap<String, Object>( sendMailMap );
			boolean centralInterface = false;
			try {
				pstmt = central_handler.getConnection().prepareStatement(
					//"SELECT COUNT(*) CNT, MAX(INTERFACE_DATE) LAST_INF_DATE FROM APMAS_LOGGING WHERE TRUNC(INTERFACE_DATE) >= TRUNC(SYSDATE - ?) AND INTERFACE_TYPE = 'SAP' AND INOUT = 'I' AND FLAG = 'C'"
					"SELECT INTERFACE_DATE, EXECUTE_DATETIME FROM APMAS_LOGGING"
						+ " WHERE TRUNC(INTERFACE_DATE) >= TRUNC(LOCALTIMESTAMP - ?) AND INTERFACE_TYPE = 'SAP' AND INOUT = 'I' AND FLAG = 'C' AND ROWNUM = 1"
						+ " ORDER BY INTERFACE_DATE DESC"
				);
				pstmt.setInt(1, whileDays);
				rset = pstmt.executeQuery();

				if( rset.next() ) {
					centralInterface = true;
					java.util.Date sourceDate = rset.getTimestamp(1);
					java.util.Date execDate = rset.getTimestamp(2);
					logger.debug( "[" + scheduleCode + "] "+ scheduleName + " APMAS_LOGGING(count: 1"
						+ " cetralInterface: "+ centralInterface
						+ " whileDays: "+ whileDays
						+ " sourceDate: "+ sourceDate
						+ " sourceTimestamp: "+ (sourceDate == null ? null : sourceDate.getTime())
						+ " handlerTimeZone: "+ central_handler.getTimeZone().getID()
					+")" );

					if( mailMap.containsKey("host") ) {
						String sdfPattern = "yyyy-MM-dd HH:mm:ss";
						SimpleDateFormat sdf = new SimpleDateFormat(sdfPattern);

						String subject = (String)mailMap.get( "subject" );
						mailMap.put( "subject", subject + " Master Interface Time." );
						mailMap.put( "contents", "D-Portal Interface Execute Time : " + sdf.format(new java.util.Date())
											+ "<br>SAP INTERFACE_DATE : " + sdf.format(sourceDate) + "<br>SAP EXECUTE_DATETIME : " + sdf.format(execDate) );
					}
				} else {
					logger.debug( "[" + scheduleCode + "] "+ scheduleName + " APMAS_LOGGING(count: 0"
						+ " cetralInterface: "+ centralInterface
						+ " whileDays: "+ whileDays
						+ " handlerTimeZone: "+ central_handler.getTimeZone().getID()
					+")" );

					String subject = (String)mailMap.get( "subject" );
					mailMap.put( "subject", subject + " Master Interface Time." );
					mailMap.put( "contents", "D-Portal master interface not running." );
				}

				if( mailMap.containsKey("host") ) {
					com.irt.util.Utility.sendMail( mailMap, logger );
				}
			} finally {
				try { rset.close(); } catch( Exception ex ) {}
				try { pstmt.close(); } catch( Exception ex ) {}
			}

			if( centralInterface ) {
				Process masterInterface = new com.irt.dpr.tools.ScheduleProcess( handler, central_handler, scheduleCode, scheduleName, extraValue );
				central_handler = null;
				logger.debug( "[" + scheduleCode + "] " + scheduleName + " getInstance started." + "(masterInterface:"+(masterInterface==null?false:true)+")" );

				return masterInterface;
			} else
				return null;
		} catch( SQLException sqlEx ) {
			logger.debug( sqlEx );
		} catch( IllegalArgumentException argEx ) {
			return null;
		} finally {
			if( central_handler != null )  central_handler.close();
			logger.debug( "["+ scheduleCode +"] "+ scheduleName +" getInstance executed." );
		}
		return null;
	}

	@Override
	public Logger getLogger() {
		return Logger.getLogger( "com.irt.dpr.tools.ScheduleRunner" );
	}

	@Override
	public String getDescription() {
		return "DPRScheduleRunner";
	}

	@Override
	public String getProcessName() {
		return "DPRScheduleRunner";
	}

	public void setSmtpHost( String host ) {
		sendMailMap.put( "host", host );
	}

	public void setSmtpPassword( String password ) {
		sendMailMap.put( "pw", password );
	}

	public void setSmtpUserid( String userId ) {
		sendMailMap.put( "id", userId );
	}

	public void setSmtpFrom( String from ) {
		sendMailMap.put( "fromAddress", from );
	}

	public void setSmtpTo( String to ) {
		sendMailMap.put( "toAddress", to );
	}

	public void setSmtpSubject( String subject ) {
		sendMailMap.put( "subject", subject );
	}

	/*
	public Process getProcessInstance( SQLHandler handler, String scheduleCode, String scheduleName, String extraValue ) throws ProcessException {

		SQLHandler central_handler = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;

		try {
			central_handler = DPRTools.central_systemConfig.createSQLHandler( DPRTools.central_systemConfig.getMessageHandler() );

			if( central_handler == null )
				throw new ProcessException( "Not Connected Central DB" );

			boolean centralInterface = false;
			try {
				pstmt = central_handler.getConnection().prepareStatement(
					"SELECT COUNT(*) FROM APMAS_LOGGING WHERE TRUNC(INTERFACE_DATE) = TRUNC(SYSDATE) AND INTERFACE_TYPE = 'SAP' AND INOUT = 'I' AND FLAG = 'C'"
				);
				rset = pstmt.executeQuery();
				centralInterface = ( rset.next() && rset.getInt(1) > 0 );
			} finally {
				try { rset.close(); } catch( Exception ex ) {}
				try { pstmt.close(); } catch( Exception ex ) {}
			}

			if( centralInterface ) {
				Process masterInterface = new com.irt.dpr.tools.ScheduleProcess( handler, central_handler, scheduleCode, scheduleName, extraValue );
				central_handler = null;

				return masterInterface;
			} else
				return null;
		} catch( SQLException sqlEx ) {
			throw new ProcessException( sqlEx );
		} catch( IllegalArgumentException argEx ) {
			return null;
		} finally {
			if( central_handler != null )  central_handler.close();
		}
	}
	*/
}
