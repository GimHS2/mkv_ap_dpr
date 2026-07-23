/*
 *	File Name:	HyperLinkImpl.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	RecordFormat 변경사항 적용: format( recordMap, msghandler, stringBuffer ) 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	GimHS		2007/10/31		2.1.1	CONDITION_CONTAINS 추가, isValidCondition() 오류 수정(toString() 추가)
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.HyperLinkImpl -> com.irt.html.HyperLinkImpl
 *	stghr12		2006/02/28		2.0.0	version up(기존의 HyperLink은 HyperLinkImpl로 바꾸고, interface로 변경)
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.format.RecordFormat;
import com.irt.system.SessionManager;
import com.irt.util.MessageHandler;
import java.util.Map;
import java.util.Set;

/**
 *
 */
class HyperLinkImpl implements HyperLink {
	final static int CONDITION_NONE						= ' ';
	final static int CONDITION_NOT_NULL					= 'N';
	final static int CONDITION_NOT_ZERO					= 'Z';
	final static int CONDITION_EQUALS					= 'E';
	final static int CONDITION_CONTAINS					= 'C';

	String key;
	RecordFormat href, help;
	String systemCode, packageCode;
	int conditionClass = CONDITION_NONE;
	String conditionFieldKey, conditionValue;

	HyperLinkImpl( String key, RecordFormat href ) {
		this.key = key;
		this.href = href;
	}

	HyperLinkImpl( String key, RecordFormat href, RecordFormat help ) {
		this.key = key;
		this.href = href;
		this.help = help;
	}

	HyperLinkImpl( HyperLinkImpl hyperLinkImpl ) {
		this( hyperLinkImpl.key, hyperLinkImpl );
	}

	HyperLinkImpl( String key, HyperLinkImpl hyperLinkImpl ) {
		this.key = key;
		this.href = hyperLinkImpl.href;
		this.help = hyperLinkImpl.help;
		this.systemCode = hyperLinkImpl.systemCode;
		this.packageCode = hyperLinkImpl.packageCode;
		this.conditionClass = hyperLinkImpl.conditionClass;
		this.conditionFieldKey = hyperLinkImpl.conditionFieldKey;
		this.conditionValue = hyperLinkImpl.conditionValue;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		href.addFieldKeyToSet( fieldKeySet );
		if( help != null ) help.addFieldKeyToSet( fieldKeySet );
	}

	public String format( Map recordMap, MessageHandler msghandler ) {
		return getLinkString( recordMap, msghandler );
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		return stringBuffer.append( getLinkString(recordMap, msghandler) );
	}

	public String getHelpString( Map recordMap, MessageHandler msghandler ) {
		if( help == null ) return "";
		return help.format( recordMap, msghandler );
	}

	public String getLinkString( Map recordMap, MessageHandler msghandler ) {
		return href.format( recordMap, msghandler );
	}

	public String getKey() {
		return key;
	}

	boolean isAuthorized( SessionManager sessionMng ) {
		if( systemCode == null || packageCode == null ) return true;
		return sessionMng.isAuthorized( systemCode, packageCode );
	}

	boolean isValidCondition( Map recordMap ) {

		switch( conditionClass ) {
		case CONDITION_NONE:
			return true;
		case CONDITION_NOT_NULL:
			return ( recordMap.get(conditionFieldKey) != null );
		case CONDITION_NOT_ZERO:
			Object value = recordMap.get( conditionFieldKey );
			if( value != null )
				return !"0".equals( value.toString() );
			else
				return false;
		case CONDITION_EQUALS:
			value = recordMap.get( conditionFieldKey );
			if( value != null )
				return conditionValue.equals( value.toString() );
			else
				return false;
		case CONDITION_CONTAINS:
			value = recordMap.get( conditionFieldKey );
			if( value != null ) {
				String condValues[] = conditionValue.split( "," );
				for( int i = 0; i < condValues.length; i++ )
					if( condValues[i].equals(value.toString()) ) return true;
			} else
				return false;
		default:
			return false;
		}
	}

	public boolean isValidLink( Map recordMap, SessionManager sessionMng ) {
		return( isAuthorized(sessionMng) && isValidCondition(recordMap) );
	}

	void setCondition( int conditionClass, String conditionFieldKey, String conditionValue ) {
		this.conditionClass = conditionClass;
		this.conditionFieldKey = conditionFieldKey;
		this.conditionValue = conditionValue;
	}

	void setSystemPackage( String systemCode, String packageCode ) {
		this.systemCode = systemCode;
		this.packageCode = packageCode;
	}
}
