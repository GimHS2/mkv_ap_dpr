/*
	*	File Name:	Plant.java
	*	Version:	2.2.0
	*
	*	Description:
	*
	*	Note:
	*
	*	Modified	(YYYY/MM/DD)	Ver		Content
	*	jbaek		2014/02/16		2.2.0		create
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
public class Plant extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_PLANT );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_PLANT );

	public Plant( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String plantCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "plantCode", plantCode );
		return primaryMap;
	}
}
