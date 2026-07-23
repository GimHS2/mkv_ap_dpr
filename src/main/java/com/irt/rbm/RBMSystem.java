/*
 *	File Name:	RBMSystem.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/06/20		2.2.2	initialized() 추가
 *	stghr12		2008/05/31		2.2.1	*_GLN -> RETAILGLN_*
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getDefaultResourceBundle() -> getDefaultResourceBundleName()
 *										usingSystem() 추가
 *	stghr12		2007/04/30		2.1.1	EMART_GLN, HOMEEVER_GLN, LOTTEMART_GLN, TESCO_GLN 추가
 *	stghr12		2006/12/01		2.1.0	getSystemEnvDouble(), removeSystemEnv() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.rbm;

import com.irt.data.DataException;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import java.sql.*;
import java.util.Properties;

/**
 *
 */
public class RBMSystem {
	private static Properties properties = null;

	public final static String RETAILGLN_EMART			= "8800900169010";
	public final static String RETAILGLN_GSMART			= "8800900225013";
	public final static String RETAILGLN_GSSUPER		= "8800900227505";
	public final static String RETAILGLN_HOMEEVER		= "8808573000012";
	public final static String RETAILGLN_LOTTEMART		= "8800900859010";
	public final static String RETAILGLN_TESCO			= "8800900009002";

	public static String getDefaultResourceBundleName() {
		return "com.irt.rbm.mesg.RBMMessages";
	}

	public static String getSystemEnv( String systemCode, String key ) {
		return getSystemEnv( systemCode, key, null );
	}

	public static String getSystemEnv( String systemCode, String key, String defaultValue ) {
		String value = properties.getProperty( systemCode +"."+ key );
		return( value == null ? defaultValue : value );
	}

	public static boolean getSystemEnvBool( String systemCode, String key, boolean defaultValue ) {
		try {
			String value = properties.getProperty( systemCode +"."+ key ).toUpperCase();
			return( "Y".equals(value) || "YES".equals(value) || "TRUE".equals(value) || "1".equals(value) );
		} catch( NullPointerException nullEx ) {
			return defaultValue;
		}
	}

	public static double getSystemEnvDouble( String systemCode, String key, double defaultValue ) {
		String value = properties.getProperty( systemCode +"."+ key );
		return( value != null ? Double.parseDouble(value) : defaultValue );
	}

	public static int getSystemEnvInt( String systemCode, String key, int defaultValue ) {
		String value = properties.getProperty( systemCode +"."+ key );
		return( value != null ? Integer.parseInt(value) : defaultValue );
	}

	public static boolean initialized() {
		return( properties != null );
	}

	public static synchronized void initSystemEnv( SQLHandler handler ) throws SQLException {
		if( properties != null ) return;
		properties = loadSystemEnv( handler );
		com.irt.rbm.sys.CategoryCode.initCategory();
	}

	private static Properties loadSystemEnv( SQLHandler handler ) throws SQLException {
		Properties properties = new Properties();

		Connection conn = handler.getConnection();
		PreparedStatement pstmt = conn.prepareStatement( "SELECT SYSTEM_CD FROM SYS_SYSTEM" );
		ResultSet rset = null;
		try {
			rset = pstmt.executeQuery();
			while( rset.next() )
				properties.setProperty( rset.getString(1), "Y" );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}

		pstmt = conn.prepareStatement( "SELECT SYSTEMCD, NAME, VALUE FROM SYS_SYSTEM_ENV" );
		rset = null;
		try {
			rset = pstmt.executeQuery();
			while( rset.next() ) {
				String systemCode = rset.getString(1);
				String name = rset.getString(2);
				String value = rset.getString(3);

				int idx1 = 0, idx2 = 0;
				do {
					idx2 = value.indexOf( ';', idx1 );
					while( idx2 >= 0 && (idx2+1) < value.length() && value.charAt(idx2+1) == ';' )
						idx2 = value.indexOf( ';', idx2+2 );

					String[] key_n_value;
					if( idx2 < 0 )
						key_n_value = value.substring(idx1).replaceAll(";;", ";").split( "=", 2 );
					else
						key_n_value = value.substring(idx1, idx2).replaceAll(";;", ";").split( "=", 2 );
					if( key_n_value.length == 2 )
						properties.setProperty( systemCode +"."+ name +";"+ key_n_value[0], key_n_value[1] );
					idx1 = idx2 + 1;
				} while( idx1 > 0 );
				properties.setProperty( systemCode +"."+ name, value );
			}
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}

		return properties;
	}

	public static boolean usingSystem( String systemCode ) {
		return ( properties.getProperty(systemCode) != null );
	}

	public static synchronized void reloadSystemEnv( SQLHandler handler ) throws SQLException {
		properties = loadSystemEnv( handler );
	}

	public static void removeSystemEnv( SQLHandler handler, String systemCode, String key ) throws DataException, SQLException {
		SQLManager.callStatement( handler, "call pkSYSStandard.pRemoveSystemEnv(?, ?)", systemCode, key );
		if( properties == null )
			initSystemEnv( handler );
		properties.remove( systemCode +"."+ key );
	}

	public static void setSystemEnv( SQLHandler handler, String systemCode, String key, String value ) throws DataException, SQLException {
		SQLManager.callStatement( handler, "call pkSYSStandard.pPutSystemEnv(?, ?, ?)", systemCode, key, value );
		if( properties == null )
			initSystemEnv( handler );
		properties.setProperty( systemCode +"."+ key, value );
	}
}
