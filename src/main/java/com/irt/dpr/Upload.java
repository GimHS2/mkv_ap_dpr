/*
 *	File Name:	Upload.java
 *	Version:	2.2.7
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/12/31		2.2.7	upload option 기능 추가
 *	jbaek		2019/06/30		2.2.6	StopItem, PackDeal 추가, allowUOM null경우 처리하지 않음.
 *	jbaek		2019/03/30		2.2.5	side-effect있는 function을 쓰면 쿼리가 느려지므로 임시로 날짜를 넣음.
 *	jbaek		2018/10/30		2.2.4	createTextReader() -> createDataReader() 변경.
 *  hankalam	2017/08/31		2.2.3	sellingSkuListRead(): newItemInd 항목 추가
 *	hankalam	2017/02/28		2.2.2	Order Input 에서 Order Item 업로드 시 Customer 별 사용 가능 UOM 검사로직 추가
 *	jbaek		2014/10/30		2.2.1	fieldKeyArray 사용, org별 uom validation 적용.소스정리.
 *	guksm		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataReader;
import com.irt.data.DataResult;
import com.irt.data.FieldException;
import com.irt.data.Record;
import com.irt.data.ValidableField;
import com.irt.data.ValidableFieldSet;
import com.irt.sql.ConditionQueryBuffer;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryFactory;
import com.irt.sql.QueryableImpl;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 */
public class Upload extends com.irt.rbm.ManipulableManagerImpl {//@formatter:off
	public static String UPLOADTYPE_CUSTOMER_ID_MAPPING		= "CIM";	// Local
	public static String UPLOADTYPE_CUSTOMER_MASTER			= "CMT";	// Central
	public static String UPLOADTYPE_INVENTORY				= "INV";	// Central
	public static String UPLOADTYPE_SELLING_SKU_LIST		= "SSL";	// Local
	public static String UPLOADTYPE_SELLOUT					= "SEO";	// Central
	public static String UPLOADTYPE_SKU_MAPPING				= "SKM";	// Local
	public static String UPLOADTYPE_ORDER_DETAIL			= "ORD";	// Local

	public static String STATUS_COMPLETE					= "CO";
	public static String STATUS_ERROR						= "ER";
	public static String STATUS_READY						= "RD";

	public static String SSLTYPE_DISTRIBUTOR				= "D";
	public static String SSLTYPE_ORGANIZATION				= "O";

	public static String UPLOAD_OPTION_ADD					= "ADD";
	public static String UPLOAD_OPTION_REPLACE				= "REP";

	public static String DEFAULT_DATE_TOKEN = "-";
	public static String[] DATE_TOKENS = new String[] { ":", "-", "/" };

	private int tableIndex = 0;

	private final static Table[] tables =  new Table[] {
			Schema.findTable( Schema.DPR_UPLOAD_HEADER ), Schema.findTable( Schema.DPR_UPLOAD_CIM )
			, Schema.findTable( Schema.DPR_UPLOAD_CMT ), Schema.findTable( Schema.DPR_UPLOAD_INV )
			, Schema.findTable( Schema.DPR_UPLOAD_SSL ), Schema.findTable( Schema.DPR_UPLOAD_SEO )
			, Schema.findTable( Schema.DPR_UPLOAD_SKM ), Schema.findTable( Schema.DPR_UPLOAD_ORD )
	};
	private final static QueryFactory[] factories = new QueryFactory[] {
			Schema.findQueryFactory( Schema.DPR_UPLOAD_HEADER ), Schema.findQueryFactory( Schema.DPR_UPLOAD_CIM )
			, Schema.findQueryFactory( Schema.DPR_UPLOAD_CMT ), Schema.findQueryFactory( Schema.DPR_UPLOAD_INV )
			, Schema.findQueryFactory( Schema.DPR_UPLOAD_SSL ), Schema.findQueryFactory( Schema.DPR_UPLOAD_SEO )
			, Schema.findQueryFactory( Schema.DPR_UPLOAD_SKM ), Schema.findQueryFactory( Schema.DPR_UPLOAD_ORD )
	};

	private final static QueryFactory factory = new QueryFactory( new QueryableImpl(Schema.findQueryable(Schema.DPR_UPLOAD_HEADER)) {
		@Override
		public boolean appendCondition( ConditionQueryBuffer querybuf ) {
			boolean ret = super.appendCondition( querybuf );

			Map<String, ? extends Object> conditionMap = querybuf.getConditionMap();

			if( conditionMap != null && conditionMap.size() > 0 ) {
				String conditionType = null;
				com.irt.data.Date uploadDate_min = null;
				com.irt.data.Date uploadDate_max = null;
				TimeZone timeZone = (TimeZone)conditionMap.get( "timeZone" );

				for( String conditionKey : querybuf.getConditionKeys() ) {
					if( "uploadDate".equals(conditionKey) ) {
						conditionType = (String)conditionMap.get( "uploadDate" + Condition.SUFFIX_TYPE );
						uploadDate_min = (com.irt.data.Date)conditionMap.get( "uploadDate" );
						if( uploadDate_min == null ) {
							uploadDate_min = (com.irt.data.Date)conditionMap.get( "uploadDate" + Condition.SUFFIX_MIN_VALUE );
							uploadDate_max = (com.irt.data.Date)conditionMap.get( "uploadDate" + Condition.SUFFIX_MAX_VALUE );
						}

						break;
					}
				}

//				String functionQuery = " pkSYSDate.fConvertDate( ?, ?, ? )";
				if( conditionType == null ) {
					return ret;
				} else if( Condition.CONDTYPE_EQUALS.equals(conditionType) ) {
//					querybuf.appendCondition( "UPDH.UPLOAD_DATETIME = " + functionQuery, new Object[] { uploadDate_min, timeZone, null } );
					querybuf.appendCondition( "UPDH.UPLOAD_DATETIME = ?" + uploadDate_min );
				} else if( Condition.CONDTYPE_NOTEQUALS.equals(conditionType) ) {
//					querybuf.appendCondition( "UPDH.UPLOAD_DATETIME <> " + functionQuery, new Object[] { uploadDate_min, timeZone, null } );
					querybuf.appendCondition( "UPDH.UPLOAD_DATETIME <> ?" , uploadDate_min );
				} else if( Condition.CONDTYPE_EQUALS_NONE.equals(conditionType)
						|| Condition.CONDTYPE_EQUALS_MIN.equals(conditionType)
						|| Condition.CONDTYPE_EQUALS_MAX.equals(conditionType)
						||Condition.CONDTYPE_EQUALS_MINMAX.equals(conditionType) ) {

					String operation_min = null;
					String operation_max = null;

					if( Condition.CONDTYPE_EQUALS_NONE.equals(conditionType) ) {
						operation_min = ">";
						operation_max = "<";
					} else if( Condition.CONDTYPE_EQUALS_MIN.equals(conditionType) ) {
						operation_min = ">=";
						operation_max = "<";
					} else if( Condition.CONDTYPE_EQUALS_MAX.equals(conditionType) ) {
						operation_min = ">";
						operation_max = "<=";
					} else if( Condition.CONDTYPE_EQUALS_MINMAX.equals(conditionType) ) {
						operation_min = ">=";
						operation_max = "<=";
					}

//					querybuf.appendCondition( "UPDH.UPLOAD_DATETIME " + operation_min + functionQuery
//							, new Object[] { uploadDate_min, timeZone.getID(), null } );
					querybuf.appendCondition( "UPDH.UPLOAD_DATETIME " + operation_min + " ?", uploadDate_min );

//					querybuf.appendCondition( "UPDH.UPLOAD_DATETIME " + operation_max + functionQuery
//							, new Object[] { uploadDate_max, timeZone.getID(), null } );
					querybuf.appendCondition( "UPDH.UPLOAD_DATETIME " + operation_max + " ?", uploadDate_max );
				}

				return ret;
			}

			return ret;
		}
	} );

	public Upload( SQLHandler handler ) {
		super( handler, tables[0], factory );
	}

	public Upload( SQLHandler handler, int type ) {
		super( handler, tables[type], factory );
		tableIndex = type;
	}

	public static Map<String, Object> createPrimary( String uploadCode ) {
		return Record.createMap( "uploadCode", uploadCode );
	}

	private void convertDate( Map<String, Object> map, String[] keys ) {
		for( int i = 0; i < keys.length; i++ ) {
			String dateValue = (String)map.get( keys[i] );
			if( dateValue != null ) {
				String formatString = com.irt.util.Utility.simpleConvertDateString( dateValue );
				if( formatString != null )
					map.put( keys[i], formatString );
				else
					map.put( keys[i], dateValue );
			}
		}
	}

	public int getDetailCount( Map<String, ? extends Object> conditionMap ) throws SQLException {
		QueryBuffer querybuf = factories[tableIndex].setCountQuery( new ConditionQueryBuffer(conditionMap) );
		return SQLManager.getInt( handler, querybuf );
	}

	public List<Map<String, Object>> getDetails( Map<String, ? extends Object> conditionMap, String[] fieldKeys, int skipRows, int maxRows ) throws SQLException {
		QueryBuffer querybuf = factories[tableIndex].setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		appendOrderBy( querybuf, factories[tableIndex] );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	public String getUploadCode() throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT 'UPD' || seqDPR_UPLOAD.NEXTVAL FROM DUAL" );
	}

	/* WORKING : DIST_CHANNEL 처리 executeCustomerMaster(), executeInventory(), executeSellout */
	public String execute( DataResult result, Map<String, Object> recordMap ) throws DataException, SQLException {
		Connection centralConn = null;

		String uploadType = (String)recordMap.get( "uploadType" );
		try {
			if( UPLOADTYPE_CUSTOMER_ID_MAPPING.equals(uploadType) )
				return executeCustomerIDMapping( result, recordMap );
			else if( UPLOADTYPE_CUSTOMER_MASTER.equals(uploadType) ) {
				centralConn = com.irt.central.CentralSystem.openConnection();
				if( centralConn == null )
					throw handler.createDataException( DataException.ERR_INVALID_CONNECTION );

				return executeCustomerMaster( result, centralConn, recordMap );
			} else if( UPLOADTYPE_INVENTORY.equals(uploadType) ) {
				centralConn = com.irt.central.CentralSystem.openConnection();
				if( centralConn == null )
					throw handler.createDataException( DataException.ERR_INVALID_CONNECTION );

				return executeInventory( result, centralConn, recordMap );
			} else if( UPLOADTYPE_SELLING_SKU_LIST.equals(uploadType) )
				return executeSellingSKUList( result, recordMap );
			else if( UPLOADTYPE_SELLOUT.equals(uploadType) ) {
				centralConn = com.irt.central.CentralSystem.openConnection();
				if( centralConn == null )
					throw handler.createDataException( DataException.ERR_INVALID_CONNECTION );

				return executeSellout( result, centralConn, recordMap );
			} else if( UPLOADTYPE_SKU_MAPPING.equals(uploadType) )
				return executeSKUMapping( result, recordMap );
			else if( UPLOADTYPE_ORDER_DETAIL.equals(uploadType) )
				return executeOrderDetail( result, recordMap );
			else
				throw handler.createDataException( DataException.ERR_ERROR, handler.getMessageHandler().getMessage("ERR_INVALID_UPLOADTYPE") );
		} finally {
			if( centralConn != null ) centralConn.close();
		}
	}

