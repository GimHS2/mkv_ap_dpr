/*
 *	File Name:	DaoManager.java
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

package com.irt.util;

import com.irt.rbm.TableDaoManager;
import com.irt.servlet.Schemas;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;
import com.irt.system.SessionManager;
import com.irt.util.cst.SchemaTableFinder;

import java.util.Map.Entry;

public class DaoManager {
	public static DaoManager getInstance( Schemas schemas, String schemaTableKey, SQLHandler handler ) {
		return new DaoManager(schemas.toString(), schemaTableKey, handler);
	}

	public static DaoManager getInstance( String schemaClassName, String schemaTableKey, SQLHandler handler ) {
		return new DaoManager(schemaClassName, schemaTableKey, handler);
	}

	private String schemaClassName;

	private String schemaTableKey;

	private Table table;

	private QueryFactory factory;

	private SQLHandler handler;

	private TableDaoManager mgr;

	private SessionManager sessionMng;

	public DaoManager( Entry<String, String> schemaTableEntry ) {
		this(schemaTableEntry, null);
	}

	public DaoManager( Entry<String, String> schemaTableEntry, SQLHandler handler ) {
		this(schemaTableEntry.getKey(), schemaTableEntry.getValue(), handler);
	}

	public DaoManager( String schemaClassName, String schemaTableKey ) {
		this(schemaClassName, schemaTableKey, null);
	}

	public DaoManager( String schemaClassName, String schemaTableKey, SQLHandler handler ) {
		this.schemaClassName = schemaClassName;
		this.schemaTableKey = schemaTableKey;
		this.handler = handler;
	}

	public TableDaoManager createManager( SQLHandler handler ) {
		if( getTable() == null )
			throw new IllegalStateException("table is mandatory.");

		return new TableDaoManager(handler, table, ( factory == null ? getFactory() : factory ))
				.withSchemaClassName(schemaClassName)
				.withSchemaTableKey(schemaTableKey)
				.withSessionManager(sessionMng);
	}

	private QueryFactory getFactory() {
		if( factory == null )
			factory = SchemaTableFinder.findSchemaQueryFactory(getClass().getClassLoader(), schemaClassName, schemaTableKey);

		return factory;
	}

	public TableDaoManager getManager() {
		if( getSQLHandler() == null )
			throw new IllegalStateException("handler is mandatory.");

		if( mgr == null )
			mgr = createManager(handler);

		return mgr;
	}

	public SessionManager getSessionMng() {
		return sessionMng;
	}

	private SQLHandler getSQLHandler() {
		return handler;
	}

	private Table getTable() {
		if( table == null )
			table = SchemaTableFinder.findSchemaTable(getClass().getClassLoader(), schemaClassName, schemaTableKey);

		return table;
	}

	public void setSessionMng( SessionManager sessionMng ) {
		this.sessionMng = sessionMng;
	}

	public void setSQLHandler( SQLHandler handler ) {
		this.handler = handler;
	}

}
