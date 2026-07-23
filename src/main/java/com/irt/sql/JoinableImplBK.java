/*
 *	File Name:	JoinableImplBK.java
 *	Version:	2.1.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/04/30		2.1.0	create
 *
**/

package com.irt.sql;

/**
 * Joinable를 구현한 Class.
 */
public class JoinableImplBK implements Joinable {
	String alias, name, conditionQuery;
	String[] bindFieldKeys;
	Joinable joinable;

	/**
	 * {@link QueryBuffer#appendTableWithAlias(String, String, String) querybuf.appendTableWithAlias}( name, alias, conditionQuery );
	 */
	public JoinableImplBK( String alias, String name, String conditionQuery, String bindFieldKey ) {
		this( alias, name, conditionQuery, new String[] { bindFieldKey }, null );
	}

	/**
	 * {@link QueryBuffer#appendTableWithAlias(String, String, String) querybuf.appendTableWithAlias}( name, alias, conditionQuery );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public JoinableImplBK( String alias, String name, String conditionQuery, String bindFieldKey, Joinable joinable ) {
		this( alias, name, conditionQuery, new String[] { bindFieldKey }, joinable );
	}

	/**
	 * {@link QueryBuffer#appendTableWithAlias(String, String, String) querybuf.appendTableWithAlias}( name, alias, conditionQuery );
	 */
	public JoinableImplBK( String alias, String name, String conditionQuery, String[] bindFieldKeys ) {
		this( alias, name, conditionQuery, bindFieldKeys, null );
	}

	/**
	 * {@link QueryBuffer#appendTableWithAlias(String, String, String) querybuf.appendTableWithAlias}( name, alias, conditionQuery );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public JoinableImplBK( String alias, String name, String conditionQuery, String[] bindFieldKeys, Joinable joinable ) {
		this.alias = alias;
		this.name = name;
		this.conditionQuery = conditionQuery;
		this.bindFieldKeys = bindFieldKeys;
		this.joinable = joinable;
	}

	public boolean appendTable( QueryBuffer querybuf ) {
		boolean done;
		if( conditionQuery != null ) {
			if( done = querybuf.appendTableWithAlias( name, alias, conditionQuery ) ) {
				if( querybuf instanceof ConditionQueryBuffer ) {
					ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;
					for( int f = 0; f < bindFieldKeys.length; f++ )
						querybuf.addBindVariable( QueryBuffer.COND_BINDVAR, condquerybuf.getConditionValue(bindFieldKeys[f]) );
				} else
					for( int f = 0; f < bindFieldKeys.length; f++ )
						querybuf.addBindVariable( QueryBuffer.COND_BINDVAR, null );
			}
		} else
			done = querybuf.appendTableWithAlias( name, alias );
		if( done && joinable != null ) joinable.appendTable( querybuf );

		return done;
	}

	public boolean existTable( QueryBuffer querybuf ) {
		return querybuf.existTableAlias( alias );
	}
}
