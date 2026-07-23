/*
 *	File Name:	QueryableManager.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/05/31		2.2.1	write(): writingTitle -> writingOption(writingTitle, writingValueLiterally 포함)
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.1	getRecords() 추가
 *	stghr12		2006/12/01		2.1.0	write() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data;

import com.irt.data.cols.ColumnList;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface QueryableManager {
	public final static int OPT_NONE					= 0x0000;
	public final static int OPT_WRITING_TITLE			= 0x0001;
	public final static int OPT_WRITING_ROWNUMBER		= 0x0002;
	public final static int OPT_WRITING_EXECUTETYPE		= 0x0004;
	public final static int OPT_WRITING_VALUE_LITERALLY	= 0x0008;

	public void appendSort( String sortKey );

	public void clearSort();

	public boolean existRecord( Map<String, ? extends Object> primaryMap ) throws SQLException;

	public Object getFieldValue( Map<String, ? extends Object> primaryMap, String fieldKey ) throws IllegalArgumentException, SQLException;

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap ) throws SQLException;

	public Map<String, Object> getRecord( Map<String, ? extends Object> primaryMap, String[] fieldKeys ) throws SQLException;

	public int getRecordCount( Map<String, ? extends Object> conditionMap ) throws SQLException;

	public List<Map<String, Object>> getRecords() throws SQLException;

	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap ) throws SQLException;

	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap, int skipRows, int maxRows ) throws SQLException;

	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException;

	public List<Map<String, Object>> getRecords( Map<String, ? extends Object> conditionMap, String[] fieldKeys, int skipRows, int maxRows )
						throws SQLException;

	public void setSort( String... sortKeys );

	public void write( DataWriter out, Map<String, ? extends Object> conditionMap, String[] fieldKeys, int writingOption )
						throws IOException, SQLException;

	public void write( DataWriter out, Map<String, ? extends Object> conditionMap, ColumnList columnList, int writingOption )
						throws IOException, SQLException;
}
