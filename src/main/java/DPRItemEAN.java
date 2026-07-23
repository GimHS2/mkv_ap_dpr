/*
 *	File Name:	DPRItemEAN.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		pub_upload_input.jsp
 *		dpr_itemean_input.jsp
 *		dpr_itemean_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.3	신규 UI/UX 적용
 *	jbaek		2019/07/30		2.2.2	다운로드 권한 변경. itemName조건 변경.
 *	jbaek		2019/01/30		2.2.1	uploadInput encoding 조건 변경.
 *	jbaek		2017/06/30		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import com.irt.dpr.Item;
import com.irt.dpr.ItemEANMap;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.rbm.TableDaoException;
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
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRItemEAN"})
public class DPRItemEAN extends DPRServletModel {

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletModelException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		setDefaultParameter( ctx, conditionMap );
		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode( ctx ) );
		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage( ctx ) );

		String itemCode = getConditionValue( conditionMap.getParameter( "itemCode" ) );
		if( itemCode != null )
			Condition.putConditionValueOnly( conditionMap, "itemCode", itemCode, getConditionType( itemCode ) );
		String itemName = getConditionValue( conditionMap.getParameter( "conditionItemName" ) );
		if( itemCode != null && itemName == null ) {
			conditionMap.put( "itemName", new Item( ctx.handler ).getName( itemCode, getDisplayLanguage( ctx ) ) );
		}
		if( itemName != null ) {
			Condition.putConditionValueOnly( conditionMap, "itemName", getConditionValue( itemName ), Condition.CONDTYPE_CONTAINS );
		}

		return conditionMap;
	}

	@Override
	protected boolean download( Context ctx ) throws ServletException, IOException, SQLException {
		ItemEANMap db = (ItemEANMap)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRItemEAN%DOWN" );

		DataWriter out = createTextDataWriter( ctx, "ItemUPC" + "_" + getSavedOrganizationCode( ctx ) );

		try {
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			db.write( out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE );
		} catch( SQLException sqlEx ) {
			out.println();
			out.print( sqlEx.getMessage() );
			logger.error( "Internal Error", sqlEx );
		} finally {
			out.flush();
			out.close();
		}

		return true;
	}

	private String getConditionType( String value ) {
		if( value.indexOf( "%" ) >= 0 )
			return Condition.CONDTYPE_LIKE;
		else if( value.indexOf( "_" ) >= 0 )
			return Condition.CONDTYPE_LIKE;

		return Condition.CONDTYPE_EQUALS;
	}

	private String getConditionValue( String value ) {
		if( value == null )
			return null;
		List<String[]> list = new java.util.ArrayList<String[]>();
		if( value.indexOf( "*" ) >= 0 )
			list.add( new String[] { "*", "%" } );
		if( value.indexOf( "?" ) >= 0 )
			list.add( new String[] { "?", "_" } );

		Object[] regex = new Object[list.size()];
		list.toArray( regex );

		if( regex.length > 0 ) {
			int position = -1;
			for( int i = 0; i < regex.length; i++ ) {
				String[] str = (String[])regex[i];

				while( ( position = value.indexOf( str[0] ) ) >= 0 ) {
					if( position == 0 )
						value = str[1] + value.substring( 1 );
					else if( position > 0 )
						value = value.substring( 0, position ) + str[1] + value.substring( position + 1 );
				}
			}
		}

		return value;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	public boolean info( Context ctx, boolean inserting ) throws SQLException, IOException, ServletException {
		ItemEANMap db = (ItemEANMap)ctx.db;

		Map<String, Object> paramMap = new ParameterMap( ctx.req );

		// ready
		Map<String, Object> primaryMap;
		try {
			primaryMap = db.extractPrimary( paramMap, 0 );
		} catch( TableDaoException taoEx ) {
			Map sourceMap = taoEx.getRecordMap();
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER, taoEx.getMessage() );
		}

		// process
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap );

		// forward
		if( inserting ) {
			ctx.req.setAttribute( "fieldSet", db.getFieldSet( false ) );
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized( "DPR", "DPRItemEAN.MNG" ) );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet( true ) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_itemean_input.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals( ctx.mode ) )
			pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_INFO.equals( ctx.mode ) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemEAN.INF" );
		else if( MODE_LIST.equals( ctx.mode ) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemEAN.LST" );
		else if( MODE_LISTCOUNT.equals( ctx.mode ) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals( ctx.mode ) || MODE_MODIFYINPUT.equals( ctx.mode ) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemEAN.MNG" );
		else if( MODE_REGIST.equals( ctx.mode ) || MODE_REGISTINPUT.equals( ctx.mode ) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemEAN.MNG" );
		else if( MODE_REMOVE.equals( ctx.mode ) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemEAN.MNG" );
		else if( MODE_DOWNLOAD.equals( ctx.mode ) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemEAN.LST" );
		else if( MODE_UPLOAD.equals( ctx.mode ) || MODE_UPLOADINPUT.equals( ctx.mode ) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItemEAN.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new ItemEANMap( ctx.handler );

		String messageKey = "TITLE_DPR_ITEMEAN_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_MATERIALUPC" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		ItemEANMap db = (ItemEANMap)ctx.db;

		// ready
		Map<String, Object> conditionMap = createConditionMap( ctx );
		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION );

		// process
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRItemEAN%LIST" );
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )
			ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "records", recordList );

		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized( "DPR", "DPRItemEAN.MNG" ) );

		// forward
		return forward( ctx, systemConfig.getJspPath() + "/dpr_itemean_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized( "DPR", "DPRItemEAN.LST" ) )
				return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ( (ItemEANMap)ctx.db ).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	public boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {

		// process
		if( ctx.req.getAttribute( "record" ) == null ) {
			ctx.req.setAttribute( "fieldSet", ( (ItemEANMap)ctx.db ).getFieldSet( true ) );

			setAttributePartyMaster( ctx, createConditionMap( ctx ), PARTYMASTER_ORGANIZATION );
		}

		// forward
		return forward( ctx, systemConfig.getJspPath() + "/dpr_itemean_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws SQLException, IOException, ServletException {
		ItemEANMap db = (ItemEANMap)ctx.db;

		// if( ctx.pageConfig.getBackURL() == null )
		// throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		Map<String, Object> paramMap = new ParameterMap( ctx.req );
		Map<String, Object[]> primaryKeyValues = null;
		try {
			primaryKeyValues = db.extractPrimaryKeyValues( paramMap );
		} catch( TableDaoException taoEx ) {
			Map sourceMap = taoEx.getRecordMap();
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER, taoEx.getMessage() );
		}

		int count = 0;

		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

		for( int i = 0; i < primaryKeyValues.size(); i++ ) {

			try {
				primaryMap = db.extractPrimary( paramMap, i );
			} catch( TableDaoException taoEx ) {
				Map sourceMap = taoEx.getRecordMap();
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER, taoEx.getMessage() );
			}
			try {
				if( db.delete( primaryMap ) ) {
					count++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap( db.getFieldValue( primaryMap, "title" ), dataEx ) );
			}
		}

		// set error or success
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage( "MSG_REMOVE_SUCCESS", String.valueOf( count ) ) );

			if( ctx.pageConfig.getBackURL() == null ) {
				String redirectURL = systemConfig.getClassURL() + "/DPRItemEAN"
						+ "?menu=portal"
						+ "&locale=" + getDisplayLanguage( ctx )
						+ "&organizationCode=" + getSavedOrganizationCode( ctx );
				ctx.pageConfig.setBackURL( redirectURL );
			}
		}

		// returns
		if( ctx.req.getAttribute( "errors" ) != null ) {
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery( ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage( ctx ) ) );
		}
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		ItemEANMap db = (ItemEANMap)ctx.db;

		Map<String, Object> recordMap = createConditionMap( ctx );
		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );

		Map<String, Object> recordMap_newpk = new java.util.HashMap<String, Object>();
		for( String key_new : recordMap.keySet() ) {
			if( key_new.endsWith( "_newpk" ) ) {
				Object obj_new = recordMap.get( key_new );
				String key_org = key_new.substring( 0, key_new.length() - "_newpk".length() );
				Object obj_org = recordMap.get( key_org );
				if( obj_org != null && !obj_org.equals( obj_new ) ) {
					recordMap_newpk.put( key_org, obj_new );
				}
			}
		}

		boolean actionSuccess = false;
		try {
			if( inserting ) {
				db.regist( recordMap );
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage( "MSG_REGIST_SUCCESS" ) );
			} else {
				if( !recordMap_newpk.isEmpty() ) {

					if( !db.delete( recordMap ) ) {
						throw ctx.handler.createDataException( DataException.ERR_CANNOT_DELETE );
					}
					Map<String, Object> newpk = db.extractPrimary( recordMap, 0 );
					newpk.putAll( recordMap_newpk );// replace keys

					recordMap_newpk.putAll( recordMap );
					recordMap_newpk.putAll( newpk );
					if( !db.regist( recordMap_newpk ) ) {
						throw ctx.handler.createDataException( DataException.ERR_CANNOT_INSERT );
					}
					recordMap = recordMap_newpk;// replace
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage( "MSG_MODIFY_SUCCESS" ) );
				} else {
					if( !db.modify( recordMap ) )
						throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage( "MSG_MODIFY_SUCCESS" ) );
				}
			}

			actionSuccess = true;
		} catch( DataException dataEx ) {
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			logger.info( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			logger.error( "internal error.", sqlEx );
		}

		if( actionSuccess ) {
			ctx.req.setAttribute( "record", db.getRecord( recordMap ) );
			ctx.pageConfig.setManageAuth( true );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			return forward( ctx, systemConfig.getJspPath() + "/dpr_itemean_input.jsp" );
		} else {
			ctx.req.setAttribute( "record", recordMap );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet( inserting ) );

			String mode = ( inserting ? MODE_REGISTINPUT : MODE_MODIFYINPUT );
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage( "TITLE_DPR_ITEMEAN_" + mode.toUpperCase() ) );
			return registInput( ctx );
		}
	}

	@Override
	protected boolean upload( Context ctx ) throws ServletException, SQLException, IOException {
		UploadLog logDB = new UploadLog( ctx.handler );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRItemEAN%DOWN" );

		// columnList.getcolumn
		String[] fieldKeys = columnList.getFieldKeyArray();

		DataLoader.Loader loader = null;
		DataLoader.Logger loaderLogger = null;
		DataLoader.Validator validator = null;
		try {
			Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();
			defaultMap.put( "organizationCode", getSavedOrganizationCode( ctx ) );
			defaultMap.put( "updateUserId", ctx.sessionMng.getUniqId() );

			ItemEANMap db = (ItemEANMap)ctx.db;

			String[] updateFieldKeys = { "mapEanCode", "updateUserId" };
			loader = db.createDataLoader( fieldKeys, defaultMap, updateFieldKeys, Record.INSERT_OR_UPDATE_OR_DELETE );

			Map<String, Object> resultMap = new java.util.HashMap<String, Object>();
			resultMap.put( "systemCode", "DPR" );
			resultMap.put( "uploadType", "ItemUPC" );
			resultMap.put( "userId", ctx.sessionMng.getUniqId() );

			RecordFormat messageFormat = PatternRecordFormat.getInstance( "%{MSG_UPLOADLOG_RESULT,${lineCount},${successCount},${errorCount}}" );
			StringBuilder sb = new StringBuilder();
			String delimStr = " - ";
			for( String key : fieldKeys ) {
				sb.append( "[" );
				sb.append( "${" + key + "}" );
				sb.append( "]" );
				sb.append( delimStr );
			}
			sb.delete( sb.length() - delimStr.length(), sb.length() );
			RecordFormat lineNameFormat = PatternRecordFormat.getInstance( sb.toString() );

			loaderLogger = logDB.createLogger( resultMap, messageFormat, lineNameFormat );
			DataLoader dataLoader = createDataLoader( ctx, ctx.handler, loader, loaderLogger, validator, true );

			try {
				DataReader reader = dataLoader.getDataReader();
				java.io.PrintStream out = dataLoader.getErrorPrintStream();
				for( int i = 0; i < com.irt.data.cols.ColumnUtility.getTitleRowCount( columnList ) && !reader.isEOF(); i++ ) {
					try {
						reader.readNext();
					} catch( DataException dataEx ) {
					}
					if( reader.getLineString() != null && out != null )
						out.println( reader.getLineString() );
				}
				reader.setTrim( true );
				dataLoader.execute();
				ctx.pageConfig.setMessage( (String)loaderLogger.getResultMap().get( "message" ) );
			} finally {
				dataLoader.close( false );
			}
			loader = null;

			String uploadInputPath = "DPRItemEAN" + "?" + PARAM_MODE + "=" + MODE_UPLOADINPUT;
			String redirectURL = RBMUploadLog.getUploadLogURL( ctx, dataLoader, systemConfig.getClassURL(), uploadInputPath );

			return sendRedirect( ctx, HtmlUtility.replaceURLQuery( redirectURL, PARAM_MESSAGE_KEY, saveMessage( ctx ) ) );
		} finally {
			try {
				if( loader != null )
					loader.close();
			} catch( Exception ignored ) {
			}
		}
	}

	@Override
	protected boolean uploadInput( Context ctx ) throws SQLException, IOException, ServletException {
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		recordMap = createConditionMap( ctx );

		String organizationCode = Record.extractString( recordMap, "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode( ctx );

//		if( recordMap.get("encoding") == null ) {
//			recordMap.put( "encoding", "UTF8" );
//		}

		ctx.req.setAttribute( "record", recordMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_upload_input.jsp" );
	}

}
