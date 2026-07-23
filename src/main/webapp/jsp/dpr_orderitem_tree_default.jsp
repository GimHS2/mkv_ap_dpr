<%--
	File Name:	dpr_orderitem_tree_default.jsp
	Version:	2.2.0

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
		$(function() {
			if( parent.$("iframe.main-content") ) {
				$(".frame-content").innerHeight( parent.$(".main-content").contents().find( ".frame-content-wrap").innerHeight() );
			}
		});
	</script>
</head>
<body class='content' style='padding-left: 8px; overflow: hidden;'>
	<mtl:contentGroup groupId="tree" type="content" styleClass="frame-content" descriptionKey="jsp.dpr_order_input.GRP_ORDER_HEADER">
		<h2><mtl:message key="jsp.dpr_item_tree.MSG_PRODUCT_LIST"/></h2>
		<div class='table w100p' style='height: 100%;'>
			<div class='table-cell align-center' style='vertical-align: middle;'>
				<img src='images/alert.png'>
				<div class='blank-msg'><mtl:message key="jsp.dpr_orderitem_tree_default.MSG_BLANK"/></div>
			</div>
		</div>
	</mtl:contentGroup>
</body>
</mtl:html>
