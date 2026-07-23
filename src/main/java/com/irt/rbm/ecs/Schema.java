/*
 *	File Name:	Schema.java
 *	Version:	2.2.1
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2008/03/31		2.2.1	오류수정. priceName, priceCurr 처리
 *										createPartyCategoryCodeField() 추가
 *										makeItemQueryableFields(): prefix가 null일 경우 iCategoryCode, iCategoryName, categoryCode, categoryName 추가
 *										makeItemQueryableFields(): itemCode Schema.DESC -> Scheam.STRING으로 변경
 *										makePartyQueryableFields(): prefix가 null일 경우 parentGln 추가
 *										makeTPAuthQueryableFields(): joinable 만드는 오류 수정
 *										ECS_ITEMTP: minimumPresStockQty, maximumPresStockQty 추가
 *										ECS_PARTY_LINK, ECS_PARTY_CONTACT, ECS_PARTY_ENV, ECS_PARTY_CATE: putQueryable() 변경
 *										ECS_DELIVERYCAL_DTL: inDateStore 추가
 *	stghr12		2007/11/30		2.2.0	Java 1.5 문법으로 변경
 *										DATE, DATETIME 구분
 *										ECS_PARTY, ECS_TRADEPARNTER, ECS_TRADEPARTNER_AUTH 수정
 *										ECS_DELIVERYCAL, ECS_ITEMTP: ORDER_WEEKINFO -> ORDER_WEEKDAY, ORDER_POLICY
 *										ECS_TRADEPARTNER: TPINFO_MNGAUTH -> TPINFO_AUTHVALUE1, TPINFO_AUTHVALUE2
 *										ECS_TRADEPARTNER: ORDER_WEEKINFO 삭제, parentBuyerGln/parentSellerGln을 OP -> OR 변경
 *										partyCode, buyerPartnerCode, sellerPartnerCode, itemCode에 CHARS 사용
 *										fieldKey 이름 변경: buyerPartyCode/sellerPartyCode -> buyerPartnerCode/sellerPartnerCode
 *										fieldKey 이름 변경: buyerPartyCode/sellerPartyCode -> buyerItemPartnerCode/sellerItemPartnerCode (ECS_ITEMTP)
 *										fieldKey 이름 변경: roundToInnerpackPercent -> roundToInnerPackPercent
 *										fieldKey 이름 변경: startAvailDateTime/endAvailDateTime -> startAvailDate/endAvailDate
 *										fieldKey 이름 변경: startAvailDateTime/endAvailDateTime -> tradeStartAvailDate/tradeEndAvailDate
 *										makeTPAuthQueryableFields() 추가
 *										makeItemQueryableFields(), makePartyQueryableFields(): descriptionKey 추가, Joinable과 분리
 *										FIELD_ECS_PARTY_RETAILGLN -> FIELD_RETAILGLN
 *										FIELD_ECS_PARTY_RETAILNAME -> FIELD_RETAILNAME
 *										FIELD_ECS_PARTY_STOREGLN -> FIELD_STOREGLN
 *										FIELD_ECS_PARTY_STORENAME -> FIELD_STORENAME
 *	stghr12		2007/04/30		2.1.1	field size 조절
 *										descriptionKey 변경: RBM_MNGKEY_MNGKEY -> RBM_MNGKEY_MANAGEKEY
 *										makeItemLink() -> makeItemQueryableFields(), makePartyLink() -> makePartyQueryableFields()
 *										com.irt.rbm.sys.Schema.makeCategoryLevelQueryFields() 활용
 *										ECS_ITEM_INFO: manufGln 추가
 *										ECS_ITEM_MASTER: consumerAvailDateTime -> consumerAvailDate
 *										ECS_ITEM_FILE, ECS_ITEM_FILE_TMP: ECS_ITEM_FILE_BFILE, ECS_ITEM_FILE_BLOB로 변경
 *										ECS_RETAIL, ECS_STORE 수정
 *	stghr12		2006/12/01		2.1.0	ConditionalQueryable 변경사항 적용
 *										checkMessageKey() 삭제
 *										makeItemLink(), makePartyLink(): 사용하지 않는 descriptionKey 삭제
 *	stghr12		2006/07/07		2.0.1	rangeType 추가
 *	stghr12		2006/02/28		2.0.0	create
 *
**/

package com.irt.rbm.ecs;

import com.irt.data.Condition;
import com.irt.sql.*;

/**
 *
 */
public class Schema extends com.irt.sql.Schema {
	public final static String ECS_FILECLASS			= "FileClass";
	public final static String ECS_PRICETYPE			= "PriceType";
	public final static String ECS_SEASON				= "Season";
	public final static String ECS_PROMOTIONTYPE		= "PromotionType";
	public final static String ECS_ICATE_NEWTERM		= "iCategoryNewTerm";
	public final static String ECS_CATE_NEWTERM			= "CategoryNewTerm";
	public final static String ECS_ORIGIN				= "Origin";
	public final static String ECS_ST_AREA				= "StoreAttrArea";
	public final static String ECS_ST_STATUS			= "StoreAttrStatus";
	public final static String ECS_ST_COMAREA			= "StoreAttrComarea";
	public final static String ECS_ST_SCALE				= "StoreAttrScale";
	public final static String ECS_ST_FACTOR1			= "StoreAttrFactor1";
	public final static String ECS_ST_FACTOR2			= "StoreAttrFactor2";

	public final static String ECS_PARTY				= "Party";
	public final static String ECS_PARTY_LINK			= "PartyLink";
	public final static String ECS_PARTY_CONTACT		= "PartyContact";
	public final static String ECS_PARTY_ENV			= "PartyEnvironment";
	public final static String ECS_PARTY_CATE			= "PartyCategory";
	public final static String ECS_PARTY_OPER			= "PartyOperation";
	public final static String ECS_PARTY_OPERDAY		= "PartyOperationDay";
	public final static String ECS_TRADEPARTNER			= "TradePartner";
	public final static String ECS_TRADEPARTNER_AUTH	= "TradePartnerAuth";
	public final static String ECS_TRADEPARTNER_INFO	= "TradePartnerInfo";
	public final static String ECS_TRADEPARTNER_SELLERCODE	= "TradePartnerSellerCode";
	public final static String ECS_RETAIL				= "Retail";
	public final static String ECS_STORE				= "Store";
	public final static String ECS_DELIVERYCAL			= "DeliveryCalendar";
	public final static String ECS_DELIVERYCAL_DTL		= "DeliveryCalendarDetail";

	public final static String ECS_ITEM					= "Item";
	public final static String ECS_ITEM_INFO			= "ItemInfo";
	public final static String ECS_ITEM_MASTER			= "ItemMaster";
	public final static String ECS_ITEM_LINK			= "ItemLink";
	public final static String ECS_ITEM_MANUFGLN		= "ItemGlnManuf";
	public final static String ECS_ITEM_PRIVATEGLN		= "ItemGlnPrivate";
	public final static String ECS_ITEM_PRICE			= "ItemPrice";
	public final static String ECS_ITEM_ORIGIN			= "ItemOrigin";
	public final static String ECS_ITEM_DESCRIPTION		= "ItemDescription";
	public final static String ECS_ITEM_HIERARCHY		= "ItemHierarchy";
	public final static String ECS_ITEM_MEASURE			= "ItemMeasure";
	public final static String ECS_ITEM_MEASUREUNIT		= "ItemMeasureUnit";
	public final static String ECS_ITEM_PACKAGING		= "ItemPackaging";
	public final static String ECS_ITEM_HANDLING		= "ItemHandling";
	public final static String ECS_ITEM_ORDERING		= "ItemOrdering";
	public final static String ECS_ITEM_SEASON			= "ItemSeason";
	public final static String ECS_ITEM_FASHION			= "ItemFashion";
	public final static String ECS_ITEM_ATTRIBUTE		= "ItemAttribute";
	public final static String ECS_ITEM_FILE			= "ItemFile";
	public final static String ECS_ITEM_FILE_BFILE		= "ItemFileBFILE";
	public final static String ECS_ITEM_FILE_BLOB		= "ItemFileBlob";

	public final static String ECS_ITEM_MNG				= "ItemManage";
	public final static String ECS_ITEMM				= "ItemMNG";					// 없음
	public final static String ECS_ITEMM_INFO			= "ItemInfoMNG";				// 없음
	public final static String ECS_ITEMM_LINK			= "ItemLinkMNG";				// 없음
	public final static String ECS_ITEMM_GLN			= "ItemGlnMNG";					// 없음
	public final static String ECS_ITEMM_ORIGIN			= "ItemOriginMNG";				// 없음
	public final static String ECS_ITEMM_DESC			= "ItemDescMNG";				// 없음
	public final static String ECS_ITEMM_HIERARCHY		= "ItemHierarchyMNG";			// 없음
	public final static String ECS_ITEMM_MEASURE		= "ItemMeasureMNG";				// 없음
	public final static String ECS_ITEMM_MEASUREU		= "ItemMeasureUnitMNG";			// 없음
	public final static String ECS_ITEMM_PACKAGING		= "ItemPackagingMNG";			// 없음
	public final static String ECS_ITEMM_HANDLING		= "ItemHandlingMNG";			// 없음
	public final static String ECS_ITEMM_ORDERING		= "ItemOrderingMNG";			// 없음
	public final static String ECS_ITEMM_SEASON			= "ItemSeasonMNG";				// 없음
	public final static String ECS_ITEMM_FASHION		= "ItemFashionMNG";				// 없음
	public final static String ECS_ITEMM_ATTRIBUTE		= "ItemAttributeMNG";			// 없음

	public final static String ECS_ITEMTP				= "TradeItem";
	public final static String ECS_ITEMTP_INFO			= "TradeItemInfo";
	public final static String ECS_ITEMTP_ORDERING		= "TradeItemOrdering";
	public final static String ECS_ITEMTP_PRICE			= "TradeItemPrice";
	public final static String ECS_ITEMTP_SELLERCODE	= "TradeItemSellerCode";

	public final static String ECS_ITEMTP_MNG			= "TradeItemManage";
	public final static String ECS_ITEMTPM				= "TradeItemMNG";				// 없음
	public final static String ECS_ITEMTPM_INFO			= "TradeItemInfoMNG";			// 없음
	public final static String ECS_ITEMTPM_ORDERING		= "TradeItemOrderingMNG";		// 없음

	private final static String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";

	private final static Schema schema = new Schema();

