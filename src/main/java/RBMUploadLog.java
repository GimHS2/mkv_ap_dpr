/*
 *	File Name:	RBMUploadLog.java
 *	Version:	2.2.3c(dpr)
 *
 *	Description:
 *
 *	Note:
 *		pub_list_count.jsp
 *		rbm_uploadlog_list.jsp
 *		systemConfig.getProperty( "uploadPath" )
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/10/30		2.2.3c	S3 Storage 방식 추가
 *	hankalam	2019/08/31		2.2.2c	dpr customize
 *	jbaek		2017/09/30		2.2.2	copied from rbm2: fileType and fileName
 *	stghr12		2008/05/31		2.2.1	조회권한 처리
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataLoader;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.*;
import com.irt.util.S3Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/RBMUploadLog"})
public class RBMUploadLog extends DPRServletModel {
	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		String logId = conditionMap.getParameter( "logId" );
		if( logId != null )
			Condition.putConditionValueOnly( conditionMap, "logId", logId );

		if( ctx.sessionMng.isAuthorized("RBM", "RBMUploadLog.LST_ALL") ) {
			Condition.putConditionValueOnly( conditionMap, "partyId", ctx.sessionMng.getPartyId() );
		} else
			Condition.putConditionValueOnly( conditionMap, "userId", ctx.sessionMng.getUniqId() );

		if( !ctx.sessionMng.isSystemAdmin() ) {
			UploadLog db = (UploadLog)ctx.db;

			List<String> uploadTypeList = new java.util.ArrayList();
			String[] uploadTypes = conditionMap.getParameterValues( "uploadType" );
			if( uploadTypes != null ) {
				for( int i = 0; i < uploadTypes.length; i++ )
					if( ctx.sessionMng.isAuthorized("RBM", "RBMUploadLog.TYPE."+ uploadTypes[i]) )
						uploadTypeList.add( uploadTypes[i] );
			}

			if( uploadTypeList.size() == 0 ) {
				for( Map<String, Object> uploadTypeMap : db.getUploadTypes() ) {
					if( ctx.sessionMng.isAuthorized("RBM", "RBMUploadLog.TYPE."+ uploadTypeMap.get("code")) )
						uploadTypeList.add( (String)uploadTypeMap.get("code") );
				}
			}

			if( uploadTypeList.size() == 0 )
				Condition.putConditionValueOnly( conditionMap, "uploadType", " " );
			else if( uploadTypeList.size() == 1 )
				Condition.putConditionValueOnly( conditionMap, "uploadType", uploadTypeList.get(0) );
			else
				Condition.putConditionValueOnly( conditionMap, "uploadType", uploadTypeList.toArray( new String[uploadTypeList.size()] ) );
		}
		return conditionMap;
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		UploadLog db = (UploadLog)ctx.db;

		String logId = ctx.req.getParameter( "logId" );
		if( logId == null || logId.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		Map<String, Object> primaryMap = UploadLog.createPrimary( logId );
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

		if( !ctx.sessionMng.isSystemAdmin() ) {
			if( !ctx.sessionMng.getPartyId().equals(recordMap.get("partyId")) )
				throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
			else if( !ctx.sessionMng.getUniqId().equals(recordMap.get("userId")) ) {
				if( !ctx.sessionMng.isAuthorized("RBM", "RBMUploadLog.LST_ALL") )
					throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
			}
		}

		boolean isS3Storage = "Y".equals( systemConfig.getProperty("s3Storage") );
		String uploadPath = systemConfig.getProperty( "uploadPath" );
		String encoding = (String)recordMap.get( "encoding" );
		String fileName = (String)recordMap.get( "fileName" );
		String fileType = (String)recordMap.get( "fileType" );

		String uploadFileName = (String)recordMap.get( "uploadFileName" );
		if( fileName == null || (!isS3Storage && uploadPath == null) )
			throw new ServletModelException( "MSG_RBM_CANNOT_FIND_UPLOADFILE" );

		String downloadType = ctx.req.getParameter( "downloadType" );
		fileName = fileName + "." + com.irt.util.RBMWorkbook.getFileExtension( fileType ) + ( "err".equals(downloadType) ? ".err" : "" );
		java.io.File downloadFile = null;
		if( isS3Storage ) {
			if( !S3Service.s3Instance.existFile(fileName) ) {
				throw new ServletModelException( "MSG_RBM_CANNOT_FIND_UPLOADFILE" );
			}

		} else {
			downloadFile = new java.io.File( uploadPath, fileName );
			if( !downloadFile.exists() )
				throw new ServletModelException( "MSG_RBM_CANNOT_FIND_UPLOADFILE" );
		}

		ctx.res.setContentType( com.irt.util.RBMWorkbook.getResponseContentType( fileType ) );
		if( uploadFileName == null )
			uploadFileName = fileName;
		else if( encoding != null ) {
			try {
				uploadFileName = new String( uploadFileName.getBytes(encoding), "8859_1" );
			} catch( java.io.UnsupportedEncodingException encodeEx ) {
				uploadFileName = fileName;
			}
		}
		ctx.res.setHeader( "Content-Disposition", "attachment; filename="+ uploadFileName +";" );

		java.io.OutputStream outputStream = ctx.res.getOutputStream();
		if( isS3Storage ) {
			try {
				S3Service.s3Instance.download( outputStream, fileName );
			} finally {
				try { outputStream.close(); } catch( Exception ignored ) {}
			}
		} else {
			java.io.InputStream inputStream = new java.io.FileInputStream( downloadFile );
			try {
				int length;
				byte[] buffer = new byte[1024 * 10];

				while( (length = inputStream.read(buffer, 0, buffer.length)) != -1 )
					outputStream.write( buffer, 0, length );
			} finally {
				try { outputStream.close(); } catch( Exception ignored ) {}
				try { inputStream.close(); } catch( Exception ignored ) {}
			}
		}

		return true;
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "RBM" );
	}

	protected static String getUploadLogURL( Context ctx, DataLoader dataLoader, String classURL, String uploadInputPath ) {
		String backURL;

		if( uploadInputPath == null )
			backURL = ctx.pageConfig.getBackURL();
		else
			backURL = makeRedirectURL( ctx, classURL +"/"+ uploadInputPath, ctx.pageConfig.getBackURL() );

		String redirectURL = classURL +"/RBMUploadLog?"+ PARAM_MODE +"="+ MODE_LIST +"&logId="+ dataLoader.getLoaderLogId();
		if( dataLoader.getDataResult().getErrorCount() > 0 )
			redirectURL += "&status=ER";

		return makeRedirectURL( ctx, redirectURL, backURL );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMUploadLog.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", null );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "RBM", "RBMUploadLog.LST" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new UploadLog( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_RBM_UPLOADLOG_LIST") );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		UploadLog db = (UploadLog)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList;

		List recordList;
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		String logId = (String)conditionMap.get( "logId" );
		if( logId != null ) {
			Map<String, Object> primaryMap = UploadLog.createPrimary( logId );
			Map<String, Object> recordMap = db.getRecord( primaryMap );
			if( recordMap == null )
				throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

			if( !ctx.sessionMng.isSystemAdmin() ) {
				if( !ctx.sessionMng.getPartyId().equals(recordMap.get("partyId")) )
					throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
				else if( !ctx.sessionMng.getUniqId().equals(recordMap.get("userId")) ) {
					if( !ctx.sessionMng.isAuthorized("RBM", "RBMUploadLog.LST_ALL") )
						throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
				}
			}
			ctx.req.setAttribute( "record_log", recordMap );
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_RBM_UPLOADLOG_DETAIL") );

			columnList = getColumnList( ctx, "RBMUploadLogDetail%LIST" );
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			recordList = db.getDetails( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		} else {
			columnList = getColumnList( ctx, "RBMUploadLog%LIST" );
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		}
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		List<Map<String, Object>> uploadTypeList = db.getUploadTypes();
		if( uploadTypeList != null && !ctx.sessionMng.isSystemAdmin() ) {
			for( java.util.Iterator<Map<String, Object>> iterator = uploadTypeList.iterator(); iterator.hasNext(); ) {
				Map<String, Object> uploadTypeMap = iterator.next();
				if( !ctx.sessionMng.isAuthorized("RBM", "RBMUploadLog.TYPE."+ uploadTypeMap.get("code")) )
					iterator.remove();
			}
		}
		ctx.req.setAttribute( "uploadtypes", uploadTypeList );

		// setAttribute()
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		return forward( ctx, systemConfig.getJspPath() + "/rbm_uploadlog_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("RBM", "RBMUploadLog.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}

		String logId = (String)conditionMap.get( "logId" );
		if( logId == null )
			ctx.pageConfig.getListIndexVariables()[2] = ((UploadLog)ctx.db).getRecordCount( conditionMap );
		else
			ctx.pageConfig.getListIndexVariables()[2] = ((UploadLog)ctx.db).getDetailCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}
}
