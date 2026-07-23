/*
 *	File Name:	ManipulableManagerImpl.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	createDataLoader() 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.1	getRecordWithLock() 추가
 *										deleteEach(), modifyEach(), registEach(): public -> protected
 *	stghr12		2006/12/01		2.1.0	ManipulableManager 변경사항 적용
 *										생성자 ManipulableManagerImpl( handler, table, factory, factory_summary ) 추가
 *										deleteEach(), modifyEach(), registEach() 추가
 *										existData() 삭제
 *										Table.STATEMENT_* -> Record.*
 *	stghr12		2006/02/28		2.0.0	create
 *
 **/

package com.irt.rbm;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class ManipulableManagerImpl extends QueryableManagerImpl implements com.irt.data.ManipulableManager {
	Table table;

	protected ManipulableManagerImpl( SQLHandler handler, Table table ) {
		super( handler, new QueryFactory(table) );
		this.table = table;
	}

	protected ManipulableManagerImpl( SQLHandler handler, Table table, QueryFactory factory ) {
		super( handler, factory );
		this.table = table;
	}

	protected ManipulableManagerImpl( SQLHandler handler, Table table, QueryFactory factory, QueryFactory factory_summary ) {
		super( handler, factory, factory_summary );
		this.table = table;
	}

	@Override
	public DataLoader.Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, int statementType )
			throws SQLException, UnsupportedOperationException {
		return new TableDataLoader( fieldKeys, defaultMap, handler, table, statementType );
	}

	@Override
	public DataLoader.Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
			, int statementType ) throws SQLException, UnsupportedOperationException {
		return new TableDataLoader( fieldKeys, defaultMap, handler, table, updateFieldKeys, statementType );
	}

	@Override
	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		return SQLManager.manageRecord( handler, table, primaryMap, Record.DELETE );
	}

	@Override
	public DataResult deleteAll( Collection<Map<String, Object>> records ) throws SQLException {
		return SQLManager.manageRecordAll( handler, table, records, Record.DELETE );
	}

	protected DataResult deleteEach( Collection<Map<String, Object>> records ) throws SQLException {
		DataResult result = new DataResult();

		int idx = 0;
		for( java.util.Iterator<Map<String, Object>> iterator = records.iterator(); iterator.hasNext(); idx++ ) {
			Map<String, Object> recordMap = iterator.next();

			try {
				if( !delete(recordMap) )
					throw handler.createDataException( DataException.ERR_NO_RECORD_DELETE, recordMap );

				result.increaseDeleteCount();
			} catch( DataException dataEx ) {
				result.appendError( new DataException(idx, dataEx) );
			}
		}

		return result;
	}

	@Override
	public AbstractFieldSet getFieldSet( boolean inserting ) {
		return table.getValidableFieldSet( inserting );
	}

	@Override
	public Map<String, Object> getRecordWithLock( Map<String, ? extends Object> primaryMap, String[] fieldKeys ) throws DataException, SQLException {
		return getRecordWithLock( primaryMap, fieldKeys, true );
	}

	@Override
	public Map<String, Object> getRecordWithLock( Map<String, ? extends Object> primaryMap, String[] fieldKeys, boolean waiting )
			throws DataException, SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( primaryMap );

		(new QueryFactory(table)).setDataQuery( querybuf.appendLock(waiting), fieldKeys );
		if( setPrimaryConditionQuery(querybuf) == null ) return null;

		try {
			return SQLManager.getRecordMap( handler, null, querybuf );
		} catch( SQLException sqlEx ) {
			throw handler.createDataException( sqlEx, primaryMap );
		}
	}

	@Override
	public boolean modify( Map<String, Object> recordMap ) throws DataException, SQLException {
		return SQLManager.manageRecord( handler, table, recordMap, Record.UPDATE );
	}

	@Override
	public boolean modify( Map<String, Object> recordMap, String[] fieldKeys ) throws DataException, SQLException {
		return SQLManager.updateRecord( handler, table, recordMap, fieldKeys );
	}

	@Override
	public DataResult modifyAll( Collection<Map<String, Object>> records ) throws SQLException {
		return SQLManager.manageRecordAll( handler, table, records, Record.UPDATE );
	}

	@Override
	public DataResult modifyAll( Collection<Map<String, Object>> records, String[] fieldKeys ) throws SQLException {
		return SQLManager.updateRecordAll( handler, table, records, fieldKeys );
	}

	protected DataResult modifyEach( Collection<Map<String, Object>> records ) throws SQLException {
		DataResult result = new DataResult();

		int idx = 0;
		for( java.util.Iterator<Map<String, Object>> iterator = records.iterator(); iterator.hasNext(); idx++ ) {
			Map<String, Object> recordMap = iterator.next();

			try {
				if( !modify(recordMap) )
					throw handler.createDataException( DataException.ERR_NO_RECORD_UPDATE, recordMap );

				result.increaseModifyCount();
			} catch( DataException dataEx ) {
				result.appendError( new DataException(idx, dataEx) );
			}
		}

		return result;
	}

	protected DataResult modifyEach( Collection<Map<String, Object>> records, String[] fieldKeys ) throws SQLException {
		DataResult result = new DataResult();

		int idx = 0;
		for( java.util.Iterator<Map<String, Object>> iterator = records.iterator(); iterator.hasNext(); idx++ ) {
			Map<String, Object> recordMap = iterator.next();

			try {
				if( !modify(recordMap, fieldKeys) )
					throw handler.createDataException( DataException.ERR_NO_RECORD_UPDATE, recordMap );

				result.increaseModifyCount();
			} catch( DataException dataEx ) {
				result.appendError( new DataException(idx, dataEx) );
			}
		}

		return result;
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		return SQLManager.manageRecord( handler, table, recordMap, Record.INSERT );
	}

	@Override
	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException {
		return SQLManager.manageRecordAll( handler, table, records, Record.INSERT );
	}

	protected DataResult registEach( Collection<Map<String, Object>> records ) throws SQLException {
		DataResult result = new DataResult();

		int idx = 0;
		for( java.util.Iterator<Map<String, Object>> iterator = records.iterator(); iterator.hasNext(); idx++ ) {
			Map<String, Object> recordMap = iterator.next();

			try {
				if( regist(recordMap) )
					result.increaseRegistCount();
			} catch( DataException dataEx ) {
				result.appendError( new DataException(idx, dataEx) );
			}
		}

		return result;
	}

	@Override
	protected QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) {
		try {
			return table.setPrimaryConditionQuery( querybuf );
		} catch( FieldException fieldEx ) {
			return null;
		}
	}

	public void validate( Map<String, ? extends Object> recordMap, boolean inserting ) throws DataException {
		try {
			table.validate( recordMap, inserting );
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		}
	}
}
