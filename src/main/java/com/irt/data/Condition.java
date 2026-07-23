/*
 *	File Name:	Condition.java
 *	Version:	2.2.6c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 * 	jbaek		2018/10/30		2.2.6c	Timestamp 지원
 *	GimHS		2016/09/30		2.2.6	Java Document 주석 추가, Generic Type warning 수정
 *	GimHS		2014/12/31		2.2.5	CONDTYPE_REGEX(정규표현식을 사용하는 조건) 추가
 *	stghr12		2011/02/28		2.2.4	checkRangeCondition() 추가
 *	stghr12		2010/08/31		2.2.3	varargs 사용([] -> ...)
 *	yjcha		2010/04/16		2.2.2	NOT LIKE 조건 추가
 *	stghr12		2009/06/30		2.2.1	GROUPINGWAY_CONDITIONKEY 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.2	BASIS_CONDITIONKEY, getConditionValueOnly(), removeReservedKeys() 추가
 *	stghr12		2007/04/30		2.1.1	isConditionTrue(conditionValue), moveCondition() 추가
 *										isConditionTrue(conditionMap, conditionValue): conditionMap이 null일 경우 처리
 *	stghr12		2006/12/01		2.1.0	CONDTYPE_RANGE, DISTINCT_CONDITIONKEY, GROUPING_CONDITIONKEY 추가
 *										clearCondition(), clearConditionSuffix(), containsGroupKey(), copyConditionValue() 추가
 *										putConditionValueOnly() 추가
 *										getConditionKeys(): "_"로 시작하는 conditionKey 무시하는 로직 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

import java.util.Map;
import java.util.Set;

/**
 * 쿼리에 사용할 조건을 정의하고 처리하는 Class.
 */
public final class Condition {
	/** FIELD = &#63; **/
	public final static String CONDTYPE_EQUALS			= "EQU";
	/** FIELD <> &#63; **/
	public final static String CONDTYPE_NOTEQUALS		= "NEQ";
	/** FIELD LIKE &#63;% **/
	public final static String CONDTYPE_STARTSWITH		= "STW";
	/** FIELD LIKE %&#63; **/
	public final static String CONDTYPE_ENDSWITH		= "EDW";
	/** FIELD LIKE %&#63;% **/
	public final static String CONDTYPE_CONTAINS		= "CTS";
	/** FIELD LIKE &#63; **/
	public final static String CONDTYPE_LIKE			= "LKE";
	/** FIELD NOT LIKE &#63;% **/
	public final static String CONDTYPE_NOT_STARTSWITH	= "NSW";
	/** ',', '-', '/' 사용하는 범위조건 (예 : "1,2" -> 1과 2, "1-3" -> 1부터 3까지, "1-10/4-6 -> 1부터 10중에 4부터 6은 제외) **/
	public final static String CONDTYPE_RANGE			= "RNG";
	/** 정규표현식을 사용하는 조건 **/
	public final static String CONDTYPE_REGEX			= "REX";

	/** Min, Max쪽 모두 등호가 사용되지 않음, min < x < max **/
	public final static String CONDTYPE_EQUALS_NONE		= "ENN";
	/** Min쪽에 등호가 사용됨, min <= x < max **/
	public final static String CONDTYPE_EQUALS_MIN		= "EMN";
	/** Max쪽에 등호가 사용됨, min < x <= max **/
	public final static String CONDTYPE_EQUALS_MAX		= "EMX";
	/** Min, Max쪽 모두 등호가 사용됨, min <= x <= max **/
	public final static String CONDTYPE_EQUALS_MINMAX	= "EMM";

