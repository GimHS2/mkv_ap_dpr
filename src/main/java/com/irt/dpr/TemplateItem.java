/*
 *	File Name:	TemplateItem.java
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

import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

/*
 *
 */
public class TemplateItem extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_ORDER_TEMPLATE_DTL );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_ORDER_TEMPLATE_DTL );

	public TemplateItem( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String templatekey, String lineNumber ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();

		primaryMap.put( "templatekey", templatekey );
		primaryMap.put( "lineNumber", lineNumber );

		return primaryMap;
	}

	public String getName(String templateKey, String lineNumber) throws SQLException {
		return (String) SQLManager.getObjectValue(handler,
				"SELECT ITEMCD FROM DPR_TEMPLATE_DTL WHERE TEMPLATEKEY = ? AND LINE_NO = ?",
				new Object[] { templateKey, lineNumber });
	}

}
