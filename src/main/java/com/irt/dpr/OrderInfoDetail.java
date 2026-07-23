/*
 *	File Name:	OrderInfoDetail.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/03/31		2.2.1	DELIVERY_STATUS 추가
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
public class OrderInfoDetail extends com.irt.rbm.ManipulableManagerImpl {
	public static final String DELIVERY_STATUS_NOTRELEVANT	=	"NR";
	public static final String DELIVERY_STATUS_OPEN			=	"OP";
	public static final String DELIVERY_STATUS_COMPLETED	=	"CP";
	public static final String DELIVERY_STATUS_DELETED		=	"DE";
	public static final String DELIVERY_STATUS_CANCELED		=	"CE";

	private final static Table table = Schema.findTable( Schema.DPR_ORDER_INFO_DTL );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ORDER_INFO_DTL );

	public OrderInfoDetail( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String orderNumber, String lineNumber ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "orderNumber", orderNumber );
		primaryMap.put( "lineNumber", lineNumber );

		return primaryMap;
	}

	public static Table getTable() {
		return table;
	}
}
