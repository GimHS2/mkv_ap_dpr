/*
 *	File Name:	ColumnAdapter.java
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

package com.irt.html;

import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnGroup;
import com.irt.data.cols.ColumnList;
import com.irt.data.cols.ColumnPool;
import com.irt.data.cols.ColumnResourceBundle;
import com.irt.data.cols.ColumnWrapper;
import com.irt.data.cols.ConstantColumn;
import com.irt.html.ColumnGroupImpl.ColumnG;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.rbm.RBMSystem;
import com.irt.resbdl.ColumnResourceFileWriter;
import com.irt.resbdl.DatabaseResourceReloadable;
import com.irt.resbdl.DatabaseResourceRepositoryImpl;
import com.irt.resbdl.Schema;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.util.MapUtil;
import com.irt.util.cst.DateTimeUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

public class DatabaseColumnResource extends ManipulableManagerImpl implements DatabaseResourceReloadable.ColumnResourceCheckable {//@formatter:on

	private final static Table table = Schema.findTable(Schema.CST_COLUMN_RESOURCE);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.CST_COLUMN_RESOURCE);

	static Map<String, Object> lookup = java.util.Collections.synchronizedMap(new java.util.HashMap<String, Object>());

	private static final long MINIMUM_TIME_TO_LIVE = 15000;
	private static long timeToLive = MINIMUM_TIME_TO_LIVE;
	private static long lastLoadedTime = 0;

	private static Column[] getColumns( ColumnResourceBundle bundle, String poolName, String... columnKeys ) {
		ColumnPool columnPool = bundle.getColumnPool(poolName);
		if( columnPool != null ) {
			return columnPool.getColumns(columnKeys);
		}

		return null;
	}

	public static boolean tryReloadAndSync( SQLHandler handler, ColumnResourceBundle bundle, File baseDir, String partyId, Locale locale,
			String orgColResName ) {
		try {
			DatabaseColumnResource colres = new DatabaseColumnResource(handler);
			if( colres.needsReload(partyId, locale) ) {
				try {
					return ColumnConfigureFile.Reloadable.clear();
				} finally {
					List<Map<String, Object>> list = colres.getListForSync(bundle, partyId, locale);
					try {
						ColumnResourceFileWriter.getInstance(baseDir, partyId, locale).writeSync(list, orgColResName);
					} catch( IOException ioEx ) {
						Logger.getRootLogger().error("orgColResName: " + orgColResName, ioEx);
					}
				}
			}
		} catch( SQLException sqlEx ) {
			Logger.getRootLogger().error(sqlEx);
		} catch( DataException dataEx ) {
			Logger.getRootLogger().error(dataEx);
		}

		return false;
	}

	private Logger logger = Logger.getLogger(getClass());

	ColumnAdapter adapter = new ColumnAdapter();

	public DatabaseColumnResource( SQLHandler handler ) {
		super(handler, table, factory);
	}

	@Override
	public Long findColResNewestTimestamp( String partyId, Locale locale ) throws SQLException {
		String localeString = DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale);
		QueryBuffer querybuf = new QueryBuffer();

		String tableAlias = table.getTableAlias();
		querybuf.appendTableWithAlias(table.getTableName(), tableAlias);
		querybuf.appendConditionByField(tableAlias + "." + ( (Table.Field)table.getField("partyId") ).getFieldName(), partyId);
		querybuf.appendConditionByField(tableAlias + "." + ( (Table.Field)table.getField("poolLocale") ).getFieldName(), localeString);

		String fieldName = String.format(DateTimeUtil.FIELD_FORMAT_ISO8601_FROM_ORACLE_DBDATE, "MAX(" + table.getTableAlias() + ".UPGDATE)");

		querybuf.appendDataWithAlias("DECODE(MAX(" + table.getTableAlias() + ".UPGDATE),NULL,'N'," + fieldName + ")", "maxTimeIso");

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

	public List<Map<String, Object>> getCurrentColumnResource( ColumnResourceBundle bundle, String partyId, String columnListName,
			Locale locale ) {

		List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();

		String poolName = columnListName;
		if( columnListName.contains("%") ) {
			poolName = columnListName.split("%")[0];
		}

		ColumnPool columnPool = bundle.getColumnPool(poolName);
		if( columnPool != null && columnPool instanceof ColumnPoolImpl ) {
			records.addAll(adapter.getDbColumnSuffixRecords((ColumnPoolImpl)bundle.getColumnPool(poolName), partyId, locale));
		}
		ColumnList _columnList = bundle.getColumnList(columnListName);
		if( _columnList instanceof OptionColumnListImpl ) {
			OptionColumnListImpl columnList = (OptionColumnListImpl)_columnList;
			for( Column _column : columnList.optionColumns ) {
				records.addAll(resolveToDbRecords(_column, poolName, partyId, locale));
			}
		} else {
			for( Column _column : _columnList.getColumns() ) {
				records.addAll(resolveToDbRecords(_column, poolName, partyId, locale));
			}
		}

		return records;
	}

	List<Map<String, Object>> resolveToDbRecords( Column _column, String poolName, String partyId, Locale locale ) {
		List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = adapter.toDbRecord(_column, partyId, locale.getLanguage(), poolName);
		if( _column instanceof ColumnImpl ) {
			ColumnImpl column = (ColumnImpl)_column;

			ColumnGroup columnGroup = column.getColumnGroup();
			if( columnGroup == null ) {
				records.add(map);
			} else if( !records.contains(map) ) {
				records.add(map);
			}
		} else if( _column instanceof ConstantColumn ) {
			throw new ProblemException("currently not ready yet." + _column.getClass().getCanonicalName());
		} else if( _column instanceof LinkedColumn ) {
			if( map.get("columnLinkKeys") != null ) {
				Map<String, Object> _map = null;
				if( map.get("headerLink") != null ) {
					records.add((Map<String, Object>)map.get("headerLink"));
				} else if( map.get("columnLink") != null ) {
					records.add((Map<String, Object>)map.get("columnLink"));
				}
			}
			records.add(map);
		} else if( _column instanceof ColumnWrapper ) {
			if( _column instanceof ColumnG ) {
				throw new ProblemException("currently not ready yet." + _column.getClass().getCanonicalName());
				// ColumnG column = (ColumnG)_column;
			} else {
				throw new ProblemException("found this.!!!" + _column.getClass().getSimpleName());
			}
		} else {
			throw new ProblemException("found unknown type.!!!" + _column.getClass().getSimpleName());
		}

		return records;
	}

	/**
	 * @return column resource message( maybe cached data or from database )
	 */
	public List<Map<String, Object>> getDatabaseColumnResourceMessages( String partyId, Locale locale ) throws SQLException {
		return getDatabaseColumnResourceMessages(partyId, locale, true);
	}

	private List<Map<String, Object>> getDatabaseColumnResourceMessages( String partyId, Locale locale, boolean useCached ) throws SQLException {
		if( useCached ) {
			List<Map<String, Object>> cachedBundleData = readCachedData(partyId, locale);
			if( cachedBundleData != null && cachedBundleData.size() > 0 )
				return cachedBundleData;
		}

		Map<String, Object> conditionMap = Record.createMap("partyId", partyId);
		conditionMap.put("poolLocale", DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale));

		return getRecords(conditionMap);
	}

	private long getLastLoadedTimeThreadSafely() {
		synchronized( DatabaseColumnResource.class ) {
			return lastLoadedTime;
		}
	}

	private List<Map<String, Object>> getListForSync( ColumnResourceBundle bundle, String partyId, Locale locale ) throws SQLException {
		loadDatabaseData(partyId, locale);

		List<Map<String, Object>> list = getDatabaseColumnResourceMessages(partyId, locale);
		if( list == null )
			return list;

		// {currColumnGroup: [{dbRecordMap}]}
		Map<ColumnGroup, Collection<Map<String, Object>>> dbColGroupMap = new HashMap<ColumnGroup, Collection<Map<String, Object>>>();

		for( Map<String, Object> frDb : list ) {
			if( ColumnAdapter.ColumnType.ColumnGroup.toString().equals(frDb.get("columnType")) ) {
				ColumnPool columnPool = bundle.getColumnPool((String)frDb.get("poolName"));
				if( columnPool != null ) {
					ColumnGroup columnGroup = columnPool.getColumnGroup((String)frDb.get("columnParentKey"));
					if( columnGroup != null ) {
						Collection<Map<String, Object>> dbColumnMaps = null;
						if( dbColGroupMap.containsKey(columnGroup) ) {
							dbColumnMaps = dbColGroupMap.get(columnGroup);
						} else {
							dbColumnMaps = new HashSet<Map<String, Object>>();
						}

						dbColumnMaps.add(frDb);
						dbColGroupMap.put(columnGroup, dbColumnMaps);
					}
				}
			}
		}

		for( ColumnGroup currView : dbColGroupMap.keySet() ) {
			Collection<Map<String, Object>> dbColumnRecords = dbColGroupMap.get(currView);

			Map<String, Object> dbColGroup_firstMap = dbColumnRecords.iterator().next();
			String poolName = (String)dbColGroup_firstMap.get("poolName");
			String[] currView_group_columnKeys = currView.getColumnKeys();

			Column[] columns = null;
			ColumnPool columnPool = bundle.getColumnPool(poolName);
			if( columnPool != null ) {
				columns = columnPool.getColumns(currView_group_columnKeys);
			}

			// add missing columnGroup subColumns
			if( columns != null ) {
				Collection<Object> inDb = MapUtil.extractValues(dbColumnRecords, "columnKey");
				for( String inCurr : currView_group_columnKeys ) {
					if( !inDb.contains(inCurr) ) {
						for( Column col : columns ) {
							if( inCurr.equals(col.getKey()) ) {
								list.add(adapter.toDbRecord(col, (String)dbColGroup_firstMap.get("partyId"),
										(String)dbColGroup_firstMap.get("poolLocale"),
										(String)dbColGroup_firstMap.get("poolName")));
							}
						}
					}
				}
			}
		}

		return list;
	}

	private Long handleLastColResChecked() {
		String sLastColResChecked = RBMSystem.getSystemEnv("SYS", "DatabaseResource;lastColResChecked");
		Long lastColResChecked = null;
		if( sLastColResChecked != null && sLastColResChecked.length() > 0 ) {
			lastColResChecked = Long.valueOf(sLastColResChecked, 10);
		}
		return lastColResChecked;
	}

	public boolean isDebug() {
		return RBMSystem.getSystemEnvBool("SYS", "DatabaseResource;debug", false);
	}

	private void loadDatabaseData( String partyId, Locale locale ) throws SQLException {
		synchronized( lookup ) {
			String bundleName = DatabaseResourceRepositoryImpl.BundleNaming.toBundleName(partyId, locale);
			boolean canUseCached = false;
			lookup.put(bundleName, getDatabaseColumnResourceMessages(partyId, locale, canUseCached));
			setLastLoadedTimeThreadSafely();
		}
	}

	private boolean needsReload( String partyId, Locale locale ) throws DataException, SQLException {

		if( !( RBMSystem.getSystemEnvBool("SYS", "DatabaseResource;useColumnResourceReload", true) ) ) {
			logger.warn("DatabaseColumnResource reload function disabled by SystemEnv.");
		}

		try {
			Long lastColResChecked = handleLastColResChecked();

			// long maxDormantMinute = 60 * 60 * 1000;// minute
			// Long lastLoadedTime = getLastLoadedTimeThreadSafely();
			// long timeDiff = System.currentTimeMillis() - lastLoadedTime;

			if( lastColResChecked != null && ( lastColResChecked + timeToLive < System.currentTimeMillis() ) ) {
				Long newestTime = null;
				try {
					newestTime = findColResNewestTimestamp(partyId, locale);
				} catch( SQLException sqlEx ) {
					logger.error(sqlEx);
				}

				// boolean dbHasData = ( newestTime != null );
				boolean bundleHasData = ( lookup != null && !lookup.isEmpty() );

				boolean needsReload = com.irt.resbdl.DatabaseMessageBundleControl.needsReload(lastLoadedTime, newestTime, bundleHasData);
				return needsReload;
			}
		} finally {
			RBMSystem.setSystemEnv(handler, "SYS", "DatabaseResource;lastColResChecked", Long.toString(System.currentTimeMillis()));
		}

		return false;
	}

	private List<Map<String, Object>> readCachedData( String partyId, Locale locale ) {
		synchronized( lookup ) {
			String bundleName = DatabaseResourceRepositoryImpl.BundleNaming.toBundleName(partyId, locale);
			return (List<Map<String, Object>>)lookup.get(bundleName);
		}
	}

	private void setLastLoadedTimeThreadSafely() {
		synchronized( DatabaseColumnResource.class ) {
			lastLoadedTime = System.currentTimeMillis();
		}
	}

	class ProblemException extends RuntimeException {
		public ProblemException( String message ) {
			super(message);
		}
	}

}
