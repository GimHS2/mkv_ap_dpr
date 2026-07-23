/*
 *	File Name:	ProcessWrapper.java
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
public class ProcessWrapper implements Process {
	Process process;
	String processName, description;

	public ProcessWrapper( Process process ) {
		this( process, process.getProcessName(), process.getDescription() );
	}

	public ProcessWrapper( Process process, String processName ) {
		this( process, processName, process.getDescription() );
	}

	public ProcessWrapper( Process process, String processName, String description ) {
		this.process = process;
		this.processName = processName;
		this.description = description;
	}

	public void close() {
		process.close();
	}

	public boolean continueProcessing() {
		return process.continueProcessing();
	}

	public boolean execute() throws InterruptedException {
		return process.execute();
	}

	public String getDescription() {
		return description;
	}

	public String getProcessName() {
		return processName;
	}

	public Process getSourceProcess() {
		return process;
	}
}
