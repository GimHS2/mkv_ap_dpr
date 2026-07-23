/*
 *	File Name:	FTPProcess.java
 *	Version:	2.2.6
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2022/11/30		2.2.6	Runner가 한번 실행될때 FTP서버에 한번만 로그인 하도록 수정(기존에는 파일별로 로그인->전송 함)
 *	jbaek		2022/06/30		2.2.5	리모트 파일 변경 체크를 위한 changeCheckSec, dataTimeoutSec, limitFileCount 추가
 *	GimHS		2021/06/30		2.2.4	prefixKeys 처리 로직 추가
 *	chan1914	2020/04/30		2.2.3	receivingDirectory configuration 추가
 *										executeFTP(), executeSFTP() : 파일이 다운로드 되는 중간에 처리되는 것을 방지하기 위해, "receivingDirectory" Directory에  다운로드 받고, 파일을 처리하는 Directory로 이동
 *	GimHS		2018/08/31		2.2.2	중요하지 않는 로그는 debug로 출력
 *	GimHS		2016/12/30		2.2.1	FTP용(not SFTP) configuration 추가(controlEncoding, defaultDateFormatStr, recentDateFormatStr 등)
 *	hankalam	2016/12/30		2.2.1	파일 다운로드 기능 추가
 *	hankalam	2016/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

import com.irt.util.FTP;
import com.irt.util.SFTP;

/**
 *
 */
public class FTPProcess implements Process {
	static final int DEFAULT_CHANGE_CHECK_SEC = 10;
	static final int DEFAULT_LIMIT_FILE_COUNT = 50;
	static final String UPLOAD		= "upload";
	static final String DOWNLOAD	= "download";

	protected FTP ftp;
	protected SFTP sftp;
	protected File file;
	protected String transportType, serverBackupDir, receivingDirectory;
	protected int limitFileCount, changeCheckSec, dataTimeoutSec;
	protected boolean executed;
	protected Logger logger;
	protected String[] prefixKeys;

	public FTPProcess( File file, FTP ftp, SFTP sftp, String transportType, String serverBackupDir, String receivingDirectory, String[] prefixKeys, Logger logger ) {
		this.executed = false;

		this.ftp = ftp;
		this.sftp = sftp;
		this.file = file;
		this.transportType = transportType;
		this.serverBackupDir = serverBackupDir;
		this.receivingDirectory = receivingDirectory;
		this.logger = logger;
		this.prefixKeys = null;
	}

	@Override
	public void close() {}

	@Override
	public boolean continueProcessing() {
		return executed;
	}

	@Override
	public boolean execute() throws InterruptedException {
		this.executed = false;
		try{
			logger.debug( getDescription() +" started." );
			if( sftp != null )
				executeSFTP();
			else
				executeFTP();

			logger.debug( getDescription() +" completed." );
			this.executed = true;
			return true;
		} catch( IOException ioEx ) {
			logger.error( getDescription() +" error. ", ioEx );
			return false;
		} catch( ProcessException processEx ) {
			logger.error( getDescription() +" error. ", processEx );
			return false;
		}
	}

	public void executeFTP() throws InterruptedException, IOException, ProcessException {
		if( ftp == null ) throw new ProcessException( "unable to connect(ftp is null)." );

		if( UPLOAD.equals(transportType) ) {
			try {
				ftp.storeFile( this.file );
			} catch( IOException ioEx ) {
				throw new ProcessException( "'" + this.file.getName() + "' upload failed. : " + ioEx );
			}
			logger.info( getDescription() + " : '" + this.file.getName() + "' upload completed.");
		} else {
			if( this.changeCheckSec > 0 )
				ftp.setChangeCheckSec( this.changeCheckSec );
			if( this.dataTimeoutSec > 0 )
				ftp.setDataTimeoutSec( this.dataTimeoutSec );
			String[] fileArr =  ftp.listFiles( this.limitFileCount );
			int successCount = 0, errorCount = 0;

			for( String fileName : fileArr ) {
				if( this.prefixKeys != null ) {
					boolean bool = false;
					for( int i = 0; i < this.prefixKeys.length; i++ ) {
						if( fileName.contains( this.prefixKeys[i]) )
							bool = true;
					}
					if( !bool )	continue;
				}
				if( fileName.endsWith(".tmp") || fileName.endsWith(".ing") ) continue;

				try {
					if( receivingDirectory != null && receivingDirectory.length() > 0 ) {
						// receiving Folder에서 Directory로 이동 (파일을 받는 도중에 처리되는 것을 방지하기 위해)
						ftp.retrieveFile( fileName, receivingDirectory );
						logger.info( getDescription() + " : '" + fileName + "' download completed.");

						File receivingFile = new File( receivingDirectory, fileName );
						receivingFile.renameTo( new File( file, fileName ) );
						logger.info( getDescription() + " : '" + fileName + "' receiving Folder move to directory Folder complete.");
					} else {
						ftp.retrieveFile( fileName, this.file.getAbsolutePath() );
						logger.info( getDescription() + " : '" + fileName + "' download completed.");
					}

					if( serverBackupDir != null && serverBackupDir.length() > 0 ) {
						StringBuffer newFileName = new StringBuffer();
						newFileName.append( serverBackupDir ).append( "/" );
						newFileName.append( org.apache.commons.io.FilenameUtils.getBaseName(fileName) );
						java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat( "yyyyMMddkkmmssS" );
						newFileName.append( "_" ).append( dateFormat.format(System.currentTimeMillis()) );

						String extension = org.apache.commons.io.FilenameUtils.getExtension( fileName );
						if( extension != null && extension.length() > 0 )
							newFileName.append( "." + extension );
						ftp.renameFile( fileName, newFileName.toString() );
					} else
						ftp.deleteFile( fileName );

					successCount++;
				} catch( IOException ioEx ) {
					logger.error( getDescription() + " : '" + fileName + "' download failed. : ", ioEx );
					errorCount++;
				}
			}
			if( errorCount > 0 )
				throw new ProcessException( errorCount + " files download failed." );

			if( successCount > 0 )
				logger.info( getDescription() + " : " + successCount + " files download completed.");
			else
				logger.debug( getDescription() + " : " + successCount + " files download completed.");
		}
	}

