/*
 *	File Name:	UserSessionAccessLog.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/09/04		2.1.0	create
 *
**/

package com.irt.rbm.usr;

import com.irt.data.*;
import com.irt.sql.*;
import java.util.Map;

/**
 *
 */
public class UserSessionAccessLog extends com.irt.rbm.QueryableManagerImpl implements com.irt.data.SummaryQueryableManager {
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.UESR_SESSION_ACCESSLOG );
	private final static QueryFactory factory_summary = new QueryFactory( new UserSessionAccessLog.SummaryQuery() );

	public UserSessionAccessLog( SQLHandler handler ) {
		super( handler, factory, factory_summary );
	}

	/**
	 *
	 */
	private static class SummaryQuery extends com.irt.sql.QueryableImpl {
		static NestedJoinable summary = new NestedJoinable("USA") {
			{
				initNestedFields( new NestedJoinable.Field[] {
					  new NestedJoinable.Field( "RECORD_CNT", "COUNT(*)" )
					, new NestedJoinable.Field( "USER_CNT", "COUNT(DISTINCT USA.UNIQID)" )
				} );
			}

			public QueryBuffer getInnerTableQueryBuffer( Map<String, ? extends Object> conditionMap ) {
				ConditionQueryBuffer inner_querybuf = new ConditionQueryBuffer( conditionMap );

				int groupKeyCount = 0;
				Object[] groupKeys = inner_querybuf.getConditionValues( Condition.GROUPING_CONDITIONKEY );
				for( int i = 0; groupKeys != null && i < groupKeys.length; i++ ) {
					groupKeyCount++;
					if( "accessDate".equals(groupKeys[i]) )
						inner_querybuf.appendDataWithGroupBy( "TRUNC(USA.ACCESSTIME)", "ACC_DATE" );
					else if( "accessWeek".equals(groupKeys[i]) )
						inner_querybuf.appendDataWithGroupBy( "pkSYSDate.fGetWeek(USA.ACCESSTIME)", "ACC_WEEK" );
					else if( "accessMonth".equals(groupKeys[i]) )
						inner_querybuf.appendDataWithGroupBy( "TO_CHAR(USA.ACCESSTIME, 'YYYYMM')", "ACC_MONTH" );
					else if( "title".equals(groupKeys[i]) )
						inner_querybuf.appendDataWithGroupBy( "USA.TITLE" );
					else if( "partyId".equals(groupKeys[i]) )
						inner_querybuf.appendDataWithGroupBy( "USA.PARTYID" );
					else if( "userId".equals(groupKeys[i]) ) {
						inner_querybuf.appendDataWithGroupBy( "USA.PARTYID" );
						inner_querybuf.appendDataWithGroupBy( "USA.USERID" );
					} else
						groupKeyCount--;
				}
				if( groupKeyCount == 0 ) inner_querybuf.appendDataWithAlias( "'x'", "DUMMYCODE" );
				inner_querybuf.appendCondition( "USA.REQUESTMODE <> 'cnt'" );
				inner_querybuf.findConditionDate( "accessDate", "USA.ACCESSTIME" );

				factory.setConditionQuery( inner_querybuf );

				return inner_querybuf;
			}
		};

		SummaryQuery() {
			super( summary );

			QueryBufferValid querybufValid;

			append( new QueryableField[] {
				  new QueryableFieldImpl( Schema.INTEGER, false, "accessCount", "USA.RECORD_CNT", summary.getJoinable("RECORD_CNT") )
				, new QueryableFieldImpl( Schema.INTEGER, false, "userCount", "USA.USER_CNT", summary.getJoinable("USER_CNT") )
			} );

			// accessDate, accessWeek, accessMonth, title
			appendCND( new QueryBufferValid.GroupKey("title"), new QueryableFieldImpl( Schema.DESC, false, "title", "USA.TITLE" ) );
			appendCND( new QueryBufferValid.GroupKey("accessDate"), new QueryableFieldImpl( Schema.DATE, false, "accessDate", "USA.ACC_DATE" ) );
			appendCND( new QueryBufferValid.GroupKey("accessWeek"), new QueryableFieldImpl( Schema.STRING, false, "accessWeek", "USA.ACC_WEEK" ) );
			appendCND( new QueryBufferValid.GroupKey("accessMonth"), new QueryableFieldImpl( Schema.STRING, false, "accessMonth", "USA.ACC_MONTH" ) );

			// partyId, partyName
			querybufValid = new QueryBufferValid.GroupKey( new String[] { "partyId", "userId" } );
			Joinable tbl_UPT = new JoinableImpl( "UPT", "USR_PARTY", "UPT.PARTY_ID(+) = USA.PARTYID" );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.STRING, false, "partyId", "USA.PARTYID" ) );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.DESC, false, "partyName", "UPT.PARTY_NAME", tbl_UPT ) );

			// userId, userName
			querybufValid = new QueryBufferValid.GroupKey( "userId" );
			Joinable tbl_USR = new JoinableImpl( "USR", "USR_USER", "USR.PARTYID(+) = USA.PARTYID AND USR.USER_ID(+) = USA.USERID" );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.STRING, false, "userId", "USA.USERID" ) );
			appendCND( querybufValid, new QueryableFieldImpl( Schema.DESC, false, "userName", "USR.USER_NAME", tbl_USR ) );
		}
	}
}
