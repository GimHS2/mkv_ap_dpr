<%--
	File Name:	error_passwd.jsp
	Version:	2.2.1c(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2022/09/30		2.2.1c	partyId, userId 를 sessionMng -> recordMap 으로 불러오도록 변경
	hankalam	2021/11/30		2.2.1c	신규 UI/UX 적용
	jbaek		2014/03/31		2.2.1	CrossBrowsing 적용
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getBaseURL() -> systemConfig.getBaseURL(htmlpage.getLocale())
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리, 'menu_tesco.css' 삭제
										wintype이 sub일 때 처리
	stghr12		2006/07/12		2.0.1	'common.js' 삭제
	stghr12		2006/02/28		2.0.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr'%>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= systemConfig.getSystemName() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>
	<link rel='stylesheet' href='style/dpr_common.css'/>
	<link rel='stylesheet' href='style/menu_portal.css'/>
	<link rel='stylesheet' href='style/jquery-ui.css'/>
	<script type='text/javascript' src='script/jquery.min.js'></script>
	<script type='text/javascript' src='script/jquery-ui.js'></script>
	<%@ include file="include_pub_passwd.inc" %>
	<script type='text/javascript'>
		$(function() {
			var customPopup = {
				dialog : function( messages, callback ) {
					var yesButton, noButton;
					if( parent.$("body.frame-content").length ) {
						yesButton = parent.$("div.popup.dialog button[name=confirm]");
						noButton = parent.$("div.popup.dialog button[name=cancel]");
					} else {
						yesButton = $("div.popup.dialog button[name=confirm]");
						noButton = $("div.popup.dialog button[name=cancel]");
					}

					yesButton.on( "click", function() {
						$(this).unbind( "click" );
						noButton.unbind( "click" );
						customPopup.close( "dialog" );
						callback( true );
					});

					noButton.on( "click", function() {
						$(this).unbind( "click" );
						yesButton.unbind( "click" );
						customPopup.close( "dialog" );
						callback( false );
					});

					this.open( "dialog", messages );
				},

				confirm : function( messages, callback ) {
					var confirmButton, cancelButton;
					if( parent.$("body.frame-content").length ) {
						confirmButton = parent.$("div.popup.confirm button[name=confirm]");
						cancelButton = parent.$("div.popup.confirm button[name=cancel]");
					} else {
						confirmButton = $("div.popup.confirm button[name=confirm]");
						cancelButton = $("div.popup.confirm button[name=cancel]");
					}

					confirmButton.on( "click", function() {
						$(this).unbind( "click" );
						cancelButton.unbind( "click" );
						customPopup.close( "confirm" );
						callback( true );
					});

					cancelButton.on( "click", function() {
						$(this).unbind( "click" );
						confirmButton.unbind( "click" );
						customPopup.close( "confirm" );
						callback( false );
					});

					this.open( "confirm", messages );
				},

				alert : function( messages ){
					if( parent.$("body.frame-content").length ) {
						button = parent.$("div.popup.alert button[name=confirm]");
					} else {
						button = $("div.popup.alert button[name=confirm]");
					}

					button.on( "click", function() {
						$(this).unbind( "click" );
						customPopup.close( "alert" );
					});

					this.open( "alert", messages );
				},

				open : function( type, messages ) {
					if( parent.$("body.frame-content").length ) {
						togglePopupParent( true, type, messages );
					} else {
						togglePopup( true, type, messages );
					}
				},

				close : function( type ) {
					if( parent.$("body.frame-content").length ) {
						togglePopupParent( false, type );
					} else {
						togglePopup( false, type );
					}
				}
			};

			$(".content-overlay").css( "display", "flex" );
			$("img[name=password_tooltip]").tooltip( {
				position: { my: "left-15 bottom-15", at: "right center" },
				content: function() {
					return $("#content_id_password_tip").html();
				}
			}).css( "cursor", "pointer" );

		<% char pageResult = htmlpage.getResultLevel();
			String messageClass;

			switch( pageResult ) {
				case HtmlPage.PAGE_RESULT_WARNING:
					messageClass = "warning";
					break;
				case HtmlPage.PAGE_RESULT_ERROR:
					messageClass = "error";
					break;
				case HtmlPage.PAGE_RESULT_NULL:
				case HtmlPage.PAGE_RESULT_SUCCESS:
				default:
					messageClass = "success";
			}
		%>

			var messageInit = function() {
				var msgObj = $("#messagebar");
				if( msgObj.length ) {
					msgObj.addClass( "messagebar" );
					var msgbox = $("<div class='msgbox table-cell'>");
					msgbox.append( "<a class='circle-close'>" );
					msgObj.append( msgbox );
				}
				<%
					String msg = HtmlUtility.toScriptString( htmlpage.getMessage() );
					if( msg != null ) {
						msg = msg.replaceAll( "\t|\n", "" ).trim();
					}
				%>
				var message = "<%= msg %>";
				var messageClass = "<%= messageClass %>";
				if( message && message != "" && msgObj.length ) {
					msgObj.css( "display", "table" );
					msgObj.addClass( messageClass );
					msgObj.children().append( message );
					msgObj.find( ".circle-close" ).addClass( messageClass );
				}
			}
			$("#messagebar").on( "click", "div.msgbox .circle-close", function() {
				$(this).closest( ".messagebar" ).fadeOut();
			});
			messageInit();
		});

		function checkInput() {
			if( frmMain.presentPassword.value == "" ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.error_passwd.MSG_ENTER_PRESENTPASSWORD" encodeScript="true"/>" } );
				frmMain.presentPassword.focus();
				return false;
			}
			if( frmMain.password.value == "" ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.error_passwd.MSG_ENTER_PASSWORD" encodeScript="true"/>" } );
				frmMain.password.focus();
				return false;
			}
			if( frmMain.password2.value == "" ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.error_passwd.MSG_ENTER_PASSWORD" encodeScript="true"/>" } );
				frmMain.password2.focus();
				return false;
			}
			if( frmMain.password.value != frmMain.password2.value ) {
				customPopup.alert( { "header" : "<mtl:message key="jsp.error_passwd.MSG_NOT_EQUAL_PASSWORD" encodeScript="true"/>" } );
				frmMain.password.value = frmMain.password2.value = "";
				frmMain.password.focus();
				return false;
			}
			if( !checkPassword(frmMain.password.value, frmMain.presentPassword.value, frmMain.partyId.value, frmMain.userId.value) ) {
				frmMain.password.value = frmMain.password2.value = "";
				frmMain.password.focus();
				return false;
			}

			return true;
		}

	</script>
