/*
 *	File Name:	HSSF.java
 *	Version:	2.2.4
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2009/06/30		2.2.4	getColumnName() 추가
 *										setBorder() 로직변경
 *	stghr12		2009/01/31		2.2.3	POI 3.2에 맞게 수정
 *	stghr12		2008/06/30		2.2.2	createCell(sheet, rownum, colnum, cellValue, cellStyle)등 추가
 *										createFont() 추가
 *	stghr12		2008/05/31		2.2.1	HSSFDataFormat.getBuiltinFormat() -> workbook.createDataFormat().getFormat()
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	create
 *
**/

package com.irt.util;

import com.irt.data.cols.Column;
import java.util.Map;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.CellRangeAddress;

/**
 *
 */
public class HSSF {
	Map<String, HSSFCellStyle> cellStyleMap;

	public static HSSFCell createCell( HSSFRow hssfRow, int colnum ) {
		HSSFCell hssfCell = hssfRow.getCell( colnum );
		if( hssfCell != null ) return hssfCell;

		return hssfRow.createCell( colnum );
	}

	public static HSSFCell createCell( HSSFSheet sheet, int rownum, int colnum ) {
		HSSFRow hssfRow = sheet.getRow( rownum );
		if( hssfRow != null ) {
			HSSFCell hssfCell = hssfRow.getCell( colnum );
			if( hssfCell != null ) return hssfCell;

			return hssfRow.createCell( colnum );
		}

		return sheet.createRow( rownum ).createCell( colnum );
	}

	public static HSSFCell createCell( HSSFSheet sheet, int rownum, int colnum, Object cellValue, HSSFCellStyle cellStyle ) {
		return createCell( HSSF.createRow(sheet, rownum), colnum, cellValue, cellStyle );
	}

	public static HSSFCell createCell( HSSFRow hssfRow, int colnum, Object cellValue, HSSFCellStyle cellStyle ) {
		HSSFCell hssfCell = HSSF.createCell( hssfRow, colnum );

		if( cellStyle != null ) hssfCell.setCellStyle( cellStyle );
		if( cellValue != null ) {
			if( cellValue instanceof Number )
				hssfCell.setCellValue( ((Number)cellValue).doubleValue() );
			else if( cellValue instanceof java.util.Date )
				hssfCell.setCellValue( (java.util.Date)cellValue );
			else {
				if( cellValue instanceof String )
					hssfCell.setCellValue( new HSSFRichTextString((String)cellValue) );
				else
					hssfCell.setCellValue( new HSSFRichTextString(cellValue.toString()) );
			}
		}

		return hssfCell;
	}

	public static HSSFCell createCell( HSSFSheet sheet, int rownum, int colnum, Object cellValue, HSSFCellStyle cellStyle, int rows, int cols ) {
		if( cols > 1 || rows > 1 ) {
			if( cols == 0 ) cols = 1;
			if( rows == 0 ) rows = 1;
			if( cellStyle != null ) {
				for( int row = rownum; row < rownum + rows; row++ ) {
					HSSFRow hssfRow = HSSF.createRow( sheet, row );
					for( int col = colnum; col < colnum + cols; col++ )
						HSSF.createCell(hssfRow, col).setCellStyle( cellStyle );
				}
			}
			sheet.addMergedRegion( new CellRangeAddress(rownum, rownum + rows - 1, colnum, colnum + cols - 1) );
		}

		return createCell( HSSF.createRow(sheet, rownum), colnum, cellValue, cellStyle );
	}

