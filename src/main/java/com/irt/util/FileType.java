/*
 *	File Name:	FileType.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2013/08/30		2.2.0	create
 *
**/

package com.irt.util;


/*
 * RBM_UPLOADLOG.FILE_TYPE Column
 */
public interface FileType {

	/*
	 * XLS type: Excel 2003 형식 ( binary processing )
	 */
	public static final String XLS = "XLS";

	/*
	 * XLSX type: Excel 2007형식 ( xml dom processing )
	 */
	public static final String XLX = "XLX";

	/*
	 * XL Fast type: Excel 2007형식 (xml sax processing ) org.apache.poi.xssf.streaming 소스 활용하여, 기존 XSSF와의 연동이 용이함.
	 */
	public static final String XLF = "XLF";

	/*
	 * Comma Separated Value type: char delimiter = ','. CSVReader, CSVWriter
	 */
	public static final String CSV = "CSV";

	/*
	 * Tab Separated Value type: char delimiter = '\t' CSVReader, CSVWriter
	 */
	public static final String TAB = "TAB";

	/*
	 * 고정장 type: FLVReader, FLVWriter
	 */
	public static final String FLV = "FLV";

	/*
	 * Custom Type ( ex. com.rbm.pds.POSDataReader )
	 */
	public static final String CST = "CST";


	public String getFileType();
}
