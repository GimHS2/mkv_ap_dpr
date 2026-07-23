/*
 *	File Name:	DPRPackDealCfgRlt.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	jbaek		2019/05/30		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.dpr.PackDealCfgRlt;
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
import javax.servlet.annotation.WebServlet;

@WebServlet( urlPatterns = { "/servlet/DPRPackDealCfgRlt" } )
public class DPRPackDealCfgRlt extends DPRServletModel {

	public final static String MODE_COND_SETTING = "rtp";

	private Map<String, Object> createConditionMap( Context ctx ) throws SQLException, ServletException {
		ParameterMap parameterMap = new ParameterMap(ctx.req, true);
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		conditionMap.put("countryCode", getUserCountryCode(ctx));
		conditionMap.put("division", getDivisionCode(ctx));
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));

		if( parameterMap.containsKey("distributionChannelCode") )
			Condition.putConditionValueOnly(conditionMap, "distributionChannelCode", parameterMap.get("distributionChannelCode"));
		if( parameterMap.containsKey("officeCode") )
			Condition.putConditionValueOnly(conditionMap, "officeCode", parameterMap.get("officeCode"));
		if( parameterMap.containsKey("groupCode") )
			Condition.putConditionValueOnly(conditionMap, "groupCode", parameterMap.get("groupCode"));
		setAttributePartyMaster(ctx, conditionMap, PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);

		if( parameterMap.containsKey("partyCode") )
			Condition.putConditionValueOnly(conditionMap, "partyCode", parameterMap.get("partyCode"));
		else {
			String partyCode = getRequestPartyCode(ctx);
			if( partyCode != null ) {
				Condition.putConditionValueOnly(conditionMap, "partyCode", partyCode);
			}
		}

		if( !conditionMap.containsKey("distributionChannelCode") ) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>) ctx.req.getAttribute( "distributionChannels" );
			conditionMap.put( "distributionChannelCode", Record.extractObjectArray(distributionChannels, "distributionChannelCode") );
		}

		setAttributePartner(ctx, conditionMap, PARTNER_SOLD);

		if( parameterMap.containsKey("isPackdealDate") ) {
			Condition.putConditionValueOnly(conditionMap, "isPackdealDate", parameterMap.get("isPackdealDate"));
		} else {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRPackDealCfg.MNG") )
				Condition.putConditionValueOnly(conditionMap, "isPackdealDate", "Y");
		}

		if( parameterMap.containsKey("dealCode") )
			Condition.putConditionValueOnly( conditionMap, "dealCode", parameterMap.get("dealCode"), Condition.CONDTYPE_CONTAINS );

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {

		if( MODE_COND_SETTING.equals(ctx.mode) )
			return setAttributeCondition(ctx);

		return super.doRequest(ctx, isPost);
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		PackDealCfgRlt db = (PackDealCfgRlt)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRPackDeal.RLT%LIST", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML);
		String filename = ctx.msghandler.getMessage("TITLE_DPR_PACKDEALCFGRLT_");
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

	private String getRequestPartyCode( Context ctx ) throws ServletModelException, SQLException {
		String partyCode = ctx.req.getParameter("partyCode");
		String btype = (String)ctx.extraObj;

		if( ( partyCode == null || partyCode.length() == 0 )
				&& com.irt.rbm.SessionMng.GROUPCLASS_ORDER.equals(ctx.sessionMng.getGroupClass()) )
			partyCode = getUserDistributorCode(ctx);

		return ( partyCode == null || partyCode.length() == 0 ? null : partyCode );
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("DPR");
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		ctx.pageConfig.setProperty("mngtype", "pdrlt");
		ctx.pageConfig.setProperty("mngtypeName", "PackDealCfgRlt");

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfgRlt.INF");
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfgRlt.LST");
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfgRlt.LST");
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRPackDealCfgRlt.LST");
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new PackDealCfgRlt(ctx.handler);

		String messageKey = "TITLE_DPR_PACKDEALCFGRLT_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_PACKDEALSETTING", "TITLE_DPR_PACKDEALCFGRLT_" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		PackDealCfgRlt db = (PackDealCfgRlt)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);

		setAttributePartyMaster(ctx, conditionMap,
				PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);
		setAttributePackDealCfg(ctx, conditionMap);

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRPackDeal.RLT%LIST");

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
		List recordList = db.getRecords(conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1]);
		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);

		ctx.req.setAttribute("records", recordList);
		ctx.req.setAttribute("columnList", columnList);
		ctx.req.setAttribute("condition", conditionMap);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		return forward(ctx, systemConfig.getJspPath() + "/dpr_packdealcfg_list.jsp");
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap(ctx);
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", null) )
				return false;
			conditionMap = createConditionMap(ctx);
		}
		ctx.pageConfig.getListIndexVariables()[2] = ( (QueryableManager)ctx.db ).getRecordCount(conditionMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_list_count.jsp");
	}

	@Override
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap(ctx.req);

		if( conditionMap.containsKey("distributionChannelCode") )
			conditionMap.put("distributionChannelCode", getDistributionChannelCode(ctx));

		String ctype = (String)conditionMap.get("ctype");
		boolean typeOnly = false;
		if( conditionMap.containsKey("ctype_only") )
			typeOnly = Boolean.parseBoolean((String)conditionMap.get("ctype_only"));

		if( ctype == null || ctype.length() == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		if( "dc".equals(ctype) ) {
			setAttributePartyMaster(ctx, conditionMap, PARTYMASTER_DISTRIBUTIONCHANNEL);
			List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)ctx.req.getAttribute("distributionChannels");
			if( distributionChannels != null ) {
				Object[] channelCodes = com.irt.data.Record.extractObjectArray(distributionChannels, "distributionChannelCode");

				if( channelCodes.length == 1 )
					conditionMap.put("distributionChannelCode", channelCodes[0]);
			}
		} else if( "sg".equals(ctype) ) {
			setAttributePartyMaster(ctx, conditionMap, PARTYMASTER_GROUP);
			List<Map<String, Object>> groups = (List<Map<String, Object>>)ctx.req.getAttribute("groups");
			if( groups != null ) {
				Object[] groupCodes = com.irt.data.Record.extractObjectArray(groups, "groupCode");

				if( groupCodes.length == 1 )
					conditionMap.put("groupCode", groupCodes[0]);
			}

			if( !typeOnly ) {
				setAttributePartner(ctx, conditionMap, PARTNER_SOLD);
			}
		} else if( "sp".equals(ctype) ) {
			setAttributePartner(ctx, conditionMap, PARTNER_SOLD);
			List<Map<String, Object>> groups = (List<Map<String, Object>>)ctx.req.getAttribute("groups");
			if( groups != null ) {
				Object[] groupCodes = com.irt.data.Record.extractObjectArray(groups, "groupCode");

				if( groupCodes.length == 1 )
					conditionMap.put("groupCode", groupCodes[0]);
			}

			if( !typeOnly ) {
				setAttributePartner(ctx, conditionMap, PARTNER_SOLD);
			}
		} else {
			setAttributePartner(ctx, conditionMap, PARTNER_SOLD);
		}

		ctx.req.setAttribute("condition", conditionMap);

		if( true ) {

			List<Map> groups = (List<Map>)ctx.req.getAttribute("groups");
			assert groups != null;

		}
		return forward(ctx, systemConfig.getJspPath() + "/dpr_item_cond.jsp");
	}

}
