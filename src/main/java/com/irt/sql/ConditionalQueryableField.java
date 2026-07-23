/*
 *	File Name:	ConditionalQueryableField.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	makeConditionalQueryableField() -> makeConditionalQueryableFields()
 *	stghr12		2007/10/31		2.1.2	ConditionalQueryableField(): 생성자 로직 변경
 *										getQueryBufferValid() 추가
 *	stghr12		2007/04/30		2.1.1	final class로 수정
 *										makeConditionalQueryableField() 추가
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.sql;

/**
 * 조건이 맞을 경우에만 추가되는 QueryableField Class.
 */
public final class ConditionalQueryableField extends com.irt.data.AbstractFieldWrapper implements QueryableField {
	QueryableField qfield;
	QueryBufferValid querybufValid;

	public ConditionalQueryableField( QueryBufferValid querybufValid, QueryableField qfield ) {
		super( qfield );
		if( qfield instanceof ConditionalQueryableField ) {
			this.qfield = ((ConditionalQueryableField)qfield).qfield;
			this.querybufValid = new QueryBufferValid.Join( querybufValid, ((ConditionalQueryableField)qfield).querybufValid );
		} else {
			this.qfield = qfield;
			this.querybufValid = querybufValid;
		}
	}

	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		return( querybufValid.hasValidCondition(querybuf) && qfield.appendCondition(querybuf) );
	}

	public boolean appendData( QueryBuffer querybuf ) {
		return( querybufValid.hasValidCondition(querybuf) && qfield.appendData(querybuf) );
	}

	public QueryBufferValid getQueryBufferValid() {
		return querybufValid;
	}

	public static ConditionalQueryableField[] makeConditionalQueryableFields( QueryBufferValid querybufValid, QueryableField[] qfields ) {
		ConditionalQueryableField[] cqfields = new ConditionalQueryableField[ qfields.length ];

		for( int i = 0; i < qfields.length; i++ )
			cqfields[i] = new ConditionalQueryableField( querybufValid, qfields[i] );

		return cqfields;
	}
}
