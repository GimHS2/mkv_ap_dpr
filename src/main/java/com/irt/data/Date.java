/*
 *	File Name:	Date.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/12/30		2.2.1	ZonedDate를 구현하기 위해서 constructor를 protected로 변경
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
public class Date extends java.sql.Date {
	private long millis;
	private String formatString;

	protected Date( long date, String formatString ) {
		super( date );
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
	public com.irt.data.Date getDate( int days ) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();

		calendar.setTime( this );
		calendar.add( java.util.Calendar.DATE, days );

		return new com.irt.data.Date( calendar.getTimeInMillis(), null );
	}

	public String getDateWeek() {
		return (new Week(this)).toString();
	}

	public static DateFormat getDefaultDateFormat() {
		return new java.text.SimpleDateFormat( "yyyy-MM-dd" );
	}

	public static com.irt.data.Date getInstance() {
		return getInstance( new java.util.Date() );
	}

	public static com.irt.data.Date getInstance( TimeZone zone ) {
		return getInstance( new java.util.Date(), zone );
	}

	public static com.irt.data.Date getInstance( java.util.Date date ) {
		DateFormat dateFormat = getDefaultDateFormat();

		try {
			String formatString = dateFormat.format( date );
			return new com.irt.data.Date( dateFormat.parse(formatString).getTime(), formatString );
		} catch( java.text.ParseException parseEx ) {
			return null;
		}
	}

	public static com.irt.data.Date getInstance( java.util.Date date, TimeZone zone ) {
		DateFormat dateFormat = getDefaultDateFormat();
		dateFormat.setTimeZone( zone );

		try {
			return com.irt.data.Date.getInstance( dateFormat.format(date) );
		} catch( java.text.ParseException parseEx ) {
			return null;
		}
	}

	public static com.irt.data.Date getInstance( String formatString ) throws java.text.ParseException {
		if( formatString.length() == 8 )
			formatString = formatString.substring(0, 4) +"-"+ formatString.substring(4, 6) +"-"+ formatString.substring(6, 8);

		return new com.irt.data.Date( getDefaultDateFormat().parse(formatString).getTime(), formatString );
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
	 * defaultDateFormat.format(date)를 return.
	 */
	public static String toString( java.util.Date date ) {
		return getDefaultDateFormat().format( date );
	}

	public Week toWeek() {
		return new Week( this );
	}
}
