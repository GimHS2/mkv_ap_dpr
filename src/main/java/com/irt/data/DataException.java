/*
 *	File Name:	DataException.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2013/08/30		2.2.2	XLX 관련 Exception추가
 *	stghr12		2011/06/30		2.2.1	생성자에 Throwable 추가
 *	stghr12		2010/08/31		2.2.0	ERR_IO_TOO_MANY_ERRORLINE 추가
 *	stghr12		2007/04/30		2.1.1	ERR_LARGE_FILESIZE 추가
 *	stghr12		2006/12/01		2.1.0	ERR_IO_INVALID_HEADERLINE, ERR_IO_INVALID_FLV_LINE, ERR_IO_INCORRECT_ENCODING 추가
 *										linenum, getLineNumber(), 생성자 추가
 *	stghr12		2006/07/19		2.0.1	ERR_DEADLOCK_DETECTED 추가
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	stghr12		2002/04/15				create
 *
**/

package com.irt.data;

import java.util.Map;

/**
 * Data처리중에 발생한 Exception Class.
 */
public class DataException extends Exception {
	/** 정의되지 않은 그외의 에러 **/
	public final static String ERR_ERROR				= "ERR_ERROR";
	/** 사용중일 경우 */
	public final static String ERR_RECORD_LOCKED		= "ERR_RECORD_LOCKED";
	/** DEADLOCK 발생한 경우 */
	public final static String ERR_DEADLOCK_DETECTED	= "ERR_DEADLOCK_DETECTED";
	/** 이미 입력된 항목의 경우 **/
	public final static String ERR_UNIQUE_CONSTRAINT	= "ERR_UNIQUE_CONSTRAINT";
	/** CHECK에서 걸린 경우 **/
	public final static String ERR_CHECK_VIOLATED		= "ERR_CHECK_VIOLATED";
	/** 부모키가 없는 경우 **/
	public final static String ERR_PARENTKEY_NOTFOUND	= "ERR_PARENTKEY_NOTFOUND";
	/** 자식키가 있는 경우(삭제가 안됨) **/
	public final static String ERR_CHILD_RECORD_FOUND	= "ERR_CHILD_RECORD_FOUND";
	/** 입력값이 필요이상으로 많은 경우 **/
	public final static String ERR_TOO_MANY_VALUE		= "ERR_TOO_MANY_VALUE";
	/** Connection이 잘못된 경우 **/
	public final static String ERR_INVALID_CONNECTION	= "ERR_INVALID_CONNECTION";
	/** NULL이어야 하는 항목에 값이 있는 경우 **/
	public final static String ERR_EXIST_NOTNULL_VALUE	= "ERR_EXIST_NOTNULL_VALUE";
	/** 필수항목을 입력하지 않은 경우 **/
	public final static String ERR_CANNOT_NULL			= "ERR_CANNOT_NULL";
	/** 입력값이 큰 경우(수치/문자 모두) */
	public final static String ERR_LARGE_VALUE			= "ERR_LARGE_VALUE";
	/** 입력값이 작은 경우(수치/문자 모두) */
	public final static String ERR_SMALL_VALUE			= "ERR_SMALL_VALUE";
	/** 수정할 항목이 없는 경우 **/
	public final static String ERR_NO_RECORD_UPDATE		= "ERR_NO_RECORD_UPDATE";
	/** 삭제할 항목이 없는 경우 **/
	public final static String ERR_NO_RECORD_DELETE		= "ERR_NO_RECORD_DELETE";
	/** Code등의 길이가 맞지 않을 때 **/
	public final static String ERR_INVALID_LEN			= "ERR_INVALID_LEN";
	/** 값이 잘못된 경우 **/
	public final static String ERR_INVALID_VALUE		= "ERR_INVALID_VALUE";
	/** 사용할 수 없는 문자가 포함된 경우 **/
	public final static String ERR_INVALID_CHAR			= "ERR_INVALID_CHAR";
	/** 수치형에 문자가 들어간 경우 **/
	public final static String ERR_INVALID_NUMBER		= "ERR_INVALID_NUMBER";
	/** 날짜형이 적절하지 못할 경우 **/
	public final static String ERR_INVALID_DATE			= "ERR_INVALID_DATE";
	/** 날짜범위가 잘못된 경우 **/
	public final static String ERR_INVALID_DATESCOPE	= "ERR_INVALID_DATESCOPE";
	/** 숫자범위가 잘못된 경우 **/
	public final static String ERR_INVALID_NUMBERSCOPE	= "ERR_INVALID_NUMBERSCOPE";
	/** 입력파일이 너무 큰 경우 **/
	public final static String ERR_LARGE_FILESIZE		= "ERR_LARGE_FILESIZE";

