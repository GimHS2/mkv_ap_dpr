<%--

	File Name:	usr_user_input.jsp
	Version:	2.2.8(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/04/30		2.2.8	한국일 경우에 사용자 정보 숨김.
	jbaek		2019/11/30		2.2.7	User 기본 조직 기능 추가
	jbaek		2015/06/30		2.2.6	minor design 변경: EmployeeId 관련.
	jbaek		2013/10/30		2.2.5	userId Html escaping
	lsinji		2010/12/30		2.2.4	employeeId 표시 갯수 변경
	lsinji		2008/09/26		2.2.3	setPartyAuth() 추가: DPR용
										DPR Page tail (legal notice) add
										Employee ID 추가
	stghr12		2008/05/31		2.2.2	"암호만료"상태일 때 암호가 복잡성 검사를 하지 않음.
										사용자등록할 대 "암호만료"상태이면 암호가 사용자ID로 변경되는 오류 수정
	stghr12		2008/03/31		2.2.1	serviceDate 옵션처리 오류 수정
										userId.maxlength: fieldOption으로 처리
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
	stghr12		2007/04/30		2.1.1	UserUserFieldOption 사용
										암호만료일 경우라도 암호를 입력하도록 수정
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리
	stghr12		2006/06/01		2.0.0	version up
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
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

		function modifyReq() {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?mode=imod&uniqId=<mtl:value id="record" key="uniqId"/>" );
		}

		function registReq() {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?mode=ireg" );
		}

		function uniqueCheckReq( elementObj ) {
			if( !Field.checkMandatory(elementObj) ) return;

			var url = "<%= htmlpage.getRequestURL() %>?mode=chk&wintype=sub&slname="+ elementObj.name;
			url = replaceQueryValue( url, "partyId", encodeURIComponent(frmMain.partyId.value) );
			windowOpen( replaceQueryValue(url, elementObj.name, elementObj.value), "clsName" );
		}

		function uniqueCheckPopup( available, message ) {
			if( available ) {
				toggleFieldErrorMessage( frmMain.userId, false );
				var messages = { "header" : message };
				customPopup.alert( messages );
			} else {
				toggleFieldErrorMessage( frmMain.userId, true, message );
			}
		}

		function setPartyAuth() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyAuth?mode=list";
			url = replaceQueryValue( url, "uniqId", "<mtl:value id="record" key="uniqId"/>" );
			url = replaceQueryValue( url, "partyId", "<mtl:value id="record" key="partyId"/>" );
			url = replaceQueryValue( url, "userId", "<mtl:value id="record" key="userId"/>" );

			windowSelfOpen( url, getLocationURL());
		}

	</script>
</head>

