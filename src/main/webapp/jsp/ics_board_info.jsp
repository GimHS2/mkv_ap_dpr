<%--
	File Name:	ics_board_info.jsp
	Version:	2.2.4c (dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2017/09/30		2.2.4c	FAQ 페이지 적용: content-wrapper, windowClose 적용
	jbaek		2016/08/31		2.2.3	CrossBrowsing 적용: table-layout:fixed; width:100%;
	....
	stghr12		2011/06/30		2.2.2	pageEncoding="euc-kr" 추가
	stghr12		2010/07/31		2.2.1	정리
	lsinji		2009/10/25		2.2.0	create
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
<%
	java.util.Map<String, Object> recordMap = (java.util.Map<String, Object>)pageContext.findAttribute( "record" );
	String boardClassCode = null;
	String boardOption = null;
	if( recordMap != null ) {
		boardClassCode = (String)recordMap.get( "boardClassCode" );
		boardOption = (String)recordMap.get( "boardOption" );
	}
	String type = "NAME";
	if(request.getParameter("type")!=null)
		type = request.getParameter("type").toString();
%>
<head>
	<%@ include file="include_ics_board_header.inc" %>
	<%@ include file="include_ics_ajax.inc" %>
	<%@ include file="include_ics_board_comment.inc" %>
	<style type='text/css'>
		div.notice_content {
			background-color: #FFFFFF;
			width: 100%; height: 280px;
			border: 1px solid #666666;
			padding: 5px;
			overflow-y: auto;
		}
	</style>

	<script type='text/javascript'>

		function bodyLoad() {
			if( parent.$("iframe.menu-content") ) {
				$(".frame-content").innerHeight( parent.$(".menu-content").contents().find( ".frame-content").innerHeight() );
			}

			var updated = "<mtl:value id="request" key="updateComplete"/>";
			var removed = "<mtl:value id="request" key="removeComplete"/>";
			if( "Y" == updated || "Y" == removed ) {
				parent.frames["menu_content"].location.reload();
			}
		}

		function deleteReq() {
			var returnURL = "<%= htmlpage.getRequestURL() %>?boardClassCode=<mtl:value id="record" key="boardClassCode"/>";
			returnURL = replaceQueryValue( returnURL, "mode", "blank" );

			var url = "<%= htmlpage.getRequestURL() %>?mode=del";
			url = replaceQueryValue( url, "boardClassCode", "<mtl:value id="record" key="boardClassCode"/>" );
			url = replaceQueryValue( url, "boardNumber", "<mtl:value id="record" key="boardNumber"/>" );
			url += "&url="+ encodeURIComponent( returnURL );
			customPopup.confirm( { "detail" : "<mtl:message key="MSG_ICS_BOARD_CONFIRM_DELETE" encodeScript="true"/>" }, function(res) {
				if( res ) {
					location.replace( url );
				}
			});
		}

		function downloadAttachReq( attachNumber ) {
			var url = "<%= systemConfig.getClassURL() %>/ICSBoardAttach?mode=down";
			url = replaceQueryValue( url, "boardClassCode", "<mtl:value id="record" key="boardClassCode"/>" );
			url = replaceQueryValue( url, "boardNumber", "<mtl:value id="request" key="boardNumber"/>" );
			url = replaceQueryValue( url, "attachManageKey", "<mtl:value id="record" key="attachManageKey"/>" );
			url = replaceQueryValue( url, "attachNumber", attachNumber );

			windowSelfOpen( url, getLocationURL() );
		}

		function infoReq( boardNumber ) {
			var url = "<%= htmlpage.getRequestURL() %>?mode=info&type=<%=type%>";
			url = replaceQueryValue( url, "boardClassCode", "<mtl:value id="record" key="boardClassCode"/>" );
			url = replaceQueryValue( url, "boardNumber", "<mtl:value id="record" key="boardNumber"/>" );

			windowSelfOpen( url );
		}

		function modifyReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=imod&type=<%=type%>";
			url = replaceQueryValue( url, "boardClassCode", "<mtl:value id="record" key="boardClassCode"/>" );
			url = replaceQueryValue( url, "boardNumber", "<mtl:value id="record" key="boardNumber"/>" );

			windowSelfOpen( url );
		}

		function replyReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&type=<%=type%>";
			url = replaceQueryValue( url, "boardClassCode", "<mtl:value id="record" key="boardClassCode"/>" );
			url = replaceQueryValue( url, "originalBoardNumber", "<mtl:value id="record" key="boardNumber"/>" );

			windowSelfOpen( url );
		}
	</script>
</head>

<body class='content' style='padding-left: 8px; overflow: hidden;'>
<%@ include file="include_pub_menuhtml_bodyheader.inc" %>
	<%@ include file="include_ics_board_bodyheader.inc" %>
	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return false;">
		<mtl:hidden id="property" key="type"/>
		<mtl:containsElse id="record">
			<mtl:contentGroup groupId="info" type="content" styleClass="frame-content">
				<div class='table w100p' style='height: 100%;'>
					<div class='table-cell align-center' style='vertical-align: middle;'>
						<img src='images/product_view.png'>
						<div class='blank-msg'><mtl:message key="jsp.ics_board_info.MSG_SELECT_POST"/></div>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:containsElse>

		<mtl:contains id="record">
			<mtl:contentGroup groupId="info" type="content" styleClass="frame-content">
				<h2><mtl:value id="record" key="title"/></h2>
				<table class='line_content' cellspacing='0' cellpadding='0' style='margin-bottom: 4px; margin-top: 4px;'>
				<tr>
					<td><div><mtl:value id="record" key="registUserName"/><span style='padding: 0 8px;'>&#124;</span><mtl:valuef id="record" format="${createDateTime~0~10}"/></div></td>
					<mtl:contains id="attaches">
					<td>
						<div class='div_box' align='right'>
							<mtl:loop id="attaches" loopId="loop">
								<img src='images/board/ico_attach_file.gif' align='absmiddle'/>
								<a href='JavaScript:downloadAttachReq("<mtl:value id="loop" key="attachNumber"/>");'>
									<font style='font-size: 11px; font-family: sans-serif;'><mtl:value id="loop" key="fileName"/>
										<mtl:valuef id="loop" format="${(:fileSize#NF.FLOAT2; byte)}"/>
									</font>
								</a><br>
							</mtl:loop>
						</div>
					</td>
					</mtl:contains>
				</tr>
				</table>
				<div style='margin-top: 15px; overflow: auto; height: calc(100% - 120px); border-bottom: 2px solid #C6C6C6;'>
					<table class='line_content' cellspacing='0' cellpadding='0' style='margin-bottom: 4px; margin-top: 4px;'>
					<tr><td style='padding: 20px; padding-top: 30px;'>
						<div class='board-content'>
						<% if( boardOption != null && boardOption.startsWith("H") ) { %>
							<mtl:value id="record" key="content" encodeHTML="false"/>
						<% } else { %>
							<pre class='notice_content'><mtl:value id="record" key="content"/></pre>
						<% } %>
						</div>
					</td></tr>
					</table>
				</div>
				<div class='list-function'>
					<div class='button'>
					<% if( htmlpage.hasManageAuth() ) { %>
						<mtl:button type="modify"/>
						<mtl:button type="delete"/>
					<% } %>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:contains>
	</mtl:form>
</body>
</mtl:html>
