/*
 *	File Name:	QueryBuffer.java
 *	Version:	2.2.9
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2016/09/30		2.2.9	Java Document 주석 오류 수정
 *	stghr12		2011/06/30		2.2.8	WITH문 지원: appendWith() 추가
 *	stghr12		2011/02/28		2.2.7	appendConditionByField(): caseSensitive 추가, CONDTYPE_NOT_STARTSWITH 오류수정(OR -> AND)
 *	stghr12		2010/09/30		2.2.6	hasDataQuery() 추가
 *	GimHS		2010/08/31		2.2.5	appendConnectBy(), clearConnectBy(), getConnectByQuery() 추가
 *	yjcha		2010/04/30		2.2.4	appendConditionByField(): CONDTYPE_NOT_STARTSWITH 추가
 *	stghr12		2009/10/31		2.2.3	clearCondition(), getDataAliases(), getDataQuery(), getOrderByQuery() 추가
 *										InnerQueryBuffer(v2.2.0) 사용
 *	stghr12		2009/06/30		2.2.2	union(): savedQuery 처리 안 되는 오류 수정
 *	stghr12		2009/01/31		2.2.1	appendHaving(), clearHaving() 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.2	appendLock(), hasConditionQuery(), hasSavedQuery(), usingDistinct(), usingLock() 추가
 *	stghr12		2007/04/30		2.1.1	appendConditionByField(), appendConditionByFieldRange(), replaceTable() 추가
 *										생성자 QueryBuffer( querybuf ) 추가
 *	stghr12		2006/12/01		2.1.0	getTableAliasCount() 추가
 *										appendTable( querybuf, alias, conditionQuery ) 추가
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/11		1.0.0	create
 *
**/

package com.irt.sql;

import com.irt.data.Condition;
import java.util.List;
import java.util.Map;

/**
 * Query를 저장하는 BufferedString.
 * <p>
 * HINT절, SELECT절, FROM절, WHERE절, CONNECT BY절, GROUP BY절, HAVING절, ORDER BY절을 각각 다른 StringBuffer에 저장.
 * bindVariable를 관리.
 */
public class QueryBuffer implements InnerQueryBuffer {
	/** bindVariable을 추가할 위치: WITH절 */
	public final static int WITH_BINDVAR				= 0;
	/** bindVariable을 추가할 위치: SELECT절 */
	public final static int DATA_BINDVAR				= 1;
	/** bindVariable을 추가할 위치: FROM절 */
	public final static int FROM_BINDVAR				= 2;
	/** bindVariable을 추가할 위치: WHERE절 */
	public final static int COND_BINDVAR				= 3;
	/** bindVariable을 추가할 위치: CONNECT BY절 */
	public final static int CONNECT_BINDVAR				= 4;
	/** bindVariable을 추가할 위치: HAVING절 */
	public final static int HAVING_BINDVAR				= 5;

	private final static String DISTINCT_QUERY			= "DISTINCT ";

	List<Object>[] bindVars;
	boolean useDistinct, useLock, useLockWaiting;
	String savedQuery;
	StringBuffer withbuf, hintbuf, databuf, frombuf, condbuf, connectbuf, groupbuf, havingbuf, orderbuf;
	Map<String, Object> dataAliasMap, tableAliasMap;
	List<Object[]> innerQueryBufferList;

	public QueryBuffer() {
		clear();
	}

	public QueryBuffer( QueryBuffer querybuf ) {
		clear();

		if( querybuf.bindVars[WITH_BINDVAR] != null )
			this.bindVars[WITH_BINDVAR] = new java.util.ArrayList<Object>( querybuf.bindVars[WITH_BINDVAR] );
		if( querybuf.bindVars[DATA_BINDVAR] != null )
			this.bindVars[DATA_BINDVAR] = new java.util.ArrayList<Object>( querybuf.bindVars[DATA_BINDVAR] );
		if( querybuf.bindVars[FROM_BINDVAR] != null )
			this.bindVars[FROM_BINDVAR] = new java.util.ArrayList<Object>( querybuf.bindVars[FROM_BINDVAR] );
		this.bindVars[COND_BINDVAR] = new java.util.ArrayList<Object>( querybuf.bindVars[COND_BINDVAR] );
		if( querybuf.bindVars[CONNECT_BINDVAR] != null )
			this.bindVars[CONNECT_BINDVAR] = new java.util.ArrayList<Object>( querybuf.bindVars[CONNECT_BINDVAR] );
		if( querybuf.bindVars[HAVING_BINDVAR] != null )
			this.bindVars[HAVING_BINDVAR] = new java.util.ArrayList<Object>( querybuf.bindVars[HAVING_BINDVAR] );
		this.useDistinct = querybuf.useDistinct;
		this.useLock = querybuf.useLock;
		this.useLockWaiting = querybuf.useLockWaiting;
		this.savedQuery = querybuf.savedQuery;

		if( (this.withbuf = querybuf.withbuf) != null ) this.withbuf = new StringBuffer( querybuf.withbuf.toString() );
		if( (this.hintbuf = querybuf.hintbuf) != null ) this.hintbuf = new StringBuffer( querybuf.hintbuf.toString() );
		if( (this.databuf = querybuf.databuf) != null ) this.databuf = new StringBuffer( querybuf.databuf.toString() );
		if( (this.frombuf = querybuf.frombuf) != null ) this.frombuf = new StringBuffer( querybuf.frombuf.toString() );
		if( (this.condbuf = querybuf.condbuf) != null ) this.condbuf = new StringBuffer( querybuf.condbuf.toString() );
		if( (this.connectbuf = querybuf.connectbuf) != null ) this.connectbuf = new StringBuffer( querybuf.connectbuf.toString() );
		if( (this.groupbuf = querybuf.groupbuf) != null ) this.groupbuf = new StringBuffer( querybuf.groupbuf.toString() );
		if( (this.havingbuf = querybuf.havingbuf) != null ) this.havingbuf = new StringBuffer( querybuf.havingbuf.toString() );
		if( (this.orderbuf = querybuf.orderbuf) != null ) this.orderbuf = new StringBuffer( querybuf.orderbuf.toString() );
		this.dataAliasMap = new java.util.HashMap<String, Object>( querybuf.dataAliasMap );
		this.tableAliasMap = new java.util.HashMap<String, Object>( querybuf.tableAliasMap );
		if( (this.innerQueryBufferList = querybuf.innerQueryBufferList) != null )
			this.innerQueryBufferList = new java.util.ArrayList<Object[]>( querybuf.innerQueryBufferList );
	}

