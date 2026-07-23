/*
 *	File Name:	OrderSimulationReader.java
 *	Version:	2.3.0
 *
 *	Description:
 *
 *	Note:
 *	OrderResultLogging 처리
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/12/31		2.3.0	readHeader() : 국가별 환경변수 useSimrXmlCorrection 값에 따른 headerMap 처리 로직 추가
 *										getErrorMap() : BTPi Response XML에 맞게 에러 메시지 갖고 오도록 로직 수정
 *	dudwls3720	2025/10/31		2.2.9	readItem() : 파싱 태그 수정
 *										writeSimulationResult() : 접근제어자 private -> public으로 변경
 *	GimHS		2025/09/30		2.2.8	BTPi interface(REST) 적용
 *	hankalam	2022/08/31		2.2.7	readItem(): Freegoods 상품이 존재할 때 orderValue 계산로직 추가
 *	jbaek		2020/03/30		2.2.6	price가 NaN일 경우 처리.
 *	hankalam	2019/07/31		2.2.5	Freegoods 기능 추가
 *	jbaek		2019/07/30		2.2.4	PCK salesUnit 추가
 *	jbaek		2019/05/30		2.2.4	Currency Correction 기능 개발.
 *	jbaek		2013/01/30		2.2.3	PIPO 기능  개발
 *	jbaek		2011/08/31		2.2.2	getErrorMap()에서 errorXMLString 가져올때 encoding(utf-16->utf-8) 강제 변경.
 *	lsinji		2010/05/28		2.2.1	getErrorMap()에서 errorXMLString 가져올때 encoding(utf-16be->utf-8) 강제 변경.
 *										** 파일과 encoding(utf-16be)의 차이 이슈
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.dpr.Country;
import com.irt.dpr.util.Loggers;
import com.irt.sql.SQLHandler;
import com.irt.util.FileUtil;

import java.util.Map;

import java.io.FileNotFoundException;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

/**
 *
 */
public class OrderSimulationReader extends CanonicalXMLReader {
	String orderKey, orderNumber;

	Map<String, Object> headerMap;
	List<Map<String, Object>> itemList;
	Map<Integer, String[]> itemsMap;

	Document outputDocument;

	OrderSimulationReader2 xmlDataProc;
	public OrderSimulationReader( SQLHandler handler, Logger logger ) {
		super( handler, logger );

		this.orderKey = null;
		this.headerMap = new java.util.HashMap<String, Object>();
		this.itemList = new java.util.ArrayList<Map<String, Object>>();
		this.itemsMap = new java.util.HashMap<Integer, String[]>();
		this.xmlDataProc = new OrderSimulationReader2();
	}

	public OrderSimulationReader( SQLHandler handler, Logger logger, String orderKey ) {
		super( handler, logger );

		this.orderKey = orderKey;
		this.headerMap = new java.util.HashMap<String, Object>();
		this.itemList = new java.util.ArrayList<Map<String, Object>>();
		this.itemsMap = new java.util.HashMap<Integer, String[]>();
		this.xmlDataProc = new OrderSimulationReader2();
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
		return new Object[] { headerMap, itemList };
	}

	public String getSalesOrderNumber() {
		return (String)headerMap.get("salesOrderNumber");
	}

	@Override
	public int read() throws CanonicalXMLException {
		return -1;
	}

	public int read( String outputXML, Map<String, Object> infoMap, Map<Number, String[]> details ) throws CanonicalXMLException {
		try {
			Loggers.business.debug( "{}: {}", orderKey, "reading start." );

			readHeader( infoMap );

			int recordCount = readItem( details, infoMap );

			Loggers.business.debug( "{}: {}", orderKey, "reading end. (" + recordCount + ") items end." );

			return recordCount;
		} catch( CanonicalXMLException xmlEx ) {
			this.resultStatus = "ER";
			this.resultMessage = xmlEx.getMessage();

			throw xmlEx;
		}
	}

