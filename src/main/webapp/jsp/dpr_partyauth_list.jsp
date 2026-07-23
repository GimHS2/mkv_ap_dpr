<%--
	File Name:	dpr_partyauth_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/12/31		2.2.1	multi-sold-to 기능 추가
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
<%
	boolean multiSoldTo = com.irt.rbm.RBMSystem.getSystemEnvBool( "DPR", (sessionMng.getPartyId()+";multiSoldTo"), "JNJAP_CN".equals(sessionMng.getPartyId()) );
	java.util.Map conditionMap = (java.util.Map)pageContext.findAttribute( "condition" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;

		$(function() {
			var options = $.extend( {}, selectmenuOptions, { width: "auto" } );
			$("select[name=filterType]").singleSelectmenu( options );
		});

		function registReq() {
			var url = getLocationURL( "url" );

			url = replaceQueryValue( url, "uniqId", "<mtl:value id="request" key="uniqId"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="request" key="distributionChannelCode"/>" );

			var selected = CheckBox.getValues( frmMain.listcheckbox );
			var userGroupClass = "<%= property.getProperty("userGroupClass") %>";
			if( userGroupClass == "OR" && "<%= multiSoldTo %>" == "false" ) {
				var selected = CheckBox.getValues( frmMain.listcheckbox );
				if( selected == null || selected.length > 1 ) {
					customPopup.alert( { "header" : "<mtl:message key="MSG_CHOOSE_ONLY_ONE" encodeScript="true"/>" } );

					return;
				}
			}

			var query = CheckBox.getValues( frmMain.listcheckbox );
			if( query ) {
				url = getRequestMultiURL( url, "reg", frmMain.listcheckbox, "listOrganizationCode,partyCode" );
				if( url && checkURLLength(url) ) {
					customPopup.confirm( { "detail" : "<mtl:message key="MSG_CONFIRM_REGIST" encodeScript="true"/>" }, function(res) {
						if( res ) {
							windowSelfOpen( url, getLocationURL() );
						}
					});
				}
			} else {
				customPopup.confirm( { "detail" : "<mtl:message key="MSG_DPR_CONFIRM_REGIST_ALL" encodeScript="true"/>" }, function(res) {
					if( res ) {
						var organizationCode = frmMain.organizationCode.value;
						if( !organizationCode ) {
							if( !organizationCode ) {
								customPopup.alert( { "header" : "<mtl:message key="MSG_MUST_CHOICE_ORGANIZATION" encodeScript="true"/>" } );

								return;
							}
						}

						url = replaceQueryValue( url, "mode", "reg" );
						url = replaceQueryValue( url, "organizationCode", organizationCode );
						windowSelfOpen( url, getLocationURL() );
					}
				});
			}
		}

		function deleteReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPartyAuth";
			url = replaceQueryValue( url, "uniqId", "<mtl:value id="request" key="uniqId"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="request" key="distributionChannelCode"/>" );

			requestDelete( url, frmMain.listcheckbox, "listOrganizationCode,partyCode" );
		}

		function infoReq( partyCode, organizationCode ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=info";
			url += "&partyCode=" + encodeURIComponent(partyCode);
			url += "&organizationCode=" + encodeURIComponent(organizationCode);

			windowOpen( url +"&wintype=sub", "clsMng" );
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
				infoReq( linkmenu.params[0], linkmenu.params[1] );
			}
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contains id="record_usr" copyId="_record">
			<mtl:contentGroup groupId="condition" type="search">
				<div id='messagebar'></div>
				<%@ include file="include_usr_user_info.inc" %>
			</mtl:contentGroup>
		</mtl:contains>

		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>
			<div class='list-menu'>
				<mtl:select id="condition" key="organizationCode"
						listId="organizations" listCodeKey="organizationCode"
						listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="JavaScript:listLink(this);"
						searchable="false" width="auto"/>
				<mtl:select id="condition" key="distributionChannelCode"
						listId="distributionChannels" listCodeKey="distributionChannelCode"
						listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="JavaScript:listLink(this);"
						searchable="false" width="auto"/>
				<mtl:select id="condition" key="linkSource" defaultValue="A"
						hasBlank="false" prefixKey="MSG_DPR_PARTY_AUTH_SOURCE_" codeValues="A,S,D"
						modified="JavaScript:listLink(this);" searchable="false" width="auto"/>
				<mtl:select id="condition" key="authIndicator" nullValueKey="jsp.dpr_partyauth_list.AUTH_INDICATOR_NULL"
						hasBlank="true" prefixKey="jsp.dpr_partyauth_list.AUTH_INDICATOR_" codeValues="Y,N"
						modified="JavaScript:listLink(this);" searchable="false" width="auto"/>
				<mtl:select id="condition" key="partyStatus" nullValueKey="MSG_DPR_PARTY_STATUS_"
						hasBlank="true" prefixKey="MSG_DPR_PARTY_STATUS_" codeValues="00,99,XX"
						modified="JavaScript:listLink(this);" searchable="false" width="auto"/>
			</div>

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );
				if( property.containsKey("listmsg") )
					listWriter.print( out, property.getProperty("listmsg") );
				else
					listWriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( listWriter.containsData() ) { %>
					<% if( conditionMap.get("authIndicator") == null ) { %>
						<mtl:button type="regist"/>
						<mtl:button type="delete"/>
					<% } else if(  "N".equals(conditionMap.get("authIndicator")) ) { %>
						<mtl:button type="regist"/>
					<% } else if( "Y".equals(conditionMap.get("authIndicator")) ) { %>
						<mtl:button type="delete"/>
					<% } %>
				<% } %>
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>
			<div class='list-function'>
				<div class='button'>
					<select name='filterType'>
						<option value='partyCode'><mtl:message key="FIELD_DPR_PARTY_CODE"/></option>
						<option value='partyName'><mtl:message key="FIELD_DPR_PARTY_SALES_PARTYNAME"/></option>
					</select>
					<input type='text' name='filterValue' class='input-field' style='width: 200px' onKeyDown='JavaScript:callByKeydown(filterReq);'>
					<mtl:button type="search" styleClass="btn btn-secondary" icon="images/ico_search.png" onClick="JavaScript:filterReq();"/>
				</div>
			</div>
		</mtl:contentGroup>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
