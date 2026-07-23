/*
 *	File Name:	FTPProcessRunner.java
 *	Version:	2.2.6
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	GimHS		2022/11/30		2.2.7	Runner가 한번 실행될때 FTP서버에 한번만 로그인 하도록 수정(기존에는 파일별로 로그인->전송 함)
 *	jbaek		2022/06/30		2.2.6	리모트 파일 변경 체크를 위한 changeCheckSec, dataTimeoutSec, limitFileCount 추가
 *	GimHS		2021/06/30		2.2.5	prefixKeys 처리 로직 추가
 *	chan1914	2020/04/30		2.2.4	파일 다운로드 중간에 처리되는 것을 방지하기 위한 receivingDirectory 경로 Configuration 추가
 *	GimHS		2018/08/31		2.2.3	FTP가 실행될 시간 범위 Configuration 추가 (beginTime, endTime)
 *	hankalam	2017/07/31		2.2.2	excuteUpload(): 지정한 파일 수 단위로 처리하는 로직 추가
 *	GimHS		2016/12/30		2.2.1	FTP용(not SFTP) configuration 추가(controlEncoding, defaultDateFormatStr, recentDateFormatStr 등)
 *	hankalam	2016/12/30		2.2.1	파일 다운로드 기능 추가
 *	hankalam	2016/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.sql.SQLHandler;
import com.irt.system.SystemConfig;
import com.irt.util.FTP;
import com.irt.util.SFTP;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 */
public class FTPProcessRunner extends FileProcessRunner {
	String hostName, userId, password, receivingDirectory, serverBackupDir, serverErrorDir, serverPath, transportType;
	String controlEncoding, systemKey, defaultDateFormatStr, recentDateFormatStr, serverLanguageCode, serverTimeZoneId;
	int port, beginTime, endTime;
	boolean isSsh;
	Logger logger = getLogger();
	private int changeCheckSec, dataTimeoutSec;

	FTP ftp = null;
	SFTP sftp = null;

	public FTPProcessRunner( SystemConfig systemConfig, File directory ) {
		super( systemConfig, directory );
		this.isSsh = false;

		this.beginTime = -1;
		this.endTime = -1;
	}

	@Override
	public boolean execute() {
		String HHmm = (new java.text.SimpleDateFormat("HHmm")).format( new java.util.Date() );
		int currentTime = ( Integer.parseInt(HHmm.substring(0, 2)) * 60 + Integer.parseInt(HHmm.substring(2, 4)) );

		boolean executing = ( ( beginTime < 0 || currentTime >= beginTime ) && ( endTime < 0 || currentTime <= endTime ) );
		if( !executing ) return true;

		if( FTPProcess.DOWNLOAD.equals(transportType) ) return executeDownload();
		else if( FTPProcess.UPLOAD.equals(transportType) ) return executeUpload();

		logger.error( "Invalid transportType : upload or download" );
		return false;
	}

	public boolean executeDownload() {
		Process process;
		try {
			process = getProcessInstance( this.directory, logger );
		} catch( ProcessException processEx ) {
			logger.error( getDescription() +" error.", processEx.getCause() != null ? processEx.getCause() : processEx );
			return false;
		}

		if( process != ProcessRunner.EMPTY ) {
			process = new ProcessWrapper( process, "FTPProcess."+ transportType, "'"+ transportType +"'" ) {
				@Override
				public void close() {
					super.close();
				}

				@Override
				public boolean execute() throws InterruptedException {
					return process.execute();
				}
			};

			if( !execute(process) ) return false;
		}

		try {
			if( sftp != null && sftp.isConnected() ) {
				sftp.disconnect();
				logger.debug( getDescription() + " : " + sftp.getName() +" disconnected.");
				sftp = null;
			}
			sftp = null;
			if( ftp != null && ftp.isConnected() ) {
				ftp.disconnect();
				logger.debug( getDescription() + " : " + ftp.getName() +" disconnected.");
			}
			ftp = null;
		} catch( IOException ex ) {
			logger.warn( getDescription() +" error.", ex );
		}

		return true;
	}

