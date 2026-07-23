/*
 *	File Name:	JoinableImpl.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.1	appendTable(): (joinable != null)일 때 언제나 joinable.appendTable() 수행
 *	stghr12		2007/07/31		2.1.0	replaceAlias(): conditionQuery에 '가 있을 경우 제대로 동작하지 않는 오류 수정
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

import java.text.MessageFormat;

/**
 * Joinable를 구현한 Class.
 */
public class JoinableImpl implements Joinable {
	String alias, name, conditionQuery;
	Joinable joinable;

	/**
	 * {@link QueryBuffer#appendTableWithAlias(String, String) querybuf.appendTableWithAlias}( name, alias );
	 */
	public JoinableImpl( String alias, String name ) {
		this( alias, name, null, null );
	}

	/**
	 * {@link QueryBuffer#appendTableWithAlias(String, String, String) querybuf.appendTableWithAlias}( name, alias, conditionQuery );
	 */
	public JoinableImpl( String alias, String name, String conditionQuery ) {
		this( alias, name, conditionQuery, null );
	}

	/**
	 * {@link QueryBuffer#appendTableWithAlias(String, String, String) querybuf.appendTableWithAlias}( name, alias, conditionQuery );
	 * {@link Joinable#appendTable(QueryBuffer) joinable.appendTable}( querybuf );
	 */
	public JoinableImpl( String alias, String name, String conditionQuery, Joinable joinable ) {
		this.alias = alias;
		this.name = name;
		this.conditionQuery = conditionQuery;
		this.joinable = joinable;
	}

	public boolean appendTable( QueryBuffer querybuf ) {
		boolean done;
		if( conditionQuery != null )
			done = querybuf.appendTableWithAlias( name, alias, conditionQuery );
		else
			done = querybuf.appendTableWithAlias( name, alias );
		if( joinable != null ) joinable.appendTable( querybuf );

		return done;
	}

	public boolean existTable( QueryBuffer querybuf ) {
		return querybuf.existTableAlias( alias );
	}

	/**
	 * joinable에서 새로운 JoinableImpl 생성.
	 * JoinableImpl을 extends한 것은 사용하지 말 것.
	 * <p>
	 * 사용법:
	 * <pre>
	 * {
	 *   JoinableImpl joinableImpl_src = new JoinableImpl( "{0}", "SYS_LANG", "{0}.LANG_CD(+) = {1}.LANGCD" );
	 *   JoinableImpl joinableImpl1 = JoinableImpl.replaceAlias( joinableImpl_src, "LANG", "ITM" );
	 *   JoinableImpl joinableImpl2 = JoinableImpl.replaceAlias( joinableImpl_src, "LANG", "PTY" );
	 * }
	 * </pre>
	 */
	public static JoinableImpl replaceAlias( JoinableImpl joinable, String... aliases ) {
		String alias;
		String conditionQuery = joinable.conditionQuery;
		Joinable joinable_new = joinable.joinable;

		alias = MessageFormat.format( joinable.alias, (Object[])aliases );
		if( conditionQuery != null )
			conditionQuery = MessageFormat.format( conditionQuery.replaceAll("'", "''"), (Object[])aliases );
		if( joinable_new != null && joinable_new instanceof JoinableImpl )
			joinable_new = JoinableImpl.replaceAlias( (JoinableImpl)joinable_new, aliases );

		if( alias.equals(joinable.alias)
				&& (conditionQuery == null || conditionQuery.equals(joinable.conditionQuery))
				&& joinable.joinable == joinable_new )
			return joinable;

		return new JoinableImpl( alias, joinable.name, conditionQuery, joinable_new );
	}
}