	public static HSSFCellStyle createCellStyle( HSSFWorkbook workbook, HSSFCellStyle cellStyle ) {
		HSSFCellStyle cellStyle_new = workbook.createCellStyle();
		if( cellStyle != null ) {
			cellStyle_new.setAlignment( cellStyle.getAlignment() );
			cellStyle_new.setBorderBottom( cellStyle.getBorderBottom() );
			cellStyle_new.setBorderLeft( cellStyle.getBorderLeft() );
			cellStyle_new.setBorderRight( cellStyle.getBorderRight() );
			cellStyle_new.setBorderTop( cellStyle.getBorderTop() );
			cellStyle_new.setBottomBorderColor( cellStyle.getBottomBorderColor() );
			cellStyle_new.setDataFormat( cellStyle.getDataFormat() );
			cellStyle_new.setFillBackgroundColor( cellStyle.getFillBackgroundColor() );
			cellStyle_new.setFillForegroundColor( cellStyle.getFillForegroundColor() );
			cellStyle_new.setFillPattern( cellStyle.getFillPattern() );
			cellStyle_new.setFont( workbook.getFontAt(cellStyle.getFontIndex()) );
			cellStyle_new.setHidden( cellStyle.getHidden() );
			cellStyle_new.setIndention( cellStyle.getIndention() );
			cellStyle_new.setLeftBorderColor( cellStyle.getLeftBorderColor() );
			cellStyle_new.setLocked( cellStyle.getLocked() );
			cellStyle_new.setRightBorderColor( cellStyle.getRightBorderColor() );
			cellStyle_new.setRotation( cellStyle.getRotation() );
			cellStyle_new.setTopBorderColor( cellStyle.getTopBorderColor() );
			cellStyle_new.setVerticalAlignment( cellStyle.getVerticalAlignment() );
			cellStyle_new.setWrapText( cellStyle.getWrapText() );
		}

		return cellStyle_new;
	}

	public static HSSFCellStyle createCellStyle( HSSFWorkbook workbook, Column column ) {
		return createCellStyle( workbook, column, null );
	}

	public static HSSFCellStyle createCellStyle( HSSFWorkbook workbook, Column column, HSSFCellStyle cellStyle ) {
		cellStyle = createCellStyle( workbook, cellStyle );

		try {
			HSSF.ColumnStyle columnStyle = (HSSF.ColumnStyle)column.getColumnAttr();
			if( columnStyle != null ) {
				if( columnStyle.alignment >= 0 ) cellStyle.setAlignment( columnStyle.alignment );
				if( columnStyle.dataFormat != null ) cellStyle.setDataFormat( workbook.createDataFormat().getFormat(columnStyle.dataFormat) );
			}
		} catch( ClassCastException castEx ) {}

		return cellStyle;
	}

	public static HSSFFont createFont( HSSFWorkbook workbook, HSSFFont hssfFont ) {
		HSSFFont hssfFont_new = workbook.createFont();

		hssfFont_new.setBoldweight( hssfFont.getBoldweight() );
		hssfFont_new.setColor( hssfFont.getColor() );
		hssfFont_new.setFontHeight( hssfFont.getFontHeight() );
		hssfFont_new.setFontHeightInPoints( hssfFont.getFontHeightInPoints() );
		hssfFont_new.setFontName( hssfFont.getFontName() );
		hssfFont_new.setItalic( hssfFont.getItalic() );
		hssfFont_new.setStrikeout( hssfFont.getStrikeout() );
		hssfFont_new.setTypeOffset( hssfFont.getTypeOffset() );
		hssfFont_new.setUnderline( hssfFont.getUnderline() );

		return hssfFont_new;
	}

	public static HSSFRow createRow( HSSFSheet sheet, int rownum ) {
		HSSFRow hssfRow = sheet.getRow( rownum );
		if( hssfRow != null ) return hssfRow;

		return sheet.createRow( rownum );
	}

