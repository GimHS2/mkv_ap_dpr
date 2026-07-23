<%--
	File Name:	usr_effectuser_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										FIELD_USR_PARTYID, FIELD_USR_USERID -> FIELD_PARTYID, FIELD_USERID
	stghr12		2007/04/30		2.1.0	version up(not changed)
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/21		1.0.0	create
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
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 600 );
			resetForm( frmMain );
			focusForm( frmMain );
		}

		function checkInput() {
			if( !Field.checkMandatory(frmMain.partyId) ) return false;
			if( !Field.checkMandatory(frmMain.userId) ) return false;

			return submitInput();
		}

		function selectUserUserReq( naming ) {
			if( !Field.checkMandatory(frmMain.partyId) ) return;
			if( naming )
				return namingUserUser( "user", "opt&useUserId=true&partyId="+ frmMain.partyId.value, frmMain.userId.value );
			else
				selectUserUser( "user", "opt&useUserId=true&partyId="+ frmMain.partyId.value, frmMain.userId.value );
		}
	</script>
</head>

<body>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>

		<mtl:contentGroup groupId="effectUser" type="content" descriptionKey="jsp.usr_effectuser_input.GRP_EFFECTUSER">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='20%'><mtl:message key="FIELD_PARTYID"/></td>
				<td class='content0' width='30%'><%= HtmlUtility.toHtmlString(sessionMng.getPartyId()) %></td>
				<td class='subject0' width='20%'><mtl:message key="FIELD_USR_PARTYNAME"/></td>
				<td class='content0' width='30%'><%= HtmlUtility.toHtmlString(sessionMng.getPartyName()) %></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='20%'><mtl:message key="FIELD_USERID"/></td>
				<td class='content0' width='30%'><%= HtmlUtility.toHtmlString(sessionMng.getUserId()) %></td>
				<td class='subject0' width='20%'><mtl:message key="FIELD_USR_USERNAME"/></td>
				<td class='content0' width='30%'><%= HtmlUtility.toHtmlString(sessionMng.getUserName()) %></td>
			</tr>
			</table>
		</mtl:contentGroup>

		<mtl:contentGroup groupId="user" type="content" descriptionKey="jsp.usr_effectuser_input.GRP_USER">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='20%'><mtl:title key='partyId' descriptionKey="FIELD_PARTYID" mandatory="true"/></td>
				<td class='content0' width='80%'>
					<mtl:text id="record" key="partyId" mandatory="true" styleClass="length_userid"/>
					<mtl:ibutton type="select" href="JavaScript:selectUserParty(\"party\", \"opt\");"
							naming="JavaScript:return namingUserParty(\"party\", \"opt\", frmMain.partyId.value);"
							titleKey="jsp.BTN_SELECT_PARTYID" styleClass="tbtn"/>
					<mtl:text id="record" key="partyName" readonly="true" styleClass="length_45"/>
				</td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='20%'><mtl:title key='userId' descriptionKey="FIELD_USERID" mandatory="true"/></td>
				<td class='content0' width='80%'>
					<mtl:text id="record" key="userId" mandatory="true" styleClass="length_userid"/>
					<mtl:ibutton type="select" href="JavaScript:selectUserUserReq(false);"
							naming="JavaScript:return selectUserUserReq(true);"
							titleKey="jsp.BTN_SELECT_USERID" styleClass="tbtn"/>
					<mtl:text id="record" key="userName" readonly="true" styleClass="length_45"/>
				</td>
			</tr>
			</table>
		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return" styleClass="btn_page"/>
			<mtl:button type="submit" styleClass="btn_page"/>
			<mtl:button type="reset" styleClass="btn_page"/>
			<mtl:button type="close_if" styleClass="btn_page"/>
		</td></tr>
		</table>
	</mtl:form>
</body>
</mtl:html>
