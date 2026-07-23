/*
 *	File Name:	SimpleDateFormatSafe.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.0c	create
 *
**/

package com.irt.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Thread Safe SimpleDateFormat
 */
public class SimpleDateFormatSafe {//@formatter:on

	private final ThreadLocal<SimpleDateFormat> localSimpleDateFormat;

	public SimpleDateFormatSafe( final String pattern, final Locale locale, final TimeZone timeZone ) {
		localSimpleDateFormat = new ThreadLocal<SimpleDateFormat>() {
			protected SimpleDateFormat initialValue() {
				SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, locale);
				dateFormat.setTimeZone(timeZone);
				return dateFormat;
			}
		};
	}

	public Object clone() {
		return localSimpleDateFormat.get().clone();
	}

	public boolean equals( Object obj ) {
		return localSimpleDateFormat.get().equals(obj);
	}

	public String format( Date time ) {
		return localSimpleDateFormat.get().format(time);
	}

	public int hashCode() {
		return localSimpleDateFormat.get().hashCode();
	}

	public Date parse( String source ) throws ParseException {
		return localSimpleDateFormat.get().parse(source);
	}

	public String toString() {
		return localSimpleDateFormat.get().toString();
	}
}
