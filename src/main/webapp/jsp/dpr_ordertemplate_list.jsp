<%--
	File Name:	dpr_order_template_list.jsp
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
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var windowWidth = 920;
		var linkmenu = null;
		function registReq( orderKey ) {
			var url = "<%= systemConfig.getClassURL() %>/DPROrderTemplate?mode=ireg";

			if( orderKey )
				url += "&orderKey=" + encodeURIComponent(orderKey);
			else {
				alert( "" );
			}

			windowOpen( url +"&wintype=sub" );
		}

		function infoReq( templateKey ) {
			var url = "<%= systemConfig.getClassURL() %>/DPROrderTemplate?mode=info";

			if( templateKey )
				url += "&templateKey=" + encodeURIComponent(templateKey);

			windowSelfOpen( url, getLocationURL() );
		}

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>?", frmMain.listcheckbox, "templateKey" );
		}

		function linkItemReq( templateKey ) {
			var url = "<%= systemConfig.getClassURL() %>/DPROrderTemplateItem?mode=list";

			if( templateKey )
				url += "&templateKey=" + encodeURIComponent(templateKey);
			else {
				alert( "" );
			}
			windowSelfOpen( url, getLocationURL() );
		}

		function modifyReq( templateKey ) {
			requestModify( "<%= systemConfig.getClassURL() %>/DPROrderTemplate", frmMain.listcheckbox, "templateKey", templateKey, "_self" );
		}

		function linkMenuReq( templateKey ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_ORDER_TEMPLATE_INFO" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("INFO");' );
				menu[1] = new Array( '<mtl:message key="jsp.LMENU_DPR_ORDER_TEMPLATE_MODIFY" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("MODIFY");' );
				menu[2] = new Array( '<mtl:message key="jsp.LMENU_DPR_ORDER_TEMPLATE_ITEM_LIST" encodeScript="true"/>'
						, 'self', 'JavaScript:linkMenuReqClick("ITEM_LIST");' );

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "INFO" )
				infoReq( linkmenu.params[0] );
			else if( menu == "MODIFY" )
				modifyReq( linkmenu.params[0] );
			else if( menu == "ITEM_LIST" )
				linkItemReq( linkmenu.params[0] );
		}

		function templateToOrder(){
			var values = CheckBox.getValues( frmMain.listcheckbox );
			if( values == null || values.length > 1 ) {
				customPopup.alert( { "header" : "<mtl:message key="MSG_CHOOSE_ONLY_ONE" encodeScript="true"/>" } );
				return;
			}

			var templateKey = values[0];
			if( templateKey ){
				parent.main_content.loadTemplate( templateKey );
				//windowClose( false );
				parent.$(".sub-content-wrap").hide();
			}
			else{
				customPopup.alert( { "header" : "not Choose template" } );
			}
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_rbm_bodyheader.inc" %>

		<mtl:form name="frmHeader" method="get" onSubmit="JavaScript: return submitInput();">
			<%@ include file="include_rbm_form.inc" %>
			<%@ include file="include_pub_input.inc" %>
			<%@ include file="include_pub_calendar.inc" %>
			<mtl:contentGroup groupId="condition" type="search" styleClass="search_content none-card" style="margin-bottom: 15px; margin-top: 20px;">
				<div class='search-table'>
					<div class='row'>
						<div class='cell' style='padding-bottom: 10px;'>
							<div class='field-title'><mtl:message key="FIELD_DPR_TEMPLATE_NAME"/></div>
							<div class='field'><mtl:text id="condition" key="templateName"/></div>
						</div>
						<div class='cell' style='padding-bottom: 10px;'>
							<div class='field-title'><mtl:message key="FIELD_DPR_TEMPLATE_USERID"/></div>
							<div class='field'><mtl:text id="condition" key="userId"/></div>
						</div>
					</div>
				</div>
				<div class='search-bottom'>
					<div class='table-cell search-button'>
						<mtl:button type="reset" styleClass="seccondary-w135"/>
						<mtl:button type="search" styleClass="primary-w135"/>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:form>

		<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
			<mtl:contentGroup groupId="list" type="list" styleClass="list_content none-card">
				<div class='list-menu' style='text-align: right;'>
					<span style='font-size: 12px; font-weight: bold; margin-right: 8px;'><mtl:message key="jsp.SHOWCOUNT"/></span><mtl:showcount modified="JavaScript:changeShowCount(this);" customOption="smallSelectmenuOptions"/>
				</div>

				<%
					ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
					listwriter.print( out );
				%>

				<div class='list-function'>
					<div class='button'>
						<mtl:button type="return"/>
					<% if( htmlpage.hasManageAuth() && listwriter.containsData() ) { %>
						<mtl:button type="button" onClick="JavaScript: templateToOrder();"
								icon="images/ico_load_white.png" styleClass="primary" messageKey="jsp.BTN_LOAD"/>
						<mtl:button type="delete"/>
						<mtl:button type="modify"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
					<div id='list_page' class='page'>
						<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:form>
	</div>
</body>
</mtl:html>


