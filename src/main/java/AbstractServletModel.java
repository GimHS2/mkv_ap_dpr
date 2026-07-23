/*
 *	File Name:	AbstractServletModel.java
 *	Version:	2.2.16
 *
 *	Description:
 *		IMPORTANT! customize 대상파일
 *
 *	Note:
 *		error.jsp
 *		error_passwd.jsp
 *		error_session.jsp
 *		systemConfig.getProperty( "uploadPath" )
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.16	신규 UI/UX 적용
 *	hankalam	2020/10/30		2.2.15	createDataLoader(): S3 Storage 방식 추가
 *	jbaek		2020/03/30		2.2.14	$mobile
 *	jbaek		2020/03/30		2.2.14	tryWorkbookAutoSizeColumn(): description의 경우만 autosize사용하도록 변경.
 *	jbaek		2020/06/30		2.2.14	Revise Order Feature. printErrorPage(): warn level logging
 *	hankalam	2020/03/31		2.2.14	createTextDataWriter(), createDataWriter(): cookieOption 추가
 *	jbaek		2019/07/30		2.2.13	createDataWriter(): filename utf-8 encode, getDownUpColumnList(): updateFieldKeys 표시, tryWorkbookAutoSizeColumn() 추가
 *	jbaek		2019/01/30		2.2.12	AbstractServletModel에서 ctx.mode가 "down"으로 시작시 OPTIONKEY_DELETE_HTML이 기본으로 적용, jsonResponse() 추가
 *	jbaek		2018/04/30		2.2.11	createDataLoader() : encoding detect suggestion 처리
 *	jbaek		2017/09/30		2.2.10	tryGetDataDate() : noticeStartDate, noticeEndDate 처리
 *										getUploadFileType() : FileInputStream close 추가
 *										printErrorPage() : res.isCommitted() 임시 추가
 *										createDataLoader() : warn log 추가
 *	hankalam	2017/02/28		2.2.9	setMaintenanceNotice(): noticeStartDate, noticeEndDate 조건 변경
 *	jbaek		2017/02/28		2.2.9	getFileType(), getUploadFileType() 보완
 *	hankalam	2016/05/31		2.2.8	printError() : url 웹취약성 수정
 *	jbaek		2015/04/30		2.2.7	maintenanceNotice 기간 설정. IllegalArgumentException 처리
 *	jbaek		2014/12/31		2.2.6	maintenanceNotice 적용
 *	jbaek		2014/12/31		2.2.5	TextDataReader() 추가
 *	jbaek		2014/09/30		2.2.4	Product Hierarhcy Level 기능 추가 : 엑셀 다운로드 기능 추가
 *	lsinji		2008/09/26		2.2.3	MASTER_LANGUAGE 추가
 *	stghr12		2008/06/20		2.2.2	init() 추가: RBMSystem.initSystemEnv() 호출
 *										setAttributeTPLower(): authKey, authValue를 사용할 경우 제대로 못 가져오는 오류수정
 *	stghr12		2008/03/31		2.2.1	createDataLoader() 추가
 *										getTradePartnerRole(): static으로 수정
 *										makeRedirectURL() 추가
 *										setAttributeTPLower() 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getItemMap(), getPartyMap(), getTradeItemMap(), getTradePartnerMap(), getUserUserMap() 추가
 *										getTradePartnerRole(), hasAuthorityTP(), setAttributeMaster(), setAttributeTP() 추가
 *										getUserParty() -> getUserPartyMap()
 *										com.irt.system.SystemConfig 변경사항 적용: Locale 사용
 *										ctx.pageConfig.getLocale() -> ctx.locale
 *										ROLE_*, MASTER_* 추가
 *	stghr12		2007/07/31		2.1.1	getColumnResourceBundle(): msghandler 추가
 *	stghr12		2007/04/30		2.1.0	version up
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;

import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import com.irt.data.Record;
import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;
import com.irt.data.cols.ColumnListWrapper;
import com.irt.data.cols.ColumnResourceBundle;
import com.irt.data.cols.ColumnWrapper;
import com.irt.html.ColumnConfigureFile;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.json.Jsoner;
import com.irt.rbm.RBMSystem;
import com.irt.rbm.usr.UserParty;
import com.irt.resbdl.ColumnResourceFileWriter;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.ServletUtility2;
import com.irt.sql.SQLHandler;
import com.irt.system.SessionManagerException;
import com.irt.util.*;

/**
 *
 */
public abstract class AbstractServletModel extends com.irt.servlet.ServletModel {//@formatter:off
	protected final static String COLUMN_RESOURCE_NAME	= "mesg.ColumnResource";
	protected final static String MAINTENANCE_NOTICE_FILENAME	= "maintenanceNotice.xml";
	protected final static String MAINTENANCE_MESSAGE	= "MSG_SYSTEM_MAINTENANCE";

	protected final static char ROLE_BUYER				= 'B';
	protected final static char ROLE_SELLER				= 'S';

	protected final static int MASTER_CURRENCY			= 0x01;
	protected final static int MASTER_TIMEZONE			= 0x02;
	protected final static int MASTER_LANGUAGE			= 0x03;
	protected final static int MASTER_COUNTRY			= 0x04;
	protected static final String[] DEFAULT_DOWNUP_UPDATE_SYS_FIELDKEYS = { "updateUserId" };
	protected static final String DEFAULT_DOWNUP_UPDATE_MARK_COLOR = "yellow";
	protected static final String DEFAULT_DOWNUP_INSERT_MARK_COLOR = "sky_blue";

	Map encodingFallbackRules = new java.util.HashMap(){
		{
			put("GB18030", "GB2312");
		}
	};

	Map encodingNamingRules = new java.util.HashMap(){
		{
			put("UTF8", "UTF-8");
			put("UTF16", "UTF-16");
		}
	};

	Map fileTypeBinaryInfo = new java.util.HashMap(){
		{
			put("XLS", "Y");
			put("XLX", "Y");
			put("XLF", "Y");
		}
	};

