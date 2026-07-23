<%--
	File Name:	dpr_order_freegoods_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2019/07/31		2.2.0	create
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
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			if( document.all.list_maxcount ) document.all.list_maxcount.innerHTML = document.all._list_maxcount_.innerHTML;
			setInnerHTML( parent.document.all.freegoods_list, document.all.freegoods_list.innerHTML );
			setInnerHTML( parent.document.all.freegoods_msg, "<mtl:message key="jsp.dpr_order_freegoods_list.MSG_FREEGOODS_ORDER"/>" );
			var tableObj = parent.document.getElementsByClassName( "list_content_data_scroll" );
			if( tableObj ) {
				if( tableObj.length > 1 ) {
					for( var i = 0; i < tableObj.length; i++ ) {
						fitHeightToWindow( tableObj[i], 220 );
					}
				} else {
					fitHeightToWindow( tableObj, 220 );
				}
			}
		}

		function setInnerHTML( contentObj, html ) {
			if( contentObj ) contentObj.innerHTML = html;
		}
	</script>
</head>

<body onload="JavaScript:bodyLoad();">
<%@ include file="include_pub_list.inc" %>
	<span id='freegoods_list'>
		<table id='content_title' cellspacing='0' cellpadding='0'>
		<tr>
			<td class='title' nowrap><%= HtmlUtility.toHtmlString(htmlpage.getTitle()) %></td>
			</tr>
		</table>
		<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
<!-- 			<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px'>
			<tr><td class='list_content_top'>
			</td></tr>
			</table> -->
			<table class='list_content_top msg' style='position: absolute; padding:3 0 3 5' cellspacing='0' cellpadding='0'>
				<tr><td><pre id='msg'><font color='red'><mtl:message key="jsp.dpr_order_freegoods_list.MSG_FREEGOODS_LIST"/></font></pre></td></tr>
			</table>
			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
			</td></tr>
			</table>

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );
				listWriter.print( out );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<% if( listWriter.containsData() && sessionMng.isAuthorized("DPR", "DPROrder.MNG") ) { %>
					<mtl:button type="download" styleClass="btn_list" href="JavaScript:freegoodsDownloadReq()"/>
				<% } %>
			</td></tr>
			</table>

		</mtl:contentGroup>
		</mtl:form>
	</span>
</body>
</mtl:html>