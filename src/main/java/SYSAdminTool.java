/*
 *	File Name:	SYSAdminTool.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.0c	reloadLogConfig( log4j2 기반 ) 기능 추가.
 *	jbaek		2018/10/30		2.2.0c	reloadColumnResource, reloadMessageResource, reloadJvmSystemEnv 기능 추가
 *	stghr12		2008/03/31		2.2.0	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *										systemConfig.initSystemEnvironment() -> com.irt.rbm.RBMSystem.reloadSystemEnv()
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

import com.irt.data.Record;
import com.irt.dpr.util.Loggers;
import com.irt.json.Jsoner;
import com.irt.rbm.RBMSystem;
import com.irt.resbdl.ResourceLoader;
import com.irt.servlet.ServletUtility2;
import com.irt.util.Log4jReflector;
import com.irt.util.Utility2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

/**
 *
 */
@javax.servlet.annotation.WebServlet( urlPatterns = { "/servlet/SYSAdminTool" } )
public class SYSAdminTool extends AbstractServletModel {//@formatter:off
	public static final String MODE_LOG = "log";

	protected boolean defaultReq( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		String executeType = ctx.req.getParameter( "executeType" );
		if( "reloadSystemEnv".equals(executeType) ) {
			com.irt.rbm.RBMSystem.reloadSystemEnv( ctx.handler );
			logger.info( "reloadSystemEnv executed. (all cache evicted and reloaded)" );
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_APPLY_SUCCESS") );
		} else if( "getSystemEnv".equals(executeType) ) {
			String envKey = ctx.req.getParameter( "envKey" );
			if( envKey != null ) {
				String[] envKeys = envKey.split( "\\." );
				if( envKeys != null && envKeys.length == 3 ) {
					String envVal = RBMSystem.getSystemEnv( envKeys[0], envKeys[1]+";"+envKeys[2] );

					ctx.req.setAttribute( "env", Record.createMap(envKey, envVal) );
					if( envVal != null ) {
						ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_APPLY_SUCCESS") );
					}
				}
			}

			return jsonResponse(ctx, "env");
		} else if( "reloadLogConfig".equals(executeType) ) {
			ResourceLoader configLoader = (ResourceLoader)ctx.req.getServletContext().getAttribute( "configLoader" );
			boolean isLog4j2 = false;

			URL log4j2URL = configLoader.getResource( "log4j2.xml" );
			if( configLoader != null ) {
				log4j2URL = configLoader.getResource( "log4j2.xml" );
				if( log4j2URL != null ) {
					isLog4j2 = true;
				}
			}
			if( isLog4j2 ) {
//				LoggerContext newLogCtx = (org.apache.logging.log4j.core.LoggerContext)LogManager.getContext( false);
				LoggerContext newLogCtx = (org.apache.logging.log4j.core.LoggerContext)LogManager.getContext( ctx.req.getServletContext().getClassLoader(), false );
				newLogCtx.setExternalContext( ctx.req.getServletContext().getClassLoader() );
				// this will force a reconfiguration
				try {
					newLogCtx.setConfigLocation( log4j2URL.toURI() );
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_APPLY_SUCCESS") );
				} catch( URISyntaxException e ) {
					ctx.req.getServletContext().log( "url syntax exception", e );
				}
				ctx.req.getServletContext().log( "log4j2 configuration executed. started?:" + newLogCtx.isStarted() + " " + log4j2URL.getFile() );
			} else {
				String configFilename = ServletUtility2.getLogConfigFilePath( ctx.req );
				if( configFilename != null && configFilename.length() > 0 ) {
					new Log4jReflector().setupPropertyConfigurator( configFilename );
					ctx.req.getServletContext().log( "log4j1 configuration executed. "+ configFilename );
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_APPLY_SUCCESS") );
				}
			}
		} else if( "reloadColumnResource".equals(executeType) ) {
			com.irt.html.ColumnConfigureFile.Reloadable.clear();
			logger.info( "reloadColumnResource executed globally(all cache evicted)" );
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_APPLY_SUCCESS") );
		} else if( "reloadMessageResource".equals(executeType) ) {
			ResourceBundle.clearCache();
			logger.info( "reloadMessageResource executed globally(all cache evicted)" );
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_APPLY_SUCCESS") );
		} else if( "reloadJvmSystemEnv".equals(executeType) ) {
			com.irt.rbm.RBMSystem.reloadSystemEnv( ctx.handler );
			logger.info( "reloadJvmSystemEnv(reloadSystemEnv) executed. (all cache evicted and reloaded)" );
			String reloadableKeys = RBMSystem.getSystemEnv( "SYS", "JvmSystemEnv;reloadableKeys" );
			if( reloadableKeys != null ) {
				String[] jvmkeys = reloadableKeys.split( "," );
				if( jvmkeys != null ) {
					for( String jvmkey : jvmkeys ) {
						if( jvmkey != null ) {
							String[] envkey = jvmkey.split( "\\." );
							if( envkey != null && envkey.length == 3 ) {
								String toInject = RBMSystem.getSystemEnv( envkey[0], envkey[1]+";"+ envkey[2] );
								if( toInject != null ) {
									String beforeValue = Utility2.setJvmSystemEnv( envkey[0], envkey[1]+";"+ envkey[2], toInject );
									if( System.getProperty(jvmkey)!= null && toInject.equals(System.getProperty(jvmkey)) ) {
										logger.info( "JvmSystemEnv applied. key: "+ jvmkey +" value: "+ toInject + " beforevalue: "+ beforeValue );
									}
								}
							}
						}
					}
				}
			}

			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_APPLY_SUCCESS") );
		}

		return true;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( isPost ) {
			if( MODE_LOG.equals(ctx.mode) )
				return log( ctx );
		}

		return super.doRequest( ctx, isPost );
	}

	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "SYS" );
	}

	protected void initContext( Context ctx ) throws ServletException {
		super.createPageConfig( ctx );
		ctx.pageConfig.setSystemPackageCode( "SYS", "SYSAdminTool" );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_SYS_ADMINTOOL") );
	}

	private boolean log( Context ctx ) {
		java.util.Map<String, String[]> pmap = ctx.req.getParameterMap();
		java.util.Set<String> keys = null;
		if( true ) {
			keys = pmap.keySet();
		}

		Loggers.client_sync.debug(Jsoner.getNewInstance().toJson(pmap));

		return true;
	}
}
