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
import com.irt.data.format.RecordFormat;
import com.irt.util.BundleUtil;
import com.irt.util.FileUtil;
import com.irt.util.MessageBundle;
import com.irt.util.MessageHandler;
import com.irt.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class MessageResourceFileWriter implements ResourceBundleWriter {
	static final String[] KNOWN_SECTION_CODES = { "SYS", "USR", "RBM",
			"ECS_PARTY", "ECS_TP", "ECS_ITEMTP", "ECS_ITEM", "ECS", // order matter
			"EDI", "OSS", "CPFR",
			"PDSPA", "PDS", // order matter
			"DPR",
			"ICS",
			"SIS"
	};
	static final String[] KNOWN_JSP_SECTION_CODES = { "sys", "usr", "rbm", "ecs", "pds", "dpr", "ics", "sis" };

	private static final String NEWLINE = "\n";
	static final String[] KNOWN_MESSAGE_GROUP_BASEPREFIXES = {
			"FIELD_",
			"MSG_",
			"ERR_UQC_PIDX_",
			"ERR_UQC_UIDX_",
			"ERR_PNF_FIDX_",
			"ERR_CRF_FIDX_",
			"ERR_CCV_CIDX_",
			"ERR_CST_",
			"ERR_",
	};

	static final String[] KNOWN_MESSAGE_GROUP_PREFIXES = {

			"jsp.${jspSectionCode}", // order matter
			"jsp.",

			"NF.",

			"PUB_",

			"TITLE_MODE_",
			"TITLE_${sectionCode}",
			"TITLE_",

			"MSG_FIELD_", // order matter
			"MSG_${sectionCode}",
			"MSG_",

			"${sectionCode}", // order matter
			"FIELD_${sectionCode}", // order matter
			"FIELD_",

			"ERR_UQC_PIDX_${sectionCode}", // order matterh
			"ERR_UQC_PIDX_",

			"ERR_UQC_UIDX_${sectionCode}", // order matter
			"ERR_UQC_UIDX_",

			"ERR_PNF_FIDX_${sectionCode}", // order matter
			"ERR_PNF_FIDX_",

			"ERR_CRF_FIDX_${sectionCode}", // order matter
			"ERR_CRF_FIDX_",

			"ERR_CCV_CIDX_${sectionCode}", // order matter
			"ERR_CCV_CIDX_",

			"ERR_${sectionCode}",
			"ERR_CST_",
			"ERR_",

	};

	public static final String REGEX_HIDDEN_PROPERTIES_FILE = ".*\\/\\.\\w+.*properties";

	private final static String MESSAGE_NAME_LOGICAL_DELIMITER = "_";

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

	public static String getBaseName( String originalBundleName ) {
		return originalBundleName.replaceAll("_.*$", "");
	}

	/*
	 * TODO: find bug. currently grouping is not strictly correctly done.
	 */
	static int getKnownMessageGroupPrefixIndex( List<String> knownPrefix, String key ) {
		int idx = -1;
		for( int i = 0; i < knownPrefix.size(); i++ ) {
			String knownPrefixKey = knownPrefix.get(i);
			if( key.startsWith(knownPrefixKey) ) {
				idx = i;
				break;
			}
		}
		if( idx == -1 ) {
			List<String> orgs = java.util.Arrays.asList(KNOWN_MESSAGE_GROUP_BASEPREFIXES);
			for( String org : orgs ) {
				if( key.startsWith(org) ) {
					idx = knownPrefix.indexOf(org);
					break;
				}
			}
		}

		return idx;
	}

	static List<String> getKnownMessageGroupPrefixList() {
		List<String> list = new ArrayList<String>();

		Map<String, Object> sectionNameMap = new TreeMap<String, Object>();
		for( String sectionCode : MessageResourceFileWriter.KNOWN_SECTION_CODES ) {
			sectionNameMap.put("sectionCode", sectionCode);
		}
		for( String jspSectionCode : MessageResourceFileWriter.KNOWN_JSP_SECTION_CODES ) {
			sectionNameMap.put("jspSectionCode", jspSectionCode);
		}

		for( String prefix : MessageResourceFileWriter.KNOWN_MESSAGE_GROUP_PREFIXES ) {
			String compiled = null;
			if( prefix.contains("${") ) {
				RecordFormat formatter = PatternRecordFormat.getInstance(prefix);
				compiled = formatter.format(sectionNameMap, null);
				list.add(compiled);
			} else {
				list.add(prefix);
			}
		}
		return list;
	}

	static String getKnownMessageGroupPrefixName( List<String> knownPrefix, String key ) {
		int idx = getKnownMessageGroupPrefixIndex(knownPrefix, key);
		if( idx != -1 ) {
			return knownPrefix.get(idx);
		}

		return "";
	}

	private static String toBundleName( String originalBundleName, Locale locale ) {
		return getBaseName(originalBundleName) + "_" + DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale);
	}

	static MessageBundle toMessageBundle( MessageHandler msghandler ) {
		if( msghandler instanceof MessageBundle ) {
			return (MessageBundle)msghandler;
		}

		return null;
	}

	// ** if 3 level deep for delimiter then grouping the lines together for better formatting and manage and view for human
	private final int MESSAGE_LINES_GROUPING_BY_DELIMITER_LEVEL = 3;// will add one line for each group

	Map<String, Object> props;

	File baseDir;

	Locale locale;

	private long lastTimestamp;

	private File lastCreatedTempFile;

	private final String FILE_TOPHEADER_NAME = this.getClass().getSimpleName();

	private int maxKeyCharLength = -1;

	private static Map<String, Integer> MSGKEY_PREFIX_INDEX = Collections.synchronizedMap(new HashMap<String, Integer>(3000));

	private static int getAndCacheKnownMessageGroupPrefixIndex( List<String> knownPrefix, String msgkey ) {
		synchronized( MSGKEY_PREFIX_INDEX ) {
			Integer cached = MSGKEY_PREFIX_INDEX.get(msgkey);
			if( cached == null ) {
				cached = MessageResourceFileWriter.getKnownMessageGroupPrefixIndex(knownPrefix, msgkey);
				MSGKEY_PREFIX_INDEX.put(msgkey, cached);
			}

			return cached;
		}
	}

	static final List<String> knownPrefix = MessageResourceFileWriter.getKnownMessageGroupPrefixList();

	final List<String> knownBasePrefix = java.util.Arrays.asList(KNOWN_MESSAGE_GROUP_BASEPREFIXES);

	Comparator<String> COMP = new Comparator<String>() {

		@Override
		public int compare( String o1, String o2 ) {
			int sect1 = MessageResourceFileWriter.getAndCacheKnownMessageGroupPrefixIndex(knownPrefix, o1);
			int sect2 = MessageResourceFileWriter.getAndCacheKnownMessageGroupPrefixIndex(knownPrefix, o2);

			if( sect1 > -1 && sect2 > -1 ) {
				if( sect1 > sect2 ) {
					return 1;
				} else if( sect1 < sect2 ) {
					return -1;
				} else {
					return o1.compareTo(o2);
				}
			} else {
				return o1.compareTo(o2);
			}
		}
	};

	public MessageResourceFileWriter( File baseDir, Locale locale ) {
		this.baseDir = baseDir;
		this.locale = locale;
		this.props = new TreeMap<String, Object>();
	}

	private File createBundleTempFile( String originalBundleName ) throws IOException {
		return createBundleTempFile(originalBundleName, lastTimestamp = System.currentTimeMillis());
	}

	private File createBundleTempFile( String originalBundleName, long lastTimestamp ) throws IOException {
		String bundleName = toBundleName(originalBundleName, locale);

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

		return lastCreatedTempFile = createTempFile("." + basenaming + "." + lastTimestamp, ".properties", leafDir);
	}

	private File createFile( String filewithExt, File directory ) throws IOException {
		File file = new File(directory, filewithExt);

		if( !file.exists() )
			file.createNewFile();

		return file;
	}

	public File getBundleFile( String bundleName, Locale locale ) {
		return new File(baseDir, getBundleFileName(bundleName, locale));
	}

	private String getBundleFileName( String bundleName, Locale locale ) {
		return FileUtil.convertDotToSlash(getBundleName(bundleName, locale)) + ".properties";
	}

	private String getBundleName( String bundleName, Locale locale ) {
		return getBaseName(bundleName) + "_" + DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale);
	}

	public TreeMap<String, Object> getEntriesAndSetMaxKeyLen( ResourceBundle bundle, KeyLengthCounter counter ) {
		TreeMap<String, Object> entries = new TreeMap<String, Object>(COMP);
		for( String key : bundle.keySet() ) {
			counter.compareAndSet(key);

			entries.put(key, bundle.getString(key));
		}

		return entries;
	}

	public KeyLengthCounter createKeyLengthCounter() {
		return new KeyLengthCounter() {
			private int sofar = 0;

			@Override
			public int getMaxKeyLength() {
				return sofar;
			}

			@Override
			public void compareAndSet( String key ) {
				if( sofar < key.length() )
					sofar = key.length();
			}

		};
	}

	public static interface KeyLengthCounter {
		void compareAndSet( String key );

		int getMaxKeyLength();
	}

	public List<Map<String, Object>> toList( TreeMap<String, Object> props ) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for( String key : props.keySet() ) {
			list.add(Record.createMap(key, props.get(key)));
		}
		return list;
	}

	public TreeMap<String, Object> getEntriesAndSetConfig( ResourceBundle bundle ) {
		TreeMap<String, Object> entries = new TreeMap<String, Object>(COMP);
		int maxkeylen = 0;
		for( String key : bundle.keySet() ) {
			if( maxkeylen < key.length() )
				maxkeylen = key.length();

			entries.put(key, bundle.getString(key));
		}

		setMaxKeyCharLength(maxkeylen);

		return entries;
	}

	/*
	 * find "ColumnResource.properties" or "ColumnResource_${partyId}.properties" or "ColumnResource_${partyId}_${locale}.properties"
	 */
	private String getOriginalBundleName( Locale locale, String originalColumnResourceName, boolean fallbackToParent ) {
		String bundleName = originalColumnResourceName + "_" + DatabaseResourceRepositoryImpl.BundleNaming.getLocaleString(locale);
		boolean localeResourceExist = new File(baseDir, bundleName.replaceAll("\\.", "/") + ".properties").exists();
		if( fallbackToParent ) {
			if( localeResourceExist ) {
				return bundleName;
			} else {
				bundleName = originalColumnResourceName;
				boolean topResourceExist = new File(baseDir, bundleName.replaceAll("\\.", "/") + ".properties").exists();

				return bundleName;
			}
		}

		if( localeResourceExist )
			return bundleName;

		throw new IllegalStateException();
	}

	public Map<String, Map<String, Object>> grouping( TreeMap<String, Object> props ) throws IOException {
		Map<String, Map<String, Object>> ret = new TreeMap<String, Map<String, Object>>(COMP);

		if( props != null ) {
			String prevName = null;
			Map<String, Object> _g = null;
			for( String key : props.keySet() ) {
				String sectName = "";

				int sectIdx = MessageResourceFileWriter.getKnownMessageGroupPrefixIndex(knownPrefix, key);
				if( sectIdx > -1 ) {
					sectName = knownPrefix.get(sectIdx);
				}

				Map<String, Object> existing = ret.get(sectName);
				if( existing == null ) {
					existing = new TreeMap<String, Object>();
				}
				existing.put(key, props.get(key));

				ret.put(sectName, existing);
			}
		}

		return ret;

	}

	@Override
	public String makeFileHeader() {
		return NEWLINE
				+ NEWLINE + "#===== " + FILE_TOPHEADER_NAME + " Start for " + new Timestamp(lastTimestamp).getIsoLocal() + " ====="
				+ NEWLINE + "#===== " + "Below contents are generated by " + FILE_TOPHEADER_NAME + " =====";
	}

	@Override
	public String makeFileTail() {
		return NEWLINE
				+ NEWLINE
				+ NEWLINE
				+ "#===== " + FILE_TOPHEADER_NAME + " End for " + new Timestamp(lastTimestamp).getIsoLocal() + " =====";
	}

	@Override
	public String makeHeaderSection( String sectionName ) {
		return NEWLINE
				+ NEWLINE
				+ NEWLINE
				+ "########################################################################################################################"
				+ NEWLINE
				+ "#	" + FILE_TOPHEADER_NAME + ": " + ( sectionName == null ? "" : sectionName )
				+ NEWLINE
				+ "########################################################################################################################";
	}

	void putAll( Map<String, Object> map ) {
		this.props.putAll(map);
	}

	public void setMaxKeyCharLength( int maxKeyCharLength ) {
		this.maxKeyCharLength = maxKeyCharLength;
	}

	public List<String> sortToWrite( TreeMap<String, Object> props ) throws IOException {

		// TreeMap<String, Object> ret = new TreeMap<String, Object>();
		List<String> ret = new ArrayList<String>();

		if( props != null ) {
			String prevPrefix = null;
			int prevIdx = -2;
			int sectIdx = -1;

			for( String key : props.keySet() ) {
				sectIdx = MessageResourceFileWriter.getKnownMessageGroupPrefixIndex(knownPrefix, key);
				if( sectIdx >= 0 ) {
					prevPrefix = knownPrefix.get(sectIdx);
				} else {
					prevPrefix = "";
				}

				if( sectIdx == prevIdx ) {

					int tabcnt = 0;
					if( maxKeyCharLength > 0 ) {
						int spacing = maxKeyCharLength - key.length();
						tabcnt = spacing / 4;
					}
					String tabSpacing = "\t\t";
					for( int i = 0; i < tabcnt; i++ ) {
						tabSpacing += "\t";
					}

					String line = key + tabSpacing + "=\t" + StringUtil.getUnicodeEncoded((String)props.get(key));
					ret.add(line);
					// ret.put(key, line);
				} else {
					String section = makeHeaderSection(prevPrefix.replaceAll("_$", ""));
					// ret.put(prevPrefix, section);
					ret.add(section);
					prevIdx = sectIdx;
				}
			}
		}
		return ret;
	}

	public static List<String> sortLines( TreeMap<String, Object> props, int maxKeyCharLength ) throws IOException {

		// TreeMap<String, Object> ret = new TreeMap<String, Object>();
		List<String> ret = new ArrayList<String>();

		if( props != null ) {
			String prevPrefix = null;
			int prevIdx = -2;
			int sectIdx = -1;

			for( String key : props.keySet() ) {
				sectIdx = MessageResourceFileWriter.getKnownMessageGroupPrefixIndex(knownPrefix, key);
				if( sectIdx >= 0 ) {
					prevPrefix = knownPrefix.get(sectIdx);
				} else {
					prevPrefix = "";
				}

				if( sectIdx == prevIdx ) {

					int tabcnt = 0;
					if( maxKeyCharLength > 0 ) {
						int spacing = maxKeyCharLength - key.length();
						tabcnt = spacing / 4;
					}
					String tabSpacing = "\t\t";
					for( int i = 0; i < tabcnt; i++ ) {
						tabSpacing += "\t";
					}

					String line = key + tabSpacing + "=\t" + StringUtil.getUnicodeEncoded((String)props.get(key));
					ret.add(line);
					// ret.put(key, line);
				}
			}
		}
		return ret;
	}

	synchronized void syncFile( File from, File to ) throws IOException {
		com.irt.util.FileUtil.copyFileContent(from, to);
	}

	public List<File> writeMessageBundles( MessageHandler msghandler ) throws IOException {
		MessageBundle msgbundle = toMessageBundle(msghandler);

		List<File> bundleFiles = new ArrayList<File>();

		ResourceBundle[] bundles = BundleUtil.getBundlesUnsafely(msghandler);
		String[] sourceNames = BundleUtil.getBundleBaseNamesUnsafely(msghandler);

		int maxkeylen = 0;
		for( int b = 0; b < bundles.length; b++ ) {
			ResourceBundle bdl = bundles[b];
			String sourceName = sourceNames[b];
			TreeMap<String, Object> entries = getEntriesAndSetConfig(bdl);

			writeSync(entries, sourceName, true);
			setMaxKeyCharLength(-1);

			File bundleFile = getBundleFile(sourceName, locale);
			bundleFiles.add(bundleFile);
		}

		return bundleFiles;
	}

	/**
	 * 
	 * @param props
	 * @param bundleName
	 * @param ignoreOriginalFile:
	 *            ignore originalFile and write the map data.
	 * @throws IOException
	 */
	public void writeSync( TreeMap<String, Object> props, String bundleName, boolean ignoreOriginalFile ) throws IOException {
		// DatabaseColumnResource.properties
		File newDbBundleTempFile = createBundleTempFile(bundleName);

		// ColumnResource.properties
		String originalName = null;
		File originalFile = null;
		if( ignoreOriginalFile ) {
			originalName = FileUtil.convertDotToSlash(getBundleName(bundleName, locale));
			originalFile = FileUtil.getCreatedFile(originalName + ".properties", baseDir);
		} else {
			originalName = FileUtil.convertDotToSlash(getBundleFileName(bundleName, locale));
			originalFile = createFile(originalName + ".properties", baseDir);
			if( originalFile.length() < 5 ) {
				Logger.getRootLogger().warn("originalFile should have content. Please check program state." + this.toString());
				return;
			}

			syncFile(originalFile, newDbBundleTempFile);
		}

		Logger.getRootLogger()
				.debug("sync start Bundle: from "
						+ originalFile + "(" + originalFile.exists() + ")"
						+ " to newDbBundleTempFile: " + newDbBundleTempFile + "(" + newDbBundleTempFile.exists() + ")");

		java.io.RandomAccessFile raf = new java.io.RandomAccessFile(newDbBundleTempFile, "rw");
		if( !ignoreOriginalFile ) {
			raf.seek(originalFile.length());
		}

		raf.writeBytes(makeFileHeader());
		// raf.writeBytes(makeHeaderSection(null));
		if( props != null ) {
			Map<String, Map<String, Object>> groups = grouping(props);
			for( String sectName : groups.keySet() ) {
				Map<String, Object> sections = groups.get(sectName);
				List<String> lines = sortToWrite((TreeMap<String, Object>)sections);
				for( String line : lines ) {
					raf.write(NEWLINE.getBytes());
					raf.write(line.getBytes());
				}
			}
		}
		raf.writeBytes(makeFileTail());
		raf.close();

		// DatabaseColumnResource_${lang}.properties
		File bundleFile = createFile(FileUtil.convertDotToSlash(getOriginalBundleName(locale, bundleName, true)) + ".properties", baseDir);
		syncFile(newDbBundleTempFile, bundleFile);
		Logger.getRootLogger()
				.debug("sync done Bundle: from "
						+ originalFile + "(" + originalFile.length() + ")"
						+ " to newDbBundleTempFile: " + newDbBundleTempFile + "(" + newDbBundleTempFile.length() + ")");
	}

}
