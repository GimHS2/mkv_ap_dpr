/*
 *	File Name:	OrderDetail.java
 *	Version:	2.1.16
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2026/04/30		2.1.16	쿼리튜닝
 *	yjkdev21	2026/03/31		2.1.15	TRUNC(SYSDATE) -> timezone 에 맞게 변경
 *	jbaek		2020/06/30		2.1.14	Revise Order Feature.
 *	hankalam	2020/06/30		2.1.14	OrderDetailQuery() : dangerousInd 항목 추가
 *	jbaek		2020/04/30		2.1.13	mview snapshot에서부터 최근 발주까지 감안한 수량 계산.
 *	jbaek		2019/09/30		2.1.12	updateLineNo 추가
 *	hankalam	2019/07/31		2.1.12	Freegoods 관련 항목 추가
 *	jbaek		2019/07/30		2.1.11	deleteWithLineNoUpdate 추가, brand추가, SalesUnit IMTS사용
 *	hankalam	2019/06/28		2.1.11	Item Price 수정
 *	jbaek		2019/05/30		2.1.11	StopItem, PackDeal 추가
 *	jbaek		2019/01/30		2.1.10	salesUnit 추가
 *	jbaek		2018/04/30		2.1.9	ItemMasterExtra 추가
 *	hankalam	2017/11/30		2.1.8	getShortageItem() 추가
 *	hankalam	2017/08/31		2.1.8	OrderDetailQuery() : itemDisplayInd 추가
 *	jbaek		2017/06/30		2.1.8	shelfLife, caseCount 수정, consumerEANCode 추가
 *	hankalam	2017/02/28		2.1.7	OrderDetailQuery() : formatCSEQty, plantInd 추가
 *										Plant Exclusion 구분 기능 추가
 *	song7981	2016/05/20		2.1.6	Item Price 추가
 *	song7981	2016/04/25		2.1.5	OrderDetailQuery()에 shelfLife 추가
 *										order Download 시 caseCount 추가
 *	song7981	2016/02/29		2.1.4	simulation 시 plant comment 가져오도록 수정
 *	jbaek		2014/09/30		2.1.3	Product Hierarchy Level 기능 개발
 *	jbaek		2013/01/30		2.1.2	PIPO 기능  개발, LineItemStatus 기능 개발
 *	lsinji		2009/12/11		2.1.1	STATUS_NORMAL, STATUS_SAP_ADDED, STATUS_SAP_DELETED 추가
 *										OrderDetailQuery()에 detailStatus 추가
 *	lsinji		2009/06/30		2.1.0	DPR_ORDER_DTL 기준, DPR_ORDER_ITEM 기준으로 쿼리 할 수 있도록 변경
 *	guksm		2008/09/26		2.0.0	create
 *
**/

package com.irt.dpr;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFSheetConditionalFormatting;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataWriter;
import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;
import com.irt.rbm.RBMSystem;
import com.irt.sql.ConditionQueryBuffer;
import com.irt.sql.ConditionalQueryableField;
import com.irt.sql.Joinable;
import com.irt.sql.JoinableImpl;
import com.irt.sql.JoinableImplBK;
import com.irt.sql.JoinableImplBS;
import com.irt.sql.JoinableWrapper;
import com.irt.sql.NestedJoinable;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryBufferValid;
import com.irt.sql.QueryFactory;
import com.irt.sql.Queryable;
import com.irt.sql.QueryableField;
import com.irt.sql.QueryableFieldImpl;
import com.irt.sql.QueryableFieldImplBK;
import com.irt.sql.QueryableFieldImplBS;
import com.irt.sql.QueryableFieldWrapper;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.util.SS;
import com.irt.util.SSDataWriter;
import com.irt.util.StringUtil;

/**
 *
 */
public class OrderDetail extends com.irt.rbm.ManipulableManagerImpl {//@formatter:off
	public final static String ITEM						= "ITEM";
	public final static String ORDER						= "ORD";

	public final static int CHILD_LINENUMBER_NORMAL		= 0;
	public final static int CHILD_LINENUMBER_PIPO_FIRST	= 1;
	public final static int CHILD_LINENUMBER_PIPO_SECOND	= 2;

	public final static String ITEMREF_NORMAL				= "NO";
	public final static String ITEMREF_PIPO_ORIGINAL		= "OG";
	public final static String ITEMREF_PIPO_PO_ONLY		= "PO";
	public final static String ITEMREF_PIPO_PI_ONLY		= "PI";
	public final static String ITEMREF_PIPO_PIPO_BOTH		= "PP";
	public final static String ITEMREF_REPLACEMENT		= "RP";
	public final static String ITEMREF_PIPO_CHILD_DELETED	= "DC";

	public final static String STATUS_NORMAL				= "00";
	public final static String STATUS_SAP_ADDED			= "SP";
	public final static String STATUS_SAP_DELETED			= "DE";

	public final static String CONDKEY_NO_IMP_TABLE_IND = "_noIMP_tbl";
	public final static String CONDKEY_ORD_NO_OITM_COND_IND = "_ORD_noOITM_cnd";

	private final static int DEFAULT_ORDERCASEQTY		= 1;
	private final static Table table = Schema.findTable( Schema.DPR_ORDER_DTL );
	private final static QueryFactory factory = new QueryFactory( new OrderDetailQuery() );

	public OrderDetail( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map createPrimary( String orderKey, String lineNumber ) {
		Map map = new java.util.TreeMap();

		map.put( "orderKey", orderKey );
		map.put( "lineNumber", lineNumber );

		return map;
	}

	@Override
	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		return (deleteWithLineNoUpdate((String)primaryMap.get("orderKey"), new String[]{(String)primaryMap.get("lineNumber")}) > 0);
	}

	public int deleteWithLineNoUpdate( String orderKey, String[] delLineNumbers ) throws SQLException, DataException {
		PreparedStatement del_pstmt = null;
		del_pstmt = handler.getConnection().prepareStatement(
				"DELETE DPR_ORDER_DTL WHERE ORDERKEY = ? AND TRUNC(LINE_NO, -1) = TRUNC( ?, -1)"
		);

		int delCount = 0, upgCount = 0;
		try {
			for( int i = delLineNumbers.length - 1; i >= 0; i-- ) {
				del_pstmt.setString( 1, orderKey );
				del_pstmt.setString( 2, delLineNumbers[i] );
				delCount += del_pstmt.executeUpdate();
			}
			if( delCount > 0 ) {
				upgCount = updateLineNo(orderKey);
				com.irt.dpr.util.Loggers.business.debug( "{}: {}", orderKey, "DEL: " + delCount + "UPG: "+ upgCount );
			}
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx );
		} finally {
			try { del_pstmt.close(); } catch( Exception ex ) {}
			com.irt.dpr.util.Loggers.business.debug( "{}: {}", orderKey, "end." );
		}

