/*
 *	File Name:	FileComment.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/08/30		2.2.0c	create
 *
**/

package com.irt.util.cmtlog;

import com.irt.util.CharsetDetector;
import com.irt.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File Comment Object
 */
public class FileComment {
	public final static String DEFAULT_NULLVERSION_NUMBER_STRING = "0.0.1";
	public final static String PATTERN_JAVAFILE_COMMENT_BLOCK_END = "^.*\\*\\/";

	public final static String PATTERN_JAVAFILE_COMMENT_LOG_CONTINUE_CONTENT = "^\\s+\\*\\s+(.*)$";
	public final static String PATTERN_JAVAFILE_COMMENT_LOG_LINE = "^\\s+\\*\\s+(\\w+)\\s+(\\w+\\/\\w+\\/\\w+)\\s+"
			+ "(\\w+\\.\\w+\\.\\w+|\\w+\\.\\w+|[ \\t]?)"
			+ "\\s+(.*)$";
	public final static String PATTERN_JAVAFILE_COMMENT_LOG_TITLE = "^.*Modified.*\\(YYYY\\/MM\\/DD\\).*Ver.*Content.*$";
	public final static String PATTERN_JAVAFILE_END_OF_COMMENT = "[^package\\s+.*;$|^.*class.*\\{.*$]";
	public final static String PATTERN_JAVAFILE_FILENAME = "^.*\\*.*File Name:(.*)$";
	public final static String PATTERN_JAVAFILE_PACKAGEPATH = "^package\\s+(.*);$";
	public final static String PATTERN_JAVAFILE_VERSION = "^.*\\*.*Version:(.*)$";

	public final static String PATTERN_SQLFILE_COMMENT_BLOCK_END = "^----------.*$|^REM\\s?------.*$";

	public final static String PATTERN_SQLFILE_COMMENT_LOG_CONTINUE_CONTENT = "^REM\\s+(.*)$";
	public final static String PATTERN_SQLFILE_COMMENT_LOG_LINE = "^REM\\s+(\\w+)\\s+" + "(\\w+\\/\\w+\\/\\w+)\\s+"
			+ "(\\w+\\.\\w+\\.\\w+|\\w+\\.\\w+|[ \\t]?)"
			+ "\\s+(.*)$";
	public final static String PATTERN_SQLFILE_COMMENT_LOG_TITLE = "^REM\\s+(MODIFIED)\\s+\\((YYYY\\/MM\\/DD)\\)\\s+(VER)\\s+(CONTENT)";
	public final static String PATTERN_SQLFILE_CONVERSION = "^conVersion\\s+CONSTANT\\s+VARCHAR2.*\\s+'(.*)';$";
	public final static String PATTERN_SQLFILE_END_OF_COMMENT = "^conVersion\\s+CONSTANT\\s+VARCHAR2.*$|^CREATE\\s+TABLE.*$|^CREATE.*SYNONYM.*$|^DROP\\s+TABLE.*$";
	public final static String PATTERN_SQLFILE_FILENAME = "^REM\\s+HEAD\\s+(.*)";
	public final static String PATTERN_SQLFILE_PACKAGEPATH = "script\\/(.*)\\/.*\\.sql";
	public final static String PATTERN_SQLFILE_VERSION = "^REM\\s+VERSION\\s+(.*)";
	public final static String RESERVED_SINGLELINE_DISPLAY_NEWLINE_SYMBOL = ";";

	public static String calcModuleName( File sourceFile ) {
		String absPath = sourceFile.getAbsolutePath();
		return calcModuleName(absPath);
	}

	public static String calcModuleName( String absPath ) {
		String _moduleName = null;
		// String ptn_dynamic = "webapps\\/(dynamic)\\/WEB-INF\\/source";
		// String ptn_script = "(script)\\/[cre|pub|priv].*sql";
		// String ptn_javaapi = "java-api\\/(.*)\\_src\\/";
		// "(script)\\/.*[cre|pub|priv|drop].*sql",
		// "(install)\\/.*[cre|pub|priv|drop].*sql",

		String[] ptns = new String[] {
				"webapps\\/(dynamic)\\/WEB-INF\\/source",
				"(script)\\/.*\\.sql",
				"(install)\\/.*\\.sql",
				"java-api\\/(.*)\\_src\\/"
		};
		for( String ptnStr : ptns ) {
			_moduleName = parseLine(absPath, ptnStr, 1);
			if( _moduleName != null ) {
				break;
			}
		}

		if( _moduleName == null || _moduleName.length() <= 0 ) {
			// throw new IllegalArgumentException("Please supply right argument or define new module pattern. for: " + absPath);
			_moduleName = "undefined";
		}

		return _moduleName;
	}

