/*
 *	File Name:	DPRStockQuery.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		pub_upload_input.jsp
 *		dpr_stockquery_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2024/08/28		2.2.2	backURL 생성시 isSecure 옵션값을 준수하도록 변경
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	hankalam	2020/12/31		2.2.0	create
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
import com.irt.data.DataResult;
import com.irt.data.DataWriter;
import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.FreeGoods;
import com.irt.dpr.Party;
import com.irt.dpr.PlantMapping;
import com.irt.dpr.StockQuery;
import com.irt.dpr.StockQueryManage;
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
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRStockQuery"})
public class DPRStockQuery extends DPRServletModel {
	protected final static String MODE_SIMULATION				= "sim";
	protected final static String MODE_CONDITION_SIMULATION		= "conds";
	protected final static String MODE_WAIT						= "wait";
	protected final static String MODE_MANAGEMENT_INPUT			= "imng";
	protected final static String MODE_MANAGEMENT_UPDATE		= "mng";
	protected final static String MODE_TEMPLATE_DOWNLOAD		= "tdown";

	protected Map<String, Object> createConditionMap ( Context ctx ) throws SQLException, IOException, ServletException {
		ParameterMap paramMap = new ParameterMap( ctx.req, true );
		String organizationCode = paramMap.getParameter( "organizationCode" );
		if( organizationCode == null ) {
			Condition.putConditionValueOnly( paramMap, "organizationCode", getSavedOrganizationCode(ctx) );
		}
		Condition.putConditionValueOnly( paramMap, "divisionCode", getDivisionCode(ctx) );
		Condition.putConditionValueOnly( paramMap, "displayLanguage", getDisplayLanguage(ctx) );
		return paramMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_SIMULATION.equals(ctx.mode) )
			return simulation( ctx );
		else if( MODE_CONDITION_SIMULATION.equals(ctx.mode) )
			return singleSimulation( ctx );
		else if( MODE_WAIT.equals(ctx.mode) )
			return wait( ctx );
		else if( MODE_MANAGEMENT_INPUT.equals(ctx.mode) )
			return manageInput( ctx );
		else if( MODE_MANAGEMENT_UPDATE.equals(ctx.mode) )
			return manageUpdate( ctx );
		else if( MODE_TEMPLATE_DOWNLOAD.equals(ctx.mode) )
			return download( ctx );
		return super.doRequest( ctx, isPost );
	}

	@Override
	protected boolean download( Context ctx) throws IOException, ServletException, SQLException {
		StockQuery db = (StockQuery) ctx.db;
		Map<String, Object> conditionMap = createConditionMap( ctx );
		String columnListName;
		String filename;
		if( MODE_TEMPLATE_DOWNLOAD.equals(ctx.mode) ) {
			columnListName = "DPRStockQuery%UP";
			conditionMap.put( "queryKey", "X" );
			filename = ctx.msghandler.getMessage( "MSG_DPR_STOCK_QUERY_TEMPLATE_FILENAME" );
		} else {
			if( !conditionMap.containsKey("simulationKey") ) {
				String queryKey = (String) conditionMap.get( "queryKey" );
				if( queryKey != null && queryKey.length() > 0 ) {
					conditionMap.put( "queryKey", queryKey );
					conditionMap.remove( "organizationCode" );
					conditionMap.remove( "divisionCode" );
				} else {
					conditionMap.put( "queryKey", "X" );
				}
			}
			columnListName = "DPRStockQuery%DOWN";
			filename = ctx.msghandler.getMessage( "TITLE_DPR_STOCKQUERY_" );
		}
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, columnListName, com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
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
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRStockQuery.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRStockQuery.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_CONDITION_SIMULATION.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRStockQuery.SIM" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) || MODE_TEMPLATE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRStockQuery.DWN" );
		else if( MODE_SIMULATION.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRStockQuery.SIM" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRStockQuery.SIM" );
		else if( MODE_WAIT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRStockQuery.SIM" );
		else if( MODE_MANAGEMENT_INPUT.equals(ctx.mode) || MODE_MANAGEMENT_UPDATE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRStockQuery.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new StockQuery( ctx.handler );

		String messageKey = "TITLE_DPR_STOCKQUERY_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_STOCKQUERY" );
	}


	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		StockQuery db = (StockQuery)ctx.db;
		String queryKey = ctx.req.getParameter( "queryKey" );
		if( queryKey == null || queryKey.length() < 1 ) {
			Map<String, Object> conditionMap = com.irt.data.Record.createMap( "dateValue", com.irt.data.Date.getInstance() );
			conditionMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			queryKey = db.getMaxQueryKey( conditionMap );
		}
		return list( ctx, queryKey );
	}

	protected boolean list( Context ctx, String queryKey ) throws IOException, ServletException, SQLException {
		StockQuery db = (StockQuery)ctx.db;

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRStockQuery%LIST" );

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = null;
		Map<String, Object> conditionMap = null;
		if( queryKey != null ) {
			conditionMap = com.irt.data.Record.createMap( "queryKey", queryKey );
			conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
			recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		}
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		conditionMap = createConditionMap( ctx );

		{
			StockQueryManage stmngDB = new StockQueryManage( ctx.handler );
			String countryCode = getUserCountryCode( ctx );

			Map<String, Object> primaryMap = StockQueryManage.createPrimary( countryCode );
			Map<String, Object> manageMap = stmngDB.getRecord( primaryMap );
			if( manageMap == null ) {
				manageMap = new java.util.HashMap<String, Object>();
				manageMap.put( "autoRetry", StockQueryManage.DEFAULT_AUTO_RETRY );
			}
			manageMap.put( "inputQty", StockQuery.DEFAULT_SIMULATION_INPUT_QTY );

			ctx.req.setAttribute( "manageMap", manageMap );
		}

		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "queryKey", queryKey );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );
		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRStockQuery.MNG") );

		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_STOCKQUERY_" + ctx.mode.toUpperCase()) );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_stockquery_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRStockQuery.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((StockQuery)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	protected boolean manageInput( Context ctx ) throws IOException, ServletException, SQLException {
		StockQueryManage stmngDB = new StockQueryManage( ctx.handler );
		String countryCode = getUserCountryCode( ctx );

		Map<String, Object> primaryMap = StockQueryManage.createPrimary( countryCode );
		Map<String, Object> recordMap = stmngDB.getRecord( primaryMap );
		if( recordMap == null ) {
			recordMap = new java.util.HashMap<String, Object>();
			recordMap.put( "autoRetry", StockQueryManage.DEFAULT_AUTO_RETRY );
		}
		if( recordMap.get("inputQty") == null ) {
			recordMap.put( "inputQty", StockQuery.DEFAULT_SIMULATION_INPUT_QTY );
		}
		if( recordMap.get("simulationLineNumber") == null ) {
			recordMap.put( "simulationLineNumber", StockQuery.DEFAULT_SIMULATION_LINENUMBER );
		}

		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", stmngDB.getFieldSet(true) );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_stockquery_mng.jsp");
	}

	protected boolean manageUpdate( Context ctx ) throws IOException, ServletException, SQLException {
		StockQueryManage stmngDB = new StockQueryManage( ctx.handler );

		Map<String, Object> recordMap = new ParameterMap( ctx.req, true );
		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", stmngDB.getFieldSet(true) );

		String countryCode = getUserCountryCode( ctx );
		String autoRetry = (String) recordMap.get( "autoRetry" );
		if( autoRetry == null || autoRetry.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		recordMap.put( "countryCode", countryCode );

		String[] updateFieldKeys = { "autoRetry", "inputQty", "updateUserId" };
		if( ctx.sessionMng.isSystemAdmin() ) {
			updateFieldKeys = com.irt.util.Arrays.append( updateFieldKeys, "simulationLineNumber" );
		}

		try {
			if( !stmngDB.modify(recordMap, updateFieldKeys) )
				stmngDB.regist( recordMap );
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
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

		return manageInput( ctx );
	}

	protected boolean simulation( Context ctx ) throws IOException, ServletException, SQLException {
		StockQuery db = new StockQuery( ctx.handler, systemConfig );
		StockQueryManage stmngDB = new StockQueryManage( ctx.handler );
		String backURL = new StringBuffer( ((HtmlPage)ctx.pageConfig).getRequestURL() ).append( "?" ).append( ctx.req.getQueryString() ).toString();
		String queryKey = ctx.req.getParameter( "queryKey" );
		if( queryKey == null || queryKey.length() == 0 ) {
			ctx.pageConfig.setBackURL( backURL );
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}
		String countryCode = getUserCountryCode( ctx );
		boolean autoRetry = "Y".equals( StockQueryManage.DEFAULT_AUTO_RETRY );
		Map<String, Object> mngRecordMap = stmngDB.getRecord( StockQueryManage.createPrimary(countryCode) );
		if( mngRecordMap != null ) {
			autoRetry = "Y".equals( mngRecordMap.get("autoRetry") );
		}
		Map<String, Object> parameterMap = new java.util.HashMap<String, Object>();
		String userId = ctx.sessionMng.getUniqId();
		parameterMap.put( "queryKey", queryKey );
		parameterMap.put( "countryCode", getUserCountryCode(ctx) );
		parameterMap.put( "updateUserId", userId );

		try {
			db.executeSimulation( parameterMap, autoRetry );
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_STOCK_QUERY_STATUS_CO") );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "error.", sqlEx );
		}

		String redirectURL = "DPRStockQuery?"+ PARAM_MODE +"="+ MODE_LIST;
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_LOCALE, ctx.req.getParameter("locale") );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_MENU, ctx.pageConfig.getSystemMenu() );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "queryKey", queryKey );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx) );

		return sendRedirect( ctx, redirectURL );
	}

	protected boolean singleSimulation( Context ctx ) throws IOException, ServletException, SQLException {
		StockQuery db = (StockQuery) ctx.db;
		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.Date dateValue = com.irt.data.Date.getInstance();
		String soldPartyCode = (String) conditionMap.get( "soldPartyCode" );
		String shipPartyCode = (String) conditionMap.get( "shipPartyCode" );
		String plantCode = (String) conditionMap.get("plantCode" );
		String itemCode = (String) conditionMap.get( "itemCode" );
		try {
			StockQueryManage stmngDB = new StockQueryManage( ctx.handler );
			String countryCode = getUserCountryCode( ctx );
			Map<String, Object> mngRecordMap = stmngDB.getRecord( StockQueryManage.createPrimary(countryCode) );
			int inputQty = StockQuery.DEFAULT_SIMULATION_INPUT_QTY;
			if( mngRecordMap != null ) {
				inputQty = ((Number)mngRecordMap.get("inputQty")).intValue();
			}

			if( plantCode != null ) {
				PlantMapping plantMapDB = new PlantMapping( ctx.handler );
				Map<String, Object> defaultPartyMap = plantMapDB.getDefaultPartyMap( getSavedOrganizationCode(ctx), plantCode );
				if( defaultPartyMap == null || defaultPartyMap.size() == 0 ) {
					throw new IllegalArgumentException( ctx.msghandler.getMessage("MSG_DPR_STOCK_QUERY_NOT_EXIST_DEFAULT_PLANT", plantCode) );

				}
				soldPartyCode = (String) defaultPartyMap.get( "soldPartyCode" );
				shipPartyCode = (String) defaultPartyMap.get( "shipPartyCode" );
				conditionMap.put( "soldPartyCode", soldPartyCode );
				conditionMap.put( "shipPartyCode", shipPartyCode );
			}
			if( soldPartyCode == null || soldPartyCode.length() == 0 || shipPartyCode == null || shipPartyCode.length() == 0
					|| itemCode == null || itemCode.length() == 0 ) {
				throw new IllegalArgumentException( ctx.msghandler.getMessage(ServletModelException.NEEDED_PARAMETER) );
			}

			Party partyDB = new Party( ctx.handler );
			String distributionChannelCode = partyDB.getDistributionChannelCode( soldPartyCode, conditionMap.get("organizationCode"), conditionMap.get("divisionCode") );
			if( distributionChannelCode == null ) {
				distributionChannelCode = getDistributionChannelCode( ctx );
			}
			conditionMap.put( "distributionChannelCode", distributionChannelCode );
			checkPartnerCode( ctx, conditionMap );

			String queryKey = StockQuery.createQueryKey(ctx.handler);
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>( conditionMap );
			recordMap.put( "dateValue", dateValue );
			recordMap.put( "queryKey", queryKey );
			recordMap.put( "simulationKey", StockQuery.createSimulationKey(ctx.handler) );
			recordMap.put( "countryCode", countryCode );
			recordMap.put( "lineNumber", 10 );
			recordMap.put( "inputQty", inputQty );
			recordMap.put( "uom", StockQuery.DEFAULT_SIMULATION_UOM );
			recordMap.put( "status", StockQuery.STATUS_UPLOAD );
			recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			db.regist( recordMap );
			return wait( ctx, queryKey );
		} catch( IllegalArgumentException argEx ) {
			ctx.pageConfig.setMessage( argEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error("internal error.", sqlEx);
		}

		String redirectURL = "DPRStockQuery?"+ PARAM_MODE +"="+ MODE_LIST;
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_LOCALE, ctx.req.getParameter("locale") );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_MENU, ctx.pageConfig.getSystemMenu() );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx) );

		return sendRedirect( ctx, redirectURL );
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		StockQuery db = (StockQuery) ctx.db;
		UploadLog logDB = new UploadLog( ctx.handler );
		String queryKey = null;

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRStockQuery%UP", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			StockQueryManage stmngDB = new StockQueryManage( ctx.handler );
			String countryCode = getUserCountryCode( ctx );
			Map<String, Object> mngRecordMap = stmngDB.getRecord( StockQueryManage.createPrimary(countryCode) );
			int inputQty = StockQuery.DEFAULT_SIMULATION_INPUT_QTY;
			int simulationLineNumber = StockQuery.DEFAULT_SIMULATION_LINENUMBER;
			if( mngRecordMap != null ) {
				if( mngRecordMap.get("inputQty") != null ) {
					inputQty = ((Number)mngRecordMap.get("inputQty")).intValue();
				}
				if( mngRecordMap.get("simulationLineNumber") != null ) {
					simulationLineNumber = ((Number)mngRecordMap.get("simulationLineNumber")).intValue();
				}
			}

			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			queryKey = StockQuery.createQueryKey( ctx.handler );
			defaultMap.put( "queryKey", queryKey );
			defaultMap.put( "dateValue", com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()) );
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			defaultMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
			defaultMap.put( "distributionChannelCode", getDistributionChannelCode(ctx) );
			defaultMap.put( "divisionCode", getDivisionCode(ctx) );
			defaultMap.put( "countryCode", getUserCountryCode(ctx) );
			defaultMap.put( "inputQty", inputQty );
			defaultMap.put( "simulationLineNumber", simulationLineNumber );
			defaultMap.put( "uom", "CSE" );
			defaultMap.put( "status", StockQuery.STATUS_UPLOAD );
			String uploadType = "STQ";

			RecordFormat lineNameFormat;

			loader = db.createDataLoader( fieldKeys, defaultMap, Record.INSERT|Record.UPDATE );
			lineNameFormat = PatternRecordFormat.getInstance( "[${soldPartyCode}]-[${shipPartyCode}]-[${plantCode}]-[${itemCode}]" );

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "DPR" );
			resultMap.put( "uploadType", uploadType );
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			RecordFormat messageFormat = PatternRecordFormat
				.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );

			loaderLogger = logDB.createLogger( resultMap, messageFormat, lineNameFormat );
			DataLoader dataLoader = createTextDataLoader( ctx, ctx.handler, loader, loaderLogger, validator, false );

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

			DataResult result = dataLoader.getDataResult();
			if( result.getErrorCount() < 1 ) {
				ctx.req.setAttribute( "uploadStatus", StockQuery.STATUS_UPLOAD );
				ctx.req.setAttribute( "queryKey", queryKey );
				return uploadInput( ctx );
			} else {

				String uploadInputPath = "DPRStockQuery?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT;
				String redirectURL = RBMUploadLog.getUploadLogURL( ctx, dataLoader, systemConfig.getClassURL(), uploadInputPath );

				return sendRedirect( ctx, HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
			}
		} finally {
			try { if( loader != null ) loader.close(); } catch( Exception ignored ) {}
		}
	}

	private boolean checkPartnerCode( Context ctx, Map<String, Object> conditionMap ) throws ServletModelException, SQLException {
		com.irt.dpr.PartyLink db = new com.irt.dpr.PartyLink( ctx.handler );
		Map<String, Object> partnerConditionMap = new java.util.HashMap<String, Object>( conditionMap );
		partnerConditionMap.put( "partyCode", partnerConditionMap.remove("soldPartyCode") );
		partnerConditionMap.put( "countryCode", getUserCountryCode(ctx) );
		partnerConditionMap.put( "partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
		partnerConditionMap.put( "linkStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
		String shipPartyCode = (String) partnerConditionMap.remove( "shipPartyCode" );

		partnerConditionMap.put( "linkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );
		if( db.getRecordCount(partnerConditionMap) < 1 ) {
			throw new IllegalArgumentException( ctx.msghandler.getMessage("ERR_CANNOT_FIND_SOLDTO") );
		}

		partnerConditionMap.put( "linkType", com.irt.dpr.PartyLink.LINKTYPE_SHIP );
		partnerConditionMap.put( "linkPartyCode", shipPartyCode );
		if( db.getRecordCount(partnerConditionMap) < 1 ) {
			throw new IllegalArgumentException( ctx.msghandler.getMessage("ERR_CANNOT_FIND_SHIPTO") );
		}

		return true;
	}

	@Override
	protected boolean uploadInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		recordMap.put( "encoding", "UTF8" );
		ctx.req.setAttribute( "record", recordMap );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_stockquery_upload.jsp" );
	}

	protected boolean wait( Context ctx ) throws IOException, ServletException, SQLException {
		String queryKey = ctx.req.getParameter( "queryKey" );
		return wait( ctx, queryKey );
	}

	protected boolean wait( Context ctx, String queryKey ) throws IOException, ServletException, SQLException {
		if( queryKey == null || queryKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		ctx.req.setAttribute( "queryKey", queryKey );
		ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_WAITING_SIMULATION") );

		return forward( ctx, systemConfig.getJspPath() +"/dpr_stockquery_wait.jsp" );
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
