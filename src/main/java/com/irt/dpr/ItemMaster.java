/*
 *	File Name:	ItemMaster.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;

/**
 *
 */
public class ItemMaster extends com.irt.rbm.ManipulableManagerImpl {//@formatter:on
	private final static Table table = Schema.findTable(Schema.DPR_ITEM_MASTER);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_ITEM_MASTER);

	public ItemMaster( SQLHandler handler ) {
		super(handler, table, factory);
	}
}
