/*
 *	File Name:	DatabaseMessageBundleControl.java
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

import com.irt.sql.BasicSQLHandler;
import com.irt.sql.SQLHandler;
import com.irt.util.MessageBundle;
import com.irt.util.Utility2;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * DatabaseResourceBundle reloaded from database by DatabaseResourceBundleControl.timeToLive.
 * 
 * SYSAdminTool has 'reloadMessageResource' clear cache now( then reload ).
 * 
 * <pre>
 * 
 * ( if it is okay SYSAdminTool?executeType=reloadMessageResource should be okay. )
 * 
 * TODO: waiting 5min is not ok.. so maybe dynamicTtl calc 
 * To control check cache ttl.
 * 
 * if resource message change is dormant(user is not changing!) set bigger value until MAX_TTL.
 * if resource message change is consistent(some admin user is changing now!) set smaller value until MIN_TTL.
 * 
private static long DEFAULT_TTL = 60000;// 60sec
private static long MIN_TTL = 15000;// 15sec
private static long MAX_TTL = 3600000;// 60min
private static int TTL_CHANGE_RATE = 80;// change 80% up or 80% down the current dynamicTtl value
 * 
 * 
 * 
 * </pre>
 * 
 * In Development(Not good for Production)
 * 
 * <pre>
 * EXEC pkSYSStandard.pPutSystemEnv( 'SYS', 'DatabaseBundle;ttl', 15000 );
 * </pre>
 *
 */
public class DatabaseMessageBundleControl extends ResourceBundle.Control implements DatabaseBundleControl {
	public static final List<String> FORMAT_DATABASE = Collections.unmodifiableList(Arrays.asList("bundle.database"));

	private static long SUGGESTED_DEVELOPER_TTL = 15000;// 15sec. in development environment.
	public static long INITIAL_TTL = 300000;// 30*10sec.
	private static long MINIMUM_TTL = 10000;
	private static long DEFAULT_TTL = 3000000;// 5*10min(resource bundle refreshes by this value)

	private static DatabaseMessageBundleControl DB_CONTROL = null;

	private static DatabaseResourceRepository messageRepository;

	public static DatabaseMessageBundleControl getMaybeInstance() {
		return DB_CONTROL;
	}

	public static DatabaseMessageBundleControl getInstance( DataSource dataSource ) {
		if( DB_CONTROL == null ) {
			synchronized( DatabaseMessageBundleControl.class ) {
				if( DB_CONTROL == null ) {
					DB_CONTROL = new DatabaseMessageBundleControl();
				}

				if( DB_CONTROL.dataSource == null ) {
					DB_CONTROL.setDataSource(dataSource);
					Logger.getRootLogger().debug("dataSource set from " + DB_CONTROL.dataSource);
				}
				if( !dataSource.equals(DB_CONTROL.dataSource) ) {
					DB_CONTROL.setDataSource(dataSource);
					Logger.getRootLogger().debug("dataSource replaced from " + DB_CONTROL.dataSource + " to " + dataSource);
				}

			}
		}

		return DB_CONTROL;
	}

	org.slf4j.Logger LOG = LoggerFactory.getLogger(DatabaseMessageBundleControl.class);

	private DataSource dataSource;

	/** ResourceBundle Cache Life ( = ResourceBundle will refresh every 'timeToLive' value ) */
	private volatile long timeToLive = INITIAL_TTL;

	private ResourceBundle fallbackResourceBundleParent;

	private ResourceBundle getDefaultFallbackPropertyResourceBundleParent( String baseName, Locale locale ) {
		ResourceBundle fallbackParent = null;
		try {
			ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES);
			fallbackParent = control.newBundle(baseName, locale, ResourceBundle.Control.FORMAT_PROPERTIES.get(0), this.getClass().getClassLoader(),
					false);
		} catch( IllegalAccessException ignored ) {
			LOG.warn("{}", ignored);
		} catch( InstantiationException ignored ) {
			LOG.warn("{}", ignored);
		} catch( IOException ignored ) {
			LOG.warn("{}", ignored);
		}

