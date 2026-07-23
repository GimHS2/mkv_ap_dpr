/*
 *	File Name:	Schema.java
 *	Version:	2.2.2c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	hankalam	2021/11/30		2.2.2c	ICS_BOARD: truncateContent 항목 추가
 *	hankalam	2017/02/28		2.2.2c	ICS_HELP_BOARD 추가
 *	stghr12		2010/08/31		2.2.2	ICS_BOARD_ATTACH: CONTENT_TYPE VARCHAR2(30) -> VARCHAR2(100) 적용
 *	stghr12		2010/07/31		2.2.1	version up
 *	lsinji		2009/10/25		2.2.0	create
 *
**/

package com.irt.ics;

import com.irt.sql.*;

/**
 *
 */
public class Schema extends com.irt.sql.Schema {
	public final static String ICS_BOARDCLASS			= "BoardClass";
	public final static String ICS_BOARD				= "Board";
	public final static String ICS_BOARD_ATTACH			= "BoardAttach";
	public final static String ICS_BOARD_DOWNUSER		= "BoardDownuser";
	public final static String ICS_BOARD_HEADWORD		= "BoardHeadword";
	public final static String ICS_BOARD_READUSER		= "BoardReaduser";
	public final static String ICS_BOARD_COMMENT		= "BoardComment";
	public final static String ICS_HELP_BOARD			= "HelpBoard";

	private final static Schema schema = new Schema();

