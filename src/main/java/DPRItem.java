/*
 *	File Name:	DPRItem.java
 *	Version:	2.2.9
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_common_name.jsp
 *		pub_list_count.jsp
 *		dpr_item_list.jsp
 *		dpr_item_main.jsp
 *		dpr_item_tree.jsp
 *		dpr_item_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.9	신규 UI/UX 적용
 *	hankalam	2020/12/31		2.2.8	remove() 추가
 *	hankalam	2020/06/30		2.2.7	createConditionMap(): useDangerousItem 옵션 추가,
 *	hankalam	2019/07/30		2.2.6	mode = sel 일때 DISTINCT 추가
 *	jbaek		2019/07/30		2.2.6	select()시에 oitmHierIndex에 따라서 orderItem을 distinct하게 보여주도록 추가.
 *										download()시에 distributionChannelCode를 선택값으로 변경.
 *	hankalam	2019/06/28		2.2.6	name() 추가
 *	jbaek		2019/05/30		2.2.6	기존 select()-> cond()변경. list에 select() 기능추가. StopItem, PackDeal추가
 *	jbaek		2018/10/30		2.2.5	isChinaCountry() 삭제
 *	jbaek		2018/10/30		2.2.5	barCodeMultiItems 기능 추가
 *	hankalam	2017/02/28		2.2.4	Selling SKU vs Plant SKU 기능 추가
 *										tree() : Party 별 Allow UOM 추가
 *	hankalam	2015/10/30		2.2.3	웹취약성 수정. setAttributeCondition() : distributionChannelCode 파라미터 위험문자 검사.
 *	jbaek		2014/02/16		2.2.2	Plant SKU 제외 기능 개발
 *	jbaek		2013/11/30		2.2.1	Material Status Auto Update 기능 개발
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.dpr.Country;
import com.irt.dpr.Item;
import com.irt.dpr.ItemIntro;
import com.irt.dpr.ItemStatus;
import com.irt.dpr.Order;
import com.irt.dpr.OrderItem;
import com.irt.dpr.util.CondPred;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.json.Jsoner;
import com.irt.rbm.RBMSystem;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModel;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;

/**
 *
 */
@javax.servlet.annotation.WebServlet( urlPatterns = { "/servlet/DPRItem" } )
public class DPRItem extends DPRServletModel {//@formatter:off
	public final static String MODE_TREE				= "tree";
	public final static String MODE_TREE_DEFAULT		= "treedef";
	public final static String MODE_REMOVEALL			= "delall";
	public final static String MODE_COND_SETTING		= "rtp";
	public final static String MODE_MOD_STATUS			= "modstatus";
	public final static String MODE_MOD_STATUSINPUT		= "imodstatus";
	public final static String MODE_SET_TRADEPARTNER	= "stp";

	public final static String PARAM_TYPE				= "type";
	public final static String TYPE_ITEM				= "itm";
	public final static String TYPE_ORDER				= "ord";
	public final static String TYPE_SOLD				= "sold";
	public final static String TYPE_SHIP				= "ship";

	public final static String PARAM_SEARCHTYPE			= "searchType";
	public final static String SEARCHTYPE_ALL			= "all";

	public final static String PARAM_REQTYPE			= "rtype";
	public final static String REQTYPE_SEARCH			= "search";
	public final static String REQTYPE_QUICK			= "quick";

	public final static String PARAM_ITEMBASISTYPE		= "btype";
	public final static String ITEMBASISTYPE_ITEM		= "itm";
	public final static String ITEMBASISTYPE_ORDER		= "ord";
	public final static String ITEMBASISTYPE_ORDER_CUSTOMER		= "ordall";

	public final static String PARAM_MATERIALSTATUSTYPE = "mstype";
	public final static String MATERIALSTATUSTYPE_SALES = "sales";
	public final static String MATERIALSTATUSTYPE_CHAIN = "chain";
	public final static String MATERIALSTATUS_NULLDATESTRING = "00000000";

	public final static String VIEWTYPE_FRAME			= "frm";
	public final static String VIEWTYPE_INFO			= "info";

