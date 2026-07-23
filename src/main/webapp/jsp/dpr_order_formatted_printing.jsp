<%--
	File Name:	dpr_order_formatted_printing.jsp.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2009/01/09		2.2.0	create
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
	Map<String, Object> condition = (Map<String, Object>)pageContext.findAttribute( "condition" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 1024 );
		}
	</script>
</head>

<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contains id="header">
			<mtl:contentGroup groupId="headerInfo" type="content" descriptionKey="jsp.dpr_order_info.GRP_HEADER">
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:message key="jsp.dpr_order_input.FIELD_ORDER_NUMBER"/></td>
					<td class='content2'><mtl:value id="header" key="orderNumber"/></td>
					<td class='subject'><mtl:message key="FIELD_DPR_ORDER_CONFIRMINDATE"/></td>
					<td class='content2'><mtl:value id="header" key="inDateConfirm"/></td>
				</tr>
				</table>

				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:message key="FIELD_DPR_ORDER_SOLDPARTYCODE"/></td>
					<td class='content2'><mtl:valuef id="header" format="[$f{pure(soldPartyCode)}] ${soldPartyName}"/></td>
					<td class='subject'><mtl:message key="FIELD_DPR_ORDER_SHIPPARTYCODE"/></td>
					<td class='content2'><mtl:valuef id="header" format="[$f{pure(shipPartyCode)}] ${shipPartyName}"/></td>
				</tr>
				</table>
			</mtl:contentGroup>
		</mtl:contains>

		<mtl:contentGroup groupId="list" type="list">
			<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px'>
			<tr><td class='list_content_top'>
				<mtl:select id="request" key="ftype" prefixKey="jsp.dpr_order_input.MSG_FORMATTYPE_"
						codeValues="PC,DZ" modified="JavaScript:tabLink(this, \"detail\", null, \"sort\");"/>
			</td></tr>
			</table>

			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
				<span id='list_showcount'><mtl:message key="jsp.SHOWCOUNT"/>
			<td></tr>
			</table>

			<%
				ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage, "details" );
				listWriter.setUseVerticalScroll( false );
				listWriter.setScrollHeight( 900, false );
				listWriter.setNumbering( false );
				listWriter.setSortable( false );
				listWriter.print( out );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_list"/>
				<% if( listWriter.containsData() ) { %>
				<% } %>
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


