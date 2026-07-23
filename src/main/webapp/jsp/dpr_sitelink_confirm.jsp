<%--
	File Name:	dpr_sitelink_confirm.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2014/03/31		2.2.1	CrossBrowsing Àû¿ë
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
<%
	java.util.Map<String, Object> record = (java.util.Map<String, Object>)pageContext.findAttribute( "record" );
	String url = (String)record.get( "requestURL" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<link rel='stylesheet' href='style/menu_portal.css'/>
	<style type='text/css'><!--
		div.warning {
			background: #FFFFFF;
			border: 1px solid #AAAAAA;
			margin-bottom: 5px;
			padding: 10px;
		}

		h3 {
			text-decoration: underline;
			text-align: center;
			color: #FF0000;
			font: bold 2em "times";
			margin-left: 50px; margin-right: 50px;
		}

		div p {
			text-align: center;
		}

		tr, td { margin: 0 0 0 ; font-family: tahoma, Verdana, Arial, Helvetica, sans-serif; font-size:12px; color:#767676; }
	//--></style>
	<script type='text/javascript'>

		function siteLinkReq( isAgree ) {
			if( isAgree == "Y" ) {
				var url = "<%= url %>";
				window.open( url, "_blank" );
			}

			windowClose();
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
			<div class='warning'>
				<h3>
					<mtl:message key="MSG_PUB_WARNING_TITLE"/>
				</h3>

				<p>
					<mtl:message key="MSG_PUB_WARNING_CONTENT1"/><br>
					<mtl:message key="MSG_PUB_WARNING_CONTENT2"/>
				</p>
			</div>

			<div style='text-align: center; margin-top: 20px;'>
				<mtl:button type="agree" onClick="JavaScript:siteLinkReq(\"Y\");" messageKey="jsp.BTN_YES" styleClass="primary"/>
				<mtl:button type="disagree" onClick="JavaScript:siteLinkReq(\"N\");" messageKey="jsp.BTN_NO" style="margin-left: 10px;"/>
			</div>
		</mtl:form>
	</div>
</body>
</mtl:html>
