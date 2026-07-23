/*
 *	File Name:	QueryableFieldWrapper.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	RecordFormat 변경사항 적용: format( recordMap, msghandler, stringBuffer ) 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getValidDateFormat() 삭제
 *	stghr12		2007/10/31		2.1.0	AbstractFieldWrapper extends 제거: QueryableFieldWrapper를 중복사용할 경우의 Perpormance를 위해서 수정
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

import com.irt.util.MessageHandler;
import java.util.Map;

/**
 * QueryableField Wrapper Class.
 * <p>
 * <ul type='square'>
 * <li>Joinable추가
 * <li>appendCondition() 비활성화
 * </ul>
 */
public final class QueryableFieldWrapper implements QueryableField {
	QueryableField qfield;
	Joinable joinable;
	boolean conditionable;

	/**
	 * {@link #QueryableFieldWrapper( QueryableField, Joinable, boolean ) this}( qfield, null, true );
	 * @see #QueryableFieldWrapper( QueryableField, Joinable, boolean )
	 */
	public QueryableFieldWrapper( QueryableField qfield ) {
		this( qfield, null, true );
	}

	/**
	 * {@link #QueryableFieldWrapper( QueryableField, Joinable, boolean ) this}( qfield, null, conditionable );
	 * @see #QueryableFieldWrapper( QueryableField, Joinable, boolean )
	 */
	public QueryableFieldWrapper( QueryableField qfield, boolean conditionable ) {
		this( qfield, null, conditionable );
	}

	/**
	 * {@link #QueryableFieldWrapper( QueryableField, Joinable, boolean ) this}( qfield, joinable, true );
	 * @see #QueryableFieldWrapper( QueryableField, Joinable, boolean )
	 */
	public QueryableFieldWrapper( QueryableField qfield, Joinable joinable ) {
		this( qfield, joinable, true );
	}

	/**
	 * @param joinable		qfield추가시 {@link Joinable#appendTable(QueryBuffer) Joinable.appendTable}수행
	 * @param conditionable	false일 경우 {@link #appendCondition(ConditionQueryBuffer) appendCondition}에서 아무것도 하지 않음
	 */
	public QueryableFieldWrapper( QueryableField qfield, Joinable joinable, boolean conditionable ) {
		if( qfield instanceof QueryableFieldWrapper ) {
			QueryableFieldWrapper qfieldw = (QueryableFieldWrapper)qfield;
			this.qfield = qfieldw.qfield;
			if( joinable == null )
				this.joinable = qfieldw.joinable;
			else if( qfieldw.joinable == null )
				this.joinable = joinable;
			else
				this.joinable = new JoinableWrapper( joinable, qfieldw.joinable );
			this.conditionable = ( qfieldw.conditionable && conditionable );
		} else {
			this.qfield = qfield;
			this.joinable = joinable;
			this.conditionable = conditionable;
		}
	}

	public void addFieldKeyToSet( java.util.Set<String> fieldKeySet ) {
		qfield.addFieldKeyToSet( fieldKeySet );
	}

	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		if( conditionable && qfield.appendCondition(querybuf) ) {
			if( joinable != null ) joinable.appendTable( querybuf );
			return true;
		}
		return false;
	}

	public boolean appendData( QueryBuffer querybuf ) {
		if( qfield.appendData(querybuf) ) {
			if( joinable != null ) joinable.appendTable( querybuf );
			return true;
		}
		return false;
	}

	public String format( java.util.Map recordMap, com.irt.util.MessageHandler msghandler ) {
		return qfield.format( recordMap, msghandler );
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		return qfield.format( recordMap, msghandler, stringBuffer );
	}

	public char getDataType() {
		return qfield.getDataType();
	}

	public String getDescriptionKey() {
		return qfield.getDescriptionKey();
	}

	public String getFieldKey() {
		return qfield.getFieldKey();
	}

	public String getPrefixKey() {
		return qfield.getPrefixKey();
	}

	public com.irt.data.AbstractField getSourceField() {
		return qfield;
	}
}