	Schema() {
		Double double_0 = new Double( 0.0 );
		Table table;
		Table.Field[] tfields;
		Table.Field fld_classcd;
		QueryableField[] qfields;
		HierarchyCodeField fld_cd;

		JoinableImpl tbl_CATE = new JoinableImpl( "{0}", "SYS_CATE",	"{0}.CATE_CD(+) = {1}.CATECD" );
		JoinableImpl tbl_ICATE = new JoinableImpl( "{0}", "SYS_ICATE",	"{0}.ICATE_CD(+) = {1}.ICATECD" );


		/***************************************************************************************************
		 *	ECS_FILECLASS
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"FILECLASS_CD",			"CODE",							3, 3, CHARS )
			, createFD( MD, "name",					"FILECLASS_NAME",		"NAME",							DESC, 0, 128 )
			, createFD( OP, "description",			"FILECLASS_DESC",		"DESCRIPTION",					DESC, 0, 500 )
			, createFD( MD, "type",					"TYPE",					"ECS_FILECLASS_TYPE",			"ECS_FILECLASS_TYPE_", "IMG,MOV,DOC" )
			, createFD( MD, "limitSize",			"LIMITSIZE",			"ECS_FILECLASS_LIMITSIZE",		INTEGER, 0, 99999999, EQU_NY )
			, createFD( OP, "displaySequence",		"DISPLAY_SEQ",			"ECS_FILECLASS_DISPLAYSEQUENCE",INTEGER, 0, 999999 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_FILECLASS, table = createTable("ECS_FILECLASS", "FLC", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_PRICETYPE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"PRICETYPE_CD",			"CODE",							3, 3, CHARS )
			, createFD( MD, "name",					"PRICETYPE_NAME",		"NAME",							DESC, 0, 128 )
			, createFD( MD, "useMethod",			"USEMETHOD",			"ECS_PRICETYPE_USEMETHOD",		"ECS_PRICETYPE_USEMETHOD_", "P,T,M" )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
		};
		putTable( ECS_PRICETYPE, table = createTable("ECS_PRICETYPE", "PRT", tfields) );


		/***************************************************************************************************
		 *	ECS_SEASON
		***************************************************************************************************/
		fld_classcd = createFD( MR, "classCode", "CLASSCD", "CLASSCODE", "ECS_SEASON_CLASSCODE_", "1,2" );
		fld_cd = new HierarchyCodeField( createFD(PM, "code", "SEASON_CD", "CODE", 0, 6, CHARS), fld_classcd, new int[] { 3, 6 } );
		tfields = new Table.Field[] {
			  fld_cd, fld_classcd
			, createFD( MD, "name",					"SEASON_NAME",			"NAME",							DESC, 0, 128 )
			, createFD( OP, "description",			"SEASON_DESC",			"DESCRIPTION",					DESC, 0, 500 )
			, createFD( OP, "startDate",			"STARTDATE",			"ECS_SEASON_STARTDATE",			DATE )
			, createFD( OP, "endDate",				"ENDDATE",				"ECS_SEASON_ENDDATE",			DATE )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_SEASON, table = createTable("ECS_SEASON", "SEA", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_SEASON, new QueryableImpl(table, new QueryableField[] {
			  new QueryableFieldImpl( INTEGER, "lowerCount"
					, "(SELECT COUNT(*) FROM ECS_SEASON SB WHERE SB.SEASON_CD LIKE SEA.SEASON_CD || '%'"
							+" AND SB.CLASSCD = TO_CHAR(TO_NUMBER(SEA.CLASSCD)+1) )", "LOWERLEVELCOUNT" )
			, new QueryableFieldImpl( STRING, "levelCode", fld_cd.getLevelCodeQuery(), "LEVELCODE" )
		}) );


		/***************************************************************************************************
		 *	ECS_PROMOTIONTYPE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"PMTTYPE_CD",			"CODE",							3, 3, CHARS )
			, createFD( MD, "name",					"PMTTYPE_NAME",			"NAME",							DESC, 0, 128 )
			, createFD( OP, "description",			"PMTTYPE_DESC",			"DESCRIPTION",					DESC, 0, 500 )
			, createFD( OP, "promotionClass",		"PMTTYPECLASS",			"ECS_PMTTYPE_PROMOTIONCLASS",	2, 2 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_PROMOTIONTYPE, table = createTable("ECS_PROMOTIONTYPE", "PMT", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ICATE_NEWTERM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "baseCategoryCode",		"ICATECD",				"APPLYING_CATEGORY",			0, 20 )
			, createFD( MD, "days",					"DAYS",					"ECS_CLASSNEWTERM_DAYS",		INTEGER, 0, 99999 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_ICATE_NEWTERM, table = createTable("ECS_ICATE_NEWTERM", "CNW", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ICATE_NEWTERM, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( DESC, "baseCategoryName", "ICATE.ICATE_NAME", "ICATEGORY", JoinableImpl.replaceAlias(tbl_ICATE, "ICATE", "CNW") )
		}) );


		/***************************************************************************************************
		 *	ECS_CATE_NEWTERM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "baseCategoryCode",		"CATECD",				"APPLYING_CATEGORY",			0, 20 )
			, createFD( MD, "days",					"DAYS",					"ECS_CLASSNEWTERM_DAYS",		INTEGER, 0, 99999 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_CATE_NEWTERM, table = createTable("ECS_CATE_NEWTERM", "CNW", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_CATE_NEWTERM, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( DESC, "baseCategoryName", "CATE.CATE_NAME", "CATEGORY", JoinableImpl.replaceAlias(tbl_CATE, "CATE", "CNW") )
		}) );


		/***************************************************************************************************
		 *	ECS_ORIGIN
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"ORIGIN_CD",			"CODE",							0, 6, CHARS )
			, createFD( MD, "name",					"ORIGIN_NAME",			"NAME",							DESC, 0, 40 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_ORIGIN, table = createTable("ECS_ORIGIN", "ORG", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ST_AREA
		***************************************************************************************************/
		fld_classcd = createFD( MR, "classCode", "CLASSCD", "CLASSCODE", "PUB_CLASSCODE_", "1,2,3" );
		fld_cd = new HierarchyCodeField( createFD(PM, "code", "AREA_CD", "CODE", 0, 9, CHARS), fld_classcd, new int[] { 3, 6, 9 } );
		tfields = new Table.Field[] {
			  fld_cd, fld_classcd
			, createFD( MD, "name",					"AREA_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( ECS_ST_AREA, table = createTable("ECS_ST_AREA", "STA", tfields) );
		putQueryable( ECS_ST_AREA, new QueryableImpl(table, new QueryableField[] {
			  new QueryableFieldImpl( INTEGER, "lowerCount"
					, "(SELECT COUNT(*) FROM ECS_ST_AREA SB WHERE SB.AREA_CD LIKE STA.AREA_CD || '%'"
							+" AND SB.CLASSCD = TO_CHAR(TO_NUMBER(STA.CLASSCD)+1) )", "LOWERLEVELCOUNT" )
			, new QueryableFieldImpl( STRING, "levelCode", fld_cd.getLevelCodeQuery(), "LEVELCODE" )
		}) );


		/***************************************************************************************************
		 *	ECS_ST_STATUS
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"STATUS_CD",			"CODE",							3, 3, CHARS )
			, createFD( MD, "name",					"STATUS_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( ECS_ST_STATUS, table = createTable("ECS_ST_STATUS", "STS", tfields) );


		/***************************************************************************************************
		 *	ECS_ST_COMAREA
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"COMAREA_CD",			"CODE",							3, 3, CHARS )
			, createFD( MD, "name",					"COMAREA_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( ECS_ST_COMAREA, table = createTable("ECS_ST_COMAREA", "STC", tfields) );


		/***************************************************************************************************
		 *	ECS_ST_SCALE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"SCALE_CD",				"CODE",							3, 3, CHARS )
			, createFD( MD, "name",					"SCALE_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( ECS_ST_SCALE, table = createTable("ECS_ST_SCALE", "STL", tfields) );


		/***************************************************************************************************
		 *	ECS_ST_FACTOR1
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"FACTOR1_CD",			"CODE",							3, 3, CHARS )
			, createFD( MD, "name",					"FACTOR1_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( ECS_ST_FACTOR1, table = createTable("ECS_ST_FACTOR1", "ST1", tfields) );


		/***************************************************************************************************
		 *	ECS_ST_FACTOR2
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "code",					"FACTOR2_CD",			"CODE",							3, 3, CHARS )
			, createFD( MD, "name",					"FACTOR2_NAME",			"NAME",							DESC, 0, 128 )
		};
		putTable( ECS_ST_FACTOR2, table = createTable("ECS_ST_FACTOR2", "ST2", tfields) );


		/***************************************************************************************************
		 *	ECS_PARTY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gln",					"GLN",					"GLN",							0, 13, "0123456789" )
			, createFD( MR, "glnType",				"GLNTYPE",				"ECS_PARTY_GLNTYPE",			"ECS_GLNTYPE_", "GLN,KAN,PRV" )
			, createFD( MR, "partyRole",			"PARTY_ROLE",			"ECS_PARTY_PARTYROLE",			CODE )
					.setValidValueList( "ECS_PARTYROLE_", "RTL,RWH,STR,SUP,SWH,PLT,CSP,PUB" )
			, createFD( OR, "parentGln",			"PARENT_GLN",			"ECS_PARTY_PARENTGLN",			0, 13 )
			, createFD( OP, "partyCode",			"PARTY_CD",				"ECS_PARTY_PARTYCODE",			0, 20, CHARS )
			, createFD( OP, "companyName",			"COMPANY_NAME",			"ECS_PARTY_COMPANYNAME",		DESC, 0, 128 )
			, createFD( OP, "locationType",			"LOCATION_TYPE",		"ECS_PARTY_LOCATIONTYPE",		DESC, 0, 50 )
			, createFD( MD, "locationName",			"LOCATION_NAME",		"ECS_PARTY_LOCATIONNAME",		DESC, 0, 128 )
			, createFD( OP, "locationShortName",	"LOCATION_SHORTNAME",	"ECS_PARTY_LOCATIONSHORTNAME",	DESC, 0, 50 )
			, createFD( OP, "locationDescription",	"LOCATION_DESC",		"ECS_PARTY_LOCATIONDESCRIPTION",DESC, 0, 128 )
			, createFD( OP, "searchString",			"SEARCHSTRING",			"ECS_PARTY_SEARCHSTRING",		DESC, 0, 300 )
			, createFD( OP, "languageCode",			"LANGCD",				"ECS_PARTY_LANGUAGE",			2, 2 )
			, createFD( OP, "currencyCode",			"CURRCD",				"ECS_PARTY_CURRENCY",			3, 3 )
			, createFD( OP, "countryCode",			"COUNTRYCD",			"ECS_PARTY_COUNTRY",			2, 2 )
			, createFD( OP, "state",				"STATE",				"ECS_PARTY_STATE",				DESC, 0, 35 )
			, createFD( OP, "city",					"CITY",					"ECS_PARTY_CITY",				DESC, 0, 35 )
			, createFD( OP, "streetAddress1",		"ADDRESS1",				"ECS_PARTY_STREETADDRESS1",		DESC, 0, 128 )
			, createFD( OP, "streetAddress2",		"ADDRESS2",				"ECS_PARTY_STREETADDRESS2",		DESC, 0, 128 )
			, createFD( OP, "addressTrim",			"ADDRESS_TRIM",			"ECS_PARTY_ADDRESSTRIM",		DESC, 0, 255 )
			, createFD( OP, "postalCode",			"POSTALCODE",			"ECS_PARTY_POSTALCODE",			0, 10 )
			, createFD( OP, "timeZone",				"TIMEZONE",				"TIMEZONE",						0, 30 )
			, createFD( OP, "owner",				"OWNER",				"ECS_PARTY_OWNER",				DESC, 0, 35 )
			, createFD( OP, "businessCond",			"BUSINESS_COND",		"ECS_PARTY_BUSINESSCOND",		DESC, 0, 128 )
			, createFD( OP, "businessItem",			"BUSINESS_ITEM",		"ECS_PARTY_BUSINESSITEM",		DESC, 0, 128 )
			, createFD( OP, "businessNumber",		"BUSINESS_NUMBER",		"ECS_PARTY_BUSINESSNUMBER",		0, 12 )
			, createFD( OP, "registeredNumber",		"REGISTERED_NUMBER",	"ECS_PARTY_REGISTEREDNUMBER",	0, 14 )
			, createFD( OP, "isAppointRequried",	"APPOINT_REQURIED_IND",	"ECS_PARTY_ISAPPOINTREQURIED",	"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "operatingDay",			"OPERATING_DAY",		"ECS_PARTY_OPERATINGDAY",		7, 7 )
			, createFD( OP, "operatingTime",		"OPERATING_TIME",		"ECS_PARTY_OPERATINGTIME",		8, 8 )
			, createFD( OP, "bankCode",				"BANKCD",				"ECS_PARTY_BANK",				2, 2 )
			, createFD( OP, "bankBranch",			"BANK_BRANCH",			"ECS_PARTY_BANKBRANCH",			DESC, 0, 70 )
			, createFD( OP, "accountName",			"ACCOUNTNAME",			"ECS_PARTY_ACCOUNTNAME",		DESC, 0, 35 )
			, createFD( OP, "accountNumber",		"ACCOUNTNUMBER",		"ECS_PARTY_ACCOUNTNUMBER",		0, 35 )
			, createFD( OP, "accountHolderName",	"ACCOUNTHOLDERNAME",	"ECS_PARTY_ACCOUNTHOLDERNAME",	DESC, 0, 35 )
			, createFD( OP, "ediAddress",			"EDI_ADDRESS",			"ECS_PARTY_EDIADDRESS",			0, 70 )
			, createFD( OP, "defaultLeadTime",		"DEFAULT_LEADTIME",		"ECS_PARTY_DEFAULTLEADTIME",	INTEGER, 0, 999999 )
			, createFD( OP, "mallName",				"MALLNAME",				"ECS_PARTY_MALLNAME",			DESC, 0, 70 )
			, createFD( OP, "webSite",				"WEBSITE",				"ECS_PARTY_WEBSITE",			DESC, 0, 70 )
			, createFD( OP, "effectiveChangeDate",	"EFFECTIVE_CHGDATETIME", "ECS_PARTY_EFFECTIVECHANGEDATE", DATE )
			, createFD( OP, "startAvailDate",		"STARTAVAIL_DATETIME",	"ECS_PARTY_STARTAVAILDATE",		DATE )
			, createFD( OP, "endAvailDate",			"ENDAVAIL_DATETIME",	"ECS_PARTY_ENDAVAILDATE",		DATE )
			, createFD( MO, "status",				"STATUS",				"ECS_PARTY_STATUS",				"ECS_PARTY_STATUS_", "00,01" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_PARTY, table = createTable("ECS_PARTY", "PTY", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_PARTY, new QueryableImpl( getTable(ECS_PARTY) ) {
			{
				append( makePartyQueryableFields( "parent", "PPTY", "PPTY.GLN(+) = PTY.PARENT_GLN" ) );
				append( new QueryableField[] {
					  new QueryableFieldImpl( DESC, "languageName"
							, "( SELECT SB.LANG_NAME FROM SYS_LANG SB WHERE SB.LANG_CD = PTY.LANGCD )", "ECS_PARTY_LANGUAGE" )
					, new QueryableFieldImpl( DESC, "currencyName"
							, "( SELECT SB.CURR_NAME FROM SYS_CURRENCY SB WHERE SB.CURR_CD = PTY.CURRCD )", "ECS_PARTY_CURRENCY" )
					, new QueryableFieldImpl( DESC, "currencySymbol"
							, "( SELECT SB.CURR_SYMBOL FROM SYS_CURRENCY SB WHERE SB.CURR_CD = PTY.CURRCD )", "ECS_PARTY_CURRENCY" )
					, new QueryableFieldImpl( DESC, "countryName"
							, "( SELECT SB.COUNTRY_NAME FROM SYS_COUNTRY SB WHERE SB.COUNTRY_CD = PTY.COUNTRYCD )", "ECS_PARTY_COUNTRY" )
					, new QueryableFieldImpl( DESC, "bankName"
							, "( SELECT SB.BANK_NAME FROM SYS_BANK SB WHERE SB.BANK_CD = PTY.BANKCD )", "ECS_PARTY_BANK" )
					, new QueryableFieldImpl( DESC, "timeZoneName", "TMZ.TIMEZONE_NAME", "TIMEZONE"
							, new JoinableImpl( "TMZ", "SYS_TIMEZONE", "TMZ.TIMEZONE_CD(+) = PTY.TIMEZONE" ) )
					, new QueryableFieldImpl( INTEGER, "childCount", "( SELECT COUNT(*) FROM ECS_PARTY SB WHERE SB.PARENT_GLN = PTY.GLN )" )
					, new QueryableFieldImpl( INTEGER, "itemCount", "( SELECT COUNT(*) FROM ECS_ITEM SB WHERE SB.GLN = PTY.GLN )" )
					, new QueryableFieldImpl( INTEGER, "offdayCount", "( SELECT COUNT(*) FROM ECS_PARTY_OPER SB WHERE SB.GLN = PTY.GLN )" )
					, new QueryableFieldImpl( INTEGER, "availOffdayCount"
							, "( SELECT COUNT(*) FROM ECS_PARTY_OPER SB "
									+" WHERE ( SB.PTN_TYPE <> '0' OR SB.PTN_DATE >= TRUNC(pkSYSDate.fCurrentDate(PTY.TIMEZONE)) )"
									+" AND SB.GLN = PTY.GLN )" )
					, new QueryableFieldImpl( STRING, "postalCode1", "SUBSTRB( PTY.POSTALCODE, 1, 3)" )
					, new QueryableFieldImpl( STRING, "postalCode2", "SUBSTRB( PTY.POSTALCODE, 5, 3)" )
				} );
			}

			public boolean appendCondition( ConditionQueryBuffer querybuf ) {
				Object[] relatedGlns = querybuf.getConditionValues( "relatedGln" );
				if( relatedGlns == null ) return super.appendCondition( querybuf );

				Object valueObj = querybuf.getConditionValue( "relatedGln" + Condition.SUFFIX_TYPE );
				String type = ( valueObj instanceof String ? (String)valueObj : Condition.CONDTYPE_EQUALS );

				super.appendCondition( querybuf );
				if( Condition.CONDTYPE_NOTEQUALS.equals(type) ) {
					for( int i = 0; i < relatedGlns.length; i++ ) {
						querybuf.appendCondition( "PTY.GLN <> ?", relatedGlns[i] );
						querybuf.appendCondition( "( PTY.PARENT_GLN <> ? OR PTY.PARENT_GLN IS NULL )", relatedGlns[i] );
					}
				} else {
					StringBuffer sbuf = new StringBuffer();
					for( int i = 0; i < relatedGlns.length; i++ ) {
						sbuf.append( " OR PTY.GLN = ? OR PTY.PARENT_GLN" );
						querybuf.addBindVariable( QueryBuffer.COND_BINDVAR, relatedGlns[i] );
						querybuf.addBindVariable( QueryBuffer.COND_BINDVAR, relatedGlns[i] );
					}
					querybuf.appendCondition( "("+ sbuf.substring(4) +")" );
				}

				return true;
			}
		} );


		/***************************************************************************************************
		 *	ECS_PARTY_LINK
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "linkClass",			"LINKCLASS",			"ECS_PARTY_LINKCLASS",			"ECS_PARTY_LINKCLASS_", "LW" )
			, createFD( PM, "linkGln",				"LINKGLN",				"ECS_PARTY_LINKGLN",			0, 13 )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"CREATEUSERID",			"CREATEUSERID",					0, 30 )
		};
		putTable( ECS_PARTY_LINK, table = createTable("ECS_PARTY_LINK", "PLK", tfields) );
		putQueryable( ECS_PARTY_LINK, new QueryableImpl(table) {{
			append( makePartyQueryableFields( null, "PTY", "PTY.GLN(+) = PLK.LINKGLN" ) );
			append( makePartyQueryableFields( "link", "LPTY", "LPTY.GLN(+) = PLK.LINKGLN" ) );
		}} );


		/***************************************************************************************************
		 *	ECS_PARTY_CONTACT
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "contactName",			"CONTACT_NAME",			"ECS_PARTY_CONTACTNAME",		DESC, 0, 70 )
			, createFD( OP, "contactDescription",	"CONTACT_DESC",			"ECS_PARTY_CONTACTDESCRIPTION",	DESC, 0, 500 )
			, createFD( OP, "email",				"EMAIL",				"ECS_PARTY_EMAIL",				DESC, 0, 70 )
			, createFD( OP, "mobilephone",			"MOBILE",				"ECS_PARTY_MOBILEPHONE",		0, 25 )
			, createFD( OP, "fax",					"TELEFAX",				"ECS_PARTY_FAX",				0, 25 )
			, createFD( OP, "telephone",			"TELEPHONE",			"ECS_PARTY_TELEPHONE",			0, 25 )
			, createFD( OP, "webSite",				"WEBSITE",				"ECS_PARTY_WEBSITE",			DESC, 0, 70 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_PARTY_CONTACT, table = createTable("ECS_PARTY_CONTACT", "PCT", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_PARTY_CONTACT, new QueryableImpl(table, makePartyQueryableFields(null, "PTY", "PTY.GLN(+) = PCT.GLN")) );


		/***************************************************************************************************
		 *	ECS_PARTY_ENV
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( OP, "categoryLevel",		"CATE_LEVEL",			"ECS_PARTYENV_CATEGORYLEVEL",	"PUB_CLASSCODE_", "0,1,2,3,4,5,6,7,8,9" )
			, createFD( OP, "categoryLength",		"CATE_LEN",				"ECS_PARTYENV_CATEGORYLENGTH",	0, 30, "0123456789" )
			, createFD( OP, "manageUserId",			"MNGUSERID",			"ECS_PARTYENV_MANAGEUSERID",	0, 30 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_PARTY_ENV, table = createTable("ECS_PARTY_ENV", "PEV", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_PARTY_ENV, new QueryableImpl(table, makePartyQueryableFields(null, "PTY", "PTY.GLN(+) = PEV.GLN")) );


		/***************************************************************************************************
		 *	ECS_PARTY_CATE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "code",					"CATE_CD",				"ECS_PARTYCATE_CODE",			0, 20 )
			, createFD( MR, "classCode",			"CLASSCD",				"ECS_PARTYCATE_CLASSCODE",		"PUB_CLASSCODE_", "1,2,3,4,5,6,7,8,9" )
			, createFD( MD, "name",					"CATE_NAME",			"ECS_PARTYCATE_NAME",			DESC, 0, 128 )
			, createFD( OP, "description",			"CATE_DESC",			"ECS_PARTYCATE_DESCRIPTION",	DESC, 0, 500 )
			, createFD( OP, "manageUserId",			"MNGUSERID",			"ECS_PARTYCATE_MANAGEUSERID",	0, 30 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_PARTY_CATE, table = createTable("ECS_PARTY_CATE", "PCA", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_PARTY_CATE, new QueryableImpl(table, makePartyQueryableFields(null, "PTY", "PTY.GLN(+) = PCA.GLN")) );


		/***************************************************************************************************
		 *	ECS_PARTY_OPER
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "patternType",			"PTN_TYPE",				"SCHEDULE_PATTERN_TYPE",		"PUB_SCHEDULEPTN_TYPE_", "0,1,2,3,4,5" )
			, createFD( PM, "patternIndex",			"PTN_INDEX",			"SCHEDULE_PATTERN_INDEX",		INTEGER, 0, 99 )
			, createFD( PM, "patternDate",			"PTN_DATE",				"SCHEDULE_PATTERN_DATE",		DATE )
			, createFD( OP, "name",					"OPER_NAME",			"ECS_PARTYOPER_NAME",			DESC, 0, 128 )
			, createFD( OP, "existOrder",			"ORDER_IND",			"ECS_PARTYOPER_EXISTORDER",			"ECS_PARTYOPER_EXIST_", "D,Y,N" )
			, createFD( OP, "existReceiveOrder",	"RCVORDER_IND",			"ECS_PARTYOPER_EXISTRECEIVEORDER",	"ECS_PARTYOPER_EXIST_", "D,Y,N" )
			, createFD( OP, "existSales",			"SALES_IND",			"ECS_PARTYOPER_EXISTSALES",			"ECS_PARTYOPER_EXIST_", "D,Y,N" )
			, createFD( OP, "existWarehousing",		"WAREHOUSING_IND",		"ECS_PARTYOPER_EXISTWAREHOUSING",	"ECS_PARTYOPER_EXIST_", "D,Y,N" )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_PARTY_OPER, table = createTable("ECS_PARTY_OPER", "POP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_PARTY_OPER, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( CODE, "availableInd"
					, "(CASE WHEN POP.PTN_TYPE <> '0' OR POP.PTN_DATE >= TRUNC(pkECSPartyOper.fCurrentDate(POP.GLN)) THEN 'Y' ELSE 'N' END)"
					, "ECS_PARTYOPER_AVAILABLEIND" )
		}) );


		/***************************************************************************************************
		 *	ECS_PARTY_OPERDAY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "operDay",				"OPERDAY",				"ECS_PARTYOPER_OPERDAY",		DATE )
			, createFD( RD, "name",					"OPER_NAME",			"ECS_PARTYOPER_NAME",			DESC, 0, 128)
			, createFD( RD, "existOrder",			"ORDER_IND",			"ECS_PARTYOPER_EXISTORDER",			"ECS_PARTYOPER_EXIST_", "D,Y,N" )
			, createFD( RD, "existReceiveOrder",	"RCVORDER_IND",			"ECS_PARTYOPER_EXISTRECEIVEORDER",	"ECS_PARTYOPER_EXIST_", "D,Y,N" )
			, createFD( RD, "existSales",			"SALES_IND",			"ECS_PARTYOPER_EXISTSALES",			"ECS_PARTYOPER_EXIST_", "D,Y,N" )
			, createFD( RD, "existWarehousing",		"WAREHOUSING_IND",		"ECS_PARTYOPER_EXISTWAREHOUSING",	"ECS_PARTYOPER_EXIST_", "D,Y,N" )
		};
		putTable( ECS_PARTY_OPERDAY, table = createTable("ECS_PARTY_OPERDAY", "POPD", tfields) );


		/***************************************************************************************************
		 *	ECS_TRADEPARTNER
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( PM, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( MR, "tradeInfoType",		"TPINFO_TYPE",			"ECS_TP_TRADEINFOTYPE",			"ECS_TP_INFOTYPE_", "TP,LW" )
			, createFD( OP, "tradeInfoAuthValue1",	"TPINFO_AUTHVALUE1",	"ECS_TP_TRADEINFOVALUE1",		0, 20 )
			, createFD( OP, "tradeInfoAuthValue2",	"TPINFO_AUTHVALUE2",	"ECS_TP_TRADEINFOVALUE2",		0, 20 )
			, createFD( OP, "supplierGln",			"SUPPLIERGLN",			"ECS_TP_SUPPLIERGLN",			0, 13 )
			, createFD( OP, "dcGln",				"DC_GLN",				"ECS_TP_DCGLN",					0, 13 )
			, createFD( OR, "parentBuyerGln",		"PARENT_BUYERGLN",		"ECS_TP_PARENTBUYERGLN",		0, 13 )
			, createFD( OR, "parentSellerGln",		"PARENT_SELLERGLN",		"ECS_TP_PARENTSELLERGLN",		0, 13 )
			, createFD( OP, "buyerUserId",			"BUYER_USERID",			"ECS_TP_BUYERUSERID",			0, 30 )
			, createFD( OP, "buyerContactName",		"BUYER_CONTACTNAME",	"ECS_TP_BUYERCONTACTNAME",		DESC, 0, 70 )
			, createFD( OP, "buyerPartnerCode",		"BUYER_PARTYCD",		"ECS_TP_BUYERPARTNERCODE",		0, 25, CHARS )
			, createFD( OP, "sellerUserId",			"SELLER_USERID",		"ECS_TP_SELLERUSERID",			0, 30 )
			, createFD( OP, "sellerContactName",	"SELLER_CONTACTNAME",	"ECS_TP_SELLERCONTACTNAME",		DESC, 0, 70 )
			, createFD( OP, "sellerPartnerCode",	"SELLER_PARTYCD",		"ECS_TP_SELLERPARTNERCODE",		0, 25, CHARS )
			, createFD( OP, "effectiveChangeDate",	"EFFECTIVE_CHGDATETIME", "ECS_TP_EFFECTIVECHANGEDATE",	DATE )
			, createFD( OP, "tradeStartAvailDate",	"STARTAVAIL_DATETIME",	"ECS_TP_STARTAVAILDATE",		DATE )
			, createFD( OP, "tradeEndAvailDate",	"ENDAVAIL_DATETIME",	"ECS_TP_ENDAVAILDATE",			DATE )
			, createFD( MO, "tradeStatus",			"STATUS",				"ECS_TP_STATUS",				"ECS_TP_STATUS_", "00,99" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_TRADEPARTNER, table = createTable("ECS_TRADEPARTNER", "TP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_TRADEPARTNER, new QueryableImpl(table) {
			{
				append( makePartyQueryableFields( "buyer", "PBY", "PBY.GLN(+) = TP.BUYERGLN" ) );
				append( makePartyQueryableFields( "seller", "PSL", "PSL.GLN(+) = TP.SELLERGLN" ) );
				append( makePartyQueryableFields( "parentBuyer", "PPBY", "PPBY.GLN(+) = TP.PARENT_BUYERGLN" ) );
				append( makePartyQueryableFields( "parentSeller", "PPSL", "PPSL.GLN(+) = TP.PARENT_SELLERGLN" ) );
				append( makePartyQueryableFields( "supplier", "PSP", "PSP.GLN(+) = TP.SUPPLIERGLN" ) );
				append( makePartyQueryableFields( "dc", "PDC", "PDC.GLN(+) = TP.DC_GLN" ) );
				append( makeTPAuthQueryableFields( "TP", "TP" ) );
				append( new QueryableField[] {
					  new QueryableFieldImpl( STRING, "buyerUserName", "UBY.USER_NAME", "ECS_TP_BUYERUSERNAME"
							, new JoinableImpl( "UBY", "USR_USER", "UBY.UNIQID(+) = TP.BUYER_USERID" ) )
					, new QueryableFieldImpl( STRING, "sellerUserName", "USL.USER_NAME", "ECS_TP_SELLERUSERNAME"
							, new JoinableImpl( "USL", "USR_USER", "USL.UNIQID(+) = TP.SELLER_USERID" ) )
					, new QueryableFieldImpl(INTEGER, "sellerCodeCount"
							, "( SELECT COUNT(*) FROM ECS_TRADEPARTNER_SLCODE SB WHERE SB.BUYERGLN = TP.BUYERGLN AND SB.SELLERGLN = TP.SELLERGLN )"
							, "ECS_TP_SELLERCODECOUNT" )
					, new QueryableFieldImpl( INTEGER, "tradeLocationCount"
							, "( SELECT COUNT(*) FROM ECS_TRADEPARTNER SB"
									+" WHERE SB.PARENT_BUYERGLN = TP.BUYERGLN AND SB.PARENT_SELLERGLN = TP.SELLERGLN )"
							, "ECS_TP_TRADELOCATIONCOUNT_ALL" )
					, new QueryableFieldImpl( INTEGER, "tradeLocationCount00"
							, "( SELECT COUNT(*) FROM ECS_TRADEPARTNER SB"
									+" WHERE SB.PARENT_BUYERGLN = TP.BUYERGLN AND SB.PARENT_SELLERGLN = TP.SELLERGLN AND SB.STATUS = '00' )"
							, "ECS_TP_TRADELOCATIONCOUNT" )
					, new QueryableFieldImpl( INTEGER, "tradeItemCount"
							, "( SELECT COUNT(*) FROM ECS_ITEMTP SB WHERE SB.BUYERGLN = TP.BUYERGLN AND SB.SELLERGLN = TP.SELLERGLN )"
							, "ECS_TP_TRADEITEMCOUNT_ALL" )
					, new QueryableFieldImpl( INTEGER, "tradeItemCount00"
							, "( SELECT COUNT(*) FROM ECS_ITEMTP SB WHERE SB.BUYERGLN = TP.BUYERGLN AND SB.SELLERGLN = TP.SELLERGLN"
									+" AND SB.STATUS = '00' )"
							, "ECS_TP_TRADEITEMCOUNT" )
					, new QueryableFieldImpl( INTEGER, "tradeItemCountNull"
							, "DECODE( TP.TPINFO_TYPE, 'TP',"
								+ "( SELECT COUNT(*) FROM ECS_ITEM ITM, ECS_ITEMTP ITP"
									+" WHERE ITP.BUYERGLN(+) = TP.BUYERGLN AND ITP.SELLERGLN(+) = TP.SELLERGLN AND ITP.GTIN(+) = ITM.GTIN"
									+" AND ITM.GLN = TP.SELLERGLN AND ITP.GTIN IS NULL )"
								+ ", 'LW', "
								+ "( SELECT COUNT(*) FROM ECS_ITEMTP ITP, ECS_ITEMTP CITP"
									+" WHERE ITP.BUYERGLN = TP.PARENT_BUYERGLN AND ITP.SELLERGLN = TP.PARENT_SELLERGLN"
									+" AND CITP.BUYERGLN(+) = TP.BUYERGLN AND CITP.SELLERGLN(+) = TP.SELLERGLN AND CITP.GTIN(+) = ITP.GTIN"
									+" AND CITP.GTIN IS NULL )"
								+ ")"
							, "ECS_TP_TRADEITEMCOUNT_NULL" )
				} );
			}

			public boolean appendCondition( ConditionQueryBuffer querybuf ) {
				boolean hasCondition = super.appendCondition( querybuf );

				if( querybuf.findConditionKey( "tradePartner", new String[] { "TP.BUYERGLN", "TP.SELLERGLN" } ) > 0 ) hasCondition = true;

				Object[] relatedGlns = querybuf.getConditionValues( "relatedTPGln" );
				if( relatedGlns != null ) {
					hasCondition = true;

					StringBuffer sbuf = new StringBuffer();
					for( int i = 0; i < relatedGlns.length; i++ ) {
						sbuf.append( " OR TP.BUYERGLN = ? OR TP.SELLERGLN = ? OR TP.PARENT_BUYERGLN = ? OR TP.PARENT_SELLERGLN = ?" );
						querybuf.addBindVariable( QueryBuffer.COND_BINDVAR
								, new Object[] { relatedGlns[i], relatedGlns[i], relatedGlns[i], relatedGlns[i] } );
					}
					querybuf.appendCondition( "("+ sbuf.substring(4) +")" );
				}

				return hasCondition;
			}
		} );


		/***************************************************************************************************
		 *	ECS_TRADEPARTNER_AUTH
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( PM, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( PM, "authUserId",			"USERID",				"ECS_TPAUTH_AUTHUSERID",		0, 30 )
			, createFD( MD, "manageType",			"MNGTYPE",				"ECS_TPAUTH_MANAGETYPE",		"ECS_TPAUTH_MANAGETYPE_", "A,P,S" )
			, createFD( OP, "authValue1",			"AUTHVALUE1",			"ECS_TPAUTH_AUTHVALUE1",		0, 20, "VMX" )
			, createFD( OP, "authValue2",			"AUTHVALUE2",			"ECS_TPAUTH_AUTHVALUE2",		0, 20, "VMX" )
			, createFD( RD, "tradeInfoType",		"TPINFO_TYPE",			"ECS_TP_TRADEINFOTYPE",			"ECS_TP_INFOTYPE_", "TP,LW" )
			, createFD( RD, "parentBuyerGln",		"PARENT_BUYERGLN",		"ECS_TP_PARENTBUYERGLN",		0, 13 )
			, createFD( RD, "parentSellerGln",		"PARENT_SELLERGLN",		"ECS_TP_PARENTSELLERGLN",		0, 13 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_TRADEPARTNER_AUTH, table = createTable("ECS_TRADEPARTNER_AUTH", "TPA", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_TRADEPARTNER_AUTH, new QueryableImpl(table) {{
			Joinable tbl_USR = new JoinableImpl( "USR", "USR_USER", "USR.UNIQID(+) = TPA.USERID" );

			append( makePartyQueryableFields( "buyer", "PBY", "PBY.GLN(+) = TPA.BUYERGLN" ) );
			append( makePartyQueryableFields( "seller", "PSL", "PSL.GLN(+) = TPA.SELLERGLN" ) );
			append( new QueryableField[] {
				  createQFD( T, "authUserUserId",		"USR.USER_ID",						"ECS_TPAUTH_AUTHUSERID",			tbl_USR )
				, createQFD( T, "authUserName",			"USR.USER_NAME",					"ECS_TPAUTH_AUTHUSERNAME",			tbl_USR )
				, createQFD( T, "authUserClass",		"USR.USERCLASS",					"ECS_TPAUTH_AUTHUSERCLASS",			tbl_USR )
				, createQFD( T, "authUserPartyId",		"USR.PARTYID",						"ECS_TPAUTH_AUTHUSERPARTYID",		tbl_USR )
			} );

			Joinable tbl_P = new JoinableImpl( "TPA_P", "ECS_TRADEPARTNER_AUTH"
					, "TPA_P.BUYERGLN(+) = TPA.PARENT_BUYERGLN AND TPA_P.SELLERGLN(+) = TPA.PARENT_SELLERGLN AND TPA_P.USERID(+) = TPA.USERID" );

			String prefixKey = "ECS_TPAUTH_VALUE_";
			String authValue1 = "NVL(TPA.AUTHVALUE1, DECODE(TPA_P.MNGTYPE, 'A', TPA_P.AUTHVALUE1, NULL))";
			String authValue2 = "NVL(TPA.AUTHVALUE2, DECODE(TPA_P.MNGTYPE, 'A', TPA_P.AUTHVALUE2, NULL))";
			append( new QueryableField[] {
				  createQFD( T, "applyingManageType"
						, "NVL2(TPA.AUTHVALUE1, TPA.MNGTYPE, DECODE(TPA_P.MNGTYPE, 'A', 'X', 'P', NVL2(TPA.MNGTYPE, 'X', NULL), TPA.MNGTYPE))"
						, "ECS_TPAUTH_MANAGETYPE", "ECS_TPAUTH_MANAGETYPE_" )
				, createQFD( T, "applyingAuthValue1",		authValue1,							"ECS_TPAUTH_AUTHVALUE1" )
				, createQFD( T, "applyingAuthValue2",		authValue2,							"ECS_TPAUTH_AUTHVALUE2" )
				, createQFD( T, "authValue1TP",				"SUBSTRB("+ authValue1 +", 1, 1)",	"ECS_TPAUTH_AUTHVALUE1_TP",			prefixKey, tbl_P )
				, createQFD( T, "authValue1Operday",		"SUBSTRB("+ authValue1 +", 2, 1)",	"ECS_TPAUTH_AUTHVALUE1_OPERDAY",	prefixKey, tbl_P )
				, createQFD( T, "authValue1Item",			"SUBSTRB("+ authValue1 +", 3, 1)",	"ECS_TPAUTH_AUTHVALUE1_ITEM",		prefixKey, tbl_P )
				, createQFD( T, "authValue2Scenario",		"SUBSTRB("+ authValue2 +", 1, 1)",	"ECS_TPAUTH_AUTHVALUE2_SCENARIO",	prefixKey, tbl_P )
				, createQFD( T, "authValue2Item",			"SUBSTRB("+ authValue2 +", 2, 1)",	"ECS_TPAUTH_AUTHVALUE2_ITEM",		prefixKey, tbl_P )
				, createQFD( T, "authValue2SFC",			"SUBSTRB("+ authValue2 +", 3, 1)",	"ECS_TPAUTH_AUTHVALUE2_SFC",		prefixKey, tbl_P )
				, createQFD( T, "authValue2OFC",			"SUBSTRB("+ authValue2 +", 4, 1)",	"ECS_TPAUTH_AUTHVALUE2_OFC",		prefixKey, tbl_P )
				, createQFD( T, "authValue2Event",			"SUBSTRB("+ authValue2 +", 5, 1)",	"ECS_TPAUTH_AUTHVALUE2_EVENT",		prefixKey, tbl_P )
				, createQFD( T, "authValue2Inventory",		"SUBSTRB("+ authValue2 +", 6, 1)",	"ECS_TPAUTH_AUTHVALUE2_INVENTORY",	prefixKey, tbl_P )
				, createQFD( T, "authValue2Order",			"SUBSTRB("+ authValue2 +", 7, 1)",	"ECS_TPAUTH_AUTHVALUE2_ORDER",		prefixKey, tbl_P )
				, createQFD( T, "authValue2OrderManual",	"SUBSTRB("+ authValue2 +", 8, 1)",	"ECS_TPAUTH_AUTHVALUE2_ORDERMANUAL",prefixKey, tbl_P )
				, createQFD( T, "authValue2KPI",			"SUBSTRB("+ authValue2 +", 9, 1)",	"ECS_TPAUTH_AUTHVALUE2_KPI",		prefixKey, tbl_P )
			} );
		}} );


		/***************************************************************************************************
		 *	ECS_TRADEPARTNER_INFO
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( PM, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( OP, "buyerUNB",				"BUYERUNB",				"ECS_TP_BUYERUNB",				0, 70 )
			, createFD( OP, "sellerUNB",			"SELLERUNB",			"ECS_TP_SELLERUNB",				0, 70 )
			, createFD( OP, "orderNumberRule",		"ORDER_NUMBER_RULE",	"ECS_TP_ORDERNUMBERRULE",		0, 10 )
			, createFD( OP, "orderSortingRule",		"ORDER_SORTING_RULE",	"ECS_TP_ORDERSORTINGRULE",		CODE )
					.setValidValueList( "ECS_TP_ORDER_SORTING_RULE_", "NONE,GTIN,ITEMCODE,RETAILCODE" )
			, createFD( OP, "bankCode",				"BANKCD",				"ECS_TP_BANK",					2, 2 )
			, createFD( OP, "bankBranch",			"BANK_BRANCH",			"ECS_TP_BANKBRANCH",			0, 70 )
			, createFD( OP, "accountName",			"ACCOUNTNAME",			"ECS_TP_ACCOUNTNAME",			DESC, 0, 35 )
			, createFD( OP, "accountNumber",		"ACCOUNTNUMBER",		"ECS_TP_ACCOUNTNUMBER",			0, 35 )
			, createFD( OP, "accountHolderName",	"ACCOUNTHOLDERNAME",	"ECS_TP_ACCOUNTHOLDERNAME",		DESC, 0, 35 )
			, createFD( OP, "freightTermsCode",		"FREIGHT_TERMS",		"ECS_TP_FREIGHTTERMS",			0, 10 )
			, createFD( OP, "shipMethodCode",		"SHIP_METHOD",			"ECS_TP_SHIPMETHOD",			0, 10 )
			, createFD( OP, "paymentTermsCode",		"PAYMENT_TERMS",		"ECS_TP_PAYMENTTERMS",			0, 10 )
			, createFD( OP, "paymentMethodCode",	"PAYMENT_METHOD",		"ECS_TP_PAYMENTMETHOD",			0, 10 )
			, createFD( OP, "isReturnAllow",		"RET_ALLOW_IND",		"ECS_TP_ISRETURNALLOW",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "returnMinimumMoney",	"RET_MINMONEY",			"ECS_TP_RETURNMINIMUMMONEY",	DOUBLE, double_0, null )
			, createFD( OP, "returnCourier",		"RET_COURIER",			"ECS_TP_RETURNCOURIER",			DESC, 0, 70 )
			, createFD( OP, "handlingPercentage",	"HANDLING_PCT",			"ECS_TP_HANDLINGPERCENTAGE",	DOUBLE, double_0, null )
			, createFD( OP, "isPreMark",			"PRE_MARK_IND",			"ECS_TP_ISPREMARK",				"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isBackOrder",			"BACKORDER_IND",		"ECS_TP_ISBACKORDER",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "deliveryPolicyCode",	"DELIVERY_POLICY",		"ECS_TP_DELIVERYPOLICY",		0, 10 )
			, createFD( OP, "inTime",				"INTIME",				"ECS_TP_INTIME",				0, 10 )
			, createFD( OP, "roundingRulesDescription",		"ROUNDINGRULES_DESC",		"ECS_TP_ROUNDINGRULESDESCRIPTION",		DESC, 0, 80 )
			, createFD( OP, "safetyStockRulesDescription",	"SAFETYSTOCKRULES_DESC",	"ECS_TP_SAFETYSTOCKRULESDESCRIPTION",	DESC, 0, 80 )
			, createFD( OP, "transportstrategyDescription",	"TRANSPORTSTRATEGY_DESC",	"ECS_TP_TRANSPORTSTRATEGYDESCRIPTION",	DESC, 0, 80 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_TRADEPARTNER_INFO, table = createTable("ECS_TRADEPARTNER_INFO", "TPI", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_TRADEPARTNER_INFO, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( DESC, "bankName"
					, "( SELECT SB.BANK_NAME FROM SYS_BANK SB WHERE SB.BANK_CD = TPI.BANKCD )", "ECS_TP_BANK" )
			, new QueryableFieldImpl( DESC, "freightTermsName"
					, "( SELECT SB.CODE_NAME FROM SYS_CODE SB WHERE SB.CODE_CD = TPI.FREIGHT_TERMS )", "ECS_TP_FREIGHTTERMS" )
			, new QueryableFieldImpl( DESC, "shipMethodName"
					, "( SELECT SB.CODE_NAME FROM SYS_CODE SB WHERE SB.CODE_CD = TPI.SHIP_METHOD )", "ECS_TP_SHIPMETHOD" )
			, new QueryableFieldImpl( DESC, "paymentTermsName"
					, "( SELECT SB.CODE_NAME FROM SYS_CODE SB WHERE SB.CODE_CD = TPI.PAYMENT_TERMS )", "ECS_TP_PAYMENTTERMS" )
			, new QueryableFieldImpl( DESC, "paymentMethodName"
					, "( SELECT SB.CODE_NAME FROM SYS_CODE SB WHERE SB.CODE_CD = TPI.PAYMENT_METHOD )", "ECS_TP_PAYMENTMETHOD" )
			, new QueryableFieldImpl( DESC, "deliveryPolicyName"
					, "( SELECT SB.CODE_NAME FROM SYS_CODE SB WHERE SB.CODE_CD = TPI.DELIVERY_POLICY )", "ECS_TP_DELIVERYPOLICY" )
		}) );


		/***************************************************************************************************
		 *	ECS_TRADEPARTNER_SELLERCODE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( PM, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( PM, "sellerPartnerCode",	"SELLER_PARTYCD",		"ECS_TP_SELLERPARTNERCODE",		0, 25, CHARS )
			, createFD( MD, "sellerName",			"SELLER_NAME",			"ECS_TP_SELLERNAME",			DESC, 0, 128 )
			, createFD( OP, "taxType",				"TAXTYPE",				"ECS_TP_TAXTYPE",				"ECS_TAXTYPE_", "Y,N,Z" )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"ECS_TPSL_EXTRAVALUE",			0, 128 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_TRADEPARTNER_SELLERCODE, table = createTable("ECS_TRADEPARTNER_SLCODE", "TPS", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_RETAIL
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "retailGln",			"RT_GLN",				"RETAILGLN",					0, 13 )
			, createFD( MD, "retailName",			"RT_NAME",				"RETAILNAME",					DESC, 0, 128 )
			, createFD( MR, "manageKey",			"MNGKEY",				"RBM_MNGKEY_MANAGEKEY",			7, 7 )
					.setInsertQuery( "NVL2( NULL, ?, 'R' || seqRBM_MNG_KEY.CURRVAL )" )
			, createFD( MD, "analOption",			"ANALOPTION",			"ECS_PARTY_ANALOPTION",			0, 32 )
			, createFD( OP, "displaySequence",		"DISPLAY_SEQ",			"DISPLAYSEQUENCE",				INTEGER, 1, 999999 )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"ECS_PARTY_RETAIL_EXTRAVALUE",	0, 128 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_RETAIL, table = createTable("ECS_RETAIL", "RTL", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_RETAIL, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( INTEGER, "analysisStoreCount", "( SELECT COUNT(*) FROM ECS_STORE SB WHERE SB.RT_GLN = RTL.RT_GLN )" )
		}) );


		/***************************************************************************************************
		 *	ECS_STORE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "storeGln",				"ST_GLN",				"STOREGLN",						0, 13 )
			, createFD( OR, "retailGln",			"RT_GLN",				"RETAILGLN",					0, 13 )
			, createFD( MD, "storeName",			"ST_NAME",				"STORENAME",					DESC, 0, 128 )
			, createFD( MR, "manageKey",			"MNGKEY",				"RBM_MNGKEY_MANAGEKEY",			7, 7 )
					.setInsertQuery( "NVL2( NULL, ?, 'P' || seqRBM_MNG_KEY.CURRVAL )" )
			, createFD( MD, "analOption",			"ANALOPTION",			"ECS_PARTY_ANALOPTION",			0, 32 )
			, createFD( MD, "areaCode",				"AREACD",				"ECS_PARTY_AREACODE",			0, 9 )
			, createFD( MD, "statusCode",			"STATUSCD",				"ECS_PARTY_STATUSCODE",			3, 3 )
			, createFD( MD, "comareaCode",			"COMAREACD",			"ECS_PARTY_COMAREACODE",		3, 3 )
			, createFD( MD, "scaleCode",			"SCALECD",				"ECS_PARTY_SCALECODE",			3, 3 )
			, createFD( OP, "factor1Code",			"FACTOR1CD",			"ECS_PARTY_FACTOR1CODE",		3, 3 )
			, createFD( OP, "factor2Code",			"FACTOR2CD",			"ECS_PARTY_FACTOR2CODE",		3, 3 )
			, createFD( OP, "extraValue",			"EXTRAVALUE",			"ECS_PARTY_STORE_EXTRAVALUE",	0, 128 )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_STORE, table = createTable("ECS_STORE", "STR", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_STORE, new QueryableImpl(table) {{
			Joinable tbl_RTL = new JoinableImpl( "RTL", "ECS_RETAIL", "RTL.RT_GLN(+) = STR.RT_GLN" );

			append( new QueryableField[] {
				  new QueryableFieldImpl( DESC, "retailName", "RTL.RT_NAME", "RETAILNAME", tbl_RTL )
				, new QueryableFieldImpl( STRING, "retailAnalOption", "RTL.ANALOPTION", "ECS_PARTY_ANALOPTION", tbl_RTL )
				, new QueryableFieldImpl( DESC, "areaName"
						, "( SELECT SB.AREA_NAME FROM ECS_ST_AREA SB WHERE SB.AREA_CD = STR.AREACD )", "ECS_PARTY_AREANAME" )
				, new QueryableFieldImpl( DESC, "statusName"
						, "( SELECT SB.STATUS_NAME FROM ECS_ST_STATUS SB WHERE SB.STATUS_CD = STR.STATUSCD )", "ECS_PARTY_STATUSNAME" )
				, new QueryableFieldImpl( DESC, "comareaName"
						, "( SELECT SB.COMAREA_NAME FROM ECS_ST_COMAREA SB WHERE SB.COMAREA_CD = STR.COMAREACD )", "ECS_PARTY_COMAREANAME" )
				, new QueryableFieldImpl( DESC, "scaleName"
						, "( SELECT SB.SCALE_NAME FROM ECS_ST_SCALE SB WHERE SB.SCALE_CD = STR.SCALECD )", "ECS_PARTY_SCALENAME" )
				, new QueryableFieldImpl( DESC, "factor1Name"
						, "( SELECT SB.FACTOR1_NAME FROM ECS_ST_FACTOR1 SB WHERE SB.FACTOR1_CD = STR.FACTOR1CD )", "ECS_PARTY_FACTOR1NAME" )
				, new QueryableFieldImpl( DESC, "factor2Name"
						, "( SELECT SB.FACTOR2_NAME FROM ECS_ST_FACTOR2 SB WHERE SB.FACTOR2_CD = STR.FACTOR2CD )", "ECS_PARTY_FACTOR2NAME" )
				, new QueryableFieldImpl( DESC, "factor2Name"
						, "( SELECT SB.FACTOR2_NAME FROM ECS_ST_FACTOR2 SB WHERE SB.FACTOR2_CD = STR.FACTOR2CD )", "ECS_PARTY_FACTOR2NAME" )
			} );
		}} );


		/***************************************************************************************************
		 *	ECS_DELIVERYCAL
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "deliveryCalendarKey",	"DELIVERYCAL_KEY",		"ECS_TP_DELIVERYCALENDARKEY",	10, 10 )
			, createFD( MD, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( MD, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( OP, "deliveryRoute",		"DELIVERYROUTE",		"ECS_TP_DELIVERYROUTE",			"ECS_DELIVERYROUTE_", "R1,R2,R3" )
			, createFD( OP, "supplierGln",			"SUPPLIERGLN",			"ECS_TP_SUPPLIERGLN",			0, 13 )
			, createFD( OP, "dcGln",				"DC_GLN",				"ECS_TP_DCGLN",					0, 13 )
			, createFD( OP, "destinationGln",		"DESTINATION_GLN",		"ECS_ITEMTP_DESTINATIONGLN",	0, 13 )
			, createFD( OP, "orderLeadTimeToDestination",	"ORDER_LEADTIME_TO_DEST",	"ECS_TP_ORDERLEADTIMETODESTINATION",	INTEGER, 0, 999999 )
			, createFD( OP, "orderLeadTimeToBuyer",			"ORDER_LEADTIME_TO_BUYER",	"ECS_TP_ORDERLEADTIMETOBUYER",			INTEGER, 0, 999999 )
			, createFD( OP, "orderWeekday",			"ORDER_WEEKDAY",		"ECS_TP_ORDERWEEKDAY",			7, 7, "YN" )
			, createFD( OP, "orderPolicy",			"ORDER_POLICY",			"ECS_TP_ORDERPOLICY",			"ECS_ORDER_POLICY_", "ND,NO,IG" )
			, createFD( RD, "status",				"STATUS",				"STATUS",						"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_DELIVERYCAL, table = createTable("ECS_DELIVERYCAL", "DLV", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_DELIVERYCAL, new QueryableImpl(table) {{
			append( makePartyQueryableFields( "buyer", "PPBY", "PPBY.GLN(+) = DLV.PARENT_BUYERGLN" ) );
			append( makePartyQueryableFields( "seller", "PPSL", "PPSL.GLN(+) = DLV.PARENT_SELLERGLN" ) );
			append( makePartyQueryableFields( "supplier", "PSP", "PSP.GLN(+) = DLV.SUPPLIERGLN" ) );
			append( makePartyQueryableFields( "dc", "PDC", "PDC.GLN(+) = DLV.DC_GLN" ) );
			append( makePartyQueryableFields( "destination", "PDT", "PDT.GLN(+) = DLV.DESTINATION_GLN" ) );
		}} );


		/***************************************************************************************************
		 *	ECS_DELIVERYCAL_DTL
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "deliveryCalendarKey",	"DELIVERYCAL_KEY",		"ECS_TP_DELIVERYCALENDARKEY",	10, 10 )
			, createFD( PM, "orderDate",			"ORDDATE",				"ORDERDATE",					DATE )
			, createFD( MD, "existOrder",			"ORDER_EXIST_IND",		"ECS_DLVSCH_EXISTORDER",		"PUB_WHETHER_", "Y,N" )
			, createFD( MD, "inDate",				"INDATE",				"INDATE",						DATE )
			, createFD( OP, "inDateStore",			"INDATE_STORE",			"INDATE_STORE",					DATE )
			, createFD( OP, "planPeriod",			"PLANPERIOD",			"ECS_DLVSCH_ORDERPLANPERIOD",	INTEGER, 0, 99 )
			, createFD( MO, "status",				"STATUS",				"ECS_DLVSCH_STATUS",			"ECS_DLVSCH_STATUS_", "00,FX" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( RD, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_DELIVERYCAL_DTL, table = createTable("ECS_DELIVERYCAL_DTL", "DLVD", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEM
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25, "0123456789" )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( MR, "itemInfoType",			"ITEMINFO_TYPE",		"ECS_ITEM_ITEMINFOTYPE",		CODE )
					.setValidValueList( "ECS_ITEM_INFOTYPE_", "RI,RT,RC,SI,SP,SC" )
			, createFD( OR, "parentGln",			"PARENT_GLN",			"ECS_ITEM_PARENTGLN",			0, 13 )
			, createFD( MO, "informationGln",		"INFORMATION_GLN",		"ECS_ITEM_INFORMATIONGLN",		0, 13 )
			, createFD( MO, "baseGtin",				"BASE_GTIN",			"ECS_ITEM_BASEGTIN",			0, 25 )
			, createFD( OR, "parentGtin",			"PARENT_GTIN",			"ECS_ITEM_PARENTGTIN",			0, 25 )
			, createFD( OR, "grandParentGtin",		"GRANDPARENT_GTIN",		"ECS_ITEM_GRANDPARENTGTIN",		0, 25 )
			, createFD( OR, "itemLevel",			"ITEMLEVEL",			"ECS_ITEM_ITEMLEVEL",			"ECS_ITEM_LEVEL_", "1,2,3" )
			, createFD( OR, "transactionLevel",		"TRANLEVEL",			"ECS_ITEM_TRANSACTIONLEVEL",	"ECS_ITEM_LEVEL_", "1,2,3" )
			, createFD( OP, "partyCategoryCode",	"PARTY_CATECD",			"ECS_ITEM_PARTYCATEGORY",		0, 20 )
			, createFD( OP, "itemCode",				"ITEMCODE",				"ECS_ITEM_ITEMCODE",			0, 25, CHARS )
			, createFD( MD, "itemName",				"ITEMNAME",				"ECS_ITEM_ITEMNAME",			DESC, 0, 200 )
			, createFD( OP, "brandName",			"BRANDNAME",			"ECS_ITEM_BRANDNAME",			DESC, 0, 100 )
			, createFD( MD, "shortDescription",		"SHORTDESC",			"ECS_ITEM_SHORTDESCRIPTION",	DESC, 0, 40 )
			, createFD( OP, "measureDescription",	"MEASUREDESC",			"ECS_ITEM_MEASUREDESCRIPTION",	DESC, 0, 40 )
			, createFD( OP, "additionalDescription","ADDITIONALDESC",		"ECS_ITEM_ADDITIONALDESCRIPTION", DESC, 0, 350 )
			, createFD( OP, "searchString",			"SEARCHSTRING",			"ECS_ITEM_SEARCHSTRING",		DESC, 0, 300 )
			, createFD( MO, "isConsumerUnit",		"CONSUMER_IND",			"ECS_ITEM_ISCONSUMERUNIT",		"PUB_WHETHER_", "Y,N" )
			, createFD( MO, "effectiveChangeDate",	"EFFECTIVE_CHGDATE",	"ECS_ITEM_EFFECTIVECHANGEDATE",	DATE )
			, createFD( OP, "publicationDate",		"PUBLIC_DATE",			"ECS_ITEM_PUBLICATIONDATE",		DATE )
			, createFD( OP, "startAvailDate",		"STARTAVAIL_DATETIME",	"ECS_ITEM_STARTAVAILDATE",		DATE )
			, createFD( OP, "endAvailDate",			"ENDAVAIL_DATETIME",	"ECS_ITEM_ENDAVAILDATE",		DATE )
			, createFD( OP, "consumerAvailDate",	"CONSUMERAVAIL_DATETIME", "ECS_ITEM_CONSUMERAVAILDATE", DATE )
			, createFD( OP, "discontinuedDate",		"DISCONTINUED_DATE",	"ECS_ITEM_DISCONTINUEDDATE",	DATE )
			, createFD( OP, "canceledDate",			"CANCELED_DATE",		"ECS_ITEM_CANCELEDDATE",		DATE )
			, createFD( OP, "retailPrice",			"RETAILPRICE",			"ECS_ITEM_RETAILPRICE",			DOUBLE, double_0, null )
			, createFD( OP, "retailPriceCurr",		"RETAILPRICE_CURR",		"ECS_ITEM_RETAILPRICECURR",		3, 3 )
			, createFD( OP, "retailPriceBasis",		"RETAILPRICEBASIS",		"ECS_ITEM_RETAILPRICEBASIS",	DOUBLE, double_0, null )
			, createFD( OP, "isInformationPrivate",	"PRIVATE_IND",			"ECS_ITEM_ISINFORMATIONPRIVATE","ECS_ITEM_PRIVATEIND_", "Y,C,M,S,N" )
			, createFD( OP, "taxType",				"TAXTYPE",				"ECS_ITEM_TAXTYPE",				"ECS_TAXTYPE_", "Y,N,Z" )
			, createFD( MO, "sourcing",				"SOURCING",				"ECS_ITEM_SOURCING",			"ECS_ITEM_SOURCING_", "S,I,E" )
			, createFD( RD, "itemKind",				"ITEMKIND",				"ECS_ITEM_ITEMKIND",			"ECS_ITEMKIND_", "00,GR,FS,FE,FW" )
			, createFD( RD, "itemUnit",				"ITEMUNIT",				"ECS_ITEM_ITEMUNIT",			"ECS_ITEMUNIT_", "EA,PK,CA,PL" )
			, createFD( RD, "iCategoryCode",		"ICATECD",				"ECS_ITEM_ICATEGORYCODE",		0, 20 )
			, createFD( RD, "categoryCode",			"CATECD",				"ECS_ITEM_CATEGORYCODE",		0, 20 )
			, createFD( RD, "hierarchyRefGtin",		"HIERARCHY_REFGTIN",	"ECS_ITEM_HIERARCHYREFGTIN",	0, 25 )
			, createFD( RD, "hierarchyRefGln",		"HIERARCHY_REFGLN",		"ECS_ITEM_HIERARCHYREFGLN",		0, 13 )
			, createFD( RD, "measureRefGtin",		"MEASURE_REFGTIN",		"ECS_ITEM_MEASUREREFGTIN",		0, 25 )
			, createFD( RD, "measureRefGln",		"MEASURE_REFGLN",		"ECS_ITEM_MEASUREREFGLN",		0, 13 )
			, createFD( RD, "packagingRefGtin",		"PACKAGING_REFGTIN",	"ECS_ITEM_PACKAGINGREFGTIN",	0, 25 )
			, createFD( RD, "packagingRefGln",		"PACKAGING_REFGLN",		"ECS_ITEM_PACKAGINGREFGLN",		0, 13 )
			, createFD( RD, "handlingRefGtin",		"HANDLING_REFGTIN",		"ECS_ITEM_HANDLINGREFGTIN",		0, 25 )
			, createFD( RD, "handlingRefGln",		"HANDLING_REFGLN",		"ECS_ITEM_HANDLINGREFGLN",		0, 13 )
			, createFD( RD, "orderingRefGtin",		"ORDERING_REFGTIN",		"ECS_ITEM_ORDERINGREFGTIN",		0, 25 )
			, createFD( RD, "orderingRefGln",		"ORDERING_REFGLN",		"ECS_ITEM_ORDERINGREFGLN",		0, 13 )
			, createFD( RD, "priceRefGtin",			"PRICE_REFGTIN",		"ECS_ITEM_PRICEREFGTIN",		0, 25 )
			, createFD( RD, "priceRefGln",			"PRICE_REFGLN",			"ECS_ITEM_PRICEREFGLN",			0, 13 )
			, createFD( MO, "itemStatus",			"STATUS",				"ECS_ITEM_STATUS",				"ECS_ITEM_STATUS_", "00,01,LK,99,XX" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM, table = createTable("ECS_ITEM", "ITM", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ITEM, new QueryableImpl(table) {
			{
				QueryableField[] qfields;

				append( makePartyQueryableFields( null, "PTY", "PTY.GLN(+) = ITM.GLN" ) );
				append( makePartyQueryableFields( "parent", "PPTY", "PPTY.GLN(+) = ITM.PARENT_GLN" ) );
				append( makePartyQueryableFields( "information", "PINF", "PINF.GLN(+) = ITM.INFORMATION_GLN" ) );
				append( makeItemQueryableFields( "base", "BITM", "BITM.GLN(+) = ITM.GLN AND BITM.GTIN(+) = ITM.BASE_GTIN" ) );
				append( makeItemQueryableFields( "parent", "PITM", "PITM.GLN(+) = ITM.GLN AND PITM.GTIN(+) = ITM.PARENT_GTIN" ) );
				append( makeItemQueryableFields( "grandParent", "GPITM", "GPITM.GLN(+) = ITM.GLN AND GPITM.GTIN(+) = ITM.GRANDPARENT_GTIN" ) );

				qfields = com.irt.rbm.sys.Schema.makeCategoryLevelQueryFields( com.irt.rbm.sys.Schema.SYS_ICATEGORY, "ITM.ICATECD" );
				if( qfields != null ) append( qfields );

				qfields = com.irt.rbm.sys.Schema.makeCategoryLevelQueryFields( com.irt.rbm.sys.Schema.SYS_CATEGORY, "ITM.CATECD" );
				if( qfields != null ) append( qfields );

				append( new QueryableField[] {
					  new QueryableFieldImpl( STRING, "retailPriceName"
							, "(SELECT SB.CURR_NAME FROM SYS_CURRENCY SB WHERE SB.CURR_CD = ITM.RETAILPRICE_CURR)", "ECS_ITEM_RETAILPRICECURR" )
					, new QueryableFieldImpl( STRING, "retailPriceSymbol"
							, "(SELECT SB.CURR_SYMBOL FROM SYS_CURRENCY SB WHERE SB.CURR_CD = ITM.RETAILPRICE_CURR)", "ECS_ITEM_RETAILPRICECURR" )
					, new QueryableFieldImpl( INTEGER, "childItemCount"
							, "( SELECT COUNT(*) FROM ECS_ITEM SB WHERE SB.GLN = ITM.GLN AND SB.PARENT_GTIN = ITM.GTIN )", "ECS_ITEM_CHILDITEMCOUNT" )
					, new QueryableFieldImpl( INTEGER, "grandChildItemCount"
							, "( SELECT COUNT(*) FROM ECS_ITEM SB WHERE SB.GLN = ITM.GLN AND SB.GRANDPARENT_GTIN = ITM.GTIN )"
							, "ECS_ITEM_GRANDCHILDITEMCOUNT" )
					, new QueryableFieldImpl( INTEGER, "tradeRetailCount"
							, "( SELECT COUNT(*) FROM ECS_ITEMTP SB "
									+" WHERE SB.SELLERGLN = ITM.GLN AND SB.SELLERGTIN = ITM.GTIN AND SB.ITEMINFO_TYPE = 'TP' )" )
				} );
			}

			public boolean appendCondition( ConditionQueryBuffer querybuf ) {
				boolean hasCondition = super.appendCondition( querybuf );

				Object[] values = querybuf.getConditionValues( "originCode" );
				if( values != null ) {
					QueryBuffer s_querybuf = new QueryBuffer();

					hasCondition = true;
					s_querybuf.appendData( "'x'" );
					s_querybuf.appendTableWithAlias( "ECS_ITEM_ORIGIN", "SB", "SB.GTIN = ITM.GTIN AND SB.GLN = ITM.INFORMATION_GLN" );
					s_querybuf.appendConditionByField( "SB.ORIGINCD", values );
					querybuf.appendCondition( "EXISTS("+ s_querybuf.getQuery() +")", s_querybuf.getBindVariables() );
				}

				return hasCondition;
			}
		} );


		/***************************************************************************************************
		 *	ECS_ITEM_INFO
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( MR, "gtinType",				"GTINTYPE",				"ECS_ITEM_GTINTYPE",			CODE )
					.setValidValueList( "ECS_GTINTYPE_", "GTN,E08,E13,E14,UPC,NDC,P13,PRV,PUA" )
			, createFD( MD, "itemKind",				"ITEMKIND",				"ECS_ITEM_ITEMKIND",			"ECS_ITEMKIND_", "00,GR,FS,FE,FW" )
			, createFD( MR, "itemUnit",				"ITEMUNIT",				"ECS_ITEM_ITEMUNIT",			"ECS_ITEMUNIT_", "EA,PK,CA,PL" )
			, createFD( OP, "replacedGtin",			"REPLACED_GTIN",		"ECS_ITEM_REPLACEDGTIN",		0, 25 )
			, createFD( OP, "iCategoryCode",		"ICATECD",				"ECS_ITEM_ICATEGORYCODE",		0, 20 )
			, createFD( OP, "categoryCode",			"CATECD",				"ECS_ITEM_CATEGORYCODE",		0, 20 )
			, createFD( OP, "uNSPSCCode",			"SPSCCD",				"ECS_ITEM_UNSPSCCODE",			0, 12 )
			, createFD( OP, "hSCode",				"HSCD",					"ECS_ITEM_HSCODE",				0, 10 )
			, createFD( OP, "groupIdCode",			"GROUPID_CD",			"ECS_ITEM_GROUPIDCODE",			0, 20 )
			, createFD( OP, "groupIdDescription",	"GROUPID_DESC",			"ECS_ITEM_GROUPIDDESCRIPTION",	DESC, 0, 100 )
			, createFD( OP, "manufGln",				"MANUF_GLN",			"ECS_ITEM_MANUFGLN",			0, 13 )
			, createFD( OP, "brandOwnerGln",		"BRANDOWNER_GLN",		"ECS_ITEM_BRANDOWNERGLN",		0, 13 )
			, createFD( MD, "isBaseUnit",			"BASEUNIT_IND",			"ECS_ITEM_ISBASEUNIT",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isDespatchUnit",		"DESPATCH_IND",			"ECS_ITEM_ISDESPATCHUNIT",		"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isInvoiceUnit",		"INVOICE_IND",			"ECS_ITEM_ISINVOICEUNIT",		"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isOrderableUnit",		"ORDERABLE_IND",		"ECS_ITEM_ISORDERABLEUNIT",		"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isVariableWeight",		"VARIABLEWEIGHT_IND",	"ECS_ITEM_ISVARIABLEWEIGHT",	"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "palletTypeCode",			"PALLET_TYPECODE",			"ECS_ITEM_PALLETTYPECODE",				3, 3 )
			, createFD( OP, "palletTermsAndConditions",	"PALLET_TERMSANDCONDS",		"ECS_ITEM_PALLETTERMSANDCONDITIONS",	2, 2 )
			, createFD( OP, "priceOnPackInd",			"PRICEONPACK_IND",			"ECS_ITEM_PRICEONPACKIND",				"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "retailPriceOnItem",		"RETAILPRICEONPACK",		"ECS_ITEM_RETAILPRICEONITEM",			DOUBLE, double_0, null )
			, createFD( OP, "retailPriceOnItemCurr",	"RETAILPRICEONPACK_CURR",	"ECS_ITEM_RETAILPRICEONITEMCURR",		3, 3 )
			, createFD( OP, "cataloguePrice",			"CATALOGUE_PRICE",			"ECS_ITEM_CATALOGUEPRICE",				DOUBLE, double_0, null )
			, createFD( OP, "cataloguePriceCurr",		"CATALOGUE_PRICE_CURR",		"ECS_ITEM_CATALOGUEPRICECURR",			3, 3 )
			, createFD( OP, "suggestedRetailPrice",		"SUGGEST_RETAILPRICE",		"ECS_ITEM_SUGGESTEDRETAILPRICE",		DOUBLE, double_0, null )
			, createFD( OP, "suggestedRetailPriceCurr", "SUGGEST_RETAILPRICE_CURR",	"ECS_ITEM_SUGGESTEDRETAILPRICECURR",	3, 3 )
			, createFD( OP, "isNonSoldReturnable",	"ISNONSOLD_RETURNABLE",	"ECS_ITEM_ISNONSOLDRETURNABLE",	"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isMarkedAsRecyclable",	"ISMARKED_RECYCLABLE",	"ECS_ITEM_ISMARKEDASRECYCLABLE","PUB_WHETHER_", "Y,N" )
			, createFD( OP, "minimumLifespan",		"MIN_LIFESPAN",			"ECS_ITEM_MINIMUMLIFESPAN",		INTEGER, 0, 999999 )
			, createFD( OP, "vatRate",				"VATRATE",				"ECS_ITEM_VATRATE",				(double)0.0, (double)100.0 )
			, createFD( OP, "liquorTaxRate",		"LIQUORTAXRATE",		"ECS_ITEM_LIQUORTAXRATE",		(double)0.0, (double)100.0 )
			, createFD( RD, "status",				"STATUS",				"ECS_ITEM_STATUS",				"PUB_STATUS_", "00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_INFO, table = createTable("ECS_ITEM_INFO", "IIF", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ITEM_INFO, new QueryableImpl(table) {{
			append( makePartyQueryableFields( "manuf", "PMF", "PMF.GLN(+) = IIF.MANUF_GLN" ) );
			append( new QueryableField[] {
				  new QueryableFieldImpl( STRING, "retailPriceOnItemName"
						, "(SELECT SB.CURR_NAME FROM SYS_CURRENCY SB WHERE SB.CURR_CD = IIF.RETAILPRICEONPACK_CURR)"
						, "ECS_ITEM_RETAILPRICEONITEMCURR" )
				, new QueryableFieldImpl( STRING, "retailPriceOnItemSymbol"
						, "(SELECT SB.CURR_SYMBOL FROM SYS_CURRENCY SB WHERE SB.CURR_CD = IIF.RETAILPRICEONPACK_CURR)"
						, "ECS_ITEM_RETAILPRICEONITEMCURR" )
				, new QueryableFieldImpl( STRING, "cataloguePriceName"
						, "(SELECT SB.CURR_NAME FROM SYS_CURRENCY SB WHERE SB.CURR_CD = IIF.CATALOGUE_PRICE_CURR)", "ECS_ITEM_CATALOGUEPRICECURR" )
				, new QueryableFieldImpl( STRING, "cataloguePriceSymbol"
						, "(SELECT SB.CURR_SYMBOL FROM SYS_CURRENCY SB WHERE SB.CURR_CD = IIF.CATALOGUE_PRICE_CURR)", "ECS_ITEM_CATALOGUEPRICECURR" )
				, new QueryableFieldImpl( STRING, "suggestedRetailPriceName"
						, "(SELECT SB.CURR_NAME FROM SYS_CURRENCY SB WHERE SB.CURR_CD = IIF.SUGGEST_RETAILPRICE_CURR)"
						, "ECS_ITEM_SUGGESTEDRETAILPRICECURR" )
				, new QueryableFieldImpl( STRING, "suggestedRetailPriceSymbol"
						, "(SELECT SB.CURR_SYMBOL FROM SYS_CURRENCY SB WHERE SB.CURR_CD = IIF.SUGGEST_RETAILPRICE_CURR)"
						, "ECS_ITEM_SUGGESTEDRETAILPRICECURR" )
			} );
		}} );


		/***************************************************************************************************
		 *	ECS_ITEM_MASTER
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( MR, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( MR, "informationGln",		"INFORMATION_GLN",		"ECS_ITEM_INFORMATIONGLN",		0, 13 )
			, createFD( OP, "manufGln",				"MANUF_GLN",			"ECS_ITEM_MANUFGLN",			0, 13 )
			, createFD( OP, "gtinType",				"GTINTYPE",				"ECS_ITEM_GTINTYPE",			"ECS_GTINTYPE_", "GTN,PRV" )
			, createFD( OP, "iCategoryCode",		"ICATECD",				"ECS_ITEM_ICATEGORYCODE",		0, 20 )
			, createFD( OP, "categoryCode",			"CATECD",				"ECS_ITEM_CATEGORYCODE",		0, 20 )
			, createFD( OP, "itemName",				"ITEMNAME",				"ECS_ITEM_ITEMNAME",			DESC, 0, 200 )
			, createFD( OP, "brandName",			"BRANDNAME",			"ECS_ITEM_BRANDNAME",			DESC, 0, 100 )
			, createFD( OP, "shortDescription",		"SHORTDESC",			"ECS_ITEM_SHORTDESCRIPTION",	DESC, 0, 40 )
			, createFD( OP, "consumerAvailDate",	"CONSUMERAVAIL_DATETIME", "ECS_ITEM_CONSUMERAVAILDATE",	DATE )
			, createFD( OP, "measureValue",			"MEASURE",				"ECS_ITEM_MEASUREVALUE",		DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "measureValueUnit",		"MEASURE_UNIT",			"ECS_ITEM_MEASUREVALUEUNIT",	3, 3 )
			, createFD( RD, "status",				"STATUS",				"ECS_ITEM_STATUS",				"PUB_STATUS_", "00" )
		};
		putTable( ECS_ITEM_MASTER, table = createTable("ECS_ITEM_MASTER", "IMT", tfields) );
		putQueryable( ECS_ITEM_MASTER, new QueryableImpl(table) {{
			QueryableField[] qfields;

			append( makePartyQueryableFields( null, "PTY", "PTY.GLN(+) = IMT.GLN" ) );
			append( makePartyQueryableFields( "manuf", "PMF", "PMF.GLN(+) = IMT.MANUF_GLN" ) );
			append( makePartyQueryableFields( "information", "PINF", "PINF.GLN(+) = IMT.INFORMATION_GLN" ) );
			qfields = com.irt.rbm.sys.Schema.makeCategoryLevelQueryFields( com.irt.rbm.sys.Schema.SYS_ICATEGORY, "IMT.ICATECD" );
			if( qfields != null ) append( qfields );

			qfields = com.irt.rbm.sys.Schema.makeCategoryLevelQueryFields( com.irt.rbm.sys.Schema.SYS_CATEGORY, "IMT.CATECD" );
			if( qfields != null ) append( qfields );
		}} );


		/***************************************************************************************************
		 *	ECS_ITEM_LINK
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "lowerLevelGtin",		"LOWERGTIN",			"ECS_ITEM_LOWERGTIN",			0, 25 )
			, createFD( MD, "lowerQty",				"LOWERQTY",				"ECS_ITEM_LOWERQTY",			INTEGER, 0, 999999, EQU_NY )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_LINK, table = createTable("ECS_ITEM_LINK", "ILK", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEM_GLN
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "subGlnClass",			"SUB_CLASS",			"ECS_ITEM_SUBGLNCLASS",			"ECS_ITEM_SUBGLNCLASS_", "PV" )
					.setInsertQuery( "NVL( ?, 'PV' )" )
			, createFD( PM, "privateGln",			"SUB_GLN",				"ECS_ITEM_PRIVATEGLN",			0, 13 )
			, createFD( OP, "publicationDate",		"PUBLIC_DATE",			"ECS_ITEM_PUBLICATIONDATE",		DATE )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_PRIVATEGLN, table = createTable("ECS_ITEM_GLN", "IPV", tfields, "UPGDATE = SYSDATE") );


		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "subGlnClass",			"SUB_CLASS",			"ECS_ITEM_SUBGLNCLASS",			"ECS_ITEM_SUBGLNCLASS_", "MF" )
					.setInsertQuery( "NVL( ?, 'MF' )" )
			, createFD( PM, "manufGln",				"SUB_GLN",				"ECS_ITEM_MANUFGLN",			0, 13 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_MANUFGLN, table = createTable("ECS_ITEM_GLN", "IMF", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEM_PRICE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "priceTypeCode",		"PRICETYPECD",			"ECS_ITEM_PRICETYPECODE",		3, 3 )
			, createFD( PM, "effectiveStartDate",	"EFFECTIVE_STARTDATE",	"ECS_ITEM_EFFECTIVESTARTDATE",	DATE )
			, createFD( OP, "effectiveEndDate",		"EFFECTIVE_ENDDATE",	"ECS_ITEM_EFFECTIVEENDDATE",	DATE )
			, createFD( MD, "price",				"PRICE",				"ECS_ITEM_PRICE",				DOUBLE, double_0, null )
			, createFD( MD, "priceCurr",			"PRICE_CURR",			"ECS_ITEM_PRICECURR",			3, 3 )
			, createFD( OP, "reasonOfPriceChangeCode", "CHANGE_REASON",		"ECS_ITEM_REASONOFPRICECHANGECODE", 0, 10 )
			, createFD( MO, "status",				"STATUS",				"ECS_ITEM_PRICE_STATUS",		CODE )
					.setValidValueList( "ECS_ITEM_PRICE_STATUS_", "00,01,02,03,10,11" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_PRICE, table = createTable("ECS_ITEM_PRICE", "IPR", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEM_ORIGIN
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "originCode",			"ORIGINCD",				"ECS_ITEM_ORIGINCODE",			0, 6 )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"CREATEUSERID",			"CREATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_ORIGIN, table = createTable("ECS_ITEM_ORIGIN", "IOG", tfields) );
		putQueryable( ECS_ITEM_ORIGIN, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( DESC, "originName", "ORG.ORIGIN_NAME", "ECS_ITEM_ORIGINNAME"
					, new JoinableImpl( "ORG", "ECS_ORIGIN", "ORG.ORIGIN_CD(+) = IOG.ORIGINCD" ) )
		}) );


		/***************************************************************************************************
		 *	ECS_ITEM_DESCRIPTION
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "languageCode",			"LANGCD",				"ECS_ITEM_DESCRIPTIONLANG",		2, 2 )
			, createFD( OP, "itemName",				"ITEMNAME",				"ECS_ITEM_ITEMNAME",			DESC, 0, 200 )
			, createFD( OP, "brandName",			"BRANDNAME",			"ECS_ITEM_BRANDNAME",			DESC, 0, 100 )
			, createFD( OP, "shortDescription",		"SHORTDESC",			"ECS_ITEM_SHORTDESCRIPTION",	DESC, 0, 40 )
			, createFD( OP, "functionalName",		"FUNCTIONALNAME",		"ECS_ITEM_FUNCTIONALNAME",		DESC, 0, 100 )
			, createFD( OP, "additionalDescription","ADDITIONALDESC",		"ECS_ITEM_ADDITIONALDESCRIPTION", DESC, 0, 350 )
			, createFD( OP, "invoiceName",			"INVOICENAME",			"ECS_ITEM_INVOICENAME",			DESC, 0, 100 )
			, createFD( OP, "productRange",			"PRODUCTRANGE",			"ECS_ITEM_PRODUCTRANGE",		DESC, 0, 100 )
			, createFD( OP, "subBrand",				"SUBBRAND",				"ECS_ITEM_SUBBRAND",			DESC, 0, 100 )
			, createFD( OP, "groupIdDescription",	"GROUPID_DESC",			"ECS_ITEM_GROUPIDDESCRIPTION",	DESC, 0, 100 )
			, createFD( OP, "formDescription",		"FORMDESC",				"ECS_ITEM_FORMDESCRIPTION",		DESC, 0, 100 )
			, createFD( OP, "variant",				"VARIANT",				"ECS_ITEM_VARIANT",				DESC, 0, 100 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_DESCRIPTION, table = createTable("ECS_ITEM_DESC", "IDE", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEM_HIERARCHY
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( OP, "qtyOfInnerPack",		"INNERPACKS",			"ECS_ITEM_QTYOFINNERPACK",		INTEGER, 0, 999999, EQU_NY )
			, createFD( OP, "qtyOfLowerItemsInInnerPack", "LOWERITEMS_INNERPACK", "ECS_ITEM_QTYOFINNERPACK", INTEGER, 0, 999999, EQU_NY )
			, createFD( OP, "qtyOfLayers",			"LAYERS",				"ECS_ITEM_QTYOFLAYERS",			INTEGER, 0, 999999, EQU_NY )
			, createFD( OP, "qtyOfItemsInLayer",	"ITEMS_INALAYER",		"ECS_ITEM_QTYOFITEMSINLAYER",	INTEGER, 0, 999999, EQU_NY )
			, createFD( OP, "qtyOfLayersPerPallet",	"LAYERS_PERPALLET",		"ECS_ITEM_QTYOFLAYERSPERPALLET",INTEGER, 0, 999999, EQU_NY )
			, createFD( OP, "qtyOfItemsPerLayer",	"ITEMS_PERLAYER",		"ECS_ITEM_QTYOFITEMSPERLAYER",	INTEGER, 0, 999999, EQU_NY )
			, createFD( OP, "qtyOfItemsPerPallet",	"ITEMS_PERPALLET",		"ECS_ITEM_QTYOFITEMSPERPALLET",	INTEGER, 0, 999999, EQU_NY )
			, createFD( OP, "qtyOfItemsPerInnerPack", "ITEMS_PERINNERPACK", "ECS_ITEM_QTYOFITEMSPERINNERPACK", INTEGER, 0, 999999, EQU_NY )
			, createFD( OP, "qtyOfItemsPerCase",	"ITEMS_PERCASE",		"ECS_ITEM_QTYOFITEMSPERCASE",	INTEGER, 0, 999999, EQU_NY )
			, createFD( OP, "caseName",				"CASE_NAME",			"ECS_ITEM_CASENAME",			0, 10 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_HIERARCHY, table = createTable("ECS_ITEM_HIERARCHY", "IHR", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEM_MEASURE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( OP, "measureValue",			"MEASURE",				"ECS_ITEM_MEASUREVALUE",		DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "measurePrice",			"MEASURE_PRICE",		"ECS_ITEM_MEASUREPRICE",		DOUBLE, double_0, null )
			, createFD( OP, "measureValueUnit",		"MEASURE_UNIT",			"ECS_ITEM_MEASUREVALUEUNIT",	3, 3 )
			, createFD( OP, "depth",				"DEPTH",				"ECS_ITEM_DEPTH",				DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "height",				"HEIGHT",				"ECS_ITEM_HEIGHT",				DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "width",				"WIDTH",				"ECS_ITEM_WIDTH",				DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "diameter",				"DIAMETER",				"ECS_ITEM_DIAMETER",			DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "lengthUnit",			"LENGTH_UNIT",			"ECS_ITEM_LENGTHUNIT",			3, 3 )
			, createFD( OP, "grossWeight",			"GROSSWEIGHT",			"ECS_ITEM_GROSSWEIGHT",			DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "netWeight",			"NETWEIGHT",			"ECS_ITEM_NETWEIGHT",			DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "drainedWeight",		"DRAINEDWEIGHT",		"ECS_ITEM_DRAINEDWEIGHT",		DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "weightUnit",			"WEIGHT_UNIT",			"ECS_ITEM_WEIGHTUNIT",			3, 3 )
			, createFD( OP, "netContent",			"NETCONTENT",			"ECS_ITEM_NETCONTENT",			DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "netContentUnit",		"NETCONTENT_UNIT",		"ECS_ITEM_NETCONTENTUNIT",		3, 3 )
			, createFD( OP, "pegHorizontal",		"PEGH",					"ECS_ITEM_PEGHORIZONTAL",		DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "pegVertical",			"PEGV",					"ECS_ITEM_PEGVERTICAL",			DOUBLE, double_0, null, EQU_NY )
			, createFD( OP, "pegUnit",				"PEG_UNIT",				"ECS_ITEM_PEGUNIT",				3, 3 )
			, createFD( OP, "liquidVolume",			"LIQUIDVOL",			"ECS_ITEM_LIQUIDVOLUME",		DOUBLE, double_0, null, EQU_YY )
			, createFD( OP, "liquidVolumeUnit",		"LIQUIDVOL_UNIT",		"ECS_ITEM_LIQUIDVOLUMEUNIT",	3, 3 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_MEASURE, table = createTable("ECS_ITEM_MEASURE", "IMS", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ITEM_MEASURE, new QueryableImpl(table, new QueryableField[] {
			  new QueryableFieldImpl( STRING, "measureValueName"
					, "(SELECT SB.UNIT_NAME FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.MEASURE_UNIT)", "ECS_ITEM_MEASUREVALUEUNIT" )
			, new QueryableFieldImpl( STRING, "measureValueSymbol"
					, "(SELECT SB.UNIT_SYMBOL FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.MEASURE_UNIT)", "ECS_ITEM_MEASUREVALUEUNIT" )
			, new QueryableFieldImpl( STRING, "lengthName"
					, "(SELECT SB.UNIT_NAME FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.LENGTH_UNIT)", "ECS_ITEM_LENGTHUNIT" )
			, new QueryableFieldImpl( STRING, "lengthSymbol"
					, "(SELECT SB.UNIT_SYMBOL FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.LENGTH_UNIT)", "ECS_ITEM_LENGTHUNIT" )
			, new QueryableFieldImpl( STRING, "weightName"
					, "(SELECT SB.UNIT_NAME FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.WEIGHT_UNIT)", "ECS_ITEM_WEIGHTUNIT" )
			, new QueryableFieldImpl( STRING, "weightSymbol"
					, "(SELECT SB.UNIT_SYMBOL FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.WEIGHT_UNIT)", "ECS_ITEM_WEIGHTUNIT" )
			, new QueryableFieldImpl( STRING, "netContentName"
					, "(SELECT SB.UNIT_NAME FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.NETCONTENT_UNIT)", "ECS_ITEM_NETCONTENTUNIT" )
			, new QueryableFieldImpl( STRING, "netContentSymbol"
					, "(SELECT SB.UNIT_SYMBOL FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.NETCONTENT_UNIT)", "ECS_ITEM_NETCONTENTUNIT" )
			, new QueryableFieldImpl( STRING, "pegName"
					, "(SELECT SB.UNIT_NAME FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.PEG_UNIT)", "ECS_ITEM_PEGUNIT" )
			, new QueryableFieldImpl( STRING, "pegSymbol"
					, "(SELECT SB.UNIT_SYMBOL FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.PEG_UNIT)", "ECS_ITEM_PEGUNIT" )
			, new QueryableFieldImpl( STRING, "liquidVolumeName"
					, "(SELECT SB.UNIT_NAME FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.LIQUIDVOL_UNIT)", "ECS_ITEM_LIQUIDVOLUMEUNIT" )
			, new QueryableFieldImpl( STRING, "liquidVolumeSymbol"
					, "(SELECT SB.UNIT_SYMBOL FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMS.LIQUIDVOL_UNIT)", "ECS_ITEM_LIQUIDVOLUMEUNIT" )
		}) );


		/***************************************************************************************************
		 *	ECS_ITEM_MEASUREUNIT
		***************************************************************************************************/
		tfields[3] = tfields[2];
		tfields[2] = createFD( PM, "itemUnit", "ITEMUNIT", "ECS_ITEM_ITEMUNIT", "ECS_ITEMUNIT_", "EA,PK,CA,PL" );
		putTable( ECS_ITEM_MEASUREUNIT, table = createTable("ECS_ITEM_MEASUREU", "IMSU", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ITEM_MEASUREUNIT, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( STRING, "lengthName"
					, "(SELECT SB.UNIT_NAME FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMSU.LENGTH_UNIT)", "ECS_ITEM_LENGTHUNIT" )
			, new QueryableFieldImpl( STRING, "lengthSymbol"
					, "(SELECT SB.UNIT_SYMBOL FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMSU.LENGTH_UNIT)", "ECS_ITEM_LENGTHUNIT" )
			, new QueryableFieldImpl( STRING, "weightName"
					, "(SELECT SB.UNIT_NAME FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMSU.WEIGHT_UNIT)", "ECS_ITEM_WEIGHTUNIT" )
			, new QueryableFieldImpl( STRING, "weightSymbol"
					, "(SELECT SB.UNIT_SYMBOL FROM SYS_UNIT SB WHERE SB.UNIT_CD = IMSU.WEIGHT_UNIT)", "ECS_ITEM_WEIGHTUNIT" )
		}) );


		/***************************************************************************************************
		 *	ECS_ITEM_PACKAGING
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( OP, "packagingTypeCode",	"PKGTYPECD",			"ECS_ITEM_PACKAGINGTYPECODE",	3, 3 )
			, createFD( OP, "isBarcodeOnThePackage",		"ISBARCODEONPACKAGE",		"ECS_ITEM_ISBARCODEONPACKAGE",		"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isPkgMarkedAsRecyclable",		"ISMARKED_PACKRECYCLABLE",	"ECS_ITEM_ISPKGMARKEDASRECYCLABLE",	"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isPkgMarkedReturnable",		"ISMARKED_PACKRETURNABLE",	"ECS_ITEM_ISPKGMARKEDRETURNABLE",	"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isPkgMarkedWithExpirationDate","ISMARKED_EXPIRATIONDATE",	"ECS_ITEM_ISPKGMARKEDWITHEXPIRATIONDATE"
					, "PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isPkgMarkedWithGreenDot",		"ISMARKED_GREENDOT",		"ECS_ITEM_ISPKGMARKEDWITHGREENDOT",	"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "pkgMaterialCode",				"PKGMATERIALCD",			"ECS_ITEM_PKGMATERIALCODE", 3, 3 )
			, createFD( OP, "pkgMaterialComposition",		"PKGMATERIAL_COMPQTY",		"ECS_ITEM_PKGMATERIALCOMPOSITION",	DOUBLE, double_0, null )
			, createFD( OP, "pkgMaterialCompositionUnit",	"PKGMATERIAL_COMPQTY_UNIT",	"ECS_ITEM_PKGMATERIALCOMPOSITIONUNIT", 3, 3 )
			, createFD( OP, "packagingTermsAndConditions",	"PACKAGING_TERMSANDCONDS",	"ECS_ITEM_PACKAGINGTERMSANDCONDITIONS", 3, 3 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_PACKAGING, table = createTable("ECS_ITEM_PACKAGING", "IPK", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ITEM_PACKAGING, new QueryableImpl(table, new QueryableField[] {
			  new QueryableFieldImpl( STRING, "packagingTypeName"
					, "(SELECT SB.PKGTYP_NAME FROM SYS_PKGTYPE SB WHERE SB.PKGTYP_CD = IPK.PKGTYPECD)", "ECS_ITEM_PACKAGINGTYPENAME" )
		}) );


		/***************************************************************************************************
		 *	ECS_ITEM_HANDLING
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( OP, "handlingInstructionsCode",			"HANDLING_INSTRUCTIONS",	"ECS_ITEM_HANDLINGINSTRUCTIONCODE",			3, 3 )
			, createFD( OP, "stackingFactor",					"STACKING_FACTOR",			"ECS_ITEM_STACKINGFACTOR", INTEGER, 0, 999999 )
			, createFD( OP, "stackingWeightMaximum",			"STACKING_MAXWEIGHT",		"ECS_ITEM_STACKINGWEIGHTMAXIMUM"
					, DOUBLE, double_0, null )
			, createFD( OP, "stackingWeightMaximumUnit",		"STACKING_MAXWEIGHT_UNIT",	"ECS_ITEM_STACKINGWEIGHTMAXIMUMUNIT",		3, 3 )
			, createFD( OP, "deliveryToDCMinTemperature",		"MINTEMP_DELIVERYTODC",		"ECS_ITEM_DELIVERYTODCMINTEMPERATURE",		DOUBLE )
			, createFD( OP, "deliveryToDCMaxTemperature",		"MAXTEMP_DELIVERYTODC",		"ECS_ITEM_DELIVERYTODCMAXTEMPERATURE",		DOUBLE )
			, createFD( OP, "deliveryToMarketMinTemperature",	"MINTEMP_DELIVERYTOMARKET",	"ECS_ITEM_DELIVERYTOMARKETMINTEMPERATURE",	DOUBLE )
			, createFD( OP, "deliveryToMarketMaxTemperature",	"MAXTEMP_DELIVERYTOMARKET",	"ECS_ITEM_DELIVERYTOMARKETMAXTEMPERATURE",	DOUBLE )
			, createFD( OP, "storageHandlingMinTemperature",	"MINTEMP_STORAGEHANDLING",	"ECS_ITEM_STORAGEHANDLINGMINTEMPERATURE",	DOUBLE )
			, createFD( OP, "storageHandlingMaxTemperature",	"MAXTEMP_STORAGEHANDLING",	"ECS_ITEM_STORAGEHANDLINGMAXTEMPERATURE",	DOUBLE )
			, createFD( OP, "temperatureUnit",		"TEMPERATURE_UNIT",		"ECS_ITEM_TEMPERATUREUNIT",		3, 3 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_HANDLING, table = createTable("ECS_ITEM_HANDLING", "IHD", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ITEM_HANDLING, new QueryableImpl(table, new QueryableField[] {
			  new QueryableFieldImpl( STRING, "temperatureSymbol"
					, "(SELECT SB.UNIT_SYMBOL FROM SYS_UNIT SB WHERE SB.UNIT_CD(+) = IHD.TEMPERATURE_UNIT)" )
		}) );


		/***************************************************************************************************
		 *	ECS_ITEM_ORDERING
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( OP, "orderingLeadTime",		"ORDER_LEADTIME",		"ECS_ITEM_ORDERINGLEADTIME",	INTEGER, 0, 999999 )
			, createFD( OP, "orderUnit",			"ORDER_UNIT",			"ECS_ITEM_ORDERUNIT",			"ECS_ITEMUNIT_", "EA,PK,CA,PL" )
			, createFD( OP, "minimumOrderQty",		"ORDER_MINQTY",			"ECS_ITEM_MINIMUMORDERQTY",		INTEGER, 0, 999999999 )
			, createFD( OP, "maximumOrderQty",		"ORDER_MAXQTY",			"ECS_ITEM_MAXIMUMORDERQTY",		INTEGER, 0, 999999999 )
			, createFD( OP, "orderQtyMultiple",		"ORDER_MULTIPLEQTY",	"ECS_ITEM_ORDERQTYMULTIPLE",	INTEGER, 0, 999999999, EQU_NY )
			, createFD( OP, "orderSizingFactor",	"ORDER_SIZINGFACTOR",	"ECS_ITEM_ORDERSIZINGFACTOR",	INTEGER, 0, 999999999 )
			, createFD( OP, "orderSizingFactorUnit","ORDER_SIZINGFACTOR_UNIT", "ECS_ITEM_ORDERSIZINGFACTORUNIT",	3, 3 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_ORDERING, table = createTable("ECS_ITEM_ORDERING", "IOR", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEM_SEASON
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "seasonCode",			"SEASONCD",				"ECS_ITEM_SEASONCODE",			0, 6 )
			, createFD( RD, "createDateTime",		"CREATEDATE",			"CREATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"CREATEUSERID",			"CREATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_SEASON, table = createTable("ECS_ITEM_SEASON", "ISS", tfields) );
		putQueryable( ECS_ITEM_SEASON, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( DESC, "seasonName", "SEA.SEASON_NAME", "ECS_ITEM_SEASONNAME"
					, new JoinableImpl( "SEA", "ECS_SEASON", "SEA.SEASON_CD(+) = ISS.SEASONCD" ) )
		}) );


		/***************************************************************************************************
		 *	ECS_ITEM_FASHION
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( OP, "colorCode",			"COLORCD",				"ECS_ITEM_COLORCODE",			0, 20 )
			, createFD( OP, "colorDescription",		"COLORDESC",			"ECS_ITEM_COLORDESCRIPTION",	DESC, 0, 70 )
			, createFD( OP, "sizeCode",				"SIZECD",				"ECS_ITEM_SIZECODE",			0, 20 )
			, createFD( OP, "sizeDescription",		"SIZEDESC",				"ECS_ITEM_SIZEDESCRIPTION",		DESC, 0, 70 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_FASHION, table = createTable("ECS_ITEM_FASHION", "IFS", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEM_ATTRIBUTE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( OP, "extraNumber1",			"NUMBER1",				"ECS_ITEM_EXTRANUMBER1",		DOUBLE )
			, createFD( OP, "extraNumber2",			"NUMBER2",				"ECS_ITEM_EXTRANUMBER2",		DOUBLE )
			, createFD( OP, "extraNumber3",			"NUMBER3",				"ECS_ITEM_EXTRANUMBER3",		DOUBLE )
			, createFD( OP, "extraNumber4",			"NUMBER4",				"ECS_ITEM_EXTRANUMBER4",		DOUBLE )
			, createFD( OP, "extraInd1",			"IND1",					"ECS_ITEM_EXTRAIND1",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd2",			"IND2",					"ECS_ITEM_EXTRAIND2",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd3",			"IND3",					"ECS_ITEM_EXTRAIND3",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd4",			"IND4",					"ECS_ITEM_EXTRAIND4",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd5",			"IND5",					"ECS_ITEM_EXTRAIND5",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd6",			"IND6",					"ECS_ITEM_EXTRAIND6",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd7",			"IND7",					"ECS_ITEM_EXTRAIND7",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd8",			"IND8",					"ECS_ITEM_EXTRAIND8",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd9",			"IND9",					"ECS_ITEM_EXTRAIND9",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd10",			"IND10",				"ECS_ITEM_EXTRAIND10",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd11",			"IND11",				"ECS_ITEM_EXTRAIND11",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraInd12",			"IND12",				"ECS_ITEM_EXTRAIND12",			"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "extraText1",			"TEXT1",				"ECS_ITEM_EXTRATEXT1",			DESC, 0, 40 )
			, createFD( OP, "extraText2",			"TEXT2",				"ECS_ITEM_EXTRATEXT2",			DESC, 0, 40 )
			, createFD( OP, "extraText3",			"TEXT3",				"ECS_ITEM_EXTRATEXT3",			DESC, 0, 40 )
			, createFD( OP, "extraText4",			"TEXT4",				"ECS_ITEM_EXTRATEXT4",			DESC, 0, 40 )
			, createFD( OP, "extraDate1",			"DATE1",				"ECS_ITEM_EXTRADATE1",			DATE )
			, createFD( OP, "extraDate2",			"DATE2",				"ECS_ITEM_EXTRADATE2",			DATE )
			, createFD( OP, "extraDate3",			"DATE3",				"ECS_ITEM_EXTRADATE3",			DATE )
			, createFD( OP, "extraDate4",			"DATE4",				"ECS_ITEM_EXTRADATE4",			DATE )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_ATTRIBUTE, table = createTable("ECS_ITEM_ATTRIBUTE", "IAT", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEM_FILE, ECS_ITEM_FILE_BFILE, ECS_ITEM_FILE_BLOB
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "fileClassCode",		"FILECLASSCD",			"ECS_ITEMFILE_FILECLASSCODE",	3, 3 )
			, createFD( MD, "fileName",				"FILENAME",				"ECS_ITEMFILE_FILENAME",		0, 40 )
			, createFD( OP, "fileSize",				"FILESIZE",				"ECS_ITEMFILE_FILESIZE",		INTEGER, 0, 99999999 )
			, createFD( MD, "fileType",				"FILETYPE",				"ECS_ITEMFILE_FILETYPE",		0, 30 )
			, createFD( MO, "status",				"STATUS",				"STATUS",						"ECS_ITEMFILE_STATUS_", "00,ER" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEM_FILE_BFILE, table = createTable("ECS_ITEM_FILE_BFILE", "IFL", tfields, "UPGDATE = SYSDATE") );
		putTable( ECS_ITEM_FILE_BLOB, table = createTable("ECS_ITEM_FILE_BLOB", "IFL", tfields, "UPGDATE = SYSDATE") );

		qfields = new QueryableField[] {
			  createQFD( T, "gtin",					"IFL.GTIN",				"GTIN",							STRING )
			, createQFD( T, "gln",					"IFL.GLN",				"GLN",							STRING )
			, createQFD( T, "fileClassCode",		"IFL.FILECLASSCD",		"ECS_ITEMFILE_FILECLASSCODE",	STRING )
			, createQFD( T, "fileClassName",		"FLC.FILECLASS_NAME",	"ECS_ITEMFILE_FILECLASSNAME",	STRING
					, new JoinableImpl( "FLC", "ECS_FILECLASS", "FLC.FILECLASS_CD(+) = IFL.FILECLASSCD" ) )
			, createQFD( T, "fileName",				"IFL.FILENAME",			"ECS_ITEMFILE_FILENAME",		STRING )
			, createQFD( T, "fileSize",				"IFL.FILESIZE",			"ECS_ITEMFILE_FILESIZE",		INTEGER )
			, createQFD( T, "fileType",				"IFL.FILETYPE",			"ECS_ITEMFILE_FILETYPE",		STRING )
			, createQFD( T, "status",				"IFL.STATUS",			"STATUS",						STRING )
			, createQFD( T, "createDateTime",		"IFL.REGDATE",			"CREATEDATETIME",				DATETIME )
			, createQFD( T, "updateDateTime",		"IFL.UPGDATE",			"UPDATEDATETIME",				DATETIME )
			, createQFD( T, "updateUserId",			"IFL.UPGUSERID",		"UPDATEUSERID",					STRING )
		};
		putQueryable( ECS_ITEM_FILE, new QueryableImpl(new JoinableImpl("IFL", "vwECS_ITEM_FILE"), qfields) );
		putQueryable( ECS_ITEM_FILE_BFILE, new QueryableImpl(new JoinableImpl("IFL", "ECS_ITEM_FILE_BFILE"), qfields) );
		putQueryable( ECS_ITEM_FILE_BLOB, new QueryableImpl(new JoinableImpl("IFL", "ECS_ITEM_FILE_BLOB"), qfields) );


		/***************************************************************************************************
		 *	ECS_ITEM_MNG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "gln",					"GLN",					"GLN",							0, 13 )
			, createFD( PM, "manageType",			"MNGTYPE",				"ECS_ITEMMNG_TYPE",				"ECS_ITEM_MANAGETYPE_", "RQ,RC,PS" )
			, createFD( MO, "effectiveChangeDate",	"EFFECTIVE_CHGDATE",	"ECS_ITEM_EFFECTIVECHANGEDATE",	DATE )
			, createFD( MR, "itemManageKey",		"ITEMMNG_KEY",			"ECS_ITEMMNG_MANAGEKEY",		10, 10 )
					.setInsertQuery( "NVL( ?, 'M' || seqECS_ITEM_MNG.NEXTVAL )" )
			, createFD( OP, "requestMessage",		"MESSAGE",				"ECS_ITEM_REQUESTMESSAGE",		DESC, 0, 255 )
			, createFD( OP, "requestDateTime",		"REQUEST_DATETIME",		"ECS_ITEM_REQUESTDATETIME",		DATETIME )
			, createFD( OP, "requestUserId",		"REQUEST_USERID",		"ECS_ITEM_REQUESTUSERID",		0, 30 )
			, createFD( OP, "approveDateTime",		"APPROVE_DATETIME",		"ECS_ITEM_APPROVEDATETIME",		DATETIME )
			, createFD( OP, "approveUserId",		"APPROVE_USERID",		"ECS_ITEM_APPROVEUSERID",		0, 30 )
			, createFD( MO, "manageStatus",			"STATUS",				"ECS_ITEM_MANAGESTATUS",		"ECS_ITEM_MANAGESTATUS_", "00,01" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_ITEM_MNG, table = createTable("ECS_ITEM_MNG", "IMNG", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ITEM_MNG, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( DESC, "requestUserName", "IMNG_RUSR.USER_NAME", "ECS_ITEM_REQUESTUSERNAME"
					, new JoinableImpl( "IMNG_RUSR", "USR_USER", "IMNG_RUSR.UNIQID(+) = IMNG.REQUEST_USERID" ) )
			, new QueryableFieldImpl( DESC, "approveUserName", "IMNG_AUSR.USER_NAME", "ECS_ITEM_APPROVEUSERNAME"
					, new JoinableImpl( "IMNG_AUSR", "USR_USER", "IMNG_AUSR.UNIQID(+) = IMNG.APPROVE_USERID" ) )
		}) );


		/***************************************************************************************************
		 *	ECS_ITEMTP
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( PM, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( OP, "buyerGtin",			"BUYERGTIN",			"ECS_ITEMTP_BUYERGTIN",			0, 25 )
			, createFD( OP, "sellerGtin",			"SELLERGTIN",			"ECS_ITEMTP_SELLERGTIN",		0, 25 )
			, createFD( MR, "tradeItemInfoType",	"ITEMINFO_TYPE",		"ECS_ITEMTP_ITEMINFOTYPE",		"ECS_ITEMTP_INFOTYPE_", "TP,LW" )
			, createFD( MD, "primaryInd",			"PRIMARY_IND",			"ECS_ITEMTP_PRIMARYIND",		"PUB_WHETHER_", "Y,N" )
			, createFD( RD, "deliveryCalendarKey",	"DELIVERYCAL_KEY",		"ECS_ITEMTP_DELIVERYCALENDARKEY", 10, 10 )
			, createFD( MD, "deliveryRoute",		"DELIVERYROUTE",		"ECS_ITEMTP_DELIVERYROUTE",		CODE )
					.setValidValueList( "ECS_DELIVERYROUTE_", "R1,R2,R3,NU,ML" )
			, createFD( OP, "supplierGln",			"SUPPLIERGLN",			"ECS_ITEMTP_SUPPLIERGLN",		0, 13 )
			, createFD( OP, "dcGln",				"DC_GLN",				"ECS_ITEMTP_DCGLN",				0, 13 )
			, createFD( OP, "destinationGln",		"DESTINATION_GLN",		"ECS_ITEMTP_DESTINATIONGLN",	0, 13 )
			, createFD( OP, "buyerItemPartnerCode",	"BUYER_PARTYCD",		"ECS_ITEMTP_BUYERPARTNERCODE",	0, 25, CHARS )
			, createFD( OP, "buyerUserId",			"BUYER_USERID",			"ECS_ITEMTP_BUYERUSERID",		0, 30 )
			, createFD( OP, "sellerItemPartnerCode","SELLER_PARTYCD",		"ECS_ITEMTP_SELLERPARTNERCODE",	0, 25, CHARS )
			, createFD( OP, "sellerUserId",			"SELLER_USERID",		"ECS_ITEMTP_SELLERUSERID",		0, 30 )
			, createFD( OP, "orderLeadTimeToDestination",	"ORDER_LEADTIME_TO_DEST",	"ECS_ITEMTP_ORDERLEADTIMETODESTINATION",INTEGER, 0, 999999 )
			, createFD( OP, "orderLeadTimeToBuyer",			"ORDER_LEADTIME_TO_BUYER",	"ECS_ITEMTP_ORDERLEADTIMETOBUYER",		INTEGER, 0, 999999 )
			, createFD( OP, "orderWeekday",			"ORDER_WEEKDAY",		"ECS_ITEMTP_ORDERWEEKDAY",		7, 7, "YN" )
			, createFD( OP, "orderPolicy",			"ORDER_POLICY",			"ECS_ITEMTP_ORDERPOLICY",		"ECS_ORDER_POLICY_", "ND,NO,IG" )
			, createFD( OP, "minimumPresStockQty",	"PRES_STOCKQTY_MIN",	"ECS_ITEMTP_PRESSTOCKQTY_MIN",	INTEGER, 0, 999999 )
			, createFD( OP, "maximumPresStockQty",	"PRES_STOCKQTY_MAX",	"ECS_ITEMTP_PRESSTOCKQTY_MAX",	INTEGER, 0, 999999 )
			, createFD( OR, "parentBuyerGln",		"PARENT_BUYERGLN",		"ECS_ITEMTP_PARENTBUYERGLN",	0, 13 )
			, createFD( OR, "parentSellerGln",		"PARENT_SELLERGLN",		"ECS_ITEMTP_PARENTSELLERGLN",	0, 13 )
			, createFD( MD, "effectiveChangeDate",	"EFFECTIVE_CHGDATE",	"ECS_ITEMTP_EFFECTIVECHANGEDATE", DATE )
			, createFD( OP, "tradeStartAvailDate",	"STARTAVAIL_DATETIME",	"ECS_ITEMTP_STARTAVAILDATE",	DATE )
			, createFD( OP, "tradeEndAvailDate",	"ENDAVAIL_DATETIME",	"ECS_ITEMTP_ENDAVAILDATE",		DATE )
			, createFD( OP, "discontinuedDate",		"DISCONTINUED_DATE",	"ECS_ITEMTP_DISCONTINUEDDATE",	DATE )
			, createFD( OP, "canceledDate",			"CANCELED_DATE",		"ECS_ITEMTP_CANCELEDDATE",		DATE )
			, createFD( MD, "hierarchyRefInd",		"HIERARCHY_REFIND",		"ECS_ITEMTP_HIERARCHYREFIND",	"ECS_ITEMTP_HIERARCHYIND_", "B,S,P" )
			, createFD( OP, "hierarchyRefGtin",		"HIERARCHY_REFGTIN",	"ECS_ITEMTP_HIERARCHYREFGTIN",	0, 25 )
			, createFD( OP, "hierarchyRefGln",		"HIERARCHY_REFGLN",		"ECS_ITEMTP_HIERARCHYREFGLN",	0, 13 )
			, createFD( OP, "tradePrice",			"TRADEPRICE",			"ECS_ITEMTP_TRADEPRICE",		DOUBLE, double_0, null )
			, createFD( OP, "tradePriceCurr",		"TRADEPRICE_CURR",		"ECS_ITEMTP_TRADEPRICECURR",	0, 3 )
			, createFD( MD, "priceManageInd",		"PRICEMNG_IND",			"ECS_ITEMTP_PRICEMANAGEIND",	"ECS_ITEMTP_PRICEMNGIND_", "P,S" )
			, createFD( OP, "extraValueTP",			"EXTRAVALUE",			"ECS_ITEMTP_EXTRAVALUE",		0, 128 )
			, createFD( MO, "tradeStatus",			"STATUS",				"ECS_ITEMTP_STATUS",			"ECS_ITEMTP_STATUS_", "00,NO,99" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEMTP, table = createTable("ECS_ITEMTP", "ITP", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ITEMTP, new QueryableImpl(table) {{
			Joinable tbl_ITOR = new JoinableImpl( "ITOR", "ECS_ITEMTP_ORDERING"
					, "ITOR.BUYERGLN(+) = ITP.BUYERGLN AND ITOR.SELLERGLN(+) = ITP.SELLERGLN AND ITOR.GTIN(+) = ITP.GTIN" );

			append( makePartyQueryableFields( "buyer", "PBY", "PBY.GLN(+) = ITP.BUYERGLN" ) );
			append( makePartyQueryableFields( "seller", "PSL", "PSL.GLN(+) = ITP.SELLERGLN" ) );
			append( makePartyQueryableFields( "supplier", "PSP", "PSP.GLN(+) = ITP.SUPPLIERGLN" ) );
			append( makePartyQueryableFields( "dc", "PDC", "PDC.GLN(+) = ITP.DC_GLN" ) );
			append( makePartyQueryableFields( "destination", "PDT", "PDT.GLN(+) = ITP.DESTINATION_GLN" ) );
			append( makePartyQueryableFields( "parentBuyer", "PPBY", "PPBY.GLN(+) = ITP.PARENT_BUYERGLN" ) );
			append( makePartyQueryableFields( "parentSeller", "PPSL", "PPSL.GLN(+) = ITP.PARENT_SELLERGLN" ) );
			append( makeTPAuthQueryableFields( "ITP", "ITP" ) );
			append( new QueryableField[] {
				new QueryableFieldImpl( CODE, "existOrderInfo", "NVL2(ITOR.GTIN, 'Y', 'N')", "ECS_ITEMTP_EXISTORDERINFO", tbl_ITOR )
			  , new QueryableFieldImpl( INTEGER, "childTradeItemCount"
					, "(SELECT COUNT(*) FROM ECS_ITEMTP SB WHERE SB.PARENT_BUYERGLN = ITP.BUYERGLN AND SB.PARENT_SELLERGLN = ITP.SELLERGLN "
							+" AND SB.GTIN = ITP.GTIN)" )
			  , new QueryableFieldImpl( INTEGER, "childTradeItemCount00"
					, "(SELECT COUNT(*) FROM ECS_ITEMTP SB WHERE SB.PARENT_BUYERGLN = ITP.BUYERGLN AND SB.PARENT_SELLERGLN = ITP.SELLERGLN "
							+" AND SB.GTIN = ITP.GTIN AND SB.STATUS = '00')" )
			  , new QueryableFieldImpl( INTEGER, "tradeLocationCount"
					, "(SELECT COUNT(DISTINCT SB.BUYERGLN) FROM ECS_ITEMTP SB "
							+" WHERE SB.PARENT_BUYERGLN = ITP.BUYERGLN AND SB.PARENT_SELLERGLN = ITP.SELLERGLN AND SB.GTIN = ITP.GTIN)" )
			  , new QueryableFieldImpl( STRING, "tradePriceName"
					, "(SELECT SB.CURR_NAME FROM SYS_CURRENCY SB WHERE SB.CURR_CD = ITP.TRADEPRICE_CURR)", "ECS_ITEMTP_TRADEPRICECURR" )
			  , new QueryableFieldImpl( STRING, "tradePriceSymbol"
					, "(SELECT SB.CURR_SYMBOL FROM SYS_CURRENCY SB WHERE SB.CURR_CD = ITP.TRADEPRICE_CURR)", "ECS_ITEMTP_TRADEPRICECURR" )
			} );
		}} );


		/***************************************************************************************************
		 *	ECS_ITEMTP_INFO
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( PM, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( OP, "isDespatchUnit",		"DESPATCH_IND",			"ECS_ITEMTP_ISDESPATCHUNIT",	"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isInvoiceUnit",		"INVOICE_IND",			"ECS_ITEMTP_ISINVOICEUNIT",		"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isOrderableUnit",		"ORDERABLE_IND",		"ECS_ITEMTP_ISORDERABLEUNIT",	"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "palletTypeCode",		"PALLET_TYPECODE",		"ECS_ITEMTP_PALLETTYPECODE",	3, 3 )
			, createFD( OP, "palletTermsAndConditions",	"PALLET_TERMSANDCONDS",	"ECS_ITEMTP_PALLETTERMSANDCONDITIONS",	2, 2 )
			, createFD( OP, "isNonSoldReturnable",		"ISNONSOLD_RETURNABLE",	"ECS_ITEMTP_ISNONSOLDRETURNABLE",		"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "isMarkedAsRecyclable",		"ISMARKED_RECYCLABLE",	"ECS_ITEMTP_ISMARKEDASRECYCLABLE",		"PUB_WHETHER_", "Y,N" )
			, createFD( OP, "minimumLifespan",		"MIN_LIFESPAN",			"ECS_ITEMTP_MINIMUMLIFESPAN",	INTEGER, 0, 999999 )
			, createFD( OP, "consignmentRate",		"CONSIGNMENT_RATE",		"ECS_ITEMTP_CONSIGNMENTRATE",	(double)0.0, (double)100.0 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEMTP_INFO, table = createTable("ECS_ITEMTP_INFO", "ITIF", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEMTP_ORDERING
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( PM, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( OP, "orderingLeadTime",		"ORDER_LEADTIME",		"ECS_ITEM_ORDERINGLEADTIME",	INTEGER, 0, 999999 )
			, createFD( OP, "orderUnit",			"ORDER_UNIT",			"ECS_ITEM_ORDERUNIT",			"ECS_ITEMUNIT_", "EA,PK,CA,PL" )
			, createFD( OP, "minimumOrderQty",		"ORDER_MINQTY",			"ECS_ITEM_MINIMUMORDERQTY",		INTEGER, 0, 999999999 )
			, createFD( OP, "maximumOrderQty",		"ORDER_MAXQTY",			"ECS_ITEM_MAXIMUMORDERQTY",		INTEGER, 0, 999999999 )
			, createFD( OP, "orderQtyMultiple",		"ORDER_MULTIPLEQTY",	"ECS_ITEM_ORDERQTYMULTIPLE",	INTEGER, 0, 999999999, EQU_NY )
			, createFD( OP, "roundLevel",			"ROUND_LEVEL",			"ECS_ITEMTP_ROUNDLEVEL",		0, 10 )
			, createFD( OP, "roundToInnerPackPercent", "ROUND_TO_INNER_PCT","ECS_ITEMTP_ROUNDPCT_INNER",	(double)0.0, (double)100.0 )
			, createFD( OP, "roundToCasePercent",	"ROUND_TO_CASE_PCT",	"ECS_ITEMTP_ROUNDPCT_CASE",		(double)0.0, (double)100.0 )
			, createFD( OP, "roundToLayerPercent",	"ROUND_TO_LAYER_PCT",	"ECS_ITEMTP_ROUNDPCT_LAYER",	(double)0.0, (double)100.0 )
			, createFD( OP, "roundToPalletPercent",	"ROUND_TO_PALLET_PCT",	"ECS_ITEMTP_ROUNDPCT_PALLET",	(double)0.0, (double)100.0 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEMTP_ORDERING, table = createTable("ECS_ITEMTP_ORDERING", "ITOR", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEMTP_PRICE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( PM, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "priceTypeCode",		"PRICETYPECD",			"ECS_ITEM_PRICETYPECODE",		3, 3 )
			, createFD( PM, "effectiveStartDate",	"EFFECTIVE_STARTDATE",	"ECS_ITEM_EFFECTIVESTARTDATE",	DATE )
			, createFD( OP, "effectiveEndDate",		"EFFECTIVE_ENDDATE",	"ECS_ITEM_EFFECTIVEENDDATE",	DATE )
			, createFD( MD, "price",				"PRICE",				"ECS_ITEM_PRICE",				DOUBLE, double_0, null )
			, createFD( MD, "priceCurr",			"PRICE_CURR",			"ECS_ITEM_PRICECURR",			3, 3 )
			, createFD( OP, "reasonOfPriceChangeCode", "CHANGE_REASON",		"ECS_ITEM_REASONOFPRICECHANGECODE", 0, 10 )
			, createFD( MO, "status",				"STATUS",				"ECS_ITEM_PRICE_STATUS",		CODE )
					.setValidValueList( "ECS_ITEM_PRICE_STATUS_", "00,01,02,03,10,11" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
			, createFD( OP, "updateUserId",			"UPGUSERID",			"UPDATEUSERID",					0, 30 )
		};
		putTable( ECS_ITEMTP_PRICE, table = createTable("ECS_ITEMTP_PRICE", "ITPR", tfields, "UPGDATE = SYSDATE") );


		/***************************************************************************************************
		 *	ECS_ITEMTP_SELLERCODE
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( PM, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "sellerPartnerCode",	"SELLER_PARTYCD",		"ECS_ITEMTP_SELLERPARTNERCODE",	0, 25, CHARS )
			, createFD( OP, "tradePrice",			"TRADEPRICE",			"ECS_ITEMTP_TRADEPRICE",		DOUBLE, double_0, null )
			, createFD( OP, "tradePriceCurr",		"TRADEPRICE_CURR",		"ECS_ITEMTP_TRADEPRICECURR",	0, 3 )
			, createFD( OP, "consignmentRate",		"CONSIGNMENT_RATE",		"ECS_ITEMTP_CONSIGNMENTRATE",	(double)0.0, (double)100.0 )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_ITEMTP_SELLERCODE, table = createTable("ECS_ITEMTP_SLCODE", "ITSL", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ITEMTP_SELLERCODE, new QueryableImpl(table, new QueryableField[] {
			    new QueryableFieldImpl( STRING, "tradePriceName"
					, "(SELECT SB.CURR_NAME FROM SYS_CURRENCY SB WHERE SB.CURR_CD = ITP.TRADEPRICE_CURR)", "ECS_ITEMTP_TRADEPRICECURR" )
			  , new QueryableFieldImpl( STRING, "tradePriceSymbol"
					, "(SELECT SB.CURR_SYMBOL FROM SYS_CURRENCY SB WHERE SB.CURR_CD = ITP.TRADEPRICE_CURR)", "ECS_ITEMTP_TRADEPRICECURR" )
		}) );


		/***************************************************************************************************
		 *	ECS_ITEMTP_MNG
		***************************************************************************************************/
		tfields = new Table.Field[] {
			  createFD( PM, "buyerGln",				"BUYERGLN",				"BUYERGLN",						0, 13 )
			, createFD( PM, "sellerGln",			"SELLERGLN",			"SELLERGLN",					0, 13 )
			, createFD( PM, "gtin",					"GTIN",					"GTIN",							0, 25 )
			, createFD( PM, "manageType",			"MNGTYPE",				"ECS_ITEMMNG_TYPE",				"ECS_ITEMTP_MANAGETYPE_", "RQ,RC,PS" )
			, createFD( MO, "effectiveChangeDate",	"EFFECTIVE_CHGDATE",	"ECS_ITEMTP_EFFECTIVECHANGEDATE", DATE )
			, createFD( MR, "itemManageKey",		"ITEMMNG_KEY",			"ECS_ITEMMNG_MANAGEKEY",		10, 10 )
					.setInsertQuery( "NVL( ?, 'M' || seqECS_ITEM_MNG.NEXTVAL )" )
			, createFD( OP, "requestMessage",		"MESSAGE",				"ECS_ITEMTP_REQUESTMESSAGE",	DESC, 0, 255 )
			, createFD( OP, "requestDateTime",		"REQUEST_DATETIME",		"ECS_ITEMTP_REQUESTDATETIME",	DATETIME )
			, createFD( OP, "requestUserId",		"REQUEST_USERID",		"ECS_ITEMTP_REQUESTUSERID",		0, 30 )
			, createFD( OP, "approveDateTime",		"APPROVE_DATETIME",		"ECS_ITEMTP_APPROVEDATETIME",	DATETIME )
			, createFD( OP, "approveUserId",		"APPROVE_USERID",		"ECS_ITEMTP_APPROVEUSERID",		0, 30 )
			, createFD( OP, "buyerGtin",			"BUYERGTIN",			"ECS_ITEMTP_BUYERGTIN",			0, 25 )
			, createFD( OP, "sellerGtin",			"SELLERGTIN",			"ECS_ITEMTP_SELLERGTIN",		0, 25 )
			, createFD( MO, "tradeManageStatus",	"STATUS",				"ECS_ITEMTP_MANAGESTATUS",		CODE )
					.setValidValueList( "ECS_ITEMTP_MANAGESTATUS_", "RQ,01,RJ,WK,00" )
			, createFD( RD, "createDateTime",		"REGDATE",				"CREATEDATETIME",				DATETIME )
			, createFD( RD, "updateDateTime",		"UPGDATE",				"UPDATEDATETIME",				DATETIME )
		};
		putTable( ECS_ITEMTP_MNG, table = createTable("ECS_ITEMTP_MNG", "ITMNG", tfields, "UPGDATE = SYSDATE") );
		putQueryable( ECS_ITEMTP_MNG, new QueryableImpl(table, new QueryableField[] {
			new QueryableFieldImpl( DESC, "requestUserName", "ITMNG_RUSR.USER_NAME", "ECS_ITEM_REQUESTUSERNAME"
					, new JoinableImpl( "ITMNG_RUSR", "USR_USER", "ITMNG_RUSR.UNIQID(+) = ITMNG.REQUEST_USERID" ) )
			, new QueryableFieldImpl( DESC, "approveUserName", "ITMNG_AUSR.USER_NAME", "ECS_ITEM_APPROVEUSERNAME"
					, new JoinableImpl( "ITMNG_AUSR", "USR_USER", "ITMNG_AUSR.UNIQID(+) = ITMNG.APPROVE_USERID" ) )
		}) );
	}

	static HierarchyCodeField createPartyCategoryCodeField( int[] codeLengths ) {
		String classCodeValue = "";
		for( int i = 1; i <= codeLengths.length; i++ )
			classCodeValue = ","+ i;

		Table.Field fld_classcd = createFD( MR, "classCode", "CLASSCD", "CLASSCODE", "PUB_CLASSCODE_", classCodeValue.substring(1) );

		return new HierarchyCodeField( createFD(PM, "code", "CATE_CD", "CODE", 0, 20, CHARS), fld_classcd, codeLengths );
	}

	public static QueryableField[] makeItemQueryableFields( String prefix, String alias, String conditionQuery ) {
		return makeItemQueryableFields( prefix, alias, new JoinableImpl(alias, "ECS_ITEM", conditionQuery) );
	}

	public static QueryableField[] makeItemQueryableFields( String prefix, String alias, Joinable joinable ) {
		if( prefix == null ) {
			Joinable tbl_CATE = new JoinableImpl( "CATE", "SYS_CATE", "CATE.CATE_CD(+) = "+ alias +".CATECD", joinable );
			Joinable tbl_ICATE = new JoinableImpl( "ICATE", "SYS_ICATE", "ICATE.ICATE_CD(+) = "+ alias +".ICATECD", joinable );

			return new QueryableField[] {
				  new QueryableFieldImpl( STRING, "itemCode",			alias +".ITEMCODE",		"ECS_ITEM_ITEMCODE",			joinable )
				, new QueryableFieldImpl( STRING, "categoryCode",		alias +".CATECD",		"ECS_ITEM_CATEGORYCODE",		joinable )
				, new QueryableFieldImpl( STRING, "iCategoryCode",		alias +".ICATECD",		"ECS_ITEM_ICATEGORYCODE",		joinable )
				, new QueryableFieldImpl( DESC, "itemName",				alias +".ITEMNAME",		"ECS_ITEM_ITEMNAME",			joinable )
				, new QueryableFieldImpl( DESC, "shortDescription",		alias +".SHORTDESC",	"ECS_ITEM_SHORTDESCRIPTION",	joinable )
				, new QueryableFieldImpl( DESC, "categoryName",			"CATE.CATE_NAME",		"ECS_ITEM_CATEGORY",			tbl_CATE )
				, new QueryableFieldImpl( DESC, "iCategoryName",		"ICATE.ICATE_NAME",		"ECS_ITEM_ICATEGORY",			tbl_ICATE )
			};
		} else {
			return new QueryableField[] {
				  new QueryableFieldImpl( STRING, prefix +"ItemCode",			alias +".ITEMCODE",		joinable )
				, new QueryableFieldImpl( DESC, prefix +"ItemName",				alias +".ITEMNAME",		joinable )
				, new QueryableFieldImpl( DESC, prefix +"ShortDescription",		alias +".SHORTDESC",	joinable )
			};
		}
	}

	public static QueryableField[] makePartyQueryableFields( String prefix, String alias, String conditionQuery ) {
		return makePartyQueryableFields( prefix, alias, new JoinableImpl(alias, "ECS_PARTY", conditionQuery) );
	}

	public static QueryableField[] makePartyQueryableFields( String prefix, String alias, Joinable joinable ) {
		if( prefix == null ) {
			return new QueryableField[] {
				  new QueryableFieldImpl( CODE, "partyRole",			alias +".PARTY_ROLE",			"ECS_PARTY_PARTYROLE",			joinable )
				, new QueryableFieldImpl( DESC, "parentGln",			alias +".PARENT_GLN",			"ECS_PARTY_PARENTGLN",			joinable )
				, new QueryableFieldImpl( DESC, "partyCode",			alias +".PARTY_CD",				"ECS_PARTY_PARTYCODE",			joinable )
				, new QueryableFieldImpl( DESC, "companyName",			alias +".COMPANY_NAME",			"ECS_PARTY_COMPANYNAME",		joinable )
				, new QueryableFieldImpl( DESC, "locationName",			alias +".LOCATION_NAME",		"ECS_PARTY_LOCATIONNAME",		joinable )
				, new QueryableFieldImpl( DESC, "locationShortName",	alias +".LOCATION_SHORTNAME",	"ECS_PARTY_LOCATIONSHORTNAME",	joinable )
			};
		} else {
			return new QueryableField[] {
				  new QueryableFieldImpl( CODE, prefix +"PartyRole",			alias +".PARTY_ROLE",			joinable )
				, new QueryableFieldImpl( DESC, prefix +"PartyCode",			alias +".PARTY_CD",				joinable )
				, new QueryableFieldImpl( DESC, prefix +"CompanyName",			alias +".COMPANY_NAME",			joinable )
				, new QueryableFieldImpl( DESC, prefix +"LocationName",			alias +".LOCATION_NAME",		joinable )
				, new QueryableFieldImpl( DESC, prefix +"LocationShortName",	alias +".LOCATION_SHORTNAME",	joinable )
			};
		}
	}

	public static QueryableField[] makeTPAuthQueryableFields( String alias ) {
		Joinable joinable = new JoinableImplBK( "TPA_P", "ECS_TRADEPARTNER_AUTH"
				, "TPA_P.BUYERGLN(+) = "+ alias +".BUYERGLN AND TPA_P.SELLERGLN(+) = "+ alias +".SELLERGLN AND TPA_P.USERID(+) = ?"
				, new String[] { "authUserId" } );
		QueryableField[] qfields = makeTPAuthQueryableFields( true, joinable );

		return ConditionalQueryableField.makeConditionalQueryableFields( new QueryBufferValid.Condition("authUserId"), qfields );
	}

	public static QueryableField[] makeTPAuthQueryableFields( String alias, String aliasP ) {
		return makeTPAuthQueryableFields( alias, aliasP, null );
	}

	public static QueryableField[] makeTPAuthQueryableFields( String alias, String aliasP, Joinable joinableTP ) {
		Joinable tbl_TPA = new JoinableImplBK( "TPA", "ECS_TRADEPARTNER_AUTH"
				, "TPA.BUYERGLN(+) = "+ alias +".BUYERGLN AND TPA.SELLERGLN(+) = "+ alias +".SELLERGLN AND TPA.USERID(+) = ?"
				, new String[] { "authUserId" } );
		Joinable tbl_TPA_P = new JoinableImplBK( "TPA_P", "ECS_TRADEPARTNER_AUTH"
				, "TPA_P.BUYERGLN(+) = "+ aliasP +".PARENT_BUYERGLN AND TPA_P.SELLERGLN(+) = "+ aliasP +".PARENT_SELLERGLN AND TPA_P.USERID(+) = ?"
				, new String[] { "authUserId" } );

		QueryableField[] qfields;
		if( alias.equals(aliasP) )
			qfields = makeTPAuthQueryableFields( false, new JoinableWrapper( tbl_TPA, tbl_TPA_P ) );
		else {
			if( joinableTP == null )
				joinableTP = new JoinableImpl( aliasP, "ECS_TRADEPARTNER"
						, aliasP +".BUYERGLN(+) = "+ alias +".BUYERGLN AND "+ aliasP +".SELLERGLN(+) = "+ alias +".SELLERGLN" );
			qfields = makeTPAuthQueryableFields( false, new JoinableWrapper( tbl_TPA, tbl_TPA_P, joinableTP ) );
		}

		return ConditionalQueryableField.makeConditionalQueryableFields( new QueryBufferValid.Condition("authUserId"), qfields );
	}

	public static QueryableField[] makeTPAuthQueryableFields( boolean isParentLevel, Joinable joinable ) {
		String prefixKey = "ECS_TPAUTH_VALUE_";
		String authValue1, authValue2, manageType, authLocationCount;

		if( isParentLevel ) {
			manageType = "TPA_P.MNGTYPE";
			authValue1 = "TPA_P.AUTHVALUE1";
			authValue2 = "TPA_P.AUTHVALUE2";
		} else {
			manageType = "NVL2(TPA.AUTHVALUE1, TPA.MNGTYPE, DECODE(TPA_P.MNGTYPE, 'A', 'X', 'P', NVL2(TPA.MNGTYPE, 'X', NULL), TPA.MNGTYPE))";
			authValue1 = "NVL(TPA.AUTHVALUE1, DECODE(TPA_P.MNGTYPE, 'A', TPA_P.AUTHVALUE1, 'P', NVL2(TPA.MNGTYPE, TPA_P.AUTHVALUE1, NULL), NULL))";
			authValue2 = "NVL(TPA.AUTHVALUE2, DECODE(TPA_P.MNGTYPE, 'A', TPA_P.AUTHVALUE2, 'P', NVL2(TPA.MNGTYPE, TPA_P.AUTHVALUE2, NULL), NULL))";
		}

		authLocationCount =
			"DECODE( TPA.MNGTYPE, 'A'"
				+", (SELECT COUNT(*) FROM ECS_TRADEPARTNER SB WHERE SB.PARENT_BUYERGLN = TPA.BUYERGLN AND SB.PARENT_SELLERGLN = TPA.SELLERGLN)"
				+", (SELECT COUNT(*) FROM ECS_TRADEPARTNER_AUTH SB "
						+" WHERE SB.PARENT_BUYERGLN = TPA.BUYERGLN AND SB.PARENT_SELLERGLN = TPA.SELLERGLN AND SB.USERID = TPA.USERID) )";

		if( joinable == null ) {
			return new QueryableField[] {
				  createQFD( T, "manageType",			manageType,							"ECS_TPAUTH_MANAGETYPE",			CODE )
				, createQFD( T, "authValue1",			authValue1,							"ECS_TPAUTH_AUTHVALUE1" )
				, createQFD( T, "authValue1TP",			"SUBSTRB("+ authValue1 +", 1, 1)",	"ECS_TPAUTH_AUTHVALUE1_TP",			prefixKey )
				, createQFD( T, "authValue1Operday",	"SUBSTRB("+ authValue1 +", 2, 1)",	"ECS_TPAUTH_AUTHVALUE1_OPERDAY",	prefixKey )
				, createQFD( T, "authValue1Item",		"SUBSTRB("+ authValue1 +", 3, 1)",	"ECS_TPAUTH_AUTHVALUE1_ITEM",		prefixKey )
				, createQFD( T, "authValue2",			authValue2,							"ECS_TPAUTH_AUTHVALUE2" )
				, createQFD( T, "authValue2Scenario",	"SUBSTRB("+ authValue2 +", 1, 1)",	"ECS_TPAUTH_AUTHVALUE2_SCENARIO",	prefixKey )
				, createQFD( T, "authValue2Item",		"SUBSTRB("+ authValue2 +", 2, 1)",	"ECS_TPAUTH_AUTHVALUE2_ITEM",		prefixKey )
				, createQFD( T, "authValue2SFC",		"SUBSTRB("+ authValue2 +", 3, 1)",	"ECS_TPAUTH_AUTHVALUE2_SFC",		prefixKey )
				, createQFD( T, "authValue2OFC",		"SUBSTRB("+ authValue2 +", 4, 1)",	"ECS_TPAUTH_AUTHVALUE2_OFC",		prefixKey )
				, createQFD( T, "authValue2Event",		"SUBSTRB("+ authValue2 +", 5, 1)",	"ECS_TPAUTH_AUTHVALUE2_EVENT",		prefixKey )
				, createQFD( T, "authValue2Inventory",	"SUBSTRB("+ authValue2 +", 6, 1)",	"ECS_TPAUTH_AUTHVALUE2_INVENTORY",	prefixKey )
				, createQFD( T, "authValue2Order",		"SUBSTRB("+ authValue2 +", 7, 1)",	"ECS_TPAUTH_AUTHVALUE2_ORDER",		prefixKey )
				, createQFD( T, "authValue2OrderManual","SUBSTRB("+ authValue2 +", 8, 1)",	"ECS_TPAUTH_AUTHVALUE2_ORDERMANUAL",prefixKey )
				, createQFD( T, "authValue2KPI",		"SUBSTRB("+ authValue2 +", 9, 1)",	"ECS_TPAUTH_AUTHVALUE2_KPI",		prefixKey )
				, createQFD( T, "authLocationCount",	authLocationCount,					"ECS_TPAUTH_AUTHLOCATIONCOUNT" )
			};
		} else {
			return new QueryableField[] {
				  createQFD( T, "manageType",			manageType,							"ECS_TPAUTH_MANAGETYPE",			CODE, joinable )
				, createQFD( T, "authValue1",			authValue1,							"ECS_TPAUTH_AUTHVALUE1",			joinable )
				, createQFD( T, "authValue1TP",			"SUBSTRB("+ authValue1 +", 1, 1)",	"ECS_TPAUTH_AUTHVALUE1_TP",			prefixKey, joinable )
				, createQFD( T, "authValue1Operday",	"SUBSTRB("+ authValue1 +", 2, 1)",	"ECS_TPAUTH_AUTHVALUE1_OPERDAY",	prefixKey, joinable )
				, createQFD( T, "authValue1Item",		"SUBSTRB("+ authValue1 +", 3, 1)",	"ECS_TPAUTH_AUTHVALUE1_ITEM",		prefixKey, joinable )
				, createQFD( T, "authValue2",			authValue2,							"ECS_TPAUTH_AUTHVALUE2",			joinable )
				, createQFD( T, "authValue2Scenario",	"SUBSTRB("+ authValue2 +", 1, 1)",	"ECS_TPAUTH_AUTHVALUE2_SCENARIO",	prefixKey, joinable )
				, createQFD( T, "authValue2Item",		"SUBSTRB("+ authValue2 +", 2, 1)",	"ECS_TPAUTH_AUTHVALUE2_ITEM",		prefixKey, joinable )
				, createQFD( T, "authValue2SFC",		"SUBSTRB("+ authValue2 +", 3, 1)",	"ECS_TPAUTH_AUTHVALUE2_SFC",		prefixKey, joinable )
				, createQFD( T, "authValue2OFC",		"SUBSTRB("+ authValue2 +", 4, 1)",	"ECS_TPAUTH_AUTHVALUE2_OFC",		prefixKey, joinable )
				, createQFD( T, "authValue2Event",		"SUBSTRB("+ authValue2 +", 5, 1)",	"ECS_TPAUTH_AUTHVALUE2_EVENT",		prefixKey, joinable )
				, createQFD( T, "authValue2Inventory",	"SUBSTRB("+ authValue2 +", 6, 1)",	"ECS_TPAUTH_AUTHVALUE2_INVENTORY",	prefixKey, joinable )
				, createQFD( T, "authValue2Order",		"SUBSTRB("+ authValue2 +", 7, 1)",	"ECS_TPAUTH_AUTHVALUE2_ORDER",		prefixKey, joinable )
				, createQFD( T, "authValue2OrderManual","SUBSTRB("+ authValue2 +", 8, 1)",	"ECS_TPAUTH_AUTHVALUE2_ORDERMANUAL",prefixKey, joinable )
				, createQFD( T, "authValue2KPI",		"SUBSTRB("+ authValue2 +", 9, 1)",	"ECS_TPAUTH_AUTHVALUE2_KPI",		prefixKey, joinable )
				, createQFD( T, "authLocationCount",	authLocationCount,					"ECS_TPAUTH_AUTHLOCATIONCOUNT" )
			};
		}
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
