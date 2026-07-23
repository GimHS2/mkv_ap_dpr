/*
 *	File Name:	RemoteSyncRunner.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0c	create
 *
**/

package com.irt.rbm.tools;

import com.irt.rbm.tools.FileListerCommand.FindExecutor;
import com.irt.rbm.tools.FileListerCommand.FindOption;
import com.irt.rbm.tools.FileListerCommand.SortBaseType;
import com.irt.sql.SQLHandler;
import com.irt.system.SystemConfig;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * One Way File Sync( Remote ssh -> Local dir )
 * 
 * - Remote ( ssh server on linux is supported )
 * - Locale ( Linux(with bash) or Windows(with powershell) is supported )
 *
 */
public class RemoteSyncRunner extends FileWatchRunner {

	static final String NEWLINE = "\n";

	static final String propContent = ""
			+ NEWLINE + "	systemName								=	" + RemoteSyncRunner.class.getSimpleName() + " Example System"
			+ NEWLINE + "	messageSource							=	com.irt.rbm.mesg.RBMMessages"
			+ NEWLINE + "	tempDirectory							=	${user.dir}/tmp"
			+ NEWLINE + "	defaultSystem							=	FBK"
			// + NEWLINE + " log4j.properties = ${user.dir}/ools/conf/log4j.properties.org"
			// + NEWLINE + " DataSource.dpr.driverClassName = oracle.jdbc.driver.OracleDriver"
			// + NEWLINE + " DataSource.dpr.url = jdbc:oracle:thin:@192.168.0.15:1521/RBM2"
			// + NEWLINE + " DataSource.dpr.username = dpradmin"
			// + NEWLINE + " DataSource.dpr.password = dpradmin"
			// + NEWLINE + " DataSource.dpr.maxActive = 20"
			// + NEWLINE + " DataSource.dpr.maxWait = 10000"

			+ NEWLINE + "	DataSource.emb.driverClassName			=	org.h2.Driver"
			+ NEWLINE + "	DataSource.emb.url						=	jdbc:h2:mem:AZ;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
			// + NEWLINE + " DataSource.emb.url = jdbc:h2:tcp://localhost:9092/~/AZ;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
			+ NEWLINE + "	DataSource.emb.username					=	sa"
			+ NEWLINE + "	DataSource.emb.password					=	"
			+ NEWLINE + "	DataSource.emb.maxActive				=	20"
			+ NEWLINE + "	DataSource.emb.maxWait					=	10000"

			+ NEWLINE + "	FBKSystem.SQLHandler					=	com.irt.rbm.RBMDataHandler"
			+ NEWLINE + "	FBKSystem.dataSource					=	emb"
			+ NEWLINE + "";

	static final String processContent = ""
			+ NEWLINE + "	ProcessRunner.wk_up.className				=	" + RemoteSyncRunner.class.getCanonicalName()
			+ NEWLINE + "	ProcessRunner.wk_up.system					=	FBK"
			+ NEWLINE + "	ProcessRunner.wk_up.directory				=	${user.home}"

			// if backupDirectory not defined then file is not deleted, and the file stays at the original place.
			// if backupDirectory is defined then file is moved to backup directory
			// + NEWLINE + " ProcessRunner.wk.backupDirectory = /tmp/"
			// + NEWLINE + " ProcessRunner.wk.errorDirectory = /tmp/"

			// + NEWLINE + " ProcessRunner.wk.FilenameParser.parsedKeys = transferType;orderKey;eventTime"
			// + NEWLINE + " ProcessRunner.wk.FilenameParser.regex = ^(SRES)_(.*)_(.*).xml$"

			+ NEWLINE + "	ProcessRunner.wk_up.maxThreadCount			=	5"
			+ NEWLINE + "	ProcessRunner.wk_up.sleepMillis			=	150"

			+ NEWLINE + "	ProcessRunner.wk_up.Watch.from				=	now-10days"
			+ NEWLINE + "	ProcessRunner.wk_up.Watch.to				=	now-1minutes"
			+ NEWLINE + "	ProcessRunner.wk_up.Watch.each				=	10days"
			+ NEWLINE + "	ProcessRunner.wk_up.Watch.maxDepth			=	3"
			+ NEWLINE + "	ProcessRunner.wk_up.Watch.maxDormantMillis	=	600000"

