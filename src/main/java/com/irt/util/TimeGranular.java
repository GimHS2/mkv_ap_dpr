/*
 *	File Name:	TimeGranular.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Formatter:	eclipse
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/06/30		2.2.0c	create
 *
 **/

package com.irt.util;

import com.irt.util.cst.DateTimeUtil;

import java.text.DateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 *
 * deprecated use {@link com.irt.util.TimeTerm}
 */
public class TimeGranular {//@formatter:on

	public static final String DEFAULT_EVENTTIME_PATTERN = "yyyyMMddHHmmss";

	public static SimpleDateFormatSafe DEFAULT_EVENTTIME_FORMAT = new SimpleDateFormatSafe(DEFAULT_EVENTTIME_PATTERN,
			Locale.getDefault(), TimeZone.getDefault());

	private static final String PATTERN_DATE_STRING = "^\\d{4}\\-\\d{2}\\-\\d{2}$";

	private static final String PATTERN_DATETIME_STRING = "^\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}:\\d{2}:\\d{2}$";

	public static SimpleDateFormatSafe getEventTimeFormat( Locale locale, TimeZone zone ) {
		return new SimpleDateFormatSafe(DEFAULT_EVENTTIME_PATTERN, locale, zone);
	}

	public static SimpleDateFormatSafe getEventTimeFormat( TimeZone zone ) {
		return getEventTimeFormat(Locale.getDefault(), zone);
	}

	public static Map.Entry<Date, Date> getMinMaxDates( Collection<Date> dates ) {
		Date min = null;
		Date max = null;
		for( Date dt : dates ) {
			if( min != null && max != null ) {
				if( dt.before(min) ) {
					min = dt;
				}
				if( dt.after(max) ) {
					max = dt;
				}
			} else {
				if( min == null ) {
					min = dt;
				}
				if( max == null ) {
					max = dt;
				}
			}
		}

		return Util.createGranDate(min, max);
	}

	public static boolean isInclusive( Date eventTime, Date start, Date end ) {
		if( eventTime.before(end) && eventTime.after(start) ) {
			return true;
		} else {
			return false;
		}
	}

	/** static int field in {@link Calendar} like {@link Calendar#HOUR_OF_DAY} */
	private int calUnit = -1;
	private int eachAmount = -1;
	private int maxAmount = -1;

	private int offsetAmount = 0;

	private String baseTime = "now";

	public String getBaseTime() {
		if( "now".equals(baseTime) ) {
			com.irt.data.Timestamp ts = new com.irt.data.Timestamp(new Date());

			String isoLocalDateTime = ts.toString().replace(" ", "T");
			return isoLocalDateTime;
		}
		return baseTime;
	}

	public java.util.Date getBaseTimeInstance() throws java.text.ParseException {
		return com.irt.util.cst.DateTimeUtil.parseISODate(getBaseTime());
	}

	public int getCalUnit() {
		return calUnit;
	}

	public Date getConcreteDateLatest( final Date refDate ) {
		List<Date> dates = getConcreteDateList(refDate);
		return dates.get(0);
	}

	public List<Date> getConcreteDateList( final Date refDate ) {
		isValidOrThrow();

		// Date relativeLatest = dateFloor(refDate, calUnit, relativeAmount);
		Date relativeLatest = Util.dateAdd(refDate, calUnit, offsetAmount);
		if( calUnit == Calendar.DATE ) {
			relativeLatest = Util.removeTime(relativeLatest);
		}
		if( !relativeLatest.before(refDate) ) {
			throw new IllegalStateException();
		}

		int relativeAmount = ( eachAmount * maxAmount );

		Date pastFirst = Util.dateAdd(relativeLatest, calUnit, -( relativeAmount + offsetAmount ));

		if( Logger.getRootLogger().isEnabledFor(Level.TRACE) ) {
			Logger.getRootLogger().trace("debug:" + " refDate: " + refDate + " pastFirst: " + pastFirst + " relativeLatest: "
					+ relativeLatest + " relativeAmount: " + relativeAmount + " offsetAmount: " + offsetAmount);
		}

		List<Date> dates = Util.getFloorPrevDates(relativeLatest, pastFirst, calUnit, eachAmount);
		return dates;
	}

