/*
 *	File Name:	StockQuerySimulationReader.java
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

import com.irt.dpr.util.Loggers;
import com.irt.sql.SQLHandler;
import java.util.Map;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

/**
 *
 */
public class StockQuerySimulationReader extends CanonicalXMLReader {
	String simulationKey, queryKey;

	Map<String, Object> headerMap;
	List<Map<String, Object>> itemList;
	Map<Integer, String[]> itemsMap;

	Map<String, Object> systemInfo;


	public StockQuerySimulationReader( SQLHandler handler, Logger logger, String simulationKey, String queryKey, Map<String, Object> systemInfo ) {
		super( handler, logger );

		this.simulationKey = simulationKey;
		this.queryKey = queryKey;
		this.systemInfo = systemInfo;
		this.headerMap = new java.util.HashMap<String, Object>();
		this.itemList = new java.util.ArrayList<Map<String, Object>>();
		this.itemsMap = new java.util.HashMap<Integer, String[]>();
	}
/*
	public Map<String, Object> getDataMap( com.wm.data.IData response ) {
		Map<String, Object> output = new java.util.HashMap<String, Object> ();

		output.put( OrderCanonicalProcess.WM_PARAMS, OrderCanonicalProcess.WM_PARAMS_SIMULATION );

		output.put( "NetAmount", WMDataUtility.getStringMoneyValue(response, "NetAmount", isFractionCorrection) );
		output.put( "VAT", WMDataUtility.getStringMoneyValue(response, "VAT", isFractionCorrection) );
		output.put( "DamageDiscount", WMDataUtility.getStringMoneyValue(response, "DamageDiscount", isFractionCorrection) );
		output.put( "Total", WMDataUtility.getStringMoneyValue(response, "Total", isFractionCorrection) );
		output.put( "LatestDate", WMDataUtility.getStringValue(response, "LatestDate") );

		com.wm.data.IData[] lines = WMDataUtility.getIDataValues( response, "Lines" );
		if( lines == null || lines.length == 0 )
			return output;

		java.util.List<Map<String, Object>> lineList = new java.util.ArrayList<Map<String, Object>> ();
		for( int n = 0; n < lines.length ; n++ ) {
			com.wm.data.IData line = lines[n];
			Map<String, Object> lineMap = new java.util.HashMap<String, Object> ();
			lineMap.put( "LineNo", WMDataUtility.getLongValue( line, "LineNo" ) );
			lineMap.put( "UnitPrice", WMDataUtility.getStringValue( line, "UnitPrice" ) );
			lineMap.put( "OrderQty", WMDataUtility.getStringValue( line, "OrderQty" ) );
			lineMap.put( "OrderValue", WMDataUtility.getStringMoneyValue( line, "OrderValue", isFractionCorrection ) );
			lineMap.put( "Material", WMDataUtility.getStringValue( line, "Material" ) );
			lineMap.put( "MaterialEntered", WMDataUtility.getStringValue( line, "MaterialEntered" ) );
			lineMap.put( "UOM", WMDataUtility.getStringValue( line, "UOM" ) );
			lineMap.put( "MaterialDescription", WMDataUtility.getStringValue( line, "MaterialDescription" ) );

			com.wm.data.IData[] schedules = WMDataUtility.getIDataValues( line, "ScheduleLines" );
			java.util.List scheduleList = new java.util.ArrayList<Map<String, Object>> ();
			for( int m = 0; m < schedules.length ; m++ ) {
				Map<String, Object> scheduleMap = new java.util.HashMap<String, Object> ();
				scheduleMap.put( "RDD", WMDataUtility.getStringValue( schedules[m], "RDD" ) );
				scheduleMap.put( "ConfirmedQty", WMDataUtility.getStringValue( schedules[m], "ConfirmedQty" ) );
				scheduleMap.put( "ConfirmedValue", WMDataUtility.getStringMoneyValue( schedules[m], "ConfirmedValue", isFractionCorrection ) );

				scheduleList.add( scheduleMap );
			}
			lineMap.put( "ScheduleLines", scheduleList );

			lineList.add( lineMap );
		}
		output.put( "Lines", lineList );

		return output;
	}
*/
	@Override
	public java.net.URL getSchemaURL() {
		return null;
	}
	public String getSimulationKey() {
		return simulationKey;
	}

