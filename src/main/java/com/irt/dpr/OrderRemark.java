/*
 *	File Name:	OrderRemark.java
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
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/*
 *
 */
public class OrderRemark extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_ORDER_REMARK );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ORDER_REMARK );

	public OrderRemark( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String orderKey ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "orderKey", orderKey );

		return primaryMap;
	}

	public boolean update( Map<String, Object> record ) throws SQLException, DataException {
		return SQLManager.manageRecord( handler, table, record, Record.UPDATE | Record.INSERT );
	}
}
