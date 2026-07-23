/*
 *	File Name:	ResourceBundleWriter.java
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

import com.irt.sql.QueryFactory;
import com.irt.sql.Queryable;
import com.irt.sql.Table;

public class Schema extends com.irt.sql.Schema {
	public final static String CST_MESSAGE_RESOURCE = DatabaseResourceRepository.CST_MESSAGE_RESOURCE;
	public final static String CST_COLUMN_RESOURCE = DatabaseResourceRepository.CST_COLUMN_RESOURCE;

	private final static String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";

	private final static Schema schema = new Schema();

	Schema() {
		Table table;
		Table.Field[] tfields;

		/***************************************************************************************************
		 * CST_MSGRES
		 ***************************************************************************************************/
		tfields = new Table.Field[] {
				createFD(PM, "resourceBaseName", "RES_NAME", "RESOURCE_NAME", 0, 50, ( CHARS + '.' )),
				createFD(PM, "resourceLocale", "RES_LOCALE", "RESOURCE_LOCALE", STRING, 0, 30),
				createFD(PM, "resourceKey", "RES_KEY", "RESOURCE_KEY", STRING, 0, 256),
				createFD(OP, "resourceValue", "RES_VALUE", "RESOURCE_VALUE", STRING),
				createFD(OP, "resourcePageId", "EXTRA1", "EXTRA1", STRING),
				createFD(OP, "modifyUserId", "MODIFYUSERID", "MODIFYUSERID", 0, 30),
				createFD(RD, "status", "STATUS", "STATUS", "PUB_STATUS_", "00"),
				createFD(RD, "createDateTime", "REGDATE", "CREATEDATETIME", DATETIME),
				createFD(OP, "updateDateTime", "UPGDATE", "UPDATEDATETIME", DATETIME)
		};
		putTable(CST_MESSAGE_RESOURCE, table = createTable("CST_MSGRES", "MSGRES", tfields));

		/***************************************************************************************************
		 * CST_COLRES
		 ***************************************************************************************************/
		tfields = new Table.Field[] {
				createFD(PM, "poolName", "POOL_NAME", "POOL_NAME", STRING, 0, 50),
				createFD(PM, "poolLocale", "POOL_LOCALE", "RESOURCE_LOCALE", STRING, 0, 30),
				createFD(PM, "partyId", "PARTYID", "PARTY_ID", STRING, 0, 50),
				createFD(PM, "columnType", "COLTYPE", "POOL_COLUMN_KEY", STRING, 0, 256),
				createFD(PM, "columnKey", "COLKEY", "POOL_COLUMN_KEY", STRING, 0, 256),
				createFD(OP, "columnParentKey", "COL_PARENTKEY", "POOL_COLUMN_PARENT_KEY", STRING),
				createFD(OP, "columnTitle", "COL_NAME", "POOL_COLUMN_NAME", STRING),
				createFD(OP, "columnSortable", "COL_SORT", "POOL_COLUMN_SORT", STRING),
				createFD(OP, "columnAttr", "COL_ATTR", "POOL_COLUMN_ATTR", STRING),
				createFD(OP, "columnDataPattern", "COL_DATA_PTN", "POOL_COLUMN_DATA_PATTERN", STRING),
				createFD(OP, "columnHelpPattern", "COL_HELP_PTN", "POOL_COLUMN_HELP_PATTERN", STRING),
				createFD(OP, "columnDataCellAttr", "COL_DATACELL_ATTR", "POOL_COLUMN_DATACELL_ATTR", STRING),
				createFD(OP, "columnLinkKeys", "COL_LINK_KEYS", "POOL_COLUMN_LINK_KEYS", STRING),
				createFD(OP, "columnLinkHeaderKey", "COL_LINK_HDRKEY", "POOL_COLUMN_LINK_HEADERLINKKEY", STRING),
				createFD(OP, "columnLinkColumnKey", "COL_LINK_COLKEY", "POOL_COLUMN_LINK_COLUMNLINKKEY", STRING),
				// createFD(OP, "linkType", "LNK_TYPE", "POOL_LINK_TYPE", STRING),
				// createFD(OP, "linkKey", "LNK_KEY", "POOL_LINK_KEY", STRING),
				// createFD(OP, "linkHrefPattern", "LNK_HREF_PTN", "POOL_LINK_HREF_PATTERN", STRING),
				// createFD(OP, "linkHelpPattern", "LNK_HELP_PTN", "POOL_LINK_HELP_PATTERN", STRING),
				createFD(OP, "linkAuthSystemCode", "LNK_AUTH_SYSCD", "POOL_LINK_AUTH_SYSTEM_CODE", STRING),
				createFD(OP, "linkAuthPackageCode", "LNK_AUTH_PKGCD", "POOL_LINK_AUTH_PACKAGE_CODE", STRING),
				createFD(OP, "linkCondClass", "LNK_COND_CLASS", "POOL_LINK_COND_CLASS", STRING),
				createFD(OP, "linkCondFieldKey", "LNK_COND_FIELDKEY", "POOL_LINK_COND_FIELDKEY", STRING),
				createFD(OP, "linkCondValue", "LNK_COND_VALUE", "POOL_LINK_COND_VALUE", STRING),
				createFD(OP, "modifyUserId", "MODIFYUSERID", "MODIFYUSERID", 0, 30),
				createFD(RD, "status", "STATUS", "STATUS", "PUB_STATUS_", "00"),
				createFD(RD, "createDateTime", "REGDATE", "CREATEDATETIME", DATETIME),
				createFD(OP, "updateDateTime", "UPGDATE", "UPDATEDATETIME", DATETIME),
		};
		putTable(CST_COLUMN_RESOURCE, table = createTable("CST_COLRES", "COLRES", tfields));
	}

	public static Queryable findQueryable( String key ) {
		return schema.getQueryable(key);
	}

	public static QueryFactory findQueryFactory( String key ) {
		Queryable queryable = schema.getQueryable(key);
		return ( queryable == null ? null : new QueryFactory(queryable) );
	}

	public static Table findTable( String key ) {
		return schema.getTable(key);
	}

}
