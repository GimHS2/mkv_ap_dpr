/*
 *	File Name:	ColumnPoolImpl.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	getColumns(): "columnKey+suffixKey+suffixKey+..." 구현
 *										getColumnSuffix(), putColumnSuffix() 추가
 *										PatternRecordFormat.getInstance() -> new ListRecordFormat()
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										createColumnList()에 columnListType 추가
 *	stghr12		2007/10/31		2.1.2	IllegalArgumentException message 변경: invalid -> illegal
 *	stghr12		2007/07/31		2.1.1	createColumnList() 오류 수정
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnPool -> com.irt.html.ColumnPool
 *	stghr12		2006/08/25		2.0.1	getColumnGroupMap() 오류 수정
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.*;
import com.irt.util.Arrays;
import java.util.Map;
import com.irt.data.format.RecordFormat;

/**
 *
 */
class ColumnPoolImpl implements com.irt.data.cols.ColumnPool {
	ColumnPool parent;
	String columnPoolName;
	Map<String, Column> columnMap;
	Map<String, ColumnGroup> columnGroupMap;
	Map<String, RecordFormat> columnSuffixMap;
	Map<String, HyperLink> hyperLinkMap;
	String[] primaryFieldKeys, sortKeys;

	ColumnPoolImpl( String columnPoolName ) {
		this.columnPoolName = columnPoolName;
		this.columnMap = new java.util.LinkedHashMap<String, Column>();
	}

	ColumnPoolImpl( String columnPoolName, ColumnPool parent ) {
		this.parent = parent;
		this.columnPoolName = columnPoolName;
		this.columnMap = new java.util.LinkedHashMap<String, Column>();
		if( parent instanceof ColumnPoolImpl ) {
			this.primaryFieldKeys = ((ColumnPoolImpl)parent).primaryFieldKeys;
			this.sortKeys = ((ColumnPoolImpl)parent).sortKeys;
		}
	}

	public ColumnList createColumnList( String columnListName, String[] columnKeys ) throws IllegalArgumentException {
		return createColumnList( columnListName, null, columnKeys, null, null, null );
	}

	public ColumnList createColumnList( String columnListName, String[] columnKeys, String[] primaryFieldKeys ) throws IllegalArgumentException {
		return createColumnList( columnListName, null, columnKeys, primaryFieldKeys, null, null );
	}

	public ColumnList createColumnList( String columnListName, String[] columnKeys, String[] primaryFieldKeys, String[] hiddenFieldKeys )
						throws IllegalArgumentException {
		return createColumnList( columnListName, null, columnKeys, primaryFieldKeys, hiddenFieldKeys, null );
	}

	public ColumnList createColumnList( String columnListName, String[] columnKeys, String[] primaryFieldKeys, String[] hiddenFieldKeys
						, String[] sortKeys ) throws IllegalArgumentException {
		return createColumnList( columnListName, null, columnKeys, primaryFieldKeys, hiddenFieldKeys, sortKeys );
	}

	public ColumnList createColumnList( String columnListName, String columnListType, String[] columnKeys, String[] primaryFieldKeys
						, String[] hiddenFieldKeys, String[] sortKeys ) throws IllegalArgumentException {
		if( primaryFieldKeys == null ) primaryFieldKeys = this.primaryFieldKeys;
		if( sortKeys == null ) sortKeys = this.sortKeys;

		String[] keys = null;
		String[] optionKeys = null;
		for( int k = 0; k < columnKeys.length; k++ ) {
			String[] splitKeys = columnKeys[k].split("/", 2);

			if( splitKeys.length > 1 ) {
				if( keys == null ) {
					keys = Arrays.clone( columnKeys );
					optionKeys = new String[ columnKeys.length ];
				}
				keys[k] = splitKeys[0];
				optionKeys[k] = splitKeys[1];
			} else if( keys != null )
				keys[k] = splitKeys[0];
		}

		if( keys == null )
			return new ColumnListImpl( this, columnListName, getColumns(columnKeys, true), primaryFieldKeys, hiddenFieldKeys, sortKeys );
		else
			return new OptionColumnListImpl(
				this, columnListName, columnListType, getColumns(keys, false), optionKeys, primaryFieldKeys, hiddenFieldKeys, sortKeys
			);
	}

