/*
 *	File Name:	WorkFileProcessRunner.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2018/10/30		2.2.0c	create
 *
**/
package com.irt.rbm.tools;

import com.irt.sql.SQLHandler;
import com.irt.system.SystemConfig;
import com.irt.util.FilenameParser;
import com.irt.util.TimeGranular;
import com.irt.util.TraceHelper;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * @deprecated not performant( java polling is not performant.) use FileWatchRunner instead.( os polling way )
 */
@Deprecated
public class WorkFileProcessRunner extends com.irt.rbm.tools.FileProcessRunner {

	private static final String FILE_EVENTTIME_KEY = "eventTime";

	private static final int STATE_WORKABLE = 0;
	private static final int STATE_WORKING = 1;
	private static final int STATE_WORKDONE = 99;

	/*
	 * who created this instance? usually RBMTools or RBMToolsDaemon
	 */
	long deamonThreadId = 0;
	long runnerThreadId = 0;

	static Map<Map.Entry<Date, Date>, Object> granDateStates = java.util.Collections
			.synchronizedMap(new java.util.HashMap<Map.Entry<Date, Date>, Object>());
	static Map<String, Object> processedFilenames = java.util.Collections.synchronizedMap(new java.util.HashMap<String, Object>());

	private Date baseDateTime;
	FilenameParser filenameParser = new FilenameParser();
	TimeGranular timeGranular = new TimeGranular();
	private List<Map.Entry<Date, Date>> granDates;

	private String verbose = "";
	private File workDir;
	private Map.Entry<Date, Date> workEntry;
	private Map.Entry<Date, Date> workMinMaxDates;

	public WorkFileProcessRunner( SystemConfig systemConfig, File directory ) {
		super(systemConfig, directory);
		this.deamonThreadId = Thread.currentThread().getId();
	}

	private String _processDescriptionHash;

	@Override
	public final boolean accept( File file ) {
		// FileProcessRunner's FileFilter is not used.
		// just return true for all
		// and child should not override this function.
		return true;
	}

	public boolean closeWorkFilesExecute() {
		synchronized( processedFilenames ) {
			if( isVerboseDebug() ) {
				StringBuffer sbuf = new StringBuffer();
				for( String filename : processedFilenames.keySet() ) {
					sbuf.append("\t");
					sbuf.append(filename + ": " + processedFilenames.get(filename));
				}
				getLogger().warn(getProcessName()
						+ " " + TimeGranular.Util.getGranDateString(workEntry)
						+ " close work files count: " + processedFilenames.size()
						+ "\n" + sbuf.toString());
			}
			processedFilenames.clear();
		}

		return workStatesRemove(workEntry);
	}

	public void closeProcessedFile( File origfile, boolean executed ) {
		File directory = ( executed || getErrDir() == null ? getBckDir() : getErrDir() );

		if( isVerboseTrace() ) {
			getLogger().warn(getDescription() + " executed: " + executed + " file('" + origfile.getName() + "')" + " will move to '"
					+ directory.getAbsolutePath() + "'");
		}

		File workfile = getWorkFile(origfile);

		String message = null;
		if( directory == null ) {
			workfile.delete();
			message = "deleted: " + getDescription();
			getLogger().info(getDescription() + " executed: " + executed + " file('" + workfile.getName() + "') deleted.");
		} else {
			File renameFile = new File(directory, workfile.getName());

			if( renameFile.exists() )
				renameFile.delete();
			boolean moved = false;
			if( !workfile.renameTo(renameFile) ) {
				java.io.InputStream inputStream = null;
				java.io.OutputStream outputStream = null;
				try {
					int length;
					byte[] buffer = new byte[10240];

					inputStream = new java.io.FileInputStream(workfile);
					outputStream = new java.io.FileOutputStream(renameFile);
					while( ( length = inputStream.read(buffer) ) > 0 )
						outputStream.write(buffer, 0, length);
					moved = true;
				} catch( IOException ioEx ) {
					message = "error: " + getDescription() + ioEx.getMessage() + "\n" + ioEx.getCause().toString();
					getLogger().error(getDescription() + " file('" + workfile.getName() + "') move error.", ioEx);
				} finally {
					try {
						if( inputStream != null )
							inputStream.close();
					} catch( Exception ignored ) {
					}
					try {
						if( outputStream != null )
							outputStream.close();
					} catch( Exception ignored ) {
					}
				}
				if( moved ) {
					workfile.delete();
				}
			} else {
				moved = true;
			}

			if( moved ) {
				message = "moved: " + getDescription();
			}
			getLogger()
					.info(getDescription() + " executed: " + executed + " file('" + workfile.getName() + "') move to '" + directory.getAbsolutePath()
							+ "'.");
		}
		synchronized( processedFilenames ) {
			processedFilenames.put(workfile.getName(), message);
		}
	}