	/**
	 * bindVariable을 하나 추가.
	 * @param bindIdx	{@link #DATA_BINDVAR}: SELECT절에 BIND, {@link #FROM_BINDVAR}: FROM절에 BIND, {@link #COND_BINDVAR}: WHERE절에 BIND
	 */
	public void addBindVariable( int bindIdx, Object bindVar ) {
		try {
			bindVars[bindIdx].add( bindVar );
		} catch( NullPointerException nullEx ) {
			if( bindVars[bindIdx] != null ) throw nullEx;
			bindVars[bindIdx] = new java.util.ArrayList<Object>();
			bindVars[bindIdx].add( bindVar );
		}
	}

	/**
	 * bindVariable을 추가.
	 * @param bindIdx	{@link #DATA_BINDVAR}: SELECT절에 BIND, {@link #FROM_BINDVAR}: FROM절에 BIND, {@link #COND_BINDVAR}: WHERE절에 BIND
	 */
	public void addBindVariables( int bindIdx, Object... bindVars ) {
		addBindVariable( bindIdx, bindVars[0] );
		for( int i = 1; i < bindVars.length; i++ )
			this.bindVars[bindIdx].add( bindVars[i] );
	}

	/**
	 * bindVariable을 추가.
	 * @param bindIdx	{@link #DATA_BINDVAR}: SELECT절에 BIND, {@link #FROM_BINDVAR}: FROM절에 BIND, {@link #COND_BINDVAR}: WHERE절에 BIND
	 */
	public void addBindVariables( int bindIdx, List<? extends Object> bindVars ) {
		try {
			this.bindVars[bindIdx].addAll( bindVars );
		} catch( NullPointerException nullEx ) {
			if( this.bindVars[bindIdx] != null ) throw nullEx;
			this.bindVars[bindIdx] = new java.util.ArrayList<Object>();
			this.bindVars[bindIdx].addAll( bindVars );
		}
	}

	/**
	 * SELECT절에 내용 추가.
	 * @see #appendData(String)
	 */
	public QueryBuffer append( String query ) {
		return appendData( query );
	}

	/**
	 * WHERE절에 내용 추가.
	 */
	public QueryBuffer appendCondition( String query ) {
		try {
			condbuf.append( " AND " ).append( query );
		} catch( NullPointerException nullEx ) {
			if( condbuf != null ) throw nullEx;
			condbuf = new StringBuffer( query );
		}
		return this;
	}

	/**
	 * WHERE절에 내용 및 bindVariable 추가.
	 */
	public QueryBuffer appendCondition( String query, Object... bindVars ) {
		addBindVariables( COND_BINDVAR, bindVars );
		return appendCondition( query );
	}

	public QueryBuffer appendConditionByField( String fieldName, Object conditionValue ) {
		return appendConditionByField( fieldName, conditionValue, null, true );
	}

	public QueryBuffer appendConditionByField( String fieldName, Object conditionValue, boolean caseSensitive ) {
		return appendConditionByField( fieldName, conditionValue, null, caseSensitive );
	}

	public QueryBuffer appendConditionByField( String fieldName, Object conditionValue, Object conditionType ) {
		return appendConditionByField( fieldName, conditionValue, conditionType, true );
	}

