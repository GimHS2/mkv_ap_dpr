<%--
	File Name:	dpr_sales_mov_list.jsp
	Version:	2.2.3

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/03/31		2.2.3	ship-to 레벨 추가
	hankalam	2020/06/30		2.2.2	위험상품 레벨 MOV 기능 추가
	jbaek		2014/07/13		2.2.1	Sold-to Level MOV 기능 개발
	song7981	2013/04/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_rbm_catelink.inc"%>
	<script type='text/javascript'>
		var linkmenu = null;

		function deleteReq() {
			var btype = "<mtl:value id="property" key="btype"/>";
			var url = "<%= htmlpage.getRequestURL() %>";
			url = replaceQueryValue( url, "btype", btype );
			if( "PARTY" == btype || "DPARTY" == btype ) {
				requestDelete( url, frmMain.listcheckbox, "organizationCode,distributionChannelCode,divisionCode,officeCode,partyCode,dangerousInd" );
			} else if( "SPARTY" == btype || "DSPARTY" == btype ) {
				requestDelete( url, frmMain.listcheckbox, "organizationCode,distributionChannelCode,divisionCode,shipPartyCode,dangerousInd" );
			} else {
				requestDelete( url, frmMain.listcheckbox, "organizationCode,distributionChannelCode,divisionCode,officeCode,dangerousInd" );
			}
		}

		function downloadReq() {
			customPopup.confirm( { "detail" :"<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" }, function( res ) {
				if( res ) {
					var url = replaceQueryValue( getLocationURL("url"), "mode", "down" );
					url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
					url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );
					windowOpen( url );
				}
			});
		}

		function infoReq( organizationCode, distributionChannelCode, divisionCode, officeCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRSalesMov?mode=info";
			url = replaceQueryValue( url, "organizationCode", encodeURIComponent(organizationCode) );
			url = replaceQueryValue( url, "distributionChannelCode", encodeURIComponent(distributionChannelCode) );
			url = replaceQueryValue( url, "divisionCode", encodeURIComponent(divisionCode) );
			url = replaceQueryValue( url, "dangerousInd", encodeURIComponent(dangerousInd) );
			url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );

			var btype = "<mtl:value id="property" key="btype"/>";
			if( "PARTY" == btype || "DPARTY" == btype ) {
				url = replaceQueryValue( url, "partyCode", encodeURIComponent(partyCode) );
				url = replaceQueryValue( url, "officeCode", encodeURIComponent(officeCode) );
			} else if( "SPARTY" == btype || "DSPARTY" == btype ) {
				url = replaceQueryValue( url, "shipPartyCode", encodeURIComponent(shipPartyCode) );
			} else {
				url = replaceQueryValue( url, "officeCode", encodeURIComponent(officeCode) );
			}
			windowOpen( url + "&wintype=sub" , "claMng" );
		}

		function modifyReq( organizationCode, distributionChannelCode, divisionCode, officeCode ) {
			var btype = "<mtl:value id="property" key="btype"/>";
			var url = "<%= htmlpage.getRequestURL() %>";
			url = replaceQueryValue( url, "btype", btype );
			if( "PARTY" == btype || "DPARTY" == btype ) {
				requestModify( url, frmMain.listcheckbox, "organizationCode,distributionChannelCode,divisionCode,officeCode,partyCode,dangerousInd" );
			} else if( "SPARTY" == btype || "DSPARTY" == btype ) {
				requestModify( url, frmMain.listcheckbox, "organizationCode,distributionChannelCode,divisionCode,shipPartyCode,dangerousInd" );
			} else {
				requestModify( url, frmMain.listcheckbox, "organizationCode,distributionChannelCode,divisionCode,officeCode,dangerousInd" );
			}
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>")
			url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
			windowOpen( url, "sub-content" );
		}

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRSalesMov?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
			windowOpen( url, "sub-content" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>
			<div class='list-menu'>
			<%
				String prefixKey = "MSG_DPR_SALES_MOV_BASETYPE_";
				String btypes = "OFFICE,PARTY,SPARTY";
				if( com.irt.dpr.Country.isFeature(sessionMng.getExtraValue(), "useDangerousItem") ) {
					btypes += ",DOFFICE,DPARTY,DSPARTY";
					prefixKey += "USEDANGER_";
				}
			%>
				<mtl:select id="property" key="btype" prefixKey="<%= prefixKey %>" codeValues="<%= btypes %>"
						searchable="false" hasBlank="false" defaultValue="PARTY" modified="listLink(this);" width="auto"/>
			</div>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="regist"/>
					<% if( listwriter.containsData() ) { %>
						<mtl:button type="modify"/>
						<mtl:button type="delete"/>
					<% } %>
					<mtl:button type="upload"/>
				<% } %>
				<% if( listwriter.containsData() ) { %>
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