	public Column getColumn( String columnKey ) {
		Column column = handleGetColumn( columnKey );
		while( column == null ) {
			int index = columnKey.lastIndexOf( '.' );
			if( index < 0 ) return column;

			column = handleGetColumn( columnKey = columnKey.substring(0, index) );
		}

		return column;
	}

	public Map<String, Column> getColumnFamily( String fieldKey ) {
		Map<String, Column> map = null;
		if( parent != null )
			map = parent.getColumnFamily( fieldKey );

		for( Column column : columnMap.values() ) {
			if( fieldKey.equals(column.getFieldKey()) ) {
				if( map == null ) map = new java.util.HashMap<String, Column>();
				map.put( column.getKey(), column );
			}
		}

		return map;
	}

	public ColumnGroup getColumnGroup( String columnGroupKey ) {
		ColumnGroup columnGroup = null;
		try {
			columnGroup = columnGroupMap.get( columnGroupKey );
		} catch( NullPointerException nullEx ) {}
		if( columnGroup == null && parent != null )
			columnGroup = parent.getColumnGroup( columnGroupKey );

		return columnGroup;
	}

	public Column[] getColumns( String... columnKeys ) throws IllegalArgumentException {
		return getColumns( columnKeys, true );
	}

	private Column[] getColumns( String[] columnKeys, boolean containsColumnGroup ) throws IllegalArgumentException {
		Column[] columns = new Column[columnKeys.length];
		HyperLink[][] hyperLinks = new HyperLink[columnKeys.length][2];

		for( int k = 0; k < columnKeys.length; k++ ) {
			String[] keys = columnKeys[k].split("#");
			String[] splitKeys = keys[0].split("\\*");

			if( splitKeys.length > 2 )
				throw new IllegalArgumentException( "illegal columnKey '"+ keys[0] +"'" );

			if( splitKeys[0].startsWith("(") && splitKeys[0].endsWith(")") ) {
				String string = splitKeys[0].substring( 1, splitKeys[0].length()-1 );
				if( splitKeys.length == 1 )
					columns[k] = new ConstantColumn( string, 1 );
				else {
					try {
						columns[k] = new ConstantColumn( string, Integer.parseInt(splitKeys[1]) );
					} catch( NumberFormatException numEx ) {
						throw new IllegalArgumentException( "illegal number '"+ splitKeys[1] +"'" );
					}
				}
			} else if( splitKeys[0].startsWith("{") && splitKeys[0].endsWith("}") ) {
				String columnKey = splitKeys[0].substring( 1, splitKeys[0].length()-2 );
				columns[k] = new ColumnImpl( columnKey, columnKey );

				if( splitKeys.length > 1 ) {
					try {
						((ColumnImpl)columns[k]).columnSize = Integer.parseInt( splitKeys[1] );
					} catch( NumberFormatException numEx ) {
						throw new IllegalArgumentException( "illegal number '"+ splitKeys[1] +"'" );
					}
				}
			} else {
				String[] suffixKeys = splitKeys[0].split( "\\+" );

				columns[k] = getColumn( suffixKeys[0] );
				if( columns[k] == null )
					throw new IllegalArgumentException( "illegal columnKey '"+ suffixKeys[0] +"'" );

				ColumnImpl columnImpl = null;
				if( suffixKeys.length > 1 || splitKeys.length > 1 )
					columns[k] = columnImpl = new ColumnImpl( columns[k].getKey(), (ColumnImpl)columns[k] );

				if( suffixKeys.length > 1 ) {
					RecordFormat[] formats = new RecordFormat[ suffixKeys.length ];

					if( columnImpl.columnValue == null ) {
						if( columnImpl.convertToHtml )
							columnImpl.columnValue = com.irt.data.format.PatternRecordFormat.getInstance( "$H{"+ columnImpl.fieldKey +"}" );
						else
							columnImpl.columnValue = com.irt.data.format.PatternRecordFormat.getInstance( "${"+ columnImpl.fieldKey +"}" );
					}

					formats[0] = columnImpl.columnValue;
					for( int f = 1; f < suffixKeys.length; f++ ) {
						formats[f] = getColumnSuffix( suffixKeys[f] );
						if( formats[f] == null )
							throw new IllegalArgumentException( "illegal suffixKey '"+ suffixKeys[f] +"'" );
					}
					columnImpl.columnValue = new com.irt.data.format.ListRecordFormat( formats );
				}

				if( splitKeys.length > 1 ) {
					try {
						columnImpl.columnSize = Integer.parseInt( splitKeys[1] );
					} catch( NumberFormatException numEx ) {
						throw new IllegalArgumentException( "illegal number '"+ splitKeys[1] +"'" );
					}
				}
			}

			if( keys.length > 1 && keys[1].length() > 0 ) {
				hyperLinks[k][0] = getHyperLink( keys[1] );
				if( hyperLinks[k][0] == null )
					throw new IllegalArgumentException( "illegal linkKey '"+ keys[1] +"'" );
			}
			if( keys.length > 2 && keys[2].length() > 0 ) {
				hyperLinks[k][1] = getHyperLink( keys[2] );
				if( hyperLinks[k][1] == null )
					throw new IllegalArgumentException( "illegal linkKey '"+ keys[2] +"'" );
			}
		}

		if( containsColumnGroup ) setColumnGroupToColumnArray( columns );

		for( int k = 0; k < columnKeys.length; k++ ) {
			if( hyperLinks[k][1] == null ) {
				if( hyperLinks[k][0] != null )
					columns[k] = new LinkedColumn( columns[k], hyperLinks[k][0] );
			} else
				columns[k] = new LinkedColumn( columns[k], hyperLinks[k][0], hyperLinks[k][1] );
		}

		return columns;
	}

