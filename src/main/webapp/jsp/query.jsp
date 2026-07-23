<%--
	File Name:	query.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	GimHS		2012/12/31		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding="UTF-8" %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<head>
	<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title>Query</title>
	<script language='JavaScript'>
		function bodyLoad() {
			var href = location.href;
			var name;

			if( href.indexOf("<") > 0 || href.indexOf("<") > 0 || href.indexOf("(") > 0 || href.indexOf(")") > 0 || href.indexOf("'") > 0 || href.indexOf("\"") > 0 || href.indexOf("..") > 0 )
				return;

			var idx = href.indexOf( "?name=" );
			if( idx < 0 ) idx = href.indexOf( "&name=" );

			if( idx >= 0 ) {
				idx = href.indexOf( "=", idx );
				var idx2 = href.indexOf( "&", ++idx );
				name = ( idx2 < 0 ? href.substring( idx ) : href.substring( idx, idx2 ) );

				var srcHTML = opener.document.getElementById( name );
				document.getElementById("detHTML").innerHTML = srcHTML.innerHTML;
			}
			self.focus();
		}
	</script>
</head>

<body onLoad='JavaScript:bodyLoad();'>
	<span id='detHTML'></span>
</body>
</mtl:html>
