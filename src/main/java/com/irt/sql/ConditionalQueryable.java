/*
 *	File Name:	ConditionalQueryable.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										생성자 ConditionalQueryable( querybufValid, queryable, unconditionFieldKeys ) 삭제
 *	stghr12		2007/10/31		2.1.1	IllegalArgumentException message 변경: invalid -> illegal
 *	stghr12		2006/12/01		2.1.0	QueryBufferValid사용으로 수정
 *										getQueryableFieldArray() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

import java.util.Map;

/**
 * 조건이 맞을 경우에만 추가되는 Queryable Class.
 */
public class ConditionalQueryable implements Queryable {
	Map<String, QueryableField> fieldMap;
	Queryable queryable;
	QueryBufferValid querybufValid;

	public ConditionalQueryable( QueryBufferValid querybufValid, Queryable queryable ) {
		this.queryable = queryable;
		this.querybufValid = querybufValid;

		this.fieldMap = new java.util.HashMap<String, QueryableField>( queryable.getQueryableFieldMap() );
		for( QueryableField qfield : queryable.getQueryableFieldMap().values() )
			fieldMap.put( qfield.getFieldKey(), new ConditionalQueryableField(querybufValid, qfield) );

		this.fieldMap = java.util.Collections.unmodifiableMap( this.fieldMap );
	}

	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		return ( querybufValid.hasValidCondition(querybuf) && queryable.appendCondition(querybuf) );
	}

	public boolean appendTable( QueryBuffer querybuf ) {
		return ( querybufValid.hasValidCondition(querybuf) && queryable.appendTable(querybuf) );
	}

	public boolean existTable( QueryBuffer querybuf ) {
		return ( querybufValid.hasValidCondition(querybuf) && queryable.existTable(querybuf) );
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
		return fieldMap;
	}
}
