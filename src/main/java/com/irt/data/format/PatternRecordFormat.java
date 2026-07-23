/*
 *	File Name:	PatternRecordFormat.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	방식변경: PatternRecordFormat 대신 RecordFormat return
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.2	MessageRecordFormat 변경사항 적용: messageKey에 Pattern 사용 가능
 *	stghr12		2007/07/31		2.1.1	formatMap: WeakHashMap() 사용, ConstantRecordFormat는 저장하지 않음
 *	stghr12		2006/12/01		2.1.0	FunctionRecordFormat 추가
 *										FieldRecordFormat.isByteLength 관련 처리 추가
 *	stghr12		2006/09/09		2.0.1	오류수정('%%'문제, '$H{'문제, '$S{'문제)
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data.format;

import com.irt.util.MessageHandler;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * <i>pattern</i> := <i>pattern_s</i>[<i>pattern</i>]<br>
 * <i>pattern_s</i> := <i>fld_pattern</i> | <i>msg_pattern</i> | <i>string</i> | <i>func_pattern</i><br>
 * <i>string</i> := STRING<br>
 * <p>
 * <i>msg_pattern</i> := <b>%{</b><i>msgkey</i>[,<i>msg_params</i>]<b>}</b><br>
 * <i>msg_params</i> := <i>param_pattern</i>[<b>,</b><i>msg_params</i>]<br>
 * <i>param_pattern</i> := <i>pattern</i><br>
 * <p>
 * <i>fld_pattern</i> := <b>$</b>[<b>H</b>|<b>S</b>]<b>{</b>[<i>prefixValue_pattern</i><b>:</b>]<i>fld_pattern_s</i>[<b>,</b><i>nullValue_pattern</i>][<b>;</b><i>suffixValue_pattern</i>]<b>}</b><br>
 * <i>fld_pattern_s</i> := <i>prefixKey</i><b>@</b><i>fieldKey</i>
 *			| <i>fieldKey</i><b>#</b><i>numberFormatKey</i>
 *			| <i>fieldKey</i><b>~</b><i>beginIndex</i><b>~</b><i>endIndex</i>
 *			| <i>fieldKey</i><b>~</b><i>maxlength</i><br>
 * <i>prefixKey</i>, <i>fieldKey</i>, <i>numberFormatKey</i> := STRING<br>
 * <i>beginIndex</i>, <i>endIndex</i>, <i>maxlength</i> := NUMBER<br>
 * <i>prefixValue_pattern</i>, <i>nullValue_pattern</i>, <i>suffixValue_pattern</i> := <i>pattern</i><br>
 * <p>
 * <i>func_pattern</i> := <b>$f{</b><i>func_name</i><b>(</b><i>params</i><b>)}</b><br>
 * <i>func_name</i> := STRING<br>
 * <i>params</i> := <i>pattern</i>[<b>,</b><i>param</i>]<br>
 * <i>param</i> := <i>pattern</i>
 */
public class PatternRecordFormat implements RecordFormat {
	private static Map<String, PatternRecordFormat> formatMap
			= java.util.Collections.synchronizedMap( new java.util.WeakHashMap<String, PatternRecordFormat>(2000) );

	String pattern;
	RecordFormat format;

	PatternRecordFormat( String pattern, RecordFormat format ) {
		this.pattern = pattern;
		this.format = format;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		format.addFieldKeyToSet( fieldKeySet );
	}

