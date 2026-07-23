<%--
	File Name:	sys_classcode_input.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2008/03/31		2.2.1	findAttribute("field") -> findAttribute("codeField")
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/20		1.0.0	create
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
	com.irt.sql.HierarchyCodeField field = (com.irt.sql.HierarchyCodeField)pageContext.findAttribute( "codeField" );
	java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
	String type = request.getParameter( "type" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 600 );
			resetInput();
			focusForm( frmMain, frmMain.code );
		}

		function changeClassCode() {
			if( !frmMain.classCode ) return;
			switch( frmMain.classCode.value ) {
			<%
				int level = field.getLastLevel();
				for( int l = 0; l < level; l++ )
					out.println( "case '"+ (l+1) +"': frmMain.code.maxLength = "+ field.getLength(l+1) +"; break;" );
			%>
			}
		}

		function modifyReq() {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?type=<%= type %>&mode=imod&code=<mtl:value id="record" key="code"/>" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?type=<%= type %>&mode=ireg&code=<%= field.getUpperLevelCode((String)recordMap.get("code")) %>";
			windowSelfOpen( url );
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
				<td class='subject0' width='25%'><mtl:title key="classCode"/></td>
				<td class='content0' width='75%'><mtl:select id="record" key="classCode"
						modified="JavaScript:changeClassCode(); Field.modified(this);"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='25%'><mtl:title key="code"/></td>
				<td class='content0' width='75%'><mtl:text id="record" key="code" styleClass="length_15"/></td>
				<% if( "reg".equals(htmlpage.getMode()) || "ireg".equals(htmlpage.getMode()) ) { %>
					<mtl:script>
						var message = "<mtl:message key="jsp.sys_classcode_input.ERR_CODE_LENGTH" encodeScript="true"/>";
						<% if( "hs".equals(type) ) { %>
							switch( frmMain.classCode ) {
							case '1':
							case '2':
								if( frmMain.code.value.length != frmMain.code.maxLength )
									return Field.alertError( frmMain.code, message );
								break;
							case '3':
								if( frmMain.code.value.length <= 4 )
									return Field.alertError( frmMain.code, message );
								break;
							case '4':
								if( frmMain.code.value.length <= 6 )
									return Field.alertError( frmMain.code, message );
								break;
							}
						<% } else { %>
							if( frmMain.code.value.length != frmMain.code.maxLength )
								return Field.alertError( frmMain.code, message );
						<% } %>
					</mtl:script>
				<% } %>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='25%'><mtl:title key="name"/></td>
				<td class='content0' width='75%'><mtl:text id="record" key="name" styleClass="length_40"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject0' width='25%'><mtl:title key="description"/></td>
				<td class='content0' width='75%'><mtl:textarea id="record" key="description" rows="3" styleClass="length_40"/></td>
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
				changeClassCode();
			}
		</script>
	</mtl:form>
</body>
</mtl:html>
