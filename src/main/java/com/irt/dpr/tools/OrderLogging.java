/*
 *	File Name:	OrderLogging.java
 *	Version:	2.2.5(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/10/31		2.2.5	OrderLogging() : tempDirectory 를 String -> File 객체로 처리하도록 수정
 *	GimHS		2025/09/30		2.2.4	BTPi interface(REST) 적용
 *	jbaek		2018/03/31		2.2.3	tempWmDir이 존재하지 않을시 자동생성
 *	jbaek		2017/09/30		2.2.2	getProcessPrefix added.
 *	GimHS		2011/07/31		2.2.1	dateFormat "yyyyMMddhhmmss" -> "yyyyMMddHHmmss" 변경
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.irt.util.Utility;

/**
 *
 */
public class OrderLogging {
	private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMddHHmmssSSS" );

	private String[] processTypes = new String[] {
			OrderCanonicalProcess.ORDER_IF_RDD, OrderCanonicalProcess.ORDER_IF_SIMULATION, OrderCanonicalProcess.ORDER_IF_CREATION
			, OrderCanonicalProcess.ORDER_IF_STATUSLIST, OrderCanonicalProcess.ORDER_IF_STATUS, OrderCanonicalProcess.ORDER_IF_BILLING, StockQueryCanonical.STOCKQUERY_IF_SIMULATION };

	Logger logger;
	Map<String, Object> properties;
	String tempPath;

	public OrderLogging() {
		this.logger = null;
		this.properties = null;
		this.tempPath = null;
	}

	public OrderLogging( Logger logger, Map<String, Object> systemInfo ) {
		this.logger = logger;
		this.properties = systemInfo;
		if( systemInfo == null || systemInfo.size() == 0 )
			this.properties = OrderCanonicalProcess.getSystemInfo();

		this.tempPath = null;

		if( properties != null ) {
			String path = (String)properties.get( "fileTempDirectory" );
			if( path == null || path.length() == 0 ) {
				if( properties.get("tempDirectory") != null ) {
					path = ((File)properties.get("tempDirectory")).getAbsolutePath();
				} else {
					path = new File(System.getProperty("user.dir"), "wm").getAbsolutePath();
				}
			}

			tempPath = path;
		}

		File tempWmDir = new File(tempPath);
		if( !tempWmDir.exists() ) tempWmDir.mkdirs();
		File byProcessDir = null;
		for( String processType : processTypes ) {
			byProcessDir = new File(tempWmDir, processType );
			if( !byProcessDir.exists() ) byProcessDir.mkdir();
		}
	}

	public void makeOrderTraceFile( String processType, String prefix, Map<String, Object> data ) {
		File file = getFile( processType, prefix );

		try {
			makeOrderTraceFile( file, OrderXMLUtility.mapConvertDoc( data, logger ) );
		} catch( com.irt.dpr.tools.CanonicalXMLException ex ) {
			logger.error( ex );
		}
	}

	public void makeOrderTraceFile( String processType, String prefix, Document document ) {
		File file = getFile( processType, prefix );

		makeOrderTraceFile( file, document );
	}

	public Document makeOrderTraceFile( File file, String data ) {
		java.io.PrintWriter out = null;
		try {
			Utility.validateFile( file );
			out = new java.io.PrintWriter( file );
			out.print( data );
		} catch( java.io.FileNotFoundException fileEx ) {
			logger.error( "Can't make XML file: " + file.getName(), fileEx );
		} catch( javax.xml.transform.TransformerFactoryConfigurationError factoryConfigEx ) {
			logger.error( "Can't make XML file" + file.getName(), factoryConfigEx );
		} finally {
			if( out != null ) out.close();
		}
		logger.debug( "XML file name : "+ file.getAbsolutePath() +"("+ file.getName() +")" );

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );
		} catch (ParserConfigurationException e) {}
		DocumentBuilder builder;
		Document doc = null;

		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse( file );
			doc.getDocumentElement().normalize();
		} catch( ParserConfigurationException e ) {
			logger.error( e );
		} catch( SAXException e ) {
			logger.error( e );
		} catch( IOException e ) {
			logger.error( e );
		}

		return doc;
	}

	public void makeOrderTraceFile( File file, Document document ) {
		OrderXMLUtility.documentWriteFile( file, document, logger );
	}

	/**
	 *
	 * @param processType
	 *            : status, statuslist
	 * @param requestPrefix
	 *            : request to wm, response from wm
	 * @return : eg. ERES, EREQ ...
	 */
	public static String getProcessPrefix( String processType, String requestPrefix ) {
		String fullPrefix = null;
		if( OrderCanonicalProcess.ORDER_IF_CREATION.equals( processType ) )
			fullPrefix = "C" + requestPrefix;
		else if( OrderCanonicalProcess.ORDER_IF_SIMULATION.equals( processType ) || StockQueryCanonical.STOCKQUERY_IF_SIMULATION.equals(processType) )
			fullPrefix = "S" + requestPrefix;
		else if( OrderCanonicalProcess.ORDER_IF_STATUS.equals( processType ) )
			fullPrefix = "E" + requestPrefix;
		else if( OrderCanonicalProcess.ORDER_IF_STATUSLIST.equals( processType ) )
			fullPrefix = "L" + requestPrefix;
		else if( OrderCanonicalProcess.ORDER_IF_BILLING.equals( processType ) )
			fullPrefix = "B" + requestPrefix;
		else if( OrderCanonicalProcess.ORDER_IF_RDD.equals( processType ) )
			fullPrefix = "R" + requestPrefix;
		else
			fullPrefix = "N" + requestPrefix;

		return fullPrefix;
	}

	private String sanitizeFileComponent( String value ) {
		if( value == null )
			return "EMPTY";

		String normalized = value.trim();
		if( normalized.length() == 0 )
			return "EMPTY";

		normalized = normalized.replace('/', '_').replace('\\', '_');
		while( normalized.indexOf("..") >= 0 )
			normalized = normalized.replace("..", "_");

		normalized = normalized.replaceAll("[^A-Za-z0-9_-]", "_");
		if( normalized.length() == 0 )
			return "EMPTY";

		return normalized;
	}

	private boolean isAllowedProcessType( String processType ) {
		if( processType == null ) return false;
		for( String allowed : processTypes ) {
			if( processType.equals( allowed ) ) return true;
		}
		return false;
	}

	public File getFile( String processType, String prefix ) {
		String times = dateFormat.format( new java.util.Date( System.currentTimeMillis() ) );

		File file = null;
		try {
			if( !isAllowedProcessType(processType) )
				throw new IllegalArgumentException( "Invalid process type" );

			String fullPreFix = getProcessPrefix( processType, sanitizeFileComponent(prefix) );

			File baseDir = new File( tempPath );
			File processDir = new File( baseDir, processType );
			File candidateFile = new File( processDir, fullPreFix + "_" + times + ".xml" );

			File canonicalBaseDir = baseDir.getCanonicalFile();
			File canonicalCandidateFile = candidateFile.getCanonicalFile();

			String basePath = canonicalBaseDir.getPath();
			String candidatePath = canonicalCandidateFile.getPath();

			if( candidatePath == null || basePath == null
					|| !(candidatePath.equals(basePath) || candidatePath.startsWith(basePath + File.separator)) ) {
				throw new IllegalArgumentException( "Invalid file path" );
			}

			file = canonicalCandidateFile;
		} catch( Exception ex ) {
			logger.error( ex );
		}

		return file;
	}
}
