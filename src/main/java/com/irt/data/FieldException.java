/*
 *	File Name:	FieldException.java
 *	Version:	2.1.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2006/12/01		2.1.0	ERR_INVALID_TIME 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

/**
 *
 */
public class FieldException extends Exception {
	/** 필수항목의 값이 없음 **/
	public final static String ERR_CANNOT_NULL			= "ERR_CANNOT_NULL";
	/** 값이 숫자형이 아님 **/
	public final static String ERR_INVALID_NUMBER		= "ERR_INVALID_NUMBER";
	/** 값이 날짜형이 아님 **/
	public final static String ERR_INVALID_DATE			= "ERR_INVALID_DATE";
	/** 값이 시간형이 아님 **/
	public final static String ERR_INVALID_TIME			= "ERR_INVALID_TIME";
	/** 값의 길이가 잘못됨 **/
	public final static String ERR_INVALID_LENGTH		= "ERR_INVALID_LENGTH";
	/** 값의 길이가 짧음 **/
	public final static String ERR_SMALL_LENGTH			= "ERR_SMALL_LENGTH";
	/** 값의 길이가 김 **/
	public final static String ERR_LARGE_LENGTH			= "ERR_LARGE_LENGTH";
	/** 값이 너무 작음 **/
	public final static String ERR_SMALL_VALUE			= "ERR_SMALL_VALUE";
	/** 값이 너무 큼 **/
	public final static String ERR_LARGE_VALUE			= "ERR_LARGE_VALUE";
	/** 사용할 수 없는 문자가 포함된 경우 **/
	public final static String ERR_INVALID_CHAR			= "ERR_INVALID_CHAR";
	/** 값의 잘못됨 **/
	public final static String ERR_INVALID_VALUE		= "ERR_INVALID_VALUE";
	/** 잘못된 Object일 경우 **/
	public final static String ERR_INVALID_TYPE			= "ERR_INVALID_TYPE";

	private String errorKey;
	private AbstractField errorField;
	private Object errorFieldValue;

	public FieldException( String errorKey, AbstractField errorField ) {
		this( errorKey, errorField, null );
	}

	public FieldException( String errorKey, AbstractField errorField, Object errorFieldValue ) {
		super( errorKey );
		this.errorKey = errorKey;
		this.errorField = errorField;
		this.errorFieldValue = errorFieldValue;
	}

	public String getErrorKey() {
		return errorKey;
	}

	public AbstractField getErrorField() {
		return errorField;
	}

	public Object getErrorFieldValue() {
		return errorFieldValue;
	}
}
