/*
 *	File Name:	Timestamp.java
 *	Version:	2.2.3c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.3c	getIsoLocal(), getIsoUtc() 추가
 *	jbaek		2018/04/30		2.2.3c	getZone() 추가
 *	jbaek		2017/06/30		2.2.3	getInstance()에 formatPattern 인자 추가
 *	GimHS		2016/10/31		2.2.2	Java Document 주석 추가
 *	stghr12		2010/01/31		2.2.1	ceil(), floor(), getInstance() 추가
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.data;

import com.irt.util.cst.DateTimeUtil;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * java.sql.Timestamp Class를 상속받아 iRT Package 에서 사용할 Method를 추가 구현한 class.
 */
public class Timestamp extends java.sql.Timestamp {
	/** 1분. **/
	public final static int MINUTE						=	1;
	/** 5분. **/
	public final static int MINUTE_5					=	5;
	/** 10분. **/
	public final static int MINUTE_10					=	10;
	/** 15분. **/
	public final static int MINUTE_15					=	15;
	/** 20분. **/
	public final static int MINUTE_20					=	20;
	/** 30분. **/
	public final static int MINUTE_30					=	30;
	/** 1시간. **/
	public final static int HOUR							=	60;
	/** 2시간. **/
	public final static int HOUR_2						=	120;

	private long millis;
	private TimeZone zone;
	private String formatString;

	/**
	 * default TimeZone을 사용하고 현재 시간을 가지는 생성자 함수.
	 */
	public Timestamp() {
		this( (new java.util.Date()).getTime(), TimeZone.getDefault() );
	}

	/**
	 * TimeZone(zone)을 사용하고 현재 시간을 가지는 생성자 함수.
	 */
	public Timestamp( TimeZone zone ) {
		this( (new java.util.Date()).getTime(), zone );
	}

	/**
	 * default TimeZone을 사용하고 date(long) 시간을 가지는 생성자 함수.
	 */
	public Timestamp( long date ) {
		this( date, TimeZone.getDefault() );
	}

	/**
	 * TimeZone(zone)을 사용하고 date(long) 시간을 가지는 생성자 함수.
	 */
	public Timestamp( long date, TimeZone zone ) {
		super( date );
		this.millis = date;
		this.zone = zone;
		this.formatString = null;
	}

	/**
	 * default TimeZone을 사용하고 date(java.util.Date) 시간을 가지는 생성자 함수.
	 */
	public Timestamp( java.util.Date date ) {
		this( date.getTime(), TimeZone.getDefault() );
	}

	/**
	 * TimeZone(zone)을 사용하고 date(java.util.Date) 시간을 가지는 생성자 함수.
	 */
	public Timestamp( java.util.Date date, TimeZone zone ) {
		this( date.getTime(), zone );
	}

	/**
	 * minute 단위로 시간을 올림.
	 */
	public void ceil( int minute ) {
		round( minute, true );
	}

	/**
	 * minute 단위로 시간을 내림.
	 */
	public void floor( int minute ) {
		round( minute, false );
	}

	/**
	 * formatString(문자열, "yyyy-MM-dd HH:mm:ss")에 해당하는 Timestamp를 생성하여 return.
	 */
	public static com.irt.data.Timestamp getInstance( String formatString ) throws java.text.ParseException {
		return getInstance( formatString, TimeZone.getDefault() );
	}

	/**
	 * TimeZone(zone)을 사용하여 formatString(문자열, "yyyy-MM-dd HH:mm:ss")에 해당하는 Timestamp를 생성하여 return.
	 */
	public static com.irt.data.Timestamp getInstance( String formatString, TimeZone zone ) throws java.text.ParseException {
		DateFormat dateFormat;

		if( formatString.length() == 16 )
			dateFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd HH:mm" );
		else
			dateFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		dateFormat.setLenient( true );

		return new com.irt.data.Timestamp( dateFormat.parse(formatString).getTime(), zone );
	}

	/**
	 * formatString(문자열)을 formatPattern(패턴 문자열)을 사용하여 Timestamp를 생성하여 return.
	 */
	public static com.irt.data.Timestamp getInstance( String formatString, String formatPattern ) throws java.text.ParseException {
		return getInstance( formatString, formatPattern, TimeZone.getDefault() );
	}

	/**
	 * TimeZone(zone)을 사용하여 formatString(문자열)을  formatPattern(패턴 문자열)을 사용하여 Timestamp를 생성하여 return.
	 */
	public static com.irt.data.Timestamp getInstance( String formatString, String formatPattern, TimeZone zone ) throws java.text.ParseException {
		DateFormat dateFormat;

		dateFormat = new java.text.SimpleDateFormat(formatPattern);
		dateFormat.setLenient( true );

		return new com.irt.data.Timestamp( dateFormat.parse(formatString).getTime(), zone );
	}

	public TimeZone getZone() {
		return zone;
	}

	private void round( int minute, boolean up ) {
		Calendar calendar = Calendar.getInstance( zone );
		calendar.setTime( this );

		long millis =
			calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000
			+ calendar.get(Calendar.MINUTE) * 60 * 1000
			+ calendar.get(Calendar.SECOND) * 1000
			+ calendar.get(Calendar.MILLISECOND);
		long millis_unit = minute * 60 * 1000;
		long millis_rest = ( millis % millis_unit );

		if( millis_rest > 0 )
			setTime( this.millis - millis_rest + ( up ? millis_unit : 0 ) );
	}

	/**
	 * 시간을 formatString(문자열, "yyyy-MM-dd HH:mm:ss") 형태로 return.
	 */
	@Override
	public String toString() {
		long date = getTime();

		if( formatString == null || millis != date ) {
			DateFormat dateFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			dateFormat.setTimeZone( zone );

			millis = date;
			formatString = dateFormat.format( this );
		}

		return formatString;
	}

	public String getIsoDateTime() {
		return getIsoUtc();
	}

	public String getIsoLocal() {
		return getIsoDateString( zone );
	}

	public String getIsoUtc() {
		return getIsoDateString( TimeZone.getTimeZone("UTC") );
	}

	private DateFormat getIsoDateFormat( TimeZone zone ) {
		DateFormat dateFormat = new java.text.SimpleDateFormat( DateTimeUtil.ISO8601_SDF_PATTERN );
		dateFormat.setTimeZone( zone );
		return dateFormat;
	}

	private String getIsoDateString( TimeZone zone ) {
		DateFormat dateFormat = getIsoDateFormat(zone);

		return dateFormat.format(this);
	}
}
