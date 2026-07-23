/*
 *	File Name:	DPROrderReport.java
 *	Version:	2.2.5
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_orderreport_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.5	신규 UI/UX 적용
 *	hankalam	2020/06/30		2.2.4	download(), listonly(): columnList 에 useDangerousItem 옵션키 추가
 *	jbaek		2019/07/30		2.2.3	권한 조건 추가.
 *	hankalam	2019/06/28		2.2.3	useCustomerPONumber 옵션 적용
 *	jbaek		2019/05/30		2.2.3	PackDeal 추가
 *	jbaek		2018/10/30		2.2.2	기능 단순화. orderDate conditionMap 조건 변경.
 *	jbaek		2018/03/31		2.2.1	오류수정: orders 가져올때 CD상태조건 추가
 *	jbaek		2017/09/30		2.2.0	create
 *
**/

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.irt.data.Condition;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.dpr.Order;
import com.irt.dpr.OrderDetail;
import com.irt.dpr.OrderInfoDetail;
import com.irt.dpr.util.CondPred;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;
import com.irt.sql.QueryUtility;
import com.irt.sql.SQLManager;
import com.irt.util.cst.DateTimeUtil;

/**
 *
 * {@link DPREnquiryOrder#list(Context)} 기능은 order의 status를 업데이트하고 개별 오더는 사용자가 클릭할시에 가져오게 되어 있음
 *
 * {@link DPROrderReport#list(Context)} 기능은 order및 order detail list을 업데이트하고 정보를 가져옴.
 *
 *
 * 자동으로 order의 detail등을 전부 업데이트하고 가져오게 되기 때문에 기존의 {@link DPREnquiryOrder#list(Context)}보다 더 많은 일을 하게 됨.
 *
 * Servlet thread가 작업을 오래 하고 있으면, 만약 서버는 이런 request를 많이 받아서 작업을 수행하게 되면,
 *
 * 서버는 maxinum servlet thread리미트에 걸리게 되고, WAS Server는 Connection refuse error가 걸리게 된다.
 *
 * 따라서 기존보다 더 많은일을 하고 오래 걸릴 것이기 때문에 {@link WebServlet#asyncSupported()}를 적용함.
 *
 *
 * web.xml asyncSurpported config
 *
 * <pre>
 *...
	<servlet>
		<servlet-name>DPROrderReport</servlet-name>
		<servlet-class>DPROrderReport</servlet-class>
		<async-supported>true</async-supported>
	</servlet>
 *...
 * </pre>
 *
 *
 */
@javax.servlet.annotation.WebServlet( urlPatterns = { "/servlet/DPROrderReport" }, asyncSupported = true )
public class DPROrderReport extends DPROrderServletModel {//@formatter:on

