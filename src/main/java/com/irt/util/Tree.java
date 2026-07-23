/*
 *	File Name:	Tree.java
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

/**
 * Tree
 */
public class Tree<E> {
	public final static int PREORDER					= 0x01;
	public final static int POSTORDER					= 0x02;
	public final static int CHILID						= 0x03;
	public final static int LEAF						= 0x04;
	public final static int LEVEL						= 0x05;

	private int childCount;
	private E data;
	private Tree<E> parent, firstchild, lastchild, next, prev;

	public Tree() {
		this( null );
	}

	public Tree( E data ) {
		this.data = data;
		parent = firstchild = lastchild = next = prev = null;
	}

	public Tree<E> addChild( Tree<E> tree ) {
		return addChild( tree, childCount );
	}

	public Tree<E> addChild( Tree<E> tree, int index ) {
		tree.parent = this;
		if( firstchild == null )
			firstchild = lastchild = tree;
		else if( index >= childCount || index < 0 ) {
			lastchild.next = tree;
			tree.prev = lastchild;
			tree.next = null;
			lastchild = tree;
		} else if( index == 0 ) {
			tree.prev = null;
			tree.next = firstchild;
			firstchild.prev = tree;
			firstchild = tree;
		} else {
			Tree<E> tree1 = getChildAt(index);

			tree.prev = tree1.prev;
			tree.next = tree1;
			tree1.prev.next = tree;
			tree1.prev = tree;
		}
		childCount++;

		return tree;
	}

	public boolean equals( Object obj ) {
		try {
			if( data == null )
				return ( ((Tree)obj).data == null );
			else
				return data.equals( ((Tree)obj).data );
		} catch( ClassCastException castEx ) {
			return false;
		}
	}

	public Tree<E> getChildAt( int index ) {
		if( firstchild == null ) return null;

		Tree<E> tree = firstchild;
		for( int i = 0; i < index; i++ )
			if( (tree = tree.next) == null )
				return null;

		return tree;
	}

	public Tree<E>[] getChilds() {
		if( childCount == 0 ) return null;

		Tree<E>[] childs = new Tree[childCount];
		childs[0] = firstchild;
		for( int i = 1; i < childCount; i++ )
			childs[i] = childs[i-1].next;

		return childs;
	}

	public int getChildCount() {
		return childCount;
	}

	public E getData() {
		return data;
	}

	public Tree<E> getFirstChild() {
		return firstchild;
	}

	public Tree<E> getLastChild() {
		return lastchild;
	}

	public int getLevel() {
		return ( (parent == null) ? 0 : parent.getLevel() + 1 );
	}

	public Tree<E> getNextSibling() {
		return next;
	}

	public Tree<E> getParent() {
		return parent;
	}

	public Tree<E> getPreviousSibling() {
		return prev;
	}

	public boolean hasChild() {
		return ( childCount > 0 );
	}

	public Tree<E> insertSibling( Tree<E> tree ) {
		if( parent == null )
			throw new IllegalArgumentException( "can't be inserted in top-level tree" );

		tree.parent = parent;
		tree.prev = this;
		tree.next = next;

		if( next != null )
			next.prev = tree;
		else
			parent.lastchild = tree;

		next = tree;
		parent.childCount++;

		return tree;
	}

	public java.util.Iterator iterator( int iteratorType ) {
		return new Tree.Iterator( iteratorType, this );
	}

	public Tree<E> next( int iteratorType ) {
		return next( iteratorType, null );
	}

	private Tree<E> next( int iteratorType, Tree<E> top ) {
		Tree<E> tree = this;

		switch( iteratorType ) {
		case PREORDER:
			if( tree.firstchild != null ) return tree.firstchild;

			while( tree != top ) {
				if( tree.next != null ) return tree.next;
				tree = tree.parent;
			}

			return null;
		case POSTORDER:
			if( tree == top ) return null;
			if( tree.next == null ) return tree.parent;

			for( tree = tree.next; tree.firstchild != null; )
				tree = tree.firstchild;

			return tree;
		case CHILID:
			return tree.next;
		case LEAF:
			while( tree != top ) {
				if( tree.next != null ) {
					for( tree = tree.next; tree.firstchild != null; tree = tree.firstchild );
					return tree;
				}
				tree = tree.parent;
			}

			return tree;
		case LEVEL:
			if( tree.next != null ) return tree.next;

			tree = tree.parent;
			for( int depth = 0; tree != null; depth++, tree = tree.parent )
				if( tree.next != null ) {
					Tree<E> child = tree.next.firstchild;
					for( int d = 0; child != null; d++, child = child.firstchild )
						if( d == depth )
							return child;
				}

			return null;
		default:
			throw new IllegalArgumentException( "illegal iteratorType '"+ iteratorType +"'" );
		}
	}

	public void remove() {
		if( parent == null ) return;

		if( prev == null && next == null )
			parent.firstchild = parent.lastchild = null;
		else if( prev == null && next != null ) {
			parent.firstchild = next;
			next.prev = null;
		} else if( prev != null && next == null ) {
			parent.lastchild = prev;
			prev.next = null;
		} else {
			prev.next = next;
			next.prev = prev;
		}

		parent.childCount--;
	}

	/**
	 *
	 */
	private class Iterator implements java.util.Iterator {
		int iteratorType;
		Tree top, prev, next;

		Iterator( int iteratorType, Tree tree ) {
			this.top = tree;

			switch( this.iteratorType = iteratorType ) {
			case CHILID:
				this.next = tree.firstchild;
				break;
			case POSTORDER:
			case LEAF:
				while( tree.firstchild != null )
					tree = tree.firstchild;
			case PREORDER:
			case LEVEL:
				this.next = tree;
				break;
			default:
				throw new IllegalArgumentException( "illegal iteratorType '"+ iteratorType +"'" );
			}
		}

		public boolean hasNext() {
			return ( next != null );
		}

		public Object next() throws java.util.NoSuchElementException {
			if( next == null )
				throw new java.util.NoSuchElementException();

			prev = next;
			next = prev.next( iteratorType );

			return prev.getData();
		}

		public void remove() {
			prev.remove();
		}
	}
}