	public RecordFormat getColumnSuffix( String suffixKey ) {
		if( columnSuffixMap != null && columnSuffixMap.containsKey(suffixKey) )
			return columnSuffixMap.get( suffixKey );
		else if( parent != null && parent instanceof ColumnPoolImpl )
			return ((ColumnPoolImpl)parent).getColumnSuffix( suffixKey );
		else
			return null;
	}

	public HyperLink getHyperLink( String linkKey ) {
		HyperLink link = null;
		try {
			link = hyperLinkMap.get( linkKey );
		} catch( NullPointerException nullEx ) {}
		if( link == null && parent != null && parent instanceof ColumnPoolImpl )
			link = ((ColumnPoolImpl)parent).getHyperLink( linkKey );

		return link;
	}

	public String getName() {
		return columnPoolName;
	}

	public ColumnPool getParent() {
		return parent;
	}

	public Column handleGetColumn( String columnKey ) {
		Column column = columnMap.get( columnKey );
		if( column == null && parent != null )
			column = parent.handleGetColumn( columnKey );

		return column;
	}

	ColumnImpl makeColumn( String columnKey ) {
		Column column = columnMap.get( columnKey );
		if( column != null ) {
			if( column instanceof ColumnImpl )
				return (ColumnImpl)column;
			else
				return null;
		}

		column = handleGetColumn( columnKey );
		for( int index = columnKey.length(); column == null; ) {
			index = columnKey.lastIndexOf( '.', --index );
			if( index < 0 ) return null;

			column = handleGetColumn( columnKey.substring(0, index) );
		}

		if( column instanceof ColumnImpl ) {
			putColumn( column = new ColumnImpl( columnKey, (ColumnImpl)column ) );
			return (ColumnImpl)column;
		} else
			return null;
	}

