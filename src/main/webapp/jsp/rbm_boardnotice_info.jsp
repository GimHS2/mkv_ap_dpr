<%--
	File Name:	rbm_boardnotice_info.jsp
	Version:	2.2.3

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.3	DPR Page tail (legal notice) add
	stghr12		2008/05/31		2.2.2	deleteReq(): backURL 버그 수정
	stghr12		2008/03/31		2.2.1	boardOption -> boardOption1
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										windowResizeTo( 650 ); -> windowResizeTo( 800 );
										deleteReq(): attachDefaultParameter() 추가
	GimHS		2007/04/30		2.1.1	BOARDOPTION 추가
	stghr12		2006/12/01		2.1.0	encodeScript(mtl:message) 처리, style 변경
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
<head>
	<%@ include file="include_rbm_header.inc" %>
	<style type='text/css'><!--
		div.notice_content {
			background-color: #FFFFFF;
			width: 100%; height: 280px;
			border: 1px solid #666666;
			padding: 5px;
			overflow-y: auto;
		}
	//--></style>

	<script type='text/javascript'>
		function deleteReq() {
			<% if( htmlpage.getBackURL() != null ) { %>
				var returnURL = "<%= htmlpage.getBackURL() %>";
			<% } else { %>
				var returnURL = "<%= htmlpage.getRequestURL() %>?boardClassCode=<mtl:value id="record" key="boardClassCode"/>";
			<% } %>

			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<mtl:value id="record" key="boardClassCode"/>&mode=del";
			url += "&boardNumber=<mtl:value id="record" key="boardNumber"/>";
			url = attachDefaultParameter( url );
			url = replaceQueryValue( url, "url", encodeURIComponent(returnURL) );
			if( confirm( "<mtl:message key="MSG_CONFIRM_DELETE" encodeScript="true"/>" ) )
				location.replace( url );
		}

		function infoReq( boardNumber ) {
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<mtl:value id="record" key="boardClassCode"/>&mode=info";
			url += "&boardNumber="+ boardNumber;
			windowSelfOpen( url );
		}

		function modifyReq() {
			var url = "<%= htmlpage.getRequestURL() %>?boardClassCode=<mtl:value id="record" key="boardClassCode"/>&mode=imod";
			url += "&boardNumber=<mtl:value id="record" key="boardNumber"/>";
			windowSelfOpen( url );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return false;">
		<mtl:contentGroup groupId="info" type="content">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_TITLE"/></td>
				<td class='content1'><mtl:value id="record" key="title"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_BOARDOPTION"/></td>
				<td class='content4'><mtl:valuef id="record" format="${RBM_BOARD_BOARDOPTION1_@boardOption1,%{RBM_BOARD_BOARDOPTION1_T}}"/></td>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_REGISTUSERNAME"/></td>
				<td class='content4'><mtl:value id="record" key="registUserName"/></td>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_NOTICESTARTDATE"/></td>
				<td class='content4'><mtl:valuef id="record" format="${noticeStartDate~0~10}"/></td>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_NOTICEENDDATE"/></td>
				<td class='content4'><mtl:valuef id="record" format="${noticeEndDate~0~10}"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td class='content0' style='padding: 7px;'>
				<div class='notice_content'/>
				<%
					java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
					if( "H".equals(recordMap.get("boardOption1")) ) {
				%>
					<pre class='notice_content'><mtl:value id="record" key="content" encodeHTML="false"/></pre>
				<% } else { %>
					<pre class='notice_content'><mtl:value id="record" key="content"/></pre>
				<% } %>
				</div>
			</td></tr>
			</table>

		<% if( "info".equals(htmlpage.getMode()) ) { %>
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td class='content0' style='border-top: 1px solid #0078CC;'>
				<mtl:message key="jsp.rbm_boardnotice_info.NEXTBOARD"/>:
				<a href='JavaScript:infoReq("<mtl:value id="nextRecord" key="boardNumber"/>");'><mtl:value id="nextRecord" key="title"/></a>
				<br>
				<mtl:message key="jsp.rbm_boardnotice_info.PREVBOARD"/>:
				<a href='JavaScript:infoReq("<mtl:value id="prevRecord" key="boardNumber"/>");'><mtl:value id="prevRecord" key="title"/></a>
			</td></tr>
			</table>
		<% } %>
		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return"/>
			<% if( htmlpage.hasManageAuth() ) { %>
				<mtl:button type="modify"/>
				<mtl:button type="delete"/>
			<% } else if( sessionMng.isAuthorized("RBM", "RBMBoard."+ property.getProperty("boardClassCode") +".DEL") ) { %>
				<mtl:button type="delete"/>
			<% } %>
			<mtl:button type="close_if"/>
		</td></tr>
		</table>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
