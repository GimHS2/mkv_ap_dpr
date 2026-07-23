/*
 *	File Name:	ColumnListTemplate.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.ColumnList;
import com.irt.data.cols.ColumnPool;
import com.irt.data.cols.ColumnResourceBundle;
import java.util.List;

/**
 *
 */
class ColumnListTemplate {
	ColumnList columnList;

	String columnPoolName;
	String columnListName;
	String[] columnKeys;
	String[] primaryFieldKeys, hiddenFieldKeys, sortKeys, propertyValues;

	String defaultColumnListTypeKey;
	List<String[]> columnListTypeList;

	ColumnListTemplate( String columnPoolName, String columnListName, String[] columnKeys
						, String[] primaryFieldKeys, String[] hiddenFieldKeys, String[] sortKeys, String[] propertyValues
						, String defaultColumnListTypeKey, List<String[]> columnListTypeList ) {
		this.columnList = null;

		this.columnPoolName = columnPoolName;
		this.columnListName = columnListName;
		this.columnKeys = columnKeys;
		this.primaryFieldKeys = primaryFieldKeys;
		this.hiddenFieldKeys = hiddenFieldKeys;
		this.sortKeys = sortKeys;
		this.propertyValues = propertyValues;
		this.defaultColumnListTypeKey = defaultColumnListTypeKey;
		this.columnListTypeList = columnListTypeList;
	}

	public String getName() {
		return columnListName;
	}

	public ColumnList toColumnList( ColumnResourceBundle columnResourceBundle ) {
		if( columnList != null ) return columnList;

		ColumnPool columnPool = columnResourceBundle.getColumnPool( columnPoolName );
		if( columnPool == null ) return null;

		columnList = columnPool.createColumnList(
			columnListName, defaultColumnListTypeKey, columnKeys, primaryFieldKeys, hiddenFieldKeys, sortKeys
		);
		if( columnList instanceof ColumnListImpl ) {
			if( propertyValues != null ) ((ColumnListImpl)columnList).propertyValues = propertyValues;
			if( defaultColumnListTypeKey != null ) ((ColumnListImpl)columnList).setColumnListType( defaultColumnListTypeKey, columnListTypeList );
		}

		return columnList;
	}
}
