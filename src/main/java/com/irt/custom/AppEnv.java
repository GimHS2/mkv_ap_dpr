package com.irt.custom;

import java.util.Properties;

/**
 * 
 * CAUTION: currently not working as expected.
 * need to seperate instance() for classloader... currently _instance is static which is JVM.
 * 
 * java.lang.System.properties is JVM backed JVM System properties( by each JVM Level )
 * 
 * com.irt.rbm.RBMSystem.properties is Database backed Application properties.( by each DB SYS_SYSTEM_ENV Level )
 * 
 * com.irt.util.AppEnv.properties is ClassLoader backed Application properties.( by each ClassLoader Level )
 * 
 * com.irt.util.AppEnv.getJvmSystemEnv is to read injected DB SYS_SYSTEM_ENV variable.
 * ( this webapp instance only. not affect to all webapp in tomcat )
 * 
 */
public class AppEnv {
	static private AppEnv _instance = null;
	static public String PROP_APP_ID = "appId";

	static public AppEnv instance() {
		if( _instance == null )
			_instance = new AppEnv();

		return _instance;
	}

	private Properties props;

	protected AppEnv() {
		props = new Properties();
	}

	public void setAppId( String value ) {
		this.setProperty(PROP_APP_ID, value);
	}

	public String getAppId() {
		return this.getProperty(PROP_APP_ID, "");
	}

	public String getProperty( String key ) {
		return props.getProperty(key);
	}

	public String getProperty( String key, String defaultValue ) {
		return props.getProperty(key, defaultValue);
	}

	public void setProperty( String key, String value ) {
		props.setProperty(key, value);
	}

	private String getAppIdMarker() {
		return getAppIdMarker(getAppId());
	}

	public static String getAppIdMarker( String ctxAppId ) {
		return ( ctxAppId == null ? "" : ctxAppId + "#" );
	}

	public static String getAppSystmeEnvKey( String ctxAppId, String systemEnvKey ) {
		return getAppIdMarker(ctxAppId) + systemEnvKey;
	}

	public String getAppSystemEnvKey( String systemEnvKey ) {
		return getAppIdMarker() + systemEnvKey;
	}

	/**
	 * @param systemCode
	 *            : same param as 'RBMSystem.getSystemEnv( systemCode, key );
	 * @param key
	 *            : same param as 'RBMSystem.getSystemEnv( systemCode, key );
	 *            ( DO NOT put appIdMarker in key )
	 * @return property from AppEnv System or null.
	 */
	public String getAppSystemEnv( String systemCode, String key ) {
		return getAppSystemEnv(systemCode, getAppSystemEnvKey(key), null);
	}

	/**
	 * @param systemCode
	 *            : same param as 'RBMSystem.getSystemEnv( systemCode, key );
	 * @param key
	 *            : same param as 'RBMSystem.getSystemEnv( systemCode, key );
	 *            ( DO NOT put appIdMarker in key )
	 * @param defaltValue
	 * @return property from AppEnv System or defaultValue
	 */
	public String getAppSystemEnv( String systemCode, String key, String defaultValue ) {
		if( key != null && key.contains(";") ) {
			return this.getProperty(systemCode + "." + key.replace(";", "."));
		}
		return defaultValue;
	}

	/**
	 * @param systemCode
	 *            : same param as 'RBMSystem.setSystemEnv( systemCode, key );
	 * @param key
	 *            : same param as 'RBMSystem.setSystemEnv( systemCode, key );
	 *            ( DO NOT put appIdMarker in key )
	 * @param value
	 *            : value will be applied only to AppEnv( not RBMSystem )
	 * @return beforeValue
	 */
	public String setAppSystemEnv( String systemCode, String key, String value ) {
		String beforeValue = null;
		if( key != null && key.contains(";") ) {
			String thekey = getAppSystemEnvKey(key);
			String appLevelKey = systemCode + "." + thekey.replace(";", ".");
			beforeValue = this.getProperty(appLevelKey);
			this.setProperty(appLevelKey, value);
			return beforeValue;
		}

		return beforeValue;
	}
}
