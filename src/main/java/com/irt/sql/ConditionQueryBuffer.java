/*
 *	File Name:	ConditionQueryBuffer.java
 *	Version:	2.2.9
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2018/03/30		2.2.9	findConditionSimple(): isOuterJoin 파리미터 추가
 *	GimHS		2014/12/31		2.2.8	findCondition(): Condition.CONDTYPE_REGEX 지원
 *	stghr12		2011/02/28		2.2.7	appendConditionByField(): CONDTYPE_NOT_STARTSWITH 오류수정(OR -> AND)
 *	mir0033		2010/12/31		2.2.6	findConditionCode(): String[]에서 각 값별로 '|'를 사용하도록 수정
 *	stghr12		2010/09/30		2.2.5	findCondition(): caseSensitive 추가
 *	yjcha		2010/04/16		2.2.4	findCondition(), findConditionName(): NOT LIKE 조건 처리 추가
 *	stghr12		2009/04/30		2.2.3	findCondition(), findConditionCode(): IS_NULL이나 IS_NOT_NULL 처리 시 1 대신 10 return
 *										findConditionDate(): IS_NULL_OR이 MIN/MAX에서도 동작하도록 수정
 *	stghr12		2009/03/31		2.2.2	findConditionKey() 오류 수정
 *	stghr12		2008/03/31		2.2.1	findConditionKey() 오류 수정
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										convertDate(): com.irt.data.Date 사용
 *										AbstractField.TYPE_DATETIME 적용
 *	stghr12		2007/10/31		2.1.2	IllegalArgumentException message 변경: invalid -> illegal
 *	stghr12		2007/04/30		2.1.1	getConditionValue( key, dataType ): 로직 변경
 *	stghr12		2006/12/01		2.1.0	isConditionTrue(): Condition.isConditionTrue()사용으로 변경
 *										findConditionKey(), findConditionRange(), findConditionSimple( key, fieldName, dataType ) 추가
 *										findCondition(), findConditionDate(), findConditionNumber(): findConditionRange() 호출 추가
 *										findConditionDate(), findConditionNumber(): Condition.SUFFIX_IS_NULL_OR 지원
 *										getConditionValue( key, dataType ) 추가
 *										makeDateQuery() 제거, convertDate() 추가
 *	stghr12		2006/09/01		2.0.2	DateFormat 동기화 오류 수정
 *	stghr12		2006/07/07		2.0.1	default값 변경: CONDTYPE_EQUALS_NONE -> CONDTYPE_EQUALS_MINMAX
 *	stghr12		2006/02/28		2.0.0	create(QueryBuffer에서 분리)
 *
**/

package com.irt.sql;

import com.irt.data.AbstractField;
import com.irt.data.Condition;
import com.irt.data.Record;
import java.util.*;

/**
 * Query를 저장하는 BufferedString.
 * <p>
 * SELECT절, FROM절, WHERE절, GROUP BY절, ORDER BY절, HINT절을 각각 다른 StringBuffer에 저장.
 * conditionMap과 bindVariable를 관리.
 */
public class ConditionQueryBuffer extends QueryBuffer {
	Map<String, ? extends Object> conditionMap;

	public ConditionQueryBuffer( Map<String, ? extends Object> conditionMap ) {
		super();
		this.conditionMap = conditionMap;
	}

