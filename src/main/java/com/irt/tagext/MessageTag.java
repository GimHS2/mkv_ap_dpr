/*
 *	File Name:	MessageTag.java
 *	Version:	2.1.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/01/30		2.1.0c	pageEdit 적용
 *	stghr12		2006/12/01		2.1.0	encodeHTML, encodeScript 추가
 *	stghr12		2006/02/28		2.0.0	create(SystemMessage -> MessageTag )
 *
**/

package com.irt.tagext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.RBMSystem;
import com.irt.resbdl.PageMessageKeys;
import com.irt.servlet.SystemConfig;

/**
 * message 출력.
 *
 * <ul type='square'>
 * <li>(O)encodeHTML - false
 * <li>(O)encodeScript - false
 * <li>(M)key
 * </ul>
 */
public class MessageTag extends javax.servlet.jsp.tagext.TagSupport {
	boolean encodeHTML = false, encodeScript = false;
	String key;

	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
			String messageValue = com.irt.resbdl.PageMessageKeys.getMessageAndProcess(htmlpage, key);
			if( encodeHTML )
				out.print( HtmlUtility.toHtmlString(messageValue) );
			else if( encodeScript )
				out.print( HtmlUtility.toScriptString(messageValue) );
			else
				out.print( messageValue );
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}
		return EVAL_PAGE;
	}

	public void setEncodeHTML( boolean encodeHTML ) {
		if( this.encodeHTML = encodeHTML )
			this.encodeScript = false;
	}

	public void setEncodeScript( boolean encodeScript ) {
		if( this.encodeScript = encodeScript )
			this.encodeHTML = false;
	}

	public void setKey( String key ) {
		this.key = key;
	}
}
