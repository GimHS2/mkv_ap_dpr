<%--
	File Name:	dpr_party_list.jsp
	Version:	2.2.3

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/04/30		2.2.3	DPRParty select()->cond() mode 변경
	jbaek		2015/04/30		2.2.2	CrossBrowsing 적용: subject에 유동 width적용.
	jbaek		2014/03/31		2.2.1	CrossBrowsing 적용
	lsinji		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.List, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	java.util.Map conditionMap = (java.util.Map)pageContext.findAttribute( "condition" );
	boolean useDivision = (com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;division") == null);
	boolean useDistributionChannel = (com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;distributionChannel") == null);
	boolean readOnly = com.irt.rbm.SessionMng.GROUPCLASS_ORDER.equals(sessionMng.getGroupClass());
	String organizationCode = (String)conditionMap.get("organizationCode");
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;

		function deleteJDMS() {
			requestJDMSManage( "del" );
		}

		function linkListReq( partyCode, divisionCode, distributionChannelCode, organizationCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyLink?mode=list";
			url = replaceQueryValue( url, "partyCode", encodeURIComponent(partyCode) );
			url = replaceQueryValue( url, "divisionCode", encodeURIComponent(divisionCode) );
			url = replaceQueryValue( url, "distributionChannelCode", encodeURIComponent(distributionChannelCode) );
			url = replaceQueryValue( url, "organizationCode", encodeURIComponent(organizationCode) );

			windowOpen( url +"&wintype=sub", "sub-content" );
		}

		function infoReq( partyCode, divisionCode, distributionChannelCode, organizationCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=info";
			url = replaceQueryValue( url, "partyCode", encodeURIComponent(partyCode) );
			url = replaceQueryValue( url, "divisionCode", encodeURIComponent(divisionCode) );
			url = replaceQueryValue( url, "distributionChannelCode", encodeURIComponent(distributionChannelCode) );
			url = replaceQueryValue( url, "organizationCode", encodeURIComponent(organizationCode) );

			windowOpen( url +"&wintype=sub", "sub-content" );
		}

		function linkMenuReq( partyCode, organizationCode ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_DPR_PARTY_INFO" encodeScript="true"/>'
						, 'new', 'JavaScript:linkMenuReqClick("INFO");' );

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "INFO" ) {
				infoReq( linkmenu.params[0], linkmenu.params[1], linkmenu.params[2], linkmenu.params[3] );
			} else if( menu == "LINK_LIST" ) {
				linkListReq( linkmenu.params[0], linkmenu.params[1], linkmenu.params[2], linkmenu.params[3] );
			}
		}

		function registJDMS() {
			requestJDMSManage( "reg" );
		}

		function requestJDMSManage( mode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyJDMS";
			url = getRequestMultiURL( url, mode, frmMain.listcheckbox, "partyCode,organizationCode,distributionChannelCode,divisionCode" );
			if( !url ) return;

			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
			if( checkURLLength(url) ) {
				if( mode == "reg" ) {
					customPopup.confirm( { "detail" : "<mtl:message key="MSG_CONFIRM_REGIST" encodeScript="true"/>" }, function(res) {
						if( res ) {
							location.replace( url );
						}
					});
				} else if( mode == "del" ) {
					customPopup.confirm( { "detail" : "<mtl:message key="MSG_CONFIRM_DELETE" encodeScript="true"/>" }, function(res) {
						if( res ) {
							location.replace( url );
						}
					});
				}
			}
		}

	<% if( sessionMng.isSystemAdmin() ) { %>
		function userPartyReq() {
			var url = "<%= systemConfig.getClassURL() %>/USRParty?mode=list";
			windowOpen( url );
		}
	<% } %>
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_dpr_party_cond.inc" %>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>
			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				if( !htmlpage.hasManageAuth() || !com.irt.dpr.Country.isFeature(organizationCode, "useJdms") )
					listwriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_NUMBER );

				if( property.containsKey("listmsg") )
					listwriter.print( out, property.getProperty("listmsg") );
				else
					listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() && listwriter.containsData() && com.irt.dpr.Country.isFeature(organizationCode, "useJdms") ) { %>
					<mtl:button type="button" onClick="JavaScript:registJDMS();" messageKey="jsp.BTN_JDMS_REGIST"/>
					<mtl:button type="button" onClick="JavaScript:deleteJDMS();" messageKey="jsp.BTN_JDMS_DELETE"/>
				<% } %>
					<mtl:button type="close_if"/>
				<% if( sessionMng.isSystemAdmin() ) { %>
					<mtl:button type="button" onClick="JavaScript: userPartyReq();" messageKey="TITLE_USR_PARTY_"/>
				<% } %>
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
