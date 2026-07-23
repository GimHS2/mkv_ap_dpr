/*
 *	File Name:	StockQuery.java
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.FieldException;
import com.irt.data.Record;
import com.irt.data.ValidableField;
import com.irt.data.ValidableFieldSet;
import com.irt.dpr.util.Loggers;
import com.irt.servlet.SystemConfig;
import com.irt.sql.ConditionQueryBuffer;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.sql.TableDataLoader;

/**
 *
 */
public class StockQuery extends com.irt.rbm.ManipulableManagerImpl {//@formatter:on
	private final static Table table = Schema.findTable( Schema.DPR_STOCK_QUERY );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_STOCK_QUERY );

	public static final String STATUS_UPLOAD					= "00";
	public static final String STATUS_SIMULATION_COMPLETE		= "CO";
	public static final String STATUS_ERROR						= "ER";

	public static final String STOCKQUERY_IF_SIMULATION			= com.irt.dpr.tools.StockQueryCanonical.STOCKQUERY_IF_SIMULATION;
	public final static String DEFAULT_SIMULATION_UOM			= "CSE";
	public final static int DEFAULT_SIMULATION_INPUT_QTY		= 99999;
	public final static int DEFAULT_SIMULATION_LINENUMBER		= 50;

	public final String[] REGEX_ERR_SAP_MSG = { "SAP: Material (\\d+) is not listed and therefore not allowed", "SAP: Material (\\d+) has status: Test Part" };

	private SystemConfig systemConfig;
	Logger logger = Logger.getLogger( StockQuery.class );

	public StockQuery( SQLHandler handler ) {
		super(handler, table, factory);
	}

	public StockQuery( SQLHandler handler, SystemConfig systemConfig ) {
		super( handler, table, factory );
		this.systemConfig = systemConfig;
	}

	@Override
	public DataLoader.Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, int statementType ) throws SQLException {
		return new TableDataLoader( fieldKeys, defaultMap, handler, table, statementType ) {
			Map<String, Object> defaultPlantMap;
			Map<String, Object> plantCodeMap;;
			Map<String, Object> simulationKeyMap;
			Map<String, Object> lineNumberMap;
			Map<String, Object> validSoldToMap;
			Map<String, Object> validShipToMap;

			PreparedStatement pstmt_defaultParty = null, pstmt_plant = null;
			int startLineNumber = 10;
			int increaseLineNumber = 10;
			int simulationLineNumber;

			String defaultDistChannelCode = null;

			@Override
			public void close() {
				super.close();
				try { if( pstmt_defaultParty != null ) pstmt_defaultParty.close(); } catch( Exception ignored ) {}
				try { if( pstmt_plant != null ) pstmt_plant.close(); } catch( Exception ignored ) {}
			}

			@Override
			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				String soldPartyCode = (String) recordMap.get( "soldPartyCode" );
				String shipPartyCode = (String) recordMap.get( "shipPartyCode" );
				String plantCode = (String) recordMap.get( "plantCode" );
				String simulationKey;
				int lineNumber;

				ResultSet resultSet = null;

				Map<String, Object> conditionMap = new java.util.HashMap<String, Object>();
				conditionMap.put( "countryCode", defaultMap.get("countryCode") );
				conditionMap.put( "soldPartyCode", soldPartyCode );
				conditionMap.put( "shipPartyCode", shipPartyCode );
				conditionMap.put( "organizationCode", defaultMap.get("organizationCode") );
				conditionMap.put( "divisionCode", defaultMap.get("divisionCode") );

				String distributionChannelCode;
				if( soldPartyCode != null && soldPartyCode.length() > 0 && shipPartyCode != null && shipPartyCode.length() > 0 ) {
					Party partyDB = new Party( handler );
					distributionChannelCode = partyDB.getDistributionChannelCode( soldPartyCode, defaultMap.get("organizationCode"), defaultMap.get("divisionCode") );
					if( distributionChannelCode == null ) {
						distributionChannelCode = defaultDistChannelCode;
					}
					conditionMap.put( "distributionChannelCode", distributionChannelCode );

					checkPartnerCode( conditionMap );
					if( plantCodeMap.containsKey(shipPartyCode) ) {
						plantCode = (String) plantCodeMap.get( "shipPartyCode" );
					} else {
						pstmt_plant.setString( 2, distributionChannelCode );
						pstmt_plant.setString( 4, shipPartyCode );
						try {
							resultSet = pstmt_plant.executeQuery();
							if( resultSet.next() ) {
								plantCode = resultSet.getString( 1 );
								defaultPlantMap.put( shipPartyCode, plantCode );
							} else {
								throw handler.createDataException( DataException.ERR_ERROR
										, handler.getMessageHandler().getMessage("MSG_DPR_STOCK_QUERY_NOT_EXIST_PLANT", shipPartyCode) );
							}
						} finally {
							try { resultSet.close(); } catch( Exception ignored ) {}
						}
					}

					recordMap.put( "plantCode", plantCode );
				} else if( plantCode != null && plantCode.length() > 0 ) {
					if( defaultPlantMap.containsKey(plantCode) ) {
						String[] partyCodes = (String[]) defaultPlantMap.get( plantCode );
						soldPartyCode = partyCodes[0];
						shipPartyCode = partyCodes[1];
					} else {
						pstmt_defaultParty.setString( 2, plantCode );
						try {
							resultSet = pstmt_defaultParty.executeQuery();
							if( resultSet.next() ) {
								soldPartyCode = resultSet.getString( 1 );
								shipPartyCode = resultSet.getString( 2 );
								String[] partyCodes = { soldPartyCode, shipPartyCode };
								defaultPlantMap.put( plantCode, partyCodes );
							} else {
								throw handler.createDataException( DataException.ERR_ERROR
										,  handler.getMessageHandler().getMessage("MSG_DPR_STOCK_QUERY_NOT_EXIST_DEFAULT_PLANT") );
							}
						} finally {
							try { resultSet.close(); } catch( Exception ignored ) {}
						}
					}
					Party partyDB = new Party( handler );
					distributionChannelCode = partyDB.getDistributionChannelCode( soldPartyCode, defaultMap.get("organizationCode"), defaultMap.get("divisionCode") );
					if( distributionChannelCode == null ) {
						distributionChannelCode = defaultDistChannelCode;
					}
					conditionMap.put( "distributionChannelCode", distributionChannelCode );

					checkPartnerCode( conditionMap );
					recordMap.put( "soldPartyCode", soldPartyCode );
					recordMap.put( "shipPartyCode", shipPartyCode );
				} else {
					throw handler.createDataException( DataException.ERR_CANNOT_NULL );
				}
				recordMap.put( "distributionChannelCode", distributionChannelCode );

				if( simulationKeyMap.containsKey(soldPartyCode + shipPartyCode) ) {
					simulationKey = (String) simulationKeyMap.get( soldPartyCode + shipPartyCode );
					lineNumber = (Integer) lineNumberMap.get( simulationKey );
					lineNumber += increaseLineNumber;
				} else {
					simulationKey = StockQuery.createSimulationKey( handler );
					simulationKeyMap.put( soldPartyCode + shipPartyCode, simulationKey );
					lineNumber = startLineNumber;
				}

				lineNumberMap.put( simulationKey, lineNumber );
				if( lineNumber >= simulationLineNumber ) {
					simulationKeyMap.remove( soldPartyCode + shipPartyCode );
				}

				recordMap.put( "lineNumber", lineNumber );
				recordMap.put( "simulationKey", simulationKey );
				return super.processLine( handler, recordMap );
			}

			@Override
			public void start( SQLHandler handler ) throws SQLException {
				Connection conn = handler.getConnection();
				defaultPlantMap = new java.util.HashMap<String, Object>();
				plantCodeMap = new java.util.HashMap<String, Object>();
				simulationKeyMap = new java.util.HashMap<String, Object>();
				lineNumberMap = new java.util.HashMap<String, Object>();
				validSoldToMap = new java.util.HashMap<String, Object>();
				validShipToMap = new java.util.HashMap<String, Object>();

				simulationLineNumber = (Integer) defaultMap.get( "simulationLineNumber" );
				simulationLineNumber *= increaseLineNumber;
				String organizationCode = (String) defaultMap.get( "organizationCode" );
				String divisionCode = (String) defaultMap.get( "divisionCode" );

				pstmt_defaultParty = conn.prepareStatement(
					"SELECT SOLD_PARTYCD, SHIP_PARTYCD FROM DPR_STOCK_QUERY_PLANTMAP"
							+ " WHERE ORGANIZATIONCD = ? AND PLANTCD = ?"
				);
				pstmt_defaultParty.setString( 1, organizationCode );

				pstmt_plant = conn.prepareStatement(
					"SELECT DELIVERY_PLANT FROM DPR_PARTY_SALES"
							+ " WHERE ORGANIZATIONCD = ? AND DIST_CHANNELCD = ? AND DIVISIONCD = ? AND PARTYCD = ?"
				);
				pstmt_plant.setString( 1, organizationCode );
				pstmt_plant.setString( 3, divisionCode );

				defaultDistChannelCode = (String) defaultMap.remove( "distributionChannelCode" );
			}

			public boolean checkPartnerCode( Map<String, Object> conditionMap ) throws SQLException, DataException {
				String soldPartyCode = (String)conditionMap.get( "soldPartyCode" );
				String shipPartyCode = (String)conditionMap.get( "shipPartyCode" );
				String validSoldTo = (String) validSoldToMap.get( soldPartyCode );
				if( "Y".equals(validSoldTo) ) {
					return true;
				} else if( "N".equals(validSoldTo) ) {
					throw handler.createDataException( DataException.ERR_ERROR
							,  handler.getMessageHandler().getMessage("ERR_CANNOT_FIND_SOLDTO") );
				}
				String validShipTo = (String) validShipToMap.get( shipPartyCode );
				if( "Y".equals(validShipTo) ) {
					return true;
				} else if( "N".equals(validShipTo) ) {
					throw handler.createDataException( DataException.ERR_ERROR
							,  handler.getMessageHandler().getMessage("ERR_CANNOT_FIND_SOLDTO") );
				}

				com.irt.dpr.PartyLink db = new com.irt.dpr.PartyLink( handler );
				Map<String, Object> partnerConditionMap = new java.util.HashMap<String, Object>( conditionMap );
				partnerConditionMap.put( "partyCode", partnerConditionMap.remove("soldPartyCode") );
				partnerConditionMap.put( "partyStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );
				partnerConditionMap.put( "linkStatus", com.irt.dpr.Party.PARTYSTATUS_ACTIVE );

				partnerConditionMap.put( "linkType", com.irt.dpr.PartyLink.LINKTYPE_SOLD );
				if( db.getRecordCount(partnerConditionMap) < 1 ) {
					validSoldToMap.put( soldPartyCode, "N" );
					throw handler.createDataException( DataException.ERR_ERROR
							,  handler.getMessageHandler().getMessage("ERR_CANNOT_FIND_SOLDTO") );
				}
				validSoldToMap.put( soldPartyCode, "Y" );

				partnerConditionMap.put( "linkType", com.irt.dpr.PartyLink.LINKTYPE_SHIP );
				partnerConditionMap.put( "linkPartyCode", partnerConditionMap.remove("shipPartyCode") );
				if( db.getRecordCount(partnerConditionMap) < 1 ) {
					validSoldToMap.put( shipPartyCode, "N" );
					throw handler.createDataException( DataException.ERR_ERROR
							,  handler.getMessageHandler().getMessage("ERR_CANNOT_FIND_SHIPTO") );
				}
				validSoldToMap.put( shipPartyCode, "Y" );

				return true;
			}
		};
	}

	public static Map<String, Object> createPrimary( Object itemCode, Object plantCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "itemCode", itemCode );
		primaryMap.put( "plantCode", plantCode );

		return primaryMap;
	}

	public static Map<String, Object> createPrimary( Object itemCode, Object plantCode, Object loadingGroup ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "itemCode", itemCode );
		primaryMap.put( "plantCode", plantCode );
		primaryMap.put( "loadingGroup", loadingGroup );

		return primaryMap;
	}

	public static String createQueryKey( SQLHandler handler) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT 'Q' || seqDPR_STOCK_QUERY.nextval FROM DUAL" );
	}

	public static String createSimulationKey( SQLHandler handler ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT 'SQ' || seqDPR_SIMULATION.nextval FROM DUAL" );
	}

	public void executeSimulation( Map<String, Object> defaultParamMap, boolean autoRetry ) throws DataException, SQLException {
		boolean existWebmethod = com.irt.rbm.RBMSystem.getSystemEnvBool( "DPR", "WebMethods;IsExist", true );
		if( !existWebmethod ) return;
		String queryKey = (String) defaultParamMap.get( "queryKey" );
		List<Map<String, Object>> headerList = getHeaderRecords( queryKey );

		PreparedStatement all_pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_STOCK_QUERY SET MESSAGE = ?, STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ?"
					+" WHERE SIMULATION_KEY = ? AND STATUS <> 'ER'" );
		all_pstmt.setString( 3, (String)defaultParamMap.get("updateUserId") );

		PreparedStatement each_pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_STOCK_QUERY SET MESSAGE = ?, STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ?"
					+ " WHERE SIMULATION_KEY = ? AND ORGANIZATIONCD = ? AND DIST_CHANNELCD = ? AND SOLD_PARTYCD = ?"
					+ " AND SHIP_PARTYCD = ? AND ITEMCD = ? AND STATUS <> 'ER'" );
		each_pstmt.setString( 3, (String)defaultParamMap.get("updateUserId") );

		for( int i = 0; i < headerList.size(); i++ ) {
			Map<String, Object> headerMap = headerList.get( i );
			headerMap.putAll( defaultParamMap );
			com.irt.data.Date dateValue = (com.irt.data.Date)headerMap.get( "dateValue" );
			com.irt.data.Date inDate = com.irt.data.Date.getInstance( dateValue.getDate(2) );
			headerMap.put( "inDate", inDate );

			Loggers.business.info( "{}-{}: {}", headerMap.get("queryKey"), headerMap.get("simulationKey"), STOCKQUERY_IF_SIMULATION + " start." );
			com.irt.dpr.tools.StockQueryCanonical ocp = new com.irt.dpr.tools.StockQueryCanonical( handler, systemConfig, headerMap );
			try {
				PreparedStatement detail_pstmt = handler.getConnection().prepareStatement(
						"SELECT COUNT(*) FROM DPR_STOCK_QUERY WHERE SIMULATION_KEY = ? AND STATUS <> 'ER'"
				);
				ResultSet rset = null;
				try {
					detail_pstmt.setString( 1, (String)headerMap.get("simulationKey") );
					rset = detail_pstmt.executeQuery();
					if( rset.next() && rset.getInt(1) < 1 ) {
						Loggers.business.info( "{}-{}: {}", headerMap.get("queryKey"), headerMap.get("simulationKey"), STOCKQUERY_IF_SIMULATION + " failed. No data." );
						continue;
					}
				} finally {
					try { rset.close(); } catch( Exception ex ) {}
					try { detail_pstmt.close(); } catch( Exception ex ) {}
				}

				ocp.execute();
				handler.commit();
			} catch( OrderProcessException opEx ) {
				handler.rollback();
				String message = opEx.getMessage().substring( 0, Math.min(opEx.getMessage().length(), 510) );
				if( message.indexOf("<?xml version=\"1.0\"?>") > 0 ) {
					message = message.substring( 0, message.indexOf("<?xml version=\"1.0\"?>") );
				}
				boolean stopRetry = true;
				if( autoRetry ) {
					String itemCode = null;
					for( String regex : REGEX_ERR_SAP_MSG ) {
						itemCode = sapErrorMatcher( message, regex );
						if( itemCode != null ) break;
					}

					if( itemCode != null ) {
						each_pstmt.setString( 1, message );
						each_pstmt.setString( 2, STATUS_ERROR );
						each_pstmt.setString( 4, (String)headerMap.get("simulationKey") );
						each_pstmt.setString( 5, (String)headerMap.get("organizationCode") );
						each_pstmt.setString( 6, (String)headerMap.get("distributionChannelCode") );
						each_pstmt.setString( 7, (String)headerMap.get("soldPartyCode") );
						each_pstmt.setString( 8, (String)headerMap.get("shipPartyCode") );
						each_pstmt.setString( 9, itemCode );
						int count = each_pstmt.executeUpdate();
						if( count > 0 ) {
							handler.commit();
							Loggers.business.info( "{}-{}: {}", headerMap.get("queryKey"), headerMap.get("simulationKey")
									, itemCode + " item status error. Retry simulation." + message );
							i--;
							stopRetry = false;
						} else {
							handler.rollback();
							stopRetry = true;
						}
					}
				}

				if( stopRetry ) {
					all_pstmt.setString( 1, message );
					all_pstmt.setString( 2, STATUS_ERROR );
					all_pstmt.setString( 4, (String)headerMap.get("simulationKey") );
					all_pstmt.executeUpdate();
					handler.commit();

					Loggers.business.info( "{}-{}: {}", headerMap.get("queryKey"), headerMap.get("simulationKey")
							, STOCKQUERY_IF_SIMULATION + " failed. " + message );
				}
			} finally {
				Loggers.business.info( "{}: {}", queryKey, STOCKQUERY_IF_SIMULATION + " end." );
			}
		}
	}

	public List<Map<String, Object>> getHeaderRecords( String queryKey ) throws SQLException {
		QueryBuffer querybuf = new ConditionQueryBuffer( Record.createMap("queryKey", queryKey) );
		factory.setQuery( querybuf, new String[] { "dateValue", "simulationKey", "organizationCode", "distributionChannelCode", "divisionCode"
				, "soldPartyCode", "shipPartyCode", "plantCode", "JDMSIndicator", "inputQty" } );
		querybuf.appendDistinct();

		return SQLManager.getRecordList( handler, querybuf, 0, -1 );
	}

	public String getMaxQueryKey( Map<String, Object> conditionMap ) throws SQLException {
		QueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );
		factory.setConditionQuery( querybuf );
		querybuf.appendDataWithAlias( "MAX(QUERY_KEY)", "queryKey" );

		return (String) SQLManager.getObjectValue( handler, querybuf );
	}

	public String sapErrorMatcher( String message, String regex ) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile( regex );
		java.util.regex.Matcher matcher = pattern.matcher( message );
		String itemCode = null;
		if( matcher.find() ) {
			try {
				itemCode = matcher.group( 1 );
			} catch( Exception ignore ) {}
		}
		return itemCode;
	}
