/*
 *	File Name:	TitleTag.java
 *	Version:	2.2.1c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/01/30		2.2.1c	pageEdit 적용
 *	stghr12		2010/01/31		2.2.1	name을 사용하지 않는 오류 수정
 *	stghr12		2009/10/31		2.2.0	HtmlPage(v2.2.3) 적용
 *	stghr12		2006/12/01		2.1.0	key를 mandatory에서 optional로 변경
 *	stghr12		2006/02/28		2.0.0	create(SystemMessage -> MessageTag )
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * 타이틀 출력.
 *
 * <ul type='square'>
 * <li>(O)description | descriptionKey
 * <li>(O)key
 * <li>(O)mandatory
 * <li>(O)name
 * </ul>
 *
 * @see HtmlPage#getContentGroup() HtmlPage.getContentGroup()
 * @see HtmlPage#getFieldTitleAttribute( String, boolean ) HtmlPage.getFieldTitleAttribute()
 */
public class TitleTag extends javax.servlet.jsp.tagext.TagSupport {
	String description, descriptionKey;
	String key;
	String name;
	boolean usedefault = true, mandatory;

	@Override
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );

			String title = null;
			boolean mandatory = this.mandatory;

			if( description != null )
				title = description;
			else if( descriptionKey != null )
				title = com.irt.resbdl.PageMessageKeys.getMessageAndProcess(htmlpage, descriptionKey );

			com.irt.data.AbstractFieldSet fieldSet = htmlpage.getContentGroup().fieldSet;
			if( fieldSet != null && key != null ) {
				com.irt.data.AbstractField field = fieldSet.getField( key );
				if( field != null ) {
					if( title == null ) title = com.irt.resbdl.PageMessageKeys.getMessageAndProcess(htmlpage, field.getDescriptionKey() );
					if( usedefault && htmlpage.getContentGroup().autoMandatory && field instanceof com.irt.data.ValidableField )
						mandatory = ( !((com.irt.data.ValidableField)field).nullable() );
				}
			}
			if( title == null ) title = key;

			String styleClass;
			if( mandatory )
				styleClass = htmlpage.getProperty().getProperty( "jsp.styleClass.title.mandatory" );
			else
				styleClass = htmlpage.getProperty().getProperty( "jsp.styleClass.title.optional" );

			String attribute = ( key != null ? " id='title_"+ ( name == null ? key : name ) +"'" : "" );
			if( styleClass != null ) attribute += " class='"+ styleClass +"'";

			out.print( "<span "+ attribute +">"+ title +"</span>" );
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}
		return EVAL_PAGE;
	}

	public void setDescription( String description ) {
		this.description = description;
	}

	public void setDescriptionKey( String descriptionKey ) {
		this.descriptionKey = descriptionKey;
	}

	public void setKey( String key ) {
		this.key = key;
	}

	public void setMandatory( boolean mandatory ) {
		this.usedefault = false;
		this.mandatory = mandatory;
	}

	public void setName( String name ) {
		this.name = name;
	}
}
