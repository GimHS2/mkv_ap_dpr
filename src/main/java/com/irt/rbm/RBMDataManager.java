/*
 *	File Name:	RBMDataManager.java
 *	Version:	2.2.2c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2026/03/31		2.2.2c	appendOrderBy(): NULLS FIRST/LAST 옵션 추가
 *	jbaek		2019/05/30		2.2.2c	write(): setColumnList call 추가
 *	stghr12		2008/05/31		2.2.2	write(): writingTitle -> writingOption(writingTitle, writingValueLiterally 포함)
 *	stghr12		2008/03/31		2.2.1	write(): ColumnUtility.writeTitle(out, columnList) -> ColumnUtility.writeTitle(out, columnList, msghandler)
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										SQLManager 변경사항 적용: fieldMap 삭제
 *										sortKeyList: default -> protected
 *	stghr12		2007/07/31		2.1.2	updateTable(): checkExistance 버그 수정
 *	stghr12		2007/04/30		2.1.1	updateTable(): (DataException)result.getErrors().iterator().next() -> result.getException()
 *										write() 추가
 *	stghr12		2006/12/01		2.1.0	setSort(): sortKey를 sortKeyList에 추가할 때만 sortKeyList 초기화하도록 수정
 *										appendOrderBy(QueryBuffer, QueryFactory), getSortKeys() 추가
 *										updateTable() 통합, existData() 삭제
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.rbm;

import com.irt.data.*;
import com.irt.data.cols.ColumnList;
import com.irt.data.cols.ColumnUtility;
import com.irt.sql.*;
import com.irt.util.Utility;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RBMDataManager {
	protected SQLHandler handler;
	protected List<String> sortKeyList;

	protected RBMDataManager( SQLHandler handler ) {
		this.handler = handler;
		this.sortKeyList = null;
	}

	/**
	 * querybuf에 ORDER BY 추가. default sort가 필요한 경우 overriding.
	 * @see #QueryBuffer.appendOrderByFieldName( String, String )
	 */
	protected boolean appendOrderBy( QueryBuffer querybuf ) {
		if( sortKeyList != null ) {
			for( String sortKey : sortKeyList ) {
				String[] keys = sortKey.split( "#", 2 );
				if( keys.length < 2 )
					querybuf.appendOrderByFieldName( keys[0] );
				else if( "DESC".equalsIgnoreCase(keys[1]) )
					querybuf.appendOrderByFieldName( keys[0], "DESC NULLS LAST" );
				else if( "ASC".equalsIgnoreCase(keys[1]) )
					querybuf.appendOrderByFieldName( keys[0], "ASC NULLS FIRST" );
				else
					querybuf.appendOrderByFieldName( keys[0], "NULLS FIRST" );
			}
			return true;
		}

		return false;
	}

	/**
	 * querybuf에 ORDER BY 추가. default sort가 필요한 경우 overriding.
	 * @see #QueryBuffer.appendOrderByFieldName( String, String )
	 */
	protected boolean appendOrderBy( QueryBuffer querybuf, QueryFactory factory ) {
		if( sortKeyList != null && sortKeyList.size() > 0 ) {
			String[] sortKeys = new String[ sortKeyList.size() ];
			String[] sortWays = new String[ sortKeyList.size() ];

			int k = 0;
			for( String sortKey : sortKeyList ) {
				String[] keys = sortKey.split( "#", 2 );
				if( keys.length >= 2 ) {
					if( "DESC".equalsIgnoreCase(keys[1]) )
						sortWays[k] = keys[1].toUpperCase() +" NULLS LAST";
					else if( "ASC".equalsIgnoreCase(keys[1]) )
						sortWays[k] = keys[1].toUpperCase() +" NULLS FIRST";
				}
				sortKeys[k++] = keys[0];
			}
			factory.setDataQuery( querybuf, sortKeys );
			for( k = 0; k < sortKeys.length; k++ ) {
				if( sortWays[k] == null )
					querybuf.appendOrderByFieldName( sortKeys[k], "NULLS FIRST" );
				else
					querybuf.appendOrderByFieldName( sortKeys[k], sortWays[k] );
			}

			return true;
		}

		return false;
	}

	public void appendSort( String sortKey ) {
		String normalizeSortKey = Utility.normalizeSortKey( sortKey );
		if( normalizeSortKey == null ) return;

		try {
			sortKeyList.add( normalizeSortKey );
		} catch( NullPointerException nullEx ) {
			sortKeyList = new java.util.ArrayList<String>();
			sortKeyList.add( normalizeSortKey );
		}
	}

	public void clearSort() {
		sortKeyList = null;
	}

	protected Map<String, Object> getRecordMap( QueryFactory factory, Table table, Map<String, ? extends Object> primaryMap ) throws SQLException {
		try {
			QueryBuffer querybuf = table.setPrimaryConditionQuery( new ConditionQueryBuffer(primaryMap) );
			return SQLManager.getRecordMap( handler, null, factory.setDataQuery(querybuf) );
		} catch( FieldException fieldEx ) {
			return null;
		}
	}

	protected Map<String, Object> getRecordMap( QueryFactory factory, Table table, Map<String, ? extends Object> primaryMap, String[] fieldKeys )
						throws SQLException {
		try {
			QueryBuffer querybuf = table.setPrimaryConditionQuery( new ConditionQueryBuffer(primaryMap) );
			return SQLManager.getRecordMap( handler, null, factory.setDataQuery(querybuf, fieldKeys) );
		} catch( FieldException fieldEx ) {
			return null;
		}
	}

	public String[] getSortKeys() {
		if( sortKeyList != null && sortKeyList.size() > 0 ) {
			String[] sortKeys = new String[ sortKeyList.size() ];

			int k = 0;
			for( String sortKey : sortKeyList )
				sortKeys[k++] = sortKey.split("#", 2)[0];

			return sortKeys;
		}

		return null;
	}

	public void setSort( String... sortKeys ) {
		if( sortKeys != null && sortKeys.length > 0 ) {
			sortKeyList = new java.util.ArrayList<String>();
			for( int i = 0; i < sortKeys.length; i++ ) {
				String normalizeSortKey = Utility.normalizeSortKey( sortKeys[i] );
				if( normalizeSortKey != null )
					sortKeyList.add( normalizeSortKey );
			}
		} else
			sortKeyList = null;
	}

	protected int updateTable( Table table, Map<String, Object> recordMap, String[] primaryKeys, String collectionKey, int statementType )
						throws DataException, SQLException {
		return updateTable( table, recordMap, primaryKeys, collectionKey, statementType, false );
	}

	protected int updateTable( Table table, Map<String, Object> recordMap, String[] primaryKeys, String collectionKey, int statementType
						, boolean checkExistance ) throws DataException, SQLException {
		Collection<Map<String, Object>> records;
		try {
			records = (Collection<Map<String, Object>>)recordMap.get( collectionKey );
			if( records == null ) return 0;
		} catch( ClassCastException castEx ) {
			throw handler.createDataException( FieldException.ERR_INVALID_TYPE, recordMap );
		}

		if( primaryKeys != null ) {
			Object[] values = new Object[primaryKeys.length];
			for( int i = 0; i < primaryKeys.length; i++ )
				values[i] = recordMap.get( primaryKeys[i] );

			try {
				for( Map<String, Object> map : records ) {
					for( int i = 0; i < primaryKeys.length; i++ )
						map.put( primaryKeys[i], values[i] );
				}
			} catch( ClassCastException castEx ) {
				throw handler.createDataException( FieldException.ERR_INVALID_TYPE, recordMap );
			}
		}
		if( checkExistance ) {
			AbstractField[] fields = table.getFieldArray();
			for( int i = 0; i < fields.length; i++ ) {
				Table.Field tfield = (Table.Field)fields[i];
				if( tfield.primary() )
					fields[i] = null;
				else {
					String fieldKey = tfield.getFieldKey();
					if( "status".equals(fieldKey) || "updateUserId".equals(fieldKey) )
						fields[i] = null;
				}
			}

			records = new java.util.ArrayList<Map<String, Object>>( records );
			for( java.util.Iterator<Map<String, Object>> iterator = records.iterator(); iterator.hasNext(); ) {
				Map<String, Object> map = iterator.next();

				boolean exist = false;
				for( int i = 0; i < fields.length; i++ ) {
					Table.Field tfield = (Table.Field)fields[i];
					if( fields[i] != null ) {
						Object value = map.get( tfield.getFieldKey() );
						if( value != null && !"".equals(value) ) {
							exist = true;
							break;
						}
					}
				}
				if( !exist ) iterator.remove();
			}
		}

		DataResult result = SQLManager.manageRecordAll( handler, table, records, statementType );

		if( result.getErrorCount() > 0 )
			throw result.getException();
		else
			return result.getSuccessCount();
	}

	protected void write( DataWriter out, QueryFactory factory, Map<String, ? extends Object> conditionMap, String[] fieldKeys, int writingOption )
						throws IOException, SQLException {
		QueryableField[] fields = factory.getQueryableFieldArray( fieldKeys );

		if( (writingOption & QueryableManager.OPT_WRITING_TITLE) > 0 ) {
			char dataType = out.getDataType();
			com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

			out.setDataType( DataWriter.TITLE );
			if( (writingOption & QueryableManager.OPT_WRITING_ROWNUMBER) > 0 ) out.print( "No" );
			if( (writingOption & QueryableManager.OPT_WRITING_EXECUTETYPE) > 0 ) out.print( msghandler.getMessage("FIELD_EXECUTETYPE") );
			for( int f = 0; f < fields.length; f++ ) {
				if( fields[f] == null )
					out.printNull();
				else
					out.print( msghandler.getMessage(fields[f].getDescriptionKey()) );
			}
			out.setDataType( dataType );
			out.println();
		}

		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		appendOrderBy( querybuf, factory );

		if( (writingOption & QueryableManager.OPT_WRITING_VALUE_LITERALLY) > 0 )
			SQLManager.write( handler, out, fieldKeys, querybuf, writingOption );
		else
			SQLManager.write( handler, out, fields, querybuf, writingOption );
	}

	protected void write( DataWriter out, QueryFactory factory, Map<String, ? extends Object> conditionMap, ColumnList columnList, int writingOption )
						throws IOException, SQLException {
		if( (writingOption & QueryableManager.OPT_WRITING_TITLE) > 0 ) {
			char dataType = out.getDataType();
			com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

			out.setDataType( DataWriter.TITLE );
			switch( (writingOption & (QueryableManager.OPT_WRITING_ROWNUMBER | QueryableManager.OPT_WRITING_EXECUTETYPE)) ) {
			case (QueryableManager.OPT_WRITING_ROWNUMBER | QueryableManager.OPT_WRITING_EXECUTETYPE):
				ColumnUtility.writeTitle( out, columnList, msghandler, "No", msghandler.getMessage("FIELD_EXECUTETYPE") );
				break;
			case QueryableManager.OPT_WRITING_ROWNUMBER:
				ColumnUtility.writeTitle( out, columnList, msghandler, "No" );
				break;
			case QueryableManager.OPT_WRITING_EXECUTETYPE:
				ColumnUtility.writeTitle( out, columnList, msghandler, msghandler.getMessage("FIELD_EXECUTETYPE") );
				break;
			default:
				ColumnUtility.writeTitle( out, columnList, msghandler );
			}
			out.setDataType( dataType );
		}

		if( out instanceof com.irt.util.SSDataWriter )
			((com.irt.util.SSDataWriter)out).setColumnList( columnList );
		else if( out instanceof com.irt.util.XLFDataWriter )
			((com.irt.util.XLFDataWriter)out).setColumnList( columnList );

		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), columnList.getFieldKeys() );
		appendOrderBy( querybuf, factory );

		SQLManager.write( handler, out, columnList, querybuf, writingOption );
	}
}
