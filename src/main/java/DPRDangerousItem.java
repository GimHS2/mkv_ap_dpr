/*
 *	File Name:	DPRDangerousItem.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		pub_upload_input.jsp
 *		dpr_dangerousitem_input.jsp
 *		dpr_dangerousitem_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	hankalam	2020/06/30		2.2.0	create
 *
**/

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.Country;
import com.irt.dpr.FreeGoods;
import com.irt.dpr.Item;
import com.irt.dpr.ItemMasterPlant;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.sql.SQLHandler;


/*
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRDangerousItem"})
public class DPRDangerousItem extends DPRServletModel {
	protected Map<String, Object> createConditionMap ( Context ctx ) throws SQLException, IOException, ServletException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );
		//Condition.putConditionValueOnly( conditionMap, "dangerousNumber", Item.ITEMTYPE_DANGEROUS );
		Condition.putConditionValueOnly( conditionMap, "loadingGroup", Item.ITEMTYPE_DANGEROUS );
		String organizationCode = getSavedOrganizationCode( ctx );
		String dangerousPlant = Country.getFeatureValue( organizationCode, "dangerousPlant" );
		if( dangerousPlant != null ) {
			Condition.putConditionValueOnly( conditionMap, "plantCode", dangerousPlant );
		} else {
			throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_NEEDED_ENVIRONMENT", "dangerousPlant") );
		}
		return conditionMap;
	}

	@Override
	protected boolean download( Context ctx) throws IOException, ServletException, SQLException {
		ItemMasterPlant db = (ItemMasterPlant) ctx.db;
		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRDangerousItem%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String filename = ctx.msghandler.getMessage( "TITLE_DPR_DANGEROUSITEM_" );
		DataWriter out = createTextDataWriter( ctx, filename );

		try{
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			db.write( out, conditionMap, columnList, FreeGoods.OPT_WRITING_TITLE );
		} catch ( SQLException sqlEx ){
			out.println();
			out.print( sqlEx.getMessage() );
			logger.error("internal Error", sqlEx );
		}finally {
			out.flush();
			out.close();
		}
		return true;
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		ItemMasterPlant db = (ItemMasterPlant)ctx.db;

		String itemCode = ctx.req.getParameter( "itemCode" );
		String organizationCode = getSavedOrganizationCode( ctx );
		String plantCode = Country.getFeatureValue( organizationCode, "dangerousPlant" );

		if( itemCode == null || itemCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( plantCode == null || plantCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );


		Map<String, Object> primaryMap = ItemMasterPlant.createPrimary( itemCode, plantCode );
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

		ctx.req.setAttribute( "record", recordMap );

		if( inputting ) {
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			ctx.req.setAttribute( "fieldSet_new", db.getFieldSet(false) );
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRDangerousItem.INF") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_dangerousitem_input.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRDangerousItem.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRDangerousItem.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRDangerousItem.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRDangerousItem.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRDangerousItem.MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRDangerousItem.MNG" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRDangerousItem.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new ItemMasterPlant( ctx.handler );

		String messageKey = "TITLE_DPR_DANGEROUSITEM_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_DANGEROUSMATERIAL" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		ItemMasterPlant db = (ItemMasterPlant)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRDangerousItem%LIST" );

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );
		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRDangerousItem.MNG") );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_dangerousitem_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRDangerousItem.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((ItemMasterPlant)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> recordMap = (Map<String, Object>) ctx.req.getAttribute( "record" );
		if( recordMap == null ) {
			recordMap = new java.util.HashMap<String, Object>();
		}
		recordMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
		ctx.req.setAttribute( "record", recordMap );

		ctx.req.setAttribute( "fieldSet", ((ItemMasterPlant)ctx.db).getFieldSet(true) );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_dangerousitem_input.jsp");
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		ItemMasterPlant db = (ItemMasterPlant)ctx.db;

		String[] itemCodes = ctx.req.getParameterValues( "itemCode" );
		String[] plantCodes = ctx.req.getParameterValues( "plantCode" );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( itemCodes == null || itemCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( plantCodes == null || plantCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		// 레코드 삭제
		int count = 0;
		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < itemCodes.length; i++) {
			primaryMap = ItemMasterPlant.createPrimary( itemCodes[i], plantCodes[i] );
			try {
				if( db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getFieldValue(primaryMap, "itemCode"), dataEx) );
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
		ItemMasterPlant db = (ItemMasterPlant)ctx.db;

		Map<String, Object> recordMap = new ParameterMap( ctx.req, true );
		String itemCode = (String)recordMap.get( "itemCode" );
		String manualInd = (String)recordMap.get( "manualInd" );

		if( itemCode == null || itemCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( manualInd == null || manualInd.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		recordMap.put( "loadingGroup", Item.ITEMTYPE_DANGEROUS );
		String plantCode = (String)recordMap.get( "plantCode" );
		if( plantCode == null ) {
			String organizationCode = getSavedOrganizationCode( ctx );
			String dangerousPlant = Country.getFeatureValue( organizationCode, "dangerousPlant" );
			if( dangerousPlant == null ) {
				throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_NEEDED_ENVIRONMENT", "dangerousPlant") );
			}
			recordMap.put( "plantCode", dangerousPlant );
		}

		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );

		try {
			int recordCount = db.getRecordCount( ItemMasterPlant.createPrimary(itemCode, plantCode) );
			if( recordCount > 0 ) {
				if( !db.modify(recordMap) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			} else {
				db.regist( recordMap );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
			}

			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_dangerousitem_input.jsp" );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
		} catch ( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
		}

		String mode = ( inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_DANGEROUSITEM_"+ mode.toUpperCase()) );

		return registInput(ctx);
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		UploadLog logDB = new UploadLog( ctx.handler );

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRDangerousItem.MNG") )
			throw new ServletModelException( ServletModelException.HAS_NOAUTH );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRDangerousItem%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			String organizationCode = getSavedOrganizationCode( ctx );
			String dangerousPlant = Country.getFeatureValue( organizationCode, "dangerousPlant" );
			if( dangerousPlant != null ) {
				defaultMap.put( "plantCode", dangerousPlant );
				defaultMap.put( "loadingGroup", Item.ITEMTYPE_DANGEROUS );
			} else {
				throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_NEEDED_ENVIRONMENT", "dangerousPlant") );
			}

			String uploadType = "DAI";
			ItemMasterPlant db = (ItemMasterPlant) ctx.db;
			RecordFormat lineNameFormat;

			String[] updateFieldKeys = { "loadingGroup", "manualInd", "updateUserId" };

			loader = db.createDataLoader( fieldKeys, defaultMap, updateFieldKeys, Record.INSERT | Record.UPDATE );
			lineNameFormat = PatternRecordFormat.getInstance( "[${itemCode}]" );

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "DPR" );
			resultMap.put( "uploadType", uploadType );
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			RecordFormat messageFormat = PatternRecordFormat
				.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );

			loaderLogger = logDB.createLogger( resultMap, messageFormat, lineNameFormat );
			DataLoader dataLoader = createTextDataLoader( ctx, ctx.handler, loader, loaderLogger, validator, true );

			try {
				DataReader reader = dataLoader.getDataReader();
				java.io.PrintStream out = dataLoader.getErrorPrintStream();
				for( int i = 0; i < com.irt.data.cols.ColumnUtility.getTitleRowCount(columnList) && !reader.isEOF(); i++ ) {
					try {
						reader.readNext();
					} catch( DataException dataEx ) {}
					if( reader.getLineString() != null && out != null )
						out.println( reader.getLineString() );
				}

				reader.setTrim(true);

				dataLoader.execute();
				ctx.pageConfig.setMessage( (String)loaderLogger.getResultMap().get("message") );
			} finally {
				dataLoader.close( false );
			}
			loader = null;

			String uploadInputPath = "DPRDangerousItem?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT;
			String redirectURL = RBMUploadLog.getUploadLogURL( ctx, dataLoader, systemConfig.getClassURL(), uploadInputPath );

			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		} finally {
			try { if( loader != null ) loader.close(); } catch( Exception ignored ) {}
		}
	}

	@Override
	protected boolean uploadInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		recordMap.put( "encoding", "UTF8" );
		ctx.req.setAttribute( "record", recordMap );
		return forward( ctx, systemConfig.getJspPath() + "/pub_upload_input.jsp" );
	}


	protected int getAuthLevel( SQLHandler handler, String userId, String countryCode, String organizationCode, boolean isAdminUser, Map<String, Object> recordMap )
						throws SQLException {
		if( countryCode == null ) return AUTHLEVEL_NOAUTH;
		if( organizationCode == null ) return AUTHLEVEL_NOAUTH;

		if( isAdminUser ) {
			Object upOrgCode = recordMap.get( "organizationCode" );
			if( upOrgCode == null ) return AUTHLEVEL_NOAUTH;
			if( upOrgCode.equals(organizationCode) ) return AUTHLEVEL_MANAGE;
			return AUTHLEVEL_NOAUTH;
		} else {
			return AUTHLEVEL_NOAUTH;
		}
	}

	protected class AuthValidator implements com.irt.data.DataLoader.Validator {
		String userId, countryCode, organizationCode;
		boolean isAdminUser;

		public AuthValidator( Context ctx ) {
			this.userId = ctx.sessionMng.getUniqId();
			this.countryCode = ctx.sessionMng.getGln();
			this.organizationCode = ctx.sessionMng.getExtraValue();
			this.isAdminUser = ctx.sessionMng.isAdminUser();
		}

		@Override
		public void close() {}

		@Override
		public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
			int authLevel = getAuthLevel( handler, userId, countryCode, organizationCode, isAdminUser, recordMap );

			if( authLevel == AUTHLEVEL_MANAGE ) return;

			throw handler.createDataException( "ERR_HAS_NOAUTH", recordMap );
		}
	}
}
