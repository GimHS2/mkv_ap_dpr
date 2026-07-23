<%--
	File Name:	notice.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
										common.css 추가, style에서 font 종류 제외
	stghr12		2007/04/30		2.1.0	version up(not changed)
	stghr12		2006/02/28		2.0.0	create
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
	<link rel='stylesheet' href='style/common.css'/>
	<style type='text/css'><!--
		body {
			margin: 0;
			overflow: hidden;
		}
		div, pre {
			font: 12px; color: #333333;
			word-break: break-all; word-wrap: break-word;
		}
		img {
			border: 0;
		}
		div {
			scrollbar-face-color: #E8E8E8;
			scrollbar-shadow-color: #FFFFFF;
			scrollbar-highlight-color: #FFFFFF;
			scrollbar-3dlight-color: #E8E8E8;
			scrollbar-darkshadow-color:#BDBDBD;
			scrollbar-track-color: ;
			scrollbar-arrow-color: #0065AA;
		}
		div.notice {
			background: #F8F8F8;
			border: 1 solid #CECECE;
			width: 100%;
			margin-right: 20; margin-left: 20;
			padding: 10 10 10 10;
		}
	//--></style>

	<script type='text/javascript'>
		function bodyLoad() {
			window.resizeTo( 400, document.body.scrollHeight );
			window.resizeBy( 0, document.body.scrollHeight - document.body.clientHeight );
			self.focus();
		}
	</script>
</head>

<%
	java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
	String boardOption = (String)recordMap.get( "boardOption" );
%>
<body bgcolor='#F2F2F2' onLoad='JavaScript:bodyLoad();'>
	<div style='background: url(images/notice_title_bg.gif); padding-top: 19;'><img src='images/notice_title.gif'></div>

	<div class='notice' style='margin-top: 20; font: bold; position: relative;'><mtl:value id="record" key="title"/></div>

	<div class='notice' style='height: 250; margin-top: 5; margin-bottom: 5; overflow-y: auto;'>
	<% if( boardOption != null && boardOption.startsWith("H") ) { %>
		<mtl:value id="record" key="content" encodeHTML="false"/>
	<% } else { %>
		<pre><mtl:value id="record" key="content"/></pre>
	<% } %>
	</div>

	<div align='center' style='width: 100%; height: 40px; padding-top: 10;'>
		<a href="JavaScript:self.close();"><img src='images/notice_btn_close.gif'></a>
	</div>
</body>
</mtl:html>
