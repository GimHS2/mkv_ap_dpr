/*
 *	File Name:	InvalidPasswordException.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/09/26				create
 *
**/

package com.irt.dpr;

import java.util.Map;

/**
 * Password policy 검사 중에 발생한 Exception
 */
public class InvalidPasswordException extends Exception {
	public final static String ERR_PASSWORD_MINIMUM_LENGTH				= "ERR_PASSWORD_MINIMUM_LENGTH";
	public final static String ERR_PASSWORD_MINLENGTH_COMPLEXITY_6		= "ERR_PASSWORD_MINLENGTH_COMPLEXITY_6";
	public final static String ERR_PASSWORD_MINLENGTH_COMPLEXITY_8		= "ERR_PASSWORD_MINLENGTH_COMPLEXITY_8";
	public final static String ERR_PASSWORD_INCLUDING_USERINFO			= "ERR_PASSWORD_INCLUDING_USERINFO";
	public final static String ERR_PASSWORD_BEFORE_USED_PASSWORD		= "ERR_PASSWORD_BEFORE_USED_PASSWORD";

	private String errorKey;

	public InvalidPasswordException( String errorKey ) {
		super( errorKey );
		this.errorKey = errorKey;
	}

	public InvalidPasswordException( Throwable throwable ) {
		super( throwable );
		this.errorKey = null;
	}

	public InvalidPasswordException( String errorKey, Throwable throwable ) {
		super( throwable );
		this.errorKey = errorKey;
	}

	public String getErrorKey() {
		return errorKey;
	}
}