	private FileFilter createFileFilter( final Map.Entry<Date, Date> workEntry ) {
		FileFilter fileFilter = new FileFilter() {
			public boolean accept( File file ) {
				if( file.isFile() && file.canWrite() ) {
					String filename = file.getName();
					Date modDateTime = new Date(file.lastModified());
					FilenameParser nameParser = getFilenameParser();
					if( !nameParser.matched(filename) ) {
						return false;
					}

					Map<String, Object> parsedMap = nameParser.parse(filename);
					if( parsedMap == null ) {
						throw new IllegalStateException("filename: " + filename + " regex: '" + nameParser.getRegex()
								+ "'" + " parsedKeys: " + java.util.Arrays.asList(nameParser.getParsedKeyArray()));
					}

					try {
						// if setted correctly, process date based filtering
						// if not, just return true.
						if( getTimeGranular().isValid() ) {
							if( workEntry == null ) {
								return false;
							}

							Date eventTime = TimeGranular.DEFAULT_EVENTTIME_FORMAT
									.parse((String)parsedMap.get(FILE_EVENTTIME_KEY));
							boolean isIncl = TimeGranular.isInclusive(eventTime, workEntry.getKey(), workEntry.getValue());
							if( isVerboseTrace() ) {
								getLogger().warn(getProcessName() + " file: " + file.getName()
										+ " isIncl: " + isIncl
										+ " baseTime: " + TimeGranular.Util.getDateString(baseDateTime)
										+ " eventTime: " + eventTime
										+ " workEntry: "
										+ TimeGranular.Util.getGranDateString(workEntry));
							}
							return isIncl;
						} else {
							return true;
						}
					} catch( ParseException parseEx ) {
						getLogger().warn("file: " + file.getAbsolutePath(), parseEx);
					}
					return false;
				}
				return false;
			}
		};

		return fileFilter;
	}

	private void resetGranDates() {
		this.granDates = null;
	}

