<%--
	File Name:	dpr_stockquery_mng.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.0	create
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
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc"%>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<input type="hidden" name="mode" value="mng">
			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="autoRetry"/></div>
							<div class='field'>
								<mtl:select id="record" key="autoRetry" prefixKey="DPR_STOCK_QUERY_AUTORETRY_" codeValues="Y,N" hasBlank="false"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="inputQty"/></div>
							<div class='field'>
								<mtl:text id="record" key="inputQty" readonly="<%= !sessionMng.isSystemAdmin() %>" />
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="simulationLineNumber"/></div>
							<div class='field'>
								<mtl:text id="record" key="simulationLineNumber"/>
							</div>
						</div>
					</div>
				</div>

				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="save"/>
						<mtl:button type="reset"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>
					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