	public boolean appendConditionIf( String key, String conditionQuery ) {
		if( Condition.isConditionTrue(conditionMap, key) ) {
			appendCondition( conditionQuery );
			return true;
		} else
			return false;
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * @see #findCondition(String, String[], String, boolean)
	 */
	public int findCondition( String key, String fieldName ) {
		return findCondition( key, new String[] { fieldName }, null, true );
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * @see #findCondition(String, String[], String, boolean)
	 */
	public int findCondition( String key, String fieldName, boolean caseSensitive ) {
		return findCondition( key, new String[] { fieldName }, null, caseSensitive );
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * @see #findCondition(String, String[], String, boolean)
	 */
	public int findCondition( String key, String fieldName, String defaultType ) {
		return findCondition( key, new String[] { fieldName }, defaultType, true );
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * @see #findCondition(String, String[], String, boolean)
	 */
	public int findCondition( String key, String fieldName, String defaultType, boolean caseSensitive ) {
		return findCondition( key, new String[] { fieldName }, defaultType, caseSensitive );
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * @see #findCondition(String, String[], String, boolean)
	 */
	public int findCondition( String key, String[] fieldNames ) {
		return findCondition( key, fieldNames, null, true );
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * @see #findCondition(String, String[], String, boolean)
	 */
	public int findCondition( String key, String[] fieldNames, boolean caseSensitive ) {
		return findCondition( key, fieldNames, null, caseSensitive );
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * @see #findCondition(String, String[], String, boolean)
	 */
	public int findCondition( String key, String[] fieldNames, String defaultType ) {
		return findCondition( key, fieldNames, defaultType, true );
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * <p>
	 * 다음 순서로 처리:
	 * <ul type='square'>
	 * <li>conditionMap에 (key + "_isNull")이 포함된 경우: appendCondition(fieldNames[0] +" IS NULL")
	 * <li>conditionMap에 (key + "_isNotNull")이 포함된 경우: appendCondition(fieldNames[0] +" IS NOT NULL")
	 * <li>이외의 경우, conditionMap에 (key +"_type")에 따라 다음과 같이 처리:
	 * <ul>
	 * <li>{@link Condition#CONDTYPE_EQUALS}: (fieldName +" = ?")
	 * <li>{@link Condition#CONDTYPE_NOTEQUALS}: (fieldName +" <> ?")
	 * <li>{@link Condition#CONDTYPE_STARTSWITH}: (fieldName +" LIKE ? || '%'")
	 * <li>{@link Condition#CONDTYPE_CONTAINS}: (fieldName +" LIKE '%' || ? || '%'")
	 * <li>{@link Condition#CONDTYPE_LIKE}: (fieldName +" LIKE ?")
	 * <li>{@link Condition#CONDTYPE_NOT_STARTSWITH}: (fieldName +" NOT LIKE ? || '%'")
	 * <li>{@link Condition#CONDTYPE_RANGE}: ',', '-', '/' 사용하는 범위조건
	 * <li>{@link Condition#CONDTYPE_REGEX}: 정규표현식을 사용하는 조건
	 * <li>conditionMap에 (key +"_isNullOr")이 포함된 경우: (" OR "+ fieldName +" IS NULL") 추가
	 * </ul>
	 * </ul>
	 * @param fieldNames	값이 여러개일 경우에는, OR로 처리( isNull, isNotNull, isNullOr은 값이 하나일 경우에만 처리 )
	 * @param defaultType	null일 경우 {@link Condition#CONDTYPE_EQUALS}로 처리
	 * @return 추가된 조건 개수
	 */
	public int findCondition( String key, String[] fieldNames, String defaultType, boolean caseSensitive ) {
		if( conditionMap == null ) return 0;

		// (key + SUFFIX_IS_NULL)이 "Y" 또는 "true"인 경우 처리
		// (key + SUFFIX_IS_NOTNULL)이 "Y" 또는 "true"인 경우 처리
		if( fieldNames.length == 1 ) {
			if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NULL) ) {
				appendCondition( fieldNames[0] +" IS NULL" );
				return 10;
			} else if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NOTNULL) ) {
				appendCondition( fieldNames[0] +" IS NOT NULL" );
				return 10;
			}
		}

		// valueObjs, type 설정
		Object[] valueObjs = getConditionValues( key );
		if( valueObjs == null ) return 0;

		// type, operation 정의
		String operation = ( caseSensitive ? "?" : "LOWER(?)" );
		Object valueObj = conditionMap.get( key + Condition.SUFFIX_TYPE );
		String type = ( valueObj instanceof String ? (String)valueObj : defaultType );
		if( type == null || Condition.CONDTYPE_EQUALS.equals(type) ) {
			type = Condition.CONDTYPE_EQUALS;
			operation = " = "+ operation;
		} else if( Condition.CONDTYPE_NOTEQUALS.equals(type) )
			operation = " <> "+ operation;
		else if( Condition.CONDTYPE_STARTSWITH.equals(type) )
			operation = " LIKE "+ operation +" || '%'";
		else if( Condition.CONDTYPE_CONTAINS.equals(type) )
			operation = " LIKE '%' || "+ operation +" || '%'";
		else if( Condition.CONDTYPE_LIKE.equals(type) )
			operation = " LIKE "+ operation;
		else if( Condition.CONDTYPE_NOT_STARTSWITH.equals(type) )
			operation = " NOT LIKE "+ operation +" || '%'";
		else if( Condition.CONDTYPE_RANGE.equals(type) )
			return findConditionRange( key, fieldNames[0], false );
		else if( Condition.CONDTYPE_REGEX.equals(type) ) {
			bindVars[COND_BINDVAR].add( valueObjs[0] );
			appendCondition( "REGEXP_LIKE(LOWER("+ fieldNames[0] +"), LOWER(?))" );
			return 1;
		} else {
			type = Condition.CONDTYPE_EQUALS;
			operation = " = "+ operation;
		}

		// 조건 설정
		int conditionCount = 0;
		boolean hasNullOrCondition = false;
		StringBuffer sbuf = new StringBuffer();
		if( fieldNames.length == 1 && valueObjs.length > 1
				&& (Condition.CONDTYPE_EQUALS.equals(type) || Condition.CONDTYPE_NOTEQUALS.equals(type)) ) {
			conditionCount++;
			sbuf.append( caseSensitive ? fieldNames[0] : "LOWER("+ fieldNames[0] +")" )
				.append( Condition.CONDTYPE_EQUALS.equals(type) ? " IN (" : " NOT IN (" );
			for( Object value : valueObjs ) {
				sbuf.append( caseSensitive ? " ?," : " LOWER(?)," );
				bindVars[COND_BINDVAR].add( value );
			}
			sbuf.setCharAt( sbuf.length() - 1, ')' );
		} else {
			String conditionQuery = ( Condition.CONDTYPE_NOTEQUALS.equals(type) || Condition.CONDTYPE_NOT_STARTSWITH.equals(type) ? " AND " : " OR " );
			for( Object value : valueObjs ) {
				for( String fieldName : fieldNames ) {
					conditionCount++;
					sbuf.append( conditionQuery ).append( caseSensitive ? fieldName : "LOWER("+ fieldName +")" ).append( operation );
					bindVars[COND_BINDVAR].add( value );
				}
			}
			sbuf.delete( 0, 4 );
		}

		// (key + SUFFIX_IS_NULL_OR)이 "Y" 또는 "true"인 경우 처리
		if( fieldNames.length == 1 && Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NULL_OR) ) {
			conditionCount++;
			hasNullOrCondition = true;
			sbuf.append( " OR "+ fieldNames[0] +" IS NULL" );
		}

		// buffer에 조건 추가
		if( conditionCount > 1 ) sbuf.insert( 0, '(' ).append( ')' );
		appendCondition( sbuf.toString() );

		return valueObjs.length + ( hasNullOrCondition ? 10 : 0 );
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가. (A, B) IN ( (?, ?), (?, ?) ) 방식.
	 * @return 추가된 조건 개수
	 */
	public int findCondition( String[] keys, String[] fieldNames ) {
		if( keys.length != fieldNames.length )
			throw new IllegalArgumentException( "illegal parameters." );
		if( conditionMap == null ) return 0;

		// valueCount, valueObjs, sbuf0, sbuf1 설정
		int valueCount = 0;
		StringBuffer sbuf0 = new StringBuffer( "(" );
		StringBuffer sbuf1 = new StringBuffer( "(" );
		Object valueObjs[][] = new Object[keys.length][];
		for( int k = 0; k < keys.length; k++ ) {
			valueObjs[k] = getConditionValues( keys[k] );
			if( valueObjs[k] == null ) return 0;

			if( valueCount == 0 || valueCount > valueObjs[k].length )
				valueCount = valueObjs[k].length;

			sbuf0.append( fieldNames[k] ).append( "," );
			sbuf1.append( " ?," );
		}
		sbuf0.setCharAt( sbuf0.length() - 1, ')' );
		sbuf1.setCharAt( sbuf1.length() - 1, ')' );

		// 조건 추가
		sbuf0.append( " IN (" );
		for( int v = 0; v < valueCount; v++ ) {
			sbuf0.append( sbuf1 ).append( "," );
			for( int k = 0; k < keys.length; k++ )
				bindVars[COND_BINDVAR].add( valueObjs[k][v] );
		}
		sbuf0.setCharAt( sbuf0.length() - 1, ')' );
		appendCondition( sbuf0.toString() );

		return valueCount;
	}

	/**
	 * conditionMap에서 key의 값(코드형)을 찾아서 WHERE절에 추가.
	 * <pre>
	 * (key +"_type"), (key +"_isNull"), (key +"_isNotNull"), (key +"_isNullOr") 사용.
	 * {@link Condition#CONDTYPE_NOTEQUALS}와 {@link Condition#CONDTYPE_EQUALS} 사용.
	 * 단, value가 "ALL"이면 조건을 추가하지 않고, |이 포함된 경우 value[]인 것처럼 처리.
	 * </pre>
	 */
	public int findConditionCode( String key, String fieldName ) {
		if( conditionMap == null ) return 0;

		// (key + SUFFIX_IS_NULL)이 "Y" 또는 "true"인 경우 처리
		// (key + SUFFIX_IS_NOTNULL)이 "Y" 또는 "true"인 경우 처리
		if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NULL) ) {
			appendCondition( fieldName +" IS NULL" );
			return 10;
		} else if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NOTNULL) ) {
			appendCondition( fieldName +" IS NOT NULL" );
			return 10;
		}

		Object[] valueObjs = null;
		try {
			Object valueObj = conditionMap.get( key );
			if( valueObj == null | "ALL".equals(valueObj) ) return 0;
			if( valueObj instanceof String[] ) {
				valueObjs = new String[0];

				for( int v = 0; v < ((String[])valueObj).length; v++ )
					valueObjs = com.irt.util.Arrays.append( (String[])valueObjs, ((String[])valueObj)[v].split("\\|") );
			} else if( valueObj instanceof Object[] )
				valueObjs = (Object[])valueObj;
			else if( valueObj instanceof Collection )
				valueObjs = ((Collection)valueObj).toArray();
			else if( valueObj instanceof String )
				valueObjs = ((String)valueObj).split( "\\|" );
			else
				return 0;
		} catch( NullPointerException nullEx ) {
			return 0;
		}

		Object typeObj = conditionMap.get( key + Condition.SUFFIX_TYPE );

		StringBuffer sbuf = new StringBuffer( fieldName );
		if( valueObjs.length == 1 ) {
			sbuf.append( Condition.CONDTYPE_NOTEQUALS.equals(typeObj) ? " <> ?" : " = ?" );
			bindVars[COND_BINDVAR].add( valueObjs[0] );
		} else {
			sbuf.append( Condition.CONDTYPE_NOTEQUALS.equals(typeObj) ? " NOT IN (" : " IN (" );
			for( Object value : valueObjs ) {
				sbuf.append( " ?," );
				bindVars[COND_BINDVAR].add( value );
			}
			sbuf.setCharAt( sbuf.length() - 1, ')' );
		}

		// (key + SUFFIX_IS_NULL_OR)이 "Y" 또는 "true"인 경우 처리
		if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NULL_OR) ) {
			sbuf.append( " OR "+ fieldName +" IS NULL " );
			sbuf.insert( 0, '(' ).append( ')' );
		}
		appendCondition( sbuf.toString() );

		return valueObjs.length;
	}

	/**
	 * conditionMap에서 key의 값(DATE형식)을 찾아서 WHERE절에 추가.
	 * @see #findConditionDate(String, String, String)
	 */
	public boolean findConditionDate( String key, String fieldName ) {
		return findConditionDate( key, fieldName, null );
	}

	/**
	 * conditionMap에서 key의 값(DATE형식)을 찾아서 WHERE절에 추가.
	 * {@link #findConditionNumber(String, String, String) findConditionNumber}와 같은 방식으로 처리(단, CONDTYPE_RANGE는 사용 안함).
	 */
	public boolean findConditionDate( String key, String fieldName, String defaultType ) {
		if( conditionMap == null ) return false;

		// (key + SUFFIX_IS_NULL)이 "Y" 또는 "true"인 경우 처리
		// (key + SUFFIX_IS_NOTNULL)이 "Y" 또는 "true"인 경우 처리
		if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NULL) ) {
			appendCondition( fieldName +" IS NULL" );
			return true;
		} else if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NOTNULL) ) {
			appendCondition( fieldName +" IS NOT NULL" );
			return true;
		}

		StringBuffer sbuf = new StringBuffer();

		// type 설정
		Object valueObj = conditionMap.get( key + Condition.SUFFIX_TYPE );
		String type = ( valueObj instanceof String ? (String)valueObj : defaultType );

		boolean existCondition = false;
		Object valueObjs[] = getConditionValues( key );
		if( valueObjs != null ) {
			existCondition = true;

			if( valueObjs.length == 1 ) {
				if( Condition.CONDTYPE_NOTEQUALS.equals(type) )
					sbuf.append( fieldName +" <> ?" );
				else
					sbuf.append( fieldName +" = ?" );
				bindVars[COND_BINDVAR].add( convertDateOrTimestamp(valueObjs[0], conditionMap, key) );
			} else {
				for( Object value : valueObjs ) {
					sbuf.append( ", ?" );
					bindVars[COND_BINDVAR].add( convertDateOrTimestamp(value, conditionMap, key) );
				}

				sbuf.setCharAt( 0, '(' );
				sbuf.append( " )" );
				if( Condition.CONDTYPE_NOTEQUALS.equals(type) )
					sbuf.insert( 0, fieldName + " NOT IN " );
				else
					sbuf.insert( 0, fieldName + " IN " );
			}
		}

		// 최소값 조건
		valueObj = conditionMap.get( key + Condition.SUFFIX_MIN_VALUE );
		if( valueObj != null ) {
			existCondition = true;

			bindVars[COND_BINDVAR].add( convertDateOrTimestamp(valueObj, conditionMap, key) );
			if( sbuf.length() > 0 ) sbuf.append( " AND " );
			if( Condition.CONDTYPE_EQUALS_NONE.equals(type) || Condition.CONDTYPE_EQUALS_MAX.equals(type) )
				sbuf.append( fieldName +" > ?" );
			else
				sbuf.append( fieldName +" >= ?" );
		}

		// 최대값 조건
		valueObj = conditionMap.get( key + Condition.SUFFIX_MAX_VALUE );
		if( valueObj != null ) {
			existCondition = true;

			bindVars[COND_BINDVAR].add( convertDateOrTimestamp(valueObj, conditionMap, key) );
			if( sbuf.length() > 0 ) sbuf.append( " AND " );
			if( Condition.CONDTYPE_EQUALS_NONE.equals(type) || Condition.CONDTYPE_EQUALS_MIN.equals(type) )
				sbuf.append( fieldName +" < ?" );
			else
				sbuf.append( fieldName +" <= ?" );
		}

		// (key + SUFFIX_IS_NULL_OR)이 "Y" 또는 "true"인 경우 처리
		if( existCondition ) {
			if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NULL_OR) )
				sbuf.insert( 0, "( (" ).append( ") OR "+ fieldName +" IS NULL )" );
			appendCondition( sbuf.toString() );
		}

		return existCondition;
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * <p>
	 * key의 값은 fieldNames에 해당하는 값이 ';'로 구분되어 들어있다고 가정.
	 */
	public int findConditionKey( String key, String[] fieldNames ) {
		return findConditionKey( key, fieldNames, null );
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * <p>
	 * key의 값은 fieldNames에 해당하는 값이 ';'로 구분되어 들어있다고 가정.
	 */
	public int findConditionKey( String key, String[] fieldNames, char[] dataTypes ) {
		Object valueObjs[] = getConditionValues( key );
		if( valueObjs == null ) return 0;

		List<Object> varList2 = null;
		StringBuffer sbuf0 = null, sbuf1 = null, sbuf2 = null;
		for( Object value : valueObjs ) {
			String[] values = null;
			try {
				values = ((String)value).split( ";" );
			} catch( ClassCastException castEx ) {
			} catch( NullPointerException nullEx ) {
			}

			boolean hasNull = false;
			List<Object> varList = new java.util.ArrayList<Object>();
			StringBuffer sbuf = new StringBuffer();
			for( int f = 0; f < fieldNames.length; f++ ) {
				if( f > 0 ) sbuf.append( " AND " );
				if( values == null || values.length <= f || values[f].length() == 0 ) {
					hasNull = true;
					sbuf.append( fieldNames[f] +" IS NULL" );
				} else {
					sbuf.append( fieldNames[f] +" = ?" );
					if( dataTypes != null && f < dataTypes.length ) {
						switch( dataTypes[f] ) {
						case AbstractField.TYPE_DATE:
						case AbstractField.TYPE_DATETIME:
							varList.add( convertDate(values[f]) );
							break;
						case AbstractField.TYPE_INTEGER:
						case AbstractField.TYPE_LONG:
						case AbstractField.TYPE_DOUBLE:
							try {
								varList.add( Double.valueOf(values[f]) );
							} catch( NumberFormatException numEx ) {
								varList.add( null );
							}
							break;
						default:
							varList.add( values[f] );
							break;
						}
					} else
						varList.add( values[f] );
				}
			}

			if( valueObjs.length == 1 ) {
				appendCondition( sbuf.toString() );
				bindVars[COND_BINDVAR].addAll( varList );
			} else if( hasNull ) {
				if( sbuf2 == null ) {
					sbuf2 = sbuf.insert( 0, "(" ).append( ")" );
					varList2 = varList;
				} else {
					sbuf2.append(" OR (") .append( sbuf ).append( ")" );
					varList2.addAll( varList );
				}
			} else {
				if( sbuf0 == null ) {
					sbuf0 = new StringBuffer( "(" );
					sbuf1 = new StringBuffer( "(" );
					for( String fieldName : fieldNames ) {
						sbuf0.append( fieldName ).append( "," );
						sbuf1.append( " ?," );
					}
					sbuf0.setCharAt( sbuf0.length() - 1, ')' );
					sbuf1.setCharAt( sbuf1.length() - 1, ')' );

					sbuf0.append( " IN (" );
				}

				sbuf0.append( sbuf1 ).append( "," );
				bindVars[COND_BINDVAR].addAll( varList );
			}
		}

		if( sbuf0 != null ) {
			sbuf0.setCharAt( sbuf0.length() - 1, ')' );
			if( sbuf2 == null )
				appendCondition( sbuf0.toString() );
			else
				appendCondition( "("+ sbuf0.toString() +" OR "+ sbuf2.toString() +")" );
		} else if( sbuf2 != null )
			appendCondition( "("+ sbuf2.toString() +")" );
		if( varList2 != null ) bindVars[COND_BINDVAR].addAll( varList2 );

		return valueObjs.length;
	}

	/**
	 * conditionMap에서 key의 값(NUMBER형식)을 찾아서 WHERE절에 추가.
	 * @see #findConditionNumber(String, String, String)
	 */
	public boolean findConditionNumber( String key, String fieldName ) {
		return findConditionNumber( key, fieldName, null );
	}

	/**
	 * conditionMap에서 key의 값(NUMBER형식)을 찾아서 WHERE절에 추가.
	 * <p>
	 * 다음 순서로 처리:
	 * <ul type='square'>
	 * <li>conditionMap에 (key + "_isNull")이 포함된 경우: appendCondition(fieldName + " IS NULL")
	 * <li>conditionMap에 (key + "_isNotNull")이 포함된 경우: appendCondition(fieldName + " IS NOT NULL")
	 * <li>이외의 경우, 다음과 같이 처리
	 * <ul>
	 * <li>getValues(key)값을 (fieldName + " IN ( ?, ? )")
	 * <li>getValues(key + "_min")값을 (fieldName + " > ? "), defaultType에 따라 '=' 포함
	 * <li>getValues(key + "_max")값을 (fieldName + " < ? "), defaultType에 따라 '=' 포함
	 * </ul>
	 * </ul>
	 * @param defaultType	null일 경우 {@link Condition#CONDTYPE_EQUALS_MINMAX}으로 처리
	 * @return 조건이 추가되었는지 여부
	 */
	public boolean findConditionNumber( String key, String fieldName, String defaultType ) {
		if( conditionMap == null ) return false;

		// (key + SUFFIX_IS_NULL)이 "Y" 또는 "true"인 경우 처리
		// (key + SUFFIX_IS_NOTNULL)이 "Y" 또는 "true"인 경우 처리
		if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NULL) ) {
			appendCondition( fieldName +" IS NULL" );
			return true;
		} else if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NOTNULL) ) {
			appendCondition( fieldName +" IS NOT NULL" );
			return true;
		}

