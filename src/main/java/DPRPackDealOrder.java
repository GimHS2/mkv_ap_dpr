/*
 *	File Name:	DPRPackDealOrder.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2024/08/28		2.2.3	backURL 생성시 isSecure 옵션값을 준수하도록 변경
 *	hankalam	2021/11/30		2.2.2	신규 UI/UX 적용
 *	jbaek		2020/05/30		2.2.1	모든 orderQty가 0일 경우에 exception 처리.
 *	jbaek		2019/10/04		2.2.1	서블렛에서 orderQty 와 simulationOrderQty 값 비교 체크
 *	jbaek		2019/09/30		2.2.1	jsp deleteZero param 삭제. placeOrder시에 체크하고 삭제하도록 변경.
 *	jbaek		2019/05/30		2.2.0	create
 *
**/

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataResult;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.cols.ColumnList;
import com.irt.dpr.Country;
import com.irt.dpr.ItemUOM;
import com.irt.dpr.Order;
import com.irt.dpr.OrderDetail;
import com.irt.dpr.PackDealCfgRlt;
import com.irt.dpr.PackDealItem;
import com.irt.dpr.RddTrigger;
import com.irt.dpr.Upload;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.json.SubmodeManager;
import com.irt.servlet.MultipartHttpRequest;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;
import com.irt.sql.SQLHandler;
import com.irt.util.Arrays;
import com.irt.util.MapUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

@WebServlet( urlPatterns = { "/servlet/DPRPackDealOrder" } )
public class DPRPackDealOrder extends DPROrderServletModel {
	private final static int SYSTEMRDD_PLUS_DAYS = 1;
	private final static int SAPRDDNULL_PLUS_DAYS = 1;// When sap RDD does not have... default value to be 2
	private final static long CHINA_ORDERCLOSING_TIME = 52200000;
	private final static int[] CHINA_HOLIDAYS = new int[] { Calendar.SUNDAY };
	private final static int[] SHANGHAI_HOLIDAYS = new int[] { Calendar.SATURDAY, Calendar.SUNDAY };
	public final static String MODE_PUT = "put";

	private final static String MODE_UPDATEHEADER = "uph";

	private final static String MODE_ORDERINPUT = "ior";

	private final static String MODE_REMOVEDETAIL = "rmd";

	private final static String MODE_WAIT = "wait";

	private final static String MODE_SIMULATION = "sim";

	private final static String MODE_CREATION = "cre";

	private final static String PARAM_LISTTYPE = "ltype";

	private static int ORDERQTY_LOW_LIMIT = com.irt.dpr.Order.PACKDEALORDER_LOW_LIMIT;

	private final static String MODE_UPDATEDETAIL = "upd";

	private SubmodeManager submoder = new SubmodeManager();

