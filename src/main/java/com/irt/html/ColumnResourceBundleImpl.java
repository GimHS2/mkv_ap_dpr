/*
 *	File Name:	ColumnResourceBundleImpl.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	ColumnListTemplate 사용: columnList를 사용하는 시점에서 생성하도록 로직변경
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										getColumnList(), handleGetColumnList()에 columnListType 추가
 *	stghr12		2007/04/30		2.1.1	ColumnConfigureFile.OPTIONKEY_DELETE_HTML 처리
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.ColumnPoolSet -> com.irt.html.ColumnResourceBundleImpl
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.ColumnList;
import com.irt.data.cols.ColumnPool;
import com.irt.data.cols.ColumnResourceBundle;
import java.util.Map;

/**
 *
 */
class ColumnResourceBundleImpl implements com.irt.data.cols.ColumnResourceBundle {
	ColumnResourceBundle parent;
	Map<String, Object> objectMap;

	ColumnResourceBundleImpl( ColumnResourceBundle parent ) {
		this.parent = parent;
		this.objectMap = new java.util.HashMap<String, Object>();
	}

	boolean containsKey( String key ) {
		return objectMap.containsKey( key );
	}

	public ColumnList getColumnList( String columnListName ) {
		ColumnList columnList = handleGetColumnList( columnListName );
		if( columnList != null ) return columnList;

		int index0 = columnListName.lastIndexOf( '%' );
		String postfix = ( index0 < 0 ? null : columnListName.substring(index0) );
		do {
			int index1 = columnListName.lastIndexOf( '.' );
			if( index1 < 0 ) return columnList;

			if( index1 > index0 )
				columnListName = columnListName.substring( 0, index1 );
			else
				columnListName = columnListName.substring( 0, index1 ) + postfix;

			columnList = handleGetColumnList( columnListName );
		} while( columnList == null );

		return columnList;
	}

	public ColumnList getColumnList( String columnListName, String columnListType, String... optionKeys ) {
		ColumnList columnList = handleGetColumnList( columnListName, columnListType, optionKeys );
		if( columnList != null ) return columnList;

		int index0 = columnListName.lastIndexOf( '%' );
		String postfix = ( index0 < 0 ? null : columnListName.substring(index0) );
		do {
			int index1 = columnListName.lastIndexOf( '.' );
			if( index1 < 0 ) return columnList;

			if( index1 > index0 )
				columnListName = columnListName.substring( 0, index1 );
			else
				columnListName = columnListName.substring( 0, index1 ) + postfix;

			columnList = handleGetColumnList( columnListName, columnListType, optionKeys );
		} while( columnList == null );

		return columnList;
	}

	public ColumnPool getColumnPool( String columnPoolName ) {
		ColumnPool columnPool = handleGetColumnPool( columnPoolName );
		while( columnPool == null ) {
			int index = columnPoolName.lastIndexOf( '.' );
			if( index < 0 ) return columnPool;

			columnPool = handleGetColumnPool( columnPoolName = columnPoolName.substring(0, index) );
		}

		return columnPool;
	}

	public String[] getFieldKeyArray( String fieldKeyArrayName ) {
		String[] fieldKeys = handleGetFieldKeyArray( fieldKeyArrayName );
		while( fieldKeys == null ) {
			int index = fieldKeyArrayName.lastIndexOf( '.' );
			if( index < 0 ) return fieldKeys;

			fieldKeys = handleGetFieldKeyArray( fieldKeyArrayName = fieldKeyArrayName.substring(0, index) + "[]" );
		}

		return fieldKeys;
	}

	public String[] getFieldKeyArray( String fieldKeyArrayName, String... optionKeys ) {
		String[] fieldKeys = handleGetFieldKeyArray( fieldKeyArrayName, optionKeys );
		while( fieldKeys == null ) {
			int index = fieldKeyArrayName.lastIndexOf( '.' );
			if( index < 0 ) return fieldKeys;

			fieldKeys = handleGetFieldKeyArray( fieldKeyArrayName = fieldKeyArrayName.substring(0, index) + "[]", optionKeys );
		}

		return fieldKeys;
	}

