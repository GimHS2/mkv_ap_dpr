/*
 *	File Name:	FileCommentLog.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/12/31		2.2.0	create
 *
**/

package com.irt.util.cmtlog;

import java.util.ArrayList;
import java.util.List;

/**
 * List of "Content" for File Comment
 */
class FileCommentLog {
	public static FileCommentLog parseJavaFileCommentLog( String line ) {
		String modifiedBy = FileComment.parseLineOrThrow(line, FileComment.PATTERN_JAVAFILE_COMMENT_LOG_LINE, 1);
		String date = FileComment.parseLineOrThrow(line, FileComment.PATTERN_JAVAFILE_COMMENT_LOG_LINE, 2);
		String version = FileComment.parseLineOrThrow(line, FileComment.PATTERN_JAVAFILE_COMMENT_LOG_LINE, 3);
		String content = FileComment.parseLineOrThrow(line, FileComment.PATTERN_JAVAFILE_COMMENT_LOG_LINE, 4);
		FileCommentLog cmtLog = new FileCommentLog();
		cmtLog.setModifiedBy(modifiedBy);
		cmtLog.setDate(date);
		cmtLog.setVersion(version);
		cmtLog.addContent(content);
		return cmtLog;
	}

	public static FileCommentLog parseSqlFileCommentLog( String line ) {
		String modifiedBy = FileComment.parseLineOrThrow(line, FileComment.PATTERN_SQLFILE_COMMENT_LOG_LINE, 1);
		String date = FileComment.parseLineOrThrow(line, FileComment.PATTERN_SQLFILE_COMMENT_LOG_LINE, 2);
		String content = FileComment.parseLineOrThrow(line, FileComment.PATTERN_SQLFILE_COMMENT_LOG_LINE, 4);
		String version = FileComment.parseLineOrThrow(line, FileComment.PATTERN_SQLFILE_COMMENT_LOG_LINE, 3);

		if( version == null ) {
			if( modifiedBy != null && date != null && content != null ) {
				version = FileComment.DEFAULT_NULLVERSION_NUMBER_STRING;
			}
		}

		FileCommentLog cmtLog = new FileCommentLog();
		cmtLog.setModifiedBy(modifiedBy);
		cmtLog.setDate(date);
		cmtLog.setVersion(version);
		cmtLog.addContent(content);
		return cmtLog;
	}

	private List<String> content;

	private String date;

	private String modifiedBy;

	private String version;

	public FileCommentLog() {
	}

	public FileCommentLog addContent( String content ) {
		getContent().add(content);
		return this;
	}

	public List<String> getContent() {
		if( content == null )
			content = new ArrayList<String>();

		return content;
	}

	public String getDate() {
		return date;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}
	public String getVersion() {
		return version;
	}
	public void setDate( String date ) {
		this.date = date;
	}
	public void setModifiedBy( String modifiedBy ) {
		this.modifiedBy = modifiedBy;
	}

	public void setVersion( String version ) {
		this.version = version;
	}

	public String toString() {
		String s = "{"
				+ " modified : " + modifiedBy
				+ " date : " + date
				+ " version : " + version
				+ " content : ";

		for( String line : content ) {
			s += "\t";
			s += line;
			s += "\n";
		}
		s += " }";
		return s;
	}

	// public static String parseFileCommentLogContinueContent( String line ) {
	// return FileComment.parseLineOrThrow(line, PATTERN_JAVAFILE_COMMENT_LOG_CONTINUE_CONTENT, 1);
	// }

}
