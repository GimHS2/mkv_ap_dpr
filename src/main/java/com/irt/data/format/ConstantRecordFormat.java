/*
 *	File Name:	ConstantRecordFormat.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	extends PattenRecordFormat -> implements RecordFormat, public으로 변경
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	getString() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data.format;

import com.irt.util.MessageHandler;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ConstantRecordFormat implements RecordFormat {
	String string;

	public ConstantRecordFormat( String string ) {
		this.string = string;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {}

	public String format( Map recordMap, MessageHandler msghandler ) {
		return string;
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		return stringBuffer.append( string );
	}

	public String getString() {
		return string;
	}
}
