<%--
	File Name:	sub_menu.jsp(dpr)
	Version:	2.2.18

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.18	StockQuery 메뉴 추가
	jbaek		2020/06/30		2.2.17	Revise Order Feature.
	hankalam	2020/06/30		2.2.17	Dangerous Material 메뉴 추가
	jbaek		2019/07/30		2.2.16	OrderClose 메뉴 추가. 한국 조건 추가
	hankalam	2019/06/28		2.2.16	Predefined RDD 메뉴 추가
	jbaek		2019/04/30		2.2.16	StopItem, MasterMng(Customer,Product) 메뉴 추가, 메뉴 width 변경.
	jbaek		2018/04/30		2.2.15	ItemExtra, OrderComment 메뉴 추가
	hankalam	2017/08/31		2.2.14	Product requirement 메뉴 추가.
	jbaek		2017/09/30		2.2.13	FAQ Board 메뉴 추가
	jbaek		2017/06/30		2.2.12	ItemEAN(Material UPC) 메뉴 추가.
	hankalam	2017/02/28		2.2.11	Inbox, Message Board, Selling SKU(With plant), UOM Management, Admin Board 메뉴 추가
	song7981	2016/05/20		2.2.10	ItemPrice 메뉴 추가
	song7981	2016/02/29		2.2.9	Plant Recovery 메뉴 추가
	hankalam	2015/10/30		2.2.8	웹취약성 수정. locale 값 pageConfig 에서 갖고오도록 수정
	jbaek		2015/01/31		2.2.7	minor design 변경: default submenu관련
	jbaek		2014/03/31		2.2.6	savedOrganizationCode 선택에 따라서 Main Frame Reload 되도록 변경.
	jbaek		2014/02/16		2.2.5	Plant SKU 제외 기능 개발
	jbaek		2013/04/30		2.2.4	Sales Mov 관리
	jbaek		2011/11/30		2.2.3	OrderInputAuth Condition 체크: Order Input 메뉴 보여지는 로직 추가
										메뉴layout 재조정및 조직메뉴 width 지정
	lsinji		2011/02/28		2.2.2	organizationCode listNameFormat 수정
	lsinji		2009/04/20		2.2.1	DPR PARAMS 처리 오류 처리( &<%= PARAMS %> )
	stghr12		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	String PARAM = htmlpage.getLocale().getLanguage();
	PARAM = ( PARAM == null || PARAM.length() == 0 ? "&menu=portal" : "&menu=portal&locale="+ PARAM );

	String organizationCode = sessionMng.getExtraValue();
%>

<style type='text/css'><!--
	body {
		margin: 0; padding: 0;
	}

	div.top_menu ul {
		text-align: right;
		margin: 0; margin-right: 25px;
		margin-top: 2px; margin-bottom: 1px;
	}
	div.top_menu li {
		list-style: 0;
		display: inline;
	}
	div.top_menu P.msg {
		margin: 2px 15px 0px
	}

	div.menu_layer {
		height: 34px;
		margin-left: 15px; margin-right: 15px;
		position: relative;
	}
	div.menu_layer p {
		text-align: center;
		width: 125px;
		margin-top: 0px; left: 0px;
		float: left; top: -15px;
		position: absolute;
	}

	div.menu {
		background: #F06908;
		width: auto; height: 34px;
		margin-left: 160px; padding: 0;
	}

	ul#menu {
		background: #F06908;
		width: 1010px; height: 34px;
		margin: 0; margin-left: 20px;
		padding: 0;
		position: absolute;
		float: left; _float: ;
		z-index: 2;
	}

	ul#menu li {
		background: #F06908;
		width: 110px;
		margin: 0;
		margin-top: 0px;
		padding: 0;
		list-style: none;
		float: left;
		display: hidden;
	}

	ul#menu li a.menu {
		display: block;
		text-align: center;
		width: 100px;
		margin: 0; padding: 0;
		text-decoration: none;
	}

	ul#menu li a.menu:hover {
		background: #F06908;;
	}

	.submenu {
		width: 1200px;
		margin: 0;
		display: none;
		position: absolute;
		z-index: 3;
	}

	.submenu div {
		background: #F4F4F4; color: #ABABAB;
		border: 1px solid #FFBE90;
		text-align: center;
		margin-top: 2px;
		padding: 0;
		float: left;
	}

	.submenu a:link, .submenu a:visited, .submenu a:active {
		display: block;
		font: 11px tahoma;
		text-align: left;
		text-decoration: none;
		padding: 1px;
		color: #ABABAB;
		float: left;
	}

	.submenu a:hover {
		color: #545454;
		float: left;
		}