	public void readHeader( Map<String, Object> infoMap ) throws CanonicalXMLException {
		Loggers.business.debug( "{}: {}", orderKey, "<OrderHeaderIn/> and <OrderHeaderIn/> reading start." );

		configMoneyFractionCorrection((String)infoMap.get("organizationCode"));

		headerMap.put( "simulationKey", orderNumber = (String)infoMap.get("purchaseOrderNumber") );
		headerMap.put( "organizationCode", infoMap.get("organizationCode") );
		headerMap.put( "distributionChannelCode", infoMap.get("distributionChannelCode") );
		headerMap.put( "divisionCode", infoMap.get("divisionCode") );
		headerMap.put( "inDateSimulation", infoMap.get("inDate") );

		headerMap.put( "orderDate", infoMap.get("orderDate") );
		headerMap.put( "shipPartyCode", infoMap.get("shipPartyCode") );
		headerMap.put( "soldPartyCode", infoMap.get("soldPartyCode") );

		if( Country.isFeature((String)infoMap.get("organizationCode"), "useSimrXmlCorrection") ) {
			try {
				if( xmlDataProc.readyCorrection(getSavedFile()) ){
					Loggers.business.debug("before headerMap correct: "+ headerMap);
					Map<String,Object> temp = xmlDataProc.correctHeaderMap(headerMap, (String)infoMap.get("organizationCode"));
					headerMap.putAll(temp);
					Loggers.business.debug("after headerMap correct: "+ headerMap);
				} else {
					logger.debug("Cannot correct headerMap!!!");
				}
			} catch( FileNotFoundException shouldnothappen ) {
				logger.warn(shouldnothappen);
			}
		} else {
			headerMap.put( "orderValue", OrderXMLUtility.getTagValue(this.document, "out_OrderSimulateReturn", "NetAmount", isFractionCorrection) );
			headerMap.put( "orderTax", OrderXMLUtility.getTagValue(this.document, "out_OrderSimulateReturn", "VAT", isFractionCorrection) );
			headerMap.put( "orderDiscount", OrderXMLUtility.getTagValue(this.document, "out_OrderSimulateReturn", "DamageDiscount", isFractionCorrection) );
			headerMap.put( "orderTotal", OrderXMLUtility.getTagValue(this.document, "out_OrderSimulateReturn", "Total", isFractionCorrection) );
		}

		headerMap.put( "latestDate", OrderXMLUtility.getTagValue(this.document, "out_OrderSimulateReturn", "LatestDate") );

		if( Loggers.business.isTraceEnabled() ) {
			StringBuffer lbuf = new StringBuffer();
			for( String key : headerMap.keySet() )
				lbuf.append( key + ": " +  headerMap.get(key) + ", " );
			Loggers.business.trace( "{}: {}", orderKey, lbuf.toString() );
			lbuf.delete( 0, lbuf.capacity() );
		}

		Loggers.business.debug( "{}: {}", orderKey, "<OrderHeaderIn/> and <OrderHeaderIn/> reading end." );
	}

