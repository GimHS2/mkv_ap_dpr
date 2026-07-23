/*
 *	File Name:	ColumnListImpl.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getColumnListType(), getColumnListTypeList(), setColumnListType() 추가
 *										eraseHtmlConversion() 오류수정: primaryFieldKeys, hiddenFieldKeys, sortKeys, propertyValues 추가
 *	stghr12		2007/07/31		2.1.2	getPropertyValue() 추가
 *	stghr12		2007/04/30		2.1.1	eraseHtmlConversion() 추가.
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnListImpl -> com.irt.html.ColumnListImpl
 *	stghr12		2006/02/28		2.0.0	version up(기존의 LinkedColumn은 LinkedColumnImpl로 바꾸고, interface로 변경)
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.Column;
import java.util.List;
import java.util.Set;

/**
 *
 */
class ColumnListImpl implements com.irt.data.cols.ColumnList {
	ColumnPoolImpl columnPoolImpl;
	String columnListName;
	int columnSize;
	Column[] columns;
	String[] primaryFieldKeys, hiddenFieldKeys, sortKeys, propertyValues;
	Set<String> fieldKeySet;

	String columnListType;
	List<String[]> columnListTypeList;

	ColumnListImpl() {}

	ColumnListImpl( ColumnPoolImpl columnPoolImpl, String columnListName, Column[] columns, String[] primaryFieldKeys, String[] hiddenFieldKeys
						, String[] sortKeys ) {
		this.columnPoolImpl = columnPoolImpl;
		this.columnListName = columnListName;
		this.columns = columns;
		this.primaryFieldKeys = primaryFieldKeys;
		this.hiddenFieldKeys = hiddenFieldKeys;
		this.sortKeys = sortKeys;

		columnSize = 0;
		for( int c = 0; c < columns.length; c++ )
			columnSize += columns[c].getColumnSize();
		initializeFieldKeySet();
	}

	ColumnListImpl eraseHtmlConversion() {
		ColumnListImpl columnListImpl = new ColumnListImpl();

		columnListImpl.columnPoolImpl = this.columnPoolImpl;
		columnListImpl.columnListName = this.columnListName;
		columnListImpl.primaryFieldKeys = this.primaryFieldKeys;
		columnListImpl.hiddenFieldKeys = this.hiddenFieldKeys;
		columnListImpl.sortKeys = this.sortKeys;
		columnListImpl.propertyValues = this.propertyValues;

		columnListImpl.columnListType = this.columnListType;
		columnListImpl.columnListTypeList = this.columnListTypeList;

		columnListImpl.columnSize = 0;
		columnListImpl.columns = new Column[ this.columns.length ];
		for( int c = 0; c < columns.length; c++ ) {
			Column column = ( columns[c] instanceof LinkedColumn ? ((LinkedColumn)columns[c]).getSourceColumn() : columns[c] );
			if( column instanceof ColumnImpl )
				column = ((ColumnImpl)column).eraseHtmlConversion();

			columnListImpl.columns[c] = column;
			columnListImpl.columnSize += column.getColumnSize();
		}
		columnListImpl.initializeFieldKeySet();

		return columnListImpl;
	}

	public Column getColumn( int index ) {
		return columns[index];
	}

	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnListType() {
		return columnListType;
	}

	public List<String[]> getColumnListTypeList() {
		return columnListTypeList;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public Column[] getColumns() {
		Column[] new_columns = new Column[columns.length];
		System.arraycopy( columns, 0, new_columns, 0, columns.length );
		return new_columns;
	}

	public String[] getFieldKeyArray() {
		String[] fieldKeys = new String[ columns.length ];
		for( int c = 0; c < columns.length; c++ )
			fieldKeys[c] = columns[c].getFieldKey();

		return fieldKeys;
	}

	public String[] getFieldKeys() {
		return fieldKeySet.toArray( new String[fieldKeySet.size()] );
	}

	public String[] getFieldKeys( String extraFieldKey ) {
		String[] fieldKeys;

		if( fieldKeySet.contains(extraFieldKey) )
			fieldKeys = getFieldKeys();
		else {
			fieldKeys = fieldKeySet.toArray( new String[fieldKeySet.size() + 1] );
			fieldKeys[fieldKeySet.size()] = extraFieldKey;
		}

		return fieldKeys;
	}

	public String[] getFieldKeys( String... extraFieldKeys ) {
		java.util.List<String> list = new java.util.ArrayList<String>();

		for( int i = 0; i < extraFieldKeys.length; i++ )
			if( !fieldKeySet.contains(extraFieldKeys[i]) )
				list.add( extraFieldKeys[i] );

		String[] fieldKeys;
		if( list.size() == 0 )
			fieldKeys = getFieldKeys();
		else {
			fieldKeys = fieldKeySet.toArray( new String[fieldKeySet.size() + list.size()] );
			if( list.size() == 1 )
				fieldKeys[fieldKeySet.size()] = list.get(0);
			else {
				if( list.size() < extraFieldKeys.length )
					extraFieldKeys = list.toArray( new String[list.size()] );
				System.arraycopy( extraFieldKeys, 0, fieldKeys, fieldKeySet.size(), extraFieldKeys.length );
			}
		}

		return fieldKeys;
	}

	public String[] getHiddenFieldKeys() {
		if( hiddenFieldKeys == null ) return null;
		return com.irt.util.Arrays.clone( hiddenFieldKeys );
	}

	public String getName() {
		return columnListName;
	}

	public String[] getPrimaryFieldKeys() {
		if( primaryFieldKeys == null ) return null;
		return com.irt.util.Arrays.clone( primaryFieldKeys );
	}

	public String getProperty( String key ) {
		if( propertyValues == null ) return null;
		for( int i = 0; i < propertyValues.length; i++ )
			if( propertyValues[i].startsWith(key +"=") )
				return propertyValues[i].substring( key.length()+1 );

		return null;
	}

	public String[] getSortKeys() {
		if( sortKeys == null ) return null;
		return com.irt.util.Arrays.clone( sortKeys );
	}

	void initializeFieldKeySet() {
		this.fieldKeySet = new java.util.HashSet<String>();
		for( int c = 0; c < columns.length; c++ )
			columns[c].addFieldKeyToSet( fieldKeySet );

		if( primaryFieldKeys != null )
			for( int i = 0; i < primaryFieldKeys.length; i++ )
				fieldKeySet.add( primaryFieldKeys[i] );

		if( hiddenFieldKeys != null )
			for( int i = 0; i < hiddenFieldKeys.length; i++ )
				fieldKeySet.add( hiddenFieldKeys[i] );
	}

	void setColumnListType( String columnListType, List<String[]> columnListTypeList ) {
		this.columnListType= columnListType;
		this.columnListTypeList = columnListTypeList;
	}
}