	private static RecordFormat createInstance( String pattern ) {
		int idx_s = 0;
		List<Object> tokenList = null;
		if( isEncodePattern(pattern) ) {
			idx_s = getIndexOfCloseBrace( pattern, 1 );
			if( idx_s < 0 )
				return new ConstantRecordFormat( pattern );
			else if( ++idx_s == pattern.length() ) {
				if( pattern.charAt(0) == '$' )
					return ( pattern.startsWith("$f{") ? createInstanceFunction(pattern) : createInstanceField(pattern) );
				else
					return createInstanceMessage( pattern );
			} else {
				tokenList = new java.util.ArrayList<Object>();
				tokenList.add( getInstance(pattern.substring(0, idx_s)) );
			}
		}

		StringBuffer sbuf = new StringBuffer();
		for( int idx = idx_s; idx < pattern.length(); ) {
			int idx_v = idx;
			boolean variable = false;
			if( pattern.charAt(idx) == '$' ) {
				try {
					switch( pattern.charAt(++idx) ) {
					case 'H':
					case 'S':
					case 'f':
						if( pattern.charAt(++idx) != '{' ) break;
					case '{':
						variable = true;
						break;
					case '$':
						sbuf.append( pattern.substring(idx_s, idx) );
						idx_s = ++idx;
					}
				} catch( IndexOutOfBoundsException indexBx ) {
					break;
				}
			} else if( pattern.charAt(idx) == '%' ) {
				try {
					switch( pattern.charAt(++idx) ) {
					case '{':
						variable = true;
						break;
					case '%':
						sbuf.append( pattern.substring(idx_s, idx) );
						idx_s = ++idx;
					}
				} catch( IndexOutOfBoundsException indexBx ) {
					break;
				}
			} else
				++idx;

			if( variable ) {
				int idx_n = getIndexOfCloseBrace( pattern, idx );
				if( idx_n < 0 ) break;

				if( tokenList == null ) tokenList = new java.util.ArrayList<Object>();
				if( idx_s != idx_v ) {
					if( sbuf.length() > 0 ) {
						tokenList.add( sbuf.toString() + pattern.substring(idx_s, idx_v) );
						sbuf = new StringBuffer();
					} else
						tokenList.add( pattern.substring(idx_s, idx_v) );
				} else if( sbuf.length() > 0 ) {
					tokenList.add( sbuf.toString() );
					sbuf = new StringBuffer();
				}
				tokenList.add( getInstance(pattern.substring(idx_v, ++idx_n)) );
				idx = idx_s = idx_n;
			}
		}

		if( idx_s == 0 )
			return new ConstantRecordFormat( pattern );
		else if( tokenList == null ) {
			sbuf.append( pattern.substring(idx_s) );
			return new ConstantRecordFormat( sbuf.toString() );
		} else {
			sbuf.append( pattern.substring(idx_s) );
			if( sbuf.length() > 0 )
				tokenList.add( sbuf.toString() );

			return new ListRecordFormat( tokenList.toArray() );
		}
	}

	private static RecordFormat createInstanceFunction( String pattern ) throws IllegalArgumentException {
		if( !pattern.endsWith(")}") ) throw new IllegalArgumentException( pattern );

		int idx = pattern.indexOf( '(' );
		if( idx < 0 ) throw new IllegalArgumentException( pattern );

		String functionName = pattern.substring( 3, idx );

		int p_idx = ++idx;
		List<String> paramList = new java.util.ArrayList<String>();
		while( idx < pattern.length() - 2 ) {
			switch( pattern.charAt(idx) ) {
			case '$':
				switch( pattern.charAt(++idx) ) {
				case 'H':
				case 'S':
				case 'f':
					if( pattern.charAt(++idx) != '{' ) break;
				case '{':
					idx = getIndexOfCloseBrace( pattern, idx );
				case '$':
					++idx;
				}
				break;
			case '%':
				switch( pattern.charAt(++idx) ) {
				case '{':
					idx = getIndexOfCloseBrace( pattern, idx );
				case '%':
					++idx;
				}
				break;
			case ',':
				paramList.add( pattern.substring(p_idx, idx) );
				p_idx = ++idx;
				break;
			default:
				++idx;
			}
		}
		paramList.add( pattern.substring(p_idx, pattern.length() - 2) );

		Object[] params = paramList.toArray();
		for( int p = 0; p < params.length; p++ ) {
			String param = (String)params[p];
			if( param.length() == 0 )
				params[p] = null;
			else if( param.indexOf('$') >= 0 || param.indexOf('%') >= 0 ) {
				params[p] = getInstance( param );
				if( params[p] instanceof ConstantRecordFormat )
					params[p] = ((ConstantRecordFormat)params[p]).getString();
			}
		}
		if( params.length == 1 && params[0] == null ) params = null;

		return FunctionRecordFormat.getInstance( functionName, params );
	}

