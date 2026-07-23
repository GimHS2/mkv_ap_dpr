/*
 *	File Name:	HSSFDataWriter.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2009/01/31		2.2.1	POI 3.2에 맞게 수정
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	create
 *
**/

package com.irt.util;

import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;

/**
 *
 */
public class HSSFDataWriter implements com.irt.data.DataWriter {
	private final static int STYLE_TITLE				= 0;
	private final static int STYLE_DATA					= 1;
	private final static int STYLE_SUMMARY				= 2;

	HSSFWorkbook workbook;
	HSSFSheet sheet;
	HSSFRow hssfRow;
	HSSFCellStyle[] defaultCellStyles, dataCellStyles, summaryCellStyles;
	int[] cellTypes;

	char dataType = DATA;
	int rownum, cellnum;

	public HSSFDataWriter() {
		this( new HSSFWorkbook() );
	}

	public HSSFDataWriter( HSSFWorkbook workbook ) {
		this( workbook, workbook.createSheet() );
	}

	public HSSFDataWriter( HSSFWorkbook workbook, HSSFSheet sheet ) {
		this.workbook = workbook;
		this.sheet = sheet;
		this.hssfRow = null;
		this.rownum = this.cellnum = -1;

		HSSFFont hssfFont;

		this.defaultCellStyles = new HSSFCellStyle[3];
		this.defaultCellStyles[STYLE_TITLE] = workbook.createCellStyle();
		this.defaultCellStyles[STYLE_TITLE].setFillPattern( HSSFCellStyle.SOLID_FOREGROUND );
		this.defaultCellStyles[STYLE_TITLE].setFillForegroundColor( new HSSFColor.LIGHT_YELLOW().getIndex() );
		this.defaultCellStyles[STYLE_TITLE].setVerticalAlignment( HSSFCellStyle.VERTICAL_CENTER );
		this.defaultCellStyles[STYLE_TITLE].setAlignment( HSSFCellStyle.ALIGN_CENTER );
		this.defaultCellStyles[STYLE_TITLE].setWrapText( true );
		this.defaultCellStyles[STYLE_TITLE].setBorderLeft( HSSFCellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_TITLE].setBorderRight( HSSFCellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_TITLE].setBorderTop( HSSFCellStyle.BORDER_MEDIUM );
		this.defaultCellStyles[STYLE_TITLE].setBorderBottom( HSSFCellStyle.BORDER_MEDIUM );
		this.defaultCellStyles[STYLE_TITLE].setFont( hssfFont = workbook.createFont() );
		hssfFont.setBoldweight( HSSFFont.BOLDWEIGHT_BOLD );

		this.defaultCellStyles[STYLE_DATA] = workbook.createCellStyle();
		this.defaultCellStyles[STYLE_DATA].setVerticalAlignment( HSSFCellStyle.VERTICAL_CENTER );
		this.defaultCellStyles[STYLE_DATA].setBorderLeft( HSSFCellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_DATA].setBorderRight( HSSFCellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_DATA].setBorderTop( HSSFCellStyle.BORDER_DASHED );
		this.defaultCellStyles[STYLE_DATA].setBorderBottom( HSSFCellStyle.BORDER_DASHED );

		this.defaultCellStyles[STYLE_SUMMARY] = workbook.createCellStyle();
		this.defaultCellStyles[STYLE_SUMMARY].setFillPattern( HSSFCellStyle.SOLID_FOREGROUND );
		this.defaultCellStyles[STYLE_SUMMARY].setFillForegroundColor( new HSSFColor.GREY_25_PERCENT().getIndex() );
		this.defaultCellStyles[STYLE_SUMMARY].setVerticalAlignment( HSSFCellStyle.VERTICAL_CENTER );
		this.defaultCellStyles[STYLE_SUMMARY].setAlignment( HSSFCellStyle.ALIGN_CENTER );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderLeft( HSSFCellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderRight( HSSFCellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderTop( HSSFCellStyle.BORDER_MEDIUM );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderBottom( HSSFCellStyle.BORDER_MEDIUM );
	}

	public void close() {}

	private HSSFCell createCell() {
		if( cellnum == -1 ) createRow();

		HSSFCell hssfCell = HSSF.createCell( hssfRow, cellnum );
		HSSFCellStyle cellStyle = getCellStyle();
		if( cellTypes != null && cellnum < cellTypes.length && cellTypes[cellnum] != HSSFCell.CELL_TYPE_BLANK )
			hssfCell.setCellType( cellTypes[cellnum] );
		if( cellStyle != null ) hssfCell.setCellStyle( cellStyle );

		cellnum++;

		return hssfCell;
	}

	private HSSFCell createCell( int colspan ) {
		if( colspan <= 1 ) return createCell();
		if( cellnum == -1 ) createRow();

		if( colspan > 1 )
			sheet.addMergedRegion( new CellRangeAddress(rownum, rownum, cellnum, cellnum + colspan - 1) );

		HSSFCellStyle defaultCellStyle = null;
		HSSFCellStyle[] cellStyles = null;
		switch( dataType ) {
		case TITLE:
			if( defaultCellStyles[STYLE_TITLE] != null ) {
				for( int c = cellnum; c < cellnum + colspan; c++ )
					HSSF.createCell( hssfRow, c ).setCellStyle( defaultCellStyles[STYLE_TITLE] );
			}
			break;
		case DATA:
			defaultCellStyle = defaultCellStyles[STYLE_DATA];
			cellStyles = dataCellStyles;
		case SUMMARY:
			defaultCellStyle = defaultCellStyles[STYLE_SUMMARY];
			cellStyles = summaryCellStyles;
		}

		if( defaultCellStyle != null || cellStyles != null ) {
			for( int c = cellnum; c < cellnum + colspan; c++ ) {
				int cellType = HSSFCell.CELL_TYPE_BLANK;
				HSSFCellStyle cellStyle = defaultCellStyle;

				if( cellTypes != null && c < cellTypes.length ) cellType = cellTypes[c];
				if( cellStyles != null && c < cellStyles.length && cellStyles[c] != null ) cellStyle = cellStyles[c];
				if( cellStyle != null || cellType != HSSFCell.CELL_TYPE_BLANK ) {
					HSSFCell hssfCell = HSSF.createCell( hssfRow, c );
					if( cellType != HSSFCell.CELL_TYPE_BLANK ) hssfCell.setCellType( cellTypes[cellnum] );
					if( cellStyle != null ) hssfCell.setCellStyle( cellStyle );
				}
			}
		}

		HSSFCell hssfCell = HSSF.createCell( hssfRow, cellnum );
		cellnum += colspan;

		return hssfCell;
	}

	private HSSFRow createRow() {
		hssfRow = HSSF.createRow( sheet, ++rownum );
		cellnum = 0;

		return hssfRow;
	}

	public void flush() {}

	public char getDataType() {
		return dataType;
	}

	public HSSFSheet getHSSFSheet() {
		return sheet;
	}

	public HSSFWorkbook getHSSFWorkbook() {
		return workbook;
	}

	/**
	 * 출력한 Line수를 return.
	 */
	public int getLineNumber() {
		return ( rownum + 1 );
	}

	private HSSFCellStyle getCellStyle() {
		HSSFCellStyle cellStyle;

		switch( dataType ) {
		case TITLE:
			return defaultCellStyles[STYLE_TITLE];
		case DATA:
			cellStyle = defaultCellStyles[STYLE_DATA];
			if( dataCellStyles != null && cellnum < dataCellStyles.length && dataCellStyles[cellnum] != null )
				cellStyle = dataCellStyles[cellnum];

			return cellStyle;
		case SUMMARY:
			cellStyle = defaultCellStyles[STYLE_SUMMARY];
			if( summaryCellStyles != null && cellnum < summaryCellStyles.length && summaryCellStyles[cellnum] != null )
				cellStyle = summaryCellStyles[cellnum];

			return cellStyle;
		default:
			return null;
		}
	}

	public void print( double d ) {
		createCell().setCellValue( d );
	}

	public void print( float f ) {
		createCell().setCellValue( (double)f );
	}

	public void print( int i ) {
		createCell().setCellValue( (double)i );
	}

	public void print( long l ) {
		createCell().setCellValue( (double)l );
	}

	public void print( Object o ) {
		if( o == null )
			printNull();
		else if( o instanceof Number )
			print( ((Number)o).doubleValue() );
		else
			print( o.toString() );
	}

	public void print( Object o, int colspan ) {
		if( o == null )
			printNull( colspan );
		else {
			HSSFCell hssfCell = createCell( colspan );
			if( o instanceof Number )
				hssfCell.setCellValue( ((Number)o).doubleValue() );
			else
				hssfCell.setCellValue( new HSSFRichTextString(o.toString()) );
		}
	}

	public void print( String s ) {
		if( s == null )
			printNull();
		else {
			HSSFCell hssfCell = createCell();
			hssfCell.setCellValue( new HSSFRichTextString(s) );
		}
	}

	public void print( String s, int colspan ) {
		if( s == null )
			printNull( colspan );
		else {
			HSSFCell hssfCell = createCell( colspan );
			hssfCell.setCellValue( new HSSFRichTextString(s) );
		}
	}

	public void print( String... ss ) {
		for( int i = 0; i < ss.length; i++ ) {
			if( ss[i] == null )
				printNull();
			else {
				HSSFCell hssfCell = createCell();
				hssfCell.setCellValue( new HSSFRichTextString(ss[i]) );
			}
		}
	}

	public void println() {
		cellnum = -1;
	}

	public void println( String... ss ) {
		print( ss );
		println();
	}

	public boolean printBorder() {
		if( cellTypes == null ) return false;
		return printBorder( rownum + 1, cellTypes.length );
	}

	public boolean printBorder( int rows, int columns ) {
		if( rows <= 0 ) return false;

		HSSF.setBorder(
			workbook, sheet, new CellRangeAddress( 0, rows - 1, 0, columns - 1 )
			, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM
		);

		return true;
	}

	/**
	 * 빈문자를 출력.
	 */
	public void printNull() {
		if( cellnum == -1 )
			createRow();
		createCell().setCellValue( new HSSFRichTextString("") );
	}

	/**
	 * 빈문자를 nullcnt번 출력.
	 */
	public void printNull( int nullcnt ) {
		if( cellnum == -1 )
			createRow();
		for( int i = 0; i < nullcnt; i++ ) createCell().setCellValue( new HSSFRichTextString("") );
	}

	public void setCellStyles( HSSFCellStyle[] dataCellStyles ) {
		this.cellTypes = null;
		this.dataCellStyles = dataCellStyles;
		this.summaryCellStyles = null;
	}

	public void setCellStyles( HSSFCellStyle[] dataCellStyles, HSSFCellStyle[] summaryCellStyles ) {
		this.cellTypes = null;
		this.dataCellStyles = dataCellStyles;
		this.summaryCellStyles = summaryCellStyles;
	}

	public void setColumnList( ColumnList columnList ) {
		Column[] columns = columnList.getColumns();
		int[] cellTypes = new int[ columns.length ];
		HSSFCellStyle[] dataCellStyles = new HSSFCellStyle[ columns.length ];
		HSSFCellStyle[] summaryCellStyles = new HSSFCellStyle[ columns.length ];

		for( int c = 0; c < columns.length; c++ ) {
			HSSF.ColumnStyle columnStyle = (HSSF.ColumnStyle)columns[c].getColumnAttr();

			cellTypes[c] = HSSFCell.CELL_TYPE_BLANK;
			if( columnStyle != null ) {
				sheet.setColumnWidth( c, columnStyle.getWidth() );
				cellTypes[c] = columnStyle.getCellType();
				dataCellStyles[c] = HSSF.createCellStyle( workbook, columns[c], defaultCellStyles[STYLE_DATA] );
				summaryCellStyles[c] = HSSF.createCellStyle( workbook, columns[c], defaultCellStyles[STYLE_SUMMARY] );
			} else {
				dataCellStyles[c] = defaultCellStyles[STYLE_DATA];
				summaryCellStyles[c] = defaultCellStyles[STYLE_SUMMARY];
			}
		}
		this.cellTypes = cellTypes;
		this.dataCellStyles = dataCellStyles;
		this.summaryCellStyles = summaryCellStyles;
	}

	public void setDataType( char dataType ) {
		this.dataType = dataType;
	}

	public void setDefaultCellStyle( char dataType, HSSFCellStyle cellStyle ) {
		switch( dataType ) {
		case TITLE:
			defaultCellStyles[STYLE_TITLE] = cellStyle;
			break;
		case DATA:
			defaultCellStyles[STYLE_DATA] = cellStyle;
			break;
		case SUMMARY:
			defaultCellStyles[STYLE_SUMMARY] = cellStyle;
			break;
		}
	}

	public void write( OutputStream out ) throws IOException {
		workbook.write( out );
	}

	public String getFileType() {
		// TODO Auto-generated method stub
		return null;
	}
}