	/** 조인 쿼리를 생성할때 Basic 테이블을 구분하는 Key **/
	public final static String BASIS_CONDITIONKEY		= "basisValue";
	/** 쿼리에 DISTINCT 문을 추가할지 여부를 가리키는 Key(Y,N, true, false 등을 값으로 사용) **/
	public final static String DISTINCT_CONDITIONKEY	= "distinctingValue";
	/** 쿼리에서 GROUP BY 절에 추가할 fieldKey를 가리키는 Key  **/
	public final static String GROUPING_CONDITIONKEY	= "groupBy";
	/** 쿼리에서 GROUP BY 절에 fieldKey를 추가할 때 정렬 방법(ASC, DESC)을 가리키는 Key **/
	public final static String GROUPINGWAY_CONDITIONKEY	= "groupByWay";

	/** 쿼리의 조건 타입, 이 Key에 대한 값은 CONDTYPE_* 중 하나가 들어감 (예: conditionMap.put("gtin"+ SUFFIX_TYPE, CONDTYPE_EQUALS)) **/
	public final static String SUFFIX_TYPE				= "_type";
	/** FIELD IS NOT NULL (예: conditionMap.put("gtin"+ SUFFIX_IS_NOTNULL, "Y")) **/
	public final static String SUFFIX_IS_NOTNULL		= "_isNotNull";
	/** FIELD IS NULL (예: conditionMap.put("gtin"+ SUFFIX_IS_NULL, "Y")) **/
	public final static String SUFFIX_IS_NULL			= "_isNull";
	/** FIELD = &#63; OR FIELD IS NULL (예: conditionMap.put("gtin"+ SUFFIX_IS_NULL_OR, "Y")) **/
	public final static String SUFFIX_IS_NULL_OR		= "_isNullOr";
	/** FIELD > &#63;, CONDTYPE_EQUALS_* 조건과 함께 사용 (예: conditionMap.put("gtin"+ SUFFIX_MIN_VALUE, 2), conditionMap.put("gtin"+ SUFFIX_TYPE, CONDTYPE_EQUALS_NONE)) **/
	public final static String SUFFIX_MIN_VALUE			= "_min";
	/** FIELD < &#63;, CONDTYPE_EQUALS_* 조건과 함께 사용 (예: conditionMap.put("gtin"+ SUFFIX_MAX_VALUE, 5), conditionMap.put("gtin"+ SUFFIX_TYPE, CONDTYPE_EQUALS_NONE)) **/
	public final static String SUFFIX_MAX_VALUE			= "_max";

	public final static String SUFFIX_IS_TIMESTAMP			= "_isTimestamp";

	/** 기본 생성자 함수 **/
	Condition() {}

	private final static String[] SUFFIXS = new String[] {
		SUFFIX_TYPE, SUFFIX_IS_NOTNULL, SUFFIX_IS_NULL, SUFFIX_IS_NULL_OR, SUFFIX_MIN_VALUE, SUFFIX_MAX_VALUE
	};

	/**
	 * 범위조건(rangeConditionValue)에 값(value)이 속하는 지 검사
	 * @param rangeConditionValue ',', '-', '/' 사용하는 범위조건 (예 : "1,2" -> 1과 2, "1-3" -> 1부터 3까지, "1-10/4-6 -> 1부터 10중에 4부터 6은 제외)
	 * @param value 체크할 값({@link Number})
	 */
	public static boolean checkRangeCondition( String rangeConditionValue, Number value ) {
		if( value == null ) return false;

		double doubleValue = value.doubleValue();

		String[] conditionValues = rangeConditionValue.split( "," );
		for( String conditionValue : conditionValues ) {
			String[] splitValues, inValues, outValues;

			splitValues = conditionValue.split( "/" );
			inValues = splitValues[0].split( "-" );
			outValues = ( splitValues.length > 1 ? splitValues[1].split("-") : null );
			if( splitValues.length > 2 || inValues.length > 2 || (outValues != null && outValues.length > 2) ) continue;

			try {
				double minValue, maxValue;

				minValue = Double.parseDouble( inValues[0].trim() );
				if( inValues.length > 1 ) {
					maxValue = Double.parseDouble( inValues[1].trim() );
					if( doubleValue < minValue || doubleValue > maxValue )
						continue;
				} else {
					if( doubleValue != minValue )
						continue;
				}
				if( outValues == null ) continue;

				minValue = Double.parseDouble( outValues[0].trim() );
				if( outValues.length > 1 ) {
					maxValue = Double.parseDouble( outValues[1].trim() );
					if( doubleValue < minValue || doubleValue > maxValue ) return true;
				} else {
					if( doubleValue != minValue ) return true;
				}
			} catch( NumberFormatException numEx ) {
				continue;
			}
		}

		return false;
	}

