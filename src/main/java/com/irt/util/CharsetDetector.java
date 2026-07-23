/*
 *	File Name:	CharsetDetector.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/12/31		2.2.0	create
 *
**/

package com.irt.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Simple Charset Detector.
 *
 * 인식률은 낮을 수 있으나 간단히 EUC-KR과 UTF-8 구분가능. 사용은 아래와 같이 수행함.
 *
 * <pre>
 * Charset charset = new CharsetDetector().detectCharset(file, new String[] { "EUC-KR", "UTF-8" });
 * </pre>
 */
public class CharsetDetector {

	/**
	 * @param file
	 * @param charsetsByPriority
	 *            : first defined is higher priority. so below is usually used.
	 *
	 *            <pre>
	 *  new String[]{ "EUC-KR" "UTF-8" }
	 *            </pre>
	 *
	 * @return
	 */
	public Charset detectCharset( File file, String[] charsetsByPriority ) {

		Charset charset = null;

		for( String charsetName : charsetsByPriority ) {
			charset = detectCharset(file, Charset.forName(charsetName));
			if( charset != null ) {
				break;
			}
		}

		return charset;
	}

	private Charset detectCharset( File file, Charset charset ) {
		try {
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

			CharsetDecoder decoder = charset.newDecoder();
			decoder.reset();

			byte[] buffer = new byte[512];
			boolean identified = false;
			while( ( input.read(buffer) != -1 ) && ( !identified ) ) {
				identified = identify(buffer, decoder);

			}

			buffer = null;
			input.close();

			if( identified ) {
				return charset;
			} else {
				return null;
			}

		} catch( Exception e ) {
			return null;
		}
	}

	private boolean identify( byte[] bytes, CharsetDecoder decoder ) {
		try {
			decoder.decode(ByteBuffer.wrap(bytes));
		} catch( CharacterCodingException e ) {
			return false;
		}
		return true;
	}

	public static void main( String[] args ) {
		File f = new File("example.txt");

		String[] charsetsToBeTested = { "EUC-KR", "UTF-8", "windows-1253", "ISO-8859-7" };

		CharsetDetector cd = new CharsetDetector();
		Charset charset = cd.detectCharset(f, charsetsToBeTested);

		if( charset != null ) {
			try {
				InputStreamReader reader = new InputStreamReader(new FileInputStream(f), charset);
				int c = 0;
				while( ( c = reader.read() ) != -1 ) {
					System.out.print((char)c);
				}
				reader.close();
			} catch( FileNotFoundException fnfe ) {
				fnfe.printStackTrace();
			} catch( IOException ioe ) {
				ioe.printStackTrace();
			}

		} else {
			System.out.println("Unrecognized charset.");
		}
	}
}