	/** excel and text file */
	protected DataLoader createDataLoader( Context ctx, SQLHandler handler, DataLoader.Loader loader, DataLoader.Logger loaderLogger
						, DataLoader.Validator validator, boolean commitByLine ) throws IOException, ServletException {
		if( !(ctx.req instanceof com.irt.servlet.MultipartHttpRequest) )
			throw new ServletModelException( ServletModelException.INVALID_REQUEST );

		String inputFileName = null;
		java.io.File uploadFile, errorFile = null;
		com.irt.servlet.MultipartHttpRequest req = (com.irt.servlet.MultipartHttpRequest)ctx.req;
		if( (uploadFile = req.getFile("file")) == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else
			inputFileName = req.getInputFileName( "file" );

		String detectedEncoding = req.getDetectedEncoding("file");

		String fileType = getUploadFileType( ctx, uploadFile, inputFileName );
		String encoding = ctx.req.getParameter( "encoding" );

		if( encoding != null ) {
			if( encodingNamingRules.get(encoding) != null ) {
				encoding = (String)encodingNamingRules.get(encoding);
			}
		}

		if( (fileTypeBinaryInfo.get(fileType) == null || !"Y".equals(fileTypeBinaryInfo.get(fileType)))
				&& encoding != null ) {
			if( detectedEncoding != null ) {
				if( !encoding.equals(detectedEncoding)) {
					if( encodingFallbackRules.containsKey(detectedEncoding)
							&& encoding.equals(encodingFallbackRules.get(detectedEncoding)) ) {
						logger.warn("encoding detected: "+detectedEncoding+" but accepted to use " + encoding + " by encodingFallbackRules rule." );
					} else {
						String fallbackEncoding = (String)encodingFallbackRules.get(detectedEncoding);

						String suggestion = detectedEncoding;
						if( fallbackEncoding != null ) {
							suggestion = fallbackEncoding + "(" + detectedEncoding +")";
						}

						throw new ServletModelException(ServletModelException.INVALID_PARAMETER, ctx.msghandler.getMessage("ERR_SUGGEST_DETECTED_ENCODING", suggestion) ) ;
					}
				}
			}
		}
		if( encoding == null ) {
			encoding = systemConfig.getEncoding( ctx.locale );
		}

		Map<String, Object> resultMap = loaderLogger.getResultMap();
		if( resultMap != null && resultMap.get("uploadType") != null ) {
			String uploadPath = systemConfig.getProperty( "uploadPath" );
			boolean isS3Storage = "Y".equals( systemConfig.getProperty("s3Storage") );
			if( isS3Storage || uploadPath != null && uploadPath.length() > 0 ) {
				String uploadType = (String)resultMap.get( "uploadType" );
				String dateTimeValue = (new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")).format( new java.util.Date() );
				String fileName = uploadType.toLowerCase() +"_"+ ctx.sessionMng.getUserId() +"_"+ dateTimeValue +"_"+ loaderLogger.getLogId();
				if( !Utility.isSafeFileName(fileName) )
					throw new ServletModelException( ServletModelException.INVALID_PARAMETER, "invaild file name" );

				if( isS3Storage ) {
					errorFile = new java.io.File( systemConfig.getTemporaryDirectory(), fileName +"."+ com.irt.util.RBMWorkbook.getFileExtension(fileType) +".err" );
					try {
						S3Service.s3Instance.upload( uploadFile, fileName +"."+ com.irt.util.RBMWorkbook.getFileExtension(fileType) );
					} catch( com.amazonaws.AmazonServiceException asEx ) {
						throw new ServletModelException( ServletModelException.INTERNAL_ERROR
								, ctx.msghandler.getMessage("ERR_INTERNAL_ERROR"
										, ctx.msghandler.getMessage("MSG_S3_STORAGE") + " " + ctx.msghandler.getMessage("MSG_ERROR")) ) ;
					}
					resultMap.put( "fileName", fileName );
				} else if( uploadPath != null && uploadPath.length() > 0 ) {
					if( !Utility.isSafeFilePath(uploadPath) )
						throw new ServletModelException( ServletModelException.INVALID_PARAMETER, "invaild file upload path" );

					java.io.File newFile;

					newFile = new java.io.File( uploadPath, fileName +"."+ com.irt.util.RBMWorkbook.getFileExtension(fileType) );
					errorFile = new java.io.File( uploadPath, fileName +"."+ com.irt.util.RBMWorkbook.getFileExtension(fileType) +".err" );
					if( !uploadFile.renameTo(newFile) ) {
						java.io.InputStream inputStream = new java.io.FileInputStream( uploadFile );
						java.io.OutputStream outputStream = null;
						try {
							int length;
							byte[] buffer = new byte[1024 * 10];

							outputStream = new java.io.FileOutputStream( newFile );
							while( (length = inputStream.read(buffer)) >= 0 ) {
								outputStream.write( buffer, 0, length );
								outputStream.flush();
							}
						} finally {
							try { inputStream.close(); } catch( Exception ignored ) {}
							try { if( outputStream != null ) outputStream.close(); } catch( Exception ignored ) {}
						}
					}
					uploadFile = newFile;

					resultMap.put( "fileName", fileName );
				} else {
					logger.warn( "uploadPath is not configured. -> cannot use temp file. (for " + loaderLogger.getClass().getSimpleName() + ")" );
				}
			}
			resultMap.put( "encoding", encoding );
			resultMap.put( "fileType", fileType );
			resultMap.put( "uploadFileName", inputFileName );
		}

		// get DataReader
		com.irt.data.DataReader dataReader = null;
		try {
			dataReader = com.irt.util.RBMWorkbook.getDataReader( uploadFile, fileType, encoding );
		} catch( com.irt.data.DataException dataEx ) {
			throw new ServletModelException( dataEx.getErrorKey(), ctx.msghandler.getMessage(dataEx.getMessage()) );
		}

		// get DataLoader
		com.irt.data.DataLoader dataLoader = null;
		if( errorFile == null )
			dataLoader = new DataLoader( handler, loader, loaderLogger, validator, dataReader, commitByLine );
		else if( encoding == null )
			dataLoader = new DataLoader( handler, loader, loaderLogger, validator, dataReader, commitByLine, errorFile );
		else
			dataLoader = new DataLoader( handler, loader, loaderLogger, validator, dataReader, commitByLine, errorFile, encoding );

		return dataLoader;
	}

	/** text file */
	protected DataLoader createTextDataLoader( Context ctx, SQLHandler handler, DataLoader.Loader loader, DataLoader.Logger loaderLogger
						, DataLoader.Validator validator, boolean commitByLine ) throws IOException, ServletException {
		if( !(ctx.req instanceof com.irt.servlet.MultipartHttpRequest) )
			throw new ServletModelException( ServletModelException.INVALID_REQUEST );

		java.io.File uploadFile, errorFile = null;
		com.irt.servlet.MultipartHttpRequest req = (com.irt.servlet.MultipartHttpRequest)ctx.req;
		if( (uploadFile = req.getFile("file")) == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String encoding = ctx.req.getParameter( "encoding" );
		String fileType = "TAB".equals(ctx.req.getParameter("fileType")) ? "TAB" : "CSV";
		String extension = fileType != null ? "." + fileType.toLowerCase() : "";
		Map<String, Object> resultMap = loaderLogger.getResultMap();
		{
			if( encoding == null ) encoding = systemConfig.getEncoding( ctx.locale );

			String uploadPath = systemConfig.getProperty( "uploadPath" );
			boolean isS3Storage = "Y".equals( systemConfig.getProperty("s3Storage") );
			if( isS3Storage || uploadPath != null && uploadPath.length() > 0 ) {
				String uploadType = (String)resultMap.get( "uploadType" );
				String dateTimeValue = (new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")).format( new java.util.Date() );
				String fileName = uploadType.toLowerCase() +"_"+ ctx.sessionMng.getUserId() +"_"+ dateTimeValue +"_"+ loaderLogger.getLogId();
				if( !Utility.isSafeFileName(fileName) )
					throw new ServletModelException( ServletModelException.INVALID_PARAMETER, "invaild file name" );

				if( isS3Storage ) {
					errorFile = new java.io.File( systemConfig.getTemporaryDirectory(), fileName + extension + ".err" );
					try {
						S3Service.s3Instance.upload( uploadFile, fileName + extension );
					} catch( com.amazonaws.AmazonServiceException asEx ) {
						throw new ServletModelException( ServletModelException.INTERNAL_ERROR
								, ctx.msghandler.getMessage("ERR_INTERNAL_ERROR"
										, ctx.msghandler.getMessage("MSG_S3_STORAGE") + ctx.msghandler.getMessage("MSG_ERROR")) ) ;
					}
					resultMap.put( "fileName", fileName );
				} else if( uploadPath != null && uploadPath.length() > 0 ) {
					java.io.File newFile;
					errorFile = new java.io.File( uploadPath, fileName + extension + ".err" );
					//uploadFile.renameTo( newFile = new java.io.File( uploadPath, fileName +".txt" ) );
					if( !uploadFile.renameTo(newFile = new java.io.File( uploadPath, fileName + extension )) ) {
						java.io.InputStream inputStream = new java.io.FileInputStream( uploadFile );
						java.io.OutputStream outputStream = null;
						try {
							int length;
							byte[] buffer = new byte[1024 * 10];

							outputStream = new java.io.FileOutputStream( newFile );
							while( (length = inputStream.read(buffer)) >= 0 ) {
								outputStream.write( buffer, 0, length );
								outputStream.flush();
							}
						} finally {
							try { inputStream.close(); } catch( Exception ignored ) {}
							try { if( outputStream != null ) outputStream.close(); } catch( Exception ignored ) {}
						}
					}

					uploadFile = newFile;

					resultMap.put( "fileName", fileName );
				}
			}

			resultMap.put( "encoding", encoding );
			resultMap.put( "fileType", fileType );
			resultMap.put( "uploadFileName", req.getInputFileName("file") );
		}

		java.io.InputStream inputStream = new java.io.FileInputStream( uploadFile );
		try {
			java.io.Reader inputStreamReader = null;
			try {
				if( encoding != null )
					inputStreamReader = new java.io.InputStreamReader( inputStream, encoding );
				else
					inputStreamReader = new java.io.InputStreamReader( inputStream );
			} catch( java.io.UnsupportedEncodingException encodeEx ) {
				encoding = null;
				resultMap.remove( "encoding" );
				inputStreamReader = new java.io.InputStreamReader( inputStream );
			}

			DataReader reader = null;
			if( "TAB".equals(fileType) )
				reader = new com.irt.util.CSVReader( inputStreamReader, '\t' );
			else
				reader = new com.irt.util.CSVReader( inputStreamReader );
			reader.setTrim( true );

			DataLoader dataLoader = null;
			if( errorFile == null )
				dataLoader = new DataLoader( handler, loader, loaderLogger, validator, reader, commitByLine );
			else if( encoding == null )
				dataLoader = new DataLoader( handler, loader, loaderLogger, validator, reader, commitByLine, errorFile );
			else
				dataLoader = new DataLoader( handler, loader, loaderLogger, validator, reader, commitByLine, errorFile, encoding );
			inputStream = null;

			return dataLoader;
		} finally {
			try { if( inputStream != null ) inputStream.close(); } catch( Exception ignored ) {}
		}
	}

	/** excel and text */
	protected DataReader createDataReader( Context ctx ) throws IOException, ServletException {
		if( !(ctx.req instanceof com.irt.servlet.MultipartHttpRequest) )
			throw new ServletModelException( ServletModelException.INVALID_REQUEST );

		com.irt.servlet.MultipartHttpRequest req = (com.irt.servlet.MultipartHttpRequest)ctx.req;
		if( req.getFile("file") == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String fileType = getFileType(ctx);
		String encoding = ctx.req.getParameter( "encoding" );
		if( encoding == null ) encoding = systemConfig.getEncoding( ctx.locale );

		DataReader dataReader = null;
		try {
			dataReader = com.irt.util.RBMWorkbook.getDataReader( req.getFile("file"), fileType, encoding );
		} catch( com.irt.data.DataException dataEx ) {
			throw new ServletModelException( dataEx.getErrorKey(), ctx.msghandler.getMessage(dataEx.getMessage()) );
		} catch( java.lang.IllegalArgumentException iae ) {
			throw new ServletModelException( ServletModelException.INTERNAL_ERROR );
		}

		return dataReader;
	}

	protected DataReader createTextDataReader( Context ctx ) throws IOException, ServletException {
		if( !(ctx.req instanceof com.irt.servlet.MultipartHttpRequest) )
			throw new ServletModelException( ServletModelException.INVALID_REQUEST );

		com.irt.servlet.MultipartHttpRequest req = (com.irt.servlet.MultipartHttpRequest)ctx.req;
		if( req.getFile("file") == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		java.io.InputStream inputStream = new java.io.FileInputStream( req.getFile("file") );
		try {
			java.io.Reader inputStreamReader = null;
			try {
				String encoding = ctx.req.getParameter( "encoding" );
				if( encoding == null ) encoding = systemConfig.getEncoding( ctx.locale );
				if( encoding != null )
					inputStreamReader = new java.io.InputStreamReader( inputStream, encoding );
				else
					inputStreamReader = new java.io.InputStreamReader( inputStream );
			} catch( java.io.UnsupportedEncodingException encodeEx ) {
				inputStreamReader = new java.io.InputStreamReader( inputStream );
			}

			DataReader reader;
			if( "TAB".equals(ctx.req.getParameter("fileType")) )
				reader = new com.irt.util.CSVReader( inputStreamReader, '\t' );
			else
				reader = new com.irt.util.CSVReader( inputStreamReader );
			reader.setTrim( true );
			inputStream = null;

			return reader;
		} finally {
			try { if( inputStream != null ) inputStream.close(); } catch( Exception ignored ) {}
		}
	}

	protected String getUploadFileType( Context ctx, java.io.File uploadFile, String uploadFileName ) throws ServletModelException, IOException {
		String fileType = null;

		if( uploadFileName != null ) {// can we trust user?
			String extension = RBMWorkbook.getFileExtension(uploadFileName);
			if( extension != null ) {
				if( "xls".equalsIgnoreCase(extension) ) {
					fileType = FileType.XLS;
				} else if ( "xlsx".equalsIgnoreCase(extension) ) {
					fileType = FileType.XLX;
				}
				// 그외 'csv' 및 'tsv' extension은  csv이면서 tab을 delimiter로 쓰는 경우가 있을 수 있어 detect하지 않음.
			}
		}

		if( uploadFile == null ) {
			com.irt.servlet.MultipartHttpRequest req = (com.irt.servlet.MultipartHttpRequest)ctx.req;
			if( (uploadFile = req.getFile("file")) == null ) {
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			}
		}

		if( fileType == null && uploadFile != null ) {// detect binary magic byte
			java.io.FileInputStream fis = null;
			try {
				fis = new java.io.FileInputStream(uploadFile);
				fileType = com.irt.util.RBMWorkbook.getFileType( fis );
			} catch( IOException ioEx ) {
			} finally {
				if( fis != null ) fis.close();
			}
		}

		if( fileType != null ) { // binary 경우 처리
			//binary가 xlsx이고   file size가 클 경우 xlf Process 수행
			if( com.irt.util.FileType.XLX.equals(fileType) ) {
				int disabledXLFProcess = -1;
				int xlfProcessEnableSize = com.irt.rbm.RBMSystem.getSystemEnvInt( "RBM", "Default;xlfProcessEnableSize", disabledXLFProcess );
				if( xlfProcessEnableSize != disabledXLFProcess && uploadFile.length() > xlfProcessEnableSize )
					fileType = com.irt.util.FileType.XLF;
			}

			//SYSTEM_ENV에 정의된 filetype사용. uploadFileType을 call하는 class. (eg. 서블렛이름.upload )
			String callerClassMethodName = com.irt.util.TraceHelper.getClassMethodName(com.irt.util.TraceHelper.CALLER);
			String envFileType = com.irt.rbm.RBMSystem.getSystemEnv( "RBM", "UploadFileType;" +callerClassMethodName, null );
			if( envFileType != null && envFileType.length() > 0 )
				fileType = envFileType;

		} else { // text 경우 처리
			String paramFileType = ctx.req.getParameter( "fileType" );
			if( paramFileType != null && paramFileType.length() > 0 )
				fileType =  paramFileType;
			else
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

		return fileType;
	}

	protected String getFileType( Context ctx ) throws ServletModelException, IOException {
		String fileType = null;
		String classMethodName = com.irt.util.TraceHelper.getClassMethodName(com.irt.util.TraceHelper.CURRENT);
		if( logger.isDebugEnabled() ) logger.debug( "TraceHelper.getClassMethodName::"+ classMethodName );

		if( com.irt.rbm.RBMSystem.getSystemEnvBool( "RBM", "EnvAutoReload;"+classMethodName, false ) ) {
			try {
				com.irt.rbm.RBMSystem.reloadSystemEnv(ctx.handler);
			} catch (SQLException e) {
				logger.debug(classMethodName + "::"+ e);
			}
		}

		if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) ) {
			if( !(ctx.req instanceof com.irt.servlet.MultipartHttpRequest) )
				throw new ServletModelException( ServletModelException.INVALID_REQUEST );

			String inputFileName = null;
			java.io.File uploadFile;
			com.irt.servlet.MultipartHttpRequest req = (com.irt.servlet.MultipartHttpRequest)ctx.req;
			if( (uploadFile = req.getFile("file")) == null )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			else
				inputFileName = req.getInputFileName( "file" );

			fileType = getUploadFileType( ctx, uploadFile, inputFileName );
		} else {//mode == download 및 그외
			fileType = ctx.req.getParameter( "fileType" );

			if( fileType == null || fileType.length() == 0 ) {
				String defaultFileType = com.irt.rbm.RBMSystem.getSystemEnv( "RBM", "Default;DownFileType", com.irt.util.FileType.XLS );

				//SYSTEM_ENV에 정의된 filetype사용
				String callerClassMethodName = com.irt.util.TraceHelper.getClassMethodName(com.irt.util.TraceHelper.CALLER);
				String envFileType = com.irt.rbm.RBMSystem.getSystemEnv( "RBM", "DownFileType;" +callerClassMethodName, null );
				if( envFileType != null && envFileType.length() > 0 ) {
					fileType = envFileType;
				} else {
					if( com.irt.util.FileType.XLS.equals(defaultFileType) || com.irt.util.FileType.XLX.equals(defaultFileType) || com.irt.util.FileType.XLF.equals(defaultFileType) )
						fileType = defaultFileType;
					else//현재 파일 다운로드 방식이 sheetWriter를 사용한 방식과 SSDataWriter방식이 혼용되어 있어 XLS 파일타입을 최종 default로 지정( CSV, TAB등은 default로 받아들일 수 없음 )
						fileType = com.irt.util.FileType.XLS;
				}
			}
		}
		return fileType;
	}

	/** excel and text file */
	protected com.irt.data.DataWriter createDataWriter( Context ctx, String filename ) throws IOException, ServletException {
		String fileType = getFileType(ctx);
		String cookieOption = systemConfig.getCookieOption();
		cookieOption = cookieOption != null ? cookieOption : "";
		cookieOption = cookieOption.replaceAll( "HttpOnly;", "" );
		String encoding = ctx.req.getParameter( "encoding" );
		if( encoding == null ) encoding = systemConfig.getEncoding( ctx.locale );

		ctx.res.setContentType( com.irt.util.RBMWorkbook.getResponseContentType(fileType) );
		filename = java.net.URLEncoder.encode(filename, "UTF-8");
		ctx.res.setHeader( "Content-Disposition", "attachment; filename="+ filename +"." + com.irt.util.RBMWorkbook.getFileExtension(fileType) );
		ctx.res.setHeader( "Set-Cookie", "fileDownload=true; path=/;" + cookieOption );

		if( logger.isTraceEnabled() ) logger.trace( "createDataWriter: " + fileType + encoding );

		return com.irt.util.RBMWorkbook.getDataWriter( ctx.res.getOutputStream(), fileType, encoding );
	}

	/** text file */
	protected com.irt.data.DataWriter createTextDataWriter( Context ctx, String filename ) throws IOException, ServletException {
		String cookieOption = systemConfig.getCookieOption();
		cookieOption = cookieOption != null ? cookieOption : "";
		cookieOption = cookieOption.replaceAll( "HttpOnly;", "" );
		ctx.res.setContentType( "application/smnet" );
		ctx.res.setHeader( "Content-Disposition", "attachment; filename="+ filename +".csv;" );
		ctx.res.setHeader( "Set-Cookie", "fileDownload=true; path=/;" + cookieOption );

		java.io.Writer out = null;
		try {
			String encoding = ctx.req.getParameter( "encoding" );
			if( encoding == null ) encoding = systemConfig.getEncoding( ctx.locale );
			logger.debug( "textwriter: filename: "+ filename +" enc: "+ encoding );
			if( encoding != null )
				out = new java.io.PrintWriter( new java.io.OutputStreamWriter(ctx.res.getOutputStream(), encoding) );
			else
				out = new java.io.PrintWriter( new java.io.OutputStreamWriter(ctx.res.getOutputStream()) );
		} catch( java.io.UnsupportedEncodingException encodeEx ) {
			out = new java.io.PrintWriter( new java.io.OutputStreamWriter(ctx.res.getOutputStream()) );
		}

		return new com.irt.util.CSVWriter( out, "\r\n" );
	}

	protected static Map<String, Object> createErrorMap( Object name, String message ) {
		Map<String, Object> errorMap = new java.util.TreeMap<String, Object>();

		errorMap.put( "name", name );
		errorMap.put( "message", message );

		return errorMap;
	}

	protected static Map<String, Object> createErrorMap( Object name, Throwable throwable ) {
		return createErrorMap( name, throwable.getMessage() );
	}

	protected PageConfig createPageConfig( Context ctx ) throws ServletException {
		try {
			com.irt.rbm.usr.UserPartyEnvironment partyEnv = new com.irt.rbm.usr.UserPartyEnvironment(ctx.handler, ctx.sessionMng.getPartyId());
			String useMobileProfileFeature = partyEnv.getValue("SYS", "Mobile;useMobileProfile", "N");
			String useMobileAutoFeature = partyEnv.getValue("SYS", "Mobile;useMobileAuto", "N");
			String csvMobileAutoDesktop = partyEnv.getValue("SYS", "Mobile;csvMobileAutoDesktop", "");
			ctx.req.setAttribute("useMobileProfile", useMobileProfileFeature);
			ctx.req.setAttribute("useMobileAuto", useMobileAutoFeature);
			ctx.req.setAttribute("csvMobileAutoDesktop", csvMobileAutoDesktop);
		} catch( SQLException sqlEx ) {
			logger.error("internal error.", sqlEx);
		}
		//
		ctx.pageConfig = new com.irt.custom.PageConfig( ctx.req, systemConfig, ctx.locale );
		String _defaultSupportLocale = "en";
		String _defaultSupportLocaleLabel = "English";
		if( "JNJAP_CN".equals(ctx.sessionMng.getPartyId()) ) {
			_defaultSupportLocale += ",zh";
			_defaultSupportLocaleLabel += ",Simplified-Chinese";
		} else if( "JNJAP_TH".equals(ctx.sessionMng.getPartyId()) ) {
			_defaultSupportLocale += ",th";
			_defaultSupportLocaleLabel += ",Thai";
		} else if( "JNJAP_KR".equals(ctx.sessionMng.getPartyId()) ) {
			_defaultSupportLocale += ",ko";
			_defaultSupportLocaleLabel += ",Korean";
		}
		final String _supportLocale = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocale;"+ ctx.sessionMng.getPartyId(), _defaultSupportLocale);
		final String _supportLocaleLabel = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocaleLabel;"+ ctx.sessionMng.getPartyId(), _defaultSupportLocaleLabel);
		ctx.pageConfig.setProperty("partySupportLocale", _supportLocale);
		ctx.pageConfig.setProperty("partySupportLocaleLabel", _supportLocaleLabel);
		ctx.req.setAttribute( "htmlpage", ctx.pageConfig );
		return ctx.pageConfig;
	}

	protected PageConfig createPageConfig( HttpServletRequest req ) {
		return new com.irt.custom.PageConfig( req, systemConfig, ServletUtility.getLocale(req) );
	}

	protected int getAuthLevelTP( Context ctx, Map recordMap, int authType ) throws SQLException {
		com.irt.rbm.ecs.TradePartnerAuth authDB = new com.irt.rbm.ecs.TradePartnerAuth( ctx.handler );
		return authDB.getAuthLevel( (String)recordMap.get("buyerGln"), (String)recordMap.get("sellerGln"), ctx.sessionMng.getUniqId(), authType );
	}

	protected int getAuthLevelTP( Context ctx, String buyerGln, String sellerGln, int authType ) throws SQLException {
		com.irt.rbm.ecs.TradePartnerAuth authDB = new com.irt.rbm.ecs.TradePartnerAuth( ctx.handler );
		return authDB.getAuthLevel( buyerGln, sellerGln, ctx.sessionMng.getUniqId(), authType );
	}

	private ColumnResourceBundle getDatabaseColumnResourceBundle( Context ctx ) throws ServletException {
		java.util.Locale locale = ctx.locale;
		ClassLoader loader = null;
		java.io.File msgloc = ColumnResourceFileWriter.getCreatedDirectory(".message-location", com.irt.custom.SystemConfig.getInstance("RBM").getTemporaryDirectory());

		try {
			loader = com.irt.resbdl.ResourceLoader.newInstance(msgloc.toURI().toURL(), this.getClass().getClassLoader());
		} catch( MalformedURLException e ) {
			logger.warn("DatabaseColumnResource function cannot be used because '.message-location' directory is not exists.");
			loader = this.getClass().getClassLoader();
		}

		ColumnResourceBundle parent, columnResourceBundle;
		try {
			parent = com.irt.html.ColumnConfigureFile.getColumnResourceBundle( COLUMN_RESOURCE_NAME, ctx.msghandler, locale, loader );
			if( parent == null )
				throw new ServletModelException( ServletModelException.CANNOT_FIND_COLUMNRESOURCEBUNDLE );

			String resourceName = COLUMN_RESOURCE_NAME +"_"+ ctx.sessionMng.getPartyId();
			columnResourceBundle = com.irt.html.ColumnConfigureFile.getColumnResourceBundle( parent, resourceName, ctx.msghandler, locale, loader );
			if( columnResourceBundle == null ) columnResourceBundle = parent;

			boolean reloaded = com.irt.html.DatabaseColumnResource.tryReloadAndSync(ctx.handler, columnResourceBundle, msgloc, ctx.sessionMng.getPartyId(), locale, COLUMN_RESOURCE_NAME);
			logger.trace("ColumnResourceBundle reloaded: "+ reloaded);

			if( loader instanceof com.irt.resbdl.ResourceLoader ) {
				List<File> files = ((com.irt.resbdl.ResourceLoader)loader).getOriginalColumnResourcePropertyFiles(COLUMN_RESOURCE_NAME);
				try {
					if( files != null ) {
						for( File file : files ) {
							if( file != null ) {
								File toDir = new File(msgloc, COLUMN_RESOURCE_NAME.split("\\.")[0]);
								if( !toDir.exists() ) toDir.mkdir();
								File toFile = new File(toDir, file.getName());
								if( !toFile.exists() ) toFile.createNewFile();

								FileUtil.copyFileContent(file, toFile);
							}
						}
					}
				} catch( IOException ioEx ) {
					logger.warn(ioEx);
				}

				parent = columnResourceBundle;
				String _resourceName = ColumnResourceFileWriter.getBaseName(ctx.sessionMng.getPartyId());
				ColumnResourceBundle _columnResourceBundle = com.irt.html.ColumnConfigureFile.getColumnResourceBundle( parent, _resourceName, ctx.msghandler, locale, loader );
				if( _columnResourceBundle != null ) {
					columnResourceBundle = _columnResourceBundle;
					if( !parent.equals(columnResourceBundle) ) {
						logger.trace("resourceName: "+ _resourceName + " ColumnResourceBundle replace from: "+ parent + " to "+ columnResourceBundle);
					}
				}
			}
		} catch( IOException ioEx ) {
			throw new ServletModelException( ioEx );
		}

		return columnResourceBundle;
	}

	@Override
	protected ColumnResourceBundle getColumnResourceBundle( Context ctx ) throws ServletException {
		if( RBMSystem.getSystemEnvBool("SYS", "DatabaseResource;useColumnResource", false) ) {
			return getDatabaseColumnResourceBundle(ctx);
		}

		java.util.Locale locale = ctx.locale;
		ClassLoader loader = this.getClass().getClassLoader();

		ColumnResourceBundle parent, columnResourceBundle;
		try {
			parent = com.irt.html.ColumnConfigureFile.getColumnResourceBundle( COLUMN_RESOURCE_NAME, ctx.msghandler, locale, loader );
			if( parent == null )
				throw new ServletModelException( ServletModelException.CANNOT_FIND_COLUMNRESOURCEBUNDLE );

			String resourceName = COLUMN_RESOURCE_NAME +"_"+ ctx.sessionMng.getPartyId();
			columnResourceBundle = com.irt.html.ColumnConfigureFile.getColumnResourceBundle( parent, resourceName, ctx.msghandler, locale, loader );

			if( columnResourceBundle == null ) columnResourceBundle = parent;
		} catch( IOException ioEx ) {
			throw new ServletModelException( ioEx );
		}

		return columnResourceBundle;
	}

	@Override
	protected ColumnList getColumnList( Context ctx, String columnListName, String... optionKeys ) throws ServletException {
		return getNormalColumnList(ctx, columnListName, optionKeys);
	}

	private ColumnList getNormalColumnList( Context ctx, String columnListName, String... optionKeys ) throws ServletException {
		List<String> optionKeyList = new ArrayList<String>();

		if( optionKeys != null && optionKeys.length > 0 )
			optionKeyList.addAll(java.util.Arrays.asList(optionKeys));

		if( ctx.mode != null && ctx.mode.startsWith("down") )
			if( !optionKeyList.contains(ColumnConfigureFile.OPTIONKEY_NODEL_HTML) )
				optionKeyList.add(ColumnConfigureFile.OPTIONKEY_DELETE_HTML);

		if( ctx.sessionMng.isSystemAdmin() )
			optionKeyList.add("SYSADMIN");

		return super.getColumnList(ctx, columnListName, optionKeyList.toArray(new String[0]));
	}

	protected ColumnList getDownUpColumnList( Context ctx, String columnListName, final String[] altKeys, String... optionKeys ) throws ServletException {
		ColumnList columnList = this.getNormalColumnList(ctx, columnListName, optionKeys);

		String[] cl_primaryFieldKeys = columnList.getPrimaryFieldKeys();
		/** when insert only table loader */
		String[] cl_insertFieldKeys = ( columnList.getProperty("insertFieldKeys") == null ? null
				: columnList.getProperty("insertFieldKeys").split(",\\s?+") );
		List<String> insertFieldKeyList = new ArrayList<String>();
		if( cl_primaryFieldKeys != null && cl_primaryFieldKeys.length > 0 )
			insertFieldKeyList.addAll(java.util.Arrays.asList(cl_primaryFieldKeys));
		if( cl_insertFieldKeys != null && cl_insertFieldKeys.length > 0 )
			insertFieldKeyList.addAll(java.util.Arrays.asList(cl_insertFieldKeys));
		final String[] insertFieldKeys = insertFieldKeyList.toArray(new String[0]);

		/** defined update keys using normal upload processing */
		final String[] cl_updateFieldKeys = ( columnList.getProperty("updateFieldKeys") == null ? null
				: columnList.getProperty("updateFieldKeys").split(",\\s?+") );
		/** update by extra processing( usually updating other table ) */
		final String[] cl_updateExtKeys = ( columnList.getProperty("updateExtKeys") == null ? null
				: columnList.getProperty("updateExtKeys").split(",\\s?+") );
		final String[] updateSysFieldKeys = ( columnList.getProperty("updateSysKeys") == null ? DEFAULT_DOWNUP_UPDATE_SYS_FIELDKEYS
				: columnList.getProperty("updateSysKeys").split(",\\s?+") ) ;
		final String insertMarkColor = ( columnList.getProperty("insertMarkColor") == null ? DEFAULT_DOWNUP_INSERT_MARK_COLOR
				: columnList.getProperty("insertMarkColor") );
		final String updateMarkColor = ( columnList.getProperty("updateMarkColor") == null ? DEFAULT_DOWNUP_UPDATE_MARK_COLOR
				: columnList.getProperty("updateMarkColor") );

		ColumnListWrapper clw = new ColumnListWrapper(columnList) {

			List<String> upgMarkKeyList = getUpdateMarkKeyList(cl_updateFieldKeys, altKeys, cl_updateExtKeys);

			@Override
			public Column getColumn( int index ) {
				ColumnWrapper cw = null;
				final Column column = super.getColumn(index);

				if( !java.util.Arrays.asList(updateSysFieldKeys).contains(column.getFieldKey())
						&&  ( upgMarkKeyList.contains(column.getFieldKey())
								|| ( insertFieldKeys != null && java.util.Arrays.asList(insertFieldKeys).contains(column.getFieldKey()) )
						) ) {
					cw = new ColumnWrapper(column) {
						@Override
						public Object getColumnAttr() {
							String columnAttr = (String)super.getColumnAttr();

							String attr = "";
							attr += getForegroundColorAttr(column, upgMarkKeyList.toArray(new String[0]), updateMarkColor);

							if( insertFieldKeys != null && insertFieldKeys.length > 0 ) {
								attr += getForegroundColorAttr(column, insertFieldKeys, insertMarkColor);
							}

							return ( attr.length() == 0 ? columnAttr : columnAttr + attr );
						};
					};
				}

				return ( cw == null ? column : cw );
			}

			@Override
			public Column[] getColumns() {
				Column[] cc = new Column[getColumnCount()];
				for( int i = 0; i < cc.length; i++ ) {
					cc[i] = getColumn(i);
				}
				return cc;
			}

			private String getForegroundColorAttr( Column column, String[] keys, String fgcolor ) {
				List<String> fieldKeyList = java.util.Arrays.asList(keys);

				String attr = "";
				String foregroundColor = StringUtil.extractAttrValue((String)column.getColumnAttr(), "fgcolor");
				if( foregroundColor == null && fieldKeyList.contains(column.getFieldKey()) ) {
					attr += " fgcolor='" + fgcolor + "'";
				}

				return attr;
			}

			@Override
			public String getProperty( String key ) {
				if( "updateFieldKeys".equals(key) ) {
					return StringUtil.strJoin(getUpdatePureFieldKeys(cl_updateFieldKeys, altKeys, cl_updateExtKeys, updateSysFieldKeys), ",");
				}

				return super.getProperty(key);
			}

//			private String[] getUpdatePureKeys( String[] upgKeys, String[] updateExtKeys ) {
//				List<String> upgPureKeyList = new java.util.ArrayList<String>();
//				if( upgKeys != null && upgKeys.length > 0 ) {
//					for( int i = 0; i < upgKeys.length; i++ ) {
//						if( !com.irt.util.Arrays.contains(updateExtKeys, upgKeys[i]) )
//							upgPureKeyList.add(upgKeys[i]);
//					}
//				}
//				return upgPureKeyList.toArray(new String[0]);
//			}

			private List<String> getUpdateMarkKeyList( String[] updateFieldKeys, String[] altKeys, String[] updateExtKeys ) {
				List<String> upgMarkKeyList = new java.util.ArrayList<String>();

				if( updateFieldKeys != null && updateFieldKeys.length > 0 ) {
					upgMarkKeyList.addAll(java.util.Arrays.asList(updateFieldKeys));
				} else if( altKeys != null && altKeys.length > 0 ) {
					upgMarkKeyList.addAll(java.util.Arrays.asList(altKeys));
				}

				if( updateExtKeys != null && updateExtKeys.length > 0 ) {
					upgMarkKeyList.addAll(java.util.Arrays.asList(updateExtKeys));
				}

				return upgMarkKeyList;
			}

			private String[] getUpdateMarkKeys( String[] updateFieldKeys, String[] altKeys, String[] updateExtKeys ) {
				return getUpdateMarkKeyList(updateFieldKeys, altKeys, updateExtKeys).toArray(new String[0]);
			}

			private String[] getUpdatePureFieldKeys( String[] updateFieldKeys, String[] altKeys, String[] updateExtKeys, String[] updateSysKeys ) {
				List<String> upgFieldKeyList = new java.util.ArrayList<String>();

				if( updateFieldKeys != null && updateFieldKeys.length > 0 ) {
					upgFieldKeyList.addAll(java.util.Arrays.asList(updateFieldKeys));

					if( updateExtKeys != null && updateExtKeys.length > 0 ) {
						for (String updateExtKey : updateExtKeys) {
							if( upgFieldKeyList.contains(updateExtKey) )
								upgFieldKeyList.remove(updateExtKey);
						}
					}

//					if( updateSysFieldKeys != null && updateSysFieldKeys.length > 0 ) {
//						for( int i = 0; i < updateSysFieldKeys.length; i++ ) {
//							if( !upgFieldKeyList.contains(updateSysFieldKeys[i]) )
//								upgFieldKeyList.remove(updateSysFieldKeys[i]);
//						}
//					}
				} else if( altKeys != null && altKeys.length > 0 ) {
					upgFieldKeyList.addAll(java.util.Arrays.asList(altKeys));
				}


				return upgFieldKeyList.toArray(new String[0]);
			}
		};

		return clw;
	}

	protected Map<String, Object> getItemMap( Context ctx, String gtin, String gln ) throws SQLException {
		return getItemMap( ctx, com.irt.rbm.ecs.Item.createPrimary(gtin, gln) );
	}

	protected Map<String, Object> getItemMap( Context ctx, Map<String, ? extends Object> primaryMap ) throws SQLException {
		com.irt.rbm.ecs.Item db = new com.irt.rbm.ecs.Item( ctx.handler );

		String[] fieldKeys = new String[] {
			"gtin", "gln", "companyName", "locationName", "itemName", "itemStatus", "measureDescription", "qtyOfItemsPerCase"
		};

		return db.getRecord( primaryMap, fieldKeys );
	}

	protected Map<String, Object> getPartyMap( Context ctx, String gln ) throws SQLException {
		return getPartyMap( ctx, com.irt.rbm.ecs.Party.createPrimary(gln) );
	}

	protected Map<String, Object> getPartyMap( Context ctx, Map<String, ? extends Object> primaryMap ) throws SQLException {
		com.irt.rbm.ecs.Party db = new com.irt.rbm.ecs.Party( ctx.handler );

		String[] fieldKeys = new String[] { "gln", "partyRole", "companyName", "locationName", "locationType", "parentGln", "childCount", "status" };

		return db.getRecord( primaryMap, fieldKeys );
	}

	protected Map<String, Object> getTradeItemMap( Context ctx, String buyerGln, String sellerGln, String gtin ) throws SQLException {
		return getTradeItemMap( ctx, com.irt.rbm.ecs.TradeItem.createPrimary( buyerGln, sellerGln, gtin ) );
	}

	protected Map<String, Object> getTradeItemMap( Context ctx, Map<String, ? extends Object> primaryMap ) throws SQLException {
		com.irt.rbm.ecs.TradeItem db = new com.irt.rbm.ecs.TradeItem( ctx.handler );

		String[] fieldKeys = new String[] {
			"buyerGln", "buyerPartyRole", "buyerCompanyName", "buyerLocationName", "parentBuyerGln"
			, "sellerGln", "sellerPartyRole", "sellerCompanyName", "sellerLocationName", "parentSellerGln"
			, "gtin", "itemName", "tradeItemInfoType", "qtyOfItemsPerCase", "qtyOfItemsPerPallet", "orderQtyMultiple"
			, "deliveryRoute", "dcGln", "dcLocationName", "destinationGln", "destinationCompanyName"
			, "orderLeadTimeToDestination", "orderLeadTimeToBuyer", "orderWeekday", "orderPolicy", "tradeStatus"
		};

		return db.getRecord( primaryMap, fieldKeys );
	}

	protected Map<String, Object> getTradePartnerMap( Context ctx, String buyerGln, String sellerGln ) throws SQLException {
		return getTradePartnerMap( ctx, com.irt.rbm.ecs.TradePartner.createPrimary( buyerGln, sellerGln ) );
	}

	protected Map<String, Object> getTradePartnerMap( Context ctx, Map<String, ? extends Object> primaryMap ) throws SQLException {
		com.irt.rbm.ecs.TradePartner db = new com.irt.rbm.ecs.TradePartner( ctx.handler );

		String[] fieldKeys = new String[] {
			"buyerGln", "buyerPartyRole", "buyerCompanyName", "buyerLocationName", "buyerUserId", "buyerUserName", "buyerContactName"
			, "sellerGln", "sellerPartyRole", "sellerCompanyName", "sellerLocationName", "sellerUserId", "sellerUserName", "sellerContactName"
			, "tradeInfoType", "parentBuyerGln", "parentSellerGln"
			, "tradeStatus", "startAvailDate", "endAvailDate"
		};

		return db.getRecord( primaryMap, fieldKeys );
	}

	protected static char getTradePartnerRole( Context ctx ) {
		return( ctx.sessionMng.isBuyerParty() && !ctx.sessionMng.isSellerParty() ? ROLE_BUYER : ROLE_SELLER );
	}

	protected Map<String, Object> getUserPartyMap( Context ctx, String partyId ) throws SQLException {
		com.irt.rbm.usr.UserParty db = new com.irt.rbm.usr.UserParty( ctx.handler );

		String[] fieldKeys = new String[] {
			"partyId", "partyName", "partyClass", "partyGln", "ediId", "serviceStartDate", "serviceEndDate", "status"
		};

		return db.getRecord( com.irt.rbm.usr.UserParty.createPrimary(partyId), fieldKeys );
	}

	protected Map<String, Object> getUserUserMap( Context ctx, String uniqId ) throws SQLException {
		com.irt.rbm.usr.UserUser db = new com.irt.rbm.usr.UserUser( ctx.handler );

		String[] fieldKeys = new String[] {
			"partyId", "partyName", "gln", "uniqId", "userId", "userName", "userClass", "serviceStartDate", "serviceEndDate", "status"
		};

		return db.getRecord( uniqId, fieldKeys );
	}

	protected boolean hasAuthorityTP( Context ctx, String authKey, String authValue ) throws SQLException {
		if( ctx.sessionMng.isAdminUser() ) return true;

		com.irt.rbm.ecs.TradePartnerAuth db = new com.irt.rbm.ecs.TradePartnerAuth( ctx.handler );

		Map<String, Object> conditionMap = new java.util.TreeMap<String, Object>();
		conditionMap.put( "tradeInfoType", "TP" );
		conditionMap.put( getTradePartnerRole(ctx) == ROLE_BUYER ? "buyerGln" : "sellerGln", ctx.sessionMng.getGln() );
		conditionMap.put( "authUserId", ctx.sessionMng.getUniqId() );
		conditionMap.put( authKey, authValue );

		return( db.getRecordCount(conditionMap) > 0 );
	}

	@Override
	public void init( javax.servlet.ServletConfig config ) throws ServletException {
		super.init( config );

		if( !com.irt.rbm.RBMSystem.initialized() ) {
			SQLHandler handler = null;
			com.irt.servlet.SystemConfig systemConfig = com.irt.custom.SystemConfig.getInstance( "RBM" );
			try {
				com.irt.rbm.RBMSystem.initSystemEnv( handler = systemConfig.createSQLHandler( systemConfig.getMessageHandler() ) );
			} catch( SQLException sqlEx ) {
				throw new ServletException( sqlEx );
			} finally {
				try { handler.close(); } catch( Exception ignored ) {}
			}
		}
	}

	protected static String makeRedirectURL( Context ctx, String redirectURL, String backURL ) {
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_LOCALE, ctx.req.getParameter("locale") );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_WINTYPE, ctx.pageConfig.getWindowType() );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_MENU, ctx.pageConfig.getSystemMenu() );
		if( backURL != null )
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_URL, java.net.URLEncoder.encode(backURL) );

		return redirectURL;
	}

	@Override
	protected void printErrorPage( final HttpServletRequest req, HttpServletResponse res, final String errorKey, final String message, final boolean sessionError )
						throws ServletException, IOException {

		PageConfig pageConfig = (PageConfig)req.getAttribute( "htmlpage" );
		if( pageConfig == null ) req.setAttribute( "htmlpage", pageConfig = createPageConfig(req) );
		pageConfig.setMessage( message );

		//setMaintenanceNotice( req, res, pageConfig.getHtmlPage().getLocale() );
		//String isMaintenanceWindow = (String) req.getAttribute( "isMaintenanceWindow" );

		String errorType = "";
		javax.servlet.RequestDispatcher rd;
//		if( "Y".equals(isMaintenanceWindow) ) {
//			errorType = "isMaintenance";
//			rd = req.getRequestDispatcher( systemConfig.getJspPath() + "/error_session2.jsp" );
		if( SessionManagerException.PASSWORD_EXPIRED.equals(errorKey) || sessionError ) {
			errorType = "isSessionerr";
			StringBuffer sbuf = req.getRequestURL();
			if( req.getQueryString() != null ) {
				String queryString = req.getQueryString().replaceAll( "<", "%3C" ).replaceAll( ">", "%3E" ).replaceAll( "'", "%27" );
				sbuf.append( "?"+ queryString );
			}

			pageConfig.setBackURL( sbuf.toString() );
			if( SessionManagerException.PASSWORD_EXPIRED.equals(errorKey) ) {
				com.irt.system.SessionManager sessionMng = (com.irt.system.SessionManager)req.getAttribute( "sessionMng" );
				Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
				recordMap.put( "partyId", sessionMng.getPartyId() );
				recordMap.put( "userId", sessionMng.getUserId() );
				req.setAttribute( "record", recordMap );
				rd = req.getRequestDispatcher( systemConfig.getJspPath() + "/error_passwd.jsp" );
			} else {
				rd = req.getRequestDispatcher( systemConfig.getJspPath() + "/error_session2.jsp" );
			}
		} else {
			errorType = "isError";
			rd = req.getRequestDispatcher( systemConfig.getJspPath() + "/error.jsp" );
		}
		res.setHeader("error-type", errorType);

		com.irt.dpr.util.Loggers.excwarn.warn(com.irt.dpr.util.Loggers.STR_EXCWARN, "{}", new java.util.HashMap<String, Object>() {{
			put("errorKey", errorKey);
			put("message", message);
			put("sessionError", sessionError);
		}});
		if( res.isCommitted() ) {
			logger.error( "response already been committed before reaching this method block.\n"
					+ "( errorKey: "+ errorKey + " message: " + message + " sessionError: "+ sessionError + " )\n"
					+ TraceHelper.formatCurrentStacktrace() );
		} else {
			rd.forward( req, res );
		}
	}

	@Override
	protected void printErrorPage( HttpServletRequest req, HttpServletResponse res, final Throwable throwable ) throws IOException, ServletException {
		PageConfig pageConfig = (PageConfig)req.getAttribute( "htmlpage" );
		if( pageConfig == null ) req.setAttribute( "htmlpage", pageConfig = createPageConfig(req) );
		pageConfig.setMessage( pageConfig.getMessageHandler().getMessage(ServletModelException.INTERNAL_ERROR) );
		pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );

		req.setAttribute( "throwable", throwable );

		String errorType = "";
		javax.servlet.RequestDispatcher rd = req.getRequestDispatcher( systemConfig.getJspPath() + "/error.jsp" );
		res.setHeader("errorType", errorType);

		com.irt.dpr.util.Loggers.excwarn.warn(com.irt.dpr.util.Loggers.STR_EXCWARN, "{}", new java.util.HashMap<String, Object>() {{
			put("message", throwable.getMessage());
		}}, throwable);
		if( res.isCommitted() ) {
			logger.error( "response already been committed before reaching this method block.\n" , throwable );
		} else {
			rd.forward( req, res );
		}
	}

