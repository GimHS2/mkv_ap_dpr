/*
 *	File Name:	OrderCanonicalXMLReader.java
 *	Version:	2.2.3(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/10/31		2.2.3	readHeader() : orderNumber의 값을 앞에 0 제거하도록 수정
 *	GimHS		2025/09/30		2.2.2	BTPi interface(REST) 적용
 *	jbaek		2019/07/30		2.2.1	PCK salesUnit 추가
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.sql.SQLHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class OrderCanonicalXMLReader extends CanonicalXMLReader {
	String orderKey, orderNumber;

	Map<String, Object> headerMap;
	List<Map<String, Object>> itemList;
	Map<Integer, String[]> itemsMap;

	public OrderCanonicalXMLReader( SQLHandler handler, Logger logger ) {
		super( handler, logger );
		this.orderKey = null;

		headerMap = new java.util.HashMap<String, Object>();
		itemList = new java.util.ArrayList<Map<String, Object>>();
		itemsMap = new java.util.HashMap<Integer, String[]>();
	}

	public OrderCanonicalXMLReader( SQLHandler handler, Logger logger, String orderKey ) {
		super( handler, logger );
		this.orderKey = orderKey;

		headerMap = new java.util.HashMap<String, Object>();
		itemList = new java.util.ArrayList<Map<String, Object>>();
		itemsMap = new java.util.HashMap<Integer, String[]>();
	}

	@Override
	public java.util.Date getDocumentDate() {
		return (com.irt.data.Date)headerMap.get("orderDate");
	}

	public com.irt.data.Date getOrderDate() {
		return (com.irt.data.Date)headerMap.get("orderDate");
	}

	public String getOrderKey() {
		return orderKey;
	}

	public String getOrderNumber() {
		return (String)headerMap.get("orderNumber");
	}

	public Object[] getStoredMap() {
		return new Object[] { headerMap, itemList };
	}

	public String getSalesOrderNumber() {
		return (String)headerMap.get("salesOrderNumber");
	}

	@Override
	public java.net.URL getSchemaURL() {
		ClassLoader loader = CanonicalXMLReader.class.getClassLoader();
		return loader.getResource( "com/irt/dpr/tools/PPO2Canonical.xsd" );
	}

	@Deprecated
	public void read( Document document, String startTagName ) throws CanonicalXMLException {
		try {
			logger.info( "OrderCanonicalXMLReader start." );

			readHeader( document );
			readItem( document );
			readLine( document );

			logger.info( "OrderCanonicalXMLReader end." );
		} catch( CanonicalXMLException xmlEx ) {
			this.resultStatus = "ER";
			this.resultMessage = xmlEx.getMessage();
			logger.error( "OrderCanonicalXMLReader error.", xmlEx );
			throw xmlEx;
		}
	}

	@Deprecated
	public void read( Document document ) throws CanonicalXMLException {
		try {
			logger.info( "OrderCanonicalXMLReader start." );

			readHeader( document );
			readItem( document );
			readLine( document );

			logger.info( "OrderCanonicalXMLReader end." );
		} catch( CanonicalXMLException xmlEx ) {
			this.resultStatus = "ER";
			this.resultMessage = xmlEx.getMessage();
			logger.error( "OrderCanonicalXMLReader error.", xmlEx );
			throw xmlEx;
		}
	}

	@Override
	public int read() throws CanonicalXMLException {
		try {
			logger.info( "OrderCanonicalXMLReader start." );
			this.messageId = createMessageId();
			logger.info( this.messageId +": The message ID created." );

			readHeader();
			readItem();
			int recordCount = readLine();

			this.resultStatus = "CP";
			this.resultMessage = recordCount +" rows completed.("+ headerMap.get("orderDate") +")";
			logger.info( "OrderCanonicalXMLReader end." );

			return recordCount;
		} catch( CanonicalXMLException xmlEx ) {
			this.resultStatus = "ER";
			this.resultMessage = xmlEx.getMessage();
			logger.error( "OrderCanonicalXMLReader error.", xmlEx );

			throw xmlEx;
		} catch( SQLException sqlEx ) {
			this.resultStatus = "ER";
			this.resultMessage = sqlEx.getMessage();
			logger.error( "OrderCanonicalXMLReader error.", sqlEx );

			throw new CanonicalXMLException(sqlEx);
		}
	}

	/**
	 *	<TransportHeader/>, <OrderHeader/>를 읽는다.
	 */
	public void readHeader() throws CanonicalXMLException {
		readHeader( document );
	}

	public void readHeader( Document document ) throws CanonicalXMLException {
		Node node;

		logger.info( this.messageId +": <TransportHeader/> and <OrderHeader/> reading start." );

		// TransportHeader: documentType, documentId, senderId, receiverId
		node = document.getElementsByTagName("TransportHeader").item(0);
		headerMap.put( "documentType", getChildNodeTextValue(node, "EDIDocumentType") );
		headerMap.put( "documentId", getChildNodeTextValue(node, "BusinessManagerID") );
		headerMap.put( "senderId", getChildNodeTextValue(node, "SenderID") );
		headerMap.put( "receiverId", getChildNodeTextValue(node, "ReceiverID") );
		headerMap.put( "orderKey", orderKey );

		// OrderHeader: orderNumber, salesOrderNumber, orderDate
		node = document.getElementsByTagName("OrderHeader").item(0);
		headerMap.put( "orderNumber", this.orderNumber = getChildNodeTextValueTrim(node, "PurchaseOrderNumber") );

		String dateType = getChildNodeTextValue( node, "DateType" );
		headerMap.put( "orderDate", getChildNodeTextValue(node, "PurchaseOrderDate") );
		headerMap.put( "inDate", getChildNodeTextValue(node, "RequestedDeliveryDate") );
		headerMap.put( "organizationCode", getChildNodeTextValue(node, "SalesOrganisation") );
		headerMap.put( "divisionCode", getChildNodeTextValue(node, "Division") );
		headerMap.put( "distributionChannelCode", getChildNodeTextValue(node, "DistributionChannel") );


		NodeList nodeList = document.getElementsByTagName("OrderPartners").item(0).getChildNodes();
		for( int n = 0; n < nodeList.getLength(); n++ ) {
			if( !"item".equals(nodeList.item(n).getNodeName()) )
				continue;

			Node childeNode = nodeList.item ( n );
			String partnerRole = getChildNodeTextValue( childeNode, "PARTN_ROLE" );
			if( "WE".equals(partnerRole) || "SH".equals(partnerRole) )
				headerMap.put( "shipPartyCode", getChildNodeTextValue(childeNode, "PARTN_NUMB") );
			else if( "AG".equals(partnerRole) || "SP".equals(partnerRole) )
				headerMap.put( "soldPartyCode", getChildNodeTextValue(childeNode, "PARTN_NUMB") );
		}

		logger.info( this.messageId +": <TransportHeader/> and <OrderHeader/> read." );
		logger.info( this.messageId +": "+ senderId +" -> "+ receiverId +" (OrderNumber: "+ this.orderNumber +")" );
	}

	/**
	 *	<Item/>을 읽는다. readHeader()를 먼저 호출해야 한다.
	 */
	public Map<Integer, String[]> readItem() throws CanonicalXMLException {
		return readItem( document );
	}

	public Map<Integer, String[]> readItem( Document document ) throws CanonicalXMLException {
		if( headerMap == null || headerMap.size() == 0 ) return this.itemsMap;


		Map<Integer, String[]> map = new java.util.HashMap<Integer, String[]>();
		logger.info( this.messageId +": <Item/> reading start." );

		NodeList nodeList = document.getElementsByTagName( "Item" );
		for( int n = 0; n < nodeList.getLength(); n++ ) {

			Node childNode = nodeList.item( n );

			Integer lineNumber = getChildNodeTextValueInt( childNode, "LineNumber" );
			String materialNumber = getChildNodeTextValue( childNode, "MaterialNumber" );
			if( materialNumber == null ) {
				throw new CanonicalXMLException( "cannot find item number for '"+ lineNumber +"'." );
			}

			String salesUnit = getChildNodeTextValue( childNode, "SalesUnit" );
			if( salesUnit == null )
				throw new CanonicalXMLException( "cannot find sales unit for '"+ lineNumber +"'." );
			else if( "CS".equals(salesUnit) || "CSE".equals(salesUnit) )
				salesUnit = "CSE";
			else if( "DZ".equals(salesUnit) || "DZN".equals(salesUnit) )
				salesUnit = "DZ";
			else if( "PC".equals(salesUnit) || "PCE".equals(salesUnit) )
				salesUnit = "PC";
			else if( "PCK".equals(salesUnit) )
				salesUnit = "PCK";
			else
				throw new CanonicalXMLException( "illegal sales unit '"+ salesUnit +"' for '"+ lineNumber +"'." );

			String unitOfMeasure = getChildNodeTextValue( childNode, "UnitOfMeasure" );

			map.put( lineNumber, new String[] { materialNumber, salesUnit, unitOfMeasure } ) ;
		}
		logger.info( this.messageId +": <Item/> read.("+ map.size() +" items)" );

		return ( this.itemsMap = map );
	}

	/**
	 *	<ItemSchedule/>을 읽는다. readHeader(), readItem()를 먼저 호출해야 한다.
	 */
	public int readLine() throws CanonicalXMLException {
		return readLine( document );
	}

	public int readLine( Document document ) throws CanonicalXMLException {
		logger.info( this.messageId +": <ItemSchedule/> reading start." );

		NodeList nodeList = document.getElementsByTagName( "ItemSchedule" );
		if( nodeList == null ) {
			logger.info( this.messageId +": <ItemSchedule/> read.(0 lines)" );
			return 0;
		}

		for( int n = 0; n < nodeList.getLength(); n++ ) {
			Map<String, Object> map = new java.util.HashMap<String, Object>();
			Node childNode = nodeList.item(n);

			Integer lineNumber = getChildNodeTextValueInt( childNode, "LineNumber" );
			Integer orderQty = getChildNodeTextValueInt( childNode, "Quantity" );
			String[] itemValues = itemsMap.get( lineNumber );
			if( itemValues == null )
				throw new CanonicalXMLException( "cannot find item number for '"+ lineNumber +"'." );
			String itemCode = itemValues[0];

			map.put( "lineNumber", lineNumber );
			map.put( "orderQty", orderQty );
			map.put( "itemCode", itemCode );
			map.put( "salesUnit", itemValues[1] );
			map.put( "uom", itemValues[2] );

			itemList.add( map );
		}
		logger.info( this.messageId +": <ItemSchedule/> read.("+ nodeList.getLength() +" lines)" );

		return nodeList.getLength();
	}
}
