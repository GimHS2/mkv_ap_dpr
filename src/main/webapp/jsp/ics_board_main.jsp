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
	String listWidth = "760px";
	String boardClass;
	String list_url, info_url;
	String localeValue = htmlpage.getLocale().getLanguage();
	list_url = systemConfig.getClassURL();
	info_url = systemConfig.getClassURL();
	if( "helpboard".equals(property.getProperty("type")) ) {
		list_url += "/ICSHelpBoard?mode=list&";
		info_url += "/ICSHelpBoard?";
		listWidth = "1000px";
	} else {
		list_url += "/ICSBoard?mode=list&boardClassCode=" + request.getParameter( "boardClassCode" ) + "&";
		info_url += "/ICSBoard?boardClassCode=" + request.getParameter( "boardClassCode" ) + "&";
	}

	String menuKey = htmlpage.getSystemMenu();
	if( menuKey == null || menuKey.length() == 0 )
		menuKey = systemConfig.getProperty( "initMenuKey" );
	if( localeValue != null && localeValue.length() > 0 )
		list_url += ("&locale="+ localeValue);

	if( menuKey != null ) {
		list_url += ("&menu=" + menuKey);
	}

	String boardNumber = request.getParameter( "boardNumber" );
	if( boardNumber != null ) {
		info_url += "mode=info";
		info_url += "&boardNumber=" + request.getParameter( "boardNumber" );
	} else {
		info_url += "mode=blank";
		info_url += "&boardNumber=" + request.getParameter( "boardNumber" );
	}
	info_url += ( "&menu=" + menuKey );
	info_url += ( "&locale=" + localeValue );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>

	<script type='text/javascript'>

		$(function() {
			$("#menu_content").bind( "load", function () {
				if( $(".content-overlay .loading").is(":visible") ) {
					toggleLoading( false );
				}
			});
			$("#main_content").bind( "load", function () {
				if( $(".content-overlay .loading").is(":visible") ) {
					toggleLoading( false );
				}
			});
		});
	</script>
</head>

<body class='frame-content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<div class='table w100p content' style='min-height: 500px;'>
		<div class='cell' style='width: 45%; min-width: <%= listWidth %>;'>
			<iframe id='menu_content' name='menu_content' class='menu-content' src='<%= list_url %>'></iframe>
		</div>
		<div class='cell' style='min-width: 760px;'>
			<iframe id='main_content' name='main_content' class='main-content' src='<%= info_url %>'></iframe>
		</div>
	</div>
</body>
<%@ include file="include_dpr_tail.inc" %>
</mtl:html>