	ColumnGroupImpl makeColumnGroup( String columnGroupKey ) {
		ColumnGroup columnGroup = null;
		try {
			columnGroup = columnGroupMap.get( columnGroupKey );
			if( columnGroup != null ) {
				if( columnGroup instanceof ColumnGroupImpl )
					return (ColumnGroupImpl)columnGroup;
				else
					return null;
			}
		} catch( NullPointerException nullEx ) {}

		if( parent != null ) {
			columnGroup = parent.getColumnGroup( columnGroupKey );
			if( columnGroup != null && columnGroup instanceof ColumnGroupImpl ) {
				putColumnGroup( columnGroup = new ColumnGroupImpl( columnGroupKey, (ColumnGroupImpl)columnGroup ) );
				return (ColumnGroupImpl)columnGroup;
			}
		}

		return null;
	}

	HyperLinkImpl makeHyperLink( String linkKey ) {
		HyperLink link = null;
		try {
			link = hyperLinkMap.get( linkKey );
			if( link != null ) {
				if( link instanceof HyperLinkImpl )
					return (HyperLinkImpl)link;
				else
					return null;
			}
		} catch( NullPointerException nullEx ) {}

		if( parent != null && parent instanceof ColumnPoolImpl ) {
			link = ((ColumnPoolImpl)parent).getHyperLink( linkKey );
			if( link != null && link instanceof HyperLinkImpl ) {
				putHyperLink( link = new HyperLinkImpl( linkKey, (HyperLinkImpl)link ) );
				return (HyperLinkImpl)link;
			}
		}

		return null;
	}

	void putColumn( Column column ) {
		columnMap.put( column.getKey(), column );
	}

	void putColumnGroup( ColumnGroup columnGroup ) {
		try {
			columnGroupMap.put( columnGroup.getKey(), columnGroup );
		} catch( NullPointerException nullEx ) {
			if( columnGroupMap != null ) throw nullEx;
			(columnGroupMap = new java.util.LinkedHashMap<String, ColumnGroup>()).put( columnGroup.getKey(), columnGroup );
		}
	}

	void putColumnSuffix( String suffixKey, RecordFormat suffixFormat ) {
		try {
			columnSuffixMap.put( suffixKey, suffixFormat );
		} catch( NullPointerException nullEx ) {
			if( columnSuffixMap != null ) throw nullEx;
			(columnSuffixMap = new java.util.LinkedHashMap<String, RecordFormat>()).put( suffixKey, suffixFormat );
		}
	}

	void putHyperLink( HyperLink link ) {
		try {
			hyperLinkMap.put( link.getKey(), link );
		} catch( NullPointerException nullEx ) {
			if( hyperLinkMap != null ) throw nullEx;
			(hyperLinkMap = new java.util.LinkedHashMap<String, HyperLink>()).put( link.getKey(), link );
		}
	}

	public boolean setColumnGroupToColumnArray( Column... columns ) {
		boolean hasColumnGroup = false;
		if( columnGroupMap != null ) {
			ColumnGroup groups[] = new ColumnGroup[ columns.length ];
			for( int c = 0; c < columns.length; c++ )
				groups[c] = columns[c].getColumnGroup();

			for( ColumnGroup columnGroup : columnGroupMap.values() ) {
				for( int c = 0; c < columns.length - 1; c++ ) {
					if( groups[c+1] != null )
						c++;
					else if( groups[c] == columnGroup ) {
						if( columnGroup.contains(columns[c+1]) )
							groups[c+1] = columnGroup;
					} else if( groups[c] == null ) {
						if( columnGroup.contains(columns[c]) && columnGroup.contains(columns[c+1]) ) {
							hasColumnGroup = true;
							groups[c] = groups[c+1] = columnGroup;
						}
					}
				}
			}

			if( hasColumnGroup ) {
				for( int c = 0; c < columns.length; c++ ) {
					if( groups[c] != null )
						columns[c] = groups[c].createGroupColumn( columns[c] );
				}
			}
		}
		if( parent != null && parent.setColumnGroupToColumnArray(columns) )
			return true;

		return hasColumnGroup;
	}
}
