/*
 *	File Name:	SQLHandler.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getTimeZone() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

import com.irt.data.FieldException;
import com.irt.data.DataException;
import com.irt.util.MessageHandler;
import java.sql.SQLException;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 */
public interface SQLHandler {
	public void close();

	public void commit() throws java.sql.SQLException;

	public DataException createDataException( String errorCode );

	public DataException createDataException( String errorCode, Map recordMap );

	public DataException createDataException( String errorCode, String message );

	public DataException createDataException( String errorCode, String message, Map recordMap );

	public DataException createDataException( FieldException fieldEx, Map recordMap );

	public DataException createDataException( SQLException sqlEx ) throws SQLException;

	public DataException createDataException( SQLException sqlEx, Map recordMap ) throws SQLException;

	public boolean debugging();

	public void disableDebugging();

	public void enableDebugging();

	public java.sql.Connection getConnection();

	public MessageHandler getMessageHandler();

	public QueryStorage getSavedQuery();

	public TimeZone getTimeZone();

	public void rollback() throws java.sql.SQLException;

	public boolean saveQuery( QueryBuffer querybuf, long elapsedMilliTime );
}