	public boolean executeUpload() {
		File[] files;

		int count = 0;
		loop: {
			do {
				files = listFiles( directory );
				if( files == null || files.length == 0 ) return true;
				logger.info( getDescription() +" '"+ directory.getAbsolutePath() +"': "+ files.length +" files found." );

				for( File file : files ) {
					Process process;

					try {
						process = getProcessInstance( file, logger );
					} catch( ProcessException processEx ) {
						logger.error( getDescription() +" error.", processEx.getCause() != null ? processEx.getCause() : processEx );
						return false;
					}

					if( process == null ) {
						logger.info( "'"+ file.getName() +"' ignored." );
						closeProcessedFile( file, true );
					} else if( process != ProcessRunner.EMPTY ) {
						final File final_file = file;

						process = new ProcessWrapper( process,
								"FileProcess."+ file.getName(), "'"+ file.getName() +"'" ) {
							File file = final_file;

							@Override
							public void close() {
								super.close();
							}

							@Override
							public boolean execute() throws InterruptedException {
								try {
									return process.execute();
								} finally {
									closeProcessedFile( file, process.continueProcessing() );
								}
							}
						};

						if( !execute(process) ) return false;
					}

					count++;
					if( isInterrupted() ) break loop;
				}

				logger.info( getDescription() +" '"+ directory.getAbsolutePath() +"': "+ count +" files processed." );

				try {
					if( sftp != null && sftp.isConnected() ) {
						sftp.disconnect();
						logger.debug( getDescription() + " : " + sftp.getName() +" disconnected.");
						sftp = null;
					}
					sftp = null;
					if( ftp != null && ftp.isConnected() ) {
						ftp.disconnect();
						logger.debug( getDescription() + " : " + ftp.getName() +" disconnected.");
					}
					ftp = null;
				} catch( IOException ex ) {
					logger.warn( getDescription() +" error.", ex );
				}
			} while( files != null );
		}

		return true;
	}

	@Override
	public String getDescription() {
		return "FTPProcessRunner (host:"+ hostName +", id:"+ userId +", "+ (isSsh ? "SFTP" : "FTP") +") "+ transportType;
	}

	public Map<String, String> getFTPConfigMap() {
		Map<String, String> configMap = new java.util.HashMap<String, String>();

		if( controlEncoding != null && controlEncoding.length() > 0 )
			configMap.put( "controlEncoding", controlEncoding );
		if( systemKey != null && systemKey.length() > 0 )
			configMap.put( "systemKey", systemKey );
		if( defaultDateFormatStr != null && defaultDateFormatStr.length() > 0 )
			configMap.put( "defaultDateFormatStr", defaultDateFormatStr );
		if( recentDateFormatStr != null && recentDateFormatStr.length() > 0 )
			configMap.put( "recentDateFormatStr", recentDateFormatStr );
		if( serverLanguageCode != null && serverLanguageCode.length() > 0 )
			configMap.put( "serverLanguageCode", serverLanguageCode );
		if( serverTimeZoneId != null && serverTimeZoneId.length() > 0 )
			configMap.put( "serverTimeZoneId", serverTimeZoneId );

		return ( configMap.size() > 0 ? configMap : null );
	}

	@Override
	public Logger getLogger() {
		return Logger.getLogger( "com.irt.rbm.tools.FTPProcessRunner" );
	}

	@Override
	public Process getProcessInstance( SQLHandler handler, File file ) throws ProcessException {
		throw new ProcessException( "Does not supported." );
	}

