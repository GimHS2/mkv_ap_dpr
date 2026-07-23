/*
 *	File Name:	UserEmployee.java
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
public class UserEmployee extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_USER_EMPLOYEE );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_USER_EMPLOYEE );

	public UserEmployee( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String uniqId, String organizationCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "uniqId", uniqId );
		primaryMap.put( "organizationCode", organizationCode );

		return primaryMap;
	}

	public String getEmployeeId( String uniqId,  String organizationCode ) throws SQLException {
		return (String)getFieldValue( UserEmployee.createPrimary(uniqId, organizationCode), "employeeId" );
	}

	public List<Map<String, Object>> getRecords( String uniqId, String countryCode, boolean baseOrganization ) throws SQLException {
		Map<String, Object> conditionMap = Record.createMap( "uniqId", uniqId );
		conditionMap.put( "countryCode", countryCode );

		return getRecords( conditionMap, baseOrganization );
	}

	public List<Map<String, Object>> getRecords( Map<String, Object> conditionMap, boolean baseOrganization ) throws SQLException {
		if( baseOrganization ) {
			QueryFactory _factory = new QueryFactory( new QueryableImpl(Schema.findQueryable(Schema.DPR_COUNTRY_COND)) {
				{
					Joinable tbl_UEMP = new JoinableImplBK( "UEMP", "DPR_USER_EMPLOYEE",
							"UEMP.ORGANIZATIONCD(+) = CCND.ORGANIZATIONCD AND UEMP.COUNTRYCD(+) = CCND.COUNTRYCD AND UEMP.UNIQID(+) = ?", "uniqId" );

					append( new QueryableField[] {
							new QueryableFieldImpl( Schema.STRING, false, "uniqId", "UEMP.UNIQID", tbl_UEMP )
							, new QueryableFieldImpl( Schema.STRING, false, "employeeId", "UEMP.EMPLOYEE_ID", tbl_UEMP )
					} );
				}
			} );

			QueryBuffer querybuf = _factory.setQuery( new ConditionQueryBuffer(conditionMap) );
			appendOrderBy( querybuf, _factory );

			return SQLManager.getRecordList( handler, querybuf );
		} else {
			return getRecords( conditionMap );
		}
	}

	public boolean update( List<Map<String, Object>> records, Object uniqId, boolean inserting ) throws SQLException, DataException {
		String systemDateTime = (String)SQLManager.getObjectValue( handler,
				"SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM DUAL" );

		if( records != null && records.size() > 0 ) {
			for( Map<String, Object> map : records ) {
				boolean ret = SQLManager.manageRecord( handler, table, map, Record.UPDATE | Record.INSERT );
			}
		}

		if( !inserting ) {
			Object[] bindVars = new Object[] { uniqId, systemDateTime };
			String statement = "DELETE FROM DPR_USER_EMPLOYEE WHERE UNIQID = ? AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";
			SQLManager.executeStatement( handler, statement, bindVars );
		}

		return true;
	}
}
