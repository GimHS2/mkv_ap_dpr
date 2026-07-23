/*
 *	File Name:	LoopTag.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/12/31		2.2.0	records가 null일 때도 rows, minRows가 있으면 동작하도록 수정
 *	stghr12		2007/04/30		2.1.0	minRows, rows 추가
 *										loopIndex option 처리
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/03		1.0.0	version 관리
 *	stghr12		2002/10/02				create
 *
**/

package com.irt.tagext;

import java.util.Collection;
import javax.servlet.jsp.JspException;

/**
 * loopId Collection의 개수만큼 body내용 출력.
 *
 * <ul type='square'>
 * <li>(M)id
 * <li>(M)loopId
 * <li>(M)loopIndex
 * <li>(O)maxRows
 * <li>(O)minRows
 * <li>(O)rows
 * </ul>
 */
public class LoopTag extends javax.servlet.jsp.tagext.BodyTagSupport {
	String id;
	String loopId;
	String loopIndex;
	int maxRows = -1;
	int minRows = -1;
	int rows = -1;
	int index;
	java.util.Iterator iterator;

	public int doAfterBody() throws JspException {
		Object value;

		index++;
		if( iterator != null && iterator.hasNext() ) {
			if( maxRows > 0 && index >= maxRows )
				return SKIP_BODY;
			value = iterator.next();
		} else {
			if( index >= minRows )
				return SKIP_BODY;
			value = null;
		}

		pageContext.setAttribute( loopId, value );
		if( loopIndex != null )
			pageContext.setAttribute( loopIndex, new Integer(index) );

		return EVAL_BODY_AGAIN;
	}

	public int doStartTag() throws JspException {
		if( rows > 0 ) minRows = maxRows = rows;

		Collection records = (Collection)pageContext.findAttribute( id );
		if( records == null || records.size() == 0 ) {
			iterator = null;
			if( minRows <= 0 ) return SKIP_BODY;
		} else {
			iterator = records.iterator();
			pageContext.setAttribute( loopId, iterator.next() );
		}

		index = 0;
		if( loopIndex != null )
			pageContext.setAttribute( loopIndex, new Integer(index) );

		return EVAL_BODY_INCLUDE;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public void setLoopId( String loopId ) {
		this.loopId = loopId;
	}

	public void setLoopIndex( String loopIndex ) {
		this.loopIndex = loopIndex;
	}

	public void setMaxRows( int maxRows ) {
		this.maxRows = maxRows;
	}

	public void setMinRows( int minRows ) {
		this.minRows = minRows;
	}

	public void setRows( int rows ) {
		this.rows = rows;
	}
}
