/*
 *	File Name:	OrderCanonicalProcess.java
 *	Version:	2.2.20(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	yjkdev21	2026/05/29		2.2.20	execute_statuslist() : orderDate 종료일 범위 수정, BTPi Request 시 정렬 상관없이 sold-to 별로 묶어 요청하도록 변경
 *	dudwls3720	2025/12/31		2.2.19	execute_simulation() : BTPi Response XML에 xmlData 태그 이스케이프 처리
 *										execute_creation() : BTPi Response XML에 ORDER_CREATE_RESPONSE 태그 이스케이프 처리, OrderProcessException 발생 시 원본 예외 메시지 전달하도록 수정
 *	dudwls3720	2025/10/31		2.2.18	execute_simulation() : BTPi response 처리 로직 수정
 *										connectBTPi() : URL 파라미터 추가 및 null 체크 로직 추가
 *	GimHS		2025/09/30		2.2.17	BTPi interface(REST) 적용
 *	dudwls3720	2024/02/29		2.2.16	getSystemInfo() : smtp 캔뷰로 변경
 *	jbaek		2023/08/27		2.2.15	execute_creation(): SAP에 발주 요청할때의 ITEMCD_ENTERED 칼럼 업데이트
 *	hankalam	2021/11/30		2.2.14	execute_statuslist(): 최근 10개 발주정보 가져오는 기능 추가
 *	jbaek		2020/06/30		2.2.13	org.slf4j.MDC(uniqId)를 log에 남기기 위해서 caller에서 remove하도록 변경.
 *	jbaek		2020/06/30		2.2.13	deleteZeroOrderQty 적용
 *	jbaek		2020/06/30		2.2.13	org.slf4j.MDC추가..
 *	jbaek		2019/06/30		2.2.12	getSystemInfo() 추가.
 *	jbaek		2019/03/30		2.2.11	BAPIXML body Path 정의. WM_PARAMS_BILLING 변수 오류 수정.
 *	jbaek		2018/03/31		2.2.10	FakeOrderCanonical 기능추가.
 *	jbaek		2017/09/30		2.2.9	execute_status(): commit 추가
 *	jbaek		2015/01/30		2.2.8	RDD Failure Email 기능: 국가별 메일 전송.
 *	jbaek		2013/05/30		2.2.7	HeaderStatus, LineItemStatus 기능 추가
 *	jbaek		2013/01/30		2.2.6	PIPO 기능 개발, LineItemStatus 기능 개발
 *	jbaek		2012/08/30		2.2.5	RDD Failure Auto Email 기능추가
 *	GimHS		2011/07/31		2.2.4	dateFormat "yyyyMMddhhmmss" -> "yyyyMMddHHmmss" 변경
 *	GimHS		2011/04/29		2.2.3	Error log 메시지 수정
 *	lsinji		2009/10/23		2.2.2	execute_simulation()에서 FieldException.ERR_CANNOT_NULL일 때 OrderProcessException 별도 에러코드 처리
 *	lsinji		2009/04/18		2.2.1	USER Parameter에 sold_to만 들어갈수 있도록 변경(employeeID 삭제)
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.data.DataException;
import com.irt.dpr.OrderProcessException;
import com.irt.dpr.util.Loggers;
import com.irt.servlet.SystemConfig;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

public class OrderCanonicalProcess {//@formatter:off
	public final static String ORDER_IF_CREATION		= "creation";
	public final static String ORDER_IF_RDD				= "rdd";
	public final static String ORDER_IF_SIMULATION		= "simulation";
	public final static String ORDER_IF_STATUSLIST		= "statuslist";
	public final static String ORDER_IF_STATUS			= "status";
	public final static String ORDER_IF_BILLING			= "billing";

	public final static String BAPIXML_BODYPATH_SIMULATION = "/biztalk_1/body/doc:SalesOrder.Simulate.Response";
	public final static String BAPIXML_BODYPATH_CREATION = "/biztalk_1/body/doc:SalesOrder.ZBapiWrapperForCreateFromDat2.Response";

	public final static String WM_PARAMS				= "RootParameter";
	public final static String WM_PARAMS_CREATION		= "OrderPlacement";
	public final static String WM_PARAMS_RDD			= "RDD";
	public final static String WM_PARAMS_SIMULATION		= "out_OrderSimulateReturn";
	public final static String WM_PARAMS_STATUSLIST		= "STATUS_RECORDS2";
	public final static String WM_PARAMS_STATUS			= "OrderStatusReturn";
	public final static String WM_PARAMS_BILLING		= "billingResponse";

	public final static String ORDERPROCESS_PROPERTIES	= "tools.conf";

	SQLHandler handler;
	private static SystemConfig systemConfig;
	Logger logger;
	OrderLogging orderLogging;

	String processType;
	String errorKey;

	Map<String, Object> systemInfo;
	Map<String, Object> parameterMap;

	public OrderCanonicalProcess( SQLHandler handler, SystemConfig systemConfig, String processType ) {
		this.handler = handler;
		OrderCanonicalProcess.systemConfig = systemConfig;
		this.processType = processType;

		this.logger = getLogger();
		this.orderLogging = new OrderLogging( this.logger, null );

		this.parameterMap = new java.util.HashMap<String, Object>();
		this.systemInfo = new java.util.HashMap<String, Object>();
		this.errorKey = null;
	}

	public OrderCanonicalProcess( SQLHandler handler, SystemConfig systemConfig, String processType, Map<String, Object> systemInfo ) {
		this.handler = handler;
		this.processType = processType;

		this.logger = getLogger();
		this.orderLogging = new OrderLogging( this.logger, systemInfo );

		this.parameterMap = new java.util.HashMap<String, Object>();
		this.systemInfo = systemInfo;
		this.errorKey = null;
	}

	public String execute() throws OrderProcessException {
		Loggers.business.debug( "{}: {} {}", new Object[]{ getParameter("orderKey"), processType ,"start." } );
		org.slf4j.MDC.put("ocp.prcType", processType);
		if( (String)getParameter("orderKey") != null )
			org.slf4j.MDC.put("ocp.refKey", (String)getParameter("orderKey"));
		if( (String)getParameter("updateUserId") != null )
			org.slf4j.MDC.put("uniqId", (String)getParameter("updateUserId"));
		try {
			if( ORDER_IF_CREATION.equals(processType) )
				execute_creation();
			else if( ORDER_IF_RDD.equals(processType) )
				return execute_rdd();
			else if( ORDER_IF_SIMULATION.equals(processType) )
				execute_simulation();
			else if( ORDER_IF_STATUSLIST.equals(processType) )
				execute_statuslist();
			else if( ORDER_IF_STATUS.equals(processType) )
				execute_status();
			else if( ORDER_IF_BILLING.equals(processType) )
				execute_billing();
			else
				logger.error( "InValid Process Type : " + processType );
		} catch( OrderProcessException proEx ) {
			logger.error( proEx.getMessage(), proEx.getException() );

			Loggers.business.debug( "{}: {} {}", new Object[]{ getParameter("orderKey"), processType ,"throw proEx;" } );
			throw proEx;
		} finally {
			Loggers.business.debug( "{}: {} {}", new Object[]{ getParameter("orderKey"), processType ,"return null;" } );
			org.slf4j.MDC.remove("ocp.prcType");
			org.slf4j.MDC.remove("ocp.refKey");
			//org.slf4j.MDC.remove("uniqId"); To use MDC correctly, remove uniqId at ServletModel.execute()
		}

		return null;
	}

	/**
	* Parameters : orderKey
	**/
	public void execute_creation() throws OrderProcessException {
		String orderKey = (String)getParameter( "orderKey" );
		if( orderKey == null || orderKey.length() == 0 )
			throw new OrderProcessException ( OrderProcessException.ERR_INVALID_PARAMETER );

		// Record lock
		if( !orderLockFromOrderKey(orderKey) )
			throw new OrderProcessException( OrderProcessException.ERR_CANNOT_LOCK_ORDER );

		OrderCanonicalXMLWriter writer = new OrderCanonicalXMLWriter( this.handler, orderKey, ORDER_IF_CREATION, logger );
		String xmlString = null;
		try {
			Document document = getDocument();

			int deleted = -1;
			int count = 0;
			try {
				deleted = new com.irt.dpr.OrderDetail(this.handler).deleteZeroOrderQty(orderKey);
				if( deleted > 0 )
					logger.info( orderKey + ": zeroOrderQty deleted("+ deleted +")" );

				count = writer.write( document );

				if( count == 0 ) {
					logger.error( "[OrderKey : "+ orderKey + "] - no Data(Creation)!" );
					throw new OrderProcessException( OrderProcessException.ERR_HAS_NODATA );
				}
			} catch( DataException dataEx ) {
				logger.error( "Order Creation requestError", dataEx );
				throw new OrderProcessException( OrderProcessException.ERR_WM_DATACREATION_FAILED, dataEx );
			} catch( SQLException sqlEx ) {
				logger.error( "Order Creation requestError" );
				throw new OrderProcessException( OrderProcessException.ERR_WM_DATACREATION_FAILED, sqlEx );
			}

			orderLogging.makeOrderTraceFile( ORDER_IF_CREATION, "REQ_" + orderKey, document );

			xmlString = OrderXMLUtility.docConvertString( document, logger );
			if( xmlString == null || xmlString.length() == 0 ) {
				throw new OrderProcessException( OrderProcessException.ERR_WM_CANONICALXML_FAILED );
			}

			Map<String, Object> paramsMap = new java.util.HashMap<String, Object>();
			paramsMap.put( "salesOrder", xmlString );
			paramsMap.put( "portalUser", getUserId(writer.getPortalUser()) );

			String outputXML = connectBTPi( ORDER_IF_CREATION, paramsMap, logger );
			if( outputXML != null && outputXML.contains("<ORDER_CREATE_RESPONSE>") ) {
				String xmlDataString = outputXML.substring( outputXML.indexOf("<ORDER_CREATE_RESPONSE>") + 23, outputXML.indexOf("</ORDER_CREATE_RESPONSE>") );
				xmlDataString = xmlDataString.replaceAll( "<", "&lt;" ).replaceAll( ">", "&gt;" );
				xmlDataString = xmlDataString + "</ORDER_CREATE_RESPONSE>";

				outputXML = outputXML.substring( 0, outputXML.indexOf("<ORDER_CREATE_RESPONSE>") + 23 ) + xmlDataString + "</OrderPlacementResponse>";
			}

			OrderCreationReader reader = new OrderCreationReader( handler, logger, orderKey );
			Object[] xmlObjs = null;
			try {
				reader.read( outputXML, writer.getHeaderMap() );
				xmlObjs = reader.getStoredMap();
			} catch( CanonicalXMLException ex ) {
				logger.error( "[OrderKey : "+ orderKey + "] - "+ ex.getMessage() );
				throw new OrderProcessException( ex.getMessage(), ex );
			}

			if( xmlObjs == null || xmlObjs.length < 1 ) {
				logger.error( "[OrderKey : "+ orderKey + "] - No read data(Creation)!" );
				throw new OrderProcessException( OrderProcessException.ERR_WM_NO_READ_DATA );
			} else {
				try {
					@SuppressWarnings("unchecked")
					boolean ret = new com.irt.dpr.Order(handler).updateFromProcess(
							ORDER_IF_CREATION, (Map<String,Object>)xmlObjs[0] );
/* WORKING : order status 처리 */
					if( ret )
						logger.info( "Order Header Updated Success: "+ writer.getOrderNumber() + "->" + reader.getOrderNumber() );
					@SuppressWarnings("unchecked")
					boolean infoResult = new com.irt.dpr.OrderInfo(handler).updateOrderInfo( (Map<String,Object>)xmlObjs[0] );
					if( infoResult )
						logger.info( "Order Info Update Success: "+ writer.getOrderNumber() + "->" + reader.getOrderNumber() );
					else
						logger.info( "Order info Update Failed: "+ writer.getOrderNumber() + "->" + reader.getOrderNumber() );

					if( !ret && !infoResult ) {
						logger.info( "Order Creation Result reflected failed. Order Heaer :"+ ret + ", Order Info :" + infoResult );
						throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT );
					}

				} catch ( com.irt.data.DataException dataEx ) {
					logger.error( "order result writing has failed to DPR_ORDER Table" );
					throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, dataEx );
				} catch ( SQLException sqlEx ) {
					logger.error( "order result writing has failed to DPR_ORDER Table" );
					throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, sqlEx );
				}
			}
		} catch( CanonicalXMLException xmlEx ) {
			throw new OrderProcessException( OrderProcessException.ERR_WM_CANONICALXML_FAILED );
		} finally {
			try { handler.commit(); } catch( SQLException sqlEx ) {}
		}
	}

	public void execute_billing() throws OrderProcessException {
		String billingNumber = (String)getParameter( "billingNumber" );
		String soldPartyCode = (String)getParameter( "soldPartyCode" );
		String uniqId = (String)getParameter( "updateUserId" );

		Logger logger = getLogger();

		Map<String, Object> paramsMap = new java.util.HashMap<String, Object>();
		paramsMap.put( "BILLING_DOC", billingNumber );
		paramsMap.put( "USER", soldPartyCode );
		paramsMap.put( "portalUser", getUserId(uniqId) );

		String outputXML = connectBTPi( ORDER_IF_BILLING, paramsMap, logger );
		try {
			OrderBillingStatusReader billing = new OrderBillingStatusReader( handler, logger, null, null, billingNumber );
			int ret = billing.read( outputXML );
			if( ret > 0 )
				billing.process();
		} catch( CanonicalXMLException xmlEx ) {
			logger.error( xmlEx );
			throw new OrderProcessException( OrderProcessException.ERR_WM_CANONICALXML_FAILED );
		}
	}

	/**
	* Parameters : orderKey
	**/
	public String execute_rdd() throws OrderProcessException {
		String orderKey = (String)getParameter( "orderKey" );
		Map<String, Object> recordMap = new java.util.HashMap<String, Object>( parameterMap );
		Logger logger = getLogger();
		try {
			if( recordMap.size() < 6 && orderKey != null && orderKey.length() > 0 ) {
				recordMap = new com.irt.dpr.Order(handler).getRecord( com.irt.dpr.Order.createPrimary(orderKey) );
			}
		} catch( java.sql.SQLException sqlEx ) {
			logger.error( "Can't read paramters" );
			throw new OrderProcessException ( OrderProcessException.ERR_INVALID_PARAMETER, sqlEx );
		}

		if( recordMap.size() <= 1 ) {
			logger.info( "Parameters incorrect : "+ recordMap.size() );

			throw new OrderProcessException ( OrderProcessException.ERR_INVALID_PARAMETER );
		}

		if( orderKey != null && orderKey.length() > 0 ) {
			try {
				if( (new com.irt.dpr.Order(handler).getRecordCount(com.irt.dpr.Order.createPrimary(orderKey))) > 0 )
					if( !orderLockFromOrderKey(orderKey) )
						throw new OrderProcessException( OrderProcessException.ERR_CANNOT_LOCK_ORDER );
			} catch( SQLException sqlEx ) {}
		}

		Map<String, Object> paramsMap = new java.util.HashMap<String, Object>();
		paramsMap.put( "portalUser", getUserId((String)recordMap.get("updateUserId")) );
		paramsMap.put( "customerShipTo", recordMap.get("shipPartyCode") );
		paramsMap.put( "salesOrg", recordMap.get("organizationCode") );
		paramsMap.put( "distChannel", recordMap.get("distributionChannelCode") );
		paramsMap.put( "division", recordMap.get("divisionCode") );

		String outputXML = connectBTPi( ORDER_IF_RDD, paramsMap, logger );
		try {
			OrderLogging orderLogger = new OrderLogging( logger, null );
			Document doc = orderLogger.makeOrderTraceFile( orderLogger.getFile(OrderCanonicalProcess.ORDER_IF_RDD, "RES_" + orderKey), outputXML );

			if( outputXML != null ) {
				String rdd = OrderXMLUtility.getTagValue( doc, "GetRDDResponse", "RDD" );

				String formatString = null;
				if( rdd != null ) {
					if( rdd.length() == 8 )
						formatString = rdd.substring(5) + "-" + rdd.substring( 2, 4) + "-" + rdd.substring( 0, 2 );
					else if( rdd.length() == 10 && !rdd.equals("00.00.0000") ) {
						formatString = rdd.substring(6) + "-" + rdd.substring( 3, 5) + "-" + rdd.substring( 0, 2 );
					} else {
						logger.debug("RDDLOG: SAPRDD Value - " + rdd +" ( ERR_WM_DATA_INCORRECT_FORMAT ) [ "
								+ "orderKey=" + orderKey +","
								+ "portalUser=" + paramsMap.get("portalUser") +","
								+ "customerShipTo=" + paramsMap.get("customerShipTo") +","
								+ "salesOrg=" + paramsMap.get("salesOrg") +","
								+ "distChannel=" + paramsMap.get("distChannel") +","
								+ "division=" + paramsMap.get("division") + " ]" );
						paramsMap.put( "requestedDateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()) );
						paramsMap.put( "organizationCode", paramsMap.get("salesOrg") );
						paramsMap.put( "orderKey", orderKey );
						paramsMap.put( "messageKey", OrderProcessException.ERR_WM_DATA_INCORRECT_FORMAT );
						paramsMap.put( "message", handler.getMessageHandler().getMessage(OrderProcessException.ERR_WM_DATA_INCORRECT_FORMAT) );
						MailTransport sender = new MailTransport(handler, logger, paramsMap );
						sender.send( MailTransport.SENDMAILTYPE_RDDFAILURE );
						throw new OrderProcessException( OrderProcessException.ERR_WM_DATA_INCORRECT_FORMAT );
					}
				} else {
					logger.debug("RDDLOG: SAPRDD Value - null ( ERR_WM_NO_READ_DATA ) [ "
								+ "orderKey=" + orderKey +","
								+ "portalUser=" + paramsMap.get("portalUser") +","
								+ "customerShipTo=" + paramsMap.get("customerShipTo") +","
								+ "salesOrg=" + paramsMap.get("salesOrg") +","
								+ "distChannel=" + paramsMap.get("distChannel") +","
								+ "division=" + paramsMap.get("division") + " ]" );
					paramsMap.put( "requestedDateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()) );
					paramsMap.put( "organizationCode", paramsMap.get("salesOrg") );
					paramsMap.put( "orderKey", orderKey );
					paramsMap.put( "messageKey", OrderProcessException.ERR_WM_NO_READ_DATA );
					paramsMap.put( "message", handler.getMessageHandler().getMessage(OrderProcessException.ERR_WM_NO_READ_DATA) );
					MailTransport sender = new MailTransport(handler, logger, paramsMap );
					sender.send( MailTransport.SENDMAILTYPE_RDDFAILURE );
					throw new OrderProcessException( OrderProcessException.ERR_WM_NO_READ_DATA );
				}

				logger.info( "SAP RDD is converting " + rdd + "->" + formatString );

				return formatString;
			} else {
				logger.debug("RDDLOG: SAPRDD Output - null ( ERR_WM_NO_READ_DATA ) [ "
								+ "orderKey=" + orderKey +","
								+ "portalUser=" + paramsMap.get("portalUser") +","
								+ "customerShipTo=" + paramsMap.get("customerShipTo") +","
								+ "salesOrg=" + paramsMap.get("salesOrg") +","
								+ "distChannel=" + paramsMap.get("distChannel") +","
								+ "division=" + paramsMap.get("division") + " ]" );
				paramsMap.put( "requestedDateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()) );
				paramsMap.put( "organizationCode", paramsMap.get("salesOrg") );
				paramsMap.put( "orderKey", orderKey );
				paramsMap.put( "messageKey", OrderProcessException.ERR_WM_NO_READ_DATA );
				paramsMap.put( "message", handler.getMessageHandler().getMessage(OrderProcessException.ERR_WM_NO_READ_DATA) );
				MailTransport sender = new MailTransport(handler, logger, paramsMap );
				sender.send( MailTransport.SENDMAILTYPE_RDDFAILURE );
				throw new OrderProcessException( OrderProcessException.ERR_WM_NO_READ_DATA );
			}
		} finally {
			try { handler.commit(); } catch( SQLException ex ) {}
		}
	}

	@SuppressWarnings({ "unchecked" })
	public void execute_simulation() throws OrderProcessException {
		String orderKey = (String)getParameter("orderKey");
		if( orderKey == null || orderKey.length() == 0 )
			throw new OrderProcessException ( OrderProcessException.ERR_INVALID_PARAMETER );

		if( !orderLockFromOrderKey(orderKey) )
			throw new OrderProcessException( OrderProcessException.ERR_CANNOT_LOCK_ORDER );

		OrderCanonicalXMLWriter writer = new OrderCanonicalXMLWriter( this.handler, orderKey, logger );
		String xmlString = null;
		try {
			Document document = getDocument();
			try {
				writer.write( document );
			} catch( SQLException sqlEx ) {
				logger.info( Loggers.STR_BUSINESS + orderKey + ": Order Simulation request Error" );
				throw new OrderProcessException( OrderProcessException.ERR_WM_DATACREATION_FAILED, sqlEx );
			}

			/* Requset Data Logging */
			orderLogging.makeOrderTraceFile( ORDER_IF_SIMULATION, "REQ_" + orderKey, document );

			xmlString = OrderXMLUtility.docConvertString( document, logger );
			if( xmlString == null || xmlString.length() == 0 )
				throw new OrderProcessException( OrderProcessException.ERR_WM_CANONICALXML_FAILED );

			Map<String, Object> paramsMap = new java.util.HashMap<String, Object>();
			paramsMap.put( "salesOrder", xmlString );
			paramsMap.put( "portalUser", getUserId(writer.getPortalUser()) );
			paramsMap.put( "salesOrg", writer.getOrganizationCode() );

			String outputXML = connectBTPi( ORDER_IF_SIMULATION, paramsMap, logger );
			if( outputXML != null && outputXML.contains("<xmlData>") ) {
				String xmlDataString = outputXML.substring( outputXML.indexOf("<xmlData>") + 9, outputXML.indexOf("</xmlData>") );
				xmlDataString = xmlDataString.replaceAll( "<", "&lt;" ).replaceAll( ">", "&gt;" );
				xmlDataString = "&lt;?xml version=\"1.0\"?&gt;" + xmlDataString + "</xmlData>";

				outputXML = outputXML.substring( 0, outputXML.indexOf("<xmlData>") + 9) + xmlDataString + "</out_OrderSimulateReturn>";
			}

			OrderSimulationReader reader = new OrderSimulationReader( handler, logger, orderKey );
			reader.writeSimulationResult( outputXML );
			if( outputXML != null && outputXML.contains("doc:SalesOrder.Simulate.Response") )
				outputXML = outputXML.replaceAll( "doc:SalesOrder.Simulate.Response", "SalesOrder.Simulate.Response" );

			document = OrderXMLUtility.stringConvertDoc( outputXML, logger );
			reader.read( outputXML, writer.getHeaderMap(), writer.getDetailMap() );
			Object[] xmlObjs = reader.getStoredMap();

			if( xmlObjs == null || xmlObjs.length != 2 ) {
				logger.error( orderKey + ": Can't not read simulation result!" );
				throw new OrderProcessException( OrderProcessException.ERR_WM_NO_READ_DATA );
			} else {
				try {
					// default parameter setting
					if( ((Map<String, Object>)xmlObjs[0]).get("orderKey") == null )
						((Map<String, Object>)xmlObjs[0]).put( "orderKey", orderKey );
					if( ((Map<String, Object>)xmlObjs[0]).get("updateUserId") == null )
						((Map<String, Object>)xmlObjs[0]).put( "updateUserId", writer.getUpdateUserId() );

					com.irt.dpr.Order order = new com.irt.dpr.Order( handler );
					boolean ret = order.updateFromProcess( ORDER_IF_SIMULATION, (Map<String, Object>)xmlObjs[0]
														, (List<Map<String, Object>>)xmlObjs[1] );

					if( ret ) {
						Loggers.business.debug( "{}: {} {}", new Object[]{orderKey, "Order Simulation Success:"
							+ writer.getOrderNumber() +"->"+ reader.getOrderNumber()} );
					} else
						throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT );
				} catch ( com.irt.data.DataException dataEx ) {
					String errorKey = OrderProcessException.ERR_WM_APPLY_RESULT;
					if( dataEx.getErrorKey() == com.irt.data.FieldException.ERR_CANNOT_NULL )
						errorKey = OrderProcessException.ERR_INVALID_ORDER_LINES;

					logger.info( Loggers.STR_BUSINESS + orderKey + ": " + "Order simulation result writing failed to DPR_ORDER Table. dataEx" );
					throw new OrderProcessException( errorKey, dataEx );
				} catch ( SQLException sqlEx ) {
					logger.info( Loggers.STR_BUSINESS + orderKey + ": " + "Order simulation result writing failed to DPR_ORDER Table. sqlEx" );

					throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, sqlEx );
				}
			}

		} catch( CanonicalXMLException xmlEx ) {
			throw new OrderProcessException( xmlEx.getMessage(), xmlEx );
		} finally {
			try { handler.commit(); } catch( SQLException sqlEx ) {}
		}
	}

	public void execute_statuslist() throws OrderProcessException {
		Object enquiryStartDate = getParameter( "startOrderDate" );
		Object enquiryEndDate = getParameter( "endOrderDate" );
		Object updateUserId = getParameter( "updateUserId" );
		Object organizationCode = getParameter( "organizationCode" );
		Object simulationKey  = getParameter( "simulationKey" );
		Object orderNumber  = getParameter( "orderNumber" );

		int recentCount = 0;
		if( parameterMap.containsKey("recentCount") ) {
			recentCount = (int) parameterMap.get( "recentCount" );
		}

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>( getParameterMap() );
		if( recentCount < 1 ) {
			com.irt.data.Date startDate = null;
			try {
				if( enquiryStartDate instanceof String )
					startDate = com.irt.data.Date.getInstance( (String)enquiryStartDate );
				else if( enquiryStartDate instanceof com.irt.data.Date )
					startDate = (com.irt.data.Date)enquiryStartDate;
			} catch ( java.text.ParseException parseEx ) {
				logger.error( "Enquiry Startdate is invalid date format('YYYY-MM-DD')" );

				throw new OrderProcessException ( OrderProcessException.ERR_INVALID_PARAMETER );
			}

			com.irt.data.Date endDate = null;
			try {
				if( enquiryEndDate instanceof String )
					endDate = com.irt.data.Date.getInstance( (String)enquiryEndDate );
				else if( enquiryEndDate instanceof com.irt.data.Date )
					endDate = (com.irt.data.Date)enquiryEndDate;
			} catch ( java.text.ParseException parseEx ) {}

			if( conditionMap.containsKey("orderDate" + com.irt.data.Condition.SUFFIX_MIN_VALUE)
					|| !conditionMap.containsKey("orderNumber") || conditionMap.containsKey("simulationKey") ) {

				if( endDate != null ) {
					conditionMap.put( "orderDate" + com.irt.data.Condition.SUFFIX_MIN_VALUE, startDate );
					conditionMap.put( "orderDate" + com.irt.data.Condition.SUFFIX_MAX_VALUE, (endDate != null ? endDate.getDate(1) : endDate) );
					conditionMap.put( "orderDate" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MIN );
				} else {
					conditionMap.put( "orderDate", startDate );
				}

				if( orderNumber != null )
					conditionMap.put( "orderNumber", orderNumber );
				else if( simulationKey != null )
					conditionMap.put( "simulationKey", simulationKey );

			}
		} else {
			conditionMap.remove( "orderDate" );
			conditionMap.remove( "orderNumber" );
			conditionMap.remove( "simulationKey" );
		}

		conditionMap.put( "status", com.irt.dpr.Order.STATUS_CREATED );
		conditionMap.remove( "updateUserId" );

		// 사용자 조건
		List<Map<String, Object>> recordList = null;
		try {
			com.irt.dpr.Order db = new com.irt.dpr.Order( handler );
			recordList = db.getRecords( conditionMap,
					new String[] { "orderKey", "orderNumber", "soldPartyCode", "organizationCode", "updateUserId" } );
		} catch( java.sql.SQLException sqlEx ) {
			logger.error( sqlEx );
			throw new OrderProcessException( OrderProcessException.ERR_WM_DATACREATION_FAILED, sqlEx );
		}

		List<Map<String, Object>> soldPartyList = new java.util.ArrayList<Map<String, Object>>();
		if( recordList != null && recordList.size() > 0 ) {
			Map<String, List<String []>> record = new java.util.HashMap<String, List<String []>>();
			List<String []> orderNumberList;

			for( Map<String, Object> map : recordList ) {
				String partyCode = (String)map.get( "soldPartyCode" );
				String _orderNumber = (String)map.get( "orderNumber" );
				String orderKey = (String)map.get( "orderKey" );

				if( record.containsKey(partyCode) )
					orderNumberList = record.get( partyCode );
				else
					orderNumberList = new java.util.ArrayList<String []>();

				orderNumberList.add( new String[] { _orderNumber, orderKey } );
				record.put( partyCode, orderNumberList );
			}

			for( Iterator<String> itr = record.keySet().iterator(); itr.hasNext(); ) {
				String soldPartyCode = itr.next();

				Map<String, Object> map = new java.util.HashMap<String, Object> ();
				map.put( "soldPartyCode", soldPartyCode );
				map.put( "orderNumbers", record.get(soldPartyCode) );
				soldPartyList.add( map );
			}
		}

		if( soldPartyList.size() == 0 ) {
			logger.error( "[SimulationKey : "+ simulationKey + "] - no Data(StatusList)!" );
			throw new OrderProcessException( OrderProcessException.ERR_HAS_NODATA );
		}

		OrderProcessException processException = null;
		for( Map<String, Object> map : soldPartyList ) {
			String soldPartyCode = (String)map.get( "soldPartyCode" );

			@SuppressWarnings("unchecked")
			List<String []> orderNumberList = (List<String []>)map.get( "orderNumbers" );
			Object[] arrays = new Object[ orderNumberList.size() ];
			orderNumberList.toArray( arrays );
			String[] orderNumbers = new String[ orderNumberList.size() ];
			for( int i = 0; i < arrays.length; i++ )
				orderNumbers[i] = ((String [])arrays[i])[0];

			Map<String, Object> paramsMap = new java.util.HashMap<String, Object>();
			paramsMap.put( "ORDER_DOC_ID", orderNumbers );
			paramsMap.put( "USER", soldPartyCode );
			paramsMap.put( "portalUser", getUserId((String)updateUserId) );
			paramsMap.put( "salesOrg", organizationCode );

			String outputXML = connectBTPi( ORDER_IF_STATUSLIST, paramsMap, logger );
			// function
			try {
				invoking_statuslist( outputXML, (String)organizationCode, getUserId((String)updateUserId) +"_"+ orderNumbers.length +"_"+ orderNumbers[0], soldPartyList );
			} catch( OrderProcessException opEx ) {
				/* WORKING DB Logging */
				logger.error( opEx );

				processException = opEx;
			}
		}

		/* WORKING: User logging */
		if( processException != null )
			throw processException;
	}

	/* Table logging 필요 */
	@SuppressWarnings("unchecked")
	public void invoking_statuslist( String outputXML, String organizationCode, String portalUser, List<Map<String, Object>> soldPartyList ) throws OrderProcessException {
		Loggers.business.debug( "{}: {}", getParameter("orderKey"), "start." );

		try {
			OrderStatusListReader reader = new OrderStatusListReader( logger, organizationCode, portalUser, soldPartyList );
			reader.setUniqId( (String)getParameter("updateUserId") );

			reader.read( outputXML );
			Object[] xmlObjs = reader.getStoredMap();

			if( xmlObjs == null || xmlObjs.length < 1 ) {
				logger.error( "Can't not read statuslist result" );
				throw new OrderProcessException( OrderProcessException.ERR_WM_NO_READ_DATA );
			}

			// DPR_ORDER_INFO, DPR_ORDER_DELIVERY, DPR_ORDER_BILLING, DPR_ORDER_MEMOS Insert, Update
			com.irt.data.DataResult dataResult = new com.irt.data.DataResult();
			try {
				dataResult = new com.irt.dpr.OrderInfo(handler).updateWithRelation(
						(List<Map<String, Object>>)xmlObjs[0] );

				if( dataResult.getException() != null )
					logger.error( dataResult.getException() );
				logger.info( "Status List Info allCount : "+ dataResult.getCount() +" Insert : " + dataResult.getRegistCount()
						+ ", Modify : " + dataResult.getModifyCount() + " Error : " + dataResult.getErrorCount() );

			} catch( com.irt.data.DataException dataEx ) {
				throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, dataEx );
			}

			// DPR_ORDER Update
			dataResult = new com.irt.dpr.Order(handler).updateOrderHeaderFromStatus(
					(List<Map<String, Object>>)xmlObjs[0], ORDER_IF_STATUSLIST );

			if( dataResult.getException() != null )
				logger.error( dataResult.getException() );
			logger.info( "Order Header update count: ALL:" + dataResult.getCount() + ", UPG:" + dataResult.getModifyCount()
					+ ", ERR:"+ dataResult.getErrorCount() );

			if( dataResult.getCount() > 0 && dataResult.getSuccessCount() == 0 )
				logger.error( "Can't write Order Header Table: condition:"+ getParameterMap(), dataResult.getException() );
			else {
				logger.info( "Status List Enquiry Success" );

				handler.commit();
			}

		} catch( CanonicalXMLException xmlEx ) {
			logger.error( xmlEx );

			throw new OrderProcessException( OrderProcessException.ERR_WM_CANONICALXML_FAILED, xmlEx );
		} catch( SQLException sqlEx ) {
			logger.error( sqlEx );

			throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, sqlEx );
		}
	}

	@SuppressWarnings("unchecked")
	public void execute_status() throws OrderProcessException {
		Object orderKey = getParameter( "orderKey" );
		Object orderNumber = getParameter( "orderNumber" );
		Object soldPartyCode = getParameter( "soldPartyCode" );
		Object organizationCode = getParameter( "organizationCode" );
		Object uniqId = getParameter( "updateUserId" );

		Map<String, Object> headerMap= null;

		/* orderKey, orderNumber가 있으면 Table에서 정보를 읽음 */
		if( (orderKey != null && ((String)orderKey).length() > 0) || (orderNumber != null && ((String)orderNumber).length() > 0) ) {
			try {
				Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
				conditionMap.put( "status", com.irt.dpr.Order.STATUS_CREATED );
				if( orderKey != null && ((String)orderKey).length() > 0 )
					conditionMap.put( "orderKey", orderKey );
				else
					conditionMap.put( "orderNumber", orderNumber );

				List<Map<String, Object>> recordList = new com.irt.dpr.Order(handler).getRecords( conditionMap
					, new String[] { "orderKey", "orderNumber", "organizationCode", "soldPartyCode", "shipPartyCode", "updateUserId" } );
				headerMap = ( recordList != null ? recordList.get(0) : null );
				if( headerMap != null ) {
					orderKey = headerMap.get( "orderKey" );
					orderNumber = headerMap.get( "orderNumber" );
					organizationCode = headerMap.get( "organizationCode" );
					soldPartyCode = headerMap.get( "soldPartyCode" );
					if( uniqId == null || ((String)uniqId).length() == 0 )
						uniqId = headerMap.get( "updateUserId" );
				}
			} catch( SQLException sqlEx ) {
				logger.info( "Can't read order header [OrderKey : "+ orderKey +"]" );
			}
		}

		if( orderNumber == null || soldPartyCode == null || getUserId((String)uniqId) == null )
			throw new OrderProcessException( OrderProcessException.ERR_INVALID_PARAMETER );

		Map<String, Object> paramsMap = new java.util.HashMap<String, Object>();
		paramsMap.put( "SALES_ORDER", orderNumber );
		paramsMap.put( "USER", soldPartyCode );
		paramsMap.put( "portalUser", getUserId((String)uniqId) );

		String outputXML = connectBTPi( ORDER_IF_STATUS, paramsMap, logger );
		if( outputXML == null ) return;
		else if( outputXML.startsWith("*****ECC Returned null") ) {
			logger.warn( "### invalid XML format ###" );
			logger.debug( "### headerMap:"+ headerMap );
			logger.debug( "### outputXML:"+ outputXML );

			try {
				(new com.irt.dpr.Order(handler)).updateOrderHeaderFromStatus( headerMap, ORDER_IF_STATUS );
			} catch( SQLException sqlEx ) {
				throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, sqlEx );
			} catch( com.irt.data.DataException dataEx ) {
				throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, dataEx );
			}
			return;
		}

		try {
			OrderStatusReader reader = new OrderStatusReader( handler, logger, (String)orderKey, (String)organizationCode );
			reader.setUniqId( (String)uniqId );

			int ordItemCnt = 0;
			ordItemCnt = reader.read( outputXML );
			Object[] xmlObjs = reader.getStoredMap();

			if( xmlObjs == null ) {
				logger.error( orderKey + ": Cannot read status result!" );
				throw new OrderProcessException( OrderProcessException.ERR_WM_NO_READ_DATA );
			} else if( xmlObjs.length < 2 ) {
				logger.error( orderKey + ": Cannot read status detail result!" );
				throw new OrderProcessException( OrderProcessException.ERR_WM_NO_READ_DATA );
			}

			// All items is null
			if( ordItemCnt == -1 ) {
				Loggers.business.debug( "{}: {}", orderKey, "Cannot read WM[ orderLines ] status result." );
				try {
					String resetQuery =
						"UPDATE DPR_ORDER_INFO_DTL SET CONFIRMED_ORDERQTY = 0, CONFIRMED_ORDERVALUE = 0"
								+ ", CONFIRMED_ORDERTAX = 0, CONFIRMED_ORDERDISCOUNT = 0, CONFIRMED_ORDERTOTAL = 0"
								+ ", DLVRY_STATUS = NULL, DLVRY_OPENQTY = NULL, DLVRY_INTRAQTY = NULL, DLVRY_COMPQTY = NULL"
						+ " WHERE ORDER_NUMBER = ?";
					int resetCount = SQLManager.executeStatement( handler, resetQuery, new Object[] { orderNumber } );

					boolean ret = new com.irt.dpr.Order(handler).updateStatusWithDetailOnly( (String)orderKey, com.irt.dpr.OrderDetail.STATUS_SAP_DELETED, (String)uniqId );

					if( ret ) {
						handler.commit();
						Loggers.business.debug( "{}: {}", orderKey, "(DPR_ORDER) Status 'DE' save end. [orderNumber: "+ reader.getOrderNumber() +"]" );
					} else {
						handler.rollback();
						Loggers.business.debug( "{}: {}", orderKey, "(DPR_ORDER) Status 'DE' save failed. [orderNumber: "+ reader.getOrderNumber() +"]" );
						throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT );
					}
					Loggers.business.debug( "{}: {}", orderKey, "ret=" + ret + " resetCount=" + resetCount );

				} catch( SQLException sqlEx ) {
					Loggers.business.debug( "{}: {}", orderKey, "throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, sqlEx );" );
					throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, sqlEx );
				} catch (DataException dataEx) {
					Loggers.business.debug( "{}: {}", orderKey, "throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, dataEx );" );
					throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, dataEx );
				}
			} else {
				try {
					new com.irt.dpr.Order(handler).updateStatusWithDetailOnly( (String)orderKey, com.irt.dpr.OrderDetail.STATUS_NORMAL, (String)uniqId );

					((Map<String,Object>)xmlObjs[0]).put( "updateUserId", uniqId );
					boolean ret = new com.irt.dpr.OrderInfo(handler).updateWithDetail(
							(Map<String,Object>)xmlObjs[0], (List<Map<String, Object>>)xmlObjs[1] );

					if( ret ) {
						handler.commit();
						logger.info( Loggers.STR_BUSINESS + orderKey + ": Status Enquiry Information(DPR_ORDER_INFO) save done. ["+ reader.getOrderNumber() +"]" );
					} else {
						handler.rollback();
						logger.info( Loggers.STR_BUSINESS + orderKey + ": Status Enquiry Informaton(DPR_ORDER_INFO) save failed. ["+ reader.getOrderNumber() +"]" );
						throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT );
					}

					// DPR_ORDER, DPR_ORDER_DTL Update
					com.irt.dpr.Order order = new com.irt.dpr.Order( handler );
					ret = order.updateOrderHeaderFromStatus( (Map<String,Object>)xmlObjs[0], ORDER_IF_STATUS );
					if( ret ) {
						com.irt.data.DataResult resultSet = order.updateOrderDetailFromStatus( (List<Map<String,Object>>)xmlObjs[1], (String)orderKey );
						logger.info( Loggers.STR_BUSINESS + orderKey + ": Order Detail update count: ALL:" + resultSet.getCount()
								+ ", SUC:" + resultSet.getSuccessCount() + ", INS:" + resultSet.getRegistCount()
								+ ", UPG:" + resultSet.getModifyCount() + ", ERR:"+ resultSet.getErrorCount() );

						if( resultSet.getCount() == (resultSet.getErrorCount() + resultSet.getIgnoreCount()) ) {
							handler.rollback();
							logger.error( orderKey + ": Can't write Order Detail Table: condition:"+ getParameterMap(), resultSet.getException() );
						} else {
							handler.commit();
							logger.info( Loggers.STR_BUSINESS + orderKey + ": Status Enquiry Success" );
						}
					} else {
						handler.rollback();
						logger.error( orderKey + ": ret=false Can't write Order Header Table: condition:"+ getParameterMap() );
						throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT );
					}
				} catch( SQLException sqlEx ) {
					throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, sqlEx );
				} catch( com.irt.data.DataException dataEx ) {
					throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, dataEx );
				}
			}
		} catch( CanonicalXMLException xmlEx ) {
			logger.error( xmlEx );

			throw new OrderProcessException( OrderProcessException.ERR_WM_CANONICALXML_FAILED, xmlEx );
		} catch( OrderProcessException proEx ) {
			logger.error( handler.getMessageHandler().getMessage(proEx.getErrorKey()), proEx.getException() );

			throw proEx;
		}
	}

	public String connectBTPi( String processType, Map<String, Object> paramsMap, Logger logger ) throws OrderProcessException {
		HttpURLConnection conn = null;

		try {
			if( systemInfo == null || systemInfo.size() == 0 )
				systemInfo = OrderCanonicalProcess.getSystemInfo();

			String urlString = (String)systemInfo.get( processType  + "_url" );
			String userName = (String)systemInfo.get( "userName" );
			String userPassword = (String)systemInfo.get( "password" );
			String basicAuth = "Basic " + Base64.getEncoder().encodeToString( (userName +":"+ userPassword).getBytes() );

			String[] paramkeys = { "portalUser", "customerShipTo", "salesOrg", "distChannel", "division", "SALES_ORDER", "USER", "ORDER_DOC_ID", "BILLING_DOC" };
			for( String key : paramkeys ) {
				Object[] objs;
				Object obj = paramsMap.get( key );
				if( obj == null ) continue;

				if( obj instanceof Object[] )
					objs = (Object[])obj;
				else
					objs = new Object[] { obj };
				for( Object val : objs ) {
					if( val == null ) continue;
					urlString += (urlString.indexOf("?") > 0 ? "&" : "?") + key +"="+ URLEncoder.encode(val.toString(), "UTF-8");
				}
			}
			logger.debug( "Rest UserName : "+ userName +", URL : "+ urlString );

			conn = (HttpURLConnection)(new URL(urlString)).openConnection();
			conn.setDoOutput( true );
			conn.setDoInput( true );
			conn.setRequestMethod( "POST" );
			conn.setRequestProperty( "Content-Type", "application/xml; charset=UTF-8" );
			conn.setRequestProperty( "Accept", "application/xml" );
			conn.setRequestProperty( "Authorization", basicAuth );

			Object salesOrderXml = paramsMap.get( "salesOrder" );
			if( salesOrderXml != null ) {
				try( OutputStream os = conn.getOutputStream() ) {
					os.write( salesOrderXml.toString().getBytes("UTF-8") );
				}
			}

			int responseCode = conn.getResponseCode();
			logger.debug( "ResponseCode : "+ responseCode );

			StringBuilder sb = new StringBuilder();
			try( BufferedReader in = new BufferedReader(new InputStreamReader((responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()), "UTF-8")) ) {
				String line;
				while( (line = in.readLine()) != null )
					sb.append( line );
			}

			String responseXml = sb.toString();
			if( responseCode != 200 )
				logger.error("REST call failed. HTTP " + responseCode + " Response: " + responseXml);

			return responseXml;
		} catch( MalformedURLException ex ) {
			throw new OrderProcessException( OrderProcessException.ERR_WM_CONNECTION_FAILED, ex );
		} catch( ProtocolException ex ) {
			throw new OrderProcessException( OrderProcessException.ERR_WM_CONNECTION_FAILED, ex );
		} catch( IOException ex ) {
			throw new OrderProcessException( OrderProcessException.ERR_WM_CONNECTION_FAILED, ex );
		} catch( IllegalArgumentException illegalEx ) {
			throw new OrderProcessException( OrderProcessException.ERR_WM_CONNECTION_FAILED, illegalEx );
		} finally {
			if( conn != null ) conn.disconnect();
		}
	}

	public Document getDocument() throws OrderProcessException {
		try {
			javax.xml.parsers.DocumentBuilder builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.newDocument();
		} catch( javax.xml.parsers.ParserConfigurationException parserEx ) {
			throw new OrderProcessException( OrderProcessException.ERR_WM_CANONICALXML_FAILED, parserEx );
		}
	}

	public Logger getLogger() {
		return Logger.getLogger( "com.irt.dpr.tools.OrderCanonicalProcess" );
	}

	public Object getParameter( String key ) {
		return parameterMap.get( key );
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	private String getUserId( String portalUser ) {
		if( portalUser == null ) return null;

		if( portalUser.indexOf("@") > 0 ) return portalUser.substring( 0, portalUser.indexOf("@") );
		else return portalUser;
	}

	public static Map<String, Object> getSystemInfo() throws IllegalArgumentException {
		return getSystemInfo(systemConfig);
	}

	public static Map<String, Object> getSystemInfo( SystemConfig systemConfig ) throws IllegalArgumentException {
		Properties properties = new Properties();
		Map<String, Object> systemInfoMap = new java.util.HashMap<String, Object>();
		String toolsConfigureFileName = null;

		java.io.InputStream inputStream = null;
		try {
			toolsConfigureFileName = systemConfig.getProperty( "toolsConfig" );
			inputStream = new java.io.FileInputStream( new java.io.File(toolsConfigureFileName) );
			properties.load( inputStream );

			com.irt.rbm.tools.Configure configure = new com.irt.rbm.tools.Configure();
			configure.load( properties );

			com.irt.dpr.tools.Configure dpr_configure = new com.irt.dpr.tools.Configure( configure );
			systemInfoMap = dpr_configure.getSystemInfo();
		} catch( Exception ex ) {
			throw new IllegalArgumentException( "Cant't read property files. ["+ toolsConfigureFileName +"]", ex );
		} finally {
			try { inputStream.close(); } catch( Exception ignored ) {}
		}

		return systemInfoMap;
	}

	private boolean orderLockFromOrderKey( String orderKey ) {
		String query = "SELECT * FROM DPR_ORDER ORD, DPR_ORDER_DTL ORDD WHERE ORDD.ORDERKEY(+) = ORD.ORDER_KEY"
				+ " AND ORD.ORDER_KEY = ?";

		java.sql.PreparedStatement pstmt = null;
		java.sql.ResultSet rset = null;
		try {
			pstmt = handler.getConnection().prepareStatement( query );
			pstmt.setObject( 1, orderKey );

			rset = pstmt.executeQuery();
			return (rset.next() ? true : false );
		} catch( SQLException sqlEx ) {
			try { handler.rollback(); } catch( SQLException ex ) {}

			return false;
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public void setConnectionInfo( Map<String, Object> infoMap ) {
		this.systemInfo = infoMap;
	}

	public void setParameter( Map<String, Object> requestMap ) {
		for( Map.Entry<String, Object> entry : requestMap.entrySet() )
			parameterMap.put( entry.getKey(), entry.getValue() );
	}

	public void setParameter( String key, Object value ) {
		parameterMap.put( key, value );
	}
}
