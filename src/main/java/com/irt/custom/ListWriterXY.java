/*
 *	File Name:	ListWriterXY.java
 *	Version:	2.2.8c(dpr)
 *
 *	Description:
 *
 *	Note:
 *		style
 *			div.list_content_xy_data
 *			div.list_content_xy_data_f
 *			div.list_content_xy_header
 *			div.list_content_xy_header_f
 *			div.list_content_xy_header_top
 *			div.list_content_xy_summary
 *			div.list_content_xy_summary_f
 *			table.list_content_xy
 *			table.list_content_xy_data
 *			table.list_content_xy_data_f
 *			table.list_content_xy_header
 *			table.list_content_xy_header_f
 *			table.list_content_xy_summary
 *			table.list_content_xy_summary_f
 *			td.list_content_xy_d
 *			td.list_content_xy_d_s
 *			td.list_content_xy_header_top
 *			td.list_content_xy_f
 *			td.list_content_xy_s
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.8c	신규 UI/UX 적용
 *	hankalam	2021/03/31		2.2.7	div 스크롤 동기화 방식 jquery 로 변경(스크롤 표준 속도로 맞추기 위함)
 *	soma		2020/06/30		2.2.6	printInitScript(): 추가
 *	hankalam	2019/08/30		2.2.5	print() : content_xy_"+ contentId +"_data_f_tab 의 style width : inherit 으로 변경
 *										printHeader() : content_xy_"+ contentId +"_header_f_tab 의 style width : inherit 으로 변경
 *										printTail() : 틀고정 테이블 width 초기화 스크립트 추가
 *	GimHS		2015/02/27		2.2.4	printHead() : fitHeightToWindow() 호출시 두번째 파라미터를 true로 넘김(틀고정인 경우 true)
 *	GimHS		2014/08/29		2.2.3	CrossBrowsing 적용: Mobile user agent의 경우 x, y scroll을 사용 안하도록 수정
 *	GimHS		2012/12/31		2.2.2	CrossBrowsing 적용
 *										 -> JavaScript에서 Tag의 Style 변경시 style 속성 명시
 *										 -> scroll_"+ contentId +"(): event를 통해 srcElement를 가져오던걸 파라미터로 srcElement를 넘겨 처리
 *										 -> 리스트 화면에서 Table의 Header와 Summary에 항상 Y scroll 추가
 *										 -> 기타 스타일 변경
 *										 -> div 태그에서 onResize event를 지원하지 않아 onDblClick event로 대체
 *										 -> col 태그에서 width 외의 스타일을 지원하지 않아 td 태그에 스타일 추가
 *										 -> "rules='rows'" 속성 삭제
 *	stghr12		2008/03/31		2.2.1	heightResizable 사용
 *	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
 *										div.list_content_xy_header_top, td.list_content_xy_header_top 추가
 *										style 변경: rules='rows' 추가
 *	stghr12		2007/04/30		2.1.2	style 변경
 *	stghr12		2007/02/01		2.1.1	스크롤 속도 느린 문제 해결
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.custom;

import com.irt.data.cols.*;
import com.irt.html.HtmlPage;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

/**
 *
 */
public class ListWriterXY extends ListWriter implements com.irt.html.ListWriter {
	public final static int WHOLE						= 0;
	public final static int FIXED						= 1;
	public final static int UNFIXED						= 2;

	// variable
	protected int printingMode = WHOLE;
	protected String contentId;

	// columnList 정보
	ColumnList columnList;
	ColumnList columnList_f;
	ColumnList[] summaryColumnLists;
	ColumnList[] summaryColumnLists_f;

	// options
	char checkboxType = CHECKBOXTYPE_NONE;
	boolean numbering = false;

	public ListWriterXY( HttpServletRequest request, HtmlPage htmlpage, String contentId ) {
		this( request, htmlpage, contentId, (ColumnList)request.getAttribute("columnList"), (ColumnList)request.getAttribute("columnList_f") );
	}

	public ListWriterXY( HttpServletRequest request, HtmlPage htmlpage, String contentId, ColumnList columnList, ColumnList columnList_f ) {
		super( request, htmlpage, columnList );

		if( columnList_f == null )
			throw new IllegalArgumentException( "columnList_f cannot be null." );

		this.contentId = contentId;
		this.columnList = columnList;
		this.columnList_f = columnList_f;
		this.checkboxType = super.checkboxType;
		this.numbering = super.numbering;

		setPrintingMode( FIXED );
		Object summaryColumnList = request.getAttribute( "summaryColumnList_f" );
		if( summaryColumnList instanceof ColumnList )
			setSummaryColumnList( (ColumnList)summaryColumnList );
		else if( summaryColumnList instanceof ColumnList[] )
			setSummaryColumnLists( (ColumnList[])summaryColumnList );
	}

