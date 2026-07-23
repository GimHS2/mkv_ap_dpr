/*
 *	File Name:	DPRRddMng.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_rddmng_list.jsp
 *		dpr_rddmng_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2024/02/29		2.2.2	download() : 컬럼리스트 이름 변경, tryWorkbookAutoSizeColumn() 삭제
 *										upload() : 컬럼리스트 이름 변경
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	hankalam	2019/06/28		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import com.irt.data.ManipulableManager;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.cols.ColumnList;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.Country;
import com.irt.dpr.Party;
import com.irt.dpr.PartyOperation;
import com.irt.dpr.RddIndicator;
import com.irt.dpr.RddTrigger;
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
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRRddMng"})
public class DPRRddMng extends DPRServletModel {
	public final static String PARAM_VIEWTYPE			= "vtype";
	public final static String VIEW_TYPE_TRIGGER		= "TRG";
	public final static String VIEW_TYPE_INDICATOR		= "IND";

	public final static String PARAM_TYPE				= "type";
	public final static String TYPE_SOLD				= "sold";
	public final static String TYPE_SHIP				= "ship";

	public final static String MODE_COND_SETTING		= "rtp";
	public final static String MODE_SET_TRADEPARTNER	= "stp";
	public final static String MODE_GET_PREDEFINED_RDD	= "rdd";
	public String viewType;

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap parameterMap = new ParameterMap( ctx.req, true );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
		conditionMap.put("countryCode", getUserCountryCode(ctx));
		conditionMap.put("divisionCode", getDivisionCode(ctx));
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));

		if( parameterMap.containsKey("partyCode") )
			Condition.putConditionValueOnly(conditionMap, "partyCode", parameterMap.get("partyCode"));

		if( parameterMap.containsKey("shipPartyCode") )
			Condition.putConditionValueOnly(conditionMap, "shipPartyCode", parameterMap.get("shipPartyCode"));

		if( parameterMap.containsKey("officeCode") )
			Condition.putConditionValueOnly(conditionMap, "officeCode", parameterMap.get("officeCode"));
		if( parameterMap.containsKey("groupCode") )
			Condition.putConditionValueOnly(conditionMap, "groupCode", parameterMap.get("groupCode"));

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
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_COND_SETTING.equals(ctx.mode) )
			return setAttributeCondition( ctx );
		else if( MODE_SET_TRADEPARTNER.equals(ctx.mode) )
			return setTradePartner( ctx );
		else if( MODE_GET_PREDEFINED_RDD.equals(ctx.mode) )
			return setPredefinedRDD( ctx );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected boolean download( Context ctx) throws IOException, ServletException, SQLException {
		QueryableManager db = (QueryableManager) ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRRddMngHSSF." + viewType + "%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String filename = ctx.msghandler.getMessage( "TITLE_DPR_RDD_" + viewType + "_" );
		DataWriter out = createDataWriter( ctx, filename );

		try{
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			db.write( out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE );
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
		ManipulableManager db = (ManipulableManager)ctx.db;

		String organizationCode = ctx.req.getParameter( "organizationCode" );
		String distributionChannelCode = ctx.req.getParameter( "distributionChannelCode" );
		String officeCode = ctx.req.getParameter( "officeCode" );
		String soldPartyCode = ctx.req.getParameter( "partyCode" );
		String shipPartyCode = ctx.req.getParameter( "shipPartyCode" );
		Map<String, Object> primaryMap;

		if( organizationCode == null || organizationCode.length() == 0 || distributionChannelCode == null || distributionChannelCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( VIEW_TYPE_TRIGGER.equals(viewType) ) {
			String trgKey = ctx.req.getParameter( "trgKey" );
			if( officeCode == null || officeCode.length() == 0 ) {
				officeCode = "0";
			}
			primaryMap = RddTrigger.createPrimary( trgKey, organizationCode, distributionChannelCode, officeCode );
		} else {
			if( soldPartyCode == null || soldPartyCode.length() == 0 ) {
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			}
			if( shipPartyCode == null || shipPartyCode.length() == 0 ) {
				shipPartyCode = "0";
			}
			primaryMap = RddIndicator.createPrimary( organizationCode, distributionChannelCode, soldPartyCode, shipPartyCode );
		}

		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

		String allowDays = (String) recordMap.get( "allowDays" );
		if( allowDays != null && allowDays.length() > 0 ) {
			for( String allowDay : allowDays.split(",") ) {
				recordMap.put( "allowDays" + allowDay, allowDay );
			}
		}
		String dayOfWeeks = (String) recordMap.get( "dayOfWeek" );
		if( dayOfWeeks != null && dayOfWeeks.length() > 0 ) {
			for( String dayOfWeek : dayOfWeeks.split(",") ) {
				recordMap.put( "dayOfWeek" + dayOfWeek, dayOfWeek );
			}
		}

		ctx.req.setAttribute( "record", recordMap );

		// auth checking
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( true );
		else if( ctx.sessionMng.isCountryAdmin() )
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRRddMng.MNG") );
		else
			ctx.pageConfig.setManageAuth( false );

		if( inputting ) {

			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
			conditionMap.put( "partyCode", soldPartyCode );
			setAttributePartner( ctx, conditionMap, PARTNER_SHIP );
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRRddMng.INF") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_rddmng_input.jsp" );
		}
	}


	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		viewType = ctx.req.getParameter( PARAM_VIEWTYPE );
		if( !VIEW_TYPE_INDICATOR.equals(viewType) && !VIEW_TYPE_TRIGGER.equals(viewType) ) {
			viewType = VIEW_TYPE_TRIGGER;
		}
		if( VIEW_TYPE_INDICATOR.equals(viewType) ) {
			ctx.db = new RddIndicator( ctx.handler );
		} else {
			ctx.db = new RddTrigger( ctx.handler );
		}
		pageConfig.setProperty( PARAM_VIEWTYPE, viewType );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRRddMng.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRRddMng.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRRddMng.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRRddMng.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRRddMng.MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRRddMng.LST" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRRddMng.MNG" );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_SET_TRADEPARTNER.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRRddMng.MNG" );
		else if( MODE_GET_PREDEFINED_RDD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		String messageKey = "TITLE_DPR_RDD_"+ viewType.toUpperCase() + "_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_PARTNER", "jsp.SUBMENU_RDDSETTING" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		QueryableManager db = (QueryableManager)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList;
		String columnListName = "DPRRddMng." + viewType + "%LIST";
		columnList = getColumnList( ctx, columnListName );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );

		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		setAttributePartyMaster(ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() ) ctx.pageConfig.setManageAuth( true );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_rddmng_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRRddMng.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((Country)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		QueryableManager db = (QueryableManager) ctx.db;

		ctx.req.setAttribute( "fieldSet", ((ManipulableManagerImpl) db).getFieldSet(true) );

		String organizationCode = ctx.req.getParameter( "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		Map<String, Object> conditionMap = createConditionMap( ctx );
		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE );

		if( ctx.req.getAttribute("record") == null ) {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			//default sales org
			com.irt.dpr.CountryCondition ccnd = new com.irt.dpr.CountryCondition( ctx.handler );
			Map<String, Object> ccndMap = ccnd.getRecord( ccnd.createPrimary(ctx.sessionMng.getGln(), organizationCode) );
			recordMap.put( "organizationCode", ccndMap.get( "organizationCode" ) );
			recordMap.put( "organizationName", ccndMap.get( "organizationName" ) );

			ctx.req.setAttribute( "record", recordMap );
		}

		return forward( ctx, systemConfig.getJspPath() + "/dpr_rddmng_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		ManipulableManager db = (ManipulableManager)ctx.db;

		String[] trgKey = ctx.req.getParameterValues( "trgKey" );
		String[] organizationCode = ctx.req.getParameterValues( "organizationCode" );
		String[] distributionChannelCode = ctx.req.getParameterValues( "distributionChannelCode" );
		String[] officeCode = ctx.req.getParameterValues( "officeCode" );
		String[] soldPartyCode = ctx.req.getParameterValues( "partyCode" );
		String[] shipPartyCode = ctx.req.getParameterValues( "shipPartyCode" );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( trgKey == null || organizationCode == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( trgKey.length == 0 || organizationCode.length == 0 )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		// 레코드 삭제
		int count = 0;
		Map<String, Object> dataMap = new java.util.TreeMap<String, Object>();
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < organizationCode.length; i++ ) {
			try {
				Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();
				primaryMap.put( "trgKey", trgKey[i] );
				primaryMap.put( "organizationCode", organizationCode[i] );
				primaryMap.put( "distributionChannelCode", distributionChannelCode[i] );
				String[] nullValueFieldKeys;

				if( VIEW_TYPE_TRIGGER.equals(viewType) ) {
					primaryMap.put( "officeCode", officeCode[i] );
					nullValueFieldKeys = new String[] { "distributionChannelCode", "officeCode" };
				} else {
					if( soldPartyCode[i] == null || soldPartyCode[i].length() == 0 ) {
						throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
					}

					primaryMap.put( "partyCode", soldPartyCode[i] );
					primaryMap.put( "shipPartyCode", shipPartyCode[i] );
					nullValueFieldKeys = new String[] { "distributionChannelCode", "shipPartyCode" };
				}
				replaceNullValues( primaryMap, nullValueFieldKeys, NULL_VALUE );
				dataMap.clear();
				dataMap.putAll( primaryMap );
				if( db.delete(primaryMap) ) count++;

				ctx.handler.commit();
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				if( VIEW_TYPE_TRIGGER.equals(viewType) ) {
					errorList.add( createErrorMap(db.getFieldValue(dataMap, "officeCode"), dataEx) );
				} else {
					errorList.add( createErrorMap(db.getFieldValue(dataMap, "partyCode"), dataEx) );
				}
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
	protected ColumnList getColumnList( Context ctx, String columnListName, String... optionKeys ) throws ServletException {
		if( MODE_DOWNLOAD.equals(ctx.mode) || MODE_UPLOAD.equals(ctx.mode) ) {
			return getDownUpColumnList(ctx, columnListName, null, optionKeys);
		}

		return super.getColumnList(ctx, columnListName, optionKeys);
	}

	public String getUserPartyCode( Context ctx ) throws SQLException, ServletModelException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		conditionMap.put( "divisionCode", getDivisionCode(ctx) );
		conditionMap.put( "distributionChannelCode", getDistributionChannelCode(ctx) );
		conditionMap.put( "uniqId", ctx.sessionMng.getUniqId() );
		conditionMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
		conditionMap.put( "authIndicator", "Y" );
		conditionMap.put( "partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
		conditionMap.put( "baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );

		List<Map<String, Object>> recordList = new com.irt.dpr.PartyAuth(ctx.handler).getRecords( conditionMap, new String[] { "partyCode" } );
		if( recordList != null && recordList.size() > 0 ) {
			Map recordMap = recordList.get( 0 );
			if( recordMap != null )
				return (String)recordMap.get( "partyCode" );
		}

		return null;
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		ManipulableManager db = (ManipulableManager) ctx.db;

		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );

		if( VIEW_TYPE_TRIGGER.equals(viewType) ) {
			String[] nullValueFieldKeys = new String[] { "distributionChannelCode", "officeCode" };
			replaceNullValues( recordMap, nullValueFieldKeys, NULL_VALUE );
		} else {
			String[] nullValueFieldKeys = new String[] { "distributionChannelCode", "shipPartyCode" };
			replaceNullValues( recordMap, nullValueFieldKeys, NULL_VALUE );
		}

		try {
			if( inserting ) {
				if( VIEW_TYPE_TRIGGER.equals(viewType) ) {
					recordMap.put( "trgKey", ((RddTrigger)db).createSequence() );
				}
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

			return forward( ctx, systemConfig.getJspPath() + "/dpr_rddmng_input.jsp" );
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
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_RDD_" + viewType + "_" + mode.toUpperCase()) );
		return registInput( ctx );
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		UploadLog logDB = new UploadLog( ctx.handler );

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRRddMng.MNG") )
			throw new ServletModelException( ServletModelException.HAS_NOAUTH );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRRddMngHSSF." + viewType + "%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );

			String uploadType = "RDD" + viewType;
			ManipulableManager db = (ManipulableManager) ctx.db;
			RecordFormat lineNameFormat;
			if( VIEW_TYPE_INDICATOR.equals(viewType) ) {
				loader = ((RddIndicator)db).createDataLoader( fieldKeys, defaultMap, new String[] { "updateUserId" }, Record.INSERT );
				lineNameFormat = PatternRecordFormat.getInstance( "[${organizationCode}] - [${partyCode}] - [${shipPartyCode}] - [${usePreDefined}]" );
			} else {
				loader = ((RddTrigger)db).createDataLoader( fieldKeys, defaultMap, new String[] { "updateUserId" }, Record.INSERT );
				lineNameFormat = PatternRecordFormat.getInstance( "[${organizationCode}] - [${officeCode}] - [${dayOfWeek}] - [${allowDays}]" );
			}
			//validator = new AuthValidator(ctx);

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "DPR" );
			resultMap.put( "uploadType", uploadType );
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			RecordFormat messageFormat = PatternRecordFormat
				.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );

			loaderLogger = logDB.createLogger( resultMap, messageFormat, lineNameFormat );
			DataLoader dataLoader = createDataLoader( ctx, ctx.handler, loader, loaderLogger, validator, true );

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

			String uploadInputPath = "DPRRddMng?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT;
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

		recordMap.put( "organizationCode", organizationCode );
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

	protected boolean setTradePartner( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		String type = ctx.req.getParameter( PARAM_TYPE );

		if( TYPE_SOLD.equals(type) ) {
			/*
			String shipPartyCode = ctx.req.getParameter( "shipPartyCode" );
			if( shipPartyCode != null && shipPartyCode.length() > 0 )
				conditionMap.put( "linkPartyCode", shipPartyCode );
			*/
			setAttributePartner( ctx, conditionMap, PARTNER_SOLD );
		} else if( TYPE_SHIP.equals(type) ) {
			String soldPartyCode = ctx.req.getParameter( "partyCode" );

			if( soldPartyCode != null && soldPartyCode.length() > 0 )
				conditionMap.put( "partyCode", soldPartyCode );

			setAttributePartner( ctx, conditionMap, PARTNER_SHIP );
		} else
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		ctx.pageConfig.setProperty( "type", type );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_rddmng_input_cond.jsp" );
	}

	@Override
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		Object distributionChannelCode = conditionMap.get( "distributionChannelCode" );
		if( distributionChannelCode instanceof String ){
			conditionMap.put( "distributionChannelCode", new String[] { (String)distributionChannelCode } );
		}

		String ctype = (String)conditionMap.get( "ctype" );
		setAttributeCondition( ctx, conditionMap, ctype );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_attribute_party.jsp" );
	}

	protected boolean setPredefinedRDD( Context ctx ) throws IllegalArgumentException, SQLException, IOException, ServletException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		String organizationCode = (String) conditionMap.get( "organizationCode" );
		String distributionChannelCode = (String) conditionMap.get( "distributionChannelCode" );
		String divisionCode = getDivisionCode(ctx);
		String soldPartyCode = (String) conditionMap.get( "partyCode" );
		String shipPartyCode = (String) conditionMap.get( "shipPartyCode" );


		Party partyDB = new com.irt.dpr.Party( ctx.handler );
		Map<String, Object> primaryMap = Party.createPrimary( soldPartyCode, organizationCode, distributionChannelCode, divisionCode );
		Map<String, Object> recordMap = partyDB.getRecord( primaryMap, new String[] { "officeCode", "groupCode" } );
		String officeCode = (String) recordMap.get( "officeCode" );
		String groupCode = (String) recordMap.get( "groupCode" );

		/*
		if( shipPartyCode == null || shipPartyCode.length() == 0 ) {
			shipPartyCode = "0";
		}
		*/
		RddIndicator indDB = new RddIndicator( ctx.handler );
		boolean isPredefined = indDB.isPredefined( organizationCode, distributionChannelCode, divisionCode, shipPartyCode );
		RddTrigger trgDB = new RddTrigger( ctx.handler );
		ctx.req.setAttribute( "rddValues", trgDB.getRddValues( isPredefined, organizationCode, distributionChannelCode, officeCode, groupCode ) );

		PartyOperation operDB = new PartyOperation( ctx.handler );

		com.irt.data.Date orderDate;
		try {
			String dateValue = (String)conditionMap.get( "orderDate" );
			if( dateValue == null || dateValue.length() == 0 ) {
				throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
			}
			orderDate = com.irt.data.Date.getInstance( dateValue );
		} catch ( ParseException e ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		String orderInd = operDB.getOperationInd( organizationCode, distributionChannelCode, officeCode, groupCode, orderDate, "orderInd" );
			ctx.pageConfig.setProperty( "orderInd", orderInd );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_order_rddset.jsp" );
	}
}
