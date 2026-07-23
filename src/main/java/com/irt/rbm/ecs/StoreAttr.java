/*
 *	File Name:	StoreAttr.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	wykim		2002/01/18				create
 *
**/

package com.irt.rbm.ecs;

import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

public class StoreAttr extends com.irt.rbm.ManipulableManagerImpl {
	StoreAttr( SQLHandler handler, Table table, QueryFactory factory ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String code ) {
		return Record.createMap( "code", code );
	}

	public int delete( String code ) throws DataException, SQLException {
		if( delete(Record.createMap("code", code)) )
			return 1;
		else
			return 0;
	}

	public String getName( String code ) throws SQLException {
		return (String)getFieldValue( Record.createMap("code", code), "name" );
	}
}
