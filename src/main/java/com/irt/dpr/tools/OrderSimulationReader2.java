/*
 *	File Name:	OrderSimulationReader2.java
 *	Version:	2.2.1c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/12/31		2.2.1c	findBapiXmlSourceString() : xmlData 태그 파싱 로직 수정
 *	jbaek		2019/04/30		2.2.0c	create
 *
**/

package com.irt.dpr.tools;

import com.irt.data.AbstractField;
import com.irt.data.Field;
import com.irt.rbm.RBMSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

/**
 *
 * NetValue/(VATLogic) related correction( useSimrXmlCorrection )
 *
 */
public class OrderSimulationReader2 {

	protected final static int TOT_INDEX_NETVALUE = 0;

	protected final static int TOT_INDEX_TAX = 1;

	protected final static int TOT_INDEX_DISCOUNT = 2;

	protected final static int TOT_INDEX_TOTAL = 3;

	private static Double getDoubleFromBapi( Map<String, Object> parsed, String key ) throws CanonicalXMLException {
		String value = (String)parsed.get(key);
		if( value == null ) {
			throw new CanonicalXMLException("mandatory value ('" + key + "') is missing.");
		}
		return Double.parseDouble(value);
	}

	private Map<Number, Map<String, Object>> orderItemsOut = new HashMap<Number, Map<String, Object>>();
	@SuppressWarnings("unused")
	private Map<Number, Map<String, Object>> orderItemsIn = new HashMap<Number, Map<String, Object>>();

	Map<String, Map<String, Field>> bapiStructs = null;

	private XMLInputFactory _factory;
	Map<String, Object> header = new HashMap<String, Object>();

	private boolean isReadyToCorrection;

	private Object[] objs;

	private String xmlData;

	private Logger logger = Logger.getLogger(OrderSimulationReader2.class);

	public OrderSimulationReader2() {
		initBapiStruct();
	}

	/**
	 * 현재 RB2B에서 리턴하는 값은 VATLogic 적용된 값.
	 * 이 값을 원하지 않고 simple total( NetValue+Tax)을 원하는 경우 correct 하는 기능.
	 */
	Map<String, Object> correctHeaderMap( Map<String, Object> headerMap, String organizationCode ) throws CanonicalXMLException {
		boolean vatLogic = RBMSystem.getSystemEnvBool("DPR", "VATLogic;" + organizationCode, false);

		@SuppressWarnings("unchecked")
		Double[] tot = getTotalFromBapiXml((Map<Number, Map<String, Object>>)objs[1], vatLogic);
		if( tot != null ) {
			headerMap.put("orderValue", tot[TOT_INDEX_NETVALUE]);
			headerMap.put("orderTax", tot[TOT_INDEX_TAX]);
			headerMap.put("orderDiscount", tot[TOT_INDEX_DISCOUNT]);
			headerMap.put("orderTotal", tot[TOT_INDEX_TOTAL]);
		}

		return headerMap;
	}

	/**
	 * 현재 RB2B에서 SimulationOutput에 리턴하는 값은 VATLogic 적용된 값.
	 * 이 값을 원하지 않을 경우에 vatSimple( NetValue + Tax )로 correct 하는 기능.
	 */
	Map<String, Object> correctItemMap( Map<String, Object> targetMap, String organizationCode )
			throws CanonicalXMLException {
		boolean vatLogic = RBMSystem.getSystemEnvBool("DPR", "VATLogic;" + organizationCode, false);

		Object _lineNumber = targetMap.get("lineNumber");
		Number lineNumber = null;
		if( _lineNumber != null ) {
			if( _lineNumber instanceof Number ) {
				lineNumber = (Number)_lineNumber;
			} else if( _lineNumber instanceof String ) {
				lineNumber = Long.valueOf((String)_lineNumber);
			} else {
				throw new CanonicalXMLException("illegal 'lineNumber' format: " + _lineNumber);
			}
		} else {
			throw new CanonicalXMLException("'lineNumber' is mandatory.");
		}

		@SuppressWarnings("unchecked")
		Map<Number, Map<String, Object>> lines = (Map<Number, Map<String, Object>>)objs[1];
		if( lines != null ) {
			Map<String, Object> sourceMap = lines.get(lineNumber);
			if( sourceMap == null ) {
				logger.error("cannot find lineNumber: " + _lineNumber + " in OrderItemsOut structure: " + lines.size() + "\n" + lines);
			} else {
				targetMap.put("orderValue", getNetValueFromBapiXml(sourceMap, vatLogic));
				targetMap.put("price", getUnitPriceFromBapiXml(sourceMap, vatLogic));
			}
		}

		return targetMap;
	}

