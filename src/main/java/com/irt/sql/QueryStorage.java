/*
 *	File Name:	QueryStorage.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/02/28		2.0.0	create(com.irt.system.ConnectionContext에서 분리)
 *
**/

package com.irt.sql;

/**
 *
 */
public class QueryStorage {
	java.util.List<Object[]> savedQueryBufferList;

	public QueryStorage() {
		this.savedQueryBufferList = null;
	}

	public QueryBuffer getSavedQueryBuffer( int index ) {
		return (QueryBuffer)savedQueryBufferList.get(index)[1];
	}

	public String[] getSavedQueryCallerTrace( int index ) {
		return (String[])savedQueryBufferList.get(index)[0];
	}

	public long getSavedQueryElapsedTime( int index ) {
		return ((Long)savedQueryBufferList.get(index)[2]).longValue();
	}

	public int getSavedQuerySize() {
		return( savedQueryBufferList == null ? 0 : savedQueryBufferList.size() );
	}

	public void saveQuery( QueryBuffer querybuf, long elapsedTimeMilli ) {
		try {
			throw new Exception();
		} catch( Exception ex ) {
			if( savedQueryBufferList == null ) savedQueryBufferList = new java.util.Vector<Object[]>();

			String[] methods = null;
			StackTraceElement[] stackTraceElements = ex.getStackTrace();
			if( stackTraceElements != null && stackTraceElements.length > 1 ) {
				methods = new String[stackTraceElements.length - 1];
				for( int i = 1; i < stackTraceElements.length; i++ ) {
					StackTraceElement stackTraceElement = stackTraceElements[i];
					methods[i-1] = stackTraceElement.toString();
				}
			}

			savedQueryBufferList.add( new Object[] { methods, querybuf, new Long(elapsedTimeMilli) } );
		}
	}
}
