/*
 *	File Name:	RBMWorkbook.java
 *	Version:	2.2.2c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/02/28		2.2.2c	setColumnList(): sheet writer 기능의 columnlist setter.
 *	jbaek		2013/11/30		2.2.2	createWorkbook(): 엑셀이 아닌 파일타입 요청시 에러메세지 변경
 *	jbaek		2013/08/30		2.2.1	RBMDatabook상속. 파일 및 sheet이름 결정 로직 추가. maxRow/maxColumn 결정 로직 추가
 *	jbaek		2013/02/28		2.2.0	create
 *
 **/

package com.irt.util;

import com.irt.data.DataException;
import com.irt.data.DataWriter;

import java.io.IOException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *	RBMDatabook을 상속받음.
 *	RBMWorkbook은 POI관련 된 것만 처리함.
 *	RBMWorkbook은 RBMDatabook의 method들을 사용할 수 있기 때문에 RBMWorkbook은 POI 및 Text 형식 Data R/W 시 사용.
 */
public abstract class RBMWorkbook extends RBMDatabook {
	protected Workbook workbook;
	protected Sheet sheet;
	protected Sheet[] sheets;

	/** SAX Process시 기본 100 row까지는 재접근 허용. **/
	public final static int DEFAULT_XLF_ROWWINDOWSIZE		= SXSSFWorkbook.DEFAULT_WINDOW_SIZE;
	/** SAX Process시 무한대 row까지 재접근 허용. memory많아야 함. 만약 파일이 크면 jvm heap Memory 에러 발생가능. **/
	public final static int DEFAULT_XLF_ANAL_ROWWINDOWSIZE	= -1;

	public final static String DEFAULT_SHEETNAME			= "report";

	public final static String WORKBOOKTYPE_HSSF			= "HSSF";
	public final static String WORKBOOKTYPE_SXSSF			= "SXSSF";
	public final static String WORKBOOKTYPE_XSSF			= "XSSF";

	private final static int XLS_MAX_COLUMN_SIZE			= 256;
	private final static int XLS_MAX_ROW_SIZE				= 65536;
	private final static int XLX_MAX_COLUMN_SIZE			= 16384;
	private final static int XLX_MAX_ROW_SIZE				= 1048576;

	private boolean IS_ANAL_MODE = false;

	/** XLSX 파일처리시 기본값으로 xls 파일 row size */
	private final static int XLX_DEFAULT_MAX_ROW_SIZE		= XLS_MAX_ROW_SIZE;

	private boolean maxColChanged = false;
	private boolean maxRowChanged = false;
	private int MAX_COLUMN_SIZE								= XLS_MAX_COLUMN_SIZE;
	private int MAX_ROW_SIZE								= XLS_MAX_ROW_SIZE;

	public final void setAnalMode( boolean tf ) {
		this.IS_ANAL_MODE = tf;
	}

	public final boolean isAnalMode() {
		return IS_ANAL_MODE;
	}

	public final void setMaxRowSize( int maxRow ) {
		this.MAX_ROW_SIZE = maxRow;
		this.maxRowChanged = true;
	}

	public final int getMaxRowSize() {
		if( maxRowChanged )
			return MAX_ROW_SIZE;
		if( workbook instanceof HSSFWorkbook )
			return XLS_MAX_ROW_SIZE;
		else
			return XLX_DEFAULT_MAX_ROW_SIZE;
	}

	public final void setMaxColumnSize( int maxCol ) {
		if( maxCol > XLS_MAX_COLUMN_SIZE )
		this.MAX_COLUMN_SIZE = maxCol;
		this.maxRowChanged = true;
	}

	public final int getMaxColumnSize() {
		if( maxColChanged )
			return MAX_COLUMN_SIZE;
		if( workbook instanceof HSSFWorkbook )
			return XLS_MAX_COLUMN_SIZE;
		else
			return XLX_MAX_COLUMN_SIZE;
	}

	/**
	 * @return Workbook ( HSSFWorkbook or XSSFWorkbook or SXSSFWorkbook )
	 * SXSSFWorkbook 생성시 DEFAULT_XLF_ROWWINDOWSIZE를 따른다.(= 100)
	 */
	public static final Workbook createWorkbook( String xlFileType ) throws IOException {
		return createWorkbook( xlFileType, DEFAULT_XLF_ROWWINDOWSIZE );
	}

