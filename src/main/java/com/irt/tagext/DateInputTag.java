/*
 *	File Name:	DateInputTag.java
 *	Version:	2.2.0c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.0c	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlUtility;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * &lt;input type="text"&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)id
 * <li>(M)key
 * <li>(O)format
 * <li>(O)mandatory
 * <li>(O)modified
 * <li>(O)name
 * <li>(O)readonly
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * </ul>
 */
public class DateInputTag extends InputTag {
	final static String INPUT_DATE			= "date";

	@Override
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			if( mandatoryType == INPUT_INFORMATION ) {
				if( format != null )
					out.print( getFormatValue() );
				else {
					Object value = getValueOne();
					if( value != null )
						out.print( HtmlUtility.toHtmlString(value) );
				}
			} else {
				String name = getName();
				String dateFormat = null;

				if( dateFormat == null ) {
					try {
						dateFormat = msghandler.getMessageValue( "jsp.DATEPICKER_DATE_FORMAT" );
					} catch( java.util.MissingResourceException  ex ) {
						dateFormat = "yy-mm-dd";
					}
				}

				int maxlength = 0;
				if( dateFormat != null ) {
					maxlength = dateFormat.length();
					for( int i = 0; i < dateFormat.length(); i++) {
						if( dateFormat.charAt(i) == 'y') {
							maxlength++;
						}
					}
				}
				utility.printText( out, name, getValueOne(), maxlength, getAttribute(INPUT_DATE) );

				if( htmlpage.getContentGroup().autoValidation )
					utility.putValidationScript( field, mandatoryType, htmlpage.getFormName() +"."+ name );
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}

		return EVAL_PAGE;
	}
}
