/*
 *	File Name:	OrderCanonicalXMLWriter.java
 *	Version:	2.2.10(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2019/07/31		2.2.10	Freegoods 발주 기능 추가
 *	jbaek		2019/07/30		2.2.9	PackDeal 기능 추가, CustomerItemCode 추가
 *	jbaek		2018/10/30		2.2.8	isChinaCountry() 삭제
 *	hankalam	2018/06/18		2.2.7	China Country 시에 DeliveryBlock 빈값으로 변경
 *	jbaek		2013/04/30		2.2.6	모든 org DeliveryBlock 사용으로 변경.
 *	jbaek		2013/01/30		2.2.5	PIPO 기능  개발, LineItemStatus 기능  개발
 *	jbaek		2011/08/31		2.2.4	Sales Org가 JJC이거나 SJJ일 경우에 DeliveryBlock Field값을 Blank로 처리
 *	jbaek		2011/07/31		2.2.3	China일 때만 DeliveryBlock Field값을 Blank로 처리
 *	lsinji		2010/12/31		2.2.2	SJJP 추가
 *	lsinji		2009/06/30		2.2.1	China Sender ID 변경. (Thailand는 JDMSIndicator가 'Y'라도 SenderID에 변화 없음)
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.irt.dpr.Country;
import com.irt.dpr.util.Loggers;
import com.irt.sql.SQLHandler;

/**
 *
 */
public class OrderCanonicalXMLWriter extends CanonicalXMLWriter {
	public static final String WM_JNJID								= "JNJ";
	public static final String DELIVERYBLOCK_DEFAULT				= "02";
	public static final String DELIVERYBLOCK_FREEGOODS				= "16";
	public static final String DELIVERYBLOCK_PORTAL					= "10";
	public static final String COUNTRYCODE_CHINA					= "100053";

	Map<String, Object> headerMap;
	Map<Number, String[]> itemMap;
	String orderKey, orderNumber;
	String orderType;
	String organizationCode, soldPartyCode, portalUser;
	String documentCountryCode;

	public OrderCanonicalXMLWriter( SQLHandler handler, String orderKey ) {
		super( handler, Logger.getLogger(CanonicalXMLWriter.class) );

		this.orderKey = orderKey;
		this.orderType = OrderCanonicalProcess.ORDER_IF_SIMULATION;
		super.receiverId = CanonicalXML.DEFAULT_ORDER_JNJID;
		headerMap = new java.util.HashMap<String, Object>();
		itemMap = new java.util.HashMap<Number, String[]>();
	}

	public OrderCanonicalXMLWriter( SQLHandler handler, String orderKey, String orderType ) {
		super( handler, Logger.getLogger(CanonicalXMLWriter.class) );

		this.orderKey = orderKey;
		this.orderType = orderType;
		super.receiverId = CanonicalXML.DEFAULT_ORDER_JNJID;
		headerMap = new java.util.HashMap<String, Object>();
		itemMap = new java.util.HashMap<Number, String[]>();
	}

	public OrderCanonicalXMLWriter( SQLHandler handler, String orderKey, Logger logger ) {
		super( handler, logger );

		this.orderKey = orderKey;
		this.orderType = OrderCanonicalProcess.ORDER_IF_SIMULATION;
		super.receiverId = CanonicalXML.DEFAULT_ORDER_JNJID;
		headerMap = new java.util.HashMap<String, Object>();
		itemMap = new java.util.HashMap<Number, String[]>();
	}

	public OrderCanonicalXMLWriter( SQLHandler handler, String orderKey, String orderType, Logger logger ) {
		super( handler, logger );

		this.orderKey = orderKey;
		this.orderType = orderType;
		super.receiverId = CanonicalXML.DEFAULT_ORDER_JNJID;
		headerMap = new java.util.HashMap<String, Object>();
		itemMap = new java.util.HashMap<Number, String[]>();
	}

	@Override
	public java.util.Date getDocumentDate() {
		if( headerMap != null )
			return (java.util.Date)headerMap.get( "orderDate" );
		else
			return null;
	}

	public String getBusinessManagerID() {
		return orderKey;
	}


	public Map<String, Object> getHeaderMap() {
		return new java.util.HashMap<String, Object> ( headerMap );
	}

	public Map<Number, String[]> getDetailMap() {
		return itemMap;
	}

