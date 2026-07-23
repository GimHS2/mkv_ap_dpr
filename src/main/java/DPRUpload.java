/*
 *	File Name:	DPRUpload.java
 *	Version:	2.2.5
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_upload_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.5	신규 UI/UX 적용
 *	hankalam	2020/12/31		2.2.4	Upload Option 기능 추가
 *	hankalam	2019/08/31		2.2.3	download(): Selling SKU Upload 시 China Upload 양식 구분
 *	jbaek		2014/12/31		2.2.2	createTextDataWriter 사용
 *	jbaek		2014/09/30		2.2.1	Product Hierarchy Level 기능 개발
 *	guksm		2008/09/26		2.2.0	create
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
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.Upload;
import com.irt.dpr.UploadDetail;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.rbm.UploadLog;
import com.irt.servlet.MultipartHttpRequest;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;
import com.irt.sql.SQLHandler;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRUpload"})
public class DPRUpload extends DPRServletModel {
	public static String UPLOADTYPE_CUSTOMER_ID_MAPPING		= "CIM";
	public static String UPLOADTYPE_CUSTOMER_MASTER			= "CMT";
	public static String UPLOADTYPE_INVENTORY				= "INV";
	public static String UPLOADTYPE_ORDER_DETAIL			= "ORD";
	public static String UPLOADTYPE_SELLOUT					= "SEO";
	public static String UPLOADTYPE_SKU_MAPPING				= "SKM";
	public static String UPLOADTYPE_SELLING_SKU_LIST		= "SSL";
	public static String UPLOADTYPE_PRODUCT_CATEGORY_DESCRIPTION			= "PCD";

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	private Map<String, Object> createConditionMap( Context ctx ) throws ServletException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		String startUploadDate = Record.extractString( conditionMap, "startUploadDate" );
		String endUploadDate = Record.extractString( conditionMap, "endUploadDate" );

		String dateConditionType = Condition.CONDTYPE_EQUALS_MINMAX;
		com.irt.data.Date startDate = null;
		com.irt.data.Date endDate = null;
		try {
			if( startUploadDate != null ) startDate = com.irt.data.Date.getInstance( startUploadDate );
			if( endUploadDate != null ) endDate = com.irt.data.Date.getInstance(endUploadDate).getDate(1);
		} catch( java.text.ParseException parseEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		if( startDate == null || endDate == null ) {
			if( startDate == null ) {
				startDate = com.irt.data.Date.getInstance( ctx.sessionMng.getTimeZone() );
				conditionMap.put( "startUploadDate", startDate );
			}
			endDate = startDate.getDate( 1 );

			dateConditionType = Condition.CONDTYPE_EQUALS_MIN;
		}
		conditionMap.put( "uploadDate"+ Condition.SUFFIX_MIN_VALUE, startDate );
		conditionMap.put( "uploadDate"+ Condition.SUFFIX_MAX_VALUE, endDate );
		conditionMap.put( "uploadDate"+ Condition.SUFFIX_TYPE, dateConditionType );
		conditionMap.put( "timeZone", ctx.sessionMng.getTimeZone() );

		conditionMap.put( "organizationCode", getSavedOrganizationCode( ctx ));

		if( !ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin() )
			Condition.putConditionValueOnly( conditionMap, "uploadUserId", ctx.sessionMng.getUniqId() );

		return conditionMap;
	}

	private boolean downloadRBM( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		String uploadType = Record.extractString( conditionMap, "uploadType" );
		if( UPLOADTYPE_PRODUCT_CATEGORY_DESCRIPTION.equals( uploadType ) ) {
			com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRUpload%DOWN."+ uploadType.toUpperCase()
					, new String[] { com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML } );

			com.irt.dpr.MasterDesc master_organization = new com.irt.dpr.MasterDesc( ctx.handler );
			conditionMap.put( "masterType", com.irt.dpr.MasterLink.MASTERTYPE_PRODUCT_CATEGORY );

			String masterLanguageCode = ctx.req.getParameter( "masterLangCode" );
			String masterOrganizationCode = ctx.req.getParameter( "organizationCode" );
			conditionMap.put( "masterLanguageCode", masterLanguageCode );
			conditionMap.put( "masterOrganizationCode", masterOrganizationCode );

			String downloadFileName = ctx.msghandler.getMessage("DPR_UPLOAD_PCD_DOWNLOAD_FILE") +"_"+ masterOrganizationCode +"_"+ masterLanguageCode.toLowerCase();
			DataWriter out = createDataWriter( ctx, downloadFileName );

			try {
				ServletUtility.setSort( ctx.req, master_organization, "masterCode" );
				master_organization.write( out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE );
			} finally {
				out.flush();
				out.close();
			}
		}
		return true;
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		String uploadType = Record.extractString( conditionMap, "uploadType" );
		if( UPLOADTYPE_PRODUCT_CATEGORY_DESCRIPTION.equals( uploadType ) )
			return downloadRBM( ctx );

		UploadDetail db = null;
		String uploadCode = Record.extractString( conditionMap, "uploadCode" );
		if( uploadCode == null || uploadCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( uploadType == null || uploadType.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String filenameKey = "DPR_UPLOAD_FILENAME_" + uploadType.toUpperCase();
		if( UPLOADTYPE_CUSTOMER_ID_MAPPING.equals(uploadType) )
			db = new UploadDetail( ctx.handler, UploadDetail.IDX_CUSTOMER_ID_MAPPING );
		else if( UPLOADTYPE_CUSTOMER_MASTER.equals(uploadType) )
			db = new UploadDetail( ctx.handler, UploadDetail.IDX_CUSTOMER_MASTER );
		else if( UPLOADTYPE_INVENTORY.equals(uploadType) )
			db = new UploadDetail( ctx.handler, UploadDetail.IDX_INVENTORY );
		else if( UPLOADTYPE_ORDER_DETAIL.equals(uploadType) )
			db = new UploadDetail( ctx.handler, UploadDetail.IDX_ORDERDETAIL );
		else if( UPLOADTYPE_SELLOUT.equals(uploadType) )
			db = new UploadDetail( ctx.handler, UploadDetail.IDX_SELLOUT );
		else if( UPLOADTYPE_SKU_MAPPING.equals(uploadType) )
			db = new UploadDetail( ctx.handler, UploadDetail.IDX_SKU_MAPPING );
		else if( UPLOADTYPE_SELLING_SKU_LIST.equals(uploadType) ) {
			db = new UploadDetail( ctx.handler, UploadDetail.IDX_SELLING_SKU_LIST );
			if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode( ctx ), "useSSLNewItemInd") ) {
				uploadType += ".CN";
			}
		}
		else
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		com.irt.data.cols.ColumnList columnList = null;
		columnList = getColumnList( ctx, "DPRUpload%DOWN."+ uploadType.toUpperCase()
				, new String[] { com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML } );

		DataWriter out = createTextDataWriter( ctx, ctx.msghandler.getMessage(filenameKey) );

		try {
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			db.write( out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE );
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

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	/**
	*	1 : UPLOADTYPE_CUSTOMER_ID_MAPPING
	*	2 : UPLOADTYPE_CUSTOMER_MASTER
	*	3 : UPLOADTYPE_INVENTORY
	*	4 : UPLOADTYPE_SELLOUT
	*	5 : UPLOADTYPE_SKU_MAPPING
	*	6 : UPLOADTYPE_SELLING_SKU_LIST
	**/
	private String getUploadCodeWithCheckAuth( Context ctx ) {
		String pakcageCode = "DPRUpload";
		String temp = "";
		String[] types = new String[] {
			UPLOADTYPE_CUSTOMER_ID_MAPPING, UPLOADTYPE_CUSTOMER_MASTER, UPLOADTYPE_INVENTORY
			, UPLOADTYPE_ORDER_DETAIL, UPLOADTYPE_SELLOUT, UPLOADTYPE_SKU_MAPPING, UPLOADTYPE_SELLING_SKU_LIST
		};

		boolean isParentAuth = false;
		if( ctx.sessionMng.isAuthorized("DPR", pakcageCode) )
			isParentAuth = true;

		for (String type : types)
			if( isParentAuth || ctx.sessionMng.isAuthorized("DPR", pakcageCode +"."+ type) )
				temp += "," + type;

		String typeValue = "";
		if( temp.length() > 0 )
			typeValue = temp.substring(1);

		return typeValue;
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );
		if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRUpload.DOWN" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRUpload.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_UPLOAD.equals(ctx.mode) ) {
			String uploadType = ctx.req.getParameter( "uploadType" );
			if( uploadType == null || uploadType.length() == 0 )
				throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

			pageConfig.setSystemPackageCode( "DPR", "DPRUpload."+ uploadType );
		} else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		String type = ctx.req.getParameter( "type" );
		int idx = 0;
		if( type != null ) {
			if( "HEADER".endsWith(type) ) {
				idx = 0;
			} else if( "CIM".equals(type) ) {
				idx = 1;
			} else if( "CMT".equals(type) ) {
				idx = 2;
			} else if( "INV".equals(type) ) {
				idx = 3;
			} else if( "SSL".equals(type) ) {
				idx = 4;
			} else if( "SEO".equals(type) ) {
				idx = 5;
			} else if( "SKM".equals(type) ) {
				idx = 6;
			} else if( "ORD".equals(type) ) {
				idx = 7;
			}
		}
		ctx.db = new Upload( ctx.handler, idx );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_UPLOAD_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		Upload db = (Upload)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		boolean hasError = "Y".equals( ctx.req.getParameter("hasError") );

		if( hasError && "JNJAP_KR".equals(ctx.sessionMng.getPartyId()) ) {
			conditionMap.put( "status", "ER" );
		}
		String uploadCode = (String)conditionMap.get( "uploadCode" );