	public QueryBuffer appendConditionByField( String fieldName, Object conditionValue, Object conditionType, boolean caseSensitive ) {
		Object[] conditionValues;
		if( conditionValue instanceof Object[] )
			conditionValues = (Object[])conditionValue;
		else
			conditionValues = new Object[] { conditionValue };

		String operation = ( caseSensitive ? "?" : "LOWER(?)" );
		if( conditionType == null || Condition.CONDTYPE_EQUALS.equals(conditionType) ) {
			conditionType = Condition.CONDTYPE_EQUALS;
			operation = " = "+ operation;
		} else if( Condition.CONDTYPE_NOTEQUALS.equals(conditionType) )
			operation = " <> "+ operation;
		else if( Condition.CONDTYPE_LIKE.equals(conditionType) )
			operation = " LIKE "+ operation;
		else if( Condition.CONDTYPE_STARTSWITH.equals(conditionType) )
			operation = " LIKE "+ operation +" || '%'";
		else if( Condition.CONDTYPE_ENDSWITH.equals(conditionType) )
			operation = " LIKE "+ "'%' || "+ operation;
		else if( Condition.CONDTYPE_NOT_STARTSWITH.equals( conditionType) )
			operation = " NOT LIKE "+ operation +" || '%'";
		else if( Condition.CONDTYPE_CONTAINS.equals(conditionType) )
			operation = " LIKE '%' || "+ operation +" || '%'";
		else if( Condition.CONDTYPE_RANGE.equals(conditionType) )
			return appendConditionByFieldRange( fieldName, conditionValues[0] == null ? null : conditionValues[0].toString(), false );
		else {
			conditionType = Condition.CONDTYPE_EQUALS;
			operation = " = ?";
		}

		// 조건 설정
		StringBuffer sbuf = new StringBuffer();
		if( conditionValues.length > 1 && (Condition.CONDTYPE_EQUALS.equals(conditionType) || Condition.CONDTYPE_NOTEQUALS.equals(conditionType)) ) {
			sbuf.append( caseSensitive ? fieldName : "LOWER("+ fieldName +")" )
				.append( Condition.CONDTYPE_EQUALS.equals(conditionType) ? " IN (" : " NOT IN (" );
			for( int v = 0; v < conditionValues.length; v++ )
				sbuf.append( " ?," );
			sbuf.setCharAt( sbuf.length() - 1, ')' );
		} else {
			sbuf.append( caseSensitive ? fieldName : "LOWER("+ fieldName +")" ).append( operation );
			if( conditionValues.length > 1 ) {
				String conditionQuery = ( Condition.CONDTYPE_NOTEQUALS.equals(conditionType) || Condition.CONDTYPE_NOT_STARTSWITH.equals(conditionType) ? " AND " : " OR " );
				for( int v = 1; v < conditionValues.length; v++ )
					sbuf.append( conditionQuery ).append( caseSensitive ? fieldName : "LOWER("+ fieldName +")" ).append( operation );
				sbuf.insert( 0, '(' ).append( ')' );
			}
		}
		addBindVariables( COND_BINDVAR, conditionValues );

		return appendCondition( sbuf.toString() );
	}

	public QueryBuffer appendConditionByFieldRange( String fieldName, String conditionValue ) {
		return appendConditionByFieldRange( fieldName, conditionValue, false );
	}

	public QueryBuffer appendConditionByFieldRange( String fieldName, String conditionValue, boolean isNumberType ) {
		int conditionCount = 0;
		StringBuffer sbuf = new StringBuffer();
		List<Object> bindVarList = new java.util.ArrayList<Object>();
		String[] values = conditionValue.split( "," );
		for( String value : values ) {
			String[] splitValues, inValues, outValues;

			splitValues = value.split( "/" );
			inValues = splitValues[0].split( "-" );
			outValues = ( splitValues.length > 1 ? splitValues[1].split("-") : null );
			if( splitValues.length > 2 || inValues.length > 2 || (outValues != null && outValues.length > 2) ) {
				if( !isNumberType ) {
					conditionCount++;
					sbuf.append( " OR "+ fieldName +" = ?" );
					bindVarList.add( value );
				}
				continue;
			}

			int bindVarCount = 0;
			Object bindVars[] = new Object[ outValues == null ? inValues.length : inValues.length + outValues.length ];
			if( isNumberType ) {
				try {
					bindVars[bindVarCount++] = Double.valueOf( inValues[0].trim() );
					if( inValues.length > 1 ) bindVars[bindVarCount++] = Double.valueOf( inValues[1].trim() );
					if( outValues != null ) {
						bindVars[bindVarCount++] = Double.valueOf( outValues[0].trim() );
						if( outValues.length > 1 ) bindVars[bindVarCount++] = Double.valueOf( outValues[1].trim() );
					}
				} catch( NumberFormatException numEx ) {
					continue;
				}
			} else {
				bindVars[bindVarCount++] = inValues[0].trim();
				if( inValues.length > 1 ) bindVars[bindVarCount++] = inValues[1].trim();
				if( outValues != null ) {
					bindVars[bindVarCount++] = outValues[0].trim();
					if( outValues.length > 1 ) bindVars[bindVarCount++] = outValues[1].trim();
				}
			}
			for( int i = 0; i < bindVars.length; i++ )
				bindVarList.add( bindVars[i] );

			if( inValues.length == 1 )
				sbuf.append( " OR ("+ fieldName +" = ?" );
			else
				sbuf.append( " OR ("+ fieldName +" BETWEEN ? AND ?" );
			if( outValues != null ) {
				if( outValues.length == 1 )
					sbuf.append( " AND "+ fieldName +" <> ?" );
				else
					sbuf.append( " AND NOT ("+ fieldName +" BETWEEN ? AND ?)" );
			}
			sbuf.append( ")" );
			conditionCount++;
		}

		if( conditionCount == 0 )
			return appendCondition( fieldName +" = ?", (Object)null );
		else {
			addBindVariables( COND_BINDVAR, bindVarList );
			return appendCondition( "("+ sbuf.substring(4) +")" );
		}
	}

