/*
 *	File Name:	ListWriter.java
 *	Version:	2.2.9c
 *
 *	Description:
 *
 *	Note:
 *		msghandler.getMessage()
 *			MSG_NO_RECORD_FOUND
 *			MSG_SORT
 *		image
 *			ico_check.gif
 *			ico_sort_asc.gif
 *			ico_sort_desc.gif
 *			ico_sort_none.gif
 *		script(using)
 *			CheckBox.checkAll()
 *			fitHeightToWindow()
 *			listClick()
 *			listMouseOut()
 *			listMouseOver()
 *			listSort()
 *		style
 *			col.check
 *			col.number
 *			div.list_content_data_scroll
 *			table.list_content_header
 *			table.list_content_data
 *			table.list_content_summary
 *			table.cell
 *			tr.list_content
 *			tr.list_sum
 *			tr.header_one
 *			th.header_sort
 *			th.header_top
 *			td.blankcell
 *			td.cell
 *			td.list_linemsg
 *			td.list_linemsg_err
 *			td.list_msg
 *			td.separate
 *			td.underline
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.9c	신규 UI/UX 적용
 *	hankalam	2019/08/30		2.2.8c	x scroll bar 숨김.
 *	jbaek		2018/10/30		2.2.8c	PageListIdentifiable, FlexDataLine 적용
 *	jbaek		2014/09/30		2.2.8c	rbm2적용. 기존 constructor 남겨둠.
 *	GimHS		2014/08/29		2.2.8	getDataCellAttr() 추가 (DataCell의 attribute를 가져오는 부분을 함수로 분리)
 *	GimHS		2014/03/31		2.2.7	printDataCell(): dataCellAttr 옵션 처리 추가
 *	GimHS		2012/12/31		2.2.6	CrossBrowsing 적용
 *										  -> 리스트 화면에서 Table의 Header와 Summary에 항상 Y scroll 추가
 *										  -> div 태그에서 onResize event를 지원하지 않아 onDblClick event로 대체
 *										  -> col 태그에서 width 외의 스타일을 지원하지 않아 td 태그에 스타일 추가
 *										  -> JavaScript에서 event object를 사용하는 경우 바로 사용할수 없어서
 *										     a 태그에서 onClick event의 JavaScript:setEventObject(event) 함수를 이용하여 event를 저장하도록 변경
 *										  -> tr 태그 안에 값이 없더라도 td 태그까지 정의(td 태그 없으면 style이 적용 안됨)
 *										  -> setScrollHeight(): heightResizable의 default값을 true로 변경
 *										  -> getColumnValue(), getSummaryValue(): 값이 없을때 "&nbsp;"를 return 하도록 변경
 *	stghr12		2011/02/28		2.2.5	setScrollHeightStyle() 추가
 *										list_content_data_scroll의 style 처리방법 변경
 *	stghr12		2010/07/31		2.2.4	setImageBasePath(), setPrintingUnderLine() 추가
 *	stghr12		2010/02/28		2.2.3	errors 출력 오류 수정
 *										columnList.getProperty("lineAttribute")를 사용로직 수정
 *	stghr12		2008/05/31		2.2.2	printDataLineAttribute(): columnList.getProperty("lineAttribute")를 사용
 *	stghr12		2008/03/31		2.2.1	printHeaderCell(): getColumnTitle() -> getColumnTitle(recordMap, msghandler)
 *										printHeaderLine(): getGroupTitle() -> getGroupTitle(recordMap, msghandler)
 *										setMaximumRows() 삭제
 *										setScrollHeight(scrollHeight, resizable) 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										style 변경: td.cell, td.blankcell, table.cell 사용
 *										setScrollHeight() 추가
 *	stghr12		2007/10/31		2.1.2	getStartingNumber(), setPrintingErrors(), setPrintingHeader(), setStartingNumber() 추가
 *	stghr12		2007/04/30		2.1.1	com.irt.data.cols.Column 변경내용 적용, style 변경
 *										printHead(), printTail(): printErrors() 위치 변경
 *	stghr12		2006/12/01		2.1.0	com.irt.html.ListWriter implements
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.custom;

import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnGroup;
import com.irt.data.cols.ColumnList;
import com.irt.data.cols.ConstantColumn;
import com.irt.data.format.*;
import com.irt.html.*;
import com.irt.resbdl.PageListIdentifiable;
import com.irt.resbdl.PageMessageKeys;
import com.irt.system.SessionManager;
import com.irt.util.MessageHandler;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

/**
 *
 */
public class ListWriter implements com.irt.html.ListWriter, PageListIdentifiable, FlexDataLine {//@formatter:off
	public HttpServletRequest request;
	public HtmlPage htmlpage;
	public MessageHandler msghandler;
	public SessionManager sessionMng;

	protected String contentId;
	protected String prefixContentId = "content";

	// data
	Collection<? extends Map> records;
	Collection<? extends Map> errors;
	Map summaryMap;

	// variable
	int startingNumber = 0;
	String sortKey, sortWay;

	// columnList 정보
	ColumnList columnList;
	Column[] columns;
	String[] primaryKeys, hiddenKeys;
	ColumnList[] summaryColumnLists;
	int[] flexDataMaxWidths;

	// options
	char checkboxType = CHECKBOXTYPE_NONE;
	String checkboxName = "listcheckbox";
	String imageBasePath = "images";
	boolean useFlexDataLine = false;
	boolean useLabelForCheckbox = true;
	boolean numbering = false;
	boolean disableSort = false;
	boolean useVerticalScroll = true;
	boolean printingErrors = true;
	boolean printingHeader = true;
	boolean printingUnderLine = false;
	String heightStyle = "220";
	String scrollStyle = null;
	boolean heightResizable = true;
	RecordFormat errorPattern = PatternRecordFormat.getInstance( "$H{name} -&gt ${message}" );


