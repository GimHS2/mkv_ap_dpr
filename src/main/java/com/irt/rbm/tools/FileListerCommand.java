/*
 *	File Name:	FileListerCommand.java
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

import com.irt.data.Record;
import com.irt.data.format.PatternRecordFormat;
import com.irt.util.CSVReader;
import com.irt.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class FileListerCommand {

	///////////////////////////////////////////////////
	///// Bash
	//
	// find
	// -newerXY reference
	// Compares the timestamp of the current file with reference. The
	// reference argument is normally the name of a file (and one of
	// its timestamps is used for the comparison) but it may also be a
	// string describing an absolute time. X and Y are placeholders
	// for other letters, and these letters select which time belonging
	// to how reference is used for the comparison.
	//
	// a The access time of the file reference
	// B The birth time of the file reference
	// c The inode status change time of reference
	// m The modification time of the file reference
	// t reference is interpreted directly as a time
	//
	///////////////////////////////////////////////////
	private static final String SH_FIND_OPT_FILTER_BY_MODIFIEDTIME = "-newermt";// usually well supported
	private static final String SH_FIND_OPT_FILTER_BY_CREATEDTIME = "-newerBt";// may not supported by older unix 'find' command
	private static final String SH_FIND_OPT_FILTER_BY_ACCESSEDTIME = "-newerat";// usually not up-to-date

	private static final String SH_CTX_LANG = "LANG=en_US.UTF-8";

	private static final String SH_FIND_GREP_IGNORE_EXCEPTION = " grep -v 'Permission denied' | grep -v 'No such file or directory' ";

	private static final String SH_FIND_RANGE_WITHUSER_COMMAND = " find ${directory} -maxdepth ${maxDepth} -not -path \"${directory}/.${checksum}/*\"-user `whoami` -regextype posix-extended -regex '${regex}' ${byRefTime} \"${gtTimestamp}\" ! ${byRefTime} \"${ltTimestamp}\" -type f 2>&1 ";

	private static final String SH_FIND_RANGE_COMMAND = " find \"${directory}\" -maxdepth ${maxDepth} -not -path \"${directory}/.${checksum}/*\" -regextype posix-extended -regex '${regex}' ${byRefTime} \"${gtTimestamp}\" ! ${byRefTime} \"${ltTimestamp}\" -type f 2>&1 ";

	private static final String SH_LS_FULLTIME_COMMAND = " ls -al -t --full-time ";

	private static final String SH_LS_ISOTIME_COMMAND = " ls -al -t --time-style=+\"%Y-%m-%dT%H:%M:%S%:z\" ";

	private static final String SH_XARGS_LS_COMMAND = " xargs -I{} " + SH_LS_FULLTIME_COMMAND + " {} ";

	private static final String SH_SORTEXP_MODBASE = " -k6,6V -k7,7V -k8,8V -k9,9d -k5,5V ";// by ModTime(6,7,8)#ASC, by Name#ASC, by Size#ASC
	private static final String SH_SORTEXP_NAMEASE = " -k9,9d -k6,6V -k7,7V -k8,8V -k5,5V ";// by Name#ASC, by ModTime(6,7,8)#ASC, by Size#ASC

	private static final String SH_LS_SORT_COMMAND = " sort ${sortExp} ";

	private static final String SH_CNTLINE_COMMAND = " wc -l ";
	private static final String SH_TPL_FIND_RANGE_COMMAND_COUNT = SH_FIND_RANGE_COMMAND + " | " + SH_FIND_GREP_IGNORE_EXCEPTION + " | "
			+ SH_CNTLINE_COMMAND;
	private static final String SH_TPL_FIND_RANGE_COMMAND_FILES = SH_FIND_RANGE_COMMAND + " | " + SH_FIND_GREP_IGNORE_EXCEPTION + " | "
			+ SH_XARGS_LS_COMMAND + " | " + SH_LS_SORT_COMMAND;

	private static final String SH_TPL_GET_TIMEZONE_ID = "cd /usr/share/zoneinfo && find * -type f -exec sh -c \"diff -q /etc/localtime '{}' >/dev/null && echo {}\" \\;";
	private final static String SH_TOUCH_TIMESTAMP_FORMAT = "yyyyMMddHHmm";

	///////////////////////////////////////////////////
	///// Powershell
	///////////////////////////////////////////////////

	private static final String PS_FIND_OPT_FILTER_BY_MODIFIEDTIME = "LastWriteTime";
	private static final String PS_FIND_OPT_FILTER_BY_CREATEDTIME = "CreationTime";
	private static final String PS_FIND_OPT_FILTER_BY_ACCESSEDTIME = "LastAccessTime";// usually not enabled by default

	private final static String PS_FN_USING_CULTURE = ""
			+ "Function Using-Culture ( "
			+ "[System.Globalization.CultureInfo]$culture = (throw \"USAGE: Using-Culture -Culture culture -Script {scriptblock}\"), "
			+ "[ScriptBlock]$script= (throw \"USAGE: Using-Culture -Culture culture -Script {scriptblock}\")) "
			+ "{ "
			+ "    $OldCulture = [System.Threading.Thread]::CurrentThread.CurrentCulture "
			+ "    trap  "
			+ "    { "
			+ "        [System.Threading.Thread]::CurrentThread.CurrentCulture = $OldCulture "
			+ "    } "
			+ "    [System.Threading.Thread]::CurrentThread.CurrentCulture = $culture "
			+ "    Invoke-Command $script "
			+ "    [System.Threading.Thread]::CurrentThread.CurrentCulture = $OldCulture "
			+ "} "
			+ "";

	private final static String PS_FN_GET_CHILD_ITEM_TO_DEPTH = ""
			+ "	Function Get-ChildItemToDepth { "
			+ "	    Param( "
			+ "	        [String]$Path = $$PWD, "
			+ "	        [String]$Filter = \"*\", "
			+ "	        [Byte]$ToDepth = 255, "
			+ "	        [Byte]$CurrentDepth = 0, "
			+ "	        [Switch]$DebugMode "
			+ "	    ) "
			+ " "
			+ "	    $$CurrentDepth++ "
			+ "	    If ($$DebugMode) { "
			+ "	        $$DebugPreference = \"Continue\" "
			+ "	    } "
			+ " "
			+ "	    Get-ChildItem $Path | %{ "
			+ "	        $_ | ?{ $$_.Name -Like $$Filter } "
			+ " "
			+ "	        If ($$_.PsIsContainer) { "
			+ "	            If ($$CurrentDepth -le $$ToDepth) { "
			+ " "
			+ "	                # Callback to this function "
			+ "	                Get-ChildItemToDepth -Path $$_.FullName -Filter $$Filter ` "
			+ "	                  -ToDepth $$ToDepth -CurrentDepth $$CurrentDepth "
			+ "	            } "
			+ "	            Else { "
			+ "	                Write-Debug $(\"Skipping GCI for Folder: $$($$_.FullName) \" + ` "
			+ "	                  \"(Why: Current depth $$CurrentDepth vs limit depth $$ToDepth)\") "
			+ "	            } "
			+ "	        } "
			+ "	    } "
			+ "	} "
			+ "";

	private final static String PS_FN_TO_ISO8601 = "(Get-Date -Format \"o\")";

	private final static String PS_FOLDER_ONLY = "$_.PsIsContainer";

	private final static String PS_FILE_ONLY = "!" + PS_FOLDER_ONLY;

	private final static String PS_USING_CULTURE_START = " using-culture -Culture en-US -Script { ";
	private final static String PS_USING_CULTURE_END = " } ";

	private final static String PS_FIND_RANGE_COMMAND = ""
			+ " Get-ChildItemToDepth -ToDepth ${maxDepth} \"${directory}\" "
			+ " -Exclude \"${directory}\\.${checksum}\\*\" "
			+ "| Where-Object -FilterScript {$$_.Name -match '${regex}' -and " + PS_FILE_ONLY + " } "
			+ "| ? {$$_.${byRefTime} -gt (Get-Date \"${gtTimestamp}\")} "
			+ "| ? {$$_.${byRefTime}-lt (Get-Date \"${ltTimestamp}\")} "
			+ "";

	private final static String PS_TPL_FIND_RANGE_COMMAND_COUNT = ""
			+ PS_USING_CULTURE_START
			+ PS_FIND_RANGE_COMMAND
			+ " 2>&1 "
			+ "| Measure-Object | Select count"
			+ PS_USING_CULTURE_END
			+ "";

	private final static String PS_LS_HEADERS = ""
			+ " @{Name='" + LsHeader.PermStr.name() + "'; Expression={$$_.Mode}} "
			+ " , @{Name='" + LsHeader.PermNum.name() + "'; Expression={'-1'}} "// cannot find a way to numbering for permission on windows.
			+ " , @{Name='" + LsHeader.Owner.name() + "'; Expression={\"\"\"$$($$_.GetAccessControl().Owner)\"\"\"}} "
			+ " , @{Name='" + LsHeader.Group.name() + "'; Expression={\"\"\"$$($$_.GetAccessControl().Group)\"\"\"}} "
			+ " , @{Name='" + LsHeader.Size.name() + "'; Expression={$$_.Length}} "
			+ " , @{Name='" + LsHeader.ModTime1.name() + "'; Expression={$$_.LastWriteTime.ToString(\"yyyy-MM-dd\")}} "
			+ " , @{Name='" + LsHeader.ModTime2.name() + "'; Expression={$$_.LastWriteTime.ToString(\"HH:mm:ss.fffffff\")}} "
			+ " , @{Name='" + LsHeader.ModTime3.name() + "'; Expression={$$_.LastWriteTime.ToString(\"zzz\")}} "
			+ " , @{Name='" + LsHeader.Name.name() + "'; Expression={\"\"\"$$($$_.Directory)\\$$($$_.Name)\"\"\"}} "
			+ "";

	private final static String PS_SORTEXP_MODBASE = " @{E={$$_.LastWriteTime}; Ascending=$$true} "
			+ " @{E={$($$_.Directory\\$$_.Name)}; Ascending=$$true} "
			+ " @{E={[long]$$_.Length)}; Ascending=$$true} ";

	private final static String PS_SORTEXP_NAMEBASE = " @{E={$($$_.Directory\\$$_.Name)}; Ascending=$$true} "
			+ " @{E={$$_.LastWriteTime}; Ascending=$$true} "
			+ " @{E={[long]$$_.Length)}; Ascending=$$true} ";

	private final static String PS_LS_SORT_COMMAND = " Sort ${sortExp}";

	private final static String PS_TPL_FIND_RANGE_COMMAND_FILES = ""
			+ PS_USING_CULTURE_START
			+ PS_FIND_RANGE_COMMAND
			+ "| Sort LastWriteTime -descending "
			+ "| Format-Table -HideTableHeader -Property "
			+ PS_LS_HEADERS
			+ " 2>&1 "
			+ "| Out-String -Width 1024 "
			+ PS_USING_CULTURE_END
			+ "";

	private static final char DELIM_SPACE = ' ';

	private String executeOs;

	FindExecutor findExecutor;

	public FindOption createFindOption() {
		return new FindOption();
	}

	/**
	 * Execute a bash command. We can handle complex bash commands including
	 * multiple executions (; | && ||), quotes, expansions ($), escapes (\), e.g.:
	 * "cd /abc/def; mv ghi 'older ghi '$(whoami)"
	 * 
	 * @param command
	 * @return true if bash got started, but your command may have failed.
	 */
	List<String> executeUnixCommand( String command ) {
		Runtime r = Runtime.getRuntime();

		String contextCommand = SH_CTX_LANG + " ";

		java.lang.Process proTz = null;
		String timeZoneIds = "";
		String timeZoneId = "";
		try {
			proTz = r.exec(new String[] { "bash", "-c", contextCommand + SH_TPL_GET_TIMEZONE_ID });
			proTz.waitFor();
			InputStream is = proTz.getInputStream();
			java.util.Scanner scan = new java.util.Scanner(is).useDelimiter("\\A");
			timeZoneIds = scan.hasNext() ? scan.next() : "";
			if( timeZoneIds != null && timeZoneIds.trim().length() > 0 ) {
				timeZoneId = timeZoneIds.trim().contains("\n") ? timeZoneIds.trim().split("\n")[0] : timeZoneIds;
			}
			scan.close();
		} catch( Exception e ) {
			getLogger().error("Failed to execute command: " + SH_TPL_GET_TIMEZONE_ID, e);
		} finally {
			if( proTz != null )
				proTz.destroy();
		}

		if( getLogger().isDebugEnabled() ) {
			getLogger().debug("Executing command(tz:" + timeZoneId.trim() + "):\n   " + command);
		}

		// Use bash -c so we can handle things like multi commands separated by ; and
		// things like quotes, $, |, and \. My tests show that command comes as
		// one argument to bash, so we do not need to quote it to make it one thing.
		// Also, exec may object if it does not have an executable file as the first thing,
		// so having bash here makes it happy provided bash is installed and in path.
		String[] commands = { "bash", "-c", contextCommand + command };
		List<String> lines = new ArrayList<String>();
		java.lang.Process p = null;
		try {
			p = r.exec(commands);

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
			String line;
			while( ( line = br.readLine() ) != null ) {
				lines.add(line);
			}
			br.close();
		} catch( Exception e ) {
			getLogger().error("Failed to execute command: " + command);
		} finally {
			if( p != null )
				p.destroy();
		}

		return lines;
	}

	List<String> executeWindowsCommand( String command ) {
		Runtime r = Runtime.getRuntime();

		String timeZoneId = TimeZone.getDefault().getID();

		if( getLogger().isDebugEnabled() ) {
			getLogger().debug("Executing command(tz:" + timeZoneId.trim() + "):\n   " + command);
		}

		String contextCommand = PS_FN_USING_CULTURE + " ; " + PS_FN_GET_CHILD_ITEM_TO_DEPTH + " ; ";

		String[] commands = { "powershell", "-command", contextCommand + command };
		List<String> lines = new ArrayList<String>();
		java.lang.Process p = null;
		try {
			p = r.exec(commands);

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
			String line;
			while( ( line = br.readLine() ) != null ) {
				lines.add(line);
			}
			br.close();
		} catch( Exception e ) {
			getLogger().error("Failed to execute command: " + command);
		} finally {
			if( p != null )
				p.destroy();
		}

		return lines;
	}

	public int findRangedFileCount( FindOption opt ) {
		return findRangedFileCount(opt, ( getFindExecutor() == null ? new LocalFindExecutor() : getFindExecutor() ));
	}

	public int findRangedFileCount( FindOption opt, FindExecutor exec ) {
		List<String> ret = exec.find(opt, true, isWin());
		int cnt = 0;
		try {
			if( ret != null && ret.size() > 0 )
				cnt = Integer.parseInt(ret.get(0));
		} catch( NumberFormatException nfe ) {
			cnt = -1;
		}

		return cnt;
	}

	public List<String> findRangedFileLines( FindOption opt ) {
		return findRangedFileLines(opt, ( getFindExecutor() == null ? new LocalFindExecutor() : getFindExecutor() ));
	}

	public List<String> findRangedFileLines( FindOption opt, FindExecutor exec ) {
		return exec.find(opt, false, isWin());
	}

	public String getExecuteOs() {
		return executeOs;
	}

	public FindExecutor getFindExecutor() {
		return findExecutor;
	}

	private Logger getLogger() {
		return Logger.getLogger(FileListerCommand.class);
	}

	public static String getLsLineData( String line, int colidx ) {
		String[] vals = CSVReader.split(line, DELIM_SPACE, Character.MIN_VALUE);
		if( vals != null ) {

			if( LsHeader.Owner.colidx() == colidx || LsHeader.Group.colidx() == colidx ) {
				try {
					return vals[colidx].replaceAll("^\"", "").replaceAll("\"$", "");
				} catch( java.lang.ArrayIndexOutOfBoundsException arrEx ) {
					throw new java.lang.ArrayIndexOutOfBoundsException("vals: " + java.util.Arrays.asList(vals));
				}
			} else if( LsHeader.Name.colidx() == colidx ) {
				try {

					if( vals.length > LsHeader.Name.colidx() ) {
						String[] nameMaybeSpaced = new String[vals.length - LsHeader.Name.colidx()];
						System.arraycopy(vals, LsHeader.Name.colidx(), nameMaybeSpaced, 0, vals.length - LsHeader.Name.colidx());
						return StringUtil.strJoin(nameMaybeSpaced, " ").replaceAll("^\"", "").replaceAll("\"$", "");
					} else {
						return vals[colidx].replaceAll("^\"", "").replaceAll("\"$", "");
					}
				} catch( java.lang.ArrayIndexOutOfBoundsException arrEx ) {
					throw new java.lang.ArrayIndexOutOfBoundsException("vals: " + java.util.Arrays.asList(vals));
				}
			} else {
				return vals[colidx];
			}
		}

		return null;
	}

	// public String[] getLsLineData( String line ) {
	// return CSVReader.split(line, ' ', Character.MIN_VALUE);
	// }

	String getRangedFileCountCommand( Map<String, Object> map, boolean isWin ) {
		return isWin
				? PatternRecordFormat.getInstance(PS_TPL_FIND_RANGE_COMMAND_COUNT).format(map, null)
				: PatternRecordFormat.getInstance(SH_TPL_FIND_RANGE_COMMAND_COUNT).format(map, null);
	}

	String getRangedFileLinesCommand( Map<String, Object> map, boolean isWin ) {
		return isWin
				? PatternRecordFormat.getInstance(PS_TPL_FIND_RANGE_COMMAND_FILES).format(map, null)
				: PatternRecordFormat.getInstance(SH_TPL_FIND_RANGE_COMMAND_FILES).format(map, null);
	}

	private boolean isWin() {
		String decidedOs = executeOs == null ? System.getProperty("os.name", "") : executeOs;

		if( decidedOs.startsWith("Win") || decidedOs.startsWith("win") ) {
			return true;
		} else {
			return false;
		}
	}

	public void setExecuteOs( String executeOs ) {
		this.executeOs = executeOs;
	}

	public void setFindExec( FindExecutor findExec ) {
		this.findExecutor = findExec;
	}

	interface FindExecutor {
		List<String> find( FindOption opt, boolean isCountQuery, boolean isWin );
	}

	public class FindOption {
		public final static String REGEX_FILE_ALL = ".*";
		public final static String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
		public final static int DEFAULT_MAX_DEPTH = 1;
		public final static String DEFAULT_CHECKSUM = "MD5";

		private RefTimeType byRefTime = RefTimeType.ModifiedTime;

		private int maxDepth;

		public String getChecksum() {
			return checksum;
		}

		public void setChecksum( String checksum ) {
			this.checksum = checksum;
		}

		private String checksum = DEFAULT_CHECKSUM;

		private String regex;

		public SortBaseType getBySortBase() {
			return bySortBase;
		}

		public void setBySortBase( SortBaseType bySortBase ) {
			this.bySortBase = bySortBase;
		}

		public void setBySortBase( String bySortBase ) {
			this.bySortBase = SortBaseType.valueOf(bySortBase);
		}

		private SortBaseType bySortBase = SortBaseType.ModAscBase;

		private long gtTime;

		private long ltTime;

		private String directory;

		public RefTimeType getByRefTime() {
			return byRefTime;
		}

		public String getDirectory() {
			return directory;
		}

		public long getGtTime() {
			return gtTime;
		}

		public long getLtTime() {
			return ltTime;
		}

		public int getMaxDepth() {
			return maxDepth;
		}

		public String getRegex() {
			return regex;
		}

		public void setByRefTime( RefTimeType byRefTime ) {
			this.byRefTime = byRefTime;
		}

		public void setByRefTime( String byRefTime ) {
			this.byRefTime = RefTimeType.valueOf(byRefTime);
		}

		public void setDirectory( String directory ) {
			this.directory = directory;
		}

		public void setGtTime( long gtTime ) {
			this.gtTime = gtTime;
		}

		public void setLtTime( long ltTime ) {
			this.ltTime = ltTime;
		}

		public void setMaxDepth( int maxDepth ) {
			this.maxDepth = maxDepth;
		}

		public void setRegex( String regex ) {
			this.regex = regex;
		}

		public Map<String, Object> toMap( boolean isWin ) {
			if( directory == null || directory.length() == 0 ) {
				throw new IllegalStateException("directory is mandatory.");
			} else if( gtTime <= 0 ) {
				throw new IllegalStateException("gtTime is mandatory.");
			} else if( ltTime <= 0 ) {
				throw new IllegalStateException("ltTime is mandatory.");
			}

			Map<String, Object> map = Record.createMap("directory", directory);

			map.put("byRefTime", byRefTime.compileRefTime(byRefTime, isWin));
			map.put("sortExp", bySortBase.compileSortExp(bySortBase, isWin));

			if( regex == null || regex.length() == 0 ) {
				map.put("regex", REGEX_FILE_ALL);
			} else if( regex.startsWith("^") ) {
				if( isWin ) {
					map.put("regex", regex);// on windows, match only to file name
				} else {
					// because in unix 'find' command full path is used to match.(so replace '^' to last path seperator)
					map.put("regex", regex.replaceFirst("\\^", ".*\\/"));
				}
			}

			SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
			map.put("gtTimestamp", sdf.format(gtTime));
			map.put("ltTimestamp", sdf.format(ltTime));

			map.put("checksum", checksum);

			if( maxDepth > 0 ) {
				map.put("maxDepth", maxDepth);
			} else {
				map.put("maxDepth", DEFAULT_MAX_DEPTH);
			}

			return map;
		}

		public FindOption withByRefTime( RefTimeType value ) {
			setByRefTime(value);
			return this;
		}

		public FindOption withDirectory( String value ) {
			setDirectory(value);
			return this;
		}

		public FindOption withGtTime( long value ) {
			setGtTime(value);
			return this;
		}

		public FindOption withLtTime( long value ) {
			setLtTime(value);
			return this;
		}

		public FindOption withMaxDepth( int value ) {
			setMaxDepth(value);
			return this;
		}

		public FindOption withRegex( String value ) {
			setRegex(value);
			return this;
		}

		public FindOption withBySortBase( SortBaseType value ) {
			setBySortBase(value);
			return this;
		}

	}

	private class LocalFindExecutor implements FindExecutor {
		@Override
		public List<String> find( FindOption opt, boolean isCountQuery, boolean isWin ) {
			Map<String, Object> optMap = opt.toMap(isWin);

			if( isCountQuery ) {
				String command = getRangedFileCountCommand(optMap, isWin);
				return ( isWin ? executeWindowsCommand(command) : executeUnixCommand(command) );
			} else {
				String command = getRangedFileLinesCommand(optMap, isWin);
				return ( isWin ? executeWindowsCommand(command) : executeUnixCommand(command) );
			}
		}
	}

	public enum LsHeader {
		PermStr( 0 ), PermNum( 1 ), Owner( 2 ), Group( 3 ), Size( 4 ), //
		ModTime1( 5 ), ModTime2( 6 ), ModTime3( 7 ), Name( 8 );

		private final int colidx;

		LsHeader( int colidx ) {
			this.colidx = colidx;
		}

		public int colidx() {
			return colidx;
		}

		/**
		 * expect following formats only
		 * 
		 * -bash(ls -al --full-time)
		 * 
		 * <pre>
		 * -rw------- 1 base base 32 2018-12-04 13:23:08.730239293 +0900 /tmp/some-file
		 * </pre>
		 * 
		 * -powershell({@link FileListerCommand#PS_LS_HEADERS})
		 * 
		 * <pre>
		 * -a--- -1 "base-PC\base" "base-PC\None" 5775016 2017-11-28 18:21:36.0850156 +09:00 "C:\Windows\Temp\some spaced file"
		 * </pre>
		 * 
		 */
		public long parseModifiedTime( String datePart, String timePart, String zoneOffsetPart ) throws ParseException {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S Z");
			Date date;
			String timeShortPart = null;
			String milliPart = "";
			try {
				if( timePart.contains("\\.") ) {
					String[] timeParts = timePart.split("\\.");
					timeShortPart = timeParts[0];
					if( timeParts[1] != null && timeParts[1].length() > 0 ) {
						int i = 0;
						for( ; i < 3 || i < timeParts[1].length(); i++ ) {
							milliPart += timeParts[1].charAt(i);
						}
					} else {
						milliPart = "000";
					}
				} else {
					timeShortPart = timePart;
					milliPart = "000";
				}

				return sdf.parse(datePart + " " + timeShortPart + "." + milliPart + " " + zoneOffsetPart).getTime();
			} catch( ParseException parseEx ) {
				throw new IllegalStateException("cannot parse date with input:"
						+ " '" + datePart + "'"
						+ " '" + timePart + "'(" + timeShortPart + ")"
						+ " '" + zoneOffsetPart + "'", parseEx);
			}
		}
	}

	public interface LsLineSelector {

		public String select( String lsLine );

	}

	public enum RefTimeType {
		AccessedTime, CreatedTime, ModifiedTime;

		public String compileRefTime( RefTimeType byRefTime, boolean isWin ) {
			String refTimeExp = "";
			if( isWin ) {
				switch( byRefTime ) {
				case CreatedTime:
					refTimeExp = PS_FIND_OPT_FILTER_BY_CREATEDTIME;
					break;
				case ModifiedTime:
					refTimeExp = PS_FIND_OPT_FILTER_BY_MODIFIEDTIME;
					break;
				case AccessedTime:
					// usually os update 'AccessdTime' later. dont relyon accesstime.
					// s_byRefTime = PS_FIND_OPT_FILTER_BY_ACCESSEDTIME;
					throw new UnsupportedOperationException(
							"Do not rely on access time. Usually OS does not keep up-to-date AccessedTime information.");
				default:
					throw new UnsupportedOperationException("type: " + byRefTime);
				}
			} else {
				switch( byRefTime ) {
				case CreatedTime:
					refTimeExp = SH_FIND_OPT_FILTER_BY_CREATEDTIME;
					break;
				case ModifiedTime:
					refTimeExp = SH_FIND_OPT_FILTER_BY_MODIFIEDTIME;
					break;
				case AccessedTime:
					// usually os update 'AccessdTime' later. dont relyon accesstime.
					// s_byRefTime = SH_FIND_OPT_FILTER_BY_ACCESSEDTIME;
					throw new UnsupportedOperationException(
							"Do not rely on access time. Usually OS does not keep up-to-date AccessedTime information.");
				default:
					throw new UnsupportedOperationException("type: " + byRefTime);
				}
			}

			return refTimeExp;
		}
	}

	public enum SortBaseType {
		ModAscBase, ModDescBase, NameAscBase, NameDescBase;

		public String compileSortExp( SortBaseType bySortBase, boolean isWin ) {
			String sortExp = "";
			if( isWin ) {
				String sortExpFormat = " @{E={%s}; A=$$%b} "
						+ " @{E={%s}; A=$$%b} "
						+ " @{E={%s}; A=$$%b} "
						+ "'";
				switch( bySortBase ) {
				case ModAscBase:
					sortExp = String.format(sortExpFormat, new Object[] {
							"$$_.LastWriteTime", true,
							"$$($$_.Directory\\$$_.Name)", true,
							"[long]$$_.Length", true,
					});
					break;
				case ModDescBase:
					sortExp = String.format(sortExpFormat, new Object[] {
							"$$_.LastWriteTime", false,
							"$$($$_.Directory\\$$_.Name)", true,
							"[long]$$_.Length", true,
					});
					break;
				case NameAscBase:
					sortExp = String.format(sortExpFormat, new Object[] {
							"$$($$_.Directory\\$$_.Name)", true,
							"$$_.LastWriteTime", true,
							"[long]$$_.Length", true,
					});
					break;
				case NameDescBase:
					sortExp = String.format(sortExpFormat, new Object[] {
							"$$($$_.Directory\\$$_.Name)", false,
							"$$_.LastWriteTime", true,
							"[long]$$_.Length", true,
					});
					break;
				default:
					sortExp = PS_SORTEXP_MODBASE;
				}
			} else {
				String sortExpFormat = " -k%d,%d%s -k%d,%d%s -k%d,%d%s -k%d,%d%s ";
				switch( bySortBase ) {
				case ModAscBase:
					sortExp = String.format(sortExpFormat, new Object[] {
							( LsHeader.ModTime1.colidx() + 1 ), ( LsHeader.ModTime1.colidx() + 1 ), "V",
							( LsHeader.ModTime2.colidx() + 1 ), ( LsHeader.ModTime2.colidx() + 1 ), "V",
							( LsHeader.ModTime3.colidx() + 1 ), ( LsHeader.ModTime3.colidx() + 1 ), "V",
							( LsHeader.Name.colidx() + 1 ), ( LsHeader.Name.colidx() + 1 ), "d",
							( LsHeader.Size.colidx() + 1 ), ( LsHeader.Size.colidx() + 1 ), "V",
					});
					break;
				case ModDescBase:
					sortExp = String.format(sortExpFormat, new Object[] {
							( LsHeader.ModTime1.colidx() + 1 ), ( LsHeader.ModTime1.colidx() + 1 ), "Vr",
							( LsHeader.ModTime2.colidx() + 1 ), ( LsHeader.ModTime2.colidx() + 1 ), "Vr",
							( LsHeader.ModTime3.colidx() + 1 ), ( LsHeader.ModTime3.colidx() + 1 ), "Vr",
							( LsHeader.Name.colidx() + 1 ), ( LsHeader.Name.colidx() + 1 ), "d",
							( LsHeader.Size.colidx() + 1 ), ( LsHeader.Size.colidx() + 1 ), "V",
					});
					break;
				case NameAscBase:
					sortExp = String.format(sortExpFormat, new Object[] {
							( LsHeader.Name.colidx() + 1 ), ( LsHeader.Name.colidx() + 1 ), "d",
							( LsHeader.ModTime1.colidx() + 1 ), ( LsHeader.ModTime1.colidx() + 1 ), "V",
							( LsHeader.ModTime2.colidx() + 1 ), ( LsHeader.ModTime2.colidx() + 1 ), "V",
							( LsHeader.ModTime3.colidx() + 1 ), ( LsHeader.ModTime3.colidx() + 1 ), "V",
							( LsHeader.Size.colidx() + 1 ), ( LsHeader.Size.colidx() + 1 ), "V",
					});
					break;
				case NameDescBase:
					sortExp = String.format(sortExpFormat, new Object[] {
							( LsHeader.Name.colidx() + 1 ), ( LsHeader.Name.colidx() + 1 ), "dr",
							( LsHeader.ModTime1.colidx() + 1 ), ( LsHeader.ModTime1.colidx() + 1 ), "V",
							( LsHeader.ModTime2.colidx() + 1 ), ( LsHeader.ModTime2.colidx() + 1 ), "V",
							( LsHeader.ModTime3.colidx() + 1 ), ( LsHeader.ModTime3.colidx() + 1 ), "V",
							( LsHeader.Size.colidx() + 1 ), ( LsHeader.Size.colidx() + 1 ), "V",
					});
					break;
				default:
					sortExp = SH_SORTEXP_MODBASE;
				}
			}

			return sortExp;
		}

	}
}
