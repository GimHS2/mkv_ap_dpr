/***********************************************************************************************************************
	script(using)
		classURL
		getLocationURL()
		windowOpen( url, winname )
		windowSelfOpen( url, backURL )
***********************************************************************************************************************/


/***********************************************************************************************************************
	_info( url, self )
***********************************************************************************************************************/
function _info( url, self ) {
	if( self )
		windowSelfOpen( url, getLocationURL() );
	else
		windowOpen( url +"&wintype=sub", "clsMng" );
}


/***********************************************************************************************************************
	_selectCode( url, slname, attr, value, winname, naming )
	_selectClassCode( url, slname, slcls, attr, value, winname, naming )
***********************************************************************************************************************/
function _selectCode( url, slname, attr, value, winname, naming ) {
	url += ( url.indexOf("?") > 0 ? "&" : "?" );
	url += "slname="+ slname + ( attr ? "&attr="+ attr : "" );
	if( naming )
		windowOpen( url +"&mode=name&code="+ encodeURIComponent(value), winname ? winname : "clsName" );
	else {
		url += ( value ? "&slcode="+ encodeURIComponent(value) : "" );
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );
	}
	return false;
}


function _selectClassCode( url, slname, slcls, attr, value, winname, naming ) {
	url += ( url.indexOf("?") > 0 ? "&" : "?" );
	url += "slname="+ slname + ( slcls ? "&slcls="+ slcls : "" ) + ( attr ? "&attr="+ attr : "" );
	if( naming )
		windowOpen( url +"&mode=name&code="+ encodeURIComponent(value), winname ? winname : "clsName" );
	else {
		url += ( value ? "&slcode="+ encodeURIComponent(value) : "" );
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );
	}
	return false;
}


/***********************************************************************************************************************
	����: USR
		userPartyInfo( partyid, self )
		userUserInfo( uniqid, self )
***********************************************************************************************************************/
function userPartyInfo( partyid, self ) {
	_info( classURL +"USRParty?mode=info&partyId="+ encodeURIComponent(partyid), self );
}

function userUserInfo( uniqid, self ) {
	_info( classURL +"USRUser?mode=info&uniqId="+ encodeURIComponent(uniqid), self );
}


/***********************************************************************************************************************
	����: SYS
		namingCate( slname, slcls, attr, value, winname )
		namingCode( type, slname, attr, value, winname )
		namingHS( slname, slcls, attr, value, winname )
		namingICate( slname, slcls, attr, value, winname )
		namingSPSC( slname, slcls, attr, value, winname )
		namingStdCode( type, slname, attr, value, winname )
		selectCate( slname, slcls, attr, value, winname )
		selectCode( type, slname, attr, value, winname )
		selectHS( slname, slcls, attr, value, winname )
		selectICate( slname, slcls, attr, value, winname )
		selectPost( slname, countryCode, winname )
		selectSPSC( slname, slcls, attr, value, winname )
		selectStdCode( type, slname, attr, value, winname )
***********************************************************************************************************************/
function selectCate( slname, slcls, attr, value, winname ) {
	_selectClassCode( classURL +"SYSClassCode?type=cate", slname, slcls, attr, value, winname );
}
function namingCate( slname, slcls, attr, value, winname ) {
	return _selectClassCode( classURL +"SYSClassCode?type=cate", slname, slcls, attr, value, winname, true );
}

function selectCode( type, slname, attr, value, winname ) {
	_selectCode( classURL +"SYSCode?type="+ type, slname, attr, value, winname );
}
function namingCode( type, slname, attr, value, winname ) {
	return _selectCode( classURL +"SYSCode?type="+ type, slname, attr, value, winname, true );
}

function selectHS( slname, slcls, attr, value, winname ) {
	_selectClassCode( classURL +"SYSClassCode?type=hs", slname, slcls, attr, value, winname );
}
function namingHS( slname, slcls, attr, value, winname ) {
	return _selectClassCode( classURL +"SYSClassCode?type=hs", slname, slcls, attr, value, winname, true );
}

