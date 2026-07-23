/*
 *	File Name:	Schema.java
 *	Version:	2.2.2c(dpr)
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2018/11/30		2.2.1c	SYS_SYSTEM_ENVIRONMENT 추가
 *	jbaek		2017/06/30		2.2.1c	SYS_SERVGRP_PKG table key 이름 맵핑 오류 수정.
 *	stghr12		2008/03/31		2.2.1	loadCategory(): classCode를 "1,2,3"으로만 정의하는 오류 수정
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										DATE, DATETIME 구분
 *										SYS_POSTALCODE: COUNTRYCD 추가
 *										SYS_TIMEZONE 추가
 *	stghr12		2007/10/31		2.1.2	SYS_CODE.CODE_CD의 validCharacters 변경
 *	stghr12		2007/04/30		2.1.1	field size 조절
 *										loadCategory() 수정
 *										makeCategoryLevelQueryFields() 추가
 *										SYS_SPSC, SYS_HS 순서변경
 *	stghr12		2006/12/01		2.1.0	checkMessageKey() 삭제
 *										displaySequence의 범위를 (0 ~ 999999)에서 (1 ~ 999999)로 변경
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.rbm.sys;

import java.util.Map;

import com.irt.data.FieldException;
import com.irt.sql.HierarchyCodeField;
import com.irt.sql.Joinable;
import com.irt.sql.JoinableImpl;
import com.irt.sql.QueryFactory;
import com.irt.sql.Queryable;
import com.irt.sql.QueryableField;
import com.irt.sql.QueryableFieldImpl;
import com.irt.sql.QueryableImpl;
import com.irt.sql.Table;

/**
 *
 */
public class Schema extends com.irt.sql.Schema {
	public final static String SYS_SYSTEM				= "System";
	public final static String SYS_SYSTEM_PACKAGE		= "SystemPackage";
	public final static String SYS_SYSTEM_ENVIRONMENT	= "SystemEnv";
	public final static String SYS_SERVICEGRP			= "ServiceGroup";
	public final static String SYS_SERVICEGRP_PACKAGE	= "ServiceGroupPackage";
	public final static String SYS_SERVICEGRP_LINK		= "ServiceGroupLink";
	public final static String SYS_ICATEGORY			= "iCategory";
	public final static String SYS_CATEGORY				= "Category";
	public final static String SYS_SPSC					= "UNSPSC";
	public final static String SYS_HS					= "HS";
	public final static String SYS_COUNTRY				= "Country";
	public final static String SYS_LANGUAGE				= "Language";
	public final static String SYS_CURRENCY				= "Currency";
	public final static String SYS_UNIT					= "Unit";
	public final static String SYS_HANDLINGINFO			= "HandlingInfo";
	public final static String SYS_PACKAGETYPE			= "PackageType";
	public final static String SYS_PACKAGEMATERIAL		= "PackageMaterial";
	public final static String SYS_WEATHER				= "Weather";
	public final static String SYS_BANK					= "Bank";
	public final static String SYS_POSTALCODE			= "PostalCode";
	public final static String SYS_TIMEZONE				= "TimeZone";
	public final static String SYS_CODETYPE				= "CodeType";
	public final static String SYS_CODE					= "Code";
	public final static String SYS_TRACE				= "Trace";						// 없음
	public final static String SYS_PACKAGE_ERROR		= "PackageError";				// 없음

	private final static String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";

	private final static Schema schema = new Schema();

