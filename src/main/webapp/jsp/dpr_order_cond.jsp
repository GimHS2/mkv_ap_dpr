<%--
	File Name:	dpr_order_cond.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	guksm		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr'%>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Map<String, Object> conditionMap = (Map<String, Object>)pageContext.findAttribute( "condition" );
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
</head>

<body class='content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_dpr_order_cond.inc" %>

	<%@ include file="include_dpr_tail.inc" %>
</body>
</mtl:html>
