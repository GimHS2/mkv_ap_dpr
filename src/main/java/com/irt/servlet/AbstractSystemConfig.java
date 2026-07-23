/*
 *	File Name:	AbstractSystemConfig.java
 *	Version:	2.2.2c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/09/29		2.2.2c	isSecure 추가
 *	hankalam	2020/03/31		2.2.2c	cookieOption, getCookieOption() 추가
 *	jbaek		2019/01/30		2.2.1c	fallback으로 pageProperty가 external file인 경우에도 로드되도록 변경.
 *	jbaek		2017/02/28		2.2.1c	baseURL이 "/"로 끝나도록 강제변경.
 *	stghr12		2008/08/29		2.2.1	getDefaultPageProperty() 추가
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.servlet;

import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.irt.system.SessionManager;
import com.irt.system.SessionManagerException;
import com.irt.util.MessageHandler;

/**
	*
	*/
public abstract class AbstractSystemConfig extends com.irt.system.SystemConfig implements com.irt.servlet.SystemConfig {
	String sessionKey, cookieOption, isSecure;
	int maximumSessionTime;
	String domain, baseURL, classURL, homepageURLs[], jspPath, encoding;
	Properties defaultPageProperty;

	protected AbstractSystemConfig( com.irt.system.SystemConfig systemConfig ) {
		super( systemConfig );

		this.domain = getProperty( "domain" );
		this.baseURL = getProperty( "baseURL" );
		if( baseURL != null && !baseURL.endsWith("/") ) this.baseURL = baseURL + "/";

		this.classURL = getProperty( "classURL", baseURL + "servlet" );
		this.homepageURLs = new String[] { getProperty("homeURL"), getProperty("homeURL.login") };

		this.jspPath = getProperty( "jspPath", "/jsp" );
		this.encoding = getProperty( "encoding" );

		this.sessionKey = getProperty( "sessionKey" );
		this.cookieOption = getProperty( "cookieOption", "" );
		this.isSecure = getProperty( "isSecure", "N" );

		try {
			this.maximumSessionTime = Integer.parseInt( getProperty("sessionTime") );
		} catch( NumberFormatException numEx ) {
			throw new IllegalArgumentException( "illegal sessionTime '"+ getProperty("sessionTime") +"'." );
		}

		this.defaultPageProperty = new Properties();
		String pagePropertyName = getProperty( "pageProperty", "pages.properties" );
		if( pagePropertyName != null ) {
			java.io.InputStream inputStream = AbstractSystemConfig.class.getClassLoader().getResourceAsStream( pagePropertyName );// WEB-INF/classes/pages.properties
			if( inputStream == null ) {
				try {
					inputStream = new java.io.FileInputStream( pagePropertyName );//external pageProperty
				} catch( FileNotFoundException ignored ) {
				}
				if( inputStream == null ) {
					Logger.getRootLogger().warn( "illegal pageProperty '"+ pagePropertyName +"'. trying next default defined." );
				}
			}
			try {
				defaultPageProperty.load( inputStream );
			} catch( java.io.IOException ioEx ) {
				throw new IllegalArgumentException( "illegal pageProperty '"+ pagePropertyName +"': "+ ioEx.getMessage() );
			} finally {
				try { inputStream.close(); } catch( Exception ex ) {}
			}
		}
	}

	@Override
	public abstract SessionManager createSessionManager( HttpServletRequest req, MessageHandler msghandler ) throws SessionManagerException;

	@Override
	public String getBaseURL( Locale locale ) {
		return getProperty( "baseURL."+ locale.getLanguage(), baseURL );
	}

	@Override
	public String getClassURL() {
		return classURL;
	}

	@Override
	public String getCookieOption() {
		return cookieOption;
	}

	@Override
	public Properties getDefaultPageProperty() {
		return defaultPageProperty;
	}

	@Override
	public String getDomain() {
		return domain;
	}

	@Override
	public String getEncoding( Locale locale ) {
		return getProperty( "encoding."+ locale.getLanguage(), encoding );
	}

	@Override
	public String getHomepageURL( Locale locale, boolean login ) {
		if( login ) {
			String homepageURL = getProperty( "homeURL."+ locale.getLanguage() +".login" );
			if( homepageURL == null ) homepageURL = getProperty( "homeURL."+ locale.getLanguage(), homepageURLs[1] );

			return homepageURL;
		} else
			return getProperty( "homeURL."+ locale.getLanguage(), homepageURLs[0] );
	}

	public String getIsSecure() {
		return isSecure;
	}

	@Override
	public String getJspPath() {
		return jspPath;
	}

	@Override
	public int getMaximumSessionTime() {
		return maximumSessionTime;
	}

	public String getSessionId( HttpServletRequest req ) {
		javax.servlet.http.Cookie[] cookies = req.getCookies();
		if( cookies != null ) {
			for( int i = 0; i < cookies.length; i++ )
				if( sessionKey.equals(cookies[i].getName()) )
					return cookies[i].getValue();
		}

		return null;
	}

	@Override
	public String getSessionKey() {
		return sessionKey;
	}
}