	@Deprecated
	public static final String MODE_DATAAGELIST = "daglist";
	public static final String MODE_LISTONLY = "listonly";
	@Deprecated
	public static final String MODE_FETCH = "fetch";
	@Deprecated
	public static final String MODE_SYNCED_FETCH = "synfetch";
	@Deprecated
	public static final String MODE_FETCH_LIST = "fetchlist";
	@Deprecated
	public static final String MODE_FETCH_PARALLEL = "fetchp";

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize(ctx, true);
	}

	protected Map<String, Object> createConditionMap( Context ctx ) throws SQLException, ServletException {
		ParameterMap conditionMap = new ParameterMap(ctx.req, true);
		Object distChannelCode = conditionMap.get( "distributionChannelCode" );
		setDefaultParameterMultiDist( ctx, conditionMap );
		if( !com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useDetailCondition") ) {
			com.irt.data.Condition.clearCondition(conditionMap, "distributionChannelCode");// user may not in default distribution channel
		} else {
			Map<String, Object> _condition = new java.util.HashMap<String, Object>();
			_condition.put( "organizationCode", conditionMap.get("organizationCode") );
			_condition.put( "countryCode", getUserCountryCode(ctx) );
			_condition.put("organizationCode", getSavedOrganizationCode(ctx));
			_condition.put("displayLanguage", getDisplayLanguage(ctx));
			setAttributePartyMaster(ctx, _condition, PARTYMASTER_DISTRIBUTIONCHANNEL);

			if( distChannelCode == null ) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>) ctx.req.getAttribute( "distributionChannels" );
				conditionMap.put( "distributionChannelCode", Record.extractObjectArray(distributionChannels, "distributionChannelCode") );
			}
		}

		com.irt.data.Condition.putConditionValueOnly(conditionMap, "countryCode", getUserCountryCode(ctx));
		com.irt.data.Condition.putConditionValueOnly(conditionMap, "displayLanguage", getDisplayLanguage(ctx));

		CondPred.putValueIfNoKey(conditionMap, "endOrderDate", com.irt.data.Date.getInstance(ctx.handler.getTimeZone()));
		String startOrderDate = Record.extractString(conditionMap, "startOrderDate");
		String endOrderDate = Record.extractString(conditionMap, "endOrderDate");
		try {
			com.irt.data.Date today = com.irt.data.Date.getInstance();
			if( endOrderDate != null && endOrderDate.length() > 0 ) {
				if( startOrderDate == null || startOrderDate.length() <= 0 ) {
					CondPred.putIsEquals(conditionMap, "orderDate", com.irt.data.Date.getInstance(endOrderDate));
				} else if( startOrderDate.equals(endOrderDate) ) {
					CondPred.putIsEquals(conditionMap, "orderDate", com.irt.data.Date.getInstance(endOrderDate));
				} else {// has startOrderDate
					conditionMap.put("orderDate" + com.irt.data.Condition.SUFFIX_MIN_VALUE, com.irt.data.Date.getInstance(startOrderDate));
					conditionMap.put("orderDate" + com.irt.data.Condition.SUFFIX_MAX_VALUE, com.irt.data.Date.getInstance(endOrderDate).getDate(1));
					conditionMap.put("orderDate" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MIN);
				}
			}
		} catch( java.text.ParseException parseEx ) {
			throw new ServletModelException(ServletModelException.INVALID_PARAMETER);
		}

		// determine user's sold party code
		if( ctx.sessionMng.USERCLASS_USER.equals(ctx.sessionMng.getUserClass())
				&& !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& !conditionMap.containsKey("partyCode") ) {
			List<Map<String, Object>> userDistributors = getUserDistributorCodes(ctx);
			if( userDistributors != null ) {
				if( userDistributors.size() <= 0 ) {// user is no map to dist
					conditionMap.put("partyCode", "");
				} else if( userDistributors.size() == 1 ) {// user is map to single dist
					Map<String, Object> map = userDistributors.get(0);
					conditionMap.put("partyCode", map.get("partyCode"));
				} else {// user is map to multiple dist
					conditionMap.put("partyCode", "");
				}
			} else {// user is map to no dist
				conditionMap.put("partyCode", "");
			}
		}

		CondPred.putIsNotNullIfNoKey(conditionMap, "orderNumber");

		Map<String, Object> masterConditionMap = new java.util.HashMap<String, Object>(conditionMap);
		if( masterConditionMap.containsKey("partyCode") ) {
			setAttributePartner(ctx, masterConditionMap, PARTNER_SHIP);
		}
		masterConditionMap.remove("partyCode");
		masterConditionMap.remove("soldPartyCode");
		setAttributePartner(ctx, masterConditionMap, PARTNER_SOLD);

		CondPred.putIsNotEquals(conditionMap, "itemRefInd", OrderDetail.ITEMREF_PIPO_ORIGINAL);

		boolean onlyAliveRecords = false;// usually better to request deleted records( will request more records )
		if( onlyAliveRecords ) {// orderStatus is null ==> means last wm responses was null. so maybe deleted records.
			CondPred.putIsNotNullIfNoKey(conditionMap, "orderStatus");
		}

		if( conditionMap.containsKey("packdealInd") ) {
			Condition.putConditionValueOnly(conditionMap, "isPackdealOrder", conditionMap.get("packdealInd"));
		}

		return conditionMap;
	}

	@Deprecated
	protected boolean dataAgeList( Context ctx ) throws IOException, ServletException, SQLException {
		setOrderDataAgeList(ctx);
		String[] jsonAttributes = new String[] { "dataAgeList", "dataAgeZone", "dataAgeCondition", "dataAgeSource", "sysDefaultZone", "sessionZone" };

		return jsonResponse(ctx, jsonAttributes);
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( MODE_FETCH.equals(ctx.mode) )
			return fetch(ctx);
		else if( MODE_FETCH_LIST.equals(ctx.mode) )
			return fetchList(ctx);
		else if( MODE_SYNCED_FETCH.equals(ctx.mode) )
			return syncedFetch(ctx);
		else if( MODE_FETCH_PARALLEL.equals(ctx.mode) )
			return fetchParallel(ctx);
		else if( MODE_DATAAGELIST.equals(ctx.mode) )
			return dataAgeList(ctx);
		else if( MODE_LISTONLY.equals(ctx.mode) )
			return listonly(ctx);

		return super.doRequest(ctx, isPost);
	}

