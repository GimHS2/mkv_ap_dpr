/*
 *	File Name:	QueryFactory.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getQueryable() 추가
 *	stghr12		2006/12/01		2.1.0	getQueryableFieldArray() 추가
 *										Condition.DISTINCT_CONDITIONKEY 적용
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2006/01/07		1.4.0	create
 *
**/

package com.irt.sql;

import com.irt.data.Condition;
import java.util.Map;

/**
 * Queryable을 가지고 QueryBuffer에 Query를 append하는 Class.
 */
public class QueryFactory implements Queryable {
	Queryable queryable;

	public QueryFactory( Queryable queryable ) {
		this.queryable = queryable;
	}

	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		return queryable.appendCondition( querybuf );
	}

	/**
	 * QueryBuffer에 fieldKey에 해당하는 데이터 추가.
	 * @return 추가된 경우 true return. (이미 추가되어 있는 경우 false)
	 */
	public boolean appendData( QueryBuffer querybuf, String fieldKey ) {
		QueryableField qfield = getQueryableField( fieldKey );
		if( qfield != null && qfield.appendData(querybuf) ) {
			setBaseTable( querybuf );
			return true;
		} else
			return false;
	}

	public boolean appendTable( QueryBuffer querybuf ) {
		return queryable.appendTable( querybuf );
	}

	public boolean existTable( QueryBuffer querybuf ) {
		return queryable.existTable( querybuf );
	}

	public Queryable getQueryable() {
		return queryable;
	}

	public QueryableField getQueryableField( String fieldKey ) {
		return queryable.getQueryableField( fieldKey );
	}

	public QueryableField[] getQueryableFieldArray( String... fieldKeys ) throws IllegalArgumentException {
		return queryable.getQueryableFieldArray( fieldKeys );
	}

	public Map<String, ? extends QueryableField> getQueryableFieldMap() {
		return queryable.getQueryableFieldMap();
	}

	/**
	 * QueryBuffer에 Queryable의 기본 테이블 추가.
	 * queryable.appendTable( querybuf ) 호출
	 */
	public QueryBuffer setBaseTable( QueryBuffer querybuf ) {
		queryable.appendTable( querybuf );
		return querybuf;
	}

	/**
	 * QueryBuffer(ConditionQueryBuffer일 경우)에 조건 추가.
	 * {@link #setBaseTable(QueryBuffer) setBaseTable}( querybuf ) 수행.
	 */
	public QueryBuffer setConditionQuery( QueryBuffer querybuf ) {
		setBaseTable( querybuf );
		if( (querybuf instanceof ConditionQueryBuffer) && ((ConditionQueryBuffer)querybuf).hasConditionMap() ) {
			ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;
			for( String conditionKey : condquerybuf.getConditionKeys() ) {
				QueryableField qfield = getQueryableField( conditionKey );
				if( qfield != null ) qfield.appendCondition( condquerybuf );
			}

			appendCondition( condquerybuf );
		}

		return querybuf;
	}

	/**
	 * {@link #setConditionQuery(QueryBuffer) setConditionQuery}( {@link QueryBuffer#appendData(String) querybuf.appendData}("COUNT(*)") );
	 */
	public QueryBuffer setCountQuery( QueryBuffer querybuf ) {
		return setConditionQuery( querybuf.appendData("COUNT(*)") );
	}

	/**
	 * QueryBuffer에 모든 Queryable에 속한 모든 QueryableField 추가.
	 * {@link #setBaseTable(QueryBuffer) setBaseTable}( querybuf ) 수행.
	 * @see #getQueryableFieldMap
	 */
	public QueryBuffer setDataQuery( QueryBuffer querybuf ) {
		setBaseTable( querybuf );

		if( querybuf instanceof ConditionQueryBuffer ) {
			if( ((ConditionQueryBuffer)querybuf).isConditionTrue(Condition.DISTINCT_CONDITIONKEY) )
				querybuf.appendDistinct();
		}

		Map<String, ? extends QueryableField> fieldMap = getQueryableFieldMap();
		if( fieldMap != null ) {
			for( QueryableField qfield : fieldMap.values() )
				qfield.appendData( querybuf );
		}

		return querybuf;
	}

	/**
	 * QueryBuffer에 fieldKeys[]에 해당하는 데이터 추가.
	 * {@link #setBaseTable(QueryBuffer) setBaseTable}( querybuf ) 수행.
	 * @see #getQueryableField(String) getQueryableField
	 */
	public QueryBuffer setDataQuery( QueryBuffer querybuf, String[] fieldKeys ) {
		setBaseTable( querybuf );

		if( querybuf instanceof ConditionQueryBuffer ) {
			if( ((ConditionQueryBuffer)querybuf).isConditionTrue(Condition.DISTINCT_CONDITIONKEY) )
				querybuf.appendDistinct();
		}

		for( int k = 0; k < fieldKeys.length; k++ )
			if( fieldKeys[k] != null ) {
				QueryableField qfield = getQueryableField( fieldKeys[k] );
				if( qfield != null )
					qfield.appendData( querybuf );
			}

		return querybuf;
	}

	/**
	 * {@link #setConditionQuery(QueryBuffer) setConditionQuery}( {@link #setDataQuery(QueryBuffer) setDataQuery}(querybuf) );
	 */
	public QueryBuffer setQuery( QueryBuffer querybuf ) {
		return setConditionQuery( setDataQuery(querybuf) );
	}

	/**
	 * {@link #setConditionQuery(QueryBuffer) setConditionQuery}( {@link #setDataQuery(QueryBuffer, String[]) setDataQuery}(querybuf, fieldKeys) );
	 */
	public QueryBuffer setQuery( QueryBuffer querybuf, String[] fieldKeys ) {
		return setConditionQuery( setDataQuery(querybuf, fieldKeys) );
	}
}
