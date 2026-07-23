/*
 *	File Name:	RBMDataHandler.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	extends com.irt.sql.BasicSQLHandler
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getTimeZone() 추가
 *	stghr12		2007/04/30		2.1.0	오타수정: INTERAL -> INTERNAL
 *	stghr12		2006/11/30		2.0.2	DBERR_CUSTOM_* message 처리 오류 수정
 *	stghr12		2006/07/19		2.0.1	DBERR_DEADLOCK_DETECTED 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.rbm;

import com.irt.data.DataException;
import com.irt.util.MessageHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 */
public class RBMDataHandler extends com.irt.sql.BasicSQLHandler {
	public final static int DBERR_UNIQUE_CONSTRAINT				= 1;
	public final static int DBERR_LOCK_NO_WAIT					= 54;
	public final static int DBERR_DEADLOCK_DETECTED				= 60;
	public final static int DBERR_CANNOT_INSERT_NULL			= 1400;
	public final static int DBERR_TOO_LARGE_INSERT				= 1401;
	public final static int DBERR_INVALID_NUMBER				= 1722;
	public final static int DBERR_CHECK_VIOLATED				= 2290;
	public final static int DBERR_PARENTKEY_NOTFOUND			= 2291;
	public final static int DBERR_CHILD_RECORD_FOUND			= 2292;

	// pkSYSStandard에 선언된 Error
	public final static int DBERR_CUSTOM_ERROR_CUSTOM			= 20000;
	public final static int DBERR_UNIQUE_CONSTRAINT_CUSTOM		= 20001;
	public final static int DBERR_CHECK_VIOLATED_CUSTOM			= 20290;
	public final static int DBERR_PARENTKEY_NOTFOUND_CUSTOM		= 20291;
	public final static int DBERR_CHILD_RECORD_FOUND_CUSTOM		= 20292;
	public final static int DBERR_CANNOT_INSERT_CUSTOM			= 20901;
	public final static int DBERR_CANNOT_UPDATE_CUSTOM			= 20902;
	public final static int DBERR_CANNOT_DELETE_CUSTOM			= 20903;
	public final static int DBERR_INVALID_VALUE_CUSTOM			= 20904;
	public final static int DBERR_INVALID_DATESCOPE_CUSTOM		= 20905;

	public RBMDataHandler( Connection conn, MessageHandler msghandler, TimeZone zone ) {
		super( conn, msghandler, zone );
	}

	public DataException createDataException( String errorCode, String message, Map recordMap ) {
		return new RBMDataException( errorCode, message, recordMap );
	}

	public DataException createDataException( SQLException sqlEx ) throws SQLException {
		return createDataException( sqlEx, null );
	}