	private String executeCustomerIDMapping( DataResult result, Map<String, Object> recordMap )
				throws DataException, SQLException {
		CallableStatement cstmt = null;
		String uploadCode = (String)recordMap.get( "uploadCode" );
		String updateUserId = (String)recordMap.get( "uploadUserId" );

		try {
			cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkDPRUpload.fCustomerIDMapping( ?, ?, ?, ?, ? ); END;" );
			cstmt.registerOutParameter( 1, Types.INTEGER );
			cstmt.registerOutParameter( 2, Types.VARCHAR );
			cstmt.registerOutParameter( 3, Types.INTEGER );
			cstmt.registerOutParameter( 4, Types.INTEGER );
			cstmt.setObject( 5, uploadCode );
			cstmt.setObject( 6, updateUserId );
			cstmt.executeUpdate();

			if( cstmt.getInt(1) > 0 ) {
				result.increaseRegistCount( cstmt.getInt(3) );
				result.increaseModifyCount( cstmt.getInt(4) );
			} else
				return cstmt.getString( 2 );
		} finally {
			if( cstmt != null ) cstmt.close();
		}

		return null;
	}

	private String executeCustomerMaster( DataResult result, Connection centralConn, Map<String, Object> recordMap )
				throws DataException, SQLException {
		PreparedStatement dpsel_pstmt = null, dpupd_pstmt = null, cpins_pstmt = null, cpupd_pstmt = null;
		CallableStatement cstmt = null;
		ResultSet rset = null;

		String uploadCode = (String)recordMap.get( "uploadCode" );
		String updateUserId = (String)recordMap.get( "uploadUserId" );
		String countryCode = (String)recordMap.get( "countryCode" );
		String organizationCode = (String)recordMap.get( "organizationCode" );
		String divisionCode = (String)recordMap.get( "divisionCode" );
		Object[] distributionChannelCodes = com.irt.data.Record.extractObjectArray( recordMap, "distributionChannelds" );
		try {
			String distributionQuery = "DIST_CHANNELCD IN (";
			for( int i = 0; i < distributionChannelCodes.length; i++ )
				distributionQuery = " ?,";
			distributionQuery = distributionQuery.substring( distributionQuery.length() - 1 ) + " )";

			dpsel_pstmt = handler.getConnection().prepareStatement(
				"SELECT CMT.ROWID, VAL.PARTYCD, VAL.DIST_CHANNELCD, CMT.DISTRIBUTOR_ID, CMT.CUSTOMER_CD"
						+", CMT.CUSTOMER_NAME1, CMT.CUSTOMER_NAME2, CMT.CUSTOMER_GROUP, CMT.CUSTOMER_TYPE"
						+", CMT.ADDRESS1, CMT.ADDRESS2, CMT.POSTAL_CD, CMT.CONTACT_PERSON, CMT.PHONE_NUMBER"
						+", CMT.REGION_CD, CMT.PROVINCE_CITY, CMT.ACTIVE_ID, CMT.CUSTOM_FIELD1, CMT.CUSTOM_FIELD2"
					+" FROM DPR_UPLOAD_CMT CMT"
						+", (SELECT PAUT.PARTYCD, CM.LINK_PARTYCD, PAUT.DIST_CHANNELCD "
							+" FROM DPR_CUSTOMER_MAP CM, DPR_PARTY_AUTH PAUT"
							+" WHERE CM.PARTYCD = PAU.PARTYCD AND CM.COUNTRYCD = ?"
								+" AND PAUT.ORGANIZATIONCD = ? "
								+" AND PAUT." + distributionQuery
								+" AND PAUT.DIVISIONCD = ?"
								+" AND PAUT.UNIQID = ?) VAL"
					+" WHERE VAL.LINK_PARTYCD(+) = CMT.DISTRIBUTOR_ID AND CMT.UPLOADCD = ?" );

			dpupd_pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_UPLOAD_CMT SET MESSAGE = ?, STATUS = ? WHERE ROWID = ?" );

			cpins_pstmt = centralConn.prepareStatement(
				"INSERT INTO CUSTOMER_MASTER(DISTRIBUTOR_CD, SALES_ORG_ID, DIST_CHANNEL_ID, DIVISION_ID, CUSTOMER_CD"
						+", CUSTOMER_NAME1, CUSTOMER_NAME2, CUSTOMER_GROUP, CUSTOMER_TYPE, ADDRESS1, ADDRESS2, POSTAL_CD, CONTACT_PERSON"
						+", PHONE_NUMBER, REGION_CD, PROVINCE_CITY, ACTIVE_ID, CUSTOM_FIELD1, CUSTOM_FIELD2, UPGUSERID )"
					+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )" );
			cpupd_pstmt = centralConn.prepareStatement(
				"UPDATE CUSTOMER_MASTER SET CUSTOMER_NAME1 = ?, CUSTOMER_NAME2 = ?, CUSTOMER_GROUP = ?, CUSTOMER_TYPE = ?"
						+", ADDRESS1 = ?, ADDRESS2 = ?, POSTAL_CD = ?, CONTACT_PERSON = ?, PHONE_NUMBER = ?, REGION_CD = ?"
						+", PROVINCE_CITY = ?, ACTIVE_ID = ?, CUSTOM_FIELD1 = ?, CUSTOM_FIELD2 = ?, UPGDATE = SYSDATE, UPGUSERID = ?"
					+" WHERE DISTRIBUTOR_CD = ? AND SALES_ORG_ID = ? AND DIST_CHANNEL_ID = ? AND DIVISION_ID = ? AND CUSTOMER_CD = ?" );

			int index = 1;
			dpsel_pstmt.setObject( index++, countryCode );
			dpsel_pstmt.setObject( index++, organizationCode );
			for( int i = 0; i < distributionChannelCodes.length; i++ )
				dpsel_pstmt.setObject( index++, distributionChannelCodes[i] );
			dpsel_pstmt.setObject( index++, divisionCode );
			dpsel_pstmt.setObject( index++, updateUserId );
			dpsel_pstmt.setObject( index++, uploadCode );
			rset = dpsel_pstmt.executeQuery();
			while( rset.next() ) {
				try {
					String partyCode = rset.getString( "PARTYCD" );
					String distributionChannelCode = rset.getString( "DIST_CHANNELCD" );
					if( partyCode == null || partyCode.length() == 0 || distributionChannelCode == null || distributionChannelCode.length() == 0) {
						dpupd_pstmt.setObject( 1, handler.getMessageHandler().getMessage("ERR_UPLOAD_INVALID_DISTRIBUTORID", rset.getString("DISTRIBUTOR_ID")) );
						dpupd_pstmt.setObject( 2, STATUS_ERROR );
						dpupd_pstmt.setObject( 3, rset.getObject(1) );
						dpupd_pstmt.executeUpdate();

						continue;
					}

					index = 1;
					cpins_pstmt.setObject( index++, partyCode );
					cpins_pstmt.setObject( index++, organizationCode );
					cpins_pstmt.setObject( index++, rset.getObject("DIST_CHANNELCD") );
					cpins_pstmt.setObject( index++, divisionCode );
					cpins_pstmt.setObject( index++, rset.getObject("CUSTOMER_CD") );
					cpins_pstmt.setObject( index++, rset.getObject("CUSTOMER_NAME1") );
					cpins_pstmt.setObject( index++, rset.getObject("CUSTOMER_NAME2") );
					cpins_pstmt.setObject( index++, rset.getObject("CUSTOMER_GROUP") );
					cpins_pstmt.setObject( index++, rset.getObject("CUSTOMER_TYPE") );
					cpins_pstmt.setObject( index++, rset.getObject("ADDRESS1") );
					cpins_pstmt.setObject( index++, rset.getObject("ADDRESS2") );
					cpins_pstmt.setObject( index++, rset.getObject("POSTAL_CD") );
					cpins_pstmt.setObject( index++, rset.getObject("CONTACT_PERSON") );
					cpins_pstmt.setObject( index++, rset.getObject("PHONE_NUMBER") );
					cpins_pstmt.setObject( index++, rset.getObject("REGION_CD") );
					cpins_pstmt.setObject( index++, rset.getObject("PROVINCE_CITY") );
					cpins_pstmt.setObject( index++, rset.getObject("ACTIVE_ID") );
					cpins_pstmt.setObject( index++, rset.getObject("CUSTOM_FIELD1") );
					cpins_pstmt.setObject( index++, rset.getObject("CUSTOM_FIELD2") );
					cpins_pstmt.setObject( index++, updateUserId );
					try {
						if( cpins_pstmt.executeUpdate() == 1 )
							result.increaseRegistCount();
					} catch( SQLException sqlEx ) {
						if( com.irt.rbm.RBMDataHandler.DBERR_UNIQUE_CONSTRAINT == sqlEx.getErrorCode() ) {
							index = 1;
							cpupd_pstmt.setObject( index++, rset.getObject("CUSTOMER_NAME1") );
							cpupd_pstmt.setObject( index++, rset.getObject("CUSTOMER_NAME2") );
							cpupd_pstmt.setObject( index++, rset.getObject("CUSTOMER_GROUP") );
							cpupd_pstmt.setObject( index++, rset.getObject("CUSTOMER_TYPE") );
							cpupd_pstmt.setObject( index++, rset.getObject("ADDRESS1") );
							cpupd_pstmt.setObject( index++, rset.getObject("ADDRESS2") );
							cpupd_pstmt.setObject( index++, rset.getObject("POSTAL_CD") );
							cpupd_pstmt.setObject( index++, rset.getObject("CONTACT_PERSON") );
							cpupd_pstmt.setObject( index++, rset.getObject("PHONE_NUMBER") );
							cpupd_pstmt.setObject( index++, rset.getObject("REGION_CD") );
							cpupd_pstmt.setObject( index++, rset.getObject("PROVINCE_CITY") );
							cpupd_pstmt.setObject( index++, rset.getObject("ACTIVE_ID") );
							cpupd_pstmt.setObject( index++, rset.getObject("CUSTOM_FIELD1") );
							cpupd_pstmt.setObject( index++, rset.getObject("CUSTOM_FIELD2") );
							cpupd_pstmt.setObject( index++, updateUserId );
							cpupd_pstmt.setObject( index++, partyCode );
							cpupd_pstmt.setObject( index++, organizationCode );
							cpupd_pstmt.setObject( index++, rset.getObject("DIST_CHANNELCD") );
							cpupd_pstmt.setObject( index++, divisionCode );
							cpupd_pstmt.setObject( index++, rset.getObject("CUSTOMER_CD") );
							if( cpupd_pstmt.executeUpdate() == 1 )
								result.increaseModifyCount();
						} else
							throw sqlEx;
					}

					dpupd_pstmt.setObject( 1, null );
					dpupd_pstmt.setObject( 2, STATUS_COMPLETE );
					dpupd_pstmt.setObject( 3, rset.getObject("ROWID") );
					dpupd_pstmt.executeUpdate();
				} catch( SQLException sqlEx ) {
					result.increaseWarningCount();

					dpupd_pstmt.setObject( 1, sqlEx.getMessage() );
					dpupd_pstmt.setObject( 2, STATUS_ERROR );
					dpupd_pstmt.setObject( 3, rset.getObject("ROWID") );
					dpupd_pstmt.executeUpdate();
				}
			}
		} finally {
			if( dpsel_pstmt != null ) dpsel_pstmt.close();
			if( dpupd_pstmt != null ) dpsel_pstmt.close();
			if( cpins_pstmt != null ) cpins_pstmt.close();
			if( cpupd_pstmt != null ) cpupd_pstmt.close();
			if( cstmt != null ) cstmt.close();
			if( rset != null ) rset.close();
		}

		centralConn.commit();

		return null;
	}

