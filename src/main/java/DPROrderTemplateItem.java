/*
 *	File Name:	DPROrderTemplateItem.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	keehe		2008/09/26		2.2.0	create
 *
**/

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.dpr.TemplateItem;
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
@javax.servlet.annotation.WebServlet(urlPatterns={"/servlet/DPROrderTemplateItem"})
public class DPROrderTemplateItem extends DPRServletModel {

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletException, SQLException {
		ParameterMap conditionMap = new ParameterMap( ctx.req, true );

		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );

		conditionMap.put( "templateKey", ctx.req.getParameter("templateKey"));

/*  Data에 대한 condition를 Map으로 구성 한다. */
/*
		conditionMap.put( "itemCode", );
		conditionMap.put( "imageStatus", );
		conditionMap.put( "imageExisting", );
*/
		return conditionMap;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected boolean info(Context ctx) throws IOException, ServletException, SQLException {
		TemplateItem db = (TemplateItem) ctx.db;

		String templateKey = ctx.req.getParameter("templateKey");
		String itemCode = ctx.req.getParameter("itemCode");
		if (templateKey == null || templateKey.length() == 0)
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		Map<String, Object> primaryMap = TemplateItem.createPrimary(templateKey, itemCode);
		Map<String, Object> recordMap = db.getRecord(primaryMap);
		if (recordMap == null)
			throw new ServletModelException(ServletModelException.NO_RECORD_FOUND);
		ctx.req.setAttribute("record", recordMap);

		// auth checking
		/*
		 * 다른 servlet에서 이 로직으로 사용. boolean authValue = getUserAuthParty( ctx,
		 * null ); if( ctx.sessionMng.isSystemAdmin() ||
		 * ctx.sessionMng.isPartyAdmin() ) ctx.pageConfig.setManageAuth(
		 * authValue ); else if( authValue ) ctx.pageConfig.setManageAuth(
		 * ctx.sessionMng.isAuthorized("DPR", "DPROrderTemplateItem.MNG") );
		 */
		if (ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin())
			ctx.pageConfig.setManageAuth(true);
		else
			ctx.pageConfig.setManageAuth(false);

		// 임시
		ctx.pageConfig.setManageAuth(true);

		ctx.pageConfig.setInputStatus(HtmlPage.INPUTSTATUS_INFORMATION);
		ctx.req.setAttribute("fieldSet", db.getFieldSet(true));

		return forward(ctx, systemConfig.getJspPath() + "/dpr_itemimage_input.jsp");
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );
		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_LIST );

		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrderTemplateItem.INF" );
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrderTemplateItem.LST" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrderTemplateItem.MNG" );
		else if( MODE_MULTIMODIFY.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrderTemplateItem.LST" );

		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new TemplateItem( ctx.handler );
		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_TEMPLATE_DTL_"+ ctx.mode.toUpperCase()) );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		TemplateItem db = (TemplateItem)ctx.db;
		// Condition Map
		Map<String, Object> conditionMap = createConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrderTemplateItem%LIST" );

		// template info
		com.irt.dpr.Template templateDB = new com.irt.dpr.Template( ctx.handler );
		Map<String, Object> templateInfoMap = templateDB.getRecord( com.irt.dpr.Template.createPrimary( Record.extractString(conditionMap, "templateKey") ) );
		ctx.req.setAttribute( "templateInfo", templateInfoMap );

		int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, db, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = db.getRecords( conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1] );
		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );
		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() ) ctx.pageConfig.setManageAuth( true );
		ctx.pageConfig.setManageAuth( true );
		return forward( ctx, systemConfig.getJspPath() + "/dpr_ordertemplate_item_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPROrderTemplateItem.LST") ) return false;
			conditionMap = createConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((TemplateItem)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}

	@Override
	protected boolean remove(Context ctx) throws IOException, ServletException, SQLException {
		TemplateItem db = (TemplateItem) ctx.db;
		String templateKey = ctx.req.getParameter("templateKey");
		String lineNumber[] = ctx.req.getParameterValues("lineNumber");
		if (ctx.pageConfig.getBackURL() == null)
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if (templateKey == null || templateKey.length() == 0)
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if (lineNumber == null || lineNumber.length == 0)
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		// 레코드 삭제
		int count = 0;
		Map<String, Object> primaryMap = new java.util.HashMap<String, Object>();
		primaryMap.put("templateKey", templateKey);
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		for (int i = 0; i < lineNumber.length; i++) {
			try {
				primaryMap.put("lineNumber", lineNumber[i]);
				if (db.delete(primaryMap)) {
					count++;
					ctx.handler.commit();
				}
			} catch (DataException dataEx) {
				ctx.handler.rollback();
				errorList.add(createErrorMap(db.getName(templateKey, lineNumber[i]), dataEx));
			}
		}

		// forward & sendRedirect
		if (errorList.size() > 0) {
			ctx.req.setAttribute("errors", errorList);
			return forward(ctx, systemConfig.getJspPath() + "/error.jsp");
		} else {
			ctx.pageConfig.setMessage(
					ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", new String[] { String.valueOf(count) }));
			return sendRedirect(ctx,
					HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)));
		}
	}

	@Override
	protected boolean multiUpdate(Context ctx, boolean inputting) throws IOException, ServletException, SQLException {
		TemplateItem db = (TemplateItem) ctx.db;

		if (ctx.pageConfig.getBackURL() == null)
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		// 레코드 읽기
		ParameterMap recordMap = new ParameterMap(ctx.req);
		List<Map<String, Object>> values = recordMap.extractGroupList("value");
		ctx.req.setAttribute("condition", recordMap);

		try {
			for (Iterator<Map<String, Object>> iterator = values.iterator(); iterator.hasNext();) {
				Map<String, Object> map = iterator.next();
				map.put("templateKey", map.get("templateKey"));
				map.put("lineNumber", map.get("lineNumber"));
				map.put("uom", map.get("uom"));
				map.put("orderQty", map.get("orderQty"));
				map.put("updateUserId", ctx.sessionMng.getUniqId());
				if (!db.modify(map))
					throw new DataException(DataException.ERR_NO_RECORD_UPDATE,
							ctx.msghandler.getMessage(DataException.ERR_NO_RECORD_UPDATE));
			}
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS"));
		} catch (DataException dataEx) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(dataEx.getMessage());
		} catch (SQLException sqlEx) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(sqlEx.getMessage());
		}

		return sendRedirect(ctx,
				HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)));
	}
}