function selectICate( slname, slcls, attr, value, winname ) {
	_selectClassCode( classURL +"SYSClassCode?type=icate", slname, slcls, attr, value, winname );
}
function namingICate( slname, slcls, attr, value, winname ) {
	return _selectClassCode( classURL +"SYSClassCode?type=icate", slname, slcls, attr, value, winname, true );
}

function selectPost( slname, countryCode, winname ) {
	windowOpen( classURL +"SYSPostalCode?mode=sel&wintype=sub&countryCode="+ countryCode +"&slname="+ slname, winname ? winname : "clsSel" );
}

function selectSPSC( slname, slcls, attr, value, winname ) {
	_selectClassCode( classURL +"SYSClassCode?type=spsc", slname, slcls, attr, value, winname );
}
function namingSPSC( slname, slcls, attr, value, winname ) {
	return _selectClassCode( classURL +"SYSClassCode?type=spsc", slname, slcls, attr, value, winname, true );
}

function selectStdCode( type, slname, attr, value, winname ) {
	_selectCode( classURL +"SYSStdCode?type="+ type, slname, attr, value, winname );
}
function namingStdCode( type, slname, attr, value, winname ) {
	return _selectCode( classURL +"SYSStdCode?type="+ type, slname, attr, value, winname, true );
}


/***********************************************************************************************************************
	����: USR
		_selectUserParty( type, slname, attr, value, winname, naming )
		_selectUserUser( slname, namecls, attr, value, winname, naming )
		namingUserParty( slname, attr, value, winname )
		namingUserUser( slname, namecls, attr, value, winname )
		selectUserParty( slname, attr, winname )
		selectUserUser( slname, namecls, attr, winname )
***********************************************************************************************************************/
function _selectUserParty( slname, attr, value, winname, naming ) {
	var url = classURL +"USRParty?slname="+ slname;
	url += ( attr ? "&attr="+ attr : "" );
	if( naming )
		windowOpen( url +"&mode=name&partyId="+ encodeURIComponent(value), winname ? winname : "clsName" );
	else
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );
	return false;
}
function selectUserParty( slname, attr, winname ) {
	_selectUserParty( slname, attr, null, winname );
}
function namingUserParty( slname, attr, value, winname ) {
	return _selectUserParty( slname, attr, value, winname, true );
}

function _selectUserUser( slname, namecls, attr, value, winname, naming ) {
	var url = classURL +"USRUser?slname="+ slname;
	url += ( namecls ? "&namecls="+ namecls : "" );
	url += ( attr ? "&attr="+ attr : "" );
	if( naming ) {
		if( namecls == 'Q' )
			windowOpen( url +"&mode=name&uniqId="+ encodeURIComponent(value), winname ? winname : "clsName" );
		else
			windowOpen( url +"&mode=name&userId="+ encodeURIComponent(value), winname ? winname : "clsName" );
	} else
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );
	return false;
}
function selectUserUser( slname, namecls, attr, winname ) {
	_selectUserUser( slname, namecls, attr, null, winname );
}
function namingUserUser( slname, namecls, attr, value, winname ) {
	return _selectUserUser( slname, namecls, attr, value, winname, true );
}


