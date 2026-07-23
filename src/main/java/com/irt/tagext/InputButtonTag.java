/*
 *	File Name:	InputButtonTag.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/02/28		2.2.1	styleId를 'ibtn_null'로 출력하는 오류수정
 *	stghr12		2009/10/31		2.2.0	HtmlPage(v2.2.3) 적용
 *	stghr12		2006/12/01		2.1.0	HtmlPage.ContentGroup.getValidableField() 사용
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * 버튼(입력용:선택버튼 등) 출력.
 *
 * <ul type='square'>
 * <li>(O)href
 * <li>(O)imageSrc
 * <li>(O)key
 * <li>(O)naming
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * <li>(M)type
 * </ul>
 *
 * @see HtmlPage#getInputStatus() HtmlPage.getInputStatus()
 * @see HtmlPage#getContentGroup() HtmlPage.getContentGroup()
 * @see HtmlPage#getInputButtonTag( String, String, String, String, String, String, String, String, String ) HtmlPage.getInputButtonTag()
 */
public class InputButtonTag extends javax.servlet.jsp.tagext.TagSupport {
	String href, imageSrc, key, naming;
	String style, styleClass, styleId;
	String title, titleKey, type;

	public int doEndTag() throws JspException {
		HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
		if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION )
			return EVAL_PAGE;
		else if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_READONLY )
			return EVAL_PAGE;
		else {
			try {
				com.irt.data.ValidableField field = htmlpage.getContentGroup().getValidableField( key );
				if( field != null && field.readonly() )
					return EVAL_PAGE;
			} catch( ClassCastException castEx ) {
			} catch( NullPointerException nullEx ) {}
		}

		JspWriter out = pageContext.getOut();
		try {
			String title = this.title;
			if( title == null && titleKey != null )
				title = htmlpage.getMessageHandler().getMessage( titleKey );
			String attribute = TagUtility.getStyleAttribute( style, ( styleId != null ? styleId : (key != null ? "ibtn_"+ key : null) ), styleClass );

			(new TagUtility(htmlpage)).printInputButton( out, type, key, href, naming, imageSrc, title, attribute );
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}
		return EVAL_PAGE;
	}

	public void setHref( String href ) {
		this.href = href;
	}

	public void setImageSrc( String imageSrc ) {
		this.imageSrc = imageSrc;
	}

	public void setKey( String key ) {
		this.key = key;
	}

	public void setNaming( String naming ) {
		this.naming = naming;
	}

	public void setStyle( String style ) {
		this.style = style;
	}

	public void setStyleClass( String styleClass ) {
		this.styleClass = styleClass;
	}

	public void setStyleId( String styleId ) {
		this.styleId = styleId;
	}

	public void setTitle( String title ) {
		this.title = title;
	}

	public void setTitleKey( String titleKey ) {
		this.titleKey = titleKey;
	}

	public void setType( String type ) {
		this.type = type;
	}
}
