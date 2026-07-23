/*
 *	File Name:	DateTimeUtil.java
 *	Version:	2.2.1c
 *
 *	Description:
 *
 *	Note:
 *
 *	Formatter:	eclipse
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.1c	jaxb 의존성 제거하고 java.time 패키지 사용
 *	jbaek		2020/06/30		2.2.0c	create
 *
**/

package com.irt.util.cst;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 */
public class DateTimeUtil {//@formatter:on
	/**
	 * Usage: String upgFieldName = String.format(FIELD_FORMAT_ISO8601_FROM_ORACLE_DBTIMESTAMP, "SOME_DB_TIMESTAMP_COLUMN_NAME")
	 * Caution: DONT USE THIS FUNCTION ON DatabaseTimeZone is in DaylightSavingTimeZone.
	 * Because SYSTIMESTAMP gives only refer to the current( if the referenced date is past date you are in trouble )
	 */
	public static final String FIELD_FORMAT_ISO8601_FROM_ORACLE_DBTIMESTAMP = "TO_CHAR(%s, 'YYYY-MM-DD\"T\"HH24:MI:SSTZH:TZM') ";

	/**
	 * Usage: String upgFieldName = String.format(FIELD_FORMAT_ISO8601_FROM_ORACLE_DBDATE, "SB.UPGDATE")
	 * Caution: DONT USE THIS FUNCTION ON DatabaseTimeZone is in DaylightSavingTimeZone.
	 * Because SYSTIMESTAMP gives only refer to the current( if the referenced date is past date you are in trouble )
	 * )
	 */
	public static final String FIELD_FORMAT_ISO8601_FROM_ORACLE_DBDATE = "TO_CHAR(%s, 'YYYY-MM-DD\"T\"HH24:MI:SS') || TO_CHAR(SYSTIMESTAMP,'TZH:TZM')";
//	public static final String FIELD_FORMAT_ISO8601_FROM_ORACLE_DBDATE = "DECODE(TO_CHAR(SYSTIMESTAMP,'TZH:TZM'), TO_CHAR(%s, 'YYYY-MM-DD\"T\"HH24:MI:SS') || TO_CHAR(SYSTIMESTAMP,'TZH:TZM'), NULL)";

	public static final String ISO8601_ORACLE_PATTERN = "YYYY-MM-DD\"T\"HH24:MI:SSTZH:TZM";

	/** timezone 'Z': RFC 822 format. timezone 'XXX'(from jdk1.7?): ISO 8601 format but */
	public static final String ISO8601_SDF_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

	public static final long UNITMS_MILLISECOND = (long)( 1 );
	public static final long UNITMS_SECOND = (long)( 1000 );
	public static final long UNITMS_MINUTE = ( UNITMS_SECOND * 60 );
	public static final long UNITMS_HOUR = ( UNITMS_MINUTE * 60 );
	public static final long UNITMS_DAY = ( UNITMS_HOUR * 24 );
	public static final double ROUND_BY_HALF = 0.5;
	public static final double ROUND_BY_CEIL = 1.00;
	public static final double ROUND_BY_FLOOR = 0;

	public synchronized static Date ceil( Date date, long UNITMS ) {
		return new Date(ceil(date.getTime(), UNITMS));
	}

	public synchronized static long ceil( long timeMillis, long UNITMS ) {
		return increaseByUnit(truncateByUnit(timeMillis, UNITMS), UNITMS);
	}

	public static long decreaseByUnit( long timeMillis, long UNITMS ) {
		return ( timeMillis - UNITMS );
	}

	public static long decreaseByUnit( long timeMillis, long UNITMS, int unitAmount ) {
		if( unitAmount == 0 ) {
			return timeMillis;
		} else {
			return ( timeMillis - ( UNITMS * unitAmount ) );
		}
	}

	public synchronized static Date floor( Date date, long UNITMS ) {
		return new Date(floor(date.getTime(), UNITMS));
	}

	public synchronized static long floor( long timeMillis, long UNITMS ) {
		return truncateByUnit(timeMillis, UNITMS);
	}