<body class='content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>
		<mtl:contains id="record" key="gln">
			<mtl:hidden id="record" key="gln" name="countryCode"/>
		</mtl:contains>
		<mtl:hidden id="record" key="extraValue1"/>
		<div class='table table-fixed' style='min-width: 1200px;'>
			<div class='row'>
				<div class='cell' style='width: 40%; padding-right: 10px;'>
					<mtl:contentGroup groupId="userInfo" type="content" styleClass="card">
						<h2 style='margin-bottom: 25px;'><mtl:message key="jsp.usr_user_info.SUBTITLE_USER_INFO"/></h2>
						<div>
							<img src='images/user_profile.png' style='margin-right: 25px; vertical-align: top'/>
							<div style='display: inline-block; width:calc(100% - 155px);'>
								<div class='info-table table-fixed'>
									<div class='row'>
										<div class='cell w140'>
											<div class='field-title'><mtl:title key="partyId"/></div>
										</div>
										<div class='cell'>
											<div class='field'>
												<mtl:value id="record" key="partyName"/>
												<mtl:hidden id="record" key="partyId"/>
											</div>
										</div>
									</div>
									<div class='row'>
										<div class='cell'>
											<div class='field-title'><mtl:title key="userId"/></div>
										</div>
										<div class='cell'>
											<div class='field'>
											<% if( "ireg".equals(htmlpage.getMode()) ) { %>
												<mtl:text id="record" key="userId" maxlength="<%= fieldOption.getMaxLength(\"userId\") %>"
														style="width: calc(100% - 100px);"/>
												<mtl:button type="s_button" onClick="JavaScript: uniqueCheckReq(frmMain.userId);" messageKey="jsp.BTN_CHECK"/>
											<% } else { %>
												<mtl:text id="record" key="userId" maxlength="<%= fieldOption.getMaxLength(\"userId\") %>"/>
											<% } %>
											</div>
										</div>
									</div>
									<div class='row'>
										<div class='cell cell-btn'>
											<div class='field-title'><mtl:title key="userName"/></div>
										</div>
										<div class='cell cell-btn'>
											<div class='field'><mtl:text id="record" key="userName"/></div>
										</div>
									</div>
									<div class='row'>
										<div class='cell'>
											<div class='field-title'><mtl:title key="userClass" mandatory="true"/></div>
										</div>
										<div class='cell'>
											<div class='field'>
											<% if( fieldOption.hasManageAuth("userClass") ) { %>
												<mtl:select id="record" key="userClass" codeValues="<%= fieldOption.getValidUserClassValues() %>" searchable="false"/>
											<% } else {%>
												<mtl:valuef id="record" key="userClass"/>
											<% } %>
											</div>
										</div>
									</div>
									<div class='row'>
										<div class='cell'>
											<div class='field-title'><mtl:title key="status" mandatory="true"/></div>
										</div>
										<div class='cell'>
											<div class='field'>
											<% if( fieldOption.hasManageAuth("status") ) { %>
												<mtl:select id="record" key="status" codeValues="00,PW,LK,99" mandatory="true" searchable="false"/>
											<% } else { %>
												<mtl:valuef id="record" key="status"/>
											<% } %>
											</div>
										</div>
									</div>
									<div class='row'>
										<div class='cell'>
											<div class='field-title'><mtl:title key="password" descriptionKey="FIELD_PASSWORD"/></div>
										</div>
										<div class='cell'>
											<div class='field'>
												<input type='password' name='password' class='input-field' maxlength='20' style='width: calc(100% - 40px);'>
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
										</div>
										<div class='cell'>
											<div class='field'>
												<input type='password' name='password2' class='input-field' maxlength='20' style='width: calc(100% - 40px);'>
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
						<h2 style='margin-bottom: 25px;'><mtl:message key="jsp.usr_user_info.SUBTITLE_USER_DETAIL_INFO"/></h2>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell w210'>
									<div class='field-title'><mtl:title key="groupId"/></div>
								</div>
								<div class='cell'>
									<div class='field'>
									<% if( fieldOption.hasManageAuth("groupId") && pageContext.findAttribute("groups") != null ) { %>
										<mtl:select id="record" key="groupId" format="$H{groupName}" listId="groups"
												listCodeKey="groupId" listNameFormat="$H{groupName}" searchable="false"/>
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
												codeValues="<%= fieldOption.getVaildAccessCountValues() %>" searchable="false"/>
									<% } else { %>
										<mtl:valuef id="record" format="${USR_AVAILACCESSCOUNT_@availAccessCount}"/>
										<mtl:hidden id="record" key="availAccessCount"/>
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
										<mtl:date id="record" key="serviceStartDate"/>
									<% } else { %>
										<mtl:text id="record" key="serviceStartDate" readonly="true"/>
									<% } %>
									</div>
								</div>
								<div class='cell w160'>
									<div class='field-title'><mtl:title key="serviceEndDate"/></div>
								</div>
								<div class='cell'>
									<div class='field'>
									<% if( fieldOption.hasManageAuth("serviceStartDate") ) { %>
										<mtl:date id="record" key="serviceEndDate"/>
									<% } else { %>
										<mtl:text id="record" key="serviceEndDate" readonly="true"/>
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
										<mtl:select id="record" key="extraValue4" defaultValue="<%=countryDefaultOrgCode%>" searchable="false"
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
									<div class='field-title'><mtl:title key="department"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="department"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="position"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="position"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="email"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="email"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="mobilephone"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="mobilephone"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="telephone"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="telephone"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="fax"/></div>
								</div>
								<div class='cell'>
									<div class='field'><mtl:text id="record" key="fax"/></div>
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
									<div class='field'><mtl:textarea id="record" key="description" rows="4"/></div>
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
				<mtl:button type="submit"/>
				<mtl:button type="reset"/>
			</div>
		</div>

		<script type='text/javascript'>
			function checkInput() {
				<%= htmlpage.getValidationScript() %>
				return submitInput();
			}
		</script>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
