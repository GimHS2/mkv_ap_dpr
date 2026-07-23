<%--
	File Name:	pub_code_list.jsp
	Version:	2.2.2

	Description:
		property.getProperty()
			"useFilter"

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.2	DPR용 Page tail (legal notice) 추가
	stghr12		2008/05/31		2.2.1	download 추가
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										pageindex style 변경 및 "Loading ..." message 처리
										filterValueInput() -> callByKeydown(filterReq)
	stghr12		2007/04/30		2.1.1	linkmenu 추가
	stghr12		2006/12/01		2.1.0	com.irt.html.ListWriter 적용
										property.getProperty("useFilter") 사용
	stghr12		2006/02/28		2.0.0	create
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
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "code" );
		}

		function downloadReq() {
			if( confirm("<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>") )
				windowOpen( replaceQueryValue( getLocationURL("url"), "mode", "down" ) );
		}

		function infoReq( code ) {
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "code", code );
		}

		function linkMenuReq( code ) {
			if( !linkmenu ) {
				var menu = new Array;

				menu[0] = new Array( '<mtl:message key="jsp.LMENU_INFO" encodeScript="true"/>', 'new', 'JavaScript:linkMenuReqClick("INFO");' );
				<% if( htmlpage.hasManageAuth() ) { %>
					menu[1] = new Array( '<mtl:message key="jsp.LMENU_MOD" encodeScript="true"/>', 'new', 'JavaScript:linkMenuReqClick("MOD");' );
				<% } %>

				linkmenu = createLinkMenu( menu );
			}
			linkmenu.show();
			linkmenu.params = linkMenuReq.arguments;
		}

		function linkMenuReqClick( menu ) {
			if( menu == "INFO" )
				infoReq( linkmenu.params[0] );
			else if( menu == "MOD" )
				modifyReq( linkmenu.params[0] );
		}

		function modifyReq( code ) {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "code", code );
		}

		function registReq() {
			windowOpen( "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub", "clsMng" );
		}
	</script>
</head>

<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<% if( !"N".equals(property.getProperty("useFilter")) ) { %>
					<select name='filterType'>
						<option value='code'><mtl:message key="FIELD_CODE"/></option>
						<option value='name' selected><mtl:message key="FIELD_NAME"/></option>
					</select>

					<input type='text' name='filterValue' class='length_20' onKeyDown='JavaScript:callByKeydown(filterReq);'>
					<a href='JavaScript:filterReq();'><img src='images/lbtn_filter.gif' class='tbtn'></a>
				<% } %>
			</td></tr>
			</table>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				listwriter.print( out );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_list"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="regist" styleClass="btn_list"/>
					<% if( listwriter.containsData() ) { %>
						<mtl:button type="modify" styleClass="btn_list"/>
						<mtl:button type="delete" styleClass="btn_list"/>
					<% } %>
					<% if( htmlpage.hasAuthority("download") ) { %>
						<mtl:button type="download" styleClass="btn_list"/>
					<% } %>
				<% } %>
				<mtl:button type="close_if" styleClass="btn_list"/>
			</td></tr>
			</table>

			<table class='list_content_bottom' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='list_content_bottom' align='left' nowrap>
					<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
					<span id='list_showcount'><mtl:message key="jsp.SHOWCOUNT"/> <mtl:showcount modified="JavaScript:changeShowCount(this);"/></span>
				</td>
				<td class='list_content_bottom' id='index_lst' align='center' width='100%' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
				<td class='list_content_bottom' id='index_btn' align='right' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
			</tr>
			</table>
		</mtl:contentGroup>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
