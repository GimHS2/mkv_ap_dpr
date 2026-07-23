<%--

	File Name:	usr_user_info.jsp
	Version:	2.2.0(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='utf-8' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
	com.irt.rbm.usr.UserUserFieldOption fieldOption = (com.irt.rbm.usr.UserUserFieldOption)pageContext.findAttribute( "fieldOption" );

	boolean isCountryAdmin = "AD".equals(sessionMng.getGroupClass()) || sessionMng.isSystemAdmin();
	String focus = (String)request.getAttribute("focus");
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_dpr_passwd.inc" %>
	<script type='text/javascript'>
		var windowWidth = 1400;

		$(function() {
			$("#inputPassword, #inputUserName, #inputDetail, #inner_overlay").hide();
			$(".inner-content").css( "background-color", "#FFFFFF" ).css( "background-color", "" );
			$("#content_userInfo, #content_userDetailInfo").find( ".field-info" ).each( function(index, item) {
				if( $(item).text().trim() === "" ) {
					$(item).text( "–" );
				}
			});

			if( "<%= HtmlUtility.toScriptString(htmlpage.getMessage()) %>" != "" ) {
				var messages = { "header" : "<%= HtmlUtility.toScriptString(htmlpage.getMessage()) %>" };
				customPopup.alert( messages );
			}

			var userInfo = $("#content_userInfo").innerHeight();
			var userDetailInfo = $("#content_userDetailInfo").innerHeight();
			if( userInfo > userDetailInfo ) {
				$("#content_userDetailInfo").innerHeight( userInfo );
			} else {
				$("#content_userInfo").innerHeight( userDetailInfo );
			}

			$("#inner_overlay button[name=cancel]").on( "click", function() {
				$(this).closest( "form" )[0].reset()
				$("#inner_overlay").fadeOut( 150 );
			});

			$("#sub-content").remove();

			$("img[name=password_tooltip]").tooltip( {
				position: { my: "left-15 bottom-15", at: "right center" },
				content: function() {
					return $("#content_id_password_tip").html();
				}
			}).css( "cursor", "pointer" );

			$("#inner_overlay input[name=url]").val( getLocationURL() );

			$(window).resize( function () {
				if( $("#inputDetail").is(":visible") ) {
					contentResize( true );
				}
			});
		});

		function contentResize( windowResize ) {
			var contentMaxHeight = $("#inner_overlay").height() - 120;
			var minHeight = 400;
			if( contentMaxHeight <= minHeight) {
				return;
			}

			$("#content_detail").height( minHeight );
			var frameDocHeight = $( "#content_detail" )[0].scrollHeight + 65;

			if( contentMaxHeight > frameDocHeight ) {
				$("#content_detail").height( frameDocHeight );
			} else {
				$("#content_detail").height( contentMaxHeight );
			}
		}

		function focusCheck() {
			var focus = "<%= focus %>";

			if( focus == "employeeId" )
				focusForm( frmMain, frmMain.employeeId );
			else
				focusForm( frmMain );
		}

		function changePartyId( partyId ) {
			var url = "<%= systemConfig.getClassURL() %>/USRUser?mode=org&partyId=" + partyId;

			windowOpen( url, "clsName" );
		}

		function toggleInput( id ) {
			$("#inner_overlay").fadeIn( 150 ).css( "display", "flex" );
			$("#" + id).show();
			$("#" + id).siblings().hide();
			if( id == "inputDetail" ) {
				$(".inner-content-wrap").css( "width", 900 ).css( "min-width", 500 );
				contentResize();
			} else {
				$(".inner-content-wrap").css( "width", 550 ).css( "min-width", 300 );
			}
		}

		function registReq() {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?mode=ireg" );
		}

		function uniqueCheckReq( elementObj ) {
			if( !Field.checkMandatory(elementObj) ) return;

			var url = "<%= htmlpage.getRequestURL() %>?mode=chk&wintype=sub&slname="+ elementObj.name;
			url = replaceQueryValue( url, "partyId", encodeURIComponent(frmMain.partyId.value) );
			windowOpen( replaceQueryValue(url, elementObj.name, elementObj.value), "clsMng" );
		}

		function setPartyAuth() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyAuth?mode=list";
			url = replaceQueryValue( url, "uniqId", "<mtl:value id="record" key="uniqId"/>" );
			url = replaceQueryValue( url, "partyId", "<mtl:value id="record" key="partyId"/>" );
			url = replaceQueryValue( url, "userId", "<mtl:value id="record" key="userId"/>" );

			windowSelfOpen( url, getLocationURL());
		}

		function detailCheckInput() {
			$("#inner_overlay").fadeOut( 150 );
			return true;
		}

		function passwordCheckInput() {
			if( !Field.checkMultiMandatory([frmPassword.presentPassword, frmPassword.password, frmPassword.password2], true) ) return false;
			if( frmPassword.password.value != frmPassword.password2.value ) {
				toggleFieldErrorMessage( frmPassword.password, true, "<mtl:message key="jsp.error_passwd.MSG_NOT_EQUAL_PASSWORD" encodeScript="true"/>" );
				return false;
			}
			if( !checkPassword(frmPassword.password, frmPassword.presentPassword, frmPassword.partyId.value, frmPassword.userId.value) ) {
				frmPassword.password.value = frmPassword.password2.value = "";
				frmPassword.password.focus();
				return false;
			}
			$("#inner_overlay").fadeOut( 150 );
			return true;
		}

		function userNameCheckInput() {
			if( !Field.checkMandatory(frmUserName.userName) ) return false;
			$("#inner_overlay").fadeOut( 150 );
			return submitInput();
		}

		function checkPassword( passwordObj, password_oldObj ) {
			var password = passwordObj.value;
			var password_old = password_oldObj.value;

			if( password.length < 6 ) {
				toggleFieldErrorMessage( passwordObj, true, "<mtl:message key="jsp.include_pub_passwd.MSG_TOO_SHORT" encodeScript="true"/>" );
				return false;
			} else if( password_old == password ) {
				toggleFieldErrorMessage( passwordObj, true, "<mtl:message key="jsp.include_pub_passwd.MSG_NEED_CHANGE" encodeScript="true"/>" );
				return false;
			}
			for( var i = 2; i < checkPassword.arguments.length; i++ ) {
				if( password.indexOf(checkPassword.arguments[i]) >= 0 || checkPassword.arguments[i].indexOf(password) >= 0 ) {
					toggleFieldErrorMessage( passwordObj, true, "<mtl:message key="jsp.include_pub_passwd.MSG_TOO_EASY" encodeScript="true"/>" );
					return false;
				}
			}

			var cnt = 0;
			var bool_a = false;
			var bool_n = false;
			for( var i = 0; i < password.length; i++ ) {
				if( password.charAt(i) >= '0' && password.charAt(i) <= '9' )
					bool_n = true;
				else
					bool_a = true;
				if( i == 0 || password.charAt(i) != password.charAt(i-1) )
					cnt = 0;
				else if( ++cnt > 2 ) {
					toggleFieldErrorMessage( passwordObj, true, "<mtl:message key="jsp.include_pub_passwd.MSG_TOO_MANY_CHAR_REPEAT" encodeScript="true"/>" );
					return false;
				}
			}
			if( !bool_n || !bool_a ) {
				toggleFieldErrorMessage( passwordObj, true, "<mtl:message key="jsp.include_pub_passwd.MSG_NEED_CHAR_NUMBER" encodeScript="true"/>" );
				return false;
			}

			return true;
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_usr_user_info2.inc" %>
	</div>
</body>
</mtl:html>
