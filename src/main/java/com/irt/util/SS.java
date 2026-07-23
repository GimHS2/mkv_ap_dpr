/*
 *	File Name:	SS.java
 *	Version:	2.2.3c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/06/30		2.2.3c	getInstance(): cssDateStyle에서 dataFormat 추출.
 *	jbaek		2017/09/30		2.2.3c	createCell(): Hyperlink 구현
 *	jbaek		2017/02/28		2.2.3c	printFieldSet() method 구현.
 *	GimHS		2016/02/29		2.2.3	setBorder(): border의 color를 지정할 수 있도록 파라미터 추가
 *										createCellStyle(): columnStyle에 속성이 없을 때 Default style으로 셋팅
 *										ColumnSytle.setForegroundColor() 추가
 *	GimHS		2014/03/31		2.2.2	columnAttr에 "fgcolor" 속성 추가 (foregroundColor 적용)
 *	jbaek		2013/11/30		2.2.1	createCell(): RichTextString 가져오는 방식 변경.
 *	jbaek		2013/02/28		2.2.0	create: POI 3.9에 맞게 수정( HSSF.java -> SS.java )
 *
**/

package com.irt.util;

import com.irt.data.AbstractFieldSet;
import com.irt.data.DataWriter;
import com.irt.data.cols.Column;
import com.irt.data.format.PatternRecordFormat;

import java.io.IOException;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;


/**
 *
 */
public class SS {
	private static final double POI_DATETIME_CHAR_MULTI		= 5.5;
	private static final String POI_DATETIME_YMD			= "yyyy-mm-dd";
	private static final String POI_DATETIME_YMDH			= "yyyy-mm-dd hh";
	private static final String POI_DATETIME_YMDHI			= "yyyy-mm-dd hh:mm";
	private static final String POI_DATETIME_YMDHIS			= "yyyy-mm-dd hh:mm:ss";

	Map<String, CellStyle> cellStyleMap;

	public static Cell createCell( Row ssRow, int colnum ) {
		Cell ssCell = ssRow.getCell( colnum );
		if( ssCell != null ) return ssCell;

		return ssRow.createCell( colnum );
	}

	public static Cell createCell( Sheet sheet, int rownum, int colnum ) {
		Row ssRow = sheet.getRow( rownum );
		if( ssRow != null ) {
			Cell ssCell = ssRow.getCell( colnum );
			if( ssCell != null ) return ssCell;

			return ssRow.createCell( colnum );
		}

		return sheet.createRow( rownum ).createCell( colnum );
	}

	public static Cell createCell( Sheet sheet, int rownum, int colnum, Object cellValue, CellStyle cellStyle ) {
		return createCell( SS.createRow(sheet, rownum), colnum, cellValue, cellStyle );
	}

	public static Cell createCell( Row ssRow, int colnum, Object cellValue, CellStyle cellStyle ) {
		Cell ssCell = SS.createCell( ssRow, colnum );
		RichTextString richString = null;

		if( cellStyle != null ) ssCell.setCellStyle( cellStyle );
		if( cellValue != null ) {
			if( cellValue instanceof Number )
				ssCell.setCellValue( ((Number)cellValue).doubleValue() );
			else if( cellValue instanceof java.util.Date )
				ssCell.setCellValue( (java.util.Date)cellValue );
			else if( cellValue instanceof Hyperlink ) {
				Hyperlink link = (Hyperlink)cellValue;
				ssCell.setHyperlink( link );
				ssCell.setCellValue( link.getLabel() );
			} else {
				if( cellValue instanceof String )
					richString = ssRow.getSheet().getWorkbook().getCreationHelper().createRichTextString( (String)cellValue );
				else
					richString = ssRow.getSheet().getWorkbook().getCreationHelper().createRichTextString( cellValue.toString() );
				ssCell.setCellValue( richString );
			}
		}

		return ssCell;
	}

	public static Cell createCell( Sheet sheet, int rownum, int colnum, Object cellValue, CellStyle cellStyle, int rows, int cols ) {
		if( cols > 1 || rows > 1 ) {
			if( cols == 0 ) cols = 1;
			if( rows == 0 ) rows = 1;
			if( cellStyle != null ) {
				for( int row = rownum; row < rownum + rows; row++ ) {
					Row ssRow = SS.createRow( sheet, row );
					for( int col = colnum; col < colnum + cols; col++ )
						SS.createCell(ssRow, col).setCellStyle( cellStyle );
				}
			}
			sheet.addMergedRegion( new CellRangeAddress(rownum, rownum + rows - 1, colnum, colnum + cols - 1) );
		}

		return createCell( SS.createRow(sheet, rownum), colnum, cellValue, cellStyle );
	}

