/*
 *	File Name:	CSTMenu.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.1	delete responseJson(). use jsonResponse()
 *	jbaek		2017/09/30		2.2.0	create
 *
**/

import com.irt.cst.MenuCode;
import com.irt.cst.MenuHtml;
import com.irt.data.DataResult;
import com.irt.data.Record;
import com.irt.json.Jsoner;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.SystemConfig;
import com.irt.util.StringUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

/*
 *
 */
@javax.servlet.annotation.WebServlet( urlPatterns = { "/servlet/CSTMenu" } )
public class CSTMenu extends AbstractServletModel {//@formatter:on

	protected final static String MODE_MENU = "menu";

	protected Map<String, Object> createConditionMap( Context ctx ) throws ServletModelException, SQLException {
		ParameterMap conditionMap = new ParameterMap(ctx.req, true);

		return conditionMap;
	}

	protected Map<String, Object> createConditionMap( Context ctx, String... conditionKeys ) throws ServletModelException, SQLException {
		ParameterMap conditionMap = new ParameterMap(ctx.req, true);

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_MENU.equals(ctx.mode) )
			return menu(ctx);

		return super.doRequest(ctx, isPost);
	}

	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("RBM");
	}

	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		String menuId = ctx.req.getParameter("menuId");
		if( menuId == null || menuId.length() <= 0 ) {
			ServletModelException smEx = new ServletModelException(ServletModelException.NEEDED_PARAMETER);
			ctx.pageConfig.setMessage(smEx.getMessage());
			throw smEx;
			// return responseJson(ctx);
		}

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_MENU);

		if( MODE_MENU.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);// menu for logged in user
		else if( MODE_MULTIMODIFY.equals(ctx.mode) || MODE_MULTIMODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "CSTMenu.MNG" + "." + menuId);
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new MenuCode(ctx.handler);
		pageConfig.setTitle(ctx.msghandler.getMessage("TITLE_CST_MENU_", ctx.mode.toUpperCase()));
	}

	protected boolean jsonDataMultiUpdate( Context ctx ) throws ServletModelException, SQLException, IOException {
		MenuCode db = (MenuCode)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);
		conditionMap.put("updateUserId", ctx.sessionMng.getUniqId());

		ParameterMap m = new ParameterMap(ctx.req);
		logger.info(m);
		String jsonString = (String)conditionMap.get("data");
		if( StringUtil.isNullOrEmpty(jsonString) ) {
			ServletModelException smEx = new ServletModelException(ServletModelException.NEEDED_PARAMETER);
			ctx.pageConfig.setMessage(smEx.getMessage());
			ctx.req.setAttribute("message", smEx.getMessage());
		} else {
			List arr = Jsoner.getInstance().fromJson(jsonString, List.class);
			DataResult result = db.deleteInsert(arr);
			if( result == null || result.getErrorCount() > 0 ) {
				ctx.handler.rollback();
				ctx.pageConfig.setMessage("failure");
			} else {
				ctx.handler.commit();
				ctx.pageConfig.setMessage("success");
			}
		}
		return jsonResponse(ctx);
	}

	protected boolean menu( Context ctx ) throws SQLException, IOException {
		MenuCode db = (MenuCode)ctx.db;

		String menuId = ctx.req.getParameter("menuId");
		String menuLocale = ctx.req.getParameter("menuLocale");

		if( StringUtil.isNullOrEmpty(menuId) ) {
			ServletModelException smEx = new ServletModelException(ServletModelException.NEEDED_PARAMETER,
					ctx.msghandler.getMessage(ServletModelException.NEEDED_PARAMETER));
			ctx.pageConfig.setMessage(smEx.getMessage());
			return jsonResponse(ctx);
		} else if( StringUtil.isNullOrEmpty(menuLocale) ) {
			ServletModelException smEx = new ServletModelException(ServletModelException.NEEDED_PARAMETER,
					ctx.msghandler.getMessage(ServletModelException.NEEDED_PARAMETER));
			ctx.pageConfig.setMessage(smEx.getMessage());
			return jsonResponse(ctx);
		} else {
			Map conditionMap = Record.createMap("menuId", menuId);
			conditionMap.put("menuLocale", menuLocale);

			List<Map<String, Object>> records = db.getRecords(conditionMap);
			if( records == null || records.size() <= 0 ) {
				ctx.pageConfig.setMessage(ctx.msghandler.getMessage(ServletModelException.NO_RECORD_FOUND));
				return jsonResponse(ctx);
			} else {
				String menuhtml = MenuHtml.Converter.getMenuHtml(records);
				if( StringUtil.isNullOrEmpty(menuhtml) ) {
					ctx.pageConfig.setMessage(ctx.msghandler.getMessage(ServletModelException.NO_RECORD_FOUND));
					return jsonResponse(ctx);
				} else {
					ctx.req.setAttribute("menuhtml", menuhtml);
					return jsonResponse(ctx, "menuhtml");
				}
			}
		}
	}

	protected boolean multiUpdate( Context ctx, boolean inserting ) throws ServletModelException, SQLException, IOException {
		return jsonDataMultiUpdate(ctx);
	}
}
