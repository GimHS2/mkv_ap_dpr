/*
 *	File Name:	FreeGoods.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/10/30		2.2.1	registFreegoodsOrder(): 오류수정
 *	hankalam	2019/07/31		2.2.0	create
 *
 **/

package com.irt.dpr;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;
import com.irt.sql.ConditionQueryBuffer;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.sql.TableDataLoader;
import com.irt.system.SessionManager;
import com.irt.util.MessageHandler;

/*
 *
 */
public class FreeGoods extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_FREEGOODS );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_FREEGOODS );

	public final static String	ORDER_MOST_FREEGOODS			= "1";
	public final static String	ORDER_MOST_NORMAL				= "2";
	public final static String	ORDER_ONLY_NORMAL				= "3";

	public FreeGoods( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String freegoodsKey ) {
		Map<String, Object> primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "freegoodsKey", freegoodsKey );

		return primaryMap;
	}

	@Override
	public DataLoader.Loader createDataLoader( String[] fieldKeys, final Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
			, int statementType ) throws SQLException {
		return new TableDataLoader( fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType ) {
			PreparedStatement pstmt_freegoods = null;
			PreparedStatement pstmt_office = null;
			PreparedStatement pstmt_party = null;
			PreparedStatement pstmt_item = null;
			ResultSet rset;
			@Override
			public void close() {
				try { if( pstmt_freegoods != null ) pstmt_freegoods.close(); } catch( Exception ignored ) {};
				try { if( pstmt_office != null ) pstmt_office.close(); } catch( Exception ignored ) {};
				try { if( pstmt_party != null ) pstmt_party.close(); } catch( Exception ignored ) {};
				try { if( pstmt_item != null ) pstmt_item.close(); } catch( Exception ignored ) {};
				super.close();
			}

			@Override
			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				String partyCode = (String)recordMap.get( "partyCode" );
				String freegoodsKey = (String)recordMap.get( "freegoodsKey" );
				String officeCode = (String)recordMap.get( "officeCode" );
				String itemCode = (String)recordMap.get( "itemCode" );
				String organizationCode = (String)defaultMap.get( "organizationCode" );

				if( freegoodsKey != null && freegoodsKey.length() > 0 ) {
					pstmt_freegoods.setString( 1, freegoodsKey );
					rset = pstmt_freegoods.executeQuery();
					try {
						if( rset.next() ) {
							String value = rset.getString(1);
							if( value == null || value.length() == 0 ) {
								throw handler.createDataException( DataException.ERR_ERROR
										, handler.getMessageHandler().getMessage("ERR_CST_DPR_FREEGOODS_INVALID_KEY", freegoodsKey) );
							}
						}
					} finally {
						try { rset.close(); } catch( Exception ex ) {}
					}
				} else {
					recordMap.put( "freegoodsKey", createSequence() );
				}

				pstmt_office.setString( 1, officeCode );
				rset = pstmt_office.executeQuery();
				try {
					int count = rset.next() ? rset.getInt(1) : 0;
					if( count < 1 ) {
						throw handler.createDataException( DataException.ERR_ERROR
								, handler.getMessageHandler().getMessage("ERR_PNF_FIDX_DPR_PARTYSA_OFFICE") );
					}
				} finally {
					try { rset.close(); } catch( Exception ex ) {}
				}

				if( partyCode == null || partyCode.trim().length() < 1 || "0".equals(partyCode) ) {
					recordMap.put( "partyCode", "0" );
				} else {
					pstmt_party.setString( 1, organizationCode );
					pstmt_party.setString( 2, partyCode );
					pstmt_party.setString( 3, officeCode );
					rset = pstmt_party.executeQuery();
					try {
						int count = rset.next() ? rset.getInt(1) : 0;
						if( count < 1 ) {
							throw handler.createDataException( DataException.ERR_ERROR
									, handler.getMessageHandler().getMessage("ERR_PNF_FIDX_DPR_PARTYSA_PTY") );
						}
					} finally {
						try { rset.close(); } catch( Exception ex ) {}
					}
				}

				pstmt_item.setString( 1, organizationCode );
				pstmt_item.setString( 2, itemCode );
				rset = pstmt_item.executeQuery();
				try {
					int count = rset.next() ? rset.getInt(1) : 0;
					if( count < 1 ) {
						throw handler.createDataException( DataException.ERR_ERROR
								, handler.getMessageHandler().getMessage("ERR_PNF_FIDX_DPR_ITEM_ITMMST") );
					}
				} finally {
					try { rset.close(); } catch( Exception ex ) {}
				}
				return super.processLine( handler, recordMap );
			}

			@Override
			public void start( SQLHandler handler ) throws SQLException {
				Connection conn = handler.getConnection();
				pstmt_freegoods = conn.prepareStatement(
						"SELECT FREEGOODS_KEY FROM DPR_FREEGOODS WHERE FREEGOODS_KEY = ?"
						);
				pstmt_office = conn.prepareStatement(
						"SELECT COUNT(*) FROM DPR_SALES_OFFICE WHERE OFFICE_CD = ?"
						);
				pstmt_party = conn.prepareStatement(
						"SELECT COUNT(*) FROM DPR_PARTY_SALES WHERE ORGANIZATIONCD = ? AND PARTYCD = ? AND OFFICECD = ?"
						);
				pstmt_item = conn.prepareStatement(
						"SELECT COUNT(*) FROM DPR_ITEM WHERE ORGANIZATIONCD = ? AND ITEMCD = ?"
						);
			}
		};
	}

	public static Map<String, Object> createPrimary( String organizationCode, String officeCode, String partyCode, String startDate, String endDate, String itemCode ) {
		Map<String, Object> primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "officeCode", officeCode );
		primaryMap.put( "partyCode", partyCode );
		primaryMap.put( "startDate", startDate );
		primaryMap.put( "endDate", endDate );
		primaryMap.put( "itemCode", itemCode );

		return primaryMap;
	}

	public Object createSequence() throws SQLException {
		return SQLManager.getObjectValue( handler, "SELECT 'FG' || seqDPR_FREEGOODS.nextval from dual" );
	}

	public boolean lockFreegoods( String freegoodsKey ) throws SQLException {
		PreparedStatement pstmt = null;

		try {
			pstmt = handler.getConnection().prepareStatement( "SELECT FREEGOODS_KEY FROM DPR_FREEGOODS WHERE FREEGOODS_KEY = ? FOR UPDATE WAIT 120" );
			SQLManager.bindVariables( pstmt, new Object[] { freegoodsKey } );
			return pstmt.execute();
		}finally {
			if( pstmt != null ) pstmt.close();
		}
	}

	public Map<String, Object> getFreeGoods( Map<String, Object> conditionMap, String[] fieldKeys ) throws SQLException {
		Map<String, Object> _conditionMap = new java.util.HashMap<String, Object>( conditionMap );
		com.irt.data.Date today = com.irt.data.Date.getInstance( handler.getTimeZone() );
		_conditionMap.put( "startDate"+ Condition.SUFFIX_MAX_VALUE, today );
		_conditionMap.put( "startDate"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MINMAX );
		_conditionMap.put( "endDate"+ Condition.SUFFIX_MIN_VALUE, today );
		_conditionMap.put( "endDate"+ Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MINMAX );

		QueryBuffer querybuf;
		if( fieldKeys != null ) {
			querybuf = factory.setQuery( new ConditionQueryBuffer(_conditionMap), fieldKeys );
		} else {
			querybuf = factory.setQuery( new ConditionQueryBuffer(_conditionMap) );
		}
		Map<String, Object> recordMap = SQLManager.getRecordMap( handler, null, querybuf );
		if( recordMap == null || recordMap.size() < 1 ) {
			_conditionMap.put( "partyCode", "0" );

			querybuf.clear();
			if( fieldKeys != null ) {
				querybuf = factory.setQuery( new ConditionQueryBuffer(_conditionMap), fieldKeys );
			} else {
				querybuf = factory.setQuery( new ConditionQueryBuffer(_conditionMap) );
			}
			recordMap = SQLManager.getRecordMap( handler, null, querybuf );
		}
		conditionMap.put( "_condition", _conditionMap );
		return recordMap;
	}

	public int getFreeGoodsQty( Map<String, Object> conditionMap, int orderQty ) throws SQLException {
		Map<String, Object> recordMap = getFreeGoods( conditionMap, new String[] { "orderableRatio", "surplusQty" } );
		BigDecimal bdFreegoodsQty =  new BigDecimal( "0" );
		int freegoodsQty = 0;
		if( recordMap != null && recordMap.size() > 0 ) {
			int surplusQty = ((BigDecimal)recordMap.get("surplusQty")).intValue();
			BigDecimal bdOrderQty = new BigDecimal( orderQty );
			if( surplusQty > 0 ) {
				BigDecimal bdOrderableRatio = (BigDecimal)recordMap.get( "orderableRatio" );
				bdFreegoodsQty = bdOrderQty.multiply( bdOrderableRatio.divide(new BigDecimal("100")) );
				bdFreegoodsQty = bdFreegoodsQty.setScale( 0, BigDecimal.ROUND_DOWN );
				freegoodsQty = bdFreegoodsQty.intValue();
				freegoodsQty = surplusQty > freegoodsQty ? freegoodsQty : surplusQty;
			}
		}
		return freegoodsQty;
	}

	public int getSurplusOrderQty( Map<String, Object> conditionMap, int orderQty ) throws SQLException {
		Map<String, Object> recordMap = getFreeGoods( conditionMap, new String[] { "surplusQty" } );
		int freegoodsOrderQty = 0;
		if( recordMap != null && recordMap.size() > 0 ) {
			int surplusQty = ( (BigDecimal)recordMap.get("surplusQty") ).intValue();

			if( surplusQty > 0 ) {
				freegoodsOrderQty = orderQty > surplusQty ? surplusQty : orderQty;
			}
		}
		return freegoodsOrderQty;
	}

	public boolean makeFreegoodsValue( Map<String, Object> recordMap ) {
		BigDecimal bdOrgOrderQty = (BigDecimal) recordMap.get("orderQty");
		BigDecimal bdSimulationOrderQty = (BigDecimal) recordMap.get("simulationOrderQty");
		BigDecimal bdInputTotalQty = (BigDecimal) recordMap.get("inputTotalQty");
		BigDecimal bdSimulationTotalQty = (BigDecimal) recordMap.get("simulationTotalQty");
		BigDecimal bdFreegoodsRatio = (BigDecimal) recordMap.get("freegoodsRatio");

		bdOrgOrderQty = bdOrgOrderQty != null ? bdOrgOrderQty : new BigDecimal( "0" );
		bdSimulationOrderQty = bdSimulationOrderQty != null ? bdSimulationOrderQty : new BigDecimal( "0" );
		bdInputTotalQty = bdInputTotalQty != null ? bdInputTotalQty : new BigDecimal( "0" );
		bdSimulationTotalQty = bdSimulationTotalQty != null ? bdSimulationTotalQty : new BigDecimal( "0" );
		bdFreegoodsRatio = bdFreegoodsRatio != null ? bdFreegoodsRatio : new BigDecimal( "0" );

		BigDecimal bdAvailFreegoodsQty = bdInputTotalQty.subtract( bdOrgOrderQty );

		boolean hasMost = false;
		int orgOrderQty = bdOrgOrderQty.intValue();
		int simulationOrderQty = bdSimulationOrderQty.intValue();
		int inputTotalQty = bdInputTotalQty.intValue();
		int simulationTotalQty = bdSimulationTotalQty.intValue();
		double freegoodsRatio = bdFreegoodsRatio.doubleValue();
		if( freegoodsRatio <= 0 ) {
			if( "Y".equals(recordMap.get("freegoodsInd")) ) {
				recordMap.put( "freegoodsInd", "N" );
			}
			return false;
		}

		if( simulationTotalQty > 0 ) {
			if( inputTotalQty > simulationTotalQty ) {
				BigDecimal bdOrderQty = bdSimulationTotalQty.divide( new BigDecimal("1").add(bdFreegoodsRatio.divide(new BigDecimal("100"))), BigDecimal.ROUND_DOWN );
				BigDecimal bdFreegoodsQty = bdOrderQty.multiply( bdFreegoodsRatio.divide(new BigDecimal("100")) ).setScale( 0, BigDecimal.ROUND_DOWN );
				int availFreegoodsQty = bdAvailFreegoodsQty.intValue();
				int orderQty = bdOrderQty.intValue();
				int freegoodsQty = bdFreegoodsQty.intValue();

				if( freegoodsQty < 1 ) {
					if( "Y".equals(recordMap.get("freegoodsInd")) ) {
						recordMap.put( "freegoodsInd", "N" );
					}
					return false;
				}
				BigDecimal bdMostFreegoodsOrderQty;
				if( availFreegoodsQty < freegoodsQty ) {
					bdMostFreegoodsOrderQty = bdAvailFreegoodsQty.divide( bdFreegoodsRatio.divide(new BigDecimal("100")), BigDecimal.ROUND_UP );
					freegoodsQty = availFreegoodsQty;
				} else {
					bdMostFreegoodsOrderQty = bdFreegoodsQty.divide( bdFreegoodsRatio.divide(new BigDecimal("100")), BigDecimal.ROUND_UP );
				}
				orderQty = bdMostFreegoodsOrderQty.intValue();
				recordMap.put( "mostFreegoodsOrderQty", orderQty );
				recordMap.put( "mostFreegoodsQty", freegoodsQty );
				int differenceQty = simulationTotalQty - orderQty - freegoodsQty;
				orderQty = orderQty + differenceQty;

				//				int orderQty = (int) ( simulationTotalQty / ( 1.0 + freegoodsRatio / 100.0 ) );
				//				int freegoodsQty = (int) ( orderQty * freegoodsRatio / 100.0 );
				//				if( freegoodsQty < 1 ) {
				//					if( "Y".equals(recordMap.get("freegoodsInd")) ) {
				//						recordMap.put( "freegoodsInd", "N" );
				//					}
				//					return false;
				//				}
				//				recordMap.put( "mostFreegoodsOrderQty", orderQty );
				//				recordMap.put( "mostFreegoodsQty", freegoodsQty );
				//
				//				int differenceQty = simulationTotalQty - orderQty - freegoodsQty;
				//				orderQty = orderQty + differenceQty;
				recordMap.put( "mostNormalOrderQty", orderQty );
				recordMap.put( "mostNormalFreegoodsQty", freegoodsQty );
				recordMap.put( "normalOrderQty", orgOrderQty > simulationOrderQty ? simulationOrderQty : orgOrderQty );

				if( differenceQty == 0 ) {
					hasMost = false;
				} else {
					hasMost = true;
				}
			} else {
				BigDecimal bdFreegoodsQty = new BigDecimal( inputTotalQty - orgOrderQty );
				BigDecimal bdMostFreegoodsOrderQty = bdFreegoodsQty.divide( bdFreegoodsRatio.divide(new BigDecimal("100")), BigDecimal.ROUND_UP );

				recordMap.put( "mostFreegoodsOrderQty", bdMostFreegoodsOrderQty.intValue() );
				recordMap.put( "mostFreegoodsQty", inputTotalQty - orgOrderQty );
				recordMap.put( "mostNormalOrderQty", orgOrderQty );
				recordMap.put( "mostNormalFreegoodsQty", inputTotalQty - orgOrderQty );
				recordMap.put( "normalOrderQty", orgOrderQty );
				if( bdMostFreegoodsOrderQty.intValue() != orgOrderQty ) {
					hasMost = true;
				}
			}
		} else {
			if( "Y".equals(recordMap.get("freegoodsInd")) ) {
				recordMap.put( "freegoodsInd", "N" );
			}
		}
		return hasMost;
	}

	public List<Map<String, Object>> registFreegoodsOrder( String normalOrderKey, SessionManager sessionMng ) throws SQLException, DataException {
		Order db = new Order( handler );
		Map<String, Object> headerMap = db.getRecord( Order.createPrimary(normalOrderKey)
				, new String[] { "partyCode", "countryCode", "organizationCode", "distributionChannelCode", "divisionCode"
						, "shipPartyCode", "soldPartyCode", "orderType", "inDate", "inDateDefault", "freegoodsOrderWay", "simulationKey"
		} );
		String howToOrder = (String) headerMap.get( "freegoodsOrderWay" );
		String orderKey = null;
		String officeCode = (String) new Party( handler ).getFieldValue( headerMap, "officeCode" );
		List<Map<String, Object>> freegoodsList = null;

		MessageHandler msghandler = handler.getMessageHandler();
		if( howToOrder != null && !ORDER_ONLY_NORMAL.equals(howToOrder) ) {
			orderKey = db.getOrderKey();
			String simulationKey = (String) headerMap.get( "simulationKey" );
			simulationKey = simulationKey.replace( Order.NORMAL_ORDERKEY_SUFFIX, Order.FREEGOODS_ORDERKEY_SUFFIX );
			com.irt.data.Date today = com.irt.data.Date.getInstance( sessionMng.getTimeZone() );
			headerMap.put( "orderDate", today );
			headerMap.put( "updateUserId", sessionMng.getUniqId() );
			headerMap.put( "orderKey", orderKey );
			headerMap.put( "simulationKey", simulationKey );
			headerMap.put( "status", Order.STATUS_SIMULATED );
			headerMap.put( "freegoodsOrderInd", "Y" );
			headerMap.put( "parentOrderKey", normalOrderKey );
			//headerMap.remove( "freegoodsOrderWay" );
			if( !db.regist(headerMap) ) {
				throw new DataException( DataException.ERR_CANNOT_INSERT
						, msghandler.getMessage("MSG_DPR_FREEGOODS_ORDER") + " " + msghandler.getMessage(DataException.ERR_CANNOT_INSERT) );
			}
		}

		if( orderKey != null ) {
			OrderDetail detailDB = new OrderDetail( handler );
			Map<String, Object> conditionMap = new HashMap<String, Object>();
			conditionMap.put( "orderKey", normalOrderKey );
			conditionMap.put( "freegoodsInd", "Y" );
			conditionMap.put( "simulationTotalQty", null );
			conditionMap.put( "simulationTotalQty_min", 0 );
			conditionMap.put( "organizationCode", headerMap.get("organizationCode") );
			conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );
			/*String[] fieldKeys = { "lineNumber", "itemCode", "itemCodeConfirmed", "itemRefInd", "shelfLife", "uom", "orderQty"
					, "simulationOrderQty", "inputTotalQty", "simulationTotalQty", "freegoodsRatio", "freegoodsQty", "price", "packSize" };*/
			List<Map<String, Object>> detailList = detailDB.getRecords( conditionMap );

			Map<String, Object> fgConditionMap = new HashMap<String, Object>();
			fgConditionMap.put( "organizationCode", headerMap.get("organizationCode") );
			fgConditionMap.put( "officeCode", officeCode );
			fgConditionMap.put( "partyCode", headerMap.get("partyCode") );


			Map<String, Object> freegoodsDetailMap = new java.util.HashMap<String, Object>();
			freegoodsDetailMap.put( "orderKey", orderKey );
			freegoodsDetailMap.put( "updateUserId", sessionMng.getUniqId() );

			String[] mostFieldKeys = new String[2];
			if( ORDER_MOST_NORMAL.equals(howToOrder) ) {
				mostFieldKeys[0] = "mostNormalOrderQty";
				mostFieldKeys[1] = "mostNormalFreegoodsQty";
			} else if( ORDER_MOST_FREEGOODS.equals(howToOrder) ) {
				mostFieldKeys[0] = "mostFreegoodsOrderQty";
				mostFieldKeys[1] = "mostFreegoodsQty";
			}

			freegoodsList = new java.util.ArrayList<Map<String, Object>>();

			for( Map<String, Object> record : detailList ) {
				makeFreegoodsValue( record );
				String freegoodsInd = (String) record.get( "freegoodsInd" );
				Map<String, Object> normalDetailMap = Record.createMap( "orderKey", normalOrderKey );
				normalDetailMap.put( "lineNumber", record.get("lineNumber") );
				if( !"Y".equals(freegoodsInd) ) {
					normalDetailMap.put( "freegoodsInd", "N" );
					detailDB.modify( normalDetailMap, new String[] { "freegoodsInd" } );
					continue;
				}

				int orderQty = (Integer)record.get( mostFieldKeys[0] );
				normalDetailMap.put( "orderQty", orderQty );
				detailDB.modify( normalDetailMap, new String[] { "orderQty" } );

				fgConditionMap.put( "itemCode", record.get("itemCode") );
				Map<String, Object> fgmap = getFreeGoods( fgConditionMap, new String[] { "freegoodsKey" } );
				lockFreegoods( (String)fgmap.get("freegoodsKey") );

				int newFreegoodsQty = getFreeGoodsQty( fgConditionMap, orderQty );
				int oldFreegoodsQty = (Integer)record.get( mostFieldKeys[1] );
				if( oldFreegoodsQty != newFreegoodsQty ) {
					record.put( "freegoodsQty", newFreegoodsQty );
					record.put( "inputTotalQty", newFreegoodsQty );
				}

				if( newFreegoodsQty > 0 ) {
					freegoodsDetailMap.put( "lineNumber", detailDB.getNextLineNumber(orderKey) );
					freegoodsDetailMap.put( "itemCode", record.get("itemCode") );
					freegoodsDetailMap.put( "itemCodeConfirmed", record.get("itemCodeConfirmed") );
					freegoodsDetailMap.put( "childLineNumber", OrderDetail.CHILD_LINENUMBER_NORMAL);
					freegoodsDetailMap.put( "itemRefInd", record.get("itemRefInd") );
					freegoodsDetailMap.put( "orderQty", newFreegoodsQty );
					BigDecimal bgPrice = (BigDecimal) record.get( "price" );
					double price = bgPrice != null ? bgPrice.doubleValue() : 0;
					freegoodsDetailMap.put( "orderValue", newFreegoodsQty * price );
					freegoodsDetailMap.put( "simulationOrderQty", newFreegoodsQty );
					freegoodsDetailMap.put( "simulationOrderValue", newFreegoodsQty * price );
					freegoodsDetailMap.put( "uom", record.get("uom") );
					freegoodsDetailMap.put( "simulationUOM", record.get("simulationUOM") );
					freegoodsDetailMap.put( "packSize", record.get("packSize") );
					freegoodsDetailMap.put( "price", price );
					freegoodsDetailMap.put( "status", OrderDetail.STATUS_NORMAL );
					if( detailDB.modify(freegoodsDetailMap) || detailDB.regist(freegoodsDetailMap) ) {
					} else {
						throw new DataException( DataException.ERR_CANNOT_INSERT
								, msghandler.getMessage("MSG_DPR_FREEGOODS_ORDER") + " " + msghandler.getMessage(DataException.ERR_CANNOT_INSERT) );
					}
					freegoodsList.add( freegoodsDetailMap );
				}
			}
			if( freegoodsList.size() < 1 )
				return null;
		}
		return freegoodsList;
	}

	public boolean updateFreegoodsQty( SQLHandler handler, String orderKey ) throws SQLException, DataException {
		OrderDetail detailDB = new OrderDetail( handler );

		PreparedStatement pstmt = null;
		try {
			pstmt = handler.getConnection().prepareStatement(
					"UPDATE DPR_FREEGOODS SET USED_QTY = NVL(USED_QTY, 0) + ?"
							+ " WHERE ORGANIZATIONCD = ? AND OFFICECD = ? AND PARTYCD = ?"
							+ " AND STARTAVAIL_DATE <= ? AND ENDAVAIL_DATE >= ?"
							+ " AND ITEMCD = ?" );

			String[] fieldKeys = { "orderQty", "organizationCode", "officeCode", "partyCode", "orderDate", "itemCode" };
			Map<String, Object> conditionMap = Record.createMap("orderKey", orderKey);
			conditionMap.put( Condition.BASIS_CONDITIONKEY, OrderDetail.ORDER );
			List<Map<String, Object>> detailList = detailDB.getRecords( conditionMap, fieldKeys );
			if( detailList != null && detailList.size() > 0 ) {
				String organizationCode = (String) detailList.get(0).get( "organizationCode" );
				String officeCode = (String) detailList.get(0).get( "officeCode" );
				String partyCode = (String) detailList.get(0).get( "partyCode" );
				Map<String, Object> fgConditionMap = new java.util.HashMap<String, Object>();
				fgConditionMap.put( "organizationCode", organizationCode );
				fgConditionMap.put( "officeCode", officeCode );
				fgConditionMap.put( "partyCode", partyCode );

				for( Map<String, Object> detailMap : detailList ) {
					fgConditionMap.put( "itemCode", detailMap.get("itemCode") );
					getFreeGoods( fgConditionMap, null );
					Map<String, Object> _conditionMap = (Map<String, Object>) fgConditionMap.get( "_condition" );
					partyCode = (String) _conditionMap.get( "partyCode" );

					pstmt.setInt( 1, ((BigDecimal) detailMap.get("orderQty")).intValue() );
					pstmt.setString( 2, organizationCode );
					pstmt.setString( 3, officeCode );
					pstmt.setString( 4, partyCode );
					pstmt.setDate( 5, (com.irt.data.Date)detailMap.get("orderDate") );
					pstmt.setDate( 6, (com.irt.data.Date)detailMap.get("orderDate") );
					pstmt.setString( 7, (String)detailMap.get("itemCode") );

					pstmt.executeUpdate();
				}
			}
			return true;
		} finally {
			try {
				if( pstmt != null ) pstmt.close();
			} catch( SQLException sqlEx ) {}
		}
	}

	public void write( List<Map<String, Object>> recordList, SQLHandler handler, DataWriter out, ColumnList columnList, int writingOption, int maxRows ) throws IOException, SQLException {
		if( (writingOption & QueryableManager.OPT_WRITING_TITLE) > 0 )
			SQLManager.writeTitle(handler, out, columnList, writingOption);
		if( out instanceof com.irt.util.SSDataWriter )
			((com.irt.util.SSDataWriter) out).setColumnList(columnList);

		Column[] columns = columnList.getColumns();
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

		for( int rownum = 1; rownum <= recordList.size(); rownum++ ) {
			if( maxRows > 0 && rownum > maxRows )
				break;
			Map<String, Object> recordMap = recordList.get(rownum - 1);
			recordMap.put("rowNumber", rownum);
			if( (writingOption & OPT_WRITING_ROWNUMBER) > 0 )
				out.print(rownum);
			if( (writingOption & OPT_WRITING_EXECUTETYPE) > 0 )
				out.print("U");
			for (Column column : columns)
				out.print(column.getColumnValue(recordMap, msghandler),
						column.getColumnSize());
			out.println();
		}
	}
}
