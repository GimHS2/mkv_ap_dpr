/*
 *	File Name:	MessageHandler.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.util;

/**
 *
 */
public interface MessageHandler {
	/**
	 * key에 해당하는 message가 없으면 key return.
	 */
	public String getMessage( String key );

	/**
	 * key에 해당하는 message가 없으면 key return.
	 */
	public String getMessage( String key, String... arguments );

	/**
	 * key에 해당하는 message가 없으면 MissingResourceException 발생.
	 */
	public String getMessageValue( String key ) throws java.util.MissingResourceException;
}
