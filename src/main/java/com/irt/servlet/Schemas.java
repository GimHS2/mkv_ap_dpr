/*
 *	File Name:	Schemas.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Formatter:	eclipse
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/04/30		2.2.0c	create
 *
**/

package com.irt.servlet;

import com.irt.sql.Schema;

import java.lang.reflect.TypeVariable;

public enum Schemas {
	SYS( "com.irt.rbm.sys.Schema" ), RBM( "com.irt.rbm.rbm.Schema" ), USR( "com.irt.rbm.usr.Schema" ), //
	DPR( "com.irt.dpr.Schema" ), CST( "com.irt.cst.Schema" ), SIS( "com.irt.sis.Schema" ), ICS( "com.irt.ics.Schema" );

	private final String schemaClass;

	Schemas( String schemaClass ) {
		this.schemaClass = schemaClass;
	}

	public Schemas bySchemaClassName( String schemaClassName ) {
		return valueOf(schemaClassName);
	}

	/**
	 * @return 'systemCode'
	 */
	public String getSchemaSystemCode() {
		return name();
	}

	/**
	 * @return fully qulified class canonical name
	 */
	public String getSchemaClassName() {
		return schemaClass;
	}

	@Override
	public String toString() {
		return schemaClass;
	}
}