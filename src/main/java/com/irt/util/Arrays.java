/*
 *	File Name:	Arrays.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.1	iterator(), toList() 추가
 *	stghr12		2006/12/01		2.1.0	copy() -> clone()
 *	stghr12		2006/02/28		2.0.0	create( ArrayUtil -> Arrays )
 *
**/

package com.irt.util;

/**
 *
 */
public class Arrays {
	Arrays() {}

	public static String[] append( String[] arrays, String value ) {
		String[] arrays_new = new String[ arrays.length + 1 ];
		System.arraycopy( arrays, 0, arrays_new, 0, arrays.length );
		arrays_new[arrays.length] = value;

		return arrays_new;
	}

	public static String[] append( String[] arrays, String[] values ) {
		String[] arrays_new = new String[ arrays.length + values.length ];
		System.arraycopy( arrays, 0, arrays_new, 0, arrays.length );
		System.arraycopy( values, 0, arrays_new, arrays.length, values.length );

		return arrays_new;
	}

	public static String[] clone( String[] arrays ) {
		String[] arrays_new = new String[ arrays.length ];
		System.arraycopy( arrays, 0, arrays_new, 0, arrays.length );

		return arrays_new;
	}

	public static boolean contains( Object[] arrays, Object value ) {
		for( int i = 0; i < arrays.length; i++ )
			if( value.equals(arrays[i]) )
				return true;

		return false;
	}

	public static <E> java.util.Iterator<E> iterator( E[] arrays ) {
		return new Arrays.Iterator<E>( arrays );
	}

	public static <E> java.util.List<E> toList( E[] arrays ) {
		java.util.List<E> list = new java.util.ArrayList<E>( arrays.length );
		for( int i = 0; i < arrays.length; i++ )
			list.add( arrays[i] );

		return list;
	}

	/**
	 *
	 */
	private static class Iterator<E> implements java.util.Iterator<E> {
		int index;
		E[] arrays;

		Iterator( E[] arrays ) {
			this.index = 0;
			this.arrays = arrays;
		}

		public boolean hasNext() {
			try {
				return ( index < arrays.length );
			} catch( NullPointerException nullEx ) {
				return false;
			}
		}

		public E next() throws java.util.NoSuchElementException {
			try {
				return arrays[index++];
			} catch( ArrayIndexOutOfBoundsException idxEx ) {
				throw new java.util.NoSuchElementException();
			}
		}

		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
}
