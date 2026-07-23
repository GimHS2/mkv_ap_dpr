/*
 *	File Name:	ManipulableManager.java
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
 *	stghr12		2006/12/01		2.1.0	deleteAll(), modifyAll(), registAll() return type변경: Collection -> DataResult
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public interface ManipulableManager {
	public DataLoader.Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, int statementType )
						throws SQLException, UnsupportedOperationException;

	public DataLoader.Loader createDataLoader( String[] fieldKeys, Map<String, ? extends Object> defaultMap, String[] updateFieldKeys
						, int statementType ) throws SQLException, UnsupportedOperationException;

	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException, UnsupportedOperationException;

	public DataResult deleteAll( Collection<Map<String, Object>> records ) throws SQLException, UnsupportedOperationException;

	public boolean existRecord( Map<String, ? extends Object> primaryMap ) throws SQLException;

	public AbstractFieldSet getFieldSet( boolean inserting ) throws UnsupportedOperationException;

	public Object getFieldValue( Map<String, ? extends Object> primaryMap, String fieldKey ) throws IllegalArgumentException, SQLException;

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap ) throws SQLException;

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, String[] fieldKeys ) throws SQLException;

	public Map<String, Object> getRecordWithLock( Map<String, ? extends Object> primaryMap, String[] fieldKeys ) throws DataException, SQLException;

	public Map<String, Object> getRecordWithLock( Map<String, ? extends Object> primaryMap, String[] fieldKeys, boolean waiting )
						throws DataException, SQLException;

	public boolean modify( Map<String, Object> recordMap ) throws DataException, SQLException, UnsupportedOperationException;

	public boolean modify( Map<String, Object> recordMap, String[] fieldKeys ) throws DataException, SQLException, UnsupportedOperationException;

	public DataResult modifyAll( Collection<Map<String, Object>> records ) throws SQLException, UnsupportedOperationException;

	public DataResult modifyAll( Collection<Map<String, Object>> records, String[] fieldKeys ) throws SQLException, UnsupportedOperationException;

	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException, UnsupportedOperationException;

	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException, UnsupportedOperationException;
}
