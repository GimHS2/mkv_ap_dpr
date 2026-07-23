<%--
	File Name:	usr_party_name.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
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
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<%@ include file="include_pub_select.inc" %>
	<script type='text/javascript'>
		var value = "<mtl:value id="record" key="partyId" encodeScript="true"/>;<mtl:value id="record" key="partyName" encodeScript="true"/>";

		Select.setElementNames( "id", "name" );
		Select.setValue( new Array(value) );
	</script>
</head>

<body></body>
</mtl:html>