	/**
	 * CONNECT BY절에 내용 추가.
	 */
	public QueryBuffer appendConnectBy( String startWithQuery, String connectByQuery ) {
		if( startWithQuery == null )
			connectbuf = new StringBuffer( "CONNECT BY "+ connectByQuery );
		else
			connectbuf = new StringBuffer( "START WITH "+ startWithQuery +" CONNECT BY "+ connectByQuery );
		return this;
	}

	/**
	 * CONNECT BY절에 내용 및 bindVariable 추가.
	 */
	public QueryBuffer appendConnectBy( String startWithQuery, String connectByQuery, Object... bindVars ) {
		addBindVariables( CONNECT_BINDVAR, bindVars );
		return appendConnectBy( startWithQuery, connectByQuery );
	}

	/**
	 * SELECT절에 내용 추가.
	 * <p>
	 * 예제) append( "ITM.ITEMNAME \"itemName\"" );
	 */
	public QueryBuffer appendData( String query ) {
		try {
			databuf.append( ", " ).append( query );
		} catch( NullPointerException nullEx ) {
			if( databuf != null ) throw nullEx;
			databuf = new StringBuffer( query );
		}
		return this;
	}

	/**
	 * SELECT절에 subquery로 내용 추가.
	 * <p>
	 * 예제) appendData( querybuf, "itemName" );
	 * @param querybuf	subquery의 내용이 포함되어 있는 QueryBuffer
	 */
	public boolean appendData( QueryBuffer querybuf, String alias ) {
		if( dataAliasMap.put(alias, querybuf) == null ) {
			appendData( "( "+ querybuf.getQuery() +" ) \""+ alias +"\"" );
			if( querybuf.getBindVariableCount() > 0 ) addBindVariables( DATA_BINDVAR, querybuf.getBindVariables() );
			return true;
		}
		return false;
	}

	/**
	 * SELECT절에 내용 추가.
	 * <p>
	 * 예제) appendDataWithAlias( "ITM.ITEM_NAME", "itemName" );
	 */
	public boolean appendDataWithAlias( String query, String alias ) {
		if( dataAliasMap.put(alias, query) == null ) {
			appendData( query +" \""+ alias +"\"" );
			return true;
		}
		return false;
	}

	/**
	 * SELECT절, GROUP BY절에 동시에 내용 추가.
	 */
	public QueryBuffer appendDataWithGroupBy( String query ) {
		return appendData(query).appendGroupBy(query);
	}

	/**
	 * SELECT절, GROUP BY절에 동시에 내용 추가.
	 */
	public boolean appendDataWithGroupBy( String query, String alias ) {
		if( appendDataWithAlias(query, alias) ) {
			appendGroupBy( query );
			return true;
		}
		return false;
	}

	/**
	 * SELECT절에 DISTINCT 추가.
	 */
	public QueryBuffer appendDistinct() {
		useDistinct = true;
		return this;
	}

	/**
	 * GROUP BY절에 내용 추가.
	 */
	public QueryBuffer appendGroupBy( String query ) {
		try {
			groupbuf.append( ", " ).append( query );
		} catch( NullPointerException nullEx ) {
			if( groupbuf != null ) throw nullEx;
			groupbuf = new StringBuffer( query );
		}
		return this;
	}

	/**
	 * HAVING절에 내용 추가.
	 */
	public QueryBuffer appendHaving( String query ) {
		try {
			havingbuf.append( " AND " ).append( query );
		} catch( NullPointerException nullEx ) {
			if( havingbuf != null ) throw nullEx;
			havingbuf = new StringBuffer( query );
		}
		return this;
	}

	/**
	 * HINT절에 내용 추가.
	 */
	public QueryBuffer appendHint( String query ) {
		try {
			hintbuf.append( " " ).append( query );
		} catch( NullPointerException nullEx ) {
			if( hintbuf != null ) throw nullEx;
			hintbuf = new StringBuffer( query );
		}
		return this;
	}

	/**
	 * QUERY에 FOR UPDATE 추가.
	 */
	public QueryBuffer appendLock( boolean waiting ) {
		useLock = true;
		useLockWaiting = waiting;

		return this;
	}

