/*
 *	File Name:	PlantMapping.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/12/31		2.2.0	create
 *
**/

package com.irt.dpr;

import java.sql.SQLException;
import java.util.Map;

import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.sql.TableDataLoader;

/**
 *
 */
public class PlantMapping extends com.irt.rbm.ManipulableManagerImpl {//@formatter:on
	private final static Table table = Schema.findTable( Schema.DPR_STOCK_QUERY_PLANTMAP );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_STOCK_QUERY_PLANTMAP );

	public PlantMapping( SQLHandler handler ) {
		super(handler, table, factory);
	}

	public static Map<String, Object> createPrimary( String organizationCode, String distributionChannelCode, String divisionCode, String plantCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributionChannelCode", distributionChannelCode );
		primaryMap.put( "divisionCode", divisionCode );
		primaryMap.put( "plantCode", plantCode );

		return primaryMap;
	}

	@Override
	public DataLoader.Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
			, int statementType ) throws SQLException, UnsupportedOperationException {
		return new TableDataLoader( fieldKeys, defaultMap, handler, table, statementType ) {
			String defaultDistChannelCode = null;

			@Override
			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {

				Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
				conditionMap.put( "soldPartyCode", recordMap.get("soldPartyCode") );
				conditionMap.put( "shipPartyCode",  recordMap.get("shipPartyCode") );
				conditionMap.put( "organizationCode", defaultMap.get("organizationCode") );
				conditionMap.put( "divisionCode", defaultMap.get("divisionCode") );

				Party partyDB = new Party( handler );
				String distributionChannelCode = partyDB.getDistributionChannelCode( recordMap.get("soldPartyCode"), defaultMap.get("organizationCode"), defaultMap.get("divisionCode") );
				if( distributionChannelCode == null ) {
					distributionChannelCode = defaultDistChannelCode;
				}
				conditionMap.put( "distributionChannelCode", distributionChannelCode );
				checkPartnerCode( conditionMap );
				recordMap.put( "distributionChannelCode", distributionChannelCode );
				return super.processLine( handler, recordMap );
			}

			@Override
			public void start( SQLHandler handler ) throws SQLException {
				defaultDistChannelCode = (String) defaultMap.remove( "distributionChannelCode" );
				super.start( handler );
			}

			public boolean checkPartnerCode( Map<String, Object> conditionMap ) throws SQLException, DataException {
				com.irt.dpr.PartyLink db = new com.irt.dpr.PartyLink( handler );
				Map<String, Object> partnerConditionMap = new java.util.HashMap<String, Object>( conditionMap );
				partnerConditionMap.put( "partyCode", partnerConditionMap.remove("soldPartyCode") );
				partnerConditionMap.put( "partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
				partnerConditionMap.put( "linkStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );

				partnerConditionMap.put( "linkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );
				if( db.getRecordCount(partnerConditionMap) < 1 ) {
					throw handler.createDataException( DataException.ERR_ERROR
							,  handler.getMessageHandler().getMessage("ERR_CANNOT_FIND_SOLDTO") );
				}
				partnerConditionMap.put( "linkType", com.irt.dpr.PartyLink.LINKTYPE_SHIP );
				partnerConditionMap.put( "linkPartyCode", partnerConditionMap.remove("shipPartyCode") );
				if( db.getRecordCount(partnerConditionMap) < 1 ) {
					throw handler.createDataException( DataException.ERR_ERROR
							,  handler.getMessageHandler().getMessage("ERR_CANNOT_FIND_SHIPTO") );
				}

				return true;
			}
		};
	}

	public Map<String, Object> getDefaultPartyMap( String organizationCode, String plantCode ) throws SQLException {
		String query = "SELECT SOLD_PARTYCD \"soldPartyCode\", SHIP_PARTYCD \"shipPartyCode\" FROM DPR_STOCK_QUERY_PLANTMAP"
						+ " WHERE ORGANIZATIONCD = ? AND PLANTCD = ?";

		return SQLManager.getRecordMap( handler, null, query, organizationCode, plantCode );
	}
}