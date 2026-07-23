/*
 *	File Name:	DPREnquiryOrder.java
 *	Version:	2.2.18
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwdls3720	2026/01/30		2.2.18	info() : columnList에 getSavedOrganizationCode( ctx ) 옵션 적용
 *	dudwls3720	2024/07/31		2.2.17	createConditionMap() : checkXSS 기능 추가
 *	jbaek		2023/09/27		2.2.16	DPRPartyAuth.AUTH_ALL_READ 추가
 *	hankalam	2021/11/30		2.2.15	신규 UI/UX 적용
 *	hankalam	2020/12/31		2.2.14	Logistics Tracking 기능 추가
 *	hankalam	2020/07/31		2.2.13	freegoodsMapping(): type 추가
 *	jbaek		2020/06/30		2.2.12	Revise Order Feature.
 *	hankalam	2020/06/30		2.2.12	downHeader(), list(): columnList에 useDangerousItem 옵션키 적용, writeSheetHeaderBlock(): dangerousItem 항목 추가
 *	hankalam	2020/03/31		2.2.11	multiDownload(): cookieOption 추가
 *	hankalam	2019/07/31		2.2.10	Freegoods 기능 추가
 *	jbaek		2019/07/30		2.2.9	오류수정: reuseCellStyles 사용. (error: The maximum number of cell styles was exceeded.)
 *	jbaek		2019/07/30		2.2.9	downHeader() 추가, sync다운, unsync다운 추가.
 *	hankalam	2019/06/28		2.2.9	useCustomerPONumber 옵션 적용, useDetailCondition 옵션 적용
 *	jbaek		2019/05/30		2.2.9	PackDeal 기능 추가
 *	jbaek		2018/10/30		2.2.8	isChinaCountry() 삭제
 *	jbaek		2018/10/30		2.2.8	Order Extra Info 기능 추가
 *	jbaek		2018/03/31		2.2.7	오류수정: executeStatus()기능이 conditionMap사 용하도록 수정
 *	jbaek		2017/02/28		2.2.6	orderStatus multi download
 *	hankalam	2016/09/30		2.2.5	날짜 검색 조건 변경
 *	jbaek		2013/05/30		2.2.4	오류수정: orderNumber가 없을시 billingInfo가져오지 않게 변경
 *	jbaek		2013/02/28		2.2.3	OrderStatus시 Formatted Printing용 칼럼 리소스 추가
 *	lsinji		2009/06/30		2.2.2	DPROrder -> (DPRPlaceOrder, DPREnquiryOrder)로 변경
 *	lsinji		2009/04/18		2.2.1	executeEnquiry() parameter에 employeeID 제거
 *	guksm		2008/09/26		2.2.0	create
 *
 **/

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

import com.irt.data.AbstractFieldSet;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.dpr.Country;
import com.irt.dpr.FreeGoods;
import com.irt.dpr.LogisticsTracking;
import com.irt.dpr.LogisticsTracking.HttpClientException;
import com.irt.dpr.Order;
import com.irt.dpr.OrderDetail;
import com.irt.dpr.OrderRemark;
import com.irt.dpr.util.CondPred;
import com.irt.html.ColumnConfigureFile;
import com.irt.html.HtmlPage;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;

/**
 *
 */
@javax.servlet.annotation.WebServlet( urlPatterns = { "/servlet/DPREnquiryOrder" } )
public class DPREnquiryOrder extends DPROrderServletModel {//@formatter:off
	private final static String MODE_BILLING					= "bil";
	private final static String MODE_FREEGOODS_MANAGE_INPUT		= "ifgm";
	private final static String MODE_FREEGOODS_MANAGE			= "fgm";
	private final static String MODE_LIST_DETAIL				= "dlist";
	private final static String MODE_MULTI_DOWNLOAD				= "mdown";
	private final static String MODE_HEADER_DOWNLOAD			= "hdrdwn";
	private final static String MODE_LOGISTICS_QUERY_INFO		= "loqi";
	private final static String MODE_LOGISTICS_QUERY			= "loq";
	private final static String MODE_LOGISTICS_SENDREDIRECT		= "sndr";
	private final static String MODE_COND_SETTING				= "rtp";

	private final static String TYPE_CREATE						= "create";
	private final static String REQTYPE_REGIST					= "reg";

