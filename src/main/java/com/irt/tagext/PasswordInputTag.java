/*
 *	File Name:	PasswordInputTag.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2009/10/31		2.2.0	TagUtility 사용
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * &lt;input type="password"&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)id
 * <li>(M)key
 * <li>(O)mandatory
 * <li>(O)maxlength
 * <li>(O)modified
 * <li>(O)name
 * <li>(O)onBlur
 * <li>(O)onKeyDown
 * <li>(O)readonly
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * </ul>
 */
public class PasswordInputTag extends InputTag {
	int maxlength = -1;

	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			if( mandatoryType != INPUT_INFORMATION ) {
				String name = getName();
				int maxlength = ( this.maxlength < 0 && field != null ? field.getMaxLength() : this.maxlength );

				utility.printText( out, HtmlPage.INPUT_PASSWORD, name, null, maxlength, getAttribute(HtmlPage.INPUT_PASSWORD) );
				if( htmlpage.getContentGroup().autoValidation )
					utility.putValidationScript( field, mandatoryType, htmlpage.getFormName() +"."+ name );
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}

		return EVAL_PAGE;
	}

	public void setMaxlength( int maxlength ) {
		this.maxlength = maxlength;
	}
}
