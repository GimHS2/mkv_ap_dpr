/*
 *	File Name:	MoqItem.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/06/30		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.rbm.QueryableManagerImpl;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;

public class MoqItem extends QueryableManagerImpl {
	private static final QueryFactory factory = Schema.findQueryFactory(Schema.DPR_MOQITEM_RLT);

	public MoqItem( SQLHandler handler ) {
		this(handler, factory);
	}

	protected MoqItem( SQLHandler handler, QueryFactory factory ) {
		super(handler, factory);
	}
}
