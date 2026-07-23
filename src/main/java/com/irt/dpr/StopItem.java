/*
 *	File Name:	StopItem.java
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

import com.irt.rbm.QueryableManagerImpl;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;

public class StopItem extends QueryableManagerImpl {
	private final static Table table = Schema.findTable(Schema.DPR_STOPITEM);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_STOPITEM);

	public StopItem( SQLHandler handler ) {
		super(handler, factory);
	}
}
