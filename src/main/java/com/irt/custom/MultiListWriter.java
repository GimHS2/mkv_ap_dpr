/*
 *	File Name:	MultiListWriter.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		한 라인에 여러개의 레코드(billing, delivery, memos)를 찍기위한 ListWriter
 *		D-Portal 2.0 만 사용한다.
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2	신규 UI/UX 적용
 *	hankalam	2021/04/30		2.2.1	printDataLine(): freegoodsOrderList 정보가 체크박스 values 에 포함되도록 수정
 *	guksm		2008/09/26		2.2.0	create
 *
**/

package com.irt.custom;

import com.irt.data.cols.*;
import com.irt.html.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;


/**
 *
 */
public class MultiListWriter extends ListWriter {
	String[] appendNames = null;
	ColumnList[] appendColumnLists = null;

	public MultiListWriter( HttpServletRequest request, HtmlPage htmlpage ) throws IllegalArgumentException {
		super( request, htmlpage );
	}

	public MultiListWriter( HttpServletRequest request, HtmlPage htmlpage, ColumnList columnList ) throws IllegalArgumentException {
		super( request, htmlpage, columnList );
	}

	public MultiListWriter( HttpServletRequest request, HtmlPage htmlpage, ColumnList columnList, ColumnList summaryColumnList )
						throws IllegalArgumentException {
		super( request, htmlpage, columnList, summaryColumnList );
	}

	public MultiListWriter( HttpServletRequest request, HtmlPage htmlpage, ColumnList columnList, ColumnList[] summaryColumnLists )
						throws IllegalArgumentException {
		super( request, htmlpage, columnList, summaryColumnLists );
	}

	@Override
	public void print( JspWriter out ) throws IOException {
		printHead( out );
		if( records == null || records.size() == 0 )
			printMessageLine( out, msghandler.getMessage("MSG_NO_RECORD_FOUND"), false );
		else if( appendNames == null || appendColumnLists == null || appendNames.length != appendColumnLists.length )
			printMessageLine( out, msghandler.getMessage("MSG_INVALID_VARIABLES"), false );
		else {
			int row = 0;
			for( java.util.Iterator iterator = records.iterator(); iterator.hasNext(); row++ ) {
				Map recordMap = (Map)iterator.next();

				if( maximumRowCount > 0 && maximumRowCount == row ) {
					if( maximumMessageKey != null ) {
						printMessageLine( out, msghandler.getMessage(maximumMessageKey, String.valueOf(maximumRowCount)), true );
					}
					break;
				}

				List arrList = new java.util.ArrayList();
				for( int i = 0; i < appendNames.length; i++ )
					arrList.add( recordMap.get(appendNames[i]) );

				int rowSpanCount = 0;
				for( int i = 0; i < arrList.size(); i++ ) {
					List<Map<String, Object>> appendRecord = (List<Map<String, Object>>)arrList.get( i );
					if( appendRecord != null && appendRecord.size() > 0 )
						rowSpanCount += appendRecord.size();
				}

				printDataLine( out, recordMap, row, ++rowSpanCount );

				for( int i = 0; i < arrList.size(); i++ ) {
					List<Map<String, Object>> appendRecord = (List<Map<String, Object>>)arrList.get( i );

					if( appendRecord != null && appendRecord.size() > 0 ) {
						Column[] appendColumns = appendColumnLists[i].getColumns();
						if( appendColumns != null && appendColumns.length > 0 ) {
							for( Object obj : appendRecord ) {
								Map map = (Map)obj;

								out.print( "<tr" );
								printDataLineAttribute( out, map, row );
								out.println( ">" );

								for( int c = 0; c < appendColumns.length; c++ )
									printDataCell( out, appendColumns[c], map, row, c );

								out.println( "</tr>" );
							}
						}
					}
				}
				out.println( "</tbody>" );
			}
		}
		printTail( out );
	}

	public void printDataLine( JspWriter out, Map recordMap, int row, int appendRowCount ) throws IOException {
		out.println( "<tbody class='multi-line'>" );
		out.print( "<tr" );
		printDataLineAttribute( out, recordMap, row );
		out.println( ">" );

		if( checkboxType == CHECKBOXTYPE_CHECK || checkboxType == CHECKBOXTYPE_RADIO ) {
			if( appendRowCount > 0 )
				out.print( "<td class='line-check' rowspan='"+ String.valueOf(appendRowCount) +"'>" );
			else
				out.print( "<td class='line-check'>" );

			out.print( "<div class='cell-checkbox'>" );

			if( checkboxType == CHECKBOXTYPE_CHECK )
				out.print( "<input type='checkbox' name='"+ checkboxName +"'" );
			else
				out.print( "<input type='radio' name='"+ checkboxName +"'" );
			out.print( " id='"+ checkboxName +"_"+ row +"'" );

			String checkboxValue = getCheckBoxValue( recordMap );
			List<java.util.Map<String, Object>> freegoodsOrderList = (List<Map<String, Object>>) recordMap.get( "freegoodsOrderList" );
			if( freegoodsOrderList != null && freegoodsOrderList.size() > 0 ) {
				java.util.Map<String, Object> freegoodsMap = freegoodsOrderList.get(0);
				String orderKey = (String) freegoodsMap.get( "orderKey" );
				String orderNumber = (String) freegoodsMap.get( "orderNumber" );
				checkboxValue += ";" + orderKey + ";" + orderNumber;
			}
			if( checkboxValue != null ) out.print( " value='"+ checkboxValue +"'" );
			if( !enableCheckbox(recordMap, row) ) out.print( " disabled='disabled'" );
			out.print( "/>" );
			out.print( "<label for='" + checkboxName + "_" + row + "'></label>" );

			if( hiddenKeys != null ) {
				for( int i = 0; i < hiddenKeys.length; i++ ) {
					out.print( "<input type='hidden' name='"+ checkboxName +"_"+ hiddenKeys[i] +"'" );
					out.print( " value='"+ HtmlUtility.toHtmlString(recordMap.get(hiddenKeys[i])) +"'/>" );
				}
			}
			out.println( "</div></td>" );
		}

		if( numbering ) {
			if( appendRowCount > 0 )
				out.print( "<td rowspan='"+ String.valueOf(appendRowCount) +"'>" );
			else
				out.print( "<td>" );

			out.print( "<div>" + (startingNumber + row + 1) +"</div></td>" );
		}

		for( int c = 0; c < columns.length; c++ )
			printDataCell( out, columns[c], recordMap, row, c );

		out.println( "</tr>" );
	}

	public void setAppendNames( String[] names ) {
		this.appendNames = names;
	}

	public void setAppendColumnLists( ColumnList[] columnLists ) {
		this.appendColumnLists = columnLists;
	}
}