	StreamSource createBapiXmlSource( String xmlString ) {
		return new StreamSource(new StringReader(doBapiXmlReplaceRawAmpersandToXmlEntity(xmlString)));
	}

	private Map<String, Field> createFieldMap( Field[] fields ) {
		Map<String, Field> fieldMap = new HashMap<String, Field>();
		for( com.irt.data.Field fd : fields ) {
			fieldMap.put(fd.getFieldKey(), fd);
		}

		return fieldMap;
	}

	/**
	 *
	 * webmethod "xmlData" xmlString is correct xml except some SHORT_TEXT element has '&' char.
	 *
	 * safe to replace '&' to '&amp'
	 */
	private String doBapiXmlReplaceRawAmpersandToXmlEntity( String xmlString ) {
		// return xmlString.replaceAll("&", "&amp;#038;");
		return xmlString.replaceAll("&", "&amp;");
	}

	XMLInputFactory doConfigureFactory( XMLInputFactory f, boolean dtd, boolean nsAware, boolean coalescing, boolean replEntities ) {
		//
		f.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, nsAware);
		//
		f.setProperty(XMLInputFactory.SUPPORT_DTD, dtd);
		//
		f.setProperty(XMLInputFactory.IS_COALESCING, coalescing);
		//
		f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
		//
		f.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, replEntities);
		//
		f.setProperty(XMLInputFactory.IS_VALIDATING, false);
		//
		return f;
	}

	/*
	 * bapixml from webmethod "xmlData" as string.
	 *
	 * // if the underlying string has xml entity returns raw string
	 * // eg. &amp; -> &
	 * // &apos; -> '
	 * // &&#x21; -> !
	 * // a&#63; -> ?
	 * // &#33; -> !
	 *
	 */
	private XMLInputFactory doConfigureFactoryForBapiXml( XMLInputFactory f ) {
		return doConfigureFactory(f, false, false, true, false);
	}

	/**
	 * webMethods specific bapixml
	 *
	 * "xmlData" value as string
	 * eg
	 * <value name="xmlData">
	 * &lt;biztalk~~
	 * </value>
	 *
	 */
	private StreamSource findBapiXmlSource( XMLInputFactory factory, InputStream stream, String encoding )
			throws UnsupportedEncodingException, XMLStreamException {
		String xmlString = findBapiXmlSourceString(factory, stream, encoding);
		if( xmlString != null ) {
			return createBapiXmlSource(xmlString);
		}

		return null;
	}

	String findBapiXmlSourceString( XMLInputFactory factory, InputStream stream, String encoding )
			throws UnsupportedEncodingException, XMLStreamException {
		final XMLEventReader reader = getXmlReader(factory, stream, encoding);

		while( reader.hasNext() ) {
			final XMLEvent event = reader.nextEvent();

			if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("xmlData") ) {
				return reader.getElementText();
			}
		}

		return null;
	}

	private Map<String, Field> getBapiFieldMap( String fieldsetName ) {
		return bapiStructs.get(fieldsetName);
	}

	public String getBapiXml() {
		return xmlData;
	}

	protected Double getNetValueFromBapiXml( Map<String, Object> parsed, boolean vatLogic ) throws CanonicalXMLException {
		String key = ( vatLogic ? "subTotal2" : "netValue" );
		try {
			return getDoubleFromBapi(parsed, key);
		} catch( CanonicalXMLException xmlEx ) {
			logger.error(xmlEx);
		}

		return null;
	}

	Object[] getStoredMap() {
		return objs;
	}

	/*
	 * calculate total from "xmlData" webMethods value string.( xml string is inside xmlData )
	 * "xmlDataItems" is parsed datamap
	 */
	protected Double[] getTotalFromBapiXml( Map<Number, Map<String, Object>> parsedDetails, boolean vatLogic ) throws CanonicalXMLException {
		Double[] tot = new Double[4];

		tot[TOT_INDEX_NETVALUE] = 0.0;
		tot[TOT_INDEX_TAX] = 0.0;
		tot[TOT_INDEX_DISCOUNT] = 0.0;
		tot[TOT_INDEX_TOTAL] = 0.0;

		if( vatLogic ) {
			// double temp = WMDataUtility.getDoubleMoneyValue(items[i], "KZWI5", isFractionCorrection);
			// billingNetAmount = WMDataUtility.getDoubleMoneyValue(items[i], "KZWI2", isFractionCorrection);
			// billingTotal = WMDataUtility.getDoubleMoneyValue(items[i], "KZWI4", isFractionCorrection);
			// billingTax = billingTotal - temp;
			// billingDamagedDiscount = temp - billingNetAmount;

			for( Number lineNo : parsedDetails.keySet() ) {
				Map<String, Object> parsed = parsedDetails.get(lineNo);

				try {
					Double temp = getDoubleFromBapi(parsed, "subTotal5");
					Double netValue = getDoubleFromBapi(parsed, "subTotal2");
					Double totalValue = getDoubleFromBapi(parsed, "subTotal4");

					Double taxValue = totalValue - netValue;
					Double discountValue = temp - netValue;

					tot[TOT_INDEX_NETVALUE] += netValue;
					tot[TOT_INDEX_TAX] += taxValue;
					tot[TOT_INDEX_DISCOUNT] += discountValue;
					tot[TOT_INDEX_TOTAL] += totalValue;
				} catch( CanonicalXMLException xmlEx ) {
					logger.error(xmlEx);
					return null;
				}
			}
		} else {// simple netValue and Tax and Total.
			// billingNetAmount = WMDataUtility.getDoubleMoneyValue(items[i], "NETWR", isFractionCorrection);
			// billingTax = WMDataUtility.getDoubleMoneyValue(items[i], "MWSBP", isFractionCorrection);
			// billingTotal = billingNetAmount + billingTax;
			for( Number lineNo : parsedDetails.keySet() ) {
				Map<String, Object> parsed = parsedDetails.get(lineNo);

				try {
					Double netValue = getDoubleFromBapi(parsed, "netValue");
					Double taxValue = getDoubleFromBapi(parsed, "taxValue");
					tot[TOT_INDEX_NETVALUE] += netValue;
					tot[TOT_INDEX_TAX] += taxValue;
				} catch( CanonicalXMLException xmlEx ) {
					logger.error(xmlEx);
					return null;
				}
			}
			tot[TOT_INDEX_TOTAL] = tot[TOT_INDEX_NETVALUE] + tot[TOT_INDEX_TAX];
		}

		return tot;
	}

	protected Double getUnitPriceFromBapiXml( Map<String, Object> parsed, boolean vatLogic ) throws CanonicalXMLException {
		try {
			return getNetValueFromBapiXml(parsed, vatLogic) / getDoubleFromBapi(parsed, "orderQty");
		} catch( CanonicalXMLException xmlEx ) {
			logger.error(xmlEx);
		}

		return null;
	}

	XMLInputFactory getXmlFactory() {
		if( _factory == null ) {
			_factory = XMLInputFactory.newInstance();
			_factory = doConfigureFactoryForBapiXml(_factory);
		}
		return _factory;
	}

	XMLEventReader getXmlReader( XMLInputFactory factory, InputStream stream, String encoding ) throws XMLStreamException {
		return factory.createXMLEventReader(stream, encoding);
	}

	private Map<String, Map<String, Field>> initBapiStruct() {
		if( bapiStructs == null ) {
			bapiStructs = new HashMap<String, Map<String, Field>>();
			bapiStructs.put("OrderItemsOut", createFieldMap(OrderItemsOut.BAPI_FIELDS));
			bapiStructs.put("OrderItemsIn", createFieldMap(OrderItemsIn.BAPI_FIELDS));
		}

		return bapiStructs;
	}

	public boolean isReadyToCorrection() {
		return isReadyToCorrection;
	}

	/**
	 * StreamSource's inner source should be closed from caller.( if StreamSource's inner source is String then no need to close() );
	 */
	Object[] parseBapiByEventReader( StreamSource stmsrc ) throws XMLStreamException {
		final XMLEventReader reader = getXmlFactory().createXMLEventReader(stmsrc);

		final String END_OF_INTEREST_STRUCT = "OrderItemsOut";// no need to loop after this struct

		while( reader.hasNext() ) {
			final XMLEvent event = reader.nextEvent();

			// if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("OrderItemsIn") ) {
			// this.orderItemsIn = parseOrderItemsIn(reader);
			// }

			if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("OrderItemsOut") ) {
				this.orderItemsOut = parseStructItems(reader, "OrderItemsOut");
			}

			if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(END_OF_INTEREST_STRUCT) ) {
				break;
			}
		}
		//
		//// reader.close(); //?

		return new Object[] { header, orderItemsOut };
	}

	/**
	 * not really deprecated. use XMLEventReader because it is much simpler.
	 * but if lower level control is needed then use XMLStreamReader.
	 */
	Object[] parseBapiByStreamReader( StreamSource stmsrc ) throws XMLStreamException {
		final XMLStreamReader reader = getXmlFactory().createXMLStreamReader(stmsrc);

		boolean wasStart = false;
		boolean wasChars = false;
		StringBuffer charsInElement = new StringBuffer();

		while( reader.hasNext() ) {
			try {
				wasStart = reader.isStartElement() ? true : false;

				if( reader.isStartElement() ) {
					if( reader.getName().getLocalPart().equals("OrderItemsOut") ) {
						this.orderItemsOut = parseStructItems(reader, "OrderItemsOut");
						break;
					}
				}

				reader.next();

				if( wasStart && reader.isCharacters() ) {
					wasChars = reader.getTextLength() > 0;// CAUTION: reader.getTextLength() or same kind is needed. to really check text...
				} else {
					wasChars = false;
				}
			} catch( XMLStreamException xmlEx ) {
				if( wasChars && reader.isCharacters() ) {
					char[] target = new char[reader.getTextLength()];
					reader.getTextCharacters(0, target, reader.getTextStart(), reader.getTextLength());
					charsInElement.append(target);
					Logger.getRootLogger().debug(reader.getTextLength());

					charsInElement.append("&amp; ");// hack: usually char inside char has & char and stream reader cannot parse the text if it has '&'
					int nextEvent = reader.next();
					if( nextEvent == XMLStreamConstants.CHARACTERS ) {
						Logger.getRootLogger().debug(reader.getTextLength());
						// char[] target = new char[reader.getTextLength()];
						target = new char[reader.getTextLength()];
						reader.getTextCharacters(0, target, 0, reader.getTextLength());
						charsInElement.append(target);
						continue;
					}
				}

				throw xmlEx;
			}
		}
		charsInElement = null;
		return new Object[] { header, orderItemsOut };
	}

	Object[] parseBapiXmlStreamReader( StreamSource stmsrc ) throws XMLStreamException {
		final XMLStreamReader reader = getXmlFactory().createXMLStreamReader(stmsrc);

		boolean wasStart = false;
		boolean wasChars = false;
		StringBuffer charsInElement = new StringBuffer();

		while( reader.hasNext() ) {
			try {
				wasStart = reader.isStartElement() ? true : false;

				if( reader.isStartElement() ) {
					Logger.getRootLogger().debug(reader.getName().getLocalPart());
					if( reader.getName().getLocalPart().equals("OrderItemsOut") ) {
						// this.details = parseOrderItemsOut(getXmlFactory().createXMLEventReader(reader));
						this.orderItemsOut = parseStructItems(reader, "OrderItemsOut");
						break;
					}
				}

				reader.next();

				if( wasStart && reader.isCharacters() ) {
					wasChars = reader.getTextLength() > 0;// CAUTION: reader.getTextLength() or same kind is needed. to really check text...
				} else {
					wasChars = false;
				}
			} catch( XMLStreamException xmlEx ) {
				if( wasChars && reader.isCharacters() ) {
					char[] target = new char[reader.getTextLength()];
					reader.getTextCharacters(0, target, reader.getTextStart(), reader.getTextLength());
					charsInElement.append(target);
					Logger.getRootLogger().debug(reader.getTextLength());

					charsInElement.append("&amp; ");// hack: usually char inside char has & char and stream reader cannot parse the text if it has '&'
					int nextEvent = reader.next();
					if( nextEvent == XMLStreamConstants.CHARACTERS ) {
						Logger.getRootLogger().debug(reader.getTextLength());
						// char[] target = new char[reader.getTextLength()];
						target = new char[reader.getTextLength()];
						reader.getTextCharacters(0, target, 0, reader.getTextLength());
						charsInElement.append(target);
						continue;
					}
				}

				throw xmlEx;
			}
		}
		charsInElement = null;
		return new Object[] { header, orderItemsOut };
	}

	private Map<String, Object> parseItem( final XMLStreamReader reader, Map<String, Field> fieldMap ) throws XMLStreamException {
		HashMap<String, Object> item = new HashMap<String, Object>();

		boolean wasStart = false;
		boolean wasChars = false;
		StringBuffer charsInElement = new StringBuffer();

		while( reader.hasNext() ) {

			try {
				wasStart = reader.isStartElement() ? true : false;

				if( reader.isEndElement() && reader.getName().getLocalPart().equals("item") ) {
					break;
				}

				reader.next();

				if( reader.isStartElement() ) {
					final String elName = reader.getName().getLocalPart();

					if( fieldMap == null ) {
						item.put(elName, reader.getElementText());
					} else {
						Field fd = fieldMap.get(elName);
						if( fd != null ) {
							String fieldKey = fd.getDescriptionKey().replaceFirst("FIELD_", "");// reversed keyset

							item.put(fieldKey, reader.getElementText());
						}
					}
				}

				// parsing charsInElement

				if( wasStart && reader.isCharacters() ) {
					wasChars = reader.getTextLength() > 0;// CAUTION: reader.getTextLength() or same kind is needed. to really check text...
				} else {
					wasChars = false;
				}
			} catch( XMLStreamException xmlEx ) {
				if( wasChars && reader.isCharacters() ) {
					char[] target = new char[reader.getTextLength()];
					reader.getTextCharacters(0, target, reader.getTextStart(), reader.getTextLength());
					charsInElement.append(target);
					Logger.getRootLogger().debug(reader.getTextLength());

					charsInElement.append("&amp; ");// hack: usually char inside char has & char and stream reader cannot parse the text if it has '&'
					int nextEvent = reader.next();
					if( nextEvent == XMLStreamConstants.CHARACTERS ) {
						Logger.getRootLogger().debug(reader.getTextLength());
						// char[] target = new char[reader.getTextLength()];
						target = new char[reader.getTextLength()];
						reader.getTextCharacters(0, target, 0, reader.getTextLength());
						charsInElement.append(target);
						continue;
					}
				}

				throw xmlEx;
			}
		}
		return item;
	}

	/**
	 * webMethod "xmlData" String has "item" section
	 */
	private Map<String, Object> parseStructItem( final XMLEventReader reader, Map<String, Field> fieldMap ) throws XMLStreamException {
		HashMap<String, Object> item = new HashMap<String, Object>();

		while( reader.hasNext() ) {
			final XMLEvent event = reader.nextEvent();
			if( event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("item") ) {
				break;
			}

			if( event.isStartElement() ) {
				final String elName = event.asStartElement().getName().getLocalPart();

				if( fieldMap == null ) {
					item.put(elName, reader.getElementText());
				} else {
					Field fd = fieldMap.get(elName);
					if( fd != null ) {
						String fieldKey = fd.getDescriptionKey().replaceFirst("FIELD_", "");// reversed keyset

						item.put(fieldKey, reader.getElementText());
					}
				}
			}
		}
		return item;
	}

	private Map<Number, Map<String, Object>> parseStructItems( final XMLEventReader reader, String structName )
			throws XMLStreamException {

		Map<String, Field> fieldMap = getBapiFieldMap(structName);
		Map<Number, Map<String, Object>> details = new HashMap<Number, Map<String, Object>>();
		while( reader.hasNext() ) {
			final XMLEvent event = reader.nextEvent();

			if( event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(structName) ) {
				return details;
			}

			if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("item") ) {
				if( fieldMap != null ) {
					Map<String, Object> item = parseStructItem(reader, fieldMap);
					String _lineNumber = (String)item.get("lineNumber");
					details.put(Long.parseLong(_lineNumber), item);
				}
			}
		}
		return details;
	}

	private Map<Number, Map<String, Object>> parseStructItems( final XMLStreamReader reader, String structName )
			throws XMLStreamException {

		Map<String, Field> fieldMap = getBapiFieldMap(structName);
		Map<Number, Map<String, Object>> details = new HashMap<Number, Map<String, Object>>();
		int count = 0;
		boolean wasStart = false;
		boolean wasChars = false;
		StringBuffer charsInElement = new StringBuffer();
		while( reader.hasNext() ) {

			try {
				wasStart = reader.isStartElement() ? true : false;

				if( reader.isEndElement()
						&& reader.getName().getLocalPart().equals(structName) ) {
					Logger.getRootLogger().debug("parseManyItems: looping " + " end");
					return details;
					// break;// read next item
				}

				reader.next();

				if( reader.isStartElement() && reader.getName().getLocalPart().equals("item") ) {
					count++;
					if( fieldMap != null ) {
						Map<String, Object> item = parseItem(reader, fieldMap);
						String _lineNumber = (String)item.get("lineNumber");
						details.put(Long.parseLong(_lineNumber), item);
					}
				}

				if( wasStart && reader.isCharacters() ) {
					wasChars = reader.getTextLength() > 0;// CAUTION: reader.getTextLength() or same kind is needed. to really check text...
				} else {
					wasChars = false;
				}
			} catch( XMLStreamException xmlEx ) {
				if( wasChars && reader.isCharacters() ) {
					char[] target = new char[reader.getTextLength()];
					reader.getTextCharacters(0, target, reader.getTextStart(), reader.getTextLength());
					charsInElement.append(target);
					Logger.getRootLogger().debug(reader.getTextLength());

					charsInElement.append("&amp; ");// hack: usually char inside char has & char and stream reader cannot parse the text if it has '&'
					int nextEvent = reader.next();
					if( nextEvent == XMLStreamConstants.CHARACTERS ) {
						Logger.getRootLogger().debug(reader.getTextLength());
						// char[] target = new char[reader.getTextLength()];
						target = new char[reader.getTextLength()];
						reader.getTextCharacters(0, target, 0, reader.getTextLength());
						charsInElement.append(target);
						continue;
					}
				}

				throw xmlEx;
			}
		}
		Logger.getRootLogger().debug("parseManyItems: " + count + " end");
		return details;
	}

	synchronized boolean readyCorrection( File wmFile ) throws CanonicalXMLException, FileNotFoundException {
		if( isReadyToCorrection )
			return isReadyToCorrection;
		else {
			FileInputStream fis = new FileInputStream(wmFile);

			try {
				String utf8 = "UTF-8";
				boolean readyinternal = false;
				try {
					StreamSource stmsrc = findBapiXmlSource(getXmlFactory(), fis, utf8);
					readyinternal = readyCorrectionInternal(stmsrc);

					if( readyinternal )
						isReadyToCorrection = true;
				} catch( UnsupportedEncodingException encEx ) {
					Logger.getRootLogger().debug(encEx);
				} catch( XMLStreamException xmlEx ) {
					Logger.getRootLogger().debug(xmlEx);
				}

			} finally {
				if( fis != null ) {
					try {
						fis.close();
					} catch( IOException ignored ) {
					}
				}
			}
		}
		return isReadyToCorrection;
	}

	private synchronized boolean readyCorrectionInternal( StreamSource xmlDataStm ) throws CanonicalXMLException, FileNotFoundException {
		if( xmlDataStm != null ) {
			try {
				// objs = parseBapiByStreamReader(xmlDataStm);
				objs = parseBapiByEventReader(xmlDataStm);
			} catch( XMLStreamException xmlEx ) {
				Logger.getRootLogger().debug(xmlEx);
				throw new CanonicalXMLException(xmlEx);
			}
		}
		return objs != null;
	}

	public void setReadyToCorrection( boolean isReadyToCorrection ) {
		this.isReadyToCorrection = isReadyToCorrection;
	}

	public void setXmlData( String xmlData ) {
		this.xmlData = xmlData;
	}

	interface BapiStruct {
	}

	static class OrderItemsIn implements BapiStruct {
		static com.irt.data.Field[] BAPI_FIELDS = new com.irt.data.Field[] {
				new com.irt.data.Field(AbstractField.TYPE_STRING, "ITM_NUMBER", "lineNumber"),
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SHORT_TEXT", "shortText"),
				new com.irt.data.Field(AbstractField.TYPE_STRING, "TARGET_QTY", "inputQty"),
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SALES_UNIT", "salesUnit"), // eg. "DZ"
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SALESQTYNR", "salesUnitPieceQty"),// eg. "12"
		};
	}

	static class OrderItemsOut implements BapiStruct {
		static com.irt.data.Field[] BAPI_FIELDS = new com.irt.data.Field[] {
				new com.irt.data.Field(AbstractField.TYPE_STRING, "ITM_NUMBER", "lineNumber"),
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL_1", "subTotalText1"), // absolute value( no minus )
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL_2", "subTotalText2"), // absolute value( no minus )
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL_3", "subTotalText3"), // absolute value( no minus )
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL_4", "subTotalText4"), // absolute value( no minus )
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL_5", "subTotalText5"), // absolute value( no minus )
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL_6", "subTotalText6"), // absolute value( no minus )
				new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL1", "subTotal1"),
				new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL2", "subTotal2"),
				new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL3", "subTotal3"),
				new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL4", "subTotal4"),
				new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL5", "subTotal5"),
				new com.irt.data.Field(AbstractField.TYPE_STRING, "SUBTOTAL6", "subTotal6"),
				new com.irt.data.Field(AbstractField.TYPE_STRING, "NET_VALUE1", "netValue"),
				new com.irt.data.Field(AbstractField.TYPE_STRING, "TX_DOC_CUR", "taxValue"),
				new com.irt.data.Field(AbstractField.TYPE_STRING, "SHORT_TEXT", "shortText"),
				new com.irt.data.Field(AbstractField.TYPE_STRING, "REQ_QTY", "orderQty"),
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SALES_UNIT", "salesUnit"), // eg. "DZ"
				// new com.irt.data.Field(AbstractField.TYPE_STRING, "SALESQTYNR", "salesUnitPieceQty"),// eg. "12"
		};
	}
}
