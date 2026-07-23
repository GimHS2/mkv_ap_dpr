/*
 *	File Name:	ItemFile.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *	stghr12		2007/04/30		2.1.0	create
 *
 **/

package com.irt.rbm.ecs;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

/**
 *
 */
public class ItemFile extends com.irt.rbm.QueryableManagerImpl {
	private final static int MODE_BFILE					= 0x01;
	private final static int MODE_BLOB					= 0x02;
	private final static int MODE_BLOB_AND_BFILE		= 0x03;

	private final static Table table_BF = Schema.findTable( Schema.ECS_ITEM_FILE_BFILE );
	private final static Table table_BL = Schema.findTable( Schema.ECS_ITEM_FILE_BLOB );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ECS_ITEM_FILE );

	private int mode;

	public ItemFile( SQLHandler handler ) {
		super( handler, factory );

		String manageType = com.irt.rbm.RBMSystem.getSystemEnv( "ECS", "ItemOption;ItemFileManageType" );
		if( "BLOB".equals(manageType) )
			mode = MODE_BLOB;
		else if( "BFILE".equals(manageType) )
			mode = MODE_BFILE;
		else
			mode = MODE_BLOB_AND_BFILE;
	}

	public static Map<String, Object> createPrimary( String gtin, String gln, String fileClassCode ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "gtin", gtin );
		primaryMap.put( "gln", gln );
		primaryMap.put( "fileClassCode", fileClassCode );

		return primaryMap;
	}

	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		boolean deleted;

		switch( mode ) {
		case MODE_BFILE:
			return SQLManager.manageRecord( handler, table_BF, primaryMap, Record.DELETE );
		case MODE_BLOB:
			return SQLManager.manageRecord( handler, table_BL, primaryMap, Record.DELETE );
		case MODE_BLOB_AND_BFILE:
		default:
			deleted = SQLManager.manageRecord( handler, table_BL, primaryMap, Record.DELETE );
			deleted = SQLManager.manageRecord( handler, table_BF, primaryMap, Record.DELETE ) | deleted;

			return deleted;
		}
	}

	public String getExternalFileName( Map<String, ? extends Object> recordMap ) throws SQLException {
		Object[] bindVars = Record.extractValues( recordMap, new String[] { "gtin", "gln", "fileClassCode", "fileType" } );
		return (String)SQLManager.getObjectValue( handler, "SELECT pkECSItem.fGetItemFileName( ?, ?, ?, ? ) FROM DUAL", bindVars );
	}

	public String getExternalFileName( String gtin, String gln, String fileClassCode, String fileType ) throws SQLException {
		Object[] bindVars = new String[] { gtin, gln, fileClassCode, fileType };
		return (String)SQLManager.getObjectValue( handler, "SELECT pkECSItem.fGetItemFileName( ?, ?, ?, ? ) FROM DUAL", bindVars );
	}

	public java.awt.Image getImage( String gtin, String gln, String fileClassCode ) throws IOException, SQLException {
		Connection conn = handler.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rset = null;

		try {
			switch( mode ) {
			case MODE_BLOB:
			case MODE_BLOB_AND_BFILE:
				pstmt = conn.prepareStatement( "SELECT DATA FROM ECS_ITEM_FILE_BLOB WHERE GTIN = ? AND GLN = ? AND FILECLASSCD = ?" );
				pstmt.setString( 1, gtin );
				pstmt.setString( 2, gln );
				pstmt.setString( 3, fileClassCode );
				rset = pstmt.executeQuery();
				if( rset.next() ) {
					java.io.InputStream inputStream = rset.getBinaryStream(1);
					try {
						return javax.imageio.ImageIO.read( inputStream );
					} finally {
						try { inputStream.close(); } catch( Exception ex ) {}
					}
				} else if( mode == MODE_BLOB )
					return null;
			case MODE_BFILE:
				pstmt = conn.prepareStatement( "SELECT DATA FROM ECS_ITEM_FILE_BFILE WHERE GTIN = ? AND GLN = ? AND FILECLASSCD = ?" );
				pstmt.setString( 1, gtin );
				pstmt.setString( 2, gln );
				pstmt.setString( 3, fileClassCode );
				rset = pstmt.executeQuery();
				if( rset.next() ) {
					oracle.sql.BFILE bfile = (oracle.sql.BFILE)rset.getObject(1);
					bfile.openFile();
					java.io.InputStream inputStream = bfile.getBinaryStream();

					try {
						return javax.imageio.ImageIO.read( inputStream );
					} finally {
						try { inputStream.close(); } catch( Exception ex ) {}
						bfile.closeFile();
					}
				}
			}

			return null;
		} finally {
			try { if( rset != null ) rset.close(); } catch( Exception ex ) {}
			try { if( pstmt != null ) pstmt.close(); } catch( Exception ex ) {}
		}
	}

	public boolean modify( Map<String, Object> recordMap ) throws DataException, IOException, SQLException {
		if( mode != MODE_BFILE ) throw new UnsupportedOperationException();
		if( SQLManager.manageRecord( handler, table_BF, recordMap, Record.UPDATE ) )
			return updateFile( recordMap, null );

		return false;
	}

	public boolean modify( Map<String, Object> recordMap, java.io.InputStream inputStream ) throws DataException, IOException, SQLException {
		if( mode == MODE_BFILE ) throw new UnsupportedOperationException();
		if( SQLManager.manageRecord( handler, table_BL, recordMap, Record.UPDATE ) )
			return updateFile( recordMap, inputStream );

		return false;
	}

	public boolean regist( Map<String, Object> recordMap ) throws DataException, IOException, SQLException {
		if( mode != MODE_BFILE ) throw new UnsupportedOperationException();
		if( SQLManager.manageRecord( handler, table_BF, recordMap, Record.INSERT ) )
			return updateFile( recordMap, null );

		return false;
	}

	public boolean regist( Map<String, Object> recordMap, java.io.InputStream inputStream ) throws DataException, IOException, SQLException {
		if( mode == MODE_BFILE ) throw new UnsupportedOperationException();
		if( SQLManager.manageRecord( handler, table_BL, recordMap, Record.INSERT ) )
			return updateFile( recordMap, inputStream );

		return false;
	}

	@Override
	protected QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) {
		try {
			return table_BF.setPrimaryConditionQuery( querybuf );
		} catch( FieldException fieldEx ) {
			return null;
		}
	}

	private boolean updateFile( Map<String, Object> recordMap, java.io.InputStream inputStream ) throws DataException, IOException, SQLException {
		if( mode == MODE_BFILE ) {
			PreparedStatement pstmt = handler.getConnection().prepareStatement(
					"UPDATE ECS_ITEM_FILE_BFILE "
							+" SET DATA = BFILENAME( 'DIR_ECSITEMFILE', NVL(?, pkECSItem.fGetItemFileName(GTIN, GLN, FILECLASSCD, FILETYPE)) )"
							+" WHERE GTIN = ? AND GLN = ? AND FILECLASSCD = ?"
					);
			try {
				Object externalFileName = Record.extractValue( recordMap, "externalFileName" );
				if( externalFileName == null )
					pstmt.setNull( 1, Types.VARCHAR );
				else
					pstmt.setObject( 1, externalFileName );
				SQLManager.bindVariables( pstmt, table_BF.extractPrimaryValues(recordMap), 2 );
				pstmt.executeUpdate();
			} catch( FieldException fieldEx ) {
				throw handler.createDataException( fieldEx, recordMap );
			} finally {
				try { pstmt.close(); } catch( Exception ex ) {}
			}
		} else {
			long filesize = 0;
			Object[] primaryValues;
			try {
				primaryValues = table_BF.extractPrimaryValues( recordMap );
			} catch( FieldException fieldEx ) {
				throw handler.createDataException( fieldEx, recordMap );
			}

			SQLManager.executeStatement( handler,
					"UPDATE ECS_ITEM_FILE_BLOB SET DATA = EMPTY_BLOB() WHERE GTIN = ? AND GLN = ? AND FILECLASSCD = ?"
					, primaryValues );

			Connection conn = handler.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(
					"SELECT IFL.DATA, FLC.LIMITSIZE FROM ECS_ITEM_FILE_BLOB IFL, ECS_FILECLASS FLC"
							+" WHERE IFL.GTIN = ? AND IFL.GLN = ? AND IFL.FILECLASSCD = ? AND FLC.FILECLASS_CD(+) = IFL.FILECLASSCD"
					);
			ResultSet rset = null;
			try {
				SQLManager.bindVariables( pstmt, primaryValues );
				rset = pstmt.executeQuery();
				if( rset.next() ) {
					filesize = SQLManager.fillBlob( handler, rset.getBlob(1), inputStream );
					if( filesize > rset.getLong(2) * 1024 )
						throw handler.createDataException( DataException.ERR_LARGE_FILESIZE, recordMap );
				}
			} finally {
				try { if( rset != null ) rset.close(); } catch( Exception ex ) {}
				try { pstmt.close(); } catch( Exception ex ) {}
			}

			pstmt = conn.prepareStatement( "UPDATE ECS_ITEM_FILE_BLOB SET FILESIZE = ? WHERE GTIN = ? AND GLN = ? AND FILECLASSCD = ?" );
			try {
				pstmt.setLong( 1, filesize );
				pstmt.setObject( 2, primaryValues[0] );
				pstmt.setObject( 3, primaryValues[1] );
				pstmt.setObject( 4, primaryValues[2] );
				pstmt.executeUpdate();
			} finally {
				try { pstmt.close(); } catch( Exception ex ) {}
			}
		}

		return true;
	}

	public boolean updateStatus( String gtin, String gln, String fileClassCode, String status ) throws DataException, SQLException {
		switch( mode ) {
		case MODE_BLOB:
		case MODE_BLOB_AND_BFILE:
			int resultCount = SQLManager.executeStatement( handler,
					"UPDATE ECS_ITEM_FILE_BLOB SET STATUS = ?, UPGDATE = SYSDATE WHERE GTIN = ? AND GLN = ? AND FILECLASSCD = ?"
					, status, gtin, gln, fileClassCode );
			if( resultCount > 0 || mode == MODE_BLOB )
				return ( resultCount > 0 );
		case MODE_BFILE:
			return( SQLManager.executeStatement( handler,
					"UPDATE ECS_ITEM_FILE_BFILE SET STATUS = ?, UPGDATE = SYSDATE WHERE GTIN = ? AND GLN = ? AND FILECLASSCD = ?"
					, status, gtin, gln, fileClassCode ) > 0 );
		}

		return false;
	}

	public boolean useBLOBType() {
		return ( mode != MODE_BFILE );
	}

	public boolean writeFile( Map<String, ? extends Object> primaryMap, java.io.OutputStream outputStream ) throws IOException, SQLException {
		try {
			Object[] primaryValues = table_BF.extractPrimaryValues( primaryMap );
			return writeFile( (String)primaryValues[0], (String)primaryValues[1], (String)primaryValues[2], outputStream );
		} catch( FieldException fieldEx ) {
			return false;
		}
	}

	public boolean writeFile( String gtin, String gln, String fileClassCode, java.io.OutputStream outputStream ) throws IOException, SQLException {
		Connection conn = handler.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rset = null;

		try {
			switch( mode ) {
			case MODE_BLOB:
			case MODE_BLOB_AND_BFILE:
				pstmt = conn.prepareStatement( "SELECT DATA FROM ECS_ITEM_FILE_BLOB WHERE GTIN = ? AND GLN = ? AND FILECLASSCD = ?" );
				pstmt.setString( 1, gtin );
				pstmt.setString( 2, gln );
				pstmt.setString( 3, fileClassCode );
				rset = pstmt.executeQuery();
				if( rset.next() ) {
					java.io.InputStream inputStream = rset.getBinaryStream(1);
					try {
						writeFile( inputStream, outputStream );
					} finally {
						try { inputStream.close(); } catch( Exception ex ) {}
					}

					return true;
				} else if( mode == MODE_BLOB )
					return false;
			case MODE_BFILE:
				pstmt = conn.prepareStatement( "SELECT DATA FROM ECS_ITEM_FILE_BFILE WHERE GTIN = ? AND GLN = ? AND FILECLASSCD = ?" );
				pstmt.setString( 1, gtin );
				pstmt.setString( 2, gln );
				pstmt.setString( 3, fileClassCode );
				rset = pstmt.executeQuery();
				if( rset.next() ) {
					oracle.sql.BFILE bfile = (oracle.sql.BFILE)rset.getObject(1);
					bfile.openFile();
					java.io.InputStream inputStream = bfile.getBinaryStream();

					try {
						writeFile( inputStream, outputStream );
					} finally {
						try { inputStream.close(); } catch( Exception ex ) {}
						bfile.closeFile();
					}

					return true;
				}
			}

			return false;
		} finally {
			try { if( rset != null ) rset.close(); } catch( Exception ex ) {}
			try { if( pstmt != null ) pstmt.close(); } catch( Exception ex ) {}
		}
	}

	private void writeFile( java.io.InputStream inputStream, java.io.OutputStream outputStream ) throws IOException, SQLException {
		int length;
		byte buffer[] = new byte[ SQLManager.DEFAULT_BUFFER_SIZE ];

		while( (length = inputStream.read(buffer, 0, buffer.length)) != -1 )
			outputStream.write( buffer, 0, length );
		outputStream.flush();
	}
}
