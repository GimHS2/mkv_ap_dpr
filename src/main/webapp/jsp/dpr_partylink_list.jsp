<%--
	File Name:	dpr_partylink_list.jsp
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
		linkmenu = null;

		function bodyLoad() {
			windowResizeTo( 800 );
		}

		function infoReq( partyCode, isOpener ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRParty?mode=info&partyCode=" + partyCode;
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			url = replaceQueryValue( url, "divisionCode", "<mtl:value id="condition" key="divisionCode"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="condition" key="distributionChannelCode"/>" );

			var parentWindowType;
			if( opener.frmMain.wintype ) {
				var parentWindowType = opener.frmMain.wintype.value;
			}

			if( parentWindowType == "sub" ) {
				url = attachDefaultParameter( url );

				opener.windowSelfOpen( url, opener.getLocationURL() );
				windowClose( false );
			} else {
				windowSelfOpen( url, getLocationURL() );
			}
		}

		function linkMenuReq( partyCode ) {
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
				infoReq( linkmenu.params[0] );
			}
		}
	</script>
</head>

<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:hidden id="condition" key="organizationCode"/>
		<mtl:hidden id="condition" key="distributionChannelCode"/>
		<mtl:hidden id="condition" key="divisionCode"/>

		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<table class='search_content' cellspacing='0' cellpadding='0'>
				<tr><td>
					<table class='search_line_content' cellspacing='0' cellpadding='0'>
					<tr>
						<td class='search_subject' width='65'><mtl:message key="FIELD_DPR_PARTY_CODE"/>
						<td class='search_content'>
							<mtl:text id="condition" key="partyCode" styleClass="length_15" readonly="true"/>
						</td>

						<td class='search_subject'><mtl:message key="FIELD_DPR_PARTYLINK_LINKPARTYCODE"/>
						<td class='search_content'>
							<mtl:text id="condition" key="linkPartyCode" styleClass="length_15"/>
						</td>
					</tr>
					</table>

					<table class='search_line_content' cellspacing='0' cellpadding='0'>
					<tr>
						<td class='search_subject'><mtl:message key="FIELD_DPR_PARTYLINK_LINKPARTYCODE"/>
						<td class='search_content'>
							<mtl:select id="condition" key="linkType" nullValueKey="jsp.MSG_DPR_PARTYLINK_TYPE" hasBlank="true"
									prefixKey="DPR_PARTY_LINKTYPE_" codeValues="AG,WE,RE,RG"/>
						</td>
					</tr>
					</table>
				</td><td class='search_button'>
					<mtl:button type="submit" imageSrc="images/btn_search.gif" styleClass="btn_list"/>
					<mtl:button type="reset" href="JavaScript:resetSearchCond();" styleClass="btn_list"/>
				</td></tr>
			</table>

			<script type='text/javaScript'>
				function checkSearchCond() {
					return submitInput();
				}

				function initSearchCond() {
					initConditionField( new Array(
						frmCond.linkPartyCode
					), Field.modified );
				}
				attachWindowEvent( "load", initSearchCond );

				function resetSearchCond() {
					frmCond.reset();
					resetForm( frmCond );
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px'>
			<tr><td class='list_content_top'>
			</td></tr>
			</table>

			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
				<span id='list_showcount'><mtl:message key="jsp.SHOWCOUNT"/> <mtl:showcount modified="JavaScript:changeShowCount(this);"/></span>
			<td></tr>
			</table>

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );
				listWriter.print( out );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_list"/>
				<mtl:button type="close_if" styleClass="btn_list"/>
			</td></tr>
			</table>

			<table class='list_content_bottom' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='list_content_bottom' align='left' nowrap>
				<td>
				<td class='list_content_bottom' id='index_lst' align='center' width='100%' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
				<td class='list_content_bottom' id='index_btn' align='right' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
			</tr>
			</table>
		</mtl:contentGroup>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>


