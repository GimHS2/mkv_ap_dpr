/*
 *	File Name:	FieldKeyArray.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.1	IllegalArgumentException message 변경: invalid -> illegal
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.html;

import java.util.List;

/**
 *
 */
class FieldKeyArray {
	String fieldKeyArrayName;
	String[] fieldKeys;
	String[] options;

	FieldKeyArray( String fieldKeyArrayName, String[] fieldKeys, String[] optionKeys ) throws IllegalArgumentException {
		if( optionKeys != null && fieldKeys.length != optionKeys.length )
			throw new IllegalArgumentException( "illegal optionKeys length" );

		this.fieldKeyArrayName = fieldKeyArrayName;
		this.fieldKeys = fieldKeys;
		this.options = optionKeys;
	}

	String[] getFieldKeys() {
		if( options == null ) return fieldKeys;

		List<String> list = new java.util.ArrayList<String>();
		for( int f = 0; f < fieldKeys.length; f++ )
			if( options[f] == null )
				list.add( fieldKeys[f] );

		return list.toArray( new String[list.size()] );
	}

	String[] getFieldKeys( String optionKey ) {
		if( options == null ) return fieldKeys;

		List<String> list = new java.util.ArrayList<String>();
		for( int f = 0; f < fieldKeys.length; f++ )
			if( options[f] == null || options[f].equals(optionKey) )
				list.add( fieldKeys[f] );

		return list.toArray( new String[list.size()] );
	}

	String[] getFieldKeys( String... optionKeys ) {
		if( options == null ) return fieldKeys;

		List<String> list = new java.util.ArrayList<String>();
		for( int f = 0; f < fieldKeys.length; f++ ) {
			if( options[f] == null )
				list.add( fieldKeys[f] );
			else {
				for( int i = 0; i < optionKeys.length; i++ )
					if( options[f].equals(optionKeys[i]) ) {
						list.add( fieldKeys[f] );
						break;
					}
			}
		}

		return list.toArray( new String[list.size()] );
	}

	String getName() {
		return fieldKeyArrayName;
	}
}