	/**
	 * ORDER BY절에 내용 추가.
	 */
	public QueryBuffer appendOrderBy( String query ) {
		try {
			orderbuf.append( ", " ).append( query );
		} catch( NullPointerException nullEx ) {
			if( orderbuf != null ) throw nullEx;
			orderbuf = new StringBuffer( query );
		}
		return this;
	}

	/**
	 * ORDER BY절에 fieldName을 이용하여 내용 추가.
	 * @return 추가되었는지 여부
	 * @see #appendOrderByFieldName(String, String)
	 */
	public boolean appendOrderByFieldName( String fieldName ) {
		return appendOrderByFieldName( fieldName, null );
	}

	/**
	 * ORDER BY절에 fieldName을 이용하여 내용 추가.
	 * {@link #existData(String) existData}(fieldName)가 true일 때 추가.
	 * <p>
	 * appendOrderBy( "\""+ fieldName +"\""+ (sortWay != null ? " "+ sortWay : "") )
	 * @return 추가되었는지 여부
	 */
	public boolean appendOrderByFieldName( String fieldName, String sortWay ) {
		if( existData(fieldName) ) {
			appendOrderBy( "\""+ fieldName +"\""+ (sortWay != null ? " "+ sortWay : "") );
			return true;
		}

		return false;
	}

	/**
	 * FROM절에 내용 추가.
	 * <p>
	 * 테이블 추가시 {@link #appendTable}보다, {@link #appendTableWithAlias(String, String, String) appendTableWithAlias}쓰기를 권고
	 * @see #appendTable(String, String)
	 */
	public QueryBuffer appendTable( String tableQuery ) {
		try {
			frombuf.append( ", " ).append( tableQuery );
		} catch( NullPointerException nullEx ) {
			if( frombuf != null ) throw nullEx;
			frombuf = new StringBuffer( tableQuery );
		}
		return this;
	}

	/**
	 * FROM절, WHERE절에 내용 추가.
	 * <p>
	 * 테이블 추가시 {@link #appendTable}보다, {@link #appendTableWithAlias(String, String, String) appendTableWithAlias}쓰기를 권고
	 * @return 추가되었는지 여부
	 */
	public QueryBuffer appendTable( String tableQuery, String conditionQuery ) {
		if( conditionQuery != null ) appendCondition( conditionQuery );
		return appendTable( tableQuery );
	}

	/**
	 * FROM절에 내용 추가(이미 있는 alias일 경우 추가하지 않음).
	 * {@link QueryBuffer}를 저장
	 * @param querybuf	subquery의 내용이 포함되어 있는 QueryBuffer
	 * @return 추가되었는지 여부
	 * @see #getInnerQueryBuffer(String)
	 */
	public boolean appendTable( InnerQueryBuffer querybuf, String alias ) {
		if( tableAliasMap.containsKey(alias) ) return false;
		try {
			innerQueryBufferList.add( new Object[] { alias, querybuf } );
		} catch( NullPointerException nullEx ) {
			if( innerQueryBufferList != null ) throw nullEx;
			innerQueryBufferList = new java.util.ArrayList<Object[]>();
			innerQueryBufferList.add( new Object[] { alias, querybuf } );
		}
		tableAliasMap.put( alias, querybuf );
		return true;
	}

	/**
	 * FROM절에 내용 추가(이미 있는 alias일 경우 추가하지 않음).
	 * {@link QueryBuffer}를 저장
	 * 테이블이 추가될 경우에만 WHERE절을 추가
	 * @param querybuf	subquery의 내용이 포함되어 있는 QueryBuffer
	 * @return 추가되었는지 여부
	 * @see #getInnerQueryBuffer(String)
	 */
	public boolean appendTable( InnerQueryBuffer querybuf, String alias, String conditionQuery ) {
		if( appendTable( querybuf, alias ) ) {
			if( conditionQuery != null ) appendCondition( conditionQuery );
			return true;
		} else
			return false;
	}

	/**
	 * FROM절에 내용 추가(이미 있는 alias일 경우 추가하지 않음).
	 * @return 추가되었는지 여부
	 */
	public boolean appendTableWithAlias( String tableQuery, String alias ) {
		if( tableAliasMap.containsKey(alias) ) return false;
		try {
			frombuf.append( ", " ).append( tableQuery + " " + alias );
		} catch( NullPointerException nullEx ) {
			if( frombuf != null ) throw nullEx;
			frombuf = new StringBuffer( tableQuery + " " + alias );
		}
		tableAliasMap.put( alias, tableQuery );
		return true;
	}

	/**
	 * FROM절에 내용 추가(이미 있는 alias일 경우 추가하지 않음).
	 * 테이블이 추가될 경우에만 WHERE절을 추가
	 * @return 추가되었는지 여부
	 */
	public boolean appendTableWithAlias( String tableQuery, String alias, String conditionQuery ) {
		if( appendTableWithAlias( tableQuery, alias ) ) {
			if( conditionQuery != null ) appendCondition( conditionQuery );
			return true;
		} else
			return false;
	}

