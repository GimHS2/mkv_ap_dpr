/*
 *	File Name:	FileWatchRunner.java
 *	Version:	2.2.1c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2021/07/30		2.2.1c	execute, closeProcessedFile을 상위 클래스인 FileWatchProcessRunner에서 사용하도록 수정
 *	jbaek		2019/08/30		2.2.0c	create
 *
**/

package com.irt.rbm.tools;

import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.rbm.tools.FileListerCommand.FindOption;
import com.irt.sql.SQLHandler;
import com.irt.system.SystemConfig;
import com.irt.util.FileUtil;
import com.irt.util.FilenameParser;
import com.irt.util.TimeTerm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/** deprecated This class is deprecated. use FileWatchProcessRunner. */
public class FileWatchRunner extends FileWatchProcessRunner {

	static final String NEWLINE = "\n";

	static final String propContent = ""
			+ NEWLINE + "	systemName								=	" + FileWatchRunner.class.getSimpleName() + " Example System"
			+ NEWLINE + "	messageSource							=	com.irt.rbm.mesg.RBMMessages"
			+ NEWLINE + "	tempDirectory							=	${user.dir}/tools/tmp"
			// + NEWLINE + " log4j.properties = ${user.dir}/ools/conf/log4j.properties.org"
			+ NEWLINE + "	DataSource.dpr.driverClassName			=	oracle.jdbc.driver.OracleDriver"
			+ NEWLINE + "	DataSource.dpr.url						=	jdbc:oracle:thin:@192.168.0.15:1521/RBM2"
			+ NEWLINE + "	DataSource.dpr.username					=	dpradmin"
			+ NEWLINE + "	DataSource.dpr.password					=	dpradmin"
			+ NEWLINE + "	DataSource.dpr.maxActive				=	20"
			+ NEWLINE + "	DataSource.dpr.maxWait					=	10000"
			+ NEWLINE + "	RBMSystem.SQLHandler					=	com.irt.rbm.RBMDataHandler"
			+ NEWLINE + "	RBMSystem.dataSource					=	dpr"
			+ NEWLINE + "";

	static final String processContent = ""
			+ NEWLINE + "	ProcessRunner.wk.className				=	" + FileWatchRunner.class.getCanonicalName()
			+ NEWLINE + "	ProcessRunner.wk.system					=	RBM"
			+ NEWLINE + "	ProcessRunner.wk.directory				=	${user.home}"

			// if backupDirectory not defined then file is not deleted, and the file stays at the original place.
			// if backupDirectory is defined then file is moved to backup directory
			// + NEWLINE + " ProcessRunner.wk.backupDirectory = /tmp/"
			// + NEWLINE + " ProcessRunner.wk.errorDirectory = /tmp/"

			// + NEWLINE + " ProcessRunner.wk.FilenameParser.parsedKeys = transferType;orderKey;eventTime"
			// + NEWLINE + " ProcessRunner.wk.FilenameParser.regex = ^(SRES)_(.*)_(.*).xml$"

			+ NEWLINE + "	ProcessRunner.wk.maxThreadCount			=	5"
			+ NEWLINE + "	ProcessRunner.wk.sleepMillis			=	150"

			+ NEWLINE + "	ProcessRunner.wk.Watch.from				=	now-5minutes"
			+ NEWLINE + "	ProcessRunner.wk.Watch.to				=	now-1minutes"
			+ NEWLINE + "	ProcessRunner.wk.Watch.each				=	2minutes"
			+ NEWLINE + "	ProcessRunner.wk.Verbose.level			=	5"
			+ NEWLINE + "";

	static final String processContent2 = ""
			+ NEWLINE + "	ProcessRunner.wkback.className				=	" + FileWatchRunner.class.getCanonicalName()
			+ NEWLINE + "	ProcessRunner.wkback.system					=	RBM"
			+ NEWLINE + "	ProcessRunner.wkback.directory				=	${user.home}"

