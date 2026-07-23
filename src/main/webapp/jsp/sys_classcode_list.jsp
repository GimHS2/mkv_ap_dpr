<%--
	File Name:	sys_classcode_list.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2008/05/31		2.2.2	download 추가
	stghr12		2008/03/31		2.2.1	table.list_content_top: absolute외 width 추가
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										pageindex style 변경 및 "Loading ..." message 처리
										filterValueInput() -> callByKeydown(filterReq)
	stghr12		2007/04/30		2.1.1	linkmenu 추가
	stghr12		2006/12/01		2.1.0	com.irt.html.ListWriter 적용
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/20		1.0.0	create
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
	<%@ include file="include_rbm_catelink.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		var linkmenu = null;

		function categoryLinkReq( code ) {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?type=<mtl:value id="request" key="type"/>&code="+ encodeURIComponent(code) );
		}

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>?type=<mtl:value id="request" key="type"/>", frmMain.listcheckbox, "code" );
		}

		function downloadAllReq() {
			if( confirm("<mtl:message key="MSG_CONFIRM_DOWNLOAD_ALL" encodeScript="true"/>") )
				windowOpen( "<%= htmlpage.getRequestURL() %>?type=<mtl:value id="request" key="type"/>&mode=down" );
		}

		function infoReq( code ) {
			requestInfo( "<%= htmlpage.getRequestURL() %>?type=<mtl:value id="request" key="type"/>", frmMain.listcheckbox, "code", code );
		}

		function linkReq( code ) {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?type=<mtl:value id="request" key="type"/>&code="+ encodeURIComponent(code) );
		}

		function linkMenuReq( code, lower ) {
			if( !linkmenu ) {
				var idx = 0;
				var menu = new Array;

				menu[idx++] = new Array( '<mtl:message key="jsp.LMENU_INFO" encodeScript="true"/>', 'new', 'JavaScript:linkMenuReqClick("INFO");' );
				<% if( htmlpage.hasManageAuth() ) { %>
					menu[idx++] = new Array( '<mtl:message key="jsp.LMENU_MOD" encodeScript="true"/>', 'new', 'JavaScript:linkMenuReqClick("MOD");' );
				<% } %>
				if( lower ) {
					menu[idx++] = new Array( '<mtl:message key="jsp.LMENU_LOWERLEVEL_LIST" encodeScript="true"/>'
							, 'self', 'JavaScript:linkMenuReqClick("LOWER");' );
				}

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
			else if( menu == "LOWER" )
				categoryLinkReq( linkmenu.params[0] );
		}

		function modifyReq( code ) {
			requestModify( "<%= htmlpage.getRequestURL() %>?type=<mtl:value id="request" key="type"/>", frmMain.listcheckbox, "code", code );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?type=<mtl:value id="request" key="type"/>&mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "code", getQueryValue(location.href, "code") );
			windowOpen( url, "clsMng" );
		}
	</script>
</head>

<body>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px;'>
			<tr><td class='list_content_top'>
				<mtl:categoryLevel format="${[:levelCode;]} $H{name}" categoryCode="<%= request.getParameter("code") %>" level="-1"
						styleClass="cate_link" nullCodeMessageKey="jsp.MSG_CATEGORYLEVEL_NULL" nextCodeMessageKey="jsp.MSG_CATEGORYLEVEL_LOWER"/>
			</td></tr>
			</table>

			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<select name='filterType'>
					<option value='code'><mtl:message key="FIELD_CODE"/></option>
					<option value='name' selected><mtl:message key="FIELD_NAME"/></option>
				</select>

				<input type='text' name='filterValue' class='length_20' onKeyDown='JavaScript:callByKeydown(filterReq);'>
				<a href='JavaScript:filterReq();'><img src='images/lbtn_filter.gif' class='tbtn'></a>
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
				<% } %>
				<% if( htmlpage.hasAuthority("download") ) { %>
					<mtl:button type="download" href="JavaScript:downloadAllReq();" imageSrc="images/btn_down_all.gif" styleClass="btn_list"/>
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
	</mtl:form>
</body>
</mtl:html>
