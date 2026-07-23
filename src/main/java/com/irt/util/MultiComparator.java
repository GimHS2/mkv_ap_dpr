/*
 *	File Name:	MultiComparator.java
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

package com.irt.util;

import java.util.Comparator;

/**
 *
 */
public class MultiComparator<T> implements java.util.Comparator<T> {
	Comparator<T>[] comparators;

	public MultiComparator( Comparator<T>... comparators ) {
		this.comparators = comparators;
	}

	public int compare( T obj1, T obj2 ) {
		for( int i = 0; i < comparators.length; i++ ) {
			int comp = comparators[i].compare( obj1, obj2 );
			if( comp != 0 ) return comp;
		}

		return 0;
	}

	public boolean equals( Object obj ) {
		if( this == obj ) return true;
		if( !(obj instanceof MultiComparator) ) return false;

		MultiComparator comparator = (MultiComparator)obj;

		if( this.comparators == comparator.comparators ) return true;
		if( this.comparators.length != comparator.comparators.length ) return false;

		for( int i = 0; i < this.comparators.length; i++ ) {
			if( !this.comparators[i].equals(comparator.comparators[i]) )
				return false;
		}

		return true;
	}
}
