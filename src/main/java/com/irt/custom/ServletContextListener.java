/*
 *	File Name:	ServletContextListener.java
 *	Version:	2.2.2c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/09/28		2.2.2c	contextInitialized(): hashAlgorithm, S3 Storage 관련 변수 초기화 추가
 *	jbaek		2020/06/30		2.2.2c	configLoader 사용.( external '${webRootPath}/conf' or 'WEB-INf/conf'. url을 file:/// 사용토록 변경.)
 *	jbaek		2018/10/30		2.2.2c	LocationUtil 사용. mapdb disable.
 *	jbaek		2017/09/30		2.2.2c	SQLManager.getDBTimeZone()  rset.getTimestamp(int, Calendar)
 *	jbaek		2017/06/30		2.2.2	servlet 3.0 문법에 맞게 변경.
 *	jbaek		2017/02/28		2.2.1	resolve relative configureFile path( eg. WEB-INF/rbmweb.conf )
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.custom;

import com.irt.rbm.RBMSystem;
import com.irt.resbdl.ResourceLoader;
import com.irt.servlet.ContextServiceInitiator;
import com.irt.sql.SQLHandler;
import com.irt.util.FileUtil;
import com.irt.util.LocationUtil;
import com.irt.util.S3Service;
import com.irt.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 *
 */
@WebListener( "ServletContextListener" )
public class ServletContextListener implements javax.servlet.ServletContextListener {//@formatter:on

	/**
	 * access {@link System#getProperty(String)} name of {@link ServletContext#getContextPath()} getContextPath() has starts with "/" but this value
	 * does not starts with "/".
	 */
	private final static String SYSJVMPROPKEY_CONTEXT_PATH_NAME = "contextPathName";

	/**
	 * access {@link System#getProperty(String)}
	 *
	 * <pre>
	 * if /home/base/rbm2/webapps/rbm2.war
	 *	then /home/base/rbm2/logs is webappLogPath and the dir should exist before run.
	 *
	 * if /home/base/tomcat/webapps/rbm2.war
	 *	then /home/base/tomcat/logs
	 * </pre>
	 */
	public final static String SYSJVMPROPKEY_WEBAPP_LOG_PATH = "webappLogPath";

	/**
	 * access {@link SystemConfig#getProperty(String)} logging properties absolute file path
	 */
	public final static String SYSCFGPROPKEY_LOG_CONFIG_FILE_PATH = "logConfigFilePath";

	/**
	 * if /home/base/rbm2/webapps/rbm2.war then /home/base/rbm2 is webappSvcroot
	 *
	 */
	private final static String EVALPROPKEY_WEBAPP_SVCROOT = "webappSvcroot";
	private final static String EVALPROPKEY_WEBAPP_CONTEXT_REAL_PATH = "webappContextRealPath";

	public final static String CTXATTRPROPKEY_WEBAPP_SVCROOT_NAME = "webappSvcrootName";
	public final static String CTXATTRPROPKEY_UNIQ_INSTANCE_NAME_ON_MACHINE = "uniqInstanceNameOnMachine";
	public final static String CTXATTRPROPKEY_UNIQ_INSTANCE_NAME_ON_CLUSTER = "uniqInstanceNameOnCluster";

	/**
	 * To be used by log4j,,,
	 *
	 *
	 * URL resource = this.getClass().getClassLoader().getResource("");
	 *
	 * if /home/base/rbm2/webapps/rbm2##12345.war ( is war file, tomcat ) then rbm2##12345
	 *
	 * if /home/base/rbm2/webapps/rbm2.war ( is war file, JBoss case ) then rbm2.war-a234fefcxw
	 *
	 */
	private final static String SYSPROPKEY_WEBAPP_REAL_PATH_NAME = "webappRealPathName";

	private LogConfigContextListener logcfgListener = new LogConfigContextListener();