	@Override
	public boolean execute() {
		this.runnerThreadId = Thread.currentThread().getId();
		this.addWorkFileDirCleanupHook();

		if( isVerboseTrace() ) {
			getLogger().warn(getProcessName() + " workIsTimeBase?: " + getTimeGranular().isValid() + "\n timeGranular: "
					+ getTimeGranular() + "\n filenameParser: " + getFilenameParser());
		}

		long start, duration = 0;
		int filesCount = 0;
		File[] files = null;
		while( files == null || initWorkEntry() ) {
			if( granDateStates.size() == 0 ) {
				initWorkStates();
			}

			start = System.currentTimeMillis();
			files = getFilesToProcessing();
			duration = System.currentTimeMillis() - start;
			filesCount = ( files == null || files.length == 0 ) ? 0 : files.length;

			if( filesCount > 0 ) {
				getLogger().info(getProcessName() + "(" + getProcessType() + ")" + " opened - " + "\n files('"
						+ directory.getAbsolutePath() + "') found: " + filesCount
						+ "\n for timeGranular: "
						+ TimeGranular.Util.getGranDateString(workEntry) + "\n on overall {timeGranular: "
						+ TimeGranular.Util.getGranDateString(workMinMaxDates) + " size: " + granDateStates.size()
						+ " baseTime: " + TimeGranular.Util.getDateString(baseDateTime) + "}"
						+ "\n within duration(ms): " + duration
						+ "\n "
						+ getWorkStatesReport());
				break;
			} else {
				if( this.workEntry == null ) {
					reassignWorkBaseTime(duration);
				} else {
					if( isVerboseTrace() ) {
						synchronized( processedFilenames ) {
							int processed = processedFilenames.size();
							int origsize = getCountOrigFiles(workEntry);
							int worksize = getCountWorkFiles(workEntry);
							String message = TimeGranular.Util.getGranDateString(workEntry) + " processed: " + processed + " origsize: "
									+ origsize + " worksize: " + worksize;
							getLogger().warn(getProcessName() + "\n " + message + "\n " + getWorkStatesReport());
						}
					}
					reassignWorkEntry(duration);
				}
			}
		}

		if( filesCount <= 0 ) {
			getLogger().info(getProcessName() + " finally no more files for "
					+ TimeGranular.Util.getGranDateString(workMinMaxDates)
					+ " return to super processor."
					+ "\n files('"
					+ directory.getAbsolutePath() + "') found: " + filesCount
					+ "\n on overall {timeGranular: "
					+ TimeGranular.Util.getGranDateString(workMinMaxDates)
					+ " size: " + granDateStates.size()
					+ " baseTime: " + TimeGranular.Util.getDateString(baseDateTime) + "}"
					+ "\n within duration(ms): " + duration
					+ "\n " + getWorkStatesReport());
			resetGranDates();
			return true;
		}

		Logger logger = getLogger();
		SQLHandler handler = null;
		try {
			logger.info(getDescription() + " ofstart. " + " '" + getDir().getAbsolutePath() + "': " + files.length + " files found. duration(ms): "
					+ duration);

			start = System.currentTimeMillis();
			int count = 0;
			for( File origfile : files ) {
				Process process;

				File file = null;
				try {
					file = workFileCreate(origfile);
					if( isVerboseDebug() ) {
						getLogger().info(getWorkDescMarker() + " create success workFile: " + file.getAbsolutePath());
					}
				} catch( IOException ioEx ) {
					logger.error(getDescription() + " error.",
							ioEx.getCause() != null ? ioEx.getCause() : ioEx);
					return false;
				}

				try {
					if( handler == null )
						handler = getSQLHandler();
					logger.info(getWorkDescMarker() + " SQLHandler created. file('" + file.getName() + "')");

					process = getProcessInstance(handler, file);
				} catch( ProcessException processEx ) {
					logger.error(getDescription() + " error.",
							processEx.getCause() != null ? processEx.getCause() : processEx);
					return false;
				}

				if( process == null ) {
					logger.info(getWorkDescMarker() + " '" + file.getName() + "' ignored.");
					closeProcessedFile(file, true);
				} else if( process != ProcessRunner.EMPTY ) {
					final File final_file = file;
					final boolean final_closingSQLHandler = usingThread;
					final SQLHandler final_handler = handler;

					process = new ProcessWrapper(process, "FileProcess." + file.getName(), "'" + file.getName() + "'") {
						File file = final_file;
						boolean closingSQLHandler = final_closingSQLHandler;
						SQLHandler handler = final_handler;

						public void close() {
							if( closingSQLHandler )
								handler.close();
							super.close();
						}

						public boolean execute() throws InterruptedException {
							try {
								return process.execute();
							} finally {
								closeProcessedFile(file, process.continueProcessing());
							}
						}
					};
					if( final_closingSQLHandler )
						handler = null;

					if( !execute(process) )
						return false;
				}
				count++;
				if( isInterrupted() )
					break;
			}

			logger.info(getDescription() + " ofend. " + " '" + getDir().getAbsolutePath() + "': " + count + " files processed. duration(ms): "
					+ ( System.currentTimeMillis() - start ));
			return true;
		} finally {
			try {
				if( handler != null )
					handler.close();
			} catch( Exception ignored ) {
			}
			closeWorkFilesExecute();
		}
	}

