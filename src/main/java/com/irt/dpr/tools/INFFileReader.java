/*
 *	File Name:	INFFileReader.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.0	create
 *
**/

package com.irt.dpr.tools;

import com.irt.data.DataException;
import com.irt.dpr.tools.INFFileException;
import com.irt.sql.SQLHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public interface INFFileReader {

	public java.util.Date getDocumentDate();

	public String getDocumentType();

	public String getMessageId();

	public Map<String, Object> readNext( String[] keys ) throws DataException, IOException;

	public String getResultMessage();

	public String getResultStatus();

	public int read() throws INFFileException, IOException, SQLException;

	public void updateMessageLog( String documentFileName ) throws DataException, SQLException;

	public void updateMessageLog( String documentFileName, String previousMessageId ) throws DataException, SQLException;

}
