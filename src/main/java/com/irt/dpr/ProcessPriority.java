/*
 *	File Name:	ProcessPriority.java
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
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.List;

/**
 *
 */
public class ProcessPriority extends com.irt.rbm.RBMDataManager {
	public final static String PROCESSTYPE_MASTER			= "MST";
	public final static String PROCESSTYPE_SSL				= "SSL";
	public final static String PROCESSTYPE_ORDERPROCESS		= "ORD";
	public final static String PROCESSTYPE_INV				= "INV";
	public final static String PROCESSTYPE_SELLOUT			= "SEL";
	public final static String PROCESSTYPE_CUSTOMERMST		= "CMT";
	public final static String PROCESSTYPE_CUSTOMERID		= "CID";
	public final static String PROCESSTYPE_SKUMAPPING		= "SKU";

	private final static int WAITING_LOOP					= 3;  // loop num
	private final static int WAITING_INTERVAL				= 3;  // second
	Map<String, String[]> processCond;

	public ProcessPriority( SQLHandler handler ) {
		super( handler );

		processCond = new java.util.HashMap<String, String[]>();

		processCond.put( PROCESSTYPE_MASTER, new String[] { PROCESSTYPE_MASTER, PROCESSTYPE_SSL } );
		processCond.put( PROCESSTYPE_SSL, new String[] { PROCESSTYPE_SSL, PROCESSTYPE_MASTER, PROCESSTYPE_ORDERPROCESS } );
		processCond.put( PROCESSTYPE_ORDERPROCESS, new String[] { PROCESSTYPE_SSL } );
		processCond.put( PROCESSTYPE_INV, new String[] { PROCESSTYPE_SKUMAPPING } );
		processCond.put( PROCESSTYPE_SELLOUT, new String[] { PROCESSTYPE_CUSTOMERMST, PROCESSTYPE_CUSTOMERID, PROCESSTYPE_SKUMAPPING } );
		processCond.put( PROCESSTYPE_CUSTOMERMST, new String[] { PROCESSTYPE_SELLOUT, PROCESSTYPE_CUSTOMERID } );
		processCond.put( PROCESSTYPE_CUSTOMERID, new String[] { PROCESSTYPE_SELLOUT, PROCESSTYPE_CUSTOMERMST } );
		processCond.put( PROCESSTYPE_SKUMAPPING, new String[] { PROCESSTYPE_SELLOUT } );
	}

	/**
	  * Record가 없는 상태에서 두개의 프로세스가 요청 하면, 두개의 프로세스가 돌아 갈 수 있음.
	**/
	public int getCount( ResultSet rset ) throws SQLException {
		int cnt = 0;
		while( rset.next() ) { cnt++; }

		return cnt;
	}

