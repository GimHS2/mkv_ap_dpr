/*
 *	File Name:	ColumnPool.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										createColumnList()에 columnListType 추가
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnPool -> com.irt.data.cols.ColumnPool: interface로 변경.
 *	stghr12		2006/08/25		2.0.1	getColumnGroupMap() 오류 수정
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.data.cols;

import java.util.Map;

/**
 *
 */
public interface ColumnPool {
	public ColumnList createColumnList( String columnListName, String[] columnKeys ) throws IllegalArgumentException;

	public ColumnList createColumnList( String columnListName, String[] columnKeys, String[] primaryFieldKeys ) throws IllegalArgumentException;

	public ColumnList createColumnList( String columnListName, String[] columnKeys, String[] primaryFieldKeys, String[] hiddenFieldKeys )
						throws IllegalArgumentException;

	public ColumnList createColumnList( String columnListName, String[] columnKeys, String[] primaryFieldKeys, String[] hiddenFieldKeys
						, String[] sortKeys ) throws IllegalArgumentException;

	public ColumnList createColumnList( String columnListName, String columnListType, String[] columnKeys, String[] primaryFieldKeys
						, String[] hiddenFieldKeys, String[] sortKeys ) throws IllegalArgumentException;

	/**
	 * columnKey에 해당하는 Column을 return(없을 경우 상위 columnKey 검색).
	 */
	public Column getColumn( String columnKey );

	public Map<String, Column> getColumnFamily( String fieldKey );

	public ColumnGroup getColumnGroup( String columnGroupKey );

	public Column[] getColumns( String... columnKeys ) throws IllegalArgumentException;

	public String getName();

	public ColumnPool getParent();

	/**
	 * columnKey에 해당하는 Column을 return(없을 경우 null return).
	 */
	public Column handleGetColumn( String columnKey );

	/**
	 * columns에 ColumnGroup을 설정.
	 * @return 설정이 된 Column이 있으면 true, 하나도 변화가 없으면 false.
	 */
	public boolean setColumnGroupToColumnArray( Column... columns );
}
