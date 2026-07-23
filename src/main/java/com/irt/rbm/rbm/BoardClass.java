/*
 *	File Name:	BoardClass.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.rbm;

import com.irt.data.FieldException;
import com.irt.data.Record;
import com.irt.sql.*;
import java.util.Map;

/**
 *
 */
public class BoardClass extends com.irt.rbm.QueryableManagerImpl {
	private final static Table table = Schema.findTable( Schema.RBM_BOARD_CLASS );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.RBM_BOARD_CLASS );

	public BoardClass( SQLHandler handler ) {
		super( handler, factory );
	}

	public static Map<String, Object> createPrimary( String boardClassCode ) {
		return Record.createMap( "boardClassCode", boardClassCode );
	}

	protected QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) {
		try {
			return table.setPrimaryConditionQuery( querybuf );
		} catch( FieldException fieldEx ) {
			return null;
		}
	}
}