		StringBuffer sbuf = new StringBuffer();

		// type 설정
		Object valueObj = conditionMap.get( key + Condition.SUFFIX_TYPE );
		String type = ( valueObj instanceof String ? (String)valueObj : defaultType );
		if( Condition.CONDTYPE_STARTSWITH.equals(type) || Condition.CONDTYPE_CONTAINS.equals(type)
				|| Condition.CONDTYPE_LIKE.equals(type) || Condition.CONDTYPE_NOT_STARTSWITH.equals(type) )
			return ( findCondition( key, new String[] { fieldName }, defaultType ) > 0 );
		else if( Condition.CONDTYPE_RANGE.equals(type) )
			return ( findConditionRange( key, fieldName, true ) > 0 );

		boolean existCondition = false;
		Object valueObjs[] = getConditionValues( key );
		if( valueObjs != null ) {
			existCondition = true;
			for( Object value : valueObjs ) {
				try {
					if( value instanceof Number )
						bindVars[COND_BINDVAR].add( value );
					else if( value instanceof String )
						bindVars[COND_BINDVAR].add( Double.valueOf((String)value) );
					else
						bindVars[COND_BINDVAR].add( null );
				} catch( NumberFormatException numEx ) {
					bindVars[COND_BINDVAR].add( null );
				}
				sbuf.append( ", ?" );
			}

			if( valueObjs.length == 1 ) {
				if( Condition.CONDTYPE_NOTEQUALS.equals(type) )
					sbuf = new StringBuffer( fieldName + " <> ?" );
				else
					sbuf = new StringBuffer( fieldName + " = ?" );
			} else {
				sbuf.setCharAt( 0, '(' );
				sbuf.append( " )" );
				if( Condition.CONDTYPE_NOTEQUALS.equals(type) )
					sbuf.insert( 0, fieldName + " NOT IN " );
				else
					sbuf.insert( 0, fieldName + " IN " );
			}
		}

