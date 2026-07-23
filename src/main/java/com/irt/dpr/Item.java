/*
 *	File Name:	Item.java
 *	Version:	2.2.5
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.5	getItemList() 추가
 *	hankalam	2020/06/30		2.2.4	ITEMTYPE_DANGEROUS 추가
 *	jbaek		2019/07/30		2.2.3	브랜드 검색 조건 추가.
 *	jbaek		2017/06/30		2.2.2	upc/ean 검색 기능 추가.( itemConsumerEANCode )
 *	jbaek		2013/11/30		2.2.1	Material Status Auto Update 기능 개발
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.irt.sql.ConditionQueryBuffer;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryBufferValid;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;

/*
 *
 */
public class Item extends com.irt.rbm.ManipulableManagerImpl {//@formatter:off
	public final static int IMAGESAVETYPE_BLOB			= 0x01;
	public final static int IMAGESAVETYPE_PLACE			= 0x02;

	public final static int ITEMCODE_LENGTH				= 18;
	public final static String ITEMCODE_FIXEDVALUE		= "0";

	public final static String ITEMSTATUS_NORMAL		= "00";
	public final static String ITEMSTATUS_TRADEOFF		= "99";

	public final static String ITEMTYPE_DANGEROUS		= "8";

	private final static Table table = Schema.findTable( Schema.DPR_ITEM );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ITEM );

	public Item( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( Object countryCode, Object itemCode, Object organizationCode, Object distributionChannelCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "countryCode", countryCode );
		primaryMap.put( "itemCode", itemCode );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );

		return primaryMap;
	}

	public String getDefaultItemName( String countryCode, String itemCode ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler
			, "SELECT IMTD.ITEMNAME"
					+" FROM DPR_COUNTRY CNR, DPR_ITEM_MASTER_DESC IMTD"
					+" WHERE CNR.LANGCD = IMTD.LANGCD AND CNR.COUNTRY_CD = ? AND IMTD.ITEMCD = ?"
			, new Object[] { countryCode, itemCode }
		);
	}

	public List getMaterialStatusDescriptions() throws SQLException {
		return SQLManager.getRecordList( handler, "SELECT STATUS_CD \"statusCode\", STATUS_DESC \"statusDescription\" FROM DPR_ITEM_MASTER_STATUS", new Object[] {} );
	}

	public List<Map<String, Object>> getItemList( Map<String, Object> conditionMap ) throws SQLException {
		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), new String[] { "itemCode", "itemName", "itemConsumerEANCode" } );
		querybuf.appendOrderByFieldName( "itemName" );
		querybuf.appendDistinct();

		long timemillis = System.currentTimeMillis();
		PreparedStatement pstmt = handler.getConnection().prepareStatement( querybuf.getQuery() );
		ResultSet rset = null;
		try {
			SQLManager.bindVariables( pstmt, querybuf.getBindVariables() );
			rset = pstmt.executeQuery();
			List<Map<String, Object>> list;
			list = new ArrayList<Map<String, Object>>( SQLManager.DEFAULT_LIST_INITCAPACITY );
			ResultSetMetaData meta = rset.getMetaData();
			while( rset.next()  ) {
				Map<String, Object> map = new java.util.HashMap<String, Object>( meta.getColumnCount() );

				for( int i = 1; i <= meta.getColumnCount(); i++ ) {
					String columnName = meta.getColumnName(i);

					Object obj = rset.getObject(i);
					if( obj == null ) continue;
					if( obj instanceof java.util.Date ) {
						if( columnName.endsWith("DateTime") ) {
							map.put( columnName, new com.irt.data.Timestamp( rset.getTimestamp(i) , handler.getTimeZone() ) );
							map.put( columnName + "Zone", handler.getTimeZone().getID() );
						} else
							map.put( columnName, com.irt.data.Date.getInstance( (java.util.Date)obj ) );
					} else
						map.put( columnName, obj );
				}
				list.add( map );
			}
			return( list.size() == 0 ? null : list );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
			handler.saveQuery( querybuf, System.currentTimeMillis() - timemillis );
		}
	}

	public String getName( String itemCode, String displayLanguageCode ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler
			, "SELECT ITEMNAME FROM DPR_ITEM_MASTER_DESC WHERE ITEMCD = ? AND LANGCD = ?"
			, new Object[] { itemCode, displayLanguageCode } );
	}

	public List getItemTreeList( Map<String, Object> conditionMap ) throws SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );

		String codeLength = String.valueOf( ProductHierarchy.getLength(conditionMap) );
		String pateQuery = "DECODE(LENGTH(SUBSTR(ITM.PCATECD, 1, " + codeLength + ")), "+ codeLength +", SUBSTR(ITM.PCATECD, 1, "+ codeLength + "), NULL )";
		querybuf.appendDataWithAlias( "DECODE(HIMT.PCATECD, 'x', MSTD.MASTER_CD, null, MSTD.MASTER_CD, HIMT.PCATECD)", "cateCode" );
		querybuf.appendDataWithAlias( "DECODE(HIMT.PCATECD, 'x', MSTD.MASTER_NAME, null, MSTD.MASTER_NAME, MSTD_H.MASTER_NAME)", "cateName" );
		querybuf.appendDataWithAlias( "ITM.ITEMCD", "itemCode" );
		querybuf.appendDataWithAlias( "IMTD.ITEMNAME", "itemName" );
		querybuf.appendDataWithAlias( "ITM.NEWITEM_IND", "newItemInd" );
		querybuf.appendDataWithAlias( "ITM.PROMOTION_IND", "promotionItemInd" );
		querybuf.appendDataWithAlias( "ITM.DIST_CHANNELCD", "distributionChannelCode" );
		querybuf.appendDataWithAlias( "ITM.BRANDCD", "brandCode" );

		querybuf.appendTableWithAlias( "DPR_COUNTRY", "CNR" );
		querybuf.appendTableWithAlias( "DPR_ITEM", "ITM", "CNR.COUNTRY_CD = ITM.COUNTRYCD" );
		querybuf.appendTableWithAlias( "DPR_ITEM_MASTER_DESC", "IMTD", "IMTD.ITEMCD(+) = ITM.ITEMCD" );
		querybuf.appendTableWithAlias( "DPR_MASTER_DESC", "MSTD"
				, "MSTD.MASTER_CD(+) = "+ pateQuery +" AND MSTD.MASTER_TYPE(+) = 'PC'" );
		querybuf.appendTableWithAlias( "DPR_PRODUCT_CATE", "PRC", "PRC.PCATE_CD = "+ pateQuery );
		querybuf.appendTable( ProductHierarchy.getInnerHierarchyQuery(conditionMap), "HIMT"
				, "ITM.COUNTRYCD(+) = HIMT.COUNTRYCD AND ITM.ORGANIZATIONCD(+) = HIMT.ORGANIZATIONCD "
					+ "AND ITM.DIST_CHANNELCD(+) = HIMT.DIST_CHANNELCD AND ITM.ITEMCD(+) = HIMT.ITEMCD" );
		querybuf.appendTableWithAlias( "DPR_MASTER_DESC", "MSTD_H", "MSTD_H.MASTER_CD(+) = HIMT.PCATECD AND MSTD_H.MASTER_TYPE(+) = 'PC'" );

		querybuf.findCondition( "countryCode", "ITM.COUNTRYCD" );
		querybuf.findCondition( "organizationCode", "ITM.ORGANIZATIONCD" );
		querybuf.findCondition( "distributionChannelCode", "ITM.DIST_CHANNELCD" );
		querybuf.findCondition( "displayLanguage", "IMTD.LANGCD(+)" );

		//모든 Organization의 Product Hierarchy는 English를 사용
		querybuf.appendCondition( "MSTD.LANGCD(+) = ?", "en" );
		querybuf.appendCondition( "MSTD_H.LANGCD(+) = ?", "en" );
		querybuf.findCondition( "classCode", "PRC.CLASSCD" );
		querybuf.findCondition( "newItemInd", "ITM.NEWITEM_IND" );
		querybuf.findCondition( "promotionItemInd", "ITM.PROMOTION_IND" );
		querybuf.findCondition( "status", "ITM.STATUS" );

		//too slow		querybuf.appendDataWithAlias( "NVL2(SIMHST.REGULAR_ITEM_RANK, ROW_NUMBER() OVER(ORDER BY SIMHST.REGULAR_ITEM_RANK ASC NULLS LAST), NULL)", "regularItemRank" );
		querybuf.appendDataWithAlias( "SIMHST.SIM_ITEM_CNT", "simItemCount" );
		ConditionQueryBuffer qb_SIMHST = Schema.getRegularItemQueryBuffer( conditionMap );
		querybuf.appendTable( qb_SIMHST, "SIMHST", "SIMHST.ITEMCD(+) = ITM.ITEMCD" );

		QueryBufferValid valid_upceanCode = new QueryBufferValid.Condition( "itemConsumerEANCode" );
		if( valid_upceanCode.hasValidCondition( querybuf ) ) {
			querybuf.appendTableWithAlias( "vwDPR_ITEM_EANRLT", "IMEAN", "IMEAN.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD AND IMEAN.ITEMCD(+) = ITM.ITEMCD" );
			querybuf.appendDataWithAlias( "IMEAN.ITEM_CONS_EAN", "itemConsumerEANCode" );
			querybuf.findCondition( "itemConsumerEANCode", "IMEAN.ITEM_CONS_EAN" );
		}

		Object availableDate = querybuf.getConditionValue( "availableDate" );
		if( availableDate != null ) {
			querybuf.appendCondition( "(ITM.STARTAVAIL_DATE IS NULL OR ITM.STARTAVAIL_DATE <= ?)", availableDate );
			querybuf.appendCondition( "(ITM.ENDAVAIL_DATE IS NULL OR ITM.ENDAVAIL_DATE > ?)", availableDate );
		}
		querybuf.findCondition( "itemCode", "ITM.ITEMCD" );
		querybuf.findCondition( "itemName", "IMTD.ITEMNAME" );
		querybuf.appendOrderByFieldName( "cateName" );
		querybuf.appendOrderBy( "\"cateCode\", \"itemCode\", \"itemName\" NULLS LAST" );

		return SQLManager.getRecordList( handler, querybuf );
	}

	public static String fixedItemCodeValue( String itemCode ) {
		if( itemCode == null ) return null;

		int length = itemCode.length();
		int i = ITEMCODE_LENGTH - length;
		StringBuffer buf = new StringBuffer();
		while( 0 < i-- ) {
			buf.append( ITEMCODE_FIXEDVALUE );
		}

		return buf.toString() + itemCode;
	}

	public List<Map<String, Object>> getBrands( Map<String, Object> conditionMap ) throws SQLException {
//		TableDaoManager dm = new TableDaoManager(handler, com.irt.dpr.Schema.findTable(com.irt.dpr.Schema.DPR_ITEM_MASTER_SALES),
//				com.irt.dpr.Schema.findQueryFactory(com.irt.dpr.Schema.DPR_ITEM_MASTER_SALES));

		return getRecords(conditionMap, new String[] { "brandCode", "brandName" });
	}
}
