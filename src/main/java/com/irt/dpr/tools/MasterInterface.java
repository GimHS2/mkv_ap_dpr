/*
 *	Name:	MasterInterface.java
 *	Version:	2.2.7
 *
 *	Description:
 *		D-Portal 2.0 Master 용 배치모듈
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	yjkdev21	2026/04/30		2.2.7	executeStatement(): 데이터 삭제 일자 기준 변경
 *	hankalam	2021/03/31		2.2.6	DPR_INF_CUSTOMER_ADDRESS 필드 추가
 *	hankalam	2020/06/30		2.2.5	DPR_INF_MATERIAL_PLANT 추가
 *	hankalam	2017/08/31		2.2.4	DPR_INF_CUSTOMER_PARTNER: SALES_ORG_ID, DIST_CHANNEL_ID, DIVISION_ID 추가
 *	song7981	2016/04/25		2.2.3	DPR_MATERIAL_MASTER: shelf_life 정보 추가
 *	jbaek		2014/02/16		2.2.2	Plant SKU Management 기능 개발: DPR_INF_PLANT 테이블
 *	lsinji		2010/05/21		2.2.1	PrimaryKey IS NOT NULL 조건 추가
 *										APMAS DB 사용자명 제거
 *										Characterset 설정 제거(UTF-8)
 *	guksm		2008/09/26		2.2.0	create
 *
**/
package com.irt.dpr.tools;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.log4j.Logger;

import com.irt.sql.SQLHandler;

class MasterInterface {

	private static Connection portal_conn						= null;
	private static Connection apmasq_conn						= null;

	private static void generateConnection( SQLHandler handler, SQLHandler central_handler) throws SQLException {
		portal_conn = handler.getConnection();
		apmasq_conn = central_handler.getConnection();
	}

	public static void masterInterface( SQLHandler handler, SQLHandler central_handler, Logger logger ) throws SQLException{
		String processName = null;
		String manageKey = null;

		logger.info( "Start Master Interface" );

		generateConnection( handler, central_handler );

		try {

			if( portal_conn == null )
				logger.error( "Not Connected Distributor Portal" );
			else if( apmasq_conn == null )
				logger.error( "Not Connected Central DB" );
			else {
				long currentTimeMillis = System.currentTimeMillis();

				if( manageKey == null || manageKey.length() == 0 )
					manageKey = createMng( logger );

				if( manageKey != null && manageKey.length() > 0 ) {
					if( !validManageKey(manageKey) ) {
						logger.error( "Invalid ManageKey("+ manageKey +")" );

						return;
					}
					logger.info( "Manager insert success("+ manageKey +")" );
				} else {
					logger.error( "Manager insert failed("+ manageKey +")" );
					portal_conn.rollback();

					return;
				}

				if( processName == null || processName.length() == 0 ) {
					logger.info( "Start copy("+ manageKey +")" );
					executeCopy( logger, manageKey );

					if( updateMng(logger, manageKey, System.currentTimeMillis() - currentTimeMillis, true) )
						logger.info( "End copy("+ manageKey +")" );
					else
						logger.error( "Manager update failed" );
					portal_conn.commit();
				}

// Package 실행
				if( processName == null || processName.length() == 0 ) {
					currentTimeMillis = System.currentTimeMillis();
					logger.info( "Start migration("+ manageKey +")" );
					executeMigration( logger, manageKey );

					if( updateMng(logger, manageKey, System.currentTimeMillis() - currentTimeMillis, false) )
						logger.info( "End migration("+ manageKey +")" );
					else
						logger.error( "Manager update failed" );

					portal_conn.commit();
				}
			}
		} catch( SQLException sqlEx ) {
			logger.error( sqlEx );
		}
		logger.info( "End Master Interface" );
	}

	private static boolean validManageKey( String manageKey ) throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rset = null;

