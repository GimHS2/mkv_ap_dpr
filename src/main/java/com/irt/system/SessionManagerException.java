/*
 *	File Name:	SessionManagerException.java
 *	Version:	2.0.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2008/09/26		2.0.0	TURNOVER_DISMATCHING_PASSWORD, USER_ACCOUNT_LOCKED 추가
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	stghr12		2002/04/15				create
 *
**/

package com.irt.system;

/**
 *
 */
public class SessionManagerException extends Exception {
	/**	SESSION값이 없는 경우 **/
	public final static String NO_SESSIONID				= "ERR_NO_SESSIONID";
	/**	SESSION값이 잘못된 경우 **/
	public final static String INVALID_SESSIONID		= "ERR_INVALID_SESSIONID";
	/**	업체ID 또는 사용자ID가 없는 경우 **/
	public final static String INVALID_USERID			= "ERR_INVALID_USERID";
	/**	암호가 잘못된 경우 **/
	public final static String INVALID_PASSWORD			= "ERR_INVALID_PASSWORD";
	/**	사용자가 사용가능한 상태가 아닌 경우 **/
	public final static String INVALID_USERSTATUS		= "ERR_INVALID_USERSTATUS";
	/**	SESSION이 강제 접속해제등을 당한 경우 **/
	public final static String INVALID_SESSIONSTATUS	= "ERR_INVALID_SESSIONSTATUS";
	/**	암호가 사용기간이 만료된 경우 **/
	public final static String PASSWORD_EXPIRED			= "ERR_PASSWORD_EXPIRED";
	/**	서비스 시작일이 아직 안된 경우 **/
	public final static String SERVICE_NOTSTARTED		= "ERR_SERVICE_NOTSTARTED";
	/**	서비스 종료일을 넘긴 경우 **/
	public final static String SERVICE_EXPIRED			= "ERR_SERVICE_EXPIRED";
	/**	동시접속회수를 초과한 경우 **/
	public final static String EXCEED_MAX_ACCESSCOUNT	= "ERR_EXCEED_MAX_ACCESSCOUNT";
	/**	로긴타임이나 세션타임을 초과한 경우 **/
	public final static String SESSION_TIMEOVER			= "ERR_SESSION_TIMEOVER";
	/**	회사가 권한이 없는 경우 **/
	public final static String HAS_NOAUTH_PARTY			= "ERR_HAS_NOAUTH_PARTY";
	/**	사용자가 권한이 없는 경우 **/
	public final static String HAS_NOAUTH_USER			= "ERR_HAS_NOAUTH_USER";
	/** 15분동안 연속 5번 암호가 틀린 경우 **/
	public final static String TURNOVER_DISMATCHING_PASSWORD	= "ERR_TURNOVER_DISMATCHING_PASSWORD";
	/** 15분동안 연속 5번 암호가 틀려 계정이 잠긴 경우 **/
	public final static String USER_ACCOUNT_LOCKED		= "ERR_USER_ACCOUNT_LOCKED";

	private String errorKey;

	public SessionManagerException( String errorKey ) {
		super( errorKey );
		this.errorKey = errorKey;
	}

	public SessionManagerException( Throwable throwable ) {
		super( throwable );
		this.errorKey = null;
	}

	public String getErrorKey() {
		return errorKey;
	}

	/**
	 * Session상태에 문제가 있어서 다시 로그인해야 하는 경우인지 여부
	 */
	public static boolean isSessionError( String errorKey ) {
		return(
			NO_SESSIONID.equals(errorKey)
			|| INVALID_SESSIONID.equals(errorKey)
			|| INVALID_SESSIONSTATUS.equals(errorKey)
			|| SESSION_TIMEOVER.equals(errorKey)
		);
	}

	/**
	 * Session상태에 문제가 있어서 다시 로그인해야 하는 경우인지 여부
	 */
	public boolean isSessionError() {
		return isSessionError( errorKey );
	}
}
