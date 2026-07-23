/*
 *	File Name:	FileProcessRunner.java
 *	Version:	2.2.13c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2022/07/29		2.2.13	lmitFileCount -> limitFileCount 오타수정
 *	GimHS		2021/06/30		2.2.12	accept(): 임시 파일(*.ing, *.tmp)은 제외 처리
 *	hankalam	2021/02/26		2.2.11	accept(), listFiles(): 변경 중인 파일 체크 시 일괄 체크되도록 변경
 *	hankalam	2019/07/31		2.2.10	errorDirectory, directory, backupDirectory 접근자 protected 로 변경
 *	chan1914	2018/06/29		2.2.9	setFileNamePrefix() 추가, accept(): prefixKeys로 받아들일 파일 체크하는 로직 추가
 *	hankalam	2017/07/31		2.2.8	accept(): 변경 중인 파일 체크시 지정한 millisecond 만큼 체크하는 로직 추가
 *										listFiles() 추가
 *										excute(): 지정한 파일 수 단위로 처리하는 로직 추가
 *	GimHS		2016/12/30		2.2.7	accept(): 변경 중인 파일은 제외하는 로직 추가
 *	hankalam	2016/03/31		2.2.6	checkDirectory() 추가
 *	chan1914	2016/02/29		2.2.5	setBackupDirectory(), setErrorDirectory(): 해당 경로에 directory 존재 여부를 체크하여 없으면 생성하도록 수정
 *	jbaek		2018/10/30		2.2.4c	getDir(), getErrDir(), getBckDir() 추가
 *	stghr12		2011/06/30		2.2.4	execute() throws InterruptedException 처리
 *										getSQLHandler() 추가, setBackupDirectory()/setErrorDirectory(): Directory 검사 후 set으로 변경
 *	stghr12		2011/02/28		2.2.3	void execute() throws ProcessException -> boolean execute()
 *										closeProcessedFile()에서 throws IOException 제거
 *	stghr12		2009/10/31		2.2.2	accept(): 쓰기 가능한 파일만 읽기
 *	stghr12		2008/12/31		2.2.1	process.continueProcessing()을 closeProcessedFile()에 사용
 *	stghr12		2008/03/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.sql.SQLHandler;
import com.irt.system.SystemConfig;
import com.irt.util.MessageHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 *
 */
public abstract class FileProcessRunner extends ProcessRunner {
	protected File errorDirectory, directory, backupDirectory;

	SystemConfig systemConfig;
	int limitFileCount = 100;
	long checkFileSleepMillis = 2000;
	protected String[] prefixKeys;

	public FileProcessRunner( SystemConfig systemConfig, File directory ) {
		this.systemConfig = systemConfig;
		if( checkDirectory(directory) )
			this.directory = directory;

		this.backupDirectory = null;
		this.errorDirectory = null;
	}

	public boolean accept( File file ) {
		if( file.isFile() && file.canWrite() ) {
			String fileName = file.getName();

			if( fileName.toLowerCase().endsWith(".ing") || fileName.toLowerCase().endsWith(".tmp") )
				return false;

			if( this.prefixKeys != null ) {
				for( int i = 0; i < this.prefixKeys.length; i++ ) {
					if( fileName.contains( this.prefixKeys[i]) )
						return true;
				}
				return false;
			}

			return true;
		}
		return false;
	}

	public boolean checkDirectory( File directory ) {
		if( !directory.exists() ) directory.mkdir();
		if( !directory.isDirectory() )
			throw new IllegalArgumentException( "illegal directory '"+ directory.getAbsolutePath() +"'." );
		else if( !directory.canWrite() )
			throw new IllegalArgumentException( "directory '"+ directory.getAbsolutePath() +"' is not writable." );

		return true;
	}

	public void closeProcessedFile( File file, boolean executed ) {
		File directory = ( executed || errorDirectory == null ? backupDirectory : errorDirectory );

		if( directory == null ) {
			file.delete();
			getLogger().info( getDescription() +" file('"+ file.getName() +"') deleted." );
		} else {
			File renameFile = new File( directory, file.getName() );

			if( renameFile.exists() ) renameFile.delete();
			if( !file.renameTo(renameFile) ) {
				java.io.InputStream inputStream = null;
				java.io.OutputStream outputStream = null;
				try {
					int length;
					byte[] buffer = new byte[10240];

					inputStream = new java.io.FileInputStream( file );
					outputStream = new java.io.FileOutputStream( renameFile );
					while( (length = inputStream.read(buffer)) > 0 )
						outputStream.write( buffer, 0, length );
				} catch( IOException ioEx ) {
					getLogger().error( getDescription() +" file('"+ file.getName() +"') move error.", ioEx );
				} finally {
					try { if( inputStream != null ) inputStream.close(); } catch( Exception ignored ) {}
					try { if( outputStream != null ) outputStream.close(); } catch( Exception ignored ) {}
				}
				file.delete();
			}
			getLogger().info( getDescription() +" file('"+ file.getName() +"') move to '"+ directory.getAbsolutePath() +"'." );
		}
	}

