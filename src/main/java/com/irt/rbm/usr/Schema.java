/*
 *	File Name:	Schema.java
 *	Version:	2.2.1c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2020/12/31		2.2.1c	USR_USER: multi-sold-to 기능 추가
 *	hankalam	2020/09/28		2.2.1c	USR_USER: hashAlgorithm 추가
 *	jbaek		2019/11/30		2.2.1c	User 기본 조직 기능 추가
 *	jbaek		2019/07/30		2.2.1c	USR_PARTY_ENV, USR_USER_ENV 추가
 *	jbaek		2018/10/30		2.2.1c	USR_PARTY: blockIsoDate, mtnceIsoDate, blockTemplate, mtnceTemplate 추가
 *	jbaek		2014/12/31		2.2.1c	USR_SESSION_ACCESSLOG: userAgent 추가
 *	lsinji		2008/09/26		2.2.0c	USR_GROUP에 GROUP_CLASS 추가, USR_USER에 GROUP_CLASS QueryableField 추가 (DPR용)
 *										USR_USER.STATUS에 LOCK(LK) 추가
 *	stghr12		2007/11/30		2.2.0	DATE, DATETIME 구분
 *										USER_PARTY: EDIID -> TIMEZONE
 *										USER_USER: PASSWORD_CHGDATETIME 추가
 *										FIELD_USR_PARTYID, FIELD_USR_USERID -> FIELD_PARTYID, FIELD_USERID
 *	stghr12		2007/10/31		2.1.3	USER_USER: gln 추가
 *	stghr12		2007/09/04		2.1.2	USER_SESSION_HIST 오류 수정
 *	stghr12		2007/04/30		2.1.1	field size 조절
 *	stghr12		2006/12/01		2.1.0	checkMessageKey() 삭제
 *										USER_PARTY: extraValue1, extraValue2, extraValue3 descriptionKey 수정
 *										USER_USER: extraValue1, extraValue2, extraValue3 추가
 *										USER_SESSION: lastAccessTitle Query 변경, USER_USER와 JOIN
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.rbm.usr;

import com.irt.sql.*;

/**
 *
 */
public class Schema extends com.irt.sql.Schema {
	public final static String USER_PARTY				= "UserParty";
	public final static String USER_PARTY_SERVICEAUTH	= "UserPartyServiceAuth";
	public final static String USER_PARTY_ENV			= "UserPartyEnv";		// 없음
	public final static String USER_GROUP				= "UserGroup";
	public final static String USER_GROUP_SERVICEAUTH	= "UserGroupServiceAuth";
	public final static String USER_USER				= "UserUser";
	public final static String USER_USER_SERVICEAUTH	= "UserUserServiceAuth";
	public final static String USER_USER_ENV			= "UserUserEnv";		// 없음
	public final static String USER_USER_ICATE_MNG		= "UserUseriCategoryManage";
	public final static String USER_USER_CATE_MNG		= "UserUserCategoryManage";
	public final static String USER_SESSION				= "UserSession";
	public final static String USER_SESSION_HIST		= "UserSessionHistory";
	public final static String UESR_SESSION_ACCESSLOG	= "UserSessionAccessLog";

	private final static String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";

	private final static Schema schema = new Schema();

