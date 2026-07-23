/*
 *	File Name:	SheetWriter.java
 *	Version:	2.2.1c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.1c	writeData(): 엑셀에 hyperlink 삽입기능 추가
 *	jbaek		2013/11/30		2.2.1	write(): SAX Process시 setBorder disable시킴.
 *	jbaek		2013/08/30		2.2.0	copy from map_pds. RBMWorkbook상속.
 *
**/

package com.irt.util;

import com.irt.data.DataException;
import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;
import com.irt.sql.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 */
public class SheetWriter extends com.irt.util.RBMWorkbook {
	private final static int ROWNUM_HEADER				= 0;
	private final static int ROWNUM_DATA				= 1;

	QueryFactory factory;
	SQLHandler handler;
	MessageHandler msghandler;

	List<Map<String, Object>> recordList;

	ColumnList columnList;
	Column[] columns;
	int[] cellTypes;
	CellStyle headerCellStyle, defaultCellStyle, dataCellStyles[];

	public SheetWriter( SQLHandler handler, QueryFactory factory, Workbook workbook ) {
		this.factory = factory;
		this.handler = handler;
		this.msghandler = handler.getMessageHandler();
		this.workbook = workbook;
		this.recordList = null;

		initCellStyle();
	}

	public SheetWriter( SQLHandler handler, List<Map<String, Object>> recordList, Workbook workbook ) {
		this.factory = null;
		this.handler = handler;
		this.msghandler = handler.getMessageHandler();
		this.workbook = workbook;
		this.recordList = recordList;

		initCellStyle();
	}

	private void initCellStyle() {
		Font defaultFont = workbook.createFont();
		defaultFont.setFontName( "Tahoma" );
		defaultFont.setFontHeightInPoints( (short)9 );

		Font hssfFont = workbook.createFont();
		hssfFont.setBoldweight( Font.BOLDWEIGHT_BOLD );
		hssfFont.setFontName( "Tahoma" );
		hssfFont.setFontHeightInPoints( (short)10 );
		hssfFont.setBoldweight( Font.BOLDWEIGHT_BOLD );
		hssfFont.setFontName( "Tahoma" );
		hssfFont.setFontHeightInPoints( (short)10 );

		defaultCellStyle = workbook.createCellStyle();
		defaultCellStyle.setBorderLeft( CellStyle.BORDER_THIN );
		defaultCellStyle.setBorderRight( CellStyle.BORDER_THIN );
		defaultCellStyle.setBorderBottom( CellStyle.BORDER_DASHED );
		defaultCellStyle.setFont( defaultFont );

		headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setBorderLeft( CellStyle.BORDER_THIN );
		headerCellStyle.setBorderRight( CellStyle.BORDER_THIN );
		headerCellStyle.setBorderTop( CellStyle.BORDER_MEDIUM );
		headerCellStyle.setBorderBottom( CellStyle.BORDER_MEDIUM );
		headerCellStyle.setAlignment( CellStyle.ALIGN_CENTER );
		headerCellStyle.setFillPattern( CellStyle.SOLID_FOREGROUND );
		headerCellStyle.setFillForegroundColor( IndexedColors.GREY_25_PERCENT.getIndex() );
		headerCellStyle.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
		headerCellStyle.setWrapText( true );
		headerCellStyle.setFont( hssfFont = workbook.createFont() );
		hssfFont.setBoldweight( Font.BOLDWEIGHT_BOLD );
		hssfFont.setFontName( "Tahoma" );
		hssfFont.setFontHeightInPoints( (short)10 );
	}

	public void setColumnList( ColumnList columnList ) {
		this.columnList = columnList;
		this.columns = columnList.getColumns();
		this.cellTypes = new int[ columnList.getColumnCount() ];
		this.dataCellStyles = new CellStyle[ columnList.getColumnCount() ];

		for( int c = 0; c < this.columns.length; c++ ) {
			String fieldKey = this.columns[c].getFieldKey();

			dataCellStyles[c] = SS.createCellStyle( workbook, this.columns[c], defaultCellStyle );
			try {
				SS.ColumnStyle columnStyle = (SS.ColumnStyle)this.columns[c].getColumnAttr();
				if( columnStyle != null )
					cellTypes[c] = columnStyle.getCellType();
			} catch( ClassCastException castEx ) {}
		}
	}