	public String getQueryKey() {
		return queryKey;
	}

	public Object[] getStoredMap() {
		return new Object[] { headerMap, itemList };
	}


	public int read( String outputXML, Map<String, Object> infoMap, Map<Number, String[]> details ) throws CanonicalXMLException {
		try {
			Loggers.business.debug( "{}: {}", simulationKey, "reading start." );
			writeSimulationResult( outputXML );
			readHeader( infoMap );
			int recordCount = readItem( details, infoMap );
			Loggers.business.debug( "{}: {}", simulationKey, "reading end. (" + recordCount + ") items end." );

			return recordCount;
		} catch( CanonicalXMLException xmlEx ) {
			this.resultStatus = "ER";
			this.resultMessage = xmlEx.getMessage();

			throw xmlEx;
		}
	}

	public void readHeader( Map<String, Object> infoMap ) throws CanonicalXMLException {
		Loggers.business.debug( "{}: {}", simulationKey, "<OrderHeaderIn/> and <OrderHeaderIn/> reading start." );

		configMoneyFractionCorrection((String)infoMap.get("organizationCode"));

		headerMap.putAll( infoMap );

		if( Loggers.business.isTraceEnabled() ) {
			StringBuffer lbuf = new StringBuffer();
			for( String key : headerMap.keySet() )
				lbuf.append( key + ": " +  headerMap.get(key) + ", " );
			Loggers.business.trace( "{}: {}", simulationKey, lbuf.toString() );
			lbuf.delete( 0, lbuf.capacity() );
		}

		Loggers.business.debug( "{}: {}", simulationKey, "<OrderHeaderIn/> and <OrderHeaderIn/> reading end." );
	}

