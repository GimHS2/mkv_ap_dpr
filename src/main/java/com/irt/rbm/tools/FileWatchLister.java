/*
 *	File Name:	FileWatchLister.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2023/07/27		2.2.1	MyFileFilter 적용
 *	jbaek		2021/12/31		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import java.io.File;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.irt.sql.SQLHandler;

import org.apache.log4j.Logger;

/**
 *  Local FileSystem only.( This will not work on Network FileSystem like NFS or SAMBA )
 */
class FileWatchLister extends com.irt.rbm.tools.ProcessRunner {

	private ConcurrentHashMap<Path, FileTime> crePaths, modPaths;

	private final static Kind<Path> ENTRY_CREATE = StandardWatchEventKinds.ENTRY_CREATE;

	private final static Kind<Path> ENTRY_DELETE = StandardWatchEventKinds.ENTRY_DELETE;

	private final static Kind<Path> ENTRY_MODIFY = StandardWatchEventKinds.ENTRY_MODIFY;

	private final static Kind<Object> OVERFLOW = StandardWatchEventKinds.OVERFLOW;

	private File directory;

	private Path dirPath = null;

	private MyFileFilter fileFilter;

	private ProcessRunner parentRunner;

	private WatchService watcher;

	private WatchKey watchKey = null;

	private String changeSetMarker = newChangeSetMarker();

	public FileWatchLister( File directory ) {
		this.directory = directory;
		this.crePaths = new ConcurrentHashMap<Path, FileTime>();
		this.modPaths = new ConcurrentHashMap<Path, FileTime>();
	}

	public boolean accept( File file ) {
		return file.exists() && file.canWrite();
	}

	public void close() {
		getLogger().trace( getDescription() + " FileWatchLister closing." );
		if( watcher != null ) {
			getLogger().trace( getDescription() + " closing." + " watcher:" + watcher );
			try {
				watcher.close();
			} catch( IOException ioEx ) {
				getLogger().trace( getDescription() + " error." + ioEx.getCause() != null ? ioEx.getCause() : ioEx );
			}
			watcher = null;
		}
		super.close();
	}

	public boolean continueProcessing() {
		return true;
	}

