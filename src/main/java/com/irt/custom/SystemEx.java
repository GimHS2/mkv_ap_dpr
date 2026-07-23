/*
 *	File Name:	SystemEx.java
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

package com.irt.custom;

import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.rbm.RBMSystem;
import com.irt.rbm.tools.extra.SchemaTableDdl;
import com.irt.sql.QueryBuffer;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Schema;
import com.irt.sql.Table;
import com.irt.system.SystemConfigManager;
import com.irt.util.FileUtil;
import com.irt.util.Utility2;
import com.irt.util.cst.ReflectUtil;
import com.irt.util.cst.SchemaTableFinder;

import java.io.File;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class SystemEx {
	public static final String DEFAULT_EMBDB_DATASOURCE_NAME = "emb";

	public static final String DEFAULT_RBMDB_DATASOURCE_NAME = "rbm";

	public static String[] MANDATORY_SYS_TABLES = { "com.irt.rbm.sys.Schema;System", "com.irt.rbm.sys.Schema;SystemEnvironment" };

	private static final String TEMPLATE_EMBDB_DATASOURCE_URL = "jdbc:h2:mem:${dataSource.name};MODE=ORACLE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

	private static final String TEMPLATE_TEMP_DIRECTORY = "${tempDirectory}/tools/tmp";
	private static final String[] EMBDB_MANDATORY_SYS_TABLES = { "com.irt.rbm.sys.Schema;System", "com.irt.rbm.sys.Schema;SystemEnvironment" };

	static final String REGEX_SPLIT_ENVENTRY = "([^;]+=[^;]+|.*)";
	static final String REGEX_ENV_VAL = "[^;]+;?";

	static final String TEMPLATE_UPDATE_OF_SYSTEM_ENV = ""
			+ "UPDATE ${envTable,SYS_SYSTEM_ENV} SENV"
			+ "	SET	SENV.VALUE= "
			+ "		(CASE WHEN '${envKey}' = SENV.NAME "
			+ "				THEN '${envVal}'"
			+ "			WHEN '${envKey}' IS NOT NULL AND '${envVal}' IS NULL"
			+ "				THEN REGEXP_REPLACE(SENV.VALUE, '${envKey}=" + REGEX_ENV_VAL + "','')"
			+ "			ELSE"
			+ "				REGEXP_REPLACE(SENV.VALUE, '${envKey}=" + REGEX_ENV_VAL + "','${envKey}=${envVal};')"
			+ "		END)"
			+ "	WHERE	SENV.SYSTEMCD = ?"
			+ "		AND SENV.NAME = ?"
			+ "";

	static final String TEMPLATE_UPDATE_OF_USR_PARTY_ENV = ""
			+ "UPDATE ${envTable} SENV"
			+ "	SET	SENV.VALUE= "
			+ "		(CASE WHEN '${envKey}' = SENV.NAME "
			+ "				THEN '${envVal}'"
			+ "			WHEN '${envKey}' IS NOT NULL AND '${envVal}' IS NULL"
			+ "				THEN REGEXP_REPLACE(SENV.VALUE, '${envKey}=" + REGEX_ENV_VAL + "','')"
			+ "			ELSE"
			+ "				REGEXP_REPLACE(SENV.VALUE, '${envKey}=" + REGEX_ENV_VAL + "','${envKey}=${envVal};')"
			+ "		END)"
			+ "	WHERE	SENV.SYSTEMCD = ?"
			+ "		AND SENV.NAME = ?"
			+ "		AND SENV.PARTYID = ?"
			+ "";

	static final String TEMPLATE_UPDATE_OF_USR_USER_ENV = ""
			+ "UPDATE ${envTable} SENV"
			+ "	SET	SENV.VALUE= "
			+ "		(CASE WHEN '${envKey}' = SENV.NAME "
			+ "				THEN '${envVal}'"
			+ "			WHEN '${envKey}' IS NOT NULL AND '${envVal}' IS NULL"
			+ "				THEN REGEXP_REPLACE(SENV.VALUE, '${envKey}=" + REGEX_ENV_VAL + "','')"
			+ "			ELSE"
			+ "				REGEXP_REPLACE(SENV.VALUE, '${envKey}=" + REGEX_ENV_VAL + "','${envKey}=${envVal};')"
			+ "		END)"
			+ "	WHERE	SENV.SYSTEMCD = ?"
			+ "		AND SENV.NAME = ?"
			+ "		AND SENV.PARTYID = ?"
			+ "		AND SENV.USERID = ?"
			+ "";

	static final String TEMPLATE_INSERT_OF_SYSTEM_ENV = ""
			+ "INSERT INTO ${envTable,SYS_SYSTEM_ENV} (SYSTEMCD, NAME, VALUE)"
			+ "VALUES( ?, ?, "
			+ "		(CASE WHEN '${envKey}' = '${envName}' "
			+ "				THEN '${envVal}'"
			+ "			ELSE"
			+ "				'${envKey}=${envVal};'"
			+ "		END)"
			+ ")";
	static final String TEMPLATE_INSERT_OF_USR_PARTY_ENV = ""
			+ "INSERT INTO ${envTable} (SYSTEMCD, NAME, VALUE, PARTYID)"
			+ "VALUES( ?, ?, "
			+ "		(CASE WHEN '${envKey}' = '${envName}' "
			+ "				THEN '${envVal}'"
			+ "			ELSE"
			+ "				'${envKey}=${envVal};'"
			+ "		END),"
			+ "		?"
			+ ")";

	static final String TEMPLATE_INSERT_OF_USR_USER_ENV = ""
			+ "INSERT INTO ${envTable} (SYSTEMCD, NAME, VALUE, PARTYID, USERID)"
			+ "VALUES( ?, ?, "
			+ "		(CASE WHEN '${envKey}' = '${envName}' "
			+ "				THEN '${envVal}'"
			+ "			ELSE"
			+ "				'${envKey}=${envVal};'"
			+ "		END),"
			+ "		?, ?"
			+ ")";

	static final String TEMPLATE_CONF_CONTENT = ""
			+ "\n" + "	systemName									=	RBM System"
			+ "\n" + "	messageSource								=	${messageSource,com.irt.rbm.mesg.RBMMessages}"
			+ "\n" + "	tempDirectory								=	${tempDirectory}"
			// + "\n" + " DataSource.dpr.driverClassName = oracle.jdbc.driver.OracleDriver"
			// + "\n" + " DataSource.dpr.url = jdbc:oracle:thin:@192.168.0.15:1521/RBM2"
			// + "\n" + " DataSource.${dataSource.name}.driverClassName = ${dataSource.driverClassName}"
			+ "\n" + "	DataSource.default							=	${dataSource.name}"
			+ "\n" + "	DataSource.${dataSource.name}.url			=	${dataSource.url}"
			+ "\n" + "	DataSource.${dataSource.name}.username		=	${dataSource.username}"
			+ "\n" + "	DataSource.${dataSource.name}.password		=	${dataSource.password}"
			+ "\n" + "	DataSource.${dataSource.name}.maxActive		=	20"
			+ "\n" + "	DataSource.${dataSource.name}.maxWait		=	10000"
			+ "\n" + "	RBMSystem.SQLHandler				=	com.irt.rbm.RBMDataHandler"
			+ "\n" + "	RBMSystem.dataSource				=	${dataSource.name}"
			+ "\n" + "";

	public final static int OPT_INIT_SYSTEMENV = 0x01;

	public final static int OPT_INIT_CUSTOMSYS = 0x02;

	private static String compileProperties( Map<String, Object> dsMap ) {
		return PatternRecordFormat.getInstance(TEMPLATE_CONF_CONTENT).format(dsMap, null);
	}

	private static Map<String, Object> createDataSourceMap( final Properties props, final String dsName ) {
		String url = getDbUrl(props, dsName, null);
		String username = getDbUsername(props, dsName, null);
		String password = getDbPassword(props, dsName, null);
		String driverClassName = DriverClassName.None.getDriverClassName(url).value;

		Map<String, Object> map = Record.createMap("dataSource.name", dsName);
		map.put("dataSource.url", url);
		map.put("dataSource.username", username);
		map.put("dataSource.password", password);
		map.put("dataSource.driverClassName", driverClassName);

		return map;
	}

	public static com.irt.system.SystemConfig createEMBSystem() throws SQLException, IOException {
		return createEMBSystem(null, null, null, null);
	}

	public static com.irt.system.SystemConfig createEMBSystem( Properties extraProps ) throws SQLException, IOException {
		return createEMBSystem(null, null, null, extraProps);
	}

	public static com.irt.system.SystemConfig createEMBSystem( String url, String username, String password, Properties extraProps )
			throws SQLException, IOException {
		String dsName = DEFAULT_EMBDB_DATASOURCE_NAME;

		String tempDirString = System.getProperty("user.dir");
		if( url == null && username == null && password == null ) {
			url = PatternRecordFormat.getInstance(TEMPLATE_EMBDB_DATASOURCE_URL).format(Record.createMap("dataSource.name", dsName), null);
			username = "sa";
			password = "";
			Map<String, Object> dsMap = getRawDataSourceMap(url, username, password, dsName, tempDirString);

			File tempDirectory = getCreatedTempDirectory(tempDirString);

			Properties props = getRawSystemProperties(dsMap, extraProps);

			Properties dsProps = new Properties();
			DriverClassName.None.getDriverClassName(url);

			createTables(props, dsName, tempDirectory, EMBDB_MANDATORY_SYS_TABLES);

			return new SystemEx().createSystem(dsMap, extraProps);
		} else {
			if( url.startsWith("jdbc:h2") ) {
				Map<String, Object> dsMap = getRawDataSourceMap(url, username, password, dsName, tempDirString);
				return new SystemEx().createSystem(dsMap, extraProps);
			} else {
				throw new IllegalStateException("currently only support h2.");
			}
		}
	}

	public static void createLinkedTables( Properties tableSourceProps, String tableSourceDsName, Properties executionProps, String executionDsName,
			File tempDirectory,
			Class<? extends Schema>... schemaClasses ) throws IOException, SQLException {
		try {
			Map<String, Table> schemaTableMap = SystemEx.getSchemaTableMap(schemaClasses);

			createLinkedTables(tableSourceProps, tableSourceDsName, executionProps, executionDsName, tempDirectory,
					schemaTableMap.values().toArray(new Table[0]));
		} catch( IllegalArgumentException e ) {
			throw new IOException(e);
		} catch( IllegalAccessException e ) {
			throw new IOException(e);
		}
	}

	static void createLinkedTables( Properties tableSourceProps, String tableSourceDsName, Properties executionProps, String executionDsName,
			File tempDirectory,
			Table... sourceSchemaTables )
			throws IOException, SQLException {
		for( Table schemaTable : sourceSchemaTables ) {
			String template = SchemaTableDdl.getCreateLinkedTableTemplate(schemaTable.getTableName(), schemaTable.getTableName());

			// Map<String, Object> sourceDataSourceMap = createDataSourceMap(sourceProps, sourceDsName);
			Map<String, Object> targetDataSourceMap = createDataSourceMap(tableSourceProps, tableSourceDsName);
			String ddl = PatternRecordFormat.getInstance(template).format(targetDataSourceMap, null);

			String ddlFilename = ( schemaTable + ".createLinkedTable" );
			File ddlFile = new File(tempDirectory, ddlFilename);
			FileUtil.writeFileContent(ddlFile, ddl);

			try {
				String url = getDbUrl(executionProps, executionDsName, null);
				if( url.startsWith("jdbc:h2") ) {
					// h2RunScript(url, user, password, fileName, charset, continueOnError);
					h2RunScript(url, getDbUsername(executionProps, executionDsName, null),
							getDbPassword(executionProps, executionDsName, null),
							ddlFile.getAbsolutePath(), Charset.forName("UTF-8"), true);
				} else {
					throw new IllegalStateException("execution database should support 'CREATE LINKED TABLE' statement(eg. h2database).");
				}
			} catch( Exception ex ) {
				if( h2IsIgnoreableTableOrViewExistException(ex) ) {
					Logger.getRootLogger().warn("ignored: ", ex);
				} else {
					throw new IOException("ddl: " + ddlFilename, ex);
				}
			}
		}
	}

	public static com.irt.system.SystemConfig createRBMSystem( String url, String username, String password ) throws SQLException {
		return createRBMSystem(url, username, password, false, null);
	}

	public static com.irt.system.SystemConfig createRBMSystem( String url, String username, String password, boolean init ) throws SQLException {
		return createRBMSystem(url, username, password, init, null);
	}
	public static com.irt.system.SystemConfig createRBMSystem( String url, String username, String password, boolean init, Properties extraProps )
			throws SQLException {
		String tempDirectory = ( extraProps != null ? extraProps.getProperty("tempDirectory") : null );
		com.irt.system.SystemConfig rbm = SystemEx.getInstance(url, username, password, DEFAULT_RBMDB_DATASOURCE_NAME, tempDirectory, extraProps);

		if( rbm != null && init && !RBMSystem.initialized() ) {
			RBMSystem.initSystemEnv(rbm.createSQLHandler(rbm.getMessageHandler()));
			return rbm;
		} else {
			throw new NullPointerException("rbm system should not null. 1");
		}
	}

	public static com.irt.system.SystemConfig createRBMSystem( String url, String username, String password, int initOpt, Properties extraProps )
			throws SQLException {
		String tempDirectory = ( extraProps != null ? extraProps.getProperty("tempDirectory") : null );

		boolean initCustomSys = false;
		try {
			String dsName = DEFAULT_RBMDB_DATASOURCE_NAME;
			Properties rbmProps = SystemEx.getRawSystemProperties(url, username, password, dsName, tempDirectory, extraProps);

			if( ( initOpt & OPT_INIT_CUSTOMSYS ) > 0 ) {
				com.irt.custom.SystemConfig.initialize(rbmProps);
				initCustomSys = true;
			}
		} catch( IOException e ) {
			Logger.getRootLogger().error(e);
		} catch( Exception e ) {
			Logger.getRootLogger().error(e);
		} finally {
			if( !initCustomSys )
				throw new NullPointerException("custom system not initialized.");
		}

		com.irt.system.SystemConfig rbm = SystemEx.getInstance(url, username, password, DEFAULT_RBMDB_DATASOURCE_NAME, tempDirectory, extraProps);

		if( rbm != null ) {
			if( (initOpt & OPT_INIT_SYSTEMENV ) > 0 && !RBMSystem.initialized() ) {
				RBMSystem.initSystemEnv(rbm.createSQLHandler(rbm.getMessageHandler()));
			}
			return rbm;
		} else {
			throw new NullPointerException("rbm system should not null. 2");
		}
	}

	private static void createTables( Properties props, String dsName, File tempDirectory, String... schemaSpecs ) throws IOException, SQLException {
		for( String schemaSpec : schemaSpecs ) {
			String schemaClass = schemaSpec.split(";")[0];
			String schemaTable = schemaSpec.split(";")[1];

			String ddl = SchemaTableDdl
					.getCreateTable(SchemaTableFinder.findSchemaTable(SchemaTableDdl.class.getClassLoader(), schemaClass, schemaTable));

			String ddlFilename = ( schemaClass + "-" + schemaTable + ".create" );
			File ddlFile = new File(tempDirectory, ddlFilename);
			FileUtil.writeFileContent(ddlFile, ddl);

			try {
				String url = getDbUrl(props, dsName, null);

				h2RunScript(url, getDbUsername(props, dsName, null), getDbPassword(props, dsName, null),
						ddlFile.getAbsolutePath(), Charset.forName("UTF-8"), true);
			} catch( Exception ex ) {
				// if( ex instanceof JdbcSQLException
				// && ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1 == ( (JdbcSQLException)ex ).getErrorCode() ) {
				// Logger.getRootLogger().warn("ignored: ", ex);
				// } else {
				// throw new IOException("ddl: " + ddlFilename, ex);
				// }
				if( h2IsIgnoreableTableOrViewExistException(ex) ) {
					Logger.getRootLogger().warn("ignored: ", ex);
				} else {
					throw new IOException("ddl: " + ddlFilename, ex);
				}
			}
		}
	}

	private static void dropTables( Properties props, String dsName, File tempDirectory, String... schemaSpecs ) throws IOException, SQLException {
		for( String schemaSpec : schemaSpecs ) {
			String schemaClass = schemaSpec.split(";")[0];
			String schemaTable = schemaSpec.split(";")[1];

			String ddl = SchemaTableDdl
					.getDropTable(SchemaTableFinder.findSchemaTable(SchemaTableDdl.class.getClassLoader(), schemaClass, schemaTable));

			String ddlFilename = ( schemaClass + "-" + schemaTable + ".drop" );
			File ddlFile = new File(tempDirectory, ddlFilename);
			FileUtil.writeFileContent(ddlFile, ddl);

			try {
				h2RunScript(getDbUrl(props, dsName, null), getDbUsername(props, dsName, null), getDbPassword(props, dsName, null),
						ddlFile.getAbsolutePath(), Charset.forName("UTF-8"), true);
			} catch( Exception ex ) {
				if( h2IsIgnoreableTableOrViewExistException(ex) ) {
					Logger.getRootLogger().warn("ignored: ", ex);
				} else {
					throw new IOException("ddl: " + ddlFilename, ex);
				}
			}
		}
	}

	private static File getCreatedTempDirectory( String tempDirString ) {
		File tempDirectory = new File(PatternRecordFormat.getInstance(TEMPLATE_TEMP_DIRECTORY)
				.format(Record.createMap("tempDirectory", tempDirString), null));
		if( !tempDirectory.exists() ) {
			FileUtil.getCreatedDir(tempDirectory.getParentFile(), tempDirectory.getName());
		}
		return tempDirectory;
	}

	private static String getDbPassword( final Properties props, final String dsName, final String defaultValue ) {
		return props.getProperty("DataSource." + dsName + ".password", defaultValue);
	}

	private static String getDbUrl( Properties props, String dsName, String defaultValue ) {
		return props.getProperty("DataSource." + dsName + ".url", defaultValue);
	}

	private static String getDbUsername( final Properties props, final String dsName, final String defaultValue ) {
		return props.getProperty("DataSource." + dsName + ".username", defaultValue);
	}

	public static java.util.Properties getEMBSystemProperties() throws SQLException, IOException {
		return getEMBSystemProperties(null);
	}

	public static java.util.Properties getEMBSystemProperties( Properties extraProps ) throws SQLException, IOException {
		return getEMBSystemProperties(null, null, null, extraProps);
	}

	public static java.util.Properties getEMBSystemProperties( String url, String username, String password, Properties extraProps )
			throws SQLException, IOException {
		String dsName = DEFAULT_EMBDB_DATASOURCE_NAME;
		String tempDirString = System.getProperty("user.dir");

		if( url == null && username == null && password == null ) {
			url = PatternRecordFormat.getInstance(TEMPLATE_EMBDB_DATASOURCE_URL).format(Record.createMap("dataSource.name", dsName), null);
			username = "sa";
			password = "";

			return getRawSystemProperties(url, username, password, dsName, tempDirString, extraProps);
		} else {
			if( url.startsWith("jdbc:h2") ) {
				username = "sa";
				password = "";

				return getRawSystemProperties(url, username, password, dsName, tempDirString, extraProps);
			} else {
				throw new IllegalStateException("currently only support h2.");
			}
		}
	}

	public static List<Map<String, Object>> getEnvDataRecords( EnvData envTable, SQLHandler handler ) throws SQLException {
		QueryBuffer querybuf = new QueryBuffer();

		querybuf.appendOrderByFieldName("systemCode");
		querybuf.appendOrderByFieldName("envName");

		querybuf.appendTableWithAlias(envTable.table, "SENV");
		if( envTable == EnvData.USRPTYENV ) {
			querybuf.appendDataWithAlias("PARTYID", "partyId");
			querybuf.appendOrderByFieldName("partyId");
		} else if( envTable == EnvData.USRUSRENV ) {
			querybuf.appendDataWithAlias("PARTYID", "partyId");
			querybuf.appendDataWithAlias("USERID", "userId");
			querybuf.appendOrderByFieldName("partyId");
			querybuf.appendOrderByFieldName("userId");
		}
		querybuf.appendDataWithAlias("SYSTEMCD", "systemCode");
		querybuf.appendDataWithAlias("NAME", "envName");
		querybuf.appendDataWithAlias("VALUE", "envArray");
		querybuf.appendDataWithAlias("STATUS", "status");
		querybuf.appendDataWithAlias("REGDATE", "createDateTime");
		querybuf.appendDataWithAlias("UPGDATE", "updateDateTime");

		List<Map<String, Object>> list = SQLManager.getRecordList(handler, querybuf);
		if( list == null )
			return null;

		List<Map<String, Object>> envKeyValues = new ArrayList<Map<String, Object>>();

		for( Map<String, Object> map : list ) {
			String name = (String)map.get("envName");
			String value = (String)map.get("envArray");
			int idx1 = 0, idx2 = 0;
			do {
				idx2 = value.indexOf(';', idx1);
				while( idx2 >= 0 && ( idx2 + 1 ) < value.length() && value.charAt(idx2 + 1) == ';' )
					idx2 = value.indexOf(';', idx2 + 2);

				String[] key_n_value;
				if( idx2 < 0 )
					key_n_value = value.substring(idx1).replaceAll(";;", ";").split("=", 2);
				else
					key_n_value = value.substring(idx1, idx2).replaceAll(";;", ";").split("=", 2);
				if( key_n_value.length == 2 ) {
					Map<String, Object> newMap = new TreeMap<String, Object>(map);
					newMap.put("envKey", key_n_value[0]);
					newMap.put("envVal", key_n_value[1]);
					envKeyValues.add(newMap);
				}
				idx1 = idx2 + 1;
			} while( idx1 > 0 );

			if( idx1 == 0 ) {
				if( value != null && !value.contains("=") ) {
					Map<String, Object> newMap = new TreeMap<String, Object>(map);
					newMap.put("envKey", name);
					newMap.put("envVal", value);
					envKeyValues.add(newMap);
				}
			}
		}

		return envKeyValues;
	}

	public static com.irt.system.SystemConfig getInstance( String url, String username, String password, String dsName, String tempDirectory ) {
		return getInstance(url, username, password, dsName, tempDirectory, null);
	}

	public static com.irt.system.SystemConfig getInstance( String url, String username, String password, String dsName, String tempDirectory,
			Properties extraProps ) {
		Map<String, Object> map = getRawDataSourceMap(url, username, password, dsName, tempDirectory);
		return new SystemEx().createSystem(map, extraProps);
	}

	private static java.util.Map<String, Object> getRawDataSourceMap( String url, String username, String password, String dsName,
			String tempDirectory ) {
		Map<String, Object> dsMap = Record.createMap("dataSource.name", dsName);
		dsMap.put("dataSource.url", url);
		dsMap.put("dataSource.username", username);
		dsMap.put("dataSource.password", password);
		dsMap.put("tempDirectory", tempDirectory);

		return dsMap;
	}

	public static java.util.Properties getRawSystemProperties( Map<String, Object> dsMap, Properties extraProps ) throws SQLException, IOException {

		Properties props = new Properties();

		if( extraProps != null )
			props.putAll(extraProps);

		for( Entry entry : extraProps.entrySet() ) {
			if( !dsMap.containsKey(entry.getKey()) || dsMap.get(entry.getKey()) == null ) {
				if( entry.getValue() instanceof File ) {
					dsMap.put((String)entry.getKey(), ( (File)entry.getValue() ).getAbsoluteFile());
				} else {
					dsMap.put((String)entry.getKey(), (String)entry.getValue());
				}
			}
		}

		String compiled = compileProperties(dsMap);

		props.load(new StringBufferInputStream(compiled));

		return props;
	}

	public static java.util.Properties getRawSystemProperties( String url, String username, String password, String dsName, String tempDirectory,
			Properties extraProps ) throws SQLException, IOException {

		Map<String, Object> dsMap = getRawDataSourceMap(url, username, password, dsName, tempDirectory);

		return getRawSystemProperties(dsMap, extraProps);
	}

	public static java.util.Properties getRBMSystemProperties( String url, String username, String password ) throws SQLException, IOException {
		String dsName = DEFAULT_RBMDB_DATASOURCE_NAME;

		if( url == null && username == null && password == null ) {
			throw new IllegalStateException("Please supply mandatory parameters.");
		} else {
			if( url.startsWith("jdbc:oracle") ) {
				String tempDirString = System.getProperty("user.dir");

				return getRawSystemProperties(url, username, password, dsName, tempDirString, null);
			} else {
				throw new IllegalStateException("currently only support oracle.");
			}
		}
	}

	static Map<String, Table> getSchemaTableMap( Class<? extends com.irt.sql.Schema>... schemaClasses )
			throws IllegalArgumentException, IllegalAccessException {
		Map<String, Table> schemaTableMap = new HashMap<String, Table>();
		for( Class<? extends com.irt.sql.Schema> schemaClass : schemaClasses ) {
			Map<String, Field> fieldMap = ReflectUtil.getDeclaredFields(schemaClass, null);
			for( String fieldName : fieldMap.keySet() ) {
				Field fd = fieldMap.get(fieldName);
				if( java.lang.reflect.Modifier.isPublic(fd.getModifiers()) ) {
					if( java.lang.reflect.Modifier.isStatic(fd.getModifiers()) ) {
						if( java.lang.reflect.Modifier.isFinal(fd.getModifiers()) ) {
							Object fieldObjectValue = ReflectUtil.getDeclaredFieldObject(schemaClass, null, fieldName);

							if( fieldObjectValue != null ) {
								String schemaTableKey = (String)fieldObjectValue;
								Table table = SchemaTableFinder.findSchemaTable(schemaClass.getClassLoader(), schemaClass.getCanonicalName(),
										schemaTableKey);
								if( table != null )
									schemaTableMap.put(schemaClass.getCanonicalName() + "." + schemaTableKey, table);
							}
						}
					}
				}
			}

		}
		return schemaTableMap;
	}

	public static List<Map<String, Object>> getSysEnvRecords( SQLHandler handler ) throws SQLException {
		return getEnvDataRecords(EnvData.SYSENV, handler);
	}

	/**
	 * to keep h2database.jar file for runtime dependency( include the jar if you need )
	 */
	private static boolean h2IsIgnoreableTableOrViewExistException( Exception ex ) {
		String canIgnoreExceptionClassName = "org.h2.jdbc.JdbcSQLException";
		int canIgnoreExceptionCode = 42102;// org.h2.api.ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1 = 42102
		if( canIgnoreExceptionClassName.equals(ex.getClass().getCanonicalName()) ) {
			try {
				Object _errorCode = ReflectUtil.getDeclaredFieldObject(ex.getClass(), ex, "getErrorCode");
				if( _errorCode != null ) {
					Integer errorCode = (Integer)_errorCode;
					if( canIgnoreExceptionCode == errorCode ) {
						return true;
					}
				}
			} catch( IllegalArgumentException e ) {
			} catch( IllegalAccessException e ) {
			}
		}

		return false;
	}

	public static void h2RunScript( String url, String user, String password,
			String fileName, Charset charset, boolean continueOnError )
			throws SQLException {

		String x = "org.h2.tools.RunScript";
		try {
			Class<?> clz;
			clz = Class.forName(x);
			Object instance = clz.newInstance();

			java.lang.reflect.Method mt = clz.getMethod("process", String.class, String.class, String.class, String.class, Charset.class,
					boolean.class);

			mt.invoke(instance, url, user, password, fileName, charset, continueOnError);
		} catch( Exception ex ) {
			Logger.getRootLogger().warn(ex);
		}
	}

	public static synchronized void initEmbedDb( File tempDirectory, Properties properties ) throws IOException, SQLException {
		initEmbedDb(tempDirectory, properties, DEFAULT_EMBDB_DATASOURCE_NAME);
	}

	public static synchronized void initEmbedDb( File tempDirectory, Properties properties, String dsName ) throws IOException, SQLException {
		createTables(properties, dsName, tempDirectory, MANDATORY_SYS_TABLES);
	}

	public static void initializeCustomConfig( Properties properties ) throws Exception {
		com.irt.custom.SystemConfig.initialize(properties);
	}

	public static void injectRBMSystemAppSystemEnv( String ctxAppId ) {
		String reloadableKeys = RBMSystem.getSystemEnv("SYS", "AppSystemEnv" + "#" + AppEnv.getAppIdMarker(ctxAppId) + ";reloadableKeys");
		if( reloadableKeys != null ) {
			String[] jvmkeys = reloadableKeys.split(",");
			if( jvmkeys != null ) {
				for( String thekey : jvmkeys ) {
					if( thekey != null ) {
						String[] envkey = thekey.split("\\.");
						if( envkey != null && envkey.length == 3 ) {
							String toInject = RBMSystem.getSystemEnv(envkey[0], AppEnv.getAppSystmeEnvKey(ctxAppId, envkey[1]) + ";" + envkey[2]);
							if( toInject != null ) {
								String beforeValue = AppEnv.instance().setAppSystemEnv(envkey[0], envkey[1] + ";" + envkey[2], toInject);
								if( AppEnv.instance().getProperty(thekey) != null && toInject.equals(AppEnv.instance().getProperty(thekey)) ) {
									Logger.getRootLogger()
											.info("AppSystemEnv applied. key: " + thekey + " value: " + toInject + " beforevalue: " + beforeValue);
								}
							}
						}
					}
				}
			}
		}
	}

	/** inject for jvm system wide enviroment */
	public static void injectRBMSystemJvmSystemEnv() {
		injectRBMSystemJvmSystemEnv(null);
	}

	/** inject for application level environment(eg. multiple webapp in single tomcat server ) */
	public static void injectRBMSystemJvmSystemEnv( String ctxAppId ) {
		String ctxAppIdMarker = ( ctxAppId == null ? "" : "#" + ctxAppId );

		String reloadableKeys = RBMSystem.getSystemEnv("SYS", "JvmSystemEnv" + ctxAppIdMarker + ";reloadableKeys");
		if( reloadableKeys != null ) {
			String[] jvmkeys = reloadableKeys.split(",");
			if( jvmkeys != null ) {
				for( String jvmkey : jvmkeys ) {
					if( jvmkey != null ) {
						String[] envkey = jvmkey.split("\\.");
						if( envkey != null && envkey.length == 3 ) {
							String toInject = RBMSystem.getSystemEnv(envkey[0], envkey[1] + ";" + envkey[2]);
							if( toInject != null ) {
								String beforeValue = Utility2.setJvmSystemEnv(envkey[0], envkey[1] + ";" + envkey[2], toInject);
								if( System.getProperty(jvmkey) != null && toInject.equals(System.getProperty(jvmkey)) ) {
									Logger.getRootLogger()
											.info("JvmSystemEnv applied. key: " + jvmkey + " value: " + toInject + " beforevalue: " + beforeValue);
								}
							}
						}
					}
				}
			}
		}
	}

	public static void removeSystemEnv( SQLHandler handler, String systemCode, String key ) throws DataException, SQLException {
		String[] keys = key.split(";", 2);
		String envName = keys[0];
		String envKey = ( keys.length < 2 || keys[1] == null ) ? envName : keys[1];
		String envVal = null;

		Map<String, Object> recordMap = Record.createMap("envKey", envKey);
		recordMap.put("envVal", envVal);

		String statement = PatternRecordFormat.getInstance(TEMPLATE_UPDATE_OF_SYSTEM_ENV).format(recordMap, null);

		SQLManager.executeStatement(handler, statement, new Object[] { systemCode, envName });
	}

	public static void setEnvData( EnvData envTable, SQLHandler handler, String systemCode, String key, String value, String partyId, String userId )
			throws DataException, SQLException {
		String[] keys = key.split(";", 2);
		String envName = keys[0];
		String envKey = ( keys.length < 2 || keys[1] == null ) ? envName : keys[1];
		String envVal = value;

		Map<String, Object> recordMap = Record.createMap("envKey", envKey);
		recordMap.put("envVal", envVal);
		recordMap.put("envName", envName);
		recordMap.put("envTable", envTable.table);

		List<Object> bindList = new ArrayList<Object>();
		bindList.add(systemCode);
		bindList.add(envName);
		if( envTable == EnvData.USRPTYENV ) {
			bindList.add(partyId);
		} else if( envTable == EnvData.USRUSRENV ) {
			bindList.add(partyId);
			bindList.add(userId);
		}
		Object[] bindVars = bindList.toArray();

		String u_template = null;
		switch( envTable ) {
		case USRPTYENV:
			u_template = TEMPLATE_UPDATE_OF_USR_PARTY_ENV;
			break;
		case USRUSRENV:
			u_template = TEMPLATE_UPDATE_OF_USR_USER_ENV;
			break;
		default:
			u_template = TEMPLATE_UPDATE_OF_SYSTEM_ENV;
		}
		String statement = PatternRecordFormat.getInstance(u_template).format(recordMap, null);
		int ret = SQLManager.executeStatement(handler, statement, bindVars);
		// System.out.println("update: " + envName + ";" + envKey + " -->" + statement);
		if( ret <= 0 ) {
			String i_template = null;
			switch( envTable ) {
			case USRPTYENV:
				i_template = TEMPLATE_INSERT_OF_USR_PARTY_ENV;
				break;
			case USRUSRENV:
				i_template = TEMPLATE_INSERT_OF_USR_USER_ENV;
				break;
			default:
				i_template = TEMPLATE_INSERT_OF_SYSTEM_ENV;
			}
			statement = PatternRecordFormat.getInstance(i_template).format(recordMap, null);
			// System.out.println("insert: " + envName + ";" + envKey + " -->" + statement);

			ret = SQLManager.executeStatement(handler, statement, bindVars);
		}
	}

	/**
	 * CAUTION: do commit manually.
	 */
	public static void setSystemEnv( SQLHandler handler, String systemCode, String key, String value ) throws DataException, SQLException {
		String[] keys = key.split(";", 2);
		String envName = keys[0];
		String envKey = ( keys.length < 2 || keys[1] == null ) ? envName : keys[1];
		String envVal = value;

		Map<String, Object> recordMap = Record.createMap("envKey", envKey);
		recordMap.put("envVal", envVal);
		recordMap.put("envName", envName);

		Object[] bindVars = new Object[] { systemCode, envName };

		String statement = PatternRecordFormat.getInstance(TEMPLATE_UPDATE_OF_SYSTEM_ENV).format(recordMap, null);
		int ret = SQLManager.executeStatement(handler, statement, bindVars);
		// System.out.println("update: " + envName + ";" + envKey + " -->" + statement);
		if( ret <= 0 ) {
			statement = PatternRecordFormat.getInstance(TEMPLATE_INSERT_OF_SYSTEM_ENV).format(recordMap, null);
			// System.out.println("insert: " + envName + ";" + envKey + " -->" + statement);

			ret = SQLManager.executeStatement(handler, statement, bindVars);
		}
	}

	public static List<Map<String, Object>> syncSystemEnvRecords( SQLHandler from, SQLHandler to ) throws SQLException, DataException {
		List<Map<String, Object>> list = SystemEx.getSysEnvRecords(from);
		int count = 0;
		if( list != null ) {
			for( Map<String, Object> map : list ) {
				String envName = (String)map.get("envName");
				String envKey = (String)map.get("envKey");
				String value = (String)map.get("envVal");

				String key = ( envName.equals(envKey) ? envName : envName + ";" + envKey );

				SystemEx.setSystemEnv(to, (String)map.get("systemCode"), key, value);
				count++;
			}
		}

		return SystemEx.getSysEnvRecords(to);
	}

	public com.irt.system.SystemConfig createSystem( Map<String, Object> dsMap ) {
		return createSystem(dsMap, null);
	}

	public com.irt.system.SystemConfig createSystem( Map<String, Object> dsMap, Properties extraProps ) {
		Properties props = null;
		try {
			props = getRawSystemProperties(dsMap, extraProps);// order matter( dsMap is higher priority )
		} catch( SQLException e1 ) {
			e1.printStackTrace();
		} catch( IOException e1 ) {
			e1.printStackTrace();
		}

		SystemConfigManager mgr = null;
		try {
			mgr = SystemConfigManager.load(props);
			return mgr.getSystemConfig("RBM");
		} catch( Exception e ) {
			Logger.getRootLogger().error(com.irt.util.TraceHelper.formatCurrentStacktrace(), e);
		}

		return null;
	}

	private enum DriverClassName {
		Oracle( "oracle.jdbc.OracleDriver" ), MySql( "com.mysql.jdbc.Driver" ), MariaDb( "org.mariadb.jdbc.Driver" ), //
		PostgreSql( "org.postgresql.Driver" ), MsSql( "com.microsoft.sqlserver.jdbc.SQLServerDriver" ), H2( "org.h2.Driver" ), //
		None( "" );

		final String value;

		DriverClassName( String name ) {
			this.value = name;
		}

		public DriverClassName getDriverClassName( String url ) {
			if( url == null )
				return DriverClassName.None;

			if( url.startsWith("jdbc:oracle") ) {
				return Oracle;
			} else if( url.startsWith("jdbc:postgresql") ) {
				return PostgreSql;
			} else if( url.startsWith("jdbc:mysql") ) {
				return MySql;
			} else if( url.startsWith("jdbc:mariadb") ) {
				return MariaDb;
			} else if( url.startsWith("jdbc:h2") ) {
				return H2;
			} else if( url.startsWith("jdbc:sqlserver") ) {
				return MsSql;
			} else {
				return None;
			}
		}
	}

	public enum EnvData {
		SYSENV( "SYS_SYSTEM_ENV" ), USRPTYENV( "USR_PARTY_ENV" ), USRUSRENV( "USR_USER_ENV" );

		final String table;

		private EnvData( String table ) {
			this.table = table;
		}

	}

}
