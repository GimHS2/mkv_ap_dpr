/*
 *	File Name:	Schema.java
 *	Version:	2.2.3(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2017/09/30		2.2.3	RBM_UPLOADLOG: FILE_TYPE XLX,XLF
 *	lsinji		2008/09/26		2.2.2	RBM_BOARD, RBM_BOARDCLASS: boardClassCode 코드 길이 변경 VARCHAR2(15)
 *	stghr12		2008/03/31		2.2.1	RBM_BOARD: noticeStartDate, noticeEndDate, extraValue, boardOption1 ~ boardOption9 추가
 *										RBM_BOARD: readedByUser QueryableFieldImplBK 사용
 *										RBM_DELETED_BFILE: "UPGDATE = SYSDATE" 삭제
 *										RBM_UPLOADLOG, RBM_UPLOADLOG_DETAIL 추가
 *	stghr12		2007/11/30		2.2.0	DATE, DATETIME 구분
 *										RBM_SCHEDULE 변경사항 적용: TIMEZONE, MNGUSERID 추가
 *	stghr12		2007/10/31		2.1.2	RBM_SCHEDULE 오류 수정
 *	stghr12		2007/04/30		2.1.1	field size 조절
 *										descriptionKey 변경: RBM_MNGKEY_MNGKEY -> RBM_MNGKEY_MANAGEKEY
 *	stghr12		2006/12/01		2.1.0	checkMessageKey() 삭제
 *	stghr12		2006/11/30		2.0.1	Field("readedByUser") 오류 수정
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.rbm.rbm;

import com.irt.sql.*;

/**
 *
 */
public class Schema extends com.irt.sql.Schema {
	public final static String RBM_MNG_KEY				= "ManageKey";
	public final static String RBM_SCHEDULE				= "Schedule";
	public final static String RBM_SCHEDULE_PTN			= "SchedulePattern";
	public final static String RBM_DELETED_BFILE		= "DeletedBFILE";
	public final static String RBM_BOARD				= "Board";
	public final static String RBM_BOARD_CLASS			= "BoardClass";
	public final static String RBM_UPLOADLOG			= "UploadLog";
	public final static String RBM_UPLOADLOG_DETAIL		= "UploadLogDetail";

	private final static Schema schema = new Schema();

