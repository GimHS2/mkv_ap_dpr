<%--
	File Name:	dpr_ordertemplate_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	keehe		2008/09/26		2.2.0	create
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
		java.util.Map<String, Object> condition = (java.util.Map<String, Object>)pageContext.findAttribute( "condition" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		var windowWidth= 620;
		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPROrderTemplate?mode=imod";
			windowSelfOpen( url +"&templateKey="+ encodeURIComponent("<mtl:value id="record" key="templateKey"/>") );
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<mtl:hidden id="record" key="countryCode"/>
			<mtl:hidden id="record" key="orderKey"/>
			<mtl:hidden id="record" key="templateKey" />
			<mtl:hidden id="record" key="updateDateTime" />

			<mtl:contentGroup groupId="orderTemplate" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="templateName"/></div>
							<div class='field'><mtl:text id="record" key="templateName"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="manageUserId"/></div>
							<div class='field'><mtl:text id="record" key="manageUserId" readonly="true"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:message key="FIELD_DPR_TEMPLATE_SOLDPARTYCODE"/></div>
							<div class='field'><mtl:text id="record" key="soldPartyName" readonly="true"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="updateDateTime"/></div>
							<div class='field'><mtl:text id="record" key="updateDateTime" readonly="true"/></div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="return" style="letter-spacing: 1"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="save" style="letter-spacing: 1"/>
						<mtl:button type="reset" style="letter-spacing: 1"/>
					<% } else if( htmlpage.hasManageAuth() ) { %>
						<mtl:button type="modify" style="letter-spacing: 1"/>
					<% } %>
						<mtl:button type="close_if" onClick="javascript:windowClose(false);" style="letter-spacing: 1"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					if( !Field.checkMandatory(frmMain.templateName) ) return false;
					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