	private final static String DOCUMENTNUMBER_TYPE_ORDER		= "O";
	private final static String DOCUMENTNUMBER_TYPE_CUSTOMER	= "P";

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	@Override
	protected boolean cond( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();
		conditionMap.put( "startOrderDate", com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()) );
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDetailCondition") ) {
			conditionMap.put( "countryCode", getUserCountryCode(ctx) );
			setDefaultParameterMultiDist( ctx, conditionMap );
			setAttributePartyMaster( ctx, conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP );
			setAttributePartner( ctx, conditionMap );
		} else if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDangerousItem") ) {
			conditionMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
		}

		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() +"/dpr_order_cond.jsp" );
	}

	private Map<String, Object> createConditionMap( Context ctx ) throws ServletException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );
		String startOrderDate = Record.extractString( conditionMap, "startOrderDate" );
		String endOrderDate = Record.extractString( conditionMap, "endOrderDate" );
		String documentNumber = Record.extractString( conditionMap, "documentNumber" );
		Object orderKey = conditionMap.get( "orderKey" );
		Object orderNumber = conditionMap.get( "orderKey" );

		if( ((ParameterMap)conditionMap).checkXSS() )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );


		try {
			if( startOrderDate != null ) com.irt.data.Date.getInstance( startOrderDate );
			if( endOrderDate != null ) com.irt.data.Date.getInstance( endOrderDate );
		} catch( java.text.ParseException parseEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		try {
			boolean valid = false;

			String recentCount = (String) conditionMap.get( "recentCount" );
			if( recentCount == null || recentCount.length() < 1 ) {
				if( startOrderDate != null && startOrderDate.length() > 0  || endOrderDate != null && endOrderDate.length() > 0 ) {
					if( (startOrderDate != null && startOrderDate.length() > 0) && (endOrderDate == null || endOrderDate.length() < 1) ) {
						endOrderDate = startOrderDate;
						conditionMap.put( "endOrderDate" , endOrderDate );
					} else if( (endOrderDate != null && endOrderDate.length() > 0) && (startOrderDate == null || startOrderDate.length() < 1) ) {
						startOrderDate = endOrderDate;
						conditionMap.put( "startOrderDate" , startOrderDate );
					}

					conditionMap.put( "orderDate"+ Condition.SUFFIX_MIN_VALUE, com.irt.data.Date.getInstance(startOrderDate) );
					conditionMap.put( "orderDate"+ Condition.SUFFIX_MAX_VALUE, com.irt.data.Date.getInstance(endOrderDate).getDate(1) );
					conditionMap.put( "orderDate"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MIN );
					valid = true;
				}
				if( orderKey != null && orderNumber != null ) {
					valid = true;
				}
			} else {
				conditionMap.put( "recentCount", Integer.valueOf(recentCount) );
				valid = true;
			}
			if( (documentNumber != null && documentNumber.length() > 0)  ) {
				valid = true;
				String orderNumberType = Record.extractString( conditionMap, "orderNumberType" );
				if( DOCUMENTNUMBER_TYPE_ORDER.equals(orderNumberType) )
					conditionMap.put( "orderNumber", documentNumber );
				else if( DOCUMENTNUMBER_TYPE_CUSTOMER.equals(orderNumberType) )
					conditionMap.put( "simulationKey", documentNumber );
				else
					valid = false;
			}
			if( !valid )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		} catch( java.text.ParseException parseEx ) {
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );
		}

		String billingNumber = (String) conditionMap.get( "billingNumber" );
		if( billingNumber != null ) {
			conditionMap.put( "billingNumber"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_CONTAINS );
		}

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL_READ")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			Condition.putConditionValueOnly( conditionMap, "authUniqId", ctx.sessionMng.getUniqId() );
			Condition.putConditionValueOnly( conditionMap, "authPartyValue", "Y" );
		}
		Condition.putConditionValueOnly( conditionMap, Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );

		if( conditionMap.containsKey("packdealInd") ) {
			Condition.putConditionValueOnly(conditionMap, "isPackdealOrder", conditionMap.get("packdealInd"));
		}

		boolean useDangerousItem = Country.isFeature( getSavedOrganizationCode(ctx), "useDangerousItem" );
		if( !useDangerousItem ) {
			conditionMap.remove( "orderType" );
		}

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( isPost ) {
			if( MODE_MULTI_DOWNLOAD.equals(ctx.mode) ) return multiDownload( ctx );
			else if( MODE_HEADER_DOWNLOAD.equals(ctx.mode) ) return downHeader( ctx );
			else if( MODE_FREEGOODS_MANAGE.equals(ctx.mode) ) return freegoodsMapping( ctx );
		} else {
			if( MODE_BILLING.equals(ctx.mode) ) return enuiqryBilling( ctx );
			else if( MODE_LIST_DETAIL.equals(ctx.mode) ) return listDetail( ctx );
			else if( MODE_MULTI_DOWNLOAD.equals(ctx.mode) ) return multiDownload( ctx );
			else if( MODE_HEADER_DOWNLOAD.equals(ctx.mode) ) return downHeader( ctx );
			else if( MODE_FREEGOODS_MANAGE_INPUT.equals(ctx.mode) ) return freegoodsMappingInput( ctx );
			else if( MODE_LOGISTICS_QUERY.equals(ctx.mode) ) return logisticsQuery( ctx );
			else if( MODE_LOGISTICS_QUERY_INFO.equals(ctx.mode) ) return logisticsQueryInfo( ctx );
			else if( MODE_LOGISTICS_SENDREDIRECT.equals(ctx.mode) ) return logisticsSendRedirect( ctx );
			else if( MODE_COND_SETTING.equals(ctx.mode) ) return setAttributeCondition( ctx );
		}

		return super.doRequest( ctx, isPost );
	}

	protected boolean enuiqryBilling( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;

		String updateUserId = ctx.sessionMng.getUniqId();

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
		conditionMap.put( "orderKey", ctx.req.getParameter("orderKey") );
		conditionMap.put( "billingNumber", ctx.req.getParameter("billingNumber") );
		conditionMap.put( "soldPartyCode", ctx.req.getParameter("soldPartyCode") );
		conditionMap.put( "updateUserId", updateUserId );

		try {
			db.executeEnquiry( com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_BILLING, conditionMap );
		} catch ( DataException dataEx ) {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage(dataEx.getErrorKey()) );
		}

		com.irt.dpr.BillingDetail billingDetailDB = new com.irt.dpr.BillingDetail( ctx.handler );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRBilling.DETAIL%LIST" );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, billingDetailDB, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = billingDetailDB.getRecords(conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.req.setAttribute( "summary", billingDetailDB.getBillingSummary(conditionMap) );
		ctx.req.setAttribute( "orderInfo", db.getRecord(conditionMap) );

		return true;
	}

	protected boolean freegoodsMapping( Context ctx ) throws SQLException, IOException, ServletException {
		String orderKey = ctx.req.getParameter( "orderKey" );
		String orderNumber = ctx.req.getParameter( "orderNumber" );
		if( orderKey == null || orderKey.length() == 0 || orderNumber == null || orderNumber.length() == 0 ) {
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}
		String type = ctx.req.getParameter( "type" );
		ctx.pageConfig.setProperty( "type", type );
		Order db = (Order)ctx.db;
		Map<String, Object> conditionMap = Order.createPrimary( orderKey );
		Map<String, Object> headerMap;
		try {
			if( TYPE_CREATE.equals(type) ) {
				headerMap = db.getRecord( conditionMap
						, new String[] { "partyCode", "countryCode", "organizationCode", "distributionChannelCode", "divisionCode"
								, "shipPartyCode", "soldPartyCode", "orderType", "inDate", "inDateDefault", "simulationKey"
				} );
				String freegoodsOrderKey = db.getOrderKey();
				String simulationKey = (String) headerMap.get( "simulationKey" );
				simulationKey = simulationKey.replace( Order.NORMAL_ORDERKEY_SUFFIX, Order.FREEGOODS_ORDERKEY_SUFFIX );
				com.irt.data.Date today = com.irt.data.Date.getInstance( ctx.sessionMng.getTimeZone() );
				headerMap.put( "orderDate", today );
				headerMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
				headerMap.put( "orderKey", freegoodsOrderKey );
				headerMap.put( "simulationKey", simulationKey );
				headerMap.put( "status", Order.STATUS_CREATED );
				headerMap.put( "freegoodsOrderInd", "Y" );
				headerMap.put( "parentOrderKey", orderKey );
				headerMap.put( "orderNumber", orderNumber );
				if( !db.regist(headerMap) ) {
					throw new DataException( DataException.ERR_CANNOT_INSERT
							, ctx.msghandler.getMessage("MSG_DPR_FREEGOODS_ORDER") + " " + ctx.msghandler.getMessage(DataException.ERR_CANNOT_INSERT) );
				}


				Map<String,Object> statusListCondition = new HashMap<String,Object>();
				statusListCondition.put( "startOrderDate", com.irt.data.Date.getInstance() );
				statusListCondition.put( "endOrderDate", com.irt.data.Date.getInstance().getDate(1) );
				statusListCondition.put( "orderNumber", orderNumber );
				executeStatusList(ctx, statusListCondition);

				Map<String, Object> fgConditionMap = new java.util.HashMap<String, Object>();
				fgConditionMap.put( "orderKey", freegoodsOrderKey );
				fgConditionMap.put( "status", Order.STATUS_CREATED );
				executeStatus( ctx, fgConditionMap );

				db.updateOrderStatus( ctx.handler, freegoodsOrderKey, Order.ORDERSTATUS_HOLD, ctx.sessionMng.getUniqId() );
				FreeGoods fgDB = new FreeGoods( ctx.handler );
				fgDB.updateFreegoodsQty( ctx.handler, freegoodsOrderKey );
			} else {
				headerMap = db.getRecord( conditionMap );
				String status = (String) headerMap.get( "status" );
				String freegoodsOrderInd = (String) headerMap.get( "freegoodsOrderInd" );
				if( !Order.STATUS_ERROR.equals(status) ) {
					throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("MSG_DPR_ENQUIRYORDER_FREEGOODS_STATUS_ERROR") );
				}
				if( !"Y".equals(freegoodsOrderInd) ) {
					throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("MSG_DPR_ENQUIRYORDER_NOT_FREEGOODS") );
				}
				Map<String, Object> recordMap = Order.createPrimary( orderKey );
				recordMap.put( "orderNumber", orderNumber );
				recordMap.put( "status", Order.STATUS_CREATED );
				if( !db.modify(recordMap, new String[] { "orderNumber", "status" }) ) {
					throw new DataException( DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE) );
				}
				/*OrderDetail detailDB = new OrderDetail( ctx.handler );
				conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );
				String[] fieldKeys = new String[] { "lineNumber", "orderKey", "itemCode", "orderQty" };
				List<Map<String, Object>> recordList = detailDB.getRecords( conditionMap, fieldKeys );
				for( Map<String, Object> record : recordList ) {
					BigDecimal bgOrderQty = (BigDecimal)record.get( "orderQty" );
					int orderQty = bgOrderQty != null ? bgOrderQty.intValue() : 0;
					if( orderQty > 0 ) {
						Map<String, Object> fgConditionMap = new HashMap<String, Object>();
						fgConditionMap.put( "organizationCode", headerMap.get("organizationCode") );
						fgConditionMap.put( "officeCode", officeCode );
						fgConditionMap.put( "partyCode", headerMap.get("partyCode") );
						fgConditionMap.put( "itemCode", record.get("itemCode") );
						orderQty = new FreeGoods( ctx.handler ).getSurplusOrderQty( fgConditionMap, orderQty );
					}

					db.modify( record, new String[] { "orderQty" } );
				}*/

				Map<String,Object> statusListCondition = new HashMap<String,Object>();
				statusListCondition.put( "startOrderDate", com.irt.data.Date.getInstance().getDate(-1) );
				statusListCondition.put( "endOrderDate", com.irt.data.Date.getInstance().getDate(1) );
				statusListCondition.put( "orderNumber", orderNumber );
				executeStatusList( ctx, statusListCondition );

				Map<String, Object> fgConditionMap = new java.util.HashMap<String, Object>();
				fgConditionMap.put( "orderKey", orderKey );
				fgConditionMap.put( "status", Order.STATUS_CREATED );
				executeStatus( ctx, fgConditionMap );

				db.updateOrderStatus( ctx.handler, orderKey, Order.ORDERSTATUS_HOLD, ctx.sessionMng.getUniqId() );
				FreeGoods fgDB = new FreeGoods( ctx.handler );
				fgDB.updateFreegoodsQty( ctx.handler, orderKey );
			}
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
		} finally {
			ctx.handler.commit();
		}

		ctx.req.setAttribute( "header", db.getRecord(conditionMap) );
		return forward( ctx, systemConfig.getJspPath() +"/dpr_order_freegoods_info.jsp" );
	}

	protected boolean freegoodsMappingInput( Context ctx ) throws SQLException, IOException, ServletException {
		String type = ctx.req.getParameter( "type" );
		ctx.pageConfig.setProperty( "type", type );
		String orderKey = ctx.req.getParameter( "orderKey" );
		if( orderKey == null || orderKey.length() == 0 ) {
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}
		Order db = (Order)ctx.db;
		OrderDetail detailDB = new OrderDetail( ctx.handler );
		Map<String, Object> conditionMap = Order.createPrimary(orderKey);
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		Map<String, Object> headerMap = db.getRecord( conditionMap );
		String status = (String) headerMap.get( "status" );
		conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrder.FREGOODS%LIST" );
		String conditionKey;
		List<Map<String, Object>> recordList = null;
		if( TYPE_CREATE.equals(type) ) {
			conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
			conditionMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
			conditionMap.put( "freegoodsInd", "Y" );
			conditionMap.put( "simulationTotalQty", null );
			conditionMap.put( "simulationTotalQty_min", 0 );
			String[] fieldKeys = { "freegoodsInd", "orderQty", "simulationOrderQty", "inputTotalQty", "simulationTotalQty", "freegoodsRatio" };
			recordList = detailDB.getRecords( conditionMap, columnList.getFieldKeys(fieldKeys) );

			FreeGoods fgDB = new FreeGoods( ctx.handler );
			List<Map<String, Object>> freegoodsList = null;
			if( recordList != null ) {
				freegoodsList = new java.util.ArrayList<Map<String, Object>>();
				for( Map<String, Object> record : recordList ) {
					fgDB.makeFreegoodsValue( record );
					record.put( "orderQty", record.get("mostFreegoodsQty") );
					if( "Y".equals(record.get("freegoodsInd")) ) {
						freegoodsList.add( record );
					}
				}
			}

			ctx.req.setAttribute( "records", freegoodsList );
			headerMap.put( "parentOrderNumber", headerMap.remove("orderNumber") );
			conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, freegoodsList );
		} else {

			String freegoodsOrderInd = (String) headerMap.get( "freegoodsOrderInd" );
			if( !Order.STATUS_ERROR.equals(status) ) {
				throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("MSG_DPR_ENQUIRYORDER_FREEGOODS_STATUS_ERROR") );
			}
			if( !"Y".equals(freegoodsOrderInd) ) {
				throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("MSG_DPR_ENQUIRYORDER_NOT_FREEGOODS") );
			}
			recordList = detailDB.getRecords( conditionMap, columnList.getFieldKeys() );
			ctx.req.setAttribute( "records", recordList );
			conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		}

		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		ctx.req.setAttribute( "fieldSet", db.getFieldSet(true) );
		ctx.req.setAttribute( "header", headerMap );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );
		ctx.pageConfig.setManageAuth( false );

		return forward( ctx, systemConfig.getJspPath() +"/dpr_order_freegoods_info.jsp" );
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
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
			Map<String, Object> recordMap = recordList.get( 0 );
			if( recordMap != null )
				return (String)recordMap.get( "partyCode" );
		}

		return null;
	}

	@Override
	protected boolean info( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
		String orderKey = ctx.req.getParameter( "orderKey" );
		String orderNumber = ctx.req.getParameter( "orderNumber" );
		if( orderKey == null || orderKey.length() == 0 ) {
			orderKey = headerDB.getOrderKey( orderNumber );
			if( orderKey == null || orderKey.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

		if( Order.STATUS_CREATED.equals(ctx.req.getParameter("status")) )
			executeStatus( ctx );

		conditionMap.put( "orderKey", orderKey );
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		conditionMap.put( com.irt.data.Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );
		if( Country.isFeature(getSavedOrganizationCode(ctx), "useRevOrd"))
			conditionMap.put("useRevOrd", "Y");

		Map<String, Object> headerMap = headerDB.getRecord( conditionMap );
		if( headerMap == null )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		String formatType = ctx.req.getParameter( PARAM_FORMATTYPE );
		if( formatType == null || formatType.length() == 0 )
			formatType = FORMATTYPE_PC;

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrder.DETAIL%LIST."+ formatType.toUpperCase(), getSavedOrganizationCode( ctx ) );
		com.irt.data.cols.ColumnList billingColumnList = getColumnList( ctx, "DPRBilling%LIST" );
		com.irt.data.cols.ColumnList memosColumnList = getColumnList( ctx, "DPRMemos%LIST" );

		ServletUtility.setSort( ctx.req, detailDB, columnList.getSortKeys() );

		conditionMap.put( "formatUOM", formatType.toUpperCase() );

		CondPred.putIsNotEquals(conditionMap, "detailStatus", OrderDetail.STATUS_SAP_DELETED);

		List<Map<String, Object>> detailList = detailDB.getRecords( conditionMap, columnList.getFieldKeys() );

		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, detailList );
		if( conditionKey == null ) conditionKey = ServletUtility.pushTemporaryObject( ctx.req, "condition", conditionMap );
		ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useFreegoods") ) {
			String freegoodsOrderKey = null;
			if( headerMap.containsKey("childOrderNumber") ) {
				freegoodsOrderKey = headerDB.getOrderKey( (String) headerMap.get("childOrderNumber") );
			}

			if( freegoodsOrderKey != null ) {
				com.irt.data.cols.ColumnList fgColumnList = getColumnList( ctx, "DPROrder.DETAIL.FREEGOODS%LIST."+ formatType.toUpperCase() );
				Map<String, Object> fgConditionMap = new java.util.HashMap<String, Object>( conditionMap );
				fgConditionMap.put( "orderKey", freegoodsOrderKey );
				List<Map<String, Object>> fgDetailList = detailDB.getRecords( fgConditionMap, fgColumnList.getFieldKeys() );
				ctx.req.setAttribute( "fgColumnList", fgColumnList );
				ctx.req.setAttribute( "fgDetails", fgDetailList );
			}
		}

		{
			Map<String, Object> remarkRecord = new OrderRemark(ctx.handler).getRecord( OrderRemark.createPrimary((String)headerMap.get("orderKey")), new String[] { "writerUserId", "remark" } );
			if( remarkRecord != null )
				for( Map.Entry<String, Object> entry : remarkRecord.entrySet() )
					headerMap.put( entry.getKey(), entry.getValue() );
		}

		ctx.req.setAttribute( "header", headerMap );
		ctx.req.setAttribute( "fieldSet", headerDB.getFieldSet(false) );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "billingColumnList", billingColumnList );
		ctx.req.setAttribute( "memosColumnList", memosColumnList );
		ctx.req.setAttribute( "details", detailList );


		Map<String, Object> condMap = new java.util.HashMap<String, Object>();
		String returnedOrderNumber = (String) headerMap.get("orderNumber");
		if( returnedOrderNumber != null && returnedOrderNumber.length() > 0 ) {
			condMap.put( "orderNumber", returnedOrderNumber );
			ctx.req.setAttribute( "billings", headerDB.getExtraRecords(condMap, Order.EXTRATYPE_BILLING) );
			ctx.req.setAttribute( "deliveries", headerDB.getExtraRecords(condMap, Order.EXTRATYPE_DELIVERY) );
			ctx.req.setAttribute( "memos", headerDB.getExtraRecords(condMap, Order.EXTRATYPE_MEMOS) );
		}

		String rtype = ctx.req.getParameter( "rtype" );
		if( REQTYPE_REGIST.equals(rtype) ) {
			ctx.pageConfig.setProperty( "rtype", rtype );
		}

		if( "Y".equals(ctx.req.getParameter("create")) ) {
			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_PLACEMENT") );
			ctx.pageConfig.setSubTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_INFO") );

			ctx.req.setAttribute( "path", null );
			setPath( ctx, "jsp.MENU_ORDER", "TITLE_DPR_ORDER_FRM", "TITLE_DPR_ORDER_PLACEMENT" );
		}

		return forward( ctx, systemConfig.getJspPath() +"/dpr_order_info.jsp" );
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		String orderKey = ctx.req.getParameter( "orderKey" );
		String orderNumber = ctx.req.getParameter( "orderNumber" );
		if( orderKey == null || orderKey.length() == 0 ) {
			orderKey = headerDB.getOrderKey( orderNumber );
			if( orderKey == null || orderKey.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

		if( Order.STATUS_CREATED.equals(ctx.req.getParameter("status")) )
			executeStatus( ctx );

		conditionMap.put( "orderKey", orderKey );
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		conditionMap.put( com.irt.data.Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );

		Map<String, Object> recordMap = headerDB.getRecord( conditionMap );
		if( recordMap == null )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		String formatType = ctx.req.getParameter( PARAM_FORMATTYPE );
		if( formatType == null || formatType.length() == 0 )
			formatType = FORMATTYPE_PC;

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrder.DETAIL%LIST."+ formatType.toUpperCase(), getSavedOrganizationCode( ctx ) );

		ServletUtility.setSort( ctx.req, detailDB, columnList.getSortKeys() );

		conditionMap.put( "formatUOM", formatType.toUpperCase() );
		List<Map<String, Object>> detailList = detailDB.getRecords( conditionMap, columnList.getFieldKeys() );

		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, detailList );
		if( conditionKey == null ) conditionKey = ServletUtility.pushTemporaryObject( ctx.req, "condition", conditionMap );
		ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		AbstractFieldSet afs = headerDB.getFieldSet(false);
		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_"+ MODE_DOWNLOAD.toUpperCase()) );

		{
			DataWriter out = createDataWriter( ctx, ctx.msghandler.getMessage("jsp.dpr_order_list.FILE_NAME") );
			com.irt.util.RBMWorkbook.setColumnList( out, columnList );
			try {
				int fieldSpan = 3;

				writeSheetHeaderBlock(out, ctx.msghandler, afs, recordMap, fieldSpan);
				ServletUtility.setSort( ctx.req, detailDB, columnList.getSortKeys() );
				detailDB.write( out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE );
				out.println();
				writeSheetTailBlock(out, ctx.msghandler, afs, recordMap, fieldSpan);

			} catch( SQLException sqlEx ) {
				out.println();
				out.print( sqlEx.getMessage() );
				logger.error( "internal error.", sqlEx );
			} finally {
				out.flush();
				out.close();
			}
		}

		return true;
	}

	protected boolean downHeader( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);
		String organizationCode = getSavedOrganizationCode( ctx );
		conditionMap.put( "organizationCode", organizationCode );

		List<String> optionKeyList = new ArrayList<String>();
		if( com.irt.dpr.Country.isFeature(organizationCode, "useCustomerPONumber") )
			optionKeyList.add( "PO" );
		if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) {
			optionKeyList.add( "OT" );
		}
		optionKeyList.add( ColumnConfigureFile.OPTIONKEY_DELETE_HTML );

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPROrder.HEADER%DOWN", optionKeyList.toArray(new String[optionKeyList.size()]) );

		String filename = ctx.msghandler.getMessage(ctx.pageConfig.getTitle());
		DataWriter out = createDataWriter(ctx, filename);

		try {
			ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
			db.write(out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE);
		} catch( SQLException sqlEx ) {
			out.println();
			out.print(sqlEx.getMessage());
			logger.error("internal Error", sqlEx);
		} finally {
			out.flush();
			out.close();
		}
		return true;
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);
		if( MODE_COND.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.INF");
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.LST");
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_LIST_DETAIL.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.LST");
		else if( MODE_BILLING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.DWN");
		else if( MODE_HEADER_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.DWN");
		else if( MODE_MULTI_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.DWN");
		else if( MODE_FREEGOODS_MANAGE_INPUT.equals(ctx.mode) ||  MODE_FREEGOODS_MANAGE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MAP");
		else if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode( ctx ), "useLogisticsQuery")
				&& (MODE_LOGISTICS_QUERY.equals(ctx.mode) || MODE_LOGISTICS_QUERY_INFO.equals(ctx.mode) || MODE_LOGISTICS_SENDREDIRECT.equals(ctx.mode)) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.LST");
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new Order(ctx.handler, systemConfig);
		ctx.extraObj = new OrderDetail(ctx.handler);

		String messageKey = "TITLE_DPR_ORDERSTATUS_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_ORDER", "jsp.SUBMENU_ORDERSTATUS" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);
		String organizationCode = getSavedOrganizationCode(ctx);
		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));
		conditionMap.put("organizationCode", organizationCode );

		String status = (String)conditionMap.get("status");
		if( status == null || Order.STATUS_CREATED.equals(status) ) {
			Map<String, Object> _condition = new java.util.HashMap<String, Object>( conditionMap );
			if( status == null ) {
				_condition.put( "status", Order.STATUS_CREATED );
			}
			int count = headerDB.getRecordCount(_condition);
			if( count > 0 )
				executeStatusList(ctx, _condition);
		}

		String poOption = null;
		if( com.irt.dpr.Country.isFeature(organizationCode, "useCustomerPONumber") ) {
			poOption = "PO";
		}
		String orderTypeOption = null;
		if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) {
			orderTypeOption = "OT";
		}
		String logisticsQueryOption = null;
		if( com.irt.dpr.Country.isFeature(organizationCode, "useLogisticsQuery") ) {
			orderTypeOption = "LQ";
		}
		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPROrder.HEADER%LIST", poOption, orderTypeOption, logisticsQueryOption );
		com.irt.data.cols.ColumnList billingColumnList = getColumnList(ctx, "DPRBilling.STATUS%LIST", poOption, orderTypeOption, logisticsQueryOption );
		com.irt.data.cols.ColumnList deliveryColumnList = getColumnList(ctx, "DPRDelivery.STATUS%LIST");
		com.irt.data.cols.ColumnList memosColumnList = getColumnList(ctx, "DPRMemos.STATUS%LIST");

		String[] extraFieldKeys = new String[] {"status"};
		if( com.irt.dpr.Country.isFeature(ctx.pageConfig.getProperty("savedOrgCd"), "useRevOrd") ) {
			extraFieldKeys = com.irt.util.Arrays.append(extraFieldKeys
					, new String[] {"reviseStatus", "revHbrdLastDate", "reviseHelpType"});
		}

		conditionMap.put( "freegoodsOrderInd" + com.irt.data.Condition.SUFFIX_IS_NULL_OR, "Y");
		conditionMap.put( "freegoodsOrderInd" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_NOTEQUALS );
		conditionMap.put( "freegoodsOrderInd", "Y" );
		if( Country.isFeature(getSavedOrganizationCode(ctx), "useRevOrd"))
			conditionMap.put("useRevOrd", "Y");

		ServletUtility.setSort(ctx.req, headerDB, columnList.getSortKeys());
		List<Map<String, Object>> recordList = headerDB.getRecords(conditionMap, columnList.getFieldKeys(extraFieldKeys));

		if( recordList != null && recordList.size() > 0 ) {
			for( Map<String, Object> obj : recordList ) {
				Map<String, Object> recordMap = obj;

				if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useFreegoods") ) {
					String orderKey = Record.extractString( recordMap, "orderKey" );
					Map<String, Object> condMap = new java.util.HashMap<String, Object>();
					condMap.put( "parentOrderKey", orderKey );
					condMap.put( "freegoodsOrderInd", "Y" );
					List<Map<String, Object>> freegoodsOrderList = headerDB.getRecords( condMap );
					if( freegoodsOrderList != null && freegoodsOrderList.size() > 0 )
						recordMap.put("freegoodsOrderList", freegoodsOrderList);
				}

				if( Order.STATUS_CREATED.equals(recordMap.get("status")) ) {
					Map<String, Object> condMap = new java.util.HashMap<String, Object>();
					String orderNumber = Record.extractString(recordMap, "orderNumber");
					if( orderNumber != null && orderNumber.length() > 0 ) {
						condMap.put("orderNumber", orderNumber);
						List<Map<String, Object>> billingList = headerDB.getExtraRecords(condMap, Order.EXTRATYPE_BILLING);
						List<Map<String, Object>> deliveryList = headerDB.getExtraRecords(condMap, Order.EXTRATYPE_DELIVERY);

						Map<String, Object> creditCond = new java.util.HashMap<String, Object>(condMap);
						creditCond.put("extraValue1", com.irt.dpr.Memos.CREDIT_MEMEOS);
						List<Map<String, Object>> creditMemosList = headerDB.getExtraRecords(creditCond, Order.EXTRATYPE_MEMOS);

						Map<String, Object> debitCond = new java.util.HashMap<String, Object>(condMap);
						debitCond.put("extraValue1", com.irt.dpr.Memos.DEBIT_MEMEOS);
						List<Map<String, Object>> debitMemosList = headerDB.getExtraRecords(debitCond, Order.EXTRATYPE_MEMOS);

						if( billingList != null && billingList.size() > 0 )
							recordMap.put("billingList", billingList);
						if( deliveryList != null && deliveryList.size() > 0 )
							recordMap.put("deliveryList", deliveryList);
						if( creditMemosList != null && creditMemosList.size() > 0 )
							recordMap.put("creditMemosList", creditMemosList);
						if( debitMemosList != null && debitMemosList.size() > 0 )
							recordMap.put("debitMemosList", debitMemosList);
					}
				}
			}
		}

		if( com.irt.dpr.Country.isFeature(organizationCode, "useDetailCondition") ) {
			Map<String, Object> _conditionMap = new java.util.HashMap<String, Object>();
			_conditionMap.put( "organizationCode", conditionMap.get("organizationCode") );
			_conditionMap.put( "distributionChannelCode", conditionMap.get("distributionChannelCode") );
			_conditionMap.put( "officeCode", conditionMap.get("officeCode") );
			_conditionMap.put( "groupCode", conditionMap.get("groupCode") );
			_conditionMap.put( "countryCode", getUserCountryCode(ctx) );
			setAttributePartyMaster( ctx, _conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP );

			if( _conditionMap.get("distributionChannelCode") == null ) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>) ctx.req.getAttribute( "distributionChannels" );
				_conditionMap.put( "distributionChannelCode", Record.extractObjectArray(distributionChannels, "distributionChannelCode") );
			}

			setAttributePartner( ctx, _conditionMap );

			conditionMap.putAll( _conditionMap );
		}
		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		if( recordList != null ) {
			idxVars[1] = idxVars[2] = idxVars[3] = recordList.size();
		}
		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		ctx.req.setAttribute("columnList", columnList);
		ctx.req.setAttribute("billingColumnList", billingColumnList);
		ctx.req.setAttribute("deliveryColumnList", deliveryColumnList);
		ctx.req.setAttribute("creditMemosColumnList", memosColumnList);
		ctx.req.setAttribute("debitMemosColumnList", memosColumnList);
		ctx.req.setAttribute("condition", conditionMap);
		ctx.req.setAttribute("records", recordList);

		if( conditionMap.containsKey("recentCount") ) {
			int recentCount = (int) conditionMap.get( "recentCount" );
			if( recentCount > 0 ) {
				String subTitle = ctx.msghandler.getMessage( "TITLE_DPR_ORDERSTATUS_" + ctx.mode.toUpperCase() );
				subTitle += " " + ctx.msghandler.getMessage( "MSG_LAST_LIST", String.valueOf(recentCount) );
				ctx.pageConfig.setSubTitle( subTitle );
			}
		}

		return forward(ctx, systemConfig.getJspPath() + "/dpr_order_list.jsp");
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		return true;
	}

	protected boolean listDetail( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		String orderKey = ctx.req.getParameter("orderKey");
		String orderNumber = ctx.req.getParameter("orderNumber");
		if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		if( orderNumber == null || orderNumber.length() == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		Map<String, Object> primaryMap = new java.util.HashMap<String, Object>();
		primaryMap.put("orderKey", orderKey);
		primaryMap.put("orderNumber", orderNumber);
		primaryMap.put("status", Order.STATUS_CREATED);
		primaryMap.put("displayLanguage", getDisplayLanguage(ctx));
		if( !ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin() ) {
			Condition.putConditionValueOnly(primaryMap, "authUniqId", ctx.sessionMng.getUniqId());
			Condition.putConditionValueOnly(primaryMap, "authPartyValue", "Y");
		}

		Map<String, Object> headerMap = headerDB.getRecord(primaryMap);

		/* Detail */
		String formatType = ctx.req.getParameter(PARAM_FORMATTYPE);
		if( formatType == null || formatType.length() == 0 )
			formatType = FORMATTYPE_PC;

		List<String> optionKeyList = new ArrayList<String>();
		String columnListName = "DPROrder.DETAIL%LIST";
		if( Country.isFeature(getSavedOrganizationCode(ctx), "usePrintList") )
			columnListName = "DPROrder.DETAIL%PRINTLIST.CN";
		if( Country.isFeature(getSavedOrganizationCode(ctx), "useCnf") )
			optionKeyList.add("useCnf");

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, columnListName + "." + formatType.toUpperCase(), optionKeyList.toArray(new String[0]));
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>(primaryMap);
		conditionMap.put(com.irt.data.Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER);
		conditionMap.remove("status");

		ServletUtility.setSort(ctx.req, detailDB, columnList.getSortKeys());
		detailDB.setSort("lineNumber");

		conditionMap.put("formatUOM", formatType.toUpperCase());
		List<Map<String, Object>> detailList = detailDB.getRecords(conditionMap, columnList.getFieldKeys());

		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, detailList);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		ctx.req.setAttribute("header", headerMap);
		ctx.req.setAttribute("details", detailList);
		ctx.req.setAttribute("condition", conditionMap);
		ctx.req.setAttribute("columnList", columnList);

		return forward(ctx, systemConfig.getJspPath() + "/dpr_order_formatted_printing.jsp");
	}

	protected boolean logisticsQuery( Context ctx ) throws IOException, ServletException {
		LogisticsTracking logisticsTrack = new LogisticsTracking( ctx.handler );
		String deliveryNumber = ctx.req.getParameter( "deliveryNumber" );
		if( deliveryNumber == null || deliveryNumber.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		try {
			Map<String, Object> logisticsMap = logisticsTrack.executeQuery( deliveryNumber );
			Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
			recordMap.put( "deliveryNumber", deliveryNumber );
			boolean success = (Boolean)logisticsMap.get( "success" );
			recordMap.put( "success", success );
			if( !success ) {
				recordMap.put( "errorMessage", logisticsMap.get("errorMessage") );
			} else {
				recordMap.put( "3plRequestUrl", LogisticsTracking.userRequestUrl + deliveryNumber );
			}
			ctx.req.setAttribute( "record", recordMap );
		} catch( HttpClientException httpEx ) {
			throw new ServletModelException( ServletModelException.INTERNAL_ERROR, httpEx.getMessage() );
		} catch( IOException ioEx ) {
			throw new ServletModelException( ServletModelException.INTERNAL_ERROR, ioEx.getMessage() );
		}
		return forward(ctx, systemConfig.getJspPath() + "/dpr_logisticstracking_info.jsp");
	}

	protected boolean logisticsQueryInfo( Context ctx ) throws IOException, ServletException {
		String deliveryNumber = ctx.req.getParameter( "deliveryNumber" );
		if( deliveryNumber == null || deliveryNumber.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
		recordMap.put( "deliveryNumber", deliveryNumber );
		ctx.req.setAttribute( "record", recordMap );
		return forward(ctx, systemConfig.getJspPath() + "/dpr_logisticstracking_info.jsp");
	}

	protected boolean logisticsSendRedirect( Context ctx ) throws ServletModelException, IOException {
		String deliveryNumber = ctx.req.getParameter( "deliveryNumber" );
		if( deliveryNumber == null || deliveryNumber.length() == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		String redirectURL = LogisticsTracking.userRequestUrl + deliveryNumber;
		return sendRedirect( ctx, redirectURL );
	}

	protected boolean multiDownload( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		String[] orderKeys = ctx.req.getParameterValues("orderKey");
		if( orderKeys == null || orderKeys.length == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		String[] orderNumbers = ctx.req.getParameterValues("orderNumber");
		if( orderNumbers == null || orderNumbers.length == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		String[] statuses = ctx.req.getParameterValues("status");
		if( statuses == null || statuses.length == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		String dntype = ctx.req.getParameter("dntype");
		if( dntype == null )
			dntype = "unsync";

		/* no need to do again
		Map<String, Object> conditionMap = createConditionMap(ctx);
		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));

		if( Order.STATUS_CREATED.equals(conditionMap.get("status")) ) {
			int count = headerDB.getRecordCount(conditionMap);
			if( count > 0 )
				executeStatusList(ctx, conditionMap);
		}
		 */

		String formatType = ctx.req.getParameter(PARAM_FORMATTYPE);
		if( formatType == null || formatType.length() == 0 )
			formatType = FORMATTYPE_PC;

		com.irt.data.cols.ColumnList detail_columnList = getColumnList(ctx, "DPROrder.DETAIL%LIST." + formatType.toUpperCase(), ColumnConfigureFile.OPTIONKEY_DELETE_HTML);

		String fileType = getFileType(ctx);
		org.apache.poi.ss.usermodel.Workbook workbook = com.irt.util.RBMWorkbook.createWorkbook(fileType);
		String filename =  ctx.msghandler.getMessage( "jsp.dpr_order_list.FILE_NAME" ) + "_" + com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone());
		filename = java.net.URLEncoder.encode( filename, "UTF-8" );
		String cookieOption = systemConfig.getCookieOption();
		cookieOption = cookieOption != null ? cookieOption : "";
		cookieOption = cookieOption.replaceAll( "HttpOnly;", "" );
		ctx.res.setContentType(com.irt.util.RBMWorkbook.getResponseContentType(fileType));
		ctx.res.setHeader("Content-Disposition", "attachment; filename=" + filename + "." + com.irt.util.RBMWorkbook.getFileExtension(fileType));
		ctx.res.setHeader( "Set-Cookie", "fileDownload=true; path=/;" + cookieOption );
		java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(ctx.res.getOutputStream());
		com.irt.util.SSDataWriter out = new com.irt.util.SSDataWriter(bos, workbook, true);
		out.setReuseCellStyles(true);

		AbstractFieldSet afs = headerDB.getFieldSet(false);
		int fieldSpan = 3;
		try {
			for( int i = 0; i < orderKeys.length; i++ ) {
				Map<String, Object> condMap = new java.util.HashMap<String, Object>();
				condMap.put("orderKey", orderKeys[i]);
				condMap.put("formatUOM", formatType.toUpperCase());
				condMap.put("displayLanguage", getDisplayLanguage(ctx));
				condMap.put(com.irt.data.Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER);

				if( orderNumbers[i] == null || orderNumbers[i].length() == 0 ) {
					continue;
				}

				// if( RBMSystem.getSystemEnvBool("DPR", "DPREnquiryOrder;useMultiDownSync", true) ) {
				if( "sync".equals(dntype) ) {
					if( orderNumbers[i] != null && orderNumbers[i].length() > 0 ) {
						condMap.put("orderNumber", orderNumbers[i]);
						executeStatus(ctx, condMap);
					}
				}

				Map<String, Object> headerMap = headerDB.getRecord(condMap);
				String simulationKey = (String)headerMap.get("simulationKey");

				String sheetName = orderNumbers[i];
				if( sheetName == null || sheetName.length() == 0 ) {
					sheetName = ( simulationKey == null || simulationKey.length() == 0 ) ? orderKeys[i] : simulationKey;
				}

				out.setSheet(workbook.createSheet(sheetName));

				writeSheetHeaderBlock(out, ctx.msghandler, afs, headerMap, fieldSpan);
				ServletUtility.setSort(ctx.req, detailDB, detail_columnList.getSortKeys());
				detailDB.write( out, condMap, detail_columnList, QueryableManager.OPT_WRITING_TITLE );
				out.println();
				writeSheetTailBlock(out, ctx.msghandler, afs, headerMap, fieldSpan);
			}
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			logger.error(sqlEx.getMessage(), sqlEx);
			out.println();
			out.print(sqlEx.getMessage());
		} finally {
			out.flush();
			out.close();
			if( bos != null )
				bos.close();
		}

		return true;
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

	protected boolean writeSheetHeaderBlock( DataWriter out, com.irt.util.MessageHandler msghandler, AbstractFieldSet afs,
			Map<String, Object> recordMap, int fieldSpan ) throws IOException {
		String organizationCode = (String)recordMap.get("organizationCode");
		char originalDataType = out.getDataType();

		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "orderDate"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "orderDate"), fieldSpan, fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "organizationCode"), com.irt.util.SS
				.getFieldDataString(msghandler, afs, recordMap, "organizationCode", "[$f{pure(organizationCode)}] ${organizationName}"), fieldSpan,
				fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "deliveryNumber"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "deliveryNumber"), fieldSpan, fieldSpan, true);

		if( !com.irt.dpr.Country.isFeature(organizationCode, "useInputRDD") ) {
			com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "inDateDefault"),
					com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "inDateDefault"), fieldSpan, fieldSpan);
		} else {
			com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "inDate"),
					com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "inDate"), fieldSpan, fieldSpan);
		}
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "inDateSimulation"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "inDateSimulation"), fieldSpan, fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "goodsIssueDate"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "goodsIssueDate"), fieldSpan, fieldSpan, true);

		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "soldPartyCode"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "soldPartyCode", "[$f{pure(soldPartyCode)}] ${soldPartyName}"),
				fieldSpan, fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "orderNumber"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "orderNumber"), fieldSpan, fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "creditReleaseDate"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "creditReleaseDate"), fieldSpan, fieldSpan, true);

		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "shipPartyCode"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "shipPartyCode", "[$f{pure(shipPartyCode)}] ${shipPartyName}"),
				fieldSpan, fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "status"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "status"), fieldSpan, fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "orderStatus"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "orderStatus", "${DPR_ORDER_ORDERSTATUS_@orderStatus}"), fieldSpan,
				fieldSpan, true);

		if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) {
			com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "orderType"),
					com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "orderType", "${DPR_ORDER_ORDERTYPE_@orderType}"), fieldSpan, fieldSpan);
			com.irt.util.SS.printFieldSet(out, "", "", fieldSpan, fieldSpan);
			com.irt.util.SS.printFieldSet(out, "", "", fieldSpan, fieldSpan, true);
		}

		out.println();

		out.setDataType(originalDataType);

		return true;
	}

	protected boolean writeSheetTailBlock( DataWriter out, com.irt.util.MessageHandler msghandler, AbstractFieldSet afs,
			Map<String, Object> recordMap, int fieldSpan ) throws IOException {
		char originalDataType = out.getDataType();

		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "orderValue"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "orderValue"), fieldSpan, fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "confirmedOrderValue"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "confirmedOrderValue"), fieldSpan, fieldSpan, true);

		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "orderTax"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "orderTax"), fieldSpan, fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "confirmedOrderTax"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "confirmedOrderTax"), fieldSpan, fieldSpan, true);

		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "orderDiscount"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "orderDiscount"), fieldSpan, fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "confirmedOrderDiscount"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "confirmedOrderDiscount"), fieldSpan, fieldSpan, true);

		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "orderTotal"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "orderTotal"), fieldSpan, fieldSpan);
		com.irt.util.SS.printFieldSet(out, com.irt.util.SS.getFieldHeaderString(msghandler, afs, "confirmedOrderTotal"),
				com.irt.util.SS.getFieldDataString(msghandler, afs, recordMap, "confirmedOrderTotal"), fieldSpan, fieldSpan, true);

		out.setDataType(originalDataType);
		return true;
	}
}