			+ NEWLINE + "	ProcessRunner.wk_up.Ftp.username		=	base"
			+ NEWLINE + "	ProcessRunner.wk_up.Ftp.password		=	base"
			+ NEWLINE + "	ProcessRunner.wk_up.Ftp.hostname		=	192.168.0.83"
			+ NEWLINE + "	ProcessRunner.wk_up.Ftp.serverPath		=	/tmp/RemoteSyncRunner.upload-from-base"
			+ NEWLINE + "	ProcessRunner.wk_up.Ftp.useSsh			=	true"
			+ NEWLINE + "	ProcessRunner.wk_up.Ftp.transportType	=	upload"
			+ NEWLINE + "	ProcessRunner.wk_up.Ftp.keepModTime		=	true"

			+ NEWLINE + "	ProcessRunner.wk_up.Verbose.level			=	5"
			+ NEWLINE + "";

	static final String processContent2 = ""
			+ NEWLINE + "	ProcessRunner.wk_dn.className				=	" + RemoteSyncRunner.class.getCanonicalName()
			+ NEWLINE + "	ProcessRunner.wk_dn.system					=	FBK"

			// if backupDirectory not defined then file is not deleted, and the file stays at the original place.
			// if backupDirectory is defined then file is moved to backup directory
			// + NEWLINE + " ProcessRunner.wk_dn.backupDirectory = /tmp/"
			// + NEWLINE + " ProcessRunner.wk_dn.errorDirectory = /tmp/"

			// + NEWLINE + " ProcessRunner.wk_dn.FilenameParser.parsedKeys = transferType;orderKey;eventTime"
			// + NEWLINE + " ProcessRunner.wk_dn.FilenameParser.regex = ^(SRES)_(.*)_(.*).xml$"

			+ NEWLINE + "	ProcessRunner.wk_dn.maxThreadCount		=	20"
			+ NEWLINE + "	ProcessRunner.wk_dn.sleepMillis		=	10000"
			+ NEWLINE + "	ProcessRunner.wk_dn.directory			=	${java.io.tmpdir}" + File.separator + RemoteSyncRunner.class.getSimpleName()

			+ NEWLINE + "	ProcessRunner.wk_dn.Watch.from			=	now-220days"
			+ NEWLINE + "	ProcessRunner.wk_dn.Watch.to			=	now"
			+ NEWLINE + "	ProcessRunner.wk_dn.Watch.each			=	360days"
			+ NEWLINE + "	ProcessRunner.wk_dn.Watch.maxDepth		=	3"

			+ NEWLINE + "	ProcessRunner.wk_dn.Ftp.username		=	base"
			+ NEWLINE + "	ProcessRunner.wk_dn.Ftp.password		=	base"
			+ NEWLINE + "	ProcessRunner.wk_dn.Ftp.hostname		=	192.168.0.83"
			+ NEWLINE + "	ProcessRunner.wk_dn.Ftp.serverPath		=	/tmp"
			+ NEWLINE + "	ProcessRunner.wk_dn.Ftp.useSsh			=	true"
			+ NEWLINE + "	ProcessRunner.wk_dn.Ftp.transportType	=	download"

			+ NEWLINE + "	ProcessRunner.wk_dn.FilenameParser.parsedKeys = namePart;extenPart"
			+ NEWLINE + "	ProcessRunner.wk_dn.FilenameParser.regex = ^(.*)(\\.txt|\\.md|\\.ini|\\.properties|\\.clj)$"

			+ NEWLINE + "	ProcessRunner.wk_dn.Verbose.level		=	6"
			+ NEWLINE + "";

	private static boolean isPortInUse( String host, int port ) throws UnknownHostException, IOException {
		// Assume no connection is possible.
		boolean result = false;

		try {
			( new Socket(host, port) ).close();
			result = true;
		} catch( SocketException e ) {
			// Could not connect.
		}

		return result;
	}

