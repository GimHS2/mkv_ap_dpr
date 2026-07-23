/*
 *	File Name:	ScheduleProcessHandler.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/08/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.sql.SQLHandler;

/**
 *
 */
public interface ScheduleProcessHandler {
	public Process getInstance( SQLHandler handler, String scheduleCode, String scheduleName, String extraValue ) throws ProcessException;
}
