/*
 *	File Name:	UserSession.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										SQLManager 변경사항 적용: fieldMap 삭제
 *	stghr12		2007/09/04		2.1.1	com.irt.rbm.QueryableManagerImpl extends로 변경
 *	stghr12		2006/12/01		2.1.0	RBMDataManager.appendOrderBy 변경사항 반영
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	kdmcom		2002/12/20				create
 *
**/

package com.irt.rbm.usr;

import com.irt.data.*;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class UserSession extends com.irt.rbm.QueryableManagerImpl implements com.irt.data.SummaryQueryableManager {
	public final static String SESSIONSTATUS_NORMAL		= "00";
	public final static String SESSIONSTATUS_LOGOUT		= "LO";
	public final static String SESSIONSTATUS_KILL		= "LK";

	private final static QueryFactory factory = Schema.findQueryFactory( Schema.USER_SESSION_HIST );
	private final static QueryFactory factory_curr = Schema.findQueryFactory( Schema.USER_SESSION );
	private final static QueryFactory factory_summary = new QueryFactory( new UserSession.SummaryQuery() );

	public UserSession( SQLHandler handler ) {
		super( handler, factory, factory_summary );
	}

	public int getCurrentUserCount( Map<String, ? extends Object> conditionMap ) throws SQLException {
		QueryBuffer querybuf = factory_curr.setCountQuery( new ConditionQueryBuffer(conditionMap) );
		return SQLManager.getInt( handler, querybuf );
	}

	public List<Map<String, Object>> getCurrentUsers( Map<String, ? extends Object> conditionMap ) throws SQLException {
		return getCurrentUsers( conditionMap, 0, -1 );
	}

	public List<Map<String, Object>> getCurrentUsers( Map<String, ? extends Object> conditionMap, int skipRows, int maxRows ) throws SQLException {
		QueryBuffer querybuf = factory_curr.setQuery( new ConditionQueryBuffer(conditionMap) );
		appendOrderBy( querybuf, factory_curr );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	public List<Map<String, Object>> getCurrentUsers( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException {
		return getCurrentUsers( conditionMap, fieldKeys, 0, -1 );
	}

	public List<Map<String, Object>> getCurrentUsers( Map<String, ? extends Object> conditionMap, String[] fieldKeys, int skipRows, int maxRows )
						throws SQLException {
		QueryBuffer querybuf = factory_curr.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		appendOrderBy( querybuf, factory_curr );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	public boolean killSession( String sessionId ) throws DataException, SQLException {
		return( SQLManager.executeStatement( handler, "UPDATE USR_SESSION SET STATUS = ? WHERE SESSIONID = ?", SESSIONSTATUS_KILL, sessionId ) > 0 );
	}

	/**
	 *
	 */
	private static class SummaryQuery extends com.irt.sql.QueryableImpl {
		static NestedJoinable summary = new NestedJoinable("USH") {
			{
				initNestedFields( new NestedJoinable.Field[] {
					  new NestedJoinable.Field( "RECORD_CNT", "COUNT(*)" )
					, new NestedJoinable.Field( "USER_CNT", "COUNT(DISTINCT USH.UNIQID)" )
					, new NestedJoinable.Field( "IP_CNT", "COUNT(DISTINCT USH.IP)" )
					, new NestedJoinable.Field( "ACC_TIME", "SUM(USH.LASTACC_TIME - USH.LOGIN_TIME) * 24 * 60" )
					, new NestedJoinable.Field( "ACC_CNT", "SUM(USH.ACCCOUNT)" )
				} );
			}

			public QueryBuffer getInnerTableQueryBuffer( Map<String, ? extends Object> conditionMap ) {
				ConditionQueryBuffer inner_querybuf = new ConditionQueryBuffer( conditionMap );

				int groupKeyCount = 0;
				Object[] groupKeys = inner_querybuf.getConditionValues( Condition.GROUPING_CONDITIONKEY );
				for( int i = 0; groupKeys != null && i < groupKeys.length; i++ ) {
					groupKeyCount++;
					if( "accessDate".equals(groupKeys[i]) )
						inner_querybuf.appendDataWithGroupBy( "TRUNC(USH.LOGIN_TIME)", "ACC_DATE" );
					else if( "accessWeek".equals(groupKeys[i]) )
						inner_querybuf.appendDataWithGroupBy( "pkSYSDate.fGetWeek(USH.LOGIN_TIME)", "ACC_WEEK" );
					else if( "accessMonth".equals(groupKeys[i]) )
						inner_querybuf.appendDataWithGroupBy( "TO_CHAR(USH.LOGIN_TIME, 'YYYYMM')", "ACC_MONTH" );
					else if( "ip".equals(groupKeys[i]) )
						inner_querybuf.appendDataWithGroupBy( "USH.IP" );
					else if( "partyId".equals(groupKeys[i]) )
						inner_querybuf.appendDataWithGroupBy( "USH.PARTYID" );
					else if( "userId".equals(groupKeys[i]) ) {
						inner_querybuf.appendDataWithGroupBy( "USH.PARTYID" );
						inner_querybuf.appendDataWithGroupBy( "USH.USERID" );
					} else
						groupKeyCount--;
				}
				if( groupKeyCount == 0 ) inner_querybuf.appendDataWithAlias( "'x'", "DUMMYCODE" );
				inner_querybuf.findConditionDate( "accessDate", "USH.LOGIN_TIME" );

				factory.setConditionQuery( inner_querybuf );

				return inner_querybuf;
			}
		};

		SummaryQuery() {
			super( summary );

			QueryBufferValid querybufValid;

			append( new QueryableField[] {
				  new QueryableFieldImpl( Schema.INTEGER, false, "loginCount", "USH.RECORD_CNT", summary.getJoinable("RECORD_CNT") )
				, new QueryableFieldImpl( Schema.INTEGER, false, "userCount", "USH.USER_CNT", summary.getJoinable("USER_CNT") )
				, new QueryableFieldImpl( Schema.INTEGER, false, "ipCount", "USH.IP_CNT", summary.getJoinable("IP_CNT") )
				, new QueryableFieldImpl( Schema.INTEGER, false, "accessTime", "USH.ACC_TIME", summary.getJoinable("ACC_TIME") )
				, new QueryableFieldImpl( Schema.INTEGER, false, "accessTimeHour", "TRUNC(USH.ACC_TIME / 60)", summary.getJoinable("ACC_TIME") )
				, new QueryableFieldImpl( Schema.INTEGER, false, "accessTimeMinute", "MOD(USH.ACC_TIME, 60)", summary.getJoinable("ACC_TIME") )
				, new QueryableFieldImpl( Schema.INTEGER, false, "accessCount", "USH.ACC_CNT", summary.getJoinable("ACC_CNT") )
			} );

			// accessDate, accessWeek, accessMonth, ip
			appendCND( new QueryBufferValid.GroupKey("ip"), new QueryableFieldImpl( Schema.DESC, false, "ip", "USH.IP" ) );
			appendCND( new QueryBufferValid.GroupKey("accessDate"), new QueryableFieldImpl( Schema.DATE, false, "accessDate", "USH.ACC_DATE" ) );
			appendCND( new QueryBufferValid.GroupKey("accessWeek"), new QueryableFieldImpl( Schema.STRING, false, "accessWeek", "USH.ACC_WEEK" ) );
			appendCND( new QueryBufferValid.GroupKey("accessMonth"), new QueryableFieldImpl( Schema.STRING, false, "accessMonth", "USH.ACC_MONTH" ) );

			// partyId, partyName
			querybufValid = new QueryBufferValid.GroupKey( "partyId", "userId" );
			Joinable tbl_UPT = new JoinableImpl( "UPT", "USR_PARTY", "UPT.PARTY_ID(+) = USH.PARTYID" );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.STRING, false, "partyId", "USH.PARTYID" ) );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.DESC, false, "partyName", "UPT.PARTY_NAME", tbl_UPT ) );

			// userId, userName
			querybufValid = new QueryBufferValid.GroupKey( "userId" );
			Joinable tbl_USR = new JoinableImpl( "USR", "USR_USER", "USR.PARTYID(+) = USH.PARTYID AND USR.USER_ID(+) = USH.USERID" );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.STRING, false, "userId", "USH.USERID" ) );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.DESC, false, "userName", "USR.USER_NAME", tbl_USR ) );
		}

		public boolean appendTable( QueryBuffer querybuf ) {
			if( super.appendTable(querybuf) ) {
				if( querybuf instanceof ConditionQueryBuffer ) {
					ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;

					if( condquerybuf.isConditionTrue("isAllUser") && Condition.containsGroupKey(condquerybuf.getConditionMap(), "userId") ) {
						querybuf.appendTableWithAlias( "USR_USER", "USR", "USR.PARTYID = USH.PARTYID(+) AND USR.USER_ID = USH.USERID(+)" );
						querybuf.appendDataWithAlias( "USR.PARTYID", "partyId" );
						querybuf.appendDataWithAlias( "USR.USER_ID", "userId" );
					}
				}

				return true;
			}

			return false;
		}
	}
}