/** TODO: check this alink thing.. is really used ?  seems it is only for testing or temporary thing...*/
	a.alink {
			fond-color: black;
		color: black;
	}

	a.alink:hover {
		color: black;
		background-color: #DDDDDD;
	}

	input.toggle[type=checkbox] {
   position: absolute;
   top: -9999px;
   left: -9999px;
   /* For mobile, it's typically better to position checkbox on top of clickable
	  area and turn opacity to 0 instead. */
}

.submenu a.nav-link:hover {
	background-color: #DDDDDD;
}

/* Toggled State */
input.toggle[type=checkbox]:checked a.alink {
		color: black;
		background-color: #DDDDDD;
}

	a.alink:target{
		color: black;
		background-color: #DDDDDD;
	}

//--></style>

<script type='text/javascript'>
	function changeEffectUserReq() {
		window.open(
			"<%= systemConfig.getClassURL() %>/USREffectUser?wintype=sub<%= PARAM %>"
			, "winMng", "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=yes, scrollbars=yes, width=50, height=50"
		);
	}

	function initPageReq(siteLocale) {
		var url	= "<%= systemConfig.getClassURL() %>/DPRMain?mode=default<%= PARAM %>";
		if( siteLocale != undefined ) {
			url = replaceQueryValue( url, "locale", siteLocale );
		}
		Menu.hide();
		window.open( url, "main" );
	}

	function noticeReq() {
		Menu.hide( 1 );
		var url = "<%= systemConfig.getClassURL() %>/RBMBoardNotice";
		var boardClassCode = "NO";
		var partyId = "<%= sessionMng.getPartyId() %>";
		if( partyId != null && partyId != "" )
			boardClassCode += ("." + partyId);
		url = replaceQueryValue( url, "boardClassCode", boardClassCode );

		window.open( url + "<%= PARAM %>", "main" );
	}

	function helpBoardRegistReq(  ) {
		var width = 650;
		var height = 530;
		var left = ( screen.availWidth - width ) * 2 / 3;
		var top = ( screen.availHeight- height ) / 4;

		window.open( "<%= systemConfig.getClassURL() %>/DPRHelpBoard?mode=ireg<%= PARAM %>&wintype=sub", "helpboardReg"
				, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=yes"
					+ ", left="+ left +", top="+ top +", width="+ width +", height="+ height
			);

	}

	function systemInfoReq() {
		var width = 480;
		var height = 285;
		var left = ( screen.availWidth - width ) * 2 / 3;
		var top = ( screen.availHeight- height ) / 4;
		window.open(
			"<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>systeminfo_rss_cpfr.html"
			, "winInfo"
			, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=no, scrollbars=no"
				+ ", left="+ left +", top="+ top +", width="+ width +", height="+ height
		);
	}

	function changeOrganization() {
		var width = 480;
		var height = 285;
		var left = ( screen.availWidth - width ) * 2 / 3;
		var top = ( screen.availHeight- height ) / 4;

		window.open( "<%= systemConfig.getClassURL() %>/DPRMain?mode=ireg<%= PARAM %>&wintype=sub", "winInfo"
			, "toolbar=no, status=no, location=no,, directories=no, menubar=no, resizable=no, scrollbars=no"
				+ ", left="+ left +", top="+ top +", width="+ width +", height="+ height
		);
	}

	function refreshMain() {
		var url = parent.frames["top"].location.href;
		parent.frames["top"].location.href = url + ""
		parent.frames["main"].location.reload();
	}

	function setOrganization( organizationCode ) {
		var url = "<%= systemConfig.getClassURL() %>/DPRMain?mode=def&rtype=main";
		url = replaceQueryValue( url, "organizationCode", organizationCode );
		url = replaceQueryValue( url, "url", encodeURIComponent(location.href) );

		window.open( url + "<%= PARAM %>", "_self" );
	}

	function simpleImageOver( obj ) {
		obj.src = obj.src.replace("_off.gif", "_on.gif");
	}

	function simpleImageOut( obj ) {
		obj.src = obj.src.replace("_on.gif", "_off.gif");
	}

