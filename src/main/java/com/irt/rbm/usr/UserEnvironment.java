/*
 *	File Name:	UserEnvironment.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.0	create
 *
**/

package com.irt.rbm.usr;

import com.irt.data.DataException;
import com.irt.rbm.RBMSystem;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public abstract class UserEnvironment {
	boolean usingSystemEnv;
	SQLHandler handler;

	UserEnvironment( SQLHandler handler ) {
		this.handler = handler;
	}

	public Map<String, Object> getDefaultLocaleMap( java.util.Locale locale ) throws SQLException {
		String countryCode = getValue( "SYS", "Default;CountryCode", locale.getCountry() );
		String languageCode = getValue( "SYS", "Default;LanguageCode", locale.getLanguage() );
		String currencyCode = getValue( "SYS", "Default;CurrencyCode" );

		Object languageName = null;
		if( languageCode != null ) {
			languageName = SQLManager.getObjectValue( handler, "SELECT LANG_NAME FROM SYS_LANG WHERE LANG_CD = ?", languageCode );
			if( languageName == null ) languageCode = null;
		}

		Object countryName = null;
		if( countryCode != null ) {
			countryName = SQLManager.getObjectValue( handler, "SELECT COUNTRY_NAME FROM SYS_COUNTRY WHERE COUNTRY_CD = ?", countryCode );
			if( countryName == null )
				countryCode = null;
			else if( currencyCode == null ) {
				locale = new java.util.Locale( "en", countryCode );
				try {
					currencyCode = java.util.Currency.getInstance(locale).getCurrencyCode();
				} catch( Exception ex ) {}
			}
		}

		Map<String, Object> localeMap = null;
		if( currencyCode != null ) {
			localeMap = SQLManager.getRecordMap( handler, null,
				"SELECT CURR_NAME \"currencyName\", CURR_SYMBOL \"currencySymbol\" FROM SYS_CURRENCY WHERE CURR_CD = ?"
			, currencyCode );
			if( localeMap != null ) localeMap.put( "currencyCode", currencyCode );
		}
		if( localeMap == null ) localeMap = new java.util.HashMap<String, Object>();

		if( countryCode != null ) {
			localeMap.put( "countryCode", countryCode );
			localeMap.put( "countryName", countryName );
		}
		if( languageCode != null ) {
			localeMap.put( "languageCode", languageCode );
			localeMap.put( "languageName", languageName );
		}

		return localeMap;
	}

	public String getValue( String systemCode, String key ) throws SQLException {
		String value = handleGetValue( systemCode, key );
		return ( usingSystemEnv && value == null ? RBMSystem.getSystemEnv(systemCode, key) : value );
	}

	public String getValue( String systemCode, String key, String defaultValue ) throws SQLException {
		String value = getValue( systemCode, key );
		return( value == null ? defaultValue : value );
	}

	public boolean getValue( String systemCode, String key, boolean defaultValue ) throws SQLException {
		String value = getValue( systemCode, key );
		if( value == null ) return defaultValue;

		return( "Y".equals(value) || "YES".equals(value) || "TRUE".equals(value) || "1".equals(value) );
	}

	public double getValue( String systemCode, String key, double defaultValue ) throws SQLException {
		String value = getValue( systemCode, key );
		return( value != null ? Double.parseDouble(value) : defaultValue );
	}

	public int getValue( String systemCode, String key, int defaultValue ) throws SQLException {
		String value = getValue( systemCode, key );
		return( value != null ? Integer.parseInt(value) : defaultValue );
	}

	public abstract String handleGetValue( String systemCode, String key ) throws SQLException;

	public abstract void removeValue( String systemCode, String key ) throws DataException, SQLException;

	public abstract void setValue( String systemCode, String key, String value ) throws DataException, SQLException;

	public void setUsingSystemEnv( boolean usingSystemEnv ) {
		this.usingSystemEnv = usingSystemEnv;
	}
}