			// if backupDirectory not defined then file is not deleted, and the file stays at the original place.
			// if backupDirectory is defined then file is moved to backup directory
			// + NEWLINE + " ProcessRunner.wkback.backupDirectory = /tmp/"
			// + NEWLINE + " ProcessRunner.wkback.errorDirectory = /tmp/"

			// + NEWLINE + " ProcessRunner.wkback.FilenameParser.parsedKeys = transferType;orderKey;eventTime"
			// + NEWLINE + " ProcessRunner.wkback.FilenameParser.regex = ^(SRES)_(.*)_(.*).xml$"

			+ NEWLINE + "	ProcessRunner.wkback.maxThreadCount			=	20"
			+ NEWLINE + "	ProcessRunner.wkback.sleepMillis			=	10000"

			+ NEWLINE + "	ProcessRunner.wkback.Watch.from			=	now-720days"
			+ NEWLINE + "	ProcessRunner.wkback.Watch.to			=	now-2days"
			+ NEWLINE + "	ProcessRunner.wkback.Watch.each			=	700days"
			+ NEWLINE + "	ProcessRunner.wkback.Watch.maxDepth		=	3"
			+ NEWLINE + "	ProcessRunner.wkback.FilenameParser.parsedKeys = namePart;extenPart"
			+ NEWLINE + "	ProcessRunner.wkback.FilenameParser.regex = ^(.*)(\\.txt|\\.md|\\.ini)$"
			+ NEWLINE + "	ProcessRunner.wkback.Verbose.level		=	6"
			+ NEWLINE + "";

	public static void main( String[] args ) throws Exception {
		List<String> argList = new ArrayList<String>(java.util.Arrays.asList(args));
		if( !argList.contains("-daemon") ) {
			argList.add("-daemon");
		}

		// String template = propContent + processContent + processContent2;
		String template = propContent + processContent2;
		// String template = propContent + processContent;
		Map<String, Object> map = Record.createMap("user.dir", System.getProperty("user.dir"));
		map.put("user.home", System.getProperty("user.home"));
		String content = PatternRecordFormat.getInstance(template).format(map, null);

		BasicConfigurator.configure();// console logger

		RBMTools t = new RBMTools();
		Properties properties = new Properties();
		properties.load(new StringReader(content));

		String tempDirectoryPath = properties.getProperty("tempDirectory");
		File tempDirectory = FileUtil.getCreatedDir("tmp", new File(tempDirectoryPath).getParentFile());
		if( !tempDirectory.exists() )
			throw new IllegalStateException("'tempDirectory' is mandatory.");

		t.init(properties);

		if( argList.contains("-daemon") ) {
			RBMToolsDaemon d = new RBMToolsDaemon();
			d.tools = t;

			d.start();

		} else {
			t.execute(new String[] {
					"run", "wk"
			});
		}
	}

	FilenameParser filenameParser = new FilenameParser();

	private long myNextCheckFrom = -1;

	FileListerCommand lister = new FileListerCommand();

	Verbose verbose = new Verbose();

	WatchList watch = new WatchList();

	private long dormantMillis = 0;

	private long originalSleepMillis = -1;

	public FileWatchRunner( SystemConfig systemConfig, File directory ) {
		super(systemConfig, directory);
	}

	@Override
	public boolean accept( File file ) {
		if( file.isFile() && file.canRead() ) {
			if( getFilenameParser().isParseReady() ) {
				return getFilenameParser().matched(file.getName());
			} else {
				return true;
			}
		}

		return false;
	}

	Long[] compileCheckBase( long refBase ) {
		Long[] checks = getWatch().compilePeriod(refBase, myNextCheckFrom);
		if( checks == null ) {
			this.myNextCheckFrom = -1;
		} else {
			this.myNextCheckFrom = checks[2];
		}
		return checks;
	}

