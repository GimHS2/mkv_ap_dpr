/*
 *	File Name:	MasterLink.java
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
public class MasterLink extends com.irt.rbm.ManipulableManagerImpl {
	public final static String MASTERTYPE_CUSTOMERGROUP				= "CG";
	public final static String MASTERTYPE_DISTRIBUTION_CHANNEL		= "DC";
	public final static String MASTERTYPE_DIVISION					= "DI";
	public final static String MASTERTYPE_REGION					= "RG";
	public final static String MASTERTYPE_SALES_DISTRICT			= "SD";
	public final static String MASTERTYPE_SALES_GROUP				= "SG";
	public final static String MASTERTYPE_SALES_OFFICE				= "SF";
	public final static String MASTERTYPE_SALES_ORGANIZATION		= "SO";

	public final static String MASTERTYPE_BASEPRODUCT				= "BP";
	public final static String MASTERTYPE_MEGABRAND					= "MB";
	public final static String MASTERTYPE_BRAND						= "BR";
	public final static String MASTERTYPE_VARIANT					= "VA";
	public final static String MASTERTYPE_PUTUP						= "PU";
	public final static String MASTERTYPE_PRODUCT_CATEGORY			= "PC";

	private final static Table table = Schema.findTable( Schema.DPR_MASTER_LINK );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_MASTER_LINK );

	public MasterLink( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String masterCode, String linkType, String linkMasterCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "masterCode", masterCode );
		primaryMap.put( "linkType", linkType );
		primaryMap.put( "linkMasterCode", linkMasterCode );

		return primaryMap;
	}

	protected static class Valid implements QueryBufferValid {
		String conditionKey;
		String value;

		public Valid( String conditionKey, String value ) {
			this.conditionKey = conditionKey;
			this.value = value;
		}

		public boolean hasValidCondition( QueryBuffer querybuf ) {
			if( value == null || value.length() == 0 ) return false;

			if( querybuf instanceof ConditionQueryBuffer ) {
				ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;
				if( value.equals(condquerybuf.getConditionValue(conditionKey)) )
					return true;

				return false;
			} else
				return false;
		}
		
	}
}