		try {
			pstmt = portal_conn.prepareStatement( "SELECT 'xx' FROM DPR_INF_MNG WHERE MNG_KEY = ?" );
			pstmt.setString( 1, manageKey );
			rset = pstmt.executeQuery();

			if( rset.next() )
				return true;
			else
				return false;
		} finally {
			if( pstmt != null ) pstmt.close();
			if( rset != null ) rset.close();
		}
	}

	private static String createMng( Logger logger ) throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rset = null;

		try {
			String manageKey = null;
			pstmt = portal_conn.prepareStatement( "SELECT 'UPD' || seqDPR_INF_MNG.NEXTVAL FROM DUAL" );
			rset = pstmt.executeQuery();
			if( rset != null && rset.next() )
				manageKey = rset.getString( 1 );

			if( manageKey == null || manageKey.length() == 0 )
				return null;

			pstmt = portal_conn.prepareStatement( "INSERT INTO DPR_INF_MNG(MNG_KEY, INF_DATETIME) VALUES( ?, SYSDATE )" );
			pstmt.setString( 1, manageKey );

			if( pstmt.executeUpdate() != 1 )
				return null;

			return manageKey;
		} finally {
			if( pstmt != null ) pstmt.close();
			if( rset != null ) rset.close();
		}
	}

	private static boolean updateMng( Logger logger, String manageKey, long executeTime, boolean isCopy )
			throws SQLException {
		PreparedStatement pstmt = null;

		try {
			if( isCopy )
				pstmt = portal_conn.prepareStatement( "UPDATE DPR_INF_MNG SET COPY_EXECTIME = ? WHERE MNG_KEY = ?" );
			else
				pstmt = portal_conn.prepareStatement( "UPDATE DPR_INF_MNG SET PROC_EXECTIME = ? WHERE MNG_KEY = ?" );
			pstmt.setLong( 1, executeTime );
			pstmt.setString( 2, manageKey );

			if( pstmt.executeUpdate() == 1 )
				return true;
			else
				return false;
		} catch (SQLException sqlEx) {
			throw sqlEx;
		} finally {
			if( pstmt != null ) pstmt.close();
		}
	}

	private static void executeStatement( Logger logger, String manageKey, String portal_stmt, String apmasq_stmt, String tableName )
			throws SQLException {
		PreparedStatement portal_pstmt = null, apmasq_pstmt = null;
		Statement portal_delstmt = null;
		ResultSet rset = null;
		String charName = null;
		int deltemp = 0;
		int delcnt = 0;

		int successCount = 0, failCount = 0;
		int[] cnt = null;

		try {
			do{
				portal_delstmt = portal_conn.createStatement();
				String sql = "DELETE FROM " + "DPRADMIN." + tableName.toUpperCase() + " WHERE REGDATE < SYSDATE-10 and rownum <= 20000";
				deltemp = portal_delstmt.executeUpdate(sql);
				delcnt = delcnt + deltemp;
				portal_conn.commit();
				if( portal_delstmt != null) portal_delstmt.close();
				if(deltemp < 20000) break;
			}while(true);

		} catch ( SQLException sqlEx ) {
			logger.error( " DELETE : " + sqlEx );
		} finally {
			logger.info( "DELETE : " + delcnt );
			if( portal_delstmt != null) portal_delstmt.close();
			if( rset != null ) rset = null;
		}

		try {
			portal_pstmt = portal_conn.prepareStatement( portal_stmt );
			apmasq_pstmt = apmasq_conn.prepareStatement( apmasq_stmt );
			rset = apmasq_pstmt.executeQuery();

			portal_pstmt.setString( 1, manageKey );

			boolean isLast = false;
			boolean isLanguage = true;
			for( int i = 0; true ; i++ ) {
				isLast = !rset.next();

				try {
					if( i % 20000 == 0 || isLast ) {
						cnt = portal_pstmt.executeBatch();
						for (int element : cnt) {
							if( element > 0 || element == Statement.SUCCESS_NO_INFO )
								successCount++;
							else
								failCount++;
						}

						portal_conn.commit();
					}
				} catch( BatchUpdateException buEx ) {
					logger.error( "(batchUpdate)("+ buEx.getErrorCode() +")"+ buEx.getMessage() );
				} catch( SQLException sqEx) {
					logger.error( "(batchUpdate)("+ sqEx.getErrorCode() +")"+ sqEx );
				}

				if( isLast ) break;

				try {
					if( isLanguage ) {
						String language = rset.getString( "LANGUAGE" );
						if( language != null && language.length() > 0 ) {
							charName = "UTF-8";
/*
							if( "1".equals(language) || "ZH".equals(language) )
								charName = "GB2312";
							else if( "2".equals(language) || "TH".equals(language) )
								charName = "Windows-874";
							else if( "E".equals(language) || "EN".equals(language) )
								charName = "UTF-8";
							else if( "J".equals(language) || "JA".equals(language) )
								charName = "EUC-JP";
							else if( "M".equals(language) || "ZF".equals(language) )
								charName = "BIG5";
							else
								charName = "UTF-8";
*/
						}
					}
				} catch( SQLException sqlEx ) {
// 17006 is Invalid Column Name
					if( sqlEx.getErrorCode() != 17006 )
						throw sqlEx;
					else isLanguage = false;
				}

				for( int j = 1; j <= rset.getMetaData().getColumnCount(); j++ ) {
					if( charName != null && charName.length() > 0 && rset.getObject(j) != null )
						portal_pstmt.setString( j+1, new String(rset.getBytes(j), charName) );
					else
						portal_pstmt.setObject( j+1, rset.getObject(j) );
				}

				portal_pstmt.addBatch();
			}
		} catch( SQLException sqlEx ) {
			logger.error( sqlEx );
		} catch( IOException ioEx ) {
			logger.error( ioEx.getMessage() );
		} finally {
			if( portal_pstmt != null ) portal_pstmt.close();
			if( apmasq_pstmt != null ) apmasq_pstmt.close();
			if( rset != null ) rset.close();
			logger.info( tableName +" - success count: "+ String.valueOf(successCount) +", fail count: "+ String.valueOf(failCount) );
		}
	}

	private static void executeCopy( Logger logger, String manageKey ) throws SQLException {
		String portal_stmt, apmasq_stmt, tableName;

/*	MASTER
	RB2BMAST.JJ_SALES_DISTRICT			-> DPR_INF_SALES_DISTRICT
	RB2BMAST.JJ_SALES_ORG				-> DPR_INF_SALES_ORG
	RB2BMAST.JJ_SALES_GROUP				-> DPR_INF_SALES_GROUP
	RB2BMAST.JJ_SALES_OFFICE				-> DPR_INF_SALES_OFFICE
	RB2BMAST.JJ_SALES_OFFICE_GROUP		-> DPR_INF_SALES_OFFICE
	RB2BMAST.JJ_REGION					-> DPR_INF_REGION
	RB2BMAST.JJ_DIST_CHANNEL				-> DPR_INF_DIST_CHANNEL
	RB2BMAST.JJ_PRODUCT_HIERARCHY		-> DPR_INF_PRODUCT_HIERARCHY
	RB2BMAST.JJ_PRODUCT_HIERARCHY_TEXT	-> DPR_INF_PRODUCT_HIERARCHY_TEXT
	RB2BMAST.JJ_EMPLOYEE					-> DPR_INF_EMPLOYEE
*/

//	RB2BMAST.JJ_SALES_DISTRICT: 조건없이 모두 가져옴
		tableName = "DPR_INF_SALES_DISTRICT";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, SALES_DISTRICT_ID, SALES_DISTRICT_DESC, LANGUAGE)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT SALES_DISTRICT_ID, SALES_DISTRICT_DESC, LANGUAGE"
						+" FROM JJ_SALES_DISTRICT WHERE SALES_DISTRICT_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_SALES_ORG: 조건없이 모두 가져옴
		tableName = "DPR_INF_SALES_ORG";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, SALES_ORG_ID, DIVISION_ID, SALES_ORG_NAME, LANGUAGE)"
						+" VALUES( ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT SALES_ORG_ID, DIVISION_ID, SALES_ORG_NAME, LANGUAGE"
						+" FROM JJ_SALES_ORG";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_SALES_GROUP: 조건없이 모두 가져옴
		tableName = "DPR_INF_SALES_GROUP";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, SALES_GROUP_ID, SALES_GROUP_NAME, LANGUAGE)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT SALES_GROUP_ID, SALES_GROUP_NAME, LANGUAGE"
						+" FROM JJ_SALES_GROUP WHERE SALES_GROUP_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_SALES_OFFICE: 조건없이 모두 가져옴
		tableName = "DPR_INF_SALES_OFFICE";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, SALES_OFFICE_ID, SALES_ORG_ID, SALES_OFFICE_DESC, LANGUAGE)"
						+" VALUES( ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT SALES_OFFICE_ID, SALES_ORG_ID, SALES_OFFICE_DESC, LANGUAGE"
						+" FROM JJ_SALES_OFFICE WHERE SALES_OFFICE_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_SALES_OFFICE: 조건없이 모두 가져옴
		tableName = "DPR_INF_SALES_OFFICE_GROUP";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, SALES_GROUP_ID, SALES_OFFICE_ID)"
						+" VALUES( ?, ?, ? )";
		apmasq_stmt = "SELECT SALES_GROUP_ID, SALES_OFFICE_ID"
						+" FROM JJ_SALES_OFFICE_GROUP WHERE SALES_GROUP_ID IS NOT NULL AND SALES_OFFICE_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_REGION: 조건없이 모두 가져옴
		tableName = "DPR_INF_REGION";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, REGION_ID, COUNTRY_KEY, REGION_DESC, LANGUAGE)"
						+" VALUES( ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT REGION_ID, COUNTRY_KEY, REGION_DESC, LANGUAGE"
						+" FROM JJ_REGION WHERE REGION_ID IS NOT NULL AND LANGUAGE IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_DIST_CHANNEL: 조건없이 모두 가져옴
		tableName = "DPR_INF_DIST_CHANNEL";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, DIST_CHANNEL_ID, DIST_CHANNEL_NAME, LANGUAGE)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT DIST_CHANNEL_ID, DIST_CHANNEL_NAME, LANGUAGE"
						+" FROM JJ_DIST_CHANNEL WHERE DIST_CHANNEL_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_PLANT: 조건없이 모두 가져옴
		tableName = "DPR_INF_PLANT";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, PLANT_ID, PLANT_NAME, LOCAL_PLANT_NAME)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT PLANT_ID, PLANT_NAME, LOCAL_PLANT_NAME"
						+" FROM JJ_PLANT WHERE PLANT_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_PRODUCT_HIERARCHY: 조건없이 모두 가져옴
		tableName = "DPR_INF_PRODUCT_HIERARCHY";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, PRODUCT_HIERARCHY, HIERARCHY_LEVEL)"
						+" VALUES( ?, ?, ? )";
		apmasq_stmt = "SELECT PRODUCT_HIERARCHY, HIERARCHY_LEVEL"
						+" FROM JJ_PRODUCT_HIERARCHY WHERE PRODUCT_HIERARCHY IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_PRODUCT_HIERARCHY_TEXT: 조건없이 모두 가져옴
		tableName = "DPR_INF_PRODUCT_HIERARCHY_TEXT";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, PRODUCT_HIERARCHY, HIERARCHY_DESC, LANGUAGE)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT PRODUCT_HIERARCHY, HIERARCHY_DESC, LANGUAGE"
						+" FROM JJ_PRODUCT_HIERARCHY_TEXT WHERE PRODUCT_HIERARCHY IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_EMPLOYEE: 조건없이 모두 가져옴
		tableName = "DPR_INF_EMPLOYEE";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, EMPLOYEE_ID, FIRST_NAME, LAST_NAME, ZH_NAME)"
						+" VALUES( ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT EMPLOYEE_ID, FIRST_NAME, LAST_NAME, ZH_NAME"
						+" FROM JJ_EMPLOYEE WHERE EMPLOYEE_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );
