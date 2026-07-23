/*
 *	File Name:	MessageBundle.java
 *	Version:	2.2.2c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.2c	bundleBaseNames 추가
 *	stghr12		2010/03/31		2.2.2	getMessage( key, arguments )에서 "@" 지원
 *	stghr12		2009/06/30		2.2.1	"@" 지원
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/06/23		1.0.0	create
 *
**/

package com.irt.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * ResourceBundle로부터 값을 가지고 와서 처리.
 * <p><pre>
 * getMessage(), getMessageValue()은 key를 ResourceBundle배열에서 순서대로 검색하여 나온 값을 다음과 같이 처리한다.
 * 1. $$ -> $
 * 2. ${SOURCE_KEY}는 원래 key값
 * 3. ${XXX}는 getMessage로 XXX검색
 * getMessage(key), getMessageValue(key)는 MissingResourceException을 throw하느냐, 하지 않느냐의 차이가 있다.
 * </pre></p>
 */
public class MessageBundle implements MessageHandler {//@formatter:off
	private ResourceBundle[] bundles;
	private String[] bundleBaseNames;

	public MessageBundle( ResourceBundle... bundles ) {
		this.bundles = bundles;
	}

	public void setBundleBaseNames( String[] bundleBaseNames ) {
		this.bundleBaseNames = bundleBaseNames;
	}

	private String[] convertArguments( String... arguments ) {
		for( int i = 0; i < arguments.length; i++ )
			if( arguments[i].length() > 0 ) {
				if( arguments[i].charAt(0) == '\\' )
					arguments[i] = arguments[i].substring(1);
				else
					arguments[i] = getMessage( arguments[i] );
			}

		return arguments;
	}

	public String getMessage( String key ) {
		try {
			return getMessageValue( key, key );
		} catch( MissingResourceException misEx ) {
			return key;
		}
	}

	public String getMessage( String key, String... arguments ) {
		String sourceKey = key;

		int idx = sourceKey.indexOf( "@" );
		if( idx > 0 ) {
			key = sourceKey.substring( 0, idx );
			arguments = com.irt.util.Arrays.append( convertArguments( sourceKey.substring(idx+1).split("@") ), arguments );
		}

		try {
			return MessageFormat.format( getMessageValue(key +"_"+ arguments.length, sourceKey), (Object[])arguments );
		} catch( MissingResourceException misEx ) {
			return MessageFormat.format( getMessage(sourceKey), (Object[])arguments );
		}
	}

	public String getMessageValue( String key ) throws MissingResourceException {
		return getMessageValue( key, key );
	}

	private String getMessageValue( String key, String sourceKey ) throws MissingResourceException {
		String message = null;

		int idx = key.indexOf( "@" );
		if( idx > 0 ) {
			String key_new = key.substring( 0, idx );
			String[] arguments = convertArguments( key.substring( idx+1 ).split("@") );

			try {
				return MessageFormat.format( getMessageValue(key_new +"_"+ arguments.length, key), (Object[])arguments );
			} catch( MissingResourceException misEx ) {
				return MessageFormat.format( getMessageValue(key_new, key), (Object[])arguments );
			}
		}

		MissingResourceException missingEx = null;
		for( int i = 0; i < bundles.length; i++ ) {
			try {
				message = bundles[i].getString( key );
				break;
			} catch( MissingResourceException misEx ) {
				missingEx = misEx;
			}
		}
		if( message == null ) throw missingEx;

		int idx0 = 0;
		StringBuffer sbuf = new StringBuffer();
		do {
			int idx1 = message.indexOf( '$', idx0 );
			if( idx1 < 0 ) break;

			try {
				if( message.charAt(idx1+1) != '{' ) {
					sbuf.append( message.substring(idx0, ++idx1) );
					if( message.charAt(idx0 = idx1) == '$' ) ++idx0;
					continue;
				}
			} catch( IndexOutOfBoundsException indexBx ) {
				break;
			}
			sbuf.append( message.substring(idx0, idx1) );

			idx1 = message.indexOf( '}', idx0 = idx1 );
			if( idx1 < 0 ) break;

			key = message.substring( idx0 + 2, idx1 );
			if( "SOURCE_KEY".equals(key) )
				sbuf.append( sourceKey );
			else {
				try {
					sbuf.append( getMessageValue(key, sourceKey) );
				} catch( MissingResourceException mixEx ) {
					sbuf.append( key );
				}
			}

			idx0 = idx1 + 1;
		} while( true );

		if( idx0 == 0 )
			return message;
		else {
			try {
				sbuf.append( message.substring(idx0) );
			} catch( IndexOutOfBoundsException indexEx ) {}
			return sbuf.toString();
		}
	}
}
