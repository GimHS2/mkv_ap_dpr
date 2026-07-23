/*
 *	File Name:	ZonedDate.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/12/30	2.2.1	기존 com.irt.data.Date 와 다르게 getInstance(Date, TimeZone) 시에 기본 timeZone을 따라가지 않고 timeZone 파라미터의 존을 따름.
 *	stghr12		2007/11/30		2.2.0	dateFormat 제거, Timestamp와 분리
 *										addDays() -> getDate(): Calendar 이용
 *	stghr12		2007/07/31		2.1.2	java.sql.Date extends -> java.util.Date extend : Timestamp 지원
 *	stghr12		2007/04/30		2.1.1	getDateMonth(), getDateWeek(), toWeek() 추가
 *	stghr12		2006/12/01		2.1.0	java.util.Date extends -> java.sql.Date extends
 *										addDays() 추가
 *	stghr12		2006/09/01		2.0.1	DateFormat 동기화 오류 수정
 *										equals() 구현
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

import java.text.DateFormat;
import java.util.TimeZone;

/**
 *
 */
public class ZonedDate extends com.irt.data.Date {
	private long millis;
	private String formatString;

	private ZonedDate( long date, String formatString ) {
		super( date, formatString );
		this.millis = date;
		this.formatString = formatString;
	}

	public boolean equals( Object obj ) {
		if( obj instanceof String )
			return toString().equals( obj );
		else
			return super.equals( obj );
	}

	public String getDateMonth() {
		return (new java.text.SimpleDateFormat("yyyyMM")).format( this );
	}

	/**
	 * days만큼 더해진 새로운 com.irt.data.Date를 생성하여 return.
	 */
	public com.irt.data.ZonedDate getDate( int days ) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();

		calendar.setTime( this );
		calendar.add( java.util.Calendar.DATE, days );

		return new com.irt.data.ZonedDate( calendar.getTimeInMillis(), null );
	}


	public com.irt.data.ZonedDate getDate( int days, TimeZone timeZone ) {
		java.util.Calendar calendar = java.util.Calendar.getInstance( timeZone );

		calendar.setTime( this );
		calendar.add( java.util.Calendar.DATE, days );

		return new com.irt.data.ZonedDate( calendar.getTimeInMillis(), null );
	}

	public String getDateWeek() {
		return (new Week(this)).toString();
	}

	public static DateFormat getDefaultDateFormat() {
		return new java.text.SimpleDateFormat( "yyyy-MM-dd" );
	}

	public static com.irt.data.ZonedDate getInstance() {
		return getInstance( new java.util.Date() );
	}

	public static com.irt.data.ZonedDate getInstance( TimeZone zone ) {
		return getInstance( new java.util.Date(), zone );
	}

	public static com.irt.data.ZonedDate getInstance( java.util.Date date ) {
		DateFormat dateFormat = getDefaultDateFormat();

		try {
			String formatString = dateFormat.format( date );
			return new com.irt.data.ZonedDate( dateFormat.parse(formatString).getTime(), formatString );
		} catch( java.text.ParseException parseEx ) {
			return null;
		}
	}

	public static com.irt.data.ZonedDate getInstance( java.util.Date date, TimeZone zone ) {
		DateFormat dateFormat = getDefaultDateFormat();
		dateFormat.setTimeZone( zone );

		try {
			return com.irt.data.ZonedDate.getInstance( dateFormat.format(date), zone );
		} catch( java.text.ParseException parseEx ) {
			return null;
		}
	}

	public static com.irt.data.ZonedDate getInstance( String formatString ) throws java.text.ParseException {
		return getInstance( formatString, TimeZone.getDefault() );
	}

	public static com.irt.data.ZonedDate getInstance( String formatString, TimeZone zone ) throws java.text.ParseException {
		if( formatString.length() == 8 )
			formatString = formatString.substring(0, 4) +"-"+ formatString.substring(4, 6) +"-"+ formatString.substring(6, 8);

		DateFormat dateFormat = getDefaultDateFormat();
		dateFormat.setTimeZone( zone );

		return new com.irt.data.ZonedDate( dateFormat.parse(formatString).getTime(), formatString );
	}

	public String toString() {
		long date = getTime();

		if( formatString == null || millis != date ) {
			millis = date;
			formatString = getDefaultDateFormat().format( this );
		}

		return formatString;
	}

	/**
	 * defaultDateFormat.format(date)쨍짝 return.
	 */
	public static String toString( java.util.Date date ) {
		return getDefaultDateFormat().format( date );
	}

	public Week toWeek() {
		return new Week( this );
	}
}
