/*
 *	File Name:	MoqItemCfg.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/06/30		2.2.1	createDataLoader(): loadLine() 에서 selling sku validator 수행되도록 변경
 *	jbaek		2019/06/30		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.data.DataLoader;
import com.irt.data.DataLoader.Loader;
import com.irt.data.DataLoader.Validator;
import com.irt.data.FieldException;
import com.irt.data.ValidableField;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.rbm.TableAccessor;
import com.irt.rbm.TableDao;
import com.irt.rbm.TableDaoException;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;
import com.irt.util.MessageHandler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MoqItemCfg extends ManipulableManagerImpl implements TableAccessor {
	private final static Table table = Schema.findTable(Schema.DPR_MOQITEM_CFG);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_MOQITEM_CFG);

	private TableDao tdao;

	public MoqItemCfg( SQLHandler handler ) {
		super(handler, table, factory);
		this.tdao = new TableDao(table, handler.getMessageHandler());
	}

	@Override
	public Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, int statementType )
			throws SQLException, UnsupportedOperationException {
		return this.createDataLoader(fieldKeys, defaultMap, null, statementType);
	}

	@Override
	public Loader createDataLoader( String[] fieldKeys, final Map<String, ? extends Object> defaultMap, String[] updateFieldKeys, int statementType )
			throws SQLException, UnsupportedOperationException {
		TableProcDataLoader tableLoader = new TableProcDataLoader(fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType) {
			DataLoader.Validator validator;

			@Override
			public Map<String, Object> loadLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				try {
					validator.validateLine( handler, recordMap );
				} catch ( DataException dataEx ) {
					String errorKey = dataEx.getErrorKey();
					if( "ERR_ISNOT_SELLINGSKU".equals(errorKey) ) {
						Map<String, Object> resultMap = new java.util.TreeMap<String, Object>();
						resultMap.put( "executeType", this.getExecuteType() );
						resultMap.put( "status", com.irt.data.DataLoader.WARNING );
						resultMap.put( "message", dataEx.getMessage() );
						return resultMap;
					}
				}
				return super.loadLine( handler, recordMap );
			}

			@Override
			public void start( SQLHandler handler ) throws SQLException {
				validator = createValidator( (Map<String, Object>)defaultMap );
			}
		};
		tableLoader.setLineProcessor(createLineProcessor(defaultMap));

		//tableLoader.setValidator(createValidator((Map<String, Object>)defaultMap));

		return tableLoader;
	}

	public LineProcessor createLineProcessor( final Map<String, ? extends Object> lineDefaultMap ) {
		return new LineProcessor() {

			Party party = null;

			@Override
			public void close() {
				if( this.party != null )
					this.party = null;
			}

			@Override
			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				// mandatory.
				if( lineDefaultMap.containsKey("updateUserId") )
					recordMap.put("updateUserId", lineDefaultMap.get("updateUserId"));

				if( recordMap.get("partyCode") != null && !"0".equals(recordMap.get("partyCode")) ) {
					if( recordMap.get("distributionChannelCode") == null || "0".equals(recordMap.get("distributionChannelCode")) ) {
						throw new DataException(DataException.ERR_CANNOT_NULL, DataException.ERR_CANNOT_NULL);
					}

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
				if( recordMap.get("groupCode") != null && !"0".equals(recordMap.get("groupCode")) ) {
					if( recordMap.get("distributionChannelCode") == null || "0".equals(recordMap.get("distributionChannelCode")) ) {
						throw new DataException(DataException.ERR_CANNOT_NULL, DataException.ERR_CANNOT_NULL);
					}
				}
				if( recordMap.get("officeCode") != null && !"0".equals(recordMap.get("officeCode")) ) {
					if( recordMap.get("distributionChannelCode") == null || "0".equals(recordMap.get("distributionChannelCode")) ) {
						throw new DataException(DataException.ERR_CANNOT_NULL, DataException.ERR_CANNOT_NULL);
					}
				}

				if( recordMap.get("partyCode") == null )
					recordMap.put("partyCode", "0");
				if( recordMap.get("officeCode") == null )
					recordMap.put("officeCode", "0");
				if( recordMap.get("groupCode") == null )
					recordMap.put("groupCode", "0");
				if( recordMap.get("distributionChannelCode") == null )
					recordMap.put("distributionChannelCode", "0");

				if( recordMap.get("updateUserId") == null )
					recordMap.put("updateUserId", lineDefaultMap.get("updateUserId"));

				return recordMap;

			};
		};
	}

	public Validator createValidator( Map<String, Object> defaultMap ) {
		final Validator moqDateValidator = new Validator() {
			@Override
			public void close() {
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				ValidableField day = new ValidableField(true, "alMoqDay", "DPR_MOQITEM_ALLMOQ_DAY", Schema.INTEGER);
				ValidableField month = new ValidableField(true, "alMoqMonth", "DPR_MOQITEM_ALLMOQ_MONTH", Schema.INTEGER);

				MessageHandler msghandler = handler.getMessageHandler();
				ValidableField f = null;
				try {
					f = day;
					Integer dayMoq = (Integer)f.validate(recordMap);

					f = month;
					Integer monthMoq = (Integer)f.validate(recordMap);

					if( dayMoq != null && monthMoq != null && dayMoq > monthMoq ) {
						String message = msghandler.getMessage(DataException.ERR_INVALID_NUMBERSCOPE,
								( msghandler.getMessage(day.getDescriptionKey()) + "'" + dayMoq + "'" ),
								( msghandler.getMessage(month.getDescriptionKey()) + "'" + monthMoq + "'" ));

						throw new DataException(DataException.ERR_INVALID_NUMBERSCOPE, message, recordMap);
					}
				} catch( FieldException fdEx ) {
					String message = msghandler.getMessage(fdEx.getMessage(),
							msghandler.getMessage(f.getDescriptionKey()));

					throw new DataException(fdEx.getErrorKey(), message, recordMap);
				}
			}
		};
		final Validator sellingSkuValidator = new OrderItem(handler).createSellingSkuValidator(defaultMap);

		Validator multi = new Validator() {
			@Override
			public void close() {
				sellingSkuValidator.close();
				moqDateValidator.close();
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				sellingSkuValidator.validateLine(handler, recordMap);
				moqDateValidator.validateLine(handler, recordMap);
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