	Schema() {
		Table table;
		Table.Field[] tfields;

		/***************************************************************************************************
		 *	ICS_BOARDCLASS
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "boardClassCode",		"BOARDCLASS_CD",		"ICS_BOARD_BOARDCLASSCODE",		0, 10 )
			, createFD( MD, "boardClassName",		"BOARDCLASS_NAME",		"ICS_BOARD_BOARDCLASSNAME",		0, 128 )
			, createFD( RD, "lastBoardNumber",		"LAST_SEQID",			"ICS_BOARD_BOARDNUMBER_LAST",	INTEGER )
			, createFD( RD, "recordCount",			"RECORDCOUNT",			"ICS_BOARD_RECORDCOUNT",		INTEGER )
			, createFD( MD, "useReadedUser",		"USE_READEDUSER",		"ICS_BOARD_USEREADEDUSER",		"ICS_BOARD_USEREADEDUSER_", "Y,N" )
			, createFD( MD, "boardOption",			"BOARD_OPTION",			"ICS_BOARD_BOARDOPTION",		0,4)
			, createFD( MD, "extraValue",			"EXTRAVALUE",			"ICS_BOARD_EXTRAVALUE",			0,25)
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ICS_BOARDCLASS, table = createTable("ICS_BOARDCLASS", "BCL", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ICS_BOARD_HEADWORD
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "headwordCode",			"HEADWORD_CD",			"ICS_BOARD_HEADWORDCODE",		0, 15 )
			, createFD( MD, "boardClassCode",		"BOARDCLASSCD",			"ICS_BOARD_BOARDCLASS",			0, 10 )
			, createFD( MD, "headwordName",			"HEADWORD_DESC",		"ICS_BOARD_HEADWORDNAME",		0, 30 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ICS_BOARD_HEADWORD, table = createTable("ICS_BOARD_HEADWORD", "BHW", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ICS_BOARD_HEADWORD, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( STRING, "boardClassName"
					, "(SELECT BOARDCLASS_NAME FROM ICS_BOARDCLASS BCL WHERE BCL.BOARDCLASS_CD = BHW.BOARDCLASSCD)" )
		}) );


		/***************************************************************************************************
		 *	ICS_BOARD
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "boardClassCode",		"BOARDCLASSCD",			"ICS_BOARD_BOARDCLASS",			0, 10 )
			, createFD( PM, "boardNumber",			"SEQID",				"ICS_BOARD_BOARDNUMBER",		INTEGER )
			, createFD( MD, "boardType",			"BOARD_TYPE",			"ICS_BOARD_BOARDTYPE",			"ICS_BOARD_TYPE_", "C,N,R" )
			, createFD( OP, "boardOption",			"BOARD_OPTION",			"ICS_BOARD_BOARDOPTION",		0, 10 )
			, createFD( OP, "headwordCode",			"HEADWORDCD",			"ICS_BOARD_HEADWORD",			0, 15 )
			, createFD( MD, "title",				"TITLE",				"ICS_BOARD_TITLE",				0, 128 )
			, createFD( OP, "registUserId",			"REGUSERID",			"ICS_BOARD_REGISTUSERID",		0, 30 )
			, createFD( OP, "originalBoardNumber",	"ORIGINAL_SEQID",		"ICS_BOARD_BOARDNUMBER_ORIGINAL",	INTEGER )
			, createFD( MD, "boardGroupNumber",		"BOARDGRP_SEQID",		"ICS_BOARD_BOARDGROUPNUMBER",		INTEGER )
			, createFD( OP, "boardGroupDisplaySeq",	"BOARDGRP_DISPLAYSEQ",	"ICS_BOARD_BOARDGROUPDISPLAYSEQ",	INTEGER )
			, createFD( OP, "boardGroupLevel",		"BOARDGRP_LEVEL",		"ICS_BOARD_BOARDGROUPLEVEL",	INTEGER )
			, createFD( OP, "noticeStartDate",		"NOTICE_STARTDATE",		"ICS_BOARD_NOTICESTARTDATE",	DATE )
			, createFD( OP, "noticeEndDate",		"NOTICE_ENDDATE",		"ICS_BOARD_NOTICEENDDATE",		DATE )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"ICS_BOARD_EXTRAVALUE",			1,128 )
			, createFD( RD, "viewCount",			"VIEWCOUNT",			"ICS_BOARD_VIEWCOUNT",			INTEGER )
			, createFD( MD, "content",				"CONTENT",				"ICS_BOARD_CONTENT",			DESC )
			, createFD( OP, "attachManageKey",		"ATTACH_MNGKEY",		"ICS_BOARD_ATTACHMANAGEKEY",	1,50 )
			, createFD( OP, "lastCommentNember",	"LAST_COMMSEQID",		"ICS_BOARD_COMMENT_NUMBER_LAST",	INTEGER )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ICS_BOARD, table = createTable("ICS_BOARD", "BRD", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ICS_BOARD, new QueryableImpl(table) {{
			Joinable tbl_URR = new JoinableImpl( "URR", "vwUSR_USER", "URR.UNIQID(+) = BRD.REGUSERID" );

			append( new QueryableField[] {
				  new QueryableFieldImpl( INTEGER, "readedUserCount"
						, "(SELECT COUNT(*) FROM ICS_BOARD_READUSER SB WHERE SB.BOARDCLASSCD = BRD.BOARDCLASSCD AND SB.SEQID = BRD.SEQID)"
						, "ICS_BOARD_READEDUSERCOUNT" )
				, new QueryableFieldImpl( STRING, "registUserName", "URR.USERNAME", "ICS_BOARD_REGISTUSERNAME", tbl_URR )
				, new QueryableFieldImpl( STRING, "registUserUserId", "URR.USERID", "ICS_BOARD_REGISTUSERID", tbl_URR )
				, new QueryableFieldImpl( STRING, "registUserPartyName", "URR.PARTYNAME", "ICS_BOARD_REGISTUSERPARTYNAME", tbl_URR )
				, new QueryableFieldImpl( STRING, "email", "(SELECT EMAIL FROM USR_USER WHERE UNIQID = BRD.REGUSERID)" )
				, new Schema.Field( "readedByUser", "ICS_BOARD_READEDBYUSER" )
				, new QueryableFieldImpl( STRING, "attachedFileInd"
						, "(SELECT DECODE(COUNT(*), 0, 'N', 'Y') FROM ICS_BOARD_ATTACH SB"
							+" WHERE SB.BOARDCLASSCD = BRD.BOARDCLASSCD AND SB.SEQID = BRD.SEQID AND SB.FILE_TYPE = 'FLE' AND SB.STATUS = '00')" )
				, new QueryableFieldImpl( STRING, "registeredImageFileInd"
						, "(SELECT DECODE(COUNT(*), 0, 'N', 'Y') FROM ICS_BOARD_ATTACH SB"
							+" WHERE SB.BOARDCLASSCD = BRD.BOARDCLASSCD AND SB.SEQID = BRD.SEQID AND SB.FILE_TYPE = 'IMG' AND SB.STATUS = '00')" )
				, new QueryableFieldImpl( INTEGER, "replyCount"
						, "(SELECT COUNT(*) FROM ICS_BOARD SB"
							+" WHERE SB.BOARDCLASSCD = BRD.BOARDCLASSCD AND SB.BOARDGRP_SEQID = BRD.SEQID AND SB.BOARDGRP_SEQID <> BRD.SEQID)" )
				, new QueryableFieldImpl( STRING, "commentCount"
						, "(SELECT COUNT(*) FROM ICS_BOARD_COMMENT SB"
							+" WHERE SB.BOARDCLASSCD = BRD.BOARDCLASSCD AND SB.SEQID = BRD.SEQID AND STATUS = '00')" )
				, new QueryableFieldImpl( INTEGER, "headwordSeq"
						, "(SELECT NVL(SUBSTR(HEADWORD_SEQ, -1), 0) FROM (SELECT ROWNUM HEADWORD_SEQ, HEADWORD_CD, HEADWORD_DESC FROM ICS_BOARD_HEADWORD SB2 WHERE SB2.BOARDCLASSCD = BRD.BOARDCLASSCD) SB"
							+" WHERE SB.HEADWORD_CD = BRD.HEADWORDCD)" )
				, new QueryableFieldImpl( STRING, "headwordName"
						, "(SELECT HEADWORD_DESC FROM ICS_BOARD_HEADWORD SB"
							+" WHERE SB.BOARDCLASSCD = BRD.BOARDCLASSCD AND SB.HEADWORD_CD = BRD.HEADWORDCD)" )
				, new QueryableFieldImpl( STRING, "globalHeadwordName"
						, "(SELECT HEADWORD_DESC FROM ICS_BOARD_HEADWORD SB WHERE SB.HEADWORD_CD = BRD.HEADWORDCD)" )
				, new QueryableFieldImplBK( CODE, "isNew"
						, "(CASE WHEN SYSDATE - BRD.REGDATE <= ? THEN 'Y' ELSE 'N' END)", new String[] { "newDays" } )
				, new QueryableFieldImpl( STRING, "truncateContent" , "DBMS_LOB.SUBSTR( CONTENT, 200, 1 )" )
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
		 *	ICS_BOARD_ATTACH
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "attachManageKey",		"ATTACH_MNGKEY",		"ICS_BOARD_ATTACHMANAGEKEY",	1,50 )
			, createFD( PM, "attachNumber",			"ATTACH_SEQID",			"ICS_BOARD_ATTACH_NUMBER",		INTEGER )
			, createFD( OP, "boardClassCode",		"BOARDCLASSCD",			"ICS_BOARD_BOARDCLASS",			0, 10 )
			, createFD( OP, "boardNumber",			"SEQID",				"ICS_BOARD_BOARDNUMBER",		INTEGER )
			, createFD( OP, "contentType",			"CONTENT_TYPE",			"ICS_BOARD_ATTACH_CONTENTTYPE",	0, 100 )
			, createFD( MR, "fileType",				"FILE_TYPE",			"ICS_BOARD_ATTACH_FILETYPE",	"ICS_BOARD_ATTACHTYPE_", "FLE,IMG" )
			, createFD( MR, "fileName",				"FILE_NAME",			"ICS_BOARD_ATTACH_FILENAME",	0, 128 )
			, createFD( OR, "filePath",				"FILE_PATH",			"ICS_BOARD_ATTACH_FILEPATH",	0, 128 )
			, createFD( MR, "fileSize",				"FILE_SIZE",			"ICS_BOARD_ATTACH_FILESIZE",	DOUBLE )
			, createFD( MR, "serverFileName",		"SERVER_FILENAME",		"ICS_BOARD_ATTACH_FILENAME_SERVER",	0, 50 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						2, 2 )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATE )
		};
		putTable( ICS_BOARD_ATTACH, table = createTable("ICS_BOARD_ATTACH", "BDA", tfields) );


		/***************************************************************************************************
		 *	ICS_BOARD_DOWNUSER
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "attachManagerKey",		"ATTACH_MNGKEY",		"ICS_BOARD_ATTACHMANAGEKEY",	1,50 )
			, createFD( PM, "attachNumber",			"ATTACH_SEQID",			"ICS_BOARD_ATTACH_NUMBER",		INTEGER )
			, createFD( PM, "userId",				"USERID",				"USERID",						0, 30 )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATE )
		};
		putTable( ICS_BOARD_DOWNUSER, table = createTable("ICS_BOARD_DOWNUSER", "BDU", tfields) );


		/***************************************************************************************************
		 *	ICS_BOARD_READUSER
		***************************************************************************************************/
		tfields = new Table.Field[] {
			   createFD( PM, "boardClassCode",		"BOARDCLASSCD",			"ICS_BOARD_BOARDCLASS",			0, 10 )
			 , createFD( PM, "boardNumber",			"SEQID",				"ICS_BOARD_BOARDNUMBER",		INTEGER )
			 , createFD( PM, "userId",				"USERID",				"USERID",						0, 30 )
			 , createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATE )
		};
		putTable( ICS_BOARD_READUSER, table = createTable("ICS_BOARD_READUSER", "BRU", tfields) );


