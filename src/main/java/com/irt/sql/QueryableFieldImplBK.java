/*
 *	File Name:	QueryableFieldImplBK.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	AbstractField 변경사항 적용: validDateFormat 삭제
 *	stghr12		2007/10/31		2.1.1	QueryableFieldImplBK(field): bindFieldKeys 잘못 설정하는 버그 수정.
 *	stghr12		2007/04/30		2.1.0	create
 *
**/

package com.irt.sql;

/**
 * QueryableField를 구현한 Class.
 */
public class QueryableFieldImplBK extends com.irt.data.Field implements QueryableField {
	String query;
	String[] bindFieldKeys;
	Joinable joinable;

	/**
	 * QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String bindFieldKey ) {
		this( dataType, fieldKey, query, new String[] { bindFieldKey }, (Joinable)null );
	}

	/**
	 * Join을 하는 QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String bindFieldKey, Joinable joinable ) {
		this( dataType, fieldKey, query, new String[] { bindFieldKey }, joinable );
	}

	/**
	 * QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String[] bindFieldKeys ) {
		this( dataType, fieldKey, query, bindFieldKeys, (Joinable)null );
	}

	/**
	 * Join을 하는 QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String[] bindFieldKeys, Joinable joinable ) {
		super( dataType, fieldKey );
		this.query = query;
		this.bindFieldKeys = bindFieldKeys;
		this.joinable = joinable;
	}

	/**
	 * QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String bindFieldKey, String descriptionKey ) {
		this( dataType, fieldKey, query, new String[] { bindFieldKey }, descriptionKey, (Joinable)null );
	}

	/**
	 * Join을 하는 QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String bindFieldKey, String descriptionKey, Joinable joinable ) {
		this( dataType, fieldKey, query, new String[] { bindFieldKey }, descriptionKey, joinable );
	}

	/**
	 * QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String[] bindFieldKeys, String descriptionKey ) {
		this( dataType, fieldKey, query, bindFieldKeys, descriptionKey, (Joinable)null );
	}

	/**
	/**
	 * Join을 하는 QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String[] bindFieldKeys, String descriptionKey, Joinable joinable ) {
		super( dataType, fieldKey, descriptionKey );
		this.query = query;
		this.bindFieldKeys = bindFieldKeys;
		this.joinable = joinable;
	}

	/**
	 * QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String bindFieldKey, String descriptionKey, String prefixKey ) {
		this( dataType, fieldKey, query, new String[] { bindFieldKey }, descriptionKey, prefixKey, (Joinable)null );
	}

	/**
	 * Join을 하는 QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String bindFieldKey
						, String descriptionKey, String prefixKey, Joinable joinable ) {
		this( dataType, fieldKey, query, new String[] { bindFieldKey }, descriptionKey, prefixKey, joinable );
	}

	/**
	 * QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String[] bindFieldKeys, String descriptionKey, String prefixKey ) {
		this( dataType, fieldKey, query, bindFieldKeys, descriptionKey, prefixKey, (Joinable)null );
	}

	/**
	 * Join을 하는 QueryableFieldImplBK 생성.
	 * {@link QueryBuffer#appendDataWithAlias(String, String) querybuf.appendDataWithAlias}( query, fieldKey );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public QueryableFieldImplBK( char dataType, String fieldKey, String query, String[] bindFieldKeys
						, String descriptionKey, String prefixKey, Joinable joinable ) {
		super( dataType, fieldKey, descriptionKey, prefixKey );
		this.query = query;
		this.bindFieldKeys = bindFieldKeys;
		this.joinable = joinable;
	}

	QueryableFieldImplBK( QueryableFieldImplBK qfield ) {
		super( qfield );
		this.query = qfield.query;
		this.bindFieldKeys = qfield.bindFieldKeys;
		this.joinable = qfield.joinable;
	}

	public boolean appendCondition( ConditionQueryBuffer querybuf ) {
		return false;
	}

	public boolean appendData( QueryBuffer querybuf ) {
		if( querybuf.appendDataWithAlias(query, getFieldKey()) ) {
			if( querybuf instanceof ConditionQueryBuffer ) {
				ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;
				for( int f = 0; f < bindFieldKeys.length; f++ )
					querybuf.addBindVariable( QueryBuffer.DATA_BINDVAR, condquerybuf.getConditionValue(bindFieldKeys[f]) );
			} else
				for( int f = 0; f < bindFieldKeys.length; f++ )
					querybuf.addBindVariable( QueryBuffer.DATA_BINDVAR, null );

			if( joinable != null ) joinable.appendTable( querybuf );
			return true;
		}
		return false;
	}

	public String getQuery() {
		return query;
	}
}
