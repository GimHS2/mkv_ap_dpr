/*
 *	File Name:	CanonicalXMLException.java
 *	Version:	2.2.1(mjsnjsAP)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2014/12/30		2.2.1	INVALID_ORDERSTATUS 추가
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.dpr.tools;

/**
 *
 */
public class CanonicalXMLException extends Exception {
	/** com.irt.dpr.Order.getOrderStatus() 에서 order status를 가져오지 못할 경우. */
	public final static String INVALID_ORDERSTATUS			= "ERR_INVALID_ORDERSTATUS";

	private String errorKey;
	Exception exception;

	public CanonicalXMLException( Exception exception ) {
		super( exception.getMessage() );
		this.exception = exception;
	}

	public CanonicalXMLException( String errorKey ) {
		super( errorKey );
		this.errorKey = errorKey;
	}

	public CanonicalXMLException( String errorKey, String message ) {
		super( message );
		this.errorKey = errorKey;
		this.exception = exception;
	}

	public CanonicalXMLException( String errorKey , Exception exception ) {
		super( errorKey );
		this.errorKey = errorKey;
		this.exception = exception;
	}

	public Exception getException() {
		return exception;
	}

	public String getErrorKey() {
		return this.errorKey;

	}
}
