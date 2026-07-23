/*
 *	File Name:	QueryableManagerImpl.java
 *	Version:	2.2.1c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/12/30		2.2.1c	org.slf4j.MDC("condMap") 적용.
 *	stghr12		2008/05/31		2.2.1	write(), writeSummary(): writingTitle -> writingOption(writingTitle, writingValueLiterally 포함)
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										SQLManager 변경사항 적용: fieldMap 삭제
 *										getFieldValue(), getRecord(): setPrimaryConditionQuery() 호출 순서 변경
 *	stghr12		2007/04/30		2.1.1	getRecords() 추가
 *										DataWriter.setDataType() 활용
 *										write(), writeSummary(); RBMDataManager로 이동
 *	stghr12		2006/12/01		2.1.0	QueryableManager 변경사항 적용
 *										RBMDataManager.appendOrderBy 변경사항 반영
 *										생성자 QueryableManagerImpl( handler, factory, factory_summary ) 추가
 *										getSummary(), getSummaryCount(), getSummarys(), write(), writeSummary() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.rbm;

import com.irt.sql.*;
import com.irt.data.DataWriter;
import com.irt.data.FieldException;
import com.irt.data.cols.ColumnList;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class QueryableManagerImpl extends RBMDataManager implements com.irt.data.QueryableManager {
	QueryFactory factory;
	QueryFactory factory_summary;

	protected QueryableManagerImpl( SQLHandler handler, QueryFactory factory ) {
		super( handler );
		this.factory = factory;
		this.factory_summary = null;
	}

	protected QueryableManagerImpl( SQLHandler handler, QueryFactory factory, QueryFactory factory_summary ) {
		super( handler );
		this.factory = factory;
		this.factory_summary = factory_summary;
	}

	public boolean existRecord( Map<String, ? extends Object> primaryMap ) throws SQLException {
		QueryBuffer querybuf = setPrimaryConditionQuery( new ConditionQueryBuffer(primaryMap) );
		if( querybuf == null ) return false;

		return ( SQLManager.getObjectValue(handler, querybuf.append("'x'")) != null );
	}

	public Object getFieldValue( Map<String, ? extends Object> primaryMap, String fieldKey ) throws IllegalArgumentException, SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( primaryMap );

		if( !factory.appendData(querybuf, fieldKey) )
			throw new IllegalArgumentException( "invalid fieldKey '"+ fieldKey +"'" );
		if( setPrimaryConditionQuery(querybuf) == null ) return null;

		return SQLManager.getObjectValue( handler, querybuf );
	}

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap ) throws SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( primaryMap );

		factory.setDataQuery( querybuf );
		if( setPrimaryConditionQuery(querybuf) == null ) return null;

		return SQLManager.getRecordMap( handler, null, querybuf );
	}

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, String[] fieldKeys ) throws SQLException {
		ConditionQueryBuffer querybuf = new ConditionQueryBuffer( primaryMap );

		factory.setDataQuery( querybuf, fieldKeys );
		if( setPrimaryConditionQuery(querybuf) == null ) return null;

		return SQLManager.getRecordMap( handler, null, querybuf );
	}

	public int getRecordCount( Map<String, ? extends Object> conditionMap ) throws SQLException {
		QueryBuffer querybuf = factory.setCountQuery( new ConditionQueryBuffer(conditionMap) );
		return SQLManager.getInt( handler, querybuf );
	}

	public List<Map<String, Object>> getRecords() throws SQLException {
		return getRecords( (Map<String, Object>)null, 0, -1 );
	}

	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap ) throws SQLException {
		return getRecords( conditionMap, 0, -1 );
	}

	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap, int skipRows, int maxRows ) throws SQLException {
		QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap) );
		appendOrderBy( querybuf, factory );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException {
		return getRecords( conditionMap, fieldKeys, 0, -1 );
	}

	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap, String[] fieldKeys, int skipRows, int maxRows )
						throws SQLException {
		org.slf4j.MDC.put("condMap", (conditionMap == null ? "{}" : conditionMap.toString()));
		try {
			QueryBuffer querybuf = factory.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
			appendOrderBy( querybuf, factory );

			return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
		} finally {
			org.slf4j.MDC.remove("condMap");
		}
	}

	public Map<String, Object> getSummary( Map<String, ? extends Object> conditionMap ) throws SQLException, UnsupportedOperationException {
		if( factory_summary == null ) throw new UnsupportedOperationException();

		QueryBuffer querybuf = factory_summary.setQuery( new ConditionQueryBuffer(conditionMap) );
		if( querybuf == null ) return null;

		return SQLManager.getRecordMap( handler, null, factory_summary.setDataQuery(querybuf) );
	}

	public Map<String, Object> getSummary( Map<String, ? extends Object> conditionMap, String[] fieldKeys )
						throws SQLException, UnsupportedOperationException {
		if( factory_summary == null ) throw new UnsupportedOperationException();

		QueryBuffer querybuf = factory_summary.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		if( querybuf == null ) return null;

		return SQLManager.getRecordMap( handler, null, factory_summary.setDataQuery(querybuf, fieldKeys) );
	}

	public int getSummaryCount( Map<String, ? extends Object> conditionMap ) throws SQLException, UnsupportedOperationException {
		if( factory_summary == null ) throw new UnsupportedOperationException();

		QueryBuffer querybuf = factory_summary.setCountQuery( new ConditionQueryBuffer(conditionMap) );
		return SQLManager.getInt( handler, querybuf );
	}

	public List<Map<String, Object>> getSummarys( Map<String, ? extends Object> conditionMap ) throws SQLException, UnsupportedOperationException {
		if( factory_summary == null ) throw new UnsupportedOperationException();

		return getSummarys( conditionMap, 0, -1 );
	}

	public List<Map<String, Object>> getSummarys( Map<String, ? extends Object> conditionMap, int skipRows, int maxRows )
						throws SQLException, UnsupportedOperationException {
		if( factory_summary == null ) throw new UnsupportedOperationException();

		QueryBuffer querybuf = factory_summary.setQuery( new ConditionQueryBuffer(conditionMap) );
		appendOrderBy( querybuf, factory_summary );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	public List<Map<String, Object>> getSummarys( Map<String, ? extends Object> conditionMap, String[] fieldKeys )
						throws SQLException, UnsupportedOperationException {
		if( factory_summary == null ) throw new UnsupportedOperationException();

		return getSummarys( conditionMap, fieldKeys, 0, -1 );
	}

	public List<Map<String, Object>> getSummarys( Map<String, ? extends Object> conditionMap, String[] fieldKeys, int skipRows, int maxRows )
						throws SQLException, UnsupportedOperationException {
		if( factory_summary == null ) throw new UnsupportedOperationException();

		QueryBuffer querybuf = factory_summary.setQuery( new ConditionQueryBuffer(conditionMap), fieldKeys );
		appendOrderBy( querybuf, factory_summary );

		return SQLManager.getRecordList( handler, querybuf, skipRows, maxRows );
	}

	protected QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) {
		return factory.setConditionQuery( querybuf );
	}

	protected static QueryBuffer setPrimaryConditionQuery( Table table, ConditionQueryBuffer querybuf ) {
		try {
			return table.setPrimaryConditionQuery( querybuf );
		} catch( FieldException fieldEx ) {
			return null;
		}
	}

	public void write( DataWriter out, Map<String, ? extends Object> conditionMap, String[] fieldKeys, int writingOption )
						throws IOException, SQLException {
		write( out, factory, conditionMap, fieldKeys, writingOption );
	}

	public void write( DataWriter out, Map<String, ? extends Object> conditionMap, ColumnList columnList, int writingOption )
						throws IOException, SQLException {
		write( out, factory, conditionMap, columnList, writingOption );
	}

	public void writeSummary( DataWriter out, Map<String, ? extends Object> conditionMap, String[] fieldKeys, int writingOption )
						throws IOException, SQLException {
		if( factory_summary == null ) throw new UnsupportedOperationException();
		write( out, factory_summary, conditionMap, fieldKeys, writingOption );
	}

	public void writeSummary( DataWriter out, Map<String, ? extends Object> conditionMap, ColumnList columnList, int writingOption )
						throws IOException, SQLException {
		if( factory_summary == null ) throw new UnsupportedOperationException();
		write( out, factory_summary, conditionMap, columnList, writingOption );
	}
}
