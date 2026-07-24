/*
 *	File Name:	OrderXMLUtility.java
 *	Version:	2.2.3(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/12/31		2.2.3	getTagValueMap() : XML 파싱 로직 수정
 *	GimHS		2025/09/30		2.2.2	BTPi interface(REST) 적용
 *	jbaek		2013/01/30		2.2.1	로그메세지 수정
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.irt.util.Utility;

/**
 *
 */
public class OrderXMLUtility {
	Logger logger = Logger.getLogger( "com.irt.dpr.tools.OrderCanonicalProcess" );

	private static Element createElement( Document document, String name, Object text ) {
		Element element = document.createElement( name );
		if( text != null )
			element.appendChild( document.createTextNode(text.toString()) );

		return element;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Element createElements( Document document, Element parentElement, Element currentElement, Object data ) {
		if( data instanceof Map ) {
			for( Map.Entry entry : ((Map<String, Object>)data).entrySet() ) {
				Object obj = entry.getValue();

				if( (obj instanceof Map) || (obj instanceof Collection) ) {
					Element childElement = document.createElement( (String)entry.getKey() );

					createElements( document, currentElement, childElement, obj );
				} else
					currentElement.appendChild( createElement(document, (String)entry.getKey(), obj) );
			}
		} else if( data instanceof Collection ) {
			for( java.util.Iterator iterator = ((Collection)data).iterator(); iterator.hasNext(); ) {
				parentElement.appendChild( currentElement = document.createElement(currentElement.getTagName()) );
				Object obj = iterator.next();

				if( obj instanceof Collection ) {
					parentElement = currentElement;
					currentElement = document.createElement( currentElement.getTagName() );
				}

				createElements( document, parentElement, currentElement, obj );
			}
		}

		return currentElement;
	}

	public static String docConvertString( Document document, Logger logger ) throws CanonicalXMLException {
		Transformer transformer = null;

		String str = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

			Source source = new DOMSource( document );
			java.io.StringWriter sw = new java.io.StringWriter();
			StreamResult result = new StreamResult( sw );
			transformer.transform( source, result );

			try {
				str = sw.toString();
			} finally {
				sw.flush();
				try { sw.close(); } catch( IOException ex ) {}
			}
		} catch( TransformerException transEx ) {
			logger.info( "Converting Error( Document -> String )" );
		}

		if( str != null )
			return str;
		else
			throw new CanonicalXMLException( "Cannot convert Document to String" );
	}

	public static void documentWriteFile( java.io.File file, Document document, Logger logger ) {
		if( file == null )
			throw new IllegalArgumentException( "invalid file" );

		java.io.PrintWriter out = null;
		javax.xml.transform.Transformer transformer;

		try {
			out = new java.io.PrintWriter( file );
			transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty( "encoding", "UTF-8" );

			transformer.transform(
				new javax.xml.transform.dom.DOMSource( document )
				, new javax.xml.transform.stream.StreamResult( out )
			);
		} catch( java.io.FileNotFoundException fileEx ) {
			logger.error( "Can't make XML file: " + file.getName(), fileEx );
		} catch( javax.xml.transform.TransformerException transEx ) {
			logger.error( "Can't make XML file" + file.getName(), transEx );
		} catch( javax.xml.transform.TransformerFactoryConfigurationError factoryConfigEx ) {
			logger.error( "Can't make XML file" + file.getName(), factoryConfigEx );
		} finally {
			if( out != null ) out.close();
		}
	}

	public static String getTagValue( Document doc, String tagName ) {
		return getTagValue( doc, null, tagName, false );
	}
	public static String getTagValue( Document doc, String parentTagName, String tagName ) {
		return getTagValue( doc, parentTagName, tagName, false );
	}
	public static String getTagValue( Document doc, String parentTagName, String tagName, boolean isFractionCorrection ) {
		if( doc == null || tagName ==  null ) return null;

		Node findNode = null;
		if( parentTagName != null ) {
			NodeList nodeList = doc.getElementsByTagName( parentTagName );
			if( nodeList == null ) return null;

			Node parentNode = null;
			for( int n = 0; n < nodeList.getLength(); n++ ) {
				Node node = nodeList.item(n);
				if( node.getNodeType() == Node.ELEMENT_NODE ) {
					parentNode = node;
					break;
				}
			}
			if( parentNode == null ) return null;

			NodeList childNodeList = parentNode.getChildNodes();
			for( int n = 0; n < childNodeList.getLength(); n++ ) {
				Node node = childNodeList.item(n);
				if( node.getNodeType() == Node.ELEMENT_NODE && tagName.equals(node.getNodeName()) ) {
					findNode = node;
					break;
				}
			}
		} else {
			NodeList nodeList = doc.getElementsByTagName( tagName );
			for( int n = 0; n < nodeList.getLength(); n++ ) {
				Node node = nodeList.item(n);
				if( node.getNodeType() == Node.ELEMENT_NODE ) {
					findNode = node;
					break;
				}
			}
		}

		if( findNode == null ) return null;

		String val = (findNode.getChildNodes().item(0) != null) ? findNode.getChildNodes().item(0).getNodeValue() : null;
		if( isFractionCorrection )
			return ( val == null ? null : val.replace(".", "").replace(",", "") );
		else
			return val;
	}

