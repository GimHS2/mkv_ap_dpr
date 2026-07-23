/*
 *	File Name:	INFFileProcess.java
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

import com.irt.data.DataException;
import com.irt.dpr.tools.INFFileException;
import com.irt.sql.SQLHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 */
public class INFFileProcess extends com.irt.rbm.tools.ProcessImpl {
	protected SQLHandler handler;
	protected File file;
	protected Logger logger;
	protected boolean executed = false;

	public INFFileProcess( SQLHandler handler, File file, Logger logger ) throws IOException {
		super( "INFFileProcess."+file.getName(), "'"+ file.getName() + "'" );
		this.handler = handler;
		this.file = file;
		this.logger = logger;
		this.executed = false;
	}

	public boolean continueProcessing() {
		return this.executed;
	}

	public boolean execute() {
		this.executed = false;

		try {
			logger.info( getDescription() +" started." );
			if( readDocument() ) this.executed = true;
			logger.info( getDescription() +" completed." );

			return true;
		} catch( IOException ioEx ) {
			logger.error( getDescription() +" error.", ioEx );
			return false;
		} catch( SQLException sqlEx ) {
			logger.error( getDescription() +" error.", sqlEx );
			return false;
		} finally {
			try { handler.rollback(); } catch( Exception ignored ) {}
		}
	}

	protected INFFileReader getINFFileReader( SQLHandler handler ) throws INFFileException, IOException,SQLException {
		String prefix = file.getName().toUpperCase().split("_")[0];

		if( "FDK43".equals(prefix) ) {
			CreditInfoFileReader edireader = new CreditInfoFileReader( handler, file, logger );
			edireader.messageId = file.getName().toUpperCase().replaceAll("_", "").replaceAll("\\.CSV$", "");
			edireader.documentType = prefix;
			return edireader;
		} else {
			throw new INFFileException( 0, "start", "illegal file name: '"+ file.getName() +"'" );
		}
	}

	protected boolean readDocument() throws IOException, SQLException {
		INFFileReader edireader = null;

		try {
			edireader = getINFFileReader( handler );

			if( edireader == null )
				throw new INFFileException( 0, "", "Not find INFFileReader" );
		} catch( INFFileException fileEx ) {
			logger.log( fileEx.getLevel() == INFFileException.WARNING ? Level.WARN : Level.ERROR, getDescription() +" error.", fileEx );
			return false;
		}

		try {
			return ( edireader.read() >= 0 );
		} catch( INFFileException fileEx ) {
			logger.log( fileEx.getLevel() == INFFileException.WARNING ? Level.WARN : Level.ERROR, getDescription() +" error.", fileEx );
			return false;
		} finally {
			try {
				edireader.updateMessageLog( file.getName() );
			} catch( DataException dataEx ) {
				logger.error( getDescription() +" error.", dataEx );
				return false;
			}
		}
	}
}