	private List<Map<String, Object>> readMaintenanceXml( java.io.File xmlFileFullPath ) {
		org.dom4j.io.SAXReader reader = new org.dom4j.io.SAXReader();
		org.dom4j.Document doc = null;
		try {
			if( xmlFileFullPath != null && xmlFileFullPath.canRead() ) doc = reader.read( xmlFileFullPath );
		} catch ( org.dom4j.DocumentException e ) {
			logger.error(e);
		}

		List<Map<String, Object>> maintenanceNotices = null;
		if( doc != null ) {
			maintenanceNotices = new java.util.ArrayList<Map<String,Object>>();

			Map<String, Object> map = null;
			org.dom4j.Element root = doc.getRootElement();
			for( java.util.Iterator iterator = root.elementIterator(); iterator.hasNext(); ) {
				org.dom4j.Element elmt = (org.dom4j.Element) iterator.next();

				map = new java.util.HashMap<String,Object>();
				for( java.util.Iterator iterator2 = elmt.elementIterator(); iterator2.hasNext(); ) {
					org.dom4j.Element  elmt2 = (org.dom4j.Element) iterator2.next();
					String key = elmt2.getName();
					String value = elmt2.getText();
					map.put( key, value );
				}
				maintenanceNotices.add( map );
			}
		}
		return maintenanceNotices;
	}

