/*
 *	File Name:	Column.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2014/03/31		2.2.2	getDataCellAttr() 추가
 *	stghr12		2008/03/31		2.2.1	getColumnTitle(): return String -> return Object
 *										getColumnTitle( recordMap, msghandler ) 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.1	getColumnValue()의 return값을 String에서 Object로 변경.
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.Column -> com.irt.data.cols.Column
 *										getColumnAttr()의 return값을 String에서 Object로 변경.
 *										getColumnSize() 추가
 *	stghr12		2006/02/28		2.0.0	version up(기존의 Column은 ColumnImpl로 바꾸고, interace로 변경)
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.data.cols;

import com.irt.util.MessageHandler;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public interface Column extends com.irt.data.format.RecordFormat {
	public void addFieldKeyToSet( Set<String> fieldKeySet );

	/**
	 * {@link #getColumnValue(Map, MessageHandler) getColumnValue}와 동일.
	 */
	public String format( Map recordMap, MessageHandler msghandler );

	public Object getColumnAttr();

	public ColumnGroup getColumnGroup();

	public String getColumnHelp( Map recordMap, MessageHandler msghandler );

	/**
	 * Column의 크기 return.
	 * html에서 colspan에 사용.
	 */
	public int getColumnSize();

	public Object getColumnTitle();

	public String getColumnTitle( Map recordMap, MessageHandler msghandler );

	public Object getColumnValue( Map recordMap, MessageHandler msghandler );

	public String getDataCellAttr();

	public String getFieldKey();

	public String getKey();

	public boolean sortable();
}
