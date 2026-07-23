/*
 *	File Name:	HtmlTag.java
 *	Version:	2.0.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2021/06/01		2.0.1	add html lang tag
 *	stghr12		2006/02/28		2.0.0	create(InitPageTag -> HtmlTag)
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import com.irt.resbdl.PageMessageKeys;
import com.irt.servlet.ServletUtility;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * &lt;html&gt; 출력.
 *
 * <ul type='square'>
 * <li>(M)errorPage
 * </ul>
 */
public class HtmlTag extends javax.servlet.jsp.tagext.BodyTagSupport {
	String errorPage = null;

	@Override
	public int doEndTag() throws JspException {
		try {
			if( pageContext.findAttribute("htmlpage") != null ) {
				if( com.irt.rbm.RBMSystem.getSystemEnvBool("SYS", "DatabaseResource;usePageEdit", false) ) {
					String pageId = PageMessageKeys.getPageId((HtmlPage)pageContext.findAttribute("htmlpage"));
					String reqId = PageMessageKeys.getRequestId((javax.servlet.http.HttpServletRequest)pageContext.getRequest());
					PageMessageKeys.putPage((javax.servlet.http.HttpServletRequest)pageContext.getRequest(), pageId);
					pageContext.getOut().print("<div style='display:hidden;' class='msgres' data-page-id='" + pageId + "' data-req-id='"+reqId+"'>");
					pageContext.getOut().print("</div>");
					PageMessageKeys.putPage((javax.servlet.http.HttpServletRequest)pageContext.findAttribute("request"), pageId);
				}

				pageContext.getOut().print( "</html>" );
				return EVAL_PAGE;
			}

			pageContext.getOut().clear();
			pageContext.forward( errorPage );

			return SKIP_PAGE;
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		} catch( javax.servlet.ServletException servletEx ) {
			throw new JspException( servletEx );
		}
	}

	@Override
	public int doStartTag() throws JspException {
		if( pageContext.findAttribute("htmlpage") != null ) {
			try {
				Locale selectedLocale = ServletUtility.getLocale((HttpServletRequest)pageContext.getRequest());
				pageContext.getOut().print( "<html data-lang='" + (selectedLocale == null ? "" : selectedLocale.getLanguage()) + "'"
						+ (selectedLocale==null ? "" : (" lang='"+ selectedLocale.getLanguage() + "'")) + ">" );
			} catch( java.io.IOException ioEx ) {
				throw new JspException( ioEx );
			}
			return EVAL_BODY_INCLUDE;
		}
		return SKIP_BODY;
	}

	/**
	 * 초기화 중 에러발생시 forward될 Page
	 */
	public void setErrorPage( String errorPage ) throws JspException {
		this.errorPage = errorPage;
	}
}
