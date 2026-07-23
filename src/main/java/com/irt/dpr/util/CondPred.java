/*
 *	File Name:	CondPred.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/03/30		2.2.0c	create
**/


package com.irt.dpr.util;

import com.irt.data.Condition;

import java.util.Map;

/**
 * Reusable Condition Predicates for "condition" or "conditionMap<String, Object>" Map object.
 */
public class CondPred {

	public static Map<String, Object> putIsEquals( Map<String, Object> conditionMap, String fieldKey, Object fieldValue ) {
		conditionMap.put(fieldKey, fieldValue);
		conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS);
		return conditionMap;
	}

	/**
	 * is same as below statement
	 *
	 * <pre>
	 * if( !conditionMap.containsKey(fieldKey) ) {
	 * 	conditionMap.put(fieldKey, fieldValue);
	 * 	conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS);
	 * }
	 * </pre>
	 *
	 * @param conditionMap
	 * @param fieldKey
	 * @param fieldValue
	 * @return
	 */
	public static Map<String, Object> putIsEqualsIfNoKey( Map<String, Object> conditionMap, String fieldKey, Object fieldValue ) {
		if( !conditionMap.containsKey(fieldKey) ) {
			conditionMap.put(fieldKey, fieldValue);
			conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS);
		}
		return conditionMap;
	}

	public static Map<String, Object> putNullOrValue( Map<String, Object> conditionMap, String fieldKey, Object fieldValue ) {
		conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_IS_NULL_OR, "Y");
		conditionMap.put(fieldKey, fieldValue);

		return conditionMap;
	}

	public static Map<String, Object> putDateLessThan( Map<String, Object> conditionMap, String fieldKey, java.util.Date date ) {
		conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_MAX_VALUE, date);
		conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS_NONE);
		return conditionMap;
	}

	public static Map<String, Object> putIsNotEquals( Map<String, Object> conditionMap, String fieldKey, Object fieldValue ) {
		conditionMap.put(fieldKey, fieldValue);
		conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_NOTEQUALS);
		return conditionMap;
	}

	public static Map<String, Object> putIsNotEqualsOrIsNull( Map<String, Object> conditionMap, String fieldKey, Object fieldValue ) {
		conditionMap.put(fieldKey, fieldValue);
		conditionMap.put(fieldKey + Condition.SUFFIX_IS_NULL_OR, "Y");
		conditionMap.put(fieldKey + Condition.SUFFIX_TYPE, Condition.CONDTYPE_NOTEQUALS);
		return conditionMap;
	}

	public static Map<String, Object> putIsNotEqualsIfNoKey( Map<String, Object> conditionMap, String fieldKey, Object fieldValue ) {
		if( !conditionMap.containsKey(fieldKey) ) {
			conditionMap.put(fieldKey, fieldValue);
			conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_NOTEQUALS);
		}
		return conditionMap;
	}

	public static Map<String, Object> putIsNotNull( Map<String, Object> conditionMap, String fieldKey ) {
		conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_IS_NOTNULL, "Y");
		return conditionMap;
	}

	public static Map<String, Object> putIsNotNullIfNoKey( Map<String, Object> conditionMap, String fieldKey ) {
		if( !conditionMap.containsKey(fieldKey) ) {
			conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_IS_NOTNULL, "Y");
		}
		return conditionMap;
	}

	public static Map<String, Object> putValueIfNoKey( Map<String, Object> conditionMap, String fieldKey, Object fieldValue ) {
		if( !conditionMap.containsKey(fieldKey) ) {
			conditionMap.put(fieldKey, fieldValue);
		}
		return conditionMap;
	}

	/**
	 * @deprecated use putValueIfNoKey ( method name is not right, putting suffix_type is wrong. use other two other method putValueIfNoKey or
	 *             putIsEqaulsIfNoKey )
	 * @param conditionMap
	 * @param fieldKey
	 * @param fieldValue
	 * @return
	 */
	@Deprecated
	public static Map<String, Object> putOnlyIfNoKey( Map<String, Object> conditionMap, String fieldKey, Object fieldValue ) {
		if( !conditionMap.containsKey(fieldKey) ) {
			conditionMap.put(fieldKey, fieldValue);
			conditionMap.put(fieldKey + com.irt.data.Condition.SUFFIX_TYPE, com.irt.data.Condition.CONDTYPE_EQUALS);
		}
		return conditionMap;
	}

	public static Map<String, Object> putDistinct( Map<String, Object> conditionMap ) {
		conditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");
		return conditionMap;
	}
}
