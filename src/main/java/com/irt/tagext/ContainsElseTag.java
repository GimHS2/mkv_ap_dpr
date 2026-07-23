/*
 *	File Name:	ContainsElseTag.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2009/10/31		2.2.0	create
 *
**/

package com.irt.tagext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * 특정한 Map에 특정한 Key의 값을 포함하지 않을 경우에 body 출력.
 *
 * <ul type='square'>
 * <li>(O)copyId
 * <li>(M)id
 * <li>(O)key
 * </ul>
 */
public class ContainsElseTag extends javax.servlet.jsp.tagext.BodyTagSupport {
	String copyId, id;
	String key = null;

	public int doStartTag() throws JspException {
		Object obj = pageContext.findAttribute( id );
		if( obj == null && "request".equals(id) )
			obj = new com.irt.servlet.ParameterMap( pageContext.getRequest() );
		else if( obj instanceof javax.servlet.http.HttpServletRequest )
			obj = new com.irt.servlet.ParameterMap( (javax.servlet.http.HttpServletRequest)obj );

		if( obj == null )
			return EVAL_BODY_INCLUDE;

		if( copyId != null ) pageContext.setAttribute( copyId, obj, PageContext.PAGE_SCOPE );
		if( key == null )
			return SKIP_BODY;
		else if( obj instanceof java.util.Map )
			return( ((java.util.Map)obj).containsKey(key) ? SKIP_BODY : EVAL_BODY_INCLUDE );
		else
			return EVAL_BODY_INCLUDE;
	}

	public void setCopyId( String copyId ) {
		this.copyId = copyId;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public void setKey( String key ) {
		this.key = key;
	}
}