//	@Override
//	protected ColumnList getColumnList( Context ctx, String columnListName, String... optionKeys ) throws ServletException {
//		String countryKey = Country.getCountryKeyFromPartyId(ctx.sessionMng.getPartyId());
//
//		ColumnList columnList = super.getColumnList( ctx, columnListName+"."+countryKey, optionKeys );
//		return columnList != null ? columnList : super.getColumnList( ctx, columnListName, optionKeys );
//	}

	@Override
	protected boolean download( Context ctx ) throws ServletException, IOException, SQLException {
		OrderInfoDetail db = (OrderInfoDetail)ctx.extraObj;

		Map<String, Object> conditionMap = createConditionMap(ctx);
		String displayUnknown = (String)conditionMap.get("displayUnknown");
		if( displayUnknown == null || "N".equals(displayUnknown) ) {
			com.irt.dpr.util.CondPred.putIsNotNull(conditionMap, "orderStatus");
		}

		String organizationCode = getSavedOrganizationCode( ctx );
		List<String> optionKeyList = new ArrayList<String>();
		if( com.irt.dpr.Country.isFeature(organizationCode, "useCustomerPONumber") )
			optionKeyList.add("PO");
		if( com.irt.dpr.Country.isFeature(organizationCode, "useDetailCondition") ) {
			optionKeyList.add("D");
			if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() || ctx.sessionMng.isCountryAdmin() ) {
				optionKeyList.add( "A" );
			}
		}

		if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) {
			optionKeyList.add( "OT" );
		}

		if( com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useDetailCondition") ) {
			conditionMap = getDetailConditionMap(ctx, conditionMap);
		}

		// authUniqId
		if( !ctx.sessionMng.isAuthorized("DPR", "DPRPartyAuth.AUTH_ALL")
				&& (!ctx.sessionMng.isSystemAdmin() && !ctx.sessionMng.isPartyAdmin() && !ctx.sessionMng.isCountryAdmin()) ) {
			Condition.putConditionValueOnly( conditionMap, "authUniqId", ctx.sessionMng.getUniqId() );
			Condition.putConditionValueOnly( conditionMap, "authPartyValue", "Y" );
		}

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrderReport%DOWN", optionKeyList.toArray(new String[0]));

		if( columnList.getProperty("filename") == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		String filename = PatternRecordFormat.getInstance(columnList.getProperty("filename"))//
				.format(conditionMap, ctx.msghandler);

		com.irt.data.DataWriter out = createDataWriter(ctx, filename);

		try {
			ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());
			db.write(out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE);
		} catch( SQLException sqlEx ) {
			out.println();
			out.print(sqlEx.getMessage());
			logger.error("Internal Error", sqlEx);
		} finally {
			out.flush();
			out.close();
		}

		return true;
	}

	@Override
	protected boolean downloadInput( Context ctx ) throws ServletException, IOException, SQLException {
		Map<String, Object> conditionMap = createConditionMap(ctx);
		setAttributePartner(ctx, conditionMap, PARTNER_SOLD);
		ctx.req.setAttribute("condition", conditionMap);
		return forward(ctx, systemConfig.getJspPath() + "/dpr_orderreport_download.jsp");
	}

	@Deprecated
	protected boolean fetch( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);
		Map qparams = new com.irt.servlet.ParameterMap(ctx.req);

		Map<String, Object> _conditionMap = new java.util.HashMap<String, Object>(conditionMap);

		Map<String, Object> ret = new java.util.HashMap<String, Object>();
		int newWorkStartNum = getIntValue(ctx.req.getParameter("skipRows"), 0);
		int maxWorkSize = getIntValue(ctx.req.getParameter("maxRows"), 10);

		List<String> optionKeyList = new ArrayList<String>();
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useCustomerPONumber") )
			optionKeyList.add("PO");
		if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useDetailCondition") )
			optionKeyList.add("D");

		// process
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrderReport%LIST", optionKeyList.toArray(new String[0]));
		{
			_conditionMap.put("displayLanguage", getDisplayLanguage(ctx));
			_conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
			_conditionMap.put("status", Order.STATUS_CREATED);

			if( qparams.get("jsonDebug") != null && ( (String)qparams.get("jsonDebug") ).split("Y").length > 0 ) {
				setOrderDataAgeList(ctx);
				Map<String, Object> before = new java.util.HashMap<String, Object>();
				before.put("dataAgeList", ctx.req.getAttribute("dataAgeList"));
				before.put("dataAgeZone", ctx.req.getAttribute("dataAgeZone"));
				before.put("sysDefaultZone", ctx.req.getAttribute("sysDefaultZone"));
				before.put("sessionZone", ctx.req.getAttribute("sessionZone"));
				before.put("dataAgeCondition", ctx.req.getAttribute("dataAgeCondition"));
				before.put("dataAgeSource", ctx.req.getAttribute("dataAgeSource"));
				ctx.req.setAttribute("before", before);
			}

			java.util.Date min = null;
			java.util.Date max = null;
			boolean isBounded = false;
			List<Map<String, Object>> daglist = (List<Map<String, Object>>)ctx.req.getAttribute("dataAgeList");
			if( daglist != null && daglist.size() > 0 ) {
				for( Map<String, Object> map : daglist ) {
					java.util.Date updateTime = (java.util.Date)map.get("updateDateTime");

					if( min == null || updateTime.before(min) ) {
						min = updateTime;
					}
					if( max == null || updateTime.after(max) ) {
						max = updateTime;
					}
				}
			}
			logger.warn("min: " + min + " max: " + max);

			java.util.Date ubnd = null;
			if( ctx.req.getParameter("ucbndDate") == null || ctx.req.getParameter("ucbndDate").length() == 0 ) {
				if( max == null ) {
					ubnd = new java.util.Date();
				} else {
					ubnd = max;
				}
			} else {
				try {
					ubnd = com.irt.util.cst.DateTimeUtil.parseISODate(ctx.req.getParameter("ucbndDate"));
					if( !isBounded ) {
						isBounded = true;
					}
				} catch( ParseException parseEx ) {
					throw new ServletModelException(parseEx.getMessage());
				}
			}
			java.util.Date lbnd = null;
			if( ctx.req.getParameter("lcbndDate") == null || ctx.req.getParameter("lcbndDate").length() == 0 ) {
				if( min == null ) {
					lbnd = new java.util.Date();
				} else {
					lbnd = min;
				}
			} else {
				try {
					lbnd = com.irt.util.cst.DateTimeUtil.parseISODate(ctx.req.getParameter("lcbndDate"));
					if( !isBounded ) {
						isBounded = true;
					}
				} catch( ParseException parseEx ) {
					throw new ServletModelException(parseEx.getMessage());
				}
			}
			logger.warn("lbnd: " + lbnd + " ubnd: " + ubnd);
			ret.put("lcbndDate", lbnd);
			ret.put("ucbndDate", ubnd);

			if( isBounded ) {
				_conditionMap.put("updateDateTime" + com.irt.data.Condition.SUFFIX_MAX_VALUE, ubnd);
				_conditionMap.put("updateDateTime" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MAX);
				_conditionMap.put("updateDateTime" + com.irt.data.Condition.SUFFIX_IS_TIMESTAMP, "Y");
				_conditionMap.put("updateDateTime" + com.irt.data.Condition.SUFFIX_IS_NULL_OR, "Y");

				_conditionMap.put("updateDateTime" + com.irt.data.Condition.SUFFIX_MIN_VALUE, lbnd);
				_conditionMap.put("updateDateTime" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MIN);
				_conditionMap.put("updateDateTime" + com.irt.data.Condition.SUFFIX_IS_TIMESTAMP, "Y");
				_conditionMap.put("updateDateTime" + com.irt.data.Condition.SUFFIX_IS_NULL_OR, "Y");
			}
			ret.put("isBounded", isBounded);

			ret.put("dataAgeCondition", _conditionMap);

			String[] _fieldKeys = new String[] { "orderKey", "simulationKey", "customerOrderNumber", "orderNumber", "partyCode",
					"organizationCode", "itemCount", "updateUserId", "updateDateTime", "orderStatus.status", "status", "orderDate" };

			String formatType = ctx.req.getParameter(PARAM_FORMATTYPE);
			if( formatType == null || formatType.length() == 0 )
				formatType = FORMATTYPE_PC;

			String[] _workFieldKeys = new String[] { "orderKey", "orderNumber", "updateDateTime", "createDateTime" };
			ServletUtility.setSort(ctx.req, headerDB, "createDateTime");
			List<Map<String, Object>> orders = headerDB.getRecords(_conditionMap, _workFieldKeys, newWorkStartNum, maxWorkSize);

			if( orders == null || orders.size() == 0 ) {
				ret.put("dataAgeOldCount", 0);
			} else if( orders != null && orders.size() > 0 ) {
				ret.put("dataAgeOldCount", orders.size());

				int touched = 0;
				int toleranced = 0;
				int toleranceable = 0;
				int executed = 0;
				int executable = 0;
				List<String> executedOrderNumbers = new java.util.ArrayList<String>();
				List<String> updatedTimeTypeList = new java.util.ArrayList<String>();
				int dataAgeToleranceMinute = getIntValue(ctx.req.getParameter("dataAgeToleranceMinute"), 5);
				long minTime = getToleranceMinimumTime(dataAgeToleranceMinute);
				List<Long> updatedTimeList = new java.util.ArrayList<Long>();
				for( Map<String, Object> order : orders ) {
					final String orderKey = (String)order.get("orderKey");
					final String orderNumber = (String)order.get("orderNumber");
					Object updateDateTime = order.get("updateDateTime");
					if( orderKey != null && orderNumber != null ) {

						// com.irt.data.Timestamp utime = null;
						long updatedTime = -1;
						String msg = "{";
						if( updateDateTime instanceof java.util.Date ) {
							// updatedTime = new com.irt.data.Timestamp(updateDateTime);
							msg += "type:" + updateDateTime.getClass().getCanonicalName() + ",";

							boolean isSelected = false;
							if( updateDateTime instanceof java.sql.Timestamp ) {
								isSelected = true;
								msg += "isSqlTimestamp: true, ";
								updatedTime = ( (java.sql.Timestamp)updateDateTime ).getTime();
							}
							if( updateDateTime instanceof com.irt.data.Date ) {
								isSelected = true;
								msg += "isDataDate: true, ";
								updatedTime = ( (com.irt.data.Timestamp)updateDateTime ).getTime();
							}
							if( updateDateTime instanceof java.sql.Date ) {
								msg += "isSqlDate: true";
								isSelected = true;
								updatedTime = ( (java.sql.Date)updateDateTime ).getTime();
							} else {
								msg += "isJavaDate: true, ";
								updatedTime = ( (java.util.Date)updateDateTime ).getTime();
							}
							msg += "java.sql.Timestamp: " + ( (java.sql.Timestamp)updateDateTime ).getTime() + ", ";
							msg += "com.irt.data.Timestamp: " + ( (com.irt.data.Timestamp)updateDateTime ).getTime() + ", ";
							// msg += "java.sql.Date: "+((java.sql.Date)updateDateTime).getTime()+", ";
							msg += "java.util.Date: " + ( (java.util.Date)updateDateTime ).getTime() + ", ";
							updatedTimeList.add(updatedTime);
							msg += "stringValue: " + updateDateTime.toString() + ", ";
							msg += "instanceZone: " + ( (com.irt.data.Timestamp)updateDateTime ).getZone().getID() + ", ";
						} else {
							throw new ServletModelException("data type is not convertable: type: " + updateDateTime.getClass().getCanonicalName()
									+ " value: " + updateDateTime);
						}

						if( updatedTime < minTime ) {
							executable++;
						} else {
							toleranceable++;
						}
						// 'executable' is checking and 'executed' is really executed.
						// currently toleranced function is beta function
						// so just execute to get the latest data.
						{
							executeStatus(ctx, new java.util.TreeMap<String, Object>() {
								{
									put("orderKey", orderKey);
									put("orderNumber", orderNumber);
								}
							});
							executedOrderNumbers.add(orderNumber);
							executed++;
						}

						msg += "diff(utime-mintime): " + ( updatedTime - minTime ) + ",";

						msg += "}";
						updatedTimeTypeList.add(msg);
					}
					touched++;
				}
				ret.put("dataAgeToleranceMinute", dataAgeToleranceMinute);
				ret.put("dataAgeTouchedCount", touched);
				ret.put("dataAgeToleranceableCount", toleranceable);
				ret.put("dataAgeTolerancedCount", toleranced);
				ret.put("dataAgeExecutedKeys", executedOrderNumbers);
				ret.put("dataAgeExecutableCount", executable);
				ret.put("dataAgeExecutedCount", executed);
				ret.put("dataAgeProcessedCount", null);// currently cannot know it processed or not.
				ret.put("dataAgeMinTime", minTime);
				com.irt.data.Timestamp minTimestamp = new com.irt.data.Timestamp(minTime);
				ret.put("dataAgeMinTimestamp", minTimestamp);
				ret.put("dataAgeMinTimestampZone", minTimestamp.getZone().getID());
				ret.put("updatedTimeList", updatedTimeList);
				ret.put("updatedTimeTypeList", updatedTimeTypeList);
			}
		}

		ctx.req.setAttribute("qparams", qparams);
		ctx.req.setAttribute("condition", _conditionMap);
		ctx.req.setAttribute("record", ret);

		if( qparams.get("jsonDebug") != null && ( (String)qparams.get("jsonDebug") ).split("Y").length > 0 ) {
			setOrderDataAgeList(ctx);
			Map<String, Object> after = new java.util.HashMap<String, Object>();
			after.put("dataAgeList", ctx.req.getAttribute("dataAgeList"));
			after.put("sessionZone", ctx.req.getAttribute("sessionZone"));
			after.put("sysDefaultZone", ctx.req.getAttribute("sysDefaultZone"));
			after.put("dataAgeCondition", ctx.req.getAttribute("dataAgeCondition"));
			after.put("dataAgeSource", ctx.req.getAttribute("dataAgeSource"));
			ctx.req.setAttribute("after", after);
		}

		// no more work. than call executeStatusList to match other status
		// maybe better do this first when skipRows == 0 and forget about the dataAge timeing and just update all....
		// since executeStatusList also update the order table, then we cannot use the updateDateTime for dataAge check...
		// Integer dataAgeOldCount = (Integer)ret.get("dataAgeOldCount");
		// if( dataAgeOldCount == 0 ) {
		// int count = headerDB.getRecordCount(_conditionMap);
		// if( count > 0 ) {
		// if( newWorkstartNUm == count ) {
		// executeStatusList(ctx, _conditionMap);
		// ret.put("dataAgeFullCount", count);
		// }
		// }
		// }

		return jsonResponse(ctx, "record", "condition", "qparams", "before", "after");
	}

	@Deprecated
	protected boolean fetchList( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);
		Map qparams = new com.irt.servlet.ParameterMap(ctx.req);

		ctx.req.setAttribute("qparams", qparams);
		ctx.req.setAttribute("condition", conditionMap);

		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));
		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		conditionMap.put("status", Order.STATUS_CREATED);
		Map<String, Object> ret = new java.util.HashMap<String, Object>();
		int count = headerDB.getRecordCount(conditionMap);
		if( count > 0 ) {
			executeStatusList(ctx, conditionMap);
			ret.put("dataAgeFullCount", count);
			ret.put("executed", true);
			ret.put("processed", "null");// does not know if processed or not.
		}

		ctx.req.setAttribute("record", ret);

		return jsonResponse(ctx, "record", "condition", "qparams");
	}

	@Deprecated
	protected boolean fetchParallel( Context ctx ) throws IOException, ServletException, SQLException {
		return true;
	}

	private Map<String,Object> getDetailConditionMap( Context ctx, Map<String,Object> originalMap ) throws ServletModelException {
		Map<String, Object> _conditionMap = new java.util.HashMap<String, Object>(originalMap);
		_conditionMap.put( "organizationCode", originalMap.get("organizationCode") );
		_conditionMap.put( "distributionChannelCode", originalMap.get("distributionChannelCode") );
		_conditionMap.put( "officeCode", originalMap.get("officeCode") );
		_conditionMap.put( "groupCode", originalMap.get("groupCode") );
		_conditionMap.put( "countryCode", getUserCountryCode(ctx) );
		boolean isBlankPartyCode = (originalMap.get("partyCode") != null && originalMap.get("partyCode") instanceof String && ((String)originalMap.get("partyCode")).length() == 0);
		if( isBlankPartyCode  )
			_conditionMap.remove("partyCode");

		return _conditionMap;
	}

	private int getIntValue( String paramValueInt, int defaultValue ) {
		int paramValue = defaultValue;
		if( paramValueInt != null && paramValueInt.length() > 0 ) {
			try {
				paramValue = Integer.parseInt(paramValueInt);
			} catch( NumberFormatException nfEx ) {
				logger.error(nfEx);
			}
		}
		return paramValue;
	}

	private String getLastSavedQuery( com.irt.sql.SQLHandler handler ) throws UnsupportedEncodingException {
		com.irt.sql.QueryStorage qstore = handler.getSavedQuery();
		String content = "";
		if( qstore != null ) {
			com.irt.sql.QueryBuffer querybuf = qstore.getSavedQueryBuffer(qstore.getSavedQuerySize() - 1);// the last one
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			java.io.PrintStream out = new java.io.PrintStream(baos);
			QueryUtility.printQuery(out, querybuf);
			content = new String(baos.toByteArray(), "UTF-8");
		}
		return content;
	}

	private com.irt.data.Timestamp getMinimumTimestamp( int dataAgeToleranceMinute ) {
		long _minTime = DateTimeUtil.decreaseByUnit(new java.util.Date().getTime(), DateTimeUtil.UNITMS_MINUTE, dataAgeToleranceMinute);

		com.irt.data.Timestamp ts = new com.irt.data.Timestamp(_minTime);
		return ts;
	}

	private com.irt.data.Timestamp getRefetchPointTimestamp( int cacheKeepMinute ) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(SQLManager.getDBTimeZone());
		final long refetchPointTimeMs = cal.getTimeInMillis() - java.util.concurrent.TimeUnit.MINUTES.toMillis(cacheKeepMinute);
		com.irt.data.Timestamp refetchPointTime = new com.irt.data.Timestamp(refetchPointTimeMs, SQLManager.getDBTimeZone());
		return refetchPointTime;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("DPR");
	}

	private long getToleranceMinimumTime( int dataAgeToleranceMinute ) {
		long _minTime = DateTimeUtil.decreaseByUnit(DateTimeUtil.getNowUTCTimestamp().getTime(), DateTimeUtil.UNITMS_MINUTE, dataAgeToleranceMinute);
		return _minTime;
	}

	@Override
	public List<Map<String, Object>> getUserDistributorCodes( Context ctx ) throws SQLException, ServletModelException {
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

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);

		if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderReport.LST");
		else if( MODE_LISTONLY.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderReport.LST");
		else if( MODE_FETCH.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderReport.LST");
		else if( MODE_SYNCED_FETCH.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderReport.LST");
		else if( MODE_FETCH_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderReport.LST");
		else if( MODE_FETCH_PARALLEL.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderReport.LST");
		else if( MODE_DATAAGELIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderReport.LST");
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_DOWNLOAD.equals(ctx.mode) || MODE_DOWNLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderReport.DWN");
		else if( MODE_CODENAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderReport.LST");
		else if( MODE_COND_SETTING.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new Order(ctx.handler, getSystemConfig());
		ctx.extraObj = new OrderInfoDetail(ctx.handler);

		String messageKey = "TITLE_DPR_ORDERREPORT_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_ORDER", "jsp.SUBMENU_ORDERREPORT" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		return listSimple(ctx);
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap(ctx);
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPROrderReport.LST") )
				return false;
			conditionMap = createConditionMap(ctx);
		}
		ctx.pageConfig.getListIndexVariables()[2] = ( (OrderInfoDetail)ctx.extraObj ).getRecordCount(conditionMap);

		return forward(ctx, systemConfig.getJspPath() + "/pub_list_count.jsp");
	}

	protected boolean listonly( Context ctx ) throws IOException, ServletException, SQLException {
		OrderInfoDetail db = (OrderInfoDetail)ctx.extraObj;

		// ready
		Map<String, Object> conditionMap = createConditionMap(ctx);
		String displayUnknown = (String)conditionMap.get("displayUnknown");
		if( displayUnknown == null || "N".equals(displayUnknown) ) {
			com.irt.dpr.util.CondPred.putIsNotNull(conditionMap, "orderStatus");
		}
		String organizationCode = getSavedOrganizationCode( ctx );
		List<String> optionKeyList = new ArrayList<String>();
		if( com.irt.dpr.Country.isFeature(organizationCode, "useCustomerPONumber") )
			optionKeyList.add( "PO" );
		if( com.irt.dpr.Country.isFeature(organizationCode, "useDetailCondition") ) {
			optionKeyList.add( "D" );
			if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isPartyAdmin() || ctx.sessionMng.isCountryAdmin() ) {
				optionKeyList.add( "A" );
			}
		}
		if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) {
			optionKeyList.add( "OT" );
		}

		// process
		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrderReport%LIST", optionKeyList.toArray(new String[optionKeyList.size()]));
		String[] fieldKeys = columnList.getFieldKeys();
		fieldKeys = com.irt.util.Arrays.append(fieldKeys, "headerOrderStatus");

		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort(ctx.req, db, columnList.getSortKeys());

		if( com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useDetailCondition") ) {
			conditionMap = getDetailConditionMap(ctx, conditionMap);
			Map<String,Object> _conditionMap = new java.util.TreeMap<String,Object>(conditionMap);
			_conditionMap.remove("partyCode");
			_conditionMap.remove("shipPartyCode");
			_conditionMap.remove("soldPartyCode");
			setAttributePartyMaster( ctx, _conditionMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL | PARTYMASTER_OFFICE | PARTYMASTER_GROUP );
			setAttributePartner( ctx, _conditionMap, PARTNER_SOLD );
		}

		List<Map<String, Object>> recordList = db.getRecords(conditionMap, fieldKeys, idxVars[0], idxVars[1]);

		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		Map<String, Object> itemConditionMap = Record.createMap( "organizationCode", conditionMap.get("organizationCode") );
		List<Map<String, Object>> items = getItemList( ctx, itemConditionMap );
		ctx.req.setAttribute( "items", items );
		ctx.req.setAttribute("condition", conditionMap);
		ctx.req.setAttribute("columnList", columnList);
		ctx.req.setAttribute("records", recordList);

		ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPROrderReport.MNG"));

		// forward
		return forward(ctx, systemConfig.getJspPath() + "/dpr_orderreport_list.jsp");
	}


	private boolean listSimple( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;

		String maxReqSizeInt = ctx.req.getParameter("maxReqSize");
		int maxReqSize = getIntValue(maxReqSizeInt, 200);// request to wm size ( "status" request=single order )

		// ready
		Map<String, Object> conditionMap = createConditionMap(ctx);

		Map<String, Object> _conditionMap_list = new java.util.TreeMap<String, Object>(conditionMap);
		_conditionMap_list.put("displayLanguage", getDisplayLanguage(ctx));
		_conditionMap_list.put("organizationCode", getSavedOrganizationCode(ctx));
		_conditionMap_list.put("status", Order.STATUS_CREATED);
		int count = headerDB.getRecordCount(_conditionMap_list);
		if( count > 0 ) {
			logger.info("executeStatusList invoked for count: " + count);
			executeStatusList(ctx, _conditionMap_list);
		}

		List<Map<String, Object>> orders = headerDB.getRecords(_conditionMap_list, new String[] { "orderKey", "orderNumber" });
		if( orders != null ) {
			if( orders.size() > maxReqSize ) {
				ctx.pageConfig.setMessage(ctx.msghandler.getMessage("ERR_EXCEED_MAX_REQUEST_SIZE"));
			} else {
				for( Map<String, Object> order : orders ) {
					final String orderKey = (String)order.get("orderKey");
					final String orderNumber = (String)order.get("orderNumber");
					if( orderKey != null && orderNumber != null ) {
						Map<String, Object> map = new TreeMap<String, Object>();
						map.put("orderKey", orderKey);
						map.put("orderNumber", orderNumber);

						executeStatus(ctx, map);
					}
				}
			}
		}

		return listonly(ctx);
	}

	@Override
	protected boolean setAttributeCondition( Context ctx ) throws IOException, ServletException, SQLException {
		Map<String, Object> paramMap = new ParameterMap( ctx.req );
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>( paramMap );
		setDefaultParameterMultiDist( ctx, conditionMap );

		if( !com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useDetailCondition") ) {
			com.irt.data.Condition.clearCondition(conditionMap, "distributionChannelCode");
		}

		String ctype = (String)conditionMap.get( "ctype" );
		setAttributeCondition( ctx, conditionMap, ctype );
		ctx.req.setAttribute( "condition", conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_attribute_party.jsp" );
	}

	@Deprecated
	protected void setOrderDataAgeList( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;
		Map<String, Object> conditionMap = createConditionMap(ctx);
		conditionMap.put("status", Order.STATUS_CREATED);
		String[] fieldKeys = new String[] { "orderNumber", "status", "orderStatus",
				"updateDateTime", "updateDateTimeZone",
				"createDateTime", "createDateTimeZone" };

		ServletUtility.setSort(ctx.req, headerDB, null);
		boolean wasDebugging = ctx.handler.debugging();
		if( !ctx.handler.debugging() ) {
			ctx.handler.enableDebugging();
		}
		List<Map<String, Object>> dataAgeList = headerDB.getRecords(conditionMap, fieldKeys);
		String sqlQuery = getLastSavedQuery(ctx.handler);
		if( !wasDebugging ) {
			ctx.handler.disableDebugging();
		}

		// ctx.req.setAttribute("dataAgeSql", sqlQuery);
		ctx.req.setAttribute("dataAgeCondition", conditionMap);
		ctx.req.setAttribute("sysDefaultZone", java.util.TimeZone.getDefault().getID());
		ctx.req.setAttribute("sessionZone", ctx.sessionMng.getTimeZone().getID());
		ctx.req.setAttribute("dataAgeList", dataAgeList);
		ctx.req.setAttribute("dataAgeSource", com.irt.dpr.Schema.DPR_ORDER);
	}

	/** 'synchedFetch' fetches all dates in single function.'fetch' fetches using skipRows and maxRows to make it granularFetch */
	@Deprecated
	protected boolean syncedFetch( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;

		Map<String, Object> conditionMap = createConditionMap(ctx);
		Map qparams = new com.irt.servlet.ParameterMap(ctx.req);

		ctx.req.setAttribute("qparams", qparams);
		Map<String, Object> ret = new java.util.HashMap<String, Object>();

		Map<String, Object> _conditionMap = new TreeMap<String, Object>(conditionMap);
		_conditionMap.put("displayLanguage", getDisplayLanguage(ctx));
		_conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));
		_conditionMap.put("status", Order.STATUS_CREATED);

		String[] _workFieldKeys = new String[] { "orderKey", "orderNumber", "updateDateTime", "createDateTime" };
		ServletUtility.setSort(ctx.req, headerDB, "createDateTime");
		ctx.req.setAttribute("condition", _conditionMap);
		List<Map<String, Object>> orders = headerDB.getRecords(_conditionMap, _workFieldKeys);

		if( orders == null || orders.size() == 0 ) {
			ret.put("dataAgeOldCount", 0);
		} else if( orders != null && orders.size() > 0 ) {
			ret.put("dataAgeOldCount", orders.size());

			int touched = 0;
			int executed = 0;
			List<String> executedOrderNumbers = new java.util.ArrayList<String>();

			for( Map<String, Object> order : orders ) {
				final String orderKey = (String)order.get("orderKey");
				final String orderNumber = (String)order.get("orderNumber");
				Object updateDateTime = order.get("updateDateTime");
				if( orderKey != null && orderNumber != null ) {
					{
						executeStatus(ctx, new java.util.TreeMap<String, Object>() {
							{
								put("orderKey", orderKey);
								put("orderNumber", orderNumber);
							}
						});
						executedOrderNumbers.add(orderNumber);
						executed++;
					}
				}
				touched++;
			}
		}

		ctx.req.setAttribute("record", ret);

		return jsonResponse(ctx, "record", "condition", "qparams");
	}
}
