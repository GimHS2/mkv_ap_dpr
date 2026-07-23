/*
 *	File Name:	FileCommentManager.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/10/30		2.2.0	create
 *
**/

package com.irt.util.cmtlog;

import com.irt.util.cmtlog.FileComment.ReportableException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sql/java 파일의 코멘트 로그를 읽고 변경사항로그 표시. </br>
 * 기본 svn status 로 체크하므로 "?"(unversioned) 소스도 변경사항로그에 표시됨.
 *
 *
 * Option:
 * 
 * <pre>
 * -DshowDate=true : version앞에 날짜 표시.
 * -DbaseDir=/home/project/rbm2 : project root full path
 * -DscmBinPath=/usr/bin/svn : svn command line program full path
 * </pre>
 *
 * <pre>
 *
 * java -DbaseDir=/home/project/rbm2 -cp webapps/dynamic/WEB-INF/classes com.irt.util.cmtlog.FileCommentManager
 * or
 * java -DbaseDir=/home/project/rbm2 -DcmtFiles=/home/project/rbm2/java-api/irtapi_src/com/irt/util/cmtlog/FileComment.java -cp webapps/dynamic/WEB-INF/classes com.irt.util.cmtlog.FileCommentManager
 * or
 * java -cp webapps/dynamic/WEB-INF/lib/irtapi22.jar com.irt.util.cmtlog.FileCommentManager /home/project/rbm2
 * or
 * java -cp webapps/dynamic/WEB-INF/lib/irtapi22.jar com.irt.util.cmtlog.FileCommentManager /home/project/rbm2/java-api/irtapi_src/com/irt/util/Utility.java,/home/project/rbm2/script/create.sql
 * or
 *
 * </pre>
 *
 */
public class FileCommentManager {

	public final static String[] FILE_EXCLUDE_DIR_NAMES = new String[] { "classes", "target",
			"java-api/test", "java-api/develop_src", "src/develop",
			"webapps/tools/customize"
	};
	public final static String[] FILE_INCLUDE_EXTENSION = new String[] { "sql", "java" };

	static FileFilter FILTER_FILE_COMMENT = new FileFilter() {
		@Override
		public boolean accept( File pathname ) {
			if( pathname.isFile() ) {
				return isCommentLogFile(pathname.getAbsolutePath());
			} else
				return true;
		}
	};

	public static String USER_DIR = System.getProperty("user.dir");

	/**
	 * show file log's date with version
	 */
	public static String SHOW_DATE = System.getProperty("showDate");

	public static String BASE_DIR = System.getProperty("baseDir");

	public static String CMT_FILES = System.getProperty("cmtFiles");

	public static String SCM_BIN_PATH = System.getProperty("scmBinPath");

	static String basename( final String absPath, final String fileSep ) {
		return absPath.substring(absPath.lastIndexOf(fileSep) + 1);
	}

	static String basePath( final String absPath, final String relPath ) {
		if( absPath.equals(relPath) ) {
			throw new IllegalArgumentException("relPath is the same as absPath.");
		}

		if( absPath.contains(relPath) ) {
			return absPath.substring(0, absPath.length() - relPath.length() - 1);
		} else {
			return absPath;
		}
	}

