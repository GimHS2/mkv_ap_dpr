/*
 *	File Name:	SelectListWriter.java
 *	Version:	2.1.2
 *
 *	Description:
 *
 *	Note:
 *		script(using)
 *			selectOne()
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/05/31		2.1.2	printDataLineAttribute(): super.printDataLineAttribute() 사용
 *	stghr12		2007/04/30		2.1.1	SelectListWriter(req, htmlpage) 생성자: columnList 읽는 부분 수정
 *	stghr12		2006/12/01		2.1.0	version up
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.custom;

import com.irt.data.cols.ColumnList;
import com.irt.html.HtmlPage;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

/**
 *
 */
public class SelectListWriter extends ListWriter {
	public SelectListWriter( HttpServletRequest request, HtmlPage htmlpage ) throws IllegalArgumentException {
		this( request, htmlpage, (ColumnList)request.getAttribute("columnList") );
	}

	public SelectListWriter( HttpServletRequest request, HtmlPage htmlpage, ColumnList columnList ) throws IllegalArgumentException {
		super( request, htmlpage, columnList );

		this.checkboxType = ( "chk".equals(request.getParameter("attr")) ? CHECKBOXTYPE_CHECK : CHECKBOXTYPE_RADIO );
		this.numbering = false;
		this.useLabelForCheckbox = false;
	}

	public void printDataLineAttribute( JspWriter out, Map recordMap, int row ) throws IOException {
		super.printDataLineAttribute( out, recordMap, row );
		out.print( " style='cursor: hand;' onClick='JavaScript:selectOne("+ row +");'" );
	}
}
