/*
 *	File Name:	BillingDetail.java
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
public class BillingDetail extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_ORDER_BILLING_DTL );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ORDER_BILLING_DTL );

	public BillingDetail( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String billingNumber, String lineNumber ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "billingNumber", billingNumber );
		primaryMap.put( "lineNumber", lineNumber );

		return primaryMap;
	}

	public Map<String, Object> getBillingSummary( Map<String, Object> conditionMap ) throws SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );

		querybuf.appendDataWithAlias( "OBID.VOLUME", "volume" );
		querybuf.appendDataWithAlias( "OBID.VOLUME_UNIT", "volumeUnit" );
		querybuf.appendDataWithAlias( "OBID.WEIGHT", "weight" );
		querybuf.appendDataWithAlias( "OBID.WEIGHT_UNIT", "weightUnit" );
		querybuf.appendDataWithAlias( "OBID.BILLING_QTY", "billingQty" );
		querybuf.appendDataWithAlias( "OBID.BILLING_NETAMOUNT", "billingNetAmount" );
		querybuf.appendDataWithAlias( "OBID.BILLING_TAX", "billingTax" );
		querybuf.appendDataWithAlias( "OBID.BILLING_DAMAGEDDISCOUNT", "billingDamagedDiscount" );
		querybuf.appendDataWithAlias( "OBID.BILLING_VALUE", "billingValue" );

		/* Summary Inner Table */
		ConditionQueryBuffer inner_querybuf = new ConditionQueryBuffer( conditionMap );
		inner_querybuf.appendDataWithAlias( "SUM(OBID.VOLUME)", "VOLUME" );
		inner_querybuf.appendDataWithAlias( "MAX(OBID.VOLUME_UNIT)", "VOLUME_UNIT" );
		inner_querybuf.appendDataWithAlias( "SUM(OBID.WEIGHT)", "WEIGHT" );
		inner_querybuf.appendDataWithAlias( "MAX(OBID.WEIGHT_UNIT)", "WEIGHT_UNIT" );
		inner_querybuf.appendDataWithAlias( "SUM(OBID.BILLING_QTY)", "BILLING_QTY" );
		inner_querybuf.appendDataWithAlias( "SUM(OBID.BILLING_NETAMOUNT)", "BILLING_NETAMOUNT" );
		inner_querybuf.appendDataWithAlias( "SUM(OBID.BILLING_TAX)", "BILLING_TAX" );
		inner_querybuf.appendDataWithAlias( "SUM(OBID.BILLING_DAMAGEDDISCOUNT)", "BILLING_DAMAGEDDISCOUNT" );
		inner_querybuf.appendDataWithAlias( "SUM(OBID.BILLING_VALUE)", "BILLING_VALUE" );

		inner_querybuf.appendTableWithAlias( "DPR_ORDER_BILLING_DTL", "OBID" );
		inner_querybuf.findCondition( "billingNumber", "OBID.BILLING_NUMBER" );

		querybuf.appendTable( inner_querybuf, "OBID" );

		return SQLManager.getRecordMap( handler, null, querybuf );
	}

	public static Table getTable() {
		return table;
	}
}
