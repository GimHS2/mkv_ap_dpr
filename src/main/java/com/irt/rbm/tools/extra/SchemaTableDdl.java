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

package com.irt.rbm.tools.extra;

import com.irt.data.AbstractField;
import com.irt.data.Record;
import com.irt.sql.Table;
import com.irt.sql.Table.Field;

import java.util.Arrays;

public class SchemaTableDdl {

	/** needs dataSource Map */
	public static String getCreateLinkedTableTemplate( String sourceTableName, String linkTableName ) {
		String ddl = ""
				+ "CREATE LINKED TABLE " + sourceTableName + " ("
				+ "'${dataSource.driverClassName}', "
				+ "'${dataSource.url}', "
				+ "'${dataSource.username}', "
				+ "'${dataSource.password}', "
				+ "'(SELECT * FROM " + linkTableName + ")'"
				+ ");"
				+ "";

		return ddl;
	}

	public static String getCreateTable( Table table ) {

		Field[] ins = table.getBindFieldArray(Record.INSERT);

		Field[] upd = table.getBindFieldArray(Record.UPDATE);

		StringBuffer sbuf = new StringBuffer();
		for( Field fd : ins ) {
			sbuf.append(fd.getFieldName() + " ");
			sbuf.append(getDbDataType(fd.getDataType()) + " ");
			sbuf.append(", ");
		}

		Field[] pks = table.getPrimaryFieldArray();

		for( Field fd : upd ) {
			if( !Arrays.asList(fd).contains(fd) ) {
				sbuf.append(fd.getFieldName() + " ");
				sbuf.append(getDbDataType(fd.getDataType()) + " ");
				sbuf.append(", ");
			}
		}

		// for( Field fd : table.getFieldMap() ) {
		//
		// }

		for( String fdkey : table.getFieldMap().keySet() ) {
			Table.Field fd = (Field)table.getField(fdkey);
			if( !Arrays.asList(upd).contains(fd) ) {
				if( !Arrays.asList(pks).contains(fd) ) {
					if( !Arrays.asList(ins).contains(fd) ) {
						sbuf.append(fd.getFieldName() + " ");
						sbuf.append(getDbDataType(fd.getDataType()) + " ");
						sbuf.append(", ");
					}
				}
			}
		}

		String primary = null;
		for( Field pk : pks ) {
			if( primary == null )
				primary = "PRIMARY KEY (";
			primary += pk.getFieldName() + ", ";
		}
		if( primary != null ) {
			primary = primary.replaceAll(", $", "");
			primary += ")";
		}

		String template = ""
				+ "CREATE TABLE " + table.getTableName() + " ("
				+ ( primary == null ? sbuf.toString().replaceAll(", $", "") : sbuf.toString() )
				+ primary
				+ ")"
				+ "";

		return template;
	}

	private static String getDbDataType( char stDataType ) {
		switch( stDataType ) {
		case AbstractField.TYPE_INTEGER:
			return "INT";
		case AbstractField.TYPE_LONG:
			return "BIGINT";
		case AbstractField.TYPE_DOUBLE:
			return "DOUBLE";
		case AbstractField.TYPE_DATE:
			return "DATE";
		case AbstractField.TYPE_DATETIME:
			return "TIMESTAMP";
		case AbstractField.TYPE_TIME:
			return "TIME";
		case AbstractField.TYPE_CODE:
		case AbstractField.TYPE_DESCRIPTION:
		case AbstractField.TYPE_STRING:
		default:
			return "VARCHAR";
		}
	}

	public static String getDropTable( Table table ) {
		return "DROP TABLE " + table.getTableName() + " ";
	}

}
