/*
 *	File Name:	SSDataWriter.java
 *	Version:	2.2.2c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2016/02/29		2.2.3	print(java.sql.Date) 함수 추가
 *										print(), printNull() 함수 파라미터에 SS.ColumnStyle을 받을 수 있도록 수정
 *										default Cell style 변경
 *	jbaek		2019/07/30		2.2.2c	setColumnList(): 여러개 sheet 같은 형태일 경우 reuseCellStyle 사용하용 가능하도록 기능 추가
 *										( 관련 error: The maximum number of cell styles was exceeded.. )
 *	jbaek		2017/02/28		2.2.2c	multiple sheet 엑셀 파일 생성 기능 추가
 *	jbaek		2014/11/30		2.2.2c	STYLE_FIELDHEADER, STYLE_FIELDDATA 추가
 *	GimHS		2013/12/31		2.2.2	Cell style 적용 안되는 문제 수정, Formula 적용
 *	jbaek		2013/08/30		2.2.1	RBMWorkbook상속. close(), flush() 적용
 *	jbaek		2013/02/28		2.2.0	create: POI 3.9에 맞게 수정( HSSFDataWriter.java -> SSDataWriter.java )
 *
**/

package com.irt.util;

import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;

import java.io.IOException;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 */
public class SSDataWriter extends com.irt.util.RBMWorkbook implements com.irt.data.DataWriter, com.irt.util.OptionCellStyleReusable {
	private final static int STYLE_TITLE				= 0;
	private final static int STYLE_DATA					= 1;
	private final static int STYLE_SUMMARY				= 2;
	private final static int STYLE_FIELDHEADER			= 3;
	private final static int STYLE_FIELDDATA			= 4;


	Row ssRow;
	CellStyle[] defaultCellStyles, dataCellStyles, summaryCellStyles;
	int[] cellTypes;
	boolean reuseCellStyles;

	char dataType = DATA;
	int rownum, cellnum;

	boolean isMultiSheet;
	java.io.OutputStream out;

	public SSDataWriter( java.io.OutputStream out, Workbook workbook ) {
		this( out, workbook, null );
	}

	public SSDataWriter( java.io.OutputStream out, Workbook workbook, Sheet sheet ) {
		this.out = out;
		this.workbook = workbook;
		if( sheet == null ) {
			if( this.workbook.getSheet( DEFAULT_SHEETNAME ) != null )
				this.sheet = this.workbook.getSheet( DEFAULT_SHEETNAME );
			else
				this.sheet = this.workbook.createSheet( DEFAULT_SHEETNAME );
		} else
			this.sheet = this.workbook.getSheet( sheet.getSheetName() );

		resetSheetGridIndex();
		setDefaultCellStyles();
	}

	public SSDataWriter( java.io.OutputStream out, Workbook workbook, boolean isMultiSheet ) {
		this.out = out;
		this.workbook = workbook;
		this.isMultiSheet = isMultiSheet;
		resetSheetGridIndex();
		setDefaultCellStyles();
	}

	@Override
	public void close() throws IOException {
		this.out.close();
	}

	private Cell createCell() {
		if( cellnum == -1 ) createRow();

		Cell ssCell = SS.createCell( ssRow, cellnum );
		CellStyle cellStyle = getCellStyle();
		if( cellTypes != null && cellnum < cellTypes.length && cellTypes[cellnum] != Cell.CELL_TYPE_BLANK )
			ssCell.setCellType( cellTypes[cellnum] );
		if( cellStyle != null ) ssCell.setCellStyle( cellStyle );

		cellnum++;

		return ssCell;
	}

	private Cell createCell( int colspan ) {
		return createCell( colspan, 1 );
	}

	private Cell createCell( int colspan, int rowspan ) {
		if( colspan <= 1 && rowspan <= 1 ) return createCell();
		if( cellnum == -1 ) createRow();

		if( colspan > 1 || rowspan > 1 )
			sheet.addMergedRegion( new CellRangeAddress(rownum, rownum + rowspan - 1, cellnum, cellnum + colspan - 1) );

		CellStyle defaultCellStyle = null;
		CellStyle[] cellStyles = null;
		switch( dataType ) {
		case FIELDHEADER:
			if( defaultCellStyles[STYLE_FIELDHEADER] != null ) {
				for( int c = cellnum; c < cellnum + colspan; c++ )
					SS.createCell( ssRow, c ).setCellStyle( defaultCellStyles[STYLE_FIELDHEADER] );
			}
			break;
		case FIELDDATA:
			if( defaultCellStyles[STYLE_FIELDDATA] != null ) {
				for( int c = cellnum; c < cellnum + colspan; c++ )
					SS.createCell( ssRow, c ).setCellStyle( defaultCellStyles[STYLE_FIELDDATA] );
			}
			break;
		case TITLE:
			if( defaultCellStyles[STYLE_TITLE] != null ) {
				for( int c = cellnum; c < cellnum + colspan; c++ )
					SS.createCell( ssRow, c ).setCellStyle( defaultCellStyles[STYLE_TITLE] );
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
					Cell ssCell = (Cell) SS.createCell( ssRow, c );
					if( cellType != Cell.CELL_TYPE_BLANK ) ssCell.setCellType( cellTypes[cellnum] );
					if( cellStyle != null ) ssCell.setCellStyle( cellStyle );
				}
			}
		}

