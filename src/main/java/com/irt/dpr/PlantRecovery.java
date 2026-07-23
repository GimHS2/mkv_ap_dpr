/*
+	*	File Name:	PlantRecovery.java
+	*	Version:	2.0.0
+	*
+	*	Description:
+	*
+	*	Note:
+	*
+	*	Modified	(YYYY/MM/DD)	Ver		Content
+	*	song7981	2015/02/29		2.0.0		create
+**/

package com.irt.dpr;

import java.sql.PreparedStatement;
import java.util.Map;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.sql.*;

/*
 *
 */
public class PlantRecovery extends ManipulableManagerImpl{
	private final static Table table = Schema.findTable( Schema.DPR_PLANT_RCV );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_PLANT_RCV );

	public PlantRecovery ( SQLHandler handler ) {
		super ( handler, table, factory );
	}

	PreparedStatement pstmt = null;

	public static Map<String, Object> createPrimary( String organizationCode, String plantCode, String itemCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "plantCode", plantCode );
		primaryMap.put( "itemCode", itemCode );

		return primaryMap;
	}
}