	Schema() {
		Table table;
		Table.Field[] tfields;

		/***************************************************************************************************
		 *	RBM_MNG_KEY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "manageKey",			"MNGKEY",				"RBM_MNGKEY_MANAGEKEY",			7, 7 )
			, createFD( MD, "extraValue",			"EXTRAVALUE",			"RBM_MNGKEY_EXTRAVALUE",		0, 128 )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
		};
		putTable( RBM_MNG_KEY, table = createTable("RBM_MNG_KEY", "MNG", tfields) );


		/***************************************************************************************************
		 *	RBM_SCHEDULE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"SCH_CD",				"RBM_SCHEDULE_CODE",			10, 10 )
					.setInsertQuery( "NVL( ?, 'S' || seqRBM_SCHEDULE.NEXTVAL )" )
			, createFD( MD, "name",					"SCH_NAME",				"RBM_SCHEDULE_NAME",			DESC, 0, 128 )
			, createFD( OP, "description",			"SCH_DESC",				"DESCRIPTION",					DESC, 0, 500 )
			, createFD( MR, "scheduleClass",		"SCH_CLASS",			"RBM_SCHEDULE_CLASS",			"RBM_SCHEDULE_CLASS_", "PUB,SFC,OFC,ORD" )
			, createFD( MR, "oncePerDay",			"ONCEADAY_IND",			"RBM_SCHEDULE_ONCEPERDAY",		"PUB_WHETHER_", "Y,N" )
			, createFD( OR, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( OP, "timeZone",				"TIMEZONE",				"TIMEZONE",						0, 30 )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"RBM_SCHEDULE_EXTRAVALUE",		0, 128 )
			, createFD( RD, "nextScheduleDateTime",	"NEXTSCHDATETIME",		"RBM_SCHEDULE_NEXTSCHEDULEDATETIME", DATETIME )
			, createFD( OP, "manageUserId",			"MNGUSERID",			"MANAGEUSER",					0, 30 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( RBM_SCHEDULE, table = createTable("RBM_SCHEDULE", "SCH", tfields, "UPGDATE = SYSDATE") );
		putQueryable( RBM_SCHEDULE, new QueryableImpl(table, new QueryableField[] {
			  new QueryableFieldImpl( DESC, "timeZoneName", "TMZ.TIMEZONE_NAME", "TIMEZONE"
					, new JoinableImpl( "TMZ", "SYS_TIMEZONE", "TMZ.TIMEZONE_CD(+) = SCH.TIMEZONE" ) )
		}) );


		/***************************************************************************************************
		 *	RBM_SCHEDULE_PTN
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"SCHCD",				"RBM_SCHEDULE_CODE",			10, 10 )
			, createFD( PM, "patternType",			"PTN_TYPE",				"SCHEDULE_PATTERN_TYPE",		CODE )
					.setValidValueList( "PUB_SCHEDULEPTN_TYPE_", "0,1,2,3,4,5,6,7" )
			, createFD( PM, "patternIndex",			"PTN_INDEX",			"SCHEDULE_PATTERN_INDEX",		INTEGER, 0, 99 )
			, createFD( PM, "patternDate",			"PTN_DATE",				"SCHEDULE_PATTERN_DATE",		DATE )
			, createFD( PM, "time",					"TIME",					"SCHEDULE_PATTERN_TIME",		0, 5 )
			, createFD( OP, "priority",				"PRIORITY",				"RBM_SCHEDULEPTN_PRIORITY",		INTEGER, 0, 99 )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"RBM_SCHEDULEPTN_EXTRAVALUE",	0, 128 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( RBM_SCHEDULE_PTN, table = createTable("RBM_SCHEDULE_PTN", "SCHP", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	RBM_DELETED_BFILE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "directoryName",		"DIRECTORYNAME",		"RBM_DELETEDBFILE_DIRNAME",		STRING )
			, createFD( RD, "fileName",				"FILENAME",				"RBM_DELETEDBFILE_FILENAME",	STRING )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
		};
		putTable( RBM_DELETED_BFILE, table = createTable("RBM_DELETED_BFILE", "DBF", tfields) );


		/***************************************************************************************************
		 *	RBM_BOARD_CLASS
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "boardClassCode",		"BOARDCLASS_CD",		"RBM_BOARD_BOARDCLASSCODE",		0, 15 )
			, createFD( MD, "boardClassName",		"BOARDCLASS_NAME",		"RBM_BOARD_BOARDCLASSNAME",		0, 128 )
			, createFD( RD, "lastBoardNumber",		"LAST_SEQID",			"RBM_BOARD_LASTBOARDNUMBER",	INTEGER )
			, createFD( RD, "recordCount",			"RECORDCOUNT",			"RBM_BOARD_RECORDCOUNT",		INTEGER )
			, createFD( MD, "useReadedUser",		"USE_READEDUSER",		"RBM_BOARD_USEREADEDUSER",		"RBM_BOARD_USEREADEDUSER_", "Y,N" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( RBM_BOARD_CLASS, table = createTable("RBM_BOARDCLASS", "BCL", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	RBM_BOARD
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "boardClassCode",		"BOARDCLASSCD",			"RBM_BOARD_BOARDCLASSCODE",		0, 15 )
			, createFD( PM, "boardNumber",			"SEQID",				"RBM_BOARD_BOARDNUMBER",		INTEGER )
			, createFD( MD, "boardType",			"BOARD_TYPE",			"RBM_BOARD_BOARDTYPE",			"RBM_BOARD_TYPE_", "C,N,R" )
			, createFD( OP, "boardOption",			"BOARD_OPTION",			"RBM_BOARD_BOARDOPTION",		0, 10 )
			, createFD( MD, "title",				"TITLE",				"RBM_BOARD_TITLE",				0, 640 )
			, createFD( OP, "registUserId",			"REGUSERID",			"RBM_BOARD_REGISTUSERID",		0, 30 )
			, createFD( OP, "originalBoardNumber",	"ORIGINAL_SEQID",		"RBM_BOARD_ORIGINALBOARDNUMBER",	INTEGER )
			, createFD( MD, "boardGroupNumber",		"BOARDGRP_SEQID",		"RBM_BOARD_BOARDGROUPNUMBER",		INTEGER )
			, createFD( OP, "boardGroupDisplaySeq",	"BOARDGRP_DISPLAYSEQ",	"RBM_BOARD_BOARDGROUPDISPLAYSEQ",	INTEGER )
			, createFD( OP, "boardGroupLevel",		"BOARDGRP_LEVEL",		"RBM_BOARD_BOARDGROUPLEVEL",	INTEGER )
			, createFD( OP, "noticeStartDateTime",	"NOTICE_STARTDATE",		"RBM_BOARD_NOTICESTARTDATE",	DATETIME )
			, createFD( OP, "noticeEndDateTime",	"NOTICE_ENDDATE",		"RBM_BOARD_NOTICEENDDATE",		DATETIME )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"RBM_BOARD_EXTRAVALUE",			0, 128 )
			, createFD( RD, "viewCount",			"VIEWCOUNT",			"RBM_BOARD_VIEWCOUNT",			INTEGER )
			, createFD( MD, "content",				"CONTENT",				"RBM_BOARD_CONTENT",			DESC )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( RBM_BOARD, table = createTable("RBM_BOARD", "BRD", tfields, "UPGDATE = SYSDATE") );
		putQueryable( RBM_BOARD, new QueryableImpl(table) {
			{
				Joinable tbl_BRU = new JoinableImplBK( "BRU", "RBM_BOARD_READUSER"
						, "BRU.BOARDCLASSCD(+) = BRD.BOARDCLASSCD AND BRU.SEQID(+) = BRD.SEQID AND BRU.USERID(+) = ?"
						, new String[] { "userId" } );

				append( new QueryableField[] {
					  new QueryableFieldImpl( DATE, "noticeStartDate", "NOTICE_STARTDATE", "RBM_BOARD_NOTICESTARTDATE" )
					, new QueryableFieldImpl( DATE, "noticeEndDate", "NOTICE_ENDDATE", "RBM_BOARD_NOTICEENDDATE" )
					, new QueryableFieldImpl( CODE, "boardOption1", "SUBSTRB(BOARD_OPTION, 1, 1)", "RBM_BOARD_BOARDOPTION1" )
					, new QueryableFieldImpl( CODE, "boardOption2", "SUBSTRB(BOARD_OPTION, 2, 1)", "RBM_BOARD_BOARDOPTION2" )
					, new QueryableFieldImpl( CODE, "boardOption3", "SUBSTRB(BOARD_OPTION, 3, 1)", "RBM_BOARD_BOARDOPTION3" )
					, new QueryableFieldImpl( CODE, "boardOption4", "SUBSTRB(BOARD_OPTION, 4, 1)", "RBM_BOARD_BOARDOPTION4" )
					, new QueryableFieldImpl( CODE, "boardOption5", "SUBSTRB(BOARD_OPTION, 5, 1)", "RBM_BOARD_BOARDOPTION5" )
					, new QueryableFieldImpl( CODE, "boardOption6", "SUBSTRB(BOARD_OPTION, 6, 1)", "RBM_BOARD_BOARDOPTION6" )
					, new QueryableFieldImpl( CODE, "boardOption7", "SUBSTRB(BOARD_OPTION, 7, 1)", "RBM_BOARD_BOARDOPTION7" )
					, new QueryableFieldImpl( CODE, "boardOption8", "SUBSTRB(BOARD_OPTION, 8, 1)", "RBM_BOARD_BOARDOPTION8" )
					, new QueryableFieldImpl( CODE, "boardOption9", "SUBSTRB(BOARD_OPTION, 9, 1)", "RBM_BOARD_BOARDOPTION9" )
					, new QueryableFieldImpl( CODE, "boardOption9", "SUBSTRB(BOARD_OPTION, 9, 1)", "RBM_BOARD_BOARDOPTION9" )
					, new QueryableFieldImpl( CODE, "boardType2", "'S'" )
					, new QueryableFieldImpl( DESC, "boardClassName", "BCL.BOARDCLASS_NAME", "RBM_BOARD_BOARDCLASSNAME"
							, new JoinableImpl( "BCL", "RBM_BOARDCLASS", "BCL.BOARDCLASS_CD(+) = BRD.BOARDCLASSCD" ) )
					, new QueryableFieldImpl( DESC, "registUserName", "USR.USERNAME", "RBM_BOARD_REGISTUSERNAME"
							, new JoinableImpl( "USR", "vwUSR_USER", "USR.UNIQID(+) = BRD.REGUSERID" ) )
					, new QueryableFieldImplBK( CODE, "readedByUser", "DECODE( ?, BRD.REGUSERID, 'Y', BRU.USERID, 'Y', 'N' )"
							, new String[] { "userId" }, "RBM_BOARD_READEDBYUSER", tbl_BRU )
				} );
			}

			@Override
			public boolean appendCondition( ConditionQueryBuffer querybuf ) {
				boolean hasCondition = super.appendCondition( querybuf );

				Object noticeDate = querybuf.getConditionValue( "noticeDate", DATE );
				if( noticeDate != null ) {
					hasCondition = true;
					querybuf.appendCondition( "(BRD.NOTICE_STARTDATE IS NULL OR BRD.NOTICE_STARTDATE <= ?)", noticeDate );
					querybuf.appendCondition( "(BRD.NOTICE_ENDDATE IS NULL OR BRD.NOTICE_ENDDATE >= ?)", noticeDate );
				}

				return hasCondition;
			}
		} );


		/***************************************************************************************************
		 *	RBM_UPLOADLOG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "logId",				"LOG_ID",				"RBM_UPLOADLOG_LOGID",			13, 13 )
			, createFD( MR, "systemCode",			"SYSTEMCD",				"RBM_UPLOADLOG_SYSTEM",			0, 30 )
			, createFD( MR, "uploadType",			"UPLOAD_TYPE",			"RBM_UPLOADLOG_UPLOADTYPE",		0, 10 )
			, createFD( OR, "uploadFileName",		"UPLOAD_FILENAME",		"RBM_UPLOADLOG_UPLOADFILENAME",	0, 128 )
			, createFD( OR, "fileName",				"FILE_NAME",			"RBM_UPLOADLOG_FILENAME",		0, 128 )
			, createFD( OR, "fileType",				"FILE_TYPE",			"RBM_UPLOADLOG_FILETYPE",		"PUB_FILEFORMAT_", "CSV,XLS,FLV,TAB,XLX,XLF" )
			, createFD( OR, "encoding",				"ENCODING",				"RBM_UPLOADLOG_ENCODING",		0, 10 )
			, createFD( OR, "userId",				"USERID",				"RBM_UPLOADLOG_USER",			0, 30 )
			, createFD( OP, "lineCount",			"ALLCNT",				"RBM_UPLOADLOG_LINECOUNT",		INTEGER )
			, createFD( OP, "successCount",			"EXECNT",				"RBM_UPLOADLOG_SUCCESSCOUNT",	INTEGER )
			, createFD( OP, "warningCount",			"WRNCNT",				"RBM_UPLOADLOG_WARNINGCOUNT",	INTEGER )
			, createFD( OP, "errorCount",			"ERRCNT",				"RBM_UPLOADLOG_ERRORCOUNT",		INTEGER )
			, createFD( OP, "message",				"MESSAGE",				"RBM_UPLOADLOG_MESSAGE",		0, 500 )
			, createFD( MD, "status",				"STATUS",				"RBM_UPLOADLOG_STATUS",			"RBM_UPLOADLOG_STATUS_", "CP,CL,ER,RU" )
			, createFD( MR, "startDateTime",		"START_DATETIME",		"RBM_UPLOADLOG_STARTDATETIME",	DATETIME )
			, createFD( OP, "endDateTime",			"END_DATETIME",			"RBM_UPLOADLOG_ENDDATETIME",	DATETIME )
		};
		putTable( RBM_UPLOADLOG, table = createTable("RBM_UPLOADLOG", "LOG", tfields) );
		putQueryable( RBM_UPLOADLOG, new QueryableImpl(table, new QueryableField[] {
			  new QueryableFieldImpl( DESC, "userName", "USR.USER_NAME", "RBM_UPLOADLOG_USER"
					, new JoinableImpl( "USR", "USR_USER", "USR.UNIQID(+) = LOG.USERID" ) )
			, new QueryableFieldImpl( STRING, "partyId", "USR.PARTYID"
					, new JoinableImpl( "USR", "USR_USER", "USR.UNIQID(+) = LOG.USERID" ) )
			, new QueryableFieldImpl( DESC, "uploadTypeName", "PKG.PKG_NAME"
					, new JoinableImpl( "PKG", "SYS_SYSTEM_PKG"
							, "PKG.SYSTEMCD(+) = 'RBM' AND PKG.PKG_CD(+) = 'RBMUploadLog.TYPE.' || LOG.UPLOAD_TYPE" ) )
		}) );


		/***************************************************************************************************
		 *	RBM_UPLOADLOG_DETAIL
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( MD, "logId",				"LOGID",				"RBM_UPLOADLOG_LOGID",			13, 13 )
			, createFD( OP, "lineNumber",			"LINE_NUM",				"RBM_UPLOADLOG_LINENUMBER",		INTEGER )
			, createFD( OP, "lineName",				"LINE_NAME",			"RBM_UPLOADLOG_LINENAME",		0, 500 )
			, createFD( OP, "executeType",			"EXECUTE_TYPE",			"RBM_UPLOADLOG_EXECUTETYPE",	1, 1 )
			, createFD( OP, "message",				"MESSAGE",				"RBM_UPLOADLOG_LINEMESSAGE",	0, 500 )
			, createFD( MD, "status",				"STATUS",				"RBM_UPLOADLOG_LINESTATUS",		"RBM_UPLOADLOG_LINESTATUS_", "CP,WN,ER" )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
		};
		putTable( RBM_UPLOADLOG_DETAIL, table = createTable("RBM_UPLOADLOG_DTL", "LOGD", tfields) );
	}

	public static Queryable findQueryable( String key ) {
		return schema.getQueryable( key );
	}

	public static QueryFactory findQueryFactory( String key ) {
		Queryable queryable = schema.getQueryable( key );
		return ( queryable == null ? null : new QueryFactory(queryable) );
	}

	public static Table findTable( String key ) {
		return schema.getTable( key );
	}
}
