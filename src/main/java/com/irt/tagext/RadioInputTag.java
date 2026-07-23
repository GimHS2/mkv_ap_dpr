/*
 *	File Name:	RadioInputTag.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/04/22		2.2.1	getValue() -> getValueOne()
 *	stghr12		2009/10/31		2.2.0	TagUtility 사용
 *	stghr12		2007/04/30		2.1.1	codeValues의 값이 ALL일 때 msghandler.getMessageValue(prefixKey + "ALL")를 우선 사용
 *	stghr12		2006/12/01		2.1.0	prefixKey가 null일 경우 처리
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * &lt;input type="radio"&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)codeValues
 * <li>(O)defaultValue
 * <li>(O)id
 * <li>(M)key
 * <li>(O)mandatory
 * <li>(O)modified
 * <li>(O)name
 * <li>(O)onBlur
 * <li>(O)onKeyDown
 * <li>(O)prefixKey
 * <li>(O)readonly
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * </ul>
 */
public class RadioInputTag extends InputTag {
	String prefixKey;
	String[] codeValues;

	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			String prefixKey = this.prefixKey;
			String[] codeValues = this.codeValues;
			if( field != null ) {
				if( prefixKey == null ) prefixKey = field.getPrefixKey();
				if( codeValues == null ) codeValues = field.getValidValues();
			}

			Object value = getValueOne();
			if( value != null && !(value instanceof String) ) value = value.toString();
			if( mandatoryType == INPUT_INFORMATION ) {
				if( value != null ) {
					String[][] optionValues = utility.makeOptionValues( prefixKey, new String[] { (String)value } );
					out.print( optionValues[0][1] );
				}
			} else {
				String name = getName();
				String attribute = getAttribute( HtmlPage.INPUT_RADIO );

				utility.printRadio( out, name, value, prefixKey, codeValues, attribute );
				if( htmlpage.getContentGroup().autoValidation )
					utility.putValidationScript( field, mandatoryType, htmlpage.getFormName() +"."+ name );
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}

		return EVAL_PAGE;
	}

	public void setCodeValues( String codeValues ) {
		this.codeValues = codeValues.split( "," );
	}

	public void setPrefixKey( String prefixKey ) {
		this.prefixKey = prefixKey;
	}
}
