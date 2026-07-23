<%--
	File Name:	dpr_country_list.jsp
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
		var linkmenu = null;

		function infoReq( countryCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRCountry?mode=info";

			if( countryCode )
				url += "&countryCode=" + encodeURIComponent(countryCode);

			windowOpen( url +"&wintype=sub", "sub-content" );
		}

		function listReq( countryCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=list";
			url = replaceQueryValue( url, "countryCode", countryCode );

			windowSelfOpen( url, getLocationURL() );
		}

		function modifyReq( countryCode ) {
			requestModify( "<%= systemConfig.getClassURL() %>/DPRCountry", frmMain.listcheckbox, "countryCode", countryCode );
		}

		function registReq( countryCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRCountry?mode=ireg";
			if( countryCode )
				url += "&countryCode=" + encodeURIComponent(countryCode);

			windowOpen( url +"&wintype=sub", "sub-content" );
		}

		function linkMenuReq( countryCode ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_COUNTRY_INFO" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("INFO");' );
				menu[1] = new Array( '<mtl:message key="jsp.LMENU_DPR_COUNTRY_MODIFY" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("MODIFY");' );
				menu[2] = new Array( '<mtl:message key="jsp.LMENU_DPR_COUNTRY_CONDITION_LIST" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("COND_LIST");' );

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
			else if( menu == "COND_LIST" )
				setCondition( linkmenu.params[0] );
			else if( menu == "LOWERPARTY_LIST" )
				listReq( linkmenu.params[0] );
		}

		function setCondition( countryCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRCountryCondition?mode=list";
			requestOne( url, frmMain.listcheckbox, "countryCode", countryCode );
		}

		function setManager( countryCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRCountryAuth?mode=list";
			requestOne( url, frmMain.listcheckbox, "countryCode", countryCode );
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
						<div class='field-title'><mtl:message key="FIELD_DPR_COUNTRY_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="countryCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:message key="FIELD_DPR_COUNTRY_NAME"/></div>
						<div class='field'>
							<mtl:hidden key="countryName_type" defaultValue="CTS"/>
							<mtl:text id="condition" key="countryName"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:message key="FIELD_DPR_SALESORGANIZATION_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasBlank="true" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}"/>
						</div>
					</div>
					<div class='cell'>
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
					return submitInput();
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );
				listWriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<% if( com.irt.rbm.RBMSystem.getSystemEnvBool("DPR", "Country;AuthRegister", false) ) { %>
						<mtl:button type="regist"/>
					<% } %>
					<% if( listWriter.containsData() ) { %>
						<mtl:button type="modify"/>
						<mtl:button type="button" onClick="JavaScript:setCondition();" messageKey="jsp.BTN_COUNTRY_ORG_SETTING"/>
					<% } %>
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