	public String executeInventory( DataResult result, Connection centralConn, Map<String, Object> recordMap )
				throws DataException, SQLException {
		PreparedStatement dpsel_pstmt = null, dpupd_pstmt = null, cpins_pstmt = null, cpupd_pstmt = null;
		CallableStatement cstmt = null;
		ResultSet rset = null;

		String uploadCode = (String)recordMap.get( "uploadCode" );
		String updateUserId = (String)recordMap.get( "uploadUserId" );
		String countryCode = null, organizationCode = null, distributionChannelCode = null, divisionCode = null;
		try {
			dpsel_pstmt = handler.getConnection().prepareStatement(
				"SELECT INV.ROWID, VAL.PARTYCD, INV.DISTRIBUTOR_ID, INV.INV_DATE, INV.ITEMCD, INV.GTIN, INV.DIST_WAREHOUSE, INV.DESCRIPTION"
						+", INV.UNIT_PRICE, INV.UOM"
						+", INV.STOCK_QTY, UOM.PACKSIZE * STOCK_QTY \"STOCK_PIECES\", STOCK_QTY * UNIT_PRICE \"STOCK_PRICE\""
						+", INV.ONORDER_QTY, UOM.PACKSIZE * ONORDER_QTY \"ONORDER_PIECES\", ONORDER_QTY * UNIT_PRICE \"ONORDER_PRICE\""
						+", INV.COMMITTED_QTY, UOM.PACKSIZE * COMMITTED_QTY \"COMMITTED_PIECES\", COMMITTED_QTY * UNIT_PRICE \"COMMITTED_PRICE\""
						+", INV.TOTAL_AMOUNT, INV.CUSTOM_FIELD1, INV.CUSTOM_FIELD2"
					+" FROM DPR_UPLOAD_INV INV"
						+", (SELECT PAU.PARTYCD, CM.LINK_PARTYCD"
							+" FROM DPR_CUSTOMER_MAP CM, DPR_PARTY_AUTH PAU"
							+" WHERE CM.PARTYCD = PAU.PARTYCD AND CM.COUNTRYCD = ?"
								+" AND PAU.ORGANIZATIONCD = ? AND PAU.DIST_CHANNELCD = ? AND PAU.DIVISIONCD = ? AND PAU.UNIQID = ?) VAL"
						+", (SELECT SB.ITEMCD, SB.UOM_CD, SB.PACKSIZE FROM DPR_ITEM_MASTER_UOM SB) UOM"
					+" WHERE VAL.LINK_PARTYCD(+) = INV.DISTRIBUTOR_ID AND INV.UPLOADCD = ?"
						+" AND UOM.ITEMCD(+) = INV.ITEMCD AND UOM.UOM_CD(+) = INV.UOM" );
			dpupd_pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_UPLOAD_INV SET MESSAGE = ?, STATUS = ? WHERE ROWID = ?" );
			cpins_pstmt = centralConn.prepareStatement(
				"INSERT INTO INVENTORY(DISTRIBUTOR_CD, MATERIAL_ID, INV_DATE, DIST_WAREHOUSE, DIST_ITEMCD, DIST_GTIN, DIST_DESC, UNIT_PRICE"
						+", STOCK_QTY, STOCK_UOM, STOCK_PIECES, STOCK_PRICE, ONORDER_QTY, ONORDER_UOM, ONORDER_PIECES, ONORDER_PRICE"
						+", COMMITTED_QTY, COMMITTED_UOM, COMMITTED_PIECES, COMMITTED_PRICE, TOTAL_AMOUNT, CUSTOM_FIELD1, CUSTOM_FIELD2"
						+", UPGUSERID)"
					+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )" );
			cpupd_pstmt = centralConn.prepareStatement(
				"UPDATE INVENTORY SET DIST_ITEMCD = ?, DIST_GTIN = ?, DIST_DESC = ?, UNIT_PRICE = ?"
						+", STOCK_QTY = ?, STOCK_UOM = ?, STOCK_PIECES = ?, STOCK_PRICE = ?"
						+", ONORDER_QTY = ?, ONORDER_UOM = ?, ONORDER_PIECES = ?, ONORDER_PRICE = ?"
						+", COMMITTED_QTY = ?, COMMITTED_UOM = ?, COMMITTED_PIECES = ?, COMMITTED_PRICE = ?"
						+", CUSTOM_FIELD1 = ?, CUSTOM_FIELD2 = ?, UPGDATE = SYSDATE, UPGUSERID = ?"
					+" WHERE DISTRIBUTOR_CD = ? AND MATERIAL_ID = ? AND INV_DATE = ? AND DIST_WAREHOUSE = ?" );
			cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkDPRUpload.fGetDefaultCodes( ?, ?, ?, ?, ?, ? ); END;" );

			cstmt.registerOutParameter( 1, Types.INTEGER );
			cstmt.setObject( 2, uploadCode );
			cstmt.registerOutParameter( 3, Types.VARCHAR );
			cstmt.registerOutParameter( 4, Types.VARCHAR );
			cstmt.registerOutParameter( 5, Types.VARCHAR );
			cstmt.registerOutParameter( 6, Types.VARCHAR );
			cstmt.registerOutParameter( 7, Types.VARCHAR );
			cstmt.executeUpdate();

			if( cstmt.getInt(1) >= 0 ) {
				countryCode = cstmt.getString( 4 );
				organizationCode = cstmt.getString( 5 );
				distributionChannelCode = cstmt.getString( 6 );
				divisionCode = cstmt.getString( 7 );
			} else
				return cstmt.getString( 3 );

			dpsel_pstmt.setObject( 1, countryCode );
			dpsel_pstmt.setObject( 2, organizationCode );
			dpsel_pstmt.setObject( 3, distributionChannelCode );
			dpsel_pstmt.setObject( 4, divisionCode );
			dpsel_pstmt.setObject( 5, updateUserId );
			dpsel_pstmt.setObject( 6, uploadCode );
			rset = dpsel_pstmt.executeQuery();
			while( rset.next() ) {
				try {
					String partyCode = rset.getString( "PARTYCD" );
					if( partyCode == null || partyCode.length() == 0 ) {
						dpupd_pstmt.setObject( 1, handler.getMessageHandler().getMessage("ERR_UPLOAD_INVALID_DISTRIBUTORID", rset.getString("DISTRIBUTOR_ID")) );
						dpupd_pstmt.setObject( 2, STATUS_ERROR );
						dpupd_pstmt.setObject( 3, rset.getObject(1) );
						dpupd_pstmt.executeUpdate();

						continue;
					}

					cpins_pstmt.setObject( 1, partyCode );
					cpins_pstmt.setObject( 2, rset.getObject("ITEMCD") );
					cpins_pstmt.setObject( 3, rset.getObject("INV_DATE") );
					cpins_pstmt.setObject( 4, rset.getObject("DIST_WAREHOUSE") );
					cpins_pstmt.setObject( 5, null );
					cpins_pstmt.setObject( 6, rset.getObject("GTIN") );
					cpins_pstmt.setObject( 7, rset.getObject("DESCRIPTION") );
					cpins_pstmt.setObject( 8, rset.getObject("UNIT_PRICE") );
					cpins_pstmt.setObject( 9, rset.getObject("STOCK_QTY") );
					cpins_pstmt.setObject( 10, rset.getObject("UOM") );
					cpins_pstmt.setObject( 11, rset.getObject("STOCK_PIECES") );
					cpins_pstmt.setObject( 12, rset.getObject("STOCK_PRICE") );
					cpins_pstmt.setObject( 13, rset.getObject("ONORDER_QTY") );
					cpins_pstmt.setObject( 14, rset.getObject("UOM") );
					cpins_pstmt.setObject( 15, rset.getObject("ONORDER_PIECES") );
					cpins_pstmt.setObject( 16, rset.getObject("ONORDER_PRICE") );
					cpins_pstmt.setObject( 17, rset.getObject("COMMITTED_QTY") );
					cpins_pstmt.setObject( 18, rset.getObject("UOM") );
					cpins_pstmt.setObject( 19, rset.getObject("COMMITTED_PIECES") );
					cpins_pstmt.setObject( 20, rset.getObject("COMMITTED_PRICE") );
					cpins_pstmt.setObject( 21, rset.getObject("TOTAL_AMOUNT") );
					cpins_pstmt.setObject( 22, rset.getObject("CUSTOM_FIELD1") );
					cpins_pstmt.setObject( 23, rset.getObject("CUSTOM_FIELD2") );
					cpins_pstmt.setObject( 24, updateUserId );
					try {
						if( cpins_pstmt.executeUpdate() == 1 )
							result.increaseRegistCount();
					} catch( SQLException sqlEx ) {
						if( com.irt.rbm.RBMDataHandler.DBERR_UNIQUE_CONSTRAINT == sqlEx.getErrorCode() ) {
							cpupd_pstmt.setObject( 1, null );
							cpupd_pstmt.setObject( 2, rset.getObject("GTIN") );
							cpupd_pstmt.setObject( 3, rset.getObject("DESCRIPTION") );
							cpupd_pstmt.setObject( 4, rset.getObject("UNIT_PRICE") );
							cpupd_pstmt.setObject( 5, rset.getObject("STOCK_QTY") );
							cpupd_pstmt.setObject( 6, rset.getObject("UOM") );
							cpupd_pstmt.setObject( 7, rset.getObject("STOCK_PIECES") );
							cpupd_pstmt.setObject( 8, rset.getObject("STOCK_PRICE") );
							cpupd_pstmt.setObject( 9, rset.getObject("ONORDER_QTY") );
							cpupd_pstmt.setObject( 10, rset.getObject("UOM") );
							cpupd_pstmt.setObject( 11, rset.getObject("ONORDER_PIECES") );
							cpupd_pstmt.setObject( 12, rset.getObject("ONORDER_PRICE") );
							cpupd_pstmt.setObject( 13, rset.getObject("COMMITTED_QTY") );
							cpupd_pstmt.setObject( 14, rset.getObject("UOM") );
							cpupd_pstmt.setObject( 15, rset.getObject("COMMITTED_PIECES") );
							cpupd_pstmt.setObject( 16, rset.getObject("COMMITTED_PRICE") );
							cpupd_pstmt.setObject( 17, rset.getObject("CUSTOM_FIELD1") );
							cpupd_pstmt.setObject( 18, rset.getObject("CUSTOM_FIELD2") );
							cpupd_pstmt.setObject( 19, updateUserId );
							cpupd_pstmt.setObject( 20, rset.getObject("PARTYCD") );
							cpupd_pstmt.setObject( 21, rset.getObject("ITEMCD") );
							cpupd_pstmt.setObject( 22, rset.getObject("INV_DATE") );
							cpupd_pstmt.setObject( 23, rset.getObject("DIST_WAREHOUSE") );
							if( cpupd_pstmt.executeUpdate() == 1 )
								result.increaseModifyCount();
						} else
							throw sqlEx;
					}

					dpupd_pstmt.setObject( 1, null );
					dpupd_pstmt.setObject( 2, STATUS_COMPLETE );
					dpupd_pstmt.setObject( 3, rset.getObject("ROWID") );
					dpupd_pstmt.executeUpdate();
				} catch( SQLException sqlEx ) {
					result.increaseWarningCount();

					dpupd_pstmt.setObject( 1, sqlEx.getMessage() );
					dpupd_pstmt.setObject( 2, STATUS_ERROR );
					dpupd_pstmt.setObject( 3, rset.getObject("ROWID") );
					dpupd_pstmt.executeUpdate();
				}
			}
		} finally {
			if( dpsel_pstmt != null ) dpsel_pstmt.close();
			if( dpupd_pstmt != null ) dpsel_pstmt.close();
			if( cpins_pstmt != null ) cpins_pstmt.close();
			if( cpupd_pstmt != null ) cpupd_pstmt.close();
			if( cstmt != null ) cstmt.close();
			if( rset != null ) rset.close();
		}

		centralConn.commit();

		return null;
	}

