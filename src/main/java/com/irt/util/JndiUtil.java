package com.irt.util;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

/**
 * 
 * example
 * 
 *  - Define Order Matters
 * 
 * <pre>
 * 
 * RBMSystem.SQLHandler				=	com.irt.rbm.RBMDataHandler
 * RBMSystem.jndi					=	jdbc/myRBM2
 * 
 * DataSource.rbm2._className		= org.apache.commons.dbcp.BasicDataSource
 * DataSource.rbm2._jndiName		= jdbc/myRBM2
 * DataSource.rbm2.url				= jdbc:oracle:thin:@192.168.0.15:1521/RBM2
 * DataSource.rbm2.username			= rbmadmin
 * DataSource.rbm2.password			= rbmadmin
 * 
 * DataSource.hikari._className		= com.zaxxer.hikari.HikariDataSource
 * DataSource.hikari._jndiName		= jdbc/myHikariRBM2
 * DataSource.hikari.url			= jdbc:oracle:thin:@192.168.0.15:1521/RBM2
 * DataSource.hikari.username		= rbmadmin
 * DataSource.hikari.password		= rbmadmin
 * 
 * DataSource.emb._className		= org.h2.jdbcx.JdbcDataSource
 * DataSource.emb._jndiName			= jdbc/myEMB
 * DataSource.emb.url				= jdbc:h2:mem:emb;MODE=ORACLE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;
 * DataSource.emb.user				= sa
 * DataSource.emb.password			=
 * 
 * </pre>
 * 
 *
 */
public class JndiUtil {

	static String[] parseJndiDataSource( Properties properties ) {
		List<String> prefixes = new ArrayList<String>();
		for( Entry<Object, Object> entry : properties.entrySet() ) {
			String key = (String)entry.getKey();
			if( key.startsWith("DataSource.") ) {
				String[] keys = key.split("\\.");
				String sourceName = keys[1];

				if( properties.get("DataSource." + sourceName + "." + "_className") != null
						&& properties.get("DataSource." + sourceName + "." + "_jndiName") != null ) {

					String prefix = "DataSource." + sourceName + ".";
					if( !prefixes.contains(prefix) ) {
						prefixes.add(prefix);
					}
				}
			}
		}

		return prefixes.toArray(new String[0]);
	}

	public synchronized static void setupDataSourceInitialContext( final Properties properties ) {
		String sourceName = properties.getProperty("jndiDataSourceNames");
		if( sourceName != null && sourceName.contains(",") ) {
			String[] sourceNames = sourceName.split(",");

			List<String> prefixes = new ArrayList<String>();
			for( String name : sourceNames ) {
				prefixes.add("DataSource." + name + ".");
			}

			setupDataSourceInitialContext(properties, prefixes.toArray(new String[0]));
		} else {
			String[] prefixes = parseJndiDataSource(properties);
			if( prefixes.length > 0 ) {
				setupDataSourceInitialContext(properties, prefixes);
			}
		}
	}

	static synchronized void setupDataSourceInitialContext( final Properties properties, final String[] prefixes ) {
		try {
			NamingManager.setInitialContextFactoryBuilder(new InitialContextFactoryBuilder() {

				@Override
				public InitialContextFactory createInitialContextFactory( Hashtable<?, ?> environment ) throws NamingException {
					return new InitialContextFactory() {

						@Override
						public Context getInitialContext( Hashtable<?, ?> environment ) throws NamingException {
							return new InitialContext() {

								private final static String JAVA_COMPNENT_ENV_PREFIX = "java:/comp/env/";

								private Hashtable<String, DataSource> dataSources = new Hashtable<String, DataSource>();

								@Override
								public Object lookup( String name ) throws NamingException {

									if( dataSources.isEmpty() ) { // init datasources

										Properties prop = new Properties();
										prop.putAll(properties);
										try {
											for( int i = 0; i < prefixes.length; i++ ) {
												String prefix = prefixes[i];
												if( !prefix.endsWith(".") )
													prefix = prefix + ".";

												String jndiName = prop.getProperty(prefix + "_jndiName");

												String className = prop.getProperty(prefix + "_className");
												Class<?> clazz = Class.forName(className);
												Object instance = clazz.newInstance();
												prop.remove(prefix + "_className");

												prop.remove(prefix + "_jndiName");

												PropertySetter.setProperties(instance, prop, prefix);
												dataSources.put(JAVA_COMPNENT_ENV_PREFIX + jndiName, (DataSource)instance);

											}
										} catch( InvocationTargetException e ) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch( IntrospectionException e ) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch( InstantiationException e ) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch( IllegalAccessException e ) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch( ClassNotFoundException e ) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

										// add more datasources to the list as necessary
									}

									if( dataSources.containsKey(name) ) {
										return dataSources.get(name);
									}

									throw new NamingException("Unable to find datasource: " + name);
								}
							};
						}

					};
				}

			});
		} catch( NamingException ne ) {
			ne.printStackTrace();
		}
	}

}
