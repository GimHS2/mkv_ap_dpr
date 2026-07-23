/*
 *	File Name:	DPROrderServletModel.java
 *	Version:	2.2.4
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.4	신규 UI/UX 적용
 *	hankalam	2019/07/31		2.2.3	Freegoods 기능 추가
 *	jbaek		2018/03/31		2.2.2	executePlaceOrderProcess(): inDate가 orderDate보다 작은지 체크
 *										executeStatus(): conditionMap 파라미터 추가
 *	jbaek		2011/11/30		2.2.1	OrderInputAuth Condition 체크: setTradePartner()에 OrderInputAuth Condition 적용
 *	lsinji		2009/06/30		2.2.0	create
 *
**/

import com.irt.data.*;
import com.irt.dpr.Order;
import com.irt.dpr.OrderDetail;
import com.irt.html.HtmlPage;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.ServletException;

/**
 *
 */
public abstract class DPROrderServletModel extends DPRServletModel {
	protected final static String PARAM_TYPE					= "type";
	protected final static String TYPE_SOLD						= "sold";
	protected final static String TYPE_SHIP						= "ship";

	protected final static String PARAM_FORMATTYPE				= "ftype";
	protected final static String FORMATTYPE_PC					= "PC";
	protected final static String FORMATTYPE_DZ					= "DZ";

	protected final static String DOCUMENTNUMBER_TYPE_ORDER		= "O";
	protected final static String DOCUMENTNUMBER_TYPE_CUSTOMER	= "P";

	protected boolean executeBilling( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = new Order( ctx.handler, systemConfig );

		String updateUserId = ctx.sessionMng.getUniqId();
		String organizationCode = getSavedOrganizationCode( ctx );

		Map<String, Object> conditionMap = new java.util.HashMap();
		conditionMap.put( "orderKey", ctx.req.getParameter("orderKey") );
		conditionMap.put( "billingNumber", ctx.req.getParameter("billingNumber") );
		conditionMap.put( "soldPartyCode", ctx.req.getParameter("soldPartyCode") );
		conditionMap.put( "updateUserId", updateUserId );

		try {
			db.executeEnquiry( com.irt.dpr.Order.ORDER_IF_BILLING, conditionMap );
		} catch ( DataException dataEx ) {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage(dataEx.getErrorKey()) );
		}

