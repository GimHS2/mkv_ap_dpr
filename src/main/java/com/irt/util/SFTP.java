/*
 *	File Name:	SFTP.java
 *	Version:	2.2.8
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.8	listFiles(): sftpClient null 경우 처리
 *	jbaek		2022/06/30		2.2.7	리모트 파일 변경 체크를 위한 changeCheckSec, dataTimeoutSec, limitFileCount 추가
 *	GimHS		2021/06/30		2.2.6	listFiles(): 임시 파일(*.ing, *.tmp)은 제외 처리
 *	GimHS		2020/04/29		2.2.5	Generic Type warning 수정
 *	jbaek		2018/03/30		2.2.4	디버깅을 위한 로그 추가
 *	jbaek		2017/06/30		2.2.3	connect(): JSchLogger 설정. 재접속 로직 추가.
 *	GimHS		2016/12/30		2.2.2	listFiles(): 파일 사이즈를 체크하여 변경중인 파일은 제외하는 로직 추가
 *										getLsEntry() 함수 추가
 *	hankalam	2016/12/30		2.2.2	deleteFile(), listFiles(), renameFile(), retrieveFile(), storeFile() 기능 추가
 *										throw 수정
 *	hankalam	2016/03/31		2.2.1	DEFAULT_PORT 추가
 *	jbaek		2014/01/31		2.2.0	create
 *
**/