		return delLineNumbers.length;
	}

	public int updateLineNo( String orderKey ) throws DataException, SQLException {
		PreparedStatement upg_pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_ORDER_DTL DTL"
				+ " SET DTL.LINE_NO = (ROWNUM*10 + MOD(DTL.LINE_NO, 10))"
				+ " WHERE DTL.ORDERKEY = ?"
				+ " AND EXISTS ("
				+ "		SELECT	NULL"
				+ "		FROM	(SELECT ORDERKEY, LINE_NO, CHILD_LINE_NO FROM DPR_ORDER_DTL SB ORDER BY ORDERKEY, LINE_NO) SB"
				+ "		WHERE	DTL.ORDERKEY = SB.ORDERKEY"
				+ "			AND DTL.LINE_NO = SB.LINE_NO"
				+ " )"
		);

		int upgCount = -1;
		try {
			upg_pstmt.setString( 1, orderKey );
			return upgCount = upg_pstmt.executeUpdate();
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx );
		} finally {
			try { upg_pstmt.close(); } catch( Exception ex ) {}
				com.irt.dpr.util.Loggers.business.debug( "{}: {}", orderKey, "UPG: "+ upgCount );
		}
	}

	public int deleteZeroOrderQty( String orderKey ) throws DataException, SQLException {
		PreparedStatement del_pstmt = handler.getConnection().prepareStatement(
				"DELETE DPR_ORDER_DTL WHERE ORDERKEY = ? AND ORDERQTY = 0"
		);

		int delCount = -1;
		try {
			del_pstmt.setString( 1, orderKey );
			return delCount = del_pstmt.executeUpdate();
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx );
		} finally {
			try { del_pstmt.close(); } catch( Exception ex ) {}
			com.irt.dpr.util.Loggers.business.debug( "{}: {}", orderKey, "DEL: " + delCount );
			if( delCount > 0 ) {
				updateLineNo(orderKey);
			}
		}
	}

	public int getDetailCount( String orderKey ) throws SQLException {
		String query = "SELECT COUNT(*) FROM vwDPR_ORDER ODTL"
				+ "	WHERE ORDERKEY = ?";

		return SQLManager.getInt( handler, query, orderKey );
	}

	public static Map<String, Object> createUniq( String orderKey, String lineNumber, String itemCode, String itemCodeConfirmed, String itemRefInd ) {
		Map<String, Object> map = new java.util.TreeMap<String, Object>();

		map.put( "orderKey", orderKey );
		map.put( "lineNumber", lineNumber );
		map.put( "itemCode", itemCode );
		map.put( "itemCodeConfirmed", itemCodeConfirmed );
		map.put( "itemRefInd", itemRefInd );

		return map;
	}

	public int getNextLineNumber( String orderKey ) throws SQLException {
		return SQLManager.getInt( handler
				, "SELECT NVL(MAX(TRUNC(LINE_NO, -1)), 0) + 10 FROM DPR_ORDER_DTL WHERE ORDERKEY = ?", new Object[] { orderKey } );
	}

	public List<Map<String, Object>> getShortageItem( String orderKey, String displayLanguage ) throws SQLException {
		String queryBuffer = "SELECT ODTL.ITEMCD \"itemCode\", ODTL.ITEMCD_CNF \"itemCodeConfirmed\", ODTL.CONS_EAN \"itemConsumerEANCodeCNF\", IMTD.ITEMNAME \"itemName\""
				+ ", IMT.SHELFLIFE/365 \"shelfLife\", ODTL.UOM \"uom\", NVL(ODTL.ORDERQTY, 0) - NVL(ODTL.SIMULATION_ORDERQTY, 0) \"qty\""
				+ " FROM DPR_ORDER_DTL ODTL, DPR_ITEM_MASTER IMT, DPR_ITEM_MASTER_DESC IMTD"
				+ " WHERE ODTL.ORDERKEY = ? AND NVL(ODTL.SIMULATION_ORDERQTY, 0) < NVL(ODTL.ORDERQTY, 0)"
				+ " AND IMT.ITEM_CD(+) = ODTL.ITEMCD"
				+ " AND IMTD.ITEMCD(+) = ODTL.ITEMCD AND IMTD.LANGCD(+) = ?";
		Object[] bindVars = new String[] { orderKey, displayLanguage };

		return SQLManager.getRecordList( handler, queryBuffer, bindVars );
	}

