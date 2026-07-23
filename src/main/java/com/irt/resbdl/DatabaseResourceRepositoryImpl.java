/*
 *	File Name:	DatabaseResourceRepositoryImpl.java
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

import com.irt.sql.QueryBuffer;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.sql.Table.Field;
import com.irt.util.MapUtil;
import com.irt.util.MessageHandler;
import com.irt.util.cst.DateTimeUtil;
import com.irt.util.cst.SchemaTableFinder;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class DatabaseResourceRepositoryImpl implements DatabaseResourceRepository, DatabaseResourceReloadable.ColumnResourceCheckable {
	private static DatabaseResourceRepositoryImpl DB_RES_MSG_INSTANCE;

	public static DatabaseResourceRepository getInstance( com.irt.system.SystemConfig systemConfig ) throws SQLException {
		if( DB_RES_MSG_INSTANCE == null ) {
			MessageHandler nullMsghandler = new com.irt.util.MessageBundle(new ResourceBundle[] {});
			DB_RES_MSG_INSTANCE = new DatabaseResourceRepositoryImpl(systemConfig.createSQLHandler(nullMsghandler));
		}

		return DB_RES_MSG_INSTANCE;
	}
	private SQLHandler handler;

	private Table msgresTable;

	private Table colresTable;

	private Logger logger = Logger.getLogger(getClass());

	protected DatabaseResourceRepositoryImpl( SQLHandler handler ) {
		this.handler = handler;
		this.msgresTable = SchemaTableFinder.create()
				.withSchemaClassName("com.irt.resbdl.Schema")
				.withSchemaTableKey(DatabaseResourceRepository.CST_MESSAGE_RESOURCE)
				.getTable();
		this.colresTable = SchemaTableFinder.create()
				.withSchemaClassName("com.irt.resbdl.Schema")
				.withSchemaTableKey(DatabaseResourceRepository.CST_COLUMN_RESOURCE)
				.getTable();
	}

	@Override
	public List<Map.Entry<String, Object>> findByBaseName( final String baseName, final Locale locale ) throws SQLException {
		return findByBaseName(baseName, DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale));
	}

	private List<Map.Entry<String, Object>> findByBaseName( String baseName, String localeString ) throws SQLException {
		String[] fieldKeys = new String[] { "resourceKey", "resourceValue" };

		QueryBuffer querybuf = getQueryBuffer(fieldKeys);

		List<Map<String, Object>> list = SQLManager.getRecordList(handler, querybuf.toString(), new Object[] { baseName, localeString });

		if( list != null ) {
			return (List<Map.Entry<String, Object>>)MapUtil.getEntryPairList(list, fieldKeys);
		} else {
			return new ArrayList<Map.Entry<String, Object>>();
		}
	}

	@Override
	public List<Map.Entry<String, Object>> findByBundle( String bundleName ) throws SQLException {
		logger.info("findBundle() : " + bundleName);
		String[] nameAndLocale = DatabaseResourceRepositoryImpl.BundleNaming.fromBundleName(bundleName);

		return findByBaseName(nameAndLocale[0], nameAndLocale[1]);
	}

	@Override
	public Long findColResNewestTimestamp( String partyId, Locale locale ) throws SQLException {
		String localeString = DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale);
		QueryBuffer querybuf = new QueryBuffer();

		String tableAlias = colresTable.getTableAlias();
		querybuf.appendTableWithAlias(colresTable.getTableName(), tableAlias);
		querybuf.appendConditionByField(tableAlias + "." + "PARTYID", partyId);
		querybuf.appendConditionByField(tableAlias + "." + "POOL_LOCALE", localeString);

		String fieldName = String.format(DateTimeUtil.FIELD_FORMAT_ISO8601_FROM_ORACLE_DBDATE, "MAX(" + colresTable.getTableAlias() + ".UPGDATE)");
		querybuf.appendDataWithAlias("DECODE(MAX(" + colresTable.getTableAlias() + ".UPGDATE),NULL,'N'," + fieldName + ")", "maxTimeIso");

		String datetimeIso = (String)SQLManager.getObjectValue(handler, querybuf);

		if( datetimeIso != null && !"N".equals(datetimeIso) ) {
			try {
				return DateTimeUtil.parseISODate(datetimeIso).getTime();
			} catch( ParseException parseEx ) {
				logger.debug(" partyId: '" + partyId + "'" + " locale: '" + locale + "'"
						+ " datetimeIso: '" + datetimeIso + "'", parseEx);
			}
		}

		return null;
	}

	@Override
	public Long findNewestTimestamp( String bundleName ) throws SQLException {
		QueryBuffer querybuf = getQueryBuffer(null);
		String fieldName = String.format(DateTimeUtil.FIELD_FORMAT_ISO8601_FROM_ORACLE_DBDATE, "MAX(" + msgresTable.getTableAlias() + ".UPGDATE)");
		querybuf.appendDataWithAlias("DECODE(MAX(" + msgresTable.getTableAlias() + ".UPGDATE),NULL,'N'," + fieldName + ")", "maxTimeIso");

		String[] nameAndLocale = DatabaseResourceRepositoryImpl.BundleNaming.fromBundleName(bundleName);

		String datetimeIso = (String)SQLManager.getObjectValue(handler, querybuf.toString(), nameAndLocale);

		if( datetimeIso != null && !"N".equals(datetimeIso) ) {
			try {
				return DateTimeUtil.parseISODate(datetimeIso).getTime();
			} catch( ParseException parseEx ) {
				logger.debug(" baseName: '" + nameAndLocale[0] + "'" + " locale: '" + nameAndLocale[1] + "'"
						+ " datetimeIso: '" + datetimeIso + "'", parseEx);
			}
		}
		return null;
	}

	private QueryBuffer getQueryBuffer( String[] fieldKeys ) {
		String tableAlias = msgresTable.getTableAlias();

		QueryBuffer querybuf = new QueryBuffer();
		querybuf.appendTableWithAlias(msgresTable.getTableName(), tableAlias);

		querybuf.appendCondition(tableAlias + ".RES_NAME = ?");
		querybuf.appendCondition(tableAlias + ".RES_LOCALE = ?");

		if( fieldKeys != null ) {
			for( int i = 0; i < fieldKeys.length; i++ ) {
				Table.Field fd = (Field)msgresTable.getField(fieldKeys[i]);
				String fieldName = fd.getFieldName();

				querybuf.appendDataWithAlias(tableAlias + "." + fieldName, fieldKeys[i]);
			}
		}

		return querybuf;
	}

	public static class BundleNaming {
		public static String[] fromBundleName( String bundleName ) {
			String[] nameAndLocale = bundleName.split("_", 2);
			if( nameAndLocale.length == 1 ) {
				return new String[] { bundleName, "" };
			} else {
				return nameAndLocale;
			}
		}

		public static Locale getLocaleByLang( String language ) {
			return new Locale(language);
		}

		public static String getLocaleString( Locale locale ) {
			return locale.getLanguage();
		}

		/**
		 * 
		 * @param baseName
		 * @return first part split by "_"
		 */
		public static String getPureBaseName( String baseName ) {
			if( baseName.contains("_") )
				return baseName.split("_", 2)[0];
			else
				return baseName;
		}

		public static Locale getPureLocale( Locale locale ) {
			return getLocaleByLang(locale.getLanguage());
		}

		/**
		 * currently only support language part
		 */
		public static String toBundleName( String baseName, Locale locale ) {
			String lang = getLocaleString(locale);
			return toBundleName(baseName, lang);
		}

		/**
		 * currently only support language part
		 */
		public static String toBundleName( String baseName, String localeString ) {
			return baseName + "_" + localeString;
		}
	}

}