	Schema() {
		Table table;
		Table.Field[] tfields;
		Table.Field fld_classcd;
		HierarchyCodeField fld_cd;

		JoinableImpl tbl_SYS = new JoinableImpl( "{0}", "SYS_SYSTEM",		"{0}.SYSTEM_CD(+) = {1}.SYSTEMCD" );
		JoinableImpl tbl_PKG = new JoinableImpl( "{0}", "SYS_SYSTEM_PKG",	"{0}.SYSTEMCD(+) = {1}.SYSTEMCD AND {0}.PKG_CD(+) = {1}.PKGCD" );
		JoinableImpl tbl_SVG = new JoinableImpl( "{0}", "SYS_SERVGRP",		"{0}.SERVGRP_CD(+) = {1}.SERVGRPCD" );

		final QueryableFieldImpl fld_systemName, fld_packageName, fld_serviceGroupName;
		fld_systemName = new QueryableFieldImpl( DESC, "systemName", "{0}.SYSTEM_NAME", "SYS_SYSTEMNAME", tbl_SYS );
		fld_packageName = new QueryableFieldImpl( DESC, "packageName", "{0}.PKG_NAME", "SYS_PACKAGENAME", tbl_PKG );
		fld_serviceGroupName = new QueryableFieldImpl( DESC, "serviceGroupName", "{0}.SERVGRP_NAME", "SYS_SERVICEGRPNAME", tbl_SVG );


		/***************************************************************************************************
		 *	SYS_SYSTEM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "systemCode",			"SYSTEM_CD",			"SYS_SYSTEMCODE",				0, 30 )
			, createFD( MD, "systemName",			"SYSTEM_NAME",			"SYS_SYSTEMNAME",				DESC, 0, 128 )
			, createFD( OP, "version",				"SYSTEM_VERSION",		"SYS_VERSION",					0, 10 )
			, createFD( OP, "description",			"SYSTEM_DESC",			"DESCRIPTION",					DESC, 0, 500 )
			, createFD( OP, "installer",			"SYSTEM_INSTALLER",		"SYS_INSTALLER",				0, 20 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
			, createFD( RD, "installDateTime",		"INSTALLDATE",			"SYS_INSTALLDATETIME",			DATETIME )
		};
		putTable( SYS_SYSTEM, table = createTable("SYS_SYSTEM", "SYS", tfields) );


		/***************************************************************************************************
		 *	SYS_SYSTEM_PACKAGE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "systemCode",			"SYSTEMCD",				"SYS_SYSTEMCODE",				0, 30 )
			, createFD( PM, "packageCode",			"PKG_CD",				"SYS_PACKAGECODE",				0, 30 )
			, createFD( MD, "packageName",			"PKG_NAME",				"SYS_PACKAGENAME",				DESC, 0, 128 )
			, createFD( OP, "version",				"PKG_VERSION",			"SYS_VERSION",					0, 10 )
			, createFD( OP, "description",			"PKG_DESC",				"DESCRIPTION",					DESC, 0, 500 )
			, createFD( OP, "parentPackageCode",	"PARENTPKGCD",			"SYS_PARENT_PACKAGECODE",		0, 30 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
			, createFD( RD, "installDateTime",		"INSTALLDATE",			"SYS_INSTALLDATETIME",			DATETIME )
		};
		putTable( SYS_SYSTEM_PACKAGE, table = createTable("SYS_SYSTEM_PKG", "PKG", tfields) );
		putQueryable( SYS_SYSTEM_PACKAGE, new QueryableImpl(table) {{
			append( getQueryable(SYS_SYSTEM), "SYS.SYSTEM_CD(+) = PKG.SYSTEMCD" );
			append( new QueryableFieldImpl( DESC, "parentPackageName", "PPKG.PKG_NAME", "SYS_PARENT_PACKAGENAME",
					new JoinableImpl( "PPKG", "SYS_SYSTEM_PKG", "PPKG.SYSTEMCD(+) = PKG.SYSTEMCD AND PPKG.PKG_CD(+) = PKG.PARENTPKGCD" )
			) );
		}} );

		/***************************************************************************************************
		 *	SYS_SYSTEM_ENV
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "systemCode",			"SYSTEMCD",				"SYS_SYSTEMCODE",				0, 30 )
			, createFD( PM, "envName",				"NAME",					"ENV_NAME",						DESC, 0, 128 )
			, createFD( OP, "envValue",				"VALUE",				"ENV_VALUE",					DESC, 0, 500 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( SYS_SYSTEM_ENVIRONMENT, table = createTable("SYS_SYSTEM_ENV", "ENV", tfields) );

		/***************************************************************************************************
		 *	SYS_SERVICEGRP
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "serviceGroupCode",		"SERVGRP_CD",			"SYS_SERVICEGRPCODE",			0, 30 )
			, createFD( MD, "serviceGroupName",		"SERVGRP_NAME",			"SYS_SERVICEGRPNAME",			DESC, 0, 128 )
			, createFD( OP, "description",			"SERVGRP_DESC",			"DESCRIPTION",					DESC, 0, 500 )
			, createFD( OP, "systemCode",			"SYSTEMCD",				"SYS_SYSTEMCODE",				0, 30 )
			, createFD( OP, "partyId",				"PARTYID",				"USR_PARTYID",					0, 30 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( SYS_SERVICEGRP, table = createTable("SYS_SERVGRP", "SVG", tfields, "UPGDATE = SYSDATE") );
		putQueryable( SYS_SERVICEGRP, new QueryableImpl(table, new QueryableField[] {
			QueryableFieldImpl.replaceAlias( fld_systemName, "SYS", "SVG" )
			, new QueryableFieldImpl( DESC, "partyName", "UPT.PARTY_NAME", "USR_PARTYNAME",
					new JoinableImpl( "UPT", "USR_PARTY", "UPT.PARTY_ID(+) = SVG.PARTYID" ) )
			, new QueryableFieldImpl( INTEGER, "packageCount"
					, "(SELECT COUNT(*) FROM SYS_SERVGRP_PKG SB WHERE SB.SERVGRPCD = SERVGRP_CD )", "SYS_SERVICEGRP_PACKAGECOUNT" )
			, new QueryableFieldImpl( INTEGER, "subServiceGroupCount"
					, "(SELECT COUNT(*) FROM SYS_SERVGRP_LINK SB WHERE SB.SERVGRPCD = SERVGRP_CD )", "SYS_SERVICEGRP_SUBSERVICEGRPCOUNT" )
		}) );


		/***************************************************************************************************
		 *	SYS_SERVICEGRP_PACKAGE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "serviceGroupCode",		"SERVGRPCD",			"SYS_SERVICEGRPCODE",			0, 30 )
			, createFD( PM, "systemCode",			"SYSTEMCD",				"SYS_SYSTEMCODE",				0, 30 )
			, createFD( PM, "packageCode",			"PKGCD",				"SYS_PACKAGECODE",				0, 30 )
			, createFD( OP, "authLevel",			"AUTHLEVEL",			"SYS_SERVICEGRP_AUTHLEVEL",		INTEGER, 0, 999 )
			, createFD( OP, "extraValue1",			"EXTRAVALUE1",			"SYS_SERVICEGRP_EXTRA1",		0, 128 )
			, createFD( OP, "extraValue2",			"EXTRAVALUE2",			"SYS_SERVICEGRP_EXTRA2",		0, 128 )
			, createFD( OP, "extraValue3",			"EXTRAVALUE3",			"SYS_SERVICEGRP_EXTRA3",		0, 128 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( SYS_SERVICEGRP_PACKAGE, table = createTable("SYS_SERVGRP_PKG", "SVGP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( SYS_SERVICEGRP_PACKAGE, new QueryableImpl(table, new QueryableField[] {
			QueryableFieldImpl.replaceAlias( fld_systemName, "SYS", "SVGP" )
			, QueryableFieldImpl.replaceAlias( fld_packageName, "PKG", "SVGP" )
			, QueryableFieldImpl.replaceAlias( fld_serviceGroupName, "SVG", "SVGP" )
		}) );


		/***************************************************************************************************
		 *	SYS_SERVICEGRP_LINK
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "serviceGroupCode",		"SERVGRPCD",			"SYS_SERVICEGRPCODE",			0, 30 )
			, createFD( PM, "serviceGroupSubCode",	"SERVGRPSUBCD",			"SYS_SERVICEGRPSUBCODE",		0, 30 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( SYS_SERVICEGRP_LINK, table = createTable("SYS_SERVGRP_LINK", "SVGL", tfields, "UPGDATE = SYSDATE") );
		putQueryable( SYS_SERVICEGRP_LINK, new QueryableImpl(table, new QueryableField[] {
			QueryableFieldImpl.replaceAlias( fld_serviceGroupName, "SVG", "SVGL" )
			, new QueryableFieldImpl( DESC, "serviceGroupSubName", "SSVG.SERVGRP_NAME", "SYS_SERVICEGRPSUBNAME",
					new JoinableImpl( "SSVG", "SYS_SERVGRP", "SSVG.SERVGRP_CD(+) = SVGL.SERVGRPCD" ) )
		}) );


		/***************************************************************************************************
		 *	SYS_ICATEGORY
		***************************************************************************************************/
		fld_classcd = createFD( MR, "classCode", "CLASSCD", "CLASSCODE", "PUB_CLASSCODE_", "1,2,3" );
		fld_cd = new HierarchyCodeField( createFD(PM, "code", "ICATE_CD", "CODE", 0, 20, CHARS), fld_classcd, new int[] { 2, 4, 6 } );
		tfields = new Table.Field[] {
			  fld_cd, fld_classcd
			, createFD( MD, "name",					"ICATE_NAME",			"NAME",							DESC, 0, 128 )
			, createFD( OP, "description",			"ICATE_DESC",			"DESCRIPTION",					DESC, 0, 500 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( SYS_ICATEGORY, table = createTable("SYS_ICATE", "ICATE", tfields, "UPGDATE = SYSDATE") );
		putQueryable( SYS_ICATEGORY, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( INTEGER, "lowerCount"
					, "(SELECT COUNT(*) FROM SYS_ICATE SB WHERE SB.ICATE_CD LIKE ICATE.ICATE_CD || '%'"
							+" AND SB.CLASSCD = TO_CHAR(TO_NUMBER(ICATE.CLASSCD)+1) )", "LOWERLEVELCOUNT" )
			, new QueryableFieldImpl( STRING, "levelCode", fld_cd.getLevelCodeQuery(), "LEVELCODE" )
		}) );


		/***************************************************************************************************
		 *	SYS_SPSC
		***************************************************************************************************/
		fld_classcd = createFD( MR, "classCode", "CLASSCD", "CLASSCODE", "PUB_CLASSCODE_", "1,2,3,4,5" );
		fld_cd = new HierarchyCodeField( createFD(PM, "code", "SPSC_CD", "CODE", 0, 12, CHARS), fld_classcd, new int[] { 2, 4, 6, 8, 10 } );
		tfields = new Table.Field[] {
			  fld_cd, fld_classcd
			, createFD( MD, "name",					"SPSC_NAME",			"NAME",							DESC, 0, 128 )
			, createFD( OP, "description",			"SPSC_DESC",			"DESCRIPTION",					DESC, 0, 500 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( SYS_SPSC, table = createTable("SYS_SPSC", "SPSC", tfields, "UPGDATE = SYSDATE") );
		putQueryable( SYS_SPSC, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( INTEGER, "lowerCount"
					, "(SELECT COUNT(*) FROM SYS_SPSC SB WHERE SB.SPSC_CD LIKE SPSC.SPSC_CD || '%'"
							+" AND SB.CLASSCD = TO_CHAR(TO_NUMBER(SPSC.CLASSCD)+1) )", "LOWERLEVELCOUNT" )
			, new QueryableFieldImpl( STRING, "levelCode", fld_cd.getLevelCodeQuery(), "LEVELCODE" )
		}) );


		/***************************************************************************************************
		 *	SYS_HS
		***************************************************************************************************/
		fld_classcd = createFD( MR, "classCode", "CLASSCD", "CLASSCODE", "PUB_CLASSCODE_", "1,2,3,4" );
		fld_cd = new HierarchyCodeField( createFD(PM, "code", "HS_CD", "CODE", 0, 10, CHARS), fld_classcd, new int[] { 2, 4, 6, 10 } ) {
			@Override
			public String[] getUpperLevelCodes( String code, boolean containsSelf ) {
				if( code == null || code.length() < 6 ) return super.getUpperLevelCodes( code, containsSelf );

				String[] codes = new String[ code.length() - (containsSelf ? 2 : 3) ];
				codes[0] = code.substring( 0, 2 );
				codes[1] = code.substring( 0, 4 );
				for( int i = 2; i < codes.length; i++ )
					codes[i] = code.substring( 0, i+3 );

				return codes;
			}

			@Override
			public Object validate( Map recordMap ) throws FieldException {
				String value = (String)super.validate( extractValue(recordMap), true );

				int length = getLength( recordMap );
				if( length > 0 && value != null ) {
					int valueLength = value.length();
					if( valueLength > 6 )
						valueLength = 10;
					else if( valueLength > 4 )
						valueLength = 6;

					if( valueLength != length )
						throw new FieldException( FieldException.ERR_INVALID_LENGTH, this, value );
				}
				return value;
			}
		};

		tfields = new Table.Field[] {
			  fld_cd, fld_classcd
			, createFD( MD, "name",					"HS_NAME",				"NAME",							DESC, 0, 128 )
			, createFD( OP, "description",			"HS_DESC",				"DESCRIPTION",					DESC, 0, 500 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( SYS_HS, table = createTable("SYS_HS", "HS", tfields, "UPGDATE = SYSDATE") );
		putQueryable( SYS_HS, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( INTEGER, "lowerCount"
					, "(SELECT COUNT(*) FROM SYS_HS SB WHERE SB.HS_CD LIKE HS.HS_CD || '%'"
							+" AND SB.CLASSCD = TO_CHAR(TO_NUMBER(HS.CLASSCD)+1) )", "LOWERLEVELCOUNT" )
			, new QueryableFieldImpl( STRING, "levelCode", fld_cd.getLevelCodeQuery(), "LEVELCODE" )
		}) );


		/***************************************************************************************************
		 *	SYS_COUNTRY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"COUNTRY_CD",			"CODE",							2, 2, CHARS )
			, createFD( MD, "name",					"COUNTRY_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( SYS_COUNTRY, table = createTable("SYS_COUNTRY", "CD", tfields) );


		/***************************************************************************************************
		 *	SYS_LANGUAGE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"LANG_CD",				"CODE",							2, 2, CHARS )
			, createFD( MD, "name",					"LANG_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( SYS_LANGUAGE, table = createTable("SYS_LANG", "CD", tfields) );


		/***************************************************************************************************
		 *	SYS_CURRENCY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"CURR_CD",				"CODE",							3, 3, CHARS )
			, createFD( MD, "symbol",				"CURR_SYMBOL",			"SYMBOL",						0, 40 )
			, createFD( MD, "name",					"CURR_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( SYS_CURRENCY, table = createTable("SYS_CURRENCY", "CD", tfields) );


		/***************************************************************************************************
		 *	SYS_UNIT
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"UNIT_CD",				"CODE",							3, 3, CHARS )
			, createFD( MD, "classCode",			"UNIT_CLASSCD",			"SYS_UNIT_CLASSCODE",			"SYS_UNITCLASS_", "LN,AR,WG,ST,TP,VL,ET" )
			, createFD( MD, "symbol",				"UNIT_SYMBOL",			"SYMBOL",						0, 40 )
			, createFD( MD, "name",					"UNIT_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( SYS_UNIT, table = createTable("SYS_UNIT", "CD", tfields) );


		/***************************************************************************************************
		 *	SYS_HANDLINGINFO
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"HANDINFO_CD",			"CODE",							3, 3, CHARS, true )
			, createFD( MD, "name",					"HANDINFO_NAME",		"NAME",							DESC, 0, 128 )
		};
		putTable( SYS_HANDLINGINFO, table = createTable("SYS_HANDLINGINFO", "CD", tfields) );


		/***************************************************************************************************
		 *	SYS_PACKAGETYPE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"PKGTYP_CD",			"CODE",							3, 3, CHARS, true )
			, createFD( MD, "name",					"PKGTYP_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( SYS_PACKAGETYPE, table = createTable("SYS_PKGTYPE", "CD", tfields) );


		/***************************************************************************************************
		 *	SYS_PACKAGEMATERIAL
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"PKGMTR_CD",			"CODE",							3, 3, CHARS, true )
			, createFD( MD, "name",					"PKGMTR_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( SYS_PACKAGEMATERIAL, table = createTable("SYS_PKGMATERIAL", "CD", tfields) );


		/***************************************************************************************************
		 *	SYS_WEATHER
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"WEATHER_CD",			"CODE",							2, 2, CHARS )
			, createFD( MD, "name",					"WEATHER_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( SYS_WEATHER, table = createTable("SYS_WEATHER", "CD", tfields) );


		/***************************************************************************************************
		 *	SYS_BANK
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"BANK_CD",				"CODE",							2, 2, CHARS )
			, createFD( MD, "name",					"BANK_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( SYS_BANK, table = createTable("SYS_BANK", "CD", tfields) );


		/***************************************************************************************************
		 *	SYS_POSTALCODE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( MD, "countryCode",			"COUNTRYCD",			"SYS_POSTAL_COUNTRYCD",				2, 2 )
			, createFD( MD, "code",					"CODE",					"SYS_POSTAL_CODE",					0, 10 )
			, createFD( MD, "state",				"STATE",				"SYS_POSTAL_STATE",					DESC, 0, 35 )
			, createFD( MD, "city",					"CITY",					"SYS_POSTAL_CITY",					DESC, 0, 35 )
			, createFD( OP, "street",				"STREET",				"SYS_POSTAL_STREET",				DESC, 0, 35 )
			, createFD( OP, "address",				"ADDRESS",				"SYS_POSTAL_ADDRESS",				DESC, 0, 70 )
			, createFD( OP, "addressNumberFrom",	"ADDR_FRNUM",			"SYS_POSTAL_ADDRESSNUMBER_FROM",	DESC, 0, 20 )
			, createFD( OP, "addressNumberTo",		"ADDR_TONUM",			"SYS_POSTAL_ADDRESSNUMBER_TO",		DESC, 0, 20 )
		};
		putTable( SYS_POSTALCODE, table = createTable("SYS_POSTCODE", "POST", tfields) );
		putQueryable( SYS_POSTALCODE, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( STRING, "postalCode", "SUBSTR(POST.CODE, 1, 3) || '-' || SUBSTR(POST.CODE, 4)", "SYS_POSTAL_POSTALCODE" )
		}) );


		/***************************************************************************************************
		 *	SYS_TIMEZONE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( MD, "code",					"TIMEZONE_CD",			"CODE",							0, 30 )
			, createFD( MD, "name",					"TIMEZONE_NAME",		"NAME",							DESC, 0, 128 )
			, createFD( OP, "description",			"TIMEZONE_DESC",		"DESCRIPTION",					DESC, 0, 128 )
			, createFD( OP, "offset",				"OFFSET",				"TIMEZONE_OFFSET",				0, 6 )
			, createFD( OP, "usingDaylightSaving",	"DAYLIGHTSAVING_IND",	"TIMEZONE_DAYLIGHTSAVING_IND",	"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "displaySequence",		"DISPLAY_SEQ",			"DISPLAYSEQUENCE",				INTEGER, 1, 999999 )
		};
		putTable( SYS_TIMEZONE, table = createTable("SYS_TIMEZONE", "TMZ", tfields) );


		/***************************************************************************************************
		 *	SYS_CODETYPE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"CODETYPE_CD",			"CODE",							3, 3, CHARS )
			, createFD( MD, "name",					"CODETYPE_NAME",		"NAME",							DESC, 0, 128 )
		};
		putTable( SYS_CODETYPE, table = createTable("SYS_CODETYPE", "CDT", tfields) );


		/***************************************************************************************************
		 *	SYS_CODE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"CODE_CD",				"CODE",							0, 10, CHARS, true )
			, createFD( MR, "codeTypeCode",			"CODETYPECD",			"SYS_CODE_CODETYPECODE",		3, 3 )
			, createFD( MD, "name",					"CODE_NAME",			"NAME",							DESC, 0, 128 )
			, createFD( OP, "displaySequence",		"DISPLAY_SEQ",			"SYS_CODE_DISPLAYSEQUENCE",		INTEGER, 1, 999999 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( SYS_CODE, table = createTable("SYS_CODE", "CD", tfields, "UPGDATE = SYSDATE") );
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

	static synchronized void loadCategory( int[] codeLengths ) {
		/****************************************************************************************************
		 *	SYS_CATEGORY
		****************************************************************************************************/
		String classCodeValue = "";
		for( int i = 1; i <= codeLengths.length; i++ )
			classCodeValue = ","+ i;

		Table table;
		Table.Field fld_classcd = createFD( MR, "classCode", "CLASSCD", "CLASSCODE", "PUB_CLASSCODE_", classCodeValue.substring(1) );
		HierarchyCodeField fld_cd = new HierarchyCodeField( createFD(PM, "code", "CATE_CD", "CODE", 0, 20, CHARS), fld_classcd, codeLengths );
		Table.Field[] tfields = new Table.Field[] {
			  fld_cd, fld_classcd
			, createFD( MD, "name",					"CATE_NAME",			"NAME",							DESC, 0, 128 )
			, createFD( OP, "description",			"CATE_DESC",			"DESCRIPTION",					DESC, 0, 500 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		schema.putTable( SYS_CATEGORY, table = createTable("SYS_CATE", "CATE", tfields, "UPGDATE = SYSDATE") );
		schema.putQueryable( SYS_CATEGORY, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( INTEGER, "lowerCount"
					, "(SELECT COUNT(*) FROM SYS_CATE SB WHERE SB.CATE_CD LIKE CATE.CATE_CD || '%'"
							+" AND SB.CLASSCD = TO_CHAR(TO_NUMBER(CATE.CLASSCD)+1) )", "LOWERLEVELCOUNT" )
			, new QueryableFieldImpl( STRING, "levelCode", fld_cd.getLevelCodeQuery(), "LEVELCODE" )
		}) );
	}

