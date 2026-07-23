/*
 *	File Name:	Item.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	getDefaultRecord(): TimeZone이 null일 경우 발생하는 오류 수정
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										INFO_BASE 추가
 *										ItemQuery() 변경사항 적용
 *										getCheckDigit() -> calculateCheckDigit()
 *										createItem(), getDefaultRecord() 추가
 *										getRecords(): appendMultiValue() 포함
 *	stghr12		2007/10/31		2.1.2	getItemInfoType() 추가
 *										getItemMeasure() 오류수정: itemUnit 조건 추가
 *	stghr12		2007/04/30		2.1.1	createPrimary(): java.util.HashMap -> java.util.TreeMap
 *										appendMultiValue(): INFO_PRIVATEGLN Query 오류 수정
 *										checkUniqueness() 추가
 *										modify( recordMap, infoType, fieldKeys ), modifyAll( records, infoType, fieldKeys ) 추가
 *	stghr12		2006/12/01		2.1.0	ManipulableManager 변경사항 적용
 *										deleteAll(): deleteEach() 사용으로 수정
 *										modifyAll(): modifyEach() 사용으로 수정
 *										registAll(): registEach() 사용으로 수정
 *										RBMDataManager.updateTable() 변경사항 적용
 *										Table.STATEMENT_* -> Record.*
 *	stghr12		2006/03/31		2.0.0	version up
 *	stghr12		2004/02/29		1.0.0	create
 *
 **/

