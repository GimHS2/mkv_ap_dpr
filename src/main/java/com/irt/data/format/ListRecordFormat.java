/*
 *	File Name:	ListRecordFormat.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	extends PattenRecordFormat -> implements RecordFormat, public으로 변경
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
public class ListRecordFormat implements RecordFormat {
	Object[] objects;

	public ListRecordFormat( Object... objects ) {
		this.objects = objects;
	}

	public ListRecordFormat( RecordFormat... formats ) {
		this.objects = formats;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		for( int i = 0; i < objects.length; i++ )
			if( objects[i] instanceof RecordFormat )
				((RecordFormat)objects[i]).addFieldKeyToSet( fieldKeySet );
	}

	public String format( Map recordMap, MessageHandler msghandler ) {
		return format( recordMap, msghandler, new StringBuffer() ).toString();
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		for( int i = 0; i < objects.length; i++ ) {
			if( objects[i] instanceof RecordFormat )
				((RecordFormat)objects[i]).format( recordMap, msghandler, stringBuffer );
			else if( objects[i] != null )
				stringBuffer.append( objects[i].toString() );
		}

		return stringBuffer;
	}
}
