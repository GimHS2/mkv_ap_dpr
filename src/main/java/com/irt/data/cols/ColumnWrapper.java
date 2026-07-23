/*
 *	File Name:	ColumnWrapper.java
 *	Version:	2.2.2
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2014/03/31		2.2.2	getDataCellAttr() 추가
 *	stghr12		2008/03/31		2.2.1	RecordFormat 변경사항 적용: format( recordMap, msghandler, stringBuffer ) 추가
 *										Column 변경사항 적용: getColumnTitle() 변경/추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.1	com.irt.data.cols.Column 변경사항 적용.
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnWrapper -> com.irt.data.cols.ColumnWrapper
 *										getSourceColumn() 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.data.cols;

import com.irt.util.MessageHandler;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class ColumnWrapper implements Column {
	Column column;

	public ColumnWrapper( Column column ) {
		this.column = column;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		column.addFieldKeyToSet( fieldKeySet );
	}

	public String format( Map recordMap, MessageHandler msghandler ) {
		return column.format( recordMap, msghandler );
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		return column.format( recordMap, msghandler, stringBuffer );
	}

	public Object getColumnAttr() {
		return column.getColumnAttr();
	}

	public ColumnGroup getColumnGroup() {
		return column.getColumnGroup();
	}

	public String getColumnHelp( Map recordMap, MessageHandler msghandler ) {
		return column.getColumnHelp( recordMap, msghandler );
	}

	public int getColumnSize() {
		return column.getColumnSize();
	}

	public Object getColumnTitle() {
		return column.getColumnTitle();
	}

	public String getColumnTitle( Map recordMap, MessageHandler msghandler ) {
		return column.getColumnTitle( recordMap, msghandler );
	}

	public Object getColumnValue( Map recordMap, MessageHandler msghandler ) {
		return column.getColumnValue( recordMap, msghandler );
	}

	public String getDataCellAttr() {
		return column.getDataCellAttr();
	}

	public String getKey() {
		return column.getKey();
	}

	public String getFieldKey() {
		return column.getFieldKey();
	}

	public Column getSourceColumn() {
		return column;
	}

	public boolean sortable() {
		return column.sortable();
	}
}
