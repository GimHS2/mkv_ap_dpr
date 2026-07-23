/*
 *	File Name:	HtmlUtility.java
 *	Version:	2.2.5c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2024/08/30		2.2.5c	checkXSS() : 로직에서 특수문자 & 삭제
 *	dudwls3720	2024/07/31		2.2.4c	checkXSS() : 특수문자 &, +, .. 추가하고 문자열 비교시 소문자로 변환하여 비교하도록 변경
 *	hankalam	2020/12/31		2.2.3c	checkURL() 추가
 *	jbaek		2020/06/30		2.2.2c	encodeURIComponenet/decodeURIComponent 추가.
 *	hankalam	2016/09/30		2.2.2	checkXSS(), cleanXSS(), toASCIICodeString() 추가
 *	hankalam	2016/08/31		2.2.1	toScriptString() : 문자변환 추가
 *	hankalam	2015/10/30		2.2.0	toScriptString() : / 문자 변환 추가
 *	stghr12		2006/02/28		2.0.0	create( StringFormat -> HtmlUtility )
 *
**/

package com.irt.html;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;

/**
 *
 */
public class HtmlUtility {
	HtmlUtility() {}

	public static boolean checkURL( String url, String[] domainWhiteList ) {
		String checkURL = url;
		checkURL = checkURL.replaceAll("^(?i)(http://)|^(?i)(https://)", "" );
		checkURL = checkURL.replaceAll( "^(www\\.)", "" );

		for( String domain : domainWhiteList ) {
			domain = domain.replaceAll( "^(?i)(http://)|^(https://)", "" );
			domain = domain.replaceAll( "^(?i)(www\\.)", "" );
			domain = domain.trim();
			if( checkURL.toLowerCase().startsWith(domain.toLowerCase()) ) {
				return true;
			}
		}
		return false;
	}

	public static String checkXSS( String value ) {
		if( value == null ) return "";
		try {
			if( value.indexOf(java.net.URLDecoder.decode("%00", "UTF-8")) > 0 )
				return "";
		} catch( java.io.UnsupportedEncodingException ex ) {}

		if( value.indexOf("<") >= 0 || value.indexOf(">") >= 0 || value.indexOf("\\(") >= 0 || value.indexOf("\\)") >= 0
				|| value.indexOf("'") >= 0 || value.indexOf("\"") >= 0 || value.indexOf("+") >= 0 || value.indexOf("..") >= 0
				|| value.toLowerCase().indexOf("eval\\((.*)\\)") >= 0 || value.toLowerCase().indexOf("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']") >= 0 ) {
			return "";
		}

		return value;
	}

