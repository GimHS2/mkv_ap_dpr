/*
 *	File Name:	SystemConfig.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/01/01		2.2.0c	DatabaseResourceBundle 기능 추가
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.system;

import com.irt.resbdl.DatabaseMessageBundleControl;
import com.irt.resbdl.DatabaseResourceRepositoryImpl;
import com.irt.sql.SQLHandler;
import com.irt.util.MessageHandler;

import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 *
 */
public class SystemConfig {

	Logger logger = Logger.getLogger(getClass());

	String systemCode, systemName;

	Constructor handlerConstructor;
	DataSource dataSource;

	File tempDirectory;
	Properties properties;
	String[] messageSourceNames;

	protected SystemConfig( SystemConfig systemConfig ) {
		this.systemCode = systemConfig.systemCode;
		this.systemName = systemConfig.systemName;
		this.handlerConstructor = systemConfig.handlerConstructor;
		this.dataSource = systemConfig.dataSource;
		this.tempDirectory = systemConfig.tempDirectory;
		this.properties = systemConfig.properties;
		this.messageSourceNames = systemConfig.messageSourceNames;
	}

	public SystemConfig( String systemCode, String systemName, Class handlerClass, DataSource dataSource, String[] messageSourceNames
						, File tempDirectory, Properties properties ) {
		this.systemCode = systemCode;
		this.systemName = systemName;
		try {
			this.handlerConstructor = handlerClass.getConstructor( Connection.class, MessageHandler.class, TimeZone.class );
		} catch( NoSuchMethodException noSuchMethodEx ) {
			throw new IllegalArgumentException( "illegal handlerClass: "+ noSuchMethodEx.getMessage() );
		}
		this.dataSource = dataSource;
		this.messageSourceNames = messageSourceNames;
		this.tempDirectory = tempDirectory;
		this.properties = properties;
	}

	public SQLHandler createSQLHandler( MessageHandler msghandler ) throws SQLException {
		return createSQLHandler( msghandler, TimeZone.getDefault() );
	}

	public SQLHandler createSQLHandler( MessageHandler msghandler, TimeZone zone ) throws SQLException {
		Connection conn = dataSource.getConnection();
		try {
			if( conn == null ) return null;
			conn.setAutoCommit(false);
		} catch( SQLException sqlEx ) {}

		try {
			return (SQLHandler)handlerConstructor.newInstance( conn, msghandler, zone );
		} catch( IllegalAccessException accessEx ) {
			accessEx.printStackTrace( System.err );
		} catch( InstantiationException instantEx ) {
			instantEx.printStackTrace( System.err );
		} catch( java.lang.reflect.InvocationTargetException invocationEx ) {
			invocationEx.printStackTrace( System.err );
		}

		return null;
	}

	public SQLHandler createSQLHandler( MessageHandler msghandler, int trialCount, long waitMillis ) throws java.sql.SQLException {
		return createSQLHandler( msghandler, TimeZone.getDefault(), trialCount, waitMillis );
	}

	public SQLHandler createSQLHandler( MessageHandler msghandler, TimeZone zone, int trialCount, long waitMillis ) throws java.sql.SQLException {
		SQLHandler handler = createSQLHandler( msghandler, zone );

		try {
			for( int t = 0; handler == null && t < trialCount; t++ ) {
				Thread.sleep( waitMillis );
				handler = createSQLHandler( msghandler, zone );
			}
		} catch( InterruptedException interruptEx ) {}

		return handler;
	}

	public MessageHandler getMessageHandler() {
		return getMessageHandler( Locale.getDefault() );
	}

	public MessageHandler getMessageHandler( Locale locale ) {
		ResourceBundle[] bundles = new ResourceBundle[ messageSourceNames.length ];

		for( int i = 0; i < messageSourceNames.length; i++ ) {
			if( "Y".equals(com.irt.util.Utility2.getJvmSystemEnv("SYS", "DatabaseResource;useMessageResource")) ) {
				String pureName = DatabaseResourceRepositoryImpl.BundleNaming.getPureBaseName(messageSourceNames[i]);
				Locale pureLocale = DatabaseResourceRepositoryImpl.BundleNaming.getLocaleByLang(locale.getLanguage());
				bundles[i] = ResourceBundle.getBundle(pureName, pureLocale, DatabaseMessageBundleControl.getInstance(dataSource));
				if( !(bundles[i] instanceof com.irt.resbdl.DatabaseMessageBundle) ) {
					ResourceBundle.clearCache(this.getClass().getClassLoader());
				}
				bundles[i] = ResourceBundle.getBundle(pureName, pureLocale, DatabaseMessageBundleControl.getInstance(dataSource));
			} else {
				bundles[i] = ResourceBundle.getBundle(messageSourceNames[i], locale);
			}
		}

		com.irt.util.MessageBundle mb = new com.irt.util.MessageBundle( bundles );
		mb.setBundleBaseNames(messageSourceNames);
		return mb;
	}

	public String getProperty( String key ) {
		if( properties == null ) return null;
		return properties.getProperty( key );
	}

	public String getProperty( String key, String defaultValue ) {
		if( properties == null ) return defaultValue;
		return properties.getProperty( key, defaultValue );
	}

	public String getSystemCode() {
		return systemCode;
	}

	public String getSystemName() {
		return systemName;
	}

	public File getTemporaryDirectory() {
		return tempDirectory;
	}
}
