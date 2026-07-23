 /*
 *	File Name:	DataResult.java
 *	Version:	2.2.3c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2019/08/30		2.2.3c	increaseErrorCount( int ) 추가
 *	jbaek		2019/05/30		2.2.2c	appendWarn(), getWarns() 추가
 *	guksm		2008/09/18		2.2.1	rowCount, getRowCount(), increaseRowCount() 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/10/31		2.1.2	getIgnoreCount(), increaseIgnoreCount(), increaseWarningCount(warningCount) 추가
 *										increaseErrorCount(): increaseWarningCount(): count도 함께 증가하도록 수정
 *										increaseRegistCount(): 버그수정
 *	stghr12		2007/04/30		2.1.1	getException(), getExecuteTimeMillis(), increaseErrorCount(), setExecuteTimeMillis() 추가
 *	stghr12		2006/12/01		2.1.0	create
 *
**/

package com.irt.data;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public class DataResult {
	int count, deleteCount, errorCount, ignoreCount, modifyCount, registCount, warningCount, rowCount;
	long executeTimeMillis;
	List<DataException> errors;
	List<DataException> warns;

	public DataResult() {
		this.count = 0;
		this.deleteCount = 0;
		this.errorCount = 0;
		this.ignoreCount = 0;
		this.modifyCount = 0;
		this.registCount = 0;
		this.warningCount = 0;
		this.rowCount = 0;
		this.executeTimeMillis = 0;
	}

	public void appendError( DataException dataEx ) {
		count++;
		errorCount++;
		if( errors == null ) errors = new java.util.ArrayList<DataException>();
		errors.add( dataEx );
	}

	public void appendError( int linenum, DataException dataEx ) {
		appendError( new DataException(linenum, dataEx) );
	}

	public void appendWarn( DataException dataEx ) {
		count++;
		warningCount++;
		if( warns == null ) warns = new java.util.ArrayList<DataException>();
		warns.add( dataEx );
	}

	public void appendWarn( int linenum, DataException dataEx ) {
		appendWarn( new DataException(linenum, dataEx) );
	}

	public void clear() {
		this.count = this.deleteCount = this.errorCount = this.ignoreCount = this.modifyCount = this.registCount = this.warningCount = this.rowCount= 0;
	}

	public int getCount() {
		return count;
	}

	public int getDeleteCount() {
		return deleteCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public Collection<DataException> getErrors() {
		return errors;
	}

	public Collection<DataException> getWarns() {
		return warns;
	}

	public DataException getException() {
		return ( errors == null ? null : errors.get(0) );
	}

	public long getExecuteTimeMillis() {
		return executeTimeMillis;
	}

	public int getIgnoreCount() {
		return ignoreCount;
	}

	public int getModifyCount() {
		return modifyCount;
	}

	public int getRegistCount() {
		return registCount;
	}

	public int getSuccessCount() {
		return deleteCount + modifyCount + registCount;
	}

	public int getWarningCount() {
		return warningCount;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void increaseErrorCount() {
		this.count++;
		this.errorCount++;
	}

	public void increaseErrorCount( int errorCount ) {
		this.errorCount += errorCount;
	}

	public void increaseCount() {
		this.count++;
	}

	public void increaseDeleteCount() {
		this.count++;
		this.deleteCount++;
	}

	public void increaseDeleteCount( int deleteCount ) {
		this.count += deleteCount;
		this.deleteCount += deleteCount;
	}

	public void increaseIgnoreCount() {
		this.count++;
		this.ignoreCount++;
	}

	public void increaseIgnoreCount( int ignoreCount ) {
		this.count += ignoreCount;
		this.ignoreCount += ignoreCount;
	}

	public void increaseModifyCount() {
		this.count++;
		this.modifyCount++;
	}

	public void increaseModifyCount( int modifyCount ) {
		this.count += modifyCount;
		this.modifyCount += modifyCount;
	}

	public void increaseRegistCount() {
		this.count++;
		this.registCount++;
	}

	public void increaseRegistCount( int registCount ) {
		this.count += registCount;
		this.registCount += registCount;
	}

	public void increaseSuccessCount( int type ) {
		switch( type ) {
		case Record.DELETE:
			this.deleteCount++;
			break;
		case Record.MODIFY:
			this.modifyCount++;
			break;
		case Record.REGIST:
			this.registCount++;
			break;
		default:
			return;
		}
		this.count++;
	}

	public void increaseSuccessCount( int type, int count ) {
		switch( type ) {
		case Record.DELETE:
			this.deleteCount += count;
			break;
		case Record.MODIFY:
			this.modifyCount += count;
			break;
		case Record.REGIST:
			this.registCount += count;
			break;
		default:
			return;
		}
		this.count += count;
	}

	public void increaseWarningCount() {
		this.count++;
		this.warningCount++;
	}

	public void increaseWarningCount( int warningCount ) {
		this.count += warningCount;
		this.warningCount += warningCount;
	}

	public void increaseRowCount() {
		this.rowCount++;
	}

	public void setExecuteTimeMillis( long executeTimeMillis ) {
		this.executeTimeMillis = executeTimeMillis;
	}
}
