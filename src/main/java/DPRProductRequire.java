/*
 *	File Name:	DPRProductRequire.java
 *	Version:	2.2.4
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_item_list.jsp
 *		dpr_item_main.jsp
 *		dpr_item_tree.jsp
 *		dpr_item_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.4	신규 UI/UX 적용
 *	hankalam	2020/12/31		2.2.3	오류 수정
 *	hankalam	2020/03/31		2.2.2	createDataWriter(): cookieOption 추가
 *	jbaek		2018/10/30		2.2.1	isChinaCountry() 삭제
 *	hankalam	2017/08/31		2.2.0	create.
 *
**/

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.dpr.Item;
import com.irt.dpr.OrderItem;
import com.irt.dpr.ProductRequire;
import com.irt.dpr.util.CondPred;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRProductRequire"})
public class DPRProductRequire extends DPRServletModel {
	public final static String MODE_TREE				= "tree";
	public final static String MODE_TREE_DEFAULT		= "treedef";
	public final static String PARAM_TYPE				= "type";
	public final static String PARAM_VIEWTYPE			= "vtype";
	public final static String TYPE_SIMULATION			= "sim";
	public final static String VIEW_TYPE_INPUT			= "input";
	public final static String VIEW_TYPE_LIST			= "list";

	private final static String HIERARCHY_PARAMETERKEY	= "productHierarchyCode_";

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap  = new ParameterMap( ctx.req, true );