	// 최대표시건수
	int maximumRowCount = -1;
	String maximumMessageKey = null;

	public ListWriter( HttpServletRequest request, HtmlPage htmlpage ) {
		this( request, htmlpage, (ColumnList)request.getAttribute("columnList") );
	}

	public ListWriter( HttpServletRequest request, HtmlPage htmlpage, String listKey ) throws IllegalArgumentException {
		this( request, htmlpage, (ColumnList)request.getAttribute("columnList"), listKey );
	}

	public ListWriter( HttpServletRequest request, HtmlPage htmlpage, ColumnList columnList ) throws IllegalArgumentException {
		this( request, htmlpage, columnList, (String)null );
	}

	public ListWriter( HttpServletRequest request, HtmlPage htmlpage, ColumnList columnList, String listKey ) throws IllegalArgumentException {
			if( columnList == null )
				throw new IllegalArgumentException( "columnList cannot be null" );

		this.request = request;
		this.htmlpage = htmlpage;
		this.msghandler = htmlpage.getMessageHandler();
		this.sessionMng = (SessionManager)request.getAttribute( "sessionMng" );

		this.startingNumber = htmlpage.getListIndexVariables()[0];
		this.sortKey = request.getParameter( "sort" );
		this.sortWay = null;
		if( this.sortKey != null ) {
			String[] sortKeys = sortKey.split( "#", 2 );
			if( sortKeys.length > 1 ) {
				this.sortKey = sortKeys[0];
				this.sortWay = sortKeys[1];
			}
		}

		this.columnList = columnList;
		this.columns = columnList.getColumns();
		this.primaryKeys = columnList.getPrimaryFieldKeys();
		this.hiddenKeys = columnList.getHiddenFieldKeys();
		if( this.sortKey == null ) {
			String[] sortKeys = columnList.getSortKeys();
			if( sortKeys != null ) {
				sortKeys = sortKeys[0].split( "#", 2 );
				this.sortKey = sortKeys[0];
				this.sortWay = ( sortKeys.length > 1 ? sortKeys[1] : null );
			}
		}

		if( htmlpage.hasManageAuth() ) {
			this.checkboxType = CHECKBOXTYPE_CHECK;
			this.numbering = false;
		} else {
			this.checkboxType = CHECKBOXTYPE_NONE;
			this.numbering = true;
		}

		if( listKey == null ) listKey = "records";
		setRecords( (Collection<? extends Map>)request.getAttribute(listKey) );
		setErrors( (Collection<? extends Map>)request.getAttribute("errors") );
		setSummaryMap( (Map)request.getAttribute("summary") );

		Object summaryColumnList = request.getAttribute( "summaryColumnList" );
		if( summaryColumnList instanceof ColumnList )
			setSummaryColumnList( (ColumnList)summaryColumnList );
		else if( summaryColumnList instanceof ColumnList[] )
			setSummaryColumnLists( (ColumnList[])summaryColumnList );

		this.flexDataMaxWidths = new int[columns.length];
	}

	public ListWriter( HttpServletRequest request, HtmlPage htmlpage, ColumnList columnList, ColumnList summaryColumnList ) {
		this( request, htmlpage, columnList );
		setSummaryColumnList( summaryColumnList );
	}

	public ListWriter( HttpServletRequest request, HtmlPage htmlpage, ColumnList columnList, ColumnList[] summaryColumnLists ) {
		this( request, htmlpage, columnList );
		setSummaryColumnLists( summaryColumnLists );
	}

	@Override
	public boolean containsData() {
		return ( records != null && records.size() > 0 );
	}

	@Override
	public boolean enableCheckbox( Map recordMap, int row ) {
		return true;
	}

	public String getCheckBoxValue( Map recordMap ) {
		if( primaryKeys != null ) {
			StringBuffer sbuf = new StringBuffer();
			for( int i = 0; i < primaryKeys.length; i++ )
				sbuf.append( ";" ).append( HtmlUtility.toHtmlString(recordMap.get(primaryKeys[i])) );

			return sbuf.substring(1);
		}

		return null;
	}

	@Override
	public int getColumnSize() {
		int columnSize = columnList.getColumnSize();
		if( checkboxType == CHECKBOXTYPE_CHECK || checkboxType == CHECKBOXTYPE_RADIO ) columnSize++;
		if( numbering ) columnSize++;

		return columnSize;
	}

	@Override
	public String getColumnValue( Column column, Map recordMap, int row, int col ) {
		String value = column.format( recordMap, msghandler );
		return ( value == null || value.length() == 0 ? "&nbsp;" : value );
	}

	public String getDataCellAttr( Column column, int row ) {
		String columnAttr = (String)column.getColumnAttr();
		if( columnAttr == null ) columnAttr = "";

		String dataCellAttr = column.getDataCellAttr();
		if( dataCellAttr != null ) columnAttr = columnAttr +" "+ dataCellAttr;

		return columnAttr;
	}

	@Override
	public int getStartingNumber() {
		return startingNumber;
	}

	@Override
	public String getSummaryValue( Column column, Map summaryMap, int row, int col ) {
		String value = column.format( summaryMap, msghandler );
		return ( value == null || value.length() == 0 ? "&nbsp;" : value );
	}