	public String executeOrderDetail( DataResult result, Map<String, Object> recordMap ) throws DataException, SQLException {
		CallableStatement cstmt = null;

		String uploadCode = (String)recordMap.get( "uploadCode" );
		String updateUserId = (String)recordMap.get( "uploadUserId" );
		try {
			cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkDPRUpload.fUploadOrderDetail( ?, ?, ?, ?, ? ); END;" );
			cstmt.registerOutParameter( 1, Types.INTEGER );
			cstmt.registerOutParameter( 2, Types.VARCHAR );
			cstmt.registerOutParameter( 3, Types.INTEGER );
			cstmt.registerOutParameter( 4, Types.INTEGER );
			cstmt.setObject( 5, uploadCode );
			cstmt.setObject( 6, updateUserId );
			cstmt.executeUpdate();

			if( cstmt.getInt(1) > 0 ) {
				result.increaseRegistCount( cstmt.getInt(3) );
				result.increaseModifyCount( cstmt.getInt(4) );
			} else
				return cstmt.getString( 2 );
		} finally {
			if( cstmt != null ) cstmt.close();
		}

		return null;
	}

	public String executeSellout( DataResult result, Connection centralConn, Map<String, Object> recordMap )
				throws DataException, SQLException {
		PreparedStatement dpsel_pstmt = null, dpupd_pstmt = null, cpins_pstmt = null, cpupd_pstmt = null;
		CallableStatement cstmt = null;
		ResultSet rset = null;

		String uploadCode = (String)recordMap.get( "uploadCode" );
		String updateUserId = (String)recordMap.get( "uploadUserId" );
		String countryCode = null, organizationCode = null, distributionChannelCode = null, divisionCode = null;
		try {
			dpsel_pstmt = handler.getConnection().prepareStatement(
				"SELECT SEO.ROWID, VAL.PARTYCD, SEO.DISTRIBUTOR_ID, SEO.ITEMCD, SEO.SELLOUT_CD, SEO.SELLOUT_DATE, SEO.SELLOUT_TYPE"
						+", SEO.CUSTOMER_CD, SEO.GTIN, SEO.DIST_WAREHOUSE, SEO.DESCRIPTION, SEO.SALES_EMPLOYEE, SEO.PRICE"
						+", SEO.SELLOUT_QTY, SEO.SELLOUT_UOM, SEO.TOTAL_PRICE, SEO.DISCOUNT_PRICE, SEO.TOTAL_DISCOUNT_PRICE"
						+", SEO.BOTTOMLINE_DISCOUNT_PRICE, SEO.TOTAL_AMOUNT, SEO.TOTAL_AMOUNT_TAX"
						+", SEO.TOTAL_AMOUNT_TAX - SEO.TOTAL_AMOUNT \"TAX\", CUSTOM_FIELD1, CUSTOM_FIELD2, CUSTOM_FIELD3"
					+" FROM DPR_UPLOAD_SEO SEO"
						+", (SELECT PAU.PARTYCD, CM.LINK_PARTYCD"
							+" FROM DPR_CUSTOMER_MAP CM, DPR_PARTY_AUTH PAU"
							+" WHERE CM.PARTYCD = PAU.PARTYCD AND CM.COUNTRYCD = ?"
								+" AND PAU.ORGANIZATIONCD = ? AND PAU.DIST_CHANNELCD = ? AND PAU.DIVISIONCD = ? AND PAU.UNIQID = ?) VAL"
						+", (SELECT SB.ITEMCD, SB.UOM_CD, SB.PACKSIZE FROM DPR_ITEM_MASTER_UOM SB) UOM"
					+" WHERE VAL.LINK_PARTYCD(+) = SEO.DISTRIBUTOR_ID AND SEO.UPLOADCD = ?"
						+" AND UOM.ITEMCD(+) = SEO.ITEMCD AND UOM.UOM_CD(+) = SEO.SELLOUT_UOM" );
			dpupd_pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_UPLOAD_SEO SET MESSAGE = ?, STATUS = ? WHERE ROWID = ?" );
			cpins_pstmt = centralConn.prepareStatement(
				"INSERT INTO SELL_OUT(DISTRIBUTOR_CD, MATERIAL_ID, SELLOUT_DATE, SELLOUT_CD, SELLOUT_TYPE, CUSTOMER_CD, DIST_ITEMCD"
						+", DIST_GTIN, DIST_WAREHOUSE, DIST_DESC, SALES_EMPLOYEE, PRICE, SELLOUT_QTY, SELLOUT_QTY_CON, SELLOUT_QTY_PIECES"
						+", SELLOUT_UOM, TOTAL_PRICE, DISCOUNT_PRICE, TOTAL_DISCOUNT_PRICE, BOTTOMLINE_DISCOUNT_PRICE, TAX"
						+", TOTAL_AMOUNT, TOTAL_AMOUNT_TAX, CUSTOM_FIELD1, CUSTOM_FIELD2, CUSTOM_FIELD3, UPGUSERID)"
					+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?  )" );
			cpupd_pstmt = centralConn.prepareStatement(
				"UPDATE SELL_OUT SET SELLOUT_TYPE = ?, CUSTOMER_CD = ?, DIST_ITEMCD = ?, DIST_GTIN = ?, DIST_WAREHOUSE = ?, DIST_DESC = ?"
						+", SALES_EMPLOYEE = ?, PRICE = ?, SELLOUT_QTY = ?, SELLOUT_QTY_CON = ?, SELLOUT_QTY_PIECES = ?, SELLOUT_UOM = ?"
						+", TOTAL_PRICE = ?, DISCOUNT_PRICE = ?, TOTAL_DISCOUNT_PRICE = ?, BOTTOMLINE_DISCOUNT_PRICE = ?"
						+", TAX = ?, TOTAL_AMOUNT = ?, TOTAL_AMOUNT_TAX = ?"
						+", CUSTOM_FIELD1 = ?, CUSTOM_FIELD2 = ?, CUSTOM_FIELD3 = ?, UPGDATE = SYSDATE, UPGUSERID = ?"
					+" WHERE DISTRIBUTOR_CD = ? AND MATERIAL_ID = ? AND SELLOUT_DATE = ? AND SELLOUT_CD = ?" );
			cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkDPRUpload.fGetDefaultCodes( ?, ?, ?, ?, ?, ? ); END;" );

			cstmt.registerOutParameter( 1, Types.INTEGER );
			cstmt.setObject( 2, uploadCode );
			cstmt.registerOutParameter( 3, Types.VARCHAR );
			cstmt.registerOutParameter( 4, Types.VARCHAR );
			cstmt.registerOutParameter( 5, Types.VARCHAR );
			cstmt.registerOutParameter( 6, Types.VARCHAR );
			cstmt.registerOutParameter( 7, Types.VARCHAR );
			cstmt.executeUpdate();

			if( cstmt.getInt(1) >= 0 ) {
				countryCode = cstmt.getString( 4 );
				organizationCode = cstmt.getString( 5 );
				distributionChannelCode = cstmt.getString( 6 );
				divisionCode = cstmt.getString( 7 );
			} else
				return cstmt.getString( 3 );

			dpsel_pstmt.setObject( 1, countryCode );
			dpsel_pstmt.setObject( 2, organizationCode );
			dpsel_pstmt.setObject( 3, distributionChannelCode );
			dpsel_pstmt.setObject( 4, divisionCode );
			dpsel_pstmt.setObject( 5, updateUserId );
			dpsel_pstmt.setObject( 6, uploadCode );
			rset = dpsel_pstmt.executeQuery();
			while( rset.next() ) {
				try {
					String partyCode = rset.getString( "PARTYCD" );
					if( partyCode == null || partyCode.length() == 0 ) {
						dpupd_pstmt.setObject( 1, handler.getMessageHandler().getMessage("ERR_UPLOAD_INVALID_DISTRIBUTORID", rset.getString("DISTRIBUTOR_ID")) );
						dpupd_pstmt.setObject( 2, STATUS_ERROR );
						dpupd_pstmt.setObject( 3, rset.getObject(1) );
						dpupd_pstmt.executeUpdate();

						continue;
					}

					cpins_pstmt.setObject( 1, partyCode );
					cpins_pstmt.setObject( 2, rset.getObject("ITEMCD") );
					cpins_pstmt.setObject( 3, rset.getObject("SELLOUT_DATE") );
					cpins_pstmt.setObject( 4, rset.getObject("SELLOUT_CD") );
					cpins_pstmt.setObject( 5, null );
					cpins_pstmt.setObject( 6, rset.getObject("CUSTOMER_CD") );
					cpins_pstmt.setObject( 7, null );
					cpins_pstmt.setObject( 8, rset.getObject("GTIN") );
					cpins_pstmt.setObject( 9, rset.getObject("DIST_WAREHOUSE") );
					cpins_pstmt.setObject( 10, rset.getObject("DESCRIPTION") );
					cpins_pstmt.setObject( 11, rset.getObject("SALES_EMPLOYEE") );
					cpins_pstmt.setObject( 12, rset.getObject("PRICE") );
					cpins_pstmt.setObject( 13, rset.getObject("SELLOUT_QTY") );
					cpins_pstmt.setObject( 14, null );
					cpins_pstmt.setObject( 15, null );
					cpins_pstmt.setObject( 16, rset.getObject("SELLOUT_UOM") );
					cpins_pstmt.setObject( 17, rset.getObject("TOTAL_PRICE") );
					cpins_pstmt.setObject( 18, rset.getObject("DISCOUNT_PRICE") );
					cpins_pstmt.setObject( 19, rset.getObject("TOTAL_DISCOUNT_PRICE") );
					cpins_pstmt.setObject( 20, rset.getObject("BOTTOMLINE_DISCOUNT_PRICE") );
					cpins_pstmt.setObject( 21, rset.getObject("TAX") );
					cpins_pstmt.setObject( 22, rset.getObject("TOTAL_AMOUNT") );
					cpins_pstmt.setObject( 23, rset.getObject("TOTAL_AMOUNT_TAX") );
					cpins_pstmt.setObject( 24, rset.getObject("CUSTOM_FIELD1") );
					cpins_pstmt.setObject( 25, rset.getObject("CUSTOM_FIELD2") );
					cpins_pstmt.setObject( 26, rset.getObject("CUSTOM_FIELD3") );
					cpins_pstmt.setObject( 27, updateUserId );
					try {
						if( cpins_pstmt.executeUpdate() == 1 )
							result.increaseRegistCount();
					} catch( SQLException sqlEx ) {
						if( com.irt.rbm.RBMDataHandler.DBERR_UNIQUE_CONSTRAINT == sqlEx.getErrorCode() ) {
							cpupd_pstmt.setObject( 1, null );
							cpupd_pstmt.setObject( 2, rset.getObject("CUSTOMER_CD") );
							cpupd_pstmt.setObject( 3, null );
							cpupd_pstmt.setObject( 4, rset.getObject("GTIN") );
							cpupd_pstmt.setObject( 5, rset.getObject("DIST_WAREHOUSE") );
							cpupd_pstmt.setObject( 6, rset.getObject("DESCRIPTION") );
							cpupd_pstmt.setObject( 7, rset.getObject("SALES_EMPLOYEE") );
							cpupd_pstmt.setObject( 8, rset.getObject("PRICE") );
							cpupd_pstmt.setObject( 9, rset.getObject("SELLOUT_QTY") );
							cpupd_pstmt.setObject( 10, null );
							cpupd_pstmt.setObject( 11, null );
							cpupd_pstmt.setObject( 12, rset.getObject("SELLOUT_UOM") );
							cpupd_pstmt.setObject( 13, rset.getObject("TOTAL_PRICE") );
							cpupd_pstmt.setObject( 14, rset.getObject("DISCOUNT_PRICE") );
							cpupd_pstmt.setObject( 15, rset.getObject("TOTAL_DISCOUNT_PRICE") );
							cpupd_pstmt.setObject( 16, rset.getObject("BOTTOMLINE_DISCOUNT_PRICE") );
							cpupd_pstmt.setObject( 17, rset.getObject("TAX") );
							cpupd_pstmt.setObject( 18, rset.getObject("TOTAL_AMOUNT") );
							cpupd_pstmt.setObject( 19, rset.getObject("TOTAL_AMOUNT_TAX") );
							cpupd_pstmt.setObject( 20, rset.getObject("CUSTOM_FIELD1") );
							cpupd_pstmt.setObject( 21, rset.getObject("CUSTOM_FIELD2") );
							cpupd_pstmt.setObject( 22, rset.getObject("CUSTOM_FIELD3") );
							cpupd_pstmt.setObject( 23, updateUserId );
							cpupd_pstmt.setObject( 24, partyCode );
							cpupd_pstmt.setObject( 25, rset.getObject("ITEMCD") );
							cpupd_pstmt.setObject( 26, rset.getObject("SELLOUT_DATE") );
							cpupd_pstmt.setObject( 27, rset.getObject("SELLOUT_CD") );
							if( cpupd_pstmt.executeUpdate() == 1 )
								result.increaseModifyCount();
						} else
							throw sqlEx;
					}

					dpupd_pstmt.setObject( 1, null );
					dpupd_pstmt.setObject( 2, STATUS_COMPLETE );
					dpupd_pstmt.setObject( 3, rset.getObject("ROWID") );
					dpupd_pstmt.executeUpdate();
				} catch( SQLException sqlEx ) {
					result.increaseWarningCount();

					dpupd_pstmt.setObject( 1, sqlEx.getMessage() );
					dpupd_pstmt.setObject( 2, STATUS_ERROR );
					dpupd_pstmt.setObject( 3, rset.getObject("ROWID") );
					dpupd_pstmt.executeUpdate();
				}
			}
		} finally {
			if( dpsel_pstmt != null ) dpsel_pstmt.close();
			if( dpupd_pstmt != null ) dpsel_pstmt.close();
			if( cpins_pstmt != null ) cpins_pstmt.close();
			if( cpupd_pstmt != null ) cpupd_pstmt.close();
			if( cstmt != null ) cstmt.close();
			if( rset != null ) rset.close();
		}

		centralConn.commit();

		return null;
	}

