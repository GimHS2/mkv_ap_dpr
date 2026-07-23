<%--
	File Name:	usr_user_select.jsp
	Version:	2.2.3

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2014/03/31		2.2.3	Cross Browsing 적용
	lsinji		2008/09/26		2.2.2	namecls가 "Q"일때도 userId 표시
										DPR Page tail (legal notice) add
	stghr12		2008/03/31		2.2.1	상태/그룹 조건 추가, 사용자ID검색을 언제나 사용할 수 있도록 수정
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										pageindex style 변경 및 "Loading ..." message 처리
										filterValueInput() -> callByKeydown(filterReq)
										listwriter.setMaximumRows(h) -> listwriter.setScrollHeight(h*24)
										listwriter.setPrintingHeader() 사용
										FIELD_USR_USERID -> FIELD_USERID
	stghr12		2006/12/01		2.1.0	com.irt.html.ListWriter, com.irt.html.ColumnListFactory 적용
										sytle 변경: class='userid_s' => class='userid'
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/06/03		1.0.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding="euc-kr" %>
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
	<%@ include file="include_pub_select.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 800 );
			Select.setElementNames( "id", "name" );
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
			<% if( htmlpage.hasManageAuth() ) { %>
				<mtl:select id="request" key="status" prefixKey="USR_USER_STATUS_" codeValues="00,PW,99"
						hasBlank="true" nullValueKey="MSG_USR_STATUS_ALL"
						modified="JavaScript:listLink(this);"/>
			<% } %>
			<% if( pageContext.findAttribute("groups") != null ) { %>
				<mtl:select id="request" key="groupId" listId="groups" listCodeKey="groupId" listNameFormat="$H{groupName}"
						hasBlank="true" nullValueKey="MSG_USR_GROUPID_ALL" modified="JavaScript:listLink(this);"/>
			<% } %>
			</td></tr>
			</table>

			<table class='list_content_top' cellspacing='0' cellpadding='0'>
			<tr><td class='list_content_top' align='right'>
				<span id='list_maxcount'><%= msghandler.getMessage( "jsp.LIST_ALLROWS", "..." ) %></span>
				<span id='list_showcount'></span>
			</td></tr>
			</table>

			<%
				ColumnListFactory factory = new ColumnListFactory( "columnList" );

				if( "Q".equals(request.getParameter("namecls")) ) {
					factory.appendColumn( "userId", "&nbsp;", "class='userid'" );
					factory.setPrimaryFieldKeys( new String[] { "uniqId", "userName" } );
				} else {
					factory.appendColumn( "userId", "&nbsp;", "class='userid'" );
					factory.setPrimaryFieldKeys( new String[] { "userId", "userName" } );
				}
				factory.appendColumn( "userName", "&nbsp;", "class='description'" );

				ListWriter listwriter = new com.irt.custom.SelectListWriter( request, htmlpage, factory.getColumnList() );
				listwriter.setScrollHeight( 288 );
				listwriter.setPrintingHeader( false );
				listwriter.print( out, 3 );
			%>

			<table class='list_content_function' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_list"/>
				<% if( listwriter.containsData() ) { %>
					<mtl:button type="select" styleClass="btn_list"/>
					<% if( "chk".equals(request.getParameter("attr")) ) { %>
						<mtl:button type="reset" href="JavaScript:Select.reset(frmMain.listcheckbox);" styleClass="btn_list"/>
					<% } %>
				<% } %>
				<mtl:button type="close_if" styleClass="btn_list"/>
			</td></tr>
			</table>

			<table class='list_content_bottom' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='list_content_bottom' align='left' nowrap>
					<select name='filterType'>
						<option value='userId'><mtl:message key="FIELD_USERID"/></option>
						<option value='userName' selected><mtl:message key="FIELD_USR_USERNAME"/></option>
					</select>

					<input type='text' name='filterValue' class='length_20' onKeyDown='JavaScript:callByKeydown(filterReq);'>
					<a href='JavaScript:filterReq();'><img src='images/lbtn_filter.gif' class='tbtn'></a>
				</td>
				<td class='list_content_bottom' id='index_lst' align='right' nowrap><mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/></td>
			</tr>
			</table>
		</mtl:contentGroup>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
