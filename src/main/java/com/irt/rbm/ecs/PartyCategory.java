/*
 *	File Name:	PartyCategory.java
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

package com.irt.rbm.ecs;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class PartyCategory extends com.irt.rbm.ManipulableManagerImpl {
	private final static Table table = Schema.findTable( Schema.ECS_PARTY_CATE );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_PARTY_CATE );

	private java.util.Map<String, HierarchyCodeField> instanceMap;

	public PartyCategory( SQLHandler handler ) {
		super( handler, table, factory );
		instanceMap = java.util.Collections.synchronizedMap( new java.util.WeakHashMap<String, HierarchyCodeField>() );
	}

	public static Map<String, Object> createPrimary( String gln, String code ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "gln", gln );
		primaryMap.put( "code", code );

		return primaryMap;
	}

	@Override
	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		String gln = null;
		HierarchyCodeField codeField = null;

		gln = Record.extractString( primaryMap, "gln" );
		if( gln != null ) codeField = getCodeField( gln );
		if( codeField == null ) return false;

		try {
			String statement = "DELETE ECS_PARTY_CATE WHERE GLN = ? AND CATE_CD LIKE ? || '%'";
			return ( SQLManager.executeStatement( handler, statement, new Object[] { gln, codeField.extractValue(primaryMap) } ) > 0 );
		} catch( FieldException fieldEx ) {
			return false;
		}
	}

	public int delete( String gln, String code ) throws DataException, SQLException {
		return SQLManager.executeStatement( handler, "DELETE ECS_PARTY_CATE WHERE GLN = ? AND CATE_CD LIKE ? || '%'", new Object[] { gln, code } );
	}

	@Override
	public DataResult deleteAll( Collection<Map<String, Object>> records ) throws SQLException {
		return deleteEach( records );
	}

	public HierarchyCodeField getCodeField( String gln ) throws SQLException {
		HierarchyCodeField codeField = null;

		if( instanceMap.containsKey(gln) )
			return instanceMap.get( gln );

		String codeLengthValue = (String)SQLManager.getObjectValue( handler, "SELECT CATE_LEN FROM ECS_PARTY_ENV WHERE GLN = ?", gln );
		if( codeLengthValue != null ) {
			int[] codeLengths = new int[codeLengthValue.length() / 2];
			for( int l = 0; l < codeLengthValue.length() / 2; l++ )
				codeLengths[l] = Integer.parseInt( codeLengthValue.substring(l*2, l*2+2) );

			if( codeLengths.length > 0 )
				codeField = Schema.createPartyCategoryCodeField( codeLengths );
		}
		instanceMap.put( gln, codeField );

		return codeField;
	}

	@Override
	public boolean regist( Map<String, Object> recordMap ) throws DataException, SQLException {
		String gln = null;
		HierarchyCodeField codeField = null;

		gln = Record.extractString( recordMap, "gln" );
		if( gln != null ) codeField = getCodeField( gln );
		if( codeField == null ) return false;

		try {
			String code = (String)codeField.extractValue( recordMap );
			int level = codeField.getLevel( code );
			if( level > 1 ) {
				String[] codes = codeField.getUpperLevelCodes( code );

				StringBuffer sbuf = new StringBuffer( "SELECT CATE_CD FROM ECS_PARTY_CATE WHERE GLN = ? AND CLASSCD = ? AND CATE_CD IN (" );
				for( int i = 0; i < codes.length; i++ ) {
					if( i > 0 ) sbuf.append( "," );
					sbuf.append( "?" );
				}
				sbuf.append( ") FOR UPDATE NOWAIT" );

				PreparedStatement pstmt = handler.getConnection().prepareStatement( sbuf.toString() );
				ResultSet rset = null;
				try {
					pstmt.setString( 1, gln );
					pstmt.setString( 2, String.valueOf(level-1) );
					SQLManager.bindVariables( pstmt, codes, 3 );

					rset = pstmt.executeQuery();
					if( !rset.next() )
						throw handler.createDataException( DataException.ERR_NO_UPPERLEVELCODE, recordMap );
				} finally {
					try { rset.close(); } catch( Exception ex ) {}
					try { pstmt.close(); } catch( Exception ex ) {}
				}
			}
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		}

		return super.regist( recordMap );
	}

	@Override
	public DataResult registAll( Collection<Map<String, Object>> records ) throws SQLException {
		return registEach( records );
	}

	public boolean usePartyCategory( String gln ) throws SQLException {
		return ( getCodeField(gln) != null );
	}

	@Override
	public void validate( Map<String, ? extends Object> recordMap, boolean inserting ) throws DataException {
		try {
			if( inserting ) {
				String gln = Record.extractString( recordMap, "gln" );
				if( gln != null ) {
					try {
						HierarchyCodeField codeField = getCodeField( gln );
						if( codeField != null )
							codeField.validate( recordMap );
					} catch( SQLException sqlEx ) {}
				}
			}

			table.validate( recordMap, inserting );
		} catch( FieldException fieldEx ) {
			throw handler.createDataException( fieldEx, recordMap );
		}
	}
}