/***********************************************************************************************************************
	����: ECS
		_selectItem( slname, namecls, attr, gtin, gln, winname, naming )
		_selectManuf( slname, attr, value, winname, naming )
		_selectParty( slname, namecls, attr, value, winname, naming )
		_selectStore( slname, attr, value, winname, naming )
		namingItem( slname, namecls, attr, gtin, gln, winname )
		namingManuf( slname, attr, value, winname )
		namingOrigin( slname, attr, value, winname )
		namingParty( slname, namecls, attr, value, winname )
		namingSeason( slname, slcls, attr, value, winname )
		namingStore( slname, attr, value, winname )
		selectItem( slname, namecls, attr, winname )
		selectManuf( slname, attr, value, winname )
		selectOrigin( slname, attr, value, winname )
		selectParty( slname, namecls, attr, value, winname )
		selectSeason( slname, slcls, attr, value, winname )
		selectStore( slname, attr, value, winname )
***********************************************************************************************************************/
function _selectItem( slname, namecls, attr, gtin, gln, winname, naming ) {
	var url = classURL +"ECSItem?slname="+ slname;
	url += ( namecls ? "&namecls="+ namecls : "" );
	url += ( attr ? "&attr="+ attr : "" );
	if( naming )
		windowOpen( url +"&mode=name&gtin="+ encodeURIComponent(gtin) +"&gln="+ encodeURIComponent(gln), winname ? winname : "clsName" );
	else
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );

	return false;
}
function selectItem( slname, namecls, attr, winname ) {
	_selectItem( slname, namecls, attr, null, null, winname );
}
function namingItem( slname, namecls, attr, gtin, gln, winname ) {
	return _selectItem( slname, namecls, attr, gtin, gln, winname, true );
}

function _selectManuf( slname, attr, value, winname, naming ) {
	var url = classURL +"ECSManuf?slname="+ slname;
	url += ( attr ? "&attr="+ attr : "" );
	if( naming )
		windowOpen( url +"&mode=name&gln="+ encodeURIComponent(value), winname ? winname : "clsName" );
	else
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );

	return false;
}
function selectManuf( slname, attr, value, winname ) {
	_selectManuf( slname, attr, value, winname );
}
function namingManuf( slname, attr, value, winname ) {
	return _selectManuf( slname, attr, value, winname, true );
}

function selectOrigin( slname, attr, value, winname ) {
	_selectCode( classURL +"ECSOrigin", slname, attr, value, winname );
}
function namingOrigin( slname, attr, value, winname ) {
	return _selectCode( classURL +"ECSOrigin", slname, attr, value, winname, true );
}

function _selectParty( slname, namecls, attr, value, winname, naming ) {
	var url = classURL +"ECSParty?slname="+ slname;
	url += ( namecls ? "&namecls="+ namecls : "" );
	url += ( attr ? "&attr="+ attr : "" );
	if( naming )
		windowOpen( url +"&mode=name&gln="+ encodeURIComponent(value), winname ? winname : "clsName" );
	else
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );

	return false;
}
function selectParty( slname, namecls, attr, value, winname ) {
	_selectParty( slname, namecls, attr, value, winname );
}
function namingParty( slname, namecls, attr, value, winname ) {
	return _selectParty( slname, namecls, attr, value, winname, true );
}

function selectSeason( slname, slcls, attr, value, winname ) {
	_selectClassCode( classURL +"ECSSeason", slname, slcls, attr, value, winname );
}
function namingSeason( slname, slcls, attr, value, winname ) {
	return _selectClassCode( classURL +"ECSSeason", slname, slcls, attr, value, winname, true );
}

function _selectStore( slname, attr, value, winname, naming ) {
	var url = classURL +"ECSStore?slname="+ slname;
	url += ( attr ? "&attr="+ attr : "" );
	if( naming )
		windowOpen( url +"&mode=name&gln="+ encodeURIComponent(value), winname ? winname : "clsName" );
	else
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );

	return false;
}
function selectStore( slname, attr, value, winname ) {
	_selectStore( slname, attr, value, winname );
}
function namingStore( slname, attr, value, winname ) {
	return _selectStore( slname, attr, value, winname, true );
}


/***********************************************************************************************************************
	����: ECS
		deliveryScheduleInfo( query, self );
		itemInfo( gtin, gln, self );
		tradeItemInfo( buyerGln, sellerGln, gtin, self );
		tradePartnerInfo( buyerGln, sellerGln, self );
***********************************************************************************************************************/
function deliveryScheduleInfo( query, self ) {
	_info( classURL +"ECSDlvSchedule?mode=info"+ ( query ? "&"+ query : "" ), self );
}