	/**
	 * WITH절에 내용 추가.
	 */
	public QueryBuffer appendWith( String query, String alias ) {
		try {
			withbuf.append( ", ").append( alias +" AS ("+ query +")" );
		} catch( NullPointerException nullEx ) {
			if( withbuf != null ) throw nullEx;
			withbuf = new StringBuffer( alias +" AS ("+ query +")" );
		}

		return this;
	}

	/**
	 * WITH절에 내용 추가.
	 */
	public QueryBuffer appendWith( QueryBuffer querybuf, String alias ) {
		try {
			withbuf.append( ", ").append( alias +" AS ("+ querybuf.getQuery() +")" );
		} catch( NullPointerException nullEx ) {
			if( withbuf != null ) throw nullEx;
			withbuf = new StringBuffer( alias +" AS ("+ querybuf.getQuery() +")" );
		}
		if( querybuf.getBindVariableCount() > 0 ) addBindVariables( WITH_BINDVAR, querybuf.getBindVariables() );

		return this;
	}

	/**
	 * Query내용을 모두 지움.
	 */
	public void clear() {
		this.bindVars = new List[] { null, null, null, new java.util.ArrayList<Object>(), null, null };
		this.useDistinct = false;
		this.useLock = false;
		this.useLockWaiting = false;
		this.savedQuery = null;
		this.withbuf = null;
		this.hintbuf = null;
		this.databuf = null;
		this.frombuf = null;
		this.condbuf = null;
		this.connectbuf = null;
		this.groupbuf = null;
		this.havingbuf = null;
		this.orderbuf = null;
		this.dataAliasMap = new java.util.HashMap<String, Object>();
		this.tableAliasMap = new java.util.HashMap<String, Object>();
		this.innerQueryBufferList = null;
	}

	/**
	 * WHERE절의 내용을 지움.
	 */
	public void clearCondition() {
		this.condbuf = null;
		this.bindVars[COND_BINDVAR] = new java.util.ArrayList<Object>();
	}

	/**
	 * CONNECT BY절의 내용을 지움.
	 */
	public void clearConnectBy() {
		this.connectbuf = null;
		this.bindVars[CONNECT_BINDVAR] = null;
	}

	/**
	 * DATA절 및 저장된Query의 내용을 지움.
	 */
	public void clearData() {
		this.savedQuery = null;
		this.databuf = null;
		this.bindVars[DATA_BINDVAR] = null;
	}

	/**
	 * GROUP BY절의 내용을 지움.
	 */
	public void clearGroupBy() {
		this.groupbuf = null;
	}

	/**
	 * HAVING절의 내용을 지움.
	 */
	public void clearHaving() {
		this.havingbuf = null;
	}

	/**
	 * ORDER BY절의 내용을 지움.
	 */
	public void clearOrderBy() {
		this.orderbuf = null;
	}

	/**
	 * 이미 SELECT절에 추가된 내용인지를 검사.
	 */
	public boolean existData( String fieldName ) {
		if( existDataAlias(fieldName) ) return true;
		try {
			int idx = databuf.indexOf( fieldName, 0 );
			while( idx >= 0 ) {
				if( idx == 0 || databuf.charAt(idx-1) == ' ' ) {
					idx += fieldName.length();
					if( idx == databuf.length() || databuf.charAt(idx) == ',' || databuf.charAt(idx) == '"' )
						return true;
				} else if( databuf.charAt(idx-1) == '"' ) {
					idx += fieldName.length();
					if( idx < databuf.length() && databuf.charAt(idx) == '"' )
						return true;
				}
				idx = databuf.indexOf( fieldName, ++idx );
			}
		} catch( NullPointerException nullEx ) {
			if( databuf != null ) throw nullEx;
		}
		return false;
	}

	/**
	 * 이미 SELECT절에 추가된 alias인지를 검사.
	 */
	public boolean existDataAlias( String alias ) {
		return dataAliasMap.containsKey( alias );
	}

	/**
	 * 이미 FROM절에 추가된 alias인지를 검사.
	 */
	public boolean existTableAlias( String alias ) {
		return tableAliasMap.containsKey( alias );
	}

	/**
	 * bindVariable개수 return
	 */
	public int getBindVariableCount() {
		int count = 0;
		if( bindVars[WITH_BINDVAR] != null ) count += bindVars[WITH_BINDVAR].size();
		if( bindVars[DATA_BINDVAR] != null ) count += bindVars[DATA_BINDVAR].size();
		if( bindVars[FROM_BINDVAR] != null ) count += bindVars[FROM_BINDVAR].size();
		if( innerQueryBufferList != null ) {
			for( Object[] objs : innerQueryBufferList )
				count += ((InnerQueryBuffer)objs[1]).getBindVariableCount();
		}
		if( bindVars[COND_BINDVAR] != null ) count += bindVars[COND_BINDVAR].size();
		if( bindVars[CONNECT_BINDVAR] != null ) count += bindVars[CONNECT_BINDVAR].size();
		if( bindVars[HAVING_BINDVAR] != null ) count += bindVars[HAVING_BINDVAR].size();

		return count;
	}

