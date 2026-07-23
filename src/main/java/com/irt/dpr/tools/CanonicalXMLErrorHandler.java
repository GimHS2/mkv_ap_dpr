/*
 *	File Name:	CanonicalXMLErrorHandler.java
 *	Version:	2.2.0(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2007/11/30		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 */
class CanonicalXMLErrorHandler implements org.xml.sax.ErrorHandler {
	Logger logger;

	public CanonicalXMLErrorHandler( Logger logger ) {
		this.logger = logger;
	}

	public void error( SAXParseException saxEx ) throws SAXException {
		logger.info( "CanonicalXMLErrorHandler error.", saxEx );
	}

	public void fatalError( SAXParseException saxEx ) throws SAXException {
		logger.info( "CanonicalXMLErrorHandler fatal error.", saxEx );
	}

	public void warning( SAXParseException saxEx ) throws SAXException {
		logger.info( "CanonicalXMLErrorHandler warning.", saxEx );
	}
}
