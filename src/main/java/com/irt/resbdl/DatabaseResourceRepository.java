/*
 *	File Name:	DatabaseResourceReloadable.java
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

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface DatabaseResourceRepository {

	public static String CST_RESOURCE_SCHEMA_CLASS = "com.irt.resbdl.Schema";
	public static String CST_MESSAGE_RESOURCE = "MessageResource";
	public static String CST_COLUMN_RESOURCE = "ColumnResource";

	public List<Map.Entry<String, Object>> findByBaseName( String baseName, Locale locale ) throws SQLException;

	public List<Map.Entry<String, Object>> findByBundle( String bundleName ) throws SQLException;

	public Long findNewestTimestamp( String bundleName ) throws SQLException;

}