	/**
	 * @return Workbook ( HSSFWorkbook or XSSFWorkbook or SXSSFWorkbook )
	 * xlRowWindowSize는 SXSSFWorkbook에만 적용.
	 */
	public static final Workbook createWorkbook( String xlFileType, int xlfRowWindowSize ) throws IOException {
		Workbook wb;

		if( FileType.XLS.equals(xlFileType) ) {
			wb = new HSSFWorkbook();
		} else if( FileType.XLX.equals(xlFileType) ) {
			wb = new XSSFWorkbook();
		} else if( FileType.XLF.equals(xlFileType) ) {
			wb = new SXSSFWorkbook( new XSSFWorkbook(), xlfRowWindowSize );
		} else {
			throw new IOException( DataException.ERR_IO_INVALID_REQUEST_XSL_FILETYPE );
		}
		return wb;
	}

	/**
	 * @return POI fileType com.irt.util.FileType( XLS or XLX )
	 */
	public static final String getFileType( java.io.InputStream inputStream ) throws IOException {
		String fileType = null;
		if( !inputStream.markSupported() )
			inputStream = new java.io.PushbackInputStream( inputStream, 8 );
		if( org.apache.poi.poifs.filesystem.POIFSFileSystem.hasPOIFSHeader(inputStream) ) {
			fileType = FileType.XLS;
		} else if( org.apache.poi.POIXMLDocument.hasOOXMLHeader(inputStream) )
			fileType = FileType.XLX;
		if( fileType == null )
			throw new IOException( DataException.ERR_IO_INVALID_XSL_FILE );
		return fileType;
	}

	/**
	 * @return Workbook ( HSSFWorkbook or XSSFWorkbook )
	 * SXSSFWorkbook을 얻기 위해서는 fileType으로 생성해야함.
	 */
	public static final Workbook createWorkbook( java.io.InputStream inputStream ) throws IOException {
		Workbook wb = null;
		try {
			if( !inputStream.markSupported() )
				inputStream = new java.io.PushbackInputStream( inputStream, 8 );
			if( org.apache.poi.poifs.filesystem.POIFSFileSystem.hasPOIFSHeader(inputStream) ) {
				java.io.InputStream wrappedStream = POIFSFileSystem.createNonClosingInputStream( inputStream );
				wb = new HSSFWorkbook( new org.apache.poi.poifs.filesystem.POIFSFileSystem(wrappedStream) );
			} else if( org.apache.poi.POIXMLDocument.hasOOXMLHeader(inputStream) )
				wb = WorkbookFactory.create( inputStream );
		} catch( InvalidFormatException e ) {
			throw new IOException( DataException.ERR_IO_XLX_INVALID_FORMAT_EXCEPTION );
		} finally {
			if( wb == null )
				throw new IOException( DataException.ERR_IO_INVALID_XSL_FILE );
		}
		return wb;
	}

