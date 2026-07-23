<%--
	File Name:	dpr_user_org.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.0	create
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
	boolean isCountryAdmin = ( "AD".equals(sessionMng.getGroupClass()) );
%>
<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<script type='text/javascript' src='script/main.js'></script>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			setInnerHTML( parent.document.all.document_id_employee, document.all.document_employee.innerHTML );
		}

		function setInnerHTML( contentObj, html ) {
			if( contentObj ) contentObj.innerHTML = html;
		}
	</script>
</head>

<body onload="JavaScript:bodyLoad();">
<mtl:form name="frmMain">
	<span id='document_employee'>
		<mtl:loop id="organizations" loopId="_record" maxRows="2">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'>
					<%= msghandler.getMessage( "jsp.usr_user_input.USER_EMPLOYEE_ID", (String)_record.get("organizationCode") ) %>
				<td class='content1'>
					<mtl:text id="_record" key="employeeId" styleClass="length_20" readonly="<%= !isCountryAdmin %>"/>
					<mtl:hidden id="_record" key="organizationCode"/>
				</td>
			</tr>
			</table>
		</mtl:loop>
	</span>
</mtl:form>

</body>
</mtl:html>
