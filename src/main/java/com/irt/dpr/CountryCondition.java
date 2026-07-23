/*
 *	File Name:	CountryCondition.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2013/04/30		2.2.2	Sales Mov 관리 기능
 *	jbaek		2012/07/30		2.2.1	minOrderTotal 추가
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
public class CountryCondition extends com.irt.rbm.ManipulableManagerImpl {
	public final static String CONDITION_INDICATOR_REGISTRED		= "00";
	public final static String CONDITION_INDICATOR_NONE				= "99";

	private final static Table table = Schema.findTable( Schema.DPR_COUNTRY_COND );
	private final static Queryable queryable = new QueryableImpl( new JoinableImpl("SORG", "DPR_SALES_ORGANIZATION") ) {
		{
			Joinable tbl_CCND = new JoinableImplBK("CCND", "DPR_COUNTRY_COND"
					, "CCND.ORGANIZATIONCD(+) = SORG.ORGANIZATION_CD AND CCND.COUNTRYCD(+) = ?", "countryCode" );
			append( new QueryableField[] {
				  new QueryableFieldImpl( Schema.STRING, false, "countryCode", "CCND.COUNTRYCD", tbl_CCND )
				, new QueryableFieldImpl( Schema.STRING, "organizationCode", "SORG.ORGANIZATION_CD" )
				, new QueryableFieldImpl( Schema.STRING, "countryName"
						, "(SELECT SB.COUNTRYNAME FROM DPR_COUNTRY SB WHERE SB.COUNTRY_CD = CCND.COUNTRYCD)", "DPR_COUNTRY_NAME", tbl_CCND )
				, new QueryableFieldImpl( Schema.DOUBLE, "minOrderTotal", "CCND.MIN_ORDERTOTAL", tbl_CCND )
				, new QueryableFieldImplBK( Schema.STRING, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SORG.ORGANIZATION_CD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_ORGANIZATION_NAME" )
				  , new QueryableFieldImpl( Schema.STRING, "conditionInd", "NVL2(CCND.ORGANIZATIONCD, '00', '99')", tbl_CCND )
			} );
		}
	};

	private final static QueryFactory factory = new QueryFactory( queryable );

	public CountryCondition( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String countryCode, String organizationCode ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object> ();
		primaryMap.put( "countryCode", countryCode );
		primaryMap.put( "organizationCode", organizationCode );

		return primaryMap;
	}

	public List<Map<String, Object>> getCountryOrganizations( Object countryCode, String[] fieldKeys ) throws SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();
		conditionMap.put( "conditionInd", CONDITION_INDICATOR_REGISTRED );
		conditionMap.put( "countryCode", countryCode );

		return getRecords( conditionMap, fieldKeys );
	}
}
