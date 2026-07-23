<%--
	File Name:	dpr_boardnotice_input.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
	hankalam	2017/02/28		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.Map"%>
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
	<%@ include file="include_pub_input.inc" %>
	<style type='text/css'><!--
		textarea.notice_content {
			background-color: #FFFFFF;
			width: 100%; height: 280px;
			table-layout: fixed;
			border: 1px solid #666666;
			padding: 5px;
			overflow: auto;
		}
	//--></style>

	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 800 );
			resetForm( frmMain );
			focusForm( frmMain, frmMain.title );
		}

		function checkNoticeDate( noticeStartDate, noticeEndDate ) {
			if( noticeStartDate.value > noticeEndDate.value ) {
					alert("<mtl:message key="ERR_INVALID_NOTICEDATE"/>");
				return false;
			}
			else
				return true;
		}


	</script>
</head>

<body class='content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>
		<input type='hidden' name='boardType' value='C'>
		<mtl:hidden id="record" key="boardClassCode"/>
		<mtl:hidden id="record" key="boardNumber"/>
		<mtl:hidden id="record" key="boardOption"/>

		<mtl:contentGroup groupId="input" type="content">
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
					out.print( "<input type='text' name='title_" + locale + "' class='input-field' data-mandatory='true' maxlength='128' value='"
								+ titleValue + "'>" );
					out.println( "</td>" );
					out.println( "</tr>" );
				}
			%>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_BOARDOPTION"/></td>
				<td class='content3'><mtl:select id="record" key="boardOption1" nullValueKey="RBM_BOARD_BOARDOPTION1_T"
						prefixKey="RBM_BOARD_BOARDOPTION1_" codeValues="T,H" searchable="false"/></td>
				<td class='subject'><mtl:message key="jsp.dpr_boardnotice_input.FIELD_BLOCK_LOGIN"/></td>
				<td class='content3'><mtl:select id="record" key="boardOption2" defaultValue="N"
						prefixKey="PUB_WHETHER_" codeValues="Y,N" searchable="false"/></td>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_REGISTUSERNAME"/></td>
				<td class='content3'><mtl:value id="record" key="registUserName"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_NOTICESTARTDATE"/></td>
				<td class='content2'>
 					<mtl:date id="record" key="noticeStartDate" style="width: 49%"/>
 					<mtl:text id="record" key="noticeStartTime" style="width: 49%"/>
				</td>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_NOTICEENDDATE"/></td>
				<td class='content2'>
					<mtl:date id="record" key="noticeEndDate" style="width: 49%"/>
					<mtl:text id="record" key="noticeEndTime" style="width: 49%"/>
				</td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="jsp.dpr_boardnotice_input.FIELD_MAINTENANCE_START"/></td>
				<td class='content2'>
 					<mtl:date id="record" key="maintenanceStart" style="width: 49%"/>
 					<mtl:text id="record" key="maintenanceStartTime" style="width: 49%"/>
				</td>
				<td class='subject'><mtl:message key="jsp.dpr_boardnotice_input.FIELD_MAINTENANCE_END"/></td>
				<td class='content2'>
					<mtl:date id="record" key="maintenanceEnd" style="width: 49%"/>
					<mtl:text id="record" key="maintenanceEndTime" style="width: 49%"/>
				</td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_DPR_BOARD_TIMEZONE"/></td>
				<td class='content1'>
					<mtl:text id="record" key="timeZone" defaultValue="Asia/Singapore" styleClass="length_date_ymd"/>
				</td>

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
					out.print( "<td class='content1' style='padding: 7px;'>" );
					out.print( "<textarea name='content_" + locale + "' class='input-field notice_content length_full' style='height: 90px;'>" );
					out.print( content );
					out.print( "</textarea>" );
					out.println( "</td>" );
					out.println( "</tr>" );
				}
			%>
			</table>
		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return"/>
			<mtl:button type="submit"/>
			<mtl:button type="reset"/>
			<mtl:button type="close_if"/>
		</td></tr>
		</table>

		<script type='text/javascript'>
			function checkInput() {
				<%= htmlpage.getValidationScript() %>

				var boardopt = "";
				for( var i = 1; i <= 9; i++ ) {
					var elementObj = eval( "frmMain.boardOption"+ i );
					if( !elementObj ) break;

					boardopt = boardopt + ( elementObj.value ? elementObj.value : " " );
				}
				frmMain.boardOption.value = boardopt + frmMain.boardOption.value.substring( boardopt.length );

				if( !checkNoticeDate( frmMain.noticeStartDate, frmMain.noticeEndDate ) ) {
					frmMain.noticeEndDate.focus();
					return false;
				}


				return submitInput();
			}

			function resetInput() {
				frmMain.reset();
				resetForm( frmMain );
			}
		</script>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