	@SuppressWarnings("unused")
	private File getCreatedRunnerTempDirectory() {
		File runnerTempDir = FileUtil.getCreatedDir(systemConfig.getTemporaryDirectory(), "." + getProcessName());

		String identityFileName = ".FileWatchRunner";
		try {
			File identityFile = FileUtil.getCreatedFile(identityFileName, runnerTempDir);
			if( !identityFile.exists() || identityFile.length() <= 0 ) {
				FileWriter fw = new FileWriter(identityFile);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(Record.createMap("directory", directory).toString());
				bw.flush();
				bw.close();
			}
		} catch( IOException ioEx ) {
			getLogger().error(getDescription() + " ProcessRunner identity save error.", ioEx);
		}
		return runnerTempDir;
	}

	@Override
	public String getDescription() {
		return "FileWatchRunner";
	}

	@Override
	public FilenameParser getFilenameParser() {
		return filenameParser;
	}

	@Override
	public Logger getLogger() {
		return Logger.getLogger("com.irt.rbm.tools.FileWatchRunner");
	}

	@Override
	public Process getProcessInstance( SQLHandler handler, File file ) throws ProcessException {
		try {
			Process dummy = new ProcessImpl("DUMMY." + file.getAbsolutePath(), "DUMMY") {
				@Override
				public boolean continueProcessing() {
					return executed = true;
				}
			};

			return dummy;
		} finally {
			getLogger().debug(getDescription() + " '" + directory.getAbsolutePath() + "': " + "'" + file.getAbsolutePath() + "' modtime("
					+ isoTime(file.lastModified()) + ") process instantiated.");
		}
	}

	@Override
	public String getProcessName() {
		return "FileWatchRunner" + "#" + directory.toString().hashCode();
	}

	int getRangedFileCount( FindOption findOption ) {
		return lister.findRangedFileCount(findOption);
	}

	List<String> getRangedFileLines( FindOption findOption ) throws IOException {
		List<String> paths = lister.findRangedFileLines(findOption);

		if( paths == null ) {
			if( verbose.isDebug() ) {
				getLogger().debug("Executing Command result: " + "\n" + "null" + "");
			}
		} else {
			if( verbose.isDebug() ) {
				if( paths.size() == 1 ) {
					getLogger().debug("Executing Command result: " + "\n"
							+ "\t" + "[0]: " + paths.get(0));
				} else {
					int max = paths.size() - 1;
					int digits = (int)( Math.log10(max) + 1 );
					String min_paded = digits > 1 ? String.format("%1$-" + ( digits - 1 ) + "s", " ") + 0 : "0";
					getLogger().debug("Executing Command result: "
							+ "\n\t" + "[" + min_paded + "]: " + ( paths.size() > 1 ? paths.get(0) : null )
							+ "\n\t" + "[" + max + "]: " + paths.get(max));
				}
			}
		}

		return paths;
	}

	private TimeTerm getTerm( String timeDef ) {
		return new TimeTerm(timeDef, watch.isTrim());
	}

	public Verbose getVerbose() {
		return verbose;
	}

	public WatchList getWatch() {
		return watch;
	}

	String isoTime( long ts ) {
		return new com.irt.data.Timestamp(ts).getIsoLocal();
	}

	/** can run file process with read access only.( does not need write access ) */
	@Override
	public boolean isWatching() {
		return true;
	}

	void manageDormantMillis( long elapsed ) {
		if( TimeUnit.MILLISECONDS.toSeconds(elapsed) < 5 ) {
			if( dormantMillis < getWatch().getMaxDormantMillis() )
				dormantMillis = dormantMillis + 1000L;
		} else {
			dormantMillis = dormantMillis / 5000L;
		}

		if( this.originalSleepMillis < 0 )
			this.originalSleepMillis = sleepMillis;

		if( dormantMillis > sleepMillis ) {
			this.sleepMillis = dormantMillis;
			getLogger().debug(getDescription()
					+ " sleepMillis(" + sleepMillis + ")"
					+ " dormancy(" + dormantMillis + ")"
					+ " maxDormancy(" + getWatch().getMaxDormantMillis() + ")");
		} else {
			if( this.sleepMillis != this.originalSleepMillis )
				this.sleepMillis = this.originalSleepMillis;
		}
	}

