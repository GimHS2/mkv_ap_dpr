/*
 *	File Name:	DPRSalesMov.java
 *	Version:	2.2.5
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_sales_mov_input.jsp
 *		dpr_sales_mov_list.jsp
 *		dpr_sales_mov_upload.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.5	신규 UI/UX 적용
 *	hankalam	2021/03/31		2.2.4	ship-to 레벨 추가
 *	hankalam	2020/06/30		2.2.3	위험상품 레벨 MOV 기능 추가
 *	jbaek		2014/12/31		2.2.2	createTextDataWriter 사용
 *	jbaek		2014/07/06		2.2.1	Sold-to Level MOV 기능 개발
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
import com.irt.data.ManipulableManager;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.cols.ColumnList;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.SalesMov;
import com.irt.dpr.SalesMovPty;
import com.irt.dpr.SalesMovShipPty;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.sql.SQLHandler;


/*
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRSalesMov"})
public class DPRSalesMov extends DPRServletModel {
	/** MOV BASE Type */
	public final static String PARAM_BASETYPE			= "btype";
	public final static String BASETYPE_NORMAL_OFFICE	= "OFFICE";
	public final static String BASETYPE_NORMAL_PARTY	= "PARTY";
	public final static String BASETYPE_NORMAL_SPARTY	= "SPARTY";
	public final static String BASETYPE_DANGER_OFFICE	= "DOFFICE";
	public final static String BASETYPE_DANGER_PARTY	= "DPARTY";
	public final static String BASETYPE_DANGER_SPARTY	= "DSPARTY";

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
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		String btype = (String)ctx.extraObj;
		Condition.putConditionValueOnly( conditionMap, "btype", btype );
		if( BASETYPE_DANGER_OFFICE.equals(btype) || BASETYPE_DANGER_PARTY.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype) ) {
			Condition.putConditionValueOnly( conditionMap, "dangerousInd", "Y" );
		} else {
			Condition.putConditionValueOnly( conditionMap, "dangerousInd", "N" );
		}

		Condition.putConditionValueOnly( conditionMap, "organizationCode", getSavedOrganizationCode( ctx ) );

		return conditionMap;
	}
	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	@Override
	protected boolean doRequest ( Context ctx, boolean isPost ) throws SQLException, IOException, ServletException {
		// 이 서블릿은 Admin사용자만 사용가능
		if( !ctx.sessionMng.isAdminUser() ) return false;
		return super.doRequest( ctx, isPost );
	}

	@Override
	protected com.irt.servlet.SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		ManipulableManager db = (ManipulableManager) ctx.db;
		String btype = (String) ctx.extraObj;

		setColumnList( ctx );
		com.irt.data.cols.ColumnList columnList = (com.irt.data.cols.ColumnList)ctx.req.getAttribute( "columnList" );

		String organizationCode = ctx.req.getParameter( "organizationCode" );
		String distributionChannelCode = ctx.req.getParameter( "distributionChannelCode" );
		String divisionCode = ctx.req.getParameter( "divisionCode" );
		String officeCode = ctx.req.getParameter( "officeCode" );
		String partyCode = ctx.req.getParameter( "partyCode" );
		String shipPartyCode = ctx.req.getParameter( "shipPartyCode" );
		String dangerousInd = ctx.req.getParameter( "dangerousInd" );

		if( organizationCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( distributionChannelCode == null || distributionChannelCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( divisionCode == null || divisionCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( BASETYPE_NORMAL_PARTY.equals(btype) || BASETYPE_DANGER_PARTY.equals(btype) ) {
			if( partyCode == null || partyCode.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			else if( officeCode == null || officeCode.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}
		if( BASETYPE_NORMAL_OFFICE.equals(btype) || BASETYPE_DANGER_OFFICE.equals(btype) ) {
			if( officeCode == null || officeCode.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}
		if( BASETYPE_NORMAL_SPARTY.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype) ) {
			if( shipPartyCode == null || shipPartyCode.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

		if( BASETYPE_DANGER_OFFICE.equals(btype) || BASETYPE_DANGER_PARTY.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype) ) {
			dangerousInd = "Y";
		} else {
			dangerousInd = "N";
		}


		Map<String, Object> primaryMap;
		if( BASETYPE_NORMAL_PARTY.equals(btype) || BASETYPE_DANGER_PARTY.equals(btype)) {
			primaryMap = SalesMovPty.createPrimary( organizationCode, distributionChannelCode, divisionCode, officeCode, partyCode, dangerousInd );
		} else if ( BASETYPE_NORMAL_SPARTY.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype)) {
			primaryMap = SalesMovShipPty.createPrimary( organizationCode, distributionChannelCode, divisionCode, shipPartyCode, dangerousInd );
		} else {
			primaryMap = SalesMov.createPrimary( organizationCode, distributionChannelCode, divisionCode, officeCode, dangerousInd );
		}

		Map<String, Object> recordMap = db.getRecord( primaryMap, columnList.getFieldKeys() );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

		com.irt.dpr.CountryCondition ccnd = new com.irt.dpr.CountryCondition( ctx.handler );
		Map<String, Object> ccndMap = ccnd.getRecord( com.irt.dpr.CountryCondition.createPrimary(ctx.sessionMng.getGln(), organizationCode) );
		recordMap.put( "organizationCode", ccndMap.get( "organizationCode" ) );
		recordMap.put( "organizationName", ccndMap.get( "organizationName" ) );

		ctx.req.setAttribute( "record", recordMap );

		if( inputting ) {
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRSalesMov.INF") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_sales_mov_input.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSalesMov.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSalesMov.MNG" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSalesMov.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSalesMov.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSalesMov.MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRSalesMov.MNG" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRSalesMov.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		String btype = ctx.req.getParameter( PARAM_BASETYPE );
		if( btype == null || btype.length() == 0 )
			btype = BASETYPE_NORMAL_OFFICE;
		ctx.pageConfig.setProperty( "btype", btype );
		ctx.extraObj = btype;

		if( BASETYPE_NORMAL_PARTY.equals(btype) || BASETYPE_DANGER_PARTY.equals(btype) )
			ctx.db = new SalesMovPty( ctx.handler );
		else if( BASETYPE_NORMAL_SPARTY.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype) )
			ctx.db = new SalesMovShipPty( ctx.handler );
		else
			ctx.db = new SalesMov( ctx.handler );

		String titleKey = "TITLE_DPR_SALES_MOV_";
		pageConfig.setTitle( ctx.msghandler.getMessage(titleKey) );
		String prefixTitle = null;
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDangerousItem") ) {
			if( BASETYPE_NORMAL_PARTY.equals(btype) || BASETYPE_NORMAL_SPARTY.equals(btype) || BASETYPE_NORMAL_OFFICE.equals(btype) ) {
				prefixTitle = ctx.msghandler.getMessage( "MSG_DPR_SALES_MOV_NORMAL" );
			} else {
				prefixTitle = ctx.msghandler.getMessage( "MSG_DPR_SALES_MOV_DANGEROUS" );
			}
			prefixTitle += " ";
		}
		titleKey += btype.toUpperCase() +"_";
		pageConfig.setSubTitle( prefixTitle + ctx.msghandler.getMessage(titleKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_PARTNER", "jsp.SUBMENU_SALESMOV" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		QueryableManager db = (QueryableManager) ctx.db;
		String btype = (String) ctx.extraObj;

		setColumnList( ctx );
		com.irt.data.cols.ColumnList columnList = (com.irt.data.cols.ColumnList)ctx.req.getAttribute( "columnList" );

		Map<String, Object> conditionMap = createConditionMap( ctx );
		conditionMap.put( "btype", btype );

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "condition", conditionMap );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );
		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRSalesMov.MNG") );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_sales_mov_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRSalesMov.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((QueryableManager)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		QueryableManager db = (QueryableManager) ctx.db;

		ctx.req.setAttribute( "fieldSet", ((ManipulableManagerImpl) db).getFieldSet(true) );

		String organizationCode = ctx.req.getParameter( "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		Map<String, Object> conditionMap = new ParameterMap( ctx.req, true );
		conditionMap.put( "organizationCode", organizationCode );
		conditionMap.put( "countryCode", getUserCountryCode(ctx) );
		setDefaultParameter( ctx, conditionMap );

		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE );
		setAttributePartner( ctx, conditionMap );

		if( ctx.req.getAttribute("record") == null ) {
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			//default sales org
			com.irt.dpr.CountryCondition ccnd = new com.irt.dpr.CountryCondition( ctx.handler );
			Map<String, Object> ccndMap = ccnd.getRecord( com.irt.dpr.CountryCondition.createPrimary(ctx.sessionMng.getGln(), organizationCode) );
			recordMap.put( "organizationCode", ccndMap.get( "organizationCode" ) );
			recordMap.put( "organizationName", ccndMap.get( "organizationName" ) );
			recordMap.put( "distributionChannelCode", conditionMap.get("distributionChannelCode") );

			String btype = (String) ctx.extraObj;
			if( BASETYPE_DANGER_OFFICE.equals(btype) || BASETYPE_DANGER_PARTY.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype) ) {
				recordMap.put( "dangerousInd", "Y" );
			} else {
				recordMap.put( "dangerousInd", "N" );
			}

			ctx.req.setAttribute( "record", recordMap );
		}

		return forward( ctx, systemConfig.getJspPath() + "/dpr_sales_mov_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, SQLException, ServletException {
		com.irt.data.ManipulableManager db = (ManipulableManager) ctx.db;
		String btype = (String)ctx.extraObj;

		String[] organizationCodes = ctx.req.getParameterValues( "organizationCode" );
		String[] distributionChannelCodes = ctx.req.getParameterValues( "distributionChannelCode" );
		String[] divisionCodes = ctx.req.getParameterValues( "divisionCode" );
		String[] officeCodes = ctx.req.getParameterValues( "officeCode" );
		String[] partyCodes = ctx.req.getParameterValues( "partyCode" );
		String[] shipPartyCodes = ctx.req.getParameterValues( "shipPartyCode" );
		String[] dangerousInds	= ctx.req.getParameterValues( "dangerousInd" );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCodes == null || organizationCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( distributionChannelCodes == null || distributionChannelCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( divisionCodes == null || divisionCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( BASETYPE_NORMAL_PARTY.equals(btype) || BASETYPE_DANGER_PARTY.equals(btype) ) {
			if( partyCodes == null || partyCodes.length == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
			else if( officeCodes == null || officeCodes.length == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}
		if( BASETYPE_NORMAL_OFFICE.equals(btype) || BASETYPE_DANGER_OFFICE.equals(btype) ) {
			if( officeCodes == null || officeCodes.length == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}
		if( BASETYPE_NORMAL_SPARTY.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype) ) {
			if( shipPartyCodes == null || shipPartyCodes.length == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

		// 레코드 삭제
		int count = 0;
		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < organizationCodes.length; i++) {
			if( BASETYPE_NORMAL_PARTY.equals(btype) || BASETYPE_DANGER_PARTY.equals(btype) ) {
				primaryMap = SalesMovPty.createPrimary(  organizationCodes[i], distributionChannelCodes[i],
						divisionCodes[i], officeCodes[i], partyCodes[i], dangerousInds[i] );
			} else if( BASETYPE_NORMAL_SPARTY.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype) ) {
				primaryMap = SalesMovShipPty.createPrimary(  organizationCodes[i], distributionChannelCodes[i],
						divisionCodes[i], shipPartyCodes[i], dangerousInds[i] );
			} else {
				primaryMap = SalesMov.createPrimary(  organizationCodes[i], distributionChannelCodes[i],
						divisionCodes[i], officeCodes[i], dangerousInds[i] );
			}

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
		QueryableManager db = (QueryableManager) ctx.db;
		String btype = (String) ctx.extraObj;
		Map<String, Object> conditionMap = createConditionMap( ctx );

		setColumnList( ctx );
		com.irt.data.cols.ColumnList columnList = (com.irt.data.cols.ColumnList)ctx.req.getAttribute( "columnList" );

		String fileNamePrefix = "DPR_SALES_MOV_";
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDangerousItem") ) {
			fileNamePrefix += "USEDANGER_";
		}

		String filename = ctx.msghandler.getMessage( fileNamePrefix + btype + "_DOWNLOAD_FILE" );
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
		ManipulableManager db = (ManipulableManager) ctx.db;
		String btype = (String) ctx.extraObj;

		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		if( BASETYPE_DANGER_PARTY.equals(btype) || BASETYPE_DANGER_OFFICE.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype) ) {
			recordMap.put( "dangerousInd", "Y" );
		} else {
			recordMap.put( "dangerousInd", "N" );
		}
		recordMap.put( "divisionCode", getDivisionCode(ctx) );
		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "fieldSet", db.getFieldSet(inserting) );

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

			return forward( ctx, systemConfig.getJspPath() + "/dpr_sales_mov_input.jsp" );
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
		String titlePrefixKey = "TITLE_DPR_SALES_MOV_";
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDangerousItem") ) {
			titlePrefixKey += "USEDANGER_";
		}
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage(titlePrefixKey + btype.toUpperCase()+"_"+ mode.toUpperCase()) );

		return registInput( ctx );
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		UploadLog logDB = new UploadLog( ctx.handler );

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRSalesMov.MNG") )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		setColumnList( ctx );
		com.irt.data.cols.ColumnList columnList = (ColumnList) ctx.req.getAttribute( "columnList" );
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );

			ManipulableManager db = (ManipulableManager) ctx.db;
			String uploadType, lineNameFormatString;
			String btype = (String) ctx.extraObj;
			if( BASETYPE_NORMAL_PARTY.equals(btype) || BASETYPE_DANGER_PARTY.equals(btype) ) {
				if( BASETYPE_NORMAL_PARTY.equals(btype) ) {
					uploadType = "SMOVPTY";
					defaultMap.put( "dangerousInd", "N" );
				} else {
					uploadType = "DSMOVPTY";
					defaultMap.put( "dangerousInd", "Y" );
				}
				lineNameFormatString = "[${organizationCode}] - [${distributionChannelCode}] - [${divisionCode}] - [${officeCode}] - [${partyCode}]";
			} else if( BASETYPE_NORMAL_SPARTY.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype) ) {
				if( BASETYPE_NORMAL_SPARTY.equals(btype) ) {
					uploadType = "SMOVSPTY";
					defaultMap.put( "dangerousInd", "N" );
				} else {
					uploadType = "DSMOVSPTY";
					defaultMap.put( "dangerousInd", "Y" );
				}
				lineNameFormatString = "[${organizationCode}] - [${distributionChannelCode}] - [${divisionCode}] - [${shipPartyCode}]";
			} else {
				if( BASETYPE_NORMAL_OFFICE.equals(btype) ) {
					uploadType = "SALESMOV";
					defaultMap.put( "dangerousInd", "N" );
				} else {
					uploadType = "DSALESMOV";
					defaultMap.put( "dangerousInd", "Y" );
				}
				lineNameFormatString = "[${organizationCode}] - [${distributionChannelCode}] - [${divisionCode}] - [${officeCode}]";
			}

			loader = db.createDataLoader( fieldKeys, defaultMap, new String[] { "minimumValue", "updateUserId" }, Record.INSERT_OR_UPDATE_OR_DELETE);
			loader.setLoaderOption( "deleteCheckingKey", "minimumValue" );
			validator = new AuthValidator(ctx);

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "DPR" );
			resultMap.put( "uploadType", uploadType );
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			String headerInd = ctx.req.getParameter( "headerInd" );
			headerInd = ( "Y".equals(headerInd) ? "Y" : "N" );
			resultMap.put( "headerInd", headerInd );

			RecordFormat messageFormat = PatternRecordFormat.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );
			RecordFormat lineNameFormat = PatternRecordFormat.getInstance( lineNameFormatString );

			loaderLogger = logDB.createLogger( resultMap, messageFormat, lineNameFormat );
			DataLoader dataLoader = createTextDataLoader( ctx, ctx.handler, loader, loaderLogger, validator, true );

			try {
				/*
				Column[] columns = columnList.getColumns();
				String[] titles = null;
				DataReader reader = dataLoader.getDataReader();
				java.io.PrintStream out = dataLoader.getErrorPrintStream();
				for( int i = 0; i < headerCount && !reader.isEOF(); i++ ) {
					try {
						titles = reader.readNext();
					} catch( DataException dataEx ) {}
					if( reader.getLineString() != null && out != null )
						out.println( reader.getLineString() );

					// columnList columnTitle vs 업로드된 파일의 Title 비교.
					if( headerCount > 0 ) {
					int cnt = 0;
					if( columns.length <= titles.length ) {
						for( int j = 0; j < columns.length; j++ )
							if( columns[j].getColumnTitle().equals(titles[j]) ) cnt++;

						if( columns.length != cnt )
							throw new ServletModelException( "ERR_INTERNAL_ERROR" );
					} else
						throw new ServletModelException( "ERR_INTERNAL_ERROR" );
					}
				}
				*/
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

			String uploadInputPath = "DPRSalesMov?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT;
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

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
		conditionMap.put( "countryCode", getUserCountryCode(ctx) );
		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION );

		com.irt.dpr.CountryCondition ccnd = new com.irt.dpr.CountryCondition( ctx.handler );
		Map<String, Object> ccndMap = ccnd.getRecord( com.irt.dpr.CountryCondition.createPrimary(ctx.sessionMng.getGln(), organizationCode) );
		recordMap.putAll(ccndMap);
		recordMap.put( "encoding", "UTF8" );
		ctx.req.setAttribute( "record", recordMap );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_sales_mov_upload.jsp" );
	}

	private void setColumnList( Context ctx ) throws ServletException {
		String btype = (String) ctx.extraObj;

		String columnListType;
		if( com.irt.servlet.ServletModel.MODE_DOWNLOAD.equals(ctx.mode) || com.irt.servlet.ServletModel.MODE_UPLOAD.equals(ctx.mode) )
			columnListType = "DOWN";
		else
			columnListType = "LIST";

		com.irt.data.cols.ColumnList columnList;
		if( BASETYPE_NORMAL_PARTY.equals(btype) || BASETYPE_DANGER_PARTY.equals(btype) ) {
			columnList = getColumnList( ctx, "DPRSalesMov.Party%" + columnListType );
		} else if( BASETYPE_NORMAL_SPARTY.equals(btype) || BASETYPE_DANGER_SPARTY.equals(btype) ) {
			columnList = getColumnList( ctx, "DPRSalesMov.ShipParty%" + columnListType );
		} else {
			columnList = getColumnList( ctx, "DPRSalesMov%" + columnListType );
		}
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "btype", btype );
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

	protected boolean isValidRecord( String btype, ColumnList columnList, Map<String, Object> recordMap ) {
		com.irt.data.cols.Column[] columns = columnList.getColumns();
		int cnt = 0;

		// 다운로드 칼럼의 갯수와 recordMap의 갯수 비교.
		for( com.irt.data.cols.Column column : columns )
			if( null != recordMap.get( column.getFieldKey() ) ) cnt++;

		if( columns.length == cnt )
			return true;
		else
			return false;
	}

	protected class AuthValidator implements com.irt.data.DataLoader.Validator {
		String userId, countryCode, organizationCode, btype;
		ColumnList columnList;
		boolean isAdminUser;

		public AuthValidator( Context ctx ) {
			this.userId = ctx.sessionMng.getUniqId();
			this.countryCode = ctx.sessionMng.getGln();
			this.organizationCode = ctx.sessionMng.getExtraValue();
			this.isAdminUser = ctx.sessionMng.isAdminUser();
			this.btype = (String) ctx.extraObj;
			this.columnList = (ColumnList) ctx.req.getAttribute( "columnList" );
		}

		@Override
		public void close() {}

		@Override
		public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
			int authLevel = getAuthLevel( handler, userId, countryCode, organizationCode, isAdminUser, recordMap );


			if(	!isValidRecord(btype, columnList, recordMap) )
				throw handler.createDataException( "ERR_HAS_INVALID", recordMap );

			if( authLevel != AUTHLEVEL_MANAGE )
				throw handler.createDataException( "ERR_HAS_NOAUTH", recordMap );
			else
				return;
		}
	}

}
