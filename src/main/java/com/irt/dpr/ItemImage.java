/*
 *	File Name:	ItemImage.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	lsinji		2008/09/26		2.2.0	create
 *
 **/

package com.irt.dpr;

import com.irt.data.*;
import com.irt.data.Record;
import com.irt.sql.*;
import com.irt.util.Utility;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Map;

/*
 *
 */
public class ItemImage extends com.irt.rbm.ManipulableManagerImpl {
	public final static int IMAGESAVETYPE_BLOB			= 0x01;
	public final static int IMAGESAVETYPE_PLACE			= 0x02;

	public final static String DEFAULT_DISPLAYTYPE		= "FR";
	public final static String DEFAULT_IMAGEEXTENTION	= "gif";

	private final static Map<String, Object> imageTypes = java.util.Collections.unmodifiableMap( new java.util.HashMap<String, Object>()
	{{
		put( "image/pjpeg", "jpg" );
		put( "image/jpeg", "jpg" );
		put( "image/jpg", "jpg" );
		put( "image/gif", "gif" );
		put( "image/png", "png" );
	}} );
	/* */
	public final static String[] VALID_IMAGETYPES		= new String[] { "gif", "jpeg", "jpg", "pjpeg" };

	private final static Joinable joinable_MST = new JoinableImplTBK( "MST"
			, "(SELECT ITM.COUNTRYCD, ITM.ORGANIZATIONCD, ITM.ITEMCD"
					+ " FROM DPR_ITEM ITM"
					+ " WHERE COUNTRYCD = ? AND ORGANIZATIONCD = ?"
					+ " GROUP BY ITM.COUNTRYCD, ITM.ORGANIZATIONCD, ITM.ITEMCD)", null, new String[] {"countryCode", "organizationCode"} );

	private final static Table table = Schema.findTable( Schema.DPR_ITEM_IMG );
	private final static QueryFactory factory = new QueryFactory( new QueryableImpl(joinable_MST) {{
		Joinable tbl_DESC = new JoinableImplBK( "IMTD", "DPR_ITEM_MASTER_DESC"
				, "IMTD.ITEMCD(+) = MST.ITEMCD AND IMTD.LANGCD(+) = ?", "displayLanguage" );
		Joinable tbl_IMT = new JoinableImpl( "IMT", "DPR_ITEM_MASTER", "IMT.ITEM_CD(+) = MST.ITEMCD" );
		Joinable tbl_IIMG = new JoinableImpl( "IIMG", "DPR_ITEM_IMG"
				, "IIMG.COUNTRYCD(+) = MST.COUNTRYCD AND IIMG.ORGANIZATIONCD(+) = MST.ORGANIZATIONCD AND IIMG.ITEMCD(+) = MST.ITEMCD" );

		append( new QueryableField[] {
				new QueryableFieldImpl( Schema.STRING, true, "itemCode", "MST.ITEMCD" )
				, new QueryableFieldImpl( Schema.STRING, true, "countryCode", "MST.COUNTRYCD" )
				, new QueryableFieldImpl( Schema.STRING, "itemName", "IMTD.ITEMNAME", tbl_DESC )
				, new QueryableFieldImpl( Schema.STRING, "additionalItemName", "IMTD.ITEMNAME_ADDITION", tbl_DESC )
				, new QueryableFieldImpl( Schema.STRING, "description", "IMTD.DESCRIPTION", tbl_DESC )
				, new QueryableFieldImpl( Schema.STRING, "additionalDescription", "IMTD.DESCRIPTION_ADDITION", tbl_DESC )
				, new QueryableFieldImpl( Schema.STRING, "countryName"
						, "(SELECT SB.COUNTRYNAME FROM DPR_COUNTRY SB WHERE SB.COUNTRY_CD = MST.COUNTRYCD)" )
				, new QueryableFieldImpl( Schema.STRING, "productCategory", "IMT.PCATECD", tbl_IMT )
				, new QueryableFieldImpl( Schema.STRING, "imageType", "IIMG.IMAGE_TYPE", tbl_IIMG )
				, new QueryableFieldImpl( Schema.STRING, "imageExtention", "IIMG.IMAGE_EXTENTION", tbl_IIMG )
				, new QueryableFieldImpl( Schema.STRING, "imageSize", "IIMG.IMAGE_SIZE", tbl_IIMG )
				, new QueryableFieldImpl( Schema.STRING, "imagePath", "IIMG.IMAGE_PATH", tbl_IIMG )
				, new QueryableFieldImpl( Schema.STRING, "imageFileName", "IIMG.IMAGE_FILENAME", tbl_IIMG )
				, new QueryableFieldImpl( Schema.STRING, "imageFileFullName"
						, "IIMG.IMAGE_FILENAME || NVL2(IIMG.IMAGE_EXTENTION,  '.' || LOWER(IIMG.IMAGE_EXTENTION), null )", tbl_IIMG )
				, new QueryableFieldImpl( Schema.STRING, "imageWidth", "IIMG.WIDTH", tbl_IIMG )
				, new QueryableFieldImpl( Schema.STRING, "imageHeight", "IIMG.HEIGHT", tbl_IIMG )
				, new QueryableFieldImpl( Schema.STRING, "imageRegistered" , "NVL2(IIMG.ITEMCD, DECODE(IIMG.STATUS, '00','Y', 'N'), 'N')", tbl_IIMG )
				, new QueryableFieldImpl( Schema.STRING, "imageFileExisting" , "NVL2(IIMG.IMAGE_FILENAME, 'Y', 'N')", tbl_IIMG )
				, new QueryableFieldImpl( Schema.STRING, "imageRegisterDate" , "IIMG.UPGDATE", tbl_IIMG )
		} );

		append( new QueryableFieldImpl( Schema.STRING, "displayType", "IIMG.DISPLAY_TYPE", tbl_IIMG ) {
			@Override
			public boolean appendCondition( ConditionQueryBuffer querybuf ) {
				boolean hasCondition = appendCondition( querybuf, getDataType(), getFieldKey(), getQuery() + "(+)" );
				Joinable joinable = getJoinable();
				if( hasCondition && joinable != null ) joinable.appendTable( querybuf );

				return hasCondition;
			}
		} );
	}} );

