/*
 *	File Name:	StandardCode.java
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
 *	stghr12		2002/04/15				create
 *
**/

package com.irt.rbm.sys;

import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Map;

public class StandardCode extends com.irt.rbm.ManipulableManagerImpl {
	StandardCode( SQLHandler handler, Table table, QueryFactory factory ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String code ) {
		return Record.createMap( "code", code );
	}

	public String getName( String code ) throws SQLException {
		return (String)getFieldValue( Record.createMap("code", code), "name" );
	}
}
