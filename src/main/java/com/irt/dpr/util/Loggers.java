/*
 *	File Name:	Loggers.java
 *	Version:	2.2.1(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/11/30		2.2.1	client logger added.
 *	jbaek		2017/09/30		2.2.1	wm logger added.
 *	jbaek		2013/01/30		2.2.0	create
 *
**/

package com.irt.dpr.util;

import org.slf4j.Marker;

public interface Loggers {

	public static final String STR_BUSINESS = "[BIZ] ";
	public static final String STR_PERFORMANCE = "[PERF] ";
	public static final String STR_EXCWARN = "[EXCWARN] ";

	Marker ORDER_IF_RDD = org.slf4j.MarkerFactory.getMarker(com.irt.dpr.Order.ORDER_IF_RDD);
	Marker ORDER_IF_SIMULATION = org.slf4j.MarkerFactory.getMarker(com.irt.dpr.Order.ORDER_IF_SIMULATION);
	Marker ORDER_IF_CREATION = org.slf4j.MarkerFactory.getMarker(com.irt.dpr.Order.ORDER_IF_CREATION);
	Marker ORDER_IF_STATUSLIST = org.slf4j.MarkerFactory.getMarker(com.irt.dpr.Order.ORDER_IF_STATUSLIST);
	Marker ORDER_IF_STATUS = org.slf4j.MarkerFactory.getMarker(com.irt.dpr.Order.ORDER_IF_STATUS);
	Marker ORDER_IF_BILLING = org.slf4j.MarkerFactory.getMarker(com.irt.dpr.Order.ORDER_IF_BILLING);
	Marker TEMP_TRACE = org.slf4j.MarkerFactory.getMarker("TEMP_TRACE");

	org.slf4j.Logger business = org.slf4j.LoggerFactory.getLogger("business");
	org.slf4j.Logger wm = org.slf4j.LoggerFactory.getLogger("wm");
	org.slf4j.Logger client = org.slf4j.LoggerFactory.getLogger("client");
	org.slf4j.Logger client_sync = org.slf4j.LoggerFactory.getLogger("client.sync");
	org.slf4j.Logger fakewm = org.slf4j.LoggerFactory.getLogger("fakewm");
	org.slf4j.Logger excwarn = org.slf4j.LoggerFactory.getLogger("excwarn");
	org.slf4j.Logger ext = org.slf4j.LoggerFactory.getLogger("ext");

}
