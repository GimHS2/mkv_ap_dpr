/*
 *	File Name:	TableAccessor.java
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

package com.irt.rbm;

import java.util.Map;

public interface TableAccessor {

	/**
	 * @return primaryFieldKeys
	 */
	String[] getPrimaryFieldKeys();

	String[] getBindFieldKeys( int statementType );

	String[] getReadonlyFieldKeys();

	/**
	 * TODO: needs to resolve when there is no key and when value is array so cannot decide which one is right value from sourceMap to primaryMap ( no
	 * access db or other instance )
	 * 
	 * @param sourceMap
	 *            : usually {@link com.irt.servlet.ParameterMap}
	 * @param valueIndex
	 *            : if Object is array then get the value at 'valueIndex'
	 * @return primaryMap
	 * @throws TableDaoException
	 */
	Map<String, Object> extractPrimary( Map<String, Object> sourceMap, int valueIndex ) throws TableDaoException;

	/**
	 * 
	 * @param sourceMap
	 *            : usually {@link com.irt.servlet.ParameterMap}
	 * @return
	 * @throws TableDaoException
	 */
	Map<String, Object[]> extractPrimaryKeyValues( Map<String, Object> sourceMap ) throws TableDaoException;

}
