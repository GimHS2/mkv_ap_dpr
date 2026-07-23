/*
 *	File Name:	NestedJoinable.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/08/31		2.2.3	varargs 사용([] -> ...)
 *	stghr12		2009/10/31		2.2.2	extends InnerJoinable
 *	stghr12		2009/06/30		2.2.1	NestedJoinable.AbstractField 추가
 *										getAlias() 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.2	getInnerTableQueryBuffer( querybuf ) 삭제
 *										getInnerTableQueryBuffer( querybuf, create ) : create 사용하도록 수정
 *										FieldJoinable: public -> private
 *	stghr12		2007/07/31		2.1.1	NestedJoinable.NestedJoinable 생성자 변경, NestedJoinable.Field.appendData() 추가
 *										NestedJoinable.FieldBK 추가
 *										appendInnerData(): NestedJoinable.Field.appendData() 사용
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.sql;

import java.util.Map;

/**
 * InnerQueryBuffer를 사용하는 Joinable Class
 */
public abstract class NestedJoinable extends InnerJoinable {
	Map<String, NestedJoinable.AbstractField> fieldMap;

	public NestedJoinable( String alias ) {
		super( alias );
		this.fieldMap = null;
	}

	public NestedJoinable( String alias, NestedJoinable.AbstractField... nfields ) {
		super( alias );
		initNestedFields( nfields );
	}

	public boolean appendInnerData( InnerQueryBuffer inner_querybuf, String fieldKey ) {
		if( inner_querybuf instanceof QueryBuffer )
			return appendInnerData( (QueryBuffer)inner_querybuf, fieldKey );
		else
			return false;
	}

	public boolean appendInnerData( QueryBuffer inner_querybuf, String fieldKey ) {
		NestedJoinable.AbstractField nfield = fieldMap.get( fieldKey );
		if( nfield == null ) return false;

		return nfield.appendData( inner_querybuf );
	}

	public abstract QueryBuffer getInnerTableQueryBuffer( Map<String, ? extends Object> conditionMap );

	protected void initNestedFields( NestedJoinable.AbstractField... nfields ) {
		this.fieldMap = new java.util.LinkedHashMap<String, NestedJoinable.AbstractField>();
		for( NestedJoinable.AbstractField nfield : nfields )
			this.fieldMap.put( nfield.getFieldKey(), nfield );
	}

	/**
	 *
	 */
	public static interface AbstractField {
		public boolean appendData( QueryBuffer inner_querybuf );

		public String getFieldKey();
	}

	/**
	 *
	 */
	public static class Field implements AbstractField {
		String fieldKey, query;
		Joinable joinable;

		public Field( String fieldKey, String query ) {
			this( fieldKey, query, null );
		}

		public Field( String fieldKey, String query, Joinable joinable ) {
			this.fieldKey = fieldKey;
			this.query = query;
			this.joinable = joinable;
		}

		public boolean appendData( QueryBuffer inner_querybuf ) {
			if( inner_querybuf.appendDataWithAlias(query, fieldKey) ) {
				if( joinable != null ) joinable.appendTable( inner_querybuf );
				return true;
			}

			return false;
		}

		public String getFieldKey() {
			return fieldKey;
		}
	}

	/**
	 *
	 */
	public static class FieldBK extends NestedJoinable.Field {
		String[] bindFieldKeys;

		public FieldBK( String fieldKey, String query, String bindFieldKey ) {
			this( fieldKey, query, new String[] { bindFieldKey }, null );
		}

		public FieldBK( String fieldKey, String query, String[] bindFieldKeys ) {
			this( fieldKey, query, bindFieldKeys, null );
		}

		public FieldBK( String fieldKey, String query, String bindFieldKey, Joinable joinable ) {
			this( fieldKey, query, new String[] { bindFieldKey }, joinable );
		}

		public FieldBK( String fieldKey, String query, String[] bindFieldKeys, Joinable joinable ) {
			super( fieldKey, query, joinable );
			this.bindFieldKeys = bindFieldKeys;
		}

		public boolean appendData( QueryBuffer inner_querybuf ) {
			if( super.appendData(inner_querybuf) ) {
				if( inner_querybuf instanceof ConditionQueryBuffer ) {
					ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)inner_querybuf;
					for( String bindFieldKey : bindFieldKeys )
						inner_querybuf.addBindVariable( QueryBuffer.DATA_BINDVAR, condquerybuf.getConditionValue(bindFieldKey) );
				} else
					for( String bindFieldKey : bindFieldKeys )
						inner_querybuf.addBindVariable( QueryBuffer.DATA_BINDVAR, null );

				return true;
			}

			return false;
		}
	}
}
