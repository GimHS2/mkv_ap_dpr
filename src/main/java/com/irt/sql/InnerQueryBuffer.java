/*
 *	File Name:	InnerQueryBuffer.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2009/10/31		2.2.0	create
 *
**/

package com.irt.sql;

import java.util.List;

/**
 *
 */
public interface InnerQueryBuffer {
	public boolean existDataAlias( String fieldName );

	public int getBindVariableCount();

	public List<Object> getBindVariableList();

	public String getQuery();
}
