/*
 *	File Name:	SessionMng.java
 *	Version:	2.2.3c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/09/29		2.2.3c	changePassword(): HASH_ALGORITHM 추가
 *										login(): HASH_ALGORITHM 에 따른 로그인 처리 기능 추가
 *	jbaek		2020/06/30		2.2.3c	Properties 추가. userEmail 추가.
 *	jbaek		2019/11/30		2.2.3c	User 기본 조직 기능 추가
 *	jbaek		2014/12/31		2.2.3	updateLastAccess(): userAgent 추가
 *	lsinji		2008/09/26		2.2.2	getGroupClass() 추가, isCountryAdmin() 추가
 *										comparePassword() 호출 하도록 변경
 *										SessionManagerException.TURNOVER_DISMATCHING_PASSWORD 추가
 *										USR_USER_SECURITY, USR_SESSION_FAILLOG 관련 처리 추가
 *										failCount >= 5 이면 USR_USER.STATUS = STATUS_LOCKED로 변경
 *										DEFAULT_SESSION_TIME = 30
 *										Session Time Over 버그 수정
 *	stghr12		2008/03/31		2.2.1	updateLastAccess(): elapsedTimeMilli 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getTimeZone() 추가, TimeZone 처리 추가
 *										vwUSR_USER 변경사항 적용: SERV_START/SERV_END -> SERV_START_IND/SERV_END_IND
 *	stghr12		2007/10/31		2.1.1	isAdminUser() 추가
 *	stghr12		2006/12/01		2.1.0	getPartyGln() -> getGln()
 *										getUserExtraValue() 추가
 *										login( partyId, userId, killuser ) 추가
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/09		1.0.0	version 관리
 *	stghr12		2002/04/15				create
 *
**/

package com.irt.rbm;

import com.irt.data.DataException;
import com.irt.rbm.usr.UserUser;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.system.SessionManagerException;

import java.sql.*;
import java.util.*;

/**
	* 사용자와 Login/Logout등을 처리하는 Class
	*/
public class SessionMng implements com.irt.system.SessionManager {
	private final static int DEFAULT_LOGIN_TIME			= 24 * 60;	// 하루
	private final static int DEFAULT_SESSION_TIME		= 90;		// 30 minute

	public final static String STATUS_NORMAL			= "00";
	public final static String STATUS_PASSWORD_EXPIRED	= "PW";
	public final static String STATUS_LOCKED			= "LK";

	public final static String STATUS_LOGIN				= "00";
	public final static String STATUS_LOGOUT			= "LO";
	public final static String STATUS_KILL				= "LK";

	public final static String GROUPCLASS_ADMIN			= "AD";
	public final static String GROUPCLASS_MASTER		= "MR";
	public final static String GROUPCLASS_ORDER			= "OR";
	public final static String GROUPCLASS_DEFAULT		= "BA";

	private SQLHandler handler;
	private boolean useEffectUser;
	private String sessionId, requestIP;
	private int maximumSessionTime, maximumLoginTime;
	private String partyId, partyClass, partyName, groupId, groupClass;
	private String uniqId, userId, userName, userClass;
	private String[] userExtraValues;
	private String effectGln, gln;
	private TimeZone zone;
	private Properties property;

	public SessionMng( String sessionId, String requestIP, SQLHandler handler ) throws SQLException {
		this( sessionId, requestIP, handler, DEFAULT_SESSION_TIME, DEFAULT_LOGIN_TIME );
	}

	public SessionMng( String sessionId, String requestIP, SQLHandler handler, int maximumSessionTime ) throws SQLException {
		this( sessionId, requestIP, handler, maximumSessionTime, DEFAULT_LOGIN_TIME );
	}

	public SessionMng( String sessionId, String requestIP, SQLHandler handler, int maximumSessionTime, int maximumLoginTime ) throws SQLException {
		this.handler = handler;
		this.useEffectUser = false;
		this.sessionId = sessionId;
		this.requestIP = requestIP;
		this.maximumSessionTime = maximumSessionTime;
		this.maximumLoginTime = maximumLoginTime;
		this.property = new Properties();
		initSessionInfo( null );
	}