	@Override
	public int getColumnSize() {
		return ( printingMode == WHOLE ? 3 : super.getColumnSize() );
	}

	@Override
	public void print( JspWriter out ) throws IOException {
		if( records == null || records.size() == 0 ) {
			print( out, msghandler.getMessage("MSG_NO_RECORD_FOUND") );
			printInitScript( out );
			return;
		}

		printHead( out );

		String scrollHeight = "";
		if( useVerticalScroll && !heightResizable )
			scrollHeight = "expression(this.childNodes(0).offsetHeight > "+ this.heightStyle +" ? "+ this.heightStyle +" : \"\")";

		// 데이터 출력(고정부분)
		setPrintingMode( FIXED );
		{
			int row = 0;

			out.println( "<tr><td class='list_content_xy_f' valign='top'>" );
			out.print( "<div id='content_xy_"+ contentId +"_data_f' class='list_content_xy_data_f'" );
			if( htmlpage.isMobileAgent() ) {
				out.print( " style='height: "+ scrollHeight +"; width: 100%; overflow-y: hidden;'>" );
			} else {
				out.print( " style='height: "+ scrollHeight +"; width: 100%; overflow-x: scroll; overflow-y: hidden;'>" );
			}
			out.print( "<table id='content_xy_"+ contentId +"_data_f_tab' class='list_content_xy_data_f' cellspacing='0' cellpadding='0'" );
			out.println( " style='width: inherit; table-layout: fixed;'>" );
			printTableColgroup( out );
			for( java.util.Iterator iterator = records.iterator(); iterator.hasNext(); row++ ) {
				Map recordMap = (Map)iterator.next();

				if( maximumRowCount > 0 && maximumRowCount == row ) {
					if( maximumMessageKey != null ) {
						printMessageLine( out, "", true );
					}
					break;
				}
				printDataLine( out, recordMap, row );
			}
			out.println( "</table></div>" );
			out.println( "</td>" );
		}

		// 데이터 출력(비고정부분)
		setPrintingMode( UNFIXED );
		{
			int row = 0;

			out.println( "<td class='list_content_xy_d_s'>" );
			out.print( "<div id='content_xy_"+ contentId +"_data' class='list_content_xy_data'" );
			if( htmlpage.isMobileAgent() ) {
				out.print( " style='height: "+ scrollHeight +"; width: 100%;'>" );
			} else {
				out.print( " style='height: "+ scrollHeight +"; width: 100%; overflow-x: scroll; overflow-y: scroll;'" );
				if( heightResizable ) out.print( " onDblClick='JavaScript:resize_"+ contentId +"();'>" );
			}
			out.print( "<table class='list_content_xy_data' cellspacing='0' cellpadding='0'" );
			out.println( " style='width: expression(this.offsetWidth); table-layout: fixed;'>" );
			printRightTableColgroup( out );
			for( java.util.Iterator iterator = records.iterator(); iterator.hasNext(); row++ ) {
				Map recordMap = (Map)iterator.next();

				if( maximumRowCount > 0 && maximumRowCount == row ) {
					if( maximumMessageKey != null ) {
						printMessageLine( out, msghandler.getMessage(maximumMessageKey, String.valueOf(maximumRowCount)), true );
					}
					break;
				}
				printDataLine( out, recordMap, row );
			}
			out.println( "</table></div>" );
			out.println( "</td></tr>" );
		}

		printTail( out );
		printInitScript( out );
	}