	/**
	 * 범위조건(rangeConditionValue)에 값(value)이 속하는 지 검사
	 * @param rangeConditionValue ',', '-', '/' 사용하는 범위조건 (예 : "A,B" -> A와 B, "A-C" -> A부터 C까지, "A-Z/O-P -> A부터 Z중에 O부터 P는 제외)
	 * @param value 체크할 값({@link String})
	 */
	public static boolean checkRangeCondition( String rangeConditionValue, String value ) {
		if( value == null ) return false;

		String[] conditionValues = rangeConditionValue.split( "," );
		for( String conditionValue : conditionValues ) {
			String[] splitValues, inValues, outValues;

			splitValues = conditionValue.split( "/" );
			inValues = splitValues[0].split( "-" );
			outValues = ( splitValues.length > 1 ? splitValues[1].split("-") : null );
			if( splitValues.length > 2 || inValues.length > 2 || (outValues != null && outValues.length > 2) ) {
				if( value.equals(conditionValue) )
					return true;
			}

			String minValue, maxValue;

			minValue = inValues[0].trim();
			if( inValues.length > 1 ) {
				maxValue = inValues[1].trim();
				if( value.compareTo(minValue) < 0 || value.compareTo(maxValue) > 0 )
					continue;
			} else {
				if( !value.equals(minValue) )
					continue;
			}
			if( outValues == null ) continue;

			minValue = outValues[0].trim();
			if( outValues.length > 1 ) {
				maxValue = outValues[1].trim();
				if( value.compareTo(minValue) < 0 || value.compareTo(maxValue) > 0 ) return true;
			} else {
				if( !value.equals(minValue) ) return true;
			}
		}

		return false;
	}

	/**
	 * conditionMap에서 conditionKey와 conditionKey + SUFFIX_* 들을 제거.
	 */
	public static void clearCondition( Map<String, ? extends Object> conditionMap, String... conditionKeys ) {
		for( String conditionKey : conditionKeys ) {
			conditionMap.remove( conditionKey );
			for( int i = 0; i < SUFFIXS.length; i++ )
				conditionMap.remove( conditionKey + SUFFIXS[i] );
		}
	}

	/**
	 * conditionMap에 conditionKey값만 유지하고 conditionKey + SUFFIX_* 들을 제거.
	 */
	public static void clearConditionSuffix( Map<String, ? extends Object> conditionMap, String... conditionKeys ) {
		for( String conditionKey : conditionKeys ) {
			for( int i = 0; i < SUFFIXS.length; i++ )
				conditionMap.remove( conditionKey + SUFFIXS[i] );
		}
	}

	/**
	 * conditionMap안에 {@link #GROUPING_CONDITIONKEY}의 값으로 groupKey을 포함하는 지 여부 return.
	 */
	public static boolean containsGroupKey( Map<String, ? extends Object> conditionMap, String groupKey ) {
		Object[] conditionGroupKeys = Record.extractObjectArray( conditionMap, GROUPING_CONDITIONKEY );
		if( conditionGroupKeys == null ) return false;

		for( int i = 0; i < conditionGroupKeys.length; i++ )
			if( groupKey.equals(conditionGroupKeys[i]) )
				return true;

		return false;
	}

