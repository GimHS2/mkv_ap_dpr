/*
 *	File Name:	ValueTag.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/03/31		2.2.0	TagUtility.getValue() -> TagUtility.getValueOne()
 *	stghr12		2006/02/28		2.0.0	create(GetValueTag -> ValueTag)
 *	stghr12		2004/02/05		1.0.0	version 관리
 *	stghr12		2002/10/02				create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlUtility;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * Map에 있는 값을 출력.
 *
 * <ul type='square'>
 * <li>(O)encodeHTML
 * <li>(O)encodeScript
 * <li>(M)id
 * <li>(M)key
 * </ul>
 */
public class ValueTag extends javax.servlet.jsp.tagext.TagSupport {
	boolean encodeHTML = true, encodeScript = false;
	String id;
	String key;

	@Override
	public int doEndTag() throws JspException {
		Object value = TagUtility.getValueOne( pageContext, id, key );
		if( value != null ) {
			JspWriter out = pageContext.getOut();
			try {
				if( encodeHTML )
					out.print( HtmlUtility.toHtmlString(value.toString()) );
				else if( encodeScript )
					out.print( HtmlUtility.toScriptString(value.toString()) );
				else
					out.print( value.toString() );
			} catch( java.io.IOException ioEx ) {
				throw new JspException( ioEx );
			}
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

	@Override
	public void setId( String id ) {
		this.id = id;
	}

	public void setKey( String key ) {
		this.key = key;
	}
}
