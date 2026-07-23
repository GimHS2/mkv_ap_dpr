/*
 *	File Name:	ResourceBundleWriter.java
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

public interface ResourceBundleWriter {

	String makeFileHeader();

	String makeHeaderSection( String sectionName );

	String makeFileTail();

}
