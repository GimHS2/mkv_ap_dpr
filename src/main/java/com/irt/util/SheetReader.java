/*
 *	File Name:	SheetReader.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *  GimHS		2014/03/31		2.2.2	geEvaluator 추가, getStringValue() : Formula type 처리 logic 변경
 *  yjcha		2013/12/31		2.2.1	getLineString() 구현, getStringValue(): 숫자 TYPE 처리 시 BigDecimal 처리
 *	jbaek		2013/08/30		2.2.0	copy from map_pds. RBMWorkbook상속. rownum로직 변경.
 *
**/

package com.irt.util;

import com.irt.data.DataException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;

/**
 *
 */
public class SheetReader extends RBMWorkbook implements com.irt.data.DataReader {
	private final static int ROWNUM_HEADER				= 0;
	private final static int ROWNUM_DATA				= 1;

	private boolean trim;

	private int sheetIndex, rownum;
	private String[] lines;
	private FormulaEvaluator formulaEvaluator;

	public SheetReader( java.io.File file ) throws IOException {
		this.workbook = RBMWorkbook.createWorkbook( file );
		this.formulaEvaluator = this.workbook.getCreationHelper().createFormulaEvaluator();

		this.sheetIndex = -1;
		this.rownum = -1;
	}

	public SheetReader( java.io.InputStream inputStream ) throws IOException {
		this.workbook = RBMWorkbook.createWorkbook( inputStream );
		this.formulaEvaluator = this.workbook.getCreationHelper().createFormulaEvaluator();

		this.sheetIndex = -1;
		this.rownum = -1;
	}

	public SheetReader( Workbook workbook ) {
		this.workbook = workbook;
		this.formulaEvaluator = this.workbook.getCreationHelper().createFormulaEvaluator();

		this.sheetIndex = -1;
		this.rownum = -1;
	}

	public void close() {
	}

	public int getLineNumber() {
		return ( rownum + 1 );
	}

	public String[] getLines() {
		return lines;
	}

	public String getLineString() {
		if( lines == null )
			return null;
		return getLineString( RBMWorkbook.DEFAULT_DELIM );
	}

	public String getLineString( char delim ) {
		if( lines == null )
			return null;

		StringBuffer sbuf = new StringBuffer();
		for( int i = 0; i < lines.length; i++ ) {
			if( i != 0 ) sbuf.append( delim );
			sbuf.append( lines[i] );
		}

		return sbuf.toString();
	}

	public String getSheetName() {
		if( sheetIndex >= 0 && sheetIndex < workbook.getNumberOfSheets() )
			return workbook.getSheetName( sheetIndex );
		else
			return null;
	}


	/**poi's DataFormatter example */
	private String getStringValueByFormatter( Row ssRow, int cellnum ) {
		Cell ssCell = ssRow.getCell( cellnum );
		if( ssCell == null ) return null;

		return new DataFormatter().formatCellValue( ssCell, formulaEvaluator );
	}

	/**use custom method */
	private String getStringValue( int cellType, Cell ssCell ) {
		switch( cellType ) {
		case Cell.CELL_TYPE_NUMERIC:
			if( org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(ssCell)
					&& org.apache.poi.ss.usermodel.DateUtil.isValidExcelDate(ssCell.getNumericCellValue()) ) {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(RBMWorkbook.DEFAULT_DATEPATTERN);
				return sdf.format(org.apache.poi.ss.usermodel.DateUtil.getJavaDate(ssCell.getNumericCellValue()) );
			} else if( ssCell.getNumericCellValue() == (int)ssCell.getNumericCellValue() )
				return String.valueOf( (int)ssCell.getNumericCellValue() );
			else {
				BigDecimal bd = new BigDecimal( ssCell.getNumericCellValue() );
				return String.valueOf( bd );
			}
		case Cell.CELL_TYPE_BLANK:
		case Cell.CELL_TYPE_ERROR:
			return null;
		default:
			try {
				RichTextString textString = ssCell.getRichStringCellValue();
				if( textString == null ) return null;

				String stringValue = textString.getString();
				if( trim && stringValue != null ) stringValue = stringValue.trim();

				return stringValue;
			} catch( IllegalStateException stateEx ) {
				return null;
			}
		}
	}

	private String getStringValue( Row ssRow, int cellnum ) {
		Cell ssCell = ssRow.getCell( cellnum );
		if( ssCell == null ) return null;

		if( ssCell.getCellType() == Cell.CELL_TYPE_FORMULA )
			return getStringValue( ssCell.getCachedFormulaResultType(), ssCell );
		else
			return getStringValue( ssCell.getCellType(), ssCell );
	}

	public boolean isBlankLine() {
		if( lines == null ) return true;

		for( int i = 0; i < lines.length; i++ ) {
			if( lines[i] != null && lines[i].length() > 0 )
				return false;
		}

		return true;
	}

	public boolean isEOF() {
		return( sheetIndex >= workbook.getNumberOfSheets() );
	}

	public Map<String, Object> readNext( String[] keys ) throws DataException {
		if( readNext(keys.length) == null ) return null;

		Map<String, Object> map = new java.util.HashMap<String, Object>( keys.length );
		for( int i = 0; i < keys.length && i < lines.length; i++ )
			if( lines[i] != null && lines[i].length() > 0 )
				map.put( keys[i], lines[i] );

		return map;
	}

	public String[] readNext() throws DataException {
		return readNext( getMaxColumnSize() );
	}

	private String[] readNext( int colsize ) throws DataException {
		if( sheetIndex >= workbook.getNumberOfSheets() ) return null;

		lines = null;
		if( sheet == null || ++rownum < ROWNUM_DATA ) {
			while( !readNextSheet() ) {
				if( sheetIndex >= workbook.getNumberOfSheets() ) return null;
			}
		}

		Row ssRow = sheet.getRow( rownum );
		if( ssRow == null ) {
			if( rownum > sheet.getLastRowNum() ) sheet = null;
			return readNext( colsize );
		}
		if( colsize > ssRow.getLastCellNum() ) colsize = ssRow.getLastCellNum() + 1;

		lines = new String[ colsize ];
		for( int c = 0; c < colsize; c++ )
			lines[c] = getStringValue( ssRow, c );

		return lines;
	}

	public boolean readNextSheet() {
		rownum = -1;
		if( workbook.getNumberOfSheets() > ++sheetIndex )
			sheet = workbook.getSheetAt( sheetIndex );
		else {
			sheet = null;
			return false;
		}

		rownum = rownum + ROWNUM_DATA;

		return true;
	}

	public void setTrim( boolean trim ) {
		this.trim = trim;
	}
}
