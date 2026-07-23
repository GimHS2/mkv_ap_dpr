<%--
	File Name:	dpr_stockquery_wait.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">

<%
	String queryKey = (String)pageContext.findAttribute( "queryKey" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			var url = "<%= htmlpage.getRequestURL() %>";
			url = replaceQueryValue( url, "mode", "sim" );
			url = replaceQueryValue( url, "queryKey", "<%= queryKey %>" );

			windowSelfOpen( url );
		}
	</script>
</head>

<body class='content'>
	<div class='content-overlay' style='display: flex;'>
		<div class='loading' style='display: block; padding: 60px;'>
			<p style='font-size: 24px; font-weight: bold; line-height: 200%; margin: 0; margin-bottom: 25px;'><%= htmlpage.getMessage() %></p>
			<img src="images/progress.gif">
		</div>
	</div>
</body>
</mtl:html>
