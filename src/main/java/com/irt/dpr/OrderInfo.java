/*
 *	File Name:	OrderInfo.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2013/01/30		2.2.1	PIPO 기능 개발
 *	lsinji		2008/09/26		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.dpr.util.Loggers;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/*
 *
 */
public class OrderInfo extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_ORDER_INFO );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ORDER_INFO );

	public OrderInfo( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String orderNumber ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "orderNumber", orderNumber );

		return primaryMap;
	}

	/*
	 * UPDATE DPR_ORDER_INFO
	 */
	public boolean updateOrderInfo( Map<String, Object> headerMap ) throws SQLException, DataException {
		Map<String, Object> recordMap = new java.util.HashMap<String, Object>( headerMap );
		String[] fieldKeys = new String[] { "soldPartyCode", "shipPartyCode", "orderDate", "inDateSimulation", "inDateConfirm" };

		Map<String, Object> primaryMap = new java.util.HashMap<String, Object>();
		primaryMap.put( "orderKey", headerMap.get("orderKey") );
		primaryMap.put( "soldPartyCode", headerMap.get("soldPartyCode") );
		primaryMap.put( "shipPartyCode", headerMap.get("shipPartyCode") );

		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( primaryMap );
		QueryFactory factory_order = Schema.findQueryFactory( Schema.DPR_ORDER );
		SQLManager.getRecordMap( handler, recordMap, factory_order.setQuery(querybuf, fieldKeys) );
		if( recordMap.get("inDateConfirm") == null )
			recordMap.put( "inDateConfirm", recordMap.get("inDateSimulation") );

		return SQLManager.manageRecord( handler, table, recordMap, Record.UPDATE | Record.INSERT );
	}

	/*
	 * CALLING FROM  WM: ORDER_IF_STATUSLIST
	 * UPDATE DPR_ORDER_INFO, UPDATE DPR_ORDER_MEMOS, UPDATE DPR_ORDER_BILLING
	 */
	public DataResult updateWithRelation( List<Map<String, Object>> statusList ) throws SQLException, DataException {
		List<Map<String, Object>> list = new java.util.ArrayList( statusList );

		Logger logger = Logger.getLogger( "com.irt.dpr.tools.OrderCanonicalProcess" );

		DataResult resultSet = new DataResult();
		for( Map<String, Object> map : list ) {
			List<Map<String, Object>> memosList = (List<Map<String, Object>>)map.remove( "memosDocument" );
			List<Map<String, Object>> billingList = (List<Map<String, Object>>)map.remove( "billingDocument" );

			try {
				boolean ret = SQLManager.manageRecord( handler, table, map, Record.UPDATE | Record.INSERT );

				DataResult resultM = new DataResult();
				DataResult resultB = new DataResult();
				if( ret ) {
					try {
						resultSet.increaseCount();
						handler.commit();

						try {
							resultM = SQLManager.manageRecordAll( handler, Memos.getTable(), memosList, Record.INSERT | Record.UPDATE );
						} catch( SQLException sqlEx ) {
							logger.info( Loggers.STR_BUSINESS + map.get("orderKey") + ": " + "MEMOS record count( ALL:" + resultM.getCount() + ", ER:"+ resultM.getErrorCount() + " )" );
							logger.error( sqlEx );
						}

						try {
							resultB = SQLManager.manageRecordAll( handler, Billing.getTable(), billingList, Record.INSERT | Record.UPDATE );
						} catch( SQLException sqlEx ) {
							logger.info( Loggers.STR_BUSINESS + map.get("orderKey") + ": " + "BILLING record count( ALL:" + resultB.getCount() + ", ER:"+ resultB.getErrorCount() + " )" );
							logger.error( sqlEx );
						}

						logger.info( Loggers.STR_BUSINESS + map.get("orderKey") + ": " + "MEMOS[ ALL:"+ resultM.getCount() + ", ER:"+ resultM.getErrorCount() +"]"
								+ ", BILLING[ ALL:" + resultB.getCount() +", ER:"+ resultB.getErrorCount() + "] is proceed" );
					} catch( SQLException sqlEx ) {
						Logger.getLogger( "com.irt.servlet.ServletModel" ).error( sqlEx );
					}
				} else {
					resultSet.increaseErrorCount();
					logger.error( map.get("orderKey") + ": " + "Can't saved order status Information["+ map.get("orderNumber") +"]" );
				}
			} catch( DataException dataEx ) {
				resultSet.appendError( dataEx );

				handler.rollback();
			}
		}
		handler.commit();

		return resultSet;
	}

	/*
	 * CALLING FROM WM: ORDER_IF_STATUS
	 * ORDER_IF_STATUS: UPDATE DPR_ORDER_INFO_DTL
	 */
	public boolean updateWithDetail( Map<String, Object> headerMap, List<Map<String, Object>> detailList ) throws SQLException, DataException {
		Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), "start." );

		if( SQLManager.manageRecord(handler, table, headerMap, Record.INSERT | Record.UPDATE) ) {
			String orderNumber = (String)headerMap.get( "orderNumber" );
			String systemDateTime = (String)SQLManager.getObjectValue( handler,
					"SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM DPR_ORDER_INFO WHERE ORDER_NUMBER= ?", new Object[] {orderNumber} );

			//IF CHILD_LINE_NO != 0 THEN PIPO CHILD ITEM 으로 판단하여 삭제 가능한 것으로  표시. 만약  WM으로부터 LINE정보를  받았다면  ITEMREF_IND가 업데이트 될 것임.
			String dcQuery = "UPDATE DPR_ORDER_INFO_DTL SET ITEMREF_IND = 'DC', CONFIRMED_ORDERQTY = 0, CONFIRMED_ORDERVALUE = 0"
					+", UOM = NULL , PRICE = 0 WHERE ORDER_NUMBER = ? AND CHILD_LINE_NO != 0";
			int childResetCount = SQLManager.executeStatement( handler, dcQuery, new Object[] { headerMap.get("orderNumber") } );

			if( Loggers.business.isTraceEnabled() ) {
				StringBuffer lbuf = new StringBuffer();
				for (Map<String, Object> map : detailList) {
					for(String key : map.keySet()) {
						lbuf.append( key + ": " + map.get(key) + ", " );
					}
					Loggers.business.debug( "{}: {}", new Object[] { headerMap.get("orderKey"), lbuf.toString() } );
					lbuf.delete(0, lbuf.capacity());
				}
			}

			Table detailTable = OrderInfoDetail.getTable();
			DataResult resultSet = SQLManager.manageRecordAll( handler, detailTable, detailList, Record.INSERT | Record.UPDATE );

			if( resultSet.getException() != null ) {
				Loggers.business.debug( "{}: {}", new Object[] { headerMap.get("orderKey"), "thow resultSet.getException();" } );
				throw resultSet.getException();
			} else {
				if( systemDateTime != null ) {
					// IF UPGDATE < SYSTEMTIME THEN 해당 라인을  안  받은  것으로  판단.
					String resetQuery =
							"UPDATE DPR_ORDER_INFO_DTL SET CONFIRMED_ORDERQTY = 0, CONFIRMED_ORDERVALUE = 0"
									+ ", CONFIRMED_ORDERTAX = 0, CONFIRMED_ORDERDISCOUNT = 0, CONFIRMED_ORDERTOTAL = 0"
									+ ", DLVRY_STATUS = 'DE', DLVRY_OPENQTY = 0, DLVRY_INTRAQTY = 0, DLVRY_COMPQTY = 0"
									+ " WHERE ORDER_NUMBER = ? AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";

					int resetCount = SQLManager.executeStatement( handler, resetQuery, new Object[] { orderNumber, systemDateTime  } );
					Loggers.business.debug( "{}: {}", new Object[] { headerMap.get("orderKey"), "RESET: " + resetCount + " - " + systemDateTime } );
				}
				int childDeleteCount = 0;
				if( childResetCount > 0 ) {
					childDeleteCount = SQLManager.executeStatement( handler, "DELETE FROM DPR_ORDER_INFO_DTL WHERE ORDER_NUMBER = ? AND ITEMREF_IND = ?"
							, new Object[] { headerMap.get("orderNumber"), OrderDetail.ITEMREF_PIPO_CHILD_DELETED } );
					Loggers.business.debug( "{}: {}", new Object[] { headerMap.get("orderKey"), ", CHILD RESET: " + childResetCount + ", CHILD DELETE: " + childDeleteCount } );
				}
			}
			Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), "return true;" );
			return true;
		}
		Loggers.business.debug( "{}: {}", headerMap.get("orderKey"), "return false;" );
		return false;
	}
}
