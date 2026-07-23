/*
 *	File Name:	LoopTagExtraInfo.java
 *	Version:	2.1.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/04/30		2.1.0	loopIndex option 처리
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/03		1.0.0	version 관리
 *	stghr12		2002/10/02				create
 *
**/

package com.irt.tagext;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 *
 */
public class LoopTagExtraInfo extends javax.servlet.jsp.tagext.TagExtraInfo {
	public VariableInfo[] getVariableInfo( TagData data ) {
		if( data.getAttributeString("loopIndex") == null )
			return new VariableInfo[] { new VariableInfo ( data.getAttributeString("loopId"), "java.util.Map", true, VariableInfo.NESTED ) };
		else
			return new VariableInfo[] {
				  new VariableInfo ( data.getAttributeString("loopId"), "java.util.Map", true, VariableInfo.NESTED )
				, new VariableInfo ( data.getAttributeString("loopIndex"), "Integer", true, VariableInfo.NESTED )
			};
	}
}
