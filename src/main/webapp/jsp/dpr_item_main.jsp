<%--
	File Name:	dpr_item_main.jsp
	Version:	2.2.2

	Description:
		IMPORTANT! customize 대상파일

	Note:
		systemConfig.getProperty()
			"initMenuKey"
			"initPage"

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2015/10/30		2.2.2	웹취약성 수정. locale, menu 값 pageConfig 에서 갖고오도록 수정
	jbaek		2014/03/31		2.2.1	CrossBrowsing 적용
	stghr12		2008/09/26		2.2.0	create
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

	String tree_url, main_url;
	String localeValue = htmlpage.getLocale().getLanguage();

	tree_url = systemConfig.getClassURL() + "/DPRItem?mode=tree&type=itm&btype=ord";
	String menuKey = htmlpage.getSystemMenu();
	if( menuKey == null || menuKey.length() == 0 )
		menuKey = systemConfig.getProperty( "initMenuKey" );
	if( localeValue != null && localeValue.length() > 0 )
		tree_url += ("&locale="+ localeValue);

	if( menuKey != null ) {
		tree_url += ("&menu=" + menuKey);
	}

	main_url = systemConfig.getClassURL() + "/DPRItem";
	main_url += ( "?menu=" + menuKey );
	main_url += ( "&locale=" + localeValue );
	main_url += "&vtype=frm";
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
</head>

<body class='frame-content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<div class='table w100p content' style='min-height: 500px;'>
		<div class='table-cell' style='width: 608px; min-width: 400px;'>
			<iframe id='menu_content' name='menu_content' class='menu-content' src='<%= tree_url %>'></iframe>
		</div>
		<div class='table-cell' style='min-width: 800px;'>
			<iframe id='main_content' name='main_content' class='main-content' src='<%= main_url %>'></iframe>
		</div>
	</div>
</body>
<%@ include file="include_dpr_tail.inc" %>
</mtl:html>
