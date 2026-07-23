/*
 *	File Name:	DPRPlantItem.java
 *	Version:	2.2.6
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_plantitem_upload.jsp
 *		dpr_sales_mov_input.jsp
 *		dpr_sales_mov_list.jsp
 *		dpr_sales_mov_upload.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.6	신규 UI/UX 적용
 *	jbaek		2019/07/30		2.2.5	itemName 조건 변경.
 *	jbaek		2018/10/30		2.2.4	Sales Office별 Plant Exclusion 기능 추가
 *	hankalam	2017/02/28		2.2.3	Delete & Insert 기능 추가
 *	jbaek		2015/04/07		2.2.2	plantCode 를 PK에 추가
 *	jbaek		2014/12/31		2.2.1	createTextDataWriter 사용
 *	jbaek		2013/04/30		2.2.0	create
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
import com.irt.dpr.PlantItem;
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
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRPlantItem"})
public class DPRPlantItem extends DPRServletModel {

	public final static String MODE_COND_SETTING		= "rtp";

	public final static String PARAM_PLANTTYPE			= "pttype";
	public final static String PLANTTYPE_EXCLUDE		= "excl";
	public final static String PLANTTYPE_NONE			= "none";

	@Override
	protected DataLoader createTextDataLoader( Context ctx, SQLHandler handler, DataLoader.Loader loader, DataLoader.Logger loaderLogger
						, DataLoader.Validator validator, boolean commitByLine ) throws IOException, ServletException {

		// Basic Validation and Check File Extension
		if( !(ctx.req instanceof com.irt.servlet.MultipartHttpRequest) )
			throw new ServletModelException( ServletModelException.INVALID_REQUEST );
		com.irt.servlet.MultipartHttpRequest req = (com.irt.servlet.MultipartHttpRequest)ctx.req;
		if( ( req.getFile("file")) == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		String inputFileName = req.getInputFileName( "file" );
		String fileExt = inputFileName.substring( inputFileName.lastIndexOf(".") + 1, inputFileName.length() ).toLowerCase();
		if( !("CSV".equals(fileExt.toUpperCase()) || "TAB".equals(fileExt.toUpperCase()) || "TXT".equals(fileExt.toUpperCase())) )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER, ctx.msghandler.getMessage("ERR_INVALID_FILETYPE", fileExt.toUpperCase()) );

		return super.createTextDataLoader(ctx, handler, loader, loaderLogger, validator, commitByLine);
	}

	protected Map<String, Object> createConditionMap ( Context ctx ) throws SQLException, IOException, ServletException {
		ParameterMap parameterMap = new ParameterMap( ctx.req, true );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		// organizationCode, divisionCode, distributionChannelCode
		setDefaultParameterMultiDist( ctx, conditionMap );
		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode(ctx) );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );

		if( parameterMap.containsKey("itemCode") ) {
			Condition.putConditionValueOnly(conditionMap, "itemCode", parameterMap.get("itemCode"), Condition.CONDTYPE_CONTAINS );
		}
		if( parameterMap.containsKey("itemName") ) {
			Condition.putConditionValueOnly(conditionMap, "itemName", parameterMap.get("itemName"), Condition.CONDTYPE_CONTAINS);
		}
		if( parameterMap.containsKey("plantCode") ) {
			Condition.putConditionValueOnly(conditionMap, "plantCode", parameterMap.get("plantCode") );
		}
		if( parameterMap.containsKey("shipPartyCode") ) {
			Condition.putConditionValueOnly(conditionMap, "shipPartyCode", parameterMap.get("shipPartyCode") );
		}

		return conditionMap;
	}

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	@Override
	protected boolean doRequest ( Context ctx, boolean isPost ) throws SQLException, IOException, ServletException {
		if( MODE_COND_SETTING.equals(ctx.mode) )
			return setAttributeCondition( ctx );

		return super.doRequest( ctx, isPost );
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		PlantItem db = (PlantItem)ctx.db;

		String plantCode = ctx.req.getParameter( "plantCode" );
		String shipPartyCode = ctx.req.getParameter( "shipPartyCode" );
		String officeCode = ctx.req.getParameter( "officeCode" );
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		String distributionChannelCode = ctx.req.getParameter( "distributionChannelCode" );
		String itemCode = ctx.req.getParameter( "itemCode" );

		if( plantCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( shipPartyCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( officeCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( organizationCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( distributionChannelCode == null || distributionChannelCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( itemCode == null || itemCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		Map<String, Object> primaryMap = PlantItem.createPrimary( plantCode, shipPartyCode, officeCode, organizationCode, distributionChannelCode, itemCode );

		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap );

		if( inputting ) {
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRPlantItem.MNG") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			ctx.req.setAttribute( "fieldSet_new", db.getFieldSet(false) );
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRPlantItem.MNG") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_plantitem_input.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPlantItem.INF" );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPlantItem.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPlantItem.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPlantItem.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPlantItem.MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRPlantItem.DWN" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPlantItem.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		// Plant Status Type
		String pttype = ctx.req.getParameter( PARAM_PLANTTYPE );
		if( pttype == null || pttype.length() == 0 )
			pttype = PLANTTYPE_EXCLUDE;
		ctx.pageConfig.setProperty( "pttype", pttype );

		ctx.db = new PlantItem( ctx.handler );
		String messageKey = "TITLE_DPR_PLANTITEM_"+ pttype.toUpperCase() + "_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_PLANTSKU" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		PlantItem db = (PlantItem)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPlantItem%LIST" );

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		//setAttribute
		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL );
		{
			Map <String, Object> _condition = new java.util.HashMap<String, Object> ( conditionMap );
			_condition.remove( "partyCode" );

			_condition.put(Condition.DISTINCT_CONDITIONKEY, "Y");
			setAttributePartner( ctx, _condition, PARTNER_SHIP );
		}

		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );

		Map <String, Object> _condition = new java.util.HashMap<String, Object> ( conditionMap );
		_condition.remove("partyCode");
		_condition.remove("plantCode");
		_condition.put(Condition.DISTINCT_CONDITIONKEY, "Y");
		com.irt.dpr.Plant plant = new com.irt.dpr.Plant( ctx.handler );
		plant.setSort( "linkPlantCode" );
		List<Map<String, Object>> plantList = plant.getRecords( _condition,new String[]{"linkPlantCode", "linkPlantName"} );
		ctx.req.setAttribute( "plants", plantList );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );
		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRPlantItem.MNG") );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_plantitem_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRPlantItem.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((PlantItem)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}


	@Override
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> paramMap = new ParameterMap( ctx.req );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>( paramMap );
		setDefaultParameterMultiDist( ctx, conditionMap );

		String ctype = (String)conditionMap.get( "ctype" );
		setAttributeCondition( ctx, conditionMap, ctype );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_attribute_party.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		if( ctx.req.getAttribute("record") == null ) {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			com.irt.dpr.CountryCondition ccnd = new com.irt.dpr.CountryCondition( ctx.handler );
			Map<String, Object> ccndMap = ccnd.getRecord( com.irt.dpr.CountryCondition.createPrimary(ctx.sessionMng.getGln(), organizationCode) );

			recordMap.put( "organizationCode", ccndMap.get( "organizationCode" ) );
			recordMap.put( "organizationName", ccndMap.get( "organizationName" ) );

			ctx.req.setAttribute( "record", recordMap );
		}

		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", organizationCode );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );
		ctx.req.setAttribute( "fieldSet", ((PlantItem)ctx.db).getFieldSet(true) );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_plantitem_input.jsp");
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, SQLException, ServletException {
		PlantItem db = (PlantItem) ctx.db;

		String[] plantCodes = ctx.req.getParameterValues( "plantCode" );
		String[] shipPartyCodes = ctx.req.getParameterValues( "shipPartyCode" );
		String[] officeCodes = ctx.req.getParameterValues( "officeCode" );
		String[] organizationCodes = ctx.req.getParameterValues( "organizationCode" );
		String[] distributionChannelCodes = ctx.req.getParameterValues( "distributionChannelCode" );
		String[] itemCodes = ctx.req.getParameterValues( "itemCode" );


		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( plantCodes == null || plantCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( shipPartyCodes == null || shipPartyCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( officeCodes == null || shipPartyCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCodes == null || organizationCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( distributionChannelCodes == null || distributionChannelCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( itemCodes == null || itemCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		// 레코드 삭제
		int count = 0;
		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < organizationCodes.length; i++) {
			primaryMap = PlantItem.createPrimary( plantCodes[i], shipPartyCodes[i], officeCodes[i], organizationCodes[i], distributionChannelCodes[i], itemCodes[i] );
			try {
				if( db.delete(primaryMap) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getFieldValue(primaryMap, "title"), dataEx) );
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
	protected boolean download( Context ctx) throws IOException, ServletException, SQLException {
		PlantItem db = (PlantItem) ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPlantItem%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String filename = ctx.msghandler.getMessage("DPR_PLANTITEM_"+ ctx.pageConfig.getProperty("pttype").toUpperCase() +"_DOWNLOAD_FILE");
		DataWriter out = createTextDataWriter( ctx, filename );

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
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		PlantItem db = (PlantItem)ctx.db;

		Map<String, Object> recordMap = new ParameterMap( ctx.req, true );
		setDefaultParameter( ctx, recordMap );
		Condition.putConditionValueOnly( recordMap, "countryCode", getUserCountryCode(ctx) );
		Condition.putConditionValueOnly( recordMap, "displayLanguage", getDisplayLanguage(ctx) );

		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );

		try {
			if( inserting ) {
				db.regist( recordMap );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );

				ctx.req.setAttribute( "record", db.getRecord(recordMap) );
			} else {
				Map<String, Object> recordMap_old = PlantItem.createPrimary( (String)recordMap.get("plantCode"), (String)recordMap.get("shipPartyCode"), (String)recordMap.get("officeCode"), (String)recordMap.get("organizationCode"), (String)recordMap.get("distributionChannelCode"),(String)recordMap.get("itemCode") );
				Map<String, Object> recordMap_new = PlantItem.createPrimary( (String)recordMap.get("plantCode_new"), (String)recordMap.get("shipPartyCode_new"), (String)recordMap.get("officeCode_new"), (String)recordMap.get("organizationCode_new"), (String)recordMap.get("distributionChannelCode_new"),(String)recordMap.get("itemCode_new") );

				for(String fieldKey : recordMap.keySet() ) {
					if( !recordMap_new.containsKey(fieldKey) )
						recordMap_new.put( fieldKey, recordMap.get(fieldKey) );
				}
				try {
					if( !db.delete(recordMap_old) ) {
						String message = "recordMap_old: "+ recordMap_old + " recordMap_new: "+ recordMap_new;
						logger.debug( "delete failed. " + message );
						throw ctx.handler.createDataException( DataException.ERR_CANNOT_DELETE );
					}

					if( !db.regist(recordMap_new) ) {
						String message = "recordMap_old: "+ recordMap_old + " recordMap_new: "+ recordMap_new;
						logger.debug( "regist failed. " + message );
						throw ctx.handler.createDataException( DataException.ERR_CANNOT_INSERT );
					}
				} catch( SQLException sqlEx ) {
					List<String> fieldKeys_new = new java.util.ArrayList<String>();
					for(String fieldKey : recordMap.keySet() ) {
						if( fieldKey.endsWith("_new") ) {
							fieldKeys_new.add(fieldKey);
						}
					}
					for( String fieldKey_new : fieldKeys_new ) {
						recordMap.remove(fieldKey_new);
					}
					ctx.req.setAttribute( "record", recordMap );
					throw sqlEx;
				}

				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
				ctx.req.setAttribute( "record", db.getRecord(recordMap_new) );
			}

			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_plantitem_input.jsp" );
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
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PLANTITEM_"+ ctx.pageConfig.getProperty("pttype").toUpperCase() +"_"+ mode.toUpperCase()) );

		return registInput(ctx);
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		UploadLog logDB = new UploadLog( ctx.handler );

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPlantItem.MNG") )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRPlantItem%DOWN" );
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			defaultMap.put( "status", PlantItem.PLANTITEMSTATUS_EXCLUDE );
			defaultMap.put( "countryCode", getUserCountryCode(ctx) );
			defaultMap.put( "divisionCode", getDivisionCode(ctx) );

			String uploadOption = ctx.req.getParameter( "uploadOption" );
			PlantItem db = new PlantItem( ctx.handler );
			loader = db.createDataLoader( fieldKeys, defaultMap, new String[] { "updateUserId" }, Record.INSERT | Record.UPDATE, uploadOption );
//			loader.setLoaderOption( "deleteCheckingKey", "plantCode" );
			validator = new AuthValidator(ctx);

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "DPR" );
			resultMap.put( "uploadType", "PLTEX");
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			String headerInd = ctx.req.getParameter( "headerInd" );
			headerInd = ( "Y".equals(headerInd) ? "Y" : "N" );
			resultMap.put( "headerInd", headerInd );

			RecordFormat messageFormat = PatternRecordFormat
				.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );
			RecordFormat lineNameFormat = PatternRecordFormat.getInstance( "[${plantCode}] - [${shipPartyCode}] - [${officeCode}] - [${organizationCode}] - [${distributionChannelCode}] - [${itemCode}]" );

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

			String uploadInputPath = "DPRPlantItem?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT;
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
		if( !recordMap.containsKey("uploadOption") ) {
			recordMap.put( "uploadOption", "UPD" );
		}
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