	public List<String> getConcreteDateStringList( final Date refDate, DateFormat fmt ) {
		List<Date> dates = getConcreteDateList(refDate);

		List<String> dateStrings = new java.util.ArrayList<String>();
		for( Date date : dates ) {
			dateStrings.add(fmt.format(date));
		}
		return dateStrings;
		// return dates.stream().map(x -> fmt.format(x)).collect(java.util.streams.Collectors.toList());
	}

	public int getEachAmount() {
		return eachAmount;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public Map.Entry<Date, Date> getMinMaxDates( final Date refDate ) {
		List<Date> dates = getConcreteDateList(refDate);
		Date min = null;
		Date max = null;
		for( Date dt : dates ) {
			if( min != null && max != null ) {
				if( dt.before(min) ) {
					min = dt;
				}
				if( dt.after(max) ) {
					max = dt;
				}
			} else {
				if( min == null ) {
					min = dt;
				}
				if( max == null ) {
					max = dt;
				}
			}
		}

		return Util.createGranDate(min, max);
	}

	public int getOffsetAmount() {
		return offsetAmount;
	}

	/** date higest to lowest. For example, from today to past some day. */
	public List<java.util.Map.Entry<Date, Date>> granulizeBackword( List<Date> dates ) {
		return Util.granulize(dates, false);
	}

	/** date lowest to highest. For example, from past some day to today. */
	public List<java.util.Map.Entry<Date, Date>> granulizeForward( List<Date> dates ) {
		return Util.granulize(dates, true);
	}

	public boolean isValid() {
		return ( ( calUnit == -1 ) || ( eachAmount == -1 ) || ( maxAmount == -1 ) ) ? false : true;
	}

	private boolean isValidBaseTimeString( String baseTime ) {
		if( baseTime == null ) {
			return false;
		} else if( "now".equals(baseTime) ) {
			return true;
		} else if( baseTime.matches(PATTERN_DATE_STRING) || baseTime.matches(PATTERN_DATETIME_STRING) ) {
			return true;
		}

		return false;
	}

	public boolean isValidOrThrow() {
		if( calUnit == -1 ) {
			throw new ShouldNotNull("should set 'calUnit' field.");
		}
		if( eachAmount == -1 ) {
			throw new ShouldNotNull("should set 'eachAmount' field.");
		}
		if( maxAmount == -1 ) {
			throw new ShouldNotNull("should set 'maxAmount' field.");
		}
		return true;
	}

	public void setBaseTime( String baseTime ) {
		if( !isValidBaseTimeString(baseTime) ) {
			throw new IllegalStateException("invalid BaseTimeString. baseTime: " + baseTime);
		}

		this.baseTime = baseTime;
	}

	/**
	 * @param calUnit
	 *            : static int field in {@link Calendar} like {@link Calendar#HOUR_OF_DAY} {@link Calendar#MINUTE}
	 */
	public void setCalUnit( int calUnit ) {
		this.calUnit = calUnit;
	}

	/**
	 *
	 * @param eachAmount
	 *            : amount by the {@link #calUnit}
	 */
	public void setEachAmount( int eachAmount ) {
		this.eachAmount = eachAmount;
	}

	/**
	 *
	 * @param maxAmount
	 *            : max amount by the {@link #calUnit}
	 */
	public void setMaxAmount( int maxAmount ) {
		this.maxAmount = maxAmount;
	}

	public void setOffsetAmount( int offsetAmount ) {
		this.offsetAmount = offsetAmount;
	}

	@Override
	public String toString() {
		return "TimeGranular{" + "calUnit: " + calUnit + " eachAmount: " + eachAmount + " maxAmount: " + maxAmount
				+ " offsetAmount: " + offsetAmount + " baseTime: " + baseTime + "}";
	}

	private class ShouldNotNull extends RuntimeException {
		public ShouldNotNull( String message ) {
			super(message);
		}
	}

	public static class Util {

		public static Map.Entry<Date, Date> createGranDate( Date key, Date value ) {
			return new AbstractMap.SimpleEntry<Date, Date>(key, value) {
				@Override
				public String toString() {
					return getGranDateString(this.getKey(), this.getValue());
				}
			};
		}

		public static Date dateAdd( final Date date, int calUnit, int amount ) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(calUnit, amount);
			return cal.getTime();
		}

		public static Date dateCeil( final Date input, int calUnit, int amount ) {
			return dateRoundOff(input, calUnit, amount, true);
		}

		/** round by date */
		public static Date dateRoundOff( final Date input, int calUnit, int amount, boolean up ) {
			// Date date = (Date)input.clone();

			Date date = removeTime((Date)input.clone());

			Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			long millis =
					// cal.get(Calendar.DATE) * 24 * 60 * 60 * 1000 +
					cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 + cal.get(Calendar.MINUTE) * 60 * 1000
							+ cal.get(Calendar.SECOND) * 1000 + cal.get(Calendar.MILLISECOND);

			if( Calendar.HOUR == calUnit ) {
				throw new UnsupportedOperationException(
						"Please use 24 hour base Calendar.HOUR_OF_DAY instead because Calendar.HOUR is 12hour base.");
			}

			long millis_amount = 0;
			long millis_unit = 0;

			if( Calendar.HOUR_OF_DAY == calUnit ) {
				millis_amount = amount * 60 * 60 * 1000;
				millis_unit = 60 * 60 * 1000;
			} else if( Calendar.MINUTE == calUnit ) {
				millis_amount = amount * 60 * 1000;
				millis_unit = 60 * 1000;
			} else if( Calendar.SECOND == calUnit ) {
				millis_amount = amount * 1000;
				millis_unit = 1000;
			} else if( Calendar.MILLISECOND == calUnit ) {
				millis_amount = 1;
				millis_unit = 1;
			} else {
				if( Calendar.DATE == calUnit ) {
					millis_amount = amount * 24 * 60 * 60 * 1000;
					millis_unit = 24 * 60 * 60 * 1000;
				} else {
					throw new UnsupportedOperationException(
							"Supporting calUnit: HOUR_OF_DAY, MINUTE, SECOND, MILLISECOND" + " experimental: "
									+ " DATE ");
				}
			}
			long millis_oneDay = java.util.concurrent.TimeUnit.DAYS.toMillis(1);
			if( millis == millis_oneDay ) {
				throw new IllegalStateException();
			}

			long millis_rest = 0;
			millis_rest = ( millis % millis_unit );

			if( up ) {
				date.setTime(cal.getTimeInMillis() + millis_amount - millis_rest + millis_oneDay);
			} else {
				date.setTime(cal.getTimeInMillis() + millis_amount - millis_rest - millis_oneDay);
				;
			}

			return date;
		}

		public static Date deprecatedDateFloor( final Date input, int calUnit, int amount ) {
			return dateRoundOff(input, calUnit, amount, false);
		}

		public static String getDateString( java.util.Date date ) {
			if( date == null )
				return null;

			return DEFAULT_EVENTTIME_FORMAT.format(date);
		}

		/**
		 * eg. if calUnit is minute then remove second part from date.
		 */
		public static Date getFloorByEachAmount( final Date refDate, int calUnit, int eachAmount ) {
			long unitMillis = DateTimeUtil.getUnitMillisByCalUnit(calUnit);
			long eachMillis = unitMillis * eachAmount;
			long refTime = refDate.getTime();
			long floorTime = DateTimeUtil.floor(refTime, eachMillis);

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(floorTime);
			return cal.getTime();
		}

		public static List<Date> getFloorPrevDates( final Date refDate, final Date pastFirst, int calUnit,
				int eachAmount ) {
			if( refDate.before(pastFirst) ) {
				throw new RuntimeException(
						"base should be after pastFirst but base: " + refDate + " pastFirst: " + pastFirst);
			}

			if( calUnit == Calendar.DATE ) {
				if( !refDate.equals(removeTime(refDate)) ) {
					throw new IllegalStateException(
							" refDate: " + refDate + " refDate(w/o time):" + Util.removeTime(refDate));
				}
			}

			// Date refDate_floor = dateFloor(refDate, calUnit, eachAmount);
			Date refDate_floor = getFloorByEachAmount(refDate, calUnit, eachAmount);
			if( calUnit == Calendar.DATE ) {
				// if calUnit is date(day) then remove times to make midnight timing.
				// else if calUnit is time then floor the time amount to smaller granuler
				refDate_floor = removeTime(refDate_floor);

				// if (!refDate_floor.equals(removeTime(refDate_floor))) {
				// throw new IllegalStateException(
				// " refDate: " + refDate + " refDate(w/o time):" + Util.removeTime(refDate) + " refDate_floor: "+ refDate_floor);
				// }
			}

			List<Date> dates = new ArrayList<Date>();
			dates.add(refDate_floor);

			while( pastFirst.before(dates.get(dates.size() - 1)) ) {
				Date newRefDate = dates.get(dates.size() - 1);
				Date pastByAmount = dateAdd(newRefDate, calUnit, -eachAmount);
				if( pastByAmount.after(newRefDate) ) {
					throw new IllegalStateException("calculation is wrong. newRefDate:" + newRefDate + " pastByAmount: " + pastByAmount);
				}

				if( Logger.getRootLogger().isEnabledFor(Level.TRACE) ) {
					Logger.getRootLogger().trace(" refDate_floor: " + refDate_floor + " newRefDate: " + newRefDate
							+ " pastByAmount: " + pastByAmount + " ");
				}
				dates.add(pastByAmount);
			}

			return dates;
		}

		public static String getGranDatesString( Collection<Map.Entry<Date, Date>> granDates ) {
			StringBuffer sbuf = new StringBuffer();
			for( Map.Entry<Date, Date> entry : granDates ) {
				sbuf.append("\t");
				sbuf.append(getGranDateString(entry));
			}
			return sbuf.toString();
		}

		public static String getGranDateString( Date first, Date second ) {
			if( first == null || second == null ) {
				return null;
			}

			boolean directionFoward = false;
			if( first.before(second) ) {
				directionFoward = true;
			}

			if( directionFoward ) {
				return "GranDate{" + "direct: " + "forward" + " from: " + getDateString(first) + " to: " + getDateString(second) + "}";
			} else {
				return "GranDate{" + "direct: " + "backward" + " from: " + getDateString(second) + " to: " + getDateString(first) + "}";
			}
		}

		public static String getGranDateString( Map.Entry<Date, Date> granDate ) {
			if( granDate == null ) {
				return null;
			}
			return getGranDateString(granDate.getKey(), granDate.getValue());
		}

		public static String getTimeString( long timeDifference1 ) {
			long timeDifference = timeDifference1 / 1000;
			int h = (int)( timeDifference / ( 3600 ) );
			int m = (int)( ( timeDifference - ( h * 3600 ) ) / 60 );
			int s = (int)( timeDifference - ( h * 3600 ) - m * 60 );

			return String.format("%02d:%02d:%02d", h, m, s);
		}

		public static List<java.util.Map.Entry<Date, Date>> granulize( List<Date> dates, boolean forward ) {
			List<java.util.Map.Entry<Date, Date>> granDates = new ArrayList<java.util.Map.Entry<Date, Date>>();

			if( forward ) {
				for( int i = dates.size() - 1; i > 0; i-- ) {
					java.util.Map.Entry<Date, Date> entry = createGranDate(dates.get(i), dates.get(i - 1));

					granDates.add(entry);
				}
			} else {
				for( int i = 0; i < dates.size() - 1; i++ ) {
					java.util.Map.Entry<Date, Date> entry = createGranDate(dates.get(i), dates.get(i + 1));

					granDates.add(entry);
				}
			}

			return granDates;
		}

		public static Date removeTime( Date date ) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime();
		}

	}
}
