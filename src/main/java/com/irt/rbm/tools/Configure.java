/*
 *	File Name:	Configure.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/08/31		2.2.2	SystemConfig 2개 받는 생성자 지원
 *	stghr12		2010/05/31		2.2.1	ProcessRunnerExaminer 로직 추가
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.system.SystemConfig;
import com.irt.system.SystemConfigManager;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class Configure {
	SystemConfigManager systemConfigManager;
	Map<String, ProcessRunner> runnerMap;

	public Configure() {
		this.systemConfigManager = null;
		this.runnerMap = new java.util.HashMap<String, ProcessRunner>();
	}

	public void load( String fileName ) throws Exception {
		java.io.InputStream inputStream = new java.io.FileInputStream( fileName );
		try {
			load( inputStream );
		} finally {
			try { inputStream.close(); } catch( Exception ignored ) {}
		}
	}

	public void load( java.io.InputStream inputStream ) throws Exception {
		java.util.Properties properties = new java.util.Properties();

		properties.load( inputStream );

		load( properties );
	}

	public void load( Properties properties ) throws Exception {
		// runnerPropertyMap 준비

		Map<String, Properties> runnerPropertyMap = com.irt.util.Utility.extractPropertiesMap( properties, "ProcessRunner." );

		this.runnerMap = new java.util.HashMap<String, ProcessRunner>();
		this.systemConfigManager = SystemConfigManager.load( properties );

		if( runnerPropertyMap == null ) return;

		// runnerMap 설정
		ClassLoader classLoader = com.irt.rbm.tools.Configure.class.getClassLoader();
		for( Map.Entry<String, Properties> entry : runnerPropertyMap.entrySet() ) {
			String runnerName = entry.getKey();
			Properties options = entry.getValue();

			// systemConfig
			SystemConfig systemConfig = null;
			String systemConfigName = (String)options.remove( "system" );
			if( systemConfigName != null ) {
				systemConfig = systemConfigManager.getSystemConfig( systemConfigName );
				if( systemConfig == null )
					throw new IllegalArgumentException( systemConfigName +"System cannot be found." );
			}

			// runner
			ProcessRunner runner = null;
			Class runnerClass = classLoader.loadClass( (String)options.remove("className") );
			try {
				Constructor constructor = runnerClass.getConstructor( SystemConfig.class, File.class );
				String directory = (String)options.remove( "directory" );

				if( systemConfigName == null )
					throw new IllegalArgumentException( "ProcessRunner."+ runnerName +".system cannot be null." );
				else if( directory == null )
					throw new IllegalArgumentException( "ProcessRunner."+ runnerName +".directory cannot be null." );

				runner = (ProcessRunner)constructor.newInstance( systemConfig, new File(directory) );
			} catch( NoSuchMethodException noSuchMethodEx ) {}

			if( runner == null ) {
				try {
					Constructor constructor = runnerClass.getConstructor( SystemConfig.class, SystemConfig.class );
					if( systemConfigName == null )
						throw new IllegalArgumentException( "ProcessRunner."+ runnerName +".system cannot be null." );

					String systemConfigNameTo = (String)options.remove( "systemTo" );
					SystemConfig systemConfigTo = null;
					if( systemConfigNameTo == null )
						throw new IllegalArgumentException( "ProcessRunner."+ runnerName +".systemTo cannot be null." );
					else {
						systemConfigTo = systemConfigManager.getSystemConfig( systemConfigNameTo );
						if( systemConfig == null )
							throw new IllegalArgumentException( systemConfigNameTo +"System cannot be found." );
					}

					runner = (ProcessRunner)constructor.newInstance( systemConfig, systemConfigTo );
				} catch( NoSuchMethodException noSuchMethodEx ) {}
			}

			if( runner == null ) {
				try {
					Constructor constructor = runnerClass.getConstructor( SystemConfig.class );
					if( systemConfigName == null )
						throw new IllegalArgumentException( "ProcessRunner."+ runnerName +".system cannot be null." );

					runner = (ProcessRunner)constructor.newInstance( systemConfig );
				} catch( NoSuchMethodException noSuchMethodEx ) {}
			}

			if( runner == null ) {
				runner = (ProcessRunner)runnerClass.getConstructor().newInstance();
				try {
					Method method = runnerClass.getMethod( "setSystemConfig", SystemConfig.class );
					method.invoke( runner, systemConfig );
				} catch( NoSuchMethodException ignored ) {}
			}

			// runner.set()
			com.irt.util.PropertySetter.setProperties( runner, options );

			// runner.setConfigure()
			try {
				Method method = runnerClass.getMethod( "setConfigure", Configure.class );
				method.invoke( runner, this );
			} catch( NoSuchMethodException ignored ) {}

			runnerMap.put( runnerName, runner );
		}
	}

	public ProcessRunner getProcessRunner( String runnerName ) {
		return runnerMap.get( runnerName );
	}

	public Collection<ProcessRunner> getProcessRunners() {
		return runnerMap.values();
	}

	public String getProperty( String propertyKey ) {
		return systemConfigManager.getProperty( propertyKey );
	}

	public String getProperty( String propertyKey, String defaultValue ) {
		return systemConfigManager.getProperty( propertyKey, defaultValue );
	}

	public SystemConfig getSystemConfig( String systemCode ) {
		return systemConfigManager.getSystemConfig( systemCode );
	}
}
