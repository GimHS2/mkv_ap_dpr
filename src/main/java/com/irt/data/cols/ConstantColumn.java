/*
 *	File Name:	ConstantColumn.java
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
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ConstantColumn -> com.irt.data.cols.ConstantColumn
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
public class ConstantColumn implements Column {
	String string;
	int columnSize;

	public ConstantColumn( String string, int columnSize ) {
		this.string = string;
		this.columnSize = columnSize;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {}

	public String format( Map recordMap, MessageHandler msghandler ) {
		return string;
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		return stringBuffer.append( string );
	}

	public Object getColumnAttr() {
		return null;
	}

	public ColumnGroup getColumnGroup() {
		return null;
	}

	public String getColumnHelp( Map recordMap, MessageHandler msghandler ) {
		return null;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public Object getColumnTitle() {
		return "";
	}

	public String getColumnTitle( Map recordMap, MessageHandler msghandler ) {
		return "";
	}

	public Object getColumnValue( Map recordMap, MessageHandler msghandler ) {
		return string;
	}

	public String getDataCellAttr() {
		return null;
	}

	public String getFieldKey() {
		return "("+ string +")";
	}

	public String getKey() {
		return "("+ string +")";
	}

	public boolean sortable() {
		return false;
	}
}
