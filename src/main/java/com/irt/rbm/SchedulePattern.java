/*
 *	File Name:	SchedulePattern.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.rbm;

import com.irt.data.AbstractField;
import com.irt.data.FieldException;
import com.irt.data.ValidableField;
import java.util.Map;

/**
 *
 */
public class SchedulePattern {
	public final static char TYPE_DATE					= '0';
	public final static char TYPE_DAY_OF_MONTH			= '1';
	public final static char TYPE_WEEK_OF_MONTH			= '2';
	public final static char TYPE_DAYINTERVAL			= '3';
	public final static char TYPE_DAY_OF_WEEK			= '4';
	public final static char TYPE_EVERYDAY				= '5';
	public final static char TYPE_EVERYHOUR				= '6';
	public final static char TYPE_EVERYTIME				= '7';

	public final static String VALIDTYPES_ALL			= "01234567";
	public final static String VALIDTYPES_DAY			= "012345";

	final static ValidableField fld_patternType = new ValidableField( false, "patternType", "SCHEDULE_PATTERN_TYPE", 1, 1, VALIDTYPES_ALL );
	final static ValidableField fld_patternIndex = new ValidableField( false, "patternIndex", "SCHEDULE_PATTERN_INDEX", AbstractField.TYPE_INTEGER );
	final static ValidableField fld_patternDate = new ValidableField( false, "patternDate", "SCHEDULE_PATTERN_DATE", AbstractField.TYPE_DATE );

	public static Map<String, Object> createPatternMap( char patternType, int patternIndex, java.util.Date patternDate ) {
		Map<String, Object> patternMap = new java.util.TreeMap<String, Object>();

		switch( patternType ) {
		case TYPE_DATE:
			patternIndex = 0;
			break;
		case TYPE_EVERYDAY:
			patternIndex = 0;
		case TYPE_DAY_OF_MONTH:
		case TYPE_WEEK_OF_MONTH:
		case TYPE_DAY_OF_WEEK:
		case TYPE_EVERYHOUR:
		case TYPE_EVERYTIME:
			patternDate = getDefaultPatternDate();
			break;
		}
		patternMap.put( "patternType", String.valueOf(patternType) );
		patternMap.put( "patternIndex", new Integer(patternIndex) );
		patternMap.put( "patternDate", patternDate );

		return patternMap;
	}

	public static Map<String, Object> createPatternMap( Object patternType, Object patternIndex, Object patternDate ) {
		Map<String, Object> patternMap = new java.util.TreeMap<String, Object>();

		if( patternType != null && patternType.toString().length() == 1 ) {
			switch( patternType.toString().charAt(0) ) {
			case TYPE_DATE:
				patternIndex = new Integer(0);
				break;
			case TYPE_EVERYDAY:
				patternIndex = new Integer(0);
			case TYPE_DAY_OF_MONTH:
			case TYPE_WEEK_OF_MONTH:
			case TYPE_DAY_OF_WEEK:
			case TYPE_EVERYHOUR:
			case TYPE_EVERYTIME:
				patternDate = getDefaultPatternDate();
				break;
			}
		}
		patternMap.put( "patternType", patternType );
		patternMap.put( "patternIndex", patternIndex );
		patternMap.put( "patternDate", patternDate );

		return patternMap;
	}

	public static java.util.Date getDefaultPatternDate() {
		try {
			return com.irt.data.Date.getInstance( "1900-01-01" );
		} catch( java.text.ParseException parseEx ) {
			return null;
		}
	}

	public static boolean validate( Map<String, ? extends Object> patternMap ) throws FieldException {
		return validate( patternMap, VALIDTYPES_ALL );
	}

	public static boolean validate( Map<String, ? extends Object> patternMap, String validTypeListValue ) throws FieldException {
		char patternType = ((String)fld_patternType.validate(patternMap)).charAt(0);
		int patternIndex = ((Number)fld_patternIndex.validate(patternMap)).intValue();
		java.util.Date patternDate = (java.util.Date)fld_patternDate.validate( patternMap );

		if( validTypeListValue.indexOf(patternType) < 0 )
			throw new FieldException( FieldException.ERR_INVALID_CHAR, fld_patternType, String.valueOf(patternType) );

		return validate( patternType, patternIndex, patternDate );
	}

	public static boolean validate( char patternType, int patternIndex, java.util.Date patternDate ) throws FieldException {
		switch( patternType ) {
		case TYPE_DATE:
			break;
		case TYPE_DAY_OF_MONTH:
			if( patternIndex < 0 || patternIndex > 31 )
				throw new FieldException( FieldException.ERR_INVALID_VALUE, fld_patternIndex, new Integer(patternIndex) );
			break;
		case TYPE_WEEK_OF_MONTH:
			if( patternIndex < 10 || patternIndex >= 60 )
				throw new FieldException( FieldException.ERR_INVALID_VALUE, fld_patternIndex, new Integer(patternIndex) );
			else if( (patternIndex % 10) < 1 || (patternIndex % 10) > 7 )
				throw new FieldException( FieldException.ERR_INVALID_VALUE, fld_patternIndex, new Integer(patternIndex) );
			break;
		case TYPE_DAYINTERVAL:
			if( patternIndex < 1 || patternIndex > 99 )
				throw new FieldException( FieldException.ERR_INVALID_VALUE, fld_patternIndex, new Integer(patternIndex) );
			break;
		case TYPE_DAY_OF_WEEK:
			if( patternIndex < 1 || patternIndex > 7 )
				throw new FieldException( FieldException.ERR_INVALID_VALUE, fld_patternIndex, new Integer(patternIndex) );
			break;
		case TYPE_EVERYDAY:
			break;
		case TYPE_EVERYHOUR:
		case TYPE_EVERYTIME:
			if( patternIndex < 0 || patternIndex > 59 )
				throw new FieldException( FieldException.ERR_INVALID_VALUE, fld_patternIndex, new Integer(patternIndex) );
			break;
		default:
			throw new FieldException( FieldException.ERR_INVALID_CHAR, fld_patternType, String.valueOf(patternType) );
		}

		return true;
	}
}
