/*
 *	File Name:	RddTrigger.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2019/10/31		2.2.1	getRddValues(): 발주일 <-> 배송일 사이에 휴일이 있을 경우 휴일 계산되도록 변경
 *	hankalam	2019/06/28		2.2.0	create
 *
**/

package com.irt.dpr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.sql.ConditionQueryBuffer;
import com.irt.sql.QueryBuffer;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.sql.TableDataLoader;

/*
	*
	*/
public class RddTrigger extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_RDD_TRG );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_RDD_TRG );

	public RddTrigger( SQLHandler handler ) {
		super( handler, table, factory );
	}

	@Override
	public DataLoader.Loader createDataLoader( String[] fieldKeys, final Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
			, int statementType ) throws SQLException {
		PreparedStatement pstmt_del = null;
		try {
			Connection conn = handler.getConnection();
			pstmt_del = conn.prepareStatement( "DELETE FROM DPR_RDD_TRG" );
			pstmt_del.executeUpdate();
		} finally { if( pstmt_del != null ) pstmt_del.close(); }

		return new TableDataLoader( fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType ) {
			@Override
			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				String distributionChannelCode = (String)recordMap.get( "distributionChannelCode" );
				String officeCode = (String)recordMap.get( "officeCode" );
				if( distributionChannelCode == null || distributionChannelCode.trim().length() < 1 ) {
					recordMap.put( "distributionChannelCode", "0" );
				}
				if( officeCode == null || officeCode.trim().length() < 1 ) {
					recordMap.put( "officeCode", "0" );
				}

				String allowDays = (String)recordMap.get( "allowDays" );
				if( allowDays != null && allowDays.length() > 0 ) {
					allowDays = allowDays.replaceAll( " ", "" );
					for( String allowDay : allowDays.split(",") ) {
						try {
							int d = Integer.parseInt( allowDay );
							if( d < 1 || d > 7 ) {
								throw handler.createDataException( DataException.ERR_ERROR
										, handler.getMessageHandler().getMessage("ERR_RDDIND_INVALID_ALLOWDAYS") );
							}
						} catch( NumberFormatException nfEx ) {
							throw handler.createDataException( DataException.ERR_ERROR
									, handler.getMessageHandler().getMessage("ERR_RDDIND_INVALID_ALLOWDAYS") );
						}
					}
					recordMap.put( "allowDays", allowDays );
				}
				String dayOfWeeks = (String) recordMap.get( "dayOfWeek" );
				if( dayOfWeeks != null && dayOfWeeks.length() > 0 ) {
					dayOfWeeks = dayOfWeeks.replaceAll( " ", "" );
					for( String dayOfWeek : dayOfWeeks.split(",") ) {
						try {
							int d = Integer.parseInt( dayOfWeek );
							if( d < 1 || d > 7 ) {
								throw handler.createDataException( DataException.ERR_ERROR
										, handler.getMessageHandler().getMessage("ERR_RDDIND_INVALID_DAYOFWEEK") );
							}
						} catch( NumberFormatException nfEx ) {
							throw handler.createDataException( DataException.ERR_ERROR
									, handler.getMessageHandler().getMessage("ERR_RDDIND_INVALID_DAYOFWEEK") );
						}
					}
					recordMap.put( "dayOfWeek", dayOfWeeks );
				}
				recordMap.put( "trgKey", createSequence() );
				return super.processLine( handler, recordMap );
			}
		};
	}

	public Object createSequence() throws SQLException {
		return SQLManager.getObjectValue( handler, "SELECT seqDPR_RDD_TRG.nextval from dual" );
	}

	public static Map<String, Object> createPrimary( String organizationCode, String distributionChannelCode, String officeCode ) {
		return createPrimary( null, organizationCode, distributionChannelCode, officeCode );
	}

	public static Map<String, Object> createPrimary( String trgKey, String organizationCode, String distributionChannelCode, String officeCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		if( trgKey != null ) {
			primaryMap.put( "trgKey", trgKey );
		}
		primaryMap.put( "organizationCode", organizationCode );
		if( distributionChannelCode != null ) {
			primaryMap.put( "distributionChannelCode", distributionChannelCode );
		}
		if( officeCode != null ) {
			primaryMap.put( "officeCode", officeCode );
		}

		return primaryMap;
	}

	public String getCurrentDate( TimeZone timezone ) {
		Calendar currentDate = Calendar.getInstance( timezone );
		String currentDay = new java.text.SimpleDateFormat( "yyyyMMdd" ).format( currentDate.getTime() );
		return currentDay;
	}

	public boolean isPredefiendRdd( String organizationCode, String distributionChannelCode, String divisionCode, String soldPartyCode, String shipPartyCode ) throws SQLException {
		Party partyDB = new com.irt.dpr.Party( handler );
		RddIndicator indDB = new RddIndicator( handler );
		boolean isPredefined = indDB.isPredefined( organizationCode, distributionChannelCode, divisionCode, shipPartyCode );

		Map<String, Object> conditionMap = Party.createPrimary( soldPartyCode, organizationCode, distributionChannelCode, divisionCode );
		Map<String, Object> recordMap = partyDB.getRecord( conditionMap, new String[] { "officeCode", "groupCode" } );
		if( recordMap == null || recordMap.size() < 1 ) {
			return false;
		}

		if( isPredefined ) {
			Calendar cal = Calendar.getInstance() ;
			cal.setTime( com.irt.data.Date.getInstance() );
			int currDayOfWeek = cal.get( Calendar.DAY_OF_WEEK );

			String officeCode = (String) recordMap.get( "officeCode" );
			conditionMap = createPrimary( organizationCode, distributionChannelCode, officeCode );
			Condition.putConditionValueOnly( conditionMap, "dayOfWeek", "%" + String.valueOf(currDayOfWeek) + "%", Condition.CONDTYPE_LIKE );
			recordMap = getRecord( conditionMap, new String[] { "dayOfWeek", "allowDays" } );
			if( recordMap == null || recordMap.size() < 1 ) {
				conditionMap.putAll( createPrimary(organizationCode, "0", officeCode) );
				recordMap = getRecord( conditionMap, new String[] { "dayOfWeek", "allowDays" } );
			}

			if( recordMap == null || recordMap.size() < 1 ) {
				conditionMap.putAll( createPrimary(organizationCode, distributionChannelCode, "0") );
				recordMap = getRecord( conditionMap, new String[] { "dayOfWeek", "allowDays" } );
			}

			if( recordMap == null || recordMap.size() < 1 ) {
				conditionMap.putAll( createPrimary(organizationCode, "0", "0") );
				recordMap = getRecord( conditionMap, new String[] { "dayOfWeek", "allowDays" } );
			}

			if( recordMap == null || recordMap.size() < 1 ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Map<String, Object> getRecord( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException {
		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		return SQLManager.getRecordMap( handler, null, querybuf );
	}

	public List<Map<String, Object>> getRddValues( boolean isPredefined, String organizationCode, String distributionChannelCode
			, String officeCode, String groupCode ) throws SQLException {

		String dayOfWeeks = null;
		String allowDays;
		Calendar cal = Calendar.getInstance();
		cal.setTime( com.irt.data.Date.getInstance() );
		int currDayOfWeek = cal.get( Calendar.DAY_OF_WEEK );

		if( !isPredefined ) {
			allowDays = "2";
			dayOfWeeks = "1,2,3,4,5,6,7";
		} else {
			Map<String, Object> conditionMap = createPrimary( organizationCode, distributionChannelCode, officeCode );
			Condition.putConditionValueOnly( conditionMap, "dayOfWeek", "%" + String.valueOf(currDayOfWeek) + "%", Condition.CONDTYPE_LIKE );

			Map<String, Object> recordMap = getRecord( conditionMap, new String[] { "dayOfWeek", "allowDays" } );
			if( recordMap == null || recordMap.size() < 1 ) {
				conditionMap.putAll( createPrimary(organizationCode, "0", officeCode) );
				recordMap = getRecord( conditionMap, new String[] { "dayOfWeek", "allowDays" } );
			}

			if( recordMap == null || recordMap.size() < 1 ) {
				conditionMap.putAll( createPrimary(organizationCode, distributionChannelCode, "0") );
				recordMap = getRecord( conditionMap, new String[] { "dayOfWeek", "allowDays" } );
			}

			if( recordMap == null || recordMap.size() < 1 ) {
				conditionMap.putAll( createPrimary(organizationCode, "0", "0") );
				recordMap = getRecord( conditionMap, new String[] { "dayOfWeek", "allowDays" } );
			}

			if( recordMap == null || recordMap.size() < 1 ) {
				return null;
			}

			dayOfWeeks = (String)recordMap.get( "dayOfWeek" );
			allowDays = (String) recordMap.get( "allowDays" );
		}

		List<Map<String, Object>> rddValues = new java.util.ArrayList<Map<String, Object>>();

		boolean isRddTrigger = dayOfWeeks != null ? dayOfWeeks.indexOf( String.valueOf(currDayOfWeek) ) > -1 : false;

		if( !isPredefined || isRddTrigger ) {
			Calendar lastCal = (Calendar) cal.clone();
			int lastAllowDay = 0;

			for( String day : allowDays.split(",") ) {
				int allowDay = Integer.parseInt( day ) - lastAllowDay;
				lastAllowDay = Integer.parseInt( day );
				PartyOperation operDB = new PartyOperation( handler );
				String operInd = null;
				int count = 0;
				int totalCount = 0;
				do {
					lastCal.add( Calendar.DATE, 1 );
					com.irt.data.Date operDate = com.irt.data.Date.getInstance( lastCal.getTime() );
					operInd = operDB.getOperationInd( organizationCode, distributionChannelCode, officeCode, groupCode, operDate, "delvInd" );
					if( !"N".equals(operInd) ) {
						count++;
					}
					totalCount++;
				} while( count < allowDay && totalCount < 30 );

				if( totalCount < 30 ) {
					lastCal = (Calendar) lastCal.clone();
					String dateValue = com.irt.data.Date.toString( lastCal.getTime() );
					Map<String, Object> map = new java.util.HashMap<String, Object>();
					map.put("dateValue", dateValue);
					rddValues.add( map );
				}
			}
		}

		return rddValues;
	}
}
