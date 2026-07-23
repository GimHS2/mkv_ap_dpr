/*
 *	File Name:	QueryableImpl.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										appendBST(): QueryableFieldImplBS.getInstance() 사용
 *	stghr12		2007/10/31		2.1.2	IllegalArgumentException message 변경: invalid -> illegal
 *										appendARR(), appendBST() 추가
 *										convertToUnconditionable() 추가
 *	stghr12		2007/04/30		2.1.1	getBaseJoinable(), remove() 추가
 *	stghr12		2006/12/01		2.1.0	baseQueryable -> baseJoinable
 *										getQueryableFieldArray() 추가
 *										append( queryable, fieldKeys, conditionQuery ) 추가
 *										append( queryable, fieldKeys, conditionQuery, conditionable ) 추가
 *										appendCND() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

import java.util.Map;
import java.util.List;

/**
 * 기본 Joinable(Queryable)을 중심으로 Queryable를 구현한 Class.
 */
public class QueryableImpl implements Queryable {
	Joinable baseJoinable;
	Map<String, QueryableField> fieldMap;
	List<Object[]> queryableList;

	public QueryableImpl( Joinable baseJoinable ) {
		this.baseJoinable = baseJoinable;
		this.fieldMap = new java.util.LinkedHashMap<String, QueryableField>();
		this.queryableList = new java.util.ArrayList<Object[]>();

		if( baseJoinable instanceof Queryable ) {
			Queryable baseQueryable = (Queryable)baseJoinable;
			for( QueryableField qfield : baseQueryable.getQueryableFieldMap().values() )
				fieldMap.put( qfield.getFieldKey(), qfield );

			queryableList.add( new Object[] { baseQueryable, null } );
		}
	}

	public QueryableImpl( Joinable baseJoinable, QueryableField[] qfields ) {
		this( baseJoinable );
		append( qfields );
	}

	protected void append( Queryable queryable ) {
		for( QueryableField qfield : queryable.getQueryableFieldMap().values() ) {
			qfield = fieldMap.put( qfield.getFieldKey(), new QueryableFieldWrapper(qfield, queryable) );
			if( qfield != null ) fieldMap.put( qfield.getFieldKey(), qfield );
		}
		queryableList.add( new Object[] { queryable, queryable } );
	}

	protected void append( Queryable queryable, String conditionQuery ) {
		append( queryable, conditionQuery, true );
	}

	protected void append( Queryable queryable, String conditionQuery, boolean conditionable ) {
		Joinable joinable = new JoinableWrapper( queryable, conditionQuery );
		for( QueryableField qfield : queryable.getQueryableFieldMap().values() ) {
			qfield = fieldMap.put( qfield.getFieldKey(), new QueryableFieldWrapper( qfield, joinable, conditionable ) );
			if( qfield != null ) fieldMap.put( qfield.getFieldKey(), qfield );
		}
		queryableList.add( new Object[] { queryable, joinable } );
	}

	protected void append( Queryable queryable, String[] fieldKeys, String conditionQuery ) {
		append( queryable, fieldKeys, conditionQuery, true );
	}

	protected void append( Queryable queryable, String[] fieldKeys, String conditionQuery, boolean conditionable ) {
		Joinable joinable = new JoinableWrapper( queryable, conditionQuery );
		for( int f = 0; f < fieldKeys.length; f++ ) {
			QueryableField qfield = queryable.getQueryableField( fieldKeys[f] );
			fieldMap.put( qfield.getFieldKey(), new QueryableFieldWrapper( qfield, joinable, conditionable ) );
		}
		queryableList.add( new Object[] { queryable, joinable } );
	}

	protected void append( QueryableField qfield ) {
		append( qfield, false );
	}

	protected void append( QueryableField qfield, boolean updateExist ) {
		qfield = fieldMap.put( qfield.getFieldKey(), qfield );
		if( qfield != null && !updateExist ) fieldMap.put( qfield.getFieldKey(), qfield );
	}

	protected void append( QueryableField[] qfields ) {
		append( qfields, false );
	}

	protected void append( QueryableField[] qfields, boolean updateExist ) {
		for( int f = 0; f < qfields.length; f++ )
			append( qfields[f], updateExist );
	}

