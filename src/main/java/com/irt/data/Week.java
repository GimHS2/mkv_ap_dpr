/*
 *	File Name:	Week.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/07/31		2.2.3	getFirstdayOfWeek(), getWeeksOfMonth() 추가
 *	stghr12		2009/04/30		2.2.2	firstdayOfWeek 추가
 *	stghr12		2008/08/25		2.2.1	getFirstWeekOfMonth(), getMonthValue(), getWeekOfMonth() 추가
 *	stghr12		2007/11/30		2.2.0	addWeek() -> addWeeks(), addYear() -> addYears(), getFirstDay() -> getFirstday()
 *										Week( zone ), Week( date, zone ) 추가
 *	stghr12		2007/04/30		2.1.0	create
 *
**/

package com.irt.data;

import java.util.Calendar;
import java.util.TimeZone;

public class Week implements Comparable {
	public final static char NORMAL						= 'N';
	public final static char MONTH445					= 'W';
	public final static int FIRSTDAY_OF_WEEK			= Calendar.MONDAY;

	private int year, week, firstdayOfWeek;
	private Calendar firstDay;

	public Week() {
		this( Calendar.getInstance(), FIRSTDAY_OF_WEEK );
	}

	public Week( int firstdayOfWeek ) {
		this( Calendar.getInstance(), firstdayOfWeek );
	}

	public Week( String weekValue ) {
		this( weekValue, FIRSTDAY_OF_WEEK );
	}

	public Week( String weekValue, int firstdayOfWeek ) {
		try {
			this.year = Integer.parseInt( weekValue.substring(0, 4) );
			this.week = Integer.parseInt( weekValue.substring(4, 6) );
			this.firstdayOfWeek = firstdayOfWeek;
		} catch( IndexOutOfBoundsException idxEx ) {
			throw new IllegalArgumentException( "illegal weekValue '"+ weekValue +"'." );
		} catch( NumberFormatException numEx ) {
			throw new IllegalArgumentException( "illegal weekValue '"+ weekValue +"'." );
		}

		this.firstDay = getFirstday( this.year, this.week, this.firstdayOfWeek );
	}

	public Week( Calendar calendar ) {
		this( calendar, FIRSTDAY_OF_WEEK );
	}

	public Week( Calendar calendar, int firstdayOfWeek ) {
		this.firstdayOfWeek = firstdayOfWeek;
		setCalendar( calendar );
	}

	public Week( TimeZone zone ) {
		this( Calendar.getInstance(zone), FIRSTDAY_OF_WEEK );
	}

	public Week( TimeZone zone, int firstdayOfWeek ) {
		this( Calendar.getInstance(zone), firstdayOfWeek );
	}

	public Week( java.util.Date date ) {
		this( date, FIRSTDAY_OF_WEEK );
	}

	public Week( java.util.Date date, int firstdayOfWeek ) {
		this.firstdayOfWeek = firstdayOfWeek;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime( date );

		setCalendar( calendar );
	}

	public Week( java.util.Date date, TimeZone zone ) {
		this( date, zone, FIRSTDAY_OF_WEEK );
	}

	public Week( java.util.Date date, TimeZone zone, int firstdayOfWeek ) {
		this.firstdayOfWeek = firstdayOfWeek;

		Calendar calendar = Calendar.getInstance( zone );
		calendar.setTime( date );

		setCalendar( calendar );
	}

	public Week( int year, int week ) {
		this( year, week, FIRSTDAY_OF_WEEK );
	}

	public Week( int year, int week, int firstdayOfWeek ) {
		this.year = year;
		this.week = week;
		this.firstdayOfWeek = firstdayOfWeek;
		this.firstDay = getFirstday( this.year, this.week, this.firstdayOfWeek );
	}

	public void addWeeks( int weeks ) {
		setWeek( week + weeks );
	}

	public void addYears( int years ) {
		setYear( year + years );
	}

	public boolean after( Week weekObj ) {
		if( this.year > weekObj.year )
			return true;
		else if( this.year == weekObj.year )
			return( this.week > weekObj.week );
		else
			return false;
	}

	public boolean before( Week weekObj ) {
		if( this.year > weekObj.year )
			return false;
		else if( this.year == weekObj.year )
			return( this.week < weekObj.week );
		else
			return true;
	}

	public Object clone() {
		return new Week( firstDay, firstdayOfWeek );
	}

	public int compareTo( Object obj ) {
		if( obj instanceof String )
			return getWeekValue().compareTo( (String)obj );
		else
			return compareTo( (Week)obj );
	}

	public int compareTo( Week weekObj ) {
		return compareTo( weekObj.year, weekObj.week );
	}

