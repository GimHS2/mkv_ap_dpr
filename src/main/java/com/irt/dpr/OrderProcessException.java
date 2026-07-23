/*
 *	File Name:	OrderProcessException.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2009/10/23		2.2.1	ERR_INVALID_ORDER_LINES 에러코드 추가
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

/**
 *
 */
public class OrderProcessException extends Exception {
	public final static String ERR_CANNOT_GET_PROCESS_PRIORITY		= "ERR_ORDERPROCESS_PROCESS_PRIORITY";
	public final static String ERR_CANNOT_LOCK_ORDER				= "ERR_ORDERPROCESS_LOCKING_RECORD";

	// Paramter가 잘못된 경우
	public final static String ERR_INVALID_PARAMETER 				= "ERR_INVALID_PARAMETER";
	// 전송할 Data가 없는 경우
	public final static String ERR_HAS_NODATA 						= "ERR_HAS_NODATA";
	// Connection 단계에서 실패 한 경우
	public final static String ERR_WM_CONNECTION_FAILED 			= "ERR_WM_CONNECTION_FAILED";
	// Service를 invoke 하지 못한 경우
	public final static String ERR_WM_INVOKING_FAILED 				= "ERR_WM_INVOKING_FAILED";
	// 요청할 Data가 없는 경우
	public final static String ERR_WM_EMPTY_REQUEST_DATA	  		= "ERR_WM_EMPTY_REQUEST_DATA";
	// WM과 Data creation 단계에서 실패한 경우
	public final static String ERR_WM_DATACREATION_FAILED   		= "ERR_WM_DATACREATION_FAILED";
	// WM Result 가 없거나 읽지 못하는 경우
	public final static String ERR_WM_NO_READ_DATA      			= "ERR_WM_NO_READ_DATA";
	// Data가 format과 상이한 경우
	public final static String ERR_WM_DATA_INCORRECT_FORMAT			= "ERR_WM_DATA_INCORRECT_FORMAT";
	// Result를 DB에 저장 하는 단계에서 실패한 경우
	public final static String ERR_WM_APPLY_RESULT      			= "ERR_WM_APPLY_RESULT";
	// XML과 관련한 에러
	public final static String ERR_WM_CANONICALXML_FAILED   		= "ERR_WM_CANONICALXML_FAILED";
	// Order Simulation 단계에서 PIPO(Phase In, Phase Out)에 의해 추가된 Line으로 인한 오류
	public final static String ERR_INVALID_ORDER_LINES   			= "ERR_INVALID_ORDER_LINES";

	Exception exception;
	String errorKey;

	public OrderProcessException( Exception exception ) {
		super( exception.getMessage() );
		this.exception = exception;
	}

	public OrderProcessException( String errorKey ) {
		super( errorKey );
		this.errorKey = errorKey;
	}

	public OrderProcessException( String errorKey, Exception exception ) {
		super( exception.getMessage(), exception );
		this.exception = exception;
		this.errorKey = errorKey;
	}

	public Exception getException() {
		return exception;
	}

	public String getErrorKey() {
		return this.errorKey;
	}
}