	/**
	 * bindVariableList return
	 */
	public List<Object> getBindVariableList() {
		return getBindVariableList( new java.util.ArrayList<Object>() );
	}

	private List<Object> getBindVariableList( List<Object> list ) {
		if( bindVars[WITH_BINDVAR] != null ) list.addAll( bindVars[WITH_BINDVAR] );
		if( bindVars[DATA_BINDVAR] != null ) list.addAll( bindVars[DATA_BINDVAR] );
		if( bindVars[FROM_BINDVAR] != null ) list.addAll( bindVars[FROM_BINDVAR] );
		if( innerQueryBufferList != null ) {
			for( Object[] objs : innerQueryBufferList )
				if( ((InnerQueryBuffer)objs[1]).getBindVariableList() != null )
					list.addAll( ((InnerQueryBuffer)objs[1]).getBindVariableList() );
		}
		if( bindVars[COND_BINDVAR] != null ) list.addAll( bindVars[COND_BINDVAR] );
		if( connectbuf != null && bindVars[CONNECT_BINDVAR] != null ) list.addAll( bindVars[CONNECT_BINDVAR] );
		if( groupbuf != null && bindVars[HAVING_BINDVAR] != null ) list.addAll( bindVars[HAVING_BINDVAR] );

		return list;
	}

	/**
	 * bindVariable[] return
	 */
	public Object[] getBindVariables() {
		List list = getBindVariableList();
		return ( list.size() == 0 ? null : list.toArray() );
	}

	public QueryBuffer getBlankQueryBuffer() {
		return new QueryBuffer();
	}

	public Object[] getConditionBindVariables() {
		List list = bindVars[COND_BINDVAR];
		return list.size() == 0 ? null : list.toArray();
	}

	/**
	 * WHERE절을 return(WHERE 포함)
	 */
	public String getConditionQuery() {
		return( condbuf == null ? "" : " WHERE "+ condbuf.toString() );
	}

	/**
	 * CONNECT BY절을 return
	 */
	public String getConnectByQuery() {
		return( connectbuf == null ? "" : connectbuf.toString() );
	}

	public String[] getDataAliases() {
		return dataAliasMap.keySet().toArray( new String[dataAliasMap.size()] );
	}

	/**
	 * SELECT절을 return(SELECT 미포함)
	 */
	public String getDataQuery() {
		return ( databuf == null ? "" : databuf.toString() );
	}

	/**
	 * GROUP BY절을 return(GROUP BY 미포함)
	 */
	public String getGroupByQuery() {
		return ( groupbuf == null ? "" : groupbuf.toString() );
	}

	/**
	 * ORDER BY절을 return(ORDER BY 미포함)
	 */
	public String getOrderByQuery() {
		return ( orderbuf == null ? "" : orderbuf.toString() );
	}

	/**
	 * 저장된 {@link QueryBuffer}를 return
	 * @see #appendTable(InnerQueryBuffer, String)
	 */
	public InnerQueryBuffer getInnerQueryBuffer( String tableKey ) {
		Object obj = tableAliasMap.get(tableKey);
		if( obj instanceof InnerQueryBuffer )
			return (InnerQueryBuffer)obj;
		else
			return null;
	}

	public String getQuery() {
		if( databuf == null || (frombuf == null && innerQueryBufferList == null) ) return null;
		StringBuffer query = new StringBuffer();

		if( savedQuery != null ) query.append( savedQuery );
		if( withbuf != null )
			query.append( "WITH " ).append( withbuf ).append( " " );
		query.append( "SELECT " );
		if( hintbuf != null )
			query.append( " /*+ " ).append( hintbuf ).append( " */ " );
		if( useDistinct ) query.append( DISTINCT_QUERY );
		query.append( databuf );

		query.append( " FROM " );
		if( frombuf != null )
			query.append( frombuf );

		if( innerQueryBufferList != null ) {
			StringBuffer sbuf = ( frombuf != null ? query : new StringBuffer() );
			for( Object[] objs : innerQueryBufferList )
				sbuf.append(", (").append(((InnerQueryBuffer)objs[1]).getQuery()).append(") ").append((String)objs[0]);
			if( sbuf != query ) query.append( sbuf.substring(1) );
		}

		if( condbuf != null )
			query.append( " WHERE " ).append( condbuf );
		if( connectbuf != null )
			query.append( connectbuf );
		if( groupbuf != null ) {
			query.append( " GROUP BY " ).append( groupbuf );
			if( havingbuf != null )
				query.append( " HAVING " ).append( havingbuf );
		}
		if( orderbuf != null )
			query.append( " ORDER BY " ).append( orderbuf );
		if( useLock ) query.append( useLockWaiting ? " FOR UPDATE" : " FOR UPDATE NOWAIT" );

		return query.toString();
	}

	public int getTableAliasCount() {
		return tableAliasMap.size();
	}

