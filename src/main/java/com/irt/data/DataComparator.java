/*
 *	File Name:	DataComparator.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	create
 *
**/

package com.irt.data;

import java.util.Map;

/**
 *
 */
public class DataComparator implements java.util.Comparator<Map> {
	public final static char NULLS_FIRST				= 'F';
	public final static char NULLS_LAST					= 'L';
	public final static char NULLS_NONE					= ' ';

	private String[] fieldKeys;
	private boolean[] ascendings;
	private char[] nullOptions;

	public DataComparator( String fieldKey, boolean ascending ) {
		this( fieldKey, ascending, NULLS_NONE );
	}

	public DataComparator( String fieldKey, boolean ascending, char nullOption ) {
		this.fieldKeys = new String[] { fieldKey };
		this.ascendings = new boolean[] { ascending };
		this.nullOptions = new char[] { nullOption };
	}

	public DataComparator( String[] fieldKeys, boolean[] ascendings ) {
		if( fieldKeys.length != ascendings.length )
			throw new IllegalArgumentException( "illegal argument length("+ fieldKeys.length +", "+ ascendings.length +")" );
		this.fieldKeys = fieldKeys;
		this.ascendings = ascendings;
		this.nullOptions = new char[fieldKeys.length];
		for( int i = 0; i < fieldKeys.length; i++ )
			this.nullOptions[i] = NULLS_NONE;
	}

	public DataComparator( String[] fieldKeys, boolean[] ascendings, char[] nullOptions ) {
		if( fieldKeys.length != ascendings.length || fieldKeys.length != nullOptions.length )
			throw new IllegalArgumentException( "illegal argument ("+ fieldKeys.length +", "+ ascendings.length +", "+ nullOptions.length +")" );
		this.fieldKeys = fieldKeys;
		this.ascendings = ascendings;
		this.nullOptions = nullOptions;
	}

	public void append( String fieldKey, boolean ascending ) {
		append( fieldKey, ascending, NULLS_NONE );
	}

	public void append( String fieldKey, boolean ascending, char nullOption ) {
		String[] fieldKeys = new String[ this.fieldKeys.length + 1 ];
		boolean[] ascendings = new boolean[ this.ascendings.length + 1 ];
		char[] nullOptions = new char[ this.nullOptions.length + 1 ];

		System.arraycopy( this.fieldKeys, 0, fieldKeys, 0, this.fieldKeys.length );
		System.arraycopy( this.ascendings, 0, ascendings, 0, this.ascendings.length );
		System.arraycopy( this.nullOptions, 0, nullOptions, 0, this.nullOptions.length );
		fieldKeys[ this.fieldKeys.length ] = fieldKey;
		ascendings[ this.ascendings.length ] = ascending;
		nullOptions[ this.nullOptions.length ] = nullOption;

		this.fieldKeys = fieldKeys;
		this.ascendings = ascendings;
		this.nullOptions = nullOptions;
	}

	public int compare( Map map1, Map map2 ) {
		Object obj1 = map1;
		Object obj2 = map2;

		int index = 0;
		boolean ascending = ascendings[0];
		char nullOption = nullOptions[0];
		while( true ) {
			if( obj1 == obj2 ) {
				if( index == 0 ) return 0;
			} else if( obj1 == null ) {
				switch( nullOption ) {
				case NULLS_FIRST:
					return -1;
				case NULLS_LAST:
					return 1;
				default:
					return ( ascending ? 1 : -1 );
				}
			} else if( obj2 == null ) {
				switch( nullOption ) {
				case NULLS_FIRST:
					return 1;
				case NULLS_LAST:
					return -1;
				default:
					return ( ascending ? -1 : 1 );
				}
			} else if( obj1 instanceof Comparable ) {
				int comp;
				if( ascending )
					comp = ((Comparable)obj1).compareTo( obj2 );
				else
					comp = ((Comparable)obj2).compareTo( obj1 );

				if( comp != 0 ) return comp;
			}
			if( index >= fieldKeys.length ) return 0;

			obj1 = map1.get( fieldKeys[index] );
			obj2 = map2.get( fieldKeys[index] );
			ascending = ascendings[index];
			nullOption = nullOptions[index];
			index++;
		}
	}

	public boolean equals( Object obj ) {
		if( this == obj ) return true;
		if( !(obj instanceof DataComparator) ) return false;

		DataComparator comparator = (DataComparator)obj;

		if( this.fieldKeys.length != comparator.fieldKeys.length ) return false;
		if( this.fieldKeys == comparator.fieldKeys && this.ascendings == comparator.ascendings && this.nullOptions == comparator.nullOptions )
			return true;

		for( int i = 0; i < this.fieldKeys.length; i++ ) {
			if( !this.fieldKeys[i].equals(comparator.fieldKeys[i]) )
				return false;
			else if( this.ascendings[i] != comparator.ascendings[i] )
				return false;
			else if( this.nullOptions[i] != comparator.nullOptions[i] );
				return false;
		}

		return true;
	}

	public String getFieldKey( int index ) {
		return fieldKeys[index];
	}

	public String toString() {
		StringBuffer sbuf = new StringBuffer();

		sbuf.append( "DataComparator { " );
		for( int i = 0; i < this.fieldKeys.length; i++ ) {
			if( i > 0 ) sbuf.append( ", " );
			sbuf.append( this.fieldKeys[i] +"#"+ (this.ascendings[i] ? "ASC" : "DESC") );
			switch( this.nullOptions[i] ) {
			case NULLS_FIRST:
				sbuf.append( " NULLS FIRST" );
				break;
			case NULLS_LAST:
				sbuf.append( " NULLS LAST" );
				break;
			}
		}
		sbuf.append( " }" );

		return sbuf.toString();
	}
}