	public Process getProcessInstance( File file, Logger logger ) throws ProcessException {
		if( this.isSsh && sftp == null ) {
			this.port = ( port == 0 ) ? SFTP.DEFAULT_PORT : this.port;

			sftp = new SFTP( this.hostName, this.port, this.userId, this.password, this.serverPath );

			try {
				if( sftp.connect() == null ) throw new ProcessException( "unable to connect." );
				logger.debug( getProcessName() + " : " + sftp.getName() + " connected.");
			} catch( IOException ex ) {
				throw new ProcessException( ex );
			}
		} else if( !this.isSsh && ftp == null ) {
			this.port = ( port == 0 ) ? FTP.DEFAULT_PORT : this.port;

			Map<String, String> configMap = getFTPConfigMap();
			if( configMap != null ) {
				configMap.put( "hostName", hostName );
				configMap.put( "port", String.valueOf(port) );
				configMap.put( "userId", userId );
				configMap.put( "password", password );
				configMap.put( "serverPath", serverPath );

				ftp = new FTP( configMap );
			} else
				ftp = new FTP( this.hostName, this.port, this.userId, this.password, this.serverPath );

			try {
				if( ftp.connect() == null ) throw new ProcessException( "unable to connect." );
				logger.debug( getProcessName() + " : " + ftp.getName() + " connected.");
			} catch( IOException ex ) {
				throw new ProcessException( ex );
			}
		}

		FTPProcess ftppro = new FTPProcess( file, ftp, sftp, transportType, serverBackupDir, receivingDirectory, prefixKeys, logger );

		if( this.dataTimeoutSec > 0 ) ftppro.dataTimeoutSec = this.dataTimeoutSec;
		if( FTPProcess.DOWNLOAD.equals(transportType) ) {
			ftppro.changeCheckSec = ( this.changeCheckSec <= 0 ? FTPProcess.DEFAULT_CHANGE_CHECK_SEC : this.changeCheckSec );
			ftppro.limitFileCount = ( this.limitFileCount <= 0 ? FTPProcess.DEFAULT_LIMIT_FILE_COUNT : this.limitFileCount );
		}

		return ftppro;
	}

	@Override
	public String getProcessName() {
		return "FTPProcessRunner";
	}

	public void setBeginTime( String HHmm ) {
		if( HHmm != null && HHmm.length() > 3 ) {
			if( HHmm.charAt(2) == ':' ) HHmm = HHmm.substring(0, 2) + HHmm.substring(3, 5);
			this.beginTime = ( Integer.parseInt(HHmm.substring(0, 2)) * 60 + Integer.parseInt(HHmm.substring(2, 4)) );
		}
	}

	public void setControlEncoding( String controlEncoding ) {
		this.controlEncoding = controlEncoding;
	}

	public void setChangeCheckSec( int changeCheckSec ) {
			this.changeCheckSec = changeCheckSec;
	}

	public void setDataTimeoutSec( int dataTimeoutSec ) {
			this.dataTimeoutSec = dataTimeoutSec;
	}

	public void setDefaultDateFormatStr( String defaultDateFormatStr ) {
		this.defaultDateFormatStr = defaultDateFormatStr;
	}

	public void setEndTime( String HHmm ) {
		if( HHmm != null && HHmm.length() > 3 ) {
			if( HHmm.charAt(2) == ':' ) HHmm = HHmm.substring(0, 2) + HHmm.substring(3, 5);
			this.endTime = ( Integer.parseInt(HHmm.substring(0, 2)) * 60 + Integer.parseInt(HHmm.substring(2, 4)) );
		}
	}

	public void setHostName( String hostName ) {
		this.hostName = hostName;
	}

	public void setIsSsh( boolean isSsh ) throws ProcessException {
		this.isSsh = isSsh;
	}

	public void setPassword( String password ) {
		this.password = password;
	}

	public void setPort( String port ) {
		this.port = Integer.parseInt( port );
	}

	public void setReceivingDirectory( String receivingDirectory ) {
		this.receivingDirectory = receivingDirectory;
	}

	public void setRecentDateFormatStr( String recentDateFormatStr ) {
		this.recentDateFormatStr = recentDateFormatStr;
	}

	public void setServerBackupDir( String serverBackupDir ) {
		this.serverBackupDir = serverBackupDir;
	}

	public void setServerLanguageCode( String serverLanguageCode ) {
		this.serverLanguageCode = serverLanguageCode;
	}

	public void setServerPath( String serverPath ) {
		this.serverPath = serverPath;
	}

	public void setServerTimeZoneId( String serverTimeZoneId ) {
		this.serverTimeZoneId = serverTimeZoneId;
	}

	public void setSystemKey( String systemKey ) {
		this.systemKey = systemKey;
	}

	public void setTransportType( String transportType ) throws ProcessException {
		if( !FTPProcess.UPLOAD.equals(transportType.toLowerCase()) && !FTPProcess.DOWNLOAD.equals(transportType.toLowerCase()) )
			throw new ProcessException( "Invalid transportType : upload or download" );
		this.transportType = transportType;
	}

	public void setUserId( String userId ) {
		this.userId = userId;
	}
}

