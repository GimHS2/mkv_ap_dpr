/*
 *	File Name:	ColumnList.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getColumnListType(), getColumnListTypeList() 추가
 *	stghr12		2007/07/31		2.1.1	getProperty() 추가
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnList -> com.irt.data.cols.ColumnList
 *										getFieldKeyArray(), getFieldKeys(extraFieldKey), getFieldKeys(extraFieldKeys) 추가
 *	stghr12		2006/02/28		2.0.0	version up(기존의 LinkedColumn은 LinkedColumnImpl로 바꾸고, interface로 변경)
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.data.cols;

import java.util.List;

/**
 *
 */
public interface ColumnList {
	public Column getColumn( int index );

	/**
	 * Column의 개수를 return.
	 */
	public int getColumnCount();

	public String getColumnListType();

	/**
	 * ColumnListType code, name으로 이루어진 Map의 List를 return.
	 */
	public List<String[]> getColumnListTypeList();

	/**
	 * Column의 size의 합계를 return.
	 */
	public int getColumnSize();

	public Column[] getColumns();

	/**
	 * Column순서대로 Column의 getFieldKey()를 Array로 return.
	 */
	public String[] getFieldKeyArray();

	public String[] getFieldKeys();

	public String[] getFieldKeys( String... extraFieldKeys );

	public String[] getHiddenFieldKeys();

	public String getName();

	public String[] getPrimaryFieldKeys();

	public String getProperty( String key );

	public String[] getSortKeys();
}
