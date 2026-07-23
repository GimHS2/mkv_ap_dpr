<%--
	File Name:	dpr_order_main.jsp
	Version:	2.2.8

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.8	신규 UI/UX 적용
	jbaek		2019/07/30		2.2.7	dlvrySpBrands에 따른 odrdlvGroup 파라미터 추가
	hankalam	2017/05/31		2.2.6	같은 sold to, ship to 인 Order 상태가 Creating 일 때 중복 발주가 안되도록 메시지 출력 로직 추가
	hankalam	2016/08/31		2.2.5	reOrder 가 Y일 때만 parameter 추가
	song7981	2016/06/03		2.2.4	rdd refresh를 위한 parameter 추가
	hankalam	2015/10/30		2.2.3	웹취약성 수정. locale, menu 값 pageConfig 에서 갖고오도록 수정
	jbaek		2014/04/30		2.2.2	수직 스크롤 나오도록 변경.
	jbaek		2014/03/31		2.2.1	CrossBrowsing 적용
	guksm		2008/09/26		2.2.0	create
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
	String localeValue = htmlpage.getLocale().getLanguage();

	String tree_url;
	tree_url = systemConfig.getClassURL() + "/DPRItem?mode=tree&type=ord";
	String menuKey = htmlpage.getSystemMenu();
	if( menuKey == null || menuKey.length() == 0 )
		menuKey = systemConfig.getProperty( "initMenuKey" );
	if( localeValue != null && localeValue.length() > 0 )
		tree_url += ("&locale="+ localeValue);

	if( menuKey != null )
		tree_url += ("&menu=" + menuKey);

	String main_url = systemConfig.getClassURL() + "/DPRPlaceOrder";
	String mode = request.getParameter( "redirectMode" );
	if( "simr".equals(mode) )
		main_url += "?mode=" + mode;
	else
		main_url += "?mode=ior";

	main_url += ( "&menu=" + menuKey );
	main_url += ( "&locale=" + localeValue );

	String orderKey = property.getProperty( "orderKey" );
	String reOrder = property.getProperty( "reOrder" );
	String odrdlvGroup = property.getProperty( "odrdlvGroup" );
	if( orderKey != null && orderKey.length() > 0 ) {
		main_url += ( "&orderKey="+ orderKey );
		if( reOrder != null && reOrder.length() > 0 )
			main_url += ( "&reOrder="+ reOrder );
	}

	tree_url = systemConfig.getClassURL() + "/DPRItem?mode=treedef";
	tree_url += ( "&menu=" + menuKey );
	tree_url += ( "&locale=" + localeValue );
	if( odrdlvGroup != null && odrdlvGroup.length() > 0 ) {
		main_url += ( "&odrdlvGroup="+ odrdlvGroup );
		tree_url += ( "&odrdlvGroup="+ odrdlvGroup );
	}
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<script type='text/javascript'>
		var isContinueOrder= false;
	</script>
</head>

<body class='frame-content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<div class='table w100p content' style='min-height: 500px;'>
		<div class='table-cell' style='min-width: 800px;'>
			<iframe id='main_content' name='main_content' class='main-content' style='min-height:500px;' src='<%= main_url %>'></iframe>
		</div>
		<div class='table-cell' style='width: 608px; min-width: 510px;'>
			<iframe id='menu_content' name='menu_content' class='menu-content' style='min-height:500px;' src='<%= tree_url %>'></iframe>
		</div>
	</div>
</body>
<%@ include file="include_dpr_tail.inc" %>
</mtl:html>