	/* this function may be collision. but return simple positive number */
	private int getSimpleHash( String str ) {
		return ( str.hashCode() & 0x7fffffff );
	}

	@Override
	public String getDescription() {
		return getWorkDescMarker() + " " + getWorkDescription();
	}

	public String getWorkDescMarker() {
		return "[" + getSimpleHash(getWorkDescription()) + "]";
	}

	public String getWorkDescription() {
		if( getTimeGranular().isValid() ) {
			return getProcessName() + " baseTime: " + TimeGranular.Util.getDateString(baseDateTime) + " "
					+ TimeGranular.Util.getGranDateString(workEntry) + " " + getTimeGranular();
		} else {
			return getProcessName();
		}
	}

	@Override
	public com.irt.rbm.tools.Process getProcessInstance( SQLHandler handler, final File workfile ) throws ProcessException {
		if( !workfile.exists() ) {
			getLogger().info(getProcessName() + " t: " + getThreadIdSuffix() + "workfile not exist. : "
					+ workfile.getAbsolutePath());
			return null;
		}

		ProcessImpl dummy = new ProcessImpl("Dummay." + workfile.getName(), "Dummy." + workfile.getName()) {

			@Override
			public boolean execute() {
				Random rand = new Random();
				int max = 10;
				int min = 1;

				if( !workfile.exists() ) {
					throw new IllegalStateException("workfile is not exists. before sleep.");
				}
				int randNum = rand.nextInt(max + 1 - min) + min;
				try {
					Thread.sleep(randNum * 1000L);
				} catch( InterruptedException ignored ) {
				}

				if( !workfile.exists() ) {
					throw new IllegalStateException("workfile is not exists. after sleep.");
				}

				return ( executed = true );
			}
		};

		return dummy;
	}

	private File workFileCreate( File origfile ) throws IOException {
		if( origfile.isDirectory() ) {
			throw new IllegalStateException(getProcessName() + " should supply origfile but is directory: " + origfile.getAbsolutePath());
		}
		return workFileCreate(getWorkDir(), origfile);
	}

	private File workFileCreate( File workDir, File origfile ) throws IOException {
		File workFileDir = getWorkFileDir(workDir);

		if( !workFileDir.exists() ) {
			workFileDir.mkdir();
		}

		if( !workFileDir.isDirectory() ) {
			throw new IllegalStateException("cannot create workFileDir: " + workFileDir.getAbsolutePath());
		}

		long prevFileSize = origfile.length();
		File workfile = null;
		try {
			workfile = moveFileNoRewrite(workFileDir, origfile);
		} catch( RenameFileExistsException renameFileExists ) {
			workfile = new File(workFileDir, origfile.getName());
		}

		if( workfile.exists() && workfile.canWrite() ) {
			if( origfile.exists() && workfile.length() == prevFileSize ) {
				boolean deleted = origfile.delete();
				getLogger().warn(getProcessName() + " workfile exists: " + workfile.getAbsolutePath()
						+ "\n so invoked delete origfile(deleted?:" + deleted + "): "
						+ origfile.getAbsolutePath());
			} else {
				if( prevFileSize != workfile.length() ) {
					throw new IllegalStateException(getProcessName()
							+ "\n prev origfile size: " + prevFileSize
							+ "\n workfile exists with size: " + workfile.length() + " path: " + workfile.getAbsolutePath()
							+ "\n origfile exists with size: " + origfile.length() + " path: " + origfile.getAbsolutePath());
				}
			}
		} else {
			throw new IllegalStateException(getProcessName() + " supposed to be deleted the origfile: " + origfile.getAbsolutePath());
		}

		return workfile;
	}

	private File getWorkDir() {
		if( workDir == null ) {
			workDir = new File(getDir(), ".wrk-" + getProcessType());
		}

		if( !workDir.exists() ) {

			workDir.mkdir();
		}

		return workDir;
	}

	private File getWorkFileDir( File workDir ) {
		File workInstanceDir = new File(workDir, getGlobalProcessId());
		if( !workInstanceDir.exists() ) {
			workInstanceDir.mkdir();
		}

		return workInstanceDir;
	}