/*
		{ // EMPLOYEE를 위한 특수한 경우
			PreparedStatement portal_pstmt = null, apmasq_pstmt = null;
			ResultSet rset = null;
			String charName = null;

			int successCount = 0, failCount = 0;
			int[] cnt = null;
			try {
				portal_pstmt = portal_conn.prepareStatement( portal_stmt );
				apmasq_pstmt = apmasq_conn.prepareStatement( apmasq_stmt );
				rset = apmasq_pstmt.executeQuery();

				portal_pstmt.setString( 1, manageKey );

				boolean isLast = false;
				for( int i = 0; true ; i++ ) {
					isLast = !rset.next();

					try {
						if( i % 20000 == 0 || isLast ) {
							cnt = portal_pstmt.executeBatch();
							for( int j = 0; j < cnt.length; j++ ) {
								if( cnt[j] > 0 || cnt[j] == Statement.SUCCESS_NO_INFO )
									successCount++;
								else
									failCount++;
							}

							portal_conn.commit();
						}
					} catch( BatchUpdateException buEx ) {
						logger.error( "(batchUpdate)("+ buEx.getErrorCode() +")"+ buEx.getMessage() );
					}

					if( isLast ) break;

					for( int j = 1; j <= rset.getMetaData().getColumnCount(); j++ ) {
						if( j == 4 && rset.getBytes(j) != null )	// j == 4 -> ZH_NAME
							portal_pstmt.setString( j+1, new String(rset.getBytes(j), "GB2312") );
						else
							portal_pstmt.setObject( j+1, rset.getObject(j) );
					}

					portal_pstmt.addBatch();
				}
			} catch( SQLException sqlEx ) {
				logger.error( sqlEx );
			} catch( IOException ioEx ) {
				logger.error( ioEx.getMessage() );
			} finally {
				if( portal_pstmt != null ) portal_pstmt.close();
				if( apmasq_pstmt != null ) apmasq_pstmt.close();
				if( rset != null ) rset.close();
				logger.info( ""
					+ tableName +" - success count: "+ String.valueOf(successCount) +", fail count: "+ String.valueOf(failCount) );
			}
		}
*/

