/*
 *	File Name:	INFFileException.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/12/31		2.2.0	create(copy com.irt.kredi.EDIFileException)
 *
**/

package com.irt.dpr.tools;

/**
 *
 */
public class INFFileException extends Exception {
	public final static int ERROR						= 0x01;
	public final static int WARNING						= 0x02;

	private String line;
	private int linenum;
	private int level;

	public INFFileException( int linenum, String line, String message ) {
		this( ERROR, linenum, line, message );
	}

	public INFFileException( int linenum, String line, Exception exception ) {
		this( ERROR, linenum, line, exception.getMessage() );
	}

	public INFFileException( int level, int linenum, String line, String message ) {
		super( "Line "+ linenum +": "+ message );
		this.level = level;
		this.line = line;
		this.linenum = linenum;
	}

	public INFFileException( int level, int linenum, String line, Exception exception ) {
		this( level, linenum, line, exception.getMessage() );
	}

	public int getLevel() {
		return level;
	}

	public int getLineNumber() {
		return linenum;
	}

	public String getLineString() {
		return line;
	}
}