	private static RecordFormat createInstanceField( String pattern ) throws IllegalArgumentException {
		int idx_prefix_e = -1, idx_null_b = -1, idx_suffix_b = -1;
		int idx_prefixkey_e = -1, idx_number_b = -1, idx_length_b = -1, idx_length_e = -1;

		int encodeType = pattern.charAt(1);
		boolean invalid = false;
		for( int idx = ( encodeType == '{' ? 2 : 3 ); idx < pattern.length() - 1 && idx_suffix_b < 0; ) {
			switch( pattern.charAt(idx) ) {
			case '$':
				switch( pattern.charAt(++idx) ) {
				case 'H':
				case 'S':
				case 'f':
					if( pattern.charAt(++idx) != '{' ) break;
				case '{':
					idx = getIndexOfCloseBrace( pattern, idx );
				case '$':
					++idx;
				}
				break;
			case '%':
				switch( pattern.charAt(++idx) ) {
				case '{':
					idx = getIndexOfCloseBrace( pattern, idx );
				case '%':
					++idx;
				}
				break;
			case ':':
				if( idx_prefix_e > 0 )
					throw new IllegalArgumentException( pattern );

				invalid = false;
				idx_prefix_e = idx++;
				idx_prefixkey_e = idx_number_b = idx_length_b = idx_length_e = idx_null_b = -1;
				break;
			case '@':
				if( idx_prefixkey_e > 0 ) invalid = true;
				idx_prefixkey_e = idx++;
				break;
			case ',':
				if( idx_null_b > 0 ) invalid = true;
				idx_null_b = idx++;
				break;
			case '~':
				if( idx_length_b < 0 )
					idx_length_b = idx++;
				else {
					if( idx_length_e > 0 ) invalid = true;
					idx_length_e = idx++;
				}
				break;
			case '#':
				if( idx_number_b > 0 ) invalid = true;
				idx_number_b = idx++;
				break;
			case ';':
				idx_suffix_b = idx++;
				break;
			default:
				idx++;
			}
		}
		if( idx_prefixkey_e > 0 )
			invalid = ( idx_number_b > 0 || idx_length_b > 0 );
		else if( idx_number_b > 0 )
			invalid = ( idx_length_b > 0 );
		if( invalid ) throw new IllegalArgumentException( pattern );

		FieldRecordFormat format = new FieldRecordFormat();
		format.encodeType = encodeType;

		int idx_b = ( encodeType == '{' ? 2 : 3 );
		if( idx_prefix_e > 0 ) {
			format.prefixValue = getInstance( pattern.substring(idx_b, idx_prefix_e) );
			idx_b = idx_prefix_e + 1;
		}

		int idx_e;
		if( idx_null_b > 0 ) {
			idx_e = idx_null_b;
			if( idx_suffix_b > 0 ) {
				format.nullValue = getInstance( pattern.substring(++idx_null_b, idx_suffix_b) );
				format.suffixValue = getInstance( pattern.substring(++idx_suffix_b, pattern.length() - 1) );
			} else
				format.nullValue = getInstance( pattern.substring(++idx_null_b, pattern.length() - 1) );
		} else if( idx_suffix_b > 0 ) {
			idx_e = idx_suffix_b;
			format.suffixValue = getInstance( pattern.substring(++idx_suffix_b, pattern.length() - 1) );
		} else
			idx_e = pattern.length() - 1;

		if( idx_prefixkey_e > 0 ) {
			format.prefixKey = pattern.substring( idx_b, idx_prefixkey_e );
			format.fieldKey = pattern.substring( ++idx_prefixkey_e, idx_e );
			if( isEncodePattern(format.prefixKey) )
				throw new IllegalArgumentException( pattern );
		} else if( idx_number_b > 0 ) {
			format.fieldKey = pattern.substring( idx_b, idx_number_b );
			format.numberFormatKey = pattern.substring( ++idx_number_b, idx_e );
			if( isEncodePattern(format.numberFormatKey) )
				throw new IllegalArgumentException( pattern );
		} else if( idx_length_b > 0 ) {
			format.fieldKey = pattern.substring( idx_b, idx_length_b );
			try {
				if( format.isByteLength = (pattern.charAt(idx_e-1) == 'b') ) idx_e--;
				if( idx_length_e > 0 ) {
					if( ++idx_length_b == idx_length_e )
						format.beginIndex = 0;
					else
						format.beginIndex = Integer.parseInt( pattern.substring(idx_length_b, idx_length_e) );

					if( ++idx_length_e < idx_e )
						format.endIndex = Integer.parseInt( pattern.substring(idx_length_e, idx_e) );
				} else {
					format.maxlength = Integer.parseInt( pattern.substring(++idx_length_b, idx_e) );
				}
			} catch( NumberFormatException numEx ) {
				throw new IllegalArgumentException( pattern );
			}
		} else
			format.fieldKey = pattern.substring( idx_b, idx_e );
		if( isEncodePattern(format.fieldKey) )
			throw new IllegalArgumentException( pattern );

		return format;
	}