	@Override
	public void print( JspWriter out ) throws IOException {
		printHead( out );
		if( records == null || records.size() == 0 ) {
			if( !printingErrors || errors == null )
				printMessageLine( out, msghandler.getMessage("MSG_NO_RECORD_FOUND"), false );
		} else {
			int row = 0;
			for( java.util.Iterator iterator = records.iterator(); iterator.hasNext(); row++ ) {
				Map recordMap = (Map)iterator.next();

				if( maximumRowCount > 0 && maximumRowCount == row ) {
					if( maximumMessageKey != null )
						printMessageLine( out, msghandler.getMessage(maximumMessageKey, String.valueOf(maximumRowCount)), true );
					break;
				}
				printDataLine( out, recordMap, row );
			}
		}
		printTail( out );
	}

	@Override
	public void print( JspWriter out, int rowsize ) throws IOException {
		StringBuffer colgroupBuffer = new StringBuffer();

		colgroupBuffer.append( "<colgroup>" );
		colgroupBuffer.append( "<col width='"+ (100/rowsize) +"%'/>" );
		for( int i = 1; i < rowsize; i++ )
			colgroupBuffer.append( "<col/><col width='"+ (100/rowsize) +"%'/>" );
		colgroupBuffer.append( "</colgroup>" );

		if( useVerticalScroll && this.heightResizable && printingHeader
				&& ( (records != null && records.size() > 0) || (printingErrors && errors != null) ) )
			out.println( "<div class='list_content_header_scroll' style='height: expression(this.childNodes(0).offsetHeight+2); overflow-y: scroll;'>" );
		else if( records == null || records.size() == 0 ) {
			out.println( "<div id='" + prefixContentId + "_data' class='list_content_data_scroll'>" );
		}
		out.println( "<table class='list_content_header' cellspacing='0' cellpadding='0' rules='rows'>" );
		out.println( colgroupBuffer.toString() );

		if( printingHeader ) {
			out.println( "<tr>" );
			for( int r = 0; r < rowsize; ) {
				out.println( "<td class='cell'>" );
				out.println( "<table class='cell' cellspacing='0' cellpadding='0'>" );
				printTableColgroup( out );
				printHeaderLine( out );
				out.println( "</table>" );
				out.print( "</td>" );
				if( ++r < rowsize ) out.println( "<td class='separate'/>" );
			}
			out.println( "</tr>" );
		}
		if( printingErrors ) printErrors( out, (rowsize*2-1) );

		if( records == null || records.size() == 0 ) {
			out.println( "<tr><td colspan='"+ (rowsize*2-1) +"' class='list_msg'>"+ msghandler.getMessage("MSG_NO_RECORD_FOUND") +"</td></tr>" );
			out.println( "</table>" );
			if( useVerticalScroll && this.heightResizable && printingHeader ) out.println( "</div>" );
		} else {
			if( !printingHeader ) out.println( "<tr><td colspan='"+ (rowsize*2-1) +"'/></tr>" );
			out.println( "</table>" );

			if( useVerticalScroll ) {
				if( this.heightResizable ) {
					if( printingHeader )
						out.println( "</div>" );

					//out.println( "<script type='text/javascript'>window.onresize = fitHeightToWindow;</script>" );
					out.println( "<div class='list_content_data_scroll' onDblClick='JavaScript:fitHeightToWindow(this, "+ this.heightStyle +");'>" );
				} else {
					if( scrollStyle == null ) {
						String height = "expression(this.childNodes(0).offsetHeight < "+ this.heightStyle +" ? \"\" : "+ this.heightStyle +")";
						String overflow = "expression(this.childNodes(0).offsetHeight < "+ this.heightStyle +" ? \"hidden\" : \"scroll\")";
						out.println( "<div class='list_content_data_scroll' style='height: "+ height +"; overflow-y: "+ overflow +";'>" );
					} else
						out.println( "<div class='list_content_data_scroll' style='"+ scrollStyle +"'>" );
				}
			}

			out.println( "<table class='list_content_data' cellspacing='0' cellpadding='0' rules='rows'>" );
			out.println( colgroupBuffer.toString() );

			int row = 0;
			for( java.util.Iterator iterator = records.iterator(); iterator.hasNext(); row++ ) {
				Map recordMap = (Map)iterator.next();

				if( maximumRowCount > 0 && maximumRowCount == row ) {
					if( maximumMessageKey != null ) {
						if( row%rowsize > 0 ) {
							out.print( "<td colspan='"+ ((rowsize-row%rowsize)*2-1) +"' class='list_linemsg'>" );
							row += rowsize - row%rowsize;
						} else
							out.print( "<tr class='list_content'><td colspan='"+ (rowsize*2-1) +"' class='list_linemsg'>" );
						out.println( msghandler.getMessage(maximumMessageKey, String.valueOf(maximumRowCount)) +"</td></tr>" );
					}
					break;
				}
				if( row%rowsize == 0 ) out.print( "<tr>" );
				if( printingHeader ) {
					out.println( "<td class='cell'>" );
					out.println( "<table class='cell' cellspacing='0' cellpadding='0'>" );
				} else {
					out.println( "<td class='blankcell'>" );
					out.println( "<table width='100%' cellspacing='0' cellpadding='0'>" );
				}
				printTableColgroup( out, true );
				printDataLine( out, recordMap, row );
				out.println( "</table>" );
				out.print( "</td>" );

				if( row%rowsize == rowsize - 1 )
					out.println( "</tr>" );
				else
					out.println( "<td class='separate'/>" );
			}
			if( row%rowsize > 0 ) {
				out.println( "<td class='blankcell'>&nbsp;</td>" );
				while( (++row)%rowsize > 0 )
					out.print( "<td class='separate'/><td class='blankcell'>&nbsp;</td>" );
				out.println( "</tr>" );
			}
			out.println( "</table>" );
			if( useVerticalScroll ) out.println( "</div>" );
		}
	}