		return true;
	}

	protected boolean executePlaceOrderProcess( Context ctx, String orderKey, String processType ) throws DataException, ServletException, SQLException {
		Order db = new Order( ctx.handler, systemConfig );

		if( Order.ORDER_IF_CREATION.equals(processType) || Order.ORDER_IF_SIMULATION.equals(processType) ) {
			if( !db.checkInDateAtLeast(orderKey) ) {
				throw new ServletModelException("ERR_INVALID_INDATE_ABOUT_ORDERDATE");
			}

			if( Order.ORDER_IF_CREATION.equals(processType) ) {
				String freegoodsOrderInd = (String) db.getFieldValue( Order.createPrimary(orderKey), "freegoodsOrderInd" );

				Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();
				conditionMap.put( "orderKey", orderKey );
				conditionMap.put( "headerStatus", Order.STATUS_SIMULATED );
				if( !"Y".equals(freegoodsOrderInd) ) {
					conditionMap.put( "orderQtySimulation" + Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_NONE );
					conditionMap.put( "orderQtySimulation" + Condition.SUFFIX_MIN_VALUE, "0" );
				} else {
					conditionMap.put( "orderQty" + Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_NONE );
					conditionMap.put( "orderQty" + Condition.SUFFIX_MIN_VALUE, "0" );
				}


				int count = (new OrderDetail(ctx.handler)).getRecordCount( conditionMap );
				if( count <= 0 )
					throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_ORD_CREATION_NO_DATA_CONTINUE") );
			}

			Map<String, Object> map = new java.util.HashMap<String, Object>();
			map.put( "orderKey", orderKey );
			map.put( "countryCode", getUserCountryCode(ctx) );
			map.put( "updateUserId", ctx.sessionMng.getUniqId() );

			db.executeEnquiry( processType, map );
			return true;
		}

		return false;
	}

	protected boolean executeStatusList( Context ctx, Map<String, Object> conditionMap ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;
		String organizationCode = getSavedOrganizationCode( ctx );
		String updateUserId = ctx.sessionMng.getUniqId();

		Map<String, Object> reqConditionMap = new java.util.HashMap<String, Object> ( conditionMap );
		reqConditionMap.put( "countryCode", getUserCountryCode(ctx) );
		reqConditionMap.put( "organizationCode", organizationCode );
		reqConditionMap.put( "updateUserId", updateUserId );

		try {
			db.executeEnquiry( com.irt.dpr.Order.ORDER_IF_STATUSLIST, reqConditionMap );
		} catch( DataException dataEx ) {
			ctx.pageConfig.setMessage( dataEx.getMessage() );
		}

		return true;
	}

	protected boolean executeStatus( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;

		String orderNumber = ctx.req.getParameter( "orderNumber" );
		String orderKey = ctx.req.getParameter( "orderKey" );
		if( (orderNumber == null || orderNumber.length() == 0) && (orderKey == null || orderKey.length() == 0) )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();
		conditionMap.put( "orderNumber", orderNumber );
		conditionMap.put( "orderKey", orderKey );
		conditionMap.put( "status", Order.STATUS_CREATED );

		boolean isValid = ( db.getRecordCount(conditionMap) > 0 );

		if( isValid ) {
			try {
				String organizationCode = getSavedOrganizationCode( ctx );
				String updateUserId = ctx.sessionMng.getUniqId();

				Map<String, Object> map = new java.util.HashMap();
				map.put( "orderNumber", orderNumber );
				map.put( "orderKey", orderKey );
				map.put( "countryCode", getUserCountryCode(ctx) );
				map.put( "updateUserId", updateUserId );

				db.executeEnquiry( com.irt.dpr.Order.ORDER_IF_STATUS, map );
			} catch( DataException dataEx ) {
				ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
				ctx.pageConfig.setMessage( dataEx.getMessage() );
			}
		} else
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("ERR_CANNOT_EXECUTE_STATUS") );

		return true;
	}

	protected boolean executeStatus( Context ctx, Map<String, Object> conditionMap ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;

		Map<String, Object> reqConditionMap = new java.util.HashMap<String, Object> (conditionMap);
		reqConditionMap.put( "status", Order.STATUS_CREATED );

		String orderNumber = (String)reqConditionMap.get( "orderNumber" );
		String orderKey = (String)reqConditionMap.get( "orderKey" );
		if( (orderNumber == null || orderNumber.length() == 0) && (orderKey == null || orderKey.length() == 0) )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );


		boolean isValid = ( db.getRecordCount(reqConditionMap) > 0 );

		if( isValid ) {
			try {
				String organizationCode = getSavedOrganizationCode( ctx );
				String updateUserId = ctx.sessionMng.getUniqId();

				Map<String, Object> map = new java.util.HashMap();
				map.put( "orderNumber", orderNumber );
				map.put( "orderKey", orderKey );
				map.put( "countryCode", getUserCountryCode(ctx) );
				map.put( "updateUserId", updateUserId );

				db.executeEnquiry( com.irt.dpr.Order.ORDER_IF_STATUS, map );
			} catch( DataException dataEx ) {
				ctx.pageConfig.setMessage( dataEx.getMessage() );
			}
		} else
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("ERR_CANNOT_EXECUTE_STATUS") );

		return true;
	}

	protected boolean setTradePartner( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap();

		String type = ctx.req.getParameter( PARAM_TYPE );
		String countryCode = getUserCountryCode( ctx );
		setDefaultParameter( ctx, conditionMap );
		if( type == null || type.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( TYPE_SOLD.equals(type) ) {
			conditionMap.put( "partyNotOrderable" + Condition.SUFFIX_IS_NULL_OR, "Y" );
			conditionMap.put( "partyNotOrderable", "N" );

			setAttributePartner( ctx, conditionMap, PARTNER_SOLD );
		} else if( TYPE_SHIP.equals(type) ) {
			String soldPartyCode = ctx.req.getParameter( "soldPartyCode" );
			if( soldPartyCode == null || soldPartyCode.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			conditionMap.put( "soldPartyCode", soldPartyCode );
			setAttributePartner( ctx, conditionMap, PARTNER_SHIP );
		} else
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		ctx.pageConfig.setProperty( "type", type );

		return forward( ctx, systemConfig.getJspPath() +"/dpr_order_tpset.jsp" );
	}
}