//@formatter:off
	static class OrderDetailQuery extends com.irt.sql.QueryableImpl {
		OrderDetailQuery() {
			super( Schema.findTable(Schema.DPR_ORDER_ITEM) );

			QueryBufferValid notWant_oitmCond = new QueryBufferValid.ConditionTrue(CONDKEY_ORD_NO_OITM_COND_IND);
			QueryableField[] notWant_oitmCondFields = this.getQueryableFieldArray(new String[] {
				"organizationCode", "partyCode", "distributionChannelCode", "divisionCode", "countryCode"
			});
			for( int i = 0; i < notWant_oitmCondFields.length; i++ ) {
				final QueryableField theDefaultField = notWant_oitmCondFields[i];
				notWant_oitmCondFields[i] = new com.irt.sql.QueryableFieldImplAR( theDefaultField, new ConditionalQueryableField( notWant_oitmCond, new QueryableFieldWrapper(theDefaultField, false) ) );
			}
			appendBST( ORDER, notWant_oitmCondFields );
//			appendBST( ORDER, new QueryableFieldImpl(Schema.STRING, "itemCode", "ODTL.ITEMCD") );
//			appendBST( ORDER, new ConditionalQueryableField(noOITMValid
//					, new QueryableFieldWrapper( new QueryableFieldImpl(Schema.STRING, "itemCode", "ODTL.ITEMCD") , false )
//			));

			Queryable queryable_ODTL = Schema.findQueryable( Schema.DPR_ORDER_DTL );
			QueryableField[] orderFields = queryable_ODTL.getQueryableFieldArray( new String[] {
				"orderNumber", "orderVolume", "orderVolumeUnit", "orderWeight", "orderWeightUnit"
				, "orderValue", "orderTax", "orderDiscount", "orderTotal"
				, "simulationOrderQty", "simulationOrderValue", "simulationOrderTax", "simulationOrderDiscount", "simulationOrderTotal"
				, "confirmedOrderQty", "confirmedOrderValue", "confirmedOrderTax", "confirmedOrderDiscount", "confirmedOrderTotal"
				, "shipPartyCode", "soldPartyCode"
				, "price", "priceCurrency", "itemRefInd", "consumerEANCode", "brandCode", "brandName"
				, "infoDeliveryStatus", "infoDeliveryOpenQty", "infoDeliveryIntraQty", "infoDeliveryCompQty", "plantInd", "itemDisplayInd"
				, "rgRmnMonth", "rgRmnDay", "rgRmnMaxQty", "rgRmnMaxUomQty"
				, "inputTotalQty", "inputTotalValue", "simulationTotalQty", "simulationTotalValue"
				, "freegoodsQty", "freegoodsRatio", "freegoodsInd"
			} );
			for( int i = 0; i < orderFields.length; i++ )
				orderFields[i] = new QueryableFieldWrapper( orderFields[i], false );
			append( orderFields );

			QueryableField[] orderBSTFields = queryable_ODTL.getQueryableFieldArray( new String[] {
				"parentOrderNumber", "parentReviseModCount", "parentReviseHbrdSeqId", "reviseHbrdSeqId", "reviseHbrdContent"
				, "reviseChangeIndex", "boardNumber"
			} );
			for( int i = 0; i < orderBSTFields.length; i++ )
				orderBSTFields[i] = new QueryableFieldWrapper( orderBSTFields[i], false );
			appendBST( ORDER, orderBSTFields );

			QueryableField[] orderConditionableFields = queryable_ODTL.getQueryableFieldArray(new String[] {
				"parentReviseStatus", "reviseHelpType", "revisingBaseDate", "reviseStatus", "soldPartyCode", "parentOrderKey", "origOrderKey"
				, "reviseStatus", "revChgedInd", "revOrderNumber"
			});
			for( int i = 0; i < orderConditionableFields.length; i++ )
				orderConditionableFields[i] = new QueryableFieldWrapper( orderConditionableFields[i], true );
			appendBST( ORDER, orderConditionableFields );


			JoinableImpl tbl_ORDTL = new JoinableImpl("ORDTL", "DPR_ORDER_DTL", "ORDTL.ORDERKEY(+) = ODTL.ORDERKEY AND ORDTL.LINE_NO(+) = ODTL.LINE_NO");
			append( new QueryableFieldWrapper(queryable_ODTL.getQueryableField("orderKey"), false) );
			append( new QueryableField[] {
				  new QueryableFieldImpl( Schema.STRING, "detailStatus", "ODTL.DETAIL_STATUS" )
//				, new QueryableFieldImpl( Schema.INTEGER, "lineNumber", "CAST(DECODE(ODTL.DETAIL_STATUS,'DE',NULL,ODTL.LINE_NO) AS INT)" )
				, new QueryableFieldImpl( Schema.INTEGER, "lineNumber", "ODTL.LINE_NO" )
				, new QueryableFieldImpl( Schema.INTEGER, "orderQty", "CAST(DECODE(ODTL.DETAIL_STATUS,'DE',NULL,ODTL.ORDERQTY) AS INT)" )
				, new QueryableFieldImpl( Schema.STRING, "lineStatus", "ORDTL.STATUS", tbl_ORDTL )
				, new QueryableFieldImpl( Schema.INTEGER, "reviseSimInputQty", "ORDTL.REVSIM_IPTQTY", tbl_ORDTL)
				, new QueryableFieldImpl( Schema.INTEGER, "reviseSimFinalQty", "ORDTL.REVSIM_FINQTY", tbl_ORDTL)
				, new QueryableFieldImpl( Schema.STRING, "reviseSimFinalShortInd", "(CASE WHEN ORDTL.REVSIM_FINQTY < ORDTL.REVSIM_IPTQTY THEN 'Y' ELSE 'N' END)", tbl_ORDTL)
				, new QueryableFieldImpl( Schema.STRING, "reviseBeforeCnfUom", "ORDTL.REVBF_CNFUOM", tbl_ORDTL)
				, new QueryableFieldImpl( Schema.STRING, "reviseAfterCnfUom", "ORDTL.UOM", tbl_ORDTL)
				, new QueryableFieldImpl( Schema.INTEGER, "reviseBeforeCnfQty", "ORDTL.CONFIRMED_ORDERQTY", tbl_ORDTL)
				, new QueryableFieldImpl( Schema.INTEGER, "reviseAfterCnfQty", "NVL(ORDTL.SIMULATION_ORDERQTY,0)", tbl_ORDTL)
			} );
			appendBST( ORDER, queryable_ODTL.getQueryableFieldArray(
				new String[] {
					"packSize", "uom", "simulationUOM", "infoUOM", "childLineNumber"
					, "formatQty", "formatCSEQty", "formatPCQty", "formatDozenQty"
					, "formatSimulationQty", "formatSimulationPCQty", "formatSimulationDozenQty"
					, "formatConfirmedQty", "formatConfirmedPCQty", "formatConfirmedDozenQty"
				} )
			);

			QueryableField[] packdealFields = queryable_ODTL.getQueryableFieldArray(
					new String[] { "packdealDiscountRate" ,"packdealDisplaySeq", "packdealSimPriceFirst", "packdealSimUomFirst"
							, "pdRmnMonth", "pdRmnDay", "pdRmnMaxQty", "pdRmnMaxUomQty" }
			);
			for( int i = 0; i < packdealFields.length; i++ )
				packdealFields[i] = new QueryableFieldWrapper( packdealFields[i], true );
			appendBST( ORDER, packdealFields );

			Joinable tbl_PLNK = new JoinableImpl( "PLNK", "DPR_PARTY_LINK"
					, "PLNK.LINKTYPE IN ( 'WE', 'SH' )" );
			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES"
					, "PTYS.PARTYCD = PLNK.PARTYCD AND ODTL.SHIP_PARTYCD = PLNK.LINK_PARTYCD"
							+ " AND PTYS.ORGANIZATIONCD = PLNK.ORGANIZATIONCD AND PTYS.DIST_CHANNELCD = PLNK.DIST_CHANNELCD"
							+ " AND PTYS.DIVISIONCD = PLNK.DIVISIONCD"
							+ " AND PTYS.PARTYCD = OITM.PARTYCD AND PTYS.ORGANIZATIONCD = OITM.ORGANIZATIONCD"
							+ " AND PTYS.DIST_CHANNELCD = OITM.DIST_CHANNELCD AND PTYS.DIVISIONCD = OITM.DIVISIONCD", tbl_PLNK );
			Joinable tbl_PTYS_LNK = new JoinableImpl( "PTYS_LNK", "DPR_PARTY_SALES"
					, "PTYS_LNK.PARTYCD = PLNK.LINK_PARTYCD AND PTYS_LNK.ORGANIZATIONCD = PTYS.ORGANIZATIONCD AND PTYS_LNK.DIST_CHANNELCD = PTYS.DIST_CHANNELCD"
							+ " AND PTYS_LNK.DIVISIONCD = PTYS.DIVISIONCD AND PTYS_LNK.STATUS = '00'", tbl_PTYS );

			appendBST( ORDER, Schema.makeItemEANQueryableFields( "IMEAN", "OITM", false ) );
			appendBST( ORDER, Schema.makeItemEANQueryableFields( "IMEAN_CNF", "ODTL", true ) );


			JoinableImpl tbl_IMTEXT = new JoinableImpl( "IMTEXT", "DPR_ITEM_MASTER_EXTRA"
					, "IMTEXT.ITEMCD(+) = ODTL.ITEMCD_CNF AND IMTEXT.ORGANIZATIONCD(+) = ODTL.ORGANIZATIONCD" );
			appendBST( ORDER, new QueryableField[] {
				  new QueryableFieldImpl( Schema.STRING, "uomNameLocal", "IMTEXT.UOM_NAME_LOCAL", tbl_IMTEXT )
				, new QueryableFieldImpl( Schema.STRING, "itemExtraDesc", "IMTEXT.ITEM_NAME", tbl_IMTEXT )
				, new QueryableFieldImpl( Schema.STRING, "itemExtraCate", "IMTEXT.ITEM_CATE", tbl_IMTEXT )
				, new QueryableFieldImpl( Schema.STRING, "itemExtraAbbrev", "IMTEXT.ITEM_ABBREV", tbl_IMTEXT )
				, new QueryableFieldImpl( Schema.STRING, "itemExtraSpec", "IMTEXT.ITEM_SPEC", tbl_IMTEXT )
				, new QueryableFieldImpl( Schema.INTEGER, "caseCount",
						"(SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB WHERE SB.ITEMCD(+) = OITM.ITEMCD AND SB.UOM_CD(+) = 'CSE')" )
			} );

			appendBST( ORDER, new QueryableFieldImpl[]{
				  new QueryableFieldImpl( Schema.STRING, "inputPackSize", "(SELECT PACKSIZE FROM DPR_ITEM_MASTER_UOM WHERE ITEMCD(+) = ODTL.ITEMCD AND UOM_CD(+) = ODTL.UOM)")
				, new QueryableFieldImpl( Schema.STRING, "simulationPackSize", "(SELECT PACKSIZE FROM DPR_ITEM_MASTER_UOM WHERE ITEMCD(+) = ODTL.ITEMCD AND UOM_CD(+) = ODTL.SIMULATION_UOM)")
			});

			Joinable tbl_IMTS = new JoinableImpl( "IMTS", "DPR_ITEM_MASTER_SALES"
					, "IMTS.ITEMCD(+) = OITM.ITEMCD AND IMTS.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND IMTS.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD" );
			Joinable tbl_IMT = new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMT.ITEM_CD(+) = OITM.ITEMCD" );
			Joinable tbl_IMT_CNF = new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMT.ITEM_CD(+) = ODTL.ITEMCD_CNF" );
			appendBST( ORDER, new QueryableField[] {
				  new QueryableFieldImpl( Schema.STRING, "officeCode", "ODTL.OFFICECD" )
				, new QueryableFieldImpl( Schema.STRING, "itemCode", "ODTL.ITEMCD" )
				, new QueryableFieldImpl( Schema.DATE, "orderDate", "ODTL.ORDDATE" )
				, new QueryableFieldImpl( Schema.STRING, "orderStatus", "ODTL.ORDER_STATUS" )
				, new QueryableFieldImpl( Schema.STRING, "orderValue", "ODTL.ORDERVALUE" )
				, new QueryableFieldImpl( Schema.STRING, "itemCodeConfirmed", "ODTL.ITEMCD_CNF" )
				, new QueryableFieldImpl( Schema.STRING, "salesUnit", "IMTS.SALES_UNIT", tbl_IMTS )
				, new QueryableFieldImpl( Schema.STRING, "shelfLife", "IMT.SHELFLIFE/365", tbl_IMT )
				, new QueryableFieldImpl( Schema.STRING, "simulationKey", "ODTL.SIMULATIONKEY" )
				/*
				, new QueryableFieldImpl( Schema.CODE, "plantInd", "DECODE( PNITM.PARTYCD, NULL, 'N', 'Y' )"
						, new JoinableImpl( "PNITM"
							, "vwDPR_PLANT_EXCL_ITEM"
							, "ODTL.ITEMCD = PNITM.ITEMCD(+) AND ODTL.ORGANIZATIONCD = PNITM.ORGANIZATIONCD(+)"
									+ " AND ODTL.DIST_CHANNELCD = PNITM.DIST_CHANNELCD(+) AND ODTL.DIVISIONCD = PNITM.DIVISIONCD(+)"
									+ " AND ODTL.COUNTRYCD = PNITM.COUNTRYCD(+) AND ODTL.SHIP_PARTYCD = PNITM.PARTYCD(+)") )
									*/

				, new QueryableFieldImpl( Schema.CODE, "plantInd", "DECODE( PTYS_LNK.DELIVERY_PLANT, PNITM.PLANTCD, 'Y', 'N' )"
						, new JoinableImpl( "PNITM"
											, "vwDPR_PLANT_EXCL_ITEM"
							, "ODTL.ITEMCD = PNITM.ITEMCD(+) AND ODTL.ORGANIZATIONCD = PNITM.ORGANIZATIONCD(+)"
									+ " AND ODTL.DIST_CHANNELCD = PNITM.DIST_CHANNELCD(+) AND ODTL.DIVISIONCD = PNITM.DIVISIONCD(+)"
									+ " AND ODTL.COUNTRYCD = PNITM.COUNTRYCD(+) AND ODTL.PARTYCD = PNITM.PARTYCD(+)"
									+ " AND ODTL.SHIP_PARTYCD = PNITM.SHIP_PARTYCD(+)"
							, tbl_PTYS_LNK) )
				, new QueryableFieldImpl( Schema.CODE, "itemEANCode", "IMUBS.EANCODE", "DPR_ITEMUOM_EANCODE"
										, new JoinableImpl( "IMUBS", "DPR_ITEM_MASTER_UOM", "IMUBS.ITEMCD(+) = IMT.ITEM_CD AND IMUBS.UOM_CD(+) = IMT.BASE_UOM"
												, new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMT.ITEM_CD(+) = ODTL.ITEMCD" )) )
				, new QueryableFieldImpl( Schema.CODE, "itemEANCodeCNF", "IMUBS_CNF.EANCODE", "DPR_ITEMUOM_EANCODE"
										, new JoinableImpl( "IMUBS_CNF", "DPR_ITEM_MASTER_UOM", "IMUBS_CNF.ITEMCD(+) = IMT.ITEM_CD AND IMUBS_CNF.UOM_CD(+) = IMT.BASE_UOM"
												, new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMT.ITEM_CD(+) = ODTL.ITEMCD_CNF" )) )
				, new QueryableFieldImplBK( Schema.STRING, "itemName"
						, "(SELECT SB.ITEMNAME FROM DPR_ITEM_MASTER_DESC SB WHERE SB.ITEMCD = ODTL.ITEMCD AND SB.LANGCD = NVL(?, 'en'))"
						, "displayLanguage" )
				, new QueryableFieldImplBK( Schema.STRING, "itemNameConfirmed"
						, "(SELECT SB.ITEMNAME FROM DPR_ITEM_MASTER_DESC SB WHERE SB.ITEMCD = ODTL.ITEMCD_CNF AND SB.LANGCD = NVL(?, 'en'))"
						, "displayLanguage" )
				, new QueryableFieldImpl( Schema.INTEGER, "pipoSimulationOrderQty"
						, "DECODE( ODTL.ITEMREF_IND, 'OG', LEAD((SELECT SUM(SIMULATION_ORDERQTY) FROM DPR_ORDER_DTL SB"
								+ " WHERE SB.ORDERKEY = ODTL.ORDERKEY AND SB.ITEMCD = ODTL.ITEMCD AND SB.ITEMREF_IND = ODTL.ITEMREF_IND AND SB.CHILD_LINE_NO IN( 1,2)), 1)"
								+ " OVER(ORDER BY ODTL.LINE_NO), ODTL.SIMULATION_ORDERQTY )" )
				, new QueryableFieldImpl( Schema.DOUBLE, "pipoSimulationOrderValue"
						, "DECODE( ODTL.ITEMREF_IND, 'OG', LEAD((SELECT SUM(SIMULATION_ORDERVALUE) FROM DPR_ORDER_DTL SB"
								+ " WHERE SB.ORDERKEY = ODTL.ORDERKEY AND SB.ITEMCD = ODTL.ITEMCD AND SB.ITEMREF_IND = ODTL.ITEMREF_IND AND SB.CHILD_LINE_NO IN( 1,2)), 1)"
								+ " OVER(ORDER BY ODTL.LINE_NO), ODTL.SIMULATION_ORDERVALUE )" )
				, new QueryableFieldImpl( Schema.DOUBLE, "pipoConfirmedOrderValue"
						, "DECODE( ODTL.ITEMREF_IND, 'OG', LEAD((SELECT SUM(CONFIRMED_ORDERVALUE) FROM DPR_ORDER_DTL SB"
								+ " WHERE SB.ORDERKEY= ODTL.ORDERKEY AND SB.ITEMCD = ODTL.ITEMCD AND SB.ITEMREF_IND = ODTL.ITEMREF_IND AND SB.CHILD_LINE_NO IN (1,2)), 1)"
								+ " OVER(ORDER BY ODTL.LINE_NO), ODTL.CONFIRMED_ORDERVALUE )" )
				, new QueryableFieldImpl( Schema.STRING, "masterSalesUnit"
						, "(SELECT SB.SALES_UNIT FROM DPR_ITEM_MASTER SB WHERE SB.ITEM_CD = ODTL.ITEMCD)" )
				, new QueryableFieldImpl( Schema.STRING, "detailStatus", "ODTL.DETAIL_STATUS" )
			} );

			QueryBufferValid queryValid_dangerous = new QueryBufferValid.Condition( "dangerousPlant", "dangerousNumber" );
			Joinable tbl_IMSP = new JoinableImplBS (
					  new JoinableImplBK( "IMTP", "DPR_ITEM_MASTER_PLANT", "IMTP.ITEMCD(+) = ODTL.ITEMCD AND IMTP.PLANT(+) = ? AND IMTP.LOADING_GRP(+) = ?"
							, new String[] { "dangerousPlant", "dangerousNumber" } )
					, ITEM
					, new JoinableImplBK( "IMTP", "DPR_ITEM_MASTER_PLANT", "IMTP.ITEMCD(+) = OITM.ITEMCD AND IMTP.PLANT(+) = ? AND IMTP.LOADING_GRP(+) = ?"
							, new String[] { "dangerousPlant", "dangerousNumber" } )
			);
			appendCND( queryValid_dangerous,
					new QueryableFieldImplBK( Schema.STRING, "dangerousInd", "DECODE(IMTP.LOADING_GRP, ?, 'Y', 'N')", "dangerousNumber",  tbl_IMSP )
			);

			//
			// BASE: ITEM

			Joinable tbl_STIMRT = new JoinableImpl( "STIMRT", "vwDPR_STOPITEM_CFGRLT"
					, "STIMRT.ITEMCD(+) = OITM.ITEMCD AND STIMRT.PARTYCD(+) = OITM.PARTYCD"
					+ " AND STIMRT.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND STIMRT.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD" );
			appendBST( ITEM, Schema.makeStopItemQueryableFields("STIMRT", tbl_STIMRT) );

			Joinable tbl_ITM = new JoinableImpl( "ITM", "DPR_ITEM"
					, "ITM.COUNTRYCD(+) = OITM.COUNTRYCD AND ITM.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD"
						+" AND ITM.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD AND ITM.ITEMCD(+) = OITM.ITEMCD" );
			Joinable tbl_OCL = new JoinableImpl( "OCL", "DPR_ORDCLOSE", "OCL.ORGANIZATIONCD(+) = ITM.ORGANIZATIONCD AND OCL.BRANDCD(+) = ITM.BRANDCD", tbl_ITM );
			appendBST( ITEM, Schema.makeCloseItemQueryableFields("OCL", tbl_OCL) );



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
			inner_PDR.appendCondition( "'Y' = (CASE WHEN PDR.DEAL_STARTDATE <= TRUNC(pkCustom.fCurrentDate(PDR.ORGANIZATIONCD))"
					+ " AND PDR.DEAL_ENDDATE+12/24 > TRUNC(pkCustom.fCurrentDate(PDR.ORGANIZATIONCD))"
					+ " AND PDR.DEAL_STOP_IND != 'Y' THEN 'Y' ELSE 'N' END)" );

			Joinable tbl_PDR = new JoinableImpl( "PDR", "("+inner_PDR.toString()+")"
					, "PDR.ITEMCD(+) = OITM.ITEMCD AND PDR.PARTYCD(+) = OITM.PARTYCD"
					+ " AND PDR.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND PDR.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD" );
			appendBST( ITEM, Schema.makePackdealDateQueryableFields("PDR", tbl_PDR) );

			appendBST( ITEM, Schema.makeItemEANQueryableFields( "IMEAN", "OITM", false ) );
			appendBST( ITEM, new QueryableField[] {
				  new QueryableFieldImplBK( Schema.STRING, "itemName"
							, "(SELECT SB.ITEMNAME FROM DPR_ITEM_MASTER_DESC SB WHERE SB.ITEMCD = OITM.ITEMCD AND SB.LANGCD = ?)"
							, "displayLanguage" )
				, new QueryableFieldImpl( Schema.STRING, "shelfLife", "IMT.SHELFLIFE/365", tbl_IMT )// shelfLife(packsize)
				, new QueryableFieldImpl( Schema.STRING, "salesUnit", "IMTS.SALES_UNIT", tbl_IMTS )// shelfLife(packsize)
				, new QueryableFieldImpl( Schema.INTEGER, "caseCount",
						"(SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB WHERE SB.ITEMCD(+) = OITM.ITEMCD AND SB.UOM_CD(+) = 'CSE')" )
				, new QueryableFieldImpl( Schema.STRING, "itemCode", "OITM.ITEMCD" )
				, new QueryableFieldImpl( Schema.CODE, "itemEANCode", "IMUBS.EANCODE", "DPR_ITEMUOM_EANCODE",
												new JoinableImpl( "IMUBS", "DPR_ITEM_MASTEDR_UOM", "IMUBS.ITEMCD(+) = IMT.ITEM_CD AND IMUBS.UOM_CD(+) = IMT.BASE_UOM"
														, new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMT.ITEM_CD(+) = OITM.ITEMCD" )) )
				//, new QueryableFieldImpl( Schema.STRING, "uom", "NVL(ODTL.UOM, 'CSE')" )
				, getFormatedOrderQuantityField( "formatQty", "CSE", DEFAULT_ORDERCASEQTY )
				, getFormatedOrderQuantityField( "formatCSEQty", "CSE", DEFAULT_ORDERCASEQTY )
				, getFormatedOrderQuantityField( "formatPCQty", "PC", DEFAULT_ORDERCASEQTY )
				, getFormatedOrderQuantityField( "formatDozenQty", "DZ", DEFAULT_ORDERCASEQTY )
				, getFormatedOrderQuantityField( "formatSimulationQty", "CSE", DEFAULT_ORDERCASEQTY )
				, getFormatedOrderQuantityField( "formatSimulationPCQty", "PC", DEFAULT_ORDERCASEQTY )
				, getFormatedOrderQuantityField( "formatSimulationDozenQty", "DZ", DEFAULT_ORDERCASEQTY )
				, getFormatedOrderQuantityField( "formatConfirmedQty", "CSE", DEFAULT_ORDERCASEQTY )
				, getFormatedOrderQuantityField( "formatConfirmedPCQty", "PC", DEFAULT_ORDERCASEQTY )
				, getFormatedOrderQuantityField( "formatConfirmedDozenQty", "DZ", DEFAULT_ORDERCASEQTY )
				, new QueryableFieldImpl( Schema.STRING, "detailStatus", "ODTL.DETAIL_STATUS" )
				, new QueryableFieldImpl( Schema.CODE, "brandCode", "ITM.BRANDCD", tbl_ITM )
				, new QueryableFieldImplBK( Schema.STRING, "brandName"
					, "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_TYPE = 'BR' AND SB.LANGCD = NVL(?, 'en')"
							+ " AND SB.MASTER_CD = ITM.BRANDCD)"
					, "displayLanguage", tbl_ITM )
			} );

			QueryBufferValid valid_salesUnit = new QueryBufferValid.ConditionTrue("useSuggestSalesUnitInput");
			appendBST( ITEM, ConditionalQueryableField.makeConditionalQueryableFields(valid_salesUnit, new QueryableFieldImpl[]{
				  new QueryableFieldImpl( Schema.STRING, "uom", "COALESCE(ODTL.UOM"
//						+ ", (SELECT SALES_UNIT FROM DPR_ITEM_MASTER_SALES SB WHERE SB.ITEMCD = OITM.ITEMCD AND SB.ORGANIZATIONCD = OITM.ORGANIZATIONCD AND SB.DIST_CHANNELCD = OITM.DIST_CHANNELCD)"
						+ ", (SELECT REGEXP_SUBSTR(LISTAGG(UOM_CD,';') WITHIN GROUP(ORDER BY DECODE(UOM_CD,IMTS.SALES_UNIT,0,'CSE',1,'PCK',2,'DZ',3,'PC',4,5 )),'[^;]+',1,1)"
								  + " FROM DPR_ITEM_MASTER_UOM WHERE ITEMCD = OITM.ITEMCD)"
						+ ", PTYS.ALLOW_UOM)",
						new JoinableWrapper(new JoinableImpl( "PTYS", "DPR_PARTY_SALES", "ORD.PARTYCD = PTYS.PARTYCD AND ORD.ORGANIZATIONCD = PTYS.ORGANIZATIONCD "
								+ "AND ORD.DIST_CHANNELCD = PTYS.DIST_CHANNELCD AND ORD.DIVISIONCD = PTYS.DIVISIONCD" ,
						new JoinableImplBK( "ORD", "DPR_ORDER", "ORD.ORDER_KEY = ?", "orderKey"))
								, tbl_IMTS )
								)
				, new QueryableFieldImpl( Schema.STRING, "packSize", "NVL(ODTL.PACKSIZE"
						+ ", (SELECT REGEXP_SUBSTR(LISTAGG(PACKSIZE,';') WITHIN GROUP(ORDER BY DECODE(UOM_CD,IMTS.SALES_UNIT,0,'CSE',1,'PCK',2,'DZ',3,'PC',4,5 )),'[^;]+',1,1)"
							+ " FROM DPR_ITEM_MASTER_UOM WHERE ITEMCD = OITM.ITEMCD))"
						, tbl_IMTS )
			}) );
			appendBST( ITEM, new QueryableField[]{
				  new QueryableFieldImpl( Schema.STRING, "uom", "COALESCE(ODTL.UOM, PTYS.ALLOW_UOM)",
						new JoinableImpl( "PTYS", "DPR_PARTY_SALES", "ORD.PARTYCD = PTYS.PARTYCD AND ORD.ORGANIZATIONCD = PTYS.ORGANIZATIONCD "
								+ "AND ORD.DIST_CHANNELCD = PTYS.DIST_CHANNELCD AND ORD.DIVISIONCD = PTYS.DIVISIONCD" ,
						new JoinableImplBK( "ORD", "DPR_ORDER", "ORD.ORDER_KEY = ?", "orderKey")) )
				, new QueryableFieldImpl( Schema.STRING, "packSize", "NVL(ODTL.PACKSIZE, (SELECT PACKSIZE FROM DPR_ITEM_MASTER_UOM WHERE ITEMCD = OITM.ITEMCD AND UOM_CD = 'CSE'))" )
			});

			QueryBufferValid querybufValid = new QueryBufferValid.Condition( "countryHierarchyCondition" );
			appendCND( querybufValid, new QueryableField[] {
				  new QueryableFieldImpl( Schema.STRING, "displayCategoryCode", "ITM_PH.DISPLAY_PCATECD" )
				, new QueryableFieldImpl( Schema.STRING, "displayCategoryName", "ITM_PH.DISPLAY_PCATENAME" )
				, new QueryableFieldImpl( Schema.STRING, "productCategoryCode", "ITM_PH.PCATECD" )
			} );
			appendCND( new QueryBufferValid.Join( true, querybufValid, new QueryBufferValid.Basis(ITEM) ), new QueryableField[] {
				new QueryableFieldImpl( Schema.STRING, "displayPCateName", "ITM_PHD.DISPLAY_PCATENAME" )
				, new QueryableFieldImpl( Schema.STRING, "parentDisplayPCateName", "ITM_PHD.PARENT_DISPLAY_PCATENAME" )
			} );
			appendCND( new QueryBufferValid.Join( true, querybufValid, new QueryBufferValid.Basis(ORDER) ), new QueryableField[] {
				new QueryableFieldImpl( Schema.STRING, "displayPCateNameCNF", "ICNF_PHD.DISPLAY_PCATENAME" )
				, new QueryableFieldImpl( Schema.STRING, "parentDisplayPCateNameCNF", "ICNF_PHD.PARENT_DISPLAY_PCATENAME" )
			} );
		}

		private void appendBST( String basisKey, QueryableField[] fields, Joinable joinable ) {
			QueryableField[] fieldsBS = new QueryableField[ fields.length ];
			for( int i = 0; i < fields.length; i++ )
				if( fields[i] != null )
					fieldsBS[i] = new QueryableFieldImplBS( fields[i], basisKey, joinable );

			appendBST( basisKey, fieldsBS );
		}

		@Override
		public boolean appendTable( QueryBuffer querybuf ) {
			if( querybuf instanceof ConditionQueryBuffer ) {
				ConditionQueryBuffer conditionQueryBuffer = (ConditionQueryBuffer)querybuf;

				Map<String, ? extends Object> conditionMap = conditionQueryBuffer.getConditionMap();

				String basisValue = (String)conditionMap.get( Condition.BASIS_CONDITIONKEY );
				String baseConditionQuery = null;
				String hierarchyConditionQuery = null;
				String PHDConditionQuery = null;
				String ICNF_PHDConditionQuery = null;
				String shipPartyCode = (String) conditionMap.get( "shipPartyCode" );
				if( ITEM.equals(basisValue) ) {
					baseConditionQuery =  "ODTL.SOLD_PARTYCD(+) = OITM.PARTYCD AND ODTL.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD"
							+ " AND ODTL.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD AND ODTL.DIVISIONCD(+) = OITM.DIVISIONCD"
							+ " AND ODTL.ITEMCD(+) = OITM.ITEMCD";
					hierarchyConditionQuery = "ITM_PH.ITEMCD(+) = OITM.ITEMCD"
							+ " AND ITM_PH.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND ITM_PH.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD";

					PHDConditionQuery = "ITM_PHD.ITEMCD(+) = OITM.ITEMCD"
							+ " AND ITM_PHD.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND ITM_PHD.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD";

					conditionQueryBuffer.findCondition( "orderKey", "ODTL.ORDERKEY(+)" );

					boolean usePltRcvDispItem = RBMSystem.getSystemEnvBool("DPR", "Feature;usePltRcvDispItem", false);
					if( usePltRcvDispItem ) {
						ConditionQueryBuffer rcvbuffer = new ConditionQueryBuffer(conditionQueryBuffer.getConditionMap());

						//recovery
						conditionQueryBuffer.appendCondition( "PTRCV.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD" );
						conditionQueryBuffer.appendCondition( "PTRCV.ITEMCD(+) = OITM.ITEMCD" );

						conditionQueryBuffer.appendDataWithAlias( "PTRCV.RECOVERY", "recovery" );

						rcvbuffer.appendDataWithAlias( "PTYS.ORGANIZATIONCD", "ORGANIZATIONCD" );
						rcvbuffer.appendDataWithAlias( "PTYS.PARTYCD", "PARTYCD" );
						rcvbuffer.appendDataWithAlias( "RCV.PLANTCD", "PLANTCD" );
						rcvbuffer.appendDataWithAlias( "RCV.ITEMCD", "ITEMCD" );
						rcvbuffer.appendDataWithAlias( "RCV.RECOVERY", "RECOVERY" );

						rcvbuffer.appendTableWithAlias( "DPR_PARTY_SALES", "PTYS" );
						rcvbuffer.appendTableWithAlias( "DPR_PLANT_RCV", "RCV" );

						rcvbuffer.appendCondition( "RCV.PLANTCD = PTYS.DELIVERY_PLANT" );
						rcvbuffer.appendCondition( "RCV.ORGANIZATIONCD = PTYS.ORGANIZATIONCD" );
						rcvbuffer.appendCondition( "PTYS.PARTYCD IN ( SELECT LINK_PARTYCD FROM DPR_PARTY_LINK WHERE LINKTYPE = 'WE' AND LINK_PARTYCD = ? )", shipPartyCode );

						conditionQueryBuffer.appendTable( rcvbuffer, "PTRCV" );
						if( shipPartyCode != null && shipPartyCode.length() > 0 ) {
							conditionQueryBuffer.appendCondition( "PTRCV.PARTYCD(+) = ?", shipPartyCode );
						}
					} else {
						conditionQueryBuffer.appendDataWithAlias( "NULL", "recovery" );
					}

					//String shipPartyCode = (String) conditionMap.get( "shipPartyCode" );
					if( shipPartyCode != null && shipPartyCode.length() > 0 ) {
//						 conditionQueryBuffer.appendTableWithAlias( "vwDPR_PLANT_EXCL_ITEM", "PTITME",
//								"PTITME.PARTYCD(+) = OITM.PARTYCD AND PTITME.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD AND PTITME.DIVISIONCD(+) = OITM.DIVISIONCD" +
//								" AND PTITME.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD AND PTITME.ITEMCD(+) = OITM.ITEMCD" );
						querybuf.appendTable( OrderItem.getPlantExclusionQueryBuffer(conditionQueryBuffer.getConditionMap()), "PTITME",
											  "PTITME.PARTYCD(+) = OITM.PARTYCD AND PTITME.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD"
											  + " AND PTITME.DIVISIONCD(+) = OITM.DIVISIONCD AND PTITME.DIST_CHANNELCD(+) = OITM.DIST_CHANNELCD"
											  + " AND PTITME.COUNTRYCD(+) = OITM.COUNTRYCD AND PTITME.ITEMCD(+) = OITM.ITEMCD" );
						conditionQueryBuffer.appendCondition( "PTITME.SHIP_PARTYCD(+) = ?", shipPartyCode );
						conditionQueryBuffer.appendCondition( "PTITME.ITEMCD IS NULL" );

						Object availableDate = conditionQueryBuffer.getConditionValue( "availableDate" );
						if( availableDate != null ) {
							conditionQueryBuffer.appendCondition( "(OITM.STARTAVAIL_DATE IS NULL OR OITM.STARTAVAIL_DATE <= ?)", availableDate );
							conditionQueryBuffer.appendCondition( "(OITM.ENDAVAIL_DATE IS NULL OR OITM.ENDAVAIL_DATE > ?)", availableDate );

							String itemDisplayInd = " 'Y' = (SELECT"
								+ " (CASE WHEN IMTSS.STATUS_CD IS NULL AND LTRIM(ITM.SALES_STATUS_FROM,'0') IS NULL THEN"
										+ " CASE WHEN IMTSC.STATUS_CD IS NULL AND LTRIM(ITM.CHAIN_STATUS_FROM,'0') IS NULL THEN 'Y'"
										+ " WHEN IMTSC.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.CHAIN_STATUS_FROM,'00000000',NULL,ITM.CHAIN_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
										+ " ELSE 'Y' END"
								+ " ELSE (CASE WHEN IMTSS.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.SALES_STATUS_FROM,'00000000',NULL,ITM.SALES_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
										+ " WHEN IMTSC.DISPLAY_IND = 'N' AND TO_DATE(DECODE(ITM.CHAIN_STATUS_FROM,'00000000',NULL,ITM.CHAIN_STATUS_FROM),'YYYYMMDD') <= ? THEN 'N'"
										+ " ELSE 'Y' END)"
								+ " END)"
								+ " FROM DPR_ITEM ITM, DPR_ITEM_MASTER_STATUS IMTSS, DPR_ITEM_MASTER_STATUS IMTSC"
								+ " WHERE IMTSS.STATUS_CD(+) = ITM.SALES_STATUS AND IMTSC.STATUS_CD(+) = ITM.CHAIN_STATUS"
										+ " AND ITM.ORGANIZATIONCD = OITM.ORGANIZATIONCD AND ITM.DIST_CHANNELCD = OITM.DIST_CHANNELCD AND ITM.ITEMCD = OITM.ITEMCD)";

							conditionQueryBuffer.appendCondition( itemDisplayInd, new Object[]{availableDate, availableDate, availableDate} );
						}
					}
				} else {

//					String noOITMInd = (String)conditionQueryBuffer.getConditionValue(CONDKEY_NO_ORDERITEM_IND, Schema.STRING);
//					if( "Y".equals(noOITMInd) ) {
//						baseConditionQuery = null;
//					} else {
						baseConditionQuery =  "OITM.PARTYCD(+) = ODTL.SOLD_PARTYCD AND OITM.ORGANIZATIONCD(+) = ODTL.ORGANIZATIONCD"
								+ " AND OITM.DIST_CHANNELCD(+) = ODTL.DIST_CHANNELCD AND OITM.DIVISIONCD(+) = ODTL.DIVISIONCD"
								+ " AND OITM.ITEMCD(+) = ODTL.ITEMCD";
//					}

					hierarchyConditionQuery = "ITM_PH.ITEMCD(+) = ODTL.ITEMCD"
							+ " AND ITM_PH.ORGANIZATIONCD(+) = ODTL.ORGANIZATIONCD AND ITM_PH.DIST_CHANNELCD(+) = ODTL.DIST_CHANNELCD";

					PHDConditionQuery = "ITM_PHD.ITEMCD(+) = ODTL.ITEMCD"
							+ " AND ITM_PHD.ORGANIZATIONCD(+) = ODTL.ORGANIZATIONCD AND ITM_PHD.DIST_CHANNELCD(+) = ODTL.DIST_CHANNELCD";

					ICNF_PHDConditionQuery = "ICNF_PHD.ITEMCD(+) = ODTL.ITEMCD_CNF"
							+ " AND ICNF_PHD.ORGANIZATIONCD(+) = ODTL.ORGANIZATIONCD AND ICNF_PHD.DIST_CHANNELCD(+) = ODTL.DIST_CHANNELCD";

					conditionQueryBuffer.findCondition( "orderKey", "ODTL.ORDERKEY" );

					if( com.irt.dpr.Country.isFeature((String)conditionMap.get("organizationCode"), "useFreegoods") ) {
						conditionQueryBuffer.findCondition( "freegoodsInd", "ODTL.FREEGOODS_IND" );
						conditionQueryBuffer.findConditionNumber( "simulationTotalQty", "ODTL.SIMULATION_TOTAL_QTY", Condition.CONDTYPE_EQUALS_NONE );
					}

					//recovery ( placeOrder의 count 쿼리 시 포함안되도록 shipPartyCode조건 추가)
					if( shipPartyCode != null && shipPartyCode.length() > 0 ) {

						ConditionQueryBuffer rcvbuffer = new ConditionQueryBuffer(conditionQueryBuffer.getConditionMap());
						conditionQueryBuffer.appendCondition( "PTRCV.ORGANIZATIONCD(+) = OITM.ORGANIZATIONCD" );
						conditionQueryBuffer.appendCondition( "PTRCV.ITEMCD(+) = OITM.ITEMCD" );

						conditionQueryBuffer.appendDataWithAlias( "PTRCV.RECOVERY", "recovery" );
						conditionQueryBuffer.appendDataWithAlias( "PTRCV.PLANTCD", "plantCode" );

						rcvbuffer.appendDataWithAlias( "PTYS.ORGANIZATIONCD", "ORGANIZATIONCD");
						rcvbuffer.appendDataWithAlias( "PTYS.PARTYCD", "PARTYCD" );
						rcvbuffer.appendDataWithAlias( "RCV.PLANTCD", "PLANTCD" );
						rcvbuffer.appendDataWithAlias( "RCV.ITEMCD", "ITEMCD" );
						rcvbuffer.appendDataWithAlias( "RCV.RECOVERY", "RECOVERY" );

						rcvbuffer.appendTableWithAlias( "DPR_PARTY_SALES", "PTYS" );
						rcvbuffer.appendTableWithAlias( "DPR_PLANT_RCV", "RCV" );

						rcvbuffer.appendCondition( "RCV.PLANTCD = PTYS.DELIVERY_PLANT" );
						rcvbuffer.appendCondition( "RCV.ORGANIZATIONCD = PTYS.ORGANIZATIONCD" );
						rcvbuffer.appendCondition( "PTYS.PARTYCD IN ( SELECT LINK_PARTYCD FROM DPR_PARTY_LINK WHERE LINKTYPE = 'WE' AND LINK_PARTYCD = ? )", shipPartyCode );

						conditionQueryBuffer.appendTable(rcvbuffer, "PTRCV");
					}
				}

				String noIMPInd = (String)conditionQueryBuffer.getConditionValue(CONDKEY_NO_IMP_TABLE_IND, Schema.STRING);
				boolean useManualItemPrice = Country.isFeature( (String)conditionMap.get("organizationCode"), "useManualItemPrice" );
				if( !"Y".equals(noIMPInd) && useManualItemPrice ) {
					//itmePrice
					conditionQueryBuffer.appendDataWithAlias( "IMP.PRICE", "itemPrice" );
					conditionQueryBuffer.appendCondition( "OITM.ORGANIZATIONCD = IMP.ORGANIZATIONCD(+)" );
					conditionQueryBuffer.appendCondition( "OITM.DIST_CHANNELCD = IMP.DIST_CHANNELCD(+)" );
//						conditionQueryBuffer.appendCondition( "ODTl.OFFICECD = IMP.OFFICECD(+)" );
//						conditionQueryBuffer.appendCondition( "ODTL.GROUPCD = IMP.GROUPCD(+)" );
					conditionQueryBuffer.appendCondition( "OITM.PARTYCD = IMP.PARTYCD(+)" );
					conditionQueryBuffer.appendCondition( "OITM.ITEMCD = IMP.ITEMCD(+)" );

					ConditionQueryBuffer pricebuffer = new ConditionQueryBuffer(conditionQueryBuffer.getConditionMap());
					pricebuffer.appendTableWithAlias( "DPR_ITEM_PRICE", "IMP" );
					pricebuffer.appendTableWithAlias( "DPR_PARTY_SALES", "PTYS" );

					pricebuffer.appendDataWithAlias( "PTYS.ORGANIZATIONCD", "ORGANIZATIONCD" );
					pricebuffer.appendDataWithAlias( "PTYS.DIST_CHANNELCD", "DIST_CHANNELCD" );
					pricebuffer.appendDataWithAlias( "PTYS.OFFICECD", "OFFICECD" );
					pricebuffer.appendDataWithAlias( "PTYS.GROUPCD", "GROUPCD" );
					pricebuffer.appendDataWithAlias( "PTYS.PARTYCD", "PARTYCD" );
					pricebuffer.appendDataWithAlias( "IMP.ITEMCD", "ITEMCD" );
					pricebuffer.appendDataWithAlias( "MAX(PRICE) KEEP ( DENSE_RANK LAST ORDER BY DECODE(IMP.PARTYCD, '0', '0', '1' )"
							+ " || DECODE(IMP.GROUPCD, '0', '0', '1' ) || DECODE(IMP.OFFICECD, '0', '0', '1' ) || DECODE(IMP.DIST_CHANNELCD, '0', '0', '1' ) )"
							, "PRICE" );
					pricebuffer.appendCondition( "IMP.ORGANIZATIONCD = PTYS.ORGANIZATIONCD" );
					pricebuffer.appendCondition( "( (IMP.DIST_CHANNELCD =  PTYS.DIST_CHANNELCD AND IMP.OFFICECD = PTYS.OFFICECD AND IMP.GROUPCD = PTYS.GROUPCD AND IMP.PARTYCD = PTYS.PARTYCD)"
							+ " OR (IMP.DIST_CHANNELCD = PTYS.DIST_CHANNELCD AND IMP.OFFICECD = PTYS.OFFICECD AND IMP.GROUPCD = '0' AND IMP.PARTYCD = PTYS.PARTYCD)"
							+ " OR (IMP.DIST_CHANNELCD = PTYS.DIST_CHANNELCD AND IMP.OFFICECD = '0' AND IMP.GROUPCD = '0' AND IMP.PARTYCD = PTYS.PARTYCD)"
							+ " OR (IMP.DIST_CHANNELCD = '0' AND IMP.OFFICECD = '0' AND IMP.GROUPCD = '0' AND IMP.PARTYCD = PTYS.PARTYCD)"
							+ " OR (IMP.DIST_CHANNELCD = PTYS.DIST_CHANNELCD AND IMP.OFFICECD = PTYS.OFFICECD AND IMP.GROUPCD = PTYS.GROUPCD AND IMP.PARTYCD = '0')"
							+ " OR (IMP.DIST_CHANNELCD = '0' AND IMP.OFFICECD = PTYS.OFFICECD AND IMP.GROUPCD = PTYS.GROUPCD AND IMP.PARTYCD = '0')"
							+ " OR (IMP.DIST_CHANNELCD = PTYS.DIST_CHANNELCD AND IMP.OFFICECD = PTYS.OFFICECD AND IMP.GROUPCD = '0' AND IMP.PARTYCD = '0')"
							+ " OR (IMP.DIST_CHANNELCD = '0' AND IMP.OFFICECD = PTYS.OFFICECD AND IMP.GROUPCD = '0' AND IMP.PARTYCD = '0')"
							+ " OR (IMP.DIST_CHANNELCD = PTYS.DIST_CHANNELCD AND IMP.OFFICECD = '0' AND IMP.GROUPCD = '0' AND IMP.PARTYCD = '0')"
							+ " OR (IMP.DIST_CHANNELCD = '0' AND IMP.OFFICECD = '0' AND IMP.GROUPCD = '0' AND IMP.PARTYCD = '0') )" );
					pricebuffer.appendCondition( "PTYS.ORGANIZATIONCD = ?", (String)conditionMap.get("organizationCode") );
					pricebuffer.appendCondition( "PTYS.DIST_CHANNELCD = ?", (String)conditionMap.get("distributionChannelCode") );
					pricebuffer.appendCondition( "PTYS.PARTYCD = ?", (String)conditionMap.get("partyCode") );

					pricebuffer.appendGroupBy( "PTYS.ORGANIZATIONCD, PTYS.DIST_CHANNELCD, PTYS.OFFICECD, PTYS.GROUPCD, PTYS.PARTYCD, IMP.ITEMCD" );

					conditionQueryBuffer.appendTable( pricebuffer, "IMP" );
				} else {
					conditionQueryBuffer.appendDataWithAlias( "NULL", "itemPrice" );
				}

				conditionQueryBuffer.appendTableWithAlias( "vwDPR_ORDER", "ODTL", baseConditionQuery );

				Map<String, Object> hierarchyCondition = (Map<String, Object>)conditionMap.get( "countryHierarchyCondition" );
				if( hierarchyCondition != null ) {
					NestedJoinable nested_PH = new com.irt.dpr.ProductHierarchy.NestedJoinable( "ITM_PH", hierarchyCondition );
					Joinable joinable_PH = new JoinableWrapper( nested_PH, hierarchyConditionQuery );
					joinable_PH.appendTable( querybuf );

					if( ITEM.equals( basisValue ) ) {
						NestedJoinable nested_PHD = new com.irt.dpr.ProductHierarchy.NestedPHDJoinable( "ITM_PHD", hierarchyCondition );
						Joinable joinable_PHD = new JoinableWrapper( nested_PHD, PHDConditionQuery );
						joinable_PHD.appendTable( querybuf );
					} else {
						NestedJoinable nested_ICNF_PHD = new com.irt.dpr.ProductHierarchy.NestedPHDJoinable( "ICNF_PHD", hierarchyCondition );
						Joinable joinable_ICNF_PHD = new JoinableWrapper( nested_ICNF_PHD, ICNF_PHDConditionQuery );
						joinable_ICNF_PHD.appendTable( querybuf );
					}
				}
			}

			if( querybuf.existTableAlias("PDR") )
				querybuf.appendHint( "USE_HASH(PDR)" );

			return super.appendTable( querybuf );
		}

		public QueryableField getFormatedOrderQuantityField( String fieldKey, String uomCode, int orderCaseQty ) {
			Joinable tbl_UOM = new JoinableImpl( "IMU_Q", "DPR_ITEM_MASTER_UOM", "IMU_Q.ITEMCD = OITM.ITEMCD AND IMU_Q.UOM_CD = 'CSE'" );

			return new QueryableFieldImpl( Schema.INTEGER, fieldKey, orderCaseQty + " * IMU_Q.PACKSIZE"
					+ " / (SELECT DECODE(SB.PACKSIZE, 0, 1, SB.PACKSIZE) FROM DPR_ITEM_MASTER_UOM SB"
						+ " WHERE SB.ITEMCD(+) = ODTL.ITEMCD_CNF AND SB.UOM_CD(+) = '" + uomCode + "')", tbl_UOM );
		}

		@Override
		public boolean appendCondition( ConditionQueryBuffer querybuf ) {
			boolean hasCondition = querybuf.hasConditionQuery();

			if( querybuf.hasConditionValue("odrdlvGroup") ) {
				querybuf.findConditionCode("brandCode", "ITM.BRANDCD");
				hasCondition = true;
			}

			if( querybuf.hasConditionValue("dangerousInd") ) {
				Object[] bindValues = { querybuf.getConditionValue("dangerousNumber"),  querybuf.getConditionValue("dangerousInd") };
				querybuf.appendCondition( "DECODE(IMTP.LOADING_GRP, ?, 'Y', 'N') = ?", bindValues );
				hasCondition = true;
			}
			return hasCondition;
		}
	}
//@formatter:on

	@Override
	public void write( DataWriter out, Map<String, ? extends Object> conditionMap, ColumnList columnList, int writingOption )
			throws IOException, SQLException {

		if( out instanceof SSDataWriter ) {
			int titleRowCount = com.irt.data.cols.ColumnUtility.getTitleRowCount(columnList);
			SSDataWriter sout = (SSDataWriter)out;

			org.apache.poi.ss.usermodel.CellStyle[] cellStyles = new org.apache.poi.ss.usermodel.CellStyle[columnList.getColumnCount()];

			org.apache.poi.ss.usermodel.Workbook workbook = sout.getWorkbook();
			String[] dataNotBlankFgcolor = new String[columnList.getColumnCount()];
			for( int c = 0; c < columnList.getColumnCount(); c++ ) {
				Column column = columnList.getColumn(c);
				cellStyles[c] = SS.createCellStyle(workbook, columnList.getColumn(c));

				if( column.getDataCellAttr() != null ) {
					String dataCellAttr = StringUtil.extractAttrValue(column.getDataCellAttr(), "data-cond-not-blank");
					if( dataCellAttr != null )
						dataNotBlankFgcolor[c] = dataCellAttr;
				}
			}
			sout.setCellStyles(cellStyles);

			if( workbook instanceof HSSFWorkbook ) {
				int maxrow = SpreadsheetVersion.EXCEL97.getMaxRows();
				HSSFSheet sheet = (HSSFSheet)workbook.getSheetAt(0);

				HSSFSheetConditionalFormatting condFmt = sheet.getSheetConditionalFormatting();

				for( int c = 0; c < columnList.getColumnCount(); c++ ) {
					if( dataNotBlankFgcolor[c] != null ) {
						ConditionalFormattingRule rule = condFmt.createConditionalFormattingRule(ComparisonOperator.NOT_EQUAL, "\"\"", null);
						String s_fgcolor = StringUtil.extractAttrValue(dataNotBlankFgcolor[c], "fgcolor");
						IndexedColors n_fgcolor = IndexedColors.valueOf(s_fgcolor.toUpperCase());
						PatternFormatting ptnFmt = rule.createPatternFormatting();
						ptnFmt.setFillBackgroundColor(n_fgcolor.getIndex());

						String excel_colname = SS.getColumnName(c);
						int startRow = ( 1 + titleRowCount );
						CellRangeAddress[] range = { CellRangeAddress.valueOf(excel_colname + startRow + ":" + excel_colname + maxrow) };
						condFmt.addConditionalFormatting(range, rule);
					}
				}
			} else {
				int maxrow = SpreadsheetVersion.EXCEL2007.getMaxRows();
				XSSFSheet sheet = (XSSFSheet)workbook.getSheetAt(0);

				XSSFSheetConditionalFormatting condFmt = sheet.getSheetConditionalFormatting();

				for( int c = 0; c < columnList.getColumnCount(); c++ ) {
					if( dataNotBlankFgcolor[c] != null ) {
						ConditionalFormattingRule rule = condFmt.createConditionalFormattingRule(ComparisonOperator.NOT_EQUAL, "\"\"", null);
						String s_fgcolor = StringUtil.extractAttrValue(dataNotBlankFgcolor[c], "fgcolor");
						IndexedColors n_fgcolor = IndexedColors.valueOf(s_fgcolor.toUpperCase());
						PatternFormatting ptnFmt = rule.createPatternFormatting();
						ptnFmt.setFillBackgroundColor(n_fgcolor.getIndex());

						String colname = SS.getColumnName(c);
						CellRangeAddress[] range = { CellRangeAddress.valueOf(colname + 1 + ":" + colname + maxrow) };
						condFmt.addConditionalFormatting(range, rule);
					}
				}
			}

			super.write(sout, conditionMap, columnList, writingOption);
		} else {
			super.write(out, conditionMap, columnList, writingOption);
		}
	}

	public boolean modifyWithValidate( Map<String, Object> recordMap ) throws DataException, SQLException {
		return modifyWithValidate(recordMap, null);
	}

	public boolean modifyWithValidate( Map<String, Object> recordMap, String[] fieldKeys ) throws DataException, SQLException {
		if( oitm == null )
			oitm = new OrderItem(handler);

		DataLoader.Validator validator1 = null;
		DataLoader.Validator validator2 = null;
		try {
			validator1 = oitm.createNormalOrderStopItemValidator(recordMap);
			validator2 = oitm.createNormalOrderPackDealValidator(recordMap);

			if( validator1 != null )
				validator1.validateLine(handler, recordMap);
			if( validator2 != null )
				validator2.validateLine(handler, recordMap);
		} finally {
			if( validator1 != null )
				validator1.close();
			if( validator2 != null )
				validator2.close();
		}

		return super.modify(recordMap, fieldKeys);
	}

	private OrderItem oitm = null;

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		if( oitm == null )
			oitm = new OrderItem(handler);

		DataLoader.Validator validator1 = null;
		DataLoader.Validator validator2 = null;
		try {
			validator1 = oitm.createNormalOrderStopItemValidator(recordMap);
			validator2 = oitm.createNormalOrderPackDealValidator(recordMap);

			if( validator1 != null )
				validator1.validateLine(handler, recordMap);
			if( validator2 != null )
				validator2.validateLine(handler, recordMap);
		} finally {
			if( validator1 != null )
				validator1.close();
			if( validator2 != null )
				validator2.close();
		}

		return super.regist(recordMap);
	}

	public int updateOrderItemPrice( String orderKey ) throws DataException, SQLException {
		return SQLManager.callStatementInt( handler, "BEGIN ? := pkCustom.fOrderItemPriceUpdate( ? ); END;", orderKey );
	}
}