	public int compareTo( String weekValue ) {
		try {
			return compareTo(
				Integer.parseInt(weekValue.substring(0, 4))
				, Integer.parseInt(weekValue.substring(4, 6))
			);
		} catch( IndexOutOfBoundsException idxEx ) {
			throw new IllegalArgumentException( "illegal weekValue '"+ weekValue +"'." );
		} catch( NumberFormatException numEx ) {
			throw new IllegalArgumentException( "illegal weekValue '"+ weekValue +"'." );
		}
	}

	public int compareTo( int year, int week ) {
		int diff = 0;
		if( this.year == year )
			diff = 0;
		else if( this.year > year ) {
			do {
				diff += getWeeksOfYear(year) + 1 - week;
				year++;
				week = 1;
			} while( this.year > year );
		} else {
			do {
				diff -= week;
				year--;
				week = getWeeksOfYear( year );
			} while( this.year < year );
		}
		return diff + this.week - week;
	}

	public boolean equals( Object obj ) {
		if( obj == this ) return true;
		if( !(obj instanceof Week) ) return false;

		return( this.year == ((Week)obj).year && this.week == ((Week)obj).week );
	}

	public Calendar getFirstday() {
		return (Calendar)firstDay.clone();
	}

	public int getFirstdayOfWeek() {
		return firstdayOfWeek;
	}

	public static Calendar getFirstday( int year, int week ) {
		return getFirstday( year, week, FIRSTDAY_OF_WEEK );
	}

	public static Calendar getFirstday( int year, int week, int firstdayOfWeek ) {
		Calendar firstDay = Calendar.getInstance();
		firstDay.setFirstDayOfWeek( firstdayOfWeek );
		firstDay.setMinimalDaysInFirstWeek( 4 );

		firstDay.set( year, 0, 1 );
		if( firstDay.get(Calendar.WEEK_OF_YEAR) != 1 ) {
			while( firstdayOfWeek != firstDay.get(Calendar.DAY_OF_WEEK) )
				firstDay.add( Calendar.DATE, 1 );
		} else {
			while( firstdayOfWeek != firstDay.get(Calendar.DAY_OF_WEEK) )
				firstDay.add( Calendar.DATE, -1 );
		}
		firstDay.add( Calendar.DATE, week * 7 - 7 );

		return firstDay;
	}

	public static Week getFirstWeekOfMonth( String monthValue, char calendarType ) {
		return getFirstWeekOfMonth( monthValue, calendarType, FIRSTDAY_OF_WEEK );
	}

	public static Week getFirstWeekOfMonth( String monthValue, char calendarType, int firstdayOfWeek ) {
		java.util.Date date;

		try {
			if( monthValue == null || monthValue.length() != 6 )
				throw new IllegalArgumentException( "illegal monthValue '"+ monthValue +"'." );

			date = (new java.text.SimpleDateFormat("yyyyMMdd")).parse( monthValue + "04" );
		} catch( java.text.ParseException parseEx ) {
			throw new IllegalArgumentException( "illegal monthValue '"+ monthValue +"'." );
		}

		switch( calendarType ) {
		case NORMAL:
			return new Week( date, firstdayOfWeek );
		case MONTH445:
			if( firstdayOfWeek != Calendar.MONDAY )
				throw new IllegalArgumentException( "firstdayOfWeek must be MONDAY." );

			int year = Integer.parseInt( monthValue.substring(0, 4) );
			int month = Integer.parseInt( monthValue.substring(4, 6) ) - 1;

			return new Week( year, month * 4 + (int)(month/3) + 1 );
		default:
			throw new IllegalArgumentException( "illegal calendarType '"+ calendarType +"'." );
		}
	}

	public Calendar getLastday() {
		Calendar lastDay = (Calendar)firstDay.clone();
		lastDay.add( Calendar.DATE, 6 );

		return lastDay;
	}

	public String getMonthValue( char calendarType ) {
		switch( calendarType ) {
		case NORMAL:
			Calendar centeralDay = (Calendar)firstDay.clone();
			centeralDay.add( Calendar.DATE, 3 );

			return (new java.text.SimpleDateFormat("yyyyMM")).format( centeralDay.getTime() );
		case MONTH445:
			if( firstdayOfWeek != Calendar.MONDAY )
				throw new IllegalArgumentException( "firstdayOfWeek must be MONDAY." );

			int month = (int)( (week-1)/13 ) * 3;
			if( month < 12 ) {
				if( week%13 == 0 )
					month += 3;
				else
					month += ((week-1)%13) / 4 + 1;
			}

			StringBuffer sbuf = new StringBuffer();
			if( year < 1000 ) {
				if( year < 10 )
					sbuf.append( "000" + year );
				else if( year < 100 )
					sbuf.append( "00" + year );
				else
					sbuf.append( "0" + year );
			} else
				sbuf.append( year );
			if( month < 10 )
				sbuf.append( "0" + month );
			else
				sbuf.append( month );

			return sbuf.toString();
		default:
			throw new IllegalArgumentException( "illegal calendarType '"+ calendarType +"'." );
		}
	}

