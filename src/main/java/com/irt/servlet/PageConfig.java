/*
 *	File Name:	PageConfig.java
 *	Version:	2.2.5c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.5c	신규 UI/UX 적용
 *	hankalam	2016/09/30		2.2.5	XSS 보안 이슈 관련 수정: mode, backURL, systemMenu, windowType 모두 cleanXSS() 적용
 *																 cleanXSSProperty() 추가
 *	hankalam	2015/10/30		2.2.4	XSS 보안 이슈 관련 수정: systemMenu와 windowType에 "+", ";"가 있을 경우 null 처리
 *	jbaek		2011/08/31		2.2.3	XSS 보안 이슈 관련 수정: systemMenu와 windowType에 "<", ">"가 있을 경우 null 처리
 *	stghr12		2010/01/31		2.2.2	initialize(): backURL.replaceAll( "<", "%3C" ).replaceAll( ">", "%3E" ); 처리
 *	stghr12		2008/08/29		2.2.1	생성자 PageConfig(property) 추가
 *	stghr12		2007/11/30		2.2.0	getLocale() 삭제
 *	stghr12		2006/12/01		2.1.0	inputStatus를 int형에서 char형으로 변경
 *										backURL이 빈문자일 경우 null로 처리
 *										hasAuthority(authType), setAuthority(authType, hasAuthority) 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.servlet;

import com.irt.html.HtmlPage;
import com.irt.util.MessageHandler;

import java.util.Properties;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class PageConfig {
	String backURL, message, mode, title, subTitle, systemMenu, windowType;
	String systemCode, packageCode;
	char inputStatus, resultLevel;
	int[] idxVars;
	boolean hasManageAuth;
	Properties property;

	HtmlPage htmlpage;
	MessageHandler msghandler;

	public PageConfig() {
		this.inputStatus = HtmlPage.INPUTSTATUS_INPUT;
		this.idxVars = new int[] { 0, -1, -1, -1 };
		this.hasManageAuth = false;
		this.property = new Properties();
		this.resultLevel = HtmlPage.PAGE_RESULT_NULL;
	}

	public PageConfig( Properties property ) {
		this.inputStatus = HtmlPage.INPUTSTATUS_INPUT;
		this.idxVars = new int[] { 0, -1, -1, -1 };
		this.hasManageAuth = false;
		this.property = new Properties( property );
	}

	public String getBackURL() {
		return backURL;
	}

	public HtmlPage getHtmlPage() {
		return htmlpage;
	}

	public char getInputStatus() {
		return inputStatus;
	}

	public int[] getListIndexVariables() {
		return idxVars;
	}

	public String getMessage() {
		return message;
	}

	public MessageHandler getMessageHandler() {
		return msghandler;
	}

	public String getMode() {
		return mode;
	}

	public String getPackageCode() {
		return packageCode;
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

	public char getResultLevel() {
		return resultLevel;
	}

	public String getSystemCode() {
		return systemCode;
	}

	public String getSystemMenu() {
		return systemMenu;
	}

	public String getTitle() {
		return title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public String getWindowType() {
		return windowType;
	}

	public boolean hasAuthority( String authType ) {
		return "Y".equals( getProperty("hasAuth;"+ authType) );
	}

	public boolean hasManageAuth() {
		return hasManageAuth;
	}

	protected void initialize( HttpServletRequest req, HtmlPage htmlpage, MessageHandler msghandler ) {
		this.htmlpage = htmlpage;
		this.msghandler = msghandler;

		// List Index Variable 설정
		try {
			int skipRows = Integer.parseInt( req.getParameter(ServletModel.PARAM_SKIPROWS) );
			if( skipRows >= 0 ) idxVars[0] = skipRows;
		} catch( NumberFormatException numEx ) {}
		try {
			int maxRows = Integer.parseInt( req.getParameter(ServletModel.PARAM_MAXROWS) );
			if( maxRows == -1 || maxRows > 0 ) idxVars[1] = maxRows;
		} catch( NumberFormatException numEx ) {}
		try {
			idxVars[2] = Integer.parseInt( req.getParameter(ServletModel.PARAM_ALLROWS) );
		} catch( NumberFormatException numEx ) {}

		// PARAM_MODE, PARAM_URL, PARAM_MENU, PARAM_WINTYPE
		this.mode = com.irt.html.HtmlUtility.checkXSS( req.getParameter(ServletModel.PARAM_MODE) );
		this.backURL = com.irt.html.HtmlUtility.toASCIICodeString( req.getParameter(ServletModel.PARAM_URL) );
		this.systemMenu = com.irt.html.HtmlUtility.checkXSS( req.getParameter(ServletModel.PARAM_MENU) );
		this.windowType = com.irt.html.HtmlUtility.checkXSS( req.getParameter(ServletModel.PARAM_WINTYPE) );

		// PARAM_MESSAGE_KEY 설정
		String messageKey = req.getParameter( ServletModel.PARAM_MESSAGE_KEY );
		if( messageKey != null ) {
			try {
				this.message = (String)ServletUtility.popTemporaryObject( req, messageKey );
			} catch( ClassCastException castEx ) {
			} catch( java.util.NoSuchElementException elementEx ) {}
		}
	}

	public void setBackURL( String backURL ) {
		this.backURL = backURL;
	}

	public void setInputStatus( char inputStatus ) {
		this.inputStatus = inputStatus;
	}

	public void setAuthority( String authType, boolean hasAuthority ) {
		setProperty( "hasAuth;"+ authType, hasAuthority ? "Y" : "N" );
	}

	public void setManageAuth( boolean hasManageAuth ) {
		this.hasManageAuth = hasManageAuth;
	}

	public void setMessage( String message ) {
		this.message = message;
	}

	public void setMode( String mode ) {
		this.mode = mode;
	}

	public void cleanXSSProperty() {
		java.util.Enumeration<Object> keys = property.keys();

		while( keys.hasMoreElements() ) {
			String key = (String) keys.nextElement();
			property.setProperty( key, com.irt.html.HtmlUtility.cleanXSS(property.getProperty(key)) );
		}
	}

	public void setProperty( String key, String value ) {
		if( value != null )
			property.setProperty( key, value );
		else
			property.remove( key );
	}

	public void setResultLevel( char resultLevel ) {
		this.resultLevel = resultLevel;
	}

	public void setSystemMenu( String systemMenu ) {
		this.systemMenu = systemMenu;
	}

	public void setSystemPackageCode( String systemCode, String packageCode ) {
		this.systemCode = systemCode;
		this.packageCode = packageCode;
	}

	public void setTitle( String title ) {
		this.title = title;
	}

	public void setSubTitle( String subTitle ) {
		this.subTitle = subTitle;
	}

	public void setWindowType( String windowType ) {
		this.windowType = ( windowType == null || windowType.length() == 0 ? null : windowType );
	}
}
