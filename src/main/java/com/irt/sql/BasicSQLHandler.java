/*
 *	File Name:	BasicSQLHandler.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.sql;

import com.irt.data.*;
import com.irt.sql.HierarchyCodeField;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryStorage;
import com.irt.util.MessageHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 */
public class BasicSQLHandler implements com.irt.sql.SQLHandler {
	Connection conn;
	MessageHandler msghandler;
	TimeZone zone;

	boolean debugging;
	QueryStorage queryStorage;

	public BasicSQLHandler( Connection conn, MessageHandler msghandler, TimeZone zone ) {
		this.conn = conn;
		this.msghandler = msghandler;
		this.zone = zone;
	}

	public void close() {
		try {
			conn.rollback();
		} catch( SQLException sqlEx ) {}

		try {
			conn.close();
			conn = null;
		} catch( SQLException sqlEx ) {}
	}

	public void commit() throws SQLException {
		conn.commit();
	}

	public DataException createDataException( String errorCode ) {
		return createDataException( errorCode, msghandler.getMessage(errorCode), null );
	}

	public DataException createDataException( String errorCode, Map recordMap ) {
		return createDataException( errorCode, msghandler.getMessage(errorCode), recordMap );
	}

	public DataException createDataException( String errorCode, String message ) {
		return createDataException( errorCode, message, null );
	}

	public DataException createDataException( String errorCode, String message, Map recordMap ) {
		return new DataException( errorCode, message, recordMap );
	}

	public DataException createDataException( FieldException fieldEx, Map recordMap ) {
		String errorKey = fieldEx.getErrorKey();
		AbstractField field = fieldEx.getErrorField();
		String message = null;
		if( field != null ) {
			String fieldName = msghandler.getMessage( field.getDescriptionKey() );

			if( FieldException.ERR_INVALID_LENGTH.equals(errorKey) ) {
				int length;
				if( field instanceof HierarchyCodeField )
					length = ((HierarchyCodeField)field).getLength( recordMap );
				else
					length = ((ValidableField)field).getMaxLength();
				if( length > 0 )
					message = msghandler.getMessage( errorKey, fieldName, String.valueOf(length) );
			}
			if( message == null )
				message = msghandler.getMessage( errorKey, fieldName );
		} else
			message = msghandler.getMessage( errorKey );

		return createDataException( errorKey, message, recordMap );
	}

	public DataException createDataException( SQLException sqlEx ) throws SQLException {
		throw sqlEx;
	}

	public DataException createDataException( SQLException sqlEx, Map recordMap ) throws SQLException {
		throw sqlEx;
	}

	public boolean debugging() {
		return debugging;
	}

	public void disableDebugging() {
		debugging = false;
	}

	public void enableDebugging() {
		debugging = true;
		if( queryStorage == null ) queryStorage = new QueryStorage();
	}

	public Connection getConnection() {
		return conn;
	}

	public MessageHandler getMessageHandler() {
		return msghandler;
	}

	public QueryStorage getSavedQuery() {
		return queryStorage;
	}

	public TimeZone getTimeZone() {
		return zone;
	}

	public void rollback() throws SQLException {
		conn.rollback();
	}

	public boolean saveQuery( QueryBuffer querybuf, long elapsedMilliTime ) {
		if( queryStorage != null ) {
			queryStorage.saveQuery( querybuf, elapsedMilliTime );
			return true;
		}
		return false;
	}
}
