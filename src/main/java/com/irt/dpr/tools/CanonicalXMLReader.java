/*
 *	File Name:	CanonicalXMLReader.java
 *	Version:	2.2.3(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/10/31		2.2.3	getChildNodeTextValueTrim() : 값 앞에 0을 제거하는 기능 추가
 *	jbaek		2014/10/30		2.2.2	nullDateString( 0000.00.00 ) regular expression 적용.
 *	stghr12		2008/03/31		2.2.1	updateScenarioLog(): pkCPFRScenario.pUpdateScenarioDailyLog() 사용
 *	GimHS		2007/11/30		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.sql.SQLHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 */
public abstract class CanonicalXMLReader extends CanonicalXML {
	protected CanonicalXMLReader( SQLHandler handler, Logger logger ) {
		super( handler, logger );
	}

	/**
	 * somehow RB2B has automatically apply number "123456789" -> "1234567.89" for all numbers.
	 * for correct this mis calculation D-Portal convert back "1234567.89" -> "123456789"
	 * for such as Vietname, Korea which does not have fractional currency.
	 *
	 */
	boolean isFractionCorrection = false;
	public boolean configMoneyFractionCorrection( String organizationCode ) {
		return isFractionCorrection = com.irt.dpr.Country.isFeature(organizationCode, "useFractionCorrection");
	}

	protected static com.irt.data.Date convertDate( String dateType, String dateValue ) throws CanonicalXMLException {
		return convertDate( dateType, dateValue, true );
	}

	protected static com.irt.data.Date convertDate( String dateType, String dateValue, boolean useNullDateString ) throws CanonicalXMLException {
		if( dateValue == null ) return null;

		try {
			if( useNullDateString && Pattern.matches( "0000.00.00", dateValue ) )
				return null;

			if( dateType == null ) {
				if( dateValue.length() == 10 )
					return com.irt.data.Date.getInstance( dateValue );
				else if( dateValue.length() == 8 )
					return com.irt.data.Date.getInstance( dateValue );
			} else if( DATETYPE_LEN10.equals(dateType) ) {
				if( dateValue.length() == 10 )
					return com.irt.data.Date.getInstance( dateValue );
			} else if( DATETYPE_LEN8.equals(dateType) ) {
				if( dateValue.length() == 8 )
					return com.irt.data.Date.getInstance( dateValue );
			} else
				throw new CanonicalXMLException( "illegal date type '"+ dateType +"'" );
		} catch( java.text.ParseException parseEx ) {}

		throw new CanonicalXMLException( "unparseable date '"+ dateValue +"'." );
	}

	protected static Node getChildNode( Node node, String nodeName ) {
		return getChildNode( node, nodeName, false );
	}

	protected static Node getChildNode( Node node, String nodeName, boolean recursive ) {
		for( Node childNode = node.getFirstChild(); childNode != null; childNode = childNode.getNextSibling() ) {
			if( childNode.getNodeType() == Node.ELEMENT_NODE && nodeName.equals(childNode.getNodeName()) )
				return childNode;
		}

		if( recursive ) {
			for( node = node.getFirstChild(); node != null; node = node.getNextSibling() ) {
				if( node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes() ) {
					Node childNode = getChildNode( node, nodeName, true );
					if( childNode != null )
						return childNode;
				}
			}
		}

		return null;
	}

	protected static String getChildNodeTextValueTrim( Node node, String nodeName ) {
		String val = getChildNodeTextValue( node, nodeName, false );
		if( val != null ) val = val.replaceFirst( "^0+", "" );
		return val;
	}

	protected static String getChildNodeTextValue( Node node, String nodeName ) {
		return getChildNodeTextValue( node, nodeName, false );
	}

	protected static String getChildNodeTextValueMoney( Node node, String nodeName, boolean isFractionCorrection ) {
		String value = getChildNodeTextValue( node, nodeName, false );
		return value == null ? value
				: (isFractionCorrection? value.replace(".", "").replace(",","") : value);
	}

	protected static String getChildNodeTextValue( Node node, String nodeName, boolean recursive ) {
		return getNodeTextValue( getChildNode(node, nodeName, recursive) );
	}

	protected static Double getChildNodeTextValueDouble( Node node, String nodeName ) throws CanonicalXMLException {
		return getChildNodeTextValueDouble( node, nodeName, false );
	}

	protected static Double getChildNodeTextValueDoubleMoney( Node node, String nodeName, boolean isFractionCorrection ) throws CanonicalXMLException {
		return getChildNodeTextValueDoubleMoney( node, nodeName, false, isFractionCorrection );
	}

	protected static Double getChildNodeTextValueDouble( Node node, String nodeName, boolean recursive ) throws CanonicalXMLException {
		String value = getChildNodeTextValue( node, nodeName, recursive );
		try {
			return ( value == null ? null : Double.valueOf(value) );
		} catch( NumberFormatException numEx ) {
			throw new CanonicalXMLException( "illegal number '"+ value +"'." );
		}
	}

	protected static Double getChildNodeTextValueDoubleMoney( Node node, String nodeName, boolean recursive, boolean isFractionCorrection ) throws CanonicalXMLException {
		String value = getChildNodeTextValue( node, nodeName, recursive );
		try {
			return ( value == null
					? null
					: (isFractionCorrection ? Double.valueOf(value.replace(".", "").replace(",","")) : Double.valueOf(value)) );
		} catch( NumberFormatException numEx ) {
			throw new CanonicalXMLException( "illegal number '"+ value +"'." );
		}
	}

	protected static Integer getChildNodeTextValueInt( Node node, String nodeName ) throws CanonicalXMLException {
		return getChildNodeTextValueInt( node, nodeName, false );
	}

	protected static Integer getChildNodeTextValueInt( Node node, String nodeName, boolean recursive ) throws CanonicalXMLException {
		String value = getChildNodeTextValue( node, nodeName, recursive );
		try {
			return ( value == null ? null : Integer.valueOf(value) );
		} catch( NumberFormatException numEx ) {
			try {
				double doubleValue = Double.parseDouble( value );
				if( doubleValue == (int)doubleValue )
					return Integer.valueOf ( (int)doubleValue );
			} catch( NumberFormatException ignored ) {}

			throw new CanonicalXMLException( "illegal number '"+ value +"'." );
		}
	}

	protected static String getNodeTextValue( Node node ) {
		if( node != null && node.hasChildNodes() ) {
			String value = node.getFirstChild().getNodeValue().trim();
			if( value.length() > 0 )
				return value;
		}

		return null;
	}


	public abstract int read() throws CanonicalXMLException, SQLException;

	public abstract java.net.URL getSchemaURL();

	public void validate() throws IOException, SAXException {
		validate( document, getSchemaURL(), new CanonicalXMLErrorHandler(logger) );
	}
}