	private static HSSFCellStyle getCellStyleBorder( HSSFWorkbook workbook, Map<String, HSSFCellStyle> cellStyleMap, HSSFCellStyle cellStyle
						, short borderLeft, short borderRight, short borderTop, short borderBottom ) {
		if( cellStyle.getBorderLeft() == borderLeft ) borderLeft = HSSFCellStyle.BORDER_NONE;
		if( cellStyle.getBorderRight() == borderRight ) borderRight = HSSFCellStyle.BORDER_NONE;
		if( cellStyle.getBorderTop() == borderTop ) borderTop = HSSFCellStyle.BORDER_NONE;
		if( cellStyle.getBorderBottom() == borderBottom ) borderBottom = HSSFCellStyle.BORDER_NONE;

		if( borderLeft == HSSFCellStyle.BORDER_NONE && borderRight == HSSFCellStyle.BORDER_NONE
				&& borderTop == HSSFCellStyle.BORDER_NONE && borderBottom == HSSFCellStyle.BORDER_NONE )
			return cellStyle;

		String key = cellStyle.getIndex() +"_"+ borderLeft +"_"+ borderRight +"_"+ borderTop +"_"+ borderBottom;
		HSSFCellStyle cellStyle_new = cellStyleMap.get( key );
		if( cellStyle_new == null ) {
			cellStyle_new = HSSF.createCellStyle( workbook, cellStyle );
			if( borderLeft != HSSFCellStyle.BORDER_NONE ) cellStyle_new.setBorderLeft( borderLeft );
			if( borderRight != HSSFCellStyle.BORDER_NONE ) cellStyle_new.setBorderRight( borderRight );
			if( borderTop != HSSFCellStyle.BORDER_NONE ) cellStyle_new.setBorderTop( borderTop );
			if( borderBottom != HSSFCellStyle.BORDER_NONE ) cellStyle_new.setBorderBottom( borderBottom );

			cellStyleMap.put( key, cellStyle_new );
		}

		return cellStyle_new;
	}

	public static String getColumnName( int column ) {
		if( column < 26 )
			return String.valueOf( (char)('A' + column) );
		else
			return String.valueOf( (char)('A' + column/26 - 1) ) + String.valueOf( (char)('A' + column%26) );
	}

	public static HSSF.ColumnStyle getColumnStyle( String columnAttr ) {
		return HSSF.ColumnStyle.getInstance( columnAttr );
	}

	public static void setBorder( HSSFWorkbook workbook, HSSFSheet sheet, CellRangeAddress cellRangeAddress
						, short borderLeft, short borderRight, short borderTop, short borderBottom ) {
		Map<String, HSSFCellStyle> cellStyleMap = new java.util.HashMap<String, HSSFCellStyle>();

		int firstCol = cellRangeAddress.getFirstColumn();
		int lastCol = cellRangeAddress.getLastColumn();
		int firstRow = cellRangeAddress.getFirstRow();
		int lastRow = cellRangeAddress.getLastRow();

		if( borderLeft != HSSFCellStyle.BORDER_NONE && ( firstCol != lastCol || borderRight == HSSFCellStyle.BORDER_NONE ) ) {
			for( int row = firstRow; row <= lastRow; row++ ) {
				HSSFCell hssfCell = HSSF.createCell( sheet, row, firstCol );
				HSSFCellStyle cellStyle = getCellStyleBorder(
					workbook, cellStyleMap
					, hssfCell.getCellStyle()
					, borderLeft
					, HSSFCellStyle.BORDER_NONE
					, row == firstRow ? borderTop : HSSFCellStyle.BORDER_NONE
					, row == lastRow ? borderBottom : HSSFCellStyle.BORDER_NONE
				);
				hssfCell.setCellStyle( cellStyle );
			}
		}

		if( borderRight != HSSFCellStyle.BORDER_NONE ) {
			if( firstCol != lastCol ) borderLeft = HSSFCellStyle.BORDER_NONE;
			for( int row = firstRow; row <= lastRow; row++ ) {
				HSSFCell hssfCell = HSSF.createCell( sheet, row, lastCol );
				HSSFCellStyle cellStyle = getCellStyleBorder(
					workbook, cellStyleMap
					, hssfCell.getCellStyle()
					, borderLeft
					, borderRight
					, row == firstRow ? borderTop : HSSFCellStyle.BORDER_NONE
					, row == lastRow ? borderBottom : HSSFCellStyle.BORDER_NONE
				);
				hssfCell.setCellStyle( cellStyle );
			}
		}

		if( borderTop != HSSFCellStyle.BORDER_NONE && (firstRow != lastRow || borderBottom == HSSFCellStyle.BORDER_NONE) ) {
			HSSFRow hssfRow = HSSF.createRow( sheet, firstRow );
			for( int col = firstCol; col <= lastCol; col++ ) {
				HSSFCell hssfCell = HSSF.createCell( hssfRow, col );
				HSSFCellStyle cellStyle = getCellStyleBorder(
					workbook, cellStyleMap
					, hssfCell.getCellStyle(), HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, borderTop, HSSFCellStyle.BORDER_NONE
				);
				hssfCell.setCellStyle( cellStyle );
			}
		}

		if( borderBottom != HSSFCellStyle.BORDER_NONE ) {
			HSSFRow hssfRow = HSSF.createRow( sheet, lastRow );
			if( firstRow != lastRow ) borderTop = HSSFCellStyle.BORDER_NONE;
			for( int col = firstCol; col <= lastCol; col++ ) {
				HSSFCell hssfCell = HSSF.createCell( hssfRow, col );
				HSSFCellStyle cellStyle = getCellStyleBorder(
					workbook, cellStyleMap
					, hssfCell.getCellStyle(), HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, borderTop, borderBottom
				);
				hssfCell.setCellStyle( cellStyle );
			}
		}
	}

