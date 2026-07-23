<%--
	File Name:	sys_stdcode_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
	stghr12		2007/04/30		2.1.0	version up(not changed)
	stghr12		2006/02/28		2.0.0	version up(sys_code_input.jsp에서 분리)
	stghr12		2004/06/03		1.0.0	create
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
	String type = request.getParameter( "type" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 600 );
			resetForm( frmMain );
			focusForm( frmMain );
		}

		function modifyReq() {
			var url = "<%= htmlpage.getRequestURL() %>?type=<%= type %>&mode=imod&code="+ encodeURIComponent( "<mtl:value id="record" key="code"/>" );
			windowSelfOpen( url );
		}

		function registReq() {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?type=<%= type %>&mode=ireg" );
		}
	</script>
</head>

<body>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>
		<mtl:hidden id="request" key="type"/>

		<mtl:contentGroup groupId="input" type="content">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='25%'><mtl:title key="code"/></td>
				<td class='content0' width='75%'><mtl:text id="record" key="code" styleClass="length_5"/></td>
			</tr>
			</table>

		<% if( "unit".equals(type) ) { %>
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='25%'><mtl:title key="classCode"/></td>
				<td class='content0' width='75%'><mtl:select id="record" key="classCode"/></td>
			</tr>
			</table>
		<% } %>

		<% if( "curr".equals(type) || "unit".equals(type) ) { %>
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='25%'><mtl:title key="symbol"/></td>
				<td class='content0' width='75%'><mtl:text id="record" key="symbol" styleClass="length_10"/></td>
			</tr>
			</table>
		<% } %>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='25%'><mtl:title key="name"/></td>
				<td class='content0' width='75%'><mtl:text id="record" key="name" styleClass="length_40"/></td>
			</tr>
			</table>
		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return" styleClass="btn_page"/>
			<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
				<mtl:button type="submit" styleClass="btn_page"/>
				<mtl:button type="reset" styleClass="btn_page"/>
			<% } else if( htmlpage.hasManageAuth() ) { %>
				<% if( "reg".equals(htmlpage.getMode()) ) { %>
					<mtl:button type="regist" styleClass="btn_page"/>
				<% } %>
				<mtl:button type="modify" styleClass="btn_page"/>
			<% } %>
			<mtl:button type="close_if" styleClass="btn_page"/>
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
