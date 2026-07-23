/*
 *	File Name:	HiddenInputTag.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/04/22		2.2.1	getValue() -> getValueOne()
 *	stghr12		2009/10/31		2.2.0	TagUtility 사용
 *	stghr12		2006/09/15		2.0.1	TAG안에 value에 대해서 HtmlUtility.toScriptString() -> HtmlUtility.toHtmlString()
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * &lt;input type="hidden"&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)defaultValue
 * <li>(O)id
 * <li>(M)key
 * <li>(O)modified
 * <li>(O)name
 * <li>(O)onBlur
 * <li>(O)onKeyDown
 * </ul>
 */
public class HiddenInputTag extends InputTag {
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			utility.printText( out, HtmlPage.INPUT_HIDDEN, getName(), getValueOne(), 0, getAttribute(HtmlPage.INPUT_HIDDEN) );
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}

		return EVAL_PAGE;
	}
}
