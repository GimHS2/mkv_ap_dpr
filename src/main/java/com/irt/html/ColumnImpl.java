/*
 *	File Name:	ColumnImpl.java
 *	Version:	2.2.3
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2014/03/31		2.2.3	dataCellAttr, getDataCellAttr() 추가
 *	stghr12		2008/08/29		2.2.2	format(), getColumnValue(): recordMap이 null일 경우 "" return
 *	stghr12		2008/03/31		2.2.1	RecordFormat 변경사항 적용: format( recordMap, msghandler, stringBuffer ) 추가
 *										Column 변경사항 적용: getColumnTitle() 변경/추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										eraseHtmlConversion(): (<BR> -> "")를 (<BR> -> " ")으로 변경, trim() 처리
 *	stghr12		2007/04/30		2.1.1	com.irt.data.cols.Column 변경사항 적용.
 *										convertToHtml, eraseHtmlConversion() 추가.
 *										columnAttr: String -> Object
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnImpl -> com.irt.html.ColumnImpl
 *	stghr12		2006/02/28		2.0.0	version up(기존의 Column은 ColumnImpl로 바꾸고, interace로 변경)
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.ColumnGroup;
import com.irt.data.format.RecordFormat;
import com.irt.html.HtmlUtility;
import com.irt.util.MessageHandler;
import java.util.Map;
import java.util.Set;

/**
 *
 */
class ColumnImpl implements com.irt.data.cols.Column {
	String columnKey, fieldKey, dataCellAttr = null;
	Object columnName;
	Object columnAttr;
	RecordFormat columnValue, columnHelp;
	ColumnGroup columnGroup;
	int columnSize;
	boolean sortable;
	boolean convertToHtml = true;

	ColumnImpl( String columnKey ) {
		this( columnKey, null, null, null, null, false );
	}

	ColumnImpl( String columnKey, Object columnName ) {
		this( columnKey, columnName, null, null, null, false );
	}

	ColumnImpl( String columnKey, Object columnName, Object columnAttr ) {
		this( columnKey, columnName, columnAttr, null, null, false );
	}

	ColumnImpl( String columnKey, Object columnName, Object columnAttr, RecordFormat columnValue ) {
		this( columnKey, columnName, columnAttr, columnValue, null, false );
	}

	ColumnImpl( String columnKey, Object columnName, Object columnAttr, RecordFormat columnValue, RecordFormat columnHelp ) {
		this( columnKey, columnName, columnAttr, columnValue, columnHelp, false );
	}

	ColumnImpl( String columnKey, Object columnName, Object columnAttr, RecordFormat columnValue, RecordFormat columnHelp, boolean sortable ) {
		int idx = columnKey.indexOf( '.' );
		if( idx < 0 )
			this.columnKey = this.fieldKey = columnKey;
		else {
			this.columnKey = columnKey;
			this.fieldKey = columnKey.substring( 0, idx );
		}
		this.columnName = columnName;
		this.columnAttr = columnAttr;
		this.columnValue = columnValue;
		this.columnHelp = columnHelp;
		this.columnSize = 1;
		this.sortable = sortable;
	}

	ColumnImpl( String columnKey, Object columnName, Object columnAttr, RecordFormat columnValue, RecordFormat columnHelp, boolean sortable
						, boolean convertToHtml ) {
		this( columnKey, columnName, columnAttr, columnValue, columnHelp, sortable );
		this.convertToHtml = convertToHtml;
	}

	ColumnImpl( ColumnImpl columnImpl ) {
		this( columnImpl.columnKey, columnImpl );
	}

	ColumnImpl( String columnKey, ColumnImpl columnImpl ) {
		this.columnKey = columnKey;
		this.columnName = columnImpl.columnName;
		this.columnAttr = columnImpl.columnAttr;
		this.fieldKey = columnImpl.fieldKey;
		this.columnValue = columnImpl.columnValue;
		this.columnHelp = columnImpl.columnHelp;
		this.columnGroup = columnImpl.columnGroup;
		this.columnSize = columnImpl.columnSize;
		this.sortable = columnImpl.sortable;
		this.convertToHtml = columnImpl.convertToHtml;
		this.dataCellAttr = columnImpl.dataCellAttr;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		if( fieldKey != null ) fieldKeySet.add( fieldKey );
		if( columnValue != null ) columnValue.addFieldKeyToSet( fieldKeySet );
		if( columnHelp != null ) columnHelp.addFieldKeyToSet( fieldKeySet );
	}

	ColumnImpl eraseHtmlConversion() {
		ColumnImpl columnImpl = new ColumnImpl( columnKey, this );
		if( columnImpl.columnName instanceof String )
			columnImpl.columnName = ((String)columnImpl.columnName).replaceAll("<[bB][rR]>", " ").trim();
		columnImpl.convertToHtml = false;

		return columnImpl;
	}

	public String format( Map recordMap, MessageHandler msghandler ) {
		if( columnValue == null ) {
			if( recordMap == null ) return "";

			Object value = recordMap.get( fieldKey );
			if( value == null ) return "";

			if( convertToHtml )
				return HtmlUtility.toHtmlString( value );
			else
				return value.toString();
		} else
			return columnValue.format( recordMap, msghandler );
	}

	public StringBuffer format( Map recordMap, MessageHandler msghandler, StringBuffer stringBuffer ) {
		return stringBuffer.append( format(recordMap, msghandler) );
	}

	public Object getColumnAttr() {
		return columnAttr;
	}

	public ColumnGroup getColumnGroup() {
		return columnGroup;
	}

	public String getColumnHelp( Map recordMap, MessageHandler msghandler ) {
		return ( columnHelp == null ? null : columnHelp.format(recordMap, msghandler) );
	}

	public int getColumnSize() {
		return columnSize;
	}

	public Object getColumnTitle() {
		return columnName;
	}

	public String getColumnTitle( Map recordMap, MessageHandler msghandler ) {
		if( columnName instanceof RecordFormat )
			return ((RecordFormat)columnName).format( recordMap, msghandler );
		else if( columnName != null )
			return columnName.toString();
		else
			return "";
	}

	public Object getColumnValue( Map recordMap, MessageHandler msghandler ) {
		if( columnValue == null ) {
			if( recordMap == null ) return "";

			Object value = recordMap.get( fieldKey );
			if( value == null ) return "";

			if( convertToHtml )
				return HtmlUtility.toHtmlString( value );
			else
				return value;
		} else
			return columnValue.format( recordMap, msghandler );
	}

	public String getDataCellAttr() {
		return dataCellAttr;
	}

	public String getFieldKey() {
		return fieldKey;
	}

	public String getKey() {
		return columnKey;
	}

	public boolean sortable() {
		return sortable;
	}
}