	protected void setAttributeMaster( Context ctx, int type ) throws SQLException {
		com.irt.data.QueryableManager db;

		switch( type ) {
		case MASTER_COUNTRY:
			db = new com.irt.rbm.sys.CountryCode( ctx.handler );
			db.setSort( "name" );
			ctx.req.setAttribute( "countries", db.getRecords( null, new String[] {"code", "name"} ) );
			break;
		case MASTER_CURRENCY:
			db = new com.irt.rbm.sys.CurrencyCode( ctx.handler );
			db.setSort( "symbol" );
			ctx.req.setAttribute( "currencies", db.getRecords( null, new String[] {"code", "symbol", "name"} ) );
			break;
		case MASTER_LANGUAGE:
			db = new com.irt.rbm.sys.LanguageCode( ctx.handler );
			db.setSort( "name" );
			ctx.req.setAttribute( "languages", db.getRecords( null, new String[] {"code", "name"} ) );
			break;
		case MASTER_TIMEZONE:
			db = new com.irt.rbm.sys.TimeZone( ctx.handler );
			db.setSort( "displaySequence" );
			ctx.req.setAttribute( "timezones", db.getRecords( null, new String[] {"code", "name"} ) );
			break;
		}
	}

	protected void setAttributeTP( Context ctx, String authKey, String authValue ) throws SQLException {
		com.irt.data.QueryableManager db;

		Map<String, Object> conditionMap = new java.util.TreeMap<String, Object>();
		conditionMap.put( "tradeInfoType", "TP" );
		conditionMap.put( getTradePartnerRole(ctx) == ROLE_BUYER ? "buyerGln" : "sellerGln", ctx.sessionMng.getGln() );
		if( authKey == null || ctx.sessionMng.isAdminUser() )
			db = new com.irt.rbm.ecs.TradePartner( ctx.handler );
		else {
			db = new com.irt.rbm.ecs.TradePartnerAuth( ctx.handler );
			conditionMap.put( "authUserId", ctx.sessionMng.getUniqId() );
			conditionMap.put( authKey, authValue );
		}
		db.setSort( "buyerCompanyName", "sellerCompanyName" );

		List recordList = db.getRecords( conditionMap, new String[] { "buyerGln", "sellerGln", "buyerCompanyName", "sellerCompanyName" } );
		ctx.req.setAttribute( "partners", recordList );
	}

