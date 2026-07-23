<%--
	File Name:	dpr_packdealitem_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/05/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.List, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Map<String, Object> condition = (Map<String, Object>)pageContext.findAttribute( "condition" );
	com.irt.data.cols.ColumnList columnList = (com.irt.data.cols.ColumnList)pageContext.findAttribute("columnList");

	String primaryKeysCsv = com.irt.util.StringUtil.strJoin(columnList.getPrimaryFieldKeys(), ",");
// 	List<Map<String, Object>> categories = (List<Map<String, Object>>)pageContext.findAttribute( "categories" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;
		function bodyLoad() {
		<% if( !htmlpage.hasManageAuth() ) { %>
			Field.lock( frmCond.dealCode );
		<% } %>
		}

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,dealCode,itemCode" );
		}

		function infoReq( dealCode, itemCode ) {
			var values = encodeURIComponent(dealCode) + ";" + itemCode;
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,dealCode,itemCode", values );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,distributionChannelCode,officeCode,groupCode,partyCode,dealCode,itemCode" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "dealCode", encodeURIComponent("<mtl:value id="condition" key="dealCode" encodeScript="true"/>") );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="property" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="property" key="distributionChannelCode"/>" );
			url = replaceQueryValue( url, "officeCode", "<mtl:value id="property" key="officeCode"/>" );
			url = replaceQueryValue( url, "groupCode", "<mtl:value id="property" key="groupCode"/>" );
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="property" key="partyCode"/>" );
			windowOpen( url, "sub-content" );
		}

		function downloadReq() {
			var url = "<%= systemConfig.getClassURL () %>/DPRPackDealItem?mode=down";
			url = replaceQueryValue( url, "dealCode", encodeURIComponent("<mtl:value id="condition" key="dealCode" encodeScript="true"/>") );
			if( frmCond.brandCode.value )
				url = replaceQueryValue( url, "brandCode", encodeURIComponent(frmCond.brandCode.value) );

			if( frmCond.itemCode.value )
				url = replaceQueryValue( url, "itemCode", encodeURIComponent(frmCond.itemCode.value) );
			else if( frmCond.itemName.value )
				url = replaceQueryValue( url, "itemName", encodeURIComponent(frmCond.itemName.value) );
			if( frmCond.itemConsumerEANCode.value )
				url = replaceQueryValue( url, "itemConsumerEANCode", encodeURIComponent(frmCond.itemConsumerEANCode.value) );

			windowSelfOpen( url );
		}

		function uploadReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}

		function linkMenuReq( itemCode ) {
			if( !linkmenu ) {
				var menu = new Array;

				var idx = 0;
				<% if( !"pdcfg".equals(htmlpage.getProperty().getProperty("mngtype")) && sessionMng.isAuthorized("DPR", "DPRPackDealCfg.MNG") ) { %>
				menu[idx++] = new Array( '<mtl:message key="jsp.LMENU_DPR_PACKDEAL_CFG_LIST" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("CFG_LIST");' );
				<% } %>
				<% if( !"pdrlt".equals(htmlpage.getProperty().getProperty("mngtype")) && sessionMng.isAuthorized("DPR", "DPRPackDealCfgRlt.LST") ) { %>
				menu[idx++] = new Array( '<mtl:message key="jsp.LMENU_DPR_PACKDEAL_CFGRLT_LIST" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("CFGRLT_LIST");' );
				<% } %>

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "CFGRLT_LIST" )
				linkPackdealCfgRltListReq( linkmenu.params[0] );
			else if( menu == "CFG_LIST" )
				linkPackdealCfgListReq( linkmenu.params[0] );
		}

		function linkPackdealCfgRltListReq( dealCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPackDealCfgRlt?mode=list";

			url = replaceQueryValue( url, "dealCode", encodeURIComponent(dealCode) );
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributonChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );

			windowSelfOpen( url, getLocationURL("url") );
		}

		function linkPackdealCfgListReq( dealCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPackDealCfg?mode=list";

			url = replaceQueryValue( url, "dealCode", encodeURIComponent(dealCode) );
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "distributonChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );

			windowSelfOpen( url, getLocationURL("url") );
		}

		function orderItemLinkMenuReq( partyCode, itemCode ) {
			var menu = new Array;

			menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_ITEM_INFO" encodeScript="true"/>'
					, 'new', 'JavaScript:orderItemLinkMenuReqClick("INFO");' );

			linkmenu = createLinkMenu( menu );
			linkmenu.show();
			linkmenu.params = orderItemLinkMenuReq.arguments;
		}

		function partyInfoReq( partyCode, linkType ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=info";

			url = replaceQueryValue( url, "partyCode", partyCode );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "displayLinkType", linkType );

			windowOpen( url + "&wintype=sub", "sub-content" );
		}

		function partyLinkMenuReq( partyCode, linkType ) {
			var menu = new Array;

			menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_PARTY_INFO" encodeScript="true"/>'
					, 'new', 'JavaScript:partyLinkMenuReqClick("INFO");' );

			linkmenu = createLinkMenu( menu );
			linkmenu.show();
			linkmenu.params = partyLinkMenuReq.arguments;
		}

		function partyLinkMenuReqClick( menu ) {
			if( menu == "INFO" )
				partyInfoReq( linkmenu.params[0], linkmenu.params[1] );
		}

		function releaseField( obj ) {
			var fieldName = obj.name;
			if( !obj.value ) {
				var prefix = "frmCond.condition";
				var nameObj = eval( prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length - 4) + "Name" );
				nameObj.value = "";
				Field.release( nameObj );
			}
		}

	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<input type='hidden' name='mode' value='list'/>
		<mtl:hidden id="property" key="mngtype"/>
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>

			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="dealCode" descriptionKey="FIELD_DPR_PACKDEAL_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="dealCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="brandCode" descriptionKey="FIELD_DPR_BRAND_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="brandCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_BRAND"
									listId="brands" listCodeKey="brandCode" listNameFormat="$S{[:brandCode;] $S{:brandName}}"/>
						</div>
					</div>
					<div class='cell'></div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode" descriptionKey="FIELD_DPR_ITEM_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemName" descriptionKey="FIELD_DPR_ITEM_MASTER_NAME"/></div>
						<div class='field'><mtl:text id="condition" key="itemName"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemConsumerEANCode" descriptionKey="jsp.FIELD_DPR_UPC_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemConsumerEANCode"/></div>
					</div>
				</div>
			</div>
			<mtl:hidden id="request" key="search-fold" defaultValue="N"/>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
				function checkSearchCond() {
					disableBlankInput( frmCond, true );
					return submitInput();
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );

				if( htmlpage.hasManageAuth() )
					listWriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_CHECK );

				if( property.containsKey("listmsg") )
					listWriter.print( out, property.getProperty("listmsg") );
				else
					listWriter.print( out );

			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="regist"/>
					<% if( listWriter.containsData() ) { %>
						<mtl:button type="modify"/>
						<% if( sessionMng.isAuthorized("DPR", "DPRPackDealItem.MNG_DEL") ) { %>
							<mtl:button type="delete"/>
						<% } %>
					<% } %>
					<mtl:button type="upload"/>
				<% } %>
				<% if( listWriter.containsData() ) { %>
					<mtl:button type="download"/>
				<% } %>
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>
		</mtl:contentGroup>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