	private static RecordFormat createInstanceMessage( String pattern ) throws IllegalArgumentException {
		List<String> tokenList = new java.util.ArrayList<String>();

		int idx_s = 2;
		for( int idx = idx_s; idx < pattern.length() - 1; ) {
			switch( pattern.charAt(idx) ) {
			case '$':
				switch( pattern.charAt(++idx) ) {
				case 'H':
				case 'S':
				case 'f':
					if( pattern.charAt(++idx) != '{' ) break;
				case '{':
					idx = getIndexOfCloseBrace( pattern, idx );
				case '$':
					++idx;
				}
				break;
			case '%':
				switch( pattern.charAt(++idx) ) {
				case '{':
					idx = getIndexOfCloseBrace( pattern, idx );
				case '%':
					++idx;
				}
				break;
			case ',':
				tokenList.add( pattern.substring(idx_s, idx) );
				idx_s = ++idx;
				break;
			default:
				++idx;
			}
		}
		tokenList.add( pattern.substring(idx_s, pattern.length() - 1) );

		String messageKey = tokenList.get(0);
		RecordFormat messageFormat = getInstance( messageKey );
		if( messageFormat instanceof ConstantRecordFormat )
			messageFormat = null;

		if( tokenList.size() == 1 ) {
			if( messageFormat != null )
				return new MessageRecordFormat( messageFormat );
			else
				return new MessageRecordFormat( messageKey );
		} else {
			RecordFormat[] params = new RecordFormat[tokenList.size() - 1];
			for( int i = 1; i < tokenList.size(); i++ )
				params[i-1] = getInstance( tokenList.get(i) );

			if( messageFormat != null )
				return new MessageRecordFormat( messageFormat, params );
			else
				return new MessageRecordFormat( messageKey, params );
		}
	}

	public String format( Map recordMap, MessageHandler msghandler ) {
		return format.format( recordMap, msghandler );
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		return format.format( recordMap, msghandler, stringBuffer );
	}

	private static int getIndexOfCloseBrace( String pattern, int fromIndex ) {
		int idx = fromIndex;
		int deep = 1;

		while( idx < pattern.length() ) {
			switch( pattern.charAt(idx) ) {
			case '$':
				try {
					switch( pattern.charAt(++idx) ) {
					case 'H':
					case 'S':
					case 'f':
						if( pattern.charAt(++idx) != '{' ) break;
					case '{':
						++deep;
					case '$':
						++idx;
					}
				} catch( IndexOutOfBoundsException indexBx ) {
					return -1;
				}
				break;
			case '%':
				try {
					switch( pattern.charAt(++idx) ) {
					case '{':
						++deep;
					case '%':
						++idx;
					}
				} catch( IndexOutOfBoundsException indexBx ) {
					return -1;
				}
				break;
			case '}':
				if( --deep == 0 ) return idx;
			default:
				++idx;
			}
		}

		return -1;
	}

	public static RecordFormat getInstance( String pattern ) throws IllegalArgumentException {
		PatternRecordFormat format = formatMap.get( pattern );
		if( format == null ) {
			try {
				format = new PatternRecordFormat( pattern, createInstance(pattern) );
				if( !(format.format instanceof ConstantRecordFormat) )
					formatMap.put( format.pattern, format );
			} catch( IllegalArgumentException argumentEx ) {
				throw new IllegalArgumentException( pattern );
			}
		}

		return format.format;
	}

	public String getPattern() {
		return pattern;
	}

	private static boolean isEncodePattern( String pattern ) {
		return	(
			pattern.startsWith("${")
			|| pattern.startsWith("$H{")
			|| pattern.startsWith("$S{")
			|| pattern.startsWith("$f{")
			|| pattern.startsWith("%{")
		);
	}
}
