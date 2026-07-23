/*
 *	File Name:	JoinableWrapper.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/10/14		2.0.1	Joinable[]를 이용한 생성자 버그 수정
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.sql;

import java.util.List;

/**
 * Joinable Wrapper Class.
 * <p>
 * <ul type='square'>
 * <li>conditionQuery 추가
 * <li>여러 개의 Joinable Union
 * </ul>
 */
public final class JoinableWrapper implements Joinable {
	Joinable[] joinables;
	String conditionQuery;

	/**
	 * {@link #appendTable(QueryBuffer) appendTable}할 때, conditionQuery를 추가.
	 */
	public JoinableWrapper( Joinable joinable, String conditionQuery ) {
		this.joinables = new Joinable[] { joinable };
		this.conditionQuery = conditionQuery;

		if( joinable instanceof JoinableWrapper ) {
			JoinableWrapper joinablew = (JoinableWrapper)joinable;
			if( joinablew.conditionQuery == null )
				this.joinables = joinablew.joinables;
			else if( joinablew.joinables.length == 1 ) {
				this.joinables = joinablew.joinables;
				this.conditionQuery = joinablew.conditionQuery;
				if( conditionQuery != null )
					this.conditionQuery += " AND "+ conditionQuery;
			}
		}
	}

	/**
	 * {@link #appendTable(QueryBuffer) appendTable}할 때, 모든 Joinable 추가.
	 */
	public JoinableWrapper( Joinable... joinables ) {
		List<Joinable> list = addJoinables( new java.util.ArrayList<Joinable>(), joinables );
		this.joinables = list.toArray( new Joinable[list.size()] );
		this.conditionQuery = null;
	}

	private List<Joinable> addJoinables( List<Joinable> list, Joinable[] joinables ) {
		for( int i = 0; i < joinables.length; i++ ) {
			if( joinables[i] instanceof JoinableWrapper ) {
				JoinableWrapper joinablew = (JoinableWrapper)joinables[i];
				if( joinablew.conditionQuery != null )
					list.add( joinablew );
				else
					addJoinables( list, joinablew.joinables );
			} else
				list.add( joinables[i] );
		}
		return list;
	}

	public boolean appendTable( QueryBuffer querybuf ) {
		boolean done = false;
		for( int i = 0; i < joinables.length; i++ )
			done = ( joinables[i].appendTable(querybuf) || done );

		if( done && joinables.length == 1 && conditionQuery != null )
			querybuf.appendCondition( conditionQuery );

		return done;
	}

	public boolean existTable( QueryBuffer querybuf ) {
		for( int i = 0; i < joinables.length; i++ )
			if( !joinables[i].existTable(querybuf) )
				return false;

		return true;
	}
}
