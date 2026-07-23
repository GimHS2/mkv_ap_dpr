<%--
	File Name:	dpr_user_alert.jsp
	Version:	2.2.0(dpr)

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
	<script type='text/javascript'>
		function bodyLoad() {
			var expireAlert = "<%= (String)pageContext.findAttribute( "expireAlert" ) %>";

			if( expireAlert == "Y" )
				customPopup.alert( { "header" : "<%= msghandler.getMessage( "jsp.login.MSG_BEFORE_EXPIRE_PASSWORD", (String)pageContext.findAttribute("remainExpireAlertDay") ) %>" } );

			var url = "<%= systemConfig.getClassURL() %>/Menu";
			url = replaceQueryValue( url, "alerted", "Y" );
			url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );

			window.open( url, "_self" );
		}
	</script>
</head>

<body>
</body>
</mtl:html>
