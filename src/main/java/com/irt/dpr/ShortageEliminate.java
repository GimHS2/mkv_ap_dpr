/*
 *	File Name:	ShortageEliminate.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2017/08/31		2.2.0	create
 *
**/

package com.irt.dpr;

import com.irt.data.DataException;
import com.irt.data.DataWriter;
import com.irt.data.QueryableManager;
import com.irt.data.cols.Column;
import com.irt.data.cols.ColumnList;
import com.irt.sql.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 *
 */
public class ShortageEliminate extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.DPR_SHORTAGE_ELIMINATE_ITEM );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.DPR_SHORTAGE_ELIMINATE_ITEM );

	public ShortageEliminate( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String orderKey, String itemCode ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "productReqKey", orderKey );
		primaryMap.put( "itemCode", itemCode );

		return primaryMap;
	}

	public boolean updateStatus( String orderKey, String itemCode, String status ) throws DataException, SQLException {
		String statement = "UPDATE DPR_SHORTAGE_ELIMINATE_ITEM SET STATUS = ?, UPGDATE = SYSDATE WHERE ORDERKEY = ? AND ITEMCD = ?";
		Object[] bindVars = new String[] { status, orderKey, itemCode };

		return ( SQLManager.executeStatement( handler, statement, bindVars ) > 0 );
	}

	public void write( List<Map<String, Object>> recordList, SQLHandler handler, DataWriter out, ColumnList columnList, int writingOption, int maxRows ) throws IOException, SQLException {
		if( (writingOption & QueryableManager.OPT_WRITING_TITLE) > 0 )
			SQLManager.writeTitle(handler, out, columnList, writingOption);
		if( out instanceof com.irt.util.SSDataWriter )
			((com.irt.util.SSDataWriter) out).setColumnList(columnList);

		Column[] columns = columnList.getColumns();
		com.irt.util.MessageHandler msghandler = handler.getMessageHandler();

		for( int rownum = 1; rownum <= recordList.size(); rownum++ ) {
			if( maxRows > 0 && rownum > maxRows )
				break;
			Map<String, Object> recordMap = recordList.get(rownum - 1);
			recordMap.put("rowNumber", rownum);
			if( (writingOption & OPT_WRITING_ROWNUMBER) > 0 )
				out.print(rownum);
			if( (writingOption & OPT_WRITING_EXECUTETYPE) > 0 )
				out.print("U");
			for( int c = 0; c < columns.length; c++ )
				out.print(columns[c].getColumnValue(recordMap, msghandler),
						columns[c].getColumnSize());
			out.println();
		}
	}

}
