/*
 *	File Name:	FormatValueTag.java
 *	Version:	2.1.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2006/12/01		2.1.0	HtmlPage.ContentGroup.getField() 사용
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.data.format.PatternRecordFormat;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * Map에서 값을 가지고 와서 format방식으로 출력.
 *
 * <ul type='square'>
 * <li>(O)encodeHTML
 * <li>(O)encodeScript
 * <li>(M)id
 * <li>(M)key
 * </ul>
 * <ul type='square'>
 * <li>(M)id
 * <li>(M)format
 * </ul>
 *
 * @see RecordFormat RecordFormat
 * @see PatternRecordFormat PatternRecordFormat
 * @see HtmlPage#getContentGroup() HtmlPage.getContentGroup()
 */
public class FormatValueTag extends javax.servlet.jsp.tagext.TagSupport {
	boolean encodeHTML = true, encodeScript = false;
	String format, id, key;

	public int doEndTag() throws JspException {
		Map map = TagUtility.getMap( pageContext, id );
		HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );

		String value = null;
		if( format != null )
			value = PatternRecordFormat.getInstance( format ).format( map, htmlpage.getMessageHandler() );
		else {
			try {
				com.irt.data.format.RecordFormat format = htmlpage.getContentGroup().getField( key );
				value = format.format( map, htmlpage.getMessageHandler() );
			} catch( ClassCastException castEx ) {
			} catch( NullPointerException nullEx ) {}

			if( value == null ) {
				try {
					value = map.get(key).toString();
				} catch( NullPointerException nullEx ) {
					return EVAL_PAGE;
				}
			}
		}

		JspWriter out = pageContext.getOut();
		try {
			if( format != null )
				out.print( value );
			else if( encodeHTML )
				out.print( HtmlUtility.toHtmlString(value) );
			else if( encodeScript )
				out.print( HtmlUtility.toScriptString(value) );
			else
				out.print( value );
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

	public void setFormat( String format ) {
		this.format = format;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public void setKey( String key ) {
		this.key = key;
	}
}