	private String executeSellingSKUList( DataResult result, Map<String, Object> recordMap )
				throws DataException, SQLException {
		CallableStatement cstmt = null;

		String uploadCode = (String)recordMap.get( "uploadCode" );
		String uploadOption = (String)recordMap.get( "uploadOption" );
		String type = (String)recordMap.get( "sslType" );
		String updateUserId = (String)recordMap.get( "uploadUserId" );
		try {
			cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkDPRUpload.fSellingSKUList( ?, ?, ?, ?, ?, ?, ?, ? ); END;" );
			cstmt.registerOutParameter( 1, Types.INTEGER );
			cstmt.registerOutParameter( 2, Types.VARCHAR );
			cstmt.registerOutParameter( 3, Types.INTEGER );
			cstmt.registerOutParameter( 4, Types.INTEGER );
			cstmt.registerOutParameter( 5, Types.INTEGER );
			cstmt.setObject( 6, uploadCode );
			cstmt.setObject( 7, uploadOption );
			cstmt.setObject( 8, type );
			cstmt.setObject( 9, updateUserId );
			cstmt.executeUpdate();

			if( cstmt.getInt(1) > 0 ) {
				result.increaseRegistCount( cstmt.getInt(3) );
				result.increaseErrorCount( cstmt.getInt(4) );
				result.increaseIgnoreCount( cstmt.getInt(5) );
			} else
				return cstmt.getString( 2 );
		} finally {
			try { if( cstmt != null ) cstmt.close(); } catch( Exception ex ) {}
		}

		return null;
	}

	private String executeSKUMapping( DataResult result, Map<String, Object> recordMap ) throws DataException, SQLException {
		CallableStatement cstmt = null;

		String uploadCode = (String)recordMap.get( "uploadCode" );
		String updateUserId = (String)recordMap.get( "uploadUserId" );
		try {
			cstmt = handler.getConnection().prepareCall( "BEGIN ? := pkDPRUpload.fSKUMapping( ?, ?, ?, ?, ? ); END;" );
			cstmt.registerOutParameter( 1, Types.INTEGER );
			cstmt.registerOutParameter( 2, Types.VARCHAR );
			cstmt.registerOutParameter( 3, Types.INTEGER );
			cstmt.registerOutParameter( 4, Types.INTEGER );
			cstmt.setObject( 5, uploadCode );
			cstmt.setObject( 6, updateUserId );
			cstmt.executeUpdate();

			if( cstmt.getInt(1) > 0 ) {
				result.increaseRegistCount( cstmt.getInt(3) );
				result.increaseModifyCount( cstmt.getInt(4) );
			} else
				return cstmt.getString( 2 );
		} finally {
			if( cstmt != null ) cstmt.close();
		}

		return null;
	}

	public DataResult read( DataReader reader, String uploadType, String uploadCode, String type, String updateUserId, String headerInd )
				throws DataException, IOException, SQLException {
		if( UPLOADTYPE_CUSTOMER_ID_MAPPING.equals(uploadType) )
			return customerIdMappingRead( reader, uploadCode, updateUserId, headerInd );
		else if( UPLOADTYPE_CUSTOMER_MASTER.equals(uploadType) )
			return customerMasterRead( reader, uploadCode, headerInd );
		else if( UPLOADTYPE_INVENTORY.equals(uploadType) )
			return inventoryRead( reader, uploadCode, headerInd );
		else if( UPLOADTYPE_SELLING_SKU_LIST.equals(uploadType) )
			return sellingSkuListRead( reader, uploadCode, type, updateUserId, headerInd );
		else if( UPLOADTYPE_SELLOUT.equals(uploadType) )
			return selloutRead( reader, uploadCode, headerInd );
		else if( UPLOADTYPE_SKU_MAPPING.equals(uploadType) )
			return skuMappingRead( reader, uploadCode, updateUserId, headerInd );
		else
			return null;
	}

	public DataResult read( DataReader reader, String uploadType, Map<String, ? extends Object> parameterMap, String updateUserId, String headerInd ) throws DataException, IOException, SQLException {
		return orderDetailRead( reader, parameterMap, headerInd, null );
	}

	public DataResult read( DataReader reader, String uploadType, Map<String, ? extends Object> parameterMap, String updateUserId, String headerInd, String allowUOM ) throws DataException, IOException, SQLException {
		return orderDetailRead( reader, parameterMap, headerInd, allowUOM );
	}

