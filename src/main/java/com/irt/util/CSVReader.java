/*
 *	File Name:	CSVReader.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	java.nio.charset.CharacterCodingException 발생시 line을 빈문자로 초기화
 *										DEFAULT_DELIM, DEFAULT_ENCAP: private -> public
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										sun.io.MalformedInputException -> java.nio.charset.CharacterCodingException
 *	stghr12		2007/10/31		2.1.2	IllegalArgumentException message 변경: invalid -> illegal
 *	stghr12		2007/04/30		2.1.1	readNext(keys): 빈문자는 null로 처리
 *										split() 추가
 *	stghr12		2006/12/01		2.1.0	com.irt.data.DataReader 사용
 *										CSVReader 생성자 변경.
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/29		1.0.0	version 관리
 *	stghr12		2002/07/04				create
 *
**/

package com.irt.util;

import com.irt.data.DataException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * CSV 형식 Reader.
 */
public class CSVReader implements com.irt.data.DataReader {
	public final static char DEFAULT_DELIM				= ',';
	public final static char DEFAULT_ENCAP				= '"';

	private char delim, encap;
	private BufferedReader reader;
	private boolean trim;

	private int linenum;
	private String line;
	private String[] lines;

	/**
	 * default delimiter, default encapsulator 사용한 CSVReader 생성.
	 */
	public CSVReader( Reader reader ) {
		this( reader, DEFAULT_DELIM, DEFAULT_ENCAP );
	}

	/**
	 * 주어진 delimiter를 사용한 CSVReader 생성.(encapsulator 없음)
	 */
	public CSVReader( Reader reader, char delim ) {
		this( reader, delim, delim );
	}

	/**
	 * 주어진 delimiter, encapsulator를 사용한 CSVReader 생성.
	 */
	public CSVReader( Reader reader, char delim, char encap ) throws IllegalArgumentException {
		if( reader == null )
			throw new IllegalArgumentException( "reader cannot be null" );

		if( reader instanceof BufferedReader )
			this.reader = (BufferedReader)reader;
		else
			this.reader = new BufferedReader( reader );
		this.delim = delim;
		this.encap = encap;
		this.trim = false;
		this.linenum = 0;
		this.line = null;
		this.lines = null;
	}

	public void close() throws IOException {
		if( reader != null ) {
			reader.close();
			reader = null;
		}
	}

	public int getLineNumber() {
		return linenum;
	}

	public String[] getLines() {
		return lines;
	}

	public String getLineString() {
		return line;
	}

	public boolean isBlankLine() {
		if( lines == null ) return true;

		for( int i = 0; i < lines.length; i++ ) {
			if( lines[i].length() > 0 )
				return false;
		}

		return true;
	}

	public boolean isEOF() {
		return( reader == null );
	}

	public Map<String, Object> readNext( String[] keys ) throws DataException, IOException {
		String[] lines = readNext();
		if( lines == null ) return null;

		Map<String, Object> map = new java.util.HashMap<String, Object>( keys.length );
		for( int i = 0; i < keys.length && i < lines.length; i++ ) {
			if( lines[i] != null && lines[i].length() > 0 )
				map.put( keys[i], lines[i] );
		}

		return map;
	}

	public String[] readNext() throws DataException, IOException {
		if( reader == null ) return null;

		try {
			line = reader.readLine();
		} catch( java.nio.charset.CharacterCodingException codingEx ) {
			line = "";
			throw new DataException( DataException.ERR_IO_INCORRECT_ENCODING, codingEx.getMessage() );
		}
		if( line == null ) {
			reader.close();
			reader = null;
			return ( lines = null );
		}
		++linenum;

		lines = split( line, delim, encap, trim );
		if( lines == null )
			throw new DataException( DataException.ERR_IO_INVALID_CSV_LINE, "illegal CSV file format." );

		return lines;
	}

	public void setTrim( boolean trim ) {
		this.trim = trim;
	}

	public static String[] split( String line ) {
		return split( line, DEFAULT_DELIM, DEFAULT_ENCAP, false );
	}

	public static String[] split( String line, boolean trim ) {
		return split( line, DEFAULT_DELIM, DEFAULT_ENCAP, trim );
	}

	public static String[] split( String line, char delim, char encap ) {
		return split( line, delim, encap, false );
	}

	public static String[] split( String line, char delim, char encap, boolean trim ) {
		int idx0 = 0;
		List<String> list = new java.util.ArrayList<String>();
		do {
			String value;
			if( idx0 == line.length() || line.charAt(idx0) == delim ) {
				value = "";
				idx0++;
			} else if( line.charAt(idx0) == encap ) {
				StringBuffer sbuf = new StringBuffer();

				++idx0;
				do {
					int idx1 = line.indexOf( encap, idx0 );
					if( idx1 < 0 ) return null;

					if( ++idx1 == line.length() || line.charAt(idx1) == delim ) {
						sbuf.append( line.substring(idx0, idx1 - 1) );
						idx0 = idx1 + 1;
						break;
					} else {
						sbuf.append( line.substring(idx0, idx1) );
						idx0 = ( line.charAt(idx1) == encap ? idx1 + 1 : idx1 );
					}
				} while( true );
				value = sbuf.toString();
			} else {
				int idx1 = line.indexOf( delim, idx0 );
				if( idx1 < 0 ) {
					value = line.substring( idx0 );
					idx0 = line.length() + 1;
				} else {
					value = line.substring( idx0, idx1 );
					idx0 = idx1 + 1;
				}
			}
			list.add( trim ? value.trim() : value );
		} while( idx0 <= line.length() );

		return list.toArray( new String[list.size()] );
	}

	public String getFileType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isBinary() {
		// TODO Auto-generated method stub
		return false;
	}
}