	public ItemImage( SQLHandler handler ) {
		super( handler, table, factory );
	}

	public static String checkContentTypeAndGetFileExtention( String fileContentType ) throws SQLException {
		if( fileContentType == null ) return null;

		return (String)imageTypes.get(fileContentType);
	}

	public boolean checkExistingImage( Map<String, Object> primaryMap ) throws SQLException {
		Map<String, Object> record = getRecord( primaryMap );

		if( record == null )
			return false;
		else {
			String imageFileName = getStoredImageFileName( record );

			boolean existing = false;
			try {
				File file = new File( imageFileName );
				existing = file.exists();
			} catch( SecurityException securityEx ) {
				return false;
			}

			return existing;
		}
	}

	public static Map<String, Object> createPrimary( String countryCode, String organizationCode, String itemCode, String displayType ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();
		primaryMap.put( "countryCode", countryCode );
		primaryMap.put( "organizationCode", organizationCode );
		primaryMap.put( "itemCode", itemCode );
		primaryMap.put( "displayType", displayType );

		return primaryMap;
	}

	public boolean deleteImage( Map<String, Object> primaryMap ) throws SQLException, DataException {
		Map<String, Object> record = getRecord( primaryMap );
		if( record == null )
			throw handler.createDataException( DataException.ERR_NO_RECORD_DELETE, (String)null );

		String imageFileName = getStoredImageFileName( record );
		File imageFile = new File( imageFileName );
		try {
			if( imageFile.exists() )
				imageFile.delete();
		} catch( SecurityException securityEx ) {
			throw handler.createDataException( DataException.ERR_ERROR, (String)null );
		}

		return delete( primaryMap );
	}

	public static Map<String, Object> extractPrimaryMap( Map<String, Object> conditionMap ) {
		Map<String, Object>primaryMap = new java.util.HashMap<String, Object> ();

		return createPrimary( (String)conditionMap.get("countryCode"), (String)conditionMap.get("organizationCode"), (String)conditionMap.get("itemCode"), (String)conditionMap.get("displayType") );
	}

