/*
 *	File Name:	CountTag.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/07/25		2.2.0	numberFormat, numberFormatKey 지원
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import java.text.NumberFormat;
import java.util.Collection;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * 데이터건수를 출력.
 *
 * <ul type='square'>
 * <li>(M)listId
 * <li>(O)messageKey
 * <li>(O)numberFormat
 * <li>(O)numberFormatKey
 * </ul>
 */
public class CountTag extends javax.servlet.jsp.tagext.TagSupport {
	String listId;
	String messageKey;
	String numberFormatValue, numberFormatKey;

	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
			Collection collection = (Collection)pageContext.findAttribute( listId );

			int count = ( collection == null ? 0 : collection.size() );
			if( messageKey != null )
				out.print( htmlpage.getMessageHandler().getMessage(messageKey, String.valueOf(count)) );
			else if( numberFormatValue != null ) {
				NumberFormat numberFormat = new java.text.DecimalFormat( numberFormatValue );
				out.print( numberFormat.format(count) );
			} else if( numberFormatKey != null ) {
				String numberFormatValue = htmlpage.getMessageHandler().getMessage( numberFormatKey );
				if( numberFormatValue != null ) {
					NumberFormat numberFormat = new java.text.DecimalFormat( numberFormatValue );
					out.print( numberFormat.format(count) );
				}
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}
		return EVAL_PAGE;
	}

	public void setListId( String listId ) {
		this.listId = listId;
	}

	public void setMessageKey( String messageKey ) {
		this.messageKey = messageKey;
	}

	public void setNumberFormat( String numberFormatValue ) {
		this.numberFormatValue = numberFormatValue;
	}

	public void setNumberFormatKey( String numberFormatKey ) {
		this.numberFormatKey = numberFormatKey;
	}
}
