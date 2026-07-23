/*
 *	File Name:	OrderCreationReader.java
 *	Version:	2.2.5(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2026/05/29		2.2.6	getErrorMap(): 에러 메시지 가져오는 로직 수정
 *	dudwls3720	2025/12/31		2.2.5	read() : 오더 번호가 0이거나 Null일 때 CanonicalXMLException 발생 시 Response XML에 있는 Text 태그 메시지를 갖고 오도록 수정
 *	dudwls3720	2025/10/31		2.2.4	read() : Number 캐스팅을 Double.valueOf(String.valueOf())로 변경하여 캐시팅 오류 안나도록 수정, salesOrderNumber의 값에 앞에 0 제거되도록 수정
 *	GimHS		2025/09/30		2.2.3	BTPi interface(REST) 적용
 *	hankalam	2019/07/31		2.2.2	Freegoods 기능 추가
 *	jbaek		2019/07/30		2.2.1	sendOrderMail 환경변수에 따라 call하도록 변경.
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.sql.SQLHandler;
import com.irt.data.Record;
import com.irt.dpr.Country;
import com.irt.dpr.util.Loggers;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class OrderCreationReader extends CanonicalXMLReader {
	String orderKey, orderNumber;

	Map<String, Object> headerMap;

	public OrderCreationReader( SQLHandler handler, Logger logger ) {
		super( handler, logger );
		this.orderKey = null;

		headerMap = new java.util.HashMap<String, Object>();
	}

	public OrderCreationReader( SQLHandler hanlder, Logger logger, String orderKey ) {
		super( hanlder, logger );
		this.orderKey = orderKey;

		headerMap = new java.util.HashMap<String, Object>();
	}

	private void writeCreationResult( String outputXML ) {
		OrderLogging orderLogger = new OrderLogging( logger, null );

		java.io.File file = orderLogger.getFile( OrderCanonicalProcess.ORDER_IF_CREATION, "RES_"+ getOrderKey() );
		logger.debug( "XML file name : "+ file.getAbsolutePath() +"("+ file.getName() +")" );
		this.document = orderLogger.makeOrderTraceFile( file, outputXML );
	}

	public Map<String, Object> getDataMap() {
		Map<String, Object> output = new java.util.HashMap<String, Object>();

		output.put( OrderCanonicalProcess.WM_PARAMS, OrderCanonicalProcess.WM_PARAMS_CREATION );

		output.put( "SALES_ORDER", OrderXMLUtility.getTagValue(this.document, "OrderPlacementResponse", "SALES_ORDER") );
		output.put( "ORDER_VALUE", OrderXMLUtility.getTagValue(this.document, "OrderPlacementResponse", "ORDER_VALUE", isFractionCorrection) );
		output.put( "ORDER_VOLUME", OrderXMLUtility.getTagValue(this.document, "OrderPlacementResponse", "ORDER_VOLUME") );
		output.put( "ORDER_WEIGHT", OrderXMLUtility.getTagValue(this.document, "OrderPlacementResponse", "ORDER_WEIGHT") );
		output.put( "NET_VALUE", OrderXMLUtility.getTagValue(this.document, "OrderPlacementResponse", "NET_VALUE", isFractionCorrection) );
		output.put( "CONFIRMED_VAT", OrderXMLUtility.getTagValue(this.document, "OrderPlacementResponse", "CONFIRMED_VAT", isFractionCorrection) );
		output.put( "CONFIRMED_DAMAGEDISCOUNT", OrderXMLUtility.getTagValue(this.document, "OrderPlacementResponse", "CONFIRMED_DAMAGEDISCOUNT", isFractionCorrection) );
		output.put( "CREDIT_STATUS", OrderXMLUtility.getTagValue(this.document, "OrderPlacementResponse", "CREDIT_STATUS") );

		return output;
	}

	@Override
	public java.util.Date getDocumentDate() {
		return (com.irt.data.Date)headerMap.get("orderDate");
	}

	public Map<String, Object> getErrorMap() throws CanonicalXMLException {
		NodeList nodeList = this.document.getElementsByTagName( "ORDER_CREATE_RESPONSE" );
		String errorXMLString = null;
		if( nodeList != null )
			errorXMLString = nodeList.item(0).getTextContent();

		Map<String, Object> errorMap = new java.util.HashMap<String, Object>();
		if( errorXMLString == null ) {
			throw new CanonicalXMLException( "Order Creation failed. [No error message]" );
		}
		errorXMLString = errorXMLString.replaceFirst( "utf-16", "utf-8" );

		Document document = null;
		try {
			try {
				document = OrderXMLUtility.stringConvertDoc( errorXMLString, logger );
			} catch( CanonicalXMLException ex ) {
				throw new CanonicalXMLException( "Order Creation failed. [" + ex.getMessage() + "]" + errorXMLString );
			}

			List<Map<String, Object>> lineList = OrderXMLUtility.getTagValueList( document, "Collection", "item" );
			if( lineList != null ) {
				for( Map<String, Object> map : lineList ) {
					Map<String, Object> messageMap = (Map<String, Object>)map.get( "Message" );
					String id = (String)messageMap.get( "ID" );
					String number = (String)messageMap.get( "Number" );
					String text = (String)messageMap.get("Text");
					messageMap.put("Text", "SAP: " + text);
					if( "V1".equals(id) && "118".equals(number) )
						return messageMap;
				}

				if( lineList.size() > 0 )
					return (Map<String, Object>)lineList.get(0).get( "Message" );
			} else {
				nodeList = document.getElementsByTagName( "biztalk_1" );
				if( nodeList == null || nodeList.getLength() == 0 )
					throw new CanonicalXMLException( "Order Creation failed. [No error message]" );

				Node node = nodeList.item(0);
				if( node != null ) {
					Node childNode = getChildNode( node, "body", true );
					if( childNode != null )
						childNode = getChildNode( childNode, "Message" );
					if( childNode != null )
						return OrderXMLUtility.getTagValueMap( childNode );
				}
			}

			throw new CanonicalXMLException( "Order Creation failed. [No error message]" );
		} catch( CanonicalXMLException xmlEx ) {
			throw new CanonicalXMLException( xmlEx );
		}
	}

	@Override
	public java.net.URL getSchemaURL() {
		return null;
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

	public String getPortalUser( String uniqId ) {
		if( uniqId != null && uniqId.indexOf("@") > 0 ) return uniqId.substring( 0, uniqId.indexOf("@") );
		else return uniqId;
	}

	public Object[] getStoredMap() {
		return new Object[] { headerMap };
	}

	@Override
	public int read() throws CanonicalXMLException {
		return -1;
	}

	public void read( String outputXML, Map<String, Object>infoMap ) throws CanonicalXMLException {
		logger.info( Loggers.STR_BUSINESS + orderKey + ": " + "OrderCreationReader Start." );

		String organizationCode;
		headerMap.put( "organizationCode", organizationCode = Record.extractString(infoMap, "organizationCode") );
		configMoneyFractionCorrection(organizationCode);

		writeCreationResult( outputXML );
		Map<String, Object> dataMap = getDataMap();

		headerMap.put( "orderKey", infoMap.get("orderKey") );
		headerMap.put( "soldPartyCode", infoMap.get("soldPartyCode") );
		headerMap.put( "shipPartyCode", infoMap.get("shipPartyCode") );
		headerMap.put( "divisionCode", Record.extractString(infoMap, "divisionCode") );
		headerMap.put( "distributionChannelCode", Record.extractString(infoMap, "distributionChannelCode") );
		headerMap.put( "updateUserId", infoMap.get("updateUserId") );

		String salesOrderNumber = String.valueOf( dataMap.get("SALES_ORDER") );
		String status = (String)dataMap.get( "U_STATUS" );

		boolean isSuccess = true;
		if( salesOrderNumber == null || salesOrderNumber.length() == 0 || "0".equals( salesOrderNumber ) || "0000000000".equals( salesOrderNumber ) )
			isSuccess = false;

		if( isSuccess ) {
			/* Calcaultion 'confirmedNetAmount' */
			Number _orderValue;
			Number _confirmedOrderValue, _confirmedOrderTax, _confirmedOrderDiscount, _confirmedTotalValue;
			_orderValue = null;
			_confirmedOrderValue = _confirmedOrderTax = _confirmedOrderDiscount = _confirmedTotalValue = null;

			_confirmedOrderValue = Double.valueOf(String.valueOf(dataMap.get("NET_VALUE")));
			_confirmedOrderTax = Double.valueOf(String.valueOf(dataMap.get("CONFIRMED_VAT")));
			_confirmedOrderDiscount = Double.valueOf(String.valueOf(dataMap.get("CONFIRMED_DAMAGEDISCOUNT")));
			_confirmedTotalValue = Double.valueOf(String.valueOf(dataMap.get("ORDER_VALUE")));

			salesOrderNumber = salesOrderNumber.replaceFirst( "^0+", "" );

			Map<String, Object> orderInfo = null;
			try {
				orderInfo = new com.irt.dpr.Order(handler).getRecord(com.irt.dpr.Order.createPrimary(orderKey));
			} catch( Exception ex ) {
				logger.error( ex );
			}

			if( orderInfo != null && orderInfo.size() > 0 ) {
				_orderValue = (Number)orderInfo.get( "orderValue" );

				/* inDate, soldPartyName, shipPartyName */
				headerMap.put( "inDate", orderInfo.get("inDate") );
				headerMap.put( "soldPartyName", orderInfo.get("soldPartyName") );
				headerMap.put( "shipPartyName", orderInfo.get("shipPartyName") );
			}

			headerMap.put( "orderNumber", salesOrderNumber );
			headerMap.put( "confirmedOrderValue", _confirmedOrderValue );
			headerMap.put( "confirmedOrderTax", _confirmedOrderTax );
			headerMap.put( "confirmedOrderDiscount", _confirmedOrderDiscount );
			headerMap.put( "confirmedOrderTotal", _confirmedTotalValue );
			headerMap.put( "orderVolume", dataMap.get( "ORDER_VOLUME") );
			headerMap.put( "orderWeight", dataMap.get( "ORDER_WEIGHT") );
			headerMap.put( "orderValue", _orderValue );
			headerMap.put( "freegoodsOrderInd", infoMap.get("freegoodsOrderInd") );
			headerMap.put( "parentOrderNumber", infoMap.get("parentOrderNumber") );
			headerMap.put( "creditStatus", dataMap.get( "CREDIT_STATUS") );
			headerMap.put( "status", com.irt.dpr.Order.STATUS_CREATED  );
		}

		if( Loggers.business.isTraceEnabled() ) {
			StringBuffer lbuf = new StringBuffer();
			for( String str : headerMap.keySet() )
				lbuf.append( str + ": " + headerMap.get(str) + ", " );
			Loggers.business.trace( "{}: {}", orderKey, lbuf.toString() );
			lbuf.delete( 0, lbuf.capacity() );
		}

		if( Country.isFeature(organizationCode, "useSendOrderMail") )
			sendOrderMail( salesOrderNumber, status, headerMap );
		if( !isSuccess ) {
			if( "0".equals( salesOrderNumber ) || "0000000000".equals( salesOrderNumber ) ) {
				throw new CanonicalXMLException( (String)getErrorMap().get("Text") );
			} else
				throw new CanonicalXMLException( "Order creation is failed[We shoudn't sales order number]" );
		}

		logger.info( Loggers.STR_BUSINESS + orderKey + ": " + "OrderCreationReader End.("+ headerMap.get("purchaseOrderNumber") +"["+ orderKey +"] is created "+ salesOrderNumber +"." );
	}

	private void sendOrderMail( String salesOrderNumber, String status, Map<String, Object>headerMap ) {
		Map<String, Object> parameterMap = new java.util.HashMap<String, Object>( headerMap );
		parameterMap.put( "orderNumber", salesOrderNumber );
		parameterMap.put( "createUser", getPortalUser((String)headerMap.get("updateUserId")) );
		parameterMap.put( "status", status );

		String freegoodsOrderInd = (String) headerMap.get( "freegoodsOrderInd" );
		if( "Y".equals(freegoodsOrderInd) ) {
			String parentOrderNumber = (String) headerMap.get( "parentOrderNumber" );
			parameterMap.put( "parentOrderNumber", parentOrderNumber );
		}

		Loggers.business.trace( Loggers.TEMP_TRACE, "{}: {}", orderKey, "sender start." );
		MailTransport sender = new MailTransport( handler, logger, parameterMap );
		if( (salesOrderNumber == null || salesOrderNumber.length() == 0) && "X".equals(status) ) {
			Loggers.business.trace( Loggers.TEMP_TRACE, "{}: {}", orderKey, "Ready to sending order error mail." );
			sender.send( MailTransport.SENDMAILTYPE_ERROR );
			Loggers.business.trace( Loggers.TEMP_TRACE, "{}: {}", orderKey, "Order error mail is sent" );
		} else {
			Loggers.business.trace( Loggers.TEMP_TRACE, "{}: {}", orderKey, "Ready to sending order info mail." );
			if( "Y".equals(freegoodsOrderInd) ) {
				sender.send( MailTransport.SENDMAILTYPE_FREEGOOD_ORDER );
				Loggers.business.trace( Loggers.TEMP_TRACE, "{}: {}", orderKey, "Free goods Order info mail is sent" );
			} else {
				sender.send( MailTransport.SENDMAILTYPE_INFO );
				Loggers.business.trace( Loggers.TEMP_TRACE, "{}: {}", orderKey, "Order info mail is sent" );
			}
		}
		Loggers.business.trace( Loggers.TEMP_TRACE, "{}: {}", orderKey, "sender end." );
	}
}