	private Map<String, Object> createConditionMap( Context ctx ) throws ServletModelException, SQLException {
		ParameterMap parameterMap = new ParameterMap(ctx.req, true);
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		if( parameterMap.containsKey("dealCode") )
			Condition.putConditionValueOnly(conditionMap, "dealCode", parameterMap.get("dealCode"));

		if( parameterMap.containsKey("distributionChannelCode") )
			conditionMap.put("distributionChannelCode", parameterMap.get("distributionChannelCode"));

		conditionMap.put("organizationCode", getSavedOrganizationCode(ctx));

		conditionMap.put("displayLanguage", getDisplayLanguage(ctx));

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {

		if( isPost ) {
			if( MODE_UPDATEHEADER.equals(ctx.mode) )
				return updateHeader(ctx);
			// else if( MODE_REGISTDETAIL.equals(ctx.mode) ) return registDetail( ctx );
			else if( MODE_UPDATEDETAIL.equals(ctx.mode) )
				return updateDetail(ctx);
			else if( MODE_WAIT.equals(ctx.mode) )
				return wait(ctx);
			// else if( MODE_SHORTAGE_ELIMINATE.equals(ctx.mode) ) return shortageEliminate( ctx );
			// else if( MODE_SHORTAGE_LIST.equals(ctx.mode) ) return shortageList( ctx );
		} else {
			// if( MODE_FRAME.equals(ctx.mode) )
			// return mainFrame(ctx);
			// else if( MODE_LOADTEMPLATE.equals(ctx.mode) ) return loadTemplate( ctx );
			if( MODE_ORDERINPUT.equals(ctx.mode) )
				return orderInput(ctx);
			// else if( MODE_REGISTDETAIL.equals(ctx.mode) ) return registDetail( ctx );
			else if( MODE_REMOVEDETAIL.equals(ctx.mode) )
				return removeDetail(ctx);
			// else if( MODE_SET_TRADEPARTNER.equals(ctx.mode) ) return setTradePartner( ctx );
			else if( MODE_SIMULATION.equals(ctx.mode) )
				return simulationOrder(ctx);
			// else if( MODE_SIMULATION_RESULT.equals(ctx.mode) ) return simulationResult( ctx );
			// else if( MODE_SHORTAGE_DOWNLOAD.equals(ctx.mode) ) return shortageDownload( ctx );
			else if( MODE_CREATION.equals(ctx.mode) )
				return placeOrder(ctx);
			else if( MODE_WAIT.equals(ctx.mode) )
				return wait(ctx);
		}

		// TODO Auto-generated method stub
		return super.doRequest(ctx, isPost);
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		OrderDetail db = (OrderDetail)ctx.extraObj;

		Map<String, Object> conditionMap = createConditionMap(ctx);

		String orderKey = ctx.req.getParameter("orderKey");
		String dealCode = (String)conditionMap.get("dealCode");
		if( conditionMap.get("dealCode") == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if( orderKey == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		conditionMap.put("orderKey", orderKey);

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPROrder.DEALITM%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML);
		String filename = ctx.msghandler.getMessage("TITLE_DPR_PACKDEALORDER_");

		filename += "-" + dealCode;

		conditionMap.put(Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER);

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

	/**
	 * 오더 클로징 감안한 RDD가져오기
	 *
	 * @param ctx
	 * @param rdd
	 *            : SAP에서 받은 RDD(혹은 SAP에서 NULL을 받아서 default로 설정된)
	 * @return : 발주 closing시간을 감안하고 주말등을 감안한 rdd값 을 리턴함
	 */
	private com.irt.data.Date getRequestDeliveryDate( Context ctx, com.irt.data.Date rdd ) {
		boolean calculation = com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "Default;ChinaRDDLogic", false);

		com.irt.data.Date rddDate = com.irt.data.Date.getInstance(rdd);
		String organizationCode = ctx.req.getParameter("organizationCode");

		if( calculation && Country.isFeature(organizationCode, "useCalcOrderClosing") ) {
			Calendar calendar = Calendar.getInstance(ctx.sessionMng.getTimeZone());
			int ordDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			long orderTimeMillis = calendar.getTimeInMillis();

			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			long midnightMillis = calendar.getTimeInMillis();

			int plusDays = 0;
			switch( ordDayOfWeek ) {
			case Calendar.MONDAY:
			case Calendar.TUESDAY:
			case Calendar.WEDNESDAY:
			case Calendar.THURSDAY:
				if( ( midnightMillis + CHINA_ORDERCLOSING_TIME ) < orderTimeMillis )
					plusDays = 1;
				break;
			case Calendar.FRIDAY:
				if( ( midnightMillis + CHINA_ORDERCLOSING_TIME ) < orderTimeMillis )
					plusDays = 3;
				break;
			case Calendar.SATURDAY:
				plusDays = 2;
				break;
			case Calendar.SUNDAY:
				plusDays = 1;
				break;
			}

			rddDate = rddDate.getDate(plusDays);
		}
		return rddDate;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance("DPR");
	}

	public List<Map<String, Object>> getUserPartyPackDealList( Context ctx, Map<String, Object> conditionMap ) throws SQLException {
		String[] fieldKeys = new String[] { "dealCode" };

		if( conditionMap.get("partyCode") == null )
			conditionMap.put("partyCode", "");

		conditionMap.put("hasItem", "Y");
		conditionMap.put("isPackdealDate", "Y");
		conditionMap.put("isSellingSku", "Y");
		PackDealCfgRlt pdrlt = new PackDealCfgRlt(ctx.handler);
		pdrlt.setSort("updateDateTime", "dealCode");
		return pdrlt.getRecords(conditionMap, fieldKeys);
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig(ctx);

		if( MODE_DEFAULT.equals(ctx.mode) )
			pageConfig.setMode(ctx.mode = MODE_ORDERINPUT);
		if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", null);
		// else if( MODE_FRAME.equals(ctx.mode) )
		// pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_UPDATEHEADER.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_ORDERINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_SIMULATION.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_CREATION.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_WAIT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_UPDATEDETAIL.equals(ctx.mode) || MODE_REMOVEDETAIL.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode("DPR", "DPROrder.MNG");
		else
			throw new ServletModelException(ServletModelException.INVALID_MODE);

		ctx.db = new Order(ctx.handler, systemConfig);
		ctx.extraObj = new OrderDetail(ctx.handler);

		pageConfig.setTitle(ctx.msghandler.getMessage("TITLE_DPR_PACKDEALORDER_" + ctx.mode.toUpperCase()));
		setPath( ctx, "jsp.MENU_ORDER" );
	}

	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		return true;
	}

	public boolean orderInput( Context ctx ) throws ServletException, IOException, SQLException {
		setPath( ctx, "jsp.SUBMENU_PACKDEAL_ORDER" );

		Order headerDB = (Order)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		if( "Y".equals(ctx.req.getParameter("isFirstSim")) ) {
			ctx.pageConfig.setProperty("isFirstSim", "Y");
		}

		String dealCode = ctx.req.getParameter("dealCode");

		Map<String, Object> conditionMap = createConditionMap(ctx);

		List<Map<String, Object>> detailList = null;
		String formatType = ctx.req.getParameter(DPRPlaceOrder.PARAM_FORMATTYPE);

		if( formatType == null || formatType.length() == 0 )
			formatType = DPRPlaceOrder.FORMATTYPE_PC;

		Map<String, Object> headerMap = new HashMap<String, Object>();

		if( dealCode != null ) {
			headerMap.put("dealCode", dealCode);
		}

		String orderKey = ctx.req.getParameter("orderKey");
		String organizationCode = getSavedOrganizationCode(ctx);
		ColumnList columnList = null;

		List<String> optionKeyList = new ArrayList<String>();
		if( com.irt.dpr.Country.isFeature(organizationCode, "useMoq") )
			optionKeyList.add("useMoq");

		if( ctx.req.getParameter("orderKey") == null ) {
			String partyCode = ctx.req.getParameter("partyCode");
			if( partyCode == null && ctx.req.getParameter("soldPartyCode") != null ) {
				partyCode = ctx.req.getParameter("soldPartyCode");
			}
			if( partyCode != null ) {
				headerMap.put("soldPartyCode", partyCode);
				headerMap.put("partyCode", partyCode);
			}

			if( true ) {
				Map<String, Object> _conditionMap = new HashMap<String, Object>(conditionMap);
				_conditionMap.putAll(headerMap);
				_conditionMap.remove("status");
				_conditionMap.remove("dealCode");
				List<Map<String, Object>> packdeals = getUserPartyPackDealList(ctx, _conditionMap);
				if( packdeals != null && packdeals.size() > 0 ) {
					ctx.req.setAttribute("packdeals", packdeals);
					headerMap.put("dealCode", packdeals.get(0));
				} else {
					if( partyCode != null ) {
						ctx.pageConfig.setMessage(ctx.handler.getMessageHandler().getMessage("MSG_INFO_NO_PACKDEAL"));
						ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
					}
				}
			}

			columnList = getColumnList(ctx, "DPROrder.DEALITM%LIST");
			com.irt.data.Date today = com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone());
			headerMap = new java.util.HashMap<String, Object>();
			headerMap.put("countryCode", getUserCountryCode(ctx));
			headerMap.put("divisionCode", getDivisionCode(ctx));
			headerMap.put("orderDate", today);
			String inDate = ctx.req.getParameter("inDate");
			if( inDate != null ) {
				try {
					headerMap.put("inDate", com.irt.data.Date.getInstance(inDate));
				} catch( ParseException e ) {
					headerMap.put("inDate", getRequestDeliveryDate(ctx, today.getDate(SYSTEMRDD_PLUS_DAYS)));
				}
			} else {
				headerMap.put("inDate", getRequestDeliveryDate(ctx, today.getDate(SYSTEMRDD_PLUS_DAYS)));
			}

			setDefaultParameter(ctx, headerMap);

			String shipPartyCode = ctx.req.getParameter( "shipPartyCode" );
			if( shipPartyCode != null ) {
				headerMap.put( "shipPartyCode", shipPartyCode );
			}

			if( organizationCode != null && organizationCode.length() > 0 ) {
				List<Map<String, Object>> dists = getUserSoldParties(ctx);
				if( dists == null ) {
					setAttributePartner( ctx, headerMap, PARTNER_SOLD );
				} else if( dists.size() == 1 ) {
					headerMap.putAll(dists.get(0));
					setAttributePartner( ctx, headerMap, PARTNER_SOLD );
				} else if( dists.size() > 1 ){
					List<Object> distChans = MapUtil.extractValueList(dists, "distributionChannelCode");
					java.util.Set<Object> uniqDistChans = new java.util.HashSet<Object>(distChans);
					if( uniqDistChans != null && uniqDistChans.size() == 1 ) {
						headerMap.put("distributionChannelCode", uniqDistChans.iterator().next());
					}

					setAttributePartner( ctx, headerMap, PARTNER_SOLD );
				}
			}

			if( partyCode != null ) {
				headerMap.put("soldPartyCode", partyCode);
				headerMap.put("partyCode", partyCode);
				setAttributePartner(ctx, headerMap, PARTNER_SHIP);
			}

			ctx.req.setAttribute("header", headerMap);

			if( dealCode != null ) {
				PackDealItem item = new PackDealItem(ctx.handler);
				item.setSort(new String[] { "itemName", "itemCode" });
				Map<String, Object> _conditionMap = new HashMap<String, Object>(headerMap);
				Condition.putConditionValueOnly(_conditionMap, "dealCode", dealCode);
				_conditionMap.put("displayLanguage", getDisplayLanguage(ctx));

				detailList = item.getOrderItems(_conditionMap, columnList.getFieldKeys("packSize", "salesUnit", "isStopItem", "stopStartDate",
						"stopEndDate", "isCloseItem", "ordCloseTime", "isSslBase", "isSslOrder"), true);

				if( detailList != null && detailList.size() > 0 ) {
					ctx.req.setAttribute("details", detailList);
				} else {
					ctx.pageConfig.setProperty("hasInitPackdealItem", "N");
					ctx.pageConfig.setMessage(ctx.handler.getMessageHandler().getMessage("MSG_INFO_NO_PACKDEALITEM", dealCode));
					ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
				}
			}
		} else {
			String currStatus = headerDB.checkCurrentStatus(orderKey);
			if( Order.STATUS_CREATED.equals(currStatus) ||
					Order.STATUS_SIMULATED.equals(currStatus) ||
					Order.STATUS_WORKSHEET.equals(currStatus) ) {
			} else {
				if( !headerDB.checkProceedStatus(orderKey, Order.STATUS_WORKSHEET) ) {
					logger.debug("currStatus:" + currStatus);
					throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_ORD_CANNOT_MODIFY_ORDER"));
				}
			}

			conditionMap = Order.createPrimary(orderKey);
			headerMap = headerDB.getRecord(conditionMap);

			if( dealCode == null )
				dealCode = (String)headerMap.get("dealCode");

			if( headerMap.get("dealCode") == null ) {
				if( dealCode != null ) {
					headerMap.put("dealCode", dealCode);
					try {
						headerDB.modify(headerMap);
					} catch( DataException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			if( headerMap.get("status") != null ) {
				optionKeyList.add((String)headerMap.get("status"));
			}

			columnList = getColumnList(ctx, "DPROrder.DEALITM%LIST", optionKeyList.toArray(new String[0]));
			ServletUtility.setSort(ctx.req, detailDB, columnList.getSortKeys());

			conditionMap.put("displayLanguage", getDisplayLanguage(ctx));
			conditionMap.put("formatUOM", formatType.toUpperCase());
			conditionMap.put(Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER);

			com.irt.dpr.OrderItem itemDB = new com.irt.dpr.OrderItem(ctx.handler);
			Map<String, Object> primaryMap = com.irt.dpr.Party.createPrimary((String)headerMap.get("soldPartyCode"),
					(String)headerMap.get("organizationCode"), (String)headerMap.get("distributionChannelCode"),
					getDivisionCode(ctx));
			String allowUOM = null;
			if( com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM") )
				allowUOM = itemDB.getDistAllowUOM(primaryMap);
			if( allowUOM == null )
				allowUOM = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "uom;" + organizationCode, com.irt.dpr.Party.DEFAULT_UOM);
			ctx.pageConfig.setProperty("allowUOM", allowUOM);

			com.irt.data.Date availableDate = com.irt.data.Date.getInstance();
			conditionMap.put("availableDate", availableDate);
			if( Country.isFeature(organizationCode, "useSuggestSalesUnitInput") ) {
				conditionMap.put("useSuggestSalesUnitInput", "Y");
			}

			String[] extraFieldKeys = new String[] { "lineNumber", "orderQtySimulation", //
					"packdealDisplaySeq", "packdealDiscountRate", "dealCode", "simulationOrderQty", "simulationOrderValue", "simulationUOM",
					"salesUnit"
			};
			if( "CD".equals(currStatus) ) {
				extraFieldKeys = new String[] { "lineNumber", "orderQtySimulation", //
						"packdealDisplaySeq", "packdealDiscountRate", "dealCode", //
						"simulationOrderQty", "simulationOrderValue", "confirmedOrderQty", "confirmedOrderValue", "simulationUOM", "confirmedUOM" };
			}
			extraFieldKeys = Arrays.append(extraFieldKeys, new String[] { "pdRmnMonth", "pdRmnDay" });

			if( dealCode != null )
				conditionMap.put("dealCode", dealCode);

			String[] fieldKeys = columnList.getFieldKeys(extraFieldKeys);
			detailList = detailDB.getRecords(conditionMap, fieldKeys);

			if( detailList == null ) {
				if( dealCode != null ) {
					PackDealItem item = new PackDealItem(ctx.handler);
					Map<String, Object> _conditionMap = MapUtil.getPartialMap(headerMap, new String[] {
							"organizationCode", "partyCode", "distributionChannelCode", "divisionCode", "dealCode"
					});
					Condition.putConditionValueOnly(_conditionMap, "dealCode", dealCode);

					detailList = item.getOrderItems(_conditionMap,
							columnList.getFieldKeys("packSize", "salesUnit", "isStopItem", "isCloseItem", "isSslBase", "isSslOrder"), true);
				}
			}

			if( detailList != null && detailList.size() > 0 ) {
				ArrayList<String> lineNumberList = new ArrayList<String>();
				int detailListSize = detailList.size();

				for( int i = 0; i < detailListSize; i++ ) {
					Map<String, Object> detailMap = detailList.get(i);
					String plantInd = (String)detailMap.get("plantInd");
					String itemDisplayInd = (String)detailMap.get("itemDisplayInd");
					if( ( plantInd != null && plantInd.length() > 0 && "Y".equals(plantInd) )
							|| ( itemDisplayInd != null && itemDisplayInd.length() > 0 && "N".equals(itemDisplayInd) ) ) {
						lineNumberList.add(detailMap.get("lineNumber").toString());
					}
				}
				if( lineNumberList.size() > 0 ) {
					ctx.pageConfig.setProperty("isPlantInd", "Y");

					String[] lineNumbers = lineNumberList.toArray(new String[lineNumberList.size()]);

					if( !( (Order)ctx.db ).lockOrder(orderKey) )
						throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_RECORD_LOCKED"));

					Map<String, Object> deleteMap = new HashMap<String, Object>();
					deleteMap.put("orderKey", orderKey);

					Object status = ( (Order)ctx.db ).getFieldValue(Order.createPrimary(orderKey), "status");
					if( Order.STATUS_SIMULATING.equals(status) || Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
						throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_DELETE_STATUS"));
					else if( Order.STATUS_SIMULATED.equals(status) ) {
						// ( (Order)ctx.db ).updateStatusWithDetail(orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId());
					} else if( Order.STATUS_ERROR.equals(status) )
						( (Order)ctx.db ).updateStatus(orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId());

					try {
						if( detailDB.deleteWithLineNoUpdate(orderKey, lineNumbers) <= 0 )
							throw new DataException( DataException.ERR_CANNOT_DELETE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE) );
					} catch( DataException dataEx ) {
						ctx.handler.rollback();
						// throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE) );
					} catch( SQLException sqlEx ) {
						ctx.handler.rollback();
						// throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE) );
					}

					ctx.handler.commit();

					detailList = detailDB.getRecords(conditionMap,
							columnList.getFieldKeys(new String[] { "lineNumber", "orderQtySimulation" }));
				}
			}

			Map<String, Object> tradePartnerMap = new java.util.HashMap<String, Object>();
			if( headerMap != null ) {
				if( !com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()).equals(headerMap.get("orderDate"))
						&& !headerDB.checkProceedStatus(orderKey, Order.STATUS_SIMULATED) )
					throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_ORD_CANNOT_MODIFY_ORDER"));

				tradePartnerMap.put("organizationCode", headerMap.get("organizationCode"));
				tradePartnerMap.put("distributionChannelCode", headerMap.get("distributionChannelCode"));
				tradePartnerMap.put("soldPartyCode", headerMap.get("soldPartyCode"));
				tradePartnerMap.put("shipPartyCode", headerMap.get("shipPartyCode"));
			}
			setAttributePartner(ctx, tradePartnerMap);
		}

		if( dealCode != null )
			headerMap.put("dealCode", dealCode);

		if( dealCode != null ) {
			PackDealCfgRlt rlt = new PackDealCfgRlt(ctx.handler);
			Map map = MapUtil.getPartialMap(headerMap, new String[] { "organizationCode", "distributionChannelCode", "partyCode" });
			map.put("dealCode", dealCode);
			map.put("isPackdealDate", "Y");
			List<Map<String, Object>> packdealList = rlt.getRecords(map,
					new String[] { "dealCode", "targetTotalAmount", "toleranceRate", "isPackdealDate" });
			if( packdealList != null && packdealList.size() > 0 ) {
				Map<String, Object> packdealMap = packdealList.get(0);
				ctx.req.setAttribute("packdeal", packdealMap);
			} else {
			}
		}

		{
			String[] dateFieldKeys = { "orderDate", "inDate", "inDateDefault", "inDateSimulation", "inDateConfirm" };
			for( String fieldKey : dateFieldKeys ) {
				java.util.Date dt = (java.util.Date)headerMap.get(fieldKey);
				if( dt != null ) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(dt);
					int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
					headerMap.put(fieldKey + "DOW", ctx.msghandler.getMessage("jsp.dpr_order_input.MSG_DAYOFWEEK_" + dayOfWeek));
				}
			}
		}

		ctx.req.setAttribute("header", headerMap);
		ctx.req.setAttribute("headerFieldSet", headerDB.getFieldSet(( orderKey == null || orderKey.length() == 0 )));
		// ctx.req.setAttribute("packdealFieldSet", packdeal.getFieldSet(( orderKey == null || orderKey.length() == 0 )));
		ctx.req.setAttribute("details", detailList);
		ctx.req.setAttribute("columnList", columnList);
		setAttributePartyMaster(ctx, headerMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL);

		ctx.pageConfig.getListIndexVariables()[2] = ( detailList != null ? detailList.size() : 0 );
		if( com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()).equals(headerMap.get("orderDate")) )
			ctx.pageConfig.setManageAuth(ctx.sessionMng.isAuthorized("DPR", "DPROrder.MNG"));
		else
			ctx.pageConfig.setManageAuth(false);

		if( !"Y".equals(ctx.req.getParameter("isContinueOrder")) ) {
			if( headerDB.checkCreatingStatus((String)headerMap.get("soldPartyCode"), (String)headerMap.get("shipPartyCode")) )
				ctx.pageConfig.setProperty("hasCreatingOrder", "Y");
		}
		String message = ctx.req.getParameter("msg");
		if( message != null && message.length() > 0 ) {
			ctx.pageConfig.setMessage(message);
		}

		if( detailList != null ) {
			try {
				( new com.irt.dpr.ItemUOM(ctx.handler) ).pushUOM(detailList);
				for( Map<String, Object> dtl : detailList ) {
					List<Map<String, Object>> uoms = (java.util.ArrayList)dtl.get("uoms");

					String itemCode = (String)dtl.get("itemCode");
					String salesUnit = (String)dtl.get("salesUnit");
					String uom = (String)dtl.get("uom");
					if( uom == null ) {
						if( salesUnit != null ) {
							uom = salesUnit;
						} else {
							uom = getDefaultUnitOfMeasure(ctx);
						}
					}

					String simulationUOM = (String)dtl.get("simulationUOM");
					String infoUOM = (String)dtl.get("infoUOM");

					if( uoms != null ) {
						boolean ipt_loop_done = ( uom == null );
						boolean sim_loop_done = ( simulationUOM == null );
						boolean cre_loop_done = ( infoUOM == null );
						for( Map<String, Object> m : uoms ) {
							if( dtl.get("packSize") == null ) {
								if( itemCode != null && itemCode.equals(m.get("itemCode")) ) {
									if( uom != null && uom.equals(m.get("uomCode")) ) {
										dtl.put("packSize", m.get("packSize"));
										ipt_loop_done = true;
									}
								}
							}
							if( dtl.get("simulationPackSize") == null ) {
								if( itemCode != null && itemCode.equals(m.get("itemCode")) ) {
									if( simulationUOM != null && simulationUOM.equals(m.get("uomCode")) ) {
										dtl.put("simulationPackSize", m.get("packSize"));
										sim_loop_done = true;
									}
								}
							}
							if( dtl.get("confirmedPackSize") == null ) {
								if( itemCode != null && itemCode.equals(m.get("itemCode")) ) {
									if( infoUOM != null && infoUOM.equals(m.get("uomCode")) ) {
										dtl.put("confirmedPackSize", m.get("packSize"));
										cre_loop_done = true;
									}
								}
							}
							if( ipt_loop_done == true && sim_loop_done == true && cre_loop_done == true ) {
								break;
							}
						}
					}
				}
			} catch( Exception ex ) {
			}
			ctx.req.setAttribute("details", detailList);
		}

		// forward
		return forward(ctx, systemConfig.getJspPath() + "/dpr_packdealorder_input.jsp");
	}

