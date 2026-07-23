/*
 *	File Name:	ButtonTag.java
 *	Version:	2.2.2c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2c	신규 UI/UX 적용
 *	GimHS		2012/12/31		2.2.2	onClick 속성 추가
 *	stghr12		2010/03/31		2.2.1	getWindowType() 변경사항 적용('@' 사용)
 *	stghr12		2009/10/31		2.2.0	HtmlPage(v2.2.3) 적용
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import java.util.MissingResourceException;
import java.util.Properties;


/**
 * 버튼 출력.
 *
 * <ul type='square'>
 * <li>(O)href
 * <li>(O)onClick
 * <li>(O)id
 * <li>(O)imageSrc
 * <li>(O)message
 * <li>(O)messageKey
 * <li>(O)name
 * <li>(O)style
 * <li>(O)styleClass
 * <li>(O)styleId
 * <li>(O)title | titleKey
 * <li>(M)type
 * </ul>
 */
public class ButtonTag extends javax.servlet.jsp.tagext.TagSupport {
	public final static String BUTTON_CLOSE				= "close";
	public final static String BUTTON_CLOSE_IF			= "close_if";
	public final static String BUTTON_RETURN			= "return";
	public final static String BUTTON_SUBMIT			= "submit";
	public final static String BUTTON_RESET				= "reset";
	public final static String BUTTON_BUTTON			= "button";

	public final static String BUTTON_STYLE_PRIMARY		= "primary";
	public final static String BUTTON_STYLE_SECONDARY	= "secondary";

	String id, name;
	String onClick, icon, iconPosition;
	String style, styleClass, styleId;
	String title, titleKey, type;
	String message, messageKey;

	@Override
	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
			Properties properties = htmlpage.getProperty();

			String type = this.type;
			String buttonType = properties.getProperty( "jsp.button."+ type +".buttonType" );

			if( buttonType == null ) {
				if( BUTTON_SUBMIT.equals(type) ) {
					buttonType = BUTTON_SUBMIT;
				} else if( BUTTON_RESET.equals(type) ) {
					buttonType = BUTTON_RESET;
				} else {
					buttonType = BUTTON_BUTTON;
				}
			}

			if( BUTTON_CLOSE_IF.equals(type) ) {
				String windowType = htmlpage.getWindowType();
				if( windowType == null || !"sub".equals(windowType.split("@")[0]) ) return EVAL_PAGE;
				type = BUTTON_CLOSE;
			}

			String onClick = this.onClick;
			if( onClick == null ) {
				if( BUTTON_RETURN.equals(type) )
					onClick = htmlpage.getBackURL();
				else if( BUTTON_CLOSE.equals(type) ) {
					if( com.irt.servlet.ServletModel.MODE_MODIFY.equals(htmlpage.getMode())
							|| com.irt.servlet.ServletModel.MODE_MULTIMODIFY.equals(htmlpage.getMode())
							|| com.irt.servlet.ServletModel.MODE_REGIST.equals(htmlpage.getMode())
							|| com.irt.servlet.ServletModel.MODE_UPLOAD.equals(htmlpage.getMode()) )
						onClick = properties.getProperty( "jsp.button."+ type +".href.reload" );
					else
						onClick = properties.getProperty( "jsp.button."+ type +".href" );
				} else
					onClick = properties.getProperty( "jsp.button."+ type +".href" );
				if( (onClick == null || onClick.length() == 0)
						&& (BUTTON_CLOSE.equals(type) || BUTTON_RETURN.equals(type)) ) return EVAL_PAGE;
			}

			if( BUTTON_RETURN.equals(type) ) {
				onClick = "location.href=\"" + onClick + "\"";
			}

			String title = this.title;
			if( title == null && titleKey != null )
				title = htmlpage.getMessageHandler().getMessage( titleKey );

			String attribute = "";
			String btnClass = null;
			if( BUTTON_STYLE_PRIMARY.equals(this.styleClass) || BUTTON_STYLE_SECONDARY.equals(this.styleClass) ) {
				btnClass = this.styleClass;
				btnClass = properties.getProperty( "jsp.styleClass.button." + btnClass );
			} else if( this.styleClass != null ) {
				btnClass = properties.getProperty( "jsp.styleClass.button." + this.styleClass );
				if( btnClass == null ) {
					btnClass = this.styleClass;
				}
			} else {
				if( BUTTON_SUBMIT.equals(buttonType) ) {
					btnClass = properties.getProperty( "jsp.styleClass.button." + BUTTON_STYLE_PRIMARY );
				} else {
					btnClass = properties.getProperty( "jsp.button."+ type +".styleClass" );
					if( btnClass != null ) {
						btnClass = properties.getProperty( "jsp.styleClass.button." + btnClass, btnClass );
					} else {
						btnClass = properties.getProperty( "jsp.styleClass.button." + BUTTON_STYLE_SECONDARY );
					}
				}
			}

			if( btnClass != null ) {
				attribute += " class='" + btnClass + "'";
			}

			if( style != null ) {
				attribute += " style='" + this.style + "'";;
			}

			out.print( "<button type='" + buttonType + "'" );

			if( this.id != null ) {
				out.print( " id='" + this.id + "'" );
			}

			if( this.name != null ) {
				out.print( " name='" + this.name + "'" );
			}

			if( !BUTTON_SUBMIT.equals(type) ) {
				if( onClick != null ) {
					out.print( " onclick='"+ onClick +"'"+ ( title != null ? " title='"+ title +"'" : "" ) );
				}
			}
			out.print( attribute + ">" );
			String icon = this.icon;
			if( icon == null ) {
				icon = properties.getProperty( "jsp.button."+ type +".icon" );
			}
			if( icon != null && !"right".equals(iconPosition) ) {
				out.print( "<img src='"+ icon +"' class='btn-icon-left'/>" );
			}

			String message = "";
			if( this.message != null ) {
				message = this.message;
			} else if( this.messageKey != null ) {
				message = htmlpage.getMessageHandler().getMessage( this.messageKey );
			} else {
				String messageKey = properties.getProperty( "jsp.button."+ type +".messageKey" );
				if( messageKey == null ) {
					messageKey = "jsp.BTN_" + type.toUpperCase();
				}
				try {
					message = htmlpage.getMessageHandler().getMessageValue( "jsp.BTN_" + type.toUpperCase() );
				} catch( MissingResourceException ex ) {
					message = "&nbsp;";
				}
			}
			out.print( message );

			if( icon != null && "right".equals(iconPosition) ) {
				out.print( "<img src='"+ icon +"' class='btn-icon-right'/>" );
			}

			out.print( "</button>" );
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}
		return EVAL_PAGE;
	}

	public void setIcon( String icon ) {
		this.icon = icon;
	}

	public void setIconPosition( String iconPosition ) {
		this.iconPosition = iconPosition;
	}

	@Override
	public void setId( String id ) {
		this.id = id;
	}

	public void setMessage( String message ) {
		this.message = message;
	}

	public void setMessageKey( String messageKey ) {
		this.messageKey = messageKey;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public void setOnClick( String onClick ) {
		this.onClick = onClick;
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
