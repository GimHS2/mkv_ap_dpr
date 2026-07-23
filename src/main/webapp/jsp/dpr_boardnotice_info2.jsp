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

		pre {
			font-size: 14px;
		}
	//--></style>
	<script type='text/javascript'>
		var windowWidth = 900;
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<%@ include file="include_rbm_frame_bodyheader.inc" %>
		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return false;">
			<mtl:contentGroup groupId="info" type="content">
				<h2><mtl:value id="record" key="title"/></h2>
				<table class='line_content' cellspacing='0' cellpadding='0' style='margin-bottom: 4px; margin-top: 4px;'>
				<tr>
					<td>
						<div style='font-size: 16px; font-weight: bold;'>
							<mtl:valuef id="record" format="${maintenanceStartDateTime~0~16}"/> ~ <mtl:valuef id="record" format="${maintenanceEndDateTime~0~16}"/> (<mtl:value id="record" key="timeZone"/>)
						</div>
					</td>
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

				<div style='overflow: auto; border-bottom: 2px solid #C6C6C6; margin-top: 10px; padding-bottom: 20px;'>
					<table class='line_content' cellspacing='0' cellpadding='0' style='margin-bottom: 4px; margin-top: 4px;'>
					<tr><td>
						<div class='board-content'>
						<%
							java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
							if( "H".equals(recordMap.get("boardOption1")) ) {
						%>
							<mtl:value id="record" key="content" encodeHTML="false"/>
						<% } else { %>
							<pre class='notice_content' style='white-space: pre-line; word-break: break-word; line-height: 200%;'><mtl:value id="record" key="content"/></pre>
						<% } %>
						</div>
					</td></tr>
					</table>
				</div>

				<div class='list-function'>
					<div class='button'>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:form>
	</div>
</body>
</mtl:html>
