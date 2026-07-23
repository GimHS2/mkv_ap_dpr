/*
 *	File Name:	ColumnSelectTag.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2012/12/31		2.2.1	CrossBrowsing 적용: '0;' -> '0px;' 변경
 *	stghr12		2010/03/31		2.2.0	create
 *
**/

package com.irt.tagext;

import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.util.MessageHandler;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * <ul type='square'>
 * <li>(O)columnListId
 * <li>(M)name
 * <li>(O)newLineColumnKey
 * <li>(O)tableColumnCount
 * <li>(M)totalColumnListId
 * <li>(M)type
 * </ul>
 */
public class ColumnSelectTag extends javax.servlet.jsp.tagext.TagSupport {
	String NEWLINE_COLUMN_KEY = "_newLine";
	String name = null, type = null;
	String columnListId = "columnList";
	String totalColumnListId = "totalColumnList";
	int tableColumnCount = 0;

	public int doEndTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			HtmlPage htmlpage = (HtmlPage)pageContext.findAttribute( "htmlpage" );
			MessageHandler msghandler = htmlpage.getMessageHandler();

			ColumnList columnList = (ColumnList)pageContext.findAttribute( columnListId );
			ColumnList totalColumnList = (ColumnList)pageContext.findAttribute( totalColumnListId );

			if( "table".equals(type) ) {
				int TABLE_COLUMN_CNT = tableColumnCount;
				if( TABLE_COLUMN_CNT == 0 ) {
					int count = 0;
					for( int i = 0; i < totalColumnList.getColumnCount(); i++, count++ ) {
						Column column = totalColumnList.getColumn(i);
						if( NEWLINE_COLUMN_KEY.equals(column.getKey()) ) {
							if( count > TABLE_COLUMN_CNT ) TABLE_COLUMN_CNT = count;
							count = 0;
						}
					}
					if( count > TABLE_COLUMN_CNT ) TABLE_COLUMN_CNT = count;
				}

				int count = 0;
				out.println( "<table cellspacing='0' cellpadding='0' border='0'>" );
				out.println( "<tr>" );
				for( int idx = 0; idx < totalColumnList.getColumnCount(); idx++ ) {
					boolean selected = false;
					Column column = totalColumnList.getColumn(idx);

					if( columnList != null ) {
						for( int c = 0; c < columnList.getColumnCount(); c++ )
							if( column.getKey().equals(columnList.getColumn(c).getKey()) ) {
								selected = true;
								break;
							}
					}

					boolean isNewLine = false;
					if( tableColumnCount > 0 ) {
						if( NEWLINE_COLUMN_KEY.equals(column.getKey()) ) continue;
						isNewLine = ( count%TABLE_COLUMN_CNT == 0 );
					} else if( NEWLINE_COLUMN_KEY.equals(column.getKey()) ) {
						isNewLine = true;
						if( count%TABLE_COLUMN_CNT > 0 ) {
							for( int i = TABLE_COLUMN_CNT - count%TABLE_COLUMN_CNT; i > 0; i-- )
								out.print( "<td/>" );
							out.println();
						}
					}
					if( isNewLine && count > 0 ) {
						out.println( "</tr>" );
						out.println( "<tr>" );
					}

					if( !NEWLINE_COLUMN_KEY.equals(column.getKey()) ) {
						out.print( "<td style='margin: 0px; padding: 0px;'>" );
						out.print( "<input type='checkbox' name='"+ name +"' value='"+ column.getKey() +"'" );
						if( selected ) out.print( " checked" );
						out.print( " id='"+ name +"_"+ column.getKey() +"'><label for='"+ name +"_"+ column.getKey() +"'>" );
						out.print( HtmlUtility.toHtmlString(column.getColumnTitle(null, msghandler)) );
						out.print( "</label>" );
						out.println( "</td>" );

						count++;
					}
				}
				if( count%TABLE_COLUMN_CNT > 0 ) {
					for( int i = TABLE_COLUMN_CNT - count%TABLE_COLUMN_CNT; i > 0; i-- )
						out.print( "<td/>" );
					out.println();
				}
				if( count > 0 ) out.println( "</tr>" );
				out.println( "</table>" );
			} else {
				out.println( "<select name='"+ name +"'>" );
				for( int i = 0; i < totalColumnList.getColumnCount(); i++ ) {
					boolean selected = false;
					Column column = totalColumnList.getColumn(i);

					if( NEWLINE_COLUMN_KEY.equals(column.getKey()) ) continue;
					if( columnList != null ) {
						for( int c = 0; c < columnList.getColumnCount(); c++ )
							if( column.getKey().equals(columnList.getColumn(c).getKey()) ) {
								selected = true;
								break;
							}
					}

					out.print( "<option value='"+ column.getKey() +"'"+ (selected ? " selected" : "") +">" );
					out.print( HtmlUtility.toHtmlString(column.getColumnTitle(null, msghandler)) );
					out.println( "</option>" );
				}
				out.println( "</select>" );
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

	public void setNewLineColumnKey( String newLineColumnKey ) {
		this.NEWLINE_COLUMN_KEY = newLineColumnKey;
	}

	public void setTableColumnCount( int tableColumnCount ) {
		this.tableColumnCount = tableColumnCount;
	}

	public void setTotalColumnListId( String totalColumnListId ) {
		this.totalColumnListId = totalColumnListId;
	}

	public void setType( String type ) {
		this.type = type;
	}
}
