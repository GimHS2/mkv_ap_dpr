/*
*	File Name:	DPRPlaceOrder.java
 *	Version:	2.2.40
 *
 *	Description:
 *
 *	Note:
 *		error.jsp
 *		pub_list_count.jsp
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2025/12/31		2.2.40	2대의 서버 구성으로 redirect 시 에러 메시지가 전달되지 않던 문제 수정 
 *	dudwls3720	2024/09/30		2.2.39	updateHeader() : inDate가 NullPointerException 발생하지 않도록 변경
 *	jbaek		2024/08/28		2.2.38	backURL 생성시 isSecure 옵션값을 준수하도록 변경
 *	dudwls3720	2024/07/31		2.2.37	updateHeader() : checkXSS 기능으로 제한된 파라미터 검사 로직 추가
 *	jbaek		2023/07/27		2.2.36	발주 상품에 대해서 SellingSku 체크 로직 추가, OrderDetail 테이블에 packSize 업데이트 가능하도록 변경
 *	jbaek		2023/07/27		2.2.35	Credit Info 기능 개발
 *	jbaek		2023/03/30		2.2.34	China RDD Closing 오류 수정
 *	paramil		2023/02/28		2.2.33	simulationResult(): 한국에 한하여 거래단가와 sap 단가 차이를 %로 표시(gapPrice)
 *	hankalam	2021/11/30		2.2.32	신규 UI/UX 적용
 *	hankalam	2021/04/30		2.2.31	orderInput(): userPredefinedRdd 가 Y 인경우에만 userRdd 적용되도록 변경
 *	jbaek		2020/06/30		2.2.30	Revise Order Feature.
 *	hankalam	2020/06/30		2.2.30	orderInput(), registDetail(), updateDetail() : 위험상품 여부 검사 로직 추가
 *										updateHeader(): useSelectRdd 가 true 인 경우 미리 정의된 rdd 검사하는 로직 추가
 *	jbaek		2020/04/30		2.2.29	checkMaxOrderQtyRegular: mview snapshot에서부터 에서부터 최근 발주까지 감안한 수량 계산.
 *	jbaek		2019/10/30		2.2.28	shortageRegist(): partyCode없어 regist안되던 오류 수정.
 *	hankalam	2019/10/30		2.2.28	한국 경우: inDate를 Predefined RDD 우선 적용.
 *	hankalam	2019/07/31		2.2.29	Freegoods 발주 기능 추가
 *	jbaek		2019/08/30		2.2.28	defaultOrderQty 추가
 *	jbaek		2019/07/30		2.2.28	deleteWithLineNoUpdate추가, placeOrder후에 orderStatusList를 call하여 orderFlag업데이트하도록 변경. distributionChannel자동선택 추가.
 *	hankalam	2019/06/28		2.2.28	Predefined RDD 적용, useCustomerPONumber 옵션 적용, Item Price 적용
 *	jbaek		2019/05/30		2.2.28	StopItem, PackDeal 추가, placeOrder후 executeStatusList 적용
 *	jbaek		2019/01/30		2.2.27	salesUnit 추가
 *	jbaek		2018/10/30		2.2.26	isChinaCountry() 삭제
 *	jbaek		2018/04/30		2.2.25	barCodeMultiItems 기능 추가. updateDetailContent(): barCode(itemConsumerEANCode)에 따르는 itemCode를 변경할수 있도록 개발
 *	jbaek		2018/03/31		2.2.24	orderInput() : reOrder할때  inDate업데이트하여 sim시에 새 ordDate에 맞는 inDate(RDD)가 사용되도록 수정
 *	hankalam	2017/11/30		2.2.23	Shortage item 제거 및 Product requirement 등록 기능 추가
 *	hankalam	2017/08/31		2.2.23	download() : 발주불가 상품 필터링 적용
 *										orderInput() : itemDisplayInd 항목 N 인 상품 제거 로직 추가
 *										Simulation 시 수량 수정 및 재고 부족분 제거, 부족분 리포트 업데이트 기능 추가
 *	hankalam	2017/05/31		2.2.22	orderInput() : 같은 sold to, ship to 인 Order 상태가 Creating 일 때 중복 발주가 안되도록 메시지 출력 로직 추가
 *	hankalam	2017/02/28		2.2.21	orderInput() : Party 별 UOM 선택 기능 추가
 *										orderInput(), simulationResult() :  Plant Exclusion 적용
 *	hankalam	2016/08/31		2.2.20	orderInput() : reOrder 조건 수정
 *	song7981	2016/06/03		2.2.19	sim상태에서 creation진행 시 orderDate가 현재날짜아닌 경우 rdd refresh되도록 추가
 *	song7981	2016/04/25		2.2.18	shelfLife 정보 추가(중국만)
 *	song7981	2016/02/29		2.2.17	updateHeader,SimulationResult 시 plant of material, comment 나오도록 수정
 *	hankalam	2015/10/30		2.2.16	웹취약성 수정. mainFrame() orderKey 파라미터 위험문자 검사.
 *	jbaek		2014/09/30		2.2.15	Product Hierarchy Level 기능 개발 : download simulationResult 기능 개발
 *	jbaek		2013/11/30		2.2.14	Material Status Auto Update 기능개발
 *	jbaek		2013/04/30		2.2.13	오류수정: updateHeader() 두번째  업데이트시  distributionChannel을 잘못보내는 경우 수정
 *	jbaek		2013/04/30		2.2.12	Sales MOV 관리 기능
 *	jbaek		2013/01/30		2.2.11	PIPO 기능 개발, regulate minOrderTotal 처리방법  변경
 *	jbaek		2012/08/30		2.2.10	RDD Logic 변경: RDDFrSAP이 null일때 처리방법 변경
 *	jbaek		2012/09/07		2.2.9	Quick Add 시 사용가능 기간 validation 설정
 *	jbaek		2012/08/30		2.2.8	rdd Logic 변경, duplicate request patch 처리
 *	jbaek		2012/07/30		2.2.7	regulate minOrderTotal 기능 추가
 *	jbaek		2011/11/30		2.2.6	China RDD Logic에 HoCo조직추가
 *	lsinji		2009/12/11		2.2.5	orderqty에 대한 NumberFormatException 처리
 *										registDetail()에 existingOrderInd 처리
 *										registDetail, updateDetailContent에 status 추가
 *	lsinji		2009/10/23		2.2.4	Simulation상태 이후에 Order Detail이 수정 되면 simulation과 관련한 값을 null로 처리
 *	lsinji		2009/06/30		2.2.3	DPROrder -> (DPRPlaceOrder, DPREnquiryOrder)로 변경
 *	lsinji		2009/04/23		2.2.2	China RDD Logic 수정, orderqty 0 이하 입력 제한
 *	lsinji		2009/04/18		2.2.1	executeEnquiry() parameter에 employeeID 제거
 *	guksm		2008/09/26		2.2.0	create
 *
**/

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import com.irt.data.AbstractFieldSet;
import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataResult;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.dpr.Country;
import com.irt.dpr.FreeGoods;
import com.irt.dpr.Item;
import com.irt.dpr.Order;
import com.irt.dpr.OrderDetail;
import com.irt.dpr.Party;
import com.irt.dpr.ProductRequire;
import com.irt.dpr.RddIndicator;
import com.irt.dpr.RddTrigger;
import com.irt.dpr.ShortageEliminate;
import com.irt.dpr.Upload;
import com.irt.dpr.util.CondPred;
import com.irt.dpr.util.Loggers;
import com.irt.html.HtmlPage;
import com.irt.html.HtmlUtility;
import com.irt.ics.Board;
import com.irt.ics.BoardHeadword;
import com.irt.ics.HelpBoard;
import com.irt.rbm.RBMSystem;
import com.irt.rbm.usr.UserUser;
import com.irt.servlet.MultipartHttpRequest;
import com.irt.servlet.PageConfig;
import com.irt.servlet.ParameterMap;
import com.irt.servlet.ServletModelException;
import com.irt.servlet.ServletUtility;
import com.irt.servlet.SystemConfig;
import com.irt.sql.SQLHandler;
import com.irt.util.Arrays;
import com.irt.util.MapUtil;
import com.irt.util.Utility2;

/**
 *
 */
@javax.servlet.annotation.WebServlet( urlPatterns = { "/servlet/DPRPlaceOrder" } )
public class DPRPlaceOrder extends DPROrderServletModel {//@formatter:off
	private final static int	SYSTEMRDD_PLUS_DAYS				= Order.SYSTEMRDD_PLUS_DAYS;
	private final static int	SAPRDDNULL_PLUS_DAYS			= Order.SAPRDDNULL_PLUS_DAYS;//When sap RDD does not have... default value to be 2
	public final static long	CHINA_ORDERCLOSING_TIME			= Order.CHINA_ORDERCLOSING_TIME;

	private final static int	DEFAULT_ORDERQTY				= 1;

	protected final static String MODE_CREATION					= "cre";
	private final static String MODE_FRAME						= "frm";
	private final static String MODE_LOADTEMPLATE				= "ltp";
	private final static String MODE_ORDERINPUT					= "ior";
	private final static String MODE_REGISTDETAIL				= "rgd";
	protected final static String MODE_REMOVEDETAIL				= "rmd";
	private final static String MODE_SET_TRADEPARTNER			= "stp";
	private final static String MODE_SET_CREDITSTATUS			= "scrd";
	protected final static String MODE_SIMULATION					= "sim";
	private final static String MODE_SIMULATION_RESULT			= "simr";
	protected final static String MODE_UPDATEDETAIL				= "upd";
	private final static String MODE_UPDATEHEADER				= "uph";
	protected final static String MODE_WAIT						= "wait";
	protected final static String MODE_SHORTAGE_ELIMINATE			= "shrt";
	protected final static String MODE_SHORTAGE_LIST				= "slist";
	private final static String MODE_SHORTAGE_DOWNLOAD			= "sdown";
	private final static String MODE_FREEGOODS_DOWNLOAD			= "fdown";
	private final static String MODE_FREEGOODS_LIST				= "flist";

	protected final static String PARAM_REQUESTTYPE				= "rtype";
	private final static String PARAM_LISTTYPE					= "ltype";
	private final static String	LISTTYPE_INPUT					= "input";
	private final static String REQTYPE_QUICK					= "quick";
	private final static String REQTYPE_REGIST					= "reg";

	private final static String PARAM_DOWNTYPE					= "dwntype";
	private final static String DOWNTYPE_ITEM					= "item";
	private final static String DOWNTYPE_SIMULATION_RESULT		= "simr";

	@Override
	protected boolean checkAuthorize( Context ctx ) throws ServletException {
		return checkAuthorize( ctx, true );
	}

