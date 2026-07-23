/*
 *	File Name:	ContextServiceInitiator.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.0	create
**/

package com.irt.servlet;

import com.irt.custom.ThreadFactoryBuilder;

import java.io.File;
import java.util.concurrent.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * used by {@link ServletContextListener} to setup AsyncRequestProcess or AsyncServlet or other thread task
 */
public class ContextServiceInitiator {

	public static final int DEFAULT_CORE_POOL_SIZE = 10;
	public static final int DEFAULT_MAX_POOL_SIZE = 50;

	public static final long DEFAULT_KEEP_ALIVE_TIME_MILLI = 50000L;// 50 sec

	/**
	 * @param storeDir
	 * @param mapdbName
	 *            : usually ".mapdb" extension. if not add ".mapdb" extension.
	 * @return
	 */
	public static DB createMapdb( File storeDir, String mapdbName ) {
		return createMapdb(storeDir.getAbsolutePath(), mapdbName);
	}

	/**
	 * @param storeDir
	 * @param mapdbName
	 *            : usually ".mapdb" extension. forces add ".mapdb" extension.
	 * @return
	 */
	public static DB createMapdb( String storeDir, String mapdbName ) {
		if( !mapdbName.endsWith(".mapdb") ) {
			mapdbName = mapdbName + ".mapdb";
		}
		File mapdbFile = new File(storeDir, mapdbName);
		if( !mapdbFile.getParentFile().exists() )
			mapdbFile.getParentFile().mkdirs();

		return DBMaker
				.newFileDB(mapdbFile)
				.mmapFileEnableIfSupported()// if 32bit, disable mmap for safety.
				.closeOnJvmShutdown()
				.make();
	}

	/**
	 *
	 * static synchronized : class level lock. other the class's static synchrnized method is blocked.
	 *
	 * @param sce
	 */
	public static synchronized void onContextDestroyed( ServletContextEvent sce ) {
		if( sce != null ) {
			ExecutorService executor = (ExecutorService)sce.getServletContext().getAttribute("executor");
			if( executor != null ) {
				executor.shutdown();
				sce.getServletContext().log("executor.shutdown(): " + executor.isShutdown());
			}
		}
	}

	/**
	 *
	 * static synchronized : class level lock. other the class's static synchrnized method is blocked.
	 *
	 * @param sce
	 */
	public static synchronized void onContextInitalized( ServletContextEvent sce ) {
		String named = (String)sce.getServletContext()
				.getAttribute(com.irt.custom.ServletContextListener.CTXATTRPROPKEY_UNIQ_INSTANCE_NAME_ON_MACHINE);

		ThreadPoolExecutor executor = null;
		if( named == null || named.length() <= 0 ) {
			executor = new ThreadPoolExecutor(//
					DEFAULT_CORE_POOL_SIZE, // initial pool size
					DEFAULT_MAX_POOL_SIZE, // maximum pool size
					DEFAULT_KEEP_ALIVE_TIME_MILLI, // kill if idle for this time
					TimeUnit.MILLISECONDS, // time unit
					new ArrayBlockingQueue<Runnable>(50));// waiting queue for pool. put task to queue, and gives to pool
		} else {
			ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
					.setNamePrefix(named)
					.build();
			executor = new ThreadPoolExecutor(//
					DEFAULT_CORE_POOL_SIZE, // initial pool size
					DEFAULT_MAX_POOL_SIZE, // maximum pool size
					DEFAULT_KEEP_ALIVE_TIME_MILLI, // kill if idle for this time
					TimeUnit.MILLISECONDS, // time unit
					new ArrayBlockingQueue<Runnable>(50), // waiting queue for pool. put task to queue, and gives to pool
					namedThreadFactory);
		}
		sce.getServletContext().setAttribute("executor", executor);
	}
}