	@Override
	public boolean changePassword( String partyId, String userId, String password, String newPassword ) throws SessionManagerException {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try {
			pstmt = handler.getConnection().prepareStatement( "SELECT * FROM vwUSR_USER WHERE PARTYID = ? AND USERID = ?" );
			pstmt.setString( 1, partyId );
			pstmt.setString( 2, userId );
			rset = pstmt.executeQuery();
			if( !rset.next() ) return false;

			if( !com.irt.rbm.usr.UserUser.comparePassword(password, rset.getString("PASSWORD"), rset.getString("HASH_ALGORITHM")) ) {
				this.partyId = partyId;
				this.userId = userId;
				throw new SessionManagerException( SessionManagerException.INVALID_PASSWORD );
			}
			initSessionInfo( rset );

			return( SQLManager.executeStatement(handler,
				"UPDATE USR_USER SET PASSWORD = ?, HASH_ALGORITHM = ?, STATUS = DECODE(STATUS, 'PW', '00', STATUS), UPGDATE = SYSDATE WHERE PARTYID = ? AND USER_ID = ?"
			, com.irt.rbm.usr.UserUser.getEncryptionPassword(newPassword), UserUser.hashAlgorithm, partyId, userId ) > 0 );
		} catch( DataException dataEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( dataEx );
		} catch( SQLException sqlEx ) {
			throw new SessionManagerException( sqlEx );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	@Override
	public void checkAuthorize( String systemCode ) throws SessionManagerException {
		if( systemCode == null ) return;
		if( sessionId == null )
			throw new SessionManagerException( SessionManagerException.NO_SESSIONID );
		if( isSystemAdmin() ) return;

		try {
			systemCode = (String)SQLManager.getObjectValue( handler,
				"SELECT PS.SYSTEMCD FROM vwUSR_PARTY_SERV PS WHERE PS.PARTYID = ? AND PS.SYSTEMCD = ?"
			, partyId, systemCode );
			if( systemCode == null )
				throw new SessionManagerException( SessionManagerException.HAS_NOAUTH_PARTY );

			systemCode = (String)SQLManager.getObjectValue( handler,
				"SELECT UA.SYSTEMCD FROM vwUSR_USER_AUTH UA WHERE UA.PARTYID = ? AND UA.USERID = ? AND UA.SYSTEMCD = ?"
			, partyId, userId, systemCode );
			if( systemCode == null )
				throw new SessionManagerException( SessionManagerException.HAS_NOAUTH_USER );
		} catch( SQLException sqlEx ) {
			throw new SessionManagerException( sqlEx );
		}
	}

	@Override
	public void checkAuthorize( String systemCode, String packageCode ) throws SessionManagerException {
		checkAuthorize( systemCode, packageCode, AUTHMODE_SELF );
	}

	@Override
	public void checkAuthorize( String systemCode, String packageCode, int authMode ) throws SessionManagerException {
		if( systemCode == null ) return;
		if( sessionId == null )
			throw new SessionManagerException( SessionManagerException.NO_SESSIONID );
		if( packageCode == null || isSystemAdmin() ) return;

		String packageSQL;
		switch( authMode ) {
		case AUTHMODE_AT_LEAST_ONE:
			packageSQL = "SELECT PKG_CD FROM SYS_SYSTEM_PKG "
					+ " CONNECT BY PRIOR SYSTEMCD = SYSTEMCD AND PRIOR PKG_CD = PARENTPKGCD "
					+ " START WITH SYSTEMCD = ? AND PKG_CD = ? "
				+ " UNION SELECT PKG_CD FROM SYS_SYSTEM_PKG "
					+ " CONNECT BY PRIOR SYSTEMCD = SYSTEMCD AND PRIOR PARENTPKGCD = PKG_CD "
					+ " START WITH SYSTEMCD = ? AND PKG_CD = ? ";
			break;
		case AUTHMODE_SELF:
		default:
			packageSQL = "SELECT PKG_CD FROM SYS_SYSTEM_PKG "
					+ " CONNECT BY PRIOR SYSTEMCD = SYSTEMCD AND PRIOR PARENTPKGCD = PKG_CD "
					+ " START WITH SYSTEMCD = ? AND PKG_CD = ? ";
			break;
		}

		// vwUSR_PARTY_SERV 검사
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try {
			int idx = 1;
			pstmt = handler.getConnection().prepareStatement(
				" SELECT * FROM vwUSR_PARTY_SERV PS, ("+ packageSQL +") P WHERE PS.PARTYID = ? AND PS.SYSTEMCD = ? AND PS.PKGCD = P.PKG_CD "
			);
			pstmt.setString( idx++, systemCode );
			pstmt.setString( idx++, packageCode );
			if( authMode == AUTHMODE_AT_LEAST_ONE ) {
				pstmt.setString( idx++, systemCode );
				pstmt.setString( idx++, packageCode );
			}
			pstmt.setString( idx++, partyId );
			pstmt.setString( idx++, systemCode );
			rset = pstmt.executeQuery();
			if( !rset.next() )
				throw new SessionManagerException( SessionManagerException.HAS_NOAUTH_PARTY );
		} catch( SQLException sqlEx ) {
			throw new SessionManagerException( sqlEx );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}

		// vwUSR_USER_AUTH 검사
		rset = null;
		try {
			int idx = 1;
			pstmt = handler.getConnection().prepareStatement(
				" SELECT * FROM vwUSR_USER_AUTH UA, (" + packageSQL + ") P "
					+ " WHERE UA.PARTYID = ? AND UA.USERID = ? AND UA.SYSTEMCD = ? AND UA.PKGCD = P.PKG_CD "
			);
			pstmt.setString( idx++, systemCode );
			pstmt.setString( idx++, packageCode );
			if( authMode == AUTHMODE_AT_LEAST_ONE ) {
				pstmt.setString( idx++, systemCode );
				pstmt.setString( idx++, packageCode );
			}
			pstmt.setString( idx++, partyId );
			pstmt.setString( idx++, userId );
			pstmt.setString( idx++, systemCode );
			rset = pstmt.executeQuery();
			if( !rset.next() )
				throw new SessionManagerException( SessionManagerException.HAS_NOAUTH_USER );
		} catch( SQLException sqlEx ) {
			throw new SessionManagerException( sqlEx );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	@Override
	public void checkLogin() throws SessionManagerException {
		if( sessionId == null )
			throw new SessionManagerException( SessionManagerException.NO_SESSIONID );

		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try {
			if( useEffectUser ) {
				pstmt = handler.getConnection().prepareStatement(
					"SELECT	S.STATUS SSTATUS, DECODE(U1.STATUS, ?, U2.STATUS, U1.STATUS) USTATUS"
							+ ", LEAST( NVL(U1.SERV_START_IND, 'N'), NVL(U2.SERV_START_IND, 'N') ) SERV_START_IND "
							+ ", GREATEST( NVL(U1.SERV_END_IND, 'Y'), NVL(U2.SERV_END_IND, 'Y') ) SERV_END_IND "
							+ ", TRUNC( (SYSDATE-S.LASTACC_TIME)*24*60 ) AT, TRUNC( (SYSDATE-S.LOGIN_TIME)*24*60 ) LT "
						+ " FROM vwUSR_USER U1, vwUSR_USER U2, USR_SESSION S "
						+ " WHERE SESSIONID = ? AND U1.UNIQID = S.UNIQID AND U2.UNIQID = S.EFFECT_UNIQID "
				);
				pstmt.setString( 1, STATUS_NORMAL );
				pstmt.setString( 2, sessionId );
			} else {
				pstmt = handler.getConnection().prepareStatement(
					"SELECT	S.STATUS SSTATUS, U.STATUS USTATUS, U.SERV_START_IND, U.SERV_END_IND"
							+ ", TRUNC( (SYSDATE-S.LASTACC_TIME)*24*60 ) AT, TRUNC( (SYSDATE-S.LOGIN_TIME)*24*60 ) LT "
						+ " FROM vwUSR_USER U, USR_SESSION S "
						+ " WHERE SESSIONID = ? AND U.PARTYID = S.PARTYID AND U.USERID = S.USERID "
				);
				pstmt.setString( 1, sessionId );
			}
			rset = pstmt.executeQuery();
			if( !rset.next() )
				throw new SessionManagerException( SessionManagerException.INVALID_SESSIONID );
			if( !STATUS_NORMAL.equals( rset.getString("SSTATUS") ) )
				throw new SessionManagerException( SessionManagerException.INVALID_SESSIONSTATUS );
			if( STATUS_PASSWORD_EXPIRED.equals( rset.getString("USTATUS") ) )
				throw new SessionManagerException( SessionManagerException.PASSWORD_EXPIRED );
			if( !STATUS_NORMAL.equals( rset.getString("USTATUS") ) )
				throw new SessionManagerException( SessionManagerException.INVALID_USERSTATUS );
			if( rset.getInt("LT") > maximumLoginTime || rset.getInt("AT") > maximumSessionTime )
				throw new SessionManagerException( SessionManagerException.SESSION_TIMEOVER );
			if( "N".equals(rset.getString("SERV_START_IND")) )
				throw new SessionManagerException( SessionManagerException.SERVICE_NOTSTARTED );
			if( "Y".equals(rset.getString("SERV_END_IND")) )
				throw new SessionManagerException( SessionManagerException.SERVICE_EXPIRED );
		} catch( SQLException sqlEx ) {
			throw new SessionManagerException( sqlEx );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
	}

	private void clearOldSession() throws DataException, SQLException {
		Object[] bindVars = new Object[] { new Integer(maximumLoginTime), new Integer(maximumSessionTime) };
		SQLManager.callStatement( handler, "call pkUSRSession.pClearOldSession( ?, ? )", bindVars );
		handler.commit();
	}

	@Override
	public void close() {
		handler.close();
	}

	private String generateSessionId() {
		String uid = new java.rmi.server.UID().toString();
		try {
			return java.net.URLEncoder.encode( uid, "8859_1" );
		} catch( java.io.UnsupportedEncodingException encodingEx ) {
			return uid;
		}
	}

	@Override
	public String getExtraValue() {
		return (String)getSessionValue( "EXTRAVALUE" );
	}

	@Override
	public String getGln() {
		return ( effectGln != null ? effectGln : gln );
	}

	@Override
	public String getGroupId() {
		return groupId;
	}

	@Override
	public String getGroupClass() {
		return groupClass;
	}

	@Override
	public String getIP() {
		return requestIP;
	}

	@Override
	public int getLastAccessCount() {
		return ((Number)getSessionValue( "LASTACC_TIME" )).intValue();
	}

	@Override
	public String getLastAccessSystemCode() {
		return (String)getSessionValue( "LASTACC_SYSTEMCD" );
	}

	@Override
	public String getLastAccessPackageCode() {
		return (String)getSessionValue( "LASTACC_PKGCD" );
	}

	@Override
	public java.util.Date getLastAccessTime() {
		return (java.util.Date)getSessionValue( "LASTACC_TIME" );
	}

	@Override
	public java.util.Date getLoginTime() {
		return (java.util.Date)getSessionValue( "LOGIN_TIME" );
	}

	@Override
	public String getPartyClass() {
		return partyClass;
	}

	@Override
	public String getPartyId() {
		return partyId;
	}

	@Override
	public String getPartyName() {
		return partyName;
	}

	public Properties getProperty() {
		return property;
	}

	public String getProperty( String key ) {
		return property.getProperty( key );
	}

	public String getProperty( String key, String defaultValue ) {
		return property.getProperty( key, defaultValue );
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	private Object getSessionValue( String fieldName ) {
		try {
			return SQLManager.getObjectValue( handler, "SELECT "+ fieldName +" FROM USR_SESSION WHERE SESSIONID = ?", sessionId );
		} catch( SQLException sqlEx ) {
			sqlEx.printStackTrace( System.err );
			return null;
		}
	}

	@Override
	public TimeZone getTimeZone() {
		return zone;
	}

	@Override
	public String getUniqId() {
		return uniqId;
	}

	@Override
	public String getUserClass() {
		return userClass;
	}

	@Override
	public String getUserExtraValue( int index ) {
		return userExtraValues[index];
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	private void initSessionInfo( ResultSet rset ) throws SQLException {
		if( rset == null ) {
			if( sessionId == null ) {
				partyId = partyClass = partyName = groupId = groupClass = null;
				uniqId = userId = userName = userClass = null;
				userExtraValues = new String[4];
				effectGln = gln = null;
				zone = TimeZone.getDefault();
				return;
			}

			PreparedStatement pstmt;
			if( useEffectUser )
				pstmt = handler.getConnection().prepareStatement(
					"SELECT U.*, S.EFFECT_GLN, UU.EMAIL USR_EMAIL FROM USR_SESSION S, vwUSR_USER U, USR_USER UU WHERE S.SESSIONID = ? AND U.UNIQID = S.EFFECT_UNIQID AND U.UNIQID = UU.UNIQID"
				);
			else
				pstmt = handler.getConnection().prepareStatement(
					"SELECT U.*, S.EFFECT_GLN, UU.EMAIL USR_EMAIL FROM USR_SESSION S, vwUSR_USER U, USR_USER UU WHERE S.SESSIONID = ? AND U.UNIQID = S.UNIQID AND U.UNIQID = UU.UNIQID"
				);
			try {
				pstmt.setString( 1, sessionId );
				rset = pstmt.executeQuery();
				if( rset.next() ) {
					effectGln = rset.getString( "EFFECT_GLN" );
					initSessionInfo( rset );
					setProperty( "userEmail", rset.getString("USR_EMAIL") );
				} else {
					partyId = partyClass = partyName = groupId = groupClass = null;
					uniqId = userId = userName = userClass = null;
					userExtraValues = new String[4];
					effectGln = gln = null;
					zone = TimeZone.getDefault();
				}
			} finally {
				try { rset.close(); } catch( Exception ex ) {}
				try { pstmt.close(); } catch( Exception ex ) {}
			}
		} else {
			String timezone = rset.getString( "TIMEZONE" );
			if( timezone == null )
				zone = TimeZone.getDefault();
			else
				zone = TimeZone.getTimeZone( timezone );

			partyId = rset.getString( "PARTYID" );
			partyClass = rset.getString( "PARTYCLASS" );
			partyName = rset.getString( "PARTYNAME" );
			groupId = rset.getString( "GROUPID" );
			groupClass = rset.getString( "GROUPCLASS" );

			uniqId = rset.getString( "UNIQID" );
			userId = rset.getString( "USERID" );
			userName = rset.getString( "USERNAME" );
			userClass = rset.getString( "USERCLASS" );
			userExtraValues = new String[4];
			userExtraValues[0] = rset.getString( "EXTRAVALUE1" );
			userExtraValues[1] = rset.getString( "EXTRAVALUE2" );
			userExtraValues[2] = rset.getString( "EXTRAVALUE3" );
			userExtraValues[3] = rset.getString( "EXTRAVALUE4" );

			gln = rset.getString( "GLN" );
		}
	}

	@Override
	public boolean isAdminUser() {
		return USERCLASS_PARTY_ADMIN.equals(getUserClass()) || USERCLASS_SYSTEM_ADMIN.equals(getUserClass());
	}

	@Override
	public boolean isAuthorized( String systemCode ) {
		try {
			checkAuthorize( systemCode );
			return true;
		} catch( SessionManagerException sessionEx ) {
			return false;
		}
	}

	@Override
	public boolean isAuthorized( String systemCode, String packageCode ) {
		return isAuthorized( systemCode, packageCode, AUTHMODE_SELF );
	}

	@Override
	public boolean isAuthorized( String systemCode, String packageCode, int authMode ) {
		try {
			checkAuthorize( systemCode, packageCode, authMode );
			return true;
		} catch( SessionManagerException sessionEx ) {
			return false;
		}
	}

	@Override
	public boolean isBuyerParty() {
		return( "XB".equals(partyClass) || "SB".equals(partyClass) );
	}

	@Override
	public boolean isCountryAdmin() {
		return GROUPCLASS_ADMIN.equals( getGroupClass() );
	}

	@Override
	public boolean isLoginUser() {
		try {
			checkLogin();
			return true;
		} catch( SessionManagerException sessionEx ) {
			return false;
		}
	}

	@Override
	public boolean isPartyAdmin() {
		return USERCLASS_PARTY_ADMIN.equals( getUserClass() );
	}

	@Override
	public boolean isSellerParty() {
		return( "SX".equals(partyClass) || "SB".equals(partyClass) );
	}

	@Override
	public boolean isSystemAdmin() {
		return USERCLASS_SYSTEM_ADMIN.equals( getUserClass() );
	}

	@Override
	public boolean login( String partyId, String userId, boolean killuser ) throws SessionManagerException {
		return login( partyId, userId, null, killuser, false );
	}

	@Override
	public boolean login( String partyId, String userId, String password, boolean killuser ) throws SessionManagerException {
		return login( partyId, userId, password, killuser, true );
	}

	private boolean login( String partyId, String userId, String password, boolean killuser, boolean checkPassword ) throws SessionManagerException {
		if( partyId == null || userId == null )
			throw new SessionManagerException( SessionManagerException.INVALID_USERID );

		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try {
/* LOCK이 되었을때 USR_USER.STATUS 처리*/
			pstmt = handler.getConnection().prepareStatement(
				"SELECT USR.*, NVL(SYSDATE - USR.PASSWORD_CHGDATETIME, 90) PASSWORD_CHGDAYS"
						+ ", NVL( SIGN(UUS.LOCK_DATETIME - SYSDATE), -1 ) LOCK_IND"
					+ " FROM vwUSR_USER USR, USR_USER_SECURITY UUS"
					+ " WHERE USR.PARTYID = ? AND USR.USERID = ? AND UUS.PARTYID(+) = USR.PARTYID AND UUS.USERID(+) = USR.USERID"
			);
			pstmt.setString( 1, partyId );
			pstmt.setString( 2, userId );
			rset = pstmt.executeQuery();
			if( !rset.next() )
				throw new SessionManagerException( SessionManagerException.INVALID_USERID );
			if( STATUS_LOCKED.equals(rset.getString("STATUS")) )
				throw new SessionManagerException( SessionManagerException.USER_ACCOUNT_LOCKED );
/*
			if( rset.getInt("LOCK_IND") > 0 )
				throw new SessionManagerException( SessionManagerException.TURNOVER_DISMATCHING_PASSWORD );
*/
			String userHashAlgorithm = rset.getString( "HASH_ALGORITHM" );
			if( !com.irt.rbm.usr.UserUser.HASH_ALGORITHM_MD5.equals(userHashAlgorithm) && !com.irt.rbm.usr.UserUser.HASH_ALGORITHM_SHA2.equals(userHashAlgorithm) ) {
				userHashAlgorithm = com.irt.rbm.usr.UserUser.hashAlgorithm;
			}

			if( checkPassword ) {
				this.partyId = partyId;
				this.userId = userId;

				if( !com.irt.rbm.usr.UserUser.comparePassword(password, rset.getString("PASSWORD"), userHashAlgorithm) ) {
					SQLManager.executeStatement( handler,
						"INSERT INTO USR_SESSION_FAILLOG( PARTYID, USERID, PARTYNAME, USERNAME, IP, ACCESS_TIME )"
							+ " VALUES( ?, ?, ?, ?, ?, SYSDATE )"
					, partyId, userId, rset.getString("PARTYNAME"), rset.getString("USERNAME"), requestIP );

					int failCount = SQLManager.getInt( handler,
						"SELECT COUNT(*) FROM USR_USER_SECURITY UUS, USR_SESSION_FAILLOG USF"
							+ " WHERE UUS.PARTYID = ? AND UUS.USERID = ? AND USF.PARTYID = UUS.PARTYID AND USF.USERID = UUS.USERID"
							+ " AND USF.ACCESS_TIME > SYSDATE - 1/24/4 AND USF.ACCESS_TIME > NVL(UUS.LAST_LOGINTIME, SYSDATE - 1/24/4)"
					, partyId, userId );

					int count = SQLManager.executeStatement( handler,
						"UPDATE USR_USER_SECURITY SET PASSWD_FAILCOUNT = ?, LOCK_DATETIME = DECODE( ?, 1, SYSDATE + 1/24/4 ) WHERE PARTYID = ? AND USERID = ?"
					, failCount, failCount >= 5 ? 1 : 0, partyId, userId );
					if( count == 0 ) {
						SQLManager.executeStatement( handler,
							"INSERT INTO USR_USER_SECURITY ( PASSWD_FAILCOUNT, LOCK_DATETIME, PARTYID, USERID )"
								+ " VALUES( ?, DECODE(?, 1, SYSDATE + 1/24/4), ?, ? )"
						, failCount, failCount >= 5 ? 1 : 0, partyId, userId );
					}

					/* 5회 이상 틀린 경우 USR_USER.STATUS 변경후 throw */
					if( failCount >= 5 ) {
						SQLManager.executeStatement( handler,
							"UPDATE USR_USER SET STATUS = ? WHERE PARTYID = ? AND USER_ID = ?"
						, STATUS_LOCKED, partyId, userId );
					}

					handler.commit();

					if( failCount >= 5 )
						throw new SessionManagerException( SessionManagerException.TURNOVER_DISMATCHING_PASSWORD );

					throw new SessionManagerException( SessionManagerException.INVALID_PASSWORD );
				}
			}

			{
				int count = SQLManager.executeStatement( handler,
					"UPDATE USR_USER_SECURITY SET LAST_LOGINTIME = SYSDATE WHERE PARTYID = ? AND USERID = ?"
				, partyId, userId );
				if( count == 0 ) {
					SQLManager.executeStatement( handler,
						"INSERT INTO USR_USER_SECURITY ( LAST_LOGINTIME, PARTYID, USERID ) VALUES( SYSDATE, ?, ? )"
					, partyId, userId );
				}
				handler.commit();
			}

			initSessionInfo( rset );
			if( STATUS_PASSWORD_EXPIRED.equals( rset.getString("STATUS") ) )
				throw new SessionManagerException( SessionManagerException.PASSWORD_EXPIRED );
			if( rset.getInt("PASSWORD_CHGDAYS") >= 90 )
				throw new SessionManagerException( SessionManagerException.PASSWORD_EXPIRED );
			if( !UserUser.hashAlgorithm.equals(userHashAlgorithm) )
				throw new SessionManagerException( SessionManagerException.PASSWORD_EXPIRED );
			if( !STATUS_NORMAL.equals( rset.getString("STATUS") ) )
				throw new SessionManagerException( SessionManagerException.INVALID_USERSTATUS );
			if( "N".equals(rset.getString("SERV_START_IND")) )
				throw new SessionManagerException( SessionManagerException.SERVICE_NOTSTARTED );
			if( "Y".equals(rset.getString("SERV_END_IND")) )
				throw new SessionManagerException( SessionManagerException.SERVICE_EXPIRED );

			clearOldSession();
			int availAccessCount = rset.getInt( "AVAILACCCNT" );
			if( availAccessCount > 0 ) {
				String[] sessionIds = SQLManager.getStringValues( handler,
					"SELECT	SESSIONID FROM USR_SESSION WHERE PARTYID = ? AND USERID = ? AND STATUS = ?"
						+ " ORDER BY LOGIN_TIME DESC"
				, partyId, userId, STATUS_LOGIN );

				if( sessionIds != null ) {
					if( killuser ) {
						for( int i = availAccessCount - 1; i < sessionIds.length; i++ )
							updateStatus( sessionIds[i], STATUS_KILL );
					} else if( sessionIds.length >= availAccessCount )
						throw new SessionManagerException( SessionManagerException.EXCEED_MAX_ACCESSCOUNT );
				}
			}

			String[] bindVars = new String[] {
				generateSessionId(), rset.getString("UNIQID"), partyId, userId
				, rset.getString("PARTYNAME"), rset.getString("USERNAME"), rset.getString("TIMEZONE")
				, rset.getString("UNIQID"), partyId, userId, rset.getString("PARTYNAME"), rset.getString("USERNAME")
				, requestIP, STATUS_LOGIN
			};
			SQLManager.executeStatement( handler,
				"INSERT INTO USR_SESSION ( SESSIONID, UNIQID, PARTYID, USERID, PARTYNAME, USERNAME, TIMEZONE"
						+ ", EFFECT_UNIQID, EFFECT_PARTYID, EFFECT_USERID, EFFECT_PARTYNAME, EFFECT_USERNAME"
						+ ", IP, LOGIN_TIME, LASTACC_SYSTEMCD, LASTACC_PKGCD, LASTACC_TIME, ACCCOUNT, STATUS )"
					+ " VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, NULL, NULL, SYSDATE, 1, ? )"
			, (Object[])bindVars );
			handler.commit();

			this.sessionId = bindVars[0];
			this.effectGln = null;
		} catch( DataException dataEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( dataEx );
		} catch( SQLException sqlEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( sqlEx );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}

		return true;
	}

	@Override
	public void logout() throws SessionManagerException {
		try {
			updateStatus( sessionId, STATUS_LOGOUT );
		} catch( DataException dataEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( dataEx );
		} catch( SQLException sqlEx ) {
			throw new SessionManagerException( sqlEx );
		}
	}

	@Override
	public boolean setEffectGln( String gln ) throws SessionManagerException {
		try {
			String userClass = (String)SQLManager.getObjectValue( handler,
				"SELECT U.USERCLASS FROM USR_SESSION S, vwUSR_USER U WHERE S.SESSIONID = ? AND U.UNIQID = S.UNIQID"
			, sessionId );

			if( userClass == null )
				throw new SessionManagerException( SessionManagerException.NO_SESSIONID );
			if( !USERCLASS_SYSTEM_ADMIN.equals(userClass) )
				throw new SessionManagerException( SessionManagerException.HAS_NOAUTH_USER );

			SQLManager.callStatement( handler, "call pkUSRSession.pSetEffectiveGln( ?, ? )", sessionId, this.effectGln = gln );
			handler.commit();
		} catch( DataException dataEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( dataEx );
		} catch( SQLException sqlEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( sqlEx );
		}

		return true;
	}

	@Override
	public boolean setEffectUser( String partyId, String userId ) throws SessionManagerException {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try {
			String userClass = (String)SQLManager.getObjectValue( handler,
				"SELECT U.USERCLASS FROM USR_SESSION S, vwUSR_USER U WHERE S.SESSIONID = ? AND U.UNIQID = S.UNIQID"
			, sessionId );

			if( userClass == null )
				throw new SessionManagerException( SessionManagerException.NO_SESSIONID );
			if( !USERCLASS_SYSTEM_ADMIN.equals(userClass) )
				throw new SessionManagerException( SessionManagerException.HAS_NOAUTH_USER );

			pstmt = handler.getConnection().prepareStatement( "SELECT * FROM vwUSR_USER WHERE PARTYID = ? AND USERID = ?" );
			pstmt.setString( 1, partyId );
			pstmt.setString( 2, userId );
			rset = pstmt.executeQuery();
			if( !rset.next() )
				throw new SessionManagerException( SessionManagerException.INVALID_USERID );

			SQLManager.callStatement( handler, "call pkUSRSession.pSetEffectiveUser( ?, ?, ? )", sessionId, partyId, userId );
			handler.commit();

			if( useEffectUser ) {
				this.effectGln = null;
				initSessionInfo( rset );
			}
		} catch( DataException dataEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( dataEx );
		} catch( SQLException sqlEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( sqlEx );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}

		return true;
	}

	@Override
	public void setExtraValue( String extraValue ) throws SessionManagerException {
		try {
			SQLManager.callStatement( handler, "call pkUSRSession.pSetExtraValue( ?, ? )", sessionId, extraValue );
			handler.commit();
		} catch( DataException dataEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( dataEx );
		} catch( SQLException sqlEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( sqlEx );
		}
	}

	public void setProperty( String key, String value ) {
		if( value != null )
			property.setProperty( key, value );
		else
			property.remove( key );
	}

	@Override
	public void setUseEffectUser( boolean useEffectUser ) throws SessionManagerException {
		if( this.useEffectUser != useEffectUser ) {
			this.useEffectUser = useEffectUser;
			try {
				initSessionInfo( null );
			} catch( SQLException sqlEx ) {
				throw new SessionManagerException( sqlEx );
			}
		}
	}

	public void updateLastAccess( String systemCode, String packageCode, String className, String requestMode, String title
						, String returnValue, String message, long elapsedTimeMilli ) throws SessionManagerException {
		if( sessionId == null ) return;

		Object[] bindVars = new Object[] {
			sessionId, systemCode, packageCode, className, requestMode, title, returnValue, new Long(elapsedTimeMilli), message, null
			, null
		};
		try {
			SQLManager.callStatement( handler, "call pkUSRSession.pUpdateAccessLog( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )", bindVars );
			handler.commit();
		} catch( DataException dataEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( dataEx );
		} catch( SQLException sqlEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( sqlEx );
		}
	}

	@Override
	public void updateLastAccess( String systemCode, String packageCode,
			String className, String requestMode, String title, String returnValue,
			String message, long elapsedTimeMilli, String userAgent )
			throws SessionManagerException {

		if( sessionId == null ) return;

		Object[] bindVars = new Object[] {
			sessionId, systemCode, packageCode, className, requestMode, title, returnValue, new Long(elapsedTimeMilli), message, null
			, userAgent
		};
		try {
			SQLManager.callStatement( handler, "call pkUSRSession.pUpdateAccessLog( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )", bindVars );
			handler.commit();
		} catch( DataException dataEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( dataEx );
		} catch( SQLException sqlEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw new SessionManagerException( sqlEx );
		}
	}

		private void updateStatus( String sessionId, String status ) throws DataException, SQLException {
		try {
			SQLManager.callStatement( handler, "call pkUSRSession.pSetStatus( ?, ? )", sessionId, status );
			handler.commit();
		} catch( DataException dataEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw dataEx;
		} catch( SQLException sqlEx ) {
			try { handler.rollback(); } catch( Exception ex ) {}
			throw sqlEx;
		}
	}

	@Override
	public boolean usingEffectUser() {
		return useEffectUser;
	}
}
