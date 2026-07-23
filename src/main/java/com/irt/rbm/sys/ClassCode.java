/*
 *	File Name:	ClassCode.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	ClassCode() 생성자 수정
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2006/12/01		2.1.0	ManipulableManager 변경사항 적용
 *										delete(), deleteAll(), registAll() 구현
 *										delete(), regist(): SQLManager 사용으로 변경
 *	stghr12		2006/02/28		2.0.0	version up
 *	stghr12		2004/02/04		1.0.0	version 관리
 *	stghr12		2002/04/15				create
 *
**/

package com.irt.rbm.sys;

import com.irt.data.*;
import com.irt.sql.*;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class ClassCode extends StandardCode {
	public final static String CATEGORYTYPE_CATE				= "CATE";
	public final static String CATEGORYTYPE_ICATE				= "ICATE";
	public final static String CATEGORYTYPE_HS					= "HS";
	public final static String CATEGORYTYPE_SPSC				= "SPSC";

	HierarchyCodeField codeField;

	ClassCode( SQLHandler handler, Table table, QueryFactory factory ) {
		super( handler, table, factory );
		if( table != null )
			this.codeField = (HierarchyCodeField)table.getField( "code" );
	}

	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		try {
			return ( SQLManager.deleteRecord( handler, codeField, (String)codeField.extractValue(primaryMap) ) > 0 );
		} catch( FieldException fieldEx ) {
			return false;
		}
	}

	public int delete( String code ) throws DataException, SQLException {
		return SQLManager.deleteRecord( handler, codeField, code );
	}

	public DataResult deleteAll( Collection<Map<String, Object>> records ) throws SQLException {
		return SQLManager.deleteRecordAll( handler, codeField, records );
	}

	public HierarchyCodeField getCodeField() {
		return codeField;
	}

	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		try {
			if( !SQLManager.lockTable( handler, codeField, (String)codeField.extractValue(recordMap) ) )
				throw handler.createDataException( DataException.ERR_NO_UPPERLEVELCODE, recordMap );

			return super.regist( recordMap );
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		}
	}

	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException {
		return registEach( records );
	}
}