	public boolean execute() {
		Logger logger = getLogger();
		SQLHandler handler = null;

		String watchDir = directory.getAbsolutePath();
		try {
			synchronized( this ) {
				if( watcher == null )
					watcher = FileSystems.getDefault().newWatchService();
			}
			synchronized( this ) {
				if( this.dirPath == null ) {
					this.dirPath = Paths.get( watchDir );
					this.dirPath.register( watcher, ENTRY_CREATE, ENTRY_MODIFY, OVERFLOW );
				}
			}
			while( true ) {
				try {
					try {
						// take() will wait for a key to be. Thread is blocking
						watchKey = watcher.take();
						// watchKey = watcher.poll( 10, TimeUnit.SECONDS );

						logger.trace( getDescription() + " taken " + watchKey + " from " + watchDir );
					} catch( InterruptedException interruptEx ) {
						logger.trace( getDescription() + " watchService error."
									  , interruptEx.getCause() != null ? interruptEx.getCause() : interruptEx );
						if( watchKey != null ) {
							watchKey.reset();
							watchKey.cancel();
						}
						if( watcher != null ) {
							logger.trace( getDescription() + " watcher closing, watchKey:" + watchKey );
							watcher.close();
						}
						// invoke current thread interrupt, so eventually interrupt ProcessRunner.run() method
						Thread.currentThread().interrupt();
						break;
					} catch( ClosedWatchServiceException closeEx ) {
						logger.trace( getDescription() + " take is killed from closeException." );
						break;
					}

					if( watchKey == null ) {
						logger.trace( getDescription() + " error." + " watchKey not initialized. skip current loop." );
						break;
					}

					for( WatchEvent<?> event : watchKey.pollEvents() ) {

						WatchEvent.Kind<?> kind = event.kind();

						WatchEvent<Path> ev = ( WatchEvent<Path> ) event;

						Path fileName = ev.context();// only subpath from the directory

						Path filePath = Paths.get( dirPath.toAbsolutePath().toString(), fileName.toString() );
						File file = filePath.toFile();
						if( this.fileFilter != null ) {
							if( !this.fileFilter.accept(file) ) {
								logger.trace( getDescription() + " file not accepted by fileFilter:" + fileName );
								continue;
							}
						} else {
							if( !this.accept(file) ) {
								logger.trace( getDescription() + " file not accepted by default fileFilter:" + fileName );
								continue;
							}
						}

						logger.trace( getDescription() + " " + kind.name() + ": " + fileName + " found event." );
						if( (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) && !Files.exists(filePath) )
							continue;

						boolean isLockFree = isLockFree( filePath );
						boolean canWrite = filePath.toFile().canWrite();
						FileTime lastModified = null;
						if( kind == ENTRY_CREATE || kind == ENTRY_MODIFY ) {
							BasicFileAttributes attr = Files.readAttributes( filePath, BasicFileAttributes.class );
							lastModified = attr.lastModifiedTime();
							logger.trace( getDescription() + " " + kind.name() + ": " + fileName.getFileName()
									+ " canWrite:" + canWrite + " isLockFree:" + isLockFree + " mod:"
									+ attr.lastModifiedTime() + " now:" + Instant.now() );
						}

						if( kind == OVERFLOW ) {
							logger.error( getDescription() + " " + kind.name() + ": " + fileName
									+ " you may missed event. you may need to speed up your processing performance to prevent overflow event.");
							continue;
							// } else if( kind == ENTRY_DELETE ) {
						} else if( kind == ENTRY_CREATE ) {
							if( isLockFree ) {
								crePaths.put( filePath, lastModified );
								logger.trace( getDescription() + " registered crePaths. cnt:" + crePaths.size() );
							}
						} else if( kind == ENTRY_MODIFY ) {
							if( isLockFree ) {
								modPaths.put( filePath, lastModified );
								logger.trace( getDescription() + " registered modPaths. cnt:" + modPaths.size() );
							}
						}
					}
				} finally {
					// IMPORTANT: The key must be reset after processed
					if( watchKey != null ) {
						boolean valid = watchKey.reset();
						logger.trace( getDescription() + " watchKey is valid?:" + valid );
						if( !valid ) {
							break;
						}
					}
				}
			}
		} catch( java.io.IOException ioEx ) {
			logger.error( getDescription() + " error.", ioEx.getCause() != null ? ioEx.getCause() : ioEx );
			return false;
		} finally {
			try {
				if( handler != null )
					handler.close();
			} catch( Exception ignored ) {
			}
			if( modPaths.size() > 0 || crePaths.size() > 0 ) {
				logger.info( getDescription() + " events are detected." + " mod:" + modPaths.size() + " cre:"
						+ crePaths.size() + " Processor will be initiated shortly as the processor configuration." );
			}

			return true;
		}
	}

	public int getChangedCount( int maxSize, Duration stayDu ) {
		return getChangedPaths( maxSize, stayDu, true ).length;
	}

	private int getChangedCount( int maxSize, Duration stayDu, boolean recordCleanup ) {
		return getChangedPaths( maxSize, stayDu, recordCleanup ).length;
	}

	public int getChangedEventCount() {
		return modPaths.size() + crePaths.size();
	}

	public Path[] getChangedPaths( int maxSize, Duration stayDu ) {
		return getChangedPaths( maxSize, stayDu, true );
	}

	private Path[] getChangedPaths( int maxSize, Duration stayDu, boolean recordCleanup ) {
		java.util.Set<Path> chgFiles = new java.util.TreeSet<Path>();
		if( getChangedEventCount() == 0 ) {
			changeSetMarker = newChangeSetMarker();
		}
		synchronized( chgFiles ) {
			long refTime = System.currentTimeMillis();
			List<Path> modFiles = getModifiedPathList( refTime, maxSize, stayDu, recordCleanup );
			List<Path> creFiles = getCreatedPathList( refTime, maxSize, stayDu, recordCleanup );
			chgFiles.addAll( modFiles );
			chgFiles.addAll( creFiles );
		}

		return chgFiles.toArray( new Path[0] );
	}

	public String getChangeSetMarker() {
		return changeSetMarker;
	}

	/**
	 * return stayed enough files: marked as created and stayed for duration
	 * ( usually file has moved too fast so that the modify event did not occurred )
	 */
	private List<Path> getCreatedPathList( long refTime, int maxSize, Duration stayDu, boolean recordCleanup ) {
		return getPathList( crePaths, refTime, maxSize, stayDu, recordCleanup );
	}

