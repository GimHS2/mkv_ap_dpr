/*
 *	File Name:	SimHistWorkProcess.java
 *	Version:	2.2.2c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/07/30		2.2.2c	NaN 경우 발생하지 않도록 수정
 *	jbaek		2019/07/30		2.2.1c	PackDeal 추가
 *	jbaek		2018/10/30		2.2.0c	create
 *
**/

package com.irt.dpr.tools;

import com.irt.data.DataResult;
import com.irt.data.Record;
import com.irt.rbm.tools.ProcessImpl;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;
import com.irt.sql.TableDataLoader;
import com.irt.util.FilenameParser;
import com.irt.util.TimeGranular;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SimHistWorkProcess extends ProcessImpl {//@formatter:on
	Logger logger = Logger.getLogger(SimHistWorkProcess.class);

	private boolean processed;

	private SQLHandler handler;

	private String orderKey;
	private String fakeOrderKey;

	private File file;
	private Table table;
	private Object[] xmlObjs;

	public void setFakeOrderKey( String fakeOrderKey ) {
		this.fakeOrderKey = fakeOrderKey;
	}

	public SimHistWorkProcess( SQLHandler handler, File file, Table table ) {
		super("SimHistWorkProcess." + file.getName(), "SimHistWorkProcess." + file.getName());
		this.processed = false;
		this.handler = handler;
		this.file = file;
		this.table = table;
	}

	/**
	 * continue processing for ProcessRunner
	 */
	@Override
	public boolean continueProcessing() {
		return processed;
	}

	private boolean executeMain() {
		if( xmlObjs == null ) {
			throw new IllegalStateException("!!! check !!! xmlObjs is mandatory.");
		} else if( xmlObjs[0] == null ) {
			throw new IllegalStateException("!!! check !!! xmlObjs[0] is mandatory.");
		}
		if( table == null ) {
			throw new IllegalStateException("!!! check !!! table is mandatory");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> lineDefaultMap = (Map<String, Object>)xmlObjs[0];

		if( !lineDefaultMap.containsKey("fileName") ) {
			throw new IllegalStateException("!!! check !!! fileName is mandatory.");
		}

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> details = (List<Map<String, Object>>)xmlObjs[1];

		String[] lineFieldKeys = table.getFieldKeyArray();

		DataResult result = new DataResult();
		try {
			TableDataLoader loader = new TableDataLoader(lineFieldKeys, lineDefaultMap, handler, table,
					Record.INSERT | Record.UPDATE);

			for( Map<String, Object> detail : details ) {
				Map<String, Object> processed = null;
				try {
					processed = loader.processLine(handler, detail);
					loader.loadLine(handler, processed);
				} catch( com.irt.data.DataException dataEx ) {
					result.appendError(handler.createDataException(dataEx.getErrorKey(), dataEx.getMessage(), processed));
				}
			}
		} catch( SQLException sqlEx ) {
			getLogger().error(getDescription() + " file: " + file.getName(), sqlEx);
			try {
				handler.rollback();
			} catch( SQLException ignored ) {
			}
			return false;
		} finally {
			if( result.getErrorCount() > 0 ) {
				StringBuffer sbuf = new StringBuffer();
				sbuf.append(getDescription() + " file: " + file.getName() + "\n");
				for( com.irt.data.DataException err : result.getErrors() ) {
					sbuf.append("\t");
					sbuf.append(err.getMessage());
					sbuf.append("\n");
				}
				getLogger().error(sbuf.toString());
				try {
					handler.rollback();
				} catch( SQLException ignored ) {
				}
				return false;
			} else {
				try {
					handler.commit();
				} catch( SQLException ignored ) {
				}
			}
		}

		return true;
	}

	public Logger getLogger() {
		return logger;
	}

	private FilenameParser nameParser;

	public void setFilenameParser( FilenameParser nameParser ) {
		this.nameParser = nameParser;
	}

	public FilenameParser getFilenameParser() {
		return nameParser;
	}

	public void setXmlObjs( Object[] xmlObjs ) {
		this.xmlObjs = xmlObjs;
	}

	@Override
	public boolean execute() {
		if( handler == null ) {
			throw new IllegalStateException("!!! check !!! handler is mandatory.");
		}
		if( !file.exists() ) {
			throw new IllegalStateException(
					"!!! check !!! file is not exists(mayb already?) file: " + file.getAbsolutePath());
		}

		try {
			Object[] xmlObjs = this.readFile(handler, file);
			setXmlObjs(xmlObjs);
		} catch( SuppressedIgnoreableException ignored ) {
			return ( this.processed = true );
		} catch( Exception anyEx ) {
			throw new IllegalStateException("!!! check !!! error when readFile: " + file.getName(), anyEx);
		}

		return ( this.processed = executeMain() );
	}

	public Map<String, Object> getHeaderMap( SQLHandler handler, String orderKey ) throws SQLException {
		return new com.irt.dpr.Order(handler).getRecord(com.irt.dpr.Order.createPrimary(orderKey));
	}

	class SuppressedIgnoreableException extends RuntimeException {
		SuppressedIgnoreableException( String message ) {
			this(message, null);
		}

		SuppressedIgnoreableException( String message, Throwable throwable ) {
			super(message, throwable);
		}
	}

	protected Object[] readFile( SQLHandler handler, File file )
			throws IOException, com.irt.data.DataException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, ParseException {

		if( file == null ) {
			throw new IllegalStateException("!!! check !!! file is null.");
		}
		if( !file.exists() ) {
			throw new IllegalStateException("!!! check !!! file is not eixsts.");
		}
		String filename = file.getName();

		Map<String, Object> parsedMap = getFilenameParser().parse(filename);

		String parsedOrderKey = (String)parsedMap.get("orderKey");
		String parsedEventTime = (String)parsedMap.get("eventTime");
		Date eventTime = TimeGranular.getEventTimeFormat(handler.getTimeZone()).parse(parsedEventTime);
		TimeZone eventZone = handler.getTimeZone();

		this.orderKey = parsedOrderKey;
		OrderSimulationReader reader = new OrderSimulationReader(handler, logger);

		Map<String, Object> headerMap = null;
		if( this.fakeOrderKey != null ) {
			headerMap = getHeaderMap(handler, fakeOrderKey);
		} else {
			headerMap = getHeaderMap(handler, parsedOrderKey);
		}

		if( headerMap == null || headerMap.size() <= 0 ) {
			throw new IllegalStateException("!!! check !!! headerMap is null. :" + filename);
		}

		boolean isFractionCorrection = reader.configMoneyFractionCorrection((String)headerMap.get("organizationCode"));

		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse( file );
			doc.getDocumentElement().normalize();

			List<Map<String, Object>> lineList = OrderXMLUtility.getTagValueList( doc, "Lines", "Line" );
			if( lineList == null || lineList.size() == 0 ) {
				logger.info(getDescription() + " file: " + file.getAbsolutePath() + " has null lines.");

				Map<String, Object> errorMap = null;
				try {
					errorMap = getErrorMap( doc );
				} catch( CanonicalXMLException canoEx ) {
					throw new SuppressedIgnoreableException(canoEx.getMessage());
				}
				throw new SuppressedIgnoreableException((String)errorMap.get("errorMessage"));
			}

			List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
			for( int n = 0; n < lineList.size() ; n++ ) {
				Map<String, Object> lineMap = lineList.get( n );
				Map<String, Object> map = new HashMap<String, Object>();
				/* LineNo is uniqkey */
				Number lineNumber = DataUtility.getLongValue( lineMap, "LineNo");

				String itemRefInd;

				map.put("orderKey", parsedOrderKey);
				map.put("lineNumber", lineNumber);
				map.put("itemCode", DataUtility.getStringValue( lineMap, "MaterialEntered"));
				map.put("itemCodeConfirmed", DataUtility.getStringValue( lineMap, "Material"));
				itemRefInd = DataUtility.getStringValue( lineMap, "PIPOIndicate");

				map.put("itemRefInd", itemRefInd);

				map.put("simulationUOM", DataUtility.getStringValue( lineMap, "UOM"));
				map.put("orderQty", DataUtility.getDoubleMetricValue( lineMap, "OrderQty"));
				map.put("price", DataUtility.getDoubleMoneyValue( lineMap, "UnitPrice", isFractionCorrection));
				map.put("orderValue", DataUtility.getDoubleMoneyValue( lineMap, "OrderValue", isFractionCorrection));


				Object price = map.get("price");
				if( price instanceof String ) {
					if( "NaN".equalsIgnoreCase((String)price) )
						map.remove("price");
				} else if( price instanceof Double ) {
					if( ( (Double)price ).isNaN() )
						map.remove("price");
				}
				Object orderValue = map.get("orderValue");
				if( orderValue instanceof String ) {
					if( "NaN".equalsIgnoreCase((String)orderValue) )
						map.remove("orderValue");
				} else if( orderValue instanceof Double ) {
					if( ( (Double)orderValue ).isNaN() )
						map.remove("orderValue");
				}

				itemList.add(map);
			}

			for( Map<String, Object> map : itemList ) {
				logger.trace(filename + " : " + map);
			}

			headerMap.put("fileName", file.getName());
			headerMap.put("simulationOutDateTime", eventTime);
			headerMap.put("eventTime", eventTime);
			headerMap.put("eventTimeZone", eventZone.getID());

			return new Object[] { headerMap, itemList };
		} catch( SuppressedIgnoreableException passingEx ) {
			throw passingEx;
		} catch( Exception anyEx ) {
			throw new com.irt.data.DataException(com.irt.data.DataException.ERR_ERROR,
					getDescription() + " file:" + file.getAbsolutePath(), anyEx);
		}
	}

	public Map<String, Object> getErrorMap( Document doc ) throws CanonicalXMLException {
		NodeList nodeList = doc.getElementsByTagName( "xmlData" );
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
				Node childNode = CanonicalXMLReader.getChildNode( node, "body", true );
				if( childNode != null ) {
					NodeList childNodeList = childNode.getChildNodes();
					for( int n = 0; n < childNodeList.getLength(); n++ ) {
						if( Node.ELEMENT_NODE == childNodeList.item(n).getNodeType() ){
							childNode = CanonicalXMLReader.getChildNode( childNodeList.item(n), "Message" );
							break;
						}
					}
				}

				if( childNode != null ) {
					String errorID, errorNumber;
					errorMap.put( "errorID", errorID = CanonicalXMLReader.getChildNodeTextValue(childNode, "ID") );
					errorMap.put( "errorLine", errorNumber = CanonicalXMLReader.getChildNodeTextValue(childNode, "Number") );
					String errorMessage = CanonicalXMLReader.getChildNodeTextValue( childNode, "Text" );
					if( "V4".equals(errorID) && "115".equals(errorNumber) )
						errorMap.put( "errorMessage", "Purchase order number \"" + orderKey +"\" already exists. (" +  errorMessage +")" );
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

}
