/*
 *	File Name:	ColumnListFactory.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.1	appendColumn(columnKey, columnName, columnAttr, dataPattern, helpPattern, sortable): helpPattern관련 오류 수정
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnListFactory -> com.irt.html.ColumnListFactory
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2005/08/05		1.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnGroup;
import com.irt.data.cols.ColumnList;
import com.irt.data.format.PatternRecordFormat;
import com.irt.data.format.RecordFormat;
import java.util.List;

/**
 *
 */
public class ColumnListFactory {
	String columnListName;
	List<Column> list;
	String[] primaryFieldKeys, hiddenFieldKeys;
	Column column;
	ColumnGroupImpl columnGroupImpl;

	public ColumnListFactory( ColumnList columnList ) {
		this.columnListName = columnList.getName();
		this.primaryFieldKeys = columnList.getPrimaryFieldKeys();
		this.hiddenFieldKeys = columnList.getHiddenFieldKeys();
		this.list = new java.util.ArrayList<Column>();

		Column[] columns = columnList.getColumns();
		for( int c = 0; c < columns.length; c++ )
			list.add( columns[c] );
	}

	public ColumnListFactory( String columnListName ) {
		this.columnListName = columnListName;
		this.list = new java.util.ArrayList<Column>();
	}

	public Column appendColumn( String columnKey, String columnName ) {
		return appendColumn( columnKey, columnName, null, null, null, false );
	}

	public Column appendColumn( String columnKey, String columnName, String columnAttr ) {
		return appendColumn( columnKey, columnName, columnAttr, null, null, false );
	}

	public Column appendColumn( String columnKey, String columnName, String columnAttr, boolean sortable ) {
		return appendColumn( columnKey, columnName, columnAttr, null, null, sortable );
	}

	public Column appendColumn( String columnKey, String columnName, String columnAttr, String dataPattern ) {
		return appendColumn( columnKey, columnName, columnAttr, dataPattern, null, false );
	}

	public Column appendColumn( String columnKey, String columnName, String columnAttr, String dataPattern, boolean sortable ) {
		return appendColumn( columnKey, columnName, columnAttr, dataPattern, null, sortable );
	}

	public Column appendColumn( String columnKey, String columnName, String columnAttr, String dataPattern, String helpPattern ) {
		return appendColumn( columnKey, columnName, columnAttr, dataPattern, helpPattern, false );
	}

	public Column appendColumn( String columnKey, String columnName, String columnAttr, String dataPattern, String helpPattern, boolean sortable ) {
		ColumnImpl columnImpl;

		if( dataPattern == null )
			columnImpl = new ColumnImpl( columnKey, columnName, columnAttr );
		else
			columnImpl = new ColumnImpl( columnKey, columnName, columnAttr, PatternRecordFormat.getInstance(dataPattern) );
		if( helpPattern != null )
			columnImpl.columnHelp = PatternRecordFormat.getInstance( helpPattern );
		columnImpl.sortable = sortable;
		columnImpl.columnGroup = columnGroupImpl;
		list.add( this.column = columnImpl );

		return columnImpl;
	}

	/**
	 * 마지막으로 append한 Column에 HyperLink append.
	 */
	public HyperLink appendColumnLink( String hrefPattern, String helpPattern ) {
		return appendHyperLink( hrefPattern, helpPattern, 'C' );
	}

	/**
	 * 마지막으로 append한 Column에 HyperLink append.
	 */
	public HyperLink appendHeaderLink( String hrefPattern, String helpPattern ) {
		return appendHyperLink( hrefPattern, helpPattern, 'H' );
	}

	private HyperLink appendHyperLink( String hrefPattern, String helpPattern, int type ) {
		int size = list.size();
		if( size == 0 ) return null;

		RecordFormat href = PatternRecordFormat.getInstance( hrefPattern );
		RecordFormat help = ( helpPattern == null ? null : PatternRecordFormat.getInstance(helpPattern) );

		HyperLink link = new HyperLinkImpl( column.getKey(), href, help );
		if( type == 'C' ) {
			if( column instanceof LinkedColumn )
				((LinkedColumn)column).columnLink = link;
			else
				list.set( size - 1, new LinkedColumn(column, link) );
		} else {
			if( column instanceof LinkedColumn )
				((LinkedColumn)column).headerLink = link;
			else
				list.set( size - 1, new LinkedColumn(column, null, link) );
		}

		return link;
	}

	public ColumnGroup beginColumnGroup( String columnGroupKey, String columnGroupName ) {
		return this.columnGroupImpl = new ColumnGroupImpl( columnGroupKey, columnGroupName );
	}

	public ColumnGroup beginColumnGroup( String columnGroupKey, String columnGroupName, String columnGroupAttr ) {
		return this.columnGroupImpl = new ColumnGroupImpl( columnGroupKey, columnGroupName, columnGroupAttr );
	}

	public void endColumnGroup() {
		this.columnGroupImpl = null;
	}

	public ColumnList getColumnList() {
		if( list.size() == 0 ) return null;

		return new ColumnListImpl( null, columnListName, list.toArray(new Column[list.size()]), primaryFieldKeys, hiddenFieldKeys, null );
	}

	public void setHiddenFieldKeys( String... hiddenFieldKeys ) {
		this.hiddenFieldKeys = hiddenFieldKeys;
	}

	public void setPrimaryFieldKeys( String... primaryFieldKeys ) {
		this.primaryFieldKeys = primaryFieldKeys;
	}
}