	public String getSenderID() {
		if( headerMap == null ) return null;
		String senderID = null;

		if( soldPartyCode == null )
			soldPartyCode = (String)headerMap.get( "soldPartyCode" );
		if( organizationCode == null )
			organizationCode = (String)headerMap.get( "organizationCode" );

		if( soldPartyCode != null && soldPartyCode.length() > 0 && organizationCode != null & organizationCode.length() > 0 )
			senderID = soldPartyCode + "(" + organizationCode + ")";

		if( senderID != null && ("1000".equals(organizationCode) || "1900".equals(organizationCode)) && "Y".equals(headerMap.get("JDMSIndicator")) )
			senderID += ", CA105_JDMS_China";

		return senderID;
	}

	public String getOrganizationCode() {
		return organizationCode;
	}

	public String getSoldPartyCode() {
		return soldPartyCode;
	}

	public String getPortalUser() {
		return getPortalUser( portalUser );
	}

	public String getPortalUser( String uniqId ) {
		if( uniqId == null )
			uniqId = portalUser;

		if( uniqId != null && uniqId.indexOf("@") > 0 ) return uniqId.substring( 0, uniqId.indexOf("@") );
		else return uniqId;
	}

	public String getOrderNumber() {
		if( headerMap == null ) return null;

		String purchaseOrderNumber = (String)headerMap.get( "purchaseOrderNumber" );
		if( purchaseOrderNumber != null && purchaseOrderNumber.length() > 0 )
			return purchaseOrderNumber;

		String customerOrderNumber = (String)headerMap.get( "customerOrderNumber" );
		if( customerOrderNumber != null && customerOrderNumber.length() > 0 )
			this.orderNumber = purchaseOrderNumber = customerOrderNumber;
		else {
			String simulationKey = null;
			try {
				simulationKey = new com.irt.dpr.Order(handler).getSimulationKey( this.orderKey, false );
			} catch( SQLException sqlEx ) {
				logger.error( Loggers.STR_BUSINESS + orderKey + ": " + "Can't create simulation key with sequence", sqlEx );
			}

			if( simulationKey != null && simulationKey.length() > 0 ) {
				if( "Y".equals(headerMap.get("isPackdealOrder")) ) {
					simulationKey = simulationKey.replaceAll("^DP", "PACKDL");
					headerMap.put("customerOrderNumber", simulationKey);
				}

				this.orderNumber = purchaseOrderNumber = simulationKey;
			} else
				this.orderNumber = purchaseOrderNumber = orderKey;
		}

		headerMap.put( "purchaseOrderNumber", purchaseOrderNumber );

		return purchaseOrderNumber;
	}

	public String getUpdateUserId() {
		return (String)headerMap.get( "updateUserId" );
	}

	private boolean lockOrder( String orderKey ) throws SQLException {
		return new com.irt.dpr.Order(handler).lockOrder( orderKey );
	}

	private Map<String, Object> getOrderHeader( String orderKey ) throws SQLException {
		return new com.irt.dpr.Order(handler).getRecord( com.irt.dpr.Order.createPrimary(orderKey) );
	}