	private String getDefaultImageFileName() {
		String defaultImagePath = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Image;defaultImagePath" );
		String defaultImageFileName = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Image;defaultImageName" );

		if( Utility.isSafeFile( defaultImagePath, defaultImageFileName) )
			throw new IllegalArgumentException( "Invalid default image path" );

		return defaultImagePath + java.io.File.separator + defaultImageFileName;
	}

	@Override
	protected QueryBuffer setPrimaryConditionQuery( ConditionQueryBuffer querybuf ) {
		return factory.setConditionQuery( querybuf );
	}

	private String getStoredImageFileName( Map<String, Object>recordMap ) {
		if( recordMap == null ) return null;

		return getStoredImageFileName( (String)recordMap.get("organizationCode"), (String)recordMap.get("itemCode"), (String)recordMap.get("displayType")
				, (String)recordMap.get("imageFilePath"), (String)recordMap.get("imageExtention") );
	}

	private String getStoredImageFileName( String organizationCode, String itemCode, String displayType, String imageFilePath, String imageExtention ) {
		if( imageFilePath == null )
			imageFilePath = com.irt.rbm.RBMSystem.getSystemEnv( "DPR", "Image;defaultImagePath" );
		if( displayType == null )
			displayType = DEFAULT_DISPLAYTYPE;
		if( imageExtention == null )
			imageExtention = DEFAULT_IMAGEEXTENTION;

		String imageFileName = organizationCode + "_"+ itemCode + "_" + displayType + "." + imageExtention.toLowerCase();

		if( Utility.isSafeFile(imageFilePath, imageFileName) )
			throw new IllegalArgumentException( "Invalid stored image file name." );

		return imageFilePath + java.io.File.separator + imageFileName;
	}

	public boolean update( Map<String, Object> recordMap ) throws IOException, SQLException, DataException {
		if( recordMap == null )
			return false;

		String imageFileName = getStoredImageFileName( recordMap );
		File imageFile = new File( imageFileName );
		try {
			if( !imageFile.canWrite() ) {
				if( imageFile.exists() )
					deleteImage( recordMap );
			}

			Object inputImageObj = recordMap.get( "inputImageFile" );
			if( !(inputImageObj instanceof File) || !Utility.isValidateFile((File)inputImageObj) )
				throw new IllegalArgumentException( "Invalid input image file." );

			FileInputStream inputStream = new FileInputStream( (File)inputImageObj );
			FileOutputStream outputStream = new FileOutputStream( imageFile );
			try {
				writeFile( inputStream, outputStream );
			} finally {
				try {
					inputStream.close();
					outputStream.close();
				} catch( Exception ex ) {}
			}
		} catch( java.io.FileNotFoundException fileEx ) {
			throw handler.createDataException( DataException.ERR_ERROR, (String)null );
		} catch( SecurityException securityEx ) {
			throw handler.createDataException( DataException.ERR_ERROR, (String)null );
		}

		return SQLManager.manageRecord( handler, table, recordMap, Record.INSERT | Record.UPDATE );
	}

