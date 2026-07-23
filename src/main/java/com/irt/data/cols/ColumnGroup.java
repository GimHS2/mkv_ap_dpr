/*
 *	File Name:	ColumnGroup.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.0	getGroupTitle(): return String -> return Object
 *										getGroupTitle( recordMap, msghandler ) 추가
 *	stghr12		2007/10/31		2.1.1	getColumnKeys() 추가
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnGroup -> com.irt.data.cols.ColumnGroup
 *										getColumnAttr()의 return값을 String에서 Object로 변경.
 *	stghr12		2006/02/28		2.0.0	version up(기존의 ColumnGroup은 ColumGroupImpl로 바꾸고, interface로 변경)
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.data.cols;

import com.irt.util.MessageHandler;
import java.util.Map;

/**
 *
 */
public interface ColumnGroup {
	public boolean contains( String columnKey );

	public boolean contains( Column column );

	public Column createGroupColumn( Column column );

	public String[] getColumnKeys();

	public Object getGroupAttr();

	public Object getGroupTitle();

	public String getGroupTitle( Map recordMap, MessageHandler msghandler );

	public String getKey();
}
