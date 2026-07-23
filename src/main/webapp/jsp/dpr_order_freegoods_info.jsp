<%--
	File Name:	dpr_order_freegoods_info.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/07/31		2.2.1	Freegoods order 생성 성공 여부 메시지 추가
	hankalam	2019/07/31		2.2.0	create
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
	java.util.Map headerMap = (java.util.Map)request.getAttribute("header");
	String type = property.getProperty( "type" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			var type = "<mtl:value id="property" key="type"/>";
			var mode = "<mtl:value id="request" key="mode"/>";
			if( "fgm" == mode && ("create" == type || "map" == type) ) {
				if( "CD" != "<mtl:value id="header" key="status"/>" ) {
					customPopup.alert( { "header" : "<%= htmlpage.getMessageHandler().getMessage( "MSG_FREEGOODS_CREATION_FAILED", HtmlUtility.toHtmlString(htmlpage.getMessage()) ) %>" } );
				} else {
					customPopup.alert( { "header" : "<%= htmlpage.getMessageHandler().getMessage( "MSG_FREEGOODS_CREATION_SUCCESS" ) %>" } );
				}
			}
			windowResizeTo( 800, 600 );
			resetForm( frmMain );
			focusForm( frmMain );
		}
	</script>
</head>
<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<mtl:hidden id="header" key="orderKey" />
			<mtl:hidden id="property" key="type" />
			<input type='hidden' name='mode' value='fgm' />
			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="parentOrderNumber" descriptionKey="FIELD_DPR_ORDER_PARENT_ORDERNUMBER"/></div>
							<div class='field'>
								<mtl:value id="header" key="parentOrderNumber"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="partyCode" mandatory="false"/></div>
							<div class='field'>
								<mtl:value id="header" key="partyCode"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="shipPartyCode" mandatory="false"/></div>
							<div class='field'>
								<mtl:value id="header" key="shipPartyCode"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="orderNumber" mandatory="true"/></div>
							<div class='field'>
								<mtl:text id="header" key="orderNumber" mandatory="true"/>
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

		<% if( "create".equals(type) || "ER".equals((String)headerMap.get("status")) ) { %>
			<mtl:contentGroup groupId="list" type="list">
				<%
					ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );
					listWriter.print( out );
				%>
				<div class='list-function'>
					<div class='button'>
					</div>
					<div id='list_page' class='page'>
						<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
					</div>
				</div>
			</mtl:contentGroup>
		<% } %>
		</mtl:form>
	</div>
</body>
</mtl:html>
