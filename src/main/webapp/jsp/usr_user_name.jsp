<%--
	File Name:	usr_user_name.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2014/03/31		2.2.1	Cross Browsing 적용
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
	stghr12		2007/04/30		2.1.0	version up(not changed)
	stghr12		2006/02/28		2.0.0	version up(usr_id_name.jsp -> usr_user_name.jsp)
	stghr12		2004/06/21		1.0.0	create
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
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8' pageEncoding='euc-kr'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<%@ include file="include_pub_select.inc" %>
	<script type='text/javascript'>
		<% if( "Q".equals(request.getParameter("namecls")) ) { %>
			var value = "<mtl:value id="record" key="uniqId" encodeScript="true"/>;<mtl:value id="record" key="userName" encodeScript="true"/>";
		<% } else { %>
			var value = "<mtl:value id="record" key="userId" encodeScript="true"/>;<mtl:value id="record" key="userName" encodeScript="true"/>";
		<% } %>

		Select.setElementNames( "id", "name" );
		Select.setValue( new Array(value) );
	</script>
</head>

<body></body>
</mtl:html>
