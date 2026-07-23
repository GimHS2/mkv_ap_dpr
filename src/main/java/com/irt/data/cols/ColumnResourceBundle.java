/*
 *	File Name:	ColumnResourceBundle.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getColumnList(), handleGetColumnList()에 columnListType 추가
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnPoolSet -> com.irt.data.cols.ColumnResourceBundle
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.data.cols;

/**
 *
 */
public interface ColumnResourceBundle {
	/**
	 * columnListName에 해당하는 ColumnList를 return(없을 경우 상위 columnListName 검색).
	 */
	public ColumnList getColumnList( String columnListName );

	/**
	 * columnListName에 해당하는 ColumnList를 return(없을 경우 상위 columnListName 검색).
	 */
	public ColumnList getColumnList( String columnListName, String columnListType, String... optionKeys );

	/**
	 * columnPoolName에 해당하는 ColumnPool를 return(없을 경우 상위 columnPoolName 검색).
	 */
	public ColumnPool getColumnPool( String columnPoolName );

	/**
	 * fieldKeyArrayName에 해당하는 fieldKeys를 return(없을 경우 상위 fieldKeys 검색).
	 */
	public String[] getFieldKeyArray( String fieldKeyArrayName );

	/**
	 * fieldKeyArrayName에 해당하는 fieldKeys를 return(없을 경우 상위 fieldKeys 검색).
	 */
	public String[] getFieldKeyArray( String fieldKeyArrayName, String... optionKeys );

	public ColumnResourceBundle getParent();

	/**
	 * columnListName에 해당하는 ColumnList를 return(없을 경우 null return).
	 */
	public ColumnList handleGetColumnList( String columnListName );

	/**
	 * columnListName에 해당하는 ColumnList를 return(없을 경우 null return).
	 */
	public ColumnList handleGetColumnList( String columnListName, String columnListType, String... optionKeys );

	/**
	 * columnPoolName에 해당하는 ColumnPool를 return(없을 경우 null return).
	 */
	public ColumnPool handleGetColumnPool( String columnPoolName );

	/**
	 * fieldKeyArrayName에 해당하는 fieldKeys를 return(없을 경우 null return).
	 */
	public String[] handleGetFieldKeyArray( String fieldKeyArrayName );

	/**
	 * fieldKeyArrayName에 해당하는 fieldKeys를 return(없을 경우 null return).
	 */
	public String[] handleGetFieldKeyArray( String fieldKeyArrayName, String... optionKeys );
}