//		if( uploadCode == null ) {
//			uploadCode = (String) ctx.req.getAttribute( "uploadCode" );
//		}
		List<Map<String, Object>> recordList;
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		com.irt.data.cols.ColumnList columnList;

		/* Set user list */
		if( uploadCode != null ) {
			Map<String, Object> primaryMap = Upload.createPrimary( uploadCode );
			Map<String, Object> recordMap = new Upload( ctx.handler ).getRecord( primaryMap );
			conditionMap.put( "uploadCode", uploadCode );
			if( recordMap == null )
				throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

			ctx.req.setAttribute( "record_log", recordMap );
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_RBM_UPLOADLOG_DETAIL") );

			columnList = getColumnList( ctx, "DPRUpload.DETAIL%LIST" );
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			recordList = db.getDetails( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		} else {
			Map<String, Object> uploadUserMap = new java.util.HashMap<String, Object> ();
			uploadUserMap.put( "organizationCode", getSavedOrganizationCode( ctx ));
			uploadUserMap.put( com.irt.data.Condition.DISTINCT_CONDITIONKEY, "Y" );
			ctx.req.setAttribute( "uploadUser", db.getRecords(uploadUserMap, new String[] { "userId", "userName", "uploadUserId" } ) );
			ctx.pageConfig.setProperty( "uploadTypeValues", getUploadCodeWithCheckAuth(ctx) );

			String option = null;
			if( ctx.sessionMng.isAuthorized("DPR", "DPRUpload.OPT") ) {
				option = "OPT";
			}
			columnList = getColumnList( ctx, "DPRUpload%LIST", option );
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		}
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRUpload.LST") );
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );

		setPath( ctx, "jsp.MENU_UPLOADLOG", "TITLE_DPR_UPLOAD_LIST" );
		return forward( ctx, systemConfig.getJspPath() +"/dpr_upload_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		String uploadTypeKey;
		try {
			conditionMap = popConditionMap( ctx );
			uploadTypeKey = "type";
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRUpload.DTL") ) return false;
			conditionMap = createConditionMap( ctx );
			uploadTypeKey = "uploadType";
		}
		String uploadCode = (String)conditionMap.get( "uploadCode" );
		if( uploadCode == null )
			ctx.pageConfig.getListIndexVariables()[2] = ((Upload)ctx.db).getRecordCount( conditionMap );
		else {
			String type = (String)conditionMap.get( uploadTypeKey );
			int idx = 0;
			if( type != null ) {
				if( "HEADER".endsWith(type) ) {
					idx = 0;
				} else if( "CIM".equals(type) ) {
					idx = 1;
				} else if( "CMT".equals(type) ) {
					idx = 2;
				} else if( "INV".equals(type) ) {
					idx = 3;
				} else if( "SSL".equals(type) ) {
					idx = 4;
				} else if( "SEO".equals(type) ) {
					idx = 5;
				} else if( "SKM".equals(type) ) {
					idx = 6;
				} else if( "ORD".equals(type) ) {
					idx = 7;
				}
			}
			ctx.pageConfig.getListIndexVariables()[2] = new Upload( ctx.handler, idx ).getDetailCount( conditionMap );
		}

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	private boolean uploadRBM( Context ctx ) throws IOException, ServletException, SQLException {
		String uploadType = ctx.req.getParameter( "uploadType" );
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		final String masterLangCode = ctx.req.getParameter( "masterLangCode" );

		if( uploadType == null || uploadType.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode( ctx );

		UploadLog logDB = new UploadLog( ctx.handler );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRUpload%DOWN."+ uploadType.toUpperCase() );

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
			defaultMap.put( "countryCode", getUserCountryCode(ctx) );
			defaultMap.put( "languageCode", masterLangCode );//웹에서 선택된 lang만 적용됨.
			defaultMap.put( "status", "00" );
			defaultMap.put( "masterType", com.irt.dpr.MasterLink.MASTERTYPE_PRODUCT_CATEGORY );


			com.irt.dpr.MasterDesc db = new com.irt.dpr.MasterDesc( ctx.handler );
			String[] updateFieldKeys = new String[] { "orgMasterName", "orgMasterDescription" };
			loader = db.createDataLoader( columnList.getFieldKeyArray(), defaultMap, updateFieldKeys, Record.UPDATE | Record.INSERT );

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "DPR" );
			resultMap.put( "uploadType", "MSTDO_PC");
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			RecordFormat messageFormat = PatternRecordFormat
				.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );
			RecordFormat lineNameFormat = PatternRecordFormat.getInstance( "[${masterCode}] - [${masterOrganizationCode}] - [${languageCode}]" );

			final String masterOrganizationCode = organizationCode;
			final String validateMessageFormat = " vs "+ "[%s]" +" - "+ "[%s] : " + ctx.msghandler.getMessage( "ERR_INVALID_VALUE" );
			validator = new DataLoader.Validator() {
				@Override
				public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
					if( masterLangCode.equals(recordMap.get("languageCode")) && masterOrganizationCode.equals(recordMap.get("masterOrganizationCode")) )
						return;
					throw handler.createDataException( "ERR_INVALID_VALUE", String.format(validateMessageFormat, masterOrganizationCode, masterLangCode) );
				}
				@Override
				public void close() {}
			};

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

			String uploadInputPath = "DPRUpload?"+ PARAM_MODE +"="+ MODE_UPLOADINPUT+ "&uploadType="+ uploadType.toUpperCase();
			String redirectURL = RBMUploadLog.getUploadLogURL( ctx, dataLoader, systemConfig.getClassURL(), uploadInputPath );

			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		} finally {
			try { if( loader != null ) loader.close(); } catch( Exception ignored ) {}
		}
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		String uploadType = ctx.req.getParameter( "uploadType" );
		if( UPLOADTYPE_PRODUCT_CATEGORY_DESCRIPTION.equals( uploadType ) )
			return uploadRBM( ctx );

		Upload db = (Upload)ctx.db;
		DataReader reader = createDataReader( ctx );
		//reader.setTrim( true );
		MultipartHttpRequest req = (MultipartHttpRequest)ctx.req;
		Map<String, Object> recordMap = new java.util.HashMap<String, Object>();

		long millisecond = System.currentTimeMillis();
		int errorCount = 0;

		String uploadCode = db.getUploadCode();
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		String uploadHeader = ctx.req.getParameter( "headerInd" );
		String sslType = ctx.req.getParameter( "sslType" );
		String uploadOption = ctx.req.getParameter( "uploadOption" );
		if( uploadCode == null || uploadCode.length() == 0 )
			throw new ServletModelException( ServletModelException.INTERNAL_ERROR );
		else if( uploadType == null || uploadType.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode( ctx );

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRUpload.OPT") ) {
			uploadOption = Upload.UPLOAD_OPTION_REPLACE;
		} else if ( uploadOption == null || uploadOption.length() == 0 ) {
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

// 락을 걸 수 있는 방법을 강구하자
// 락용 테이블을 만들어서 사용하자(countryCode, organizationCode 별로)
		try {
			recordMap.put( "uploadCode", uploadCode );
			recordMap.put( "uploadType", uploadType );
			recordMap.put( "uploadOption", uploadOption );
			recordMap.put( "countryCode", getUserCountryCode(ctx) );
			recordMap.put( "organizationCode", organizationCode );
			recordMap.put( "fileName", req.getInputFileName("file") );
			recordMap.put( "uploadUserId", ctx.sessionMng.getUniqId() );
			recordMap.put( "sslType", sslType );
			recordMap.put( "status", Upload.STATUS_READY );
			if( !db.regist(recordMap) )
				throw new DataException( DataException.ERR_CANNOT_INSERT, ctx.msghandler.getMessage(DataException.ERR_CANNOT_INSERT) );
			ctx.handler.commit();

			DataResult result = db.read( reader, uploadType, uploadCode, sslType, ctx.sessionMng.getUniqId(), uploadHeader );
			if( result.getErrorCount() > 0 ) {
				ctx.handler.rollback();
				recordMap.put( "status", Upload.STATUS_ERROR );

				if( !db.modify(recordMap) )
					throw new DataException( DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE) );

				ctx.req.setAttribute( "ex_errors", result.getErrors() );

				return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
			} else {
				ctx.handler.commit();

				recordMap.put( "rowCount", String.valueOf(result.getRowCount()) );
				recordMap.put( "distributionChannelCodes",
						new com.irt.dpr.CountryDistChannel(ctx.handler).getDistributionChannels(getUserCountryCode(ctx), organizationCode) );

				String message = db.execute( result, recordMap );
				if( message == null || message.length() == 0 ) {
					if( Upload.UPLOADTYPE_SELLING_SKU_LIST.equals(uploadType) ) {
						recordMap.put( "createCount", String.valueOf(result.getRegistCount()) );
						recordMap.put( "insertCount", String.valueOf(result.getRowCount() - result.getErrorCount()) );
						recordMap.put( "ignoreCount", String.valueOf(result.getIgnoreCount()) );
					} else
						recordMap.put( "insertCount", String.valueOf(result.getRegistCount()) );
					recordMap.put( "updateCount", String.valueOf(result.getModifyCount()) );
					recordMap.put( "deleteCount", String.valueOf(result.getDeleteCount()) );
					recordMap.put( "errorCount", String.valueOf(result.getErrorCount()) );
					recordMap.put( "status", Upload.STATUS_COMPLETE );
					errorCount = result.getErrorCount();
				} else {
					recordMap.put( "message", message );
					recordMap.put( "status", Upload.STATUS_ERROR );
				}

				recordMap.put( "executeTime", String.valueOf( (System.currentTimeMillis() - millisecond) / 1000)  );

				String[] params;
				if( UPLOADTYPE_SELLING_SKU_LIST.equals(uploadType) && ctx.sessionMng.isAuthorized("DPR", "DPRUpload.OPT") ) {
					params = new String[] {
						  String.valueOf(result.getRowCount()), String.valueOf(result.getRegistCount())
						, String.valueOf(result.getModifyCount()), String.valueOf(result.getDeleteCount())
						, String.valueOf(result.getWarningCount()), String.valueOf(result.getErrorCount())
						, String.valueOf(result.getRowCount() - result.getErrorCount())
						, String.valueOf(result.getIgnoreCount())
					};
				} else {
					params = new String[] {
						  String.valueOf(result.getRowCount()), String.valueOf(result.getRegistCount())
						, String.valueOf(result.getModifyCount()), String.valueOf(result.getDeleteCount())
						, String.valueOf(result.getWarningCount()), String.valueOf(result.getErrorCount()), String.valueOf(result.getRowCount() - result.getErrorCount())
					};
				}

				/* Upload Header Update */
				if( !db.modify(recordMap) )
					throw new DataException( DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE) );

				if( message == null || message.length() == 0 ) {
					if( UPLOADTYPE_SELLING_SKU_LIST.equals(uploadType) )
						ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_UPLOADSSL_SUCCESS", params) );
					else
						ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_UPLOAD_SUCCESS", params) );
				} else
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_UPLOAD_FAILED", message) );
			}
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
			return uploadInput( ctx );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
			return uploadInput( ctx );
		} finally {
			reader.close();
		}
