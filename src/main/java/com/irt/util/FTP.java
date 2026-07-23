/*
 *	File Name:	FTP.java
 *	Version:	2.2.5
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2022/06/30		2.2.5	리모트 파일 변경 체크를 위한 changeCheckSec, dataTimeoutSec, limitFileCount 추가
 *	GimHS		2021/06/30		2.2.4	listFiles(): 임시 파일(*.ing, *.tmp)은 제외 처리
 *	GimHS		2019/08/30		2.2.3	listFiles(): null point 에러 수정
 *	GimHS		2016/12/30		2.2.2	FTP용(not SFTP) configuration 추가(controlEncoding, defaultDateFormatStr, recentDateFormatStr 등)
 *										FTP 서버의 파일을 control할때 파일 이름에 encoding 처리
 *										listFiles(): 파일 사이즈를 체크하여 변경중인 파일은 제외하는 로직 추가
 *										getFTPFile(), getNewFTPClientConfig(), setClientConfig() 함수 추가
 *	hankalam	2016/12/30		2.2.2	deleteFile(), listFiles(), renameFile(), retrieveFile(), storeFile() 기능 추가
 *										throw 수정, connenct(): PassiveMode 옵션 추가
 *	hankalam	2016/03/31		2.2.1	에러메세지 세분화
 *	stghr12		2011/06/30		2.2.0	create
 *
**/

package com.irt.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.net.ftp.*;

/**
 *
 */
public class FTP {
	public final static int DEFAULT_PORT = 21;
	public final static int DEFAULT_LIMIT_FILE_COUNT = 100;
	private final static String REGEX_TAB_CONT_PLUS_LAST = "\\\t(?=[^\t]*$)"; // 'x	y	z' -> [x	y][z]
	private int changeCheckSec = 10;

	String controlEncoding;
	String hostName, userId, password, serverPath;
	int port, dataTimeoutSec;

	private FTPClient client = null;
	private FTPClientConfig clientConfig = null;

	public FTP( String hostName, String userId, String password ) {
		this( hostName, DEFAULT_PORT, userId, password, null );
	}

	public FTP( String hostName, int port, String userId, String password ) {
		this( hostName, port, userId, password, null );
	}

	public FTP( String hostName, int port, String userId, String password, String serverPath ) {
		this.hostName = hostName;
		this.port = port;
		this.userId = userId;
		this.password = password;
		this.serverPath = serverPath;

		this.controlEncoding = null;
	}

	/**
	 * 생성자 함수(FTP 설정값을 Map 형태로 받음).<br>
	 * <ul>
	 * <li> hostName : FTP 주소
	 * <li> port : FTP 포트 (default : 21)
	 * <li> userId : FTP 계정 ID
	 * <li> password : FTP 계정 비번
	 * <li> serverPath : FTP 홈 경로
	 * <li> controlEncoding : Control Connection의 인코딩 (보통 ftp 커맨드의 화면 인코딩 eg. UTF-8 or EUC-KR)
	 * <li> systemKey : FTP 서버의 시스템 종류 (UNIX, WINDOWS etc, {@link org.apache.commons.net.ftp.FTPClientConfig} 참고, 보통 자동 감지함)
	 * <li> defaultDateFormatStr : 날짜 포맷 (날짜 값 parsing 시 사용, {@link java.util.text.SimpleDateFormat} 참고, default는 en_US를 따름(MM dd yyyy), 한글 서버의 경우 M월 d일 yyyy)
	 * <li> recentDateFormatStr : 최근 날짜 포맷 (최근 날짜 값 parsing 시 사용, {@link java.util.text.SimpleDateFormat} 참고, default는 en_US를 따름(MM dd HH:mm), 한글 서버의 경우 M월 d일 HH:mm)
	 * <li> serverLanguageCode : FTP 서버의 LANG(언어, eg. ko or en)
	 * <li> serverTimeZoneId : FTP 서버의 타임존 ({@link java.util.TimeZone} 참고 eg. America/Denver eg. Asia/Seoul)
	 * </ul>
	 * @param configMap FTP 설정값.
	 */
	public FTP( Map<String, String> configMap ) {
		this( configMap.get("hostName"), Integer.parseInt(configMap.get("port")), configMap.get("userId"), configMap.get("password")
				, configMap.get("serverPath") );

		this.controlEncoding = configMap.get( "controlEncoding" );
		this.clientConfig = getNewFTPClientConfig( configMap );
	}

