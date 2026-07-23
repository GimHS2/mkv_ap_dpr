/*
 *	File Name:	TradeItem.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	getDefaultRecord(): TimeZone이 null일 경우 발생하는 오류 수정
 *										usingSellerGtin() 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										ItemQuery() 사용
 *										getDefaultRecord(), getDeliveryCalendarKeys(), getDistinctDeliverySchedules() 추가
 *	stghr12		2007/10/31		2.1.2	getFieldSet(), update() 오류수정
 *	stghr12		2007/04/30		2.1.1	createPrimary(): java.util.HashMap -> java.util.TreeMap
 *	stghr12		2006/12/01		2.1.0	ManipulableManager 변경사항 적용
 *										modifyAll(): modifyEach() 사용으로 변경
 *										registAll(): registEach() 사용으로 변경
 *										Table.STATEMENT_* -> Record.*
 *	stghr12		2006/03/31		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	create
 *
 **/

package com.irt.rbm.ecs;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TradeItem extends com.irt.rbm.ManipulableManagerImpl {
	public final static int INFO_ITEM					= Item.INFO_ITEM;
	public final static int INFO_ITEMINFO				= Item.INFO_ITEMINFO;
	public final static int INFO_ORDERING				= Item.INFO_ORDERING;
	public final static int INFO_HIERARCHY				= Item.INFO_HIERARCHY;
	public final static int INFO_ALL					= INFO_ITEM | INFO_ITEMINFO | INFO_ORDERING | INFO_HIERARCHY;

	public final static char ROLE_BUYER					= 'B';
	public final static char ROLE_SELLER				= 'S';

	private final static int IDX_ITEM					= 0;
	private final static int IDX_ITEMINFO				= 1;
	private final static int IDX_ORDERING				= 2;

	private final static Table[] tables = new Table[] {
			Schema.findTable( Schema.ECS_ITEMTP )
			, Schema.findTable( Schema.ECS_ITEMTP_INFO )
			, Schema.findTable( Schema.ECS_ITEMTP_ORDERING )
	};
	private final static ItemQuery itemQueryTP = new ItemQuery( ItemQuery.ITEMTP );
	private final static ItemQuery itemQueryBY = new ItemQuery( ItemQuery.ITEMTP_BUYER );
	private final static ItemQuery itemQuerySL = new ItemQuery( ItemQuery.ITEMTP_SELLER );

	private ItemQuery itemQuery;
	private QueryFactory factory;

	public TradeItem( SQLHandler handler ) {
		super( handler, tables[IDX_ITEM], new QueryFactory(itemQueryTP) );
		this.factory = new QueryFactory( this.itemQuery = itemQueryTP );
	}

	public TradeItem( SQLHandler handler, char role ) {
		super( handler, tables[IDX_ITEM], new QueryFactory( role == ROLE_BUYER ? itemQueryBY : itemQuerySL ) );
		this.factory = new QueryFactory( this.itemQuery = ( role == ROLE_BUYER ? itemQueryBY : itemQuerySL ) );
	}

	public static Map<String, Object> createPrimary( String buyerGln, String sellerGln, String gtin ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "buyerGln", buyerGln );
		primaryMap.put( "sellerGln", sellerGln );
		primaryMap.put( "gtin", gtin );

		return primaryMap;
	}

	public boolean delete( Map<String, Object> primaryMap, int infoType ) throws DataException, SQLException {
		if( (infoType & INFO_ITEM) > 0 ) return delete( primaryMap );

		int resultCnt = 0;
		String statement = null;
		Object[] primaryVars = Record.extractValues( primaryMap, new String[] { "buyerGln", "sellerGln", "gtin" } );

		if( (infoType & INFO_ITEMINFO) > 0 ) {
			statement = "DELETE ECS_ITEMTP_INFO WHERE BUYERGLN = ? AND SELLERGLN = ? AND GTIN = ?";
			resultCnt += SQLManager.executeStatement( handler, statement, primaryVars );
		}
		if( (infoType & INFO_ORDERING) > 0 ) {
			statement = "DELETE ECS_ITEMTP_ORDERING WHERE BUYERGLN = ? AND SELLERGLN = ? AND GTIN = ?";
			resultCnt += SQLManager.executeStatement( handler, statement, primaryVars );
		}

		return ( resultCnt > 0 );
	}

	public Map<String, Object> getDefaultRecord( Map<String, Object> recordMap ) throws SQLException {
		Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();

		defaultMap.put( "effectiveChangeDate", com.irt.data.Date.getInstance() );
		defaultMap.put( "orderWeekday", "YYYYYYY" );

		if( recordMap != null ) {
			PreparedStatement pstmt = handler.getConnection().prepareStatement( "SELECT TIMEZONE, CURRCD FROM ECS_PARTY WHERE GLN = ?" );
			try {
				pstmt.setString( 1, Record.extractString(recordMap, "sellerGln") );
				ResultSet rset = pstmt.executeQuery();
				try {
					if( rset.next() ) {
						String timeZone = rset.getString( 1 );
						if( timeZone != null )
							defaultMap.put( "effectiveChangeDate", com.irt.data.Date.getInstance(java.util.TimeZone.getTimeZone(timeZone)) );
						else
							defaultMap.put( "effectiveChangeDate", com.irt.data.Date.getInstance() );

						if( rset.getString(2) != null )
							defaultMap.put( "tradePriceCurr", rset.getString(2) );
					}
				} finally {
					try { rset.close(); } catch( Exception ex ) {}
				}
			} finally {
				try { pstmt.close(); } catch( Exception ex ) {}
			}

			defaultMap.putAll( recordMap );
		}

		return defaultMap;
	}

	public List<Map<String, Object>> getDeliveryCalendarKeys( Map<String, ? extends Object> conditionMap ) throws SQLException {
		QueryBuffer querybuf = factory.setConditionQuery( new ConditionQueryBuffer(conditionMap) );

		querybuf.appendDataWithAlias( "ITP.DELIVERYCAL_KEY", "deliveryCalendarKey" );
		querybuf.appendDataWithAlias( "COUNT(*)", "recordCount" );
		querybuf.appendGroupBy( "ITP.DELIVERYCAL_KEY" );

		return SQLManager.getRecordList( handler, querybuf );
	}

	public List<Map<String, Object>> getDistinctDeliverySchedules( Map<String, ? extends Object> conditionMap, java.util.Date orderDate, int days )
			throws SQLException {
		DeliverySchedule dlvDB = new DeliverySchedule( handler );
		String[] distinctFieldKeys = new String[] {
				"deliveryRoute", "dcGln", "orderLeadTimeToDestination", "orderLeadTimeToBuyer", "orderWeekday", "orderPolicy"
		};
		String[] distinctR1FieldKeys = com.irt.util.Arrays.append( distinctFieldKeys, "buyerGln" );
		String[] fieldKeys = new String[] {
				"deliveryRoute", "dcGln", "orderLeadTimeToDestination", "orderLeadTimeToBuyer", "orderWeekday", "orderPolicy"
				, "buyerGln", "buyerCompanyName", "buyerLocationName", "sellerGln", "sellerCompanyName", "sellerLocationName", "dcLocationName"
				, "deliveryCalendarKey"
		};

		com.irt.data.Date minimumOrderDate = com.irt.data.Date.getInstance( orderDate );
		com.irt.data.Date maximumOrderDate = minimumOrderDate.getDate( days - 1 );

		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		querybuf.appendDistinct();
		querybuf.appendOrderBy( "1, 2, 3, 4, 5, 6" );

		List<Map<String, Object>> recordList = SQLManager.getRecordList( handler, querybuf );
		if( recordList == null ) return null;

		List<Map<String, Object>> list = new java.util.ArrayList<Map<String, Object>>();
		List<Map<String, Object>> dlvcalList = new java.util.ArrayList<Map<String, Object>>();

		Map<String, Object> previousMap = null;
		for( Map<String, Object> recordMap : recordList ) {
			if( previousMap != null ) {
				String[] distinctKeys = ( "R1".equals(previousMap.get("deliveryRoute")) ? distinctR1FieldKeys : distinctFieldKeys );
				for( int f = 0; f < distinctKeys.length && previousMap != null; f++ ) {
					Object previousValue = previousMap.get( distinctKeys[f] );
					if( previousValue == null ) {
						if( recordMap.get(distinctKeys[f]) != null )
							previousMap = null;
					} else if( !previousValue.equals(recordMap.get(distinctKeys[f])) )
						previousMap = null;
				}
			}

			if( previousMap == null && list.size() > 0 ) {
				list = dlvDB.getDistinctDeliverySchedules( list, minimumOrderDate, maximumOrderDate );
				if( list != null ) dlvcalList.addAll( list );
				list = new java.util.ArrayList<Map<String, Object>>();
			}
			list.add( previousMap = recordMap );
		}
		if( list.size() > 0 ) {
			list = dlvDB.getDistinctDeliverySchedules( list, minimumOrderDate, maximumOrderDate );
			if( list != null ) dlvcalList.addAll( list );
		}

		return ( dlvcalList.size() > 0 ? dlvcalList : null );
	}

	@Override
	public AbstractFieldSet getFieldSet( boolean inserting ) {
		return TradeItemFieldSet.getInstance( inserting );
	}

	@Override
	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap ) throws SQLException {
		return getRecord( primaryMap, INFO_ALL );
	}

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, int infoType ) throws SQLException {
		return super.getRecord( primaryMap, itemQuery.getFieldKeys(infoType) );
	}

	@Override
	public boolean modify( Map<String, Object> recordMap ) throws DataException, SQLException {
		return update( recordMap, INFO_ALL, false );
	}

	public boolean modify( Map<String, Object> recordMap, int infoType ) throws DataException, SQLException {
		return update( recordMap, infoType, false );
	}

	@Override
	public DataResult modifyAll( Collection<Map<String, Object>> records ) throws SQLException {
		return modifyEach( records );
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		return update( recordMap, INFO_ALL, true );
	}

	public boolean regist( Map<String, Object> recordMap, int infoType ) throws DataException, SQLException {
		return update( recordMap, infoType, true );
	}

	@Override
	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException {
		return registEach( records );
	}

	private boolean update( Map<String, Object> recordMap, int infoType, boolean inserting ) throws DataException, SQLException {
		// ECS_ITEMTP
		if( inserting )
			SQLManager.manageRecord( handler, tables[IDX_ITEM], recordMap, Record.INSERT );
		else if( (infoType & INFO_ITEM) > 0 ) {
			if( !SQLManager.manageRecord( handler, tables[IDX_ITEM], recordMap, Record.UPDATE ) )
				return false;
		} else {
			Object[] primaryVars = Record.extractValues( recordMap, new String[] { "buyerGln", "sellerGln", "gtin" } );
			Object value = SQLManager.getObjectValue( handler,
					"SELECT 'x' FROM ECS_ITEMTP WHERE BUYERGLN = ? AND SELLERGLN = ? AND GTIN = ?"
					, primaryVars );
			if( value == null ) return false;
		}

		// ECS_ITEMTP_INFO
		if( (infoType & INFO_ITEMINFO) > 0 ) {
			if( inserting || !SQLManager.manageRecord(handler, tables[IDX_ITEMINFO], recordMap, Record.UPDATE) )
				SQLManager.manageRecord( handler, tables[IDX_ITEMINFO], recordMap, Record.INSERT );
		}

		// ECS_ITEMTP_ORDERING
		if( (infoType & INFO_ORDERING) > 0 ) {
			if( inserting || !SQLManager.manageRecord(handler, tables[IDX_ORDERING], recordMap, Record.UPDATE) )
				SQLManager.manageRecord( handler, tables[IDX_ORDERING], recordMap, Record.INSERT );
		}

		return true;
	}

	public static boolean usingSellerGtin() {
		return !"BY".equals( com.irt.rbm.RBMSystem.getSystemEnv("ECS", "ItemOption;TradeGtinInd", "SL") );
	}
}
