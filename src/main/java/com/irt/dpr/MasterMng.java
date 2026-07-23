/*
 *	File Name:	MasterMng.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/05/30		2.2.0		create
**/

package com.irt.dpr;

import com.irt.data.Condition;
import com.irt.data.DataException;
import com.irt.data.DataLoader.Validator;
import com.irt.data.Field;
import com.irt.data.FieldException;
import com.irt.data.QueryableManager;
import com.irt.data.Record;
import com.irt.data.ValidableField;
import com.irt.data.ValidableFieldSet;
import com.irt.servlet.Schemas;
import com.irt.sql.SQLHandler;
import com.irt.sql.SQLManager;
import com.irt.sql.Table;
import com.irt.util.DaoManager;
import com.irt.util.MapUtil;
import com.irt.util.MessageHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MasterMng {

	public LineProcessor createLineProcessor( MngType mngtype, final Map<String, Object> lineDefaultMap ) {
		List<LineProcessor> lineProcessors = new ArrayList<LineProcessor>();
		switch( mngtype ) {
		case PartySales:
			LineProcessor ptyBase_regProcessor = new LineProcessor() {
				DaoManager partyBase_dm;

				@Override
				public void close() {
					if( partyBase_dm != null )
						partyBase_dm = null;
				}

				@Override
				public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
					if( partyBase_dm == null )
						partyBase_dm = DaoManager.getInstance(Schemas.DPR, com.irt.dpr.Schema.DPR_PARTY, handler);

					if( !recordMap.containsKey("organizationCode") )
						recordMap.put("organizationCode", lineDefaultMap.get("organizationCode"));
					if( !recordMap.containsKey("divisionCode") )
						recordMap.put("divisionCode", lineDefaultMap.get("divisionCode"));
					if( !recordMap.containsKey("countryCode") )
						recordMap.put("countryCode", lineDefaultMap.get("countryCode"));

					Map<String, Object> newMap = new HashMap<String, Object>(recordMap);
					newMap.put("partyType", "DIS");
					if( !newMap.containsKey("status") )
						newMap.put("status", "00");

					if( !partyBase_dm.getManager().existRecord(newMap) ) {
						partyBase_dm.getManager().regist(newMap);
					}

					return recordMap;
				}
			};

			LineProcessor ptyLink_defaultLinkingProcessor = new LineProcessor() {

				PartyLink ptyLink;
				Party ptySales;

				@Override
				public void close() {
					if( ptySales != null )
						ptySales = null;
					if( ptyLink != null )
						ptyLink = null;
				}

				private String[] getCsvArray( Object linkTypeCsv ) {
					String[] defaultLinkType = null;
					if( linkTypeCsv instanceof String[] ) {
						defaultLinkType = (String[])linkTypeCsv;
					} else if( linkTypeCsv instanceof String ) {
						defaultLinkType = ( linkTypeCsv == null ? null : ( (String)linkTypeCsv ).split(",\\s?+") );
					}

					return ( defaultLinkType == null ? new String[] {} : defaultLinkType );
				}

				@Override
				public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {

					if( !recordMap.containsKey("organizationCode") )
						recordMap.put("organizationCode", lineDefaultMap.get("organizationCode"));
					if( !recordMap.containsKey("divisionCode") )
						recordMap.put("divisionCode", lineDefaultMap.get("divisionCode"));
					if( !recordMap.containsKey("countryCode") )
						recordMap.put("countryCode", lineDefaultMap.get("countryCode"));

					Object linkTypeCsv = (Object)recordMap.get("defaultLinkType");
					String[] defaultLink = getCsvArray(linkTypeCsv);
					List<String> defaultLinkList = new ArrayList<String>(new HashSet<String>(java.util.Arrays.asList(defaultLink)));
					String[] defaultLinkOrig = getCsvArray(recordMap.get("defaultLinkType_orig"));

					if( ptySales == null )
						ptySales = new Party(handler);
					if( ptyLink == null )
						ptyLink = new PartyLink(handler);

					if( defaultLinkOrig == null || defaultLinkOrig.length == 0 ) {
						String linkTypeOrigCsv = (String)ptySales.getFieldValue(recordMap, "defaultLinkType");
						if( linkTypeOrigCsv != null )
							defaultLinkOrig = ( (String)linkTypeOrigCsv ).split(",\\s?+");
						else
							defaultLinkOrig = new String[] {};
					}
					List<String> orig_defaultLinkList = new ArrayList<String>(new HashSet<String>(java.util.Arrays.asList(defaultLinkOrig)));

					Map<String, Object> actionMap = new HashMap<String, Object>();
					if( defaultLink != null && defaultLink.length == 1 && "WE".equals(defaultLink[0]) ) {
						Map<String, Object> record_db = ptySales.getRecord(recordMap);
						if( record_db != null )
							recordMap.putAll(record_db);
						recordMap.put("_isShipOnly", "Y");
					}

					for( String orig : orig_defaultLinkList ) {
						if( !defaultLinkList.contains(orig) ) {
							actionMap.put(orig, Record.DELETE);
						}
					}
					if( defaultLinkList == null || defaultLinkList.size() == 0 ) {
						defaultLinkList.addAll(java.util.Arrays.asList(new String[] { "AG", "RE", "RG", "WE" }));
					}
					for( String linkType : defaultLinkList ) {
						if( !orig_defaultLinkList.contains(linkType) ) {
							actionMap.put(linkType, Record.INSERT);
						}
					}

					for( String linkType : actionMap.keySet() ) {
						Integer recordAction = (Integer)actionMap.get(linkType);

						if( ( Record.DELETE & recordAction ) > 0 ) {
							Map<String, Object> newMap = new HashMap<String, Object>(recordMap);
							newMap.remove("linkType");
							newMap.put("linkType", linkType);
							newMap.put("linkPartyCode", newMap.get("partyCode"));
							newMap.put("status", "00");
							String[] primaryKeys = new String[] { "organizationCode", "distributionChannelCode", "divisionCode", "partyCode",
									"linkType", "linkPartyCode" };
							Map primaryMap = MapUtil.getPartialMap(newMap, primaryKeys);
							if( primaryMap.keySet().size() != primaryKeys.length ) {
								throw new DataException(DataException.ERR_CANNOT_NULL, "", newMap);
							}
							ptyLink.delete(newMap);
						} else if( ( Record.INSERT & recordAction ) > 0 ) {
							Map<String, Object> newMap = new HashMap<String, Object>(recordMap);
							newMap.remove("linkType");
							newMap.put("linkType", linkType);
							newMap.put("linkPartyCode", newMap.get("partyCode"));
							newMap.put("displaySequence", getPartyLinkNextDisplaySeq(ptyLink, newMap));
							newMap.put("status", "00");
							if( !ptyLink.existRecord(newMap) ) {
								ptyLink.regist(newMap);
							}
						}
					}

					if( "Y".equals(recordMap.get("_isShipOnly")) ) {
						if( recordMap.containsKey("officeCode") )
							recordMap.remove("officeCode");
						if( recordMap.containsKey("groupCode") )
							recordMap.remove("groupCode");
						if( recordMap.containsKey("districtCode") )
							recordMap.remove("districtCode");
						if( recordMap.containsKey("customerGroupCode") )
							recordMap.remove("customerGroupCode");
					}

					if( !recordMap.containsKey("status") )
						recordMap.put("status", lineDefaultMap.get("status"));

					return recordMap;
				}
			};

			lineProcessors.add(ptyBase_regProcessor);
			lineProcessors.add(ptyLink_defaultLinkingProcessor);
			break;
		case PartyFunction:
			LineProcessor ptyLink_displaySequenceProcessor = new LineProcessor() {
				PartyLink ptyLink;

				@Override
				public void close() {
					if( ptyLink != null )
						ptyLink = null;
				}

				@Override
				public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
					if( ptyLink == null )
						ptyLink = new PartyLink(handler);

					recordMap.put("displaySequence", getPartyLinkNextDisplaySeq(ptyLink, recordMap));

					return recordMap;
				}
			};

			lineProcessors.add(ptyLink_displaySequenceProcessor);
			break;
		}

		return createMultiProcessors(lineProcessors);
	}

	public LineProcessor createMultiProcessors( final List<LineProcessor> processors ) {
		if( processors == null || processors.size() == 0 )
			return null;

		return new LineProcessor() {
			@Override
			public void close() {
				for( LineProcessor processor : processors ) {
					processor.close();
				}
			}

			@Override
			public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				Map<String, Object> passing = null;
				for( LineProcessor processor : processors ) {
					passing = processor.processLine(handler, ( passing == null ? recordMap : passing ));
				}

				return ( passing == null ? recordMap : passing );
			}
		};
	}

	public Validator createMultiValidators( final List<Validator> validators ) {
		if( validators == null || validators.size() == 0 )
			return null;

		return new Validator() {

			@Override
			public void close() {
				for( Validator validator : validators ) {
					validator.close();
				}
			}

			@Override
			public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
				for( Validator validator : validators ) {
					validator.validateLine(handler, recordMap);
				}
			}
		};
	}

	public com.irt.data.DataLoader.Validator createValidator( MngType mngtype ) {
		List<Validator> validators = new ArrayList<Validator>();
		switch( mngtype ) {
		case PartySales:
			Validator ptySales_validator = new Validator() {
				@Override
				public void close() {
				}

				@Override
				public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
					if( !"Y".equals(recordMap.get("_isShipOnly")) ) {
						ValidableFieldSet vset = new ValidableFieldSet(new ValidableField[] {
								new ValidableField(false, "officeCode", "DPR_PARTY_SALESOFFICE_CODE", Field.TYPE_STRING),
								new ValidableField(false, "groupCode", "DPR_PARTY_SALESGROUP_CODE", Field.TYPE_STRING),
								new ValidableField(false, "districtCode", "DPR_PARTY_SALESDISTRICT_CODE", Field.TYPE_STRING),
								new ValidableField(false, "customerGroupCode", "DPR_PARTY_CUSTOMERGROUP_CODE", Field.TYPE_STRING)
						});

						try {
							vset.validate(recordMap);
						} catch( FieldException fieldEx ) {
							throw handler.createDataException(fieldEx, recordMap);
						}
					}
				}
			};

			validators.add(ptySales_validator);
			break;
		case PartyFunction:
			Validator ptyLink_validator = new Validator() {
				PartyLink ptyLink;

				@Override
				public void close() {
					if( ptyLink != null )
						ptyLink = null;
				}

				@Override
				public void validateLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
					if( ptyLink == null )
						ptyLink = new PartyLink(handler);

					if( ptyLink.existRecord(recordMap) ) {
						throw handler.createDataException(DataException.ERR_UNIQUE_CONSTRAINT,
								handler.getMessageHandler().getMessage(DataException.ERR_UNIQUE_CONSTRAINT), recordMap);
					}
				}

			};
			validators.add(ptyLink_validator);
			break;
		}

		return createMultiValidators(validators);
	}

	private Object getPartyLinkNextDisplaySeq( QueryableManager db, Map<String, Object> recordMap ) throws SQLException {
		Map<String, Object> condMap = new HashMap<String, Object>();
		condMap.put("partyCode", recordMap.get("partyCode"));
		condMap.put("linkType", recordMap.get("linkType"));
		condMap.put("countryCode", recordMap.get("countryCode"));
		condMap.put("divisionCode", recordMap.get("divisionCode"));
		condMap.put("distributionChannelCode", recordMap.get("distributionChannelCode"));
		condMap.put("organizationCode", recordMap.get("organizationCode"));

		String[] groupKeys = new String[] { "countryCode", "divisionCode", "organizationCode", "distributionChannelCode", "partyCode",
				"linkType" };
		String[] fieldKeys = new String[] { "nextDisplaySequence" };
		condMap.put(Condition.GROUPING_CONDITIONKEY, groupKeys);

		List<Map<String, Object>> list = db.getRecords(condMap, fieldKeys);
		if( list != null && list.size() > 0 ) {
			return list.get(0).get("nextDisplaySequence");
		}

		return null;
	}

	public enum MngType {
		MasterDesc( "mstdesc" ), //
		ProductCategory( "pcate" ), ItemMaster( "itmbase" ), ItemMasterDesc( "itmdesc" ), ItemMasterUom( "itmuom" ), ItemMasterSales( "itmsales" )//
		, PartySales( "ptysales" ), PartyFunction( "ptyfunc" )//
		;

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