	protected void appendARR( QueryBufferValid querybufValid, QueryableField qfield ) {
		ConditionalQueryableField cqfield = new ConditionalQueryableField( querybufValid, qfield );
		qfield = fieldMap.put( qfield.getFieldKey(), cqfield );
		if( qfield != null )
			fieldMap.put( qfield.getFieldKey(), new QueryableFieldImplAR(qfield, cqfield) );
	}

	protected void appendARR( QueryBufferValid querybufValid, QueryableField[] qfields ) {
		for( int f = 0; f < qfields.length; f++ )
			appendARR( querybufValid, qfields[f] );
	}

	protected void appendBST( String basisValue, QueryableField qfield ) {
		qfield = QueryableFieldImplBS.getInstance( getQueryableField(qfield.getFieldKey()), basisValue, qfield );
		fieldMap.put( qfield.getFieldKey(), qfield );
	}

	protected void appendBST( String basisValue, QueryableField[] qfields ) {
		for( int f = 0; f < qfields.length; f++ )
			appendBST( basisValue, qfields[f] );
	}

	protected void appendCND( QueryBufferValid querybufValid, Queryable queryable ) {
		append( new ConditionalQueryable(querybufValid, queryable) );
	}

	protected void appendCND( QueryBufferValid querybufValid, Queryable queryable, String conditionQuery ) {
		append( new ConditionalQueryable(querybufValid, queryable), conditionQuery );
	}

	protected void appendCND( QueryBufferValid querybufValid, Queryable queryable, String conditionQuery, boolean conditionable ) {
		append( new ConditionalQueryable(querybufValid, queryable), conditionQuery, conditionable );
	}

	protected void appendCND( QueryBufferValid querybufValid, QueryableField qfield ) {
		appendCND( querybufValid, qfield, false );
	}

	protected void appendCND( QueryBufferValid querybufValid, QueryableField qfield, boolean updateExist ) {
		append( new ConditionalQueryableField(querybufValid, qfield), updateExist );
	}

	protected void appendCND( QueryBufferValid querybufValid, QueryableField[] qfields ) {
		appendCND( querybufValid, qfields, false );
	}

	protected void appendCND( QueryBufferValid querybufValid, QueryableField[] qfields, boolean updateExist ) {
		for( int f = 0; f < qfields.length; f++ )
			appendCND( querybufValid, qfields[f], updateExist );
	}

	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		boolean hasCondition = false;
		for( Object[] objects : queryableList ) {
			if( ((Queryable)objects[0]).appendCondition(querybuf) ) {
				hasCondition = true;
				if( objects[1] != null ) ((Joinable)objects[1]).appendTable( querybuf );
			}
		}

		return hasCondition;
	}

	public boolean appendTable( QueryBuffer querybuf ) {
		return baseJoinable.appendTable( querybuf );
	}

	protected void convertToUnconditionable( String... fieldKeys ) {
		for( int f = 0; f < fieldKeys.length; f++ ) {
			QueryableField qfield = getQueryableField( fieldKeys[f] );
			fieldMap.put( qfield.getFieldKey(), new QueryableFieldWrapper(qfield, false) );
		}
	}

	public boolean existTable( QueryBuffer querybuf ) {
		return baseJoinable.existTable( querybuf );
	}

	public Joinable getBaseJoinable() {
		return baseJoinable;
	}

	public QueryableField getQueryableField( String fieldKey ) {
		return fieldMap.get( fieldKey );
	}

	public QueryableField[] getQueryableFieldArray( String... fieldKeys ) throws IllegalArgumentException {
		QueryableField[] qfields = new QueryableField[fieldKeys.length];
		for( int f = 0; f < fieldKeys.length; f++ ) {
			if( fieldKeys[f] == null ) continue;
			qfields[f] = fieldMap.get( fieldKeys[f] );
			if( qfields[f] == null )
				throw new IllegalArgumentException( "illegal fieldKey '"+ fieldKeys[f] +"'" );
		}

		return qfields;
	}

	public Map<String, ? extends QueryableField> getQueryableFieldMap() {
		return java.util.Collections.unmodifiableMap( fieldMap );
	}

	protected void remove( String... fieldKeys ) {
		for( int f = 0; f < fieldKeys.length; f++ )
			fieldMap.remove( fieldKeys[f] );
	}
}
