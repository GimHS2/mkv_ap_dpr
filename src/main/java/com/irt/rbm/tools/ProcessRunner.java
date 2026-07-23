/*
 *	File Name:	ProcessRunner.java
 *	Version:	2.2.6c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek			2019/12/30		2.2.6c	MDC 처리
 *	stghr12		2011/06/30		2.2.6	InterruptedException 처리
 *										close() 오류수정: thread.interrupt() 처리
 *										usingDaemon, waitThread() 추가
 *	stghr12		2011/02/28		2.2.5	logger 정리 및 ProcessException 제거
 *	stghr12		2010/08/31		2.2.4	execute(args) 추가, isThreadFull() 추가, getSleepMiliis() 삭제
 *	stghr12		2009/12/31		2.2.3	DEFAULT_MAXIMUM_CONSISTENT_ERROR값을 10 -> 20 변경
 *	stghr12		2009/02/28		2.2.2	getSleepMillis(), setSleepMillis() 추가(Spell 오류)
 *	stghr12		2008/05/31		2.2.1	consistentErrorCount, maxConsistentErrorCount 추가: 연속적으로 에러가 발생하면 ProcessRunner 중단
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 */
public abstract class ProcessRunner extends Thread implements Process {
	public final static Process	EMPTY									= new ProcessImpl( "", "" );
	public final static long DEFAULT_SLEEP_MILLIS						= 15000;
	public final static long DEFAULT_THREAD_WAIT_MILLIS					= 50;
	public final static long DEFAULT_SQLHANDLER_WAIT_MILLIS				= 50;
	public final static int DEFAULT_SQLHANDLER_CREATING_TRIALCOUNT		= 20;
	public final static int DEFAULT_MAXIMUM_CONSISTENT_ERROR			= 10;

	int maxThreadCount, maxConsistentErrorCount;
	int consistentErrorCount;
	int consistentThreadErrorCount;
	boolean usingThread, usingDaemon;
	long sleepMillis;
	Map<String, Thread> processMap;

	public ProcessRunner() {
		this.maxThreadCount = 0;
		this.maxConsistentErrorCount = DEFAULT_MAXIMUM_CONSISTENT_ERROR;
		this.consistentErrorCount = 0;
		this.consistentThreadErrorCount = 0;
		this.usingThread = false;
		this.usingDaemon = true;
		this.sleepMillis = DEFAULT_SLEEP_MILLIS;
		this.processMap = java.util.Collections.synchronizedMap( new java.util.HashMap<String, Thread>(100) );
	}

	protected ProcessRunner( ProcessRunner runner ) {
		this.maxThreadCount = runner.maxThreadCount;
		this.maxConsistentErrorCount = runner.maxConsistentErrorCount;
		this.consistentErrorCount = 0;
		this.consistentThreadErrorCount = 0;
		this.usingThread = runner.usingThread;
		this.usingDaemon = runner.usingDaemon;
		this.sleepMillis = runner.sleepMillis;
		this.processMap = java.util.Collections.synchronizedMap( new java.util.HashMap<String, Thread>(100) );
	}

	public void close() {
		Thread[] threads = processMap.values().toArray( new Thread[processMap.size()] );

		for( Thread thread : threads ) thread.interrupt();
		for( Thread thread : threads ) {
			try {
				thread.join();
			} catch( InterruptedException interruptEx ) {}
		}
	}

	public boolean continueProcessing() {
		return true;
	}

	public abstract boolean execute() throws InterruptedException;

	public boolean execute( String... args ) {
		return true;
	}

	/**
	 * process를 수행한다. process.execute()를 실행하고, execute() 실행여부와 관계없이 반드시 마지막에 close()가 호출된다.
	 */
	protected boolean execute( Process process ) {
		return execute( process, this.usingThread );
	}