	@Override
	public void contextDestroyed( javax.servlet.ServletContextEvent sce ) {
		if( sce != null ) {
			try {
				Class<?> clazz = Class.forName("com.irt.custom.ServletContextInitiator", false, sce.getServletContext().getClassLoader());
				java.lang.reflect.Method m = null;
				try {
					m = clazz.getDeclaredMethod("onContextDestroyed", javax.servlet.ServletContextEvent.class);
				} catch( NoSuchMethodException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch( SecurityException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					m.invoke(null, sce);
				} catch( IllegalAccessException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch( IllegalArgumentException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch( InvocationTargetException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch( ClassNotFoundException ignored ) {
			}
		}
		logcfgListener.contextDestroyed(sce);
	}

	@Override
	public void contextInitialized( javax.servlet.ServletContextEvent sce ) {
		ServletContext context = sce.getServletContext();

		java.util.Properties rbmwebProps = new java.util.Properties();
		String appEnvId = sce.getServletContext().getInitParameter("appEnvId");
		if( appEnvId == null )
			throw new IllegalArgumentException("appEnvId is mandatory.");
		else if( !(appEnvId.startsWith("dv") || appEnvId.startsWith("qa") || appEnvId.startsWith("pd")) )
			throw new IllegalArgumentException("appEnvId string must start with [dv|qa|pd].");
		Properties appProps = getAppProps(sce, appEnvId);
		String webappRootPath = appProps.getProperty("webappRootPath");
		if( webappRootPath == null )
			sce.getServletContext().log( "[WARN] webappRootPath is not supplied. will not use external conf dir." );
		ResourceLoader configLoader = getWebConfigResourceLoader(sce, appProps.getProperty("webappRootPath"));
		context.setAttribute("configLoader", configLoader);
		context.setAttribute("appProps", appProps);

		URL rbmwebUrl = configLoader.getResource("rbmweb.conf");
		if( rbmwebUrl == null ) {
			throw new IllegalArgumentException("configureFile cannot be null. configPaths tried: " + java.util.Arrays.asList(configLoader.getURLs()));
		}
		InputStream rbmwebStrm = null;
		try {
			rbmwebStrm = rbmwebUrl.openStream();
			rbmwebProps.load(rbmwebStrm);
		} catch( IOException e ) {
			sce.getServletContext().log("initialized error", e);
		} finally {
			try {
				rbmwebStrm.close();
			} catch( IOException ignored ) {
				rbmwebStrm = null;
			}
		}

		rbmwebProps.put("appEnvId", appEnvId);
		for( java.util.Map.Entry<Object, Object> entry : appProps.entrySet() ) {
			String key = (String)entry.getKey();
			String value = null;
			if( "debugJS".equals(key) ) {
				rbmwebProps.put(key, ( appProps.getProperty(key) == null ? "N" : appProps.getProperty(key) ));
			} else if( "traceJS".equals(key) ) {
				rbmwebProps.put(key, ( appProps.getProperty(key) == null ? "N" : appProps.getProperty(key) ));
			} else if( "useClientLogging".equals(key) ) {
				rbmwebProps.put(key, ( appProps.getProperty(key) == null ? "N" : appProps.getProperty(key) ));
			}
		}

		// should run after configloader is set
		logcfgListener.contextInitialized(sce);

		if( false ) {
			String configureFileName = sce.getServletContext().getInitParameter("configureFile");
			try {
				java.io.FileInputStream inputStream = null;
				try {
					if( configureFileName == null && sce.getServletContext().getMajorVersion() >= 3 ) {
						configureFileName = context.getRealPath("WEB-INF/rbmweb.conf");
					} else if( !LocationUtil.isAbsolutePathForm(configureFileName) ) {
						configureFileName = context.getRealPath(configureFileName);
					}

					if( configureFileName == null ) {
						throw new IllegalArgumentException("configureFile cannot be null.");
					}

					try {
						rbmwebProps.load(inputStream = new java.io.FileInputStream(configureFileName));
					} catch( java.io.FileNotFoundException fne ) {
						rbmwebProps.load(inputStream = new java.io.FileInputStream(context.getRealPath("WEB-INF/rbmweb.conf")));
					}
				} finally {
					try {
						if( inputStream != null )
							inputStream.close();
					} catch( Exception ignored ) {
					}
				}
			} catch( Exception ex ) {
				sce.getServletContext().log("initialized error", ex);
			}
		}

		// s3 setting
		boolean s3Storage = "Y".equals( rbmwebProps.getProperty("s3Storage", "N") );
		if( s3Storage ) {
			String s3AccessKey = rbmwebProps.getProperty( "s3AccessKey" );
			String s3SecretKey = rbmwebProps.getProperty( "s3SecretKey" );
			String s3Region = rbmwebProps.getProperty( "s3Region" );
			String s3Bucket = rbmwebProps.getProperty( "s3Bucket" );
			String s3Path = rbmwebProps.getProperty( "s3Path" );

			S3Service.s3Instance = new S3Service( s3AccessKey, s3SecretKey, s3Region, s3Bucket );
			if( s3Path != null ) {
				S3Service.s3Instance.setDefaultPath( s3Path );
			}
		}

		Map<String, Object> contextEvalSrcMap = getEvalSourceMap(context);
		rbmwebProps = StringUtil.evalPlaceholders(rbmwebProps, contextEvalSrcMap);
		setSystemProperty(context, rbmwebProps, contextEvalSrcMap);
		setContextAttribute(context, rbmwebProps, contextEvalSrcMap);
		boolean traceContextInfo = "Y".equals(rbmwebProps.getProperty("traceContextInfo", "N"));
		if( traceContextInfo ) {
			traceInfoToContextLogger(context, contextEvalSrcMap);
		}

		if( rbmwebProps.getProperty("sessionKey") == null )
			rbmwebProps.put("sessionKey", SystemConfig.DEFAULT_SESSION_KEY);
		if( rbmwebProps.getProperty("sessionTime") == null )
			rbmwebProps.put("sessionTime", String.valueOf(SystemConfig.DEFAULT_SESSION_TIME));
		if( rbmwebProps.getProperty(SYSCFGPROPKEY_LOG_CONFIG_FILE_PATH) == null )
			rbmwebProps.put(SYSCFGPROPKEY_LOG_CONFIG_FILE_PATH, //
					String.valueOf(context.getRealPath("WEB-INF/classes/log4j.properties")));

		try {
			SystemConfig.initialize(rbmwebProps);
		} catch( Exception ex ) {
			sce.getServletContext().log("initialized error", ex);
		}

		SQLHandler handler = null;
		SystemConfig systemConfig = SystemConfig.getInstance("RBM");
		try {
			com.irt.rbm.RBMSystem.initSystemEnv(handler = systemConfig.createSQLHandler(systemConfig.getMessageHandler()));
		} catch( Exception ex ) {
			sce.getServletContext().log("initialized error", ex);
		} finally {
			try {
				handler.close();
			} catch( Exception ignored ) {
			}
		}

		// set DBTimeZone for SQLManager.getDBTimeZone()
		String dbTimeZone = RBMSystem.getSystemEnv("SYS", "TimeZone;DBTimeZone");
		if( dbTimeZone != null && dbTimeZone.length() > 0 ) {
			System.setProperty("DBTimeZone", dbTimeZone);
		}

		AppEnv.instance().setAppId(sce.getServletContext().getContextPath().replaceFirst("^/", ""));
		SystemEx.injectRBMSystemAppSystemEnv(AppEnv.instance().getAppId());
		String useAnsiJoin = AppEnv.instance().getAppSystemEnv("SYS", "QueryBuffer;useAnsiJoin");

		String useMessageResource = RBMSystem.getSystemEnv("SYS", "DatabaseResource;useMessageResource");
		if( useMessageResource != null && useMessageResource.length() > 0 ) {
			com.irt.util.Utility2.setJvmSystemEnv("SYS", "DatabaseResource;useMessageResource", useMessageResource);
		}

		if( RBMSystem.getSystemEnvBool("DPR", "Feature;useMapDb", false) ) {
			ContextServiceInitiator.onContextInitalized(sce);
		}

		com.irt.rbm.usr.UserUser.hashAlgorithm = RBMSystem.getSystemEnv("USR", "SecurityPolicy;PasswordHashAlgorithm"
				, com.irt.rbm.usr.UserUser.HASH_ALGORITHM_SHA2 );

		String env = com.irt.rbm.usr.UserUser.hashAlgorithm;
		try {
			if( !com.irt.rbm.usr.UserUser.HASH_ALGORITHM_MD5.equals(env) && !com.irt.rbm.usr.UserUser.HASH_ALGORITHM_SHA2.equals(env) ) {
				com.irt.rbm.usr.UserUser.hashAlgorithm = com.irt.rbm.usr.UserUser.HASH_ALGORITHM_SHA2;
				throw new IllegalArgumentException( "The password hash algorithm is incorrect. Set it to SHA-256." );
			}
		} catch( IllegalArgumentException ex ) {
			sce.getServletContext().log( "initialized error", ex );
		}
	}

	/**
	 * Removes ending "/"
	 *
	 * @param context
	 * @return
	 */
	private String getContextRealPathString( ServletContext context ) {
		String realPath = context.getRealPath("/");
		if( realPath != null ) {
			if( realPath.endsWith("/") ) {
				realPath = realPath.substring(0, realPath.length() - 1);
			}
		}
		return realPath;
	}

	private java.util.Map<String, Object> getEvalSourceMap( ServletContext context ) {
		return getEvalSourceMap(context, false, true);
	}

	/**
	 *
	 * custom map is higer priority. then os property then java property
	 *
	 */
	private java.util.Map<String, Object> getEvalSourceMap( ServletContext context, boolean inclOsEnv, boolean inclJavaProp ) {
		String webappPath = getContextRealPathString(context);
		java.io.File webappParentPathFile = new java.io.File(webappPath).getParentFile();
		Map<String, Object> map = new HashMap<String, Object>();

		if( inclOsEnv ) {
			map.putAll(System.getenv());
		}
		if( inclJavaProp ) {
			map.putAll((Map)System.getProperties());
		}

		map.put(EVALPROPKEY_WEBAPP_SVCROOT, webappParentPathFile.getParentFile().getAbsolutePath());
		map.put(EVALPROPKEY_WEBAPP_CONTEXT_REAL_PATH, StringUtil.getPathWithFwdSlash(webappPath));
		map.put(CTXATTRPROPKEY_WEBAPP_SVCROOT_NAME, webappParentPathFile.getParentFile().getName());
		String uniqInstanceNameOnMachine = getUniqInstanceNameOnMachine((String)map.get(CTXATTRPROPKEY_WEBAPP_SVCROOT_NAME),
				context.getContextPath(), context.getInitParameter("deploy-version"));
		map.put(CTXATTRPROPKEY_UNIQ_INSTANCE_NAME_ON_MACHINE, uniqInstanceNameOnMachine);

		return map;
	}

	/**
	 * on single machine multiple services
	 *
	 * @param webappSvcrootName
	 * @param contextPathName
	 * @return
	 */
	private String getUniqInstanceNameOnMachine( String webappSvcrootName, String contextPathName, String deployVersion ) {
		StringBuilder uniqInstanceName = new StringBuilder();

		if( webappSvcrootName.startsWith("/") ) {
			uniqInstanceName.append(webappSvcrootName);
		} else {
			uniqInstanceName.append("/" + webappSvcrootName);
		}
		if( contextPathName.startsWith("/") ) {
			uniqInstanceName.append(contextPathName);
		} else {
			uniqInstanceName.append("/" + contextPathName);
		}
		uniqInstanceName.append("##" + deployVersion);

		return uniqInstanceName.toString();
	}

	public Properties getAppProps( ServletContextEvent event, String appEnvId ) {
		event.getServletContext().log("appEnvId: " + appEnvId);
		ServletContext webContext = event.getServletContext();

		Properties appProps = new Properties();
		if( appEnvId != null && appEnvId.length() > 0 ) {
			try {
				URL appPropsUrl = webContext.getResource("/WEB-INF/conf/" + "app." + appEnvId + ".properties");

				if( appPropsUrl != null ) {
					InputStream is = null;
					try {
						is = appPropsUrl.openStream();
						appProps.load(is);
						appProps.setProperty("appEnvId", appEnvId);
						appProps.setProperty("appEnvProfile", appEnvId.substring(0, 2));
						if( appProps.getProperty("webappRootPath") != null ) {
							appProps.setProperty("webappRootPath", FileUtil.backslashToslash(appProps.getProperty("webappRootPath")));
						}
					} catch( IOException ioEx ) {
						webContext.log("err: " + "(" + appPropsUrl + ")", ioEx);
					} finally {
						try {
							is.close();
						} catch( IOException ignored ) {
						}
					}
				}
			} catch( MalformedURLException urlEx ) {
				webContext.log("err: ", urlEx);
			}
		}
		return appProps;
	}

	/**
	 * search resource from external '${webappRootPath}/conf' or internal 'WEB-INF/conf' or 'WEB-INF'
	 */
	private ResourceLoader getWebConfigResourceLoader( ServletContextEvent event, String webappRootPath ) {
		File externConfDir = (webappRootPath == null ? null : new File(new File(webappRootPath), "conf"));

		URL webInfUrl = null;
		URL webInfConfUrl = null;
		try {
			webInfUrl = event.getServletContext().getClassLoader().getResource("/../");
			boolean isWarExplodedAsExternalSystemPath = ( webInfUrl == null ? true : false );
			if( isWarExplodedAsExternalSystemPath ) {
				webInfUrl = new URL("file:///"+ event.getServletContext().getRealPath("/WEB-INF")+"/");
			}
			event.getServletContext().log("webInfUrl: " + webInfUrl);

			if( webInfUrl != null )
				webInfConfUrl = LocationUtil.getChildURL(webInfUrl, "conf");
		} catch( MalformedURLException urlEx ) {
			event.getServletContext().log("url error.", urlEx);
		}

		URL[] configPaths = null;
		try {
			if( externConfDir != null && externConfDir.exists() ) {
				configPaths = new URL[] { externConfDir.toURI().toURL(), webInfConfUrl, webInfUrl };
			} else {
				configPaths = new URL[] { webInfConfUrl, webInfUrl };
			}
		} catch( MalformedURLException e ) {
			event.getServletContext().log("configpath malformedurl('" + webappRootPath + "')", e);
		} finally {
			event.getServletContext().log("configPaths: " + java.util.Arrays.asList(configPaths));
		}

		return new ResourceLoader(configPaths, event.getServletContext().getClassLoader());
	}

	private void setContextAttribute( ServletContext context, java.util.Properties mainProp, Map contextEvalSrcMap ) {
		setContextAttribute(context, CTXATTRPROPKEY_WEBAPP_SVCROOT_NAME, contextEvalSrcMap.get(CTXATTRPROPKEY_WEBAPP_SVCROOT_NAME));
		setContextAttribute(context, CTXATTRPROPKEY_UNIQ_INSTANCE_NAME_ON_MACHINE,
				contextEvalSrcMap.get(CTXATTRPROPKEY_UNIQ_INSTANCE_NAME_ON_MACHINE));
	}

	private void setContextAttribute( ServletContext context, String attrName, Object attrValue ) {
		context.setAttribute(attrName, attrValue);
	}

	/**
	 * JVM System wide Property setting using {@link System#setProperty(String, String)} To be used by eg. log4j
	 *
	 * @param context
	 * @param mainProp
	 *            : if has key then use this over context value.( mainProp is higher priority than {@link ServletContext} )
	 */
	private void setSystemProperty( ServletContext context, java.util.Properties mainProp, Map evalSrcMap ) {
		setSystemProperty(SYSJVMPROPKEY_CONTEXT_PATH_NAME, getContextRealPathString(context));

		String webappPathStr = getContextRealPathString(context);
		File webappRealPath = new File(webappPathStr);

		// if rbmweb.conf has webappLogPath at configured time.
		String webappLogPath = mainProp.getProperty(SYSJVMPROPKEY_WEBAPP_LOG_PATH);
		if( webappLogPath == null || webappLogPath.length() <= 0 ) {
			java.io.File webappLogPathFile = new java.io.File(webappRealPath.getParentFile().getParentFile(), //
					File.separator + "logs");
			webappLogPath = webappLogPathFile.getAbsolutePath();
		}

		setSystemProperty(SYSJVMPROPKEY_WEBAPP_LOG_PATH, StringUtil.getPathWithFwdSlash(webappLogPath));
		setSystemProperty(SYSPROPKEY_WEBAPP_REAL_PATH_NAME, webappRealPath.getName());
	}

	private void setSystemProperty( String keyName, String value ) {
		boolean isWin = System.getProperty("os.name").toLowerCase().startsWith("win");
		if( "contextPathName".equals(keyName) ) {
			if( value != null ) {
				if( value.startsWith("/") ) {
					value = value.substring(1);
				}
				if( value.endsWith("/") ) {
					value = value.substring(0, value.length() - 1);
				}
			}
		}
		if( value != null && value.length() > 0 ) {
			if( isWin ) {
				System.setProperty(keyName, value.replace("\\", "/"));
			} else {
				System.setProperty(keyName, value);
			}
		}
	}

	private void traceInfoToContextLogger( ServletContext context, Map<String, Object> mainProp ) {
		StringBuilder sb = new StringBuilder();
		sb.append("{traceInfo: {");
		sb.append("\n\t");
		sb.append(SYSJVMPROPKEY_WEBAPP_LOG_PATH + " : " + System.getProperty(SYSJVMPROPKEY_WEBAPP_LOG_PATH));
		sb.append("\n\t");
		sb.append(SYSPROPKEY_WEBAPP_REAL_PATH_NAME + " : " + System.getProperty(SYSPROPKEY_WEBAPP_REAL_PATH_NAME));
		sb.append("\n\t");
		sb.append(EVALPROPKEY_WEBAPP_SVCROOT + " : " + mainProp.get(EVALPROPKEY_WEBAPP_SVCROOT));
		sb.append("\n\t");
		sb.append(EVALPROPKEY_WEBAPP_CONTEXT_REAL_PATH + " : " + mainProp.get(EVALPROPKEY_WEBAPP_CONTEXT_REAL_PATH));
		sb.append("\n\t");
		sb.append(CTXATTRPROPKEY_WEBAPP_SVCROOT_NAME + " : " + context.getAttribute(CTXATTRPROPKEY_WEBAPP_SVCROOT_NAME));
		sb.append("\n\t");
		sb.append(CTXATTRPROPKEY_UNIQ_INSTANCE_NAME_ON_MACHINE + " : " + context.getAttribute(CTXATTRPROPKEY_UNIQ_INSTANCE_NAME_ON_MACHINE));
		sb.append("\n\t");
		sb.append("deploy-version" + " : " + context.getInitParameter("deploy-version"));

		sb.append("\n");
		sb.append("}}");
		context.log(sb.toString());
	}
}