	public int getWeek() {
		return week;
	}

	public int getWeekOfMonth( char calendarType ) {
		switch( calendarType ) {
		case NORMAL:
			Calendar centeralDay = (Calendar)firstDay.clone();
			centeralDay.add( Calendar.DATE, 3 );

			return centeralDay.get( Calendar.WEEK_OF_MONTH );
		case MONTH445:
			if( firstdayOfWeek != Calendar.MONDAY )
				throw new IllegalArgumentException( "firstdayOfWeek must be MONDAY." );

			int week13 = week%13;
			if( week > 52 )
				return 6;
			else if( week13 == 0 )
				return 5;
			else
				return (week13 - 1)%4 + 1;
		default:
			throw new IllegalArgumentException( "illegal calendarType '"+ calendarType +"'." );
		}
	}

	public static int getWeeksOfMonth( int year, int month ) {
		return getWeeksOfMonth( year, month, FIRSTDAY_OF_WEEK );
	}

	public static int getWeeksOfMonth( int year, int month, int firstdayOfWeek ) {
		Calendar calendar = Calendar.getInstance();

		calendar.setFirstDayOfWeek( firstdayOfWeek );
		calendar.setMinimalDaysInFirstWeek( 4 );
		calendar.set( year, month - 1, 1 );

		return calendar.getActualMaximum( Calendar.WEEK_OF_MONTH );
	}

	public static int getWeeksOfMonth( String monthValue ) {
		return getWeeksOfMonth( monthValue, FIRSTDAY_OF_WEEK );
	}

	public static int getWeeksOfMonth( String monthValue, int firstdayOfWeek ) {
		try {
			int year = Integer.parseInt( monthValue.substring(0, 4) );
			int month = Integer.parseInt( monthValue.substring(4, 6) );

			return getWeeksOfMonth( year, month, firstdayOfWeek );
		} catch( IndexOutOfBoundsException idxEx ) {
			throw new IllegalArgumentException( "illegal monthValue '"+ monthValue +"'." );
		} catch( NumberFormatException numEx ) {
			throw new IllegalArgumentException( "illegal monthValue '"+ monthValue +"'." );
		}
	}

	public static int getWeeksOfYear( int year ) {
		return getWeeksOfYear( year, FIRSTDAY_OF_WEEK );
	}

	public static int getWeeksOfYear( int year, int firstdayOfWeek ) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek( firstdayOfWeek );
		cal.setMinimalDaysInFirstWeek( 4 );

		cal.set( year, 11, 31 );
		int weeks = cal.get( Calendar.WEEK_OF_YEAR );
		if( weeks == 1 ) {
			cal.add( Calendar.DATE, -7 );
			weeks = cal.get( Calendar.WEEK_OF_YEAR );
		}

		return weeks;
	}

	public String getWeekValue() {
		StringBuffer sbuf = new StringBuffer();

		if( year < 1000 ) {
			if( year < 10 )
				sbuf.append( "000" + year );
			else if( year < 100 )
				sbuf.append( "00" + year );
			else
				sbuf.append( "0" + year );
		} else
			sbuf.append( year );
		if( week < 10 )
			sbuf.append( "0" + week );
		else
			sbuf.append( week );

		return sbuf.toString();
	}

	public int getYear() {
		return year;
	}

	public void setCalendar( Calendar calendar ) {
		this.firstDay = (Calendar)(calendar.clone());
		this.firstDay.setFirstDayOfWeek( this.firstdayOfWeek );
		this.firstDay.setMinimalDaysInFirstWeek( 4 );

		while( this.firstdayOfWeek != this.firstDay.get(Calendar.DAY_OF_WEEK) )
			this.firstDay.add( Calendar.DATE, -1 );

		this.year = this.firstDay.get( Calendar.YEAR );
		this.week = this.firstDay.get( Calendar.WEEK_OF_YEAR );
		int month = this.firstDay.get( Calendar.MONTH );
		if( this.week == 1 && month == 11 )
			this.year++;
		else if( this.week >= 52 && month == 0 )
			this.year--;
	}

	public void setWeek( int week ) {
		firstDay.add( Calendar.DATE, (week - this.week) * 7 );

		this.week = firstDay.get( Calendar.WEEK_OF_YEAR );
		if( this.week != week ) {
			this.year = firstDay.get( Calendar.YEAR );
			int month = firstDay.get( Calendar.MONTH );
			if( this.week == 1 && month == 11 )
				this.year++;
			else if( this.week >= 52 && month == 0 )
				this.year--;
		}
	}

	public void setYear( int year ) {
		this.year = year;

		if( week > 52 && week > getWeeksOfYear(year) )
			week = getWeeksOfYear(year);

		firstDay = getFirstday( year, week, firstdayOfWeek );
	}

	public String toString() {
		return getWeekValue();
	}
}