	public static List<Map<String, Object>> getTagValueList( Document doc, String tagName ) {
		return getTagValueList( doc, null, tagName );
	}
	public static List<Map<String, Object>> getTagValueList( Document doc, String parentTagName, String tagName ) {
		if( doc == null || tagName ==  null ) return null;

		NodeList nodeList;
		if( parentTagName != null ) {
			nodeList = doc.getElementsByTagName( parentTagName );
			if( nodeList == null ) return null;

			Node parentNode = null;
			for( int n = 0; n < nodeList.getLength(); n++ ) {
				Node node = nodeList.item(n);
				if( node.getNodeType() == Node.ELEMENT_NODE ) {
					parentNode = node;
					break;
				}
			}
			if( parentNode == null ) return null;

			nodeList = ((Element)parentNode).getElementsByTagName( tagName );
		} else
			nodeList = doc.getElementsByTagName( tagName );

		if( nodeList == null ) return null;

		return getTagValueList( nodeList );
	}

	public static List<Map<String, Object>> getTagValueList( NodeList nodeList ) {
		if( nodeList == null || nodeList.getLength() == 0 ) return null;

		List<Map<String, Object>> recordList = new java.util.ArrayList<Map<String, Object>>();

		for( int n = 0; n < nodeList.getLength(); n++ ) {
			Node node = nodeList.item(n);
			if( node.getNodeType() == Node.ELEMENT_NODE ) {
				recordList.add( getTagValueMap(node) );
			}
		}

		return recordList;
	}

	public static Map<String, Object> getTagValueMap( Document doc, String tagName ) {
		return getTagValueMap( doc, null, tagName );
	}
	public static Map<String, Object> getTagValueMap( Document doc, String parentTagName, String tagName ) {
		List<Map<String, Object>> recordList = getTagValueList( doc, parentTagName, tagName );
		if( recordList == null || recordList.size() < 1 )
			return null;
		else
			return recordList.get(0);
	}

	public static Map<String, Object> getTagValueMap( Node node ) {
		if( node == null ) return null;

		Map<String, Object> record = new java.util.HashMap<String, Object>();

		NodeList childNodeList = node.getChildNodes();
		for( int c = 0; c < childNodeList.getLength(); c++ ) {
			Node childNode = childNodeList.item(c);

			if( childNode.getNodeType() == Node.ELEMENT_NODE ) {
				Object val;
				NodeList valNodeList = childNode.getChildNodes();
				String key = childNode.getNodeName();

				if( "ScheduleLines".equals(key) )
					val = getTagValueList( childNode.getChildNodes() );
				else if( valNodeList.getLength() > 1 )
					val = getTagValueMap( childNode );
				else
					val = ( childNode.getChildNodes().item(0) != null ? childNode.getChildNodes().item(0).getNodeValue() : null );

				if( record.containsKey(key) ) {
					Object tmpVal = record.remove( key );
					if( tmpVal instanceof Object[] ) {
						Object[] arrays_new = new Object[ ((Object[])tmpVal).length + 1 ];
						System.arraycopy( tmpVal, 0, arrays_new, 0, ((Object[])tmpVal).length );
						arrays_new[((Object[])tmpVal).length] = val;

						val = arrays_new;
					} else
						val = new Object[] { tmpVal, val };
				}

				record.put( key, val );
			}
		}

		return record;
	}

	public static Document mapConvertDoc( Map<String, Object> data, Logger logger ) throws CanonicalXMLException {
		if( data == null ) return null;

//		this.logger = logger;
		javax.xml.parsers.DocumentBuilder builder;
		Document document = null;

		try {
			builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.newDocument();

			String rootNodeName = (String)data.remove( OrderCanonicalProcess.WM_PARAMS );
			if( rootNodeName == null || rootNodeName.length() == 0 )
				rootNodeName = "Data";

			Element rootElement, element;
			document.appendChild( rootElement = document.createElement(rootNodeName) );

			element = rootElement;
			for( Map.Entry<String, Object> entry : data.entrySet() ) {
				Object obj = entry.getValue();
				if( obj instanceof Map || obj instanceof Collection ) {
					Element childElement = document.createElement( entry.getKey() );

					createElements( document, element, childElement, obj );
				} else {
					element.appendChild( createElement(document, entry.getKey(), obj) );
				}
			}
		} catch ( ParserConfigurationException parseEX ) {
			logger.error( "Converting Error( Map -> Document )" );
		}

		return document;
	}

	public static Document stringConvertDoc( String xmlString, Logger logger ) throws CanonicalXMLException {
		Document document = null;
		java.io.InputStream strStream = null;
		try {
			strStream = new ByteArrayInputStream( xmlString.getBytes("UTF-8") );
			document = parseDocument( strStream, new CanonicalXMLErrorHandler(logger) );
		} catch ( java.io.IOException ioEx ) {
			logger.info( "Converting Error( can't makeing StringInputStream )" );
		} catch ( ParserConfigurationException parseEX ) {
			logger.info( "Converting Error( String -> Document )" );
		} catch ( SAXException saxEx ) {
			logger.info( "Converting Error( String -> Document )" );
		} finally {
			if( strStream != null ) try{ strStream.close(); } catch( java.io.IOException ex ) {}
		}

		if( document != null )
			return document;
		else
			throw new CanonicalXMLException( "Cannot convert String to Document" );
	}

	public static Document parseDocument( java.io.InputStream inputStream, ErrorHandler errorHandler )
						throws IOException, ParserConfigurationException, SAXException {
		javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();

		factory.setIgnoringComments( true );
		factory.setNamespaceAware( true );

		javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
		try {
			factory.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );
		} catch (ParserConfigurationException e) {}
		builder.setErrorHandler( errorHandler );

		return builder.parse( inputStream );
	}
}
