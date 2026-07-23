/*
 *	File Name:	FunctionRecordFormat.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	guksm		2008/08/27		2.2.3	FUNCTION_PURECODE 추가
 *	stghr12		2008/05/31		2.2.2	FUNCTION_CASE, FUNCTION_EVALUATE 추가
 *	stghr12		2008/03/31		2.2.1	extends PattenRecordFormat -> implements RecordFormat, public으로 변경
 *										구조변경, FUNCTION_ELLIPSIS, FUNCTION_SUBSTRING 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										PUB_JAVA_DAY_OF_WEEK_SHORT_ -> PUB_DAY_OF_WEEK_SHORT_
 *										com.irt.data.Date.addDays() -> com.irt.data.Date.getDate()
 *	stghr12		2007/10/31		2.1.1	FUNCTION_DATE, FUNCTION_WEEKDAY 추가
 *										FUNCTION_DECODE2: default가능하게 수정
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.data.format;

import com.irt.data.Evaluator;
import com.irt.util.MessageHandler;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 *
 */
public abstract class FunctionRecordFormat implements RecordFormat {
	private final static String FUNCTION_CASE			= "case";			// 조건에 따라 내용 표시(조건은 NUMBER만 지원)
	private final static String FUNCTION_DATE			= "date";			// 날짜표시
	private final static String FUNCTION_DATEOFWEEK		= "dateofweek";		// 요일표시
	private final static String FUNCTION_DECODE			= "decode";			// DECODE
	// 첫번째 파라미터 표시. 두번째 파라미터의 값에 따라 표시하는 값 앞뒤에 특정 문자를 표시
	private final static String FUNCTION_DECODE2		= "decode2";
	private final static String FUNCTION_DECODE_DATE	= "decode_date";	// 요일에 따라 다른 데이터 표시
	private final static String FUNCTION_EVALUATE		= "eval";
	private final static String FUNCTION_ELLIPSIS		= "ellipsis";
	private final static String FUNCTION_SUBSTRING		= "substring";
	private final static String FUNCTION_WEEKDAY		= "weekday";		// 일~토, Y/N으로 된 값 처리
	private final static String FUNCTION_PURECODE		= "pure";			// 코드 앞 0 제거

	Object[] params;

	protected FunctionRecordFormat( Object... params ) {
		this.params = params;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		if( params != null ) {
			for( int p = 0; p < params.length; p++ )
				if( params[p] instanceof RecordFormat )
					((RecordFormat)params[p]).addFieldKeyToSet( fieldKeySet );
		}
	}

	public String format( Map recordMap, MessageHandler msghandler ) {
		return format( recordMap, msghandler, new StringBuffer() ).toString();
	}

