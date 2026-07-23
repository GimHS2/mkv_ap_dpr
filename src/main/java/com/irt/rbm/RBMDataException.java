/*
 *	File Name:	RBMDataException.java
 *	Version:	2.0.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	stghr12		2002/10/18				create
 *
**/

package com.irt.rbm;

import java.util.Map;

/**
 *
 */
public class RBMDataException extends com.irt.data.DataException {
	/** GLN코드가 잘못(check digit등)된 경우 */
	public final static String ERR_INVALID_GLN						= "ERR_INVALID_GLN";
	/** GTIN코드가 잘못(check digit등)된 경우 */
	public final static String ERR_INVALID_GTIN						= "ERR_INVALID_GTIN";

	public RBMDataException( String errorKey, String message ) {
		super( errorKey, message );
	}

	public RBMDataException( String errorKey, String message, Map recordMap ) {
		super( errorKey, message, recordMap );
	}
}