	public static QueryableField[] makeCategoryLevelQueryFields( String key, String joinFieldName ) {
		return makeCategoryLevelQueryFields( key, joinFieldName, null );
	}

	public static QueryableField[] makeCategoryLevelQueryFields( String key, String joinFieldName, Joinable joinable ) {
		String fieldAliasPrefix, nameFieldName;
		HierarchyCodeField codeField;

		if( SYS_CATEGORY.equals(key) ) {
			fieldAliasPrefix = "category";
			nameFieldName = "CATE_NAME";
			codeField = CategoryCode.getCodeField_static();
		} else if( SYS_ICATEGORY.equals(key) ) {
			fieldAliasPrefix = "iCategory";
			nameFieldName = "ICATE_NAME";
			codeField = ICategoryCode.getCodeField_static();
		} else
			throw new IllegalArgumentException( "illegal categoryKey '"+ key +"'" );
		if( codeField == null ) return null;

		int lastLevel = codeField.getLastLevel();
		String tableAlias = codeField.getTable().getTableAlias();
		String tableName = codeField.getTable().getTableName();
		String codeFieldName = codeField.getFieldName();

		QueryableField[] qfields = new QueryableField[ lastLevel * 2 + 1 ];

		int idx = 0;
		int prevlength = 0;
		if( joinable == null )
			joinable = new JoinableImpl( tableAlias, tableName, tableAlias +"."+ codeFieldName +"(+) = "+ joinFieldName );
		else
			joinable = new JoinableImpl( tableAlias, tableName, tableAlias +"."+ codeFieldName +"(+) = "+ joinFieldName, joinable );
		qfields[idx++] = new QueryableFieldImpl( DESC, fieldAliasPrefix +"Name", tableAlias +"."+ nameFieldName, joinable );
		for( int level = 1; level <= lastLevel; level++ ) {
			String codeFieldQuery = "SUBSTRB("+ joinFieldName +", "+ (prevlength+1) +", "+ (codeField.getLength(level) - prevlength) +")";

			qfields[idx++] = new QueryableFieldImpl( STRING, fieldAliasPrefix +"Code"+ level, codeFieldQuery );
			if( level == lastLevel )
				qfields[idx++] = new QueryableFieldImpl( DESC, fieldAliasPrefix +"Name"+ level, tableAlias +"."+ nameFieldName, joinable );
			else {
				String alias = tableAlias + level;
				codeFieldQuery = "SUBSTRB("+ joinFieldName +", 1, "+ codeField.getLength(level) +")";
				Joinable joinable_lvl = new JoinableImpl( alias, tableName, alias +"."+ codeFieldName +"(+) = "+ codeFieldQuery );

				qfields[idx++] = new QueryableFieldImpl( DESC, fieldAliasPrefix +"Name"+ level, alias +"."+ nameFieldName, joinable_lvl );
				prevlength = codeField.getLength( level );
			}
		}

		return qfields;
	}
}
