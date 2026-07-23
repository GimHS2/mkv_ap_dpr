/*
 *	File Name:	PropUtil.java
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

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class PropUtil {

	public static List<String> getPropertyKeysByRegex( Properties properties, String keyRegex ) {
		Pattern ptn = Pattern.compile(keyRegex);

		java.util.List<String> list = new java.util.ArrayList<String>();
		for( Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			if( ptn.matcher(key).matches() ) {
				list.add(key);
			}
		}

		return list;
	}

}
