<%--
	File Name:	dpr_boardnotice_info.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2017/02/28		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	String[] supportLocales = com.irt.rbm.RBMSystem.getSystemEnv("DPR", "Default;SupportLocale", "en,zh,th,vi,ko").split( "," );
	Map<String, Object> recordMap = (Map<String, Object>)pageContext.findAttribute( "record" );
%>
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
		function bodyLoad() {
			windowResizeTo( 800 );
		}

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

			var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DELETE" encodeScript="true"/>" };
			customPopup.confirm( messages, function(res) {
				if( res ) {
					location.replace( url );
				}
			});
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
			<div id='messagebar'></div>
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<%
				for( String locale : supportLocales ) {
					String titleValue = (String)recordMap.get( "title_" + locale );
					if( titleValue == null ) titleValue = "";

					out.println( "<tr>" );
					out.print( "<td class='subject'>" );
					out.print( msghandler.getMessage("FIELD_RBM_BOARD_TITLE") + " " + locale.toUpperCase() );
					out.println( "</td>" );
					out.print( "<td class='content1'>" );
					out.print( titleValue );
					out.println( "</td>" );
					out.println( "</tr>" );
				}
			%>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_BOARDOPTION"/></td>
				<td class='content2'><mtl:valuef id="record" format="${RBM_BOARD_BOARDOPTION1_@boardOption1,%{RBM_BOARD_BOARDOPTION1_T}}"/></td>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_REGISTUSERNAME"/></td>
				<td class='content2'><mtl:value id="record" key="registUserName"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_NOTICESTARTDATE"/></td>
				<td class='content2'><mtl:valuef id="record" format="${noticeStartDateTime~0~10}"/>&nbsp;<mtl:valuef id="record" format="${noticeStartDateTime~11~16}"/></td>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_NOTICEENDDATE"/></td>
				<td class='content2'><mtl:valuef id="record" format="${noticeEndDateTime~0~10}"/>&nbsp;<mtl:valuef id="record" format="${noticeEndDateTime~11~16}"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="jsp.dpr_boardnotice_input.FIELD_MAINTENANCE_START"/></td>
				<td class='content2'>
 					<mtl:valuef id="record" format="${maintenanceStart~0~10}"/>&nbsp;<mtl:valuef id="record" format="${maintenanceStartTime~0~5}"/>
				</td>
				<td class='subject'><mtl:message key="jsp.dpr_boardnotice_input.FIELD_MAINTENANCE_END"/></td>
				<td class='content2'>
					<mtl:valuef id="record" format="${maintenanceEnd~0~10}"/>&nbsp;<mtl:valuef id="record" format="${maintenanceEndTime~0~5}"/>
				</td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_DPR_BOARD_TIMEZONE"/></td>
				<td class='content2'><mtl:value id="record" key="timeZone"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<%
				for( String locale : supportLocales ) {
					String content = (String)recordMap.get( "content_" + locale );
					if( content == null ) content = "";

					out.println( "<tr>" );
					out.print( "<td class='subject'>" );
					out.print( msghandler.getMessage("FIELD_RBM_BOARD_CONTENT") + " " + locale.toUpperCase() );
					out.println( "</td>" );
					out.println( "<td class='content1' style='padding: 7px;'>" );
					out.println( "<div class='notice_content' style='height: 120px;'>" );
					out.println( "<pre class='notice_content'>" );

					if( "H".equals(recordMap.get("boardOption1")) ) {
						content = HtmlUtility.toHtmlString( content );
					}
					out.println( content );
					out.println( "</pre></div>" );
					out.println( "</td>" );
					out.println( "</tr>" );
				}
			%>
			</table>
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
