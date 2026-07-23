/*
 *	File Name:	Order.java
 *	Version:	2.2.27
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	yjkdev21	2026/05/29		2.2.27	getRecords(): appendOrderBy 추가
 										updateOrderHeaderFromStatus(): status 값이 없을 때 로직 수정
 *	GimHS		2026/04/30		2.2.26	updateOrderHeaderFromStatus(): status값이 없을때 삭제처리하도록 로직 수정, 기타 로그 출력 추가
 *	hankalam	2022/08/31		2.2.25	updateFromProcess(): Freegoods 상품이 존재할 때 orderValue 계산오류 수정
 *	hankalam	2021/11/30		2.2.24	getRecordCount() 추가
 *	hankalam	2021/03/31		2.2.23	getMovQuery(): ship-to level 추가
 *	hankalam	2021/02/26		2.2.22	updateOrderHeaderFromStatus(): freegoods 발주일 경우 SAP 의 order flag 로 update 안되는 문제 수정
 *	hankalam	2020/06/30		2.2.21	Order type ORDER_TYPE_DANGEROUS 추가
 *	jbaek		2019/11/30		2.2.20	Order Detail Update by Line Number(EnableDtlUpdByLn)
 *	hankalam	2019/07/31		2.2.19	Freegoods 기능 추가
 *	jbaek		2019/05/30		2.2.18	PackDeal 기능 추가.
 *	jbaek		2018/03/31		2.2.17	updateOrderDate() : updateReOrderHeader() 변경
 *										reOrder할때  inDate업데이트하여 sim시에 새 ordDate에 맞는 inDate(RDD)가 사용되도록 수정
 *										checkInDateAtLeast() : inDate가 orderDate보다 작은지 체크
 *	jbaek		2017/07/30		2.2.16	updateOrderDetailFromStatus(): consumerEANCode insert코드 추가.
 *	hankalam	2017/05/31		2.2.15	Creating 상태인 오더 조회 로직 추가
 *	jbaek		2017/02/28		2.2.14	개발환경에서 WebMethods Connection Error를 무시하는 코드 추가.
 *	song7981	2016/06/03		2.2.13	rdd refresh 위한 updateOrderDate() 추가
 *	hankalam	2015/07/30		2.2.12	LoadTemplate() : 템플릿 로드시 중복아이템 체크 에러 메시지 추가
 *	jbaek		2014/11/30		2.2.11	Goods Issue Date, Credit Release Date 추가.
										invalid order status exception을 line level exception으로 낮추어 1개의 오더에 에러가 발생해도 다음 오더가 처리될 수 있도록 처리.
 *	jbaek		2014/07/13		2.2.10	Sold-to Level MOV 기능 개발
 *	jbaek		2013/12/31		2.2.9	executeEnquiry(): error message 사이즈 조정
 *	jbaek		2013/05/30		2.2.8	HeaderStatus, LineItemStatus 기능 추가
 *	jbaek		2013/04/30		2.0.7	Sales Mov 관리 기능
 *	jbaek		2012/01/30		2.0.6	PIPO 기능 개발, LineItemStatus 기능 개발
 *	jbaek		2012/08/30		2.0.5	checkCurrentStatus(): 현재 상태 체크기능 추가
										checkProceedStatus(): CG상태에서 CD상태로 변경 가능하도록 변경
 *	GimHS		2011/04/29		2.0.4	getOrderStatus(): 정의되지 않은 Status인 경우 CanonicalXMLException throw
 *	lsinji		2009/10/23		2.0.3	Simulation상태 이후에 Order Detail이 수정 되면 simulation과 관련한 값을 null로 처리
 *										Simulation Result의 라인중에 ORDER_DTL에 없는 경우 DataException화 해서 throw 하도록 변경
 *	lsinji		2009/06/30		2.0.2	ORDER_IF_CREATION 등 상수 추가
 *	lsinji		2009/03/31		2.0.1	updateOrderDetailFromStatus()에서 itemCode를 UPDATE하고
										, 조건에 itemCode -> lineNumber로 변경
 *	guksm		2008/09/26		2.0.0	create
 *
**/

package com.irt.dpr;

import com.irt.dpr.util.Loggers;
import com.irt.rbm.RBMSystem;
import com.irt.servlet.SystemConfig;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import org.apache.log4j.Logger;

import com.irt.data.DataException;
import com.irt.data.DataResult;
import com.irt.data.FieldException;
import com.irt.data.Record;
import com.irt.data.ValidableField;
import com.irt.data.ValidableFieldSet;
import com.irt.sql.ConditionQueryBuffer;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.util.Arrays;

/**
 *
 */
public class Order extends com.irt.rbm.ManipulableManagerImpl {//@formatter:off
	public static final String ORDER_TYPE_NORMAL			= "NO";
	public static final String ORDER_TYPE_DANGEROUS			= "DA";
	public static final String ORDER_TYPE_PROMOTION			= "PR";

	public static final String ORDERREVISE_MODIFYING			= "MD";
	public static final String ORDERREVISE_COMPLETE_REQUEST		= "CQ";

	public final static int	SYSTEMRDD_PLUS_DAYS				= 2;
	public final static int	SAPRDDNULL_PLUS_DAYS			= 2;//When sap RDD does not have... default value to be 2
	public final static long	CHINA_ORDERCLOSING_TIME			= 57600000;
	private final static int[]	CHINA_HOLIDAYS					= new int[] { Calendar.SUNDAY };
	private final static int[]	SHANGHAI_HOLIDAYS				= new int[] { Calendar.SATURDAY, Calendar.SUNDAY };


	public static final String STATUS_WORKSHEET				= "WK";
	public static final String STATUS_SIMULATING			= "SG";
	public static final String STATUS_SIMULATED				= "SD";
	public static final String STATUS_CREATING				= "CG";
	public static final String STATUS_CREATED				= "CD";
	public static final String STATUS_ERROR					= "ER";
	public static final String STATUS_DELETED				= "DE";

	public static final String ORDERSTATUS_DELIVERYBLOCK	= "DB";
	public static final String ORDERSTATUS_PENDING_CREDITCHECK	= "PC";
	public static final String ORDERSTATUS_INVOICED			= "IN";
	public static final String ORDERSTATUS_VATISPROCESSING	= "VP";
	public static final String ORDERSTATUS_SHIPPED			= "SH";
	public static final String ORDERSTATUS_CONFIRMED_BEINGPROCESSED	= "CP";
	public static final String ORDERSTATUS_CONFIRMED		= "CO";
	public static final String ORDERSTATUS_CLOSED			= "CL";
	public static final String ORDERSTATUS_CANCELLED		= "CE";
	public static final String ORDERSTATUS_HOLD				= "HO";
	public static final String ORDERSTATUS_DELETED			= "DE";			// define to between d-portal 2.0 and wm

	public static final String EXTRATYPE_FREEGOODS_ORDER	= "FO";
	public static final String EXTRATYPE_BILLING			= "BL";
	public static final String EXTRATYPE_DELIVERY			= "DV";
	public static final String EXTRATYPE_MEMOS				= "MM";

	public static final String FREEGOODS_ORDERKEY_SUFFIX	= "FG";
	public static final String NORMAL_ORDERKEY_SUFFIX		= "WFG";

	public static final String WM_ORDERSTATUS_PENDING_CREDITCHECK	= "1";
	public static final String WM_ORDERSTATUS_INVOICED				= "2";
	public static final String WM_ORDERSTATUS_VATISPROCESSING		= "3";
	public static final String WM_ORDERSTATUS_SHIPPED				= "4";
	public static final String WM_ORDERSTATUS_CONFIRMED_BEINGPROCESSED	= "5";
	public static final String WM_ORDERSTATUS_CONFIRMED				= "6";
	public static final String WM_ORDERSTATUS_CLOSED				= "7";
	public static final String WM_ORDERSTATUS_CANCELLED				= "8";
	public static final String WM_ORDERSTATUS_DELETED				= "D";

	public static final String ORDER_IF_CREATION			= com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_CREATION;
	public static final String ORDER_IF_RDD					= com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_RDD;
	public static final String ORDER_IF_SIMULATION			= com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_SIMULATION;
	public static final String ORDER_IF_STATUSLIST			= com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_STATUSLIST;
	public static final String ORDER_IF_STATUS				= com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_STATUS;
	public static final String ORDER_IF_BILLING				= com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_BILLING;

	public static final int ORDER_HIGH_LIMIT				= 9999;
	public static final int ORDER_LOW_LIMIT					= 1;
	public static final int PACKDEALORDER_LOW_LIMIT			= 0;

	public final static String SIMULATION_SHORTAGE_ELIMINATE		= "EL";
	public final static String SIMULATION_SHORTAGE_REQUEST		= "RQ";

	private final String ERRORTYPE_NOTSELLINGSKU			= "NSK";
	private final String ERRORTYPE_ALREADY_REGISTERED_ORDERITEM	= "ARO";