	public FTPClient connect() throws IOException {
		if( client != null ) throw new IOException( "already connected." );

		client = new FTPClient();

		if( this.dataTimeoutSec > 0 )
			client.setDataTimeout( dataTimeoutSec*1000 );

		client.connect( hostName, port );
		if( !FTPReply.isPositiveCompletion(client.getReplyCode()) )
			throw new IOException( "refused connection." );

		client.enterLocalPassiveMode();

		client.login( userId, password );
		if( !FTPReply.isPositiveCompletion(client.getReplyCode()) )
			throw new IOException( "incorrect userid or password." );

		if( controlEncoding != null ) client.setControlEncoding( controlEncoding );
		if( clientConfig != null ) client.configure( clientConfig );

		client.setFileType( FTPClient.BINARY_FILE_TYPE );
		if( serverPath != null && serverPath.length() > 0 ) changeWorkingDirectory( serverPath );

		return client;
	}

	public void changeWorkingDirectory( String serverPath ) throws IOException {
		if( serverPath == null || !client.changeWorkingDirectory(serverPath) )
			throw new IOException( "directory('"+ serverPath +"') cannot be found." );
	}

	public boolean deleteFile( String fileName ) throws IOException {
		if( controlEncoding != null )
			fileName = new String( fileName.getBytes(controlEncoding), "8859_1" );

		return client.deleteFile( fileName );
	}

	public void disconnect() throws IOException {
		client.logout();
		client.disconnect();
		client = null;
	}

	public FTPFile getFTPFile( String serverFileName ) throws IOException {
		for( FTPFile ftpFile : client.listFiles() ) {
			if( ftpFile.isFile() && ftpFile.getName().equals(serverFileName) )
				return ftpFile;
		}

		return null;
	}

	public FTPClient getFTPClient() {
		return client;
	}

	public String getName() {
		return ( hostName +":"+ port );
	}

	/**
	 * FTP 설정값을 FTPClientConfig에 셋팅 후 return.<br>
	 * <ul>
	 * <li> systemKey : FTP 서버의 시스템 종류 (UNIX, WINDOWS etc, {@link org.apache.commons.net.ftp.FTPClientConfig} 참고, 보통 자동 감지함)
	 * <li> defaultDateFormatStr : 날짜 포맷 (날짜 값 parsing 시 사용, {@link java.util.text.SimpleDateFormat} 참고, default는 en_US를 따름(MM dd yyyy), 한글 서버의 경우 M월 d일 yyyy)
	 * <li> recentDateFormatStr : 최근 날짜 포맷 (최근 날짜 값 parsing 시 사용, {@link java.util.text.SimpleDateFormat} 참고, default는 en_US를 따름(MM dd HH:mm), 한글 서버의 경우 M월 d일 HH:mm)
	 * <li> serverLanguageCode : FTP 서버의 LANG(언어, eg. ko or en)
	 * <li> serverTimeZoneId : FTP 서버의 타임존 ({@link java.util.TimeZone} 참고 eg. America/Denver eg. Asia/Seoul)
	 * </ul>
	 * @param configMap FTP 설정값.
	 */
	public FTPClientConfig getNewFTPClientConfig( Map<String, String> configMap ) {
		if( !configMap.containsKey("systemKey") && !configMap.containsKey("defaultDateFormatStr")
				&& !configMap.containsKey("recentDateFormatStr") && !configMap.containsKey("serverLanguageCode")
				&& !configMap.containsKey("serverTimeZoneId") )
			return null;

		FTPClientConfig clientConfig = null;
		if( configMap.containsKey("systemKey") )
			clientConfig = new FTPClientConfig( configMap.get("systemKey") );
		else
			clientConfig = new FTPClientConfig();

		if( configMap.containsKey("defaultDateFormatStr") )
			clientConfig.setDefaultDateFormatStr( configMap.get("defaultDateFormatStr") );
		if( configMap.containsKey("recentDateFormatStr") )
			clientConfig.setRecentDateFormatStr( configMap.get("recentDateFormatStr") );
		if( configMap.containsKey("serverLanguageCode") )
			clientConfig.setServerLanguageCode( configMap.get("serverLanguageCode") );
		if( configMap.containsKey("serverTimeZoneId") )
			clientConfig.setServerTimeZoneId( configMap.get("serverTimeZoneId") );

		return clientConfig;
	}

	private <T> List<T> intersection( List<T> list1, List<T> list2 ) {
		List<T> list = new ArrayList<T>();
		for ( T t : list1 ) {
			if( list2.contains(t) ) {
				list.add( t );
			}
		}
		return list;
	}

	public boolean isConnected() {
		return client != null && client.isConnected() ? true : false;
	}

	public String[] listFiles() throws IOException {
		return listFiles( DEFAULT_LIMIT_FILE_COUNT );
	}

