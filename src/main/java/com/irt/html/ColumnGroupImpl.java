/*
 *	File Name:	ColumnGroupImpl.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	Column 변경사항 적용: getColumnTitle() 변경/추가
 *										ColumnGroup 변경사항 적용: getGroupTitle() 변경/추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.1	getColumnKeys() 추가
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnGroupImpl -> com.irt.html.ColumnGroupImpl
 *	stghr12		2006/02/28		2.0.0	version up(기존의 ColumnGroup은 ColumGroupImpl로 바꾸고, interface로 변경)
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnGroup;
import com.irt.data.format.RecordFormat;
import com.irt.util.MessageHandler;
import java.util.Map;

/**
 *
 */
class ColumnGroupImpl implements com.irt.data.cols.ColumnGroup {
	String columnGroupKey;
	Object columnGroupName, columnGroupAttr;
	Map<String, Object[]> columnMap;

	ColumnGroupImpl( String columnGroupKey, Object columnGroupName ) {
		this( columnGroupKey, columnGroupName, null );
	}

	ColumnGroupImpl( String columnGroupKey, Object columnGroupName, Object columnGroupAttr ) {
		this.columnGroupKey = columnGroupKey;
		this.columnGroupName = columnGroupName;
		this.columnGroupAttr = ( columnGroupAttr == null ? "" : columnGroupAttr );
		this.columnMap = new java.util.LinkedHashMap<String, Object[]>();
	}

	ColumnGroupImpl( ColumnGroupImpl columnGroupImpl ) {
		this( columnGroupImpl.columnGroupKey, columnGroupImpl );
	}

	ColumnGroupImpl( String columnGroupKey, ColumnGroupImpl columnGroupImpl ) {
		this.columnGroupKey = columnGroupKey;
		this.columnGroupName = columnGroupImpl.columnGroupName;
		this.columnGroupAttr = columnGroupImpl.columnGroupAttr;
		this.columnMap = new java.util.LinkedHashMap<String, Object[]>( columnGroupImpl.columnMap );
	}

	void appendColumnKey( String columnKey ) {
		columnMap.put( columnKey, new Object[] { columnKey, null, null } );
	}

	void appendColumnKey( String columnKey, Object columnName ) {
		columnMap.put( columnKey, new Object[] { columnKey, columnName, null } );
	}

	void appendColumnKey( String columnKey, Object columnName, Object columnAttr ) {
		columnMap.put( columnKey, new Object[] { columnKey, columnName, columnAttr } );
	}

	public boolean contains( String columnKey ) {
		return columnMap.containsKey( columnKey );
	}

	public boolean contains( Column column ) {
		return columnMap.containsKey(column.getKey()) || columnMap.containsKey(column.getFieldKey());
	}

	public Column createGroupColumn( Column column ) {
		Object[] options = columnMap.get( column.getKey() );
		if( options == null ) {
			options = columnMap.get( column.getFieldKey() );
			if( options == null ) return null;
		}

		LinkedColumn linkedColumn = null;
		if( column instanceof LinkedColumn ) {
			linkedColumn = (LinkedColumn)column;
			column = linkedColumn.getSourceColumn();
		}

		if( column instanceof ColumnImpl ) {
			ColumnImpl columnImpl = new ColumnImpl( (String)options[0], (ColumnImpl)column );
			columnImpl.columnGroup = this;
			if( options[1] != null ) columnImpl.columnName = options[1];
			if( options[2] != null ) columnImpl.columnAttr = options[2];
			column = columnImpl;
		} else
			column = new ColumnGroupImpl.ColumnG( column, options[1], options[2] );

		if( linkedColumn != null )
			column = new LinkedColumn( column, linkedColumn.columnLink, linkedColumn.headerLink );

		return column;
	}

	public String[] getColumnKeys() {
		if( columnMap.size() == 0 ) return null;

		String[] columnKeys = new String[ columnMap.size() ];
		columnMap.keySet().toArray( columnKeys );

		return columnKeys;
	}

	public Object getGroupAttr() {
		return columnGroupAttr;
	}

	public Object getGroupTitle() {
		return columnGroupName;
	}

	public String getGroupTitle( Map recordMap, MessageHandler msghandler ) {
		if( columnGroupName instanceof RecordFormat )
			return ((RecordFormat)columnGroupName).format( recordMap, msghandler );
		else if( columnGroupName != null ) 
			return columnGroupName.toString();
		else
			return "";
	}

	public String getKey() {
		return columnGroupKey;
	}

	class ColumnG extends com.irt.data.cols.ColumnWrapper {
		Object columnName;
		Object columnAttr;

		ColumnG( Column column, Object columnName, Object columnAttr ) {
			super( column );
			this.columnName = ( columnName == null ? column.getColumnTitle() : columnName );
			this.columnAttr = ( columnAttr == null ? column.getColumnAttr() : columnAttr );
		}

		public Object getColumnAttr() {
			return columnName;
		}

		public ColumnGroup getColumnGroup() {
			return ColumnGroupImpl.this;
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
	}
}