	Object getObject( String key ) {
		Object object = objectMap.get( key );
		if( object == null && parent != null && (parent instanceof ColumnResourceBundleImpl) )
			object = ((ColumnResourceBundleImpl)parent).getObject( key );

		return object;
	}

	public ColumnResourceBundle getParent() {
		return parent;
	}

	public ColumnList handleGetColumnList( String columnListName ) {
		Object object = objectMap.get( columnListName );
		if( object instanceof ColumnListTemplate )
			return ((ColumnListTemplate)object).toColumnList( this );
		else if( object instanceof ColumnList )
			return (ColumnList)object;

		if( parent != null )
			return parent.handleGetColumnList( columnListName );

		return null;
	}

	public ColumnList handleGetColumnList( String columnListName, String columnListType, String... optionKeys ) {
		Object object = objectMap.get( columnListName );

		ColumnList columnList = null;
		if( object instanceof ColumnListTemplate )
			columnList = ((ColumnListTemplate)object).toColumnList( this );
		else if( object instanceof ColumnList )
			columnList = (ColumnList)object;

		if( columnList != null ) {
			if( columnList instanceof OptionColumnListImpl )
				columnList = ((OptionColumnListImpl)columnList).getColumnList( columnListType, optionKeys );

			if( columnList instanceof ColumnListImpl ) {
				for( int i = 0; i < optionKeys.length; i++ )
					if( ColumnConfigureFile.OPTIONKEY_DELETE_HTML.equals(optionKeys[i]) )
						return ((ColumnListImpl)columnList).eraseHtmlConversion();
			}

			return columnList;
		}

		if( parent != null )
			return parent.handleGetColumnList( columnListName, columnListType, optionKeys );

		return null;
	}

	public ColumnPool handleGetColumnPool( String columnPoolName ) {
		try {
			ColumnPool columnPool = (ColumnPool)objectMap.get( columnPoolName );
			if( columnPool != null ) return columnPool;
		} catch( ClassCastException castEx ) {}

		if( parent != null )
			return parent.handleGetColumnPool( columnPoolName );

		return null;
	}

	public String[] handleGetFieldKeyArray( String fieldKeyArrayName ) {
		try {
			FieldKeyArray fieldKeyArray = (FieldKeyArray)objectMap.get( fieldKeyArrayName );
			if( fieldKeyArray != null ) return fieldKeyArray.getFieldKeys();
		} catch( ClassCastException castEx ) {}

		if( parent != null )
			return parent.handleGetFieldKeyArray( fieldKeyArrayName );

		return null;
	}

	public String[] handleGetFieldKeyArray( String fieldKeyArrayName, String... optionKeys ) {
		try {
			FieldKeyArray fieldKeyArray = (FieldKeyArray)objectMap.get( fieldKeyArrayName );
			if( fieldKeyArray != null ) return fieldKeyArray.getFieldKeys( optionKeys );
		} catch( ClassCastException castEx ) {}

		if( parent != null )
			return parent.handleGetFieldKeyArray( fieldKeyArrayName, optionKeys );

		return null;
	}

	boolean makeAlias( String alias, String sourceKey ) {
		Object object = objectMap.get( sourceKey );
		if( object != null ) {
			objectMap.put( alias, object );
			return true;
		}

		return false;
	}

	void putColumnList( ColumnList columnList ) {
		objectMap.put( columnList.getName(), columnList );
	}

	void putColumnList( ColumnListTemplate columnListTemplate ) {
		objectMap.put( columnListTemplate.getName(), columnListTemplate );
	}

	void putColumnPool( ColumnPool columnPool ) {
		objectMap.put( columnPool.getName(), columnPool );
	}

	void putFieldKeyArray( FieldKeyArray fieldKeyArray ) {
		objectMap.put( fieldKeyArray.getName(), fieldKeyArray );
	}

	void putObject( String key, Object object ) {
		objectMap.put( key, object );
	}
}