	public static String calcScmLogModuleLine( String sourceFile ) {
		String modulePrefix = calcScmLogModulePrefix(sourceFile);
		String moduleName = calcModuleName(sourceFile);
		return getScmLogModuleLine(moduleName, modulePrefix);
	}

	public static String calcScmLogModulePrefix( File sourceFile ) {
		return calcScmLogModulePrefix(sourceFile.getAbsolutePath());
	}

	public static String calcScmLogModulePrefix( String sourceFile ) {
		String moduleName = calcModuleName(sourceFile);
		return getScmLogModulePrefix(moduleName);
	}

	public static Scanner getScanner( File sourceFile ) throws FileNotFoundException, UnsupportedEncodingException {
		Charset charset = new CharsetDetector().detectCharset(sourceFile, new String[] { "EUC-KR", "UTF-8" });
		if( charset == null )
			throw new UnsupportedEncodingException("undetectable encoding in file: " + sourceFile.getAbsolutePath());

		// System.out.println("detectedCharset: " + charset.name());
		Scanner r = new Scanner(sourceFile, charset.name());
		return r;
	}

	public static String getScmLogForModule( List<FileComment> moduleCmts, String moduleName ) {
		Comparator<FileComment> cmp = new Comparator<FileComment>() {
			@Override
			public int compare( FileComment o1, FileComment o2 ) {
				String pkg1 = o1.getPackageFilename();
				String pkg2 = o2.getPackageFilename();
				return pkg1.compareTo(pkg2);
			}
		};

		Collections.sort(moduleCmts, cmp);

		String moduleComment = null;
		for( FileComment cmt : moduleCmts ) {
			if( moduleComment == null ) {
				moduleComment = "  " + "* " + cmt.getScmLogModuleLine();
				moduleComment += "\n";
			}
			moduleComment += "    " + "+ " + cmt.getScmLogCommentLine();
			moduleComment += "\n";
		}
		return moduleComment;
	}

	public static String getScmLogModuleLine( String moduleName ) {
		return getScmLogModuleLine(moduleName, getScmLogModulePrefix(moduleName));
	}

	public static String getScmLogModuleLine( String moduleName, String modulePrefix ) {
		return modulePrefix + "(" + moduleName + ")";
	}

	public static String getScmLogModulePrefix( String moduleName ) {
		if( moduleName.startsWith("script") ) {
			return "Schema";
		} else if( moduleName.startsWith("install") ) {
			return "Schema";
		} else if( moduleName.equals("dynamic") ) {
			return "WEB";
		} else {
			return "API";
		}
	}