	protected String getThreadIdSuffix() {
		return "-" + Long.toString(Thread.currentThread().getId());
	}

	private String _globalProcessId;

	/* unique between jvm too. */
	public String createGlobalProcessId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/* need this value if multiple jvm(multiple execution of RBMToolsDaemon) running in single machine. */
	public String getGlobalProcessId() {
		if( this._globalProcessId == null ) {
			this._globalProcessId = createGlobalProcessId();
		}

		return this._globalProcessId;
	}

	@Override
	public String getProcessName() {
		return getProcessType() + "-" + getGlobalProcessId();
	}

	private String _realProcessType;

	public String getProcessType() {
		// returns real instance( child ) class name
		if( this._realProcessType == null ) {
			this._realProcessType = this.getClass().getSimpleName();
		}

		return this._realProcessType;
	}

	private File getWorkFile( File reffile ) {
		return new File(getWorkFileDir(getWorkDir()), reffile.getName());
	}

	public int getCountOrigFiles( Map.Entry<Date, Date> workEntry ) {
		File[] files = directory.listFiles(createFileFilter(workEntry));
		return files == null ? 0 : files.length;
	}

	public int getCountWorkFiles( Map.Entry<Date, Date> workEntry ) {
		File workFileDir = getWorkFileDir(getWorkDir());
		File[] files = workFileDir.listFiles(createFileFilter(workEntry));
		return files == null ? 0 : files.length;
	}

	private boolean workFileDirCleanupHookInstalled = false;

	private void addWorkFileDirCleanupHook() {
		if( !workFileDirCleanupHookInstalled ) {
			final String globalProcessId = getGlobalProcessId();
			final String name = "workFileDirCleanup";
			final File workFileDir = getWorkFileDir(getWorkDir());
			Thread workFileDirDeleteHook = new Thread() {
				public void run() {
					System.out.println(globalProcessId + "- " + name + " started.");
					if( workFileDir != null && workFileDir.exists() && workFileDir.isDirectory() ) {
						if( workFileDir.list().length == 0 ) {
							System.out.println(globalProcessId + "- " + name + " delete executed. dir: " + workFileDir.getAbsolutePath());
							workFileDir.delete();
						}
					}
					System.out.println(globalProcessId + "- " + name + " ended.");
				}
			};
			workFileDirDeleteHook.setDaemon(true);

			Runtime.getRuntime().addShutdownHook(workFileDirDeleteHook);
			if( workFileDirCleanupHookInstalled = false ) {
				workFileDirCleanupHookInstalled = true;
			}
		}
	}

	public void setVerbose( String verbose ) {
		this.verbose = verbose;
	}

	public String getVerbose() {
		return verbose;
	}

	public int getVerboseLevel() {
		return getVerbose().split("v").length;
	}

	public boolean isVerboseTraceTrace() {
		return ( getVerboseLevel() > 3 );
	}

	public boolean isVerboseTrace() {
		return ( getVerboseLevel() > 2 );
	}

	public boolean isVerboseDebug() {
		return ( getVerboseLevel() > 1 );
	}

	public boolean isVerboseInfo() {
		return ( getVerboseLevel() > 0 );
	}

	public void setTimeGranular( TimeGranular timeGranular ) {
		this.timeGranular = timeGranular;
	}

	public TimeGranular getTimeGranular() {
		return this.timeGranular;
	}

	public void setFilenameParser( FilenameParser parser ) {
		this.filenameParser = parser;
	}

	public FilenameParser getFilenameParser() {
		return this.filenameParser;
	}

	public File[] getFilesToProcessing() {
		return directory.listFiles(createFileFilter(workEntry));
	}

