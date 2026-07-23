/*
 *	File Name:	SchemaTableDdl.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0c	create
 *
**/

package com.irt.resbdl;

import com.irt.rbm.ManipulableManagerImpl;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;

public class DatabaseMessageResource extends ManipulableManagerImpl {//@formatter:on
	private final static Table table = Schema.findTable(Schema.CST_MESSAGE_RESOURCE);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.CST_MESSAGE_RESOURCE);

	public DatabaseMessageResource( SQLHandler handler ) {
		super(handler, table, factory);
	}
}
