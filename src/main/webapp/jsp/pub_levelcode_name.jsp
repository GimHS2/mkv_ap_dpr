<%--
	File Name:	pub_levelcode_name.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
	stghr12		2007/04/30		2.1.0	version up(not changed)
	stghr12		2006/02/28		2.0.0	version up
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
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<%@ include file="include_pub_select.inc" %>
	<script type='text/javascript'>
		var value = "<mtl:value id="record" key="code" encodeScript="true"/>"
		value += ";<mtl:value id="record" key="classCode" encodeScript="true"/>";
		value += ";<mtl:value id="record" key="lowerCount" encodeScript="true"/>";
		<% if( "L".equals(request.getParameter("namecls")) ) { %>
			value += ";<mtl:value id="record" key="levelCode" encodeScript="true"/>";
			Select.setElementNames( null, null, null, "code", "name" );
		<% } else { %>
			Select.setElementNames( "code", null, null, "name" );
		<% } %>
		value += ";<mtl:value id="record" key="name" encodeScript="true"/>";

		Select.setValue( new Array(value) );
	</script>
</head>

<body></body>
</mtl:html>
