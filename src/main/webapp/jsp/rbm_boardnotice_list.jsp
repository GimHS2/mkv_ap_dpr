<%--
	File Name:	rbm_boardnotice_list.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.2	DPR Page tail (legal notice) add
	stghr12		2008/03/31		2.2.1	table.list_content_top: absolute외 width 추가
										"boardclss" 처리
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										pageindex style 변경 및 "Loading ..." message 처리
										filterValueInput() -> callByKeydown(filterReq)
	stghr12		2006/12/01		2.1.0	com.irt.html.ListWriter 적용
										ColumnListWrapper사용 삭제: $f{decode(..)}로 처리
	stghr12		2006/02/28		2.0.0	version up
	stghr12		2004/11/22		1.0.0	create
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
	String boardClassCode = property.getProperty( "boardClassCode" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<script type='text/javascript'>
		$(function() {
			var options = $.extend( {}, selectmenuOptions, { width: "auto" } );
			$("select[name=filterType]").singleSelectmenu( options );
		});

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>", frmMain.listcheckbox, "boardNumber" );
		}

		function infoReq( boardNumber ) {
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>"
			requestInfo( url, frmMain.listcheckbox, "boardNumber", boardNumber, "_self" );
		}

		function modifyReq( boardNumber ) {
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>";
			requestModify( url, frmMain.listcheckbox, "boardNumber", boardNumber, "_self" );
		}

		function registReq() {
			windowSelfOpen( "<%= htmlpage.getRequestURL() %>?boardClassCode=<%= boardClassCode %>&mode=ireg", getLocationURL() );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<div id='messagebar'></div>
			<div class='list-menu'>
				<mtl:contains id="boardclss">
					<mtl:select id="property" key="boardClassCode" listId="boardclss" listCodeKey="boardClassCode" listNameFormat="$H{boardClassName}"
							modified="JavaScript:listLink(this);" searchable="false" width="auto"/>
				</mtl:contains>
			<% if( sessionMng.isAuthorized("RBM", "RBMBoard."+ boardClassCode +".DEL") ) { %>
				<mtl:select id="request" key="noticeManage" prefixKey="jsp.rbm_boardnotice_VIEW_" codeValues="ALL,OG"
						hasBlank="true" nullValueKey="jsp.rbm_boardnotice_VIEW_" searchable="false" width="auto"
						modified="JavaScript:listLink(this);"/>
			<% } %>
			</div>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );

				if( sessionMng.isAuthorized("RBM", "RBMBoard."+ boardClassCode +".DEL") )
					listwriter.setCheckboxType( ListWriter.CHECKBOXTYPE_CHECK );
				listwriter.setNumbering( false );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( sessionMng.isAuthorized("RBM", "RBMBoard."+ boardClassCode +".MNG") ) { %>
					<mtl:button type="regist"/>
					<% if( listwriter.containsData() ) { %>
						<mtl:button type="modify"/>
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
						<option value='title'><mtl:message key="FIELD_RBM_BOARD_TITLE"/></option>
						<option value='registUserName'><mtl:message key="FIELD_RBM_BOARD_REGISTUSERNAME"/></option>
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