/*
	public boolean raiseError( Map<String, Object> dataMap ) {
		ValidableFieldSet sqSimulationFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( false,	"message",			"DPR_STOCKQUERY_MESSAGE",			Schema.STRING )
			, new ValidableField( false,	"status",			"DPR_STOCKQUERY_STATUS",			Schema.STRING )
			, new ValidableField( false,	"updateUserId",		"UPGUSERID",						Schema.STRING )
			, new ValidableField( false,	"simulationKey",	"DPR_STOCKQUERY_SIMULATIONQTY",		Schema.STRING )
		} );

		ValidableFieldSet stockQueryFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( false,	"message",			"DPR_STOCKQUERY_MESSAGE",			Schema.STRING )
			, new ValidableField( false,	"status",			"DPR_STOCKQUERY_STATUS",			Schema.STRING )
			, new ValidableField( false,	"updateUserId",		"UPGUSERID",						Schema.STRING )
			, new ValidableField( false,	"simulationKey",	"DPR_STOCKQUERY_SIMULATIONQTY",		Schema.STRING )
		} );

		PreparedStatement sqSimulation_pstmt = null, stockQuery_pstmt = null;
		try {
			dataMap.put( "status", STATUS_ERROR );
			sqSimulation_pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_STOCK_QUERY SET MESSAGE = ?, STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ? WHERE SIMULATION_KEY = ?" );
			SQLManager.bindVariables( sqSimulation_pstmt, sqSimulationFieldSet.validate(dataMap) );
			sqSimulation_pstmt.executeUpdate();

			stockQuery_pstmt = handler.getConnection().prepareStatement(
				"UPDATE DPR_STOCK_QUERY SET MESSAGE = ?, STATUS = ?, UPGDATE = SYSDATE"
					+ "WHERE UNIQID = ? AND ORGANIZATIONCD = ?" );
			SQLManager.bindVariables( stockQuery_pstmt, stockQueryFieldSet.validate(dataMap) );
			stockQuery_pstmt.executeUpdate();
		} catch( FieldException fieldEx ) {
			return false;
		} catch( SQLException sqlEx ) {
			return false;
		} finally {
			try {
				if( sqSimulation_pstmt != null ) sqSimulation_pstmt.close();
				if( stockQuery_pstmt != null ) stockQuery_pstmt.close();
				handler.commit();
			} catch( SQLException sqlEx ) {}
		}

		return true;
	}
*/

	public boolean updateFromProcess( Map<String, Object> headerMap, List<Map<String, Object>> detailList ) throws SQLException, DataException, OrderProcessException {
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();
		Loggers.business.debug( "{}: {}", headerMap.get("simulationKey"), STOCKQUERY_IF_SIMULATION + " start." );
		ValidableFieldSet sqSimulationFieldSet = new ValidableFieldSet( new ValidableField[] {
			  new ValidableField( true,		"simulationQty",	"DPR_STOCKQUERY_SIMULATIONQTY",		Schema.DOUBLE )
			, new ValidableField( true,		"price",			"DPR_STOCKQUERY_PRICE",				Schema.DOUBLE )
			, new ValidableField( true,		"simulationUom",	"DPR_STOCKQUERY_SIMULATIONUOM",		Schema.STRING )
			, new ValidableField( false,	"status",			"DPR_STOCKQUERY_STATUS",			Schema.STRING )
			, new ValidableField( true,		"updateUserId",		"UPDATEUSERID",						Schema.STRING )
			, new ValidableField( false,	"simulationKey",	"DPR_STOCKQUERY_SIMULATIONKEY",		Schema.STRING )
			, new ValidableField( false,	"organizationCode",	"DPR_STOCKQUERY_QUERYKEY",			Schema.STRING )
			, new ValidableField( false,	"distributionChannelCode",	"DPR_STOCKQUERY_DISTCHANNELCODE",	Schema.STRING )
			, new ValidableField( false,	"soldPartyCode",	"DPR_STOCKQUERY_SOLDPARTYCODE",		Schema.STRING )
			, new ValidableField( false,	"shipPartyCode",	"DPR_STOCKQUERY_SHIPPARTYCODE",		Schema.STRING )
			, new ValidableField( false,	"itemCode",			"DPR_STOCKQUERY_ITEMCODE",			Schema.STRING )
		} );

		PreparedStatement sqSimulation_pstmt = null;
		try {
			if( detailList != null && detailList.size() > 0 ) {
				sqSimulation_pstmt = handler.getConnection().prepareStatement(
					"UPDATE DPR_STOCK_QUERY SET SIMULATION_QTY = ?, PRICE = ?, SIMULATION_UOM = ?"
						+", STATUS = ?, UPGDATE = SYSDATE, UPGUSERID = ?"
					+" WHERE SIMULATION_KEY = ? AND ORGANIZATIONCD = ? AND DIST_CHANNELCD = ? AND SOLD_PARTYCD = ? AND SHIP_PARTYCD = ? AND ITEMCD = ?"
				);

				for( Map<String, Object> detailMap : detailList ) {
					Map<String, Object> recordMap = new java.util.HashMap<String, Object>( headerMap );
					recordMap.putAll( detailMap );
					recordMap.put( "status", STATUS_SIMULATION_COMPLETE );
					SQLManager.bindVariables( sqSimulation_pstmt, sqSimulationFieldSet.validate(recordMap) );

					sqSimulation_pstmt.executeUpdate();
					Loggers.business.debug( "{}: {}", headerMap.get("simulationKey"), recordMap );
				}
			}
		} catch( FieldException fieldEx ) {
			handler.rollback();
			//headerMap.put( "message", msghandler.getMessage(fieldEx.getErrorKey()) +"(Field: "+ fieldEx.getErrorField() +", Value: "+ fieldEx.getErrorFieldValue() +")" );
			logger.error( fieldEx.getErrorField().getFieldKey() +":"+ fieldEx.getErrorKey() );
			//raiseError( headerMap );
			Loggers.business.debug( "{}: {}", headerMap.get("simulationKey"), STOCKQUERY_IF_SIMULATION + " throw handler.createDataException( fieldEx, null ); " );
			throw new OrderProcessException( fieldEx );
		} catch( SQLException sqlEx ) {
			handler.rollback();
			//headerMap.put( "message", sqlEx.getMessage() );
			logger.error( sqlEx );
			//raiseError( headerMap );
			Loggers.business.debug( "{}: {}", headerMap.get("simulationKey"), STOCKQUERY_IF_SIMULATION + " return false;" );
			throw new OrderProcessException( sqlEx );
		} finally {
			if( sqSimulation_pstmt != null ) sqSimulation_pstmt.close();
		}

		Loggers.business.debug( "{}: {}", headerMap.get("simulationKey"), STOCKQUERY_IF_SIMULATION + " return true;" );
		return true;
	}
}
