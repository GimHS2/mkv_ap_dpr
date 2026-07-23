/*
 *	File Name:	SystemConfigManager.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2016/10/31		2.2.3	Java Document 주석 추가, Generic Type warning 수정
 *	stghr12		2009/06/30		2.2.2	JNDI 읽는 방법 수정
 *	stghr12		2008/11/28		2.2.1	load() 수정
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.system;

import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;

/**
 * File이나 Stream, Properties로 부터 systemConfig를 생성하고 System 별로 systemConfig를 관리 하는 Class.
 */
public class SystemConfigManager {
	Properties properties;
	Map<String, SystemConfig> systemMap;

	SystemConfigManager( Properties properties, Map<String, DataSource> sourceMap, Map<String, SystemConfig> systemMap ) {
		this.properties = properties;
		this.systemMap = systemMap;
	}

	public String getProperty( String propertyKey ) {
		return properties.getProperty( propertyKey );
	}

	public String getProperty( String propertyKey, String defaultValue ) {
		return properties.getProperty( propertyKey, defaultValue );
	}

	public SystemConfig getSystemConfig( String systemCode ) {
		return systemMap.get( systemCode );
	}

	public Map<String, SystemConfig> getSystemConfigMap() {
		return systemMap;
	}

	public static SystemConfigManager load( String fileName ) throws Exception {
		java.io.InputStream inputStream = new java.io.FileInputStream( fileName );
		try {
			return load( inputStream );
		} finally {
			try { inputStream.close(); } catch( Exception ignored ) {}
		}
	}

	public static SystemConfigManager load( java.io.InputStream inputStream ) throws Exception {
		java.util.Properties properties = new java.util.Properties();

		properties.load( inputStream );

		return load( properties );
	}

	public static SystemConfigManager load( Properties properties ) throws Exception {
		ClassLoader classLoader = SystemConfigManager.class.getClassLoader();
		javax.naming.InitialContext context = new javax.naming.InitialContext();

		// sourcePropertyMap, systemPropertyMap 준비
		Properties defaultProperties = new Properties();
		Map<String, Properties> sourcePropertyMap = new java.util.HashMap<String, Properties>();
		Map<String, Properties> systemPropertyMap = new java.util.HashMap<String, Properties>();
		for( Map.Entry<Object, Object> entry : properties.entrySet() ) {
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();

			if( key.startsWith("DataSource.") ) {
				key = key.substring( "DataSource.".length() );
				int index = key.indexOf( '.' );
				if( index < 0 ) continue;

				String sourceName = key.substring( 0, index );
				key = key.substring( index + 1 );

				Properties options = sourcePropertyMap.get( sourceName );
				if( options == null ) sourcePropertyMap.put( sourceName, options = new Properties() );

				options.setProperty( key, value );
			} else if( key.indexOf("System.") > 0 ) {
				int index = key.indexOf( "System." );
				String systemCode = key.substring( 0, index );
				key = key.substring( index + "System.".length() );

				Properties options = systemPropertyMap.get( systemCode );
				if( options == null ) systemPropertyMap.put( systemCode, options = new Properties() );

				options.setProperty( key, value );
			} else
				defaultProperties.put( key, value );
		}

		// sourceMap, systemMap 생성
		Map<String, DataSource> sourceMap = new java.util.HashMap<String, DataSource>();
		Map<String, SystemConfig> systemMap = new java.util.HashMap<String, SystemConfig>();
		for( Map.Entry<String, Properties> entry : systemPropertyMap.entrySet() ) {
			Properties options = entry.getValue();

			String systemCode = entry.getKey();
			String systemName = options.getProperty( "systemName", defaultProperties.getProperty("systemName") );
			String handlerClassName = options.getProperty( "SQLHandler", defaultProperties.getProperty("SQLHandler") );
			String messageSourceName = options.getProperty( "messageSource", defaultProperties.getProperty("messageSource") );
			String tempDirectory = options.getProperty( "tempDirectory", defaultProperties.getProperty("tempDirectory") );

			String jndi = options.getProperty( "jndi" );
			String sourceName = options.getProperty( "dataSource" );
			if( jndi == null && sourceName == null ) {
				jndi = defaultProperties.getProperty( "jndi" );
				sourceName = defaultProperties.getProperty( "dataSource" );
			}
			if( systemName == null )
				throw new IllegalArgumentException( systemCode +"System.systemName cannot be null." );
			else if( handlerClassName == null )
				throw new IllegalArgumentException( systemCode +"System.SQLHandler cannot be null." );
			else if( messageSourceName == null )
				throw new IllegalArgumentException( systemCode +"System.messageSource cannot be null." );
			else if( tempDirectory == null )
				throw new IllegalArgumentException( systemCode +"System.tempDirectory cannot be null." );
			else if( jndi == null && sourceName == null )
				throw new IllegalArgumentException( systemCode +"System.dataSource cannot be null." );

			properties = new Properties( defaultProperties );
			properties.putAll( options );
			properties.remove( "jndi" );
			properties.remove( "systemName" );
			properties.remove( "SQLHandler" );
			properties.remove( "connection" );
			properties.remove( "messageSource" );
			properties.remove( "tempDirectory" );

			DataSource source = null;
			if( sourceName != null ) {
				source = sourceMap.get( sourceName );
				if( source == null ) {
					source = BasicDataSourceFactory.createDataSource( sourcePropertyMap.get(sourceName) );
					sourceMap.put( sourceName, source );
				}
			} else if( jndi != null ) {
				try {
					source = (DataSource)context.lookup( "java:/comp/env/" + jndi );
				} catch( javax.naming.NamingException namingEx ) {
					source = (DataSource)context.lookup( jndi );
				}
				if( source == null )
					throw new IllegalArgumentException( jndi +" dataSource cannot found." );
			}

			SystemConfig systemConfig = new SystemConfig (
				systemCode, systemName
				, classLoader.loadClass( handlerClassName )
				, source
				, messageSourceName.split( ";" )
				, new java.io.File( tempDirectory )
				, properties
			);
			systemMap.put( systemCode, systemConfig );
		}

		return new SystemConfigManager( defaultProperties, sourceMap, systemMap );
	}
}
