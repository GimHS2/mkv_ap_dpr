/*
 *	File Name:	RecordFormat.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	format( recordMap, msghandler, stringBuffer ) 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
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
public interface RecordFormat {
	/**
	 * format에서 필요로 하는 fieldKey들을 fieldKeySet에 put한다.
	 */
	public void addFieldKeyToSet( Set<String> fieldKeySet );

	/**
	 * recordMap과 msghandler를 사용하여 String을 출력한다.
	 */
	public String format( Map recordMap, MessageHandler msghandler );

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer );
}
