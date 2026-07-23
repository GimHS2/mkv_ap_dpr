/*
 *	File Name:	OrderItem.java
 *	Version:	2.2.12
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2026/04/30		2.2.12	쿼리튜닝, itemCode별로 중복으로 실행하는 쿼리를 줄이기 위해 이전 쿼리 결과를 cache Map에 저장.
 *	yjkdev21	2026/03/31		2.2.11	TRUNC(SYSDATE) -> timezone 에 맞게 변경
 *	jbaek		2023/07/27		2.2.10	SellingSku 체크 추가
 *	hankalam	2020/06/30		2.2.9	위험상품 여부에 따른 상품트리 출력
 *	jbaek		2019/07/30		2.2.8	StopItem, PackDeal 추가, 자주 시뮬되는 아이템 표시여부 조건 추가
 *	jbaek		2018/10/30		2.2.7	Sales Office별 Plant Exclusion 기능 추가
 *	jbaek		2017/06/30		2.2.6	upc/ean 검색 기능 추가.(itemConsumerEANCode)
 *	hankalam	2017/02/28		2.2.5	getDistAllowUOM() 추가
 *	jbaek		2014/09/30		2.2.4	Producth Hierarchy Level 기능 개발
 *	jbaek		2014/02/16		2.2.3	Plant SKU 제외 기능 개발
 *	jbaek		2012/08/30		2.2.2	getItemInfoMap()추가: 오더아이템의 사용가능기간 변경
 *	lsinji		2009/12/11		2.2.1	setPrimaryConditionQuery() 변경
 *										existingOrderInd 쿼리 변경
 *	guksm		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.FieldException;
import com.irt.rbm.RBMSystem;
import com.irt.sql.ConditionQueryBuffer;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryFactory;
import com.irt.sql.QueryableField;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.util.MapUtil;

/**
*
*/
public class OrderItem extends com.irt.rbm.ManipulableManagerImpl {//@formatter:off
	private final static Table table = Schema.findTable(Schema.DPR_ORDER_ITEM);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_ORDER_ITEM);
	ThreadLocal<Map<String, List<Map<String, Object>>>> cache = ThreadLocal.withInitial( HashMap::new );

	public OrderItem( SQLHandler handler ) {
		super(handler, table, factory);
	}

	private List<Map<String, Object>> getRegularItemList( Map<String, Object> conditionMap ) throws SQLException {
		ConditionQueryBuffer qb_SIMHST = Schema.getRegularItemQueryBuffer(conditionMap, new String[] { "simItemCount" });

		return SQLManager.getRecordList(handler, qb_SIMHST);
	}

	public List<Map<String, Object>> getItemTreeList( Map<String, Object> conditionMap ) throws SQLException {
		List<Map<String, Object>> orderItems = getItemTreeList(conditionMap, null);

		if( Country.isFeature((String)conditionMap.get("organizationCode"), "useRgSimItem") ) {
			Map<String, Object> regularItemsConditionMap = MapUtil.getPartialMap(conditionMap,
					new String[] { "organizationCode", "distributionChannelCode", "partyCode", "shipPartyCode" });
			List<Map<String, Object>> regularItems = getRegularItemList(regularItemsConditionMap);

			if( orderItems != null && orderItems.size() > 0 ) {
				if( regularItems != null && regularItems.size() > 0 ) {
					for( Map<String, Object> m1 : orderItems ) {
						for( Map<String, Object> m2 : regularItems ) {
							if( m1.get("itemCode") != null && m1.get("itemCode").equals(m2.get("ITEMCD")) ) {
								m1.put("simItemCount", m2.get("SIM_ITEM_CNT"));
							}
						}
					}
				}
			}
		}

		return orderItems;
	}

	public Map<String, Object> getBarCodeMultiItems( List<Map<String, Object>> tree ) {
		Map<String, Object> barCodeMultiItems = new HashMap<String, Object>();

		if( tree != null ) {
			Map<String, List<String>> temp = new HashMap<String, List<String>>();
			for( Map<String, Object> map : tree ) {
				final String barCode = (String)map.get("itemConsumerEANCode");
				final String itemCode = (String)map.get("itemCode");

				// has mapping
				if( barCode != null ) {
					List<String> multiItems = temp.get(barCode);
					if( multiItems == null ) {
						List<String> list = new ArrayList<String>();
						list.add(itemCode);
						temp.put(barCode, list);
					} else {
						multiItems.add(itemCode);
						temp.put(barCode, multiItems);
					}
				}
			}

			for( String key : temp.keySet() ) {
				List<String> multiItems = temp.get(key);

				if( multiItems.size() > 1 ) {
					barCodeMultiItems.put(key, multiItems);
				}
			}
			temp = null;
		}

		return barCodeMultiItems;
	}

	public static Map<String, String> createPrimary( String distributorCode, String organizationCode, String distributionChannelCode,
			String divisionCode, String itemCode ) {
		Map<String, String> map = new java.util.HashMap<String, String>();

		map.put("distributorCode", distributorCode);
		map.put("partyCode", distributorCode);
		map.put("organizationCode", organizationCode);
		map.put("distributionChannelCode", distributionChannelCode);
		map.put("divisionCode", divisionCode);
		map.put("itemCode", itemCode);

		return map;
	}

	public String getDistAllowUOM( Map<String, ? extends Object> primaryMap ) throws SQLException {
		return (String)SQLManager.getObjectValue(handler,
				"SELECT ALLOW_UOM FROM DPR_PARTY_SALES WHERE PARTYCD = ? AND ORGANIZATIONCD = ? AND DIST_CHANNELCD = ? AND DIVISIONCD = ?",
				primaryMap.get("partyCode"), primaryMap.get("organizationCode"), primaryMap.get("distributionChannelCode"),
				primaryMap.get("divisionCode"));
	}

	public Map<String, Object> getItemInfoMap( Map<String, ? extends Object> primaryMap, String[] fieldKeys ) throws SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer(primaryMap);

		factory.setDataQuery(querybuf, fieldKeys);
		if( setPrimaryConditionQuery(querybuf) == null )
			return null;

		Object availableDate = querybuf.getConditionValue("availableDate");
		if( availableDate != null ) {
			if( RBMSystem.getSystemEnvBool("DPR", "Feature;useItemAvailDateFilter", false) ) {// usually problem for wrong mapped dates
				querybuf.appendCondition("(ITM.STARTAVAIL_DATE IS NULL OR ITM.STARTAVAIL_DATE <= ?)", availableDate);
				querybuf.appendCondition("(ITM.ENDAVAIL_DATE IS NULL OR ITM.ENDAVAIL_DATE > ?)", availableDate);
			}
			querybuf.appendCondition("(OITM.STARTAVAIL_DATE IS NULL OR OITM.STARTAVAIL_DATE <= ?)", availableDate);
			querybuf.appendCondition("(OITM.ENDAVAIL_DATE IS NULL OR OITM.ENDAVAIL_DATE > ?)", availableDate);
		}

		String shipPartyCode = (String)primaryMap.get("shipPartyCode");
		if( shipPartyCode != null && shipPartyCode.length() > 0 ) {
			querybuf.appendTable(getPlantExclusionQueryBuffer(primaryMap), "PTITME",
					"PTITME.PARTYCD(+) = OITM.PARTYCD AND PTITME.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD"
							+ " AND PTITME.DIVISIONCD(+) = OITM.DIVISIONCD AND PTITME.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD"
							+ " AND PTITME.COUNTRYCD(+) = OITM.COUNTRYCD AND PTITME.ITEMCD(+) = OITM.ITEMCD");
			querybuf.appendCondition("PTITME.SHIP_PARTYCD(+) = ?", shipPartyCode);
			querybuf.appendCondition("PTITME.ITEMCD IS NULL");
		}

		return SQLManager.getRecordMap(handler, null, querybuf);
	}

	public static QueryBuffer getPlantExclusionQueryBuffer( Map<String, ? extends Object> conditionMap ) {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer(conditionMap);

		querybuf.appendData("PNITM.ORGANIZATIONCD");
		querybuf.appendData("PNITM.DIST_CHANNELCD");
		querybuf.appendData("PNITM.PARTYCD");
		querybuf.appendData("PNITM.SHIP_PARTYCD");
		querybuf.appendData("PNITM.ITEMCD");
		querybuf.appendData("PNITM.COUNTRYCD");
		querybuf.appendData("PNITM.DIVISIONCD");

		querybuf.appendTableWithAlias("vwDPR_PLANT_EXCL_ITEM", "PNITM");

		querybuf.findCondition("organizationCode", "PNITM.ORGANIZATIONCD");
		querybuf.findCondition("distributionChannelCode", "PNITM.DIST_CHANNELCD");
		querybuf.findCondition("partyCode", "PNITM.PARTYCD");
		querybuf.findCondition("shipPartyCode", "PNITM.SHIP_PARTYCD");
		querybuf.findCondition("itemCode", "PNITM.ITEMCD");

		return querybuf;
	}

	public List<Map<String, Object>> getItemTreeList( Map<String, Object> conditionMap, String[] optionalKeys ) throws SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer(conditionMap);

		String codeLength = String.valueOf(ProductHierarchy.getLength(conditionMap));

		String query = "";
		query = "PRC.CLASSCD";
		querybuf.appendDataWithAlias(query, "defaultClassCode");
		query = "COUNT(DISTINCT PRC.PCATE_CD) OVER ()";
		querybuf.appendDataWithAlias(query, "pCateCount");
		query = "Count(DISTINCT DECODE(HIMT.PCATECD, 'x', MSTD.MASTER_CD, null, MSTD.MASTER_CD, HIMT.PCATECD) ) OVER ()";
		querybuf.appendDataWithAlias(query, "cateCount");
		query = "CAST(DECODE(LENGTH(DECODE(HIMT.PCATECD, 'x', MSTD.MASTER_CD, null, MSTD.MASTER_CD, HIMT.PCATECD)),1,1,2,2,6,3,10,4,14,5,6) AS NUMBER)";
		querybuf.appendDataWithAlias(query, "currClassCode");
		query = "SUBSTR( RAW_CATECD,1,DECODE(CAST(DECODE(LENGTH(DECODE(HIMT.PCATECD, 'x', MSTD.MASTER_CD, null, MSTD.MASTER_CD, HIMT.PCATECD)),1,1,2,2,6,3,10,4,14,5,6)+1 AS NUMBER)"
				+ ",1,1,2,2,3,6,4,10,5,14,LENGTH(RAW_CATECD)))";
		querybuf.appendDataWithAlias(query, "childCateCode");

		querybuf.appendData("PRC.PCATE_CD \"defaultCateCode\"");
		querybuf.appendData("'1;2;6;10;14;' RAW_CATECD_LENS");
		querybuf.appendData("SUBSTR(RAW_CATECD,1,1) C1");
		querybuf.appendData("SUBSTR(RAW_CATECD,1,2) C2");
		querybuf.appendData("SUBSTR(RAW_CATECD,1,6) C3");
		querybuf.appendData("SUBSTR(RAW_CATECD,1,10) C4");
		querybuf.appendData("SUBSTR(RAW_CATECD,1,14) C5");
		querybuf.appendData("RAW_CATECD C6");
		String masterOrganizationCode = (String)conditionMap.get("organizationCode");
		String displayLanguage = (String)conditionMap.get("displayLanguage");

		query = "(SELECT MASTER_NAME FROM " + Schema.masterDescOrgQuery
				+ " SB WHERE SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ? AND SB.MASTER_CD = SUBSTR(RAW_CATECD,1,1) )";
		querybuf.appendDataWithAlias(query, "N1");
		querybuf.addBindVariables( querybuf.getBindVariableCount(), displayLanguage, masterOrganizationCode, displayLanguage );
		query = "(SELECT MASTER_NAME FROM " + Schema.masterDescOrgQuery
				+ " SB WHERE SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ? AND SB.MASTER_CD = SUBSTR(RAW_CATECD,1,2) )";
		querybuf.appendDataWithAlias(query, "N2");
		querybuf.addBindVariables( querybuf.getBindVariableCount(), displayLanguage, masterOrganizationCode, displayLanguage );
		query = "(SELECT MASTER_NAME FROM " + Schema.masterDescOrgQuery
				+ " SB WHERE SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ? AND SB.MASTER_CD = SUBSTR(RAW_CATECD,1,6) )";
		querybuf.appendDataWithAlias(query, "N3");
		querybuf.addBindVariables( querybuf.getBindVariableCount(), displayLanguage, masterOrganizationCode, displayLanguage );
		query = "(SELECT MASTER_NAME FROM " + Schema.masterDescOrgQuery
				+ " SB WHERE SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ? AND SB.MASTER_CD = SUBSTR(RAW_CATECD,1,10) )";
		querybuf.appendDataWithAlias(query, "N4");
		querybuf.addBindVariables( querybuf.getBindVariableCount(), displayLanguage, masterOrganizationCode, displayLanguage );
		query = "(SELECT MASTER_NAME FROM " + Schema.masterDescOrgQuery
				+ " SB WHERE SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ? AND SB.MASTER_CD = SUBSTR(RAW_CATECD,1,14) )";
		querybuf.appendDataWithAlias(query, "N5");
		querybuf.addBindVariables( querybuf.getBindVariableCount(), displayLanguage, masterOrganizationCode, displayLanguage );
		query = "(SELECT MASTER_NAME FROM " + Schema.masterDescOrgQuery
				+ " SB WHERE SB.MASTER_TYPE = 'PC' AND SB.LANGCD = ? AND SB.MASTER_CD = RAW_CATECD )";
		querybuf.appendDataWithAlias(query, "N6");
		querybuf.addBindVariables( querybuf.getBindVariableCount(), displayLanguage, masterOrganizationCode, displayLanguage );

		querybuf.appendDataWithAlias("DECODE(HIMT.PCATECD, 'x', MSTD.MASTER_CD, null, MSTD.MASTER_CD, HIMT.PCATECD)", "cateCode");
		querybuf.appendDataWithAlias("DECODE(HIMT.PCATECD, 'x', MSTD.MASTER_NAME, null, MSTD.MASTER_NAME, MSTD_H.MASTER_NAME)", "cateName");
		querybuf.appendDataWithAlias("OITM.ITEMCD", "itemCode");
		querybuf.appendDataWithAlias("IMTD.ITEMNAME", "itemName");
		querybuf.appendDataWithAlias("OITM.NEWITEM_IND", "newItemInd");
		querybuf.appendDataWithAlias("OITM.PROMOTION_IND", "promotionItemInd");
		querybuf.appendDataWithAlias("OITM.DIST_CHANNELCD", "distributionChannelCode");

		querybuf.appendTableWithAlias("DPR_ORDER_ITEM", "OITM");
		String shipPartyCode = (String)conditionMap.get("shipPartyCode");
		if( shipPartyCode != null && shipPartyCode.length() > 0 ) {
			querybuf.appendTable(getPlantExclusionQueryBuffer(conditionMap), "PTITME",
					"PTITME.PARTYCD(+) = OITM.PARTYCD AND PTITME.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD"
							+ " AND PTITME.DIVISIONCD(+) = OITM.DIVISIONCD AND PTITME.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD"
							+ " AND PTITME.COUNTRYCD(+) = OITM.COUNTRYCD AND PTITME.ITEMCD(+) = OITM.ITEMCD");
			querybuf.appendCondition("PTITME.SHIP_PARTYCD(+) = ?", shipPartyCode);
			querybuf.appendCondition("PTITME.ITEMCD IS NULL");
		}

		// too slow querybuf.appendDataWithAlias( "NVL2(SIMHST.REGULAR_ITEM_RANK, ROW_NUMBER() OVER(ORDER BY SIMHST.REGULAR_ITEM_RANK ASC NULLS
		// LAST),
		// NULL)", "regularItemRank" );
		if( optionalKeys != null && optionalKeys.length > 0 ) {
			if( java.util.Arrays.asList(optionalKeys).contains("simItemCount") ) {
				querybuf.appendDataWithAlias("SIMHST.SIM_ITEM_CNT", "simItemCount");
				ConditionQueryBuffer qb_SIMHST = Schema.getRegularItemQueryBuffer(conditionMap);
				querybuf.appendTable(qb_SIMHST, "SIMHST", "SIMHST.ITEMCD(+) = OITM.ITEMCD");
			}
		}

		if( Condition.isConditionTrue(conditionMap, "useStopItem") ) {
			String baseAlias = "STIMRT";
			querybuf.appendTableWithAlias("vwDPR_STOPITEM_CFGRLT", "STIMRT", "STIMRT.ITEMCD(+) = OITM.ITEMCD AND STIMRT.PARTYCD(+) = OITM.PARTYCD"
					+ " AND STIMRT.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND STIMRT.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD");

			querybuf.appendDataWithAlias("STIMRT.STOP_STARTDATE", "stopStartDate");
			querybuf.appendDataWithAlias("STIMRT.STOP_ENDDATE", "stopEndDate");
			querybuf.appendDataWithAlias("(CASE WHEN " + baseAlias + ".STOP_STARTDATE <= TRUNC(pkCustom.fCurrentDate(OITM.ORGANIZATIONCD))"
					+ " AND " + baseAlias + ".STOP_ENDDATE+12/24 > TRUNC(pkCustom.fCurrentDate(OITM.ORGANIZATIONCD)) THEN 'Y' ELSE 'N' END)",
					"isStopItem");
		}

		querybuf.appendTableWithAlias("DPR_ORDCLOSE", "OCL", "OCL.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD AND OCL.BRANDCD(+) = ITM.BRANDCD");
		querybuf.appendDataWithAlias("OCL.CLOSE_TIME", "ordCloseTime");
		querybuf.appendDataWithAlias(Schema.ordCloseDateTime, "ordCloseDateTime");
		querybuf.appendDataWithAlias(Schema.isCloseItem, "isCloseItem");


		if( Condition.isConditionTrue(conditionMap, "usePackDeal") ) {
			QueryBuffer inner_PDR = new QueryBuffer();
			inner_PDR.appendTableWithAlias("vwDPR_PACKDEAL", "PDR");
			inner_PDR.appendDataWithAlias("MAX(PDR.DEAL_ENDDATE)", "DEAL_ENDDATE");
			inner_PDR.appendDataWithAlias("MIN(PDR.DEAL_STARTDATE)", "DEAL_STARTDATE");
			inner_PDR.appendDataWithAlias("'Y'", "isPackdealDate");
			inner_PDR.appendDataWithGroupBy("PDR.DEAL_STOP_IND");
			inner_PDR.appendDataWithGroupBy("PDR.ITEMCD");
			inner_PDR.appendDataWithGroupBy("PDR.PARTYCD");
			inner_PDR.appendDataWithGroupBy("PDR.ORGANIZATIONCD");
			inner_PDR.appendDataWithGroupBy("PDR.DIST_CHANNELCD");
			inner_PDR.appendCondition("'Y' = (CASE WHEN PDR.DEAL_STARTDATE <= TRUNC(pkCustom.fCurrentDate(PDR.ORGANIZATIONCD))"
					+ " AND PDR.DEAL_ENDDATE+12/24 > TRUNC(pkCustom.fCurrentDate(PDR.ORGANIZATIONCD))"
					+ " AND PDR.DEAL_STOP_IND != 'Y' THEN 'Y' ELSE 'N' END)");

			querybuf.appendTableWithAlias("(" + inner_PDR + ")", "PDR", "PDR.ITEMCD(+) = OITM.ITEMCD AND PDR.PARTYCD(+) = OITM.PARTYCD"
					+ " AND PDR.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND PDR.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD");

			querybuf.appendDataWithAlias("PDR.DEAL_STARTDATE", "dealStartDate");
			querybuf.appendDataWithAlias("PDR.DEAL_ENDDATE", "dealEndDate");
			querybuf.appendDataWithAlias("PDR.\"isPackdealDate\"", "isPackdealDate");
		}

		querybuf.appendTableWithAlias("vwDPR_ITEM_EANRLT", "IMEAN", "IMEAN.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND IMEAN.ITEMCD(+) = OITM.ITEMCD");
		querybuf.appendDataWithAlias("IMEAN.ITEM_CONS_EAN", "itemConsumerEANCode");
		querybuf.findCondition("itemConsumerEANCode", "IMEAN.ITEM_CONS_EAN");

		querybuf.appendTableWithAlias("DPR_ITEM", "ITM", "ITM.COUNTRYCD = OITM.COUNTRYCD"
				+ " AND ITM.ORGANIZATIONCD = OITM.ORGANIZATIONCD AND ITM.DIST_CHANNELCD = OITM.DIST_CHANNELCD"
				+ " AND ITM.ITEMCD = OITM.ITEMCD");
		querybuf.appendTableWithAlias("DPR_ITEM_MASTER_DESC", "IMTD", "IMTD.ITEMCD(+) = OITM.ITEMCD");
		querybuf.appendTableWithAlias("DPR_MASTER_DESC", "MSTD",
				"MSTD.MASTER_CD(+) = SUBSTR(ITM.PCATECD, 1, " + codeLength + ") AND MSTD.MASTER_TYPE(+) = 'PC'");
		querybuf.appendTableWithAlias("DPR_PRODUCT_CATE", "PRC", "PRC.PCATE_CD = SUBSTR(ITM.PCATECD, 1, " + codeLength + ")");
		querybuf.appendTableWithAlias("DPR_PROMOTION_ITEM", "PITM",
				"PITM.PARTYCD(+) = OITM.PARTYCD AND PITM.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD"
						+ " AND PITM.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD AND PITM.ITEMCD(+) = OITM.ITEMCD"
						+ " AND PITM.COUNTRYCD(+) = OITM.COUNTRYCD");
		querybuf.appendDataWithAlias("ITM.BRANDCD", "brandCode");

		if( Condition.isConditionTrue(conditionMap, "useDangerousItem") ) {
			querybuf.appendTableWithAlias( "DPR_ITEM_MASTER_PLANT", "IMTP" );
			String dangerousPlant = (String)conditionMap.get( "dangerousPlant" );
			String dangerousNumber = (String)conditionMap.get( "dangerousNumber" );

			if( querybuf.hasConditionValue("dangerousInd") ) {
				querybuf.appendCondition( "IMTP.ITEMCD(+) = ITM.ITEMCD" );
				querybuf.appendCondition( "IMTP.PLANT(+) = ?", dangerousPlant );
				querybuf.appendCondition( "IMTP.LOADING_GRP(+) = ?", dangerousNumber );
				Object[] bindValues = { querybuf.getConditionValue("dangerousNumber"),  querybuf.getConditionValue("dangerousInd") };
				querybuf.appendCondition( "DECODE(IMTP.LOADING_GRP, ?, 'Y', 'N') = ?", bindValues );
			} else {
				querybuf.appendCondition( "IMTP.ITEMCD = ITM.ITEMCD" );
				querybuf.appendCondition( "IMTP.PLANT = ?", dangerousPlant );
				querybuf.appendCondition( "IMTP.LOADING_GRP = ?", dangerousNumber );
			}
		}

		QueryBuffer inn = ProductHierarchy.getInnerHierarchyQuery(conditionMap);
		inn.appendDataWithAlias("ITM.PCATECD", "RAW_CATECD");

		querybuf.appendTable(inn, "HIMT", "OITM.COUNTRYCD(+) = HIMT.COUNTRYCD AND OITM.ORGANIZATIONCD(+) = HIMT.ORGANIZATIONCD "
				+ "AND OITM.DIST_CHANNELCD(+) = HIMT.DIST_CHANNELCD AND OITM.ITEMCD(+) = HIMT.ITEMCD");
		querybuf.appendTableWithAlias("DPR_MASTER_DESC", "MSTD_H", "MSTD_H.MASTER_CD(+) = HIMT.PCATECD AND MSTD_H.MASTER_TYPE(+) = 'PC'");

		if( conditionMap.containsKey("orderKey") ) {
			String orderKey = (String)conditionMap.get("orderKey");
			if( orderKey != null && orderKey.length() > 0 ) {
				querybuf.appendDataWithAlias( "DECODE( (SELECT COUNT(*) FROM DPR_ORDER_DTL ODTL WHERE ODTL.ORDERKEY = ?"
						+ " AND ODTL.ITEMCD = OITM.ITEMCD), 0, 'N', 'Y')", "existingOrderInd" );
				querybuf.addBindVariable( querybuf.getBindVariableCount(), orderKey );
			}
		}

		querybuf.findCondition("countryCode", "OITM.COUNTRYCD");
		querybuf.findCondition("partyCode", "OITM.PARTYCD");
		querybuf.findCondition("organizationCode", "OITM.ORGANIZATIONCD");
		querybuf.findCondition("distributionChannelCode", "OITM.DIST_CHANNELCD");
		querybuf.findCondition("divisionCode", "OITM.DIVISIONCD");
		querybuf.findCondition("displayLanguage", "IMTD.LANGCD(+)");

		// 모든 Organization의 Product Hierarchy는 English를 사용
		querybuf.appendCondition("MSTD.LANGCD(+) = ?", "en");
		querybuf.appendCondition("MSTD_H.LANGCD(+) = ?", "en");
		querybuf.findCondition("classCode", "PRC.CLASSCD");
		querybuf.findCondition("newItemInd", "OITM.NEWITEM_IND");
		querybuf.findCondition("promotionItemInd", "OITM.PROMOTION_IND");

		Object availableDate = querybuf.getConditionValue("availableDate");
		if( availableDate != null ) {

			if( RBMSystem.getSystemEnvBool("DPR", "Feature;useItemAvailDateFilter", false) ) {// usually problem for wrong mapped dates
				querybuf.appendCondition("(ITM.STARTAVAIL_DATE IS NULL OR ITM.STARTAVAIL_DATE <= ?)", availableDate);
				querybuf.appendCondition("(ITM.ENDAVAIL_DATE IS NULL OR ITM.ENDAVAIL_DATE > ?)", availableDate);
			}

			querybuf.appendCondition("(OITM.STARTAVAIL_DATE IS NULL OR OITM.STARTAVAIL_DATE <= ?)", availableDate);
			querybuf.appendCondition("(OITM.ENDAVAIL_DATE IS NULL OR OITM.ENDAVAIL_DATE > ?)", availableDate);

			String itemDisplayInd = " 'Y' = (SELECT"
					+ " (CASE WHEN IMTSS.STATUS_CD IS NULL AND LTRIM(ITM.SALES_STATUS_FROM,'0') IS NULL THEN"
					+ " CASE WHEN IMTSC.STATUS_CD IS NULL AND LTRIM(ITM.CHAIN_STATUS_FROM,'0') IS NULL THEN 'Y'"
					+ " WHEN IMTSC.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.CHAIN_STATUS_FROM,'00000000',NULL,ITM.CHAIN_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
					+ " ELSE 'Y' END"
					+ " ELSE (CASE WHEN IMTSS.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.SALES_STATUS_FROM,'00000000',NULL,ITM.SALES_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
					+ " WHEN IMTSC.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.CHAIN_STATUS_FROM,'00000000',NULL,ITM.CHAIN_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
					+ " ELSE 'Y' END)"
					+ " END)"
					+ " FROM DPR_ITEM ITM_I, DPR_ITEM_MASTER_STATUS IMTSS, DPR_ITEM_MASTER_STATUS IMTSC"
					+ " WHERE IMTSS.STATUS_CD(+) = ITM_I.SALES_STATUS AND IMTSC.STATUS_CD(+) = ITM_I.CHAIN_STATUS"
					+ " AND ITM_I.ORGANIZATIONCD = ITM.ORGANIZATIONCD AND ITM_I.DIST_CHANNELCD = ITM.DIST_CHANNELCD AND ITM_I.ITEMCD = ITM.ITEMCD)";

			querybuf.appendCondition(itemDisplayInd, new Object[] { availableDate, availableDate, availableDate });
		}

		querybuf.findCondition("status", "OITM.STATUS");
		querybuf.findCondition("itemCode", "OITM.ITEMCD");
		querybuf.findCondition("itemName", "IMTD.ITEMNAME");
		if( querybuf.hasConditionValue("odrdlvGroup") )
			querybuf.findConditionCode("brandCode", "ITM.BRANDCD");

		querybuf.appendOrderByFieldName("currClassCode", "DESC");
		querybuf.appendOrderByFieldName("childCateCode");
		querybuf.appendOrderByFieldName("cateName");
		if( Country.KOREA_ORGANIZATION.equals(masterOrganizationCode) ) {
			querybuf.appendOrderBy("\"cateCode\", \"itemConsumerEANCode\", \"itemCode\", \"itemName\" NULLS LAST");
		} else {
			querybuf.appendOrderBy("\"cateCode\", \"itemCode\", \"itemName\" NULLS LAST");
		}
		querybuf.appendHint( "USE_HASH(PDR)" );

		return SQLManager.getRecordList(handler, querybuf);
	}

	@Override
	protected QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) {
		querybuf = (ConditionQueryBuffer)super.setPrimaryConditionQuery(querybuf);
		if( querybuf != null ) {
			Map<String, Object> conditionMap = (Map<String, Object>)querybuf.getConditionMap();
			if( conditionMap.get("orderKey") != null ) {
				QueryableField field = factory.getQueryableField("existingOrderInd");
				field.appendData(querybuf);
			}
		}

		return querybuf;
	}

	public Map<String, Object> getRecordCountWithPlant( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException {
		QueryBuffer querybuf = factory.setQuery(new ConditionQueryBuffer(conditionMap), fieldKeys);
		factory.setDataQuery(querybuf, fieldKeys);
		// if( setPrimaryConditionQuery(querybuf) == null ) return null;

		return SQLManager.getRecordMap(handler, null, querybuf);
	}

	public DataLoader.Validator createNormalOrderStopItemValidator( final Map<String, Object> inputParameterMap ) {
		if( !com.irt.dpr.Country.isFeature((String)inputParameterMap.get("organizationCode"), "useStopItem") ) {
			return null;
		}

		DataLoader.Validator stopItemFix = new DataLoader.Validator() {
			private List<Map<String, Object>> stopItemList;
			private Map<String, Integer> stopItemIndex;

			@Override
			public void close() {
				this.stopItemIndex = null;
				this.stopItemList = null;
			}

			private void loadValidationData( SQLHandler handler ) throws SQLException {
				StopItem stopItem = new StopItem(handler);
				Map<String, Object> conditionMap = MapUtil.getPartialMap(inputParameterMap,
						new String[] { "organizationCode", "divisionCode", "partyCode", "distributionChannelCode" });
				conditionMap.put("status", "00");
				conditionMap.put("isStopItem", "Y");

				String key = "S"+ inputParameterMap.get("organizationCode") + inputParameterMap.get("distributionChannelCode") + inputParameterMap.get("partyCode");
				if( cache.get().containsKey(key) ) {
					stopItemList = cache.get().get( key );
				} else {
					stopItemList = stopItem.getRecords(conditionMap, new String[] { "itemCode", "isStopItem", "stopStartDate", "stopEndDate" });
					cache.get().put( key, stopItemList );
				}

				if( stopItemList != null ) {
					stopItemIndex = new HashMap<String, Integer>();
					for( int i = 0; i < stopItemList.size(); i++ ) {
						Map<String, Object> map = stopItemList.get(i);
						stopItemIndex.put((String)map.get("itemCode"), i);
					}
				}
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				if( stopItemList == null ) {
					loadValidationData(handler);
				}
				if( recordMap != null && stopItemList != null ) {
					String orderInputItemCode = (String)recordMap.get("itemCode");

					String fieldKey_orderQty = null;
					Integer _orderInputQty = null;
					String uploadOrderQty = (String)recordMap.get("uploadOrderQty");
					if( uploadOrderQty != null ) {
						try {
							_orderInputQty = Integer.parseInt(uploadOrderQty);
							fieldKey_orderQty = "uploadOrderQty";
						} catch( NumberFormatException nfe ) {
						}
					} else {
						String orderQty = (String)recordMap.get("orderQty");
						if( orderQty != null ) {
							try {
								_orderInputQty = Integer.parseInt(orderQty);
								fieldKey_orderQty = "orderQty";
							} catch( NumberFormatException nfe ) {
							}
						}
					}

					if( stopItemIndex.containsKey(orderInputItemCode) && ( _orderInputQty != null && _orderInputQty > 0 ) ) {
						Integer index = stopItemIndex.get(orderInputItemCode);
						Map<String, Object> stopItem = stopItemList.get(index);

						String stopStartDate = null;
						String stopEndDate = null;
						if( stopItem.get("stopStartDate") != null
								&& stopItem.get("stopStartDate") instanceof java.util.Date ) {
							stopStartDate = com.irt.data.Date.getInstance((java.util.Date)stopItem.get("stopStartDate")).toString();
						}
						if( stopItem.get("stopEndDate") != null
								&& stopItem.get("stopEndDate") instanceof java.util.Date ) {
							stopEndDate = com.irt.data.Date.getInstance((java.util.Date)stopItem.get("stopEndDate")).toString();
						}

						String message = handler.getMessageHandler().getMessage("ERR_UPLOAD_ORDER_ITEM_IS_STOPITEM_3",
								(String)recordMap.get("itemCode"), stopStartDate, stopEndDate);

						Map<String, Object> newMap = new HashMap<String, Object>(recordMap);
						newMap.put(fieldKey_orderQty, "0");
						throw new DataPassingException("ERR_UPLOAD_ORDER_ITEM_IS_STOPITEM_3", message, newMap, recordMap);
					}
				}
			}
		};
		return stopItemFix;
	}

	public DataLoader.Validator createOrderCloseTimeValidator( final Map<String, Object> inputParameterMap ) {
		DataLoader.Validator packclosedItemFix = new DataLoader.Validator() {
			private List<Map<String, Object>> closedItemList;
			private Map<String, Integer> closedItemIndex;

			private synchronized void loadData( SQLHandler handler ) throws SQLException {
				Map<String, Object> conditionMap = MapUtil.getPartialMap(inputParameterMap,
						new String[] { "organizationCode", "divisionCode", "partyCode", "distributionChannelCode" });
				conditionMap.put("status", "00");
				conditionMap.put("isCloseItem", "Y");

				String key = "C"+ inputParameterMap.get("organizationCode") + inputParameterMap.get("distributionChannelCode") + inputParameterMap.get("partyCode");
				if( cache.get().containsKey(key) ) {
					closedItemList = cache.get().get( key );
				} else {
					closedItemList = getRecords(conditionMap, new String[] { "itemCode", "isCloseItem", "ordCloseTime", "ordCloseDateTime" });
					cache.get().put( key, closedItemList );
				}

				if( closedItemList != null ) {
					closedItemIndex = new HashMap<String, Integer>();
					for( int i = 0; i < closedItemList.size(); i++ ) {
						Map<String, Object> map = closedItemList.get(i);
						closedItemIndex.put((String)map.get("itemCode"), i);
					}
				}
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				if( closedItemList == null ) {
					loadData(handler);
				}

				String fieldKey_orderQty = null;
				if( recordMap != null && closedItemList != null ) {
					String orderInputItemCode = (String)recordMap.get("itemCode");

					Integer _orderInputQty = null;
					String uploadOrderQty = (String)recordMap.get("uploadOrderQty");
					if( uploadOrderQty != null ) {
						try {
							_orderInputQty = Integer.parseInt(uploadOrderQty);
							fieldKey_orderQty = "uploadOrderQty";
						} catch( NumberFormatException nfe ) {
						}
					} else {
						String orderQty = (String)recordMap.get("orderQty");
						if( orderQty != null ) {
							try {
								_orderInputQty = Integer.parseInt(orderQty);
								fieldKey_orderQty = "orderQty";
							} catch( NumberFormatException nfe ) {
							}
						}
					}

					if( closedItemIndex.containsKey(orderInputItemCode) && ( _orderInputQty != null && _orderInputQty > 0 ) ) {
						Integer index = closedItemIndex.get(orderInputItemCode);
						Map<String, Object> closedItem = closedItemList.get(index);

//						com.irt.data.Timestamp ordCloseDateTime = (com.irt.data.Timestamp)closedItem.get("ordCloseDateTime");
						String ordCloseTime = (String)closedItem.get("ordCloseTime");
						String isCloseItem = (String)closedItem.get("isCloseItem");
						if( "Y".equals(isCloseItem) ) {
							String message = handler.getMessageHandler().getMessage("ERR_UPLOAD_ORDER_ITEM_IS_CLOSED_2",
									(String)recordMap.get("itemCode"), ordCloseTime);

							Map<String, Object> newMap = new HashMap<String, Object>(recordMap);
							newMap.put(fieldKey_orderQty, "0");
							throw new DataPassingException("ERR_UPLOAD_ORDER_ITEM_IS_CLOSED_2", message, newMap, recordMap);
						}
					}
				}
			}

			@Override
			public void close() {
				this.closedItemIndex = null;
				this.closedItemList = null;
			}
		};

		return packclosedItemFix;
	}

	public DataLoader.Validator createNormalOrderPackDealValidator( final Map<String, Object> inputParameterMap ) {
		if( !com.irt.dpr.Country.isFeature((String)inputParameterMap.get("organizationCode"), "usePackDeal") ) {
			return null;
		} else if( inputParameterMap.get("dealCode") != null ) {
			return null;
		}

		DataLoader.Validator packdealItemFix = new DataLoader.Validator() {
			private List<Map<String, Object>> dealItemList;
			private Map<String, Integer> dealItemIndex;
			private PackDealItem pditm;

			private synchronized void loadData( SQLHandler handler ) throws SQLException {
				Map<String, Object> conditionMap = MapUtil.getPartialMap(inputParameterMap,
						new String[] { "organizationCode", "divisionCode", "partyCode", "distributionChannelCode" });
				conditionMap.put("status", "00");
				conditionMap.put("isPackdealDate", "Y");

				if( pditm == null )
					pditm = new PackDealItem(handler);

//				conditionMap.put("_headjoin", "Y");

				String key = "D"+ inputParameterMap.get("organizationCode") + inputParameterMap.get("distributionChannelCode") + inputParameterMap.get("partyCode");
				if( cache.get().containsKey(key) ) {
					dealItemList = cache.get().get( key );
				} else {
					dealItemList = pditm.getRecords(conditionMap, new String[] { "itemCode", "isPackdealDate", "dealStartDate", "dealEndDate" });
					cache.get().put( key, dealItemList );
				}

				if( dealItemList != null ) {
					dealItemIndex = new HashMap<String, Integer>();
					for( int i = 0; i < dealItemList.size(); i++ ) {
						Map<String, Object> map = dealItemList.get(i);
						dealItemIndex.put((String)map.get("itemCode"), i);
					}
				}
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				if( dealItemList == null ) {
					loadData(handler);
				}

				String fieldKey_orderQty = null;
				if( recordMap != null && dealItemList != null ) {
					String orderInputItemCode = (String)recordMap.get("itemCode");

					Integer _orderInputQty = null;
					String uploadOrderQty = (String)recordMap.get("uploadOrderQty");
					if( uploadOrderQty != null ) {
						try {
							_orderInputQty = Integer.parseInt(uploadOrderQty);
							fieldKey_orderQty = "uploadOrderQty";
						} catch( NumberFormatException nfe ) {
						}
					} else {
						String orderQty = (String)recordMap.get("orderQty");
						if( orderQty != null ) {
							try {
								_orderInputQty = Integer.parseInt(orderQty);
								fieldKey_orderQty = "orderQty";
							} catch( NumberFormatException nfe ) {
							}
						}
					}

					if( dealItemIndex.containsKey(orderInputItemCode) && ( _orderInputQty != null && _orderInputQty > 0 ) ) {
						Integer index = dealItemIndex.get(orderInputItemCode);
						Map<String, Object> dealItem = dealItemList.get(index);

						String dealStartDate = null;
						String dealEndDate = null;
						if( dealItem.get("dealStartDate") != null
								&& dealItem.get("dealStartDate") instanceof java.util.Date ) {
							dealStartDate = com.irt.data.Date.getInstance((java.util.Date)dealItem.get("dealStartDate")).toString();
						}
						if( dealItem.get("dealEndDate") != null
								&& dealItem.get("dealEndDate") instanceof java.util.Date ) {
							dealEndDate = com.irt.data.Date.getInstance((java.util.Date)dealItem.get("dealEndDate")).toString();
						}

						String message = handler.getMessageHandler().getMessage("ERR_UPLOAD_ORDER_ITEM_IS_PACKDEALITEM_3",
								(String)recordMap.get("itemCode"), dealStartDate, dealEndDate);

						Map<String, Object> newMap = new HashMap<String, Object>(recordMap);
						newMap.put(fieldKey_orderQty, "0");
						throw new DataPassingException("ERR_UPLOAD_ORDER_ITEM_IS_PACKDEALITEM_3", message, newMap, recordMap);
					}
				}
			}

			@Override
			public void close() {
				this.dealItemIndex = null;
				this.dealItemList = null;
			}
		};

		return packdealItemFix;
	}

	public DataLoader.Validator createOrderDetailSellingSkuValidator( final Map<String, Object> lineDefaultMap ) {
		DataLoader.Validator sellingSkuItem = new DataLoader.Validator() {
				private OrderItem oitmDB;

				@Override
				public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
					if( oitmDB == null )
						oitmDB = new OrderItem( handler );
					String itemCode = (String)recordMap.get( "itemCode" );
					Map<String, Object> conditionMap = MapUtil.getPartialMap( lineDefaultMap
							, new String[] { "organizationCode", "divisionCode", "distributionChannelCode", "partyCode" });
					conditionMap.put( "itemCode", recordMap.get("itemCode") );
					conditionMap.put( "status", "00" );

					Map<String, Object> dbRecordMap = oitmDB.getRecord( conditionMap, new String[]{ "startAvailDate", "endAvailDate" } );
					if( dbRecordMap == null || dbRecordMap.size() == 0 ) {
						Map<String, Object> dbTobeMap = new java.util.HashMap<String, Object>( recordMap );
						dbTobeMap.put( "uploadOrderQty", null );
						dbTobeMap.put( "status", Upload.STATUS_ERROR );

						String message = handler.getMessageHandler().getMessage( "ERR_INVALID_ORDERITEM_SELLINGSKU_MISSING_1", itemCode );
						throw new DataPassingException( "ERR_INVALID_ORDERITEM_SELLINGSKU_MISSING_1", message, dbTobeMap, recordMap );
					} else {
						com.irt.data.Date startAvailDate = (com.irt.data.Date)dbRecordMap.get( "startAvailDate" );
						com.irt.data.Date endAvailDate = (com.irt.data.Date)dbRecordMap.get( "endAvailDate" );
						com.irt.data.Date currDate = com.irt.data.Date.getInstance( handler.getTimeZone() );
						if( !(currDate.before(endAvailDate) && (currDate.after(startAvailDate ) || currDate.toString().equals(startAvailDate.toString()))) ) {
							Map<String, Object> dbTobeMap = new java.util.HashMap<String, Object>( recordMap );
							dbTobeMap.put( "uploadOrderQty", null );
							dbTobeMap.put( "status", Upload.STATUS_ERROR );

							String message = handler.getMessageHandler().getMessage( "ERR_INVALID_ORDERITEM_SELLINGSKU_DATERANGE_1", itemCode );
							throw new DataPassingException( "ERR_INVALID_ORDERITEM_SELLINGSKU_DATERANGE_1", message, dbTobeMap, recordMap );
						}
					}
				}

				@Override
				public void close() {
					if( oitmDB != null )
						oitmDB = null;
				}
			};

		return sellingSkuItem;
	}

	public DataLoader.Validator createSellingSkuValidator( final Map<String, Object> lineDefaultMap ) {

		DataLoader.Validator sellingSkuItem = new DataLoader.Validator() {
			private List<Map<String, Object>> itmList;
			private Map<String, Integer> itmIndex;

			private List<Map<String, Object>> oitmList;
			private Map<String, Integer> oitmIndex;

			private List<Map<String, Object>> oitmDchList;
			private Map<String, Integer> oitmDchIndex;

			private List<Map<String, Object>> oitmOfcList;
			private Map<String, Integer> oitmOfcIndex;

			private List<Map<String, Object>> oitmGrpList;
			private Map<String, Integer> oitmGrpIndex;

			private List<Map<String, Object>> oitmOrgList;
			private Map<String, Integer> oitmOrgIndex;

			private Item itm;
			private OrderItem oitm;

			private synchronized Map<String,Object> generateConditionMap(Map<String,Object> recordMap, Map<String,Object> defaultMap) throws DataException {
				Map<String, Object> conditionMap = MapUtil.getPartialMap(recordMap,
						new String[] { "organizationCode", "divisionCode", "partyCode", "distributionChannelCode", "officeCode", "groupCode" });
				conditionMap.put("status", "00");

				if( !conditionMap.containsKey("organizationCode") ) {
					if( lineDefaultMap.containsKey("organizationCode") )
						conditionMap.put("organizationCode", lineDefaultMap.get("organizationCode"));
				}
				if( !conditionMap.containsKey("distributionChannelCode") ) {
					if( lineDefaultMap.containsKey("distributionChannelCode") )
						conditionMap.put("distributionChannelCode", lineDefaultMap.get("distributionChannelCode"));
				}
				if( !conditionMap.containsKey("divisionCode") ) {
					if( lineDefaultMap.containsKey("divisionCode") )
						conditionMap.put("divisionCode", lineDefaultMap.get("divisionCode"));
				}
				try {
					table.getFieldMap().get("divisionCode").validate(conditionMap);
					table.getFieldMap().get("organizationCode").validate(conditionMap);
					if( !"0".equals(conditionMap.get("distributionChannelCode")) ) {
						table.getFieldMap().get("distributionChannelCode").validate(conditionMap);
					}
				} catch( FieldException fieldEx ) {
					throw handler.createDataException(fieldEx, recordMap);
				}

				if( "0".equals(conditionMap.get("distributionChannelCode")) )
					conditionMap.remove("distributionChannelCode");
				if( "0".equals(conditionMap.get("officeCode")) )
					conditionMap.remove("officeCode");
				if( "0".equals(conditionMap.get("groupCode")) )
					conditionMap.remove("groupCode");
				if( "0".equals(conditionMap.get("partyCode")) )
					conditionMap.remove("partyCode");

				return conditionMap;
			}

			private final int ORG = 1;
			private final int DCH = 2;
			private final int OFC = 3;
			private final int GRP = 4;
			private final int PTY = 5;

			private synchronized void loadData( SQLHandler handler, Map<String,Object> conditionMap, int hierLevel ) throws SQLException, DataException {
				if( hierLevel == PTY ) {
					if( oitm == null )
						oitm = new OrderItem(handler);
					conditionMap.put("displayInd", "Y");

					oitmList = oitm.getRecords(conditionMap, new String[] { "itemCode", "officeCode", "groupCode", "partyCode" });

					if( oitmList != null ) {
						oitmIndex = new HashMap<String, Integer>();
						for( int i = 0; i < oitmList.size(); i++ ) {
							Map<String, Object> map = oitmList.get(i);
							oitmIndex.put((String)map.get("itemCode"), i);
						}
					}
				} else {
					if( hierLevel == DCH ) {
						String[] primary = new String[]{ "itemCode", "organizationCode", "distributionChannelCode" };
						oitmDchList = getOrderItemListByGroupBy(conditionMap, primary);
						if( oitmDchList != null )
							oitmDchIndex = getOrderItemIndex(oitmDchList, primary);
					} else if( hierLevel == OFC ) {
						String[] primary = new String[]{ "itemCode", "organizationCode", "distributionChannelCode", "officeCode" };
						oitmOfcList = getOrderItemListByGroupBy(conditionMap, primary);
						if( oitmOfcList != null )
							oitmOfcIndex = getOrderItemIndex(oitmOfcList, primary);
					} else if( hierLevel == GRP ) {
						String[] primary = new String[]{ "itemCode", "organizationCode", "distributionChannelCode", "officeCode", "groupCode" };
						oitmGrpList = getOrderItemListByGroupBy(conditionMap, primary);
						if( oitmGrpList != null )
							oitmGrpIndex = getOrderItemIndex(oitmGrpList, primary);
					} else {
						String[] primary = new String[]{ "itemCode", "organizationCode" };
						oitmOrgList = getOrderItemListByGroupBy(conditionMap, primary);
						if( oitmOrgList != null )
							oitmOrgIndex = getOrderItemIndex(oitmOrgList, primary);
					}
				}
			}
			private Map<String,Integer> getOrderItemIndex(List<Map<String,Object>> oitmList, String[] primary ) {
				HashMap<String, Integer> oitmIndex = new HashMap<String, Integer>();
				if( oitmList != null ) {
					for( int i = 0; i < oitmList.size(); i++ ) {
						Map<String, Object> map = oitmList.get(i);
						StringBuffer pk = new StringBuffer();
						for( String key : primary ) {
							pk.append(map.get(key));
							pk.append(";");
						}
						pk.deleteCharAt(pk.length()-1);
						oitmIndex.put(pk.toString(), i);
					}
				}
				return oitmIndex;
			}

//			private List<Map<String,Object>> createOrderItemListByGroupBy(List<Map<String,Object>> list, Map<String,Object> conditionMap, String[] fieldKeys ) throws SQLException {
//				if( oitm == null )
//					oitm = new OrderItem(handler);
//
//				if( list == null )
//					list = new ArrayList<Map<String,Object>>();
//
//				List<Object> _groupKeyList = MapUtil.extractValueList(list, "_groupKeys");
//				if( _groupKeyList == null || _groupKeyList.size() == 0 ) {
//					conditionMap.put("displayInd", "Y");
//					conditionMap.put(Condition.GROUPING_CONDITIONKEY, fieldKeys );
//					List<Map<String,Object>> recordList = oitm.getRecords(conditionMap, fieldKeys);
//					MapUtil.pushConstantKeyValues(recordList, "_groupKeys", fieldKeys);
//					list.addAll(recordList);
//				} else {
//					String[] currKey = null;
//					for( Object _gkeys : _groupKeyList ) {
//						String[] gkkeys = (String[])_gkeys;
//						if( currKey == null )
//							currKey = gkkeys;
//
//						if( !fieldKeys.equals(gkkeys) ) {
//							conditionMap.put("displayInd", "Y");
//							conditionMap.put(Condition.GROUPING_CONDITIONKEY, fieldKeys );
//							List<Map<String,Object>> recordList = oitm.getRecords(conditionMap, fieldKeys);
//							String[] _groupKeys = MapUtil.getPartialMap(conditionMap, fieldKeys).values().toArray(new String[0]);
//							MapUtil.pushConstantKeyValues(recordList, "_groupKeys", _groupKeys);
//							list.addAll(recordList);
//						}
//					}
//				}
//
//				return list;
//			}

			private List<Map<String,Object>> getOrderItemListByGroupBy( Map<String,Object> conditionMap, String[] groupKeys ) throws SQLException {
				if( oitm == null )
					oitm = new OrderItem(handler);

				conditionMap.put("displayInd", "Y");
				conditionMap.put(Condition.GROUPING_CONDITIONKEY, groupKeys );
				conditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");
				if( conditionMap.containsKey("itemCode") )
					conditionMap.remove("itemCode");
				return oitm.getRecords(conditionMap, groupKeys);
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				Map<String,Object> conditionMap = generateConditionMap(recordMap, lineDefaultMap);

				int hierLevel = ORG;
				if( conditionMap.get("partyCode") != null ) {
					hierLevel = PTY;
				} else if( conditionMap.get("groupCode") != null ) {
					hierLevel = GRP;
				} else if( conditionMap.get("officeCode") != null ) {
					hierLevel = OFC;
				} else if( conditionMap.get("distributionChannelCode") != null ) {
					hierLevel = DCH;
				}
				loadData(handler, conditionMap, hierLevel);

				switch(hierLevel) {
					case DCH:
						if( recordMap != null ) {
							String itemCode = (String)recordMap.get("itemCode");
							String organizationCode = (String)recordMap.get("organizationCode");
							String distributionChannelCode = (String)recordMap.get("distributionChannelCode");
							String pk = itemCode + ";" + organizationCode + ";" + distributionChannelCode;

							if( oitmDchIndex == null || !oitmDchIndex.containsKey(pk) ) {
								throw new DataException("ERR_ISNOT_SELLINGSKU", handler.getMessageHandler().getMessage("ERR_ISNOT_SELLINGSKU", pk), recordMap);
							}
						}
					break;
				case PTY:
					if( recordMap != null ) {
						String itemCode = (String)recordMap.get("itemCode");
						String organizationCode = (String)recordMap.get("organizationCode");
						String distributionChannelCode = (String)recordMap.get("distributionChannelCode");
						String officeCode = (String)recordMap.get("officeCode");
						String groupCode = (String)recordMap.get("groupCode");
						String partyCode = (String)recordMap.get("partyCode");
						String pk = itemCode + ";" + organizationCode + ";" + distributionChannelCode + ";" + officeCode+";"+groupCode + ";"+ partyCode;

						if( oitmIndex == null || !oitmIndex.containsKey(itemCode) ) {
							throw new DataException("ERR_ISNOT_SELLINGSKU", handler.getMessageHandler().getMessage("ERR_ISNOT_SELLINGSKU", pk), recordMap);
						}
					}
					break;
				case OFC:
					if( recordMap != null ) {
						String itemCode = (String)recordMap.get("itemCode");
						String organizationCode = (String)recordMap.get("organizationCode");
						String distributionChannelCode = (String)recordMap.get("distributionChannelCode");
						String officeCode = (String)recordMap.get("officeCode");
						String pk = itemCode + ";" + organizationCode + ";" + distributionChannelCode + ";" + officeCode;

						if( oitmOfcIndex == null || !oitmOfcIndex.containsKey(pk) ) {
							throw new DataException("ERR_ISNOT_SELLINGSKU", handler.getMessageHandler().getMessage("ERR_ISNOT_SELLINGSKU", pk), recordMap);
						}
					}
					break;
				case GRP:
					if( recordMap != null ) {
						String itemCode = (String)recordMap.get("itemCode");
						String organizationCode = (String)recordMap.get("organizationCode");
						String distributionChannelCode = (String)recordMap.get("distributionChannelCode");
						String officeCode = (String)recordMap.get("officeCode");
						String groupCode = (String)recordMap.get("groupCode");
						String pk = itemCode + ";" + organizationCode + ";" + distributionChannelCode + ";" + officeCode+";"+groupCode;

						if( oitmGrpIndex == null || !oitmGrpIndex.containsKey(pk) ) {
							throw new DataException("ERR_ISNOT_SELLINGSKU", handler.getMessageHandler().getMessage("ERR_ISNOT_SELLINGSKU", pk), recordMap);
						}
					}
					break;
				case ORG:
					if( recordMap != null ) {
						String itemCode = (String)recordMap.get("itemCode");
						String organizationCode = (String)recordMap.get("organizationCode");
						String pk = itemCode + ";" + organizationCode;

						if( oitmOrgIndex == null || !oitmOrgIndex.containsKey(pk) ) {
							throw new DataException("ERR_ISNOT_SELLINGSKU", handler.getMessageHandler().getMessage("ERR_ISNOT_SELLINGSKU", pk), recordMap);
						}
					}
					break;
				}
			}

			@Override
			public void close() {
				this.itmIndex = null;
				this.itmList = null;
				this.oitmList = null;
				this.oitmIndex = null;
				if( oitm == null )
					oitm = null;
				if( itm == null )
					itm = null;
			}
		};

		return sellingSkuItem;
	}

	public class DataPassingException extends DataException {
		private Map originalMap;

		public DataPassingException( String errorKey, String message, Map dbTobeMap, Map originalMap ) {
			super(errorKey, message, dbTobeMap);
			this.originalMap = originalMap;
		}

		public Map getOriginalMap() {
			return originalMap;
		}

		public Map getModifiedMap() {
			return super.getRecordMap();
		}
	}
}
