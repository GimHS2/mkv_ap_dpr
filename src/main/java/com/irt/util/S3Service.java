/*
 *	File Name:	S3Service.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2020/10/30		2.0.0	create
 *
**/

package com.irt.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
 *
 */
public class S3Service {
	public static S3Service s3Instance;
	private AmazonS3 s3Client;
	private String region;
	private String accessKey;
	private String secretKey;
	private String bucket;
	private String defaultPath;

	public S3Service( String region, String bucket ) {
		this( null, null, region, bucket );
	}

	public S3Service( String accessKey, String secretKey, String region, String bucket ) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.region = region;
		this.bucket = bucket;
		init();
	}

	public void download( OutputStream outputStream, String fileName ) throws IOException, AmazonServiceException {
		download( outputStream, defaultPath, fileName );
	}

	public void download( OutputStream outputStream, String path, String fileName ) throws IOException, AmazonServiceException {
		String keyName = getFixPrePath( path );
		keyName += fileName;

		S3Object s3Obj = s3Client.getObject( bucket, keyName );
		S3ObjectInputStream s3InputStream = null;
		try {
			s3InputStream = s3Obj.getObjectContent();
			int length;
			byte[] buffer = new byte[1024 * 10];
			while( (length = s3InputStream.read(buffer, 0, buffer.length)) != -1 )
				outputStream.write( buffer, 0, length );
		} finally {
			if( s3InputStream != null ) try { s3InputStream.close(); } catch( Exception ignored ) {}
		}
	}

	public boolean existFile( String fileName ) {
		return existFile( defaultPath, fileName );
	}

	public boolean existFile( String path, String fileName ) {
		String keyName = getFixPrePath( path );
		keyName += fileName;
		try {
			S3Object obj = s3Client.getObject( bucket, keyName );
			return obj != null;
		} catch( AmazonS3Exception asEx ) {
			return false;
		}
	}

	public String getDefaultPath() {
		return defaultPath;
	}

	public String getUrl( String fileName ) {
		return getUrl( defaultPath, fileName );
	}

	public String getUrl( String path, String fileName ) {
		String keyName = getFixPrePath( path );
		keyName += fileName;

		return s3Client.getUrl( bucket, keyName ).toString();
	}

	public void init() throws AmazonServiceException, SdkClientException {
		AWSCredentialsProvider credentialsProvider = null;
		if( accessKey != null && secretKey != null ) {
			credentialsProvider = new AWSStaticCredentialsProvider( new BasicAWSCredentials(accessKey, secretKey) );
		}
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withRegion( region );
		if( credentialsProvider != null ) {
			builder.setCredentials( credentialsProvider );
		}
		s3Client = builder.build();
		s3Client.getS3AccountOwner();
	}

	public void setDefaultPath( String defaultPath ) {
		this.defaultPath = getFixPrePath( defaultPath );
	}

	public String getFixPrePath( String path ) {
		String keyName = path;
		if( keyName != null ) {
			if( keyName.startsWith("/") ) {
				keyName = keyName.substring( 1 );
			}
			if( !keyName.endsWith("/") ) {
				keyName += "/";
			}
		} else {
			keyName = "";
		}
		return keyName;
	}

	public PutObjectResult upload( File file ) throws AmazonServiceException {
		return upload( defaultPath, file.getName(), file, false );
	}

	public PutObjectResult upload( File file, boolean isPublic ) throws AmazonServiceException {
		return upload( defaultPath, file.getName(), file, isPublic );
	}

	public PutObjectResult upload( File file, String fileName ) throws AmazonServiceException {
		return upload( defaultPath, fileName, file, false );
	}

	public PutObjectResult upload( File file, String fileName, boolean isPublic ) throws AmazonServiceException {
		return upload( defaultPath, fileName, file, isPublic );
	}

	public PutObjectResult upload( String path, File file ) throws AmazonServiceException {
		return upload( path, file.getName(), file, false );
	}

	public PutObjectResult upload( String path, File file, boolean isPublic ) throws AmazonServiceException {
		return upload( path, file.getName(), file, isPublic );
	}

	public PutObjectResult upload( String path, String fileName, File file ) throws AmazonServiceException {
		return upload( path, fileName, file, false );
	}

	public PutObjectResult upload( String path, String fileName, File file, boolean isPublic ) throws AmazonServiceException {
		String keyName = getFixPrePath( path );
		keyName += fileName;

		if( isPublic ) {
			return s3Client.putObject( new PutObjectRequest(bucket, keyName, file)
					.withCannedAcl(CannedAccessControlList.PublicRead) );
		} else {
			return s3Client.putObject( bucket, keyName, file );
		}
	}
}
