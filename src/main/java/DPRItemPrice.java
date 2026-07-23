/*
 *	File Name:	DPRItemPrice.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		pub_upload_input.jsp
 *		dpr_itemprice_input.jsp
 *		dpr_itemprice_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.3	신규 UI/UX 적용
 *	hankalam	2019/06/30		2.2.2	Distribution, Group, Party 적용
 *	jbaek		2019/01/30		2.2.1	isAdminUser 조건 삭제.
 *	song7981	2016/05/20		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataLoader.Validator;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.CountryCondition;
import com.irt.dpr.ItemPrice;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
/*
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRItemPrice"})
public class DPRItemPrice extends DPRServletModel {
	protected final static String PARAM_TYPE					= "type";
	protected final static String TYPE_SOLD						= "sold";
	protected final static String TYPE_SHIP						= "ship";
	public static String MODE_COND_SETTING = "rtp" ;

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletModelException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		setDefaultParameterMultiDist( ctx, conditionMap );
		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode(ctx) );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );

		if( conditionMap.containsKey("itemCode") ) {
			Condition.putConditionValueOnly( conditionMap, "itemCode", conditionMap.get("itemCode"), Condition.CONDTYPE_CONTAINS );
		}
		if( conditionMap.containsKey("itemName") ) {
			Condition.putConditionValueOnly( conditionMap, "itemName", conditionMap.get("itemName"), Condition.CONDTYPE_CONTAINS );
		}

		if( conditionMap.containsKey("itemConsumerEANCode") ) {
			Condition.putConditionValueOnly( conditionMap, "itemConsumerEANCode", conditionMap.get("itemConsumerEANCode"), Condition.CONDTYPE_CONTAINS );
		}

		return conditionMap;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	@Override
	protected void initContext(Context ctx) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemPrice.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemPrice.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemPrice.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemPrice.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemPrice.MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemPrice.LST" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRItemPrice.MNG" );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new ItemPrice( ctx.handler );

		String messageKey = "TITLE_DPR_ITEMPRICE_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_MATERIALPRICE" );
	}

	@Override
	protected boolean download( Context ctx ) throws ServletException, IOException, SQLException {
		ItemPrice db = (ItemPrice)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		boolean useDetailCondtion = com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDetailCondition");
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRItemPrice%DOWN", useDetailCondtion ? "D" : null );
		DataWriter out = createTextDataWriter( ctx, "ItemPrice" + "_" + getSavedOrganizationCode(ctx) );

		try{
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			db.write( out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE );
		} catch( SQLException sqlEx) {
			out.println();
			out.print( sqlEx.getMessage() );
			logger.error("Internal Error", sqlEx );
		} finally {
			out.flush();
			out.close();
		}

		return true;
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		ItemPrice db = (ItemPrice)ctx.db;
		Map<String, Object> conditionMap = createConditionMap( ctx );
		boolean useDetailCondtion = com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useDetailCondition");

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRItemPrice%LIST", useDetailCondtion ? "D" : null );

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		if( com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useDetailCondition") ) {
			Map<String, Object> _conditionMap = new java.util.HashMap<String, Object>();
			_conditionMap.put( "organizationCode", conditionMap.get("organizationCode") );
			_conditionMap.put( "distributionChannelCode", conditionMap.get("distributionChannelCode") );
			_conditionMap.put( "officeCode", conditionMap.get("officeCode") );
			_conditionMap.put( "groupCode", conditionMap.get("groupCode") );
			_conditionMap.put( "countryCode", getUserCountryCode(ctx) );
			setAttributePartyMaster( ctx, _conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP );
			setAttributePartner( ctx, _conditionMap );

		} else {
			setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_OFFICE );
		}

		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );

		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRItemPrice.MNG") );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_itemprice_list.jsp" );
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_COND_SETTING.equals(ctx.mode) )
			return setAttributeCondition( ctx );

		return super.doRequest( ctx, isPost );
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

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRItemPrice.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((ItemPrice)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean upload( Context ctx ) throws ServletException, SQLException, IOException {
		UploadLog logDB = new UploadLog( ctx.handler );

		boolean useDetailCondtion = com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDetailCondition");
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRItemPrice%DOWN", useDetailCondtion ? "D" : null );

		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
			defaultMap.put( "divisionCode", getDivisionCode(ctx) );
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );

			ItemPrice db = (ItemPrice)ctx.db;

			String[] updateFieldKeys = { "itemPrice", "updateUserId" };
			loader = db.createDataLoader( fieldKeys, defaultMap, updateFieldKeys, Record.INSERT_OR_UPDATE_OR_DELETE );
			validator = db.createValidator(defaultMap);

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "RBM" );
			resultMap.put( "uploadType", "PRICE" );
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			RecordFormat messageFormat = PatternRecordFormat.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );
			RecordFormat lineNameFormat = PatternRecordFormat.getInstance( "[${officeCode}] - [${groupCode}] - [${partyCode}] - [${itemCode}]" );

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

			String uploadInputPath = "DPRItemPrice?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT;
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

		recordMap.put("encoding", "EUC-KR");
		ctx.req.setAttribute("record", recordMap );

		// encoding 값이 없을 시에 pub_upload_input.jsp 페이지에서 systemConfig의 언어별 encoding을 가져와서 처리함.
		return forward( ctx, systemConfig.getJspPath() +"/pub_upload_input.jsp" );
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		ItemPrice db = (ItemPrice)ctx.db;

		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		recordMap.put( "divisionCode", getDivisionCode(ctx) );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );

		recordMap = replaceNullValues( ctx, recordMap );

		Validator validator = db.createValidator(recordMap);
		try {
			if( validator != null)
				validator.validateLine(ctx.handler, recordMap);
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			String message = ctx.handler.getMessageHandler().getMessage(dataEx.getMessage());
			ctx.pageConfig.setMessage(message);
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			return registInput(ctx);
		} finally {
			if( validator != null )
				validator.close();
		}

		try {
			if( inserting ) {
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

			return forward( ctx, systemConfig.getJspPath() + "/dpr_itemprice_input.jsp" );
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
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ITEMPRICE_" + mode.toUpperCase()) );

		return registInput(ctx);
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		if( ctx.req.getAttribute("record") == null ) {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			com.irt.dpr.CountryCondition ccnd = new com.irt.dpr.CountryCondition( ctx.handler );
			Map<String, Object> ccndMap = ccnd.getRecord( CountryCondition.createPrimary(ctx.sessionMng.getGln(), organizationCode) );

			recordMap.put( "organizationCode", ccndMap.get( "organizationCode" ) );
			recordMap.put( "organizationName", ccndMap.get( "organizationName" ) );

			/*setAttributePartyMasterOnExisting( ctx, createConditionMap(ctx)
					, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP );
*/
			ctx.req.setAttribute( "fieldSet", ((ItemPrice)ctx.db).getFieldSet(true) );
			ctx.req.setAttribute( "record", recordMap );
		}

		Map<String, Object> conditionMap = createConditionMap( ctx );
		if( com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useDetailCondition") ) {
			setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP );
		} else {
			setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE );
		}

		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_itemprice_input.jsp");
	}

	@Override
	protected boolean remove( Context ctx ) throws SQLException, IOException, ServletException {
		ItemPrice db = (ItemPrice)ctx.db;

		String[] organizationCodes = ctx.req.getParameterValues( "organizationCode" );
		String[] distributionChannelCodes = ctx.req.getParameterValues( "distributionChannelCode" );
		String[] officeCodes = ctx.req.getParameterValues( "officeCode" );
		String[] groupCodes = ctx.req.getParameterValues( "groupCode" );
		String[] partyCodes = ctx.req.getParameterValues( "partyCode" );
		String[] itemCodes = ctx.req.getParameterValues( "itemCode" );


		String del = ctx.req.getParameter( "isdeleteAll" );
		String organizationCode_all = getSavedOrganizationCode( ctx );

		if( del == null ) {
			if( ctx.pageConfig.getBackURL() == null )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			else if( organizationCodes == null || organizationCodes.length== 0 )
					throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
			else if( itemCodes == null || itemCodes.length == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

		int count = 0;

		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

		if( del != null ) {
			try {
				if( del != null )
					db.deleteAll( organizationCode_all );	//Data 전체 삭제
			} catch ( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getFieldValue(primaryMap, "title"), dataEx) );
			}
		}

		if( del == null ) {
			for( int i = 0; i < organizationCodes.length; i++) {
				primaryMap = ItemPrice.createPrimary( organizationCodes[i], distributionChannelCodes[i], officeCodes[i], groupCodes[i], partyCodes[i], itemCodes[i] );
				try {
					if( db.delete(primaryMap) ) {
						count++;
						ctx.handler.commit();
					}
				} catch ( DataException dataEx ) {
					ctx.handler.rollback();
					errorList.add( createErrorMap(db.getFieldValue(primaryMap, "title"), dataEx) );
				}
			}
		}

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			if( del != null ) {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_ALL_SUCCESS") );
			} else {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)) );
			}

		if ( ctx.pageConfig.getBackURL() == null ) {
			String redirectURL = systemConfig.getClassURL() + "/DPRItemPrice?menu=portal"
					+ "&locale=" + getDisplayLanguage(ctx)
					+ "&organizationCode=" + getSavedOrganizationCode(ctx);
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery( redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}

		return sendRedirect( ctx, HtmlUtility.replaceURLQuery( ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	@Override
	public boolean info ( Context ctx, boolean inserting ) throws SQLException, IOException, ServletException {
		ItemPrice db = (ItemPrice)ctx.db;

		String organizationCode = ctx.req.getParameter( "organizationCode" );
		String distributionChannelCode = ctx.req.getParameter( "distributionChannelCode" );
		String officeCode = ctx.req.getParameter( "officeCode" );
		String groupCode = ctx.req.getParameter( "groupCode" );
		String partyCode = ctx.req.getParameter( "partyCode" );
		String itemCode= ctx.req.getParameter( "itemCode" );

		Map<String, Object> primaryMap = ItemPrice.createPrimary( organizationCode, distributionChannelCode, officeCode, groupCode, partyCode, itemCode );
		primaryMap = replaceNullValues( ctx, primaryMap );
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap );

		// forward
		if( inserting ) {
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRItemPrice.MNG") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_itemprice_input.jsp" );
		}
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

	public Map<String, Object> replaceNullValues( Context ctx, Map<String, Object> recordMap ) {
		String[] nullValueFieldKeys;
		if( com.irt.dpr.Country.isFeature( getSavedOrganizationCode(ctx), "useDetailCondition") ) {
			nullValueFieldKeys = new String[] { "officeCode", "groupCode", "partyCode" };
		} else {
			nullValueFieldKeys = new String[] { "distributionChannelCode", "groupCode", "partyCode" };
		}
		return replaceNullValues( recordMap, nullValueFieldKeys, NULL_VALUE );
	}

	public void setAttributePartner( Context ctx, Map<String, Object> conditionMap, String type ) throws ServletModelException, SQLException {
		String soldPartyCode = null;
		soldPartyCode = getUserPartyCode( ctx );

		Map <String, Object> _condition = new java.util.HashMap<String, Object> ( conditionMap );
		_condition.remove( "partyCode" );

		setAttributePartner( ctx, _condition, PARTNER_SOLD );

		soldPartyCode = ctx.req.getParameter( "partyCode" );
		if( soldPartyCode != null && soldPartyCode.length() > 0 )
			_condition.put( "partyCode", soldPartyCode );

		setAttributePartner( ctx, _condition, PARTNER_SHIP );
	}

	protected boolean setTradePartner( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		String type = ctx.req.getParameter( PARAM_TYPE );

		setDefaultParameter( ctx, conditionMap );
		if( type == null || type.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( TYPE_SOLD.equals(type) ) {
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

		return forward( ctx, systemConfig.getJspPath() +"/dpr_item_cond.jsp" );
	}
}
