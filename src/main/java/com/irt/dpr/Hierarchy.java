/*
 *	File Name:	Hierarchy.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2008/09/26		2.2.0	create
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
public class Hierarchy extends com.irt.rbm.RBMDataManager {

	public Hierarchy( SQLHandler handler ) {
		super( handler );
	}

	public static QueryBuffer getInnerHierarchyQuery( Map<String, Object>conditionMap ) {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );

		querybuf.appendData( "IMT.ITEM_CD \"ITEMCD\"" );
		querybuf.appendData( "MSTD.MASTER_NAME \"PCATENAME\"" );
		querybuf.appendData( "IMT.PCATECD" );
	
		querybuf.appendTableWithAlias( "DPR_ITEM_MASTER", "IMT" );
		querybuf.appendTableWithAlias( "DPR_MASTER_DESC", "MSTD", "MSTD.MASTER_CD(+) = IMT.PCATECD AND MSTD.MASTER_TYPE(+) = 'PC'" );
		querybuf.findCondition( "hierarchyCondition", "IMT.PCATECD" );
		querybuf.findCondition( "displayLanguage", "MSTD.LANGCD" );

		return querybuf;
	}
}
