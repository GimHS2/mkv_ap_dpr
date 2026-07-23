/*
 *	File Name:	Item.java
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
public class ItemDesc extends com.irt.rbm.ManipulableManagerImpl {
	public final static int IMAGESAVETYPE_BLOB			= 0x01;
	public final static int IMAGESAVETYPE_PLACE			= 0x02;

	public final static String ITEMSTATUS_NORMAL		= "00";
	public final static String ITEMSTATUS_TRADEOFF		= "99";

	private final static Table table = Schema.findTable( Schema.DPR_ITEM_MASTER_DESC );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ITEM_MASTER_DESC );

	public ItemDesc( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String itemCode, String languageCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();

		primaryMap.put( "itemCode", itemCode );
		primaryMap.put( "languageCode", languageCode );

		return primaryMap;
	}

	public static Map<String, Object> extractPrimaryMap( Map<String, Object> conditionMap ) {
		return ItemDesc.createPrimary( (String)conditionMap.get("itemCode"), (String)conditionMap.get("displayLanguage") );
	}
}
