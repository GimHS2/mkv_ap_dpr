/*
 *	File Name:	FLVReader.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	java.nio.charset.CharacterCodingException 발생시 line을 빈문자로 초기화
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										sun.io.MalformedInputException -> java.nio.charset.CharacterCodingException
 *	stghr12		2007/10/31		2.1.2	IllegalArgumentException message 변경: invalid -> illegal
 *	stghr12		2007/04/30		2.1.1	readNext(keys), readNext(keys, lengths): 빈문자는 null로 처리
 *										split( line, keys, lengths, isByteLength ): 빈문자는 null로 처리
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.util;

import com.irt.data.DataException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * 고정장 형식 Reader.
 */
public class FLVReader implements com.irt.data.DataReader {
	private int[] lengths;
	private boolean isByteLength;
	private BufferedReader reader;

	private int linenum;
	private String line;
	private String[] lines;

	/**
	 * 고정길이가 정해져 있지 않은 FLVReader 생성.
	 * byte단위로 길이 계산.
	 */
	public FLVReader( Reader reader ) {
		this( reader, (int[])null, true );
	}

	/**
	 * 고정길이가 정해져 있지 않은 FLVReader 생성.
	 * @param isByteLength byte단위로 길이를 계산할 것인지 여부. false일 경우에는 charater단위로 계산.
	 */
	public FLVReader( Reader reader, boolean isByteLength ) {
		this( reader, (int[])null, isByteLength );
	}

	/**
	 * 고정길이가 lengths인 FLVReader 생성.
	 * byte단위로 길이 계산.
	 */
	public FLVReader( Reader reader, int[] lengths ) {
		this( reader, lengths, true );
	}

	/**
	 * 고정길이가 lengths인 FLVReader 생성.
	 * @param isByteLength byte단위로 길이를 계산할 것인지 여부. false일 경우에는 charater단위로 계산.
	 */
	public FLVReader( Reader reader, int[] lengths, boolean isByteLength ) {
		if( reader == null )
			throw new IllegalArgumentException( "reader cannot be null" );

		if( reader instanceof BufferedReader )
			this.reader = (BufferedReader)reader;
		else
			this.reader = new BufferedReader( reader );
		this.lengths = lengths;
		this.isByteLength = isByteLength;
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

	private String readLine() throws DataException, IOException {
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
			lines = null;

			return null;
		}
		++linenum;

		return line;
	}

	public String[] readNext() throws DataException, IOException {
		if( lengths != null ) return readNext( lengths );
		if( readLine() == null ) return null;

		return ( lines = new String[] { line } );
	}

	public String[] readNext( int[] lengths ) throws DataException, IOException {
		if( readLine() == null ) return null;

		lines = split( line, lengths, isByteLength );
		if( lines == null )
			throw new DataException( DataException.ERR_IO_INVALID_FLV_LINE, "illegal FLV file format." );

		return lines;
	}

	public Map<String, Object> readNext( String[] keys ) throws DataException, IOException {
		if( readNext() == null ) return null;

		Map<String, Object> map = new java.util.HashMap<String, Object>( keys.length );
		for( int i = 0; i < keys.length && i < lines.length; i++ )
			if( lines[i] != null && lines[i].length() > 0 )
				map.put( keys[i], lines[i] );

		return map;
	}

	public Map<String, Object> readNext( String[] keys, int[] lengths ) throws DataException, IOException {
		if( readNext(lengths) == null ) return null;

		Map<String, Object> map = new java.util.HashMap<String, Object>( keys.length );
		for( int i = 0; i < keys.length && i < lines.length; i++ )
			if( lines[i] != null && lines[i].length() > 0 )
				map.put( keys[i], lines[i] );

		return map;
	}

	public void setLengths( int[] lengths ) {
		this.lengths = lengths;
	}

	public void setTrim( boolean trim ) {
	}

	public String[] split( int[] lengths ) {
		return split( getLineString(), lengths, isByteLength );
	}

	public Map<String, Object> split( String[] keys, int[] lengths ) {
		return split( getLineString(), keys, lengths, isByteLength );
	}

	/**
	 * @param isByteLength byte단위로 길이를 계산할 것인지 여부. false일 경우에는 charater단위로 계산.
	 */
	public static String[] split( String line, int lengths[], boolean isByteLength ) {
		if( line == null ) return null;

		try {
			int idx = 0;
			String[] lines = new String[ lengths.length ];
			if( isByteLength ) {
				byte[] bytes = line.getBytes();
				for( int i = 0; i < lengths.length; i++ ) {
					lines[i] = (new String( bytes, idx, lengths[i] )).trim();
					idx += lengths[i];
				}

				return ( bytes.length == idx ? lines : null );
			} else {
				for( int i = 0; i < lengths.length; i++ ) {
					lines[i] = line.substring( idx, idx + lengths[i] ).trim();
					idx += lengths[i];
				}

				return ( line.length() == idx ? lines : null );
			}
		} catch( StringIndexOutOfBoundsException idxEx ) {
			return null;
		}
	}

	/**
	 * @param isByteLength byte단위로 길이를 계산할 것인지 여부. false일 경우에는 charater단위로 계산.
	 */
	public static Map<String, Object> split( String line, String[] keys, int[] lengths, boolean isByteLength ) {
		String[] lines = split( line, lengths, isByteLength );
		if( lines == null ) return null;

		Map<String, Object> map = new java.util.HashMap<String, Object>( keys.length );
		for( int i = 0; i < keys.length && i < lines.length; i++ )
			if( lines[i] != null && lines[i].length() > 0 )
				map.put( keys[i], lines[i] );

		return map;
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