		return fallbackParent;
	}

	@Override
	public List<String> getFormats( String baseName ) {
		if( baseName == null )
			throw new NullPointerException();

		return FORMAT_DATABASE;
	}

	private DatabaseResourceRepository getRepoInstance() {
		if( messageRepository == null ) {
			MessageBundle msghandler = new MessageBundle(new ResourceBundle[] {});
			SQLHandler handler;
			try {
				handler = new BasicSQLHandler(dataSource.getConnection(), msghandler, TimeZone.getTimeZone("UTC"));
				messageRepository = new DatabaseResourceRepositoryImpl(handler);
			} catch( SQLException sqlEx ) {
				LOG.error("SQLException occurred( fallbackResourceBundle will be used. )", sqlEx);
			}
		}

		String ttl = Utility2.getJvmSystemEnv("SYS", "DatabaseResource;ttl", String.valueOf(INITIAL_TTL));
		if( ttl != null ) {
			long configuredTtl = Long.parseLong(ttl, 10);
			if( configuredTtl < MINIMUM_TTL ) {
				LOG.warn(
						"Please check the SystemEnv configuration. 'timeToLive' from SystemEnv does not meet minimum ttl: " + MINIMUM_TTL);
			} else {
				synchronized( DatabaseMessageBundleControl.class ) {
					boolean needsToUpdate = ( configuredTtl != timeToLive );
					if( needsToUpdate ) {
						timeToLive = configuredTtl;
						LOG.trace("timeToLive set from SystemEnv value : '" + configuredTtl + "'");
					}
				}
			}
		}

		return messageRepository;
	}

	@Override
	public long getTimeToLive( String baseName, Locale locale ) {
		if( baseName == null || locale == null )
			throw new NullPointerException();

		return timeToLive;
	}

	@Override
	public boolean needsReload( String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime ) {
		LOG.trace(
				"check needsReload() for " + baseName + " : " + locale + " : " + format + " : " + loader + " : " + bundle + " : "
						+ loadTime);
		if( !FORMAT_DATABASE.contains(format) || getRepoInstance() == null ) {
			return super.needsReload(baseName, locale, format, loader, bundle, loadTime);
		}

		try {
			String bundleName = toBundleName(baseName, locale);
			Long newestTime = getRepoInstance().findNewestTimestamp(bundleName);

			boolean bundleHasData = bundle.getKeys().hasMoreElements();

			// return true if the database table has been emptied, but the bundle is not.
			// return true if the latest timestamp is newer than the supplied loadTime of the current bundle.
			boolean ret = needsReload(loadTime, newestTime, bundleHasData);
			if( ret == true ) {
				Logger.getRootLogger().trace(
						"check needsReload(tf: " + ret + ") for " + baseName + " : " + locale + " : " + format + " : " + loader + " : " + bundle
								+ " : "
								+ loadTime);
				return ret;
			}
		} catch( SQLException sqlEx ) {
			LOG.error("DatabaseResourceBundle needsReload check error: ", sqlEx);
		}

		return super.needsReload(baseName, locale, format, loader, bundle, loadTime);
	}

	/**
	 * 
	 * @param lastLoadTime
	 * @param newestTime
	 * @param bundleHasData
	 * @return // return true if the database table has been emptied, but the bundle is not.
	 *         // return true if the latest timestamp is newer than the supplied loadTime of the current bundle.
	 */
	public static boolean needsReload( Long lastLoadTime, Long newestTime, boolean bundleHasData ) {
		if( lastLoadTime == null )
			return false;

		boolean dbIsEmptyButBundleExist = ( newestTime == null && bundleHasData );

		return ( dbIsEmptyButBundleExist || ( newestTime != null && newestTime > lastLoadTime ) );
	}

	@Override
	public ResourceBundle newBundle( String baseName, Locale locale, String format, ClassLoader loader, boolean reload )
			throws IllegalAccessException, InstantiationException, IOException {
		Locale pureLocale = DatabaseResourceRepositoryImpl.BundleNaming.getPureLocale(locale);

		ResourceBundle fallbackResourceBundle = ( fallbackResourceBundleParent == null )
				? getDefaultFallbackPropertyResourceBundleParent(baseName, locale)
				: fallbackResourceBundleParent;

		try {
			String bundleName = null;
			try {
				bundleName = toBundleName(baseName, pureLocale);
			} catch( Exception ex ) {
				LOG.warn("uncheckedException: ", ex);
			}

			DatabaseMessageBundle bundle = new DatabaseMessageBundle(getRepoInstance(), bundleName, format, loader, reload);
			if( fallbackResourceBundle != null ) {
				LOG.debug(this + " set fallback parent resource bundle: " + fallbackResourceBundle);
				bundle.setFallbackParent(fallbackResourceBundle);
			}
			return bundle;
		} catch( Exception ex ) {
			LOG.warn("uncheckedException: ", ex);
		}

		return null;
	}

	public void setDataSource( DataSource dataSource ) {
		this.dataSource = dataSource;
	}

	public void setFallbackResourceBundleParent( ResourceBundle fallbackResourceBundleParent ) {
		this.fallbackResourceBundleParent = fallbackResourceBundleParent;
	}

	public void setTimeToLive( long timeToLive ) {
		synchronized( DatabaseMessageBundleControl.class ) {
			if( this.timeToLive != timeToLive ) {
				this.timeToLive = timeToLive;
			}
		}
	}

}