	public boolean writeFile( Map<String, Object>primaryMap, java.io.OutputStream outputStream ) throws IOException, SQLException {
		Connection conn = handler.getConnection();
		PreparedStatement pstmt = null;
		ResultSet rset = null;

		try {
			/* BLOB 미처리 */
			pstmt = conn.prepareStatement( "SELECT IMAGE_PATH, IMAGE_FILENAME, IMAGE_EXTENTION FROM DPR_ITEM_IMG"
					+ " WHERE COUNTRYCD = ? AND ORGANIZATIONCD = ? AND ITEMCD = ? AND DISPLAY_TYPE = ?" );

			pstmt.setString( 1, (String)primaryMap.get("countryCode") );
			pstmt.setString( 2, (String)primaryMap.get("organizationCode") );
			pstmt.setString( 3, (String)primaryMap.get("itemCode") );
			pstmt.setString( 4, (String)primaryMap.get("displayType") );
			rset = pstmt.executeQuery();
			if( rset.next() ) {
				Object imagePath = rset.getObject(1);
				Object imageFileName = rset.getObject(2);
				Object imageExtention = rset.getObject(3);

				Map<String, Object> recordMap = new java.util.HashMap<String, Object> ( primaryMap );
				recordMap.put( "imagePath", imagePath );
				recordMap.put( "imageExtention", imageExtention );

				String imageFileFullName = getStoredImageFileName( recordMap );
				File imageFile = new File( imageFileFullName );
				try {
					if( !imageFile.exists() )
						imageFile = new File( getDefaultImageFileName() );
				} catch( SecurityException securityEx ) {
					imageFile = new File( getDefaultImageFileName() );
				}

				java.io.InputStream inputStream = new FileInputStream( imageFile );
				try {
					writeFile( inputStream, outputStream );
				} finally {
					try { inputStream.close(); } catch( Exception ex ) {}
				}

				return true;
			} else {
				java.io.InputStream inputStream = new FileInputStream( new File(getDefaultImageFileName()) );
				try {
					writeFile( inputStream, outputStream );
				} finally {
					try { inputStream.close(); } catch( Exception ex ) {}
				}
			}
		} catch( java.io.FileNotFoundException fileEx ) {
			/* ERROR 처리 */
		} finally {
			try { if( rset != null ) rset.close(); } catch( Exception ex ) {}
			try { if( pstmt != null ) pstmt.close(); } catch( Exception ex ) {}
		}

		return false;
	}

	private void writeFile( java.io.InputStream inputStream, java.io.OutputStream outputStream ) throws IOException, SQLException {
		int length;
		byte buffer[] = new byte[ SQLManager.DEFAULT_BUFFER_SIZE ];

		while( (length = inputStream.read(buffer, 0, buffer.length)) != -1 )
			outputStream.write( buffer, 0, length );
		outputStream.flush();
	}

	/* JoinableImplTBK : Table에 BindVariable 해주는 Joinable */
	static class JoinableImplTBK implements Joinable {
		String alias, name, conditionQuery;
		String[] bindFieldKeys;

		Joinable joinable;

		public JoinableImplTBK( String alias, String name, String conditionQuery, String bindFieldKey ) {
			this( alias, name, conditionQuery, new String[] { bindFieldKey }, null );
		}

		public JoinableImplTBK( String alias, String name, String conditionQuery, String bindFieldKey, Joinable joinable ) {
			this( alias, name, conditionQuery, new String[] { bindFieldKey }, joinable );
		}

		public JoinableImplTBK( String alias, String name, String conditionQuery, String[] bindFieldKeys ) {
			this( alias, name, conditionQuery, bindFieldKeys, null );
		}

		public JoinableImplTBK( String alias, String name, String conditionQuery, String[] bindFieldKeys, Joinable joinable ) {
			this.alias = alias;
			this.name = name;
			this.conditionQuery = conditionQuery;
			this.bindFieldKeys = bindFieldKeys;
			this.joinable = joinable;
		}

		@Override
		public boolean appendTable( QueryBuffer querybuf ) {
			boolean done;
			if( conditionQuery != null )
				done = querybuf.appendTableWithAlias( name, alias, conditionQuery );
			else
				done = querybuf.appendTableWithAlias( name, alias );

			if( done ) {
				if( querybuf instanceof ConditionQueryBuffer ) {
					ConditionQueryBuffer condquerybuf = (ConditionQueryBuffer)querybuf;
					String countryCode = (String)condquerybuf.getConditionValue( "countryCode" );
					String organizationCode = (String)condquerybuf.getConditionValue( "organizationCode" );

					querybuf.addBindVariable( QueryBuffer.FROM_BINDVAR, countryCode );
					querybuf.addBindVariable( QueryBuffer.FROM_BINDVAR, organizationCode );
				} else
					querybuf.addBindVariable( QueryBuffer.FROM_BINDVAR, null );

				if( joinable != null ) joinable.appendTable( querybuf );
			}

			return done;
		}

		@Override
		public boolean existTable( QueryBuffer querybuf ) {
			return querybuf.existTableAlias( alias );
		}
	}
}
