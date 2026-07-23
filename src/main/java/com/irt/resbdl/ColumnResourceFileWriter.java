/*
 *	File Name:	SchemaTableDdl.java
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

package com.irt.resbdl;

import com.irt.data.Record;
import com.irt.data.Timestamp;
import com.irt.data.format.PatternRecordFormat;
import com.irt.html.ColumnAdapter;
import com.irt.resbdl.DatabaseResourceRepositoryImpl.BundleNaming;
import com.irt.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * ColumnResource Bundle Writer
 *
 */
public class ColumnResourceFileWriter implements ResourceBundleWriter {

	public static final String DATABASE_COLUMN_RESOURCE_NAME = "mesg.${partyId}.DatabaseColumnResource";
	private static Map<String, ColumnResourceFileWriter> writers = new HashMap<String, ColumnResourceFileWriter>();

	private static final String NEWLINE = ColumnAdapter.NEWLINE;

	private static File createTempDirectory( String dirName, File parent ) {
		// File parent = new File(System.getProperty("java.io.tmpdir"));

		File temp = new File(parent, dirName);

		if( temp.exists() ) {
			temp.delete();
		}

		temp.mkdir();

		return temp;
	}

	private static File createTempFile( String prefix, String suffix, File parent ) {
		// File parent = new File(System.getProperty("java.io.tmpdir"));

		File temp = new File(parent, prefix + suffix);

		if( temp.exists() ) {
			temp.delete();
		}

		try {
			temp.createNewFile();
			temp.deleteOnExit();
		} catch( IOException ex ) {
			ex.printStackTrace();
		}

		return temp;
	}

	public static String getBaseName( String partyId ) {
		return PatternRecordFormat.getInstance(DATABASE_COLUMN_RESOURCE_NAME)
				.format(Record.createMap("partyId", partyId), null);
	}

	public static File getCreatedDirectory( String dirName, File parent ) {
		// File parent = new File(System.getProperty("java.io.tmpdir"));

		File temp = new File(parent, dirName);

		if( !temp.exists() ) {
			temp.mkdir();
		}

		return temp;
	}

