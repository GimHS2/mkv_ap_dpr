/*
 *	File Name:	DataReader.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2013/08/30		2.2.1	FileType상속. isBinary() 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.data;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public interface DataReader extends com.irt.util.FileType {
	public void close() throws IOException;

	/**
	 * 최근에 읽은 Line수를 return.
	 */
	public int getLineNumber();

	/**
	 * 최근에 읽은 Line 데이터를 return.
	 */
	public String[] getLines();

	/**
	 * 최근에 읽은 Line을 return.
	 */
	public String getLineString();

	/**
	 * DataReader가 binary 형태인지 여부 return
	 */
	public boolean isBinary();

	/**
	 * 읽은 Line이 빈칸인지 여부
	 */
	public boolean isBlankLine();

	/**
	 * EOF여부 return.
	 */
	public boolean isEOF();

	/**
	 * 다음 Line을 읽는다.
	 * 읽은 데이터를 keys순으로 Map에 put한다.
	 * @return 파일 끝일 경우 null return.
	 */
	public Map<String, Object> readNext( String[] keys ) throws DataException, IOException;

	/**
	 * 다음 Line을 읽는다.
	 * @return 파일 끝일 경우 null return.
	 */
	public String[] readNext() throws DataException, IOException;

	/**
	 * Line을 읽을 때, 데이터를 trim해서 읽을 것인지 여부 설정.
	 */
	public void setTrim( boolean trim );
}
