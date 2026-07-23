/*
 *	File Name:	YNRadioInputTag.java
 *	Version:	2.0.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	create
 *
**/

package com.irt.tagext;

import javax.servlet.jsp.JspException;

/**
 * &lt;input type="radio"&gt; 출력(예/아니오 용).
 *
 * <ul type='square'>
 * <li>(O)defaultValue
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
public class YNRadioInputTag extends RadioInputTag {
	public int doEndTag() throws JspException {
		prefixKey = "PUB_WHETHER_";
		codeValues = new String[] { "Y", "N" };

		return super.doEndTag();
	}
}