	public String[] listFiles( int limitFileCount ) throws IOException {
		java.util.List<String> retList = null;

		try{
			List<FTPFile> beforeList = lsEntryLimit( limitFileCount );
			try {Thread.sleep( changeCheckSec * 1000 );} catch( InterruptedException interuptEx ) { }
			List<FTPFile> afterList = lsEntryLimit( limitFileCount );
			List<String> beforeLsList = beforeList.stream()
				.map( entry -> ("" + entry.getTimestamp().getTimeInMillis() + "\t" + entry.getSize() + "\t" + entry.getName()) )
				.collect(Collectors.toCollection(ArrayList::new));
			List<String> afterLsList = afterList.stream()
				.map( entry -> ("" + entry.getTimestamp().getTimeInMillis() + "\t" + entry.getSize() + "\t" + entry.getName()) )
				.collect(Collectors.toCollection(ArrayList::new));

			retList = intersection( beforeLsList, afterLsList );
			if( retList.size() > 0 ) {
				return retList.stream().map( s -> s.split(REGEX_TAB_CONT_PLUS_LAST)[1] ).toArray(String[]::new);
			}
		} finally {
			org.apache.log4j.Logger.getLogger(FTP.class).debug( "listFiles" + " checkSec:" + changeCheckSec + " limit:" + limitFileCount + " found:" + (retList != null ? retList.size() : null) );
		}
		return new String[]{};
	}

	private List<FTPFile> lsEntryLimit( int limitFileCount ) throws IOException {
		List<FTPFile> limitList = new ArrayList<FTPFile>();
		if( limitFileCount <= 0 ) {
			FTPFile[] ftpFiles = client.listFiles();
			if( ftpFiles != null && ftpFiles.length > 0 ) {
				for( FTPFile ftpFile : ftpFiles ) {
					String fileName = ftpFile.getName();
					if( fileName.toLowerCase().endsWith(".ing") || fileName.toLowerCase().endsWith(".tmp") )
						continue;
					if( ftpFile.isFile() )
						limitList.add( ftpFile );
				}
			}
		} else {
			FTPListParseEngine engine = client.initiateListParsing( serverPath );
			FTPFile[] ftpFiles = null;
			while( engine.hasNext() ) {
				if( limitList.size() >= limitFileCount ) break;
				ftpFiles = engine.getNext( limitFileCount );  // "page size" you want
				for( FTPFile ftpFile : ftpFiles ) {
					if( limitList.size() >= limitFileCount ) break;
					String fileName = ftpFile.getName();
					if( fileName.toLowerCase().endsWith(".ing") || fileName.toLowerCase().endsWith(".tmp") )
						continue;
					if( ftpFile.isFile() )
						limitList.add( ftpFile );
				}
			}
		}
		return limitList;
	}

	public void renameFile( String from, String to) throws IOException {
		if( controlEncoding != null ) {
			from = new String( from.getBytes(controlEncoding), "8859_1" );
			to = new String( to.getBytes(controlEncoding), "8859_1" );
		}

		if( !client.rename(from, to) )
			throw new IOException( "file move failed. From : '" + from + "', To : '" + to + "'" );
	}

	public void retrieveFiles( String localPath ) throws IOException {
		String errorFile = null;
		String[] fileArr =  listFiles();

		for( String fileName : fileArr ) {
			try {
				retrieveFile( fileName, localPath );
			} catch( IOException ioEx ) {
				errorFile += fileName;
				errorFile += fileName.equals(fileArr[fileArr.length-1]) ? ", " : "";
			}
		}
		if( errorFile != null )
			throw new IOException( "file('"+ errorFile +"') download failed." );
	}

	public void retrieveFile( String serverFileName, String localPath ) throws IOException {
		OutputStream outputStream = new FileOutputStream( new File(localPath, serverFileName) );

		try {
			if( controlEncoding != null )
				serverFileName = new String( serverFileName.getBytes(controlEncoding), "8859_1" );

			if( !client.retrieveFile(serverFileName, outputStream) )
				throw new IOException();
		} finally {
			try {
				if( outputStream != null ) {
					outputStream.flush();
					outputStream.close();
				}
			} catch( Exception ignored ) {}
		}
	}

	public void setChangeCheckSec( int changeCheckSec ) {
		this.changeCheckSec = changeCheckSec;
	}

	public void setClientConfig( FTPClientConfig clientConfig ) {
		this.clientConfig = clientConfig;
	}

	public void setDataTimeoutSec( int dataTimeoutSec ) {
		this.dataTimeoutSec = dataTimeoutSec;
	}

	public void storeFile( File localFile ) throws IOException {
		storeFile( localFile.getName(), localFile );
	}

	public void storeFile( File[] localFiles ) throws IOException {
		for( File file : localFiles ) {
			storeFile( file.getName(), file );
		}
	}

	public void storeFile( String serverFileName, File localFile ) throws IOException {
		java.io.InputStream inputStream = null;

		try {
			if( controlEncoding != null )
				serverFileName = new String( serverFileName.getBytes(controlEncoding), "8859_1" );

			inputStream = new java.io.FileInputStream( localFile );
			if( !client.storeFile(serverFileName, inputStream) )
				throw new IOException();
		} finally {
			try { if( inputStream != null ) inputStream.close(); } catch( Exception ignored ) {}
		}
	}
}

