/*
 *	File Name:	OrderStatusReader.java
 *	Version:	2.2.6(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2026/02/27		2.2.6	readHeader() header가 null일 때 throw 처리 추가
 *	dudwls3720	2025/12/31		2.2.5	readDetail() : itemCode, itemRefInd가 null일 때 처리 로직 추가
 *	dudwls3720	2025/10/31		2.2.4	readHeader() : orderNumber 키의 값에 앞에 0을 제거하고 값 넣도록 수정
 *	GimHS		2025/09/30		2.2.3	BTPi interface(REST) 적용
 *	jbaek		2019/03/30		2.2.2	WmDataConverter.getDataMap() 오류수정.
 *	jbaek		2013/01/30		2.2.1	PIPO 기능  개발, LineItemStatus 기능 개발
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.dpr.util.Loggers;
import com.irt.sql.SQLHandler;
import java.util.Map;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

/**
 *
 */
public class OrderStatusReader extends CanonicalXMLReader {
	String orderKey, orderNumber, organizationCode, uniqId;

	Map<String, Object> headerMap;
	Map<String, Object> totalPriceMap;
	List<Map<String, Object>> detailList;
	Map<Integer, String[]> itemsMap;


	public OrderStatusReader( SQLHandler handler, Logger logger ) {
		super( handler, logger );
		this.orderKey = null;
		this.orderNumber = null;
		this.organizationCode = null;
		this.uniqId = null;

		headerMap = new java.util.HashMap<String, Object>();
		totalPriceMap = new java.util.HashMap<String, Object>();
		detailList = new java.util.ArrayList<Map<String, Object>>();
	}

	public OrderStatusReader( SQLHandler handler, Logger logger, String orderKey, String organizationCode ) {
		super( handler, logger );
		this.orderKey = orderKey;
		this.orderNumber = null;
		this.organizationCode = organizationCode;
		this.uniqId = null;

		headerMap = new java.util.HashMap<String, Object>();
		totalPriceMap = new java.util.HashMap<String, Object>();
		detailList = new java.util.ArrayList<Map<String, Object>>();
	}

