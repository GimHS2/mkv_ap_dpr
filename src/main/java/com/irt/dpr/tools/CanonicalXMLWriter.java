/*
 *	File Name:	CanonicalXMLWriter.java
 *	Version:	2.2.0(mjsnjsAP)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.sql.SQLHandler;
import java.io.*;
import java.util.Map;
import java.util.List;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 */
public abstract class CanonicalXMLWriter extends CanonicalXML {
	private final static int DEFAULT_BUFFER_SIZE		= 1024;

	protected CanonicalXMLWriter( SQLHandler handler, Logger logger ) {
		super( handler, logger );
	}

	protected Element createElement( String name, Object text ) {
		Element element = document.createElement( name );
		if( text != null )
			element.appendChild( document.createTextNode(text.toString()) );

		return element;
	}

	public abstract int write( Document document ) throws CanonicalXMLException, SQLException;
}