	private static String getDatabaseBundleName( String partyId, Locale locale ) {
		return getBaseName(partyId) + "_" + DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale);
	}

	public static ColumnResourceFileWriter getInstance( File baseDir, String partyId, Locale locale ) {
		String bundleName = toBundleName(partyId, locale);

		ColumnResourceFileWriter writer = writers.get(bundleName);
		if( writer == null ) {
			writer = new ColumnResourceFileWriter(baseDir, partyId, locale);
			writers.put(bundleName, writer);
		}

		return writer;
	}

	private static String toBundleName( String partyId, Locale locale ) {
		return getBaseName(partyId) + "_" + DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale);
	}

	ColumnAdapter adapter = new ColumnAdapter();

	private File baseDir;

	private String partyId;

	private Locale locale;

	private long lastTimestamp;

	private File lastCreatedTempFile;

	private final String FILE_TOPHEADER_NAME = this.getClass().getSimpleName();

	public ColumnResourceFileWriter() {
	}

	public ColumnResourceFileWriter( File baseDir, String partyId, Locale locale ) {
		this.baseDir = baseDir;
		this.partyId = partyId;
		this.locale = locale;
	}

	private File createFile( String filewithExt, File directory ) throws IOException {
		File file = new File(directory, filewithExt);

		if( !file.exists() )
			file.createNewFile();

		return file;
	}

	private File createDatabaseBundleTempFile() throws IOException {
		return createDatabaseBundleTempFile(lastTimestamp = System.currentTimeMillis());
	}

	private File createDatabaseBundleTempFile( long lastTimestamp ) throws IOException {
		String bundleName = toBundleName(partyId, locale);

		String[] structure = bundleName.split("\\.");
		String basenaming = structure[structure.length - 1];

		File leafDir = null;
		for( int i = 0; i < structure.length - 1; i++ ) {
			leafDir = new File(( leafDir == null ) ? baseDir : leafDir, structure[i]);
			if( !leafDir.exists() )
				leafDir.mkdir();
			else if( leafDir.isFile() ) {
				throw new FileNotFoundException("leafDir should be directory. but is file: " + leafDir);
			}
		}

		return lastCreatedTempFile = createTempFile(basenaming + "." + lastTimestamp, ".properties", leafDir);
	}

	private void debugPrintFile( File file ) throws FileNotFoundException {
		Scanner scan = new Scanner(file);

		p("#===== " + file + " =====");
		while( scan.hasNextLine() ) {
			String line = scan.nextLine();
			if( line.startsWith("#") ) {
				p("cmt: " + line);
			} else {
				p(line);
			}
		}
		p("#===== " + file + " =====" + " length : " + file.length());
		scan.close();
	}

	public void debugPrintLastCreatedFile() throws FileNotFoundException, IOException {
		File lastFile = getLastCreatedTempFile();
		if( lastFile != null )
			debugPrintFile(lastFile);
	}

	/**
	 * @return list of columnMap by columnPoolName ({poolName : [{columnMap}]}
	 */
	public Map<String, List<Map<String, Object>>> getColumnMapsByPool( List<Map<String, Object>> partyLocalePools ) {
		if( partyLocalePools == null )
			return null;

		Map<String, List<Map<String, Object>>> pools = new HashMap<String, List<Map<String, Object>>>();

		for( Map<String, Object> map : partyLocalePools ) {
			String poolLocale = (String)map.get("poolLocale");
			if( !BundleNaming.getLocaleString(locale).equals(poolLocale) )
				continue;

			String partyId = (String)map.get("partyId");
			if( !this.partyId.equals(partyId) )
				continue;

			String poolName = (String)map.get("poolName");
			List<Map<String, Object>> poolItems = pools.get(poolName);
			if( poolItems == null ) {
				poolItems = new ArrayList<Map<String, Object>>();
			}
			if( !poolItems.contains(map) ) {
				poolItems.add(map);
			}

			pools.put(poolName, poolItems);
		}

		for( String poolName : pools.keySet() ) {
			List<Map<String, Object>> poolItems = pools.get(poolName);
			if( poolItems != null ) {
				Collections.sort(poolItems, adapter.getColumnSortComparator());
			}
		}

		return pools;
	}

	protected File getLastCreatedTempFile() throws IOException {
		return lastCreatedTempFile;
	}

	public long getLastTimestamp() {
		return lastTimestamp;
	}

	/*
	 * find "ColumnResource.properties" or "ColumnResource_${partyId}.properties" or "ColumnResource_${partyId}_${locale}.properties"
	 */
	private String getOriginalBundleName( String partyId, Locale locale, String originalColumnResourceName, boolean fallbackToParent ) {
		String bundleName = originalColumnResourceName + "_" + partyId + "_" + DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale);
		boolean partyLocaleResourceExist = new File(baseDir, bundleName.replaceAll("\\.", "/") + ".properties").exists();
		if( fallbackToParent ) {
			if( partyLocaleResourceExist ) {
				return bundleName;
			} else {
				bundleName = originalColumnResourceName + "_" + DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale);
				boolean localeResourceExist = new File(baseDir, bundleName.replaceAll("\\.", "/") + ".properties").exists();
				if( localeResourceExist ) {
					return bundleName;
				} else {
					bundleName = originalColumnResourceName;
					boolean topResourceExist = new File(baseDir, bundleName.replaceAll("\\.", "/") + ".properties").exists();

					return bundleName;
				}
			}
		}

		if( partyLocaleResourceExist ) {
			return bundleName;
		}

		throw new IllegalStateException();
	}

	public String makeColumnLines( Map<String, Object> map ) {
		return NEWLINE + adapter.toFileColumnLine(map);
	}

	@Override
	public String makeFileHeader() {
		return NEWLINE
				+ NEWLINE + "#===== " + FILE_TOPHEADER_NAME + " Start for " + new Timestamp(lastTimestamp).getIsoLocal() + " ====="
				+ NEWLINE + "#===== " + "Below contents are generated by " + FILE_TOPHEADER_NAME + " ====="
				+ NEWLINE + "%optionSet( ignoreDuplicated = true )";
	}

	@Override
	public String makeFileTail() {
		return NEWLINE
				+ NEWLINE
				+ "#===== " + FILE_TOPHEADER_NAME + " End for " + new Timestamp(lastTimestamp).getIsoLocal() + " =====";
	}

	@Override
	public String makeHeaderSection( String sectionName ) {
		return NEWLINE
				+ NEWLINE
				+ "########################################################################################################################"
				+ NEWLINE
				+ "#	" + FILE_TOPHEADER_NAME + ": " + ( sectionName == null ? "" : sectionName )
				+ NEWLINE
				+ "########################################################################################################################";
	}

	private String makePoolLine( String poolName ) {
		return NEWLINE + NEWLINE + poolName + ":";
	}

	private void p( Object o ) {
		System.out.println(o);
	}

	synchronized void syncFile( File from, File to ) throws IOException {
		com.irt.util.FileUtil.copyFileContent(from, to);
	}

	@Override
	public String toString() {
		return "ColumnResourceFileWriter{"
				+ " baseDir: " + baseDir
				+ " partyId: " + partyId
				+ " locale: " + locale
				+ "}";
	}

	public void writeSync( List<Map<String, Object>> databaseView, String originalColumnResourceName ) throws IOException {
		writeSync(getColumnMapsByPool(databaseView), originalColumnResourceName);
	}

	public void writeSync( Map<String, List<Map<String, Object>>> partyLocalePools, String originalColumnResourceName ) throws IOException {
		// DatabaseColumnResource.properties
		File newDbBundleTempFile = createDatabaseBundleTempFile();

		// ColumnResource.properties
		String originalName = FileUtil.convertDotToSlash(getOriginalBundleName(partyId, locale, originalColumnResourceName, true));
		File originalFile = createFile(originalName + ".properties", baseDir);
		if( originalFile.length() < 5 ) {
			Logger.getRootLogger().warn("originalFile should have content. Please check program state." + this.toString());
			return;
		}
		Logger.getRootLogger()
				.debug("sync start ColumnResource: from "
						+ originalFile + "(" + originalFile.exists() + ")"
						+ " to newDbBundleTempFile: " + newDbBundleTempFile + "(" + newDbBundleTempFile.exists() + ")");
		syncFile(originalFile, newDbBundleTempFile);

		java.io.RandomAccessFile raf = new java.io.RandomAccessFile(newDbBundleTempFile, "rw");
		raf.seek(originalFile.length());
		raf.writeBytes(makeHeaderSection(null));
		raf.writeBytes(makeFileHeader());
		if( partyLocalePools != null ) {
			for( String poolName : partyLocalePools.keySet() ) {
				boolean alreadyPoolWritten = false;
				for( Map<String, Object> map : partyLocalePools.get(poolName) ) {
					String columnLine = makeColumnLines(map);
					if( !alreadyPoolWritten && columnLine != null && columnLine.trim().length() > 0 ) {
						// raf.writeBytes(makePoolHeaderSection(poolName));
						raf.writeBytes(makePoolLine(poolName));
						alreadyPoolWritten = true;
					}
					raf.writeBytes(columnLine);
				}
			}
		}
		raf.writeBytes(makeFileTail());
		raf.close();

		// DatabaseColumnResource_${lang}.properties
		syncFile(newDbBundleTempFile, createFile(FileUtil.convertDotToSlash(getDatabaseBundleName(partyId, locale)) + ".properties", baseDir));
	}

}
