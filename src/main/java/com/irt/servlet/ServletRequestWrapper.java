/*
 *	File Name:	ServletRequestWrapper.java
 *	Version:	2.2.1c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/06/30		2.2.1c	servlet 3.0문법에 맞게 수정
 *	hankalam	2016/07/29		2.2.0	create
 *
**/

package com.irt.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletRequestWrapper;

import com.irt.html.HtmlUtility;

public class ServletRequestWrapper extends HttpServletRequestWrapper {
	public ServletRequestWrapper( HttpServletRequest request ) {
		super( request );
	}

	public String getHeader( String name ) {
		String value = super.getHeader( name );
		if ( value == null ) {
			return null;
		}

		return HtmlUtility.cleanXSS( value );
	}

	public String getParameter( String parameter ) {
		String value = super.getParameter( parameter );
		if ( value == null ) {
			return null;
		}

		return HtmlUtility.cleanXSS( value );
	}

	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		java.util.Enumeration<?> paramNames = getParameterNames();

		while ( paramNames.hasMoreElements() ) {
			String name = paramNames.nextElement().toString();
			String[] values = getParameterValues( name );
			parameterMap.put( name, values );
		}

		return parameterMap;
	}

	public String[] getParameterValues( String parameter ) {
		String[] values = super.getParameterValues( parameter );
		if ( values == null ) {
			return null;
		}

		int count = values.length;
		String[] encodedValues = new String[count];

		for ( int i = 0; i < count; i++ ) {
			encodedValues[i] = HtmlUtility.cleanXSS( values[i] );
		}

		return encodedValues;
	}
}