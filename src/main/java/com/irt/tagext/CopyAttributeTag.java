/*
 *	File Name:	CopyAttributeTag.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/08/29		2.2.0	com.irt.servlet.ParameterMap 사용, pageContext.findAttribute("request") 사용
 *	stghr12		2007/10/31		2.1.0	create
 *
**/

package com.irt.tagext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 *
 * <ul type='square'>
 * <li>(M)copyId
 * <li>(M)id
 * </ul>
 */
public class CopyAttributeTag extends javax.servlet.jsp.tagext.TagSupport {
	String copyId, id;

	public int doEndTag() throws JspException {
		Object obj = pageContext.findAttribute( id );
		if( obj == null && "request".equals(id) )
			obj = new com.irt.servlet.ParameterMap( pageContext.getRequest() );
		else if( obj instanceof javax.servlet.http.HttpServletRequest )
			obj = new com.irt.servlet.ParameterMap( (javax.servlet.http.HttpServletRequest)obj );
		if( obj != null ) pageContext.setAttribute( copyId, obj, PageContext.PAGE_SCOPE );

		return EVAL_PAGE;
	}

	public void setCopyId( String copyId ) {
		this.copyId = copyId;
	}

	public void setId( String id ) {
		this.id = id;
	}
}
