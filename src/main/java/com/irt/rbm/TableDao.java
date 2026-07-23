/*
 *	File Name:	TableDao.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.0c	create
 *
**/

package com.irt.rbm;

import com.irt.data.Field;
import com.irt.data.Record;
import com.irt.sql.Table;
import com.irt.util.MessageHandler;
import com.irt.util.Utility2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Access Function for {@link com.irt.sql.Table}
 */
public class TableDao implements TableAccessor {

	public static String getMessageArgumentable( MessageHandler msghandler, String[] keys ) {
		StringBuilder sb = new StringBuilder();
		String delim = ", ";
		sb.append("[");
		for( String key : keys ) {
			sb.append("'");
			sb.append(msghandler.getMessage(key));
			sb.append("'");
			sb.append(delim);
		}
		sb.delete(sb.length() - delim.length(), sb.length());
		sb.append("]");
		return sb.toString();
	}

	public static String getMessageArgumentable( MessageHandler msghandler, Table.Field[] fields ) {
		String[] descKeys = new String[fields.length];
		int cnt = 0;
		for( Field field : fields ) {
			descKeys[cnt++] = field.getDescriptionKey();
		}
		return getMessageArgumentable(msghandler, descKeys);
	}

	private static boolean isArray( Object obj ) {
		return obj != null && obj.getClass().isArray();
	}

	Table table;

	MessageHandler msghandler;

	public boolean isReadonly( String key ) {
		return getReadonlyFieldKeyList().contains(key);
	}

	public TableDao( Table table, MessageHandler msghandler ) {
		this.table = table;
		this.msghandler = msghandler;
	}

	@Override
	public Map<String, Object> extractPrimary( Map<String, Object> sourceMap, int valueIndex ) throws TableDaoException {
		if( sourceMap == null )
			throw new TableDaoException(table, TableDaoException.ERR_TAO_MAP_IS_NULL, null);

		Map<String, Object> retMap = new TreeMap<String, Object>();
		String[] primaryKeys = getPrimaryFieldKeys();
		for( String pk : primaryKeys ) {
			for( String key : sourceMap.keySet() ) {
				if( key.equals(pk) ) {
					Object obj = sourceMap.get(key);
					if( isArray(obj) ) {
						Object[] arr = (Object[])obj;
						if( arr.length > 0 ) {
							Object val = null;
							try {
								val = arr[valueIndex];
							} catch( IndexOutOfBoundsException idxEx ) {
								throw new TableDaoException(table,
										TableDaoException.ERR_TAO_MAP_KEYVALUE_ARRAY_CANNOT_DETERMINE, //
										getMessageArgumentable(msghandler, table.getPrimaryFieldArray()), //
										sourceMap);
							}
							retMap.put(key, val);
						} else {
							throw new TableDaoException(table,
									TableDaoException.ERR_TAO_MAP_KEYVALUE_NOT_FOUND, //
									getMessageArgumentable(msghandler, table.getPrimaryFieldArray()), //
									sourceMap);
						}
					} else {
						retMap.put(key, obj);
					}
				}
			}
		}
		if( primaryKeys.length != retMap.size() ) {
			throw new TableDaoException(table,
					TableDaoException.ERR_TAO_MAP_KEY_MISSED, //
					getMessageArgumentable(msghandler, table.getPrimaryFieldArray()), //
					sourceMap);
		}

		return retMap;
	}

	@Override
	public Map<String, Object[]> extractPrimaryKeyValues( Map<String, Object> sourceMap ) throws TableDaoException {
		Map<String, Object[]> retMap = new TreeMap<String, Object[]>();
		String[] primaryKeys = getPrimaryFieldKeys();
		for( String pk : primaryKeys ) {
			for( String key : sourceMap.keySet() ) {
				if( key.equals(pk) ) {
					Object obj = sourceMap.get(key);
					if( isArray(obj) ) {
						Object[] arr = (Object[])obj;
						if( arr.length > 0 ) {
							retMap.put(key, arr);
						} else {
							throw new TableDaoException(table,
									TableDaoException.ERR_TAO_MAP_KEYVALUE_NOT_FOUND, //
									getMessageArgumentable(msghandler, table.getPrimaryFieldArray()), //
									sourceMap);
						}
					} else {
						retMap.put(key, new Object[] { obj });
					}
				}
			}
		}

		int firstObjLength = -1;
		for( String key : retMap.keySet() ) {
			Object[] vals = retMap.get(key);
			if( firstObjLength == -1 )
				firstObjLength = vals.length;
			if( firstObjLength != vals.length )
				throw new TableDaoException(table,
						TableDaoException.ERR_TAO_MAP_KEY_MISSED, //
						getMessageArgumentable(msghandler, table.getPrimaryFieldArray()), //
						sourceMap);
		}
		return retMap;
	}

	public List<String> getAlterableFieldKeyList() {
		Table.Field[] fields_all = table.getBindFieldArray(Record.INSERT);
		if( fields_all == null ) {
			return new ArrayList<String>();
		} else {
			Table.Field[] fields_update = table.getBindFieldArray(Record.UPDATE);
			List<String> fieldKeys = new ArrayList<String>();
			List<String> primary = getPrimaryFieldKeyList();
			for( Field fd : fields_update ) {
				if( !primary.contains(fd.getFieldKey()) ) {
					fieldKeys.add(fd.getFieldKey());
				}
			}
			return fieldKeys;
		}
	}

	public String[] getAlterableFieldKeys() {
		return getAlterableFieldKeyList().toArray(new String[0]);
	}

	public Table.Field[] getAlterableFields() {
		return table.getAlterableFieldArray(table.getFieldKeyArray());
	}

	public List<String> getBindFieldKeyList( int statementType ) {
		Table.Field[] fields = table.getBindFieldArray(statementType);
		List<String> fieldKeys = new ArrayList<String>();

		for( Field field : fields ) {
			fieldKeys.add(field.getFieldKey());
		}
		return fieldKeys;
	}

	@Override
	public String[] getBindFieldKeys( int statementType ) {
		List<String> fieldKeys = getBindFieldKeyList(statementType);
		return fieldKeys.toArray(new String[fieldKeys.size()]);
	}

	public List<String> getPrimaryFieldKeyList() {
		List<String> fieldKeys = new ArrayList<String>();
		Field[] primary = table.getPrimaryFieldArray();
		if( primary != null ) {
			for( Field fd : primary ) {
				fieldKeys.add(fd.getFieldKey());
			}
		}
		return fieldKeys;
	}

	@Override
	public String[] getPrimaryFieldKeys() {
		return getPrimaryFieldKeyList().toArray(new String[0]);
	}

	public List<String> getReadonlyFieldKeyList() {
		List<String> fieldKeys_all = Arrays.asList(table.getFieldKeyArray());
		List<String> fieldKeys_alt = getBindFieldKeyList(Record.INSERT);
		fieldKeys_alt.addAll(getBindFieldKeyList(Record.UPDATE));
		return Utility2.listSubtract(fieldKeys_all, fieldKeys_alt);
	}

	@Override
	public String[] getReadonlyFieldKeys() {
		List<String> fieldKeys = getReadonlyFieldKeyList();
		return fieldKeys.toArray(new String[fieldKeys.size()]);
	}

	public Table getTable() {
		return table;
	}
}
