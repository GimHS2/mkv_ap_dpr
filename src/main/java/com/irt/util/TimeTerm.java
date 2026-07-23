/*
 *	File Name:	TimeTerm.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Formatter:	eclipse
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0c	create
 *
 **/

package com.irt.util;

import com.irt.util.cst.DateTimeUtil;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * Supports below patterns :
 * 
 * <pre>
 * now-90days
 * now
 * now-9minutes
 * now-2seconds
 * 9hours
 * </pre>
 *
 */
public class TimeTerm {
	public static final String ISO8601_PATTERN = "(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:Z|[+-][01]\\d:?[0-5]\\d)";
	private static final String NOW_PATTERN = "[nN][oO][wW]";

	private static final String TIME_DEFINITION_PATTERN = "(^" + NOW_PATTERN + "|^" + ISO8601_PATTERN + "|^" + ")"
			+ "([-|+])?([0-9]+)?(\\w+)?$";

	public static long getDaysMillis( int duration ) {
		return TimeUnit.DAYS.toMillis(duration);
	}

	public static long getHoursMillis( int duration ) {
		return TimeUnit.HOURS.toMillis(duration);
	}

	public static long getMinutesMillis( int duration ) {
		return TimeUnit.MINUTES.toMillis(duration);
	}

	public static long getSecondsMillis( int duration ) {
		return TimeUnit.SECONDS.toMillis(duration);
	}

	private String timeDef;
	// private Pattern p = Pattern.compile("([nN][oO][wW])?([-|+])?([0-9]+)?(\\w+)?");
	private Pattern p = Pattern.compile(TIME_DEFINITION_PATTERN);
	private Matcher m;
	private final int NOW = 1;

	private final int SIGN = 2;

	private final int NUM = 3;

	private final int UNIT = 4;

	boolean trim;

	public TimeTerm( String timeDef, boolean trim ) {
		this.timeDef = timeDef;
		this.m = p.matcher(timeDef);
		m.find();
		this.trim = trim;
	}

	public long compileDuration() {
		if( definedUnit() != null )
			return compileDuration(compileUnit());
		else
			return 0;
	}

	private long compileDuration( TimeUnit unit ) {
		long milliDuration = TimeUnit.MILLISECONDS.convert(compileNum(), unit);

		if( "-".equals(definedSign()) ) {
			return -milliDuration;
		}

		return milliDuration;
	}

	public long trimTime( long base, TimeUnit unit ) {
		if( unit == null )
			return base;
		if( unit.equals(TimeUnit.DAYS) )
			return com.irt.data.Date.getInstance(new com.irt.data.Timestamp(base)).getTime();

		long rest = 0;

		switch( unit ) {
		case MICROSECONDS:
		case MILLISECONDS:
			long millis = TimeUnit.MILLISECONDS.toMillis(base) - ( TimeUnit.MILLISECONDS.toSeconds(base) * 1000 );
			rest += millis;
		case SECONDS:
			long second = TimeUnit.MILLISECONDS.toSeconds(base) - ( TimeUnit.MILLISECONDS.toMinutes(base) * 60 );
			rest += TimeUnit.SECONDS.toMillis(second);
		case MINUTES:
			long minute = TimeUnit.MILLISECONDS.toMinutes(base) - ( TimeUnit.MILLISECONDS.toHours(base) * 60 );
			rest += TimeUnit.MINUTES.toMillis(minute);
		case HOURS:
			long hours = TimeUnit.MILLISECONDS.toHours(base) - ( TimeUnit.MILLISECONDS.toDays(base) * 24 );
			rest += TimeUnit.HOURS.toMillis(hours);
		case DAYS:
			long days = TimeUnit.MILLISECONDS.toDays(base);
			rest += TimeUnit.DAYS.toMillis(days);
		}

		return rest;
	}

	public long compileNow() {
		if( isDefinedNowIsAbsolute() ) {
			try {
				return DateTimeUtil.parseISODate(definedNow()).getTime();
			} catch( ParseException parseEx ) {
				throw new IllegalArgumentException("timeDef: '" + timeDef + "' definedNow: '" + definedNow() + "'", parseEx);
			}
		} else {
			return System.currentTimeMillis();
		}
	}

	private boolean isDefinedNowIsAbsolute() {
		if( ( definedNow() == null || definedNow().length() == 0 )
				|| "NOW".equalsIgnoreCase(definedNow()) ) {
			return false;
		}

		return true;
	}

	public long compileNum() {
		try {
			return Long.parseLong(m.group(NUM), 10);
		} catch( NumberFormatException nfe ) {
			return 0;
		}
	}

	public TimeUnit compileUnit() {
		if( definedUnit() != null ) {
			try {
				return TimeUnit.valueOf(definedUnit().toUpperCase());
			} catch( IllegalArgumentException ex ) {
				throw new IllegalArgumentException("timeDef: '" + timeDef + "' definedUnit: '" + definedUnit() + "'", ex);
			}
		}

		return null;
	}

	public String definedNow() {
		return m.group(NOW) == null ? null : m.group(NOW).toUpperCase();
	}

	public String definedNum() {
		return m.group(NUM);
	}

	public String definedSign() {
		return m.group(SIGN);
	}

	public String definedTimeDef() {
		return timeDef;
	}

	private String definedUnit() {
		return m.group(UNIT);
	}

	public TimeUnit getLowerTimeUnit( TimeUnit unit ) {
		switch( unit ) {
		case DAYS:
			return TimeUnit.HOURS;
		case HOURS:
			return TimeUnit.MINUTES;
		case MINUTES:
			return TimeUnit.SECONDS;
		case SECONDS:
			return TimeUnit.MILLISECONDS;
		case MILLISECONDS:
			return TimeUnit.MICROSECONDS;
		case MICROSECONDS:
			return TimeUnit.NANOSECONDS;
		default:
			throw new UnsupportedOperationException("No lower TimeUnit for " + unit);
		}
	}

	public long compileTime() {
		return compileTime(System.currentTimeMillis());
	}

	/** if 'now' is used recompile */
	public long compileTime( long base ) {
		if( isDefinedNowIsAbsolute() ) {
			return compileTime(compileNow(), false);
		} else {
			return compileTime(base, false);
		}
	}

	private long compileTime( long rebase, boolean throwsAbsolute ) {
		if( throwsAbsolute && isDefinedNowIsAbsolute() )
			throw new IllegalArgumentException("definedNow is absolute value(" + definedNow() + "). cannot compile time. Please correct ");

		if( trim ) {
			return trimTime(rebase, compileUnit()) + compileDuration();
		} else {
			return rebase + compileDuration();
		}
	}

	public boolean isValid() {
		return m.matches();
	}

	@Override
	public String toString() {
		return timeDef;
	}
}