	private String getWorkStatesReport() {
		String stateReport = "removed: " + workStatesCount(STATE_WORKDONE) + " workable: " + workStatesCount(STATE_WORKABLE)
				+ " started: " + workStatesCount(STATE_WORKING);

		if( workEntry == null ) {
			if( isVerboseTrace() ) {
				if( workStatesCount(STATE_WORKING) > 0 ) {
					List<Map.Entry<Date, Date>> working = workStatesList(STATE_WORKING);
					stateReport += "\n working: " + TimeGranular.Util.getGranDatesString(working);
				}
				if( workStatesCount(STATE_WORKDONE) > 0 ) {
					List<Map.Entry<Date, Date>> workdone = workStatesList(STATE_WORKDONE);
					stateReport += "\n workdone: " + TimeGranular.Util.getGranDatesString(workdone);
				}

			}
		}
		return stateReport;
	}

	private void initWorkStates() {
		if( getTimeGranular().isValid() ) {
			if( baseDateTime == null ) {
				initBaseTime();
			}

			if( granDates == null ) {
				List<Date> absDates = getTimeGranular().getConcreteDateList(baseDateTime);
				workMinMaxDates = TimeGranular.getMinMaxDates(absDates);
				granDates = getTimeGranular().granulizeForward(absDates);

				if( isVerboseTrace() ) {
					getLogger().warn(getProcessName() + "\n\tworkEntry: " + TimeGranular.Util.getGranDateString(workEntry)
							+ "\n\tworkMinMaxDates: " + TimeGranular.Util.getGranDateString(workMinMaxDates) + "\n\tabsDt: "
							+ absDates + "\n\tgranDt: " + granDates);
				}
			}

			if( workStatesSize() == 0 ) {
				workStatesReady(granDates);
			}
			if( this.workEntry == null ) {
				initWorkEntry();
			}
		}
	}

	private boolean initBaseTime() {
		try {
			this.baseDateTime = getTimeGranular().getBaseTimeInstance();
		} catch( java.text.ParseException parseEx ) {
			throw new ShouldBaseTimeInstanceParseable(getDescription() + parseEx.getMessage());
		}
		return this.baseDateTime != null;
	}

	private boolean initWorkEntry() {
		this.workEntry = workStatesNextWorkable();
		return this.workEntry != null;
	}

	private int workStatesCount( Object state ) {
		int count = 0;
		synchronized( granDateStates ) {
			for( Map.Entry<Date, Date> workEntry : granDateStates.keySet() ) {
				if( ( (Integer)state == (Integer)granDateStates.get(workEntry) ) ) {
					count++;
				}
			}
		}
		return count;
	}

	private boolean workStatesIsWorkable( Map.Entry<Date, Date> granDate ) {
		synchronized( granDateStates ) {
			return ( STATE_WORKABLE == (Integer)granDateStates.get(granDate) );
		}
	}

	private void workStatesReady( List<Map.Entry<Date, Date>> granDates ) {
		for( Map.Entry<Date, Date> workEntry : granDates ) {
			synchronized( granDateStates ) {
				granDateStates.put(workEntry, 0);
			}
		}
	}

	private boolean workStatesRemove( Map.Entry<Date, Date> granDate ) {
		synchronized( granDateStates ) {
			granDateStates.put(granDate, STATE_WORKDONE);
			return STATE_WORKDONE == (Integer)granDateStates.get(granDate);
		}
	}

	private int workStatesSize() {
		synchronized( granDateStates ) {
			return granDateStates.keySet().size();
		}
	}

	private void workStatesPut( Map.Entry<Date, Date> granDate, Object obj ) {
		synchronized( granDateStates ) {
			granDateStates.put(granDate, obj);
		}
	}

	private List<Map.Entry<Date, Date>> workStatesList( int state ) {
		List<Map.Entry<Date, Date>> ret = new java.util.ArrayList<Map.Entry<Date, Date>>();
		synchronized( granDateStates ) {
			for( Map.Entry<Date, Date> workEntry : granDateStates.keySet() ) {
				if( state == (Integer)granDateStates.get(workEntry) ) {
					ret.add(workEntry);
				}
			}
		}
		return ret;
	}

