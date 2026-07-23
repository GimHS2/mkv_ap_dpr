/*
 *	File Name:	Season.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	ManipulableManager 변경사항 적용
 *										delete(), deleteAll(), registAll()
 *										regist(): SQLManager 사용으로 변경
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/01/31		1.0.0	version 관리
 *	stghr12		2003/12/02				create
 *
 **/

package com.irt.rbm.ecs;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class Season extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ECS_SEASON );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_SEASON );
	private final static HierarchyCodeField codeField = (HierarchyCodeField)table.getField( "code" );

	public Season( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static Map<String, Object> createPrimary( String code ) {
		return Record.createMap( "code", code );
	}

	@Override
	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		try {
			return ( SQLManager.deleteRecord( handler, codeField, (String)codeField.extractValue(primaryMap) ) > 0 );
		} catch( FieldException fieldEx ) {
			return false;
		}
	}

	@Override
	public DataResult deleteAll( Collection<Map<String, Object>> records ) throws SQLException {
		return SQLManager.deleteRecordAll( handler, codeField, records );
	}

	public int delete( String code ) throws DataException, SQLException {
		return SQLManager.executeStatement( handler, "DELETE ECS_SEASON WHERE SEASON_CD LIKE ? || '%'", code );
	}

	public HierarchyCodeField getCodeField() {
		return codeField;
	}

	public String getName( String code ) throws SQLException {
		return (String)SQLManager.getObjectValue( handler, "SELECT SEASON_NAME FROM ECS_SEASON WHERE SEASON_CD = ?", code );
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		try {
			if( !SQLManager.lockTable( handler, codeField, (String)codeField.extractValue(recordMap) ) )
				throw handler.createDataException( DataException.ERR_NO_UPPERLEVELCODE, recordMap );

			return super.regist( recordMap );
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		}
	}

	@Override
	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException {
		return registEach( records );
	}
}
