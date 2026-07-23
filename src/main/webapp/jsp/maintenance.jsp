<%--
	File Name:	maintenance.jsp(DPR)
	Version:	2.2.0

	Description:
		DPR login.jsp

	Note:

	Motified	(YYYY/MM/DD)	Ver		Content
	hankalam	2017/02/28		2.2.0	create
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
	boolean isLoginUser = ( sessionMng != null && sessionMng.isLoginUser() );
	String defaultPartyId = systemConfig.getProperty( "defaultPartyId" );
	if( "".equals(defaultPartyId) ) defaultPartyId = null;

	java.util.Locale locale = null;
	String locale_param = property.getProperty( "locale" );
	if( locale_param != null )
		locale = new java.util.Locale( locale_param );
	else
		locale = htmlpage.getLocale();

	boolean usingSaveId = com.irt.rbm.RBMSystem.getSystemEnvBool( "DPR", "usingSaveId", false );
	boolean usingNoticeMarquee = "Y".equals(request.getAttribute("isMaintenanceWindow"))
			|| ( request.getAttribute("maintenanceNotices") != null && !((java.util.List)request.getAttribute("maintenanceNotices")).isEmpty() );
%>
<html>
<head>
	<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8' />
	<meta http-equiv='Content-Style-Type' content='text/css' />
	<meta http-equiv='Content-Script-Type' content='text/javascript' />
	<title><%= systemConfig.getSystemName() %></title>
	<base href='<%= systemConfig.getBaseURL(locale) %>'/>
	<link rel='stylesheet' href='style/common.css'>

	<style type='text/css'><!--
		body {
			background: #FFFFFF;
			margin: 0; padding: 0;
		}

		div.login {
			background: url(images/login_jnj/login_bg.jpg) center bottom no-repeat;
			text-align: center;
			height: 260px;
			border-bottom: 3px solid #B0B0B0;
			margin: 120px 0 0 0;
		}

		div.login_box {
			font-weight: bold; font-size: 18px; color: #D00019;
			text-align: center;
			letter-spacing: normal;
			border: 3px solid #DBDBDB;
			width: 280px;
			height: 50px;
			margin-top: 30px; padding: 15px;
			float: right;
		}

		div.login_box p.msg {
			font-weight: bold; font-size: 11px; color: #D00019;
			text-align: center;
			height: 13px;
			margin: 0; margin-bottom: 2px;
			word-wrap: break-word;
		}

		div.login_box td.field {
			font: 12px "arial"; color: #747474;
			text-align: right;
			height: 23px;
		}

		td.field img {
			vertical-align: bottom;
			margin-left: 9px; margin-right: 2px;
			margin-top: 3px; margin-bottom: 2px;
			float: left;
		}

		input.field {
			font-size: 12px;
			border: 1px solid #999;
			width: 104px; height: 18px;
			padding: 0;
		}

		select.field {
			font-size: 11px;
		}

	//--></style>

	<script type='text/javascript' src='script/main.js'></script>
	<script type='text/javascript'>
		function bodyLoad() {
		<% if( usingNoticeMarquee ) { %>
			marqueeStart();
		<% } else { %>
			<% if( usingSaveId ) { %>
				frmMain.saveId.checked = getCookie( "saveId" );
				if( frmMain.saveId.checked ) {
					if( frmMain.userId.value == "" )
						frmMain.userId.value = getCookie( "userId" );
				}
			<% } %>

			self.focus();
			if( frmMain.userId.value == "" )
				frmMain.userId.focus();
			else {
				frmMain.password.select();
				frmMain.password.focus();
			}
		<% } %>
		}

		function changeLanguage( language ) {
			if( language ) {
				var url = "<%= systemConfig.getClassURL() %>/Login?locale="+ language;

				window.open( url, "_self" );
			}
		}

		function checkInput() {
			if( frmMain.userId.value == "" ) {
				alert( "<mtl:message key="jsp.login.MSG_ENTER_USERID" encodeScript="true"/>" );
				frmMain.userId.focus();
				return false;
			}
			if( frmMain.password.value == "" ) {
				alert( "<mtl:message key="jsp.login.MSG_ENTER_PASSWORD" encodeScript="true"/>" );
				frmMain.password.focus();
				return false;
			}

			var expire = new Date();
			<% if( usingSaveId ) { %>
				if( frmMain.saveId.checked )
					expire.setTime( expire.getTime() + 1000*60*60*24*365 );

				setCookie( "saveId", frmMain.saveId.checked, expire );
				setCookie( "userId", frmMain.userId.value, expire );
			<% } %>

			return true;
		}

		function logoutReq() {
			if( confirm("<mtl:message key="MSG_CONFIRM_LOGOUT" encodeScript="true"/>") ) {
				var url = "<%= systemConfig.getClassURL() %>/Login?mode=logout";
				url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );

				window.open( url, "_top" );
			}
		}
	</script>

<script language='javascript'>
	today=new Date();
	endyear=today.getFullYear();
