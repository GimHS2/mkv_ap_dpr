/*
 *	File Name:	CheckInputTag.java
 *	Version:	2.2.0c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.0c	신규 UI/UX 적용
 *	stghr12		2009/10/31		2.2.0	TagUtility 사용
 *	stghr12		2006/12/01		2.1.0	값이 배열일 경우 처리
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/06/07		1.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * &lt;input type="checkbox"&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)checkValue
 * <li>(O)defaultValue
 * <li>(M)description | descriptionKey
 * <li>(O)id
 * <li>(M)key
 * <li>(O)mandatory
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
public class CheckInputTag extends InputTag {
	String checkValue = "Y";
	String description, descriptionKey;

	@Override
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			boolean checked = false;
			Object value = getValue();
			if( value instanceof Object[] )
				checked = com.irt.util.Arrays.contains( (Object[])value, checkValue );
			else
				checked = checkValue.equals( value );

			if( mandatoryType == INPUT_INFORMATION ) {
				if( value != null && checked )
					out.print( getDescription( checkValue ) );
			} else {
				String name = getName();

				utility.printCheckbox( out, name, checkValue, getDescription( checkValue ), checked, getAttribute(HtmlPage.INPUT_CHECK) );
				if( htmlpage.getContentGroup().autoValidation )
					utility.putValidationScript( field, mandatoryType, htmlpage.getFormName() +"."+ name );
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}

		return EVAL_PAGE;
	}

	public String getDescription( Object value ) {
		String messageKey;
		if( description != null )
			return description;
		else if( descriptionKey != null )
			messageKey = descriptionKey;
		else if( field != null )
			messageKey = field.getDescriptionKey();
		else
			return "";

		try {
			return msghandler.getMessageValue( messageKey + "_" + (String)value );
		} catch( java.util.MissingResourceException ex ) {
			return msghandler.getMessage( messageKey );
		}
	}

	public void setCheckValue( String checkValue ) {
		this.checkValue = checkValue;
	}

	public void setDescription( String description ) {
		this.description = description;
	}

	public void setDescriptionKey( String descriptionKey ) {
		this.descriptionKey = descriptionKey;
	}
}