	public int readItem( Map<Number, String[]> details, Map<String, Object> infoMap ) throws CanonicalXMLException {
		if( headerMap == null || headerMap.size() == 0 ) return 0;

		List<Map<String, Object>> lineList = OrderXMLUtility.getTagValueList( this.document, "Lines", "Line" );
		if( lineList == null || lineList.size() == 0 ) {
			Map<String, Object> errorMap = getErrorMap();
			throw new CanonicalXMLException( (String)errorMap.get("errorMessage") );
		}

		Loggers.business.debug( "{}: {}", orderKey, "<Item/> reading start." );

		for( int n = 0; n < lineList.size() ; n++ ) {
			Map<String, Object> lineMap = lineList.get( n );

			final int IDX_HAS_FREEGOODS = 9;
			final int IDX_INPUT_ORDER_QTY = 6;
			/*LineNo는  document에서 uniqkey */
			Number lineNumber = DataUtility.getLongValue( lineMap, "LineNo" );
			String[] detailValues = details.get( lineNumber );
			String itemRefInd;
			String itemCode;
			String itemCodeConfirmed;
			boolean freegoodsInd;
			Number childLineNumber;

			Map<String, Object> map = new java.util.HashMap<String, Object>();
			if( detailValues == null || detailValues.length == 0 ) {
				freegoodsInd = false;
				/* new line NOT USED VALUE*/
				map.put( "dmlInd", com.irt.data.Record.INSERT );
				Loggers.business.trace( "{}: {}", orderKey, "dmlInd: INSERT" );
			} else {
				freegoodsInd = "Y".equals( detailValues[IDX_HAS_FREEGOODS] );
				/* existing line NOT USED VALUE*/
				map.put( "dmlInd", com.irt.data.Record.UPDATE );
				Loggers.business.trace( "{}: {}", orderKey, "dmlInd: UPDATE" );
			}

			map.put( "freegoodsInd", detailValues[IDX_HAS_FREEGOODS] );
			map.put( "orderKey", orderKey );
			map.put( "lineNumber", lineNumber );
			map.put( "itemCode", itemCode = DataUtility.getStringValue( lineMap, "MaterialEntered") );
			map.put( "itemCodeConfirmed", itemCodeConfirmed =  DataUtility.getStringValue( lineMap, "Material") );
			itemRefInd = DataUtility.getStringValue( lineMap, "PIPOIndicate" );

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

			map.put( "itemRefInd", itemRefInd );
			map.put( "childLineNumber", childLineNumber );

			map.put( "simulationUOM", DataUtility.getStringValue( lineMap, "UOM" ) );

			String fieldName = freegoodsInd ? "inputTotalQty" : "orderQty";
			map.put( fieldName, DataUtility.getDoubleMetricValue( lineMap, "OrderQty" ) );
			if( freegoodsInd ) {
				map.put( "orderQty", Double.parseDouble(detailValues[IDX_INPUT_ORDER_QTY]) );
			}

			if( Country.isFeature((String)infoMap.get("organizationCode"), "useSimrXmlCorrection") ) {
				try {
					if( xmlDataProc.readyCorrection(getSavedFile()) ) {
						if( xmlDataProc.isReadyToCorrection() ) {
							Map<String, Object> correctMap = xmlDataProc.correctItemMap(map, (String)infoMap.get("organizationCode"));
							Loggers.business.trace("correctItemMap: {}", correctMap);
							map.putAll(correctMap);
						}
					} else {
						logger.warn("Cannot correct itemMap!!!");
					}
				} catch( FileNotFoundException shouldnothappen ) {
					logger.warn(shouldnothappen);
				}
			} else {
				map.put( "price", DataUtility.getDoubleMoneyValue( lineMap, "UnitPrice", isFractionCorrection ) );
				fieldName = freegoodsInd ? "inputTotalValue" : "orderValue";
				map.put( fieldName, DataUtility.getStringMoneyValue( lineMap, "OrderValue", isFractionCorrection ) );
			}

			Object price = map.get("price");
			if( price instanceof String ) {
				if( "NaN".equalsIgnoreCase((String)price) )
					map.remove("price");
			} else if( price instanceof Double ) {
				if( ( (Double)price ).isNaN() )
					map.remove("price");
			}

			fieldName = freegoodsInd ? "inputTotalValue" : "orderValue";
			Object orderValue = map.get( fieldName );
			if( orderValue instanceof String ) {
				if( "NaN".equalsIgnoreCase((String)orderValue) )
					map.remove( fieldName );
			} else if( orderValue instanceof Double ) {
				if( ( (Double)orderValue ).isNaN() )
					map.remove( fieldName );
			}

			if( map.containsKey(fieldName) && freegoodsInd ) {
				double inputQty = (double) map.get( "orderQty" );
				double totalQty = (double) map.get( "inputTotalQty" );
				String totalValue = (String) map.get( "inputTotalValue" );

				try {
					double value = Double.parseDouble( totalValue ) / totalQty * inputQty;
					map.put( "orderValue", value );
				} catch( NumberFormatException ignore ) {}
			}


			/* WORKING: RDD가 여러개 일때 처리 필요  */
			readLine( lineMap, map, freegoodsInd );

			if( Loggers.business.isTraceEnabled() ) {
				StringBuffer lbuf = new StringBuffer();
				for( String key : map.keySet() )
					lbuf.append( key + ": " + map.get(key) + ", " );
				Loggers.business.trace( "{}: {}", orderKey, lbuf.toString() );
				lbuf.delete( 0, lbuf.capacity() );
			}

			itemList.add( map );
		}

		Loggers.business.debug( "{}: {}", orderKey, "<Item/> reading end. ("+ itemList.size() +" items)" );
		return itemList.size();
	}

