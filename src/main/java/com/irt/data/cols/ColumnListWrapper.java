/*
 *	File Name:	ColumnListWrapper.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getColumnListType(), getColumnListTypeList(), getSourceColumnList() 추가
 *	stghr12		2007/07/31		2.1.1	getProperty() 추가
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnListWrapper -> com.irt.data.cols.ColumnListWrapper
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data.cols;

import java.util.List;

/**
 *
 */
public class ColumnListWrapper implements ColumnList {
	ColumnList columnList;

	public ColumnListWrapper( ColumnList columnList ) {
		this.columnList = columnList;
	}

	public Column getColumn( int index ) {
		return columnList.getColumn( index );
	}

	public int getColumnCount() {
		return columnList.getColumnCount();
	}

	public String getColumnListType() {
		return columnList.getColumnListType();
	}

	public List<String[]> getColumnListTypeList() {
		return columnList.getColumnListTypeList();
	}

	public int getColumnSize() {
		return columnList.getColumnSize();
	}

	public Column[] getColumns() {
		return columnList.getColumns();
	}

	public String[] getFieldKeyArray() {
		return columnList.getFieldKeyArray();
	}

	public String[] getFieldKeys() {
		return columnList.getFieldKeys();
	}

	public String[] getFieldKeys( String... extraFieldKeys ) {
		return columnList.getFieldKeys( extraFieldKeys );
	}

	public String[] getHiddenFieldKeys() {
		return columnList.getHiddenFieldKeys();
	}

	public String getName() {
		return columnList.getName();
	}

	public String[] getPrimaryFieldKeys() {
		return columnList.getPrimaryFieldKeys();
	}

	public String getProperty( String key ) {
		return columnList.getProperty( key );
	}

	public String[] getSortKeys() {
		return columnList.getSortKeys();
	}

	public ColumnList getSourceColumnList() {
		return columnList;
	}
}
