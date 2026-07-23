<%--
	File Name:	dpr_sitelink_input.jsp
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
		java.util.Map condition = (java.util.Map)pageContext.findAttribute( "condition" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 800 );

			resetForm( frmMain );
			focusForm( frmMain, frmMain.title );
		}

        function modifyReq() {
            var url = "<%= systemConfig.getClassURL() %>/DPRSiteLink?mode=imod&linkSequence=<mtl:value id="record" key="linkSequence"/>&countryCode=<mtl:value id="record" key="countryCode"/>";
            windowSelfOpen( url);
        }

	</script>
</head>

<body>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>
		<mtl:hidden id="record" key="linkSequence"/>

		<mtl:contentGroup groupId="country" type="content" descriptionKey="jsp.GRP_DPR_COUNTRY_INFO">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:title key="displayCountryCode"/></td>
				<td class='content1'>
					<mtl:value id="record" key="countryName"/>
					<mtl:hidden id="record" key="displayCountryCode"/>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:title key="linkURL"/></td>
				<td class='content1'><mtl:text id="record" key="linkURL" styleClass="length_50" mandatory="true"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:title key="description"/></td>
				<td class='content1'><mtl:text id="record" key="description" styleClass="length_70" mandatory="true"/></td>
			</tr>
			</table>
		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return" styleClass="btn_page"/>
			<% if( sessionMng.isAuthorized("DPR", "DPRSiteLink.MNG") ) { %>
				<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
					<mtl:button type="submit" styleClass="btn_page"/>
					<mtl:button type="reset" styleClass="btn_page"/>
				<% } else  { %>
					<mtl:button type="modify" styleClass="btn_page"/>
				<% } %>
			<% } %>
			<mtl:button type="close_if" href="javascript:windowClose(true)" styleClass="btn_page"/>
		</td></tr>
		</table>

		<script type='text/javascript'>
			function checkInput() {
				if( !Field.checkMandatory(frmMain.displayCountryCode) ) return false;
				if( !Field.checkMandatory(frmMain.linkURL) ) return false;
				if( !Field.checkMandatory(frmMain.description) ) return false;

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
