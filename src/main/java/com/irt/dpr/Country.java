/*
 *	File Name:	Country.java
 *	Version:	2.2.6
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.6	HOCOGSP 추가
 *	hankalam	2020/06/30		2.2.5	getFeatureValue() 추가
 *	jbaek		2019/11/30		2.2.4	Multiple Country Manager 기능 추가
 *	jbaek		2018/10/30		2.2.3	isFeature(), getDefault(), getSetting() 추가. getCountrykeyFromOrgCode(), isChinaCountry() 삭제
 *	jbaek		2017/09/30		2.2.2	getCountryKeyFromOrgCode(), getCountryKeyFromPartyId() 추가
 *	jbaek		2011/11/30		2.2.1	isChinaCountry()에 HoCo조직 추가
 *	lsinji		2008/09/26		2.2.0	create
 *
**/

package com.irt.dpr;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.rbm.RBMSystem;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;

/*
 *
 */
public class Country extends com.irt.rbm.ManipulableManagerImpl {
	public final static int INFO_AUTH					= 0x00000001;
	public final static int INFO_COUNTRY				= 0x00000002;
	public final static int INFO_ALL					= 0x00000003;

	public final static int IDX_AUTH					= 0;
	public final static int IDX_COUNTRY					= 1;

	public final static String STATUS_NORMAL			= "00";
	public final static String STATUS_STOP				= "99";

	public final static String CHINA_ORGNAZATION		= "1000";
	public final static String HOCO_ORGANIZATION		= "1500";
	public final static String HOCOGSP_ORGANIZATION		= "1588";
	public final static String SJJP_ORGNAZATION			= "1800";
	public final static String SHANGHAI_ORGNAZATION		= "1900";
	public final static String THAILAND_ORGNAZATION		= "2400";
	public final static String VIETNAM_ORGANIZATION		= "260S";
	public final static String KOREA_ORGANIZATION		= "320S";

	private final static Table[] tables = new Table[] {
		  Schema.findTable( Schema.DPR_COUNTRY_AUTH )
		, Schema.findTable( Schema.DPR_COUNTRY )
	};

	private final static QueryFactory[] factories = new QueryFactory[] {
		  Schema.findQueryFactory( Schema.DPR_COUNTRY_AUTH )
		, Schema.findQueryFactory( Schema.DPR_COUNTRY )
	};

	public Country( SQLHandler handler ) {
		super( handler, tables[IDX_COUNTRY], factories[IDX_COUNTRY] );
	}

	public static Map<String, Object> createPrimary( String countryCode ) {
		return Record.createMap( "countryCode", countryCode );
	}

	public Object createCountryCode() throws SQLException {
		return SQLManager.getObjectValue( handler, "SELECT seqDPR_COUNTRY.nextval from dual" );
	}

	public Object getCountryName( String countryCode ) throws SQLException {
		return SQLManager.getObjectValue( handler, "SELECT COUNTRYNAME FROM DPR_COUNTRY WHERE COUNTRY_CD = ?" , countryCode );
	}

	public boolean delete( Map<String, Object> primaryMap, int infoType, boolean isDeleteAll ) throws DataException, SQLException {
		if( (infoType & INFO_COUNTRY) > 0 ) return delete( primaryMap );

		Object[] primaryVars = getAuthPrimaryVariables( primaryMap );
		int ret = 0;
		if( (infoType & INFO_AUTH ) > 0 && primaryVars != null && primaryVars.length > 0 ) {
			if( isDeleteAll ) {
				ret = SQLManager.executeStatement( handler, "DELETE FROM DPR_COUNTRY_AUTH WHERE COUNTRYCD = ?", primaryVars );
			} else if( primaryVars.length > 1 ) {
				ret = SQLManager.executeStatement( handler, "DELETE FROM DPR_COUNTRY_AUTH WHERE COUNTRYCD = ? AND UNIQID = ?", primaryVars );
			}
		}

		return ( ret > 0 );
	}

	private Object[] getAuthPrimaryVariables( Map<String, Object> primaryMap ) {
		String[] primaryKeys = new String[] { "countryCode", "authUniqId" };

		List<Object> list = new java.util.LinkedList<Object>();
		for( String key : primaryKeys ) {
			Object obj = primaryMap.get( key );
			if( obj instanceof String && obj != null )
				list.add( obj );
		}

		return list.toArray();
	}

	public static String getDefault( String organizationCode, String envKey ) {
		String value = RBMSystem.getSystemEnv( "DPR", "Default#" + organizationCode +";"+ envKey );
		if( value != null ) {
			return value;
		} else {
			return RBMSystem.getSystemEnv( "DPR", "Default;"+ envKey );
		}
	}