	@Override
	public void setFilenameParser( FilenameParser filenameParser ) {
		this.filenameParser = filenameParser;
	}

	public void setVerbose( Verbose verbose ) {
		this.verbose = verbose;
	}

	public void setWatch( WatchList watch ) {
		this.watch = watch;
	}

	static class FileAcceptor {

		/**
		 * @return whether file can be write.
		 */
		@SuppressWarnings("unused")
		private static boolean isWriteCompleted( File file ) {
			RandomAccessFile stream = null;
			try {
				stream = new RandomAccessFile(file, "rw");// wether this process can write or not/ but maybe you dont have write permission
				return true;
			} catch( Exception ex ) {
				if( Logger.getLogger(FileAcceptor.class).isTraceEnabled() ) {
					if( file.canWrite() ) {
						Logger.getLogger(FileAcceptor.class).trace("file '" + file.getName() + "' is not completely written");
					} else {
						Logger.getLogger(FileAcceptor.class).trace("file '" + file.getName() + "' is no write permission by current user");
					}
				}
			} finally {
				if( stream != null ) {
					try {
						stream.close();
					} catch( IOException ioEx ) {
						Logger.getRootLogger().error("Exception during closing file '" + file.getName() + "'", ioEx);
					}
				}
			}

			return false;
		}

		@SuppressWarnings("unused")
		private static boolean isWriteDormant( File file, long dormantMillis ) {
			return isWriteDormant(file, dormantMillis, System.currentTimeMillis());
		}

		private static boolean isWriteDormant( File file, long dormantMillis, long timeAgainst ) {
			return ( timeAgainst > file.lastModified() + dormantMillis );
		}
	}

	interface TermMaker {

		Long[] compilePeriod( long base, long nextFrom );

		String getEach();

		String getFrom();

		String getTo();

		boolean isTrim();

		void setEach( String each );

		void setFrom( String from );

		void setTo( String to );

		void setTrim( boolean trim );

	}

	class TermMakerImpl implements TermMaker {
		String each;

		String from;

		String to;

		boolean trim = true;

		@Override
		public Long[] compilePeriod( long base, long nextFrom ) {
			Calendar fromT = Calendar.getInstance();
			Calendar toT = Calendar.getInstance();

			long fromBase = nextFrom > 0 ? nextFrom : getTerm(from).compileTime(base);

			fromT.setTimeInMillis(fromBase);
			fromT.set(Calendar.SECOND, 00);
			fromT.set(Calendar.MILLISECOND, 0);

			long max = getTerm(to != null ? to : "now").compileTime(base);

			long ltBase = getTerm(each != null ? each : "now").compileTime(fromT.getTimeInMillis());

			if( ltBase > max ) {
				nextFrom = -1;// start again.
				ltBase = max;
			} else {
				nextFrom = ltBase;
			}

			toT.setTimeInMillis(ltBase);
			toT.set(Calendar.SECOND, 01);
			toT.set(Calendar.MILLISECOND, 0);

			return new Long[] { fromT.getTimeInMillis(), toT.getTimeInMillis(), nextFrom };
		}

		@Override
		public String getEach() {
			return each;
		}

		@Override
		public String getFrom() {
			return from;
		}

		@Override
		public String getTo() {
			return to;
		}

		@Override
		public boolean isTrim() {
			return trim;
		}

		@Override
		public void setEach( String each ) {
			this.each = each;
		}

		@Override
		public void setFrom( String from ) {
			this.from = from;
		}

		@Override
		public void setTo( String to ) {
			this.to = to;
		}

		@Override
		public void setTrim( boolean trim ) {
			this.trim = trim;
		}

		@Override
		public java.lang.String toString() {
			return "{TermMaker:{"
					+ " each: " + each
					+ " from: " + from
					+ " to: " + to
					+ " trim: " + trim
					+ "}}";
		}
	}

