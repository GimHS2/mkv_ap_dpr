/*
 *	File Name:	DPROrderRevise.java
 *	Version:	2.2.4
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *		dpr_orderrevise_input.jsp
 *		dpr_orderrevise_list.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2024/08/28		2.2.4	backURL 생성시 isSecure 옵션값을 준수하도록 변경
 *	dudwls3720	2024/02/29		2.2.3	download() : 컬럼리스트 이름 변경, tryWorkbookAutoSizeColumn() 삭제
 *	hankalam	2021/11/30		2.2.2	신규 UI/UX 적용
 *	jbaek		2020/10/16		2.2.1	orderItem조회시 shipTo(및 plantCode)가 적용되는 getItemTreeList에서 정보를 가져오도록 변경
 *	jbaek		2020/06/30		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.cols.*;
import com.irt.data.format.PatternRecordFormat;
import com.irt.dpr.*;
import com.irt.dpr.util.CondPred;
import com.irt.dpr.util.Loggers;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.servlet.*;
import com.irt.sql.SQLManager;
import com.irt.util.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

@WebServlet( name = "DPROrderRevise", urlPatterns = { "/servlet/DPROrderRevise" } )
public class DPROrderRevise extends DPRPlaceOrder {// @formatter:on

	private static final String MODE_REVISE_COMMIT = "revcit";
	private static final String MODE_REVISE_CANCEL = "revccl";
	private static final String MODE_REVISE_RESENDMAIL = "revrsm";
	private static final String MODE_QUERY_ITEM = "qryitm";

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize(ctx, true);
	}

	private void checkThrowParameterNull( Context ctx, String... parameterNames ) throws ServletModelException {
		if( parameterNames != null ) {
			List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
			try {
				for( String parameterName : parameterNames ) {
					if( parameterName != null && ctx.req.getParameter(parameterName) == null ) {
						if( "dv".equals(systemConfig.getProperty("appEnvId")) ) {
							String extraMsg = parameterName;
							errorList.add( createErrorMap(parameterName+".queryString", ctx.req.getQueryString()) );
							errorList.add( createErrorMap(parameterName+".url", ctx.req.getRequestURL().toString()) );
							errorList.add( createErrorMap(parameterName+".method", ctx.req.getMethod()) );
							errorList.add( createErrorMap(parameterName+".caller", com.irt.util.TraceHelper.getMethodName(1)) );
							errorList.add( createErrorMap(parameterName+".stacktrace", com.irt.util.TraceHelper.formatCurrentStacktrace()) );
							errorList.add( createErrorMap(parameterName+".pageConfig.props", ctx.pageConfig.getProperty().toString()) );
							throw new ServletModelException(ServletModelException.NEEDED_PARAMETER, ctx.msghandler
									.getMessage(ServletModelException.NEEDED_PARAMETER + "_1", extraMsg));
						} else {
							throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
						}
					}
				}
			} finally {
				if( errorList != null && errorList.size() > 0 )
					ctx.req.setAttribute("errors", errorList);
			}
		}
	}

	private Map<String, Object> createListConditionMap( Context ctx ) throws ServletModelException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap(ctx.req);
		Map<String, Object> detailConditionMap = createReviseOrderDetailConditionMap(ctx, null);
		detailConditionMap.remove("reviseStatus");
		conditionMap.putAll(detailConditionMap);

		String reviseHelpType = ctx.req.getParameter("reviseHelpType");
		if( reviseHelpType != null && reviseHelpType.length() > 0 ) {
			conditionMap.put("reviseHelpType", ctx.req.getParameter("reviseHelpType"));
		}
		String soldPartyCode = ctx.req.getParameter("soldPartyCode");
//		if( soldPartyCode != null && soldPartyCode.length() > 0 ) {
//			conditionMap.put("soldPartyCode", ctx.req.getParameter("soldPartyCode"));
//		}

		if( conditionMap.containsKey("revOrderNumber") )
			Condition.putConditionValueOnly(conditionMap, "compositeOrderNumber", conditionMap.get("revOrderNumber"));
		if( conditionMap.get("reviseStatus") == null )
			conditionMap.put("reviseStatus", new Object[] {"", "CQ", "CP"});


		conditionMap.put("revChgedInd", "Y");

		if( conditionMap.containsKey("origOrderKey") ) {
			Condition.putConditionValueOnly( conditionMap, "parentOrderKey", conditionMap.get("origOrderKey"));
		}
		CondPred.putIsNotNull(conditionMap, "parentOrderKey");
		CondPred.putIsNotNull(conditionMap, "reviseHbrdSeqId");

		if( !conditionMap.containsKey("revOrderNumber") ) {
			CondPred.putValueIfNoKey(conditionMap, "endRevBaseDate", com.irt.data.Date.getInstance(ctx.handler.getTimeZone()));
		}

		//revisingBaseDate is date+time: needs to increase endRevBaseDate+1 to show correctly
		String startRevBaseDate = Record.extractString(conditionMap, "startRevBaseDate");
		String endRevBaseDate = Record.extractString(conditionMap, "endRevBaseDate");
		try {
			com.irt.data.Date today = com.irt.data.Date.getInstance();
			if( endRevBaseDate != null && endRevBaseDate.length() > 0 ) {
				if( startRevBaseDate == null || startRevBaseDate.length() <= 0 ) {
					conditionMap.put("revisingBaseDate" + com.irt.data.Condition.SUFFIX_MIN_VALUE, com.irt.data.Date.getInstance(ctx.handler.getTimeZone()));
					conditionMap.put("revisingBaseDate" + com.irt.data.Condition.SUFFIX_MAX_VALUE, com.irt.data.Date.getInstance(endRevBaseDate).getDate(1));
					conditionMap.put("revisingBaseDate" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MIN);
				} else {// has startRevBaseDate
					conditionMap.put("revisingBaseDate" + com.irt.data.Condition.SUFFIX_MIN_VALUE, com.irt.data.Date.getInstance(startRevBaseDate));
					conditionMap.put("revisingBaseDate" + com.irt.data.Condition.SUFFIX_MAX_VALUE, com.irt.data.Date.getInstance(endRevBaseDate).getDate(1));
					conditionMap.put("revisingBaseDate" + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_MIN);
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
				String[] partyCodes = null;
				int i = 0;
				for( Map<String, Object> partyMap : userDistributors ) {
					String partyCode = (String) partyMap.get( "partyCode" );
					if( userDistributors.size() == 1 && partyCode.equals(soldPartyCode) ) {
						conditionMap.put( "partyCode", partyCode );
					} else if( userDistributors.size() > 1 ) {
						if( soldPartyCode != null ) {
							if( partyCode.equals(soldPartyCode) ) {
								conditionMap.put("partyCode", partyCode);
								break;
							}
						} else {
							if( partyCodes == null ) {
								partyCodes = new String[userDistributors.size()];
							}
							partyCodes[i] = partyCode;
							i++;
						}
					}
				}
				if( partyCodes != null ) {
					conditionMap.put("partyCode", partyCodes );
				}
				if( conditionMap.get("partyCode") == null ) {
					conditionMap.put("partyCode", "");
				}
			} else {// user is map to no dist
				conditionMap.put("partyCode", "");
			}
		}

		if( conditionMap.get("partyCode") != null && conditionMap.get("partyCode") instanceof String )
			conditionMap.put( "soldPartyCode", conditionMap.get("partyCode") );

		return conditionMap;
	}

	private Map<String, Object> createReviseOrderDetailConditionMap( Context ctx, String reviseOrderKey ) throws ServletModelException, SQLException {
		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("orderKey", reviseOrderKey);
		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));
		conditionMap.put(Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER);
		conditionMap.put(OrderDetail.CONDKEY_NO_IMP_TABLE_IND, "Y");
		conditionMap.put(OrderDetail.CONDKEY_ORD_NO_OITM_COND_IND, "Y");

		conditionMap.put("organizationCode", ctx.pageConfig.getProperty("savedOrgCd"));
		if( Country.isFeature((String)conditionMap.get("organizationCode"), "useRevOrd") )
			conditionMap.put("useRevOrd", "Y");

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( isPost ) {
			if( MODE_UPDATEDETAIL.equals(ctx.mode) )
				return updateDetail(ctx);
			else if( MODE_WAIT.equals(ctx.mode) )
				return wait(ctx);
		} else {
			if( MODE_REMOVEDETAIL.equals(ctx.mode) )
				return removeDetail(ctx);
			else if( MODE_SIMULATION.equals(ctx.mode) )
				return simulationOrder(ctx);
			else if( MODE_INFO.equals(ctx.mode) )
				return reviseInfo(ctx);
			else if( MODE_REGISTINPUT.equals(ctx.mode) )
				return reviseRegistInput(ctx);
			else if( MODE_QUERY_ITEM.equals(ctx.mode) )
				return queryItem(ctx);
			else if( MODE_REVISE_COMMIT.equals(ctx.mode) )
				return reviseCommit(ctx);
			else if( MODE_REVISE_CANCEL.equals(ctx.mode) )
				return reviseCancel(ctx);
			else if( MODE_REVISE_RESENDMAIL.equals(ctx.mode) )
				return reviseSendMail(ctx);
		}

		return super.doRequest(ctx, isPost);
	}

	@Override
	protected boolean download( Context ctx ) throws ServletException, IOException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		Map<String, Object> conditionMap = createListConditionMap(ctx);

		final com.irt.data.cols.ColumnList init_columnList = getColumnList( ctx, "DPROrderHSSF.DETAIL%DOWN");

		ColumnListWrapper columnList = new ColumnListWrapper(init_columnList) {
			@Override
			public Column[] getColumns() {
				Column[] columns = init_columnList.getColumns();
				for( int i = 0; i < columns.length; i++ ) {
					if( "reviseHbrdContent".equals(columns[i].getFieldKey()) ) {
						columns[i] = getColumn(i);
					}
				}
				return columns;
			}
			@Override
			public Column getColumn( int index ) {
				ColumnWrapper cw = null;
				final Column column = super.getColumn(index);
				if( "reviseHbrdContent".equals(column.getFieldKey()) ) {
					cw = new ColumnWrapper(column) {
						private Map getSaferMap( Map recordMap ) {
							String reviseHbrdContent = (String)recordMap.get("reviseHbrdContent");
							if( reviseHbrdContent != null && reviseHbrdContent.length() > 0 )
								recordMap.put("reviseHbrdContent", OrderRevise.getSaferHelpBoardContent(recordMap));

							return recordMap;
						}

						@Override
						public String format( Map recordMap, MessageHandler msghandler ) {
							return column.format( getSaferMap(recordMap), msghandler );
						}

						@Override
						public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
							return column.format( getSaferMap(recordMap), msghandler, stringBuffer );
						}

						@Override
						public Object getColumnValue( Map recordMap, MessageHandler msghandler ) {
							return column.getColumnValue( getSaferMap(recordMap), msghandler );
						}
					};
				}
				return ( cw == null ? column : cw );
			}
		};

		if( columnList.getProperty("filename") == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		String filename = PatternRecordFormat.getInstance(columnList.getProperty("filename"))//
				.format(conditionMap, ctx.msghandler);

		com.irt.data.DataWriter out = createDataWriter(ctx, filename);

		try {
			ServletUtility.setSort(ctx.req, detailDB, columnList.getSortKeys());
			detailDB.write(out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE);
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
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("DPR");
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_LIST);
		if( MODE_COND.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderRevise.LST");
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrderRevise.LST");
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_UPDATEDETAIL.equals(ctx.mode) || MODE_REMOVEDETAIL.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_SHORTAGE_ELIMINATE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_SIMULATION.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_WAIT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_REGISTINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_INFO.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_REVISE_COMMIT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_REVISE_CANCEL.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_REVISE_RESENDMAIL.equals(ctx.mode) ) {
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		}
		// else if( MODE_REVISE_DOWNLOAD.equals(ctx.mode) )
		// pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");

		else if( MODE_QUERY_ITEM.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new OrderRevise(ctx.handler, systemConfig);
		ctx.extraObj = new OrderDetail(ctx.handler);

		String messageKey = "TITLE_DPR_ORDERREVISE_";
		pageConfig.setTitle( ctx.msghandler.getMessage(messageKey) );
		pageConfig.setSubTitle( ctx.msghandler.getMessage(messageKey + ctx.mode.toUpperCase()) );

		setPath( ctx, "jsp.MENU_ORDER", "jsp.SUBMENU_ORDERREVISE" );
	}

	@Override
	protected boolean list( Context ctx ) throws IOException, ServletException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		Map<String, Object> conditionMap = createListConditionMap( ctx );
		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPROrder.DETAIL%REVLIST.LST");

		String[] fieldKeys = columnList.getFieldKeys("itemCode", "itemCodeConfirmed", "childLineNumber", "itemRefInd", "detailStatus"
				, "reviseStatus", "revisingBaseDate", "parentReviseStatus", "parentOrderNumber", "parentOrderKey");
		int[] idxVars = ctx.pageConfig.getListIndexVariables();
		String[] sortKeys = columnList.getSortKeys();
		ServletUtility.setSort(ctx.req, detailDB, columnList.getSortKeys());
		List<Map<String, Object>> recordList = detailDB.getRecords(conditionMap, fieldKeys, idxVars[0], idxVars[1]);
		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		ctx.req.setAttribute( "records", recordList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "condition", conditionMap );
		if( ctx.sessionMng.isSystemAdmin() || ctx.sessionMng.isAuthorized("DPR", "DPROrderRevise.MNG") )
			ctx.pageConfig.setManageAuth( true );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_orderrevise_list.jsp" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws SQLException, ServletException, IOException {
		Map<String, Object> conditionMap;
		try {
			conditionMap = popConditionMap( ctx );
		} catch( ServletModelException servletEx ) {
			if( !ctx.sessionMng.isAuthorized("DPR", "DPROrderRevise.LST") ) return false;
			conditionMap = createListConditionMap( ctx );
		}
		ctx.pageConfig.getListIndexVariables()[2] = ((OrderRevise)ctx.db).getRecordCount( conditionMap );

		return forward( ctx, systemConfig.getJspPath() + "/pub_list_count.jsp" );
	}



	public boolean queryItem( Context ctx ) throws IOException, ServletModelException, SQLException {
		OrderItem oitm = new OrderItem(ctx.handler);

		checkThrowParameterNull(ctx, "itemCode", "partyCode");

		String itemCode = ctx.req.getParameter("itemCode");
		String partyCode = ctx.req.getParameter("partyCode");
		String organizationCode = ctx.pageConfig.getProperty("savedOrgCd");
		String divisionCode = getDivisionCode(ctx);
		String shipPartyCode = ctx.req.getParameter("shipPartyCode");
		String distributionChannelCode = null;
		distributionChannelCode = getDistributionChannelCode(ctx);

		Map<String, Object> conditionMap = Record.createMap("itemCode", itemCode);
		conditionMap.put("organizationCode", organizationCode);
		conditionMap.put("distributionChannelCode", distributionChannelCode);
		conditionMap.put("divisionCode", divisionCode);
		conditionMap.put("partyCode", partyCode);
		conditionMap.put("soldPartyCode", partyCode);
		conditionMap.put("shipPartyCode", shipPartyCode);
		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));

		String countryCode = getUserCountryCode(ctx);
		Map<String, Object> recordMap = new com.irt.dpr.Country(ctx.handler).getDefaultHierarchyCondition( countryCode );
		if( recordMap != null ) {
			if( recordMap.get("defaultHierarchyLevel") != null ) {
				conditionMap.put( "defaultHierarchyLevel", recordMap.get("defaultHierarchyLevel") );
				conditionMap.put( "classCode", recordMap.get("defaultHierarchyLevel") );
			}
			if( recordMap.get("hierarchyCondition") != null ) {
				conditionMap.put( "hierarchyCondition", ((String)recordMap.get("hierarchyCondition")).split(";") );
			}
		}

		String orderType = ctx.req.getParameter("orderType");
		boolean useDangerousItem = Country.isFeature(organizationCode, "useDangerousItem") && (orderType != null && orderType.length() > 0);
		if( useDangerousItem ) {
			String dangerousPlant = Country.getFeatureValue( organizationCode, "dangerousPlant" );
			if( dangerousPlant != null ) {
				Condition.putConditionValueOnly( conditionMap, "useDangerousItem", useDangerousItem );
				Condition.putConditionValueOnly( conditionMap, "dangerousPlant", dangerousPlant );
				Condition.putConditionValueOnly( conditionMap, "dangerousNumber", Item.ITEMTYPE_DANGEROUS );
				conditionMap.put( "dangerousInd", (Order.ORDER_TYPE_DANGEROUS.equals(orderType) ? "Y" : "N") );
			} else {
				throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_NEEDED_ENVIRONMENT", "dangerousPlant") );
			}
		}

//		List<Map<String, Object>> records = oitm.getRecords(conditionMap, new String[] { "itemCode", "itemName", "salesUnit", "baseUnit", "uom", "dangerousInd" });
		List<Map<String, Object>> records = oitm.getItemTreeList( conditionMap );

		getDetailListPushUOM(ctx, records);

		if( records == null ) {
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("ERR_ISNOT_SELLINGSKU"));
		}
		ctx.req.setAttribute("records", records);

		return jsonResponse(ctx, "records");
	}

	private boolean readyReviseBeforeSimulation( Context ctx ) throws IOException, ServletException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		checkThrowParameterNull(ctx, "orderKey");
		String reviseOrderKey = ctx.req.getParameter("orderKey");

		try {
			Map<String, Object> primaryMap = Record.createMap("orderKey", reviseOrderKey);

			Map<String, Object> headerMap = headerDB.getRecord(primaryMap,
					new String[] { "orderKey", "countryCode", "organizationCode", "distributionChannelCode", "divisionCode", "partyCode",
							"shipPartyCode", "soldPartyCode"
					});

			com.irt.data.Date rddDate = headerDB.getRddByCalc(headerMap, ctx.sessionMng.getUniqId(), ctx.sessionMng.getTimeZone(),
					com.irt.dpr.Order.SAPRDDNULL_PLUS_DAYS);

			if( headerDB.reviseSimulationStart(reviseOrderKey, rddDate) ) {

				if( ctx.pageConfig.getBackURL() == null )
					throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

				List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
				int regCnt = this.registDetailContent(ctx, errorList);
				if( errorList.size() > 0 ) {
					ctx.req.setAttribute("errors", errorList);
					return false;
				}

				int updCnt = this.updateDetailContent(ctx, errorList);
				if( errorList.size() > 0 ) {
					ctx.req.setAttribute("errors", errorList);
					return false;
				}

			}
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			logger.error("error.", dataEx);
			throw new ServletModelException(ServletModelException.ERROR, dataEx.getMessage());
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			logger.error("internal error.", sqlEx);
			throw new ServletModelException(ServletModelException.ERROR, sqlEx.getMessage());
		}

		return true;
	}

	protected int registDetailContent( Context ctx, List<Map<String, Object>> errorList ) throws IOException, ServletException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		checkThrowParameterNull(ctx, "origOrderKey", "orderKey");
		String origOrderKey = ctx.req.getParameter("origOrderKey");
		String reviseOrderKey = ctx.req.getParameter("orderKey");

		Map<String, Object> conditionMap = createReviseOrderDetailConditionMap(ctx, reviseOrderKey);

		String[] odtlBase_itemFieldKeys = { "itemCode", "itemName", "lineNumber" };

		List<Map<String, Object>> db_recordList = detailDB.getRecords(conditionMap, odtlBase_itemFieldKeys);
		Collection<Object> db_lineNumbers = MapUtil.extractValues(db_recordList, "lineNumber");

		Collection<Map<String, Object>> records = ( new ParameterMap(ctx.req) ).extractGroupList("value");
		int regCnt = 0;

		if( records != null ) {
			Collection<Object> lineNumbers = MapUtil.extractValues(records, "lineNumber");
			List<Object> newLineNumbers = new java.util.ArrayList<Object>();
			List<String> _lns = new java.util.ArrayList<String>();
			for( Object dbln : db_lineNumbers ) {
				_lns.add(dbln.toString());
			}

			for( Object ln : lineNumbers ) {
				if( !_lns.contains(ln) ) {
					newLineNumbers.add(ln);
				}
			}
			Loggers.ext.debug("found newLineNumbers orderKey: " + reviseOrderKey + " : " + newLineNumbers + " condMap:" + conditionMap);

			com.irt.dpr.Item itemDB = new com.irt.dpr.Item(ctx.handler);
			for( Map<String, Object> recordMap : records ) {
				recordMap.put("orderKey", reviseOrderKey);

				if( !newLineNumbers.contains(recordMap.get("lineNumber")) ) {
					continue;
				} else if( recordMap.get("itemCode") == null ) {
					continue;
				}
				Loggers.ext.debug("registering newItemLine:" + recordMap);

				recordMap.put("countryCode", getUserCountryCode(ctx));
				recordMap.put("updateUserId", ctx.sessionMng.getUniqId());
				recordMap.put("displayLanguage", getDisplayLanguage(ctx));
				recordMap.put("status", OrderDetail.STATUS_NORMAL);

				try {
					com.irt.data.ValidableFieldSet fieldSet = (com.irt.data.ValidableFieldSet)detailDB.getFieldSet(true);
					fieldSet.validate(recordMap);
					if( detailDB.regist(recordMap) )
						regCnt++;
					else
						errorList.add(createErrorMap(
								"[" + recordMap.get("lineNumber") + "] " + itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)),
								ctx.msghandler.getMessage("ERR_CANNOT_REGIST")));

				} catch( com.irt.data.FieldException fieldEx ) {
					logger.error("field error:" + "" + fieldEx.getErrorField().getFieldKey(), fieldEx);

					errorList.add(createErrorMap("[" + recordMap.get("lineNumber") + "] " + recordMap.get("itemCode"),
							fieldEx + " " + fieldEx.getErrorField().getFieldKey()));
				} catch( DataException dataEx ) {
					logger.error("data error:", dataEx);
					errorList.add(createErrorMap("[" + recordMap.get("lineNumber") + "] " + recordMap.get("itemCode"), dataEx));
				}
			}
		}

		return regCnt;
	}

	@Override
	protected boolean removeDetail( com.irt.servlet.ServletModel.Context ctx )
			throws java.io.IOException, javax.servlet.ServletException, java.sql.SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;

		checkThrowParameterNull(ctx, "origOrderKey", "orderKey");
		String origOrderKey = ctx.req.getParameter("origOrderKey");
		String reviseOrderKey = ctx.req.getParameter("orderKey");

		String[] lineNumbers = ctx.req.getParameterValues("lineNumber");
		try {
			headerDB.reviseDetailQtyRemove(reviseOrderKey, lineNumbers);
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			logger.error("error.", dataEx);
			throw new ServletModelException(dataEx.getErrorKey(), dataEx.getMessage());
		}

		ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", new String[] { String.valueOf(lineNumbers.length) }));

		return sendRedirect(ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)));
	}

	private boolean reviseCancel( Context ctx ) throws IOException, ServletException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;

		checkThrowParameterNull(ctx, "origOrderKey", "orderKey");
		String origOrderKey = ctx.req.getParameter("origOrderKey");
		String reviseOrderKey = ctx.req.getParameter("orderKey");

		String errBackURL = new StringBuffer(((HtmlPage)ctx.pageConfig).getRequestURL()).append("?").append(ctx.req.getQueryString()).toString();
		errBackURL = HtmlUtility.replaceURLQuery(errBackURL, PARAM_MODE, "ireg");

		String redirectURL = null;
		redirectURL = systemConfig.getClassURL() + "/DPREnquiryOrder?mode=info";
		redirectURL = com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, "menu", "portal");
		redirectURL = com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, "locale", getDisplayLanguage(ctx));
		redirectURL = com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, "orderKey", origOrderKey);
		redirectURL = com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, "status", com.irt.dpr.Order.STATUS_CREATED);

		String backURL = ctx.pageConfig.getBackURL();
		if( backURL == null ) {
			backURL = systemConfig.getClassURL() + "/DPREnquiryOrder?mode=cond&menu=portal&locale=" + getDisplayLanguage(ctx);
		}
		redirectURL = com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, "url", HtmlUtility.encodeURIComponent(backURL));

		return sendRedirect(ctx,
				com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)));
	}

	private boolean reviseCommit( Context ctx ) throws IOException, ServletException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;

		checkThrowParameterNull(ctx, "reviseHelpType", "origOrderKey", "orderKey");
		String origOrderKey = ctx.req.getParameter("origOrderKey");
		String reviseOrderKey = ctx.req.getParameter("orderKey");

		String errBackURL = new StringBuffer(((HtmlPage)ctx.pageConfig).getRequestURL()).append("?").append(ctx.req.getQueryString()).toString();
		errBackURL = HtmlUtility.replaceURLQuery(errBackURL, PARAM_MODE, "ireg");

		Map<String, Object> boardMap = new ParameterMap(ctx.req, true).extractGroupMap("board");
		if( boardMap == null ) {
			boardMap = (Map<String, Object>)ctx.req.getAttribute("boardMap");
		}
		if( boardMap == null ) {
			ctx.pageConfig.setBackURL(errBackURL);
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		}

		String uniqId = ctx.sessionMng.getUniqId();
		boardMap.put("registUserId", uniqId);

		String[] nonnullKeys = { "boardClassCode", "boardType", "boardOption", "userName", "tel", "email", "registUserId" };
		if( !MapUtil.containAllKeysAndValueNotNull(boardMap, nonnullKeys) ) {
			ctx.pageConfig.setBackURL(errBackURL);
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER + "_1",  "keys:"+ java.util.Arrays.asList(nonnullKeys) + " val:" + boardMap);
		}
		if( !com.irt.util.Utility2.validateEmailCsv((String)boardMap.get("email")) )
			throw new ServletModelException(DataException.ERR_INVALID_VALUE,
				ctx.msghandler.getMessage("ERR_INVALID_EMAIL", (String)boardMap.get("email")));

		String reviseHelpType = (String)boardMap.get("reviseHelpType");
		if( reviseHelpType == null || reviseHelpType.length() == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		try {
			String currRevStatus = (String)headerDB.getFieldValue(Record.createMap("orderKey", origOrderKey), "reviseStatus");
			if( "CQ".equals(currRevStatus) ) {
				errBackURL = systemConfig.getClassURL() + "/DPREnquiryOrder?mode=info";
				errBackURL = com.irt.html.HtmlUtility.replaceURLQuery(errBackURL, "menu", "portal");
				errBackURL = com.irt.html.HtmlUtility.replaceURLQuery(errBackURL, "locale", getDisplayLanguage(ctx));
				errBackURL = com.irt.html.HtmlUtility.replaceURLQuery(errBackURL, "orderKey", origOrderKey);
				errBackURL = com.irt.html.HtmlUtility.replaceURLQuery(errBackURL, "status", com.irt.dpr.Order.STATUS_CREATED);
				ctx.pageConfig.setBackURL(errBackURL);
				throw new DataException("ERR_ORDREV_CREATION_REQUEST_ALREADY", "ERR_ORDREV_CREATION_REQUEST_ALREADY");
			}

			boardMap = headerDB.manageHelpBoard(uniqId, boardMap);

			Integer boardNumber = com.irt.util.Utility2.DataField.toInteger(boardMap.get("boardNumber"));
			logger.info("order revise commit executing. orderKey: "+ reviseOrderKey + " boardNumber: "+ boardNumber);

			Map<String, Object> mailContentMap = null;
			if( headerDB.reviseCommit(reviseOrderKey, reviseHelpType, boardNumber) ) {
				mailContentMap = headerDB.reviseCommitProcessMailContentMap(reviseOrderKey, ctx.sessionMng, getDisplayLanguage(ctx));
			} else {
				throw new DataException(DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE), boardMap);
			}
			if( mailContentMap != null ) {
				ctx.handler.commit();
				logger.debug("order revise commit success. orderKey: "+ reviseOrderKey + " boardNumber: "+ boardNumber + mailContentMap);
				headerDB.reviseCommitSendMail(mailContentMap);
				logger.debug("order revise mail send executed. orderKey: "+ reviseOrderKey + " boardNumber: "+ boardNumber);
			}
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			logger.error("error.", dataEx);
			ctx.pageConfig.setBackURL(errBackURL);
			throw new ServletModelException(dataEx);
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setBackURL(errBackURL);
			logger.error("internal error.", sqlEx);
			throw sqlEx;
		}

		ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_DPR_ORDREV_SUBMIT_SUCCESS"));

		String redirectURL = null;
		redirectURL = systemConfig.getClassURL() + "/DPREnquiryOrder?mode=info";
		redirectURL = com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, "menu", "portal");
		redirectURL = com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, "locale", getDisplayLanguage(ctx));
		redirectURL = com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, "orderKey", origOrderKey);
		redirectURL = com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, "status", com.irt.dpr.Order.STATUS_CREATED);

		String backURL = ctx.pageConfig.getBackURL();
		if( backURL == null ) {
			backURL = systemConfig.getClassURL() + "/DPREnquiryOrder?mode=cond&menu=portal&locale=" + getDisplayLanguage(ctx);
		}
		redirectURL = com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, "url", HtmlUtility.encodeURIComponent(backURL));

		return sendRedirect(ctx,
				com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)));
	}

	protected boolean reviseRegistInput( Context ctx ) throws IOException, ServletException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		com.irt.data.AbstractFieldSet boardFieldSet = new com.irt.ics.HelpBoard(ctx.handler).getFieldSet(true);
		ctx.req.setAttribute("boardFieldSet", boardFieldSet);

		checkThrowParameterNull(ctx, "origOrderKey");
		String origOrderKey = ctx.req.getParameter("origOrderKey");

		try {
			String currRevStatus = (String)headerDB.getFieldValue(Record.createMap("orderKey", origOrderKey), "reviseStatus");
			if( "CQ".equals(currRevStatus) ) {
				String errBackURL = null;
				errBackURL = systemConfig.getClassURL() + "/DPREnquiryOrder?mode=info";
				errBackURL = com.irt.html.HtmlUtility.replaceURLQuery(errBackURL, "menu", "portal");
				errBackURL = com.irt.html.HtmlUtility.replaceURLQuery(errBackURL, "locale", getDisplayLanguage(ctx));
				errBackURL = com.irt.html.HtmlUtility.replaceURLQuery(errBackURL, "orderKey", origOrderKey);
				errBackURL = com.irt.html.HtmlUtility.replaceURLQuery(errBackURL, "status", com.irt.dpr.Order.STATUS_CREATED);
				ctx.pageConfig.setBackURL(errBackURL);
				throw new DataException("ERR_ORDREV_CREATION_REQUEST_ALREADY", "ERR_ORDREV_CREATION_REQUEST_ALREADY");
			}
		} catch(DataException dataEx ) {
			logger.debug("error", dataEx);
			throw new ServletModelException(dataEx);
		}

		String reviseOrderKey = ctx.req.getParameter("orderKey");
		if (reviseOrderKey == null || reviseOrderKey.length() == 0) {
			reviseOrderKey = headerDB.createReviseOrder(origOrderKey);
		}
		if( reviseOrderKey == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		if( origOrderKey.equals(reviseOrderKey) )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		String parentOrderNumber = (String) SQLManager.getObjectValue(ctx.handler,
				"SELECT ORDER_NUMBER FROM DPR_ORDER WHERE ORDER_KEY = ?", new String[] { origOrderKey });
		if( parentOrderNumber == null )
			throw new ServletModelException(DataException.ERR_CANNOT_UPDATE);

		if( ctx.req.getParameter("shortageType") != null ) {
			ctx.pageConfig.setProperty("shortageType", ctx.req.getParameter("shortageType"));
		}

		String organizationCode = getSavedOrganizationCode(ctx);
		Map<String, Object> primaryMap = Record.createMap("orderKey", reviseOrderKey);
		primaryMap.put("organizationCode", organizationCode);
		primaryMap.put("displayLanguage", getDisplayLanguage(ctx));
		primaryMap.put("uniqId", ctx.sessionMng.getUniqId());

		String[] headerKeys = { "partyCode", "soldPartyCode", "soldPartyName", "partyName", "orderNumber", "orderKey",
				"divisionCode", "status", "shipPartyCode", "distributionChannelCode", "organizationCode",
				"reviseStatus", "parentReviseStatus", "reviseChangeIndex",
				"reviseHbrdSeqId", "revHbrdLastDate",  "revHbrdContent", "reviseHelpType", //
				"revHbrdEmail", "lastRegistUserEmail",
				"inDate", "inDateDefault", "simulationKey", "customerOrderNumber", "revisingBaseDate",
				"orderType"
		};
		Map<String, Object> headerMap = headerDB.getRecord(primaryMap, headerKeys);
		headerMap.put("orderNumber", parentOrderNumber);
		headerMap.put("orderKey", reviseOrderKey);
		headerMap.put("origOrderKey", origOrderKey);
//		if( "MD".equals(headerMap.get("reviseStatus")) ) {
//			headerMap.put("reviseHelpType", "ROM");//default
//		}

		com.irt.data.cols.ColumnList columnList = null;
		if( true ) {
			String parentRevStatus = (String)headerMap.get("parentReviseStatus");
			com.irt.data.Date revLastDate = (com.irt.data.Date)headerMap.get("revHbrdLastDate");
			com.irt.data.Date today = com.irt.data.Date.getInstance(ctx.handler.getTimeZone());
			if( "CQ".equals(parentRevStatus) ) {
				throw new ServletModelException("ERR_ORDREV_CREATION_REQUEST_ALREADY");
			}
			boolean canReviseNew = ( parentRevStatus == null
//					|| "MD".equals(parentRevStatus)
					)
					|| ( ( parentOrderNumber != null ) &&
							!( "CQ".equals(parentRevStatus) ) &&
							( revLastDate == null || today.after(revLastDate) ) );
			if( !canReviseNew ) {
				throw new ServletModelException("ERR_ORDREV_CANNOT_REGIST");
			}

			try {
				if( parentRevStatus == null
//						|| "MD".equals(parentRevStatus)
						)
					headerDB.reviseDetailUpdate(reviseOrderKey);

//				Map<String, Object> updatedMap = headerDB.reviseHeaderStart(reviseOrderKey);
//				if( updatedMap != null )
//					headerMap.putAll(updatedMap);
			} catch( DataException dataEx ) {
				ctx.handler.rollback();
				logger.error("error.", dataEx);
				throw new ServletModelException(ServletModelException.INVALID_REQUEST);
			} catch( SQLException sqlEx ) {
				ctx.handler.rollback();
				logger.error("internal error.", sqlEx);
				throw sqlEx;
			}

			columnList = getColumnList(ctx, "DPROrder.DETAIL%REVLIST.REG");
		}

		Map<String, Object> conditionMap = createReviseOrderDetailConditionMap(ctx, reviseOrderKey);
		conditionMap.put("isRevOrd", "Y");
		Map<String, Object> sumMap = headerDB.getSimulationSummary(conditionMap);
		ctx.req.setAttribute("summary", sumMap);
		if( "N".equals(sumMap.get("isPlaceRevisible")) ) {
			ctx.pageConfig.setMessage(
					ctx.msghandler.getMessage("ERR_MSG_ORDREV_TOTAL_TOO_LOW"
//							, String.valueOf(sumMap.get("revfinOrderTotal"))
//							, String.valueOf(sumMap.get("minimumValue"))
							));
		}

		com.irt.dpr.OrderItem itemDB = new com.irt.dpr.OrderItem(ctx.handler);
		String allowUOM = null;
		if( com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM") )
			allowUOM = itemDB.getDistAllowUOM(headerMap);
		if( allowUOM == null )
			allowUOM = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "uom;" + organizationCode, com.irt.dpr.Party.DEFAULT_UOM);

		ctx.pageConfig.setProperty("allowUOM", allowUOM);

		ServletUtility.setSort(ctx.req, detailDB, columnList.getSortKeys());
		List<Map<String, Object>> recordList = detailDB.getRecords(conditionMap,
				columnList.getFieldKeys("itemCode", "itemCodeConfirmed", "childLineNumber", "itemRefInd", "detailStatus"));
		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		if( recordList != null && recordList.size() > 0 ) {
			recordList = getDetailListPushUOM(ctx, recordList);
		}

		Integer maxRevAdd = OrderRevise.DEFAULT_MAX_REVISE_ADD_PER_DAY;
		Object[] revBfCnfQtys = null;
		int lastCnfLineIndex = 0;
		if( recordList != null && recordList.size() > 0 ) {
			revBfCnfQtys = Record.extractObjectArray(recordList, "reviseBeforeCnfQty");
			lastCnfLineIndex = recordList.size() - 1;
			for( int i = 0; i < revBfCnfQtys.length; i++ ) {
				if( revBfCnfQtys[i] == null ) {
					lastCnfLineIndex = i;
					break;
				}
			}

		} else {
			recordList = new ArrayList<Map<String, Object>>();
		}
		Object[] lineNumbers = Record.extractObjectArray(recordList, "lineNumber");
		Object[] revBfOrdQtys = Record.extractObjectArray(recordList, "reviseBeforeOrderQty");
		if( revBfCnfQtys != null ) {
			for( int i = 0; i < revBfCnfQtys.length; i++ ) {
				if( revBfCnfQtys[i] == null ) {
					lastCnfLineIndex = i;
					break;
				}
			}
		}

		Integer lastLineNumber = null;
		try {
			lastLineNumber = ( lineNumbers == null || lineNumbers.length == 0 ? 0
					: com.irt.util.Utility2.DataField.toInteger(lineNumbers[lineNumbers.length - 1], 0) );
		} catch( DataException ignored ) {
		}

		lastCnfLineIndex = ( lastCnfLineIndex == 0
				? ( revBfCnfQtys == null ? -1 : revBfCnfQtys.length - 1 )
				: lastCnfLineIndex - 1 );
		int maxLineCount = maxRevAdd + lastCnfLineIndex + 1;
		com.irt.dpr.util.Loggers.ext.debug("{}", "orderKey" + reviseOrderKey
				+ " lc:" + lastCnfLineIndex + " mxlc:" + maxLineCount
				+ " recsz:" + ( recordList != null ? recordList.size() : "null" ));
		int cnt = 0;
		for( int lnIndex = lastCnfLineIndex + 1; lnIndex < maxLineCount; lnIndex++ ) {
			if( revBfCnfQtys != null && ( lnIndex < revBfCnfQtys.length && revBfCnfQtys[lnIndex] == null ) ) {
				Loggers.ext.debug("{}", "skipping: " + " ln:" + lnIndex + " rb:" + java.util.Arrays.asList((revBfCnfQtys==null?new Object[] {}:revBfCnfQtys)));
				continue;
				// skip
			}

			Map<String, Object> map = Record.createMap("lineNumber", ( lastLineNumber / 10 + 1 + cnt ) * 10);
			map.put("childLineNumber", 0);
			map.put("itemRefInd", OrderDetail.ITEMREF_NORMAL);
			recordList.add(map);

			Loggers.ext.debug("{}", "added: " + " ln:" + lnIndex + " rb:" + java.util.Arrays.asList((revBfCnfQtys==null?new Object[] {}:revBfCnfQtys)));
			cnt++;
		}

		String headerEmail = (String)headerMap.get("revHbrdEmail");
		if( headerEmail == null || headerEmail.length() <= 0 )
			headerEmail = (String)headerMap.get("lastRegistUserEmail");
		headerMap.put("email", headerEmail);

		String headerContent = (String)headerMap.get("revHbrdContent");
		headerMap.put("content", headerContent);

		ctx.req.setAttribute("header", headerMap);
		ctx.req.setAttribute("records", recordList);
		ctx.req.setAttribute("condition", conditionMap);
		ctx.req.setAttribute("columnList", columnList);
		ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPROrder.MNG"));

		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDERREVISE_" + ctx.mode.toUpperCase()) );
		ctx.req.setAttribute( "path", null );
		setPath( ctx, "jsp.MENU_ORDER", "TITLE_DPR_ORDERSTATUS_DETAIL", "TITLE_DPR_ORDERREVISE_IREG" );

		if( ctx.req.getParameter("json") != null )
			return jsonResponse(ctx, "records");
		else
			return forward(ctx, systemConfig.getJspPath() + "/dpr_orderrevise_input.jsp");
	}

	protected boolean reviseInfo( Context ctx ) throws IOException, ServletException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		com.irt.data.AbstractFieldSet boardFieldSet = new com.irt.ics.HelpBoard(ctx.handler).getFieldSet(true);
		ctx.req.setAttribute("boardFieldSet", boardFieldSet);

		checkThrowParameterNull(ctx, "origOrderKey");
		String origOrderKey = ctx.req.getParameter("origOrderKey");

		String reviseOrderKey = ctx.req.getParameter("reviseOrderKey");
		if( reviseOrderKey == null ) {
			reviseOrderKey = (String)SQLManager.getObjectValue(ctx.handler
					, "select order_key from dpr_order where parent_orderkey = ? and revng_basedate is not null"
					, new Object[] {origOrderKey});
			if( reviseOrderKey == null )
				throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		}

		if( ctx.req.getParameter("shortageType") != null ) {
			ctx.pageConfig.setProperty("shortageType", ctx.req.getParameter("shortageType"));
		}

		String organizationCode = getSavedOrganizationCode(ctx);
		Map<String, Object> primaryMap = Record.createMap("orderKey", origOrderKey);
		primaryMap.put("organizationCode", organizationCode);
		primaryMap.put("displayLanguage", getDisplayLanguage(ctx));
		primaryMap.put("uniqId", ctx.sessionMng.getUniqId());
		if( Country.isFeature(organizationCode, "useRevOrd") )
			primaryMap.put("useRevOrd", "Y");

		String[] headerKeys = { "partyCode", "soldPartyCode", "soldPartyName", "partyName", "orderNumber", "reviseOrderKey",
				"divisionCode", "status", "shipPartyCode", "distributionChannelCode", "organizationCode",
				"reviseStatus", "reviseHbrdSeqId", "revHbrdLastDate",  "revHbrdContent", "reviseHelpType", //
				"revHbrdEmail", "lastRegistUserEmail",
				"inDate", "inDateDefault", "simulationKey", "customerOrderNumber", "revisingBaseDate",
		};
		Map<String, Object> headerMap = headerDB.getRecord(primaryMap, headerKeys);

		String parentOrderNumber = (String) SQLManager.getObjectValue(ctx.handler,
				"SELECT ORDER_NUMBER FROM DPR_ORDER WHERE ORDER_KEY = ?", new String[] { origOrderKey });
		if (parentOrderNumber == null)
			throw new ServletModelException(DataException.ERR_CANNOT_NULL);

		com.irt.data.cols.ColumnList columnList = null;
		if( headerMap.get("distributionChannelCode") == null )
			throw new ServletModelException(DataException.ERR_CANNOT_NULL);

		headerMap.put("headwordCode", headerMap.get("reviseHelpType"));
		String headerEmail = (String)headerMap.get("revHbrdEmail");
		if( headerEmail == null || headerEmail.length() <= 0 )
			headerEmail = (String)headerMap.get("lastRegistUserEmail");
		headerMap.put("email", headerEmail);
		ctx.pageConfig.setInputStatus(com.irt.html.HtmlPage.INPUTSTATUS_INFORMATION);

		columnList = getColumnList(ctx, "DPROrder.DETAIL%REVLIST.INFO");

		com.irt.dpr.OrderItem itemDB = new com.irt.dpr.OrderItem(ctx.handler);
		String allowUOM = null;
		if( com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM") )
			allowUOM = itemDB.getDistAllowUOM(headerMap);
		if( allowUOM == null )
			allowUOM = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "uom;" + organizationCode,
					com.irt.dpr.Party.DEFAULT_UOM);

		ctx.pageConfig.setProperty("allowUOM", allowUOM);

		Map<String, Object> conditionMap = createReviseOrderDetailConditionMap(ctx, origOrderKey);

		ServletUtility.setSort(ctx.req, detailDB, columnList.getSortKeys());
		List<Map<String, Object>> recordList = detailDB.getRecords(conditionMap,
				columnList.getFieldKeys("itemCode", "itemCodeConfirmed", "childLineNumber", "itemRefInd", "detailStatus"));
		String conditionKey = pushConditionMapAndSetListIndexVariables(ctx, conditionMap, recordList);
		if( conditionKey != null )
			ctx.pageConfig.setProperty("conditionKey", conditionKey);

		// if( recordList != null && recordList.size() > 0 ) {
		//	recordList = getDetailListPushUOM(ctx, recordList);
		// }

		if( headerMap.get("reviseHelpType") == null )
			throw new ServletModelException( ServletModelException.ERROR );

		ctx.req.setAttribute("header", headerMap);
		ctx.req.setAttribute("records", recordList);
		ctx.req.setAttribute("condition", conditionMap);
		ctx.req.setAttribute("columnList", columnList);

		ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPROrder.MNG"));

		if( ctx.req.getParameter("json") != null )
			return jsonResponse(ctx, "records");
		else
			return forward(ctx, systemConfig.getJspPath() + "/dpr_orderrevise_input.jsp");
	}

	private boolean reviseSendMail( Context ctx ) throws ServletException, IOException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPROrder.DETAIL%REVLIST.INFO");
		ctx.req.setAttribute("columnList", columnList);

		checkThrowParameterNull(ctx, "origOrderKey", "orderKey", "boardNumber");
		String reviseOrderKey = ctx.req.getParameter("orderKey");
		String s_boardNumber = ctx.req.getParameter("boardNumber");

		Integer boardNumber;
		try {
			boardNumber = com.irt.util.Utility2.DataField.toInteger(s_boardNumber);
		} catch( DataException dataEx ) {
			throw new ServletModelException(ServletModelException.INVALID_PARAMETER);
		}

		Map<String, Object> mailContentMap;
		try {
			Map<String, Object> reqMap = Record.createMap("orderKey", reviseOrderKey);
			reqMap.put("uniqId", ctx.sessionMng.getUniqId());
			reqMap.put("userName", ctx.sessionMng.getUserName());
			reqMap.put("displayLanguage", getDisplayLanguage(ctx));
			mailContentMap = headerDB.getMailContentMap(reqMap);
			boolean hasSent = false;
			if( mailContentMap != null ) {
				logger.debug("order revise content map created. orderKey: "+ reviseOrderKey + " boardNumber: "+ boardNumber + " " + mailContentMap);

				javax.mail.Message mailMessage = headerDB.reviseCommitSendMail(mailContentMap);
				try {
					hasSent = (mailMessage.getSentDate() != null ? true : false);
				} catch( MessagingException ignored ) {
				}
				logger.debug("order revise mail send executed(sent?:"+hasSent+"). orderKey: "+ reviseOrderKey + " boardNumber: "+ boardNumber);
			}
			if( hasSent )
				ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_DPR_ORDREV_SENDMAIL_SUCCESS"));
			else
				ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_DPR_ORDREV_SENDMAIL_FAILURE"));
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			logger.error("error.", dataEx);
//			throw new ServletModelException(ServletModelException.ERROR, dataEx.getMessage());
			throw new ServletModelException(dataEx);
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			logger.error("internal error.", sqlEx);
			throw sqlEx;
		}

		return forward(ctx, systemConfig.getJspPath() + "/dpr_orderrevise_input.jsp");
	}

	@Override
	protected boolean simulationResult( Context ctx, String reviseOrderKey ) throws IOException, ServletException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		String backURL = ctx.pageConfig.getBackURL();

		if( reviseOrderKey == null ) {
			checkThrowParameterNull(ctx, "orderKey");
			reviseOrderKey = ctx.req.getParameter("orderKey");
		}

		String origOrderKey = ctx.pageConfig.getProperty("origOrderKey");
		if( origOrderKey == null || origOrderKey.length() == 0 ) {
			origOrderKey = (String)SQLManager.getObjectValue(ctx.handler, "SELECT PARENT_ORDERKEY FROM DPR_ORDER WHERE ORDER_KEY = ?", new Object[] {reviseOrderKey});
			if( origOrderKey == null || origOrderKey.length() == 0 ) {
				checkThrowParameterNull(ctx, "origOrderKey");
			}
		}
		checkThrowParameterNull(ctx, "origOrderKey");

		try {
			if( headerDB.reviseSimulationEnd(reviseOrderKey) ) {
				ctx.handler.commit();
			}
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(dataEx.getMessage());
			logger.error("error.", dataEx);
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(sqlEx.getMessage());
			logger.error("internal error.", sqlEx);
		}

		String redirectURL = ctx.pageConfig.getBackURL();
		redirectURL = HtmlUtility.replaceURLQuery(redirectURL, "mode", "ireg");
		redirectURL = HtmlUtility.replaceURLQuery(redirectURL, "origOrderKey", origOrderKey);
		redirectURL = HtmlUtility.replaceURLQuery(redirectURL, "orderKey", reviseOrderKey);

		ctx.pageConfig.setProperty("isRevOrd", "Y");
		ctx.pageConfig.setProperty("revRedirURL", redirectURL);

		return super.simulationResult(ctx, reviseOrderKey);
	}

	@Override
	protected boolean updateDetail( com.irt.servlet.ServletModel.Context ctx )
			throws java.io.IOException, javax.servlet.ServletException, java.sql.SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		checkThrowParameterNull(ctx, "origOrderKey", "orderKey");
		String origOrderKey = ctx.req.getParameter("origOrderKey");
		String reviseOrderKey = ctx.req.getParameter("oderKey");

		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		int regCnt = registDetailContent(ctx, errorList);
		if( errorList.size() > 0 ) {
			ctx.req.setAttribute("errors", errorList);

			return forward(ctx, systemConfig.getJspPath() + "/error.jsp");
		}

		return super.updateDetail(ctx);
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		checkThrowParameterNull(ctx, "origOrderKey", "orderKey");

		return super.upload(ctx);
	}

	@Override
	protected boolean wait( Context ctx ) throws IOException, ServletException, SQLException {
		OrderRevise headerDB = (OrderRevise)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;
		String type = ctx.req.getParameter("type");
		final String reviseOrderKey = ctx.req.getParameter( "orderKey" );
		ctx.pageConfig.setProperty("isRevOrd", "Y");

		checkThrowParameterNull(ctx, "origOrderKey");
		ctx.pageConfig.setProperty("origOrderKey", ctx.req.getParameter("origOrderKey"));

		if( MODE_SIMULATION.equals(type) || MODE_SHORTAGE_ELIMINATE.equals(ctx.mode) ) {
			if( "sim".equals(type) ) {
				if( readyReviseBeforeSimulation(ctx) ) {
					// should be called before actually change order_detail table
					ctx.handler.commit();
				} else {
					return forward(ctx, systemConfig.getJspPath() + "/error.jsp");
				}
			}
		} else if( MODE_CREATION.equals(type) ) {
			throw new ServletModelException("ERR_ORD_CREATION_INVALID");
		}

		try {
			return super.wait(ctx);
		} finally {
			if( "POST".equals(ctx.req.getMethod()) ) {
				try {
					if( com.irt.sql.SQLManager.executeStatement(ctx.handler, "UPDATE DPR_ORDER_DTL"
									+ " SET REVSIM_IPTQTY = NVL(ORDERQTY, 0)"
									+ " WHERE ORDERKEY = ? AND REVSIM_IPTQTY != ORDERQTY", new Object[] {reviseOrderKey}) > 0 ) {
						ctx.handler.commit();
					}
				} catch( DataException dataEx ) {
					logger.error("error.", dataEx);
					throw new ServletModelException(ServletModelException.INTERNAL_ERROR, dataEx.getMessage());
				}
			}
		}
	}
}