	@Override
	public boolean execute() {
		File[] files;

		Logger logger = getLogger();
		SQLHandler handler = null;
		int count = 0;

		try {
			loop: {
				do {
					files = listFiles( directory );
					if( files == null || files.length == 0 ) return true;

					logger.info( getDescription() +" '"+ directory.getAbsolutePath() +"': "+ files.length +" files found." );

					for( File file : files ) {
						Process process;

						try {
							if( handler == null ) handler = getSQLHandler();
							logger.info( getDescription() +" SQLHandler created." );

							process = getProcessInstance( handler, file );
						} catch( ProcessException processEx ) {
							logger.error( getDescription() +" error.", processEx.getCause() != null ? processEx.getCause() : processEx );
							return false;
						}

						if( process == null ) {
							logger.info( "'"+ file.getName() +"' ignored." );
							closeProcessedFile( file, true );
						} else if( process != ProcessRunner.EMPTY ) {
							final File final_file = file;
							final boolean final_closingSQLHandler = usingThread;
							final SQLHandler final_handler = handler;

							process = new ProcessWrapper( process, "FileProcess."+ file.getName(), "'"+ file.getName() +"'" ) {
								File file = final_file;
								boolean closingSQLHandler = final_closingSQLHandler;
								SQLHandler handler = final_handler;

								@Override
								public void close() {
									if( closingSQLHandler ) handler.close();
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
							if( final_closingSQLHandler ) handler = null;

							if( !execute(process) ) return false;
						}

						count++;
						if( isInterrupted() ) break loop;
					}
				} while( files != null );
			}

			logger.info( getDescription() +" '"+ directory.getAbsolutePath() +"': "+ count +" files processed." );
			return true;
		} finally {
			try { if( handler != null ) handler.close(); } catch( Exception ignored ) {}
		}
	}

	public File getBckDir() {
		return backupDirectory;
	}

	@Override
	public String getDescription() {
		return "FileProcessRunner";
	}

	public File getDir() {
		return directory;
	}

	public File getErrDir() {
		return errorDirectory;
	}

	@Override
	public Logger getLogger() {
		return Logger.getLogger( "com.irt.rbm.tools.FileProcessRunner" );
	}

	public abstract Process getProcessInstance( SQLHandler handler, File file ) throws ProcessException;

	@Override
	public String getProcessName() {
		return "FileProcessRunner";
	}

	protected SQLHandler getSQLHandler() throws ProcessException {
		try {
			MessageHandler msghandler = systemConfig.getMessageHandler();
			SQLHandler handler = systemConfig.createSQLHandler( msghandler, DEFAULT_SQLHANDLER_CREATING_TRIALCOUNT, DEFAULT_SQLHANDLER_WAIT_MILLIS );

			if( handler == null )
				throw new ProcessException( msghandler.getMessage("ERR_CANNOT_GET_SQLHANDLER") );

			return handler;
		} catch( SQLException sqlEx ) {
			throw new ProcessException( sqlEx );
		}
	}

	boolean isWatching(){
		return false;
	}

	public File[] listFiles( File directory ) {
		String ss[] = directory.list();
		if( ss == null )
			return null;
		int count = ( ss.length <= (this.limitFileCount == 0 ? ss.length : this.limitFileCount) ) ? ss.length : this.limitFileCount;
		java.util.ArrayList<File> v = new ArrayList<File>();
		Map<File, Long> fileSizeMap = new java.util.LinkedHashMap<File, Long>();
		for( int i = 0; i < count; i++ ) {
			File file = new File( directory, ss[i] );
			if( !file.isFile() ) {
				if( ss.length > count) count++;
				continue;
			}

			if( this.accept(file) ) {
				long fileSize = file.length();
				fileSizeMap.put( file, fileSize );
			}
		}

		if( checkFileSleepMillis > 0 ) {
			try { Thread.sleep( checkFileSleepMillis ); } catch ( InterruptedException interuptEx ) {}

			for( Iterator<Map.Entry<File, Long>> it = fileSizeMap.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<File, Long> entry = it.next();
				File file = entry.getKey();
				long beforeSize = entry.getValue();
				long fileSize = file.length();
				if( beforeSize != fileSize ) {
					it.remove();
					getLogger().warn( getDescription() +" file('"+ file.getName() +"') can not be processed because the file being transferred" );
				}
			}
		}
		return fileSizeMap.keySet().toArray( new File[v.size()] );
	}

	public void setBackupDirectory( String backupDirectory ) {
		File directory = new File( backupDirectory );

		if( checkDirectory(directory) )
			this.backupDirectory = directory;
	}

	public void setCheckFileSleepMillis( long checkFileSleepMillis ) {
		this.checkFileSleepMillis = checkFileSleepMillis;
	}

	public void setErrorDirectory( String errorDirectory ) {
		File directory = new File( errorDirectory );

		if( checkDirectory(directory) )
			this.errorDirectory = directory;
	}

	public void setLimitFileCount( int limitFileCount ) {
		this.limitFileCount = limitFileCount;
	}

	public void setFileNamePrefix( String prefixKey ) {
		this.prefixKeys = prefixKey.split( "," );
	}
}

