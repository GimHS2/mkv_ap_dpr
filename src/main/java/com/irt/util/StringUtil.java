/*
 *	File Name:	StringUtil.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.0c	create
 *
**/

package com.irt.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class StringUtil {

	private final static String DEFAULT_PLACEHOLDER_PTN_OPEN = "${";
	private final static String DEFAULT_PLACEHOLDER_PTN_CLOSE = "}";

	/** A table of hex digits */
	private static final char[] hexDigit = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	/** if true then return like '\ ' */
	public final static int OPT_ESCAPE_SPACE = 0x0001;

	/** if true then return like '\t' */
	public final static int OPT_ESCAPE_TAB = 0x0010;

	/** if true then return like '/u0000' */
	public final static int OPT_ESCAPE_UNICODE = 0x0100;

	/** if true then return like '\=' */
	public final static int OPT_ESCAPE_EQUAL = 0x1000;

	// ContainsNone
	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Checks that the CharSequence does not contain certain characters.
	 * </p>
	 *
	 * <p>
	 * A {@code null} CharSequence will return {@code true}.
	 * A {@code null} invalid character array will return {@code true}.
	 * An empty CharSequence (length()=0) always returns true.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.containsNone(null, *)       = true
	 * StringUtils.containsNone(*, null)       = true
	 * StringUtils.containsNone("", *)         = true
	 * StringUtils.containsNone("ab", '')      = true
	 * StringUtils.containsNone("abab", 'xyz') = true
	 * StringUtils.containsNone("ab1", 'xyz')  = true
	 * StringUtils.containsNone("abz", 'xyz')  = false
	 * </pre>
	 *
	 * @param cs
	 *            the CharSequence to check, may be null
	 * @param searchChars
	 *            an array of invalid chars, may be null
	 * @return true if it contains none of the invalid chars, or is null
	 * @since 2.0
	 * @since 3.0 Changed signature from containsNone(String, char[]) to containsNone(CharSequence, char...)
	 */
	public static boolean containsNone( final CharSequence cs, final char... searchChars ) {
		if( cs == null || searchChars == null ) {
			return true;
		}
		final int csLen = cs.length();
		final int csLast = csLen - 1;
		final int searchLen = searchChars.length;
		final int searchLast = searchLen - 1;
		for( int i = 0; i < csLen; i++ ) {
			final char ch = cs.charAt(i);
			for( int j = 0; j < searchLen; j++ ) {
				if( searchChars[j] == ch ) {
					if( Character.isHighSurrogate(ch) ) {
						if( j == searchLast ) {
							// missing low surrogate, fine, like String.indexOf(String)
							return false;
						}
						if( i < csLast && searchChars[j + 1] == cs.charAt(i + 1) ) {
							return false;
						}
					} else {
						// ch is in the Basic Multilingual Plane
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * @param input
	 *            : eg. contains ${variable}
	 * @param vars
	 *            : eg. map of {variable=somevalue, variable2=somevalue2}
	 * @return : evaluated string of input
	 */
	public static String evalPlaceholder( String input, Map<String, Object> vars ) {
		return evalPlaceholder(input, vars, DEFAULT_PLACEHOLDER_PTN_OPEN, DEFAULT_PLACEHOLDER_PTN_CLOSE);
	}

	/**
	 * @param template
	 *            : eg. contains %variable%
	 * @param vars
	 *            : eg. map of {variable=somevalue, variable2=somevalue2}
	 * @param open
	 *            : opening symbol
	 * @param close
	 *            : closing symbol
	 * @return : evaluated string of input
	 */
	public static String evalPlaceholder( String template, Map<String, Object> vars, String open, String close ) {
		if( open == null || open.length() == 0 ) {
			throw new UnsupportedOperationException("Please supply placeholder symbol for open.");
		} else if( close == null || close.length() == 0 ) {
			throw new UnsupportedOperationException("Please supply placeholder symbol for close.");
		}

		StringBuilder builder = new StringBuilder(template);

		if( vars != null && vars.size() > 0 ) {
			for( Entry<String, Object> entry : vars.entrySet() ) {

				int start;
				String pattern = open + entry.getKey() + close;
				String value = entry.getValue().toString();

				// Replace every occurence of ${key} with value
				while( ( start = builder.indexOf(pattern) ) != -1 ) {
					builder.replace(start, start + pattern.length(), value);
				}
			}
		}

		return builder.toString();
	}

	/**
	 * find ${} pattern and replace from {@param inputProps} values.
	 *
	 * @param inputProps
	 *            : contains ${} pattern
	 * @param vars
	 *            : eg. map of {variable=somevalue, variable2=somevalue2}
	 * @return : evaluated properties
	 */
	public static Properties evalPlaceholders( Properties inputProps, Map<String, Object> vars ) {
		Properties outputProps = new Properties(inputProps);
		for( Map.Entry entry : inputProps.entrySet() ) {
			Object obj = entry.getValue();
			if( obj instanceof String ) {
				String val = (String)obj;
				if( val.contains("${") ) {
					String evaluated = evalPlaceholder(val, vars);
					if( evaluated != null ) {
						outputProps.put(entry.getKey(), evaluated);
					}
				} else {
					outputProps.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return outputProps;
	}

	/**
	 * @param attributes
	 *            eg. width='100' type='formula'
	 * @param key
	 * @return
	 */
	public static String extractAttrValue( String attribute, String key ) {
		int idx = attribute.indexOf(key + "=");
		if( idx < 0 )
			return null;

		idx += key.length() + 1;
		if( idx < attribute.length() ) {
			char ch = attribute.charAt(idx);
			if( ch == '\'' || ch == '"' ) {
				int idx2 = attribute.indexOf(ch, ++idx);
				if( idx2 < 0 )
					return null;

				return attribute.substring(idx, idx2);
			}
		}

		return null;
	}

	/**
	 * @param filename
	 * @return
	 */
	public static String getFileExtension( String filename ) {
		if( filename == null ) {
			return null;
		}
		int lastUnixPos = filename.lastIndexOf('/');
		int lastWindowsPos = filename.lastIndexOf('\\');
		int indexOfLastSeparator = Math.max(lastUnixPos, lastWindowsPos);
		int extensionPos = filename.lastIndexOf('.');
		int lastSeparator = indexOfLastSeparator;
		int indexOfExtension = lastSeparator > extensionPos ? -1 : extensionPos;
		int index = indexOfExtension;
		if( index == -1 ) {
			return "";
		} else {
			return filename.substring(index + 1);
		}
	}

	/**
	 * replace backward slash to forward slash
	 */
	public static String getPathWithFwdSlash( String pathString ) {
		if( pathString != null ) {
			return pathString.replace("\\", "/");
		}
		return null;
	}

	/*
	 * from: java.util.Properties
	 * Converts unicodes to encoded &#92;uxxxx and escapes
	 * special characters with a preceding slash
	 */
	/**
	 * @return string return by escape options parameters(OPT_ESCAPE_SPACE)
	 */
	public static String getStringConverted( String theString, int option ) {
		boolean escapeSpace = ( option & OPT_ESCAPE_SPACE ) > 0;
		boolean escapeTab = ( option & OPT_ESCAPE_TAB ) > 0;
		boolean escapeUnicode = ( option & OPT_ESCAPE_UNICODE ) > 0;
		boolean escapeEqual = ( option & OPT_ESCAPE_EQUAL ) > 0;

		int len = theString.length();
		int bufLen = len * 2;
		if( bufLen < 0 ) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuffer outBuffer = new StringBuffer(bufLen);

		for( int x = 0; x < len; x++ ) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if( ( aChar > 61 ) && ( aChar < 127 ) ) {
				if( aChar == '\\' ) {
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch( aChar ) {
			case ' ':
				if( x == 0 || escapeSpace )
					outBuffer.append('\\');
				outBuffer.append(' ');
				break;
			case '\t':
				if( escapeTab ) {
					outBuffer.append('\\');
					outBuffer.append('t');
				}
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			case '=':
				if( escapeEqual ) {
					outBuffer.append('\\');
				}
				outBuffer.append('=');
				break;
			case ':': // Fall through
			case '#': // Fall through
			case '!':
				outBuffer.append('\\');
				outBuffer.append(aChar);
				break;
			default:
				if( ( ( aChar < 0x0020 ) || ( aChar > 0x007e ) ) & escapeUnicode ) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex(( aChar >> 12 ) & 0xF));
					outBuffer.append(toHex(( aChar >> 8 ) & 0xF));
					outBuffer.append(toHex(( aChar >> 4 ) & 0xF));
					outBuffer.append(toHex(aChar & 0xF));
				} else {
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	public static OutputStreamWriter getUnicodeableOutputStreamWriter( OutputStream os ) {
		try {
			return new OutputStreamWriter(os, "8859_1");
		} catch( UnsupportedEncodingException nothappen ) {
		}

		return null;
	}

	/**
	 * @return Unicode Raw String(eg "/u0023")
	 */
	public static String getUnicodeEncoded( String theString ) {
		return getStringConverted(theString, OPT_ESCAPE_UNICODE);
	}

	public static boolean hasWhitespace( final String testCode ) {
		if( testCode != null ) {
			for( int i = 0; i < testCode.length(); i++ ) {
				if( Character.isWhitespace(testCode.charAt(i)) ) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * check if str is null or empty
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNullOrEmpty( String str ) {
		return str == null || str.length() <= 0;
	}

	public static boolean isScientificNotation( String numberString ) {

		// Validate number
		try {
			new BigDecimal(numberString);
		} catch( NumberFormatException e ) {
			return false;
		}

		// Check for scientific notation
		return numberString.toUpperCase().contains("E");
	}

	/**
	 * be carefull if null then return false
	 * only checkes if str is empty.
	 *
	 * @param str
	 * @return
	 */
	public static boolean isStrictEmpty( String str ) {
		return str != null && str.length() <= 0;
	}

	/**
	 * only checks if str is null
	 *
	 * @param str
	 * @return
	 */
	public static boolean isStrictNull( String str ) {
		return str == null;
	}

	/**
	 *
	 * @param str
	 * @return "ISO-8859-1" for ISO Latin 1
	 */
	public static boolean isValidISOLatin1( String str ) {
		return Charset.forName("US-ASCII").newEncoder().canEncode(str);
	}

	/**
	 *
	 * @param str
	 * @return
	 */
	public static boolean notIsNullOrEmpty( String str ) {
		return !isNullOrEmpty(str);
	}

	/**
	 * <p>
	 * Replaces a String with another String inside a larger String,
	 * for the first {@code max} values of the search String,
	 * case sensitively/insensisitively based on {@code ignoreCase} value.
	 * </p>
	 *
	 * <p>
	 * A {@code null} reference passed to this method is a no-op.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *, *, false)         = null
	 * StringUtils.replace("", *, *, *, false)           = ""
	 * StringUtils.replace("any", null, *, *, false)     = "any"
	 * StringUtils.replace("any", *, null, *, false)     = "any"
	 * StringUtils.replace("any", "", *, *, false)       = "any"
	 * StringUtils.replace("any", *, *, 0, false)        = "any"
	 * StringUtils.replace("abaa", "a", null, -1, false) = "abaa"
	 * StringUtils.replace("abaa", "a", "", -1, false)   = "b"
	 * StringUtils.replace("abaa", "a", "z", 0, false)   = "abaa"
	 * StringUtils.replace("abaa", "A", "z", 1, false)   = "abaa"
	 * StringUtils.replace("abaa", "A", "z", 1, true)   = "zbaa"
	 * StringUtils.replace("abAa", "a", "z", 2, true)   = "zbza"
	 * StringUtils.replace("abAa", "a", "z", -1, true)  = "zbzz"
	 * </pre>
	 *
	 * @param text
	 *            text to search and replace in, may be null
	 * @param searchString
	 *            the String to search for (case insensitive), may be null
	 * @param replacement
	 *            the String to replace it with, may be null
	 * @param max
	 *            maximum number of values to replace, or {@code -1} if no maximum
	 * @param ignoreCase
	 *            if true replace is case insensitive, otherwise case sensitive
	 * @return the text with any replacements processed,
	 *         {@code null} if null String input
	 */
	static String replace( final String text, String searchString, final String replacement, int max, final boolean ignoreCase ) {
		if( ( text == null || text.length() <= 0 ) || ( searchString == null || searchString.length() <= 0 ) || replacement == null || max == 0 ) {
			return text;
		}
		String searchText = text;
		if( ignoreCase ) {
			searchText = text.toLowerCase();
			searchString = searchString.toLowerCase();
		}
		int start = 0;
		int end = searchText.indexOf(searchString, start);
		if( end == -1 ) {
			return text;
		}
		final int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = increase < 0 ? 0 : increase;
		increase *= max < 0 ? 16 : max > 64 ? 64 : max;
		final StringBuilder buf = new StringBuilder(text.length() + increase);
		while( end != -1 ) {
			buf.append(text, start, end).append(replacement);
			start = end + replLength;
			if( --max == 0 ) {
				break;
			}
			end = searchText.indexOf(searchString, start);
		}
		buf.append(text, start, text.length());
		return buf.toString();
	}

	/**
	 * Usage:
	 *
	 * <pre>
	 * String urlParams = strJoin("&paramKey", "=", new String[] { "bParamVal1", "bParamVal2" });
	 * </pre>
	 */
	public static String strJoin( String aFill, String sSep, String[] bArr ) {

		StringBuilder sbStr = new StringBuilder();
		for( int i = 0, il = bArr.length; i < il; i++ ) {
			sbStr.append(aFill);
			sbStr.append(sSep);
			sbStr.append(bArr[i]);
		}
		return sbStr.toString();
	}

	public static String strJoin( String[] aArr, char sSep ) {
		return strJoin(aArr, String.valueOf(sSep));
	}

	public static String strJoin( String[] aArr, String sSep ) {
		StringBuilder sbStr = new StringBuilder();
		for( int i = 0, il = aArr.length; i < il; i++ ) {
			if( i > 0 )
				sbStr.append(sSep);
			sbStr.append(aArr[i]);
		}
		return sbStr.toString();
	}

	/**
	 * Usage:
	 *
	 * <pre>
	 * String urlParams = strJoin(new String[] { "&aParamKey1", "&aParamKey2" }, "=", new String[] { "bParamVal1", "bParamVal2" });
	 * </pre>
	 */
	public static String strJoin( String[] aArr, String sSep, String[] bArr ) {
		if( aArr.length != bArr.length )
			throw new IllegalStateException("array size not match:" + java.util.Arrays.asList(aArr) + " vs " + java.util.Arrays.asList(bArr));

		StringBuilder sbStr = new StringBuilder();
		for( int i = 0, il = aArr.length; i < il; i++ ) {
			sbStr.append(aArr[i]);
			sbStr.append(sSep);
			sbStr.append(bArr[i]);
		}
		return sbStr.toString();
	}

	/**
	 * Convert a nibble to a hex character
	 *
	 * @param nibble
	 *            the nibble to convert.
	 */
	private static char toHex( int nibble ) {
		return hexDigit[( nibble & 0xF )];
	}

}
