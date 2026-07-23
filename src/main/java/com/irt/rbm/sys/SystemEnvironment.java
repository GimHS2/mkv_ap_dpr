/*
 *	File Name:	TableDaoException.java
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

package com.irt.rbm.sys;

import com.irt.custom.SystemEx;
import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.sql.QueryFactory;
import com.irt.sql.Queryable;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** @deprecated*/
@Deprecated
public class SystemEnvironment extends ManipulableManagerImpl {

	private final static Table table = ThisSchema.findTable(Schema.SYS_SYSTEM_ENVIRONMENT);
	private final static QueryFactory factory = ThisSchema.findQueryFactory(Schema.SYS_SYSTEM_ENVIRONMENT);

	public SystemEnvironment( SQLHandler handler ) {
		this(handler, table, factory);
	}

	protected SystemEnvironment( SQLHandler handler, Table table, QueryFactory factory ) {
		super(handler, table, factory);
	}

	public static Map<String, Object> createPrimary( String systemCode, String envName, String envKey ) {
		Map<String, Object> primary = Record.createMap("systemCode", systemCode);
		primary.put("envName", envName);
		primary.put("envKey", envKey);
		return primary;
	}

	public static class ThisSchema extends com.irt.sql.Schema {
		private final static ThisSchema schema = new ThisSchema();

		ThisSchema() {
			Table table;
			Table.Field[] tfields;

			/***************************************************************************************************
			 * SYS_SYSTEM_ENV
			 ***************************************************************************************************/
			tfields = new Table.Field[] {
					createFD(PM, "systemCode", "SYSTEMCD", "SYS_SYSTEMCODE", 0, 30),
					createFD(PM, "envName", "NAME", "SYS_SYSTEM_ENV_NAME", STRING),
					createFD(RD, "envArray", "VALUE", "SYS_SYSTEM_ENV_ARRAY", STRING),
					createFD(RD, "status", "STATUS", "STATUS", "PUB_STATUS_", "00"),
					createFD(RD, "createDateTime", "REGDATE", "CREATEDATETIME", DATETIME),
					createFD(RD, "updateDateTime", "UPGDATE", "UPDATEDATETIME", DATETIME)
			};
			putTable(Schema.SYS_SYSTEM_ENVIRONMENT, table = createTable("SYS_SYSTEM_ENV", "SENV", tfields));

			/***************************************************************************************************
			 * USR_PARTY_ENV
			 ***************************************************************************************************/
			tfields = new Table.Field[] {
					createFD(PM, "partyId", "PARTYID", "PARTYID", 0, 30),
					createFD(PM, "systemCode", "SYSTEMCD", "SYS_SYSTEMCODE", 0, 30),
					createFD(PM, "envName", "NAME", "SYS_SYSTEM_ENV_NAME", STRING),
					createFD(RD, "envArray", "VALUE", "SYS_SYSTEM_ENV_ARRAY", STRING),
					createFD(RD, "status", "STATUS", "STATUS", "PUB_STATUS_", "00"),
					createFD(RD, "createDateTime", "REGDATE", "CREATEDATETIME", DATETIME),
					createFD(RD, "updateDateTime", "UPGDATE", "UPDATEDATETIME", DATETIME)
			};
			putTable(com.irt.rbm.usr.Schema.USER_PARTY_ENV, table = createTable("USR_PARTY_ENV", "PENV", tfields));

			/***************************************************************************************************
			 * USR_USER_ENV
			 ***************************************************************************************************/
			tfields = new Table.Field[] {
					createFD(PM, "partyId", "PARTYID", "PARTYID", 0, 30),
					createFD(PM, "userId", "USERID", "USERID", 0, 30),
					createFD(PM, "systemCode", "SYSTEMCD", "SYS_SYSTEMCODE", 0, 30),
					createFD(PM, "envName", "NAME", "SYS_SYSTEM_ENV_NAME", STRING),
					createFD(RD, "envArray", "VALUE", "SYS_SYSTEM_ENV_ARRAY", STRING),
					createFD(RD, "status", "STATUS", "STATUS", "PUB_STATUS_", "00"),
					createFD(RD, "createDateTime", "REGDATE", "CREATEDATETIME", DATETIME),
					createFD(RD, "updateDateTime", "UPGDATE", "UPDATEDATETIME", DATETIME)
			};
			putTable(com.irt.rbm.usr.Schema.USER_USER_ENV, table = createTable("USR_USER_ENV", "UENV", tfields));
		}

		public static Table findTable( String key ) {
			return schema.getTable(key);
		}

		public static QueryFactory findQueryFactory( String key ) {
			Queryable queryable = schema.getQueryable(key);
			return ( queryable == null ? null : new QueryFactory(queryable) );
		}
	}

	@Override
	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap, String[] fieldKeys, int skipRows, int maxRows )
			throws SQLException {

		return SystemEx.getSysEnvRecords(handler);
	}

	@Override
	public boolean modify( Map<String, Object> recordMap ) throws DataException, SQLException {
		String systemCode = (String)recordMap.get("systemCode");
		String envName = (String)recordMap.get("envName");
		String envKey = (String)recordMap.get("envKey");
		String envVal = (String)recordMap.get("envVal");

		if( ( systemCode == null || systemCode.length() <= 0 )
				|| ( envName == null || envName.length() <= 0 )
				|| ( envKey == null || envKey.length() <= 0 ) ) {
			throw new DataException(DataException.ERR_ERROR, DataException.ERR_ERROR, recordMap);
		}

		String key = envName + ";" + envKey;
		if( envName.equals(envKey) ) {
			if( envVal == null || envVal.contains("=") ) {
				throw new DataException(DataException.ERR_CANNOT_UPDATE, DataException.ERR_CANNOT_UPDATE, recordMap);
			} else {
				key = envName;
			}
		}

		SystemEx.setSystemEnv(handler, systemCode, key, envVal);
		return true;
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		return modify(recordMap);
	}

	@Override
	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		// Map primary = Record.createMap("systemCode", primaryMap.get("systemCode"));
		// primary.put("envName", primaryMap.get("envGroup"));

		String systemCode = (String)primaryMap.get("systemCode");
		String envGroup = (String)primaryMap.get("envName");
		String envKey = (String)primaryMap.get("envKey");
		String envVal = (String)primaryMap.get("envVal");

		if( ( systemCode == null || systemCode.length() <= 0 )
				|| ( envGroup == null || envGroup.length() <= 0 )
				|| ( envKey == null || envKey.length() <= 0 ) ) {
			throw new DataException(DataException.ERR_ERROR, DataException.ERR_ERROR, primaryMap);
		}

		String key = envGroup + ";" + envKey;
		if( envGroup.equals(envKey) ) {
			if( envVal == null || envVal.contains("=") ) {
				throw new DataException(DataException.ERR_CANNOT_DELETE, DataException.ERR_CANNOT_DELETE, primaryMap);
			} else {
				key = envGroup;
			}
		}

		SystemEx.removeSystemEnv(handler, systemCode, key);
		return true;
	}

}
