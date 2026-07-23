/*
 *	File Name:	DPROrderRemark.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		dpr_order_remark_input.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2	신규 UI/UX 적용
 *	jbaek		2019/07/30		2.2.1	displayLanguage조건 추가.
 *	lsinji		2009/01/12		2.2.0	create
 *
**/

import com.irt.dpr.Order;
import com.irt.dpr.OrderRemark;
import com.irt.data.DataException;
import com.irt.html.HtmlPage;
import com.irt.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 */
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPROrderRemark"})
public class DPROrderRemark extends DPRServletModel {
	private final static String MODE_LINK				= "link";
	private final static String MODE_WICOCONRIM			= "wcf";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		return new ParameterMap(ctx.req, true);
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info( Context ctx, boolean inputting ) throws IOException, ServletException, SQLException {
		OrderRemark db = (OrderRemark)ctx.db;

		Map<String, Object> primaryMap = createConditionMap( ctx );
		if( !primaryMap.containsKey("orderKey") )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		primaryMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		Map<String, Object> recordMap = db.getRecord( primaryMap );
		ctx.req.setAttribute( "record", recordMap );

		if( inputting ) {
			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INPUT );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );

			return registInput( ctx );
		}

		ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_order_remark_input.jsp" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_MODIFYINPUT );

		if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.RMK.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new OrderRemark( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_REMARK_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean registInput( Context ctx ) throws IOException, ServletException, SQLException {
		if( ctx.req.getAttribute("record") == null ) {
			Map conditionMap = createConditionMap(ctx);
			conditionMap.put("displayLanguage", getDisplayLanguage(ctx));
			Map<String, Object> recordMap = new Order(ctx.handler).getRecord( conditionMap
					, new String[] {"orderNumber", "orderDate", "inDateConfirm", "organizationCode", "organizationName", "soldPartyCode", "soldPartyName", "shipPartyCode", "shipPartyName"} );

			ctx.req.setAttribute( "fieldSet", ((OrderRemark)ctx.db).getFieldSet(true) );
			ctx.req.setAttribute( "record", recordMap );
		}

		return forward( ctx, systemConfig.getJspPath() + "/dpr_order_remark_input.jsp" );
	}

	@Override
	protected boolean update( Context ctx, boolean inserting ) throws IOException, ServletException, SQLException {
		OrderRemark db = new OrderRemark( ctx.handler );

		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		if( !recordMap.containsKey("orderKey") )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		recordMap.put( "displayLanguage", getDisplayLanguage(ctx) );

		String status = null;
		String orderNumber = null;
		{
			Map<String, Object> orderMap =
				new Order(ctx.handler).getRecord( Order.createPrimary( (String)recordMap.get("orderKey") ), new String[] { "orderNumber", "status" } );

			if( recordMap.containsKey("orderNumber") )
				orderNumber = (String)orderMap.get( "orderNumber" );
			status = (String)orderMap.get( "status" );
		}
	//	if( status == null || Order.STATUS_CREATED.equals(status) )
	//		throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		recordMap.put( "writeUserId", ctx.sessionMng.getUniqId() );
		recordMap.put( "orderNumber", orderNumber );

		try {
			if( !db.update( recordMap ) )
				throw new DataException( DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE));

			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );

			ctx.pageConfig.setInputStatus( HtmlPage.INPUTSTATUS_INFORMATION );
			ctx.req.setAttribute( "fieldSet", db.getFieldSet(false) );
			ctx.req.setAttribute( "record", db.getRecord(recordMap) );

			return forward( ctx, systemConfig.getJspPath() + "/dpr_order_remark_input.jsp" );
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

		return registInput( ctx );
	}
}