	protected boolean execute( Process process, boolean usingThread ) {
		if( process == ProcessRunner.EMPTY ) return true;

		if( !usingThread ) {
			try {
				org.slf4j.MDC.put("prc.runnerName", getProcessName());
				org.slf4j.MDC.put("prc.processName", process.getProcessName());
				return process.execute();
			} catch( InterruptedException interruptEx ) {
			} finally {
				processMap.remove( process.getProcessName() );
				process.close();
				org.slf4j.MDC.remove("prc.processName");
				org.slf4j.MDC.remove("prc.runnerName");
			}
		} else {
			int executeStep = 0;

			try {
				org.slf4j.MDC.put("prc.runnerName", getProcessName());
				org.slf4j.MDC.put("prc.processName", process.getProcessName());
				if( !processMap.containsKey(process.getProcessName()) ) {
					try {
						while( maxThreadCount > 0 && processMap.size() >= maxThreadCount )
							sleep( DEFAULT_THREAD_WAIT_MILLIS );

						executeStep = 1;
						processMap.put( process.getProcessName(), null );
					} catch( InterruptedException interruptEx ) {
						return true;
					}

					final Process final_process = process;

					Thread thread = new Thread() {
						Process process = final_process;

						public void run() {
							try {
								if( process.execute() )
									synchronized( processMap ) { consistentThreadErrorCount = 0; }
								else
									synchronized( processMap ) { consistentThreadErrorCount++; }
							} catch( InterruptedException interruptEx ) {
							} finally {
								processMap.remove( process.getProcessName() );
								process.close();
							}
						}

						public String toString() {
							return process.getDescription();
						}
					};
					processMap.put( process.getProcessName(), thread );

					executeStep = 2;
					thread.start();
				}
			} finally {
				switch( executeStep ) {
				case 1:
					try { processMap.remove( process.getProcessName() ); } catch( Exception ignored ) {}
				case 0:
					process.close();
					org.slf4j.MDC.remove("prc.processName");
					org.slf4j.MDC.remove("prc.runnerName");
				}
			}
		}

		return true;
	}

	public boolean executing( String processName ) {
		return processMap.containsKey( processName );
	}

	public String getDescription() {
		return "ProcessRunner";
	}

	public int getExecutingCount() {
		return processMap.size();
	}

	public Logger getLogger() {
		return Logger.getLogger( "com.irt.rbm.tools.ProcessRunner" );
	}

	public int getMaxThreadCount() {
		return maxThreadCount;
	}

	public String getProcessName() {
		return "ProcessRunner";
	}

	public long getSleepMillis() {
		return sleepMillis;
	}

	public boolean getUsingDaemon() {
		return usingDaemon;
	}

	public boolean getUsingThread() {
		return usingThread;
	}

	public boolean isThreadFull() {
		return( maxThreadCount > 0 && processMap.size() >= maxThreadCount );
	}

	public void run() {
		Logger logger = getLogger();

		logger.info( getDescription() +" started." );
		try {
			while( true ) {
				try {
					if( execute() )
						consistentErrorCount = 0;
					else
						consistentErrorCount++;
				} catch( RuntimeException runtimeEx ) {
					logger.error( getDescription() +" execute error.", runtimeEx );
					consistentErrorCount++;
				}
				if( maxConsistentErrorCount > 0 && consistentErrorCount >= maxConsistentErrorCount ) break;
				if( maxConsistentErrorCount > 0 && consistentThreadErrorCount >= maxConsistentErrorCount ) break;

				sleep( sleepMillis );
			}
		} catch( InterruptedException interruptEx ) {
			consistentErrorCount = 0;
			consistentThreadErrorCount = 0;
		}
		close();

		if( consistentErrorCount == 0 && consistentThreadErrorCount == 0 )
			logger.info( getDescription() +" stopped." );
		else if( consistentErrorCount > consistentThreadErrorCount )
			logger.error( getDescription() +" stopped("+ consistentErrorCount +" consistent error)." );
		else
			logger.error( getDescription() +" stopped("+ consistentThreadErrorCount +" consistent error)." );
	}

	public void setMaxConsistentError( int maxConsistentErrorCount ) {
		this.maxConsistentErrorCount = maxConsistentErrorCount;
	}

	public void setMaxThreadCount( int maxThreadCount ) {
		this.maxThreadCount = maxThreadCount;
	}

	public void setSleepMiliis( long sleepMillis ) {
		this.sleepMillis = sleepMillis;
	}

	public void setSleepMillis( long sleepMillis ) {
		this.sleepMillis = sleepMillis;
	}

	public void setUsingDaemon( boolean usingDaemon ) {
		this.usingDaemon = usingDaemon;
	}

	public void setUsingThread( boolean usingThread ) {
		this.usingThread = usingThread;
	}

	public void waitThread() throws InterruptedException {
		while( processMap.size() > 0 )
			sleep( DEFAULT_THREAD_WAIT_MILLIS );
	}
}