	private final static Table table = Schema.findTable( Schema.DPR_ORDER );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ORDER );

	private SystemConfig systemConfig;
	Logger logger = Logger.getLogger( Order.class );

	public Order( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public Order( SQLHandler handler, SystemConfig systemConfig ) {
		super( handler, table, factory );
		this.systemConfig = systemConfig;
	}

	public boolean checkCreatingStatus( String soldPartyCode, String shipPartyCode ) throws SQLException {
		int recordCount = SQLManager.getInt( handler
				, "SELECT COUNT(*) FROM DPR_ORDER WHERE STATUS = 'CG' AND TRUNC(ORDDATE) = TRUNC(SYSDATE)"
						+ "	AND PARTYCD = ? AND SOLD_PARTYCD = ? AND SHIP_PARTYCD = ?", soldPartyCode, soldPartyCode, shipPartyCode );
		return recordCount > 0 ? true : false;
	}

	public String checkCurrentStatus( String orderKey ) throws SQLException {
		Map<String, Object> recordMap = SQLManager.getRecordMap( handler, null
				, "SELECT ORDER_NUMBER \"orderNumber\", STATUS \"status\" FROM DPR_ORDER WHERE ORDER_KEY = ?", orderKey );

		String currentStatus = null;
		if( recordMap != null ) {
			currentStatus = (String)recordMap.get( "status" );
		}

		//check currentStatus value is valid
		if( !(STATUS_CREATED.equals(currentStatus) || STATUS_CREATING.equals(currentStatus) || STATUS_DELETED.equals(currentStatus)
				|| STATUS_ERROR.equals(currentStatus) || STATUS_SIMULATED.equals(currentStatus) || STATUS_SIMULATING.equals(currentStatus)
				|| STATUS_WORKSHEET.equals(currentStatus)) )
			return null;

		return currentStatus;
	}

	public boolean checkInDateAtLeast( String orderKey ) throws SQLException {
		Map<String, Object> recordMap = SQLManager.getRecordMap( handler, null,
				"SELECT CASE WHEN TRUNC(ORDDATE) > TRUNC(INDATE) THEN 'N' ELSE 'Y' END \"isInDateValid\""
				+ " FROM DPR_ORDER WHERE ORDER_KEY = ?", orderKey );

		if( recordMap != null ) {
			return "Y".equals(recordMap.get("isInDateValid"));
		}

		return false;
	}

	public boolean checkProceedStatus( String orderKey, String proceedStatus ) throws SQLException {
		return checkProceedStatus( orderKey, proceedStatus, false );
	}

	public boolean checkProceedStatus( String orderKey, String proceedStatus, boolean inserting ) throws SQLException {
		Map<String, Object> recordMap = SQLManager.getRecordMap( handler, null
				, "SELECT ORDER_NUMBER \"orderNumber\", STATUS \"status\" FROM DPR_ORDER WHERE ORDER_KEY = ?", orderKey );

		String currentStatus = null, orderNumber = null;
		if( recordMap != null ) {
			currentStatus = (String)recordMap.get( "status" );
			orderNumber = (String)recordMap.get( "orderNumber" );
		}

		if( orderNumber != null ) return false;
		if( STATUS_WORKSHEET.equals(proceedStatus) )
			return ( (inserting && currentStatus == null) || STATUS_WORKSHEET.equals(currentStatus)
				|| STATUS_ERROR.equals(currentStatus) || STATUS_SIMULATED.equals(currentStatus) );
		else if( STATUS_SIMULATED.equals(proceedStatus) )
			return STATUS_WORKSHEET.equals(currentStatus) || STATUS_ERROR.equals(currentStatus) || STATUS_SIMULATED.equals(proceedStatus);
		else if( STATUS_CREATED.equals(proceedStatus) )
			return STATUS_SIMULATED.equals(currentStatus) || STATUS_CREATING.equals(currentStatus);
		else
			return false;
	}

	public static Map<String, Object> createPrimary( String orderKey ) {
		return Record.createMap( "orderKey", orderKey );
	}

	public String createSimulationKey() throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT 'DP' || SEQDPR_SIMULATION.nextval FROM DUAL" );
	}

	public boolean updateReOrderHeader( String orderKey, Map<String, Object> infoMap ) throws DataException, SQLException {
		ValidableFieldSet fields = new ValidableFieldSet(new ValidableField[] {
			  new ValidableField( false, "inDate", "DPR_ORDER_INDATE", Schema.DATE )
			, new ValidableField( false, "inDateDefault", "DPR_ORDER_DEFAULTINDATE", Schema.DATE )
//			, new ValidableField( false, "inDateSimulation", "DPR_ORDER_INDATESIMULATION", Schema.DATE )
			, new ValidableField( false, "orderKey", "DPR_ORDER_KEY", Schema.STRING )
		});

		Object[] bindVars = null;
		try {
			bindVars = fields.validate(infoMap);
		} catch( FieldException fdEx ) {
			throw new DataException(fdEx.getErrorKey(), fdEx.getMessage());
		}

		String sql = "UPDATE DPR_ORDER SET ORDDATE = SYSDATE, INDATE = ?, INDATE_DEF = ?, INDATE_SIM = NULL"
					+ "	WHERE ORDER_KEY = ?";

		PreparedStatement pstmt = handler.getConnection().prepareStatement(sql);
		SQLManager.bindVariables(pstmt, bindVars);
		if( pstmt.executeUpdate() > 0 ) {
			return true;
		} else {
			return false;
		}
	}

	public void executeEnquiry( String type, Map<String, Object> parameterMap ) throws DataException, SQLException {
		// In development database, set 'N' to ignore webMethods Connection Error!
		boolean isExist = com.irt.rbm.RBMSystem.getSystemEnvBool( "DPR", "WebMethods;IsExist", true );
		executeEnquiry( type, parameterMap, isExist );
	}

	/*
	 * CALLING WM: ORDER_IF_STATUS, ORDER_IF_STATUSLIST, ORDER_IF_BILLING, ORDER_IF_SIMULATION, ORDER_IF_CREATION
	 * ORDER_IF_STATUSLIST: CALL OrderCanonicalProcess
	 * ORDER_IF_STATUS: CALL OrderCanonicalProcess
	 * ORDER_IF_BILLING: CALL OrderCanonicalProcess
	 * ORDER_IF_SIMULATION: UPDATE DPR_ORDER.STATUS = STATUS_SIMULATING, CALL OrderCanonicalProcess
	 * ORDER_IF_CREATION: UPDATE DPR_ORDER.STATUS = STATUS_CREATING, CALL OrderCanonicalProcess
	 */
	public void executeEnquiry( String type, Map<String, Object> parameterMap, boolean isExistWM ) throws DataException, SQLException {
		if( !isExistWM ) return;

		Loggers.business.info( "{}: {}", parameterMap.get("orderKey"), type + " start." );
		com.irt.dpr.tools.OrderCanonicalProcess ocp = new com.irt.dpr.tools.OrderCanonicalProcess( handler, systemConfig, type );

		if( com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_STATUS.equals(type) || com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_STATUSLIST.equals(type) || com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_BILLING.equals(type) ) {
			ocp.setParameter( parameterMap );
			try {
				ocp.execute();
				Loggers.business.info( "{}: {}", parameterMap.get("orderKey"), type + " end." );
			} catch( OrderProcessException opEx ) {
				throw handler.createDataException( opEx.getMessage() );
			}
		} else {
			PreparedStatement pstmt = null;
			try {
				if( com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_SIMULATION.equals(type) ) {
					pstmt = handler.getConnection().prepareStatement(
						"UPDATE DPR_ORDER SET MESSAGE = ?, STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ?"
							+" WHERE ORDER_KEY = ?" );
					pstmt.setString( 1, null );
					pstmt.setString( 2, STATUS_SIMULATING );
					pstmt.setString( 3, (String)parameterMap.get("updateUserId") );
					pstmt.setString( 4, (String)parameterMap.get("orderKey") );
				} else if( com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_CREATION.equals(type) ) {
					pstmt = handler.getConnection().prepareStatement(
						"UPDATE DPR_ORDER SET MESSAGE = ?, STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ? WHERE ORDER_KEY = ?" );
					pstmt.setString( 1, null );
					pstmt.setString( 2, STATUS_CREATING );
					pstmt.setString( 3, (String)parameterMap.get("updateUserId") );
					pstmt.setString( 4, (String)parameterMap.get("orderKey") );
				} else
					throw handler.createDataException( handler.getMessageHandler().getMessage("ERR_INVALID_ENQUIRYTYPE") );

				if( pstmt != null && pstmt.executeUpdate() == 1 ) {
					ocp.setParameter( parameterMap );
					try {
						ocp.execute();
					} catch( OrderProcessException opEx ) {
						pstmt.setString( 1, opEx.getMessage().substring(0, Math.min(opEx.getMessage().length(), 510)) );
						pstmt.setString( 2, STATUS_ERROR );
						pstmt.executeUpdate();
						handler.commit();

						throw handler.createDataException( handler.getMessageHandler().getMessage(opEx.getErrorKey()) );
					}
				} else
					throw handler.createDataException( handler.getMessageHandler().getMessage("ERR_FAILED_ENQUIRY_QUERY") );
			} finally {
				if( pstmt != null ) pstmt.close();
				Loggers.business.info( "{}: {}", parameterMap.get("orderKey"), type + " end." );
			}
		}
	}

	public List<Map<String, Object>> getExtraRecords( Map<String, Object> conditionMap, String type ) throws SQLException {
		QueryFactory sub_factory = null;
		if( EXTRATYPE_BILLING.equals(type) )
			sub_factory = Schema.findQueryFactory( Schema.DPR_ORDER_BILLING );
		else if( EXTRATYPE_DELIVERY.equals(type) )
			sub_factory = Schema.findQueryFactory( Schema.DPR_ORDER_DELIVERY );
		else if( EXTRATYPE_MEMOS.equals(type) )
			sub_factory = Schema.findQueryFactory( Schema.DPR_ORDER_MEMOS );
		else if( EXTRATYPE_FREEGOODS_ORDER.equals(type) )
			sub_factory = Schema.findQueryFactory( Schema.DPR_ORDER );
		else
			return null;

		if( sub_factory != null ) {
			QueryBuffer querybuf = sub_factory.setQuery( new ConditionQueryBuffer(conditionMap) );
			appendOrderBy( querybuf, sub_factory );

			return SQLManager.getRecordList( handler, querybuf, 0, -1 );
		} else
			return null;
	}

	public String getFreegoodsOrderKey( String normalOrderKey ) throws SQLException {
		Map<String, Object> conditionMap = com.irt.data.Record.createMap( "parentOrderKey", normalOrderKey );
		java.util.List<Map<String, Object>> records = getRecords( conditionMap, new String[] { "orderKey" } );

		if( records == null || records.size() == 0 || records.size() > 1 )
			return null;

		return (String)records.get(0).get( "orderKey" );
	}
	public String getOrderNumber( String purchaseOrderNumber ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler
				, "SELECT ORDER_NUMBER FROM DPR_ORDER WHERE SIMULATIONKEY = ?", purchaseOrderNumber );
	}

	public String getOrderNumberWithOrderKey( String orderKey ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler
				, "SELECT ORDER_NUMBER FROM DPR_ORDER WHERE ORDER_KEY = ?", orderKey );
	}

	public String getOrderKey( String orderNumber ) throws SQLException {
		Map<String, Object> conditionMap = com.irt.data.Record.createMap( "orderNumber", orderNumber );
		java.util.List<Map<String, Object>> records = getRecords( conditionMap, new String[] { "orderKey" } );

		if( records == null || records.size() == 0 || records.size() > 1 )
			return null;

		return (String)records.get(0).get( "orderKey" );
	}

	public String getOrderKey() throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT 'ORD' || seqDPR_ORDER.NEXTVAL FROM DUAL" );
	}

	/** SAP Order FLAG = DPR_ORDER.ORDER_STATUS. 참고로 D-Portal status( WK, SD, CD... ) = DPR_ORDER.STATUS */
	public static String getOrderStatus( String orderStatus ) throws com.irt.dpr.tools.CanonicalXMLException {
		if( orderStatus.equals(WM_ORDERSTATUS_PENDING_CREDITCHECK) )
			return ORDERSTATUS_PENDING_CREDITCHECK;
		else if( orderStatus.equals(WM_ORDERSTATUS_INVOICED) )
			return ORDERSTATUS_INVOICED;
		else if( orderStatus.equals(WM_ORDERSTATUS_VATISPROCESSING) )
			return ORDERSTATUS_VATISPROCESSING;
		else if( orderStatus.equals(WM_ORDERSTATUS_SHIPPED) )
			return ORDERSTATUS_SHIPPED;
		else if( orderStatus.equals(WM_ORDERSTATUS_CONFIRMED_BEINGPROCESSED) )
			return ORDERSTATUS_CONFIRMED_BEINGPROCESSED;
		else if( orderStatus.equals(WM_ORDERSTATUS_CONFIRMED) )
			return ORDERSTATUS_CONFIRMED;
		else if( orderStatus.equals(WM_ORDERSTATUS_CLOSED) )
			return ORDERSTATUS_CLOSED;
		else if( orderStatus.equals(WM_ORDERSTATUS_CANCELLED) )
			return ORDERSTATUS_CANCELLED;
		else if( orderStatus.equals(WM_ORDERSTATUS_DELETED) )
			return ORDERSTATUS_DELETED;
		else if( orderStatus.equals(ORDERSTATUS_HOLD) )
			return ORDERSTATUS_HOLD;
		else
			throw new com.irt.dpr.tools.CanonicalXMLException( com.irt.dpr.tools.CanonicalXMLException.INVALID_ORDERSTATUS );
	}

	@Override
	public int getRecordCount( Map<String, ? extends Object> conditionMap ) throws SQLException {
		int recentCount = 0;
		if( conditionMap.containsKey("recentCount") ) {
			recentCount = (int) conditionMap.get( "recentCount" );
		}

		if( recentCount > 0 ) {
			QueryBuffer querybuf = new QueryBuffer();
			QueryBuffer inner_querybuf = factory.setConditionQuery( new ConditionQueryBuffer(conditionMap) );
			inner_querybuf.appendData( "'DUMMY'" );
			inner_querybuf.appendOrderByFieldName( "createDateTime", "DESC" );
			querybuf.appendData( "COUNT(*)" );
			querybuf.appendTable( inner_querybuf, "ORD", "ROWNUM <= " + recentCount );

			return SQLManager.getInt( handler, querybuf );
		} else {
			return super.getRecordCount( conditionMap );
		}
	}

	@Override
	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException {
		int recentCount = 0;
		if( conditionMap.containsKey("recentCount") ) {
			recentCount = (int) conditionMap.get( "recentCount" );
		}

		if( recentCount > 0 ) {
			QueryBuffer querybuf = new QueryBuffer();
			QueryBuffer inner_querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), Arrays.append(fieldKeys, "createDateTime") );

			appendOrderBy( inner_querybuf );
			inner_querybuf.appendOrderByFieldName( "createDateTime", "DESC" );
			querybuf.appendData( "*" );
			querybuf.appendTable( inner_querybuf, "ORD", "ROWNUM <= " + recentCount );

			return SQLManager.getRecordList( handler, querybuf, 0, -1 );
		} else {
			return super.getRecords( conditionMap, fieldKeys );
		}
	}

	public String getSimulationKey( String orderKey, boolean isCreate ) throws SQLException {
		if( orderKey == null || orderKey.length() == 0 || isCreate )
			return createSimulationKey();

		String simulationKey = (String)SQLManager.getObjectValue( handler,
				"SELECT SIMULATIONKEY FROM DPR_ORDER WHERE ORDER_KEY = ?", orderKey );

		if( simulationKey == null || simulationKey.length() == 0 )
			simulationKey = createSimulationKey();

		return simulationKey;
	}

	/*
	 * DPRPlaceOrder.simulationResult
	 * Summary Lines in Simulation Result Screen
	 */
	public Map<String, Object> getSimulationSummary( Map<String, Object> conditionMap ) throws SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );

		querybuf.appendDataWithAlias( "ORD.ORDERVALUE", "orderValue" );
		querybuf.appendDataWithAlias( "ORD.ORDERTAX", "orderTax" );
		querybuf.appendDataWithAlias( "ORD.ORDERDISCOUNT", "orderDiscount" );
		querybuf.appendDataWithAlias( "ORD.ORDERTOTAL", "orderTotal" );
		querybuf.appendDataWithAlias( "CNF.INPUTTED_ORDERVALUE", "inputtedOrderValue" );
		querybuf.appendDataWithAlias( "(CNF.INPUTTED_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERTAX", "inputtedOrderTax" );
		querybuf.appendDataWithAlias( "(CNF.INPUTTED_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERDISCOUNT", "inputtedOrderDiscount" );
		querybuf.appendDataWithAlias( "( NVL(CNF.INPUTTED_ORDERVALUE, 0) + "
				+ "NVL((CNF.INPUTTED_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERTAX, 0) + "
				+ "NVL((CNF.INPUTTED_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERDISCOUNT, 0) )", "inputtedOrderTotal" );
		querybuf.appendDataWithAlias( "CNF.CONFIRMED_ORDERVALUE", "confirmedOrderValue" );
		querybuf.appendDataWithAlias( "(CNF.CONFIRMED_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERTAX", "confirmedOrderTax" );
		querybuf.appendDataWithAlias( "(CNF.CONFIRMED_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERDISCOUNT", "confirmedOrderDiscount" );
		querybuf.appendDataWithAlias( "( NVL(CNF.CONFIRMED_ORDERVALUE, 0) + "
				+ "NVL((CNF.CONFIRMED_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERTAX, 0) + "
				+ "NVL((CNF.CONFIRMED_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERDISCOUNT, 0) )", "confirmedOrderTotal" );

		querybuf.appendDataWithAlias( "NVL(SMOV.MIN_VALUE, 0)", "minimumValue" );
		querybuf.appendDataWithAlias( "(CASE WHEN " +
				"( NVL(CNF.CONFIRMED_ORDERVALUE, 0) + "
						+ "NVL((CNF.CONFIRMED_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERTAX, 0) + "
						+ "NVL((CNF.CONFIRMED_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERDISCOUNT, 0) )" +
						" < SMOV.MIN_VALUE THEN 'N' ELSE 'Y' END)", "isPlaceOrderable" );

		if( querybuf.isConditionTrue("isRevOrd") ) {
			querybuf.appendDataWithAlias( "NVL(CNF.REVFIN_ORDERVALUE, 0)", "revfinOrderValue" );
			querybuf.appendDataWithAlias( "( NVL(CNF.REVFIN_ORDERVALUE, 0) + "
					+ "NVL((CNF.REVFIN_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERTAX, 0) + "
					+ "NVL((CNF.REVFIN_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERDISCOUNT, 0) )", "revfinOrderTotal" );
			querybuf.appendDataWithAlias( "(CASE WHEN " +
					"( NVL(CNF.REVFIN_ORDERVALUE, 0) + "
							+ "NVL((CNF.REVFIN_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERTAX, 0) + "
							+ "NVL((CNF.REVFIN_ORDERVALUE / NULLIF(ORD.ORDERVALUE, 0)) * ORD.ORDERDISCOUNT, 0) )" +
							" < SMOV.MIN_VALUE THEN 'N' ELSE 'Y' END)", "isPlaceRevisible" );
		}

		querybuf.appendTableWithAlias( "DPR_ORDER", "ORD" );

		ConditionQueryBuffer inner_querybuf = new ConditionQueryBuffer( conditionMap );
		inner_querybuf.appendDataWithGroupBy("ODTL.ORDERKEY", "ORDERKEY");
		inner_querybuf.appendDataWithAlias( "SUM(ODTL.SIMULATION_ORDERVALUE)", "CONFIRMED_ORDERVALUE" );
		inner_querybuf.appendDataWithAlias( "SUM(ODTL.ORDERVALUE)", "INPUTTED_ORDERVALUE" );

		if( inner_querybuf.isConditionTrue("isRevOrd") ) {
			inner_querybuf.appendDataWithAlias( "SUM(ODTL.PRICE*((CASE WHEN ORDERQTY > (NVL(SIMULATION_ORDERQTY,0)+NVL(REVBF_CNFQTY,0))"
								+ " THEN NVL(REVBF_CNFQTY,0) + NVL(SIMULATION_ORDERQTY,0)"
								+ " ELSE ORDERQTY"
								+ " END)))", "REVFIN_ORDERVALUE" );
		}

		inner_querybuf.appendTableWithAlias( "DPR_ORDER_DTL", "ODTL" );
		inner_querybuf.findCondition( "orderKey", "ODTL.ORDERKEY" );

		querybuf.appendTable( inner_querybuf, "CNF", "CNF.ORDERKEY = ORD.ORDER_KEY" );
		querybuf.appendTable( getMovQuery(conditionMap), "SMOV", "SMOV.PARTYCD(+) = ORD.PARTYCD AND SMOV.ORGANIZATIONCD(+) = ORD.ORGANIZATIONCD" +
				" AND SMOV.DIST_CHANNELCD(+) = ORD.DIST_CHANNELCD AND SMOV.DIVISIONCD(+) = ORD.DIVISIONCD" );

		querybuf.findCondition( "orderKey", "ORD.ORDER_KEY" );
		querybuf.findCondition( "status", "ORD.STATUS" );

		return SQLManager.getRecordMap( handler, null, querybuf );
	}

	/**
	 * @return for vwDPR_ORDER list of {("itemCode" or "itemCodeConfirmed"), "itemConsumerEANCode"}
	 */
	public List<Map<String, Object>> getItemEANList( SQLHandler handler, List<Map<String, Object>> detailList, Map<String, Object> conditionMap,
			boolean isPIPOConfirmedItem ) throws SQLException {
		String itemCodeFieldKey = null;
		String itemCodeTableFieldKey = "";
		if( isPIPOConfirmedItem ) {
			itemCodeFieldKey = "itemCodeConfirmed";
			itemCodeTableFieldKey = "ITEMCD_CNF";
		} else {
			itemCodeFieldKey = "itemCode";
			itemCodeTableFieldKey = "ITEMCD";
		}
		List<Object> itemCodes = new ArrayList<Object>();
		String orderKey = null;
		for( Map<String, Object> map : detailList ) {
			Object obj = map.get( itemCodeFieldKey );
			itemCodes.add( obj );

			if( orderKey == null ) {
				orderKey = (String)map.get( "orderKey" );
			}
		}

		if( conditionMap == null )
			conditionMap = Record.createMap( "orderKey", orderKey );
		else
			conditionMap.put( "orderKey", orderKey );

		// to get pair
		conditionMap.put( itemCodeFieldKey, itemCodes );

		ConditionQueryBuffer qb_base = new ConditionQueryBuffer( conditionMap );
//		ConditionQueryBuffer qb_IMPEAN = com.irt.dpr.Schema.getItemEANQueryBuffer( conditionMap, isPIPOConfirmedItem );
		qb_base.appendTableWithAlias( "vwDPR_ITEM_EANRLT", "IMEAN" );
		qb_base.appendTableWithAlias( "vwDPR_ORDER", "ORD" );
		qb_base.appendCondition( "IMEAN.ORGANIZATIONCD(+) = ORD.ORGANIZATIONCD AND IMEAN.ITEMCD(+) = ORD." + itemCodeTableFieldKey );

		qb_base.appendDataWithAlias( "ORD." + itemCodeTableFieldKey, itemCodeFieldKey );
		qb_base.appendDataWithAlias( "IMEAN.ITEM_CONS_EAN", "itemConsumerEANCode" );

		qb_base.findCondition( "organizationCode", "ORD.ORGANIZATIONCD" );
		qb_base.findCondition( itemCodeFieldKey, "ORD." + itemCodeTableFieldKey );
		qb_base.findCondition( "orderKey", "ORD.ORDERKEY" );

		return SQLManager.getRecordList( handler, qb_base );
	}

	/**
	 * @return for vwDPR_ORDER map pair eg [{'1234567', '930555552235'}, {'123456', '93066666666'}]
	 */
	public Map<String, Object> getItemEANListOfPair( SQLHandler handler, List<Map<String, Object>> detailList, Map<String, Object> conditionMap,
			boolean isPIPOConfirmedItem ) throws SQLException {
		String itemCodeFieldKey = null;
		if( isPIPOConfirmedItem ) {
			itemCodeFieldKey = "itemCodeConfirmed";
		} else {
			itemCodeFieldKey = "itemCode";
		}

		List<Map<String, Object>> list = getItemEANList( handler, detailList, conditionMap, isPIPOConfirmedItem );

		Map<String, Object> map = new HashMap<String, Object>();
		if( list != null && list.size() > 0 ) {
			for( Map<String, Object> obj : list ) {
				map.put( (String)obj.get( itemCodeFieldKey ), obj.get( "itemConsumerEANCode" ) );
			}
		}

		return map;
	}

	private QueryBuffer getMovQuery( Map<String,Object> conditionMap ) {
		boolean useDangerousItem = Country.isFeature( (String)conditionMap.get("organizationCode"), "useDangerousItem" );
		String dangerousInd = "N";
		if( useDangerousItem ) {
			String orderType = (String)conditionMap.get( "orderType" );
			if( Order.ORDER_TYPE_DANGEROUS.equals(orderType) ) {
				dangerousInd = "Y";
			}
		}

		QueryBuffer spmov_innerquerybuf = new QueryBuffer();
		spmov_innerquerybuf.append( "SPLNK.PARTYCD, SBMOV.ORGANIZATIONCD, SBMOV.DIST_CHANNELCD, SBMOV.DIVISIONCD, SBMOV.SHIP_PARTYCD, SBMOV.DANGEROUS_IND, SBMOV.MIN_VALUE" );
		spmov_innerquerybuf.appendTableWithAlias( "DPR_SALES_MOVSPTY", "SBMOV" );
		spmov_innerquerybuf.appendTableWithAlias( "DPR_PARTY_LINK", "SPLNK"
				, "SBMOV.SHIP_PARTYCD = SPLNK.LINK_PARTYCD AND SBMOV.ORGANIZATIONCD  = SPLNK.ORGANIZATIONCD" +
				" AND SBMOV.DIST_CHANNELCD = SPLNK.DIST_CHANNELCD AND SBMOV.DIVISIONCD = SPLNK.DIVISIONCD" +
				" AND SBMOV.SHIP_PARTYCD = ? AND SBMOV.ORGANIZATIONCD = ? AND SBMOV.DIST_CHANNELCD = ? AND SBMOV.DIVISIONCD = ?");

		ConditionQueryBuffer mov_querybuf = new ConditionQueryBuffer( conditionMap );
		mov_querybuf.appendDataWithAlias( "PTYS.ORGANIZATIONCD", "ORGANIZATIONCD" );
		mov_querybuf.appendDataWithAlias( "PTYS.DIST_CHANNELCD", "DIST_CHANNELCD" );
		mov_querybuf.appendDataWithAlias( "PTYS.DIVISIONCD", "DIVISIONCD" );
		mov_querybuf.appendDataWithAlias( "PTYS.OFFICECD", "OFFICECD" );
		mov_querybuf.appendDataWithAlias( "PTYS.PARTYCD", "PARTYCD" );
		mov_querybuf.appendDataWithAlias( "NVL2(SPMOV.SHIP_PARTYCD, SPMOV.MIN_VALUE, NVL(PMOV.MIN_VALUE, SMOV.MIN_VALUE))", "MIN_VALUE" );
		mov_querybuf.appendTableWithAlias( "DPR_PARTY_SALES", "PTYS" );
		mov_querybuf.appendTableWithAlias( "DPR_SALES_MOVPTY", "PMOV"
				, "PTYS.ORGANIZATIONCD = PMOV.ORGANIZATIONCD(+) AND PTYS.DIST_CHANNELCD = PMOV.DIST_CHANNELCD(+)" +
				" AND PTYS.DIVISIONCD = PMOV.DIVISIONCD(+) AND PTYS.OFFICECD = PMOV.OFFICECD(+) AND PTYS.PARTYCD = PMOV.PARTYCD(+)" +
				" AND PMOV.DANGEROUS_IND(+) = ?" );
		mov_querybuf.appendTableWithAlias( "DPR_SALES_MOV", "SMOV"
				, "PTYS.ORGANIZATIONCD = SMOV.ORGANIZATIONCD(+) AND PTYS.DIST_CHANNELCD = SMOV.DIST_CHANNELCD(+)" +
				" AND PTYS.DIVISIONCD = SMOV.DIVISIONCD(+) AND PTYS.OFFICECD = SMOV.OFFICECD(+) AND SMOV.DANGEROUS_IND(+) = ?" );
		mov_querybuf.appendTable( spmov_innerquerybuf, "SPMOV"
				, "PTYS.ORGANIZATIONCD = SPMOV.ORGANIZATIONCD(+) AND PTYS.DIST_CHANNELCD = SPMOV.DIST_CHANNELCD(+)" +
				" AND PTYS.DIVISIONCD = SPMOV.DIVISIONCD(+) AND PTYS.PARTYCD = SPMOV.PARTYCD(+) AND SPMOV.DANGEROUS_IND(+) = ?" );

		Object[] bindVars = { conditionMap.get("shipPartyCode"), conditionMap.get("organizationCode")
				, conditionMap.get("distributionChannelCode"), conditionMap.get("divisionCode")
				, dangerousInd, dangerousInd, dangerousInd
		};

		mov_querybuf.addBindVariables( 1, bindVars );
		return mov_querybuf;
	}

	public List<Map<String, Object>> loadTemplate( String orderKey, String templateKey, String updateUserId, com.irt.data.Date toDay ) throws DataException, SQLException {
		CallableStatement cstmt = null;
		List<Map<String, Object>> errorList = new java.util.ArrayList<Map<String, Object>>();

		try {
			cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkDPRTemplate.fTemplateToOrder( ?, ?, ?, ?, ?, ?, ? ); END;" );
			cstmt.registerOutParameter( 1, Types.INTEGER );
			cstmt.registerOutParameter( 2, Types.VARCHAR );
			cstmt.registerOutParameter( 3, Types.INTEGER );
			cstmt.registerOutParameter( 4, Types.INTEGER );
			cstmt.setString( 5, orderKey );
			cstmt.setString( 6, templateKey );
			cstmt.setDate( 7, toDay );
			cstmt.setString( 8, updateUserId );
			cstmt.executeUpdate();

			if( cstmt.getInt(1) > 0 ) {
				List<Map<String, Object>> resultList = SQLManager.getRecordList( handler
						, "SELECT ITEMCD \"itemCode\", ERR_TYPE \"errorType\" FROM TMP_VAL_TEMPLATEITEM", null );
				if( resultList != null && resultList.size() > 0 ) {
					for( Map<String, Object> obj : resultList ) {
						Map<String, Object> map = obj;
						Map<String, Object> errorMap = new java.util.HashMap<String, Object>();
						errorMap.put( "name", map.get("itemCode") );

						if( ERRORTYPE_NOTSELLINGSKU.equals(map.get("errorType")) )
							errorMap.put( "message", handler.getMessageHandler().getMessage("ERR_ISNOT_SELLINGSKU") );
						else if( ERRORTYPE_ALREADY_REGISTERED_ORDERITEM.equals(map.get("errorType")) )
							errorMap.put( "message", handler.getMessageHandler().getMessage("ERR_ALREADY_REGISTERED_ORDERITEM") );
						else
							errorMap.put( "message", handler.getMessageHandler().getMessage("ERR_ERROR") );

						errorList.add( errorMap );
					}
				}
			} else {
				Map<String, Object> errorMap = new java.util.HashMap<String, Object>();
				errorMap.put( "name", null );
				errorMap.put( "message", cstmt.getString(2) );

				errorList.add( errorMap );
			}
		} finally {
			if( cstmt != null ) cstmt.close();
		}

		return errorList;
	}

	public boolean lockOrder( String orderKey ) throws SQLException {
		PreparedStatement pstmt = null;

		try {
			pstmt = handler.getConnection().prepareStatement( "SELECT ORDER_KEY FROM DPR_ORDER WHERE ORDER_KEY = ? FOR UPDATE NOWAIT" );
			SQLManager.bindVariables( pstmt, new Object[] { orderKey } );

			return pstmt.execute();
		} finally {
			if( pstmt != null ) pstmt.close();
		}
	}

	public boolean raiseError( Map<String, Object> map ) {
		ValidableFieldSet validableFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( false,	"message",			"DPR_ORDER_MESSAGE",				Schema.STRING )
			, new ValidableField( false,	"status",			"DPR_ORDER_STATUS",					Schema.STRING )
			, new ValidableField( false,	"updateUserId",		"UPGUSERID",						Schema.STRING )
			, new ValidableField( false,	"orderKey",			"DPR_ORDER_KEY",					Schema.STRING )
		} );

		PreparedStatement pstmt = null;
		try {
			map.put( "status", STATUS_ERROR );

			pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_ORDER SET MESSAGE = ?, STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ? WHERE ORDER_KEY = ?" );
			SQLManager.bindVariables( pstmt, validableFieldSet.validate(map) );
			pstmt.executeUpdate();
		} catch( FieldException fieldEx ) {
			return false;
		} catch( SQLException sqlEx ) {
			return false;
		} finally {
			try {
				if( pstmt != null ) pstmt.close();
				handler.commit();
			} catch( SQLException sqlEx ) {}
		}

		return true;
	}

	/*
	 * CALLING FROM WM: ORDER_IF_CREATION
	 * ORDER_IF_CREATION: UPDATE DPR_ORDER
	 */
	public boolean updateFromProcess( String type, Map<String, Object> headerMap ) throws SQLException, DataException {
		return updateFromProcess( type, headerMap, null );
	}

	/*
	 * CALLING FROM WM: ORDER_IF_SIMULATION, ORDER_IF_CREATION, ORDER_IF_STATUS
	 * ORDER_IF_SIMULATION: UPDATE DPR_ORDER, UPDATE DPR_ORDER_DTL
	 * ORDER_IF_CREATION: UPDATE DPR_ORDER
	 * ORDER_IF_STATUS: CALL updateOrderHeaderFromStatus()
	 */
	public boolean updateFromProcess( String type, Map<String, Object> headerMap, List<Map<String, Object>> detailList ) throws SQLException, DataException {
		Map<String, Object> hMap = new java.util.HashMap<String, Object>( headerMap );
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();
		Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " start." );
		if( com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_SIMULATION.equals(type) ) {
			ValidableFieldSet headerValidableFieldSet = new ValidableFieldSet( new ValidableField[] {
				  new ValidableField( false,	"simulationKey",	"DPR_ORDER_SIMULATIONKEY",			Schema.STRING )
				, new ValidableField( false,	"inDateSimulation",	"DPR_ORDER_INDATESIMULATION",		Schema.DATE )
				, new ValidableField( true,		"orderValue",		"DPR_ORDER_ORDERVALUE",				Schema.DOUBLE )
				, new ValidableField( true,		"orderTax",			"DPR_ORDER_ORDER_TAX",				Schema.DOUBLE )
				, new ValidableField( true,		"orderDiscount",	"DPR_ORDER_ORDERDAMAGEDDISCOUNT",	Schema.DOUBLE )
				, new ValidableField( true,		"orderTotal",		"DPR_ORDER_ORDERTOTAL",				Schema.DOUBLE )
				, new ValidableField( false,	"status",			"DPR_ORDER_STATUS",					Schema.STRING )
				, new ValidableField( false,	"updateUserId",		"UPGUSERID",						Schema.STRING )
				, new ValidableField( false,	"orderKey",			"DPR_ORDER_KEY",					Schema.STRING )
				, new ValidableField( false,	"organizationCode",	"DPR_ORDER_ORGANIZATIONCODE",		Schema.STRING )
				, new ValidableField( false,	"shipPartyCode",	"DPR_ORDER_SHIPPARTYCODE",			Schema.STRING )
				, new ValidableField( false,	"soldPartyCode",	"DPR_ORDER_SOLDPARTYCODE",			Schema.STRING )
			} );

			ValidableFieldSet detailValidableFieldSet1 = new ValidableFieldSet( new ValidableField[] {
				  new ValidableField( true,		"simulationOrderQty",	"DPR_ORDERDTL_SIMULATION_ORDERQTY",		Schema.DOUBLE )
				, new ValidableField( true,		"simulationOrderValue",	"DPR_ORDERDTL_SIMULATION_ORDERVALUE",	Schema.DOUBLE )
				, new ValidableField( true,		"simulationTotalQty",	"DPR_ORDERDTL_SIMULATION_TOTALQTY",		Schema.DOUBLE )
				, new ValidableField( true,		"simulationTotalValue",	"DPR_ORDERDTL_SIMULATION_TOTALVALUE",	Schema.DOUBLE )
				, new ValidableField( true,		"orderQty",			"DPR_ORDERDTL_ORDERQTY",			Schema.DOUBLE )
				, new ValidableField( true,		"orderValue",		"DPR_ORDERDTL_ORDERVALUE",			Schema.DOUBLE )
				, new ValidableField( true,		"inputTotalQty",	"DPR_ORDERDTL_INPUT_TQTY",			Schema.DOUBLE )
				, new ValidableField( true,		"inputTotalValue",	"DPR_ORDERDTL_INPUT_TVALUE",		Schema.DOUBLE )
				, new ValidableField( true,		"priceCurrency",	"DPR_ORDERDTL_PRICECURRENCY",		Schema.STRING )
				, new ValidableField( true,		"price",			"DPR_ORDERDTL_PRICE",				Schema.DOUBLE )
				, new ValidableField( true,		"uom",				"DPR_ORDERDTL_UOM",					Schema.STRING )
				, new ValidableField( true,		"simulationUOM",	"DPR_ORDERDTL_UOM",					Schema.STRING )
				, new ValidableField( false,	"itemCodeConfirmed", "DPR_ORDERDTL_ITEMCODE_CONFIRMED",	Schema.STRING )
				, new ValidableField( false,	"itemRefInd",		"DPR_ORDERDTL_ITEMREF_IND",			Schema.STRING )
				, new ValidableField( false,	"childLineNumber",	"DPR_ORDERDTL_CHILD_LINENUMBER",	Schema.STRING )
				, new ValidableField( true,		"updateUserId",		"UPGUSERID",						Schema.STRING )
				, new ValidableField( false,	"orderKey",			"DPR_ORDERDTL_ORDERKEY",			Schema.STRING )
				, new ValidableField( false,	"lineNumber",		"DPR_ORDERDTL_LINENUMBER",			Schema.STRING )
				, new ValidableField( false,	"itemCode",			"DPR_ORDERDTL_ITEMCODE",			Schema.STRING )
			} );

			/*
			 * if( freegoodsSimInd ) {
				detailValidableFieldSet1 = new ValidableFieldSet( new ValidableField[] {
					  new ValidableField( true,		"simulationOrderQty",	"DPR_ORDERDTL_SIMULATION_ORDERQTY",	Schema.DOUBLE )
					, new ValidableField( true,		"simulationOrderValue",	"DPR_ORDERDTL_SIMULATION_ORDERVALUE",	Schema.DOUBLE )
					, new ValidableField( true,		"orderQty",			"DPR_ORDERDTL_ORDERQTY",			Schema.DOUBLE )
					, new ValidableField( true,		"orderValue",		"DPR_ORDERDTL_ORDERVALUE",			Schema.DOUBLE )
					, new ValidableField( true,		"priceCurrency",	"DPR_ORDERDTL_PRICECURRENCY",		Schema.STRING )
					, new ValidableField( true,		"price",			"DPR_ORDERDTL_PRICE",				Schema.DOUBLE )
					, new ValidableField( true,		"uom",				"DPR_ORDERDTL_UOM",					Schema.STRING )
					, new ValidableField( true,		"simulationUOM",	"DPR_ORDERDTL_UOM",					Schema.STRING )
					, new ValidableField( false,	"itemCodeConfirmed", "DPR_ORDERDTL_ITEMCODE_CONFIRMED",	Schema.STRING )
					, new ValidableField( false,	"itemRefInd",		"DPR_ORDERDTL_ITEMREF_IND",			Schema.STRING )
					, new ValidableField( false,	"childLineNumber",	"DPR_ORDERDTL_CHILD_LINENUMBER",	Schema.STRING )
					, new ValidableField( true,		"updateUserId",		"UPGUSERID",						Schema.STRING )
					, new ValidableField( false,	"orderKey",			"DPR_ORDERDTL_ORDERKEY",			Schema.STRING )
					, new ValidableField( false,	"lineNumber",		"DPR_ORDERDTL_LINENUMBER",			Schema.STRING )
					, new ValidableField( false,	"itemCode",			"DPR_ORDERDTL_ITEMCODE",			Schema.STRING )
				} );
			} else {
				detailValidableFieldSet1 = new ValidableFieldSet( new ValidableField[] {
						  new ValidableField( true,		"simulationTotalQty",	"DPR_ORDERDTL_SIMULATION_TOTALQTY",		Schema.DOUBLE )
						, new ValidableField( true,		"simulationTotalValue",	"DPR_ORDERDTL_SIMULATION_TOTALVALUE",	Schema.DOUBLE )
						, new ValidableField( true,		"inputTotalQty",	"DPR_ORDERDTL_INPUT_TQTY",			Schema.DOUBLE )
						, new ValidableField( true,		"inputTotalValue",	"DPR_ORDERDTL_INPUT_TVALUE",		Schema.DOUBLE )
						, new ValidableField( true,		"freegoodsSimInd",	"DPR_ORDERDTL_FREEGOODS_SIM_IND",	Schema.STRING )
						, new ValidableField( true,		"priceCurrency",	"DPR_ORDERDTL_PRICECURRENCY",		Schema.STRING )
						, new ValidableField( true,		"price",			"DPR_ORDERDTL_PRICE",				Schema.DOUBLE )
						, new ValidableField( true,		"uom",				"DPR_ORDERDTL_UOM",					Schema.STRING )
						, new ValidableField( true,		"simulationUOM",	"DPR_ORDERDTL_UOM",					Schema.STRING )
						, new ValidableField( false,	"itemCodeConfirmed", "DPR_ORDERDTL_ITEMCODE_CONFIRMED",	Schema.STRING )
						, new ValidableField( false,	"itemRefInd",		"DPR_ORDERDTL_ITEMREF_IND",			Schema.STRING )
						, new ValidableField( false,	"childLineNumber",	"DPR_ORDERDTL_CHILD_LINENUMBER",	Schema.STRING )
						, new ValidableField( true,		"updateUserId",		"UPGUSERID",						Schema.STRING )
						, new ValidableField( false,	"orderKey",			"DPR_ORDERDTL_ORDERKEY",			Schema.STRING )
						, new ValidableField( false,	"lineNumber",		"DPR_ORDERDTL_LINENUMBER",			Schema.STRING )
						, new ValidableField( false,	"itemCode",			"DPR_ORDERDTL_ITEMCODE",			Schema.STRING )
					} );
			}
*/
			PreparedStatement header_pstmt = null, detail_pstmt = null;
			try {
				header_pstmt = handler.getConnection().prepareStatement(
					"UPDATE DPR_ORDER SET SIMULATIONKEY = ?, INDATE_SIM = ? "
						+", ORDERVALUE = ?, ORDERTAX = ?, ORDERDISCOUNT = ?, ORDERTOTAL = ?"
						+", STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ?"
					+" WHERE ORDER_KEY = ? AND ORGANIZATIONCD = ? AND SHIP_PARTYCD = ? AND SOLD_PARTYCD = ? AND STATUS = 'SG'" );

				/*	String detailSQL;
				if( !freegoodsSimInd ) {
					detailSQL = "UPDATE DPR_ORDER_DTL SET SIMULATION_TOTAL_QTY = ?, SIMULATION_TOTAL_VALUE = ?"
							+", INPUT_TOTAL_QTY = ?, INPUT_TOTAL_VALUE = ?, FREEGOODS_SIM_IND = ?"
							+", PRICE_CURR = NVL(?, PRICE_CURR), PRICE = NVL(?, PRICE)"
							+", UOM = NVL(?, UOM), SIMULATION_UOM = NVL(?, SIMULATION_UOM), ITEMCD_CNF = NVL(?, ITEMCD_CNF)"
							+", ITEMREF_IND = NVL(?, ITEMREF_IND), CHILD_LINE_NO = NVL(?, CHILD_LINE_NO)"
							+", UPGDATE = SYSDATE, UPGUSERID = NVL(?, UPGUSERID)"
						+" WHERE ORDERKEY = ? AND LINE_NO = ? AND ITEMCD = ?";
				} else {
					detailSQL = "UPDATE DPR_ORDER_DTL SET SIMULATION_ORDERQTY = ?, SIMULATION_ORDERVALUE = ?"
							+", ORDERQTY = ?, ORDERVALUE = ?"
							+", PRICE_CURR = NVL(?, PRICE_CURR), PRICE = NVL(?, PRICE)"
							+", UOM = NVL(?, UOM), SIMULATION_UOM = NVL(?, SIMULATION_UOM), ITEMCD_CNF = NVL(?, ITEMCD_CNF)"
							+", ITEMREF_IND = NVL(?, ITEMREF_IND), CHILD_LINE_NO = NVL(?, CHILD_LINE_NO)"
							+", UPGDATE = SYSDATE, UPGUSERID = NVL(?, UPGUSERID)"
						+" WHERE ORDERKEY = ? AND LINE_NO = ? AND ITEMCD = ?";
				}
*/


				detail_pstmt = handler.getConnection().prepareStatement(
					"UPDATE DPR_ORDER_DTL SET SIMULATION_ORDERQTY = ?, SIMULATION_ORDERVALUE = ?, SIMULATION_TOTAL_QTY = ?, SIMULATION_TOTAL_VALUE = ?"
						+", ORDERQTY = ?, ORDERVALUE = ?, INPUT_TOTAL_QTY = ?, INPUT_TOTAL_VALUE = ?"
						+", PRICE_CURR = NVL(?, PRICE_CURR), PRICE = NVL(?, PRICE)"
						+", UOM = NVL(?, UOM), SIMULATION_UOM = NVL(?, SIMULATION_UOM), ITEMCD_CNF = NVL(?, ITEMCD_CNF)"
						+", ITEMREF_IND = NVL(?, ITEMREF_IND), CHILD_LINE_NO = NVL(?, CHILD_LINE_NO)"
						+", UPGDATE = SYSDATE, UPGUSERID = NVL(?, UPGUSERID)"
					+" WHERE ORDERKEY = ? AND LINE_NO = ? AND ITEMCD = ?"
				);

				String dcQuery = "UPDATE DPR_ORDER_DTL SET ITEMREF_IND = 'DC' WHERE ORDERKEY = ? AND CHILD_LINE_NO != 0";
				int childResetCount = SQLManager.executeStatement( handler, dcQuery, new Object[] { hMap.get("orderKey") } );

				hMap.put( "status", STATUS_SIMULATED );
				SQLManager.bindVariables( header_pstmt, headerValidableFieldSet.validate(hMap) );

				if( header_pstmt.executeUpdate() != 1 ) {
					handler.rollback();
					hMap.put( "message", msghandler.getMessage("ERR_CANNOT_UPDATE_SIMULATIONINFO") );

					Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " return raiseError( hMap );" );
					return raiseError( hMap );
				}

				if( detailList != null && detailList.size() > 0 ) {
					DataResult result = new DataResult();
					Table detailTable = Schema.findTable( Schema.DPR_ORDER_DTL );
					for( Map<String, Object> obj : detailList ) {
						Map<String, Object> map = obj;
						map.put( "orderKey", hMap.get("orderKey") );
						map.put( "detailStatus", "00" );
						map.put( "status", "00" );
						// if CHILD_LINE_NUMBER != 0 then the item is pipo child( no need orderinput qty )
						if( OrderDetail.CHILD_LINENUMBER_NORMAL != (Integer)map.get("childLineNumber") ) {
							map.put( "orderQty", "" );
							map.put( "orderValue", "" );
						// if ITEMREF_IND = OG then set simulation qty as 0( no need simulation qty )
						} else if( OrderDetail.ITEMREF_PIPO_ORIGINAL.equals(map.get("itemRefInd"))) {
							map.put( "simulationUOM", "" );
							map.put( "simulationOrderQty", "" );
							map.put( "simulationOrderValue", "" );
						}
						Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), map );

						try {
							SQLManager.bindVariables( detail_pstmt, detailValidableFieldSet1.validate(map) );
							int upCount = detail_pstmt.executeUpdate();
							if( upCount > 0 )
								result.increaseModifyCount();
							else if( upCount == 0 ) {
								if( SQLManager.manageRecord(handler, detailTable, map, Record.INSERT) )
									result.increaseRegistCount();
								else
									result.increaseErrorCount();
							}
						} catch( FieldException fieldEx ) {
							result.increaseErrorCount();
							result.appendError( handler.createDataException(fieldEx.getMessage()) );
						} catch( DataException dataEx ) {
							result.increaseErrorCount();
							result.appendError( handler.createDataException(dataEx.getMessage()) );
						} catch( SQLException sqlEx ) {
							result.increaseErrorCount();
							throw sqlEx;
						}
					}

					int childDeleteCount = 0;
					if( childResetCount > 0 ) {
						childDeleteCount = SQLManager.executeStatement( handler, "DELETE FROM DPR_ORDER_DTL WHERE ORDERKEY = ? AND ITEMREF_IND = ?"
								, new Object[] { headerMap.get("orderKey"), OrderDetail.ITEMREF_PIPO_CHILD_DELETED } );
						Loggers.business.debug( "{}: {}", headerMap.get("orderKey")
							, type + ", CHILD RESET: " + childResetCount + ", CHILD DELETE: " + childDeleteCount );
					}
					Loggers.business.debug( "{}: {}", headerMap.get("orderKey")
							, type + " count: ALL:" + result.getCount() + ", UPG:" + result.getModifyCount()
							+ ", REG:" + result.getRegistCount() + ", ERR:"+ result.getErrorCount() );
				} else {
					handler.rollback();
					hMap.put( "message", msghandler.getMessage("ERR_HAVENOT_DETAILLIST") );

					Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " return raiseError( hMap );" );
					return raiseError( hMap );
				}
			} catch( FieldException fieldEx ) {
				handler.rollback();
				hMap.put( "message", msghandler.getMessage(fieldEx.getErrorKey()) +"(Field: "+ fieldEx.getErrorField() +", Value: "+ fieldEx.getErrorFieldValue() +")" );
				logger.error( fieldEx.getErrorField().getFieldKey() +":"+ fieldEx.getErrorKey() );
				raiseError( hMap );

				Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " throw handler.createDataException( fieldEx, null ); " );
				throw handler.createDataException( fieldEx, null );
			} catch( SQLException sqlEx ) {
				handler.rollback();
				hMap.put( "message", sqlEx.getMessage() );
				logger.error( sqlEx );
				raiseError( hMap );

				Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " return false;" );
				return false;
			} finally {
				if( header_pstmt != null ) header_pstmt.close();
				if( detail_pstmt != null ) detail_pstmt.close();
			}

			Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " return true;" );
			return true;
		} else if( com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_CREATION.equals(type) ) {
			ValidableFieldSet headerValidableFieldSet = new ValidableFieldSet( new ValidableField[] {
				  new ValidableField( true,		"orderNumber",		"DPR_ORDER_ORDERNUMBER",			Schema.STRING )
				, new ValidableField( true,		"orderVolume",		"DPR_ORDER_ORDERVOLUME",			Schema.DOUBLE )
				, new ValidableField( true,		"orderWeight",		"DPR_ORDER_ORDERWEIGHT",			Schema.DOUBLE )
				, new ValidableField( true,		"confirmedOrderValue",	"DPR_ORDER_CONFIRMED_ORDERVALUE",	Schema.DOUBLE )
				, new ValidableField( true,		"confirmedOrderTax",	"DPR_ORDER_CONFIRMED_ORDERTAX",	Schema.DOUBLE )
				, new ValidableField( true,		"confirmedOrderDiscount",	"DPR_ORDERDTL_CONFIRMED_ORDERDISCOUNT",	Schema.DOUBLE )
				, new ValidableField( true,		"confirmedOrderTotal",	"DPR_ORDER_CONFIRMED_ORDERTOTAL",	Schema.DOUBLE )
				, new ValidableField( true,		"creditStatus",		"DPR_ORDER_CREDIT_STATUS",			Schema.STRING )
				, new ValidableField( true,		"status",			"DPR_ORDER_STATUS",					Schema.STRING )
				, new ValidableField( true,		"updateUserId",		"UPDATEUSERID",						Schema.STRING )
				, new ValidableField( true,		"createUserId",		"CREATEUSERID",						Schema.STRING )
				, new ValidableField( false,	"orderKey",			"DPR_ORDERDTL_ORDERKEY",			Schema.STRING )
			} );

			PreparedStatement pstmt = null;
			try {
				pstmt = handler.getConnection().prepareStatement(
					"UPDATE DPR_ORDER SET ORDER_NUMBER = ?, ORDER_VOLUME = ?, ORDER_WEIGHT = ?"
						+ ", CONFIRMED_ORDERVALUE = ?, CONFIRMED_ORDERTAX = ?"
						+ ", CONFIRMED_ORDERDISCOUNT = ?, CONFIRMED_ORDERTOTAL = ?"
						+ ", CREDIT_STATUS = ?, STATUS = ?"
						+ ", UPGDATE = SYSDATE, UPGUSERID = ?, CREUSERID = ?"
						+ " WHERE ORDER_KEY = ?"
				);

				headerMap.put( "createUserId", headerMap.get("updateUserId") );
				SQLManager.bindVariables( pstmt, headerValidableFieldSet.validate(headerMap) );
				pstmt.executeUpdate();
			} catch( FieldException fieldEx ) {
				logger.error( fieldEx.getErrorField().getFieldKey() );

				Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " throw handler.createDataException( fieldEx.getMessage() );" );
				throw handler.createDataException( fieldEx.getMessage() );
			}

			Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " return true;" );
			return true;
		} else if( com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_STATUS.equals(type) ) {
			Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " return updateOrderHeaderFromStatus( headerMap, type );" );
			return updateOrderHeaderFromStatus( headerMap, type );
		}

		Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " return false;" );
		return false;
	}

	/*
	 * CALLING FROM WM: ORDER_IF_STATUS
	 * ORDER_IF_STATUS: UPDATE DPR_ORDER_DTL
	 */
	public DataResult updateOrderDetailFromStatusByLineNumber( List<Map<String, Object>> detailList, String orderKey ) throws SQLException, DataException {
		Loggers.business.debug( "{}: {}", orderKey, "start." );
		Table detailTable = Schema.findTable( Schema.DPR_ORDER_DTL );

		ValidableFieldSet detailValidableFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( true,	"orderNumber",			"DPR_ORDER_ORDERNUMBER",			Schema.STRING )
			, new ValidableField( false,	"itemCodeConfirmed",	"DPR_ORDERDTL_ITEMCODE_CONFIRMED",	Schema.STRING )
			, new ValidableField( false,	"itemRefInd",			"DPR_ORDERDTL_ITEMREF_IND",			Schema.STRING )
			, new ValidableField( false,	"childLineNumber",		"DPR_ORDERDTL_CHILD_LINENUMBER",	Schema.INTEGER )
			, new ValidableField( true,	"orderQty",				"DPR_ORDERDTL_ORDERQTY",			Schema.DOUBLE )
			, new ValidableField( true,	"orderValue",			"DPR_ORDERDTL_ORDERVALUE",			Schema.DOUBLE )
			, new ValidableField( true,	"orderTax",				"DPR_ORDERDTL_ORDERTAX",			Schema.DOUBLE )
			, new ValidableField( true,	"confirmedOrderQty",	"DPR_ORDERDTL_CONFIRMED_ORDERQTY",	Schema.DOUBLE )
			, new ValidableField( true,	"confirmedOrderValue",	"DPR_ORDERDTL_CONFIRMED_ORDERVALUE",	Schema.DOUBLE )
			, new ValidableField( true,	"priceCurrency",		"DPR_ORDERDTL_PRICECURRENCY",		Schema.STRING )
			, new ValidableField( true,	"price",				"DPR_ORDERDTL_PRICE",				Schema.DOUBLE )
			, new ValidableField( true,	"consumerEANCode",		"DPR_ORDERDTL_CONSUMEREAN",			Schema.STRING )
			, new ValidableField( false,	"itemCode",				"DPR_ORDERDTL_ITEMCODE",			Schema.STRING )
			, new ValidableField( false,	"orderKey",				"DPR_ORDERDTL_ORDERKEY",			Schema.STRING )
			, new ValidableField( false,	"lineNumber",			"DPR_ORDERDTL_LINENUMBER",			Schema.STRING )
		} );

		PreparedStatement pstmt = null;
		DataResult result = new DataResult();
		try {
			Map<String, Object> itemEANPair = new HashMap<String, Object>();
			boolean enableSoldEanCode = RBMSystem.getSystemEnvBool( "DPR", "Order;EnableSoldEanCode", true );
			if( enableSoldEanCode ) {
				itemEANPair = getItemEANListOfPair( handler, detailList, null, true );
			}

			String systemDateTime = (String)SQLManager.getObjectValue( handler,
					"SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM DPR_ORDER WHERE ORDER_KEY = ?", new Object[] {orderKey} );

			pstmt = handler.getConnection().prepareStatement(
					"UPDATE DPR_ORDER_DTL SET ORDER_NUMBER = ?, ITEMCD_CNF = NVL(?, ITEMCD_CNF), ITEMREF_IND = NVL(?, ITEMREF_IND)"
							+ ", CHILD_LINE_NO = NVL(?, CHILD_LINE_NO)"
							+ ", ORDERQTY = DECODE(CHILD_LINE_NO, 0, ?, NULL), ORDERVALUE = DECODE(CHILD_LINE_NO, 0, ?, NULL), ORDERTAX = ?"
							+ ", CONFIRMED_ORDERQTY = DECODE(ITEMREF_IND, 'OG', NULL, ?), CONFIRMED_ORDERVALUE = DECODE(ITEMREF_IND, 'OG', NULL, ?)"
							+ ", PRICE_CURR = NVL(?, PRICE_CURR), PRICE = NVL(?, PRICE)"
							+ ", CONS_EAN = NVL2(CONS_EAN, CONS_EAN, ?), UPGDATE = SYSDATE"
							+ ", ITEMCD = NVL(?, ITEMCD)"
							+ " WHERE ORDERKEY = ? AND LINE_NO = ?" );

			String dcQuery = "UPDATE DPR_ORDER_DTL SET ITEMREF_IND = '" + OrderDetail.ITEMREF_PIPO_CHILD_DELETED + "'"
					+ " WHERE ORDERKEY = ? AND CHILD_LINE_NO != 0";
			int childResetCount = SQLManager.executeStatement( handler, dcQuery, new Object[] { orderKey } );

			for( Map<String, Object> map : detailList ) {
				try {
					if( enableSoldEanCode ) {
						String itemConsumerEANCode = (String)itemEANPair.get( map.get( "itemCodeConfirmed" ) );
						map.put( "consumerEANCode", itemConsumerEANCode );
					}
					SQLManager.bindVariables( pstmt, detailValidableFieldSet.validate(map) );

					int count = pstmt.executeUpdate();
					if( count > 0 ) {
						result.increaseModifyCount();
					} else if( count == 0 ) {
						// if new non pipo record. 'NO' OR  'RP' OR  NEW LINE
						Map<String, Object> _map = new java.util.HashMap<String, Object> ( map );

						if( OrderDetail.CHILD_LINENUMBER_NORMAL != (Integer)_map.get("childLineNumber") ) {
							_map.put( "orderQty", "" );
							_map.put( "orderValue", "" );
							_map.put( "uom", "" );
							_map.put( "status", "00" );
						} else if( OrderDetail.ITEMREF_PIPO_ORIGINAL.equals(_map.get("itemRefInd"))) {
							_map.put( "simulationUOM", "" );
							_map.put( "simulationOrderQty", "" );
							_map.put( "simulationOrderValue", "" );
							_map.put( "status", "00" );
						} else if( OrderDetail.CHILD_LINENUMBER_NORMAL == (Integer)_map.get("childLineNumber") ){
							_map.put( "status", "SP" );
						}
						if( SQLManager.manageRecord( handler, detailTable, _map, Record.INSERT) )
							result.increaseRegistCount();
						else
							result.increaseErrorCount();
					}
				} catch( FieldException fieldEx ) {
					result.appendError( handler.createDataException(fieldEx.getErrorField().getFieldKey()+":"+fieldEx.getErrorFieldValue(), fieldEx.getMessage()+" : "+ fieldEx.getErrorField().getFieldKey(), map) );
				} catch( DataException dataEx ) {
					result.appendError( handler.createDataException(dataEx.getErrorKey(), dataEx.getMessage(), map) );
				} catch( SQLException sqlEx ) {
					throw sqlEx;
				}
			}

			int delCount = 0;
			if( systemDateTime != null ) {
				String query =
					"UPDATE DPR_ORDER_DTL SET STATUS = 'DE', CONFIRMED_ORDERQTY = 0, CONFIRMED_ORDERVALUE = 0"
						+ ", CONFIRMED_ORDERTAX = 0, CONFIRMED_ORDERDISCOUNT = 0, CONFIRMED_ORDERTOTAL = 0"
					+ " WHERE ORDERKEY = ? AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";

				delCount = SQLManager.executeStatement( handler, query, new Object[] { orderKey, systemDateTime } );
			}

			int childDeleteCount = 0;
			if( childResetCount > 0 ) {
			childDeleteCount = SQLManager.executeStatement( handler, "DELETE FROM DPR_ORDER_DTL WHERE ORDERKEY = ? AND ITEMREF_IND = ?"
					, new Object[] { orderKey, OrderDetail.ITEMREF_PIPO_CHILD_DELETED } );
			Loggers.business.debug( "{}: {}", orderKey, " CHILD RESET: " + childResetCount + ", CHILD DELETE: " + childDeleteCount );
			}

			Loggers.business.debug( "{}: {}", orderKey, " UPG: "+ result.getModifyCount() + " ERR: "+ result.getErrorCount()
					+ " SP INS: " + result.getRegistCount() + " DE UPG: " + delCount + " - " + systemDateTime );
			return result;
		} finally {
			try { if( pstmt != null ) pstmt.close(); } catch( Exception ex ) {}
			Loggers.business.debug( "{}: {}", orderKey, "end." );
		}
	}

	/*
	 * CALLING FROM WM: ORDER_IF_STATUS
	 * ORDER_IF_STATUS: UPDATE DPR_ORDER_DTL
	 */
	public DataResult updateOrderDetailFromStatus( List<Map<String, Object>> detailList, String orderKey ) throws SQLException, DataException {
		if( RBMSystem.getSystemEnvBool("DPR", "Order;EnableDtlUpdByLn", false) )
			return updateOrderDetailFromStatusByLineNumber(detailList, orderKey);

		Loggers.business.debug( "{}: {}", orderKey, "start." );
		Table detailTable = Schema.findTable( Schema.DPR_ORDER_DTL );

		ValidableFieldSet detailValidableFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( true,	"orderNumber",			"DPR_ORDER_ORDERNUMBER",			Schema.STRING )
			, new ValidableField( false,	"itemCodeConfirmed",	"DPR_ORDERDTL_ITEMCODE_CONFIRMED",	Schema.STRING )
			, new ValidableField( false,	"itemRefInd",			"DPR_ORDERDTL_ITEMREF_IND",			Schema.STRING )
			, new ValidableField( false,	"childLineNumber",		"DPR_ORDERDTL_CHILD_LINENUMBER",	Schema.INTEGER )
			, new ValidableField( true,	"orderQty",				"DPR_ORDERDTL_ORDERQTY",			Schema.DOUBLE )
			, new ValidableField( true,	"orderValue",			"DPR_ORDERDTL_ORDERVALUE",			Schema.DOUBLE )
			, new ValidableField( true,	"orderTax",				"DPR_ORDERDTL_ORDERTAX",			Schema.DOUBLE )
			, new ValidableField( true,	"confirmedOrderQty",	"DPR_ORDERDTL_CONFIRMED_ORDERQTY",	Schema.DOUBLE )
			, new ValidableField( true,	"confirmedOrderValue",	"DPR_ORDERDTL_CONFIRMED_ORDERVALUE",	Schema.DOUBLE )
			, new ValidableField( true,	"priceCurrency",		"DPR_ORDERDTL_PRICECURRENCY",		Schema.STRING )
			, new ValidableField( true,	"price",				"DPR_ORDERDTL_PRICE",				Schema.DOUBLE )
			, new ValidableField( true,	"consumerEANCode",		"DPR_ORDERDTL_CONSUMEREAN",			Schema.STRING )
			, new ValidableField( false,	"orderKey",				"DPR_ORDERDTL_ORDERKEY",			Schema.STRING )
			, new ValidableField( false,	"lineNumber",			"DPR_ORDERDTL_LINENUMBER",			Schema.STRING )
			, new ValidableField( false,	"itemCode",				"DPR_ORDERDTL_ITEMCODE",			Schema.STRING )
		} );

		PreparedStatement pstmt = null;
		DataResult result = new DataResult();
		try {
			Map<String, Object> itemEANPair = new HashMap<String, Object>();
			boolean enableSoldEanCode = RBMSystem.getSystemEnvBool( "DPR", "Order;EnableSoldEanCode", true );
			if( enableSoldEanCode ) {
				itemEANPair = getItemEANListOfPair( handler, detailList, null, true );
			}

			String systemDateTime = (String)SQLManager.getObjectValue( handler,
					"SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM DPR_ORDER WHERE ORDER_KEY = ?", new Object[] {orderKey} );

			pstmt = handler.getConnection().prepareStatement(
					"UPDATE DPR_ORDER_DTL SET ORDER_NUMBER = ?, ITEMCD_CNF = NVL(?, ITEMCD_CNF), ITEMREF_IND = NVL(?, ITEMREF_IND)"
							+ ", CHILD_LINE_NO = NVL(?, CHILD_LINE_NO)"
							+ ", ORDERQTY = DECODE(CHILD_LINE_NO, 0, ?, NULL), ORDERVALUE = DECODE(CHILD_LINE_NO, 0, ?, NULL), ORDERTAX = ?"
							+ ", CONFIRMED_ORDERQTY = DECODE(ITEMREF_IND, 'OG', NULL, ?), CONFIRMED_ORDERVALUE = DECODE(ITEMREF_IND, 'OG', NULL, ?)"
							+ ", PRICE_CURR = NVL(?, PRICE_CURR), PRICE = NVL(?, PRICE)"
							+ ", CONS_EAN = NVL2(CONS_EAN, CONS_EAN, ?), UPGDATE = SYSDATE"
							+ " WHERE ORDERKEY = ? AND LINE_NO = ? AND ITEMCD = ?" );

			String dcQuery = "UPDATE DPR_ORDER_DTL SET ITEMREF_IND = '" + OrderDetail.ITEMREF_PIPO_CHILD_DELETED + "'"
					+ " WHERE ORDERKEY = ? AND CHILD_LINE_NO != 0";
			int childResetCount = SQLManager.executeStatement( handler, dcQuery, new Object[] { orderKey } );

			for( Map<String, Object> map : detailList ) {
				try {
					if( enableSoldEanCode ) {
						String itemConsumerEANCode = (String)itemEANPair.get( map.get( "itemCodeConfirmed" ) );
						map.put( "consumerEANCode", itemConsumerEANCode );
					}
					SQLManager.bindVariables( pstmt, detailValidableFieldSet.validate(map) );

					int count = pstmt.executeUpdate();
					if( count > 0 ) {
						result.increaseModifyCount();
					} else if( count == 0 ) {
						// if new non pipo record. 'NO' OR  'RP' OR  NEW LINE
						Map<String, Object> _map = new java.util.HashMap<String, Object> ( map );

						if( OrderDetail.CHILD_LINENUMBER_NORMAL != (Integer)_map.get("childLineNumber") ) {
							_map.put( "orderQty", "" );
							_map.put( "orderValue", "" );
							_map.put( "uom", "" );
							_map.put( "status", "00" );
						} else if( OrderDetail.ITEMREF_PIPO_ORIGINAL.equals(_map.get("itemRefInd"))) {
							_map.put( "simulationUOM", "" );
							_map.put( "simulationOrderQty", "" );
							_map.put( "simulationOrderValue", "" );
							_map.put( "status", "00" );
						} else if( OrderDetail.CHILD_LINENUMBER_NORMAL == (Integer)_map.get("childLineNumber") ){
							_map.put( "status", "SP" );
						}
						if( SQLManager.manageRecord( handler, detailTable, _map, Record.INSERT) )
							result.increaseRegistCount();
						else
							result.increaseErrorCount();
					}
				} catch( FieldException fieldEx ) {
					result.appendError( handler.createDataException(fieldEx.getMessage()) );
				} catch( DataException dataEx ) {
					result.appendError( dataEx );
				} catch( SQLException sqlEx ) {
					throw sqlEx;
				}
			}

			int delCount = 0;
			if( systemDateTime != null ) {
				String query =
					"UPDATE DPR_ORDER_DTL SET STATUS = 'DE', CONFIRMED_ORDERQTY = 0, CONFIRMED_ORDERVALUE = 0"
						+ ", CONFIRMED_ORDERTAX = 0, CONFIRMED_ORDERDISCOUNT = 0, CONFIRMED_ORDERTOTAL = 0"
					+ " WHERE ORDERKEY = ? AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";

				delCount = SQLManager.executeStatement( handler, query, new Object[] { orderKey, systemDateTime } );
			}

			int childDeleteCount = 0;
			if( childResetCount > 0 ) {
			childDeleteCount = SQLManager.executeStatement( handler, "DELETE FROM DPR_ORDER_DTL WHERE ORDERKEY = ? AND ITEMREF_IND = ?"
					, new Object[] { orderKey, OrderDetail.ITEMREF_PIPO_CHILD_DELETED } );
			Loggers.business.debug( "{}: {}", orderKey, " CHILD RESET: " + childResetCount + ", CHILD DELETE: " + childDeleteCount );
			}

			Loggers.business.debug( "{}: {}", orderKey, " UPG: "+ result.getModifyCount() + " ERR: "+ result.getErrorCount()
					+ " SP INS: " + result.getRegistCount() + " DE UPG: " + delCount + " - " + systemDateTime );
			return result;
		} finally {
			try { if( pstmt != null ) pstmt.close(); } catch( Exception ex ) {}
			Loggers.business.debug( "{}: {}", orderKey, "end." );
		}
	}

	public DataResult updateOrderHeaderFromStatus( List<Map<String, Object>> headerList, String type ) throws SQLException {
		Loggers.business.debug( "{}: {}", null, type + " start." );
		DataResult result = new DataResult();
		if( !com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_STATUSLIST.equals(type) )
			return result;

		for( Map<String, Object> map : headerList ) {
			try {
				if( !updateOrderHeaderFromStatus(map, type) ) {
					Loggers.business.debug( "{}: {}", null, type + " ### OrderHeader: "+ map );
					result.increaseErrorCount();
				} else
					result.increaseSuccessCount( Record.UPDATE );
			} catch( DataException dataEx ) {
				result.appendError( dataEx );
			} catch( SQLException sqlEx ) {
				throw sqlEx;
			}
		}

		Loggers.business.debug( "{}: {}", null, type + " return result;" );
		return result;
	}

	/*
	 *	WM: ORDER_IF_STATUSLIST, ORDER_IF_STATUS, STATUS FIELD = STATUS_DELETED
	 *	UPDATE DPR_ORDER FROM WM STATUS, STATUSLIST
	 */
	public boolean updateOrderHeaderFromStatus( Map<String, Object> headerMap, String type ) throws SQLException, DataException {
		Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " start." );

		if( headerMap.get("status") == null || headerMap.get("soldPartyCode") == null || headerMap.get("shipPartyCode") == null ) {
			headerMap.put( "lineErrorMessage", "This is a deleted order. " );
		}

		String status = (String)headerMap.get( "status" );
		String lineErrorMessage = (String)headerMap.get( "lineErrorMessage" );
		if( STATUS_DELETED.equals(status) || ( lineErrorMessage != null ) ) {
			Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), "### headerMap:"+ headerMap );
			Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " " + status + " execute update.");
			return (SQLManager.executeStatement( handler
					, "UPDATE DPR_ORDER SET ORDER_STATUS = ?, STATUS = NVL(?, STATUS), MESSAGE = NVL(?, MESSAGE), UPGUSERID = ?, UPGDATE = SYSDATE"
						+ " WHERE ORDER_KEY = ?"
					, Record.extractValues(headerMap, new String[] {"orderStatus", "status", "lineErrorMessage", "updateUserId", "orderKey"})
			) > 0 );
		}

		String orderCreationStatus = STATUS_CREATED;
		ValidableFieldSet headerValidableFieldSet = null;
		String updateQuery = null;
		if( com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_STATUS.equals(type) ) {
			headerValidableFieldSet = new ValidableFieldSet( new ValidableField[] {
				  new ValidableField( false,	"shipPartyCode",	"DPR_ORDER_SHIPPARTYCODE",			Schema.STRING )
				, new ValidableField( true,	"orderVolume",		"DPR_ORDER_ORDERVOLUME",			Schema.DOUBLE )
				, new ValidableField( true,	"orderVolumeUnit",	"DPR_ORDER_ORDERVOLUME_UNIT",		Schema.STRING )
				, new ValidableField( true,	"orderWeight",		"DPR_ORDER_ORDERWEIGHT",			Schema.DOUBLE )
				, new ValidableField( true,	"orderWeightUnit",	"DPR_ORDER_ORDERWEIGHT_UNIT",		Schema.STRING )
				, new ValidableField( true,	"orderValue",		"DPR_ORDER_ORDERVALUE",				Schema.DOUBLE )
				, new ValidableField( true,	"orderTax",			"DPR_ORDER_ORDER_TAX",				Schema.DOUBLE )
				, new ValidableField( true,	"orderDiscount",	"DPR_ORDER_ORDERDISCOUNT",			Schema.DOUBLE )
				, new ValidableField( true,	"orderTotal",		"DPR_ORDER_ORDERTOTAL",				Schema.DOUBLE )
				, new ValidableField( true,	"confirmedOrderValue",	"DPR_ORDER_CONFIRMED_ORDERVALUE",	Schema.DOUBLE )
				, new ValidableField( true,	"confirmedOrderTax",	"DPR_ORDER_CONFIRMED_ORDERTAX",	Schema.DOUBLE )
				, new ValidableField( true,	"confirmedOrderDiscount",	"DPR_ORDERDTL_CONFIRMED_ORDERDISCOUNT",	Schema.DOUBLE )
				, new ValidableField( true,	"confirmedOrderTotal",	"DPR_ORDER_CONFIRMED_ORDERTOTAL",	Schema.DOUBLE )
				, new ValidableField( true,	"updateUserId",		"UPDATEUSERID",						Schema.STRING )
				, new ValidableField( false,	"orderKey",			"DPR_ORDER_KEY",					Schema.STRING )
				, new ValidableField( false,	"orderNumber",		"DPR_ORDER_ORDERNUMBER",			Schema.STRING )
				, new ValidableField( false,	"organizationCode",	"DPR_ORDER_ORGANIZATIONCODE",		Schema.STRING )
				, new ValidableField( false,	"soldPartyCode",	"DPR_ORDER_SOLDPARTYCODE",			Schema.STRING )
			} );

			updateQuery =
				"UPDATE DPR_ORDER SET"
					+ " SHIP_PARTYCD = ?"
					+ ", ORDER_VOLUME = NVL(?, ORDER_VOLUME), ORDER_VOLUME_UNIT = NVL(?, ORDER_VOLUME_UNIT)"
					+ ", ORDER_WEIGHT = NVL(?, ORDER_WEIGHT), ORDER_WEIGHT_UNIT = NVL(?, ORDER_WEIGHT_UNIT)"
					+ ", ORDERVALUE = NVL(?, ORDERVALUE), ORDERTAX = NVL(?, ORDERTAX)"
					+ ", ORDERDISCOUNT = NVL(?, ORDERDISCOUNT), ORDERTOTAL = NVL(?, ORDERTOTAL)"
					+ ", CONFIRMED_ORDERVALUE = NVL(?, CONFIRMED_ORDERVALUE)"
					+ ", CONFIRMED_ORDERTAX = NVL(?, CONFIRMED_ORDERTAX)"
					+ ", CONFIRMED_ORDERDISCOUNT = NVL(?, CONFIRMED_ORDERDISCOUNT)"
					+ ", CONFIRMED_ORDERTOTAL = NVL(?, CONFIRMED_ORDERTOTAL)"
					+ ", UPGDATE = SYSDATE, UPGUSERID = ?"
				+ " WHERE ORDER_KEY = ? AND ORDER_NUMBER = ? AND ORGANIZATIONCD = ?"
					+ " AND SOLD_PARTYCD = ? AND STATUS = '" + orderCreationStatus + "'";

		} else if( com.irt.dpr.tools.OrderCanonicalProcess.ORDER_IF_STATUSLIST.equals(type) ) {
			headerValidableFieldSet = new ValidableFieldSet( new ValidableField[] {
				  new ValidableField( false,	"shipPartyCode",	"DPR_ORDER_SHIPPARTYCODE",			Schema.STRING )
				, new ValidableField( true,		"inDateConfirm",	"DPR_ORDER_CONFIRMINDATE",			ValidableField.TYPE_DATE )
				, new ValidableField( true,		"deliveryNumber",	"DPR_ORDER_ORDER_NUMBER",			Schema.STRING )
				, new ValidableField( true,		"goodsIssueDate",	"DPR_ORDER_GOODSISSUE_DATE",			ValidableField.TYPE_DATE )
				, new ValidableField( true,		"creditReleaseDate",	"DPR_ORDER_CREDITRELEASE_DATE",			ValidableField.TYPE_DATE )
				, new ValidableField( true,		"orderVolume",		"DPR_ORDER_ORDERVOLUME",			Schema.DOUBLE )
				, new ValidableField( true,		"orderVolumeUnit",	"DPR_ORDER_ORDERVOLUME_UNIT",		Schema.STRING )
				, new ValidableField( true,		"orderWeight",		"DPR_ORDER_ORDERWEIGHT",			Schema.DOUBLE )
				, new ValidableField( true,		"orderWeightUnit",	"DPR_ORDER_ORDERWEIGHT_UNIT",		Schema.STRING )
				, new ValidableField( true,		"orderValue",		"DPR_ORDER_ORDERVALUE",				Schema.DOUBLE )
				, new ValidableField( true,		"orderStatus",		"DPR_ORDER_ORDERSTATUS",			Schema.STRING )
				, new ValidableField( true,		"updateUserId",		"UPDATEUSERID",						Schema.STRING )
				, new ValidableField( false,	"orderKey",			"DPR_ORDER_KEY",					Schema.STRING )
				, new ValidableField( false,	"orderNumber",		"DPR_ORDER_ORDERNUMBER",			Schema.STRING )
				, new ValidableField( false,	"organizationCode",	"DPR_ORDER_ORGANIZATIONCODE",		Schema.STRING )
				, new ValidableField( false,	"soldPartyCode",	"DPR_ORDER_SOLDPARTYCODE",			Schema.STRING )
			} );

			String orderStatus = (String)headerMap.get( "orderStatus" );
			String orderStatusQuery = "NVL( " + ( orderStatus != null ? "'" + orderStatus + "'" : "NULL" ) + ", ORDER_STATUS)";
			updateQuery =
				"UPDATE DPR_ORDER SET"
					+ " SHIP_PARTYCD = ?"
					+ ", INDATE_CNF = NVL(?, INDATE_CNF)"
					+ ", DELIVERY_NUMBER = NVL(?, DELIVERY_NUMBER)"
					+ ", GOODS_ISSUE_DATE = NVL(?, GOODS_ISSUE_DATE), CREDIT_RELEASE_DATE = NVL(?, CREDIT_RELEASE_DATE)"
					+ ", ORDER_VOLUME = NVL(?, ORDER_VOLUME), ORDER_VOLUME_UNIT = NVL(?, ORDER_VOLUME_UNIT)"
					+ ", ORDER_WEIGHT = NVL(?, ORDER_WEIGHT), ORDER_WEIGHT_UNIT = NVL(?, ORDER_WEIGHT_UNIT)"
					+ ", ORDERVALUE = NVL(?, ORDERVALUE)"
					+ ", ORDER_STATUS = DECODE(FREEGOODS_ORDER_IND, 'Y', DECODE(?, 'CO', 'HO', " + orderStatusQuery + "), "+ orderStatusQuery +")"
					+ ", UPGDATE = SYSDATE, UPGUSERID = ?"
				+ " WHERE ORDER_KEY = ? AND ORDER_NUMBER = ? AND ORGANIZATIONCD = ?"
					+ " AND SOLD_PARTYCD = ? AND STATUS = '" + orderCreationStatus + "'";
		} else {
			Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " return false;" );
			return false;
		}

		PreparedStatement pstmt = null;
		try {
			pstmt = handler.getConnection().prepareStatement( updateQuery );
			SQLManager.bindVariables( pstmt, headerValidableFieldSet.validate(headerMap) );

			int ret = pstmt.executeUpdate();
			if( ret < 1 )
				Loggers.business.debug( "{}: {}", headerMap.get("orderKey")
						, "###headerMap:"+ headerMap );
			Loggers.business.debug( "{}: {}", headerMap.get("orderKey")
					, type + " (" + headerMap.get("orderNumber") + ") " + ret + " rows updated.");

			return ( ret > 0 );
		} catch( FieldException fieldEx ) {
			logger.error( fieldEx.getErrorField().getFieldKey() );

			Loggers.business.debug( "{}: {}", headerMap.get("orderKey")
					, type + " throw handler.createDataException( fieldEx.getMessage() );" );
			Loggers.business.debug( "{}: {}", headerMap.get("orderKey")
					, "### OrderHeader: "+ headerMap );
			throw handler.createDataException( fieldEx.getMessage() );
		} finally {
			try { if( pstmt != null ) pstmt.close(); } catch( Exception ex ) {}
			Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), type + " end.");
		}
	}

	public boolean updateOrderStatus( SQLHandler this_handler, String orderKey, String status, String updateUserId ) throws SQLException {
		Loggers.business.debug( "{}: {}", orderKey, status + " " + updateUserId + " start." );

		PreparedStatement pstmt = null;
		try {
			pstmt = this_handler.getConnection().prepareStatement(
				"UPDATE DPR_ORDER SET ORDER_STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ? WHERE ORDER_KEY = ?" );

			pstmt.setString( 1, status );
			pstmt.setString( 2, updateUserId );
			pstmt.setString( 3, orderKey );

			return pstmt.executeUpdate() == 1;
		} finally {
			if( pstmt != null ) pstmt.close();
			Loggers.business.debug( "{}: {}", orderKey, status + " " + updateUserId + " end." );
		}
	}

	/*
	 * CALL FROM: OrderCanonicalProcess WM ORDER_IF_STATUS
	 * UPDATE DPR_ORDER_DTL STATUS
	 */
	public boolean updateStatusWithDetailOnly( String orderKey, String status, String updateUserId ) throws SQLException {
		Loggers.business.debug( "{}: {}", orderKey, status + " start.");

		PreparedStatement pstmt_dtl = null;
		try {
			if( OrderDetail.STATUS_SAP_DELETED.equals(status) ) {
				pstmt_dtl = handler.getConnection().prepareStatement(
						"UPDATE DPR_ORDER_DTL SET SIMULATION_ORDERQTY = null, SIMULATION_ORDERVALUE = null, ORDERVALUE = null"
						+ ", CONFIRMED_ORDERQTY = null, CONFIRMED_ORDERVALUE = null"
						+ ", PRICE_CURR = null, PRICE = null, STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ? WHERE ORDERKEY = ?" );
			} else {
				pstmt_dtl = handler.getConnection().prepareStatement(
				"UPDATE DPR_ORDER_DTL SET STATUS = NVL(?, STATUS), UPGDATE = SYSDATE, UPGUSERID = ? WHERE ORDERKEY = ?" );
			}

			pstmt_dtl.setString( 1, status );
			pstmt_dtl.setString( 2, updateUserId );
			pstmt_dtl.setString( 3, orderKey );

			Loggers.business.debug( "{}: {}", orderKey, status + " return pstmt_dtl.executeUpdate() > 0;" );
			return pstmt_dtl.executeUpdate() > 0;
		} finally {
			if( pstmt_dtl != null ) pstmt_dtl.close();
			Loggers.business.debug( "{}: {}", orderKey, status + " end." );
		}
	}

	/*
	 * CALL FROM: DPRPlaceOrder
	 * if STATUS = SIMULATED
	 * CHANGE DPR_ORDER_DTL ROW, DPR_ORDER TOTALS REMOVED
	 */
	public boolean updateStatusWithDetail( String orderKey, String status, String updateUserId ) throws SQLException {
		Loggers.business.debug( "{}: {}", orderKey, status + " start.");

		PreparedStatement pstmt = null;
		PreparedStatement pstmt_dtl = null;
		try {
			pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_ORDER SET INDATE_SIM = null, ORDERVALUE = null, ORDERTAX = null, ORDERDISCOUNT = null, ORDERTOTAL = null"
					+ ", STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ? WHERE ORDER_KEY = ?" );

			pstmt_dtl = handler.getConnection().prepareStatement(
				"UPDATE DPR_ORDER_DTL SET SIMULATION_ORDERQTY = null, SIMULATION_ORDERVALUE = null, ORDERVALUE = null"
					+ ", PRICE_CURR = null, PRICE = null, STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ? WHERE ORDERKEY = ?" );

			pstmt.setString( 1, status );
			pstmt.setString( 2, updateUserId );
			pstmt.setString( 3, orderKey );

			if( pstmt.executeUpdate() > 0 ) {
				pstmt_dtl.setString( 1, status );
				pstmt_dtl.setString( 2, updateUserId );
				pstmt_dtl.setString( 3, orderKey );

				Loggers.business.debug( "{}: {}", orderKey, status + " return pstmt_dtl.executeUpdate() > 0;" );
				return pstmt_dtl.executeUpdate() > 0;
			}

			Loggers.business.debug( "{}: {}", orderKey, status + " return false;" );
			return false;
		} finally {
			if( pstmt != null ) pstmt.close();
			if( pstmt_dtl != null ) pstmt_dtl.close();
			Loggers.business.debug( "{}: {}", orderKey, status + " end." );
		}
	}

	public boolean updateStatus( String orderKey, String status, String updateUserId ) throws SQLException {
		Loggers.business.debug( "{}: {}", orderKey, status + " " + updateUserId + " start." );

		PreparedStatement pstmt = null;
		try {
			pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_ORDER SET STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ? WHERE ORDER_KEY = ?" );

			pstmt.setString( 1, status );
			pstmt.setString( 2, updateUserId );
			pstmt.setString( 3, orderKey );

			return pstmt.executeUpdate() == 1;
		} finally {
			if( pstmt != null ) pstmt.close();
			Loggers.business.debug( "{}: {}", orderKey, status + " " + updateUserId + " end." );
		}
	}

	public boolean updateStatus( SQLHandler this_handler, String orderKey, String status, String updateUserId ) throws SQLException {
		Loggers.business.debug( "{}: {}", orderKey, status + " " + updateUserId + " start." );

		PreparedStatement pstmt = null;
		try {
			pstmt = this_handler.getConnection().prepareStatement(
				"UPDATE DPR_ORDER SET STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ? WHERE ORDER_KEY = ?" );

			pstmt.setString( 1, status );
			pstmt.setString( 2, updateUserId );
			pstmt.setString( 3, orderKey );

			return pstmt.executeUpdate() == 1;
		} finally {
			if( pstmt != null ) pstmt.close();
			Loggers.business.debug( "{}: {}", orderKey, status + " " + updateUserId + " end." );
		}
	}

	public boolean validateOrderKey( String orderKey, String distributorCode, String countryCode ) throws SQLException {
		return SQLManager.getObjectValue( handler, "SELECT ORD.ORDER_KEY FROM DPR_ORDER ORD WHERE ORD.ORDER_KEY = ? AND PARTYCD = ? AND COUNTRYCD = ? AND ORDDATE = TRUNC(SYSDATE)" ) != null;
	}

	public boolean validOrderCreation( String orderKey ) throws SQLException, DataException {
		return ( SQLManager.executeStatement(handler,
			"SELECT ORDER_KEY FROM DPR_ORDER WHERE ORDER_KEY = ? AND STATUS = ?", new Object[]{ orderKey, STATUS_CREATED }) > 0 );
	}
}
