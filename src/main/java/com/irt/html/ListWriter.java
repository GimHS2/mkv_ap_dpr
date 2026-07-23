/*
 *	File Name:	ListWriter.java
 *	Version:	2.2.3c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.3c	setContentId() 추가
 *	stghr12		2011/02/28		2.2.3	setScrollHeightStyle() 추가
 *	stghr12		2010/07/31		2.2.2	setImageBasePath(), setPrintingUnderLine() 추가
 *	stghr12		2008/03/31		2.2.1	setMaximumRows() 삭제
 *										setScrollHeight(scrollHeight, resizable) 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										setScrollHeight() 추가
 *	stghr12		2007/10/31		2.1.2	getStartingNumber(), print( out, rowsize ), setPrintingErrors(), setPrintingHeader(), setStartingNumber() 추가
 *	stghr12		2007/04/30		2.1.1	printDataLineSeperate() 제거
 *										setPrimaryKeys() 추가
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;
import com.irt.data.format.RecordFormat;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.servlet.jsp.JspWriter;


/**
 *
 */
public interface ListWriter {
	public final static char CHECKBOXTYPE_CHECK			= 'C';
	public final static char CHECKBOXTYPE_RADIO			= 'R';
	public final static char CHECKBOXTYPE_NUMBER		= 'N';
	public final static char CHECKBOXTYPE_NONE			= ' ';

	public boolean containsData();

	public boolean enableCheckbox( Map recordMap, int row );

	public int getColumnSize();

	public String getColumnValue( Column column, Map recordMap, int row, int col );

	public int getStartingNumber();

	public String getSummaryValue( Column column, Map summaryMap, int row, int col );

	/**
	 * printHead(out), Data라인 출력, printTail(out)
	 */
	public void print( JspWriter out ) throws IOException;

	public void print( JspWriter out, int rowsize ) throws IOException, UnsupportedOperationException;

	public void print( JspWriter out, String linemsg ) throws IOException;

	public void printDataCell( JspWriter out, Column column, Map recordMap, int row, int col ) throws IOException;

	public void printDataLine( JspWriter out, Map recordMap, int row ) throws IOException;

	public void printDataLineAttribute( JspWriter out, Map recordMap, int row ) throws IOException;

	public void printErrors( JspWriter out ) throws IOException;

	/**
	 * Data라인을 앞부분 출력
	 */
	public void printHead( JspWriter out ) throws IOException;

	public void printHeaderCell( JspWriter out, Column column, int rowspan ) throws IOException;

	public void printHeaderLine( JspWriter out ) throws IOException;

	public void printMessageLine( JspWriter out, String message, boolean hasDataLine ) throws IOException;

	public void printSummaryCell( JspWriter out, Column column, Map summaryMap, int row, int col ) throws IOException;

	public void printSummaryLine( JspWriter out ) throws IOException;

	public void printTableColgroup( JspWriter out ) throws IOException;

	/**
	 * Data라인을 뒤부분 출력
	 */
	public void printTail( JspWriter out ) throws IOException;

	public void setCheckboxName( String checkboxName );

	public void setCheckboxType( char checkboxType );

	public void setCheckboxTypeAndNumbering( char type );

	public void setContentId( String contendId );

	public void setErrors( Collection<? extends Map> errors );

	public void setErrorPattern( RecordFormat errorPattern );

	public void setImageBasePath( String imageBasePath );

	/**
	 * 화면에 표시할 최대 표시건수(Record 건수).
	 */
	public void setMaximumRowCount( int maximumRowCount );

	/**
	 * 화면에 표시할 최대 표시건수(Record 건수).
	 * @param messageKey 최대건수를 넘었을 경우 표시할 messageKey, null일 경우 표시안 함
	 */
	public void setMaximumRowCount( int maximumRowCount, String messageKey );

	public void setNumbering( boolean numbering );

	public void setPrimaryKeys( String... primaryKeys );

	public void setPrintingErrors( boolean printingErrors );

	public void setPrintingHeader( boolean printingHeader );

	public void setPrintingUnderLine( boolean printingUnderLine );

	public void setRecords( Collection<? extends Map> records );

	public void setScrollHeight( int scrollHeight );

	public void setScrollHeight( int scrollHeight, boolean resizable );

	public void setScrollHeightStyle( String scrollStyle );

	public void setSortable( boolean sortable );

	public void setStartingNumber( int startingNumber );

	public void setSummaryColumnList( ColumnList summaryColumnList );

	public void setSummaryColumnLists( ColumnList... summaryColumnLists );

	public void setSummaryMap( Map summaryMap );

	public void setUseVerticalScroll( boolean useVerticalScroll );
}
