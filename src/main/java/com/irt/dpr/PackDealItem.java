/*
 *	File Name:	PackDealItem.java
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
import com.irt.data.FieldException;
import com.irt.data.Record;
import com.irt.data.ValidableField;
import com.irt.rbm.ManipulableManagerImpl;
import com.irt.rbm.TableAccessor;
import com.irt.rbm.TableDao;
import com.irt.rbm.TableDaoException;
import com.irt.servlet.ServletModelException;
import com.irt.sql.QueryFactory;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.util.MapUtil;
import com.irt.util.MessageHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class PackDealItem extends ManipulableManagerImpl implements TableAccessor {
	private final static Table table = Schema.findTable(Schema.DPR_PACKDEAL_ITEM);
	private final static QueryFactory factory = Schema.findQueryFactory(Schema.DPR_PACKDEAL_ITEM);

	private TableDao tdao;

	public PackDealItem( SQLHandler handler ) {
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
		TableProcDataLoader tableLoader = new TableProcDataLoader(fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType);

		LineProcessor multi = new LineProcessor() {
			private LineProcessor l1 = createLineProcessor(defaultMap);
			private LineProcessor l2 = createLineMoqProcessor(defaultMap);

			@Override
			public void close() {
				if( l1 != null )
					l1.close();
				if( l2 != null )
					l2.close();
			}

			@Override
			public synchronized Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap )
					throws DataException, SQLException {

				Map<String, Object> m1 = l1.processLine(handler, recordMap);

				Map<String, Object> m2 = l2.processLine(handler, m1);

				return m2;
			}
		};

		tableLoader.setLineProcessor(multi);

		tableLoader.setValidator(createValidator((Map<String, Object>)defaultMap));

		return tableLoader;
	}

	static Logger logger = Logger.getLogger(PackDealItem.class);

	public LineProcessor createLineMoqProcessor( final Map<String, ? extends Object> lineDefaultMap ) {
		return new LineProcessor() {

			MoqItemCfg moqcfg;
			PackDealCfg pdcfg;

			@Override
			public void close() {
				if( moqcfg != null )
					moqcfg = null;
				if( pdcfg != null )
					pdcfg = null;
			}

			@Override
			public synchronized Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap )
					throws DataException, SQLException {
				// update to moqitem table

				String[] fieldKeys = new String[] { "organizationCode", "distributionChannelCode", "officeCode", "groupCode", "partyCode" };
				if( recordMap.get("pdMoqDay") != null || recordMap.get("pdMoqMonth") != null ) {

					if( !recordMap.containsKey("dealCode") )
						throw new DataException(DataException.ERR_CANNOT_NULL,
								handler.getMessageHandler().getMessage(DataException.ERR_CANNOT_NULL, "FIELD_DPR_PACKDEAL_CODE"),
								recordMap);

					if( pdcfg == null )
						pdcfg = new PackDealCfg(handler);

					Map<String, Object> conditionMap = pdcfg.getUniqHeader((String)recordMap.get("dealCode"));

					Map<String, Object> moqmap = MapUtil.getPartialMap(conditionMap, fieldKeys);
					if( moqmap == null )
						throw new DataException(ServletModelException.NO_RECORD_FOUND,
								handler.getMessageHandler().getMessage(ServletModelException.NO_RECORD_FOUND, "${FIELD_DPR_PACKDEAL_CODE}"),
								recordMap);

					moqmap.put("itemCode", recordMap.get("itemCode"));
					moqmap.put("pdMoqDay", recordMap.get("pdMoqDay"));
					moqmap.put("pdMoqMonth", recordMap.get("pdMoqMonth"));
					moqmap.put("updateUserId",
							( recordMap.get("updateUserId") == null ? lineDefaultMap.get("updateUserId") : recordMap.get("updateUserId") ));
					moqmap.put("status", "00");


					if( moqcfg == null )
						moqcfg = new MoqItemCfg(handler);
					if( moqcfg.modify(moqmap, new String[] { "pdMoqDay", "pdMoqMonth", "updateUserId" }) || moqcfg.regist(moqmap) )
						;

					recordMap.putAll(MapUtil.getPartialMap(moqmap, fieldKeys));
				} else {
					if( pdcfg == null )
						pdcfg = new PackDealCfg(handler);
					Map<String, Object> conditionMap = pdcfg.getUniqHeader((String)recordMap.get("dealCode"));
					recordMap.putAll(MapUtil.getPartialMap(conditionMap, fieldKeys));
				}

				//
				return recordMap;
			};
		};
	}

	public LineProcessor createLineProcessor( final Map<String, ? extends Object> lineDefaultMap ) {
		return new LineProcessor() {

			@Override
			public void close() {
			}

			@Override
			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				// mandatory
				if( lineDefaultMap.containsKey("updateUserId") )
					recordMap.put("updateUserId", lineDefaultMap.get("updateUserId"));

				if( recordMap.get("packdealDisplaySeq") == null ) {
					recordMap.put("packdealDisplaySeq", 0);
				}
				if( recordMap.get("organizationCode") == null ) {
					recordMap.put("organizationCode", lineDefaultMap.get("organizationCode"));
				}

				return recordMap;
			};
		};
	}

	public Validator createValidator( Map<String, Object> defaultMap ) {
		final Validator moqDatesValidator = new Validator() {
			@Override
			public void close() {
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				ValidableField day = new ValidableField(true, "pdMoqDay", "DPR_MOQITEM_PACKDEALMOQ_DAY", Schema.INTEGER);
				ValidableField month = new ValidableField(true, "pdMoqMonth", "DPR_MOQITEM_PACKDEALMOQ_MONTH", Schema.INTEGER);

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
				moqDatesValidator.close();
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				Logger.getRootLogger().debug("doing sslvalidator");
				sellingSkuValidator.validateLine(handler, recordMap);

				Logger.getRootLogger().debug("doing moq");
				moqDatesValidator.validateLine(handler, recordMap);
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

	public String getName( String itemCode, String displayLanguageCode ) throws SQLException {
		return (String)SQLManager.getObjectValue(handler, "SELECT ITEMNAME FROM DPR_ITEM_MASTER_DESC WHERE ITEMCD = ? AND LANGCD = ?",
				new Object[] { itemCode, displayLanguageCode });
	}

	public List<Map<String, Object>> getOrderItems( Map<String, Object> conditionMap, String[] fieldKeys, boolean getOnlyAvail ) throws SQLException {
		if( getOnlyAvail ) {
			conditionMap.put("isStopItem", "N");
			conditionMap.put("isCloseItem", "N");
			conditionMap.put("isSslOrder", "Y");
			conditionMap.put("isPackdealDate", "Y");
		}

		return getRecords(conditionMap, fieldKeys);
	}

	public List<Map<String, Object>> getPackDealItems( Object[] dealCodes, String[] fieldKeys ) throws SQLException {
		return getRecords(Record.createMap("dealCode", dealCodes), fieldKeys);
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
