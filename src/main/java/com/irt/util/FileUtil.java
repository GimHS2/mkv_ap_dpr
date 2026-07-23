/*
 *	File Name:	FileUtil.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/11/30		2.2.0c	create
 *
**/

package com.irt.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * 
 */
public class FileUtil {

	public static String backslashToslash( String maybeBackslash ) {
		if( maybeBackslash == null )
			return null;

		return maybeBackslash.replaceAll("\\\\", "/");
	}

	public static String convertDotToSlash( String dottedName ) {
		return dottedName == null ? null : dottedName.replaceAll("\\.", "/");
	}

	public static boolean copyFileContent( File aFile, File bFile ) throws IOException {
		java.io.InputStream inputStream = null;
		java.io.OutputStream outputStream = null;

		inputStream = new java.io.FileInputStream(aFile);
		outputStream = new java.io.FileOutputStream(bFile);

		copyFileContent(inputStream, outputStream);

		return true;
	}

	public static boolean copyFileContent( InputStream inputStream, OutputStream outputStream ) throws IOException {
		try {
			int length;
			byte[] buffer = new byte[10240];

			while( ( length = inputStream.read(buffer) ) > 0 )
				outputStream.write(buffer, 0, length);
		} catch( IOException ioEx ) {
			// getLogger().error(getDescription() + " file('" + file.getName() + "') move error.", ioEx);
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

		return true;
	}

	public static File getCreatedDir( File parent, String singleDirName ) {
		File temp = new File(parent, singleDirName);

		if( !temp.exists() ) {
			temp.mkdir();
		}

		return temp;
	}

	public static File getCreatedDir( String pathFromParent, File parent ) throws IOException {
		String leafDirname = null;

		File dir = new File(parent, pathFromParent);
		if( !dir.getParentFile().exists() ) {
			if( pathFromParent != null ) {
				String[] paths = pathFromParent.split("/");
				if( paths != null && paths.length > 0 ) {
					for( int i = 0; i < paths.length - 1; i++ ) {
						parent = getCreatedDir(parent, paths[i]);
					}
					leafDirname = paths[paths.length - 1];
				}
				dir = new File(parent, leafDirname);
			}
		} else {
			if( dir.isFile() ) {
				throw new FileNotFoundException("The path(" + dir.getAbsolutePath() + ") should be directory, but the path is a file.");
			} else {

			}
		}

		if( !dir.exists() ) {
			dir.mkdir();
		}

		return dir;
	}

	public static File getCreatedFile( File parent, String filename ) throws IOException {
		File file = new File(parent, filename);

		if( !file.exists() )
			file.createNewFile();

		return file;
	}

	public static File getCreatedFile( String pathFromParent, File parent ) throws IOException {
		String leafFilename = null;

		if( parent == null || !parent.exists() )
			throw new FileNotFoundException("parent: " + parent + " is not exists.");

		File file = new File(parent, pathFromParent);
		if( !file.getParentFile().exists() ) {
			if( pathFromParent != null ) {
				String[] paths = pathFromParent.split("/");
				if( paths != null && paths.length > 0 ) {
					for( int i = 0; i < paths.length - 1; i++ ) {
						parent = getCreatedDir(parent, paths[i]);
					}
					leafFilename = paths[paths.length - 1];
				}
				file = new File(parent, leafFilename);
			}
		} else {
			if( file.getParentFile().isFile() ) {
				throw new FileNotFoundException("The path(" + file.getParentFile() + ") should be directory, but the path is a file.");
			} else {

			}
		}

		if( !file.exists() )
			file.createNewFile();

		return file;
	}

	public static String getResourceString( ClassLoader loader, String path ) throws IOException {
		return getResourceString(loader, path, "UTF-8");
	}

	public static String getResourceString( ClassLoader loader, String path, String charset ) throws IOException {
		URL _sql1_url = loader.getResource(path);
		if( _sql1_url != null ) {
			InputStream is = _sql1_url.openStream();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			// Scanner scan = new Scanner(is);
			FileUtil.copyFileContent(is, baos);
			is.close();
			return baos.toString(charset);
		}
		return null;
	}

	public static void printFileContent( File file, OutputStream output ) throws IOException {
		Scanner input = new Scanner(file);
		try {

			while( input.hasNextLine() ) {
				output.write(input.nextLine().getBytes());
				output.write("\n".getBytes());
			}
		} finally {
			if( input != null )
				input.close();
		}
	}

	public static byte[] readFileAsByte( String pathname ) throws IOException {
		RandomAccessFile f = new RandomAccessFile(pathname, "r");
		byte[] b = new byte[(int)f.length()];
		f.readFully(b);
		return b;
	}

	/**
	 * @param pathname
	 * @param charset
	 * @param peekByteLength
	 *            : limit by byte length
	 * @return
	 * @throws IOException
	 */
	public static String readFileByByte( String pathname, Charset charset, int peekByteLength ) throws IOException {
		File file = new File(pathname);
		InputStream in = new FileInputStream(file);
		byte[] b = new byte[(int)file.length()];
		int len = peekByteLength < 0 ? b.length : peekByteLength;
		int total = 0;

		while( total < len ) {
			int result = in.read(b, total, len - total);
			if( result == -1 ) {
				break;
			}
			total += result;
		}

		return new String(b, charset);
	}

	/**
	 * @param pathname
	 * @param charset
	 * @param peekLineLength
	 *            : limit by line ( max line to peek )
	 * @return
	 * @throws IOException
	 */
	public static String readFileByLine( String pathname, Charset charset, int peekLineLength ) throws IOException {
		File file = new File(pathname);
		StringBuilder fileContents = new StringBuilder((int)file.length());
		Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));
		String lineSeparator = System.getProperty("line.separator");

