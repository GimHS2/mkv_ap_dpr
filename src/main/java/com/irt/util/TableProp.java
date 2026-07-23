/*
 *	File Name:	TableProp.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.0c	create
 *
**/

package com.irt.util;

import com.irt.sql.Table;
import com.irt.util.cst.SchemaTableFinder;

/**
 * Steps to use :
 * 
 * <pre>
 * 1. Implement:
 * 2. Define configuration( usually tools.conf ):
 * 3. Use object:
 * </pre>
 * 
 * 
 * 1. Implement:
 * 
 * <pre>
 * public com.irt.somepkg.SomeProcessRunner implements com.irt.rbm.tools.ProcessRunner {
 * 
 * private TableProp tableProp = new TableProp();
 * 
 * public void setTableProp( TableProp finder ) {
 * 		this.tableProp = finder;
 * }
 * 
 * public TableProp getTableProp() {
 * 		return this.tableProp;
 * }
 * 
 * ....
 * 
 * }
 * </pre>
 * 
 * 
 * 2. Define configuration( usually tools.conf ):
 * 
 * <pre>
 * ....
 * ProcessRunner.somerunner.className = com.irt.somepkg.SomeProcessRunner
 * # irt's "Schema.java" canonical name
 * ProcessRunner.somerunner.TableProp.schemaClassName = com.irt.rbm.usr.Schema 
 * # irt's "Schema Table Object Key" in "Schema.java"
 * ProcessRunner.somerunner.TableProp.schemaTableKey = UserUser
 * ....
 * </pre>
 * 
 * 
 * 3. Use object:
 * 
 * <pre>
 * 
 * public void someTableOperation() {
 * 		Map<String, Object> userMap = ....;
 * 
 *		Table userTable = TableProp.getInstance();
 *
 *		SQLManager.manageRecord( handler, userTable, userMap, Record.INSERT | Record.UPDATE );
 *		...
 *
 *		handler.commit();
 *		...
 * }
 * 
 * </pre>
 *
 */
public class TableProp {//@formatter:on
	private String schemaTableKey;
	private String schemaClassName;

	public TableProp() {
	}

	public TableProp( String schemaClassName, String schemaTableKey ) {
		this.schemaClassName = schemaClassName;
		this.schemaTableKey = schemaTableKey;
	}

	public void setSchemaTableKey( String schemaTableKey ) {
		this.schemaTableKey = schemaTableKey;
	}

	public void setSchemaClassName( String schemaClassName ) {
		this.schemaClassName = schemaClassName;
	}

	public String getSchemaTableKey() {
		return schemaTableKey;
	}

	public String getSchemaClassName() {
		return schemaClassName;
	}

	public boolean isValid() {
		if( schemaClassName == null || schemaTableKey == null ) {
			return false;
		}

		return true;
	}

	public String toString() {
		return "TableProp{" + "schemaClassName: " + schemaClassName + " schemaTableKey: " + schemaTableKey + "}";
	}

	public Table getInstance() {
		return SchemaTableFinder.findSchemaTable(this.getClass().getClassLoader(), schemaClassName, schemaTableKey);
	}
}
