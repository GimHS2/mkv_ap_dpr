/*
 *	File Name:	Process.java
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
public interface Process {
	public void close();

	/**
	 * execute() 후 다음수행을 계속 진행할 것이 여부를 return. (예를 들어, nextScheduleDateTime 갱신, 처리한 문서 이동 등)
	 */
	public boolean continueProcessing();

	public boolean execute() throws InterruptedException;

	public String getDescription();

	public String getProcessName();
}