	@Override
	public int write( Document document ) throws CanonicalXMLException, SQLException {
		Element rootElement, orderElement;

		this.document = document;

		logger.info( Loggers.STR_BUSINESS + this.orderKey + ": OrderCanonicalXMLWriter start." );
		try {
			boolean result = lockOrder( orderKey );
			this.headerMap = new java.util.HashMap<String, Object>();
			if( result )
				headerMap = getOrderHeader( orderKey );
			else
				throw new CanonicalXMLException( "Can't lock on DPR_ORDER Table" );

			setParameter( headerMap );

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

			logger.info( Loggers.STR_BUSINESS + this.orderKey +": OrderCanonicalWriter end.("+ recordCount +" items read)" );

			return recordCount;
		} catch( CanonicalXMLException xmlEx ) {
			logger.error( this.orderKey +": OrderCanonicalWriter error.", xmlEx );
			handler.rollback();

			throw xmlEx;
		} catch( SQLException sqlEx ) {
			logger.error( this.orderKey +": OrderCanonicalWriter error.", sqlEx );
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
		if( "Y".equals(headerMap.get("freegoodsOrderInd")) ) {
			documentType = getFreegoodsDocumentCountryCode( (String)headerMap.get("organizationCode") );
		} else {
			documentType = getDocumentCountryCode( (String)headerMap.get("organizationCode") );
		}
		element.appendChild( createElement("DocumentType", documentType) );
		element.appendChild( createElement("SalesOrganisation", headerMap.get("organizationCode")) );

		Object indate = null;
		if( OrderCanonicalProcess.ORDER_IF_SIMULATION.equals(orderType) ) {
			indate = headerMap.get( "inDate" );
			if( indate == null )
				indate = headerMap.get( "inDateDefault" );
		} else if( OrderCanonicalProcess.ORDER_IF_CREATION.equals(orderType) ) {
			indate = headerMap.get( "inDate" );
			if( indate == null )
				indate = headerMap.get( "inDateDefault" );
		}
		element.appendChild( createElement("RequestedDeliveryDate", indate) );

		element.appendChild( createElement("DateType", DATETYPE_LEN10) );
		element.appendChild( createElement("PurchaseOrderDate", headerMap.get("orderDate")) );
		element.appendChild( createElement("PurchaseOrderNumber", getOrderNumber() ) );
		element.appendChild( createElement("PurchaseOrderMethods", PURCHASEORDER_METHOD_EDI) );
		element.appendChild( createElement("DistributionChannel", headerMap.get("distributionChannelCode")) );
		element.appendChild( createElement("Division", headerMap.get("divisionCode")) );
		if( OrderCanonicalProcess.ORDER_IF_CREATION.equals(orderType) ) {
			Object organizationCode = headerMap.get("organizationCode");

			if( com.irt.dpr.Country.isFeature((String)organizationCode, "useFreegoods") && "Y".equals(headerMap.get("freegoodsOrderInd")) ) {
				element.appendChild( createElement("DeliveryBlock", DELIVERYBLOCK_FREEGOODS) );
			} else if( com.irt.dpr.Country.isFeature((String)organizationCode, "useDeliveryBlock") )
				element.appendChild( createElement("DeliveryBlock", DELIVERYBLOCK_PORTAL) );
			else
				element.appendChild( createElement("DeliveryBlock", "") );

		}
		element.appendChild( createElement("PurchaserName", getPortalUser((String)headerMap.get("updateUserId")) ) );
		element.appendChild( createElement("ShipCondition", null) );
		element.appendChild( createElement("Currency", null) );

		logger.info( Loggers.STR_BUSINESS + orderKey + ": " + this.messageId +": <OrderHeader/> created.('"+ getOrderNumber() +"')" );

		parentElement.appendChild( element );
	}

	/**
	 * <OrderItems/>, <OrderSchedules/>를 생성한다. readHeader()를 먼저 호출해야 한다.
	 */
	public int writeOrderItems( Element parentElement ) throws SQLException {
		int recordCount = 0;
		Element itemElement = document.createElement( "OrderItems" );
		Element schedElement = document.createElement( "OrderSchedules" );

		logger.info( Loggers.STR_BUSINESS + orderKey + ": " + this.messageId +": <OrderItems/> and <OrderSchedules/> creating start." );

		PreparedStatement pstmt = handler.getConnection().prepareStatement(
			"SELECT ORDD.LINE_NO, ORDD.ITEMCD, ORDD.ITEMCD_CNF, ORDD.ITEMREF_IND, ORDD.CHILD_LINE_NO"
				+ "	, ORDD.ORDERQTY, ORDD.INPUT_TOTAL_QTY, ORDD.SIMULATION_ORDERQTY, ORDD.UOM, MST.SALES_UNIT, IMEAN.ITEM_CONS_EAN, ORDD.FREEGOODS_IND"
				+ " FROM vwDPR_ORDER ORDD, DPR_ITEM_MASTER MST, vwDPR_ITEM_EANRLT IMEAN"
				+ " WHERE MST.ITEM_CD(+) = ORDD.ITEMCD AND ORDD.ORDERKEY = ?"
				+ " AND IMEAN.ORGANIZATIONCD(+) = ORDD.ORGANIZATIONCD"
				+ " AND IMEAN.ITEMCD(+) = ORDD.ITEMCD"
				+ " AND ORDD.LINE_NO IS NOT NULL"
				+ " ORDER BY 1"
		);
		ResultSet rset = null;
		try {
			Object inDate = headerMap.get( "inDate" );

			pstmt.setString( 1, this.orderKey );
			rset = pstmt.executeQuery();
			while( rset.next() ) {
				String lineNumber = rset.getString( 1 );
				String itemCode = rset.getString( 2 );
				String itemCodeConfirmed = rset.getString( 3 );
				String itemRefInd = rset.getString( 4 );
				String childLineNumber = rset.getString( 5 );
				String unitOfMeasure = rset.getString( 9 );
				String salesUnit = rset.getString( 10 );
				String itemEanCode = rset.getString( 11 );
				String orderQty = null;
				String inputOrderQty = null;
				String freegoodsInd = rset.getString( 12 );

				if( OrderCanonicalProcess.ORDER_IF_SIMULATION.equals(orderType) ) {
					orderQty = "Y".equals(freegoodsInd) ? rset.getString( 7 ) : rset.getString( 6 );
					inputOrderQty = rset.getString( 6 );
				} else if( OrderCanonicalProcess.ORDER_IF_CREATION.equals(orderType) ) {
					orderQty = rset.getString( 6 );
					inputOrderQty = rset.getString( 6 );
				}

				if( orderQty != null && unitOfMeasure != null
						&& com.irt.dpr.OrderDetail.CHILD_LINENUMBER_NORMAL == Integer.parseInt(childLineNumber) ) {
					Element element = document.createElement( "Item" );
					element.appendChild( createElement("LineNumber", lineNumber) );
					element.appendChild( createElement("MaterialNumber", itemCode) );
					if( Country.isFeature((String)headerMap.get("organizationCode"), "useOrdCusItemCode") ) {
						element.appendChild( createElement("CustomerItemCode", itemEanCode) );
					}
					element.appendChild( createElement("Quantity", orderQty) );
					element.appendChild( createElement("SalesUnit", (unitOfMeasure != null ? unitOfMeasure : salesUnit) ) );
					itemElement.appendChild( element );

					element = document.createElement( "ItemSchedule" );
					element.appendChild( createElement("LineNumber", lineNumber) );
					element.appendChild( createElement("RequestedDeliveryDate", inDate) );
					element.appendChild( createElement("DateType", DATETYPE_LEN10) );
					element.appendChild( createElement("Quantity", orderQty) );
					schedElement.appendChild( element );
					recordCount++;

					String[] fieldKeys = new String[] { orderKey, itemCode, itemCodeConfirmed, itemRefInd, childLineNumber, orderQty, inputOrderQty, unitOfMeasure, salesUnit, freegoodsInd };

					Number number = Long.valueOf( lineNumber );
					itemMap.put( number, fieldKeys );
				}
			}
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
		logger.info( Loggers.STR_BUSINESS + orderKey + ": " + this.messageId +": <OrderItems/> and <OrderSchedules/> created.("+ recordCount +" lines)" );

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

		logger.info( Loggers.STR_BUSINESS + orderKey + ": " + this.messageId +": <OrderPartners/> created.("+ shipTo +")" );

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

		logger.info( Loggers.STR_BUSINESS + orderKey + ": " + this.messageId +": <OrderTexts/> created.('"+ receiverId +"')" );

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
		element.appendChild( createElement("BusinessManagerID", getOrderNumber() ) );
		element.appendChild( createElement("SenderID", getSenderID() ) );
		element.appendChild( createElement("ReceiverID", WM_JNJID ) );
		element.appendChild( createElement("EDIDocumentType", DOCUMENT_TYPE_ORDER) );

		logger.info( Loggers.STR_BUSINESS + orderKey + ": " + this.messageId + ": <TransportHeader/> created.("+ getSenderID() +" -> "+ WM_JNJID +")" );

		parentElement.appendChild( element );
	}

	public static void writeXMLDocument( OutputStreamWriter outputStream, String orderKey, Logger logger ) {
		com.irt.system.SystemConfig systemConfig = com.irt.custom.SystemConfig.getInstance( "RBM" );
		com.irt.util.MessageHandler msghandler = systemConfig.getMessageHandler();

		javax.xml.parsers.DocumentBuilder builder;
		javax.xml.transform.Transformer transformer;
		try {
			builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
			transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty( "encoding", "UTF-8" );
		} catch( javax.xml.parsers.ParserConfigurationException parserEx ) {
			logger.error( orderKey +": " + "OrderCanonicalXMLWriter sending error.", parserEx );
			return;
		} catch( javax.xml.transform.TransformerConfigurationException transEx ) {
			logger.error( orderKey + ": " + "OrderCanonicalXMLWriter sending error.", transEx );
			return;
		}

		Document document = builder.newDocument();
		try {
			OrderCanonicalXMLWriter writer = new OrderCanonicalXMLWriter( systemConfig.createSQLHandler(msghandler), orderKey, logger );

			writer.write( document );
		} catch( SQLException sqlEx ) {
		} catch( CanonicalXMLException xmlEx ) {
		}
	}

	public void setParameter( Map<String, Object> headerMap ) {
		organizationCode = (String)headerMap.get( "organizationCode" );
		soldPartyCode = (String)headerMap.get( "soldPartyCode" );
		portalUser = (String)headerMap.get( "updateUserId" );
	}
}
