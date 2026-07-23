/*
 *	File Name:	PartyMaster.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.1	DPR_SALES_OFFICE, DPR_SALES_OFFICE_GROUP 을 view로 처리.
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;

import java.util.Map;

/*
 *
 */
public class PartyMaster extends com.irt.rbm.QueryableManagerImpl {
	public final static int IDX_CUSTOMER_GROUP			= 0;
	public final static int IDX_DISTRIBUTIONCHANNEL		= 1;
	public final static int IDX_DIVISION				= 2;
	public final static int IDX_REGION					= 3;
	public final static int IDX_SALES_ORGANIZATION		= 4;
	public final static int IDX_SALES_DISTRICT			= 5;
	public final static int IDX_SALES_OFFICE			= 6;
	public final static int IDX_SALES_GROUP				= 7;

	private final static Object[] primaryKeys = new Object[] {
		  new String[] { "customerGroupCode" }, new String[] { "channelCode" }, new String[] { "divisionCode" }
		, new String[] { "regionCode", "countryKey" }, new String[] { "organizationCode" }, new String[] { "districtCode" }
		, new String[] { "officeCode" }, new String[] { "officeCode", "grouopCode" }
	};

	private final static QueryFactory[] factories = new QueryFactory[] {
		  Schema.findQueryFactory( Schema.DPR_CUSTOMER_GROUP ), Schema.findQueryFactory( Schema.DPR_DISTRIBUTION_CHANNEL )
		, Schema.findQueryFactory( Schema.DPR_DIVISION ), Schema.findQueryFactory( Schema.DPR_REGION )
		, Schema.findQueryFactory( Schema.DPR_SALES_ORGANIZATION ), Schema.findQueryFactory( Schema.DPR_SALES_DISTRICT )
		, Schema.findQueryFactory( Schema.vwDPR_SALES_OFFICE ), Schema.findQueryFactory( Schema.vwDPR_SALES_OFFICE_GROUP )
	};

	public PartyMaster( SQLHandler handler, int type ) {
		super( handler, factories[type] );
	}

	public static Map<String, Object> createPrimary( int type, String... primaryValues ) {
		Map<String, Object> primaryMap = new java.util.HashMap<String, Object> ();

		try {
			String[] keys = (String [])primaryKeys[type];

			for( int i = 0; i < keys.length; i++ )
				primaryMap.put( keys[i], primaryValues[i] );

		} catch( ArrayIndexOutOfBoundsException arrayEx ) {
			return new java.util.HashMap<String, Object> ();
		}

		return new java.util.HashMap<String, Object> ( primaryMap );
	}
}
