/*
 *	File Name:	ToolsCommandExecuter.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/09/30		2.2.1	getCommandDescriptors() 추가
 *	stghr12		2010/08/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.util.MessageHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public interface ToolsCommandExecuter {
	public void execute( Configure configure, String[] commands ) throws IllegalArgumentException, IOException, SQLException;

	public List<ToolsCommandDescriptor> getCommandDescriptors( MessageHandler msghandler );
}
