/*
 *	File Name:	CountryHierarchyCondition.java
 *	Version:	2.2.0
 *
 *	Description:
 * 		WORKING Class
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2009/06/30		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.sql.*;
import java.sql.SQLException;
import java.sql.*;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class CountryHierarchyCondition {
	private SQLHandler handler;

	private static Map<String, Map<String, Object>> conditionMap = null;

	public static Map<String, Map<String, Object>> getAllHierarchyCondition() {
		return conditionMap;
	}

	public static Map<String, Object> getCountryHierarchyCondition( SQLHandler handler, String countryCode ) throws SQLException {
		if( conditionMap == null )
			loadHierarchyCondition( handler );

		return conditionMap.get( countryCode );
	}

	public static Map<String, Object> getCountryHierarchyCondition( String countryCode ) {
		return (conditionMap != null ? conditionMap.get( countryCode ) : null );
	}

	public static String[] getHierarchyCondition( SQLHandler handler, String countryCode ) throws SQLException {
		Map<String, Object> map = getCountryHierarchyCondition( handler, countryCode );
		if( map != null )
			return (String [])map.get( "hierarchyCondition" );

		return null;
	}

	public static String[] getHierarchyCondition( String countryCode ) {
		Map<String, Object> map = getCountryHierarchyCondition( countryCode );

		return ( map != null ? (String [])map.get("hierarchyCondition") : null );
	}

	public static String getDefaultHierarchyLevel( SQLHandler handler, String countryCode ) throws SQLException {
		Map<String, Object> map = getCountryHierarchyCondition( handler, countryCode );
		if( map != null )
			return (String)map.get( "defaultHierarchyLevel" );

		return null;
	}

	public static String getDefaultHierarchyLevel( String countryCode ) {
		Map<String, Object> map = getCountryHierarchyCondition( countryCode );

		return ( map != null ? (String)map.get("defaultHierarchyLevel") : null );
	}

	public static void loadHierarchyCondition( SQLHandler handler ) throws SQLException {
		CountryHierarchyCondition.loadHierarchyCondition( handler, null );
	}

	public static void loadHierarchyCondition( SQLHandler handler, String countryCode ) throws SQLException {
		Connection conn = handler.getConnection();

		String query = "SELECT COUNTRY_CD, DEF_HIERARCHY_LEVEL, HERARCHY_COND FROM DPR_COUNTRY";
		if( countryCode != null )
			query += " WHERE COUNTRY_CD = '" + countryCode + "'";

		PreparedStatement pstmt = conn.prepareStatement( query );

		conditionMap = new java.util.HashMap<String, Map<String, Object>> ();
		ResultSet rset = null;
		try {
			Map<String, Object> countryCondition = new java.util.HashMap<String, Object> ();
			rset = pstmt.executeQuery();
			while( rset.next() ) {
				String code = rset.getString( 1 );
				String defaultHierarchyLevel = rset.getString( 2 );
				String hierarchyCondition = rset.getString( 3 );
				String[] conditionValues = null;
				if( hierarchyCondition != null && hierarchyCondition.length() > 0 )
					conditionValues = hierarchyCondition.split( ";" );

				countryCondition.put( "defaultHierarchyLevel", defaultHierarchyLevel );
				countryCondition.put( "hierarchyCondition", conditionValues );

				conditionMap.put( code, countryCondition );
			}
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public static void reloadHierarchyCondition( SQLHandler handler ) throws SQLException {
		loadHierarchyCondition( handler, null );
	}

	public static void removeCondition( String countryCode ) {
		if( conditionMap != null )
			conditionMap.remove( countryCode );
	}

	public static void setDefaultHierarchyLevel( SQLHandler handler, String countryCode, String level ) throws SQLException {
		Map<String, Object> map = getCountryHierarchyCondition( handler, countryCode );
		if( map == null )
			map = new java.util.HashMap<String, Object> ();

		if( map != null ) {
			map.put( "defaultHierarchyLevel", level );
			conditionMap.put( countryCode, map );
		}
	}

	public static void setHierarchyCondition( SQLHandler handler, String countryCode, String code ) throws SQLException {
		Map<String, Object> map = getCountryHierarchyCondition( handler, countryCode );
		if( map == null )
			map = new java.util.HashMap<String, Object> ();

		if( map != null ) {
			String[] codes = null;
			if( code != null )
				codes = code.split( ";" );
			map.put( "hierarchyCondition", codes );

			conditionMap.put( countryCode, map );
		}
	}

	public static void setValues( SQLHandler handler, String countryCode, String level, String code ) throws SQLException {
		CountryHierarchyCondition.setDefaultHierarchyLevel( handler, countryCode, level );
		CountryHierarchyCondition.setHierarchyCondition( handler, countryCode, code );
	}
}
