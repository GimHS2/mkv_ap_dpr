/*
 *	File Name:	ContentGroupTag.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2009/10/31		2.2.1	HtmlPage(v2.2.3) 적용
 *	stghr12		2009/01/31		2.2.0	message, messageKey 추가
 *	stghr12		2006/12/01		2.1.0	validationType을 int형에서 char형으로 변경
 *	stghr12		2006/02/28		2.0.0	version up
 *
**/

package com.irt.tagext;

import com.irt.data.format.PatternRecordFormat;
import com.irt.html.HtmlPage;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * body를 contentGroup으로 묶어주는 Tag.
 *
 * <ul type='square'>
 * <li>(O)description | descriptionKey
 * <li>(O)fieldSetId
 * <li>(M)groupId
 * <li>(O)mandatory
 * <li>(O)message | messageKey
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)type
 * <li>(O)validation
 * </ul>
 *
 * @see HtmlPage#getContentGroup() HtmlPage.getContentGroup()
 * @see HtmlPage#popContentGroup() HtmlPage.popContentGroup()
 * @see HtmlPage#pushContentGroup( String, AbstractFieldSet, Boolean, Boolean ) HtmlPage.pushContentGroup()
 */
public class ContentGroupTag extends javax.servlet.jsp.tagext.BodyTagSupport {
	String fieldSetId, groupId;
	String description, descriptionKey;
	String message, messageKey;
	String type;
	String style, styleClass;
	Boolean autoMandatory = null;
	Boolean autoValidation = null;

	HtmlPage htmlpage;

	public int doEndTag() throws JspException {
		htmlpage.popContentGroup();

		JspWriter out = pageContext.getOut();
		try {
			String html = htmlpage.getProperty().getProperty( "jsp.contentGroup."+ type +".closeTag" );
			if( html != null ) out.print( html );
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}
		return EVAL_PAGE;
	}

	public int doStartTag() throws JspException {
		com.irt.data.AbstractFieldSet fieldSet = null;
		if( fieldSetId != null )
			fieldSet = (com.irt.data.AbstractFieldSet)pageContext.findAttribute( fieldSetId );

		htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
		htmlpage.pushContentGroup( groupId, fieldSet, autoMandatory, autoValidation );

		String description = this.description;
		if( description == null && this.descriptionKey != null )
			description = htmlpage.getMessageHandler().getMessage( descriptionKey );

		String message = this.message;
		if( message == null && this.messageKey != null )
			message = htmlpage.getMessageHandler().getMessage( messageKey );

		JspWriter out = pageContext.getOut();
		try {
			String format = htmlpage.getProperty().getProperty( "jsp.contentGroup."+ type +".openTag" );
			if( format != null ) {
				Map<String, Object> recordMap = new java.util.HashMap<String, Object>();

				recordMap.put( "groupId", groupId );
				recordMap.put( "style", style );
				recordMap.put( "styleClass", styleClass );
				recordMap.put( "description", description );
				recordMap.put( "message", message );

				out.print( PatternRecordFormat.getInstance(format).format( recordMap, htmlpage.getMessageHandler() ) );

				if( (description != null && description.length() > 0) || (message != null && message.length() > 0) ) {
					format = htmlpage.getProperty().getProperty( "jsp.contentGroup."+ type +".description" );
					if( format != null )
						out.print( PatternRecordFormat.getInstance(format).format( recordMap, htmlpage.getMessageHandler() ) );
				}
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}
		return EVAL_BODY_INCLUDE;
	}

	public void setDescription( String description ) {
		this.description = description;
	}

	public void setDescriptionKey( String descriptionKey ) {
		this.descriptionKey = descriptionKey;
	}

	public void setFieldSetId( String fieldSetId ) {
		this.fieldSetId = fieldSetId;
	}

	public void setGroupId( String groupId ) {
		this.groupId = groupId;
	}

	public void setMandatory( boolean autoMandatory ) {
		this.autoMandatory = ( autoMandatory ? Boolean.TRUE : Boolean.FALSE );
	}

	public void setMessage( String message ) {
		this.message = message;
	}

	public void setMessageKey( String messageKey ) {
		this.messageKey = messageKey;
	}

	public void setStyle( String style ) {
		this.style = style;
	}

	public void setStyleClass( String styleClass ) {
		this.styleClass = styleClass;
	}

	public void setType( String type ) {
		this.type = type;
	}

	public void setValidation( boolean autoValidation ) {
		this.autoValidation = ( autoValidation ? Boolean.TRUE : Boolean.FALSE );
	}
}