	public static String getSetting( String organizationCode, String envKey ) {
		String value = RBMSystem.getSystemEnv( "DPR", "Setting#" + organizationCode +";"+ envKey );
		if( value != null ) {
			return value;
		}

		return null;
	}

	public static boolean isFeature( String organizationCode, String featureKey ) {
		if( organizationCode == null )
			return false;

		if( featureKey != null && !featureKey.startsWith("use") ) {
			Logger.getRootLogger().warn("Better to starts with 'use' for 'featureKey' value.");
		}

		return RBMSystem.getSystemEnvBool( "DPR", "Feature#" + organizationCode +";"+ featureKey, false );
	}

	public static String getFeatureValue( String organizationCode, String featureKey ) {
		return getFeatureValue( organizationCode, featureKey, null );
	}

	public static String getFeatureValue( String organizationCode, String featureKey, String defaultValue ) {
		if( organizationCode == null )
			return null;

		return RBMSystem.getSystemEnv( "DPR", "Feature#" + organizationCode +";"+ featureKey, defaultValue );
	}

	public Map<String, Object> getDefaultHierarchyCondition( Object countryCode ) throws SQLException {
		return SQLManager.getRecordMap( handler, null,
			"SELECT DEF_HIERARCHY_LEVEL \"defaultHierarchyLevel\", HERARCHY_COND \"hierarchyCondition\""
					+ "  FROM DPR_COUNTRY"
					+ " WHERE COUNTRY_CD = ?", new Object[] { countryCode } );
	}

	@Override
	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap ) throws SQLException {
		return getRecord( primaryMap, INFO_ALL );
	}

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, int infoType ) throws SQLException {
		Map<String, Object> recordMap = super.getRecord( primaryMap );
		if( recordMap == null ) return null;

		if( (infoType & INFO_AUTH) > 0 ) {
			QueryBuffer querybuf = factories[IDX_AUTH].setDataQuery( new QueryBuffer() );
			querybuf.appendCondition( "CAUT.COUNTRYCD = ?", Record.extractValue(recordMap, "countryCode") );
			querybuf.appendOrderByFieldName( "createDateTime" );

			recordMap.put( "countryAuthList", SQLManager.getRecordList(handler, querybuf) );
		}

		return recordMap;
	}

	/**
	 * @param partyId
	 * @return second part of partyId string ( eg. 'JNJAP_CN' -> 'CN' )
	 */
	public static String getCountryKeyFromPartyId( String partyId ) {
		String countryKey = "";
		if( partyId != null ) {
			countryKey = partyId.split("_")[1];
		}
		return countryKey;
	}

	public boolean update( Map<String, Object> recordMap ) throws DataException, SQLException {
		return update( recordMap, INFO_ALL, false );
	}

	public boolean update( Map<String, Object> recordMap, int infoType ) throws DataException, SQLException {
		return update( recordMap, infoType, false );
	}

	public boolean update( Map<String, Object> recordMap, int infoType, boolean inserting ) throws DataException, SQLException {
		Object[] primaryVars = Record.extractValues( recordMap, new String[] { "countryCode" } );

		if( (infoType & INFO_COUNTRY) > 1 ) {
			if( inserting )
				SQLManager.manageRecord( handler, tables[IDX_COUNTRY], recordMap, Record.INSERT );
			else
				SQLManager.manageRecord( handler, tables[IDX_COUNTRY], recordMap, Record.UPDATE );
		}

		String systemDateTime = (String)SQLManager.getObjectValue( handler,
			"SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM DPR_COUNTRY WHERE COUNTRY_CD = ?"
		, primaryVars );
		if( systemDateTime == null ) return false;
		Object[] bindVars = new Object[] { primaryVars[0], systemDateTime };

		if( (infoType & INFO_AUTH) > 0 ) {
			int statement = ( inserting ? Record.INSERT : (Record.INSERT | Record.UPDATE) );
			if( recordMap.get("countryAuthList") != null ) {
				java.util.List<Map<String,Object>> countryAuthList = new java.util.ArrayList((Collection)recordMap.get("countryAuthList"));
				for( int i = 0; i< countryAuthList.size(); i++ ) {
					recordMap.put("authUniqId", countryAuthList.get(i).get("authUniqId"));
					recordMap.put("countryAuthList", countryAuthList.subList(i, i+1));
					updateTable( tables[IDX_AUTH], recordMap, new String[] { "countryCode", "authUniqId" }, "countryAuthList", statement );
				}
				if( !inserting ) {
					String sql = "DELETE FROM DPR_COUNTRY_AUTH WHERE COUNTRYCD = ? AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";
					SQLManager.executeStatement( handler, sql, bindVars );
				}
			}
		}

		return true;
	}
}
