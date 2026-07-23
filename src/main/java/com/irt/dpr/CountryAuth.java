/*
 *	File Name:	CountryAuth.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class CountryAuth extends com.irt.rbm.ManipulableManagerImpl {
	public final static String DEFAULT_AUTHORIZATIONVALUE   = "YYYYY";

	private final static Table table = Schema.findTable( Schema.DPR_COUNTRY_AUTH );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_COUNTRY_AUTH );

	public CountryAuth( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String countryCode, String uniqId ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object> ();
		primaryMap.put( "countryCode", countryCode );
		primaryMap.put( "uniqId", uniqId );

		return primaryMap;
	}
}
