/*
 *	File Name:	StockQueryCanonical.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/12/31		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.irt.dpr.Country;
import com.irt.dpr.OrderProcessException;
import com.irt.dpr.StockQuery;
import com.irt.dpr.util.Loggers;
import com.irt.servlet.SystemConfig;
import com.irt.sql.SQLHandler;

/**
 *
 */
public class StockQueryCanonical {//@formatter:off
	public final static String STOCKQUERY_IF_SIMULATION		= "stock_simulation";



	SQLHandler handler;
	Logger logger;
	SystemConfig systemConfig;
	OrderLogging orderLogging;

	String simulationKey;
	String processType;
	String errorKey;

	Map<String, Object> headerMap;
	Map<String, Object> systemInfo;

	public StockQueryCanonical( SQLHandler handler, SystemConfig systemConfig, Map<String, Object> headerMap ) {
		this.handler = handler;
		this.logger = getLogger();
		this.systemInfo = OrderCanonicalProcess.getSystemInfo(systemConfig);
		this.orderLogging = new OrderLogging( this.logger, this.systemInfo );
		this.systemConfig = systemConfig;
		this.headerMap = headerMap;
		this.errorKey = null;
		this.processType = STOCKQUERY_IF_SIMULATION;
		this.simulationKey = (String) headerMap.get( "simulationKey" );
	}