	public void executeSFTP() throws InterruptedException, IOException, ProcessException {
		if( sftp == null ) throw new ProcessException( "unable to connect(sftp is null)." );

		if( UPLOAD.equals(transportType) ) {
			try {
				sftp.storeFile( this.file );
			} catch( IOException ioEx ) {
				logger.trace( ioEx );
				throw new ProcessException( "'" + this.file.getName() + "' upload failed. : " + ioEx );
			}
			logger.info( getDescription() + " : '" + this.file.getName() + "' upload completed.");
		} else {
			if( this.changeCheckSec > 0 )
				sftp.setChangeCheckSec( this.changeCheckSec );
			if( this.dataTimeoutSec > 0 )
				sftp.setDataTimeoutSec( this.dataTimeoutSec );
			String[] fileArr =  sftp.listFiles( this.limitFileCount );
			int successCount = 0, errorCount = 0;

			for( String fileName : fileArr ) {
				if( this.prefixKeys != null ) {
					boolean bool = false;
					for( int i = 0; i < this.prefixKeys.length; i++ ) {
						if( fileName.contains( this.prefixKeys[i]) )
							bool = true;
					}
					if( !bool )	continue;
				}
				if( fileName.endsWith(".tmp") || fileName.endsWith(".ing") ) continue;

				try {
					if( receivingDirectory != null && receivingDirectory.length() > 0 ) {
						// receiving Folder에서 Directory로 이동 (파일을 받는 도중에 처리되는 것을 방지하기 위해)
						sftp.retrieveFile( fileName, receivingDirectory );
						logger.info( getDescription() + " : '" + fileName + "' download completed.");

						File receivingFile = new File( receivingDirectory, fileName );
						receivingFile.renameTo( new File( file, fileName ) );
						logger.info( getDescription() + " : '" + fileName + "' receiving Folder move to directory Folder complete.");
					} else {
						sftp.retrieveFile( fileName, this.file.getAbsolutePath() );
						logger.info( getDescription() + " : '" + fileName + "' download completed.");
					}

					if( serverBackupDir != null && serverBackupDir.length() > 0 ) {
						StringBuffer newFileName = new StringBuffer();
						newFileName.append( serverBackupDir ).append( "/" );
						newFileName.append( org.apache.commons.io.FilenameUtils.getBaseName(fileName) );
						java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat( "yyyyMMddkkmmssS" );
						newFileName.append( "_" ).append( dateFormat.format(System.currentTimeMillis()) );

						String extension = org.apache.commons.io.FilenameUtils.getExtension( fileName );
						if( extension != null && extension.length() > 0 )
							newFileName.append( "." + extension );

						sftp.renameFile( fileName, newFileName.toString() );
					} else
						sftp.deleteFile( fileName );

					successCount++;
				} catch( IOException ioEx ) {
					logger.error( getDescription() + " : '" + fileName + "' download failed. : ", ioEx );
					errorCount++;
				}
			}
			if( errorCount > 0 )
				throw new ProcessException( errorCount + " files download failed." );

			if( successCount > 0 )
				logger.info( getDescription() + " : " + successCount + " files download completed.");
			else
				logger.debug( getDescription() + " : " + successCount + " files download completed.");
		}
	}

	@Override
	public String getDescription() {
		return getProcessName() +" "+ transportType.toUpperCase();
	}

	@Override
	public String getProcessName() {
		return "FTPProcess";
	}
}

