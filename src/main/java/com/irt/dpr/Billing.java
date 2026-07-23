/*
 *	File Name:	Billing.java
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
import java.util.List;
import java.util.Map;

/*
 *
 */
public class Billing extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_ORDER_BILLING );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ORDER_BILLING );

	private final static Table tableDtl = Schema.findTable( Schema.DPR_ORDER_BILLING_DTL );
	private final static QueryFactory factoryDtl = Schema.findQueryFactory( Schema.DPR_ORDER_BILLING_DTL );

	public Billing( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String billingNumber ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "billingNumber", billingNumber );

		return primaryMap;
	}

	public static Table getTable() {
		return table;
	}

	public static Table getDetailTable() {
		return tableDtl;
	}

	public String getSoldPartyWithBilling( String billingNumber, String orderNumber ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler
				, "SELECT NVL(OBIL.SOLD_PARTYCD, ORD.PARTYCD) FROM DPR_ORDER_BILLING OBIL, DPR_ORDER ORD"
						+ " WHERE OBIL.ORDER_NUMBER(+) = ORD.ORDER_NUMBER AND OBIL.BILLING_NUMBER(+) = ?"
						+ " AND ORD.ORDER_NUMBER = ?", new Object[] { billingNumber, orderNumber } );
	}


	public boolean updateHeader( Map<String, Object> header ) throws SQLException, DataException {
		return SQLManager.manageRecord( handler, table, header, Record.UPDATE | Record.INSERT );
	}

	public DataResult updateDetail( List<Map<String, Object>> details ) throws SQLException {
		return SQLManager.manageRecordAll( handler, tableDtl, details, Record.UPDATE | Record.INSERT );
	}
}