</script>
	<style type='text/css'>
		.marquee {
			position: relative;
			overflow: hidden;
			width: 100%;
			height: 1.5em;
			border: solid black 1px;
		}
		.marquee span {
			white-space:nowrap;
		}
	</style>
	<script type='text/javascript'>
		// Continuous Text Marquee
		// copyright 30th September 2009by Stephen Chapman
		// http://javascript.about.com
		// permission to use this Javascript on your web page is granted
		// provided that all of the code below in this script (including these
		// comments) is used without any alteration
		function objWidth(obj) {if(obj.offsetWidth) return  obj.offsetWidth; if (obj.clip) return obj.clip.width; return 0;} var mqr = []; function mq(id){this.mqo=document.getElementById(id); var wid = objWidth(this.mqo.getElementsByTagName('span')[0])+ 5; var fulwid = objWidth(this.mqo); var txt = this.mqo.getElementsByTagName('span')[0].innerHTML; this.mqo.innerHTML = ''; var heit = this.mqo.style.height; this.mqo.onmouseout=function() {mqRotate(mqr);}; this.mqo.onmouseover=function() {clearTimeout(mqr[0].TO);}; this.mqo.ary=[]; var maxw = Math.ceil(fulwid/wid)+1; for (var i=0;i < maxw;i++){this.mqo.ary[i]=document.createElement('div'); this.mqo.ary[i].innerHTML = txt; this.mqo.ary[i].style.position = 'absolute'; this.mqo.ary[i].style.left = (wid*i)+'px'; this.mqo.ary[i].style.width = wid+'px'; this.mqo.ary[i].style.height = heit; this.mqo.appendChild(this.mqo.ary[i]);} mqr.push(this.mqo);} function mqRotate(mqr){if (!mqr) return; for (var j=mqr.length - 1; j > -1; j--) {maxa = mqr[j].ary.length; for (var i=0;i<maxa;i++){var x = mqr[j].ary[i].style;  x.left=(parseInt(x.left,10)-1)+'px';} var y = mqr[j].ary[0].style; if (parseInt(y.left,10)+parseInt(y.width,10)<0) {var z = mqr[j].ary.shift(); z.style.left = (parseInt(z.style.left) + parseInt(z.style.width)*maxa) + 'px'; mqr[j].ary.push(z);}} mqr[0].TO=setTimeout('mqRotate(mqr)',40);}
	</script>
	<script type='text/javascript'>
		function getStyle(elid,styleProp) {
			el = document.getElementById(elid);
			var result;
			if(el.currentStyle) {
				result = el.currentStyle[styleProp];
			} else if (window.getComputedStyle) {
				result = document.defaultView.getComputedStyle(el,null)
					.getPropertyValue(styleProp);
			} else {
				result = 'unknown';
			}
			return result;
		}

		function marqueeStart() {
			var isMaintenanceWindow = false;
			var maintenanceNoticesHeight = 0;

			var m = null;
			var isIE = /*@cc_on!@*/false || !!document.documentMode; // At least IE6
			if( isIE ) {
				var div = document.getElementById( 'maintenanceNoticesDiv' );
				m = div.getElementsByTagName( 'div' );//IE에서 document.getElementsByName으로 object를 찾지 못하여 div에서 찾음.
			} else {
				m = document.getElementsByName( 'maintenanceNotice' );
			}
			for( i=0; i < m.length; i++ ) {
				if( m[i].getAttribute('name') == 'maintenanceNotice' ) {
					maintenanceNoticesHeight += parseInt( getStyle(m[i].id, 'height').replace("px", "") );
					new mq( m[i].id );
				}
			}
			var loginDiv = document.getElementById( 'login' );
			if( loginDiv )
				loginDiv.style['margin-top'] = maintenanceNoticesHeight +"px";

			mqRotate(mqr); // must come last
		}
	</script>
</head>

<body onLoad='JavaScript:bodyLoad();'>
	<form name='frmMain' method='post' action='<%=systemConfig.getClassURL()%>/Login' onSubmit='return checkInput();' autocomplete='off'>
	<input type='hidden' name='mode' value='login'>
	<input type='hidden' name='menu' value='portal'>
	<input type='hidden' name='type' value='dpr'>
	<input type='hidden' name='killuser' value='true'>
	<div style=' top: 25px; left: 25px; white-space: normal; text-align: normal; position: relative;'>
		<p style='margin: 0;'><img src='images/login_jnj/login_customer_logo.gif'></p>
	</div>

	<div class='login'>
	<div style='width: 100%; margin: 0 0 30px 0; overflow: hidden; text-align: center; white-space: nowrap; font-size: 1em; letter-spacing: 2.5em; line-height: .6em;'>
		<div style='width: 380px; vertical-align: top; margin-top: 20px; white-space: normal; text-align: normal; display: inline-block; float: none;'>
			<p style='margin: 0;'><img src='images/login_jnj/login_client_logo.gif'></p>
			<p style='margin: 0;'><img src='images/login_jnj/login_summary.gif'></p>
		</div>
		<div class='login_box' style='width: 280px; white-space: normal; text-align: normal; display: inline-block; float: none;'>
		<p>Under maintenance.</p>
		</div>
	</div>
</div>
</form>

	<div id='maintenanceNoticesDiv'>
		<mtl:loop id="maintenanceNotices" loopId="loop">
			<div id='maintenanceNotice-<mtl:value key="boardNumber" id="loop"/>' name='maintenanceNotice' class='marquee'
				data-maintenance-window='<mtl:value id="loop" key="isMaintenanceWindow"/>'
				data-maintenance-start='<mtl:value id="loop" key="maintenanceStart"/>'
				data-maintenance-end='<mtl:value id="loop" key="maintenanceEnd"/>'
				data-maintenance-tz='<mtl:value id="loop" key="maintenanceTimeZone"/>'>
				<span><mtl:valuef id="loop" format="${content}"/></span>
			</div>
		</mtl:loop>
	</div>

	<%@ include file="include_dpr_login_tail.inc" %>
	<%@ include file="include_dpr_warning.inc" %>
</body>
</html>
</mtl:html>
