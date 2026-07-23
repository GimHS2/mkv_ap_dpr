/*
 *	File Name:	StopItemCfg.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.data.DataLoader.Loader;
import com.irt.data.DataLoader.Validator;
import com.irt.data.FieldException;
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
import java.util.HashMap;
import java.util.Map;

public class StopItemCfg extends ManipulableManagerImpl implements TableAccessor {
	private final static Table table = Schema.findTable(Schema.DPR_STOPITEM_CFG);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_STOPITEM_CFG);

	TableDao tdao;

	public StopItemCfg( SQLHandler handler ) {
		super(handler, table, factory);
		this.tdao = new TableDao(table, handler.getMessageHandler());
	}

	@Override
	public Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, int statementType )
			throws SQLException, UnsupportedOperationException {
		return this.createDataLoader(fieldKeys, defaultMap, null, statementType);
	}

	@Override
	public Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, String[] updateFieldKeys, int statementType )
			throws SQLException, UnsupportedOperationException {
		TableProcDataLoader tableLoader = new TableProcDataLoader(fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType);
		tableLoader.setLineProcessor(createLineProcessor(defaultMap));

		tableLoader.setValidator(createValidator((Map<String, Object>)defaultMap));

		return tableLoader;
	}

	public com.irt.dpr.LineProcessor createLineProcessor( final Map<String, ? extends Object> lineDefaultMap ) {
		return new com.irt.dpr.LineProcessor() {
			Party party = null;

			public void close() {
				if( this.party != null )
					this.party = null;
			}

			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				// mandatory.
				if( lineDefaultMap.containsKey("updateUserId") )
					recordMap.put("updateUserId", lineDefaultMap.get("updateUserId"));

				if( recordMap.get("partyCode") != null && !"0".equals(recordMap.get("partyCode")) ) {
					if( "0".equals(recordMap.get("officeCode"))
							|| recordMap.get("officeCode") == null
							|| "0".equals(recordMap.get("groupCode"))
							|| recordMap.get("groupCode") == null ) {
						if( lineDefaultMap != null ) {
							for( String key : lineDefaultMap.keySet() ) {
								if( !recordMap.containsKey(key) ) {
									recordMap.put(key, lineDefaultMap.get(key));
								}
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

				return recordMap;
			}
		};
	}

	public com.irt.data.DataLoader.Validator createValidator( Map<String, Object> defaultMap ) {
		final Validator stopStartEndDateValidator = new Validator() {
			@Override
			public void close() {
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				try {
					table.getFieldMap().get("stopStartDate").validate(recordMap);
					table.getFieldMap().get("stopEndDate").validate(recordMap);
				} catch( FieldException fieldEx ) {
					throw handler.createDataException(fieldEx, recordMap);
				}

				String stopStartDate = (String)recordMap.get("stopStartDate");
				String stopEndDate = (String)recordMap.get("stopEndDate");
				MessageHandler msghandler = handler.getMessageHandler();
				try {

					com.irt.data.Date stopStart = com.irt.data.Date.getInstance(stopStartDate);
					com.irt.data.Date stopEnd = com.irt.data.Date.getInstance(stopEndDate);
					if( stopEnd.before(stopStart) ) {
						String message = msghandler.getMessage("ERR_INVALID_DATESCOPE",
								msghandler.getMessage(table.getField("stopStartDate").getDescriptionKey())
										+ "(" + stopStart.toString() + ")"
										+ " " +
										msghandler.getMessage(table.getField("stopEndDate").getDescriptionKey())
										+ "(" + stopEnd + ")");
						throw new DataException(DataException.ERR_INVALID_DATESCOPE, message);
					}
				} catch( ParseException parseEx ) {
					throw handler.createDataException(DataException.ERR_INVALID_DATE, parseEx.getMessage());
				}
			}
		};
		final Validator sellingSkuValidator = new OrderItem(handler).createSellingSkuValidator((Map<String, Object>)defaultMap);

		Validator multi = new Validator() {
			@Override
			public void close() {
				sellingSkuValidator.close();
				stopStartEndDateValidator.close();
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				sellingSkuValidator.validateLine(handler, recordMap);
				stopStartEndDateValidator.validateLine(handler, recordMap);
			}
		};
		return multi;
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
}