	public class Verbose {
		private static final int VERBOSE_NONE = 0;
		private static final int VERBOSE_FATAL = 1;
		private static final int VERBOSE_ERROR = 2;
		private static final int VERBOSE_WARN = 3;
		private static final int VERBOSE_INFO = 4;
		private static final int VERBOSE_DEBUG = 5;
		private static final int VERBOSE_TRACE = 6;
		private static final int VERBOSE_TRACETRACE = 7;

		int level = VERBOSE_INFO;

		public int getLevel() {
			return level;
		}

		public boolean isDebug() {
			return ( level >= VERBOSE_DEBUG );
		}

		public boolean isError() {
			return ( level >= VERBOSE_ERROR );
		}

		public boolean isFatal() {
			return ( level >= VERBOSE_FATAL );
		}

		public boolean isInfo() {
			return ( level >= VERBOSE_WARN );
		}

		public boolean isNone() {
			return ( level == VERBOSE_NONE );
		}

		public boolean isTrace() {
			return ( level >= VERBOSE_TRACE );
		}

		public boolean isTraceTrace() {
			return ( level >= VERBOSE_TRACETRACE );
		}

		public boolean isWarn() {
			return ( level >= VERBOSE_WARN );
		}

		public void setLevel( int level ) {
			this.level = level;
		}
	}

	public interface WatchEntrySelector {
		public final int CONTINUE = 0;
		public final int BREAK = 1;

		/**
		 * <p>
		 * The <code>select</code> method will be invoked in <code>ls</code>
		 * method for each file entry. If this method returns BREAK,
		 * <code>ls</code> will be canceled.
		 *
		 * @param entry
		 *            one of entry from ls
		 * @return if BREAK is returned, the 'ls' operation will be canceled.
		 */
		public int select( String filename );
	}

	public class WatchList implements TermMaker {
		public static final long MARK_NORMAL_FILE = -5;

		public static final long MARK_READONLY_FILE = -6;

		public static final long MARK_FAILED_FILE = -7;

		public static final long MARK_RETRYRETIRED_FILE = -8;

		private int maxDepth = FileListerCommand.FindOption.DEFAULT_MAX_DEPTH;

		private long maxDormantMillis = TimeUnit.MINUTES.toMillis(10);

		private long normalCount = 0;

		private long readonlyCount = 0;

		private long failedCount = 0;

		private long processedCount = 0;

		private int maxRetryReadonly = 10;

		private int maxRetryFailed = 0;

		private Map<String, Long> fileProcessing;

		private Map<String, Long> fileReProcessing;

		private TermMaker termDelegate = new TermMakerImpl();

		WatchList() {
			this.fileProcessing = java.util.Collections.synchronizedMap(new java.util.HashMap<String, Long>(100));
			this.fileReProcessing = java.util.Collections.synchronizedMap(new java.util.HashMap<String, Long>(50));
		}

		@Override
		public Long[] compilePeriod( long base, long nextFrom ) {
			return termDelegate.compilePeriod(base, nextFrom);
		}

		public boolean containsKey( String key ) {
			return fileProcessing.containsKey(key);
		}

		public Long get( String key ) {
			return fileProcessing.get(key);
		}

		@Override
		public String getEach() {
			return termDelegate.getEach();
		}

		public List<String> getEvictableKeys( long timeAgainst ) {
			List<String> list = new ArrayList<String>();
			for( String key : fileProcessing.keySet() ) {
				if( isEvictable(key, timeAgainst) )
					list.add(key);
			}
			return list;
		}

		public long getFailedCount() {
			return failedCount;
		}

		@Override
		public String getFrom() {
			return termDelegate.getFrom();
		}

		public int getMaxDepth() {
			return maxDepth;
		}

		public long getMaxDormantMillis() {
			return maxDormantMillis;
		}

		public int getMaxRetryFailed() {
			return maxRetryFailed;
		}

		public int getMaxRetryReadonly() {
			return maxRetryReadonly;
		}

		public long getNormalCount() {
			return normalCount;
		}

		public Map<String, Long> getPro() {
			return fileProcessing;
		}

		public long getProcessedCount() {
			return processedCount;
		}

		public long getProcessingCount() {
			return getPro().size() - getProcessedCount();
		}

