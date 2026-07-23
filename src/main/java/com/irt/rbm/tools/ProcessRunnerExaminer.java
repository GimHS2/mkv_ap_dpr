/*
 *	File Name:	ProcessRunnerExaminer.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/06/30		2.2.2	boolean execute() -> boolean execute() throws InterruptedException
 *	stghr12		2011/02/28		2.2.1	void execute() throws ProcessException -> boolean execute()
 *	stghr12		2010/05/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 */
public abstract class ProcessRunnerExaminer extends ProcessRunner {
	private final static long DEFAULT_WATING_MILLIS		= 30000;
	Configure configure;

	String[] runnerNames = null;
	Set<ProcessRunner> runnerSet = null;

	public ProcessRunnerExaminer() {}

	public boolean execute() throws InterruptedException {
		if( runnerSet == null ) {
			sleep( DEFAULT_WATING_MILLIS );

			runnerSet = new java.util.HashSet<ProcessRunner>();
			if( runnerNames == null ) {
				for( ProcessRunner runner : configure.getProcessRunners() )
					if( runner != this )
						runnerSet.add( runner );
			} else {
				for( String runnerName : runnerNames ) {
					ProcessRunner runner = configure.getProcessRunner( runnerName );
					if( runner != null && runner != this )
						runnerSet.add( runner );
				}
			}
		}

		for( java.util.Iterator<ProcessRunner> iterator = runnerSet.iterator(); iterator.hasNext(); ) {
			ProcessRunner runner = iterator.next();

			if( !runner.isAlive() ) {
				try {
					if( !handleDeadProcessRunner(runner) )
						iterator.remove();
				} catch( ProcessException processEx ) {
					getLogger().error( getDescription() +" error.", processEx.getCause() != null ? processEx.getCause() : processEx );
					return false;
				}
			}
		}

		return true;
	}

	public String getDescription() {
		return "ProcessRunnerExaminer";
	}

	/**
	 *	@return runner를 다시 실행시켰는지 여부
	 */
	public abstract boolean handleDeadProcessRunner( ProcessRunner runner ) throws ProcessException;

	public void setConfigure( Configure configure ) {
		this.configure = configure;
	}

	public void setProcessRunnerName( String runnerNameList ) {
		this.runnerNames = runnerNameList.split( "," );
	}
}
