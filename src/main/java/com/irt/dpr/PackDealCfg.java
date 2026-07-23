/*
 *	File Name:	PackDealCfg.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/05/30		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.data.DataLoader.Loader;
import com.irt.data.DataLoader.Validator;
import com.irt.data.Record;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.rbm.TableAccessor;
import com.irt.rbm.TableDao;
import com.irt.rbm.TableDaoException;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;
import com.irt.util.MessageHandler;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackDealCfg extends ManipulableManagerImpl implements TableAccessor {
	private final static Table table = Schema.findTable(Schema.DPR_PACKDEAL_CFG);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_PACKDEAL_CFG);

	private TableDao tdao;

	public PackDealCfg( SQLHandler handler ) {
		super(handler, table, factory);
		this.tdao = new TableDao(table, handler.getMessageHandler());
	}

	@Override
	public Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, String[] updateFieldKeys, int statementType )
			throws SQLException, UnsupportedOperationException {
		TableProcDataLoader tableLoader = new TableProcDataLoader(fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType);
		tableLoader.setLineProcessor(createLineProcessor(defaultMap));

		tableLoader.setValidator(createValidator((Map<String, Object>)defaultMap));

		return tableLoader;
	}

	public LineProcessor createLineProcessor( final Map<String, ? extends Object> lineDefaultMap ) {
		return new LineProcessor() {

			Party party = null;

			public void close() {
				if( this.party != null )
					this.party = null;
			}

			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				// mandatory
				if( lineDefaultMap.containsKey("updateUserId") )
					recordMap.put("updateUserId", lineDefaultMap.get("updateUserId"));

				if( recordMap.get("partyCode") != null && !"0".equals(recordMap.get("partyCode")) ) {
					if( "0".equals(recordMap.get("officeCode"))
							|| recordMap.get("officeCode") == null
							|| "0".equals(recordMap.get("groupCode"))
							|| recordMap.get("groupCode") == null ) {
						for( String key : lineDefaultMap.keySet() ) {
							if( !recordMap.containsKey(key) ) {
								recordMap.put(key, lineDefaultMap.get(key));
							}
						}

						if( this.party == null )
							this.party = new Party(handler);

						Map<String, Object> map = new HashMap<String, Object>();
						map.putAll(recordMap);
						map.remove("officeCode");
						map.remove("groupCode");

						Map<String, Object> partyMap = party.getRecord(map);
						if( partyMap != null ) {
							recordMap.put("officeCode", partyMap.get("officeCode"));
							recordMap.put("groupCode", partyMap.get("groupCode"));
						}
					}
				}

				if( recordMap.get("partyCode") == null )
					recordMap.put("partyCode", "0");
				if( recordMap.get("officeCode") == null )
					recordMap.put("officeCode", "0");
				if( recordMap.get("groupCode") == null )
					recordMap.put("groupCode", "0");
				if( recordMap.get("updateUserId") == null )
					recordMap.put("updateUserId", lineDefaultMap.get("updateUserId"));

				MessageHandler msghandler = handler.getMessageHandler();
				if( recordMap.get("dealCode") != null ) {
					String dealCode = (String)recordMap.get("dealCode");
					if( dealCode.matches(".*\\s+.*") ) {
						String message = msghandler.getMessage(DataException.ERR_INVALID_CHAR,
								msghandler.getMessage(table.getField("dealCode").getDescriptionKey())
										+ "(" + dealCode + ")");
						throw new DataException(DataException.ERR_INVALID_CHAR, message);
					}
				}

				if( recordMap.get("dealStopInd") == null ) {
					if( "N".equals(recordMap.get("isPackdealDate")) )
						recordMap.put("dealStopInd", "Y");
					else
						recordMap.put("dealStopInd", "N");
				}

				return recordMap;

			};
		};
	}

	public com.irt.data.DataLoader.Validator createValidator( Map<String, Object> defaultMap ) {
		Validator startEndDateValidator = new Validator() {

			@Override
			public void close() {
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				String dealStartDate = (String)recordMap.get("dealStartDate");
				String dealEndDate = (String)recordMap.get("dealEndDate");

				MessageHandler msghandler = handler.getMessageHandler();
				try {
					com.irt.data.Date dealStart = com.irt.data.Date.getInstance(dealStartDate);
					com.irt.data.Date dealEnd = com.irt.data.Date.getInstance(dealEndDate);

					if( dealEnd.before(dealStart) ) {

						String message = msghandler.getMessage("ERR_INVALID_DATESCOPE",
								msghandler.getMessage(table.getField("dealStartDate").getDescriptionKey())
										+ "(" + dealStart.toString() + ")"
										+ " " +
										msghandler.getMessage(table.getField("dealEndDate").getDescriptionKey())
										+ "(" + dealEnd + ")");
						throw new DataException(DataException.ERR_INVALID_DATESCOPE, message);
					}

				} catch( ParseException parseEx ) {
					throw handler.createDataException(DataException.ERR_INVALID_DATE, parseEx.getMessage());
				}
			}
		};

		return startEndDateValidator;
	}

	@Override
	public Map<String, Object> extractPrimary( Map<String, Object> sourceMap, int valueIndex ) throws TableDaoException {
		return tdao.extractPrimary(sourceMap, valueIndex);
	}

	@Override
	public Map<String, Object[]> extractPrimaryKeyValues( Map<String, Object> sourceMap ) throws TableDaoException {
		return tdao.extractPrimaryKeyValues(sourceMap);
	}

	@Override
	public String[] getBindFieldKeys( int statementType ) {
		return tdao.getBindFieldKeys(statementType);
	}

	@Override
	public String[] getPrimaryFieldKeys() {
		return tdao.getPrimaryFieldKeys();
	}

	@Override
	public String[] getReadonlyFieldKeys() {
		return tdao.getReadonlyFieldKeys();
	}

	public Map<String, Object> getUniqHeader( String dealCode ) throws SQLException {
		return getUniqHeader(dealCode, null);
	}

	public Map<String, Object> getUniqHeader( String dealCode, String[] fieldKeys ) throws SQLException {
		List<Map<String, Object>> list = null;
		if( fieldKeys == null )
			list = getRecords(Record.createMap("dealCode", dealCode));
		else
			list = getRecords(Record.createMap("dealCode", dealCode), fieldKeys);

		return ( ( list == null || list.size() == 0 ) ? null : list.get(0) );
	}

	public enum MngType {
		PackDealCfgRlt( "pdrlt" ), PackDealCfg( "pdcfg" ), PackDealItem( "pditm" );

		public static MngType fromValue( String paramvalue ) {
			for( MngType t : values() ) {
				if( t.value.equals(paramvalue) )
					return t;
			}
			return null;
		}

		public static List<Map<String, Object>> namevalues() {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			for( MngType t : values() ) {
				Map<String, Object> map = Record.createMap("code", t.value);
				map.put("name", t.name());
				list.add(map);
			}

			return list;
		}

		public static List<Map<String, Object>> namevalues( MessageHandler msghandler, String prefixKey ) {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			for( MngType t : values() ) {
				Map<String, Object> map = Record.createMap("code", t.value);
				map.put("name", msghandler.getMessage(prefixKey + t.name().toUpperCase()));
				list.add(map);
			}

			return list;
		}

		private final String value;

		MngType( String paramvalue ) {
			this.value = paramvalue;
		}

		public String getValue() {
			return value;
		}
	}

}