	private DataResult customerIdMappingRead( DataReader reader, String uploadCode, String updateUserId, String headerInd )
				throws DataException, IOException, SQLException {
		String[] formats = new String[] {
			"distributorId", "distributorCode", "customerCategory", "salesType", "currencyCode", "globalParentCode", "globalGrandParentCode", "calendarType"
		};
		ValidableFieldSet validableFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( false,	"uploadCode",			"DPR_UPLOADCIM_UPLOADCODE",				0, 10 )
			, new ValidableField( false,	"distributorId",		"DPR_UPLOADCIM_DISTRIBUTORID",			0, 30 )
			, new ValidableField( false,	"distributorCode",		"DPR_UPLOADCIM_DISTRIBUTORCODE",		0, 15 )
			, new ValidableField( true,		"customerCategory",		"DPR_UPLOADCIM_CUSTOMERCATEGORY",		0, 40 )
			, new ValidableField( true,		"salesType",			"DPR_UPLOADCIM_SALESTYPE",				0, 40 )
			, new ValidableField( true,		"currencyCode",			"DPR_UPLOADCIM_CURRENCYCODE",			0, 3 )
			, new ValidableField( true,		"globalParentCode",		"DPR_UPLOADCIM_GLOBALPARENTCODE",		0, 20 )
			, new ValidableField( true,		"globalGrandParentCode","DPR_UPLOADCIM_GLOBALGRANDPARENTCODE",	0, 20 )
			, new ValidableField( true,		"calendarType",			"DPR_UPLOADCIM_CALENDARTYPE",			0, 40 )
			, new ValidableField( false,	"lineNumber",			"DPR_UPLOADCIM_LINENUMBER",				ValidableField.TYPE_INTEGER, 0, 99999999 )
			, new ValidableField( false,	"status",				"DPR_UPLOADCIM_STATUS",					0, 2 )
		} );

		DataResult result = new DataResult();
		PreparedStatement pstmt = null;
		CallableStatement cstmt = null;
		long startTimeMillis = System.currentTimeMillis();
		try {
			pstmt = handler.getConnection().prepareStatement(
				"INSERT INTO DPR_UPLOAD_CIM( UPLOADCD, DISTRIBUTOR_ID, DISTRIBUTOR_CD, CUSTOMER_CATEGORY, SALES_TYPE, CURRENCYCD"
						+", GLOBAL_PARENTCD, GLOBAL_GRANDPARENTCD, CALENDAR_TYPE, LINE_NUM, STATUS )"
					+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"
			);

			try { if("Y".equals(headerInd) ) reader.readNext(); } catch( Exception ignored ) {}

			while( true ) {
				Map<String, Object> recordMap = null;

				do {
					try {
						recordMap = reader.readNext( formats );
					} catch ( DataException dataEx ) {
						result.appendError( reader.getLineNumber(), handler.createDataException(dataEx.getErrorKey()) );
					}
				} while( !reader.isEOF() && reader.isBlankLine() );
				if( reader.isEOF() ) break;

				recordMap.put( "uploadCode", uploadCode );
				recordMap.put( "lineNumber", String.valueOf(reader.getLineNumber()) );
				recordMap.put( "status", STATUS_READY );
				try {
					SQLManager.bindVariables( pstmt, validableFieldSet.validate(recordMap) );
					if( pstmt.executeUpdate() > 0 ) result.increaseRowCount();
				} catch( FieldException fieldEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(fieldEx, recordMap) );
				} catch( SQLException sqlEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(sqlEx, recordMap) );
				}
			}
		} finally {
			if( pstmt != null ) pstmt.close();
			if( cstmt != null ) cstmt.close();
			result.setExecuteTimeMillis( System.currentTimeMillis() - startTimeMillis );
		}

		return result;
	}

	private DataResult customerMasterRead( DataReader reader, String uploadCode, String headerInd )
				throws DataException, IOException, SQLException {
		String[] formats = new String[] {
			  "distributorId", "customerCode", "customerName1", "customerName2", "address1", "address2", "postalCode", "contactPerson"
			, "phoneNumber", "regionCode", "provinceCity", "activateId", "customerGroup", "customerType", "customField1", "customField2"
		};
		ValidableFieldSet validableFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( false,	"uploadCode",			"DPR_UPLOADCMT_UPLOADCODE",				0, 10 )
			, new ValidableField( false,	"distributorId",		"DPR_UPLOADCMT_DISTRIBUTORID",			0, 15 )
			, new ValidableField( false,	"customerCode",			"DPR_UPLOADCMT_CUSTOMERCODE",			0, 15 )
			, new ValidableField( false,	"customerName1",		"DPR_UPLOADCMT_CUSTOMERNAME1",			0, 200 )
			, new ValidableField( true,		"customerName2",		"DPR_UPLOADCMT_CUSTOMERNAME2",			0, 200 )
			, new ValidableField( false,	"customerGroup",		"DPR_UPLOADCMT_CUSTOMERGROUP",			0, 8 )
			, new ValidableField( true,		"customerType",			"DPR_UPLOADCMT_CUSTOMERTYPE",			0, 3 )
			, new ValidableField( true,		"address1",				"DPR_UPLOADCMT_ADDRESS1",				0, 500 )
			, new ValidableField( true,		"address2",				"DPR_UPLOADCMT_ADDRESS2",				0, 500 )
			, new ValidableField( true,		"postalCode",			"DPR_UPLOADCMT_POSTALCODE",				0, 8 )
			, new ValidableField( true,		"contactPerson",		"DPR_UPLOADCMT_CONTACTPERSON",			0, 40 )
			, new ValidableField( true,		"phoneNumber",			"DPR_UPLOADCMT_PHONENUMBER",			0, 50 )
			, new ValidableField( true,		"regionCode",			"DPR_UPLOADCMT_REGIONCODE",				0, 8 )
			, new ValidableField( true,		"provinceCity",			"DPR_UPLOADCMT_PROVENCECITY",			0, 20 )
			, new ValidableField( true,		"activeId",				"DPR_UPLOADCMT_ACTIVEID",				0, 2 )
			, new ValidableField( true,		"customField1",			"DPR_UPLOADCMT_CUSTOMFIELD1",			0, 20 )
			, new ValidableField( true,		"customField2",			"DPR_UPLOADCMT_CUSTOMFIELD2",			0, 20 )
			, new ValidableField( false,	"lineNumber",			"DPR_UPLOADCMT_LINENUMBER",				ValidableField.TYPE_INTEGER, 0, 99999999 )
			, new ValidableField( false,	"status",				"DPR_UPLOADCMT_STATUS",					0, 2 )
		} );

		DataResult result = new DataResult();
		PreparedStatement pstmt = null;
		long startTimeMillis = System.currentTimeMillis();
		try {
			java.sql.Connection conn = handler.getConnection();

			pstmt = conn.prepareStatement(
				"INSERT INTO DPR_UPLOAD_CMT( UPLOADCD, DISTRIBUTOR_ID, CUSTOMER_CD, CUSTOMER_NAME1, CUSTOMER_NAME2, CUSTOMER_GROUP"
						+", CUSTOMER_TYPE, ADDRESS1, ADDRESS2, POSTAL_CD, CONTACT_PERSON, PHONE_NUMBER, REGION_CD, PROVINCE_CITY, ACTIVE_ID"
						+", CUSTOM_FIELD1, CUSTOM_FIELD2, LINE_NUM, STATUS )"
					+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"
			);

			try { if("Y".equals(headerInd) ) reader.readNext(); } catch( Exception ignored ) {}

			while( true ) {
				Map<String, Object> recordMap = null;

				do {
					try {
						recordMap = reader.readNext( formats );
					} catch ( DataException dataEx ) {
						result.appendError( reader.getLineNumber(), handler.createDataException(dataEx.getErrorKey()) );
					}
				} while( !reader.isEOF() && reader.isBlankLine() );
				if( reader.isEOF() ) break;

				recordMap.put( "uploadCode", uploadCode );
				recordMap.put( "lineNumber", String.valueOf(reader.getLineNumber()) );
				recordMap.put( "status", STATUS_READY );
				try {
					SQLManager.bindVariables( pstmt, validableFieldSet.validate(recordMap) );
					if( pstmt.executeUpdate() > 0 ) result.increaseRowCount();
				} catch( FieldException fieldEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(fieldEx, recordMap) );
				} catch( SQLException sqlEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(sqlEx, recordMap) );
				}
			}
		} finally {
			if( pstmt != null ) pstmt.close();
			result.setExecuteTimeMillis( System.currentTimeMillis() - startTimeMillis );
		}
		return result;
	}

	private DataResult inventoryRead( DataReader reader, String uploadCode, String headerInd )
				throws DataException, IOException, SQLException {
		String[] formats = new String[] {
			  "distributorId", "invDate", "distributorWarehouse", "itemCode", "gtin", "description"
			, "unitPrice", "stockQty", "onOrderQty", "committedQty", "uom", "totalAmount", "customField1", "customField2"
		};
		ValidableFieldSet validableFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( false,	"uploadCode",			"DPR_UPLOADINV_UPLOADCODE",				0, 10 )
			, new ValidableField( false,	"distributorId",		"DPR_UPLOADINV_DISTRIBUTORID",			0, 15 )
			, new ValidableField( false,	"invDate",				"DPR_UPLOADINV_INVDATE",				ValidableField.TYPE_DATE )
			, new ValidableField( false,	"distributorWarehouse",	"DPR_UPLOADINV_DISTWAREHOUSE",			0, 40 )
			, new ValidableField( false,	"itemCode",				"DPR_UPLOADINV_ITEMCODE",				0, 20 )
			, new ValidableField( true,		"gtin",					"DPR_UPLOADINV_GTIN",					0, 14 )
			, new ValidableField( true,		"description",			"DPR_UPLOADINV_DESCRIPTION",			0, 200 )
			, new ValidableField( true,		"unitPrice",			"DPR_UPLOADINV_UNITPRICE",				ValidableField.TYPE_DOUBLE )
			, new ValidableField( false,	"stockQty",				"DPR_UPLOADINV_STOCKQTY",				ValidableField.TYPE_DOUBLE )
			, new ValidableField( true,		"onOrderQty",			"DPR_UPLOADINV_ONORDERQTY",				ValidableField.TYPE_DOUBLE )
			, new ValidableField( true,		"committedQty",			"DPR_UPLOADINV_COMMITTEDQTY",			ValidableField.TYPE_DOUBLE )
			, new ValidableField( false,	"uom",					"DPR_UPLOADINV_UOM",					0, 3 )
			, new ValidableField( true,		"totalAmount",			"DPR_UPLOADINV_TOTALAMOUNT",			ValidableField.TYPE_DOUBLE )
			, new ValidableField( true,		"customField1",			"DPR_UPLOADINV_CUSTOMFIELD1",			0, 200 )
			, new ValidableField( true,		"customField2",			"DPR_UPLOADINV_CUSTOMFIELD2",			0, 200 )
			, new ValidableField( false,	"lineNumber",			"DPR_UPLOADINV_LINENUMBER",				ValidableField.TYPE_INTEGER, 0, 99999999 )
			, new ValidableField( false,	"status",				"DPR_UPLOADINV_STATUS",					0, 2 )
		} );

		DataResult result = new DataResult();
		PreparedStatement pstmt = null;
		long startTimeMillis = System.currentTimeMillis();
		try {
			java.sql.Connection conn = handler.getConnection();

			pstmt = conn.prepareStatement(
				"INSERT INTO DPR_UPLOAD_INV(UPLOADCD, DISTRIBUTOR_ID, INV_DATE, DIST_WAREHOUSE, ITEMCD, GTIN, DESCRIPTION"
						+", UNIT_PRICE, STOCK_QTY, ONORDER_QTY, COMMITTED_QTY, UOM, TOTAL_AMOUNT"
						+", CUSTOM_FIELD1, CUSTOM_FIELD2, LINE_NUM, STATUS)"
					+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"
			);

			try { if("Y".equals(headerInd) ) reader.readNext(); } catch( Exception ignored ) {}

			while( true ) {
				Map<String, Object> recordMap = null;

				do {
					try {
						recordMap = reader.readNext( formats );
					} catch ( DataException dataEx ) {
						result.appendError( reader.getLineNumber(), handler.createDataException(dataEx.getErrorKey()) );
					}
				} while( !reader.isEOF() && reader.isBlankLine() );
				if( reader.isEOF() ) break;

				convertDate( recordMap, new String[] { "invDate" } );
				recordMap.put( "uploadCode", uploadCode );
				recordMap.put( "lineNumber", String.valueOf(reader.getLineNumber()) );
				recordMap.put( "status", STATUS_READY );
				try {
					SQLManager.bindVariables( pstmt, validableFieldSet.validate(recordMap) );
					if( pstmt.executeUpdate() > 0 ) result.increaseRowCount();
				} catch( FieldException fieldEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(fieldEx, recordMap) );
				} catch( SQLException sqlEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(sqlEx, recordMap) );
				}
			}
		} finally {
			if( pstmt != null ) pstmt.close();
			result.setExecuteTimeMillis( System.currentTimeMillis() - startTimeMillis );
		}

		return result;
	}

	private DataResult orderDetailRead( DataReader reader, Map<String, ? extends Object> parameterMap, String headerInd, String allowUOM )
			throws DataException, IOException, SQLException {

		/* Ignore FIeld
			productHierarchyCode, productHierarchyName, itemName, unitPrice, formatPCQty, formatDozenQty
			, originalOrderQty, confirmedOrderQty, orderAmount
		String[] formats = new String[] {
			"productHierarchyCode", "productHierarchyName", "itemCode", "itemName"
			, "orderLineNumber", "unitPrice", "uom", "formatPCQty", "formatDozenQty"
			, "orderQty", "originalOrderQty", "confirmedOrderQty", "orderAmount"
		};
		*/

		List<DataLoader.Validator> validators = (List<DataLoader.Validator>) parameterMap.get("validators");


		String[] fieldKeyArray = (String[]) parameterMap.get( "fieldKeyArray" );

		String organizationCode = (String) parameterMap.get( "organizationCode" );
		String uomValues_enabled = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "uom;"+ organizationCode, "CSE,DZ,PC" );


		ValidableFieldSet validableFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( false,	"uploadCode",			"DPR_UPLOADORD_UPLOADCODE",				0, 10 )
			, new ValidableField( false,	"orderKey",				"DPR_UPLOADORD_ORDERKEY",				0, 20 )
			, new ValidableField( false,	"itemCode",				"DPR_UPLOADORD_ITEMCODE",				0, 20 )
			, new ValidableField( true,		"orderLineNumber",		"DPR_UPLOADORD_ORDER_LINE_NO",			ValidableField.TYPE_INTEGER, 0, 999999999 )
			, new ValidableField( true,		"uom",					"DPR_UPLOADORD_UOM",					0, 3 )
			, new ValidableField( true,		"uploadOrderQty",		"DPR_UPLOADORD_ORDERQTY",				ValidableField.TYPE_INTEGER, 0, 9999 )
			, new ValidableField( false,	"lineNumber",			"DPR_UPLOADSSL_LINENUMBER",				ValidableField.TYPE_INTEGER, 0, 99999999 )
			, new ValidableField( false,	"status",				"DPR_UPLOADORD_STATUS",					0, 2 )
		} );

		DataResult result = new DataResult();

		String uploadCode = (String)parameterMap.get( "uploadCode" );
		String orderKey = (String)parameterMap.get( "orderKey" );

		PreparedStatement pstmt = null;
		long startTimeMillis = System.currentTimeMillis();
		try {
			java.sql.Connection conn = handler.getConnection();
			pstmt = conn.prepareStatement(
					"INSERT INTO DPR_UPLOAD_ORD( UPLOADCD, ORDER_KEY, ITEMCD, ORDER_LINE_NO, UOM, ORDERQTY, LINE_NUM, STATUS )"
					+ " VALUES( ?, ?, ?, ?, ?, ?, ?, ? )"
			);

			try { if("Y".equals(headerInd) ) reader.readNext(); } catch( Exception ignored ) {}

			boolean isUOM = true;
			while( true ) {
				Map<String, Object> recordMap = null;

				do {
					try {
						recordMap = reader.readNext( fieldKeyArray );
						if( !reader.isEOF() && !reader.isBlankLine() ) {
							if( validators != null && validators.size() > 0 ) {
								for( DataLoader.Validator validator : validators ) {
									try {
										if( validator != null )
											validator.validateLine(handler, recordMap);
									} catch( OrderItem.DataPassingException passWarn ) {
										recordMap = passWarn.getModifiedMap();
										result.appendWarn( reader.getLineNumber(), handler.createDataException(passWarn.getErrorKey(), passWarn.getMessage()) );
									}
								}
							}

							String uom = (String)recordMap.get( "uom" );

							if( uom != null && allowUOM != null && !allowUOM.contains(uom.toUpperCase()) ) {
								isUOM = false;
								throw handler.createDataException( "ERR_UPLOAD_ORDER_INVALID_UOM" );
							}
						}
					} catch ( DataException dataEx ) {
						result.appendError( reader.getLineNumber(), handler.createDataException(dataEx.getErrorKey(), dataEx.getMessage()) );
					}
				} while( !reader.isEOF() && reader.isBlankLine() );
				if( reader.isEOF() ) break;


				try {
					recordMap.put( "uploadCode", uploadCode );
					recordMap.put( "orderKey", orderKey );
					recordMap.put( "lineNumber", String.valueOf(reader.getLineNumber()) );
					recordMap.put( "status", STATUS_READY );

					SQLManager.bindVariables( pstmt, validableFieldSet.validate(recordMap) );
					if( isUOM && pstmt.executeUpdate() > 0 ) result.increaseRowCount();
				} catch( FieldException fieldEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(fieldEx, recordMap) );
				} catch( SQLException sqlEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(sqlEx, recordMap) );
				}
			}
		} finally {
			if( pstmt != null ) try { pstmt.close(); } catch( Exception ex ) {}
			result.setExecuteTimeMillis( System.currentTimeMillis() - startTimeMillis );

			if( validators != null && validators.size() > 0 ) {
				for( DataLoader.Validator validator : validators ) {
					if( validator != null )
						validator.close();
				}
			}
		}

		return result;
	}

	private DataResult sellingSkuListRead( DataReader reader, String uploadCode, String type, String updateUserId, String headerInd )
				throws DataException, IOException, SQLException {
		String[] formats = new String[] { "itemCode", "officeCode", "groupCode", "districtCode", "distributorCode", "startDate", "endDate", "promotionInd", "newItemInd" };
		ValidableFieldSet validableFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( false,	"uploadCode",			"DPR_UPLOADSSL_UPLOADCODE",				0, 10 )
			, new ValidableField( false,	"itemCode",				"DPR_UPLOADSSL_ITEMCODE",				0, 20 )
			, new ValidableField( true,		"officeCode",			"DPR_UPLOADSSL_OFFICECODE",				0, 8 )
			, new ValidableField( true,		"groupCode",			"DPR_UPLOADSSL_GROUPCODE",				0, 8 )
			, new ValidableField( true,		"districtCode",			"DPR_UPLOADSSL_DISTRICTCODE",			0, 8 )
			, new ValidableField( true,		"distributorCode",		"DPR_UPLOADSSL_DISTRIBUTORCODE",		0, 15 )
			, new ValidableField( false,	"startDate",			"DPR_UPLOADSSL_STARTDATE",				ValidableField.TYPE_DATE )
			, new ValidableField( false,	"endDate",				"DPR_UPLOADSSL_ENDDATE",				ValidableField.TYPE_DATE )
			, new ValidableField( true,		"promotionInd",			"DPR_UPLOADSSL_PROMOTIONIND",			"PUB_WHETHER_", "Y,N" )
			, new ValidableField( true,		"newItemInd",			"DPR_UPLOADSSL_NEWITEMIND",				"PUB_WHETHER_", "Y,N" )
			, new ValidableField( false,	"lineNumber",			"DPR_UPLOADSSL_LINENUMBER",				ValidableField.TYPE_INTEGER, 0, 99999999 )
			, new ValidableField( false,	"status",				"DPR_UPLOADSSL_STATUS",					0, 2 )
		} );

		DataResult result = new DataResult();
		PreparedStatement pstmt = null;
		CallableStatement cstmt = null;
		long startTimeMillis = System.currentTimeMillis();
		try {
			java.sql.Connection conn = handler.getConnection();
			ResultSet result_header = null;
			try {
				pstmt = conn.prepareStatement(
						"SELECT COUNTRYCD, ORGANIZATIONCD FROM DPR_UPLOAD_HEADER WHERE UPLOAD_CD = ?" );
				pstmt.setObject( 1, uploadCode );

				result_header = pstmt.executeQuery();
				if( result_header.next() ) {
				}
			} finally {
				if( result_header != null ) try { result_header.close(); } catch( Exception ex ) {}
				if( pstmt != null )  try { pstmt.close(); } catch( Exception ex ) {}
			}

			pstmt = conn.prepareStatement(
				"INSERT INTO DPR_UPLOAD_SSL( UPLOADCD, ITEMCD, OFFICECD, GROUPCD, DISTRICTCD, DISTRIBUTOR_CD"
						+", START_DATE, END_DATE, PROMOTION_IND, NEWITEM_IND, LINE_NUM, STATUS )"
					+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"
			);

			try { if("Y".equals(headerInd) ) reader.readNext(); } catch( Exception ignored ) {}

			while( true ) {
				Map<String, Object> recordMap = null;

				do {
					try {
						recordMap = reader.readNext( formats );

					} catch ( DataException dataEx ) {
						result.appendError( reader.getLineNumber(), handler.createDataException(dataEx.getErrorKey()) );
					}
				} while( !reader.isEOF() && reader.isBlankLine() );
				if( reader.isEOF() ) break;

				try {
					recordMap.put( "uploadCode", uploadCode );
					recordMap.put( "lineNumber", String.valueOf(reader.getLineNumber()) );
					recordMap.put( "status", STATUS_READY );
					convertDate( recordMap, new String[] { "startDate", "endDate" } );

					SQLManager.bindVariables( pstmt, validableFieldSet.validate(recordMap) );
					if( pstmt.executeUpdate() > 0 ) result.increaseRowCount();
				} catch( FieldException fieldEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(fieldEx, recordMap) );
				} catch( SQLException sqlEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(sqlEx, recordMap) );
				}
			}
		} finally {
			if( pstmt != null ) pstmt.close();
			if( cstmt != null ) cstmt.close();
			result.setExecuteTimeMillis( System.currentTimeMillis() - startTimeMillis );
		}

		return result;
	}

	private DataResult selloutRead( DataReader reader, String uploadCode, String headerInd )
				throws DataException, IOException, SQLException {
		String[] formats = new String[] {
			  "distributorId", "sellOutCode", "sellOutDate", "sellOutType", "customerCode", "distributorWarehouse", "itemCode"
			, "gtin", "description", "price", "sellOutQty", "uom", "totalPrice", "discountPrice", "totalDiscountPrice"
			, "bottomlineDiscountPrice", "totalAmount", "totalAmountTax", "salesEmployee"
			, "customField1", "customField2", "customField3"
		};
		ValidableFieldSet validableFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( false,	"uploadCode",			"DPR_UPLOADSEO_UPLOADCODE",				0, 10 )
			, new ValidableField( false,	"distributorId",		"DPR_UPLOADSEO_DISTRIBUTORID",			0, 15 )
			, new ValidableField( false,	"itemCode",				"DPR_UPLOADSEO_ITEMCODE",				0, 20 )
			, new ValidableField( false,	"sellOutCode",			"DPR_UPLOADSEO_SELLOUTCODE",			0, 40 )
			, new ValidableField( false,	"sellOutDate",			"DPR_UPLOADSEO_SELLOUTDATE",			ValidableField.TYPE_DATE )
			, new ValidableField( true,		"sellOutType",			"DPR_UPLOADSEO_SELLOUTTYPE",			0, 2 )
			, new ValidableField( false,	"customerCode",			"DPR_UPLOADSEO_CUSTOMERCODE",			0, 15 )
			, new ValidableField( true,		"gtin",					"DPR_UPLOADSEO_GTIN",					0, 14 )
			, new ValidableField( true,		"distributorWarehouse",	"DPR_UPLOADSEO_DISTWAREHOUSE",			0, 40 )
			, new ValidableField( true,		"description",			"DPR_UPLOADSEO_DESCRIPTION",			0, 200 )
			, new ValidableField( true,		"salesEmployee",		"DPR_UPLOADSEO_SALESEMPLOYEE",			0, 50 )
			, new ValidableField( true,		"price",				"DPR_UPLOADSEO_PRICE",					ValidableField.TYPE_DOUBLE )
			, new ValidableField( false,	"sellOutQty",			"DPR_UPLOADSEO_SELLOUTQTY",				ValidableField.TYPE_DOUBLE )
			, new ValidableField( true,		"uom",					"DPR_UPLOADSEO_UOM",					0, 3 )
			, new ValidableField( true,		"totalPrice",			"DPR_UPLOADSEO_TOTALPRICE",				ValidableField.TYPE_DOUBLE )
			, new ValidableField( true,		"discountPrice",		"DPR_UPLOADSEO_DISCOUNTPRICE",			ValidableField.TYPE_DOUBLE )
			, new ValidableField( true,		"totalDiscountPrice",	"DPR_UPLOADSEO_TOTALDISCOUNTPRICE",		ValidableField.TYPE_DOUBLE )
			, new ValidableField( true,		"bottomlineDiscountPrice","DPR_UPLOADSEO_BOTTOMLINEDISCOUNTPRICE",ValidableField.TYPE_DOUBLE )
			, new ValidableField( false,	"totalAmount",			"DPR_UPLOADSEO_TOTALAMOUNT",			ValidableField.TYPE_DOUBLE )
			, new ValidableField( true,		"totalAmountTax",		"DPR_UPLOADSEO_TOTALAMOUNTTAX",			ValidableField.TYPE_DOUBLE )
			, new ValidableField( true,		"customField1",			"DPR_UPLOADSEO_CUSTOMFIELD1",			0, 200 )
			, new ValidableField( true,		"customField2",			"DPR_UPLOADSEO_CUSTOMFIELD2",			0, 200 )
			, new ValidableField( true,		"customField3",			"DPR_UPLOADSEO_CUSTOMFIELD3",			0, 200 )
			, new ValidableField( false,	"lineNumber",			"DPR_UPLOADSEO_LINENUMBER",				ValidableField.TYPE_INTEGER, 0, 99999999 )
			, new ValidableField( false,	"status",				"DPR_UPLOADSEO_STATUS",					0, 2 )
		} );

		DataResult result = new DataResult();
		PreparedStatement pstmt = null;
		long startTimeMillis = System.currentTimeMillis();
		try {
			java.sql.Connection conn = handler.getConnection();

			pstmt = conn.prepareStatement(
				"INSERT INTO DPR_UPLOAD_SEO(UPLOADCD, DISTRIBUTOR_ID, ITEMCD, SELLOUT_CD, SELLOUT_DATE, SELLOUT_TYPE, CUSTOMER_CD"
						+", GTIN, DIST_WAREHOUSE, DESCRIPTION, SALES_EMPLOYEE, PRICE, SELLOUT_QTY, SELLOUT_UOM, TOTAL_PRICE"
						+", DISCOUNT_PRICE, TOTAL_DISCOUNT_PRICE, BOTTOMLINE_DISCOUNT_PRICE, TOTAL_AMOUNT, TOTAL_AMOUNT_TAX"
						+", CUSTOM_FIELD1, CUSTOM_FIELD2, CUSTOM_FIELD3, LINE_NUM, STATUS)"
					+" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )"
			);

			try { if("Y".equals(headerInd) ) reader.readNext(); } catch( Exception ignored ) {}

			while( true ) {
				Map<String, Object> recordMap = null;

				do {
					try {
						recordMap = reader.readNext( formats );
					} catch ( DataException dataEx ) {
						result.appendError( reader.getLineNumber(), handler.createDataException(dataEx.getErrorKey()) );
					}
				} while( !reader.isEOF() && reader.isBlankLine() );
				if( reader.isEOF() ) break;

				convertDate( recordMap, new String[] { "sellOutDate" } );
				recordMap.put( "uploadCode", uploadCode );
				recordMap.put( "lineNumber", String.valueOf(reader.getLineNumber()) );
				recordMap.put( "status", STATUS_READY );
				try {
					SQLManager.bindVariables( pstmt, validableFieldSet.validate(recordMap) );
					if( pstmt.executeUpdate() > 0 ) result.increaseRowCount();
				} catch( FieldException fieldEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(fieldEx, recordMap) );
				} catch( SQLException sqlEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(sqlEx, recordMap) );
				}
			}
		} finally {
			if( pstmt != null ) pstmt.close();
			result.setExecuteTimeMillis( System.currentTimeMillis() - startTimeMillis );
		}

		return result;
	}

	private DataResult skuMappingRead( DataReader reader, String uploadCode, String updateUserId, String headerInd )
				throws DataException, IOException, SQLException {
		String[] formats = new String[] { "distributorCode", "description", "itemCode", "itemName", "productCode" };
		ValidableFieldSet validableFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( false,	"uploadCode",			"DPR_UPLOADSKM_UPLOADCODE",				0, 10 )
			, new ValidableField( false,	"distributorCode",		"DPR_UPLOADSKM_DISTRIBUTORCODE",		0, 15 )
			, new ValidableField( true,		"description",			"DPR_UPLOADSKM_DESCRIPTION",			0, 200 )
			, new ValidableField( false,	"itemCode",				"DPR_UPLOADSKM_ITEMCODE",				0, 20 )
			, new ValidableField( true,		"itemName",				"DPR_UPLOADSKM_ITEMNAME",				0, 200 )
			, new ValidableField( false,	"productCode",			"DPR_UPLOADSKM_PRODUCTCODE",			0, 20 )
			, new ValidableField( false,	"lineNumber",			"DPR_UPLOADSKM_LINENUMBER",				ValidableField.TYPE_INTEGER, 0, 99999999 )
			, new ValidableField( false,	"status",				"DPR_UPLOADSKM_STATUS",					0, 2 )
		} );

		DataResult result = new DataResult();
		PreparedStatement pstmt = null;
		CallableStatement cstmt = null;
		long startTimeMillis = System.currentTimeMillis();
		try {
			java.sql.Connection conn = handler.getConnection();

			pstmt = conn.prepareStatement(
				"INSERT INTO DPR_UPLOAD_SKM( UPLOADCD, DISTRIBUTOR_CD, DIST_DESCRIPTION, ITEMCD, ITEM_DESCRIPTION, DIST_ITEMCD, LINE_NUM, STATUS )"
					+" VALUES( ?, ?, ?, ?, ?, ?, ?, ? )"
			);

			try { if("Y".equals(headerInd) ) reader.readNext(); } catch( Exception ignored ) {}

			while( true ) {
				Map<String, Object> recordMap = null;

				do {
					try {
						recordMap = reader.readNext( formats );
					} catch ( DataException dataEx ) {
						result.appendError( reader.getLineNumber(), handler.createDataException(dataEx.getErrorKey()) );
					}
				} while( !reader.isEOF() && reader.isBlankLine() );
				if( reader.isEOF() ) break;

				recordMap.put( "uploadCode", uploadCode );
				recordMap.put( "lineNumber", String.valueOf(reader.getLineNumber()) );
				recordMap.put( "status", STATUS_READY );
				try {
					SQLManager.bindVariables( pstmt, validableFieldSet.validate(recordMap) );
					if( pstmt.executeUpdate() > 0 ) result.increaseRowCount();
				} catch( FieldException fieldEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(fieldEx, recordMap) );
				} catch( SQLException sqlEx ) {
					result.appendError( reader.getLineNumber(), handler.createDataException(sqlEx, recordMap) );
				}
			}
		} finally {
			if( pstmt != null ) pstmt.close();
			if( cstmt != null ) cstmt.close();
			result.setExecuteTimeMillis( System.currentTimeMillis() - startTimeMillis );
		}

		return result;
	}
}
