<%--
	File Name:	usr_party_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2008/03/31		2.2.1	table.list_content_top: absolute¿Ü width Ãß°¡
	stghr12		2007/11/30		2.2.0	create
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
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "partyId" );
		}

		function infoReq( partyId ) {
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "partyId", partyId );
		}

		function linkMenuReq( partyId ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_USR_PARTY_INFO" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("INFO");' );
				<% if( htmlpage.hasManageAuth() ) { %>
					menu[1] = new Array( '<mtl:message key="jsp.LMENU_USR_PARTY_MOD" encodeScript="true"/>'
							, 'new', 'JavaScript:linkMenuReqClick("MOD");' );
				<% } %>
				<% if( sessionMng.isAuthorized("USR", "USRUser.LST") ) { %>
					menu[2] = new Array( '<mtl:message key="jsp.LMENU_USR_USER_LIST" encodeScript="true"/>'
							, 'self', 'JavaScript:linkMenuReqClick("LIST");' );
				<% } %>

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "INFO" )
				infoReq( linkmenu.params[0] );
			else if( menu == "MOD" )
				modifyReq( linkmenu.params[0] );
			else if( menu == "LIST" )
				userListReq( linkmenu.params[0] );
		}

		function modifyReq( partyId ) {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "partyId", partyId );
		}

		function registReq() {
			windowOpen( "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub", "sub-content" );
		}

		function userListReq( partyId ) {
			windowSelfOpen( "<%= systemConfig.getClassURL() %>/USRUser?mode=list&partyId="+ encodeURIComponent(partyId), getLocationURL() );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<div id='messagebar'></div>
			<div class='list-menu'>
			<% if( htmlpage.hasManageAuth() ) { %>
				<mtl:select id="request" key="status" prefixKey="USR_PARTY_STATUS_" codeValues="00,99"
						hasBlank="true" nullValueKey="MSG_USR_STATUS_ALL" searchable="false" width="auto"
						modified="JavaScript:listLink(this);"/>
			<% } %>
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
				<% } %>
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>
		</mtl:contentGroup>
	</mtl:form>
</body>
</mtl:html>