		public long getReadonlyCount() {
			return readonlyCount;
		}

		public long getRegisteredCount() {
			return getPro().size();
		}

		public Map<String, Long> getRePro() {
			return fileReProcessing;
		}

		public long getReProCount() {
			return fileReProcessing.size();
		}

		/** @return curr retried count */
		public Long getRetriedCount( String key ) {
			return fileReProcessing.containsKey(key) ? fileReProcessing.get(key) : 0L;
		}

		@Override
		public String getTo() {
			return termDelegate.getTo();
		}

		private boolean isEvictable( String key, long timeAgainst ) {
			if( isProcessed(key)
					&& fileProcessing.get(key) < timeAgainst ) {// old
				return true;
			} else {
				return false;
			}
		}

		public boolean isProcessed( String key ) {
			boolean hasModifiedTime = fileProcessing.get(key) > 0;
			return hasModifiedTime;
		}

		public boolean isProType( String key, long marker ) {
			return fileProcessing.containsKey(key) && fileProcessing.get(key) == marker;
		}

		@Override
		public boolean isTrim() {
			return termDelegate.isTrim();
		}

		/** @return marker's count */
		public Long putFile( String key, long marker ) {
			long ret = 0L;

			long typeWas = get(key);

			if( typeWas > 0 ) {
				ret = -1L;// processed
			} else {
				if( typeWas != marker ) {
					if( MARK_NORMAL_FILE == marker ) {
						ret = normalCount++;
					} else if( MARK_READONLY_FILE == marker ) {
						ret = readonlyCount++;
					} else if( MARK_FAILED_FILE == marker ) {
						ret = failedCount++;
					}
					fileProcessing.put(key, marker);
				}
			}

			return ret;
		}

		/** @return prev retried count */
		public Long putProcessed( String key, long fileLastModified ) {
			try {
				if( getRetriedCount(key) > 0 ) {
					return fileReProcessing.remove(key);
				} else {
					return (long)0;
				}
			} finally {
				Long marker = fileProcessing.put(key, fileLastModified);

				if( marker != null ) {
					if( MARK_NORMAL_FILE == marker ) {
						normalCount--;
					} else if( MARK_READONLY_FILE == marker ) {
						readonlyCount--;
					} else if( MARK_FAILED_FILE == marker ) {
						failedCount--;
					}
					processedCount++;
				}
			}
		}

		@Override
		public void setEach( String each ) {
			this.termDelegate.setEach(each);
		}

		@Override
		public void setFrom( String from ) {
			this.termDelegate.setFrom(from);
		}

		public void setMaxDepth( int maxDepth ) {
			this.maxDepth = maxDepth;
		}

		public void setMaxDormantMillis( long maxDormantMillis ) {
			this.maxDormantMillis = maxDormantMillis;
		}

		public void setMaxRetryFailed( int maxRetryFailed ) {
			this.maxRetryFailed = maxRetryFailed;
		}

		public void setMaxRetryReadonly( int maxRetryReadonly ) {
			this.maxRetryReadonly = maxRetryReadonly;
		}

		@Override
		public void setTo( String to ) {
			this.termDelegate.setTo(to);
		}

		@Override
		public void setTrim( boolean trim ) {
			this.termDelegate.setTrim(trim);
		}

		@Override
		public String toString() {
			return super.toString() + "("
					+ " processed: " + this.getProcessedCount()
					+ " processing: " + this.getProcessingCount()
					+ " readonly:" + this.getReadonlyCount()
					+ ")";
		}

		/** @return whether evicted or not */
		public boolean tryEvict( String key, long timeAgainst ) {
			if( isEvictable(key, timeAgainst) ) {
				processedCount--;
				fileProcessing.remove(key);
				return true;
			} else {
				return false;
			}
		}

		public int tryEvicts( long timeAgainst ) {
			int count = 0;
			for( String key : getEvictableKeys(timeAgainst) ) {
				count = tryEvict(key, timeAgainst) ? count++ : count;
			}
			return count;
		}
	}

}