	/**
	 * conditionMap안에 {@link #GROUPING_CONDITIONKEY}의 값으로 groupKeys를 포함하는 지 여부 return.
	 * @param containsAll true일 경우에는 모든 groupKeys를 포함해야 true return, false일 경우 하나의 groupKeys만 포함해도 true return.
	 */
	public static boolean containsGroupKey( Map<String, ? extends Object> conditionMap, String[] groupKeys, boolean containsAll ) {
		Object[] conditionGroupKeys = Record.extractObjectArray( conditionMap, GROUPING_CONDITIONKEY );
		if( conditionGroupKeys == null ) return false;

		for( int i = 0; i < groupKeys.length; i++ ) {
			boolean done = false;

			for( int j = 0; j < conditionGroupKeys.length; j++ ) {
				if( groupKeys[i].equals(conditionGroupKeys[j]) ) {
					done = true;
					break;
				}
			}
			if( containsAll != done ) return done;
		}

		return containsAll;
	}

	/**
	 * sourceMap의 (conditionKey와 conditionKey + SUFFIX_*) 값들을 destinationMap으로 복사.
	 */
	public static void copyConditionValue( Map<String, ? extends Object> sourceMap, Map<String, Object> destinationMap, String... conditionKeys ) {
		for( String conditionKey : conditionKeys ) {
			if( sourceMap.containsKey(conditionKey) )
				destinationMap.put( conditionKey, sourceMap.get(conditionKey) );

			for( int i = 0; i < SUFFIXS.length; i++ )
				if( sourceMap.containsKey(conditionKey + SUFFIXS[i]) )
					destinationMap.put( conditionKey + SUFFIXS[i], sourceMap.get(conditionKey + SUFFIXS[i]) );
		}
	}

	/**
	 * conditionMap에 들어있는 conditionKey들을 Set 형태로 return.
	 */
	public static Set<String> getConditionKeys( Map<String, ? extends Object> conditionMap ) {
		Set<String> keySet = new java.util.HashSet<String>();

		for( String key: conditionMap.keySet() ) {
			if( key.startsWith("_") ) continue;

			for( int i = 0; i < SUFFIXS.length; i++ ) {
				if( key.endsWith(SUFFIXS[i]) ) {
					key = key.substring( 0, key.length() - SUFFIXS[i].length() );
					break;
				}
			}
			keySet.add( key );
		}

		return keySet;
	}

	/**
	 * conditionMap에 key값에 해당하는 conditionValue return,
	 * 단, conditionMap에 {@link #SUFFIX_IS_NOTNULL}이나 {@link #SUFFIX_IS_NULL}이 없고 조건 타입이 {@link #CONDTYPE_EQUALS}이고 값이 하나일 경우만.
	 */
	public static Object getConditionValueOnly( Map<String, ? extends Object> conditionMap, String key ) {
		Object conditionValue = conditionMap.get( key );

		if( conditionValue == null || conditionValue instanceof Object[] ) return null;
		if( conditionMap.get(key + SUFFIX_IS_NOTNULL) != null ) return null;
		if( conditionMap.get(key + SUFFIX_IS_NULL) != null ) return null;
		if( conditionMap.get(key + SUFFIX_IS_NULL_OR) != null ) return null;

		Object conditionType = conditionMap.get( key + SUFFIX_TYPE );
		if( conditionType == null || CONDTYPE_EQUALS.equals(conditionType) )
			return conditionValue;
		else
			return null;
	}

	/**
	 * coditionMap에 conditionKey에 해당하는 값이 true인지 여부 return.
	 * <ul type='square'>
	 * <li>conditionKey에 해당하는 값이 Boolean instance일 때, ((Boolean)value).booleanValue() return.
	 * <li>conditionKey에 해당하는 값이 String instance일 때, "Y".equals(value) || Boolean.valueOf((String)value).booleanValue() return.
	 * </ul>
	 */
	public static boolean isConditionTrue( Map<String, ? extends Object> conditionMap, String conditionKey ) {
		try {
			return isConditionTrue( conditionMap.get(conditionKey) );
		} catch( NullPointerException nullEx ) {
			if( conditionMap == null ) return false;
			throw nullEx;
		}
	}