/*	CUSTOMER
	RB2BMAST.JJ_CUSTOMER					-> DPR_INF_CUSTOMER
	RB2BMAST.JJ_CUSTOMER_ADDRESS			-> DPR_INF_CUSTOMER_ADDRESS
	RB2BMAST.JJ_CUSTOMER_PARTNER			-> DPR_INF_CUSTOMER_PARTNER
	RB2BMAST.JJ_CUSTOMER_GROUP			-> DPR_INF_CUSTOMER_GROUP
*/
//	RB2BMAST.JJ_CUSTOMER: 조건없이 모두 가져옴
		tableName = "DPR_INF_CUSTOMER";
		portal_stmt = "INSERT INTO "+ tableName
						+"(MNGKEY, CUSTOMER_ID, SALES_ORG_ID, SALES_GROUP_ID, SALES_DISTRICT_ID, CUSTOMER_GROUP_ID, SALES_OFFICE_ID"
						+", COUNTRY_KEY, CUSTOMER_NAME1, CUSTOMER_NAME2, ADDRESS1, ADDRESS2, CITY, ADDRESS_ID, ACCOUNT_AT_CUSTOMER"
						+", POSTAL_CODE, REGION_ID, TELEPHONE_NUMBER, FAX_NUMBER, DIST_CHANNEL_ID, DELIVERY_PLANT, DIVISION_ID"
						+", IS_BLOCKED, TAX_CODE_1, TRANSPORT_ZONE, PAYMENT_TERMS, DELIVERY_PRIORITY, TAX_CODE_2"
						+", DELETION_STATUS, DELETION_STATUS_SALES)"
						+ " VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT CUSTOMER_ID, SALES_ORG_ID, SALES_GROUP_ID, SALES_DISTRICT_ID, CUSTOMER_GROUP_ID, SALES_OFFICE_ID"
							+" , COUNTRY_KEY, CUSTOMER_NAME1, CUSTOMER_NAME2, ADDRESS1, ADDRESS2, CITY, ADDRESS_ID, ACCOUNT_AT_CUSTOMER"
							+", POSTAL_CODE, REGION_ID, TELEPHONE_NUMBER, FAX_NUMBER, DIST_CHANNEL_ID, DELIVERY_PLANT, DIVISION_ID"
							+", IS_BLOCKED, TAX_CODE_1, TRANSPORT_ZONE, PAYMENT_TERMS, DELIVERY_PRIORITY, TAX_CODE_2"
							+", DELETION_STATUS, DELETION_STATUS_SALES"
						+" FROM JJ_CUSTOMER WHERE CUSTOMER_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_CUSTOMER_ADDRESS: 조건없이 모두 가져옴
		tableName = "DPR_INF_CUSTOMER_ADDRESS";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, ADDRESS_ID, ADDRESS1, ADDRESS2, ADDRESS3, ADDRESS4, NATION, DATE_FROM, CITY"
						+ ", DISTRICT, BILL_CLOSING_DATE, LANGUAGE_KEY, EXTENSION2, CITY_CODE, DIST_CHANNEL_NAME)"
						+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT ADDRESS_ID, ADDRESS1, ADDRESS2, ADDRESS3, ADDRESS4, NATION, DATE_FROM, CITY"
						+ ", DISTRICT, BILL_CLOSING_DATE, LANGUAGE_KEY, EXTENSION2, CITY_CODE, DIST_CHANNEL_NAME"
						+" FROM JJ_CUSTOMER_ADDRESS WHERE ADDRESS_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_CUSTOMER_PARTNER: 조건없이 모두 가져옴
		tableName = "DPR_INF_CUSTOMER_PARTNER";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, CUSTOMER_ID, PARTNER_FUNCTION, PARTNER_COUNTER, EMPLOYEE_ID, PARTNER_ID"
						+ ", SALES_ORG_ID, DIST_CHANNEL_ID, DIVISION_ID)"
						+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT CUSTOMER_ID, PARTNER_FUNCTION, PARTNER_COUNTER, EMPLOYEE_ID, PARTNER_ID, SALES_ORG_ID, DIST_CHANNEL_ID, DIVISION_ID"
						+" FROM JJ_CUSTOMER_PARTNER";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_CUSTOMER_GROUP: 조건없이 모두 가져옴
		tableName = "DPR_INF_CUSTOMER_GROUP";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, CUSTOMER_GROUP_ID, CUSTOMER_GROUP_DESC, LANGUAGE)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT CUSTOMER_GROUP_ID, CUSTOMER_GROUP_DESC, LANGUAGE"
						+" FROM JJ_CUSTOMER_GROUP WHERE CUSTOMER_GROUP_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

