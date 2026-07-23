/*
 *	File Name:	Delivery.java
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
public class Delivery extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_ORDER_DELIVERY );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ORDER_DELIVERY );

	public Delivery( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String deliveryNumber ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "deliveryNumber", deliveryNumber );

		return primaryMap;
	}

	public static Table getTable() {
		return table;
	}
}
