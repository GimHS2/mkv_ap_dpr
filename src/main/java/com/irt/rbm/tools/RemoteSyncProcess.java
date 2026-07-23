/*
 *	File Name:	RemoteSyncProcess.java
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

import com.irt.rbm.tools.FileListerCommand.LsHeader;
import com.irt.util.FileUtil;
import com.irt.util.SFTP;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class RemoteSyncProcess implements Process {

	Logger logger = Logger.getLogger(RemoteSyncProcess.class);

	private Collection<String> lsLines;

	private FtpProp ftp;

	private SFTP sftpManager;
	private File directory;

	private boolean executed = false;

	public RemoteSyncProcess( FtpProp ftpProps, File directory, Collection<String> lsLines ) {
		this.ftp = ftpProps;
		this.lsLines = lsLines;
		this.directory = directory;
	}

	/** cd to dir and create target directory if needed. */
	private String cdToParentDir( String targetFileFull, boolean createDir ) {

		String targetPath = FilenameUtils.getPath(targetFileFull);
		try {
			if( createDir ) {

				if( FilenameUtils.directoryContains(ftp.getServerPath(), "/" + targetPath) ) {
					try {

						String pathFromBasePath = ( "/" + targetPath ).replaceFirst(ftp.getServerPath(), "");
						String[] parts = pathFromBasePath.split("/");
						if( parts != null && parts.length > 0 ) {
							String nodePath = null;
							for( int i = 0; i < parts.length; i++ ) {
								nodePath = ( nodePath == null ? "" : nodePath ) + ( parts[i] + "/" );
								tryRemoteMkdir(ftp.getServerPath() + nodePath);
							}
						}

					} catch( SftpException e ) {
						getLogger().debug("" + ftp.getConnectIdentity() + " :"
								+ " target(" + targetFileFull + ")"
								+ " targetPath(" + targetPath + ")", e);
					}
				}
			}

			getProcessSftp().changeWorkingDirectory("/" + targetPath);

			return getProcessSftp().pwd();
		} catch( IOException ioEx ) {
			getLogger().debug("" + ftp.getConnectIdentity() + " :"
					+ " target(" + targetFileFull + ")"
					+ " targetPath(" + targetPath + ")", ioEx);
		}

		return null;
	}

	@Override
	public void close() {
		try {
			getProcessSftp().disconnect();
		} catch( IOException ignored ) {
		}
	}

	@Override
	public boolean continueProcessing() {
		return executed;
	}

	private boolean downloadFile( ChannelSftp sftpClient, String remoteFileFull, File localFile ) throws IOException {
		String remoteFileName = FilenameUtils.getName(remoteFileFull);
		File localFileParent = manageLocalParentDir(localFile);

		String currentDir = cdToParentDir(remoteFileFull, true);

		if( localFileParent.exists() ) {
			retrieveFile(sftpClient, remoteFileName, localFileParent.getAbsolutePath());
		} else {
			throw new FileNotFoundException("local parent dir cannot be found: " + localFileParent.getAbsolutePath());
		}

		return true;
	}

	@Override
	public boolean execute() throws InterruptedException {
		getLogger().debug(getDescription() + "." + ftp.getTransportType() + " start(" + lsLines.size() + ")");

		boolean isDownload = false;
		if( "download".equals(ftp.getTransportType()) ) {
			isDownload = true;
		} else if( "upload".equals(ftp.getTransportType()) ) {
			isDownload = false;
		} else {
			throw new IllegalArgumentException("illegal argument: " + ftp.getTransportType());
		}

		int already = 0;
		int count = 0;
		try {
			SFTP ss = getProcessSftp();
			long start = System.currentTimeMillis();

			Map<String, Object> md5remotes = getMd5sumRemotes();
			Map<String, Object> md5locals = getMd5sumLocals(true);

			for( String lsLine : lsLines ) {
				String sourceFileFull = FileListerCommand.getLsLineData(lsLine, LsHeader.Name.colidx());

				String targetFileFull = isDownload
						? pathToLocal(sourceFileFull, ftp.getServerPath(), directory.getAbsolutePath())
						: pathToRemote(sourceFileFull, directory.getAbsolutePath(), ftp.getServerPath());

				String md5Local = isDownload ? (String)md5locals.get(targetFileFull) : (String)md5locals.get(sourceFileFull);
				String md5Remote = isDownload ? (String)md5remotes.get(sourceFileFull) : (String)md5remotes.get(targetFileFull);

				boolean alreadySame = false;
				if( md5Local != null && md5Local.equals(md5Remote) ) {
					alreadySame = true;
				}

				if( !alreadySame ) {
					try {
						if( isDownload
								? downloadFile(ss.getClient(), sourceFileFull, new File(targetFileFull))
								: uploadFile(ss.getClient(), targetFileFull, new File(sourceFileFull)) )
							if( isDownload ) {
								getLogger().debug(ftp.getTransportType()
										+ " remote(" + sourceFileFull + ")"
										+ " local(" + targetFileFull + ")");
							} else {
							}
						count++;
					} catch( Exception ex ) {
						throw new IllegalStateException(
								"faile to " + ftp.getTransportType() + ". " + ftp.getConnectIdentity()
										+ " target(" + targetFileFull + ")"
										+ " source(" + sourceFileFull + ")"
										+ " lsLines: " + lsLine,
								ex);
					}
				} else {
					already++;
					if( getLogger().isTraceEnabled() )
						getLogger().trace(getDescription() + " target(" + targetFileFull + ")" + " already same(" + sourceFileFull + ")");
				}
			}
			long elapsed = System.currentTimeMillis() - start;

			getLogger()
					.debug(getDescription() + "." + ftp.getTransportType() + " elapsed(seconds: " + TimeUnit.MILLISECONDS.toSeconds(elapsed) + ")");
		} catch( IOException e ) {
			getLogger().warn(this.getClass().getSimpleName() + " ", e);
		}

		String direction = isDownload
				? "'" + ftp.getConnectIdentity() + "'->'" + directory.getAbsolutePath() + "'"
				: "'" + directory.getAbsolutePath() + "'->'" + ftp.getConnectIdentity() + "'";

		getLogger().debug(
				getDescription() + " " + direction + ": "
						+ count + "/" + lsLines.size()
						+ " already(" + already + ")"
						+ " files executed.(" + ftp.getTransportType() + ")");

		return executed = true;
	}

	@Override
	public String getDescription() {
		return this.getClass().getSimpleName() + "." + ftp.getTransportType();
	}

	private String getFileChecksum( MessageDigest digest, File file ) throws IOException {
		// Get file input stream for reading the file content
		FileInputStream fis = new FileInputStream(file);

		// Create byte array to read data in chunks
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;

		// Read file data and update in message digest
		while( ( bytesCount = fis.read(byteArray) ) != -1 ) {
			digest.update(byteArray, 0, bytesCount);
		}
		;

		// close the stream; We don't need it now.
		fis.close();

		// Get the hash's bytes
		byte[] bytes = digest.digest();

		// This bytes[] has bytes in decimal format;
		// Convert it to hexadecimal format
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < bytes.length; i++ ) {
			sb.append(Integer.toString(( bytes[i] & 0xff ) + 0x100, 16).substring(1));
		}

		// return complete hash
		return sb.toString();
	}

	private Logger getLogger() {
		return Logger.getLogger(this.getClass());
	}

	public Map<String, Object> getMd5sumLocals( boolean createFile ) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		for( String lsLine : lsLines ) {
			String filepath = FileListerCommand.getLsLineData(lsLine, LsHeader.Name.colidx());
			String localFileFull = resolveToLocalFileFull(filepath);

			String md5 = null;
			File localFile = new File(localFileFull);
			if( createFile ) {
				md5 = manageMd5File(localFile, directory);
			} else {
				try {
					md5 = getFileChecksum(MessageDigest.getInstance("MD5"), localFile);
				} catch( NoSuchAlgorithmException ignored ) {
				}
			}

			map.put(localFileFull, md5);
		}

		return map;
	}

	public Map<String, Object> getMd5sumRemotes() throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		SFTP ss = getProcessSftp();
		StringBuilder sbuf = new StringBuilder();
		for( String lsLine : lsLines ) {
			String filepath = FileListerCommand.getLsLineData(lsLine, LsHeader.Name.colidx());
			String remoteFileFull = resolveToRemoteFileFull(filepath);

			sbuf.append(remoteFileFull);
			sbuf.append(" ");
		}
		String md5remotes = sendSshCommand(ss.getSession(), String.format(" printf '%s' | xargs -d' ' -I{} md5sum {} ", sbuf.toString()));

		if( md5remotes != null && md5remotes.length() > 0 ) {
			for( String md5remote : md5remotes.split("\n") ) {
				String[] parts = md5remote.split("\\s+");
				if( parts != null && parts.length == 2 ) {
					String value = parts[0];
					String file = parts[1];
					map.put(file, value);
				}
			}
		}

		return map;
	}

	@Override
	public String getProcessName() {
		return this.getClass().getSimpleName() + "." + ftp.getTransportType() + "#"
				+ ( ftp.getConnectIdentity() + directory.getAbsolutePath() ).toString().hashCode();
	}

	private SFTP getProcessSftp() throws IOException {

		if( this.sftpManager == null )
			this.sftpManager = new SFTP(ftp.getHostname(), ftp.getUsername(), ftp.getPassword());

		try {
			if( this.sftpManager.getClient() == null ) {
				this.sftpManager.connect();
			}
		} catch( IOException e ) {
			getLogger().warn(this.getClass().getSimpleName() + " 1 ", e);
		}

		return this.sftpManager;
	}

	private boolean isRemoteExists( String path ) throws SftpException, IOException {
		boolean exists = false;
		try {
			getProcessSftp().getClient().lstat(path);
			exists = true;
		} catch( SftpException e ) {
			if( e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE ) {
				exists = false;
			} else {
				throw e;
			}
		}

		return exists;

	}

	private File manageLocalParentDir( File localFile ) {
		File localFileParent = localFile.getParentFile();

		localFileParent.mkdirs();

		if( localFileParent.exists() && localFileParent.isDirectory() ) {
			return localFileParent;
		}

		return null;
	}

	private String manageMd5File( File sourceFile, File baseDir ) throws IOException {
		String md5 = null;

		if( sourceFile.exists() ) {
			if( !sourceFile.getName().endsWith(".MD5") && !".MD5".equals(sourceFile.getParentFile().getName()) ) {
				File md5Dir = FileUtil.getCreatedDir(".MD5", baseDir);
				String pathPart = sourceFile.getAbsolutePath().replaceAll(baseDir.getAbsolutePath(), "");

				File md5LocalFile = FileUtil.getCreatedFile(pathPart + ".MD5", md5Dir);

				try {
					md5 = getFileChecksum(MessageDigest.getInstance("MD5"), sourceFile);
				} catch( NoSuchAlgorithmException ignored ) {
				}
				FileUtil.writeFileContent(md5LocalFile, md5);
			} else {
				getLogger().warn("prevent creating duplicated '.MD5' dir. sourceFile(" + sourceFile.getAbsolutePath() + ")");
			}
		}

		return md5;
	}

	private String pathToLocal( String remoteFileFull, String remoteBaseDir, String localBaseDir ) {

		String pathPartFromBaseDir = remoteFileFull.replaceAll(remoteBaseDir, "");
		if( pathPartFromBaseDir != null && pathPartFromBaseDir.length() > 0 )
			pathPartFromBaseDir = pathPartFromBaseDir.replaceAll("^/", "");

		return FilenameUtils.concat(localBaseDir, pathPartFromBaseDir);
	}

	private String pathToRemote( String localFileFull, String localBaseDir, String remoteBaseDir ) {

		String pathPartFromBaseDir = localFileFull.replaceAll(localBaseDir, "");
		if( pathPartFromBaseDir != null && pathPartFromBaseDir.length() > 0 )
			pathPartFromBaseDir = pathPartFromBaseDir.replaceAll("^/", "");

		return FilenameUtils.concat(remoteBaseDir, pathPartFromBaseDir);
	}

	String resolveToLocalFileFull( String filepath ) {
		String localFileFull = null;
		if( "download".equals(ftp.getTransportType()) ) {
			localFileFull = pathToLocal(filepath, ftp.getServerPath(), directory.getAbsolutePath());
		} else if( "upload".equals(ftp.getTransportType()) ) {
			localFileFull = filepath;
		} else {
			throw new IllegalArgumentException();
		}

		return localFileFull;
	}

	String resolveToRemoteFileFull( String filepath ) {
		String remoteFileFull = null;
		if( "download".equals(ftp.getTransportType()) ) {
			remoteFileFull = filepath;
		} else if( "upload".equals(ftp.getTransportType()) ) {
			remoteFileFull = pathToRemote(filepath, directory.getAbsolutePath(), ftp.getServerPath());
		} else {
			throw new IllegalArgumentException();
		}

		return remoteFileFull;
	}

	public void retrieveFile( ChannelSftp sftp, String serverFileName, String localPath ) throws IOException {
		File localFile = new File(localPath, serverFileName);

		OutputStream outputStream = new FileOutputStream(localFile);

		try {
			sftp.get(serverFileName, outputStream);
			if( ftp.isKeepModTime() ) {
				localFile.setLastModified(sftp.lstat(serverFileName).getMTime() * 1000L);
			}
		} catch( SftpException sftpEx ) {
			if( logger.isTraceEnabled() )
				logger.trace(sftpEx);
			throw new IOException(sftpEx.getMessage(), sftpEx);
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

	private String sendSshCommand( Session session, String command ) {
		StringBuilder outputBuffer = new StringBuilder();
		try {
			Channel channel = session.openChannel("exec");

			( (ChannelExec)channel ).setCommand(command);

			InputStream commandOutput = channel.getInputStream();
			channel.connect();
			int readByte = commandOutput.read();

			while( readByte != 0xffffffff ) {
				outputBuffer.append((char)readByte);
				readByte = commandOutput.read();
			}

			channel.disconnect();
		} catch( IOException ioX ) {
			logger.warn(ioX.getMessage());
			return null;
		} catch( JSchException jschX ) {
			logger.warn(jschX.getMessage());
			return null;
		}

		return outputBuffer.toString();
	}

	public void storeFile( ChannelSftp sftpClient, String serverFilename, File localFile ) throws IOException {
		java.io.InputStream inputStream = new java.io.FileInputStream(localFile);

		try {
			sftpClient.put(inputStream, serverFilename);

			if( ftp.isKeepModTime() ) {
				sftpClient.setMtime(serverFilename, (int)( localFile.lastModified() / 1000L ));
			}
		} catch( SftpException sftpEx ) {
			if( logger.isTraceEnabled() )
				logger.trace(sftpEx);
		} finally {
			try {
				if( inputStream != null )
					inputStream.close();
			} catch( Exception ignored ) {
			}
		}
	}

	private boolean tryRemoteMkdir( String path ) throws SftpException, IOException {
		boolean exists = false;
		try {
			getProcessSftp().getClient().lstat(path);
			exists = true;
		} catch( SftpException e ) {
			if( e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE ) {
				try {
					getProcessSftp().getClient().mkdir(path);
					exists = true;
				} catch( SftpException e2 ) {
					if( e2.id == ChannelSftp.SSH_FX_NO_SUCH_FILE ) {
						exists = false;
					}
				}
			} else {
				exists = false;
			}
		}

		return exists;
	}

	private boolean uploadFile( ChannelSftp sftpClient, String remoteFileFull, File localFile ) throws IOException {
		cdToParentDir(remoteFileFull, true);

		String targetFileName = FilenameUtils.getName(remoteFileFull);

		storeFile(sftpClient, targetFileName, localFile);

		return true;
	}

}
