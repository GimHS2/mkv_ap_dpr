/*
 *	File Name:	HyperLink.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.HyperLink -> com.irt.html.HyperLink
 *	stghr12		2006/02/28		2.0.0	version up(기존의 HyperLink은 HyperLinkImpl로 바꾸고, interface로 변경)
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.html;

import com.irt.system.SessionManager;
import com.irt.util.MessageHandler;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public interface HyperLink extends com.irt.data.format.RecordFormat {

	public void addFieldKeyToSet( Set<String> fieldKeySet );

	public String format( Map recordMap, MessageHandler msghandler );

	public String getHelpString( Map recordMap, MessageHandler msghandler );

	public String getLinkString( Map recordMap, MessageHandler msghandler );

	public String getKey();

	public boolean isValidLink( Map recordMap, SessionManager sessionMng );
}
