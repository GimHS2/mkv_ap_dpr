/*
 *	File Name:	PartyOperation.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	dudwls3720	2024/07/31		2.2.3	getOperationInd(): Sales office 휴일 설정 시 특정 Sales Group만 발주 할 수 있도록 로직 변경
 *	hankalam	2022/03/31		2.2.2	getOperationInd(): N 값을 기준으로 중복 휴일에 대해 우선순위 없이 전체 적용되도록 변경
 *	hankalam	2020/08/28		2.2.1	getFieldValue(): 정렬 추가
 *	hankalam	2019/06/28		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.sql.*;

import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class PartyOperation extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_PARTY_OPER );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_PARTY_OPER );

	public final static char TYPE_DATE					= '0';
	public final static char TYPE_DAY_OF_MONTH			= '1';
	public final static char TYPE_WEEK_OF_MONTH			= '2';
	public final static char TYPE_DAYINTERVAL			= '3';
	public final static char TYPE_DAY_OF_WEEK			= '4';
	public final static char TYPE_EVERYDAY				= '5';
	public final static char TYPE_DAY_OF_YEAR			= '6';

	public PartyOperation( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPatternMap( Object patternType, Object patternIndex, Object patternDate ) {
		Map<String, Object> patternMap = new java.util.TreeMap<String, Object>();

		if( patternType != null && patternType.toString().length() == 1 ) {
			switch( patternType.toString().charAt(0) ) {
			case TYPE_DATE:
				patternIndex = new Integer(0);
				break;
			case TYPE_EVERYDAY:
				patternIndex = new Integer(0);
			case TYPE_DAY_OF_MONTH:
			case TYPE_WEEK_OF_MONTH:
			case TYPE_DAY_OF_WEEK:
				patternDate = getDefaultPatternDate();
				break;
			}
		}
		patternMap.put( "patternType", patternType );
		patternMap.put( "patternIndex", patternIndex );
		patternMap.put( "patternDate", patternDate );

		return patternMap;
	}

	public static Map<String, Object> createPrimary( String organizationCode, String distributionChannelCode, String officeCode, String groupCode, Object patternType, Object patternIndex, Object patternDate ) {
		Map<String, Object> primaryMap = createPatternMap( patternType, patternIndex, patternDate );

		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );
		primaryMap.put( "officeCode", officeCode );
		primaryMap.put( "groupCode", groupCode );

		return primaryMap;
	}

	public static Map<String, Object> createCondition( String organizationCode, String distributionChannelCode, String officeCode, String groupCode, com.irt.data.Date operDate ) {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();

		conditionMap.put( "organizationCode", organizationCode );
		conditionMap.put( "distributionChannelCode", distributionChannelCode );
		conditionMap.put( "officeCode", officeCode );
		conditionMap.put( "groupCode", groupCode );
		conditionMap.put( "operDate", operDate );

		return conditionMap;
	}

	public static java.util.Date getDefaultPatternDate() {
		try {
			return com.irt.data.Date.getInstance( "1900-01-01" );
		} catch( java.text.ParseException parseEx ) {
			return null;
		}
	}

	public String getOperationInd ( String organizationCode, String distributionChannelCode, String officeCode, String groupCode, com.irt.data.Date orderDate, String fieldKey ) throws IllegalArgumentException, SQLException {
		Map<String, Object> conditionMap = createCondition( organizationCode, distributionChannelCode, officeCode, groupCode, orderDate );
		String operInd = (String) getFieldValue( conditionMap, fieldKey );
		if( operInd == null ) {
			conditionMap = createCondition( organizationCode, distributionChannelCode, "0", groupCode, orderDate );
			operInd = (String) getFieldValue( conditionMap, fieldKey );
		}
		if( operInd == null ) {
			conditionMap = PartyOperation.createCondition( organizationCode, "0", "0", groupCode, orderDate );
			operInd = (String) getFieldValue( conditionMap, fieldKey );
		}
		if( operInd == null ) {
			conditionMap = PartyOperation.createCondition( organizationCode, distributionChannelCode, officeCode, "0", orderDate );
			operInd = (String) getFieldValue( conditionMap, fieldKey );
		}
		if( operInd == null ) {
			conditionMap = PartyOperation.createCondition( organizationCode, "0", officeCode, "0", orderDate );
			operInd = (String) getFieldValue( conditionMap, fieldKey );
		}
		if( operInd == null ) {
			conditionMap = PartyOperation.createCondition( organizationCode, distributionChannelCode, "0", "0", orderDate );
			operInd = (String) getFieldValue( conditionMap, fieldKey );
		}
		if( operInd == null ) {
			conditionMap = PartyOperation.createCondition( organizationCode, "0", "0", "0", orderDate );
			operInd = (String) getFieldValue( conditionMap, fieldKey );
		}

		if( operInd == null ) {
			operInd = "Y";
		}

		return operInd;
	}

	@Override
	public Object getFieldValue( Map<String, ? extends Object> conditionMap, String fieldKey ) throws IllegalArgumentException, SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );

		if( !factory.appendData(querybuf, fieldKey) )
			throw new IllegalArgumentException( "invalid fieldKey '"+ fieldKey +"'" );

		factory.appendCondition( querybuf );

		querybuf.appendOrderByFieldName( fieldKey );
		return SQLManager.getObjectValue( handler, querybuf );
	}
}

