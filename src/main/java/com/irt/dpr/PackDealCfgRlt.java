/*
 *	File Name:	PackDealCfgRlt.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/05/30		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.Condition;
import com.irt.data.Record;
import com.irt.rbm.QueryableManagerImpl;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PackDealCfgRlt extends QueryableManagerImpl {
	private final static Table table = Schema.findTable(Schema.DPR_PACKDEAL_CFGRLT);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_PACKDEAL_CFGRLT);

	public PackDealCfgRlt( SQLHandler handler ) {
		super(handler, factory);
	}

	public List<Map<String, Object>> getPackDealCodeList( Map<String, Object> conditionMap, String[] fieldKeys ) throws SQLException {
		if( !conditionMap.containsKey("partyCode") && conditionMap.containsKey("soldPartyCode") )
			Condition.putConditionValueOnly(conditionMap, "partyCode", conditionMap.get("soldPartyCode"));

		conditionMap.put(Condition.DISTINCT_CONDITIONKEY, "Y");

		conditionMap.put("hasItem", "Y");

		return getRecords(conditionMap, fieldKeys);
	}

	public Object[] getPackDealCodes( Map<String, Object> conditionMap ) throws SQLException {
		return Record.extractObjectArray(getPackDealCodeList(conditionMap, new String[] { "dealCode" }), "dealCode");
	}
}