	/**
	 * <pre>
	 * dateString should be following format.
	 *
	 * 2018-04-01T19:30:00Z // UTC
	 * </pre>
	 */
	public static Date fromISO8601UTC( String dateStr ) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);

		try {
			return df.parse(dateStr);
		} catch( ParseException e ) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Caution: DONT USE THIS FUNCTION ON DatabaseTimeZone is in DaylightSavingTimeZone.
	 * (Date Does not have the timezone info.
	 * And SYSTIMESTAP does not have the past time info.)
	 *
	 */
	public static String getFieldNameFromOracleDbDateColumn( String dbdateColumnName ) {
		return String.format(FIELD_FORMAT_ISO8601_FROM_ORACLE_DBDATE, dbdateColumnName);

	}

	public static java.sql.Timestamp getNowUTCTimestamp() {
		Calendar sysCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));// java's system calendar

		return new java.sql.Timestamp(sysCal.getTimeInMillis());
	}

	/**
	 * Calendar.MILLISECOND = 14
	 * Calendar.SECOND = 13
	 * Calendar.MINUTE = 12
	 * Calendar.HOUR_OF_DAY= 11
	 * Calendar.DATE = 5
	 */
	public static long getUnitMillisByCalUnit( int calUnit ) {
		switch( calUnit ) {
		case Calendar.MILLISECOND:
			return 1;
		case Calendar.SECOND:
			return UNITMS_SECOND;
		case Calendar.MINUTE:
			return UNITMS_MINUTE;
		case Calendar.HOUR_OF_DAY:
			return UNITMS_HOUR;
		case Calendar.DATE:
			return UNITMS_DAY;
		default:
			throw new IllegalArgumentException("unsupported calendar unit: " + calUnit);
		}
	}

	public static long increaseByUnit( long timeMillis, long UNITMS ) {
		return ( timeMillis + UNITMS );
	}

	public static long increaseByUnit( long timeMillis, long UNITMS, int unitAmount ) {
		if( unitAmount == 0 ) {
			return timeMillis;
		} else {
			return ( timeMillis + ( UNITMS * unitAmount ) );
		}
	}

	/**
	 * <pre>
	 * dateString can be following three flexible format.
	 *
	 * 2018-04-01T18:00:00+08:00 // UTC+8
	 *
	 * 2018-04-01T19:30:00Z // UTC
	 *
	 * 2018-04-01T18:00:00 // Local( Jvm System TimeZone )
	 * </pre>
	 *
	 */
	public static Date parseISODate( final String dateString ) throws java.text.ParseException {
		try {
			if( dateString != null ) {
				return DateTimeUtil.parseDateTime( dateString );
			} else {
				throw new IllegalArgumentException("dateString is mandatory.");
			}
		} catch( java.time.format.DateTimeParseException | IllegalArgumentException argEx ) {
			throw new java.text.ParseException("illegal ISO_8601 Date String: '" + dateString + "'", 0);
		}
	}

	/* simulate javax.xml.bind.DatatypeConverter.parseDateTime */
	private static Date parseDateTime( String dateTimeString ) throws java.time.format.DateTimeParseException {
		try {
			java.time.OffsetDateTime offsetDateTime = java.time.OffsetDateTime.parse( dateTimeString );
			return java.util.Date.from( offsetDateTime.toInstant() );
		} catch( java.time.format.DateTimeParseException parseEx1 ) {
			try {
				java.time.Instant instantDateTime = java.time.Instant.parse( dateTimeString );
				return java.util.Date.from( instantDateTime );
			} catch( java.time.format.DateTimeParseException parseEx2 ) {
				try {
					java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse( dateTimeString );
					return java.util.Date.from( localDateTime.atZone( java.time.ZoneId.systemDefault() ).toInstant() );
				} catch( java.time.format.DateTimeParseException parseEx3 ) {
					String errMessage = String.format( "Text '%s' could not be parsed using ISO_OFFSET_DATE_TIME, ISO_INSTANT, ISO_LOCAL_DATE_TIME", dateTimeString );
					throw new java.time.format.DateTimeParseException( errMessage, dateTimeString, 0 );
				}
			}
		}
	}

	/**
	 * <pre>
	 * dateString return can be following format.
	 *
	 * 2018-04-01T18:00:00+08:00 // UTC+8
	 *
	 * 2018-04-01T19:30:00Z // UTC
	 * </pre>
	 */
	public static String printISODate( final Calendar cal ) {
		return java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.format( java.time.Instant.ofEpochMilli(cal.getTimeInMillis()) );
	}

	/**
	 * <pre>
	 * dateString return can be following format.
	 *
	 * 2018-04-01T18:00:00+08:00 // UTC+8
	 *
	 * 2018-04-01T19:30:00Z // UTC
	 * </pre>
	 */
	public static String printISODate( final Date date, final TimeZone zone ) {
		Calendar cal = Calendar.getInstance(zone);
		cal.setTime(date);
		return printISODate(cal);
	}

	/**
	 * <pre>
	 * dateString return can be following format.
	 *
	 * 2018-04-01T18:00:00+08:00 // UTC+8
	 *
	 * 2018-04-01T19:30:00Z // UTC
	 * </pre>
	 */
	public static String printISODateLocal( final Date date ) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return printISODate(cal);
	}

	public synchronized static Date round( Date date, long UNITMS ) {
		long time = date.getTime();
		long timeOutOfUnit = time % UNITMS;

		time -= timeOutOfUnit;
		if( timeOutOfUnit >= ( UNITMS / 2 ) ) {
			time += UNITMS;
		}

		return new Date(time);
	}

	public synchronized static Date roundBy( Date date, long UNITMS, double roundPercentValue ) {
		long time = date.getTime();
		long timeOutOfUnit = time % UNITMS;

		time -= timeOutOfUnit;
		if( roundPercentValue == 0 ) {
			return floor(date, UNITMS);

		} else if( roundPercentValue == 1 ) {
			return ceil(date, UNITMS);
		} else if( roundPercentValue < 0 || roundPercentValue > 1 ) {
			throw new IllegalArgumentException("roundPercentValue should be in 0 to 1 range. roundPercentValue: " + roundPercentValue);
		} else {
			if( timeOutOfUnit >= ( UNITMS * ( 1 * roundPercentValue ) ) ) {
				time += UNITMS;
			}
		}

		return new Date(time);
	}

	/**
	 * <pre>
	 * dateString return is following format.
	 *
	 * 2018-04-01T19:30:00Z // UTC
	 * </pre>
	 */
	public static String toISO8601UTC( Date date ) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);
		return df.format(date);
	}

	public static long truncateByUnit( long timeMillis, long UNITMS ) {
		long timeOutOfUnit = timeMillis % UNITMS;
		return ( timeMillis - timeOutOfUnit );
	}
}
