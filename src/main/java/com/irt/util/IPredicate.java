/*
 *	File Name:	IPredicate.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/06/30		2.2.0	create
 *
**/

package com.irt.util;

/**
 * Collection filter.
 * {@link #apply(T)} 에 boolean 조건을 정의해서 사용함.
 * 
 * @see com.irt.util.Predicate
 */
public interface IPredicate<T> {
	boolean apply( T type );
}
