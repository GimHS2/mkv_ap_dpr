/*
 *	File Name:	DPRFreeGoods.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_freegoods_list.jsp
 *		dpr_freegoods_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	hankalam	2019/07/31		2.2.0	create
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
import com.irt.dpr.FreeGoods;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;
import com.irt.sql.SQLHandler;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRFreeGoods"})
public class DPRFreeGoods extends DPRServletModel {
	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap parameterMap = new ParameterMap( ctx.req, true );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
		String startDate = Record.extractString( parameterMap, "startDate_min" );
		String endDate = Record.extractString( parameterMap, "endDate_max" );

		if( parameterMap.containsKey("partyCode") )
			Condition.putConditionValueOnly( conditionMap, "partyCode", parameterMap.get("partyCode") );

		if( parameterMap.containsKey("officeCode") )
			Condition.putConditionValueOnly( conditionMap, "officeCode", parameterMap.get("officeCode") );

		try {
			if( startDate != null && startDate.length() > 0 ) {
				conditionMap.put( "startDate"+ Condition.SUFFIX_MIN_VALUE, com.irt.data.Date.getInstance(startDate) );
				conditionMap.put( "startDate"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MINMAX );
			}

			if( endDate != null && endDate.length() > 0 ) {
				conditionMap.put( "endDate"+ Condition.SUFFIX_MAX_VALUE, com.irt.data.Date.getInstance(endDate) );
				conditionMap.put( "endDate"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MINMAX );
			}
		} catch( java.text.ParseException parseEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		if( parameterMap.containsKey("itemCode") )
			Condition.putConditionValueOnly( conditionMap, "itemCode", parameterMap.get("itemCode") );

		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode(ctx) );
		Condition.putConditionValueOnly( conditionMap, "divisionCode", getDivisionCode(ctx) );
		Condition.putConditionValueOnly( conditionMap, "organizationCode", getSavedOrganizationCode(ctx) );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );

		// authUniqId
		if( !ctx.sessionMng.isSystemAdmin() ) {
			Condition.putConditionValueOnly( conditionMap, "authUniqId", ctx.sessionMng.getUniqId() );

			if( ctx.sessionMng.isPartyAdmin() || ctx.sessionMng.isCountryAdmin() )
				Condition.putConditionValueOnly( conditionMap, "authCountryValue", "Y" );
			else
				Condition.putConditionValueOnly( conditionMap, "authCountryValue", "N" );
		}

		return conditionMap;
	}

	@Override
	protected boolean download( Context ctx) throws IOException, ServletException, SQLException {
		FreeGoods db = (FreeGoods) ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRFreeGoods%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String filename = ctx.msghandler.getMessage( "TITLE_DPR_FREEGOODS_" );
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
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		FreeGoods db = (FreeGoods)ctx.db;

		String freegoodsKey = ctx.req.getParameter( "freegoodsKey" );
		Map<String, Object> primaryMap;

		if( freegoodsKey == null || freegoodsKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		primaryMap = FreeGoods.createPrimary( freegoodsKey );

		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

		ctx.req.setAttribute( "record", recordMap );

		// auth checking
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( true );
		else if( ctx.sessionMng.isCountryAdmin() )
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRFreeGoods.MNG") );
		else
			ctx.pageConfig.setManageAuth( false );

		if( inputting ) {
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRFreeGoods.INF") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_freegoods_input.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		ctx.db = new FreeGoods( ctx.handler );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRFreeGoods.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRFreeGoods.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRFreeGoods.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRFreeGoods.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRFreeGoods.MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRFreeGoods.MNG" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRFreeGoods.MNG" );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		String messageKey = "TITLE_DPR_FREEGOODS_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_FREEQUOTA" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		FreeGoods db = (FreeGoods)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList;
		String columnListName = "DPRFreeGoods%LIST";
		columnList = getColumnList( ctx, columnListName );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );

		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		setAttributePartyMaster(ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_OFFICE );
		Map<String, Object> _condition = new java.util.HashMap<String, Object>( conditionMap );
		List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)ctx.req.getAttribute( "distributionChannels" );
		if( distributionChannels != null ) {
			Object[] channelCodes = com.irt.data.Record.extractObjectArray( distributionChannels, "distributionChannelCode" );
			_condition.put( "distributionChannelCode", channelCodes );
		}
		setAttributePartner( ctx, _condition, PARTNER_SOLD );

		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() ) ctx.pageConfig.setManageAuth( true );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_freegoods_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRFreeGoods.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((FreeGoods)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		FreeGoods db = (FreeGoods) ctx.db;

		ctx.req.setAttribute( "fieldSet", ((ManipulableManagerImpl) db).getFieldSet(true) );

		String organizationCode = ctx.req.getParameter( "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		Map<String, Object> conditionMap = createConditionMap( ctx );
		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_OFFICE );
		setAttributePartner( ctx, conditionMap, PARTNER_SOLD );
		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );

		if( ctx.req.getAttribute("record") == null ) {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			//default sales org
			com.irt.dpr.CountryCondition ccnd = new com.irt.dpr.CountryCondition( ctx.handler );
			Map<String, Object> ccndMap = ccnd.getRecord( ccnd.createPrimary(ctx.sessionMng.getGln(), organizationCode) );
			recordMap.put( "organizationCode", ccndMap.get( "organizationCode" ) );
			recordMap.put( "organizationName", ccndMap.get( "organizationName" ) );

			ctx.req.setAttribute( "record", recordMap );
		}

		return forward( ctx, systemConfig.getJspPath() + "/dpr_freegoods_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		FreeGoods db = (FreeGoods)ctx.db;

		String[] freegoodsKeys = ctx.req.getParameterValues( "freegoodsKey" );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( freegoodsKeys == null || freegoodsKeys.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		// 레코드 삭제
		int count = 0;
		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < freegoodsKeys.length; i++) {
			primaryMap = FreeGoods.createPrimary( freegoodsKeys[i] );
			try {
				if( db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getFieldValue(primaryMap, "freegoodsKeys"), dataEx) );
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
		FreeGoods db = (FreeGoods) ctx.db;
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );

		replaceNullValues( recordMap, new String[] { "partyCode" }, NULL_VALUE );
		try {
			if( inserting ) {
				recordMap.put( "freegoodsKey", db.createSequence() );
				db.regist( recordMap );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
			} else {
				if( !db.modify(recordMap) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			}

			ctx.req.setAttribute( "record", db.getRecord(recordMap) );
			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_freegoods_input.jsp" );
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

		String mode = ( inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_FREEGOODS_" + mode.toUpperCase()) );
		return registInput( ctx );
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		UploadLog logDB = new UploadLog( ctx.handler );

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRFreeGoods.MNG") )
			throw new ServletModelException( ServletModelException.HAS_NOAUTH );

		String organizationCode = ctx.req.getParameter( "organizationCode" );
		if( organizationCode == null ) {
			organizationCode = getSavedOrganizationCode( ctx );
		}
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRFreeGoods%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			defaultMap.put( "organizationCode", organizationCode );

			String uploadType = "FRG";
			FreeGoods db = (FreeGoods) ctx.db;
			RecordFormat lineNameFormat;

			String[] updateFieldKeys = { "startDate", "endDate", "officeCode", "partyCode", "itemCode", "quota", "orderableRatio", "usedQty", "updateUserId" };

			loader = db.createDataLoader( fieldKeys, defaultMap, updateFieldKeys, Record.INSERT | Record.UPDATE );
			lineNameFormat = PatternRecordFormat.getInstance( "[${organizationCode}] - [${officeCode}] - [${itemCode}]" );
			//validator = new AuthValidator(ctx);

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

			String uploadInputPath = "DPRFreeGoods?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT;
			String redirectURL = RBMUploadLog.getUploadLogURL( ctx, dataLoader, systemConfig.getClassURL(), uploadInputPath );

			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		} finally {
			try { if( loader != null ) loader.close(); } catch( Exception ignored ) {}
		}
	}

	@Override
	protected boolean uploadInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> recordMap = new ParameterMap( ctx.req );

		String organizationCode = Record.extractString( recordMap, "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

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

	@Override
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		setAttributeCondition( ctx, conditionMap, "DC" );
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>) ctx.req.getAttribute( "distributionChannels" );
		conditionMap.put( "distributionChannelCode", Record.extractObjectArray(distributionChannels, "distributionChannelCode") );

		String ctype = (String)conditionMap.get( "ctype" );
		setAttributeCondition( ctx, conditionMap, ctype );
		conditionMap.remove( "distributionChannelCode" );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_attribute_party.jsp" );
	}
}
