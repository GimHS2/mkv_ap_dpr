<%--
	File Name:	dpr_logisticstracking_info.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.Map, java.util.List" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
response.setHeader( "Cache-Control", "no-cache" );
response.setHeader( "Pragma", "no-cache" );
response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Map<String, Object> logisticsMap = (Map<String, Object>)pageContext.findAttribute( "record" );
	String deliveryNumber = (String)logisticsMap.get( "deliveryNumber" );
	boolean success = false;
	String trackingUrl = null;
	String errorMessage = null;
	if( !"loqi".equals(htmlpage.getMode()) ) {
		success = (Boolean)logisticsMap.get( "success" );
		if( success ) {
			trackingUrl = (String)logisticsMap.get( "3plRequestUrl" );
		} else {
			errorMessage = (String)logisticsMap.get( "errorMessage" );
		}
	}
%>
<head>
<%@ include file="include_rbm_header.inc" %>
<%@ include file="include_pub_input.inc" %>
<script type='text/javascript'>
	function bodyLoad() {
		windowResizeTo( 800, 825 );
	<% if( "loqi".equals(htmlpage.getMode()) ) { %>
		$("#message").html( "<mtl:message key="jsp.dpr_logisticstracking_info.MSG_QUERY"/>" );
		var url = "<%= htmlpage.getRequestURL() %>?mode=loq";
		url = replaceQueryValue( url, "deliveryNumber", "<%= deliveryNumber %>" );
		windowSelfOpen( url );
	<% } else {
		if( success ) {
	%>
			$("#message").html( "<mtl:message key="jsp.dpr_logisticstracking_info.MSG_SUCCESS"/>" );
			var url = "<%= htmlpage.getRequestURL() %>?mode=sndr";
			url = replaceQueryValue( url, "deliveryNumber", "<%= deliveryNumber %>" );
			window.open( url, "_self" );
	<% } else { %>
			$("#message").html( "<mtl:message key="jsp.dpr_logisticstracking_info.MSG_FAILED"/>" );
			var url = "<%= htmlpage.getRequestURL() %>?mode=sndr";
			url = replaceQueryValue( url, "deliveryNumber", "<%= deliveryNumber %>" );
			window.open( url, "_self" );
	<% } %>
	<% } %>
	}
</script>
<style>
	div.wrap { top: 20%; left: 0; width: 100%; border: 3px solid #FFAC70 }
	div.bg { width: 100%; height: 350px; background-color: #EEEEEE; }
	div.content { top: 50px; text-align: center; position: relative; min-width: 50em; max-width: 70em; padding: 30 0; margin: 0 auto; }
	div.content p { font-size: 2em; }
	button {
		border: none;
		color: white;
		padding: 15px;
		width: 120px;
		text-align: center;
		text-decoration: none;
		display: inline-block;
		font-weight: bold;
		font-size: 16px;
		margin: 4px 5px;
		cursor: pointer;
	}
	.btnRetry { background-color: #4CAF50; }
	.btnClose { background-color: #008CBA; }
</style>
</head>

<body class='content'>
	<div class="wrap">
		<div class="bg">
			<div class="content">
				<p><%= msghandler.getMessage( "jsp.dpr_logisticstracking_info.MSG_DELIVERYNUMBER", deliveryNumber ) %></p>
				<p id='message'></p>
			<% if( !"loqi".equals(htmlpage.getMode()) && !success ) { %>
				<!-- <button class="btnRetry" onclick="window.history.back()">RETRY</button>
				<button class="btnClose" onclick="window.close()">CLOSE</button> -->
			<% } %>
			</div>
		</div>
	</div>
</body>
</mtl:html>
