/*
 *	File Name:	DatabaseResourceReloadable.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0c	create
 *
**/


package com.irt.resbdl;

import java.sql.SQLException;
import java.util.Locale;

public interface DatabaseResourceReloadable {

	public interface ColumnResourceCheckable {
		public Long findColResNewestTimestamp( String partyId, Locale locale ) throws SQLException;
	}

	public interface MessageResourceCheckable {
		public Long findNewestTimestamp( String bundleName ) throws SQLException;
	}

}
