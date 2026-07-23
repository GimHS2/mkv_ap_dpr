/*
 *	File Name:	RBMDatabook.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	song7981	2016/05/20		2.2.3	DEFAULT_ENCAP 값 오류 수정
 *	jbaek		2013/11/30		2.2.2	getResponseContentType(): XLX contentType 변경. getPOIDataWriter(): Deprecated 표시. 삭제예정.
 *	yjcha		2013/10/11		2.2.1	getDataReader(): default fileType 설정
 *	jbaek		2013/08/30		2.2.0	create
**/

package com.irt.util;

import com.irt.data.DataException;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.SAXException;

/**
 * <pre>
 * DataWriter DataReader 관련 method 모음.
 * RBMWorkbook이 RBMDatabook을 상속하므로 static 기능사용시에는 RBMWorkbook 사용
 * Text형식의 Data Process class들인 CSVWriter, CSVReader등의 클래스에서 상속함.
 * </pre>
 */
public abstract class RBMDatabook implements DataReader, DataWriter {
	public final static String DEFAULT_CONTENT_ENCODING		= "UTF-8";
	public final static String DEFAULT_FILENAME_ENCODING	= "8859_1";
	public final static char DEFAULT_DELIM					= CSVWriter.DEFAULT_DELIM;
	public final static char DEFAULT_ENCAP					= CSVWriter.DEFAULT_ENCAP;
	public final static String DEFAULT_LINE_SEPARATOR		= "\r\n";

	/** SimpleDateFormat */
	public final static String DEFAULT_DATEPATTERN			= "yyyy-MM-dd";

	protected final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( RBMDatabook.class );

