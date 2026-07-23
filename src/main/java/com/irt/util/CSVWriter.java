/*
 *	File Name:	CSVWriter.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.1	DataWriter 변경사항 적용: getDataType(), setDataType() 추가
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.util;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.StringTokenizer;

/**
 * CSV 형식 Writer.
 */
public class CSVWriter implements com.irt.data.DataWriter {
	protected final static char DEFAULT_DELIM				= ',';
	protected final static char DEFAULT_ENCAP				= '"';

	private String lineSeparator;
	private char delim, encap;
	private PrintWriter out;

	private char dataType = DATA;
	private int linenum;
	private boolean firstElement;

	/**
	 * default delimiter, default encapsulator 사용한 CSVWriter 생성.
	 */
	public CSVWriter( Writer out ) {
		this( out, DEFAULT_DELIM, DEFAULT_ENCAP, null );
	}

	/**
	 * default delimiter, default encapsulator 사용한 CSVWriter 생성.
	 */
	public CSVWriter( Writer out, String lineSeparator ) {
		this( out, DEFAULT_DELIM, DEFAULT_ENCAP, lineSeparator );
	}

	/**
	 * 주어진 delimiter를 사용한 CSVWriter 생성.(encapsulator 없음)
	 */
	public CSVWriter( Writer out, char delim ) {
		this( out, delim, delim, null );
	}

	/**
	 * 주어진 delimiter를 사용한 CSVWriter 생성.(encapsulator 없음)
	 */
	public CSVWriter( Writer out, char delim, String lineSeparator ) {
		this( out, delim, delim, lineSeparator );
	}

	/**
	 * 주어진 delimiter, encapsulator를 사용한 CSVWriter 생성.
	 */
	public CSVWriter( Writer out, char delim, char encap ) {
		this( out, delim, encap, null );
	}

	/**
	 * 주어진 delimiter, encapsulator를 사용한 CSVWriter 생성.
	 */
	public CSVWriter( Writer out, char delim, char encap, String lineSeparator ) {
		if( out == null )
			throw new IllegalArgumentException( "out cannot be null" );

		if( out instanceof PrintWriter )
			this.out = (PrintWriter)out;
		else
			this.out = new PrintWriter( out );
		this.lineSeparator = lineSeparator;
		this.delim = delim;
		this.encap = encap;
		this.linenum = 1;
		this.firstElement = true;
	}

	private String convertString( String s ) {
		if( s == null ) return "";
		if( s.indexOf(encap) < 0 ) {
			if( s.indexOf(delim) < 0 )
				return s;
			else
				return encap + s + encap;
		} else if( encap == delim )
			return s;

		StringBuffer sbuf = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer( s, String.valueOf(encap), true );
		sbuf.append( encap );
		while( tokenizer.hasMoreTokens() ) {
			String token = tokenizer.nextToken();
			sbuf.append( token );
			if( token.charAt(0) == encap )
				sbuf.append( encap );
		}
		sbuf.append( encap );

		return sbuf.toString();
	}

	public void close() {
		out.close();
	}

	public void flush() {
		out.flush();
	}

	public char getDataType() {
		return dataType;
	}

	public int getLineNumber() {
		return firstElement ? linenum - 1 : linenum;
	}

	public void print( double d ) {
		printDelimiter();
		out.print( convertString(String.valueOf(d)) );
	}

	public void print( float f ) {
		printDelimiter();
		out.print( convertString(String.valueOf(f)) );
	}

	public void print( int i ) {
		printDelimiter();
		out.print( convertString(String.valueOf(i)) );
	}

	public void print( long l ) {
		printDelimiter();
		out.print( convertString(String.valueOf(l)) );
	}

	public void print( Object o ) {
		printDelimiter();
		if( o != null ) out.print( convertString(o.toString()) );
	}

	public void print( Object o, int colspan ) {
		printDelimiter();
		if( o != null ) out.print( convertString(o.toString()) );
		while( --colspan > 0 ) out.print( delim );
	}

	public void print( String s ) {
		printDelimiter();
		out.print( convertString(s) );
	}

	public void print( String s, int colspan ) {
		printDelimiter();
		out.print( convertString(s) );
		while( --colspan > 0 ) out.print( delim );
	}

	public void print( String... ss ) {
		if( ss.length > 0 ) {
			printDelimiter();
			out.print( convertString(ss[0]) );
			for( int i = 1; i < ss.length; i++ ) {
				out.print( delim );
				out.print( convertString(ss[i]) );
			}
		}
	}

	public void println() {
		if( lineSeparator == null )
			out.println();
		else
			out.print( lineSeparator );

		linenum++;
		firstElement = true;
	}

	public void println( String... ss ) {
		print( ss );
		println();
	}

	private void printDelimiter() {
		if( firstElement )
			firstElement = false;
		else
			out.print( delim );
	}

	public void printNull() {
		printDelimiter();
	}

	public void printNull( int nullcnt ) {
		if( nullcnt <= 0 ) return;
		printDelimiter();
		while( --nullcnt > 0 ) out.print( delim );
	}

	public void setDataType( char dataType ) {
		this.dataType = dataType;
	}

	public String getFileType() {
		// TODO Auto-generated method stub
		return null;
	}
}