		// 최소값 조건
		valueObj = conditionMap.get( key + Condition.SUFFIX_MIN_VALUE );
		if( valueObj != null ) {
			existCondition = true;
			try {
				if( valueObj instanceof String )
					valueObj = Double.valueOf( (String)valueObj );
			} catch( NumberFormatException numEx ) {}

			bindVars[COND_BINDVAR].add( valueObj instanceof Number ? valueObj : null );
			if( sbuf.length() > 0 ) sbuf.append( " AND " );
			if( Condition.CONDTYPE_EQUALS_NONE.equals(type) || Condition.CONDTYPE_EQUALS_MAX.equals(type) )
				sbuf.append( fieldName + " > ? " );
			else
				sbuf.append( fieldName + " >= ? " );
		}

		// 최소값 조건
		valueObj = conditionMap.get( key + Condition.SUFFIX_MAX_VALUE );
		if( valueObj != null ) {
			existCondition = true;
			try {
				if( valueObj instanceof String )
					valueObj = Double.valueOf( (String)valueObj );
			} catch( NumberFormatException numEx ) {}

			bindVars[COND_BINDVAR].add( valueObj instanceof Number ? valueObj : null );
			if( sbuf.length() > 0 ) sbuf.append( " AND " );
			if( Condition.CONDTYPE_EQUALS_NONE.equals(type) || Condition.CONDTYPE_EQUALS_MIN.equals(type) )
				sbuf.append( fieldName + " < ? " );
			else
				sbuf.append( fieldName + " <= ? " );
		}

