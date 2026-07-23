/*
 *	File Name:	DPRMoqItem.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	yjkdev21	2026/02/27		2.2.2	download() : 컬럼리스트 변경
 *	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
 *	jbaek		2019/06/28		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.dpr.Item;
import com.irt.dpr.MoqItem;
import com.irt.dpr.Period;
import com.irt.dpr.util.CondPred;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;
import com.irt.util.Arrays;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

@WebServlet( urlPatterns = { "/servlet/DPRMoqItem" } )
public class DPRMoqItem extends DPRServletModel {

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize(ctx, true);
	}

	private Map<String, Object> createConditionMap( Context ctx ) throws ServletModelException, SQLException {
		ParameterMap parameterMap = new ParameterMap(ctx.req, true);

		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("countryCode", getUserCountryCode(ctx));
		conditionMap.put("division", getDivisionCode(ctx));
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));

		if( parameterMap.containsKey("showall") )
			conditionMap.put("showall", parameterMap.get("showall"));

		if( parameterMap.containsKey("partyCode") )
			Condition.putConditionValueOnly(conditionMap, "partyCode", parameterMap.get("partyCode"));
		if( parameterMap.containsKey("groupCode") )
			Condition.putConditionValueOnly(conditionMap, "groupCode", parameterMap.get("groupCode"));
		if( parameterMap.containsKey("officeCode") )
			Condition.putConditionValueOnly(conditionMap, "officeCode", parameterMap.get("officeCode"));
		if( parameterMap.containsKey("distributionChannelCode") )
			Condition.putConditionValueOnly(conditionMap, "distributionChannelCode", parameterMap.get("distributionChannelCode"));

		// datevalue
		if( parameterMap.containsKey("dateValueType") )
			conditionMap.put("dateValueType", parameterMap.get("dateValueType"));
		else
			conditionMap.put("dateValueType", "M");
		if( parameterMap.containsKey("dateValueMonth") ) {
			Condition.putConditionValueOnly(conditionMap, "dateValue", parameterMap.get("dateValueMonth"));
			Condition.putConditionValueOnly(conditionMap, "dateValueMonth", parameterMap.get("dateValueMonth"));
		} else {
			if( parameterMap.containsKey("dateValue") )
				conditionMap.put("dateValue", parameterMap.get("dateValue"));
			else {
				String currUniMonth = ctx.pageConfig.getProperty("currUniMonth");
				if( currUniMonth != null ) {
					conditionMap.put("dateValue", currUniMonth);
				}
			}
		}

		CondPred.putValueIfNoKey(conditionMap, "endOrderDate", com.irt.data.Date.getInstance(ctx.handler.getTimeZone()));
		if( "D".equals(conditionMap.get("dateValueType")) ) {
			conditionMap.remove("dateValue");

			String startOrderDate = Record.extractString(parameterMap, "startOrderDate");
			String endOrderDate = Record.extractString(parameterMap, "endOrderDate");
			try {
				com.irt.data.Date today = com.irt.data.Date.getInstance();
				if( endOrderDate != null && endOrderDate.length() > 0 ) {
					if( startOrderDate == null || startOrderDate.length() <= 0 ) {
						CondPred.putIsEquals(conditionMap, "orderDate", com.irt.data.Date.getInstance(endOrderDate));
					} else if( startOrderDate.equals(endOrderDate) ) {
						CondPred.putIsEquals(conditionMap, "orderDate", com.irt.data.Date.getInstance(endOrderDate));
					} else {// has startOrderDate
						conditionMap.put("orderDate" + com.irt.data.Condition.SUFFIX_MIN_VALUE, com.irt.data.Date.getInstance(startOrderDate));
						conditionMap.put("orderDate" + com.irt.data.Condition.SUFFIX_MAX_VALUE, com.irt.data.Date.getInstance(endOrderDate));
						conditionMap.put("orderDate" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MINMAX);
					}
				}
			} catch( java.text.ParseException parseEx ) {
				throw new ServletModelException(ServletModelException.INVALID_PARAMETER);
			}
			conditionMap.put("startOrderDate", startOrderDate);
			conditionMap.put("endOrderDate", endOrderDate);
		}

		// item condition
		if( parameterMap.containsKey("itemConsumerEANCode") )
			Condition.putConditionValueOnly(conditionMap, "itemConsumerEANCode", parameterMap.get("itemConsumerEANCode"));
		String itemCode = getConditionValue((String)parameterMap.get("itemCode"));
		if( itemCode != null )
			Condition.putConditionValueOnly(conditionMap, "itemCode", itemCode, getConditionType(itemCode));
		String itemName = getConditionValue((String)parameterMap.get("itemName"));
		if( itemCode != null && itemName == null ) {
			conditionMap.put("conditionItemName", new Item(ctx.handler).getName(itemCode, getDisplayLanguage(ctx)));
		}
		if( itemName != null ) {
			Condition.putConditionValueOnly(conditionMap, "itemName", getConditionValue(itemName), Condition.CONDTYPE_CONTAINS);
		}

		if( parameterMap.containsKey("brandCode") )
			conditionMap.put("brandCode", parameterMap.get("brandCode"));

		if( parameterMap.containsKey("basetype") ) {
			Condition.putConditionValueOnly(conditionMap, Condition.BASIS_CONDITIONKEY, parameterMap.get("basetype"));
		}

		return conditionMap;
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		MoqItem db = (MoqItem)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRMoqItem.RLT.DOWN%LIST", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML);
		String filename = ctx.msghandler.getMessage("TITLE_DPR_MOQITEM_");
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

	public List<Map<String, Object>> getAllUserDistributorCodes( Context ctx ) throws SQLException, ServletModelException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		conditionMap.put("divisionCode", getDivisionCode(ctx));
		// conditionMap.put("distributionChannelCode", getDistributionChannelCode(ctx));// can be multiple channel with single sold to
		conditionMap.put("uniqId", ctx.sessionMng.getUniqId());
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		if( !ctx.sessionMng.isSystemAdmin() ) {
			conditionMap.put("authIndicator", "Y");
			conditionMap.put("partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE);
			conditionMap.put("baseLinkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD);
		}
		conditionMap.put(com.irt.data.Condition.DISTINCT_CONDITIONKEY, "Y");

		List<Map<String, Object>> recordList = new com.irt.dpr.PartyAuth(ctx.handler).getRecords(conditionMap,
				new String[] { "partyCode", "partyName" });
		return recordList;
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

		pageConfig.setProperty("mngtype", "moqrlt");
		pageConfig.setProperty("mngtypeName", "DPRMoqItem");

		// setSystemPackageCode
		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);

		// if( MODE_INFO.equals(ctx.mode) )
		// pageConfig.setSystemPackageCode("DPR", "DPRMoqItemCfg.MNG");
		// else if( MODE_MODIFY.equals(ctx.mode) || MODE_MODIFYINPUT.equals(ctx.mode) )
		// pageConfig.setSystemPackageCode("DPR", "DPRMoqItemCfg.MNG");
		// else if( MODE_REGIST.equals(ctx.mode) || MODE_REGISTINPUT.equals(ctx.mode) )
		// pageConfig.setSystemPackageCode("DPR", "DPRMoqItemCfg.MNG");
		// else if( MODE_REMOVE.equals(ctx.mode) )
		// pageConfig.setSystemPackageCode("DPR", "DPRMoqItemCfg.MNG");
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRMoqItem.LST");
		// else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
		// pageConfig.setSystemPackageCode("DPR", "DPRMoqItemCfg.MNG");
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPRMoqItem.LST");
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new MoqItem(ctx.handler);

		String messageKey = "TITLE_DPR_MOQITEM_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );
		setPath( ctx, "jsp.MENU_ORDER", "TITLE_DPR_MOQITEM_" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		MoqItem db = (MoqItem)ctx.db;

		setAttributeMonthPeriods(ctx);
		Map<String, Object> conditionMap = createConditionMap(ctx);

		setAttributePartyMaster(ctx, conditionMap,
				PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP);
		setAttributePartner(ctx, conditionMap, PARTNER_SOLD);
		setAttributeOrganizationBrands(ctx);

		ParameterMap parameterMap = new ParameterMap(ctx.req);
//		if( !"Y".equals(parameterMap.get("showall")) ) {
//			conditionMap.put("hierType" + com.irt.data.Condition.CONDTYPE_NOTEQUALS, "PTY");
//
//			if( !parameterMap.containsKey("partyCode") ) {
//				conditionMap.put("partyCode" + com.irt.data.Condition.SUFFIX_IS_NULL, "Y");
//			}
//			if( !parameterMap.containsKey("groupCode") ) {
//				conditionMap.put("groupCode" + com.irt.data.Condition.SUFFIX_IS_NULL, "Y");
//			}
//			if( !parameterMap.containsKey("officeCode") ) {
//				conditionMap.put("officeCode" + com.irt.data.Condition.SUFFIX_IS_NULL, "Y");
//			}
//			if( !parameterMap.containsKey("distributionChannelCode") ) {
//				conditionMap.put("distributionChannelCode" + com.irt.data.Condition.SUFFIX_IS_NULL, "Y");
//			}
//		} else {
//		}

		if( ctx.sessionMng.isSystemAdmin() ) {
			ctx.pageConfig.setManageAuth(true);
		} else if( ctx.sessionMng.isAuthorized("DPR", "DPRMoqItemCfg.MNG") ) {
			ctx.pageConfig.setManageAuth(true);
		}

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPRMoqItem.RLT%LIST");

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		String[] sortKeys = ServletUtility.getSortKeys(ctx.req, columnList.getSortKeys());
		if( !new ArrayList(java.util.Arrays.asList(sortKeys)).contains("hierTypeIndex") )
			sortKeys = Arrays.append(sortKeys, "hierTypeIndex");
		db.setSort(sortKeys);

		List<Map<String, Object>> recordList = db.getRecords(conditionMap, columnList.getFieldKeys(), idxVars[0], idxVars[1]);
		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );
		ctx.req.setAttribute("records", recordList);
		ctx.req.setAttribute("columnList", columnList);
		ctx.req.setAttribute("condition", conditionMap);

		return forward(ctx, systemConfig.getJspPath() + "/dpr_moqitemcfg_list.jsp");
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		MoqItem db = (MoqItem)ctx.db;

		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap(ctx);
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", null) )
				return false;
			conditionMap = createConditionMap(ctx);
		}
		ctx.pageConfig.getListIndexVariables()[2] = db.getRecordCount(conditionMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_list_count.jsp");
	}

	private void setAttributeMonthPeriods( Context ctx ) throws SQLException {
		Period prd = new Period(ctx.handler);

		Map conditionMap = Record.createMap("periodType", "M");
		conditionMap.put("periodSetName", "UNIMONTH");

		String fieldKey = "endDate";
		com.irt.data.Date date = com.irt.data.Date.getInstance().getDate(40);
		conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_MAX_VALUE, date);
		conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MAX);

		fieldKey = "startDate";
		date = com.irt.data.Date.getInstance().getDate(-100);
		conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_MIN_VALUE, date);
		conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MIN);

		prd.setSort(new String[] { "startDate", "endDate" });
		List<Map<String, Object>> recordList = prd.getRecords(conditionMap, new String[] { "startDate", "endDate", "dateValue", "currUniMonth" });

		if( recordList != null && recordList.size() > 0 ) {
			ctx.pageConfig.setProperty("currUniMonth", (String)recordList.get(0).get("currUniMonth"));
		}

		ctx.req.setAttribute("periods", recordList);
	}

}
