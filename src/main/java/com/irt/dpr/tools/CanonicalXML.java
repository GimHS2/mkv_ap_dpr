/*
 *	File Name:	CanonicalXML.java
 *	Version:	2.2.6(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.6	HOCOGSP 추가
 *	hankalam	2019/07/31		2.2.5	Freegoods 문서타입 추가
 *	jbaek		2018/10/30		2.2.4	Vietnam 추가, SalesDocType db systemEnv에서 가져오는 방식으로 변경.
 *	hankalam	2016/11/30		2.2.3	PURCHASEORDER_METHOD_EDI: CROE -> ZDPO 로 변경
 *	jbaek		2011/11/30		2.2.2	salesCountryMap에 HoCo조직의 documentType추가
 *	stghr12		2008/03/31		2.2.1	getDocumentDate() 추가
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.dpr.Country;
import com.irt.sql.SQLHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Map;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 *
 */
class CanonicalXML {
	public final static String DOCUMENT_TYPE_ORDER		= "ORDER";
	public final static String DOCUMENT_TYPE_ENQUIRY	= "ORDER_ENQUIRY";

	public final static String ROOT_NODENAME			= "JnJOrderCanonical";
	public final static String ROOT_NODENAME_ORDER		= ROOT_NODENAME;
	public final static String ROOT_NODENAME_ENQUIRY	= ROOT_NODENAME;

	public final static String DATETYPE_LEN10			= "1";
	public final static String DATETYPE_LEN8			= "2";

	public final static String PRICETYPE_TRADEPRICE		= "AAP";
	public final static String PRICETYPE_RETAILPRICE	= "RTP";

	public final static String ROLE_SOLD_TO_PARTY		= "AG";
	public final static String ROLE_SHIP_TO_PARTY		= "WE";

	public final static String DEFAULT_ORDER_JNJID		= "";

	public final static String PURCHASEORDER_METHOD_EDI = "ZDPO";

	private final static String CANONICALXML_PROPERTIES	= "mesg/CanonicalXML.properties";

	static Properties properties = null;

	SQLHandler handler;
	Logger logger;

	Document document;
	String documentType, documentId;
	String messageId;
	String senderId, receiverId;
	String resultStatus, resultMessage;
	Map<String, Object> salesCountryMap, freegoodsCountryMap;

	protected CanonicalXML( SQLHandler handler, Logger logger ) {
		this.handler = handler;
		this.logger = logger;

		salesCountryMap = new java.util.HashMap<String, Object>();
		salesCountryMap.put( "1000", "ZORC" ); // China
		salesCountryMap.put( "1110", "ZORH" ); // Hong Kong
		salesCountryMap.put( "1200", "ZORP" ); // Taiwan
		salesCountryMap.put( "1500", "ZORO" ); // HoCo
		salesCountryMap.put( "1588", "ZORO" ); // HoCo Diamond GSP
		salesCountryMap.put( "1800", "ZORB" ); // SJJP
		salesCountryMap.put( "1900", "ZORX" ); // Shanghai
		salesCountryMap.put( "2000", "ZORI" ); // Indonesia
		salesCountryMap.put( "2210", "ZORS" ); // Singapore
		salesCountryMap.put( "2100", "ZORM" ); // Malaysia
		salesCountryMap.put( "2400", "ZORL" ); // Thailand
		salesCountryMap.put( "260S", "ZORV" ); // Vietnam
		salesCountryMap.put( "3410", "ZORN" ); // New zealand
		salesCountryMap.put( "3100", "ZORJ" ); // Japan
		salesCountryMap.put( "320S", "ZORK" ); // Korea
		salesCountryMap.put( "2300", "ZORP" ); // Philippines
		salesCountryMap.put( "3310", "ZORA" ); // Australia
		salesCountryMap.put( "3510", "ZORF" ); // Fiji
		salesCountryMap.put( "5100", "ZORD" ); // India
		salesCountryMap.put( "6000", "ZORZ" ); // Sri Lanka

		freegoodsCountryMap = new java.util.HashMap<String, Object>();
		freegoodsCountryMap.put( "1500", "ZFGC" );
		freegoodsCountryMap.put( "1588", "ZFGC" );
	}

	protected String createMessageId() throws SQLException {
/* DPR용 MessageID (BussinessMessageID) */
//		return (new com.irt.rbm.oss.MessageLog(handler)).getMessageId();
return null;
	}

	public java.util.Date getDocumentDate() {
		return null;
	}

	public String getDocumentId() {
		return documentId;
	}

	public String getDocumentType() {
		return documentType;
	}

	public String getMessageId() {
		return messageId;
	}

	public static Properties getProperties() throws IOException {
		try {
			loadProperty();
		} catch( IOException ioEx ) {
			if( properties == null ) throw ioEx;
		}

		return properties;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public String getResultStatus() {
		return resultStatus;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getDocumentCountryCode( String organizationCode ) {
		String salesDocumentType = Country.getSetting(organizationCode, "SalesDocType");
		if( salesDocumentType == null || salesDocumentType.length() <= 0 )
			return (String)salesCountryMap.get( organizationCode );
		else
			return salesDocumentType;
	}

	public String getFreegoodsDocumentCountryCode( String organizationCode ) {
		String freegoodsDocumentType = Country.getSetting(organizationCode, "FregoodsDocType");
		if( freegoodsDocumentType == null || freegoodsDocumentType.length() <= 0 )
			freegoodsDocumentType = (String)freegoodsCountryMap.get( organizationCode );

		return freegoodsDocumentType != null ? freegoodsDocumentType : "ZFGC";
	}

	static void loadProperty() throws IOException {
		Properties properties = new Properties();
		java.io.InputStream inputStream = CanonicalXML.class.getClassLoader().getResourceAsStream( CANONICALXML_PROPERTIES );
		if( inputStream == null )
			throw new IOException( "cannot find file '"+ CANONICALXML_PROPERTIES +"'." );
		try {
			properties.load( inputStream );
		} finally {
			try { inputStream.close(); } catch( Exception ex ) {}
		}

		CanonicalXML.properties = properties;
	}

	static void validate( Document document, java.net.URL schemaURL, ErrorHandler errorHandler ) throws IOException, SAXException {
		javax.xml.validation.SchemaFactory factory = javax.xml.validation.SchemaFactory.newInstance( javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI );
		javax.xml.validation.Schema schema = factory.newSchema( schemaURL );
		javax.xml.validation.Validator validator = schema.newValidator();

		validator.setErrorHandler( errorHandler );
		validator.validate( new javax.xml.transform.dom.DOMSource(document) );
	}
}
