/*
 *	File Name:	ScriptTag.java
 *	Version:	2.0.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import javax.servlet.jsp.JspException;

/**
 * htmlpage에 ValidationScript를 저장.
 *
 * @see HtmlPage#putValidationScript( String ) HtmlPage.putValidationScript()
 */
public class ScriptTag extends javax.servlet.jsp.tagext.BodyTagSupport {
	public int doEndTag() throws JspException {
		if( getBodyContent() != null ) {
			HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
			htmlpage.putValidationScript( getBodyContent().getString() );
		}
		return EVAL_PAGE;
	}

	public int doStartTag() throws JspException {
		return EVAL_BODY_BUFFERED;
	}
}
