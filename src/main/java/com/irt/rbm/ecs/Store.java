/*
 *	File Name:	Store.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										SQLManager 변경사항 적용: fieldMap 삭제
 *	stghr12		2007/04/30		2.1.0	create
 *
 **/

package com.irt.rbm.ecs;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Store extends com.irt.rbm.ManipulableManagerImpl {
	private final static Queryable queryable = new QueryableImpl( Schema.findQueryable(Schema.ECS_STORE) ) {{
		append( Schema.findQueryable(Schema.ECS_PARTY), "PTY.GLN = STR.ST_GLN" );
	}};
	private final static Queryable queryable_pty = new QueryableImpl( Schema.findQueryable(Schema.ECS_PARTY) ) {
		{
			append( Schema.findQueryable(Schema.ECS_STORE), "STR.ST_GLN(+) = PTY.GLN" );
		}

		@Override
		public boolean appendTable( QueryBuffer querybuf ) {
			if( super.appendTable(querybuf) ) {
				querybuf.appendCondition( "PTY.PARTY_ROLE IN ( 'RWH', 'STR' )" );
				return true;
			}
			return false;
		}
	};

	private final static Table table = Schema.findTable( Schema.ECS_STORE );
	private final static QueryFactory factory = new QueryFactory( queryable );
	private final static QueryFactory factory_pty = new QueryFactory( queryable_pty );

	public Store( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String storeGln ) {
		return Record.createMap( "storeGln", storeGln );
	}

	@Override
	public int getRecordCount( Map<String, ? extends Object> conditionMap ) throws SQLException {
		QueryFactory factory = ( Condition.isConditionTrue(conditionMap, "analysisOnly") ? Store.factory : Store.factory_pty );
		QueryBuffer querybuf = factory.setCountQuery( new ConditionQueryBuffer(conditionMap) );

		return SQLManager.getInt( handler, querybuf );
	}

	@Override
	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap, int skipRows, int maxRows ) throws SQLException {
		QueryFactory factory = ( Condition.isConditionTrue(conditionMap, "analysisOnly") ? Store.factory : Store.factory_pty );
		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap) );
		appendOrderBy( querybuf, factory );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	@Override
	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap, String[] fieldKeys, int skipRows, int maxRows )
			throws SQLException {
		QueryFactory factory = ( Condition.isConditionTrue(conditionMap, "analysisOnly") ? Store.factory : Store.factory_pty );
		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		appendOrderBy( querybuf, factory );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		SQLManager.executeStatement( handler, "INSERT INTO RBM_MNG_KEY( MNGKEY ) VALUES( 'P' || seqRBM_MNG_KEY.NEXTVAL )" );
		return super.regist( recordMap );
	}

	@Override
	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException {
		return registEach( records );
	}
}
