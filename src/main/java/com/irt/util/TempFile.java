/*
 *	File Name:	TempFile.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2013/08/30		2.2.0	create
 *
**/

package com.irt.util;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * FileGarbageCollector를 통해서 자동 삭제 가능한 TempFile 생성.
 */
public class TempFile extends File {
	private Date d = null;

	/**
	 * The default expiry time is 1 day
	 */
	private Date getDefaultExpiry() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, 1);
		return c.getTime();
	}

	/**
	 * Constructor from super class
	 */
	public TempFile( String pathname ) {
		super(pathname);
		setExpiry(getDefaultExpiry());
	}

	/**
	 * Constructors with expiry parameter
	 */
	public TempFile( String pathname, Date expiry ) {
		super(pathname);
		setExpiry(expiry);
	}

	/**
	 * Let the user set the expiry attribute after construction of object
	 */
	public void setExpiry( Date d ) {
		this.d = d;
		FileGarbageCollector collector = FileGarbageCollector.getInstance();
		collector.addFile(this);
	}

	public Date getExpiry() {
		return this.d;
	}
}
