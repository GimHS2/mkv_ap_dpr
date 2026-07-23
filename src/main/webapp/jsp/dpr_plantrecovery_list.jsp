<%--
	File Name:	dpr_plantrecovery_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/07/30		2.2.1	conditionItemName 조건 수정.
	song7981	2016/02/29		2.2.0	create
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
	<%@ include file="include_rbm_catelink.inc"%>
	<script type='text/javascript'>
		var linkmenu = null;

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,plantCode,itemCode" );
		}

		function downloadReq( anchorObj ) {
			requestDownload( getLocationURL("url"), frmMain.listcheckbox, "organizationCode,plantCode,itemCode", anchorObj );
		}

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlantRecovery?mode=iup";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "organizationCode,plantCode,itemCode" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>")
			windowOpen( url, "sub-content" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode" descriptionKey="FIELD_DPR_ITEM_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemName" descriptionKey="FIELD_DPR_ITEM_MASTER_NAME"/></div>
						<div class='field'><mtl:text id="condition" key="itemName"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/></div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"PT\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="plantCode" descriptionKey="FIELD_DPR_PLANT_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="plantCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									listId="plants" listCodeKey="linkPlantCode" listNameFormat="$S{[:linkPlantCode;$S{] :linkPlantName}}"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_PLANT"/>
						</div>
					</div>
				</div>
			</div>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
				function checkSearchCond() {
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;

					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function readConditionReq( url, type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRPlantRecovery?mode=rtp";
					readPartyAttributeReq( url, type);
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="regist"/>
					<% if( listwriter.containsData() ) { %>
						<mtl:button type="modify"/>
						<mtl:button type="delete"/>
					<% } %>
					<mtl:button type="upload"/>
				<% } %>
				<% if( listwriter.containsData() ) { %>
					<mtl:button type="download"/>
				<% } %>
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>

		</mtl:contentGroup>
		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
