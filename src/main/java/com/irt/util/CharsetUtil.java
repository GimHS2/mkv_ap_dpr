/*
 *	File Name:	CharsetUtil.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Formatter:	eclipse
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/04/24		2.2.0c	create
 *
**/

package com.irt.util;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.tika.parser.txt.CharsetMatch;

/**
 * * require tika dependency
 *
 * <pre>
		<dependency>
			<groupId>org.apache.tika</groupId>
			<artifactId>tika-parsers</artifactId>
			<version>1.17</version>
			<scope>runtime</scope>
			<exclusions>
				<exclusion>
					<groupId>*</groupId>
					<artifactId>*</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
 * </pre>
 *
 */
public class CharsetUtil {

	/**
	 *
	 * @param content
	 *            : the cotnent should be enough to determine the encoding...
	 * @param minConfidence
	 *            : 0-100 if detected match is less than this value then return null.
	 * @return
	 */
	public static String detectEncoding( byte[] content, int minConfidence ) {
		org.apache.tika.parser.txt.CharsetDetector tika = new org.apache.tika.parser.txt.CharsetDetector();

		tika.setText(content);
		CharsetMatch match = tika.detect();
		if( match != null ) {
			if( match.getConfidence() > minConfidence ) {
				return match.getName();
			}
		}

		return null;
	}

	/**
	 *
	 * @param content
	 *            : the content should be enough to determine the encoding...
	 * @param minConfidence
	 *            : 0-100 if detected match is less than this value then return null.
	 * @return
	 * @throws IOException
	 */
	public static String detectEncoding( BufferedInputStream content, int minConfidence ) throws IOException {
		org.apache.tika.parser.txt.CharsetDetector tika = new org.apache.tika.parser.txt.CharsetDetector();

		tika.setText(content);
		CharsetMatch match = tika.detect();
		if( match != null ) {
			if( match.getConfidence() > minConfidence ) {
				return match.getName();
			}
		}

		return null;
	}
}
