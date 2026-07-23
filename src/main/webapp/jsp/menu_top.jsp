<%--
	File Name:	menu_top.jsp
	Version:	2.2.6

	Description:
		IMPORTANT! customize 대상파일

	Note:
		sub_menu.jsp가 changeEffectUserReq(), initPageReq(), noticeReq(), systemInfoReq()를 포함해야 함

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/07/30		2.2.6	changeLocale() 관련 bodyLoad 오류 수정.
	jbaek		2018/10/30		2.2.5	changeLocaleToFrame(): 'default' frame에 data-lang 삽입..
	jbaek		2017/10/30		2.2.4	changeLocale(): locale 변경 기능 추가
	hankalam	2016/09/30		2.2.3	mesgOrganization 메시지 toHtmlString() 삭제
	jbaek		2014/08/20		2.2.2	Document Management 기능 개발
	stghr12		2008/03/31		2.2.1	bodyLoad(): window.top.menuOutFunction -> window.parent.menuOutFunction
										imageOut(): 내용삭제
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
										"locale" 처리
										changeEffectUserReq(): sub_menu.jsp로 이동
										common.css 추가, style에서 font 종류 제외
	stghr12		2007/04/30		2.1.0	version up
	stghr12		2006/12/01		1.0.0	create
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
	String organizationCount = property.getProperty( "organizationCount" );
	String savedOrganizationCode = property.getProperty( "savedOrganizationCode" );
