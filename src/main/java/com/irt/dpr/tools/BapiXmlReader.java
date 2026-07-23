/*
 *	File Name:	BapiXmlReader.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0c	create.
**/

package com.irt.dpr.tools;

import com.irt.data.DataException;
import com.irt.data.DataResult;
import com.irt.data.Field;
import com.irt.data.FieldException;

import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

public class BapiXmlReader {

	public static XMLInputFactory createNewXmlFactory() {
		XMLInputFactory _factoryInstance = XMLInputFactory.newInstance();
		_factoryInstance = doConfigureFactoryForBapiXml(_factoryInstance);
		return _factoryInstance;
	}

	/**
	 * 
	 * webmethod "xmlData" xmlString is correct xml except some SHORT_TEXT element has '&' char.
	 * 
	 * safe to replace '&' to '&amp'
	 */
	private static String doBapiXmlReplaceRawAmpersandToXmlEntity( String xmlString ) {
		if( xmlString == null )
			return null;

		// return xmlString.replaceAll("&", "&amp;#038;");
		return xmlString.replaceAll("&", "&amp;");
	}

	static XMLInputFactory doConfigureFactory( XMLInputFactory f, boolean dtd, boolean nsAware, boolean coalescing, boolean replEntities ) {
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
	private static XMLInputFactory doConfigureFactoryForBapiXml( XMLInputFactory f ) {
		return doConfigureFactory(f, false, false, true, false);
	}

	public static String findBapiXmlStringSafely( XMLEventReader reader, final String wmValueName )
			throws UnsupportedEncodingException, XMLStreamException {
		while( reader.hasNext() ) {
			final XMLEvent event = reader.nextEvent();

			if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("value") ) {
				Attribute attr_name = event.asStartElement().getAttributeByName(new QName("name"));
				if( wmValueName.equals(attr_name.getValue()) ) {
					return doBapiXmlReplaceRawAmpersandToXmlEntity(reader.getElementText());
				}
			}
		}

		return null;
	}

	private XMLInputFactory _factory;

	private LinkedHashMap<String, Field[]> structs;

	DataResult errorLog = new DataResult();

	Map<String, Map<String, Field>> cachedStructs;

	/* BapiXml specific xml escaping ( '&' char ) */
	public StreamSource createBapiXmlSource( String xmlString ) {
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
	 * webMethods specific bapixml
	 * 
	 * "xmlData" value as string
	 * eg
	 * <value name="xmlData">
	 * &lt;biztalk~~
	 * </value>
	 * 
	 */
	StreamSource findBapiXmlSource( XMLInputFactory factory, InputStream stream, String encoding, final String wmValueName )
			throws UnsupportedEncodingException, XMLStreamException {
		String xmlString = findBapiXmlSourceString(factory, stream, encoding, wmValueName);

		if( xmlString != null ) {
			return createBapiXmlSource(xmlString);
		}

		return null;
	}

	public String findBapiXmlSourceString( XMLInputFactory factory, InputStream parentXmlStream, String encoding, final String wmValueName )
			throws UnsupportedEncodingException, XMLStreamException {
		final XMLEventReader reader = getXmlReader(factory, parentXmlStream, encoding);

		try {
			return findBapiXmlStringSafely(reader, wmValueName);
		} finally {
			if( reader != null )
				reader.close();
		}
	}

	public DataResult getErrorLog() {
		return errorLog;
	}

	public XMLInputFactory getXmlFactory() {
		if( _factory == null ) {
			_factory = XMLInputFactory.newInstance();
			_factory = doConfigureFactoryForBapiXml(_factory);
		}
		return _factory;
	}

	XMLEventReader getXmlReader( XMLInputFactory factory, InputStream stream, String encoding ) throws XMLStreamException {
		return factory.createXMLEventReader(stream, encoding);
	}

	private void initCachedStructs() {
		if( cachedStructs == null ) {
			cachedStructs = new HashMap<String, Map<String, Field>>();
			for( String key : structs.keySet() ) {
				cachedStructs.put(key, createFieldMap(structs.get(key)));
			}
		}
	}

	Map<String, Object> parseBapiByEventReader( StreamSource stmsrc ) throws XMLStreamException {
		if( structs == null ) {
			throw new XMLStreamException("BapiXmlReader 'structs' is mandatory.");
		}

		final XMLEventReader reader = getXmlFactory().createXMLEventReader(stmsrc);

		Map<String, Object> ret = new TreeMap<String, Object>();

		Set<String> interests = structs.keySet();
		Iterator<Entry<String, Field[]>> it = structs.entrySet().iterator();
		Entry<String, Field[]> entry = null;
		while( reader.hasNext() ) {
			final XMLEvent event = reader.nextEvent();

			// if( event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(getEndOfInterestStruct()) ) {
			// break;
			// }

			if( entry == null ) {
				if( it.hasNext() ) {
					entry = it.next();
				} else {
					break;
				}
			}

			// if( event.isStartElement() ) {
			// String elName = event.asStartElement().getName().getLocalPart();
			// System.out.println(elName);
			// }

			String structName = entry.getKey();
			Field[] structFields = entry.getValue();

			if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(structName) ) {
				Map<Entry<String, Long>, Map<String, Object>> items = parseStructItems(reader, structName, structFields);
				ret.put(structName, items);
				entry = null;
			}

		}

		reader.close();

		return ret;
	}

	private Map<String, Object> parseStructItem( final XMLEventReader reader, Map<String, Field> fieldMap ) throws XMLStreamException {
		HashMap<String, Object> item = new HashMap<String, Object>();

		int cnt = 0;
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

						Object value = null;
						try {
							value = fd.convertObject(reader.getElementText());
						} catch( FieldException fieldEx ) {
							value = reader.getElementText();
							errorLog.appendError(
									new DataException(DataException.ERR_INVALID_VALUE, "fieldMap: " + fieldMap + " cnt: " + cnt, fieldEx));
						}

						item.put(fieldKey, value);
					}
				}
			}
			cnt++;
		}
		return item;
	}

	private Map<Entry<String, Long>, Map<String, Object>> parseStructItems( final XMLEventReader reader, String structName, Field[] xmlFields )
			throws XMLStreamException {

		Map<String, Field> fieldMap = createFieldMap(xmlFields);

		Map<Entry<String, Long>, Map<String, Object>> details = new HashMap<Entry<String, Long>, Map<String, Object>>();

		int foundStructCount = 0;

		while( reader.hasNext() ) {
			final XMLEvent event = reader.nextEvent();

			if( event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(structName) ) {
				return details;
			}

			if( event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("item") ) {
				if( fieldMap != null ) {
					Map<String, Object> item = parseStructItem(reader, fieldMap);

					Entry uniqKey = new AbstractMap.SimpleEntry(structName, foundStructCount);
					details.put(uniqKey, item);
					foundStructCount++;
				}
			}
		}
		return details;
	}

	public void setErrorLog( DataResult errorLog ) {
		this.errorLog = errorLog;
	}

	public void setStructs( LinkedHashMap<String, Field[]> structs ) {
		this.structs = structs;
		initCachedStructs();
	}

}
