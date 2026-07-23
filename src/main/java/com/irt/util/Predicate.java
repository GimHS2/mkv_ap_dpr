/*
 *	File Name:	Predicate.java
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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Predicate는 조건에 따라서 Collection을 제외시키고 Collection 혹은 T를 리턴함.
 * <br> "filter" method는 Collection을 리턴.
 * <br> "select" method는 Collection안의 T를 리턴.
 * 
 * <br>
 * <br> examples.
 * <pre>
 * List<String> strColl = new ArrayList<String>();
 * strColl.add( "a10" );
 * strColl.add( "a19" );
 * strColl.add( "a20" );
 * 
 * <br> "filter" usage:
 * IPredicate<String> pred_strEndsWithZero = new IPredicate<String>() {
 * 	public boolean apply( String str ) {
 * 		return str != null && str.endsWith("0");
 * 	}
 * };
 * List<String> strEndsWithZeroList = (List<String>) Predicate.filter(strColl, pred_strEndsWithZero);
 * # strEndsWithZeroList has "a10", "a20"
 * </pre>
 * 
 * <pre>
 * <br> "select" usage:
 * IPredicate<String> pred_strNotEndsWithZero = new IPredicate<String>() {
 * 	public boolean apply( String str ) {
 * 		return str != null && !(str.endsWith("0"));
 * 	}
 * };
 * String strNotEndsWithZero = Predicate.select(strColl, pred_strNotEndsWithZero);
 * # strNotEdsWithZero is "a19"
 * </pre>
 * 
 * <pre>
 * <br> "filter" and "select" usage:
 * List<MyTypeA> missingObjects = (List<MyTypeA>) Predicate.filter(myCollectionOfA, new IPredicate<MyTypeA>() {
 * 	public boolean apply( MyTypeA objectOfA ) {
 * 		Predicate.predicateParams = objectOfA.getName();
 * 		return Predicate.select(myCollectionB, new IPredicate<MyTypeB>() {
 * 			public boolean apply( MyTypeB objectOfB ) {
 * 				return objectOfB.getName().equals(Predicate.predicateParams.toString());
 * 			}
 * 		}) == null;
 * 	}
 * });
 * </pre>
 */
public class Predicate {

	/** 
	 * "filter"안에서 "select"를 실행하는 경우같이 특수한 경우에만 사용.
	 * 리스트와 리스트의 "filter" 사용시 중간에 임시값을 저장하고,
	 * "select"안에서 이 임시값을 사용하여 조건에 맞는지 확인하는데 사용함.
	 * class javadoc의 "filter" and "select" usage 섹션 참조.
	 */
	public static Object predicateParams;

	/** 
	 * Collection에 대해서 조건에 따라서 제외시키고 Collection을 리턴시키는 기능.
	 * @param target : Collection을 상속한 Set/Bag/List 등에 대해서,
	 * @param predicate : IPredication에 정의된 조건에 따라서 filter한 후,
	 * @return : 결과값을 ArrayList(Collection)로 return함.
	 */
	public static <T> Collection<T> filter( Collection<T> target, IPredicate<T> predicate ) {
		Collection<T> result = new ArrayList<T>();
		for( T element : target ) {
			if( predicate.apply(element) ) {
				result.add(element);
			}
		}
		return result;
	}

	/** 
	 * Collection에 대해서 조건에 따라서 제외시키고 Collection을 리턴시키는 기능.
	 * "result" param을 통해서 Collection의 output Type을 결정해줌. 
	 * @param target : Collection을 상속한 Set/Bag/List/Queue등에 대해서,
	 * @param predicate : IPredication에 정의된 조건에 따라서 filter한 후, 
	 * @param result : 결과값을 저장할 장소를 미리 생성하여 파라미터로 패스하고,
	 * @return : 결과값을 result에 저장한 후 return함.
	 * ( {@link #filter(Collection, IPredicate)} 기능은 ArrayList를 "filter"기능 안에서 생성하여 리턴함. )
	 */
	public static <T> Collection<T> filter( Collection<T> target, IPredicate<T> predicate, Collection<T> result ) {
		for( T element : target ) {
			if( predicate.apply(element) ) {
				result.add(element);
			}
		}
		return result;
	}

	/** 
	 * Collection에서 조건에 맞는 처음 발견한 결과값 혹은 defaultValue를 리턴.
	 * @param target : Collection을 상속한 Set/Bag/List/Queue등에 대해서,
	 * @param predicate : IPredicate로 정의된 조건에 따라서 filter한후,
	 * @return : 처음 발견한 결과값을 리턴함
	 */
	public static <T> T select( Collection<T> target, IPredicate<T> predicate ) {
		T result = null;
		for( T element : target ) {
			if( !predicate.apply(element) )
				continue;
			result = element;
			break;
		}
		return result;
	}

	/**
	 * Collection에서 조건에 맞는 처음 발견한 결과값 혹은 defaultValue를 리턴함. 
	 * @param target : Collection을 상속한 Set/Bag/List/Queue등에 대해서,
	 * @param predicate : IPredicate로 정의된 조건에 따라서 filter한후,
	 * @param defaultValue : IPredicate 조건에 만족하지 않는 경우에 리턴할 Object를 미리 정의하여 패스한 후,
	 * @return : 처음 발견한 결과값 혹은 defaultValue를 리턴함.
	 */
	public static <T> T select( Collection<T> target, IPredicate<T> predicate, T defaultValue ) {
		T result = defaultValue;
		for( T element : target ) {
			if( !predicate.apply(element) )
				continue;
			result = element;
			break;
		}
		return result;
	}
}