	static boolean isAbsolutePathForm( String path ) {
		if( path.startsWith("/") || path.matches("^.\\:.*") || path.startsWith("file://") ) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isCommentLogFile( File absPath ) {
		return isCommentLogFile(absPath.getAbsolutePath());
	}

	public static boolean isCommentLogFile( String absPath ) {
		String basename = null;
		String parent = null;
		try {
			basename = basename(absPath, File.separator);
			parent = basePath(absPath, basename);
			if( strEndsWith(FILE_INCLUDE_EXTENSION, absPath) ) {
				if( strContains(FILE_EXCLUDE_DIR_NAMES, parent) )
					return false;
				else
					return true;
			}
		} catch( Exception ex ) {
			throw new RuntimeException("absPath: " + absPath
					+ " basename: " + basename
					+ " parent: " + parent, ex);
		}
		return false;
	}

	private static Collection<File> listFileTree( File dir ) {
		Set<File> fileTree = new HashSet<File>();
		if( dir == null || dir.listFiles() == null ) {
			return fileTree;
		}
		for( File entry : dir.listFiles() ) {
			if( entry.isFile() )
				fileTree.add(entry);
			else
				fileTree.addAll(listFileTree(entry));
		}
		return fileTree;
	}

	private static Collection<File> listFileTree( File dir, FileFilter fileFilter ) {
		Set<File> fileTree = new HashSet<File>();
		if( dir == null || dir.listFiles(fileFilter) == null ) {
			return fileTree;
		}
		for( File entry : dir.listFiles(fileFilter) ) {
			if( entry.isFile() )
				fileTree.add(entry);
			else
				fileTree.addAll(listFileTree(entry, fileFilter));
		}
		return fileTree;
	}

	public static void main( String[] args ) {
		if( args.length == 0 ) {
			String helpmsg = "";
			helpmsg += "Please execute as below";
			helpmsg += "java -cp " + FileCommentManager.class.getCanonicalName();
			helpmsg += " [FILES_CSV or PROJECT_BASE_DIR]";
			helpmsg += "\n";
			helpmsg += "eg. java -cp webapps/dynamic/WEB-INF/classes " + FileCommentManager.class.getCanonicalName()
					+ " /home/base/project/rbm2";
			helpmsg += "\n";
			helpmsg += "eg. java -cp irtapi22.jar " + FileCommentManager.class.getCanonicalName()
					+ " /home/base/project/rbm2/java-api/irtapi_src/com/irt/util/Utility.java,/home/base/project/rbm2/script/user/creUSRTable.sql";
			helpmsg += "\n";
			helpmsg += "eg. java -cp irtapi22.jar " + FileCommentManager.class.getCanonicalName()
					+ " /home/project/rbm2";
			helpmsg += "\n";
			helpmsg += "( svn binary should be in PATH variable when invoked with PROJECT_BASE_DIR option ): ";
			helpmsg += "\n";
			System.out.println(helpmsg);
		} else {
			String prjBaseDirOrfilesCsv = args[0];

			System.out.println("arg[0]: \n" + prjBaseDirOrfilesCsv);

			if( prjBaseDirOrfilesCsv.contains(",") ) {
				FileCommentManager mgr = new FileCommentManager();
				mgr.setFilesCsv(prjBaseDirOrfilesCsv);
				if( BASE_DIR == null ) {
					throw new IllegalStateException("baseDir is mandatory.");
				}
				mgr.process(System.out);
			} else {
				FileCommentManager mgr = new FileCommentManager();
				mgr.baseDirPath = prjBaseDirOrfilesCsv;
				if( CMT_FILES != null ) {
					mgr.setFilesCsv(CMT_FILES);
				}
				mgr.process(System.out);
			}
		}
	}

	protected static boolean strContains( String[] array, String instance ) {
		for( int i = 0; i < array.length; i++ ) {
			if( instance.contains(array[i]) )
				return true;
		}
		return false;
	}

	protected static boolean strEndsWith( String[] array, String instance ) {
		for( int i = 0; i < array.length; i++ ) {
			if( instance.endsWith(array[i]) )
				return true;
		}
		return false;
	}

	private String baseDirPath;

	private String filesCsv;

	private String scmBinPath;

	public FileCommentManager() {
		this.scmBinPath = SCM_BIN_PATH;
		this.baseDirPath = BASE_DIR;
	}

	public FileCommentManager( String scmBinPath, String baseDirPath ) {
		this.scmBinPath = scmBinPath;
		this.baseDirPath = baseDirPath;
	}

	public String getFilesCsv() {
		return filesCsv;
	}

	private Map<String, String> filesMarkers = new HashMap<String, String>();

	protected static String getSvnWorkingCopyRoot( File svnManagedFileOrDir ) throws IOException {
		String[] cmds = new String[] {
				"svn", "info", svnManagedFileOrDir.getAbsolutePath(), "--show-item", "wc-root", "--no-newline"
		};

		ProcessBuilder builder = new ProcessBuilder(cmds);
		builder.redirectErrorStream(true);
		Process process = builder.start();
		InputStream is = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String line = reader.readLine();
		if( line == null )
			return null;
		else
			return line;
	}

	public List<String> getFilesFromSvnStatus( File baseDir ) throws IOException, InterruptedException {
		String[] cmds = new String[] {
				"svn", "status", baseDir.getAbsolutePath()
		};

		ProcessBuilder builder = new ProcessBuilder(cmds);
		builder.redirectErrorStream(true);
		Process process = builder.start();
		InputStream is = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		List<String> listOfFiles = new ArrayList<String>();
		String line = null;
		System.out.println("### getFilesFromStatus start.");
		while( ( line = reader.readLine() ) != null ) {
			System.out.println(line);

			String markerRegex = "^([\\?|A|M|D|!])\\s+.*";
			String marker = "";
			if( Pattern.matches(markerRegex, line) ) {
				Pattern ptn = Pattern.compile(markerRegex);
				Matcher mt = ptn.matcher(line);
				if( mt.find() && mt.groupCount() > 0 ) {
					marker = mt.group(1);
				}
			}

			String filename = line.replaceAll("^[\\?|A|M|D|!]\\s+", "");

			System.out.print("marker: " + marker);
			System.out.println("\tfile: " + filename);

			filesMarkers.put(filename, marker);

			listOfFiles.add(filename.trim());
		}
		System.out.println("### getFilesFromStatus done.");
		System.out.println("");

		return listOfFiles;
	}

	/**
	 * Comment Logs By Module( irtapi, irbmapi etc )
	 */
	protected String getScmLogsByModule( List<FileComment> cmts ) {
		Map<String, List<FileComment>> modules = new HashMap<String, List<FileComment>>();
		for( FileComment cmt : cmts ) {
			if( cmt.isBroken() ) {
				String brokenGroup = "$broken";
				List<FileComment> groups = modules.get(brokenGroup);
				if( groups == null ) {
					groups = new ArrayList<FileComment>();
				}
				groups.add(cmt);

				modules.put(brokenGroup, groups);
			} else {
				List<FileComment> groups = modules.get(cmt.getModuleName());
				if( groups == null ) {
					groups = new ArrayList<FileComment>();
				}
				groups.add(cmt);

				modules.put(cmt.getModuleName(), groups);
			}
		}

		String ret = "";
		List<String> keyset = new ArrayList<String>(modules.keySet());
		Comparator<String> cmp = new Comparator<String>() {
			@Override
			public int compare( String o1, String o2 ) {
				String line1 = FileComment.getScmLogModuleLine(o1);
				String line2 = FileComment.getScmLogModuleLine(o2);
				return line1.compareTo(line2);
			}
		};
		Collections.sort(keyset, cmp);

		for( String moduleName : keyset ) {
			ret += FileComment.getScmLogForModule(modules.get(moduleName), moduleName);
		}
		return ret;
	}

	/**
	 * Comment Log Status Report
	 */
	public void process( PrintStream out ) {
		List<String> statusFiles = null;
		try {
			if( getFilesCsv() != null ) {
				statusFiles = Arrays.asList(getFilesCsv().split(","));
			} else {
				statusFiles = getFilesFromSvnStatus(new File(baseDirPath));
			}

			out.println("statusFiles: " + statusFiles);

			if( statusFiles == null || statusFiles.size() <= 0 ) {
				System.out.println(FileCommentManager.class.getSimpleName() + " : " + " No changed scm files to process.");
			} else {
				List<FileComment> cmts = readFiles(statusFiles.toArray(new String[statusFiles.size()]));
				String scmLog = getScmLogsByModule(listDedup(cmts));
				if( scmLog == null || scmLog.length() <= 0 ) {
					System.out.println(FileCommentManager.class.getSimpleName() + " : " + " No changed comment files to process.");
				} else {
					out.println("");
					out.println(scmLog);
				}
			}
		} catch( IOException ioEx ) {
			throw new RuntimeException(ioEx);
		} catch( InterruptedException intruptEx ) {
			throw new RuntimeException(intruptEx);
		}
	}

	/**
	 * remove duplicates
	 */
	protected List<FileComment> listDedup( List<FileComment> cmts ) {

		Set<FileComment> uniqFileComments = new TreeSet<FileComment>(new Comparator<FileComment>() {
			@Override
			public int compare( FileComment o1, FileComment o2 ) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		uniqFileComments.addAll(cmts);

		return new ArrayList<FileComment>(uniqFileComments);
	}

	protected List<FileComment> readDirs( String... dirs ) throws IOException {
		List<FileComment> cmts = new ArrayList<FileComment>();
		for( String dir : dirs ) {
			File srcDir = new File(dir);
			Collection<File> files = listFileTree(srcDir, FILTER_FILE_COMMENT);
			for( File file : files ) {
				FileComment c = FileComment.read(file);
				cmts.add(c);
			}
		}

		return cmts;
	}

	protected List<FileComment> readDirs( List<FileComment> cmts, String... dirs ) throws IOException {
		for( String dir : dirs ) {
			File srcDir = new File(dir);
			Collection<File> files = listFileTree(srcDir, FILTER_FILE_COMMENT);
			for( File file : files ) {
				FileComment c = FileComment.read(file);
				cmts.add(c);
			}
		}

		return cmts;
	}

	protected List<FileComment> readFiles( File baseDir, String... relFiles ) throws IOException {
		List<String> absFiles = new ArrayList<String>();
		for( String relFile : relFiles ) {
			absFiles.add(new File(baseDir.getAbsolutePath(), relFile).getAbsolutePath());
		}
		return readFiles(absFiles.toArray(new String[absFiles.size()]));
	}

	private FileComment readFile( String absOrRelPath ) throws IOException {
		FileComment cmt = null;
		String marker = this.filesMarkers.get(absOrRelPath);

		if( "D".equals(marker) ) {
			cmt = FileComment.makeDeleted(absOrRelPath);
		} else if( "!".equals(marker) ) {
			cmt = FileComment.makeBroken(absOrRelPath);
		} else {
			cmt = FileComment.makeRead(absOrRelPath);
		}
		return cmt;
	}

	protected List<FileComment> readFiles( String... absOrRelOrDirOrFile ) throws IOException {
		List<FileComment> cmts = new ArrayList<FileComment>();
		List<Entry<String, Throwable>> errors = new ArrayList<Entry<String, Throwable>>();
		try {
			for( String absOrRelPath : absOrRelOrDirOrFile ) {
				File file = null;

				if( isAbsolutePathForm(absOrRelPath) ) {
					file = new File(absOrRelPath);
					if( file.isDirectory() ) {
						cmts.addAll(readDirs(cmts, absOrRelPath));
					} else {
						if( isCommentLogFile(absOrRelPath) ) {
							try {
								FileComment cmt = readFile(absOrRelPath);
								// System.out.println(cmt);
								cmts.add(cmt);
							} catch( ReportableException rpt ) {
								errors.add(new AbstractMap.SimpleEntry(absOrRelPath, rpt));
							}
						}
					}
				} else {
					file = new File(USER_DIR, absOrRelPath);
					if( file.isDirectory() ) {
						cmts.addAll(readDirs(cmts, absOrRelPath));
					} else {
						if( isCommentLogFile(absOrRelPath) ) {
							try {
								FileComment cmt = readFile(absOrRelPath);
								// System.out.println(cmt);
								cmts.add(cmt);
							} catch( ReportableException rpt ) {
								errors.add(new AbstractMap.SimpleEntry(absOrRelPath, rpt));
							}
						}
					}
				}
			}
		} finally {
			if( !errors.isEmpty() ) {
				System.out.println("error files: ");
				for( Entry<String, Throwable> error : errors ) {
					System.out.println(error.getKey());
				}

				System.out.println("errors details ");
				for( Entry<String, Throwable> error : errors ) {
					System.out.println(error.getKey() + " : " + error.getValue());
				}

				throw new IllegalStateException("error count: " + errors.size());
			}
		}

		return cmts;
	}

	public void setFilesCsv( String filesCsv ) {
		this.filesCsv = filesCsv;
	}

}