		String distributionChannelCode = ctx.req.getParameter( "distributionChannelCode" );
		// organizationCode, divisionCode, distributionChannelCode
		setDefaultParameter( ctx, conditionMap );
		if( distributionChannelCode == null ) {
			conditionMap.remove( "distributionChannelCode" );
		}
		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode(ctx) );

		CondPred.putValueIfNoKey(conditionMap, "endOrderDate", com.irt.data.Date.getInstance(ctx.handler.getTimeZone()));
		String startOrderDate = Record.extractString( conditionMap, "startOrderDate" );
		String endOrderDate = Record.extractString( conditionMap, "endOrderDate" );
		try {
			if( startOrderDate != null && startOrderDate.length() > 0 ) {
				conditionMap.put( "startOrderDate", startOrderDate );
				conditionMap.put( "orderDate"+ Condition.SUFFIX_MIN_VALUE, com.irt.data.Date.getInstance(startOrderDate) );
				conditionMap.put( "orderDate"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MINMAX );
			}
			if( endOrderDate != null && endOrderDate.length() > 0 ) {
				conditionMap.put( "endOrderDate", endOrderDate );
				conditionMap.put( "orderDate"+ Condition.SUFFIX_MAX_VALUE, com.irt.data.Date.getInstance(endOrderDate) );
				conditionMap.put( "orderDate"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MINMAX );
			}
		} catch( java.text.ParseException parseEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );
		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			Condition.putConditionValueOnly( conditionMap, "authUniqId", ctx.sessionMng.getUniqId() );
			Condition.putConditionValueOnly( conditionMap, "authPartyValue", "Y" );
		}
		return conditionMap;
	}

	protected Map<String, Object> itemTreeCreateConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap parameterMap = new ParameterMap( ctx.req, true );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();

		// organizationCode, divisionCode, distributionChannelCode
		setDefaultParameter( ctx, conditionMap );
		conditionMap.remove( "distributionChannelCode" );
		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode(ctx) );

		if( parameterMap.containsKey("plantInd") )
			Condition.putConditionValueOnly( conditionMap, "plantInd", "Y" );

		if( parameterMap.containsKey("promotionItemInd") )
			Condition.putConditionValueOnly( conditionMap, "promotionItemInd", parameterMap.get("promotionItemInd") );
		if( parameterMap.containsKey("itemConsumerEANCode") )
			Condition.putConditionValueOnly( conditionMap, "itemConsumerEANCode", parameterMap.get("itemConsumerEANCode") );

		String partyCode = getRequestPartyCode( ctx );

		if( partyCode != null )
			Condition.putConditionValueOnly( conditionMap, "partyCode", parameterMap.get( "partyCode" ) );

		if( parameterMap.containsKey( "shipPartyCode" ) )
			Condition.putConditionValueOnly( conditionMap, "shipPartyCode", parameterMap.get( "shipPartyCode" ) );

		String itemCode = getConditionValue( (String)parameterMap.get("itemCode") );
		if( itemCode != null )
			Condition.putConditionValueOnly( conditionMap, "itemCode", itemCode, getConditionType(itemCode) );

		String itemName = getConditionValue( (String)parameterMap.get("itemName") );
		if( itemCode != null && itemName == null ) {
			conditionMap.put( "conditionItemName", new Item(ctx.handler).getName(itemCode, getDisplayLanguage(ctx)) );
		}
		if( itemName != null ) {
			Condition.putConditionValueOnly( conditionMap, "itemName", getConditionValue(itemName), Condition.CONDTYPE_CONTAINS );
		}

		String conditionPartyName = (String)parameterMap.get("conditionPartyName");
		if( partyCode != null && conditionPartyName == null ) {
			conditionPartyName = new com.irt.dpr.Party(ctx.handler).getPartyName( conditionMap );
			conditionMap.put( "conditionPartyName", conditionPartyName );
		}

		if( parameterMap.containsKey("orderKey") )
			Condition.putConditionValueOnly( conditionMap, "orderKey", parameterMap.get("orderKey") );

		String productHierarchyCode = null;
		for( int i = 0; i < 6; i++ ) {
			String code = (String)parameterMap.get( HIERARCHY_PARAMETERKEY + (i+1) );
			if( code != null )
				Condition.putConditionValueOnly( conditionMap, HIERARCHY_PARAMETERKEY + (i+1), productHierarchyCode = code );
			else
				break;
		}
		if( productHierarchyCode != null )
			Condition.putConditionValueOnly( conditionMap, "productCategoryCode", productHierarchyCode, Condition.CONDTYPE_STARTSWITH );

		Condition.putConditionValueOnly( conditionMap, "displayLanguage", getDisplayLanguage(ctx) );
		Condition.putConditionValueOnly( conditionMap, "status"
				, (conditionMap.containsKey("status") ? conditionMap.get("status") : Item.ITEMSTATUS_NORMAL) );

		com.irt.data.Date availableDate =  com.irt.data.Date.getInstance();
		Condition.putConditionValueOnly( conditionMap, "availableDate", availableDate );

		return conditionMap;
	}

	private String getConditionType( String value ) {
		if( value.indexOf( "%" ) >= 0 )
			return Condition.CONDTYPE_LIKE;
		else if( value.indexOf( "_" ) >= 0 )
			return Condition.CONDTYPE_LIKE;

		return Condition.CONDTYPE_EQUALS;
	}

	/**
	 * Wildcard Searching
	 *  '*' : some characters matches. replace to "%" oracle wildcard.
	 *  '?' : one charcter matches. replace to "_" oracle wildcard.
	**/
	private String getConditionValue( String value ) {
		if( value == null ) return null;
		List<String[]> list = new java.util.ArrayList();
		if( value.indexOf("*") >= 0 ) list.add( new String[] { "*", "%" } );
		if( value.indexOf("?") >= 0 ) list.add( new String[] { "?", "_" } );

		Object[] regex = new Object[list.size()];
		list.toArray( regex );

		if( regex.length > 0 ) {
			int cnt = 0;
			int position = -1;
			for( int i = 0; i < regex.length; i++ ) {
				String[] str = (String [])regex[i];

				while( (position = value.indexOf(str[0])) >= 0 ) {
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
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_MODIFY.equals(ctx.mode) )
			return update( ctx, false );
		else if( MODE_TREE.equals(ctx.mode) )
			return tree( ctx );
		else if( MODE_TREE_DEFAULT.equals(ctx.mode) )
			return treeDefault( ctx );

		return super.doRequest( ctx, isPost );
	}

	/** excel and text file */
	@Override
	protected com.irt.data.DataWriter createDataWriter( Context ctx, String filename ) throws IOException, ServletException {
		String fileType = getFileType(ctx);

		String encoding = ctx.req.getParameter( "encoding" );
		if( encoding == null ) encoding = systemConfig.getEncoding( ctx.locale );

		String name = new String(filename.getBytes("utf-8"), "8859_1");
		String cookieOption = systemConfig.getCookieOption();
		cookieOption = cookieOption != null ? cookieOption : "";
		cookieOption = cookieOption.replaceAll( "HttpOnly;", "" );
		ctx.res.setContentType( com.irt.util.RBMWorkbook.getResponseContentType(fileType) );
		ctx.res.setHeader( "Content-Disposition", "attachment; filename="+ name +"." + com.irt.util.RBMWorkbook.getFileExtension(fileType) );
		ctx.res.setHeader( "Set-Cookie", "fileDownload=true; path=/;" + cookieOption );

		if( logger.isTraceEnabled() ) logger.trace( "createDataWriter: " + fileType + encoding );

		return com.irt.util.RBMWorkbook.getDataWriter( ctx.res.getOutputStream(), fileType, encoding );
	}

	@Override
	protected boolean download( Context ctx) throws IOException, ServletException, SQLException {
		ProductRequire db = (ProductRequire) ctx.db;
		Map<String, Object> conditionMap = createConditionMap( ctx );
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		String type = ctx.req.getParameter( PARAM_TYPE );
		String columnName;

		if( TYPE_SIMULATION.equals(type) ) {
			columnName ="DPRProductRequire.SIM%DOWN";
		} else {
			columnName = "DPRProductRequire%DOWN";
		}

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, columnName, com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		String filename = ctx.msghandler.getMessage( "TITLE_DPR_PRODUCT_REQUIREMENT_" );
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

	private String getRequestPartyCode( Context ctx ) throws ServletModelException, SQLException {
		String partyCode = ctx.req.getParameter( "partyCode" );
		if( partyCode == null || partyCode.length() == 0 ) {
			partyCode = getUserDistributorCode( ctx );
		}

		return (partyCode == null || partyCode.length() == 0 ? null : partyCode);
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		ProductRequire db = (ProductRequire)ctx.db;
		String productReqKey = ctx.req.getParameter( "productReqKey" );
		String orderDate = ctx.req.getParameter( "orderDate" );
		String soldPartyCode = ctx.req.getParameter( "partyCode" );
		String shipPartyCode = ctx.req.getParameter( "shipPartyCode" );
		String itemCode = ctx.req.getParameter( "itemCode" );

		if( productReqKey == null || orderDate == null || soldPartyCode == null || shipPartyCode == null || itemCode == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> primaryMap = null;
		primaryMap = ProductRequire.createPrimary( productReqKey, orderDate, soldPartyCode, shipPartyCode, itemCode );
		primaryMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		String[] fieldKeys = { "productReqKey", "orderKey", "orderDate", "partyCode", "shipPartyCode", "partyName", "shipPartyName",
				"orderNumber", "deliveryPlant", "officeCode", "itemCode", "itemName", "itemConsumerEAN", "qty", "uom", "expectedDate", "description" };
		Map<String, Object> recordMap = db.getRecord( primaryMap, fieldKeys );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap );

		if( inputting ) {
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			return registInput( ctx );
		} else {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRProductRequire.INF") );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_productreq_input.jsp" );
		}
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRProductRequire.INF" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRProductRequire.DOWN" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRProductRequire.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRProductRequire.MNG" );
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRProductRequire.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRProductRequire.MNG" );
		else if( MODE_TREE_DEFAULT.equals(ctx.mode) || MODE_TREE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRProductRequire.MNG" );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new ProductRequire( ctx.handler );

		String messageKey = "TITLE_DPR_PRODUCT_REQUIREMENT_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_ORDER", "jsp.SUBMENU_PRODUCTREQUIRE" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		ProductRequire db = (ProductRequire)ctx.db;
		Map<String, Object> conditionMap = createConditionMap( ctx );
		String type = ctx.req.getParameter( PARAM_TYPE );
		com.irt.data.cols.ColumnList columnList;

		if( TYPE_SIMULATION.equals(type) ) {
			columnList = getColumnList( ctx, "DPRProductRequire.SIM%LIST" );
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PRODUCT_REQUIREMENT_SIM_"+ ctx.mode.toUpperCase()) );
			ctx.pageConfig.setProperty( PARAM_TYPE, type);
		} else {
			columnList = getColumnList( ctx, "DPRProductRequire%LIST" );
		}

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );

		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		Map<String, Object> _conditionMap = new java.util.HashMap<String, Object>();
		_conditionMap.put( "organizationCode", conditionMap.get("organizationCode") );
		_conditionMap.put( "distributionChannelCode", conditionMap.get("distributionChannelCode") );
		_conditionMap.put( "countryCode", getUserCountryCode(ctx) );
		setAttributePartner( ctx, _conditionMap );
		setAttributeCondition( ctx, _conditionMap, "PT" );

		conditionMap.putAll( _conditionMap );

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );

		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRProductRequire.MNG") );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_productreq_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRProductRequire.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((QueryableManager)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
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
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> paramMap = new ParameterMap( ctx.req );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>( paramMap );
		setDefaultParameter( ctx, conditionMap );
		conditionMap.remove( "distributionChannelCode" );

		String ctype = (String)conditionMap.get( "ctype" );
		setAttributeCondition( ctx, conditionMap, ctype );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_attribute_party.jsp" );
	}

	private void setAllowUOM( Context ctx ) throws ServletModelException, SQLException {
		String allowUOM = null;
		String organizationCode = getSavedOrganizationCode( ctx );
		String divisionCode = getDivisionCode(ctx);
		String distributorChannelCode = getDistributionChannelCode(ctx);
		String soldPartyCode = ctx.req.getParameter( "partyCode" );

		if( soldPartyCode == null || soldPartyCode.length() < 1 ) {
			soldPartyCode = getRequestPartyCode(ctx);
		}

		Map<String, Object> primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "partyCode", soldPartyCode );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributorChannelCode", distributorChannelCode );
		primaryMap.put( "divisionCode", divisionCode );

		allowUOM = new OrderItem( ctx.handler ).getDistAllowUOM( primaryMap );

		if ( allowUOM == null )
			allowUOM = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "uom;" + organizationCode, com.irt.dpr.Party.DEFAULT_UOM );

		List<Map<String, Object>> allowUOMList = new java.util.ArrayList<Map<String, Object>>();
		for( String uom : allowUOM.split(",") ) {
			Map<String, Object> uomMap = new java.util.HashMap<String, Object>();
			uomMap.put( "allowUOM", uom );
			allowUOMList.add( uomMap );
		}

		ctx.req.setAttribute( "allowUOMList", allowUOMList );

	}

	protected Map<String, Object> createItemTreeConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap = itemTreeCreateConditionMap( ctx );

		String countryCode = (String)conditionMap.get( "countryCode" );
		Map<String, Object> recordMap = new com.irt.dpr.Country(ctx.handler).getDefaultHierarchyCondition( countryCode );
		if( recordMap != null ) {
			if( recordMap.get("defaultHierarchyLevel") != null ) {
				conditionMap.put( "defaultHierarchyLevel", recordMap.get("defaultHierarchyLevel") );
				conditionMap.put( "classCode", recordMap.get("defaultHierarchyLevel") );
			}
			if( recordMap.get("hierarchyCondition") != null ) {
				conditionMap.put( "hierarchyCondition", ((String)recordMap.get("hierarchyCondition")).split(";") );
			}
		}

		com.irt.dpr.ItemCondition.setItemCondition( conditionMap, com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()) );

		return conditionMap;
	}

	protected boolean treeDefault( Context ctx ) throws IOException, ServletException {
		return forward( ctx, systemConfig.getJspPath() + "/dpr_orderitem_tree_default.jsp" );
	}

	protected boolean tree( Context ctx ) throws IOException, ServletException, SQLException {
		QueryableManager db = (QueryableManager)ctx.db;
		db = new OrderItem( ctx.handler );

		Map<String, Object> conditionMap = createItemTreeConditionMap( ctx );
		String type = ctx.req.getParameter( PARAM_TYPE );
		if( conditionMap.get("defaultHierarchyLevel") == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		setUserAuthParty( ctx, conditionMap );
		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL );
		ctx.pageConfig.setProperty( "type", type );

		String organizationCode = (String)conditionMap.get( "organizationCode" );

		if( organizationCode != null && organizationCode.length() > 0 ) {
			List<Map<String, Object>> recordList = null;
			String partyCode = (String)conditionMap.get( "partyCode" );
			String divisionCode = getDivisionCode( ctx );
			String distributionChannelCode = getDistributionChannelCode( ctx );
			Map<String, Object> primaryMap = com.irt.dpr.Party.createPrimary( partyCode, organizationCode, distributionChannelCode, divisionCode );

			String allowUOM = null;
			if( com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useDistAllowUOM") )
				allowUOM = ((OrderItem)db).getDistAllowUOM( primaryMap );
			if (allowUOM == null)
				allowUOM = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "uom;" + organizationCode,
						com.irt.dpr.Party.DEFAULT_UOM);

			ctx.pageConfig.setProperty( "allowUOM", allowUOM );
			if( partyCode != null && partyCode.length() > 0 )
				recordList = ((OrderItem)db).getItemTreeList( conditionMap );

			if( recordList == null || recordList.size() == 0 ) {
				if( partyCode != null && partyCode.length() > 0 )
					ctx.pageConfig.setProperty( "treeMsg", ctx.msghandler.getMessage("MSG_NO_RECORD_FOUND") );
			} else {
				ctx.req.setAttribute( "records", recordList );
			}
		} else
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_productreq_item_tree.jsp" );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = createConditionMap( ctx );
		setAllowUOM( ctx );
		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL );
		setAttributePartner( ctx, conditionMap, PARTNER_SOLD );

		if( ctx.req.getAttribute("record") == null ) {
			ctx.req.setAttribute( "fieldSet", ((ProductRequire)ctx.db).getFieldSet(true) );
			ctx.pageConfig.setMode( MODE_REGISTINPUT );
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PRODUCT_REQUIREMENT_"+ ctx.mode.toUpperCase()) );
		}
		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );
		ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRProductRequire.MNG") );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_productreq_input.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, SQLException, ServletException {
		ProductRequire db = (ProductRequire) ctx.db;

		String[] productReqKey = ctx.req.getParameterValues( "productReqKey" );
		String[] orderDate = ctx.req.getParameterValues( "orderDate" );
		String[] soldPartyCode = ctx.req.getParameterValues( "partyCode" );
		String[] shipPartyCode = ctx.req.getParameterValues( "shipPartyCode" );
		String[] itemCodes = ctx.req.getParameterValues( "itemCode" );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( productReqKey == null || productReqKey.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( orderDate == null || orderDate.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( soldPartyCode == null || soldPartyCode.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( shipPartyCode == null || shipPartyCode.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( itemCodes == null || itemCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		// 레코드 삭제
		int count = 0;
		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < productReqKey.length; i++) {
			primaryMap = ProductRequire.createPrimary( productReqKey[i], orderDate[i] ,soldPartyCode[i], shipPartyCode[i], itemCodes[i] );
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
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		ProductRequire db = (ProductRequire) ctx.db;
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		setDefaultParameter( ctx, recordMap );
		recordMap.put( "countryCode", getUserCountryCode(ctx) );
		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );

		try {
			if( inserting ) {
				String[] itemCodes = ctx.req.getParameterValues( "itemCode" );
				String[] qty = ctx.req.getParameterValues( "qty" );
				String[] expectedDate = ctx.req.getParameterValues( "expectedDate" );
				recordMap.put( "productReqKey", db.createProductReqKey() );
				recordMap.put( "orderDate", com.irt.data.Date.getInstance().toString() );
				List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
				for( int i = 0; i < itemCodes.length; i++) {
					recordMap.put( "itemCode", itemCodes[i] );
					recordMap.put( "qty", qty[i] );
					recordMap.put( "expectedDate", expectedDate[i] );
					try {
						if( db.regist(recordMap) ) {
							ctx.handler.commit();
						}
					} catch( DataException dataEx ) {
						ctx.handler.rollback();
						if( DataException.ERR_UNIQUE_CONSTRAINT.equals(dataEx.getErrorKey()) ) {
							errorList.add( createErrorMap(itemCodes[i]
									, ctx.msghandler.getMessage("ERR_ALREADY_REGISTERED_ORDERITEM")) );
						} else
							errorList.add( createErrorMap(itemCodes[i], dataEx) );
					}
				}
				if( errorList != null && errorList.size() > 0 ) {
					ctx.req.setAttribute( "errors", errorList );

					return forward( ctx, systemConfig.getJspPath() +"/error.jsp" );
				}

				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REGIST_SUCCESS") );
				ctx.req.setAttribute( "record", db.getRecord(recordMap) );
				ctx.pageConfig.setManageAuth( true );
				ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

				return forward( ctx, systemConfig.getJspPath() + "/dpr_productreq_input.jsp" );
			} else {
				if( !db.modify(recordMap) )
					throw ctx.handler.createDataException( DataException.ERR_NO_RECORD_UPDATE );

				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			}

			Object productReqKey = recordMap.get( "productReqKey" );
			String soldPartyCode = (String)recordMap.get( "partyCode" );
			String shipPartyCode = (String)recordMap.get( "shipPartyCode" );
			String itemCode = (String)recordMap.get( "itemCode" );
			String orderDate = (String)recordMap.get( "orderDate" );
			Map<String, Object> primaryMap = ProductRequire.createPrimary( productReqKey, orderDate, soldPartyCode, shipPartyCode, itemCode );
			primaryMap.put( "displayLanguage", getDisplayLanguage(ctx) );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			ctx.req.setAttribute( "record", db.getRecord(primaryMap) );
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

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
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PRODUCT_REQUIREMENT_"+ mode.toUpperCase()) );

		return registInput( ctx );
	}
}
