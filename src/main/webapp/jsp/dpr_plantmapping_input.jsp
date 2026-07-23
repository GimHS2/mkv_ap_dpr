<%--
	File Name:	dpr_plantmapping_input.jsp
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
<%
	boolean readOnly = "imod".equals( htmlpage.getMode() ) ? true : false;
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>?mode=imod";
			url = replaceQueryValue( url, "itemCode", "<mtl:value id="record" key="itemCode" encodeScript="true"/>" );
			windowSelfOpen( url );
		}

		function registReq() {
			var url = "<%= systemConfig.getClassURL() %>?mode=ireg";
			windowSelfOpen( url );
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_rbm_bodyheader.inc"%>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>

			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="plantCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="plantCode" readonly="<%= readOnly %>"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="soldPartyCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="soldPartyCode"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="shipPartyCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="shipPartyCode"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="close_if"/>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="reset"/>
						<mtl:button type="save"/>
					<% } %>
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