	@Override
	public void print( JspWriter out, String linemsg ) throws IOException {
		printHead( out );
		printMessageLine( out, linemsg, false );
		printTail( out );
	}

	@Override
	public void printDataCell( JspWriter out, Column column, Map recordMap, int row, int col ) throws IOException {
		HyperLink hyperLink = null;
		if( column instanceof LinkedColumn ) {
			hyperLink = ((LinkedColumn)column).getColumnLink();
			if( hyperLink == null || !hyperLink.isValidLink(recordMap, sessionMng) )
				hyperLink = null;
		}

		String columnValue = getColumnValue(column, recordMap, row, col);
		if( useFlexDataLine ) {
			final String HTML_TAG_PATTERN = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";
			java.util.regex.Pattern htmlValidator = java.util.regex.Pattern.compile(HTML_TAG_PATTERN);
			boolean hasHtml = htmlValidator.matcher(columnValue).find();
			if( hasHtml ) {
				setFlexDataMaxWidth(col, -1);
			} else {
				setFlexDataMaxWidth(col, (columnValue == null) ? 0 : (columnValue.equals("&nbsp") ? 0 : columnValue.length()));
			}
		}
		String columnAttr = getDataCellAttr( column, row );
		if( hyperLink != null ) {
			String title = hyperLink.getHelpString( recordMap, msghandler );

			out.print( "<td "+ columnAttr +">" );
			out.print( "<div>" );
			out.print( "<a href='JavaScript:void(0);' onClick='JavaScript:setEventObject(event);"
					+ hyperLink.getLinkString(recordMap, msghandler) +"'" );
			if( title != null ) {
				out.print( " title='"+ title +"'" );
			} else {
				title = column.format( recordMap, msghandler );
				if( title != null && title.length() > 0 && title.indexOf("<") < 0 ) {
					out.print( " title='"+ title +"'" );
				}
			}
			out.print( ">" );
			out.print( columnValue );
			out.println( "</a></div></td>" );
		} else {
			String title = column.getColumnHelp( recordMap, msghandler );

			if( title == null ) {
				if( columnValue.indexOf("<") < 0 )
					out.print( "<td title='"+ columnValue +"' "+ columnAttr +">" );
				else
					out.print( "<td "+ columnAttr +">" );
			} else {
				out.print( "<td title='"+ title +"' "+ columnAttr +">" );
			}
			out.print( "<div>" );
			if( col == 0 && useLabelForCheckbox ) {
				out.print( "<label for='"+ checkboxName +"_"+ row +"'>" );
				out.print( columnValue );
				out.print( "</label>" );
			} else
				out.print( columnValue );
			out.println( "</div></td>" );
		}
	}

