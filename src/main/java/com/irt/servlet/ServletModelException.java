/*
 *	File Name:	ServletModelException.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.0c	dataException 이 throwable일 경우 처리.
 *	stghr12		2008/07/25		2.2.0	ServletModelException(throwable): super(throwable) -> super(throwable.getMessage(), throwable)
 *	stghr12		2006/12/01		2.1.0	CANNOT_FIND_COLUMNPOOLSET -> CANNOT_FIND_COLUMNRESOURCEBUNDLE
 *										ERROR 추가
 *										생성자 ServletModelException( errorKey, message ) 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.servlet;

/**
 *
 */
public class ServletModelException extends javax.servlet.ServletException {
	public final static String ERROR					= "ERR_ERROR";
	/** mode값이 잘못된 경우 */
	public final static String INVALID_MODE				= "ERR_INVALID_MODE";
	/** 필요한 PARAMETER가 없는 경우 */
	public final static String NEEDED_PARAMETER			= "ERR_NEEDED_PARAMETER";
	/** PARAMETER의 값, 길이 등이 잘못된 경우 */
	public final static String INVALID_PARAMETER		= "ERR_INVALID_PARAMETER";
	/** NEEDED_PARAMETER, INVALID_PARAMETER외에 요청이 잘못된 경우 */
	public final static String INVALID_REQUEST			= "ERR_INVALID_REQUEST";
	/** 기능에 권한이 없는 경우 */
	public final static String HAS_NOAUTH				= "ERR_HAS_NOAUTH";
	/** 업로드한 파일이 너무 클 경우 */
	public final static String TOO_LARGE_FILE			= "ERR_TOO_LARGE_FILE";
	/** 알 수 없는 Exception이 발생한 경우 */
	public final static String INTERNAL_ERROR			= "ERR_INTERNAL_ERROR";
	/** DataHandler가 없을 경우 */
	public final static String INVALID_DATAHANDLER		= "ERR_INVALID_DATAHANDLER";
	/** Session이 잘못된 경우 */
	public final static String INVALID_SESSION			= "ERR_INVALID_SESSION";
	/** info(), modifyInput() 등에서 getRecord()의 결과가 null인 경우 */
	public final static String NO_RECORD_FOUND			= "ERR_NO_RECORD_FOUND";
	/** ColumnList를 찾을 수 없는 경우 */
	public final static String CANNOT_FIND_COLUMNLIST	= "ERR_CANNOT_FIND_COLUMNLIST";
	/** ColumnResourceBundle을 찾을 수 없는 경우 */
	public final static String CANNOT_FIND_COLUMNRESOURCEBUNDLE	= "ERR_CANNOT_FIND_COLUMNRESOURCEBUNDLE";

	private String errorKey;

	public ServletModelException( String errorKey ) {
		super( errorKey );
		this.errorKey = errorKey;
	}

	public ServletModelException( String errorKey, String message ) {
		super( message );
		this.errorKey = errorKey;
	}

	public ServletModelException( com.irt.data.DataException throwable ) {
		super( throwable.getMessage(), throwable );
		this.errorKey = ((com.irt.data.DataException)throwable).getErrorKey();
	}

	public ServletModelException( Throwable throwable ) {
		super( throwable.getMessage(), throwable );
		this.errorKey = null;
	}

	public String getErrorKey() {
		return errorKey;
	}
}