		if( existCondition ) {
			if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_NULL_OR) )
				sbuf.insert( 0, "(" ).append( " OR "+ fieldName +" IS NULL )" );
			appendCondition( sbuf.toString() );
		}

		return existCondition;
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * range의 값은 "a,b,c,d-e/f,g,h-k"와 같은 형식으로, '/'앞은 IN, '/'뒤에는 NOT IN으로 처리되며, format이 잘못된 경우 하나의 값으로 인식
	 */
	public int findConditionRange( String key, String fieldName ) {
		return findConditionRange( key, fieldName, false );
	}

	/**
	 * conditionMap에서 key의 값을 찾아서 WHERE절에 추가.
	 * range의 값은 "a,b,c,d-e/f,g,h-k"와 같은 형식으로, '/'앞은 IN, '/'뒤에는 NOT IN으로 처리되며, format이 잘못된 경우 하나의 값으로 인식
	 */
	public int findConditionRange( String key, String fieldName, boolean isNumberType ) {
		Object valueObj = conditionMap.get( key );
		if( valueObj == null ) return 0;

		int conditionCount = 0;
		StringBuffer sbuf = new StringBuffer();
		List<Object> bindVarList = new java.util.ArrayList<Object>();
		String[] values = (valueObj.toString()).split( "," );
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
			for( Object bindVar : bindVars )
				bindVarList.add( bindVar );

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

		if( conditionCount == 0 ) {
			appendCondition( fieldName +" = ?", (Object)null );
			return 1;
		} else {
			appendCondition( "("+ sbuf.substring(4) +")" );
			addBindVariables( COND_BINDVAR, bindVarList.toArray() );
			return conditionCount;
		}
	}

	/**
	 * conditionMap에서 key의 값을 하나만 찾아서 WHERE절에 추가.
	 */
	public boolean findConditionSimple( String key, String fieldName ) {
		return findConditionSimple( key, fieldName, false );
	}
	public boolean findConditionSimple( String key, String fieldName, boolean isOuterJoin ) {
		try {
			Object valueObj = conditionMap.get( key );
			if( valueObj != null ) {
				if( isOuterJoin )
					appendCondition( fieldName + "(+) = ?", valueObj );
				else
					appendCondition( fieldName + " = ?", valueObj );
				return true;
			}

			return false;
		} catch( NullPointerException nullEx ) {
			return false;
		}
	}

	/**
	 * conditionMap에서 key의 값을 하나만 찾아서 WHERE절에 추가.
	 */
	public boolean findConditionSimple( String key, String fieldName, char dataType ) {
		try {
			Object valueObj = conditionMap.get( key );
			if( valueObj != null ) {
				appendCondition( fieldName + " = ?", getConditionValue(key, dataType) );
				return true;
			}

			return false;
		} catch( NullPointerException nullEx ) {
			return false;
		}
	}

	public Set<String> getConditionKeys() {
		if( conditionMap == null )
			return new java.util.HashSet<String>();
		else
			return Condition.getConditionKeys( conditionMap );
	}

	/**
	 * conditionMap return
	 */
	public Map<String, ? extends Object> getConditionMap() {
		return conditionMap;
	}

	public Object getConditionValue( String key ) {
		try {
			return conditionMap.get( key );
		} catch( NullPointerException nullEx ) {
			return null;
		}
	}

	public Object getConditionValue( String key, char dataType ) {
		try {
			Object valueObj = conditionMap.get( key );
			if( valueObj instanceof Object[] ) valueObj = ((Object[])valueObj)[0];
			if( valueObj == null ) return null;

			switch( dataType ) {
			case AbstractField.TYPE_CODE:
			case AbstractField.TYPE_DESCRIPTION:
			case AbstractField.TYPE_STRING:
			case AbstractField.TYPE_TIME:
				if( valueObj instanceof String )
					return (String)valueObj;
				else
					return valueObj.toString();
			case AbstractField.TYPE_DATE:
			case AbstractField.TYPE_DATETIME:
				return convertDate( valueObj );
			case AbstractField.TYPE_INTEGER:
			case AbstractField.TYPE_LONG:
			case AbstractField.TYPE_DOUBLE:
				try {
					if( !(valueObj instanceof Number) ) {
						if( valueObj instanceof String )
							return Double.valueOf( (String)valueObj );
						else
							return null;
					}
				} catch( NumberFormatException numEx ) {
					return null;
				}
			default:
				return valueObj;
			}
		} catch( NullPointerException nullEx ) {
			return null;
		}
	}

	public Object[] getConditionValues( String key ) {
		if( conditionMap == null ) return null;
		return Record.extractObjectArray( conditionMap, key );
	}

	public boolean hasConditionMap() {
		return( conditionMap != null );
	}

	public boolean hasConditionValue( String key ) {
		try {
			return conditionMap.containsKey( key );
		} catch( NullPointerException nullEx ) {
			return false;
		}
	}

	public boolean isConditionTrue( String key ) {
		try {
			return Condition.isConditionTrue( conditionMap, key );
		} catch( NullPointerException nullEx ) {
			return false;
		}
	}

	private java.sql.Date convertDate( Object valueObj ) {
		if( valueObj instanceof String ) {
			try {
				return com.irt.data.Date.getInstance( (String)valueObj );
			} catch( java.text.ParseException parseEx ) {
				return null;
			}
		} else if( valueObj instanceof java.util.Date ) {
			if( valueObj instanceof java.sql.Date )
				return (java.sql.Date)valueObj;
			else
				return new java.sql.Date( ((java.util.Date)valueObj).getTime() );
		}

		return null;
	}

	private java.sql.Timestamp convertTimestamp( Object valueObj ) {
		if( valueObj instanceof String ) {
			try {
				return com.irt.data.Timestamp.getInstance( (String)valueObj );
			} catch( java.text.ParseException parseEx ) {
				return null;
			}
		} else if( valueObj instanceof java.util.Date ) {
			if( valueObj instanceof java.sql.Date )
				return new java.sql.Timestamp( ((java.util.Date)valueObj).getTime() );
			else if( valueObj instanceof java.sql.Timestamp )
				return (java.sql.Timestamp)valueObj;
			else
				return new java.sql.Timestamp( ((java.util.Date)valueObj).getTime() );
		}

		return null;
	}

	private Object convertDateOrTimestamp( Object valueObj, Map<String, ? extends Object> conditionMap, String key ) {
		if( Condition.isConditionTrue(conditionMap, key + Condition.SUFFIX_IS_TIMESTAMP) ) {
			return convertTimestamp(valueObj);
		} else {
			return convertDate(valueObj);
		}
	}
}