	// XSSFColor처리에  따른 생성  방법 분리
	public static CellStyle createCellStyle( Workbook workbook, CellStyle cellStyle ) {
		if( workbook instanceof org.apache.poi.hssf.usermodel.HSSFWorkbook )
			return createHSSFCellStyle( workbook, cellStyle );
		else
			return createXSSFCellStyle( workbook, cellStyle );
	}

	private static HSSFCellStyle createHSSFCellStyle( Workbook workbook, CellStyle cellStyle ) {
		HSSFCellStyle cellStyle_hssf = (HSSFCellStyle)workbook.createCellStyle();
		if( cellStyle != null ) {
			cellStyle_hssf.setAlignment( cellStyle.getAlignment() );
			cellStyle_hssf.setBorderBottom( cellStyle.getBorderBottom() );
			cellStyle_hssf.setBorderLeft( cellStyle.getBorderLeft() );
			cellStyle_hssf.setBorderRight( cellStyle.getBorderRight() );
			cellStyle_hssf.setBorderTop( cellStyle.getBorderTop() );
			cellStyle_hssf.setBottomBorderColor( cellStyle.getBottomBorderColor() );
			cellStyle_hssf.setDataFormat( cellStyle.getDataFormat() );
			cellStyle_hssf.setFillBackgroundColor( cellStyle.getFillBackgroundColor() );
			cellStyle_hssf.setFillForegroundColor( cellStyle.getFillForegroundColor() );
			cellStyle_hssf.setFillPattern( cellStyle.getFillPattern() );
			cellStyle_hssf.setFont( workbook.getFontAt(cellStyle.getFontIndex()) );
			cellStyle_hssf.setHidden( cellStyle.getHidden() );
			cellStyle_hssf.setIndention( cellStyle.getIndention() );
			cellStyle_hssf.setLeftBorderColor( cellStyle.getLeftBorderColor() );
			cellStyle_hssf.setLocked( cellStyle.getLocked() );
			cellStyle_hssf.setRightBorderColor( cellStyle.getRightBorderColor() );
			cellStyle_hssf.setRotation( cellStyle.getRotation() );
			cellStyle_hssf.setTopBorderColor( cellStyle.getTopBorderColor() );
			cellStyle_hssf.setVerticalAlignment( cellStyle.getVerticalAlignment() );
			cellStyle_hssf.setWrapText( cellStyle.getWrapText() );
		}
		return cellStyle_hssf;
	}

	private static XSSFCellStyle createXSSFCellStyle( Workbook workbook, CellStyle cellStyle ) {
		XSSFCellStyle	cellStyle_xssf = (XSSFCellStyle)workbook.createCellStyle();
		if( cellStyle != null ) {
			cellStyle_xssf.setAlignment( cellStyle.getAlignment() );
			cellStyle_xssf.setBorderBottom( cellStyle.getBorderBottom() );
			cellStyle_xssf.setBorderLeft( cellStyle.getBorderLeft() );
			cellStyle_xssf.setBorderRight( cellStyle.getBorderRight() );
			cellStyle_xssf.setBorderTop( cellStyle.getBorderTop() );
			cellStyle_xssf.setBottomBorderColor( cellStyle.getBottomBorderColor() );
			cellStyle_xssf.setDataFormat( cellStyle.getDataFormat() );
			cellStyle_xssf.setFillBackgroundColor( (XSSFColor) cellStyle.getFillBackgroundColorColor() );
			cellStyle_xssf.setFillForegroundColor( (XSSFColor) cellStyle.getFillForegroundColorColor() );
			cellStyle_xssf.setFillPattern( cellStyle.getFillPattern() );
			cellStyle_xssf.setFont( workbook.getFontAt(cellStyle.getFontIndex()) );
			cellStyle_xssf.setHidden( cellStyle.getHidden() );
			cellStyle_xssf.setIndention( cellStyle.getIndention() );
			cellStyle_xssf.setLeftBorderColor( cellStyle.getLeftBorderColor() );
			cellStyle_xssf.setLocked( cellStyle.getLocked() );
			cellStyle_xssf.setRightBorderColor( cellStyle.getRightBorderColor() );
			cellStyle_xssf.setRotation( cellStyle.getRotation() );
			cellStyle_xssf.setTopBorderColor( cellStyle.getTopBorderColor() );
			cellStyle_xssf.setVerticalAlignment( cellStyle.getVerticalAlignment() );
			cellStyle_xssf.setWrapText( cellStyle.getWrapText() );
		}
		return cellStyle_xssf;
	}

