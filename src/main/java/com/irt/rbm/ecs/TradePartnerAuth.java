/*
 *	File Name:	TradePartnerAuth.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	getAuthKeys() 추가
 *										getAuthValueQuery(): authValue1/authValue2 Query 오류수정
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TradePartnerAuth extends com.irt.rbm.ManipulableManagerImpl {
	private final static Queryable queryable_user = new QueryableImpl( com.irt.rbm.usr.Schema.findQueryable(com.irt.rbm.usr.Schema.USER_USER) ) {
		{
			append( new QueryableFieldImpl( Schema.STRING, "authUserId", "NVL(TPA.USERID, TPA_P.USERID)", "ECS_TPAUTH_USERID" ) );
			append( Schema.makeTPAuthQueryableFields( false, null ) );
		}

		public boolean appendTable( QueryBuffer querybuf ) {
			Joinable tbl_TPA;
			Joinable tbl_TPA_P;

			if( querybuf instanceof ConditionQueryBuffer ) {
				ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;

				QueryBuffer inner_querybuf2 = new QueryBuffer();
				inner_querybuf2.append( "? BUYERGLN, ? SELLERGLN, USR.*" );
				inner_querybuf2.appendTableWithAlias( "USR_USER", "USR" );
				inner_querybuf2.addBindVariable( QueryBuffer.DATA_BINDVAR, condquerybuf.getConditionValue("buyerGln", Schema.STRING) );
				inner_querybuf2.addBindVariable( QueryBuffer.DATA_BINDVAR, condquerybuf.getConditionValue("sellerGln", Schema.STRING) );

				QueryBuffer inner_querybuf = new QueryBuffer();
				inner_querybuf.append( "TP.PARENT_BUYERGLN, TP.PARENT_SELLERGLN, USR.*" );
				inner_querybuf.appendTable( inner_querybuf2, "USR" );
				inner_querybuf.appendTableWithAlias( "ECS_TRADEPARTNER", "TP", "TP.BUYERGLN(+) = USR.BUYERGLN AND TP.SELLERGLN(+) = USR.SELLERGLN" );

				if( !querybuf.appendTable(inner_querybuf, "USR") ) return false;
				tbl_TPA = new JoinableImpl( "TPA", "ECS_TRADEPARTNER_AUTH"
						, "TPA.BUYERGLN(+) = USR.BUYERGLN AND TPA.SELLERGLN(+) = USR.SELLERGLN AND TPA.USERID(+) = USR.UNIQID" );
				tbl_TPA_P = new JoinableImpl( "TPA_P", "ECS_TRADEPARTNER_AUTH"
						, "TPA_P.BUYERGLN(+) = USR.PARENT_BUYERGLN AND TPA_P.SELLERGLN(+) = USR.PARENT_SELLERGLN AND TPA_P.USERID(+) = USR.UNIQID" );
			} else {
				if( !super.appendTable(querybuf) ) return false;
				tbl_TPA = new JoinableImpl( "TPA", "ECS_TRADEPARTNER_AUTH"
						, "TPA.BUYERGLN(+) = NULL AND TPA.SELLERGLN(+) = NULL AND TPA.USERID(+) = USR.UNIQID" );
				tbl_TPA_P = new JoinableImpl( "TPA_P", "ECS_TRADEPARTNER_AUTH"
						, "TPA_P.BUYERGLN(+) = NULL AND TPA_P.SELLERGLN(+) = NULL AND TPA_P.USERID(+) = USR.UNIQID" );
			}

			tbl_TPA.appendTable( querybuf );
			tbl_TPA_P.appendTable( querybuf );

			return true;
		}
	};
	private final static Table table = Schema.findTable( Schema.ECS_TRADEPARTNER_AUTH );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_TRADEPARTNER_AUTH );
	private final static QueryFactory factory_user = new QueryFactory( queryable_user );

	public final static char TP							= 'T';		// TPA, TPA_P 모두 사용
	public final static char PARENTTP					= 'P';		// TPA_P 사용(BUYERGLN/SELLERGLN으로 JOIN)

	public final static int AUTHLEVEL_MANAGE			= 3;
	public final static int AUTHLEVEL_VIEW				= 2;
	public final static int AUTHLEVEL_NOAUTH			= 1;
	public final static int AUTHLEVEL_NODATA			= 0;

	/* Caution: authType은 반드시 0x00 ~ 0x2F사이로 설정해야 함. */
	public final static int AUTH1						= 0x10;
	public final static int AUTH1TP						= 0x11;
	public final static int AUTH1OPERDAY				= 0x12;
	public final static int AUTH1ITEM					= 0x13;
	public final static int AUTH2						= 0x20;
	public final static int AUTH2SCENARIO				= 0x21;
	public final static int AUTH2ITEM					= 0x22;
	public final static int AUTH2SFC					= 0x23;
	public final static int AUTH2OFC					= 0x24;
	public final static int AUTH2EVENT					= 0x25;
	public final static int AUTH2INVENTORY				= 0x26;
	public final static int AUTH2ORDER					= 0x27;
	public final static int AUTH2ORDERMANUAL			= 0x28;
	public final static int AUTH2KPI					= 0x29;

	public TradePartnerAuth( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String buyerGln, String sellerGln, String authUserId ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "buyerGln", buyerGln );
		primaryMap.put( "sellerGln", sellerGln );
		primaryMap.put( "authUserId", authUserId );

		return primaryMap;
	}

	public static boolean appendAuthTableQuery( ConditionQueryBuffer querybuf, String tableAlias, char optionTP ) {
		Object authUserId = querybuf.getConditionValue( "authUserId", Schema.STRING );
		if( authUserId == null ) return false;

		if( querybuf.appendTableWithAlias("ECS_TRADEPARTNER_AUTH", "TPA_P") ) {
			if( optionTP == PARENTTP )
				querybuf.appendCondition( "TPA_P.BUYERGLN(+) = "+ tableAlias +".BUYERGLN AND TPA_P.SELLERGLN(+) = "+ tableAlias +".SELLERGLN" );
			else {
				querybuf.appendCondition( "TPA_P.BUYERGLN(+) = "+ tableAlias +".PARENT_BUYERGLN" );
				querybuf.appendCondition( "TPA_P.SELLERGLN(+) = "+ tableAlias +".PARENT_SELLERGLN" );
			}
			querybuf.appendCondition( "TPA_P.USERID(+) = ?", authUserId );
		}
		if( optionTP == TP && querybuf.appendTableWithAlias("ECS_TRADEPARTNER_AUTH", "TPA") ) {
			querybuf.appendCondition( "TPA.BUYERGLN(+) = "+ tableAlias +".BUYERGLN AND TPA.SELLERGLN(+) = "+ tableAlias +".SELLERGLN" );
			querybuf.appendCondition( "TPA.USERID(+) = ?", authUserId );
		}

		return true;
	}

	public static boolean appendAuthQuery( ConditionQueryBuffer querybuf, String tableAlias, char optionTP, int authType, int authLevel ) {
		if( authLevel >= AUTHLEVEL_VIEW && appendAuthTableQuery(querybuf, tableAlias, optionTP) ) {
			if( authLevel >= AUTHLEVEL_MANAGE )
				querybuf.appendConditionByField( getAuthValueQuery(optionTP, authType), "M" );
			else
				querybuf.appendConditionByField( getAuthValueQuery(optionTP, authType), new String[] { "V", "M" } );

			return true;
		} else
			return false;
	}

	public static String[] getAuthKeys() {
		return new String[] {
			"authValue1"
			, "authValue1TP", "authValue1Operday", "authValue1Item"
			, "authValue2"
			, "authValue2Scenario", "authValue2Item", "authValue2SFC", "authValue2OFC", "authValue2Event"
			, "authValue2Inventory", "authValue2Order", "authValue2OrderManual", "authValue2KPI"
		};
	}

	public static int getAuthLevel( String authValue, int authType ) {
		int index = ( authType & 0x0F );
		if( authValue == null || authValue.length() == 0 )
			return AUTHLEVEL_NOAUTH;
		else if( index == 0 )
			return AUTHLEVEL_VIEW;
		else if( authValue.length() < index )
			return AUTHLEVEL_NOAUTH;
		else
			return "_XVM".indexOf( authValue.charAt(index - 1) );
	}

	public int getAuthLevel( String buyerGln, String sellerGln, String authUserId, int authType ) throws SQLException {
		return "_XVM".indexOf( getAuthValue(buyerGln, sellerGln, authUserId, authType) );
	}

	public int getAuthUserCount( Map<String, ? extends Object> conditionMap ) throws SQLException {
		QueryBuffer querybuf = factory_user.setCountQuery( new ConditionQueryBuffer(conditionMap) );
		return SQLManager.getInt( handler, querybuf );
	}

	public List<Map<String, Object>> getAuthUsers( Map<String, ? extends Object> conditionMap ) throws SQLException {
		return getAuthUsers( conditionMap, 0, -1 );
	}

	public List<Map<String, Object>> getAuthUsers( Map<String, ? extends Object> conditionMap, int skipRows, int maxRows ) throws SQLException {
		QueryBuffer querybuf = factory_user.setQuery( new ConditionQueryBuffer(conditionMap) );
		appendOrderBy( querybuf, factory_user );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	public List<Map<String, Object>> getAuthUsers( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException {
		return getAuthUsers( conditionMap, fieldKeys, 0, -1 );
	}

	public List<Map<String, Object>> getAuthUsers( Map<String, ? extends Object> conditionMap, String[] fieldKeys, int skipRows, int maxRows )
						throws SQLException {
		QueryBuffer querybuf = factory_user.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		appendOrderBy( querybuf, factory_user );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	public char getAuthValue( String buyerGln, String sellerGln, String authUserId, int authType ) throws SQLException {
		QueryBuffer querybuf = new QueryBuffer();

		querybuf.appendData( getAuthValueQuery(TP, authType) );
		querybuf.appendTableWithAlias( "ECS_TRADEPARTNER", "TP" );
		querybuf.appendTableWithAlias( "ECS_TRADEPARTNER_AUTH", "TPA" );
		querybuf.appendTableWithAlias( "ECS_TRADEPARTNER_AUTH", "TPA_P" );
		querybuf.appendCondition( "TP.BUYERGLN = ? AND TP.SELLERGLN = ?", new Object[] { buyerGln, sellerGln } );
		querybuf.appendCondition( "TPA.BUYERGLN(+) = TP.BUYERGLN AND TPA.SELLERGLN(+) = TP.SELLERGLN AND TPA.USERID(+) = ?", authUserId );
		querybuf.appendCondition( "TPA_P.BUYERGLN(+) = TP.PARENT_BUYERGLN AND TPA_P.SELLERGLN(+) = TP.PARENT_SELLERGLN" );
		querybuf.appendCondition( "TPA_P.USERID(+) = ?", authUserId );

		String authValue = (String)SQLManager.getObjectValue( handler, querybuf );

		return( authValue == null ? 'X' : authValue.charAt(0) );
	}

	private static String getAuthValueQuery( char optionTP, int authType ) {
		String authValue1, authValue2;

		if( optionTP == TP ) {
			authValue1 = "NVL(TPA.AUTHVALUE1, DECODE(TPA_P.MNGTYPE, 'A', TPA_P.AUTHVALUE1, 'P', NVL2(TPA.MNGTYPE, TPA_P.AUTHVALUE1, NULL), NULL))";
			authValue2 = "NVL(TPA.AUTHVALUE2, DECODE(TPA_P.MNGTYPE, 'A', TPA_P.AUTHVALUE2, 'P', NVL2(TPA.MNGTYPE, TPA_P.AUTHVALUE2, NULL), NULL))";
		} else {
			authValue1 = "TPA_P.AUTHVALUE1";
			authValue2 = "TPA_P.AUTHVALUE2";
		}

		switch( authType & 0xF0 ) {
		case AUTH1:
			if( authType == AUTH1 )
				return "NVL2("+ authValue1 +", 'V', 'X')";
			else
				return "SUBSTRB("+ authValue1 +", "+ (authType & 0x0F) +", 1)";
		case AUTH2:
			if( authType == AUTH1 )
				return "NVL2("+ authValue2 +", 'V', 'X')";
			else
				return "SUBSTRB("+ authValue2 +", "+ (authType & 0x0F) +", 1)";
		default:
			throw new IllegalArgumentException( "illegal authType '"+ authType +"'" );
		}
	}
}
