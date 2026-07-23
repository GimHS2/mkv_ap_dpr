/*
 *	File Name:	Field.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	RecordFormat 변경사항 적용: format( recordMap, msghandler, stringBuffer ) 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										AbstractField 변경사항 적용: validDateFormat 삭제
 *	stghr12		2006/12/01		2.1.0	dataType을 int형에서 char형으로 변경
 *										TYPE_TIME 추가
 *	stghr12		2006/09/01		2.0.1	DateFormat 동기화 오류 수정
 *										ParseException이 발생했을 때, com.irt.data.Date.getInstance()에서 defaultDateFormat쓰는 부분 삭제.
 *										getDefaultDateFormat(), isDefaultDateFormat() 삭제
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

import com.irt.util.MessageHandler;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class Field implements AbstractField {
	protected char dataType;
	protected String fieldKey, descriptionKey;
	protected String prefixKey = null;

	public Field( char dataType, String fieldKey ) {
		this( dataType, fieldKey, fieldKey.toUpperCase() );
	}

	public Field( char dataType, String fieldKey, String descriptionKey ) {
		this( dataType, fieldKey, descriptionKey, null );
	}

	public Field( char dataType, String fieldKey, String descriptionKey, String prefixKey ) {
		this.dataType = dataType;
		this.fieldKey = fieldKey;
		if( descriptionKey != null )
			this.descriptionKey = "FIELD_"+ descriptionKey;
		if( dataType == AbstractField.TYPE_CODE )
			this.prefixKey = prefixKey;
	}

	protected Field( Field field ) {
		this.dataType = field.dataType;
		this.fieldKey = field.fieldKey;
		this.descriptionKey = field.descriptionKey;
		this.prefixKey = field.prefixKey;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		fieldKeySet.add( fieldKey );
	}

	/**
	 * dataType에 따라 적절한 Object형태로 return.
	 * <p>
	 * <ul type='square'>
	 * <li>{@link #TYPE_NONE}: {@link String}, {@link Number}, java.util.{@link com.irt.data.Date}
	 * <li>{@link #TYPE_CODE}: {@link String}
	 * <li>{@link #TYPE_DESCRIPTION}: {@link String}
	 * <li>{@link #TYPE_STRING}: {@link String}
	 * <li>{@link #TYPE_INTEGER}: {@link Number}
	 * <li>{@link #TYPE_LONG}: {@link Number}
	 * <li>{@link #TYPE_DOUBLE}: {@link Number}
	 * <li>{@link #TYPE_DATE}: com.irt.data.{@link com.irt.data.Date}
	 * <li>{@link #TYPE_DATETIME}: com.irt.data.{@link com.irt.data.Timestamp}
	 * <li>{@link #TYPE_TIME}: {@link String}
	 * </ul>
	 */
	public Object convertObject( Object object ) throws FieldException {
		if( object == null ) return null;
		if( object instanceof String ) {
			String string = (String)object;
			if( string.length() == 0 ) return null;

			try {
				switch( dataType ) {
				case AbstractField.TYPE_INTEGER:
					return Integer.valueOf( string );
				case AbstractField.TYPE_LONG:
					return Long.valueOf( string );
				case AbstractField.TYPE_DOUBLE:
					return Double.valueOf( string );
				case AbstractField.TYPE_DATE:
					return com.irt.data.Date.getInstance( string );
				case AbstractField.TYPE_DATETIME:
					throw new FieldException( FieldException.ERR_INVALID_TYPE, this, object );
				default:
					return object;
				}
			} catch( NumberFormatException numEx ) {
				throw new FieldException( FieldException.ERR_INVALID_NUMBER, this, object );
			} catch( ParseException parseEx ) {
				throw new FieldException( FieldException.ERR_INVALID_DATE, this, object );
			}
		} else {
			switch( dataType ) {
			case AbstractField.TYPE_CODE:
			case AbstractField.TYPE_DESCRIPTION:
			case AbstractField.TYPE_STRING:
			case AbstractField.TYPE_TIME:
				if( object instanceof Number || object instanceof java.util.Date )
					return object.toString();
				else
					throw new FieldException( FieldException.ERR_INVALID_TYPE, this, object );
			case AbstractField.TYPE_INTEGER:
			case AbstractField.TYPE_LONG:
			case AbstractField.TYPE_DOUBLE:
				if( object instanceof Number )
					return object;
				else
					throw new FieldException( FieldException.ERR_INVALID_NUMBER, this, object );
			case AbstractField.TYPE_DATE:
				if( object instanceof com.irt.data.Date )
					return object;
				else if( object instanceof java.util.Date )
					return com.irt.data.Date.getInstance( (java.util.Date)object );
				else
					throw new FieldException( FieldException.ERR_INVALID_DATE, this, object );
			case AbstractField.TYPE_DATETIME:
				if( object instanceof com.irt.data.Timestamp )
					return object;
				else if( object instanceof java.util.Date )
					return new com.irt.data.Timestamp( (java.util.Date)object );
				else
					throw new FieldException( FieldException.ERR_INVALID_DATE, this, object );
			case AbstractField.TYPE_NONE:
				if( object instanceof Number )
					return object;
				else if( object instanceof com.irt.data.Date )
					return object;
				else if( object instanceof java.util.Date )
					return com.irt.data.Date.getInstance( (java.util.Date)object );
				throw new FieldException( FieldException.ERR_INVALID_TYPE, this, object );
			default:
				throw new FieldException( FieldException.ERR_INVALID_TYPE, this, object );
			}
		}
	}

	/**
	 * Map에서 Field에 해당하는 데이터(converted)를 추출.
	 * <p>
	 * @see #convertObject(Object) convertObject
	 */
	public Object extractValue( Map recordMap ) throws FieldException {
		Object object = recordMap.get( fieldKey );
		if( object instanceof Object[] )
			return convertObject( ((Object[])object)[0] );
		else
			return convertObject( object );
	}

	public String format( Map recordMap, com.irt.util.MessageHandler msghandler ) {
		Object object = recordMap.get( fieldKey );
		if( object == null ) return "";

		switch( dataType ) {
		case AbstractField.TYPE_CODE:
			if( prefixKey != null )
				return msghandler.getMessage( prefixKey + object.toString() );
			break;
		}

		return object.toString();
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		return stringBuffer.append( format(recordMap, msghandler) );
	}

	public char getDataType() {
		return dataType;
	}

	public String getDescriptionKey() {
		return descriptionKey;
	}

	public String getFieldKey() {
		return fieldKey;
	}

	public String getPrefixKey() {
		return prefixKey;
	}
}
