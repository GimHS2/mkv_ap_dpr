/*
 *	File Name:	Period.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/06/28		2.2.0	create
 *
**/


package com.irt.dpr;

import com.irt.rbm.QueryableManagerImpl;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;

public class Period extends QueryableManagerImpl {
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_PERIOD);

	public Period( SQLHandler handler ) {
		this(handler, factory);
	}

	protected Period( SQLHandler handler, QueryFactory factory ) {
		super(handler, factory);
	}

}