%>
<head>
	<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= systemConfig.getSystemName() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>
	<link rel='stylesheet' href='style/common.css'/>
	<link rel='stylesheet' href='script/assets/css/bootstrap.min.css' />
	<script type='text/javascript' src='script/main.js'></script>
	<style type='text/css'><!--
		div.top_menu {
			display: inline;
		}

		div.top_menu p.msg {
			color: #666666;
			margin: 0; margin-top: 2px;
			padding: 0;
			float: left; position: absolute;
			left: 160px;
		}

		div.top_menu p.msg span {
			color: #FF0000;
		}

		div.top_menu ul {
			text-align: right;
			margin: 0; margin-right: 25px;
			margin-top: 4px; margin-bottom: 2px;
		}

		div.top_menu li {
			list-style: 0;
			margin-top: 0px;
			display: inline;
		}

		div.top_menu li select[name=site-locale] {
			vertical-align: 5px;
		}

		body { margin: 0px; }

		td.menu_msg { padding-top: 3px; padding-left: 4px; vertical-align: top; }
		td.menu_msg img { vertical-align: -1px; }
		td.menu_toplink { padding-top: 3px; padding-right: 4px; vertical-align: top; height: 18px; }
		td.menu_toplink img { margin-left: 0px; margin-right: 5px; }
	//--></style>

	<script type='text/javascript'>
		function imageOut( imageObj ) {}

		function imageOver( imageObj ) {
			Menu.hide( imageObj.getAttribute("level") );
			Menu.showOne( imageObj );
		}

		function initMenuReq() {
			initPageReq(getSiteLocale());
		}

		function logoutReq() {
			if( confirm("<mtl:message key="MSG_CONFIRM_LOGOUT" encodeScript="true"/>") ) {
				var url = "<%= systemConfig.getClassURL() %>/Login?mode=logout";
				url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );
				window.open( url, "_top" );
			}
		}

		function changeLocale( language ) {
			if( language ) {
				changeLocaleToFrame( "", language );// the very default frame
				changeLocaleToFrame( "main", language );

				// reload with changeLang parameter to reload for the language
				var url = replaceQueryValue(getLocationURL(), "locale", language);
				url = replaceQueryValue(url, "chgLang", "true");
				url = replaceQueryValue(url, "chgLang", "true");
				var imageObj = Menu.selectedMenu[ Menu.selectedMenu.length -1 ];
				if( imageObj ) {
					url = replaceQueryValue(url, "menuFocus", imageObj.name);
				}
				location.href = url;
				// location.href = location.replace(url);
				// document.location = url;
			}
		}

		function changeLocaleStruct() {
			var ctl_box = document.querySelector(".top_menu ul");
			var opts = getTopMenuDefaultOptions();

			var sel = ctl_box.querySelector("select.site-locale-sel");
			if( sel == undefined ) {
				var sl = opts.supportLocale.split(",");
				var sll = opts.supportLocaleLabel.split(",");

				sel = document.createElement("select");
				sel.name = "site-locale";
				sel.classList = "site-locale-sel";
				for( var i = 0; i < sl.length; i++ ) {
					var selopt = document.createElement("option");
					selopt.value = sl[i];
					selopt.text = sll[i];
					sel.appendChild(selopt);
				}

				if( opts.siteLocale ) {
					var selopt = sel.querySelector("option[value='" + opts.siteLocale + "']");
					if( selopt != undefined ) {
						selopt.setAttribute("selected", true);
					}
				} else {
					alert('siteLocale null?');
				}
				sel.onchange = function( event ) {
					var idx = event.target.selectedIndex;
					var opt = event.target.options[idx];
					var siteLocale = opt.value;
					changeLocale(siteLocale);
				};
				if( ctl_box.querySelector("select.site-locale-sel") == undefined ) {
					var li = document.createElement("li");
					li.appendChild(sel);
					ctl_box.appendChild(li);
				}
			}
		}

		function changeLocaleToFrame( frameName, siteLocale ) {
			if( frameName === undefined || frameName.length <= 0 ) {
				var domHtml = window.parent.document.querySelector("html");
				domHtml.setAttribute("data-lang", siteLocale);
				if( false ) {
					var url = window.parent.location.href;
					url = replaceQueryValue( url, "locale", siteLocale );
					window.parent.location = url;
				}
			} else {
				var url = parent.frames[frameName].location.href;
				url = replaceQueryValue( url, "locale", siteLocale );
				parent.frames[frameName].location = url;
			}
		}

		function getLocationURL() {
			<% if( "GET".equals(request.getMethod()) ) { %>
				var url = replaceQueryValue( location.href, "all", null );
				url = replaceQueryValue( url, "focus", null );
				url = replaceQueryValue( url, "condkey", null );
				url = replaceQueryValue( url, "msgkey", null );
				url = replaceQueryValue( url, "savedkey", null );

				var args = getLocationURL.arguments;
				if( args != null ) {
					for( var i = 0; i < args.length; i++ )
						url = replaceQueryValue( url, args[i], null );
				}

				return url;
			<% } else { %>
				return null;
			<% } %>
		}

		function getSiteLocale() {
			var selopt = document.querySelector("div.top_menu select.site-locale-sel option[selected]");
			if( selopt != undefined ) {
				if( selopt.value ) {
					return selopt.value
				}
			}

			return getTopMenuDefaultOptions.siteLocale;
		}

		function getTopMenuDefaultOptions() {
			<%
				String defaultSupportLocale = "en";
				String defaultSupportLocaleLabel = "English";
				if( "JNJAP_CN".equals(sessionMng.getPartyId()) ) {
					defaultSupportLocale += ",zh";
					defaultSupportLocaleLabel += ",Simplified-Chinese";
				} else if( "JNJAP_TH".equals(sessionMng.getPartyId()) ) {
					defaultSupportLocale += ",th";
					defaultSupportLocaleLabel += ",Thai";
				}
				String supportLocale = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocale;"+ sessionMng.getPartyId(), defaultSupportLocale);
				String supportLocaleLabel = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocaleLabel;"+ sessionMng.getPartyId(), defaultSupportLocaleLabel);
			%>;
			return {
					supportLocale: "<%=supportLocale%>",
					supportLocaleLabel: "<%=supportLocaleLabel%>",
					siteLocale: "<%=htmlpage.getLocale()%>"// usually htmlpage.getLocale() is first locale user selected.
			};
		}

		function toggleTopMenu() {
			if( document.all.closedMenu.style.display == "" ) {
				document.all.closedMenu.style.display = "none";
				document.all.openedMenu.style.display = "";
				window.top.menuOpen();
				Menu.enabled = true;
				Menu.show();
			} else {
				document.all.closedMenu.style.display = "";
				document.all.openedMenu.style.display = "none";
				window.top.menuClose(26);
				Menu.enabled = false;
				Menu.hide();
			}
		}

		function Menu() {}
		Menu.enabled = true;
		Menu.displayMenu = new Array();
		Menu.selectedMenu = new Array();

		Menu.click = function( imageObj, subMenuName ) {
			if( Menu.selectedMenu[imageObj.getAttribute("level")] == imageObj ) return;
			if( subMenuName && document.images[subMenuName] ) {
				document.images[subMenuName].click();
				return;
			}

			for( var l = 1; Menu.selectedMenu[l]; l++ )
				Menu.displayMenu[l] = Menu.selectedMenu[l];

			Menu.selectedMenu[imageObj.getAttribute("level")] = imageObj;
			for( var l = eval(imageObj.getAttribute("level")) + 1; Menu.selectedMenu[l]; l++ )
				Menu.selectedMenu[l] = null;

			for( var l = eval(imageObj.getAttribute("level")) - 1; l > 0; l-- ) {
				imageObj = Menu.getParentMenu( imageObj );
				if( Menu.selectedMenu[l] == imageObj ) break;

				Menu.selectedMenu[l] = imageObj;
			}
			Menu.show();
		}

		Menu.getParentMenu = function( imageObj ) {
			var prefix = "menu"+ imageObj.getAttribute("level") +"_";
			var object = imageObj.parentNode;
			while( object ) {
				if( object.id && object.id.indexOf(prefix) == 0 )
					return document.images[ object.id.substring(prefix.length) ];
				object = object.parentNode;
			}
			return null;
		}

		Menu.hide = function( level ) {
			if( typeof level == "undefined" ) {
				var menuObj = document.getElementById( "menu1_top" );
				if( menuObj ) menuObj.style.display = "none";
				level = 1;
			}

			for( var l = eval(level); Menu.displayMenu[l]; l++ ) {
				var imageObj = Menu.displayMenu[l];
				if( Menu.displayMenu[l] != Menu.selectedMenu[l] )
					imageObj.src = "images/menu" + imageObj.getAttribute("level") + "_" + imageObj.name + "_off.gif";

				var menuObj = document.getElementById( "menu" + (eval(imageObj.getAttribute("level")) + 1) + "_" + imageObj.name );
				if( menuObj ) menuObj.style.display = "none";

				Menu.displayMenu[imageObj.getAttribute("level")] = null;
			}
		}

		Menu.init = function( menuName, isClick ) {
			var imageStack = new Array();

			var imageObj = document.images[menuName];
			while( imageObj ) {
				imageStack.push( imageObj );
				imageObj = Menu.getParentMenu( imageObj );
			}

			while( imageStack.length > 0 ) {
				Menu.click( imageObj = imageStack.pop() );
			}
			if( isClick && imageObj ) imageObj.click();
		}

		Menu.initPosition = function( menuTable, left, margin ) {
			var imageObj = menuTable.firstChild;
			while( imageObj ) {
				if( imageObj.getAttribute("level") ) {
					var menuObj = document.getElementById( "menu" + (eval(imageObj.getAttribute("level")) + 1) + "_" + imageObj.name );
					if( menuObj ) {
						menuObj.style.display = "";
						if( left + menuObj.firstChild.clientWidth > document.body.clientWidth )
							menuObj.style.left = document.body.clientWidth - menuObj.firstChild.clientWidth;
						else
							menuObj.style.left = left - margin;
						Menu.initPosition( menuObj.firstChild, left, margin );
					}
					left += imageObj.width;
				}

				if( imageObj.firstChild )
					imageObj = imageObj.firstChild;
				else {
					while( imageObj != menuTable && !imageObj.nextSibling )
						imageObj = imageObj.parentNode;
					imageObj = ( imageObj == menuTable ? null : imageObj.nextSibling );
				}
			}
		}

		Menu.show = function( level ) {
			if( !Menu.enabled ) return;
			if( typeof level == "undefined" ) {
				var menuObj = document.getElementById( "menu1_top" );
				if( menuObj ) menuObj.style.display = "";
				level = 1;
			}

			for( var l = eval(level); Menu.selectedMenu[l] || Menu.displayMenu[l]; l++ ) {
				if( Menu.displayMenu[l] && Menu.displayMenu[l] != Menu.selectedMenu[l] ) {
					var imageObj = Menu.displayMenu[l];
					imageObj.src = "images/menu" + imageObj.getAttribute("level") + "_" + imageObj.name + "_off.gif";

					var menuObj = document.getElementById( "menu" + (eval(imageObj.getAttribute("level")) + 1) + "_" + imageObj.name );
					if( menuObj ) menuObj.style.display = "none";
					Menu.displayMenu[imageObj.getAttribute("level")] = null;
				}
				if( Menu.selectedMenu[l] ) Menu.showOne( Menu.selectedMenu[l] );
			}
		}

		Menu.showOne = function( imageObj ) {
			imageObj.src = "images/menu" + imageObj.getAttribute("level") + "_" + imageObj.name + "_on.gif";

			var menuObj = document.getElementById( "menu" + (eval(imageObj.getAttribute("level")) + 1) + "_" + imageObj.name );
			if( menuObj ) menuObj.style.display = "block";

			Menu.displayMenu[imageObj.getAttribute("level")] = imageObj;
		}
	</script>

	<script type='text/javascript'>
		function refreshBoardByOrganization() {
			if( window.document.getElementsByName("savedOrganizationCode")[0] ) {
				var main_url = parent.frames["main"].location.href;

				var prevOrgCode = getQueryValue( main_url, "organizationCode" );
				var prevBoardCode = getQueryValue( main_url, "organizationCode" );
				var organizationCode = window.document.getElementsByName("savedOrganizationCode")[0].value;
				var boardClassCode = "NO." + organizationCode;
				if( prevOrgCode !== organizationCode || prevBoardCode !== boardClassCode ) {
					main_url = replaceQueryValue( main_url, "organizationCode", organizationCode );
					main_url = replaceQueryValue( main_url, "boardClassCode", boardClassCode );
					parent.frames["main"].location = main_url;
				}
			}
		}

		function bodyLoad() {
			var menuKey = "<%= request.getParameter("menu") %>";
			if( menuKey == "init" )
				initPageReq();
			else if( menuKey )
				Menu.init( menuKey, <%= ( request.getParameter("click") != null ) %> );

			if( window.parent ) window.parent.menuOutFunction = Menu.show;
			self.focus();

			changeLocaleStruct();
			var chgLang = "<%= request.getParameter("chgLang") %>";
			if( chgLang === "true" ) {
				var menuFocus = "<%= request.getParameter("menuFocus") %>";
				if( menuFocus ) {
					var imageObj1 = Menu.getParentMenu( document.querySelector("[name="+encodeURIComponent(menuFocus)+"]") );
					if( imageObj1 ) Menu.selectedMenu[1] = imageObj1;
					var imageObj2 = document.querySelector("[name="+encodeURIComponent(menuFocus)+"]");
					if( imageObj2 ) Menu.selectedMenu[2] = imageObj2;
				}

				return false;
			}

			var boardClassCode = getQueryValue( parent.frames["main"].location.href, "boardClassCode" );
			if( typeof boardClassCode != "undefined" ) {
				refreshBoardByOrganization();
			}
		}
	</script>
</head>

<%
	String mesgOrganization = property.getProperty( "mesgOrganization" );
	String menuMessage = msghandler.getMessage( "jsp.menu_top.LOGINMSG", new String[] {
		HtmlUtility.toHtmlString(sessionMng.getPartyName()), HtmlUtility.toHtmlString(sessionMng.getUserName()), mesgOrganization
	} );
%>
<body onLoad='JavaScript:bodyLoad();'>
	<div class='top_menu'>
	<p class='msg'><%= menuMessage %></p>
	<ul>
		<li><a href='JavaScript:initMenuReq();'><img src='images/menu_dpr_home_off.gif' onMouseOver='simpleImageOver(this)'
					onMouseOut='simpleImageOut(this)'></a>
		<li><a href='JavaScript:logoutReq();'><img src='images/menu_dpr_logout_off.gif' onMouseOver='simpleImageOver(this)'
					onMouseOut='simpleImageOut(this)'></a>
	</ul>
	</div>

	<%@ include file="sub_menu.jsp" %>
</body>
</mtl:html>
