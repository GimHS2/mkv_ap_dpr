<%--
	File Name:	error_session2.jsp
	Version:	2.2.0

	Description:

	Note:
		systemConfig.getProperty()
			"defaultPartyId"

	Modified	(YYYY/MM/DD)	Ver		Content
	GimHS		2019/07/31		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding="euc-kr" %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	String defaultPartyId = systemConfig.getProperty( "defaultPartyId" );
	if( "".equals(defaultPartyId) ) defaultPartyId = null;
	String msg = HtmlUtility.toScriptString( htmlpage.getMessage() );
	if( msg != null ) {
		msg = msg.replaceAll( "(\r\n|\r|\n|\n\r)", " " );
	}
%>
<head>
	<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= systemConfig.getSystemName() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<script type='text/javascript' src='script/utils.js'></script>
	<script type='text/javascript'>
		function bodyLoad() {
			var url = "<%= systemConfig.getClassURL() %>/Login?mode=logout";
			url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );
			url = replaceQueryValue( url, "msg", encodeURIComponent("<%= msg %>") );
			window.open( url, "_top" );
		}
	</script>
</head>

<body onLoad='JavaScript:bodyLoad();'>
</body>
</mtl:html>