		Cell ssCell = (Cell) SS.createCell( ssRow, cellnum );
		cellnum += colspan;

		return ssCell;
	}

	private Row createRow() {
		ssRow = SS.createRow( sheet, ++rownum );
		cellnum = 0;

		return ssRow;
	}

	@Override
	public void flush() throws IOException {
		flushOut( this, out );
	}

	protected java.io.OutputStream getOutputStream() {
		return out;
	}

	@Override
	public boolean getReuseCellStyles() {
		return reuseCellStyles;
	}

	@Override
	public char getDataType() {
		return dataType;
	}

	@Override
	public String getFileType() {
		return RBMWorkbook.getFileType( workbook );
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
		case FIELDHEADER:
			return defaultCellStyles[STYLE_FIELDHEADER];
		case FIELDDATA:
			return defaultCellStyles[STYLE_FIELDDATA];
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

	public boolean isMultiSheet() {
		return isMultiSheet;
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

	public void print( java.sql.Date d ) {
		createCell().setCellValue( d );
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

	public void print( Object o, int colspan, SS.ColumnStyle columnStyle ) {
		print( o, colspan, 1, columnStyle );
	}

	public void print( Object o, int colspan, int rowspan ) {
		if( o == null ) {
			// FIELD 타입일 경우에는 colspan만큼 cell merge 처리가 되도록 "" 삽입. 즉 외곽선만 찍히도록.
			if( dataType == FIELDDATA || dataType == FIELDHEADER ) {
				Cell ssCell = createCell( colspan, rowspan );
				ssCell.setCellValue( "" );
			} else {
				printNull( colspan, rowspan );
			}
		} else {
			Object cellValue = o;
			if( (dataType == DATA || dataType == SUMMARY)
					&& cellValue instanceof String
					&& cellTypes != null && cellTypes[cellnum] == Cell.CELL_TYPE_NUMERIC ) {
				try {
					cellValue = Double.valueOf( (String)cellValue );
				} catch( NumberFormatException numberEx ) {}
			}
			Cell ssCell = createCell( colspan, rowspan );

			RichTextString richString = null;
			if( cellValue != null ) {
				if( cellValue instanceof Number )
					ssCell.setCellValue( ((Number)cellValue).doubleValue() );
				else if( cellValue instanceof java.util.Date )
					ssCell.setCellValue( (java.util.Date)cellValue );
				else {
					if( cellValue instanceof String )
						richString = ssRow.getSheet().getWorkbook().getCreationHelper().createRichTextString( (String)cellValue );
					else
						richString = ssRow.getSheet().getWorkbook().getCreationHelper().createRichTextString( cellValue.toString() );
					ssCell.setCellValue( richString );
				}
			}
		}
	}

	public void print( Object o, int colspan, int rowspan, SS.ColumnStyle columnStyle ) {
		if( o == null || o.toString().length() == 0 )
			printNull( colspan, rowspan, columnStyle );
		else {
			Cell ssCell = createCell( colspan, rowspan );
			if( columnStyle != null )
				ssCell.setCellStyle( (CellStyle)SS.createCellStyle(workbook, ssCell.getCellStyle(), columnStyle) );

			if( ssCell.getCellType() == Cell.CELL_TYPE_FORMULA )
				ssCell.setCellFormula( o.toString() );
			else {
				if( o instanceof Number )
					ssCell.setCellValue( ((Number)o).doubleValue() );
				else if( o instanceof java.sql.Date )
					ssCell.setCellValue( (java.sql.Date)o );
				else
					ssCell.setCellValue( getRichTextString(o.toString()) );
			}
		}
	}

	@Override
	public void print( String s ) {
		if( s == null )
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

	public void print( String s, int colspan, SS.ColumnStyle columnStyle ) {
		print( s, colspan, 1, columnStyle );
	}

	public void print( String s, int colspan, int rowspan ) {
		if( s == null ) {
			// FIELD 타입일 경우에는 colspan만큼 cell merge 처리가 되도록 blank string 삽입
			if( dataType == FIELDDATA || dataType == FIELDHEADER ) {
				Cell ssCell = createCell( colspan, rowspan );
				ssCell.setCellValue( "" );
			} else {
				printNull( colspan, rowspan );
			}
		} else {
			Object cellValue = s;
			if( (dataType == DATA || dataType == SUMMARY)
					&& cellValue instanceof String
					&& cellTypes != null && cellTypes[cellnum] == Cell.CELL_TYPE_NUMERIC ) {
				try {
					cellValue = Double.valueOf( (String)cellValue );
				} catch( NumberFormatException numberEx ) {}
			}
			Cell ssCell = createCell( colspan, rowspan );

			if( cellValue != null ) {
				if( cellValue instanceof Number )
					ssCell.setCellValue( ((Number)cellValue).doubleValue() );
				else if( cellValue instanceof java.util.Date )
					ssCell.setCellValue( (java.util.Date)cellValue );
				else {
					RichTextString richString = null;
					if( cellValue instanceof String )
						richString = ssRow.getSheet().getWorkbook().getCreationHelper().createRichTextString( (String)cellValue );
					else
						richString = ssRow.getSheet().getWorkbook().getCreationHelper().createRichTextString( cellValue.toString() );
					ssCell.setCellValue( richString );
				}
			}
		}
	}

	public void print( String s, int colspan, int rowspan, SS.ColumnStyle columnStyle ) {
		if( s == null || s.length() == 0 )
			printNull( colspan, rowspan, columnStyle );
		else {
			Cell ssCell = createCell( colspan, rowspan );
			if( columnStyle != null )
				ssCell.setCellStyle( (CellStyle)SS.createCellStyle(workbook, ssCell.getCellStyle(), columnStyle) );

			if( ssCell.getCellType() == Cell.CELL_TYPE_FORMULA )
				ssCell.setCellFormula( s );
			else
				ssCell.setCellValue( getRichTextString(s) );
		}
	}

	@Override
	public void print( String... ss ) {
		for( int i = 0; i < ss.length; i++ ) {
			if( ss[i] == null )
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
		createRow();
	}

	@Override
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

		SS.setBorder(
			workbook, sheet, new CellRangeAddress( 0, rows - 1, 0, columns - 1 )
			, CellStyle.BORDER_MEDIUM, CellStyle.BORDER_MEDIUM, CellStyle.BORDER_MEDIUM, CellStyle.BORDER_MEDIUM
		);

		return true;
	}

	/**
	 * 빈문자를 출력. 다음셀로 이동.
	 */
	@Override
	public void printNull() {
		if( cellnum == -1 )
			createRow();
		createCell().setCellValue( "" );
	}

	/**
	 * 빈문자를 nullcnt번 출력.
	 */
	@Override
	public void printNull( int nullcnt ) {
		printNull( nullcnt, 1 );
	}

	/* "" 출력하므로, 스타일 적용됨. */
	public void printNull( int nullcnt, int rowspan ) {
		if( cellnum == -1 )
			createRow();
		for( int i = 0; i < nullcnt; i++ ) {
			createCell(1, rowspan).setCellValue( "" );
		}
	}

	public void printNull( int nullcnt, int rowspan, SS.ColumnStyle columnStyle ) {
		if( cellnum == -1 )
			createRow();
		for( int i = 0; i < nullcnt; i++ ) {
			Cell ssCell = createCell( 1, rowspan );
			if( columnStyle != null )
				ssCell.setCellStyle( (CellStyle)SS.createCellStyle(workbook, ssCell.getCellStyle(), columnStyle) );

			ssCell.setCellValue( getRichTextString("") );
		}
	}

	private void resetSheetGridIndex() {
		this.ssRow = null;
		this.rownum = this.cellnum = -1;
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

		if( reuseCellStyles ) {
			if( this.dataCellStyles == null ) this.dataCellStyles = dataCellStyles;
			if( this.summaryCellStyles == null ) this.summaryCellStyles = summaryCellStyles;
		}

		for( int c = 0; c < columns.length; c++ ) {
			SS.ColumnStyle columnStyle = null;
			if( columns[c].getColumnAttr() instanceof SS.ColumnStyle )
				columnStyle = (SS.ColumnStyle)columns[c].getColumnAttr();
			else if( columns[c].getColumnAttr() instanceof String )// SS.ColumnStyle없을시 생성.
				columnStyle = SS.getColumnStyle( (String) columns[c].getColumnAttr() );

			cellTypes[c] = Cell.CELL_TYPE_BLANK;
			if( columnStyle != null ) {
				sheet.setColumnWidth( c, columnStyle.getWidth()*50 );//web과 excel이 사이즈 단위가 다름. 임의로 50정도 곱함.
				cellTypes[c] = columnStyle.getCellType();
				dataCellStyles[c] = ( reuseCellStyles
						? ( (this.dataCellStyles[c] == null) ? SS.createCellStyle(workbook, columns[c], defaultCellStyles[STYLE_DATA]) : this.dataCellStyles[c] )
						: SS.createCellStyle(workbook, columns[c], defaultCellStyles[STYLE_DATA]) );
				summaryCellStyles[c] = ( reuseCellStyles
						? ( (this.summaryCellStyles[c] == null) ? SS.createCellStyle(workbook, columns[c], defaultCellStyles[STYLE_SUMMARY]) : this.summaryCellStyles[c] )
						: SS.createCellStyle(workbook, columns[c], defaultCellStyles[STYLE_SUMMARY]) );
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
		case FIELDHEADER:
			defaultCellStyles[STYLE_FIELDHEADER] = cellStyle;
			break;
		case FIELDDATA:
			defaultCellStyles[STYLE_FIELDDATA] = cellStyle;
			break;
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

	private void setDefaultCellStyles() {
		Font ssFont;

		this.defaultCellStyles = new CellStyle[5];
		this.defaultCellStyles[STYLE_TITLE] = this.workbook.createCellStyle();
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

		this.defaultCellStyles[STYLE_DATA] = this.workbook.createCellStyle();
		this.defaultCellStyles[STYLE_DATA].setVerticalAlignment( CellStyle.VERTICAL_CENTER );
		this.defaultCellStyles[STYLE_DATA].setBorderLeft( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_DATA].setBorderRight( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_DATA].setBorderTop( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_DATA].setBorderBottom( CellStyle.BORDER_THIN );

		this.defaultCellStyles[STYLE_SUMMARY] = this.workbook.createCellStyle();
		this.defaultCellStyles[STYLE_SUMMARY].setFillPattern( CellStyle.SOLID_FOREGROUND );
		this.defaultCellStyles[STYLE_SUMMARY].setFillForegroundColor( IndexedColors.GREY_25_PERCENT.getIndex() );
		this.defaultCellStyles[STYLE_SUMMARY].setVerticalAlignment( CellStyle.VERTICAL_CENTER );
		this.defaultCellStyles[STYLE_SUMMARY].setAlignment( CellStyle.ALIGN_CENTER );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderLeft( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderRight( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderTop( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_SUMMARY].setBorderBottom( CellStyle.BORDER_THIN );

		this.defaultCellStyles[STYLE_FIELDHEADER] = this.workbook.createCellStyle();
		this.defaultCellStyles[STYLE_FIELDHEADER].setFillPattern( CellStyle.SOLID_FOREGROUND );
		this.defaultCellStyles[STYLE_FIELDHEADER].setFillForegroundColor( IndexedColors.GREY_25_PERCENT.getIndex() );
		this.defaultCellStyles[STYLE_FIELDHEADER].setVerticalAlignment( CellStyle.VERTICAL_CENTER );
		this.defaultCellStyles[STYLE_FIELDHEADER].setAlignment( CellStyle.ALIGN_RIGHT );
		this.defaultCellStyles[STYLE_FIELDHEADER].setBorderLeft( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_FIELDHEADER].setBorderRight( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_FIELDHEADER].setBorderTop( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_FIELDHEADER].setBorderBottom( CellStyle.BORDER_THIN );

		this.defaultCellStyles[STYLE_FIELDDATA] = this.workbook.createCellStyle();
		this.defaultCellStyles[STYLE_FIELDDATA].setFillPattern( CellStyle.SOLID_FOREGROUND );
		this.defaultCellStyles[STYLE_FIELDDATA].setFillForegroundColor( IndexedColors.WHITE.getIndex() );
		this.defaultCellStyles[STYLE_FIELDDATA].setVerticalAlignment( CellStyle.VERTICAL_CENTER);
		this.defaultCellStyles[STYLE_FIELDDATA].setAlignment( CellStyle.ALIGN_LEFT );
		this.defaultCellStyles[STYLE_FIELDDATA].setBorderLeft( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_FIELDDATA].setBorderRight( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_FIELDDATA].setBorderTop( CellStyle.BORDER_THIN );
		this.defaultCellStyles[STYLE_FIELDDATA].setBorderBottom( CellStyle.BORDER_THIN );
	}

	@Override
	public void setReuseCellStyles( boolean reuseCellStyles ) {
		this.reuseCellStyles = reuseCellStyles;
	}

	public void setSheet( Sheet sheet ) {
		if(isMultiSheet) {
			resetSheetGridIndex();
			this.sheet = sheet;
		} else {
			this.sheet = sheet;
		}
	}

	public void setSheet( Sheet sheet, ColumnList columnList ) {
		setSheet(sheet);
		setColumnList(columnList);
	}
}
