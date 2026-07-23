<%--
	File Name:	dpr_itemextra_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2018/05/30		2.2.0	create
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
	</script>
</head>
<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>

	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>

		<mtl:contentGroup groupId="main" type="content">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject' width='25%'><mtl:title key="organizationCode"/></td>
				<td class='content2'>
				<mtl:valuef id="record" key="organizationCode" format="$S{[:organizationCode;]$S{ :organizationName}}"/>
				<mtl:hidden id="record" key="organizationCode"></mtl:hidden>
				<mtl:hidden id="record" key="organizationName"></mtl:hidden>
				</td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td class='subject' width='25%'><mtl:title key="itemCode"/></td>
				<td class='content2'>
					<mtl:text id="record" key="itemCode" readonly="false" styleClass="length_25"/>
				</td></tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr><td class='subject' width='25%'><mtl:title key="itemExtraDesc"/></td>
					<td class='content2'>
						<mtl:text id="record" key="itemExtraDesc" readonly="false" styleClass="length_25"/>
					</td></tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td class='subject' width='25%'><mtl:title key="itemExtraCate"/></td>
				<td class='content2'>
					<mtl:text id="record" key="itemExtraCate" readonly="false" styleClass="length_25"/>
				</td></tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td class='subject' width='25%'><mtl:title key="itemExtraAbbrev"/></td>
				<td class='content2'>
					<mtl:text id="record" key="itemExtraAbbrev" readonly="false" styleClass="length_25"/>
				</td></tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td class='subject' width='25%'><mtl:title key="itemExtraSpec"/></td>
				<td class='content2'>
					<mtl:text id="record" key="itemExtraSpec" readonly="false" styleClass="length_25"/>
				</td></tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td class='subject' width='25%'><mtl:title key="uomNameLocal"/></td>
				<td class='content2'>
					<mtl:text id="record" key="uomNameLocal" readonly="false" styleClass="length_25"/>
				</td></tr>
			</table>
		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return" styleClass="btn_page"/>

				<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
					<mtl:button type="submit" styleClass="btn_page"/>
					<mtl:button type="reset" styleClass="btn_page"/>
				<% } %>
			<mtl:button type="close_if" href="javascript:windowClose(true)" styleClass="btn_page"/>
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
</body>
</mtl:html>