	Schema() {
		Table table;
		Table.Field[] tfields;

		final JoinableImpl tbl_UPT = new JoinableImpl( "{0}", "USR_PARTY",		"{0}.PARTY_ID(+) = {1}.PARTYID" );
		final JoinableImpl tbl_UGP = new JoinableImpl( "{0}", "USR_GROUP",		"{0}.PARTYID(+) = {1}.PARTYID AND {0}.GROUP_ID(+) = {1}.GROUPID" );
		final JoinableImpl tbl_USR = new JoinableImpl( "{0}", "USR_USER",		"{0}.PARTYID(+) = {1}.PARTYID AND {0}.USER_ID(+) = {1}.USERID" );
		final JoinableImpl tbl_SVG = new JoinableImpl( "{0}", "SYS_SERVGRP",	"{0}.SERVGRP_CD(+) = {1}.SERVGRPCD" );
		final JoinableImpl tbl_CATE = new JoinableImpl( "{0}", "SYS_CATE",		"{0}.CATE_CD(+) = {1}.CATECD" );
		final JoinableImpl tbl_ICATE = new JoinableImpl( "{0}", "SYS_ICATE",	"{0}.ICATE_CD(+) = {1}.ICATECD" );

		final QueryableFieldImpl fld_partyName, fld_groupName, fld_userName, fld_serviceGroupName;
		final QueryableFieldImpl fld_cateName, fld_icateName;
		fld_partyName = new QueryableFieldImpl( DESC, "partyName", "{0}.PARTY_NAME", "USR_PARTYNAME", tbl_UPT );
		fld_groupName = new QueryableFieldImpl( DESC, "groupName", "{0}.GROUP_NAME", "USR_GROUPNAME", tbl_UGP );
		fld_userName = new QueryableFieldImpl( DESC, "userName", "{0}.USER_NAME", "USR_USERNAME", tbl_USR );
		fld_serviceGroupName = new QueryableFieldImpl( DESC, "serviceGroupName", "{0}.SERVGRP_NAME", "USR_SERVICEGRP", tbl_SVG );
		fld_cateName = new QueryableFieldImpl( DESC, "categoryName", "{0}.CATE_NAME", "CATEGORY", tbl_CATE );
		fld_icateName = new QueryableFieldImpl( DESC, "iCategoryName", "{0}.ICATE_NAME", "ICATEGORY", tbl_ICATE );


		/***************************************************************************************************
		 *	USER_PARTY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyId",				"PARTY_ID",				"PARTYID",						0, 30, CHARS )
			, createFD( MD, "partyName",			"PARTY_NAME",			"USR_PARTYNAME",				DESC, 0, 128 )
			, createFD( OP, "description",			"PARTY_DESC",			"USR_DESCRIPTION",				DESC, 0, 500 )
			, createFD( MO, "password",				"PASSWORD",				"USR_PASSWORD",					0, 30 )
					.setInsertQuery( "NVL( ?, 'PASSWORD' )" )
			, createFD( OP, "partyGln",				"GLN",					"USR_PARTYGLN",					0, 13 )
			, createFD( OP, "timeZone",				"TIMEZONE",				"TIMEZONE",						0, 30 )
			, createFD( OP, "partyRegistration",	"PARTY_REGISTRATION",	"USR_PARTYREGISTRATION",		0, 30 )
			, createFD( OP, "telephone",			"TEL",					"USR_TELEPHONE",				0, 20 )
			, createFD( OP, "serviceStartDate",		"SERV_STARTDATE",		"USR_SERVICESTARTDATE",			DATE )
			, createFD( OP, "serviceEndDate",		"SERV_ENDDATE",			"USR_SERVICEENDDATE",			DATE )
			, createFD( OP, "blockStartIsoDate",	"BLOCK_START_ISODATE",	"USR_BLOCKSTART_ISODATE",		STRING, 20, 25 )
			, createFD( OP, "blockEndIsoDate",		"BLOCK_END_ISODATE",	"USR_BLOCKEND_ISODATE",			STRING, 20, 25 )
			, createFD( OP, "blockTemplate",		"BLOCK_TEMPLATE",		"USR_BLOCK_TEMPLATE",			DESC )
			, createFD( OP, "mtnceStartIsoDate",	"MTNCE_START_ISODATE",	"USR_MTNCESTART_ISODATE",		STRING, 20, 25 )
			, createFD( OP, "mtnceEndIsoDate",		"MTNCE_END_ISODATE",	"USR_MTNCEEND_ISODATE",			STRING, 20, 25 )
			, createFD( OP, "mtnceTemplateTitle",	"MTNCE_TEMPLATE_TITLE",	"USR_MTNCE_TEMPLATE_TITLE",		STRING, 0, 128 )
			, createFD( OP, "mtnceTemplate",		"MTNCE_TEMPLATE",		"USR_MTNCE_TEMPLATE",			DESC )
			, createFD( OP, "chargeStartDate",		"CHARGE_STARTDATE",		"USR_CHARGESTARTDATE",			DATE )
			, createFD( MO, "partyClass",			"PARTYCLASS",			"USR_PARTYCLASS",				"USR_PARTY_CLASS_", "XX,SX,XB,SB" )
			, createFD( OP, "extraValue1",			"EXTRAVALUE1",			"USR_PARTY_EXTRAVALUE1",		0, 128 )
			, createFD( OP, "extraValue2",			"EXTRAVALUE2",			"USR_PARTY_EXTRAVALUE2",		0, 128 )
			, createFD( OP, "extraValue3",			"EXTRAVALUE3",			"USR_PARTY_EXTRAVALUE3",		0, 128 )
			, createFD( MO, "status",				"STATUS",				"STATUS",						"USR_PARTY_STATUS_", "00,01,99,XX" )
					.setInsertQuery( "NVL( ?, '00' )" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( USER_PARTY, table = createTable("USR_PARTY", "UPT", tfields, "UPGDATE = SYSDATE") );
		putQueryable( USER_PARTY, new QueryableImpl(table, new QueryableField[] {
			  new QueryableFieldImpl( INTEGER, "userCount", "(SELECT COUNT(*) FROM USR_USER SB WHERE SB.PARTYID = UPT.PARTY_ID)", "USR_USERCOUNT" )
			, new QueryableFieldImpl( INTEGER, "blockCondCount", "(SELECT DECODE(BLOCK_START_ISODATE, NULL, 0, 1) + DECODE(MTNCE_START_ISODATE, NULL, 0, 1) FROM DUAL)" )
			, new QueryableFieldImpl( DESC, "timeZoneName", "TMZ.TIMEZONE_NAME", "TIMEZONE"
					, new JoinableImpl( "TMZ", "SYS_TIMEZONE", "TMZ.TIMEZONE_CD(+) = UPT.TIMEZONE" ) )
			, new QueryableFieldImpl( STRING, "adminUserName"
					, "( SELECT SB.USER_NAME FROM USR_USER SB WHERE SB.PARTYID = UPT.PARTY_ID AND SB.USERCLASS = 'PA' AND ROWNUM < 2)"
					, "USR_USERNAME" )
			, new QueryableFieldImpl( STRING, "adminUserUniqId"
					, "( SELECT SB.UNIQID FROM USR_USER SB WHERE SB.PARTYID = UPT.PARTY_ID AND SB.USERCLASS = 'PA' AND ROWNUM < 2)"
					, "USR_UNIQID" )
		}) );


		/***************************************************************************************************
		 *	USER_PARTY_SERVICEAUTH
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyId",				"PARTYID",				"PARTYID",						0, 30 )
			, createFD( PM, "serviceGroupCode",		"SERVGRPCD",			"USR_SERVICEGRP",				0, 30 )
			, createFD( OP, "serviceStartDate",		"SERV_STARTDATE",		"USR_SERVICESTARTDATE",			DATE )
			, createFD( OP, "serviceEndDate",		"SERV_ENDDATE",			"USR_SERVICEENDDATE",			DATE )
			, createFD( MO, "status",				"STATUS",				"STATUS",						"USR_SERVICE_STATUS_", "00,99,XX" )
					.setInsertQuery( "NVL( ?, '00' )" )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
		};
		putTable( USER_PARTY_SERVICEAUTH, table = createTable("USR_PARTY_SERV", "UPS", tfields) );
		putQueryable( USER_PARTY_SERVICEAUTH, new QueryableImpl(table, new QueryableField[] {
			  QueryableFieldImpl.replaceAlias( fld_partyName, "UPT", "UPS" )
			, QueryableFieldImpl.replaceAlias( fld_serviceGroupName, "SVG", "UPS" )
		}) );


		/***************************************************************************************************
		 *	USER_GROUP
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyId",				"PARTYID",				"PARTYID",						0, 30 )
			, createFD( PM, "groupId",				"GROUP_ID",				"USR_GROUPID",					0, 30, CHARS )
			, createFD( MD, "groupName",			"GROUP_NAME",			"USR_GROUPNAME",				DESC, 0, 128 )
			, createFD( OP, "description",			"GROUP_DESC",			"USR_DESCRIPTION",				DESC, 0, 500 )
			, createFD( OP, "groupClass",			"GROUP_CLASS",			"USR_GROUPCLASS",				0, 2 )
			, createFD( MO, "status",				"STATUS",				"STATUS",						"USR_GROUP_STATUS_", "00,01,99,XX" )
					.setInsertQuery( "NVL( ?, '00' )" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( USER_GROUP, table = createTable("USR_GROUP", "UGP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( USER_GROUP, new QueryableImpl(table, new QueryableField[] {
			  QueryableFieldImpl.replaceAlias( fld_partyName, "UPT", "UGP" )
			, new QueryableFieldImpl( INTEGER, "userCount"
					, "(SELECT COUNT(*) FROM USR_USER SB WHERE SB.PARTYID = UGP.PARTYID AND SB.GROUPID = UGP.GROUP_ID)", "USR_USERCOUNT" )
		}) );


		/***************************************************************************************************
		 *	USER_GROUP_SERVICEAUTH
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyId",				"PARTYID",				"PARTYID",						0, 30 )
			, createFD( PM, "groupId",				"GROUPID",				"USR_GROUPID",					0, 30 )
			, createFD( PM, "serviceGroupCode",		"SERVGRPCD",			"USR_SERVICEGRP",				0, 30 )
			, createFD( MO, "status",				"STATUS",				"STATUS",						"USR_SERVICE_STATUS_", "00,99,XX" )
					.setInsertQuery( "NVL( ?, '00' )" )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
		};
		putTable( USER_GROUP_SERVICEAUTH, table = createTable("USR_GROUP_AUTH", "UGA", tfields) );
		putQueryable( USER_GROUP_SERVICEAUTH, new QueryableImpl(table, new QueryableField[] {
			  QueryableFieldImpl.replaceAlias( fld_partyName, "UPT", "UGA" )
			, QueryableFieldImpl.replaceAlias( fld_groupName, "UGP", "UGA" )
			, QueryableFieldImpl.replaceAlias( fld_serviceGroupName, "SVG", "UGA" )
		  	, new QueryableFieldImpl( STRING, "groupClass", "UGP.GROUP_CLASS", JoinableImpl.replaceAlias(tbl_UGP, "UGP", "UGA") )
		}) );


		/***************************************************************************************************
		 *	USER_USER
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyId",				"PARTYID",				"PARTYID",						0, 30 )
			, createFD( PM, "userId",				"USER_ID",				"USERID",						0, 30, CHARS )
			, createFD( MR, "uniqId",				"UNIQID",				"USR_UNIQID",					0, 30 )
			, createFD( MD, "userName",				"USER_NAME",			"USR_USERNAME",					DESC, 0, 128 )
			, createFD( OP, "description",			"USER_DESC",			"USR_DESCRIPTION",				DESC, 0, 500 )
			, createFD( MO, "password",				"PASSWORD",				"USR_PASSWORD",					0, 256 )
			, createFD( RD, "passwordChangeDateTime",	"PASSWORD_CHGDATETIME",	"USR_PASSWORD_CHANGEDATETIME",	DATETIME )
			, createFD( OP, "hashAlgorithm",		"HASH_ALGORITHM",		"USR_PASSWORD_HASHALGORITHM",	0, 20 )
			, createFD( OP, "userGln",				"GLN",					"USR_USERGLN",					0, 13 )
			, createFD( OP, "groupId",				"GROUPID",				"USR_GROUPID",					0, 30 )
			, createFD( OP, "socialNumber",			"SOCIALNUMBER",			"USR_SOCIALNUMBER",				0, 14 )
			, createFD( OP, "department",			"DEPARTMENT",			"USR_DEPARTMENT",				0, 128 )
			, createFD( OP, "position",				"POSITION",				"USR_POSITION",					0, 128 )
			, createFD( OP, "email",				"EMAIL",				"USR_EMAIL",					0, 128 )
			, createFD( OP, "telephone",			"TEL",					"USR_TELEPHONE",				0, 20 )
			, createFD( OP, "mobilephone",			"HP",					"USR_MOBILEPHONE",				0, 20 )
			, createFD( OP, "fax",					"FAX",					"USR_FAX",						0, 20 )
			, createFD( OP, "serviceStartDate",		"SERV_STARTDATE",		"USR_SERVICESTARTDATE",			DATE )
			, createFD( OP, "serviceEndDate",		"SERV_ENDDATE",			"USR_SERVICEENDDATE",			DATE )
			, createFD( MO, "availAccessCount",		"AVAILACCCNT",			"USR_AVAILACCESSCOUNT",			INTEGER, 0, 99 )
			, createFD( MO, "userClass",			"USERCLASS",			"USR_USERCLASS",				"USR_USER_CLASS_", "UR,PA,SA" )
			, createFD( OP, "extraValue1",			"EXTRAVALUE1",			"USR_USER_EXTRAVALUE1",			0, 128 )
			, createFD( OP, "extraValue2",			"EXTRAVALUE2",			"USR_USER_EXTRAVALUE2",			0, 128 )
			, createFD( OP, "extraValue3",			"EXTRAVALUE3",			"USR_USER_EXTRAVALUE3",			0, 128 )
			, createFD( OP, "extraValue4",			"EXTRAVALUE4",			"USR_USER_EXTRAVALUE4",			0, 128 )
			, createFD( MO, "status",				"STATUS",				"STATUS",						"USR_USER_STATUS_", "00,01,99,XX,PW,LK" )
					.setInsertQuery( "NVL( ?, 'PW' )" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( USER_USER, table = createTable("USR_USER", "USR", tfields, "UPGDATE = SYSDATE") );
		putQueryable( USER_USER, new QueryableImpl(table) {{
			append( getTable(USER_PARTY), "UPT.PARTY_ID(+) = USR.PARTYID" );
			append( QueryableFieldImpl.replaceAlias( fld_groupName, "UGP", "USR" ) );
			append( new QueryableFieldImpl( STRING, "gln", "NVL(USR.GLN, UPT.GLN)", "GLN", JoinableImpl.replaceAlias(tbl_UPT, "UPT", "USR") ) );
			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "groupClass", "UGP.GROUP_CLASS", JoinableImpl.replaceAlias(tbl_UGP, "UGP", "USR") )
				, new QueryableFieldImpl( STRING, "languageName", "(SELECT SB.LANG_NAME FROM SYS_LANG SB WHERE SB.LANG_CD = USR.EXTRAVALUE2)" )
			} );

			JoinableImpl tbl_PAUTS = new JoinableImpl( "PAUTS"
					, "( SELECT PAUT.UNIQID"
							+ ", LISTAGG(PAUT.PARTYCD, '; ' ON OVERFLOW TRUNCATE '...') WITHIN GROUP(ORDER BY PAUT.PARTYCD) ALL_PARTY_CODES"
							+ ", LISTAGG(DECODE(PAUT.SOURCE,'S',NULL,PAUT.PARTYCD), '; ' ON OVERFLOW TRUNCATE '...') WITHIN GROUP(ORDER BY PAUT.PARTYCD) USR_PARTY_CODES"
							+ ", LISTAGG(DECODE(PAUT.SOURCE,'S',PAUT.PARTYCD), '; ' ON OVERFLOW TRUNCATE '...') WITHIN GROUP(ORDER BY PAUT.PARTYCD) EMP_PARTY_CODES"
							+ " FROM DPR_PARTY_AUTH PAUT, DPR_PARTY_SALES PTYS"
							+ " WHERE PAUT.ORGANIZATIONCD = PTYS.ORGANIZATIONCD"
							+ " AND PAUT.DIST_CHANNELCD = PTYS.DIST_CHANNELCD"
							+ " AND PAUT.PARTYCD = PTYS.PARTYCD"
							+ " AND PTYS.STATUS = '00'"
							+ " GROUP BY PAUT.UNIQID )"
					, "PAUTS.UNIQID(+) = USR.UNIQID" );

			JoinableImpl tbl_MEMPS = new JoinableImpl( "MEMPS"
					, "( SELECT MEMP.UNIQID, (LISTAGG(DISTINCT MEMP.EMPLOYEE_ID, '; ' ON OVERFLOW TRUNCATE '...') WITHIN GROUP(ORDER BY MEMP.EMPLOYEE_ID)) EMPLOYEE_IDS"
							+ " FROM DPR_USER_MULTIEMP MEMP, DPR_PARTY_EMPLOYEE PEMP"
							+ " WHERE MEMP.EMPLOYEE_ID = PEMP.EMPLOYEE_ID"
							+ " AND MEMP.COUNTRYCD = PEMP.COUNTRYCD"
							+ " GROUP BY MEMP.UNIQID )"
					, "MEMPS.UNIQID(+) = USR.UNIQID" );

			JoinableImpl tbl_PTSEMP = new JoinableImpl( "PTSEMP"
					, "( SELECT DISTINCT NULL PARTYCD, PTYS.ORGANIZATIONCD"
							+ " , UEMP.EMPLOYEE_ID, 'S' SOURCE, UEMP.UNIQID, PTYS.COUNTRYCD"
							+ ", (LISTAGG(NVL2(UEMP.EMPLOYEE_ID,PTYS.PARTYCD,NULL), '; ' ON OVERFLOW TRUNCATE '...') WITHIN GROUP(ORDER BY PTYS.PARTYCD)"
							+ " OVER( PARTITION BY UEMP.UNIQID, PTYS.ORGANIZATIONCD, UEMP.EMPLOYEE_ID )) USR_ORG_EMP_PTY_IDS"
							+ " FROM ( SELECT UNIQID, EMPLOYEE_ID, ORGANIZATIONCD, COUNTRYCD FROM DPR_USER_EMPLOYEE"
							+ "  UNION SELECT UNIQID, EMPLOYEE_ID, ORGANIZATIONCD, COUNTRYCD FROM DPR_USER_MULTIEMP ) UEMP"
							+ ", DPR_PARTY_EMPLOYEE PTYE, DPR_PARTY_SALES PTYS, DPR_COUNTRY_DIST CDIS, DPR_COUNTRY_COND CCND"
							+ " WHERE CCND.COUNTRYCD = CDIS.COUNTRYCD"
							+ " AND CCND.ORGANIZATIONCD = CDIS.ORGANIZATIONCD"
							+ " AND CCND.COUNTRYCD = PTYS.COUNTRYCD"
							+ " AND CCND.ORGANIZATIONCD = PTYS.ORGANIZATIONCD"
							+ " AND CDIS.DIST_CHANNELCD = PTYS.DIST_CHANNELCD"
							+ " AND PTYE.PARTYCD(+) = PTYS.PARTYCD"
							+ " AND UEMP.EMPLOYEE_ID = PTYE.EMPLOYEE_ID"
							+ " AND UEMP.ORGANIZATIONCD = PTYS.ORGANIZATIONCD"
							+ " AND PTYS.DIVISIONCD = '10'"
							+ " AND PTYS.STATUS = '00'"
							+ " UNION"
							+ " SELECT DISTINCT PTYS.PARTYCD, PTYS.ORGANIZATIONCD"
							+ " , NULL EMPLOYEE_ID, 'D' SOURCE"
							+ " , PAUT.UNIQID, PTYS.COUNTRYCD"
							+ " , NULL"
							+ " FROM DPR_PARTY_AUTH PAUT, DPR_PARTY_SALES PTYS, DPR_COUNTRY_DIST CDIS, DPR_COUNTRY_COND CCND"
							+ " WHERE CCND.COUNTRYCD = CDIS.COUNTRYCD"
							+ " AND CCND.ORGANIZATIONCD = CDIS.ORGANIZATIONCD"
							+ " AND CCND.COUNTRYCD = PTYS.COUNTRYCD"
							+ " AND CCND.ORGANIZATIONCD = PTYS.ORGANIZATIONCD"
							+ " AND CDIS.DIST_CHANNELCD = PTYS.DIST_CHANNELCD"
							+ " AND PAUT.PARTYCD(+) = PTYS.PARTYCD"
							+ " AND PAUT.ORGANIZATIONCD(+) = PTYS.ORGANIZATIONCD"
							+ " AND PAUT.DIST_CHANNELCD (+) = PTYS.DIST_CHANNELCD"
							+ " AND (PAUT.SOURCE IS NULL OR PAUT.SOURCE = 'D')"
							+ " AND PTYS.DIVISIONCD = '10'"
							+ " AND PTYS.STATUS = '00' )"
							, "PTSEMP.UNIQID(+) = USR.UNIQID" );

			QueryBufferValid valid_multiSoldTo = new QueryBufferValid.ConditionTrue("_multiSoldTo");
			appendCND( valid_multiSoldTo, new QueryableFieldImpl[] {
					  new QueryableFieldImpl( STRING, "soldPartyCodes", "PAUTS.ALL_PARTY_CODES", tbl_PAUTS )
					, new QueryableFieldImpl( STRING, "usrPartyCodes", "PAUTS.USR_PARTY_CODES", tbl_PAUTS )
					, new QueryableFieldImpl( STRING, "empPartyCodes", "PAUTS.EMP_PARTY_CODES", tbl_PAUTS )
					, new QueryableFieldImpl( STRING, "employeeIds", "MEMPS.EMPLOYEE_IDS", tbl_MEMPS )
					, new QueryableFieldImpl( STRING, "soldPartyCode", "PTSEMP.PARTYCD" , tbl_PTSEMP )
					, new QueryableFieldImpl( STRING, "soldPartyOrgCode", "PTSEMP.ORGANIZATIONCD" , tbl_PTSEMP )
					, new QueryableFieldImpl( STRING, "employeeSource", "PTSEMP.SOURCE" , tbl_PTSEMP )
					, new QueryableFieldImpl( STRING, "employeeId", "PTSEMP.EMPLOYEE_ID" , tbl_PTSEMP )
					, new QueryableFieldImpl( STRING, "usrOrgEmpPtyIds", "PTSEMP.USR_ORG_EMP_PTY_IDS", tbl_PTSEMP )
			});
		}} );


		/***************************************************************************************************
		 *	USER_USER_SERVICEAUTH
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyId",				"PARTYID",				"PARTYID",						0, 30 )
			, createFD( PM, "userId",				"USERID",				"USERID",						0, 30 )
			, createFD( PM, "serviceGroupCode",		"SERVGRPCD",			"USR_SERVICEGRP",				0, 30 )
			, createFD( MO, "status",				"STATUS",				"STATUS",						"USR_SERVICE_STATUS_", "00,99,XX" )
					.setInsertQuery( "NVL( ?, '00' )" )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
		};
		putTable( USER_USER_SERVICEAUTH, table = createTable("USR_USER_AUTH", "UUA", tfields) );
		putQueryable( USER_USER_SERVICEAUTH, new QueryableImpl(table, new QueryableField[] {
			  QueryableFieldImpl.replaceAlias( fld_partyName, "UPT", "UUA" )
			, QueryableFieldImpl.replaceAlias( fld_userName, "USR", "UUA" )
			, QueryableFieldImpl.replaceAlias( fld_serviceGroupName, "SVG", "UUA" )
		}) );


		/***************************************************************************************************
		 *	USER_USER_ICATE_MNG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyId",				"PARTYID",				"PARTYID",						0, 30 )
			, createFD( PM, "userId",				"USERID",				"USERID",						0, 30 )
			, createFD( PM, "iCategoryCode",		"ICATECD",				"ICATEGORYCODE",				0, 20 )
			, createFD( PM, "extraValue",			"EXTRAVALUE",			"USR_ICATEGORYMNG_EXTRAVALUE",	0, 128 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( USER_USER_ICATE_MNG, table = createTable("USR_USER_ICATE_MNG", "UUCM", tfields) );
		putQueryable( USER_USER_ICATE_MNG, new QueryableImpl(table, new QueryableField[] {
			  QueryableFieldImpl.replaceAlias( fld_partyName, "UPT", "UUCM" )
			, QueryableFieldImpl.replaceAlias( fld_userName, "USR", "UUCM" )
			, QueryableFieldImpl.replaceAlias( fld_icateName, "ICATE", "UUCM" )
		}) );


		/***************************************************************************************************
		 *	USER_USER_CATE_MNG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "partyId",				"PARTYID",				"PARTYID",						0, 30 )
			, createFD( PM, "userId",				"USERID",				"USERID",						0, 30 )
			, createFD( PM, "iCategoryCode",		"CATECD",				"CATEGORYCODE",					0, 20 )
			, createFD( PM, "extraValue",			"EXTRAVALUE",			"USR_CATEGORYMNG_EXTRAVALUE",	0, 128 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( USER_USER_CATE_MNG, table = createTable("USR_USER_CATE_MNG", "UUCM", tfields) );
		putQueryable( USER_USER_CATE_MNG, new QueryableImpl(table, new QueryableField[] {
			  QueryableFieldImpl.replaceAlias( fld_partyName, "UPT", "UUCM" )
			, QueryableFieldImpl.replaceAlias( fld_userName, "USR", "UUCM" )
			, QueryableFieldImpl.replaceAlias( fld_cateName, "CATE", "UUCM" )
		}) );


		/***************************************************************************************************
		 *	USER_SESSION
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( OP, "sessionId",			"SESSIONID",			"USR_SESSIONID",				STRING )
			, createFD( OP, "uniqId",				"UNIQID",				"USR_UNIQID",					STRING )
			, createFD( OP, "partyId",				"PARTYID",				"PARTYID",						STRING )
			, createFD( OP, "userId",				"USERID",				"USERID",						STRING )
			, createFD( OP, "partyName",			"PARTYNAME",			"USR_PARTYNAME",				DESC )
			, createFD( OP, "userName",				"USERNAME",				"USR_USERNAME",					DESC )
			, createFD( OP, "effectiveUniqId",		"EFFECT_UNIQID",		"USR_EFFECT_UNIQID",			STRING )
			, createFD( OP, "effectivePartyId",		"EFFECT_PARTYID",		"USR_EFFECT_PARTYID",			STRING )
			, createFD( OP, "effectiveUserId",		"EFFECT_USERID",		"USR_EFFECT_USERID",			STRING )
			, createFD( OP, "effectiveGln",			"EFFECT_GLN",			"USR_EFFECT_GLN",				STRING )
			, createFD( OP, "effectivePartyName",	"EFFECT_PARTYNAME",		"USR_EFFECT_PARTYNAME",			DESC )
			, createFD( OP, "effectiveUserName",	"EFFECT_USERNAME",		"USR_EFFECT_USERNAME",			DESC )
			, createFD( OP, "ip",					"IP",					"USR_SESSION_IP",				STRING )
			, createFD( OP, "loginDateTime",		"LOGIN_TIME",			"USR_SESSION_LOGIN_TIME",		DATETIME )
			, createFD( OP, "lastAccessSystemCode",	"LASTACC_SYSTEMCD",		"USR_SESSION_LASTACCESS_SYS",	STRING )
			, createFD( OP, "lastAccessPackageCode","LASTACC_PKGCD",		"USR_SESSION_LASTACCESS_PKG",	STRING )
			, createFD( OP, "lastAccessDateTime",	"LASTACC_TIME",			"USR_SESSION_LASTACCESS_TIME",	DATETIME )
			, createFD( OP, "accessCount",			"ACCCOUNT",				"USR_SESSION_ACCESSCOUNT",		INTEGER )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"USR_SESSION_EXTRAVALUE",		STRING )
			, createFD( OP, "status",				"STATUS",				"STATUS",						"USR_SESSION_STATUS_", "00,LO,LK" )
		};
		putTable( USER_SESSION, table = createTable("USR_SESSION", "USS", tfields) );
		putQueryable( USER_SESSION, new QueryableImpl(table) {{
			append( new QueryableField[] {
				new QueryableFieldImpl( DESC, "lastAccessUserAgent"
						, "( SELECT SUBSTRB(MAX(TO_CHAR(ACCESSTIME, 'YYYYMMDDHH24MISS') || USER_AGENT), 15)"
							+ " FROM USR_SESSION_ACCESSLOG SB WHERE SB.SESSIONID = USS.SESSIONID )", "USR_SESSION_LASTACCESS_USERAGENT" )
				, new QueryableFieldImpl( DESC, "lastAccessTitle"
						, "( SELECT SUBSTRB(MAX(TO_CHAR(ACCESSTIME, 'YYYYMMDDHH24MISS') || TITLE), 15)"
							+ " FROM USR_SESSION_ACCESSLOG SB WHERE SB.SESSIONID = USS.SESSIONID )", "USR_SESSION_LASTACCESS_TITLE" )
				, new QueryableFieldImpl( DESC, "lastAccessSystemName", "SYS.SYSTEM_NAME", "USR_SESSION_LASTACCESS_SYS"
						, new JoinableImpl( "SYS", "SYS_SYSTEM", "SYS.SYSTEM_CD(+) = USS.LASTACC_SYSTEMCD" ) )
				, new QueryableFieldImpl( DESC, "lastAccessPackageName", "PKG.PKG_NAME", "USR_SESSION_LASTACCESS_PKG"
						, new JoinableImpl( "PKG", "SYS_SYSTEM_PKG"
							, "PKG.SYSTEMCD(+) = USS.LASTACC_SYSTEMCD AND PKG.PKG_CD(+) = USS.LASTACC_PKGCD" ) )
			} );

			append( getQueryable(Schema.USER_USER), "USR.UNIQID(+) = USS.UNIQID" );
		}} );


		/***************************************************************************************************
		 *	USER_SESSION_HIST
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( OP, "sessionId",			"SESSIONID",			"USR_SESSIONID",				STRING )
			, createFD( OP, "uniqId",				"UNIQID",				"USR_UNIQID",					STRING )
			, createFD( OP, "partyId",				"PARTYID",				"PARTYID",						STRING )
			, createFD( OP, "userId",				"USERID",				"USERID",						STRING )
			, createFD( OP, "partyName",			"PARTYNAME",			"USR_PARTYNAME",				DESC )
			, createFD( OP, "userName",				"USERNAME",				"USR_USERNAME",					DESC )
			, createFD( OP, "ip",					"IP",					"USR_SESSION_IP",				STRING )
			, createFD( OP, "loginDateTime",		"LOGIN_TIME",			"USR_SESSION_LOGIN_TIME",		DATETIME )
			, createFD( OP, "lastAccessDateTime",	"LASTACC_TIME",			"USR_SESSION_LASTACCESS_TIME",	DATETIME )
			, createFD( OP, "logoutDateTime",		"LOGOUT_TIME",			"USR_SESSION_LOGOUT_TIME",		DATETIME )
			, createFD( OP, "killDateTime",			"KILL_TIME",			"USR_SESSION_KILL_TIME",		DATETIME )
			, createFD( OP, "accessCount",			"ACCCOUNT",				"USR_SESSION_ACCESSCOUNT",		INTEGER )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"USR_SESSION_EXTRAVALUE",		STRING )
			, createFD( OP, "status",				"STATUS",				"STATUS",						"USR_SESSION_STATUS_", "00,LO,LK" )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
		};
		putTable( USER_SESSION_HIST, table = createTable("USR_SESSION_HIST", "USH", tfields) );


		/***************************************************************************************************
		 *	UESR_SESSION_ACCESSLOG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( OP, "sessionId",			"SESSIONID",			"USR_SESSIONID",				STRING )
			, createFD( OP, "accessDateTime",		"ACCESSTIME",			"USR_SESSION_ACCESS_TIME",		DATETIME )
			, createFD( OP, "uniqId",				"UNIQID",				"USR_UNIQID",					STRING )
			, createFD( OP, "partyId",				"PARTYID",				"PARTYID",						STRING )
			, createFD( OP, "userId",				"USERID",				"USERID",						STRING )
			, createFD( OP, "systemCode",			"SYSTEMCD",				"USR_SESSION_ACCESS_SYS",		STRING )
			, createFD( OP, "packageCode",			"PKGCD",				"USR_SESSION_ACCESS_PKG",		STRING )
			, createFD( OP, "className",			"CLASSNAME",			"USR_SESSION_ACCESS_CLASSNAME",	STRING )
			, createFD( OP, "requestMode",			"REQUESTMODE",			"USR_SESSION_ACCESS_REQUESTMODE",	STRING )
			, createFD( OP, "title",				"TITLE",				"USR_SESSION_ACCESS_TITLE",		DESC )
			, createFD( OP, "returnValue",			"RETURNVALUE",			"USR_SESSION_ACCESS_RETURNVALUE",	DESC )
			, createFD( OP, "userAgent",			"USER_AGENT",			"USR_SESSION_USERAGENT",		STRING )
			, createFD( OP, "message",				"MESSAGE",				"USR_SESSION_ACCESS_MESSAGE",	DESC )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"USR_SESSION_EXTRAVALUE",		STRING )
		};
		putTable( UESR_SESSION_ACCESSLOG, table = createTable("USR_SESSION_ACCESSLOG", "USA", tfields) );
		putQueryable( UESR_SESSION_ACCESSLOG, new QueryableImpl(table, new QueryableField[] {
			  new QueryableFieldImpl( DESC, "systemName", "SYS.SYSTEM_NAME", "USR_SESSION_ACCESS_SYS"
					, new JoinableImpl( "SYS", "SYS_SYSTEM", "SYS.SYSTEM_CD(+) = USS.SYSTEMCD" ) )
			, new QueryableFieldImpl( DESC, "packageName", "PKG.PKG_NAME", "USR_SESSION_ACCESS_PKG"
					, new JoinableImpl( "PKG", "SYS_SYSTEM_PKG", "PKG.SYSTEMCD(+) = USS.SYSTEMCD AND PKG.PKG_CD(+) = USS.PKGCD" ) )
		}) );


		/***************************************************************************************************
		 * USR_PARTY_ENV
		 ***************************************************************************************************/
		tfields = new Table.Field[] {
				createFD(PM, "partyId", "PARTYID", "PARTYID", 0, 30),
				createFD(PM, "systemCode", "SYSTEMCD", "SYS_SYSTEMCODE", 0, 30),
				createFD(PM, "envName", "NAME", "SYS_SYSTEM_ENV_NAME", STRING),
				createFD(RD, "envArray", "VALUE", "SYS_SYSTEM_ENV_ARRAY", STRING),
				createFD(RD, "status", "STATUS", "STATUS", "PUB_STATUS_", "00"),
				createFD(RD, "createDateTime", "REGDATE", "CREATEDATETIME", DATETIME),
				createFD(RD, "updateDateTime", "UPGDATE", "UPDATEDATETIME", DATETIME)
		};
		putTable(com.irt.rbm.usr.Schema.USER_PARTY_ENV, table = createTable("USR_PARTY_ENV", "PENV", tfields));


		/***************************************************************************************************
		 * USR_USER_ENV
		 ***************************************************************************************************/
		tfields = new Table.Field[] {
				createFD(PM, "partyId", "PARTYID", "PARTYID", 0, 30),
				createFD(PM, "userId", "USERID", "USERID", 0, 30),
				createFD(PM, "systemCode", "SYSTEMCD", "SYS_SYSTEMCODE", 0, 30),
				createFD(PM, "envName", "NAME", "SYS_SYSTEM_ENV_NAME", STRING),
				createFD(RD, "envArray", "VALUE", "SYS_SYSTEM_ENV_ARRAY", STRING),
				createFD(RD, "status", "STATUS", "STATUS", "PUB_STATUS_", "00"),
				createFD(RD, "createDateTime", "REGDATE", "CREATEDATETIME", DATETIME),
				createFD(RD, "updateDateTime", "UPGDATE", "UPDATEDATETIME", DATETIME)
		};
		putTable(com.irt.rbm.usr.Schema.USER_USER_ENV, table = createTable("USR_USER_ENV", "UENV", tfields));
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
