/*
 *	File Name:	ItemIntro.java
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
public class ItemIntro extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_ITEM_MASTER_INTRO );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ITEM_MASTER_INTRO );

	public ItemIntro( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String itemCode, String organizationCode, String languageCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "itemCode", itemCode );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "languageCode", languageCode );

		return primaryMap;
	}
}
