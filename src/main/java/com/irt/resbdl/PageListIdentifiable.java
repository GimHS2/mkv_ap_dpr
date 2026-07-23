/*
 *	File Name:	PageListIdentifiable.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0c	create
 *
**/

package com.irt.resbdl;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

public interface PageListIdentifiable {

	void printPageIdentity( JspWriter out ) throws IOException;

	void probeSummaryColumnList();

}