	@SuppressWarnings("unchecked")
	public int readLine( Map<String, Object> lineDataMap, Map<String, Object> lineMap, boolean freegoodsInd ) throws CanonicalXMLException {
		List<Map<String, Object>> schedules = new java.util.ArrayList<Map<String, Object>>();

		Object val = lineDataMap.get( "ScheduleLines" );
		if( val instanceof Map )
			schedules.add( (Map<String, Object>)val );
		else if( val instanceof List )
		 schedules = (List<Map<String, Object>>)val;
		else
			return 0;

		if( schedules == null || schedules.size() == 0 )
			return 0;

		Loggers.business.debug( "{}: {}", orderKey, "<schedules/> reading start." );

		String inDateString = null;
		double confirmedOrderQty = 0.0;
		double confirmedOrderValue = 0.0;
		for( int n = 0; n < schedules.size() ; n++ ) {
			Map<String, Object> schedule = schedules.get(n);

			if( inDateString == null )
				inDateString = DataUtility.getStringValue( schedule, "RDD" );

			Double qty = DataUtility.getDoubleMetricValue( schedule, "ConfirmedQty" );
			Double value = DataUtility.getDoubleMoneyValue( schedule, "ConfirmedValue", isFractionCorrection );
			if( qty != null )
				confirmedOrderQty += qty.doubleValue();
			if( value != null )
				confirmedOrderValue += value.doubleValue();
		}

/* WORKING: 순서에 무관하게 마지막 inDate를 header에 set 한다. */
		headerMap.put( "inDateSimulation", inDateString );

		lineMap.put( "inDateSimulation", inDateString );
		String fieldName = freegoodsInd ? "simulationTotalQty" : "simulationOrderQty";
		lineMap.put( fieldName, Double.valueOf(confirmedOrderQty) );
		if( freegoodsInd ) {
			double inputOrderQty = (Double)lineMap.get( "orderQty" );
			double confirmedInputQty = confirmedOrderQty > inputOrderQty ? inputOrderQty : confirmedOrderQty;
			lineMap.put( "simulationOrderQty", Double.valueOf(confirmedInputQty) );
		}

		if( Country.isFeature((String)headerMap.get("organizationCode"), "useSimrXmlCorrection") ) {
			try {
				if( xmlDataProc.readyCorrection(getSavedFile()) ) {
					Double price = (Double)lineMap.get("price");
					if( price == null )
						price = 0.0;
					fieldName = freegoodsInd ? "simulationTotalValue" : "simulationOrderValue";
					lineMap.put( fieldName, price * Double.valueOf(confirmedOrderQty) );
					if( freegoodsInd ) {
						double inputOrderQty = (Double)lineMap.get( "orderQty" );
						double confirmedInputQty = confirmedOrderQty > inputOrderQty ? inputOrderQty : confirmedOrderQty;

						lineMap.put( "simulationOrderValue", price * confirmedInputQty );
					}
				} else {
					logger.warn("Cannot correct itemMap!!!");
				}
			} catch( FileNotFoundException shouldnothappen ) {
				logger.warn(shouldnothappen);
			}
		} else {
			fieldName = freegoodsInd ? "simulationTotalValue" : "simulationOrderValue";
			lineMap.put( fieldName, Double.valueOf(confirmedOrderValue) );
			if( freegoodsInd ) {
				double price = Double.valueOf(confirmedOrderValue) / Double.valueOf(confirmedOrderQty);
				if( ((Double)price).isNaN() ) {
					price = 0.0;
				}
				double inputOrderQty = (Double)lineMap.get( "orderQty" );
				double confirmedInputValue = price * ( confirmedOrderQty > inputOrderQty ? inputOrderQty : confirmedOrderQty );


				lineMap.put( "simulationOrderValue", Double.valueOf(confirmedInputValue) );
			}
		}

		Loggers.business.debug( "{}: {}", orderKey, "<schedules/> reading end. ("+ schedules.size() +")" );

		return schedules.size();
	}