	private final static String HIERARCHY_PARAMETERKEY	= "productHierarchyCode_";

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap parameterMap = new ParameterMap( ctx.req, true );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();
		String btype = (String)ctx.extraObj;
		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			btype = TYPE_ORDER;
		}
		String organizationCode = getSavedOrganizationCode( ctx );
		//
		Condition.putConditionValueOnly(conditionMap, "useStopItem", Country.isFeature(organizationCode, "useStopItem"));
		Condition.putConditionValueOnly(conditionMap, "usePackDeal", Country.isFeature(organizationCode, "usePackDeal"));
		Condition.putConditionValueOnly( conditionMap, "countryCode", getUserCountryCode(ctx) );

		// organizationCode, divisionCode, distributionChannelCode
		setDefaultParameterMultiDist( ctx, conditionMap );

		if( MODE_LIST.equals(ctx.mode) || MODE_LISTCOUNT.equals(ctx.mode) ) {
			Map<String, Object> _condition = new java.util.HashMap<String, Object>();
			_condition.put( "organizationCode", organizationCode );
			_condition.put( "countryCode", getUserCountryCode(ctx) );
			_condition.put("organizationCode", getSavedOrganizationCode(ctx));
			_condition.put("displayLanguage", getDisplayLanguage(ctx));
			setAttributePartyMaster(ctx, _condition, PARTYMASTER_DISTRIBUTIONCHANNEL);

			if( !parameterMap.containsKey("distributionChannelCode") ) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>) ctx.req.getAttribute( "distributionChannels" );
				conditionMap.put( "distributionChannelCode", Record.extractObjectArray(distributionChannels, "distributionChannelCode") );
			}
		}

		Object partyCodeObj = getRequestPartyCode( ctx );

		if( parameterMap.containsKey("officeCode") )
			Condition.putConditionValueOnly( conditionMap, "officeCode", parameterMap.get("officeCode") );
		if( parameterMap.containsKey("groupCode") )
			Condition.putConditionValueOnly( conditionMap, "groupCode", parameterMap.get("groupCode") );
		if( partyCodeObj != null ) {
			String partyCode = null;
			if( partyCodeObj instanceof String[] ) {
				String[] t = (String[])partyCodeObj;
				if( t.length <= 1 ) {
					partyCode = t[0];
					Condition.putConditionValueOnly( conditionMap, "partyCode", partyCode );
				} else {
					Condition.putConditionValueOnly( conditionMap, "partyCode", partyCodeObj );
				}
			} else {
				Condition.putConditionValueOnly( conditionMap, "partyCode", partyCodeObj );
			}
			Condition.putConditionValueOnly( conditionMap, "partyCode", partyCode );
		}
		if( parameterMap.containsKey( "shipPartyCode" ) )
			Condition.putConditionValueOnly( conditionMap, "shipPartyCode", parameterMap.get( "shipPartyCode" ) );

		if( parameterMap.containsKey("oitmHierIndex") ) {
			Condition.putConditionValueOnly( conditionMap, "oitmHierIndex", parameterMap.get( "oitmHierIndex" ) );
		}

		if( parameterMap.containsKey("plantInd") )
			Condition.putConditionValueOnly( conditionMap, "plantInd", parameterMap.get("plantInd") );

		if( parameterMap.containsKey("newItemInd") )
			Condition.putConditionValueOnly( conditionMap, "newItemInd", parameterMap.get("newItemInd") );
		if( parameterMap.containsKey("promotionItemInd") )
			Condition.putConditionValueOnly( conditionMap, "promotionItemInd", parameterMap.get("promotionItemInd") );
		if( parameterMap.containsKey("itemConsumerEANCode") )
			Condition.putConditionValueOnly( conditionMap, "itemConsumerEANCode", parameterMap.get("itemConsumerEANCode"), Condition.CONDTYPE_CONTAINS );

		String orderType = parameterMap.getParameter("orderType");
		boolean useDangerousItem = Country.isFeature(organizationCode, "useDangerousItem") && (orderType != null && orderType.length() > 0 );
		if( useDangerousItem ) {
			String dangerousPlant = Country.getFeatureValue( organizationCode, "dangerousPlant" );
			if( dangerousPlant != null ) {
				Condition.putConditionValueOnly( conditionMap, "useDangerousItem", useDangerousItem );
				Condition.putConditionValueOnly( conditionMap, "dangerousPlant", dangerousPlant );
				Condition.putConditionValueOnly( conditionMap, "dangerousNumber", Item.ITEMTYPE_DANGEROUS );
				conditionMap.put( "dangerousInd", (Order.ORDER_TYPE_DANGEROUS.equals(orderType) ? "Y" : "N") );
			} else {
				throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_NEEDED_ENVIRONMENT", "dangerousPlant") );
			}
		}

		Condition.putConditionValueOnly( conditionMap, "btype", btype );
		if( parameterMap.containsKey("ltype") ) {
			Condition.putConditionValueOnly( conditionMap, "ltype", parameterMap.get("ltype") );
		}
		if( parameterMap.containsKey("itemCode") ) {
			Condition.putConditionValueOnly( conditionMap, "itemCode", parameterMap.get("itemCode"), Condition.CONDTYPE_CONTAINS );
		}
		if( parameterMap.containsKey("itemName") ) {
			Condition.putConditionValueOnly( conditionMap, "itemName", parameterMap.get("itemName"), Condition.CONDTYPE_CONTAINS );
		}
		if( parameterMap.containsKey("orderKey") )
			Condition.putConditionValueOnly( conditionMap, "orderKey", parameterMap.get("orderKey"), Condition.CONDTYPE_CONTAINS );

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

		// authUniqId
		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			Condition.putConditionValueOnly( conditionMap, "authUniqId", ctx.sessionMng.getUniqId() );
			Condition.putConditionValueOnly( conditionMap, "authPartyValue", "Y" );
		}

		return conditionMap;
	}

	protected Map<String, Object> createItemTreeConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap = createConditionMap( ctx );

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

		String odrdlvGroup = ctx.req.getParameter("odrdlvGroup");
		if( odrdlvGroup != null ) {
			conditionMap.put("odrdlvGroup", odrdlvGroup);
			String dlvrySpBrands = RBMSystem.getSystemEnv("DPR", "dlvrySpBrands;"+conditionMap.get("organizationCode"));
			if( "SP".equals(odrdlvGroup) ) {
				if( dlvrySpBrands != null && dlvrySpBrands.length() > 0 ) {
					conditionMap.put("brandCode", dlvrySpBrands.split(","));
				}
			} else if( "RG".equals(odrdlvGroup) ) {
				if( dlvrySpBrands != null && dlvrySpBrands.length() > 0 ) {
					CondPred.putIsNotEquals(conditionMap, "brandCode", dlvrySpBrands.split(","));
				}
			}
		}

		com.irt.dpr.ItemCondition.setItemCondition( conditionMap, com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()) );

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_FRAME.equals(ctx.mode) )
			return frame( ctx );
		else if( MODE_MODIFY.equals(ctx.mode) )
			return update( ctx, false );
		else if( MODE_TREE.equals(ctx.mode) )
			return tree( ctx );
		else if( MODE_TREE_DEFAULT.equals(ctx.mode) )
			return treeDefault( ctx );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			return setAttributeCondition( ctx );
		else if( MODE_MOD_STATUS.equals(ctx.mode) )
			return modifyStatus( ctx );
		else if( MODE_MOD_STATUSINPUT.equals(ctx.mode) )
			return modifyStatusInput( ctx );
		else if( MODE_SET_TRADEPARTNER.equals(ctx.mode) )
			return setTradePartner( ctx );
		return super.doRequest( ctx, isPost );
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		QueryableManager db = (QueryableManager)ctx.db;
		String btype = (String)ctx.extraObj;

		Map<String, Object> conditionMap = createConditionMap( ctx );

		String fileName = null;
		com.irt.data.cols.ColumnList columnList = null;
		if( ITEMBASISTYPE_ORDER_CUSTOMER.equals(btype) ) {
			columnList = getColumnList( ctx, "DPRItem.OITM%DOWN.PN" );
			fileName = ctx.msghandler.getMessage( "DPR_ITEM_DOWNLOAD_FILE_SELLINGSKU_WP" );
		} else {
			String columnListName = "DPRItem.OITM%DOWN";
			if( Country.KOREA_ORGANIZATION.equals(getSavedOrganizationCode(ctx)) ) {
				columnListName += ".KR";
			}
			columnList = getColumnList( ctx, columnListName );
			fileName = ctx.msghandler.getMessage( "DPR_ITEM_DOWNLOAD_FILE_SELLINGSKU" );
		}

		DataWriter out = createTextDataWriter( ctx, fileName );
		try {
			ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
			db.write( out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE );
		} finally {
			out.flush();
			out.close();
		}

		return true;
	}

	@Override
	protected boolean downloadInput( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();
/* encoding과 같은 값을 설정 할 경우 recordMap.put() 이용 */
		Map<String, Object> recordMap = new java.util.HashMap<String, Object> ();

		setDefaultParameter( ctx, conditionMap );
		conditionMap.put( "countryCode", getUserCountryCode(ctx) );
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL );
		//ctx.req.setAttribute( "distributionChannels", getSelectListNaZeroValue(ctx, (List<Map<String ,Object>>) ctx.req.getAttribute("distributionChannels"), "distributionChannelCode", "distributionChannelName") );

		if( recordMap.get("encoding") == null && systemConfig.getEncoding(ctx.pageConfig.getHtmlPage().getLocale()) != null )
			recordMap.put( "encoding", systemConfig.getEncoding(ctx.pageConfig.getHtmlPage().getLocale()) );

		ctx.req.setAttribute( "record", recordMap );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_item_download.jsp" );
	}

	private String itemCodeFillValue( String value ) {
		if( value != null && (value.startsWith("*") || value.startsWith("?")) )
			return value;

		return Item.fixedItemCodeValue( value );
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	/**
	 * vtype : Material Information이 frame 화면으로 구성 되었을 때 Information 초기화면
	 *	- 'frm' : Window frame 초기화면.
	**/
	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		QueryableManager db = (QueryableManager)ctx.db;

		String btype = (String)ctx.extraObj;
		if( ITEMBASISTYPE_ITEM.equals(btype) )
			db = new Item( ctx.handler );
		else
			db = new OrderItem( ctx.handler );

		String vtype = ctx.req.getParameter( "vtype" );
		if( VIEWTYPE_FRAME.equals(vtype) ) {
			ctx.pageConfig.setProperty( "infomsg", ctx.msghandler.getMessage("MSG_ITEM_CONDITION_NEEDED") );
			ctx.pageConfig.setProperty( "vtype", vtype );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_item_input.jsp" );
		}

		Map<String, Object> primaryMap = new java.util.HashMap<String, Object> ();
		String itemCode = ctx.req.getParameter( "itemCode" );
		String countryCode = getUserCountryCode( ctx );
		primaryMap.put( "countryCode", countryCode );
		primaryMap.put( "itemCode", itemCode );
		primaryMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		setDefaultParameter( ctx, primaryMap );
		String organizationCode = (String)primaryMap.get( "organizationCode" );

		if( itemCode == null || itemCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Object partyCodeObj = getRequestPartyCode( ctx );
		String partyCode = null;
		if( partyCodeObj instanceof String[] ) {
			partyCode = ((String[])partyCodeObj)[0];
		}
		if( partyCode != null )
			primaryMap.put( "partyCode", partyCode );

		Map<String, Object> recordMap = null;

		if( ITEMBASISTYPE_ORDER.equals(btype) ) {
			String[] fieldKeys = { "createDateTime", "status", "startAvailDate", "countryCode", "endAvailDate",
					"organizationCode", "updateUserId", "partyCode", "newItemInd", "divisionCode", "promotionInd",
					"distributionChannelCode", "updateDateTime", "itemCode", "itemName", "partyName", "officeCode",
					"groupCode", "districtCode", "productCategoryCode", "megabrandCode", "brandCode", "baseproductCode",
					"variantCode", "putupCode", "editableDescription", "intro", "editableIntro", "nvlIntro",
					"priceCurrency", "price", "shelfLife", "baseProductName", "megaBrandName", "brandName",
					"variantName", "putupName", "productHR1Name", "productHR2Name", "productHR3Name", "productHR4Name",
					"productHR5Name", "productHR6Name", "productHRFullName" };
			recordMap = db.getRecord( primaryMap, fieldKeys );
		} else
			recordMap = db.getRecord( primaryMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );

		ctx.req.setAttribute( "record", recordMap );
		ctx.pageConfig.setProperty( "countryCode", countryCode );
		ctx.pageConfig.setProperty( "itemCode", itemCode );

		/* UOM Data*/
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRItemUOM%LIST" );
		ctx.req.setAttribute( "records_uom", new com.irt.dpr.ItemUOM(ctx.handler).getRecords(primaryMap, columnList.getFieldKeys() ) );
		ctx.req.setAttribute( "columnList", columnList );

		// auth checking
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( true );
		else
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRItem.MNG") );

		ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
		if( ctx.sessionMng.isAuthorized("DPR", "DPRItem.MNG") && Country.isFeature(organizationCode, "useItemMod") ) {
			if( inputting ) {
				ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INPUT );
				ctx.req.setAttribute( "fieldSet", ( ITEMBASISTYPE_ITEM.equals(btype) ? ((Item)db).getFieldSet(false) : ((OrderItem)db).getFieldSet(false) ) );
			}
		}

		return forward( ctx, systemConfig.getJspPath() + "/dpr_item_input.jsp" );
	}

	protected boolean frame( Context ctx ) throws IOException, ServletException, SQLException {
		return forward( ctx, systemConfig.getJspPath() + "/dpr_item_main.jsp" );
	}

	private Object getRequestPartyCode( Context ctx ) throws ServletModelException, SQLException {
		Object partyCode = ctx.req.getParameterValues( "partyCode" );
		String btype = (String)ctx.extraObj;

		if( (partyCode == null) && ITEMBASISTYPE_ORDER.equals(btype)
				&& !ITEMBASISTYPE_ORDER_CUSTOMER.equals(btype)
				&& com.irt.rbm.SessionMng.GROUPCLASS_ORDER.equals(ctx.sessionMng.getGroupClass()) )
			partyCode = getUserDistributorCode( ctx );

		return (partyCode == null || (partyCode instanceof String[] && ((String[])partyCode).length == 0) ? null : partyCode);
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) {
			String organizationCode = getSavedOrganizationCode( ctx );
			String itemCode = ctx.req.getParameter( "itemCode" );

			if( ctx.sessionMng.isAuthorized("DPR", "DPRItem.MNG") && Country.isFeature(organizationCode, "useItemMod") && itemCode != null )
				pageConfig.setMode( ctx.mode = MODE_MODIFYINPUT );
			else
				pageConfig.setMode( ctx.mode = MODE_INFO );
		}

		String btype = ctx.req.getParameter( PARAM_ITEMBASISTYPE );
		if( btype == null || btype.length() == 0 )
			btype = ITEMBASISTYPE_ORDER;

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItem.INF" );
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItem.DOWN" );
		else if( MODE_DOWNLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItem.DOWN" );
		else if( MODE_SELECT.equals(ctx.mode) || MODE_NAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItem.LST" );
		else if( MODE_LIST.equals(ctx.mode) ) {
			if( ITEMBASISTYPE_ORDER_CUSTOMER.equals(btype) )
				pageConfig.setSystemPackageCode( "DPR", "DPRItem.WithPlant.LST" );
			else
				pageConfig.setSystemPackageCode( "DPR", "DPRItem.LST" );
		} else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItem.MNG" );
		else if( MODE_FRAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_TREE_DEFAULT.equals(ctx.mode) || MODE_TREE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItem.LST" );
		else if( MODE_MOD_STATUS.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItem.MNG" );
		else if( MODE_MOD_STATUSINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItem.MNG" );
		else if( MODE_REMOVE.equals(ctx.mode) || MODE_REMOVEALL.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItem.MNG" );
		else if( MODE_SET_TRADEPARTNER.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPRItem.WithPlant.LST" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );


		ctx.pageConfig.setProperty( "btype", btype );
		ctx.extraObj = btype;

		if( ITEMBASISTYPE_ITEM.equals(btype) )
			ctx.db = new Item( ctx.handler );
		else
			ctx.db = new ItemStatus( ctx.handler );

		if( !MODE_FRAME.equals(ctx.mode) && !MODE_TREE.equals(ctx.mode) && !MODE_TREE_DEFAULT.equals(ctx.mode) ) {
			String messageKey = "TITLE_DPR_ITEM_"+ btype.toUpperCase() + "_";
			pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
			pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );
			setPath( ctx, "jsp.MENU_MATERIAL" );
		} else {
			String title = ctx.msghandler.getMessage( "TITLE_DPR_ITEM_" + ctx.mode.toUpperCase() );
			pageConfig.setTitle( title );
			setPath( ctx, "jsp.MENU_MATERIAL", title );
		}
	}

	@Override
	protected boolean list( Context ctx, boolean listing ) throws IOException, ServletException, SQLException {
		QueryableManager db = (QueryableManager)ctx.db;

		String btype = (String)ctx.extraObj;
		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			btype = TYPE_ORDER;
		}

		Map<String, Object> conditionMap = createConditionMap( ctx );

		List<String> optionKeys = new ArrayList<String>();
		if( Country.isFeature((String)conditionMap.get("organizationCode"), "useJdms") ) {
			optionKeys.add("useJdms");
		}

		if( ITEMBASISTYPE_ITEM.equals(btype) )
			db = new Item( ctx.handler );
		else
			db = new ItemStatus( ctx.handler );

		conditionMap.put( "btype", btype );

		if( conditionMap.get("oitmHierIndex") != null ) {
			String oitmHierIndex = (String)conditionMap.get("oitmHierIndex");
			if( "1".equals(oitmHierIndex) ) {
				optionKeys.add("oitmHier1");
			} else if( "2".equals(oitmHierIndex) ) {
				optionKeys.add("oitmHier2");
			} else if( "3".equals(oitmHierIndex) ) {//office
				optionKeys.add("oitmHier3");
			} else if( "4".equals(oitmHierIndex) ) {//group
				optionKeys.add("oitmHier4");
			} else if( "5".equals(oitmHierIndex) ) {//party
				optionKeys.add("oitmHier5");
			}
		}

		String suffix = (listing ? "LIST" : "SEL");
		String[] fieldKeys = null;
		com.irt.data.cols.ColumnList columnList = null;
		if( ITEMBASISTYPE_ITEM.equals(btype) ) {
			columnList = getColumnList( ctx, "DPRItem%"+suffix, optionKeys.toArray(new String[0]) );
			fieldKeys = columnList.getFieldKeys();
			if( "SEL".equals(suffix) )
				conditionMap.put( Condition.DISTINCT_CONDITIONKEY, "Y" );
		} else if( ITEMBASISTYPE_ORDER.equals(btype) ) {
			if( Country.KOREA_ORGANIZATION.equals(getSavedOrganizationCode(ctx)) ) {
				suffix += ".KR";
			}
			columnList = getColumnList( ctx, "DPRItem.OITM%"+suffix, optionKeys.toArray(new String[0]) );
			fieldKeys = columnList.getFieldKeys();
			conditionMap.put( Condition.DISTINCT_CONDITIONKEY, "Y" );
		} else {
			suffix += ".PN";
			if( Country.KOREA_ORGANIZATION.equals(getSavedOrganizationCode(ctx)) ) {
				suffix += ".KR";
			}
			columnList = getColumnList( ctx, "DPRItem.OITM%"+suffix, optionKeys.toArray(new String[0]) );
			fieldKeys = columnList.getFieldKeys( "shipPartyCode" );
			if( "party".equals(ctx.req.getParameter("vtype")) ) {
				String partyCode = getUserPartyCode( ctx );
				if( partyCode != null )
					conditionMap.put( "partyCode", partyCode );
				else
					conditionMap.put( "partyCode", "" );
			}
			conditionMap.put( Condition.DISTINCT_CONDITIONKEY, "Y" );
		}
		List<String> fieldKeyList = new ArrayList<String>(java.util.Arrays.asList(fieldKeys));
		if( conditionMap.get("oitmHierIndex") != null ) {
			String oitmHierIndex = (String)conditionMap.get("oitmHierIndex");
			if( "1".equals(oitmHierIndex) ) {
				fieldKeyList.remove("partyCode");
				fieldKeyList.remove("groupCode");
				fieldKeyList.remove("officeCode");
				fieldKeyList.remove("distributionChannelCode");
				conditionMap.remove("partyCode");
				conditionMap.remove("groupCode");
				conditionMap.remove("officeCode");
				conditionMap.remove("distributionChannelCode");
			} else if( "2".equals(oitmHierIndex) ) {
				fieldKeyList.remove("partyCode");
				fieldKeyList.remove("groupCode");
				fieldKeyList.remove("officeCode");
				conditionMap.remove("partyCode");
				conditionMap.remove("groupCode");
				conditionMap.remove("officeCode");
			} else if( "3".equals(oitmHierIndex) ) {//office
				fieldKeyList.remove("partyCode");
				fieldKeyList.remove("groupCode");
				conditionMap.remove("partyCode");
				conditionMap.remove("groupCode");
			} else if( "4".equals(oitmHierIndex) ) {//group
				fieldKeyList.remove("partyCode");
				conditionMap.remove("partyCode");
			} else if( "5".equals(oitmHierIndex) ) {//party
			}
			fieldKeys = fieldKeyList.toArray(new String[0]);
		}

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		String[] sortKeys = (fieldKeyList.contains("partyCode") ? new String[]{"partyCode", "itemName"} : new String[]{ "itemName" });
		ServletUtility.setSort( ctx.req, db, sortKeys );

		String partyCode = (String) conditionMap.get( "partyCode" );
		setUserPartiesCondition( ctx, conditionMap, "partyCode" );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, fieldKeys, idxVars[0], idxVars[1] );

		if( partyCode == null ) {
			conditionMap.remove( "partyCode" );
		}
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP );
		setAttributePartner( ctx, conditionMap, btype );

		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );

		if( ITEMBASISTYPE_ORDER_CUSTOMER.equals(btype) ) {
			setPath( ctx, "jsp.SUBMENU_SKUSTATUS" );
		} else {
			setPath( ctx, "jsp.SUBMENU_SELLINGSKU" );
		}


		// auth checking
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() )
			ctx.pageConfig.setManageAuth( true );
		else
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPRItem.MNG") );

		if( listing )
			return forward( ctx, systemConfig.getJspPath() + "/dpr_item_list.jsp" );
		else
			return forward( ctx, systemConfig.getJspPath() + "/dpr_item_select.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		String btype = (String)ctx.extraObj;
		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			btype = TYPE_ORDER;
		}

		QueryableManager db;
		if( ITEMBASISTYPE_ITEM.equals(btype) )
			db = new Item( ctx.handler );
		else
			db = new ItemStatus( ctx.handler );

		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRItem.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}

		setUserPartiesCondition( ctx, conditionMap, "partyCode" );

		if( ITEMBASISTYPE_ORDER_CUSTOMER.equals(btype) ) {
			//com.irt.data.cols.ColumnList columnList = null;
			//columnList = getColumnList( ctx, "DPRItem.OITM%LIST.PN" );
			Map<String, Object> map = ((ItemStatus)db).getRecordCountWithPlant( conditionMap, new String[] { "withPlantCount" } );
			ctx.pageConfig.getListIndexVariables()[2] = ((Number)map.get("withPlantCount")).intValue();
		} else if( ITEMBASISTYPE_ORDER.equals(btype) ) {
			ctx.pageConfig.getListIndexVariables()[2] = ((ItemStatus)db).getSSLRecordCount( conditionMap );
		} else {
			ctx.pageConfig.getListIndexVariables()[2] = db.getRecordCount( conditionMap );
		}

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean name( Context ctx ) throws IOException, ServletException, SQLException {
		String itemCode = ctx.req.getParameter( "itemCode" );
		if( itemCode != null && itemCode.length() > 0 ) {
			Map<String, Object> conditionMap = createConditionMap(ctx);
			conditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");
			List<Map<String, Object>> recordList = ((QueryableManager)ctx.db).getRecords( conditionMap
					, new String[] { "itemCode", "itemName" }, 0, 2 );

			if( recordList != null && recordList.size() == 1 )
				ctx.req.setAttribute( "record", recordList.get(0) );
			else
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_NO_RECORD_FOUND_TO_SELECT") );
		}
		ctx.pageConfig.setProperty( "field", "itemCode=code, itemName=name" );

		return forward( ctx, systemConfig.getJspPath() + "/pub_common_name.jsp" );
	}

	@Override
	protected boolean remove( Context ctx ) throws IOException, ServletException, SQLException {
		String btype = (String)ctx.extraObj;
		if( !ITEMBASISTYPE_ORDER_CUSTOMER.equals(btype) ) {
			throw new ServletModelException( ServletModelException.INTERNAL_ERROR );
		}
		ItemStatus db = (ItemStatus)ctx.db;

		String organizationCode = getSavedOrganizationCode( ctx );
		String distributionChannelCode = ctx.req.getParameter( "distributionChannelCode" );
		String divisionCode = getDivisionCode( ctx );
		String countryCode = getUserCountryCode( ctx );
		String[] partyCodes = ctx.req.getParameterValues( "partyCode" );
		String[] itemCodes = ctx.req.getParameterValues( "itemCode" );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( distributionChannelCode == null || distributionChannelCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( partyCodes == null || partyCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( itemCodes == null || itemCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		// 레코드 삭제
		int count = 0;
		Map<String, Object> primaryMap = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for( int i = 0; i < itemCodes.length; i++) {
			try {
				count += db.delete( organizationCode, distributionChannelCode, divisionCode, countryCode, partyCodes[i], itemCodes[i] );
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				errorList.add( createErrorMap(db.getFieldValue(primaryMap, "itemCode"), dataEx) );
			}
		}

		// forward & sendRedirect
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );
			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		} else {
			if( count > 0 ) {
				ctx.handler.commit();
			}
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", String.valueOf(count)) );
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

	protected void setProductHierarchyDivisionLevel( Context ctx, Map<String, Object> parameterMap ) throws ServletModelException, SQLException {
		com.irt.dpr.ProductHierarchy db = new com.irt.dpr.ProductHierarchy( ctx.handler );

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>( parameterMap );
		conditionMap.put( Condition.DISTINCT_CONDITIONKEY, "Y" );

		List<Map<String, Object>> recordList = db.getExistProductCategoris(conditionMap);

		ctx.req.setAttribute( "categories", recordList );
	}

	/**
	 * type : Material, Order를 구분
	 *	- itm : Material
	 *	- ord : Order
	**/
	protected boolean tree( Context ctx ) throws IOException, ServletException, SQLException {
		QueryableManager db = (QueryableManager)ctx.db;

		String btype = (String)ctx.extraObj;
		if( ITEMBASISTYPE_ITEM.equals(btype) )
			db = new Item( ctx.handler );
		else
			db = new OrderItem( ctx.handler );

		Map<String, Object> conditionMap = createItemTreeConditionMap( ctx );
		String type = ctx.req.getParameter( PARAM_TYPE );

		if( type == null || type.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( conditionMap.get("defaultHierarchyLevel") == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		setUserAuthParty( ctx, conditionMap );
		setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL );
		setAttributeOrganizationBrands(ctx);
		ctx.pageConfig.setProperty( "type", type );

		String organizationCode = (String)conditionMap.get( "organizationCode" );

		List<Map<String, Object>> recordList = null;
		if( organizationCode != null && organizationCode.length() > 0 ) {
			String partyCode = (String)conditionMap.get( "partyCode" );
			String divisionCode = getDivisionCode( ctx );
			String distributionChannelCode = getDistributionChannelCode( ctx );
			Map<String, Object> primaryMap = com.irt.dpr.Party.createPrimary( partyCode, organizationCode, distributionChannelCode, divisionCode );

			com.irt.dpr.OrderItem itemDB = new com.irt.dpr.OrderItem( ctx.handler );
			String allowUOM = null;
			if( com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM") )
				allowUOM = ((OrderItem)db).getDistAllowUOM( primaryMap );
			if (allowUOM == null)
				allowUOM = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "uom;" + organizationCode,
						com.irt.dpr.Party.DEFAULT_UOM);

			ctx.pageConfig.setProperty( "allowUOM", allowUOM );

			if( ITEMBASISTYPE_ITEM.equals(btype) )
				recordList = ((Item)db).getItemTreeList( conditionMap );
			else if( ITEMBASISTYPE_ORDER.equals(btype) && partyCode != null && partyCode.length() > 0 ) {
				recordList = ((OrderItem)db).getItemTreeList( conditionMap );

				//barCodeMultiItems
				Map<String, Object> barCodeMultiItems = null;
				if( SEARCHTYPE_ALL.equals(conditionMap.get(PARAM_SEARCHTYPE)) ) {
					barCodeMultiItems = getBarCodeMultiItems(recordList);
				} else {
					Map<String, Object> _conditionMap = new HashMap<String, Object>(conditionMap);
					Condition.clearCondition(_conditionMap, "newItemInd");
					Condition.clearCondition(_conditionMap, "promotionItemInd");
					Condition.clearCondition(_conditionMap, "itemConsumerEANCode");
					Condition.clearCondition(_conditionMap, "itemCode");
					Condition.clearCondition(_conditionMap, "itemName");

					if( conditionMap.equals(_conditionMap) ) {
						barCodeMultiItems = getBarCodeMultiItems(recordList);
					} else {
						List<Map<String, Object>> recordList_all = ((OrderItem)db).getItemTreeList( _conditionMap );
						barCodeMultiItems = getBarCodeMultiItems(recordList_all);
					}
				}
				ctx.req.setAttribute("barCodeMultiItems", Jsoner.getInstance().toJson(barCodeMultiItems));
			}

			if( recordList == null || recordList.size() == 0 ) {
				if( partyCode != null && partyCode.length() > 0 )
					ctx.pageConfig.setProperty( "treeMsg", ctx.msghandler.getMessage("MSG_NO_RECORD_FOUND") );
			} else {
				if( TYPE_ITEM.equals(type) )
					setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION );
				ctx.req.setAttribute( "records", recordList );
			}
		} else
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + (TYPE_ITEM.equals(type) ? "/dpr_item_tree.jsp" : "/dpr_orderitem_tree.jsp") );
	}

	/**@deprecated moved to dprapi. use com.irt.dpr.OrderItem.getBarCodeMultiItems.*/
	@Deprecated
	private Map<String, Object> getBarCodeMultiItems( List<Map<String, Object>> tree ) {
		Map<String, Object> barCodeMultiItems = new HashMap<String, Object>();

		if( tree != null  ) {
			Map<String, List<String>> temp = new HashMap<String, List<String>>();
			for( Map<String, Object> map : tree ) {
				final String barCode = (String)map.get("itemConsumerEANCode");
				final String itemCode = (String)map.get("itemCode");

				// has mapping
				if( barCode != null ) {
					List<String> multiItems = temp.get(barCode);
					if( multiItems == null ) {
						List<String> list = new ArrayList<String>();
						list.add(itemCode);
						temp.put(barCode, list);
					} else {
						multiItems.add(itemCode);
						temp.put(barCode, multiItems);
					}
				}
			}

			for( String key : temp.keySet() ) {
				List<String> multiItems = temp.get(key);

				if( multiItems.size() > 1 ) {
					barCodeMultiItems.put(key, multiItems);
				}
			}
			temp = null;
		}

		return barCodeMultiItems;
	}

	protected boolean treeDefault( Context ctx ) throws IOException, ServletException {
		return forward( ctx, systemConfig.getJspPath() + "/dpr_orderitem_tree_default.jsp" );
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		ItemIntro db = new ItemIntro( ctx.handler );

		String countryCode = getUserCountryCode( ctx );
		String itemCode = ctx.req.getParameter( "itemCode" );
		String organizationCode = getSavedOrganizationCode( ctx );
		String languageCode = getDisplayLanguage( ctx );
		String editableIntro = ctx.req.getParameter( "editableIntro" );
		String distributionChannelCode = getDistributionChannelCode( ctx );

		if( itemCode == null || itemCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( distributionChannelCode == null || distributionChannelCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> recordMap = new java.util.HashMap<String, Object> ();
		recordMap.put( "itemCode", itemCode );
		recordMap.put( "organizationCode", organizationCode );
		recordMap.put( "languageCode", languageCode );
		recordMap.put( "editableIntro", editableIntro );

		try {
			if( !db.modify(recordMap) )
				if( !db.regist(recordMap) )
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("ERR_CANNOT_UPDATE") );

			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			ctx.handler.commit();
		} catch ( DataException dataEx ){
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
		} catch ( SQLException sqlEx ){
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "internal error.", sqlEx );
		}

		Map<String, Object> conditionMap = Item.createPrimary( countryCode, itemCode, organizationCode, distributionChannelCode );
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		conditionMap.put( "status", Item.ITEMSTATUS_NORMAL );

		ctx.req.setAttribute( "record", db.getRecord(conditionMap) );
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ITEM_"+ ServletModel.MODE_INFO.toUpperCase()) );

		return info( ctx, true );
	}

	protected String getMaterialStatusDateString( Context ctx, String source ) throws ServletModelException {
		String result = null;
		if( MATERIALSTATUS_NULLDATESTRING.equals(source) )
			result = MATERIALSTATUS_NULLDATESTRING;
		else {
			try {
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
				source = source.replaceAll( "-", "" ).replaceAll( "/", "" ).replaceAll(" ", "");
				java.util.Date date = sdf.parse( source );
				result = sdf.format( date );
			} catch( java.text.ParseException e ) {
				throw new ServletModelException(
						ServletModelException.ERROR,
						ctx.handler.getMessageHandler().getMessage("ERR_INVALID_DATE", source)
				);
			}
		}
		return result;
	}

	protected boolean modifyStatus( Context ctx ) throws IOException, ServletException, SQLException {
		String btype = (String) ctx.extraObj;
		if( !ITEMBASISTYPE_ITEM.equals(btype) ) {
			ctx.db = new Item( ctx.handler );
			ctx.extraObj = ITEMBASISTYPE_ITEM;
		}
		Item db = (Item) ctx.db;

		String mstype = ctx.req.getParameter( "mstype" );

		String countryCode = getUserCountryCode( ctx );
		String itemCode = ctx.req.getParameter( "itemCode" );
		String organizationCode = getSavedOrganizationCode( ctx );
		String distributionChannelCode = getDistributionChannelCode( ctx );

		if( itemCode == null || itemCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( distributionChannelCode == null || distributionChannelCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> conditionMap = new ParameterMap(ctx.req);

		if( MATERIALSTATUSTYPE_CHAIN.equals(mstype) ) {
			Map<String, Object> recordMap = Item.createPrimary( countryCode, itemCode, organizationCode, distributionChannelCode );

			String chainStatus = com.irt.data.Record.extractString( conditionMap, "chainStatus" );
			String chainStatusFrom = com.irt.data.Record.extractString( conditionMap, "chainStatusFrom" );
			if( chainStatusFrom == null )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );


			if( (chainStatus == null && MATERIALSTATUS_NULLDATESTRING.equals(chainStatusFrom) )
					|| (chainStatus != null && !MATERIALSTATUS_NULLDATESTRING.equals(chainStatusFrom)) ) {

				recordMap.put("chainStatus", chainStatus );
				recordMap.put("chainStatusFrom", getMaterialStatusDateString(ctx, chainStatusFrom) );

				try {
					if( !db.modify(recordMap, new String[] { "chainStatus", "chainStatusFrom"}) )
						ctx.pageConfig.setMessage( ctx.msghandler.getMessage("ERR_CANNOT_UPDATE") );

					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
					ctx.handler.commit();
				} catch ( DataException dataEx ){
					ctx.handler.rollback();
					ctx.pageConfig.setMessage( dataEx.getMessage() );
					ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
					logger.info( "error.", dataEx );
				} catch ( SQLException sqlEx ){
					ctx.handler.rollback();
					ctx.pageConfig.setMessage( sqlEx.getMessage() );
					ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
					logger.info( "internal error.", sqlEx );
				}
			} else {
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER, ctx.msghandler.getMessage("MSG_DPR_ITEM_MATERIALSTATUS_WRONG_VALUES") );
			}
		} else {
			String salesStatus = com.irt.data.Record.extractString( conditionMap, "salesStatus" );
			String salesStatusFrom = com.irt.data.Record.extractString( conditionMap, "salesStatusFrom" );
			if( salesStatusFrom == null )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			if( (salesStatus == null && MATERIALSTATUS_NULLDATESTRING.equals(salesStatusFrom) )
					|| (salesStatus != null && !MATERIALSTATUS_NULLDATESTRING.equals(salesStatusFrom)) ) {

				Map<String, Object> targetCondMap = new java.util.HashMap<String, Object>();
				targetCondMap.put("countryCode", countryCode);
				targetCondMap.put("organizationCode", organizationCode);
				targetCondMap.put("itemCode", itemCode);

				com.irt.data.DataResult result = null;
				try {
					List<Map<String, Object>> records = db.getRecords(targetCondMap);
					if( records != null ) {
						for (Map<String, Object> map: records) {
							map.put( "salesStatus", salesStatus );
							map.put( "salesStatusFrom", getMaterialStatusDateString(ctx, salesStatusFrom) );
						}
						result = db.modifyAll( records, new String[]{"salesStatus","salesStatusFrom"} );

						ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
						ctx.handler.commit();
					}
				} catch ( SQLException sqlEx ){
					ctx.handler.rollback();
					ctx.pageConfig.setMessage( sqlEx.getMessage() );
					ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
					logger.info( "internal error.", sqlEx );
				}
				if( result.getException() != null )
					logger.error( result.getException() );

				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_ITEM_MATERIALSTATUS_MANAGE_CNT"
						, String.valueOf(result.getCount()), String.valueOf(result.getSuccessCount()), String.valueOf(result.getErrorCount()) ));
			} else {
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER, ctx.msghandler.getMessage("MSG_DPR_ITEM_MATERIALSTATUS_WRONG_VALUES") );
			}
		}

		return modifyStatusInput( ctx );
	}

	protected boolean modifyStatusInput ( Context ctx ) throws IOException, ServletException, SQLException {
		String btype = (String) ctx.extraObj;
		if( !ITEMBASISTYPE_ITEM.equals(btype) ) {
			ctx.db = new Item( ctx.handler );
			ctx.extraObj = ITEMBASISTYPE_ITEM;
		}
		Item db = (Item) ctx.db;

		com.irt.data.cols.ColumnList columnList = null;
		if( ITEMBASISTYPE_ITEM.equals(btype) )
			columnList = getColumnList( ctx, "DPRItem%LIST" );
		else if( ITEMBASISTYPE_ORDER.equals(btype) )
			columnList = getColumnList( ctx, "DPRItem.OITM%LIST" );
		else
			columnList = getColumnList( ctx, "DPRItem.OITM%LIST.PN" );

		//primary
		String countryCode = getUserCountryCode( ctx );
		String itemCode = ctx.req.getParameter( "itemCode" );
		String organizationCode = getSavedOrganizationCode( ctx );
		String distributionChannelCode = getDistributionChannelCode( ctx );

		if( itemCode == null || itemCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( distributionChannelCode == null || distributionChannelCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> primaryMap = Item.createPrimary( countryCode, itemCode, organizationCode, distributionChannelCode );
		Map<String, Object> recordMap = db.getRecord( primaryMap );

		ctx.req.setAttribute( "columnList", columnList );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.NO_RECORD_FOUND );
		ctx.req.setAttribute( "record", recordMap );
		if( ctx.req.getAttribute("record") == null )
			ctx.req.setAttribute( "fieldSet", ((Item)ctx.db).getFieldSet(false) );
		ctx.pageConfig.setManageAuth( true );

		db.setSort("statusCode");
		List materialStatusNames = db.getMaterialStatusDescriptions();
		ctx.req.setAttribute("materialStatusNames", materialStatusNames);

		setAttributePartyMaster( ctx, recordMap, PARTYMASTER_DISTRIBUTIONCHANNEL );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_itemstatus_input.jsp" );
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

	public void setAttributePartner( Context ctx, Map<String, Object> conditionMap, String type ) throws ServletModelException, SQLException {
		String soldPartyCode = null;
		if( "party".equals(ctx.req.getParameter("vtype")) )
			soldPartyCode = getUserPartyCode( ctx );

		if( ITEMBASISTYPE_ORDER_CUSTOMER.equals(type) ) {
			{
				Map <String, Object> _condition = new java.util.HashMap<String, Object> ( conditionMap );
				_condition.remove( "partyCode" );
/*
				setAttributePartner( ctx, _condition, PARTNER_SHIP );

				if( soldPartyCode == null )
					soldPartyCode = ctx.req.getParameter( "partyCode" );
				String shipPartyCode = ctx.req.getParameter( "shipPartyCode" );
				if( soldPartyCode != null && soldPartyCode.length() > 0 )
					_condition.put( "partyCode", soldPartyCode );
				else if( shipPartyCode != null && shipPartyCode.length() > 0 )
					_condition.put( "linkPartyCode", shipPartyCode );
*/
				setAttributePartner( ctx, _condition, PARTNER_SOLD );

				soldPartyCode = ctx.req.getParameter( "partyCode" );
				if( soldPartyCode != null && soldPartyCode.length() > 0 )
					_condition.put( "partyCode", soldPartyCode );

				setAttributePartner( ctx, _condition, PARTNER_SHIP );
			}
		} else {
			setProductHierarchyDivisionLevel( ctx, conditionMap );
			{
				Map <String, Object> _condition = new java.util.HashMap<String, Object> ( conditionMap );
				_condition.remove( "partyCode" );

				setAttributePartner( ctx, _condition, PARTNER_SOLD );
			}
		}
	}

	protected boolean setTradePartner( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		String type = ctx.req.getParameter( PARAM_TYPE );

		setDefaultParameterMultiDist( ctx, conditionMap );
		if( type == null || type.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( TYPE_SOLD.equals(type) ) {
			/*
			String shipPartyCode = ctx.req.getParameter( "shipPartyCode" );
			if( shipPartyCode != null && shipPartyCode.length() > 0 )
				conditionMap.put( "linkPartyCode", shipPartyCode );
			*/
			setAttributePartner( ctx, conditionMap, PARTNER_SOLD );
		} else if( TYPE_SHIP.equals(type) ) {
			String soldPartyCode = ctx.req.getParameter( "partyCode" );
			/*
			if( soldPartyCode == null || soldPartyCode.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
*/
			if( soldPartyCode != null && soldPartyCode.length() > 0 )
				conditionMap.put( "partyCode", soldPartyCode );

			setAttributePartner( ctx, conditionMap, PARTNER_SHIP );
		} else
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		ctx.pageConfig.setProperty( "type", type );
		ctx.req.setAttribute( "condition", conditionMap );

		//return forward( ctx, systemConfig.getJspPath() +"/dpr_item_tpset.jsp" );
		return forward( ctx, systemConfig.getJspPath() +"/dpr_item_cond.jsp" );
	}

}
