<%--
	File Name:	dpr_order_wait.jsp
	Version:	2.0.3

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/06/30		2.0.3	isRevOrd 추가
	jbaek		2019/08/30		2.0.2	isFirstSim 추가
	hankalam	2017/11/30		2.0.1	rtype 파라미터 추가
	lsinji		2008/09/26		2.0.0	create
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
<head>
	<%@ include file="include_rbm_header.inc" %>

	<script type='text/javascript'>
		function bodyLoad() {
			var mode = "<mtl:value id="request" key="type"/>";
			var orderKey = "<mtl:value id="request" key="orderKey"/>";
			var rtype = "<mtl:value id="property" key="rtype"/>";
			var url = "<%= htmlpage.getRequestURL() %>";
			url = replaceQueryValue( url, "mode", mode );
			url = replaceQueryValue( url, "orderKey", orderKey );
			url = replaceQueryValue( url, "ftype", "<mtl:value id="property" key="ftype"/>" );
			if( rtype != null && rtype != "" ) {
				url = replaceQueryValue( url, "rtype", "<mtl:value id="property" key="rtype"/>" );
			}
			if( String("<mtl:value id="property" key="isFirstSim"/>") === "Y" ) {
				url = replaceQueryValue( url, "isFirstSim", "Y");
			}
			if( String("<mtl:value id="property" key="isRevOrd"/>") === "Y" ) {
				url = replaceQueryValue( url, "origOrderKey", "<mtl:value id="property" key="origOrderKey"/>");
			}

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
