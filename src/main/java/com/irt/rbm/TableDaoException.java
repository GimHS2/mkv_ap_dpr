/*
 *	File Name:	TableDaoException.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/06/30		2.2.0c	create
 *
**/

package com.irt.rbm;

import com.irt.data.DataException;
import com.irt.sql.Table;
import com.irt.util.MessageHandler;

import java.util.Map;

public class TableDaoException extends DataException {
	public final static String ERR_TAO_MAP_IS_NULL = "ERR_TAO_MAP_IS_NULL";
	public final static String ERR_TAO_MAP_KEY_MISSED = "ERR_TAO_MAP_KEY_MISSED";
	public final static String ERR_TAO_MAP_KEYVALUE_NOT_FOUND = "ERR_TAO_MAP_KEYVALUE_NOT_FOUND";
	public final static String ERR_TAO_MAP_KEYVALUE_ARRAY_CANNOT_DETERMINE = "ERR_TAO_MAP_KEYVALUE_ARRAY_CANNOT_DETERMINE";

	public TableDaoException( Table table, String errorKey, Map recordMap ) {
		super( errorKey, errorKey, recordMap );
	}

	public TableDaoException( Table table, String errorKey, String message, Map recordMap ) {
		super( errorKey, errorKey, recordMap );
	}
}
