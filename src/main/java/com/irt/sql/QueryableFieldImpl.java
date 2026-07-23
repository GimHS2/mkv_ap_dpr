/*
 *	File Name:	QueryableFieldImpl.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2008/09/26		2.2.1	getJoinable() 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										AbstractField 변경사항 적용: validDateFormat 삭제, TYPE_DATETIME 추가
 *	stghr12		2007/10/31		2.1.2	appendCondition( querybuf, dataType, fieldKey, query ) 추가
 *	stghr12		2007/07/31		2.1.1	replaceAlias(): conditionQuery에 '가 있을 경우 제대로 동작하지 않는 오류 수정
 *	stghr12		2006/12/01		2.1.0	dataType을 int형에서 char형으로 변경
 *										conditionable 추가: property, 생성자
 *										appendCondition(): TYPE_TIME 처리 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

import com.irt.data.AbstractField;
import com.irt.data.Condition;
import java.text.MessageFormat;

/**
 * QueryableField를 구현한 Class.
 */
public class QueryableFieldImpl extends com.irt.data.Field implements QueryableField {
	String query;
	Joinable joinable;
	boolean conditionable;

	/**
	 * 조건처리를 하는 QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImpl( char dataType, String fieldKey, String query ) {
		this( dataType, true, fieldKey, query, (Joinable)null );
	}

	/**
	 * 조건처리 및 Join을 하는 QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImpl( char dataType, String fieldKey, String query, Joinable joinable ) {
		this( dataType, true, fieldKey, query, joinable );
	}

	/**
	 * 조건처리를 하는 QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImpl( char dataType, String fieldKey, String query, String descriptionKey ) {
		this( dataType, true, fieldKey, query, descriptionKey, (Joinable)null );
	}

	/**
	 * 조건처리 및 Join을 하는 QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImpl( char dataType, String fieldKey, String query, String descriptionKey, Joinable joinable ) {
		this( dataType, true, fieldKey, query, descriptionKey, joinable );
	}

	/**
	 * 조건처리를 하는 QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImpl( char dataType, String fieldKey, String query, String descriptionKey, String prefixKey ) {
		this( dataType, true, fieldKey, query, descriptionKey, prefixKey, (Joinable)null );
	}

	/**
	 * 조건처리 및 Join을 하는 QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImpl( char dataType, String fieldKey, String query, String descriptionKey, String prefixKey, Joinable joinable ) {
		this( dataType, true, fieldKey, query, descriptionKey, prefixKey, joinable );
	}

	/**
	 * QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImpl( char dataType, boolean conditionable, String fieldKey, String query ) {
		this( dataType, conditionable, fieldKey, query, (Joinable)null );
	}

	/**
	 * Join을 하는 QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImpl( char dataType, boolean conditionable, String fieldKey, String query, Joinable joinable ) {
		super( dataType, fieldKey );
		this.query = query;
		this.joinable = joinable;
		this.conditionable = conditionable;
	}

	/**
	 * QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImpl( char dataType, boolean conditionable, String fieldKey, String query, String descriptionKey ) {
		this( dataType, conditionable, fieldKey, query, descriptionKey, (Joinable)null );
	}

	/**
	 * Join을 하는 QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImpl( char dataType, boolean conditionable, String fieldKey, String query, String descriptionKey, Joinable joinable ) {
		super( dataType, fieldKey, descriptionKey );
		this.query = query;
		this.joinable = joinable;
		this.conditionable = conditionable;
	}

	/**
	 * QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImpl( char dataType, boolean conditionable, String fieldKey, String query, String descriptionKey, String prefixKey ) {
		this( dataType, conditionable, fieldKey, query, descriptionKey, prefixKey, (Joinable)null );
	}

	/**
	 * Join을 하는 QueryableFieldImpl 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImpl( char dataType, boolean conditionable, String fieldKey, String query
						, String descriptionKey, String prefixKey, Joinable joinable ) {
		super( dataType, fieldKey, descriptionKey, prefixKey );
		this.query = query;
		this.joinable = joinable;
		this.conditionable = conditionable;
	}

	QueryableFieldImpl( QueryableFieldImpl qfield ) {
		super( qfield );
		this.query = qfield.query;
		this.joinable = qfield.joinable;
		this.conditionable = qfield.conditionable;
	}

	/**
	 * dataType에 따라 조건 추가.
	 * <ul type='square'>
	 * <li>{@link AbstractField#TYPE_NONE}: 조건없음
	 * <li>{@link AbstractField#TYPE_CODE}:
	 *     {@link ConditionQueryBuffer#findConditionCode findConditionCode}( getFieldKey(), query );
	 * <li>{@link AbstractField#TYPE_DESCRIPTION}:
	 *     {@link ConditionQueryBuffer#findCondition(String, String, String) findCondition}( getFieldKey(), query, {@link Condition#CONDTYPE_STARTSWITH} );
	 * <li>{@link AbstractField#TYPE_STRING}:
	 *     {@link ConditionQueryBuffer#findCondition(String, String) findCondition}( getFieldKey(), query );
	 * <li>{@link AbstractField#TYPE_INTEGER}:
	 *     {@link ConditionQueryBuffer#findConditionNumber(String, String, String) findConditionNumber}( getFieldKey(), query, {@link Condition#CONDTYPE_EQUALS_MINMAX} );
	 * <li>{@link AbstractField#TYPE_LONG}: TYPE_INTEGER과 동일
	 * <li>{@link AbstractField#TYPE_DOUBLE}: TYPE_INTEGER과 동일
	 * <li>{@link AbstractField#TYPE_DATE}:
	 *     {@link ConditionQueryBuffer#findConditionDate(String, String, String) findConditionDate}( getFieldKey(), query, {@link Condition#CONDTYPE_EQUALS_MINMAX} );
	 * <li>{@link AbstractField#TYPE_DATETIME}: TYPE_DATE와 동일
	 * <li>{@link AbstractField#TYPE_TIME}:
	 *     {@link ConditionQueryBuffer#findCondition(String, String) findCondition}( getFieldKey(), query );
	 * </ul>
	 */
	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		if( !conditionable ) return false;