	public boolean hasConditionQuery() {
		return ( condbuf != null );
	}

	public boolean hasDataQuery() {
		return ( databuf != null );
	}

	public boolean hasSavedQuery() {
		return ( savedQuery != null );
	}

	public boolean replaceTable( String tableQuery, String alias ) {
		Object obj = tableAliasMap.get( alias );
		if( obj instanceof String ) {
			String query = obj +" "+ alias;
			int index = 0;
			do {
				index = frombuf.indexOf( query, index );
				if( index < 0 ) return false;

				if( index == 0 || (frombuf.charAt(index - 1) == ' ' && frombuf.charAt(index - 2) == ',') ) {
					if( frombuf.length() == index + query.length() || frombuf.charAt(index + query.length()) == ',' ) {
						frombuf.replace( index, index + query.length(), tableQuery +" "+ alias );
						tableAliasMap.put( alias, tableQuery );
						return true;
					}
				}
				index++;
			} while( true );
		}

		return false;
	}

	public String toString() {
		return getQuery();
	}

	/**
	 * Query를 UNION
	 * @see #union(QueryBuffer, String)
	 */
	public QueryBuffer union() {
		return union( (QueryBuffer)null );
	}

	/**
	 * Query를 UNION
	 * @see #union(QueryBuffer, String)
	 */
	public QueryBuffer union( String unionType ) {
		return union( null, unionType );
	}

	/**
	 * Query를 UNION
	 * @see #union(QueryBuffer, String)
	 */
	public QueryBuffer union( QueryBuffer querybuf ) {
		return union( querybuf, " UNION " );
	}

	/**
	 * Query를 UNION
	 * @param querybuf		unionType뒤에 붙는 QueryBuffer
	 * @param unionType		"UNION", "UNION ALL", "MINUS" 등
	 * @see #union(QueryBuffer, String)
	 */
	public QueryBuffer union( QueryBuffer querybuf, String unionType ) {
		this.savedQuery = getQuery() + unionType;

		if( querybuf == null ) {
			this.bindVars[WITH_BINDVAR] = getBindVariableList();
			this.bindVars[DATA_BINDVAR] = null;
			this.bindVars[FROM_BINDVAR] = null;
			this.bindVars[COND_BINDVAR] = new java.util.ArrayList<Object>();
			this.bindVars[CONNECT_BINDVAR] = null;
			this.bindVars[HAVING_BINDVAR] = null;

			this.useDistinct = false;
			this.useLock = false;
			this.useLockWaiting = false;
			this.withbuf = this.hintbuf = this.databuf = this.frombuf = this.condbuf = this.connectbuf = this.groupbuf = this.havingbuf = null;
			this.dataAliasMap = new java.util.HashMap<String, Object>();
			this.tableAliasMap = new java.util.HashMap<String, Object>();
			this.innerQueryBufferList = null;
		} else {
			if( querybuf.savedQuery != null )
				this.savedQuery += querybuf.savedQuery;
			this.bindVars[WITH_BINDVAR] = getBindVariableList();
			if( querybuf.bindVars[WITH_BINDVAR] != null )
				addBindVariables( WITH_BINDVAR, querybuf.bindVars[WITH_BINDVAR] );
			this.bindVars[DATA_BINDVAR] = querybuf.bindVars[DATA_BINDVAR];
			this.bindVars[FROM_BINDVAR] = querybuf.bindVars[FROM_BINDVAR];
			this.bindVars[COND_BINDVAR] = querybuf.bindVars[COND_BINDVAR];
			this.bindVars[CONNECT_BINDVAR] = querybuf.bindVars[CONNECT_BINDVAR];
			this.bindVars[HAVING_BINDVAR] = querybuf.bindVars[HAVING_BINDVAR];

			this.useDistinct = querybuf.useDistinct;
			this.useLock = querybuf.useLock;
			this.useLockWaiting = querybuf.useLockWaiting;
			this.withbuf = querybuf.withbuf;
			this.hintbuf = querybuf.hintbuf;
			this.databuf = querybuf.databuf;
			this.frombuf = querybuf.frombuf;
			this.condbuf = querybuf.condbuf;
			this.connectbuf = querybuf.connectbuf;
			this.groupbuf = querybuf.groupbuf;
			this.havingbuf = querybuf.havingbuf;
			this.dataAliasMap = querybuf.dataAliasMap;
			this.tableAliasMap = querybuf.tableAliasMap;
			this.innerQueryBufferList = querybuf.innerQueryBufferList;
		}

		return this;
	}

	/**
	 * Query를 UNION ALL
	 * @see #union(QueryBuffer, String)
	 */
	public QueryBuffer unionAll() {
		return unionAll( null );
	}

	/**
	 * Query를 UNION ALL
	 * @see #union(QueryBuffer, String)
	 */
	public QueryBuffer unionAll( QueryBuffer querybuf ) {
		return union( querybuf, " UNION ALL " );
	}

	public boolean usingDistinct() {
		return useDistinct;
	}

	public boolean usingLock() {
		return useLock;
	}
}
