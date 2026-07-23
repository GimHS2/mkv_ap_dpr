<%--
	File Name:	dpr_productreq_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2017/08/31		2.2.0	create
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

<%
	String type = property.getProperty( "type" );
%>

<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			if( document.all.list_maxcount ) document.all.list_maxcount.innerHTML = document.all._list_maxcount_.innerHTML;
			setInnerHTML( parent.document.all.shortage_list, document.all.shortage_list.innerHTML );
		}

		function setInnerHTML( contentObj, html ) {
			if( contentObj ) contentObj.innerHTML = html;
		}
	</script>
</head>

<body onload="JavaScript:bodyLoad();">
<%@ include file="include_pub_list.inc" %>
	<span id='shortage_list'>
		<table id='content_title' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='title' nowrap><%= HtmlUtility.toHtmlString(htmlpage.getTitle()) %></td>
			</tr>
		</table>
		<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px'>
			<tr><td class='list_content_top'>
			</td></tr>
			</table>

			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
			</td></tr>
			</table>

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );
				if( sessionMng.isAuthorized("DPR", "DPRProductRequire.DOWN") && !"sim".equals(type) )
					listWriter.setCheckboxTypeAndNumbering( com.irt.custom.ListWriter.CHECKBOXTYPE_CHECK );
				listWriter.print( out );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<% if( listWriter.containsData() && sessionMng.isAuthorized("DPR", "DPRProductRequire.DOWN") ) { %>
					<mtl:button type="download" styleClass="btn_list" href="JavaScript:shortageDownloadReq()"/>
				<% } %>
			</td></tr>
			</table>
		</mtl:contentGroup>
		</mtl:form>
	</span>
	<iframe name='subwin_util' style='display: none' src='<%= systemConfig.getProperty("baseURL") %>blank.html'></iframe>
</body>
</mtl:html>