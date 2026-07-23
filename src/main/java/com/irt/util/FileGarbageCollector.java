/*
 *	File Name:	FileGarbageCollector.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2013/08/30		2.2.0	create
 *
**/

package com.irt.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Automatically runs.
 * delete com.irt.util.TempFile instance by expiry.
 */
public class FileGarbageCollector implements Runnable {
	private static FileGarbageCollector ref = null;
	private static List files = null;
	Thread t = null;

	public static FileGarbageCollector getInstance() {
		if( ref == null )
			ref = new FileGarbageCollector();
		return ref;
	}

	/**
	 * The files for which an expiry time is set.
	 */
	private FileGarbageCollector() {
		files = new ArrayList();
	}

	public void addFile( TempFile f ) {
		files.add( f );

		if( t == null || !t.isAlive() ) {
			t = new Thread( ref );
			t.start();
		}
	}

	public void run() {
		Logger logger = Logger.getLogger( "com.irt.util.FileGarbageCollector" );

		while( !files.isEmpty() && files.size() > 0 ) {
			Date d = new Date();
			for( int i = 0; i < files.size(); i++ ) {
				if( d.getTime() > ((TempFile)files.get(i)).getExpiry().getTime() ) {
					TempFile t = (TempFile)files.remove(i);
					i--;
					((TempFile)t).delete();
					logger.debug( ((TempFile)t).getPath() + " deleted at " + new Date() );
				}
			}
		}
		try {
			Thread.sleep(8000);
		} catch ( InterruptedException e ) {
			logger.error( e.getMessage() );
		} finally {
			while( !files.isEmpty() && files.size() > 0 ) {
				for( int i = 0; i < files.size(); i++ ) {
					TempFile t = (TempFile)files.remove(i);
					i--;
					((TempFile)t).delete();
					logger.debug( ((TempFile)t).getPath() + " deleted at " + new Date() );
				}
			}
		}
	}
}