	@Override
	public void print( JspWriter out, String linemsg ) throws IOException {
		printHead( out );
		printMessageLine( out, linemsg, false );

/* Firefox에서 Y축 scroll까지 생기는 문제가 있어 삭제 (그외 브라우저에서는 X축 scroll만 생김)
		setPrintingMode( FIXED );
		out.println( "<tr><td class='list_content_xy_f' valign='top'>" );
		out.print( "<div id='content_xy_"+ contentId +"_data_f' class='list_content_xy_data_f'" );
		if( htmlpage.isMobileAgent() )
			out.print( " style='width: 100%;'>" );
		else
			out.print( " style='width: 100%; overflow-x: scroll; overflow-y: hidden;'>" );
		out.print( "<table id='content_xy_"+ contentId +"_data_f_tab' class='list_content_xy_data_f' cellspacing='0' cellpadding='0'" );
		out.println( " style='width: auto; table-layout: fixed;'>" );
		printTableColgroup( out );
		out.println( "</table></div>" );
		out.println( "</td>" );

		setPrintingMode( UNFIXED );
		out.println( "<td class='list_content_xy_d'>" );
		out.print( "<div id='content_xy_"+ contentId +"_data' class='list_content_xy_data'" );
		if( htmlpage.isMobileAgent() ) {
			out.print( " style='width: 100%;'>" );
		} else {
			out.print( " style='width: 100%; overflow-x: scroll; overflow-y: scroll;'>" );
		}
		out.println( "<table class='list_content_xy_data' cellspacing='0' cellpadding='0' style='width: expression(this.offsetWidth); table-layout: fixed;'>" );
		printTableColgroup( out );
		out.println( "</table></div>" );
		out.println( "</td>" );
		out.println( "</tr>" );
*/

		printTail( out );
	}

	@Override
	public void printHead( JspWriter out ) throws IOException {
		out.println( "<script type='text/javascript'>" );
		out.println( "function resize_"+ contentId +"() {" );
		out.println( "	fitHeightToWindow(document.all.content_xy_"+ contentId +"_data, "+ this.heightStyle +", true);" );
		out.println( "	document.all.content_xy_"+ contentId +"_data.style.overflowY = 'scroll';" );
		out.println( "	document.all.content_xy_"+ contentId +"_data_f.style.height = document.all.content_xy_"+ contentId +"_data.style.height;" );
		out.println( "}" );
		out.println();
		out.println( "var timeout;");
		out.println( "$(document).ready( function() {" );
		out.println( "	$('#content_xy_"+ contentId +"_data, #content_xy_"+ contentId +"_data_f').on( 'scroll', function callback() {" );
		out.println( "		clearTimeout(timeout);" );
		out.println( "		if( $(this).is('#content_xy_"+ contentId +"_data') ) {" );
		out.println( "			$('#content_xy_"+ contentId +"_header').off( 'scroll' ).scrollLeft( $(this).scrollLeft() );" );
		out.println( "			if( $('#content_xy_"+ contentId +"_summary').length ) {" );
		out.println( "				$('#content_xy_"+ contentId +"_summary').off( 'scroll' ).scrollLeft( $(this).scrollLeft() );" );
		out.println( "			}" );
		out.println( "			$('#content_xy_"+ contentId +"_data_f').off( 'scroll' ).scrollTop( $(this).scrollTop() );" );
		out.println( "			timeout = setTimeout( function() {" );
		out.println( "				$('#content_xy_"+ contentId +"_header').on( 'scroll', callback );" );
		out.println( "				if( $('#content_xy_"+ contentId +"_summary').length ) {" );
		out.println( "					$('#content_xy_"+ contentId +"_summary').on( 'scroll', callback );" );
		out.println( "				}" );
		out.println( "				$('#content_xy_"+ contentId +"_data_f').on( 'scroll', callback );" );
		out.println( "			}, 100 );" );
		out.println( "			if( $('#content_xy_"+ contentId +"_data').scrollLeft() > 0 ) {" );
		out.println( "				$('#"+ contentId +"_content_xy_header_f').addClass( 'right-shadow' );" );
		out.println( "				$('#content_xy_"+ contentId +"_data_f').addClass( 'right-shadow' );" );
		out.println( "			} else {" );
		out.println( "				$('#"+ contentId +"_content_xy_header_f').removeClass( 'right-shadow' );" );
		out.println( "				$('#content_xy_"+ contentId +"_data_f').removeClass( 'right-shadow' );" );
		out.println( "			}" );
		out.println( "		} else if( $(this).is('#content_xy_"+ contentId +"_data_f') ) {" );
		out.println( "			var source = $(this);" );
		out.println( "			var target;" );
		out.println( "			target = $('#content_xy_"+ contentId +"_data');" );
		out.println( "			target.off( 'scroll' ).scrollTop( source.scrollTop() );" );
		out.println( "			timeout = setTimeout( function() {" );
		out.println( "				target.on( 'scroll', callback );" );
		out.println( "			}, 100 )" );
		out.println( "		}" );
		out.println( "	});" );
		out.println( "});" );
		out.println( "</script>" );

		out.print( "<table class='list_content_xy' cellspacing='0' cellpadding='0'" );
		out.println( " style='width: expression(document.all.content_title.clientWidth); table-layout: fixed;'>" );
		out.println( "<colgroup>" );
		out.println( "<col id='content_xy_"+ contentId +"_col_header'/>" );
		out.println( "<col/>" );
		out.println( "</colgroup>" );

		out.println( "<tr><td colspan='2' class='list_content_xy_header_top'><div class='list_content_xy_header_top'/></td></tr>" );

		printHeader( out );

		setPrintingMode( WHOLE );
		printErrors( out );
	}