function itemInfo( gtin, gln, self ) {
	_info( classURL +"ECSItem?mode=info&gtin="+ encodeURIComponent(gtin) +"&gln="+ encodeURIComponent(gln), self );
}

function tradeItemInfo( buyerGln, sellerGln, gtin, self ) {
	var url = classURL +"ECSTradeItem?mode=info&buyerGln="+ encodeURIComponent(buyerGln) +"&sellerGln="+ encodeURIComponent(sellerGln);
	_info( url +"&gtin="+ encodeURIComponent(gtin), self );
}

function tradePartnerInfo( buyerGln, sellerGln, self ) {
	_info( classURL +"ECSTradePartner?mode=info&buyerGln="+ encodeURIComponent(buyerGln) +"&sellerGln="+ encodeURIComponent(sellerGln), self );
}


/***********************************************************************************************************************
	����: CPFR
		ofcGroupInfo( code, self )
		ofcShareDayList( code, self )
		scenarioInfo( code, self )
		scenarioItemInfo( buyerGln, sellerGln, gtin, self )
		sfcDayIndexList( code, self )
		sfcGroupInfo( code, self )
***********************************************************************************************************************/
function ofcGroupInfo( code, self ) {
	_info( classURL +"CPFROFCGroup?mode=info&ofcCode="+ encodeURIComponent(code), self );
}

function ofcShareDayList( code, self ) {
	_info( classURL +"CPFROFCShareDay?mode=list&ofcCode="+ encodeURIComponent(code), self );
}

function scenarioInfo( code, self ) {
	_info( classURL +"CPFRScenario?mode=info&code="+ encodeURIComponent(code), self );
}

function scenarioItemInfo( buyerGln, sellerGln, gtin, self ) {
	var url = classURL +"CPFRItem?mode=info&buyerGln="+ encodeURIComponent(buyerGln) +"&sellerGln="+ encodeURIComponent(sellerGln);
	_info( url +"&gtin="+ encodeURIComponent(gtin), self );
}

function sfcDayIndexList( code, self ) {
	_info( classURL +"CPFRSFCDayIndex?mode=list&sfcCode="+ encodeURIComponent(code), self );
}

function sfcGroupInfo( code, self ) {
	_info( classURL +"CPFRSFCGroup?mode=info&sfcCode="+ encodeURIComponent(code), self );
}


/***********************************************************************************************************************
	����: PDS
		_selectAttr( ctype, slname, slcls, attr, value, winname, naming )
		_selectBrand( ctype, slname, slcls, attr, value, winname, naming )
		_selectPDSItem( slname, attr, value, winname, naming )
		namingCateAttr( slname, slcls, attr, value, winname )
		namingCateBrand( slname, slcls, attr, value, winname )
		namingICateAttr( slname, slcls, attr, value, winname )
		namingICateBrand( slname, slcls, attr, value, winname )
		namingPDSItem( slname, attr, value, winname )
		selectCateAttr( slname, slcls, attr, value, winname )
		selectCateBrand( slname, slcls, attr, value, winname )
		selectICateAttr( slname, slcls, attr, value, winname )
		selectICateBrand( slname, slcls, attr, value, winname )
		selectPDSItem( slname, attr, value, winname )
***********************************************************************************************************************/
function _selectAttr( ctype, slname, slcls, attr, value, winname, naming ) {
	var url = classURL +"PDSAttr?ctype="+ ctype +"&slname="+ slname;
	url += ( slcls ? "&slcls="+ slcls : "" ) + ( attr ? "&attr="+ attr : "" );
	if( naming )
		windowOpen( url +"&mode=name&attrCode="+ encodeURIComponent(value), winname ? winname : "clsName" );
	else {
		if( value && value.length >= 3 && slcls != 'K' )
			url += "&classCode=2&attrKindCode="+ encodeURIComponent(value.substring(0, 3));
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );
	}
	return false;
}
function selectCateAttr( slname, slcls, attr, value, winname ) {
	_selectAttr( "CATE", slname, slcls, attr, value, winname );
}
function namingCateAttr( slname, slcls, attr, value, winname ) {
	return _selectAttr( "CATE", slname, slcls, attr, value, winname, true );
}
function selectICateAttr( slname, slcls, attr, value, winname ) {
	_selectAttr( "ICATE", slname, slcls, attr, value, winname );
}
function namingICateAttr( slname, slcls, attr, value, winname ) {
	return _selectAttr( "ICATE", slname, slcls, attr, value, winname, true );
}