		boolean hasCondition = appendCondition( querybuf, getDataType(), getFieldKey(), query );
		if( hasCondition && joinable != null ) joinable.appendTable( querybuf );

		return hasCondition;
	}

	public static boolean appendCondition( ConditionQueryBuffer querybuf, char dataType, String fieldKey, String query ) {
		switch( dataType ) {
		case AbstractField.TYPE_NONE:
			return false;
		case AbstractField.TYPE_CODE:
			return ( querybuf.findConditionCode( fieldKey, query ) > 0 );
		case AbstractField.TYPE_DESCRIPTION:
			return ( querybuf.findCondition( fieldKey, query, Condition.CONDTYPE_STARTSWITH ) > 0 );
		case AbstractField.TYPE_STRING:
		case AbstractField.TYPE_TIME:
			return ( querybuf.findCondition( fieldKey, query ) > 0 );
		case AbstractField.TYPE_INTEGER:
		case AbstractField.TYPE_LONG:
		case AbstractField.TYPE_DOUBLE:
			return querybuf.findConditionNumber( fieldKey, query, Condition.CONDTYPE_EQUALS_MINMAX );
		case AbstractField.TYPE_DATE:
		case AbstractField.TYPE_DATETIME:
			return querybuf.findConditionDate( fieldKey, query, Condition.CONDTYPE_EQUALS_MINMAX );
		default:
			return false;
		}
	}

	public boolean appendData( QueryBuffer querybuf ) {
		if( querybuf.appendDataWithAlias(query, getFieldKey()) ) {
			if( joinable != null ) joinable.appendTable( querybuf );
			return true;
		}
		return false;
	}

	public String getQuery() {
		return query;
	}

	public Joinable getJoinable() {
		return joinable;
	}

	/**
	 * field에서 새로운 JoinableImpl 생성.
	 * QueryableImpl,JoinableImpl을 extends한 것은 사용하지 말 것.
	 * <p>
	 * 사용법:
	 * <pre>
	 * {
	 *   JoinableImpl joinableImpl_src = new JoinableImpl( "{0}", "SYS_LANG", "{0}.LANG_CD(+) = {1}.LANGCD" );
	 *   QueryableFieldImpl field_src = new QueryableFieldImpl( AbstractField.TYPE_DESCRIPTION, "languageName", "{0}.LANG_NAME", joinableImpl_src );
	 *   QueryableFieldImpl field1 = QueryableFieldImpl.replaceAlias( field_src, "LANG", "ITM" );
	 *   QueryableFieldImpl field2 = QueryableFieldImpl.replaceAlias( field_src, "LANG", "PTY" );
	 * }
	 * </pre>
	 */
	public static QueryableFieldImpl replaceAlias( QueryableFieldImpl field, String... aliases ) {
		String query = MessageFormat.format( field.query.replaceAll("'", "''"), (Object[])aliases );
		Joinable joinable = field.joinable;

		if( joinable instanceof JoinableImpl )
			joinable = JoinableImpl.replaceAlias( (JoinableImpl)joinable, aliases );

		if( query.equals(field.query) && field.joinable == joinable )
			return field;

		field = new QueryableFieldImpl( field );
		field.query = query;
		field.joinable = joinable;

		return field;
	}
}
