/*
 *	File Name:	IOUtil.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.0	create
 *
**/

package com.irt.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class IOUtil {

	public static byte[] readFully( InputStream is ) throws IOException {
		return readFully( is, -1, true );
	}

	public static byte[] readFully( InputStream is, int length, boolean readAll )
			throws IOException {
		byte[] output = {};
		if( length == -1 )
			length = Integer.MAX_VALUE;
		int pos = 0;
		while( pos < length ) {
			int bytesToRead;
			if( pos >= output.length ) { // Only expand when there's no room
				bytesToRead = Math.min( length - pos, output.length + 1024 );
				if( output.length < pos + bytesToRead ) {
					output = java.util.Arrays.copyOf( output, pos + bytesToRead );
				}
			} else {
				bytesToRead = output.length - pos;
			}
			int cc = is.read( output, pos, bytesToRead );
			if( cc < 0 ) {
				if( readAll && length != Integer.MAX_VALUE ) {
					throw new EOFException( "Detect premature EOF" );
				} else {
					if( output.length != pos ) {
						output = java.util.Arrays.copyOf( output, pos );
					}
					break;
				}
			}
			pos += cc;
		}
		return output;
	}

}
