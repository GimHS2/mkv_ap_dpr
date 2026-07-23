/*
 *	File Name:	Schema.java
 *	Version:	2.2.36
 *
 *	Description:
 *
 *	Note:
 *	DPR_PARTY_LINK : PTYS와 PTYS_LINK의 organization, division, distributionChannel는 같다고 가정
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	yjkdev21	2026/05/29		2.2.36	DPR_MOQITEM_RLT : DISTINCT -> MAX() 로 변경
 *	GimHS		2026/04/30		2.2.36	DPR_MOQITEM_RLT: alSlfQty2(전체 남은 MOQ) 계산식 변경
 *	jbaek		2026/03/31		2.2.35	MQQItem OrderConfirmed MoqCfg Child Qty 칼럼 추가
 *	GimHS		2026/03/31		2.2.35	MOQ관련 child를 포함한 전체 수량 항목 추가
 *	yjkdev21	2026/03/31		2.2.35	DPR_MOQITEM_CFG : DISTINCT -> MAX() 로 변경, TRUNC(SYSDATE) -> timezone 에 맞게 변경
 *	dudwls3720	2024/02/29		2.2.34	DPR_ITEM_MASTER : YN -> Y,N 오타 수정
 *	jbaek		2023/07/27		2.2.33	DPR_PARTY_CREDIT 추가, packSize 업데이트 가능하도록 변경
 *	hankalam	2021/04/30		2.2.32	DPR_PARTY_LINK, DPR_PARTY_SALES : address1, address2 추가
 *	jbaek		2020/12/31		2.2.31	DPR_USER_MULTIEMP 추가, DPR_PARTY_EMPLOYEE pk 조건 변경
 *	hankalam	2020/12/31		2.2.31	DPR_STOCK_QUERY, DPR_STOCK_QUERY_MNG, DPR_STOCK_QUERY_PLANTMAP 추가
 *	jbaek		2020/08/30		2.2.30	childOrderKey,childOrderNumber 중복 오류 수정
 *	jbaek		2020/03/30		2.2.30	$ordrev
 *	hankalam	2020/06/30		2.2.30	DPR_ITEM_MASTER: dangerousInd 추가, DPR_ITEM_MASTER_PLANT 추가
 *	jbaek		2020/04/30		2.2.29	mview snapshot에서부터 최근 발주까지 감안한 수량 계산.
 *	jbaek		2019/11/30		2.2.28	사용되지 않던 ORDI.orderStatus 칼럼키 삭제
 *	jbaek		2019/09/30		2.2.28	MOQ_ITEM_RLT 테이블을 mvMOQ_ITEM_RLT 로 변경.
 *	hankalam	2019/07/31		2.2.27	DPR_FREEGOODS 추가, DPR_ORDER: Freegoods 관련항목 추가
 *	hankalam	2019/06/28		2.2.26	Moq 추가, OrderClose 추가, SalesUnit 추가, brand추가
 *	jbaek		2019/06/28		2.2.26	Moq 추가, OrderClose 추가
 *	hankalam	2019/06/28		2.2.26	DPR_RDD_TRG, DPR_RDD_IND, DPR_PARTY_OPER 추가
 *	jbaek		2019/05/30		2.2.26	StopItem 추가, itemName 조회조건 추가, PackDeal 추가
 *	jbaek		2019/01/30		2.2.25	salesUnit 추가
 *	jbaek		2018/04/30		2.2.24	ItemMasterExtra 추가
 *	jbaek		2018/04/30		2.2.24	organizationInfo 추가
 *	hankalam	2017/11/30		2.2.23	DPR_SHORTAGE_ELIMINATE_ITEM, DPR_PRODUCT_REQ 추가, DPR_ORDER_DTL: itemDisplayInd 추가
 *	jbaek		2017/10/30		2.2.23	DPR_BILLING_REPORT 추가
 *	jbaek		2017/06/30		2.2.23	DPR_ITEM_EANMAP 추가, DPR_ORDER_DTL: CONS_EAN 추가
 *	hankalam	2017/04/28		2.2.22	vwDPR_ORDER_ITEM 추가
 *	hankalam	2017/02/28		2.2.21	DPR_ORDER_ITEM: plantInd 추가
 *										DPR_ORDER_DTL: formatCSEQty 추가
 *	song7981	2016/05/20		2.2.20	상품 가격 마스터(업로드) 추가
 *										DPR_MASTER_LINK: masterOrganizationCode 유효성 검증 추가
 *	song7981	2016/04/25		2.2.19	order Item에 shelf_life 추가
 *	song7981	2016/02/29		2.2.18	DPR_PLANT_RCV 기능 추가
 *	jbaek		2015/08/30		2.2.17	deliveryOpenQty 수량 제한 없앰.
 *	jbaek		2015/04/07		2.2.16	Plant SKU 제외 기능: plantCode를 PK에 추가
 *	jbaek		2014/10/30		2.2.15	Goods Issue Date, Credit Release Date 추가
 *	jbaek		2014/09/30		2.2.14	Product Hierarchy Level 기능 개발
 *	jbaek		2014/07/13		2.2.13	Sold-to Level MOV 기능 개발
 *	jbaek		2014/02/16		2.2.12	Plant SKU 제외 기능 개발
 *	jbaek		2013/11/30		2.2.11	Material Status Auto Update 기능 개발
 *	jbaek		2013/05/30		2.2.10	HeaderStatus, LineItemStatus 기능 추가
 *	jbaek		2013/04/30		2.2.9	Sales Mov 관리: DPR_SALES_MOV 추가
 *	jbaek		2013/01/30		2.2.8	PIPO 기능 개발, LineItemStatus 기능 개발
 *	jbaek		2012/08/30		2.2.7	soldPartyName 쿼리에 DISTINCT 추가됨
 *	jbaek		2012/07/30		2.2.6	minOrderTotal 추가
 *	jbaek		2011/12/30		2.2.5	itemName의 DataType을 STRING에서 DESC로 변경
 *	jbaek		2011/11/30		2.2.4	DPR_PARTY_SALES에 notOrderable 추가
										DPR_PARTY_LINK의 QueryableField에 partyNotOrderable 추가
 *	lsinji		2011/02/28		2.2.3	ORDER_NUMBER, BILLING_NUMBER, DELIVERY_NUMBER, SIMULATION_KEY 길이 변경
 *	lsinji		2009/12/11		2.2.2	DPR_ORDER_ITEM에 existingOrderInd 추가
 *										DPR_ORDER_DTL과 DPR_ORDER_INFO_DTL이 LINE_NO로 join 되도록 수정
 *										DPR_ORDER_DTL RD -> OP로 변경. DPR_ORDER_DTL.STATUS에 "SP", "DE" 상태 추가
 *	lsinji		2009/06/30		2.2.1	DPR_PARTY_JDMS, DPR_USER_JDMS 추가 및 DPR_PARTY_SALES, DPR_ORDER에 JDMS관련 field 추가
 *	guksm		2008/08/31		2.2.0	create
 *
**/

package com.irt.dpr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.irt.data.Condition;
import com.irt.rbm.RBMSystem;
import com.irt.sql.ConditionQueryBuffer;
import com.irt.sql.ConditionalQueryableField;
import com.irt.sql.HierarchyCodeField;
import com.irt.sql.Joinable;
import com.irt.sql.JoinableImpl;
import com.irt.sql.JoinableImplBK;
import com.irt.sql.JoinableWrapper;
import com.irt.sql.NestedJoinable;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryBufferValid;
import com.irt.sql.QueryFactory;
import com.irt.sql.Queryable;
import com.irt.sql.QueryableField;
import com.irt.sql.QueryableFieldImpl;
import com.irt.sql.QueryableFieldImplBK;
import com.irt.sql.QueryableFieldWrapper;
import com.irt.sql.QueryableImpl;
import com.irt.sql.Table;

/**
 *
 */
public class Schema extends com.irt.sql.Schema {//@formatter:off

	public static final String ordCloseDateTime = ""
					+ "(TRUNC(pkSYSDate.fCurrentDate(OCL.CLOSE_TIMEZONE)) + NUMTODSINTERVAL(CAST(SUBSTR(OCL.CLOSE_TIME,1,2) AS INTEGER),'HOUR') "
					+ "+ NUMTODSINTERVAL(CAST(SUBSTR(OCL.CLOSE_TIME,4,2) AS INTEGER),'MINUTE'))";
	public static final String isCloseItem = "(CASE WHEN (pkSYSDate.fCurrentDate(OCL.CLOSE_TIMEZONE)) >" + ordCloseDateTime + " THEN 'Y' ELSE 'N' END)";

	public final static String DPR_BILLING_REPORT			= "billingReport";
	public final static String DPR_MASTER_DESC				= "MasterDesc";
	public final static String DPR_MASTER_DESC_ORG			= "organizationMasterDescription";
	public final static String DPR_MASTER_LINK				= "masterLink";
	public final static String DPR_EMPLOYEE					= "employee";
	public final static String DPR_USER_EMPLOYEE			= "userEmployee";
	public final static String DPR_USER_MULTIEMP			= "userMultiEmployee";
	public final static String DPR_USER_JDMS				= "userJDMS";
	public final static String DPR_CUSTOMER_ADDRESS			= "customerAddress";
	public final static String DPR_CUSTOMER_GROUP			= "customerGroup";
	public final static String DPR_DISTRIBUTION_CHANNEL		= "distributionChannel";
	public final static String DPR_DIVISION					= "division";
	public final static String DPR_PLANT					= "plant";
	public final static String DPR_REGION					= "region";
	public final static String DPR_SALES_ORGANIZATION		= "salesOrganization";
	public final static String DPR_SALES_DISTRICT			= "salesDistrict";
	public final static String DPR_SALES_OFFICE				= "SalesOffice";
	public final static String vwDPR_SALES_OFFICE			= "VwSalesOffice";
	public final static String DPR_SALES_GROUP				= "salesGroup";
	public final static String DPR_SALES_OFFICE_GROUP		= "SalesOfficeGroup";
	public final static String vwDPR_SALES_OFFICE_GROUP		= "VwSalesOfficeGroup";
	public final static String DPR_SALES_MOV				= "officeMinimumValue";
	public final static String DPR_SALES_MOVPTY				= "partyMinimumValue";
	public final static String DPR_SALES_MOVSPTY			= "ShipPartyMinimumValue";
	public final static String DPR_COUNTRY					= "country";
	public final static String DPR_COUNTRY_COND				= "conuntryCondition";
	public final static String DPR_COUNTRY_DIST				= "conuntryDistributionChannel";
	public final static String DPR_COUNTRY_AUTH				= "countryAuthrize";
	public final static String DPR_COUNTRY_CATE				= "countryCategory";
	public final static String DPR_PARTY					= "party";
	public final static String DPR_PARTY_SALES				= "PartySales";
	public final static String DPR_PARTY_CREDIT				= "partyCredit";
	public final static String DPR_PARTY_AUTH				= "partyAuthrize";
	public final static String DPR_PARTY_LINK				= "PartyLink";
	public final static String DPR_PARTY_FUNCTION			= "PartyFunction";
	public final static String DPR_PARTY_EMPLOYEE			= "partyEmployee";
	public final static String DPR_PARTY_JDMS				= "partyJDMS";
	public final static String DPR_PARTY_OPER				= "partyOperation";
	public final static String DPR_BASEPRODUCT				= "baseproduct";
	public final static String DPR_MEGABRAND				= "megabrand";
	public final static String DPR_BRAND					= "brand";
	public final static String DPR_PUTUP					= "putup";
	public final static String DPR_VARIANT					= "variant";
	public final static String DPR_PRODUCT_CATE				= "ProductCategory";
	public final static String DPR_ITEM_MASTER				= "ItemMaster";
	public final static String DPR_ITEM_MASTER_PLANT		= "ItemMasterPlant";
	public final static String DPR_ITEM_MASTER_DESC			= "ItemMasterDesc";
	public final static String DPR_ITEM_MASTER_INTRO		= "itemMasterIntroduction";
	public final static String DPR_ITEM_MASTER_SALES		= "ItemMasterSales";
	public final static String DPR_ITEM_MASTER_STATUS		= "itemMasterStatus";
	public final static String DPR_ITEM_MASTER_UOM			= "ItemMasterUom";
	public final static String DPR_ITEM						= "item";
	public final static String DPR_ITEM_IMG					= "itemImage";
	public final static String DPR_ITEM_PRICE				= "itemPrice";
	public final static String DPR_ITEM_EANMAP				= "itemEanMap";
	public final static String DPR_ITEM_EAN					= "itemEan";
	public final static String DPR_ITEM_MASTER_EXTRA		= "itemMasterExtra";// by upload
	public final static String DPR_FREEGOODS				= "FreeGoods";
	public final static String DPR_MOQITEM_CFG				= "MoqItemCfg";
	public final static String DPR_MOQITEM_RLT				= "MoqItemRlt";
	public final static String DPR_MOQITEM_CFGDATA			= "MoqItemCfgData";
	public final static String DPR_PERIOD					= "Peroid";
	public final static String DPR_PACKDEAL_CFG				= "PackDealCfg";
	public final static String DPR_PACKDEAL_CFGRLT			= "PackDealCfgRlt";
	public final static String DPR_PACKDEAL_ITEM			= "PackDealItem";
	public final static String DPR_PROMOTION_ITEM			= "protmotionItem";
	public final static String DPR_PLANT_ITEM				= "plantItem";
	public final static String DPR_PLANT_RCV				= "plantRecovery";
	public final static String DPR_RDD_TRG					= "rddTrigger";
	public final static String DPR_RDD_IND					= "rddIndicator";
	public final static String DPR_STOPITEM_CFG				= "StopItemCfg";
	public final static String DPR_STOPITEM					= "StopItem";
	public final static String DPR_ORDER_ITEM				= "OrderItem";
	public final static String vwDPR_ORDER_ITEM				= "vwOrderItem";
	public final static String DPR_SIMULATION				= "sumulation";
	public final static String DPR_SIMULATION_DTL			= "simulationDetail";
	public final static String DPR_SIMULATION_HIST			= "simulationHistory";
	public final static String DPR_ORDCLOSE					= "OrderClose";
	public final static String DPR_ORDER					= "order";
	public final static String DPR_ORDER_DTL				= "orderDetail";
	public final static String DPR_ORDER_INFO				= "orderInfo";
	public final static String DPR_ORDER_INFO_DTL			= "orderInfoDetail";
	public final static String DPR_ORDER_BILLING			= "orderBilling";
	public final static String DPR_ORDER_BILLING_DTL		= "orderBillingDetail";
	public final static String DPR_ORDER_DELIVERY			= "orderDelivery";
	public final static String DPR_ORDER_MEMOS				= "orderMemos";
	public final static String DPR_ORDER_REMARK				= "orderRemark";
	public final static String DPR_ORDER_TEMPLATE			= "orderTemplate";
	public final static String DPR_ORDER_TEMPLATE_DTL		= "orderTemplateDetail";
	public final static String DPR_SHORTAGE_ELIMINATE_ITEM	= "shortageEliminateItem";
	public final static String DPR_PRODUCT_REQ				= "productRequirement";
	public final static String DPR_STOCK_QUERY_MNG			= "StockQueryManage";
	public final static String DPR_STOCK_QUERY_PLANTMAP		= "StockQueryPlantMapping";
	public final static String DPR_STOCK_QUERY				= "StockQuery";
	public final static String DPR_SKU_MAP					= "skuMap";
	public final static String DPR_CUSTOMER_MAP				= "customerMap";
	public final static String DPR_INTERFACE_LOG			= "interfaceLog";
	public final static String DPR_SITE_LINK				= "siteLink";
	public final static String DPR_UPLOAD_HEADER			= "uploadHeader";
	public final static String DPR_UPLOAD_CIM				= "uploadCustomerIdMapping";
	public final static String DPR_UPLOAD_CMT				= "uploadCustomerMaster";
	public final static String DPR_UPLOAD_INV				= "uploadInventory";
	public final static String DPR_UPLOAD_SEO				= "uploadSellOut";
	public final static String DPR_UPLOAD_SKM				= "uploadSKUMapping";
	public final static String DPR_UPLOAD_SSL				= "uploadSellingSKUList";
	public final static String DPR_UPLOAD_ORD				= "uploadOrderDetail";


	private final static String[] MASTER_NAME_FIELD_KEYS = new String[]{"itemName", "organizationName", "distributionChannelName", "officeName", "groupName"};
	public final static Integer[] PCATE_COMM_LEN	= { 1, 2, 6, 10, 14, 18 };

	private final static String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-+&";

	private final static String TYPE_BASE					= "B";
	private final static String TYPE_NONE					= "N";

	private final static Schema schema = new Schema();

	Schema() {
		Table table;
		Queryable queryable;
		Table.Field[] tfields;


		/***************************************************************************************************
			*	DPR_MASTER_DESC
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "masterCode",			"MASTER_CD",				"DPR_MASTER_CODE",				0, 20 )
			, createFD( PM, "masterType",			"MASTER_TYPE",				"DPR_MASTER_TYPE",				"DPR_MASTER_TYPE_", "CG,DC,DI,RG,SD,SG,SF,SO,BP,MB,BR,VA,PU,PC" )
			, createFD( PM, "languageCode",			"LANGCD",					"DPR_LANGUAGECODE",				2, 2 )
			, createFD( MD, "masterName",			"MASTER_NAME",				"DPR_MASTER_NAME",				DESC, 0, 128 )
			, createFD( OP, "masterDescription",	"MASTER_DESC",				"DPR_MASTER_DESCRIPTION",		DESC, 0, 128 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_MASTER_DESC, table = createTable("DPR_MASTER_DESC", "MSTD", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_MASTER_DESC, new QueryableImpl(table) {{
			Joinable tbl_DESC_BASE = new JoinableImpl( "MSTDB", "DPR_MASTER_DESC", "MSTDB.MASTER_CD(+) = MSTD.MASTER_CD"
					+ " AND MSTDB.MASTER_TYPE(+) = MSTD.MASTER_TYPE AND MSTDB.LANGCD(+) = MSTD.LANGCD" );

			Joinable tbl_DESC_ORG = new JoinableImplBK( "MSTDO", "DPR_MASTER_DESC_ORG", "MSTDO.MASTER_CD(+) = MSTDB.MASTER_CD"
					+ " AND MSTDO.MASTER_TYPE(+) = MSTDB.MASTER_TYPE AND MSTDB.LANGCD = 'en' AND MSTDO.ORGANIZATIONCD(+) = ?"
					+ " AND MSTDO.LANGCD(+) = ?"
					, new String[] { "masterOrganizationCode", "masterLanguageCode" }, tbl_DESC_BASE );

			QueryBufferValid valid_DESC_ORG = new QueryBufferValid.Condition(true, "masterOrganizationCode", "masterLanguageCode");
			appendCND( valid_DESC_ORG,  new QueryableField[] {
				  new QueryableFieldImpl( STRING, "orgMasterCode", "NVL(MSTDO.MASTER_CD, MSTD.MASTER_CD)", "DPR_MASTER_CODE", tbl_DESC_ORG )
				, new QueryableFieldImpl( STRING, "orgMasterName", "NVL(MSTDO.MASTER_NAME, MSTD.MASTER_NAME)", "DPR_ORGANIZATION_MASTER_NAME", tbl_DESC_ORG )
				, new QueryableFieldImpl( STRING, "orgMasterDescription", "NVL(MSTDO.MASTER_DESC, MSTD.MASTER_DESC)", "DPR_MASTER_DESCRIPTION", tbl_DESC_ORG )
				, new QueryableFieldImpl( STRING, "languageName"
						, "(SELECT SB.LANG_NAME FROM SYS_LANG SB WHERE SB.LANG_CD = MSTD.LANGCD)", "DPR_LANGUAGENAME" )
			} );
			QueryBufferValid querybufValid = new QueryBufferValid.Condition( "masterOrganizationCode" );
			appendCND( querybufValid,
				new QueryableFieldImplBK( STRING, "masterOrganizationCode", "NVL(MSTDO.ORGANIZATIONCD, ?)", "masterOrganizationCode", "DPR_SALESORGANIZATION_CODE", tbl_DESC_ORG )
			);
			querybufValid = new QueryBufferValid.Condition( "masterLanguageCode" );
			appendCND( querybufValid,
				new QueryableFieldImplBK( STRING, "masterLanguageCode", "NVL(MSTDO.LANGCD, ?)", "masterLanguageCode", "DPR_MASTER_MASTERLANGUAGECODE", tbl_DESC_ORG )
			);

			append( new QueryableField[]{
					  new QueryableFieldImpl( STRING, "masterCodePart1", "NVL(REGEXP_SUBSTR(MSTD.MASTER_CD, '^(.+);(.+)$', 1,1, NULL, 1), MSTD.MASTER_CD)")
					, new QueryableFieldImpl( STRING, "masterCodePart2", "REGEXP_SUBSTR( MASTER_CD, '^(.+);(.+)$', 1,1, NULL, 2 )")
			});

		}} );


		/***************************************************************************************************
			*	DPR_MASTER_DESC_ORG
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "masterCode",			"MASTER_CD",				"DPR_MASTER_CODE",				0, 20 )
			, createFD( PM, "masterType",			"MASTER_TYPE",				"DPR_MASTER_TYPE",				"DPR_MASTER_TYPE_", "CG,DC,DI,RG,SD,SG,SF,SO,BP,MB,BR,VA,PU,PC" )
			, createFD( PM, "languageCode",			"LANGCD",					"DPR_LANGUAGECODE",				2, 2 )
			, createFD( PM, "masterOrganizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( MD, "orgMasterName",			"MASTER_NAME",				"DPR_MASTER_NAME",				DESC, 0, 128 )
			, createFD( OP, "orgMasterDescription",	"MASTER_DESC",				"DPR_MASTER_DESCRIPTION",		DESC, 0, 128 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_MASTER_DESC_ORG, table = createTable("DPR_MASTER_DESC_ORG", "MSTDO", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_MASTER_LINK
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "masterCode",			"MASTER_CD",				"DPR_MASTER_CODE",				0, 20 )
			, createFD( PM, "linkType",				"LINK_TYPE",				"DPR_MASTER_LINKTYPE",			"DPR_MASTER_LINKTYPE_", "CG,DC,DI,RG,SD,SG,SF" )
			, createFD( PM, "linkMasterCode",		"LINK_MASTERCD",			"DPR_MASTER_LINKCODE",			0, 20 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_MASTER_LINK, table = createTable("DPR_MASTER_LINK", "MSTL", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_MASTER_LINK, new QueryableImpl(table) {{
			Joinable tbl_DESC_ORG = new JoinableImplBK( "MSTDO", "DPR_MASTER_DESC_ORG", "MSTDO.MASTER_CD(+) = MSTD.MASTER_CD"
					+ " AND MSTDO.MASTER_TYPE(+) = MSTD.MASTER_TYPE AND MSTDO.LANGCD(+) = MSTD.LANGCD AND MSTDO.ORGANIZATIONCD(+) = ?", new String[] { "masterOrganizationCode" } );
			Joinable tbl_DESC = new JoinableImplBK( "MSTD", "DPR_MASTER_DESC", "MSTD.MASTER_CD(+) = MSTL.LINK_MASTERCD"
					+ " AND MSTD.MASTER_TYPE(+) = MSTL.LINK_TYPE AND MSTD.LANGCD = ?", "displayLanguage", tbl_DESC_ORG );

			QueryBufferValid valid_DESC_ORG = new QueryBufferValid.Condition( "masterOrganizationCode" );
			appendCND( valid_DESC_ORG
					, new QueryableFieldImpl( STRING, false, "masterOrganizationCode", "MSTDO.ORGANIZATIONCD", "DPR_SALESORGANIZATION_CODE", tbl_DESC )
				);

			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "code", "MSTL.LINK_MASTERCD" )
				, new QueryableFieldImpl( STRING, "name", "NVL(MSTDO.MASTER_NAME, MSTD.MASTER_NAME)", tbl_DESC )
				, new QueryableFieldImpl( STRING, "displayLanguage", "MSTD.LANGCD", "DPR_LANGUAGECODE", tbl_DESC )
			} );

			QueryBufferValid valid_CG = new MasterLink.Valid( "linkType", MasterLink.MASTERTYPE_CUSTOMERGROUP );
			QueryBufferValid valid_DC = new MasterLink.Valid( "linkType", MasterLink.MASTERTYPE_DISTRIBUTION_CHANNEL );
			QueryBufferValid valid_DI = new MasterLink.Valid( "linkType", MasterLink.MASTERTYPE_DIVISION );
			QueryBufferValid valid_RG = new MasterLink.Valid( "linkType", MasterLink.MASTERTYPE_REGION );
			QueryBufferValid valid_SD = new MasterLink.Valid( "linkType", MasterLink.MASTERTYPE_SALES_DISTRICT );
			QueryBufferValid valid_SG = new MasterLink.Valid( "linkType", MasterLink.MASTERTYPE_SALES_GROUP );
			QueryBufferValid valid_SF = new MasterLink.Valid( "linkType", MasterLink.MASTERTYPE_SALES_OFFICE );

			Joinable tbl_PMST = new JoinableImpl( "CGRP", "DPR_CUSTOMER_GROUP", "CGRP.CUSTOMERGRP_CD = MSTL.LINK_MASTERCD" );
			appendCND( valid_CG
				, new QueryableFieldImpl( STRING, false, "cusotmerGroupCode", "CGRP.CUSTOMERGRP_CD", tbl_PMST )
			);

			tbl_PMST = new JoinableImpl( "DCHA", "DPR_DISTRIBUTION_CHANNEL", "DCHA.CHANNEL_CD = MSTL.LINK_MASTERCD" );
			appendCND( valid_DC
				, new QueryableFieldImpl( STRING, false, "distributionCahnnelCode", "CGRP.CUSTOMERGRP_CD", tbl_PMST )
			);

			tbl_PMST = new JoinableImpl( "DIV", "DPR_DIVISION", "DIV.DIVISION_CD = MSTL.LINK_MASTERCD" );
			appendCND( valid_DI
				, new QueryableFieldImpl( STRING, false, "divisionCode", "DIV.DIVISION_CD", tbl_PMST )
			);

			tbl_PMST = new JoinableImpl( "RGN", "DPR_REGION", "RGN.REGION_CD || ';' || RGN.COUNTRYKEY = MSTL.LINK_MASTERCD" );
			appendCND( valid_RG, new QueryableField[] {
						new QueryableFieldImpl( STRING, false, "regionCode", "RGN.REGION_CD", tbl_PMST )
				, new QueryableFieldImpl( STRING, false, "regionCountryKey", "RGN.COUNTRYKEY", tbl_PMST )
				, new QueryableFieldImplBK( STRING, "regionName", "(SELECT SB.MASTER_NAME FROM DPR_MASTER_DESC SB"
						+ " WHERE SB.MASTER_CD = RGN.REGION_CD || ';' || RGN.COUNTRYKEY AND SB.MASTER_TYPE = MSTL.LINK_TYPE"
						+ " AND SB.LANGCD = ?)", "displayLanguage", tbl_PMST )
			} );

			tbl_PMST = new JoinableImpl( "SDST", "DPR_SALES_DISTRICT", "SDST.DISTRICT_CD = MSTL.LINK_MASTERCD" );
			appendCND( valid_SD
				, new QueryableFieldImpl( STRING, false, "districtCode", "SDST.DISTRICT_CD", tbl_PMST )
			);

			/* Sales Group */
			QueryBufferValid valid_HIRSG = new QueryBufferValid.Condition( "officeCode" );
			tbl_PMST = new JoinableImpl( "SGRP", "DPR_SALES_GROUP", "SGRP.GROUP_CD = MSTL.LINK_MASTERCD" );
			appendCND( valid_SG
				, new QueryableFieldImpl( STRING, false, "groupCode", "SGRP.GROUP_CD", tbl_PMST )
			);
			appendCND( valid_HIRSG
				, new QueryableFieldImpl( STRING, true, "officeCode", "SOFG.OFFICECD", new JoinableImplBK("SOFG", "DPR_SALES_OFFICE_GROUP", "SOFG.GROUPCD = MSTL.LINK_MASTERCD AND SOFG.OFFICECD = ?", "officeCode") )
			);

			tbl_PMST = new JoinableImpl( "SOFF", "DPR_SALES_OFFICE", "SOFF.OFFICE_CD = MSTL.LINK_MASTERCD" );
			appendCND( valid_SF
				, new QueryableFieldImpl( STRING, false, "officeCode", "SOFF.OFFICE_CD", tbl_PMST )
			);
		}} );


		/***************************************************************************************************
			*	DPR_EMPLOYEE
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "employeeId",			"EMPLOYEE_ID",				"DPR_EMPLOYEE_ID",				0, 8 )
			, createFD( PM, "languageCode",			"LANGCD",					"DPR_LANGUAGECODE",				2, 2 )
			, createFD( OP, "firstName",			"FIRST_NAME",				"DPR_EMPLOYEE_FIRSTNAME",		0, 40 )
			, createFD( OP, "lastName",				"LAST_NAME",				"DPR_EMPLOYEE_LASTNAME",		0, 40 )
			, createFD( OP, "extraValue1",			"EXTRAVALUE1",				"DPR_EMPLOYEE_EXTRAVALUE1",		0, 80 )
			, createFD( OP, "extraValue2",			"EXTRAVALUE2",				"DPR_EMPLOYEE_EXTRAVALUE2",		0, 80 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_EMPLOYEE, table = createTable("DPR_EMPLOYEE", "EMP", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_USER_EMPLOYEE
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "uniqId",				"UNIQID",					"USERID",						0, 30 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( MD, "employeeId",			"EMPLOYEE_ID",				"DPR_EMPLOYEE_ID",				0, 8 )
			, createFD( OP, "extraValue1",			"EXTRAVALUE1",				"DPR_EMPLOYEE_EXTRAVALUE1",		0, 80 )
			, createFD( OP, "extraValue2",			"EXTRAVALUE2",				"DPR_EMPLOYEE_EXTRAVALUE2",		0, 80 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRYCODE",				0, 15 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_USER_EMPLOYEE, table = createTable("DPR_USER_EMPLOYEE", "UEMP", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		*	DPR_USER_MULTIEMP
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "uniqId",				"UNIQID",					"USERID",						0, 30 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "employeeId",			"EMPLOYEE_ID",				"DPR_EMPLOYEE_ID",				0, 8 )
			, createFD( OP, "extraValue1",			"EXTRAVALUE1",				"DPR_EMPLOYEE_EXTRAVALUE1",		0, 80 )
			, createFD( OP, "extraValue2",			"EXTRAVALUE2",				"DPR_EMPLOYEE_EXTRAVALUE2",		0, 80 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRYCODE",				0, 15 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_USER_MULTIEMP, table = createTable("DPR_USER_MULTIEMP", "MEMP", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_USER_JDMS
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "uniqId",				"UNIQID",					"USERID",						0, 30 )
			, createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_USER_JDMS, table = createTable("DPR_USER_JDMS", "UJDMS", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_USER_JDMS, new QueryableImpl(table) {{
			Joinable tbl_USR = new JoinableImpl( "USR", "USR_USER", "USR.UNIQID = UJDMS.UNIQID" );

			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "userId", "USR.USERID", tbl_USR )
				, new QueryableFieldImpl( STRING, "userName", "USR.USER_NAME", tbl_USR )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_CUSTOMER_ADDRESS
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "addressCode",			"ADDRESS_CD",				"DPR_CUSTOMERADDRESS_CODE",		0, 10 )
			, createFD( PM, "nation",				"NATION",					"DPR_CUSTOMERADDRESS_NATION",	3, 3 )
			, createFD( MD, "languageCode",			"LANGCD",					"DPR_CUSTOMERADDRESS_LANGCD",	2, 2 )
			, createFD( OP, "address1",				"ADDRESS1",					"DPR_CUSTOMERADDRESS_ADDRESS1",	DESC, 0, 80 )
			, createFD( OP, "address2",				"ADDRESS2",					"DPR_CUSTOMERADDRESS_ADDRESS2",	DESC, 0, 80 )
			, createFD( OP, "address3",				"ADDRESS3",					"DPR_CUSTOMERADDRESS_ADDRESS3",	DESC, 0, 80 )
			, createFD( OP, "address4",				"ADDRESS4",					"DPR_CUSTOMERADDRESS_ADDRESS4",	DESC, 0, 80 )
			, createFD( OP, "address5",				"ADDRESS5",					"DPR_CUSTOMERADDRESS_ADDRESS5",	DESC, 0, 80 )
			, createFD( OP, "dateFrom",				"DATE_FROM",				"DPR_CUSTOMERADDRESS_DATEFROM",	DESC, 0, 12 )
			, createFD( OP, "city",					"CITY",						"DPR_CUSTOMERADDRESS_CITY",		DESC, 0, 200 )
			, createFD( OP, "district",				"DISTRICT",					"DPR_CUSTOMERADDRESS_DISTRICT",	DESC, 0, 200 )
			, createFD( OP, "billClosingDate",		"BILL_CLOSING_DATE",		"DPR_CUSTOMERADDRESS_BCDATE",	DESC, 0, 25 )
			, createFD( OP, "languageKey",			"LANGUAGE_KEY",				"DPR_CUSTOMERADDRESS_LANGKEY",	DESC, 0, 3 )
			, createFD( OP, "extension2",			"EXTENSION2",				"DPR_CUSTOMERADDRESS_EXTENSION2",	DESC, 0, 48 )
			, createFD( OP, "cityCode",				"CITY_CODE",				"DPR_CUSTOMERADDRESS_CITYCODE",	DESC, 0, 2 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_CUSTOMER_ADDRESS, table = createTable("DPR_CUSTOMER_ADDRESS", "CADR", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_CUSTOMER_GROUP
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "customerGroupCode",	"CUSTOMERGRP_CD",			"DPR_CUSTOMERGROUP_CODE",		0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_CUSTOMER_GROUP, table = createTable("DPR_CUSTOMER_GROUP", "CGRP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_CUSTOMER_GROUP, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "customerGroupName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = CGRP.CUSTOMERGRP_CD"
							+ " AND SB.MASTER_TYPE = 'CG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_CUSTOMERGROUP_NAME" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_DISTRIBUTION_CHANNEL
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "channelCode",			"CHANNEL_CD",				"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_DISTRIBUTION_CHANNEL, table = createTable("DPR_DISTRIBUTION_CHANNEL", "DCHA", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_DISTRIBUTION_CHANNEL, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "channelName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = DCHA.CHANNEL_CD"
							+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = ?)", "displayLanguage", "DPR_DISTRIBUTIONCHANNEL_NAME" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_DIVISION
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "divisionCode",			"DIVISION_CD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_DIVISION, table = createTable("DPR_DIVISION", "DIV", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_DIVISION, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "divisionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = DIV.DIVISION_CD"
							+ " AND SB.MASTER_TYPE = 'DI' AND SB.LANGCD = ?)", "displayLanguage", "DPR_DIVISION_NAME" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_REGION
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "regionCode",			"REGION_CD",				"DPR_REGION_CODE",				0, 8 )
			, createFD( PM, "countryKey",			"COUNTRYKEY",				"DPR_REGION_COUNTRYKEY",		0, 3 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_REGION, table = createTable("DPR_REGION", "RGN", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_REGION, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "regionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = RGN.REGION_CD || ';' || RGN.COUNTRYKEY"
							+ " AND SB.MASTER_TYPE = 'RG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_REGION_NAME" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_SALES_ORGANIZATION
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "organizationCode",		"ORGANIZATION_CD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( MD, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_SALES_ORGANIZATION, table = createTable("DPR_SALES_ORGANIZATION", "SORG", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_SALES_ORGANIZATION, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SORG.ORGANIZATION_CD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = ?)", "displayLanguage", "DPR_SALES_ORGANIZATION_NAME" )
				, new QueryableFieldImplBK( DESC, "divisionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SORG.DIVISIONCD"
							+ " AND SB.MASTER_TYPE = 'DI' AND SB.LANGCD = ?)", "displayLanguage", "DPR_DIVISION_NAME" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_SALES_DISTRICT
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "districtCode",			"DISTRICT_CD",				"DPR_SALESDISTRICT_CODE",		0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_SALES_DISTRICT, table = createTable("DPR_SALES_DISTRICT", "SDST", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_SALES_DISTRICT, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "districtName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SDST.DISTRICT_CD AND SB.MASTER_TYPE = 'SD' AND SB.LANGCD = ?)"
						, "displayLanguage", "DPR_SALES_DISTRICT_NAME" )
			} );
		}} );


		/***************************************************************************************************
		*	DPR_SALES_OFFICE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "officeCode",			"OFFICE_CD",				"DPR_SALESOFFICE_CODE",			0, 8 )
			, createFD( MD, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_SALES_OFFICE, table = createTable("DPR_SALES_OFFICE", "SOFF", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_SALES_OFFICE, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();
			append( makeMasterNames(baseAlias, null, "organizationName") );
			append( new QueryableField[] {
					new QueryableFieldImplBK( DESC, "officeName"
								, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = "+baseAlias+".OFFICE_CD"
									+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_OFFICE_NAME" )}
			);
		}} );

		/***************************************************************************************************
		*	vwDPR_SALES_OFFICE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( RD, "officeCode",			"OFFICECD",					"DPR_SALESOFFICE_CODE",			0, 8 )
			, createFD( RD, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
		};
		putTable( vwDPR_SALES_OFFICE, table = createTable("vwDPR_SALES_OFFICE", "SOFF", tfields, "UPGDATE = SYSDATE") );
		putQueryable( vwDPR_SALES_OFFICE, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();
			append( makeMasterNames(baseAlias, null, "organizationName", "officeName") );
		}} );


		/***************************************************************************************************
			*	DPR_SALES_GROUP
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "salesGroupCode",		"GROUP_CD",					"DPR_SALESGROUP_CODE",			0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_SALES_GROUP, table = createTable("DPR_SALES_GROUP", "SGRP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_SALES_GROUP, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "groupName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SGRP.GROUP_CD"
							+ " AND SB.MASTER_TYPE = 'SG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_SALES_GROUP_NAME" )
			} );
		}} );


		/***************************************************************************************************
		*	DPR_SALES_OFFICE_GROUP
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( OP, "officeCode",			"OFFICECD",					"DPR_SALESOFFICE_CODE",			0, 8 )
			, createFD( OP, "groupCode",			"GROUPCD",					"DPR_SALESGROUP_CODE",			0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_SALES_OFFICE_GROUP, table = createTable("DPR_SALES_OFFICE_GROUP", "SOFG", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_SALES_OFFICE_GROUP, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "officeName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SOFG.OFFICECD"
							+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = ?)", "displayLanguage", "DPR_SALES_OFFICE_NAME" )
				, new QueryableFieldImplBK( DESC, "groupName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SOFG.GROUPCD"
							+ " AND SB.MASTER_TYPE = 'SG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_SALES_GROUP_NAME" )
			} );
		}} );


		/***************************************************************************************************
		*	vwDPR_SALES_OFFICE_GROUP
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( OP, "officeCode",			"OFFICECD",					"DPR_SALESOFFICE_CODE",			0, 8 )
			, createFD( OP, "groupCode",			"GROUPCD",					"DPR_SALESGROUP_CODE",			0, 8 )
			, createFD( RD, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( vwDPR_SALES_OFFICE_GROUP, table = createTable("vwDPR_SALES_OFFICE_GROUP", "SOFG", tfields, "UPGDATE = SYSDATE") );
		putQueryable( vwDPR_SALES_OFFICE_GROUP, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();
			append( Schema.makeMasterNames(baseAlias, null, "officeName", "groupName" ) );
//			append( new QueryableField[] {
//						new QueryableFieldImplBK( DESC, "officeName"
//						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SOFG.OFFICECD"
//							+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = ?)", "displayLanguage", "DPR_SALES_OFFICE_NAME" )
//				, new QueryableFieldImplBK( DESC, "groupName"
//						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SOFG.GROUPCD"
//							+ " AND SB.MASTER_TYPE = 'SG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_SALES_GROUP_NAME" )
//			} );
		}} );


		/***************************************************************************************************
			*	DPR_COUNTRY
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "countryCode",			"COUNTRY_CD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( MD, "countryName",			"COUNTRYNAME",				"DPR_COUNTRY_NAME",				DESC, 0, 512 )
			, createFD( OP, "additionalCountryName",	"COUNTRYNAME_ADDITION",		"DPR_COUNTRY_ADDITIONALNAME",	DESC, 0, 512 )
			, createFD( MD, "countryKey",			"COUNTRYKEY",				"DPR_COUNTRY_COUNTRYKEY",		2, 2 )
			, createFD( MD, "languageCode",			"LANGCD",					"DPR_COUNTRY_LANGUAGECODE",				2, 2 )
			, createFD( MD, "currencyCode",			"CURRENCYCD",				"DPR_COUNTRY_CURRENCYCODE",				3, 3 )
			, createFD( OP, "currInteger",			"CURR_INTEGER",				"DPR_COUNTRY_CURRENCY_INTEGER",			1, 1 )
			, createFD( OP, "contactName",			"CONTACT_NAME",				"DPR_COUNTRY_CONTACTNAME",		DESC, 0, 20 )
			, createFD( OP, "contactTelephone",		"CONTACT_TEL",				"DPR_COUNTRY_CONTACTTELEPHONE",	DESC, 0, 30 )
			, createFD( OP, "defaultHierarchyLevel",	"DEF_HIERARCHY_LEVEL",	"DPR_COUNTRY_ENV_HIERARCHYLEVEL", 0, 1 )
			, createFD( OP, "hierarchyCondition",	"HERARCHY_COND",			"DPR_COUNTRY_ENV_HIERARCHYCOND", 0, 256 )
			, createFD( MD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00,99" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_COUNTRY, table = createTable("DPR_COUNTRY", "CNR", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_COUNTRY, new QueryableImpl(table) {{
			Joinable tbl_UPT = new JoinableImpl( "UPT", "USR_PARTY", "UPT.GLN(+) = CNR.COUNTRY_CD" );
			QueryBufferValid.Condition validCond = new QueryBufferValid.Condition( "organizationCode" );
			appendCND( validCond, new QueryableFieldImpl( STRING, "organizationCode", "CCND.ORGANIZATIONCD"
					, new JoinableImplBK( "CCND", "DPR_COUNTRY_COND", "CCND.COUNTRYCD = CNR.COUNTRY_CD AND CCND.ORGANIZATIONCD = ?"
					, "organizationCode")), true );

			append( makeCountryAuthQueryableFields(TYPE_BASE, "CNR") );
			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "partyId", "UPT.PARTY_ID", tbl_UPT )
				, new QueryableFieldImpl( STRING, "timeZone", "UPT.TIMEZONE", tbl_UPT )
				, new QueryableFieldImpl( STRING, "timeZoneName", "(SELECT SB.TIMEZONE_NAME FROM SYS_TIMEZONE SB WHERE SB.TIMEZONE_CD = UPT.TIMEZONE)", tbl_UPT )
				, new QueryableFieldImpl( STRING, "languageName"
						, "(SELECT SB.LANG_NAME FROM SYS_LANG SB WHERE SB.LANG_CD = CNR.LANGCD)", "DPR_LANGUAGENAME" )
				, new QueryableFieldImpl( STRING, "currencySymbol"
						, "(SELECT SB.CURR_SYMBOL FROM SYS_CURRENCY SB WHERE SB.CURR_CD = CNR.CURRENCYCD)", "DPR_CURRENCYSYMBOL" )
				, new QueryableFieldImpl( STRING, "currencyName"
						, "(SELECT SB.CURR_NAME FROM SYS_CURRENCY SB WHERE SB.CURR_CD = CNR.CURRENCYCD)", "DPR_CURRENCYNAME" )
				, new QueryableFieldImpl( STRING, "countryISOName"
						, "(SELECT SB.COUNTRY_NAME FROM SYS_COUNTRY SB WHERE SB.COUNTRY_CD = CNR.COUNTRYKEY)", "DPR_COUNTRYISO_NAME" )
				, new QueryableFieldImpl( INTEGER, "lowerDistirbutorCount"
						, "(SELECT COUNT(*) FROM DPR_PARTY SB WHERE SB.COUNTRYCD = CNR.COUNTRY_CD AND SB.PARTY_TYPE = 'DIS')" )
				, new QueryableFieldImpl( INTEGER, "lowerCusotmerCount"
						, "(SELECT COUNT(*) FROM DPR_PARTY SB WHERE SB.COUNTRYCD = CNR.COUNTRY_CD AND SB.PARTY_TYPE = 'CUS')" )
				, new QueryableFieldImpl( INTEGER, "conditionCount"
						, "(SELECT COUNT(*) FROM DPR_COUNTRY_COND SB WHERE SB.COUNTRYCD = CNR.COUNTRY_CD)" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_COUNTRY_COND
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_COUNTRY_CONDITION_ORGANIZATIONCODE",	0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( OP, "minOrderTotal",		"MIN_ORDERTOTAL",			"DPR_COUNTRY_CONDITION_MIN_ORDERTOTAL", DOUBLE )
			, createFD( OP, "organizationNameDefault",		"ORG_NAME_DEFAULT",	"DPR_ORGANIZATION_NAME_DEFAULT", 0, 255 )
			, createFD( OP, "organizationNameLocal",		"ORG_NAME_LOCAL",	"DPR_ORGANIZATION_NAME_LOCAL", 0, 255 )
			, createFD( OP, "organizationAddress",			"ORG_ADDRESS",		"DPR_ORGANIZATION_ADDRESS", 0, 255 )
			, createFD( OP, "organizationAddressDefault",	"ORG_ADDRESS_DEFAULT",		"DPR_ORGANIZATION_ADDRESS_DEFAULT", 0, 255 )
			, createFD( OP, "organizationPhone",			"ORG_PHONE",		"DPR_ORGANIZATION_PHONE", 0, 35 )
			, createFD( OP, "organizationFax",			"ORG_FAX",				"DPR_ORGANIZATION_FAX", 0, 35 )
			, createFD( OP, "organizationPostCode",		"ORG_POST_CODE",		"DPR_ORGANIZATION_POST_CODE", 0, 15 )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_COUNTRY_COND, table = createTable("DPR_COUNTRY_COND", "CCND", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_COUNTRY_COND, new QueryableImpl(table) {{
			append( makeCountryAuthQueryableFields(TYPE_NONE, "CCND") );
			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "countryName"
						, "(SELECT SB.COUNTRYNAME FROM DPR_COUNTRY SB WHERE SB.COUNTRY_CD = CCND.COUNTRYCD)", "DPR_COUNTRY_NAME" )
				, new QueryableFieldImplBK( STRING, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = CCND.ORGANIZATIONCD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_ORGANIZATION_NAME" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_COUNTRY_DIST
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_COUNTRY_DIST, table = createTable("DPR_COUNTRY_DIST", "CDIS", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_COUNTRY_DIST, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( STRING, "distributionChannelName"
						, "( SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = CDIS.DIST_CHANNELCD"
							+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = ?)", "displayLanguage", "DPR_DISTRIBUTIONCHANNEL_NAME" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_COUNTRY_AUTH
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( PM, "authUniqId",			"UNIQID",					"DPR_COUNTRYAUTH_UNIQID",		0, 30 )
			, createFD( OP, "partyAll",				"PARTY_ALL",				"DPR_COUNTRYAUTH_PARTYALL",		"DPR_PARTYALL_", "Y,N" )
			, createFD( MD, "authValue",			"AUTHVALUE",				"DPR_COUNTRYAUTH_AUTHVALUE",	0, 5 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_COUNTRY_AUTH, table = createTable("DPR_COUNTRY_AUTH", "CAUT", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_COUNTRY_AUTH, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "authUserName", "(SELECT SB.USER_NAME FROM USR_USER SB WHERE SB.UNIQID = CAUT.UNIQID)", "DPR_COUNTRY_AUTH_AUTHUSERNAME" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_COUNTRY_CATE
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( PM, "categoryCode",			"CATE_CD",					"DPR_COUNTRYCATE_CATEGORYCODE",	20, 20 )
			, createFD( MD, "classCode",			"CLASSCD",					"DPR_COUNTRYCATE_CLASSCODE",	1, 1 )
			, createFD( MD, "categoryName",			"CATE_NAME",				"DPR_COUNTRYCATE_NAME",			DESC, 0, 128 )
			, createFD( OP, "manageUserId",			"MNGUSERID",				"USERID",						0, 30 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_COUNTRY_CATE, table = createTable("DPR_COUNTRY_CATE", "CCAT", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_PARTY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyCode",			"PARTY_CD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( MD, "partyType",			"PARTY_TYPE",				"DPR_PARTY_PARTYTYPE",			"DPR_PARTYTYPE_", "DIS,CUS" )
			, createFD( OP, "internalPartyCode",	"INTERNAL_PARTYCD",			"DPR_PARTY_INTERNALPARTYCODE",	0, 15 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_PARTY_COUNTRYCODE",		0, 15 )
			, createFD( MD, "countryKey",			"COUNTRYKEY",				"DPR_COUNTRY_COUNTRYKEY",			0, 15 )
			, createFD( OP, "parentPartyCode",		"PARENT_PARTYCD",			"DPR_PARTY_PARENTPARTYCODE",	0, 15 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_PARTY, table = createTable("DPR_PARTY", "PTY", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_PARTY_SALES
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_PARTY_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_PARTY_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_PARTY_DIVISION_CODE",			0, 8 )
			, createFD( MD, "partyName",			"PARTYNAME",				"DPR_PARTY_SALES_PARTYNAME",	DESC, 0, 512 )
			, createFD( OP, "additionalPartyName",	"PARTYNAME_ADDITION",		"DPR_PARTY_SALES_PARTYADDITIONALNAME",	DESC, 0, 512 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( MD, "countryKey",			"COUNTRYKEY",				"DPR_REGION_COUNTRYKEY",		0, 3 )
			, createFD( OP, "parentPartyCode",		"PARENT_PARTYCD",			"DPR_PARTY_PARENTPARTYCODE",	0, 15 )
			, createFD( MD, "regionCode",			"REGIONCD",					"DPR_PARTY_REGION_CODE",		0, 8 )
			, createFD( OP, "districtCode",			"DISTRICTCD",				"DPR_PARTY_SALESDISTRICT_CODE",	0, 8 )
			, createFD( OP, "deliveryPlant",		"DELIVERY_PLANT",			"DPR_PARTY_SALES_DELIVERYPLANT",	0, 4 )
			, createFD( OP, "groupCode",			"GROUPCD",					"DPR_PARTY_SALESGROUP_CODE",	0, 8 )
			, createFD( OP, "officeCode",			"OFFICECD",					"DPR_PARTY_SALESOFFICE_CODE",	0, 8 )
			, createFD( OP, "customerGroupCode",	"CUSTOMERGRPCD",			"DPR_PARTY_CUSTOMERGROUP_CODE",	0, 8 )
			, createFD( OP, "city",					"CITY",						"DPR_PARTY_SALES_CITY",			0, 35 )
			, createFD( OP, "addressCode",			"ADDRESSCD",				"DPR_CUSTOMERADDRESS_CODE",		0, 10 )
			, createFD( OP, "extraAddress1",		"EXTRA_ADDRESS1",			"DPR_PARTY_SALES_EXTRAADDRESS1",	0, 35 )
			, createFD( OP, "extraAddress2",		"EXTRA_ADDRESS2",			"DPR_PARTY_SALES_EXTRAADDRESS2",	0, 35 )
			, createFD( OP, "postalCode",			"POSTAL_CD",				"DPR_PARTY_SALES_POSTALCODE",	0, 15 )
			, createFD( OP, "tax1",					"TAX1",						"DPR_PARTY_SALES_TAX1",			0, 20 )
			, createFD( OP, "tax2",					"TAX2",						"DPR_PARTY_SALES_TAX2",			0, 15 )
			, createFD( OP, "contactUserID",		"CONTACT_USERID",			"DPR_PARTY_SALES_CONTACTUSERID",	0, 30 )
			, createFD( RD, "notOrderable",			"NOT_ORDERABLE",			"DPR_PARTY_SALES_NOT_ORDERABLE",	"PUB_WHETHER_", "Y,N" )
			, createFD( MD, "allowUOM",				"ALLOW_UOM",				"DPR_PARTY_SALES_ALLOW_UOM",	0, 50 )
			, createFD( OP, "transportZone",		"TRANSPORT_ZONE",			"DPR_PARTY_SALES_TRANSPORTZONE",	0, 50 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0,30 )
		};
		putTable( DPR_PARTY_SALES, table = createTable("DPR_PARTY_SALES", "PTYS", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PARTY_SALES, new QueryableImpl(table) {{
			Joinable tbl_PTY = new JoinableImpl( "PTY", "DPR_PARTY", "PTY.PARTY_CD = PTYS.PARTYCD" );
			Joinable tbl_PTYNAME = new JoinableImpl( "PTYS_1", "DPR_PARTY_SALES"
					, "PTYS_1.PARTYCD(+) = PTYS.PARENT_PARTYCD AND PTYS_1.ORGANIZATIONCD(+) = PTYS.ORGANIZATIONCD"
						+ " AND PTYS_1.DIST_CHANNELCD(+) = PTYS.DIST_CHANNELCD AND PTYS_1.DIVISIONCD(+) = PTYS.DIVISIONCD" );
			Joinable tbl_CADR = new JoinableImplBK( "CADR", "DPR_CUSTOMER_ADDRESS"
					, "CADR.ADDRESS_CD(+) = PTYS.ADDRESSCD AND CADR.LANGCD(+) = ?", "displayLanguage" );
			Joinable tbl_CADR_D = new JoinableImpl( "CADR_D", "DPR_CUSTOMER_ADDRESS"
					, "CADR_D.ADDRESS_CD(+) = PTYS.ADDRESSCD AND CADR_D.LANGCD(+) = 'xx'" );
			Joinable addrJoinable = new JoinableWrapper( tbl_CADR, tbl_CADR_D );

			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "customerName", "NVL(CADR.ADDRESS1, NVL(CADR_D.ADDRESS1, PTYS.PARTYNAME))", "DPR_PARTY_SALES_PARTYNAME" , addrJoinable )
				, new QueryableFieldImpl( STRING, "managerName", "NVL(CADR.ADDRESS2, CADR_D.ADDRESS2)", "DPR_PARTY_SALES_MANAGERNAME" , addrJoinable )
				, new QueryableFieldImpl( STRING, "address1", "NVL(CADR.ADDRESS3, NVL(CADR_D.ADDRESS3, PTYS.EXTRA_ADDRESS1))", "DPR_PARTY_SALES_EXTRAADDRESS1" , addrJoinable )
				, new QueryableFieldImpl( STRING, "address2", "NVL(CADR.ADDRESS4, NVL(CADR_D.ADDRESS4, PTYS.EXTRA_ADDRESS2))", "DPR_PARTY_SALES_EXTRAADDRESS2" , addrJoinable )
			});

			append( makeCountryAuthQueryableFields(TYPE_NONE, "PTYS") );
			append( makePartyAuthQueryableFields(TYPE_NONE, "PTYS") );
			append( new QueryableField[] {
//						new QueryableFieldImpl( STRING, "parentPartyName", "PTY.PARENT_PARTYCD", tbl_PTYNAME )
				  new QueryableFieldImpl( STRING, "internalPartyCode", "PTY.INTERNAL_PARTYCD", "DPR_PARTY_INTERNALPARTYCODE" , tbl_PTY )
				, new QueryableFieldImpl( STRING, "partyNameFull"
						, "((CASE WHEN LENGTHB(REGEXP_REPLACE(PTYS.PARTYNAME, '[[:punct:]]|[0-9a-zA-Z\\ ]')) > 0 THEN PTYS.PARTYNAME ELSE PTYS.PARTYNAME || ' ' END)"
										  + "|| DECODE(PTYS.PARTYNAME_ADDITION, '-', NULL, NULL, NULL, PTYS.PARTYNAME_ADDITION))", "DPR_PARTY_SALES_PARTYNAME" )
				, new QueryableFieldImpl( STRING, "countryISOName"
						, "(SELECT SB.COUNTRY_NAME FROM SYS_COUNTRY SB WHERE SB.COUNTRY_CD = PTYS.COUNTRYKEY)", "DPR_PARTY_COUNTRYISO_NAME" )
				, new QueryableFieldImplBK( STRING, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS.ORGANIZATIONCD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_ORGANIZATION_NAME" )
				, new QueryableFieldImplBK( STRING, "distributionChannelName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS.DIST_CHANNELCD"
							+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_DISTRIBUTIONCHANNEL_NAME" )
				, new QueryableFieldImplBK( STRING, "divisionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS.DIVISIONCD"
							+ " AND SB.MASTER_TYPE = 'DI' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_DIVISION_NAME" )
				, new QueryableFieldImplBK( STRING, "regionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS.REGIONCD || ';' || PTYS.COUNTRYKEY"
							+ " AND SB.MASTER_TYPE = 'RG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_REGION_NAME" )
				, new QueryableFieldImplBK( STRING, "districtName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS.DISTRICTCD"
							+ " AND SB.MASTER_TYPE = 'SD' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_SALESDISTRICT_NAME" )
				, new QueryableFieldImplBK( STRING, "officeName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS.OFFICECD"
							+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_SALESOFFICE_NAME" )
				, new QueryableFieldImplBK( STRING, "groupName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS.GROUPCD"
							+ " AND SB.MASTER_TYPE = 'SG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_SALESGROUP_NAME" )
				, new QueryableFieldImplBK( STRING, "customerGroupName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS.CUSTOMERGRPCD"
							+ " AND SB.MASTER_TYPE = 'CG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_CUSTOMERGROUP_NAME" )
				, new QueryableFieldImpl( STRING, "contactUserName"
						, "(SELECT USER_NAME FROM USR_USER SB WHERE SB.UNIQID = PTYS.CONTACT_USERID)", "DPR_PARTY_CONTACTUSER_NAME" )
			} );

			// JDMS
			Joinable tbl_PJDMS = new JoinableImpl( "PJDMS", "DPR_PARTY_JDMS"
					, "PJDMS.PARTYCD(+) = PTYS.PARTYCD AND PJDMS.ORGANIZATIONCD(+) = PTYS.ORGANIZATIONCD"
					+ " AND PJDMS.DIST_CHANNELCD(+) =  PTYS.DIST_CHANNELCD AND PJDMS.DIVISIONCD(+) = PTYS.DIVISIONCD" );
			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "JDMSIndicator", "NVL2(PJDMS.PARTYCD, 'Y', 'N')", "DPR_PARTY_SALES_JDMS_IND", tbl_PJDMS )
			} );

			// oracle 11gR2
			append( new QueryableFieldImpl[] {
					new QueryableFieldImpl( STRING, "defaultLinkType", "(SELECT LISTAGG(SB.LINKTYPE, ',') WITHIN GROUP( ORDER BY LINKTYPE) FROM DPR_PARTY_LINK SB"
							+ " WHERE SB.COUNTRYCD = PTYS.COUNTRYCD"
							+ " AND SB.ORGANIZATIONCD = PTYS.ORGANIZATIONCD AND SB.DIST_CHANNELCD = PTYS.DIST_CHANNELCD"
							+ " AND SB.DIVISIONCD = PTYS.DIVISIONCD AND SB.LINK_PARTYCD = PTYS.PARTYCD AND SB.PARTYCD = PTYS.PARTYCD)"
			 )
			});
		}} );


		/***************************************************************************************************
			*	DPR_PARTY_CREDIT
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "soldPartyCode",		"SOLD_PARTYCD",					"DPR_SOLD_PARTYCODE",				0, 15 )
			, createFD( PM, "creditPartyCode",		"CREDIT_PARTYCD",					"DPR_PAYER_PARTYCODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_PARTY_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",		"DIST_CHANNELCD",					"DPR_SOLD_PARTYCODE",				0, 8 )
			, createFD( MD, "creditPartyName",			"CREDIT_PARTYNAME",				"DPR_PAYER_PARTYNAME",	DESC, 0, 512 )
			, createFD( MD, "creditLimit",			"CREDIT_LIMIT",				"DPR_PARTY_CREDIT_CREDIT_LIMIT",			DOUBLE )
			, createFD( OP, "creditLimitCrcy",			"CREDIT_LIMIT_CRCY",				"DPR_PARTY_CREDIT_CREDIT_LIMIT",			DOUBLE )
			, createFD( MD, "accountReceivable",			"ACCOUNT_RECEIVABLE",				"DPR_PARTY_CREDIT_ACCOUNT_RECEIVABLE_BALANCE",			DOUBLE )
			, createFD( OP, "accountReceivableCrcy",			"ACCOUNT_RECEIVABLE_CRCY",				"DPR_PARTY_CREDIT_ACCOUNT_RECEIVABLE_BALANCE",			DOUBLE )
			, createFD( MD, "creditExposure",			"CREDIT_EXPOSURE",				"DPR_PARTY_CREDIT_CREDIT_EXPOSURE",			DOUBLE )
			, createFD( OP, "creditExposureCrcy",			"CREDIT_EXPOSURE_CRCY",				"DPR_PARTY_CREDIT_CREDIT_EXPOSURE",			DOUBLE )
			, createFD( OP, "creditCurrency",			"CREDIT_CURRENCY",				"DPR_PARTY_CREDIT_CREDIT_CURRENCY",			0, 4 )
			, createFD( MD, "creditRefDateTime",		"CREDIT_REF_DATE",					"DPR_CREDIT_REFRENCE_DATETIME",				DATE )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_PARTY_CREDIT, table = createTable("DPR_PARTY_CREDIT", "PTYC", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PARTY_CREDIT, queryable = new QueryableImpl(table) {{

			append( new QueryableField[] {
					  new QueryableFieldImpl( STRING, "soldPartyName"
					    , "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						  +" WHERE SB.PARTYCD = PTYC.SOLD_PARTYCD AND SB.ORGANIZATIONCD = PTYC.ORGANIZATIONCD"
						  +" AND SB.DIST_CHANNELCD = PTYC.DIST_CHANNELCD AND SB.DIVISIONCD = '10')" )
					, new QueryableFieldImpl( STRING, "creditPartyName"
					  , "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						  +" WHERE SB.PARTYCD = PTYC.CREDIT_PARTYCD AND SB.ORGANIZATIONCD = PTYC.ORGANIZATIONCD"
						  +" AND SB.DIST_CHANNELCD = PTYC.DIST_CHANNELCD AND SB.DIVISIONCD = '10')" )
					, new QueryableFieldImpl( STRING, "creditRiskInd"
					  , "DECODE(CREDIT_CURRENCY,NULL,(CASE WHEN CREDIT_LIMIT < CREDIT_EXPOSURE THEN 'Y' ELSE 'N' END)"
						  + ",(CASE WHEN CREDIT_LIMIT_CRCY < CREDIT_EXPOSURE_CRCY THEN 'Y' ELSE 'N' END))" )
				} );

		}} );

		/***************************************************************************************************
			*	DPR_PARTY_AUTH
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( PM, "uniqId",				"UNIQID",					"USERID",						0, 30 )
			, createFD( MD, "authValue",			"AUTHVALUE",				"DPR_PARTYAUTH_AUTHVALUE",		0, 5 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( OP, "applyType",			"APPLYTYPE",				"DPR_PARTYAUTH_APPLYTYPE",		"DPR_APPLYTYPE_", "C,S" )
			, createFD( OP, "source",				"SOURCE",					"DPR_PARTYAUTH_SOURCE",			"DPR_SOURCE", "S,D" )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PARTY_AUTH, table = createTable("DPR_PARTY_AUTH", "PAUT", tfields, "UPGDATE = SYSDATE") );

		/***************************************************************************************************
			*	DPR_PARTY_LINK
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "linkType",				"LINKTYPE",					"DPR_PARTYLINK_LINKTYPE",		2, 2 )
				.setValidValueList("DPR_PARTY_LINKTYPE_", "AG,WE,RE,RG")
			, createFD( PM, "linkPartyCode",		"LINK_PARTYCD",				"DPR_PARTYLINK_LINKPARTYCODE",	0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( MD, "displaySequence",		"DISPLAY_SEQ",				"DPR_PARTYLINK_DISPLAY_SEQUENCE", INTEGER )
			, createFD( MR, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PARTY_LINK, table = createTable("DPR_PARTY_LINK", "PLNK", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PARTY_LINK, queryable = new QueryableImpl(table) {{
			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES" , "PTYS.PARTYCD(+) = PLNK.PARTYCD" );

			QueryBufferValid validG_PLNK = new QueryBufferValid.GroupKey("countryCode", "divisionCode", "organizationCode"
					, "distributionChannelCode", "partyCode", "linkType");
			append( ConditionalQueryableField.makeConditionalQueryableFields(validG_PLNK, new QueryableFieldImpl[]{
					  new QueryableFieldImpl( INTEGER, "maxDisplaySequence", "NVL(MAX(DISPLAY_SEQ), 0)" )
					, new QueryableFieldImpl( INTEGER, "nextDisplaySequence", "NVL(MAX(DISPLAY_SEQ)+1, 0)" )
			}) );

			QueryableField[] qfields = new QueryableField[] {
						new QueryableFieldImpl( STRING, "organizationCode", "PTYS.ORGANIZATIONCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "divisionCode", "PTYS.DIVISIONCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "distributionChannelCode", "PTYS.DIST_CHANNELCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "countryCode", "PTYS.COUNTRYCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "officeCode", "PTYS.OFFICECD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "groupCode", "PTYS.GROUPCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "partyName", "PTYS.PARTYNAME", "DPR_PARTY_SALES_PARTYNAME", tbl_PTYS )
				, new QueryableFieldImpl( CODE, "partyStatus", "PTYS.STATUS", "DPR_PARTY_PARTYSTATUS", tbl_PTYS )
				, new QueryableFieldImpl( CODE, "partyNotOrderable", "PTYS.NOT_ORDERABLE", "DPR_PARTY_SALES_NOT_ORDERABLE", tbl_PTYS )
			};


			QueryBufferValid valid_PTYS = new QueryBufferValid.Condition( "organizationCode", "divisionCode", "distributionCode", "countryCode" );
			append( ConditionalQueryableField.makeConditionalQueryableFields(valid_PTYS, qfields) );

			qfields = makePartyAuthQueryableFields(TYPE_NONE, "PTYS");
			append( ConditionalQueryableField.makeConditionalQueryableFields(valid_PTYS, qfields) );

			Joinable tbl_CDIS = new JoinableImplBK( "CDIS", "DPR_COUNTRY_DIST", "CDIS.COUNTRYCD = ?", new String[] { "countryCode" } );
			Joinable tbl_PTYS_LNK = new JoinableImpl( "PTYS_LNK", "DPR_PARTY_SALES"
					, "PTYS_LNK.PARTYCD = PLNK.LINK_PARTYCD"
							+ " AND PTYS_LNK.ORGANIZATIONCD = PTYS.ORGANIZATIONCD"
							+ " AND PTYS_LNK.DIVISIONCD = PTYS.DIVISIONCD"
							+ " AND PTYS_LNK.DIST_CHANNELCD = PTYS.DIST_CHANNELCD"
							+ " AND PTYS.ORGANIZATIONCD = PLNK.ORGANIZATIONCD"
							+ " AND PTYS.DIVISIONCD = PLNK.DIVISIONCD"
							+ " AND PTYS.DIST_CHANNELCD = PLNK.DIST_CHANNELCD"
					, tbl_PTYS );

			Joinable tbl_CADR = new JoinableImplBK( "CADR", "DPR_CUSTOMER_ADDRESS"
					, "CADR.ADDRESS_CD(+) = PTYS_LNK.ADDRESSCD AND CADR.LANGCD(+) = ?", "displayLanguage" );
			Joinable tbl_CADR_D = new JoinableImpl( "CADR_D", "DPR_CUSTOMER_ADDRESS"
					, "CADR_D.ADDRESS_CD(+) = PTYS_LNK.ADDRESSCD AND CADR_D.LANGCD(+) = 'xx'" );
			Joinable addrJoinable = new JoinableWrapper( tbl_PTYS_LNK, tbl_CADR, tbl_CADR_D );

			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "linkPartyCode", "PTYS_LNK.PARTYCD", "DPR_PARTY_CODE", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkStatus", "PTYS_LNK.STATUS", "STATUS", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkPartyName", "NVL(CADR.ADDRESS1, NVL(CADR_D.ADDRESS1, PTYS_LNK.PARTYNAME))", "DPR_PARTYLINK_LINKPARTYNAME", addrJoinable )
				, new QueryableFieldImpl( STRING, "linkOrganizationCode", "PTYS_LNK.ORGANIZATIONCD", "DPR_PARTY_SALESORGANIZATION_CODE", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkDivisionCode", "PTYS_LNK.DIVISIONCD", "DPR_PARTY_DIVISION_CODE", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkDistributionChannelCode", "PTYS_LNK.DIST_CHANNELCD", "DPR_PARTY_DISTRIBUTIONCHANNEL_CODE", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkRegionCode", "PTYS_LNK.REGIONCD", "DPR_PARTY_REGION_CODE", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkCustomerGroupCode", "PTYS_LNK.CUSTOMERGRPCD", "DPR_PARTY_CUSTOMERGROUP_CODE", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkDistrictCode", "PTYS_LNK.DISTRICTCD", "DPR_PARTY_SALESDISTRICT_CODE", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkOfficeCode", "PTYS_LNK.OFFICECD", "DPR_PARTY_SALESOFFICE_CODE", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkGroupCode", "PTYS_LNK.GROUPCD", "DPR_PARTY_SALESGROUP_CODE", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkDeliveryPlant", "PTYS_LNK.DELIVERY_PLANT", "DPR_PARTY_SALES_DELIVERYPLANT", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "linkDeliveryPlant", "PTYS_LNK.DELIVERY_PLANT", "DPR_PARTY_SALES_DELIVERYPLANT", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "countryKey", "PTYS_LNK.COUNTRYKEY", "DPR_REGION_COUNTRYKEY", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "postalCode", "PTYS_LNK.POSTAL_CD", "DPR_PARTY_SALES_POSTAL_CD", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "city", "NVL(CADR.CITY, NVL(CADR_D.CITY, PTYS_LNK.CITY))", "DPR_PARTY_SALES_CITY", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "extraAddress1", "NVL(CADR.ADDRESS3, NVL(CADR_D.ADDRESS3, PTYS_LNK.EXTRA_ADDRESS1))", "DPR_PARTY_SALES_EXTRAADDRESS1", addrJoinable )
				, new QueryableFieldImpl( STRING, "extraAddress2", "NVL(CADR.ADDRESS4, NVL(CADR_D.ADDRESS4, PTYS_LNK.EXTRA_ADDRESS2))", "DPR_PARTY_SALES_EXTRAADDRESS2", addrJoinable )
				, new QueryableFieldImpl( STRING, "countryISOName"
						, "(SELECT SB.COUNTRY_NAME FROM SYS_COUNTRY SB WHERE SB.COUNTRY_CD = PTYS_LNK.COUNTRYKEY)", "DPR_PARTY_COUNTRYISO_NAME", tbl_PTYS_LNK )
				, new QueryableFieldImplBK( STRING, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS_LNK.ORGANIZATIONCD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_ORGANIZATION_NAME", tbl_PTYS_LNK )
				, new QueryableFieldImplBK( STRING, "distributionChannelName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS_LNK.DIST_CHANNELCD"
							+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_DISTRIBUTIONCHANNEL_NAME", tbl_PTYS_LNK )
				, new QueryableFieldImplBK( STRING, "divisionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS_LNK.DIVISIONCD"
							+ " AND SB.MASTER_TYPE = 'DI' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_DIVISION_NAME", tbl_PTYS_LNK )
				, new QueryableFieldImplBK( STRING, "regionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS_LNK.REGIONCD || ';' || PTYS_LNK.COUNTRYKEY"
							+ " AND SB.MASTER_TYPE = 'RG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_REGION_NAME", tbl_PTYS_LNK )
				, new QueryableFieldImplBK( STRING, "districtName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS_LNK.DISTRICTCD"
							+ " AND SB.MASTER_TYPE = 'SD' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_SALESDISTRICT_NAME", tbl_PTYS_LNK )
				, new QueryableFieldImplBK( STRING, "officeName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS_LNK.OFFICECD"
							+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_SALESOFFICE_NAME", tbl_PTYS_LNK )
				, new QueryableFieldImplBK( STRING, "groupName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS_LNK.GROUPCD"
							+ " AND SB.MASTER_TYPE = 'SG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_SALESGROUP_NAME", tbl_PTYS_LNK )
				, new QueryableFieldImplBK( STRING, "customerGroupName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS_LNK.CUSTOMERGRPCD"
							+ " AND SB.MASTER_TYPE = 'CG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_CUSTOMERGROUP_NAME", tbl_PTYS_LNK )
				, new QueryableFieldImpl( STRING, "contactUserName"
						, "(SELECT USER_NAME FROM USR_USER SB WHERE SB.UNIQID = PTYS_LNK.CONTACT_USERID)", "DPR_PARTY_CONTACTUSER_NAME", tbl_PTYS_LNK )
				//, new QueryableFieldImpl( STRING, "contactUserName", "NVL(CADR.ADDRESS2, CADR_D.ADDRESS2)", "DPR_PARTY_CONTACTUSER_NAME" , addrJoinable )
			} );

			append( new QueryableField[] {
						new QueryableFieldImplBK( STRING, "linkPartyName"
							, "(SELECT PARTYNAME FROM DPR_PARTY_SALES SB WHERE SB.PARTYCD = PLNK.LINK_PARTYCD AND SB.ORGANIZATIONCD = ? AND SB.DIVISIONCD = ? AND SB.DIST_CHANNELCD = ? )", new String[] { "organizationCode", "divisionCode", "distributionChannelCode" } )
			} );
		}} );


		/***************************************************************************************************
		*	DPR_PARTY_FUNCTION
		***************************************************************************************************/
		putTable( DPR_PARTY_FUNCTION, table);
		putQueryable(DPR_PARTY_FUNCTION, queryable);



		/***************************************************************************************************
			*	DPR_PARTY_EMPLOYEE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "employeeId",			"EMPLOYEE_ID",				"DPR_PARTYAUTH_SOURCE",			0, 8 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PARTY_EMPLOYEE, table = createTable("DPR_PARTY_EMPLOYEE", "PEMP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PARTY_EMPLOYEE, new QueryableImpl(table) {{
			Joinable tbl_CDIS = new JoinableImplBK( "CDIS", "DPR_COUNTRY_DIST", "CDIS.COUNTRYCD = ?", new String[] { "countryCode" } );
			Joinable tbl_PTYS = new JoinableImplBK( "PTYS", "DPR_PARTY_SALES", "PTYS.PARTYCD = PEMP.PARTYCD"
					+ " AND PTYS.COUNTRYCD = PEMP.COUNTRYCD AND PTYS.ORGANIZATIONCD = PEMP.ORGANIZATIONCD AND PTYS.DIVISIONCD = PEMP.DIVISIONCD"
					+ " AND PTYS.DIST_CHANNELCD = CDIS.DIST_CHANNELCD AND PTYS.ORGANIZATIONCD = CDIS.ORGANIZATIONCD"
					+ " AND PTYS.ORGANIZATIONCD = ? AND PTYS.DIVISIONCD = ?"
					, new String[] { "organizationCode", "divisionCode" }, tbl_CDIS );

			Joinable tbl_PLNK = new JoinableImpl( "PLNK", "DPR_PARTY_LINK", "PLNK.PARTYCD(+) = PTYS.PARTYCD"
					+ " AND PLNK.LINK_PARTYCD(+) = PTYS.PARTYCD AND PLNK.ORGANIZATIONCD(+) = PTYS.ORGANIZATIONCD"
					+ " AND PLNK.DIST_CHANNELCD(+) = PTYS.DIST_CHANNELCD AND PLNK.DIVISIONCD(+) = PTYS.DIVISIONCD", tbl_PTYS );

			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "partyStatus", "PTYS.STATUS", tbl_PTYS )
				, new QueryableFieldImpl( STRING, false, "organizationCode", "PTYS.ORGANIZATIONCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, false, "divisionCode", "PTYS.DIVISIONCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, true, "distributionChannelCode", "PTYS.DIST_CHANNELCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "linkType", "PLNK.LINKTYPE", tbl_PLNK )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_PARTY_JDMS
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_PARTY_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_PARTY_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_PARTY_DIVISION_CODE",			0, 8 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PARTY_JDMS, table = createTable("DPR_PARTY_JDMS", "PJDMS", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PARTY_JDMS, new QueryableImpl(table) {{
			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES", "PJDMS.PARTYCD = PTYS.PARTYCD"
					+ " AND PJDMS.ORGANIZATIONCD = PTYS.ORGANIZATIONCD AND PJDMS.DIST_CHANNELCD = PTYS.DIST_CHANNELCD"
					+ " AND PJDMS.DIVISIONCD = PTYS.DIVISIONCD"
			);

			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "partyName", "PTYS.PARTYNAME", tbl_PTYS )
				, new QueryableFieldImplBK( STRING, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PJDMS.ORGANIZATIONCD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_ORGANIZATION_NAME" )
				, new QueryableFieldImplBK( STRING, "distributionChannelName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PJDMS.DIST_CHANNELCD"
							+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_DISTRIBUTIONCHANNEL_NAME" )
				, new QueryableFieldImplBK( STRING, "divisionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PJDMS.DIVISIONCD"
							+ " AND SB.MASTER_TYPE = 'DI' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_DIVISION_NAME" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_BASEPRODUCT
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "code",					"BASEPRODUCT_CD",			"DPR_BASEPRODUCT_CODE",			0, 15 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_BASEPRODUCT, table = createTable("DPR_BASEPRODUCT", "BPD", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_BASEPRODUCT, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "name"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = BPD.BASEPRODUCT_CD"
							+ " AND SB.MASTER_TYPE = 'BP' AND SB.LANGCD = ?)", "displayLanguage", "DPR_BASEPRODUCT_NAME" )
				, new QueryableFieldImplBK( DESC, "description"
						, "(SELECT MASTER_DESC FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = BPD.BASEPRODUCT_CD"
							+ " AND SB.MASTER_TYPE = 'BP' AND SB.LANGCD = ?)", "displayLanguage", "DPR_BASEPRODUCT_DESCRIPTION" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_MEGABRAND
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "code",					"MEGABRAND_CD",				"DPR_MEGABRAND_CODE",			0, 15 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_MEGABRAND, table = createTable("DPR_MEGABRAND", "MBR", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_MEGABRAND, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "name"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = MBR.MEGABRAND_CD"
							+ " AND SB.MASTER_TYPE = 'MB' AND SB.LANGCD = ?)", "displayLanguage", "DPR_MEGABRAND_NAME" )
				, new QueryableFieldImplBK( DESC, "description"
						, "(SELECT MASTER_DESC FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = MBR.MEGABRAND_CD"
							+ " AND SB.MASTER_TYPE = 'MB' AND SB.LANGCD = ?)", "displayLanguage", "DPR_MEGABRAND_DESCRIPTION" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_BRAND
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "code",					"BRAND_CD",					"DPR_BRAND_CODE",				0, 15 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_BRAND, table = createTable("DPR_BRAND", "BRD", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_BRAND, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "name"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = BRD.BRAND_CD"
							+ " AND SB.MASTER_TYPE = 'BR' AND SB.LANGCD = ?)", "displayLanguage", "DPR_BRAND_NAME" )
				, new QueryableFieldImplBK( DESC, "description"
						, "(SELECT MASTER_DESC FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = BRD.BRAND_CD"
							+ " AND SB.MASTER_TYPE = 'BR' AND SB.LANGCD = ?)", "displayLanguage", "DPR_BRAND_DESCRIPTION" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_VARIANT
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "code",					"VARIANT_CD",				"DPR_VARIANT_CODE",				0, 15 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_VARIANT, table = createTable("DPR_VARIANT", "VAR", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_VARIANT, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "name"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = VAR.VARIANT_CD"
							+ " AND SB.MASTER_TYPE = 'VA' AND SB.LANGCD = ?)", "displayLanguage", "DPR_VARIANT_NAME" )
				, new QueryableFieldImplBK( DESC, "description"
						, "(SELECT MASTER_DESC FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = VAR.VARIANT_CD"
							+ " AND SB.MASTER_TYPE = 'VA' AND SB.LANGCD = ?)", "displayLanguage", "DPR_VARIANT_DESCRIPTION" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_PUTUP
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "code",					"PUTUP_CD",					"DPR_PUTUP_CODE",				0, 15 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_PUTUP, table = createTable("DPR_PUTUP", "PUP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PUTUP, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "name"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PUP.PUTUP_CD"
							+ " AND SB.MASTER_TYPE = 'PU' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PUTUP_NAME" )
				, new QueryableFieldImplBK( DESC, "description"
						, "(SELECT MASTER_DESC FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PUP.PUTUP_CD"
							+ " AND SB.MASTER_TYPE = 'PU' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PUTUP_DESCRIPTION" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_PRODUCT_CATE
		***************************************************************************************************/
		Table.Field fld_classcd = createFD( MR, "classCode", "CLASSCD", "CLASSCODE", "DPR_PRODUCT_CATEGORY_CLASS_", "1,2,3,4,5,6" );
		HierarchyCodeField fld_cd = new HierarchyCodeField( createFD(PM, "code", "PCATE_CD", "CODE", 0, 20, CHARS), fld_classcd, new int[] { 1, 2, 6, 10, 14, 18 } );
		tfields = new Table.Field[] {
					fld_cd, fld_classcd
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_PRODUCT_CATE, table = createTable("DPR_PRODUCT_CATE", "PRC", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PRODUCT_CATE, new QueryableImpl(table, new QueryableField[]{
				  new QueryableFieldImpl( INTEGER, "lowerCount"
						, "(SELECT COUNT(*) FROM DPR_PRODUCT_CATE SB WHERE SB.PCATE_CD LIKE PRC.PCATE_CD || '%'"
									+ " AND SB.CLASSCD = TO_CHAR(TO_NUMBER(PRC.CLASSCD)+1) )", "LOWERCOUNT" )
				, new QueryableFieldImplBK( DESC, "name"
						, "(SELECT MASTER_NAME FROM "+Schema.masterDescOrgQuery+" SB WHERE SB.MASTER_CD = PRC.PCATE_CD"
							+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ?)", Schema.masterDescOrgBKKeys , "DPR_PRODUCT_CATEGORY_NAME" )
				, new QueryableFieldImpl( STRING, "levelCode", fld_cd.getLevelCodeQuery(), "LEVELCODE" )
				, new QueryableFieldImplBK( DESC, "description"
						, "(SELECT MASTER_DESC FROM "+Schema.masterDescOrgQuery+"SB WHERE SB.MASTER_CD = PRC.PCATE_CD"
							+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ?)", Schema.masterDescOrgBKKeys, "DPR_PRODUCT_CATEGORY_DESCRIPTION" )
				, new QueryableFieldImpl( STRING, "pcateCode", "PRC.PCATE_CD", "DPR_PRODUCT_CATEGORY_CODE" )
				, new QueryableFieldImplBK( DESC, "pcateDescription"
						, "(SELECT MASTER_DESC FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PRC.PCATE_CD"
							+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_PRODUCT_CATEGORY_DESCRIPTION" )
		})

		{{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();

			QueryBufferValid valid_language = new QueryBufferValid.Condition("displayLanguage");
			Joinable tbl_MSTD = new JoinableImplBK( "MSTD", "DPR_MASTER_DESC"
					, "MSTD.MASTER_TYPE(+) = 'PC' AND MSTD.MASTER_CD(+) = "+baseAlias+".PCATE_CD AND MSTD.LANGCD(+) = ?", "displayLanguage" );
			appendCND( valid_language, new QueryableField[] {
					new QueryableFieldImpl( DESC, "pcateName", "MSTD.MASTER_NAME", "DPR_PRODUCT_CATEGORY_NAME", tbl_MSTD )
			});
		}} );


		/***************************************************************************************************
			*	DPR_ITEM_MASTER
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "itemCode",				"ITEM_CD",					"DPR_ITEM_MASTER_CODE",			0, 20 )
			, createFD( MD, "itemType",				"ITEM_TYPE",				"DPR_ITEM_MASTER_TYPE",			0, 5 )
			, createFD( OP, "baseUnitofMeasure",	"BASE_UOM",					"DPR_ITEM_MASTER_BASEUOM",		0, 5 )
			, createFD( OP, "salesUnit",			"SALES_UNIT",				"DPR_ITEM_MASTER_SALES_UNIT",	0, 5 )
			, createFD( MD, "productCategoryCode",	"PCATECD",					"DPR_ITEM_MASTER_PRODUCTCATEGORYCODE",	0, 20 )
			, createFD( RD, "startAvailableDate",	"STARTAVAIL_DATE",			"DPR_ITEM_MASTER_STARTAVAILE_DATE",	DATE )
			, createFD( RD, "endAvailableDate",		"ENDAVAIL_DATE",			"DPR_ITEM_MASTER_ENDDAVAILE_DATE",	DATE )
			, createFD( OP, "salesStatus",			"SALES_STATUS",				"DPR_ITEM_MASTER_SALESSTATUS",	0, 2 )
			, createFD( OP, "salesStatusFrom",		"SALES_STATUS_FROM",		"DPR_ITEM_MASTER_SALESSTATUS_FROM", 0, 8, "0123456789" )
			, createFD( OP, "shelfLife",			"SHELFLIFE",				"DPR_ITEM_MASTER_SHELFLIFE",	0, 4 )
			, createFD( OP, "dangerousInd",			"DANGEROUS_IND",			"DPR_ITEM_MASTER_DANGEROUSIND",	1, 1, "Y,N" )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_ITEM_MASTER, table = createTable("DPR_ITEM_MASTER", "IMT", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ITEM_MASTER, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();

			QueryBufferValid valid_language = new QueryBufferValid.Condition("displayLanguage");
			Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = "+baseAlias+".ITEM_CD AND IMTD.LANGCD(+) = ?", "displayLanguage" );
			appendCND( valid_language, new QueryableField[] {
						new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", "DPR_ITEM_MASTER_NAME", tbl_DESC )
			});
		}} );


		/***************************************************************************************************
		*	DPR_ITEM_MASTER_PLANT
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_MASTER_CODE",				0, 20 )
			, createFD( PM, "plantCode",			"PLANT",					"DPR_ITEM_MASTER_PLANT_CODE",		0, 4 )
			, createFD( OP, "plantStatus",			"PLANT_STATUS",				"DPR_ITEM_MASTER_PLANT_STATUS",		0, 4 )
			, createFD( OP, "plantStatusFrom",		"PLANT_STATUS_FROM",		"DPR_ITEM_MASTER_PLANT_STATUSFROM",	0, 24 )
			, createFD( OP, "loadingGroup",			"LOADING_GRP",				"DPR_ITEM_MASTER_PLANT_LOADGRP",	0, 4 )
			, createFD( MD, "manualInd",			"MANUAL_IND",				"DPR_ITEM_MASTER_PLANT_MANUALIND",	"DPR_ITEM_MASTER_PLANT_MANUAL_", "Y,N" )
			, createFD( RD, "status",				"STATUS",					"STATUS",							"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",					DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",					DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",						0, 30 )
		};
		putTable( DPR_ITEM_MASTER_PLANT, table = createTable("DPR_ITEM_MASTER_PLANT", "IMTP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ITEM_MASTER_PLANT, new QueryableImpl(table) {{

			QueryBufferValid valid_language = new QueryBufferValid.Condition("displayLanguage");
			Joinable tbl_IMT = new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMTP.ITEMCD = IMT.ITEM_CD" );
			Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = IMT.ITEM_CD AND IMTD.LANGCD(+) = ?", "displayLanguage", tbl_IMT );
			appendCND( valid_language, new QueryableField[] {
				  new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", "DPR_ITEM_MASTER_NAME", tbl_DESC )
				, new QueryableFieldImplBK( STRING, "dangerousInd", "DECODE(LOADING_GRP, ?, 'Y', 'N')", "dangerousNumber", "DPR_ITEM_MASTER_PLANT_DANGERIND" )
			});
		}
		/*
			@Override
			public boolean appendCondition(ConditionQueryBuffer querybuf) {
				boolean hasCondition = super.appendCondition(querybuf);

				if( querybuf.hasConditionValue("dangerousInd") ) {
					Object[] bindValues = { querybuf.getConditionValue("dangerousNumber"),  querybuf.getConditionValue("dangerousInd") };
					querybuf.appendCondition( "DECODE(IMTP.LOADING_GRP, ?, 'Y', 'N') = ?", bindValues );
					hasCondition = true;
				}
				return hasCondition;
			};
		*/
		} );


		/***************************************************************************************************
			*	DPR_ITEM_MASTER_DESC
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_MASTER_CODE",			0, 20 )
			, createFD( PM, "languageCode",			"LANGCD",					"DPR_LANGUMAGECODE",			2, 2 )
			, createFD( MD, "itemName",				"ITEMNAME",					"DPR_ITEM_MASTER_NAME",			DESC, 0, 512 )
			, createFD( OP, "additionalItemName",	"ITEMNAME_ADDITION",		"DPR_ITEM_MASTER_ADDITIONALNAME",	DESC, 0, 512 )
			, createFD( OP, "searchString",			"SEARCHSTRING",				"DPR_ITEM_MASTER_SEARCHSTRING",	DESC, 0, 512 )
			, createFD( OP, "additionalSearchString",	"SEARCHSTRING_ADDITION",	"DPR_ITEM_MASTER_ADDITIONALSEARCHSTRING",	DESC, 0, 512 )
			, createFD( OP, "description",			"DESCRIPTION",				"DPR_ITEM_MASTER_DESCRIPTION",	DESC, 0, 512 )
			, createFD( OP, "additionalDescription",	"DESCRIPTION_ADDITION",		"DPR_ITEM_MASTER_ADDITIONALDESCRIPTION",	DESC, 0, 512 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_ITEM_MASTER_DESC, table = createTable("DPR_ITEM_MASTER_DESC", "IMTD", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 * DPR_ITEM_MASTER_EXTRA
		 ***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",	"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "itemCode",				"ITEMCD",			"DPR_ITEM_CODE",				0, 20 )
			, createFD( MD, "itemExtraCate",		"ITEM_CATE",		"DPR_ITEMMASTEREXTRA_CATEGORY",		STRING )
			, createFD( MD, "itemExtraDesc",		"ITEM_NAME",		"DPR_ITEM_MASTER_NAME",				DESC, 0, 512 )
			, createFD( MD, "itemExtraAbbrev",		"ITEM_ABBREV",		"DPR_ITEMMASTEREXTRA_ABBREVIATION",	STRING )
			, createFD( MD, "itemExtraSpec",		"ITEM_SPEC",		"DPR_ITEMMASTEREXTRA_SPECIFICATION",	STRING )
			, createFD( MD, "uomNameLocal",			"UOM_NAME_LOCAL",	"DPR_ITEMMASTEREXTRA_UOMNAMELOCAL",	STRING, 0, 6 )
			, createFD( OP, "uomName",				"UOM_NAME",			"DPR_ITEMUOM_NAME",				STRING, 0, 6 )
			, createFD( RD, "createDateTime",		"REGDATE",			"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",			"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",		"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ITEM_MASTER_EXTRA, table = createTable("DPR_ITEM_MASTER_EXTRA", "IMTEXT", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_ITEM_MASTER_INTRO
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_MASTER_CODE",			0, 20 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "languageCode",			"LANGCD",					"DPR_LANGUMAGECODE",			2, 2 )
			, createFD( RD, "intro",				"INTRO",					"DPR_ITEM_MASTER_INTRO",			DESC, 0, 2048 )
			, createFD( OP, "editableIntro",		"EDIT_INTRO",				"DPR_ITEM_MASTER_EDITINTRO",		DESC, 0, 2048 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ITEM_MASTER_INTRO, table = createTable("DPR_ITEM_MASTER_INTRO", "IMTI", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_ITEM_MASTER_SALES
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_MASTER_CODE",			0, 20 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( RD, "sourcing",				"SOURCING",					"DPR_ITEM_MASTER_SALES_SOURCING",	"DPR_ITEM_SOUCING_", "S,U" )
			, createFD( RD, "startAvailDate",		"STARTAVAIL_DATE",			"DPR_ITEM_MASTER_SALES_STARTAVAILDATETIME",	DATE )
			, createFD( RD, "endAvailDate",			"ENDAVAIL_DATE",			"DPR_ITEM_MASTER_SALES_ENDAVAILDATETIME",	DATE )
			, createFD( OP, "priceCurrency",		"PRICE_CURR",				"DPR_ITEM_MASTER_SALES_PRICECURRENCY",	3, 3 )
			, createFD( OP, "price",				"PRICE",					"DPR_ITEM_MASTER_SALES_PRICE",	DOUBLE )
			, createFD( OP, "productCategoryCode",	"PCATECD",					"DPR_ITEM_MASTER_PRODUCTCATEGORYCODE",	0, 20 )
			, createFD( OP, "megaBrandCode",		"MEGABRANDCD",				"DPR_MEGABRAND_CODE",			0, 20 )
			, createFD( OP, "brandCode",			"BRANDCD",					"DPR_BRAND_CODE",				0, 20 )
			, createFD( OP, "baseProductCode",		"BASEPRODUCTCD",			"DPR_BASEPRODUCT_CODE",			0, 20 )
			, createFD( OP, "putupCode",			"PUTUPCD",					"DPR_PUTUP_CODE",				0, 20 )
			, createFD( OP, "variantCode",			"VARIANTCD",				"DPR_VARIANT_CODE",				0, 20 )
			, createFD( OP, "salesStatus",			"SALES_STATUS",				"DPR_ITEM_MASTER_SALESSTATUS",	2, 2 )
			, createFD( OP, "salesStatusFrom",		"SALES_STATUS_FROM",		"DPR_ITEM_MASTER_SALESSTATUS_FROM", 8, 8, "0123456789" )
			, createFD( OP, "chainStatus",			"CHAIN_STATUS",				"DPR_ITEM_MASTER_CHAINSTATUS",	2, 2 )
			, createFD( OP, "chainStatusFrom",		"CHAIN_STATUS_FROM",		"DPR_ITEM_MASTER_CHAINSTATUS_FROM", 8, 8, "0123456789" )
			, createFD( OP, "salesUnit",			"SALES_UNIT",				"DPR_ITEM_MASTER_SALES_UNIT",	0, 5 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ITEM_MASTER_SALES, table = createTable("DPR_ITEM_MASTER_SALES", "IMTS", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ITEM_MASTER_SALES, new QueryableImpl( table ) { {
			Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = IMTS.ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" );
			append( new QueryableField[] {
						new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", "DPR_ITEM_MASTER_NAME", tbl_DESC )
			});

			append( makeMaterialGroupFields("IMTS", null) );

		}} );


		/***************************************************************************************************
			*	DPR_ITEM_MASTER_STATUS
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "statusCode",			"STATUS_CD",				"DPR_ITEM_STATUS_CODE",			0, 20 )
			, createFD( OP, "statusDesc",			"STATUS_DESC",				"DPR_ITEM_STATUS_DESC",				DESC, 0, 128 )
			, createFD( OP, "displayInd",			"DISPLAY_IND",				"DPR_ITEM_DISPLAY_IND",			"DPR_ITEM_STATUS_DISPLAY", "Y,N" )
		};
		putTable( DPR_ITEM_MASTER_STATUS, table = createTable("DPR_ITEM_MASTER_STATUS", "IMTST", tfields) );


		/***************************************************************************************************
			*	DPR_ITEM_MASTER_UOM
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( PM, "uomCode",				"UOM_CD",					"DPR_ITEMUOM_CODE",				0, 3 )
			, createFD( MD, "uomName",				"UOM_NAME",					"DPR_ITEMUOM_NAME",				DESC, 0, 128 )
			, createFD( MD, "packSize",				"PACKSIZE",					"DPR_ITEMUOM_PACKSIZE",			INTEGER )
			, createFD( OP, "width",				"WIDTH",					"DPR_ITEMUOM_WIDTH",			DOUBLE )
			, createFD( OP, "height",				"HEIGHT",					"DPR_ITEMUOM_HEIGHT",			DOUBLE )
			, createFD( OP, "depth",				"DEPTH",					"DPR_ITEMUOM_DEPTH",			DOUBLE )
			, createFD( OP, "diameter",				"DIAMETER",					"DPR_ITEMUOM_DIAMETER",			DOUBLE )
			, createFD( OP, "volume",				"VOLUME",					"DPR_ITEMUOM_VOLUME",			DOUBLE )
			, createFD( OP, "weight",				"WEIGHT",					"DPR_ITEMUOM_WEIGHT",			DOUBLE )
			, createFD( OP, "lengthUnit",			"LENGTH_UNIT",				"DPR_ITEMUOM_LENGTHUNIT",		3, 3 )
			, createFD( OP, "volumeUnit",			"VOLUME_UNIT",				"DPR_ITEMUOM_VOLUMEUNIT",		3, 3 )
			, createFD( OP, "weightUnit",			"WEIGHT_UNIT",				"DPR_ITEMUOM_WEIGHTUNIT",		3, 3 )
			, createFD( OP, "eanCode",				"EANCODE",					"DPR_ITEMUOM_EANCODE",			0, 20 )
			, createFD( OR, "eanEffectFrom",		"EAN_EFFECT_FROM",			"DPR_ITEMUOM_EANEFFECTFROM",	DATE )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_ITEM_MASTER_UOM, table = createTable("DPR_ITEM_MASTER_UOM", "IMTU", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ITEM_MASTER_UOM, new QueryableImpl( table ) { {
			Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = IMTU.ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" );
			append( new QueryableField[] {
						new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", "DPR_ITEM_MASTER_NAME", tbl_DESC )
			});
		}} );


		/***************************************************************************************************
			*	DPR_ITEM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( OP, "editableDescription",	"EDIT_DESCRIPTION",			"DPR_ITEM_MASTER_DESCRIPTION",	DESC, 0, 512 )
			, createFD( OP, "sourcing",				"SOURCING",					"DPR_ITEM_SOURCING",			"DPR_ITEM_SOUCING_", "S,U" )
			, createFD( OP, "newItemInd",			"NEWITEM_IND",				"DPR_ITEM_NEWITEMIND",			"DPR_ITEM_NEWITEM_", "Y.N" )
			, createFD( OP, "promotionInd",			"PROMOTION_IND",			"DPR_ITEM_PROMOTIONIND",		"DPR_ITEM_PROMOTION_", "Y.N" )
			, createFD( MD, "startAvailDate",		"STARTAVAIL_DATE",			"DPR_ITEM_STARTAVAILDATETIME",	DATE )
			, createFD( OP, "endAvailDate",			"ENDAVAIL_DATE",			"DPR_ITEM_ENDAVAILDATETIME",	DATE )
			, createFD( OP, "priceCurrency",		"PRICE_CURR",				"DPR_ITEM_PRICECURRENCY",		3, 3 )
			, createFD( OP, "price",				"PRICE",					"DPR_ITEM_PRICE",				DOUBLE )
			, createFD( OP, "productCategoryCode",	"PCATECD",					"DPR_ITEM_PRODUCTCATEGORYCODE",	0, 20 )
			, createFD( OP, "megabrandCode",		"MEGABRANDCD",				"DPR_MEGABRAND_CODE",			0, 20 )
			, createFD( OP, "brandCode",			"BRANDCD",					"DPR_BRAND_CODE",				0, 20 )
			, createFD( OP, "baseproductCode",		"BASEPRODUCTCD",			"DPR_BASEPRODUCT_CODE",			0, 20 )
			, createFD( OP, "variantCode",			"VARIANTCD",				"DPR_VARIANT_CODE",				0, 20 )
			, createFD( OP, "putupCode",			"PUTUPCD",					"DPR_PUTUP_CODE",				0, 20 )
			, createFD( OP, "salesStatus",			"SALES_STATUS",				"DPR_ITEM_MASTER_SALESSTATUS",	2, 2 )
			, createFD( OP, "salesStatusFrom",		"SALES_STATUS_FROM",		"DPR_ITEM_MASTER_SALESSTATUS_FROM", 8, 8, "0123456789" )
			, createFD( OP, "chainStatus",			"CHAIN_STATUS",				"DPR_ITEM_MASTER_CHAINSTATUS",	2, 2 )
			, createFD( OP, "chainStatusFrom",		"CHAIN_STATUS_FROM",		"DPR_ITEM_MASTER_CHAINSTATUS_FROM",	8, 8, "0123456789" )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ITEM, table = createTable("DPR_ITEM", "ITM", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ITEM, new QueryableImpl(table) {{
			Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = ITM.ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" );
			Joinable tbl_INTRO = new JoinableImplBK( "IMTI", "DPR_ITEM_MASTER_INTRO"
					, "IMTI.ITEMCD(+) = ITM.ITEMCD AND IMTI.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD AND IMTI.LANGCD(+) = NVL(?, 'en')", "displayLanguage" );
			Joinable tbl_IIMG = new JoinableImpl( "IIMG", "DPR_ITEM_IMG"
					, "IIMG.COUNTRYCD(+) = ITM.COUNTRYCD AND IIMG.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD AND IIMG.ITEMCD(+) = ITM.ITEMCD" );

			Joinable tbl_IMTSS = new JoinableImpl( "IMTSS", "DPR_ITEM_MASTER_STATUS", "IMTSS.STATUS_CD(+) = ITM.SALES_STATUS" );
			Joinable tbl_IMTSC = new JoinableImpl( "IMTSC", "DPR_ITEM_MASTER_STATUS", "IMTSC.STATUS_CD(+) = ITM.CHAIN_STATUS" );

			String itemDisplayInd =
				"(CASE WHEN IMTSS.STATUS_CD IS NULL AND LTRIM(ITM.SALES_STATUS_FROM,'0') IS NULL THEN"
						+ " CASE WHEN IMTSC.STATUS_CD IS NULL AND LTRIM(ITM.CHAIN_STATUS_FROM,'0') IS NULL THEN 'Y'"
						+ " WHEN IMTSC.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.CHAIN_STATUS_FROM,'00000000',NULL,ITM.CHAIN_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
						+ " ELSE 'Y' END"
				+ " ELSE (CASE WHEN IMTSS.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.SALES_STATUS_FROM,'00000000',NULL,ITM.SALES_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
						+ " WHEN IMTSC.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.CHAIN_STATUS_FROM,'00000000',NULL,ITM.CHAIN_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
						+ " ELSE 'Y' END)"
				+ " END)";

			append( new QueryableField[] {
						new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", tbl_DESC )
				, new QueryableFieldImpl( DESC, "additionalItemName", "IMTD.ITEMNAME_ADDITION", tbl_DESC )
				, new QueryableFieldImpl( STRING, "description", "IMTD.DESCRIPTION", tbl_DESC )
				, new QueryableFieldImpl( STRING, "additionalDescription", "IMTD.DESCRIPTION_ADDITION", tbl_DESC )
				, new QueryableFieldImpl( STRING, "intro", "IMTI.INTRO", tbl_INTRO )
				, new QueryableFieldImpl( STRING, "editableIntro", "IMTI.EDIT_INTRO", tbl_INTRO )
				, new QueryableFieldImpl( STRING, "nvlIntro", "NVL(IMTI.EDIT_INTRO, IMTI.INTRO)", tbl_INTRO )
				, new QueryableFieldImpl( STRING, "countryName"
						, "(SELECT SB.COUNTRYNAME FROM DPR_COUNTRY SB WHERE SB.COUNTRY_CD = ITM.COUNTRYCD)" )
				, new QueryableFieldImpl( STRING, "imageFileName", "IIMG.IMAGE_FILENAME", tbl_IIMG )
				, new QueryableFieldImpl( STRING, "imageStatus", "IIMG.STATUS", tbl_IIMG )
				, new QueryableFieldImpl( STRING, "priceCurrencyName"
						, "(SELECT CURR_NAME FROM SYS_CURRENCY SB WHERE SB.CURR_CD = ITM.PRICE_CURR)" )
				, new QueryableFieldImpl( CODE, "salesStatusDisplayInd", "IMTSS.DISPLAY_IND", tbl_IMTSS )
				, new QueryableFieldImpl( CODE, "chainStatusDisplayInd", "IMTSC.DISPLAY_IND", tbl_IMTSC )
				, new QueryableFieldImplBK( CODE, "itemDisplayInd", itemDisplayInd , new String[]{"availableDate","availableDate","availableDate"} )
			} );


			append( Schema.makeItemEANQueryableFields( "IMEAN", "ITM", false ) );
			append( makeMaterialGroupFields("ITM", null) );
			append( makeMaterialHierarchyFields("ITM", null) );

			Joinable tbl_OCL = new JoinableImpl("OCL", "DPR_ORDCLOSE", "OCL.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD AND OCL.BRANDCD(+) = ITM.BRANDCD");
			append( Schema.makeCloseItemQueryableFields("OCL", tbl_OCL) );

			QueryBufferValid queryValid_mstOrgCode = new QueryBufferValid.Condition( "masterOrganizationCode", "displayLanguage" );
			appendCND( queryValid_mstOrgCode,
					makeMaterialHierarchyFieldBKs("ITM", null , masterDescOrgBKKeys )
			);
		}} );


		/***************************************************************************************************
			*	DPR_ITEM_IMG
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( PM, "displayType",			"DISPLAY_TYPE",				"DPR_ITEMIMAGE_DISPLAYTYPE",	"DPR_DISPLAYTYPE_", "FR,BR,RS,LS,US,DS" )
			, createFD( MD, "imageType",			"IMAGE_TYPE",				"DPR_ITEMIMAGE_IMAGETYPE",		0, 15 )
			, createFD( OP, "imageExtention",		"IMAGE_EXTENTION",			"DPR_ITEMIMAGE_IMAGEEXTENTION",	"DPR_IMAGEEXTENTION_", "JPG,PJPEG,JPEG,PNG,BMP,GIF" )
			, createFD( MD, "imageSize",			"IMAGE_SIZE",				"DPR_ITEMIMAGE_IMAGESIZE",		DOUBLE )
			, createFD( OP, "imageFile",			"IMAGE_FILE",				"DPR_ITEMIMAGE_IMAGEFILE",		DESC )
			, createFD( OP, "imagePath",			"IMAGE_PATH",				"DPR_ITEMIMAGE_IMAGEPATH",		0, 20 )
			, createFD( MD, "imageFileName",		"IMAGE_FILENAME",			"DPR_ITEMIMAGE_IMAGEFILENAME",	0, 20 )
			, createFD( OP, "imageWidth",			"WIDTH",					"DPR_ITEMIMAGE_WIDTH",			DOUBLE )
			, createFD( OP, "imageHeight",			"HEIGHT",					"DPR_ITEMIMAGE_HEIGHT",			DOUBLE )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ITEM_IMG, table = createTable("DPR_ITEM_IMG", "IIMG", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		*	DPR_ITEM_PRICE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( PM, "officeCode",			"OFFICECD",					"DPR_SALESOFFICE_CODE",			0, 8 )
			, createFD( PM, "groupCode",			"GROUPCD",					"DPR_SALESGROUP_CODE",			0, 8 )
			, createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( MD, "itemPrice",			"PRICE",					"DPR_ITEM_PRICE",				DOUBLE )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ITEM_PRICE, table = createTable("DPR_ITEM_PRICE", "ITP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ITEM_PRICE, new QueryableImpl(table) {{

			Joinable tbl_USR = new JoinableImpl( "USR", "USR_USER", "USR.UNIQID(+) = ITP.UPGUSERID" );

			append( new QueryableField[] {
					  new QueryableFieldImpl( STRING, "userName", "USR.USER_NAME", tbl_USR )
					, new QueryableFieldImpl( STRING, "partyName"
								, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
										+" WHERE SB.PARTYCD = ITP.PARTYCD AND SB.ORGANIZATIONCD = ITP.ORGANIZATIONCD"
											+" AND SB.DIST_CHANNELCD = ITP.DIST_CHANNELCD)" )
			});

			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();
			append( new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = "+baseAlias+".ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" ) ));
			append( Schema.makeMasterNames(baseAlias, null, "organizationName", "distributionChannelName", "officeName", "groupName" ) );
			append( Schema.makeItemEANQueryableFields( "IMEAN", "ITP", false ) );
		}} );


		/***************************************************************************************************
			*	DPR_ITEM_EANMAP
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( PM, "mapEffectFrom",		"MAP_EFFECT_FROM",			"DPR_ITEMEAN_MAPEFFECTFROM",	DATE )
			, createFD( MD, "mapEanCode",			"MAP_EANCODE",				"DPR_ITEMEAN_MAPCODE",			0, 15 )
			, createFD( RD, "eanSource",			"SOURCE",					"DPR_ITEMEAN_SOURCE",			"DPR_ITEMEAN_SOURCE_", "D,S" )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ITEM_EANMAP, table = createTable("DPR_ITEM_EANMAP", "IMPEAN", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ITEM_EANMAP, new QueryableImpl(table) {{
				Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
						, "IMTD.ITEMCD(+) = IMPEAN.ITEMCD AND IMTD.LANGCD(+) = ?", "displayLanguage" );

				append( new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
						, "IMTD.ITEMCD(+) = "+((Table)this.getBaseJoinable()).getTableAlias()+".ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" ) ));

				Joinable tbl_IMUCONS = new JoinableImpl( "IMUCONS", "DPR_ITEM_MASTER_UOM", "IMUCONS.ITEMCD(+) = IMPEAN.ITEMCD AND IMUCONS.UOM_CD(+) = 'PC'" );

				String field_itemConsumerEANCode = "(CASE"
						+ " WHEN IMPEAN.MAP_EFFECT_FROM = TRUNC(pkCustom.fCurrentDate(IMPEAN.ORGANIZATIONCD)) THEN IMPEAN.MAP_EANCODE"
						+ " WHEN IMUCONS.EAN_EFFECT_FROM = TRUNC(pkCustom.fCurrentDate(IMPEAN.ORGANIZATIONCD)) THEN IMUCONS.EANCODE"
						+ " WHEN IMPEAN.MAP_EFFECT_FROM < TRUNC(pkCustom.fCurrentDate(IMPEAN.ORGANIZATIONCD))"
						+ "		AND IMPEAN.MAP_EFFECT_FROM >= NVL(IMUCONS.EAN_EFFECT_FROM,IMPEAN.MAP_EFFECT_FROM) THEN IMPEAN.MAP_EANCODE"
						+ " ELSE IMUCONS.EANCODE END)";
				append( new QueryableField[] {
					  new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", tbl_DESC )
					, new QueryableFieldImpl( CODE, "itemConsumerEANCode", field_itemConsumerEANCode, tbl_IMUCONS )
					, new QueryableFieldImpl( CODE, "eanCode", "IMUCONS.EANCODE", tbl_IMUCONS )
					, new QueryableFieldImpl( CODE, "eanEffectFrom", "IMUCONS.EAN_EFFECT_FROM", tbl_IMUCONS )
					, new QueryableFieldImplBK( STRING, "organizationName"
							, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = IMPEAN.ORGANIZATIONCD"
								+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = ?)", "displayLanguage", "DPR_PARTY_ORGANIZATION_NAME" )
				} );
		}} );

		/***************************************************************************************************
		*	DPR_ITEM_EAN
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( RD, "itemConsumerEANCode",	"ITEM_CONS_EAN",			"DPR_ITEMEAN_EANCODE",	DATE )
			, createFD( MD, "eanCode",				"EANCODE",					"DPR_ITEMEAN_SAP_EANCODE",		0, 15 )
			, createFD( PM, "eanEffectFrom",		"EAN_EFFECT_FROM",			"DPR_ITEMEAN_SAP_EFFECTFROM",	DATE )
			, createFD( MD, "mapEanCode",			"MAP_EANCODE",				"DPR_ITEMEAN_DPR_MAPCODE",		0, 15 )
			, createFD( PM, "mapEffectFrom",		"MAP_EFFECT_FROM",			"DPR_ITEMEAN_DPR_EFFECTFROM",	DATE )
			, createFD( MD, "mapEanCode",			"MAP_EANCODE",				"DPR_ITEMEAN_MAPCODE",			0, 15 )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_ITEM_EAN, table = createTable("DPR_ITEM_EAN", "IMEAN", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		*	DPR_FREEGOODS
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "freegoodsKey",			"FREEGOODS_KEY",			"KEY",							0, 20 )
			, createFD( MD, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( MD, "officeCode",			"OFFICECD",					"DPR_PARTY_SALESOFFICE_CODE",	0, 8 )
			, createFD( MD, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( MD, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( MD, "startDate",			"STARTAVAIL_DATE",			"DPR_START_DATE",				DATE )
			, createFD( MD, "endDate",				"ENDAVAIL_DATE",			"DPR_END_DATE",					DATE )
			, createFD( MD, "quota",				"QUOTA",					"DPR_FREEGOODS_QUOTA",			INTEGER )
			, createFD( MD, "orderableRatio",		"ORDERABLE_RATIO",			"DPR_FREEGOODS_ORDERABLERATIO",	DOUBLE, 1, 30 )
			, createFD( OP, "usedQty",				"USED_QTY",					"DPR_FREEGOODS_USED_QTY",		INTEGER )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_FREEGOODS, table = createTable("DPR_FREEGOODS", "FRG", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_FREEGOODS, new QueryableImpl(table) {
			{
				append( new QueryableField[] {
					  new QueryableFieldImplBK( DESC, "organizationName"
							, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = FRG.ORGANIZATIONCD"
								+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_ORGANIZATION_NAME" )
					, new QueryableFieldImplBK( DESC, "officeName"
							, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = FRG.OFFICECD"
								+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_OFFICE_NAME" )
					, new QueryableFieldImplBK( STRING, "partyName"
							, "(SELECT MAX(SB.PARTYNAME) FROM DPR_PARTY_SALES SB"
								+" WHERE SB.PARTYCD = FRG.PARTYCD AND SB.ORGANIZATIONCD = FRG.ORGANIZATIONCD"
								+" AND SB.DIVISIONCD = ?)", "divisionCode", "DPR_PARTY_NAME" )
					, new QueryableFieldImplBK( DESC, "itemName"
							, "(SELECT SB.ITEMNAME FROM DPR_ITEM_MASTER_DESC SB WHERE SB.ITEMCD = FRG.ITEMCD AND SB.LANGCD = ?)", "displayLanguage", "DPR_ITEM_NAME" )
					, new QueryableFieldImpl( INTEGER, "surplusQty"
							, "NVL(QUOTA, 0) - NVL(USED_QTY, 0)", "DPR_FREEGOODS_SURPLUSQTY" )
				} );
			}
		} );


		/***************************************************************************************************
		*	DPR_MOQITEM_CFG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( PM, "officeCode",			"OFFICECD",					"DPR_PARTY_SALESOFFICE_CODE",	0, 8 )
			, createFD( PM, "groupCode",			"GROUPCD",					"DPR_PARTY_SALESGROUP_CODE",	0, 8 )
			, createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( OP, "alMoqDay",				"ALMOQ_PC_DAY",				"DPR_MOQITEM_ALLMOQ_DAY",			INTEGER, 0, Integer.MAX_VALUE )
			, createFD( OP, "alMoqMonth",			"ALMOQ_PC_MONTH",			"DPR_MOQITEM_ALLMOQ_MONTH",			INTEGER, 0, Integer.MAX_VALUE )
			, createFD( OP, "pdMoqDay",				"PDMOQ_PC_DAY",				"DPR_MOQITEM_PACKDEALMOQ_DAY",		INTEGER, 0, Integer.MAX_VALUE )
			, createFD( OP, "pdMoqMonth",			"PDMOQ_PC_MONTH",			"DPR_MOQITEM_PACKDEALMOQ_MONTH",	INTEGER, 0, Integer.MAX_VALUE )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_MOQITEM_CFG, table = createTable("DPR_MOQITEM_CFG", "MQCFG", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_MOQITEM_CFG, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();

			// beware partyName is not uniq by its code only may need organizationCode and distributionChannelCode
			append( new QueryableFieldImpl( STRING, "partyName"
						 , "(SELECT MAX(PARTYNAME) FROM DPR_PARTY_SALES SB WHERE SB.PARTYCD = "+baseAlias+".PARTYCD AND SB.ORGANIZATIONCD = "+baseAlias+".ORGANIZATIONCD)"
						 , "DPR_PARTY_SALES_PARTYNAME")
					);
			append( new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = "+((Table)this.getBaseJoinable()).getTableAlias()+".ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" ) ));

			append( new QueryableField[]{
					  new QueryableFieldImpl( INTEGER, "rgMoqDay", "(NVL(ALMOQ_PC_DAY,0) - NVL(PDMOQ_PC_DAY,0))" )
					, new QueryableFieldImpl( INTEGER, "rgMoqMonth", "(NVL(ALMOQ_PC_MONTH,0) - NVL(PDMOQ_PC_MONTH,0))" )
					, new QueryableFieldWrapper(new QueryableFieldImpl( CODE, "brandCode"
							, "(SELECT MAX(BRANDCD) FROM DPR_ITEM_MASTER_SALES SB WHERE SB.ITEMCD = MQCFG.ITEMCD)" ), false)
			});

			append( makeMasterNames("MQCFG", null, "itemName") );

		}
			@Override
			public boolean appendCondition(ConditionQueryBuffer querybuf) {
				boolean hasCondition = super.appendCondition(querybuf);

				if( querybuf.hasConditionValue("brandCode") ) {
					querybuf.appendCondition( "MQCFG.ITEMCD IN (SELECT DISTINCT ITEMCD FROM DPR_ITEM_MASTER_SALES SB WHERE SB.BRANDCD = ?)", querybuf.getConditionValue("brandCode") );

					hasCondition = true;
				}

				return hasCondition;
			};
		} );


		/***************************************************************************************************
		*	DPR_MOQITEM_RLT
		***************************************************************************************************/
		QueryableField[] qfields = new QueryableField[] {
			  createQFD( T,		"dateValue",					"DATEVALUE",			"DATEVALUE",			STRING )
			, createQFD( T,		"dateValueType",				"DATEVALUE_TYPE",			"DATEVALUE_TYPE",			CODE )
			, createQFD( T,		"itemCode",						"ITEMCD",			"DPR_ITEM_MASTER_CODE",				CODE )
			, createQFD( T,		"organizationCode",				"ORGANIZATIONCD",			"DPR_PARTY_SALESORGANIZATION_CODE",			CODE )
			, createQFD( T,		"distributionChannelCode",		"DIST_CHANNELCD",			"DPR_PARTY_DISTRIBUTIONCHANNEL_CODE",				CODE )
			, createQFD( T,		"officeCode",					"OFFICECD",				"DPR_SALESOFFICE_CODE",					CODE )
			, createQFD( T,		"groupCode",					"GROUPCD",			"DPR_SALESGROUP_CODE",				CODE )
			, createQFD( T,		"partyCode",					"PARTYCD",		"DPR_PARTY_CODE",			CODE )
			, createQFD( T,		"cfgOrganizationCode",			"CFG_ORG",		"",		CODE )
			, createQFD( T,		"cfgDistributionChannelCode",	"CFG_DCH",		"",		CODE )
			, createQFD( T,		"cfgOfficeCode",				"CFG_OFC",		"",		CODE )
			, createQFD( T,		"cfgGroupCode",					"CFG_GRP",		"",		CODE )
			, createQFD( T,		"cfgPartyCode",					"CFG_PTY",		"",		CODE )
			, createQFD( T,		"slfDistChanCode",				"SLF_DCH",		"",		CODE )
			, createQFD( T,		"slfOfficeCode",				"SLF_OFC",		"",		CODE )
			, createQFD( T,		"slfGroupCode",					"SLF_GRP",		"",		CODE )
			, createQFD( T,		"slfPartyCode",					"SLF_PTY",		"",		CODE )
			, createQFD( T,		"hierType",						"HIERTYPE",		"DPR_MOQITEM_HIERTYPE",		CODE )
			, createQFD( T,		"hierTypeIndex",				"HIERTYPE_INDEX",		"DPR_MOQITEM_HIERTYPE_INDEX",		INTEGER )
			, createQFD( T,		"alCnfQty",						"ALCNF_PC_QTY",		"DPR_MOQITEM_AL_CONFIRMED_QTY",		INTEGER )
			, createQFD( T,		"rgCnfQty",						"RGCNF_PC_QTY",		"DPR_MOQITEM_RG_CONFIRMED_QTY",		INTEGER )
			, createQFD( T,		"pdCnfQty",						"PDCNF_PC_QTY",		"DPR_MOQITEM_PD_CONFIRMED_QTY",		INTEGER )
			, createQFD( T,		"alCfgQty",						"ALMOQ_PC_QTY",		"DPR_MOQITEM_AL_MOQ_QTY",		INTEGER )
			, createQFD( T,		"rgCfgQty",						"RGMOQ_PC_QTY",		"DPR_MOQITEM_RG_MOQ_QTY",		INTEGER )
			, createQFD( T,		"pdCfgQty",						"PDMOQ_PC_QTY",		"DPR_MOQITEM_PD_MOQ_QTY",		INTEGER )
			, createQFD( T,		"alSlfQty",						"ALSLF_AGG_QTY",		"DPR_MOQITEM_AL_SELF_AGG_QTY",		INTEGER )
			, createQFD( T,		"rgSlfQty",						"RGSLF_AGG_QTY",		"DPR_MOQITEM_RG_SELF_AGG_QTY",		INTEGER )
			, createQFD( T,		"pdSlfQty",						"PDSLF_AGG_QTY",		"DPR_MOQITEM_PD_SELF_AGG_QTY",		INTEGER )
			, createQFD( T,		"alChdQty",						"ALREQ_CHD_QTY",		"DPR_MOQITEM_AL_MOQ_CHILD_QTY",		INTEGER )
			, createQFD( T,		"rgChdQty",						"RGREQ_CHD_QTY",		"DPR_MOQITEM_RG_MOQ_CHILD_QTY",		INTEGER )
			, createQFD( T,		"pdChdQty",						"PDREQ_CHD_QTY",		"DPR_MOQITEM_PD_MOQ_CHILD_QTY",		INTEGER )
			, createQFD( T,		"alCnfChdQty",					"ALCNF_CHD_QTY",		"DPR_MOQITEM_AL_MOQCNF_CHILD_QTY",		INTEGER )
			, createQFD( T,		"rgCnfChdQty",					"RGCNF_CHD_QTY",		"DPR_MOQITEM_RG_MOQCNF_CHILD_QTY",		INTEGER )
			, createQFD( T,		"pdCnfChdQty",					"PDCNF_CHD_QTY",		"DPR_MOQITEM_PD_MOQCNF_CHILD_QTY",		INTEGER )
			, createQFD( T,		"lastRefreshDateTime",			"LAST_REFRESH_DATE",	"DPR_MOQITEM_LAST_REFRESH_DATE",		DATETIME )
			, createQFD( T,		"lastRefreshType",				"LAST_REFRESH_TYPE",	"DPR_MOQITEM_LAST_REFRESH_TYPE",		STRING )
			, createQFD( T,		"orderDate",					"ORDDATE",			"DPR_ORDER_ORDERDATE",			DATE )
			, createQFD( T,		"rltType",						"RLTTYPE",			"DPR_MOQITEM_RLTTYPE_",		 "DF,UN" )
		};
		putQueryable( DPR_MOQITEM_RLT, new QueryableImpl(new JoinableImpl("MQRLT", "vwmvDPR_MOQITEM_RLT"), qfields) {{

			append( new QueryableFieldWrapper(new QueryableFieldImpl( CODE, "brandCode"
							, "(SELECT MAX(BRANDCD) FROM DPR_ITEM_MASTER_SALES SB WHERE SB.ITEMCD = MQRLT.ITEMCD)" ), false) );

			// beware partyName is not uniq by its code only may need organizationCode and distributionChannelCode
			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "partyName"
						 , "(SELECT MAX(PARTYNAME) FROM DPR_PARTY_SALES SB WHERE SB.PARTYCD = "+"MQRLT"+".PARTYCD AND SB.ORGANIZATIONCD = "+"MQRLT"+".ORGANIZATIONCD)"
						 , "DPR_PARTY_SALES_PARTYNAME")
				, new QueryableFieldImpl( INTEGER, "alCfgQty2", "NVL(MQRLT.ALMOQ_PC_QTY, 0) + NVL(MQRLT.ALREQ_CHD_QTY, 0)", "DPR_MOQITEM_AL_MOQ_QTY2" )
				, new QueryableFieldImpl( INTEGER, "rgCfgQty2", "NVL(MQRLT.RGMOQ_PC_QTY, 0) + NVL(MQRLT.RGREQ_CHD_QTY, 0)", "DPR_MOQITEM_RG_MOQ_QTY2" )
				, new QueryableFieldImpl( INTEGER, "pdCfgQty2", "NVL(MQRLT.PDMOQ_PC_QTY, 0) + NVL(MQRLT.PDREQ_CHD_QTY, 0)", "DPR_MOQITEM_PD_MOQ_QTY2" )
				, new QueryableFieldImpl( INTEGER, "alCnfQty2", "NVL(MQRLT.ALCNF_PC_QTY, 0) + NVL(MQRLT.ALCNF_CHD_QTY, 0)", "DPR_MOQITEM_AL_CONFIRMED_QTY2" )
				, new QueryableFieldImpl( INTEGER, "rgCnfQty2", "NVL(MQRLT.RGCNF_PC_QTY, 0) + NVL(MQRLT.RGCNF_CHD_QTY, 0)", "DPR_MOQITEM_RG_CONFIRMED_QTY2" )
				, new QueryableFieldImpl( INTEGER, "pdCnfQty2", "NVL(MQRLT.PDCNF_PC_QTY, 0) + NVL(MQRLT.PDCNF_CHD_QTY, 0)", "DPR_MOQITEM_PD_CONFIRMED_QTY2" )
				, new QueryableFieldImpl( INTEGER, "alSlfQty2", "NVL2(MQRLT.ALSLF_AGG_QTY, NVL(MQRLT.ALMOQ_PC_QTY, 0) + NVL(MQRLT.ALREQ_CHD_QTY, 0) - NVL(MQRLT.ALCNF_PC_QTY, 0) - NVL(MQRLT.ALCNF_CHD_QTY, 0), NULL)", "DPR_MOQITEM_AL_SELF_AGG_QTY2" )
				, new QueryableFieldImpl( INTEGER, "rgSlfQty2", "NVL2(MQRLT.RGSLF_AGG_QTY, NVL(MQRLT.RGMOQ_PC_QTY, 0) + NVL(MQRLT.RGREQ_CHD_QTY, 0) - NVL(MQRLT.RGCNF_PC_QTY, 0) - NVL(MQRLT.RGCNF_CHD_QTY, 0), NULL)", "DPR_MOQITEM_RG_SELF_AGG_QTY2" )
				, new QueryableFieldImpl( INTEGER, "pdSlfQty2", "NVL2(MQRLT.PDSLF_AGG_QTY, NVL(MQRLT.PDMOQ_PC_QTY, 0) + NVL(MQRLT.PDREQ_CHD_QTY, 0) - NVL(MQRLT.PDCNF_PC_QTY, 0) - NVL(MQRLT.PDCNF_CHD_QTY, 0), NULL)", "DPR_MOQITEM_PD_SELF_AGG_QTY2" )
			});
			append( makeMasterNames("MQRLT", null, "itemName") );
		}
			@Override
			public boolean appendCondition(com.irt.sql.ConditionQueryBuffer querybuf) {
				boolean hasCondition = super.appendCondition( querybuf );

				if( querybuf.hasConditionValue("brandCode") ) {
					querybuf.appendCondition( "MQRLT.ITEMCD IN (SELECT DISTINCT ITEMCD FROM DPR_ITEM_MASTER_SALES SB WHERE SB.BRANDCD = ?)", querybuf.getConditionValue("brandCode") );

					hasCondition = true;
				}
				if( !querybuf.isConditionTrue("showall") ) {
					querybuf.appendCondition("(MQRLT.ALMOQ_PC_QTY IS NOT NULL OR MQRLT.RGMOQ_PC_QTY IS NOT NULL OR MQRLT.PDMOQ_PC_QTY IS NOT NULL)");
				}
				if( "tvw".equals(querybuf.getConditionValue(Condition.BASIS_CONDITIONKEY, STRING)) ) {
					querybuf.replaceTable("tvwDPR_MOQITEM_RLT", "MQRLT");
					if( !querybuf.isConditionTrue("showall") ) {
						querybuf.appendCondition("(MQRLT.RLTTYPE = 'DF' OR (MQRLT.RLTTYPE = 'UN' AND MQRLT.HIERTYPE = 'PTY'))");
					}
				}

				return hasCondition;
			};
		} );


		/***************************************************************************************************
		*	DPR_MOQITEM_CFGDATA
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "dateValue",			"DATEVALUE",			"DATEVALUE",			STRING )
			, createFD( PM, "dateValueType",			"DATEVALUE_TYPE",			"DATEVALUE_TYPE",			CODE, 1 , 1 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( PM, "officeCode",			"OFFICECD",					"DPR_PARTY_SALESOFFICE_CODE",	0, 8 )
			, createFD( PM, "groupCode",			"GROUPCD",					"DPR_PARTY_SALESGROUP_CODE",	0, 8 )
			, createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( OP, "alMoqDay",			"ALMOQ_PC_DAY",				"DPR_MOQITEM_ALLMOQ_DAY",			INTEGER, 0, Integer.MAX_VALUE )
			, createFD( OP, "pdMoqDay",		"PDMOQ_PC_DAY",				"DPR_MOQITEM_PACKDEALMOQ_DAY",			INTEGER, 0, Integer.MAX_VALUE )
			, createFD( OP, "alMoqMonth",			"ALMOQ_PC_MONTH",			"DPR_MOQITEM_ALLMOQ_MONTH",		INTEGER, 0, Integer.MAX_VALUE )
			, createFD( OP, "pdMoqMonth",		"PDMOQ_PC_MONTH",			"DPR_MOQITEM_PACKDEALMOQ_MONTH",		INTEGER, 0, Integer.MAX_VALUE )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_MOQITEM_CFGDATA, table = createTable("DPR_MOQITEM_CFGDATA", "MOQCFG", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_MOQITEM_CFGDATA, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();

			// beware partyName is not uniq by its code only may need organizationCode and distributionChannelCode
			append( new QueryableFieldImpl( STRING, "partyName"
						 , "(SELECT MAX(PARTYNAME) FROM DPR_PARTY_SALES SB WHERE SB.PARTYCD = "+baseAlias+".PARTYCD AND SB.ORGANIZATIONCD = "+baseAlias+".ORGANIZATIONCD)"
						 , "DPR_PARTY_SALES_PARTYNAME")
					);

			append( new QueryableField[]{
					  new QueryableFieldImpl( INTEGER, "rgMoqDay", "RGMOQ_PC_DAY", "DPR_MOQITEM_REGULARMOQ_DAY" )
					, new QueryableFieldImpl( INTEGER, "rgMoqMonth", "RGMOQ_PC_MONTH","DPR_MOQITEM_REGULARMOQ_MONTH" )
			});

		}} );


		/***************************************************************************************************
		 *	DPR_PERIOD
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( RD, "periodSetName",		"PERIOD_SET_NAME",				"CST_MENU_ID",			STRING )
			, createFD( RD, "periodType",			"PERIOD_TYPE",			"CST_MENU_LOCALE",				CODE )
			, createFD( RD, "startDate",			"START_DATE",			"CST_MENU_HIERARCHY",			DATE )
			, createFD( RD, "endDate",				"END_DATE",			"CST_MENU_LEVEL",					DATE )
			, createFD( RD, "dateValue",			"YEARMONTH",				"CST_MENU_SEQ",				STRING )
		};
		putTable( DPR_PERIOD, table = createTable("DPR_PERIOD", "PRD", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PERIOD, new QueryableImpl(table) {{
			append( new QueryableFieldImpl( STRING, "currUniMonth", "(SELECT PRD.YEARMONTH CURR_UNIMONTH FROM DPR_PERIOD PRD"
					+ " WHERE TRUNC(SYSDATE) >= PRD.START_DATE AND TRUNC(SYSDATE) <= PRD.END_DATE"
					+ " AND PRD.PERIOD_TYPE = 'M' AND PRD.PERIOD_SET_NAME = 'UNIMONTH')") );
		}});


		/***************************************************************************************************
		*	DPR_PACKDEAL_CFG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( PM, "officeCode",			"OFFICECD",					"DPR_PARTY_SALESOFFICE_CODE",	0, 8 )
			, createFD( PM, "groupCode",			"GROUPCD",					"DPR_PARTY_SALESGROUP_CODE",	0, 8 )
			, createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "dealCode",				"DEALCD",					"DPR_PACKDEAL_CODE",			0, 128 )

			, createFD( MD, "targetTotalAmount",	"TOTAL_AMOUNT",				"DPR_PACKDEAL_TARGETTOTAL_AMOUNT",	DOUBLE, 0, Double.MAX_VALUE, Schema.EQU_YN )
			, createFD( MD, "toleranceRate",		"TOLERANCE_RATE",			"DPR_PACKDEAL_TOLERANCE_RATE",	DOUBLE, 0, 100 )
			, createFD( MD, "dealStopInd",			"DEAL_STOP_IND",			"DPR_PACKDEAL_STOP_IND",		"PUB_WHETHER_", "Y,N" )
			, createFD( MD, "dealStartDate",		"DEAL_STARTDATE",			"DPR_PACKDEAL_STARTDATE",		DATE )
			, createFD( MD, "dealEndDate",			"DEAL_ENDDATE",				"DPR_PACKDEAL_ENDDATE",			DATE )

			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PACKDEAL_CFG, table = createTable("DPR_PACKDEAL_CFG", "PDCFG", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PACKDEAL_CFG, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();

			append( new QueryableField[] {
				new QueryableFieldImpl( INTEGER, "itemCount"
					, "(SELECT COUNT(*) FROM DPR_PACKDEAL_ITEM PDITM WHERE PDITM.DEALCD = PDCFG.DEALCD)" )
			});

			// beware partyName is not uniq by its code only may need organizationCode and distributionChannelCode
			append( new QueryableFieldImpl( STRING, "partyName"
						  , "(SELECT MAX(PARTYNAME) FROM DPR_PARTY_SALES SB WHERE SB.PARTYCD = "+baseAlias+".PARTYCD AND SB.ORGANIZATIONCD = "+baseAlias+".ORGANIZATIONCD)"
						  , "DPR_PARTY_SALES_PARTYNAME")
					);
			append( Schema.makeMasterNames(baseAlias, null, "organizationName", "distributionChannelName", "officeName", "groupName" ) );
			append( makePackdealDateQueryableFields(baseAlias, null) );
		}} );


		/***************************************************************************************************
		*	DPR_PACKDEAL_CFGRLT
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "dealCode",				"DEALCD",					"DPR_PACKDEAL_CODE",				0, 20 )
			, createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( RD, "officeCode",			"OFFICECD",					"DPR_PARTY_SALESOFFICE_CODE",	0, 8 )
			, createFD( RD, "groupCode",			"GROUPCD",					"DPR_PARTY_SALESGROUP_CODE",	0, 8 )

			, createFD( OP, "targetTotalAmount",	"TOTAL_AMOUNT",				"DPR_PACKDEAL_TARGETTOTAL_AMOUNT",	DOUBLE )
			, createFD( OP, "toleranceRate",		"TOLERANCE_RATE",			"DPR_PACKDEAL_TOLERANCE_RATE",	DOUBLE, 0, 100 )
			, createFD( OP, "dealStopInd",			"DEAL_STOP_IND",			"DPR_PACKDEAL_STOP_IND",		"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "dealStartDate",		"DEAL_STARTDATE",			"DPR_PACKDEAL_STARTDATE",		DATE )
			, createFD( OP, "dealEndDate",			"DEAL_ENDDATE",				"DPR_PACKDEAL_ENDDATE",			DATE )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PACKDEAL_CFGRLT, table = createTable("vwDPR_PACKDEAL_CFGRLT", "PDRLT", tfields, "UPGDATE = SYSDATE") );
		putQueryable(DPR_PACKDEAL_CFGRLT, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();

			// beware partyName is not uniq by its code only may need organizationCode and distributionChannelCode
			append( new QueryableFieldImpl( STRING, "partyName"
						  , "(SELECT MAX(PARTYNAME) FROM DPR_PARTY_SALES SB WHERE SB.PARTYCD = "+baseAlias+".PARTYCD AND SB.ORGANIZATIONCD = "+baseAlias+".ORGANIZATIONCD)"
						  , "DPR_PARTY_SALES_PARTYNAME")
					);

			append( new QueryableField[] {
				  new QueryableFieldImpl( INTEGER, "itemCount", "(SELECT COUNT(*) FROM DPR_PACKDEAL_ITEM PDITM WHERE PDITM.DEALCD = PDRLT.DEALCD )" )
				, new QueryableFieldImpl( CODE, "hasItem", "(SELECT (CASE WHEN COUNT(PDITM.ITEMCD) > 0 THEN 'Y' ELSE 'N' END) FROM DPR_PACKDEAL_ITEM PDITM WHERE PDITM.DEALCD = PDRLT.DEALCD )" )
			});

			append( Schema.makePackdealDateQueryableFields(baseAlias, null) );
			append( Schema.makeMasterNames(baseAlias, null, "organizationName", "distributionChannelName", "officeName", "groupName" ) );
		}
			@Override
			public boolean appendCondition(ConditionQueryBuffer querybuf) {
				boolean hasCondition = super.appendCondition(querybuf);

				String isSellingSku = (String)querybuf.getConditionValue( "isSellingSku", STRING );
				String organizationCode = (String)querybuf.getConditionValue("organizationCode", STRING );
				String distributionChannelCode = (String)querybuf.getConditionValue("distributionChannelCode", STRING );
				String partyCode = (String)querybuf.getConditionValue("partyCode", STRING );
				if( "Y".equals(isSellingSku) && organizationCode != null && distributionChannelCode != null && partyCode != null ) {
					querybuf.appendCondition("EXISTS (SELECT NULL FROM DPR_PACKDEAL_ITEM PDITM, DPR_ORDER_ITEM SB"
							+ " WHERE PDITM.DEALCD(+) = PDRLT.DEALCD"
							+ " AND SB.ITEMCD(+) = PDITM.ITEMCD"
							+ " AND SB.DIVISIONCD(+) = '10'"
							+ " AND SB.ORGANIZATIONCD(+) = ?"
							+ " AND SB.DIST_CHANNELCD(+) = ?"
							+ " AND SB.PARTYCD(+) = ?)", new Object[]{organizationCode, distributionChannelCode, partyCode});
					hasCondition = true;
				}

				return hasCondition;
			};
		} );


		/***************************************************************************************************
		*	DPR_PACKDEAL_ITEM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "dealCode",				"DEALCD",					"DPR_PACKDEAL_CODE",			0, 20 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( MD, "packdealDiscountRate",	"DISCOUNT_RATE",			"DPR_PACKDEALITEM_DISCOUNT_RATE",	DOUBLE,	0, 100 )
			, createFD( MD, "packdealDisplaySeq",	"DISPLAY_SEQ",				"DPR_PACKDEALITEM_DISPLAY_SEQ",	INTEGER )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PACKDEAL_ITEM, table = createTable("DPR_PACKDEAL_ITEM", "PDITM", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PACKDEAL_ITEM, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();

			Joinable tbl_PDR = new JoinableImpl("PDR", "vwDPR_PACKDEAL", "PDR.DEALCD = PDITM.DEALCD AND PDR.ITEMCD = PDITM.ITEMCD");
			append( Schema.makePackdealDateQueryableFields("PDR", tbl_PDR) );
			append( new QueryableField[]{
					  new QueryableFieldImpl( CODE, "organizationCode", "PDR.ORGANIZATIONCD", "DPR_SALESORGANIZATION_CODE", tbl_PDR )
					, new QueryableFieldImpl( CODE, "distributionChannelCode", "PDR.DIST_CHANNELCD", "DPR_DISTRIBUTIONCHANNEL_CODE", tbl_PDR )
					, new QueryableFieldImpl( CODE, "officeCode", "PDR.OFFICECD", "DPR_SALESOFFICE_CODE", tbl_PDR )
					, new QueryableFieldImpl( CODE, "groupCode", "PDR.GROUPCD", "DPR_SALESGROUP_CODE", tbl_PDR )
					, new QueryableFieldImpl( CODE, "partyCode", "PDR.PARTYCD", "DPR_PARTY_CODE", tbl_PDR ),
					  new QueryableFieldImpl( CODE, "cfgOrg", "PDR.CFG_ORG", "DPR_SALESORGANIZATION_CODE", tbl_PDR )
					, new QueryableFieldImpl( CODE, "cfgDch", "PDR.CFG_DCH", "DPR_DISTRIBUTIONCHANNEL_CODE", tbl_PDR )
					, new QueryableFieldImpl( CODE, "cfgOfc", "PDR.CFG_OFC", "DPR_SALESOFFICE_CODE", tbl_PDR )
					, new QueryableFieldImpl( CODE, "cfgGrp", "PDR.CFG_GRP", "DPR_SALESGROUP_CODE", tbl_PDR )
					, new QueryableFieldImpl( CODE, "cfgPty", "PDR.CFG_PTY", "DPR_PARTY_CODE", tbl_PDR )
			});

			append( Schema.makePackdealDateQueryableFields("PDR", tbl_PDR) );

			Joinable tbl_STIMRT = new JoinableImpl( "STIMRT", "vwDPR_STOPITEM_CFGRLT", "STIMRT.ITEMCD(+) = PDR.ITEMCD"
					+ " AND STIMRT.PARTYCD(+) = PDR.PARTYCD AND STIMRT.ORGANIZATIONCD(+) = PDR.ORGANIZATIONCD AND STIMRT.DIST_CHANNELCD(+) = PDR.DIST_CHANNELCD"
					, tbl_PDR );
			append( Schema.makeStopItemQueryableFields("STIMRT", tbl_STIMRT) );

			Joinable tbl_IMEAN = new JoinableImpl( "IMEAN", "vwDPR_ITEM_EANRLT", "IMEAN.ORGANIZATIONCD(+) = PDR.ORGANIZATIONCD AND IMEAN.ITEMCD(+) = PDR.ITEMCD" );
			Joinable tbl_IMTS = new JoinableImpl( "IMTS", "DPR_ITEM_MASTER_SALES"
					, "IMTS.ITEMCD(+) = PDR.ITEMCD AND IMTS.ORGANIZATIONCD(+) = PDR.ORGANIZATIONCD AND IMTS.DIST_CHANNELCD(+) = PDR.DIST_CHANNELCD", tbl_PDR );
			Joinable tbl_ITM = new JoinableImpl( "ITM", "DPR_ITEM" , "ITM.ORGANIZATIONCD(+) = PDR.ORGANIZATIONCD"
						+" AND ITM.DIST_CHANNELCD(+) = PDR.DIST_CHANNELCD AND ITM.ITEMCD(+) = PDR.ITEMCD", tbl_PDR );
			Joinable tbl_OCL = new JoinableImpl( "OCL", "DPR_ORDCLOSE", "OCL.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD AND OCL.BRANDCD(+) = ITM.BRANDCD", tbl_ITM );
			append( Schema.makeCloseItemQueryableFields("OCL", tbl_OCL) );

			Joinable tbl_OITM = new JoinableImpl( "OITM", "DPR_ORDER_ITEM" , "OITM.ORGANIZATIONCD(+) = PDR.ORGANIZATIONCD"
						+" AND OITM.DIST_CHANNELCD(+) = PDR.DIST_CHANNELCD AND OITM.ITEMCD(+) = PDR.ITEMCD AND OITM.PARTYCD(+) = PDR.PARTYCD", tbl_PDR );
			append( new QueryableField[]{
					  new QueryableFieldImpl( Schema.INTEGER, "pdMoqDay", "(SELECT SB.PDMOQ_PC_DAY FROM DPR_MOQITEM_CFG SB"
							+ " WHERE SB.ORGANIZATIONCD = PDR.CFG_ORG"
							+ " AND SB.DIST_CHANNELCD = PDR.CFG_DCH"
							+ " AND SB.OFFICECD = PDR.CFG_OFC"
							+ " AND SB.GROUPCD = PDR.CFG_GRP"
							+ " AND SB.PARTYCD = PDR.CFG_PTY"
							+ " AND SB.ITEMCD = PDR.ITEMCD)", "DPR_MOQITEM_PACKDEALMOQ_DAY", tbl_PDR )
					, new QueryableFieldImpl( Schema.INTEGER, "pdMoqMonth", "(SELECT SB.PDMOQ_PC_MONTH FROM DPR_MOQITEM_CFG SB"
							+ " WHERE SB.ORGANIZATIONCD = PDR.CFG_ORG"
							+ " AND SB.DIST_CHANNELCD = PDR.CFG_DCH"
							+ " AND SB.OFFICECD = PDR.CFG_OFC"
							+ " AND SB.GROUPCD = PDR.CFG_GRP"
							+ " AND SB.PARTYCD = PDR.CFG_PTY"
							+ " AND SB.ITEMCD = PDR.ITEMCD)", "DPR_MOQITEM_PACKDEALMOQ_MONTH", tbl_PDR )
					, new QueryableFieldImpl( STRING, "salesUnit", "IMTS.SALES_UNIT", tbl_IMTS )
					, new QueryableFieldImpl( Schema.CODE, "itemConsumerEANCode", "IMEAN.ITEM_CONS_EAN", tbl_IMEAN )
					, new QueryableFieldImpl( STRING, "isSslBase", "DECODE(ITM.ITEMCD,NULL,'N','Y')", tbl_ITM )
					, new QueryableFieldImpl( STRING, "isSslOrder", "DECODE(OITM.ITEMCD,NULL,'N','Y')", tbl_OITM )
					, new QueryableFieldImpl( CODE, "brandCode", "ITM.BRANDCD", tbl_ITM )
					, new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
							, "IMTD.ITEMCD(+) = "+((Table)this.getBaseJoinable()).getTableAlias()+".ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" ) )
			});
		}

			@Override
			public boolean appendCondition(ConditionQueryBuffer querybuf) {
				boolean hasCondition = super.appendCondition(querybuf);

				if( "cfg".equals(querybuf.getConditionValue(Condition.BASIS_CONDITIONKEY, STRING)) ) {
					String dummyResultQuery = ""
							+ "(SELECT CFG.*, ITM.ITEMCD, ITM.DISCOUNT_RATE, ITM.DISPLAY_SEQ"
							+ " , CFG.ORGANIZATIONCD CFG_ORG, CFG.DIST_CHANNELCD CFG_DCH, CFG.OFFICECD CFG_OFC, CFG.GROUPCD CFG_GRP, CFG.PARTYCD CFG_PTY"
							+ " FROM DPR_PACKDEAL_CFG CFG, DPR_PACKDEAL_ITEM ITM WHERE CFG.DEALCD = ITM.DEALCD)";
					querybuf.replaceTable(dummyResultQuery, "PDR");
				}

				String isSellingSku = (String)querybuf.getConditionValue( "isSellingSku", STRING );
				String organizationCode = (String)querybuf.getConditionValue("organizationCode", STRING );
				String distributionChannelCode = (String)querybuf.getConditionValue("distributionChannelCode", STRING );
				String partyCode = (String)querybuf.getConditionValue("partyCode", STRING );
				if( "Y".equals(isSellingSku) && organizationCode != null && distributionChannelCode != null && partyCode != null ) {
					querybuf.appendCondition("EXISTS (SELECT NULL FROM DPR_ORDER_ITEM SB"
							+ " WHERE SB.ITEMCD(+) = PDITM.ITEMCD"
							+ " AND SB.DIVISIONCD = '10'"
							+ " AND SB.ORGANIZATIONCD(+) = ?"
							+ " AND SB.DIST_CHANNELCD(+) = ?"
							+ " AND SB.PARTYCD(+) = ?)", new Object[]{organizationCode, distributionChannelCode, partyCode});
					hasCondition = true;
				}

				return hasCondition;
			};
		} );


		/***************************************************************************************************
			*	DPR_PROMOTION_ITEM
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( OP, "startDate",			"STARTDATE",				"DPR_PROMOTIONITEM_STARTDATE",	DATE )
			, createFD( OP, "endDate",				"ENDDATE",					"DPR_PROMOTIONITEM_ENDDATE",	DATE )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PROMOTION_ITEM, table = createTable("DPR_PROMOTION_ITEM", "PITM", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_STOPITEM_CFG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( PM, "officeCode",			"OFFICECD",					"DPR_PARTY_SALESOFFICE_CODE",	0, 8 )
			, createFD( PM, "groupCode",			"GROUPCD",					"DPR_PARTY_SALESGROUP_CODE",	0, 8 )
			, createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( MD, "stopStartDate",		"STOP_STARTDATE",			"DPR_STOPITEM_STARTDATE",		DATE )
			, createFD( MD, "stopEndDate",			"STOP_ENDDATE",				"DPR_STOPITEM_ENDDATE",			DATE )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_STOPITEM_CFG, table = createTable("DPR_STOPITEM_CFG", "STPITM", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_STOPITEM_CFG, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();


			// beware partyName is not uniq by its code only may need organizationCode and distributionChannelCode
			append( new QueryableFieldImpl( STRING, "partyName"
						  , "(SELECT MAX(PARTYNAME) FROM DPR_PARTY_SALES SB WHERE SB.PARTYCD = "+baseAlias+".PARTYCD AND SB.ORGANIZATIONCD = "+baseAlias+".ORGANIZATIONCD)"
						  , "DPR_PARTY_SALES_PARTYNAME" )
					);

			append( new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = "+baseAlias+".ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" ) ));
			append( Schema.makeStopItemQueryableFields("STPITM", null) );
			append( Schema.makeMasterNames(baseAlias) );
			append( Schema.makeItemEANQueryableFields( "IMEAN", baseAlias, false ) );
		}} );


		/***************************************************************************************************
		*	DPR_STOPITEM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( RD, "officeCode",			"OFFICECD",					"DPR_PARTY_SALESOFFICE_CODE",	0, 8 )
			, createFD( RD, "groupCode",			"GROUPCD",					"DPR_PARTY_SALESGROUP_CODE",	0, 8 )
			, createFD( MD, "stopStartDate",		"STOP_STARTDATE",			"DPR_STOPITEM_STARTDATE",		DATE )
			, createFD( MD, "stopEndDate",			"STOP_ENDDATE",				"DPR_STOPITEM_ENDDATE",			DATE )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_STOPITEM, table = createTable("vwDPR_STOPITEM_CFGRLT", "STIMRT", tfields, "UPGDATE = SYSDATE") );
		putQueryable(DPR_STOPITEM, new QueryableImpl(table) {{
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();

			// beware partyName is not uniq by its code only may need organizationCode and distributionChannelCode
			append( new QueryableFieldImpl( STRING, "partyName"
						  , "(SELECT MAX(PARTYNAME) FROM DPR_PARTY_SALES SB WHERE SB.PARTYCD = "+baseAlias+".PARTYCD AND SB.ORGANIZATIONCD = "+baseAlias+".ORGANIZATIONCD)"
						  , "DPR_PARTY_SALES_PARTYNAME")
					);
			append( new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = "+baseAlias+".ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" ) ));

			append( Schema.makeStopItemQueryableFields("STIMRT", null) );
			append( makeMasterNames(baseAlias) );
			append( Schema.makeItemEANQueryableFields( "IMEAN", baseAlias, false ) );
		}} );


		/***************************************************************************************************
			*	DPR_PLANT
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "plantCode",			"PLANT_CD",					"DPR_PLANT_CODE",				0, 4 )
			, createFD( OP, "plantName",			"PLANT_NAME",				"DPR_PLANT_NAME",				0, 30 )
			, createFD( OP, "plantLocalName",		"LOCAL_PLANT_NAME",			"DPR_PLANT_LOCAL_NAME",			0, 60 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PLANT, table = createTable("DPR_PLANT", "PLT", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PLANT, new QueryableImpl(table) {{

			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES" , "PTYS.DELIVERY_PLANT = PLT.PLANT_CD" );

			QueryableField[] qfields = new QueryableField[] {
					  new QueryableFieldImpl( STRING, "countryCode", "PTYS.COUNTRYCD", tbl_PTYS )
					, new QueryableFieldImpl( STRING, "organizationCode", "PTYS.ORGANIZATIONCD", tbl_PTYS )
			};

			QueryBufferValid valid_PTYS = new QueryBufferValid.Condition( "countryCode", "organizationCode" );
			append( ConditionalQueryableField.makeConditionalQueryableFields(valid_PTYS, qfields) );

			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "linkPlantCode", "PLT.PLANT_CD", "DPR_PLANT_CODE", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "linkPlantName", "PLT.PLANT_NAME", "DPR_PLANT_NAME", tbl_PTYS )
			});

		}} );


		/***************************************************************************************************
			*	DPR_PLANT_ITEM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "plantCode",			"PLANTCD",					"DPR_PLANT_CODE",				0, 4 )
			, createFD( PM, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_PLANTITEM_SHIPPARTYCODE",	0, 6 )
			, createFD( PM, "officeCode",			"OFFICECD",					"DPR_SALESOFFICE_CODE",			0, 4 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( MD, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "99" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PLANT_ITEM, table = createTable("DPR_PLANT_ITEM", "PTITM", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PLANT_ITEM, new QueryableImpl(table) {{

			Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = PTITM.ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" );

			append( new QueryableField[] {
						  new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", tbl_DESC )
						, new QueryableFieldImpl( CODE, "plantExclType", "(CASE WHEN PTITM.SHIP_PARTYCD != '0' THEN 'SH' WHEN PTITM.OFFICECD != '0' AND PTITM.OFFICECD IS NOT NULL THEN 'SF' ELSE 'SO' END)" )
			});
		}} );


		/***************************************************************************************************
			*	DPR_PLANT_RCV
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "plantCode",			"PLANTCD",					"DPR_PLANT_CODE",				0, 8 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_UPLOADORD_ITEMCODE",		0, 20 )
			, createFD( MD, "brandCode",			"BRANDCD",					"DPR_BRAND_CODE",				0, 20 )
			, createFD( MD, "recovery",				"RECOVERY",					"DPR_UPLOADRCV_RECOVERY",		DESC, 0, 256 )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PLANT_RCV, table = createTable("DPR_PLANT_RCV", "PTRCV", tfields ) );
		putQueryable( DPR_PLANT_RCV, new QueryableImpl(table) {{

			Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = PTRCV.ITEMCD AND IMTD.LANGCD(+) = ?", "displayLanguage" );
			Joinable tbl_USR = new JoinableImpl( "USR", "USR_USER", "USR.UNIQID = PTRCV.UPGUSERID" );
			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES" , "PTYS.DELIVERY_PLANT(+) = PTRCV.PLANTCD" );

			append( new QueryableField[] {
						new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", "DPR_ITEM_MASTER_NAME", tbl_DESC )
					  , new QueryableFieldImpl( STRING, "userName", "USR.USER_NAME", tbl_USR )
			});
		}} );


		/***************************************************************************************************
		*	DPR_RDD_TRG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "trgKey",				"TRG_KEY",					"DPR_RDD_TRG_KEY",				0, 20 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( PM, "officeCode",			"OFFICECD",					"DPR_PARTY_SALESOFFICE_CODE",	0, 8 )
			, createFD( MD, "dayOfWeek",			"DAY_OF_WEEK",				"DPR_RDDTRG_DAYOFWEEK",			0, 20 )
			, createFD( MD, "allowDays",			"ALLOW_DAYS",				"DPR_RDDTRG_ALLOWDAYS",			0, 20 )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_RDD_TRG, table = createTable("DPR_RDD_TRG", "RDTG", tfields) );
		putQueryable( DPR_RDD_TRG, new QueryableImpl(table) {{
			append( new QueryableField[] {
				  new QueryableFieldImplBK( DESC, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = RDTG.ORGANIZATIONCD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_ORGANIZATION_NAME" )
				, new QueryableFieldImplBK( STRING, "distributionChannelName"
						, "( SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = RDTG.DIST_CHANNELCD"
							+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DISTRIBUTIONCHANNEL_NAME" )
				, new QueryableFieldImplBK( DESC, "officeName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = RDTG.OFFICECD"
							+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_OFFICE_NAME" )
			} );
		}} );


		/***************************************************************************************************
		*	DPR_RDD_IND
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( PM, "partyCode",			"SOLD_PARTYCD",				"DPR_RDDIND_SOLDPARTYCD",		0, 15 )
			, createFD( OP, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_RDDIND_SHIPPARTYCD",		0, 15 )
			, createFD( MD, "usePreDefined",		"USE_PREDEFINED",			"DPR_RDDIND_USEPREDEFEIND",		"DPR_RDD_IND_", "Y,N" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_RDD_IND, table = createTable("DPR_RDD_IND", "RDI", tfields) );
		putQueryable( DPR_RDD_IND, new QueryableImpl(table) {{
			append( new QueryableField[] {
				  new QueryableFieldImplBK( DESC, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = RDI.ORGANIZATIONCD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_ORGANIZATION_NAME" )
				, new QueryableFieldImplBK( STRING, "distributionChannelName"
						, "( SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = RDI.DIST_CHANNELCD"
							+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DISTRIBUTIONCHANNEL_NAME" )
				, new QueryableFieldImplBK( STRING, "partyName"
						, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
								+" WHERE SB.PARTYCD = RDI.SOLD_PARTYCD AND SB.ORGANIZATIONCD = RDI.ORGANIZATIONCD"
									+" AND SB.DIST_CHANNELCD = RDI.DIST_CHANNELCD AND SB.DIVISIONCD = ?)", "divisionCode" )
				, new QueryableFieldImplBK( STRING, "shipPartyName"
						, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
								+" WHERE SB.PARTYCD = RDI.SHIP_PARTYCD AND SB.ORGANIZATIONCD = RDI.ORGANIZATIONCD"
									+" AND SB.DIST_CHANNELCD = RDI.DIST_CHANNELCD AND SB.DIVISIONCD = ?)", "divisionCode" )
			} );
		}} );


		/***************************************************************************************************
		 *	DPR_PARTY_OPER
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 2 )
			, createFD( PM, "officeCode",			"OFFICECD",					"DPR_PARTY_SALESOFFICE_CODE",	0, 8 )
			, createFD( PM, "groupCode",			"GROUPCD",					"DPR_SALESGROUP_CODE",	0, 8 )
			, createFD( PM, "patternType",			"PTN_TYPE",					"SCHEDULE_PATTERN_TYPE",		"DPR_PARTYOPER_PTN_TYPE_", "0,1,2,3,4,5,6" )
			, createFD( PM, "patternIndex",			"PTN_INDEX",				"SCHEDULE_PATTERN_INDEX",		INTEGER, 0, 99 )
			, createFD( PM, "patternDate",			"PTN_DATE",					"SCHEDULE_PATTERN_DATE",		DATE )
			, createFD( OP, "name",					"OPER_NAME",				"DPR_PARTYOPER_NAME",			DESC, 0, 128 )
			, createFD( OP, "orderInd",				"ORDER_IND",				"DPR_PARTYOPER_ORDERIND",		"DPR_PARTYOPER_ORDERIND_", "Y,N" )
			, createFD( OP, "delvInd",				"DELV_IND",					"DPR_PARTYOPER_DELVIND",		"DPR_PARTYOPER_DELVIND_", "Y,N" )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_PARTY_OPER, table = createTable("DPR_PARTY_OPER", "POP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_PARTY_OPER, new QueryableImpl(table) {
			{
				append( new QueryableField[] {
					  new QueryableFieldImpl( CODE, "availableInd"
							, "(CASE WHEN POP.PTN_TYPE <> '0' OR POP.PTN_DATE >= TRUNC(pkCustom.fCurrentDate(POP.ORGANIZATIONCD)) THEN 'Y' ELSE 'N' END)"
							, "DPR_PARTYOPER_AVAILABLEIND" )
					, new QueryableFieldImplBK( DESC, "organizationName"
								, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = POP.ORGANIZATIONCD"
									+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_ORGANIZATION_NAME" )
					, new QueryableFieldImplBK( STRING, "distributionChannelName"
							, "( SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = POP.DIST_CHANNELCD"
								+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DISTRIBUTIONCHANNEL_NAME" )
					, new QueryableFieldImplBK( DESC, "officeName"
							, "NVL((SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = POP.OFFICECD"
								+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = NVL(?, 'en')), '0')", "displayLanguage", "DPR_SALES_OFFICE_NAME" )
					, new QueryableFieldImplBK( DESC, "groupName"
							, "NVL((SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = POP.GROUPCD"
								+ " AND SB.MASTER_TYPE = 'SG' AND SB.LANGCD = NVL(?, 'en')), '0')", "displayLanguage", "DPR_SALES_GROUP_NAME" )
				} );
			}
			@Override
			public boolean appendCondition( ConditionQueryBuffer querybuf ) {
				boolean hasCondition = super.appendCondition( querybuf );

				Object operDate = querybuf.getConditionValue( "operDate" );
				if( operDate != null ) {
					querybuf.appendConditionByField( "ORGANIZATIONCD", querybuf.getConditionValue("organizationCode") );
					querybuf.appendConditionByField( "DIST_CHANNELCD", querybuf.getConditionValue("distributionChannelCode") );
					querybuf.appendConditionByField( "OFFICECD", querybuf.getConditionValue("officeCode") );
					querybuf.appendConditionByField( "GROUPCD", querybuf.getConditionValue("groupCode") );
					Object[] conditionValues = new Object[11];
					for( int i = 0; i < conditionValues.length; i++ ) {
						conditionValues[i] = operDate;
					}
					querybuf.appendCondition( "( ( PTN_TYPE = '5' )"
							+ " OR ( PTN_TYPE = '4' AND PTN_INDEX = TO_CHAR(?, 'D') )"
							+ " OR ( PTN_TYPE = '3' AND PTN_DATE >= ? AND MOD(? - PTN_DATE, PTN_INDEX) = 0 )"
							+ " OR ( PTN_TYPE = '2' AND PTN_INDEX = CEIL( TO_CHAR(?, 'DD')/7 ) * 10 + TO_CHAR(?, 'D') )"
							+ " OR ( PTN_TYPE = '1' AND PTN_INDEX IN (TO_CHAR(?, 'DD'), DECODE(?, LAST_DAY(?), '0', TO_CHAR(?, 'DD'))) )"
							+ " OR ( PTN_TYPE = '0' AND PTN_DATE = ? )"
							+ " OR ( PTN_TYPE = '6' AND TO_CHAR(PTN_DATE, 'MMDD') = TO_CHAR(?, 'MMDD') ) )", conditionValues );
				}

				return hasCondition;
			}
		} );


		/***************************************************************************************************
			*	DPR_ORDER_ITEM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( OP, "newItemInd",			"NEWITEM_IND",				"DPR_ORDERITEM_NEWITEM_IND",	0, 1 )
			, createFD( OP, "promotionInd",			"PROMOTION_IND",			"DPR_ORDERITEM_PROMOTION_IND",	0, 1 )
			, createFD( OP, "startAvailDate",		"STARTAVAIL_DATE",			"DPR_ITEM_STARTAVAILDATETIME",	DATE )
			, createFD( OP, "endAvailDate",			"ENDAVAIL_DATE",			"DPR_ITEM_ENDAVAILDATETIME",	DATE )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDER_ITEM, table = createTable("DPR_ORDER_ITEM", "OITM", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_ITEM, new QueryableImpl(table) {{
			Joinable tbl_ITM = new JoinableImpl( "ITM", "DPR_ITEM"
					, "ITM.COUNTRYCD(+) = OITM.COUNTRYCD AND ITM.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD"
						+" AND ITM.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD AND ITM.ITEMCD(+) = OITM.ITEMCD" );
			Joinable tbl_PITM = new JoinableImpl( "PITM", "DPR_PROMOTION_ITEM", "PITM.PARTYCD(+) = OITM.PARTYCD"
					+ " AND PITM.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND PITM.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD"
					+ " AND PITM.ITEMCD(+) = OITM.ITEMCD" );

			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES"
					, "PTYS.PARTYCD = OITM.PARTYCD AND PTYS.ORGANIZATIONCD = OITM.ORGANIZATIONCD"
					+" AND PTYS.DIST_CHANNELCD = OITM.DIST_CHANNELCD AND PTYS.DIVISIONCD = OITM.DIVISIONCD" );

			Joinable tbl_INTRO = new JoinableImplBK( "IMTI", "DPR_ITEM_MASTER_INTRO"
					, "IMTI.ITEMCD(+) = ITM.ITEMCD AND IMTI.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD AND IMTI.LANGCD(+) = ?"
					, "displayLanguage", tbl_ITM );

			JoinableImpl tbl_IMTS = new JoinableImpl( "IMTS", "DPR_ITEM_MASTER_SALES"
					, "IMTS.ITEMCD(+) = OITM.ITEMCD AND IMTS.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND IMTS.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD" );
			JoinableImpl tbl_UOMIMT = new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMT.ITEM_CD(+) = OITM.ITEMCD" );
			JoinableImpl tbl_UOMBS = new JoinableImpl( "IMUBS", "DPR_ITEM_MASTER_UOM"
					, "IMUBS.ITEMCD(+) = IMT.ITEM_CD AND IMUBS.UOM_CD(+) = IMT.BASE_UOM", tbl_UOMIMT );
			JoinableImpl tbl_SHELF = new JoinableImpl( "MST", "DPR_ITEM_MASTER", "OITM.ITEMCD = MST.ITEM_CD(+)" );

			JoinableImpl tbl_PNITM = new JoinableImpl( "PNITM", "vwDPR_PLANT_EXCL_ITEM"
					, "OITM.ITEMCD = PNITM.ITEMCD(+) AND OITM.ORGANIZATIONCD = PNITM.ORGANIZATIONCD(+)"
							+ " AND OITM.DIST_CHANNELCD = PNITM.DIST_CHANNELCD(+) AND OITM.DIVISIONCD = PNITM.DIVISIONCD(+)"
							+ " AND OITM.COUNTRYCD = PNITM.COUNTRYCD(+) AND OITM.PARTYCD = PNITM.PARTYCD(+)"
					, tbl_PITM );
			Joinable tbl_PTYS_LNK = new JoinableImpl( "PTYS_LNK", "DPR_PARTY_SALES"
					, "PTYS_LNK.PARTYCD = PLNK.LINK_PARTYCD"
							+ " AND PTYS_LNK.ORGANIZATIONCD = PTYS.ORGANIZATIONCD"
							+ " AND PTYS_LNK.DIST_CHANNELCD = PTYS.DIST_CHANNELCD"
							+ " AND PTYS_LNK.DIVISIONCD = PTYS.DIVISIONCD"
							+ " AND PTYS_LNK.STATUS = '00'"
					, tbl_PNITM );
			Joinable tbl_PLNK = new JoinableImpl( "PLNK", "DPR_PARTY_LINK"
					, "PLNK.LINKTYPE IN ( 'WE', 'SH' ) AND PLNK.PARTYCD = PTYS.PARTYCD"
					, tbl_PNITM );
			Joinable tbl_PTY = new JoinableImpl( "PTYS", "DPR_PARTY_SALES"
					, "PTYS.PARTYCD = OITM.PARTYCD"
							+ " AND PTYS.ORGANIZATIONCD = OITM.ORGANIZATIONCD"
							+ " AND PTYS.DIST_CHANNELCD = OITM.DIST_CHANNELCD"
							+ " AND PTYS.DIVISIONCD = OITM.DIVISIONCD"
							+ " AND PTYS.ORGANIZATIONCD = PLNK.ORGANIZATIONCD"
							+ " AND PTYS.DIST_CHANNELCD = PLNK.DIST_CHANNELCD"
							+ " AND PTYS.DIVISIONCD = PLNK.DIVISIONCD"
					, tbl_PLNK );

			Joinable tbl_STIMRT = new JoinableImpl("STIMRT", "vwDPR_STOPITEM_CFGRLT", "STIMRT.ITEMCD(+) = OITM.ITEMCD AND STIMRT.PARTYCD(+) = OITM.PARTYCD"
					+ " AND STIMRT.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND STIMRT.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD");
			append( Schema.makeStopItemQueryableFields("STIMRT", tbl_STIMRT) );

			Joinable tbl_PDRLT = new JoinableImplBK( "PDRLT", "vwDPR_PACKDEAL_CFGRLT"
					, "PDRLT.DEALCD(+) = PDITM.DEALCD AND PDRLT.PARTYCD = OITM.PARTYCD AND PDRLT.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND PDRLT.DIST_CHANNELCD = OITM.DIST_CHANNELCD"
					, new String[]{ "organizationCode", "distributionChannelCode", "partyCode" } );
			append( Schema.makePackdealDateQueryableFields("PDRLT", tbl_PDRLT) );

			append( Schema.makeRegularItemQueryableFields( DPR_ORDER_ITEM, "SIMHSTC", "OITM" ) );
			append( Schema.makeItemEANQueryableFields( "IMEAN", "OITM", false ) );
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();
			append( new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = "+baseAlias+".ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" ) ));
			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "partyName", "PTYS.PARTYNAME", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "officeCode", "PTYS.OFFICECD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "groupCode", "PTYS.GROUPCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "districtCode", "PTYS.DISTRICTCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "productCategoryCode", "ITM.PCATECD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "megabrandCode", "ITM.MEGABRANDCD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "brandCode", "ITM.BRANDCD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "baseproductCode", "ITM.BASEPRODUCTCD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "variantCode", "ITM.VARIANTCD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "putupCode", "ITM.PUTUPCD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "editableDescription", "ITM.EDIT_DESCRIPTION", tbl_ITM )
				, new QueryableFieldImpl( STRING, "intro", "IMTI.INTRO", tbl_INTRO )
				, new QueryableFieldImpl( STRING, "editableIntro", "IMTI.EDIT_INTRO", tbl_INTRO )
				, new QueryableFieldImpl( STRING, "nvlIntro", "NVL(IMTI.EDIT_INTRO, IMTI.INTRO)", tbl_INTRO )
				, new QueryableFieldImpl( STRING, "priceCurrency", "ITM.PRICE_CURR", "DPR_ITEM_PRICECURRENCY", tbl_ITM )
				, new QueryableFieldImpl( INTEGER, "price", "ITM.PRICE", "DPR_ITEM_PRICE", tbl_ITM )
				, new QueryableFieldImpl( STRING, "newItemInd", "ITM.NEWITEM_IND", tbl_ITM )
				, new QueryableFieldImpl( STRING, "promotionInd", "(CASE WHEN PITM.STARTDATE <= TRUNC(pkCustom.fCurrentDate(OITM.ORGANIZATIONCD))"
						+ " AND PITM.ENDDATE >= TRUNC(pkCustom.fCurrentDate(OITM.ORGANIZATIONCD)) THEN 'Y' ELSE 'N' END)", tbl_PITM )
				, new QueryableFieldImpl( STRING, "shelfLife", "MST.SHELFLIFE", tbl_SHELF )
				, new QueryableFieldImpl( STRING, "plantInd", "DECODE( PTYS_LNK.DELIVERY_PLANT, PNITM.PLANTCD, 'Y', 'N' )", tbl_PTYS_LNK )
				, new QueryableFieldImpl( CODE, "plantExclType", "PNITM.EXCLTYPE", tbl_PNITM )
				, new QueryableFieldImpl( STRING, "shipPartyCode", "PLNK.LINK_PARTYCD", tbl_PLNK )
				, new QueryableFieldImpl( STRING, "testPart", "DECODE( ITM.CHAIN_STATUS, '04', ITM.CHAIN_STATUS, 'XX' )", tbl_ITM )
				, new QueryableFieldImpl( STRING, "withPlantCount", "COUNT(*)", tbl_PTY )
				, new QueryableFieldImpl( STRING, "salesUnit", "IMTS.SALES_UNIT", tbl_IMTS )
			} );

			QueryBufferValid queryValid_orderKey = new QueryBufferValid.Condition( "orderKey" );
			appendCND( queryValid_orderKey, new QueryableField[] {
				new QueryableFieldImplBK( STRING, "existingOrderInd", "DECODE( (SELECT COUNT(*) FROM DPR_ORDER_DTL ODTL WHERE ODTL.ORDERKEY = ? AND ODTL.ITEMCD = OITM.ITEMCD), 0, 'N', 'Y')", new String[] { "orderKey" } )
			} );

			QueryBufferValid queryValid_dangerous = new QueryBufferValid.Condition( "dangerousPlant", "dangerousNumber" );
			Joinable tbl_IMSP = new JoinableImplBK( "IMTP", "DPR_ITEM_MASTER_PLANT", "IMTP.ITEMCD(+) = ITM.ITEMCD AND IMTP.PLANT(+) = ? AND IMTP.LOADING_GRP(+) = ?"
					, new String[] { "dangerousPlant", "dangerousNumber" }, tbl_ITM );
			appendCND( queryValid_dangerous, new QueryableField[] {
				new QueryableFieldImplBK( STRING, "dangerousInd", "DECODE(LOADING_GRP, ?, 'Y', 'N')", "dangerousNumber",  tbl_IMSP )
			} );

			Joinable tbl_OCL = new JoinableImpl( "OCL", "DPR_ORDCLOSE", "OCL.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD AND OCL.BRANDCD(+) = ITM.BRANDCD", tbl_ITM );
			append( Schema.makeCloseItemQueryableFields("OCL", tbl_OCL) );

			append( makeMaterialGroupFields("ITM", tbl_ITM) );
			append( makeMaterialHierarchyFields("ITM", tbl_ITM) );

			QueryBufferValid queryValid_mstOrgCode = new QueryBufferValid.Condition( "masterOrganizationCode", "displayLanguage" );
			appendCND( queryValid_mstOrgCode,
					makeMaterialHierarchyFieldBKs( "ITM", tbl_ITM, masterDescOrgBKKeys )
			);
		}

			@Override
			public boolean appendCondition( ConditionQueryBuffer querybuf ) {
				boolean hasCondition = super.appendCondition( querybuf );

				if( querybuf.hasConditionValue("dangerousInd") ) {
					boolean useDangerousItem = (Boolean)querybuf.getConditionValue("useDangerousItem");
					if( useDangerousItem && querybuf.hasConditionValue("dangerousNumber") && querybuf.hasConditionValue("dangerousInd") ) {
						Object[] bindValues = { querybuf.getConditionValue("dangerousNumber"),  querybuf.getConditionValue("dangerousInd") };
						querybuf.appendCondition( "DECODE(IMTP.LOADING_GRP, ?, 'Y', 'N') = ?", bindValues );
						hasCondition = true;
					}
				}
				return hasCondition;
			}
		} );

		/***************************************************************************************************
		*	vwDPR_ORDER_ITEM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( RD, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( RD, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_SHIP_PARTY_CODE",			0, 15 )
			, createFD( RD, "shipPlantCode",		"SHIP_PLANTCD",				"DPR_SHIP_PLANT_CODE",			0, 5 )
			, createFD( RD, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( RD, "distributionChannelCode",	"DIST_CHANNELCD",		"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( RD, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( RD, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( RD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( RD, "newItemInd",			"NEWITEM_IND",				"DPR_ORDERITEM_NEWITEM_IND",	0, 1 )
			, createFD( RD, "promotionInd",			"PROMOTION_IND",			"DPR_ORDERITEM_PROMOTION_IND",	0, 1 )
			, createFD( RD, "startAvailDate",		"STARTAVAIL_DATE",			"DPR_ITEM_STARTAVAILDATETIME",	DATE )
			, createFD( RD, "endAvailDate",			"ENDAVAIL_DATE",			"DPR_ITEM_ENDAVAILDATETIME",	DATE )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( RD, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( vwDPR_ORDER_ITEM, table = createTable("vwDPR_ORDER_ITEM", "OITM", tfields, "UPGDATE = SYSDATE") );
		putQueryable( vwDPR_ORDER_ITEM, new QueryableImpl(table) {{
			Joinable tbl_ITM = new JoinableImpl( "ITM", "DPR_ITEM"
					, "ITM.COUNTRYCD(+) = OITM.COUNTRYCD AND ITM.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD"
						+" AND ITM.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD AND ITM.ITEMCD(+) = OITM.ITEMCD" );
			Joinable tbl_PITM = new JoinableImpl( "PITM", "DPR_PROMOTION_ITEM", "PITM.PARTYCD(+) = OITM.PARTYCD"
					+ " AND PITM.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND PITM.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD"
					+ " AND PITM.ITEMCD(+) = OITM.ITEMCD" );
			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES"
					, "PTYS.PARTYCD = OITM.PARTYCD AND PTYS.ORGANIZATIONCD = OITM.ORGANIZATIONCD"
					+" AND PTYS.DIST_CHANNELCD = OITM.DIST_CHANNELCD AND PTYS.DIVISIONCD = OITM.DIVISIONCD" );

			Joinable tbl_PNITM = new JoinableImpl( "PNITM", "vwDPR_PLANT_EXCL_ITEM"
					, "OITM.ITEMCD = PNITM.ITEMCD(+)"
							+ " AND OITM.ORGANIZATIONCD = PNITM.ORGANIZATIONCD(+)"
							+ " AND OITM.DIST_CHANNELCD = PNITM.DIST_CHANNELCD(+)"
							+ " AND OITM.DIVISIONCD = PNITM.DIVISIONCD(+)"
							+ " AND OITM.COUNTRYCD = PNITM.COUNTRYCD(+)"
							+ " AND OITM.PARTYCD = PNITM.PARTYCD(+)"
							+ " AND OITM.SHIP_PARTYCD = PNITM.SHIP_PARTYCD(+)"
					, tbl_PITM );

			Joinable tbl_STIMRT = new JoinableImpl("STIMRT", "vwDPR_STOPITEM_CFGRLT", "STIMRT.ITEMCD(+) = OITM.ITEMCD AND STIMRT.PARTYCD(+) = OITM.PARTYCD"
					+ " AND STIMRT.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND STIMRT.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD");
			append( Schema.makeStopItemQueryableFields("STIMRT", tbl_STIMRT) );

			Joinable tbl_PDR = new JoinableImpl( "PDR", "vwDPR_PACKDEAL"
					, "PDIMRT.ITEMCD(+) = OITM.ITEMCD AND PDIMRT.PARTYCD(+) = OITM.PARTYCD"
					+ " AND PDIMRT.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND PDIMRT.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD" );
			append( Schema.makePackdealDateQueryableFields("PDR", tbl_PDR) );

			append( makeMasterNames("PTYS", tbl_PTYS, "officeName", "groupName") );
			append( Schema.makeRegularItemQueryableFields( vwDPR_ORDER_ITEM, "SIMHST", "OITM" ) );
			append( Schema.makeItemEANQueryableFields( "IMEAN", "OITM", false ) );
			String baseAlias = ((Table)this.getBaseJoinable()).getTableAlias();
			append( new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = "+baseAlias+".ITEMCD AND IMTD.LANGCD(+) = NVL(?, 'en')", "displayLanguage" ) ));
			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "partyName", "PTYS.PARTYNAME", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "officeCode", "PTYS.OFFICECD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "groupCode", "PTYS.GROUPCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "districtCode", "PTYS.DISTRICTCD", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "productCategoryCode", "ITM.PCATECD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "megabrandCode", "ITM.MEGABRANDCD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "brandCode", "ITM.BRANDCD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "baseproductCode", "ITM.BASEPRODUCTCD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "variantCode", "ITM.VARIANTCD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "putupCode", "ITM.PUTUPCD", tbl_ITM )
				, new QueryableFieldImpl( STRING, "editableDescription", "ITM.EDIT_DESCRIPTION", tbl_ITM )
				, new QueryableFieldImpl( STRING, "priceCurrency", "ITM.PRICE_CURR", "DPR_ITEM_PRICECURRENCY", tbl_ITM )
				, new QueryableFieldImpl( INTEGER, "price", "ITM.PRICE", "DPR_ITEM_PRICE", tbl_ITM )
				, new QueryableFieldImpl( STRING, "newItemInd", "ITM.NEWITEM_IND", tbl_ITM )
				, new QueryableFieldImpl( STRING, "promotionInd", "(CASE WHEN PITM.STARTDATE <= TRUNC(pkCustom.fCurrentDate(OITM.ORGANIZATIONCD))"
						+ " AND PITM.ENDDATE >= TRUNC(pkCustom.fCurrentDate(OITM.ORGANIZATIONCD)) THEN 'Y' ELSE 'N' END)", tbl_PITM )
				, new QueryableFieldImpl( STRING, "plantInd", "DECODE( OITM.SHIP_PLANTCD, PNITM.PLANTCD, 'Y', 'N' )", tbl_PNITM )
				, new QueryableFieldImpl( CODE, "plantExclType", "PNITM.EXCLTYPE", tbl_PNITM )
				, new QueryableFieldImpl( STRING, "testPart", "DECODE( ITM.CHAIN_STATUS, '04', ITM.CHAIN_STATUS, 'XX' )", tbl_ITM )
				, new QueryableFieldImpl( STRING, "withPlantCount", "COUNT(*)", tbl_PNITM )
				, new QueryableFieldImpl( STRING, "sslCount", "COUNT(DISTINCT OITM.PARTYCD || OITM.ITEMCD)", tbl_PTYS )
			} );

			QueryBufferValid queryValid_orderKey = new QueryBufferValid.Condition( "orderKey" );
			appendCND( queryValid_orderKey, new QueryableField[] {
						new QueryableFieldImplBK( STRING, "existingOrderInd", "DECODE( (SELECT COUNT(*) FROM DPR_ORDER_DTL ODTL WHERE ODTL.ORDERKEY = ? AND ODTL.ITEMCD = OITM.ITEMCD), 0, 'N', 'Y')", new String[] { "orderKey" } )
			} );

			Joinable tbl_OCL = new JoinableImpl( "OCL", "DPR_ORDCLOSE", "OCL.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD AND OCL.BRANDCD(+) = ITM.BRANDCD", tbl_ITM );
			append( Schema.makeCloseItemQueryableFields("OCL", tbl_OCL) );

			append( makeMaterialGroupFields("ITM", tbl_ITM) );
			append( makeMaterialHierarchyFields("ITM", tbl_ITM) );

			QueryBufferValid queryValid_mstOrgCode = new QueryBufferValid.Condition( "masterOrganizationCode", "displayLanguage" );
			appendCND( queryValid_mstOrgCode,
					makeMaterialHierarchyFieldBKs( "ITM", tbl_ITM, masterDescOrgBKKeys )
			);
		}} );


		/***************************************************************************************************
			*	DPR_SIMULATION
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "simulationKey",		"SIMULATION_KEY",			"DPR_SIMULATION_KEY",			0, 35 )
			, createFD( MD, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( MD, "simulationDate",		"SIMULATION_DATE",			"DPR_SIMULATION_DATE",			DATE )
			, createFD( MD, "orderDate",			"ORDDATE",					"DPR_SIMULATION_ORDERDATE",		DATE )
			, createFD( MD, "inDate",				"INDATE",					"DPR_SIMULATION_INDATE",		DATE )
			, createFD( OP, "inDateConfirm",		"INDATE_CNF",				"DPR_SIMULATION_INDATECONFIRM",	DATE )
			, createFD( OP, "simulationResultNumber","SIMULRESULT_NUMBER",		"DPR_SIMULATION_RESULTNUMBER",	0, 20 )
			, createFD( OP, "orderNumber",			"ORDER_NUMBER",				"DPR_SIMULATION_ORDERNUMBER",	0, 35 )
			, createFD( OP, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( OP, "distributionChannelCode","DIST_CHANNELCD",			"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( OP, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( OP, "payerPartyCode",		"PAYER_PARTYCD",			"DPR_SIMULATION_PAYERPARTYCODE",0, 15 )
			, createFD( OP, "billPartyCode",		"BILL_PARTYCD",				"DPR_SIMULATION_BILLPARTYCODE",	0, 15 )
			, createFD( MD, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_SIMULATION_SHIPPARTYCODE",	0, 15 )
			, createFD( MD, "soldPartyCode",		"SOLD_PARTYCD",				"DPR_SIMULATION_SOLDPARTYCODE",	0, 15 )
			, createFD( MD, "orderType",			"ORDER_TYPE",				"DPR_SIMULATION_ORDERTYPE",		"DPR_ORDERTYPE_", "NO,PR" )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"DPR_SIMULATION_STATUS_", "IN,SR,SI,ER" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_SIMULATION, table = createTable("DPR_SIMULATION", "SIM", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_SIMULATION_DTL
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "simulationKey",		"SIMULATIONKEY",			"DPR_SIMULATION_KEY",			0, 35 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( OP, "lineNumber",			"LINE_NO",					"DPR_SIMULATIONDTL_LINENUMBER",	0, 10 )
			, createFD( OP, "indateConfirm",		"INDATE_CNF",				"DPR_SIMULATIONDTL_INDATECONFIRM",	DATE )
			, createFD( MD, "simulationQty",		"SIMULATIONQTY",			"DPR_SIMULATIONDTL_QTY",		DOUBLE )
			, createFD( OP, "simulationQtyConfirm",	"SIMULATIONQTY_CNF",		"DPR_SIMULATIONDTL_QTYCONFIRM",	DOUBLE )
			, createFD( MD, "uom",					"UOM",						"DPR_SIMULATIONDTL_UOM",		0, 3 )
			, createFD( OP, "priceCurrency",		"PRICE_CURR",				"DPR_SIMULATIONDTL_PRICECURRENCY",	3, 3 )
			, createFD( OP, "price",				"PRICE",					"DPR_SIMULATIONDTL_PRICE",		DOUBLE )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_SIMULATION_DTL, table = createTable("DPR_SIMULATION_DTL", "SDTL", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			* DPR_SIMULATION_HIST
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "orderKey",			"ORDERKEY",			"ORDERKEY",						STRING )
			, createFD( PM, "fileName",			"FILE_NAME",			"FILE_NAME",						STRING )
			, createFD( PM, "lineNumber",			"LINE_NO",				"LINE_NO",							INTEGER )
			, createFD( MR, "organizationCode",	"ORGANIZATIONCD",		"DPR_ORDER_ORGANIZATIONCODE",		0, 8 )
			, createFD( MR, "distributionChannelCode",	"DIST_CHANNELCD",	"DPR_ORDER_DISTRIBUTIONCHANNELCODE",	0, 8 )
			, createFD( MR, "partyCode",			"PARTYCD",				"DPR_ORDER_SOLDPARTYCODE",			0, 15 )
			, createFD( MR, "shipPartyCode",		"SHIP_PARTYCD",		"DPR_ORDER_SHIPPARTYCODE",			0, 15 )
			, createFD( MR, "orderDate",			"ORDDATE",				"DPR_ORDER_ORDERDATE",				DATE )
			, createFD( MR, "simulationOutDateTime",	"SIM_OUT_DATETIME",			"DPR_ORDER_SIMULATIONDATE",		DATETIME )
			, createFD( MD, "itemCode",			"ITEMCD",				"DPR_ITEM_CODE",					STRING )
			, createFD( MD, "itemCodeConfirmed",	"ITEMCD_CNF",			"DPR_ORDERDTL_ITEMCODE_CONFIRMED",	STRING )
			, createFD( MD, "orderQty",			"SIM_OUT_QTY",			"DPR_ORDERDTL_ORDERQTY",			INTEGER )
			, createFD( OP, "simulationUOM",		"SIM_OUT_UOM",			"DPR_ORDERDTL_UOM",				STRING )
			, createFD( OP, "price",				"SIM_OUT_PRICE",		"DPR_ORDERDTL_PRICE",				DOUBLE )
			, createFD( RD, "status",				"STATUS",				"STATUS",							"PUB_STATUS_",		"00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",		"UPGUSERID",			"UPDATEUSERID",				0, 30 )
		};
		putTable( DPR_SIMULATION_HIST, table = createTable("DPR_SIMULATION_HIST", "SIMHST", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	DPR_ORDCLOSE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",		"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "brandCode",			"BRANDCD",				"DPR_BRAND_CODE",				0, 15 )
			, createFD( MD, "ordCloseTime",			"CLOSE_TIME",			"DPR_ORDCLOSE_TIME",			TIME )
			, createFD( MD, "ordCloseTimeZone",		"CLOSE_TIMEZONE",		"DPR_ORDCLOSE_TIMEZONE",		STRING )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDCLOSE, table = createTable("DPR_ORDCLOSE", "OCL", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDCLOSE, new QueryableImpl(table) {{

			append( com.irt.dpr.Schema.makeMaterialGroupFields("OCL", null, "brandName") );

			Joinable tbl_UPT = new JoinableImpl( "UPT", "USR_PARTY", "UPT.GLN = CNR.COUNTRY_CD" );
			Joinable tbl_CNR = new JoinableImpl( "CNR", "DPR_COUNTRY", "CNR.COUNTRY_CD = CCND.COUNTRYCD", tbl_UPT );
			Joinable tbl_CCND = new JoinableImpl( "CCND", "DPR_COUNTRY_COND", "CCND.ORGANIZATIONCD = OCL.ORGANIZATIONCD", tbl_CNR );

			append(new QueryableFieldImpl[]{
					  new QueryableFieldImpl( STRING, "timeZone", "UPT.TIMEZONE", tbl_CCND )
					, new QueryableFieldImpl( Schema.TIME, "ordCloseDateTime" , com.irt.dpr.Schema.ordCloseDateTime)
			});
		}});


		/***************************************************************************************************
			*	DPR_ORDER
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "orderKey",				"ORDER_KEY",				"DPR_ORDER_KEY",				0, 20 )
			, createFD( MR, "partyCode",			"PARTYCD",					"DPR_ORDER_PARTYCODE",			0, 15 )
			, createFD( MR, "countryCode",			"COUNTRYCD",				"DPR_ORDER_COUNTRYCODE",		0, 15 )
			, createFD( MR, "orderDate",			"ORDDATE",					"DPR_ORDER_ORDERDATE",			DATE )
			, createFD( MD, "inDate",				"INDATE",					"DPR_ORDER_INDATE",				DATE )
			, createFD( OP, "inDateDefault",		"INDATE_DEF",				"DPR_ORDER_DEFAULTINDATE",		DATE )
			, createFD( OP, "inDateSimulation",		"INDATE_SIM",				"DPR_ORDER_SIMULATIONINDATE",	DATE )
			, createFD( OP, "inDateConfirm",		"INDATE_CNF",				"DPR_ORDER_CONFIRMINDATE",		DATE )
			, createFD( OP, "simulationKey",		"SIMULATIONKEY",			"DPR_ORDER_SIMULATIONKEY",		0, 35 )
			, createFD( OP, "customerOrderNumber",	"CUSTOMER_ORDER_NUMBER",	"DPR_ORDER_CUSTOMERORDERNUMBER",0, 35 )
			, createFD( OP, "orderNumber",			"ORDER_NUMBER",				"DPR_ORDER_ORDERNUMBER",		0, 35 )
			, createFD( OP, "billingNumber2",		"BILLING_NUMBER",			"DPR_ORDER_BILLINGNUMBER",		0, 35 )
			, createFD( OP, "deliveryNumber",		"DELIVERY_NUMBER",			"DPR_ORDER_DELIVERYNUMBER",		0, 35 )
			, createFD( MR, "organizationCode",		"ORGANIZATIONCD",			"DPR_ORDER_ORGANIZATIONCODE",	0, 8 )
			, createFD( MR, "distributionChannelCode","DIST_CHANNELCD",			"DPR_ORDER_DISTRIBUTIONCHANNELCODE",	0, 8 )
			, createFD( MR, "divisionCode",			"DIVISIONCD",				"DPR_ORDER_DIVISIONCODE",		0, 8 )
			, createFD( OP, "payerPartyCode",		"PAYER_PARTYCD",			"DPR_ORDER_PAYERPARTYCODE",		0, 15 )
			, createFD( OP, "billPartyCode",		"BILL_PARTYCD",				"DPR_ORDER_BILLPARTYCODE",		0, 15 )
			, createFD( MR, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_ORDER_SHIPPARTYCODE",		0, 15 )
			, createFD( MR, "soldPartyCode",		"SOLD_PARTYCD",				"DPR_ORDER_SOLDPARTYCODE",		0, 15 )
			, createFD( MR, "orderType",			"ORDER_TYPE",				"DPR_ORDER_ORDERTYPE",			"DPR_ORDER_ORDERTYPE_", "NO,DA,PR" )
			, createFD( OP, "orderVolume",			"ORDER_VOLUME",				"DPR_ORDER_ORDERVOLUME",		DOUBLE )
			, createFD( OP, "orderVolumeUnit",		"ORDER_VOLUME_UNIT",		"DPR_ORDER_ORDERVOLUME_UNIT",	0, 20 )
			, createFD( OP, "orderWeight",			"ORDER_WEIGHT",				"DPR_ORDER_ORDERWEIGHT",		DOUBLE )
			, createFD( OP, "orderWeightUnit",		"ORDER_WEIGHT_UNIT",		"DPR_ORDER_ORDERWEIGHT_UNIT",	0, 20 )
			, createFD( OP, "orderQty",				"ORDERQTY",					"DPR_ORDER_ORDERQTY",			INTEGER, 1, 9999 )
			, createFD( OP, "orderValue",			"ORDERVALUE",				"DPR_ORDER_ORDERVALUE",			DOUBLE )
			, createFD( OP, "orderTax",				"ORDERTAX",					"DPR_ORDER_ORDER_TAX",			DOUBLE )
			, createFD( OP, "orderDiscount",		"ORDERDISCOUNT",			"DPR_ORDER_ORDERDISCOUNT",		DOUBLE )
			, createFD( OP, "orderTotal",			"ORDERTOTAL",				"DPR_ORDER_ORDERTOTAL",			DOUBLE )
			, createFD( OP, "simulationOrderQty",	"SIMULATION_ORDERQTY",		"DPR_ORDER_SIMULATION_ORDERQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "simulationOrderValue",	"SIMULATION_ORDERVALUE",	"DPR_ORDER_SIMULATION_ORDERVALUE",	DOUBLE )
			, createFD( OP, "simulationOrderTax",	"SIMULATION_ORDERTAX",		"DPR_ORDER_SIMULATION_ORDERTAX",	DOUBLE )
			, createFD( OP, "simulationOrderDiscount",	"SIMULATION_ORDERDISCOUNT",		"DPR_ORDERDTL_SIMULATION_ORDERDISCOUNT",	DOUBLE )
			, createFD( OP, "simulationOrderTotal",	"SIMULATION_ORDERTOTAL",	"DPR_ORDER_SIMULATION_ORDERTOTAL",	DOUBLE )
			, createFD( OP, "confirmedOrderQty",	"CONFIRMED_ORDERQTY",		"DPR_ORDER_CONFIRMED_ORDERQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "confirmedOrderValue",	"CONFIRMED_ORDERVALUE",		"DPR_ORDER_CONFIRMED_ORDERVALUE",	DOUBLE )
			, createFD( OP, "confirmedOrderTax",	"CONFIRMED_ORDERTAX",		"DPR_ORDER_CONFIRMED_ORDERTAX",	DOUBLE )
			, createFD( OP, "confirmedOrderDiscount",	"CONFIRMED_ORDERDISCOUNT",		"DPR_ORDERDTL_CONFIRMED_ORDERDISCOUNT", DOUBLE )
			, createFD( OP, "confirmedOrderTotal",	"CONFIRMED_ORDERTOTAL",		"DPR_ORDER_CONFIRMED_ORDERTOTAL",	DOUBLE )
			, createFD( OP, "freegoodsOrderWay",	"FREEGOODS_ORDER_WAY",		"DPR_ORDER_FREEGOODS_ORDER_WAY",	0, 1 )
			, createFD( OP, "freegoodsOrderInd",	"FREEGOODS_ORDER_IND",		"DPR_ORDER_FREEGOODS_ORDER_IND",	0, 1 )
			, createFD( OP, "parentOrderKey",		"PARENT_ORDERKEY",			"DPR_ORDER_PARENT_ORDERKEY",	0, 20 )
			, createFD( OP, "goodsIssueDate",		"GOODS_ISSUE_DATE",				"DPR_ORDER_GOODSISSUE_DATE",		DATE )
			, createFD( OP, "creditReleaseDate",		"CREDIT_RELEASE_DATE",				"DPR_ORDER_CREDITRELEASE_DATE",		DATE )
			, createFD( OP, "creditStatus",			"CREDIT_STATUS",			"DPR_ORDER_CREDIT_STATUS",		0, 2 )
			, createFD( RD, "orderStatus",			"ORDER_STATUS",				"DPR_ORDER_ORDERSTATUS",		"DPR_ORDER_ORDERSTATUS_", "HO,DB,PC,IN,VP,SH,CP,CF,DE" )
			, createFD( OP, "manageUserId",			"MNGUSERID",				"DPR_ORDER_MANAGEUSERID",		0, 30 )
			, createFD( OP, "message",				"MESSAGE",					"DPR_ORDER_MESSAGE",			DESC, 0, 512 )
			, createFD( OP, "dealCode",				"PACKDEALCD",				"DPR_PACKDEAL_CODE",		STRING )
			, createFD( OP, "reviseStatus",			"REV_STATUS",				"DPR_ORDREV_REVSTATUS",			"DPR_ORDER_REVISESTATUS_", "CQ,CP" )
			, createFD( OP, "reviseHbrdSeqId",		"REV_HDSEQID",				"DPR_ORDREV_HELPBOARD_SEQID", INTEGER				)
			, createFD( OP, "reviseChangeIndex",	"REV_CHGIDX",				"DPR_ORDREV_CHANGE_INDEX", INTEGER				)
			, createFD( MD, "status",				"STATUS",					"DPR_ORDER_STATUS",				"DPR_ORDER_STATUS_", "WK,SG,SD,CG,CD,ER,DE" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDER, table = createTable("DPR_ORDER", "ORD", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER, new QueryableImpl(table) {{

			append( makeCountryAuthQueryableFields(TYPE_NONE, "ORD") );
			append( makePartyAuthQueryableFields(TYPE_NONE, "ORD", "SOLD_PARTYCD") );

			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES"
					, "PTYS.PARTYCD(+) = ORD.PARTYCD"
							+ " AND PTYS.ORGANIZATIONCD(+) = ORD.ORGANIZATIONCD"
							+ " AND PTYS.DIST_CHANNELCD(+) = ORD.DIST_CHANNELCD"
							+ " AND PTYS.DIVISIONCD(+) = ORD.DIVISIONCD" );

			append( new QueryableField[] {
					new QueryableFieldImplBK( STRING, "distributionChannelName"
					, "( SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = ORD.DIST_CHANNELCD"
						+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = ?)", "displayLanguage", "DPR_DISTRIBUTIONCHANNEL_NAME" )
				, new QueryableFieldImpl( STRING, "partyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORD.PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)" )
				, new QueryableFieldImpl( STRING, "partyNameFull"
					, "(SELECT ((CASE WHEN LENGTHB(REGEXP_REPLACE(SB.PARTYNAME, '[[:punct:]]|[0-9a-zA-Z\\ ]')) > 0 THEN SB.PARTYNAME ELSE SB.PARTYNAME || ' ' END)"
										  + "|| DECODE(SB.PARTYNAME_ADDITION, '-', NULL, NULL, NULL, SB.PARTYNAME_ADDITION)) FROM DPR_PARTY_SALES SB"
											  +" WHERE SB.PARTYCD = ORD.PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
											  +" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)" )
				, new QueryableFieldImpl( STRING, "userPartyId", "(SELECT SB.PARTY_ID FROM USR_PARTY SB WHERE SB.GLN = ORD.COUNTRYCD)" )
				, new QueryableFieldImpl( STRING, "countryName"
					, "(SELECT SB.COUNTRYNAME FROM DPR_COUNTRY SB WHERE SB.COUNTRY_CD = ORD.COUNTRYCD)" )
				, new QueryableFieldImplBK( STRING, "organizationName"
					, "(SELECT SB.MASTER_NAME FROM DPR_MASTER_DESC SB"
						+" WHERE SB.MASTER_CD = ORD.ORGANIZATIONCD AND SB.MASTER_TYPE = 'SO' AND LANGCD = ?)", "displayLanguage" )
				, new QueryableFieldImpl( STRING, "payerPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORD.PAYER_PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)" )
				, new QueryableFieldImpl( STRING, "billPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORD.BILL_PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)" )
				, new QueryableFieldImpl( STRING, "soldPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORD.SOLD_PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)" )
				, new QueryableFieldImpl( STRING, "shipPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORD.SHIP_PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)" )
				, new QueryableFieldImpl( INTEGER, "itemCount", "(SELECT COUNT(*) FROM DPR_ORDER_DTL SB WHERE SB.ORDERKEY = ORD.ORDER_KEY)" )
				, new QueryableFieldImpl( INTEGER, "pipoItemCount", "(SELECT COUNT(*) FROM DPR_ORDER_DTL SB WHERE SB.ORDERKEY = ORD.ORDER_KEY AND SB.CHILD_LINE_NO != 0)" )
				, new QueryableFieldImpl( INTEGER, "pipoItemExist", "(SELECT DECODE(COUNT(*), 0, 'N', 'Y') FROM DPR_ORDER_DTL SB WHERE SB.ORDERKEY = ORD.ORDER_KEY AND SB.CHILD_LINE_NO != 0)" )
				, new QueryableFieldImpl( STRING, "title", "DECODE(ORD.FREEGOODS_ORDER_IND, 'Y', 'FREEGOODSORD', 'ORD')" )
				, new QueryableFieldImpl( STRING, "groupCode", "PTYS.GROUPCD", "DPR_SALESGROUP_CODE", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "officeCode", "PTYS.OFFICECD", "DPR_SALESOFFICE_CODE", tbl_PTYS )
				, new QueryableFieldImplBK( DESC, "officeName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS.OFFICECD"
							+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_OFFICE_NAME", tbl_PTYS )
				, new QueryableFieldImplBK( DESC, "groupName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PTYS.GROUPCD"
								+ " AND SB.MASTER_TYPE = 'SG' AND SB.LANGCD = ?)", "displayLanguage", "DPR_SALES_GROUP_NAME", tbl_PTYS )
				, new QueryableFieldImpl( STRING, "parentOrderNumber", "NVL2(ORD.PARENT_ORDERKEY, (SELECT SB.ORDER_NUMBER FROM DPR_ORDER SB WHERE SB.ORDER_KEY = ORD.PARENT_ORDERKEY), NULL)", "DPR_ORDER_PARENT_ORDERNUMBER" )
				, new QueryableFieldImpl( STRING, "childOrderNumber", "DECODE(ORD.FREEGOODS_ORDER_IND, 'Y', NULL, (SELECT SB.ORDER_NUMBER FROM DPR_ORDER SB WHERE SB.PARENT_ORDERKEY = ORD.ORDER_KEY AND SB.FREEGOODS_ORDER_WAY IS NOT NULL AND SB.FREEGOODS_ORDER_IND = 'Y'))", "DPR_ORDER_CHILD_ORDERNUMBER" )
				, new QueryableFieldImpl( STRING, "childOrderKey", "DECODE(ORD.FREEGOODS_ORDER_IND, 'Y', NULL, (SELECT SB.ORDER_KEY FROM DPR_ORDER SB WHERE SB.PARENT_ORDERKEY = ORD.ORDER_KEY AND SB.FREEGOODS_ORDER_WAY IS NOT NULL AND SB.FREEGOODS_ORDER_IND = 'Y'))", "DPR_ORDER_CHILD_ORDERKEY" )
				, new QueryableFieldImpl( STRING, "isPackdealOrder", "DECODE(ORD.PACKDEALCD, NULL, 'N', 'Y')", "DPR_PACKDEAL_DEALIND", "PUB_WHETHER_" )
				, new QueryableFieldImpl( STRING, "deliveryNumber2", "LTRIM(ORD.DELIVERY_NUMBER, '0')", "DPR_ORDER_DELIVERYNUMBER" )
			} );

			// JDMS
			Joinable tbl_PJDMS = new JoinableImpl( "PJDMS", "DPR_PARTY_JDMS"
					, "PJDMS.PARTYCD(+) = ORD.SOLD_PARTYCD AND PJDMS.ORGANIZATIONCD(+) = ORD.ORGANIZATIONCD"
					+ " AND PJDMS.DIST_CHANNELCD(+) =  ORD.DIST_CHANNELCD AND PJDMS.DIVISIONCD(+) = ORD.DIVISIONCD" );
			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "JDMSIndicator", "NVL2(PJDMS.PARTYCD, 'Y', 'N')", tbl_PJDMS )
			} );

			Joinable tbl_CCND = new JoinableImpl( "CCND", "DPR_COUNTRY_COND"
					, "CCND.COUNTRYCD(+) = ORD.COUNTRYCD AND CCND.ORGANIZATIONCD(+) = ORD.ORGANIZATIONCD" );
			append( new QueryableField[] {
					  new QueryableFieldImpl( STRING, "organizationNameDefault", "CCND.ORG_NAME_DEFAULT", tbl_CCND )
					, new QueryableFieldImpl( STRING, "organizationNameLocal", "CCND.ORG_NAME_LOCAL", tbl_CCND )
					, new QueryableFieldImpl( STRING, "organizationAddress", "CCND.ORG_ADDRESS", tbl_CCND )
					, new QueryableFieldImpl( STRING, "organizationAddressDefault", "CCND.ORG_ADDRESS_DEFAULT", tbl_CCND )
					, new QueryableFieldImpl( STRING, "organizationPhone", "CCND.ORG_PHONE", tbl_CCND )
					, new QueryableFieldImpl( STRING, "organizationFax", "CCND.ORG_FAX", tbl_CCND )
					, new QueryableFieldImpl( STRING, "organizationPostCode", "CCND.ORG_POST_CODE", tbl_CCND )
					, new QueryableFieldImpl( STRING, "countryLanguageCode", "(SELECT SB.LANGCD FROM DPR_COUNTRY SB WHERE SB.COUNTRY_CD = CCND.COUNTRYCD)", tbl_CCND )
				} );

			/* DPR_ORDER_INFO */
			JoinableImpl tbl_ORDI = new JoinableImpl( "ORDI", "DPR_ORDER_INFO", "ORDI.ORDER_NUMBER(+) = ORD.ORDER_NUMBER" );
			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "purchaseOrderNumber", "ORDI.PO_NUMBER", tbl_ORDI )
				, new QueryableFieldImpl( DATE, "deliveryDate", "ORDI.DELIVERY_DATE", tbl_ORDI )
				, new QueryableFieldImpl( STRING, "vatNumber", "ORDI.VAT_NO", tbl_ORDI )
				, new QueryableFieldImpl( DOUBLE, "invoiceValue", "ORDI.INVOICE_VALUE", tbl_ORDI )
				, new QueryableFieldImpl( DOUBLE, "infoOrderVolume", "ORDI.ORDER_VOLUME", tbl_ORDI )
				, new QueryableFieldImpl( STRING, "infoOrderVolumeUnit", "ORDI.ORDER_VOLUME_UNIT", tbl_ORDI )
				, new QueryableFieldImpl( DOUBLE, "infoOrderWeight", "ORDI.ORDER_WEIGHT", tbl_ORDI )
				, new QueryableFieldImpl( STRING, "infoOrderWeightUnit", "ORDI.ORDER_WEIGHT_UNIT", tbl_ORDI )
				, new QueryableFieldImpl( DOUBLE, "nvlConfirmedNetAmount", "NVL(ORDI.CONFIRMED_ORDERVALUE, ORD.ORDERVALUE)", tbl_ORDI )
			} );

			QueryBufferValid valid = new QueryBufferValid.Condition( "billingNumber" );
			Joinable tbl_OBIL = new JoinableImpl( "OBIL", "DPR_ORDER_BILLING"
					, "OBIL.ORDER_NUMBER(+) = ORD.ORDER_NUMBER" );
			appendCND( valid, new QueryableField[] {
				  new QueryableFieldImpl( STRING, "billingNumber", "OBIL.BILLING_NUMBER", tbl_OBIL )
				, new QueryableFieldImpl( STRING, "billingDate", "OBIL.BILLING_DATE", tbl_OBIL )
			} );

			appendCND( new QueryBufferValid.Condition("uniqId"), new QueryableFieldImplBK( STRING, "lastRegistUserEmail"
					, "(SELECT SB.EMAIL FROM (SELECT SB.EMAIL FROM ICS_HELP_BOARD SB WHERE SB.REGUSERID = ? ORDER BY SB.REGDATE DESC) SB WHERE ROWNUM = 1)"
						, new String[] {"uniqId"} ));

			Joinable tbl_HBRD = new JoinableImpl("HBRD", "ICS_HELP_BOARD", "HBRD.BOARDCLASSCD(+) = 'HD.' || ORD.ORGANIZATIONCD"
															+ " AND HBRD.SEQID(+) = ORD.REV_HDSEQID");
			appendCND( new QueryBufferValid.ConditionTrue("useRevOrd"), new QueryableField[] {
					new QueryableFieldImpl( INTEGER, "revHbrdNumberLast", "DECODE(HBRD.COMPLETED_IND,'Y',NULL,HBRD.SEQID)", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "revHbrdAttachMngKeyLast", "HBRD.ATTACH_MNGKEY", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "reviseHelpType", "(SELECT HEADWORD_DESC FROM ICS_BOARD_HEADWORD SB"
											  +" WHERE SB.BOARDCLASSCD = HBRD.BOARDCLASSCD AND SB.HEADWORD_CD = HBRD.HEADWORDCD)"
											  , "ICS_HELP_BOARD_HEADWORD", "FIELD_ICS_HELP_BOARD_HEADWORD_"
											  , tbl_HBRD )
					, new QueryableFieldImpl( DATE, "revisingBaseDate", "HBRD.REGDATE", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "reviseFileName", "(SELECT FILE_NAME FROM ICS_BOARD_ATTACH SB"
											  +" WHERE SB.ATTACH_MNGKEY = HBRD.ATTACH_MNGKEY AND SB.ATTACH_SEQID = 0)", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "reviseServerFileName", "(SELECT SERVER_FILENAME FROM ICS_BOARD_ATTACH SB"
											  +" WHERE SB.ATTACH_MNGKEY = HBRD.ATTACH_MNGKEY AND SB.ATTACH_SEQID = 0)", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "reviseServerAttachPath", "(SELECT FILE_PATH FROM ICS_BOARD_ATTACH SB"
											  +" WHERE SB.ATTACH_MNGKEY = HBRD.ATTACH_MNGKEY AND SB.ATTACH_SEQID = 0)", tbl_HBRD )
					, new QueryableFieldImpl( DATE, "revHbrdLastDate", "HBRD.REGDATE", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "revHbrdContent", "HBRD.CONTENT", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "revHbrdEmail", "HBRD.EMAIL", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "revHbrdClassCode", "HBRD.BOARDCLASSCD", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "parentOrderNumber", "(SELECT SB.ORDER_NUMBER FROM DPR_ORDER SB WHERE SB.ORDER_KEY = ORD.PARENT_ORDERKEY)" )
					, new QueryableFieldImpl( STRING, "parentReviseStatus", "(SELECT SB.REV_STATUS FROM DPR_ORDER SB WHERE SB.ORDER_KEY = ORD.PARENT_ORDERKEY)" )
					, new QueryableFieldImpl( Schema.INTEGER, "parentReviseModCount", "(SELECT COUNT(*) FROM ICS_HELP_BOARD HBRD WHERE HBRD.ORDER_NUMBER = ORD.ORDER_NUMBER"
																		+ " AND HBRD.HEADWORDCD IN (SELECT HEADWORD_CD FROM ICS_BOARD_HEADWORD"
											  + " WHERE BOARDCLASSCD = 'HD.'||ORD.ORGANIZATIONCD AND HEADWORD_DESC IN ('ROD','ROM')))", tbl_HBRD )
			} );
		}

			@Override
			public boolean appendCondition( ConditionQueryBuffer querybuf ) {
				return makeCompositeOrderNumberCondition(querybuf);
			}

		} );


		/***************************************************************************************************
			*	DPR_ORDER_DTL
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "orderKey",				"ORDERKEY",					"DPR_ORDERDTL_ORDERKEY",		0, 20 )
			, createFD( PM, "lineNumber",			"LINE_NO",					"DPR_ORDERDTL_LINENUMBER",		0, 10 )
			, createFD( MD, "itemCode",				"ITEMCD",					"DPR_ORDERDTL_ITEMCODE",		0, 20 )
			, createFD( MD, "itemCodeConfirmed",	"ITEMCD_CNF",				"DPR_ORDERDTL_ITEMCODE_CONFIRMED",	0, 20 )
			, createFD( MD, "childLineNumber",		"CHILD_LINE_NO",			"DPR_ORDERDTL_CHILD_LINENUMBER",	0, 10 )
			, createFD( MD, "itemRefInd",			"ITEMREF_IND",				"DPR_ORDERDTL_ITEMREF_IND",		"DPR_ORDER_DTL_ITEMREF_IND_", "NO,OG,PO,PI,PP,RP,DC" )
			, createFD( OP, "orderNumber",			"ORDER_NUMBER",				"DPR_ORDER_ORDERNUMBER",		0, 35 )
			, createFD( OP, "orderVolume",			"ORDER_VOLUME",				"DPR_ORDERDTL_ORDERVOLUME",		DOUBLE )
			, createFD( OP, "orderVolumeUnit",		"ORDER_VOLUME_UNIT",		"DPR_ORDERDTL_ORDERVOLUME_UNIT",	0, 20 )
			, createFD( OP, "orderWeight",			"ORDER_WEIGHT",				"DPR_ORDERDTL_ORDERWEIGHT",		DOUBLE )
			, createFD( OP, "orderWeightUnit",		"ORDER_WEIGHT_UNIT",		"DPR_ORDERDTL_ORDERWEIGHT_UNIT",	0, 20 )
			, createFD( OP, "orderQty",				"ORDERQTY",					"DPR_ORDERDTL_ORDERQTY",		INTEGER, 0, 9999 )
			, createFD( OP, "orderValue",			"ORDERVALUE",				"DPR_ORDERDTL_ORDERVALUE",		DOUBLE )
			, createFD( OP, "orderTax",				"ORDERTAX",					"DPR_ORDERDTL_ORDERTAX",		DOUBLE )
			, createFD( OP, "orderDiscount",		"ORDERDISCOUNT",			"DPR_ORDERDTL_ORDERDISCOUNT",	DOUBLE )
			, createFD( OP, "orderTotal",			"ORDERTOTAL",				"DPR_ORDERDTL_ORDERTOTAL",		DOUBLE )
			, createFD( OP, "simulationUOM",		"SIMULATION_UOM",			"DPR_ORDERDTL_UOM",				0, 3 )
			, createFD( OP, "simulationOrderQty",	"SIMULATION_ORDERQTY",		"DPR_ORDERDTL_SIMULATION_ORDERQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "simulationOrderValue",	"SIMULATION_ORDERVALUE",	"DPR_ORDERDTL_SIMULATION_ORDERVALUE",	DOUBLE )
			, createFD( OP, "simulationOrderTax",	"SIMULATION_ORDERTAX",		"DPR_ORDERDTL_SIMULATION_ORDERTAX",	DOUBLE )
			, createFD( OP, "simulationOrderDiscount",	"SIMULATION_ORDERDISCOUNT",		"DPR_ORDERDTL_SIMULATION_ORDERDISCOUNT",	DOUBLE )
			, createFD( OP, "simulationOrderTotal",	"SIMULATION_ORDERTOTAL",	"DPR_ORDERDTL_SIMULATION_ORDERTOTAL",	DOUBLE )
			, createFD( OP, "freegoodsQty",			"FREEGOODS_QTY",			"DPR_ORDERDTL_FREEGOODS_QTY",	INTEGER )
			, createFD( OP, "freegoodsRatio",		"FREEGOODS_RATIO",			"DPR_ORDERDTL_FREEGOODS_RATIO",	INTEGER )
			, createFD( OP, "freegoodsInd",			"FREEGOODS_IND",			"DPR_ORDERDTL_FREEGOODS_IND", 0, 1 )
			, createFD( OP, "inputTotalQty",		"INPUT_TOTAL_QTY",			"DPR_ORDERDTL_INPUT_TQTY",		INTEGER )
			, createFD( OP, "inputTotalValue",		"INPUT_TOTAL_VALUE",		"DPR_ORDERDTL_INPUT_TVALUE",	DOUBLE )
			, createFD( OP, "simulationTotalQty",	"SIMULATION_TOTAL_QTY",		"DPR_ORDERDTL_SIMULATION_TOTALQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "simulationTotalValue",	"SIMULATION_TOTAL_VALUE",	"DPR_ORDERDTL_SIMULATION_TOTALVALUE",	DOUBLE )
			, createFD( OP, "simulationTotalTax",	"SIMULATION_TOTAL_TAX",		"DPR_ORDERDTL_SIMULATION_TOTALTAX",	DOUBLE )
			, createFD( OP, "simulationTotalDiscount",	"SIMULATION_TOTAL_DISCOUNT",	"DPR_ORDERDTL_SIMULATION_TOTALDISCOUNT",	DOUBLE )
			, createFD( OP, "simulationTotalAmount",	"SIMULATION_TOTAL_AMT",	"DPR_ORDERDTL_SIMULATION_TOTALAMT",	DOUBLE )
			, createFD( OP, "confirmedOrderQty",	"CONFIRMED_ORDERQTY",		"DPR_ORDERDTL_CONFIRMED_ORDERQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "confirmedOrderValue",	"CONFIRMED_ORDERVALUE",		"DPR_ORDERDTL_CONFIRMED_ORDERVALUE",	DOUBLE )
			, createFD( OP, "confirmedOrderTax",	"CONFIRMED_ORDERTAX",		"DPR_ORDERDTL_CONFIRMED_ORDERTAX",	DOUBLE )
			, createFD( OP, "confirmedOrderDiscount",	"CONFIRMED_ORDERDISCOUNT",		"DPR_ORDERDTL_CONFIRMED_ORDERDISCOUNT", DOUBLE )
			, createFD( OP, "confirmedOrderTotal",	"CONFIRMED_ORDERTOTAL",		"DPR_ORDERDTL_CONFIRMED_ORDERTOTAL",	DOUBLE )
			, createFD( OP, "consumerEANCode",		"CONS_EAN",					"DPR_ORDERDTL_UPC_CODE",			0, 20 )
			, createFD( OP, "uom",					"UOM",						"DPR_ORDERDTL_UOM",				0, 3 )
			, createFD( OP, "packSize",				"PACKSIZE",					"DPR_ORDERDTL_SIZE",			INTEGER )
			, createFD( OP, "priceCurrency",		"PRICE_CURR",				"DPR_ORDERDTL_PRICECURRENCY",	3, 3 )
			, createFD( OP, "price",				"PRICE",					"DPR_ORDERDTL_PRICE",			DOUBLE )
			, createFD( OR, "payerPartyCode",		"PAYER_PARTYCD",			"DPR_ORDERDTL_PAYERPARTYCODE",	0, 15 )
			, createFD( OR, "billPartyCode",		"BILL_PARTYCD",				"DPR_ORDERDTL_BILLPARTYCODE",	0, 15 )
			, createFD( OR, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_ORDERDTL_SHIPPARTYCODE",	0, 15 )
			, createFD( OR, "soldPartyCode",		"SOLD_PARTYCD",				"DPR_ORDERDTL_SOLDPARTYCODE",	0, 15 )
			, createFD( OP, "packdealSimPriceFirst",	"PDSIM_PRICE_FIRST",	"DPR_ORDERDTL_PRICE",			DOUBLE )
			, createFD( OP, "packdealSimUomFirst",	"PDSIM_UOM_FIRST",			"DPR_ORDERDTL_UOM",				0, 3 )
			, createFD( OP, "dealCode",				"PACKDEALCD",				"DPR_PACKDEAL_CODE",		STRING )
			, createFD( OP, "reviseBeforeCnfUom",		"REVBF_CNFUOM",			"DPR_ORDERDTL_REVBF_CNFUOM",				0, 3 )
			, createFD( OP, "reviseBeforeCnfQty",	"REVBF_CNFQTY",		"DPR_ORDERDTL_REVBF_CNFQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "reviseAfterCnfUom",		"REVAF_CNFUOM",			"DPR_ORDERDTL_REVAF_CNFUOM",				0, 3 )
			, createFD( OP, "reviseAfterCnfQty",	"REVAF_CNFQTY",		"DPR_ORDERDTL_REVAF_CNFQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "reviseSimInputQty",	"REVSIM_IPTQTY",		"DPR_ORDERDTL_REVSIM_IPTQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "reviseSimFinalQty",	"REVSIM_FINQTY",		"DPR_ORDERDTL_REVSIM_FINQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "status",				"STATUS",					"STATUS",						"DPR_ORDER_DTL_STATUS_", "00,SP,DE,SE" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDER_DTL, table = createTable("DPR_ORDER_DTL", "ODTL", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_DTL, new QueryableImpl(table) {{
			JoinableImpl tbl_UOM = new JoinableImpl( "IMU", "DPR_ITEM_MASTER_UOM"
					, "IMU.ITEMCD(+) = ODTL.ITEMCD_CNF AND IMU.UOM_CD(+) = ODTL.UOM" );

			JoinableImpl tbl_ORD = new JoinableImpl( "ORD", "DPR_ORDER", "ORD.ORDER_KEY = ODTL.ORDERKEY" );

			JoinableImpl tbl_UOMIMT = new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMT.ITEM_CD(+) = ODTL.ITEMCD" );
			JoinableImpl tbl_UOMBS = new JoinableImpl( "IMUBS", "DPR_ITEM_MASTER_UOM"
					, "IMUBS.ITEMCD(+) = IMT.ITEM_CD AND IMUBS.UOM_CD(+) = IMT.BASE_UOM", tbl_UOMIMT );

			JoinableImpl tbl_UOMSM = new JoinableImpl( "IMU_SM", "DPR_ITEM_MASTER_UOM"
					, "IMU_SM.ITEMCD(+) = ODTL.ITEMCD_CNF AND IMU_SM.UOM_CD(+) = ODTL.SIMULATION_UOM" );

			JoinableImpl tbl_OIND = new JoinableImpl( "OIND", "DPR_ORDER_INFO_DTL"
					, "OIND.ORDER_NUMBER(+) = ODTL.ORDER_NUMBER AND OIND.LINE_NO(+) = ODTL.LINE_NO AND OIND.ITEMCD(+) = ODTL.ITEMCD"
							+ " AND OIND.ITEMCD_CNF(+) = ODTL.ITEMCD_CNF AND OIND.ITEMREF_IND(+) = ODTL.ITEMREF_IND" );

			JoinableImpl tbl_UOMINFO = new JoinableImpl( "IMU_INFO", "DPR_ITEM_MASTER_UOM"
					, "IMU_INFO.ITEMCD(+) = OIND.ITEMCD_CNF AND IMU_INFO.UOM_CD(+) = OIND.UOM", tbl_OIND );

			Joinable tbl_PLNK = new JoinableImpl( "PLNK", "DPR_PARTY_LINK"
					, "PLNK.LINKTYPE IN ( 'WE', 'SH' )"
							+ " AND PTYS.PARTYCD = PLNK.PARTYCD"
							+ " AND PTYS.ORGANIZATIONCD = PLNK.ORGANIZATIONCD"
							+ " AND PTYS.DIST_CHANNELCD = PLNK.DIST_CHANNELCD"
							+ " AND PTYS.DIVISIONCD = PLNK.DIVISIONCD" );
			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES"
					, "PTYS.PARTYCD = OITM.PARTYCD"
							+ " AND PTYS.ORGANIZATIONCD = OITM.ORGANIZATIONCD"
							+ " AND PTYS.DIST_CHANNELCD = OITM.DIST_CHANNELCD"
							+ " AND PTYS.DIVISIONCD = OITM.DIVISIONCD"
					, tbl_PLNK );
			Joinable tbl_PTYS_LNK = new JoinableImpl( "PTYS_LNK", "DPR_PARTY_SALES"
					, "PTYS_LNK.PARTYCD = PLNK.LINK_PARTYCD"
							+ " AND PTYS_LNK.ORGANIZATIONCD = PTYS.ORGANIZATIONCD"
							+ " AND PTYS_LNK.DIST_CHANNELCD = PTYS.DIST_CHANNELCD"
							+ " AND PTYS_LNK.DIVISIONCD = PTYS.DIVISIONCD"
							+ " AND PTYS_LNK.STATUS = '00'"
							+ " AND PTYS_LNK.PARTYCD = ODTL.SHIP_PARTYCD"
					, tbl_PTYS );

			Joinable tbl_PNITM = new JoinableImpl( "PNITM", "vwDPR_PLANT_EXCL_ITEM"
					, "OITM.ITEMCD = PNITM.ITEMCD(+)"
							+ " AND OITM.ORGANIZATIONCD = PNITM.ORGANIZATIONCD(+)"
							+ " AND OITM.DIST_CHANNELCD = PNITM.DIST_CHANNELCD(+)"
							+ " AND OITM.DIVISIONCD = PNITM.DIVISIONCD(+)"
							+ " AND OITM.COUNTRYCD = PNITM.COUNTRYCD(+)"
							+ " AND OITM.PARTYCD = PNITM.PARTYCD(+)"
					, tbl_PTYS_LNK);
			JoinableImpl tbl_ITM = new JoinableImpl( "ITM", "DPR_ITEM"
					, "ITM.ITEMCD = OITM.ITEMCD AND ITM.ORGANIZATIONCD = OITM.ORGANIZATIONCD"
							+ " AND ITM.DIST_CHANNELCD = OITM.DIST_CHANNELCD AND ITM.COUNTRYCD = OITM.COUNTRYCD" );
			JoinableImpl tbl_IMTSS = new JoinableImpl( "IMTSS", "DPR_ITEM_MASTER_STATUS"
					, "IMTSS.STATUS_CD(+) = ITM.SALES_STATUS", tbl_ITM );
			JoinableImpl tbl_IMTSC = new JoinableImpl( "IMTSC", "DPR_ITEM_MASTER_STATUS"
					, "IMTSC.STATUS_CD(+) = ITM.CHAIN_STATUS", tbl_IMTSS );

			String itemDisplayInd =
					"(CASE WHEN IMTSS.STATUS_CD IS NULL AND LTRIM(ITM.SALES_STATUS_FROM,'0') IS NULL THEN"
							+ " CASE WHEN IMTSC.STATUS_CD IS NULL AND LTRIM(ITM.CHAIN_STATUS_FROM,'0') IS NULL THEN 'Y'"
							+ " WHEN IMTSC.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.CHAIN_STATUS_FROM,'00000000',NULL,ITM.CHAIN_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
							+ " ELSE 'Y' END"
					+ " ELSE (CASE WHEN IMTSS.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.SALES_STATUS_FROM,'00000000',NULL,ITM.SALES_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
							+ " WHEN IMTSC.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.CHAIN_STATUS_FROM,'00000000',NULL,ITM.CHAIN_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
							+ " ELSE 'Y' END)"
					+ " END)";

			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "itemName"
						, "(SELECT SB.ITEMNAME FROM DPR_ITEM_MASTER_DESC SB WHERE SB.ITEMCD = ODTL.ITEMCD AND SB.LANGCD = ?)", "displayLanguage" )
				, new QueryableFieldImpl( STRING, "headerStatus", "ORD.STATUS"
						, new JoinableImpl("ORD", "DPR_ORDER", "ODTL.ORDERKEY = ORD.ORDER_KEY") )
				, new QueryableFieldImpl( STRING, "odtlStatus", "ODTL.STATUS" )
				, new QueryableFieldImpl( STRING, "infoUOM", "OIND.UOM", tbl_OIND )
				, new QueryableFieldImpl( STRING, "infoDeliveryStatus"
						, "DECODE( ODTL.DETAIL_STATUS, 'DE', 'CP', DECODE(OIND.DLVRY_STATUS, NULL, 'CP', OIND.DLVRY_STATUS) )", tbl_OIND )
				, new QueryableFieldImpl( STRING, "infoDeliveryOpenQty", "OIND.DLVRY_OPENQTY", tbl_OIND )
				, new QueryableFieldImpl( STRING, "infoDeliveryIntraQty", "OIND.DLVRY_INTRAQTY", tbl_OIND )
				, new QueryableFieldImpl( STRING, "infoDeliveryCompQty", "OIND.DLVRY_COMPQTY", tbl_OIND )
				, new QueryableFieldImplBK( STRING, "formatQty"
						, "ODTL.ORDERQTY * IMU.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = ODTL.ITEMCD AND SB.UOM_CD(+) = NVL(?, ODTL.UOM))", "formatUOM", tbl_UOM )
				, new QueryableFieldImpl( STRING, "formatCSEQty"
						, "CEIL( ODTL.ORDERQTY * IMU.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = ODTL.ITEMCD AND SB.UOM_CD(+) = 'CSE') )", tbl_UOM )
				, new QueryableFieldImpl( STRING, "formatPCQty"
						, "ODTL.ORDERQTY * IMU.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = ODTL.ITEMCD AND SB.UOM_CD(+) = 'PC')", tbl_UOM )
				, new QueryableFieldImpl( STRING, "formatDozenQty"
						, "CEIL( ODTL.ORDERQTY * IMU.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = ODTL.ITEMCD AND SB.UOM_CD(+) = 'DZ') )", tbl_UOM )
				, new QueryableFieldImplBK( STRING, "formatSimulationQty"
						, "ODTL.SIMULATION_ORDERQTY * IMU_SM.PACKSIZE"
								+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = ODTL.ITEMCD_CNF AND SB.UOM_CD(+) = NVL(?, ODTL.SIMULATION_UOM) )"
						, "formatUOM", tbl_UOMSM )
				, new QueryableFieldImpl( STRING, "formatSimulationPCQty"
						, "ODTL.SIMULATION_ORDERQTY * IMU_SM.PACKSIZE"
								+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = ODTL.ITEMCD_CNF AND SB.UOM_CD(+) = 'PC')", tbl_UOMSM )
				, new QueryableFieldImpl( STRING, "formatSimulationDozenQty"
						, "ODTL.SIMULATION_ORDERQTY * IMU_SM.PACKSIZE"
								+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = ODTL.ITEMCD_CNF AND SB.UOM_CD(+) = 'DZ')", tbl_UOMSM )
				, new QueryableFieldImplBK( STRING, "formatConfirmedQty"
						, "NVL(OIND.CONFIRMED_ORDERQTY, ODTL.CONFIRMED_ORDERQTY) * IMU_INFO.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD_CNF AND SB.UOM_CD(+) = NVL(?, NVL(OIND.UOM, DECODE(ODTL.CHILD_LINE_NO, 0, ODTL.UOM, ODTL.SIMULATION_UOM))) )"
						, "formatUOM", tbl_UOMINFO )
				, new QueryableFieldImpl( STRING, "formatConfirmedPCQty"
						, "NVL(OIND.CONFIRMED_ORDERQTY, ODTL.CONFIRMED_ORDERQTY) * IMU_INFO.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD_CNF AND SB.UOM_CD(+) = 'PC')", tbl_UOMINFO )
				, new QueryableFieldImpl( STRING, "formatConfirmedDozenQty"
						, "NVL(OIND.CONFIRMED_ORDERQTY, ODTL.CONFIRMED_ORDERQTY) * IMU_INFO.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD_CNF AND SB.UOM_CD(+) = 'DZ')", tbl_UOMINFO )
				, new QueryableFieldImpl( CODE, "itemEANCode", "IMUBS.EANCODE", "DPR_ITEMUOM_EANCODE", tbl_UOMBS )
				, new QueryableFieldImpl( STRING, "plantInd", "DECODE( PTYS_LNK.DELIVERY_PLANT, PNITM.PLANTCD, 'Y', 'N' )", tbl_PNITM )
				, new QueryableFieldImplBK( CODE, "itemDisplayInd", itemDisplayInd , new String[]{"availableDate","availableDate","availableDate"}, tbl_IMTSC )
				, new QueryableFieldImpl( CODE, "isPackdealOrder", "DECODE(ORD.PACKDEALCD, NULL, 'N', 'Y')", "DPR_PACKDEAL_DEALIND", "PUB_WHETHER_", tbl_ORD )
				, new QueryableFieldImpl( CODE, "dealCode", "ORD.PACKDEALCD", "DPR_PACKDEAL_CODE", tbl_ORD )
				, new QueryableFieldImpl( CODE, "brandCode", "ITM.BRANDCD", tbl_ITM )
				, new QueryableFieldImplBK( STRING, "brandName", "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB"
						+ " WHERE SB.MASTER_TYPE = 'BR' AND SB.MASTER_CD = ITM.BRANDCD AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", tbl_ITM )
				, new QueryableFieldImpl( Schema.STRING, "reviseSimFinalShortInd", "(CASE WHEN ODTL.REVSIM_FINQTY < ODTL.REVSIM_IPTQTY THEN 'Y' ELSE 'N' END)" )
			} );

			Joinable tbl_PDITM = new JoinableImplBK( "PDITM", "DPR_PACKDEAL_ITEM", "PDITM.DEALCD = ? AND PDITM.ITEMCD = ODTL.ITEMCD", "dealCode" );
			QueryBufferValid valid_PDITM = new QueryBufferValid.Condition("dealCode");
			append( ConditionalQueryableField.makeConditionalQueryableFields(valid_PDITM, new QueryableField[] {
				  new QueryableFieldImpl( STRING, "packdealDiscountRate", "PDITM.DISCOUNT_RATE", tbl_PDITM )
				, new QueryableFieldImpl( STRING, "packdealDisplaySeq", "PDITM.DISPLAY_SEQ", tbl_PDITM )
			}) );

			Joinable tbl_MQORD = new JoinableImpl("MQORD", "vwDPR_MOQORDRMN_NOW", "MQORD.ITEMCD(+) = ODTL.ITEMCD AND MQORD.ORDERKEY(+) = ODTL.ORDERKEY" );
			append( new QueryableField[]{
					  new QueryableFieldImpl( Schema.INTEGER, "pdRmnDay", "MQORD.PDRMN_CAL_DAY", tbl_MQORD )
					, new QueryableFieldImpl( Schema.INTEGER, "pdRmnMonth", "MQORD.PDRMN_CAL_MONTH", tbl_MQORD )
					, new QueryableFieldImpl( Schema.INTEGER, "pdRmnMaxQty", "MQORD.PDRMN_CAL_MAX", tbl_MQORD )
					, new QueryableFieldImpl( Schema.INTEGER, "pdRmnMaxUomQty", "MQORD.PDRMN_CALMAX_UOMQTY", tbl_MQORD )
					, new QueryableFieldImpl( Schema.STRING, "pdRmnExceedInd", "(CASE WHEN ODTL.ORDERQTY > MQORD.PDRMN_CALMAX_UOMQTY THEN 'Y' ELSE 'N' END)", tbl_MQORD )
					, new QueryableFieldImpl( Schema.INTEGER, "rgRmnDay", "MQORD.RGRMN_CAL_DAY", tbl_MQORD )
					, new QueryableFieldImpl( Schema.INTEGER, "rgRmnMonth", "MQORD.RGRMN_CAL_MONTH", tbl_MQORD )
					, new QueryableFieldImpl( Schema.INTEGER, "rgRmnMaxQty", "MQORD.RGRMN_CAL_MAX", tbl_MQORD )
					, new QueryableFieldImpl( Schema.INTEGER, "rgRmnMaxUomQty", "MQORD.RGRMN_CALMAX_UOMQTY", tbl_MQORD )
					, new QueryableFieldImpl( Schema.STRING, "rgRmnExceedInd", "(CASE WHEN ODTL.ORDERQTY > MQORD.RGRMN_CALMAX_UOMQTY THEN 'Y' ELSE 'N' END)", tbl_MQORD )
			});

			JoinableImpl tbl_ORDTL = new JoinableImpl("ORDTL", "DPR_ORDER_DTL", "ORDTL.ORDERKEY(+) = ODTL.ORDERKEY AND ORDTL.LINE_NO(+) = ODTL.LINE_NO");
			Joinable tbl_HBRD = new JoinableImpl("HBRD", "ICS_HELP_BOARD", "HBRD.BOARDCLASSCD(+) = 'HD.' || ORD.ORGANIZATIONCD"
															+ " AND HBRD.SEQID(+) = ORD.REV_HDSEQID", tbl_ORD );
			Joinable tbl_REVP = new JoinableImpl( "REVP", "DPR_ORDER", "REVP.ORDER_KEY(+) = ORD.PARENT_ORDERKEY", tbl_ORD );
			appendCND( new QueryBufferValid.Condition("revOrderNumber"), new QueryableFieldImpl( STRING, "revOrderNumber", "REVP.ORDER_NUMBER", tbl_REVP) );
			appendCND( new QueryBufferValid.Condition("useRevOrd"), new QueryableField[] {
					  new QueryableFieldImpl( STRING, "reviseHelpType", "(SELECT SB.HEADWORD_DESC FROM ICS_BOARD_HEADWORD SB WHERE SB.HEADWORD_CD = HBRD.HEADWORDCD)", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "origOrderKey", "REVP.ORDER_KEY", tbl_REVP )
					, new QueryableFieldImpl( CODE, "revChgedInd", "(CASE WHEN (ORDTL.CONFIRMED_ORDERQTY IS NULL AND ORDTL.REVSIM_FINQTY = 0) THEN 'N'"
																	+ " WHEN (NVL(ORDTL.REVSIM_FINQTY,0) != NVL(ORDTL.CONFIRMED_ORDERQTY,-1)"
																	+ " OR ORDTL.REVAF_CNFUOM != NVL(ORDTL.REVBF_CNFUOM, ORDTL.UOM)) THEN 'Y' ELSE 'N' END)", tbl_ORDTL )
					, new QueryableFieldImpl( STRING, "reviseChangeIndex", "ORD.REV_CHGIDX", tbl_ORD )
					, new QueryableFieldImpl( STRING, "reviseHbrdContent", "HBRD.CONTENT", tbl_HBRD )
					, new QueryableFieldImpl( DATE, "revisingBaseDate", "HBRD.REGDATE", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "reviseHbrdUserName", "HBRD.USERNAME", tbl_HBRD )
					, new QueryableFieldImpl( STRING, "reviseHbrdSeqId", "ORD.REV_HDSEQID", tbl_ORD )
					, new QueryableFieldImpl( STRING, "boardNumber", "ORD.REV_HDSEQID", tbl_ORD )
					, new QueryableFieldImpl( STRING, "parentOrderKey", "REVP.ORDER_KEY", tbl_REVP )
					, new QueryableFieldImpl( STRING, "parentOrderNumber", "REVP.ORDER_NUMBER", tbl_REVP )
					, new QueryableFieldImpl( STRING, "parentReviseStatus", "REVP.REV_STATUS", tbl_REVP )
					, new QueryableFieldImpl( STRING, "reviseStatus", "ORD.REV_STATUS", tbl_ORD )
					, new QueryableFieldImpl( STRING, "parentReviseHbrdSeqId", "REVP.REV_HDSEQID", tbl_REVP )
					, new QueryableFieldImpl( INTEGER, "parentReviseModCount", "(SELECT COUNT(*) FROM ICS_HELP_BOARD HBRD WHERE HBRD.ORDER_NUMBER = REVP.ORDER_NUMBER"
							+ " AND HBRD.HEADWORDCD IN (SELECT HEADWORD_CD FROM ICS_BOARD_HEADWORD"
														+ " WHERE BOARDCLASSCD = 'HD.'||ORD.ORGANIZATIONCD AND HEADWORD_DESC IN ('ROD','ROM')))", tbl_REVP )
			});

		}} );


		/***************************************************************************************************
			*	DPR_ORDER_INFO
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "orderNumber",			"ORDER_NUMBER",				"DPR_ORDER_ORDERNUMBER",		0, 35 )
			, createFD( OP, "orderDate",			"ORDDATE",					"DPR_ORDER_ORDERDATE",			DATE )
			, createFD( OP, "inDateConfirm",		"INDATE",					"DPR_ORDER_CONFIRMINDATE",		DATE )
			, createFD( OP, "purchaseOrderNumber",	"PO_NUMBER",				"DPR_ORDER_PURCHASORDERNUMBER",	0, 35 )
			, createFD( OP, "billingNumber",		"BILLING_NUMBER",			"DPR_ORDER_BILLINGNUMBER",		0, 35 )
			, createFD( OP, "billingDate",			"BILLING_DATE",				"DPR_ORDER_BILLINGNUMBER",		DATE )
			, createFD( OP, "deliveryNumber",		"DELIVERY_NUMBER",			"DPR_ORDER_DELIVERYNUMBER",		0, 35 )
			, createFD( OP, "deliveryDate",			"DELIVERY_DATE",			"DPR_ORDER_INFO_DELIVERYDATE",	DATE )
			, createFD( OP, "organizationCode",		"ORGANIZATIONCD",			"DPR_ORDER_ORGANIZATIONCODE",	0, 8 )
			, createFD( OP, "distributionChannelCode","DIST_CHANNELCD",			"DPR_ORDER_DISTRIBUTIONCHANNELCODE",	0, 8 )
			, createFD( OP, "divisionCode",			"DIVISIONCD",				"DPR_ORDER_DIVISIONCODE",		0, 8 )
			, createFD( OP, "payerPartyCode",		"PAYER_PARTYCD",			"DPR_ORDER_PAYERPARTYCODE",		0, 15 )
			, createFD( OP, "billPartyCode",		"BILL_PARTYCD",				"DPR_ORDER_BILLPARTYCODE",		0, 15 )
			, createFD( OP, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_ORDER_SHIPPARTYCODE",		0, 15 )
			, createFD( OP, "soldPartyCode",		"SOLD_PARTYCD",				"DPR_ORDER_SOLDPARTYCODE",		0, 15 )
			, createFD( OP, "orderType",			"ORDER_TYPE",				"DPR_ORDER_ORDERTYPE",			0, 10 )
			, createFD( OP, "orderVolume",			"ORDER_VOLUME",				"DPR_ORDER_INFO_VOLUME",		DOUBLE )
			, createFD( OP, "orderVolumeUnit",		"ORDER_VOLUME_UNIT",		"DPR_ORDER_INFO_VOLUME_UNIT",	0, 20 )
			, createFD( OP, "orderWeight",			"ORDER_WEIGHT",				"DPR_ORDER_INFO_WEIGHT",		DOUBLE )
			, createFD( OP, "orderWeightUnit",		"ORDER_WEIGHT_UNIT",		"DPR_ORDER_INFO_WEIGHT_UNIT",	0, 20 )
			, createFD( OP, "vatNumber",			"VAT_NO",					"DPR_ORDER_INFO_VAT_NO",		DOUBLE )
			, createFD( OP, "invoiceValue",			"INVOICE_VALUE",			"DPR_ORDER_INFO_INVOICE_VALUE",	DOUBLE )
			, createFD( OP, "orderQty",				"ORDERQTY",					"DPR_ORDER_INFO_ORDERQTY",		INTEGER, 0, 99999999 )
			, createFD( OP, "orderValue",			"ORDERVALUE",				"DPR_ORDER_INFO_ORDERVALUE",	DOUBLE )
			, createFD( OP, "orderTax",				"ORDERTAX",					"DPR_ORDER_INFO_ORDER_TAX",		DOUBLE )
			, createFD( OP, "orderDiscount",		"ORDERDISCOUNT",			"DPR_ORDER_INFO_ORDERDISCOUNT",	DOUBLE )
			, createFD( OP, "orderTotal",			"ORDERTOTAL",				"DPR_ORDER_INFO_ORDERTOTAL",	DOUBLE )
			, createFD( OP, "simulationOrderQty",	"SIMULATION_ORDERQTY",		"DPR_ORDER_INFO_SIMULATION_ORDERQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "simulationOrderValue",	"SIMULATION_ORDERVALUE",	"DPR_ORDER_INFO_SIMULATION_ORDERVALUE",	DOUBLE )
			, createFD( OP, "simulationOrderTax",	"SIMULATION_ORDERTAX",		"DPR_ORDER_INFO_SIMULATION_ORDERTAX",	DOUBLE )
			, createFD( OP, "simulationOrderDiscount",	"SIMULATION_ORDERDISCOUNT",		"DPR_ORDERDTL_SIMULATION_ORDERDISCOUN",	DOUBLE )
			, createFD( OP, "simulationOrderTotal",	"SIMULATION_ORDERTOTAL",	"DPR_ORDER_INFO_SIMULATION_ORDERTOTAL",	DOUBLE )
			, createFD( OP, "confirmedOrderQty",	"CONFIRMED_ORDERQTY",		"DPR_ORDER_INFO_CONFIRMED_ORDERQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "confirmedOrderValue",	"CONFIRMED_ORDERVALUE",		"DPR_ORDER_INFO_CONFIRMED_ORDERVALUE",	DOUBLE )
			, createFD( OP, "confirmedOrderTax",	"CONFIRMED_ORDERTAX",		"DPR_ORDER_INFO_CONFIRMED_ORDERTAX",	DOUBLE )
			, createFD( OP, "confirmedOrderDiscount",	"CONFIRMED_ORDERDISCOUNT",		"DPR_ORDER_INFO_CONFIRMED_ORDERDISCOUNT", DOUBLE )
			, createFD( OP, "confirmedOrderTotal",	"CONFIRMED_ORDERTOTAL",		"DPR_ORDER_INFO_CONFIRMED_ORDERTOTAL",	DOUBLE )
			, createFD( OP, "message",				"MESSAGE",					"DPR_ORDER_MESSAGE",			DESC, 0, 512 )
			, createFD( MD, "status",				"STATUS",					"DPR_ORDER_STATUS",				"DPR_ORDER_STATUS_", "WK,SG,SD,CG,CD,ER,DE" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDER_INFO, table = createTable("DPR_ORDER_INFO", "ORDI", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_INFO, new QueryableImpl(table) {{
			JoinableImpl tbl_ORD = new JoinableImpl( "ORD", "DPR_ORDER", "ORD.ORDER_NUMBER(+) = ORDI.ORDER_NUMBER" );

			/* DPS_ORDER using */
			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "partyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORD.PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)" )
				, new QueryableFieldImpl( STRING, "countryName"
					, "(SELECT SB.COUNTRYNAME FROM DPR_COUNTRY SB WHERE SB.COUNTRY_CD = ORD.COUNTRYCD)" )
			} );

			/* DPR_ORDER using */
			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "customerOrderNumber", "ORD.CUSTOMER_ORDER_NUMBER", tbl_ORD )
				, new QueryableFieldImpl( STRING, "simulationKey", "ORD.SIMULATIONKEY", tbl_ORD )
				, new QueryableFieldImpl( STRING, "countryCode", "ORD.COUNTRYCD", tbl_ORD )
				, new QueryableFieldImpl( STRING, "inDate", "ORD.INDATE", tbl_ORD )
				, new QueryableFieldImpl( STRING, "inDateDefault", "ORD.INDATE_DEF", tbl_ORD )
				, new QueryableFieldImpl( STRING, "inDateSimulation", "ORD.INDATE_SIM", tbl_ORD )
				, new QueryableFieldImpl( STRING, "inDateCreationConfirmed", "ORD.INDATE_CNF", tbl_ORD )
				, new QueryableFieldImpl( STRING, "creditStatus", "ORD.CREDIT_STATUS", tbl_ORD )
				, new QueryableFieldImpl( STRING, "orderStatus", "ORD.ORDER_STATUS", tbl_ORD )
				, new QueryableFieldImpl( STRING, "creationStatus", "ORD.STATUS", tbl_ORD )
				, new QueryableFieldImpl( CODE, "isPackdealOrder", "DECODE(ORD.PACKDEALCD, NULL, 'N', 'Y')", "DPR_PACKDEAL_DEALIND", "PUB_WHETHER_", tbl_ORD )
				, new QueryableFieldImpl( CODE, "dealCode", "ORD.PACKDEALCD", "DPR_PACKDEAL_CODE", tbl_ORD )
			} );

			/* DPR_ORDER_INFO using */
			append( new QueryableField[] {
						new QueryableFieldImplBK( STRING, "organizationName"
					, "(SELECT SB.MASTER_NAME FROM DPR_MASTER_DESC SB"
						+" WHERE SB.MASTER_CD = ORDI.ORGANIZATIONCD AND SB.MASTER_TYPE = 'SO' AND LANGCD = ?)", "displayLanguage" )
				, new QueryableFieldImpl( STRING, "payerPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORDI.PAYER_PARTYCD AND SB.ORGANIZATIONCD = ORDI.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORDI.DIST_CHANNELCD AND SB.DIVISIONCD = ORDI.DIVISIONCD)" )
				, new QueryableFieldImpl( STRING, "billPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORDI.BILL_PARTYCD AND SB.ORGANIZATIONCD = ORDI.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORDI.DIST_CHANNELCD AND SB.DIVISIONCD = ORDI.DIVISIONCD)" )
				, new QueryableFieldImpl( STRING, "soldPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORDI.SOLD_PARTYCD AND SB.ORGANIZATIONCD = ORDI.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORDI.DIST_CHANNELCD AND SB.DIVISIONCD = ORDI.DIVISIONCD)" )
				, new QueryableFieldImpl( STRING, "shipPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORDI.SHIP_PARTYCD AND SB.ORGANIZATIONCD = ORDI.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORDI.DIST_CHANNELCD AND SB.DIVISIONCD = ORDI.DIVISIONCD)" )
			} );

		}} );


		/***************************************************************************************************
			*	DPR_ORDER_INFO_DTL
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "orderNumber",			"ORDER_NUMBER",				"DPR_ORDER_INFO_ORDERNUMBER",	0, 35 )
			, createFD( PM, "lineNumber",			"LINE_NO",					"DPR_ORDER_INFO_DTL_LINENUMBER",	0, 10 )
			, createFD( MD, "itemCode",				"ITEMCD",					"DPR_ORDER_INFO_DTL_ITEMCODE",	0, 20 )
			, createFD( MD, "itemCodeConfirmed",	"ITEMCD_CNF",				"DPR_ORDER_INFO_DTL_ITEMCODE_CONFIRMED",	0, 20 )
			, createFD( MD, "childLineNumber",		"CHILD_LINE_NO",			"DPR_ORDER_INFO_DTL_CHILD_LINENUMBER",	0, 10 )
			, createFD( MD, "itemRefInd",			"ITEMREF_IND",				"DPR_ORDERDTL_ITEMREF_IND",		"DPR_ORDER_DTL_ITEMREF_IND_",	"NO,OG,PO,PI,PP,RP,DC" )
			, createFD( OP, "uom",					"UOM",						"DPR_ORDER_INFO_DTL_UOM",		0, 3 )
			, createFD( OP, "infoPriceCurrency",	"PRICE_CURR",				"DPR_ORDER_INFO_DTL_PRICECURRENCY",	3, 3 )
			, createFD( OP, "infoPrice",			"PRICE",					"DPR_ORDER_INFO_DTL_PRICE",		DOUBLE )
			, createFD( OP, "orderVolume",			"ORDER_VOLUME",				"DPR_ORDER_INFO_DTL_ORDERVOLUME",	DOUBLE )
			, createFD( OP, "orderVolumeUnit",		"ORDER_VOLUME_UNIT",		"DPR_ORDER_INFO_DTL_ORDERVOLUME_UNIT",	0, 20 )
			, createFD( OP, "orderWeight",			"ORDER_WEIGHT",				"DPR_ORDER_INFO_DTL_ORDERWEIGHT",	DOUBLE )
			, createFD( OP, "orderWeightUnit",		"ORDER_WEIGHT_UNIT",		"DPR_ORDER_INFO_DTL_ORDERWEIGHT_UNIT",	0, 20 )
			, createFD( OP, "orderQty",				"ORDERQTY",					"DPR_ORDER_INFO_DTL_ORDERQTY",		INTEGER, 0, 99999999 )
			, createFD( OP, "orderValue",			"ORDERVALUE",				"DPR_ORDER_INFO_DTL_ORDERVALUE",	DOUBLE )
			, createFD( OP, "orderTax",				"ORDERTAX",					"DPR_ORDER_INFO_DTL_ORDER_TAX",		DOUBLE )
			, createFD( OP, "orderDiscount",		"ORDERDISCOUNT",			"DPR_ORDER_INFO_DTL_ORDERDISCOUNT",	DOUBLE )
			, createFD( OP, "orderTotal",			"ORDERTOTAL",				"DPR_ORDER_INFO_DTL_ORDERTOTAL",	DOUBLE )
			, createFD( OP, "simulationOrderQty",	"SIMULATION_ORDERQTY",		"DPR_ORDER_INFO_DTL_SIMULATION_ORDERQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "simulationOrderValue",	"SIMULATION_ORDERVALUE",	"DPR_ORDER_INFO_DTL_SIMULATION_ORDERVALUE",	DOUBLE )
			, createFD( OP, "simulationOrderTax",	"SIMULATION_ORDERTAX",		"DPR_ORDER_INFO_DTL_SIMULATION_ORDERTAX",	DOUBLE )
			, createFD( OP, "simulationOrderDiscount",	"SIMULATION_ORDERDISCOUNT",		"DPR_ORDER_INFO_DTL_SIMULATION_ORDERDISCOUNT",	DOUBLE )
			, createFD( OP, "simulationOrderTotal",	"SIMULATION_ORDERTOTAL",	"DPR_ORDER_INFO_DTL_SIMULATION_ORDERTOTAL",	DOUBLE )
			, createFD( OP, "confirmedOrderQty",	"CONFIRMED_ORDERQTY",		"DPR_ORDER_INFO_DTL_CONFIRMED_ORDERQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "confirmedOrderValue",	"CONFIRMED_ORDERVALUE",		"DPR_ORDER_INFO_DTL_CONFIRMED_ORDERVALUE",	DOUBLE )
			, createFD( OP, "confirmedOrderTax",	"CONFIRMED_ORDERTAX",		"DPR_ORDER_INFO_DTL_CONFIRMED_ORDERTAX",	DOUBLE )
			, createFD( OP, "confirmedOrderDiscount",	"CONFIRMED_ORDERDISCOUNT",		"DPR_ORDER_INFO_DTL_CONFIRMED_ORDERDISCOUNT", DOUBLE )
			, createFD( OP, "confirmedOrderTotal",	"CONFIRMED_ORDERTOTAL",		"DPR_ORDER_INFO_DTL_CONFIRMED_ORDERTOTAL",	DOUBLE )
			, createFD( OP, "deliveryDate",			"INDATE",					"DPR_ORDER_INFO_INDATE",		DATE )
			, createFD( OP, "deliveryStatus",		"DLVRY_STATUS",				"DPR_ORDER_INFO_DTL_DLVRY_STATUS",	"DPR_ORDER_INFO_DTL_DLVRY_STATUS_",	"NR,OP,CP,CE,DE" )
			, createFD( OP, "deliveryOpenQty",		"DLVRY_OPENQTY",			"DPR_ORDER_INFO_DTL_DLVRY_OPENQTY",	INTEGER )
			, createFD( OP, "deliveryIntraQty",		"DLVRY_INTRAQTY",			"DPR_ORDER_INFO_DTL_DLVRY_INTRAQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "deliveryCompQty",		"DLVRY_COMPQTY",			"DPR_ORDER_INFO_DTL_DLVRY_COMPQTY",	INTEGER, 0, 99999999 )
			, createFD( OP, "reason",				"REASON",					"DPR_ORDER_INFO_REASON",		0, 512 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDER_INFO_DTL, table = createTable("DPR_ORDER_INFO_DTL", "OIND", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_INFO_DTL, new QueryableImpl(table) {{
			Joinable tbl_ODTL = new JoinableImpl( "ODTL", "DPR_ORDER_DTL", "ODTL.ORDER_NUMBER(+) = OIND.ORDER_NUMBER "
					+ "AND ODTL.ITEMCD(+) = OIND.ITEMCD AND ODTL.ITEMCD_CNF(+) = OIND.ITEMCD_CNF AND ODTL.CHILD_LINE_NO(+) = OIND.CHILD_LINE_NO" );

			JoinableImpl tbl_ORD = new JoinableImpl( "ORD", "DPR_ORDER", "ORD.ORDER_NUMBER = OIND.ORDER_NUMBER" );

			append( new QueryableFieldImpl[] {
				  new QueryableFieldImpl( STRING, "partyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORD.SOLD_PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)", tbl_ORD )
				, new QueryableFieldImpl( STRING, "shipPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORD.SHIP_PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)", tbl_ORD )
			});

			JoinableImpl tbl_UOM = new JoinableImpl( "IMU", "DPR_ITEM_MASTER_UOM"
					, "IMU.ITEMCD(+) = ODTL.ITEMCD AND IMU.UOM_CD(+) = ODTL.UOM", tbl_ODTL );

			JoinableImpl tbl_UOMINFO = new JoinableImpl( "IMU_INFO", "DPR_ITEM_MASTER_UOM"
					, "IMU_INFO.ITEMCD(+) = OIND.ITEMCD AND IMU_INFO.UOM_CD(+) = OIND.UOM" );

			append( new QueryableField[] {
				  new QueryableFieldImpl( DOUBLE, "deliveryCompValue", "NVL(OIND.DLVRY_COMPQTY, 0) * NVL(OIND.PRICE, 0)" )
				, new QueryableFieldImplBK( DESC, "itemName"
						, "(SELECT SB.ITEMNAME FROM DPR_ITEM_MASTER_DESC SB WHERE SB.ITEMCD = OIND.ITEMCD AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage" )
				, new QueryableFieldImplBK( DESC, "itemNameConfirmed"
						, "(SELECT SB.ITEMNAME FROM DPR_ITEM_MASTER_DESC SB WHERE SB.ITEMCD = OIND.ITEMCD_CNF AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage" )
				, new QueryableFieldImpl( STRING, "headerStatus", "ORD.STATUS"
						, new JoinableImpl("ORD", "DPR_ORDER", "ODTL.ORDERKEY(+) = ORD.ORDER_KEY", tbl_ODTL) )
				, new QueryableFieldImpl( STRING, "orderStatus", "ORD.ORDER_STATUS", tbl_ORD )
				, new QueryableFieldImpl( STRING, "customerOrderNumber", "ORD.SIMULATIONKEY", tbl_ORD )
				, new QueryableFieldImpl( CODE, "isPackdealOrder", "DECODE(ORD.PACKDEALCD, NULL, 'N', 'Y')", "DPR_PACKDEAL_DEALIND", "PUB_WHETHER_", tbl_ORD )
				, new QueryableFieldImpl( CODE, "dealCode", "ORD.PACKDEALCD", "DPR_PACKDEAL_CODE", tbl_ORD )
				, new QueryableFieldImpl( STRING, "orderType", "ORD.ORDER_TYPE", "DPR_ORDER_ORDERTYPE", tbl_ORD )
			} );

			/* ODINF and IMEAN_CNF */
			QueryBuffer inner_querybuf = new QueryBuffer();
			inner_querybuf.appendData( "ORDI.ORGANIZATIONCD" );
			inner_querybuf.appendData( "OIND.ORDER_NUMBER" );
			inner_querybuf.appendData( "OIND.LINE_NO" );
			inner_querybuf.appendData( "OIND.ITEMCD" );
			inner_querybuf.appendData( "OIND.ITEMCD_CNF" );
			inner_querybuf.appendData( "OIND.ITEMREF_IND" );
			inner_querybuf.appendTableWithAlias( "DPR_ORDER_INFO", "ORDI" );
			inner_querybuf.appendTableWithAlias( "DPR_ORDER_INFO_DTL", "OIND" );
			inner_querybuf.appendCondition( "ORDI.ORDER_NUMBER = OIND.ORDER_NUMBER" );
			JoinableImpl tbl_ODINF = new JoinableImpl( "ODINF", "("+ inner_querybuf +")", "ODINF.ORDER_NUMBER = OIND.ORDER_NUMBER"
							+ " AND ODINF.LINE_NO = OIND.LINE_NO"
							+ " AND ODINF.ITEMCD = OIND.ITEMCD"
							+ " AND ODINF.ITEMCD_CNF = OIND.ITEMCD_CNF"
							+ " AND ODINF.ITEMREF_IND = OIND.ITEMREF_IND"
							, tbl_ODTL );

			append( com.irt.dpr.Schema.makeItemEANQueryableFields( "IMEAN_CNF", "ODINF", tbl_ODINF, true ) );
			JoinableImpl tbl_IMEAN_CNF = getItemEANJoinable("IMEAN_CNF", "ODINF", true );
			append( new QueryableFieldImpl( STRING, "consumerEANCode", "NVL(ODTL.CONS_EAN, IMEAN_CNF.ITEM_CONS_EAN)", new JoinableWrapper(tbl_ODINF,tbl_IMEAN_CNF) ) );;

			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES"
												  , "PTYS.PARTYCD(+) = ORD.PARTYCD"
												  + " AND PTYS.ORGANIZATIONCD(+) = ORD.ORGANIZATIONCD"
												  + " AND PTYS.DIST_CHANNELCD(+) = ORD.DIST_CHANNELCD"
												  + " AND PTYS.DIVISIONCD(+) = ORD.DIVISIONCD", tbl_ORD );

			JoinableImpl tbl_ORDI = new JoinableImpl( "ORDI", "DPR_ORDER_INFO", "ORDI.ORDER_NUMBER = OIND.ORDER_NUMBER" );
			append( new QueryableField[] {
					  new QueryableFieldImpl( CODE, "organizationCode", "ORDI.ORGANIZATIONCD", tbl_ORDI )
					, new QueryableFieldImpl( CODE, "partyCode", "ORDI.SOLD_PARTYCD", tbl_ORDI )
					, new QueryableFieldImpl( CODE, "shipPartyCode", "ORDI.SHIP_PARTYCD", tbl_ORDI )
					, new QueryableFieldImpl( DATE, "orderDate", "ORDI.ORDDATE", tbl_ORDI )
					, new QueryableFieldImpl( CODE, "officeCode", "PTYS.OFFICECD", tbl_PTYS )
					, new QueryableFieldImpl( CODE, "groupCode", "PTYS.GROUPCD", tbl_PTYS )
			});

			/* formatQty */
			append( new QueryableField[] {
						new QueryableFieldImplBK( STRING, "formatQty"
						, "OIND.ORDERQTY * IMU.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD AND SB.UOM_CD(+) = NVL(?, OIND.UOM))"
						, "formatUOM", tbl_UOMINFO )
				, new QueryableFieldImpl( STRING, "formatPCQty"
						, "OIND.ORDERQTY * IMU.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD AND SB.UOM_CD(+) = 'PC')", tbl_UOMINFO )
				, new QueryableFieldImpl( STRING, "formatDozenQty"
						, "OIND.ORDERQTY * IMU.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD AND SB.UOM_CD(+) = 'DZ')", tbl_UOMINFO )
				, new QueryableFieldImplBK( STRING, "formatSimulationQty"
						, "OIND.SIMULATION_ORDERQTY * IMU.PACKSIZE"
								+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD AND SB.UOM_CD(+) = NVL(?, OIND.UOM) )"
						, "formatUOM", tbl_UOMINFO )
				, new QueryableFieldImpl( STRING, "formatSimulationPCQty"
						, "OIND.SIMULATION_ORDERQTY * IMU.PACKSIZE"
								+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD AND SB.UOM_CD(+) = 'PC' )", tbl_UOMINFO )
				, new QueryableFieldImpl( STRING, "formatSimulationDozenQty"
						, "OIND.SIMULATION_ORDERQTY * IMU.PACKSIZE"
								+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD AND SB.UOM_CD(+) = 'DZ' )", tbl_UOMINFO )
				, new QueryableFieldImplBK( STRING, "formatConfirmedQty"
						, "NVL(OIND.CONFIRMED_ORDERQTY, ODTL.CONFIRMED_ORDERQTY) * IMU_INFO.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD AND SB.UOM_CD(+) = NVL(?, NVL(OIND.UOM, ODTL.UOM)) )"
						, "formatUOM", tbl_UOM )
				, new QueryableFieldImpl( STRING, "formatConfirmedPCQty"
						, "NVL(OIND.CONFIRMED_ORDERQTY, ODTL.CONFIRMED_ORDERQTY) * IMU_INFO.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD AND SB.UOM_CD(+) = 'PC' )", tbl_UOM )
				, new QueryableFieldImpl( STRING, "formatConfirmedDozenQty"
						, "NVL(OIND.CONFIRMED_ORDERQTY, ODTL.CONFIRMED_ORDERQTY) * IMU_INFO.PACKSIZE"
							+" / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
								+" WHERE SB.ITEMCD(+) = OIND.ITEMCD AND SB.UOM_CD(+) = 'DZ' )", tbl_UOM )
			} );
		}

			@Override
			public boolean appendCondition( ConditionQueryBuffer querybuf ) {
				return makeCompositeOrderNumberCondition(querybuf);
			}

		} );


		/***************************************************************************************************
			*	DPR_ORDER_BILLING
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "billingNumber",		"BILLING_NUMBER",			"DPR_ORDERBILLING_NUMBER",		0, 35 )
			, createFD( MD, "orderNumber",			"ORDER_NUMBER",				"DPR_ORDERBILLING_ORDERNUMBER",	0, 35 )
			, createFD( OP, "purchaseOrderNumber",	"PO_NUMBER",				"DPR_ORDERBILLING_PURCHASORDERNUMBER",	0, 35 )
			, createFD( MD, "billingDate",			"BILLING_DATE",				"DPR_ORDERBILLING_BILLINGDATE",	DATE )
			, createFD( OP, "vatNumber",			"VAT_NO",					"DPR_ORDERBILLING_VATNUMBER",	0, 20 )
			, createFD( OP, "payerPartyCode",		"PAYER_PARTYCD",			"DPR_ORDERBILLING_PAYERPARTYCODE",	0, 15 )
			, createFD( OP, "billPartyCode",		"BILL_PARTYCD",				"DPR_ORDERBILLING_BILLPARTYCODE",	0, 15 )
			, createFD( OP, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_ORDERBILLING_SHIPPARTYCODE",	0, 15 )
			, createFD( OP, "soldPartyCode",		"SOLD_PARTYCD",				"DPR_ORDERBILLING_SOLDPARTYCODE",	0, 15 )
			, createFD( OP, "invoiceValue",			"INVOICE_VALUE",			"DPR_ORDERBILLING_INVOICEVALUE",DOUBLE )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( RD, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDER_BILLING, table = createTable("DPR_ORDER_BILLING", "OBIL", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_BILLING, new QueryableImpl(table) {{
			Joinable tbl_ORD = new JoinableImpl( "ORD", "DPR_ORDER", "ORD.ORDER_NUMBER(+) = OBIL.ORDER_NUMBER" );

			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "title", "'Billing'" )
				, new QueryableFieldImpl( STRING, "orderType", "ORD.ORDER_TYPE", tbl_ORD )
				, new QueryableFieldImpl( STRING, "soldPartyName", "(SELECT PARTYNAME FROM DPR_PARTY_SALES SB"
						+ " WHERE SB.PARTYCD = ORD.SOLD_PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
						+ " AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)", tbl_ORD )
				, new QueryableFieldImpl( STRING, "orderType", "ORD.ORDER_TYPE", tbl_ORD )
			} );

		}} );


		/***************************************************************************************************
			*	DPR_ORDER_BILLING_DTL
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "billingNumber",		"BILLING_NUMBER",			"DPR_ORDERBILLING_NUMBER",		0, 35 )
			, createFD( PM, "lineNumber",			"LINE_NO",					"DPR_ORDERBILLING_DTL_LINENO",	0, 10 )
			, createFD( MD, "itemCode",				"ITEMCD",					"DPR_ORDERBILLING_DTL_ITEMCD",	0, 20 )
			, createFD( OP, "orderNumber",			"ORDER_NUMBER",				"DPR_ORDERBILLING_ORDERNUMBER",	0, 35 )
			, createFD( OP, "uom",					"UOM",						"DPR_ORDERBILLING_DTL_UOM",		0, 3 )
			, createFD( OP, "volume",				"VOLUME",					"DPR_ORDERBILLING_VOLUME",		DOUBLE )
			, createFD( OP, "volumeUnit",			"VOLUME_UNIT",				"DPR_ORDERBILLING_VOLUME_UNIT",	0, 20 )
			, createFD( OP, "weight",				"WEIGHT",					"DPR_ORDERBILLING_WEIGHT",		DOUBLE )
			, createFD( OP, "weightUnit",			"WEIGHT_UNIT",				"DPR_ORDERBILLING_WEIGHT_UNIT",	0, 20 )
			, createFD( OP, "billingQty",			"BILLING_QTY",				"DPR_ORDERBILLING_DTL_QTY",		DOUBLE )
			, createFD( OP, "billingNetAmount",		"BILLING_NETAMOUNT",		"DPR_ORDERBILLING_DTL_NETAMOUNT",	DOUBLE )
			, createFD( OP, "billingTax",			"BILLING_TAX",				"DPR_ORDERBILLING_DTL_TAX",		DOUBLE )
			, createFD( OP, "billingDamagedDiscount",	"BILLING_DAMAGEDDISCOUNT",	"DPR_ORDERBILLING_DTL_DAMAGEDDISCOUNT",		DOUBLE )
			, createFD( OP, "billingValue",			"BILLING_VALUE",			"DPR_ORDERBILLING_DTL_VALUE",	DOUBLE )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( RD, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDER_BILLING_DTL, table = createTable("DPR_ORDER_BILLING_DTL", "OBID", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_BILLING_DTL, new QueryableImpl(table) {{

			append( new QueryableField[] {
								new QueryableFieldImplBK( DESC, "itemName"
						, "(SELECT SB.ITEMNAME FROM DPR_ITEM_MASTER_DESC SB WHERE SB.ITEMCD = OBID.ITEMCD AND SB.LANGCD = ?)", "displayLanguage" )
				, new QueryableFieldImpl( STRING, "calculatePrice", "OBID.BILLING_NETAMOUNT / NULLIF(BILLING_QTY, 0)" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_ORDER_DELIVERY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "deliveryNumber",		"DELIVERY_NUMBER",			"DPR_ORDERDELIVERY_NUMBER",		0, 35 )
			, createFD( MD, "orderNumber",			"ORDER_NUMBER",				"DPR_ORDERDELIVERY_ORDERNUMBER",0, 35 )
			, createFD( MD, "deliveryDate",			"DELIVERY_DATE",			"DPR_ORDERDELIVERY_DELIVERYDATE",DATE )
			, createFD( OP, "extraValue1",			"EXTRAVALUE1",				"DPR_ORDERDELIVERY_EXTRAVALUE1",0, 20 )
			, createFD( OP, "extraValue2",			"EXTRAVALUE2",				"DPR_ORDERDELIVERY_EXTRAVALUE2",0, 20 )
			, createFD( OP, "extraValue3",			"EXTRAVALUE3",				"DPR_ORDERDELIVERY_EXTRAVALUE3",0, 20 )
			, createFD( OP, "extraValue4",			"EXTRAVALUE4",				"DPR_ORDERDELIVERY_EXTRAVALUE4",DOUBLE )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( RD, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDER_DELIVERY, table = createTable("DPR_ORDER_DELIVERY", "ODLV", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_DELIVERY, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "title", "'Delivery'" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_ORDER_MEMOS
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( MD, "memoNumber",			"MEMOS_NUMBER",				"DPR_ORDERMEMOS_NUMBER",		0, 35 )
			, createFD( MD, "orderNumber",			"ORDER_NUMBER",				"DPR_ORDERMEMOS_ORDERNUMBER",	0, 35 )
			, createFD( OP, "memoDate",				"MEMOS_DATE",				"DPR_ORDERMEMOS_MEMOSDATE",		DATE )
			, createFD( OP, "memoValue",			"MEMOS_VALUE",				"DPR_ORDERMEMOS_MEMOSDATE",		0, 50 )
			, createFD( OP, "extraValue1",			"EXTRAVALUE1",				"DPR_ORDERMEMOS_EXTRAVALUE1",	0, 50 )
			, createFD( OP, "extraValue2",			"EXTRAVALUE2",				"DPR_ORDERMEMOS_EXTRAVALUE2",	0, 50 )
			, createFD( OP, "extraValue3",			"EXTRAVALUE3",				"DPR_ORDERMEMOS_EXTRAVALUE3",	0, 50 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( RD, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDER_MEMOS, table = createTable("DPR_ORDER_MEMOS", "OMMS", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_MEMOS, new QueryableImpl(table) {{
			QueryBufferValid valid_credit = new Memos.Valid( "extraValue1", Memos.CREDIT_MEMEOS );
			QueryBufferValid valid_debit = new Memos.Valid( "extraValue1", Memos.DEBIT_MEMEOS );

			appendCND( valid_credit, new QueryableField[] {
						new QueryableFieldImpl( STRING, "title", "'Return Credit Memo'" )
			} );

			appendCND( valid_debit, new QueryableField[] {
						new QueryableFieldImpl( STRING, "title1", "'Return Debit Memo'" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_ORDER_REMARK
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "orderKey",				"ORDERKEY",					"DPR_ORDER_KEY",				0, 20 )
			, createFD( MD, "writeUserId",			"WRITE_USERID",				"DPR_ORDER_REMARK_WRITEUSER",	0, 30 )
			, createFD( OP, "orderNumber",			"ORDER_NUMBER",				"DPR_ORDER_ORDERNUMBER",		0, 35 )
			, createFD( OP, "remark",				"REMARK",					"DPR_ORDER_REMARK",				0, 1024 )
			, createFD( OP, "answerRemark",			"ANS_REMARK",				"DPR_ORDER_ANSWER_REMARK",		0, 1024 )
			, createFD( OP, "extraValue1",			"EXTRAVALUE1",				"DPR_ORDER_REMARK_EXTRAVALUE1",	0, 50 )
			, createFD( OP, "extraValue2",			"EXTRAVALUE2",				"DPR_ORDER_REMARK_EXTRAVALUE2",	0, 50 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
		};
		putTable( DPR_ORDER_REMARK, table = createTable("DPR_ORDER_REMARK", "ORMK", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_REMARK, new QueryableImpl(table) {{
			Joinable tbl_ORD = new JoinableImpl( "ORD", "DPR_ORDER", "ORD.ORDER_KEY = ORMK.ORDERKEY" );

			append( new QueryableField[] {
						new QueryableFieldImpl( STRING, "organizationCode", "ORD.ORGANIZATIONCD", tbl_ORD )
				, new QueryableFieldImpl( STRING, "orderDate", "ORD.ORDDATE", tbl_ORD )
				, new QueryableFieldImpl( STRING, "inDateConfirm", "ORD.INDATE_CNF", tbl_ORD )
				, new QueryableFieldImpl( STRING, "soldPartyCode", "ORD.SOLD_PARTYCD", tbl_ORD )
				, new QueryableFieldImpl( STRING, "shipPartyCode", "ORD.SHIP_PARTYCD", tbl_ORD )
				, new QueryableFieldImplBK( STRING, "organizationName"
					, "(SELECT SB.MASTER_NAME FROM DPR_MASTER_DESC SB"
						+" WHERE SB.MASTER_CD = ORD.ORGANIZATIONCD AND SB.MASTER_TYPE = 'SO' AND LANGCD = ?)", "displayLanguage" )
				, new QueryableFieldImpl( STRING, "soldPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORD.SOLD_PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)" )
				, new QueryableFieldImpl( STRING, "shipPartyName"
					, "(SELECT SB.PARTYNAME FROM DPR_PARTY_SALES SB"
						+" WHERE SB.PARTYCD = ORD.SHIP_PARTYCD AND SB.ORGANIZATIONCD = ORD.ORGANIZATIONCD"
							+" AND SB.DIST_CHANNELCD = ORD.DIST_CHANNELCD AND SB.DIVISIONCD = ORD.DIVISIONCD)" )
			}  );
		}} );


		/***************************************************************************************************
			*	DPR_ORDER_TEMPLATE
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "templateKey",			"TEMPLATE_KEY",				"DPR_TEMPLATE_KEY",				0, 20 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( MD, "templateName",			"TEMPLATE_NAME",			"DPR_TEMPLATE_NAME",			0, 50 )
			, createFD( OP, "payerPartyCode",		"PAYER_PARTYCD",			"DPR_TEMPLATE_PAYERPARTYCODE",	0, 15 )
			, createFD( OP, "billPartyCode",		"BILL_PARTYCD",				"DPR_TEMPLATE_BILLPARTYCODE",	0, 15 )
			, createFD( MR, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_TEMPLATE_SHIPPARTYCODE",	0, 15 )
			, createFD( MR, "soldPartyCode",		"SOLD_PARTYCD",				"DPR_TEMPLATE_SOLDPARTYCODE",	0, 15 )
			, createFD( OP, "registPath",			"REGIST_PATH",				"DPR_TEMPLATE_REGISTPATH",		"DPR_REGISTPATH_", "SIM,ORD" )
			, createFD( OP, "publicInd",			"PUBLIC_IND",				"DPR_TEMPLATE_PUBLICIND",		1, 1 )
			, createFD( OP, "viewPartyCode",		"VIEW_PARTYCD",				"DPR_TEMPLATE_VIEWPARTYCODE",	0, 15 )
			, createFD( OP, "manageUserId",			"MNGUSERID",				"DPR_TEMPLATE_USERID",			0, 30 )
			, createFD( MR, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"DPR_TEMPLATE_UPDATEDATETIME",	DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"DPR_TEMPLATE_UPGUSERID",		0, 30 )
		};
		putTable( DPR_ORDER_TEMPLATE, table = createTable("DPR_TEMPLATE", "TEM", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_TEMPLATE, new QueryableImpl(table) {{
			append( new QueryableField[] {
				new QueryableFieldImpl( INTEGER, "items"
					, "(SELECT COUNT(*) FROM DPR_TEMPLATE_DTL TDTL WHERE TDTL.TEMPLATEKEY = TEM.TEMPLATE_KEY)" )
/* WORKING soldPartyName */

/* WORKING 占쌩븝옙 partyName 占쏙옙占쏙옙. 占쏙옙占쏙옙占? org占쏙옙 占쌩븝옙占쏙옙 party占쏙옙 partyName占쏙옙占쏙옙占싹울옙 distinct占쏙옙 처占쏙옙占쏙옙. ) */
			,	new QueryableFieldImpl( STRING, "soldPartyName"
					, "(SELECT DISTINCT PARTYNAME FROM DPR_PARTY_SALES DPS WHERE DPS.PARTYCD = TEM.SOLD_PARTYCD)" )
			} );
		}} );

		/***************************************************************************************************
			*	DPR_ORDER_TEMPLATE_DTL
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "templateKey",			"TEMPLATEKEY",				"DPR_TEMPLATE_KEY",				0, 20 )
			, createFD( PM, "lineNumber",			"LINE_NO",					"DPR_TEMPLATE_LINE_NO",			0, 20 )
			, createFD( MD, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( OP, "orderQty",				"ORDER_QTY",				"DPR_TEMPLATEDTL_ORDERQTY",		INTEGER, 1, 9999 )
			, createFD( OP, "uom",					"UOM",						"DPR_TEMPLATEDTL_UOM",			0, 3 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_ORDER_TEMPLATE_DTL, table = createTable("DPR_TEMPLATE_DTL", "TDTL", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_ORDER_TEMPLATE_DTL, new QueryableImpl(table) {{
			append( new QueryableField[] {
				new QueryableFieldImplBK( DESC, "itemName"
				, "(SELECT SB.ITEMNAME FROM DPR_ITEM_MASTER_DESC SB WHERE SB.ITEMCD = TDTL.ITEMCD AND LANGCD = ?)", "displayLanguage" )
			} );
		}} );


		/***************************************************************************************************
		 *	DPR_BILLING_REPORT
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",		"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "partyCode",			"PARTYCD",				"DPR_ORDER_SOLDPARTYCODE",		0, 15 )
			, createFD( PM, "customerOrderNumber",	"CUSTOMER_ORDER_NUMBER",	"DPR_ORDER_CUSTOMERORDERNUMBER",	0, 35 )
			, createFD( PM, "billVatNumber",		"VAT_NUMBER",			"DPR_ORDERBILLING_VATNUMBER",			0, 20 )
			, createFD( PM, "billDate",				"BILL_DATE",			"DPR_BILLINGREPORT_BILLDATE",			DATE )
			, createFD( MD, "billPostNumber",		"POST_NUMBER",			"DPR_BILLINGREPORT_POSTNUMBER",			0, 35 )
			, createFD( OP, "billShipPartyCode",	"SHIP_PARTYCD",			"DPR_ORDER_SHIPPARTYCODE",		0, 15 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_BILLING_REPORT, table = createTable("DPR_BILLING_REPORT", "BLRPT", tfields, "UPGDATE = SYSDATE") );
		putQueryable( DPR_BILLING_REPORT, new QueryableImpl(table) {{
			Joinable tbl_ORD = new JoinableImpl( "ORD", "DPR_ORDER", "ORD.PARTYCD = BLRPT.PARTYCD AND ORD.SIMULATIONKEY = BLRPT.CUSTOMER_ORDER_NUMBER" );

			append( new QueryableField[] {
				  new QueryableFieldImpl( DATE, "orderDate", "ORD.ORDDATE", "DPR_ORDER_ORDERDATE", tbl_ORD )
				, new QueryableFieldImpl( STRING, "orderNumber", "ORD.ORDER_NUMBER", "DPR_ORDER_ORDERNUMBER", tbl_ORD )
				, new QueryableFieldImpl( STRING, "shipPartyCode", "NVL(BLRPT.SHIP_PARTYCD, ORD.SHIP_PARTYCD)", "DPR_ORDER_SHIPPARTYCODE", tbl_ORD )
				, new QueryableFieldImpl( STRING, "linkExternal", "pkSYSStandard.fGetSystemEnvChar( 'DPR', 'BillingLinkExternalPrefix;' || BLRPT.ORGANIZATIONCD ) || BLRPT.POST_NUMBER" )
				, new QueryableFieldImpl( STRING, "orderType", "ORD.ORDER_TYPE", "DPR_ORDER_ORDERTYPE", tbl_ORD )
			});
		}

			@Override
			public boolean appendCondition( ConditionQueryBuffer querybuf ) {
				return makeCompositeOrderNumberCondition(querybuf);
			}

		} );


		/***************************************************************************************************
			*	DPR_SALES_MOV
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode","DIST_CHANNELCD",			"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( PM, "officeCode",			"OFFICECD",					"DPR_SALES_OFFICE_CODE",		0, 8 )
			, createFD( PM, "dangerousInd",			"DANGEROUS_IND",			"DPR_SALES_MOV_DANGEROUS",		0, 1, CHARS )
			, createFD( MD, "minimumValue",			"MIN_VALUE",				"DPR_SALES_MOV",				DOUBLE )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable ( DPR_SALES_MOV, table = createTable("DPR_SALES_MOV", "SMOV", tfields) );
		putQueryable( DPR_SALES_MOV, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SMOV.ORGANIZATIONCD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_ORGANIZATION_NAME" )
				, new QueryableFieldImplBK( STRING, "distributionChannelName"
						, "( SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SMOV.DIST_CHANNELCD"
							+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DISTRIBUTIONCHANNEL_NAME" )
				, new QueryableFieldImplBK( DESC, "divisionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SMOV.DIVISIONCD"
							+ " AND SB.MASTER_TYPE = 'DI' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DIVISION_NAME" )
				, new QueryableFieldImplBK( DESC, "officeName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SMOV.OFFICECD"
							+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_OFFICE_NAME" )
			} );
		}} );

		/***************************************************************************************************
			*	DPR_SALES_MOVPTY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode","DIST_CHANNELCD",			"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( PM, "officeCode",			"OFFICECD",					"DPR_SALES_OFFICE_CODE",		0, 8 )
			, createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 8 )
			, createFD( PM, "dangerousInd",			"DANGEROUS_IND",			"DPR_SALES_MOV_DANGEROUS",		0, 1, CHARS )
			, createFD( MD, "minimumValue",			"MIN_VALUE",				"DPR_SALES_MOV",				DOUBLE )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable ( DPR_SALES_MOVPTY, table = createTable("DPR_SALES_MOVPTY", "PMOV", tfields) );
		putQueryable( DPR_SALES_MOVPTY, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PMOV.ORGANIZATIONCD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_ORGANIZATION_NAME" )
				, new QueryableFieldImplBK( STRING, "distributionChannelName"
						, "( SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PMOV.DIST_CHANNELCD"
							+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DISTRIBUTIONCHANNEL_NAME" )
				, new QueryableFieldImplBK( DESC, "divisionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PMOV.DIVISIONCD"
							+ " AND SB.MASTER_TYPE = 'DI' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DIVISION_NAME" )
				, new QueryableFieldImplBK( DESC, "officeName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = PMOV.OFFICECD"
							+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_OFFICE_NAME" )
			} );
		}} );


		/***************************************************************************************************
		*	DPR_SALES_MOVSPTY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_SALESORGANIZATION_CODE",	0, 8 )
			, createFD( PM, "distributionChannelCode","DIST_CHANNELCD",			"DPR_DISTRIBUTIONCHANNEL_CODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_DIVISION_CODE",			0, 8 )
			, createFD( PM, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_SHIPPARTY_CODE",			0, 8 )
			, createFD( PM, "dangerousInd",			"DANGEROUS_IND",			"DPR_SALES_MOV_DANGEROUS",		0, 1, CHARS )
			, createFD( MD, "minimumValue",			"MIN_VALUE",				"DPR_SALES_MOV",				DOUBLE )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable ( DPR_SALES_MOVSPTY, table = createTable("DPR_SALES_MOVSPTY", "SPMOV", tfields) );
		putQueryable( DPR_SALES_MOVSPTY, new QueryableImpl(table) {{
			append( new QueryableField[] {
						new QueryableFieldImplBK( DESC, "organizationName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SPMOV.ORGANIZATIONCD"
							+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_ORGANIZATION_NAME" )
				, new QueryableFieldImplBK( STRING, "distributionChannelName"
						, "( SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SPMOV.DIST_CHANNELCD"
							+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DISTRIBUTIONCHANNEL_NAME" )
				, new QueryableFieldImplBK( DESC, "divisionName"
						, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SPMOV.DIVISIONCD"
							+ " AND SB.MASTER_TYPE = 'DI' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DIVISION_NAME" )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_SKU_MAP
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ITEM_CODE",				0, 20 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( MD, "distributorItemCode",	"DIST_ITEMCD",				"DPR_SKUMAP_DISTRIBUTIONITEMCODE",	0, 20 )
			, createFD( OP, "distributionCustomerId","DIST_CUSTOMERID",			"DPR_SKUMAP_DISTRIBUTIONCUSTOMERID",	0, 15 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_SKU_MAP, table = createTable("DPR_SKU_MAP", "SKUM", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
			*	DPR_CUSTOMER_MAP
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "partyCode",			"PARTYCD",					"DPR_PARTY_CODE",				0, 15 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_COUNTRY_CODE",				0, 15 )
			, createFD( MD, "linkPartyCode",		"LINK_PARTYCD",				"DPR_CUSTOMERMAP_LINKPARTYCODE",0, 15 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_CUSTOMER_MAP, table = createTable("DPR_CUSTOMER_MAP", "CUTM", tfields ) );


		/***************************************************************************************************
			*	DPR_INTERFACE_LOG
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "interfaceSequence",	"INTERFACE_SEQ",			"DPR_INTERFACELOG_SEQ",			0, 20 )
			, createFD( MD, "interfaceMethod",		"INTERFACE_METHOD",			"DPR_INTERFACELOG_METHOD",		0, 10 )
			, createFD( OP, "interfaceType",		"INTERFACE_TYPE",			"DPR_INTERFACELOG_TYPE",		0, 5 )
			, createFD( OP, "inOut",				"INOUT",					"DPR_INTERFACELOG_INOUT",		0, 2 )
			, createFD( MD, "totalLineCount",		"TOTAL_LINENO",				"DPR_INTERFACELOG_TOTALLINECOUNT",	DOUBLE )
			, createFD( MD, "errorCount",			"ERROR_NO",					"DPR_INTERFACELOG_ERRORCOUNT",	DOUBLE )
			, createFD( MD, "executeDateTime",		"EXEC_DATETIME",			"DPR_INTERFACELOG_EXECUTEDATETIME",	DATE )
			, createFD( OP, "completeDateTime",		"COMPLETE_DATETIME",		"DPR_INTERFACELOG_COMPLETEDATETIME",DATE )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
		};
		putTable( DPR_INTERFACE_LOG, table = createTable("DPR_INTERFACE_LOG", "ILOG", tfields ) );


		/***************************************************************************************************
			*	DPR_SITE_LINK
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( PM, "linkSequence",			"LINK_SEQ",					"DPR_SITELINK_LINKSEQUENCE",	0, 20 )
			, createFD( MD, "displaySequence",		"DISPLAY_SEQ",				"DPR_SITELINK_DISPLAYSEQUENCE",	INTEGER )
			, createFD( MD, "displayCountryCode",	"DISPLAY_COUNTRYCD",		"DPR_SITELINK_DISPLAYCOUNTRYCODE",0, 15 )
			, createFD( MD, "linkURL",				"URL",						"DPR_SITELINK_LINKURL"			,0, 512 )
			, createFD( MD, "description",			"DESCRIPTION",				"DPR_SITELINK_DESCRIPTION",		DESC, 0, 1024 )
			, createFD( RD, "status",				"STATUS",					"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",					0, 30 )
		};
		putTable( DPR_SITE_LINK, table = createTable("DPR_SITE_LINK", "SLNK", tfields ) );
		putQueryable( DPR_SITE_LINK, new QueryableImpl(table) {{
			Joinable tbl_UPT = new JoinableImpl( "UPT", "USR_PARTY", "UPT.GLN(+) = SLNK.DISPLAY_COUNTRYCD" );

			append( new QueryableFieldImpl[] {
						new QueryableFieldImpl( STRING, "partyId", "UPT.PARTY_ID", tbl_UPT )
				, new QueryableFieldImpl( STRING, "countryName",
						"(SELECT SB.COUNTRYNAME FROM DPR_COUNTRY SB WHERE SB.COUNTRY_CD = SLNK.DISPLAY_COUNTRYCD)" )
			} );
		}} );


		/***************************************************************************************************
		 *	DPR_SHORTAGE_ELIMINATE_ITEM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "orderKey",				"ORDERKEY",					"DPR_ORDER_KEY",					0, 20 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ORDERDTL_ITEMCODE",			0, 20 )
			, createFD( OP, "itemCodeConfirmed",	"ITEMCD_CNF",				"DPR_ORDERDTL_ITEMCODE_CNF",		0, 20 )
			, createFD( OP, "itemConsumerEANCodeCNF",	"CONS_EAN",				"DPR_ORDERDTL_CONSUMEREAN",			0, 20 )
			, createFD( OP, "qty",					"QTY",						"DPR_PRODUCTREQ_QTY",				INTEGER, 1, 9999 )
			, createFD( OP, "uom",					"UOM",						"DPR_PRODUCTREQ_UOM",				0, 3 )
			, createFD( OP, "status",				"STATUS",					"DPR_PRODUCTREQ_STATUS",			"DPR_SHORTAGE_ELIMINATE_STATUS_", "00,CO,ER")
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",					DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",					DATE )
		};
		putTable( DPR_SHORTAGE_ELIMINATE_ITEM, table = createTable("DPR_SHORTAGE_ELIMINATE_ITEM", "SEI", tfields ) );
		putQueryable( DPR_SHORTAGE_ELIMINATE_ITEM, new QueryableImpl(table) {{
			Joinable tbl_IMT = new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMT.ITEM_CD(+) = SEI.ITEMCD" );
			Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = SEI.ITEMCD AND IMTD.LANGCD(+) = ?", "displayLanguage", tbl_IMT );
			Joinable tbl_UOM = new JoinableImpl( "IMU_Q", "DPR_ITEM_MASTER_UOM", "IMU_Q.ITEMCD = SEI.ITEMCD AND IMU_Q.UOM_CD = SEI.UOM" );
			Joinable tbl_ORD = new JoinableImpl( "ORD", "DPR_ORDER", "SEI.ORDERKEY = ORD.ORDER_KEY(+)" );

			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "partyCode", "ORD.PARTYCD", tbl_ORD )
				, new QueryableFieldImpl( STRING, "soldPartyCode", "ORD.SOLD_PARTYCD", tbl_ORD )
				, new QueryableFieldImpl( STRING, "shipPartyCode", "ORD.SHIP_PARTYCD", tbl_ORD )
				, new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", tbl_DESC )
				, new QueryableFieldImpl( STRING, "distributionChannelCode", "ORD.DIST_CHANNELCD", tbl_ORD )
				, new QueryableFieldImpl( STRING, "divisonCode", "ORD.DIVISIONCD", tbl_ORD )
				, new QueryableFieldImpl( STRING, "organizationCode", "ORD.ORGANIZATIONCD", tbl_ORD )
				, new QueryableFieldImpl( STRING, "countryCode", "ORD.COUNTRYCD", tbl_ORD )
				, new QueryableFieldImpl( INTEGER, "pcQty", "SEI.QTY * IMU_Q.PACKSIZE", tbl_UOM )
				, new QueryableFieldImpl( STRING, "shelfLife", "IMT.SHELFLIFE/365", tbl_IMT )
			} );
		}} );


		/***************************************************************************************************
		 *	DPR_PRODUCT_REQ
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "productReqKey",		"PRODUCTREQ_KEY",			"DPR_PRODUCT_REQ_KEY",				0, 20 )
			, createFD( PM, "orderDate",			"ORDERDATE",				"DPR_ORDER_ORDERDATE",				DATE )
			, createFD( PM, "partyCode",			"SOLD_PARTYCD",				"DPR_ORDER_SOLDPARTYCODE",			0, 15 )
			, createFD( PM, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_ORDER_SHIPPARTYCODE",			0, 15 )
			, createFD( OP, "orderKey",				"ORDERKEY",					"DPR_ORDER_KEY",					0, 20 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_ORDERDTL_ITEMCODE",			0, 20 )
			, createFD( OP, "itemCodeConfirmed",	"ITEMCD_CNF",				"DPR_ORDERDTL_ITEMCODE_CNF",		0, 20 )
			, createFD( OP, "itemConsumerEAN",		"CONS_EAN",					"DPR_ORDERDTL_UPC_CODE",			0, 20 )
			, createFD( MR, "organizationCode",		"ORGANIZATIONCD",			"DPR_ORDER_ORGANIZATIONCODE",		0, 8 )
			, createFD( MR, "distributionChannelCode","DIST_CHANNELCD",			"DPR_ORDER_DISTRIBUTIONCHANNELCODE",	0, 8 )
			, createFD( MR, "divisionCode",			"DIVISIONCD",				"DPR_ORDER_DIVISIONCODE",			0, 8 )
			, createFD( MR, "countryCode",			"COUNTRYCD",				"DPR_ORDER_COUNTRYCODE",			0, 15 )
			, createFD( MD, "expectedDate",			"EXPECTED_DATE",			"DPR_PRODUCT_REQ_EXPECTEDDATE",		DATE )
			, createFD( OP, "qty",					"QTY",						"DPR_PRODUCTREQ_QTY",				INTEGER, 1, 9999 )
			, createFD( OP, "uom",					"UOM",						"DPR_PRODUCTREQ_UOM",					0, 3 )
			, createFD( OP, "description",			"DESCRIPTION",				"DPR_PRODUCTREQ_DESC",				0, 512 )
			, createFD( RD, "status",				"STATUS",					"DPR_PRODUCTREQ_STATUS",			"PUB_STATUS_", "00,01")
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",					DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",					DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",						0, 30 )
		};
		putTable( DPR_PRODUCT_REQ, table = createTable("DPR_PRODUCT_REQ", "PRQ", tfields ) );
		putQueryable( DPR_PRODUCT_REQ, new QueryableImpl(table) {{
			Joinable tbl_PTYS_SLD = new JoinableImpl( "PTYS_SLD", "DPR_PARTY_SALES"
					, "PRQ.SOLD_PARTYCD = PTYS_SLD.PARTYCD AND PRQ.ORGANIZATIONCD = PTYS_SLD.ORGANIZATIONCD AND PRQ.DIST_CHANNELCD = PTYS_SLD.DIST_CHANNELCD"
							+ " AND PRQ.DIVISIONCD = PTYS_SLD.DIVISIONCD" );
			Joinable tbl_PTYS_SHP = new JoinableImpl( "PTYS_SHP", "DPR_PARTY_SALES"
					, "PRQ.SHIP_PARTYCD = PTYS_SHP.PARTYCD AND PRQ.ORGANIZATIONCD = PTYS_SHP.ORGANIZATIONCD AND PRQ.DIST_CHANNELCD = PTYS_SHP.DIST_CHANNELCD"
							+ " AND PRQ.DIVISIONCD = PTYS_SHP.DIVISIONCD" );
			Joinable tbl_IMT = new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMT.ITEM_CD(+) = PRQ.ITEMCD" );
			Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = PRQ.ITEMCD AND IMTD.LANGCD(+) = ?", "displayLanguage" );
			Joinable tbl_UOM = new JoinableImpl( "IMU_Q", "DPR_ITEM_MASTER_UOM", "IMU_Q.ITEMCD = PRQ.ITEMCD AND IMU_Q.UOM_CD = PRQ.UOM" );
			Joinable tbl_ORD = new JoinableImpl( "ORD", "DPR_ORDER", "PRQ.ORDERKEY = ORD.ORDER_KEY(+)" );

			append( makePartyAuthQueryableFields(TYPE_NONE, "PRQ", "SOLD_PARTYCD") );
			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "partyName", "PTYS_SLD.PARTYNAME", tbl_PTYS_SLD )
				, new QueryableFieldImpl( STRING, "officeCode", "PTYS_SLD.OFFICECD", tbl_PTYS_SLD  )
				, new QueryableFieldImpl( STRING, "shipPartyName", "PTYS_SHP.PARTYNAME", tbl_PTYS_SHP )
				, new QueryableFieldImpl( STRING, "deliveryPlant", "PTYS_SHP.DELIVERY_PLANT", tbl_PTYS_SHP )
				, new QueryableFieldImpl( STRING, "shelfLife", "IMT.SHELFLIFE/365", tbl_IMT )
				, new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", tbl_DESC )
				, new QueryableFieldImpl( INTEGER, "pcQty", "PRQ.QTY * IMU_Q.PACKSIZE", tbl_UOM )
				, new QueryableFieldImpl( STRING, "remark", "NVL(ORD.ORDER_NUMBER, PRQ.DESCRIPTION)", tbl_ORD )
				, new QueryableFieldImpl( STRING, "orderNumber", "ORD.ORDER_NUMBER", tbl_ORD )
			} );
			append( Schema.makeItemEANQueryableFields( "IMEAN", "PRQ", true ) );
		}} );


		/***************************************************************************************************
		 *	DPR_STOCK_QUERY_MNG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "countryCode",			"COUNTRYCD",				"DPR_STOCKQUERY_COUNTRYCODE",		0, 15 )
			, createFD( MD, "autoRetry",			"AUTO_RETRY",				"DPR_STOCKQUERY_AUTORETRY",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "inputQty",				"INPUT_QTY",				"DPR_STOCKQUERY_INPUTQTY",			INTEGER, 1, 999999 )
			, createFD( OP, "simulationLineNumber",	"SIMULATION_LINENUMBER",	"DPR_STOCKQUERY_SIMLINENUMBER",		INTEGER )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",					DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",					DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",						0, 30 )
		};
		putTable( DPR_STOCK_QUERY_MNG, table = createTable("DPR_STOCK_QUERY_MNG", "STQM", tfields, "UPGDATE = SYSDATE" ) );


		/***************************************************************************************************
		 *	DPR_STOCK_QUERY_PLANTMAP
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_STOCKQUERY_ORGANIZATIONCODE",	0, 8 )
			, createFD( PM, "distributionChannelCode","DIST_CHANNELCD",			"DPR_STOCKQUERY_DISTCHANNELCODE",	0, 8 )
			, createFD( PM, "divisionCode",			"DIVISIONCD",				"DPR_STOCKQUERY_DIVISIONCODE",		0, 8 )
			, createFD( PM, "plantCode",			"PLANTCD",					"DPR_STOCKQUERY_PLANTCODE",		0, 8 )
			, createFD( MD, "soldPartyCode",		"SOLD_PARTYCD",				"DPR_STOCKQUERY_SOLDPARTYCODE",		0, 15 )
			, createFD( MD, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_STOCKQUERY_SHIPPARTYCODE",		0, 15 )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",					DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",					DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",						0, 30 )
		};
		putTable( DPR_STOCK_QUERY_PLANTMAP, table = createTable("DPR_STOCK_QUERY_PLANTMAP", "STQP", tfields, "UPGDATE = SYSDATE" ) );
		putQueryable( DPR_STOCK_QUERY_PLANTMAP, new QueryableImpl(table) {{
			Joinable tbl_PTYS_SLD = new JoinableImpl( "PTYS_SLD", "DPR_PARTY_SALES"
					, "STQP.SOLD_PARTYCD = PTYS_SLD.PARTYCD AND STQP.ORGANIZATIONCD = PTYS_SLD.ORGANIZATIONCD"
							+ " AND STQP.DIST_CHANNELCD = PTYS_SLD.DIST_CHANNELCD" );
			Joinable tbl_PTYS_SHP = new JoinableImpl( "PTYS_SHP", "DPR_PARTY_SALES"
					, "STQP.SHIP_PARTYCD = PTYS_SHP.PARTYCD AND STQP.ORGANIZATIONCD = PTYS_SHP.ORGANIZATIONCD"
							+ " AND STQP.DIST_CHANNELCD = PTYS_SHP.DIST_CHANNELCD" );

			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "soldPartyName", "PTYS_SLD.PARTYNAME", tbl_PTYS_SLD )
				, new QueryableFieldImpl( STRING, "shipPartyName", "PTYS_SHP.PARTYNAME", tbl_PTYS_SHP )
			} );
		}} );


		/***************************************************************************************************
		 *	DPR_STOCK_QUERY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( MD, "dateValue",			"DATEVALUE",				"DATEVALUE",						DATE )
			, createFD( MD, "queryKey",				"QUERY_KEY",				"DPR_STOCKQUERY_QUERYKEY",			0, 20 )
			, createFD( PM, "simulationKey",		"SIMULATION_KEY",			"DPR_STOCKQUERY_SIMULATIONKEY",		0, 35 )
			, createFD( PM, "organizationCode",		"ORGANIZATIONCD",			"DPR_STOCKQUERY_ORGANIZATIONCODE",	0, 8 )
			, createFD( PM, "distributionChannelCode","DIST_CHANNELCD",			"DPR_STOCKQUERY_DISTCHANNELCODE",	0, 8 )
			, createFD( MD, "divisionCode",			"DIVISIONCD",				"DPR_STOCKQUERY_DIVISIONCODE",		0, 8 )
			, createFD( PM, "soldPartyCode",		"SOLD_PARTYCD",				"DPR_STOCKQUERY_SOLDPARTYCODE",		0, 15 )
			, createFD( PM, "shipPartyCode",		"SHIP_PARTYCD",				"DPR_STOCKQUERY_SHIPPARTYCODE",		0, 15 )
			, createFD( PM, "itemCode",				"ITEMCD",					"DPR_STOCKQUERY_ITEMCODE",			0, 20 )
			, createFD( MD, "countryCode",			"COUNTRYCD",				"DPR_STOCKQUERY_COUNTRYCODE",		0, 15 )
			, createFD( OP, "inputQty",				"INPUT_QTY",				"DPR_STOCKQUERY_INPUTQTY",			INTEGER )
			, createFD( OP, "simulationQty",		"SIMULATION_QTY",			"DPR_STOCKQUERY_SIMULATIONQTY",		INTEGER )
			, createFD( OP, "price",				"PRICE",					"DPR_STOCKQUERY_PRICE",				DOUBLE )
			, createFD( OP, "uom",					"UOM",						"DPR_STOCKQUERY_UOM",				0, 3 )
			, createFD( OP, "simulationUom",		"SIMULATION_UOM",			"DPR_STOCKQUERY_SIMULATIONUOM",		0, 3 )
			, createFD( OP, "message",				"MESSAGE",					"DPR_STOCKQUERY_MESSAGE",			DESC, 0, 512 )
			, createFD( MD, "status",				"STATUS",					"DPR_STOCKQUERY_STATUS",			"DPR_STOCK_QUERY_STATUS_", "00,CO,ER" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",					DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",					"UPDATEDATETIME",					DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",				"UPDATEUSERID",						0, 30 )
		};
		putTable( DPR_STOCK_QUERY, table = createTable("DPR_STOCK_QUERY", "STQ", tfields, "UPGDATE = SYSDATE" ) );
		putQueryable( DPR_STOCK_QUERY, new QueryableImpl(table) {{
			Joinable tbl_PTYS_SLD = new JoinableImpl( "PTYS_SLD", "DPR_PARTY_SALES"
					, "STQ.SOLD_PARTYCD = PTYS_SLD.PARTYCD AND STQ.ORGANIZATIONCD = PTYS_SLD.ORGANIZATIONCD"
							+ " AND STQ.DIST_CHANNELCD = PTYS_SLD.DIST_CHANNELCD" );
			Joinable tbl_PTYS_SHP = new JoinableImpl( "PTYS_SHP", "DPR_PARTY_SALES"
					, "STQ.SHIP_PARTYCD = PTYS_SHP.PARTYCD AND STQ.ORGANIZATIONCD = PTYS_SHP.ORGANIZATIONCD"
							+ " AND STQ.DIST_CHANNELCD = PTYS_SHP.DIST_CHANNELCD" );
			Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
					, "IMTD.ITEMCD(+) = STQ.ITEMCD AND IMTD.LANGCD(+) = ?", "displayLanguage" );
			Joinable tbl_UOM = new JoinableImpl( "IMU_Q", "DPR_ITEM_MASTER_UOM", "IMU_Q.ITEMCD(+) = STQ.ITEMCD AND IMU_Q.UOM_CD(+) = STQ.UOM" );
			Joinable tbl_PJDMS = new JoinableImpl( "PJDMS", "DPR_PARTY_JDMS"
					, "PJDMS.PARTYCD(+) = STQ.SOLD_PARTYCD AND PJDMS.ORGANIZATIONCD(+) = STQ.ORGANIZATIONCD"
					+ " AND PJDMS.DIST_CHANNELCD(+) =  STQ.DIST_CHANNELCD" );

			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "soldPartyName", "PTYS_SLD.PARTYNAME", tbl_PTYS_SLD )
				, new QueryableFieldImpl( STRING, "officeCode", "PTYS_SLD.OFFICECD", tbl_PTYS_SLD  )
				, new QueryableFieldImpl( STRING, "shipPartyName", "PTYS_SHP.PARTYNAME", tbl_PTYS_SHP )
				, new QueryableFieldImpl( STRING, "plantCode", "PTYS_SHP.DELIVERY_PLANT", tbl_PTYS_SHP )
				, new QueryableFieldImpl( DESC, "itemName", "IMTD.ITEMNAME", tbl_DESC )
				, new QueryableFieldImpl( INTEGER, "pcInputQty", "STQ.INPUT_QTY * IMU_Q.PACKSIZE", tbl_UOM )
				, new QueryableFieldImpl( INTEGER, "pcSimulationQty", "STQ.SIMULATION_QTY * IMU_Q.PACKSIZE", tbl_UOM )
				, new QueryableFieldImpl( STRING, "JDMSIndicator", "NVL2(PJDMS.PARTYCD, 'Y', 'N')", tbl_PJDMS )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_UPLOAD_HEADER
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "uploadCode",			"UPLOAD_CD",				"DPR_UPLOAD_CODE",				0, 10 )
			, createFD( RD, "uploadDateTime",		"UPLOAD_DATETIME",			"DPR_UPLOAD_UPLOADDATETIME",	DATE )
			, createFD( MR, "uploadUserId",			"UPLOAD_USERID",			"DPR_UPLOAD_UPLOADUSERID",		0, 30 )
			, createFD( MR, "uploadType",			"UPLOAD_TYPE",				"DPR_UPLOAD_UPLOADTYPE",		"DPR_UPLOADTYPE_", "CMT,CIM,SSL,SKM,INV,SEO,ORD" )
			, createFD( MR, "uploadOption",			"UPLOAD_OPTION",			"DPR_UPLOAD_UPLOADOPTION",		"DPR_UPLOAD_OPTION_", "ADD,REP" )
			, createFD( MR, "countryCode",			"COUNTRYCD",				"DPR_UPLOAD_COUNTRYCODE",		0, 15 )
			, createFD( MR, "organizationCode",		"ORGANIZATIONCD",			"DPR_UPLOAD_ORGANIZATIONCODE",	0, 8 )
			, createFD( MR, "fileName",				"FILENAME",					"DPR_UPLOAD_FILENAME",			DESC, 0, 128 )
			, createFD( OP, "rowCount",				"ROW_CNT",					"DPR_UPLOAD_ROWCOUNT",			INTEGER, 0, 99999999 )
			, createFD( OP, "createCount",			"CREATE_CNT",				"DPR_UPLOAD_CREATECOUNT",		INTEGER, 0, 99999999 )
			, createFD( OP, "insertCount",			"INSERT_CNT",				"DPR_UPLOAD_INSERTCOUNT",		INTEGER, 0, 99999999 )
			, createFD( OP, "updateCount",			"UPDATE_CNT",				"DPR_UPLOAD_UPDATECOUNT",		INTEGER, 0, 99999999 )
			, createFD( OP, "deleteCount",			"DELETE_CNT",				"DPR_UPLOAD_DELETECOUNT",		INTEGER, 0, 99999999 )
			, createFD( OP, "ignoreCount",			"IGNORE_CNT",				"DPR_UPLOAD_IGNORECOUNT",		INTEGER, 0, 99999999 )
			, createFD( OP, "warningCount",			"WARNING_CNT",				"DPR_UPLOAD_WARNINGCOUNT",		INTEGER, 0, 99999999 )
			, createFD( OP, "errorCount",			"ERROR_CNT",				"DPR_UPLOAD_ERRORCOUNT",		INTEGER, 0, 99999999 )
			, createFD( OP, "executeTime",			"EXEC_TIME",				"DPR_UPLOAD_EXECUTETIME",		INTEGER )
			, createFD( OP, "message",				"MESSAGE",					"DPR_UPLOAD_MESSAGE",			DESC, 0, 1024 )
			, createFD( OP, "extraValue1",			"EXTRA_VALUE1",				"DPR_UPLOAD_EXTRAVALUE1",		0, 40 )
			, createFD( OP, "extraValue2",			"EXTRA_VALUE2",				"DPR_UPLOAD_EXTRAVALUE2",		0, 40 )
			, createFD( OP, "extraValue3",			"EXTRA_VALUE3",				"DPR_UPLOAD_EXTRAVALUE3",		0, 40 )
			, createFD( MD, "status",				"STATUS",					"STATUS",						"DPR_UPLOAD_STATUS_", "CO,ER,IG,RD" )
		};
		putTable( DPR_UPLOAD_HEADER, table = createTable("DPR_UPLOAD_HEADER", "UPDH", tfields ) );
		putQueryable( DPR_UPLOAD_HEADER, new QueryableImpl(table) {{
			Joinable tbl_USR = new JoinableImpl( "USR", "USR_USER", "USR.UNIQID = UPDH.UPLOAD_USERID" );

			append( new QueryableFieldImpl[] {
						new QueryableFieldImpl( STRING, "userId", "USR.USER_ID", tbl_USR )
				, new QueryableFieldImpl( STRING, "userName", "USR.USER_NAME", tbl_USR )
			} );
		}} );


		/***************************************************************************************************
			*	DPR_UPLOAD_CIM
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( RD, "uploadCode",			"UPLOADCD",					"DPR_UPLOADCIM_UPLOADCODE",		0, 10 )
			, createFD( RD, "distributorId",		"DISTRIBUTOR_ID",			"DPR_UPLOADCIM_DISTRIBUTORID",	0, 30 )
			, createFD( RD, "distributorCode",		"DISTRIBUTOR_CD",			"DPR_UPLOADCIM_DISTRIBUTORCODE",0, 15 )
			, createFD( RD, "customerCategory",		"CUSTOMER_CATEGORY",		"DPR_UPLOADCIM_CUSTOMERCATEGORY",0, 40 )
			, createFD( RD, "salesType",			"SALES_TYPE",				"DPR_UPLOADCIM_SALESTYPE",		0, 40 )
			, createFD( RD, "currencyCode",			"CURRENCYCD",				"DPR_UPLOADCIM_CURRENCYCODE",	0, 3 )
			, createFD( RD, "globalParentCode",		"GLOBAL_PARENTCD",			"DPR_UPLOADCIM_GLOBALPARENTCODE",0, 20 )
			, createFD( RD, "globalGrandparentCode","GLOBAL_GRANDPARENTCD",		"DPR_UPLOADCIM_GLOBALGRANDPARENTCODE",0, 20 )
			, createFD( RD, "calendarType",			"CALENDAR_TYPE",			"DPR_UPLOADCIM_CALENDARTYPE",	0, 40 )
			, createFD( RD, "lineNumber",			"LINE_NUM",					"DPR_UPLOADCIM_LINENUMBER",		INTEGER, 0, 99999999 )
			, createFD( RD, "message",				"MESSAGE",					"DPR_UPLOADCIM_MESSAGE",		DESC, 0, 1024 )
			, createFD( RD, "status",				"STATUS",					"DPR_UPLOADCIM_STATUS",			"DPR_UPLOAD_STATUS_", "CO,ER,IG,RD" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
		};
		putTable( DPR_UPLOAD_CIM, table = createTable("DPR_UPLOAD_CIM", "UCIM", tfields ) );


		/***************************************************************************************************
			*	DPR_UPLOAD_CMT
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( RD, "uploadCode",			"UPLOADCD",					"DPR_UPLOADCMT_UPLOADCODE",		0, 10 )
			, createFD( RD, "distributorId",		"DISTRIBUTOR_ID",			"DPR_UPLOADCMT_DISTRIBUTORID",	0, 15 )
			, createFD( RD, "customerCode",			"CUSTOMER_CD",				"DPR_UPLOADCMT_CUSTOMERCODE",	0, 15 )
			, createFD( RD, "customerName1",		"CUSTOMER_NAME1",			"DPR_UPLOADCMT_CUSTOMERNAME1",	DESC, 0, 200 )
			, createFD( RD, "customerName2",		"CUSTOMER_NAME2",			"DPR_UPLOADCMT_CUSTOMERNAME2",	DESC, 0, 200 )
			, createFD( RD, "customerGroup",		"CUSTOMER_GROUP",			"DPR_UPLOADCMT_CUSTOMERGROUP",	0, 8 )
			, createFD( RD, "customerType",			"CUSTOMER_TYPE",			"DPR_UPLOADCMT_CUSTOMERTYPE",	0, 3 )
			, createFD( RD, "address1",				"ADDRESS1",					"DPR_UPLOADCMT_ADDRESS1",		DESC, 0, 500 )
			, createFD( RD, "address2",				"ADDRESS2",					"DPR_UPLOADCMT_ADDRESS2",		DESC, 0, 500 )
			, createFD( RD, "postalCode",			"POSTAL_CD",				"DPR_UPLOADCMT_POSTALCODE",		0, 8 )
			, createFD( RD, "contactPerson",		"CONTACT_PERSON",			"DPR_UPLOADCMT_CONTACTPERSON",	0, 40 )
			, createFD( RD, "phoneNumber",			"PHONE_NUMBER",				"DPR_UPLOADCMT_PHONENUMBER",	0, 50 )
			, createFD( RD, "regionCode",			"REGION_CD",				"DPR_UPLOADCMT_REGIONCODE",		0, 8 )
			, createFD( RD, "provinceCity",			"PROVINCE_CITY",			"DPR_UPLOADCMT_PROVINCECITY",	0, 20 )
			, createFD( RD, "activeId",				"ACTIVE_ID",				"DPR_UPLOADCMT_ACTIVEID",		0, 2 )
			, createFD( RD, "customField1",			"CUSTOM_FIELD1",			"DPR_UPLOADCMT_CUSTOMFIELD1",	0, 20 )
			, createFD( RD, "customField2",			"CUSTOM_FIELD2",			"DPR_UPLOADCMT_CUSTOMFIELD2",	0, 20 )
			, createFD( RD, "lineNumber",			"LINE_NUM",					"DPR_UPLOADCMT_LINENUMBER",		INTEGER, 0, 99999999 )
			, createFD( RD, "message",				"MESSAGE",					"DPR_UPLOADCMT_MESSAGE",		DESC, 0, 1024 )
			, createFD( RD, "status",				"STATUS",					"DPR_UPLOADCMT_STATUS",			"DPR_UPLOAD_STATUS_", "CO,ER,IG,RD" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
		};
		putTable( DPR_UPLOAD_CMT, table = createTable("DPR_UPLOAD_CMT", "UCMT", tfields ) );


		/***************************************************************************************************
			*	DPR_UPLOAD_SSL
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( RD, "uploadCode",			"UPLOADCD",					"DPR_UPLOADSSL_UPLOADCODE",		0, 10 )
			, createFD( RD, "itemCode",				"ITEMCD",					"DPR_UPLOADSSL_ITEMCODE",		0, 20 )
			, createFD( RD, "officeCode",			"OFFICECD",					"DPR_UPLOADSSL_OFFICECODE",		0, 8 )
			, createFD( RD, "groupCode",			"GROUPCD",					"DPR_UPLOADSSL_GROUPCODE",		0, 8 )
			, createFD( RD, "districtCode",			"DISTRICTCD",				"DPR_UPLOADSSL_DISTRICTCODE",	0, 8 )
			, createFD( RD, "distributorCode",		"DISTRIBUTOR_CD",			"DPR_UPLOADSSL_DISTRIBUTORCODE",0, 15 )
			, createFD( RD, "startDate",			"START_DATE",				"DPR_UPLOADSSL_STARTDATE",		DATE )
			, createFD( RD, "endDate",				"END_DATE",					"DPR_UPLOADSSL_ENDDATE",		DATE )
			, createFD( RD, "promotionInd",			"PROMOTION_IND",			"DPR_UPLOADSSL_PROMOTIONIND",	"PUB_WHETHER_", "Y,N" )
			, createFD( RD, "newItemInd",			"NEWITEM_IND",				"DPR_UPLOADSSL_NEWITEM",		"PUB_WHETHER_", "Y,N" )
			, createFD( RD, "lineNumber",			"LINE_NUM",					"DPR_UPLOADSSL_LINENUMBER",		INTEGER, 0, 99999999 )
			, createFD( RD, "message",				"MESSAGE",					"DPR_UPLOADSSL_MESSAGE",		DESC, 0, 1024 )
			, createFD( RD, "status",				"STATUS",					"DPR_UPLOADSSL_STATUS",			"DPR_UPLOAD_STATUS_", "CO,ER,IG,RD" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
		};
		putTable( DPR_UPLOAD_SSL, table = createTable("DPR_UPLOAD_SSL", "USSL", tfields ) );


		/***************************************************************************************************
			*	DPR_UPLOAD_SKM
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( RD, "uploadCode",			"UPLOADCD",					"DPR_UPLOADSKM_UPLOADCODE",		0, 10 )
			, createFD( RD, "distributorCode",		"DISTRIBUTOR_CD",			"DPR_UPLOADSKM_DISTRIBUTORCODE",0, 15 )
			, createFD( RD, "itemCode",				"ITEMCD",					"DPR_UPLOADSKM_ITEMCODE",		0, 20 )
			, createFD( RD, "distributorItemCode",	"DIST_ITEMCD",				"DPR_UPLOADSKM_DISTRIBUTORITEMCODE",0, 20 )
			, createFD( RD, "distributorDescription","DIST_DESCRIPTION",		"DPR_UPLOADSKM_DISTRIBUTORDESCRIPTION",DESC, 0, 200 )
			, createFD( RD, "itemDescription",		"ITEM_DESCRIPTION",			"DPR_UPLOADSKM_ITEMDESCRIPTION",DESC, 0, 200 )
			, createFD( RD, "lineNumber",			"LINE_NUM",					"DPR_UPLOADSKM_LINENUMBER",		INTEGER, 0, 99999999 )
			, createFD( RD, "message",				"MESSAGE",					"DPR_UPLOADSKM_MESSAGE",		DESC, 0, 1024 )
			, createFD( RD, "status",				"STATUS",					"DPR_UPLOADSKM_STATUS",			"DPR_UPLOAD_STATUS_", "CO,ER,IG,RD" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
		};
		putTable( DPR_UPLOAD_SKM, table = createTable("DPR_UPLOAD_SKM", "USKM", tfields ) );


		/***************************************************************************************************
			*	DPR_UPLOAD_INV
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( RD, "uploadCode",			"UPLOADCD",					"DPR_UPLOADINV_UPLOADCODE",		0, 10 )
			, createFD( RD, "distributorId",		"DISTRIBUTOR_ID",			"DPR_UPLOADINV_DISTRIBUTORID",	0, 15 )
			, createFD( RD, "invDate",				"INV_DATE",					"DPR_UPLOADINV_INVDATE",		DATE )
			, createFD( RD, "itemCode",				"ITEMCD",					"DPR_UPLOADINV_ITEMCODE",		0, 20 )
			, createFD( RD, "gtin",					"GTIN",						"DPR_UPLOADINV_GTIN",			0, 14 )
			, createFD( RD, "distributorWarehouse",	"DIST_WAREHOUSE",			"DPR_UPLOADINV_DISTRIBUTORWAREHOUSE",0, 40 )
			, createFD( RD, "description",			"DESCRIPTION",				"DPR_UPLOADINV_DESCRIPTION",	0, 200 )
			, createFD( RD, "unitPrice",			"UNIT_PRICE",				"DPR_UPLOADINV_UNITPRICE",		DOUBLE )
			, createFD( RD, "uom",					"UOM",						"DPR_UPLOADINV_UOM",			0, 3 )
			, createFD( RD, "stockQty",				"STOCK_QTY",				"DPR_UPLOADINV_STOCKQTY",		DOUBLE )
			, createFD( RD, "onOrderQty",			"ONORDER_QTY",				"DPR_UPLOADINV_ONORDERQTY",		DOUBLE )
			, createFD( RD, "committedQty",			"COMMITTED_QTY",			"DPR_UPLOADINV_COMMITTEDQTY",	DOUBLE )
			, createFD( RD, "totalAmount",			"TOTAL_AMOUNT",				"DPR_UPLOADINV_TOTALAMOUNT",	DOUBLE )
			, createFD( RD, "customField1",			"CUSTOM_FIELD1",			"DPR_UPLOADINV_CUSTOMFIELD1",	0, 200 )
			, createFD( RD, "customField2",			"CUSTOM_FIELD2",			"DPR_UPLOADINV_CUSTOMFIELD2",	0, 200 )
			, createFD( RD, "lineNumber",			"LINE_NUM",					"DPR_UPLOADINV_LINENUMBER",		0, 99999999 )
			, createFD( RD, "message",				"MESSAGE",					"DPR_UPLOADINV_MESSAGE",		DESC, 0, 1024 )
			, createFD( RD, "status",				"STATUS",					"DPR_UPLOADINV_STATUS",			"DPR_UPLOAD_STATUS_", "CO,ER,IG,RD" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
		};
		putTable( DPR_UPLOAD_INV, table = createTable("DPR_UPLOAD_INV", "UINV", tfields ) );


		/***************************************************************************************************
			*	DPR_UPLOAD_SEO
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( RD, "uploadCode",			"UPLOADCD",					"DPR_UPLOADSEO_UPLOADCODE",		0, 10 )
			, createFD( RD, "distributorId",		"DISTRIBUTOR_ID",			"DPR_UPLOADSEO_DISTRIBUTORID",	0, 15 )
			, createFD( RD, "itemCode",				"ITEMCD",					"DPR_UPLOADSEO_ITEMCODE",		0, 20 )
			, createFD( RD, "sellOutCode",			"SELLOUT_CD",				"DPR_UPLOADSEO_SELLOUTCODE",	0, 40 )
			, createFD( RD, "sellOutDate",			"SELLOUT_DATE",				"DPR_UPLOADSEO_SELLOUTDATE",	DATE )
			, createFD( RD, "sellOutType",			"SELLOUT_TYPE",				"DPR_UPLOADSEO_SELLOUTTYPE",	0, 2 )
			, createFD( RD, "customerCode",			"CUSTOMER_CD",				"DPR_UPLOADSEO_CUSTOMERCODE",	0, 15 )
			, createFD( RD, "gtin",					"GTIN",						"DPR_UPLOADSEO_GTIN",			0, 14 )
			, createFD( RD, "distributorWarehouse",	"DIST_WAREHOUSE",			"DPR_UPLOADSEO_DISTRIBUTORWAREHOUSE",0, 40 )
			, createFD( RD, "description",			"DESCRIPTION",				"DPR_UPLOADSEO_DESCRIPTION",	DESC, 0, 200 )
			, createFD( RD, "salesEmployee",		"SALES_EMPLOYEE",			"DPR_UPLOADSEO_SALESEMPLOYEE",	0, 50 )
			, createFD( RD, "price",				"PRICE",					"DPR_UPLOADSEO_PRICE",			DOUBLE )
			, createFD( RD, "sellOutQty",			"SELLOUT_QTY",				"DPR_UPLOADSEO_SELLOUTQTY",		DOUBLE )
			, createFD( RD, "sellOutUom",			"SELLOUT_UOM",				"DPR_UPLOADSEO_SELLOUTUOM",		0, 3 )
			, createFD( RD, "totalPrice",			"TOTAL_PRICE",				"DPR_UPLOADSEO_TOTALPRICE",		DOUBLE )
			, createFD( RD, "discountPrice",		"DISCOUNT_PRICE",			"DPR_UPLOADSEO_DISCOUNTPRICE",	DOUBLE )
			, createFD( RD, "totalDiscountPrice",	"TOTAL_DISCOUNT_PRICE",		"DPR_UPLOADSEO_TOTALDISCOUNTPRICE",DOUBLE )
			, createFD( RD, "bottomlineDiscountPrice","BOTTOMLINE_DISCOUNT_PRICE","DPR_UPLOADSEO_BOTTOMLINEDISCOUNTPRICE",DOUBLE )
			, createFD( RD, "totalAmount",			"TOTAL_AMOUNT",				"DPR_UPLOADSEO_TOTALAMOUNT",	DOUBLE )
			, createFD( RD, "totalAmountTax",		"TOTAL_AMOUNT_TAX",			"DPR_UPLOADSEO_TOTALAMOUNTTAX",	DOUBLE )
			, createFD( RD, "customField1",			"CUSTOM_FIELD1",			"DPR_UPLOADSEO_CUSTOMFIELD1",	0, 200 )
			, createFD( RD, "customField2",			"CUSTOM_FIELD2",			"DPR_UPLOADSEO_CUSTOMFIELD2",	0, 200 )
			, createFD( RD, "customField3",			"CUSTOM_FIELD3",			"DPR_UPLOADSEO_CUSTOMFIELD3",	0, 200 )
			, createFD( RD, "lineNumber",			"LINE_NUM",					"DPR_UPLOADSEO_LINENUMBER",		0, 99999999 )
			, createFD( RD, "message",				"MESSAGE",					"DPR_UPLOADSEO_MESSAGE",		DESC, 0, 1024 )
			, createFD( RD, "status",				"STATUS",					"DPR_UPLOADSEO_STATUS",			"DPR_UPLOAD_STATUS_", "CO,ER,IG,RD" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
		};
		putTable( DPR_UPLOAD_SEO, table = createTable("DPR_UPLOAD_SEO", "USEO", tfields ) );


		/***************************************************************************************************
			*	DPR_UPLOAD_ORD
		***************************************************************************************************/
		tfields = new Table.Field[] {
					createFD( RD, "uploadCode",			"UPLOADCD",					"DPR_UPLOADORD_UPLOADCODE",		0, 10 )
			, createFD( RD, "orderKey",				"ORDER_KEY",				"DPR_UPLOADORD_ORDERKEY",		0, 20 )
			, createFD( RD, "itemCode",				"ITEMCD",					"DPR_UPLOADORD_ITEMCODE",		0, 20 )
			, createFD( RD, "orderLineNumber",		"ORDER_LINE_NO",			"DPR_UPLOADORD_ORDER_LINE_NO",	INTEGER )
			, createFD( RD, "uom",					"UOM",						"DPR_UPLOADORD_UOM",			0, 3 )
			, createFD( RD, "orderQty",				"ORDERQTY",					"DPR_UPLOADORD_ORDERQTY",		DOUBLE )
			, createFD( RD, "message",				"MESSAGE",					"DPR_UPLOADORD_MESSAGE",		DESC, 0, 1024 )
			, createFD( RD, "lineNumber",			"LINE_NUM",					"DPR_UPLOADORD_LINENUMBER",		0, 99999999 )
			, createFD( RD, "status",				"STATUS",					"DPR_UPLOADORD_STATUS",			"DPR_UPLOAD_STATUS_", "CO,ER,IG,RD" )
			, createFD( RD, "createDateTime",		"REGDATE",					"CREATEDATETIME",				DATE )
		};
		putTable( DPR_UPLOAD_ORD, table = createTable("DPR_UPLOAD_ORD", "UORD", tfields ) );

	}

	public static Queryable findQueryable( String key ) {
		return schema.getQueryable( key );
	}

	public static QueryFactory findQueryFactory( String key ) {
		Queryable queryable = schema.getQueryable( key );
		return ( queryable == null ? null : new QueryFactory(queryable) );
	}

	public static Table findTable( String key ) {
		return schema.getTable( key );
	}

	public static QueryableField[] makeMasterNames( String baseAlias ) {
		return makeMasterNames(baseAlias, (Joinable)null);
	}

	public static QueryableField[] makeMasterNames( String baseAlias, Joinable baseJoin, String... fieldKeys ) {
		List<QueryableField> fields = new ArrayList<QueryableField>();

		boolean putAllKeys = (fieldKeys == null || fieldKeys.length == 0 );
		if( putAllKeys ) fieldKeys = MASTER_NAME_FIELD_KEYS;
		for( String fieldKey : fieldKeys ) {
			if( putAllKeys || "itemName".equals(fieldKey) ) {
				fields.add( new QueryableFieldImplBK( DESC, "itemName"
					, "(SELECT SB.ITEMNAME FROM DPR_ITEM_MASTER_DESC SB WHERE SB.ITEMCD = "+baseAlias+".ITEMCD AND SB.LANGCD = NVL(?, 'en'))"
					, "displayLanguage", "DPR_ITEM_MASTER_NAME", baseJoin));
			}
			if( putAllKeys || "organizationName".equals(fieldKey) ) {
				fields.add( new QueryableFieldImplBK( DESC, "organizationName"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = "+baseAlias+".ORGANIZATIONCD"
						+ " AND SB.MASTER_TYPE = 'SO' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_SALES_ORGANIZATION_NAME", baseJoin ));
			}
			if( putAllKeys || "distributionChannelName".equals(fieldKey) ) {
				fields.add( new QueryableFieldImplBK( DESC, "distributionChannelName"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = "+baseAlias+".DIST_CHANNELCD"
						+ " AND SB.MASTER_TYPE = 'DC' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DISTRIBUTIONCHANNEL_NAME", baseJoin ));
			}
			if( putAllKeys || "officeName".equals(fieldKey) ) {
				fields.add( new QueryableFieldImplBK( DESC, "officeName"
					, "NVL( (SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = "+baseAlias+".OFFICECD"
								+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = NVL(?, 'en'))"
						+ ", (SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = "+baseAlias+".OFFICECD"
								+ " AND SB.MASTER_TYPE = 'SF' AND SB.LANGCD = 'en') )", "displayLanguage", "DPR_SALES_OFFICE_NAME", baseJoin ));
			}
			if( putAllKeys || "groupName".equals(fieldKey) ) {
				fields.add( new QueryableFieldImplBK( DESC, "groupName"
					, "NVL( (SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = "+baseAlias+".GROUPCD"
								+ " AND SB.MASTER_TYPE = 'SG' AND SB.LANGCD = NVL(?, 'en'))"
						+ ", (SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = "+baseAlias+".GROUPCD"
								+ " AND SB.MASTER_TYPE = 'SG' AND SB.LANGCD = 'en') )", "displayLanguage", "DPR_SALES_GROUP_NAME", baseJoin ));
			}

			if( "divisionName".equals(fieldKey) ) {
				fields.add( new QueryableFieldImplBK( DESC, "divisionName"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = "+baseAlias+".DIVISIONCD"
						+ " AND SB.MASTER_TYPE = 'DI' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", "DPR_DIVISION_NAME", baseJoin ));
			}
		}

		return fields.toArray(new QueryableField[0]);
	}

	public static QueryableField[] makeCountryAuthQueryableFields( String type, String alias ) {
		String conditionQuery;
		if( TYPE_BASE.equals(type) )
			conditionQuery = "CAUT.COUNTRYCD(+) = "+ alias +".COUNTRY_CD AND CAUT.UNIQID(+) = ?";
		else
			conditionQuery = "CAUT.COUNTRYCD(+) = "+ alias +".COUNTRYCD AND CAUT.UNIQID(+) = ?";

		Joinable tbl_CAUT = new JoinableImplBK( "CAUT", "DPR_COUNTRY_AUTH", conditionQuery, new String[] { "authUniqId" } );

		QueryBufferValid validAuth = new QueryBufferValid.Condition( "authUniqId" );
		QueryableField[] qfields = new ConditionalQueryableField[] {
					new ConditionalQueryableField( validAuth, createQFD(T, "authCountryValue", "NVL2(CAUT.AUTHVALUE, 'Y', 'N')", "DPR_COUNTRYAUTH_AUTHVALUE", tbl_CAUT) )
		};

		return qfields;
	}

	public static QueryableField[] makePartyAuthQueryableFields( String type, String alias ) {
		return makePartyAuthQueryableFields( type, alias, "PARTYCD" );
	}

	public static QueryableField[] makePartyAuthQueryableFields( String type, String alias, String query ) {
		String conditionQuery;
		if( TYPE_BASE.equals(type) )
			conditionQuery = "PAUT.PARTYCD(+) = "+ alias +".PARTY_CD";
		else
			conditionQuery = "PAUT.PARTYCD(+) = "+ alias +"."+ query;

		Joinable tbl_PAUT = new JoinableImplBK( "PAUT", "DPR_PARTY_AUTH"
					, conditionQuery + " AND PAUT.ORGANIZATIONCD(+) = "+ alias +".ORGANIZATIONCD "
			+ "AND PAUT.DIVISIONCD(+) = "+ alias + ".DIVISIONCD AND PAUT.DIST_CHANNELCD(+) = "+ alias +".DIST_CHANNELCD "
			+ "AND PAUT.UNIQID(+) = ?", new String[] { "authUniqId" } );

		QueryBufferValid validAuth = new QueryBufferValid.Condition( "authUniqId" );
		QueryableField[] qfields = new ConditionalQueryableField[] {
					new ConditionalQueryableField( validAuth, createQFD(T, "authPartyValue", "NVL2(PAUT.AUTHVALUE, 'Y', 'N')", "DPR_PARTYAUTH_AUTHVALUE", tbl_PAUT) )
		};

		return qfields;
	}

	public static QueryableField[] makeMaterialGroupFields( String alias, Joinable joinable, String... fieldKeys ) {
		List<QueryableField> fields = new ArrayList<QueryableField>();

		boolean putAllKeys = (fieldKeys == null || fieldKeys.length == 0);
		if( putAllKeys ) fieldKeys = MASTER_NAME_FIELD_KEYS;
		for( String fieldKey : fieldKeys ) {
			if( putAllKeys || "baseProductName".equals(fieldKey) ) {
				fields.add(new QueryableFieldImplBK( STRING, "baseProductName"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = " + alias + ".BASEPRODUCTCD"
						+ " AND SB.MASTER_TYPE = 'BP' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", joinable ));
			}
			if( putAllKeys || "megaBrandName".equals(fieldKey) ) {
				fields.add(new QueryableFieldImplBK( STRING, "megaBrandName"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = " + alias + ".MEGABRANDCD"
						+ " AND SB.MASTER_TYPE = 'MB' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", joinable ));
			}
			if( putAllKeys || "brandName".equals(fieldKey) ) {
				 fields.add(new QueryableFieldImplBK( STRING, "brandName"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = " + alias + ".BRANDCD"
						+ " AND SB.MASTER_TYPE = 'BR' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", joinable ));
			}
			if( putAllKeys || "variantName".equals(fieldKey) ) {
				fields.add(new QueryableFieldImplBK( STRING, "variantName"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = " + alias + ".VARIANTCD"
						+ " AND SB.MASTER_TYPE = 'VA' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", joinable ));
			}
			if( putAllKeys || "putupName".equals(fieldKey) ) {
				fields.add(new QueryableFieldImplBK( STRING, "putupName"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = " + alias + ".PUTUPCD"
						+ " AND SB.MASTER_TYPE = 'PU' AND SB.LANGCD = NVL(?, 'en'))", "displayLanguage", joinable ));
			}
		}

		return fields.toArray(new QueryableField[0]);
	}

	public static String[] masterDescOrgBKKeys = { "displayLanguage", "masterOrganizationCode", "displayLanguage" };

	public static String masterDescOrgQuery =
				"(SELECT MSTD.MASTER_TYPE, MSTD.MASTER_CD, NVL(MSTDO.LANGCD,MSTD.LANGCD) LANGCD"
				+ ", NVL(MSTDO.MASTER_NAME, MSTD.MASTER_NAME) MASTER_NAME"
				+ ", NVL(MSTDO.MASTER_DESC, MSTD.MASTER_DESC) MASTER_DESC"
				+ " FROM DPR_MASTER_DESC MSTD, DPR_MASTER_DESC_ORG MSTDO"
				+ " WHERE MSTDO.MASTER_CD(+) = MSTD.MASTER_CD"
				+ " AND MSTDO.MASTER_TYPE(+) = MSTD.MASTER_TYPE"
				+ " AND MSTDO.LANGCD(+) = ?"
				+ " AND MSTDO.ORGANIZATIONCD(+) = ?"
				+ " AND MSTD.LANGCD = 'en'"
				+ ")";

	public static QueryableField[] makeMaterialHierarchyFieldBKs( String alias, Joinable joinable, String[] masterDescOrgBKKeys ) {
		String hierarchyQuery = alias + ".PCATECD";

		QueryableField[] fields = new QueryableField[] {
					new QueryableFieldImplBK( STRING, "productHR1Name"
					, "(SELECT MASTER_NAME FROM "+masterDescOrgQuery+" SB WHERE SB.MASTER_CD = SUBSTR("+ hierarchyQuery +", 1, 1)"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ?)", masterDescOrgBKKeys, joinable )
			, new QueryableFieldImplBK( STRING, "productHR2Name"
					, "(SELECT MASTER_NAME FROM "+masterDescOrgQuery+" SB WHERE SB.MASTER_CD ="
						+ " SUBSTR("+ hierarchyQuery +", 1, DECODE(LENGTH("+ hierarchyQuery +"), 1, NULL, 2))"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ?)", masterDescOrgBKKeys, joinable )
			, new QueryableFieldImplBK( STRING, "productHR3Name"
					, "(SELECT MASTER_NAME FROM "+masterDescOrgQuery+" SB WHERE SB.MASTER_CD ="
						+ " SUBSTR("+ hierarchyQuery +", 1, DECODE(LENGTH("+ hierarchyQuery +"), 1, NULL, 2, NULL, 6))"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ?)", masterDescOrgBKKeys, joinable )
			, new QueryableFieldImplBK( STRING, "productHR4Name"
					, "(SELECT MASTER_NAME FROM "+masterDescOrgQuery+" SB WHERE SB.MASTER_CD ="
						+ " SUBSTR("+ hierarchyQuery +", 1, DECODE(LENGTH("+ hierarchyQuery +"), 10, 10, 14, 10, 18, 10, NULL))"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ?)", masterDescOrgBKKeys, joinable )
			, new QueryableFieldImplBK( STRING, "productHR5Name"
					, "(SELECT MASTER_NAME FROM "+masterDescOrgQuery+" SB WHERE SB.MASTER_CD ="
						+ " SUBSTR("+ hierarchyQuery +", 1, DECODE(LENGTH("+ hierarchyQuery +"), 14, 14, 18, 14, NULL ))"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ?)", masterDescOrgBKKeys, joinable )
			, new QueryableFieldImplBK( STRING, "productHR6Name"
					, "(SELECT MASTER_NAME FROM "+masterDescOrgQuery+" SB WHERE SB.MASTER_CD ="
						+ " SUBSTR("+ hierarchyQuery +", 1, DECODE(LENGTH("+ hierarchyQuery +"), 18, 18, NULL))"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ?)", masterDescOrgBKKeys, joinable )
			, new QueryableFieldImplBK( STRING, "productHRFullName"
					, "(SELECT MASTER_NAME FROM "+masterDescOrgQuery+" SB WHERE SB.MASTER_CD = "+ hierarchyQuery
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ?)", masterDescOrgBKKeys, joinable )

		};

		return fields;
	}


	public static QueryableField[] makeMaterialHierarchyFields( String alias, Joinable joinable ) {
		String hierarchyQuery = alias + ".PCATECD";

		QueryableField[] fields = new QueryableField[] {
					new QueryableFieldImpl( STRING, "productHR1Name"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = SUBSTR("+ hierarchyQuery +", 1, 1)"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = 'en')", joinable )
			, new QueryableFieldImpl( STRING, "productHR2Name"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD ="
						+ " SUBSTR("+ hierarchyQuery +", 1, DECODE(LENGTH("+ hierarchyQuery +"), 1, NULL, 2))"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = 'en')", joinable )
			, new QueryableFieldImpl( STRING, "productHR3Name"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD ="
						+ " SUBSTR("+ hierarchyQuery +", 1, DECODE(LENGTH("+ hierarchyQuery +"), 1, NULL, 2, NULL, 6))"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = 'en')", joinable )
			, new QueryableFieldImpl( STRING, "productHR4Name"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD ="
						+ " SUBSTR("+ hierarchyQuery +", 1, DECODE(LENGTH("+ hierarchyQuery +"), 10, 10, 14, 10, 18, 10, NULL))"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = 'en')", joinable )
			, new QueryableFieldImpl( STRING, "productHR5Name"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD ="
						+ " SUBSTR("+ hierarchyQuery +", 1, DECODE(LENGTH("+ hierarchyQuery +"), 14, 14, 18, 14, NULL ))"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = 'en')", joinable )
			, new QueryableFieldImpl( STRING, "productHR6Name"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD ="
						+ " SUBSTR("+ hierarchyQuery +", 1, DECODE(LENGTH("+ hierarchyQuery +"), 18, 18, NULL))"
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = 'en')", joinable )
			, new QueryableFieldImpl( STRING, "productHRFullName"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_CD = "+ hierarchyQuery
						+ " AND SB.MASTER_TYPE = 'PC' AND SB.LANGCD = 'en')", joinable )
		};

		return fields;
	}

	public static java.util.Set<String> getMessageSet() {
		return schema.getMessageKey();
	}
//@formatter:on

	public static QueryableField[] makeRegularItemQueryableFields( String baseTable, String innerTableAlias, String joinBaseTableAlias ) {
		JoinableImpl joinable = getRegularItemJoinable(baseTable, innerTableAlias, joinBaseTableAlias);
		QueryableField[] qfields = new QueryableField[] {
				new QueryableFieldImpl(Schema.INTEGER, "regularItemRank", innerTableAlias + ".REGULAR_ITEM_RANK", joinable),
				new QueryableFieldImpl(Schema.INTEGER, "simItemCount", innerTableAlias + ".SIM_ITEM_CNT", joinable),
		};
		return qfields;
	}

	public static JoinableImpl getRegularItemJoinable( String baseTable, String innerTableAlias, String joinBaseTableAlias ) {
		QueryBuffer inner_querybuf = getRegularItemQueryBuffer(null);

		StringBuffer conditionQuery = new StringBuffer();

		conditionQuery.append(innerTableAlias + ".ORGANIZATIONCD(+) = " + joinBaseTableAlias + ".ORGANIZATIONCD");
		conditionQuery.append(" AND ");
		conditionQuery.append(innerTableAlias + ".DIST_CHANNELCD(+) = " + joinBaseTableAlias + ".DIST_CHANNELCD");
		conditionQuery.append(" AND ");
		conditionQuery.append(innerTableAlias + ".PARTYCD(+) = " + joinBaseTableAlias + ".PARTYCD");

		if( vwDPR_ORDER_ITEM.equals(baseTable) ) {
			conditionQuery.append(" AND ");
			conditionQuery.append(innerTableAlias + ".SHIP_PARTYCD(+) = " + joinBaseTableAlias + ".SHIP_PARTYCD");
		}

		conditionQuery.append(" AND ");
		conditionQuery.append(innerTableAlias + ".ITEMCD(+) = " + joinBaseTableAlias + ".ITEMCD");

		JoinableImpl tbl_SIMHSTC = new JoinableImpl(innerTableAlias, "(" + inner_querybuf.toString() + ")", conditionQuery.toString());
		return tbl_SIMHSTC;
	}

	public static ConditionQueryBuffer getRegularItemQueryBuffer( Map<String, Object> conditionMap ) {
		return getRegularItemQueryBuffer(conditionMap, null);
	}

	public static ConditionQueryBuffer getRegularItemQueryBuffer( Map<String, Object> conditionMap, String[] optionalKeys ) {
		ConditionQueryBuffer qb_base = new ConditionQueryBuffer(conditionMap);

		com.irt.sql.QueryBuffer inner_querybuf = new com.irt.sql.QueryBuffer();
		inner_querybuf.appendTableWithAlias("DPR_SIMULATION_HIST", "SIMHST");
		inner_querybuf.appendDataWithAlias("COUNT(SIMHST.ITEMCD)", "ITEM_CNT");

		inner_querybuf.appendData("SIMHST.ORGANIZATIONCD");
		inner_querybuf.appendData("SIMHST.DIST_CHANNELCD");
		inner_querybuf.appendData("SIMHST.PARTYCD");
		inner_querybuf.appendData("SIMHST.SHIP_PARTYCD");
		inner_querybuf.appendData("SIMHST.ITEMCD");

		// why SYSDATE+1 : if SIM_OUT_DATETIME is different timezone( in QA jvm )
		// inner_querybuf.appendCondition("SIMHST.SIM_OUT_DATETIME BETWEEN (SYSDATE - pkSYSStandard.fGetSystemEnvNum('DPR', 'SimHist;SimHistUseDays',
		// 90)) AND SYSDATE+1");
		// CAUSION: do not use side effect function( read table )
		int simHistUseDays = 90;
		if( RBMSystem.initialized() ) {
			simHistUseDays = RBMSystem.getSystemEnvInt("DPR", "SimHist;SimHistUseDays", 90);
		}
		inner_querybuf.appendCondition(String.format("SIMHST.SIM_OUT_DATETIME BETWEEN (SYSDATE - %s) AND SYSDATE+1", simHistUseDays));

		inner_querybuf.appendGroupBy("SIMHST.ORGANIZATIONCD");
		inner_querybuf.appendGroupBy("SIMHST.DIST_CHANNELCD");
		inner_querybuf.appendGroupBy("SIMHST.PARTYCD");
		inner_querybuf.appendGroupBy("SIMHST.SHIP_PARTYCD");
		inner_querybuf.appendGroupBy("SIMHST.ITEMCD");

		qb_base.appendTable(inner_querybuf, "SIMHST");
		qb_base.appendData("SIMHST.ORGANIZATIONCD");
		qb_base.appendData("SIMHST.DIST_CHANNELCD");
		qb_base.appendData("SIMHST.PARTYCD");
		qb_base.appendData("SIMHST.SHIP_PARTYCD");
		qb_base.appendData("SIMHST.ITEMCD");

		if( optionalKeys == null ) {
			qb_base.appendDataWithAlias("ROW_NUMBER() OVER(ORDER BY SIMHST.ITEM_CNT DESC NULLS LAST)", "REGULAR_ITEM_RANK");
			qb_base.appendDataWithAlias("SIMHST.ITEM_CNT", "SIM_ITEM_CNT");
		} else {
			if( java.util.Arrays.asList(optionalKeys).contains("simItemCount") ) {
				qb_base.appendDataWithAlias("SIMHST.ITEM_CNT", "SIM_ITEM_CNT");
			}
			if( java.util.Arrays.asList(optionalKeys).contains("regularItemRank") ) {
				qb_base.appendDataWithAlias("ROW_NUMBER() OVER(ORDER BY SIMHST.ITEM_CNT DESC NULLS LAST)", "REGULAR_ITEM_RANK");
			}
		}

		if( new QueryBufferValid.Condition("organizationCode").hasValidCondition(qb_base) ) {
			qb_base.appendConditionByField("SIMHST.ORGANIZATIONCD", conditionMap.get("organizationCode"));
		}
		if( new QueryBufferValid.Condition("distributionChannelCode").hasValidCondition(qb_base) ) {
			qb_base.appendConditionByField("SIMHST.DIST_CHANNELCD", conditionMap.get("distributionChannelCode"));
		}
		if( new QueryBufferValid.Condition("partyCode").hasValidCondition(qb_base) ) {
			qb_base.appendConditionByField("SIMHST.PARTYCD", conditionMap.get("partyCode"));
		}
		if( new QueryBufferValid.Condition("shipPartyCode").hasValidCondition(qb_base) ) {
			qb_base.appendConditionByField("SIMHST.SHIP_PARTYCD", conditionMap.get("shipPartyCode"));
		}
		if( new QueryBufferValid.Condition("itemCode").hasValidCondition(qb_base) ) {
			qb_base.appendConditionByField("SIMHST.ITEMCD", conditionMap.get("itemCode"));
		}

		return qb_base;
	}

	public static QueryableField[] makeItemEANQueryableFields( String innerTableAlias, String joinBaseTableAlias,
			boolean isPIPOConfirmedItem ) {
		JoinableImpl joinable = getItemEANJoinable(innerTableAlias, joinBaseTableAlias, isPIPOConfirmedItem);
		QueryableField[] qfields = null;
		if( isPIPOConfirmedItem ) {
			qfields = new QueryableField[] {
					new QueryableFieldImpl(Schema.CODE, "itemConsumerEANCodeCNF", innerTableAlias + ".ITEM_CONS_EAN", joinable),
					new QueryableFieldImpl(Schema.CODE, "eanCode", innerTableAlias + ".EANCODE", joinable),
					new QueryableFieldImpl(Schema.DATE, "eanEffectFrom", innerTableAlias + ".EAN_EFFECT_FROM", joinable),
					new QueryableFieldImpl(Schema.CODE, "mapEanCode", innerTableAlias + ".MAP_EANCODE", joinable),
					new QueryableFieldImpl(Schema.DATE, "mapEffectFrom", innerTableAlias + ".MAP_EFFECT_FROM", joinable)
			};
		} else {
			qfields = new QueryableField[] {
					new QueryableFieldImpl(Schema.CODE, "itemConsumerEANCode", innerTableAlias + ".ITEM_CONS_EAN", joinable),
					new QueryableFieldImpl(Schema.CODE, "eanCode", innerTableAlias + ".EANCODE", joinable),
					new QueryableFieldImpl(Schema.DATE, "eanEffectFrom", innerTableAlias + ".EAN_EFFECT_FROM", joinable),
					new QueryableFieldImpl(Schema.CODE, "mapEanCode", innerTableAlias + ".MAP_EANCODE", joinable),
					new QueryableFieldImpl(Schema.DATE, "mapEffectFrom", innerTableAlias + ".MAP_EFFECT_FROM", joinable)
			};
		}
		return qfields;
	}

	public static QueryableField[] makeStopItemQueryableFields( String baseAlias, Joinable joinable ) {
		return new QueryableField[] {
				new QueryableFieldImpl(DATE, "stopStartDate", baseAlias + ".STOP_STARTDATE", joinable),
				new QueryableFieldImpl(DATE, "stopEndDate", baseAlias + ".STOP_ENDDATE", joinable),
				new QueryableFieldImpl(CODE, "isStopItem", "(CASE WHEN " + baseAlias + ".STOP_STARTDATE <= TRUNC(pkCustom.fCurrentDate(" + baseAlias + ".ORGANIZATIONCD))"
						+ " AND " + baseAlias + ".STOP_ENDDATE+12/24 > TRUNC(pkCustom.fCurrentDate(" + baseAlias + ".ORGANIZATIONCD)) THEN 'Y' ELSE 'N' END)", "DPR_STOPITEM_STOPIND", "PUB_WHETHER_",
						joinable)
		};
	}

	public static QueryableField[] makeCloseItemQueryableFields( String baseAlias, Joinable joinable ) {
		return new QueryableField[] {
				new QueryableFieldImpl(Schema.DATETIME, "ordCloseDateTime", ordCloseDateTime, joinable),
				new QueryableFieldImpl(Schema.TIME, "ordCloseTime", "OCL.CLOSE_TIME", joinable),
				new QueryableFieldImpl(CODE, "isCloseItem", isCloseItem, joinable)
		};
	}

	public static QueryableField[] makePackdealDateQueryableFields( String baseAlias, Joinable joinable ) {
		return new QueryableField[] {
				new QueryableFieldImpl(DATE, "dealStartDate", baseAlias + ".DEAL_STARTDATE", joinable),
				new QueryableFieldImpl(DATE, "dealEndDate", baseAlias + ".DEAL_ENDDATE", joinable),
				new QueryableFieldImpl(CODE, "isPackdealDate", "(CASE WHEN " + baseAlias + ".DEAL_STARTDATE <= TRUNC(pkCustom.fCurrentDate(" + baseAlias + ".ORGANIZATIONCD))"
						+ " AND " + baseAlias + ".DEAL_ENDDATE+12/24 > TRUNC(pkCustom.fCurrentDate(" + baseAlias + ".ORGANIZATIONCD))"
						+ " AND " + baseAlias + ".DEAL_STOP_IND != 'Y' THEN 'Y' ELSE 'N' END)", "DPR_PACKDEAL_DEALIND", "PUB_WHETHER_", joinable)
		};
	}

	public static QueryableField[] makeItemEANQueryableFields( String innerTableAlias, String joinBaseTableAlias, JoinableImpl joinableBK,
			boolean isPIPOConfirmedItem ) {
		JoinableImpl joinable = getItemEANJoinable(innerTableAlias, joinBaseTableAlias, joinableBK, isPIPOConfirmedItem);
		QueryableField[] qfields = null;
		if( isPIPOConfirmedItem ) {
			qfields = new QueryableField[] {
					new QueryableFieldImpl(Schema.CODE, "itemConsumerEANCodeCNF", innerTableAlias + ".ITEM_CONS_EAN", joinable),
					new QueryableFieldImpl(Schema.CODE, "eanCode", innerTableAlias + ".EANCODE", joinable),
					new QueryableFieldImpl(Schema.DATE, "eanEffectFrom", innerTableAlias + ".EAN_EFFECT_FROM", joinable),
					new QueryableFieldImpl(Schema.CODE, "mapEanCode", innerTableAlias + ".MAP_EANCODE", joinable),
					new QueryableFieldImpl(Schema.DATE, "mapEffectFrom", innerTableAlias + ".MAP_EFFECT_FROM", joinable)
			};
		} else {
			qfields = new QueryableField[] {
					new QueryableFieldImpl(Schema.CODE, "itemConsumerEANCode", innerTableAlias + ".ITEM_CONS_EAN", joinable),
					new QueryableFieldImpl(Schema.CODE, "eanCode", innerTableAlias + ".EANCODE", joinable),
					new QueryableFieldImpl(Schema.DATE, "eanEffectFrom", innerTableAlias + ".EAN_EFFECT_FROM", joinable),
					new QueryableFieldImpl(Schema.CODE, "mapEanCode", innerTableAlias + ".MAP_EANCODE", joinable),
					new QueryableFieldImpl(Schema.DATE, "mapEffectFrom", innerTableAlias + ".MAP_EFFECT_FROM", joinable)
			};
		}
		return qfields;
	}

	public static JoinableImpl getItemEANJoinable( String innerTableAlias, String joinBaseTableAlias, boolean isPIPOConfirmedItem ) {
		return getItemEANJoinable(innerTableAlias, joinBaseTableAlias, null, isPIPOConfirmedItem);
	}

	public static JoinableImpl getItemEANJoinable( String innerTableAlias, String joinBaseTableAlias, JoinableImpl joinable,
			boolean isPIPOConfirmedItem ) {
		StringBuffer conditionQuery = new StringBuffer();
		conditionQuery.append(innerTableAlias + ".ORGANIZATIONCD(+)" + " = " + joinBaseTableAlias + ".ORGANIZATIONCD"//
				+ " AND " + innerTableAlias + ".ITEMCD(+) = " + joinBaseTableAlias);
		if( isPIPOConfirmedItem ) {
			conditionQuery.append(".ITEMCD_CNF");
		} else {
			conditionQuery.append(".ITEMCD");
		}
		return new JoinableImpl(innerTableAlias, "vwDPR_ITEM_EANRLT", conditionQuery.toString(), joinable);
	}

	public static ConditionQueryBuffer getItemEANQueryBuffer( Map<String, Object> conditionMap ) {
		return null;// getItemEANQueryBuffer( conditionMap, false );
	}

	private static NestedJoinable getNestedOrderInfoDetail( final String alias ) {
		final NestedJoinable nested_ODINF = new NestedJoinable(alias) {
			{
				initNestedFields(new NestedJoinable.Field[] {
						// external, internal
						new NestedJoinable.Field("ORGANIZATIONCD", "OIND.ORGANIZATIONCD"),
						new NestedJoinable.Field("ORDER_NUMBER", "OIND.ORDER_NUMBER"),
						new NestedJoinable.Field("LINE_NO", "OIND.LINE_NO"),
						new NestedJoinable.Field("ITEMCD", "OIND.ITEMCD"),
						new NestedJoinable.Field("ITEMCD_CNF", "OIND.ITEMCD_CNF"),
						new NestedJoinable.Field("ITEMREF_IND", "OIND.ITEMREF_IND"),
				});
			}

			@Override
			public QueryBuffer getInnerTableQueryBuffer( Map<String, ? extends Object> conditionMap ) {
				ConditionQueryBuffer inner_querybuf = new ConditionQueryBuffer(conditionMap);

				inner_querybuf.appendData("ORDI.ORGANIZATIONCD");
				inner_querybuf.appendData("OIND.ORDER_NUMBER");
				inner_querybuf.appendData("OIND.LINE_NO");
				inner_querybuf.appendData("OIND.ITEMCD");
				inner_querybuf.appendData("OIND.ITEMCD_CNF");
				inner_querybuf.appendData("OIND.ITEMREF_IND");
				inner_querybuf.appendTableWithAlias("DPR_ORDER_INFO", "ORDI");
				inner_querybuf.appendTableWithAlias("DPR_ORDER_INFO_DTL", "OIND");
				inner_querybuf.appendCondition("ORDI.ORDER_NUMBER = OIND.ORDER_NUMBER");

				return inner_querybuf;
			}
		};
		return nested_ODINF;
	}

	private static boolean makeCompositeOrderNumberCondition( ConditionQueryBuffer querybuf ) {
		boolean hasCondition = querybuf.hasConditionQuery();

		Map<String, Object> conditionMap = (Map<String, Object>)querybuf.getConditionMap();
		if( conditionMap.containsKey("compositeOrderNumber") ) {
			Object conditionValue = conditionMap.get("compositeOrderNumber");
			String fieldNameFormat = "DECODE('%s', ORD.SIMULATIONKEY, ORD.SIMULATIONKEY, ORD.ORDER_NUMBER, ORD.ORDER_NUMBER, NULL)";
			querybuf.appendConditionByField(String.format(fieldNameFormat, conditionValue), conditionValue);
			querybuf.appendTableWithAlias( "DPR_ORDER", "ORD", "ORD.ORDER_NUMBER = OIND.ORDER_NUMBER" );
			return true;
		}
		return hasCondition;
	}
}
