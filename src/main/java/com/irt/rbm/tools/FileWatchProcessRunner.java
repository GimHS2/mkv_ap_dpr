/*
 *	File Name:	FileWatchProcessRunner.java
 *	Version:	2.2.1c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.1	filenameparser
 *	jbaek		2021/12/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

import com.irt.sql.SQLHandler;
import com.irt.system.SystemConfig;
import com.irt.util.FilenameParser;

import org.apache.log4j.Logger;

/** Process for Created or Modified files using jdk WatchService */
public abstract class FileWatchProcessRunner extends FileProcessRunner implements MyFileFilter {

	String chgMarker = "";

	boolean deleteProcessed = true;

	File directory;

	int maxBatchSize = 100;// max processing files between ProcessRunner.sleepMillis

	Duration stayDuration = Duration.parse( "PT1M" );// 1minutes

	FileWatchLister watch_lister;

	FilenameParser filenameParser = new FilenameParser();

	public FileWatchProcessRunner( SystemConfig systemConfig, File directory ) {
		super( systemConfig, directory );
		this.directory = directory;
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

	@Override
	public void close() {
		String chgMarker = watch_lister.getChangeSetMarker();
		if( watch_lister != null  ) {
			int staleEventCount = watch_lister.getChangedEventCount();
			getLogger().info( getDescription(chgMarker) + " " + staleEventCount + " evetns will drop because FileWatchLister instance is stopping." );
			watch_lister.interrupt();
		}
		super.close();
	}

	@Override
	public void closeProcessedFile( File file, boolean executed ) {
		File directory = ( executed || errorDirectory == null ? backupDirectory : errorDirectory );

		if( directory == null ) {
			if( deleteProcessed ) {
				file.delete();
				getLogger().info( getDescription(chgMarker) +" file('"+ file.getName() +"') deleted." );
			} else {
				getLogger().info( getDescription(chgMarker) + " file('" + file.getName() + "') process closed." );
			}
		} else {
			File renameFile = new File( directory, file.getName() );

			if( renameFile.exists() )
				renameFile.delete();
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
				} catch ( IOException ioEx ) {
					getLogger().error( getDescription(chgMarker) + " file('" + file.getName() + "') move error.", ioEx );
				} finally {
					try {
						if( inputStream != null )
							inputStream.close();
					} catch ( Exception ignored ) {
					}
					try {
						if( outputStream != null )
							outputStream.close();
					} catch ( Exception ignored ) {
					}
				}
				file.delete();
			}
			getLogger().info( getDescription(chgMarker) + " file('" + file.getName() + "' ) move to '" + directory.getAbsolutePath() + "'." );
		}
	}

	public FilenameParser getFilenameParser() {
		return filenameParser;
	}

	@Override
	public boolean execute() {
		if( watch_lister == null  ) {
			watch_lister = new FileWatchLister( directory );
			watch_lister.setFileFilter( (MyFileFilter)this );
			watch_lister.start();
		}

		Path[] files = watch_lister.getChangedPaths( maxBatchSize, stayDuration );
		chgMarker = watch_lister.getChangeSetMarker();
		if( files == null || files.length == 0  ) return true;

		Logger logger = getLogger();
		SQLHandler handler = null;
		try {
			logger.info( getDescription(chgMarker) +" '"+ directory.getAbsolutePath() +"': "+ files.length +" files found." );

			int count = 0;
			for( Path path : files  ) {
				File file = path.toFile();

				Process process;

				try {
					if( handler == null ) handler = getSQLHandler();
					logger.info( getDescription(chgMarker) +" SQLHandler created." );

					process = getProcessInstance( handler, file  );
				} catch( ProcessException processEx ) {
					logger.error( getDescription(chgMarker) +" error.", processEx.getCause() != null ? processEx.getCause() : processEx );
					return false;
				}

				if( process == null ) {
					logger.info( "'"+ file.getName() +"' ignored." );
					closeProcessedFile( file, true  );
				} else if( process != ProcessRunner.EMPTY ) {
					final File final_file = file;
					final boolean final_closingSQLHandler = usingThread;
					final SQLHandler final_handler = handler;

					process = new ProcessWrapper( process, "FileProcess."+ file.getName(), "'"+ file.getName() +"'" ) {
						boolean closingSQLHandler = final_closingSQLHandler;
						File file = final_file;
						SQLHandler handler = final_handler;

						public void close() {
							if( closingSQLHandler ) handler.close();
							super.close();
						}

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
				if( isInterrupted() ) break;
			}

			logger.info( getDescription(chgMarker) +" '"+ directory.getAbsolutePath() +"': "+ count +" files processed." );
			return true;
		} finally {
			try { if( handler != null ) handler.close(); } catch( Exception ignored ) {}
		}
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public String getDescription( String changeSetMarker ) {
		return getDescription()+":"+changeSetMarker;
	}

	public abstract Process getProcessInstance( SQLHandler handler, File file ) throws ProcessException;

	public Duration getStayDuration() {
		return stayDuration;
	}

	public boolean isDeleteProcessed() {
		return deleteProcessed;
	}

	public void setDeleteProcessed( boolean deleteProcessed ) {
		this.deleteProcessed = deleteProcessed;
	}

	public void setFilenameParser( FilenameParser filenameParser ) {
		this.filenameParser = filenameParser;
	}

	public void setMaxBatchSize( int maxBatchSize ) {
		this.maxBatchSize = maxBatchSize;
	}

	public void setStayDuration( Duration stayDuration ) {
		this.stayDuration = stayDuration;
	}

	public void setStayDuration( String stayDurationStr ) {
		this.stayDuration = Duration.parse( stayDurationStr );
	}

}