	@Override
	public void close() throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void flush() throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public char getDataType() {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public String getFileType() {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public int getLineNumber() {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public String[] getLines() {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public String getLineString() {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public boolean isBlankLine() {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public boolean isEOF() {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void print(double d) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void print(float f) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void print(int i) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void print(long l) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void print(Object o) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void print(Object o, int colspan) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void print(String s) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void print(String s, int colspan) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void print(String... ss) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void println() throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void println(String... ss) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void printNull() throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void printNull(int nullcnt) throws IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public String[] readNext() throws DataException, IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public java.util.Map<String, Object> readNext(String[] keys) throws DataException, IOException {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void setDataType(char dataType) {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	@Override
	public void setTrim(boolean trim) {
		throw new UnsupportedOperationException(DataException.ERR_ERROR);
	}

	/**
	 * return contentType from fileType
	 * <pre>
	 * XLS = "application/vnd.ms-excel";
	 * XLX, XLF = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	 * CSV = "text/csv";
	 * TAB = "text/tsv";
	 * DEFAULT	= "application/smnet";
	 * </pre>
	 */
	public static final String getResponseContentType( String fileType ) {
		String contentType = null;
		if( FileType.XLS.equals(fileType) )
			contentType = "application/vnd.ms-excel";
		else if( FileType.XLF.equals(fileType) || FileType.XLX.equals(fileType) )
			contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		else if( FileType.CSV.equals(fileType) )
			contentType = "text/csv";
		else if( FileType.TAB.equals(fileType) )
			contentType = "text/tsv";
		else
			contentType = "application/smnet";
		return contentType;
	}

	/**
	 * return fileType from contentType
	 * <pre>
	 * XLS = "application/vnd.ms-excel"; or "application/vnd.ms-office";
	 * XLX, XLF = "application/vnd.openxmlformats-officedocument.spreadsheetml";
	 * CSV = "text/csv";
	 * TAB = "text/tsv"; or text/tab-separated-values;
	 * DEFAULT	= "application/smnet";
	 * </pre>
	 */
	public static final String getFileTypeFromContentType( String contentType ) {
		String fileType = null;
		if( contentType != null && contentType.length() > 0 ) {
			if( contentType.contains("application/vnd.ms-excel") || contentType.contains("application/vnd.ms-office") )
				fileType = FileType.XLS;
			else if( contentType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") )
				fileType = FileType.XLF;
			else if( contentType.contains("text/csv") )
				fileType = FileType.CSV;
			else if( contentType.contains("text/tab-separated-values") || contentType.contains("text/tsv") )
				fileType = FileType.TAB;
			else
				fileType = FileType.CST;
		}
		return fileType;
	}

	/**
	 * @return file Extension
	 */
	public static final String getFileExtension( String fileType ) {
		String rlt = null;
		if( FileType.XLS.equals(fileType) )
			rlt = "xls";
		else if( FileType.XLX.equals(fileType) || FileType.XLF.equals(fileType) )
			rlt = "xlsx";
		else if( FileType.CSV.equals(fileType) )
			rlt = "csv";
		else
			rlt = "txt";
		return rlt;
	}

	/**
	 * <pre>
	 * dataWriter내부에서 flush()수행될 시에 dataWriter를 사용하여 out에 flush수행.
	 * RBMWorkbook인 경우에는 write() 수행 Text형식인 경우는 flush() 수행.
	 * </pre>
	 */
	protected static void flushOut( DataWriter dataWriter, java.io.OutputStream out ) throws IOException {
		if( dataWriter instanceof com.irt.util.RBMWorkbook ) {
			((com.irt.util.RBMWorkbook) dataWriter).write( out );
		} else {
			dataWriter.flush();
			dataWriter.close();
		}
	}

	/**
	 * @return text fileType
	 * 구현 안됨. text 및 csv 및 tab 구분 로직 필요. (encoding 및 특수 문자 구분 필요 )
	 * 구현을 위해서는 icu4j 참고.
	 */
	public static String getFileType( java.io.InputStream inputStream ) throws IOException {
		throw new UnsupportedOperationException(" unsupported ");
	}

	protected static boolean isValidFileType( String fileType ) throws IOException {
		boolean found = false;
		for( java.lang.reflect.Field field : com.irt.util.FileType.class.getDeclaredFields() ) {
			if( field.getName() != null && field.getName().equals(fileType) ) {
				found = true;
				break;
			}
		}
		return found;
	}

	/** create com.irt.util.TempFile for automatic deletion of the temp file. */
	protected static TempFile getXLFTempFile( File xlsxFile ) {
		return new TempFile(org.apache.poi.util.TempFile.createTempFile(xlsxFile.getName(), "."+ FileType.XLF.toLowerCase()).getAbsolutePath() );
	}

	/**
	 * <pre>
	 * @return DataReader [ CSVReader( XLF or CSV or TAB ) or SheetReader( XLS or XLX ) ]
	 * fileType param이 XLF인 경우 첫번째 sheet를 csv로 변환하여 CSVReader return.
	 * </pre>
	 */
	public static DataReader getDataReader( java.io.File inputFile, String fileType, String encoding ) throws DataException, IOException {
		return getDataReader( inputFile, fileType, encoding, DEFAULT_ENCAP );
	}

	/** DataReader return
	 * XLF형식( xlsx로 업로드시 )에 upload파일에서 첫번째 시트만 읽어서 처리함.
	 */
	protected static DataReader getDataReader( java.io.File inputFile, String fileType, String encoding, char encap ) throws DataException, IOException {
		if( !isValidFileType(fileType) )
			throw new IOException( DataException.ERR_IO_INVALID_REQUEST_FILETYPE );

		DataReader reader = null;
		FileInputStream inputStream = null;
		InputStreamReader inputStreamReader;
		InputStream sheetInputStream = null;
		TempFile tFile = null;

		Utility.validateFile( inputFile );

		if( FileType.XLF.equals(fileType) ) {
			char xlfEncap = '"';
			int defaultMinCols = -1;
			try{
				OPCPackage pkg = OPCPackage.open( inputFile, org.apache.poi.openxml4j.opc.PackageAccess.READ );
				ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable( pkg );
				XSSFReader xr = new XSSFReader( pkg );
				StylesTable styles = xr.getStylesTable();
				sheetInputStream = xr.getSheet("rId1");
				tFile = getXLFTempFile( inputFile );
				PrintStream thisout = new PrintStream( tFile );

				XLSX2CSV thisTransformer = new XLSX2CSV( pkg, thisout, defaultMinCols );
				thisTransformer.processSheet( styles, strings, sheetInputStream );
				sheetInputStream.close();
			} catch( InvalidFormatException formatEx ) {
				throw new DataException( DataException.ERR_IO_XLX_INVALID_FORMAT_EXCEPTION, "ERR_IO_XLX_INVALID_FORMAT_EXCEPTION", formatEx );
			} catch( ParserConfigurationException pConfEx ) {
				throw new DataException( DataException.ERR_IO_XLX_PARSER_CONFIGURATION_EXCEPTION, "ERR_IO_XLX_PARSER_CONFIGURATION_EXCEPTION", pConfEx );
			} catch( SAXException saxEx ) {
				throw new DataException( DataException.ERR_IO_XLX_SAX_EXCEPTION, "ERR_IO_XLX_SAX_EXCEPTION", saxEx );
			} catch( OpenXML4JException xEx ) {
				throw new DataException( DataException.ERR_IO_XLX_OPENXML4J_EXCEPTION, "ERR_IO_XLX_OPENXML4J_EXCEPTION", xEx );
			}

			if( logger.isTraceEnabled() ) {
				logger.trace( "RBMDatabook.getDataReader: " + inputFile.getName() + " fileType:" + fileType + " DataReader created. " );
			}
			return getDataReader( tFile, FileType.CSV, encoding, xlfEncap );
		} else if( FileType.TAB.equals(fileType) ) {
			char delimiter = '\t';
			inputStream = new FileInputStream( inputFile );
			try {
				if( encoding != null )
					inputStreamReader = new java.io.InputStreamReader( inputStream, encoding );
				else
					inputStreamReader = new java.io.InputStreamReader( inputStream );
			} catch( java.io.UnsupportedEncodingException encodeEx ) {
				inputStreamReader = new java.io.InputStreamReader( inputStream );
			}
			reader = new CSVReader( inputStreamReader, delimiter, encap );
		} else if ( FileType.CSV.equals(fileType) ) {
			inputStream = new FileInputStream( inputFile );
			try {
				if( encoding != null )
					inputStreamReader = new java.io.InputStreamReader( inputStream, encoding );
				else
					inputStreamReader = new java.io.InputStreamReader( inputStream );
			} catch( java.io.UnsupportedEncodingException encodeEx ) {
				inputStreamReader = new java.io.InputStreamReader( inputStream );
			}
			reader = new CSVReader( inputStreamReader, DEFAULT_DELIM, encap );
		} else if( FileType.XLS.equals(fileType) ) {
			reader = new com.irt.util.SheetReader( inputFile );
			reader.setTrim( true );
		} else if( FileType.XLX.equals(fileType) ) {
			reader = new com.irt.util.SheetReader( inputFile );
			reader.setTrim( true );
		} else {
			inputStream = new FileInputStream( inputFile );
			try {
				if( encoding != null )
					inputStreamReader = new java.io.InputStreamReader( inputStream, encoding );
				else
					inputStreamReader = new java.io.InputStreamReader( inputStream );
			} catch( java.io.UnsupportedEncodingException encodeEx ) {
				inputStreamReader = new java.io.InputStreamReader( inputStream );
			}
			reader = new CSVReader( inputStreamReader );
		}
		inputStream = null;

		if( logger.isTraceEnabled() ) {
			if( inputFile.getName().contains("."+FileType.XLF.toLowerCase()) )
				logger.trace( "RBMDatabook.getDataReader: " +
						inputFile.getName() +" "+ FileType.XLF + " fileType converted:" + fileType + " DataReader created. " );
			else
				logger.trace( "RBMDatabook.getDataReader: " + inputFile.getName() + " fileType:" + fileType + " DataReader created. " );
		}

		return reader;
	}

	public static DataWriter getDataWriter( java.io.OutputStream out, String fileType, String encoding ) throws IOException {
		if( !isValidFileType(fileType) )
			throw new IOException( DataException.ERR_IO_INVALID_REQUEST_FILETYPE );

		if( FileType.XLS.equals(fileType) || FileType.XLX.equals(fileType) || FileType.XLF.equals(fileType) ) {
			return new SSDataWriter( out, RBMWorkbook.createWorkbook(fileType) );
		} else
			return getTextDataWriter( out, encoding, fileType );
	}

	private static DataWriter getTextDataWriter( OutputStream outputStream, String encoding, String fileType ) {
		char delim = DEFAULT_DELIM;
		if( FileType.TAB.equals(fileType) )
			delim = '\t';

		java.io.Writer out = null;
		try {
			if( encoding != null )
				out = new java.io.PrintWriter( new java.io.OutputStreamWriter(outputStream, encoding) );
			else
				out = new java.io.PrintWriter( new java.io.OutputStreamWriter(outputStream) );
		} catch( java.io.UnsupportedEncodingException encodeEx ) {
			out = new java.io.PrintWriter( new java.io.OutputStreamWriter(outputStream) );
		}
		return new com.irt.util.CSVWriter( out, delim, DEFAULT_LINE_SEPARATOR );
	}

	@Override
	public boolean isBinary() {
		return false;
	}
}
