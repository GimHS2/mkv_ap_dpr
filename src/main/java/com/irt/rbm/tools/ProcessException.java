/*
 *	File Name:	ProcessException.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

/**
 *
 */
public class ProcessException extends Exception {
	public ProcessException( String message ) {
		super( message );
	}

	public ProcessException( Throwable throwable ) {
		super( throwable );
	}
}
