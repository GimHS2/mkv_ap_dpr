/*
 *	File Name:	BasicData.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/03/30		2.2.1	evalPlaceholder 추가
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.apache.log4j.Logger;

/*
 *
 */
public class BasicData {//@formatter:off
	private final static String DEFAULT_NUMBERFORMAT		=	"###,###.00";
	private final static int BUFFER_SIZE					=	1024;

	public static String getStringValue( Object value ) {
		if( value == null ) return null;

		if( value instanceof String )
			return (String)value;
		else if( value instanceof Number )
			return getNumber( (Number)value, DEFAULT_NUMBERFORMAT );
		else 
			return value.toString();
	}

	public static String getNumber( Number value, String numberFormatString ) {
		NumberFormat numberFormat = new DecimalFormat( numberFormatString );

		return numberFormat.format( value );
	}

	public static String evalPlaceholder( java.net.URL template, Map<String,Object> contentMap, Logger logger, final String OPEN_TOKEN, final String CLOSE_TOKEN  ) {
		java.io.InputStream in=null;
		try {
			in = template.openStream();
		} catch( IOException ioEx ) {
			logger.error(ioEx);
		}

		java.io.BufferedReader reader = null;
		try {
			if( in != null ) {
				reader = new java.io.BufferedReader( new java.io.InputStreamReader(in) );
			}
		} catch( Exception ex ) {
			logger.error( ex );
		}

		if( reader == null )
			return "";

		StringBuffer strbuf = new StringBuffer();
		char[] buf = new char[BUFFER_SIZE];
		int ret = 0;
		try {
			while( (ret = reader.read(buf, 0, BUFFER_SIZE)) > 0 )
				strbuf.append( buf, 0, ret );
		} catch( java.io.IOException ioEx ) {
			logger.error( ioEx );
		}

		String html = null;
		try {
			int position = 0;
			int destination = -1;
			int index = 0;
			html = strbuf.toString();
			while( (position = html.indexOf(OPEN_TOKEN)) > 0 ) {
				destination = html.indexOf( CLOSE_TOKEN, position );
				try {
					String key = html.substring( position + OPEN_TOKEN.length(), destination );
					String value = com.irt.dpr.util.BasicData.getStringValue( contentMap.get(key) );
					if( value == null ) value = (String)"";

					html = html.replace( OPEN_TOKEN + key + CLOSE_TOKEN, value.toString() );
				} catch( java.lang.ArrayIndexOutOfBoundsException boundEx ) {
					logger.error( boundEx );
					break;
				}
			}
		} finally {
			try { reader.close(); } catch( Exception ex ) {}
			try { in.close(); } catch( Exception ex ) {}
		}

		return html.toString();
	}
}
