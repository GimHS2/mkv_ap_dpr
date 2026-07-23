/*
 *	File Name:	UserMultiEmployee.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/03/30		2.2.1	regex 오류 수정
 *	jbaek		2020/12/31		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 *
 */
public class UserMultiEmployee extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_USER_MULTIEMP );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_USER_MULTIEMP );

	public UserMultiEmployee( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String uniqId, String organizationCode, String employeeId ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "uniqId", uniqId );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "employeeId", employeeId );

		return primaryMap;
	}

	public List<String> getEmployeeIds( String uniqId, String organizationCode ) throws SQLException {
		Map<String, Object> conditionMap = new java.util.HashMap<String, Object> ();
		conditionMap.put( "uniqId", uniqId );
		conditionMap.put( "organizationCode", organizationCode );

		List<Map<String, Object>> records = this.getRecords( conditionMap, new String[] {"employeeId"});
		if( records != null && records.size() > 0 )
			return records.stream().collect(
					Collectors.mapping(map -> (String)map.get("employeeId")
					, Collectors.toList()));
		else
			return new java.util.ArrayList<>();
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
					Joinable tbl_MEMP = new JoinableImplBK( "MEMP", "DPR_USER_MULTIEMP",
							"MEMP.ORGANIZATIONCD(+) = CCND.ORGANIZATIONCD AND MEMP.COUNTRYCD(+) = CCND.COUNTRYCD AND MEMP.UNIQID(+) = ?", "uniqId" );

					append( new QueryableField[] {
							new QueryableFieldImpl( Schema.STRING, false, "uniqId", "MEMP.UNIQID", tbl_MEMP )
							, new QueryableFieldImpl( Schema.STRING, false, "employeeId"
									, "(LISTAGG(DISTINCT PEMP.EMPLOYEE_ID, '; ' ON OVERFLOW TRUNCATE '...') WITHIN GROUP(ORDER BY PEMP.EMPLOYEE_ID) OVER(PARTITION BY MEMP.UNIQID, MEMP.ORGANIZATIONCD))"
									, new JoinableImpl( "PEMP", "DPR_PARTY_EMPLOYEE", "PEMP.EMPLOYEE_ID(+) = MEMP.EMPLOYEE_ID AND PEMP.COUNTRYCD(+) = MEMP.COUNTRYCD", tbl_MEMP ) )
//							, new QueryableFieldImpl( Schema.STRING, false, "employeeId"
//									, "(LISTAGG(MEMP.EMPLOYEE_ID, '; ' ON OVERFLOW TRUNCATE '...') WITHIN GROUP(ORDER BY PEMP.EMPLOYEE_ID) OVER(PARTITION BY MEMP.UNIQID, MEMP.ORGANIZATIONCD))"
//									, tbl_MEMP )
					} );
				}
			} );

			conditionMap.put( Condition.DISTINCT_CONDITIONKEY, "Y" );

			QueryBuffer querybuf = _factory.setQuery( new ConditionQueryBuffer(conditionMap) );
			appendOrderBy( querybuf, _factory );

			return SQLManager.getRecordList( handler, querybuf );
		} else {
			return getRecords( conditionMap );
		}
	}

	public static String[] getEmployeeIdArr( Object employeeId ) {
		String employeeIdCsv = (String)employeeId;
		String[] employeeIdArr = null;
		if( employeeIdCsv != null && employeeIdCsv.contains(";") ) {
			employeeIdArr = employeeIdCsv.split("\\s?;\\s?");
		} else {
			employeeIdArr = new String[] { employeeIdCsv };
		}
		return employeeIdArr;
	}

	public boolean update( List<Map<String, Object>> records, Object uniqId, boolean inserting ) throws SQLException, DataException {
		String systemDateTime = (String)SQLManager.getObjectValue( handler,
				"SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM DUAL" );

		if( records != null && records.size() > 0 ) {
			for( Map<String, Object> map : records ) {
				String[] employeeIdArr = getEmployeeIdArr(map.get("employeeId"));
				for( String employeeId : employeeIdArr ) {
					Map<String, Object> newMap = new java.util.HashMap<>( map );
					newMap.put("employeeId", employeeId);
					SQLManager.manageRecord( handler, table, newMap, Record.UPDATE | Record.INSERT  );
				}
			}
		}

		if( !inserting ) {
			Object[] bindVars = new Object[] { uniqId, systemDateTime };
			String statement = "DELETE FROM DPR_USER_MULTIEMP WHERE UNIQID = ? AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";
			SQLManager.executeStatement( handler, statement, bindVars );
		}

		return true;
	}
}
