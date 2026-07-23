/*
 *	File Name:	LogConfigContextListener.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.0c	log4j2 add.
 *	jbaek		2018/07/30		2.2.0c	create
 *
**/

package com.irt.custom;

import com.irt.resbdl.ResourceLoader;
import com.irt.util.Arrays;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.lookup.MapLookup;
import org.slf4j.impl.StaticLoggerBinder;

/**
 */
public class LogConfigContextListener implements ServletContextListener {//@formatter:on

	// private final static String DEFAULT_BASIC_CONSOLE_APPENDER_PATTERN = "[%d{HH:mm:ss}][%X{uniqId}] [%p] %m (%F:%M:%L)%n";

	private final static String SLF4JBINDER_LOGBACK_CLASSSTR = "ch.qos.logback.classic.util.ContextSelectorStaticBinder";

	private final static String SLF4JBINDER_LOG4J2_CLASSSTR = "org.apache.logging.slf4j.Log4jLoggerFactory";

	final StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();

	org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LogConfigContextListener.class);

	// private Log4jServletContextListener log4jListener = new Log4jServletContextListener();

	@Override
	public void contextDestroyed( ServletContextEvent event ) {
		// log4jListener.contextDestroyed(event);

		// final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		// final Configuration config = ctx.getConfiguration();
		// config.getRootLogger().removeAppender("CONSOLE");
		// ctx.updateLoggers();

	}

	@Override
	public void contextInitialized( ServletContextEvent event ) {

		// initialize log4j here
		ServletContext webContext = event.getServletContext();
		String contextPath = webContext.getContextPath();

		String appEnvId = webContext.getInitParameter("appEnvId");
		// String webappRootPath = webContext.getInitParameter("webappRootPath");
		ResourceLoader configLoader = (ResourceLoader)webContext.getAttribute("configLoader");
		Properties appProps = (Properties)webContext.getAttribute("appProps");

		event.getServletContext().log("webappRootPath: " + appProps.getProperty("webappRootPath"));

		event.getServletContext().log("SLF4J binder: " + binder.getLoggerFactory());
		event.getServletContext().log("SLF4J binderClassStr: " + binder.getLoggerFactoryClassStr());
		if( SLF4JBINDER_LOG4J2_CLASSSTR.equals(binder.getLoggerFactoryClassStr()) ) {
			boolean isDev = "dv".equals(appEnvId);
			if( isDev ) {
				System.setProperty("log4j2.debug", "true");
			}

			// LoggerContext currLogCtx = (LoggerContext)LogManager.getContext(webContext.getClassLoader(), true);
			// currLogCtx.setExternalContext(webContext.getClassLoader());
			LoggerContext currLogCtx = (LoggerContext)LogManager.getContext(true);

			boolean isNullSource = ConfigurationSource.NULL_SOURCE.equals(currLogCtx.getConfiguration().getConfigurationSource());
			try {
				if( configLoader != null ) {
					URL url = configLoader.getResource(isDev ? "dv.log4j2.xml" : "log4j2.xml");

					// event.getServletContext().setInitParameter("log4jConfiguration",
					// url.getFile());
					// log4jListener.contextInitialized(event);

					event.getServletContext().log("log4j2 config file url: " + url);
					if( url == null ) {
						System.out.println("[ERROR] cannot find log4j2 config file url:"+ url);
					} else {
						LoggerContext newLogCtx = (LoggerContext)LogManager.getContext(webContext.getClassLoader(), false);
						String[] ctxInitMap = new String[] { "webappCtxName", contextPath.replaceFirst("^/", "") };
						// in log4j2.xml ${main:xxx} can be used
						String[] mainMap = null;
						ArrayList<String> list = new ArrayList<String>();
						if( !appProps.isEmpty() ) {
							for( Entry entry : appProps.entrySet() ) {
								list.add((String)entry.getKey());
								list.add((String)entry.getValue());
							}
							mainMap = list.toArray(new String[0]);
						}
						if( mainMap != null ) {
							MapLookup.setMainArguments(Arrays.append(ctxInitMap, mainMap));
						} else {
							MapLookup.setMainArguments(mainMap);
						}
						newLogCtx.setExternalContext(webContext.getClassLoader());

						// force a reconfiguration
						newLogCtx.setConfigLocation(url.toURI());

						webContext.log( "new loggers: " + newLogCtx.getConfiguration().getLoggers() );
						webContext.log( "log4j2 configuration found: started?:" + newLogCtx.isStarted() + " " + url.getFile() );
					}
				}
			} catch( URISyntaxException e ) {
				event.getServletContext().log("configPath is malformed URI: ('" + appProps.getProperty("appRootPath") + "')", e);
			}

			webContext.log("existing loggers: " + currLogCtx.getConfiguration().getLoggers());

			// ConcurrentMap logCfgProperties =
			// currLogCtx.getConfiguration().getComponent(Configuration.CONTEXT_PROPERTIES);
			// if( logCfgProperties != null ) {
			// Object x = logCfgProperties.get("webappRootPath");
			// Object y = logCfgProperties.get("webRootPath");
			// webContext.log("x: " + x + " y: " + y);
			// }

			String logConfigName = currLogCtx.getConfiguration().getName();
			Appender appender = currLogCtx.getConfiguration().getAppenders().get("webapp");
			if( appender != null && appender instanceof RollingFileAppender )
				webContext.log("(configName: " + logConfigName + ") " + "\n webapp logfile: "
						+ ( (RollingFileAppender)appender ).getFileName());
			webContext.log("current log4j2 configuration:(configName:" + logConfigName + ") "
					+ ( (LoggerContext)LogManager.getContext(true) ).getConfiguration().getConfigurationSource());
		}
	}
}
