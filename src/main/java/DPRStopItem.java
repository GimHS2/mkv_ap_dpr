
/*
 *	File Name:	DPRStopItem.java
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
 *	jbaek		2019/04/30		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.dpr.StopItem;
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

@WebServlet( urlPatterns = { "/servlet/DPRStopItem" } )
public class DPRStopItem extends DPRServletModel {

	public final static String MODE_COND_SETTING = "rtp";

	private String getRequestPartyCode( Context ctx ) throws ServletModelException, SQLException {
		String partyCode = ctx.req.getParameter("partyCode");
		String btype = (String)ctx.extraObj;

		if( ( partyCode == null || partyCode.length() == 0 )
				&& com.irt.rbm.SessionMng.GROUPCLASS_ORDER.equals(ctx.sessionMng.getGroupClass()) )
			partyCode = getUserDistributorCode(ctx);

		return ( partyCode == null || partyCode.length() == 0 ? null : partyCode );
	}

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

		if( !parameterMap.containsKey("distributionChannelCode") ) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>) ctx.req.getAttribute( "distributionChannels" );
			conditionMap.put( "distributionChannelCode", Record.extractObjectArray(distributionChannels, "distributionChannelCode") );
		}

		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			List<Map<String, Object>> partyCodes = getUserDistributorCodes(ctx);
			if( partyCodes != null && partyCodes.size() > 0 ) {
				Object[] partyCode = Record.extractObjectArray(partyCodes, "partyCode");
				conditionMap.put("partyCode", partyCode);
			}
		}
		setAttributePartner(ctx, conditionMap, PARTNER_SOLD);

		if( parameterMap.containsKey("itemConsumerEANCode") )
			Condition.putConditionValueOnly(conditionMap, "itemConsumerEANCode", parameterMap.get("itemConsumerEANCode"));
		if( parameterMap.containsKey("itemCode") ) {
			Condition.putConditionValueOnly( conditionMap, "itemCode", parameterMap.get("itemCode"), Condition.CONDTYPE_CONTAINS );
		}
		if( parameterMap.containsKey("itemName") ) {
			Condition.putConditionValueOnly( conditionMap, "itemName", parameterMap.get("itemName"), Condition.CONDTYPE_CONTAINS );
		}

		if( parameterMap.containsKey("isStopItem") ) {
			Condition.putConditionValueOnly(conditionMap, "isStopItem", parameterMap.get("isStopItem"));
		} else {
			Condition.putConditionValueOnly(conditionMap, "isStopItem", "Y");
		}

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
		StopItem db = (StopItem)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRStopItem%LIST", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML);
		String filename = ctx.msghandler.getMessage("TITLE_DPR_STOPITEM_");
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

	private String getConditionType( String value ) {
		if( value.indexOf("%") >= 0 )
			return Condition.CONDTYPE_LIKE;
		else if( value.indexOf("_") >= 0 )
			return Condition.CONDTYPE_LIKE;

		return Condition.CONDTYPE_EQUALS;
	}

	/**
	 * Wildcard Searching
	 * '*' : some characters matches. replace to "%" oracle wildcard.
	 * '?' : one charcter matches. replace to "_" oracle wildcard.
	 **/
	private String getConditionValue( String value ) {
		if( value == null )
			return null;
		List<String[]> list = new java.util.ArrayList();
		if( value.indexOf("*") >= 0 )
			list.add(new String[] { "*", "%" });
		if( value.indexOf("?") >= 0 )
			list.add(new String[] { "?", "_" });

		Object[] regex = new Object[list.size()];
		list.toArray(regex);

		if( regex.length > 0 ) {
			int cnt = 0;
			int position = -1;
			for( int i = 0; i < regex.length; i++ ) {
				String[] str = (String[])regex[i];

				while( ( position = value.indexOf(str[0]) ) >= 0 ) {
					if( position == 0 )
						value = str[1] + value.substring(1);
					else if( position > 0 )
						value = value.substring(0, position) + str[1] + value.substring(position + 1);
				}
			}
		}

		return value;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("DPR");
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);
		if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRStopItem.INF");
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRStopItem.LST");
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRStopItem.LST");
		else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRStopItem.LST");
		else if( MODE_REMOVE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRStopItem.LST");
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRStopItem.LST");
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRStopItem.LST");
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new StopItem(ctx.handler);

		pageConfig.setProperty("mngtype", "rlt");
		pageConfig.setProperty("mngtypeName", "StopItem");

		String messageKey = "TITLE_DPR_STOPITEM_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_MATERIAL", "jsp.SUBMENU_STOPITEM" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		StopItem db = (StopItem)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);

		setAttributePartyMaster(ctx, conditionMap,
				PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRStopItem%LIST");

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
		List recordList = db.getRecords(conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1]);
		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);

		ctx.req.setAttribute("records", recordList);
		ctx.req.setAttribute("columnList", columnList);
		ctx.req.setAttribute("condition", conditionMap);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);
		ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPRStopItem.MNG"));

		return forward(ctx, systemConfig.getJspPath() + "/dpr_stopitemcfg_list.jsp");
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap(ctx);
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPRStopItem.LST") )
				return false;
			conditionMap = createConditionMap(ctx);
		}
		ctx.pageConfig.getListIndexVariables()[2] = ( (StopItem)ctx.db ).getRecordCount(conditionMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_list_count.jsp");
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
}
