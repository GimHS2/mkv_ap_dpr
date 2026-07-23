/*
 *	File Name:	ItemUOM.java
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
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class ItemUOM extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_ITEM_MASTER_UOM );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ITEM_MASTER_UOM );

	public ItemUOM( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String countryCode, String itemCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "countryCode", countryCode );
		primaryMap.put( "itemCode", itemCode );

		return primaryMap;
	}

	public int getPackSize( String itemCode, String uomCode ) throws DataException, SQLException {
		return SQLManager.getInt( handler, "SELECT PACKSIZE FROM DPR_ITEM_MASTER_UOM WHERE ITEMCD = ? AND UOM_CD = ?", new Object[] { itemCode, uomCode } );
	}

	public void pushUOM( List<Map<String, Object>> list ) throws DataException, SQLException {
		if( list == null || list.size() <= 0 ) return;

		List<String> itemList = new java.util.ArrayList();
		for( Object obj : list ) {
			Map map = (Map)obj;
			itemList.add( Record.extractString(map, "itemCode") );
		}

		if( itemList.size() <= 0 ) return;

		String[] items = new String[ itemList.size() ];
		itemList.toArray( items );
		Map<String, Object> conditionMap = new java.util.HashMap();
		conditionMap.put( "itemCode", items );

		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );
		querybuf.appendDataWithAlias( "ITEMCD", "itemCode" );
		querybuf.appendDataWithAlias( "UOM_CD", "uomCode" );
		querybuf.appendDataWithAlias( "UOM_NAME", "uomName" );
		querybuf.appendDataWithAlias( "PACKSIZE", "packSize" );
		querybuf.appendTable( "DPR_ITEM_MASTER_UOM" );
		querybuf.findCondition( "itemCode", "ITEMCD" );
		querybuf.appendOrderBy( "ITEMCD, UOM_NAME" );

		List<Map<String, Object>> uomList = SQLManager.getRecordList( handler, querybuf );
		if( uomList == null || uomList.size() <= 0 ) return;

		String itemCode = null;
		Map<String, Object> uomMap = new java.util.HashMap();
		List<Map<String, Object>> arrList = new java.util.ArrayList();
		for( Object obj : uomList ) {
			Map map = (Map)obj;
			if( itemCode == null || itemCode.length() == 0 ) itemCode = Record.extractString( map, "itemCode" );
			if( !itemCode.equals(map.get("itemCode")) ) {
				uomMap.put( itemCode, arrList );
				itemCode = Record.extractString( map, "itemCode" );
				arrList = new java.util.ArrayList();
			}

			arrList.add( map );
		}
		if( arrList.size() > 0 ) uomMap.put( itemCode, arrList );

		for( Object obj : list ) {
			Map map = (Map)obj;
			map.put( "uoms", uomMap.get(map.get("itemCode")) );
		}
	}
}
