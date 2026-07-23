<%--
	File Name:	dpr_orderclose_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/06/28		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.List, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;

		function linkMenuReq( itemCode ) {
			if( !linkmenu ) {
				var idx = 0;
				var menu = new Array;

				menu[idx++] = new Array( '<mtl:message key="jsp.LMENU_DPR_MOQITEMCFG_MOD" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("CFG_MOD");' );

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "CFG_MOD" )
				linkMoqItemConfigModifyReq( linkmenu.params );
// 			else if( menu == "CFGRLT_LIST" )
// 				linkPackdealCfgRltListReq( linkmenu.params[0] );
// 			else if( menu == "CFG_LIST" )
// 				linkPackdealCfgListReq( linkmenu.params[0] );
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

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,brandCode" );
		}

		function infoReq( organizationCode,brandCode ) {
			var values = organizationCode +  ";" + brandCode;
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,brandCode", values );
		}

		function modifyReq() {
			var url = getLocationURL("url");
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,brandCode" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}

		function downloadReq() {
			requestDownload( getLocationURL("url"), frmMain.listcheckbox, "organizationCode,brandCode" );
		}

		function uploadReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<div id='messagebar'></div>

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
						<mtl:button type="delete"/>
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
