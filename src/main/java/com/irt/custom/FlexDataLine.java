/*
 *	File Name:	SystemEx.java
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

package com.irt.custom;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

public interface FlexDataLine {

	void setUseFlexDataLine( boolean useFlexDataLine );

	void printFlexDataLine( JspWriter out ) throws IOException;

	void setFlexDataMaxWidth( int colIdx, int currLength );

	int[] getFlexDataMaxWidth();

}