	@Override
	public void printDataLine( JspWriter out, Map recordMap, int row ) throws IOException {
		out.print( "<tr" );
		printDataLineAttribute( out, recordMap, row );
		out.println( ">" );

		if( checkboxType == CHECKBOXTYPE_CHECK || checkboxType == CHECKBOXTYPE_RADIO ) {
			out.print( "<td class='line-check'><div class='cell-checkbox'>" );
			if( checkboxType == CHECKBOXTYPE_CHECK )
				out.print( "<input type='checkbox' name='"+ checkboxName +"'" );
			else
				out.print( "<input type='radio' name='"+ checkboxName +"'" );
			out.print( " id='"+ checkboxName +"_"+ row +"'" );

			String checkboxValue = getCheckBoxValue( recordMap );
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
		if( numbering ) out.print( "<td class='number'><div>"+ (startingNumber + row + 1) +"</div></td>" );
		for( int c = 0; c < columns.length; c++ )
			printDataCell( out, columns[c], recordMap, row, c );

		out.println( "</tr>" );
		if( printingUnderLine )
			out.println("<tr height='1'><td class='underline' colspan='"+ getColumnSize() +"' bgcolor='#DAD8D7'></td></tr>");
	}

	@Override
	public void printDataLineAttribute( JspWriter out, Map recordMap, int row ) throws IOException {
		out.print( " class='list_content'" );

		String lineAttribute = columnList.getProperty( "lineAttribute" );
		if( lineAttribute != null ) {
			String attribute = PatternRecordFormat.getInstance(lineAttribute).format( recordMap, msghandler );
			if( attribute != null ) out.print( " "+ attribute );
		}
	}

	@Override
	public void printErrors( JspWriter out ) throws IOException {
		printErrors( out, getColumnSize() );
	}

	public void printErrors( JspWriter out, int columnSize ) throws IOException {
		if( errors == null ) return;

		for( Map errorMap : errors ) {
			out.print( "<tr class='list_content'><td colspan='"+ columnSize +"' class='list_linemsg_err'>" );
			out.print( errorPattern.format(errorMap, msghandler) );
			out.println( "</td></tr>" );
		}
	}

	@Override
	public void printFlexDataLine( JspWriter out ) throws IOException {
		out.print("<div class='list_flex_data'>");

		if( checkboxType == CHECKBOXTYPE_CHECK || checkboxType == CHECKBOXTYPE_RADIO ) {
			out.print("<span name='checkboxing' class='check'></span>");
		}
		if( numbering ) {
			out.print("<span name='numbering' class='number'></span>");
		}

		int dataMaxWidthLine = 0;
		int columnTitleWidthLine = 0;
		for( int c = 0; c < columns.length; c++ ) {
			int columnTitleWidth = columns[c].getColumnTitle(null, msghandler).length();
			out.print("<span name='" + columns[c].getFieldKey()
					+ "' data-column-fieldkey='" + columns[c].getFieldKey()
					+ "' data-column-title-width='" + columnTitleWidth
					+ "' data-column-data-max-width='" + getFlexDataMaxWidth()[c]
					+ "'></span>");
			dataMaxWidthLine += getFlexDataMaxWidth()[c];
			columnTitleWidthLine += columnTitleWidth;
		}
		out.println("</div>");
		out.print("<div class='list_flex_data_all"
				+ "' data-column-title-line-width='" + columnTitleWidthLine
				+ "' data-column-data-line-max-width='" + dataMaxWidthLine
				+ "'>");
		out.println("</div>");
	}

	@Override
	public void printHead( JspWriter out ) throws IOException {
		printPageIdentity(out);
		if( useVerticalScroll && this.heightResizable && printingHeader
				&& ( (records != null && records.size() > 0) || (printingErrors && errors != null) ) ) {
			out.println( "<div id='" + prefixContentId + "_header_wrapper' class='list_content_header_wrapper'>" );
			out.println( "<div class='scroll-blank'></div>" );
			out.println( "<div id='" + prefixContentId + "_header' class='list_content_header_scroll' style='height: expression(this.childNodes(0).offsetHeight+2); overflow: hidden'>" );
		} else if( records == null || records.size() == 0 ) {
			out.println( "<div id='" + prefixContentId + "_data' class='list_content_data_scroll'>" );
		}

		out.println( "<table class='list_content_header' cellspacing='0' cellpadding='0' rules='rows'>" );
		printTableColgroup( out );
		if( printingHeader ) printHeaderLine( out );
		if( useVerticalScroll && this.heightResizable && ( (records != null && records.size() > 0) || (printingErrors && errors != null) ) ) {
			if( !printingHeader ) out.println( "<tr><td colspan='"+ getColumnSize() +"'/></tr>" );
			out.println( "</table>" );
			if( printingHeader )
				out.println( "</div></div>" );

			if( this.heightResizable ) {
				out.println( "<script type='text/javascript'>" );
				out.println( "	$(window).resize( function () {" );
				out.println( "		resizeDescColumn();" );
				out.println( "	});" );
				out.println();
				out.println( "	function resizeDescColumn() {" );
				out.println( "		var minWidth = $(\".list_content_header th.description\").css( \"min-width\" );" );
				out.println( "		if( minWidth ) {" );
				out.println( "			if( minWidth == \"0px\" || minWidth == \"none\" ) {" );
				out.println( "				minWidth = 120;" );
				out.println( "			} else {" );
				out.println( "				minWidth = minWidth.replace( \"px\", \"\" );" );
				out.println( "			}" );
				out.println( "			if( $(\".list_content_header th.description\").width() <= minWidth ) {" );
				out.println( "				$(\".list_content_header th.description, .list_content_data td.description\").width( minWidth );" );
				out.println( "			} else {" );
				out.println( "				$(\".list_content_header th.description, .list_content_data td.description\").css( \"width\", \"\" );" );
				out.println( "			}" );
				out.println( "		}" );
				out.println( "	}" );
				out.println();
				out.println( "	$.fn.hasVerticalScroll = function() {" );
				out.println( "		return this.length ? this.get(0).scrollHeight > this.outerHeight() : false;" );
				out.println( "	};" );
				out.println();
				out.println( "	$(document).ready( function() {" );
				out.println( "		resizeDescColumn();" );
				out.println( "		if( $(\"#" + prefixContentId + "_data\").hasVerticalScroll() ) {" );
				out.println( "			var scrollWidth = getScrollBarWidth();" );
				out.println( "			$(\"#" + prefixContentId + "_header\").css( \"width\", \"calc(100% - \" + scrollWidth + \"px)\" );" );
				out.println( "			$(\"#" + prefixContentId + "_header_wrapper > div.scroll-blank\").show();" );
				out.println( "		}" );
				out.println( "		$(\"#" + prefixContentId + "_data\").on( 'scroll', function() {" );
				out.println( "			$(\"#" + prefixContentId + "_header\").scrollLeft( $(\"#" + prefixContentId + "_data\").scrollLeft() );" );
				out.println( "		});" );
				out.println( "	});" );
				out.println( "</script>" );
				out.println( "<div id='" + prefixContentId + "_data' class='list_content_data_scroll'>" );
			} else {
				if( scrollStyle == null ) {
					String height = "expression(this.childNodes(0).offsetHeight < "+ this.heightStyle +" ? \"\" : "+ this.heightStyle +")";
					String overflow = "expression(this.childNodes(0).offsetHeight < "+ this.heightStyle +" ? \"hidden\" : \"scroll\")";
					out.println( "<div id='" + prefixContentId + "_data' class='list_content_data_scroll' style='height: "+ height +"; overflow-y: "+ overflow +";'>" );
				} else
					out.println( "<div id='" + prefixContentId + "_data' class='list_content_data_scroll' style='"+ scrollStyle +"'>" );
			}
			out.println( "<table class='list_content_data' cellspacing='0' cellpadding='0' rules='rows'>" );
			printTableColgroup( out, true );
		}
		if( printingErrors ) printErrors( out );
	}

	@Override
	public void printHeaderCell( JspWriter out, Column column, int rowspan ) throws IOException {
		printHeaderCell( out, column, rowspan, false, false );
	}

	public void printHeaderCell( JspWriter out, Column column, int rowspan, boolean isLeftGroupLine, boolean isRightGroupLine ) throws IOException {
		StringBuffer attributeBuffer = new StringBuffer();
		String columnAttr = (String)column.getColumnAttr();
		String fieldKey = column.getFieldKey();
		String styleClass = "";
		if( columnAttr == null ) columnAttr = "";
		if( isLeftGroupLine ) {
			styleClass += " header-leftline";
		}
		if( isRightGroupLine ) {
			styleClass += " header-rightline";
		}
		if( rowspan > 1 )
			attributeBuffer.append( " rowspan='"+ rowspan +"'" );

		if( column instanceof LinkedColumn ) {
			HyperLink hyperLink = ((LinkedColumn)column).getHeaderLink();
			if( hyperLink != null && hyperLink.isValidLink(null, sessionMng) ) {
				String title = hyperLink.getHelpString( htmlpage.getProperty(), msghandler );

				attributeBuffer.append( " "+ columnAttr );
				out.print( "<th"+ attributeBuffer.toString() +"><div>" );
				out.print( "<a href='JavaScript:void(0);' onClick='JavaScript:setEventObject(event);"
						+ hyperLink.getLinkString(htmlpage.getProperty(), msghandler) +"'" );
				if( title != null ) out.print( " title='"+ title +"'" );
				out.print( ">" );
				out.print( column.getColumnTitle(null, msghandler) );
				out.print( "</a></div>" );
				out.print( "</th>" );
				return;
			}
		}

		if( column.sortable() && !disableSort ) {
			String sortScript = "JavaScript:listSort(\""+ fieldKey +"\");";
			String imageTag = null;

			if( fieldKey.equals(sortKey) ) {
				if( "DESC".equalsIgnoreCase(sortWay) )
					imageTag = "<img src='"+ imageBasePath +"/ico_sort_desc.png'/>";
				else {
					sortScript = "JavaScript:listSort(\""+ fieldKey +"%23DESC\");";
					imageTag = "<img src='"+ imageBasePath +"/ico_sort_asc.png'/>";
				}
				styleClass += " sort-on";
			}
			if( columnAttr.indexOf("class=") < 0 )
				attributeBuffer.append( " class='header-sort" + styleClass + "' "+ columnAttr +" title='"+ msghandler.getMessage("MSG_SORT")
						+"' onClick='"+ sortScript +"'" );
			else
				attributeBuffer.append( " class='header-sort" + styleClass + " " + columnAttr.substring(columnAttr.indexOf("class=") + 7)
						+" title='"+ msghandler.getMessage("MSG_SORT") +"' onClick='"+ sortScript +"'" );

			out.print( "<th"+ attributeBuffer.toString() +"><span>" );
			if( imageTag != null ) {
				out.print( "<div class='field-title'>" );
			}
			out.print( column.getColumnTitle(null, msghandler) );
			if( imageTag != null ) {
				out.print( "</div>" );
				out.print( "<div class='icon'>" + imageTag );
				out.print( "</div>" );
			}
			out.print( "</span></th>" );
		} else {
			if( columnAttr.indexOf("class=") < 0 ) {
				attributeBuffer.append( " class='" + styleClass + "' " + columnAttr );
			} else {
				attributeBuffer.append( " class='" + styleClass + " " + columnAttr.substring(columnAttr.indexOf("class=") + 7) );
			}
			out.print( "<th"+ attributeBuffer.toString() +"><span>" );
			out.print( column.getColumnTitle(null, msghandler) );
			out.print( "</span></th>" );
		}
	}

	@Override
	public void printHeaderLine( JspWriter out ) throws IOException {
		boolean hasColumnGroup = false;
		for( int c = 0; c < columns.length; c++ )
			if( columns[c].getColumnGroup() != null )
				hasColumnGroup = true;

		String checkScript = "JavaScript:CheckBox.checkAll("+ htmlpage.getFormName() +"."+ checkboxName +", this)";
		if( hasColumnGroup ) {
			out.print( "<tr>" );
			if( checkboxType == CHECKBOXTYPE_CHECK )
				out.print( "<th rowspan='2'><div name='title-checkbox' onclick='" + checkScript + "' class='checkbox'></div></th>" );
			else if( checkboxType == CHECKBOXTYPE_RADIO )
				out.print( "<th rowspan='2'><span title='checkbox' name='title_checkbox_radio'><img src='"+ imageBasePath +"/ico_check.gif'></span></th>" );
			if( numbering ) out.print( "<th rowspan='2'><span title='numbering' name='title_numbering'>No</span></th>" );

			int colspan = 0;
			ColumnGroup columnGroup = null;
			List<Column> groupColumnList = null;
			for( int c = 0; c < columns.length; c++ ) {
				if( columnGroup == columns[c].getColumnGroup() )
					colspan += columns[c].getColumnSize();
				else {
					if( columnGroup != null )
						out.print( "<th colspan='"+ colspan +"' class='header_top header-leftline header-rightline'>"+ columnGroup.getGroupTitle(null, msghandler) +"</th>" );

					colspan = columns[c].getColumnSize();
					columnGroup = columns[c].getColumnGroup();
				}
				if( columnGroup == null ) printHeaderCell( out, columns[c], 2 );
				else {
					if( groupColumnList == null ) {
						groupColumnList = new java.util.ArrayList<Column>();
					}
					groupColumnList.add( columns[c] );
				}
			}
			boolean hasLastColumnGroup = false;
			if( columnGroup != null ) {
				out.print( "<th colspan='"+ colspan +"' class='header_top header-leftline'>"+ columnGroup.getGroupTitle(null, msghandler) +"</th>" );
				hasLastColumnGroup = true;
			}
			out.println( "</tr>" );

			out.print( "<tr>" );
			String beforeGroupKey = null;
			for( int c = 0; c < groupColumnList.size(); c++ ) {
				ColumnGroup group = groupColumnList.get( c ).getColumnGroup();
				if( group != null ) {
					ColumnGroup nextGroup = c < groupColumnList.size() - 1 ? groupColumnList.get( c + 1 ).getColumnGroup() : null;
					boolean isGroupLeftLine = false;
					boolean isGroupRightLine = false;
					String currentGroupKey = group.getKey();
					if( beforeGroupKey == null || !beforeGroupKey.equals(currentGroupKey) ) {
						beforeGroupKey = currentGroupKey;
						isGroupLeftLine = true;
					}
					if( (nextGroup == null && !hasLastColumnGroup ) || (nextGroup != null && !currentGroupKey.equals(nextGroup.getKey())) ) {
						isGroupRightLine = true;
					}
					printHeaderCell( out, groupColumnList.get(c), 1, isGroupLeftLine, isGroupRightLine );
				}
			}
			out.println( "</tr>" );
		} else {
			out.print( "<tr class='header_one'>" );
			if( checkboxType == CHECKBOXTYPE_CHECK )
				//out.print( "<th><span title='checkbox' name='title_checkbox_check'><a href='"+ checkScript +"'><img src='"+ imageBasePath +"/ico_check.gif'></a></span></th>" );
				out.print( "<th><div name='title-checkbox' onclick='" + checkScript + "' class='checkbox'></div></th>" );
			else if( checkboxType == CHECKBOXTYPE_RADIO )
				out.print( "<th><span title='checkbox' name='title_checkbox_radio'><img src='"+ imageBasePath +"/ico_check.gif'></span></th>" );
			if( numbering ) out.print( "<th><span title='numbering' name='title_numbering'>No</span></th>" );

			for( int c = 0; c < columns.length; c++ )
				printHeaderCell( out, columns[c], 0 );
			out.println("</tr>");
		}
	}

	@Override
	public void printMessageLine( JspWriter out, String message, boolean hasDataLine ) throws IOException {
		if( hasDataLine )
			out.println( "<tr class='list_content'><td colspan='"+ getColumnSize() +"' class='list_linemsg'>"+ message +"</td></tr>" );
		else
			out.println( "<tr><td colspan='"+ getColumnSize() +"' class='list_msg'>"+ message +"</td></tr>" );
	}

	@Override
	public void printSummaryCell( JspWriter out, Column column, Map summaryMap, int row, int col ) throws IOException {
		Object columnAttr = column.getColumnAttr();
		if( columnAttr == null ) {
			columnAttr = columns[col].getColumnAttr();
			if( columnAttr == null ) columnAttr = "";
		}

		String colspan = ( column.getColumnSize() > 1 ? " colspan='"+ column.getColumnSize() +"'" : "" );
		String summaryId = ( (column instanceof ConstantColumn) ? "" : " id='summary_data'" );
		if( summaryMap == null && !(column instanceof ConstantColumn) ) {
			out.print( "<td "+ summaryId + colspan +" "+ columnAttr +">...</td>" );
			return;
		}

		HyperLink hyperLink = null;
		if( column instanceof LinkedColumn ) {
			hyperLink = ((LinkedColumn)column).getColumnLink();
			if( hyperLink == null || !hyperLink.isValidLink(summaryMap, sessionMng) )
				hyperLink = null;
		}

		if( hyperLink != null ) {
			String title = hyperLink.getHelpString( summaryMap, msghandler );

			out.print( "<td "+ summaryId + colspan +" "+ columnAttr +">" );
			out.print( "<a href='"+ hyperLink.getLinkString(summaryMap, msghandler) +"'" );
			out.print( "<a href='JavaScript:void(0);' onClick='JavaScript:setEventObject(event);"
					+ hyperLink.getLinkString(summaryMap, msghandler) +"'" );
			if( title != null ) out.print( " title='"+ title +"'" );
			out.print( ">" );
			out.print( getSummaryValue(column, summaryMap, row, col) );
			out.println( "</a></td>" );
		} else {
			String title = column.getColumnHelp( summaryMap, msghandler );

			if( title == null )
				out.print( "<td "+ summaryId + colspan +" "+ columnAttr +">" );
			else
				out.print( "<td "+ summaryId + colspan +" title='"+ title +"' "+ columnAttr +">" );
			out.print( getSummaryValue(column, summaryMap, row, col) );
			out.println( "</td>" );
		}
	}

	@Override
	public void printSummaryLine( JspWriter out ) throws IOException {
		for( int s = 0; s < summaryColumnLists.length; s++ ) {
			out.println( "<tr class='list_sum'>" );
			if( checkboxType == CHECKBOXTYPE_CHECK || checkboxType == CHECKBOXTYPE_RADIO ) out.print( "<td class='check'/>" );
			if( numbering ) out.print( "<td class='number'/>" );

			Column[] columns = summaryColumnLists[s].getColumns();
			for( int c = 0; c < columns.length; c++ )
				printSummaryCell( out, columns[c], summaryMap, s, c );
			out.println( "</tr>" );
		}
	}

	@Override
	public void printTableColgroup( JspWriter out ) throws IOException {
		printTableColgroup( out, false );
	}

	public void printTableColgroup( JspWriter out, boolean isDataLine ) throws IOException {
		out.print( "<colgroup>" );
		if( checkboxType == CHECKBOXTYPE_CHECK || checkboxType == CHECKBOXTYPE_RADIO ) out.print( "<col class='line-check'/>" );
		if( numbering ) out.print( "<col class='number'/>" );

		for( int c = 0; c < columns.length; c++ ) {
			Object columnAttr = columns[c].getColumnAttr();
			if( columnAttr == null ) columnAttr = "";
			out.print( "<col "+ columnAttr +"/>" );
			for( int i = 1; i < columns[c].getColumnSize(); i++ )
				out.print( "<col "+ columnAttr +"/>" );
		}
		out.println( "</colgroup>" );
	}

	@Override
	public void printTail( JspWriter out ) throws IOException {
		if( useVerticalScroll && this.heightResizable && ((records != null && records.size() > 0) || (printingErrors && errors != null)) ) {
			if( useFlexDataLine ) printFlexDataLine(out);
			out.println( "</table>" );
			out.println( "</div>" );

			if( records != null && records.size() > 0 && summaryColumnLists != null ) {
				out.println( "<div class='list_content_summary_scroll' style='height: expression(this.childNodes(0).offsetHeight); overflow-y: scroll;'>" );
				out.println( "<table class='list_content_summary' cellspacing='0' cellpadding='0' rules='rows'>" );
				printTableColgroup( out );
				printSummaryLine( out );
				if( useFlexDataLine ) printFlexDataLine(out);
				out.println( "</table>" );
				out.println( "</div>" );
			}
		} else {
			if( records != null && records.size() > 0 && summaryColumnLists != null )
				printSummaryLine( out );
			if( useFlexDataLine ) printFlexDataLine(out);
			out.println( "</table>" );
			if( records == null || records.size() == 0 ) {
				out.println( "</div>" );
			}
		}
	}

	@Override
	public void printPageIdentity( JspWriter out ) throws IOException {
		String reqId = PageMessageKeys.getRequestId(request);
		String pageId = PageMessageKeys.getPageId(htmlpage);
		PageMessageKeys.putPage(request, pageId);
		PageMessageKeys.putColumnListName(htmlpage, columnList.getName());
		out.print("<div class='colres' data-req-id='"+reqId+"' style='display:hidden'>");
		out.print("</div>");
	}

	@Override
	public void probeSummaryColumnList() {
		String pageId = PageMessageKeys.getPageId(htmlpage);
		if( summaryColumnLists != null ) {
			for( ColumnList scl : summaryColumnLists ) {
				PageMessageKeys.putColumnListName(pageId, scl.getName());
			}
		}
	}

	@Override
	public void setCheckboxName( String checkboxName ) {
		this.checkboxName = checkboxName;
	}

	@Override
	public void setCheckboxType( char checkboxType ) {
		this.checkboxType = checkboxType;
	}

	@Override
	public void setCheckboxTypeAndNumbering( char type ) {
		if( this.numbering = (type == CHECKBOXTYPE_NUMBER) )
			this.checkboxType = CHECKBOXTYPE_NONE;
		else
			this.checkboxType = type;
	}

	@Override
	public void setContentId( String contentId ) {
		this.contentId = contentId;
		this.prefixContentId += "_" + contentId;
	}

	@Override
	public void setFlexDataMaxWidth( int colIdx, int currLength ) {
		if( getFlexDataMaxWidth()[colIdx] < currLength )
			flexDataMaxWidths[colIdx] = currLength;
	}

	@Override
	public int[] getFlexDataMaxWidth() {
		return flexDataMaxWidths;
	}

	@Override
	public void setErrors( Collection<? extends Map> errors ) {
		this.errors = errors;
	}

	@Override
	public void setErrorPattern( RecordFormat errorPattern ) {
		this.errorPattern = errorPattern;
	}

	@Override
	public void setImageBasePath( String imageBasePath ) {
		this.imageBasePath = imageBasePath;
	}

	@Override
	public void setMaximumRowCount( int maximumRowCount ) {
		setMaximumRowCount( maximumRowCount, null );
	}

	@Override
	public void setMaximumRowCount( int maximumRowCount, String messageKey ) {
		this.maximumRowCount = maximumRowCount;
		this.maximumMessageKey = messageKey;
	}

	@Override
	public void setNumbering( boolean numbering ) {
		this.numbering = numbering;
	}

	@Override
	public void setPrintingErrors( boolean printingErrors ) {
		this.printingErrors = printingErrors;
	}

	@Override
	public void setPrintingHeader( boolean printingHeader ) {
		this.printingHeader = printingHeader;
	}

	@Override
	public void setPrintingUnderLine( boolean printingUnderLine ) {
		this.printingUnderLine = printingUnderLine;
	}

	@Override
	public void setPrimaryKeys( String... primaryKeys ) {
		this.primaryKeys = primaryKeys;
	}

	@Override
	public void setRecords( Collection<? extends Map> records ) {
		this.records = records;
	}

	@Override
	public void setScrollHeight( int scrollHeight ) {
		setScrollHeight( scrollHeight, true );
	}

	@Override
	public void setScrollHeight( int scrollHeight, boolean resizable ) {
		this.heightStyle = String.valueOf( scrollHeight );
		this.heightResizable = resizable;
	}

	@Override
	public void setScrollHeightStyle( String scrollStyle ) {
		this.scrollStyle = scrollStyle;
		this.heightResizable = false;
	}

	@Override
	public void setSortable( boolean sortable ) {
		disableSort = !sortable;
	}

	@Override
	public void setStartingNumber( int startingNumber ) {
		this.startingNumber = startingNumber;
	}

	@Override
	public void setSummaryColumnList( ColumnList summaryColumnList ) {
		if( summaryColumnList == null )
			this.summaryColumnLists = null;
		else
			setSummaryColumnList( summaryColumnList );
	}

	@Override
	public void setSummaryColumnLists( ColumnList... summaryColumnLists ) {
		this.summaryColumnLists = summaryColumnLists;
		probeSummaryColumnList();
	}

	@Override
	public void setSummaryMap( Map summaryMap ) {
		this.summaryMap = summaryMap;
	}

	@Override
	public void setUseFlexDataLine( boolean useFlexDataLine ) {
		this.useFlexDataLine = useFlexDataLine;
	}

	@Override
	public void setUseVerticalScroll( boolean useVerticalScroll ) {
		this.useVerticalScroll = useVerticalScroll;
	}
}
