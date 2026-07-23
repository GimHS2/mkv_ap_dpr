<%--
	File Name:	dpr_main_org_input.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2011/02/28		2.2.1	organizationCode listNameFormat ¼öÁ¤
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
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			var completed = "<mtl:value id="property" key="completed"/>";
			if( completed == "OK" ) {
				var winType = "<mtl:value id="request" key="wintype"/>";

				if( winType == "sub" )
					windowClose( true );
				else {
					var url = "<%= systemConfig.getClassURL() %>/Menu?type=dpr";
					url = attachDefaultParameter(url);
					url = replaceQueryValue( url, "menu", "init" );

					window.open( url, "_parent" );
				}
			} else {
				windowResizeTo( 600 );

				var organizationCode = "<%= property.getProperty("organizationCode") %>";
				if( organizationCode == null || organizationCode == "" )
					document.all.msg.innerHTML = "<%= msghandler.getMessage("ERR_NEEDED_SELECT_ORGANIZATION") %>";

				resetForm( frmMain );
				focusForm( frmMain, frmMain.title );
			}
		}

		function setOrganization( organizationCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRMain?mode=def";
			url = replaceQueryValue( url, "organizationCode", organizationCode );

			windowSelfOpen( url );
		}
	</script>
</head>

<body>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" fieldSetId="fieldSet" enctype="multipart/form-data" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>

		<mtl:contentGroup groupId="country" type="content">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:message key="MSG_DEFAULT_SALES_ORGANITION"/></td>
					<td class='content1'><mtl:select id="property" key="organizationCode" nullValueKey="MSG_COND_SALES_ORGANIZATION"
							hasBlank="true" listId="organizations" listCodeKey="organizationCode" listNameFormat="${[:organizationCode;]} ${organizationName}"
							modified="JavaScript:setOrganization(this.value);"/>
					</td>
				</tr>
				</table>
			</td></tr>
			</table>
		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return" styleClass="btn_page"/>
			<mtl:button type="close_if" href="JavaScript:windowClose(true);" styleClass="btn_page"/>
		</td></tr>
		</table>

		<script type='text/javascript'>
			function checkInput() {
				if( !Field.checkMandatory(frmMain.itemImageFile) ) return false;

				return submitInput();
			}

			function resetInput() {
				frmMain.reset();
				resetForm( frmMain );
			}
		</script>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