/*
	RB2BMAST.JJ_MATERIAL					-> DPR_INF_MATERIAL
	RB2BMAST.JJ_MATERIAL_SALES			-> DPR_INF_MATERIAL_SALES
	RB2BMAST.JJ_MATERIAL_DESC			-> DPR_INF_MATERIAL_DESC
	RB2BMAST.JJ_MATERIAL_UOM				-> DPR_INF_MATERIAL_UOM
	RB2BMAST.JJ_MATERIAL_INTRO			-> DPR_INF_MATERIAL_INTRO
	RB2BMAST.JJ_MATERIAL_MEGA_BRAND		-> DPR_INF_MATERIAL_MEGA_BRAND
	RB2BMAST.JJ_MATERIAL_BRAND			-> DPR_INF_MATERIAL_BRAND
	RB2BMAST.JJ_MATERIAL_BASE_PRODUCT	-> DPR_INF_MATERIAL_BASE_PRODUCT
	RB2BMAST.JJ_MATERIAL_VARIANT			-> DPR_INF_MATERIAL_VARIANT
	RB2BMAST.JJ_MATERIAL_PUTUP			-> DPR_INF_MATERIAL_PUTUP
*/
//	RB2BMAST.JJ_MATERIAL: 조건없이 모두 가져옴
//	SHELF_LIFE 정보 추가
		tableName = "DPR_INF_MATERIAL";
		portal_stmt = "INSERT INTO "+ tableName
							+"(MNGKEY, MATERIAL_ID, MATERIAL_TYPE, BASE_UOM, SALES_UNIT, SALES_STATUS"
							+", SALES_STATUS_FROM_DATE, PRODUCT_HIERARCHY, SHELF_LIFE)"
						+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT MATERIAL_ID, MATERIAL_TYPE, BASE_UOM, SALES_UNIT, SALES_STATUS, SALES_STATUS_FROM_DATE, PRODUCT_HIERARCHY, SHELF_LIFE"
						+" FROM JJ_MATERIAL WHERE MATERIAL_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_MATERIAL_PLANT: 조건없이 모두 가져옴
		tableName = "DPR_INF_MATERIAL_PLANT";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, MATERIAL_ID, PLANT, PLANT_STATUS, PLANT_STATUS_FROM_DATE, DANGEROUS_IND)"
						+" VALUES( ?, ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT MATERIAL_ID, PLANT, PLANT_STATUS, PLANT_STATUS_FROM_DATE, DANGEROUS_IND"
						+" FROM JJ_MATERIAL_PLANT WHERE MATERIAL_ID IS NOT NULL AND PLANT IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_MATERIAL_SALES: 조건없이 모두 가져옴
		tableName = "DPR_INF_MATERIAL_SALES";
		portal_stmt = "INSERT INTO "+ tableName
							+"(MNGKEY, MATERIAL_ID, SALES_ORG_ID, DIST_CHAN_ID, PROD_HIERARCHY, PLANT, SKU_ID, CHAIN_STATUS"
							+", CHAIN_STATUS_FROM_DATE, BASE_PRODUCT, VARIANT, PUT_UP, MEGA_BRAND, BRAND, SALES_UNIT)"
						+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT MATERIAL_ID, SALES_ORG_ID, DIST_CHAN_ID, PROD_HIERARCHY, PLANT, SKU_ID, CHAIN_STATUS"
							+", CHAIN_STATUS_FROM_DATE, BASE_PRODUCT, VARIANT, PUT_UP, MEGA_BRAND, BRAND, SALES_UNIT"
						+" FROM JJ_MATERIAL_SALES WHERE MATERIAL_ID IS NOT NULL AND SALES_ORG_ID IS NOT NULL AND DIST_CHAN_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_MATERIAL_DESC: 조건없이 모두 가져옴
		tableName = "DPR_INF_MATERIAL_DESC";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, MATERIAL_ID, MATERIAL_DESC, LANGUAGE)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT MATERIAL_ID, MATERIAL_DESC, LANGUAGE"
						+" FROM JJ_MATERIAL_DESC WHERE MATERIAL_ID IS NOT NULL AND LANGUAGE IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_MATERIAL_UOM: 조건없이 모두 가져옴
		tableName = "DPR_INF_MATERIAL_UOM";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, MATERIAL_ID, UOM, PACKAGE, LENGTH, WIDTH, HEIGHT, LENGTH_UNIT"
							+", VOLUME, VOLUME_UNIT, GROSS_WEIGHT, NET_WEIGHT, WEIGHT_UNIT, EAN_CODE)"
						+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT MATERIAL_ID, UOM, PACKAGE, LENGTH, WIDTH, HEIGHT, LENGTH_UNIT"
							+", VOLUME, VOLUME_UNIT, GROSS_WEIGHT, NET_WEIGHT, WEIGHT_UNIT, EAN_CODE"
						+" FROM JJ_MATERIAL_UOM WHERE MATERIAL_ID IS NOT NULL AND UOM IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_MATERIAL_INTRO: 조건없이 모두 가져옴
		tableName = "DPR_INF_MATERIAL_INTRO";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, MATERIAL_ID, LANGUAGE, SALES_ORG_ID, INTRO)"
						+" VALUES( ?, ?, ?, ?, ? )";
		apmasq_stmt = "SELECT MATERIAL_ID, LANGUAGE, SALES_ORG_ID, INTRO"
						+" FROM JJ_MATERIAL_INTRO WHERE MATERIAL_ID IS NOT NULL AND LANGUAGE IS NOT NULL AND SALES_ORG_ID IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_MATERIAL_MEGA_BRAND: 조건없이 모두 가져옴
		tableName = "DPR_INF_MATERIAL_MEGA_BRAND";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, MEGA_BRAND_ID, LANGUAGE, MEGA_BRAND_DESC)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT MEGA_BRAND_ID, LANGUAGE, MEGA_BRAND_DESC"
						+" FROM JJ_MATERIAL_MEGA_BRAND WHERE MEGA_BRAND_ID IS NOT NULL AND LANGUAGE IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_MATERIAL_BRAND: 조건없이 모두 가져옴
		tableName = "DPR_INF_MATERIAL_BRAND";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, BRAND_ID, LANGUAGE, BRAND_DESC)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT BRAND_ID, LANGUAGE, BRAND_DESC"
						+" FROM JJ_MATERIAL_BRAND WHERE BRAND_ID IS NOT NULL AND LANGUAGE IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_MATERIAL_BASE_PRODUCT: 조건없이 모두 가져옴
		tableName = "DPR_INF_MATERIAL_BASE_PRODUCT";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, BASE_PROD_ID, LANGUAGE, BASE_PROD_DESC)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT BASE_PROD_ID, LANGUAGE, BASE_PROD_DESC"
						+" FROM JJ_MATERIAL_BASE_PRODUCT WHERE BASE_PROD_ID IS NOT NULL AND LANGUAGE IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_MATERIAL_VARIANT: 조건없이 모두 가져옴
		tableName = "DPR_INF_MATERIAL_VARIANT";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, VARIANT_ID, LANGUAGE, VARIANT_DESC)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT VARIANT_ID, LANGUAGE, VARIANT_DESC"
						+" FROM JJ_MATERIAL_VARIANT WHERE VARIANT_ID IS NOT NULL AND LANGUAGE IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );

