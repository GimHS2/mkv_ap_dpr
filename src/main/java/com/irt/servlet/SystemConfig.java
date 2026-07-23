/*
	*	File Name:	SystemConfig.java
	*	Version:	2.2.3c
	*
	*	Description:
	*
	*	Note:
	*
	*	Modified	(YYYY/MM/DD)	Ver		Content
	*	hankalam	2020/09/29		2.2.3c	getIsSecure() 추가
	*	hankalam	2020/03/31		2.2.3	getCookieOption() 추가
	*	stghr12		2008/08/29		2.2.2	getDefaultPageProperty() 추가
	*	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
	*										getTemporaryDirectory(): return 타입 File로 변경
	*										initSystemEnvironment() 삭제
	*	stghr12		2007/11/30		2.2.0	createSessionManager(req) -> createSessionManager(req, msghandler)
	*										createSQLHandler() -> createSQLHandler(msghandler), createSQLHandler(msghandler, zone)
	*										getBaseURL() -> getBaseURL(locale)
	*										getEncoding() -> getEncoding(locale)
	*										getHomepageURL(login) -> getHomepageURL(locale, login)
	*										getMessageHandler( locale ) 추가
	*										getSystemLogPrinter(), getUserLogPrinter() 삭제
	*	stghr12		2006/12/01		2.1.0	initSystemEnvironment() 추가
	*	stghr12		2006/02/28		2.0.0	create(SystemEnvironment -> SystemConfig)
	*
**/

package com.irt.servlet;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.irt.sql.SQLHandler;
import com.irt.system.SessionManager;
import com.irt.system.SessionManagerException;
import com.irt.util.MessageHandler;

/**
 *
 */
public interface SystemConfig {
	/**
	 * SessionManager를 생성.
	 * @return Connection을 얻지 못했을 경우 null return.
	 */
	public SessionManager createSessionManager( HttpServletRequest req, MessageHandler msghandler ) throws SessionManagerException;

	/**
	 * SQLHandler를 생성.
	 * @return Connection을 얻지 못했을 경우 null return.
	 */
	public SQLHandler createSQLHandler( MessageHandler msghandler ) throws SQLException;

	/**
	 * SQLHandler를 생성.
	 * @return Connection을 얻지 못했을 경우 null return.
	 */
	public SQLHandler createSQLHandler( MessageHandler msghandler, java.util.TimeZone zone ) throws SQLException;

	public String getBaseURL( Locale locale );

	public String getClassURL();

	public String getCookieOption();

	public Properties getDefaultPageProperty();

	public String getDomain();

	public String getEncoding( Locale locale );

	public String getHomepageURL( Locale locale, boolean login );

	public String getIsSecure();

	public String getJspPath();

	public int getMaximumSessionTime();

	public MessageHandler getMessageHandler();

	public MessageHandler getMessageHandler( Locale locale );

	public String getProperty( String key );

	public String getProperty( String key, String defaultValue );

	public String getSessionKey();

	public String getSystemCode();

	public String getSystemName();

	public java.io.File getTemporaryDirectory();
}