</script>

<div class='menu_layer'>
	<p style='text-align: center; padding-left: 15px;'><a href='JavaScript:initMenuReq();'><img src='images/menu_logo.gif'></a>
		<% if( organizationCount != null && Integer.parseInt(organizationCount) > 1 ) { %>
			<p style='text-align: left; top: 36px; width: 100%; padding-left: 2px;'>
			<mtl:select id="property" key="savedOrganizationCode" nullValueKey="jsp.MSG_SUB_MENU_SELECT_ORGANIZATION"
					hasBlank="true" listId="organizations" listCodeKey="organizationCode" listNameFormat="${[:organizationCode;]} ${organizationName~20}"
					modified="Javascript:setOrganization(this.value);" style="width: 160px;"/>
			</p>
<%--
			<a href='JavaScript:changeOrganization();'><img src='images/menu_dpr_selsales_on.gif'></a></p>
--%>
		<% } %>
	</p>

	<div class='menu'>
		<img src='images/menu_bg1.gif' style='float: left; _float: ; _position: absolute;'>

		<ul id='menu'>
			<li><img src='images/menu1_dpr_material_off.gif' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'
						level='1' name='dpr_material' onClick='JavaScript:Menu.click(this, "dpr_material_list");'><br>

				<div class='submenu' id='menu2_dpr_material'>
				<div>
				<% if( sessionMng.isAuthorized("DPR", "DPRItem.INF") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRItem?mode=frm<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_material_list_off.gif' name='dpr_material_list' level='2'
							onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( sessionMng.isAuthorized("DPR", "DPRItemImage.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRItemImage?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_material_img_list_off.gif' name='dpr_material_img_list' level='2'
							onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( sessionMng.isAuthorized("DPR", "DPRItem.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRItem?mode=list&btype=itm<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_sellingsku_list_off.gif' name='dpr_sellingsku_list' level='2'
							onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( sessionMng.isAuthorized("DPR", "DPRPlantItem.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRPlantItem?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_plantsku_list_off.gif' name='dpr_plantsku_list' level='2'
							onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( sessionMng.isAuthorized("DPR", "DPRStockQuery.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRStockQuery?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_stockquery_off.gif' name='dpr_stockquery' level='2'
							onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( sessionMng.isAuthorized("DPR", "DPRItem.WithPlant.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRItem?mode=list&vtype=party&btype=ordall<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_sellingskuplant_list_off.gif' name='dpr_sellingskuplant_list' level='2'
							onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") && sessionMng.isAuthorized("DPR","DPRDangerousItem.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRDangerousItem?mode=list&dangerousInd=Y<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_dangerousitem_off.gif' name='dpr_dangerousitem' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>

				<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePlantRcv") && sessionMng.isAuthorized("DPR","DPRPlantRecovery.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRPlantRecovery?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_rcv_list_off.gif' name='dpr_rcv_list' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( sessionMng.isAuthorized("DPR","DPRItemPrice.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRItemPrice?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_material_price_list_off.gif' name='dpr_material_price_list' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( sessionMng.isAuthorized("DPR","DPRItemEAN.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRItemEAN?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_materialupc_list_off.gif' name='dpr_materialupc_list' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( com.irt.dpr.Country.isFeature(organizationCode, "useStopItem") && sessionMng.isAuthorized("DPR","DPRStopItemCfg.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRStopItemCfg?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_stopitemcfg_off.gif' name='dpr_stopitemcfg' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( com.irt.dpr.Country.isFeature(organizationCode, "useStopItem") && sessionMng.isAuthorized("DPR","DPRStopItem.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRStopItem?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_stopitem_off.gif' name='dpr_stopitem' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePackDeal") && sessionMng.isAuthorized("DPR","DPRPackDealCfg.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRPackDealCfg?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_packdealcfg_off.gif' name='dpr_packdealcfg' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
<%--				<% if( sessionMng.isAuthorized("DPR","DPRPackDealCfgRlt.LST") ) { %> --%>
<%--					<a href='<%= systemConfig.getClassURL() %>/DPRPackDealCfgRlt?mode=list<%= PARAM %>' target='main'> --%>
<!--					<img src='images/menu2_dpr_packdeal_off.gif' name='dpr_packdeal' level='2' -->
<!--						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a> -->
<%--				<% } %> --%>
				<% if( sessionMng.isAuthorized("DPR","DPRFreeGoods.MNG") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRFreeGoods?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_freegoods_off.gif' name='dpr_freegoods' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
<%--
					<a href='#'><img src='images/menu2_dpr_materialgroup_off.gif' name='dpr_materialgroup' level='2'
							onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
--%>
				</div>
				</div>
			</li>

		<% if( sessionMng.isAuthorized("DPR","DPRParty.LST") || (!("JNJAP_KR".equals(sessionMng.getPartyId())) && (sessionMng.isSystemAdmin() || sessionMng.isPartyAdmin() || sessionMng.isCountryAdmin()))
				|| (sessionMng.isAuthorized("DPR","DPRPartyUOM.MNG") && com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM"))
				|| (com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") && sessionMng.isAuthorized("DPR","DPRRddMng.LST"))
				|| (com.irt.dpr.Country.isFeature(organizationCode, "usePartyOper") && sessionMng.isAuthorized("DPR","DPRPartyOper.LST"))
				|| sessionMng.isAuthorized("DPR","DPROrderClose.LST") ) { %>
			<li><img src='images/menu1_dpr_partner_off.gif' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'
						name='dpr_partner' level='1' onClick='JavaScript:Menu.click(this, "dpr_partylist");'><br>
				<div class='submenu' id='menu2_dpr_partner'>
				<div>
			<% if( sessionMng.isAuthorized("DPR","DPRParty.LST") ) { %>
				<a href='<%= systemConfig.getClassURL() %>/DPRParty?mode=list<%= PARAM %>' target='main'>
				<img src='images/menu2_dpr_partylist_off.gif' name='dpr_partylist' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>

			<% if( !("JNJAP_KR".equals(sessionMng.getPartyId())) && (sessionMng.isSystemAdmin() || sessionMng.isPartyAdmin() || sessionMng.isCountryAdmin()) ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRCountry?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_country_list_off.gif' name='dpr_country_list' level='2'
							onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>

			<% if( !("JNJAP_KR".equals(sessionMng.getPartyId())) && (sessionMng.isSystemAdmin() || sessionMng.isPartyAdmin() || sessionMng.isCountryAdmin()) ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRSalesMov?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_salesmov_list_off.gif' name='dpr_salesmov_list' level='2'
							onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( sessionMng.isAuthorized("DPR","DPRPartyUOM.MNG") && com.irt.dpr.Country.isFeature(organizationCode, "useDistAllowUOM") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRPartyUOM?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_uommanage_off.gif' name='dpr_uommanage' level='2'
							onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>

			<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") && sessionMng.isAuthorized("DPR","DPRRddMng.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRRddMng?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_rddmng_off.gif' name='dpr_rddmng' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>

			<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePartyOper") && sessionMng.isAuthorized("DPR","DPRPartyOper.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRPartyOper?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_partyoper_off.gif' name='dpr_partyoper' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>

				<% if( sessionMng.isAuthorized("DPR","DPROrderClose.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPROrderClose?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_odrclosing_off.gif' name='dpr_odrclosing' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				</div>
				</div>
			</li>
		<% } %>
		<% 
		System.out.println("####" +  sessionMng.isAuthorized("DPR","DPRProductRequire.MNG"));
		%>
		<% if( sessionMng.isAuthorized("DPR", "DPROrder.DWN") || sessionMng.isAuthorized("DPR","DPROrder.LST")
				|| sessionMng.isAuthorized("DPR","DPROrder.INF") || sessionMng.isAuthorized("DPR","DPROrder.MNG") || sessionMng.isAuthorized("DPR","DPROrderReport")
				|| sessionMng.isAuthorized("DPR","DPRMoqItemCfg.LST")
				|| sessionMng.isAuthorized("DPR","DPROrderRevise.MNG")
				|| sessionMng.isAuthorized("DPR","DPRProductRequire.MNG")
				 ) {
			String defaultOrderSubMenu = "dpr_odrinput";
			if( com.irt.data.Condition.isConditionTrue(property.getProperty("isOrderInputAuth")) )
				defaultOrderSubMenu = "dpr_odrstatus";
			%>

			<li><img src='images/menu1_dpr_order_off.gif' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'
						name='dpr_order' level='1' onClick='JavaScript:Menu.click(this, "<%=defaultOrderSubMenu%>" );'><br>
				<div class='submenu' id='menu2_dpr_order'>
				<div>
			<% if( sessionMng.isAuthorized("DPR", "DPROrder.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPREnquiryOrder?mode=cond<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_odrstatus_off.gif' name='dpr_odrstatus' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( sessionMng.isAuthorized("DPR", "DPROrder.MNG") && com.irt.data.Condition.isConditionTrue(property.getProperty("isOrderInputAuth")) ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=frm<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_odrinput_off.gif' name='dpr_odrinput' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePackDeal") && sessionMng.isAuthorized("DPR", "DPROrder.MNG") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRPackDealOrder?mode=ior<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_packdeal_off.gif' name='dpr_packdeal' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( sessionMng.isAuthorized("DPR", "DPROrderReport.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPROrderReport?mode=list<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_odrreport_off.gif' name='dpr_odrreport' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( sessionMng.isAuthorized("DPR", "DPRBillingReport.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRBillingReport?mode=list<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_billreport_off.gif' name='dpr_billreport' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( sessionMng.isAuthorized("DPR", "DPROrderRevise.MNG") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPROrderRevise?mode=list<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_odrmod_off.gif' name='dpr_odrmod' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			
			<%
System.out.println("###"+sessionMng.getExtraValue());
System.out.println("###"+com.irt.dpr.Country.isFeature(sessionMng.getExtraValue(), "useProductRequire"));
System.out.println("###"+ sessionMng.isAuthorized("DPR", "DPRProductRequire.MNG"));
System.out.println("###"+ (property.getProperty("isOrderInputAuth"));
System.out.println("###"+  com.irt.data.Condition.isConditionTrue(property.getProperty("isOrderInputAuth")));

			if( com.irt.dpr.Country.isFeature(sessionMng.getExtraValue(), "useProductRequire") &&
					sessionMng.isAuthorized("DPR", "DPRProductRequire.MNG") && com.irt.data.Condition.isConditionTrue(property.getProperty("isOrderInputAuth")) ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRProductRequire?mode=list<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_productreq_list_off.gif' name='dpr_productreq_list' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
				<% if( sessionMng.isAuthorized("DPR","DPRMoqItemCfg.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRMoqItemCfg?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_moqitemcfg_off.gif' name='dpr_moqitemcfg' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				<% if( sessionMng.isAuthorized("DPR","DPRMoqItem.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRMoqItem?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_moqitem_off.gif' name='dpr_moqitem' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				</div>
				</div>
			</li>
		<% } %>

		<%
			boolean showDeprecatedFunction = false;
			if( sessionMng.isAuthorized("DPR", "DPRItemMasterExtra.MNG")
				|| sessionMng.isAuthorized("DPR","DPRUpload.SSL") || sessionMng.isAuthorized("DPR","DPRUpload.PCD")
				|| sessionMng.isAuthorized("DPR","DPRMasterMng.MNG")
//				|| sessionMng.isAuthorized("DPR", "DPRUpload.INV") || sessionMng.isAuthorized("DPR","DPRUpload.SEO")
//				|| sessionMng.isAuthorized("DPR","DPRUpload.CMT")
//				|| sessionMng.isAuthorized("DPR","DPRUpload.SKM") || sessionMng.isAuthorized("DPR","DPRUpload.CIM")
				) { %>

			<li><img src='images/menu1_dpr_upload_off.gif' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'
						name='dpr_upload' level='1' onClick='JavaScript:Menu.click(this, "dpr_skulist");'><br>
				<div class='submenu' id='menu2_dpr_upload'>
				<div>

				<% if( sessionMng.isAuthorized("DPR","DPRMasterMng.MNG") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRMasterMng?mode=list&mngtype=ptysales<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_custmst_off.gif' name='dpr_custmst' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>

					<a href='<%= systemConfig.getClassURL() %>/DPRMasterMng?mode=list&mngtype=itmsales<%= PARAM %>' target='main'>
					<img src='images/menu2_dpr_prdtmst_off.gif' name='dpr_prdtmst' level='2'
						onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
			<%
			boolean useOrdExtInfoMng = com.irt.rbm.RBMSystem.getSystemEnvBool( "DPR", "OrdExtInfoMng;"+ organizationCode, false );
			if( useOrdExtInfoMng ) {
				int boardNumber = com.irt.rbm.RBMSystem.getSystemEnvInt( "DPR", "OrdExtInfoMngOrdCmtBrdNum;"+ organizationCode, -1 );

				if( useOrdExtInfoMng ) { %>
					<% if( "1800".equals(organizationCode) ) { %>
						<a href='<%= systemConfig.getClassURL() %>/DPRItemMasterExtra?mode=list<%= PARAM %>' target='main'>
							<img src='images/menu2_dpr_extmst_sjjp_off.gif' name='dpr_extmst_sjjp' level='2'
									onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
					<% } else { %>
						<a href='<%= systemConfig.getClassURL() %>/DPRItemMasterExtra?mode=list<%= PARAM %>' target='main'>
							<img src='images/menu2_dpr_extmst_off.gif' name='dpr_extmst' level='2'
									onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
					<% } %>
				<% }
				if( useOrdExtInfoMng && boardNumber != -1 ) { %>
					<% if( "1800".equals(organizationCode) ) { %>
						<a href='<%= systemConfig.getClassURL() %>/ICSBoard?boardClassCode=TPL.<%=organizationCode%>&type=NAME&mode=info&boardNumber=<%=String.valueOf(boardNumber)%><%= PARAM %>' target='main'>
							<img src='images/menu2_dpr_pdfodrcmt_sjjp_off.gif' name='dpr_pdfodrcmt_sjjp' level='2'
									onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
					<% } else { %>
						<a href='<%= systemConfig.getClassURL() %>/ICSBoard?boardClassCode=TPL.<%=organizationCode%>&type=NAME&mode=info&boardNumber=<%=String.valueOf(boardNumber)%><%= PARAM %>' target='main'>
							<img src='images/menu2_dpr_pdfodrcmt_off.gif' name='dpr_pdfodrcmt' level='2'
									onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
					<% } %>
				<% } %>
			<% } %>

			<% if( sessionMng.isAuthorized("DPR","DPRUpload.SSL") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRUpload?mode=iup&uploadType=SSL<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_skulist_off.gif' name='dpr_skulist' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( sessionMng.isAuthorized("DPR", "DPRUpload.PCD") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRUpload?mode=iup&uploadType=PCD<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_pcatedesc_off.gif' name='dpr_pcatedesc' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( showDeprecatedFunction && sessionMng.isAuthorized("DPR", "DPRUpload.INV") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRUpload?mode=iup&uploadType=INV<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_inventory_off.gif' name='dpr_inventory' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( showDeprecatedFunction && sessionMng.isAuthorized("DPR","DPRUpload.SEO") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRUpload?mode=iup&uploadType=SEO<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_sellout_off.gif' name='dpr_sellout' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( showDeprecatedFunction && sessionMng.isAuthorized("DPR","DPRUpload.CMT") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRUpload?mode=iup&uploadType=CMT<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_customerid_off.gif' name='dpr_customerid' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( showDeprecatedFunction && sessionMng.isAuthorized("DPR","DPRUpload.SKM") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRUpload?mode=iup&uploadType=SKM<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_skumap_off.gif' name='dpr_skumap' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( showDeprecatedFunction && sessionMng.isAuthorized("DPR","DPRUpload.CIM") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/DPRUpload?mode=iup&uploadType=CIM<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_idmapping_off.gif' name='dpr_idmapping' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
				</div>
				</div>
			</li>
		<% } %>


		<% if( sessionMng.isAuthorized("DPR","DPRUpload.LST") ) { %>
			<li><img src='images/menu1_dpr_log_off.gif' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'
						name='dpr_log' level='1' onClick='JavaScript:Menu.click(this, "dpr_uploadlog");'><br>
				<div class='submenu' id='menu2_dpr_log'>
				<div>
				<a href='<%= systemConfig.getClassURL() %>/DPRUpload?mode=list<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_uploadlog_off.gif' name='dpr_uploadlog' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% if( "JNJAP_KR".equals(sessionMng.getPartyId()) ) { %>
				<a href='<%= systemConfig.getClassURL() %>/RBMUploadLog?mode=list<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_uploadlogkr_off.gif' name='dpr_uploadlogkr' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
				<% } %>
				</div>
				</div>
			</li>
		<% } %>

			<li><img src='images/menu1_dpr_user_off.gif' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'
						name='dpr_user' level='1' onClick='JavaScript:Menu.click(this, "dpr_userinfo");'></br>
				<div class='submenu' id='menu2_dpr_user'>
				<div>
					<a href='<%= systemConfig.getClassURL() %>/USRUser?mode=info<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_userinfo_off.gif' name='dpr_userinfo' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% if( sessionMng.isAuthorized("USR","USRUser.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/USRUser?mode=list<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_usermgt_off.gif' name='dpr_usermgt' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
			<% if( sessionMng.isAuthorized("USR","USRUserSession.LST") ) { %>
					<a href='<%= systemConfig.getClassURL() %>/USRUserSession?mode=list<%= PARAM %>' target='main'>
						<img src='images/menu2_dpr_usersession_off.gif' name='dpr_usersession' level='2'
								onClick='JavaScript:Menu.click(this);' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'></a>
			<% } %>
				</div>
				</div>
			</li>

		<%
			String faqBoardCode = "FQ";
			String countryKey = "";
			countryKey = com.irt.dpr.Country.getCountryKeyFromPartyId(sessionMng.getPartyId());
			if( countryKey != null && countryKey.length() > 0 ) {
				faqBoardCode += "." + countryKey;
			}
			if( sessionMng.isAuthorized("ICS", "ICSBoard." + faqBoardCode + ".INF") ) {
		%>
			<li><a href='<%= systemConfig.getClassURL() %>/ICSBoard?mode=faq&boardClassCode=<%=faqBoardCode%><%= PARAM %>' target='main'>
					<img src='images/menu1_dpr_faq_off.gif' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'
							name='dpr_faq' level='1' onClick='JavaScript:Menu.click(this);'></a><br>
			</li>
		<% } %>

		<%
			String packageCode = "ICSHelpBoard.HD";
			if( organizationCode != null )
				packageCode += "." + organizationCode;
			if( !("JNJAP_KR".equals(sessionMng.getPartyId())) && sessionMng.isAuthorized("ICS", packageCode + ".MNG") ) {
		%>
			<li><a href='<%= systemConfig.getClassURL() %>/DPRHelpBoard?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu1_dpr_inbox_off.gif' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'
							name='dpr_inbox' level='1' onClick='JavaScript:Menu.click(this);'></a><br>
			</li>
		<% } else if( !("JNJAP_KR".equals(sessionMng.getPartyId())) && sessionMng.isAuthorized("ICS", packageCode + ".REG") ) { %>
			<li><a href='JavaScript:helpBoardRegistReq()'>
					<img src='images/menu1_dpr_msgboard_off.gif' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'
							name='dpr_msgboard' level='1' onClick='JavaScript:Menu.click(this);'></a><br>
			</li>
		<% } %>

		<% if( sessionMng.isSystemAdmin() ) { %>
			<li><a href='<%= systemConfig.getClassURL() %>/DPRBoardNotice?mode=list<%= PARAM %>' target='main'>
					<img src='images/menu1_dpr_notice_off.gif' onMouseOver='imageOver(this)' onMouseOut='imageOut(this)'
							name='dpr_notice' level='1' onClick='JavaScript:Menu.click(this);'><br>
			</li>
		<% } %>

		</ul>

		<img src='images/menu_bg2.gif' align='right' style='right: -1; _position: absolute;'>
	</div>
</div>
