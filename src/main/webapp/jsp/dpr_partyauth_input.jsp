<%--
	File Name:	dpr_partyauth_input.jsp
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
<%
	java.util.List authOrganizations = (java.util.List)pageContext.findAttribute( "authOrganizations" );
	java.util.List authParties = (java.util.List)pageContext.findAttribute( "authParties" );
	String organizationCodes = null;
	if( authOrganizations != null ) {
		for( java.util.Iterator iterator = authOrganizations.iterator(); iterator.hasNext(); ) {
			java.util.Map<String, Object> map = (java.util.Map<String, Object>)iterator.next();
			if( organizationCodes == null )
				organizationCodes = (String)map.get( "organizationCode" );
			else
				organizationCodes += (";" + (String)map.get("organizationCode") );
		}
	}

	String partyCodes = null;
	if( authParties != null ) {
		for( java.util.Iterator iterator = authParties.iterator(); iterator.hasNext(); ) {
			java.util.Map<String, Object> map = (java.util.Map<String, Object>)iterator.next();

			partyCodes += (";" + (String)map.get("partyCode") );
		}
	}
%>

<mtl:html errorPage="error.jsp">
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_select.inc" %>

	<script type='text/javascript' src='script/tabpane.js'></script>
	<script type='text/javascript'>
		var tabPane;

		function bodyLoad() {
			initTabPaneForm( tabPane = new TabPane(document.all.tabpane_main) );

			windowResizeTo( 900 );
		}


		function deleteOrganization() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyAuth?mode=imod";

			var selectedValue = CheckBox.getValues( frmMain.listcheckbox );
			var orginalOrganizationCode = getOrganizationCodes( "<%= organizationCodes %>" );
			if( orginalOrganizationCode ) {
				if( selectedValue[0] ) {
					for( var i = 0; i < orginalOrganizationCode.length; i++ ) {

						for( var j = 0; j < selectedValue.length; j++ ) {
							if( orginalOrganizationCode[i] != selectedValue[j] ) {
								url += "&"+ "organizationCode="+ encodeURIComponent( orginalOrganizationCode[i] );
							} else
								break;
						}
					}
				} else
					for( var i = 0; i < orginalOrganizationCode.length; i++ )
						url += "&"+ "organizationCode="+ encodeURIComponent( organizationCode );
			}

			windowSelfOpen( url );
		}

		function deleteParty() {
		}

		function getOrganizationCodes( organizationCode ) {
			var values = "";

			if( organizationCode )
				for( var i = 0; i < organizationCode.length; i++ )
					values += ("&" + "organizationCode=" + encodeURIComponent(organizationCode[i]) );

			var originalOrganizationCode = "<%= organizationCodes %>";
			var originalOrganizationCodes;
			if( originalOrganizationCode != "null" ) {
				originalOrganizationCodes =  originalOrganizationCode.split( ";" );

				for( var j = 0; j < originalOrganizationCodes.length; j++ ) {
					values += ("&" + "organizationCode=" + encodeURIComponent(originalOrganizationCodes[j]) );
				}
			}

			return values.substring(1);
		}

		function getPartyCode( partyCode ) {
			var values = "";

			if( partyCode )
				for( var i = 0; i < partyCode.length; i++ )
					values += ("&" + "partyCode=" + encodeURIComponent(partyCode[i]) );

			var originalPartyCode = "<%= partyCodes %>";
			var originalPartyCodes;
			if( originalPartyCode != "null" ) {
				originalPartyCodes =  originalPartyCode.split( ";" );

				for( var j = 0; j < originalPartyCodes.length; j++ ) {
					values += ("&" + "partyCode=" + encodeURIComponent(originalPartyCodes[j]) );
				}
			}

			return values.substring(1);
		}

		function next() {
			var url = "<%= htmlpage.getRequestURL() %>"
		}

		function registOrganization() {
			var originalOrganizationCode = "<%= organizationCodes %>";
			if( originalOrganizationCode != "null" )
				selectDPRPartyMaster( "organization", "P,SO", "chk", originalOrganizationCode.split(";") );
			else
				selectDPRPartyMaster( "organization", "P,SO", "chk" );
		}

		function registParty() {
			var originalOrganizationCode = "<%= organizationCodes %>";
			if( originalOrganizationCode != "null" )
				selectDPRParty( "party", "D", "chk", null, originalOrganizationCode.split(";") );
			else
				selectDPRParty( "party", "D", "chk" );
		}

		function setOrganization( organizationCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyAuth?mode=imod";
			
			var values = getOrganizationCodes( organizationCode );
			if( values )
				url += "&" + values;

			windowSelfOpen( url );
		}

		function setParty( partyCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyAuth?mode=imod";
			
			var organizationValues = getOrganizationCodes();
			if( organizationValues )
				url += "&" + organizationValues;

			var partyValues = getPartyCodes( partyCode );
			if( partyValues )
				url += "&" + partyValues;

			windowSelfOpen( url );
		}
	</script>