	public DataException createDataException( SQLException sqlEx, Map recordMap ) throws SQLException {
		int intErr = sqlEx.getErrorCode();
		int idx_s, idx_e;
		String strerr, message;
		MessageHandler msghandler = getMessageHandler();

		switch( intErr ) {
		case DBERR_LOCK_NO_WAIT:
			message = msghandler.getMessageValue( RBMDataException.ERR_RECORD_LOCKED );
			return createDataException( RBMDataException.ERR_RECORD_LOCKED, message, recordMap );
		case DBERR_DEADLOCK_DETECTED:
			message = msghandler.getMessageValue( RBMDataException.ERR_DEADLOCK_DETECTED );
			return createDataException( RBMDataException.ERR_DEADLOCK_DETECTED, message, recordMap );
		case DBERR_UNIQUE_CONSTRAINT:
		case DBERR_UNIQUE_CONSTRAINT_CUSTOM:
			strerr = sqlEx.getMessage();
			idx_s = strerr.indexOf( "PIDX_" );
			if( idx_s < 0 ) idx_s = strerr.indexOf( "UIDX_" );

			if( idx_s >= 0 ) {
				idx_e = strerr.indexOf( ')', idx_s );
				if( idx_e < 0 ) idx_e = strerr.indexOf( '\n', idx_s );

				if( idx_e > 0 ) {
					// uniqueKey
					strerr = strerr.substring( idx_s, idx_e );
					try {
						message = msghandler.getMessageValue( "ERR_UQC_" + strerr );
						return createDataException( RBMDataException.ERR_UNIQUE_CONSTRAINT, message, recordMap );
					} catch( java.util.MissingResourceException misEx ) {}
				}
			}
			message = msghandler.getMessage( "ERR_UQC_PIDX_INTERNAL_ERROR", sqlEx.getMessage() );
			return createDataException( RBMDataException.ERR_UNIQUE_CONSTRAINT, message, recordMap );
		case DBERR_CANNOT_INSERT_NULL:
			message = msghandler.getMessage( RBMDataException.ERR_CANNOT_NULL +"_DB", sqlEx.getMessage() );
			return createDataException( RBMDataException.ERR_CANNOT_NULL, message, recordMap );
		case DBERR_TOO_LARGE_INSERT:
			message = msghandler.getMessage( RBMDataException.ERR_LARGE_VALUE +"_DB", sqlEx.getMessage() );
			return createDataException( RBMDataException.ERR_LARGE_VALUE, message, recordMap );
		case DBERR_INVALID_NUMBER:
			message = msghandler.getMessage( RBMDataException.ERR_INVALID_NUMBER +"_DB", sqlEx.getMessage() );
			return createDataException( RBMDataException.ERR_INVALID_NUMBER, message, recordMap );
		case DBERR_CHECK_VIOLATED:
		case DBERR_CHECK_VIOLATED_CUSTOM:
			strerr = sqlEx.getMessage();
			idx_s = strerr.indexOf( "CIDX_" );
			if( idx_s >= 0 ) {
				idx_e = strerr.indexOf( ')', idx_s );
				if( idx_e < 0 ) idx_e = strerr.indexOf( '\n', idx_s );

				if( idx_e > 0 ) {
					// checkKey
					strerr = strerr.substring( idx_s, idx_e );
					try {
						message = msghandler.getMessageValue( "ERR_CCV_" + strerr );
						return createDataException( RBMDataException.ERR_CHECK_VIOLATED, message, recordMap );
					} catch( java.util.MissingResourceException misEx ) {}
				}
			}
			message = msghandler.getMessage( "ERR_CCV_CIDX_INTERNAL_ERROR", strerr );
			return createDataException( RBMDataException.ERR_CHECK_VIOLATED, message, recordMap );
		case DBERR_PARENTKEY_NOTFOUND:
		case DBERR_PARENTKEY_NOTFOUND_CUSTOM:
		case DBERR_CHILD_RECORD_FOUND:
		case DBERR_CHILD_RECORD_FOUND_CUSTOM:
			String prefix, errorCode;
			if( intErr == DBERR_PARENTKEY_NOTFOUND || intErr == DBERR_PARENTKEY_NOTFOUND_CUSTOM ) {
				prefix = "ERR_PNF_";
				errorCode = RBMDataException.ERR_PARENTKEY_NOTFOUND;
			} else {
				prefix = "ERR_CRF_";
				errorCode = RBMDataException.ERR_CHILD_RECORD_FOUND;
			}

			strerr = sqlEx.getMessage();
			idx_s = strerr.indexOf( "FIDX_" );
			if( idx_s >= 0 ) {
				idx_e = strerr.indexOf( ')', idx_s );
				if( idx_e < 0 ) idx_e = strerr.indexOf( '\n', idx_s );

				if( idx_e > 0 ) {
					// foreignKey
					strerr = strerr.substring( idx_s, idx_e );
					try {
						message = msghandler.getMessageValue( prefix + strerr );
						return createDataException( errorCode, message, recordMap );
					} catch( java.util.MissingResourceException misEx ) {
						try {
							message = msghandler.getMessageValue( prefix + "FIDX_PUBLIC" + strerr.substring(strerr.lastIndexOf('_')) );
							return createDataException( errorCode, message, recordMap );
						} catch( java.util.MissingResourceException misEx2 ) {}
					}
				}
			}
			message = msghandler.getMessage( prefix +"FIDX_INTERNAL_ERROR", strerr );
			return createDataException( errorCode, message, recordMap );
		case DBERR_CUSTOM_ERROR_CUSTOM:
			message = RBMDataException.ERR_ERROR;
			break;
		case DBERR_CANNOT_INSERT_CUSTOM:
			message = RBMDataException.ERR_CANNOT_INSERT;
			break;
		case DBERR_CANNOT_UPDATE_CUSTOM:
			message = RBMDataException.ERR_CANNOT_UPDATE;
			break;
		case DBERR_CANNOT_DELETE_CUSTOM:
			message = RBMDataException.ERR_CANNOT_DELETE;
			break;
		case DBERR_INVALID_VALUE_CUSTOM:
			message = RBMDataException.ERR_INVALID_VALUE;
			break;
		case DBERR_INVALID_DATESCOPE_CUSTOM:
			message = RBMDataException.ERR_INVALID_DATESCOPE;
			break;
		default:
			throw sqlEx;
		}

		strerr = sqlEx.getMessage();
		if( (idx_s = strerr.indexOf(":")) >= 0 ) strerr = strerr.substring( idx_s+1 );
		if( (idx_e = strerr.indexOf("\n")) >= 0 ) strerr = strerr.substring( 0, idx_e );

		return createDataException( message, msghandler.getMessage("ERR_CST_"+ strerr.trim()), recordMap );
	}
}
