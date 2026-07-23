/*
 *	File Name:	Configure.java
 *	Version:	2.2.4(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/10/31		2.2.4	getSystemInfo() : 오타 수정
 *	GimHS		2025/09/30		2.2.3	getSystemInfo(): BTPi interface(REST) 적용
 *	jbaek		2019/12/30		2.2.2	log4j2 configure support
 *	jbaek		2019/11/30		2.2.2	smtpMap add
 *	jbaek		2014/12/31		2.2.1	systemName property 추가
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class Configure extends com.irt.rbm.tools.Configure {
	public static final String KEY_FILE_TEMP_DIRECTORY = "fileTempDirectory";

	com.irt.rbm.tools.Configure configure;
	public Configure( com.irt.rbm.tools.Configure configure ) {
		super();

		this.configure = configure;
		String log4jproperties = configure.getProperty( "log4j.properties" );
		if( log4jproperties != null ) {
			com.irt.util.Utility2.configureLog4j2Engine( log4jproperties );
		}
	}

	public Map<String, Object> getSystemInfo() {
		Map<String, Object> systemInfo = new java.util.HashMap<String, Object>();

		systemInfo.put( "systemName", configure.getProperty("systemName") );
		systemInfo.put( "userName", configure.getProperty("Rest.username") );
		systemInfo.put( "password", configure.getProperty("Rest.password") );
		systemInfo.put( KEY_FILE_TEMP_DIRECTORY, configure.getProperty("Rest.temp") );

		com.irt.system.SystemConfig systemConfig = configure.getSystemConfig( "RBM" );

		systemInfo.put( "tempDirectory", systemConfig.getTemporaryDirectory() );

		systemInfo.put( "creation_url", configure.getProperty("Rest.service.creation") );
		systemInfo.put( "rdd_url", configure.getProperty("Rest.service.rdd") );
		systemInfo.put( "simulation_url", configure.getProperty("Rest.service.simulation") );
		systemInfo.put( "statuslist_url", configure.getProperty("Rest.service.statuslist") );
		systemInfo.put( "status_url", configure.getProperty("Rest.service.status") );
		systemInfo.put( "billing_url", configure.getProperty("Rest.service.billing") );

		systemInfo.put( "smtp.host", configure.getProperty("OrderProcess.smtp.host") );
		systemInfo.put( "smtp.userid", configure.getProperty("OrderProcess.smtp.userid") );
		systemInfo.put( "smtp.password", configure.getProperty("OrderProcess.smtp.password") );
		systemInfo.put( "smtp.from", configure.getProperty("OrderProcess.smtp.from") );

		return systemInfo;
	}

	public static Map<String, Object> getSmtpMap( String toolsConfig ) throws IOException {
		Properties wmProps = new Properties();

		FileInputStream file = new FileInputStream(toolsConfig);
		wmProps.load(file);

		Map<String, Object> smtpMap = new HashMap<String, Object>();
		smtpMap.put("smtp.host", wmProps.get("OrderProcess.smtp.host"));
		smtpMap.put("smtp.userid", wmProps.get("OrderProcess.smtp.userid"));
		smtpMap.put("smtp.password", wmProps.get("OrderProcess.smtp.password"));
		smtpMap.put("smtp.from", wmProps.get("OrderProcess.smtp.from"));
		smtpMap.put("smtp.smtpDebug", wmProps.get("OrderProcess.smtp.smtpDebug"));

		if( smtpMap.get("smtp.host") != null && smtpMap.get("smtp.from") != null
			&& ( smtpMap.get("smtp.userid") == null || ((String)smtpMap.get("smtp.userid")).length() == 0 ) ) {
			smtpMap.put("mail.smtp.auth", "false");
		}

		return smtpMap;
	}
}
