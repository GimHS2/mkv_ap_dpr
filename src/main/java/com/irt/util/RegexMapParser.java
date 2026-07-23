/*
 *	File Name:	RegexMapParser.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.0c	create
 *
**/

package com.irt.util;

import java.util.Map;

interface RegexMapParser {
	public String[] getParsedKeyArray();

	public Map<String, Object> getParsedMap();

	public String[] getParsedValueArray();

	public Map<String, Object> parse( String source );

	public void setParsedKeys( String parsedKeys );

	public void setRegex( String regex );

	public void setSource( String source );
}