	public static String cleanXSS( String value ) {
		if( value == null ) return "";

		value = value.replaceAll( "<", "&lt;" ).replaceAll( ">", "&gt;" );
		value = value.replaceAll( "\\(", "&#40;" ).replaceAll( "\\)", "&#41;" );
		value = value.replaceAll( "'", "&#39;" );
		value = value.replaceAll( "\"", "&#34;" );
		value = value.replaceAll( "eval\\((.*)\\)", "" );
		value = value.replaceAll( "[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"" );

		try {
			value = value.replaceAll( java.net.URLDecoder.decode("%00", "UTF-8"), "" );
		} catch( java.io.UnsupportedEncodingException ex ) {}

		return value;
	}

	/**
	 * Java impl of javascript decodeURIComponenet ( UTF-8 )
	 *
	 * @param encodedURI : "http%3A%2F%2Fwww.example.com"
	 * @return : "http://www.example.com"
	 */
	public static String decodeURIComponent( String encodedURI ) {
		if( encodedURI == null ) {
			return null;
		}

		String result = null;

		try {
			result = URLDecoder.decode(encodedURI, "UTF-8");
		}

		// This exception should never occur.
		catch( UnsupportedEncodingException e ) {
			result = encodedURI;
		}

		return result;
	}

	public static String deleteXSSChar( String value ) {
		 String result = null;
		 if( value == null ) return null;

		result = value.replaceAll( "'|\"|\\(|\\)|<|>|;|\\.", "" );

		return result;
	}

	/**
	 * Java impl of javascript encodeURIComponenet ( UTF-8 )
	 *
	 * @param uri : "http://www.example.com"
	 * @return : "http%3A%2F%2Fwww.example.com"
	 */
	public static String encodeURIComponent( String uri ) {
		if( uri == null )
			return null;

		String result = null;
		try {
			result = URLEncoder.encode(uri, "UTF-8")
					.replaceAll("\\%28", "(")
					.replaceAll("\\%29", ")")
					.replaceAll("\\+", "%20")
					.replaceAll("\\%27", "'")
					.replaceAll("\\%21", "!")
					.replaceAll("\\%7E", "~");
		} catch( UnsupportedEncodingException e ) {
			result = uri;
		}

		return result;
	}

	public static String replaceURLQuery( String url, String queryKey, String queryValue ) {
		int idx0 = url.indexOf( '?' );
		if( idx0 < 0 ) {
			if( queryValue != null ) url = url +"?"+ queryKey +"="+ queryValue;
			return url;
		}

		int idx1 = url.indexOf( "?"+ queryKey +"=", idx0 );
		if( idx1 < 0 ) idx1 = url.indexOf( "&"+ queryKey +"=", idx0 );
		if( idx1 < 0 ) {
			if( queryValue != null ) url = url +"&"+ queryKey +"="+ queryValue;
			return url;
		}

		int idx2 = url.indexOf( "&", ++idx1 );
		if( idx2 < 0 ) {
			if( queryValue != null )
				url = url.substring( 0, idx1 ) + queryKey +"="+ queryValue;
			else
				url = url.substring( 0, idx1-1 );
		} else {
			if( queryValue != null )
				url = url.substring( 0, idx1 ) + queryKey +"="+ queryValue + url.substring( idx2 );
			else
				url = url.substring( 0, idx1 ) + url.substring( idx2+1 );

			idx1 = url.indexOf( "&"+ queryKey +"=", idx1 );
			while( idx1 >= 0 ) {
				idx2 = url.indexOf( "&", idx1+1 );
				if( idx2 < 0 )
					return url.substring( 0, idx1 );
				else {
					url = url.substring( 0, idx1 ) + url.substring( idx2 );
					idx1 = url.indexOf( "&"+ queryKey +"=", idx1 );
				}
			}
		}
		return url;
	}

	public static String toHtmlString( Object x ) {
		if( x == null ) return "";
		return toHtmlString( x.toString() );
	}

	/**
	 * String을 Html에서 표시가능한 String으로 변경.
	 * <, &, > 등을 변환해준다.
	 */
	public static String toHtmlString( String x ) {
		if( x == null ) return "";

		StringTokenizer tokenizer = new StringTokenizer( x, "&;\\\"'<>", true );
		StringBuffer sbuf = new StringBuffer();
		while( tokenizer.hasMoreElements() ) {
			String s = tokenizer.nextToken();
			if( "&;\\\"'<>".indexOf(s.charAt(0)) >= 0 )
				sbuf.append( "&#" + (int)(s.charAt(0)) + ";" );
			else
				sbuf.append( s );
		}
		return sbuf.toString();
	}

	public static String toScriptString( Object x ) {
		if( x == null ) return "";
		return toScriptString( x.toString() );
	}

	/**
	 * String을 Script에서 표시가능한 String으로 변경.
	 */
	public static String toScriptString( String x ) {
		return (x == null ? ""
				: x.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&#40;", "\\(")
					.replaceAll("&#41;", "\\)").replaceAll("&#39;", "'").replaceAll("&#34;", "\"").replaceAll("'|\"|/", "\\\\$0"));
	}

	/**
	 * String 에 포함된 특수문자를 ASCII Code 로 변경.
	 */
	public static String toASCIICodeString( String x ) {
		if( x == null ) return null;

		try {
			x = x.replaceAll( java.net.URLDecoder.decode("%00", "UTF-8"), "" );
		} catch( java.io.UnsupportedEncodingException ex ) {}

		StringTokenizer tokenizer = new StringTokenizer( x, ";\\\"'<>", true );
		StringBuffer sbuf = new StringBuffer();
		while( tokenizer.hasMoreElements() ) {
			String s = tokenizer.nextToken();
			if( ";\\\"'<>".indexOf(s.charAt(0)) >= 0 )
				sbuf.append( "%" + String.format("%02X", (int) s.charAt(0)) );
			else
				sbuf.append( s );
		}
		return sbuf.toString();
	}
}
