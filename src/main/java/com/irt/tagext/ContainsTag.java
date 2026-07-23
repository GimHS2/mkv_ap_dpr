/*
 *	File Name:	ContainsTag.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/08/29		2.2.0	com.irt.servlet.ParameterMap 사용, pageContext.findAttribute("request") 사용
 *	stghr12		2006/02/28		2.0.0	create(IfContainsTag -> ContainsTag)
 *
**/

package com.irt.tagext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * 특정한 Map에 특정한 Key의 값을 포함할 경우에 body 출력.
 *
 * <ul type='square'>
 * <li>(O)copyId
 * <li>(M)id
 * <li>(O)key
 * </ul>
 *
 * (사용예)
 * <pre>
 * &lt;mtl:containsTag id="ID"&gt;
 *   return ( pageContext.findAttribute("ID") != null )
 * &lt;/mtl:containsTag&gt;
 *
 * &lt;mtl:containsTag id="ID" key="KEY"&gt;
 *   Object obj = pageContext.findAttribute("ID");
 *   return ( obj != null && obj instanceof java.util.Map && ((java.util.Map)obj).containsKey("KEY") );
 * &lt;/mtl:containsTag&gt;
 *
 * &lt;mtl:containsTag id="request" key="KEY"&gt;
 *   return pageContext.getRequest().getParameterMap().containsKey("KEY");
 * &lt;/mtl:containsTag&gt;
 * </pre>
 */
public class ContainsTag extends javax.servlet.jsp.tagext.BodyTagSupport {
	String copyId, id;
	String key = null;

	public int doStartTag() throws JspException {
		Object obj = pageContext.findAttribute( id );
		if( obj == null && "request".equals(id) )
			obj = new com.irt.servlet.ParameterMap( pageContext.getRequest() );
		else if( obj instanceof javax.servlet.http.HttpServletRequest )
			obj = new com.irt.servlet.ParameterMap( (javax.servlet.http.HttpServletRequest)obj );

		if( obj == null )
			return SKIP_BODY;

		if( copyId != null ) pageContext.setAttribute( copyId, obj, PageContext.PAGE_SCOPE );
		if( key == null )
			return EVAL_BODY_INCLUDE;
		else if( obj instanceof java.util.Map )
			return( ((java.util.Map)obj).containsKey(key) ? EVAL_BODY_INCLUDE : SKIP_BODY );
		else
			return SKIP_BODY;
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