	/** 상속관계를 갖는 코드에서 상위 코드가 없는 경우 **/
	public final static String ERR_NO_UPPERLEVELCODE	= "ERR_NO_UPPERLEVELCODE";
	/** INSERT 실패 **/
	public final static String ERR_CANNOT_INSERT		= "ERR_CANNOT_INSERT";
	/** UPDATE 실패 **/
	public final static String ERR_CANNOT_UPDATE		= "ERR_CANNOT_UPDATE";
	/** DELETE 실패 **/
	public final static String ERR_CANNOT_DELETE		= "ERR_CANNOT_DELETE";

	/** 파일내용이 하나도 없을 경우 **/
	public final static String ERR_IO_BLANK_FILE		= "ERR_IO_BLANK_FILE";
	/** 파일 Header Line이 잘못된 경우 **/
	public final static String ERR_IO_INVALID_HEADERLINE= "ERR_IO_INVALID_HEADERLINE";
	/** CSV파일 라인이 잘못된 경우 **/
	public final static String ERR_IO_INVALID_CSV_LINE	= "ERR_IO_INVALID_CSV_LINE";
	/** 고정장파일 라인이 잘못된 경우 **/
	public final static String ERR_IO_INVALID_FLV_LINE	= "ERR_IO_INVALID_FLV_LINE";
	/** 파일 encoding이 잘못된 경우 */
	public final static String ERR_IO_INCORRECT_ENCODING= "ERR_IO_INCORRECT_ENCODING";

	/** XLX관련 정의되지 않은 그외의 에러 **/
	public final static String ERR_IO_XLX_EXCEPTION							= "ERR_IO_XLX_EXCEPTION";
	/** xlsx file 읽을 때 익셉션: InvalidFormatException, OpenXML4JException, ParserConfigurationException, SAXException **/
	public final static String ERR_IO_XLX_INVALID_FORMAT_EXCEPTION			= "ERR_IO_XLX_INVALID_FORMAT_EXCEPTION";
	public final static String ERR_IO_XLX_OPENXML4J_EXCEPTION				= "ERR_IO_XLX_OPENXML4J_EXCEPTION";
	public final static String ERR_IO_XLX_PARSER_CONFIGURATION_EXCEPTION	= "ERR_IO_XLX_PARSER_CONFIGURATION_EXCEPTION";
	public final static String ERR_IO_XLX_SAX_EXCEPTION						= "ERR_IO_XLX_SAX_EXCEPTION";
	/** fileType 요청이 잘못된 경우 **/
	public final static String ERR_IO_INVALID_REQUEST_FILETYPE				= "ERR_IO_INVALID_REQUEST_FILETYPE";
	/** fileType 중 엑셀 파일타입 요청이 잘못된 경우 **/
	public final static String ERR_IO_INVALID_REQUEST_XSL_FILETYPE				= "ERR_IO_INVALID_REQUEST_XSL_FILETYPE";
	/** 엑셀파일이 잘못된 경우( xls도 아니고, xlsx도 아닌 경우 ) **/
	public final static String ERR_IO_INVALID_XSL_FILE						= "ERR_IO_INVALID_XSL_FILE";
	public final static String ERR_IO_XLX_EXCEED_MAX_ROW					= "ERR_IO_XLX_EXCEED_MAX_ROW";

	private String errorKey;
	private Map recordMap;
	private int linenum = -1;

	public DataException( String errorKey, String message ) {
		super( message );
		this.errorKey = errorKey;
	}

	public DataException( String errorKey, String message, Throwable throwable ) {
		super( message, throwable );
		this.errorKey = errorKey;
	}

	public DataException( String errorKey, String message, Map recordMap ) {
		super( message );
		this.errorKey = errorKey;
		this.recordMap = recordMap;
	}
	public DataException( String errorKey, String message, Throwable throwable, Map recordMap ) {
		super( message, throwable );
		this.errorKey = errorKey;
		this.recordMap = recordMap;
	}

	public DataException( int linenum, DataException dataEx ) {
		super( dataEx.getMessage() );
		this.errorKey = dataEx.errorKey;
		this.recordMap = dataEx.recordMap;
		this.linenum = linenum;
	}

	public String getErrorKey() {
		return errorKey;
	}

	/**
	 * 에러 라인넘버를 return(데이터가 없을 경우 -1 return).
	 */
	public int getLineNumber() {
		return linenum;
	}

	public Map getRecordMap() {
		return recordMap;
	}
}
