/*
 *	File Name:	MultiDataLoader.java
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

package com.irt.sql;

import com.irt.data.*;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
public class MultiDataLoader extends BasicDataLoader {
	ValidableField fld_executeType;
	Map<String, DataLoader.Loader> loaderMap;

	public MultiDataLoader( String[] lineFieldKeys, Map<String, ? extends Object> lineDefaultMap, DataLoader.Loader[] loaders ) {
		super( lineFieldKeys, lineDefaultMap, null );

		String executeTypeValue = "";
		this.loaderMap = new java.util.HashMap<String, DataLoader.Loader>();
		for( int i = 0; i < loaders.length; i++ ) {
			String executeType = loaders[i].getExecuteType();

			if( executeType == null )
				throw new IllegalArgumentException( "illegal executeType null." );
			else if( loaderMap.put( loaders[i].getExecuteType(), loaders[i] ) != null )
				throw new IllegalArgumentException( "duplicated executeType '"+ executeType +"'." );

			executeTypeValue += ","+ executeType;
			loaderMap.put( executeType, loaders[i] );
		}
		this.fld_executeType = new ValidableField( false, "executeType", "EXECUTETYPE", "PUB_EXECUTETYPE_", executeTypeValue.substring(1) );
	}

	public void close() {
		super.close();
		for( DataLoader.Loader loader : loaderMap.values() )
			loader.close();
	}

	public DataLoader.Loader getDataLoader( String executeType ) {
		return loaderMap.get( executeType );
	}

	public Map<String, Object> loadLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
		try {
			String executeType = (String)fld_executeType.validate( recordMap );
			return getDataLoader( executeType ).loadLine( handler, recordMap );
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		}
	}

	public Map<String, Object> processLine( SQLHandler handler, Map<String, Object> recordMap ) throws DataException, SQLException {
		try {
			String executeType = (String)fld_executeType.validate( recordMap );
			return getDataLoader( executeType ).processLine( handler, recordMap );
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		}
	}
}
