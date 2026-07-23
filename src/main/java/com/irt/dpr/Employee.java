/*
 *	File Name:	Employee.java
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
public class Employee extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_EMPLOYEE );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_EMPLOYEE );

	public Employee( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String employeeId, String languageCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "employeeId", employeeId );
		primaryMap.put( "languageCode", languageCode );

		return primaryMap;
	}

	public boolean existingEmployeeId( String employeeId ) throws SQLException {
		return ( SQLManager.getInt(handler, "SELECT COUNT(*) FROM DPR_EMPLOYEE WHERE EMPLOYEE_ID = ?", employeeId) > 0 );
	}
}
