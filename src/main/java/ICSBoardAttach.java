/*
 *	File Name:	ICSBoardAttach.java
 *	Version:	2.2.3c(dpr)
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.3c	신규 UI/UX 적용
 *	jbaek		2016/08/31		2.2.3	add contentType, add json responseText
 *	yjcha		2011/08/31		2.2.2	initContext(): SystemPackageCode 변경
 *	stghr12		2010/07/31		2.2.1	version up
 *	lsinji		2009/10/25		2.2.0	create
 *
**/

import com.irt.data.DataException;
import com.irt.html.HtmlUtility;
import com.irt.ics.BoardAttach;
import com.irt.servlet.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/ICSBoardAttach"})
public class ICSBoardAttach extends ICSBoardServlet {
	public final static String MODE_SHOW					= "show";

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_SHOW.equals(ctx.mode) ) return download( ctx, false );
		return super.doRequest( ctx, isPost );
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		return download( ctx, true );
	}

	protected boolean download( Context ctx, boolean downloading ) throws IOException, ServletException, SQLException {
		BoardAttach db = (BoardAttach)ctx.db;

		String attachManageKey = ctx.req.getParameter( "attachManageKey" );
		int attachNumber = getNumberParameter( ctx, "attachNumber", true );
		if( attachManageKey == null || attachManageKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> recordMap = db.getRecord( BoardAttach.createPrimary(attachManageKey, attachNumber) );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		else if( !isAuthorized(ctx, (String)recordMap.get("boardClassCode"), "INF") )
			return false;

		String contentType = (String)recordMap.get( "contentType" );
		String fileName = (String)recordMap.get( "fileName" );
		String serverFilePath = (String)recordMap.get( "filePath" );
		String serverFileName = (String)recordMap.get( "serverFileName" );
		if( contentType == null )
			throw new ServletModelException( "MSG_ICS_BOARD_FILE_NOT_FOUND" );
		else if( serverFilePath == null || serverFilePath.length() == 0 )
			throw new ServletModelException( "MSG_ICS_BOARD_FILE_NOT_FOUND" );
		else if( serverFileName == null || serverFileName.length() == 0 )
			throw new ServletModelException( "MSG_ICS_BOARD_FILE_NOT_FOUND" );

		File serverFile = new File( serverFilePath, serverFileName );
		if( !serverFile.exists() )
			throw new ServletModelException( "MSG_ICS_BOARD_FILE_NOT_FOUND" );

		java.io.InputStream inputStream = null;
		java.io.OutputStream outputStream = null;
		try {
			int length;
			byte[] buffer = new byte[1024 * 10];
			inputStream = new java.io.FileInputStream( serverFile );

			if( downloading ) {
				ctx.res.setContentType( "application/smnet" );
				ctx.res.setHeader( "Content-Disposition", "attachment; filename="+ java.net.URLEncoder.encode(fileName, "utf-8") +";" );
				ctx.res.setHeader( "Set-Cookie", "fileDownload=true;" );
			} else {
				ctx.res.setContentType( contentType );
				ctx.res.setHeader( "Set-Cookie", "fileDownload=true;" );
			}

			outputStream = ctx.res.getOutputStream();
			while( (length = inputStream.read(buffer, 0, buffer.length)) != -1 )
				outputStream.write( buffer, 0, length );
		} finally {
			try { inputStream.close(); } catch( Exception ignored ) {}
			try { outputStream.close(); } catch( Exception ignored ) {}
		}

		return true;
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// boardClassCode
		String boardClassCode = ctx.req.getParameter( "boardClassCode" );
		if( boardClassCode == null || boardClassCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		pageConfig.setProperty( "boardClassCode", boardClassCode );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_SHOW );
		if( ATTACHFILE_PATH == null )
			throw new ServletModelException( "ERR_ICS_NOT_SUPPORT_ATTACHFILE" );
		else if( MODE_REGIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) || MODE_SHOW.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "ICS", "ICSBoard."+ boardClassCode +".INF" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new BoardAttach( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_ICS_BOARD_ATTACH_"+ ctx.mode.toUpperCase()) );
	}

	private boolean isSupportImageMIMEType( String contentType ) {
		return ( "image/gif".equals(contentType)
				|| "image/x-png".equals(contentType) || "image/png".equals(contentType)
				|| "image/pjpeg".equals(contentType) || "image/jpeg".equals(contentType) );
	}

	@Override
	protected boolean regist( Context ctx ) throws IOException, ServletException, SQLException {
		BoardAttach db = (BoardAttach)ctx.db;

		String attachManageKey = ctx.req.getParameter( "attachManageKey" );
		String boardClassCode = ctx.req.getParameter( "boardClassCode" );
		int boardNumber = getNumberParameter( ctx, "boardNumber", false );

		if( !(ctx.req instanceof MultipartHttpRequest) )
			throw new ServletModelException( ServletModelException.INVALID_REQUEST );
		else if( attachManageKey == null || attachManageKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( boardClassCode == null || boardClassCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		MultipartHttpRequest req = (MultipartHttpRequest)ctx.req;

		String[] paramNames = req.getFileNames();
		if( paramNames == null || paramNames.length != 1 )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		String paramName = paramNames[0];

		File imageFile = req.getFile( paramName );
		long imageFileSize = ( imageFile.length() / 1024L );
		if( !isSupportImageMIMEType(req.getFileContentType(paramName)) )
			throw new ServletModelException( "ERR_ICS_NOT_SUPPORT_IMAGE_EXTENSION" );
		else if( imageFileSize > MAX_IMAGE_FILESIZE_KB )
			throw new ServletModelException( "ERR_ICS_EXCEED_MAX_IMAGE_FILESIZE@"+ MAX_IMAGE_FILESIZE_KB );

		Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
		recordMap.put( "boardClassCode", boardClassCode );
		recordMap.put( "attachManageKey", attachManageKey );
		if( boardNumber >= 0 )
			recordMap.put( "boardNumber", new Integer(boardNumber) );
		recordMap.put( "contentType", req.getFileContentType(paramName) );
		recordMap.put( "fileType", BoardAttach.ATTACHTYPE_IMAGE );
		recordMap.put( "fileName", req.getInputFileName(paramName) );
		recordMap.put( "filePath", ATTACHFILE_PATH );
		recordMap.put( "fileSize", Long.valueOf(imageFileSize) );

		while( true ) {
			try {
				int attachNumber = db.getNextAttachNumberByManageKey( attachManageKey );
				recordMap.put( "serverFileName", attachManageKey +"_"+ attachNumber );
				recordMap.put( "attachNumber", Integer.valueOf(attachNumber) );
				db.regist( recordMap, imageFile );

				String reqUploadFn = ctx.req.getParameter("reqUploadFn");
				String imgAttachManageUrl = ctx.req.getParameter("imgAttachManageUrl");
				if( reqUploadFn != null && reqUploadFn.length() > 0
						&& imgAttachManageUrl !=null && imgAttachManageUrl.length() > 0
						&& "mediumInsert".equals(reqUploadFn) ) {
					ctx.res.setContentType( "application/json;charset=utf-8" );
					java.io.PrintWriter out = ctx.res.getWriter();
					String responseText = "{\"files\":[{"
							+ "\"url\":\"" +imgAttachManageUrl+ "&attachNumber=" +attachNumber+ "\","
							+ "\"attachManageKey\":\"" +attachManageKey+ "\","
							+ "\"attachNumber\":\""+ attachNumber+ "\"}]}";
					out.println( responseText );
					out.flush();
				} else {
					ctx.res.setContentType( "text/html;charset=utf-8" );
					java.io.PrintWriter out = ctx.res.getWriter();
					out.print( "IMAGE: "+ attachManageKey +";"+ attachNumber );
					out.flush();
				}

				return true;
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				if( !DataException.ERR_UNIQUE_CONSTRAINT.equals(dataEx.getErrorKey()) ) {
					ctx.pageConfig.setMessage( dataEx.getMessage() );
					logger.info( "error.", dataEx );

					return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
				}
			}
		}
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		BoardAttach db = (BoardAttach)ctx.db;

		String attachManageKey = HtmlUtility.checkXSS( ctx.req.getParameter("attachManageKey") );
		int attachNumber = getNumberParameter( ctx, "attachNumber", true );
		if( attachManageKey == null || attachManageKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> recordMap = db.getRecord( BoardAttach.createPrimary(attachManageKey, attachNumber) );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		else if( !isAuthorized(ctx, (String)recordMap.get("boardClassCode"), "MNG") )
			return false;

		try {
			db.delete( recordMap );

			ctx.res.setContentType( "text/html;charset=utf-8" );
			java.io.PrintWriter out = ctx.res.getWriter();
			out.print( "IMAGE: "+ attachManageKey +";"+ attachNumber );
			out.flush();

			return true;
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			logger.info( "error.", dataEx );

			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		}
	}
}
