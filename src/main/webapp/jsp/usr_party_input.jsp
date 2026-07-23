<%--
	File Name:	usr_party_input.jsp
	Version:	2.2.3c

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2018/10/30		2.2.3c	PartyLoginBlock 적용
	stghr12		2009/10/31		2.2.3	<mtl:select> 변경사항 적용
	GimHS		2008/10/17		2.2.2	CustomTag 속성값에 큰따옴표 사용시 오류나는 것 수정 (큰따옴표 앞에 '\' 추가)
	stghr12		2008/03/31		2.2.1	serviceDate 옵션처리 오류 수정
										partyid.maxlength: fieldOption으로 처리
	stghr12		2007/11/30		2.2.0	create
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
	java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
	com.irt.rbm.usr.UserPartyFieldOption fieldOption = (com.irt.rbm.usr.UserPartyFieldOption)pageContext.findAttribute( "fieldOption" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		var windowWidth = 1100;

		$( function() {
			if( $("textarea[name=blockTemplate]").val() == "" ) {
				$("textarea[name=blockTemplate]").val( "<%= msghandler.getMessage("MSG_TEMPLATE_LOGIN_BLOCK") %>" );
			}
			if( $("textarea[name=mtnceTemplate]").val() == "" ) {
				$("textarea[name=mtnceTemplate]").val( "<%= msghandler.getMessage("MSG_TEMPLATE_MTNCE_BLOCK") %>" );
			}
			if( $("input[name=mtnceTemplateTitle]").val() == "" ) {
				$("textarea[name=mtnceTemplate]").val( "<%= msghandler.getMessage("MSG_TEMPLATE_MTNCE_TITLE") %>" );
			}
		});

		function modifyReq() {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?mode=imod&partyId=<mtl:value id="record" key="partyId"/>" );
		}

		function registReq() {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?mode=ireg" );
		}

		function uniqueCheckReq( elementObj ) {
			if( !Field.checkMandatory(elementObj) ) return;

			var url = "<%= htmlpage.getRequestURL() %>?mode=chk&wintype=sub&slname="+ elementObj.name;
			windowOpen( replaceQueryValue(url, elementObj.name, elementObj.value), "clsMng" );
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>

			<mtl:contentGroup groupId="input" type="content">
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="partyId"/></td>
					<td class='content2'>
						<mtl:text id="record" key="partyId" maxlength="<%= fieldOption.getMaxLength(\"partyId\") %>" styleClass="length_userid"/>
						<mtl:ibutton type="button" imageSrc="images/tbtn_uniqchk.gif" key="partyId" styleClass="tbtn"
								href="JavaScript:uniqueCheckReq(frmMain.partyId);"/>
					</td>
					<td class='subject'><mtl:title key="partyName"/></td>
					<td class='content2'><mtl:text id="record" key="partyName" styleClass="length_username"/></td>
				</tr>
				</table>

				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
				<% if( fieldOption.using("partyClass") ) { %>
					<td class='subject'><mtl:title key="partyClass"/></td>
					<td class='content2'>
					<% if( fieldOption.hasManageAuth("partyClass") ) { %>
						<mtl:select id="record" key="partyClass"/>
					<% } else { %>
						<mtl:valuef id="record" key="partyClass"/>
					<% } %>
					</td>
				<% } else if( fieldOption.using("partyGln") ) { %>
					<td class='subject'><mtl:title key="partyGln"/></td>
					<td class='content2'>
					<% if( fieldOption.hasManageAuth("partyGln") ) { %>
						<mtl:text id="record" key="partyGln" styleClass="length_gln"/>
					<% } else { %>
						<mtl:text id="record" key="partyGln" readonly="true" styleClass="length_gln"/>
					<% } %>
					</td>
				<% } %>
					<td class='subject'><mtl:title key="status" mandatory="true"/></td>
					<td class='<%= fieldOption.using("partyClass") || fieldOption.using("partyGln") ? "content2" : "content1" %>'>
					<% if( fieldOption.hasManageAuth("status") ) { %>
						<mtl:select id="record" key="status" mandatory="true" codeValues="00,99"/>
					<% } else { %>
						<mtl:select id="record" key="status" readonly="true"/>
					<% } %>
					</td>
				</tr>
				</table>

	<% if( fieldOption.using("partyClass") && fieldOption.using("partyGln") ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="partyGln"/></td>
				<% if( fieldOption.hasManageAuth("partyGln") ) { %>
					<td class='content1'><mtl:text id="record" key="partyGln" styleClass="length_gln"/></td>
				<% } else { %>
					<td class='content1'><mtl:text id="record" key="partyGln" readonly="true" styleClass="length_gln"/></td>
				<% } %>
				</tr>
				</table>
	<% } %>

	<% if( com.irt.rbm.sys.TimeZone.usingTimeZone() ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="timeZone" mandatory="true"/></td>
					<td class='content1'><mtl:select id="record" key="timeZone" format="$H{timeZoneName}" mandatory="true"
							className="com.irt.rbm.sys.TimeZone"/></td>
				</tr>
				</table>
	<% } %>

	<% if( fieldOption.hasManageAuth("serviceStartDate") ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="serviceStartDate"/></td>
					<td class='content2'>
						<mtl:date id="record" key="serviceStartDate"/>
					</td>
					<td class='subject'><mtl:title key="serviceEndDate"/></td>
					<td class='content2'>
						<mtl:date id="record" key="serviceEndDate"/>
					</td>
				</tr>
				</table>
	<% } else if( fieldOption.using("serviceStartDate") ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="serviceStartDate"/></td>
					<td class='content2'><mtl:text id="record" key="serviceStartDate" readonly="true"/></td>
					<td class='subject'><mtl:title key="serviceEndDate"/></td>
					<td class='content2'><mtl:text id="record" key="serviceEndDate" readonly="true"/></td>
				</tr>
				</table>
	<% } %>

	<% if( fieldOption.using("blockStartIsoDate") ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="blockStartIsoDate"/></td>
					<td class='content2'>
					<% if( fieldOption.hasManageAuth("blockStartIsoDate") ) { %>
						<mtl:text id="record" key="blockStartIsoDate" styleClass="length_25"/>
					<% } else { %>
						<mtl:text id="record" key="blockStartIsoDate" readonly="true" styleClass="length_25"/>
					<% } %>
					</td>
					<td class='subject'><mtl:title key="blockEndIsoDate"/></td>
					<td class='content2'>
					<% if( fieldOption.hasManageAuth("blockEndIsoDate") ) { %>
						<mtl:text id="record" key="blockEndIsoDate" styleClass="length_25"/>
					<% } else { %>
						<mtl:text id="record" key="blockEndIsoDate" readonly="true" styleClass="length_25"/>
					<% } %>
					</td>
				</tr>
				</table>
	<% } %>
	<% if( fieldOption.using("mtnceStartIsoDate") ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="mtnceStartIsoDate"/></td>
					<td class='content2'>
					<% if( fieldOption.hasManageAuth("mtnceStartIsoDate") ) { %>
						<mtl:text id="record" key="mtnceStartIsoDate" styleClass="length_25"/>
					<% } else { %>
						<mtl:text id="record" key="mtnceStartIsoDate" readonly="true" styleClass="length_25"/>
					<% } %>
					</td>
					<td class='subject'><mtl:title key="mtnceEndIsoDate"/></td>
					<td class='content2'>
					<% if( fieldOption.hasManageAuth("mtnceEndIsoDate") ) { %>
						<mtl:text id="record" key="mtnceEndIsoDate" styleClass="length_25"/>
					<% } else { %>
						<mtl:text id="record" key="mtnceEndIsoDate" readonly="true" styleClass="length_25"/>
					<% } %>
					</td>
				</tr>
				</table>
	<% } %>

	<% if( fieldOption.using("blockTemplate") ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="blockTemplate"/></td>
					<td class='content1'>
						<mtl:textarea id="record" key="blockTemplate" rows="4"/>
						<%-- <mtl:text id="record" key="blockTemplate" styleClass="length_80" style="height:30px;" defaultValue='<%= msghandler.getMessage("MSG_TEMPLATE_LOGIN_BLOCK") %>'/> --%>
					</td>
				</tr>
				</table>
	<% } %>
	<% if( fieldOption.using("mtnceTemplate") ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="mtnceTemplateTitle"/></td>
					<td class='content1'>
						<mtl:text id="record" key="mtnceTemplateTitle" styleClass="length_80" style="height:30px;" defaultValue='<%= msghandler.getMessage("MSG_TEMPLATE_MTNCE_TITLE") %>'/>
					</td>
				</tr>
				</table>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="mtnceTemplate"/></td>
					<td class='content1'>
						<mtl:textarea id="record" key="mtnceTemplate" rows="4"/>
						<%-- <mtl:text id="record" key="mtnceTemplate" styleClass="length_80" style="height:30px;" defaultValue='<%= msghandler.getMessage("MSG_TEMPLATE_MTNCE_BLOCK") %>'/> --%>
					</td>
				</tr>
				</table>
	<% } %>

	<% if( fieldOption.hasManageAuth("chargeStartDate") ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="chargeStartDate"/></td>
					<td class='content1'>
						<mtl:text id="record" key="chargeStartDate" onBlur="JavaScript:Field.formatDate(this);" styleClass="length_date_ymd"/>
						<mtl:ibutton type="calendar" key="chargeStartDate" styleClass="tbtn"/>
					</td>
				</tr>
				</table>
	<% } %>

	<% if( fieldOption.hasManageAuth("partyRegistration") || fieldOption.hasManageAuth("telephone") ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
				<% if( fieldOption.hasManageAuth("partyRegistration") && fieldOption.hasManageAuth("telephone") ) { %>
					<td class='subject'><mtl:title key="partyRegistration"/></td>
					<td class='content2'><mtl:text id="record" key="partyRegistration" styleClass="length_20"/></td>
					<td class='subject'><mtl:title key="telephone"/></td>
					<td class='content2'><mtl:text id="record" key="telephone" styleClass="length_20"/></td>
				<% } else if( fieldOption.hasManageAuth("partyRegistration") ) { %>
					<td class='subject'><mtl:title key="partyRegistration"/></td>
					<td class='content1'><mtl:text id="record" key="partyRegistration" styleClass="length_20"/></td>
				<% } else { %>
					<td class='subject'><mtl:title key="telephone"/></td>
					<td class='content1'><mtl:text id="record" key="telephone" styleClass="length_20"/></td>
				<% } %>
				</tr>
				</table>
	<% } %>

				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:title key="description"/></td>
					<td class='content1'><mtl:textarea id="record" key="description" rows="4" styleClass="length_80"/></td>
				</tr>
				</table>
			</mtl:contentGroup>

			<table class='btn_page' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return"/>
				<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
					<mtl:button type="submit"/>
					<mtl:button type="reset"/>
				<% } else if( htmlpage.hasManageAuth() ) { %>
					<% if( "reg".equals(htmlpage.getMode()) ) { %>
						<mtl:button type="regist"/>
					<% } %>
					<mtl:button type="modify"/>
				<% } %>
				<mtl:button type="close_if"/>
			</td></tr>
			</table>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>
					return submitInput();
				}

				function resetInput() {
					frmMain.reset();
					resetForm( frmMain );
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
