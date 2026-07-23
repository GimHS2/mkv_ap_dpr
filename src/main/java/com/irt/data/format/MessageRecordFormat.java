/*
 *	File Name:	MessageRecordFormat.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	extends PattenRecordFormat -> implements RecordFormat, public으로 변경
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.0	messageKey에 Pattern 사용 가능하게 수정
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
public class MessageRecordFormat implements RecordFormat {
	String messageKey;
	RecordFormat messageFormat;
	Object[] params;

	public MessageRecordFormat( String messageKey ) {
		this( messageKey, null );
	}

	public MessageRecordFormat( String messageKey, Object[] params ) {
		this.messageKey = messageKey;
		this.params = params;
	}

	public MessageRecordFormat( RecordFormat messageFormat ) {
		this( messageFormat, null );
	}

	public MessageRecordFormat( RecordFormat messageFormat, Object[] params ) {
		this.messageFormat = messageFormat;
		this.params = params;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		if( messageFormat != null )
			messageFormat.addFieldKeyToSet( fieldKeySet );
		if( params != null ) {
			for( int p = 0; p < params.length; p++ )
				if( params[p] instanceof RecordFormat )
					((RecordFormat)params[p]).addFieldKeyToSet( fieldKeySet );
		}
	}

	public String format( Map recordMap, MessageHandler msghandler ) {
		String key = messageKey;
		if( messageFormat != null )
			key = messageFormat.format( recordMap, msghandler );

		if( params == null )
			return msghandler.getMessage( key );
		else {
			String[] values = new String[ params.length ];
			for( int p = 0; p < params.length; p++ ) {
				if( params[p] instanceof RecordFormat )
					values[p] = ((RecordFormat)params[p]).format( recordMap, msghandler );
				else if( params[p] != null )
					values[p] = params[p].toString();
				else
					values[p] = "";
			}

			return msghandler.getMessage( key, values );
		}
	}

	public StringBuffer format( Map recordMap, MessageHandler handler, StringBuffer stringBuffer ) {
		return stringBuffer.append( format(recordMap, handler) );
	}
}
