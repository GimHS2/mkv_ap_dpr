<%--
	File Name:	usr_user_find_account_input.jsp
	Version:	2.2.2c(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/07/31		2.2.2c	디포탈용 스타일 추가, menu_portal.css 추가
	hankalam	2019/07/31		2.2.2	새창으로 뜰때 내용만큰 높이가 자동으로 조절되게 하기 위해 <div id='contentWrap'> 태그 추가
	GimHS		2017/02/28		2.2.1	PartyId가 hidden으로 표시 될때 table 태그가 안들어가도록 수정
	hankalam	2016/07/31		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding="UTF-8" %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">

<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<script type='text/javascript'>
		$.fn.__tabs = $.fn.tabs;
		$.fn.tabs = function (a, b, c, d, e, f) {
			var base = location.href.replace(/#.*$/, '');
			$('ul>li>a[href^="#"]', this).each(function () {
				var href = $(this).attr('href');
				$(this).attr('href', base + href);
			});
			$(this).__tabs(a, b, c, d, e, f);
		};

		$(function() {
			$("#tabs-main").tabs( {
				classes: {
					"ui-tabs": "tabs-main",
					"ui-tabs-nav": "tabs-nav",
					"ui-tabs-tab": "tabs-tab",
				}
			});
		});
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<mtl:contentGroup groupId="main" type="tabpane" styleClass="tabpane none-card">
				<mtl:hidden id="record" key="mode"/>
				<div id='tabs-main'>
					<ul>
						<li><a href='#tabs-find-id' class='tabs-title'><mtl:message key="jsp.usr_user_find_account_input.GRP_ID"/></a></li>
						<li><a href='#tabs-find-pw' class='tabs-title'><mtl:message key="jsp.usr_user_find_account_input.GRP_PW"/></a></li>
					</ul>

					<div id='tabs-find-id'>
						<p><%= msghandler.getMessage( "jsp.usr_user_find_account_input.MSG_INFO_ID" ) %></p>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:title key='idPartyId' mandatory="true" descriptionKey="jsp.usr_user_find_account_input.MSG_COUNTRY"/>
									</div>
									<div class='field-info'>
										<mtl:select id="record" key="idPartyId" listId="parties" listCodeKey="partyId" listNameFormat="$H{MSG_DPR_COUNTRY_@partyId}" searchable="false"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key='name' mandatory="true" descriptionKey="FIELD_USR_USERNAME"/></div>
									<div class='field-info'><mtl:text id="record" key="name" mandatory="true"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key='idEmail' mandatory="true" descriptionKey="FIELD_USR_EMAIL"/></div>
									<div class='field-info'><mtl:text id="record" key="idEmail" mandatory="true"/></div>
								</div>
							</div>
						</div>
					</div>

					<div id='tabs-find-pw'>
						<p><%= msghandler.getMessage( "jsp.usr_user_find_account_input.MSG_INFO_PW" ) %></p>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:title key='pwPartyId' mandatory="true" descriptionKey="jsp.usr_user_find_account_input.MSG_COUNTRY"/>
									</div>
									<div class='field-info'>
										<mtl:select id="record" key="pwPartyId" listId="parties" listCodeKey="partyId" listNameFormat="$H{MSG_DPR_COUNTRY_@partyId}" searchable="false"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key='userId' mandatory="true" descriptionKey="FIELD_USERID"/></div>
									<div class='field-info'><mtl:text id="record" key="userId" mandatory="true"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key='pwEmail' mandatory="true" descriptionKey="FIELD_USR_EMAIL"/></div>
									<div class='field-info'><mtl:text id="record" key="pwEmail" mandatory="true"/></div>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div class='list-function align-right'>
					<div class='button'>
						<mtl:button type="close_if"/>
						<mtl:button type="reset"/>
						<mtl:button type="submit"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%-- 아이디, 비밀번호 찾기 탭 구분하여 처리 --%>
					if( $("#tabs-find-id").is(":visible") ){
						if( !Field.checkMandatory(frmMain.idPartyId) ) return false;
						if( !Field.checkMandatory(frmMain.name) ) return false;
						if( !Field.checkMandatory(frmMain.idEmail) ) return false;
						if( !emailCheck(frmMain.idEmail) ) return false;
						frmMain.mode.value = "fndid";
					}else{
						if( !Field.checkMandatory(frmMain.pwPartyId) ) return false;
						if( !Field.checkMandatory(frmMain.userId) ) return false;
						if( !Field.checkMandatory(frmMain.pwEmail) ) return false;
						if( !emailCheck(frmMain.pwEmail) ) return false;
						frmMain.mode.value = "rsetpwd";
					}
					frmMain.url.value = getLocationURL();
					return submitInput();
				}

				function emailCheck( elementObj ) {
					var regex = /\w+([-+.]\w+)*@\w+([-.]\w+)*\.[a-zA-Z]{2,5}$/;
					if( !regex.test(Field.getValue(elementObj)) ){
						return Field.alertError( elementObj, "<mtl:message key="jsp.usr_user_find_account_input.MSG_EMAIL_ERROR" encodeScript="true"/>" )
					}
					return true;
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
