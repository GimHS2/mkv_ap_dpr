/*
 *	File Name:	DPRPlantRecovery.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		dpr_plantrecovery_list.jsp
 *		pub_list_count.jsp
 *		dpr_plantrecovery_input.jsp
 *		pub_upload_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2	신규 UI/UX 적용
 *	jbaek		2018/10/30		2.2.1	isChinaCountry() 삭제
 *	song7981	2016/02/29		2.0.0	create
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
import com.irt.data.QueryableManager;
import com.irt.data.Record;

import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.PlantRecovery;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;


/*
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRPlantRecovery"})
public class DPRPlantRecovery extends DPRServletModel {
	protected Map<String, Object> createConditionMap ( Context ctx ) throws SQLException, IOException, ServletException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		setDefaultParameter( ctx, conditionMap );
		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode(ctx) );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );

		if( conditionMap.containsKey("itemCode") ) {
			Condition.putConditionValueOnly(conditionMap, "itemCode", conditionMap.get("itemCode"), Condition.CONDTYPE_CONTAINS );
		}
		if( conditionMap.containsKey("itemName") ) {
			Condition.putConditionValueOnly(conditionMap, "itemName", conditionMap.get("conditionItemName"), Condition.CONDTYPE_CONTAINS);
		}

		return conditionMap;
	}

	@Override
	protected boolean doRequest ( Context ctx, boolean isPost ) throws SQLException, IOException, ServletException {
		if( MODE_COND_SETTING.equals(ctx.mode) )
			return setAttributeCondition( ctx );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected boolean download( Context ctx) throws IOException, ServletException, SQLException {
		PlantRecovery db = (PlantRecovery) ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPlantRecovery%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		DataWriter out = createTextDataWriter( ctx, "plantrecovery" + "_" + getSavedOrganizationCode(ctx) );

		try{
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			db.write( out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE );
		} catch ( SQLException sqlEx ){
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
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
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPlantRecovery.LST" );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPlantRecovery.MNG" );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPlantRecovery.MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPlantRecovery.MNG" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPlantRecovery.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPlantRecovery.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new PlantRecovery( ctx.handler );

		String messageKey = "TITLE_DPR_PLANTRECOVERY_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_RECOVERYCOMMENT" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		PlantRecovery db = (PlantRecovery)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPlantRecovery%LIST" );

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION );
		setAttributeCondition( ctx, conditionMap, "PT" );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRPlantRecovery.MNG") );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_plantrecovery_list.jsp" );
	}

	@Override
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		String ctype = (String)conditionMap.get( "ctype" );
		setAttributeCondition( ctx, conditionMap, ctype );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_attribute_party.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRPlantRecovery.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((PlantRecovery)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	@Override
	protected boolean upload( Context ctx ) throws SQLException, IOException, ServletException {
		UploadLog logDB = new UploadLog( ctx.handler );

		String selected_organizationCode = ctx.req.getParameter("organizationCode");
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPlantRecovery%DOWN" );
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "organizationCode" , selected_organizationCode );
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			defaultMap.put( "countryCode", getUserCountryCode(ctx) );
			defaultMap.put( "divisionCode", getDivisionCode(ctx) );

			PlantRecovery db = (PlantRecovery)ctx.db;

			String[] updateFieldKeys = { "brandCode", "recovery", "updateUserId" };
			loader = db.createDataLoader( fieldKeys, defaultMap, updateFieldKeys, Record.INSERT_OR_UPDATE_OR_DELETE );

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "DPR" );
			resultMap.put( "uploadType", "RCV" );
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			RecordFormat messageFormat = PatternRecordFormat
				.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );
			RecordFormat lineNameFormat = PatternRecordFormat.getInstance( "[${plantCode}] - [${itemCode}] - {${itemName}]" );

			loaderLogger = logDB.createLogger( resultMap, messageFormat, lineNameFormat );
			DataLoader dataLoader = createDataLoader(ctx, ctx.handler, loader, loaderLogger, validator, true );

			try{
				DataReader reader = dataLoader.getDataReader();
				java.io.PrintStream out = dataLoader.getErrorPrintStream();
				for( int i = 0; i < com.irt.data.cols.ColumnUtility.getTitleRowCount(columnList) && !reader.isEOF(); i++ ) {
					System.out.println("count = " + com.irt.data.cols.ColumnUtility.getTitleRowCount(columnList));
					try {
						reader.readNext();
					} catch( DataException dataEx ) {}
					if( reader.getLineString() != null && out != null )
						System.out.println("lineNumber = " + reader.getLineNumber() );
				}

				reader.setTrim(true);

				dataLoader.execute();
				ctx.pageConfig.setMessage( (String)loaderLogger.getResultMap().get("message") );
			} finally {
				dataLoader.close( false );
			}
			loader = null;

			String uploadInputPath = "DPRPlantRecovery?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT;
			String redirectURL = RBMUploadLog.getUploadLogURL( ctx, dataLoader, systemConfig.getClassURL(), uploadInputPath );

			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		} finally {
			try { if( loader != null ) loader.close(); } catch( Exception ignored ) {}
		}
	}

	@Override
	protected boolean uploadInput( Context ctx ) throws SQLException, IOException, ServletException {
		Map<String, Object> recordMap = new ParameterMap( ctx.req );

		String organizationCode = Record.extractString( recordMap, "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		recordMap.put( "encoding", com.irt.dpr.Country.getDefault(organizationCode, "encoding") );

		ctx.req.setAttribute( "record", recordMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_upload_input.jsp" );
	}

	@Override
	protected boolean info( Context ctx, boolean inserting ) throws SQLException, IOException, ServletException {
		PlantRecovery db = (PlantRecovery)ctx.db;

		String organizationCode = ctx.req.getParameter( "organizationCode" );
		String plantCode = ctx.req.getParameter( "plantCode" );
		String itemCode= ctx.req.getParameter( "itemCode" );

		Map<String, Object> primaryMap = PlantRecovery.createPrimary( organizationCode, plantCode, itemCode );

		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap );

		// forward
		if( inserting ) {
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRPlantRecovery.MNG") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_plantrecovery_input.jsp" );
		}
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws SQLException, IOException, ServletException {
		PlantRecovery db = (PlantRecovery)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		conditionMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", conditionMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );

		try{
			if( inserting ){
				db.regist( conditionMap );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
			} else {
				if( !db.modify(conditionMap) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			}

			ctx.req.setAttribute( "record", db.getRecord(conditionMap) );
			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_plantrecovery_input.jsp" );
		} catch( DataException dataEx ) {
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			logger.info( "error.", dataEx );
		} catch ( SQLException sqlEx ) {
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			logger.error( "internal error.", sqlEx );
		}

		String mode = ( inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PLANTRECOVERY_" + mode.toUpperCase()) );

		return registInput(ctx);
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		if( ctx.req.getAttribute("record") == null ) {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			//default sales org
			com.irt.dpr.CountryCondition ccnd = new com.irt.dpr.CountryCondition( ctx.handler );
			Map<String, Object> ccndMap = ccnd.getRecord( ccnd.createPrimary(ctx.sessionMng.getGln(), organizationCode) );

			recordMap.put( "organizationCode", ccndMap.get( "organizationCode" ) );
			recordMap.put( "organizationName", ccndMap.get( "organizationName" ) );

			ctx.req.setAttribute( "record", recordMap );
		}
		ctx.req.setAttribute( "fieldSet", ((PlantRecovery)ctx.db).getFieldSet(true) );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_plantrecovery_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, SQLException, ServletException {
		PlantRecovery db = (PlantRecovery)ctx.db;

		String[] organizationCodes = ctx.req.getParameterValues( "organizationCode" );
		String[] plantCodes = ctx.req.getParameterValues( "plantCode" );
		String[] itemCodes= ctx.req.getParameterValues( "itemCode" );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCodes == null || organizationCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( plantCodes == null || plantCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( itemCodes == null || itemCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		int count = 0;
		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < organizationCodes.length; i++) {
			primaryMap = PlantRecovery.createPrimary(organizationCodes[i], plantCodes[i], itemCodes[i] );
			try {
				if( db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch ( DataException dataEx ) {
				ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getFieldValue(primaryMap, "title"), dataEx) );
			}
		}

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}
}
