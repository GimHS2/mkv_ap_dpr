/*
 *	File Name:	DPRTools.java
 *	Version:	2.2.1c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/08/29		2.2.1c	명령어 "cjcheck" 추가
 *	stghr12		2008/03/31		2.2.0c	create
 *
**/

package com.irt.dpr.tools;

import com.irt.rbm.tools.*;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.system.SystemConfig;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.*;

/**
 *
 */
public class DPRTools extends com.irt.rbm.tools.RBMTools {
	private final static DPRTools tools = new DPRTools();

	static SystemConfig systemConfig, central_systemConfig;

	public String getName() {
		return "DPRTools";
	}

	public void destroy() {}

	public void init( Properties properties ) throws Exception {
		super.init( properties );

		systemConfig = configure.getSystemConfig( "RBM" );
		if( systemConfig == null ) throw new IOException( "RBM SystemConfig can't be found." );

		central_systemConfig = configure.getSystemConfig( "APDB" );
		if( central_systemConfig == null ) throw new IOException( "APDB SystemConfig can't be found." );

		Logger logger = Logger.getRootLogger();
	}

	public static void main( String[] args ) throws Exception {
		if( args.length > 0 && "stop".equals(args[0]) )
			tools.stop();
		else
			executeTools( tools, args );
	}

	public void stop() {
		for( ProcessRunner runner : configure.getProcessRunners() )
			runner.interrupt();

		for( ProcessRunner runner : configure.getProcessRunners() ) {
			try {
			runner.join();
			} catch( InterruptedException interruptEx ) {}
		}
	}
}
