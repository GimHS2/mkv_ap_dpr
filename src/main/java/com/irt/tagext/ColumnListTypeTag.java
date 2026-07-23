/*
 *	File Name:	ColumnListTypeTag.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2011/08/31		2.2.1	readonly 추가
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.tagext;

import com.irt.data.cols.ColumnList;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import java.util.List;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * 데이터건수를 출력.
 *
 * <ul type='square'>
 * <li>(O)columnListId
 * <li>(O)name
 * <li>(O)onChange
 * <li>(O)readonly
 * </ul>
 */
public class ColumnListTypeTag extends javax.servlet.jsp.tagext.TagSupport {
	String columnListId = "columnList";
	String name = com.irt.servlet.ServletModel.PARAM_COLUMNLISTTYPE;
	String onchange = null;
	boolean readonly;

	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			ColumnList columnList = (ColumnList)pageContext.findAttribute( columnListId );

			if( columnList != null ) {
				String columnListTypeKey = columnList.getColumnListType();

				List<String[]> columnListTypeList = columnList.getColumnListTypeList();
				if( columnListTypeList != null ) {
					out.print( "<select name='"+ name +"' onChange='"+ onchange +"'" );
					if( readonly ) {
						HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
						out.println( " class='"+ htmlpage.getProperty().getProperty("jsp.styleClass.input.readonly") + "'" );
					}
					out.println( ">" );

					for( String[] columnListTypes: columnListTypeList ) {
						String key = columnListTypes[0];

						if( !readonly || key.equals(columnListTypeKey) ) {
							out.print( "<option value='"+ key +"'"+ ( key.equals(columnListTypeKey) ? " selected" : "") +">" );
							out.print( HtmlUtility.toHtmlString(columnListTypes[1]) );
							out.println( "</option>" );
						}
					}
					out.println( "</select>" );
				}
			}
		} catch( java.io.IOException ioEx ) {
			throw new JspException( ioEx );
		}
		return EVAL_PAGE;
	}

	public void setColumnListId( String columnListId ) {
		this.columnListId = columnListId;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public void setOnChange( String onchange ) {
		this.onchange = onchange;
	}

	public void setReadonly( boolean readonly ) {
		this.readonly = readonly;
	}
}
