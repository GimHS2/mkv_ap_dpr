/*
 *	File Name:	ProcessImpl.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/06/30		2.2.2	boolean execute() -> boolean execute() throws InterruptedException
 *	stghr12		2011/02/28		2.2.1	void execute() throws ProcessException -> boolean execute()
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

/**
 *
 */
public class ProcessImpl implements Process {
	boolean executed;
	String processName, description;

	public ProcessImpl( String processName, String description ) {
		this.executed = false;
		this.processName = processName;
		this.description = description;
	}

	public void close() {}

	public boolean continueProcessing() {
		return executed;
	}

	public boolean execute() throws InterruptedException {
		return ( executed = true );
	}

	public String getDescription() {
		return description;
	}

	public String getProcessName() {
		return processName;
	}
}
