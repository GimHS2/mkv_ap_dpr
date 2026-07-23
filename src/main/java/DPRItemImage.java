/*
 *	File Name:	DPRItemImage.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	keehe		2008/09/26		2.2.0	create
 *
**/

import com.irt.dpr.ItemImage;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.servlet.*;
import java.io.IOException;
import java.io.File;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRItemImage"})
public class DPRItemImage extends DPRServletModel {
	public final static String MODE_IMAGE				= "img";
	public final static int MAX_FILESIZE_KB				= 50;

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
    }

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode(ctx)  );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );
		Condition.putConditionValueOnly( conditionMap, "displayType", ItemImage.DEFAULT_DISPLAYTYPE );
		setDefaultParameter( ctx, conditionMap );

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_IMAGE.equals(ctx.mode) )
			return image( ctx );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	protected boolean image( Context ctx ) throws IOException, ServletException, SQLException {
		ItemImage db = (ItemImage)ctx.db;

		String countryCode = ctx.req.getParameter( "countryCode" );
		String organizationCode = getSavedOrganizationCode( ctx );
		String itemCode = ctx.req.getParameter( "itemCode" );
		Map<String, Object> primaryMap = ItemImage.createPrimary( countryCode, organizationCode, itemCode, ItemImage.DEFAULT_DISPLAYTYPE );
		String imageType = (String)db.getFieldValue( primaryMap, "imageType" );

		if( imageType == null )
			imageType = "image/gif";

		ctx.res.setContentType( imageType );
		java.io.OutputStream out = ctx.res.getOutputStream();
		try {
			db.writeFile( primaryMap, out );
		} finally {
			try { out.close(); } catch( Exception ex ) {}
		}

		return true;
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemImage.INF" );
		else if( MODE_IMAGE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemImage.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemImage.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemImage.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemImage.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new ItemImage( ctx.handler );

		String messageKey = "TITLE_DPR_ITEM_IMAGE_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_MATERIALIMAGE" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		ItemImage db = (ItemImage)ctx.db;
		// Condition Map
		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRItemImage%LIST" );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );

		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() || ctx.sessionMng.isAuthorized("DPR", "DPRItemImage.MNG")) ctx.pageConfig.setManageAuth( true );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_itemimage_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRItem.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((ItemImage)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput ( Context ctx ) throws IOException, ServletException, SQLException {
		ItemImage db = (ItemImage)ctx.db;

		/* main table에 bindVariable를 해야 하기 때문에 꼭! createConditionMap을 통해 getRecord를 할 것!*/
		Map<String, Object> conditionMap = createConditionMap( ctx );
		if( !conditionMap.containsKey("itemCode") )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> recordMap = db.getRecord( conditionMap );

		if( recordMap == null ) {
			recordMap = new com.irt.dpr.ItemDesc(ctx.handler).getRecord( com.irt.dpr.ItemDesc.extractPrimaryMap(conditionMap), new String[] { "itemCode", "itemName" } );
		}

		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

		ctx.pageConfig.setManageAuth( true );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_itemimage_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		ItemImage db = (ItemImage)ctx.db;

		String[] itemCodes = ctx.req.getParameterValues( "itemCode" );
		String[] countryCodes = ctx.req.getParameterValues( "countryCode" );
		String organizationCode = getSavedOrganizationCode( ctx );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
        else if( countryCodes == null || countryCodes.length == 0 )
            throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
        else if( itemCodes == null || itemCodes.length == 0 )
            throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "displayType", ItemImage.DEFAULT_DISPLAYTYPE );

		int count = 0;
		List<Map<String, Object>> errorList = new java.util.LinkedList<Map<String, Object>> ();
		for( int i = 0; i < itemCodes.length; i++ ) {
			primaryMap.put( "itemCode", itemCodes[i] );
			primaryMap.put( "countryCode", countryCodes[i] );
			primaryMap.put( "organizationCode", organizationCode );

			try {
				if( !db.deleteImage(primaryMap) ) {
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_DELETE );
				}

				count++;
				ctx.handler.commit();
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(itemCodes[i], dataEx) );
			}
		}

		// forward & sendRedirect
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		ItemImage db = (ItemImage)ctx.db;

		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		if( recordMap.get("itemCode") == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		recordMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );

/* Image가 등록 되어 있는지 확인 후에 있으면 삭제후에 등록 */
		try {
			if( !(ctx.req instanceof MultipartHttpRequest) )
				throw new ServletModelException( ServletModelException.INTERNAL_ERROR );

			MultipartHttpRequest req = (MultipartHttpRequest)ctx.req;

			String fileContentType = req.getFileContentType( "itemImageFile" );
			String imageExtention = null;
			if( (imageExtention = ItemImage.checkContentTypeAndGetFileExtention(fileContentType)) == null )
				throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_INVALID_FILETYPE") );

			File imageFile = getRequestImageFile( ctx, req, recordMap );
			if( imageFile == null )
				throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_INVALID_IMAGE_FILE") );

			String originalFileName = req.getInputFileName( "itemImageFile" );
			int point = originalFileName.lastIndexOf( "." );
			if( point <= 0 )
				throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_INVALID_IMAGE_FILENAME") );
			else
				recordMap.put( "imageFileName", originalFileName.substring(0, point) );

			recordMap.put( "inputImageFile", imageFile );
			recordMap.put( "imageType", fileContentType );
			recordMap.put( "imageExtention", (imageExtention != null ? imageExtention.toUpperCase() : null) );
			recordMap.put( "countryCode", getUserCountryCode(ctx) );
			recordMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
			recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );

			boolean imageExisting = db.checkExistingImage( ItemImage.extractPrimaryMap(recordMap) );
			boolean ret = true;
			if( imageExisting )
				ret = db.deleteImage( ItemImage.extractPrimaryMap(recordMap) );

			if( ret ) {
				if( !db.update( recordMap ) )
					throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_NO_RECORD_UPDATE") );

				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_IMAGE_REGIST_SUCCESS") );
			}

			Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ( recordMap );
			ctx.req.setAttribute( "record", db.getRecord(conditionMap) );

			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INPUT );
			ctx.pageConfig.setManageAuth( true );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_itemimage_input.jsp" );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
		}

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ( recordMap );
		ctx.req.setAttribute( "record", db.getRecord(conditionMap) );

		ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ITEM_IMAGE_"+ ctx.mode.toUpperCase()) );

		ctx.pageConfig.setMode( ctx.mode = MODE_REGISTINPUT );

		return registInput( ctx );
	}

	private File getRequestImageFile( Context ctx, MultipartHttpRequest req, Map<String, Object> recordMap ) throws DataException {
		File imageFile = null;

		try {
			RandomAccessFile file = new RandomAccessFile( (imageFile = req.getFile("itemImageFile")), "r" );
			try {
				if( file.length() > MAX_FILESIZE_KB * 1024 )
					throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_LARGE_FILESIZE") );

				recordMap.put( "imageSize", String.valueOf(file.length() / 1024) );
			} catch( java.io.IOException ioEx ) {
				throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_INVALID_IMAGE_FILE") );
			} finally {
				try { file.close(); } catch( Exception ex ) {}
			}
		} catch( java.io.FileNotFoundException fileEx) {
			throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_INVALID_IMAGE_FILE") );
		}

		return imageFile;
	}
}