	public static CellStyle createCellStyle( Workbook workbook, Column column ) {
		return createCellStyle( workbook, column, null );
	}

	public static CellStyle createCellStyle( Workbook workbook, Column column, CellStyle cellStyle ) {
		cellStyle = createCellStyle( workbook, cellStyle );

		SS.ColumnStyle columnStyle = null;
		try {
			columnStyle = (SS.ColumnStyle)column.getColumnAttr();
		} catch( ClassCastException castEx ) {
			if( column.getColumnAttr() instanceof String ) {
				columnStyle = getColumnStyle((String)column.getColumnAttr());
			}
		}

		if( columnStyle != null ) {
			if( columnStyle.alignment >= 0 ) cellStyle.setAlignment( columnStyle.alignment );
			if( columnStyle.dataFormat != null ) cellStyle.setDataFormat( workbook.createDataFormat().getFormat(columnStyle.dataFormat) );
			if( columnStyle.foregroundColor >= 0 ) {
				cellStyle.setFillPattern( CellStyle.SOLID_FOREGROUND );
				cellStyle.setFillForegroundColor( columnStyle.foregroundColor );
			} else if( columnStyle.foregroundColor < -1 )
				cellStyle.setFillPattern( CellStyle.NO_FILL );
		}

		return cellStyle;
	}

	public static CellStyle createCellStyle( Workbook workbook, CellStyle cellStyle, SS.ColumnStyle columnStyle ) {
		if( columnStyle != null ) {
			cellStyle = createCellStyle( workbook, cellStyle );
			if( columnStyle.alignment >= 0 )
				cellStyle.setAlignment( columnStyle.alignment );
			else
				cellStyle.setAlignment( CellStyle.ALIGN_GENERAL );
			if( columnStyle.dataFormat != null ) cellStyle.setDataFormat( workbook.createDataFormat().getFormat(columnStyle.dataFormat) );
			if( columnStyle.foregroundColor >= 0 ) {
				cellStyle.setFillPattern( CellStyle.SOLID_FOREGROUND );
				cellStyle.setFillForegroundColor( columnStyle.foregroundColor );
			} else if( columnStyle.foregroundColor < -1 )
				cellStyle.setFillPattern( CellStyle.NO_FILL );
		}

		return cellStyle;
	}

	public static Font createFont( Workbook workbook, Font ssFont ) {
		Font ssFont_new = workbook.createFont();

		ssFont_new.setBoldweight( ssFont.getBoldweight() );
		ssFont_new.setColor( ssFont.getColor() );
		ssFont_new.setFontHeight( ssFont.getFontHeight() );
		ssFont_new.setFontHeightInPoints( ssFont.getFontHeightInPoints() );
		ssFont_new.setFontName( ssFont.getFontName() );
		ssFont_new.setItalic( ssFont.getItalic() );
		ssFont_new.setStrikeout( ssFont.getStrikeout() );
		ssFont_new.setTypeOffset( ssFont.getTypeOffset() );
		ssFont_new.setUnderline( ssFont.getUnderline() );

		return ssFont_new;
	}

	public static Row createRow( Sheet sheet, int rownum ) {
		Row ssRow = sheet.getRow( rownum );
		if( ssRow != null ) return ssRow;

		return sheet.createRow( rownum );
	}