	/**
	 *
	 */
	public static class ColumnStyle {
		private static Map<String, HSSF.ColumnStyle> instanceMap
				= java.util.Collections.synchronizedMap( new java.util.WeakHashMap<String, HSSF.ColumnStyle>(50) );

		int cellType;
		short alignment, width;
		String dataFormat;

		private ColumnStyle( int cellType, short alignment, short width, String dataFormat ) {
			this.cellType = cellType;
			this.alignment = alignment;
			this.width = width;
			this.dataFormat = dataFormat;
		}

		private static String extractValue( String attribute, String key ) {
			int idx = attribute.indexOf( key+"=" );
			if( idx < 0 ) return null;

			idx += key.length() + 1;
			if( idx < attribute.length() ) {
				char ch = attribute.charAt(idx);
				if( ch == '\'' || ch == '"' ) {
					int idx2 = attribute.indexOf( ch, ++idx );
					if( idx2 < 0 ) return null;

					return attribute.substring( idx, idx2 );
				}
			}

			return null;
		}

		public short getAlignment() {
			return alignment;
		}

		public int getCellType() {
			return cellType;
		}

		public String getDataFormat() {
			return dataFormat;
		}

		private static HSSF.ColumnStyle getInstance( String columnAttr ) {
			if( columnAttr == null ) return null;

			HSSF.ColumnStyle columnStyle = instanceMap.get( columnAttr );
			if( columnStyle != null ) return columnStyle;

			int cellType = HSSFCell.CELL_TYPE_BLANK;
			short width = -1;
			short alignment = -1;
			String dataFormat = null;

			columnAttr = columnAttr.toLowerCase();
			String value = extractValue( columnAttr, "type" );
			if( value != null ) {
				if( "boolean".equals(value) || "bool".equals(value) )
					cellType = HSSFCell.CELL_TYPE_BOOLEAN;
				else if( "formula".equals(value) )
					cellType = HSSFCell.CELL_TYPE_FORMULA;
				else if( "numeric".equals(value) || "number".equals(value) )
					cellType = HSSFCell.CELL_TYPE_NUMERIC;
				else if( "string".equals(value) || "text".equals(value) )
					cellType = HSSFCell.CELL_TYPE_STRING;
			}

			value = extractValue( columnAttr, "align" );
			if( value != null ) {
				if( "left".equals(value) )
					alignment = HSSFCellStyle.ALIGN_LEFT;
				else if( "right".equals(value) )
					alignment = HSSFCellStyle.ALIGN_RIGHT;
				else if( "center".equals(value) )
					alignment = HSSFCellStyle.ALIGN_CENTER;
			}

			try {
				value = extractValue( columnAttr, "width" );
				if( value != null ) width = Short.parseShort( value );
			} catch( NumberFormatException numEx ) {}

			dataFormat = extractValue( columnAttr, "format" );
			if( alignment < 0 && width < 0 && dataFormat == null ) return null;

			columnStyle = new HSSF.ColumnStyle( cellType, alignment, width, dataFormat );
			instanceMap.put( columnAttr, columnStyle );

			return columnStyle;
		}

		public short getWidth() {
			return width;
		}
	}
}
