/*
 *	File Name:	ItemMasterExtra.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/04/03		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;

import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class ItemMasterExtra extends com.irt.rbm.ManipulableManagerImpl {//@formatter:on
	private final static Table table = Schema.findTable(Schema.DPR_ITEM_MASTER_EXTRA);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_ITEM_MASTER_EXTRA);

	public ItemMasterExtra( SQLHandler handler ) {
		super(handler, table);
	}

	protected ItemMasterExtra( SQLHandler handler, Table table ) {
		super(handler, table);
	}

	public static Map<String, Object> createPrimary( String organizationCode, String itemCode ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();
		primaryMap.put("organizationCode", organizationCode);
		primaryMap.put("itemCode", itemCode);

		return primaryMap;
	}

	public int deleteAll() throws DataException, SQLException {
		int count = SQLManager.executeStatement(handler, "DELETE DPR_ITEM_MASTER_EXTRA");

		return count;
	}

}
