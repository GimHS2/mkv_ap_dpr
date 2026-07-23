/*
 *	File Name:	OptionColumnListImpl.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										columnListType 추가
 *	stghr12		2007/07/31		2.1.2	propertyValues 처리 추가
 *	stghr12		2007/04/30		2.1.1	다중 Option 및 '!' Option 지원
 *										OptionColumnListImpl 생성자: columnSize 계산 오류 수정
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.OptionColumnListImpl -> com.irt.html.OptionColumnListImpl
 *	stghr12		2006/02/28		2.0.0	version up(기존의 LinkedColumn은 LinkedColumnImpl로 바꾸고, interface로 변경)
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.Column;
import java.util.List;

/**
 *
 */
class OptionColumnListImpl extends ColumnListImpl {
	Column[] optionColumns;
	String[][] options;

	OptionColumnListImpl( ColumnPoolImpl columnPoolImpl, String columnListName, String columnListType, Column[] columns, String[] optionKeys
						, String[] primaryFieldKeys, String[] hiddenFieldKeys, String[] sortKeys ) {
		this.columnPoolImpl = columnPoolImpl;
		this.columnListName = columnListName;
		this.columns = columns;
		this.primaryFieldKeys = primaryFieldKeys;
		this.hiddenFieldKeys = hiddenFieldKeys;
		this.sortKeys = sortKeys;

		this.optionColumns = columns;
		this.options = new String[optionKeys.length][];

		List<Column> list = new java.util.ArrayList<Column>();
		for( int c = 0; c < columns.length; c++ ) {
			if( optionKeys[c] == null )
				list.add( columns[c] );
			else {
				options[c] = optionKeys[c].split( "/" );
				if( isValidOption(options[c], columnListType) )
					list.add( columns[c] );
			}
		}
		this.columns = list.toArray( new Column[list.size()] );
		columnPoolImpl.setColumnGroupToColumnArray( this.columns );

		this.columnSize = 0;
		for( int c = 0; c < this.columns.length; c++ )
			this.columnSize += this.columns[c].getColumnSize();
		initializeFieldKeySet();
	}

	ColumnListImpl getColumnList( String columnListType, String... optionKeys ) {
		if( options == null ) return this;

		if( columnListTypeList == null )
			columnListType = null;
		else {
			if( columnListType != null ) {
				boolean isValid = false;

				for( String[] columnListTypes : columnListTypeList ) {
					if( isValid = columnListTypes[0].equals(columnListType) )
						break;
				}
				if( !isValid ) columnListType = this.columnListType;
			} else
				columnListType = this.columnListType;

			optionKeys = com.irt.util.Arrays.append( optionKeys, columnListType );
		}

		List<Column> list = new java.util.ArrayList<Column>();
		for( int c = 0; c < optionColumns.length; c++ ) {
			if( isValidOption( options[c], optionKeys ) )
				list.add( optionColumns[c] );
		}

		Column[] columns = list.toArray( new Column[list.size()] );
		columnPoolImpl.setColumnGroupToColumnArray( columns );

		ColumnListImpl columnListImpl = new ColumnListImpl( columnPoolImpl, columnListName, columns, primaryFieldKeys, hiddenFieldKeys, sortKeys );
		columnListImpl.propertyValues = propertyValues;
		columnListImpl.setColumnListType( columnListType, columnListTypeList );

		return columnListImpl;
	}

	private static boolean isValidOption( String[] options, String... optionKeys ) {
		if( options == null ) return true;

		for( int v = 0; v < options.length; v++ ) {
			if( options[v].length() > 0 && options[v].charAt(0) == '!' ) {
				String optionValue = options[v].substring(1);

				for( int i = 0; i < optionKeys.length; i++ )
					if( optionValue.equals(optionKeys[i]) )
						return false;
			} else {
				boolean validOptionValue = false;

				for( int i = 0; i < optionKeys.length; i++ )
					if( options[v].equals(optionKeys[i]) ) {
						validOptionValue = true;
						break;
					}

				if( !validOptionValue ) return false;
			}
		}
		return true;
	}
}
