/*
 *	File Name:	DPRBillingDetail.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/12/31		2.2.1	wm call에 사용하지않는 employeeID 파라미터 삭제
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

import com.irt.dpr.Billing;
import com.irt.dpr.BillingDetail;
import com.irt.dpr.tools.OrderCanonicalProcess;
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

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPRBillingDetail"})
public class DPRBillingDetail extends DPRServletModel {
	private final static String REQTYPE				= "rtype";
	private final static String REQTYPE_SAP			= "S";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap(ctx.req, true);

		if( !conditionMap.containsKey("billingNumber") )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		conditionMap.put( "displayCountryCode", getUserCountryCode(ctx) );
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );

		return conditionMap;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.INF" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new BillingDetail( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_BILLING_DETAIL_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		BillingDetail db = (BillingDetail)ctx.db;

		Map<String, Object> conditionMap = createConditionMap( ctx );

		String rtype = ctx.req.getParameter( REQTYPE );
		if( REQTYPE_SAP.equals(rtype) ) {
			try {
				OrderCanonicalProcess ocp = new OrderCanonicalProcess( ctx.handler, systemConfig, OrderCanonicalProcess.ORDER_IF_BILLING );
				String updateUserId = ctx.sessionMng.getUniqId();
				String organizationCode = getSavedOrganizationCode( ctx );
				String billingNumber = (String)conditionMap.get( "billingNumber" );
				String orderNumber = (String)conditionMap.get( "orderNumber" );

				Map<String, Object> requestMap = new java.util.HashMap<String, Object> ();
				requestMap.put( "billingNumber", conditionMap.get("billingNumber") );
				requestMap.put( "orderNumber", conditionMap.get("orderNumber") );
				requestMap.put( "updateUserId", updateUserId );
				requestMap.put( "soldPartyCode", new Billing(ctx.handler).getSoldPartyWithBilling(billingNumber, orderNumber) );
//				requestMap.put( "employeeID", new com.irt.dpr.UserEmployee(ctx.handler).getEmployeeId(updateUserId, organizationCode) );
				ocp.setParameter( requestMap );
				ocp.execute();
			} catch ( com.irt.dpr.OrderProcessException proEx ) {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage(proEx.getErrorKey()) );
			}
		}

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRBilling.DETAIL%LIST" );
		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );

		List recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		/* Header, Summary */
		ctx.req.setAttribute( "header", new com.irt.dpr.Billing(ctx.handler).getRecord(conditionMap) );
		ctx.req.setAttribute( "summary", db.getBillingSummary(conditionMap) );
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_billingdetail_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPROrder.INF") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((BillingDetail)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}
}