	public boolean getPriority( String countryCode, String processType )
						throws IllegalArgumentException, SQLException  {

		String[] typeCondition = (String[])processCond.get(processType);
		if( typeCondition == null ) return false;

		String query = "SELECT * FROM DPR_LOCK WHERE COUNTRYCD = ? AND TYPE IN (";
		for( int i = 0; i < typeCondition.length; i++ )
			query += "?,";
		query = query.substring( 0, query.length() - 1 );
		query += ") FOR UPDATE WAIT " + WAITING_INTERVAL;
		
		// check exist record
		PreparedStatement pstmt_cnt = handler.getConnection().prepareStatement( query );
		ResultSet rset_cnt = null;
		int count = 0;
		try {
			pstmt_cnt.setObject( 1, countryCode );
			for( int i = 0; i < typeCondition.length; i++ )
				pstmt_cnt.setObject( i + 2, typeCondition[i] );

			rset_cnt = pstmt_cnt.executeQuery();
			count = (rset_cnt.next() ? rset_cnt.getInt(1) : 0 );
		} finally {
			try { rset_cnt.close(); } catch( Exception ex ) {}
			try { pstmt_cnt.close(); } catch( Exception ex ) {}
		}
		
		// Master RD insert
		String status = "00";
		if( count > 0 && PROCESSTYPE_MASTER.equals(processType) ) {
			pstmt_cnt = handler.getConnection().prepareStatement(
					"SELECT * FROM DPR_LOCK WHERE COUNTRYCD = ? AND TYPE = ? AND STATUS = 'RD'" );

			int readyCount = 0;
			try {
				pstmt_cnt.setObject( 1, countryCode );
				pstmt_cnt.setObject( 2, processType );

				rset_cnt = pstmt_cnt.executeQuery();
				readyCount = getCount( rset_cnt );
			} finally {
				try { rset_cnt.close(); } catch( Exception ex ) {}
				try { pstmt_cnt.close(); } catch( Exception ex ) {}
			}

			if( readyCount != 0 ) {
				handler.rollback();
				return false;
			} else {
				status = "RD";
			}
		} else if( count != 0 ) {
			handler.rollback();
			return false;
		}

		// insert ProcessPriority
		PreparedStatement pstmt_mng = null;
		int ret = 0;
		try {
			pstmt_mng = handler.getConnection().prepareStatement(
					"INSERT INTO DPR_LOCK( COUNTRYCD, TYPE, STARTDATETIME, STATUS )"
						+ " VALUES( ?, ?, SYSDATE, ? )" );

			pstmt_mng.setObject( 1, countryCode );
			pstmt_mng.setObject( 2, processType );
			pstmt_mng.setObject( 3, status );

			ret = pstmt_mng.executeUpdate();
		} finally {
			try { pstmt_mng.close(); } catch( Exception ex ) {}
		}
		handler.commit();

		// Master RD -> 00으로 변환
		if( ret > 0 && PROCESSTYPE_MASTER.equals(processType) && "RD".equals(status) ) {
			try {
				pstmt_cnt = handler.getConnection().prepareStatement( query.substring( 0, query.indexOf("FOR") )
						 + "AND STATUS = ? FOR UPDATE WAIT " + WAITING_INTERVAL );

				pstmt_cnt.setObject( 1, countryCode );
				for( int i = 0; i < typeCondition.length; i++ )
					pstmt_cnt.setObject( i + 2, typeCondition[i] );
				pstmt_cnt.setObject( typeCondition.length + 2, "00" );

				rset_cnt = pstmt_cnt.executeQuery();

				if( getCount(rset_cnt) == 0 ) {
					pstmt_mng = handler.getConnection().prepareStatement(
						"UPDATE DPR_LOCK SET STATUS = '00', STARTDATETIME = SYSDATE WHERE COUNTRYCD = ? AND TYPE = ? AND STATUS = 'RD'" );
					pstmt_mng.setObject( 1, countryCode );
					pstmt_mng.setObject( 2, processType );

					int ret_upd = pstmt_mng.executeUpdate();
					if( ret_upd > 0 ) {
						handler.commit();
						return true;
					} else {
						/* RD를 -> 00 으로 바꾸지 못한 경우 RD 레코드 삭제후 return false */
						PreparedStatement pstmt_del = null;
						try {
							handler.getConnection().prepareStatement( "DELETE FROM DPR_LOCK"
									+ " WHERE COUNTRYCD = ? AND TYPE = ? AND STATUS = 'RD'" );

							pstmt_del.setObject( 1, countryCode );
							pstmt_del.setObject( 2, processType );

							pstmt_del.executeUpdate();
							handler.commit();

							return false;
						} finally {
							try { pstmt_del.close(); } catch( Exception ex ) {};
						}
					}
				}
			} finally {
				try { rset_cnt.close(); } catch( Exception ex ) {}
				try { pstmt_cnt.close(); } catch( Exception ex ) {}
				try { pstmt_mng.close(); } catch( Exception ex ) {}
			}
		} else if( ret > 0 ) {
			handler.commit();
			return true;
		}

		handler.rollback();
		return false;
	}

	public boolean releasePriority( String countryCode, String processType ) throws SQLException {
		PreparedStatement pstmt_mng = null;
		PreparedStatement pstmt_cnt = null;
		ResultSet rset = null;
		try {
			pstmt_cnt = handler.getConnection().prepareStatement( "SELECT * FROM DPR_LOCK"
					+ " WHERE COUNTRYCD = ? AND TYPE = ? AND STATUS = '00' FOR UPDATE WAIT " + WAITING_INTERVAL );
			pstmt_cnt.setObject( 1, countryCode );
			pstmt_cnt.setObject( 2, processType );

			rset = pstmt_cnt.executeQuery();
			int cnt = getCount( rset );
			if( cnt > 0 ) {
				int ret = 0;

				try {
					pstmt_mng = handler.getConnection().prepareStatement( "DELETE FROM DPR_LOCK WHERE COUNTRYCD = ?"
							+ " AND TYPE = ? AND STATUS = '00'" );
					pstmt_mng.setObject( 1, countryCode );
					pstmt_mng.setObject( 2, processType );

					ret = pstmt_mng.executeUpdate();
				} catch( SQLException sqlEx ) {
					handler.rollback();
					throw sqlEx;
				} finally {
					try { pstmt_mng.close(); } catch( Exception ex ) {}
				}

				if( ret > 0 ) {
					handler.commit();
					return true;
				} else {
					handler.rollback();
					return false;
				}
			} else {
				handler.rollback();
				return ( cnt == 0 ? true : false );
			}
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt_cnt.close(); } catch( Exception ex ) {}
		}
	}
}