//	RB2BMAST.JJ_MATERIAL_PUTUP: 조건없이 모두 가져옴
		tableName = "DPR_INF_MATERIAL_PUTUP";
		portal_stmt = "INSERT INTO "+ tableName +"(MNGKEY, PUTUP_ID, LANGUAGE, PUTUP_DESC)"
						+" VALUES( ?, ?, ?, ? )";
		apmasq_stmt = "SELECT PUTUP_ID, LANGUAGE, PUTUP_DESC"
						+" FROM JJ_MATERIAL_PUTUP WHERE PUTUP_ID IS NOT NULL AND LANGUAGE IS NOT NULL";
		executeStatement( logger, manageKey, portal_stmt, apmasq_stmt, tableName );
	}

	private static void executePackage( Logger logger, String manageKey, String query, String processName, String codeTableName, String descTableName )
				throws SQLException {
		CallableStatement cstmt = null;

		try {
			cstmt = portal_conn.prepareCall( query );
			cstmt.registerOutParameter( 1, Types.INTEGER );
			cstmt.registerOutParameter( 2, Types.INTEGER );
			cstmt.registerOutParameter( 3, Types.INTEGER );
			cstmt.registerOutParameter( 4, Types.INTEGER );
			cstmt.registerOutParameter( 5, Types.INTEGER );
			cstmt.registerOutParameter( 6, Types.INTEGER );
			cstmt.registerOutParameter( 7, Types.INTEGER );
			cstmt.setString( 8, manageKey );
			cstmt.executeUpdate();

			portal_conn.commit();
		} catch( SQLException sqlEx ) {
			logger.error( sqlEx );
		} finally {
			logger.info( ""
				+ processName +"(all:"+ String.valueOf(cstmt.getInt(1)) +") - "
				+ codeTableName +" insert:"+ String.valueOf(cstmt.getInt(2)) +" update:"+ String.valueOf(cstmt.getInt(3))
				+ " delete:" + String.valueOf(cstmt.getInt(4)) );
			if( descTableName != null && descTableName.length() > 0 ) {
				logger.info( descTableName +" insert:"+ String.valueOf(cstmt.getInt(5)) +" update:"+ String.valueOf(cstmt.getInt(6)) + " delete:" + String.valueOf(cstmt.getInt(7)) );
			}

			if( cstmt != null ) cstmt.close();
		}
	}

	private static void executeMigration( Logger logger, String manageKey ) throws SQLException {
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pSalesDistrict( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "SalesDistrict", "DPR_SALES_DISTRICT", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pSalesOrganization( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "SalesOrganization", "DPR_SALES_ORGANIZATION", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pSalesGroup( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "SalesGroup", "DPR_SALES_GROUP", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pSalesOffice( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "SalesOffice", "DPR_SALES_OFFICE", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pSalesOfficeGroup( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "SalesOfficeGroup", "DPR_SALES_OFFICE_GROUP", null );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pRegion( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "Region", "DPR_REGION", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pDistributionChannel( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "DistributionChannel", "DPR_DISTRIBUTION_CHANNEL", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pCustomerAddress( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "CustomerAddress", "DPR_CUSTOMER_ADDRESS", null );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pProductHierarchy( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "ProductHierarchy", "DPR_PRODUCT_CATE", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pEmployee( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "Employee", "DPR_EMPLOYEE", null );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pMegaBrand( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "MegaBrand", "DPR_MEGABRAND", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pBrand( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "Brand", "DPR_BRAND", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pBaseProduct( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "BaseProduct", "DPR_BASEPRODUCT", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pVariant( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "Variant", "DPR_VARIANT", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pPutup( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "Putup", "DPR_PUTUP", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pCustomerGroup( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "CustomerGroup", "DPR_CUSTOMER_GROUP", "DPR_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pMaterial( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "Material", "DPR_ITEM_MASTER", "DPR_ITEM_MASTER_DESC" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pMaterialPlant( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "MaterialPlant", "DPR_ITEM_MASTER_PLANT", "DPR_ITEM_MASTER" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pMaterialUom( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "MaterialUom", "DPR_ITEM_MASTER_UOM", null );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pMaterialIntro( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "MaterialIntro", "DPR_ITEM_MASTER_INTRO", null );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pMaterialSales( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "MaterialSales", "DPR_ITEM_MASTER_SALES", null );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pCustomer( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "Customer", "DPR_PARTY", "DPR_PARTY_SALES" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pCustomerPartner( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "CustomerPartner", "DPR_PARTY_LINK", "DPR_PARTY_EMPLOYEE" );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pPlant( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "Plant", "DPR_PLANT", null );
		executePackage( logger, manageKey, "BEGIN pkDPRMaster.pMakeUserAuthority( ?, ?, ?, ?, ?, ?, ?, ? ); END;"
			, "UserPartyAuth", "DPR_PARTY_AUTH", null );
	}
}
