/*
 *	File Name:	FormTag.java
 *	Version:	2.2.3c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2024/07/31		2.2.3c	doStartTag() : HtmlUtility.toHtmlString으로 XSS 공격 방지 기능 추가
 *	jbaek		2019/01/30		2.2.2c	pageEdit 적용
 *	stghr12		2009/10/31		2.2.2	HtmlPage(v2.2.3) 적용
 *	stghr12		2008/03/31		2.2.1	com.irt.system.SystemConfig -> com.irt.servlet.SystemConfig
 *	stghr12		2007/11/30		2.2.0	target 추가
 *	stghr12		2006/12/01		2.1.0	validationType을 int형에서 char형으로 변경
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.RBMSystem;
import com.irt.resbdl.PageMessageKeys;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * &lt;form&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)action
 * <li>(O)enctype
 * <li>(O)fieldSetId
 * <li>(O)madatory
 * <li>(O)method
 * <li>(M)name
 * <li>(O)onReset
 * <li>(O)onSubmit
 * <li>(O)target
 * <li>(O)validation
 * </ul>
 *
 * @see HtmlPage#setFormName( String ) HtmlPage.setFormName()
 * @see HtmlPage#getRequestURL() HtmlPage.getRequestURL()
 * @see HtmlPage#popContentGroup() HtmlPage.popContentGroup()
 * @see HtmlPage#pushContentGroup( String, AbstractFieldSet, Boolean, Boolean ) HtmlPage.pushContentGroup()
 */
public class FormTag extends javax.servlet.jsp.tagext.BodyTagSupport {
	String action;
	String enctype;
	String name;
	String fieldSetId;
	String method = "post";
	String target;
	String onReset, onSubmit;
	Boolean autoMandatory = null;
	Boolean autoValidation = null;

	HtmlPage htmlpage;

	@Override
	public int doEndTag() throws JspException {
		if( fieldSetId != null ) htmlpage.popContentGroup();

		JspWriter out = pageContext.getOut();
		try {
			if( RBMSystem.getSystemEnvBool("SYS", "DatabaseResource;usePageEdit", false) ) {
				String pageId = PageMessageKeys.getPageId((HtmlPage)pageContext.findAttribute("htmlpage"));
				String reqId = PageMessageKeys.getRequestId((javax.servlet.http.HttpServletRequest)pageContext.getRequest());
				PageMessageKeys.putPage((javax.servlet.http.HttpServletRequest)pageContext.getRequest(), pageId);
				pageContext.getOut().print("<div style='display:hidden;' class='msgres' data-page-id='" + pageId + "' data-req-id='"+reqId+"'>");
				pageContext.getOut().print("</div>");
			}

			out.print( "</form>" );
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}
		return EVAL_PAGE;
	}

	@Override
	public int doStartTag() throws JspException {
		htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
		htmlpage.setFormName( name );
		if( fieldSetId != null )
			htmlpage.pushContentGroup( null, (com.irt.data.AbstractFieldSet)pageContext.findAttribute(fieldSetId), autoMandatory, autoValidation );

		JspWriter out = pageContext.getOut();
		try {
			out.print( "<form name='"+ name +"' method='"+ method +"'" );
			if( action == null )
				out.print( " action='"+ HtmlUtility.toHtmlString(htmlpage.getRequestURL()) +"'" );
			else {
				com.irt.servlet.SystemConfig systemConfig = (com.irt.servlet.SystemConfig)pageContext.findAttribute( "systemConfig" );
				out.print( " action='"+ systemConfig.getClassURL() +"/"+ action +"'" );
			}
			if( enctype != null ) out.print( " enctype='"+ enctype +"'" );
			if( onReset != null ) out.print( " onReset='"+ onReset +"'" );
			if( onSubmit != null ) out.print( " onSubmit='"+ onSubmit +"'" );
			if( target != null ) out.print( " target='"+ target +"'" );
			out.print( " autoComplete='off'>" );
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}
		return EVAL_BODY_INCLUDE;
	}

	public void setAction( String action ) {
		this.action = action;
	}

	public void setEnctype( String enctype ) {
		this.enctype = enctype;
	}

	public void setFieldSetId( String fieldSetId ) {
		this.fieldSetId = fieldSetId;
	}

	public void setMandatory( boolean autoMandatory ) {
		this.autoMandatory = ( autoMandatory ? Boolean.TRUE : Boolean.FALSE );
	}

	public void setMethod( String method ) {
		this.method = method;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public void setOnReset( String onReset ) {
		this.onReset = onReset;
	}

	public void setOnSubmit( String onSubmit ) {
		this.onSubmit = onSubmit;
	}

	public void setTarget( String target ) {
		this.target = target;
	}

	public void setValidation( boolean autoValidation ) {
		this.autoValidation = ( autoValidation ? Boolean.TRUE : Boolean.FALSE );
	}
}
