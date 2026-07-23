<%--
	File Name:	ics_board_popup.jsp
	Version:	2.2.1c(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	stghr12		2011/06/30		2.2.1c	pageEncoding="euc-kr" 추가
	yjcha		2010/11/30		2.2.0c	create(ics_board_info.jsp 복사)
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
	java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
	String boardClassCode = (String)recordMap.get( "boardClassCode" );
	String boardOption = (String)recordMap.get( "boardOption" );
%>
<head>
	<%@ include file="include_ics_board_header.inc" %>
	<%@ include file="include_ics_ajax.inc" %>
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

		function downloadAttachReq( attachNumber ) {
			var url = "<%= systemConfig.getClassURL() %>/ICSBoardAttach?mode=down";
			url = replaceQueryValue( url, "boardClassCode", "<mtl:value id="record" key="boardClassCode"/>" );
			url = replaceQueryValue( url, "boardNumber", "<mtl:value id="request" key="boardNumber"/>" );
			url = replaceQueryValue( url, "attachManageKey", "<mtl:value id="record" key="attachManageKey"/>" );
			url = replaceQueryValue( url, "attachNumber", attachNumber );

			windowSelfOpen( url, getLocationURL() );
		}

		function bodyLoad() {
			window.resizeTo( 700, document.body.scrollHeight );
			window.resizeBy( 0, document.body.scrollHeight - document.body.clientHeight );
			<% if( com.irt.data.Condition.isConditionTrue(property.getProperty("jsp.public.focusOnLoad")) ) { %>
				self.focus();
			<% } %>
		}
	</script>
</head>
<body bgcolor='#F2F2F2' onLoad='JavaScript:bodyLoad();'>
	<div style='background: url(images/notice_title_bg.gif); padding-top: 19;'><img src='images/notice_title.gif'></div>

	<div class='notice' style='height: 400; margin-top: 5; margin-bottom: 5; overflow-y: auto;'>
		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return false;">
			<mtl:contentGroup groupId="info" type="content">
				<table class='line_content' cellspacing='0' cellpadding='0' style='margin-bottom: 4px; margin-top: 4px;'>
				<tr>
					<td class='contenboardinfo' style='margin-left: 10px;'>
						<b><mtl:value id="record" key="boardNumber"/></b>

						<img src='images/board/ico_separate.gif' align='absmiddle' >
						<mtl:valuef id="record" format="$H{[:headwordName;]}"/>
						<mtl:value id="record" key="title"/>
					</td>
					<td class='labels' align='right' style='padding-right: 10px;'>
						<mtl:message key="FIELD_CREATEDATETIME"/>
						<mtl:valuef id="record" format="${createDateTime~0~10}"/>
					</td>
				</tr>
				</table>

				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr height='3'><td background='images/board/b_dot.gif'></td></tr>
				<tr><td>
					<table class='line_content' cellspacing='0' cellpadding='0'>
					<tr>
						<td style='padding-right: 20;'>
							<mtl:contains id="attaches">
								<div class='div_box' align='right'>
									<mtl:loop id="attaches" loopId="loop">
										<img src='images/board/ico_attach_file.gif' align='absmiddle'/>
										<a href='JavaScript:downloadAttachReq("<mtl:value id="loop" key="attachNumber"/>");'>
											<font style='font-size: 11; font-family: sans-serif;'><mtl:value id="loop" key="fileName"/>
												<mtl:valuef id="loop" format="${(:fileSize#NF.FLOAT2; byte)}"/>
											</font>
										</a><br>
									</mtl:loop>
								</div>
							</mtl:contains>
						</td>
					</tr>
					</table>
				</td></tr>
				<tr><td style='padding: 20px; padding-top: 30px;'>
					<% if( boardOption != null && boardOption.startsWith("H") ) { %>
						<mtl:value id="record" key="content" encodeHTML="false"/>
					<% } else { %>
						<pre class='notice_content'><mtl:value id="record" key="content"/></pre>
					<% } %>
				</td></tr>
				</table>
			</mtl:contentGroup>
		</mtl:form>
	</div>

	<div align='center' style='width: 100%; height: 40px; padding-top: 10;'>
		<a href="JavaScript:self.close();"><img src='images/notice_btn_close.gif'></a>
	</div>
</body>
</mtl:html>