function _selectBrand( ctype, slname, slcls, attr, value, winname, naming ) {
	var url = classURL +"PDSBrand?ctype="+ ctype +"&slname="+ slname;
	url += ( slcls ? "&slcls="+ slcls : "" ) + ( attr ? "&attr="+ attr : "" );
	if( naming )
		windowOpen( url +"&mode=name&brandCode="+ encodeURIComponent(value), winname ? winname : "clsName" );
	else {
		if( value && value.length >= 3 && slcls != 'K' )
			url += "&classCode=2&attrKindCode="+ encodeURIComponent(value.substring(0, 3));
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );
	}
	return false;
}
function selectCateBrand( slname, slcls, attr, value, winname ) {
	_selectBrand( "CATE", slname, slcls, attr, value, winname );
}
function namingCateBrand( slname, slcls, attr, value, winname ) {
	return _selectBrand( "CATE", slname, slcls, attr, value, winname, true );
}
function selectICateBrand( slname, slcls, attr, value, winname ) {
	_selectBrand( "ICATE", slname, slcls, attr, value, winname );
}
function namingICateBrand( slname, slcls, attr, value, winname ) {
	return _selectBrand( "ICATE", slname, slcls, attr, value, winname, true );
}

function _selectPDSItem( slname, attr, value, winname, naming ) {
	var url = classURL +"PDSItem?slname="+ slname;
	url += ( attr ? "&attr="+ attr : "" );
	if( naming )
		windowOpen( url +"&mode=name&gtin="+ encodeURIComponent(value), winname ? winname : "clsName" );
	else
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );
	return false;
}
function selectPDSItem( slname, attr, value, winname ) {
	_selectPDSItem( slname, attr, value, winname );
}
function namingPDSItem( slname, attr, value, winname ) {
	return _selectPDSItem( slname, attr, value, winname, true );
}


/***********************************************************************************************************************
	����: DPR
		selectDPRParty( ctype, slname, slcls, attr, value, winname, naming )
***********************************************************************************************************************/
function _selectDPRParty( slname, namecls, attr, value, winname , naming) {
	var url = classURL +"DPRParty?slname="+ slname;
	url += ( namecls ? "&namecls="+ namecls : "" );
	url += ( attr ? "&attr="+ attr : "" );
	if( value && naming )
		url += "&code=" + value;
	else if( value )
		url += "&exclusiveCode=" + value;


	if( naming )
		windowOpen( url +"&mode=name", winname ? winname : "clsName" );
	else
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );

	return false;
}

function selectDPRParty( slname, namecls, attr, value, winname ) {
	_selectDPRParty( slname, namecls, attr, value, winname, null );
}

function _selectDPRPartyMaster( slname, namecls, attr, value, winname , naming) {
	var url = classURL +"DPRMaster?slname="+ slname;
	url += ( namecls ? "&namecls="+ namecls : "" );
	url += ( attr ? "&attr="+ attr : "" );
	var codeName;
	if( value && naming )
		codeName = "code";
	else if( value )
		codeName = "exclusiveCode";

	if( codeName ) {
		if( value[0] ) {
			for( var i = 0; i < value.length; i++ )
				url += "&"+ codeName +"=" + value[i];
		} else
			url += "&"+ codeName +"=" + value;
	}


	if( naming )
		windowOpen( url +"&mode=name", winname ? winname : "clsName" );
	else
		windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );

	return false;

}
function selectDPRPartyMaster( slname, namecls, attr, value, winname ) {
	_selectDPRPartyMaster( slname, namecls, attr, value, winname, null );
}