	public static String parseLine( String line, String pattern, int group ) {
		Pattern ptn = Pattern.compile(pattern);
		Matcher m = ptn.matcher(line);
		if( m.find() ) {
			if( m.groupCount() >= group ) {
				return m.group(group);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static String parseLineOrThrow( String line, String pattern ) {
		return parseLineOrThrow(line, pattern, 0);
	}

	public static String parseLineOrThrow( String line, String pattern, int group ) {
		Pattern ptn = Pattern.compile(pattern);
		Matcher m = ptn.matcher(line);
		if( m.find() ) {
			if( m.groupCount() >= group ) {
				return m.group(group);
			} else {
				throw new IllegalArgumentException("not match: " + line + " group: " + group);
			}
		} else {
			throw new IllegalArgumentException("not match: " + line);
		}
	}

	public static String parseLineWithTrimOrThrow( String line, String pattern, int group ) {
		return parseLineOrThrow(line, pattern, group).trim();
	}

	public static FileComment read( File sourceFile ) throws IOException {
		return makeRead(sourceFile.getAbsolutePath());
	}

	public static FileComment makeRead( String sourceFile ) throws IOException {
		String moduleName = calcModuleName(sourceFile);

		if( moduleName.startsWith("script") ) {
			return readSql(sourceFile);
		} else if( moduleName.startsWith("install") ) {
			return readSql(sourceFile);
		} else if( moduleName.equals("dynamic") ) {
			return readJava(sourceFile);
		} else if( moduleName.equals("undefined") ) {
			FileComment fileComment = new FileComment(new File(sourceFile));
			fileComment.setModuleName(moduleName);
			fileComment.setVerString("undefined");
			return fileComment;
		} else {// java-api
			return readJava(sourceFile);
		}
	}

	public boolean isScmEntryDeleted() {
		return scmEntryDeleted;
	}

	public void setScmEntryDeleted( boolean deleted ) {
		this.scmEntryDeleted = deleted;
	}

	private boolean scmEntryDeleted;

	public boolean isScmEntryBroken() {
		return scmEntryBroken;
	}

	public void setScmEntryBroken( boolean scmEntryBroken ) {
		this.scmEntryBroken = scmEntryBroken;
	}

	private boolean scmEntryBroken;

	public static FileComment makeDeleted( String sourceFile ) {
		String moduleName = calcModuleName(sourceFile);

		FileComment cmt = new FileComment(new File(sourceFile));
		cmt.setScmEntryDeleted(true);

		return cmt;
	}

	public static FileComment makeBroken( String sourceFile ) {
		String moduleName = calcModuleName(sourceFile);

		FileComment cmt = new FileComment(new File(sourceFile));
		cmt.setScmEntryBroken(true);

		return cmt;
	}

	public static FileComment readJava( File sourceFile ) throws IOException {
		Scanner r = getScanner(sourceFile);

		FileComment fileComment = new FileComment(sourceFile);

		List<String> lines = new ArrayList<String>();
		boolean isWithinFileLogList = false;
		boolean isFileLogContinueContent = false;
		while( r.hasNextLine() ) {
			String line = r.nextLine();
			lines.add(line);

			if( line.matches(PATTERN_JAVAFILE_FILENAME) ) {
				if( fileComment.getDefinedFilename() == null ) {
					fileComment.setDefinedFilename(parseLineWithTrimOrThrow(line, PATTERN_JAVAFILE_FILENAME, 1));
				}
			} else if( line.matches(PATTERN_JAVAFILE_VERSION) ) {
				if( fileComment.getVerString() == null ) {
					fileComment.setVerString(parseLineWithTrimOrThrow(line, PATTERN_JAVAFILE_VERSION, 1));
				}
			}

			if( line.matches(FileComment.PATTERN_JAVAFILE_COMMENT_LOG_TITLE) ) {
				isWithinFileLogList = true;
				fileComment.setFileCommentLogTitle(line);
				continue;
			}

			if( isWithinFileLogList ) {
				if( line.matches(FileComment.PATTERN_JAVAFILE_COMMENT_LOG_LINE) ) {
					isFileLogContinueContent = false;
				} else {
					isFileLogContinueContent = true;
				}
				if( !isFileLogContinueContent ) {
					FileCommentLog cmtLog = FileCommentLog.parseJavaFileCommentLog(line);
					fileComment.addCommentLog(cmtLog);
				} else {
					if( line.matches(FileComment.PATTERN_JAVAFILE_COMMENT_LOG_CONTINUE_CONTENT) ) {
						String parsed = parseLineWithTrimOrThrow(line, FileComment.PATTERN_JAVAFILE_COMMENT_LOG_CONTINUE_CONTENT, 1);
						fileComment.appendCommentLogContentToLast(parsed);
					}
				}
			}

			if( isWithinFileLogList && line.matches(PATTERN_JAVAFILE_COMMENT_BLOCK_END) ) {
				isWithinFileLogList = false;
			}

			if( line.matches(PATTERN_JAVAFILE_PACKAGEPATH) ) {
				if( fileComment.getPackagePath() == null ) {
					fileComment.setPackagePath(parseLineWithTrimOrThrow(line, PATTERN_JAVAFILE_PACKAGEPATH, 1));
				}
			}

			if( r.hasNext(PATTERN_JAVAFILE_END_OF_COMMENT) ) {
				// sometimes if next token starts with comma above if statement gets true.( which is not what preferred )
				if( r.nextLine().matches(PATTERN_JAVAFILE_PACKAGEPATH) ) {
					if( fileComment.getPackagePath() == null ) {
						fileComment.setPackagePath(parseLineWithTrimOrThrow(line, PATTERN_JAVAFILE_PACKAGEPATH, 1));
					}
					break;
				} else {
					continue;
				}
			}
		}
		fileComment.readFinalize(lines);
		return fileComment;
	}

	private static FileComment readJava( String sourceFile ) throws IOException {
		return readJava(new File(sourceFile));
	}

	public static FileComment readSql( File sourceFile ) throws IOException {
		Scanner r = getScanner(sourceFile);

		FileComment fileComment = new FileComment(sourceFile);

		List<String> lines = new ArrayList<String>();
		boolean isWithinFileLogList = false;
		boolean isFileLogContinueContent = false;
		FileCommentLog cmtLog = null;

		if( fileComment.getPackagePath() == null ) {
			String pkgpath = parseLine(fileComment.realAbsPath, PATTERN_SQLFILE_PACKAGEPATH, 1);
			if( pkgpath == null )
				pkgpath = "";

			fileComment.setPackagePath(pkgpath);
		}

		while( r.hasNextLine() ) {
			String line = r.nextLine();
			lines.add(line);

			if( !isWithinFileLogList && line.matches(PATTERN_SQLFILE_END_OF_COMMENT) ) {
				if( line.matches(PATTERN_SQLFILE_CONVERSION) ) {
					if( fileComment.getConVersion() == null ) {
						fileComment.setConVersion(parseLineWithTrimOrThrow(line, PATTERN_SQLFILE_CONVERSION, 1));
					}
				}
				break;
			}

			if( line.matches(PATTERN_SQLFILE_FILENAME) ) {
				if( fileComment.getDefinedFilename() == null ) {
					fileComment.setDefinedFilename(parseLineWithTrimOrThrow(line, PATTERN_SQLFILE_FILENAME, 1));
				}
			} else if( line.matches(PATTERN_SQLFILE_VERSION) ) {
				if( fileComment.getVerString() == null ) {
					fileComment.setVerString(parseLineWithTrimOrThrow(line, PATTERN_SQLFILE_VERSION, 1));
				}
			}

			if( line.matches(FileComment.PATTERN_SQLFILE_COMMENT_LOG_TITLE) ) {
				isWithinFileLogList = true;
				fileComment.setFileCommentLogTitle(line);
				continue;
			} else if( isWithinFileLogList && line.matches(PATTERN_SQLFILE_COMMENT_BLOCK_END) ) {
				isWithinFileLogList = false;
			}

			if( isWithinFileLogList ) {
				if( line.matches(FileComment.PATTERN_SQLFILE_COMMENT_LOG_LINE) ) {
					isFileLogContinueContent = false;
				} else {
					isFileLogContinueContent = true;
				}

				if( !isFileLogContinueContent ) {
					cmtLog = FileCommentLog.parseSqlFileCommentLog(line);
					fileComment.addCommentLog(cmtLog);
				} else {
					if( line.matches(FileComment.PATTERN_SQLFILE_COMMENT_LOG_CONTINUE_CONTENT) ) {
						String parsed = parseLineWithTrimOrThrow(line, FileComment.PATTERN_SQLFILE_COMMENT_LOG_CONTINUE_CONTENT, 1);
						fileComment.appendCommentLogContentToLast(parsed);
					}
				}
			}
		}
		fileComment.readFinalize(lines);
		return fileComment;
	}

	public static FileComment readSql( String sourceFileStr ) throws IOException {
		return readSql(new File(sourceFileStr));
	}

	private List<FileCommentLog> commentLogs;

	private Version conVersion;

	private String definedFilename;

	private String fileCommentLogTitle;

	private List<String> lines;

	private String moduleName;

	private String packagePath;
	private String realAbsPath;

	private String realFilename;

	private String scmLogModulePrefix;

	private Version version;

	private String verString;

	private File file;

	public FileComment( File sourceFile ) {
		this.file = sourceFile;
		this.realFilename = sourceFile.getName();
		this.realAbsPath = sourceFile.getAbsolutePath();
		this.setModuleName(calcModuleName(sourceFile));
		this.setScmLogModulePrefix(calcScmLogModulePrefix(sourceFile));
	}

	public void addCommentLog( FileCommentLog line ) {
		getCommentLogs().add(line);
	}

	public void appendCommentLogContentToLast( String line ) {
		FileCommentLog cmtLog = getCommentLogLast();
		if( cmtLog == null ) {
			throw new UnsupportedOperationException(
					"You are at wrong position. Probably commentlog was not created but you are append content...(wrong)\n "
							+ line + "\n"
							+ "file: " + realAbsPath + "\n"
							+ toString());
		}
		cmtLog.addContent(line);
		getCommentLogs().set(getCommentLogLastIndex(), cmtLog);
	}

	public FileCommentLog getCommentLogFirst() {
		if( getCommentLogs().size() > 0 )
			return getCommentLogs().get(0);
		else
			return null;
	}

	public FileCommentLog getCommentLogLast() {
		if( getCommentLogs().size() > 0 )
			return getCommentLogs().get(getCommentLogLastIndex());
		else
			return null;
	}

	public java.util.Date getCommentLogLastDate() {
		String lastDateString = getCommentLogLast().getDate();
		java.util.Date date = null;
		if( lastDateString != null && lastDateString.length() > 0 ) {
			lastDateString = lastDateString.replaceAll("\\/", "-");
			try {
				date = com.irt.data.Date.getInstance(lastDateString);
			} catch( ParseException parseEx ) {
				parseEx.printStackTrace();
				throw new RuntimeException(parseEx);
			}
		}
		return date;
	}

	private int getCommentLogLastIndex() {
		return getCommentLogs().size() - 1;
	}

	public List<FileCommentLog> getCommentLogs() {
		if( commentLogs == null )
			commentLogs = new ArrayList<FileCommentLog>();

		return commentLogs;
	}

	public Version getCommentLogVersionLast() {
		String verStr = getCommentLogLast().getVersion();
		Version ver = this.parseVersion(verStr);
		return ver;
	}

	public Version getConVersion() {
		return conVersion;
	}

	/**
	 * defined at commentlog
	 */
	public String getDefinedFilename() {
		return definedFilename;
	}

	public String getFilenamePart() {
		return getRealFilename().replaceAll("\\..*$", "");
	}

	public List<String> getLines() {
		return lines;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getPackageFilename() {
		String file = "";
		if( getPackagePath() != null && getPackagePath().length() > 0 ) {
			file += getPackagePath() + ".";
		}
		file += getRealFilename();
		return file;
	}

	public String getPackagePath() {
		return packagePath;
	}

	/**
	 * same as @{link {@link java.io.File#getName()}
	 */
	public String getRealFilename() {
		return realFilename;
	}

	public String getScmLogCommentLine() {
		if( isBroken() || "undefined".equals(getModuleName()) ) {
			try {
				String svnWcRoot = FileCommentManager.getSvnWorkingCopyRoot(file);

				String relFilePath = file.getAbsolutePath().replaceAll(svnWcRoot, "");
				if( relFilePath.startsWith("/") || relFilePath.startsWith("\\") ) {
					relFilePath = relFilePath.substring(1, relFilePath.length());
				}

				if( isScmEntryDeleted() ) {
					return "deleled: " + relFilePath;
				} else if( isScmEntryBroken() ) {
					return "broken: " + relFilePath;
				} else if( "undefined".equals(getModuleName()) ) {
					return "undefined: " + relFilePath;
				}
			} catch( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		List<String> ct = getCommentLogLast().getContent();
		String cmtlog = StringUtil.strJoin(ct.toArray(new String[ct.size()]), " " + RESERVED_SINGLELINE_DISPLAY_NEWLINE_SYMBOL + " ");

		String versionSymbol = "(v" + getVersion().getScmLogVersion() + ")";
		if( FileCommentManager.SHOW_DATE != null
				&& ( "true".equals(FileCommentManager.SHOW_DATE.toLowerCase())
						|| "yes".equals(FileCommentManager.SHOW_DATE.toLowerCase())
						|| "y".equals(FileCommentManager.SHOW_DATE.toLowerCase()) ) ) {

			String dataDate = com.irt.data.Date.getInstance(getCommentLogLastDate()).toString();
			versionSymbol = "(" + dataDate + " : " + "v" + getVersion().getScmLogVersion() + ")";
		}

		return getScmLogPackageFilename()
				+ versionSymbol
				+ " : "
				+ cmtlog;
	}

	public String getScmLogModuleLine() {
		return getScmLogModuleLine(getModuleName(), getScmLogModulePrefix());
	}

	public String getScmLogModulePrefix() {
		return scmLogModulePrefix;
	}

	public String getScmLogPackageFilename() {
		String file = "";
		if( getPackagePath() != null && getPackagePath().length() > 0 ) {
			file += getPackagePath() + ".";
		}
		file += getFilenamePart();
		return file;
	}

	public Version getVersion() {
		return version;
	}

	public String getVerString() {
		return verString;
	}

	public boolean isCustomVersion() {

		return false;
	}

	public Version parseVersion( String versionString ) {
		String ptn_ver3 = "(\\d+)\\.(\\d+)\\.?(\\d+)?";
		String ptn_customMarker = "(c)?";
		String ptn_customBranch = "\\(?(\\w+)?\\)?";
		String regex = ptn_ver3 + ptn_customMarker + ptn_customBranch;

		Pattern ptn = Pattern.compile(regex);
		Matcher m = ptn.matcher(versionString);
		if( m.find() ) {
			Version v = new Version();

			String str_major = m.group(1);
			v.setMajor(Integer.parseInt(str_major));

			String str_minor = m.group(2);
			v.setMinor(Integer.parseInt(str_minor));

			String str_patch = m.group(3);
			if( str_patch == null )
				v.setPatch(0);
			else
				v.setPatch(Integer.parseInt(str_patch));

			String str_cmarker = m.group(4);
			v.setCustomMarker(str_cmarker);

			String str_cbranch = m.group(5);
			v.setCustomBranch(str_cbranch);

			return v;
		} else {
			return null;
		}
	}

	public class ReportableException extends RuntimeException {
		public ReportableException( String message ) {
			super(message);
		}
	}

	public void readFinalize( List<String> lines ) {
		if( commentLogs != null )
			Collections.reverse(commentLogs);
		else {
			throw new ReportableException("Please write comment log or check comment log format."
					+ "\n"
					+ " file: " + realAbsPath + "\n"
					+ this.toString() + "\n"
					+ "lines: " + "\n"
					+ lines);
		}

		setLines(lines);
	}

	public void setCommentLogs( List<FileCommentLog> cmtLogs ) {
		this.commentLogs = cmtLogs;
	}

	public void setConVersion( String conVersionString ) {
		this.conVersion = parseVersion(conVersionString);
	}

	public void setDefinedFilename( String filename ) {
		this.definedFilename = filename;
	}

	public void setFileCommentLogTitle( String line ) {
		this.fileCommentLogTitle = line;
	}

	public void setLines( List<String> lines ) {
		this.lines = lines;
	}

	public void setModuleName( String moduleName ) {
		this.moduleName = moduleName;
	}

	public void setPackagePath( String packagePath ) {
		this.packagePath = packagePath;
	}

	public void setScmLogModulePrefix( String scmLogModulePrefix ) {
		this.scmLogModulePrefix = scmLogModulePrefix;
	}

	public void setVersion( Version version ) {
		this.version = version;
	}

	public void setVerString( String version ) {
		this.setVersion(parseVersion(version));

		this.verString = version;
	}

	public boolean isBroken() {
		return isScmEntryBroken() || isScmEntryDeleted();
	}

	public String toString() {
		if( isScmEntryDeleted() ) {
			return "FileComment{" + "\t"
					+ "scmEntryDeleted: " + this.isScmEntryDeleted() + "\t"
					+ "filepath: " + this.realAbsPath + "\t"
					+ "}";
		} else if( isScmEntryBroken() ) {
			return "FileComment{" + "\t"
					+ "scmEntryBroken: " + this.isScmEntryBroken() + "\t"
					+ "filepath: " + this.realAbsPath + "\t"
					+ "}";
		} else {
			return "FileComment{" + "\t"
					+ "module: " + getModuleName() + "\t"
					+ "file: " + getPackageFilename() + "\t"
					+ "version: " + getVerString() + "\t"
					+ "lastComment: " + getCommentLogLast() + "\t"
					+ "}";
		}
	}

	class Version {
		private String customBranch = "";// eg. "edi"

		private String customMarker = "";// eg. "c"

		private int major;

		private int minor;

		private int patch;

		public String getCustomBranch() {
			return customBranch;
		}

		public String getCustomMarker() {
			return customMarker;
		}

		public int getMajor() {
			return major;
		}

		public int getMinor() {
			return minor;
		}

		public int getPatch() {
			return patch;
		}

		public String getScmLogVersion() {
			String ver = major + "." + minor + "." + patch;
			if( getCustomMarker() != null && getCustomMarker().length() > 0 ) {
				ver += getCustomMarker();
			}
			return ver;
		}

		public void setCustomBranch( String customBranch ) {
			this.customBranch = customBranch;
		}

		public void setCustomMarker( String customMarker ) {
			this.customMarker = customMarker;
		}

		public void setMajor( int major ) {
			this.major = major;
		}

		public void setMinor( int minor ) {
			this.minor = minor;
		}

		public void setPatch( int patch ) {
			this.patch = patch;
		}

		public String toString() {
			String ver = getScmLogVersion();

			if( getCustomBranch() != null && getCustomBranch().length() > 0 ) {
				ver += "(" + getCustomBranch() + ")";
			}
			return ver;
		}
	}
}