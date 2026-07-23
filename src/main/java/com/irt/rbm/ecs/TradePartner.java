/*
 *	File Name:	TradePartner.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										PartyQuery 사용
 *										SQLManager 변경사항 적용: fieldMap 삭제
 *	stghr12		2007/10/31		2.1.2	update(): 버그수정
 *										TradePartner.FieldSet: extends com.irt.data.FieldSet -> extends com.irt.data.ValidableFieldSet
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
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class TradePartner extends com.irt.rbm.ManipulableManagerImpl {
	public final static int INFO_TP						= 0x00000001;
	public final static int INFO_TPINFO					= 0x00000002;
	public final static int INFO_ALL					= 0x00000003;

	private final static int IDX_TP						= 0;
	private final static int IDX_TPINFO					= 1;

	private final static Table[] tables = new Table[] {
			Schema.findTable( Schema.ECS_TRADEPARTNER )
			, Schema.findTable( Schema.ECS_TRADEPARTNER_INFO )
	};
	private final static QueryFactory[] factories = new QueryFactory[] {
			Schema.findQueryFactory( Schema.ECS_TRADEPARTNER )
			, Schema.findQueryFactory( Schema.ECS_TRADEPARTNER_INFO )
	};
	private final static QueryFactory factory = new QueryFactory( new PartyQuery(true) );

	public TradePartner( SQLHandler handler ) {
		super( handler, tables[IDX_TP], factory );
	}

	public static Map<String, Object> createPrimary( String buyerGln, String sellerGln ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "buyerGln", buyerGln );
		primaryMap.put( "sellerGln", sellerGln );

		return primaryMap;
	}

	public boolean delete( Map<String, Object> primaryMap, int infoType ) throws DataException, SQLException {
		if( (infoType & INFO_TP) > 0 ) return delete( primaryMap );

		int resultCnt = 0;
		Object[] primaryVars = Record.extractValues( primaryMap, new String[] { "buyerGln", "sellerGln" } );

		if( (infoType & INFO_TPINFO) > 0 )
			resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_TRADEPARTNER_INFO WHERE BUYERGLN = ? AND SELLERGLN = ?", primaryVars );

		return ( resultCnt > 0 );
	}

	@Override
	public AbstractFieldSet getFieldSet( boolean inserting ) {
		return TradePartner.FieldSet.getInstance( inserting );
	}

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, int infoType ) throws SQLException {
		QueryBuffer querybuf = setPrimaryConditionQuery( new ConditionQueryBuffer(primaryMap) );
		if( querybuf == null ) return null;

		if( (infoType & INFO_TP) > 0 )
			factories[IDX_TP].setDataQuery( querybuf );
		else
			factories[IDX_TP].setDataQuery( querybuf, new String[] { "buyerGln", "sellerGln" } );

		if( (infoType & INFO_TPINFO) > 0 ) {
			factories[IDX_TPINFO].setDataQuery( querybuf );
			querybuf.appendCondition( "TPI.BUYERGLN(+) = TP.BUYERGLN AND TPI.SELLERGLN(+) = TP.SELLERGLN" );
		}

		return SQLManager.getRecordMap( handler, null, querybuf );
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
		Object[] primaryVars = Record.extractValues( recordMap, new String[] { "buyerGln", "sellerGln" } );

		// ECS_TRADEPARTNER
		if( inserting )
			SQLManager.manageRecord( handler, tables[IDX_TP], recordMap, Record.INSERT );
		else if( (infoType & INFO_TP) > 0 ) {
			if( !SQLManager.manageRecord(handler, tables[IDX_TP], recordMap, Record.UPDATE) )
				return false;
		} else if( SQLManager.getObjectValue(handler, "SELECT 'x' FROM ECS_TRADEPARTNER WHERE BUYERGLN = ? AND SELLERGLN = ?", primaryVars) == null )
			return false;

		// ECS_TRADEPARTNER_INFO
		if( (infoType & INFO_TPINFO) > 0 ) {
			if( inserting || !SQLManager.manageRecord(handler, tables[IDX_TPINFO], recordMap, Record.UPDATE) )
				SQLManager.manageRecord( handler, tables[IDX_TPINFO], recordMap, Record.INSERT );
		}

		return true;
	}

	/**
	 *
	 */
	static class FieldSet extends com.irt.data.ValidableFieldSet {
		private static FieldSet fieldSet_i, fieldSet_u;

		private FieldSet( Map<String, ValidableField> fieldMap ) {
			super( fieldMap );
		}

		private static void append( Map<String, ValidableField> fieldMap, Table table ) {
			for( ValidableField field : table.getFieldMap().values() ) {
				String fieldKey = field.getFieldKey();

				if( "buyerGln".equals(fieldKey) || "sellerGln".equals(fieldKey) ) continue;
				if( !field.nullable() ) field = new ValidableField( field, true, field.readonly() );
				fieldMap.put( fieldKey, field );
			}
		}

		public static FieldSet getInstance( boolean inserting ) {
			if( fieldSet_i == null ) initialize();
			return( inserting ? fieldSet_i : fieldSet_u );
		}

		private static synchronized void initialize() {
			if( fieldSet_i != null ) return;

			Map<String, ValidableField> fieldMap = new java.util.HashMap<String, ValidableField>();
			append( fieldMap, Schema.findTable(Schema.ECS_TRADEPARTNER_INFO) );

			Map<String, ValidableField> fieldMap_i = fieldMap;
			Map<String, ValidableField> fieldMap_u = new java.util.HashMap<String, ValidableField>( fieldMap );

			Table table = Schema.findTable( Schema.ECS_TRADEPARTNER );
			fieldMap_i.putAll( table.getFieldMap() );
			fieldMap_u.putAll( table.getValidableFieldSet(false).getFieldMap() );

			fieldSet_i = new FieldSet( fieldMap_i );
			fieldSet_u = new FieldSet( fieldMap_u );
		}
	}
}
