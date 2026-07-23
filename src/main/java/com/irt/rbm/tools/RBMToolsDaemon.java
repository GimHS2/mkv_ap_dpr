/*
 *	File Name:	RBMToolsDaemon.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/06/30		2.2.1	중복시작 안되도록 수정, stop 오류 수정
 *	stghr12		2011/02/28		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import java.io.File;
import java.io.FileWriter;
import org.apache.commons.daemon.DaemonContext;

/**
 *
 */
public class RBMToolsDaemon implements org.apache.commons.daemon.Daemon {
	RBMTools tools = null;
	private static boolean runningDaemon = true;

	public void destroy() {
		tools.destroy();
	}

	public void init( DaemonContext context ) throws Exception {
		String[] args = context.getArguments();
		String configureFileName = null;
		for( int idx = 0; idx < args.length; idx++ ) {
			if( "-class".equals(args[idx]) ) {
				try {
					tools = (RBMTools)RBMToolsDaemon.class.getClassLoader().loadClass(args[++idx]).newInstance();
				} catch( ArrayIndexOutOfBoundsException arrEx ) {
					throw new IllegalArgumentException( "configure class name cannot be null." );
				}
			} else if( "-conf".equals(args[idx]) ) {
				try {
					configureFileName = args[++idx];
				} catch( ArrayIndexOutOfBoundsException arrEx ) {
					throw new IllegalArgumentException( "configure file name cannot be null." );
				}
			} else
				throw new IllegalArgumentException( "illegal argument '"+ args[idx] +"'." );
		}
		if( tools == null ) tools = new RBMTools();

		tools.init( configureFileName );
	}

	public static void main( String[] args ) throws Exception {
		RBMTools tools = null;

		File runningCheckFile = null;
		String configureFileName = null;
		try {
			if( args.length == 0 || !("start".equals(args[0]) || "stop".equals(args[0])) )
				throw new IllegalArgumentException( "illegal command: "+ (args.length > 0 ? args[0] : "") +"." );

			for( int idx = 1; idx < args.length; idx++ ) {
				if( "-class".equals(args[idx]) ) {
					try {
						tools = (RBMTools)RBMToolsDaemon.class.getClassLoader().loadClass(args[++idx]).newInstance();
					} catch( ArrayIndexOutOfBoundsException arrEx ) {
						throw new IllegalArgumentException( "configure class name cannot be null." );
					}
				} else if( "-chkfile".equals(args[idx]) ) {
					try {
						runningCheckFile = new java.io.File( args[++idx] );
					} catch( ArrayIndexOutOfBoundsException arrEx ) {
						throw new IllegalArgumentException( "running check file name cannot be null." );
					}
				} else if( "-conf".equals(args[idx]) ) {
					try {
						configureFileName = args[++idx];
					} catch( ArrayIndexOutOfBoundsException arrEx ) {
						throw new IllegalArgumentException( "configure file name cannot be null." );
					}
				} else
					throw new IllegalArgumentException( "illegal argument '"+ args[idx] +"'." );
			}
		} catch( IllegalArgumentException argEx ) {
			System.out.println( "Usage: RBMToolsDaemon start|stop -class <class> | -conf <file> | -chkfile <file>" );
			System.out.println();

			throw argEx;
		}
		if( tools == null ) tools = new RBMTools();

		if( "start".equals(args[0]) ) {
			configureFileName = tools.init( configureFileName );

			if( runningCheckFile != null ) {
				if( runningCheckFile.exists() ) {
					System.out.println( tools.getName() +" cannot start - "+ runningCheckFile.getName() +" file already exists." );
					return;
				}

				FileWriter writer = new FileWriter( runningCheckFile );
				try {
					writer.write( tools.getClass().getPackage().getName() +"."+ tools.getClass().getName() );
					writer.flush();
				} finally {
					writer.close();
				}
			}

			try {
				tools.start();
				System.out.println( tools.getName() +" started." );

				try {
					if( runningCheckFile != null ) {
						while( runningCheckFile.exists() && runningCheckFile.length() > 0 )
							Thread.sleep( 500 );
					} else {
						while( RBMToolsDaemon.runningDaemon )
							Thread.sleep( 500 );
					}
				} catch( InterruptedException interruptEx ) {}

				tools.stop();
				System.out.println( tools.getName() +" stopped." );
			} finally {
				if( runningCheckFile != null && runningCheckFile.exists() ) runningCheckFile.delete();
				tools.destroy();
			}
		} else if( "stop".equals(args[0]) ) {
			if( runningCheckFile != null ) {
				if( !runningCheckFile.exists() ) {
					System.out.println( "cannot find file '"+ runningCheckFile.getAbsolutePath() +"'." );
					return;
				}

				FileWriter writer = new FileWriter( runningCheckFile );
				try {
					writer.flush();
				} finally {
					try { writer.close(); } catch( Exception ignored ) {}
				}

				try {
					while( runningCheckFile.exists() )
						Thread.sleep( 100 );
				} catch( InterruptedException interruptEx ) {}

				System.out.println( tools.getName() +" stopped." );
			} else
				RBMToolsDaemon.runningDaemon = false;
		}
	}

	public void start() {
		tools.start();
	}

	public void stop() {
		tools.stop();
	}
}
