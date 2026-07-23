/*
 *	File Name:	DPRPartyUOM.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2	신규 UI/UX 적용
 *	jbaek		2019/07/30		2.2.1	권한 조건 추가
 *	hankalam	2017/02/28		2.2.0	create
 *
**/

import com.irt.dpr.Party;
import com.irt.html.HtmlUtility;
import com.irt.rbm.rbm.UploadLog;
import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import com.irt.servlet.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRPartyUOM"})
public class DPRPartyUOM extends DPRServletModel {
	public final static String MODE_COND_SETTING		= "rtp";

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap parameterMap = new ParameterMap( ctx.req, true );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();

		/* Condition Value Region Code, Organization Code*/
		String countryCode = Record.extractString( parameterMap, "countryCode" );

/* WORKING : systemAdmin and partyAdmin 처리 */
		if( countryCode == null || countryCode.length() == 0 )
			countryCode = getUserCountryCode( ctx );
		Condition.putConditionValueOnly( conditionMap, "countryCode", countryCode );
		setDefaultParameter( ctx, conditionMap );

		if( parameterMap.containsKey("parentPartyCode") )
			Condition.putConditionValueOnly( conditionMap, "parentPartyCode", parameterMap.get("parentPartyCode") );
		if( parameterMap.containsKey("regionCode") )
			Condition.putConditionValueOnly( conditionMap, "regionCode", parameterMap.get("regionCode") );

		String partyType = (String)conditionMap.get( "parytType" );
		if( Party.PARTYTYPE_CUSTOMER.equals(partyType) && !conditionMap.containsKey("parentPartyCode") )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( parameterMap.containsKey("partyCode") )
			Condition.putConditionValueOnly( conditionMap, "partyCode", parameterMap.get("partyCode") );
		else
			if( parameterMap.containsKey("partyName") )
				Condition.putConditionValueOnly( conditionMap, "partyName", parameterMap.get("partyName") );

