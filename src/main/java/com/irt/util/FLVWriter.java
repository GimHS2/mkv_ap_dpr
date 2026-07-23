/*
 *	File Name:	FLVWriter.java
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * 고정장 형식 Writer.
 */
public class FLVWriter implements com.irt.data.DataWriter {
	private String lineSeparator;
	private int[] lengths;
	private boolean[] leftAligns;
	private boolean isByteLength;
	private PrintWriter out;

	private char dataType = DATA;
	private int linenum;
	private int index;

	/**
	 * 고정길이가 lengths인 FLVWriter 생성(byte단위로 길이 계산, 좌측정렬).
	 */
	public FLVWriter( Writer out, int[] lengths ) {
		this( out, lengths, null, true, null );
	}

	/**
	 * 고정길이가 lengths인 FLVWriter 생성(byte단위로 길이 계산, 좌측정렬).
	 */
	public FLVWriter( Writer out, int[] lengths, String lineSeparator ) {
		this( out, lengths, null, true, lineSeparator );
	}

	/**
	 * 고정길이가 lengths인 FLVWriter 생성(좌측정렬).
	 * @param isByteLength byte단위로 길이를 계산할 것인지 여부. false일 경우에는 charater단위로 계산.
	 */
	public FLVWriter( Writer out, int[] lengths, boolean isByteLength ) {
		this( out, lengths, null, true, null );
	}

	/**
	 * 고정길이가 lengths인 FLVWriter 생성(좌측정렬).
	 * @param isByteLength byte단위로 길이를 계산할 것인지 여부. false일 경우에는 charater단위로 계산.
	 */
	public FLVWriter( Writer out, int[] lengths, boolean isByteLength, String lineSeparator ) {
		this( out, lengths, null, true, lineSeparator );
	}

	/**
	 * 고정길이가 lengths이고, 정렬이 leftAligns인 FLVWriter 생성(byte단위로 길이 계산).
	 * @param leftAligns true는 좌측정렬, false는 우측정렬.
	 */
	public FLVWriter( Writer out, int[] lengths, boolean[] leftAligns ) {
		this( out, lengths, leftAligns, true, null );
	}

	/**
	 * 고정길이가 lengths이고, 정렬이 leftAligns인 FLVWriter 생성(byte단위로 길이 계산).
	 * @param leftAligns true는 좌측정렬, false는 우측정렬.
	 */
	public FLVWriter( Writer out, int[] lengths, boolean[] leftAligns, String lineSeparator ) {
		this( out, lengths, leftAligns, true, lineSeparator );
	}

	/**
	 * 고정길이가 lengths이고, 정렬이 leftAligns인 FLVWriter 생성.
	 * @param leftAligns true는 좌측정렬, false는 우측정렬.
	 * @param isByteLength byte단위로 길이를 계산할 것인지 여부. false일 경우에는 charater단위로 계산.
	 */
	public FLVWriter( Writer out, int[] lengths, boolean[] leftAligns, boolean isByteLength ) {
		this( out, lengths, leftAligns, isByteLength, null );
	}

	/**
	 * 고정길이가 lengths이고, 정렬이 leftAligns인 FLVWriter 생성.
	 * @param leftAligns true는 좌측정렬, false는 우측정렬.
	 * @param isByteLength byte단위로 길이를 계산할 것인지 여부. false일 경우에는 charater단위로 계산.
	 */
	public FLVWriter( Writer out, int[] lengths, boolean[] leftAligns, boolean isByteLength, String lineSeparator ) {
		if( out == null )
			throw new IllegalArgumentException( "out cannot be null" );
		if( lengths == null )
			throw new IllegalArgumentException( "lengths cannot be null" );

		if( out instanceof PrintWriter )
			this.out = (PrintWriter)out;
		else
			this.out = new PrintWriter( out );
		this.lineSeparator = lineSeparator;
		this.lengths = lengths;
		this.leftAligns = leftAligns;
		this.isByteLength = isByteLength;
		this.linenum = 1;
		this.index = 0;
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
		return index == 0 ? linenum - 1 : linenum;
	}

	public void print( double d ) throws IOException {
		print( String.valueOf(d) );
	}

	public void print( float f ) throws IOException {
		print( String.valueOf(f) );
	}

	public void print( int i ) throws IOException {
		print( String.valueOf(i) );
	}

	public void print( long l ) throws IOException {
		print( String.valueOf(l) );
	}

	public void print( Object o ) throws IOException {
		if( o == null )
			printNull();
		else
			print( o.toString() );
	}

	public void print( Object o, int colspan ) throws IOException {
		print( o );
		printNull( colspan - 1 );
	}

	public void print( String s ) throws IOException {
		if( s == null ) {
			printNull();
			return;
		}

		int length = 0;
		try {
			length = lengths[index];
			length -= ( isByteLength ? s.getBytes().length : s.length() );
			if( length < 0 )
				throw new IOException( "value's length is too long("+ (isByteLength ? s.getBytes().length : s.length()) +")." );
		} catch( ArrayIndexOutOfBoundsException arrEx ) {
			throw new IOException( "cannot find fixed length.("+ index +")" );
		}

		if( length == 0 )
			out.print( s );
		else {
			StringBuffer sbuf = new StringBuffer();
			for( int i = 0; i < length; i++ )
				sbuf.append( ' ' );

			if( leftAligns == null || index >= leftAligns.length || leftAligns[index] ) {
				out.print( s );
				out.print( sbuf );
			} else {
				out.print( sbuf );
				out.print( s );
			}
		}

		index++;
	}

	public void print( String s, int colspan ) throws IOException {
		print( s );
		printNull( colspan - 1 );
	}

	public void print( String... ss ) throws IOException {
		if( ss.length > 0 ) {
			for( int i = 1; i < ss.length; i++ )
				print( ss[i] );
		}
	}

	public void println() throws IOException {
		if( index < lengths.length )
			printNull( lengths.length - index );
		if( lineSeparator == null )
			out.println();
		else
			out.print( lineSeparator );

		index = 0;
		linenum++;
	}

	public void println( String... ss ) throws IOException {
		print( ss );
		println();
	}

	public void printNull() throws IOException {
		printNull( 1 );
	}

	public void printNull( int nullcnt ) throws IOException {
		int length = 0;
		try {
			for( int i = 0; i < nullcnt; i++ )
				length += lengths[index++];
		} catch( ArrayIndexOutOfBoundsException arrEx ) {
			throw new IOException( "cannot find fixed length.("+ index +")" );
		}

		StringBuffer sbuf = new StringBuffer(length);
		for( int i = 0; i < length; i++ )
			sbuf.append( ' ' );

		out.print( sbuf );
	}

	public void setDataType( char dataType ) {
		this.dataType = dataType;
	}

	public String getFileType() {
		// TODO Auto-generated method stub
		return null;
	}
}
