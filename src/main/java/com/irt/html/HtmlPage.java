/*
 *	File Name:	HtmlPage.java
 *	Version:	2.2.6c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.6c	신규 UI/UX 적용
 *	jbaek		2020/06/30		2.2.6c	$mobile
 *	hankalam	2015/10/30		2.2.6	getForcus() 추가.
 *	jbaek		2014/12/31		2.2.5	getUserAgentInfo(), setUserAgentString(), getUserAgentString(), MAX_USER_AGENT_STRING_LENGTH 추가
 *	GimHS		2014/08/29		2.2.4	CrossBrowsing 적용: isMobileAgent(), isOldBrowserIE() 추가
 *	stghr12		2009/10/31		2.2.3	getButtonTag(), getFieldTitleAttribute(), getInputAttribute(), getInputButtonTag() 삭제
 *										popContentGroup(), pushContentGroup(), HtmlPage.ContentGroup 변경
 *	stghr12		2009/01/31		2.2.2	pushContentGroup( ... , message , ... ) 추가
 *	stghr12		2008/08/29		2.2.1	getDefaultShowCounts() 삭제
 *	stghr12		2007/11/30		2.2.0	getLocale() 추가
 *	stghr12		2007/04/30		2.1.1	setInputStatus() 추가
 *										BUTTON_*, INPUTBUTTON_* 상수 추가
 *	stghr12		2006/12/01		2.1.0	inputType, mandatoryType, inputStatus, validationType을 int형에서 char형으로 변경
 *										MAX_URL_LENGTH 추가
 *										INPUTSTATUS_INFORMATION, INPUTSTATUS_READONLY, INPUTSTATUS_INPUT 변수값 변경
 *										getDefaultShowCounts(), hasAuthority(authType) 추가
 *										ContentGroup.getField(fieldKey), ContentGroup.getValidableField(fieldKey) 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.AbstractField;
import com.irt.data.AbstractFieldSet;
import com.irt.data.ValidableField;
import com.irt.util.MessageHandler;
import java.util.Locale;
import java.util.Properties;

/**
 *
 */
public interface HtmlPage {
	public final static int MAX_URL_LENGTH				= 2048;
	public final static int MAX_USER_AGENT_STRING_LENGTH	= 256;

	public final static String INPUT_HIDDEN				= "hidden";
	public final static String INPUT_TEXT				= "text";
	public final static String INPUT_TEXTAREA			= "textarea";
	public final static String INPUT_PASSWORD			= "password";
	public final static String INPUT_CHECK				= "check";
	public final static String INPUT_RADIO				= "radio";
	public final static String INPUT_SELECT				= "select";

	public final static char INPUT_INFORMATION			= 'I';
	public final static char INPUT_READONLY				= 'R';
	public final static char INPUT_MANDATORY			= 'M';
	public final static char INPUT_OPTIONAL				= 'O';

	public final static char INPUTSTATUS_INFORMATION	= INPUT_INFORMATION;
	public final static char INPUTSTATUS_READONLY		= INPUT_READONLY;
	public final static char INPUTSTATUS_INPUT			= INPUT_OPTIONAL;

	public final static char PAGE_RESULT_SUCCESS		= 'S';
	public final static char PAGE_RESULT_WARNING		= 'W';
	public final static char PAGE_RESULT_ERROR			= 'E';
	public final static char PAGE_RESULT_NULL			= 0;
	public String getBackURL();

	public HtmlPage.ContentGroup getContentGroup();

	public String getFocus();

	public String getFormName();

	public char getInputStatus();

	public int[] getListIndexVariables();

	public Locale getLocale();

	public String getMessage();

	public MessageHandler getMessageHandler();

	public String getMode();

	public Properties getProperty();

	public UAgentInfo getUserAgentInfo();

	public String getUserAgentString();

	public String getRequestURL();

	public char getResultLevel();

	public String[] getStyleSheetNames();

	public String getSystemMenu();

	public String getSubTitle();

	public String getTitle();

	public String getValidationScript();

	public String getWindowType();

	public boolean hasAuthority( String authType );

	public boolean hasManageAuth();

	public boolean isMobileAgent();

	public boolean isMobileProfile();

	public boolean isOldBrowserIE();

	public HtmlPage.ContentGroup popContentGroup();

	public HtmlPage.ContentGroup pushContentGroup( String groupId, AbstractFieldSet fieldSet, Boolean autoMandatory, Boolean autoValidation );

	public void putValidationScript( String script );

	public void setFormName( String formName );

	public void setInputStatus( char inputStatus );

	public void setUserAgentString( String userAgent );

	/**
	 *
	 */
	public class ContentGroup {
		public String groupId;
		public AbstractFieldSet fieldSet;
		public boolean autoMandatory;
		public boolean autoValidation;

		public ContentGroup( String groupId, AbstractFieldSet fieldSet, boolean autoMandatory, boolean autoValidation ) {
			this.groupId = groupId;
			this.fieldSet = fieldSet;
			this.autoMandatory = autoMandatory;
			this.autoValidation = autoValidation;
		}

		public AbstractField getField( String fieldKey ) {
			if( fieldSet == null ) return null;
			return fieldSet.getField( fieldKey );
		}

		public ValidableField getValidableField( String fieldKey ) {
			AbstractField field = fieldSet.getField( fieldKey );
			try {
				return (ValidableField)field;
			} catch( ClassCastException castEx ) {
				return null;
			}
		}
	}
}