		/***************************************************************************************************
		 *	ICS_BOARD_COMMENT
		***************************************************************************************************/
		tfields = new Table.Field[] {
			   createFD( PM, "boardClassCode",			"BOARDCLASSCD",			"ICS_BOARD_BOARDCLASS",					0, 10 )
			 , createFD( PM, "boardNumber",				"SEQID",				"ICS_BOARD_BOARDNUMBER",				INTEGER )
			 , createFD( PM, "commentNumber",			"COMMENT_SEQID",		"ICS_BOARD_COMMENT_NUMBER",				INTEGER )
			 , createFD( MD, "content",					"COMMENT_CONTENT",		"ICS_BOARD_COMMENT_CONTENT",			0, 1024 )
			 , createFD( OP, "originalCommentNumber",	"ORIGINAL_COMMSEQID",	"ICS_BOARD_COMMENT_NUMBER_ORIGINAL",	INTEGER )
			 , createFD( MD, "commentGroupNumber",		"COMMGROUP_COMMSEQID",	"ICS_BOARD_COMMENT_GROUPNUMBER",		INTEGER )
			 , createFD( OP, "commentGroupDisplaySeq",	"COMMGROUP_DISPLAYSEQ",	"ICS_BOARD_COMMENT_GROUPDISPLAYSEQ",	INTEGER )
			 , createFD( OP, "commentGroupLevel",		"COMMGROUP_LEVEL",		"ICS_BOARD_COMMENT_GROUPLEVEL",			INTEGER )
			 , createFD( MO, "registUserId",			"REGUSERID",			"ICS_BOARD_COMMENT_REGISTUSERID",		0, 30)
			 , createFD( RD, "status",					"STATUS",				"STATUS",								2, 2 )
			 , createFD( RD, "createDateTime",			"REGDATE",				"CREATEDATETIME",						DATE )
			 , createFD( RD, "updateDateTime",			"UPGDATE",				"UPDATEDATETIME",						DATE )
		};
		putTable( ICS_BOARD_COMMENT, table = createTable("ICS_BOARD_COMMENT", "BDC", tfields) );
		putQueryable( ICS_BOARD_COMMENT, new QueryableImpl(table) {{
			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "registUserName", "(SELECT USER_NAME FROM USR_USER WHERE UNIQID = BDC.REGUSERID)" )
				, new QueryableFieldImpl( STRING, "registUserUserId", "(SELECT USER_ID FROM USR_USER WHERE UNIQID = BDC.REGUSERID)" )
				, new QueryableFieldImpl( STRING, "commentReplyCount"
						, "(SELECT COUNT(*) FROM ICS_BOARD_COMMENT SB"
						+" WHERE SB.BOARDCLASSCD = BDC.BOARDCLASSCD AND SB.SEQID = BDC.SEQID AND SB.ORIGINAL_COMMSEQID = BDC.COMMENT_SEQID"
						+" AND SB.STATUS = '00')" )
			} );
		}} );


		/***************************************************************************************************
		 *	ICS_HELP_BOARD
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "boardClassCode",		"BOARDCLASSCD",			"ICS_BOARD_BOARDCLASS",			0, 10 )
			, createFD( PM, "boardNumber",			"SEQID",				"ICS_BOARD_BOARDNUMBER",			INTEGER )
			, createFD( MD, "boardType",			"BOARD_TYPE",			"ICS_BOARD_BOARDTYPE",				"ICS_BOARD_TYPE_", "C,N,R" )
			, createFD( OP, "boardOption",			"BOARD_OPTION",			"ICS_BOARD_BOARDOPTION",			0, 10 )
			, createFD( MD, "headwordCode",			"HEADWORDCD",			"ICS_BOARD_HEADWORD",				"ICS_HELP_BOARD_HEADWORD_", "SI,PS,OT" )
			, createFD( OP, "registUserId",			"REGUSERID",			"ICS_HELP_BOARD_REGISTUSERID",			0, 30 )
			, createFD( MD, "userName",				"USERNAME",				"ICS_HELP_BOARD_USERNAME",				0, 128 )
			, createFD( OP, "orderNumber",			"ORDER_NUMBER",			"ICS_HELP_BOARD_ORDERNUMBER",			0, 35 )
			, createFD( MD, "tel",					"TEL",					"ICS_HELP_BOARD_TEL",					0, 20 )
			, createFD( MD, "email",				"EMAIL",				"ICS_HELP_BOARD_EMAIL",					0, 128 )
			, createFD( OP, "originalBoardNumber",	"ORIGINAL_SEQID",		"ICS_BOARD_BOARDNUMBER_ORIGINAL",	INTEGER )
			, createFD( MD, "boardGroupNumber",		"BOARDGRP_SEQID",		"ICS_BOARD_BOARDGROUPNUMBER",		INTEGER )
			, createFD( OP, "boardGroupDisplaySeq",	"BOARDGRP_DISPLAYSEQ",	"ICS_BOARD_BOARDGROUPDISPLAYSEQ",	INTEGER )
			, createFD( OP, "boardGroupLevel",		"BOARDGRP_LEVEL",		"ICS_BOARD_BOARDGROUPLEVEL",		INTEGER )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"ICS_BOARD_EXTRAVALUE",			1,128 )
			, createFD( RD, "viewCount",			"VIEWCOUNT",			"ICS_BOARD_VIEWCOUNT",				INTEGER )
			, createFD( MD, "content",				"CONTENT",				"ICS_BOARD_CONTENT",				DESC )
			, createFD( OP, "attachManageKey",		"ATTACH_MNGKEY",		"ICS_BOARD_ATTACHMANAGEKEY",		1,50 )
			, createFD( OP, "lastCommentNember",	"LAST_COMMSEQID",		"ICS_BOARD_COMMENT_NUMBER_LAST",	INTEGER )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",						DATE )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",						DATE )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",							0, 30 )
			, createFD( MD, "completedInd",			"COMPLETED_IND",		"ICS_HELP_BOARD_COMPLETED_IND",			"ICS_HELP_BOARD_COMPLETED_IND_", "Y,N" )
		};
		putTable( ICS_HELP_BOARD, table = createTable("ICS_HELP_BOARD", "HBRD", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ICS_HELP_BOARD, new QueryableImpl(table) {{
			Joinable tbl_URR = new JoinableImpl( "URR", "vwUSR_USER", "URR.UNIQID(+) = HBRD.REGUSERID" );

			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "registUserUserId", "URR.USERID", "ICS_BOARD_REGISTUSERID", tbl_URR )
				, new QueryableFieldImpl( STRING, "registUserPartyName", "URR.PARTYNAME", "ICS_BOARD_REGISTUSERPARTYNAME", tbl_URR )
				, new QueryableFieldImpl( STRING, "attachedFileInd"
						, "(SELECT DECODE(COUNT(*), 0, 'N', 'Y') FROM ICS_BOARD_ATTACH SB"
							+" WHERE SB.BOARDCLASSCD = HBRD.BOARDCLASSCD AND SB.SEQID = HBRD.SEQID AND SB.FILE_TYPE = 'FLE' AND SB.STATUS = '00')" )
				, new QueryableFieldImpl( STRING, "registeredImageFileInd"
						, "(SELECT DECODE(COUNT(*), 0, 'N', 'Y') FROM ICS_BOARD_ATTACH SB"
							+" WHERE SB.BOARDCLASSCD = HBRD.BOARDCLASSCD AND SB.SEQID = HBRD.SEQID AND SB.FILE_TYPE = 'IMG' AND SB.STATUS = '00')" )
				, new QueryableFieldImpl( INTEGER, "replyCount"
						, "(SELECT COUNT(*) FROM ICS_BOARD SB"
							+" WHERE SB.BOARDCLASSCD = HBRD.BOARDCLASSCD AND SB.BOARDGRP_SEQID = HBRD.SEQID AND SB.BOARDGRP_SEQID <> HBRD.SEQID)" )
				, new QueryableFieldImpl( STRING, "headwordName"
						, "(SELECT HEADWORD_DESC FROM ICS_BOARD_HEADWORD SB"
							+" WHERE SB.BOARDCLASSCD = HBRD.BOARDCLASSCD AND SB.HEADWORD_CD = HBRD.HEADWORDCD)" )
				, new QueryableFieldImpl( STRING, "globalHeadwordName"
						, "(SELECT HEADWORD_DESC FROM ICS_BOARD_HEADWORD SB WHERE SB.HEADWORD_CD = HBRD.HEADWORDCD)" )
				, new QueryableFieldImplBK( CODE, "isNew"
						, "(CASE WHEN SYSDATE - HBRD.REGDATE <= ? THEN 'Y' ELSE 'N' END)", new String[] { "newDays" } )
			} );
		}} );

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

	/**
	 *
	 */
	private final class Field extends com.irt.data.Field implements QueryableField {
		public Field( String fieldKey, String descriptionKey ) {
			super( TYPE_NONE, fieldKey, descriptionKey);
		}

		@Override
		public boolean appendCondition( ConditionQueryBuffer querybuf ) {
			Object userId = querybuf.getConditionValue( "userId" );
			if( querybuf.hasConditionValue("readedByUser") && userId != null ) {
				if( querybuf.isConditionTrue("readedByUser") ) {
					querybuf.addBindVariable( QueryBuffer.COND_BINDVAR, userId );
					querybuf.addBindVariable( QueryBuffer.COND_BINDVAR, userId );
					querybuf.appendCondition( "(BRD.REGUSERID = ? OR BRU.USERID = ?)" );
				} else
					querybuf.appendCondition( "BRD.REGUSERID <> ? AND BRU.USERID IS NULL", userId );
				appendTable( querybuf, userId );

				return true;
			}

			return false;
		}

		@Override
		public boolean appendData( QueryBuffer querybuf ) {
			if( querybuf instanceof ConditionQueryBuffer ) {
				Object userId = ((ConditionQueryBuffer)querybuf).getConditionValue( "userId" );
				if( userId != null ) {
					if( querybuf.appendDataWithAlias( "DECODE( ?, BRD.REGUSERID, 'Y', BRU.USERID, 'Y', 'N' )", fieldKey ) ) {
						querybuf.addBindVariable( QueryBuffer.DATA_BINDVAR, userId );
						appendTable( querybuf, userId );
						return true;
					}
				}
			}
			return false;
		}

		private boolean appendTable( QueryBuffer querybuf, Object userId ) {
			if( querybuf.appendTableWithAlias("ICS_BOARD_READUSER", "BRU") ) {
				querybuf.appendCondition( "BRU.BOARDCLASSCD(+) = BRD.BOARDCLASSCD" );
				querybuf.appendCondition( "BRU.SEQID(+) = BRD.SEQID" );
				querybuf.appendCondition( "BRU.USERID(+) = ?", userId );
				return true;
			}

			return false;
		}
	}
}
