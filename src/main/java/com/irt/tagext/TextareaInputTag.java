/*
 *	File Name:	TextareaInputTag.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/04/22		2.2.1	getValue() -> getValueOne()
 *	stghr12		2009/10/31		2.2.0	TagUtility 사용
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * &lt;textarea&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)id
 * <li>(M)key
 * <li>(O)mandatory
 * <li>(O)modified
 * <li>(O)name
 * <li>(O)onBlur
 * <li>(O)onKeyDown
 * <li>(O)readonly
 * <li>(O)rows
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * </ul>
 */
public class TextareaInputTag extends InputTag {
	int rows = 3;

	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			String value = HtmlUtility.toHtmlString( getValueOne() );
			if( mandatoryType == INPUT_INFORMATION ) {
				out.print( "<pre class='textarea'" );
				if( rows > 0 ) out.print( " rows='"+ rows +"'" );
				out.print( ">"+ value +"</pre>" );
			} else {
				String name = getName();

				out.print( "<textarea name='"+ name +"'"+ getAttribute(HtmlPage.INPUT_TEXTAREA) );
				if( rows > 0 ) out.print( " rows='"+ rows +"'" );
				out.print( ">"+ value +"</textarea>" );

				if( htmlpage.getContentGroup().autoValidation )
					utility.putValidationScript( field, mandatoryType, htmlpage.getFormName() +"."+ name );
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}

		return EVAL_PAGE;
	}

	public void setRows( int rows ) {
		this.rows = rows;
	}
}