	public abstract StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer );

	protected static String format( Object param, Map recordMap, MessageHandler msghandler ) {
		if( param instanceof RecordFormat )
			return ((RecordFormat)param).format( recordMap, msghandler );
		else if( param instanceof String )
			return (String)param;
		else if( param != null )
			return param.toString();
		else
			return "";
	}

	protected static StringBuffer format( Object param, Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		if( param instanceof RecordFormat )
			return ((RecordFormat)param).format( recordMap, msghandler, stringBuffer );
		else if( param != null )
			return stringBuffer.append( param.toString() );
		else
			return stringBuffer;
	}

	public static FunctionRecordFormat getInstance( String functionName, Object... params ) throws IllegalArgumentException {
		if( FUNCTION_CASE.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.CASE( params );
		else if( FUNCTION_DATE.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.DATE( params );
		else if( FUNCTION_DATEOFWEEK.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.DATEOFWEEK( params );
		else if( FUNCTION_DECODE.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.DECODE( params );
		else if( FUNCTION_DECODE2.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.DECODE2( params );
		else if( FUNCTION_DECODE_DATE.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.DECODE_DATE( params );
		else if( FUNCTION_EVALUATE.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.EVALUATE( params );
		else if( FUNCTION_ELLIPSIS.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.ELLIPSIS( params );
		else if( FUNCTION_SUBSTRING.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.SUBSTRING( params );
		else if( FUNCTION_WEEKDAY.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.WEEKDAY( params );
		else if( FUNCTION_PURECODE.equals(functionName) )
			return new com.irt.data.format.FunctionRecordFormat.PURECODE( params );
		else {
			ClassLoader classLoader = FunctionRecordFormat.class.getClassLoader();
			try {
				Class formatClass = classLoader.loadClass( functionName );
				Constructor formatConstructor = formatClass.getConstructor( Object[].class );

				return (FunctionRecordFormat)formatConstructor.newInstance( params );
			} catch( Exception ex ) {
				ex.printStackTrace( System.err );
				throw new IllegalArgumentException( ex );
			}
		}
	}

	protected static String getValue( Object param, Map recordMap, MessageHandler msghandler ) {
		if( param instanceof RecordFormat )
			return ((RecordFormat)param).format( recordMap, msghandler );
		else if( recordMap != null ) {
			Object value = recordMap.get( (String)param );
			if( value instanceof String )
				return (String)value;
			else if( value != null )
				return value.toString();
		}

		return null;
	}

	/**
	 *	CASE(cond1, string1[, cond2, string2][, ...][, defaultString])
	 */
	public static class CASE extends FunctionRecordFormat {
		public CASE( Object... params ) throws IllegalArgumentException {
			super( params );

			if( params == null || params.length < 2 )
				throw new IllegalArgumentException();

			for( int p = 1; p < params.length; p += 2 ) {
				if( !(params[p-1] instanceof String) ) throw new IllegalArgumentException();

				params[p-1] = new Evaluator( (String)params[p-1] );
				if( !((Evaluator)params[p-1]).isBooleanType() )
					throw new IllegalArgumentException();
			}
		}

		public void addFieldKeyToSet( Set<String> fieldKeySet ) {
			for( int p = 0; p < params.length; p++ ) {
				if( params[p] instanceof RecordFormat )
					((RecordFormat)params[p]).addFieldKeyToSet( fieldKeySet );
				else if( params[p] instanceof Evaluator )
					((Evaluator)params[p]).addFieldKeyToSet( fieldKeySet );
			}
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			int index = 0;

			for( int p = 0; p < params.length; p += 2 ) {
				if( params[p] instanceof Evaluator ) {
					Object resultValue = ((Evaluator)params[p]).evaluate( recordMap );
					if( resultValue != null && ((Boolean)resultValue).booleanValue() ) {
						index = ++p;
						break;
					}
				} else
					index = p;
			}
			if( index == 0 )
				return stringBuffer;
			else
				return FunctionRecordFormat.format( params[index], recordMap, msghandler, stringBuffer );
		}
	}

	/**
	 *	DATE([fieldKey]), DATE([fieldKey]+?), DATE([fieldKey]-?)
	 */
	public static class DATE extends FunctionRecordFormat {
		String fieldKey;
		int days;

		public DATE( Object... params ) throws IllegalArgumentException {
			super( params );

			this.days = 0;
			if( params == null || params.length == 0 )
				this.fieldKey = null;
			else if( params.length == 1 && params[0] instanceof String ) {
				String value = (String)params[0];

				int index = value.indexOf( '-' );
				if( index < 0 ) index = value.indexOf( '+' );
				if( index < 0 )
					this.fieldKey = value;
				else {
					this.fieldKey = ( index == 0 ? null : value.substring(0, index) );
					value = value.substring( value.charAt(index) == '+' ? index+1 : index );
					try {
						if( value.length() > 0 )
							this.days = Integer.parseInt( value );
					} catch( NumberFormatException numEx ) {
						throw new IllegalArgumentException();
					}
				}
			} else
				throw new IllegalArgumentException();
		}

		public void addFieldKeyToSet( Set<String> fieldKeySet ) {
			if( fieldKey != null ) fieldKeySet.add( fieldKey );
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			com.irt.data.Date date = null;

			if( fieldKey != null ) {
				Object value = ( recordMap == null ? null : recordMap.get(fieldKey) );

				if( value instanceof com.irt.data.Date )
					date = (com.irt.data.Date)value;
				else if( value instanceof java.util.Date )
					date = com.irt.data.Date.getInstance( (java.util.Date)value );
				else if( value instanceof String )
					try {
						date = com.irt.data.Date.getInstance( (String)value );
					} catch( java.text.ParseException parseEx ) {}
			} else
				date = com.irt.data.Date.getInstance();
			if( date != null )
				stringBuffer.append( date.getDate(days).toString() );

			return stringBuffer;
		}
	}

	/**
	 *	DATEOFWEEK([prefixKey@]fieldKey)
	 */
	public static class DATEOFWEEK extends FunctionRecordFormat {
		String prefixKey, fieldKey;

		public DATEOFWEEK( Object... params ) throws IllegalArgumentException {
			super( params );

			if( params == null || params.length != 1 || !(params[0] instanceof String) )
				throw new IllegalArgumentException();

			params = ((String)params[0]).split( "@", 2 );
			if( params.length == 1 ) {
				this.prefixKey = "PUB_DAY_OF_WEEK_SHORT_";
				this.fieldKey = (String)params[0];
			} else {
				this.prefixKey = (String)params[0];
				this.fieldKey = (String)params[1];
			}
		}

		public void addFieldKeyToSet( Set<String> fieldKeySet ) {
			fieldKeySet.add( fieldKey );
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			Calendar calendar = null;

			Object value = ( recordMap == null ? null : recordMap.get(fieldKey) );
			if( value instanceof Calendar )
				calendar = (Calendar)value;
			else if( value instanceof java.util.Date ) {
				calendar = Calendar.getInstance();
				calendar.setTimeInMillis( ((java.util.Date)value).getTime() );
			}
			if( calendar != null )
				stringBuffer.append( msghandler.getMessage( prefixKey + calendar.get(Calendar.DAY_OF_WEEK) ) );

			return stringBuffer;
		}
	}

	/**
	 *	DECODE(fieldKey, value1, string1[, value2, string2[, ...]][, defaultString])
	 */
	public static class DECODE extends FunctionRecordFormat {
		public DECODE( Object... params ) throws IllegalArgumentException {
			super( params );

			if( params == null || params.length == 0 || params[0] == null )
				throw new IllegalArgumentException();
		}

		public void addFieldKeyToSet( Set<String> fieldKeySet ) {
			for( int p = 0; p < params.length; p++ )
				if( params[p] instanceof RecordFormat )
					((RecordFormat)params[p]).addFieldKeyToSet( fieldKeySet );
			if( params[0] instanceof String ) fieldKeySet.add( (String)params[0] );
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			Object value = FunctionRecordFormat.getValue( params[0], recordMap, msghandler );
			if( "".equals(value) ) value = null;

			Object resultValue = null;
			int index = getIndex( recordMap, msghandler, value, 1, 2 );
			if( index > 0 )
				resultValue = params[index+1];
			else if( (params.length % 2) == 0 )
				resultValue = params[params.length-1];

			return FunctionRecordFormat.format( resultValue, recordMap, msghandler, stringBuffer );
		}

		protected int getIndex( Map recordMap, MessageHandler msghandler, Object value, int startIndex, int increaseValue ) {
			for( int p = startIndex; p <= params.length - increaseValue; p += increaseValue ) {
				String checkValue = null;
				if( params[p] instanceof RecordFormat )
					checkValue = ((RecordFormat)params[p]).format( recordMap, msghandler );
				else if( params[p] != null )
					checkValue = params[p].toString();

				if( value == null ? (checkValue == null || "".equals(checkValue)) : value.equals(checkValue) )
					return p;
			}

			return -1;
		}
	}

	/**
	 *	DECODE_DATE(fieldKey, value1, string1_1, string1_2[, value2, string2_1, string2_2[, ...]][, defaultString_1, defaultString_2])
	 */
	public static class DECODE2 extends DECODE {
		public DECODE2( Object... params ) throws IllegalArgumentException {
			super( params );

			if( params == null || params.length < 5 || (params.length%3) == 0 || params[0] == null || params[1] == null )
				throw new IllegalArgumentException();
		}

		public void addFieldKeyToSet( Set<String> fieldKeySet ) {
			for( int p = 0; p < params.length; p++ )
				if( params[p] instanceof RecordFormat )
					((RecordFormat)params[p]).addFieldKeyToSet( fieldKeySet );
			if( params[1] instanceof String ) fieldKeySet.add( (String)params[1] );
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			Object value = FunctionRecordFormat.getValue( params[1], recordMap, msghandler );
			if( "".equals(value) ) value = null;

			Object resultValue1 = null, resultValue2 = null;
			int index = getIndex( recordMap, msghandler, value, 2, 3 );
			if( index > 0 ) {
				resultValue1 = params[index+1];
				resultValue2 = params[index+2];
			} else if( (params.length%3) == 1 ) {
				resultValue1 = params[params.length-2];
				resultValue2 = params[params.length-1];
			}

			FunctionRecordFormat.format( resultValue1, recordMap, msghandler, stringBuffer );
			FunctionRecordFormat.format( params[0], recordMap, msghandler, stringBuffer );
			FunctionRecordFormat.format( resultValue2, recordMap, msghandler, stringBuffer );

			return stringBuffer;
		}
	}

	/**
	 *	DECODE_DATE(fieldKey, value1, string1[, value2, string2[, ...]][, defaultString])
	 */
	public static class DECODE_DATE extends DECODE {
		public DECODE_DATE( Object... params ) throws IllegalArgumentException {
			super( params );
			if( !(params[0] instanceof String) )
				throw new IllegalArgumentException();
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			Calendar calendar = null;

			Object value = ( recordMap == null ? null : recordMap.get((String)params[0]) );
			if( value instanceof Calendar )
				calendar = (Calendar)value;
			else if( value instanceof java.util.Date ) {
				calendar = Calendar.getInstance();
				calendar.setTimeInMillis( ((java.util.Date)value).getTime() );
			} else
				return stringBuffer;
			String dayOfWeek = String.valueOf( calendar.get(Calendar.DAY_OF_WEEK) );

			Object resultValue = null;
			int index = getIndex( recordMap, msghandler, dayOfWeek, 1, 2 );
			if( index > 0 )
				resultValue = params[index+1];
			else if( (params.length % 2) == 0 )
				resultValue = params[params.length-1];

			return FunctionRecordFormat.format( resultValue, recordMap, msghandler, stringBuffer );
		}
	}

	/**
	 *	ELLIPSIS(fieldKey, maxLength[b][, ellipsisPattern]), ELLIPSIS(pattern, maxLength[b][, ellipsisPattern])
	 */
	public static class ELLIPSIS extends FunctionRecordFormat {
		Object field, ellipsis = "...";
		int maxLength;
		boolean isByteLength = false;

		public ELLIPSIS( Object... params ) throws IllegalArgumentException {
			super( params );

			if( params == null || params.length < 2 || params.length > 3 )
				throw new IllegalArgumentException();

			this.field = params[0];
			try {
				String lengthValue = (String)params[1];
				if( lengthValue.endsWith("b") ) {
					this.isByteLength = true;
					this.maxLength = Integer.parseInt( lengthValue.substring(0, lengthValue.length()-1) );
				} else
					this.maxLength = Integer.parseInt( lengthValue );
				if( params.length > 2 ) this.ellipsis = params[2];
			} catch( ClassCastException castEx ) {
				throw new IllegalArgumentException();
			} catch( NumberFormatException numEx ) {
				throw new IllegalArgumentException();
			}
		}

		public void addFieldKeyToSet( Set<String> fieldKeySet ) {
			for( int p = 0; p < params.length; p++ )
				if( params[p] instanceof RecordFormat )
					((RecordFormat)params[p]).addFieldKeyToSet( fieldKeySet );
			if( field instanceof String ) fieldKeySet.add( (String)field );
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			String value = FunctionRecordFormat.getValue( field, recordMap, msghandler );
			if( value == null )
				return stringBuffer;
			if( isByteLength ) {
				byte[] bytes = value.getBytes();
				if( bytes.length < maxLength )
					return stringBuffer.append( value );
				else {
					String ellipsisString = FunctionRecordFormat.format( ellipsis, recordMap, msghandler );
					int length = maxLength - ellipsisString.getBytes().length;

					value = new String( bytes, 0, length );
					if( value.length() == (new String(bytes, 0, length+1)).length() )
						value = new String( bytes, 0, length-1 );
					return stringBuffer.append( value ).append( ellipsisString );
				}
			} else {
				if( value.length() < maxLength )
					return stringBuffer.append( value );
				else {
					String ellipsisString = FunctionRecordFormat.format( ellipsis, recordMap, msghandler );
					return stringBuffer.append( value.substring(0, maxLength - ellipsisString.length()) ).append( ellipsisString );
				}
			}
		}
	}

	/**
	 *	WEEKDAY(formula[, numberFormatKey])
	 */
	public static class EVALUATE extends FunctionRecordFormat {
		String numberFormatKey = null;
		Evaluator evaluator;

		public EVALUATE( Object... params ) throws IllegalArgumentException {
			super( params );
			if( params.length != 1 && params.length != 2 )
				throw new IllegalArgumentException();
			else if( !(params[0] instanceof String) )
				throw new IllegalArgumentException();
			else if( params.length == 2 && !(params[1] instanceof String) )
				throw new IllegalArgumentException();

			this.numberFormatKey = ( params.length == 2 ? (String)params[1] : null );
			this.evaluator = new Evaluator( (String)params[0] );

			if( evaluator.isBooleanType() ) throw new IllegalArgumentException();
		}

		public void addFieldKeyToSet( Set<String> fieldKeySet ) {
			evaluator.addFieldKeyToSet( fieldKeySet );
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			Object value = evaluator.evaluate( recordMap );

			if( value == null )
				return stringBuffer;
			else {
				if( numberFormatKey != null ) {
					String pattern = msghandler.getMessage( numberFormatKey );
					if( pattern != null ) {
						double doubleValue = ((Number)value).doubleValue();
						java.text.NumberFormat numberFormat = new java.text.DecimalFormat( pattern );
						return stringBuffer.append( numberFormat.format(doubleValue) );
					}
				}

				return stringBuffer.append( value );
			}
		}
	}

	/**
	 *	SUBSTRING(fieldKey, startIndex[, endIndex]), SUBSTRING(pattern, startIndex[, endIndex])
	 */
	public static class SUBSTRING extends FunctionRecordFormat {
		Object field;
		int startIndex, endIndex;

		public SUBSTRING( Object... params ) throws IllegalArgumentException {
			super( params );

			if( params == null || params.length == 0 || params.length > 3 )
				throw new IllegalArgumentException();

			this.field = params[0];
			try {
				this.startIndex = Integer.parseInt( (String)params[1] );
				this.endIndex = ( params.length > 2 ? Integer.parseInt((String)params[2]) : -1 );
				if( this.startIndex < 0 )
					throw new IllegalArgumentException();
			} catch( ClassCastException castEx ) {
				throw new IllegalArgumentException();
			} catch( NumberFormatException numEx ) {
				throw new IllegalArgumentException();
			}
		}

		public void addFieldKeyToSet( Set<String> fieldKeySet ) {
			for( int p = 0; p < params.length; p++ )
				if( params[p] instanceof RecordFormat )
					((RecordFormat)params[p]).addFieldKeyToSet( fieldKeySet );
			if( field instanceof String ) fieldKeySet.add( (String)field );
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			String value = FunctionRecordFormat.getValue( field, recordMap, msghandler );
			if( value == null || value.length() < startIndex )
				return stringBuffer;
			else if( endIndex < 0 || value.length() < endIndex )
				return stringBuffer.append( value.substring(startIndex) );
			else
				return stringBuffer.append( value.substring(startIndex, endIndex) );
		}
	}

	/**
	 *	WEEKDAY([prefixKey@]fieldKey[, alwaysMessagePattern)
	 */
	public static class WEEKDAY extends FunctionRecordFormat {
		String prefixKey, fieldKey;
		Object alwaysMessage;

		public WEEKDAY( Object... params ) throws IllegalArgumentException {
			super( params );

			if( params == null || params.length == 0 || params.length > 2 || !(params[0] instanceof String) )
				throw new IllegalArgumentException();

			if( params.length == 2 ) alwaysMessage = params[1];

			params = ((String)params[0]).split( "@", 2 );
			if( params.length == 1 ) {
				this.prefixKey = "PUB_DAY_OF_WEEK_SHORT_";
				this.fieldKey = (String)params[0];
			} else {
				this.prefixKey = (String)params[0];
				this.fieldKey = (String)params[1];
			}
		}

		public void addFieldKeyToSet( Set<String> fieldKeySet ) {
			fieldKeySet.add( fieldKey );
			if( alwaysMessage instanceof RecordFormat )
				((RecordFormat)alwaysMessage).addFieldKeyToSet( fieldKeySet );
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			Object value = ( recordMap == null ? null : recordMap.get(fieldKey) );
			if( value == null || !(value instanceof String) )
				return stringBuffer;

			if( "YYYYYYY".equals(value) && alwaysMessage != null )
				return FunctionRecordFormat.format( alwaysMessage, recordMap, msghandler, stringBuffer );

			int count = 0;
			String svalue = (String)value;
			for( int i = 0; i < svalue.length(); i++ ) {
				if( svalue.charAt(i) == 'Y' ) {
					if( count++ > 0 ) stringBuffer.append( "," );
					stringBuffer.append( msghandler.getMessage( prefixKey + (i+1) ) );
				}
			}

			return stringBuffer;
		}
	}

	/**
	 *	PURECODE(fieldKey)
	 */
	public static class PURECODE extends FunctionRecordFormat {
		Object field;

		public PURECODE( Object... params ) throws IllegalArgumentException {
			super( params );

			if( params == null || params.length == 0 || params.length > 1 )
				throw new IllegalArgumentException();

			if( params.length == 1 ) this.field = params[0];
		}

		public void addFieldKeyToSet( Set<String> fieldKeySet ) {
			for( int p = 0; p < params.length; p++ )
				if( params[p] instanceof RecordFormat )
					((RecordFormat)params[p]).addFieldKeyToSet( fieldKeySet );
			if( field instanceof String ) fieldKeySet.add( (String)field );
		}

		public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
			String value = FunctionRecordFormat.getValue( field, recordMap, msghandler );
			if( value == null || !(value instanceof String) )
				return stringBuffer;

			int startIndex = 0;
			String code = (String)value;
			for( int i = 0; i < code.length(); i++ ) {
				if( code.charAt(i) != '0' ) {
					startIndex = i;
					break;
				}
			}

			stringBuffer.append( code.substring(startIndex) );

			return stringBuffer;
		}
	}
}