	@Override
	public java.util.Date getDocumentDate() {
		return (com.irt.data.Date)headerMap.get("orderDate");
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

	public Object[] getStoredMap() {
		return new Object[] { headerMap, detailList, totalPriceMap };
	}

	@Override
	public int read() throws CanonicalXMLException {
		return -1;
	}

	public int read( String outputXML ) throws CanonicalXMLException {
		try {
			Loggers.business.debug( "{}: {}", orderKey, "start." );

			writeStatusResult( outputXML );

			readHeader();
			int recordCount = readDetail();
			readTotals();

			Loggers.business.debug( "{}: {}", orderKey, "end." );
			return recordCount;
		} catch( CanonicalXMLException xmlEx ) {
			this.resultStatus = "ER";
			this.resultMessage = xmlEx.getMessage();
			logger.error( "OrderCanonicalXMLReader error.", xmlEx );
			throw xmlEx;
		}
	}

	/**
	 *	<TransportHeader/>, <OrderHeader/>를 읽는다.
	 */
	public void readHeader() throws CanonicalXMLException {
		Loggers.business.info( "{}: {}", orderKey, "<header/> reading start." );

		Map<String, Object> header = OrderXMLUtility.getTagValueMap( this.document, "OrderStatusResponse", "header" );
		if( header == null )
			throw new CanonicalXMLException( "Unable to find order header" );

		headerMap.put( "orderKey", getOrderKey() );
		String _organizationCode = DataUtility.getStringValue( header, "salesOrgId" );
		if( _organizationCode == null || _organizationCode.length() == 0 )
			_organizationCode = this.organizationCode;
		headerMap.put( "organizationCode", _organizationCode );

		configMoneyFractionCorrection(_organizationCode);

		headerMap.put( "orderDate", DataUtility.getStringValue( header, "orderDate") );
		headerMap.put( "orderType", DataUtility.getStringValue( header, "orderType") );
		headerMap.put( "orderNumber", this.orderNumber = DataUtility.getStringValueTrim( header, "orderId") );
		headerMap.put( "purchaseOrderNumber", DataUtility.getStringValue( header, "poNumber") );
/* WORKING: investigate requestDevlieryDate == delivery date */
		headerMap.put( "deliveryDate", DataUtility.getStringValue( header, "requestDeliveryDate") );
		headerMap.put( "inDateConfirm", DataUtility.getStringValue( header, "requestDeliveryDate") );
		headerMap.put( "shipPartyCode", DataUtility.getLongValue( header, "shipTo") );
		headerMap.put( "soldPartyCode", DataUtility.getLongValue( header, "soldTo") );
		if( orderNumber != null && orderNumber.length() > 0 )
			headerMap.put( "status", com.irt.dpr.Order.STATUS_CREATED );

		if( Loggers.business.isTraceEnabled() ) {
			StringBuffer lbuf = new StringBuffer();
			for( String key : headerMap.keySet() )
				lbuf.append( key + ": " + headerMap.get(key) + ", " );
			Loggers.business.trace( "{}: {}", orderKey, lbuf.toString() );
			lbuf.delete( 0, lbuf.capacity() );
		}

		Loggers.business.info( "{}: {}", orderKey, "<header/> reading end." );
	}

	/**
	 *	<Item/>을 읽는다. readHeader()를 먼저 호출해야 한다.
	 */
	public int readDetail() throws CanonicalXMLException {
		int result = 0;

		if( headerMap == null || headerMap.size() == 0 ) return 0;
		Loggers.business.debug( "{}: {}", orderKey, "<Item/> reading start." );

		List<Map<String, Object>> dataList = OrderXMLUtility.getTagValueList( this.document, "orderLines", "item" );
		if( dataList == null || dataList.size() == 0 || DataUtility.getLongValue(dataList.get(0), "lineNumber") == null ) {
			Loggers.business.warn( "{}: {}", orderKey, "Unable to find order detail." );
			result =  -1;
		} else {
			for( int n = 0; n < dataList.size(); n++ ) {
				Map<String, Object> line = dataList.get(n);
				Map<String, Object> map = new java.util.HashMap<String, Object>();
				Number lineNumber;
				String itemCode;
				String itemRefInd;
				String itemCodeConfirmed;
				Number childLineNumber;

				map.put( "orderKey", getOrderKey() );
				map.put( "orderNumber", this.orderNumber );
				map.put( "itemCodeConfirmed", itemCodeConfirmed = DataUtility.getStringValue(line, "materialNumber") );

				map.put( "lineNumber", lineNumber = DataUtility.getLongValue(line, "lineNumber") );
				map.put( "price", DataUtility.getDoubleMoneyValue(line, "unitPrice", isFractionCorrection) );

	/* WORKING: Input, Simultaion는 update하지 않음.
				map.put( "orderQtySimulation", WMDataUtility.getDoubleValue(line, "orderQuantity") );
				map.put( "orderValue", WMDataUtility.getDoubleValue(line, "orderValue") );
	*/
				map.put( "orderQty", DataUtility.getDoubleMetricValue(line, "orderQuantity") );
				map.put( "orderValue", DataUtility.getDoubleMoneyValue(line, "orderValue", isFractionCorrection) );
				map.put( "confirmedOrderQty", DataUtility.getDoubleMetricValue(line, "confirmedQuantity") );
				map.put( "confirmedOrderValue", DataUtility.getDoubleMoneyValue(line, "confirmedValue", isFractionCorrection) );
				map.put( "inDateConfirm", DataUtility.getStringValue(line, "deliveryDate") );
				map.put( "reason", DataUtility.getStringValue(line, "reason") );
				map.put( "orderTax", DataUtility.getDoubleMoneyValue(line, "orderTax", isFractionCorrection) );
				map.put( "InputOrderTax", DataUtility.getDoubleMoneyValue(line, "orderTax", isFractionCorrection) );

				String orderType = (String) headerMap.get( "orderType" );
				if( "ZORX".equals(orderType) ) {
					itemCode = itemCodeConfirmed;
				} else {
					itemCode = DataUtility.getStringValue( line, "materialEntered" );
					if( itemCode == null )
						itemCode = itemCodeConfirmed;
				}
				map.put( "itemCode", itemCode );
				if( "ZORX".equals(orderType) ) {
					itemRefInd = "NO";
				} else {
					itemRefInd = DataUtility.getStringValue( line, "PIPOIndicate" );
					if( itemRefInd == null )
						itemRefInd = "NO";
				}
				map.put( "itemRefInd", itemRefInd );

				// PIPO
				if( "PP".equals(itemRefInd) ) {
					if( itemCode.equals(itemCodeConfirmed) )
						childLineNumber = 1;
					else
						childLineNumber = 2;
				} else if( "PO".equals(itemRefInd) || "PI".equals(itemRefInd) ) {
						childLineNumber = 1;
				} else {
					childLineNumber = 0;
				}
				map.put( "childLineNumber", childLineNumber );

				// LineItemStatus
				map.put( "deliveryStatus", DataUtility.getStringValue(line, "status") );
				map.put( "deliveryOpenQty", DataUtility.getDoubleMetricValue(line, "openQuantity") );
				map.put( "deliveryIntraQty", DataUtility.getStringValue(line, "intraQuantity") );
				map.put( "deliveryCompQty", DataUtility.getStringValue(line, "completeQuantity") );

				// DPR_ORDER_INF_DTL
				map.put( "deliveryDate", map.get("inDateConfirm") );
				map.put( "infoPrice", map.get("price") );
				map.put( "uom", "OG".equals(itemRefInd) ? "" : DataUtility.getStringValue(line, "uom") );

				// WHEN lineNumber is not valid
				if( lineNumber.intValue() <= 0 ) {
					java.sql.PreparedStatement pstmt = null;
					java.sql.ResultSet rset = null;
					try {
						pstmt = handler.getConnection().prepareStatement(
							"SELECT LINE_NO FROM DPR_ORDER_DTL WHERE ORDERKEY = ? AND ITEMCD = ? AND ITEMCD_CNF = ? AND ITEMREF_IND = ?"
						);

						pstmt.setString( 1, getOrderKey() );
						pstmt.setString( 2, itemCode );
						pstmt.setString( 3, itemCodeConfirmed );
						pstmt.setString( 4, itemRefInd );
						rset = pstmt.executeQuery();
						int originalLineNumber = ( rset.next() ? rset.getInt(1) : -1 );

						if( originalLineNumber > 0 ) {
							logger.info( Loggers.STR_BUSINESS + getOrderKey() + ": lineNumber is getting Local DB.(original lineNumber : " + originalLineNumber + ")" + " .," );
							map.put( "lineNumber", originalLineNumber );
						} else
							logger.error( getOrderKey() + ": Can't getting Line Number. ItemCode:" + itemCode + " ." );
					} catch ( java.sql.SQLException sqlEx ) {
						logger.error( sqlEx );
					} finally {
						try { if( pstmt != null ) pstmt.close(); } catch( Exception ex ) {}
						try { if( rset != null ) rset.close(); } catch( Exception ex ) {}
					}
				}

				if( Loggers.business.isTraceEnabled() ) {
					StringBuffer lbuf = new StringBuffer();
					for (String key : map.keySet() )
						lbuf.append( key + ": " + map.get(key) + ", " );
					Loggers.business.trace( "{}: {}", orderKey, lbuf );
					lbuf.delete( 0, lbuf.capacity() );
				}

				detailList.add( map );
			}

		Loggers.business.debug( "{}: {}", orderKey, "<Item/> reading end. ("+ detailList.size() +" items)" + " ." );
		result =  detailList.size();
		}
		return result;
	}

	@Deprecated
	public int readDetail( Document document ) throws CanonicalXMLException {
		if( headerMap == null || headerMap.size() == 0 ) return 0;

		logger.info( Loggers.STR_BUSINESS + "<Item/> reading start." );

		NodeList parentNodeList = document.getElementsByTagName("orderLines");
		if( parentNodeList == null )
			throw new CanonicalXMLException( "Unable to find order detail" );

		Node parentNode = parentNodeList.item(0);
		if( parentNode == null )
			throw new CanonicalXMLException( "Unable to find order detail" );

		NodeList nodeList = parentNode.getChildNodes();
		for( int n = 0; n < nodeList.getLength(); n++ ) {

			if( !"item".equals(nodeList.item(n).getNodeName() ) )
				continue;

			Node childNode = nodeList.item( n );

			Integer lineNumber = getChildNodeTextValueInt( childNode, "lineNumber" );
			String materialNumber = getChildNodeTextValue( childNode, "materialNumber" );
			if( materialNumber == null ) {
				throw new CanonicalXMLException( "cannot find item number for '"+ lineNumber +"'." );
			}

			Map<String, Object> map = new java.util.HashMap<String, Object>();

			map.put( "orderKey", getOrderKey() );
			map.put( "orderNumber", this.orderNumber );
			map.put( "lineNumber", lineNumber );
			map.put( "itemCode", materialNumber );
			map.put( "unitPrice", getChildNodeTextValueDoubleMoney(childNode, "unitPrice", isFractionCorrection) );
			map.put( "uom", getChildNodeTextValue(childNode, "uom") );
			map.put( "orderValue", getChildNodeTextValueDoubleMoney(childNode, "orderValue", isFractionCorrection) );
			map.put( "orderQtyConfirm", getChildNodeTextValueDouble(childNode, "confirmedQuantity") );
			map.put( "orderValueConfirm", getChildNodeTextValueDoubleMoney(childNode, "confirmedValue", isFractionCorrection) );
			map.put( "deliveryDate", getChildNodeTextValue(childNode, "deliveryDate") );
			map.put( "reason", getChildNodeTextValue(childNode, "reason") );
			map.put( "orderTax", getChildNodeTextValueDoubleMoney(childNode, "orderTax", isFractionCorrection) );

			detailList.add( map );
		}
		logger.info( Loggers.STR_BUSINESS + orderKey + " <Item/> read.("+ detailList.size() +" items)" );

		return detailList.size();
	}

	/**
	 *	<totals/>을 읽는다. readHeader(), readDetail()를 먼저 호출해야 한다.
	 */
	public void readTotals() throws CanonicalXMLException {
		Loggers.business.debug( "{}: {}", orderKey, "<totals/> reading start." );

		Map<String, Object> totals = OrderXMLUtility.getTagValueMap( this.document, "OrderStatusResponse", "totals" );

		headerMap.put( "orderVolume", DataUtility.getDoubleMetricValue(totals, "volume") );
		headerMap.put( "orderVolumeUnit", DataUtility.getStringValue(totals, "volumeUnit") );
		headerMap.put( "orderWeight", DataUtility.getDoubleMetricValue(totals, "weight") );
		headerMap.put( "orderWeightUnit", DataUtility.getStringValue(totals, "weightUnit") );
		headerMap.put( "orderValue", DataUtility.getDoubleMoneyValue(totals, "orderNetAmount", isFractionCorrection) );
		headerMap.put( "orderDiscount", DataUtility.getDoubleMoneyValue(totals, "orderDamagedGoodsDiscount", isFractionCorrection) );
		headerMap.put( "orderTax", DataUtility.getDoubleMoneyValue(totals, "orderTax", isFractionCorrection) );
		headerMap.put( "orderTotal", DataUtility.getDoubleMoneyValue(totals, "orderTotal", isFractionCorrection) );
		headerMap.put( "confirmedOrderValue", DataUtility.getDoubleMoneyValue(totals, "confirmedNetAmount", isFractionCorrection) );
		headerMap.put( "confirmedOrderDiscount", DataUtility.getDoubleMoneyValue(totals, "confirmedDamagedGoodsDiscount", isFractionCorrection) );
		headerMap.put( "confirmedOrderTax", DataUtility.getDoubleMoneyValue(totals, "confirmedTax", isFractionCorrection) );
		headerMap.put( "confirmedOrderTotal", DataUtility.getDoubleMoneyValue(totals, "confirmedTotal", isFractionCorrection) );

		if( Loggers.business.isTraceEnabled() ) {
			StringBuffer lbuf = new StringBuffer();
			for( String key : headerMap.keySet() )
				lbuf.append( key + ": " + headerMap.get(key) + ", " );
			Loggers.business.trace( "{}: {}", orderKey, lbuf.toString() );
			lbuf.delete( 0, lbuf.capacity() );
		}

		Loggers.business.debug( "{}: {}", orderKey, "<totals/> reading end." );
	}

	@Deprecated
	public void readTotals( Document document ) throws CanonicalXMLException {
		logger.info( Loggers.STR_BUSINESS + orderKey + ": " + "<totals/> reading start." );

		NodeList parentNodeList = document.getElementsByTagName("totals");
		if( parentNodeList == null )
			throw new CanonicalXMLException( "<totals> element is empty" );

		Node node = parentNodeList.item(0);
		if( node == null )
			throw new CanonicalXMLException( "<totals> element is empty" );

		headerMap.put( "orderVolume", getChildNodeTextValueDouble(node, "volume") );
		headerMap.put( "orderVolumeUnit", getChildNodeTextValue(node, "volumeUnit") );
		headerMap.put( "orderWeight", getChildNodeTextValueDouble(node, "weight") );
		headerMap.put( "orderWeightUnit", getChildNodeTextValue(node, "weightUnit") );
		headerMap.put( "orderNetAmount", getChildNodeTextValueDoubleMoney(node, "orderNetAmount", isFractionCorrection) );
		headerMap.put( "orderDamagedGoodsDiscount", getChildNodeTextValueDoubleMoney(node, "orderDamagedGoodsDiscount", isFractionCorrection) );
		headerMap.put( "orderTax", getChildNodeTextValueDoubleMoney(node, "orderTax", isFractionCorrection) );
		headerMap.put( "orderTotal", getChildNodeTextValueDoubleMoney(node, "orderTotal", isFractionCorrection) );
		headerMap.put( "confirmedNetAmount", getChildNodeTextValueDoubleMoney(node, "confirmedNetAmount", isFractionCorrection) );
		headerMap.put( "confirmedDamagedDiscount", getChildNodeTextValueDoubleMoney(node, "confirmedDamagedGoodsDiscount", isFractionCorrection) );
		headerMap.put( "confirmedTax", getChildNodeTextValueDoubleMoney(node, "confirmedTax", isFractionCorrection) );
		headerMap.put( "confirmedTotalValue", getChildNodeTextValueDoubleMoney(node, "confirmedTotal", isFractionCorrection) );

		logger.info( Loggers.STR_BUSINESS + orderKey + ": " + "<totals/> reading end." );
	}

	public void setUniqId( String uniqId ) {
		this.uniqId = uniqId;
	}

	private void writeStatusResult( String outputXML ) {
		OrderLogging orderLogger = new OrderLogging( logger, null );

		java.io.File file = orderLogger.getFile( OrderCanonicalProcess.ORDER_IF_STATUS, "RES_" + getOrderKey() );
		logger.debug( "XML file name : "+ file.getAbsolutePath() +"("+ file.getName() +")" );
		this.document = orderLogger.makeOrderTraceFile( file, outputXML );
	}
}
