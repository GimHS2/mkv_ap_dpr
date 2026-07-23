<%--
	File Name:	dpr_main_sitelink.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2014/03/31		2.2.1	CrossBrowsing Àû¿ë
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
	<link rel='stylesheet' href='style/menu_portal.css'/>
	<style type='text/css'><!--
		tr, td { margin: 0 0 0 ; font-family: tahoma, Verdana, Arial, Helvetica, sans-serif; font-size:12px; color:#767676; }
	//--></style>
	<script type='text/javascript'>
		function directLinkReq( linkURL ) {
			window.open( linkURL, "winSiteLink", "location=no, menubar=no, scrollbars=yes, resizable=yes" );
		}

		function confirmSiteLinkReq( linkURL ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRSiteLink?mode=wcf";
			url = replaceQueryValue( url, "requestURL", linkURL );

			url = attachDefaultParameter( url, false );

			window.open( url, "winSiteLink", "location=no, menubar=no, scrollbars=no, resizable=no, width=600, height=220" );
		}

		function siteLinkListReq() {
			parent.siteLinkListReq();
		}

	</script>
</head>

<body style='margin: 0px;'>
	<%@ include file="include_pub_list.inc" %>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
	<div class='list_content_data_scroll' style='overflow-y: auto; height: 150px;'>
		<table width='100%' cellpadding='2' cellspacing='1' class='board_index'>
		<tr>
			<th align='left' width='60%'><mtl:message key="jsp.dpr_main_sitelink.FIELD_DESCRIPTION"/></th>
			<th align='left'><mtl:message key="jsp.dpr_main_sitelink.FIELD_URL"/></th>
		</tr>
		<mtl:loop id="sites" loopId="loop" loopIndex="index">
		<tr height='20'>
			<td align='left'><mtl:value id="loop" key="description"/></td>
			<td align='left'>
				<a href='JavaScript:confirmSiteLinkReq("<%= loop.get("linkURL") %>");'><mtl:value id="loop" key="linkURL"/></a>
			</td>
		</tr>
		</mtl:loop>
		</table>
		</div>
	</mtl:form>
</body>
</mtl:html>
