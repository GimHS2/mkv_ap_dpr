<%--
	File Name:	login.jsp(DPR)
	Version:	2.2.8

	Description:
		DPR login.jsp

	Note:

	Motified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.8	신규 UI/UX 적용
	hankalam	2020/09/29		2.2.7	아이디/비밀번호 찾기 기능 추가
	jbaek		2019/07/30		2.2.6	usingSaveId 관련 스타일 변경.
	jbaek		2018/10/30		2.2.5	언어 선택 환경 변수적용. frame없는 로그인 페이지에서도 언어 선택하여 로그인 가능하도록 locale을 request에서 가져옴.
	song7981	2016/03/31		2.2.4	로고 추가, 자동완성 기능 제한옵션 추가
	jbaek		2015/06/30		2.2.3	CrossBrowsing: 로긴창 중으로 맞춤.
	jbaek		2015/04/30		2.2.2	maintenanceNotice: usingNoticeMarquee 인자 추가
	jbaek		2014/12/31		2.2.1	maintenanceNotice 적용, usingNoticeMarquee 인자 추가
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
	boolean isFindAccount = com.irt.rbm.RBMSystem.getSystemEnvBool( "USR", "SecurityPolicy;PasswordResetable", false );
	boolean isLoginUser = ( sessionMng != null && sessionMng.isLoginUser() );
	String defaultPartyId = systemConfig.getProperty( "defaultPartyId" );
	if( "".equals(defaultPartyId) ) defaultPartyId = null;

	String[] supportLocales = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;SupportLocale", "en,zh,th,vi,ko").split("\\,");

	java.util.Locale locale = null;
	String locale_request = (String)request.getParameter("locale");
	String locale_prop  = property.getProperty( "locale" );
	if( locale_request != null )
		locale = new java.util.Locale( locale_request );
	else if( request.getHeader("Accept-Language") != null )
		locale = new java.util.Locale( com.irt.servlet.ServletUtility2.parseLanguage(request.getHeader("Accept-Language"), supportLocales) );
	else if( locale_prop != null )
		locale = new java.util.Locale( locale_prop );
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
	<%@ include file="include_rbm_header.inc" %>
	<base href='<%= systemConfig.getBaseURL(locale) %>'/>
	<link rel='stylesheet' href='style/dpr_login.css'>

	<script type='text/javascript'>
		$(function() {
			if( typeof parent.jQuery == "undefined" ) {
				var script = parent.document.createElement( "script" );
				script.type = "text/javascript";
				script.src = "<%= systemConfig.getBaseURL( htmlpage.getLocale() ) %>script/jquery.min.js";
				parent.document.getElementsByTagName( "head" )[0].appendChild( script );
			}

			$(".content-overlay").css( {"box-shadow": "none", "-moz-box-shadow": "none", "-webkit-box-shadow": "none"} );

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

			if( "<%= HtmlUtility.toScriptString(htmlpage.getMessage()) %>" != "" ) {
				$(".popup-overlay > .popup.alert").show();
				$(".popup-overlay").css( "display", "flex" );
				$("#return").focus();
			}

			$("#return").click( function() {
				$(".popup-overlay").css( "display", "none" );
				self.focus();
				if( frmMain.userId.value == "" )
					frmMain.userId.focus();
				else {
					frmMain.password.select();
					frmMain.password.focus();
				}
			} );

			$.widget( "custom.localeSelectmenu", $.ui.selectmenu, {
				_renderButtonItem: function( item ) {
					var buttonItem = $( "<span>", {
						"class": "ui-selectmenu-text"
					})
					this._setText( buttonItem, item.label );
					buttonItem.css( "background-color", item.value )
					buttonItem.prepend( "<img src='images/ico_locale.png' style='top:'>");
					return buttonItem;
				},
				_renderMenu: function( ul, items ) {
					var that = this;
					$.each( items, function(index, item) {
						that._renderItemData( ul, item );
					});
					var menuWrap = $("<div/>").addClass( "selectmenu-menu-wrapper" );
					$(ul).wrap( menuWrap );
				},
				open: function( event ) {
					this._super( event );
					this.button.find( ".arrow-down").removeClass( "arrow-down" ).addClass( "arrow-up" );
				},
				close: function( event ) {
					this._super( event );
					this.button.find( ".arrow-up").removeClass( "arrow-up" ).addClass( "arrow-down" );
				}
			});

			$("select[name=locale]").localeSelectmenu({
				icons: { button: "arrow-down" },
				width: 150,
				select: function( event, ui ) {
					changeLocale( ui.item.value );
				}
			});
		});

		function changeLocale( language ) {
			if( language && language != "<%= locale.getLanguage() %>" ) {
				var url = "<%= systemConfig.getClassURL() %>/Login?locale="+ language;
				toggleLoading( true );
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

			toggleLoading( true );
			return true;
		}

	<% if( isFindAccount && !"ko".equals(locale.getLanguage()) ) { %>
		function findAccountReq()	{
			var url = "<%=systemConfig.getClassURL()%>/USRUser?mode=fndacc";
			url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );
			url = replaceQueryValue( url, "debugSQL", "<mtl:value id="request" key="debugSQL"/>" );

			windowOpen( url, "sub-content" );
		}
	<% } %>

		function logoutReq() {
			if( confirm("<mtl:message key="MSG_CONFIRM_LOGOUT" encodeScript="true"/>") ) {
				var url = "<%= systemConfig.getClassURL() %>/Login?mode=logout";
				url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );

				window.open( url, "_top" );
			}
		}
	</script>
