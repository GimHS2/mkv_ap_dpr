/*
 *	File Name:	InnerJoinable.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2010/08/31		2.2.1	implements JoinableGetter
 *	stghr12		2009/10/31		2.2.0	create
 *
**/

package com.irt.sql;

import java.util.Map;

/**
 * InnerQueryBuffer를 사용하는 Joinable Class
 */
public abstract class InnerJoinable implements Joinable, JoinableGetter {
	String alias;

	public InnerJoinable( String alias ) {
		this.alias = alias;
	}

	public boolean appendData( QueryBuffer querybuf, String fieldKey ) {
		InnerQueryBuffer inner_querybuf = getInnerTableQueryBuffer( querybuf, true );
		if( inner_querybuf == null ) return false;

		return appendInnerData( inner_querybuf, fieldKey );
	}

	public abstract boolean appendInnerData( InnerQueryBuffer inner_querybuf, String fieldKey );

	public boolean appendTable( QueryBuffer querybuf ) {
		InnerQueryBuffer inner_querybuf = querybuf.getInnerQueryBuffer( alias );
		if( inner_querybuf == null ) {
			if( querybuf instanceof ConditionQueryBuffer )
				inner_querybuf = getInnerTableQueryBuffer( ((ConditionQueryBuffer)querybuf).getConditionMap() );
			else
				inner_querybuf = getInnerTableQueryBuffer( null );

			querybuf.appendTable( inner_querybuf, alias );
			return true;
		}

		return false;
	}

	public boolean existTable( QueryBuffer querybuf ) {
		return ( querybuf.getInnerQueryBuffer(alias) != null );
	}

	public String getAlias() {
		return alias;
	}

	public abstract InnerQueryBuffer getInnerTableQueryBuffer( Map<String, ? extends Object> conditionMap );

	protected InnerQueryBuffer getInnerTableQueryBuffer( QueryBuffer querybuf, boolean create ) {
		InnerQueryBuffer inner_querybuf = querybuf.getInnerQueryBuffer( alias );
		if( inner_querybuf == null && create ) {
			appendTable( querybuf );
			inner_querybuf = querybuf.getInnerQueryBuffer( alias );
		}

		return inner_querybuf;
	}

	public Joinable getJoinable( String... fieldKeys ) {
		return new InnerJoinable.FieldJoinable( fieldKeys );
	}

	/**
	 *
	 */
	private class FieldJoinable implements Joinable {
		String[] fieldKeys;

		FieldJoinable( String... fieldKeys ) {
			this.fieldKeys = fieldKeys;
		}

		public boolean appendTable( QueryBuffer querybuf ) {
			InnerQueryBuffer inner_querybuf = getInnerTableQueryBuffer( querybuf, true );
			if( inner_querybuf == null ) return false;

			boolean done = false;
			for( String fieldKey : fieldKeys )
				if( appendInnerData(inner_querybuf, fieldKey) )
					done = true;

			return done;
		}

		public boolean existTable( QueryBuffer querybuf ) {
			InnerQueryBuffer inner_querybuf = querybuf.getInnerQueryBuffer( alias );
			if( inner_querybuf == null ) return false;

			for( String fieldKey : fieldKeys )
				if( !inner_querybuf.existDataAlias(fieldKey) )
					return false;

			return true;
		}
	}
}