	public int readItem( Map<Number, String[]> details, Map<String, Object> infoMap ) throws CanonicalXMLException {
		if( headerMap == null || headerMap.size() == 0 ) return 0;

		List<Map<String, Object>> lineList = OrderXMLUtility.getTagValueList( this.document, "Lines", "Line" );
		if( lineList == null || lineList.size() == 0 ) {
			Map<String, Object> errorMap = getErrorMap();
			throw new CanonicalXMLException( (String)errorMap.get("errorMessage") );
		}

		Loggers.business.debug( "{}: {}", simulationKey, "<Item/> reading start." );

		for( int n = 0; n < lineList.size() ; n++ ) {
			Map<String, Object> lineMap = lineList.get( n );
			/*LineNo는  document에서 uniqkey */
			Number lineNumber = DataUtility.getLongValue( lineMap, "LineNo" );
			String[] detailValues = details.get( lineNumber );
			String itemRefInd;
			String itemCode;
			String itemCodeConfirmed;
			Number childLineNumber;

			Map<String, Object> map = new java.util.HashMap<String, Object>();
			if( detailValues == null || detailValues.length == 0 ) {
				/* new line NOT USED VALUE*/
				map.put( "dmlInd", com.irt.data.Record.INSERT );
				Loggers.business.trace( "{}: {}", simulationKey, "dmlInd: INSERT" );
			} else {
				/* existing line NOT USED VALUE*/
				map.put( "dmlInd", com.irt.data.Record.UPDATE );
				Loggers.business.trace( "{}: {}", simulationKey, "dmlInd: UPDATE" );
			}

			//map.put( "lineNumber", lineNumber );
			map.put( "itemCode", itemCode = DataUtility.getStringValue(lineMap, "MaterialEntered") );
			map.put( "itemCodeConfirmed", itemCodeConfirmed =  DataUtility.getStringValue(lineMap, "Material") );
			itemRefInd = DataUtility.getStringValue(lineMap, "PIPOIndicate" );

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
			map.put( "simulationUom", DataUtility.getStringValue(lineMap, "UOM" ) );
			map.put( "inputQty", DataUtility.getDoubleMetricValue( lineMap, "OrderQty" ) );
			map.put( "price", DataUtility.getDoubleMoneyValue( lineMap, "UnitPrice", isFractionCorrection ) );
			map.put( "value", DataUtility.getDoubleMoneyValue( lineMap, "OrderValue", isFractionCorrection ) );

			Object price = map.get( "price" );
			if( price instanceof String ) {
				if( "NaN".equalsIgnoreCase((String)price) )
					map.remove("price");
			} else if( price instanceof Double ) {
				if( ( (Double)price ).isNaN() )
					map.remove("price");
			}

			Object orderValue = map.get( "value" );
			if( orderValue instanceof String ) {
				if( "NaN".equalsIgnoreCase((String)orderValue) )
					map.remove( "value" );
			} else if( orderValue instanceof Double ) {
				if( ( (Double)orderValue ).isNaN() )
					map.remove( "value" );
			}

			/* WORKING: RDD가 여러개 일때 처리 필요  */
			readLine( lineMap, map );

			if( Loggers.business.isTraceEnabled() ) {
				StringBuffer lbuf = new StringBuffer();
				for( String key : map.keySet() )
					lbuf.append( key + ": " + map.get(key) + ", " );
				Loggers.business.trace( "{}: {}", simulationKey, lbuf.toString() );
				lbuf.delete( 0, lbuf.capacity() );
			}

			itemList.add( map );
		}

		Loggers.business.debug( "{}: {}", simulationKey, "<Item/> reading end. ("+ itemList.size() +" items)" );
		return itemList.size();
	}


	@SuppressWarnings({ "unchecked" })
	public int readLine( Map<String, Object> lineDataMap, Map<String, Object> lineMap ) throws CanonicalXMLException {
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

		Loggers.business.debug( "{}: {}", simulationKey, "<schedules/> reading start." );

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
		lineMap.put( "simulationQty", Double.valueOf(confirmedOrderQty) );
		lineMap.put( "qty", Double.valueOf(confirmedOrderQty) );
		lineMap.put( "simulationValue", Double.valueOf(confirmedOrderValue) );
		Loggers.business.debug( "{}: {}", simulationKey, "<schedules/> reading end. ("+ schedules.size() +")" );

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
					NodeList childNodeList = childNode.getChildNodes();
					for( int n = 0; n < childNodeList.getLength(); n++ ) {
						if( Node.ELEMENT_NODE == childNodeList.item(n).getNodeType() ){
							childNode = getChildNode( childNodeList.item(n), "Message" );
							break;
						}
					}
				}

				if( childNode != null ) {
					String errorID, errorNumber;
					errorMap.put( "errorID", errorID = getChildNodeTextValue(childNode, "ID") );
					errorMap.put( "errorLine", errorNumber = getChildNodeTextValue(childNode, "Number") );
					String errorMessage = getChildNodeTextValue( childNode, "Text" );
					if( "V4".equals(errorID) && "115".equals(errorNumber) )
						errorMap.put( "errorMessage", "Purchase order number \"" + simulationKey +"\" already exists. (" +  errorMessage +")" );
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

	private void writeSimulationResult( String outputXML ) {
		OrderLogging orderLogger = new OrderLogging( logger, systemInfo );

		java.io.File file = orderLogger.getFile( StockQueryCanonical.STOCKQUERY_IF_SIMULATION, "RES_" + getSimulationKey() );
		this.document = orderLogger.makeOrderTraceFile( file, outputXML );
	}

	@Deprecated
	@Override
	public int read() throws CanonicalXMLException, SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
}