	protected static void setAttributeTPLower( Context ctx, Map<String, Object> conditionMap, String authKey, String authValue ) throws SQLException {
		com.irt.data.QueryableManager db;

		String parentBuyerGln, parentSellerGln;
		if( getTradePartnerRole(ctx) == ROLE_BUYER ) {
			parentBuyerGln = ctx.sessionMng.getGln();
			parentSellerGln = (String)conditionMap.get( "parentSellerGln" );
			if( parentSellerGln == null || parentSellerGln.length() == 0 ) parentSellerGln = null;
		} else {
			parentBuyerGln = (String)conditionMap.get( "parentBuyerGln" );
			parentSellerGln = ctx.sessionMng.getGln();
			if( parentBuyerGln == null || parentBuyerGln.length() == 0 ) parentBuyerGln = null;
		}

		// setAttribute( "partners_lw" )
		if( parentBuyerGln != null && parentSellerGln != null ) {
			conditionMap = new java.util.TreeMap<String, Object>();
			conditionMap.put( "parentBuyerGln", parentBuyerGln );
			conditionMap.put( "parentSellerGln", parentSellerGln );
			if( authKey != null && !ctx.sessionMng.isAdminUser() ) {
				conditionMap.put( "authUserId", ctx.sessionMng.getUniqId() );
				conditionMap.put( authKey, authValue );
			}

			db = new com.irt.rbm.ecs.TradePartner( ctx.handler );
			db.setSort( "buyerLocationName" );
			ctx.req.setAttribute( "partners_lw", db.getRecords(conditionMap, new String[] { "buyerGln", "buyerLocationName" }) );
		}

		// setAttribute( "locations_dc" )
		if( parentBuyerGln != null ) {
			conditionMap = new java.util.TreeMap<String, Object>();
			conditionMap.put( "parentGln", parentBuyerGln );
			conditionMap.put( "partyRole", com.irt.rbm.ecs.Party.PARTYROLE_RETAIL_DC );

			db = new com.irt.rbm.ecs.Party( ctx.handler );
			ctx.req.setAttribute( "locations_dc", db.getRecords(conditionMap, new String[] { "gln", "locationName" }) );
		}
	}

