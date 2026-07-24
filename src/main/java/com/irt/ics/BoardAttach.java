/*
 *	File Name:	BoardAttach.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/11/30		2.2.1c	saveAttachFileToServer() 기능 추가.
 *	stghr12		2010/07/31		2.2.1	version up
 *	lsinji		2009/10/25		2.2.0	create
 *
**/

package com.irt.ics;

import com.irt.data.DataException;
import com.irt.data.Record;
import com.irt.sql.*;
import com.irt.util.Utility;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BoardAttach extends com.irt.rbm.ManipulableManagerImpl {
	public final static String ATTACHTYPE_IMAGE			= "IMG";
	public final static String ATTACHTYPE_FILE			= "FLE";

	private final static Table table = Schema.findTable( Schema.ICS_BOARD_ATTACH );
	private final static QueryFactory factory = Schema.findQueryFactory( Schema.ICS_BOARD_ATTACH );

	public BoardAttach( SQLHandler handler ) {
		super( handler, table, factory );
	}

	private boolean checkAndMakeAttachDirectory( File directoryFile ) throws SecurityException {
		if( !directoryFile.exists() )
			return directoryFile.mkdir();

		return true;
	}

	public boolean cleanNoUsedAttaches( String attachManageKey ) throws DataException, SQLException {
		return cleanNoUsedAttaches( attachManageKey, (int[])null );
	}

	public boolean cleanNoUsedAttaches( String attachManageKey, int... remainsAttachNumbers ) throws DataException, SQLException {
		QueryBuffer querybuf = new QueryBuffer();

		querybuf.append( "ATTACH_MNGKEY \"attachManageKey\", ATTACH_SEQID \"attachNumber\"" );
		querybuf.append( "FILE_TYPE \"fileType\", FILE_PATH \"filePath\", SERVER_FILENAME \"serverFileName\"" );
		querybuf.appendTable( "ICS_BOARD_ATTACH" );
		querybuf.appendCondition( "ATTACH_MNGKEY = ?", new Object[] { attachManageKey } );

		if( remainsAttachNumbers != null && remainsAttachNumbers.length > 0 ) {
			String conditionQuery = "";
			Object[] bindVars = new Object[ remainsAttachNumbers.length ];
			for( int i = 0; i < remainsAttachNumbers.length; i++ ) {
				conditionQuery += ", ?";
				bindVars[i] = Integer.valueOf( remainsAttachNumbers[i] );
			}

			querybuf.appendCondition( "ATTACH_SEQID NOT IN (" + conditionQuery.substring(2) + ")", bindVars );
		}

		List<Map<String, Object>> recordList = SQLManager.getRecordList( handler, querybuf );
		if( recordList != null ) {
			for( Map<String, Object> recordMap : recordList )
				delete( (String)recordMap.get("fileType"), recordMap );
		}

		return true;
	}

	public boolean cleanNoUsedAttaches( String attachManageKey, String... remainsAttachNumberStrings ) throws DataException, SQLException {
		int[] remainsAttachNumbers = null;

		if( remainsAttachNumberStrings != null ) {
			remainsAttachNumbers = new int[ remainsAttachNumberStrings.length ];

			int idx = 0;
			for( String remainsAttachNumberString : remainsAttachNumberStrings ) {
				try {
					remainsAttachNumbers[idx] = Integer.parseInt( remainsAttachNumberString );
					idx++;
				} catch( NumberFormatException numberEx ) {}
			}
			if( idx == 0 )
				remainsAttachNumbers = null;
			else {
				while( idx < remainsAttachNumberStrings.length )
					remainsAttachNumbers[idx++] = remainsAttachNumbers[0];
			}
		}

		return cleanNoUsedAttaches( attachManageKey, remainsAttachNumbers );
	}

	public static Map<String, Object> createPrimary( String attachManageKey, int attachNumber ) {
		Map<String, Object> primaryMap = new java.util.TreeMap<String, Object>();

		primaryMap.put( "attachManageKey", attachManageKey );
		primaryMap.put( "attachNumber", Integer.valueOf(attachNumber) );

		return primaryMap;
	}

	public int delete( String attachManageKey, String attachPath ) throws DataException, SQLException {
		File attachDir = new File( attachPath );
		int count = SQLManager.executeStatement( handler, "UPDATE ICS_BOARD_ATTACH SET STATUS = 'DE' WHERE ATTACH_MNGKEY = ?", attachManageKey );

		if( count > 0 && attachDir.exists() ) {
			try {
				final String final_attachManageKey = attachManageKey;

				File[] attachFiles = attachDir.listFiles( new java.io.FilenameFilter() {
					@Override
					public boolean accept( File dir, String name ) {
						return name.startsWith( final_attachManageKey );
					}
				} );

				if( attachFiles != null ) {
					for( File attachFile : attachFiles )
						attachFile.delete();
				}
			} catch( SecurityException securityEx ) {}
		}

		return count;
	}

	@Override
	public boolean delete( Map<String, Object> primaryMap ) throws DataException, SQLException {
		return delete( (String)getFieldValue(primaryMap, "fileType"), primaryMap );
	}

	private boolean delete( String attachType, Map<String, Object> primaryMap ) throws DataException, SQLException {
		if( super.delete(primaryMap) ) {
			String filePath = Record.extractString( primaryMap, "filePath" );
			String serverFileName = Record.extractString( primaryMap, "serverFileName" );

			if( !Utility.isSafeFile(filePath, serverFileName) )
				return false;

			File serverFile = new File( filePath, serverFileName );
			try {
				if( serverFile.exists() ) {
					try {
						serverFile.delete();
					} catch( SecurityException securityEx ) {
						throw handler.createDataException(
							DataException.ERR_ERROR
							, handler.getMessageHandler().getMessage( "ERR_ICS_BOARD_CANNOT_DELETE_ATTACH"+ (attachType != null ? "_"+ attachType : "") )
						);
					}
				}
			} catch( SecurityException securityEx ) {}

			return true;
		}

		return false;
	}

	public int getNextAttachNumber( String boardClassCode, int boardNumber ) throws SQLException {
		return SQLManager.getInt( handler,
			"SELECT MAX(ATTACH_SEQID) + 1 FROM ICS_BOARD_ATTACH WHERE BOARDCLASSCD = ? AND SEQID = ?"
		, boardClassCode, Integer.valueOf(boardNumber) );
	}

	public int getNextAttachNumberByManageKey( String attachManageKey ) throws SQLException {
		return SQLManager.getInt( handler,
			"SELECT MAX(ATTACH_SEQID) + 1 FROM ICS_BOARD_ATTACH WHERE ATTACH_MNGKEY = ?"
		, attachManageKey );
	}

	public static String getServerFileName( String attachManageKey, int attachNumber ) {
		return attachManageKey +"_"+ attachNumber;
	}

	public static String makeAttachManageKey( String boardClassCode, String uniqId ) {
		String concateString = boardClassCode + uniqId + System.currentTimeMillis();
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance( "MD5" );

			StringBuffer sbuf = new StringBuffer();
			for( byte b : md.digest(concateString.getBytes("UTF-8")) ) {
				String hexString = Integer.toHexString( b & 0xFF );
				sbuf.append( (hexString.length() < 2 ? "0" + hexString : hexString) );
			}

			return sbuf.toString();
		} catch( java.security.NoSuchAlgorithmException ignored ) {
			return null;
		} catch( java.io.UnsupportedEncodingException ignored ) {
			return null;
		}
	}

	public boolean regist( Map<String, Object> recordMap, File inputFile ) throws DataException, IOException, SQLException {
		if( super.regist(recordMap) ) {
			String attachType = Record.extractString( recordMap, "fileType" );
			String filePath = Record.extractString( recordMap, "filePath" );
			String serverFileName = Record.extractString( recordMap, "serverFileName" );

			try {
				if( saveAttachFileToServer( filePath, inputFile, serverFileName ) ) {
					return true;
				} else
					throw handler.createDataException(
						DataException.ERR_ERROR
						, handler.getMessageHandler().getMessage( "ERR_ICS_BOARD_CANNOT_REGIST_ATTACH"+ (attachType != null ? "_"+ attachType : "") )
					);
			} catch ( SecurityException securityEx ) {
				throw handler.createDataException(
					DataException.ERR_ERROR
					, handler.getMessageHandler().getMessage( "ERR_ICS_BOARD_CANNOT_REGIST_ATTACH"+ (attachType != null ? "_"+ attachType : "") )
				);
			}
		}

		return false;
	}

	public boolean saveAttachFileToServer( String fileSaveDirFullPath, File inputFile, String savingFileName ) throws IOException {
		if( !Utility.isSafeFile(fileSaveDirFullPath, savingFileName) )
			return false;

		if( checkAndMakeAttachDirectory(new File(fileSaveDirFullPath)) ) {
			File outputFile = new File( fileSaveDirFullPath, savingFileName);
			if( !inputFile.renameTo(outputFile) ) {
				java.io.InputStream inputStream = null;
				java.io.OutputStream outputStream = null;
				try {
					inputStream = new java.io.FileInputStream( inputFile );
					outputStream = new java.io.FileOutputStream( outputFile );
					{
						int length;
						byte buffer[] = new byte[ 10240 ];

						while( (length = inputStream.read(buffer, 0, buffer.length)) != -1 )
							outputStream.write( buffer, 0, length );
						outputStream.flush();
					}
				} finally {
					try { inputStream.close(); } catch( Exception ignored ) {}
					try { outputStream.close(); } catch( Exception ignored ) {}
				}
			}

			return true;
		}
		return false;
	}
}
