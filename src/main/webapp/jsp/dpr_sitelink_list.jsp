<%--
	File Name:	dpr_partylink_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.0	create
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
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu;

		function confirmSiteLinkReq( linkURL ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRSiteLink?mode=wcf";
			url = replaceQueryValue( url, "requestURL", linkURL );

			url = attachDefaultParameter( url, false );

			window.open( url, "winSiteLink", "location=no, menubar=no, scrollbars=no, resizable=no, width=600, height=220" );
		}

		function directLinkReq( url ) {
			window.open( url, "winSiteLink", "location=no, menubar=no, scrollbars=yes, resizable=yes" );
		}

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "linkSequence" );
		}

		function infoReq( linkSequence ) {
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "linkSequence", linkSequence );
		}

		function modifyReq( linkSequence ) {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "linkSequence", linkSequence );
		}

		function linkMenuReq( linkSequence ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_SITELINK_INFO" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("INFO");' );

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "INFO" ) {
				infoReq( linkmenu.params[0] );
			}
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg";
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="request" key="countryCdoe"/>" );

			windowOpen( url + "&wintype=sub", "clsMng" );
		}
	</script>
</head>

<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px'>
			<tr><td class='list_content_top'>

			<% if( sessionMng.isSystemAdmin() ) { %>
				<mtl:contains id="countries">
					<mtl:select id="request" key="displayCountryCode" nullValueKey="jsp.dpr_sitelink.MSG_DPR_COUNTRY" hasBlank="true"
							listId="countries" listCodeKey="countryCode" listNameFormat="$H{countryName}" modified="JavaScript:listLink(this);"/>
				</mtl:contains>
			<% } %>
			</td></tr>
			</table>

			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
				<span id='list_showcount'><mtl:message key="jsp.SHOWCOUNT"/> <mtl:showcount modified="JavaScript:changeShowCount(this);"/></span>
			<td></tr>
			</table>

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );
				listWriter.print( out );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_list"/>
				<% if( htmlpage.hasManageAuth() ){ %>
					<mtl:button type="regist" styleClass="btn_list"/>
					<% if( listWriter.containsData() ) { %>
						<mtl:button type="modify" styleClass="btn_list"/>
						<mtl:button type="delete" styleClass="btn_list"/>
					<% } %>
				<% } %>
				<mtl:button type="close_if" styleClass="btn_list"/>
			</td></tr>
			</table>

			<table class='list_content_bottom' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='list_content_bottom' align='left' nowrap>
				<td>
				<td class='list_content_bottom' id='index_lst' align='center' width='100%' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
				<td class='list_content_bottom' id='index_btn' align='right' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
			</tr>
			</table>
		</mtl:contentGroup>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>