		Condition.putConditionValueOnly( conditionMap, "status", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );
		Condition.putConditionValueOnly( conditionMap, "baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );

		// authUniqId
		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			Condition.putConditionValueOnly( conditionMap, "authUniqId", ctx.sessionMng.getUniqId() );
			Condition.putConditionValueOnly( conditionMap, "authPartyValue", "Y" );
		}

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_COND_SETTING.equals(ctx.mode) )
			return setAttributeCondition( ctx );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		Party db = (Party)ctx.db;
		Map<String, Object> conditionMap = createConditionMap( ctx );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPartyUOM%DOWN" );
		com.irt.html.ColumnListFactory factory = getColumnListFactory( ctx, columnList );

		DataWriter out = createTextDataWriter( ctx, ctx.msghandler.getMessage("DPR_PARTY_UOM_DOWNLOAD_FILE") );
		if( out instanceof com.irt.util.SSDataWriter ) {
			org.apache.poi.ss.usermodel.Sheet sheet = ((com.irt.util.SSDataWriter)out).getSheet();
			sheet.setDisplayGridlines( false );
			sheet.setDisplayZeros( false );
		}

		try {
			String[] columnPoolFieldKeys = columnList.getFieldKeys();
			String[] fieldKeys = new String[ columnPoolFieldKeys.length + 1 ];
			System.arraycopy(columnPoolFieldKeys, 0, fieldKeys, 0, columnPoolFieldKeys.length);
			fieldKeys[fieldKeys.length-1] = "allowUOM";

			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			List<Map<String, Object>> recordList = db.getRecords( conditionMap, fieldKeys );
			splitUOMbyList( ctx, recordList );

			db.write( recordList, ctx.handler, out, factory.getColumnList(), Party.OPT_WRITING_TITLE, -1 );
		} catch( SQLException sqlEx ) {
			out.println();
			out.print( sqlEx.getMessage() );
			logger.error( "internal error.", sqlEx );
		} finally {
			out.flush();
			out.close();
		}

		return true;
	}

	private com.irt.html.ColumnListFactory getColumnListFactory( Context ctx, com.irt.data.cols.ColumnList columnList ) {
		com.irt.html.ColumnListFactory factory = new com.irt.html.ColumnListFactory( columnList );
		String[] uomValues = getUOMValues( ctx );
		List<Map<String, Object>> uomList = new java.util.ArrayList<Map<String, Object>>();

		if( uomValues != null ) {
			if( MODE_LIST.equals(ctx.mode) ) {
				//factory.appendColumn( "checkAll", ctx.msghandler.getMessage("MSG_ALL"), "class='check'" );
				factory.beginColumnGroup( "uomInd", "UOM Indicate" );
			}

			for( String uom : uomValues ) {
				Map<String, Object> uomMap = new java.util.HashMap<String, Object>();
				uomMap.put( "uom", uom );
				uomList.add( uomMap );
				factory.appendColumn( uom.toLowerCase() + "_uom", uom.toUpperCase(), "class='check' style='width: 80px'" );
			}
			if( MODE_LIST.equals(ctx.mode) ) {
				factory.endColumnGroup();
			}
			ctx.req.setAttribute( "uomList", uomList );
		}
		return factory;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	private String[] getUOMValues( Context ctx ) {
		String[] uomValues = com.irt.rbm.RBMSystem
				.getSystemEnv( "DPR", "uom;" + getSavedOrganizationCode(ctx), Party.DEFAULT_UOM ).split( "," );

		return uomValues;
	}
	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyUOM.LST" );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyUOM.MNG" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPartyUOM.DOWN" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPartyUOM.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new Party( ctx.handler );

		String messageKey = "TITLE_DPR_PARTYUOM_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_PARTNER", "jsp.SUBMENU_UOMSETTING" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		Party db = (Party)ctx.db;
		// Condition Map
		Map<String, Object> conditionMap = createConditionMap( ctx );

		boolean isSufficiencyCondition = false;
		if( conditionMap.containsKey("organizationCode") && conditionMap.containsKey("divisionCode") && conditionMap.containsKey("distributionChannelCode") )
			isSufficiencyCondition = true;

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPartyUOM%LIST" );
		com.irt.html.ColumnListFactory factory = getColumnListFactory( ctx, columnList );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = null;
		if( !isSufficiencyCondition ) {
			idxVars[2] = 0;
			ctx.pageConfig.setProperty( "listmsg", ctx.msghandler.getMessage("MSG_CONDITION_NEEDED") );
		} else {
			String[] columnPoolFieldKeys = columnList.getFieldKeys();
			String[] fieldKeys = new String[ columnPoolFieldKeys.length + 1 ];
			System.arraycopy(columnPoolFieldKeys, 0, fieldKeys, 0, columnPoolFieldKeys.length);
			fieldKeys[fieldKeys.length-1] = "allowUOM";

			recordList = db.getRecords( conditionMap, fieldKeys, idxVars[0], idxVars[1] );
			splitUOMbyList( ctx, recordList );

			String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
			if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

			// Condition
			if( com.irt.rbm.SessionMng.GROUPCLASS_ORDER.equals(ctx.sessionMng.getGroupClass()) ) {
				if( recordList != null && recordList.size() == 1 ) {
					Map<String, Object> _conditionMap = new java.util.HashMap<String, Object> ( conditionMap );
					_conditionMap.put( "partyCode", recordList.get(0).get("partyCode") );
					conditionMap.putAll( db.getRecord(_conditionMap, new String[] { "regionCode", "customerGroupCode", "districtCode", "officeCode", "groupCode" }) );
				}
			}
		}

		ctx.req.setAttribute( "records", recordList );

		setAttributePartyMasterOnExisting( ctx, conditionMap, PARTYMASTER_ALL );
		setAttributePartner( ctx, conditionMap );

		ctx.req.setAttribute( "countries", new com.irt.dpr.Country(ctx.handler).getRecords(null, new String[] { "countryCode", "countryName" }) );
		ctx.req.setAttribute( "columnList", factory.getColumnList() );
		ctx.req.setAttribute( "condition", conditionMap );
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( true );
		else
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRPartyUOM.MNG") );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_partyuom_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyUOM.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((Party)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean modify( Context ctx ) throws IOException, ServletException, SQLException {
		Party db = (Party)ctx.db;
		int count = 0;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

		ParameterMap paramMap = new ParameterMap( ctx.req );
		List<Map<String, Object>> recordList = paramMap.extractGroupList( "uom" );

		if( recordList != null) {
			for( Map<String, Object> recordMap : recordList ) {
				try {
					recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
					db.modify(recordMap, new String[]{ "updateUserId", "allowUOM"});

					count++;
				} catch( DataException dataEx ) {
					ctx.handler.rollback();
					errorList.add( createErrorMap(recordMap.get("itemCode"), dataEx) );
				} catch( SQLException sqlEx ) {
					ctx.handler.rollback();
					errorList.add( createErrorMap(recordMap.get("itemCode"), sqlEx) );
				}
			}
		}

		// forward & sendRedirect
		if( errorList.size() > 0 ) {
			ctx.handler.rollback();
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS", String.valueOf(count)) );

			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	@Override
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		Object distributionChannelCode = conditionMap.get( "distributionChannelCode" );
		if( distributionChannelCode == null )
			conditionMap.put( "distributionChannelCode", new String[] { getDistributionChannelCode(ctx) } );
		else if( distributionChannelCode instanceof String ){
			conditionMap.put( "distributionChannelCode", new String[] { (String)distributionChannelCode } );
		}

		String ctype = (String)conditionMap.get( "ctype" );
		setAttributeCondition( ctx, conditionMap, ctype );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_attribute_party.jsp" );
	}

	private void splitUOMbyList( Context ctx, List<Map<String, Object>> recordList) {
		if( recordList != null ) {
			for( Map<String, Object> record : recordList ) {
				for( String uom : getUOMValues(ctx) ) {
					String[] recordUOMValues = ( (String)record.get( "allowUOM" ) ).split(",");
					for( String recordUOM : recordUOMValues ) {
						if( recordUOM != null && uom.equals(recordUOM) ) {
							String value = MODE_DOWNLOAD.equals( ctx.mode ) ? "Y" : recordUOM;
							record.put( recordUOM.toLowerCase() + "_uom", value );
							break;
						}
					}
					if( !record.containsKey(uom.toLowerCase() + "_uom") ) {
						String value = MODE_DOWNLOAD.equals( ctx.mode ) ? null : "N";
						record.put( uom.toLowerCase() + "_uom", value );
					}
				}
			}
		}
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		UploadLog logDB = new UploadLog( ctx.handler );

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyUOM.MNG") )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPartyUOM%DOWN" );
		com.irt.html.ColumnListFactory factory = getColumnListFactory( ctx, columnList );

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			defaultMap.put( "status", "00" );
			defaultMap.put( "countryCode", getUserCountryCode(ctx) );
			defaultMap.put( "divisionCode", getDivisionCode(ctx) );
			defaultMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
			defaultMap.put( "distributionChannelCode", getDistributionChannelCode(ctx) );

			Party db = new Party( ctx.handler );
			loader = db.createDataLoader( factory.getColumnList().getFieldKeyArray(), defaultMap, new String[] { "updateUserId", "allowUOM" }, getUOMValues(ctx), Record.UPDATE );

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "DPR" );
			resultMap.put( "uploadType", "Party UOM");
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			String headerInd = ctx.req.getParameter( "headerInd" );
			headerInd = ( "Y".equals(headerInd) ? "Y" : "N" );
			resultMap.put( "headerInd", headerInd );

			RecordFormat messageFormat = PatternRecordFormat
				.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );
			RecordFormat lineNameFormat = PatternRecordFormat.getInstance( "[${partyCode}] - [${organizationCode}] - [${distributionChannelCode}]" );

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

			String uploadInputPath = "DPRPartyUOM?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT;
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
}
