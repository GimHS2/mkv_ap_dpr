/*
 *	File Name:	DataWriter.java
 *	Version:	2.2.1c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2014/09/30		2.2.1c	FIELDHEADER, FIELDDATA 타입 추가.
 *	jbaek		2013/08/30		2.2.1	FileType상속. close(), flush() IOException 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.1	getDataType(), setDataType() 추가
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.data;

import java.io.IOException;

/**
 *
 */
public interface DataWriter extends com.irt.util.FileType {
	public final static char TITLE						= 'T';
	public final static char DATA						= 'D';
	public final static char SUMMARY					= 'S';
	public final static char FIELDHEADER				= 'H';
	public final static char FIELDDATA					= 'F';

	public void close() throws IOException;

	public void flush() throws IOException;

	public char getDataType();

	/**
	 * 출력한 Line수를 return.
	 */
	public int getLineNumber();

	public void print( double d ) throws IOException;

	public void print( float f ) throws IOException;

	public void print( int i ) throws IOException;

	public void print( long l ) throws IOException;

	public void print( Object o ) throws IOException;

	public void print( Object o, int colspan ) throws IOException;

	public void print( String s ) throws IOException;

	public void print( String s, int colspan ) throws IOException;

	public void print( String... ss ) throws IOException;

	public void println() throws IOException;

	public void println( String... ss ) throws IOException;

	/**
	 * 빈문자를 출력.
	 */
	public void printNull() throws IOException;

	/**
	 * 빈문자를 nullcnt번 출력.
	 */
	public void printNull( int nullcnt ) throws IOException;

	public void setDataType( char dataType );
}
