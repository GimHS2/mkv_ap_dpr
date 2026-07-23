/*
 *	File Name:	PageConfig.java
 *	Version:	2.2.10c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/12/31		2.2.10c	focus XSS Clean 수정
 *	hankalam	2020/10/30		2.2.10c	isSecure = Y 일 경우 requestURL 을 무조건 https 로 변경.
 *	jbaek		2020/03/30		2.2.10c	$mobile
 *	jbaek		2017/02/28		2.2.10c	XSS 보안 이슈 관련 수정: systemMenu가 ""로 들어올 경우 default 메뉴 처리.
 *	song7918	2016/06/03		2.2.9	User-Agent 값이 null 일 경우 String 처리
 *	hankalam	2015/10/30		2.2.8	focus, getFocus() 추가
 *	jbaek		2014/12/31		2.2.7	User-Agent String 저장
 *	GimHS		2014/08/29		2.2.6c	CrossBrowsing 적용:
 *										 -> isMobileAgent(), isOldBrowserIE() 추가
 *	stghr12		2009/10/31		2.2.5	getButtonTag(), getFieldTitleAttribute(), getInputAttribute(), getInputButtonTag() 삭제
 *										popContentGroup(), pushContentGroup(), HtmlPage.ContentGroup 변경
 *	stghr12		2009/06/30		2.2.4	pushContentGroup(): type = "span" 추가
 *										Field.checkNumberFormat() 호출할 때, minus=true로 파라미터 넘김
 *	stghr12		2009/01/31		2.2.3	pushContentGroup( ... , message , ... ) 추가
 *	stghr12		2008/08/29		2.2.2	systemConfig.getDefaultPageProperty() 사용, getDefaultShowCounts() 삭제
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *										getStyleSheetNames(): getSystemMenu()가 null일 때 menu_default.css 사용
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										com.irt.system.SystemConfig 변경사항 적용: Locale 사용
 *										PageConfig(req, systemConfig) -> PageConfig(req, systemConfig, locale)
 *	stghr12		2007/04/30		2.1.1	BUTTON_*, INPUTBUTTON_* 을 com.irt.html.HtmlPage으로 이동
 *	stghr12		2006/12/01		2.1.0	inputType, mandatoryType, inputStatus, validationType을 int형에서 char형으로 변경
 *										BUTTON_DOWNLOAD, BUTTON_UPLOAD 추가
 *										defaultShowCounts, getDefaultShowCounts() 추가
 *										getButtonTag(): MODE_MULTIMODIFY일 때, windowClose(true); 호출
 *										getFieldTitleAttribute(): key가 null일 경우 처리
 *										getInputButtonTag(): 달력BUTTON icon 변경
 *										pushContentGroup(): "list" description 처리 추가, "search" 추가
 *	stghr12		2006/07/07		2.0.1	rangeType 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.custom;

import com.irt.data.*;
import com.irt.html.HtmlPage;
import com.irt.html.UAgentInfo;
import com.irt.servlet.ServletModel;
import com.irt.servlet.SystemConfig;
import java.util.Locale;
import java.util.Stack;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class PageConfig extends com.irt.servlet.PageConfig implements com.irt.html.HtmlPage {
	static String UNDEFINED_USERAGENT	= "NULL";
	boolean isMobileAgent = false;
	boolean isMobileProfile = false;
	boolean isOldBrowserIE = false;
	Locale locale;
	String formName, requestURL, userAgentString, focus;
	StringBuffer scriptBuffer;
	Stack<HtmlPage.ContentGroup> contentGroupStack;
	HtmlPage.ContentGroup contentGroup;
	UAgentInfo userAgentInfo;

	public PageConfig( HttpServletRequest req, SystemConfig systemConfig, Locale locale ) {
		super( systemConfig.getDefaultPageProperty() );
		this.locale = locale;
		this.scriptBuffer = new StringBuffer();
		this.contentGroup = new HtmlPage.ContentGroup( null, null, true, true );
		this.contentGroupStack = new Stack<HtmlPage.ContentGroup>();
		this.contentGroupStack.push( this.contentGroup );
		this.focus = req.getParameter( ServletModel.PARAM_FOCUS );
		if( this.focus != null ) {
			if( this.focus.length() == 0 || this.focus.matches(".*(\\(|\\)|<|>|;|\\+|\\.|-).*") )
				this.focus = null;
		}

		getListIndexVariables()[1] = Integer.parseInt( getProperty("defaultShowCount", "15") );

		this.requestURL = req.getRequestURL().toString();
		if( "Y".equals(systemConfig.getIsSecure()) ) {
			this.requestURL = this.requestURL.replace( "http://", "https://" );
		}
		String userAgent = req.getHeader( "User-Agent" );
		String httpAccept = req.getHeader( "Accept" );
		if( userAgent == null ) userAgent = UNDEFINED_USERAGENT;
		setUserAgentString( userAgent );
		this.userAgentInfo = new UAgentInfo( userAgent, httpAccept );
		initialize( req, this, systemConfig.getMessageHandler(locale) );

		if( userAgent.indexOf("Android") >= 0 ) this.isMobileAgent = true;
		else if( userAgent.indexOf("iPhone") >= 0 ) this.isMobileAgent = true;
		else if( userAgent.indexOf("iPad") >= 0 ) this.isMobileAgent = true;

		if( "Y".equals(req.getAttribute("useMobileProfile")) ) {
			boolean isMobileProfile = false;
			String mobileInd = req.getParameter("mobile");
			if( "Y".equals(mobileInd) || "msub".equals(mobileInd) )
				isMobileProfile = true;
			else if( "N".equals(mobileInd) )
				isMobileProfile = false;
			else if( "Y".equals(req.getAttribute("isMobileProfile")) )
				isMobileProfile = true;
			else {

				if( !isMobileProfile && "Y".equals(req.getAttribute("useMobileAuto")) && this.isMobileAgent ) {
						isMobileProfile = true;
				}
				if( !isMobileProfile && userAgent.contains("Mac") ) {
						isMobileProfile = true;
				}
				String csvMobileAutoDesktop = (String)req.getAttribute("csvMobileAutoDesktop");
				if( !isMobileProfile && csvMobileAutoDesktop != null && csvMobileAutoDesktop.length() > 0 ) {
					java.util.List<String> browserList = java.util.Arrays.asList(csvMobileAutoDesktop.split(","));
					if( browserList.contains("Chrome") && userAgent.indexOf("Chrome") >= 0 ) {
						isMobileProfile = true;
					} else if( browserList.contains("Edge") && userAgent.indexOf("Edge") >= 0 ) {
						isMobileProfile = true;
					} else if( browserList.contains("Firefox") && userAgent.indexOf("Firefox") >= 0 ) {
						isMobileProfile = true;
					} else if( browserList.contains("Safari") && userAgent.indexOf("Safari") >= 0 && userAgent.indexOf("Mac") >=0 ) {
						isMobileProfile = true;
					}
				}

				String mobileCookie = "";
				if( req.getCookies() != null )
						for( int i = 0; i < req.getCookies().length; i++ )
							if( "mobile".equals(req.getCookies()[i].getName()) ) {
								mobileCookie = req.getCookies()[i].getValue();
								break;
							}
				if( !isMobileProfile && mobileCookie != null && mobileCookie.length() > 0 ) {
					isMobileProfile = !("N".equals(mobileCookie));
				}
			}
			this.isMobileProfile = isMobileProfile;
		}

		if( userAgent.indexOf("MSIE 9.0") >= 0 ) this.isOldBrowserIE = true;
		else if( userAgent.indexOf("MSIE 8.0") >= 0 ) this.isOldBrowserIE = true;
		else if( userAgent.indexOf("MSIE 7.0") >= 0 ) this.isOldBrowserIE = true;
		else if( userAgent.indexOf("MSIE 6.0") >= 0 ) this.isOldBrowserIE = true;
	}

	@Override
	public HtmlPage.ContentGroup getContentGroup() {
		return contentGroup;
	}

	@Override
	public String getFocus() {
		return focus;
	}

	@Override
	public String getFormName() {
		return formName;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public String getRequestURL() {
		return requestURL;
	}

	@Override
	public String[] getStyleSheetNames() {
		if( getSystemMenu() == null || getSystemMenu().length() == 0 )
			return new String[] { "common.css", "menu_default.css" };
		else
			return new String[] { "common.css", "menu_"+ getSystemMenu() +".css" };
	}

	@Override
	public UAgentInfo getUserAgentInfo() {
		return this.userAgentInfo;
	}

	@Override
	public String getUserAgentString() {
		return this.userAgentString;
	}

	@Override
	public String getValidationScript() {
		return scriptBuffer.toString();
	}

	@Override
	public boolean isMobileAgent() {
		return isMobileAgent;
	}

	@Override
	public boolean isMobileProfile() {
//		String savedOrgCd = super.getProperty("savedOrgCd");
//		if( com.irt.dpr.Country.isFeature(super.getProperty().getProperty("savedOrgCd"), "useMobileProfile") ) {
			return isMobileProfile;
//		} else {
//			return false;
//		}
	}

	@Override
	public boolean isOldBrowserIE() {
		return isOldBrowserIE;
	}

	@Override
	public HtmlPage.ContentGroup popContentGroup() {
		contentGroupStack.pop();
		contentGroup = contentGroupStack.peek();

		return contentGroup;
	}

	@Override
	public HtmlPage.ContentGroup pushContentGroup( String groupId, AbstractFieldSet fieldSet, Boolean autoMandatory, Boolean autoValidation ) {
		boolean autoMandatoryBool = contentGroup.autoMandatory;
		boolean autoValidationBool = contentGroup.autoValidation;

		if( autoMandatory != null ) autoMandatoryBool = autoMandatory.booleanValue();
		if( autoValidation != null ) autoValidationBool = autoValidation.booleanValue();
		if( fieldSet == null ) fieldSet = contentGroup.fieldSet;

		contentGroup = new HtmlPage.ContentGroup( groupId, fieldSet, autoMandatoryBool, autoValidationBool );
		contentGroupStack.push( contentGroup );

		return contentGroup;
	}

	@Override
	public void putValidationScript( String script ) {
		scriptBuffer.append( script );
	}

	@Override
	public void setFormName( String formName ) {
		this.formName = formName;
	}

	/** User-Agent string은 길이 제한이 없다고 하며, 보통은 256 이하라고 함.데이터베이스에 저장하기 위해서는 길이 제한을 둬야함.*/
	@Override
	public void setUserAgentString( String userAgent ) {
		if( userAgent != null && userAgent.length() > MAX_USER_AGENT_STRING_LENGTH )
			this.userAgentString = userAgent.substring( 0, MAX_USER_AGENT_STRING_LENGTH );
		else
			this.userAgentString = userAgent;
	}
}
