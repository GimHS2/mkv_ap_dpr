<%--

	File Name:	usr_user_info.jsp
	Version:	2.2.0(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='utf-8' %>
<%@ page import="com.irt.html.*, java.util.Map, java.util.List" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Map<String, Object> recordMap = (Map<String, Object>)pageContext.findAttribute( "record" );
	List<Map<String, Object>> organizations = (List<Map<String, Object>>)request.getAttribute( "organizations" );
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

		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/USRUser?mode=imod";
			url = replaceQueryValue( url, "uniqId", "<mtl:value id="record" key="uniqId"/>")

			windowSelfOpen( url, getLocationURL() );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" fieldSetId="fieldSet">
		<div class='table table-fixed' style='min-width: 920px;'>
			<div class='row'>
				<div class='cell' style='padding-right: 10px;'>
					<mtl:contentGroup groupId="userInfo" type="content" styleClass="card">
						<h2 style='margin-bottom: 25px;'><mtl:message key="jsp.usr_user_info.SUBTITLE_USER_INFO"/></h2>
						<div>
							<img src='images/user_profile.png' style='margin-right: 25px; vertical-align: top'/>
							<div style='display: inline-block; width:calc(100% - 200px);'>
								<div class='info-table table-fixed'>
									<div class='row'>
										<div class='line-cell w140'>
											<div class='field-title'><mtl:title key="partyId" mandatory="false"/></div>
										</div>
										<div class='line-cell'>
											<div class='field-info'><mtl:value id="record" key="partyName"/></div>
										</div>
									</div>
									<div class='row'>
										<div class='line-cell'>
											<div class='field-title'><mtl:title key="userId" mandatory="false"/></div>
										</div>
										<div class='line-cell'>
											<div class='field-info'><mtl:value id="record" key="userId"/></div>
										</div>
									</div>
									<div class='row'>
										<div class='line-cell line-cell-btn'>
											<div class='field-title'><mtl:title key="userName" mandatory="false"/></div>
										</div>
										<div class='line-cell line-cell-btn'>
											<div class='field-info'>
												<mtl:value id="record" key="userName"/>
												<mtl:button type="button" icon="images/ico_edit.png" messageKey="jsp.BTN_EDIT"
														onClick="JavaScript: toggleInput(\"inputUserName\");" style="margin-left: 25px;"/>
											</div>
										</div>
									</div>
									<div class='row'>
										<div class='line-cell'>
											<div class='field-title'><mtl:title key="userClass" mandatory="false"/></div>
										</div>
										<div class='line-cell'>
											<div class='field-info'><mtl:valuef id="record" key="userClass"/></div>
										</div>
									</div>
									<div class='row'>
										<div class='line-cell'>
											<div class='field-title'><mtl:title key="status" mandatory="false"/></div>
										</div>
										<div class='line-cell'>
											<div class='field-info'><mtl:valuef id="record" key="status"/></div>
										</div>
									</div>
									<div class='row'>
										<div class='line-cell line-cell-btn'>
											<div class='field-title'><mtl:title key="password" descriptionKey="FIELD_PASSWORD" mandatory="false"/></div>
										</div>
										<div class='line-cell line-cell-btn'>
											<div class='field-info'>
												<input type='password' class='none-style' style='font-size: 16px' value='123456789' size="8" disabled>
												<mtl:button type="button" icon="images/ico_edit.png" messageKey="jsp.BTN_EDIT"
														onClick="JavaScript: toggleInput(\"inputPassword\");" style="margin-left: 25px;"/>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</mtl:contentGroup>
				</div>

				<div class='cell' style='padding-left: 10px;'>
					<mtl:contentGroup groupId="userDetailInfo" type="content" styleClass="card">
						<div class='content-title'>
							<div class='title'>
								<h2 style='margin-bottom: 25px;'><mtl:message key="jsp.usr_user_info.SUBTITLE_USER_DETAIL_INFO"/></h2>
								<div class='page-function'>
									<mtl:button type="button" icon="images/ico_edit.png" messageKey="jsp.BTN_EDIT"
											onClick="JavaScript: toggleInput(\"inputDetail\");"/>
								</div>
							</div>
						</div>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='line-cell w210'>
									<div class='field-title'><mtl:title key="groupId" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" format="$H{groupName}"/></div>
								</div>
								<div class='line-cell w160'>
									<div class='field-title'><mtl:title key="availAccessCount" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" format="${USR_AVAILACCESSCOUNT_@availAccessCount}"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="serviceStartDate" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:value id="record" key="serviceStartDate"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="serviceEndDate" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" key="serviceEndDate"/></div>
								</div>
							</div>
						</div>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='line-cell w210'>
									<div class='field-title'><mtl:message key="jsp.usr_user_input.MSG_USR_DEFAULT_SALES_ORGANIZATION"/></div>
								</div>
								<div class='line-cell'>
								<% String countryDefaultOrgCode = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "PartyDefaultSorg;"+sessionMng.getPartyId(), ""); %>
									<div class='field-info'>
										<mtl:select id="record" key="extraValue4" hasBlank="true"
											nullValueKey="jsp.dpr_item_tree.MSG_ORGANIZATION_BLANK"
											defaultValue="<%=countryDefaultOrgCode%>"
											listId="organizations" listCodeKey="organizationCode" listNameFormat="$S{[:organizationCode;$S{]:organizationName}}"/>
									</div>
								</div>
								<div class='line-cell w160'></div>
								<div class='line-cell'></div>
							</div>
						<%
							if( !"JNJAP_KR".equals(sessionMng.getPartyId()) || sessionMng.isSystemAdmin() ) {
								for( int i = 0; i < organizations.size(); i++ ) {
									Map<String, Object> _record = organizations.get(i);
									if( (i + 1) % 2 != 0 ) {
										out.println( "<div class='row'>" );
									}
									out.print( "<div class='line-cell'><div class='field-title'>" );
									out.print( msghandler.getMessage("jsp.usr_user_input.USER_EMPLOYEE_ID", (String)_record.get("organizationCode")) );
									out.println( "</div></div>" );
									out.print( "<div class='line-cell'><div class='field-info'>" );
									String employee = (String)_record.get("employeeId");
									out.print( employee == null ? "" : employee );
									out.println( "</div></div>" );
									if( (i + 1) % 2 == 0 || i + 1 >= organizations.size() ) {
										out.println( "</div>" );
									}
								}
						%>

							<div class='row'>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="department" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:value id="record" key="department"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="position" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" key="position"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="email" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:value id="record" key="email"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="mobilephone" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" key="mobilephone"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="telephone" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:value id="record" key="telephone"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-title'><mtl:title key="fax" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:valuef id="record" key="fax"/></div>
								</div>
							</div>
						<% } %>
						</div>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='line-cell w210'>
									<div class='field-title'><mtl:title key="description" mandatory="false"/></div>
								</div>
								<div class='line-cell'>
									<div class='field-info'><mtl:value id="record" key="description"/></div>
								</div>
							</div>
						</div>
					</mtl:contentGroup>
				</div>
			</div>
		</div>
		<div class='list-function'>
			<div class='button'>
				<mtl:button type="return"/>
			<% if( sessionMng.isAuthorized("USR", "USRUser.MNG") && (sessionMng.isSystemAdmin() || (sessionMng.isPartyAdmin() && sessionMng.getPartyId().equals(recordMap.get("partyId")))) ) { %>
				<mtl:button type="modify"/>
			<% } %>
			<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION && sessionMng.isAuthorized("DPR", "DPRPartyAuth.MNG") && !sessionMng.getUniqId().equals(recordMap.get("uniqId")) ) { %>
				<mtl:button type="button" onClick="JavaScript: setPartyAuth()" messageKey="jsp.BTN_TRADEPARTNER_AUTH"/>
			<% } %>
			</div>
		</div>
	</mtl:form>

	<div id='inner_overlay' class='inner-overlay'>
		<div class='inner-content-wrap'>
			<div id='inputUserName'>
				<mtl:form name="frmUserName" fieldSetId="fieldSet" onSubmit="JavaScript:return userNameCheckInput();">
					<input type='hidden' name='url'/>
					<input type='hidden' name='mode' value='usrname'>
					<input type='hidden' name='partyId' value='<%= sessionMng.getPartyId() %>'>
					<input type='hidden' name='userId' value='<%= sessionMng.getUserId() %>'>
					<mtl:contentGroup groupId="userName" type="list" styleClass="inner-content">
						<h2><mtl:message key="jsp.usr_user_info.SUBTITLE_CHANGE_USERNAME"/></h2>
						<div class='info-table'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:title key="userName"/>
									</div>
									<div class='field'>
										<mtl:text id="record" key="userName" onlyInput="true"/>
									</div>
								</div>
							</div>
						</div>
						<div class='info-bottom'>
							<div class='table-cell info-button'>
								<mtl:button type="cancel" name="cancel"/>
								<mtl:button type="save"/>
							</div>
						</div>
					</mtl:contentGroup>
				</mtl:form>
			</div>

			<div id='inputPassword'>
				<mtl:form name="frmPassword" method="post" fieldSetId="fieldSet" onSubmit="JavaScript:return passwordCheckInput();">
					<input type='hidden' name='url'/>
					<input type='hidden' name='mode' value='passwd'>
					<input type='hidden' name='partyId' value='<%= sessionMng.getPartyId() %>'>
					<input type='hidden' name='userId' value='<%= sessionMng.getUserId() %>'>
					<input type='hidden' name='locale' value='<%= htmlpage.getLocale() %>'>
					<mtl:contentGroup groupId="password" type="list" styleClass="inner-content">
						<h2><mtl:message key="jsp.usr_user_info.SUBTITLE_CHANGE_PASSWORD"/></h2>
						<div class='info-table'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:title key="password" name="presentPassword" mandatory="true" descriptionKey="FIELD_USR_PRESENT_PASSWORD"/>
									</div>
									<div class='field'>
										<input type='password' name='presentPassword' class='input-field' maxlength='20' style='width: calc(100% - 100px);'>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="password"/></div>
									<div class='field'>
										<input type='password' name='password' class='input-field' maxlength='20' style='width: calc(100% - 100px);'>
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
									<div class='field-title'><mtl:title key="password" name="password2" descriptionKey="FIELD_USR_PASSWORD2"/></div>
									<div class='field'>
										<input type='password' name='password2' class='input-field' maxlength='20' style='width: calc(100% - 100px);'>
									</div>
								</div>
							</div>
						</div>
						<div class='info-bottom'>
							<div class='table-cell info-button'>
								<mtl:button type="cancel" name="cancel"/>
								<mtl:button type="save"/>
							</div>
						</div>
					</mtl:contentGroup>
				</mtl:form>
			</div>

			<div id='inputDetail'>
				<mtl:form name="frmDetail" method="post" fieldSetId="fieldSet" onSubmit="JavaScript:return detailCheckInput();">
					<%@ include file="include_rbm_form.inc" %>
					<mtl:contains id="record" key="gln">
						<mtl:hidden id="record" key="gln" name="countryCode"/>
					</mtl:contains>
					<mtl:hidden id="record" key="extraValue1"/>
					<input type='hidden' name='url'/>
					<input type='hidden' name='mode' value='mod'>
					<input type='hidden' name='type' value='info'>
					<mtl:hidden id="record" key="partyId"/>
					<mtl:hidden id="record" key="userId"/>
					<mtl:hidden id="record" key="userName"/>
					<mtl:hidden id="record" key="userType"/>
					<mtl:hidden id="record" key="status"/>
					<mtl:contentGroup groupId="detail" type="list" styleClass="inner-content"  style='overflow: auto'>
						<h2><mtl:message key="jsp.usr_user_info.SUBTITLE_CHANGE_DETAIL"/></h2>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell w210'>
									<div class='field-title'><mtl:title key="groupId"/></div>
								</div>
								<div class='cell'>
									<div class='field'>
									<% if( fieldOption.hasManageAuth("groupId") && pageContext.findAttribute("groups") != null ) { %>
										<mtl:select id="record" key="groupId" format="$H{groupName}" listId="groups"
												listCodeKey="groupId" listNameFormat="$H{groupName}" searchable="false" onlyInput="true"/>
									<% } else { %>
										<mtl:valuef id="record" format="$H{groupName}"/>
										<mtl:hidden id="record" key="groupId"/>
									<% } %>
									</div>
								</div>
								<div class='cell w160'>
									<div class='field-title'><mtl:title key="availAccessCount"/></div>
								</div>
								<div class='cell'>
									<div class='field'>
									<% if( fieldOption.hasManageAuth("availAccessCount") ) { %>
										<mtl:select id="record" key="availAccessCount" prefixKey="USR_AVAILACCESSCOUNT_"
												codeValues="<%= fieldOption.getVaildAccessCountValues() %>" onlyInput="true"/>
									<% } else { %>
										<mtl:select id="record" key="availAccessCount" prefixKey="USR_AVAILACCESSCOUNT_" readonly="true" onlyInput="true"/>
									<% } %>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell w210'>
									<div class='field-title'><mtl:title key="serviceStartDate"/></div>
								</div>
								<div class='cell'>
									<div class='field'>
									<% if( fieldOption.hasManageAuth("serviceStartDate") ) { %>
										<mtl:date id="record" key="serviceStartDate" onlyInput="true"/>
									<% } else { %>
										<mtl:text id="record" key="serviceStartDate" readonly="true" onlyInput="true"/>
									<% } %>
									</div>
								</div>
								<div class='cell w160'>
									<div class='field-title'><mtl:title key="serviceEndDate"/></div>
								</div>
								<div class='cell'>
									<div class='field'>
									<% if( fieldOption.hasManageAuth("serviceStartDate") ) { %>
										<mtl:date id="record" key="serviceEndDate" onlyInput="true"/>
									<% } else { %>
										<mtl:text id="record" key="serviceEndDate" readonly="true" onlyInput="true"/>
									<% } %>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell w210'>
									<div class='field-title'><mtl:message key="jsp.usr_user_input.MSG_USR_DEFAULT_SALES_ORGANIZATION"/></div>
								</div>
								<div class='cell'>
								<% String countryDefaultOrgCode = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "PartyDefaultSorg;"+sessionMng.getPartyId(), ""); %>
									<div class='field'>
										<mtl:select id="record" key="extraValue4" defaultValue="<%=countryDefaultOrgCode%>" onlyInput="true"
												listId="organizations" listCodeKey="organizationCode" listNameFormat="$S{[:organizationCode;$S{]:organizationName}}"/>
									</div>
								</div>
								<div class='cell w160'></div>
								<div class='cell'></div>
							</div>

						<%
							if( !"JNJAP_KR".equals(sessionMng.getPartyId()) || sessionMng.isSystemAdmin() ) {
								for( int i = 0; i < organizations.size(); i++ ) {
									Map<String, Object> _record = organizations.get(i);
									if( (i + 1) % 2 != 0 ) {
										out.println( "<div class='row'>" );
									}
									out.print( "<div class='cell'><div class='field-title'>" );
									out.print( msghandler.getMessage("jsp.usr_user_input.USER_EMPLOYEE_ID", (String)_record.get("organizationCode")) );
									out.println( "</div></div>" );
									out.print( "<div class='cell'><div class='field'>" );
						%>
									<mtl:text id="_record" key="employeeId" readonly="<%= !isCountryAdmin %>" onlyInput="true"/>
									<mtl:hidden id="_record" key="organizationCode"/>
						<%
									out.println( "</div></div>" );
									if( (i + 1) % 2 == 0 || i + 1 >= organizations.size() ) {
										out.println( "</div>" );
									}
								}
						%>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="department" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="department" onlyInput="true"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="position" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="position" onlyInput="true"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="email" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="email" onlyInput="true"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="mobilephone" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="mobilephone" onlyInput="true"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="telephone" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="telephone" onlyInput="true"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="fax" mandatory="false"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="fax" onlyInput="true"/></div>
								</div>
							</div>
						<% } %>
						</div>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell w210' style='vertical-align: middle;'>
									<div class='field-title'><mtl:title key="description"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:textarea id="record" key="description" rows="4" onlyInput="true"/></div>
								</div>
							</div>
						</div>

						<div class='info-bottom'>
							<div class='table-cell info-button'>
								<mtl:button type="cancel" name="cancel"/>
								<mtl:button type="save"/>
							</div>
						</div>
					</mtl:contentGroup>
				</mtl:form>
			</div>
		</div>
	</div>

	<%@ include file="include_dpr_tail.inc" %>
</body>
</mtl:html>
