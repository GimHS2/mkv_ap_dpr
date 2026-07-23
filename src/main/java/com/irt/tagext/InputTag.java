/*
 *	File Name:	InputTag.java
 *	Version:	2.2.2c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2c	신규 UI/UX 적용
 *	stghr12		2011/04/22		2.2.2	getValue(), getValueOne() 분리
 *	stghr12		2011/03/31		2.2.1	TagUtility.getValue() -> TagUtility.getValueOne()
 *	stghr12		2009/10/31		2.2.0	TagUtility 사용
 *	stghr12		2007/10/31		2.1.1	title, titleKey 추가
 *	stghr12		2006/12/01		2.1.0	inputType, inputHtmlType을 int형에서 char형으로 변경
 *										HtmlPage.ContentGroup.getValidableField() 사용
 *	stghr12		2006/10/21		2.0.1	doStartTag(): 에러가능성 수정
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.data.format.PatternRecordFormat;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.util.MessageHandler;
import javax.servlet.jsp.JspException;

/**
 * &lt;input&gt; 출력.
 *
 * <ul type='square'>
 * <li>(O)defaultValue
 * <li>(O)id
 * <li>(M)key
 * <li>(O)format
 * <li>(O)mandatory
 * <li>(O)modified
 * <li>(O)name
 * <li>(O)onBlur
 * <li>(O)onKeyDown
 * <li>(O)onlyInput
 * <li>(O)readonly
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * </ul>
 *
 * @see HtmlPage#getInputStatus() HtmlPage.getInputStatus()
 * @see HtmlPage#getContentGroup() HtmlPage.getContentGroup()
 */
public class InputTag extends javax.servlet.jsp.tagext.TagSupport {
	final static char INPUT_INFORMATION					= HtmlPage.INPUT_INFORMATION;
	final static char INPUT_READONLY					= HtmlPage.INPUT_READONLY;
	final static char INPUT_MANDATORY					= HtmlPage.INPUT_MANDATORY;
	final static char INPUT_OPTIONAL					= HtmlPage.INPUT_OPTIONAL;

	String defaultValue, id, key, name, modified;
	String style, styleClass, styleId;
	String onBlur, onKeyDown;
	String title, titleKey;
	boolean usedefault = true, mandatory, readonly, onlyInput = false;
	com.irt.data.format.RecordFormat format;

	char mandatoryType;
	HtmlPage htmlpage;
	TagUtility utility;
	MessageHandler msghandler;
	com.irt.data.ValidableField field;

	@Override
	public int doStartTag() throws JspException {
		htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
		msghandler = htmlpage.getMessageHandler();
		utility = new TagUtility( htmlpage );

		field = null;
		try {
			field = htmlpage.getContentGroup().getValidableField( key );
		} catch( ClassCastException castEx ) {
		} catch( NullPointerException nullEx ) {}

		if( !onlyInput && htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION )
			mandatoryType = INPUT_INFORMATION;
		else if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_READONLY )
			mandatoryType = INPUT_READONLY;
		else {
			if( usedefault ) {
				if( field == null || !htmlpage.getContentGroup().autoMandatory )
					mandatoryType = INPUT_OPTIONAL;
				else if( field.readonly() )
					mandatoryType = INPUT_READONLY;
				else if( !field.nullable() )
					mandatoryType = INPUT_MANDATORY;
				else
					mandatoryType = INPUT_OPTIONAL;
			} else {
				if( readonly )
					mandatoryType = INPUT_READONLY;
				else if( mandatory )
					mandatoryType = INPUT_MANDATORY;
				else {
					/*
					 * if( !readonly && field != null && htmlpage.getContentGroup().autoMandatory &&
					 * !field.nullable() ) { mandatoryType = INPUT_MANDATORY; } else { mandatoryType
					 * = INPUT_OPTIONAL; }
					 */
					mandatoryType = INPUT_OPTIONAL;
				}
			}
		}

		return SKIP_BODY;
	}

	String getAttribute( String inputType ) {
		StringBuffer attrbuf = new StringBuffer();

		attrbuf.append( utility.getInputAttribute(inputType, mandatoryType, modified, style, styleId, styleClass) );

		if( onBlur != null ) attrbuf.append( " onBlur='"+ onBlur +"'" );
		if( onKeyDown != null ) attrbuf.append( " onKeyDown='"+ onKeyDown +"'" );
		if( title != null )
			attrbuf.append( " title='"+ title +"'" );
		else if( titleKey != null )
			attrbuf.append( " title='"+ HtmlUtility.toHtmlString( msghandler.getMessage(titleKey) ) +"'" );

		return attrbuf.toString();
	}

	String getFormatValue() {
		if( id != null && format != null )
			return format.format( TagUtility.getMap(pageContext, id), msghandler );
		else
			return "";
	}

	String getName() {
		return( name == null ? key : name );
	}

	Object getValue() {
		if( id != null ) {
			Object object = TagUtility.getValue( pageContext, id, key );
			return( object != null ? object : defaultValue );
		}

		return defaultValue;
	}

	Object getValueOne() {
		if( id != null ) {
			Object object = TagUtility.getValueOne( pageContext, id, key );
			return( object != null ? object : defaultValue );
		}

		return defaultValue;
	}

	public void setDefaultValue( String defaultValue ) {
		this.defaultValue = defaultValue;
	}

	public void setFormat( String format ) {
		this.format = PatternRecordFormat.getInstance( format );
	}

	@Override
	public void setId( String id ) {
		this.id = id;
	}

	public void setKey( String key ) {
		this.key = key;
	}

	public void setMandatory( boolean mandatory ) {
		this.usedefault = false;
		this.mandatory = mandatory;
	}

	public void setModified( String modified ) {
		this.modified = modified;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public void setOnBlur( String onBlur ) {
		this.onBlur = onBlur;
	}

	public void setOnKeyDown( String onKeyDown ) {
		this.onKeyDown = onKeyDown;
	}

	public void setOnlyInput( boolean onlyInput ) {
		this.onlyInput = onlyInput;
	}

	public void setReadonly( boolean readonly ) {
		this.usedefault = false;
		this.readonly = readonly;
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
}
