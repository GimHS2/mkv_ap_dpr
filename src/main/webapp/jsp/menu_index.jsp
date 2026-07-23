<%--
	File Name:	menu_index.jsp
	Version:	2.2.3

	Description:
		IMPORTANT! customize 대상파일

	Note:
		systemConfig.getProperty()
			"initMenuKey"
			"initPage"

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/01/30		2.2.4	top frame 이름을 topmenu 로 변경.( window.frames['topmenu']으로 검색 가능하도록 )
	hankalam	2015/10/30		2.2.3	웹취약성 수정. locale, menu 값 pageConfig 에서 갖고오도록 수정
	jbaek		2014/03/31		2.2.2	Cross Browsing 적용
	stghr12		2008/03/31		2.2.1	menuClose(), menuOpen(): fitHeightToWindow() 사용
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
										"locale" 처리
	GimHS		2007/04/30		2.1.1	'top' 크기 수정: 86 -> 73
	stghr12		2006/12/01		2.1.0	JavaScript 수정: menuOpen(), menuClose()에 height parameter 추가
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/18		1.0.0	create
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
	String top_url, main_url;
	String localeValue = htmlpage.getLocale().getLanguage();
	String debugSQLValue = request.getParameter( "debugSQL" );

	top_url = systemConfig.getClassURL() +"/Menu?mode=menu";
	String type = request.getParameter( "type" );
	String menuKey = htmlpage.getSystemMenu();
	if( menuKey == null || menuKey.length() == 0 )
		menuKey = systemConfig.getProperty( "initMenuKey" );
	if( type != null ) top_url += "&type="+ type;
	if( localeValue != null && localeValue.length() > 0 ) top_url += "&locale="+ localeValue;
	if( debugSQLValue != null && debugSQLValue.length() > 0 ) top_url += "&debugSQL="+ debugSQLValue;

	if( menuKey != null ) {
		top_url += "&menu="+ menuKey;
		if( htmlpage.getBackURL() == null ) top_url += "&click=on";
	}

	main_url = htmlpage.getBackURL();
	if( main_url == null || main_url.length() == 0 && menuKey == null ) {
		main_url = systemConfig.getProperty( "initPage" );
		if( main_url == null ) {
			top_url += "&menu=init";
			main_url = "";
		} else {
			if( localeValue != null && localeValue.length() > 0 )
				main_url = HtmlUtility.replaceURLQuery( main_url, "locale", localeValue );
			if( debugSQLValue != null && debugSQLValue.length() > 0 )
				main_url = HtmlUtility.replaceURLQuery( main_url, "debugSQL", debugSQLValue );
		}

	}
	//set organizationCode
	if( sessionMng.getExtraValue() != null )
		main_url = HtmlUtility.replaceURLQuery( main_url, "organizationCode", sessionMng.getExtraValue() );
%>
<head>
	<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= systemConfig.getSystemName() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>
	<script type='text/javascript'>
		var menuOutFunction = null;

		function menuClose( height ) {
			document.body.rows = ( height ? height : 20 ) +",*";
			if( window.frames["main"] && window.frames["main"].fitHeightToWindow )
				window.frames["main"].fitHeightToWindow();
		}

		function menuOpen( height ) {
			document.body.rows = ( height ? height : 80 ) +",*";
			if( window.frames["main"] && window.frames["main"].fitHeightToWindow )
				window.frames["main"].fitHeightToWindow();
		}

		function menuOut() {
			if( menuOutFunction ) menuOutFunction();
		}
	</script>
</head>

<frameset rows='80,*' frameborder='0' border='0' framespacing='0'>
	<frame name='topmenu' src='<%= top_url %>' scrolling='no' noresize='true' onMouseOut='JavaScript:menuOut();'/>
	<frame name='main' src='<%= main_url %>' scrolling='yes' noresize='true'/>
</frameset>
</mtl:html>
