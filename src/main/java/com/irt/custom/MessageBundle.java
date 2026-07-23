/*
 *	File Name:	MessageBundle.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	재작성
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.custom;

import java.text.MessageFormat;
import java.util.MissingResourceException;

/**
 *
 */
class MessageBundle implements com.irt.util.MessageHandler {
	com.irt.util.MessageHandler msghandler;

	public MessageBundle( com.irt.util.MessageHandler msghandler ) {
		this.msghandler = msghandler;
	}

	public String getMessage( String key ) {
		if( key != null && key.startsWith("TITLE_") ) {
			try {
				return msghandler.getMessageValue( key );
			} catch( java.util.MissingResourceException misEx ) {}

			int idx = key.lastIndexOf("_");
			String subKey1 = key.substring( 0, idx+1 );
			String subKey2 = "TITLE_MODE"+ key.substring( idx );

			try {
				subKey1 = msghandler.getMessageValue( subKey1 );
			} catch( java.util.MissingResourceException misEx ) {
				return key;
			}

			try {
				return MessageFormat.format( getMessageValue(subKey2 +"_1"), new Object[] { subKey1 } );
			} catch( java.util.MissingResourceException misEx ) {
				try {
					return MessageFormat.format( getMessageValue(subKey2), new Object[] { subKey1 } );
				} catch( java.util.MissingResourceException misEx2 ) {
					return key;
				}
			}
		}

		return msghandler.getMessage( key );
	}

	public String getMessage( String key, String... arguments ) {
		if( key != null && key.startsWith("TITLE_") ) {
			try {
				return MessageFormat.format( getMessageValue(key +"_"+ arguments.length), (Object[])arguments );
			} catch( MissingResourceException misEx ) {
				return MessageFormat.format( getMessage(key), (Object[])arguments );
			}
		} else
			return msghandler.getMessage( key, arguments );
	}

	public String getMessageValue( String key ) throws java.util.MissingResourceException {
		return msghandler.getMessageValue( key );
	}
}