package com.irt.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.log4j.Level;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTP {
	org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( SFTP.class );

	public final static int DEFAULT_PORT = 22;
	public final static int DEFAULT_DATA_TIMEOUT_SEC = 30;
	public final static int DEFAULT_CONNECT_TIMEOUT_SEC = DEFAULT_DATA_TIMEOUT_SEC;
	public final static int DEFAULT_LIMIT_FILE_COUNT = 100;
	private final static int MAX_SESSION_RETRY_COUNT = 3;
	private final static String REGEX_TAB_CONT_PLUS_LAST = "\\\t(?=[^\t]*$)"; // 'x	y	z' -> [x	y][z]

	private int changeCheckSec = 10;
	int dataTimeoutSec;

	String hostname, userId, password, serverPath;
	int port;

	Session session;
	ChannelSftp sftpClient;
	java.io.InputStream knownHostsInputStream;

	public SFTP( String hostname, String userId, String password ) {
		this( hostname, DEFAULT_PORT, userId, password );
	}

	public SFTP( String hostname, int port, String userId, String password ) {
		this( hostname, port, userId, password, (String) null );
	}

	public SFTP( String hostname, int port, String userId, String password, String serverPath ) {
		this( hostname, port, userId, password, (java.io.InputStream) null, serverPath );
	}

	public SFTP( String hostname, int port, String userId, String password, java.io.InputStream knownHostsInputStream ) {
		this( hostname, port, userId, password, knownHostsInputStream, (String) null );
	}

	public SFTP( String hostname, int port, String userId, String password, java.io.InputStream knownHostsInputStream, String serverPath ) {
		this.hostname = hostname;
		this.port = port;
		this.userId = userId;
		this.password = password;
		this.serverPath = serverPath;
		this.knownHostsInputStream = knownHostsInputStream;
	}

	public ChannelSftp connect() throws IOException {
		if( sftpClient != null )
			throw new IOException( "already connected." );

		if( logger.isTraceEnabled() ) {
			JSch.setLogger( new JSchLogger( Level.DEBUG ) );
		}
		JSch jsch = new JSch();

		session = getSession( jsch, MAX_SESSION_RETRY_COUNT );
		sftpClient = getSftpClient( session, MAX_SESSION_RETRY_COUNT );

		String pwd = pwd();
		if( logger.isTraceEnabled() )
			logger.debug("pwd is: '" + pwd + "'");

		if( serverPath != null && !serverPath.equals( pwd ) ) {
			changeWorkingDirectory( serverPath );
			if( logger.isTraceEnabled() )
				logger.debug( "pwd was: '" + pwd + "' now pwd is: '" + pwd() + "'" );
		}

		return sftpClient;
	}

	public void changeWorkingDirectory( String serverPath ) throws IOException {
		try {
			sftpClient.cd( serverPath );
		} catch( SftpException sftpEx ) {
			if( logger.isTraceEnabled() )
				logger.trace( sftpEx );
			throw new IOException( sftpEx.getMessage(), sftpEx );
		}
	}

	public boolean deleteFile( String fileName ) throws IOException {
		try {
			sftpClient.rm( fileName );
		} catch( SftpException sftpEx ) {
			if( logger.isTraceEnabled() )
				logger.trace( sftpEx );
			throw new IOException( sftpEx.getMessage(), sftpEx );
		}
		return true;
	}

	public void disconnect() throws IOException {
		sftpClient.disconnect();
		session.disconnect();
	}

	public ChannelSftp getClient() {
		return sftpClient;
	}

	@SuppressWarnings("unchecked")
	public ChannelSftp.LsEntry getLsEntry( String serverFileName ) throws IOException {
		java.util.Vector<ChannelSftp.LsEntry> vector;
		try {
			vector = sftpClient.ls( "." );
			for( ChannelSftp.LsEntry item : vector ) {
				if( !item.getAttrs().isDir() && item.getFilename().equals( serverFileName ) )
					return item;
			}
		} catch( SftpException sftpEx ) {
			if( logger.isTraceEnabled() )
				logger.trace( sftpEx );
			throw new IOException( sftpEx.getMessage(), sftpEx );
		}

		return null;
	}

	public String getName() {
		return ( hostname + ":" + port );
	}

	private Session getSession( JSch jsch, int retryCount ) throws IOException {
		Session session = null;

		if( retryCount <= 0 ) {
			throw new IOException( "current retryCount is less than or equals to 0. Please check parameter or network." );
		}

		String unkownHostEx = "java.net.UnknownHostException: " + hostname;
		for( int tri = 0, ok = 0; tri < retryCount; tri++ ) {
			try {
				session = jsch.getSession( userId, hostname, port );

				if( knownHostsInputStream != null )
					jsch.setKnownHosts( knownHostsInputStream );
				else
					session.setConfig( "StrictHostKeyChecking", "no" );

				session.setConfig( "PreferredAuthentications", "password" );
				session.setPassword( password );

				// set connect timeout
				session.setTimeout( (dataTimeoutSec > 0 ? dataTimeoutSec : DEFAULT_CONNECT_TIMEOUT_SEC)*1000 );
				session.connect( (dataTimeoutSec > 0 ? dataTimeoutSec : DEFAULT_CONNECT_TIMEOUT_SEC)*1000 );
				// set data timeout
				session.setTimeout( (dataTimeoutSec > 0 ? dataTimeoutSec : DEFAULT_DATA_TIMEOUT_SEC)*1000 );
				ok = 1;
			} catch( JSchException jsEx ) {
				if( jsEx.getMessage().startsWith( unkownHostEx ) ) {
					logger.warn( "Session is illegalState: retry Session. ( " + jsEx.getMessage() + " ) current retryCount: " + retryCount );
					session = getSession( jsch, retryCount-- );
					continue;
				}
				if( logger.isTraceEnabled() )
					logger.trace( jsEx );
				throw new IOException( "session connection failed. " + jsEx.getMessage(), jsEx );
			}

			if( ok == 1 ) {
				if( logger.isTraceEnabled() )
					logger.debug("'session' established.");
				break;
			}
		}

		return session;
	}

	private ChannelSftp getSftpClient( Session session, int retryCount ) throws IOException {
		ChannelSftp csftp = null;
		if( retryCount <= 0 ) {
			throw new IOException( "current retryCount is less than or equals to 0. Please check parameter or network." );
		}
		try {
			Channel channel = session.openChannel( "sftp" );
			channel.connect();
			csftp = (ChannelSftp) channel;
			// csftp.start(); // 불규칙하게 연결이 끊어지는 경우 이부분 추가 필요
		} catch( JSchException jsEx ) {
			if( logger.isTraceEnabled() )
				logger.trace( jsEx );
			throw new IOException( "sftpClient connection failed. " + jsEx.getMessage(), jsEx );
		}

		for( int tri = 0, ok = 0; tri < retryCount; tri++ ) {
			try {
				csftp.ls( "." );
				ok = 1;
			} catch( SftpException sftpEx ) {
				logger.warn( "ChannelSftp is illegalState: retry ChannelSftp. ( " + sftpEx.getMessage() + " ) current retryCount: " + retryCount );
				csftp = getSftpClient( session, retryCount-- );
			}
			if( ok == 1 ) {
				 if( logger.isTraceEnabled() )
					logger.debug("'sftpClient' established.");
				break;
			}
		}

		return csftp;
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

	/** Present Working Directory */
	public String pwd() throws IOException {
		try {
			return sftpClient.pwd();
		} catch( SftpException sftpEx ) {
			if( logger.isTraceEnabled() )
				logger.trace( sftpEx );
			throw new IOException( sftpEx.getMessage(), sftpEx );
		}
	}

	public boolean isConnected() {
		return sftpClient != null && sftpClient.isConnected() ? true : false;
	}

	public String[] listFiles() throws IOException {
		return listFiles( DEFAULT_LIMIT_FILE_COUNT );
	}

	public String[] listFiles( int limitFileCount ) throws IOException {
		java.util.List<String> retList = null;

		if( sftpClient == null ) return new String[]{};

		try {
			try {
				List<ChannelSftp.LsEntry> beforeList = lsEntryLimit( limitFileCount );
				try {Thread.sleep( changeCheckSec * 1000 );} catch( InterruptedException interuptEx ) { }
				List<ChannelSftp.LsEntry> afterList = lsEntryLimit( limitFileCount );
				List<String> beforeLsList = beforeList.stream()
					.map( entry -> ("" + entry.getAttrs().getMTime() + "\t" + entry.getAttrs().getSize() + "\t" + entry.getFilename()) )
					.collect(Collectors.toCollection(ArrayList::new));
				List<String> afterLsList =
					afterList.stream().map( entry -> ("" + entry.getAttrs().getMTime() + "\t" + entry.getAttrs().getSize() + "\t" + entry.getFilename()) )
					.collect(Collectors.toCollection(ArrayList::new));

				retList = intersection( beforeLsList, afterLsList );
				if( retList.size() > 0 ) {
					return retList.stream().map( s -> s.split(REGEX_TAB_CONT_PLUS_LAST)[1] ).toArray(String[]::new);
				}
			} catch( SftpException sftpEx ) {
				if( logger.isTraceEnabled() )
					logger.trace( sftpEx );
				throw new IOException( sftpEx.getMessage(), sftpEx );
			}
		} finally {
			logger.debug( "listFiles" + " checkSec:"+ changeCheckSec + " limit:" + limitFileCount + " found:" + (retList != null ? retList.size() : null) );
		}
		return new String[]{};
	}

	private java.util.List<ChannelSftp.LsEntry> lsEntryLimit( int limitFileCount ) throws SftpException {
		final java.util.List<ChannelSftp.LsEntry> limitList = new java.util.ArrayList<ChannelSftp.LsEntry>();
		if( limitFileCount <= 0 ) {
			@SuppressWarnings("unchecked")
			java.util.Vector<ChannelSftp.LsEntry> vector = sftpClient.ls( "." );
			for( ChannelSftp.LsEntry item : vector ) {
				String fileName = item.getFilename();
				if( fileName.toLowerCase().endsWith(".ing") || fileName.toLowerCase().endsWith(".tmp") )
					continue;

				limitList.add( item );
			}
			return limitList;
		}

		ChannelSftp.LsEntrySelector selector = new ChannelSftp.LsEntrySelector() {
				@Override
				public int select( LsEntry entry ) {
					if( limitFileCount <= 0 ) {
						return CONTINUE;
					} else {
						if( limitList.size() >= limitFileCount )
							return BREAK;
						if( entry.getFilename().toLowerCase().endsWith(".ing") || entry.getFilename().toLowerCase().endsWith(".tmp") )
							return BREAK;
						if( !entry.getAttrs().isDir() ) {
							limitList.add( entry );
							return CONTINUE;
						}
						return BREAK;
					}
				}
			};
		sftpClient.ls( "*.*", selector );
		return limitList;
	}

	public void setChangeCheckSec( int changeCheckSec ) {
		this.changeCheckSec = changeCheckSec;
	}

	public void setDataTimeoutSec( int dataTimeoutSec ) {
		this.dataTimeoutSec = dataTimeoutSec;
	}

	public void renameFile( String from, String to ) throws IOException {
		try {
			sftpClient.rename( from, to );
		} catch( SftpException sftpEx ) {
			if( logger.isTraceEnabled() )
				logger.trace( sftpEx );
			throw new IOException( sftpEx.getMessage(), sftpEx );
		}
	}

	public void retrieveFiles( String localPath ) throws IOException {
		String errorFile = null;
		String[] fileArr = listFiles();

		for( String fileName : fileArr ) {
			try {
				retrieveFile( fileName, localPath );
			} catch( IOException ioEx ) {
				errorFile += fileName;
				errorFile += fileName.equals( fileArr[fileArr.length - 1] ) ? ", " : "";
			}
		}
		if( errorFile != null )
			throw new IOException( "file('" + errorFile + "') download failed." );
	}

	public void retrieveFile( String serverFileName, String localPath ) throws IOException {
		OutputStream outputStream = new FileOutputStream( new File( localPath, serverFileName ) );

		try {
			sftpClient.get( serverFileName, outputStream );
		} catch( SftpException sftpEx ) {
			if( logger.isTraceEnabled() )
				logger.trace( sftpEx );
			throw new IOException( sftpEx.getMessage(), sftpEx );
		} finally {
			try {
				if( outputStream != null ) {
					outputStream.flush();
					outputStream.close();
				}
			} catch( Exception ignored ) {
			}
		}
	}

	public void storeFile( File localFile ) throws IOException {
		storeFile( localFile.getName(), localFile );
	}

	public void storeFile( File[] localFiles ) throws IOException {
		for( File file : localFiles ) {
			storeFile( file.getName(), file );
		}
	}

	public void storeFile( String serverFilename, File localFile ) throws IOException {
		java.io.InputStream inputStream = new java.io.FileInputStream( localFile );
		storeFile( serverFilename, inputStream );
	}

	public void storeFile( String serverFilename, java.io.InputStream inputStream ) throws IOException {
		String pwd = null;

		try {
			pwd = sftpClient.pwd();
			sftpClient.put( inputStream, serverFilename );
		} catch( SftpException sftpEx ) {
			if( logger.isTraceEnabled() )
				logger.trace( sftpEx );
			throw new IOException( "pwd( " + pwd + " ) - " + sftpEx.getMessage(), sftpEx );
		} finally {
			try {
				if( inputStream != null )
					inputStream.close();
			} catch( Exception ignored ) {
			}
		}
	}

	public Session getSession() {
		return this.session;
	}

	/** JSch 내부 로깅용.
	 *<br> SFTP에서 사용시에 SFTP.class의 log4j Level설정에 따라서 JSch의 내부 로깅 내용이 log4j에 프린트됨.
	 */
	public class JSchLogger implements com.jcraft.jsch.Logger {

		private final org.apache.log4j.Logger logger;
		private int jschEnabledLevel;
		private final static int LEVEL_ALL = -1;
		java.util.Hashtable<Integer, org.apache.log4j.Level> levels = new java.util.Hashtable<Integer, org.apache.log4j.Level>();

		public JSchLogger() {
			this( null );
		}

		public JSchLogger( org.apache.log4j.Level level ) {
			levels.put( DEBUG, org.apache.log4j.Level.DEBUG );
			levels.put( INFO, org.apache.log4j.Level.INFO );
			levels.put( WARN, org.apache.log4j.Level.WARN );
			levels.put( ERROR, org.apache.log4j.Level.ERROR );
			levels.put( FATAL, org.apache.log4j.Level.FATAL );
			levels.put( LEVEL_ALL, org.apache.log4j.Level.ALL );

			if( level == null ) {
				jschEnabledLevel = FATAL;
			} else {
				for( Map.Entry<Integer, org.apache.log4j.Level> it : levels.entrySet() ) {
					if( level.equals( it.getValue() ) ) {
						jschEnabledLevel = it.getKey();
					}
				}
			}
			logger = org.apache.log4j.Logger.getLogger( "com.irt.util.JSchLogger" );
		}

		public Map<Integer, org.apache.log4j.Level> getLevels() {
			return levels;
		}

		/* @see com.jcraft.jsch.Logger#isEnabled(int) */
		@Override
		public boolean isEnabled( int level ) {
			if( jschEnabledLevel > level )
				return false;
			else
				return true;
		}

		/* @see com.jcraft.jsch.Logger#log(int, java.lang.String) */
		@Override
		public void log( int pLevel, String message ) {
			org.apache.log4j.Level level = levels.get( pLevel );
			if( level == null ) {
				level = org.apache.log4j.Level.FATAL;
			}
			logger.log( level, message );
		}
	}

}