	/**
	 * conditionValue가 true인지 여부 return.
	 * <ul type='square'>
	 * <li>conditionValue가 Boolean instance일 때, ((Boolean)value).booleanValue() return.
	 * <li>conditionValue가 String instance일 때, "Y".equals(value) || Boolean.valueOf((String)value).booleanValue() return.
	 * </ul>
	 */
	public static boolean isConditionTrue( Object conditionValue ) {
		if( conditionValue instanceof Boolean )
			return ((Boolean)conditionValue).booleanValue();
		else if( conditionValue instanceof String )
			return ( "Y".equals(conditionValue) || Boolean.valueOf((String)conditionValue).booleanValue() );

		return false;
	}

	/**
	 * conditionMap에서 sourceKey에 해당하는 조건들을 삭제하고 destinationKey로 다시 put.
	 */
	public static void moveCondition( Map<String, Object> conditionMap, String sourceKey, String destinationKey ) {
		if( conditionMap.containsKey(sourceKey) )
			conditionMap.put( destinationKey, conditionMap.remove(sourceKey) );
		else
			conditionMap.remove( destinationKey );

		for( int i = 0; i < SUFFIXS.length; i++ )
			if( conditionMap.containsKey(sourceKey + SUFFIXS[i]) )
				conditionMap.put( destinationKey + SUFFIXS[i], conditionMap.get(sourceKey + SUFFIXS[i]) );
			else
				conditionMap.remove( destinationKey + SUFFIXS[i] );
	}

	/**
	 * conditionMap에 conditionKey 값에 conditionValue을 put하고, conditionKey + SUFFIX_* 들을 제거.
	 */
	public static void putConditionValueOnly( Map<String, Object> conditionMap, String conditionKey, Object conditionValue ) {
		for( int i = 0; i < SUFFIXS.length; i++ )
			conditionMap.remove( conditionKey + SUFFIXS[i] );
		conditionMap.put( conditionKey, conditionValue );
	}

	/**
	 * conditionMap에 conditionKey 값에 conditionValue을 put하고, conditionKey + SUFFIX_TYPE 값에 conditionType을 put하고
	 * , conditionKey + SUFFIX_* 을 제거.
	 */
	public static void putConditionValueOnly( Map<String, Object> conditionMap, String conditionKey, Object conditionValue, String conditionType ) {
		for( int i = 0; i < SUFFIXS.length; i++ )
			conditionMap.remove( conditionKey + SUFFIXS[i] );
		conditionMap.put( conditionKey, conditionValue );
		conditionMap.put( conditionKey + SUFFIX_TYPE, conditionType );
	}

	/**
	 * conditionMap에서 예약된 key({@link #BASIS_CONDITIONKEY}, {@link #DISTINCT_CONDITIONKEY}, {@link #GROUPING_CONDITIONKEY}, {@link #GROUPINGWAY_CONDITIONKEY})를 삭제.
	 */
	public static void removeReservedKeys( Map<String, ? extends Object> conditionMap ) {
		conditionMap.remove( BASIS_CONDITIONKEY );
		conditionMap.remove( DISTINCT_CONDITIONKEY );
		conditionMap.remove( GROUPING_CONDITIONKEY );
		conditionMap.remove( GROUPINGWAY_CONDITIONKEY );
	}

	/**
	 * check valueObj is pure java.sql.Timestamp
	 */
	public static boolean isTimestampOfSql( Object valueObj ) {
		if( valueObj instanceof java.sql.Timestamp ) {
			if( valueObj instanceof com.irt.data.Timestamp ) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * check valueObj is com.irt.data.Timestamp
	 */
	public static boolean isTimestampOfData( Object valueObj ) {
		if( valueObj instanceof java.sql.Timestamp ) {
			if( valueObj instanceof com.irt.data.Timestamp ) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
}
