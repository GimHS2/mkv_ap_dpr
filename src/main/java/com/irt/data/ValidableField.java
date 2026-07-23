/*
 *	File Name:	ValidableField.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	getValidValueString() 추가
 *	stghr12		2007/11/30		2.2.0	AbstractField 변경사항 적용: validDateFormat 삭제
 *	stghr12		2006/12/01		2.1.0	dataType을 int형에서 char형으로 변경
 *										TYPE_TIME validate 추가
 *	stghr12		2006/09/01		2.0.2	DateFormat 동기화 오류 수정
 *	stghr12		2006/07/07		2.0.1	rangeType 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

import java.util.Map;

/**
 *
 */
public class ValidableField extends Field {
	/** Min, Max쪽 모두 등호가 사용되지 않음. 유효범위: min < x < max **/
	public final static int RANGETYPE_EQUALS_NONE		= 0x00;
	/** Min쪽에 등호가 사용됨. 유효범위: min <= x < max **/
	public final static int RANGETYPE_EQUALS_MIN		= 0x01;
	/** Max쪽에 등호가 사용됨. 유효범위: min < x <= max **/
	public final static int RANGETYPE_EQUALS_MAX		= 0x02;
	/** Min, Max쪽 모두 등호가 사용됨.(default) 유효범위: min <= x <= max **/
	public final static int RANGETYPE_EQUALS_MINMAX		= 0x03;

	/** null가능 여부(모든 TYPE) **/
	protected boolean nullable = true;
	/** true일 경우 null여부를 제외한 validation check를 하지 않음(모든 TYPE) **/
	protected boolean readonly = false;

	/** 최소길이(TYPE_STRING, TYPE_DESCRIPTION), 최소값(TYPE_INTEGER, TYPE_LONG, TYPE_DOUBLE) **/
	protected Number minValue = null;
	/** 최대길이(TYPE_STRING, TYPE_DESCRIPTION), 최소값(TYPE_INTEGER, TYPE_LONG, TYPE_DOUBLE) **/
	protected Number maxValue = null;
	/** 최소/최대 범위(TYPE_INTEGER, TYPE_LONG, TYPE_DOUBLE) **/
	protected int rangeType = RANGETYPE_EQUALS_MINMAX;

	/** 사용가능한 Character리스트(TYPE_STRING) **/
	protected String validCharacters = null;
	/** SPACE로 끝날 수 있는지 여부(TYPE_STRING) **/
	protected boolean trailingBlank = false;
	/** 입력가능한 값(TYPE_CODE) **/
	protected String validValueList = null;

	protected ValidableField( char dataType, String fieldKey, String descriptionKey ) {
		super( dataType, fieldKey, descriptionKey );
	}

	protected ValidableField( char dataType, String fieldKey, String descriptionKey, String prefixKey ) {
		super( dataType, fieldKey, descriptionKey, prefixKey );
	}

	public ValidableField( ValidableField field ) {
		super( field );
		this.nullable = field.nullable;
		this.readonly = field.readonly;
		this.minValue = field.minValue;
		this.maxValue = field.maxValue;
		this.validCharacters = field.validCharacters;
		this.validValueList = field.validValueList;
		this.rangeType = field.rangeType;
	}

	public ValidableField( ValidableField field, boolean nullable, boolean readonly ) {
		this( field );
		this.nullable = nullable;
		this.readonly = readonly;
	}