	public void printHeader( JspWriter out ) throws IOException {
		setPrintingMode( FIXED );
		out.println( "<tr><td class='list_content_xy_f' valign='top'>" );
		out.print( "<div id='list_content_xy_header_f' class='list_content_xy_header_f' style='width: 40px'>" );
		out.print( "<table id='content_xy_"+ contentId +"_header_f_tab' class='list_content_xy_header_f' cellspacing='0' cellpadding='0'" );
		out.println( " style='width: inherit; table-layout: fixed;'>" );
		printTableColgroup( out );
		printHeaderLine( out );
		out.println( "</table></div>" );
		out.println( "</td>" );

		setPrintingMode( UNFIXED );
		out.println( "<td class='list_content_xy_d'>" );
		if( htmlpage.isMobileAgent() )
			out.print( "<div id='content_xy_"+ contentId +"_header' class='list_content_xy_header' style='width: 100%; overflow-x: hidden;'>" );
		else
			out.print( "<div id='content_xy_"+ contentId +"_header' class='list_content_xy_header' style='width: 100%; overflow-x: hidden; overflow-y: scroll;'>" );
		out.print( "<table id='content_xy_"+ contentId +"_header_tab' class='list_content_xy_header' cellspacing='0' cellpadding='0'" );
		out.println( " style='width: expression(this.offsetWidth); table-layout: fixed;'>" );
		printRightTableColgroup( out );
		printHeaderLine( out );
		out.println( "</table></div>" );
		out.println( "</td>" );
		out.println( "</tr>" );
	}

	@Override
	public void printMessageLine( JspWriter out, String message, boolean hasDataLine ) throws IOException {
		if( printingMode == WHOLE ) {
			if( hasDataLine )
				out.print( "<tr class='list_content'><td colspan='2' class='list_linemsg list_content_xy_f_d'>"+ message +"</td>" );
			else
				out.print( "<tr><td colspan='2' class='list_msg list_content_xy_f_d'>"+ message +"</td>" );
			out.println( "</tr>" );
		} else
			super.printMessageLine( out, message, hasDataLine );
	}

	public void printSummary( JspWriter out ) throws IOException {
		setPrintingMode( FIXED );
		out.println( "<tr><td class='list_content_xy_f' valign='top'>" );
		out.println( "<div class='list_content_xy_summary_f'>" );
		out.print( "<table class='list_content_xy_summary_f' cellspacing='0' cellpadding='0'" );
		out.println( " style='width: auto; table-layout: fixed;'>" );
		printTableColgroup( out );
		printSummaryLine( out );
		out.println( "</table></div>" );
		out.println( "</td>" );

		setPrintingMode( UNFIXED );
		out.println( "<td class='list_content_xy_d'>" );
		out.print( "<div id='content_xy_"+ contentId +"_summary' class='list_content_xy_summary'" );
		if( htmlpage.isMobileAgent() )
			out.println( " style='width: 100%;'>" );
		else
			out.println( " style='width: 100%; overflow-x: hidden; overflow-y: scroll;'>" );
		out.print( "<table class='list_content_xy_summary' cellspacing='0' cellpadding='0'" );
		out.println( " style='width: expression(this.offsetWidth); table-layout: fixed;'>" );
		printRightTableColgroup( out );
		printSummaryLine( out );
		out.println( "</table></div>" );
		out.println( "</td>" );
		out.println( "</tr>" );
	}

	@Override
	public void printTableColgroup( JspWriter out, boolean isDataLine ) throws IOException {
		out.print( "<colgroup>" );
		if( checkboxType == CHECKBOXTYPE_CHECK || checkboxType == CHECKBOXTYPE_RADIO ) out.print( "<col class='line-check' style='width: 50px;'/>" );
		if( numbering ) out.print( "<col class='number' style='width: 50px;'/>" );

		for( int c = 0; c < columns.length; c++ ) {
			Object columnAttr = columns[c].getColumnAttr();
			if( columnAttr == null ) columnAttr = "";
			out.print( "<col "+ columnAttr +"/>" );
			for( int i = 1; i < columns[c].getColumnSize(); i++ )
				out.print( "<col "+ columnAttr +"/>" );
		}
		out.println( "</colgroup>" );
	}

