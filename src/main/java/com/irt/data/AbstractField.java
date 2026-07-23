/*
 *	File Name:	AbstractField.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	TYPE_DATETIME 추가, getValidDateFormat() 삭제
 *	stghr12		2006/12/01		2.1.0	com.irt.data.format.RecordFormat extends로 변경
 *										dataType을 int형에서 char형으로 변경
 *										TYPE_DESCRIPTION, TYPE_DATE 값 변경
 *										TYPE_TIME 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

/**
 *
 */
public interface AbstractField extends com.irt.data.format.RecordFormat {
	public final static char TYPE_NONE					= ' ';
	/** 문자형: 값이 정해져 있는 문자 **/
	public final static char TYPE_CODE					= 'C';
	/** 문자형: 명칭, 설명 등 **/
	public final static char TYPE_DESCRIPTION			= 'E';
	/** 문자형: 일반적인 문자 **/
	public final static char TYPE_STRING				= 'S';
	/** 숫자형: Integer **/
	public final static char TYPE_INTEGER				= 'I';
	/** 숫자형: Long **/
	public final static char TYPE_LONG					= 'L';
	/** 숫자형: Double **/
	public final static char TYPE_DOUBLE				= 'F';
	/** 날짜형 **/
	public final static char TYPE_DATE					= 'D';
	/** 날짜시간형 **/
	public final static char TYPE_DATETIME				= 'T';
	/** 시간형 **/
	public final static char TYPE_TIME					= 'M';

	public char getDataType();

	public String getDescriptionKey();

	public String getFieldKey();

	public String getPrefixKey();
}
