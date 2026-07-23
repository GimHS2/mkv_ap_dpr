/*
 *	File Name:	SummaryQueryableManager.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/05/31		2.2.1	writeSummary(): writingTitle -> writingOption(writingTitle, writingValueLiterally 포함)
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	create
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
public interface SummaryQueryableManager {
	public void appendSort( String sortKey );

	public void clearSort();

	public Map<String, Object> getSummary( Map<String, ? extends Object> conditionMap ) throws SQLException;

	public Map<String, Object> getSummary( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException;

	public int getSummaryCount( Map<String, ? extends Object> conditionMap ) throws SQLException;

	public List<Map<String, Object>> getSummarys( Map<String, ? extends Object> conditionMap ) throws SQLException;

	public List<Map<String, Object>> getSummarys( Map<String, ? extends Object> conditionMap, int skipRows, int maxRows ) throws SQLException;

	public List<Map<String, Object>> getSummarys( Map<String, ? extends Object> conditionMap, String[] fieldKeys ) throws SQLException;

	public List<Map<String, Object>> getSummarys( Map<String, ? extends Object> conditionMap, String[] fieldKeys, int skipRows, int maxRows )
						throws SQLException;

	public void setSort( String... sortKeys );

	public void writeSummary( DataWriter out, Map<String, ? extends Object> conditionMap, String[] fieldKeys, int writingOption )
						throws IOException, SQLException;

	public void writeSummary( DataWriter out, Map<String, ? extends Object> conditionMap, ColumnList columnList, int writingOption )
						throws IOException, SQLException;
}
