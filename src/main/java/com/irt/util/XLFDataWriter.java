/*
 *	File Name:	XLFDataWriter.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2016/10/31		2.2.3	Java Document 주석 추가, 사용 안하는 import 삭제
 *	jbaek		2013/11/30		2.2.2	삭제예정.
 *	GimHS		2013/12/31		2.2.1	Cell style 적용 안되는 문제 수정, Formula 적용
 *	jbaek		2013/08/30		2.2.0	copy from SSDataWriter. RBMWorkbook상속. xlsx파일의 SAX Processing
**/

package com.irt.util;

import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;
import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 *
 */
public class XLFDataWriter extends com.irt.util.RBMWorkbook implements com.irt.data.DataWriter {
	private final static int STYLE_TITLE				= 0;
	private final static int STYLE_DATA					= 1;
	private final static int STYLE_SUMMARY				= 2;

	protected Row ssRow;
	CellStyle[] defaultCellStyles, dataCellStyles, summaryCellStyles;
	int[] cellTypes;

	char dataType = DATA;
	int rownum, cellnum;

	java.io.OutputStream out;

	public XLFDataWriter( java.io.OutputStream out, SXSSFWorkbook wb ) throws IOException {
		this( out, wb, null );
	}

	public XLFDataWriter( java.io.OutputStream out, SXSSFWorkbook wb, SXSSFSheet sh ) throws IOException {
		this.out = out;
		this.workbook = wb;
		if( sh == null ) {
			if( this.workbook.getSheet( DEFAULT_SHEETNAME ) != null )
				this.sheet = this.workbook.getSheet( DEFAULT_SHEETNAME );
			else
				this.sheet = this.workbook.createSheet( DEFAULT_SHEETNAME );
		} else
			this.sheet = this.workbook.getSheet( sh.getSheetName() );
		this.ssRow = null;
		this.rownum = this.cellnum = -1;

		Font ssFont;

		this.defaultCellStyles = new CellStyle[3];
		this.defaultCellStyles[STYLE_TITLE] = workbook.createCellStyle();
		this.defaultCellStyles[STYLE_TITLE].setFillPattern( CellStyle.SOLID_FOREGROUND );
		this.defaultCellStyles[STYLE_TITLE].setFillForegroundColor( IndexedColors.CORNFLOWER_BLUE.getIndex() );
		this.defaultCellStyles[STYLE_TITLE].setVerticalAlignment( CellStyle.VERTICAL_CENTER );
		this.defaultCellStyles[STYLE_TITLE].setAlignment( CellStyle.ALIGN_CENTER );
		this.defaultCellStyles[STYLE_TITLE].setWrapText( true );
		this.defaultCellStyles[STYLE_TITLE].setBorderLeft( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_TITLE].setBorderRight( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_TITLE].setBorderTop( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_TITLE].setBorderBottom( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_TITLE].setFont( ssFont = workbook.createFont() );
		ssFont.setBoldweight( Font.BOLDWEIGHT_BOLD );

		this.defaultCellStyles[STYLE_DATA] = workbook.createCellStyle();
		this.defaultCellStyles[STYLE_DATA].setVerticalAlignment( CellStyle.VERTICAL_CENTER );
		this.defaultCellStyles[STYLE_DATA].setBorderLeft( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_DATA].setBorderRight( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_DATA].setBorderTop( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_DATA].setBorderBottom( CellStyle.BORDER_THIN );

		this.defaultCellStyles[STYLE_SUMMARY] = workbook.createCellStyle();
		this.defaultCellStyles[STYLE_SUMMARY].setFillPattern( CellStyle.SOLID_FOREGROUND );
		this.defaultCellStyles[STYLE_SUMMARY].setFillForegroundColor( IndexedColors.GREY_25_PERCENT.getIndex() );
		this.defaultCellStyles[STYLE_SUMMARY].setVerticalAlignment( CellStyle.VERTICAL_CENTER );
		this.defaultCellStyles[STYLE_SUMMARY].setAlignment( CellStyle.ALIGN_CENTER );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderLeft( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderRight( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderTop( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderBottom( CellStyle.BORDER_THIN );
	}

	@Override
	public void close() throws IOException {
		this.out.close();
	}

	/*
	 * if cellnum == -1 rownum++
	 * cellnum++
	 */
	private Cell createCell() {
		if( cellnum == -1 ) createRow();

		Cell ssCell = ssRow.getCell( cellnum, Row.CREATE_NULL_AS_BLANK );
		CellStyle cellStyle = getCellStyle();
		if( cellTypes != null && cellnum < cellTypes.length && cellTypes[cellnum] != Cell.CELL_TYPE_BLANK )
			ssCell.setCellType( cellTypes[cellnum] );
		if( cellStyle != null ) ssCell.setCellStyle( cellStyle );

		cellnum++;

		return ssCell;
	}

	private Cell createCell( int colspan, int rowspan ) {
		if( colspan <= 1 && rowspan <= 1 ) return createCell();
		if( cellnum == -1 ) createRow();

		if( colspan > 1 || rowspan > 1 )
			ssRow.getSheet().addMergedRegion( new CellRangeAddress(rownum, rownum + rowspan - 1, cellnum, cellnum + colspan - 1) );

		Cell ssCell = null;
		CellStyle defaultCellStyle = null;
		CellStyle[] cellStyles = null;
		switch( dataType ) {
		case TITLE:
			if( defaultCellStyles[STYLE_TITLE] != null ) {
				for( int c = cellnum; c < cellnum + colspan; c++ )
					ssRow.getCell( c, Row.CREATE_NULL_AS_BLANK ).setCellStyle( defaultCellStyles[STYLE_TITLE] );
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
				int cellType = Cell.CELL_TYPE_BLANK;
				CellStyle cellStyle = defaultCellStyle;

				if( cellTypes != null && c < cellTypes.length ) cellType = cellTypes[c];
				if( cellStyles != null && c < cellStyles.length && cellStyles[c] != null ) cellStyle = cellStyles[c];
				if( cellStyle != null || cellType != Cell.CELL_TYPE_BLANK ) {
					ssCell = createCell();
					if( cellType != Cell.CELL_TYPE_BLANK ) ssCell.setCellType( cellTypes[cellnum] );
					if( cellStyle != null ) ssCell.setCellStyle( cellStyle );
				}
			}
		}

		ssCell = ssRow.getCell( cellnum, Row.CREATE_NULL_AS_BLANK );
		cellnum += colspan;

		return ssCell;
	}

	/*
	 * rownum = ++rownum
	 * cellnum = 0;
	 */
	private Row createRow() {
		ssRow = sheet.createRow( ++rownum );
		cellnum = 0;

		return ssRow;
	}

	@Override
	public void flush() throws IOException {
		flushOut( this, out );
	}

	@Override
	public char getDataType() {
		return dataType;
	}

	@Override
	public String getFileType() {
		return com.irt.util.FileType.XLF;
	}

	/**
	 * 출력한 Line수를 return.
	 */
	@Override
	public int getLineNumber() {
		return ( rownum + 1 );
	}

	private CellStyle getCellStyle() {
		CellStyle cellStyle;

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

	@Override
	public void print( double d ) {
		createCell().setCellValue( d );
	}

	@Override
	public void print( float f ) {
		createCell().setCellValue( f );
	}

	@Override
	public void print( int i ) {
		createCell().setCellValue( i );
	}

	@Override
	public void print( long l ) {
		createCell().setCellValue( l );
	}

	@Override
	public void print( Object o ) {
		if( o == null )
			printNull();
		else if( o instanceof Number )
			print( ((Number)o).doubleValue() );
		else
			print( o.toString() );
	}

	@Override
	public void print( Object o, int colspan ) {
		print( o, colspan, 1 );
	}

	public void print( Object o, int colspan, int rowspan ) {
		if( o == null || o.toString().length() == 0 )
			printNull( colspan, rowspan );
		else {
			Cell ssCell = createCell( colspan, rowspan );
			if( ssCell.getCellType() == Cell.CELL_TYPE_FORMULA )
				ssCell.setCellFormula( o.toString() );
			else {
				if( o instanceof Number )
					ssCell.setCellValue( ((Number)o).doubleValue() );
				else
					ssCell.setCellValue( getRichTextString(o.toString()) );
			}
		}
	}

	@Override
	public void print( String s ) {
		if( s == null || s.length() == 0 )
			printNull();
		else {
			Cell ssCell = createCell();
			if( ssCell.getCellType() == Cell.CELL_TYPE_FORMULA )
				ssCell.setCellFormula( s );
			else
				ssCell.setCellValue( getRichTextString(s) );
		}
	}

	@Override
	public void print( String s, int colspan ) {
		print( s, colspan, 1 );
	}

	public void print( String s, int colspan, int rowspan ) {
		if( s == null || s.length() == 0 )
			printNull( colspan, rowspan );
		else {
			Cell ssCell = createCell( colspan, rowspan );
			if( ssCell.getCellType() == Cell.CELL_TYPE_FORMULA )
				ssCell.setCellFormula( s );
			else
				ssCell.setCellValue( getRichTextString(s) );
		}
	}

	@Override
	public void print( String... ss ) {
		for( int i = 0; i < ss.length; i++ ) {
			if( ss[i] == null || ss[i].length() == 0 )
				printNull();
			else {
				Cell ssCell = createCell();
				if( ssCell.getCellType() == Cell.CELL_TYPE_FORMULA )
					ssCell.setCellFormula( ss[i] );
				else
					ssCell.setCellValue( getRichTextString(ss[i]) );
			}
		}
	}

	@Override
	public void println() {
		cellnum = -1;
	}

	@Override
	public void println( String... ss ) {
		print( ss );
		println();
	}

	/*
	 * TODO:TEST
	 */
	public boolean printBorder() {
		if( cellTypes == null ) return false;
		return printBorder( rownum + 1, cellTypes.length );
	}

	/*
	 * TODO:TEST
	 */
	public boolean printBorder( int rows, int columns ) {
		if( rows <= 0 ) return false;

		SS.setBorder(
			workbook, sheet, new CellRangeAddress( 0, rows - 1, 0, columns - 1 )
			, CellStyle.BORDER_MEDIUM, CellStyle.BORDER_MEDIUM, CellStyle.BORDER_MEDIUM, CellStyle.BORDER_MEDIUM
		);

		return true;
	}

	/**
	 * 빈문자를 출력.
	 */
	@Override
	public void printNull() {
		if( cellnum == -1 )
			createRow();
		createCell().setCellValue( workbook.getCreationHelper().createRichTextString("") );
	}

	/**
	 * 빈문자를 nullcnt번 출력.
	 */
	@Override
	public void printNull( int nullcnt ) {
		printNull( nullcnt, 1 );
	}

	public void printNull( int nullcnt, int rowspan ) {
		if( cellnum == -1 )
			createRow();
		for( int i = 0; i < nullcnt; i++ )
			createCell(1, rowspan).setCellValue( workbook.getCreationHelper().createRichTextString("") );
	}

	public void setCellnum( int cellnum ) {
		if( this.cellnum == -1 ) createRow();
		this.cellnum = cellnum;
	}

	public void setCellStyles( CellStyle[] dataCellStyles ) {
		this.cellTypes = null;
		this.dataCellStyles = dataCellStyles;
		this.summaryCellStyles = null;
	}

	public void setCellStyles( CellStyle[] dataCellStyles, CellStyle[] summaryCellStyles ) {
		this.cellTypes = null;
		this.dataCellStyles = dataCellStyles;
		this.summaryCellStyles = summaryCellStyles;
	}

	public void setColumnList( ColumnList columnList ) {
		Column[] columns = columnList.getColumns();
		int[] cellTypes = new int[ columns.length ];
		CellStyle[] dataCellStyles = new CellStyle[ columns.length ];
		CellStyle[] summaryCellStyles = new CellStyle[ columns.length ];

		com.irt.data.cols.ColumnGroup columnGroup = null;
		for( int c = 0; c < columns.length; c++ ) {
			SS.ColumnStyle columnStyle = null;
			if( columns[c].getColumnAttr() instanceof SS.ColumnStyle )
				columnStyle = (SS.ColumnStyle)columns[c].getColumnAttr();

			if( columns[c].getColumnGroup() == columnGroup ) columnGroup = columns[c].getColumnGroup();

			cellTypes[c] = Cell.CELL_TYPE_BLANK;
			if( columnStyle != null ) {
				sheet.setColumnWidth( c, columnStyle.getWidth() );
				cellTypes[c] = columnStyle.getCellType();
				dataCellStyles[c] = SS.createCellStyle( workbook, columns[c], defaultCellStyles[STYLE_DATA] );
				summaryCellStyles[c] = SS.createCellStyle( workbook, columns[c], defaultCellStyles[STYLE_SUMMARY] );
			} else {
				dataCellStyles[c] = defaultCellStyles[STYLE_DATA];
				summaryCellStyles[c] = defaultCellStyles[STYLE_SUMMARY];
			}
		}
		this.cellTypes = cellTypes;
		this.dataCellStyles = dataCellStyles;
		this.summaryCellStyles = summaryCellStyles;
	}

	@Override
	public void setDataType( char dataType ) {
		this.dataType = dataType;
	}

	public void setDefaultCellStyle( char dataType, CellStyle cellStyle ) {
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
}