	public Map<String, Object> getErrorMap() throws CanonicalXMLException {
		NodeList nodeList = this.document.getElementsByTagName( "xmlData" );
		String errorXMLString = null;
		if( nodeList != null )
			errorXMLString = nodeList.item(0).getTextContent();

		Map<String, Object> errorMap = new java.util.HashMap<String, Object>();
		if( errorXMLString == null ) {
			errorMap.put( "errorMessage", "Can not find error message." );
			return errorMap;
		}
		errorXMLString = errorXMLString.replaceFirst( "utf-16", "utf-8" );

		Document document = null;
		try {
			try {
				document = OrderXMLUtility.stringConvertDoc( errorXMLString, logger );
			} catch( CanonicalXMLException ex ) {
				throw new CanonicalXMLException( "Simulation failed. [" + ex.getMessage() + "]" + errorXMLString );
			}

			nodeList = document.getElementsByTagName( "biztalk_1" );
			if( nodeList == null || nodeList.getLength() == 0 )
				throw new CanonicalXMLException( "Simulation failed. [No error message]" );

			Node node = nodeList.item(0);
			if( node != null ) {
				Node childNode = getChildNode( node, "body", true );
				if( childNode != null ) {
					childNode = getChildNode( childNode, "Message" );
				}

				if( childNode != null ) {
					String errorID, errorNumber;
					errorMap.put( "errorID", errorID = getChildNodeTextValue(childNode, "ID") );
					errorMap.put( "errorLine", errorNumber = getChildNodeTextValue(childNode, "Number") );
					String errorMessage = getChildNodeTextValue( childNode, "Text" );
					if( "V4".equals(errorID) && "115".equals(errorNumber) )
						errorMap.put( "errorMessage", "Purchase order number \"" + orderNumber +"\" already exists. (" +  errorMessage +")" );
					else {
						if( errorMessage != null && errorMessage.length() > 0 )
							errorMap.put( "errorMessage", "SAP: " + errorMessage );
						else
							errorMap.put( "errorMessage", "SAP: Simulation failed. [No error message]" );
					}

					return errorMap;
				}
			}

			throw new CanonicalXMLException( "Simulation failed. [No error message]" );
		} catch( CanonicalXMLException xmlEx ) {
			throw new CanonicalXMLException( xmlEx );
		}
	}

	private java.io.File savedFile;
	private java.io.File getSavedFile() {
		long maxWait = 300000;
		long eachWait = 2000;
		long waited = 0;
		while( waited < maxWait && !FileUtil.FileAcceptor.isWriteCompleted(savedFile) ) {
			waited += eachWait;
			try {
				Thread.sleep(eachWait);
			} catch( InterruptedException ignored ) {
			}
		}
		logger.debug( "OrderSimulationReader.getSavedFile waited(ms): "+ waited );

		return savedFile;
	}

	public void writeSimulationResult( String outputXML ) {
		OrderLogging orderLogger = new OrderLogging( logger, null );

		java.io.File file = orderLogger.getFile( OrderCanonicalProcess.ORDER_IF_SIMULATION, "RES_" + getOrderKey() );
		logger.debug( "XML file name : "+ file.getAbsolutePath() +"("+ file.getName() +")" );
		this.document = orderLogger.makeOrderTraceFile( file, outputXML );
		savedFile = file;
	}
}
