/*
	*	File Name:	SessionManager.java
	*	Version:	2.2.3c
	*
	*	Description:
	*
	*	Note:
	*
	*	Modified	(YYYY/MM/DD)	Ver		Content
	*	jbaek		2014/12/31		2.2.3c	updateLastAccess(): userAgent 추가
	*	stghr12		2009/05/20		2.2.2c	encodePassword(), isSystemAdmin( useEffectUser ) 추가
	*	lsinji		2008/09/26		2.2.1c	isCountryAdmin(), getGroupClass() 추가(DPR)
	*	stghr12		2008/03/31		2.2.1	updateLastAccess(): elapsedTimeMilli 추가
	*	stghr12		2007/11/30		2.2.0	getTimeZone() 추가
	*	stghr12		2007/10/31		2.1.1	isAdminUser() 추가
	*	stghr12		2006/12/01		2.1.0	getPartyGln() -> getGln()
	*										getUserExtraValue() 추가
	*										login( partyId, userId, killuser ) 추가
	*	stghr12		2006/02/28		2.0.0	version up
	*	stghr12		2004/01/31		1.0.0	version 관리
	*	stghr12		2002/04/15				create
	*
**/

package com.irt.system;

/**
	*	사용자와 Login/Logout등을 처리하는 Class
	*/
public interface SessionManager {
	public final static String USERCLASS_SYSTEM_ADMIN	= "SA";
	public final static String USERCLASS_PARTY_ADMIN	= "PA";
	public final static String USERCLASS_USER			= "UR";

	public final static int AUTHMODE_SELF				= 'S';
	public final static int AUTHMODE_AT_LEAST_ONE		= 'L';

	public boolean changePassword( String partyId, String userId, String password, String newPassword ) throws SessionManagerException;

	public void checkAuthorize( String systemCode ) throws SessionManagerException;

	public void checkAuthorize( String systemCode, String packageCode ) throws SessionManagerException;

	public void checkAuthorize( String systemCode, String packageCode, int authMode ) throws SessionManagerException;

	public void checkLogin() throws SessionManagerException;

	public void close();

	public String getExtraValue();

	public String getGln();

	public String getGroupId();

	public String getGroupClass();

	public String getIP();

	public int getLastAccessCount();

	public String getLastAccessSystemCode();

	public String getLastAccessPackageCode();

	public java.util.Date getLastAccessTime();

	public java.util.Date getLoginTime();

	public String getPartyClass();

	public String getPartyId();

	public String getPartyName();

	public String getSessionId();

	public java.util.TimeZone getTimeZone();

	public String getUniqId();

	public String getUserClass();

	public String getUserExtraValue( int index );

	public String getUserId();

	public String getUserName();

	public boolean isAdminUser();

	public boolean isAuthorized( String systemCode );

	public boolean isAuthorized( String systemCode, String packageCode );

	public boolean isAuthorized( String systemCode, String packageCode, int authMode );

	public boolean isBuyerParty();

	public boolean isCountryAdmin();

	public boolean isLoginUser();

	public boolean isPartyAdmin();

	public boolean isSellerParty();

	public boolean isSystemAdmin();

	public boolean login( String partyId, String userId, boolean killuser ) throws SessionManagerException;

	public boolean login( String partyId, String userId, String passwd, boolean killuser ) throws SessionManagerException;

	public void logout() throws SessionManagerException;

	public boolean setEffectGln( String gln ) throws SessionManagerException;

	public boolean setEffectUser( String partyId, String userId ) throws SessionManagerException;

	public void setExtraValue( String extraValue ) throws SessionManagerException;

	public void setUseEffectUser( boolean useEffectUser ) throws SessionManagerException;

	public void updateLastAccess( String systemCode, String packageCode, String className, String requestMode, String title
						, String returnValue, String message, long elapsedTimeMilli, String userAgent ) throws SessionManagerException;

	public boolean usingEffectUser();
}