</head>

<body>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_pub_list.inc" %>

	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return false;">

		<mtl:contentGroup groupId="main" type="tabpane" styleClass="tabpane">
<%-- 1. Organization --%>
		<mtl:contentGroup groupId="base" type="tabpage" descriptionKey="jsp.dpr_partyauth_input.MSG_ORGANIZATION">
			<mtl:contentGroup groupId="list" type="list">
				<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px;'>
				<tr><td class='list_content_top' align='left'>
				</td></tr>
				</table>
					<%
						ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage,(com.irt.data.cols.ColumnList)request.getAttribute("columnList_org"), "authOrganizations" );
						listWriter.print( out );
					%>

				<table class='btn_page' cellspacing='0' cellpadding='0'>
				<tr><td>
					<% if( htmlpage.hasManageAuth() ) { %>
						<mtl:button type="regist" href="javaScript:registOrganization();" styleClass="btn_page"/>
						<mtl:button type="delete" href="javaScript:deleteOrganization();" styleClass="btn_page"/>
					<% } %>
				</td></tr>
				</table>
			</mtl:contentGroup>

			<mtl:contentGroup groupId="base0" type="fieldset" descriptionKey="jsp.dpr_partyauth_input.AUTHORIZATIONTYPE">
	            <table class='line_content' cellspacing='0' cellpadding='0'>
	            <tr>
	                <td class='subject'><mtl:message key="jsp.drp_partyauth_input.APLLY_LOWERPARTY"/></td>
	                <td class='content1'><mtl:ynInput id="record" key="applyLowerParty" defaultValue="N"/>
	            </tr>
	            </table>
			</mtl:contentGroup>

		</mtl:contentGroup>

<%-- 2. Party --%>
	<% if( authOrganizations != null ) { %>
		<mtl:contentGroup groupId="uom" type="tabpage" descriptionKey="jsp.dpr_partyauth_input.MSG_PARTY">
			<mtl:contentGroup groupId="list" type="list">
				<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px;'>
				<tr><td class='list_content_top' align='left'>
				</td></tr>
				</table>
					<%
						ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage,(com.irt.data.cols.ColumnList)request.getAttribute("columnList_pty"), "authParties" );
						listWriter.print( out );
					%>

				<table class='btn_page' cellspacing='0' cellpadding='0'>
				<tr><td>
					<% if( htmlpage.hasManageAuth() ) { %>
						<mtl:button type="regist" href="javaScript:registParty();" styleClass="btn_page"/>
						<mtl:button type="delete" href="javaScript:deleteParty();" styleClass="btn_page"/>
					<% } %>
				</td></tr>
				</table>
			</mtl:contentGroup>
		</mtl:contentGroup>
	<% } %>

		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return" styleClass="btn_page"/>
			<mtl:button type="process" href="JavaScript:next();" imageSrc="images/btn_next.gif" styleClass="btn_page"/>
			<mtl:button type="close_if" styleClass="btn_page"/>
		</td></tr>
		</table>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