</head>

<body class='content'>
	<div class='content-overlay'>
		<%-- <form name='frmMain' method='post' action='<%=systemConfig.getClassURL()%>/Login' onSubmit='return checkInput();'> --%>
		<mtl:form name="frmMain" onSubmit="JavaScript: return checkInput();">
			<input type='hidden' name='mode' value='passwd'>
			<mtl:hidden id="record" key="partyId"/>
			<mtl:hidden id="record" key="userId"/>
			<input type='hidden' name='locale' value='<%= htmlpage.getLocale() %>'>
			<% if( htmlpage.getBackURL() != null ) { %>
				<input type='hidden' name='url' value='<%= htmlpage.getBackURL() %>'>
			<% } %>
			<mtl:contentGroup groupId="password" type="content" styleClass="card" style="width: 600px;">
				<div id='messagebar'></div>
				<h2 style='margin-bottom: 25px;'><mtl:message key="jsp.error_passwd.TITLE_EXPIRE_PASSWORD"/></h2>
				<div style='margin-bottom: 20px;'><mtl:message key="ERR_PASSWORD_EXPIRED"/></div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'>
								<mtl:title key="password" name="presentPassword" mandatory="true" descriptionKey="FIELD_USR_PRESENT_PASSWORD"/>
							</div>
							<div class='field'>
								<input type='password' name='presentPassword' class='input-field' maxlength='20' style='width: calc(100% - 50px);'>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="password" mandatory="true" descriptionKey="FIELD_USR_PASSWORD"/></div>
							<div class='field'>
								<input type='password' name='password' class='input-field' maxlength='20' style='width: calc(100% - 50px);'>
								<img name='password_tooltip' src='images/ico_info.png' style='vertical-align: middle; margin-left: 5px;' title=''>
								<div id='content_id_password_tip' style='display: none;'>
									<ul>
										<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_1"/></li>
										<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_2"/></li>
										<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_3"/></li>
										<li><mtl:message key="MSG_PASSWORD_COMPLEXITY_4"/></li>
										<li><mtl:message key="MSG_PASSWORD_EXPLICATE"/></li>
									</ul>
								</div>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="password" name="password2" mandatory="true" descriptionKey="FIELD_USR_PASSWORD2"/></div>
							<div class='field'>
								<input type='password' name='password2' class='input-field' maxlength='20' style='width: calc(100% - 50px);'>
							</div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="save"/>
					</div>
				</div>
			</mtl:contentGroup>
		<!-- </form> -->
		</mtl:form>
	</div>
</body>
</mtl:html>
