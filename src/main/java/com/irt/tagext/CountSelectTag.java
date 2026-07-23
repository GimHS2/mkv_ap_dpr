/*
 *	File Name:	CountSelectTag.java
 *	Version:	2.2.0c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.0c	신규 UI/UX 적용
 *	stghr12		2008/08/29		2.2.0	htmlpage.getDefaultShowCounts() -> htmlpage.getProperty().getProperty("defaultShowCountList")
 *	stghr12		2006/12/01		2.1.0	HtmlPage.getDefaultShowCounts() 이용하도록 수정
 *										countlists sort여부 체크로직 오류 수정
 *										maxRows가 countlists의 최대값보다 클 경우 maxRows를 출력하지 않는 오류 수정
 *										throw new IllegalArgumentException( "unsorted '"+ counts[i] +"'" );
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.tagext;

import com.irt.html.HtmlPage;
import com.irt.util.MessageHandler;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * 표시건수 선택을 출력.
 *
 * <ul type='square'>
 * <li>(O)countList
 * <li>(O)customOption
 * <li>(O)customSelect
 * <li>(O)id
 * <li>(O)modified
 * <li>(O)style
 * <li>(O)styleClass
 * </ul>
 *
 * @see HtmlPage#getListIndexVariables() HtmlPage.getListIndexVariables()
 */
public class CountSelectTag extends javax.servlet.jsp.tagext.TagSupport {
	final static int[] DEFAULT_SHOWCOUNT_LIST			= new int[] { 10, 15, 20, 30, 40, 50, 100, 120, 150 };

	int[] countlists = null;
	String modified, style, styleClass, id, customOption;
	boolean customSelect = true;
	MessageHandler msghandler;

	@Override
	public int doEndTag() throws JspException {
		HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
		msghandler = htmlpage.getMessageHandler();
		int maxRows = htmlpage.getListIndexVariables()[1];

		int[] countlists = this.countlists;
		if( countlists == null ) {
			String countList = htmlpage.getProperty().getProperty( "defaultShowCountList" );
			if( countList != null ) {
				String[] counts = countList.split( "," );
				countlists = new int[counts.length];
				try {
					for( int i = 0; i < counts.length; i++ ) {
						countlists[i] = Integer.parseInt( counts[i] );
						if( countlists[i] <= 0 ) countlists[i] = -1;
						if( i > 0 ) {
							if( countlists[i] == countlists[i-1] )
								throw new IllegalArgumentException( "illegal defaultShowCountList '"+ countList +"': sort error.[0]" );
							else if( countlists[i] > 0 && (countlists[i] < countlists[i-1] || countlists[i-1] < 0) )
								throw new IllegalArgumentException( "illegal defaultShowCountList '"+ countList +"': sort error.[1]" );
						}
					}
				} catch( NumberFormatException numEx ) {
					throw new IllegalArgumentException( "illegal defaultShowCountList '"+ countList +"': "+ numEx.getMessage() );
				}
			} else
				countlists = DEFAULT_SHOWCOUNT_LIST;
		}

		JspWriter out = pageContext.getOut();
		try {
			String name = "_countSelect";
			out.print( "<select name='" + name + "'" );
			if( style != null ) out.print( " style='"+ style +"'" );
			if( id != null ) {
				out.print( " id='"+ id +"'" );
			} else {
				id = name;
				out.print( " id='" + id + "'" );
			}
			if( styleClass != null ) out.print( " class='"+ styleClass +"'" );
			if( modified != null ) out.print( " onChange='"+ modified +"'" );
			out.print( ">" );

			boolean selected = false;
			for( int i = 0; i < countlists.length; i++ ) {
				String description;
				if( countlists[i] < 0 ) {
					description = msghandler.getMessage( "MSG_ALL" );
				} else {
					String count = String.valueOf( countlists[i] );
					try {
						msghandler.getMessageValue( "jsp.SHOWCOUNT_SELECT_1" );
						description = msghandler.getMessage( "jsp.SHOWCOUNT_SELECT", count );
					} catch( java.util.MissingResourceException ex ) {
						description = count;
					}
				}

				if( countlists[i] == maxRows ) {
					selected = true;
					out.print( "<option value='"+ countlists[i] +"' selected>"+ description +"</option>" );
				} else {
					if( !selected && maxRows > 0 && countlists[i] > maxRows ) {
						selected = true;
						out.print( "<option value='"+ maxRows +"' selected>"+ maxRows +"</option>" );
					}
					out.print( "<option value='"+ countlists[i] +"'>"+ description +"</option>" );
				}
			}
			if( !selected ) {
				if( maxRows < 0 )
					out.print( "<option value='"+ maxRows +"' selected>"+ msghandler.getMessage("MSG_ALL") +"</option>" );
				else
					out.print( "<option value='"+ maxRows +"' selected>"+ maxRows +"</option>" );
			}
			out.print( "</select>" );

			if( customSelect ) {
				String customOptions = "selectmenuOptions";
				if( this.customOption != null ) {
					customOptions = this.customOption;
				}
				out.print( "<script>" );
				out.print( "	$( function() {" );
				out.print( "		var options = $.extend( {}, " + customOptions + ", { searchable: false } );" );
				out.print( "		$('#"+ id +"').singleSelectmenu( options );" );
				if( this.modified != null ) {
					out.print( "		$('#"+ id +"').on( 'singleselectmenuselect', function(event, ui) {" );
					out.print( "			" + modified + ";" );
					out.print( "		});" );
				}
				out.print( "		$('#"+ id +"').singleSelectmenu( \"option\", \"width\", \"auto\" );" );
				if( this.styleClass != null ) {
					out.print( "		$('#"+ id +"').singleSelectmenu( \"option\", \"classes.ui-selectmenu-button\", \"selectmenu " + this.styleClass + "\" );" );
				}
				out.print( "	});" );
				out.println( "</script>" );
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}

		return EVAL_PAGE;
	}

	public void setCountList( String countList ) {
		String[] counts = countList.split( "," );
		int[] countlists = new int[counts.length];

		for( int i = 0; i < counts.length; i++ ) {
			try {
				countlists[i] = Integer.parseInt( counts[i] );
				if( countlists[i] <= 0 ) countlists[i] = -1;
				if( i > 0 ) {
					if( countlists[i] == countlists[i-1] )
						throw new IllegalArgumentException( "duplicate count '"+ counts[i] +"'." );
					else if( countlists[i] > 0 && (countlists[i] < countlists[i-1] || countlists[i-1] < 0) )
						throw new IllegalArgumentException( "unsorted '"+ counts[i] +"'." );
				}
			} catch( NumberFormatException numEx ) {
				throw new IllegalArgumentException( "illegal number format '"+ counts[i] +"'." );
			}
		}

		this.countlists = countlists;
	}

	public void setCustomOption( String customOption ) {
		this.customOption = customOption;
	}

	public void setCustomSelect( boolean customSelect ) {
		this.customSelect = customSelect;
	}

	public void setModified( String modified ) {
		this.modified = modified;
	}

	public void setStyle( String style ) {
		this.style = style;
	}

	public void setStyleClass( String styleClass ) {
		this.styleClass = styleClass;
	}

	@Override
	public void setId( String id ) {
		this.id = id;
	}
}