	public void write( String sheetName, Map<String, ? extends Object> conditionMap ) throws DataException, IOException, SQLException {
		if( columnList == null ) throw new IllegalStateException( "no ColumnList" );

		this.sheet = workbook.createSheet( sheetName );

		// columnWidth 설정
		for( int c = 0; c < this.columns.length; c++ ) {
			try {
				SS.ColumnStyle columnStyle = (SS.ColumnStyle)this.columns[c].getColumnAttr();
				if( columnStyle != null && columnStyle.getWidth() > 0 )
					sheet.setColumnWidth( c, columnStyle.getWidth() );
			} catch( ClassCastException castEx ) {}
		}

		writeHeader( 0, 0, conditionMap );

		int recordCount = 0;
		if( this.recordList == null ) {
			String[] fieldKeys = columnList.getFieldKeys();

			QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
			if( columnList.getSortKeys() != null ) {
				String[] sortKeys = columnList.getSortKeys();
				String[] sortFieldKeys = new String[ sortKeys.length ];
				String[] sortWays = new String[ sortKeys.length ];

				for( int k = 0; k < sortKeys.length; k++ ) {
					String[] keys = sortKeys[k].split( "#", 2 );
					if( keys.length >= 2 ) {
						sortFieldKeys[k] = keys[0];
						if( "DESC".equalsIgnoreCase(keys[1]) || "ASC".equalsIgnoreCase(keys[1]) )
							sortWays[k] = keys[1].toUpperCase();
					} else
						sortFieldKeys[k] = keys[0];
				}

				factory.setDataQuery( querybuf, sortFieldKeys );
				for( int k = 0; k < sortKeys.length; k++ ) {
					if( sortWays[k] == null )
						querybuf.appendOrderByFieldName( sortFieldKeys[k] );
					else
						querybuf.appendOrderByFieldName( sortFieldKeys[k], sortWays[k] );
				}
			}

			this.recordList = SQLManager.getRecordList( handler, querybuf, 0, -1 );
		}
		if( this.recordList != null ) {
			recordCount = this.recordList.size();
			writeData( ROWNUM_DATA, 0, this.recordList );
		}

		// SAX Process시 xlfRowWindowSize 만큼만 재접근 허용됨. 즉 default 100row만 재접근 허용되므로 setBorder 비사용.
		if( false == workbook instanceof org.apache.poi.xssf.streaming.SXSSFWorkbook )
			SS.setBorder(
				workbook, sheet
				, new CellRangeAddress( ROWNUM_DATA - 1, ROWNUM_DATA + recordCount - 1, 0, columnList.getColumnCount() - 1 )
				, CellStyle.BORDER_MEDIUM, CellStyle.BORDER_MEDIUM, CellStyle.BORDER_MEDIUM, CellStyle.BORDER_MEDIUM
			);

		sheet.createFreezePane( 0, ROWNUM_DATA );
	}

	public void writeData( int rownum, int colnum, List<Map<String, Object>> recordList ) {
		Map<String, Integer> linkImageUriMap = null;
		for( Map<String, Object> recordMap : recordList ) {
			int cellnum = colnum;

			Row ssRow = sheet.createRow( rownum++ );
			for( int c = 0; c < this.columns.length; c++ ) {
				String columnAttr = (String)this.columns[c].getColumnAttr();
				String linkType = StringUtil.extractAttrValue( columnAttr, "link-type" );
				if( linkType != null ) {
					String href = (String)this.columns[c].getColumnValue( recordMap, msghandler );
					int linkTypeIdx = -1;
					if( linkType.equals( "document" ) ) {
						linkTypeIdx = Hyperlink.LINK_DOCUMENT;
					} else if( linkType.equals( "url" ) ) {
						linkTypeIdx = Hyperlink.LINK_URL;
					} else if( linkType.equals( "file" ) ) {
						linkTypeIdx = Hyperlink.LINK_FILE;
					} else if( linkType.equals( "email" ) ) {
						linkTypeIdx = Hyperlink.LINK_EMAIL;
					}

					Object cellValue = null;
					String help = this.columns[c].getColumnHelp( recordMap, msghandler );

					Hyperlink link_sheet = sheet.getWorkbook().getCreationHelper().createHyperlink( linkTypeIdx );
					link_sheet.setAddress( href );
					link_sheet.setLabel( help );
					cellValue = link_sheet;

					SS.createCell( ssRow, cellnum, cellValue, dataCellStyles[c] );
					cellnum += this.columns[c].getColumnSize();
				} else {
					Object cellValue = this.columns[c].getColumnValue( recordMap, msghandler );
					if( cellValue instanceof String && cellTypes[c] == Cell.CELL_TYPE_NUMERIC ) {
						try {
							cellValue = Double.valueOf( (String)cellValue );
						} catch( NumberFormatException numberEx ) {}
					}

					SS.createCell( ssRow, cellnum, cellValue, dataCellStyles[c] );
					cellnum += this.columns[c].getColumnSize();
				}
			}
		}
	}

	public void writeHeader( int rownum, int colnum, Map<String, ? extends Object> conditionMap ) throws SQLException {
		Row ssRow = sheet.createRow( ROWNUM_DATA - 1 );
		for( int c = 0; c < this.columns.length; c++ ) {
			Object cellValue = this.columns[c].getColumnTitle( null, msghandler );
			Cell ssCell = SS.createCell( ssRow, c, cellValue, headerCellStyle );
		}
	}
}