/*
		if( "JNJAP_KR".equals(ctx.sessionMng.getPartyId()) ) {
			String redirectURL = systemConfig.getClassURL() + "/DPRUpload?";
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "mode", MODE_LIST );
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "uploadCode", uploadCode );
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "type", uploadType );
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "menu", "portal" );
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "locale", getDisplayLanguage(ctx) );
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "organizationCode", getSavedOrganizationCode(ctx) );
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "url", ctx.pageConfig.getBackURL() );
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx) );

			return sendRedirect( ctx, redirectURL );
		} else {
			return uploadInput( ctx );
		}
*/
		String redirectURL = systemConfig.getClassURL() + "/DPRUpload?";
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "mode", MODE_LIST );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "uploadCode", uploadCode );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "type", uploadType );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "menu", "portal" );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "locale", getDisplayLanguage(ctx) );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "organizationCode", getSavedOrganizationCode(ctx) );
		if( errorCount > 0 ) {
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "hasError", "Y" );
		}
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "url", ctx.pageConfig.getBackURL() );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx) );

		return sendRedirect( ctx, redirectURL );
	}

	@Override
	protected boolean uploadInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		String uploadType = ctx.req.getParameter( "uploadType" );
		if( uploadType == null || uploadType.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String organizationCode = Record.extractString( recordMap, "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode( ctx );
		recordMap.put( "organizationCode", organizationCode );
		recordMap.put( "encoding", "UTF8" );

		ctx.req.setAttribute( "fieldSet", ((Upload)ctx.db).getFieldSet(true) );
		ctx.req.setAttribute( "record", recordMap );
		setAttributePartyMaster( ctx, com.irt.data.Record.createMap("countryCode", getUserCountryCode(ctx)), PARTYMASTER_ORGANIZATION );

		String titleKey = "TITLE_DPR_UPLOAD_" + uploadType + "_";
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage(titleKey + ctx.mode.toUpperCase()) );
		setPath( ctx, "jsp.MENU_UPLOAD", titleKey );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_upload_input.jsp" );
	}
}
