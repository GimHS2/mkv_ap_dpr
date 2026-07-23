<%--
	File Name:	usr_account_result.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2019/07/31		2.2.1	새창으로 뜰때 내용만큰 높이가 자동으로 조절되게 하기 위해 <div id='contentWrap'> 태그 추가
	hankalam	2016/07/31		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ page import="com.irt.html.*"%>
<%@ taglib uri="/mtltaglib" prefix="mtl"%>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">

<head>
	<%@ include file="include_rbm_header.inc"%>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<%@ include file="include_rbm_bodyheader.inc"%>
		<mtl:contentGroup groupId="msg" type="content">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='content0' width='100%'>
					<%= request.getAttribute( "resultMessage" )%>
				</td>
			</tr>
			</table>
		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr>
			<td><mtl:button type="return"/>
				<mtl:button	type="close_if"/>
			</td>
		</tr>
		</table>
	</div>
</body>
</mtl:html>