	private static CellStyle getCellStyleBorder( Workbook workbook, Map<String, CellStyle> cellStyleMap, CellStyle cellStyle
						, short borderLeft, short borderRight, short borderTop, short borderBottom ) {
		if( cellStyle.getBorderLeft() == borderLeft ) borderLeft = CellStyle.BORDER_NONE;
		if( cellStyle.getBorderRight() == borderRight ) borderRight = CellStyle.BORDER_NONE;
		if( cellStyle.getBorderTop() == borderTop ) borderTop = CellStyle.BORDER_NONE;
		if( cellStyle.getBorderBottom() == borderBottom ) borderBottom = CellStyle.BORDER_NONE;

		if( borderLeft == CellStyle.BORDER_NONE && borderRight == CellStyle.BORDER_NONE
				&& borderTop == CellStyle.BORDER_NONE && borderBottom == CellStyle.BORDER_NONE )
			return cellStyle;

		String key = cellStyle.getIndex() +"_"+ borderLeft +"_"+ borderRight +"_"+ borderTop +"_"+ borderBottom;
		CellStyle cellStyle_new = cellStyleMap.get( key );
		if( cellStyle_new == null ) {
			cellStyle_new = SS.createCellStyle( workbook, cellStyle );
			if( borderLeft != CellStyle.BORDER_NONE ) cellStyle_new.setBorderLeft( borderLeft );
			if( borderRight != CellStyle.BORDER_NONE ) cellStyle_new.setBorderRight( borderRight );
			if( borderTop != CellStyle.BORDER_NONE ) cellStyle_new.setBorderTop( borderTop );
			if( borderBottom != CellStyle.BORDER_NONE ) cellStyle_new.setBorderBottom( borderBottom );

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

	public static SS.ColumnStyle getColumnStyle( String columnAttr ) {
		return SS.ColumnStyle.getInstance( columnAttr );
	}

	public static String getFieldHeaderString( com.irt.util.MessageHandler msghandler, AbstractFieldSet afs, String fieldKey ) {
		return msghandler.getMessage( afs.getField(fieldKey).getDescriptionKey() );
	}

	public static String getFieldDataString( com.irt.util.MessageHandler msghandler, AbstractFieldSet afs, Map<String, Object> fieldMap, String fieldKey ) {
		return getFieldDataString(msghandler, afs, fieldMap, fieldKey, null);
	}

	public static String getFieldDataString( com.irt.util.MessageHandler msghandler, AbstractFieldSet afs, Map<String, Object> fieldMap, String fieldKey, String fieldFormat) {
		String value = null;
		if( fieldFormat != null ) {
			value = PatternRecordFormat.getInstance(fieldFormat).format(fieldMap, msghandler);
		} else {
			com.irt.data.format.RecordFormat format = afs.getField(fieldKey);
			value = format.format(fieldMap, msghandler);
		}
		return value;
	}

	public static void printFieldSet( DataWriter out, String headerFieldString, String dataFieldString ) throws IOException {
		printFieldSet( out, headerFieldString, dataFieldString, 1, 1, false );
	}

	public static void printFieldSet( DataWriter out, String headerFieldString, String dataFieldString, boolean eol ) throws IOException {
		printFieldSet( out, headerFieldString, dataFieldString, 1, 1, eol );
	}

	public static void printFieldSet( DataWriter out, String headerFieldString, String dataFieldString, int headerFieldSpan, int dataFieldSpan ) throws IOException {
		printFieldSet( out, headerFieldString, dataFieldString, headerFieldSpan, dataFieldSpan, false );
	}

	public static void printFieldSet( DataWriter out, String headerFieldString, String dataFieldString, int headerFieldSpan, int dataFieldSpan, boolean eol) throws IOException {
		out.setDataType( DataWriter.FIELDHEADER );
		out.print( headerFieldString, headerFieldSpan );

		out.setDataType( DataWriter.FIELDDATA );
		out.print( dataFieldString, dataFieldSpan );

		if( eol ) out.println();
	}

	public static void setBorder( Workbook workbook, Sheet sheet, CellRangeAddress cellRangeAddress
						, short borderLeft, short borderRight, short borderTop, short borderBottom ) {
		setBorder( workbook, sheet, cellRangeAddress, borderLeft, borderRight, borderTop, borderBottom, (short)-1 );
	}
	public static void setBorder( Workbook workbook, Sheet sheet, CellRangeAddress cellRangeAddress
						, short borderLeft, short borderRight, short borderTop, short borderBottom, short color ) {
		Map<String, CellStyle> cellStyleMap = new java.util.HashMap<String, CellStyle>();

		int firstCol = cellRangeAddress.getFirstColumn();
		int lastCol = cellRangeAddress.getLastColumn();
		int firstRow = cellRangeAddress.getFirstRow();
		int lastRow = cellRangeAddress.getLastRow();

		if( borderLeft != CellStyle.BORDER_NONE && ( firstCol != lastCol || borderRight == CellStyle.BORDER_NONE ) ) {
			for( int row = firstRow; row <= lastRow; row++ ) {
				Cell ssCell = SS.createCell( sheet, row, firstCol );
				CellStyle cellStyle = getCellStyleBorder(
					workbook, cellStyleMap
					, ssCell.getCellStyle()
					, borderLeft
					, CellStyle.BORDER_NONE
					, row == firstRow ? borderTop : CellStyle.BORDER_NONE
					, row == lastRow ? borderBottom : CellStyle.BORDER_NONE
				);
				if( color > 0 ) {
					cellStyle.setLeftBorderColor( color );
					if( row == firstRow && borderTop != CellStyle.BORDER_NONE ) cellStyle.setTopBorderColor( color );
					if( row == lastRow && borderBottom != CellStyle.BORDER_NONE ) cellStyle.setBottomBorderColor( color );
				}
				ssCell.setCellStyle( cellStyle );
			}
		}

		if( borderRight != CellStyle.BORDER_NONE ) {
			if( firstCol != lastCol ) borderLeft = CellStyle.BORDER_NONE;
			for( int row = firstRow; row <= lastRow; row++ ) {
				Cell ssCell = SS.createCell( sheet, row, lastCol );
				CellStyle cellStyle = getCellStyleBorder(
					workbook, cellStyleMap
					, ssCell.getCellStyle()
					, borderLeft
					, borderRight
					, row == firstRow ? borderTop : CellStyle.BORDER_NONE
					, row == lastRow ? borderBottom : CellStyle.BORDER_NONE
				);
				if( color > 0 ) {
					if( borderLeft != CellStyle.BORDER_NONE ) cellStyle.setLeftBorderColor( color );
					cellStyle.setRightBorderColor( color );
					if( row == firstRow && borderTop != CellStyle.BORDER_NONE ) cellStyle.setTopBorderColor( color );
					if( row == lastRow && borderBottom != CellStyle.BORDER_NONE ) cellStyle.setBottomBorderColor( color );
				}
				ssCell.setCellStyle( cellStyle );
			}
		}

		if( borderTop != CellStyle.BORDER_NONE && (firstRow != lastRow || borderBottom == CellStyle.BORDER_NONE) ) {
			Row ssRow = SS.createRow( sheet, firstRow );
			for( int col = firstCol; col <= lastCol; col++ ) {
				Cell ssCell = SS.createCell( ssRow, col );
				CellStyle cellStyle = getCellStyleBorder(
					workbook, cellStyleMap
					, ssCell.getCellStyle(), CellStyle.BORDER_NONE, CellStyle.BORDER_NONE, borderTop, CellStyle.BORDER_NONE
				);
				if( color > 0 ) cellStyle.setTopBorderColor( color );
				ssCell.setCellStyle( cellStyle );
			}
		}

		if( borderBottom != CellStyle.BORDER_NONE ) {
			Row ssRow = SS.createRow( sheet, lastRow );
			if( firstRow != lastRow ) borderTop = CellStyle.BORDER_NONE;
			for( int col = firstCol; col <= lastCol; col++ ) {
				Cell ssCell = SS.createCell( ssRow, col );
				CellStyle cellStyle = getCellStyleBorder(
					workbook, cellStyleMap
					, ssCell.getCellStyle(), CellStyle.BORDER_NONE, CellStyle.BORDER_NONE, borderTop, borderBottom
				);
				if( color > 0 ) {
					if( borderTop != CellStyle.BORDER_NONE ) cellStyle.setTopBorderColor( color );
					cellStyle.setBottomBorderColor( color );
				}
				ssCell.setCellStyle( cellStyle );
			}
		}
	}

	/**
	 *
	 */
	public static class ColumnStyle {
		private static Map<String, SS.ColumnStyle> instanceMap
				= java.util.Collections.synchronizedMap( new java.util.WeakHashMap<String, SS.ColumnStyle>(50) );

		int cellType;
		short alignment, width, foregroundColor;
		String dataFormat;

		private ColumnStyle( int cellType, short alignment, short width, String dataFormat ) {
			this.cellType = cellType;
			this.alignment = alignment;
			this.width = width;
			this.foregroundColor = -1;
			this.dataFormat = dataFormat;
		}

		private ColumnStyle( int cellType, short alignment, short width, short foregroundColor, String dataFormat ) {
			this.cellType = cellType;
			this.alignment = alignment;
			this.width = width;
			this.foregroundColor = foregroundColor;
			this.dataFormat = dataFormat;
		}

		@Override
		public SS.ColumnStyle clone() {
			return new SS.ColumnStyle( this.getCellType(), this.getAlignment(), this.getWidth(), this.getForegroundColor(), this.getDataFormat() );
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

		public short getForegroundColor() {
			return foregroundColor;
		}

		private static SS.ColumnStyle getInstance( String columnAttr ) {
			if( columnAttr == null ) return null;

			SS.ColumnStyle columnStyle = instanceMap.get( columnAttr );
			if( columnStyle != null ) return columnStyle;

			int cellType = Cell.CELL_TYPE_BLANK;
			short width = -1;
			short alignment = -1;
			short foregroundColor = -1;
			String dataFormat = null;

			columnAttr = columnAttr.toLowerCase();
			String value = extractValue( columnAttr, "type" );
			if( value != null ) {
				if( "boolean".equals(value) || "bool".equals(value) )
					cellType = Cell.CELL_TYPE_BOOLEAN;
				else if( "formula".equals(value) )
					cellType = Cell.CELL_TYPE_FORMULA;
				else if( "numeric".equals(value) || "number".equals(value) )
					cellType = Cell.CELL_TYPE_NUMERIC;
				else if( "string".equals(value) || "text".equals(value) )
					cellType = Cell.CELL_TYPE_STRING;
			}

			value = extractValue( columnAttr, "align" );
			if( value != null ) {
				if( "left".equals(value) )
					alignment = CellStyle.ALIGN_LEFT;
				else if( "right".equals(value) )
					alignment = CellStyle.ALIGN_RIGHT;
				else if( "center".equals(value) )
					alignment = CellStyle.ALIGN_CENTER;
			}

			value = extractValue( columnAttr, "fgcolor" );
			if( value != null ) {
				try {
					foregroundColor = IndexedColors.valueOf( value.toUpperCase() ).getIndex();
				} catch( java.lang.IllegalArgumentException ex ) {
					foregroundColor = -1;
				}
			}

			try {
				value = extractValue( columnAttr, "width" );
				if( value != null ) width = Short.parseShort( value );
			} catch( NumberFormatException numEx ) {}

			dataFormat = extractValue( columnAttr, "format" );
			if( dataFormat == null ) {
				String cssClassNames = extractValue( columnAttr, "class" );
				String[] cssClassNameArr = ( cssClassNames == null ? null : cssClassNames.split("\\s\\s?+") );
				if( cssClassNameArr != null && cssClassNameArr.length > 0 ) {
					for( String clName : cssClassNameArr ) {
						if( clName != null && clName.startsWith("date_") ) {
							if( "date_ymd".equals(clName) ) {
								dataFormat = POI_DATETIME_YMD;
								width = (short)(width >= 0 ? width : POI_DATETIME_YMD.length() * POI_DATETIME_CHAR_MULTI );
							} else if( "date_ymdh".equals(clName) ) {
								dataFormat = POI_DATETIME_YMDH;
								width = (short)(width >= 0 ? width : POI_DATETIME_YMDH.length() * POI_DATETIME_CHAR_MULTI );
							} else if( "date_ymdhi".equals(clName) ) {
								dataFormat = POI_DATETIME_YMDHI;
								width = (short)(width >= 0 ? width : POI_DATETIME_YMDHI.length() * POI_DATETIME_CHAR_MULTI );
							} else if( "date_ymdhis".equals(clName) ) {
								dataFormat = POI_DATETIME_YMDHIS;
								width = (short)(width >= 0 ? width : POI_DATETIME_YMDHIS.length() * POI_DATETIME_CHAR_MULTI );
							}
						}
						if( dataFormat != null )
							break;
					}
				}
			}

			if( alignment < 0 && width < 0 && dataFormat == null && foregroundColor < 0 ) return null;

			columnStyle = new SS.ColumnStyle( cellType, alignment, width, foregroundColor, dataFormat );
			instanceMap.put( columnAttr, columnStyle );

			return columnStyle;
		}

		public short getWidth() {
			return width;
		}

		public void setForegroundColor( short foregroundColor ) {
			this.foregroundColor = foregroundColor;
		}
	}
}
