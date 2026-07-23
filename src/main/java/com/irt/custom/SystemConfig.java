/*
 *	File Name:	SystemConfig.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	com.irt.servlet.AbstractSystemConfig 상속으로 변경
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										Locale 및 JNDI 적용
 *	stghr12		2007/04/30		2.1.0	version up
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2002/04/15		1.0.0	create
 *
**/

package com.irt.custom;

import com.irt.sql.SQLHandler;
import com.irt.system.SessionManager;
import com.irt.system.SessionManagerException;
import com.irt.util.MessageHandler;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class SystemConfig extends com.irt.servlet.AbstractSystemConfig {
	public final static int DEFAULT_LOGIN_TIME			= 24 * 60;	// 하루
	public final static int DEFAULT_SESSION_TIME		= 90;		// 90 minute
	public final static String DEFAULT_SESSION_KEY		= "chlrhdmldlstod";

	static SystemConfig defaultSystemConfig, loginSystemConfig;
	static Map<String, SystemConfig> systemMap;

	SystemConfig( com.irt.system.SystemConfig systemConfig ) {
		super( systemConfig );
	}

	public SessionManager createSessionManager( HttpServletRequest req, MessageHandler msghandler ) throws SessionManagerException {
		SQLHandler handler = null;
		try {
			handler = loginSystemConfig.createSQLHandler( msghandler );
			if( handler == null ) return null;

			return new com.irt.rbm.SessionMng( getSessionId(req), req.getRemoteAddr(), handler, getMaximumSessionTime(), DEFAULT_LOGIN_TIME );
		} catch( SQLException sqlEx ) {
			try { if( handler != null ) handler.close(); } catch( Exception ignored ) {}
			throw new SessionManagerException( sqlEx );
		} catch( RuntimeException runtimeEx ) {
			try { if( handler != null ) handler.close(); } catch( Exception ignored ) {}
			throw runtimeEx;
		}
	}

	public SQLHandler createSQLHandler( MessageHandler msghandler ) throws SQLException {
		SQLHandler handler = super.createSQLHandler( msghandler );
		if( handler != null && "Y".equals(getProperty("debugSQL", "N")) ) handler.enableDebugging();

		return handler;
	}

	public SQLHandler createSQLHandler( MessageHandler msghandler, TimeZone zone ) throws SQLException {
		SQLHandler handler = super.createSQLHandler( msghandler, zone );
		if( handler != null && "Y".equals(getProperty("debugSQL", "N")) ) handler.enableDebugging();

		return handler;
	}

	public static SystemConfig getInstance( String systemCode ) {
		SystemConfig systemConfig = systemMap.get( systemCode );
		return( systemConfig == null ? defaultSystemConfig : systemConfig );
	}

	public MessageHandler getMessageHandler() {
		return new com.irt.custom.MessageBundle( super.getMessageHandler() );
	}

	public MessageHandler getMessageHandler( Locale locale ) {
		return new com.irt.custom.MessageBundle( super.getMessageHandler(locale) );
	}

	static void initialize( java.util.Properties properties ) throws Exception {
		com.irt.system.SystemConfigManager systemConfigManager = com.irt.system.SystemConfigManager.load( properties );

		systemMap = new java.util.HashMap<String, SystemConfig>();
		for( com.irt.system.SystemConfig systemConfig : systemConfigManager.getSystemConfigMap().values() )
			systemMap.put( systemConfig.getSystemCode(), new SystemConfig(systemConfig) );

		String defaultSystemCode = systemConfigManager.getProperty( "SystemEnv.default", "RBM" );
		String loginSystemCode = systemConfigManager.getProperty( "SystemEnv.login", defaultSystemCode );

		defaultSystemConfig = systemMap.get( defaultSystemCode );
		loginSystemConfig = systemMap.get( loginSystemCode );
	}
}
