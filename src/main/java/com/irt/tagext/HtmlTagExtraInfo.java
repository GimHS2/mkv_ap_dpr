/*
 *	File Name:	HtmlTagExtraInfo.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.0	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *										handler 추가
 *	stghr12		2006/02/28		2.0.0	create(InitPageTagExtraInfo -> HtmlTagExtraInfo)
 *
**/

package com.irt.tagext;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 *
 */
public class HtmlTagExtraInfo extends javax.servlet.jsp.tagext.TagExtraInfo {
	public VariableInfo[] getVariableInfo( TagData data ) {
		return new VariableInfo[] {
			  new VariableInfo ( "systemConfig", "com.irt.servlet.SystemConfig", true, VariableInfo.NESTED )
			, new VariableInfo ( "sessionMng", "com.irt.system.SessionManager", true, VariableInfo.NESTED )
			, new VariableInfo ( "htmlpage", "com.irt.html.HtmlPage", true, VariableInfo.NESTED )
			, new VariableInfo ( "handler", "com.irt.sql.SQLHandler", true, VariableInfo.NESTED )
			, new VariableInfo ( "msghandler", "com.irt.util.MessageHandler", true, VariableInfo.NESTED )
			, new VariableInfo ( "property", "java.util.Properties", true, VariableInfo.NESTED )
		};
	}
}