	public static void main( String[] args ) throws Exception {
//		List<String> argList = new ArrayList<String>(java.util.Arrays.asList(args));
//
//		// String template = propContent + processContent + processContent2;
//		String template = propContent + processContent2;
//		// String template = propContent + processContent;
//		Map<String, Object> map = Record.createMap("user.dir", System.getProperty("user.dir"));
//		map.put("user.home", System.getProperty("user.home"));
//		map.put("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
//		String content = PatternRecordFormat.getInstance(template).format(map, null);
//
//		Properties properties = new Properties();
//		properties.load(new StringReader(content));
//
//		String tempDirectoryPath = properties.getProperty("tempDirectory");
//		File tempDirectory = FileUtil.getCreatedDir("tmp", new File(tempDirectoryPath).getParentFile());
//		if( !tempDirectory.exists() )
//			throw new IllegalStateException("'tempDirectory' is mandatory.");
//
//		EmbedDbServerRunner.initEmbedDb(tempDirectory, properties);
//
//		BasicConfigurator.configure();// console logger
//
//		RBMTools t = new RBMTools();
//		t.init(properties);
//
//		if( argList.contains("-single") ) {
//			t.execute(new String[] {
//					"run", "wk"
//			});
//		} else {
//			RBMToolsDaemon d = new RBMToolsDaemon();
//			d.tools = t;
//
//			d.start();
//		}
	}

	FtpProp ftp = new FtpProp();

	public RemoteSyncRunner( SystemConfig systemConfig, File directory ) {
		super(systemConfig, directory);
	}

	private boolean canWriteLocalDirectory() {
		return this.directory.canWrite();
	}

	@Override
	public boolean execute() {

		if( !( "download".equals(ftp.getTransportType()) || "upload".equals(ftp.getTransportType()) ) ) {
			throw new IllegalArgumentException("Ftp.transportType is incorrect. should be 'download' or 'upload'");
		}
		if( "download".equals(ftp.getTransportType()) ) {
			if( !directory.canWrite() )
				throw new IllegalArgumentException("directory '" + directory.getAbsolutePath() + "' is not writable.");
		}
		if( "upload".equals(ftp.getTransportType()) ) {
		}

		getLogger().debug(getDescription() + "." + ftp.getTransportType() + " server(" + ftp.getConnectIdentity() + ") local("
				+ directory.getAbsolutePath() + ")" + " started.");

		Long[] terms = compileCheckBase(System.currentTimeMillis());
		if( terms == null || terms.length < 2 )
			return true;

		long start = System.currentTimeMillis();

		JSch jsch = new JSch();
		String findBaseDir = ftp.getServerPath();
		if( "download".equals(ftp.getTransportType()) ) {
			if( lister.getFindExecutor() == null ) {
				lister.setFindExec(new SshFindExecutor(jsch));
			} else if( !( lister.getFindExecutor() instanceof SshFindExecutor ) ) {
				lister.setFindExec(new SshFindExecutor(jsch));
			}
		} else {
			findBaseDir = directory.getAbsolutePath();

		}

		try {
			FindOption findOpt = lister.createFindOption()
					.withDirectory(findBaseDir)
					.withGtTime(terms[0])
					.withLtTime(terms[1])
					.withMaxDepth(getWatch().getMaxDepth())
					.withBySortBase(SortBaseType.ModAscBase)
					.withRegex(filenameParser.getRegex());

			int lsCount = getRangedFileCount(findOpt);
			getLogger().debug("Executing Remote Command Result: " + lsCount);
			try {
				if( lsCount > 0 ) {
					try {
						List<String> lsLines = getRangedFileLines(findOpt);
						return executeWorks(lsLines, false);
					} catch( IOException ioEx ) {
						getLogger().error(getDescription() + "." + ftp.getTransportType());
					} finally {
						long elapsed = System.currentTimeMillis() - start;
						manageDormantMillis(elapsed);
					}
				} else {
					return false;
				}
			} catch( NumberFormatException nfe ) {
			}
		} finally {
			getLogger().debug(getDescription() + "." + ftp.getTransportType()
					+ " server(" + ftp.getConnectIdentity() + ") local(" + directory.getAbsolutePath() + ")"
					+ " end.");

		}

		return false;
	}

	public boolean executeWorks( Collection<String> lsLines, boolean isRe ) {
		if( lsLines == null )
			return true;

		Process process;

		try {
			process = getProcessInstance(lsLines, getLogger());
		} catch( ProcessException processEx ) {
			getLogger().error(getDescription() + " error.", processEx.getCause() != null ? processEx.getCause() : processEx);
			return false;
		}

		if( process != ProcessRunner.EMPTY ) {
			String processName = this.getClass().getSimpleName().replaceAll("Runner", "") + "." + ftp.getTransportType();
			process = new ProcessWrapper(process, processName, "'" + ftp.getTransportType() + "'") {
				@Override
				public void close() {
					super.close();
				}

				@Override
				public boolean execute() throws InterruptedException {
					return process.execute();
				}
			};

			if( !execute(process) )
				return false;
		}

		return true;
	}

