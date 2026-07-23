/*
 *	File Name:	RddOrderSteps.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *		Usually related to OrderRevie and China country.
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/03/30		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.data.Date;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.SystemConfig;
import com.irt.sql.SQLHandler;
import com.irt.system.SessionManager;
import com.irt.util.MapUtil;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TimeZone;

public class RddOrderSteps implements OrderSteps.Rdd {

	private Logger logger = Logger.getLogger( RddOrderSteps.class );

	private SQLHandler handler;
	private SystemConfig systemConfig;
	public RddOrderSteps( SQLHandler handler, SystemConfig systemConfig ) {
		this.handler = handler;
		this.systemConfig = systemConfig;
	}

	public int getChinaOrderClosingPlusDays( java.time.ZonedDateTime currentZonedDateTime ) {
		Calendar calendar = zonedDateTime2Calendar( currentZonedDateTime );
		int ordDayOfWeek = calendar.get( Calendar.DAY_OF_WEEK );
		long orderTimeMillis = calendar.getTimeInMillis();

		calendar.set( Calendar.HOUR_OF_DAY, 0 );
		calendar.set( Calendar.MINUTE, 0 );
		calendar.set( Calendar.SECOND, 0 );
		long midnightMillis = calendar.getTimeInMillis();
		logger.debug( "getRequestDeliveryDate Midnight Time: " + calendar.getTime().toString() );

		int plusDays = 0;
		switch( ordDayOfWeek ) {
		case Calendar.MONDAY:
		case Calendar.TUESDAY:
		case Calendar.WEDNESDAY:
		case Calendar.THURSDAY:
			break;
		case Calendar.FRIDAY:
			logger.debug( "getRequestDeliveryDate Closing Time: " + new java.util.Date(midnightMillis + Order.CHINA_ORDERCLOSING_TIME) );
			logger.debug( "getRequestDeliveryDate Closing Time compare: " + ((midnightMillis + Order.CHINA_ORDERCLOSING_TIME) < orderTimeMillis) );
			if( ( midnightMillis + Order.CHINA_ORDERCLOSING_TIME ) < orderTimeMillis )
				plusDays = 3;
			break;
		case Calendar.SATURDAY:
			logger.debug( "getRequestDeliveryDate Closing Time: " + new java.util.Date(midnightMillis + Order.CHINA_ORDERCLOSING_TIME) );
			logger.debug( "getRequestDeliveryDate Closing Time compare: " + ((midnightMillis + Order.CHINA_ORDERCLOSING_TIME) < orderTimeMillis) );
			if( ( midnightMillis + Order.CHINA_ORDERCLOSING_TIME ) < orderTimeMillis )
				plusDays = 2;
			break;
		case Calendar.SUNDAY:
			logger.debug( "getRequestDeliveryDate Closing Time: " + new java.util.Date(midnightMillis + Order.CHINA_ORDERCLOSING_TIME) );
			logger.debug( "getRequestDeliveryDate Closing Time compare: " + ((midnightMillis + Order.CHINA_ORDERCLOSING_TIME) < orderTimeMillis) );
			if( ( midnightMillis + Order.CHINA_ORDERCLOSING_TIME ) < orderTimeMillis )
				plusDays = 1;
			break;
		}
		return plusDays;
	}

	@Override
	public com.irt.data.Date getRddByCalc( Map<String, Object> headerMap, String uniqId, TimeZone timeZone, Date defaultDateWhenSapNull )
			throws ServletModelException, SQLException, DataException {
		com.irt.data.Date inDateNow = getRddFromSap( headerMap, uniqId, timeZone, defaultDateWhenSapNull );

		return getRddByChinaOrderClosingTime( headerMap, inDateNow, timeZone );
	}

	@Override
	public com.irt.data.Date getRddByCalc( Map<String, Object> headerMap, String uniqId, TimeZone timeZone, int defaultDragDaysWhenSapNull )
			throws ServletModelException, SQLException, DataException {
		com.irt.data.Date inDateNow = getRddFromSap( headerMap, uniqId, timeZone, com.irt.data.Date.getInstance(timeZone).getDate(defaultDragDaysWhenSapNull) );

		return getRddByChinaOrderClosingTime( headerMap, inDateNow, timeZone );
	}

	@Override
	public com.irt.data.Date getRddByChinaOrderClosingTime( Map<String, Object> headerMap, com.irt.data.Date rddFromSap, TimeZone timeZone ) {
		return getRddByChinaOrderClosingTime( headerMap, java.time.ZonedDateTime.now(timeZone.toZoneId()), rddFromSap, timeZone );
	}

	public Calendar zonedDateTime2Calendar( java.time.ZonedDateTime zonedDateTime ) {
		java.time.Instant instant = zonedDateTime.toInstant();
		java.util.Date date = java.util.Date.from( instant );
		TimeZone timeZone = TimeZone.getTimeZone( zonedDateTime.getZone() );

		Calendar calendar = Calendar.getInstance( timeZone );
		calendar.setTime( date );
		return calendar;
	}

	/*
	 * Note: if useCalcOrderClosing( Usually China ) then use new ZonedDate. else then returns old way( com.irt.data.Date has timezone bug )
	 * */
	public com.irt.data.Date getRddByChinaOrderClosingTime( Map<String, Object> headerMap, java.time.ZonedDateTime currentZonedDateTime, com.irt.data.Date rddFromSap, TimeZone timeZone ) {
		boolean calculation = com.irt.rbm.RBMSystem.getSystemEnvBool( "DPR", "Default;ChinaRDDLogic", false );

		String organizationCode = (String)headerMap.get( "organizationCode" );

		if( calculation && Country.isFeature(organizationCode, "useCalcOrderClosing") ) {
			int plusDays = getChinaOrderClosingPlusDays( currentZonedDateTime );
			TimeZone zone = TimeZone.getTimeZone( currentZonedDateTime.getZone().getId() );
			com.irt.data.ZonedDate rddDate = com.irt.data.ZonedDate.getInstance( rddFromSap, zone );
			return rddDate.getDate( plusDays, zone );
		} else {
			return com.irt.data.Date.getInstance( rddFromSap );
		}
	}

	@Override
	public com.irt.data.Date getRddFromSap( Map<String, Object> headerMap, String uniqId, TimeZone timeZone, com.irt.data.Date defaultDateWhenSapNull ) throws DataException {
		String[] mandKeys = new String[] { "orderKey", "countryCode", "organizationCode", "distributionChannelCode", "divisionCode", "soldPartyCode" };
		if( !MapUtil.containAllKeysAndValueNotNull(headerMap, mandKeys) ) {
			throw handler.createDataException( DataException.ERR_CANNOT_NULL,
																				handler.getMessageHandler().getMessage(DataException.ERR_CANNOT_NULL, java.util.Arrays.asList(mandKeys).toString()), headerMap );
		}

		com.irt.dpr.tools.OrderCanonicalProcess ocp = new com.irt.dpr.tools.OrderCanonicalProcess( handler, systemConfig,
																																															com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_RDD );
		Map<String, Object> infMap = new HashMap<String, Object>();
		infMap.put( "orderKey", headerMap.get("orderKey") );
		infMap.put( "countryCode", headerMap.get("countryCode") );
		infMap.put( "organizationCode", headerMap.get("organizationCode") );
		infMap.put( "distributionChannelCode", headerMap.get("distributionChannelCode") );
		infMap.put( "divisionCode", headerMap.get("divisionCode") );
		infMap.put( "shipPartyCode", headerMap.get("shipPartyCode") );
		infMap.put( "updateUserId", uniqId );
		ocp.setParameter( infMap );

		com.irt.data.Date inDateNow = null;

		try {
			String inDateSapNowString = null;
			try {
				inDateSapNowString = ocp.execute();
			} catch( com.irt.dpr.OrderProcessException opEx ) {
				logger.error( opEx );
			}

			// sap(or webmethod) returns null
			if( inDateSapNowString == null || inDateSapNowString.length() == 0 ) {
				inDateNow = defaultDateWhenSapNull;
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
				sdf.setTimeZone( timeZone );
				inDateNow = com.irt.data.Date.getInstance( sdf.parse(inDateSapNowString) );
			}
		} catch( java.text.ParseException parseExIgnored ) {
		} finally {
			if( inDateNow == null ) {// maybe parse exceptions
				inDateNow = defaultDateWhenSapNull;
			}
		}

		return inDateNow;
	}

}