	public void printRightTableColgroup( JspWriter out ) throws IOException {
		out.print( "<colgroup>" );
		//if( checkboxType == CHECKBOXTYPE_CHECK || checkboxType == CHECKBOXTYPE_RADIO ) out.print( "<col class='line-check'/>" );
		//if( numbering ) out.print( "<col class='number'/>" );

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
		if( records != null && records.size() > 0 && summaryColumnLists != null )
			printSummary( out );
		out.println( "</table>" );
	}

	public void printInitScript( JspWriter out ) throws IOException {
		String header = "document.all.content_xy_"+ contentId +"_header_tab";
		String header_f = "document.all.content_xy_"+ contentId +"_header_f_tab";

		out.println( "<script type='text/javascript'>" );
		out.println( "function initTable_"+ contentId +"() {" );
		out.println( "	if( document.all.content_xy_"+ contentId +"_data_f_tab ) {" );
		out.println( "		document.all.content_xy_"+ contentId +"_data_f_tab.style.width = document.all.content_xy_"+ contentId +"_header_f_tab.offsetWidth;" );
		out.println( "		document.all.content_xy_"+ contentId +"_col_header.style.width = document.all.content_xy_"+ contentId +"_header_f_tab.offsetWidth;" );
		out.println( "		document.all.list_content_xy_header_f.style.width = document.all.content_xy_"+ contentId +"_header_f_tab.offsetWidth;" );
		out.println( "		if( document.all.content_xy_"+ contentId +"_header_f_tab.offsetWidth < document.all.content_xy_"+ contentId +"_data_f_tab.offsetWidth ) {" );
		out.println( "			document.all.content_xy_"+ contentId +"_col_header.style.width = document.all.content_xy_"+ contentId +"_header_f_tab.offsetWidth;" );
		out.println( "			document.all.content_xy_"+ contentId +"_data_f_tab.style.width = document.all.content_xy_"+ contentId +"_col_header.style.width;" );
		out.println( "		}" );
		out.println( "	} else" );
		out.println( "		document.all.content_xy_"+ contentId +"_col_header.style.width = document.all.content_xy_"+ contentId +"_header_f_tab.offsetWidth;" );
		out.println( "	if( "+ header +".clientHeight > "+ header_f +".clientHeight )" );
		out.println( "		"+ header_f +".style.height = "+ header +".clientHeight;" );
		out.println( "	else if( "+ header +".clientHeight < "+ header_f +".clientHeight )" );
		out.println( "		"+ header +".style.height = "+ header_f +".clientHeight;" );
		out.println( "}" );
		out.println( "attachWindowEvent( 'load', initTable_"+ contentId +" );" );
		out.println( "</script>" );
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
	public void setNumbering( boolean numbering ) {
		this.numbering = numbering;
	}

	protected void setPrintingMode( int printingMode ) {
		this.printingMode = printingMode;
		if( printingMode == FIXED ) {
			super.checkboxType = this.checkboxType;
			super.numbering = this.numbering;
			super.columnList = this.columnList_f;
			super.columns = this.columnList_f.getColumns();
			super.summaryColumnLists = this.summaryColumnLists_f;
		} else {
			super.checkboxType = CHECKBOXTYPE_NONE;
			super.numbering = false;
			super.columnList = this.columnList;
			super.columns = this.columnList.getColumns();
			super.summaryColumnLists = this.summaryColumnLists;
		}
	}

	@Override
	public void setSummaryColumnList( ColumnList summaryColumnList ) {
		if( printingMode == FIXED ) {
			if( summaryColumnList == null )
				this.summaryColumnLists_f = null;
			else
				this.summaryColumnLists_f = new ColumnList[] { summaryColumnList } ;
		} else {
			if( summaryColumnList == null )
				this.summaryColumnLists = null;
			else
				this.summaryColumnLists = new ColumnList[] { summaryColumnList } ;
		}
	}

	@Override
	public void setSummaryColumnLists( ColumnList... summaryColumnLists ) {
		if( printingMode == FIXED )
			this.summaryColumnLists_f = summaryColumnLists;
		else
			this.summaryColumnLists = summaryColumnLists;
	}
}