	protected void setMaintenanceNotice( Context ctx, HttpServletRequest req, HttpServletResponse res, java.util.Locale locale ) throws IOException, ServletException, SQLException {
		boolean isMaintenanceWindow = false;
		List<Map<String,Object>> maintenanceNotices = null;
		List<Map<String,Object>> list = null;

		String boardClassCode = systemConfig.getProperty( "systemNoticeClassCode" );
		if( boardClassCode != null ) {
			com.irt.sql.SQLHandler handler = null;
			try {
				com.irt.system.SystemConfig rbm_systemConfig = com.irt.custom.SystemConfig.getInstance( "RBM" );
				java.io.File xmlFileFullPath = new java.io.File( rbm_systemConfig.getTemporaryDirectory(), MAINTENANCE_NOTICE_FILENAME );

				try {
					handler = rbm_systemConfig.createSQLHandler( rbm_systemConfig.getMessageHandler(locale) );
				} catch( SQLException e ) {}

				if( handler == null ) {
					list = readMaintenanceXml( xmlFileFullPath );
				} else {
					com.irt.rbm.rbm.Board db = new com.irt.rbm.rbm.Board( handler );
					String[] fieldKeys = new String[] { "boardClassCode", "boardNumber", "title", "createDateTime", "boardOption2", "content"
														, "extraValue", "noticeStartDate", "noticeEndDate" };
					try {
						com.irt.data.Date monday = com.irt.data.Date.getInstance( new com.irt.data.Week().getFirstday().getTime() );

						java.util.Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
						conditionMap.put( "boardClassCode", boardClassCode );
						conditionMap.put( "noticeStartDate" + com.irt.data.Condition.SUFFIX_MAX_VALUE, new java.sql.Date(new java.util.Date().getTime()) );
						conditionMap.put( "noticeStartDate" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MAX );
						conditionMap.put( "noticeStartDate" + com.irt.data.Condition.SUFFIX_IS_NULL_OR, "Y" );
						conditionMap.put( "noticeStartDate" + com.irt.data.Condition.SUFFIX_IS_TIMESTAMP, "Y" );
						conditionMap.put( "noticeEndDate" + com.irt.data.Condition.SUFFIX_MIN_VALUE, new java.sql.Date(new java.util.Date().getTime()) ) ;
						conditionMap.put( "noticeEndDate" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MIN );
						conditionMap.put( "noticeEndDate" + com.irt.data.Condition.SUFFIX_IS_NULL_OR, "Y" );
						conditionMap.put( "noticeEndDate" + com.irt.data.Condition.SUFFIX_IS_TIMESTAMP, "Y" );
						list = db.getRecords( conditionMap, fieldKeys );
					} catch ( SQLException e ) {// 여기서 SQLException이 발생하는 경우는 Connection을 가져올 수 없을 경우.( DB다운 등 )
						list = readMaintenanceXml( xmlFileFullPath );
					}
				}

				if( list != null ) {
					maintenanceNotices = new java.util.ArrayList<Map<String,Object>>();
					for( Map<String,Object> obj: list ) {
						boolean loginBlock = "Y".equals( obj.get("boardOption2") );
						if( loginBlock ) {
							String extraValue = (String) obj.get( "extraValue" );
							if( extraValue == null || extraValue.length() == 0 ) continue;
							com.irt.data.Date noticeStartDate = tryGetDataDate(obj.get("noticeStartDate"));
							com.irt.data.Date noticeEndDate = tryGetDataDate(obj.get("noticeEndDate"));

							if( noticeStartDate == null || noticeEndDate == null ) continue;
							if( extraValue != null && extraValue.contains( "maintenanceStart" )
									&&  extraValue.contains( "maintenanceEnd" ) &&  extraValue.contains( "maintenanceTimeZone" ) ) {
								String[] kvs = extraValue.split( ";" );
								for (String kv2 : kvs) {
									String[] kv = kv2.split( "=" );
									obj.put( kv[0], kv[1] );
								}

								String maintenanceTZ = (String) obj.get( "maintenanceTimeZone" );
								if( maintenanceTZ == null || maintenanceTZ.length() == 0 ) maintenanceTZ = java.util.TimeZone.getDefault().getID();
								java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat( "yyyy-MM-dd HH:mm" );
								java.util.TimeZone tz = java.util.TimeZone.getTimeZone( maintenanceTZ );
								java.util.Calendar curr = java.util.Calendar.getInstance( tz );

								String maintenanceStart = (String) obj.get( "maintenanceStart" );
								String maintenanceEnd = (String) obj.get( "maintenanceEnd" );
								java.util.Calendar start = java.util.Calendar.getInstance( tz );
								java.util.Calendar end = java.util.Calendar.getInstance( tz );

								logger.debug( "maintenanceTimeZone=" + tz + "::maintenanceStart="+maintenanceStart + "::maintenanceEnd="+maintenanceEnd
										+ "::noticeStart="+noticeStartDate+"::noticeEnd="+noticeEndDate );
								try {
									start.setTime( sdf.parse(maintenanceStart) );
									end.setTime( sdf.parse(maintenanceEnd) );
								} catch ( java.text.ParseException e ) {}// maintenanceStart, maintenanceEnd가 null이거나 sdf 포맷에 맞지 않을 경우.

								if( curr.after( start ) && curr.before( end ) ) {
									UserParty partyDB = new UserParty( ctx.handler );
									String timezoneStr = (String) partyDB.getFieldValue( Record.createMap("partyId", ctx.sessionMng.getPartyId()), "timeZone" );
									TimeZone localTimeZone = TimeZone.getTimeZone( timezoneStr );
									sdf.setTimeZone( localTimeZone );

									obj.put( "maintenanceStartDateTime", sdf.format(start.getTime()) );
									obj.put( "maintenanceEndDateTime", sdf.format(end.getTime()) );
									obj.put( "timeZone", localTimeZone.getDisplayName(false, TimeZone.SHORT) );

									maintenanceNotices.add( obj );
								}
	/*
								com.irt.data.Date currDate = com.irt.data.Date.getInstance( curr.getTime() );
								if( currDate.after( noticeStartDate.getDate(-1) ) && currDate.before( noticeEndDate.getDate(1) ) )
									maintenanceNotices.add( obj );
									*/
							}
						}
					}
					req.setAttribute( "maintenanceNotices", maintenanceNotices );
/*					if( isMaintenanceWindow ) {
						req.setAttribute( "isMaintenanceWindow", "Y" );
						req.setAttribute( "maintenanceMessage"
								, rbm_systemConfig.getMessageHandler(locale).getMessage(MAINTENANCE_MESSAGE) ) ;
					}
					if( handler != null ) writeMaintenanceXml( maintenanceNotices, xmlFileFullPath );
*/
				}
			} finally {
				if( handler != null ) handler.close();
			}
		}
	}

