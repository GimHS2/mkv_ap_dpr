/*
 *	File Name:	DeliverySchedule.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DeliverySchedule extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ECS_DELIVERYCAL_DTL );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_DELIVERYCAL_DTL );

	public DeliverySchedule( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String deliveryCalendarKey, java.util.Date orderDate ) {
		Map<String, Object> primaryMap = Record.createMap( "deliveryCalendarKey", deliveryCalendarKey );
		primaryMap.put( "orderDate", com.irt.data.Date.getInstance( orderDate ) );

		return primaryMap;
	}

	protected List<Map<String, Object>> getDistinctDeliverySchedules( List<Map<String, Object>> recordList
						, com.irt.data.Date minimumOrderDate, com.irt.data.Date maximumOrderDate ) throws SQLException {
		Map<String, Object> dlvschMap;
		List<Map<String, Object>> dlvschList = new java.util.ArrayList<Map<String, Object>>();

		// deliveryCalendarKeys 초기화
		String[] deliveryCalendarKeys = new String[ recordList.size() ];
		for( int i = 0; i < recordList.size(); i++ ) {
			Map<String, Object> recordMap = recordList.get( i );
			deliveryCalendarKeys[i] = (String)recordMap.get( "deliveryCalendarKey" );
		}

		PreparedStatement pstmt_dlvdtl = null;
		try {
			QueryBuffer querybuf = new QueryBuffer();
			querybuf.appendDataWithAlias( "DLVD.ORDDATE", "orderDate" );
			querybuf.appendDataWithAlias( "DLVD.ORDER_EXIST_IND", "existOrder" );
			querybuf.appendDataWithAlias( "DLVD.INDATE", "inDate" );
			querybuf.appendDataWithAlias( "DLVD.PLANPERIOD", "planPeriod" );
			querybuf.appendDataWithAlias( "DLVD.STATUS", "status" );
			querybuf.appendTableWithAlias( "ECS_DELIVERYCAL_DTL", "DLVD" );
			querybuf.appendCondition( "DLVD.ORDDATE BETWEEN ? AND ? AND DLVD.DELIVERYCAL_KEY = ?" );
			querybuf.appendOrderBy( "1" );
			pstmt_dlvdtl = handler.getConnection().prepareStatement( querybuf.getQuery() );
			pstmt_dlvdtl.setDate( 1, minimumOrderDate );
			pstmt_dlvdtl.setDate( 2, maximumOrderDate );

			if( recordList.size() > 1 ) {
				QueryBuffer inner_querybuf = new QueryBuffer();

				inner_querybuf.appendDistinct();
				inner_querybuf.appendData( "DLVD.ORDDATE, DLVD.ORDER_EXIST_IND, DLVD.INDATE, DLVD.PLANPERIOD, DLVD.STATUS" );
				inner_querybuf.appendTableWithAlias( "ECS_DELIVERYCAL_DTL", "DLVD" );
				inner_querybuf.appendCondition( "DLVD.ORDDATE >= ?", minimumOrderDate );
				inner_querybuf.appendCondition( "DLVD.ORDDATE <= ?", maximumOrderDate );
				inner_querybuf.appendConditionByField( "DLVD.DELIVERYCAL_KEY", deliveryCalendarKeys );

				querybuf = new QueryBuffer();
				querybuf.appendData( "COUNT(*) - COUNT(DISTINCT DLVD.ORDDATE)" );
				querybuf.appendTable( inner_querybuf, "DLVD" );

				if( SQLManager.getInt(handler, querybuf) > 0 ) {
					int[] groupIds = new int[ deliveryCalendarKeys.length ];
					int[] groupCnts = new int[ deliveryCalendarKeys.length ];
					for( int i = 0; i < deliveryCalendarKeys.length; i++ ) {
						pstmt_dlvdtl.setString( 3, deliveryCalendarKeys[i] );
						List<Map<String, Object>> detailList = SQLManager.getRecordList( handler, pstmt_dlvdtl );
						if( detailList == null ) {
							groupIds[i] = -1;
							continue;
						}

						int g = 0;
						groupCnts[i] = 0;
						for( java.util.Iterator<Map<String, Object>> iterator = dlvschList.iterator(); iterator.hasNext(); g++ ) {
							dlvschMap = iterator.next();
							if( detailList.equals(dlvschMap.get("detailList")) ) {
								detailList = null;
								break;
							}
						}
						if( detailList != null ) {
							dlvschList.add( dlvschMap = recordList.get(i) );
							dlvschMap.put( "detailList", detailList );
						}
						groupIds[i] = g;
						groupCnts[g]++;
					}

					for( int i = 0; i < dlvschList.size(); i++ ) {
						dlvschMap = dlvschList.get(i);

						dlvschMap.put( "deliveryCalendarKey", new String[groupCnts[i]] );
						groupCnts[i] = 0;
					}

					for( int i = 0; i < deliveryCalendarKeys.length; i++ ) {
						int g = groupIds[i];
						if( g < 0 ) continue;

						dlvschMap = dlvschList.get(g);
						Object detailList = dlvschMap.get( "detailList" );
						String[] keys = (String[])dlvschMap.get( "deliveryCalendarKey" );

						Record.removeUnEqualValue( dlvschMap, recordList.get(i) );

						keys[groupCnts[g]++] = deliveryCalendarKeys[i];
						dlvschMap.put( "detailList", detailList );
						dlvschMap.put( "deliveryCalendarKey", keys );
					}

					return dlvschList;
				}
			}

			pstmt_dlvdtl.setString( 3, deliveryCalendarKeys[0] );
			List<Map<String, Object>> detailList = SQLManager.getRecordList( handler, pstmt_dlvdtl );
			if( detailList == null ) return null;

			dlvschList.add( dlvschMap = recordList.get(0) );
			for( int i = 1; i < deliveryCalendarKeys.length; i++ )
				Record.removeUnEqualValue( dlvschMap, recordList.get(i) );
			dlvschMap.put( "deliveryCalendarKey", deliveryCalendarKeys );
			dlvschMap.put( "detailList", detailList );

			return dlvschList;
		} finally {
			try { pstmt_dlvdtl.close(); } catch( Exception ex ) {}
		}
	}
}
