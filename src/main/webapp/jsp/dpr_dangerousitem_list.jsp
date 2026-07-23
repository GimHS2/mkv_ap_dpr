<%--
	File Name:	dpr_dangerousitem_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/06/30		2.2.0	create
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
		function bodyLoad(){
			windowResizeTo( 800 );
		}

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "itemCode,plantCode" );
		}

		function downloadReq( anchorObj ) {
			requestDownload( getLocationURL("url"), frmMain.listcheckbox, "itemCode,plantCode", anchorObj );
		}

		function infoReq() {
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "itemCode,plantCode" );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "itemCode,plantCode" );
		}


		function registReq() {
			windowOpen( "<%= htmlpage.getRequestURL() %>?mode=ireg", "sub-content" );
		}

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRDangerousItem?mode=iup";
			windowOpen( url, "sub-content" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
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
						<div class='field-title'><mtl:title key="manualInd" descriptionKey="FIELD_DPR_ITEM_MASTER_PLANT_MANUALIND"/></div>
						<div class='field'>
							<mtl:select id="condition" key="manualInd" prefixKey="DPR_ITEM_MASTER_PLANT_MANUALIND_" codeValues="Y,N"
									hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"/>
						</div>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
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
					disableBlankInput( frmCond, true );
					return submitInput();
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
				<% } %>
					<mtl:button type="download"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="upload"/>
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
