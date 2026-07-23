/*
 *	File Name:	LineReader.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;

/**
 *
 */
public class LineReader {
	int linenum;
	boolean b_isEOF;
	LineReader.Line savedLine;
	BufferedReader reader;

	public LineReader( InputStream inputStream ) throws IOException {
		this.linenum = 0;
		this.b_isEOF = false;
		this.savedLine = null;
		this.reader = new BufferedReader( new java.io.InputStreamReader(inputStream) );
	}

	public LineReader( InputStream inputStream, String charsetName ) throws IOException {
		this.linenum = 0;
		this.b_isEOF = false;
		this.savedLine = null;
		this.reader = new BufferedReader( new java.io.InputStreamReader(inputStream, charsetName) );
	}

	public void close() {
		try { reader.close(); } catch( IOException ioEx ) {}
	}

	public int getLineNumber() {
		return linenum;
	}

	public boolean isEOF() {
		return b_isEOF;
	}

	public LineReader.Line readLine() throws IOException {
		if( savedLine != null ) {
			try {
				return savedLine;
			} finally {
				savedLine = null;
			}
		}

		linenum++;
		String buffer = reader.readLine();
		if( b_isEOF = (buffer == null) ) return null;

		int idx = 0;
		try {
			while( buffer.charAt(idx) == '\t' || buffer.charAt(idx) == ' ' )
				idx++;
			if( buffer.charAt(idx) == '#' ) return readLine();
		} catch( StringIndexOutOfBoundsException idxEx ) {
			return readLine();
		}

		return new LineReader.Line( linenum, buffer );
	}

	public void rollback( LineReader.Line line ) {
		String buffer = line.buffer;
		try {
			int idx = 0;
			while( buffer.charAt(idx) == '\t' || buffer.charAt(idx) == ' ' )
				idx++;
			if( buffer.charAt(idx) != '#' ) this.savedLine = line;
		} catch( StringIndexOutOfBoundsException idxEx ) {}
	}

	/**
	 *
	 */
	public static class Line {
		public int linenum;
		public String buffer;

		public Line( int linenum, String buffer ) {
			this.linenum = linenum;
			this.buffer = buffer;
		}

		public IOException throwIOException( String message ) throws IOException {
			throw new IOException( message +": line "+ linenum +" : '"+ buffer +"'" );
		}

		public String toString() {
			return linenum +" : '"+ buffer +"'";
		}
	}
}
