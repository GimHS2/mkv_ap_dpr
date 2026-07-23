<%--
	File Name:	usr_unique_check.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2008/09/26		2.2.1	length_userid -> length_20 변경
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										windowResizeTo( 500 ); -> windowResizeTo( 600 );
										nonexist -> validable
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
<%
	boolean validable = "Y".equals( property.getProperty("validable") );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_select.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			var windowObj = window.opener;
			if( !windowObj )
				windowObj = window.parent;

			var validable = "<%= validable%>";
			windowObj.uniqueCheckPopup( validable, "<%= HtmlUtility.toScriptString(htmlpage.getMessage()) %>" );
		}
	</script>
</head>

<body>
</body>
</mtl:html>