		int lineCount = 0;
		try {
			while( scanner.hasNextLine() && ( peekLineLength < 0 || lineCount < peekLineLength ) ) {
				fileContents.append(scanner.nextLine() + lineSeparator);
			}
			return fileContents.toString();
		} finally {
			scanner.close();
			file = null;
		}
	}

	public static String readFileContent( URL url ) throws IOException {
		if( url == null )
			throw new FileNotFoundException();

		FileInputStream is = new FileInputStream(url.getFile());
		try {
			java.util.Scanner scan = new java.util.Scanner(is).useDelimiter("\\A");
			String ret = scan.hasNext() ? scan.next() : "";

			scan.close();

			return ret;
		} finally {
			if( is != null )
				is.close();
		}
	}

	public static String readFileContent( File file ) throws IOException {
		FileInputStream is = new FileInputStream(file);
		try {
			java.util.Scanner scan = new java.util.Scanner(is).useDelimiter("\\A");
			String ret = scan.hasNext() ? scan.next() : "";

			scan.close();

			return ret;
		} finally {
			if( is != null )
				is.close();
		}
	}

	public static boolean writeFileContent( File file, String string ) throws IOException {
		FileWriter fw = new FileWriter(file);
		fw.write(string);
		fw.close();
		return true;
	}

	/**
	 * Compress a directory to ZIP file including subdirectories
	 * 
	 * @param outputDirectory
	 *            where to place the compress file
	 * @param directoryToCompress
	 *            directory to zip
	 */
	public static void zipDir( File outputDirectory, File directoryToCompress ) {
		zipDir(outputDirectory, directoryToCompress, null, null);
	}

	/**
	 * Compress a directory to ZIP file including subdirectories
	 * 
	 * @param outputDirectory
	 *            where to place the compress file
	 * @param directoryToCompress
	 *            directory to zip
	 * @param zipFilename
	 * @param excludeEntryRegex
	 *            exclusion pattern for zip entry nameing( calculation starts from root dir )
	 * 
	 */
	public static void zipDir( File outputDirectory, File directoryToCompress, String zipFilename, String excludeEntryRegex ) {
		try {
			FileOutputStream dest = new FileOutputStream(
					new File(outputDirectory, ( zipFilename == null ? directoryToCompress.getName() : zipFilename ) + ".zip"));
			ZipOutputStream zipOutputStream = new ZipOutputStream(dest);

			zipDirHelper(directoryToCompress, directoryToCompress, zipOutputStream, excludeEntryRegex);
			zipOutputStream.close();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private static void zipDirHelper( File rootDirectory, File currentDirectory, ZipOutputStream out, String excludeRegex ) throws Exception {
		byte[] data = new byte[2048];

		File[] files = currentDirectory.listFiles();
		if( files == null ) {
			// no files were found or this is not a directory

		} else {
			for( File file : files ) {
				if( file.isDirectory() ) {
					zipDirHelper(rootDirectory, file, out, excludeRegex);
				} else {
					FileInputStream fi = new FileInputStream(file);
					// creating structure and avoiding duplicate file names
					String name = file.getAbsolutePath().replace(rootDirectory.getAbsolutePath(), "");

					boolean isExclude = false;
					if( excludeRegex != null ) {
						isExclude = Pattern.compile(excludeRegex).matcher(name).matches();
						if( isExclude )
							continue;
					}

					ZipEntry entry = new ZipEntry(name);
					out.putNextEntry(entry);
					int count;
					BufferedInputStream origin = new BufferedInputStream(fi, 2048);
					while( ( count = origin.read(data, 0, 2048) ) != -1 ) {
						out.write(data, 0, count);
					}
					origin.close();
				}
			}
		}
	}

	public static class FileAcceptor {

		/**
		 * @return whether file can be write.
		 */
		public static boolean isWriteCompleted( File file ) {
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

		public static boolean isWriteDormant( File file, long dormantMillis ) {
			return isWriteDormant(file, dormantMillis, System.currentTimeMillis());
		}

		public static boolean isWriteDormant( File file, long dormantMillis, long timeAgainst ) {
			return ( timeAgainst > file.lastModified() + dormantMillis );
		}
	}

}
