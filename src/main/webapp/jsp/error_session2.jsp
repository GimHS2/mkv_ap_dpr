<%--
	File Name:	error_session2.jsp
	Version:	2.2.0

	Description:
		Session 관련 에러 발생시 로그인 화면으로 이동하는 용도로 사용하는 JSP (frame 구조 때문에 메뉴 frame에 로그인 화면이 뜨는 경우는 보기 안좋아서..)

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
			var url = "<%= systemConfig.getHomepageURL( htmlpage.getLocale(), false ) %>";
			url = replaceQueryValue( url, "locale", "<%= htmlpage.getLocale().getLanguage() %>" );
			url = replaceQueryValue( url, "debugSQL", "<mtl:value id="request" key="debugSQL"/>" );
			url = replaceQueryValue( url, "msg", "<%= HtmlUtility.toScriptString(htmlpage.getMessage()) %>" );

			submitPost( url, "_top" );
		}
	</script>
</head>

<body onLoad='JavaScript:bodyLoad();'>
</body>
</mtl:html>
