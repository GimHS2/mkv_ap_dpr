/*
 *	File Name:	LinkedColumn.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	com.irt.html.form.LinkedColumnImpl -> com.irt.html.LinkedColumn
 *	stghr12		2006/02/28		2.0.0	version up(기존의 LinkedColumn은 LinkedColumnImpl로 바꾸고, interface로 변경)
 *	stghr12		2005/01/10		1.0.0	create
 *
**/

package com.irt.html;

import com.irt.data.cols.Column;
import com.irt.util.MessageHandler;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class LinkedColumn extends com.irt.data.cols.ColumnWrapper {
	HyperLink columnLink, headerLink;

	LinkedColumn( Column column, HyperLink columnLink ) {
		this( column, columnLink, null );
	}

	LinkedColumn( Column column, HyperLink columnLink, HyperLink headerLink ) {
		super( column );
		this.columnLink = columnLink;
		this.headerLink = headerLink;
	}

	public void addFieldKeyToSet( Set<String> fieldKeySet ) {
		super.addFieldKeyToSet( fieldKeySet );
		if( columnLink != null ) columnLink.addFieldKeyToSet( fieldKeySet );
		if( headerLink != null ) headerLink.addFieldKeyToSet( fieldKeySet );
	}

	public String getColumnHelp( Map recordMap, MessageHandler msghandler ) {
		if( columnLink != null )
			return null;
		else
			return super.getColumnHelp( recordMap, msghandler );
	}

	public HyperLink getColumnLink() {
		return columnLink;
	}

	public HyperLink getHeaderLink() {
		return headerLink;
	}
}
