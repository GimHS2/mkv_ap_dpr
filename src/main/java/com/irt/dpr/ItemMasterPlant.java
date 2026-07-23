/*
 *	File Name:	ItemMasterPlant.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/06/30		2.2.0	create
 *
**/

package com.irt.dpr;

import java.sql.SQLException;
import java.util.Map;

import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;
import com.irt.sql.TableDataLoader;

/**
 *
 */
public class ItemMasterPlant extends com.irt.rbm.ManipulableManagerImpl {//@formatter:on
	private final static Table table = Schema.findTable( Schema.DPR_ITEM_MASTER_PLANT );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ITEM_MASTER_PLANT );

	public ItemMasterPlant( SQLHandler handler ) {
		super(handler, table, factory);
	}

	@Override
	public DataLoader.Loader createDataLoader( String[] fieldKeys, final Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
			, int statementType ) throws SQLException {
		return new TableDataLoader( fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType ) {
			@Override
			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				String manualInd = (String)recordMap.get( "manualInd" );
				if( manualInd != null ) {
					manualInd = manualInd.toUpperCase();
					recordMap.put( "manualInd", manualInd );
				}
				if( !"Y".equals(manualInd) && !"N".equals(manualInd) ) {
					throw handler.createDataException( DataException.ERR_ERROR
							, handler.getMessageHandler().getMessage("ERR_INVALID_MANUAL_IND") );
				}

				return super.processLine( handler, recordMap );
			}
		};
	}

	public static Map<String, Object> createPrimary( Object itemCode, Object plantCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "itemCode", itemCode );
		primaryMap.put( "plantCode", plantCode );

		return primaryMap;
	}

	public static Map<String, Object> createPrimary( Object itemCode, Object plantCode, Object loadingGroup ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "itemCode", itemCode );
		primaryMap.put( "plantCode", plantCode );
		primaryMap.put( "loadingGroup", loadingGroup );

		return primaryMap;
	}
}
