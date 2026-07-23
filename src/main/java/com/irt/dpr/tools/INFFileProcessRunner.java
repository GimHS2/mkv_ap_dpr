/*
 *	File Name:	INFFileProcessRunner.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.rbm.tools.Process;
import com.irt.rbm.tools.ProcessException;
import com.irt.sql.SQLHandler;
import com.irt.system.SystemConfig;
import java.io.File;
import org.apache.log4j.Logger;

/**
 *
 */
public class INFFileProcessRunner extends com.irt.rbm.tools.FileProcessRunner {
	public INFFileProcessRunner( SystemConfig systemConfig, File directory ) {
		super( systemConfig, directory );
	}

	public String getDescription() {
		return "DPR-INFFileProcessRunner";
	}

	public String getEDIProgramName() {
		return "DPRINF";
	}

	public Logger getLogger() {
		return Logger.getLogger( "com.irt.dpr.tools.INFFileProcessRunner" );
	}

	public Process getProcessInstance( SQLHandler handler, File file ) throws ProcessException {
		try {
			return new INFFileProcess( handler, file, Logger.getLogger("com.irt.dpr.tools.INFFileProcess") );
		 } catch( java.io.IOException ioEx ) {
			throw new ProcessException( ioEx );
		}
	}

	public String getProcessName() {
		return "DPR-INFFileProcessRunner";
	}
}