	public String execute() throws OrderProcessException {
		Loggers.business.debug( "{}: {} {}", new Object[]{ simulationKey, processType ,"start." } );
		org.slf4j.MDC.put( "ocp.prcType", processType );
		org.slf4j.MDC.put( "ocp.refKey", simulationKey );
		org.slf4j.MDC.put( "uniqId", (String)headerMap.get("updateUserId") );
		try {
			if( STOCKQUERY_IF_SIMULATION.equals(processType) )
				execute_simulation();
			else
				logger.error( "InValid Process Type : " + processType );
		} catch( OrderProcessException proEx ) {
			logger.error( proEx.getMessage(), proEx.getException() );
			Loggers.business.debug( "{}: {} {}", new Object[]{ simulationKey, processType ,"throw proEx;" } );
			throw proEx;
		} finally {
			Loggers.business.debug( "{}: {} {}", new Object[]{ simulationKey, processType ,"return null;" } );
			org.slf4j.MDC.remove( "ocp.prcType" );
			org.slf4j.MDC.remove( "ocp.refKey" );
			//org.slf4j.MDC.remove("uniqId"); To use MDC correctly, remove uniqId at ServletModel.execute()
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public void execute_simulation() throws OrderProcessException {
		if( simulationKey == null || simulationKey.length() == 0 )
			throw new OrderProcessException ( OrderProcessException.ERR_INVALID_PARAMETER );

		OrderCanonicalProcess op = new OrderCanonicalProcess( handler, systemConfig, OrderCanonicalProcess.ORDER_IF_SIMULATION );

		String queryKey = (String) headerMap.get( "queryKey" );
		StockQueryXMLWriter writer = new StockQueryXMLWriter( this.handler, headerMap, logger );
		String xmlString = null;
		try {
			Document document = getDocument();
			try {
				writer.write( document );
			} catch( SQLException sqlEx ) {
				logger.info( Loggers.STR_BUSINESS + simulationKey + ": Stock Query Simulation request Error" );
				throw new OrderProcessException( OrderProcessException.ERR_WM_DATACREATION_FAILED, sqlEx );
			}

			/* Requset Data Logging */
			orderLogging.makeOrderTraceFile( STOCKQUERY_IF_SIMULATION, "REQ_" + simulationKey, document );

			xmlString = OrderXMLUtility.docConvertString( document, logger );
			if( xmlString == null || xmlString.length() == 0 )
				throw new OrderProcessException( OrderProcessException.ERR_WM_CANONICALXML_FAILED );

			Map<String, Object> paramsMap = new java.util.HashMap<String, Object>();
			paramsMap.put( "salesOrder", xmlString );
			paramsMap.put( "portalUser", getUserId(writer.getPortalUser()) );
			paramsMap.put( "salesOrg", headerMap.get("organizationCode") );

			String outputXML = op.connectBTPi( OrderCanonicalProcess.ORDER_IF_SIMULATION, paramsMap, logger );

			Map<String, Object> extraMap = new HashMap<String, Object>();
			extraMap.put( "simulationKey", simulationKey );

			// BAPI XML Result
			StockQuerySimulationReader reader = new StockQuerySimulationReader( handler, logger, simulationKey, queryKey, systemInfo );
			reader.read( outputXML, writer.getHeaderMap(), writer.getDetailMap() );
			Object[] xmlObjs = reader.getStoredMap();

			if( xmlObjs == null || xmlObjs.length != 2 ) {
				logger.error( simulationKey + ": Can't not read simulation result!" );
				throw new OrderProcessException( OrderProcessException.ERR_WM_NO_READ_DATA );
			} else {
				try {
					// default parameter setting
					if( ((Map<String, Object>)xmlObjs[0]).get("simulationKey") == null )
						((Map<String, Object>)xmlObjs[0]).put( "simulationKey", simulationKey );
					if( ((Map<String, Object>)xmlObjs[0]).get("updateUserId") == null )
						((Map<String, Object>)xmlObjs[0]).put( "updateUserId", writer.getUpdateUserId() );

					com.irt.dpr.StockQuery stockQuery = new com.irt.dpr.StockQuery( handler );
					boolean ret = stockQuery.updateFromProcess( (Map<String, Object>)xmlObjs[0]
														, (List<Map<String, Object>>)xmlObjs[1] );

					if( ret ) {
						Loggers.business.debug( "{}: {} {}", new Object[] { simulationKey, "StockQuery Simulation Success:"
							+ simulationKey } );
					} else
						throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT );
				} catch ( com.irt.data.DataException dataEx ) {
					String errorKey = OrderProcessException.ERR_WM_APPLY_RESULT;
					if( dataEx.getErrorKey() == com.irt.data.FieldException.ERR_CANNOT_NULL )
						errorKey = OrderProcessException.ERR_INVALID_ORDER_LINES;

					logger.info( Loggers.STR_BUSINESS + simulationKey + ": " + "StockQuery simulation result writing failed to DPR_STOCK_QUERY_SIM Table. dataEx" );
					throw new OrderProcessException( errorKey, dataEx );
				} catch ( SQLException sqlEx ) {
					logger.info( Loggers.STR_BUSINESS + simulationKey + ": " + "StockQuery simulation result writing failed to DPR_STOCK_QUERY_SIM Table. sqlEx" );

					throw new OrderProcessException( OrderProcessException.ERR_WM_APPLY_RESULT, sqlEx );
				}
			}

		} catch( CanonicalXMLException xmlEx ) {
			throw new OrderProcessException( xmlEx.getMessage(), xmlEx );
		} finally {
			try { handler.commit(); } catch( SQLException sqlEx ) {}
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
		return Logger.getLogger( "com.irt.dpr.StockQueryCanonical" );
	}

	private String getUserId( String portalUser ) {
		if( portalUser == null ) return null;

		if( portalUser.indexOf("@") > 0 ) return portalUser.substring( 0, portalUser.indexOf("@") );
		else return portalUser;
	}

	class StockQueryXMLWriter extends CanonicalXMLWriter {
		public static final String WM_JNJID								= OrderCanonicalXMLWriter.WM_JNJID;
		public static final String DELIVERYBLOCK_PORTAL					= OrderCanonicalXMLWriter.DELIVERYBLOCK_PORTAL;
		Map<String, Object> headerMap;
		Map<Number, String[]> itemMap;
		String simulationKey, organizationCode, soldPartyCode, portalUser;
		String documentCountryCode;

		public StockQueryXMLWriter( SQLHandler handler, Map<String, Object> headerMap, Logger logger ) {
			super( handler, logger );
			super.receiverId = CanonicalXML.DEFAULT_ORDER_JNJID;
			this.headerMap = headerMap;
			this.itemMap = new java.util.HashMap<Number, String[]>();
			this.simulationKey = (String) headerMap.get( "simulationKey" );
			this.organizationCode = (String)headerMap.get( "organizationCode" );
			this.soldPartyCode = (String)headerMap.get( "soldPartyCode" );
			this.portalUser = (String)headerMap.get( "updateUserId" );
		}

		public Map<String, Object> getHeaderMap() {
			return new java.util.HashMap<String, Object> ( headerMap );
		}

		public Map<Number, String[]> getDetailMap() {
			return itemMap;
		}

		public String getPortalUser() {
			String uniqId = portalUser;
			if( uniqId != null && uniqId.indexOf("@") > 0 )
				return uniqId.substring( 0, uniqId.indexOf("@") );
			else
				return uniqId;
		}

		public String getSenderID() {
			if( headerMap == null ) return null;
			String senderID = null;

			if( soldPartyCode != null && soldPartyCode.length() > 0 && organizationCode != null & organizationCode.length() > 0 )
				senderID = soldPartyCode + "(" + organizationCode + ")";

			if( senderID != null && ("1000".equals(organizationCode) || "1900".equals(organizationCode)) && "Y".equals(headerMap.get("JDMSIndicator")) )
				senderID += ", CA105_JDMS_China";

			return senderID;
		}

		public String getUpdateUserId() {
			return (String)headerMap.get( "updateUserId" );
		}

		@Override
		public int write( Document document ) throws CanonicalXMLException, SQLException {
			Element rootElement, orderElement;

			this.document = document;

			logger.info( Loggers.STR_BUSINESS + this.simulationKey + ": StockQueryXMLWriter start." );
			try {
				int recordCount = 0;
				document.appendChild( rootElement = document.createElement(ROOT_NODENAME_ORDER) );
				rootElement.setAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
				writeTransportHeader( rootElement );

				rootElement.appendChild( orderElement = document.createElement("Order") );
				writeOrderHeader( orderElement );
				writeOrderPartner( orderElement );
				recordCount = writeOrderItems( orderElement );

	/* WORKING : OrderTexts를 꼭 사용 해야 하는가? */
				writeOrderTexts( orderElement );

				logger.info( Loggers.STR_BUSINESS + this.simulationKey +": StockQueryXMLWriter end.("+ recordCount +" items read)" );

				return recordCount;
			} catch( CanonicalXMLException xmlEx ) {
				logger.error( this.simulationKey +": StockQueryXMLWriter error.", xmlEx );
				handler.rollback();

				throw xmlEx;
			} catch( SQLException sqlEx ) {
				logger.error( this.simulationKey +": StockQueryXMLWriter error.", sqlEx );
				handler.rollback();

				throw sqlEx;
			}
		}

		/**
		 * <OrderHeader/>를 생성한다. readHeader()를 먼저 호출해야 한다.
		 */
		private void writeOrderHeader( Element parentElement ) {
			Element element = document.createElement( "OrderHeader" );
			String documentType;
			documentType = getDocumentCountryCode( organizationCode );

			element.appendChild( createElement("DocumentType", documentType) );
			element.appendChild( createElement("SalesOrganisation", organizationCode) );
			element.appendChild( createElement("RequestedDeliveryDate", headerMap.get("inDate")) );
			element.appendChild( createElement("DateType", DATETYPE_LEN10) );
			element.appendChild( createElement("PurchaseOrderDate", headerMap.get("dateValue")) );
			element.appendChild( createElement("PurchaseOrderNumber", simulationKey ) );
			element.appendChild( createElement("PurchaseOrderMethods", PURCHASEORDER_METHOD_EDI) );
			element.appendChild( createElement("DistributionChannel", headerMap.get("distributionChannelCode")) );
			element.appendChild( createElement("Division", headerMap.get("divisionCode")) );
			element.appendChild( createElement("PurchaserName", getPortalUser()) );
			element.appendChild( createElement("ShipCondition", null) );
			element.appendChild( createElement("Currency", null) );

			logger.info( Loggers.STR_BUSINESS + simulationKey + ": " + this.messageId +": <OrderHeader/> created.('"+ simulationKey +"')" );

			parentElement.appendChild( element );
		}

		/**
		 * <OrderItems/>, <OrderSchedules/>를 생성한다. readHeader()를 먼저 호출해야 한다.
		 */
		public int writeOrderItems( Element parentElement ) throws SQLException {
			int recordCount = 0;
			Element itemElement = document.createElement( "OrderItems" );
			Element schedElement = document.createElement( "OrderSchedules" );

			logger.info( Loggers.STR_BUSINESS + simulationKey + ": " + this.messageId +": <OrderItems/> and <OrderSchedules/> creating start." );

			PreparedStatement pstmt = handler.getConnection().prepareStatement(
				"SELECT ITEMCD, UOM FROM DPR_STOCK_QUERY WHERE SIMULATION_KEY = ? AND STATUS <> 'ER'"
			);
			ResultSet rset = null;
			try {
				int lineNumber = 10;
				Object inDate = headerMap.get( "inDate" );

				pstmt.setString( 1, this.simulationKey );
				rset = pstmt.executeQuery();
				while( rset.next() ) {
					String itemCode = rset.getString( 1 );
					String unitOfMeasure = rset.getString( 2 );

					if( unitOfMeasure == null || unitOfMeasure.length() < 1 ) {
						unitOfMeasure = StockQuery.DEFAULT_SIMULATION_UOM;
					}

					String itemEanCode = null;
					Element element = document.createElement( "Item" );
					element.appendChild( createElement("LineNumber", lineNumber) );
					element.appendChild( createElement("MaterialNumber", itemCode) );
					if( Country.isFeature((String)headerMap.get("organizationCode"), "useOrdCusItemCode") ) {
						element.appendChild( createElement("CustomerItemCode", itemEanCode) );
					}
					element.appendChild( createElement("Quantity", headerMap.get("inputQty")) );
					element.appendChild( createElement("SalesUnit", unitOfMeasure) );
					itemElement.appendChild( element );

					element = document.createElement( "ItemSchedule" );
					element.appendChild( createElement("LineNumber", lineNumber) );
					element.appendChild( createElement("RequestedDeliveryDate", inDate) );
					element.appendChild( createElement("DateType", DATETYPE_LEN10) );
					element.appendChild( createElement("Quantity", headerMap.get("inputQty")) );
					schedElement.appendChild( element );
					recordCount++;

					String[] fieldKeys = new String[] { simulationKey, itemCode, String.valueOf(headerMap.get("inputQty")), unitOfMeasure };
					Number number = Long.valueOf( lineNumber );
					itemMap.put( number, fieldKeys );
					lineNumber += 10;
				}
			} finally {
				try { rset.close(); } catch( Exception ex ) {}
				try { pstmt.close(); } catch( Exception ex ) {}
			}
			logger.info( Loggers.STR_BUSINESS + simulationKey + ": " + this.messageId +": <OrderItems/> and <OrderSchedules/> created.("+ recordCount +" lines)" );

			parentElement.appendChild( itemElement );
			parentElement.appendChild( schedElement );

			return recordCount;
		}

		/**
		 * <OrderPartner/>를 생성한다. readHeader()를 먼저 호출해야 한다.
		 */
		private void writeOrderPartner( Element parentElement ) throws CanonicalXMLException {
			Element element = document.createElement( "OrderPartners" );

			String shipTo = null, soldTo = null;

			soldTo = (String)headerMap.get( "soldPartyCode" );
			shipTo = (String)headerMap.get( "shipPartyCode" );

			Element childElement = document.createElement( "Partner" );
			element.appendChild( childElement );
			childElement.appendChild( createElement("PartnerRole", ROLE_SHIP_TO_PARTY) );
			childElement.appendChild( createElement("PartnerCode", shipTo) );

			element.appendChild( childElement = document.createElement("Partner") );
			childElement.appendChild( createElement("PartnerRole", ROLE_SOLD_TO_PARTY) );
			childElement.appendChild( createElement("PartnerCode", soldTo) );

			logger.info( Loggers.STR_BUSINESS + simulationKey + ": " + this.messageId +": <OrderPartners/> created.("+ shipTo +")" );

			parentElement.appendChild( element );
		}

		/**
		 * <OrderTexts/>를 생성한다. readHeader()를 먼저 호출해야 한다.
		 */
		private void writeOrderTexts( Element parentElement ) {
			Element elementText = document.createElement( "OrderTexts" );
			Element element = document.createElement( "Text" );

			element.appendChild( createElement("LineNumber", "000000") );
			element.appendChild( createElement("Language", "EN") );
			element.appendChild( createElement("TextID", "Z005") );
			element.appendChild( createElement("TextLine", this.receiverId) );

			logger.info( Loggers.STR_BUSINESS + simulationKey + ": " + this.messageId +": <OrderTexts/> created.('"+ receiverId +"')" );

			elementText.appendChild( element );
			parentElement.appendChild( elementText );
		}

		/**
		 * <TransportHeader/>를 생성한다. readHeader()를 먼저 호출해야 한다.
		 */
		private void writeTransportHeader( Element parentElement ) {
			Element element = document.createElement( "TransportHeader" );

			element.appendChild( createElement("CanonicalName", ROOT_NODENAME_ORDER) );
			element.appendChild( createElement("CanonicalVersion", "v1.0") );
			element.appendChild( createElement("BusinessManagerID", simulationKey ) );
			element.appendChild( createElement("SenderID", getSenderID() ) );
			element.appendChild( createElement("ReceiverID", WM_JNJID ) );
			element.appendChild( createElement("EDIDocumentType", DOCUMENT_TYPE_ORDER) );

			logger.info( Loggers.STR_BUSINESS + simulationKey + ": " + this.messageId + ": <TransportHeader/> created.("+ getSenderID() +" -> "+ WM_JNJID +")" );

			parentElement.appendChild( element );
		}


	}
}