	private Map.Entry<Date, Date> workStatesNextWorkable() {
		synchronized( granDateStates ) {
			for( Map.Entry<Date, Date> workEntry : granDateStates.keySet() ) {
				if( workStatesIsWorkable(workEntry) ) {
					workStatesPut(workEntry, STATE_WORKING);
					return workEntry;
				}
			}
		}
		return null;
	}

	public static File moveFileNoRewrite( File targetDir, File file ) throws IOException {
		return moveFile(targetDir, file, true);
	}

	public static File moveFileRewrite( File targetDir, File file ) throws IOException {
		return moveFile(targetDir, file, false);
	}

	private static File moveFile( File targetDir, File file, boolean preventRewrite ) throws IOException {
		File renameFile = new File(targetDir, file.getName());

		if( !file.exists() ) {
			if( !renameFile.exists() ) {
				throw new FileNotFoundException("origfile not exists.(maybe already processed by the other jvm runner) : " + file.getAbsolutePath());
			}
		}

		if( preventRewrite ) {
			if( renameFile.exists() && renameFile.length() > 0 && renameFile.length() == file.length() ) {
				throw new RenameFileExistsException("renameFile already exists! : " + renameFile.getAbsolutePath());
			}
		}

		boolean moved = false;
		if( !file.renameTo(renameFile) ) {
			java.io.InputStream inputStream = null;
			java.io.OutputStream outputStream = null;
			try {
				int length;
				byte[] buffer = new byte[10240];

				inputStream = new java.io.FileInputStream(file);
				outputStream = new java.io.FileOutputStream(renameFile);
				while( ( length = inputStream.read(buffer) ) > 0 )
					outputStream.write(buffer, 0, length);
				moved = true;
			} catch( IOException ioEx ) {
				Logger.getRootLogger().error(" file('" + file.getName() + "') move to " + targetDir.getAbsolutePath()
						+ " error." + "\n" + TraceHelper.formatCurrentStacktrace(), ioEx);
				throw ioEx;
			} finally {
				try {
					if( inputStream != null )
						inputStream.close();
				} catch( Exception ignored ) {
				}
				try {
					if( outputStream != null )
						outputStream.close();
				} catch( Exception ignored ) {
				}
			}
			if( moved ) {
				file.delete();
			}
		} else {
			moved = true;
		}

		return renameFile;
	}

	private void reassignWorkBaseTime( long duration ) {
		Date prevBaseTime = this.baseDateTime;
		initBaseTime();
		if( isVerboseDebug() ) {
			getLogger().warn(getProcessName() + "(" + getProcessType() + ")" + " " + " finally workEntry is null."
					+ "\n reassign baseTime"
					+ " from: " + TimeGranular.Util.getDateString(prevBaseTime)
					+ " to: " + TimeGranular.Util.getDateString(baseDateTime)
					+ "\n within duration(ms): " + duration
					+ "\n " + getWorkStatesReport());
		}
		granDateStates.clear();
	}

	private void reassignWorkEntry( long duration ) {
		Map.Entry<Date, Date> prevEntry = this.workEntry;
		workStatesRemove(workEntry);
		initWorkStates();
		if( isVerboseDebug() ) {
			getLogger().info(getProcessName() + " " + " no files." + "(" + getProcessType() + ")" + "\n files('"
					+ directory.getAbsolutePath() + "') found: " + 0 + "\n for timeGranular: "
					+ TimeGranular.Util.getGranDateString(prevEntry) + "\n reassign work workEntry to: "
					+ TimeGranular.Util.getGranDateString(workEntry) + "\n on overall {timeGranular: "
					+ TimeGranular.Util.getGranDateString(workMinMaxDates) + " size: " + granDateStates.size()
					+ " baseTime: " + TimeGranular.Util.getDateString(baseDateTime) + "}"
					+ "\n within duration(ms): " + duration
					+ "\n "
					+ getWorkStatesReport());

		}
	}

	static class RenameFileExistsException extends RuntimeException {
		RenameFileExistsException( String message ) {
			super(message);
		}
	}

	class ShouldBaseTimeInstanceParseable extends RuntimeException {
		ShouldBaseTimeInstanceParseable( String message ) {
			super(message);
		}
	}
}
