/*
 *	File Name:	PartyAuth.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/12/31		2.2.1	multi-sold-to 기능 추가
 *	lsinji		2008/09/26		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class PartyAuth extends com.irt.rbm.ManipulableManagerImpl {
	public final static String PARTYTYPE_DISTRIBUTOR		= "DIST";
	public final static String PARTYTYPE_CUSTOMER			= "CUST";

	public final static String LINKSOURCE_SAP				= "S";
	public final static String LINKSOURCE_PORTAL			= "D";

	public final static String DEFAULT_AUTHORIZATIONVALUE	= "YYYYY";

	private final static Table table = Schema.findTable( Schema.DPR_PARTY_AUTH );
	private final static QueryFactory factory = new QueryFactory( new QueryableImpl(Schema.findQueryable(Schema.DPR_COUNTRY_COND)) {
		{
			Joinable tbl_PTYS = new JoinableImpl( "PTYS", "DPR_PARTY_SALES"
					, "PTYS.COUNTRYCD = CCND.COUNTRYCD AND PTYS.ORGANIZATIONCD = CCND.ORGANIZATIONCD" );
			Joinable tbl_PAUT = new JoinableImplBK( "PAUT", "DPR_PARTY_AUTH"
					, "PAUT.PARTYCD(+) = PTYS.PARTYCD AND PAUT.ORGANIZATIONCD(+) = PTYS.ORGANIZATIONCD"
							+ " AND PAUT.DIST_CHANNELCD(+) = PTYS.DIST_CHANNELCD AND PAUT.DIVISIONCD(+) = PTYS.DIVISIONCD"
							+ " AND PAUT.UNIQID(+) = ?", "uniqId", tbl_PTYS );
			Joinable tbl_PEMP = new JoinableImpl( "PEMP", "DPR_PARTY_EMPLOYEE", "PEMP.PARTYCD(+) = PTYS.PARTYCD"
							+ " AND PEMP.ORGANIZATIONCD(+) = PTYS.ORGANIZATIONCD AND PEMP.DIST_CHANNELCD(+) = PTYS.DIST_CHANNELCD"
							+ " AND PEMP.DIVISIONCD(+) = PTYS.DIVISIONCD", tbl_PTYS );

			append( Schema.findQueryable(Schema.DPR_PARTY_SALES)
					, "PTYS.COUNTRYCD = CCND.COUNTRYCD AND PTYS.ORGANIZATIONCD = CCND.ORGANIZATIONCD" );
			append( new QueryableField[] {
					new QueryableFieldImpl( Schema.STRING, "authIndicator", "NVL2(PAUT.PARTYCD, 'Y', 'N')", tbl_PAUT )
					, new QueryableFieldImpl( Schema.STRING, "partyStatus", "PTYS.STATUS", tbl_PTYS )
					, new QueryableFieldImpl( Schema.STRING, "linkSource", "NVL2(PEMP.PARTYCD, 'S', 'D')", tbl_PEMP )
			} );

			appendCND( new QueryBufferValid.ConditionTrue("_multiSoldTo")
					, new QueryableFieldImpl( Schema.STRING, "employeeId", "PEMP.EMPLOYEE_ID", tbl_PEMP ) );

			QueryBufferValid valid_link = new QueryBufferValid.Condition( "baseLinkType" );
			appendCND( valid_link, new QueryableField[] {
					new QueryableFieldImpl( Schema.STRING, "baseLinkType", "PLNK_BS.LINKTYPE" )
					, new QueryableFieldImpl( Schema.STRING, "baseLinkPartyCode", "PLNK_BS.LINK_PARTYCD" )
			} );
		}

		@Override
		public boolean appendTable( QueryBuffer querybuf ) {
			if( querybuf instanceof ConditionQueryBuffer ) {
				ConditionQueryBuffer condQueryBuffer = (ConditionQueryBuffer)querybuf;
				if( condQueryBuffer.hasConditionValue("baseLinkType") ) {
					QueryBuffer linkQuery = PartyLink.getDistinctPartyLink( condQueryBuffer.getConditionMap() );
					querybuf.appendTable( linkQuery, "PLNK_BS", "PLNK_BS.LINK_PARTYCD = PTYS.PARTYCD" );
				}
			}

			return super.appendTable( querybuf );
		}
	} );

	public PartyAuth( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String partyCode, String organizationCode, String distributorChannelCode, String divisionCode, String uniqId ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "partyCode", partyCode );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "distributorChannelCode", distributorChannelCode );
		primaryMap.put( "divisionCode", divisionCode );
		primaryMap.put( "uniqId", uniqId );

		return primaryMap;
	}

	public List<Map<String, Object>> getAuthOrganizations( Map<String, ? extends Object> conditionMap ) throws SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( conditionMap );

		querybuf.appendDataWithGroupBy( "PAUT.ORGANIZATIONCD" );
		querybuf.appendDataWithAlias( "(SELECT MASTER_NAME FROM DPR_MASTER_DESC SB WHERE SB.MASTER_TYPE = 'SO' AND SB.MASTER_CD = PAUT.ORGANIZATIONCD AND SB.LANGCD = ?)", "organizationCode" );
		querybuf.addBindVariable( 0, conditionMap.get("displayLanguage") );

		querybuf.appendTableWithAlias( "DPR_PARTY_AUTH", "PAUT" );
		querybuf.appendTableWithAlias( "DPR_COUNTRY_COND", "CCND", "CCND.COUNTRYCD = PAUT.COUNTRYCD AND CCND.ORGANIZATIONCD = PAUT.ORGANIZATIONCD" );

		querybuf.findCondition( "divisionCode", "PAUT.DIVISIONCD" );
		querybuf.findCondition( "distributionChannelCode", "PAUT.DIST_CHANNELCD" );
		querybuf.findCondition( "countryCode", "PAUT.COUNTRYCD" );

		return SQLManager.getRecordList( handler, querybuf );
	}

	private List<Map<String, Object>> putValuesToList( List<Map<String, Object>> recordList, Object[] vars ) {
		for( Map<String, Object> record : recordList ) {
			for( int i = 0 ; i < vars.length ; i++ ) {
				try {
					record.put( ((String[]) vars[i])[0], ((String[])vars[i])[1] );
				} catch( ArrayIndexOutOfBoundsException ignored ) {}
			}
		}

		return recordList;
	}

	public boolean updateWithEmployeeId( List<Map<String, Object>> recordList, Map<String, Object> conditionMap ) throws SQLException, DataException {
		Employee employeeDB = new Employee( handler );
		PartyEmployee partyEmployeeDB = new PartyEmployee( handler );
		Party partyDB = new Party( handler );

		String countryCode = (String)conditionMap.get( "countryCode" );
		String divisionCode = (String)conditionMap.get( "divisionCode" );
		List<Map<String, Object>> countryOrganizations = new CountryCondition(handler).getCountryOrganizations(
				conditionMap.get("countryCode"), new String[] { "organizationCode" }
		);

		if( countryOrganizations == null || countryOrganizations.size() == 0 )
			throw handler.createDataException( DataException.ERR_ERROR, handler.getMessageHandler().getMessage("ERR_INTERNAL_ERROR") );

		/** manageRecord 구성 statement : "PUT", "DEL" **/
		List<Map<String, Object>> manageRecordList = new java.util.ArrayList<Map<String, Object>> ();
		for( Map<String, Object> org : countryOrganizations ) {
			Map<String, Object> manageRecord = new java.util.HashMap<String, Object> ();

			String organizationCode = (String)org.get( "organizationCode" );
			if( recordList != null ) {
				for( Map<String, Object> record : recordList ) {
					if( organizationCode != null && organizationCode.equals( record.get("organizationCode") ) ) {
						manageRecord = new java.util.HashMap<String, Object> ( record );
						manageRecord.put( "statement", "PUT" );

						break;
					}
				}
			}

			if( manageRecord.size() <= 0 ) {
				manageRecord.put( "organizationCode", organizationCode );
				manageRecord.put( "statement", "DEL" );
			}

			manageRecordList.add( manageRecord );
		}

		com.irt.data.DataResult totalResult = new com.irt.data.DataResult();
		for( Map<String, Object> manageRecord : manageRecordList ) {
			String statement = (String)manageRecord.get( "statement" );
			String organizationCode = (String)manageRecord.get( "organizationCode" );
			String[] distributionChannelCodes = new com.irt.dpr.CountryDistChannel(handler).getDistributionChannels( countryCode, organizationCode );

			if( "PUT".equals(statement) ) {
				String systemDateTime = (String)SQLManager.getObjectValue( handler
						, "SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM DUAL" );
				if( systemDateTime == null ) return false;

				String employeeId = (String)manageRecord.get( "employeeId" );
				String uniqId = (String)manageRecord.get( "uniqId" );

				DataResult result = null;
				Map<String, Object> _conditionMap = new java.util.HashMap<String, Object> ( conditionMap );
				if( employeeDB.existingEmployeeId(employeeId) ) {
					_conditionMap.put( "employeeId", employeeId );
					_conditionMap.put( "organizationCode", organizationCode );
					_conditionMap.put( "distributionChannelCode", distributionChannelCodes );
					_conditionMap.put( "partyStatus", Party.PARTYSTATUS_ACTIVE );
					_conditionMap.put( "linkType", PartyLink.LINKTYPE_SOLD );

					List<Map<String, Object>> employeeList = partyEmployeeDB.getRecords( _conditionMap
							, new String[] { "employeeId", "partyCode", "countryCode", "organizationCode"
									, "divisionCode", "distributionChannelCode" } );

					if( employeeList != null  && employeeList.size() > 0 ) {
						putValuesToList( employeeList, new Object[] {
								new String[] { "uniqId", uniqId }, new String[] { "authValue", DEFAULT_AUTHORIZATIONVALUE }
								, new String[] { "source", LINKSOURCE_SAP }
						} );

						result = SQLManager.manageRecordAll( handler, table, employeeList, Record.UPDATE | Record.INSERT );
					}
				} else {
					_conditionMap.put( "partyCode", employeeId );
					_conditionMap.put( "organizationCode", organizationCode );
					_conditionMap.put( "distributionChannelCode", distributionChannelCodes );
					_conditionMap.put( "baseLinkPartyCode", employeeId );
					_conditionMap.put( "baseLinkType", PartyLink.LINKTYPE_SOLD );
					_conditionMap.put( "status", Party.PARTYSTATUS_ACTIVE );

					List<Map<String, Object>> partyList = partyDB.getRecords( _conditionMap
							, new String[] { "partyCode", "organizationCode", "countryCode", "divisionCode", "distributionChannelCode" } );
					if( partyList != null  && partyList.size() > 0 ) {
						putValuesToList( partyList, new Object[] {
								new String[] { "uniqId", uniqId }, new String[] { "authValue", DEFAULT_AUTHORIZATIONVALUE }
								, new String[] { "source", null }
						} );

						result = SQLManager.manageRecordAll( handler, table, partyList, Record.UPDATE | Record.INSERT );
					}
				}

				if( result != null && result.getException() != null ) {
					throw result.getException();
				} else if( result != null && result.getSuccessCount() > 0 ) {
					String distributionChannelQuery = "";
					for( int i = 0; i < distributionChannelCodes.length; i++ )
						distributionChannelQuery = distributionChannelQuery + " '" + distributionChannelCodes[i] +"',";
					distributionChannelQuery = distributionChannelQuery.substring( 0, distributionChannelQuery.length()-1 );

					String deleteQuery = "DELETE FROM DPR_PARTY_AUTH WHERE UNIQID = ? AND ORGANIZATIONCD = ?"
							+ " AND DIVISIONCD = ? AND DIST_CHANNELCD IN (" + distributionChannelQuery +") AND SOURCE = ?"
							+ " AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";

					SQLManager.executeStatement( handler, deleteQuery
							, new Object[] { uniqId, organizationCode, divisionCode, LINKSOURCE_SAP, systemDateTime } );

					totalResult.increaseSuccessCount( Record.UPDATE );
				}
			} else if( "DEL".equals(statement) ) {
				Map<String, Object> _conditionMap = new java.util.HashMap<String, Object> ( conditionMap );
				_conditionMap.put( "organizationCode", organizationCode );
				_conditionMap.put( "source", LINKSOURCE_SAP );
				_conditionMap.put( "authIndicator", "Y" );

				if( getRecordCount(_conditionMap) > 0 ) {
					String uniqId = (String)conditionMap.get( "uniqId" );

					String distributionChannelQuery = "";
					for( int i = 0; i < distributionChannelCodes.length; i++ )
						distributionChannelQuery = distributionChannelQuery + " '" + distributionChannelCodes[i] +"',";
					distributionChannelQuery = distributionChannelQuery.substring( 0, distributionChannelQuery.length()-1 );

					int cnt = SQLManager.executeStatement( handler, "DELETE FROM DPR_PARTY_AUTH WHERE UNIQID = ?"
							+ " AND ORGANIZATIONCD = ? AND DIVISIONCD = ?"
							+ " AND DIST_CHANNELCD IN (" + distributionChannelQuery + ") AND SOURCE = ?"
							, new Object[] { uniqId, organizationCode, divisionCode, LINKSOURCE_SAP } );
				}

				totalResult.increaseSuccessCount( Record.DELETE );
			}
		}

		return ( totalResult.getSuccessCount() > 0 );
	}

	public boolean updateWithEmployeeIdCsv( List<Map<String, Object>> recordList, Map<String, Object> conditionMap ) throws SQLException, DataException {
		Employee employeeDB = new Employee( handler );
		PartyEmployee partyEmployeeDB = new PartyEmployee( handler );
		Party partyDB = new Party( handler );

		String countryCode = (String)conditionMap.get( "countryCode" );
		String divisionCode = (String)conditionMap.get( "divisionCode" );
		List<Map<String, Object>> countryOrganizations = new CountryCondition(handler).getCountryOrganizations(
				conditionMap.get("countryCode"), new String[] { "organizationCode" }
				);

		if( countryOrganizations == null || countryOrganizations.size() == 0 )
			throw handler.createDataException( DataException.ERR_ERROR, handler.getMessageHandler().getMessage("ERR_INTERNAL_ERROR") );

		/** manageRecord 구성 statement : "PUT", "DEL" **/
		List<Map<String, Object>> manageRecordList = new java.util.ArrayList<Map<String, Object>> ();
		for( Map<String, Object> org : countryOrganizations ) {
			Map<String, Object> manageRecord = new java.util.HashMap<String, Object> ();

			String organizationCode = (String)org.get( "organizationCode" );
			if( recordList != null ) {
				for( Map<String, Object> record : recordList ) {
					if( organizationCode != null && organizationCode.equals( record.get("organizationCode") ) ) {
						manageRecord = new java.util.HashMap<String, Object> ( record );
						manageRecord.put( "statement", "PUT" );

						break;
					}
				}
			}

			if( manageRecord.size() <= 0 ) {
				manageRecord.put( "organizationCode", organizationCode );
				manageRecord.put( "statement", "DEL" );
			}

			manageRecordList.add( manageRecord );
		}

		com.irt.data.DataResult totalResult = new com.irt.data.DataResult();
		for( Map<String, Object> manageRecord : manageRecordList ) {
			String statement = (String)manageRecord.get( "statement" );
			String organizationCode = (String)manageRecord.get( "organizationCode" );
			String[] distributionChannelCodes = new com.irt.dpr.CountryDistChannel(handler).getDistributionChannels( countryCode, organizationCode );

			boolean paut_updated = false;
			boolean uemp_updated = false;
			String bf_systemDateTime = (String)SQLManager.getObjectValue( handler
					, "SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') FROM DUAL" );
			if( bf_systemDateTime == null ) return false;

			if( "PUT".equals(statement) ) {

				String[] employeeIdArr = UserMultiEmployee.getEmployeeIdArr( manageRecord.get( "employeeId" ) );

				for( String employeeId : employeeIdArr ) {
					String uniqId = (String)manageRecord.get( "uniqId" );

					DataResult paut_result = null;
					DataResult uemp_result = null;
					Map<String, Object> _conditionMap = new java.util.HashMap<String, Object> ( conditionMap );
					if( employeeDB.existingEmployeeId(employeeId) ) {
						_conditionMap.put( "employeeId", employeeId );
						_conditionMap.put( "organizationCode", organizationCode );
						_conditionMap.put( "distributionChannelCode", distributionChannelCodes );
						_conditionMap.put( "partyStatus", Party.PARTYSTATUS_ACTIVE );
						_conditionMap.put( "linkType", PartyLink.LINKTYPE_SOLD );

						List<Map<String, Object>> employeeList = partyEmployeeDB.getRecords( _conditionMap
								, new String[] { "employeeId", "partyCode", "countryCode", "organizationCode"
										, "divisionCode", "distributionChannelCode" } );

						if( employeeList != null  && employeeList.size() > 0 ) {
							putValuesToList( employeeList, new Object[] {
									new String[] { "uniqId", uniqId }, new String[] { "authValue", DEFAULT_AUTHORIZATIONVALUE }
									, new String[] { "source", LINKSOURCE_SAP }
							} );

							uemp_result = SQLManager.manageRecordAll( handler, table, employeeList, Record.UPDATE | Record.INSERT );
						}
					} else {
						_conditionMap.put( "partyCode", employeeId );
						_conditionMap.put( "organizationCode", organizationCode );
						_conditionMap.put( "distributionChannelCode", distributionChannelCodes );
						_conditionMap.put( "baseLinkPartyCode", employeeId );
						_conditionMap.put( "baseLinkType", PartyLink.LINKTYPE_SOLD );
						_conditionMap.put( "status", Party.PARTYSTATUS_ACTIVE );

						List<Map<String, Object>> partyList = partyDB.getRecords( _conditionMap
								, new String[] { "partyCode", "organizationCode", "countryCode", "divisionCode", "distributionChannelCode" } );
						if( partyList != null  && partyList.size() > 0 ) {
							putValuesToList( partyList, new Object[] {
									new String[] { "uniqId", uniqId }, new String[] { "authValue", DEFAULT_AUTHORIZATIONVALUE }
									, new String[] { "source", null }
							} );

							paut_result = SQLManager.manageRecordAll( handler, table, partyList, Record.UPDATE | Record.INSERT );
						}
					}

					if( uemp_result != null && uemp_result.getException() != null ) {
						throw uemp_result.getException();
					} else if( uemp_result != null && uemp_result.getSuccessCount() > 0 ) {
						if( !uemp_updated )
							uemp_updated = true;
					}

					if( paut_result != null && paut_result.getException() != null ) {
						throw paut_result.getException();
					} else if( paut_result != null && paut_result.getSuccessCount() > 0 ) {
						if( !paut_updated )
							paut_updated = true;
					}
				}
			} else if( "DEL".equals(statement) ) {
				Map<String, Object> _conditionMap = new java.util.HashMap<String, Object> ( conditionMap );
				_conditionMap.put( "organizationCode", organizationCode );
				_conditionMap.put( "source", LINKSOURCE_SAP );
				_conditionMap.put( "authIndicator", "Y" );

				if( getRecordCount(_conditionMap) > 0 ) {
					String uniqId = (String)conditionMap.get( "uniqId" );

					String distributionChannelQuery = "";
					for( int i = 0; i < distributionChannelCodes.length; i++ )
						distributionChannelQuery = distributionChannelQuery + " '" + distributionChannelCodes[i] +"',";
					distributionChannelQuery = distributionChannelQuery.substring( 0, distributionChannelQuery.length()-1 );

					int cnt = SQLManager.executeStatement( handler, "DELETE FROM DPR_PARTY_AUTH WHERE UNIQID = ?"
							+ " AND ORGANIZATIONCD = ? AND DIVISIONCD = ?"
							+ " AND DIST_CHANNELCD IN (" + distributionChannelQuery + ") AND SOURCE = ?"
							, new Object[] { uniqId, organizationCode, divisionCode, LINKSOURCE_SAP } );
				}

				totalResult.increaseSuccessCount( Record.DELETE );
			}

			if( uemp_updated ) {
				String uniqId = (String)conditionMap.get("uniqId");
				String distributionChannelQuery = "";
				for( int i = 0; i < distributionChannelCodes.length; i++ )
					distributionChannelQuery = distributionChannelQuery + " '" + distributionChannelCodes[i] +"',";
				distributionChannelQuery = distributionChannelQuery.substring( 0, distributionChannelQuery.length()-1 );

				String deleteQuery = "DELETE FROM DPR_PARTY_AUTH WHERE UNIQID = ? AND ORGANIZATIONCD = ?"
						+ " AND DIVISIONCD = ? AND DIST_CHANNELCD IN (" + distributionChannelQuery +") AND SOURCE = ?"
						+ " AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";

				int deleted = SQLManager.executeStatement( handler, deleteQuery
						, new Object[] { uniqId, organizationCode, divisionCode, LINKSOURCE_SAP, bf_systemDateTime } );

				totalResult.increaseSuccessCount( Record.UPDATE );
			}

			if( paut_updated ) {
				String uniqId = (String)conditionMap.get("uniqId");
				String distributionChannelQuery = "";
				for( int i = 0; i < distributionChannelCodes.length; i++ )
					distributionChannelQuery = distributionChannelQuery + " '" + distributionChannelCodes[i] +"',";
				distributionChannelQuery = distributionChannelQuery.substring( 0, distributionChannelQuery.length()-1 );

				String deleteQuery = "DELETE FROM DPR_PARTY_AUTH WHERE UNIQID = ? AND ORGANIZATIONCD = ?"
						+ " AND DIVISIONCD = ? AND DIST_CHANNELCD IN (" + distributionChannelQuery +") AND NVL(SOURCE,'D') = NVL(?,'D')"
						+ " AND UPGDATE < TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')";

				int deleted = SQLManager.executeStatement( handler, deleteQuery
						, new Object[] { uniqId, organizationCode, divisionCode, null, bf_systemDateTime } );

				totalResult.increaseSuccessCount( Record.UPDATE );
			}
		}

		return ( totalResult.getSuccessCount() > 0 );
	}
}