	public ConcurrentHashMap<Path, FileTime> getCreatedPaths() {
		return crePaths;
	}

	public String getDescription() {
		String parentName = ( parentRunner != null ? parentRunner.getProcessName()+"." : "" );
		String suffix = ( changeSetMarker == null ? "" : ":"+changeSetMarker );
		return parentName + "FileWatchLister" + suffix;
	}

	private FileLock getFileLock( Path filePath ) throws java.io.IOException {
		if( !Files.exists(filePath) )
			return null;

		FileChannel ch = null;
		try {
			ch = FileChannel.open( filePath, StandardOpenOption.WRITE );
			FileLock lock = null;
			try {
				lock = ch.tryLock();
				if( lock == null ) {
					// no lock, other process is still writing
					return null;
				} else {
					// you got a lock, other process is done writing
					return lock;
				}
			} catch( java.nio.channels.OverlappingFileLockException lockEx ) {
				getLogger().debug( getDescription() + " error. ignoring lock exception:"
						+ ( lockEx.getCause() != null ? lockEx.getCause() : lockEx) );
				return null;
			} catch( java.io.IOException ioEx ) {
				getLogger().debug( getDescription() + " error. ignoring io exception:"
						+ ( ioEx.getCause() != null ? ioEx.getCause() : ioEx) );
				return null;
			}
		} finally {
			if( ch != null )
				ch = null;
		}
	}

	/** return stayed enough files: marked as modified and stayed for duration */
	private List<Path> getModifiedPathList( long refTime, int maxSize, Duration stayDu, boolean recordCleanup ) {
		return getPathList( modPaths, refTime, maxSize, stayDu, recordCleanup );
	}

	public ConcurrentHashMap<Path, FileTime> getModifiedPaths() {
		return modPaths;
	}

	private List<Path> getPathList( Map<Path, FileTime> paths, long refTime, int maxSize, Duration stayDu, boolean recordCleanup ) {
		List<Path> staleRemove = new ArrayList<Path>();
		List<Path> files = new ArrayList<Path>();
		synchronized( paths ) {
			for( Map.Entry<Path, FileTime> entry : paths.entrySet() ) {
				if( files.size() >= maxSize )
					break;
				Path filePath = entry.getKey();
				FileTime lastModifiedSeen = entry.getValue();
				FileTime currModifiedRead = readLastModifiedTime( entry.getKey() );
				if( currModifiedRead == null ) {
					if( !Files.exists(filePath) ) {
						staleRemove.add( filePath );
						continue;
					} else {
						getLogger().warn( getDescription() + " error." + " currModifiedRead is null:" + entry.getKey() );
					}
				}

				boolean isStayed = ( currModifiedRead != null && lastModifiedSeen != null
						&& lastModifiedSeen.toMillis() == currModifiedRead.toMillis() );
				boolean isEnoughWaited = isStayed
						&& ( lastModifiedSeen.toMillis() <= refTime - stayDu.toMillis() );
				if( isEnoughWaited ) {
					files.add( filePath );
				}
			}
			if( staleRemove.size() > 0 ) {
				for( Path path : staleRemove )
					paths.remove( path );
			}
			if( recordCleanup ) {
				for( Path path : files )
					paths.remove( path );
			}
		}

		return files;
	}

	public String getProcessName() {
		String parentName = ( parentRunner != null ? parentRunner.getProcessName()+"." : "" );
		return parentName + "FileWatchLister." + "'" + directory.getAbsolutePath() + "'";
	}

	private boolean isLockFree( Path filePath ) throws java.io.IOException {
		FileLock lock = getFileLock( filePath );
		if( lock != null ) {
			lock.release();
			return true;
		}
		return false;
	}

	private String newChangeSetMarker() {
		return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
	}

	private FileTime readLastModifiedTime( Path filePath ) {
		if( filePath == null || !Files.exists( filePath ) )
			return null;

		try {
			return Files.readAttributes( filePath, BasicFileAttributes.class).lastModifiedTime();
		} catch( IOException ioEx ) {
			getLogger().error( getDescription() + " error.", ioEx.getCause() != null ? ioEx.getCause() : ioEx );
			return null;
		}
	}

	public void setFileFilter( MyFileFilter filter ) {
		this.fileFilter = filter;
	}

	public void setParentRunner( ProcessRunner parentRunner ) {
		this.parentRunner = parentRunner;
	}

}
