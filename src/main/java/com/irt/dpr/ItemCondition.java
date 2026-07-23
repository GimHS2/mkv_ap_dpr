/*
 *	File Name:	ItemCondition.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2009/01/12		2.2.1	create
 *
**/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class ItemCondition {
	public final static int IMAGESAVETYPE_BLOB			= 0x01;
	public final static int IMAGESAVETYPE_PLACE			= 0x02;

	public final static String ITEMSTATUS_NORMAL		= "00";
	public final static String ITEMSTATUS_TRADEOFF		= "99";

	public static void setItemCondition( Map<String, Object> conditionMap, com.irt.data.Date currDate ) {
		conditionMap.put( "availableDate", currDate );
		conditionMap.put( "startAvailDate" + Condition.SUFFIX_MAX_VALUE, currDate );
		conditionMap.put( "startAvailDate" + Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_MAX );
		conditionMap.put( "startAvailDate" + Condition.SUFFIX_IS_NULL_OR, "Y" );
		conditionMap.put( "endAvailDate" + Condition.SUFFIX_MIN_VALUE, currDate );
		conditionMap.put( "endAvailDate" + Condition.SUFFIX_TYPE, Condition.CONDTYPE_EQUALS_NONE );
		conditionMap.put( "endAvailDate" + Condition.SUFFIX_IS_NULL_OR, "Y" );


	}
}
