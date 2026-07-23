/*
 *	File Name:	MergedColumnList.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	create
 *
**/

package com.irt.data.cols;

import java.util.List;

/**
 *
 */
public final class MergedColumnList extends ColumnListWrapper implements Cloneable {
	ColumnList[] columnLists;

	public MergedColumnList( ColumnList columnList ) {
		super( columnList );
		this.columnLists = new ColumnList[] { columnList };
	}

	public void addAtFirst( ColumnList columnList ) {
		ColumnList[] columnLists = new ColumnList[ this.columnLists.length + 1 ];

		System.arraycopy( this.columnLists, 0, columnLists, 1, this.columnLists.length );
		columnLists[0] = columnList;

		this.columnLists = columnLists;
	}

	public void addAtLast( ColumnList columnList ) {
		ColumnList[] columnLists = new ColumnList[ this.columnLists.length + 1 ];

		System.arraycopy( this.columnLists, 0, columnLists, 0, this.columnLists.length );
		columnLists[columnLists.length - 1] = columnList;

		this.columnLists = columnLists;
	}

	public Object clone() {
		MergedColumnList columnList = new MergedColumnList( getSourceColumnList() );
		columnList.columnLists = this.columnLists;

		return columnList;
	}

	public Column getColumn( int index ) {
		for( int i = 0; i < columnLists.length; i++ ) {
			if( columnLists[i].getColumnCount() < index )
				return columnLists[i].getColumn( index );
			index -= columnLists[i].getColumnCount();
		}

		return columnLists[0].getColumn( columnLists[0].getColumnCount() + 1 );
	}

	public int getColumnCount() {
		int columnCount = 0;

		for( int i = 0; i < columnLists.length; i++ )
			columnCount += columnLists[i].getColumnCount();

		return columnCount;
	}

	public int getColumnSize() {
		int columnSize = 0;

		for( int i = 0; i < columnLists.length; i++ )
			columnSize += columnLists[i].getColumnSize();

		return columnSize;
	}

	public Column[] getColumns() {
		int length = 0;
		Column[] columns = new Column[getColumnCount()];

		for( int i = 0; i < columnLists.length; i++ ) {
			System.arraycopy( columnLists[i].getColumns(), 0, columns, length, columnLists[i].getColumnCount() );
			length += columnLists[i].getColumnCount();
		}

		return columns;
	}

	public String[] getFieldKeyArray() {
		String[] fieldKeys = columnLists[0].getFieldKeyArray();

		for( int i = 1; i < columnLists.length; i++ )
			fieldKeys = com.irt.util.Arrays.append( fieldKeys, columnLists[i].getFieldKeyArray() );

		return fieldKeys;
	}

	public String[] getFieldKeys() {
		String[] fieldKeys = columnLists[0].getFieldKeys();

		for( int i = 1; i < columnLists.length; i++ )
			fieldKeys = columnLists[i].getFieldKeys( fieldKeys );

		return fieldKeys;
	}

	public String[] getFieldKeys( String... extraFieldKeys ) {
		String[] fieldKeys = columnLists[0].getFieldKeys( extraFieldKeys );

		for( int i = 1; i < columnLists.length; i++ )
			fieldKeys = columnLists[i].getFieldKeys( fieldKeys );

		return fieldKeys;
	}
}