	/** TYPE_STRING */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, int minValue, int maxValue ) {
		this( nullable, fieldKey, descriptionKey, minValue, maxValue, null, false );
	}

	/** TYPE_STRING */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, int minValue, int maxValue, String validCharacters ) {
		this( nullable, fieldKey, descriptionKey, minValue, maxValue, validCharacters, false );
	}

	/** TYPE_STRING */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, int minValue, int maxValue
						, String validCharacters, boolean trailingBlank ) {
		this( nullable, fieldKey, descriptionKey, TYPE_STRING, minValue, maxValue );
		this.validCharacters = validCharacters;
		this.trailingBlank = trailingBlank;
	}

	/** TYPE_CODE */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, String prefixKey, String validValueList ) {
		this( TYPE_CODE, fieldKey, descriptionKey, prefixKey );
		this.nullable = nullable;
		this.validValueList = validValueList;
	}

	/** TYPE_DOUBLE */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, double minValue, double maxValue ) {
		this( nullable, fieldKey, descriptionKey, TYPE_DOUBLE, new Double(minValue), new Double(maxValue) );
	}

	/** TYPE_DOUBLE */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, double minValue, double maxValue, int rangeType ) {
		this( nullable, fieldKey, descriptionKey, TYPE_DOUBLE, new Double(minValue), new Double(maxValue) );
		this.rangeType = rangeType;
	}

	/** TYPE_LONG */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, long minValue, long maxValue ) {
		this( nullable, fieldKey, descriptionKey, TYPE_LONG, new Long(minValue), new Long(maxValue) );
	}

	/** TYPE_LONG */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, long minValue, long maxValue, int rangeType ) {
		this( nullable, fieldKey, descriptionKey, TYPE_LONG, new Long(minValue), new Long(maxValue) );
		this.rangeType = rangeType;
	}

	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, char dataType ) {
		this( dataType, fieldKey, descriptionKey );
		this.nullable = nullable;
	}

	/** TYPE_INTEGER, TYPE_STRING, TYPE_DESCRIPTION */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, char dataType, int minLength, int maxLength ) {
		this( nullable, fieldKey, descriptionKey, dataType );

		if( dataType == TYPE_INTEGER ) {
			this.minValue = new Integer( minLength );
			this.maxValue = new Integer( maxLength );
		} else {
			if( minLength > 0 ) this.minValue = new Integer( minLength );
			if( maxLength == minLength )
				this.maxValue = this.minValue;
			else if( maxLength > 0 ) this.maxValue = new Integer( maxLength );
		}
	}

	/** TYPE_INTEGER */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, char dataType, int minValue, int maxValue, int rangeType ) {
		this( nullable, fieldKey, descriptionKey, dataType );

		if( dataType == TYPE_INTEGER ) {
			this.minValue = new Integer( minValue );
			this.maxValue = new Integer( maxValue );
			this.rangeType = rangeType;
		}
	}

	/** TYPE_INTEGER, TYPE_LONG, TYPE_DOUBLE */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, char dataType, Number minValue, Number maxValue ) {
		this( nullable, fieldKey, descriptionKey, dataType );
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/** TYPE_INTEGER, TYPE_LONG, TYPE_DOUBLE */
	public ValidableField( boolean nullable, String fieldKey, String descriptionKey, char dataType, Number minValue, Number maxValue
						, int rangeType ) {
		this( nullable, fieldKey, descriptionKey, dataType );
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.rangeType = rangeType;
	}

	public int getMaxLength() {
		switch( dataType ) {
		case TYPE_NONE:
			return -1;
		case TYPE_CODE:
			if( validValueList != null ) {
				int maxlength = -1;
				String[] validValues = validValueList.split( "," );

				for( int i = 0; i < validValues.length; i++ )
					if( validValues[i].length() > maxlength )
						maxlength = validValues[i].length();

				return maxlength;
			}
		case TYPE_DESCRIPTION:
		case TYPE_STRING:
			return( maxValue == null ? -1 : maxValue.intValue() );
		case TYPE_INTEGER:
		case TYPE_LONG:
		case TYPE_DOUBLE:
			return -1;
		case TYPE_DATE:
			return 10;
		case TYPE_DATETIME:
			return 19;
		case TYPE_TIME:
			return 5;
		}

		return -1;
	}

	public Number getMaxValue() {
		return maxValue;
	}

	public Number getMinValue() {
		return minValue;
	}

	public int getRangeType() {
		return rangeType;
	}

	public String getValidCharacters() {
		return validCharacters;
	}

	public String[] getValidValues() {
		if( validValueList == null ) return null;
		return validValueList.split( "," );
	}

	public String getValidValueString() {
		return validValueList;
	}

	public boolean nullable() {
		return nullable;
	}

	public boolean readonly() {
		return readonly;
	}

	public boolean trailingBlank() {
		return trailingBlank;
	}

	public Object validate( Map recordMap ) throws FieldException {
		return validate( extractValue(recordMap), true );
	}

	/**
	 * return {@link #validate(Object , boolean) validate}( value, false );
	 */
	public Object validate( Object value ) throws FieldException {
		return validate( value, false );
	}

	public Object validate( Object value, boolean convertedValue ) throws FieldException {
		if( !convertedValue ) value = convertObject( value );
		if( value == null ) {
			if( nullable ) return null;
			throw new FieldException( FieldException.ERR_CANNOT_NULL, this );
		}
		if( readonly ) return value;

		try {
			String s_value;
			Number n_value;

			switch( dataType ) {
			case AbstractField.TYPE_CODE:
				if( validValueList != null ) {
					s_value = (String)value;

					int idx = validValueList.indexOf( s_value );
					while( idx >= 0 ) {
						if( idx == 0 || validValueList.charAt(idx-1) == ',' ) {
							try {
								if( validValueList.charAt(idx + s_value.length()) == ',' )
									break;
							} catch( StringIndexOutOfBoundsException indexEx ) {
								break;
							}
						}
						idx = validValueList.indexOf( s_value, idx + 1 );
					}
					if( idx < 0 ) throw new FieldException( FieldException.ERR_INVALID_VALUE, this, value );
				}
				break;
			case AbstractField.TYPE_STRING:
				if( validCharacters != null ) {
					s_value = (String)value;
					int length = s_value.length();

					while( trailingBlank && s_value.charAt(length-1) == ' ' ) length--;
					for( int i = 0; i < length; i++ ) {
						char chr = s_value.charAt(i);
						if( validCharacters.indexOf(chr) < 0 )
							throw new FieldException( FieldException.ERR_INVALID_CHAR, this, String.valueOf(chr) );
					}
				}
			case AbstractField.TYPE_DESCRIPTION:
				s_value = (String)value;
				int length = s_value.getBytes().length;
				if( minValue == maxValue ) {
					if( minValue != null && minValue.intValue() != length )
						throw new FieldException( FieldException.ERR_INVALID_LENGTH, this, value );
				} else if( minValue != null && minValue.intValue() > length )
					throw new FieldException( FieldException.ERR_SMALL_LENGTH, this, value );
				else if( maxValue != null && maxValue.intValue() < length )
					throw new FieldException( FieldException.ERR_LARGE_LENGTH, this, value );
				break;
			case AbstractField.TYPE_INTEGER:
			case AbstractField.TYPE_LONG:
			case AbstractField.TYPE_DOUBLE:
				n_value = (Number)value;
				if( minValue != null ) {
					if( minValue.doubleValue() > n_value.doubleValue() )
						throw new FieldException( FieldException.ERR_SMALL_VALUE, this, value );
					else if( minValue.doubleValue() == n_value.doubleValue() && (rangeType & RANGETYPE_EQUALS_MIN) == 0 )
						throw new FieldException( FieldException.ERR_SMALL_VALUE, this, value );
				}
				if( maxValue != null ) {
					if( maxValue.doubleValue() < n_value.doubleValue() )
						throw new FieldException( FieldException.ERR_LARGE_VALUE, this, value );
					else if( maxValue.doubleValue() == n_value.doubleValue() && (rangeType & RANGETYPE_EQUALS_MAX) == 0 )
						throw new FieldException( FieldException.ERR_LARGE_VALUE, this, value );
				}
				break;
			case AbstractField.TYPE_TIME:
				s_value = (String)value;
				if( s_value.length() != 5 || s_value.charAt(2) != ':' )
					throw new FieldException( FieldException.ERR_INVALID_TIME, this, value );
				else if( s_value.charAt(0) < '0' || s_value.charAt(0) > '2' )
					throw new FieldException( FieldException.ERR_INVALID_TIME, this, value );
				else if( s_value.charAt(1) < '0' || s_value.charAt(1) > '9' )
					throw new FieldException( FieldException.ERR_INVALID_TIME, this, value );
				else if( s_value.charAt(3) < '0' || s_value.charAt(3) > '5' )
					throw new FieldException( FieldException.ERR_INVALID_TIME, this, value );
				else if( s_value.charAt(4) < '0' || s_value.charAt(4) > '9' )
					throw new FieldException( FieldException.ERR_INVALID_TIME, this, value );
				else if( s_value.charAt(0) == '2' && s_value.charAt(1) > '3' )
					throw new FieldException( FieldException.ERR_INVALID_TIME, this, value );
				break;
			}

			return value;
		} catch( ClassCastException castEx ) {
			throw new FieldException( FieldException.ERR_INVALID_TYPE, this, value );
		}
	}
}
