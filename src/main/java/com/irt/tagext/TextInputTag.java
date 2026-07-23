/*
 *	File Name:	TextInputTag.java
 *	Version:	2.2.3c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.3c	신규 UI/UX 적용
 *	stghr12		2011/04/22		2.2.3	getValue() -> getValueOne()
 *	stghr12		2009/10/31		2.2.2	TagUtility 사용
 *	stghr12		2008/03/31		2.2.1	PatternRecordFormat -> RecordFormat
 *	stghr12		2007/10/31		2.1.0	postStringFormat 추가
 *	stghr12		2006/09/15		2.0.1	TAG안에 value에 대해서 HtmlUtility.toScriptString() -> HtmlUtility.toHtmlString()
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
 * &lt;input type="text"&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)defaultValue
 * <li>(O)id
 * <li>(M)key
 * <li>(O)format
 * <li>(O)mandatory
 * <li>(O)maxlength
 * <li>(O)modified
 * <li>(O)name
 * <li>(O)onBlur
 * <li>(O)onKeyDown
 * <li>(O)onlyInput
 * <li>(O)placeholder
 * <li>(O)postString | postStringFormat | postStringKey
 * <li>(O)readonly
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * </ul>
 */
public class TextInputTag extends InputTag {
	int maxlength = -1;
	String postString, postStringKey, placeholder;
	com.irt.data.format.RecordFormat postStringFormat;

	@Override
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			if( !onlyInput && mandatoryType == INPUT_INFORMATION ) {
				out.print( "<span class='info'>" );
				if( format != null )
					out.print( getFormatValue() );
				else {
					Object value = getValueOne();
					if( value != null )
						out.print( HtmlUtility.toHtmlString(value) + getPostString() );
				}
				out.print( "</span>" );
			} else {
				String name = getName();
				int maxlength = ( this.maxlength < 0 && field != null ? field.getMaxLength() : this.maxlength );
				String attribute = getAttribute( HtmlPage.INPUT_TEXT );
				if( attribute == null ) {
					attribute = "";
				}
				if( this.placeholder != null ) {
					String placeholderAttr = msghandler.getMessage( this.placeholder );
					if( placeholderAttr != null ) {
						placeholderAttr = "placeholder='" + placeholderAttr + "'";
						if( attribute.length() > 0 ) {
							attribute = placeholderAttr + " " + attribute;
						} else {
							attribute = placeholderAttr;
						}
					}
				}
				if( mandatoryType == HtmlPage.INPUT_MANDATORY ) {
					attribute += " data-mandatory='true'";
				}

				utility.printText( out, name, getValueOne(), maxlength, attribute );
				out.print( getPostString() );

				if( htmlpage.getContentGroup().autoValidation )
					utility.putValidationScript( field, mandatoryType, htmlpage.getFormName() +"."+ name );
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}

		return EVAL_PAGE;
	}

	String getPostString() {
		if( postString != null )
			return postString;
		else if( postStringKey != null )
			return msghandler.getMessage( postStringKey );
		else if( postStringFormat != null )
			return postStringFormat.format( TagUtility.getMap(pageContext, id), msghandler );
		else
			return "";
	}

	public void setMaxlength( int maxlength ) {
		this.maxlength = maxlength;
	}

	public void setPlaceholder( String placeholder ) {
		this.placeholder = placeholder;
	}

	public void setPostString( String postString ) {
		this.postString = postString;
	}

	public void setPostStringFormat( String postStringFormat ) {
		this.postStringFormat = com.irt.data.format.PatternRecordFormat.getInstance( postStringFormat );
	}

	public void setPostStringKey( String postStringKey ) {
		this.postStringKey = postStringKey;
	}
}