</head>

<body>
	<div class='login-wrapper'><div class='login-cell'>
	<form name='frmMain' method='post' action='<%=systemConfig.getClassURL()%>/Login' onSubmit='return checkInput();' autocomplete='off'>
		<input type='hidden' name='mode' value='login'>
		<input type='hidden' name='menu' value='portal'>
		<input type='hidden' name='type' value='dpr'>
		<input type='hidden' name='killuser' value='true'>

		<div class='login-box'>
			<div class='login-header'>
				<div class='login-locale'>
					<select name='locale' class='locale' onChange='JavaScript:changeLanguage(this.value);' tabindex='5'>
					<% for( String localeString : supportLocales ) {
						if( localeString == null ) continue;
						String languageMessageKey = "MSG_LANGUAGE_BYLANGCD_" + localeString.toUpperCase();
					%>
						<option value='<%=localeString%>' <%=(localeString.equals(locale.getLanguage()) ? "selected" : "")%>>
							<mtl:message key='<%=languageMessageKey%>' />
						</option>
					<% } %>
					</select>
				</div>
			</div>
			<!-- 로긴헤드 종료-->

			<!-- 타이틀이미지 -->
			<div class='login-client-logo'><img src='images/login_jnj/login_client_logo.png'></div>
			<div class='login-form'>
				<div class='login_field'>
					<span class='login_title'><mtl:message key="FIELD_USERID"/></span>
					<input type='text' name='userId' class='field' maxlength='20' />
				</div>
				<div class='login_field'>
					<span class='login_title'><mtl:message key="FIELD_PASSWORD"/></span>
					<input type='password' name='password' class='field' maxlength='20' />
				</div>
				<div class='table'>
				<% if( isFindAccount && !"ko".equals(locale.getLanguage()) ) { %>
					<div class='table-cell'>
						<a href='JavaScript:findAccountReq()' class='login_forget'><mtl:message key="jsp.login.BTN_FIND_ACCOUNT"/></a>
					</div>
				<% } %>
					<div class='table-cell align-right'>
					<%
						String localeStyle = "margin-left:64px;";
						if( usingSaveId ) {
							localeStyle = "margin-left:5px;";
					%>
						<input type='checkbox' name='saveId' id='saveId'>
						<label for='saveId'><span><mtl:message key="jsp.login.MSG_REMEMBER_ME"/></span></label>
					<% } %>
						<button type='submit' class='btn-login'><mtl:message key="jsp.login.BTN_LOGIN"/></button>
					</div>
				</div>
			</div><!-- form 종료 -->

			<%@ include file="include_dpr_login_tail.inc" %>

			<div class='warn'>
				<h2><mtl:message key="jsp.login.MSG_TITLE_WARNING_NOTICE"/></h2>
				<p><mtl:message key="jsp.login.MSG_WARNING_NOTICE1"/></p>
				<p class='last'><mtl:message key="jsp.login.MSG_WARNING_NOTICE2"/></p>
			</div>
		</div>
	</form>
	</div></div>
 	<div class='popup-overlay'>
		<div class='popup alert' style='max-width: 540px; line-height: 250%;'>
			<p class='header-msg'><%= htmlpage.getMessage() %></p>
			<span style='float: right'><button type="button" id='return' class='btn btn-primary'><mtl:message key="jsp.login.BTN_RETURN_LOGIN"/></button></span>
		</div>
	</div>

	<div class='content-overlay'>
		<div class='loading'>
			<img src="images/loading.gif">
			<p class='msg'></p>
		</div>
		<div class='sub-content-wrap'></div>
	</div>
</body>
</html>
</mtl:html>
