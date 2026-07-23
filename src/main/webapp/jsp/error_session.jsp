<%--
	File Name:	error_session.jsp
	Version:	2.2.4(dpr)

	Description:

	Note:
		systemConfig.getProperty()
			"defaultPartyId"

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2015/04/30		2.2.4	maintenanceNotice: usingNoticeMarquee 인자 추가
	jbaek		2014/12/31		2.2.3	maintenanceNotice 적용
	lsinji		2008/09/26		2.2.2	dpr용으로 수정
	stghr12		2008/03/31		2.2.1	killuser를 언제나 넘기도록 수정
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리, 'menu_tesco.css' 삭제
										wintype이 sub일 때 처리
										defaultPartyId 사용하는 부분 수정
	stghr12		2006/07/12		2.0.1	'common.js' 삭제
	stghr12		2006/02/28		2.0.0	create
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
	String defaultPartyId = systemConfig.getProperty( "defaultPartyId" );
	if( "".equals(defaultPartyId) ) defaultPartyId = null;

	boolean usingSaveId = com.irt.rbm.RBMSystem.getSystemEnvBool( "DPR", "usingSaveId", false );
	boolean usingNoticeMarquee = "Y".equals(request.getAttribute("isMaintenanceWindow"))
			|| ( request.getAttribute("maintenanceNotices") != null && !((java.util.List)request.getAttribute("maintenanceNotices")).isEmpty() );
%>
<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= systemConfig.getSystemName() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>
	<link rel='stylesheet' href='style/common.css'/>
	<style type='text/css'>
	* { font: 12px/16px "tahoma" }
		form { margin: 0; }
		table, img { border: 0; }
		ul {
			margin: 0;
			list-style-type: none;
		}

		/**********************************************
		*	Form layout
		**********************************************/
		table#sect_layout {
			background-color: #CFEDF4;
			text-align: center;
			width: 325px;
			margin: 3px;
		}

		.msg {
			font-weight: bold; color: #FF0000;
			text-align: center; vertical-align: bottom;
			margin: 0; padding: 0; height: 25px;
		}

		ul, li { margin: 0; padding: 0; }

		ul li {
			font: bold 13px/16px "tahoma"
			height: 20;
			padding-top: 1;
		}

		ul li img {
			vertical-align: bottom;
			margin-bottom: 2px;
		}

		.field {
			background-color: #FFF;
			width: 108px; height: 18px;
			border: 1px solid #A9A9AA;
			margin: 0px; margin-left: 5px;
			padding: 0px;
			ime-mode: inactive;
		}
	</style>

	<script type='text/javascript' src='script/main.js'></script>
	<script type='text/javascript'>
		function bodyLoad() {
			<% if( "sub".equals(htmlpage.getWindowType()) ) { %>
				window.resizeTo( 600, 400 );
			<% } %>

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

		function checkInput() {
			if( frmMain.userId.value == "" ) {
				alert( "<mtl:message key="jsp.error_session.MSG_ENTER_USERID" encodeScript="true"/>" );
				frmMain.userId.focus();
				return false;
			}
			if( frmMain.password.value == "" ) {
				alert( "<mtl:message key="jsp.error_session.MSG_ENTER_PASSWORD" encodeScript="true"/>" );
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

		function roundTable( objectId ) {
			var object = document.getElementById( objectId );
			var border = object.getAttribute( 'rborder' );
			var bdcolor = object.getAttribute( 'rbdcolor' );
			var bgcolor = object.getAttribute( 'rbgcolor' );
			var radius = parseInt( object.getAttribute('radius') );
			var styleWidth = ( border ? border : '1px' );
			var styleHeight = ( border ? border : '1px' );

			var table = document.createElement( 'TABLE' );
			var tableBody = document.createElement( 'TBODY' );
			table.cellPadding = 0;
			table.cellSpacing = 0;
			table.className = 'rounded';
			table.appendChild( tableBody );

			var parentObject = object.parentNode;
			parentObject.insertBefore( table, object );
			parentObject.removeChild( object );

			for( var tridx = - radius; tridx <= radius; tridx++ ) {
				var space = radius - Math.abs( tridx );
				var objectTR = document.createElement( 'TR' );
				for( var tdidx = - radius; tdidx <= radius; tdidx++) {
					var objectTD = document.createElement( 'TD' );

					objectTD.style.width = styleWidth;
					objectTD.style.height = styleHeight;
					var idx = Math.abs( tdidx );
					if( idx == space )
						objectTD.style.backgroundColor = bdcolor;
					else if( idx < space )
						objectTD.style.backgroundColor = bgcolor;
					if( tridx == 0 && tdidx == 0 ) objectTD.appendChild( object );

					objectTR.appendChild( objectTD );
				}
				tableBody.appendChild( objectTR );
			}
		}
	</script>

	<style type='text/css'>
		.marquee {
			position: relative;
			overflow: hidden;
			width: 100%;
			height: 22px;
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

<body onLoad='JavaScript:bodyLoad();' style='text-align: center;'>
	<form name='frmMain' method='post' action='<%=systemConfig.getClassURL()%>/Login' onSubmit='return checkInput();'>
	<input type='hidden' name='mode' value='login'>
	<input type='hidden' name='sessionerr' value='sessionerr'/>
	<input type='hidden' name='killuser' value='true'>
	<mtl:hidden id="request" key="locale"/>
	<% if( htmlpage.getBackURL() != null ) { %>
		<input type='hidden' name='url' value='<%= htmlpage.getBackURL() %>'>
	<% } %>


	<table width='100%' height='100%'><tr><td align='center'>
		<table><tr><td>
		<div style='border: 3px solid #EEEEEE; width: 320px; position: absolute; left: 50%; top: 50%; margin-top: -120px;
				margin-left: -150px; padding: 5px;'>
			<p style='text-align: left; height: 50px; margin: 0 0 5px 5px; padding-top: 10px;'>
				<img src='images/errorss_img.gif' style='margin-left: 20px; margin-right: 10px;'>
				<img src='images/sect_title_login.gif'>
			</p>
		<% if( "Y".equals(request.getAttribute("isMaintenanceWindow")) ) { %>
			<div style='background: #E1E1E1; padding-bottom: 30px;'>
			<div class='msg'><%= HtmlUtility.toHtmlString( request.getAttribute("maintenanceMessage") ) %></div>
			</div>
		<% } else { %>
			<div style='background: #E1E1E1; padding-bottom: 30px;'>
				<div class='msg' style='height: auto;'><%= HtmlUtility.toHtmlString(htmlpage.getMessage()) %></div>
				<ul style='width: 200px; float: left; text-align: right;'>
					<li><img src='images/errorss_userid.gif'><input type='text' name='userId' class='field'>
					<li><img src='images/errorss_password.gif'><input type='password' name='password' class='field'>
					<li style='font-weight: normal; float:left; margin-left: 52px;'>
					<% if( usingSaveId ) { %>
						<label for='saveId_Y'><img src='images/errorss_saveid.gif'><input type='checkbox' name='saveId' id='saveId_Y'></label>
					<% } %>
				</ul>
				<ul style='padding-top: 0; text-align: left;'>
					<li><input type='image' src='images/errorss_btn_login.gif' style='margin-left: 5;'>
				</ul>
			</div>
		<% } %>
		</div>
		</td></tr></table>

		<div id='maintenanceNoticesDiv' style='margin-top: 150px;'>
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
	</td></tr></table>

	</form>
</body>
</mtl:html>