package com.irt.rbm.ecs;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.rbm.RBMSystem;
import com.irt.rbm.usr.UserEnvironment;
import com.irt.sql.*;
import java.sql.*;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class Item extends com.irt.rbm.ManipulableManagerImpl {
	public final static int INFO_ITEM					= 0x00000001;
	public final static int INFO_ITEMINFO				= 0x00000002;
	public final static int INFO_HIERARCHY				= 0x00000004;
	public final static int INFO_MEASURE				= 0x00000008;
	public final static int INFO_PACKAGING				= 0x00000010;
	public final static int INFO_HANDLING				= 0x00000020;
	public final static int INFO_ORDERING				= 0x00000040;
	public final static int INFO_FASHION				= 0x00000080;
	public final static int INFO_ATTRIBUTE				= 0x00000100;

	public final static int INFO_LINK					= 0x00001000;
	public final static int INFO_ORIGIN					= 0x00002000;
	public final static int INFO_DESCRIPTION			= 0x00010000;
	public final static int INFO_DESCRIPTION_ALL		= 0x00020000;
	public final static int INFO_MEASUREUNIT			= 0x00040000;
	public final static int INFO_SEASON					= 0x00080000;

	public final static int INFO_MANUFGLN				= 0x00100000;
	public final static int INFO_PRIVATEGLN				= 0x00200000;

	public final static int INFO_BASE					= 0x0000007F;
	public final static int INFO_ALL					= 0x003F31FF;

	private final static int IDX_ITEM					= 7;
	private final static int IDX_ITEMINFO				= 8;
	private final static int IDX_HIERARCHY				= 9;
	private final static int IDX_MEASURE				= 10;
	private final static int IDX_PACKAGING				= 11;
	private final static int IDX_HANDLING				= 12;
	private final static int IDX_ORDERING				= 13;
	private final static int IDX_FASHION				= 14;
	private final static int IDX_ATTRIBUTE				= 15;
	private final static int IDX_LINK					= 0;
	private final static int IDX_ORIGIN					= 1;
	private final static int IDX_DESCRIPTION			= 2;
	private final static int IDX_MEASUREUNIT			= 3;
	private final static int IDX_SEASON					= 4;
	private final static int IDX_MANUFGLN				= 5;
	private final static int IDX_PRIVATEGLN				= 6;

	private final static Table[] tables = new Table[] {
			Schema.findTable( Schema.ECS_ITEM_LINK )
			, Schema.findTable( Schema.ECS_ITEM_ORIGIN )
			, Schema.findTable( Schema.ECS_ITEM_DESCRIPTION )
			, Schema.findTable( Schema.ECS_ITEM_MEASUREUNIT )
			, Schema.findTable( Schema.ECS_ITEM_SEASON )
			, Schema.findTable( Schema.ECS_ITEM_MANUFGLN )
			, Schema.findTable( Schema.ECS_ITEM_PRIVATEGLN )
			, Schema.findTable( Schema.ECS_ITEM )
			, Schema.findTable( Schema.ECS_ITEM_INFO )
			, Schema.findTable( Schema.ECS_ITEM_HIERARCHY )
			, Schema.findTable( Schema.ECS_ITEM_MEASURE )
			, Schema.findTable( Schema.ECS_ITEM_PACKAGING )
			, Schema.findTable( Schema.ECS_ITEM_HANDLING )
			, Schema.findTable( Schema.ECS_ITEM_ORDERING )
			, Schema.findTable( Schema.ECS_ITEM_FASHION )
			, Schema.findTable( Schema.ECS_ITEM_ATTRIBUTE )
	};
	private final static QueryFactory[] factories = new QueryFactory[] {
			Schema.findQueryFactory( Schema.ECS_ITEM_LINK )
			, Schema.findQueryFactory( Schema.ECS_ITEM_ORIGIN )
			, Schema.findQueryFactory( Schema.ECS_ITEM_DESCRIPTION )
			, Schema.findQueryFactory( Schema.ECS_ITEM_MEASUREUNIT )
			, Schema.findQueryFactory( Schema.ECS_ITEM_SEASON )
			, Schema.findQueryFactory( Schema.ECS_ITEM_MANUFGLN )
			, Schema.findQueryFactory( Schema.ECS_ITEM_PRIVATEGLN )
	};
	private final static ItemQuery itemQuery = new ItemQuery( ItemQuery.ITEM );
	private final static QueryFactory factory = new QueryFactory( itemQuery );

	public Item( SQLHandler handler ) {
		super( handler, tables[IDX_ITEM], factory );
	}

	Item( SQLHandler handler, QueryFactory factory ) {
		super( handler, tables[IDX_ITEM], factory );
	}

	Item( SQLHandler handler, Table table, QueryFactory factory ) {
		super( handler, table, factory );
	}

	public static char calculateCheckDigit( String prefix ) {
		int odd = 0, even = 0;

		for( int i = 0; i < prefix.length(); i+= 2 ) {
			if( prefix.charAt(i) < '0' || prefix.charAt(i) > '9' ) return ' ';
			odd += prefix.charAt(i) - '0';
		}
		for( int i = 1; i < prefix.length(); i+= 2 ) {
			if( prefix.charAt(i) < '0' || prefix.charAt(i) > '9' ) return ' ';
			even += prefix.charAt(i) - '0';
		}
		if( prefix.length() % 2 == 0 )
			return "0987654321".charAt( (odd + even * 3) % 10 );
		else
			return "0987654321".charAt( (odd * 3 + even) % 10 );
	}

	public static boolean checkGtin( String gtin ) {
		return ( calculateCheckDigit(gtin+"0") == '0' );
	}

	public boolean checkUniqueness( String gtin ) throws SQLException {
		return ( SQLManager.getInt( handler, "SELECT COUNT(*) FROM ECS_ITEM WHERE GTIN = ?", gtin ) == 1 );
	}

	public void createItem( Map<String, Object> recordMap ) throws DataException, SQLException {
		Table table = tables[IDX_ITEM];
		ValidableField[] fields = new ValidableField[] {
				new ValidableField( false, "sourceGtin", "GTIN", Schema.STRING )
				, new ValidableField( false, "sourceGln", "GLN", Schema.STRING )
				, (ValidableField)table.getField( "gtin" )
				, (ValidableField)table.getField( "gln" )
				, (ValidableField)table.getField( "itemInfoType" )
				, (ValidableField)table.getField( "parentGln" )
				, new ValidableField( (ValidableField)table.getField("informationGln"), true, false )
				, (ValidableField)table.getField( "itemCode" )
				, new ValidableField( (ValidableField)table.getField("itemName"), true, false )
				, (ValidableField)table.getField( "publicationDate" )
				, (ValidableField)table.getField( "isInformationPrivate" )
				, (ValidableField)table.getField( "updateUserId" )
		};

		try {
			Object[] values = Record.validate( recordMap, fields );
			SQLManager.callStatement( handler, "call pkECSItem.pCreateItem( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )", values );
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		}
	}

	public static Map<String, Object> createPrimary( String gtin, String gln ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "gtin", gtin );
		primaryMap.put( "gln", gln );

		return primaryMap;
	}

	public static Map<String, Object> createPrimary( String gtin, String gln, String languageCode ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "gtin", gtin );
		primaryMap.put( "gln", gln );
		primaryMap.put( "languageCode", languageCode );

		return primaryMap;
	}

	@Override
	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		Object[] primaryVars = Record.extractValues( primaryMap, new String[] { "gtin", "gln" } );

		if( SQLManager.getObjectValue(handler, "SELECT 'x' FROM ECS_ITEM WHERE GTIN = ? AND GLN = ?", primaryVars) != null ) {
			SQLManager.callStatement( handler, "call pkECSItem.pDeleteItem( ?, ? )", primaryVars );
			return true;
		}

		return false;
	}

	public boolean delete( Map<String, Object> primaryMap, int infoType ) throws DataException, SQLException {
		if( (infoType & INFO_ITEM) > 0 ) return delete( primaryMap );

		int resultCnt = 0;
		Object[] primaryVars = Record.extractValues( primaryMap, new String[] { "gtin", "gln" } );

		if( (infoType & INFO_HIERARCHY) > 0 )
			resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_HIERARCHY WHERE GTIN = ? AND GLN = ?", primaryVars );
		if( (infoType & INFO_MEASURE) > 0 )
			resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_MEASURE WHERE GTIN = ? AND GLN = ?", primaryVars );
		if( (infoType & INFO_PACKAGING) > 0 )
			resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_PACKAGING WHERE GTIN = ? AND GLN = ?", primaryVars );
		if( (infoType & INFO_HANDLING) > 0 )
			resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_HANDLING WHERE GTIN = ? AND GLN = ?", primaryVars );
		if( (infoType & INFO_ORDERING) > 0 )
			resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_ORDERING WHERE GTIN = ? AND GLN = ?", primaryVars );
		if( (infoType & INFO_FASHION) > 0 )
			resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_FASHION WHERE GTIN = ? AND GLN = ?", primaryVars );
		if( (infoType & INFO_ATTRIBUTE) > 0 )
			resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_ATTRIBUTE WHERE GTIN = ? AND GLN = ?", primaryVars );


		if( (infoType & INFO_LINK) > 0 ) {
			if( primaryMap.containsKey("lowerLevelGtin") ) {
				Object[] bindVars = new Object[] { primaryVars[0], primaryVars[1], Record.extractValue(primaryMap, "lowerLevelGtin") };
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_LINK WHERE GTIN = ? AND GLN = ? AND LOWERGTIN = ?", bindVars );
			} else
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_LINK WHERE GTIN = ? AND GLN = ?", primaryVars );
		}

		if( (infoType & INFO_ORIGIN) > 0 ) {
			if( primaryMap.containsKey("originCode") ) {
				Object[] bindVars = new Object[] { primaryVars[0], primaryVars[1], Record.extractValue(primaryMap, "originCode") };
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_ORIGIN WHERE GTIN = ? AND GLN = ? AND ORIGINCD = ?", bindVars );
			} else
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_ORIGIN WHERE GTIN = ? AND GLN = ?", primaryVars );
		}

		if( (infoType & INFO_DESCRIPTION_ALL) > 0 )
			resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_DESC WHERE GTIN = ? AND GLN = ?", primaryVars );
		else if( (infoType & INFO_DESCRIPTION) > 0 ) {
			if( primaryMap.containsKey("languageCode") ) {
				Object[] bindVars = new Object[] { primaryVars[0], primaryVars[1], Record.extractValue(primaryMap, "languageCode") };
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_DESC WHERE GTIN = ? AND GLN = ? AND LANGCD = ?", bindVars );
			} else
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_DESC WHERE GTIN = ? AND GLN = ?", primaryVars );
		}

		if( (infoType & INFO_MEASUREUNIT) > 0 ) {
			if( primaryMap.containsKey("itemUnit") ) {
				Object[] bindVars = new Object[] { primaryVars[0], primaryVars[1], Record.extractValue(primaryMap, "itemUnit") };
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_MEASUREU WHERE GTIN = ? AND GLN = ? AND ITEMUNIT = ?", bindVars );
			} else
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_MEASUREU WHERE GTIN = ? AND GLN = ?", primaryVars );
		}

		if( (infoType & INFO_SEASON) > 0 ) {
			if( primaryMap.containsKey("seasonCode") ) {
				Object[] bindVars = new Object[] { primaryVars[0], primaryVars[1], Record.extractValue(primaryMap, "seasonCode") };
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_SEASON WHERE GTIN = ? AND GLN = ? AND SEASONCD = ?", bindVars );
			} else
				resultCnt += SQLManager.executeStatement( handler, "DELETE ECS_ITEM_SEASON WHERE GTIN = ? AND GLN = ?", primaryVars );
		}

		if( (infoType & INFO_MANUFGLN) > 0 ) {
			if( primaryMap.containsKey("manufGln") ) {
				String statement = "DELETE ECS_ITEM_GLN WHERE GTIN = ? AND GLN = ? AND SUB_CLASS = 'MF' AND SUB_GLN = ?";
				Object[] bindVars = new Object[] { primaryVars[0], primaryVars[1], Record.extractValue(primaryMap, "manufGln") };
				resultCnt += SQLManager.executeStatement( handler, statement, bindVars );
			} else {
				String statement = "DELETE ECS_ITEM_GLN WHERE GTIN = ? AND GLN = ? AND SUB_CLASS = 'MF'";
				resultCnt += SQLManager.executeStatement( handler, statement, primaryVars );
			}
		}

		if( (infoType & INFO_PRIVATEGLN) > 0 ) {
			if( primaryMap.containsKey("privateGln") ) {
				String statement = "DELETE ECS_ITEM_GLN WHERE GTIN = ? AND GLN = ? AND SUB_CLASS = 'PV' AND SUB_GLN = ?";
				Object[] bindVars = new Object[] { primaryVars[0], primaryVars[1], Record.extractValue(primaryMap, "privateGln") };
				resultCnt += SQLManager.executeStatement( handler, statement, bindVars );
			} else {
				String statement = "DELETE ECS_ITEM_GLN WHERE GTIN = ? AND GLN = ? AND SUB_CLASS = 'PV'";
				resultCnt += SQLManager.executeStatement( handler, statement, primaryVars );
			}
		}

		return ( resultCnt > 0 );
	}

	@Override
	public DataResult deleteAll( Collection<Map<String, Object>> records ) throws SQLException {
		return deleteEach( records );
	}

	public String generateGtin( String prefix ) throws IllegalArgumentException, SQLException {
		if( prefix == null || prefix.length() == 0 )
			prefix = "200";
		else if( prefix.length() < 3 )
			prefix = (prefix +"000").substring(0, 3);
		else if( prefix.length() > 9 )
			throw new IllegalArgumentException( "too long prefix '"+ prefix +"'" );

		for( int i = 0; i < 20; i++ ) {
			String suffix = (String)SQLManager.getObjectValue( handler, "SELECT TO_CHAR(seqECS_ITEM_CODE.NEXTVAL,'FM0000000000') FROM DUAL" );
			suffix = suffix.substring( suffix.length() + prefix.length() - 12 );

			String gtin = prefix + suffix + calculateCheckDigit( prefix + suffix );
			if( SQLManager.getObjectValue( handler, "SELECT 'x' FROM ECS_ITEM_MASTER WHERE GTIN = ?", gtin ) == null ) return gtin;
		}

		return null;
	}

	public Map<String, Object> getDefaultRecord( Map<String, Object> recordMap ) throws SQLException {
		return getDefaultRecord( recordMap, null );
	}

	public Map<String, Object> getDefaultRecord( Map<String, Object> recordMap, UserEnvironment userEnv ) throws SQLException {
		Map<String, Object> defaultMap = new java.util.HashMap<String, Object>();

		defaultMap.put( "gtinType", "GTN" );
		defaultMap.put( "itemKind", "00" );
		defaultMap.put( "itemUnit", "EA" );
		defaultMap.put( "isConsumerUnit", "Y" );
		defaultMap.put( "isInformationPrivate", "N" );
		defaultMap.put( "effectiveChangeDate", com.irt.data.Date.getInstance() );
		defaultMap.put( "taxType", "Y" );
		defaultMap.put( "sourcing", "S" );
		defaultMap.put( "isBaseUnit", "Y" );
		defaultMap.put( "isDespatchUnit", "Y" );
		defaultMap.put( "isInvoiceUnit", "Y" );
		defaultMap.put( "isOrderableUnit", "Y" );
		defaultMap.put( "isVariableWeight", "N" );

		String defaultValue = RBMSystem.getSystemEnv( "ECS", "ItemDefaultValue" );
		if( defaultValue != null )
			defaultMap.putAll( Record.decodeValueToMap(defaultValue) );

		if( userEnv != null ) {
			defaultValue = userEnv.getValue( "ECS", "ItemDefaultValue" );
			if( defaultValue != null )
				defaultMap.putAll( Record.decodeValueToMap(defaultValue) );
		}

		if( recordMap != null ) {
			defaultMap.putAll( recordMap );
			String gln = Record.extractString( recordMap, "gln" );
			if( gln != null ) {
				if( recordMap.get("informationGln") == null )
					defaultMap.put( "informationGln", gln );

				PreparedStatement pstmt = handler.getConnection().prepareStatement( "SELECT TIMEZONE, CURRCD FROM ECS_PARTY WHERE GLN = ?" );
				try {
					pstmt.setString( 1, gln );
					ResultSet rset = pstmt.executeQuery();
					try {
						if( rset.next() ) {
							String timeZone = rset.getString( 1 );
							if( timeZone != null )
								defaultMap.put( "effectiveChangeDate", com.irt.data.Date.getInstance(java.util.TimeZone.getTimeZone(timeZone)) );
							else
								defaultMap.put( "effectiveChangeDate", com.irt.data.Date.getInstance() );
							if( rset.getString(2) != null ) {
								defaultMap.put( "retailPriceCurr", rset.getString(2) );
								defaultMap.put( "retailPriceOnItemCurr", rset.getString(2) );
								defaultMap.put( "cataloguePriceCurr", rset.getString(2) );
								defaultMap.put( "suggestedRetailPriceCurr", rset.getString(2) );
							}
						}
					} finally {
						try { rset.close(); } catch( Exception ex ) {}
					}
				} finally {
					try { pstmt.close(); } catch( Exception ex ) {}
				}
			}
		}

		return defaultMap;
	}

	@Override
	public AbstractFieldSet getFieldSet( boolean inserting ) {
		return ItemFieldSet.getInstance( inserting );
	}

	public String getItemInfoType( Map<String, ? extends Object> recordMap ) throws SQLException {
		String gln = Record.extractString( recordMap, "gln" );
		String parentGln = Record.extractString( recordMap, "parentGln" );
		String informationGln = Record.extractString( recordMap, "informationGln" );
		String partyRole = (String)(new Party(handler)).getFieldValue( Party.createPrimary(gln), "partyRole" );
		if( partyRole == null || partyRole.length() == 0 )
			return null;

		if( Party.PARTYROLE_RETAIL.equals(partyRole) || Party.PARTYROLE_RETAIL_DC.equals(partyRole) || Party.PARTYROLE_STORE.equals(partyRole) ) {
			if( gln.equals(informationGln) )
				return "RI";
			else if( parentGln == null || parentGln.length() == 0 )
				return "RT";
			else
				return "RC";
		} else {
			if( gln.equals(informationGln) )
				return "SI";
			else if( parentGln == null || parentGln.length() == 0 )
				return "ST";
			else
				return "SC";
		}
	}

	public Map<String, Object> getItemMeasure( Map<String, ? extends Object> primaryMap, String itemUnit ) throws SQLException {
		Object[] bindVars = Record.extractValues( primaryMap, new String[] { "gtin", "gln" } );

		QueryBuffer querybuf = factories[IDX_MEASUREUNIT].setDataQuery( new QueryBuffer() );
		querybuf.appendTableWithAlias( "ECS_ITEM", "ITM", "IMSU.GTIN = ITM.MEASURE_REFGTIN AND IMSU.GLN = ITM.MEASURE_REFGLN" );
		querybuf.appendCondition( "IMSU.ITEMUNIT = ?", itemUnit );
		querybuf.appendCondition( "ITM.GTIN = ? AND ITM.GLN = ?", bindVars );

		return SQLManager.getRecordMap( handler, null, querybuf );
	}

	@Override
	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap ) throws SQLException {
		return getRecord( primaryMap, INFO_ALL );
	}

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, int infoType ) throws SQLException {
		Map<String, Object> recordMap = super.getRecord( primaryMap, itemQuery.getFieldKeys(infoType) );
		if( recordMap == null ) return null;

		if( (infoType & INFO_LINK) > 0 ) {
			Object[] bindVars = Record.extractValues( recordMap, new String[] { "gtin", "informationGln" } );
			String[] fieldKeys = new String[] { "lowerLevelGtin", "lowerLevelItemName", "lowerLevelShortDescription", "lowerQty" };

			QueryBuffer querybuf = factories[IDX_LINK].setDataQuery( new QueryBuffer(), fieldKeys );
			querybuf.appendCondition( "ILK.GTIN = ? AND ILK.GLN = ?", bindVars );
			recordMap.put( "lowerItemInfo", SQLManager.getRecordList(handler, querybuf) );
		}

		if( (infoType & INFO_ORIGIN) > 0 ) {
			Object[] bindVars = Record.extractValues( recordMap, new String[] { "baseGtin", "informationGln" } );

			QueryBuffer querybuf = factories[IDX_ORIGIN].setDataQuery( new QueryBuffer(), new String[] { "originCode", "originName" } );
			querybuf.appendCondition( "IOG.GTIN = ? AND IOG.GLN = ?", bindVars );
			recordMap.put( "originInfo", SQLManager.getRecordList(handler, querybuf) );
		}

		if( (infoType & INFO_DESCRIPTION_ALL) > 0 ) {
			Object[] bindVars = Record.extractValues( recordMap, new String[] { "gtin", "informationGln" } );

			QueryBuffer querybuf = factories[IDX_DESCRIPTION].setDataQuery( new QueryBuffer() );
			querybuf.appendCondition( "IDE.GTIN = ? AND IDE.GLN = ?", bindVars );
			recordMap.put( "descriptionInfo", SQLManager.getRecordList(handler, querybuf) );
		}

		if( (infoType & INFO_MEASUREUNIT) > 0 ) {
			Object[] bindVars = Record.extractValues( recordMap, new String[] { "measureRefGtin", "measureRefGln" } );

			QueryBuffer querybuf = factories[IDX_MEASUREUNIT].setDataQuery( new QueryBuffer() );
			querybuf.appendCondition( "IMSU.GTIN = ? AND IMSU.GLN = ?", bindVars );
			recordMap.put( "measureInfo", SQLManager.getRecordList(handler, querybuf) );
		}

		if( (infoType & INFO_SEASON) > 0 ) {
			Object[] bindVars = Record.extractValues( recordMap, new String[] { "gtin", "informationGln" } );

			QueryBuffer querybuf = factories[IDX_SEASON].setDataQuery( new QueryBuffer(), new String[] { "seasonCode", "seasonName" } );
			querybuf.appendCondition( "ISS.GTIN = ? AND ISS.GLN = ?", bindVars );
			recordMap.put( "seasonInfo", SQLManager.getRecordList(handler, querybuf) );
		}

		if( (infoType & INFO_MANUFGLN) > 0 ) {
			Object[] bindVars = Record.extractValues( recordMap, new String[] { "baseGtin", "informationGln" } );

			QueryBuffer querybuf = factories[IDX_MANUFGLN].setDataQuery( new QueryBuffer(), new String[] { "manufGln", "manufCompanyName" } );
			querybuf.appendCondition( "IMF.GTIN = ? AND IMF.GLN = ? AND SUB_CLASS = 'MF'", bindVars );
			recordMap.put( "manufGlnInfo", SQLManager.getRecordList(handler, querybuf) );
		}

		if( (infoType & INFO_PRIVATEGLN) > 0 ) {
			Object[] bindVars = Record.extractValues( recordMap, new String[] { "gtin", "gln" } );
			String[] fieldKeys = new String[] { "privateGln", "privateCompanyName", "publicationDate" };

			QueryBuffer querybuf = factories[IDX_PRIVATEGLN].setDataQuery( new QueryBuffer(), fieldKeys );
			querybuf.appendCondition( "IPV.GTIN = ? AND IPV.GLN = ? AND SUB_CLASS = 'PV'", bindVars );
			recordMap.put( "manufGlnInfo", SQLManager.getRecordList(handler, querybuf) );
		}

		return recordMap;
	}

	@Override
	public boolean modify( Map<String, Object> recordMap ) throws DataException, SQLException {
		return update( recordMap, INFO_ALL, false );
	}

	public boolean modify( Map<String, Object> recordMap, int infoType ) throws DataException, SQLException {
		return update( recordMap, infoType, false );
	}

	public boolean modify( Map<String, Object> recordMap, int infoType, String[] fieldKeys ) throws DataException, SQLException {
		switch( infoType ) {
		case INFO_ITEM:
			return SQLManager.updateRecord( handler, tables[IDX_ITEM], recordMap, fieldKeys );
		case INFO_ITEMINFO:
			return SQLManager.updateRecord( handler, tables[IDX_ITEMINFO], recordMap, fieldKeys );
		default:
			throw new IllegalArgumentException( "illegal infoType '"+ infoType +"'" );
		}
	}

	@Override
	public DataResult modifyAll( Collection<Map<String, Object>> records ) throws SQLException {
		return modifyEach( records );
	}

	public DataResult modifyAll( Collection<Map<String, Object>> records, int infoType, String[] fieldKeys ) throws SQLException {
		switch( infoType ) {
		case INFO_ITEM:
			return SQLManager.updateRecordAll( handler, tables[IDX_ITEM], records, fieldKeys );
		case INFO_ITEMINFO:
			return SQLManager.updateRecordAll( handler, tables[IDX_ITEMINFO], records, fieldKeys );
		default:
			throw new IllegalArgumentException( "illegal infoType '"+ infoType +"'" );
		}
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		return update( recordMap, INFO_ALL, true );
	}

	public boolean regist( Map<String, Object> recordMap, int infoType ) throws DataException, SQLException {
		return update( recordMap, infoType, true );
	}

	@Override
	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException {
		return registEach( records );
	}

	private boolean update( Map<String, Object> recordMap, int infoType, boolean inserting ) throws DataException, SQLException {
		ItemFieldSet.getInstance( inserting ).validate( handler, recordMap );
		Object[] primaryVars = Record.extractValues( recordMap, new String[] { "gtin", "gln" } );
		String[] copyFieldKeys = new String[] { "gtin", "gln", "updateUserId" };
		int statementType = inserting ? Record.INSERT : (Record.INSERT | Record.UPDATE);

		// ECS_ITEM
		if( inserting ) {
			SQLManager.manageRecord( handler, tables[IDX_ITEM], recordMap, Record.INSERT );
			SQLManager.callStatement( handler, "call pkECSItemReference.pInitReferenceGln( ?, ? )", primaryVars );

			// ECS_ITEM_INFO
			if( primaryVars[1].equals(Record.extractValue(recordMap, "informationGln")) )
				SQLManager.manageRecord( handler, tables[IDX_ITEMINFO], recordMap, Record.INSERT );
		} else if( (infoType & INFO_ITEM) > 0 )
			SQLManager.manageRecord( handler, tables[IDX_ITEM], recordMap, Record.UPDATE );


		// itemKind, itemUnit, informationGln 읽기 & bindVars 생성
		String itemKind, itemUnit, informationGln, systemDateTime;
		PreparedStatement pstmt = handler.getConnection().prepareStatement(
				"SELECT ITEMKIND, ITEMUNIT, INFORMATION_GLN, TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM ECS_ITEM WHERE GTIN = ? AND GLN = ?"
				);
		ResultSet rset = null;
		try {
			SQLManager.bindVariables( pstmt, primaryVars );
			rset = pstmt.executeQuery();
			if( !rset.next() ) return false;

			itemKind = rset.getString( 1 );
			itemUnit = rset.getString( 2 );
			informationGln = rset.getString( 3 );
			systemDateTime = rset.getString( 4 );
		} finally {
			try { rset.close(); } catch( Exception ex ) {}
			try { pstmt.close(); } catch( Exception ex ) {}
		}
		Object[] bindVars = new Object[] { primaryVars[0], primaryVars[1], systemDateTime };


		// Information Provider일 경우
		if( informationGln.equals(primaryVars[1]) ) {
			// ECS_ITEM_INFO
			Object isBaseUnit;
			if( inserting )
				isBaseUnit = Record.extractValue( recordMap, "isBaseUnit" );
			else {
				Object isBaseUnit_old = Record.extractValue( recordMap, "isBaseUnit" );
				isBaseUnit = SQLManager.getObjectValue( handler, "SELECT BASEUNIT_IND FROM ECS_ITEM_INFO WHERE GTIN = ? AND GLN = ?", primaryVars );
				if( (infoType & INFO_ITEMINFO) > 0 ) {
					SQLManager.manageRecord( handler, tables[IDX_ITEMINFO], recordMap, Record.UPDATE );
					if( "N".equals(isBaseUnit) && "Y".equals(isBaseUnit_old) ) {
						SQLManager.executeStatement( handler, "DELETE ECS_ITEM_LINK WHERE GTIN = ? AND GLN = ?", primaryVars );
						isBaseUnit = "Y";
					}
				}
			}

			// ECS_ITEM_LINK
			if( (infoType & INFO_LINK) > 0 && "N".equals(isBaseUnit) ) {
				updateTable( tables[IDX_LINK], recordMap, copyFieldKeys, "lowerItemInfo", statementType );
				if( !inserting ) {
					String statement = "DELETE ECS_ITEM_LINK WHERE GTIN = ? AND GLN = ? AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";
					SQLManager.executeStatement( handler, statement, bindVars );
				}
			}

			// ECS_ITEM_DESC
			if( (infoType & INFO_DESCRIPTION) > 0 && recordMap.containsKey("languageCode") ) {
				if( inserting || !SQLManager.manageRecord(handler, tables[IDX_DESCRIPTION], recordMap, Record.UPDATE) )
					SQLManager.manageRecord( handler, tables[IDX_DESCRIPTION], recordMap, Record.INSERT );
			}
			if( (infoType & INFO_DESCRIPTION_ALL) > 0 ) {
				updateTable( tables[IDX_DESCRIPTION], recordMap, copyFieldKeys, "descriptionInfo", statementType );
				if( !inserting ) {
					String statement = "DELETE ECS_ITEM_DESC WHERE GTIN = ? AND GLN = ? AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";
					SQLManager.executeStatement( handler, statement, bindVars );
				}
			}

			// ECS_ITEM_SEASON
			if( (infoType & INFO_SEASON) > 0 ) {
				if( !inserting ) SQLManager.executeStatement( handler, "DELETE ECS_ITEM_SEASON WHERE GTIN = ? AND GLN = ?", primaryVars );
				updateTable( tables[IDX_SEASON], recordMap, copyFieldKeys, "seasonInfo", Record.INSERT );
			}

			// ECS_ITEM_FASHION, ECS_ITEM_ORIGIN, ECS_ITEM_GLN( SUB_CLASS = 'MF' )
			if( "EA".equals(itemUnit) ) {
				if( (infoType & INFO_FASHION) > 0 && "FS".equals(itemKind) ) {
					if( inserting || !SQLManager.manageRecord(handler, tables[IDX_FASHION], recordMap, Record.UPDATE) )
						SQLManager.manageRecord( handler, tables[IDX_FASHION], recordMap, Record.INSERT );
				}
				if( (infoType & INFO_ORIGIN) > 0 ) {
					if( !inserting ) SQLManager.executeStatement( handler, "DELETE ECS_ITEM_ORIGIN WHERE GTIN = ? AND GLN = ?", primaryVars );
					updateTable( tables[IDX_ORIGIN], recordMap, copyFieldKeys, "originInfo", Record.INSERT );
				}
				if( (infoType & INFO_MANUFGLN) > 0 ) {
					if( !inserting )
						SQLManager.executeStatement( handler, "DELETE ECS_ITEM_GLN WHERE GTIN = ? AND GLN = ? AND SUB_CLASS = 'MF'", primaryVars );
					updateTable( tables[IDX_MANUFGLN], recordMap, copyFieldKeys, "manufGlnInfo", Record.INSERT );
				}
			}
		}

		// ECS_ITEM_HIERARCHY
		if( (infoType & INFO_HIERARCHY) > 0 ) {
			if( inserting || !SQLManager.manageRecord(handler, tables[IDX_HIERARCHY], recordMap, Record.UPDATE) )
				SQLManager.manageRecord( handler, tables[IDX_HIERARCHY], recordMap, Record.INSERT );
		}

		// ECS_ITEM_MEASURE
		if( (infoType & INFO_MEASURE) > 0 ) {
			if( inserting || !SQLManager.manageRecord(handler, tables[IDX_MEASURE], recordMap, Record.UPDATE) )
				SQLManager.manageRecord( handler, tables[IDX_MEASURE], recordMap, Record.INSERT );
		}

		// ECS_ITEM_PACKAGING
		if( (infoType & INFO_PACKAGING) > 0 ) {
			if( inserting || !SQLManager.manageRecord(handler, tables[IDX_PACKAGING], recordMap, Record.UPDATE) )
				SQLManager.manageRecord( handler, tables[IDX_PACKAGING], recordMap, Record.INSERT );
		}

		// ECS_ITEM_HANDLING
		if( (infoType & INFO_HANDLING) > 0 ) {
			if( inserting || !SQLManager.manageRecord(handler, tables[IDX_HANDLING], recordMap, Record.UPDATE) )
				SQLManager.manageRecord( handler, tables[IDX_HANDLING], recordMap, Record.INSERT );
		}

		// ECS_ITEM_ORDERING
		if( (infoType & INFO_ORDERING) > 0 ) {
			if( inserting || !SQLManager.manageRecord(handler, tables[IDX_ORDERING], recordMap, Record.UPDATE) )
				SQLManager.manageRecord( handler, tables[IDX_ORDERING], recordMap, Record.INSERT );
		}

		// ECS_ITEM_ATTRIBUTE
		if( (infoType & INFO_ATTRIBUTE) > 0 ) {
			if( inserting || !SQLManager.manageRecord(handler, tables[IDX_ATTRIBUTE], recordMap, Record.UPDATE) )
				SQLManager.manageRecord( handler, tables[IDX_ATTRIBUTE], recordMap, Record.INSERT );
		}

		// ECS_ITEM_GLN( SUB_CLASS = 'PV' )
		if( (infoType & INFO_PRIVATEGLN) > 0 && "EA".equals(itemUnit) ) {
			updateTable( tables[IDX_PRIVATEGLN], recordMap, copyFieldKeys, "privateGlnInfo", statementType );
			if( !inserting ) {
				String statement
				= "DELETE ECS_ITEM_GLN WHERE GTIN = ? AND GLN = ? AND SUB_CLASS = 'PV' AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";
				SQLManager.executeStatement( handler, statement, bindVars );
			}
		}

		// ECS_ITEM_MEASUREU
		if( (infoType & INFO_MEASUREUNIT) > 0 && "EA".equals(itemUnit) ) {
			updateTable( tables[IDX_MEASUREUNIT], recordMap, copyFieldKeys, "measureInfo", statementType, true );
			if( !inserting ) {
				String statement = "DELETE ECS_ITEM_MEASUREU WHERE GTIN = ? AND GLN = ? AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";
				SQLManager.executeStatement( handler, statement, bindVars );
			}
		}

		return true;
	}
}