	protected boolean placeOrder( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		String orderKey = ctx.req.getParameter("orderKey");

		if( Order.STATUS_CREATING.equals(db.checkCurrentStatus(orderKey)) ) {
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_WAITING_CREATION_AGAIN"));

			String url = ctx.pageConfig.getBackURL();
			url = HtmlUtility.replaceURLQuery(url, "mode", "wait");
			return sendRedirect(ctx, HtmlUtility.replaceURLQuery(url, PARAM_MESSAGE_KEY, saveMessage(ctx)));
		}

		String backUrl = ctx.pageConfig.getBackURL();
		if( backUrl != null ) {
			ctx.pageConfig.setBackURL(HtmlUtility.replaceURLQuery(backUrl, "isFirstSim", "N"));
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("orderKey", orderKey);
		List<Map<String, Object>> recordList = detailDB.getRecords(conditionMap, new String[] { "orderQty", "simulationOrderQty" });
		if( recordList != null && recordList.size() > 0 ) {
			for( Map<String, Object> map : recordList ) {
				BigDecimal orderQty = (BigDecimal)map.get("orderQty");
				if( orderQty == null || orderQty.intValue() == 0 ) continue;
				BigDecimal simulationOrderQty = (BigDecimal)map.get("simulationOrderQty");

				if( simulationOrderQty == null || orderQty.intValue() > simulationOrderQty.intValue() ) {
					throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_PACKDEALORDER_INPUTQTY_GT_SIMULATIONQTY"));
				}
			}
		}

		try {
			int deletedCount = detailDB.deleteZeroOrderQty(orderKey);
			if( recordList != null && recordList.size() == deletedCount ) {
				throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_ORD_CREATION_NO_DATA_CONTINUE") );
			}
		} catch( DataException dataEx ) {
			throw new ServletModelException(ServletModelException.ERROR, dataEx.getMessage());
		}

		if( !db.checkProceedStatus(orderKey, Order.STATUS_CREATED) )
			throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_ORD_CANNOT_PROCEED_CREATION"));

		SQLHandler this_handler = null;
		try {
			com.irt.custom.SystemConfig sysConfig = com.irt.custom.SystemConfig.getInstance("RBM");
			this_handler = sysConfig.createSQLHandler(sysConfig.getMessageHandler(ctx.locale));
			if( this_handler == null )
				throw new ServletModelException(ServletModelException.INVALID_DATAHANDLER);

			if( !db.updateStatus(this_handler, orderKey, Order.STATUS_CREATING, ctx.sessionMng.getUniqId()) ) {
				logger.debug("error.: " + orderKey + " : " + "ERR_CANNOT_UPDATE_STATUS" + "CREATING");
				throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_STATUS", "CREATING"));
			} else {
				logger.debug("debug.: " + orderKey + " db.updateStatus commit.");
				this_handler.commit();
			}
		} catch( Exception e ) {
			logger.debug("error.: " + orderKey, e);
		} finally {
			try {
				if( this_handler != null )
					this_handler.close();
			} catch( Exception ignored ) {
			}
		}

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		String redirectURL = ctx.pageConfig.getBackURL();

		try {
			if( !executePlaceOrderProcess(ctx, orderKey, com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_CREATION) )
				throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("MSG_CREATION_FAILED"));
			else {
				Map<String, Object> result = db.getRecord(Order.createPrimary(orderKey), new String[] { "status", "orderNumber" });
				if( Order.STATUS_CREATED.equals(result.get("status")) && result.get("orderNumber") != null ) {
					Map<String, Object> statusListCondition = new HashMap<String, Object>();
					statusListCondition.put("startOrderDate", com.irt.data.Date.getInstance());
					statusListCondition.put("endOrderDate", com.irt.data.Date.getInstance().getDate(1));
					statusListCondition.put("orderNumber", result.get("orderNumber"));
					executeStatusList(ctx, statusListCondition);

					redirectURL = systemConfig.getClassURL() + "/DPREnquiryOrder?" + ctx.req.getQueryString();
					redirectURL = HtmlUtility.replaceURLQuery(redirectURL, "mode", "info");
					redirectURL = HtmlUtility.replaceURLQuery(redirectURL, "create", "Y");
					redirectURL = HtmlUtility.replaceURLQuery(redirectURL, "status", Order.STATUS_CREATED);

					ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_CREATION_SUCCESS"));
				} else {
					ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_CREATION_FAILED"));
				}
			}
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(dataEx.getMessage());
		}

		return sendRedirect(ctx, HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)));
	}

	protected boolean removeDetail( Context ctx ) throws IOException, ServletException, SQLException {
		String[] lineNumbers = ctx.req.getParameterValues("lineNumber");
		List<Map<String, Object>> errorList = removeDetail(ctx, lineNumbers);

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute("errors", errorList);

			return forward(ctx, systemConfig.getJspPath() + "/error.jsp");
		} else {
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", new String[] { String.valueOf(lineNumbers.length) }));

			return sendRedirect(ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)));
		}
	}

	private List<Map<String, Object>> removeDetail( Context ctx, String[] lineNumbers ) throws ServletModelException, SQLException {
		OrderDetail db = (OrderDetail)ctx.extraObj;
		Map<String, Object> primaryMap = new java.util.HashMap<String, Object>();
		String orderKey = ctx.req.getParameter("orderKey");

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if( lineNumbers == null || lineNumbers.length == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		if( !( (Order)ctx.db ).lockOrder(orderKey) )
			throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_RECORD_LOCKED"));

		primaryMap.put("orderKey", orderKey);

		boolean noStatus = ( "Y".equals(ctx.req.getParameter("noStatus")) || "Y".equals(ctx.req.getParameter("deleteZero")) );

		Object status = ( (Order)ctx.db ).getFieldValue(Order.createPrimary(orderKey), "status");
		if( Order.STATUS_SIMULATING.equals(status) || Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
			throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_DELETE_STATUS"));
		else {
			if( !noStatus ) {
				if( Order.STATUS_SIMULATED.equals(status) )
					( (Order)ctx.db ).updateStatusWithDetail(orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId());
				else if( Order.STATUS_ERROR.equals(status) )
					( (Order)ctx.db ).updateStatus(orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId());
			}
		}

		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

		// for( int i = lineNumbers.length - 1; i >= 0; i-- ) {
		// primaryMap.put("lineNumber", lineNumbers[i]);
		// try {
		// if( !db.delete(primaryMap) ) {
		// throw new DataException(DataException.ERR_CANNOT_DELETE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE));
		// }
		// } catch( DataException dataEx ) {
		// ctx.handler.rollback();
		// String itemCode = (String)db.getFieldValue(primaryMap, "itemCode");
		// errorList.add(createErrorMap(( new com.irt.dpr.Item(ctx.handler) ).getName(itemCode, getDisplayLanguage(ctx)), dataEx));
		// } catch( SQLException sqlEx ) {
		// ctx.handler.rollback();
		// String itemCode = (String)db.getFieldValue(primaryMap, "itemCode");
		// errorList.add(createErrorMap(( new com.irt.dpr.Item(ctx.handler) ).getName(itemCode, getDisplayLanguage(ctx)), sqlEx));
		// }
		// }
		try {
			if( db.deleteWithLineNoUpdate(orderKey, lineNumbers) <= 0 )
				throw new DataException(DataException.ERR_CANNOT_DELETE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE));
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			// throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE) );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			// throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE) );
		}

		if( errorList.size() == 0 ) {
			ctx.handler.commit();
		} else {
			ctx.handler.rollback();
		}

		return errorList;
	}

	protected boolean simulationOrder( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;

		String backURL = new StringBuffer(((HtmlPage)ctx.pageConfig).getRequestURL()).append("?").append(ctx.req.getQueryString()).toString();
		backURL = HtmlUtility.replaceURLQuery(backURL, PARAM_MODE, MODE_WAIT);

		String orderKey = ctx.req.getParameter("orderKey");
		if( orderKey == null || orderKey.length() == 0 ) {
			ctx.pageConfig.setBackURL(backURL);

			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		} else {
			backURL = HtmlUtility.replaceURLQuery(backURL, "orderKey", orderKey);
			ctx.pageConfig.setBackURL(backURL);
		}

		String currStatus = db.checkCurrentStatus(orderKey);
		if( Order.STATUS_CREATING.equals(currStatus) ) {
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_WAITING_CREATION_AGAIN"));

			String url = ctx.pageConfig.getBackURL();
			// url = HtmlUtility.replaceURLQuery(url, PARAM_MODE, MODE_SIMULATION_RESULT);
			url = HtmlUtility.replaceURLQuery(url, PARAM_MODE, MODE_ORDERINPUT);
			return sendRedirect(ctx, HtmlUtility.replaceURLQuery(url, PARAM_MESSAGE_KEY, saveMessage(ctx)));
		}

		try {
			if( !db.checkProceedStatus(orderKey, Order.STATUS_SIMULATED) ) {
				throw new ServletModelException(ServletModelException.ERROR,
						ctx.msghandler.getMessage("ERR_ORD_CANNOT_PROCEED_SIMULATION", currStatus));
			} else {
				if( !executePlaceOrderProcess(ctx, orderKey, com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_SIMULATION) )
					throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("MSG_SIMULATION_FAILED"));
			}
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(dataEx.getMessage());
		}

		String url = ctx.pageConfig.getBackURL();
		url = HtmlUtility.replaceURLQuery(url, PARAM_MODE, MODE_ORDERINPUT);

		if( "Y".equals(ctx.req.getParameter("isFirstSim")) ) {
			url += "&isFirstSim=Y";
		}

		return sendRedirect(ctx, HtmlUtility.replaceURLQuery(url, PARAM_MESSAGE_KEY, saveMessage(ctx)));
	}

	/**
	 ** DPR_ORDER_DTL: UPDATE
	 **/
	protected boolean updateDetail( Context ctx ) throws IOException, ServletException, SQLException {
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

		// OrderDetail db = (OrderDetail)ctx.extraObj;
		// String orderKey = ctx.req.getParameter("orderKey");
		// boolean isFirst = ( db.getRecordCount(Order.createPrimary(orderKey)) <= 0 );
		int count = updateDetailContent(ctx, errorList);

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute("errors", errorList);

			return forward(ctx, systemConfig.getJspPath() + "/error.jsp");
		} else {
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS", new String[] { String.valueOf(count) }));
			return sendRedirect(ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)));
		}
	}

	protected int updateDetailContent( Context ctx, List<Map<String, Object>> errorList ) throws IOException, ServletException, SQLException {
		OrderDetail db = (OrderDetail)ctx.extraObj;
		Collection<Map<String, Object>> records = ( new ParameterMap(ctx.req) ).extractGroupList("value");
		String orderKey = ctx.req.getParameter("orderKey");
		if( records == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		else if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		if( !( (Order)ctx.db ).lockOrder(orderKey) )
			throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_RECORD_LOCKED"));

		Object status = ( (Order)ctx.db ).getFieldValue(Order.createPrimary(orderKey), "status");
		if( Order.STATUS_SIMULATING.equals(status) || Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
			throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_STATUS"));
		else if( Order.STATUS_SIMULATED.equals(status) ) {
			// ( (Order)ctx.db ).updateStatusWithDetail(orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId());
		} else if( Order.STATUS_ERROR.equals(status) )
			( (Order)ctx.db ).updateStatus(orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId());

		List<String> delLineNumbers = new ArrayList<String>();
		if( "Y".equals(ctx.req.getParameter("deleteZero")) ) {
			for( Map<String, Object> obj : records ) {
				if( obj.get("orderQty") == null || "0".equals(obj.get("orderQty")) ) {
					delLineNumbers.add((String)obj.get("lineNumber"));
				}
			}
			if( delLineNumbers.size() > 0 ) {
				List<Map<String, Object>> removeErrorList = removeDetail(ctx, delLineNumbers.toArray(new String[0]));
				if( removeErrorList.size() > 0 ) {
					ctx.req.setAttribute("errors", removeErrorList);

					throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_DELETE"));
				}
			}
		}

		ItemUOM uomDB = new ItemUOM(ctx.handler);
		int count = 0;
		int newLnIndex = 1;
		for( Map<String, Object> obj : records ) {
			if( delLineNumbers.contains(obj.get("lineNumber")) )
				continue;

			Map<String, Object> recordMap = obj;
			recordMap.put("lineNumber", newLnIndex * 10);

			String dealCode = ctx.req.getParameter("dealCode");
			recordMap.put("dealCode", dealCode);

			PackDealItem itemDB = new PackDealItem(ctx.handler);

			try {
				recordMap.put("orderKey", orderKey);
				if( recordMap.get("status") == null || "null".equals(recordMap.get("status")) )
					recordMap.put("status", OrderDetail.STATUS_NORMAL);
				if( recordMap.get("itemRefInd") == null || "null".equals(recordMap.get("itemRefInd")) )
					recordMap.put("itemRefInd", OrderDetail.ITEMREF_NORMAL);
				if( recordMap.get("childLineNumber") == null || "null".equals(recordMap.get("childLineNumber")) )
					recordMap.put("childLineNumber", OrderDetail.CHILD_LINENUMBER_NORMAL);

				// Orde Qty 제한
				int orderQty = -1;
				int registeredQty = 0;
				try {
					orderQty = Integer.parseInt((String)recordMap.get("orderQty"));
					if( recordMap.containsKey("tmp_orderQty") ) {
						registeredQty = Integer.parseInt((String)recordMap.get("tmp_orderQty"));
					} else {
						registeredQty = orderQty;
					}
				} catch( NumberFormatException numberEx ) {
					errorList.add(createErrorMap(itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)),
							ctx.msghandler.getMessage("ERR_INVALID_NUMBER")));
				}

				int simulationOrderQty = orderQty;
				if( recordMap.get("simulationOrderQty") != null ) {
					simulationOrderQty = Integer.parseInt((String)recordMap.get("simulationOrderQty"));
				}

				String uom = (String)recordMap.get("uom");
				if( orderQty < ORDERQTY_LOW_LIMIT ) {
					if( orderQty < ORDERQTY_LOW_LIMIT )
						errorList.add(createErrorMap(itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)),
								ctx.msghandler.getMessage("ERR_ORD_ORDER_LOW_LIMIT", String.valueOf(ORDERQTY_LOW_LIMIT))));
					// } else if( registeredQty != simulationOrderQty && orderQty > simulationOrderQty ) {
					// errorList.add(createErrorMap(itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)),
					// ctx.msghandler.getMessage("ERR_ORD_ORDER_HIGH_LIMIT_THAN_SIMUALTION_QTY", String.valueOf(simulationOrderQty))));
				} else if( !FORMATTYPE_PC.equals(uom) && orderQty > com.irt.dpr.Order.ORDER_HIGH_LIMIT ) {
					if( orderQty > com.irt.dpr.Order.ORDER_HIGH_LIMIT )
						errorList.add(createErrorMap(itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)),
								ctx.msghandler.getMessage("ERR_ORD_ORDER_HIGH_LIMIT")));
				} else {
					String itemCodeNew = (String)recordMap.get("itemCodeNew");
					String itemCode = (String)recordMap.get("itemCode");

					if( recordMap.get("itemCodeNew") != null && !itemCodeNew.equals(recordMap.get("itemCode")) ) {
						Map<String, Object> recordMapNew = new java.util.HashMap<String, Object>(recordMap);
						recordMapNew.remove("itemCode");
						recordMapNew.remove("itemCodeConfirmed");
						recordMapNew.put("itemCode", itemCodeNew);
						recordMapNew.put("itemCodeConfirmed", itemCodeNew);

						recordMap.put("packSize", uomDB.getPackSize(itemCodeNew, (String)recordMap.get("uom")));
						if( db.modify(recordMapNew) || db.regist(recordMapNew) )
							count++;
						else
							errorList.add(createErrorMap(itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)),
									ctx.msghandler.getMessage("ERR_CANNOT_UPDATE")));
					} else {
						recordMap.put("packSize", uomDB.getPackSize(itemCode, (String)recordMap.get("uom")));

						if( recordMap.get("itemCodeConfirmed") == null || "null".equals(recordMap.get("itemCodeConfirmed")) ) {
							if( itemCodeNew != null && !"null".equals(itemCodeNew) ) {
								recordMap.put("itemCodeConfirmed", itemCodeNew);
							} else {
								recordMap.put("itemCodeConfirmed", itemCode);
							}
						}

						if( db.modify(recordMap) || db.regist(recordMap) )
							count++;
						else
							errorList.add(createErrorMap(itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)),
									ctx.msghandler.getMessage("ERR_CANNOT_UPDATE")));
					}
				}
			} catch( DataException dataEx ) {
				errorList.add(createErrorMap(itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)), dataEx));
			} catch( SQLException sqlEx ) {
				errorList.add(createErrorMap(itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)), sqlEx));
			}
			newLnIndex++;
		}

		return count;
	}

	protected boolean updateHeader( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;
		Map<String, Object> recordMap = new ParameterMap(ctx.req);
		String countryCode = getUserCountryCode(ctx);
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);
		ctx.req.setAttribute("header", recordMap);

		boolean insert = false;

		String dealCode = ctx.req.getParameter("dealCode");
		if( dealCode != null )
			recordMap.put("dealCode", dealCode);

		String orderKey = Record.extractString(recordMap, "orderKey");
		String distributionChannelCode = (String)recordMap.get("distributionChannelCode");
		if( distributionChannelCode == null || distributionChannelCode.length() == 0 )
			distributionChannelCode = getDistributionChannelCode(ctx);
		if( orderKey == null || orderKey.length() == 0 ) {
			insert = true;
			orderKey = db.getOrderKey();
		} else if( !db.checkProceedStatus(orderKey, Order.STATUS_WORKSHEET) )
			throw new ServletModelException(ServletModelException.INVALID_PARAMETER);

		try {
			com.irt.data.Date orderDate = com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone());
			com.irt.data.Date inDate = null;
			try {
				inDate = com.irt.data.Date.getInstance((String)recordMap.get("inDate"));
				if( inDate != null && orderDate.after(inDate) )
					throw new DataException(DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_INVALID_INDATE_ABOUT_ORDERDATE"));
			} catch( java.text.ParseException parseEx ) {
				throw new DataException(DataException.ERR_INVALID_DATE, ctx.msghandler.getMessage(DataException.ERR_INVALID_DATE));
			}

			com.irt.dpr.tools.OrderCanonicalProcess ocp = new com.irt.dpr.tools.OrderCanonicalProcess(ctx.handler, systemConfig,
					com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_RDD);
			Map<String, Object> infMap = new java.util.HashMap<String, Object>();
			com.irt.data.Date inDateSystemDefault = getRequestDeliveryDate(ctx,
					com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()).getDate(SYSTEMRDD_PLUS_DAYS));
			boolean useInDateAsRDDFrSAP;
			boolean useSelectRdd = false;
			if( insert ) {
				useSelectRdd = new RddTrigger(ctx.handler)
						.isPredefiendRdd((String)recordMap.get("organizationCode"), distributionChannelCode, getDivisionCode(ctx),
								(String)recordMap.get("soldPartyCode"), (String)recordMap.get("shipPartyCode"));
			}

			if( !useSelectRdd && !Country.isFeature(getSavedOrganizationCode(ctx), "useInputRDD") )
				useInDateAsRDDFrSAP = true;// inDate = inDateDefault Value
			else
				useInDateAsRDDFrSAP = false;// inDate = userInputRDD Value

			if( insert ) {
				infMap.put("orderKey", orderKey);
				infMap.put("countryCode", getUserCountryCode(ctx));
				infMap.put("organizationCode", recordMap.get("organizationCode"));
				infMap.put("distributionChannelCode", distributionChannelCode);
				infMap.put("divisionCode", getDivisionCode(ctx));
				infMap.put("shipPartyCode", recordMap.get("shipPartyCode"));
				infMap.put("updateUserId", ctx.sessionMng.getUniqId());
				ocp.setParameter(infMap);

				String message = null;
				com.irt.data.Date inDateDefault = null;
				try {
					String inDateDefaultStr = ocp.execute();

					if( inDateDefaultStr == null || inDateDefaultStr.length() == 0 ) {
						if( useSelectRdd ) {
							inDateDefault = com.irt.data.Date.getInstance(orderDate.getDate(SAPRDDNULL_PLUS_DAYS));
							;
							logger.debug("updateHeader-RDD-insert: " + " inDateDefaultStr=null " + "useSelectRdd=true" + " useInDateAsRDDFrSAP=false "
									+ "inDate=" + inDate + " inDateDefault=" + inDateDefault);
						} else if( useInDateAsRDDFrSAP ) {
							inDate = inDateDefault = com.irt.data.Date.getInstance(orderDate.getDate(SAPRDDNULL_PLUS_DAYS));
							logger.debug("updateHeader-RDD-insert: " + " inDateDefaultStr=null " + "useSelectRdd=false" + " useInDateAsRDDFrSAP=true "
									+ "inDate=" + inDate + " inDateDefault=" + inDateDefault);
						} else {
							inDate = inDateDefault = inDateSystemDefault;
							logger.debug("updateHeader-RDD-insert: " + " inDateDefaultStr=null " + "useSelectRdd=false"
									+ " useInDateAsRDDFrSAP=false " + "inDate=" + inDate + " inDateDefault=" + inDateDefault);
						}
					} else {
						inDateDefault = getRequestDeliveryDate(ctx, com.irt.data.Date.getInstance(inDateDefaultStr));
						if( !useSelectRdd ) {
							inDate = inDateDefault;
						}

						logger.debug("updateHeader-RDD-insert: " + " inDateDefaultStr=not null " + " inDate=" + inDate);
					}
				} catch( com.irt.dpr.OrderProcessException opEx ) {
					message = ctx.msghandler.getMessage(opEx.getErrorKey());
				} catch( java.text.ParseException parseEx ) {
					message = parseEx.getMessage();
				} finally {
					if( inDateDefault == null ) {
						if( useSelectRdd ) {
							inDateDefault = com.irt.data.Date.getInstance(orderDate.getDate(SAPRDDNULL_PLUS_DAYS));
							logger.debug("updateHeader-RDD-insert: " + " inDateDefault=null " + " useInDateAsRDDFrSAP=true " + "inDate=" + inDate
									+ " inDateDefault=" + inDateDefault);
						} else if( useInDateAsRDDFrSAP ) {
							inDate = inDateDefault = com.irt.data.Date.getInstance(orderDate.getDate(SAPRDDNULL_PLUS_DAYS));
							logger.debug("updateHeader-RDD-insert: " + " inDateDefault=null " + " useInDateAsRDDFrSAP=true " + "inDate=" + inDate
									+ " inDateDefault=" + inDateDefault);
						} else {
							inDateDefault = inDateSystemDefault;
							logger.debug("updateHeader-RDD-insert: " + " inDateDefault=null " + " useInDateAsRDDFrSAP=false " + "inDate=" + inDate
									+ " inDateDefault=" + inDateDefault);
						}
					}
				}

				recordMap.put("orderKey", orderKey);
				recordMap.put("partyCode", recordMap.get("soldPartyCode"));
				recordMap.put("countryCode", countryCode);
				recordMap.put("orderDate", orderDate);
				recordMap.put("inDate", inDate);
				recordMap.put("inDateDefault", inDateDefault);
				recordMap.put("distributionChannelCode", distributionChannelCode);
				recordMap.put("divisionCode", getDivisionCode(ctx));
				recordMap.put("orderType", Order.ORDER_TYPE_NORMAL);
				recordMap.put("updateUserId", ctx.sessionMng.getUniqId());

				if( message == null || message.length() == 0 )
					recordMap.put("status", Order.STATUS_WORKSHEET);
				else {
					recordMap.put("message", message);
					recordMap.put("status", Order.STATUS_ERROR);
				}

				if( !db.regist(recordMap) )
					throw new DataException(DataException.ERR_CANNOT_INSERT, ctx.msghandler.getMessage(DataException.ERR_CANNOT_INSERT));

				if( message != null && message.length() > 0 )
					ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_ERROR_RDD_INTERFACE", new String(message)));
			} else {
				Map<String, Object> headerMap = db.getRecord(Order.createPrimary(orderKey));

				com.irt.data.Date inDateDefault = (com.irt.data.Date)headerMap.get("inDateDefault");
				String status = Record.extractString(headerMap, "status");
				if( status == null || status.length() == 0
						|| Order.STATUS_SIMULATING.equals(status)
						|| Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
					throw new DataException(DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_STATUS"));

				recordMap.put("inDate", inDate);
				recordMap.put("inDateDefault", inDateDefault);
				recordMap.put("distributionChannelCode", distributionChannelCode);
				recordMap.put("divisionCode", getDivisionCode(ctx));
				recordMap.put("status", status);

				if( Order.STATUS_SIMULATED.equals(status) ) {
					recordMap.put("inDateSimulation", headerMap.get("inDateSimulation"));
				}

				if( !db.modify(recordMap) )
					throw new DataException(DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE));

				//ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS"));
			}

			ctx.req.setAttribute("header", db.getRecord(recordMap));
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(dataEx.getMessage());
			logger.info("error.", dataEx);
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(sqlEx.getMessage());
			logger.error("internal error.", sqlEx);
		}

		String url = ctx.pageConfig.getBackURL();
		url += "&orderKey=" + orderKey;
		url += "&dealCode=" + dealCode;

		return sendRedirect(ctx, HtmlUtility.replaceURLQuery(url, PARAM_MESSAGE_KEY, saveMessage(ctx)));
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		Upload db = new Upload(ctx.handler);

		DataReader reader = createDataReader(ctx);

		String organizationCode = ctx.req.getParameter("organizationCode");

		com.irt.data.cols.ColumnList columnList = getColumnList(ctx, "DPROrder.DEALITM%DOWN", com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML);

		String[] fieldKeyArray = columnList.getFieldKeyArray();

		MultipartHttpRequest req = (MultipartHttpRequest)ctx.req;
		Map<String, Object> recordMap = new ParameterMap(ctx.req);

		String uploadCode = db.getUploadCode();
		String uploadType = (String)recordMap.get("uploadType");
		String includingHeaderInd = ctx.req.getParameter("headerInd");

		if( uploadCode == null || uploadCode.length() == 0 )
			throw new ServletModelException(ServletModelException.INTERNAL_ERROR);
		else if( uploadType == null || uploadType.length() == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		String orderKey = (String)recordMap.get("orderKey");
		Object status = ( (Order)ctx.db ).getFieldValue(Order.createPrimary(orderKey), "status");
		if( Order.STATUS_SIMULATING.equals(status) || Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
			throw new ServletModelException(ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_STATUS"));
		else if( Order.STATUS_ERROR.equals(status) )
			( (Order)ctx.db ).updateStatus(orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId());

		long millisecond = System.currentTimeMillis();
		try {
			// 업로드 옵션은 ssl 만 지원.
			recordMap.put( "uploadOption", Upload.UPLOAD_OPTION_REPLACE );
			recordMap.put("uploadCode", uploadCode);
			recordMap.put("countryCode", getUserCountryCode(ctx));
			recordMap.put("fileName", req.getInputFileName("file"));
			recordMap.put("uploadUserId", ctx.sessionMng.getUniqId());
			recordMap.put("status", Upload.STATUS_READY);

			if( !db.regist(recordMap) )
				throw new DataException(DataException.ERR_CANNOT_INSERT, ctx.msghandler.getMessage(DataException.ERR_CANNOT_INSERT));
			ctx.handler.commit();

			Map<String, Object> uploadParameterMap = new java.util.HashMap<String, Object>();
			uploadParameterMap.put("uploadCode", uploadCode);
			uploadParameterMap.put("orderKey", ctx.req.getParameter("orderKey"));
			uploadParameterMap.put("dealCode", recordMap.get("dealCode"));
			uploadParameterMap.put("organizationCode", organizationCode);
			uploadParameterMap.put("distributionChannelCode", ctx.req.getParameter("distributionChannelCode"));
			uploadParameterMap.put("divisionCode", ctx.req.getParameter("divisionCode"));
			uploadParameterMap.put("partyCode", ctx.req.getParameter("partyCode"));
			uploadParameterMap.put("fieldKeyArray", fieldKeyArray);

			com.irt.dpr.OrderItem itemDB = new com.irt.dpr.OrderItem(ctx.handler);

			Map<String, Object> primaryMap = com.irt.dpr.Party.createPrimary(ctx.req.getParameter("partyCode"),
					organizationCode, ctx.req.getParameter("distributionChannelCode"),
					ctx.req.getParameter("divisionCode"));
			String allowUOM = null;
			if( com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM") )
				allowUOM = itemDB.getDistAllowUOM(primaryMap);
			if( allowUOM == null )
				allowUOM = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "uom;" + organizationCode, com.irt.dpr.Party.DEFAULT_UOM);

			List<DataLoader.Validator> validators = new ArrayList<DataLoader.Validator>();
			validators.add(0, itemDB.createNormalOrderStopItemValidator(uploadParameterMap));
			validators.add(1, itemDB.createNormalOrderPackDealValidator(uploadParameterMap));
			uploadParameterMap.put("validators", validators);
			// UPLOAD -> ORDER_DETAIL 반영
			DataResult result = db.read(reader, uploadType, uploadParameterMap, ctx.sessionMng.getUniqId(), includingHeaderInd, allowUOM);
			if( result.getErrorCount() > 0 ) {
				ctx.handler.rollback();
				recordMap.put("status", Upload.STATUS_ERROR);

				if( !db.modify(recordMap) )
					throw new DataException(DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE));
				ctx.req.setAttribute("ex_errors", result.getErrors());
				return forward(ctx, systemConfig.getJspPath() + "/error.jsp");
			} else {
				ctx.handler.commit();

				recordMap.put("rowCount", String.valueOf(result.getRowCount()));
				recordMap.put("distributionChannelCodes",
						new com.irt.dpr.CountryDistChannel(ctx.handler).getDistributionChannels(getUserCountryCode(ctx), organizationCode));
				String message = db.execute(result, recordMap);
				if( message == null || message.length() == 0 ) {
					recordMap.put("insertCount", String.valueOf(result.getRegistCount()));
					recordMap.put("updateCount", String.valueOf(result.getModifyCount()));
					recordMap.put("deleteCount", String.valueOf(result.getDeleteCount()));
					recordMap.put("status", Upload.STATUS_COMPLETE);
				} else {
					recordMap.put("message", message);
					recordMap.put("status", Upload.STATUS_ERROR);
				}
				recordMap.put("executeTime", String.valueOf(( System.currentTimeMillis() - millisecond ) / 1000));

				String[] params = new String[] {
						String.valueOf(result.getRowCount()), String.valueOf(result.getRegistCount()), String.valueOf(result.getModifyCount()),
						String.valueOf(result.getDeleteCount()), String.valueOf(result.getWarningCount())
				};

				if( result.getWarningCount() > 0 && result.getWarns() != null ) {
					ctx.req.setAttribute("ex_warns", result.getWarns());
				}

				if( !db.modify(recordMap) )
					throw new DataException(DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE));
				if( message == null || message.length() == 0 )
					ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_DPR_UPLOAD_SUCCESS", params));
				else
					ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_DPR_UPLOAD_FAILED", message));
			}
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(dataEx.getMessage());
			logger.info("error.", dataEx);
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage(sqlEx.getMessage());
			logger.error("internal error.", sqlEx);
		} finally {
			reader.close();
		}

		ctx.req.setAttribute("record", recordMap);

		return uploadInput(ctx);
	}

	@Override
	protected boolean uploadInput( Context ctx ) throws IOException, ServletException, SQLException {
		@SuppressWarnings( "unchecked" )
		Map<String, Object> recordMap = (Map<String, Object>)ctx.req.getAttribute("record");
		if( recordMap == null )
			recordMap = new ParameterMap(ctx.req);
		recordMap.put("partyCode", ctx.req.getParameter("partyCode"));
		String uploadType = ctx.req.getParameter("uploadType");
		if( uploadType == null || uploadType.length() == 0 )
			throw new ServletModelException(ServletModelException.NEEDED_PARAMETER);

		String organizationCode = Record.extractString(recordMap, "organizationCode");
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);
		recordMap.put("organizationCode", organizationCode);
		recordMap.put("encoding", "UTF8");
		recordMap.put("orderKey", ctx.req.getParameter("orderKey"));

		ctx.req.setAttribute("record", recordMap);
		setAttributePartyMaster(ctx, com.irt.data.Record.createMap("countryCode", getUserCountryCode(ctx)), PARTYMASTER_ORGANIZATION);

		ctx.pageConfig.setTitle(ctx.msghandler.getMessage("TITLE_DPR_ORDER_DETAIL_UPLOAD"));

		return forward(ctx, systemConfig.getJspPath() + "/dpr_order_upload.jsp");
	}

	protected boolean wait( Context ctx ) throws IOException, ServletException, SQLException {
		String type = ctx.req.getParameter("type");

		if( MODE_SIMULATION.equals(type) ) {
			if( "sim".equals(type) ) {
				List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

				if( errorList.size() > 0 ) {
					ctx.req.setAttribute("errors", errorList);

					return forward(ctx, systemConfig.getJspPath() + "/error.jsp");
				}
			}

			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_WAITING_SIMULATION"));
		} else if( MODE_CREATION.equals(type) ) {
			ctx.pageConfig.setMessage(ctx.msghandler.getMessage("MSG_WAITING_CREATION"));
		}

		String ftype = ctx.req.getParameter(PARAM_FORMATTYPE);

		ctx.pageConfig.setProperty(PARAM_FORMATTYPE, ftype);

		ctx.pageConfig.setProperty("isFirstSim", ctx.req.getParameter("isFirstSim"));

		return forward(ctx, systemConfig.getJspPath() + "/dpr_order_wait.jsp");
	}

}