	@Override
	public String getDescription() {
		return RemoteSyncRunner.class.getSimpleName();
	}

	public FtpProp getFtp() {
		return ftp;
	}

	@Override
	public Logger getLogger() {
		return Logger.getLogger(RemoteSyncRunner.class.getSimpleName());
	}

	private Process getProcessInstance( Collection<String> lsLines, Logger logger ) throws ProcessException {
		return new RemoteSyncProcess(ftp, directory, lsLines);
	}

	@Override
	public Process getProcessInstance( SQLHandler handler, File file ) throws ProcessException {
		throw new ProcessException("Does not supported.");
	}

	@Override
	public String getProcessName() {
		String processName = this.getClass().getSimpleName() + "." + ftp.getTransportType() + "#"
				+ ( ftp.getConnectIdentity() + directory.getAbsolutePath() ).toString().hashCode();

		return processName;
	}

	public void setFtp( FtpProp ftp ) {
		this.ftp = ftp;
	}

	class SshFindExecutor implements FindExecutor {

		private JSch jsch;

		public SshFindExecutor( JSch jsch ) {
			this.jsch = jsch;
		}

		@Override
		public List<String> find( FindOption opt, boolean isCountQuery, boolean isWin ) {
			List<String> lines = new ArrayList<String>();

			Map<String, Object> optMap = opt.toMap(isWin);

			String command = null;
			if( isCountQuery ) {
				command = lister.getRangedFileCountCommand(optMap, isWin);
			} else {
				command = lister.getRangedFileLinesCommand(optMap, isWin);
			}

			StringBuilder lineBuffer = new StringBuilder();

			getLogger().debug("Executing Remote Command: \n\t" + command);

			try {

				// String knownHostPubKey = "ssh-rsa
				// AAAAB3NzaC1yc2EAAAADAQABAAABAQDPpMUWdcBpWAZyr1jrqZzyfmBG8f8UuMvuLwEDCg8n8Eu1Zqlhz60VsOJWBq3usUUnUPqCRGNu19PG9lX/Nwtsp8rbSGqq48v1vkHVnulAnKXedA6bO4tE8N+AcHwW6ucPLnSpRurzfrV/vElraRe6DQtGvZBv0g7GNtFKCyF81ZBNGw1ivgBrmmw5sDqDw//zNClTF0QvXb68GamZS5B1+ICQw0/HDLdynk4VLQWBZzUsTJ9XDRwTEJssVTAiGTnfLPIPEA0survVhNJMUOWij6XGbsiRT4AcdVH+5QYsNPL73bSDtTcktp8Y7rR29YiUgQXq3beMl7HcC6TMHGpT";
				// jsch.setKnownHosts(new ByteArrayInputStream(( ftp.getHostname() + " " + knownHostPubKey ).getBytes()));

				// sc.setKnownHosts("~/.ssh/known_hosts");
				// sc.setConfig("StrictHostKeyChecking", "yes");
				jsch.setConfig("StrictHostKeyChecking", "no");

				jsch.addIdentity("~/.ssh/id_rsa");

				Vector names = jsch.getIdentityNames();
				getLogger().debug("identityNames: " + names);

				Session sess = jsch.getSession(ftp.getUsername(), ftp.getHostname(), ftp.getPort());
				sess.setPassword(ftp.getPassword());

				if( !sess.isConnected() )
					sess.connect();

				Channel channel = sess.openChannel("exec");

				( (ChannelExec)channel ).setCommand(command);

				InputStream commandOutput = channel.getInputStream();
				channel.connect();
				int readByte = commandOutput.read();

				while( readByte != 0xffffffff ) {
					if( readByte == '\n' ) {
						lines.add(lineBuffer.toString());
						lineBuffer = new StringBuilder();
					} else {
						lineBuffer.append((char)readByte);
					}

					readByte = commandOutput.read();
				}

				channel.disconnect();
			} catch( IOException ioX ) {
				getLogger().warn(this.getClass().getSimpleName() + " execute " + " " + ioX.getMessage());
				return null;
			} catch( JSchException jschX ) {
				getLogger().warn(this.getClass().getSimpleName() + " execute " + " " + jschX.getMessage());
				return null;
			}

			return lines;
		}

	}

}