	public void setPath( Context ctx, String... path ) {
		String[] menuPath = (String[]) ctx.req.getAttribute( "path" );

		for( int i = 0; i < path.length; i++ ) {
			String menuTitle = ctx.msghandler.getMessage( path[i] ).toUpperCase();
			if( menuPath != null ) {
				menuPath = Arrays.append( menuPath, menuTitle );
			} else {
				menuPath = new String[1];
				menuPath[0] = menuTitle;
			}
		}
		ctx.req.setAttribute( "path", menuPath );
	}

	/**
	 * if convert fail, then return null.
	 * @param dateObj [ String(yyyy-MM-dd) or java.util.Date or com.irt.data.Date ]
	 * @return may return null.
	 */
	private static com.irt.data.Date tryGetDataDate( Object dateObj ) {
		if( dateObj instanceof String ) {
			try {
				return com.irt.data.Date.getInstance((String)dateObj);
			} catch( java.text.ParseException e ) {
				e.printStackTrace();
			}
		} else if( dateObj instanceof com.irt.data.Date ) {
			return (com.irt.data.Date)dateObj;
		} else if( dateObj instanceof java.util.Date ) {
			return com.irt.data.Date.getInstance((java.util.Date)dateObj);
		}
		return null;
	}

	/**
	 * maintenanceNoticeList parameter에 값이 존재할 경우,
	 * 기존 xml파일과 maintenanceNoticeList의 boardNumber를 비교하여 변경되었는지 체크후에
	 *  - 변경되었으면 기존 xml파일 삭제후 재생성.
	 *  - 변경되지 않았으면 xml을 생성하지 않음.
	 * maintenanceNoticeList parameter가 null일 경우(database에 존재하지 않을 경우),
	 *  - notice가 종료된 것으로 인식하고 기존 xml파일 삭제. */
	private void writeMaintenanceXml( List<Map<String, Object>> maintenanceNoticeList, java.io.File xmlFileFullPath ) {
		if(maintenanceNoticeList == null || maintenanceNoticeList.isEmpty() ) {
			xmlFileFullPath.delete();
		} else {
			List<Map<String,Object>> saved_maintenanceList = null;
			saved_maintenanceList = readMaintenanceXml( xmlFileFullPath );

			java.util.ArrayList<String> saved_boardNumbers = new java.util.ArrayList<String>();
			java.util.ArrayList<String> param_boardNumbers = new java.util.ArrayList<String>();
			boolean isChanged = false;
			boolean isNew = true;
			if( saved_maintenanceList != null ) {
				String value = null;
				for( Map map : saved_maintenanceList ) {
					Object obj = map.get("boardNumber");
					if( obj != null ) {
						value = String.valueOf( obj );
						saved_boardNumbers.add(value);
					}
				}
				for( Map map : maintenanceNoticeList ) {
					Object obj = map.get("boardNumber");
					if( obj != null ) {
						value = String.valueOf( obj );
						param_boardNumbers.add(value);
					}
				}
				isChanged = saved_boardNumbers.retainAll(param_boardNumbers);
				isNew = saved_boardNumbers.size() == 0 && param_boardNumbers.size() > 0 ;
			}

			if( isNew || isChanged ) {
				if( isChanged ) xmlFileFullPath.delete();
				org.dom4j.Document doc = org.dom4j.DocumentHelper.createDocument();
				if( maintenanceNoticeList != null && !maintenanceNoticeList.isEmpty() ) {
					org.dom4j.Element maintenanceNotices = doc.addElement( "maintenanceNotices" );
					for (Map<String, Object> map : maintenanceNoticeList) {
						org.dom4j.Element maintenanceNotice = maintenanceNotices.addElement( "maintenanceNotice" );
						maintenanceNotice.addAttribute( "boardNumber", String.valueOf(map.get("boardNumber")) );

						java.util.Set<String> keySet = map.keySet();
						for (String key : keySet) {
							org.dom4j.Element elmt = maintenanceNotice.addElement( key );
							Object obj = map.get( key );
							String str = null;
							if( obj instanceof com.irt.data.Date )
								str = obj.toString();
							else
								str = String.valueOf( obj );
							elmt.setText( str );
						}
					}
					try {
						org.dom4j.io.XMLWriter output = new org.dom4j.io.XMLWriter( new java.io.FileWriter(xmlFileFullPath) );
						output.write( doc );
						output.close();
					} catch( IOException e ) {
						logger.error( e );
					}
				}
			}
		}
	}