	public static final Workbook createWorkbook( java.io.File file ) throws IOException {
		if( file == null ||  !Utility.isValidateFile(file) )
			throw new IOException( DataException.ERR_IO_XLX_INVALID_FORMAT_EXCEPTION );

		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(file);
		} catch( InvalidFormatException ex ) {
			throw new IOException( DataException.ERR_IO_XLX_INVALID_FORMAT_EXCEPTION, ex );
		}
		return wb;
	}

	/**
	 * @return com.irt.util.FileType ( XLS or XLF or XLX )
	 */
	public static final String getFileType( Workbook workbook ) {
		if( workbook instanceof HSSFWorkbook )
			return com.irt.util.FileType.XLS;
		else if( workbook instanceof SXSSFWorkbook )
			return com.irt.util.FileType.XLF;
		else
			return com.irt.util.FileType.XLX;
	}

	public final Workbook createWorkbook( POIFSFileSystem fs ) throws IOException {
		return WorkbookFactory.create( fs );
	}

	public final RichTextString getRichTextString( String str ) {
		return workbook.getCreationHelper().createRichTextString( str );
	}

	public final Hyperlink getHyperlink( int type ) {
		return workbook.getCreationHelper().createHyperlink( type );
	}

	public final DataFormat createDataFormat() {
		return workbook.getCreationHelper().createDataFormat();
	}

	public final FormulaEvaluator createFormulaEvaluator() {
		return workbook.getCreationHelper().createFormulaEvaluator();
	}

	public final Sheet getSheet() {
		return sheet;
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	private final String getResponseContentType() {
		return getResponseContentType( this.workbook );
	}

	/**
	 * @return contentType from workbook
	 * HSSFWorkbook = "applicatoin/vnd.ms-excel";
	 * XSSFWorkbook, SXSSFWorkbook = "application/vnd.openxmlformats-officedocument.spreadsheetml";
	 */
	public static final String getResponseContentType( Workbook workbook ) {
		if( workbook instanceof HSSFWorkbook )
			return "application/vnd.ms-excel";
		else
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}

	private final String getFileExtension() {
		return getFileExtension( this.workbook );
	}

	/**
	 * @return com.irt.util.FileType ( XLS or XLX )
	 * HSSFWorkbook = XLS
	 * XSSFWorkbook, SXSSFWorkbook = XLX
	 */
	public static final String getFileExtension( Workbook workbook ) {
		if( workbook instanceof HSSFWorkbook )
			return getFileExtension( FileType.XLS );
		else
			return getFileExtension( FileType.XLX );
	}

	protected final void write( java.io.OutputStream out ) throws IOException {
		workbook.write(out);
	}

	/**
	 * HttpServletResponse의 ContentType과 Content-Disposition 을 셋팅하여 write수행
	 * 주로 AnalysisResultWriter 등에서 사용.
	 */
	public final void writeResponse( javax.servlet.http.HttpServletResponse res, String downFileName ) throws IOException {
		res.setContentType( getResponseContentType() );
		res.setHeader( "Content-Disposition", "attachment; filename=" + createSafeFullFileName(downFileName) + ";" );

		this.workbook.write( res.getOutputStream() );
	}

	public static void setColumnList( DataWriter out, com.irt.data.cols.ColumnList columnList ) {
		if( out instanceof com.irt.util.SSDataWriter ) {
			((com.irt.util.SSDataWriter)out).setColumnList( columnList );
		} else if( out instanceof com.irt.util.SheetWriter ) {
			((com.irt.util.SheetWriter)out).setColumnList( columnList );
		}
	}


	/**
	 * 현재는 DEFAULT_SHEETNAME을 fileName으로 response함.
	 * <pre>
	 * //TODO: firstSheetName에서  한글로 파일 이름 지정 어떻게 할지.
	 * //ex) String firstSheetName = new String( workbook.getSheetName(0).getBytes(UTF-8), "EUC-KR");
	 * @ workbook의 firstSheetName을 가져와서 fileName으로 보냄. @
	 * HttpServletResponse의 ContentType과 Content-Disposition 을 셋팅하여 write수행
	 * 주로 AnalysisResultWriter 등에서 사용.
	 * </pre>
	 */
	public final void writeResponse( javax.servlet.http.HttpServletResponse res ) throws IOException {
		String downFileName = null;
		//anal sheet 이름은 title 이름으로 정해지기 때문에 default로 나가는게 좋음.
		downFileName = createSafeFullFileName( workbook.getSheetName(0) );

		res.setContentType( getResponseContentType() );
		res.setHeader( "Content-Disposition", "attachment; filename=" + downFileName + ";" );
		this.workbook.write( res.getOutputStream() );
	}

	@Override
	public String getFileType() {
		return getFileType( workbook );
	}

	/**
	 * @return valid Sheet Name ( 허용하지 않는 문자 제거 , sheet 이름은 31자 이하만 허용 )
	 */
	public static String createSafeSheetName( String nameProposal, char replaceChar ) {
		return org.apache.poi.ss.util.WorkbookUtil.createSafeSheetName( nameProposal, replaceChar );
	}

	/**
	 * @return fileName 영어 체크. ( "\\w+" )
	 * 참고로 "\\p{L}+" = diacritical char 포함( 라틴어 )
	 */
	public static boolean validateFileName( String name ) {
		return name.matches( "\\w+" );
	}

	/**
	 * @return validated nameProposal's fileNamePart + ( xls or xlsx )
	 * 실패시 DEFAULT_SHEETNAME return됨.
	 */
	public String createSafeFullFileName( String nameProposal ) {
		// get NamePart
		int idx = nameProposal.lastIndexOf(".");
		if( idx < 0 ) idx = nameProposal.length();
		String fileNamePart = nameProposal.substring( 0, idx );

		if( validateFileName(fileNamePart) )
			return fileNamePart +"."+ getFileExtension();
		else
			return DEFAULT_SHEETNAME +"."+ getFileExtension();
	}

	@Override
	public boolean isBinary() {
		return true;
	}
}