	/**
	 * execute before update detail table
	 */
	protected void checkDetailContentExtra( Context ctx, List<Map<String, Object>> errorList ) throws IOException, ServletException, SQLException {
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;
		Collection<Map<String, Object>> records = (new ParameterMap(ctx.req)).extractGroupList( "value" );// from client
		String orderKey = ctx.req.getParameter( "orderKey" );
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		String distributionChannelCode = ctx.req.getParameter( "distributionChannelCode" );
		String divisionCode = getDivisionCode( ctx );
		String partyCode = ctx.req.getParameter("partyCode");
		com.irt.dpr.Item itemDB = new com.irt.dpr.Item(ctx.handler);
		com.irt.dpr.OrderItem oitemDB = new com.irt.dpr.OrderItem(ctx.handler);

		Map<String, Object> oitemPrimaryMap = new java.util.HashMap<String, Object>();
		oitemPrimaryMap.put( "partyCode", partyCode );
		oitemPrimaryMap.put( "distributorCode", partyCode );
		oitemPrimaryMap.put( "organizationCode", organizationCode );
		oitemPrimaryMap.put( "distributionChannelCode", distributionChannelCode );
		oitemPrimaryMap.put( "divisionCode", divisionCode );
		if( oitemPrimaryMap.get("distributorCode") == null
				|| oitemPrimaryMap.get("partyCode") == null
				|| oitemPrimaryMap.get("distributionChannelCode") == null
				|| oitemPrimaryMap.get("divisionCode") == null
				|| oitemPrimaryMap.get("organizationCode") == null
				)
			throw new ServletModelException( "Mandatory key is missing:" + oitemPrimaryMap );

		for( Map<String, Object> obj : records ) {
			Map<String, Object> recordMap = obj;
			try {
				oitemPrimaryMap.put( "itemCode", recordMap.get("itemCode") );
				com.irt.data.Date availableDate =  com.irt.data.Date.getInstance();
				oitemPrimaryMap.put( "availableDate", availableDate );
				Map<String, Object> oitemInfoMap = oitemDB.getItemInfoMap( oitemPrimaryMap, new String[]{ "itemCode" } );
				if( oitemInfoMap == null ) {
					errorList.add( createErrorMap("["+recordMap.get("lineNumber")+"] " + recordMap.get("itemCode") + " - "
							+ itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx))
							, ctx.msghandler.getMessage("ERR_ISNOT_SELLINGSKU")) );
					continue;
				}
			} catch( SQLException sqlEx ) {
					errorList.add( createErrorMap("["+recordMap.get("lineNumber")+"] "+ itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx))
								, sqlEx ) );
			}
		}
	}

	protected void checkMaxOrderQtyRegular( Context ctx ) throws IOException, ServletException, SQLException {
		String organizationCode = ctx.req.getParameter("organizationCode");
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);
		boolean useMoqIndAppLevel = ("dv".equals(getSystemConfig().getProperty("appEnvId")) && "Y".equals(getSystemConfig().getProperty("useMoq")));

		if( useMoqIndAppLevel || Country.isFeature(organizationCode, "useMoq") ) {
			List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
			String orderKey = ctx.req.getParameter("orderKey");
			if( orderKey == null || orderKey.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			OrderDetail detailDB = (OrderDetail)ctx.extraObj;
			Map<String, Object> conditionMap = new HashMap<String, Object>();
			conditionMap.put(Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER);
			conditionMap.put("orderKey", orderKey);

			List<Map<String,Object>> exceedList = detailDB.getRecords( conditionMap
					, new String[] {"orderQty", "rgRmnMaxQty", "uom", "rgRmnMaxUomQty", "itemCode"} );

			if( exceedList != null || exceedList.size()> 0) {
				for( Map<String, Object> map : exceedList ) {
					BigDecimal orderQty = (BigDecimal)map.get("orderQty");
					BigDecimal maxQty = (BigDecimal)map.get("rgRmnMaxUomQty");
					if( orderQty != null && maxQty != null ) {
						if( orderQty.intValue() > maxQty.intValue() ) {
							errorList.add( createErrorMap(map.get("itemCode")
									, ctx.msghandler.getMessage("ERR_MAXORDQTY_EXCEED_3", (String)map.get("itemCode"), maxQty.toString(), (String)map.get("uom"))) );
						}
					}
				}
				if( errorList.size() > 0 ) {
					ctx.req.setAttribute( "errors", errorList );
					String backURL = new StringBuffer( ((HtmlPage)ctx.pageConfig).getRequestURL() ).append( "?" ).append( ctx.req.getQueryString() ).toString();
					backURL = HtmlUtility.replaceURLQuery( backURL, PARAM_MODE, MODE_FRAME );
					throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_MAXORDQTY_EXCEED") );
				}
			}
		}
	}

	protected boolean checkInvalidItem( Context ctx ) throws ServletModelException, SQLException  {
		String organizationCode = ctx.req.getParameter("organizationCode");
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode(ctx);

		boolean useDangerousItem = Country.isFeature( organizationCode, "useDangerousItem" );
		if( useDangerousItem ) {
			String dangerousPlant = Country.getFeatureValue( organizationCode, "dangerousPlant" );
			if( dangerousPlant == null ) {
				throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_NEEDED_ENVIRONMENT", "dangerousPlant") );
			}

			String orderKey = ctx.req.getParameter("orderKey");
			if( orderKey == null || orderKey.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			OrderDetail detailDB = (OrderDetail)ctx.extraObj;
			Map<String, Object> conditionMap = new HashMap<String, Object>();
			conditionMap.put(Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER);
			conditionMap.put("orderKey", orderKey);
			conditionMap.put( "dangerousPlant", dangerousPlant );
			conditionMap.put( "dangerousNumber", Item.ITEMTYPE_DANGEROUS );

			String[] fieldKeys = { "itemCode", "dangerousInd" };
			List<Map<String, Object>> detailList = detailDB.getRecords( conditionMap, fieldKeys );

			String orderType = (String)((Order)ctx.db).getFieldValue( Order.createPrimary(orderKey), "orderType" );
			for( Map<String, Object> itemMap : detailList ) {
				boolean isDangerousItem = "Y".equals( itemMap.get("dangerousInd") );
				if( Order.ORDER_TYPE_DANGEROUS.equals(orderType) ) {
					if( !isDangerousItem ) {
						return false;
					}
				} else {
					if( isDangerousItem ) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private Map<String, Object> createConditionMap( Context ctx ) throws ServletException {
		return new ParameterMap( ctx.req );
	}

	private Map<String, Object> createDownloadConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap = new ParameterMap( ctx.req );

		if( !conditionMap.containsKey("partyCode") ) {
			if( conditionMap.containsKey("soldPartyCode") )
				conditionMap.put( "partyCode", conditionMap.get("soldPartyCode") );
		}
		if( !conditionMap.containsKey("partyCode") )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( !conditionMap.containsKey(Condition.BASIS_CONDITIONKEY) ) {
			conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );
		}

		com.irt.data.Date availableDate =  com.irt.data.Date.getInstance();
		conditionMap.put( "availableDate", availableDate );

		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		conditionMap.putAll( createHierarcyConditionMap(ctx) );

		String odrdlvGroup = ctx.req.getParameter("odrdlvGroup");
		conditionMap.put("odrdlvGroup", odrdlvGroup);
		if( "SP".equals(odrdlvGroup) ) {
			String dlvrySpBrands = RBMSystem.getSystemEnv("DPR", "dlvrySpBrands;"+conditionMap.get("organizationCode"));
			if( dlvrySpBrands != null && dlvrySpBrands.length() > 0 ) {
				conditionMap.put("brandCode", dlvrySpBrands.split(","));
			}
		} else if( "RG".equals(odrdlvGroup) ) {
			String dlvrySpBrands = RBMSystem.getSystemEnv("DPR", "dlvrySpBrands;"+conditionMap.get("organizationCode"));
			if( dlvrySpBrands != null && dlvrySpBrands.length() > 0 ) {
				CondPred.putIsNotEquals(conditionMap, "brandCode", dlvrySpBrands.split(","));
			}
		}

		return conditionMap;
	}

	protected Map<String, Object> createHierarcyConditionMap( Context ctx ) throws ServletException, SQLException {
		Map<String, Object> conditionMap = new TreeMap<String, Object>();
		// Hierarchy Condition
		String countryCode = getUserCountryCode( ctx );
		Map<String, Object> recordMap = new com.irt.dpr.Country(ctx.handler).getDefaultHierarchyCondition( countryCode );
		if( recordMap != null ) {
			Map<String, Object> hierarchyMap = new java.util.HashMap<String, Object> ();
			if( recordMap.get("defaultHierarchyLevel") != null ) {
				hierarchyMap.put( "defaultHierarchyLevel", recordMap.get("defaultHierarchyLevel") );
				hierarchyMap.put( "classCode", recordMap.get("defaultHierarchyLevel") );
			}
			if( recordMap.get("hierarchyCondition") != null ) {
				hierarchyMap.put( "hierarchyCondition", ((String)recordMap.get("hierarchyCondition")).split(";") );
			}

			conditionMap.put( "countryHierarchyCondition", hierarchyMap );
		}
		conditionMap.put( "masterOrganizationCode", getSavedOrganizationCode( ctx ) );
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );

		return conditionMap;
	}

	@Override
	protected boolean doRequest( Context ctx, boolean isPost ) throws IOException, ServletException, SQLException {
		if( isPost ) {
			if( MODE_REGISTDETAIL.equals(ctx.mode) ) return registDetail( ctx );
			else if( MODE_UPDATEDETAIL.equals(ctx.mode) ) return updateDetail( ctx );
			else if( MODE_UPDATEHEADER.equals(ctx.mode) ) return updateHeader( ctx );
			else if( MODE_WAIT.equals(ctx.mode) ) return wait( ctx );
			else if( MODE_SHORTAGE_ELIMINATE.equals(ctx.mode) ) return shortageEliminate( ctx );
		//	else if( MODE_SHORTAGE_LIST.equals(ctx.mode) ) return shortageList( ctx );
		//	else if( MODE_FREEGOODS_LIST.equals(ctx.mode) ) return simulationFreegoodslist( ctx );
		} else {
			if( MODE_FRAME.equals(ctx.mode) ) return mainFrame( ctx );
			else if( MODE_LOADTEMPLATE.equals(ctx.mode) ) return loadTemplate( ctx );
			else if( MODE_ORDERINPUT.equals(ctx.mode) ) return orderInput( ctx );
			else if( MODE_REGISTDETAIL.equals(ctx.mode) ) return registDetail( ctx );
			else if( MODE_REMOVEDETAIL.equals(ctx.mode) ) return removeDetail( ctx );
			else if( MODE_SET_TRADEPARTNER.equals(ctx.mode) ) return setAttributeCondition( ctx );
			else if( MODE_SET_CREDITSTATUS.equals(ctx.mode) ) return setCreditStatusList( ctx );
			else if( MODE_SIMULATION.equals(ctx.mode) ) return simulationOrder( ctx );
			else if( MODE_SIMULATION_RESULT.equals(ctx.mode) ) return simulationResult( ctx );
			else if( MODE_SHORTAGE_DOWNLOAD.equals(ctx.mode) ) return shortageDownload( ctx );
			else if( MODE_FREEGOODS_DOWNLOAD.equals(ctx.mode) ) return freegoodsDownload( ctx );
			else if( MODE_CREATION.equals(ctx.mode) ) return placeOrder( ctx );
			else if( MODE_WAIT.equals(ctx.mode) ) return wait( ctx );
		}

		return super.doRequest( ctx, isPost );
	}

	private boolean setCreditStatusList( Context ctx ) throws ServletException, SQLException, IOException {
		com.irt.dpr.PartyCredit db = new com.irt.dpr.PartyCredit( ctx.handler );

		String[] fieldKeys = new String[]{ "creditPartyCode", "creditPartyName", "soldPartyCode", "creditLimit", "creditLimitCrcy", "accountReceivable", "accountReceivableCrcy", "creditExposure", "creditExposureCrcy", "creditRefDateTime", "creditCurrency", "creditRiskInd" };
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
		List<Map<String, Object>> recordList = null;
		String organizationCode = getSavedOrganizationCode( ctx );
		if( organizationCode != null && organizationCode.length() > 0 ) {
			List<Map<String, Object>> dists = getUserSoldParties(ctx);
			String userGroupClass = getUserGroupClass( ctx, ctx.sessionMng.getPartyId(), ctx.sessionMng.getUniqId() );
			if( dists != null && dists.size() >= 1 && ("OR".equals(userGroupClass) || ctx.sessionMng.isSystemAdmin()) ) {
				// serve only first sold-to party
				String soldPartyCode = (String)dists.get(0).get( "partyCode" );
				if( soldPartyCode == null || soldPartyCode.length() == 0 )
					soldPartyCode = (String)dists.get(0).get("soldPartyCode");
				conditionMap.put( "organizationCode", organizationCode );
				conditionMap.put( "distributionChannelCode", dists.get(0).get("distributionChannelCode") );
				conditionMap.put( "soldPartyCode", soldPartyCode );
				recordList = db.getRecords( conditionMap, fieldKeys, 0, 10 );
			}
		}

		if( recordList != null && recordList.size() > 0 )
			ctx.req.setAttribute( "creditStatusList", recordList );

		if( ctx.req.getParameter("json") != null )
			return jsonResponse(ctx, "creditStatusList");
		else
			return true;
	}

	private void setPropertyOrderDeliveryGroup( Context ctx, Map<String, Object> headerMap ) throws SQLException {
		String DEFAULT_ODRDLVGROUP = "SP";// usually OTC products

		String orderKey = ctx.req.getParameter( "orderKey" );
		String odrdlvGroup = ctx.req.getParameter( "odrdlvGroup" );

		if( orderKey != null && orderKey.length() > 0  && orderKey.indexOf(">") < 0 && orderKey.indexOf("<") < 0
				&& orderKey.indexOf("+") < 0 && orderKey.indexOf(";") < 0 ) {
			if( headerMap == null || (!headerMap.containsKey("organizationCode") || !headerMap.containsKey("status") || !headerMap.containsKey("officeCode")) ) {
				Order headerDB = (Order)ctx.db;
				headerMap = headerDB.getRecord(Record.createMap("orderKey", orderKey), new String[]{"status", "organizationCode", "officeCode"});
			}

			if( headerMap != null ) {
				String dlvrySpOffices = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "dlvrySpOffices;"+headerMap.get("organizationCode"));
				if( dlvrySpOffices != null && dlvrySpOffices.length() > 0 ) {
					if( !(headerMap.get("status")== null || ((String)headerMap.get("status")).length() == 0)
							&& headerMap != null && java.util.Arrays.asList(dlvrySpOffices).contains(headerMap.get("officeCode")) ) {
						ctx.pageConfig.setProperty( "odrdlvGroup", ((odrdlvGroup != null && odrdlvGroup.length()>0) ? odrdlvGroup : DEFAULT_ODRDLVGROUP) );
					}
				}
			}
		}
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


	private void shortageRegist( Context ctx, List<Map<String, Object>> errorList ) throws SQLException, ServletException {
		Order headerDB = (Order)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;
		ProductRequire prDB = new ProductRequire( ctx.handler );
		ShortageEliminate shortageDB = new ShortageEliminate( ctx.handler );
		String orderKey = ctx.req.getParameter( "orderKey" );

		List<Map<String, Object>> shortageList;
		List<Map<String, Object>> detailList;
		Map<String, Object> headerMap;
		Map<String, Object> conditionMap = new HashMap<String, Object>();
		Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
		String[] fieldKeys = { "itemCode", "itemConsumerEANCodeCNF", "itemCodeConfirmed", "qty", "uom" };
		conditionMap.put( "orderKey", orderKey );
		headerMap = headerDB.getRecord( conditionMap );

		recordMap.put( "productReqKey", prDB.createProductReqKey() );
		recordMap.put( "orderDate", headerMap.get("orderDate") );
		recordMap.put( "expectedDate", headerMap.get("inDate") );
		recordMap.put( "soldPartyCode", headerMap.get("soldPartyCode") );
		recordMap.put( "partyCode", headerMap.get("soldPartyCode") );
		recordMap.put( "shipPartyCode", headerMap.get("shipPartyCode") );
		recordMap.put( "orderKey", orderKey );
		recordMap.put( "organizationCode", headerMap.get("organizationCode") );
		recordMap.put( "distributionChannelCode", headerMap.get("distributionChannelCode") );
		recordMap.put( "divisionCode", headerMap.get("divisionCode") );
		recordMap.put( "countryCode", headerMap.get("countryCode") );
		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );

		conditionMap.put( "status", "00" );
		shortageList = shortageDB.getRecords( conditionMap, fieldKeys );
		if( shortageList == null ) {
			shortageList = new ArrayList<Map<String, Object>>();
		}
		detailList = detailDB.getShortageItem( orderKey, getDisplayLanguage(ctx) );

		if( detailList != null ) {
			shortageList.addAll( detailList );
		}

		if( shortageList != null ) {
			for( Map<String, Object> shortageMap : shortageList ) {
				recordMap.put( "itemCode", shortageMap.get("itemCode") );
				recordMap.put( "itemCodeConfirmed", shortageMap.get("itemCodeConfirmed") );
				recordMap.put( "itemConsumerEAN", shortageMap.get("itemConsumerEANCodeCNF") );
				recordMap.put( "qty", shortageMap.get("qty") );
				recordMap.put( "uom", shortageMap.get("uom") );
				String resultStatus = "00";
				try {
					if( prDB.regist(recordMap) ) {
						resultStatus = "CO";
					}
				} catch( DataException dataEx ) {
					ctx.handler.rollback();
					if( DataException.ERR_UNIQUE_CONSTRAINT.equals(dataEx.getErrorKey()) ) {
						try {
							if( prDB.modify(recordMap) ) {
								resultStatus = "CO";
							}
						} catch( DataException ex ) {
							resultStatus = "ER";
							ctx.handler.rollback();
							errorList.add( createErrorMap(recordMap.get("itemCode"), ex) );
						}
					} else {
						resultStatus = "ER";
						errorList.add( createErrorMap(recordMap.get("itemCode"), dataEx) );
					}
				} catch( SQLException sqlEx ) {
					resultStatus = "ER";
					ctx.handler.rollback();
					errorList.add( createErrorMap(recordMap.get("itemCode"), sqlEx) );
				} finally {
					try {
						if( shortageDB.updateStatus(orderKey, (String)recordMap.get("itemCode"), resultStatus) ) {
							ctx.handler.commit();
						}
					} catch( DataException ex ) {
						ctx.handler.rollback();
					}
				}
			}
		}
	}

	protected boolean shortageEliminate( Context ctx ) throws IOException, ServletException, SQLException {
		String orderKey = ctx.req.getParameter( "orderKey" );
		//ParameterMap paramMap = new ParameterMap( ctx.req );
		String[] eliminateLineNumbers = ctx.req.getParameterValues( "eliminate_lineNumber" );
		String[] valueLineNumbers = ctx.req.getParameterValues( "value_lineNumber" );
		List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();

		if( valueLineNumbers != null && valueLineNumbers.length > 0 ) {
			updateDetailContent( ctx, errorList );
		}

		if( eliminateLineNumbers != null && eliminateLineNumbers.length > 0 ) {
			errorList.addAll( removeDetail(ctx, eliminateLineNumbers) );
			if( errorList.size() > 0 ) {
				ctx.req.setAttribute( "errors", errorList );
				return forward( ctx, systemConfig.getJspPath() +"/error.jsp" );
			} else {
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_ORDER_SHORTAGE_ELIMINATE_SUCCESS", new String[] { String.valueOf(eliminateLineNumbers.length) }) );
			}
		}

/*		OrderDetail detailDB = (OrderDetail)ctx.extraObj;
		Map<String, Object> conditionMap =  new HashMap<String, Object>();
		conditionMap.put( "orderKey", orderKey );
		int count = detailDB.getRecordCount( conditionMap );*/

		return wait( ctx );

/*
		return simulationResult( ctx, orderKey );*/
	}

	protected void shortageList( Context ctx ) throws IOException, ServletException, SQLException {
		ShortageEliminate shortageDB = new ShortageEliminate( ctx.handler );
		String orderKey = ctx.req.getParameter( "orderKey" );
		List<Map<String, Object>> recordList = null;

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		String[] fieldKeys = { "itemCode", "itemCodeConfirmed", "itemConsumerEANCodeCNF", "itemName", "shelfLife", "qty", "uom", "pcQty" };
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		conditionMap.put( "orderKey", orderKey );
		conditionMap.put( "status", "00" );
		List<Map<String, Object>> dbRecordList = shortageDB.getRecords( conditionMap, fieldKeys );
		if( dbRecordList != null ) {
			recordList = new ArrayList<Map<String, Object>>();
			recordList.addAll( dbRecordList );
		}

		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, recordList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRProductRequire.SIM%LIST" );
		//ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_PRODUCT_REQUIREMENT_SIM_LIST") );
		ctx.req.setAttribute( "shortageList", recordList );
		ctx.req.setAttribute( "shortageColumnList", columnList );
		//ctx.req.setAttribute( "condition", conditionMap );
		//ctx.pageConfig.setProperty( PARAM_TYPE, MODE_SIMULATION );
		//ctx.pageConfig.setManageAuth( false );

		//return forward( ctx, systemConfig.getJspPath() + "/dpr_order_shortage_list.jsp" );
	}

	@Override
	protected boolean download( Context ctx ) throws IOException, ServletException, SQLException {
		String downType = ctx.req.getParameter( PARAM_DOWNTYPE );
		if( downType == null || downType.length() ==  0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String organizationCode = getSavedOrganizationCode(ctx);
		List<String> optionKeyList = new ArrayList<String>();
		optionKeyList.add(com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML);
		if( Country.isFeature(organizationCode, "usePackDeal") )
			optionKeyList.add("usePackDeal");
		if( Country.isFeature(organizationCode, "useStopItem") )
			optionKeyList.add("useStopItem");
		if( Country.isFeature(organizationCode, "useCloseItem") )
			optionKeyList.add("useCloseItem");
		if( Country.isFeature(organizationCode, "useMoq") )
			optionKeyList.add("useMoq");
		if( Country.isFeature(organizationCode, "useSuggestSalesUnitInput") )
			optionKeyList.add("useSuggestSalesUnitInput");
		if( Country.isFeature(organizationCode, "usePlantRcv") )
			optionKeyList.add("usePlantRcv");
		if( com.irt.dpr.Country.isFeature(organizationCode, "useManualItemPrice")
			&& com.irt.dpr.Country.isFeature(organizationCode, "useSinglePrice") )
			optionKeyList.add("useSinglePrice");

		com.irt.data.cols.ColumnList columnList = null;
		if( DOWNTYPE_ITEM.equals( downType ) ) {
			OrderDetail itemDB = new OrderDetail( ctx.handler );

			Map<String, Object> conditionMap = createDownloadConditionMap( ctx );

			if( Country.isFeature((String)conditionMap.get("organizationCode"), "useSuggestSalesUnitInput") )
				conditionMap.put("useSuggestSalesUnitInput", "Y");

			boolean useDangerousItem = Country.isFeature( organizationCode, "useDangerousItem" );
			if( useDangerousItem ) {
				String dangerousPlant = Country.getFeatureValue( organizationCode, "dangerousPlant" );
				if( dangerousPlant != null ) {
					conditionMap.put( "dangerousPlant", dangerousPlant );
					conditionMap.put( "dangerousNumber", Item.ITEMTYPE_DANGEROUS );
					String orderType = (String)conditionMap.get( "orderType" );
					if( Order.ORDER_TYPE_DANGEROUS.equals(orderType) ) {
						conditionMap.put( "dangerousInd", "Y" );
					} else {
						conditionMap.put( "dangerousInd", "N" );
					}
				} else {
					throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_NEEDED_ENVIRONMENT", "dangerousPlant") );
				}
			}

			if( com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useExtra1") ) {
				String countryKey = Country.getCountryKeyFromPartyId(ctx.sessionMng.getPartyId());
				columnList = getColumnList( ctx, "DPROrderItem.DOWN%DOWN."+ countryKey, optionKeyList.toArray(new String[0]) );
				if( columnList == null )
					columnList = getColumnList( ctx, "DPROrderItem.DOWN%DOWN.CN", optionKeyList.toArray(new String[0]) );
			} else {
				columnList = getColumnList( ctx, "DPROrderItem.DOWN%DOWN", optionKeyList.toArray(new String[0]) );
			}

			List<String> sortKeyList = new ArrayList<String>(java.util.Arrays.asList(columnList.getSortKeys()));
			if( optionKeyList.contains("useCloseItem") )
				sortKeyList.add(0, "ordCloseTime");
			if( optionKeyList.contains("usePackDeal") )
				sortKeyList.add(0, "dealEndDate");
			if( optionKeyList.contains("useStopItem") )
				sortKeyList.add(0, "stopEndDate");

			DataWriter out = createDataWriter( ctx, "orderitem" );
			try {
				ServletUtility.setSort( ctx.req, itemDB, sortKeyList.toArray(new String[0]) );
				itemDB.write( out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE );
			} catch( SQLException sqlEx ) {
				out.println();
				out.print( sqlEx.getMessage() );
				logger.error( "internal error.", sqlEx );
			} finally {
				out.flush();
				out.close();
			}
		} else if( DOWNTYPE_SIMULATION_RESULT.equals(downType) ) {

			String orderKey = ctx.req.getParameter( "orderKey" );
			if( orderKey == null || orderKey.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

			Order headerDB = (Order)ctx.db;
			OrderDetail detailDB = (OrderDetail)ctx.extraObj;

			Map<String, Object> recordMap = headerDB.getRecord(createConditionMap(ctx));

			String formatType = ctx.req.getParameter( PARAM_FORMATTYPE );
			if( formatType == null || formatType.length() == 0 )
				formatType = FORMATTYPE_PC;

			Map<String, Object> conditionMap = Order.createPrimary(orderKey);
			conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
			conditionMap.put( "formatUOM", formatType.toUpperCase() );
			conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );
			conditionMap.putAll( createHierarcyConditionMap(ctx) );
			conditionMap.put( "shipPartyCode", recordMap.get("shipPartyCode") );
			conditionMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
			conditionMap.put( "distributionChannelCode", recordMap.get("distributionChannelCode") );
			conditionMap.put( "partyCode", recordMap.get("partyCode") );

			Map<String, Object> headerMap = headerDB.getRecord( conditionMap );

			// Detail
			//com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrder.SIM.RESULT%LIST." + formatType.toUpperCase() );
			if( com.irt.dpr.Country.isFeature((String)conditionMap.get("masterOrganizationCode"), "useExtra1") ) {
				columnList = getColumnList( ctx, "DPROrder.SIM.RESULT%LIST.CN." + formatType.toUpperCase(), optionKeyList.toArray(new String[0]) );
			} else {
				columnList = getColumnList( ctx, "DPROrder.SIM.RESULT%LIST." + formatType.toUpperCase(), optionKeyList.toArray(new String[0]) );
			}

			int idxVars[] = ctx.pageConfig.getListIndexVariables();
			ServletUtility.setSort( ctx.req, detailDB, columnList.getSortKeys() );
			List<Map<String, Object>> detailList = detailDB.getRecords( conditionMap, columnList.getFieldKeys("simulationPackSize"), idxVars[0], idxVars[1] );
			Map<String, Object> sumMap = headerDB.getSimulationSummary(conditionMap);

			String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, detailList );
			if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

			AbstractFieldSet hfs = headerDB.getFieldSet(false);
			ctx.req.setAttribute( "header", headerMap );
			ctx.req.setAttribute( "headerFieldSet", hfs );
			ctx.req.setAttribute( "records", detailList );
			ctx.req.setAttribute( "summary", sumMap);
			ctx.req.setAttribute( "columnList", columnList );

			ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_"+ MODE_SIMULATION_RESULT.toUpperCase()) );

			if( com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()).equals(headerMap.get("orderDate")) )
				ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPROrder.MNG") );
			else
				ctx.pageConfig.setManageAuth( false );

			DataWriter out = createDataWriter( ctx, "simulationResult" );
			try {
				int fieldSpan = 3;
				char originalDataType = out.getDataType();
				String fieldPartyNameFormat = "[ %s ] %s";
				if( out instanceof com.irt.util.SSDataWriter ) {
					((com.irt.util.SSDataWriter)out).setColumnList( columnList );
				}

				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(hfs.getField( "orderDate").getDescriptionKey()), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA );
				out.print( hfs.getField("orderDate").format(headerMap, ctx.msghandler), fieldSpan );
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(hfs.getField("organizationCode").getDescriptionKey()), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA );
				out.print( String.format(fieldPartyNameFormat, hfs.getField("organizationCode").format(headerMap, ctx.msghandler), headerMap.get("organizationName")), fieldSpan );
				out.println();

				out.setDataType( DataWriter.FIELDHEADER );
				if( !com.irt.dpr.Country.isFeature(organizationCode, "useInputRDD") )
					out.print( ctx.msghandler.getMessage(hfs.getField("inDateDefault").getDescriptionKey()), fieldSpan );
				else
					out.print( ctx.msghandler.getMessage(hfs.getField("inDate").getDescriptionKey()), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA );
				out.print( hfs.getField("inDate").format(headerMap, ctx.msghandler), fieldSpan );
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(hfs.getField("inDateSimulation").getDescriptionKey()), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA );
				out.print( hfs.getField("inDateSimulation").format(headerMap, ctx.msghandler), fieldSpan );
				out.println();

				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(hfs.getField("soldPartyCode").getDescriptionKey()), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA );
				out.print( String.format(fieldPartyNameFormat, hfs.getField("soldPartyCode").format(headerMap, ctx.msghandler), headerMap.get("soldPartyName")), fieldSpan );
				out.setDataType( DataWriter.FIELDHEADER );
				if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) {
					out.print( ctx.msghandler.getMessage(hfs.getField("orderType").getDescriptionKey()), fieldSpan );
					out.setDataType( DataWriter.FIELDDATA );
					out.print( hfs.getField("orderType").format(headerMap, ctx.msghandler), fieldSpan );
				} else {
					out.print( "", fieldSpan );
					out.setDataType( DataWriter.FIELDDATA );
					out.print( "", fieldSpan );
				}
				out.println();

				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(hfs.getField("shipPartyCode").getDescriptionKey()), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA );
				out.print( String.format(fieldPartyNameFormat,hfs.getField("shipPartyCode").format(headerMap, ctx.msghandler), headerMap.get("shipPartyName")), fieldSpan );
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage( hfs.getField("status").getDescriptionKey() ), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA );
				out.print( hfs.getField("status").format(headerMap, ctx.msghandler), fieldSpan );
				out.println();
				out.println();

				out.setDataType( originalDataType );

				ServletUtility.setSort( ctx.req, detailDB, columnList.getSortKeys() );
				detailDB.write( out, conditionMap, columnList, QueryableManager.OPT_WRITING_TITLE );

				out.println();
				String msgEst = "jsp.dpr_order_result.FIELD_ESTIMATED_NETAMOUNT";
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(hfs.getField("orderValue").getDescriptionKey()), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA);
				out.print( sumMap.get("inputtedOrderValue"), fieldSpan );
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(msgEst), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA);
				out.print( hfs.getField("orderValue").format(sumMap, ctx.msghandler), fieldSpan );
				out.println();

				msgEst = "jsp.dpr_order_result.FIELD_ESTIMATED_TAX";
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(hfs.getField("orderTax").getDescriptionKey()), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA);
				out.print( sumMap.get("inputtedOrderTax"), fieldSpan );
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(msgEst), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA);
				out.print( hfs.getField("orderTax").format(sumMap, ctx.msghandler), fieldSpan );
				out.println();


				msgEst = "jsp.dpr_order_result.FIELD_ESTIMATED_DAMAGEDDISCOUNT";
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(hfs.getField("orderDiscount").getDescriptionKey()), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA);
				out.print( sumMap.get("inputtedOrderDiscount"), fieldSpan );
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(msgEst), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA );
				out.print( hfs.getField( "orderDiscount" ).format( sumMap, ctx.msghandler ), fieldSpan );
				out.println();

				msgEst = "jsp.dpr_order_result.FIELD_ESTIMATED_TOTAL";
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage( hfs.getField( "orderTotal" ).getDescriptionKey() ), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA);
				out.print( sumMap.get( "inputtedOrderTotal" ), fieldSpan );
				out.setDataType( DataWriter.FIELDHEADER );
				out.print( ctx.msghandler.getMessage(msgEst), fieldSpan );
				out.setDataType( DataWriter.FIELDDATA);
				out.print( hfs.getField("orderTotal").format(sumMap, ctx.msghandler), fieldSpan );
				out.println();

			} catch( SQLException sqlEx ) {
				out.println();
				out.print( sqlEx.getMessage() );
				logger.error( "internal error.", sqlEx );
			} finally {
				out.flush();
				out.close();
			}
		}

		return true;
	}

	private com.irt.data.Date getCalculatedRequestDeliveryDate( Context ctx, Map<String, Object> headerMap ) throws ServletModelException, SQLException {
		com.irt.data.Date defaultDateWhenSapNull = com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()).getDate(Order.SYSTEMRDD_PLUS_DAYS);
		return getCalculatedRequestDeliveryDate(ctx, headerMap, defaultDateWhenSapNull);
	}

	private com.irt.data.Date getCalculatedRequestDeliveryDate( Context ctx, Map<String, Object> headerMap, com.irt.data.Date defaultDateWhenSapNull ) throws ServletModelException, SQLException {
		return getCalculatedRequestDeliveryDate( ctx, headerMap, defaultDateWhenSapNull, false );
	}

	/**
	 * SAP에서 RDD를 받아서 발주 closing감안한 RDD값을 리턴함.
	 * 만약 SAP에서 RDD를 NULL로 리턴할 경우에는 파라미터로 받은 "defaultDateWhenSapNull" 이 사용됨.
	 *
	 * @param ctx
	 * @param headerMap
	 * @param defaultDateWhenSapNull
	 * @return
	 * @throws ServletModelException
	 * @throws SQLException
	 */
	private com.irt.data.Date getCalculatedRequestDeliveryDate( Context ctx, Map<String, Object> headerMap, com.irt.data.Date defaultDateWhenSapNull, boolean reOrder ) throws ServletModelException, SQLException {
		com.irt.dpr.tools.OrderCanonicalProcess ocp = new com.irt.dpr.tools.OrderCanonicalProcess( ctx.handler, systemConfig, com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_RDD );
		Map<String,Object> infMap = new HashMap<String, Object>();
		infMap.put( "orderKey", headerMap.get("orderKey") );
		infMap.put( "countryCode", getUserCountryCode(ctx) );
		infMap.put( "organizationCode", headerMap.get("organizationCode") );
		infMap.put( "distributionChannelCode", headerMap.get("distributionChannelCode") );
		infMap.put( "divisionCode", getDivisionCode(ctx) );
		infMap.put( "shipPartyCode", headerMap.get("shipPartyCode") );
		infMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		ocp.setParameter( infMap );

		com.irt.data.Date inDateNow = null;

		try {
			String inDateSapNowString = null;
			try {
				inDateSapNowString = ocp.execute();
			} catch( com.irt.dpr.OrderProcessException opEx ) {
				logger.error(opEx);
			}

			// sap(or webmethod) returns null
			if( inDateSapNowString == null || inDateSapNowString.length() == 0 ) {
				inDateNow = defaultDateWhenSapNull;
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.setTimeZone(ctx.sessionMng.getTimeZone());
				inDateNow = com.irt.data.Date.getInstance(sdf.parse(inDateSapNowString));
			}
		} catch( java.text.ParseException parseExIgnored ) {
		} finally {
			if( inDateNow == null ) {// maybe parse exceptions
				inDateNow = defaultDateWhenSapNull;
			}
		}
		if( reOrder ) {
			return inDateNow;
		} else {
			return getRequestDeliveryDate(ctx, inDateNow);
		}
	}

	protected boolean shortageDownload( Context ctx ) throws SQLException, ServletException, IOException  {
		ShortageEliminate shortageDB = new ShortageEliminate( ctx.handler );
		String orderKey = ctx.req.getParameter( "orderKey" );
		DataWriter out = createDataWriter( ctx, ctx.msghandler.getMessage("TITLE_DPR_PRODUCT_REQUIREMENT_SIM_") );

		if( out instanceof com.irt.util.SSDataWriter ) {
			org.apache.poi.ss.usermodel.Sheet sheet = ((com.irt.util.SSDataWriter)out).getSheet();
			sheet.setDisplayGridlines( false );
			sheet.setDisplayZeros( false );
		}

		try {
			Map<String, Object> conditionMap = new HashMap<String, Object>();
			String[] fieldKeys = { "itemCode", "itemCodeConfirmed", "itemConsumerEANCodeCNF", "itemName", "shelfLife", "qty", "uom", "pcQty" };
			conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
			conditionMap.put( "orderKey", orderKey );
			conditionMap.put( "status", "00" );
			List<Map<String, Object>> recordList = shortageDB.getRecords( conditionMap, fieldKeys );
			com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPRProductRequire.SIM%DOWN" );
			shortageDB.write( recordList, ctx.handler, out, columnList, Party.OPT_WRITING_TITLE, -1 );
		} catch( SQLException sqlEx ) {
			out.println();
			out.print( sqlEx.getMessage() );
			logger.error( "internal error.", sqlEx );
		} finally {
			out.flush();
			out.close();
		}

		return true;
	}

	protected boolean freegoodsDownload( Context ctx ) throws SQLException, ServletException, IOException  {
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;
		String orderKey = ctx.req.getParameter( "orderKey" );
		if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		DataWriter out = createDataWriter( ctx, ctx.msghandler.getMessage("TITLE_DPR_FREEGOODS_SIM_LIST") );
		if( out instanceof com.irt.util.SSDataWriter ) {
			org.apache.poi.ss.usermodel.Sheet sheet = ((com.irt.util.SSDataWriter)out).getSheet();
			sheet.setDisplayGridlines( false );
			sheet.setDisplayZeros( false );
		}

		try {
			Map<String, Object> conditionMap = new HashMap<String, Object>();
			conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
			conditionMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
			conditionMap.put( "orderKey", orderKey );
			conditionMap.put( "freegoodsInd", "Y" );
			conditionMap.put( "simulationTotalQty", null );
			conditionMap.put( "simulationTotalQty_min", 0 );
			conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );

			String[] fieldKeys = { "freegoodsInd", "orderQty", "simulationOrderQty", "inputTotalQty", "simulationTotalQty", "freegoodsRatio" };
			com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrder.SIM.RESULT.FREGOODS%LIST", "Y" );
			ServletUtility.setSort( ctx.req, detailDB, columnList.getSortKeys() );
			List<Map<String, Object>> recordList = detailDB.getRecords( conditionMap, columnList.getFieldKeys(fieldKeys) );

			String hasMost = "N";
			List<Map<String, Object>> freegoodsList = null;
			if( recordList != null ) {
				freegoodsList = new java.util.ArrayList<Map<String, Object>>();
				for( Map<String, Object> record : recordList ) {
					String result = new FreeGoods( ctx.handler ).makeFreegoodsValue( record ) ? "Y" : "N";
					if( "N".equals(hasMost) ) {
						hasMost = result;
					}
					if( "Y".equals(record.get("freegoodsInd")) ) {
						freegoodsList.add( record );
					}
				}
				columnList = getColumnList( ctx, "DPROrder.SIM.RESULT.FREGOODS%DOWN", hasMost );
			}

			new FreeGoods( ctx.handler ).write( recordList, ctx.handler, out, columnList, Party.OPT_WRITING_TITLE, -1 );
		} catch( SQLException sqlEx ) {
			out.println();
			out.print( sqlEx.getMessage() );
			logger.error( "internal error.", sqlEx );
		} finally {
			out.flush();
			out.close();
		}
		return true;
	}

	/** excel and text file */
	@Override
	protected com.irt.data.DataWriter createDataWriter( Context ctx, String filename ) throws IOException, ServletException {
		String fileType = getFileType(ctx);

		String encoding = ctx.req.getParameter( "encoding" );
		if( encoding == null ) encoding = systemConfig.getEncoding( ctx.locale );

		String header = ctx.req.getHeader( "User-Agent" );
		String name;
		if( header.contains("MSIE") || header.contains("Trident") ) {
			name = URLEncoder.encode( filename, "UTF-8" ).replaceAll( "\\+", "%20" );
		} else {
			name = new String( filename.getBytes("UTF-8"), "8859_1" );
		}

		String cookieOption = systemConfig.getCookieOption();
		cookieOption = cookieOption != null ? cookieOption : "";
		cookieOption = cookieOption.replaceAll( "HttpOnly;", "" );
		ctx.res.setContentType( com.irt.util.RBMWorkbook.getResponseContentType(fileType) );
		ctx.res.setHeader( "Content-Disposition", "attachment; filename="+ name +"." + com.irt.util.RBMWorkbook.getFileExtension(fileType) );
		ctx.res.setHeader( "Set-Cookie", "fileDownload=true; path=/;" + cookieOption );

		if( logger.isTraceEnabled() ) logger.trace( "createDataWriter: " + fileType + encoding );

		return com.irt.util.RBMWorkbook.getDataWriter( ctx.res.getOutputStream(), fileType, encoding );
	}

	/**
	 * 오더 클로징 감안한 RDD가져오기
	 *
	 * @param ctx
	 * @param rdd : SAP에서 받은 RDD(혹은 SAP에서 NULL을 받아서 default로 설정된)
	 * @return : 발주 closing시간을 감안하고 주말등을 감안한 rdd값 을 리턴함
	 */
	private com.irt.data.Date getRequestDeliveryDate( Context ctx, com.irt.data.Date rddFromSap ) {
		Map<String, Object> headerMap = new com.irt.servlet.ParameterMap( ctx.req, true );
		return new com.irt.dpr.RddOrderSteps( ctx.handler, getSystemConfig() ).getRddByChinaOrderClosingTime( headerMap, rddFromSap, ctx.sessionMng.getTimeZone() );
	}

	@SuppressWarnings("unused")
	private Object[] getQuickItemCodes( Context ctx ) throws ServletModelException, SQLException {
		com.irt.dpr.OrderItem itemDB = new com.irt.dpr.OrderItem( ctx.handler );

		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();

		String itemCode = ctx.req.getParameter( "itemCode" );
		String partyCode = ctx.req.getParameter( "soldPartyCode" );
		if( itemCode == null || itemCode.length() == 0 )
			return null;
		if( partyCode == null || partyCode.length() == 0 )
			return null;

		conditionMap.put( "itemCode", itemCode );
		conditionMap.put( "partyCode", partyCode );
		conditionMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
		conditionMap.put( "distributionChannelCode", getDistributionChannelCode(ctx) );
		conditionMap.put( "divisionCode", getDivisionCode(ctx) );

		com.irt.dpr.ItemCondition.setItemCondition( conditionMap, com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()) );

		List<Map<String, Object>> recordList = itemDB.getRecords( conditionMap, new String[] { "itemCode" } );

		if( recordList != null && recordList.size() > 0 )
			return Record.extractObjectArray( recordList, "itemCode" );

		return null;
	}

	@Override
	protected SystemConfig getSystemConfig() {
		return com.irt.custom.SystemConfig.getInstance( "DPR" );
	}

	@Override
	protected void initContext( Context ctx ) throws ServletException {
		PageConfig pageConfig = super.createPageConfig( ctx );

		if( MODE_DEFAULT.equals(ctx.mode) ) pageConfig.setMode( ctx.mode = MODE_ORDERINPUT );
		if( MODE_CREATION.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_FRAME.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_LISTCOUNT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_LOADTEMPLATE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_ORDERINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", null );
		else if( MODE_SET_TRADEPARTNER.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_SET_CREDITSTATUS.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_SHORTAGE_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_SIMULATION.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_SIMULATION_RESULT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_REGISTDETAIL.equals(ctx.mode) || MODE_UPDATEDETAIL.equals(ctx.mode) || MODE_REMOVEDETAIL.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_UPDATEHEADER.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_UPLOAD.equals(ctx.mode) || MODE_UPLOADINPUT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_WAIT.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_SHORTAGE_ELIMINATE.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_SHORTAGE_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_FREEGOODS_DOWNLOAD.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else if( MODE_FREEGOODS_LIST.equals(ctx.mode) )
			pageConfig.setSystemPackageCode( "DPR", "DPROrder.MNG" );
		else
			throw new ServletModelException( ServletModelException.INVALID_MODE );

		ctx.db = new Order( ctx.handler, systemConfig );
		ctx.extraObj = new OrderDetail( ctx.handler );

		if( ctx.req.getParameter("odrdlvGroup") != null )
			ctx.pageConfig.setProperty("odrdlvGroup", "odrdlvGroup");

		pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_"+ ctx.mode.toUpperCase()) );
		setPath( ctx, "jsp.MENU_ORDER" );
	}

	// inputOrderDetailCount
	@Override
	protected boolean listCount( Context ctx ) throws IOException, ServletException, SQLException {
		return true;
	}

	protected boolean loadTemplate( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;

		String orderKey = ctx.req.getParameter( "orderKey" );
		String templateKey = ctx.req.getParameter( "templateKey" );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( !db.checkProceedStatus(orderKey, Order.STATUS_WORKSHEET) )
			throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_ORD_CANNOT_MODIFY_ORDER") );
		else if( templateKey == null || templateKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		try {
			errorList = db.loadTemplate( orderKey, templateKey, ctx.sessionMng.getUniqId(), com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()) );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			errorList.add( createErrorMap(null, ctx.msghandler.getMessage("ERR_LOADTEMPLATE_FAILED", dataEx.getMessage())) );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			errorList.add( createErrorMap(null, ctx.msghandler.getMessage("ERR_LOADTEMPLATE_FAILED", sqlEx.getMessage())) );
		}

		if( errorList.size() < 2 ) {
			if( errorList.size() == 1 ) {
				Map<String, Object> map = errorList.get( 0 );
				ctx.pageConfig.setMessage( (String)map.get("message") );
			} else
				ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_LOAD_TEMPLATE_SUCCESS", (new com.irt.dpr.Template(ctx.handler)).getName(templateKey)) );

			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		} else {
			ctx.req.setAttribute( "errors", errorList );

			return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
		}
	}

	protected boolean mainFrame( Context ctx ) throws IOException, ServletException, SQLException {
		String orderKey = ctx.req.getParameter( "orderKey" );
		String reOrder = ctx.req.getParameter( "reOrder" );

		if( orderKey != null && orderKey.length() > 0  && orderKey.indexOf(">") < 0 && orderKey.indexOf("<") < 0
				&& orderKey.indexOf("+") < 0 && orderKey.indexOf(";") < 0 ) {
			ctx.pageConfig.setProperty( "orderKey", orderKey );
			ctx.pageConfig.setProperty( "reOrder", reOrder );

		}

		setPath( ctx, "TITLE_DPR_ORDER_FRM" );
		return forward( ctx, systemConfig.getJspPath() +"/dpr_order_main.jsp" );
	}

	protected boolean orderInput( Context ctx ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		String formatType = ctx.req.getParameter( PARAM_FORMATTYPE );

		if( formatType == null || formatType.length() == 0 )
			formatType = FORMATTYPE_PC;

		List<Map<String, Object>> detailList = null;
		com.irt.data.cols.ColumnList columnList = null;
		String organizationCode = getSavedOrganizationCode( ctx );
		String orderKey = ctx.req.getParameter( "orderKey" );

		List<String> optionKeyList = new ArrayList<String>();
		boolean useManualItemPrice =  com.irt.dpr.Country.isFeature(organizationCode, "useManualItemPrice");
		if( useManualItemPrice && com.irt.dpr.Country.isFeature(organizationCode, "useSinglePrice") )
			optionKeyList.add("useSinglePrice");
		if( com.irt.dpr.Country.isFeature(organizationCode, "useMoq") )
			optionKeyList.add("useMoq");

		String columnListName = null;
		com.irt.data.cols.ColumnList columnList_f = getColumnList( ctx, "DPROrder.ORDERITEM%LIST_F" );

		if( useManualItemPrice && com.irt.dpr.Country.isFeature(organizationCode, "useReOrder") ) {
			boolean noSim = true;
			if( orderKey != null && orderKey.length() > 0 ) {
				noSim = headerDB.checkProceedStatus( orderKey, Order.STATUS_WORKSHEET );
			}
			if( noSim ) {
				optionKeyList.add("WK");
			} else {
				optionKeyList.remove("WK");
			}
//			columnList = getColumnList( ctx, "DPROrder.ORDERITEM%LIST.CN."+ formatType.toUpperCase(),optionKeyList.toArray(new String[0]) );
			columnListName = "DPROrder.ORDERITEM%LIST.CN."+ formatType.toUpperCase();
			String rtype = ctx.req.getParameter( PARAM_REQUESTTYPE );
			if( REQTYPE_REGIST.equals(rtype) ) {
				ctx.pageConfig.setProperty( PARAM_REQUESTTYPE, rtype );
			}
		} else if( useManualItemPrice ) {
			columnListName = "DPROrder.ORDERITEM%LIST."+ formatType.toUpperCase();
//			columnList = getColumnList( ctx, "DPROrder.ORDERITEM%LIST."+ formatType.toUpperCase(), optionKeyList.toArray(new String[0]) );
		} else {
			columnListName = "DPROrder.ORDERITEM%LIST."+ formatType.toUpperCase();
//			columnList = getColumnList( ctx, "DPROrder.ORDERITEM%LIST."+ formatType.toUpperCase(), optionKeyList.toArray(new String[0]) );
		}

		boolean resetUseSelectRdd = false;
		Map<String, Object> headerMap = new java.util.HashMap<String, Object>();
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		if( orderKey != null && orderKey.length() > 0 ) {
			if( !headerDB.checkProceedStatus(orderKey, Order.STATUS_WORKSHEET) )
				throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_ORD_CANNOT_MODIFY_ORDER") );

			conditionMap = Order.createPrimary( orderKey );
			headerMap = headerDB.getRecord( conditionMap );

			columnList = getColumnList(ctx, columnListName, optionKeyList.toArray(new String[0]));
			ServletUtility.setSort( ctx.req, detailDB, columnList_f.getSortKeys() );
			//ServletUtility.setSort( ctx.req, detailDB, columnList.getSortKeys() );

			conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
			conditionMap.put( "formatUOM", formatType.toUpperCase() );
			conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );

			com.irt.dpr.OrderItem itemDB = new com.irt.dpr.OrderItem( ctx.handler );
			Map<String, Object> primaryMap = com.irt.dpr.Party.createPrimary( (String) headerMap.get("soldPartyCode"),
					(String) headerMap.get("organizationCode"), (String) headerMap.get("distributionChannelCode"),
					getDivisionCode(ctx) );
			String allowUOM = null;
			if( com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM") )
				allowUOM = itemDB.getDistAllowUOM(primaryMap);
			if (allowUOM == null)
				allowUOM = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "uom;" + organizationCode, com.irt.dpr.Party.DEFAULT_UOM);

			ctx.pageConfig.setProperty( "allowUOM", allowUOM );

			{
				com.irt.data.Date orderDate = (com.irt.data.Date)headerMap.get( "orderDate" );
				com.irt.data.Date reOrderDate = com.irt.data.Date.getInstance( ctx.sessionMng.getTimeZone() );
				if( orderDate.compareTo( reOrderDate ) != 0 && com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") ) {
					resetUseSelectRdd = new RddTrigger( ctx.handler )
							.isPredefiendRdd( (String)headerMap.get("organizationCode"), (String)headerMap.get("distributionChannelCode")
									, getDivisionCode(ctx), (String)headerMap.get("soldPartyCode"), (String)headerMap.get("shipPartyCode") );
				}
			}

			//reOrder값은 Order_Main에서 가져옴
			String reOrder = ctx.req.getParameter( "reOrder" );
			if( "Y".equals(reOrder) && com.irt.dpr.Country.isFeature(organizationCode, "useReOrder") ) {
				boolean stateCanExecuteSimulation = headerDB.checkProceedStatus(orderKey, Order.STATUS_SIMULATED);

				com.irt.data.Date reOrderDate = com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone());
				com.irt.data.Date inDateDefault = (com.irt.data.Date)headerMap.get( "inDateDefault" );

				com.irt.data.Date calcedInDate = null;

				String status = Record.extractString( headerMap, "status" );
				boolean stateShouldNotUpdateOrder = (status == null || status.length() == 0
						|| Order.STATUS_SIMULATING.equals(status) || Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status));

				com.irt.data.Date inDateReOrderDefault = null;
				if( stateShouldNotUpdateOrder ) {
					try {
						throw new DataException( DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_STATUS") );
					} catch( DataException dataEx ) {
						ctx.handler.rollback();
						ctx.pageConfig.setMessage( dataEx.getMessage() );
						ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
						logger.info( "error.", dataEx );
					}
//				} else if( inDateDefault == null || Order.STATUS_SIMULATED.equals(status) || Order.STATUS_ERROR.equals(status) ) {
				}

				com.irt.data.Date ordDateAtFirstOrder = (com.irt.data.Date)headerMap.get( "orderDate" );
				com.irt.data.Date inDateDefaultedIfReceivedNullNow = com.irt.data.Date.getInstance(reOrderDate.getDate(SYSTEMRDD_PLUS_DAYS));
				if( inDateDefault != null ) {
					long diffMillisBetween= inDateDefault.getTime() - ordDateAtFirstOrder.getTime();
					long diffDaysNumBetween= java.util.concurrent.TimeUnit.DAYS.convert(diffMillisBetween, TimeUnit.MILLISECONDS);
					inDateReOrderDefault = inDateDefaultedIfReceivedNullNow;
					if( diffDaysNumBetween > 0 ) {
						inDateReOrderDefault = com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()).getDate(new Long(diffDaysNumBetween).intValue());
					}
				} else {
					inDateReOrderDefault = inDateDefaultedIfReceivedNullNow;
				}

				calcedInDate = getCalculatedRequestDeliveryDate(ctx, headerMap, inDateReOrderDefault, true);

				if( headerMap != null && stateCanExecuteSimulation ) {
					Map<String,Object> reOrderMap = new HashMap<String, Object>(headerMap);
					reOrderMap.remove("orderDate");
					reOrderMap.put("orderDate", reOrderDate);
					reOrderMap.remove("inDate");
					if( !resetUseSelectRdd ) {
						reOrderMap.put("inDate", calcedInDate);// used Simulation/PlaceOrder to send as XML 'RequestDeliveryDate'
					}
					reOrderMap.remove("inDateDefault");
					reOrderMap.put("inDateDefault", calcedInDate);// history purpose
					reOrderMap.remove("inDateSimulation");

					try {
						if( headerDB.updateReOrderHeader(orderKey, reOrderMap) ) {
							headerMap = reOrderMap;
						}
					} catch( DataException dataEx ) {
						logger.error(dataEx);
						ctx.handler.rollback();
					}
				}
			}

			com.irt.data.Date availableDate =  com.irt.data.Date.getInstance();

			conditionMap.put( "availableDate", availableDate );

			String[] fieldKeys = columnList_f.getFieldKeys( columnList.getFieldKeys(new String[] { "lineNumber", "orderQtySimulation" }) );
			//String[] fieldKeys = columnList.getFieldKeys(new String[] { "lineNumber", "orderQtySimulation" });
			String orderType = (String)headerMap.get( "orderType" );
			boolean useDangerousItem = Country.isFeature( organizationCode, "useDangerousItem" );
			if( useDangerousItem ) {
				String dangerousPlant = Country.getFeatureValue( organizationCode, "dangerousPlant" );
				if( dangerousPlant != null ) {
					conditionMap.put( "dangerousPlant", dangerousPlant );
					conditionMap.put( "dangerousNumber", Item.ITEMTYPE_DANGEROUS );
				} else {
					throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_NEEDED_ENVIRONMENT", "dangerousPlant") );
				}
				fieldKeys = com.irt.util.Arrays.append( fieldKeys, "dangerousInd" );
			}

			detailList = detailDB.getRecords( conditionMap, fieldKeys );

			int detailCount = detailDB.getDetailCount( orderKey );
			boolean invalidSellingSKU = false;
			invalidSellingSKU = detailCount > ( detailList != null ? detailList.size() : 0 );

			if( invalidSellingSKU ) {
				throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_INVALID_SELLINGSKU_ORDER_ITEM") );
			}

			if( detailList != null && detailList.size() > 0 ) {
				ArrayList<String> delLineNumberList = new ArrayList<String>();
				int detailListSize = detailList.size();
				boolean invalidItem = false;
				for( int i = 0; i < detailListSize; i++ ) {
					Map<String, Object> detailMap = detailList.get(i);
					String plantInd = (String) detailMap.get( "plantInd" );
					String itemDisplayInd = (String) detailMap.get( "itemDisplayInd" );
					if( (plantInd != null && plantInd.length() > 0 && "Y".equals(plantInd))
							|| (itemDisplayInd != null && itemDisplayInd.length() > 0 && "N".equals(itemDisplayInd)) ) {
						delLineNumberList.add( detailMap.get("lineNumber").toString() );
						invalidItem = true;
					} else if( useDangerousItem ) {
						boolean isDangerousItem = "Y".equals( detailMap.get("dangerousInd") );
						if( Order.ORDER_TYPE_DANGEROUS.equals(orderType) ) {
							if( !isDangerousItem ) {
								delLineNumberList.add( detailMap.get("lineNumber").toString() );
								invalidItem = true;
							}
						} else {
							if( isDangerousItem ) {
								delLineNumberList.add( detailMap.get("lineNumber").toString() );
								invalidItem = true;
							}
						}
					}
				}
				if( delLineNumberList.size() > 0 ) {
					ctx.pageConfig.setProperty( "invalidItem", invalidItem ? "Y" : "N" );
					String[] delLineNumbers = delLineNumberList.toArray( new String[delLineNumberList.size()] );
					if( !((Order)ctx.db).lockOrder(orderKey) )
						throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_RECORD_LOCKED") );

					Map<String, Object> deleteMap = new HashMap<String, Object>();
					deleteMap.put( "orderKey", orderKey );

					Object status = ((Order)ctx.db).getFieldValue( Order.createPrimary(orderKey), "status" );
					if( Order.STATUS_SIMULATING.equals(status) || Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
						throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_DELETE_STATUS") );
					else if( Order.STATUS_SIMULATED.equals(status) )
						((Order)ctx.db).updateStatusWithDetail( orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId() );
					else if( Order.STATUS_ERROR.equals(status) )
						((Order)ctx.db).updateStatus( orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId() );

					try {
						if( detailDB.deleteWithLineNoUpdate(orderKey, delLineNumbers) <= 0 )
							throw new DataException( DataException.ERR_CANNOT_DELETE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE) );
					} catch( DataException dataEx ) {
						ctx.handler.rollback();
						//throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE) );
					} catch( SQLException sqlEx ) {
						ctx.handler.rollback();
						//throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE) );
					}

					ctx.handler.commit();
					detailList = detailDB.getRecords( conditionMap, columnList_f.getFieldKeys(columnList.getFieldKeys(new String[] { "lineNumber", "orderQtySimulation" })) );
					//detailList = detailDB.getRecords( conditionMap, columnList.getFieldKeys(new String[] { "lineNumber", "orderQtySimulation" }) );
				}
			}

			ctx.req.setAttribute("details", getDetailListPushUOM(ctx, detailList));

			Map<String, Object> tradePartnerMap = new java.util.HashMap<String, Object>();
			if( headerMap != null ) {
				if( !com.irt.data.Date.getInstance( ctx.sessionMng.getTimeZone() ).equals(headerMap.get("orderDate")) && !headerDB.checkProceedStatus(orderKey, Order.STATUS_SIMULATED) )
					throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_ORD_CANNOT_MODIFY_ORDER") );

				tradePartnerMap.put( "organizationCode", headerMap.get("organizationCode") );
				tradePartnerMap.put( "distributionChannelCode", headerMap.get("distributionChannelCode") );
				tradePartnerMap.put( "soldPartyCode", headerMap.get("soldPartyCode") );
				tradePartnerMap.put( "shipPartyCode", headerMap.get("shipPartyCode") );
			}
			setAttributePartner( ctx, tradePartnerMap );
			setPropertyOrderDeliveryGroup( ctx, headerMap );
		} else {
			columnList = getColumnList(ctx, columnListName, optionKeyList.toArray(new String[0]));
			com.irt.data.Date today = com.irt.data.Date.getInstance( ctx.sessionMng.getTimeZone() );
			headerMap = new java.util.HashMap<String, Object>();
			headerMap.put( "countryCode", getUserCountryCode(ctx) );
			headerMap.put( "orderDate", today );
			if( !"JNJAP_KR".equals(ctx.sessionMng.getPartyId()) ) {
				headerMap.put( "inDate", getRequestDeliveryDate(ctx, today.getDate(SYSTEMRDD_PLUS_DAYS)) );
			}

			setDefaultParameter( ctx, headerMap );
			organizationCode = (String)headerMap.get( "organizationCode" );
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
		}

		{
			String[] dateFieldKeys = { "orderDate", "inDate", "inDateDefault", "inDateSimulation", "inDateConfirm" };
			for( String fieldKey : dateFieldKeys ) {
				java.util.Date dt = (java.util.Date)headerMap.get( fieldKey );
				if( dt != null ) {
					Calendar cal = Calendar.getInstance() ;
					cal.setTime( dt );
					int dayOfWeek = cal.get( Calendar.DAY_OF_WEEK );
					headerMap.put( fieldKey + "DOW", ctx.msghandler.getMessage("jsp.dpr_order_input.MSG_DAYOFWEEK_" + dayOfWeek) );
				}
			}
		}

		ctx.req.setAttribute( "header", headerMap );
		ctx.req.setAttribute( "headerFieldSet", headerDB.getFieldSet(true) );
		ctx.req.setAttribute( "details", detailList );
		ctx.req.setAttribute( "columnList", columnList );
		ctx.req.setAttribute( "columnList_f", columnList_f );

		setAttributePartyMaster( ctx, headerMap, PARTYMASTER_ORGANIZATION | PARTYMASTER_DISTRIBUTIONCHANNEL );

		ctx.pageConfig.getListIndexVariables()[2] = (detailList != null ? detailList.size() : 0 );
		if( com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()).equals(headerMap.get("orderDate")) ) {
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPROrder.MNG") );
			if( resetUseSelectRdd ) {
				ctx.pageConfig.setProperty( "resetUseSelectRdd", "Y" );
			}
		} else {
			ctx.pageConfig.setManageAuth( false );
		}

		if( !"Y".equals(ctx.req.getParameter("isContinueOrder")) ) {
			if( headerDB.checkCreatingStatus((String) headerMap.get("soldPartyCode"), (String) headerMap.get("shipPartyCode")) )
				ctx.pageConfig.setProperty( "hasCreatingOrder", "Y" );
		}
		String message = ctx.req.getParameter( "msg" );
		if( message != null && message.length() > 0 ) {
			ctx.pageConfig.setMessage( message );
		}

		setCreditStatusList(ctx);

		setMobileFunction(ctx);

		return forward( ctx, systemConfig.getJspPath() +"/dpr_order_input.jsp" );
	}

	private void setMobileFunction(Context ctx) throws SQLException, ServletException {
		com.irt.data.cols.ColumnList itemColumnList = getColumnList(ctx, "DPRItem.OITM.IOR%LIST");
		ctx.req.setAttribute("itemColumnList", itemColumnList);

		setAttributeOrganizationBrands(ctx);
	}

	protected boolean placeOrder( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;
		String orderKey = ctx.req.getParameter( "orderKey" );
		String msg = null;

		if( Order.STATUS_CREATING.equals(db.checkCurrentStatus(orderKey)) ) {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_WAITING_CREATION_AGAIN") );

			String url = ctx.pageConfig.getBackURL();
			url = HtmlUtility.replaceURLQuery(url, "mode", "simr");
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}

		if( !checkInvalidItem(ctx) ) {
			String url = ctx.pageConfig.getBackURL();
			url = HtmlUtility.replaceURLQuery(url, "mode", "simr");
			return sendRedirect( ctx, url );
		}

		checkMaxOrderQtyRegular(ctx);

		if( !db.checkProceedStatus(orderKey, Order.STATUS_CREATED) )
			throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_ORD_CANNOT_PROCEED_CREATION") );

		SQLHandler this_handler = null;
		try {
			com.irt.custom.SystemConfig sysConfig = com.irt.custom.SystemConfig.getInstance("RBM");
			this_handler = sysConfig.createSQLHandler(sysConfig.getMessageHandler(ctx.locale));
			if( this_handler == null )
				throw new ServletModelException( ServletModelException.INVALID_DATAHANDLER );

			if( !db.updateStatus(this_handler, orderKey, Order.STATUS_CREATING, ctx.sessionMng.getUniqId()) ){
				logger.debug( "error.: " + orderKey + " : " + "ERR_CANNOT_UPDATE_STATUS" + "CREATING" );
				throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_STATUS", "CREATING") );
			} else {
				logger.debug("debug.: " + orderKey + " db.updateStatus commit.");
				this_handler.commit();
			}
		} catch( Exception e ) {
			logger.debug( "error.: "+ orderKey, e );
		} finally {
			try{ if( this_handler != null ) this_handler.close(); } catch (Exception ignored) {}
		}

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String redirectURL = ctx.pageConfig.getBackURL();

		boolean successPlaceOrder = false;
		boolean successFreegoodsOrder = false;
		boolean isFreegoods = false;
		List<Map<String, Object>> freegoodsList = null;
		try {
			String freegoodsOrderKey = null;

			if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useFreegoods") ) {
				this_handler = null;
				try {
					com.irt.custom.SystemConfig sysConfig = com.irt.custom.SystemConfig.getInstance("RBM");
					this_handler = sysConfig.createSQLHandler(sysConfig.getMessageHandler(ctx.locale));
					if( this_handler == null )
						throw new ServletModelException( ServletModelException.INVALID_DATAHANDLER, ctx.msghandler.getMessage("MSG_FREEGOODS_CREATION_FAILED") );
					Order orderDB = new Order( this_handler );
					Map<String, Object> headerMap = orderDB.getRecord( Order.createPrimary(orderKey), new String[] { "freegoodsOrderWay", "simulationKey", "customerOrderNumber" } );
					String freegoodsOrderWay = (String) headerMap.get("freegoodsOrderWay");
					if( freegoodsOrderWay == null ) {
						freegoodsOrderWay = FreeGoods.ORDER_ONLY_NORMAL;
					}

					if( !FreeGoods.ORDER_ONLY_NORMAL.equals(freegoodsOrderWay) ) {
						isFreegoods = true;
						String[] fieldKeys = { "simulationKey" };
						Map<String, Object> recordMap = Record.createMap( "simulationKey", headerMap.get("simulationKey") + Order.NORMAL_ORDERKEY_SUFFIX );
						recordMap.put( "orderKey", orderKey );
						String customerOrderNumber = (String)headerMap.get( "customerOrderNumber" );
						if( customerOrderNumber != null && customerOrderNumber.length() > 0 ) {
							customerOrderNumber += Order.NORMAL_ORDERKEY_SUFFIX;
							recordMap.put( "customerOrderNumber", customerOrderNumber );
							fieldKeys = Arrays.append( fieldKeys, "customerOrderNumber" );
						}
						orderDB.modify( recordMap, fieldKeys );

						this_handler.commit();
						freegoodsList = new FreeGoods( this_handler ).registFreegoodsOrder( orderKey, ctx.sessionMng );
					}
				} catch( Exception e ) {
					this_handler.rollback();
					logger.debug( "error.Freegoods Order Regist error: ", e );
					Loggers.business.debug( "{}: {}", "error.Freegoods Order Regist error: ", e.getMessage() );
					msg = msg != null ? msg + " " + ctx.msghandler.getMessage( "MSG_FREEGOODS_CREATION_FAILED" ) : ctx.msghandler.getMessage( "MSG_FREEGOODS_CREATION_FAILED" );
				} finally {
					this_handler.commit();
					try{ if( this_handler != null ) this_handler.close(); } catch (Exception ignored) {}
				}
			}

			successPlaceOrder = executePlaceOrderProcess( ctx, orderKey, com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_CREATION );

			if( successPlaceOrder ) {
				Map<String, Object> headerMap = db.getRecord( Order.createPrimary(orderKey), new String[] { "orderDate", "orderNumber", "status" } );
				String status = (String) headerMap.get( "status" );
				if( Order.STATUS_CREATED.equals(status) ) {
					ctx.handler.commit();
					Map<String,Object> statusListCondition = new HashMap<String,Object>();
					statusListCondition.put("startOrderDate", com.irt.data.Date.getInstance());
					statusListCondition.put("endOrderDate", com.irt.data.Date.getInstance().getDate(1));
					statusListCondition.put("orderNumber", headerMap.get("orderNumber"));
					executeStatusList(ctx, statusListCondition);

					msg = ctx.msghandler.getMessage( "MSG_CREATION_SUCCESS" );
					redirectURL = systemConfig.getClassURL() + "/DPREnquiryOrder?" + ctx.req.getQueryString();
					redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "mode", "info" );
					redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "create", "Y" );
					redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "status", Order.STATUS_CREATED );
				} else {
					ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
					msg = ctx.msghandler.getMessage( "MSG_CREATION_FAILED" );
				}
			} else {
				throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("MSG_CREATION_FAILED") );
			}

			if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useFreegoods") && freegoodsList != null ) {
				freegoodsOrderKey = (String) freegoodsList.get(0).get( "orderKey" );
				//executePlaceOrderProcess( ctx, freegoodsOrderKey, com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_SIMULATION );
				successFreegoodsOrder = executePlaceOrderProcess( ctx, freegoodsOrderKey, com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_CREATION );

				if( successFreegoodsOrder ) {
					db.updateOrderStatus( ctx.handler, freegoodsOrderKey, Order.ORDERSTATUS_HOLD, ctx.sessionMng.getUniqId() );
					Map<String, Object> headerMap = db.getRecord( Order.createPrimary(freegoodsOrderKey), new String[] { "orderDate", "orderNumber", "status" } );
					Map<String,Object> statusListCondition = new HashMap<String,Object>();
					statusListCondition.put("startOrderDate", com.irt.data.Date.getInstance().getDate(-1));
					statusListCondition.put("endOrderDate", com.irt.data.Date.getInstance().getDate(1));
					statusListCondition.put("orderNumber", headerMap.get("orderNumber"));
					executeStatusList(ctx, statusListCondition);

					Map<String, Object> fgConditionMap = new java.util.HashMap<String, Object>();
					fgConditionMap.put( "orderKey", freegoodsOrderKey );
					fgConditionMap.put( "status", Order.STATUS_CREATED );
					msg = msg != null ? msg + " " + ctx.msghandler.getMessage( "MSG_FREEGOODS_CREATION_SUCCESS" ) : ctx.msghandler.getMessage( "MSG_FREEGOODS_CREATION_SUCCESS" );
					executeStatus( ctx, fgConditionMap );
					FreeGoods fgDB = new FreeGoods( ctx.handler );
					fgDB.updateFreegoodsQty( ctx.handler, freegoodsOrderKey );
				}
			}
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			msg = msg != null ? msg + " " + dataEx.getMessage() : dataEx.getMessage();
			logger.info( "error.", dataEx );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			msg = msg != null ? msg + " " + sqlEx.getMessage() : sqlEx.getMessage();
			logger.error( "internal error.", sqlEx );
		} finally {
			ctx.handler.commit();
			if( !successPlaceOrder ) {
				ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
				db.updateStatus( ctx.handler, orderKey, Order.STATUS_ERROR, ctx.sessionMng.getUniqId() );
				ctx.handler.commit();
			}
			if(com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useFreegoods") && successPlaceOrder && !successFreegoodsOrder && isFreegoods ) {
				try {
					HelpBoard hboardDB = new HelpBoard( ctx.handler );
					UserUser userDB = new UserUser( ctx.handler );
					Map<String, Object> userMap = userDB.getRecord( UserUser.createPrimary(ctx.sessionMng.getPartyId(), ctx.sessionMng.getUserId()) );
					Map<String, Object> headerMap = db.getRecord( Order.createPrimary(orderKey)
							, new String[] { "soldPartyCode", "soldPartyName", "shipPartyCode", "shipPartyName", "organizationCode", "orderNumber", "freegoodsOrderWay", "simulationKey" } );
					String howToOrder = (String) headerMap.get( "freegoodsOrderWay" );
					String boardClassCode = hboardDB.makeBoardClassCode( getSavedOrganizationCode(ctx) );
					String headwordCode = new BoardHeadword(ctx.handler ).getHeadwordCode( boardClassCode, "FO" );
					Map<String, Object> recordMap = new java.util.HashMap<String, Object>();
					String email = (String) userMap.get( "email" );
					if( email == null || email.length() < 1 ) {
						email = "none";
					}
					String tel = (String) userMap.get( "telephone" );
					if( tel == null || tel.length() < 1 ) {
						tel = "none";
					}
					String freegoodsOrderKey = db.getFreegoodsOrderKey( orderKey );
					if( freegoodsOrderKey != null ) {
						db.updateStatus( ctx.handler, freegoodsOrderKey, Order.STATUS_ERROR, ctx.sessionMng.getUniqId() );
					}

					recordMap.put( "boardClassCode", boardClassCode );
					recordMap.put( "headwordCode", headwordCode );
					recordMap.put( "boardOption", Board.BOARDOPTION_TEXT );
					recordMap.put( "boardType", Board.BOARDTYPE_NORMAL );
					recordMap.put( "registUserId", ctx.sessionMng.getUniqId() );
					recordMap.put( "userName", ctx.sessionMng.getUserName() );
					recordMap.put( "email", email );
					recordMap.put( "tel", tel );
					recordMap.put( "registUserPartyName", ctx.sessionMng.getPartyName() );
					recordMap.put( "registUserUserId", ctx.sessionMng.getUserId() );

					OrderDetail detailDB = (OrderDetail)ctx.extraObj;
					Map<String, Object> conditionMap = new HashMap<String, Object>();
					conditionMap.put( "orderKey", orderKey );
					conditionMap.put( "freegoodsInd", "Y" );
					conditionMap.put( "simulationTotalQty", null );
					conditionMap.put( "simulationTotalQty_min", 0 );
					conditionMap.put( "organizationCode", headerMap.get("organizationCode") );
					conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
					conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );

					String[] fieldKeys = { "lineNumber", "itemCode", "itemCodeConfirmed", "itemNameConfirmed", "itemRefInd", "shelfLife", "uom", "orderQty"
							, "simulationOrderQty", "inputTotalQty", "simulationTotalQty", "freegoodsRatio", "freegoodsQty", "price", "packSize" };

					String officeCode = (String) new Party( ctx.handler ).getFieldValue( headerMap, "officeCode" );
					Map<String, Object> fgConditionMap = new HashMap<String, Object>();
					fgConditionMap.put( "organizationCode", headerMap.get("organizationCode") );
					fgConditionMap.put( "officeCode", officeCode );
					fgConditionMap.put( "partyCode", headerMap.get("partyCode") );

					String[] mostFieldKeys = new String[2];
					if( FreeGoods.ORDER_MOST_NORMAL.equals(howToOrder) ) {
						mostFieldKeys[0] = "mostNormalOrderQty";
						mostFieldKeys[1] = "mostNormalFreegoodsQty";
					} else if( FreeGoods.ORDER_MOST_FREEGOODS.equals(howToOrder) ) {
						mostFieldKeys[0] = "mostFreegoodsOrderQty";
						mostFieldKeys[1] = "mostFreegoodsQty";
					}
					List<Map<String, Object>> detailList = detailDB.getRecords( conditionMap, fieldKeys );
					com.irt.dpr.FreeGoods fgDB = new com.irt.dpr.FreeGoods( ctx.handler );

					if( detailList != null && detailList.size() > 0 ) {
						String orderType = (String)headerMap.get( "orderType" );
						StringBuffer content = new StringBuffer();
						if( Order.ORDER_TYPE_DANGEROUS.equals(orderType) ) {
							content.append( ctx.msghandler.getMessage("MSG_DPR_DANGER_ORDERNUMBER") );
						} else {
							content.append( ctx.msghandler.getMessage("MSG_DPR_NORMAL_ORDERNUMBER") );
						}
						content.append( " : " );
						content.append( (String)headerMap.get("orderNumber") );
						content.append( "\r\n" );
						if( Order.ORDER_TYPE_DANGEROUS.equals(orderType) ) {
							content.append( ctx.msghandler.getMessage("MSG_DPR_DANGER_PONUMBER") );
						} else {
							content.append( ctx.msghandler.getMessage("MSG_DPR_NORMAL_PONUMBER") );
						}
						content.append( " : " );
						content.append( (String)headerMap.get("simulationKey") );
						content.append( "\r\n" );
						content.append( ctx.msghandler.getMessage("FIELD_DPR_ORDER_SOLDPARTYCODE") );
						content.append( " : [" );
						content.append( (String)headerMap.get("soldPartyCode") );
						content.append( "] " );
						content.append( (String)headerMap.get("soldPartyName") );
						content.append( "\r\n" );
						content.append( ctx.msghandler.getMessage("FIELD_DPR_ORDER_SHIPPARTYCODE") );
						content.append( " : [" );
						content.append( (String)headerMap.get("shipPartyCode") );
						content.append( "] " );
						content.append( (String)headerMap.get("shipPartyName") );
						content.append( "\r\n\n" );
						content.append( ctx.msghandler.getMessage("MSG_DPR_FREEGOODS_ORDER_DETAIL") );
						content.append( " " );
						content.append( ctx.msghandler.getMessage("MSG_DPR_FREEGOODS_TOTAL_ITEM_COUNT", String.valueOf(detailList.size())) );
						content.append( "\r\n" );
						for( Map<String, Object> record : detailList ) {
							fgDB.makeFreegoodsValue( record );
							int orderQty = (Integer)record.get( mostFieldKeys[0] );
							fgConditionMap.put( "itemCode", record.get("itemCode") );
							int freegoodsQty = fgDB.getFreeGoodsQty( fgConditionMap, orderQty );
							content.append( "[" );
							content.append( (String)record.get("itemCode") );
							content.append( "] " );
							content.append( (String)record.get("itemNameConfirmed") );
							content.append( ", " );
							content.append( ctx.msghandler.getMessage("MSG_DPR_FREEGOODS_QTY") );
							content.append( " : " );
							content.append( freegoodsQty );
							content.append( "\r\n" );
						}
						recordMap.put( "content", content.toString() );
						hboardDB.regist( recordMap );
						msg += ctx.msghandler.getMessage( "MSG_FREEGOODS_CREATION_FAILED" ) + " " + ctx.msghandler.getMessage( "MSG_DPR_FREEGOODS_FAILED_HELPDESK_REG" );
						ctx.handler.commit();
					}
				} catch( DataException dataEx ) {
					ctx.handler.rollback();
					logger.debug( "error.: "+ orderKey, dataEx );
					Loggers.business.debug( "{}: {}", orderKey, dataEx.getMessage() );
					msg += ctx.msghandler.getMessage( "MSG_FREEGOODS_CREATION_FAILED" ) + " " + ctx.msghandler.getMessage( "MSG_DPR_FREEGOODS_HELPDESK_REG_FAILED" ) + " " + dataEx.getMessage();
				} catch( Exception e ) {
					ctx.handler.rollback();
					logger.debug( "error.: "+ orderKey, e );
					Loggers.business.debug( "{}: {}", orderKey, e.getMessage() );
					msg += ctx.msghandler.getMessage( "MSG_FREEGOODS_CREATION_FAILED" ) + " " + ctx.msghandler.getMessage( "MSG_DPR_FREEGOODS_HELPDESK_REG_FAILED" ) + " " + e.getMessage();
				}
			}
		}

		String rtype = ctx.req.getParameter( "rtype" );
		if( REQTYPE_REGIST.equals(rtype) ) {
			List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();
			redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "rtype", rtype );
			shortageRegist( ctx, errorList );
			if( errorList.size() > 0 ) {
				if( msg != null ) {
					msg += " " + ctx.msghandler.getMessage( "MSG_DPR_ORDER_SHORTAGE_REGIST_FAILED", String.valueOf(errorList.size()) );
				} else {
					msg = ctx.msghandler.getMessage( "MSG_DPR_ORDER_SHORTAGE_REGIST_FAILED", String.valueOf(errorList.size()) );
				}
			} else {
				if( msg != null ) {
					msg += " " + ctx.msghandler.getMessage( "MSG_DPR_ORDER_SHORTAGE_REGIST_SUCCESS" );
				} else {
					msg = ctx.msghandler.getMessage( "MSG_DPR_ORDER_SHORTAGE_REGIST_SUCCESS" );
				}
			}
		}

		ctx.pageConfig.setMessage( msg );

		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "rmsg", URLEncoder.encode(msg, StandardCharsets.UTF_8.name()) );
		redirectURL = HtmlUtility.replaceURLQuery( redirectURL, "rlevel", String.valueOf(ctx.pageConfig.getResultLevel()) );

		return sendRedirect( ctx, HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
	}

	/**
	**	DPR_ORDER_DTL: INSERT
	**/
	protected boolean registDetail( Context ctx ) throws IOException, ServletException, SQLException {
		OrderDetail db = (OrderDetail)ctx.extraObj;
		Map<String, Object> recordMap = new java.util.HashMap<String, Object>();

		String rtype = ctx.req.getParameter( PARAM_REQUESTTYPE );
		String orderKey = ctx.req.getParameter( "orderKey" );
		String organizationCode = ctx.req.getParameter( "organizationCode" );
		String distributionChannelCode = getDistributionChannelCode( ctx );
		String soldPartyCode = ctx.req.getParameter( "soldPartyCode" );
		Object[] itemCodes = null;
		Object[] checkItems = ctx.req.getParameterValues( "checkItems" );
		if( checkItems != null && checkItems.length > 0 ) {
			itemCodes = new Object[checkItems.length];

			for( int i = 0; i < checkItems.length; i++ )
				itemCodes[i] = (((String)checkItems[i]).split(";"))[0];
		}

		List<String> optionKeyList = new ArrayList<String>();
		if( com.irt.dpr.Country.isFeature(organizationCode, "useMoq") )
			optionKeyList.add("useMoq");

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( organizationCode == null || organizationCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( soldPartyCode == null || soldPartyCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( distributionChannelCode == null || distributionChannelCode.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( itemCodes == null || itemCodes.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( !((Order)ctx.db).checkProceedStatus(orderKey, Order.STATUS_WORKSHEET) )
			throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_ORD_CANNOT_MODIFY_ORDER") );

		if( !((Order)ctx.db).lockOrder(orderKey) )
			throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_RECORD_LOCKED") );

		Object status = ((Order)ctx.db).getFieldValue( Order.createPrimary(orderKey), "status" );
		String orderType = (String)((Order)ctx.db).getFieldValue( Order.createPrimary(orderKey), "orderType" );
		boolean noSim = !Order.STATUS_SIMULATED.equals(status);
		if( Order.STATUS_SIMULATING.equals(status) || Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
			throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_STATUS") );
		else if( Order.STATUS_ERROR.equals(status) || Order.STATUS_SIMULATED.equals(status) )
			((Order)ctx.db).updateStatus( orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId() );

		String orderQty = String.valueOf( ((Country.getDefault(organizationCode, "orderQty") == null ? DEFAULT_ORDERQTY : Country.getDefault(organizationCode, "orderQty"))) );
		String inputUOM = getDefaultUnitOfMeasure( ctx );
		com.irt.data.Date availableDate = null;
		// Quick Add Item
		if( REQTYPE_QUICK.equals(rtype) ) {
			orderQty = ctx.req.getParameter( "inputOrderQty" );
			if ( orderQty == null || orderQty.length() < 0 )
				orderQty = String.valueOf( ((Country.getDefault(organizationCode, "orderQty") == null ? DEFAULT_ORDERQTY : Country.getDefault(organizationCode, "orderQty"))) );

			if( !com.irt.dpr.Country.isFeature(organizationCode, "useSuggestSalesUnitInput") ) {
				inputUOM = ctx.req.getParameter( "inputOrderUOM" );
			}
			availableDate =  com.irt.data.Date.getInstance();
		}

		recordMap.put( "orderKey", orderKey );
		recordMap.put( "partyCode", soldPartyCode );
		recordMap.put( "countryCode", getUserCountryCode(ctx) );
		recordMap.put( "distributionChannelCode", distributionChannelCode );
		recordMap.put( "organizationCode", organizationCode );
		recordMap.put( "divisionCode", getDivisionCode(ctx) );
		recordMap.put( "orderQty", orderQty );
		recordMap.put( "uom", inputUOM );
		recordMap.put( "availableDate", availableDate );
		recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
		recordMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		recordMap.put( "status", OrderDetail.STATUS_NORMAL );

		int lineCount = 0;
		int lineNumber = db.getNextLineNumber( orderKey );
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		String[] itemFieldKeys = { "itemCode", "itemName", "priceCurrency", "price", "existingOrderInd", "itemDisplayInd", "salesStatus", "chainStatus", "dangerousInd"
				, "salesUnit", "isCloseItem", "ordCloseTime" };

		Map<String, Object> itemPrimaryMap = new java.util.HashMap<String, Object>();
		boolean useDangerousItem = Country.isFeature( organizationCode, "useDangerousItem" );
		if( useDangerousItem ) {
			String dangerousPlant = Country.getFeatureValue( organizationCode, "dangerousPlant" );
			if( dangerousPlant != null ) {
				itemPrimaryMap.put( "dangerousPlant", dangerousPlant );
				itemPrimaryMap.put( "dangerousNumber", Item.ITEMTYPE_DANGEROUS );
			} else {
				throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_NEEDED_ENVIRONMENT", "dangerousPlant") );
			}
			itemFieldKeys = com.irt.util.Arrays.append( itemFieldKeys, "dangerousInd" );
		}

		for (Object itemCode : itemCodes) {
			recordMap.put( "lineNumber", String.valueOf(lineNumber + lineCount * 10) );
			recordMap.put( "itemCode", itemCode );
			recordMap.put( "itemCodeConfirmed", itemCode );
			recordMap.put( "itemRefInd", OrderDetail.ITEMREF_NORMAL );
			recordMap.put( "childLineNumber", OrderDetail.CHILD_LINENUMBER_NORMAL );

			Map<String, Object> itemInfoMap = null;
			try {
				itemPrimaryMap.putAll( recordMap );
				itemInfoMap = (new com.irt.dpr.OrderItem(ctx.handler)).getItemInfoMap( itemPrimaryMap, itemFieldKeys );

				if( itemInfoMap == null ) {
					errorList.add( createErrorMap(recordMap.get("itemCode"), ctx.msghandler.getMessage("ERR_ISNOT_SELLINGSKU")) );
					continue;
				} else if( useDangerousItem ) {
					boolean isDangerousItem = "Y".equals( itemInfoMap.get("dangerousInd") );
					if( Order.ORDER_TYPE_DANGEROUS.equals(orderType) ) {
						if( !isDangerousItem ) {
							errorList.add( createErrorMap(recordMap.get("itemCode"), ctx.msghandler.getMessage("ERR_CANNOT_ORDER_NORMAL_ITEM")) );
							continue;
						}
					} else {
						if( isDangerousItem ) {
							errorList.add( createErrorMap(recordMap.get("itemCode"), ctx.msghandler.getMessage("ERR_CANNOT_ORDER_DANGEROUS_ITEM")) );
							continue;
						}
					}
				} else if( "N".equals(itemInfoMap.get("itemDisplayInd")) ) {
					String orderingDisplayErrorMessage = "[ salesStatus: " + (String)itemInfoMap.get("salesStatus") + " chainStatus: " + (String)itemInfoMap.get("chainStatus") + " ]";
					errorList.add( createErrorMap(recordMap.get("itemCode"), ctx.msghandler.getMessage("ERR_ISNOT_SELLINGSKU", orderingDisplayErrorMessage)) );
					continue;
				} else if( "Y".equals(itemInfoMap.get("existingOrderInd")) ) {
					errorList.add( createErrorMap(recordMap.get("itemCode"), ctx.msghandler.getMessage("ERR_ALREADY_REGISTERED_ORDERITEM")) );
					continue;
				} else {
					String salesUnit = (String)itemInfoMap.get("salesUnit");
					if( (salesUnit != null && salesUnit.length() > 0) ) {
						if( REQTYPE_QUICK.equals(rtype) ) {
							if( com.irt.dpr.Country.isFeature(organizationCode, "useStrictSalesUnitInput") && !inputUOM.equals(salesUnit) ) {
								errorList.add( createErrorMap(recordMap.get("itemCode"), ctx.msghandler.getMessage("ERR_UPLOAD_ORDER_INVALID_UOM")) );
								continue;
							} else if( com.irt.dpr.Country.isFeature(organizationCode, "useSuggestSalesUnitInput") ) {
								recordMap.put( "uom", itemInfoMap.get("salesUnit") );
							}
						} else {
							if( com.irt.dpr.Country.isFeature(organizationCode, "useSuggestSalesUnitInput") ) {
								recordMap.put( "uom", itemInfoMap.get("salesUnit") );
							}
						}
					}

					recordMap.put( "priceCurrency", itemInfoMap.get("priceCurrency") );
					recordMap.put( "price", itemInfoMap.get("price") );
					recordMap.put( "packSize"
							,(new com.irt.dpr.ItemUOM(ctx.handler)).getPackSize((String)recordMap.get("itemCode"), (String)recordMap.get("uom")) );
				}

				if( com.irt.dpr.Country.isFeature(organizationCode, "useCloseItem") ) {
					if( "Y".equals(itemInfoMap.get("isCloseItem")) ) {
						errorList.add( createErrorMap(recordMap.get("itemCode")
								, ctx.msghandler.getMessage("ERR_UPLOAD_ORDER_ITEM_IS_CLOSED_2", (String)recordMap.get("itemCode"), (String)itemInfoMap.get("ordCloseTime"))) );
						continue;
					}
				}

				if( db.regist(recordMap) ) {
					lineCount++;
					ctx.handler.commit();
				}
			} catch( DataException dataEx ) {
				ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
				ctx.handler.rollback();
				if( DataException.ERR_UNIQUE_CONSTRAINT.equals(dataEx.getErrorKey()) ) {
					errorList.add( createErrorMap((itemInfoMap == null ? recordMap.get("itemCode") : itemInfoMap.get("itemName"))
							, ctx.msghandler.getMessage("ERR_ALREADY_REGISTERED_ORDERITEM")) );
				} else
					errorList.add( createErrorMap((itemInfoMap == null ? recordMap.get("itemCode") : itemInfoMap.get("itemName")), dataEx) );
			} catch( SQLException sqlEx ) {
				ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
				ctx.handler.rollback();
				errorList.add( createErrorMap((itemInfoMap == null ? recordMap.get("itemCode") : itemInfoMap.get("itemName")), sqlEx) );
			}
		}

		boolean useManualItemPrice =  com.irt.dpr.Country.isFeature(organizationCode, "useManualItemPrice");
		if( noSim && useManualItemPrice ) {
			try {
				db.updateOrderItemPrice( orderKey );
			} catch( DataException dataEx ) {
				ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
				ctx.handler.rollback();
				if( DataException.ERR_UNIQUE_CONSTRAINT.equals(dataEx.getErrorKey()) ) {
					errorList.add( createErrorMap( orderKey, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_ORDERITEM_PRICE")) );
				} else
					errorList.add( createErrorMap(orderKey, dataEx) );
			} catch( SQLException sqlEx ) {
				ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
				ctx.handler.rollback();
				errorList.add( createErrorMap(orderKey, sqlEx) );
			}
		}

		if( errorList != null && errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );

			return forward( ctx, systemConfig.getJspPath() +"/error.jsp" );
		} else {
			if( useManualItemPrice ) {
				com.irt.data.cols.ColumnList columnList;
				String formatType = ctx.req.getParameter( PARAM_FORMATTYPE );
				if( formatType == null || formatType.length() == 0 )
					formatType = FORMATTYPE_PC;

				if( noSim )
					optionKeyList.add("WK");
				else
					optionKeyList.remove("WK");
				if( com.irt.dpr.Country.isFeature(organizationCode, "useReOrder") ) {
					columnList = getColumnList( ctx, "DPROrder.ORDERITEM%LIST.CN."+ formatType.toUpperCase(), optionKeyList.toArray(new String[0]) );
					if( REQTYPE_REGIST.equals(rtype) ) {
						ctx.pageConfig.setProperty( PARAM_REQUESTTYPE, rtype );
					}
				} else {
					columnList = getColumnList( ctx, "DPROrder.ORDERITEM%LIST."+ formatType.toUpperCase(), optionKeyList.toArray(new String[0]) );
				}
				ctx.req.setAttribute( "columnList", columnList );
			}
			//ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_ORDER_DETAIL_SUCCESS", new String[] { String.valueOf(lineCount) }) );

			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	protected List<Map<String, Object>> removeDetail( Context ctx, String[] lineNumbers ) throws ServletModelException, SQLException {
		OrderDetail db = (OrderDetail)ctx.extraObj;
		Map<String, Object> primaryMap = new java.util.HashMap<String, Object>();
		String orderKey = ctx.req.getParameter( "orderKey" );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( lineNumbers == null || lineNumbers.length == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( !((Order)ctx.db).lockOrder(orderKey) )
			throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_RECORD_LOCKED") );

		primaryMap.put( "orderKey", orderKey );

		Object status = ((Order)ctx.db).getFieldValue( Order.createPrimary(orderKey), "status" );
		if( Order.STATUS_SIMULATING.equals(status) || Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
			throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_DELETE_STATUS") );
		else if( Order.STATUS_SIMULATED.equals(status) )
			((Order)ctx.db).updateStatusWithDetail( orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId() );
		else if( Order.STATUS_ERROR.equals(status) )
			((Order)ctx.db).updateStatus( orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId() );

		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

		try {
			if( db.deleteWithLineNoUpdate(orderKey, lineNumbers) <= 0 )
				throw new DataException( DataException.ERR_CANNOT_DELETE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE) );
		} catch( DataException dataEx ) {
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			ctx.handler.rollback();
			// throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage(DataException.ERR_CANNOT_DELETE) );
		} catch( SQLException sqlEx ) {
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
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

	protected boolean removeDetail( Context ctx ) throws IOException, ServletException, SQLException {
		String[] lineNumbers = ctx.req.getParameterValues("lineNumber");
		List<Map<String, Object>> errorList = removeDetail( ctx, lineNumbers );

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );

			return forward( ctx, systemConfig.getJspPath() +"/error.jsp" );
		} else {
			//ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_REMOVE_SUCCESS", new String[] { String.valueOf(lineNumbers.length) }) );

			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	protected void simulationFreegoodslist( Context ctx ) throws IOException, ServletException, SQLException {
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;
		String orderKey = ctx.req.getParameter( "orderKey" );
		if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put( "displayLanguage", getDisplayLanguage(ctx) );
		conditionMap.put( "organizationCode", getSavedOrganizationCode(ctx) );
		conditionMap.put( "orderKey", orderKey );
		conditionMap.put( "freegoodsInd", "Y" );
		conditionMap.put( "simulationTotalQty", null );
		conditionMap.put( "simulationTotalQty_min", 0 );
		conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, "DPROrder.SIM.RESULT.FREGOODS%LIST", "Y" );
		String[] fieldKeys = { "freegoodsInd", "orderQty", "simulationOrderQty", "inputTotalQty", "simulationTotalQty", "freegoodsRatio" };
		ServletUtility.setSort( ctx.req, detailDB, columnList.getSortKeys() );
		List<Map<String, Object>> recordList = detailDB.getRecords( conditionMap, columnList.getFieldKeys(fieldKeys) );
		String hasMost = "N";
		List<Map<String, Object>> freegoodsList = null;
		if( recordList != null ) {
			freegoodsList = new java.util.ArrayList<Map<String, Object>>();
			for( Map<String, Object> record : recordList ) {
				String result = new FreeGoods( ctx.handler ).makeFreegoodsValue( record ) ? "Y" : "N";
				if( "N".equals(hasMost) ) {
					hasMost = result;
				}
				if( "Y".equals(record.get("freegoodsInd")) ) {
					freegoodsList.add( record );
				}
			}
			columnList = getColumnList( ctx, "DPROrder.SIM.RESULT.FREGOODS%LIST", hasMost );
		}

		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, freegoodsList );
		if( conditionKey != null )  ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_FREEGOODS_SIM_LIST") );
		ctx.req.setAttribute( "freegoodsList", freegoodsList );
		ctx.req.setAttribute( "freegoodsColumnList", columnList );
		//ctx.req.setAttribute( "condition", conditionMap );
		//ctx.pageConfig.setManageAuth( false );

		//return forward( ctx, systemConfig.getJspPath() + "/dpr_order_freegoods_list.jsp" );
	}

	protected boolean simulationOrder( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;

		String backURL = new StringBuffer( ((HtmlPage)ctx.pageConfig).getRequestURL() ).append( "?" ).append( ctx.req.getQueryString() ).toString();
		backURL = HtmlUtility.replaceURLQuery( backURL, PARAM_MODE, MODE_FRAME );

		String orderKey = ctx.req.getParameter( "orderKey" );
		if( orderKey == null || orderKey.length() == 0 ) {
			ctx.pageConfig.setBackURL( backURL );

			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		} else {
			backURL = HtmlUtility.replaceURLQuery( backURL, "orderKey", orderKey );
			ctx.pageConfig.setBackURL( backURL );
		}

		checkMaxOrderQtyRegular(ctx);

		if( Order.STATUS_CREATING.equals(db.checkCurrentStatus(orderKey)) ) {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_WAITING_CREATION_AGAIN") );

			String url = ctx.pageConfig.getBackURL();
			url = HtmlUtility.replaceURLQuery( url, PARAM_MODE, MODE_SIMULATION_RESULT );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}

		try {
			if( !db.checkProceedStatus(orderKey, Order.STATUS_SIMULATED) ) {
				throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_ORD_CANNOT_PROCEED_SIMULATION") );
			} else {
				if( !executePlaceOrderProcess(ctx, orderKey, com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_SIMULATION) )
					throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("MSG_SIMULATION_FAILED") );
			}
		} catch( DataException dataEx ) {
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setProperty( "hasError", "Y" );
		}

		return simulationResult( ctx, orderKey );
	}

	protected boolean simulationResult( Context ctx ) throws IOException, ServletException, SQLException {
		return simulationResult( ctx, null );
	}

	protected boolean simulationResult( Context ctx, String orderKey ) throws IOException, ServletException, SQLException {
		Order headerDB = (Order)ctx.db;
		OrderDetail detailDB = (OrderDetail)ctx.extraObj;

		if( orderKey == null || orderKey.length() == 0 ) {
			orderKey = ctx.req.getParameter( "orderKey" );

			if( orderKey == null || orderKey.length() == 0 )
				throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		}

		List<String> optionKeyList = new ArrayList<String>();

		Map<String, Object> recordMap = headerDB.getRecord(createConditionMap(ctx));

		String formatType = ctx.req.getParameter( PARAM_FORMATTYPE );
		if( formatType == null || formatType.length() == 0 )
			formatType = FORMATTYPE_PC;

		Map<String, Object> conditionMap = Order.createPrimary(orderKey);
		conditionMap.putAll( createHierarcyConditionMap(ctx) );
		conditionMap.put( "formatUOM", formatType.toUpperCase() );
		conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );
		conditionMap.put( "shipPartyCode", recordMap.get( "shipPartyCode" ) );

		Map<String, Object> headerMap = headerDB.getRecord( conditionMap );
		if( com.irt.dpr.Country.isFeature((String)recordMap.get("organizationCode"), "usePlantRcv") )
			optionKeyList.add("usePlantRcv");
		if( com.irt.dpr.Country.isFeature((String)recordMap.get("organizationCode"), "useCnf") )
			optionKeyList.add("useCnf");
		if( com.irt.dpr.Country.isFeature((String)recordMap.get("organizationCode"), "useManualItemPrice")
			&& com.irt.dpr.Country.isFeature((String)recordMap.get("organizationCode"), "useSinglePrice") )
			optionKeyList.add("useSinglePrice");

		conditionMap.put( "organizationCode", headerMap.get("organizationCode") );
		conditionMap.put( "distributionChannelCode", headerMap.get("distributionChannelCode") );
		conditionMap.put( "partyCode", headerMap.get("partyCode") );

		// Detail
		String organizationCode = (String)headerMap.get("organizationCode" );
		boolean useShortage = com.irt.dpr.Country.isFeature( organizationCode, "useShortage" );

		String rtype = ctx.req.getParameter( PARAM_REQUESTTYPE );
		if( REQTYPE_REGIST.equals(rtype) ) {
			ctx.pageConfig.setProperty( PARAM_REQUESTTYPE, rtype );
		}

		String listType = ctx.req.getParameter( PARAM_LISTTYPE );
		String columnListName = "DPROrder.SIM.RESULT%LIST.";

		if( listType != null && listType.length() > 0 && LISTTYPE_INPUT.equals(listType) ) {
			columnListName += listType.toUpperCase() + ".";
			ctx.pageConfig.setProperty( "listType", "input" );
		}

		if( useShortage ) {
			columnListName += "CN.";
		} else {
			if(organizationCode.equals(com.irt.dpr.Country.KOREA_ORGANIZATION))
			columnListName += "KR.";
		}

		columnListName += formatType.toUpperCase();

		com.irt.data.cols.ColumnList columnList = getColumnList( ctx, columnListName, optionKeyList.toArray(new String[0]) );
		//int idxVars[] = ctx.pageConfig.getListIndexVariables();
		ServletUtility.setSort( ctx.req, detailDB, columnList.getSortKeys() );
		String[] fieldKeys;
		if( com.irt.dpr.Country.isFeature(organizationCode, "useFreegoods") ) {
			fieldKeys = columnList.getFieldKeys( new String[] { "organizationCode", "freegoodsInd", "freegoodsRatio", "inputTotalQty", "simulationTotalQty" } );
		} else {
			fieldKeys = columnList.getFieldKeys( "organizationCode" );
		}

		boolean useDangerousItem = Country.isFeature( organizationCode, "useDangerousItem" );
		if( useDangerousItem ) {
			String dangerousPlant = Country.getFeatureValue( organizationCode, "dangerousPlant" );
			if( dangerousPlant != null ) {
				conditionMap.put( "dangerousPlant", dangerousPlant );
				conditionMap.put( "dangerousNumber", Item.ITEMTYPE_DANGEROUS );
			} else {
				throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_NEEDED_ENVIRONMENT", "dangerousPlant") );
			}
			fieldKeys = com.irt.util.Arrays.append( fieldKeys, "dangerousInd" );
		}
		List<Map<String, Object>> detailList = detailDB.getRecords( conditionMap, fieldKeys );
		int detailCount = detailDB.getDetailCount( orderKey );
		boolean invalidSellingSKU = false;
		invalidSellingSKU = detailCount > ( detailList != null ? detailList.size() : 0 );

		if( invalidSellingSKU ) {
			throw new ServletModelException( ServletModelException.ERROR, ctx.handler.getMessageHandler().getMessage("ERR_INVALID_SELLINGSKU_ORDER_ITEM") );
		}

		if( "Y".equals(ctx.pageConfig.getProperty("isRevOrd")) ) {
			conditionMap.put("isRevOrd", "Y");
		}
		String orderType = (String)headerMap.get( "orderType" );
		conditionMap.put( "orderType", orderType );
		conditionMap.put( "officeCode", headerMap.get("officeCode") );
		conditionMap.put( "divisionCode", headerMap.get("divisionCode") );
		Map<String, Object> sumMap = headerDB.getSimulationSummary( conditionMap );

		boolean hasSimulationError = "Y".equals(ctx.pageConfig.getProperty( "hasError" )) ? true : false;
		if( !hasSimulationError ) {
			if( detailList != null ) {
				String invalidItem = null;
				boolean isShortageEliminate = false;
				boolean isShortageReqeust = false;
				int shortageErrorCount = 0;
				boolean isShortage = false;
				for( Map<String, Object> item : detailList ) {
					String orderQtyStr = com.irt.data.Record.extractString( item, "orderQty" );
					String simulationOrderQtyStr = com.irt.data.Record.extractString( item, "simulationOrderQty" );
					if( !isShortage && orderQtyStr != null && !orderQtyStr.equals(simulationOrderQtyStr) ) {
						isShortage = true;
					}
					String plantInd = (String) item.get("plantInd");

					if( "Y".equals(plantInd) ) {
						if( invalidItem == null ) {
							invalidItem = "Y";
							ctx.pageConfig.setProperty( "invalidItem", invalidItem );
						}
						item.put( "invalidItem", "Y" );
					} else if( useDangerousItem ) {
						boolean isDangerousItem = "Y".equals( item.get("dangerousInd") );
						if( Order.ORDER_TYPE_DANGEROUS.equals(orderType) ) {
							if( !isDangerousItem ) {
								if( invalidItem == null ) {
									invalidItem = "Y";
									ctx.pageConfig.setProperty( "invalidItem", invalidItem );
								}
								item.put( "invalidItem", "Y" );
							}
						} else {
							if( isDangerousItem ) {
								if( invalidItem == null ) {
									invalidItem = "Y";
									ctx.pageConfig.setProperty( "invalidItem", invalidItem );
								}
								item.put( "invalidItem", "Y" );
							}
						}
					}

					if( !"Y".equals(item.get("invalidItem")) ) {
						item.put( "invalidItem", "N" );
					}

					if( com.irt.dpr.Country.isFeature(organizationCode, "useFreegoods") ) {
						String result = new FreeGoods( ctx.handler ).makeFreegoodsValue( item ) ? "Y" : "N";
						if( "Y".equals(result) ) {
							ctx.pageConfig.setProperty( "hasMostWay", result );
						}

						String freegoodsInd = (String) item.get( "freegoodsInd" );
						if( freegoodsInd != null ) {
							ctx.pageConfig.setProperty( "hasFreegoods", freegoodsInd );
							simulationFreegoodslist( ctx );
						}

						if( !"Y".equals(ctx.pageConfig.getProperty("shortageFreegoods")) ) {
							BigDecimal bdInputTotalQty = (BigDecimal) item.get("inputTotalQty");
							BigDecimal bdSimulationTotalQty = (BigDecimal) item.get("simulationTotalQty");
							if( bdInputTotalQty != null && bdSimulationTotalQty != null ) {
								int inputTotalQty = bdInputTotalQty.intValue();
								int simulationTotalQty = bdSimulationTotalQty.intValue();

								if( simulationTotalQty > 0 ) {
									if( inputTotalQty > simulationTotalQty ) {
										ctx.pageConfig.setProperty( "shortageFreegoods", "Y" );
									}
								}
							}
						}
					}
					if( useShortage ) {
						java.math.BigDecimal orderQty = (java.math.BigDecimal) item.get("orderQty");
						java.math.BigDecimal simulationOrderQty = (java.math.BigDecimal) item.get("simulationOrderQty");
						int intOrderQty =  orderQty != null ? orderQty.intValue() : 0;
						int intSimulationOrderQty =  simulationOrderQty != null ? simulationOrderQty.intValue() : 0;

						if( intOrderQty > 0 ) {
							if( intOrderQty - intSimulationOrderQty > 0 || intSimulationOrderQty == 0 ) {
								ShortageEliminate shortageDB = new ShortageEliminate( ctx.handler );
								Map<String, Object> shortageRecordMap = new HashMap<String, Object>();
								shortageRecordMap.put( "orderKey", orderKey );
								shortageRecordMap.put( "itemCode", item.get("itemCode") );
								shortageRecordMap.put( "itemCodeConfirmed", item.get("itemCodeConfirmed") );
								shortageRecordMap.put( "itemConsumerEANCodeCNF", item.get("itemConsumerEANCodeCNF") );
								shortageRecordMap.put( "qty", intOrderQty - intSimulationOrderQty );
								shortageRecordMap.put( "uom", item.get("uom") );
								shortageRecordMap.put( "status", "00" );

								try {
									shortageDB.regist( shortageRecordMap );
								} catch( DataException dataEx ) {
									ctx.handler.rollback();
									if( DataException.ERR_UNIQUE_CONSTRAINT.equals(dataEx.getErrorKey()) ) {
										try {
											shortageDB.modify( shortageRecordMap );
										} catch( DataException ex ) {
											ctx.handler.rollback();
											shortageErrorCount++;
										}
									} else
										shortageErrorCount++;
								} catch( SQLException sqlEx ) {
									ctx.handler.rollback();
									shortageErrorCount++;
								}
								isShortageEliminate = true;
							}
						}
					}
					if(organizationCode.equals(com.irt.dpr.Country.KOREA_ORGANIZATION)) {
						double itemPrice;
						double simulationPackSize;
						double price;
						double gapPrice;
						if( item.get("itemPrice") != null && !"".equals(item.get("itemPrice")) )  {
							itemPrice = ((Number)item.get("itemPrice")).doubleValue();
						} else {
							itemPrice = 0.0;
						}
						if( item.get("simulationPackSize") != null && !"".equals(item.get("simulationPackSize")) ){
							simulationPackSize = Double.parseDouble(String.valueOf(item.get("simulationPackSize")));

						} else {
							simulationPackSize = 0.0;
						}
						if( item.get("price") != null && !"".equals(item.get("price")) ) {
							price = Double.parseDouble(String.valueOf(item.get("price")));
						} else {
							price = 0.0;
						}
						if( itemPrice > 0 ) {
							gapPrice = Math.floor( ((((price / simulationPackSize) / itemPrice) - 1) * 100) * 1000 ) / 1000.0;
							item.put( "gapPrice", gapPrice );
						}
					}
				}
				if( useShortage ) {
					if( isShortageEliminate )
						ctx.pageConfig.setProperty( "shortageType", Order.SIMULATION_SHORTAGE_ELIMINATE );
					else if( isShortageReqeust )
						ctx.pageConfig.setProperty( "shortageType", Order.SIMULATION_SHORTAGE_REQUEST );
					else {
						ShortageEliminate shortageDB = new ShortageEliminate( ctx.handler );
						Map<String, Object> shortageConditionMap = new HashMap<String, Object>();
						shortageConditionMap.put( "orderKey", orderKey );
						shortageConditionMap.put( "status", "00" );
						if( shortageDB.getRecordCount( shortageConditionMap ) > 0 ) {
							ctx.pageConfig.setProperty( "shortageType", Order.SIMULATION_SHORTAGE_REQUEST );
						}
					}
					shortageList( ctx );
				}

				if( (ctx.pageConfig.getMessage() == null || ctx.pageConfig.getMessage() == "") && "Y".equals(invalidItem) )
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_SIMULATIONORDER_ITEM_CLOSED") );
				// 다른 메세지가 없다면 order total setting 표시
				if( (ctx.pageConfig.getMessage() == null || ctx.pageConfig.getMessage() == "")
					&& "N".equals(sumMap.get("isPlaceOrderable")) ) {
					ctx.pageConfig.setMessage(
						ctx.msghandler.getMessage("MSG_SIMULATIONORDER_TOTAL_TOO_LOW", String.valueOf(sumMap.get("minimumValue"))) );

					ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR);
				} else if( shortageErrorCount > 0 ) {
					ctx.pageConfig.setMessage( ctx.pageConfig.getMessage() + " " + ctx.msghandler.getMessage("MSG_DPR_ORDER_SHORTAGE_REGIST_FAILED", String.valueOf(shortageErrorCount)) );
				} else if( isShortage ) {
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_ORDER_RESULT_SHORTAGEQTY") );
					ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_WARNING );
				}
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

		String conditionKey = pushConditionMapAndSetListIndexVariables( ctx, conditionMap, detailList );
		if( conditionKey != null ) ctx.pageConfig.setProperty( "conditionKey", conditionKey );

		ctx.req.setAttribute( "header", headerMap );
		ctx.req.setAttribute( "headerFieldSet", headerDB.getFieldSet(false) );
		ctx.req.setAttribute( "records", detailList );
		ctx.req.setAttribute( "summary", sumMap);
		ctx.req.setAttribute( "columnList", columnList );

		ctx.pageConfig.setTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_"+ MODE_SIMULATION_RESULT.toUpperCase()) );
		ctx.pageConfig.setSubTitle( ctx.msghandler.getMessage("jsp.dpr_order_result.SUBTITLE_SIMULATION") );
		setPath( ctx, "TITLE_DPR_ORDER_FRM", "TITLE_DPR_ORDER_SIMR" );

		if( com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()).equals(headerMap.get("orderDate")) )
			ctx.pageConfig.setManageAuth( ctx.sessionMng.isAuthorized("DPR", "DPROrder.MNG") );
		else
			ctx.pageConfig.setManageAuth( false );

		if( com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") ) {
			boolean useSelectRdd = false;
			useSelectRdd = new RddTrigger( ctx.handler )
					.isPredefiendRdd( (String)recordMap.get("organizationCode"), (String)recordMap.get("distributionChannelCode")
							, (String)recordMap.get("divisionCode"), (String)recordMap.get("soldPartyCode"), (String)recordMap.get("shipPartyCode") );

			if( useSelectRdd ) {
				ctx.pageConfig.setProperty( "useSelectRdd", "Y" );
			}
		}

		if( headerDB.checkCreatingStatus((String) headerMap.get("soldPartyCode"), (String) headerMap.get("shipPartyCode")) )
			ctx.pageConfig.setProperty( "hasCreatingOrder", "Y" );

		String rmsg = ctx.req.getParameter( "rmsg" );
		if( rmsg != null && rmsg.length() > 0 ) {
			String rlevel = ctx.req.getParameter( "rlevel" );
			if( rlevel != null && rlevel.length() > 0  )
				ctx.pageConfig.setResultLevel( rlevel.charAt(0) );
			else
				ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_NULL );
			ctx.pageConfig.setMessage( URLDecoder.decode(rmsg, StandardCharsets.UTF_8.name()) );
		}

		if( "Y".equals(ctx.pageConfig.getProperty("isRevOrd")) ) {
			String redirectURL = ctx.pageConfig.getProperty("revRedirURL");
			if( redirectURL == null ) {
				throw new ServletModelException(ServletModelException.INTERNAL_ERROR);
			}
			if( ctx.pageConfig.getProperty("shortageType") != null ) {
				redirectURL = HtmlUtility.replaceURLQuery(redirectURL, "shortageType", ctx.pageConfig.getProperty("shortageType"));
			}
			redirectURL = HtmlUtility.replaceURLQuery(redirectURL, "isPlaceRevisible", (String)sumMap.get("isPlaceRevisible"));
			if( "N".equals(sumMap.get("isPlaceRevisible")) ) {
				ctx.pageConfig.setBackURL(redirectURL);
				throw new ServletModelException("ERR_MSG_ORDREV_TOTAL_TOO_LOW"
						, ctx.msghandler.getMessage("ERR_MSG_ORDREV_TOTAL_TOO_LOW"
//								, String.valueOf(sumMap.get("revfinOrderTotal"))
//								, String.valueOf(sumMap.get("minimumValue"))
								));
			}
			if( "N".equals(sumMap.get("isPlaceOrderable")) )
				ctx.pageConfig.setMessage( null );
			return sendRedirect(ctx, com.irt.html.HtmlUtility.replaceURLQuery(redirectURL, PARAM_MESSAGE_KEY, saveMessage(ctx)));
		} else {
			return forward( ctx, systemConfig.getJspPath() + "/dpr_order_result.jsp" );
		}
	}

	/**
	**	DPR_ORDER_DTL: UPDATE
	**/
	protected boolean updateDetail( Context ctx ) throws IOException, ServletException, SQLException {
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
		int count = updateDetailContent( ctx, errorList );

		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		if( errorList.size() > 0 ) {
			ctx.req.setAttribute( "errors", errorList );

			return forward( ctx, systemConfig.getJspPath() +"/error.jsp" );
		} else {
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS", new String[] { String.valueOf(count) }) );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(ctx.pageConfig.getBackURL(), PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}
	}

	protected int updateDetailContent( Context ctx, List<Map<String, Object>> errorList ) throws IOException, ServletException, SQLException {
		OrderDetail db = (OrderDetail)ctx.extraObj;
		Collection<Map<String, Object>> records = (new ParameterMap(ctx.req)).extractGroupList( "value" );
		String orderKey = ctx.req.getParameter( "orderKey" );
		String organizationCode = ctx.req.getParameter( "organizationCode" );

		if( records == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		else if( orderKey == null || orderKey.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( !((Order)ctx.db).lockOrder(orderKey) )
			throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_RECORD_LOCKED") );

		Map<String, Object> headerMap = ((Order)ctx.db).getRecord( Order.createPrimary(orderKey), new String[] { "distributionChannelCode", "divisionCode", "partyCode", "status" } );
		headerMap.put( "organizationCode", organizationCode );
		Object status = headerMap.remove( "status" );
		boolean noSim = !Order.STATUS_SIMULATED.equals(status);
		if( Order.STATUS_SIMULATING.equals(status) || Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
			throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_STATUS") );
		else if( Order.STATUS_SIMULATED.equals(status) )
			((Order)ctx.db).updateStatusWithDetail( orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId() );
		else if( Order.STATUS_ERROR.equals(status) )
			((Order)ctx.db).updateStatus( orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId() );

		List<String> updatableKeys = new ArrayList<String>(java.util.Arrays.asList(db.getFieldSet(false).getFieldKeyArray()));
		String[] updateNotKeys = new String[]{ "orderKey", "lineNumber" };
		updatableKeys = Utility2.listSubtract(updatableKeys, java.util.Arrays.asList(updateNotKeys));

		int count = 0;
		for( Map<String, Object> obj : records ) {
			Map<String, Object> recordMap = obj;
			try {
				String childLineNumber = (String)recordMap.get("childLineNumber");
				if( childLineNumber != null ) {
					if( Integer.parseInt( (String)recordMap.get("childLineNumber")) != 0 ) continue;
				}
			} catch( NumberFormatException ignoreEx ) {
				logger.error("debugInfo map: "+ recordMap, ignoreEx);
			}

			com.irt.dpr.Item itemDB = new com.irt.dpr.Item( ctx.handler );
			try {
				recordMap.put( "orderKey", orderKey );
				recordMap.put( "packSize"
					,(new com.irt.dpr.ItemUOM(ctx.handler)).getPackSize((String)recordMap.get("itemCode"), (String)recordMap.get("uom")) );
				if( recordMap.get("status") == null )
					recordMap.put( "status", OrderDetail.STATUS_NORMAL );
				if( recordMap.get("itemRefInd") == null )
					recordMap.put( "itemRefInd", OrderDetail.ITEMREF_NORMAL);
				if( recordMap.get("childLineNumber") == null )
					recordMap.put( "childLineNumber", OrderDetail.CHILD_LINENUMBER_NORMAL);

				// Orde Qty 제한
				int orderQty = -1;
				int registeredQty = 0;
				try {
					orderQty = Integer.parseInt( (String)recordMap.get("orderQty") );
					if( recordMap.containsKey("tmp_orderQty") ) {
						registeredQty = Integer.parseInt( (String)recordMap.get("tmp_orderQty") );
					} else {
						registeredQty = orderQty;
					}
				} catch( NumberFormatException numberEx ) {
					errorList.add( createErrorMap("["+recordMap.get("lineNumber")+"] " + itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx))
						, ctx.msghandler.getMessage("ERR_INVALID_NUMBER")) );
				}

				int simulationOrderQty = orderQty;
				if( recordMap.get("simulationOrderQty") != null ) {
					simulationOrderQty = Integer.parseInt( (String)recordMap.get("simulationOrderQty") );
				}

				boolean isOrderQtyZeroDefault = "0".equals(Country.getDefault(organizationCode, "orderQty"));
				boolean isOrdRev = (recordMap.get("reviseChangeIndex")!=null);
				String uom = (String)recordMap.get( "uom" );
				if( ( ( !isOrdRev && orderQty < com.irt.dpr.Order.ORDER_LOW_LIMIT)
						|| (registeredQty != simulationOrderQty && orderQty > simulationOrderQty) ) ) {
					if( !isOrderQtyZeroDefault ) {
						if( orderQty < com.irt.dpr.Order.ORDER_LOW_LIMIT )
							errorList.add( createErrorMap("["+recordMap.get("lineNumber")+"] "+ itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx))
								, ctx.msghandler.getMessage("ERR_ORD_ORDER_LOW_LIMIT")) );
						else if( registeredQty != simulationOrderQty && orderQty > simulationOrderQty )
							errorList.add( createErrorMap("["+recordMap.get("lineNumber")+"] "+ itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx))
								, ctx.msghandler.getMessage("ERR_ORD_ORDER_HIGH_LIMIT_THAN_SIMUALTION_QTY", String.valueOf(simulationOrderQty))) );
					}
				} else if( !FORMATTYPE_PC.equals(uom) && orderQty > com.irt.dpr.Order.ORDER_HIGH_LIMIT ) {
					if( orderQty > com.irt.dpr.Order.ORDER_HIGH_LIMIT )
						errorList.add( createErrorMap("["+recordMap.get("lineNumber")+"] "+ itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx))
							, ctx.msghandler.getMessage("ERR_ORD_ORDER_HIGH_LIMIT")) );
				} else {
					String itemCodeNew = (String)recordMap.get("itemCodeNew");
					String itemCode = (String)recordMap.get("itemCode");
					boolean isNewItemCode = itemCodeNew != null && !itemCodeNew.equals( itemCode );
					if( com.irt.dpr.Country.isFeature(organizationCode, "useFreegoods") ) {
						String officeCode = (String) new Party( ctx.handler).getFieldValue( headerMap, "officeCode" );
						com.irt.dpr.FreeGoods fgDB = new com.irt.dpr.FreeGoods( ctx.handler );
						itemCode = isNewItemCode ? itemCodeNew : itemCode;
						Map<String, Object> conditionMap = new java.util.HashMap<String, Object>( headerMap );
						conditionMap.put( "officeCode", officeCode );
						conditionMap.put( "itemCode", itemCode );
						int freegoodsQty = fgDB.getFreeGoodsQty( conditionMap, orderQty );
						recordMap.put( "freegoodsQty", freegoodsQty );
						recordMap.put( "inputTotalQty", orderQty + freegoodsQty );

						Map<String, Object> freeGoodsMap =  fgDB.getFreeGoods( conditionMap, new String[] { "orderableRatio" } );
						if( freeGoodsMap != null ) {
							recordMap.put( "freegoodsRatio", ((BigDecimal)freeGoodsMap.get("orderableRatio")).doubleValue() );
						}
						if( freegoodsQty > 0 ) {
							recordMap.put( "freegoodsInd", "Y" );
						} else {
							recordMap.put( "freegoodsInd", "" );
						}
					}

					if( isNewItemCode ) {
						Map<String, Object> recordMapNew = new java.util.HashMap<String, Object>(recordMap);
						recordMapNew.remove("itemCode");
						recordMapNew.remove("itemCodeConfirmed");
						recordMapNew.put("itemCode", itemCodeNew);
						recordMapNew.put("itemCodeConfirmed", itemCodeNew);

						if( db.modifyWithValidate(recordMapNew, Utility2.listIntersect(updatableKeys, recordMapNew.keySet()).toArray(new String[0])) )
							count++;
						else
							errorList.add( createErrorMap("["+recordMap.get("lineNumber")+"] "+ itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx))
								, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE")) );
					} else {
						if( db.modify(recordMap, Utility2.listIntersect(updatableKeys, recordMap.keySet()).toArray(new String[0])) )
							count++;
						else
							errorList.add( createErrorMap("["+recordMap.get("lineNumber")+"] "+ itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx))
								, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE")) );
					}
				}
				boolean useManualItemPrice =  com.irt.dpr.Country.isFeature(organizationCode, "useManualItemPrice");
				if( noSim && useManualItemPrice ) {
					try {
						db.updateOrderItemPrice( orderKey );
					} catch( DataException dataEx ) {
						ctx.handler.rollback();
						if( DataException.ERR_UNIQUE_CONSTRAINT.equals(dataEx.getErrorKey()) ) {
							errorList.add( createErrorMap( orderKey, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_ORDERITEM_PRICE")) );
						} else
							errorList.add( createErrorMap(orderKey, dataEx) );
					} catch( SQLException sqlEx ) {
						ctx.handler.rollback();
						errorList.add( createErrorMap(orderKey, sqlEx) );
					}
				}
				if( useManualItemPrice ) {
					com.irt.data.cols.ColumnList columnList;
					String formatType = ctx.req.getParameter( PARAM_FORMATTYPE );
					if( formatType == null || formatType.length() == 0 )
						formatType = FORMATTYPE_PC;

					List<String> optionKeyList = new ArrayList<String>();
					if( noSim )
						optionKeyList.add("WK");
					else
						optionKeyList.remove("WK");

					if( com.irt.dpr.Country.isFeature(organizationCode, "useReOrder") ) {
						columnList = getColumnList( ctx, "DPROrder.ORDERITEM%LIST.CN."+ formatType.toUpperCase(), optionKeyList.toArray(new String[0]) );
					} else {
						columnList = getColumnList( ctx, "DPROrder.ORDERITEM%LIST."+ formatType.toUpperCase(), optionKeyList.toArray(new String[0]) );
					}
					ctx.req.setAttribute( "columnList", columnList );
				}
			} catch( DataException dataEx ) {
					errorList.add( createErrorMap(itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)), dataEx) );
			} catch( SQLException sqlEx ) {
					errorList.add( createErrorMap(itemDB.getName((String)recordMap.get("itemCode"), getDisplayLanguage(ctx)), sqlEx) );
			}
		}

		return count;
	}

	protected boolean updateHeader( Context ctx ) throws IOException, ServletException, SQLException {
		Order db = (Order)ctx.db;
		String url = ctx.pageConfig.getBackURL();
		Map<String, Object> recordMap = new ParameterMap( ctx.req );
		String countryCode = getUserCountryCode( ctx );
		if( ((ParameterMap)recordMap).checkXSS() )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		if( ctx.pageConfig.getBackURL() == null )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );
		ctx.req.setAttribute( "header", recordMap );

		boolean insert = false;

		String orderKey = Record.extractString( recordMap, "orderKey" );
		String distributionChannelCode = (String) recordMap.get("distributionChannelCode");
		if( distributionChannelCode == null || distributionChannelCode.length() == 0 )
			distributionChannelCode = getDistributionChannelCode( ctx );
		if( orderKey == null || orderKey.length() == 0 ) {
			insert = true;
			orderKey = db.getOrderKey();
		} else if( !db.checkProceedStatus(orderKey, Order.STATUS_WORKSHEET) )
			throw new ServletModelException( ServletModelException.INVALID_PARAMETER );

		try {
			com.irt.data.Date orderDate = com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone());
			com.irt.data.Date inDate = null;
			try {
				if( recordMap.get("inDate") != null )
				inDate = com.irt.data.Date.getInstance( (String)recordMap.get("inDate") );
				if( inDate != null && orderDate.after(inDate) )
					throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_INVALID_INDATE_ABOUT_ORDERDATE") );
			} catch( java.text.ParseException parseEx ) {
				throw new DataException( DataException.ERR_INVALID_DATE, ctx.msghandler.getMessage(DataException.ERR_INVALID_DATE) );
			}
			Map<String, Object> headerMap = null;
			String organizationCode = null;
			String divisionCode = null;
			String partyCode = null;
			String shipPartyCode = null;
			if( !insert ) {
				headerMap = db.getRecord( Order.createPrimary(orderKey) );
				organizationCode = (String) headerMap.get( "organizationCode" );
				distributionChannelCode = (String) headerMap.get( "distributionChannelCode" );
				divisionCode = (String) headerMap.get( "divisionCode" );
				partyCode = (String) headerMap.get( "partyCode" );
				shipPartyCode = (String) headerMap.get( "shipPartyCode" );
			} else {
				organizationCode = (String) recordMap.get( "organizationCode" );
				divisionCode = getDivisionCode( ctx );
				partyCode = (String) recordMap.get( "soldPartyCode" );
				shipPartyCode = (String) recordMap.get( "shipPartyCode" );
			}
			com.irt.dpr.tools.OrderCanonicalProcess ocp = new com.irt.dpr.tools.OrderCanonicalProcess( ctx.handler, systemConfig, com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_RDD );
			Map<String, Object> infMap = new java.util.HashMap<String, Object>();
			com.irt.data.Date inDateSystemDefault = getRequestDeliveryDate( ctx, com.irt.data.Date.getInstance(ctx.sessionMng.getTimeZone()).getDate(SYSTEMRDD_PLUS_DAYS) );
			boolean useInDateAsRDDFrSAP;
			boolean useSelectRdd = false;
			if( com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") ) {
				if( inDate == null ) {
					throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_INVALID_INDATE_ABOUT_MANDATORY") );
				}
				RddTrigger rtDB = new RddTrigger( ctx.handler );
				useSelectRdd = rtDB
						.isPredefiendRdd( organizationCode, distributionChannelCode, divisionCode, partyCode, shipPartyCode );

				if( useSelectRdd ) {
					RddIndicator indDB = new RddIndicator( ctx.handler );
					boolean isPredefined = indDB.isPredefined( organizationCode, distributionChannelCode, divisionCode, shipPartyCode );
					boolean existPredefineRdd = false;
					Map<String, Object> ptyConditionMap = Party.createPrimary( partyCode, organizationCode, distributionChannelCode, divisionCode );
					Map<String, Object> partyMap = new Party( ctx.handler ).getRecord( ptyConditionMap, new String[] { "officeCode", "groupCode" } );
					List<Map<String, Object>> rddList = rtDB.getRddValues( isPredefined, (String)recordMap.get("organizationCode"), distributionChannelCode, (String)partyMap.get("officeCode"), (String)partyMap.get("groupCode") );

					for( Map<String, Object> rddMap : rddList ) {
						com.irt.data.Date rdd;
						try {
							rdd = com.irt.data.Date.getInstance( (String)rddMap.get("dateValue") );
						} catch( java.text.ParseException parseEx ) {
							throw new DataException( DataException.ERR_INVALID_DATE, ctx.msghandler.getMessage("ERR_INVALID_INDATE") );
						}

						if( inDate.compareTo(rdd) == 0 ) {
							existPredefineRdd = true;
							break;
						}
					}
					if( !existPredefineRdd ) {
						throw new DataException( DataException.ERR_INVALID_DATE, ctx.msghandler.getMessage("ERR_INVALID_INDATE") );
					}
				}
			}

			if( !useSelectRdd && !Country.isFeature(getSavedOrganizationCode(ctx), "useInputRDD") )
				useInDateAsRDDFrSAP = true;// inDate = inDateDefault Value
			else
				useInDateAsRDDFrSAP = false;// inDate = userInputRDD Value

			if( insert ) {
				infMap.put( "orderKey", orderKey );
				infMap.put( "countryCode", getUserCountryCode(ctx) );
				infMap.put( "organizationCode", organizationCode );
				infMap.put( "distributionChannelCode", distributionChannelCode );
				infMap.put( "divisionCode", divisionCode );
				infMap.put( "shipPartyCode", shipPartyCode );
				infMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
				ocp.setParameter( infMap );

				String message = null;
				com.irt.data.Date inDateDefault = null;
				try {
					String inDateDefaultStr = ocp.execute();

					if( inDateDefaultStr == null || inDateDefaultStr.length() == 0 ) {
						if( useSelectRdd ) {
							inDateDefault = com.irt.data.Date.getInstance( orderDate.getDate(SAPRDDNULL_PLUS_DAYS) );;
							logger.debug( "updateHeader-RDD-insert: " + " inDateDefaultStr=null " + "useSelectRdd=true" + " useInDateAsRDDFrSAP=false " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
						} else if( useInDateAsRDDFrSAP ) {
							inDate = inDateDefault = com.irt.data.Date.getInstance(orderDate.getDate(SAPRDDNULL_PLUS_DAYS));
							logger.debug( "updateHeader-RDD-insert: " + " inDateDefaultStr=null " + "useSelectRdd=false" + " useInDateAsRDDFrSAP=true " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
						} else {
							inDate = inDateDefault = inDateSystemDefault;
							logger.debug( "updateHeader-RDD-insert: " + " inDateDefaultStr=null " + "useSelectRdd=false" + " useInDateAsRDDFrSAP=false " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
						}
					} else {
						inDateDefault = getRequestDeliveryDate( ctx, com.irt.data.Date.getInstance(inDateDefaultStr) );
						if( !useSelectRdd || useInDateAsRDDFrSAP ) {
							inDate = inDateDefault;
						}

						logger.debug( "updateHeader-RDD-insert: " + " inDateDefaultStr=not null " + " inDate=" + inDate );
					}
				} catch( com.irt.dpr.OrderProcessException opEx ) {
					message = ctx.msghandler.getMessage( opEx.getErrorKey() );
				} catch( java.text.ParseException parseEx ) {
					message = parseEx.getMessage();
				} finally {
					if( inDateDefault == null ) {
						if( useSelectRdd ) {
							inDateDefault = com.irt.data.Date.getInstance( orderDate.getDate(SAPRDDNULL_PLUS_DAYS) );
							logger.debug( "updateHeader-RDD-insert: " + " inDateDefault=null " + " useInDateAsRDDFrSAP=true " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
						} else if( useInDateAsRDDFrSAP ) {
							inDate = inDateDefault = com.irt.data.Date.getInstance( orderDate.getDate(SAPRDDNULL_PLUS_DAYS) );
							logger.debug( "updateHeader-RDD-insert: " + " inDateDefault=null " + " useInDateAsRDDFrSAP=false " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
						} else {
							inDateDefault = inDateSystemDefault;
							logger.debug( "updateHeader-RDD-insert: " + " inDateDefault=null " + " useInDateAsRDDFrSAP=false " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
						}
					}
				}

				recordMap.put( "orderKey", orderKey );
				recordMap.put( "partyCode", partyCode );
				recordMap.put( "countryCode", countryCode );
				recordMap.put( "orderDate", orderDate );
				recordMap.put( "inDate", inDate );
				recordMap.put( "inDateDefault", inDateDefault );
				recordMap.put( "distributionChannelCode", distributionChannelCode );
				recordMap.put( "divisionCode", divisionCode );

				if( !com.irt.dpr.Country.isFeature((String) recordMap.get("organizationCode"), "useDangerousItem") ) {
					recordMap.put( "orderType", Order.ORDER_TYPE_NORMAL );
				}
				recordMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
				if( message == null || message.length() == 0 )
					recordMap.put( "status", Order.STATUS_WORKSHEET );
				else {
					recordMap.put( "message", message );
					recordMap.put( "status", Order.STATUS_ERROR );
				}

				if( !db.regist(recordMap) )
					throw new DataException( DataException.ERR_CANNOT_INSERT, ctx.msghandler.getMessage(DataException.ERR_CANNOT_INSERT) );

				if( message != null && message.length() > 0 )
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_ERROR_RDD_INTERFACE", new String(message)) );
			} else {
				useSelectRdd = new RddTrigger( ctx.handler )
						.isPredefiendRdd( organizationCode, distributionChannelCode, divisionCode, partyCode, shipPartyCode );

				String message = null;
				com.irt.data.Date inDateDefault = (com.irt.data.Date)headerMap.get( "inDateDefault" );
				String status = Record.extractString( headerMap, "status" );
				if( status == null || status.length() == 0
						|| Order.STATUS_SIMULATING.equals(status)
						|| Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
					throw new DataException( DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_STATUS") );
				else if( inDateDefault == null || Order.STATUS_SIMULATED.equals(status) || Order.STATUS_ERROR.equals(status) ) {
					infMap.put( "orderKey", orderKey );
					infMap.put( "countryCode", getUserCountryCode(ctx) );
					infMap.put( "organizationCode", organizationCode );
					infMap.put( "distributionChannelCode", distributionChannelCode );
					infMap.put( "divisionCode", divisionCode );
					infMap.put( "shipPartyCode", shipPartyCode );
					infMap.put( "updateUserId", ctx.sessionMng.getUniqId() );
					ocp.setParameter( infMap );

					try {
						String inDateDefaultStr = ocp.execute();

						if( inDateDefaultStr == null || inDateDefaultStr.length() == 0 ) {
							if( useSelectRdd ) {
								inDateDefault = com.irt.data.Date.getInstance( orderDate.getDate(SAPRDDNULL_PLUS_DAYS) );;
								logger.debug( "updateHeader-RDD-insert: " + " inDateDefaultStr=null " + "useSelectRdd=true" + " useInDateAsRDDFrSAP=false " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
							} else if( useInDateAsRDDFrSAP ) {
								inDate = inDateDefault = com.irt.data.Date.getInstance(orderDate.getDate(SAPRDDNULL_PLUS_DAYS));
								logger.debug( "updateHeader-RDD-update: " + " inDateDefaultStr=null " + "useSelectRdd=false" + " useInDateAsRDDFrSAP=true " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
							} else {
								inDate = inDateDefault = inDateSystemDefault;
								logger.debug( "updateHeader-RDD-update: " + " inDateDefaultStr=null " + "useSelectRdd=false" + " useInDateAsRDDFrSAP=false " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
							}
						} else {
							inDateDefault = getRequestDeliveryDate( ctx, com.irt.data.Date.getInstance(inDateDefaultStr) );

							if( useInDateAsRDDFrSAP ) // OrderHeader업데이트시 userInputRDD사용하는 경우 inDate 와 inDateDefault 다른값 사용 가능
								inDate = inDateDefault;

							logger.debug( "updateHeader-RDD-update: " + " inDateDefaultStr=not null " + " useInDateAsRDDFrSAP=? " + " inDate=" + inDate + " inDateDefault=" + inDateDefault );
						}
					} catch( com.irt.dpr.OrderProcessException opEx ) {
						message = opEx.getMessage();
					} catch( java.text.ParseException parseEx ) {
						message = parseEx.getMessage();
					} finally {
						if( inDateDefault == null ) {
							if( useSelectRdd ) {
								inDateDefault = com.irt.data.Date.getInstance( orderDate.getDate(SAPRDDNULL_PLUS_DAYS) );
								logger.debug( "updateHeader-RDD-insert: " + " inDateDefault=null " + " useInDateAsRDDFrSAP=true " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
							} else if( useInDateAsRDDFrSAP ) {
								inDate = inDateDefault = com.irt.data.Date.getInstance( orderDate.getDate(SAPRDDNULL_PLUS_DAYS) );
								logger.debug( "updateHeader-RDD-update: " + " inDateDefault=null " + " useInDateAsRDDFrSAP=true " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
							} else {
								inDateDefault = inDateSystemDefault;
								logger.debug( "updateHeader-RDD-update: " + " inDateDefault=null " + " useInDateAsRDDFrSAP=false " + "inDate=" + inDate + " inDateDefault=" + inDateDefault );
							}
						}
					}
				}

				//inDateDefault > inDate
				else if( inDateDefault.after(inDate) )
					throw new DataException( DataException.ERR_ERROR, ctx.msghandler.getMessage("ERR_INVALID_INDATE_ABOUT_DEFAULTINDATE") );

				recordMap.put( "inDate", inDate );
				recordMap.put( "inDateDefault", inDateDefault );
				recordMap.put( "distributionChannelCode", distributionChannelCode );
				recordMap.put( "divisionCode", divisionCode );
				if( message == null || message.length() == 0 )
					recordMap.put( "status", Order.STATUS_WORKSHEET );
				else {
					recordMap.put( "message", message );
					recordMap.put( "status", Order.STATUS_ERROR );
				}

				if( !db.modify(recordMap) )
					throw new DataException( DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE) );

				//ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_MODIFY_SUCCESS") );
			}
			headerMap = db.getRecord( recordMap );

			ctx.req.setAttribute( "header", headerMap );
		} catch( DataException dataEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( dataEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.info( "error.", dataEx );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		} catch( SQLException sqlEx ) {
			ctx.handler.rollback();
			ctx.pageConfig.setMessage( sqlEx.getMessage() );
			ctx.pageConfig.setResultLevel( HtmlPage.PAGE_RESULT_ERROR );
			logger.error( "internal error.", sqlEx );
			return sendRedirect( ctx, HtmlUtility.replaceURLQuery(url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
		}

		url += "&orderKey="+ orderKey;
		return sendRedirect( ctx, HtmlUtility.replaceURLQuery(url, PARAM_MESSAGE_KEY, saveMessage(ctx)) );
	}

	@Override
	protected boolean upload( Context ctx ) throws IOException, ServletException, SQLException {
		Upload db = new Upload( ctx.handler );

		DataReader reader = createDataReader( ctx );

		com.irt.data.cols.ColumnList columnList;

		String organizationCode = ctx.req.getParameter( "organizationCode" );

		List<String> optionKeyList = new ArrayList<String>();
		optionKeyList.add( com.irt.html.ColumnConfigureFile.OPTIONKEY_DELETE_HTML );
		if( Country.isFeature(organizationCode, "usePackDeal") )
			optionKeyList.add("usePackDeal");
		if( Country.isFeature(organizationCode, "useStopItem") )
			optionKeyList.add("useStopItem");
		if( Country.isFeature(organizationCode, "useCloseItem") )
			optionKeyList.add("useCloseItem");
		if( Country.isFeature(organizationCode, "useMoq") )
			optionKeyList.add("useMoq");
		if( Country.isFeature(organizationCode, "useSuggestSalesUnitInput") )
			optionKeyList.add("useSuggestSalesUnitInput");
		if( Country.isFeature(organizationCode, "usePlantRcv") )
			optionKeyList.add("usePlantRcv");

		if( com.irt.dpr.Country.isFeature(organizationCode, "useExtra1") ) {
			String countryKey = Country.getCountryKeyFromPartyId(ctx.sessionMng.getPartyId());
			columnList = getColumnList( ctx, "DPROrderItem.DOWN%DOWN."+ countryKey, optionKeyList.toArray(new String[0]) );
			if( columnList == null )
				columnList = getColumnList( ctx, "DPROrderItem.DOWN%DOWN.CN", optionKeyList.toArray(new String[0]) );
		} else {
			columnList = getColumnList( ctx, "DPROrderItem.DOWN%DOWN", optionKeyList.toArray(new String[0]) );
		}

		String[] fieldKeyArray = columnList.getFieldKeyArray();

		MultipartHttpRequest req = (MultipartHttpRequest)ctx.req;
		Map<String, Object> recordMap = new ParameterMap( ctx.req );

		String uploadCode = db.getUploadCode();
		String uploadType = (String)recordMap.get( "uploadType" );
		String includingHeaderInd = ctx.req.getParameter( "headerInd" );

		if( uploadCode == null || uploadCode.length() == 0 )
			throw new ServletModelException( ServletModelException.INTERNAL_ERROR );
		else if( uploadType == null || uploadType.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String orderKey = (String)recordMap.get( "orderKey" );
		Object status = ((Order)ctx.db).getFieldValue( Order.createPrimary(orderKey), "status" );
		if( Order.STATUS_SIMULATING.equals(status) || Order.STATUS_CREATING.equals(status) || Order.STATUS_CREATED.equals(status) )
			throw new ServletModelException( ServletModelException.ERROR, ctx.msghandler.getMessage("ERR_CANNOT_UPDATE_STATUS") );
		else if( Order.STATUS_ERROR.equals(status) || Order.STATUS_SIMULATED.equals(status) )
			((Order)ctx.db).updateStatus( orderKey, Order.STATUS_WORKSHEET, ctx.sessionMng.getUniqId() );

		long millisecond = System.currentTimeMillis();
		try {
			// 업로드 옵션은 ssl 만 지원.
			recordMap.put( "uploadOption", Upload.UPLOAD_OPTION_REPLACE );
			recordMap.put( "uploadCode", uploadCode );
			recordMap.put( "countryCode", getUserCountryCode(ctx) );
			recordMap.put( "fileName", req.getInputFileName("file") );
			recordMap.put( "uploadUserId", ctx.sessionMng.getUniqId() );
			recordMap.put( "status", Upload.STATUS_READY );

			if( !db.regist(recordMap) )
				throw new DataException( DataException.ERR_CANNOT_INSERT, ctx.msghandler.getMessage(DataException.ERR_CANNOT_INSERT) );
			ctx.handler.commit();

			Map<String, Object> uploadParameterMap = new java.util.HashMap<String, Object> ();
			uploadParameterMap.put( "uploadCode", uploadCode );
			uploadParameterMap.put( "orderKey", ctx.req.getParameter("orderKey") );
			uploadParameterMap.put( "organizationCode", organizationCode );
			uploadParameterMap.put( "distributionChannelCode", ctx.req.getParameter("distributionChannelCode") );
			uploadParameterMap.put( "divisionCode", ctx.req.getParameter("divisionCode") );
			uploadParameterMap.put( "partyCode", ctx.req.getParameter("partyCode") );
			uploadParameterMap.put( "fieldKeyArray", fieldKeyArray );

			String partyCode = (String)uploadParameterMap.get("partyCode");
			String distributionChannelCode = (String)uploadParameterMap.get("distributionChannelCode");
			String divisionCode = (String)uploadParameterMap.get("divisionCode");

			com.irt.dpr.OrderItem itemDB = new com.irt.dpr.OrderItem( ctx.handler );

			Map<String, Object> primaryMap = com.irt.dpr.Party.createPrimary( ctx.req.getParameter("partyCode"),
					organizationCode, ctx.req.getParameter("distributionChannelCode"),
					ctx.req.getParameter("divisionCode") );
			String allowUOM = null;
			if( com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM") )
				allowUOM = itemDB.getDistAllowUOM(primaryMap);
			if (allowUOM == null) {
				if( Country.isFeature(organizationCode, "useSuggestSalesUnitInput") ) {
					allowUOM = null;
				} else {
					allowUOM = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "uom;" + organizationCode, com.irt.dpr.Party.DEFAULT_UOM);
				}
			}

			List<DataLoader.Validator> validators = new ArrayList<DataLoader.Validator>();
			int idx = 0;
			validators.add(idx++, itemDB.createOrderDetailSellingSkuValidator(uploadParameterMap));
			if( Country.isFeature(organizationCode, "useCloseItem") )
				validators.add(idx++, itemDB.createOrderCloseTimeValidator(uploadParameterMap));
			if( Country.isFeature(organizationCode, "useStopItem") )
				validators.add(idx++, itemDB.createNormalOrderStopItemValidator(uploadParameterMap));
			if( Country.isFeature(organizationCode, "usePackDeal") )
				validators.add(idx++, itemDB.createNormalOrderPackDealValidator(uploadParameterMap));
			uploadParameterMap.put("validators", validators);
			// UPLOAD -> ORDER_DETAIL 반영
			DataResult result = db.read( reader, uploadType, uploadParameterMap, ctx.sessionMng.getUniqId(), includingHeaderInd, allowUOM );
			if( result.getErrorCount() > 0 ) {
				ctx.handler.rollback();
				recordMap.put( "status", Upload.STATUS_ERROR );

				if( !db.modify(recordMap) )
					throw new DataException( DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE) );
				ctx.req.setAttribute( "ex_errors", result.getErrors() );
				return forward( ctx, systemConfig.getJspPath() + "/error.jsp" );
			} else {
				ctx.handler.commit();

				recordMap.put( "rowCount", String.valueOf(result.getRowCount()) );
				recordMap.put( "distributionChannelCodes",
						new com.irt.dpr.CountryDistChannel(ctx.handler).getDistributionChannels(getUserCountryCode(ctx), organizationCode) );
				String message = db.execute( result, recordMap );
				if( message == null || message.length() == 0 ) {
					recordMap.put( "insertCount", String.valueOf(result.getRegistCount()) );
					recordMap.put( "updateCount", String.valueOf(result.getModifyCount()) );
					recordMap.put( "deleteCount", String.valueOf(result.getDeleteCount()) );
					recordMap.put( "status", Upload.STATUS_COMPLETE );
				} else {
					recordMap.put( "message", message );
					recordMap.put( "status", Upload.STATUS_ERROR );
				}
				recordMap.put( "executeTime", String.valueOf( (System.currentTimeMillis() - millisecond) / 1000) );

				String[] params = new String[] {
					  String.valueOf(result.getRowCount()), String.valueOf(result.getRegistCount())
					, String.valueOf(result.getModifyCount()), String.valueOf(result.getDeleteCount())
					, String.valueOf(result.getWarningCount())
				};

				if( result.getWarningCount() > 0 && result.getWarns() != null ) {
					ctx.req.setAttribute( "ex_warns", result.getWarns() );
				}

				if( !db.modify(recordMap) )
					throw new DataException( DataException.ERR_CANNOT_UPDATE, ctx.msghandler.getMessage(DataException.ERR_CANNOT_UPDATE) );
				if( message != null && message.length() > 0 )
					ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_DPR_UPLOAD_FAILED", message) );
			}
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
		} finally {
			reader.close();
		}

		ctx.req.setAttribute( "record", recordMap );

		return uploadInput( ctx );
	}

	@Override
	protected boolean uploadInput( Context ctx ) throws IOException, ServletException, SQLException {
		@SuppressWarnings("unchecked")
		Map<String, Object> recordMap = (Map<String, Object>)ctx.req.getAttribute( "record" );
		if( recordMap == null )
			recordMap = new ParameterMap( ctx.req );
		recordMap.put( "partyCode", ctx.req.getParameter("partyCode") );
		String uploadType = ctx.req.getParameter( "uploadType" );
		if( uploadType == null || uploadType.length() == 0 )
			throw new ServletModelException( ServletModelException.NEEDED_PARAMETER );

		String organizationCode = Record.extractString( recordMap, "organizationCode" );
		if( organizationCode == null || organizationCode.length() == 0 )
			organizationCode = getSavedOrganizationCode( ctx );
		recordMap.put( "organizationCode", organizationCode );
		recordMap.put( "encoding", "UTF8" );
		recordMap.put( "orderKey", ctx.req.getParameter("orderKey") );

		ctx.req.setAttribute( "record", recordMap );
		setAttributePartyMaster( ctx, com.irt.data.Record.createMap("countryCode", getUserCountryCode(ctx)), PARTYMASTER_ORGANIZATION );

		ctx.pageConfig.setSubTitle( ctx.msghandler.getMessage("TITLE_DPR_ORDER_DETAIL_UPLOAD") );

		return forward( ctx, systemConfig.getJspPath() + "/dpr_order_upload.jsp" );
	}

	protected boolean wait( Context ctx ) throws IOException, ServletException, SQLException {
		String type = ctx.req.getParameter( "type" );
		String rtype = ctx.req.getParameter( PARAM_REQUESTTYPE );
		String orderKey = ctx.req.getParameter( "orderKey" );
		if( MODE_SIMULATION.equals(type) || MODE_SHORTAGE_ELIMINATE.equals(ctx.mode) ) {
			if( "sim".equals(type) ) {
				List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();
				if( !MODE_SHORTAGE_ELIMINATE.equals(ctx.mode) ) {
					checkDetailContentExtra( ctx, errorList );
					updateDetailContent( ctx, errorList );
				}

				if( errorList.size() > 0 ) {
					ctx.req.setAttribute( "errors", errorList );

					return forward( ctx, systemConfig.getJspPath() +"/error.jsp" );
				}
			}

			if( MODE_SHORTAGE_ELIMINATE.equals(ctx.mode) ) {
				System.setProperty("FakeOrderCanonicalProcess.isRandSimRlt", "false");
			} else {
				System.setProperty("FakeOrderCanonicalProcess.isRandSimRlt", "true");
			}

			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_WAITING_SIMULATION") );
		} else if( MODE_CREATION.equals(type) ) {
			if( com.irt.dpr.Country.isFeature(getSavedOrganizationCode(ctx), "useFreegoods") ) {
				String freegoodsOrderWay = ctx.req.getParameter( "freegoodsOrderWay" );
				if( freegoodsOrderWay != null ) {
					Map<String, Object> recordMap = Order.createPrimary( orderKey );
					recordMap.put( "freegoodsOrderWay" , freegoodsOrderWay );
					Order db = new Order( ctx.handler );
					try {
						db.modify( recordMap, new String[] { "freegoodsOrderWay" } );
					} catch( DataException e ) {
						logger.debug( "error.: "+ orderKey, e );
					}
				}
			}
			ctx.pageConfig.setMessage( ctx.msghandler.getMessage("MSG_WAITING_CREATION") );
		}

		String ftype = ctx.req.getParameter( PARAM_FORMATTYPE );

		if( REQTYPE_REGIST.equals(rtype) ) {
			ctx.pageConfig.setProperty( PARAM_REQUESTTYPE, rtype );
		}
		ctx.pageConfig.setProperty( PARAM_FORMATTYPE, ftype );

		return forward( ctx, systemConfig.getJspPath() +"/dpr_order_wait.jsp" );
	}

	protected List<Map<String,Object>> getDetailListPushUOM( Context ctx, List<Map<String,Object>> detailList ) {
		if( detailList != null ) {
			try {
				( new com.irt.dpr.ItemUOM(ctx.handler) ).pushUOM(detailList);
				for( Map<String, Object> dtl : detailList ) {
					List<Map<String, Object>> uoms = (java.util.ArrayList)dtl.get("uoms");

					String itemCode = (String)dtl.get("itemCode");
					String uom = (String)dtl.get("uom");
					if( uom == null )
						uom = getDefaultUnitOfMeasure(ctx);

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
		}
		return detailList;
	}
}