	protected boolean jsonResponse( Context ctx, String... reqAttrKeys ) throws IOException {
		return jsonResponse(ctx, false, reqAttrKeys);
	}

	protected boolean jsonResponse( Context ctx, boolean useNullAsEmpty, String... reqAttrKeys ) throws IOException {
		ctx.res.setContentType("application/json; charset=utf-8");
		ctx.res.setCharacterEncoding("UTF-8");
		java.io.PrintWriter pw = ctx.res.getWriter();
		Map map = com.irt.data.Record.createMap("msg", ctx.pageConfig.getMessage());
		if( reqAttrKeys == null || reqAttrKeys.length == 0 ) {
			reqAttrKeys = ServletUtility2.parseJsonRequestAttr(ctx.req);
		}
		if( reqAttrKeys != null ) {
			for( String key : reqAttrKeys ) {
				Object obj = ctx.req.getAttribute(key);
				if( obj == null ) {
					if( useNullAsEmpty ) {
						obj = "";
					} else {
						continue;
					}
				}
				map.put(key, obj);
			}
		}

		String jsonString = Jsoner.getNewInstance().toJson(map);

		pw.write(jsonString);
		pw.flush();
		pw.close();

		return true;
	}

	protected void tryWorkbookAutoSizeColumn( DataWriter out, ColumnList columnList ) {
		if( out instanceof RBMWorkbook ) {
			Workbook workbook = ( (RBMWorkbook)out ).getWorkbook();
			for( int i = 0; i < columnList.getColumnCount(); i++ ) {
				String columnAttr = (String)columnList.getColumn(i).getColumnAttr();
				if( columnAttr != null ) {
					String classValue = StringUtil.extractAttrValue( columnAttr, "class" );
					String widthValue = StringUtil.extractAttrValue( columnAttr, "width" );
					if( "description".equals(widthValue) || "description".equals(classValue) ) {
						workbook.getSheetAt(0).autoSizeColumn(i);
					}
					workbook.getSheetAt(0).autoSizeColumn(i);
//					else if( widthValue != null && widthValue.length() > 0 ) {
//						int colWidth = 0;
//						try {
//							colWidth = Integer.parseInt(widthValue);
//						} catch( NumberFormatException ignored ) {
//						}
//						org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
//						int shCurWidth = sheet.getColumnWidth(i);
//						double shWidthPixel = org.apache.poi.ss.util.SheetUtil.getColumnWidth(sheet, i, false);
//						if( shWidthPixel > colWidth/5 ) {
//							sheet.setColumnWidth(i, new Double(shWidthPixel).intValue());
//						}
//					}
				}
			}
		}
	}
}
