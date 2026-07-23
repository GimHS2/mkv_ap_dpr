/*
 *	File Name:	TableProcDataLoader.java
 *	Version:	2.2.0c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/05/30		2.2.0c	create
 *
**/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.sql.SQLHandler;
import com.irt.sql.Table;
import com.irt.sql.TableDataLoader;

import java.sql.SQLException;
import java.util.Map;

public class TableProcDataLoader extends TableDataLoader implements LineProcessor {
	LineProcessor lineProcessor;

	public TableProcDataLoader( String[] lineFieldKeys, Map<String, ? extends Object> lineDefaultMap, SQLHandler handler, Table table,
			String[] updateFieldKeys, int statementType ) throws SQLException {
		super(lineFieldKeys, lineDefaultMap, handler, table, updateFieldKeys, statementType);
	}

	@Override
	public void close() {
		if( lineProcessor != null )
			lineProcessor = null;

		super.close();
	}

	public void setLineProcessor( LineProcessor lineProc ) {
		this.lineProcessor = lineProc;
	}

	@Override
	public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
		if( lineProcessor != null ) {
			Map<String, Object> passing = lineProcessor.processLine(handler, recordMap);

			return super.processLine(handler, passing);
		}

		return super.processLine(handler, recordMap);
	}

}
