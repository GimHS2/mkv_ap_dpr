<%--
	File Name:	rbm_boardnotice_input.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.2	DPR Page tail (legal notice) add
	stghr12		2008/03/31		2.2.1	boardOption -> boardOption1
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										windowResizeTo( 650 ); -> windowResizeTo( 800 );
	GimHS		2007/04/30		2.1.0	BOARDOPTION 추가
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
		function checkNoticeDate( noticeStartDate, noticeEndDate ) {
			if( noticeStartDate.value > noticeEndDate.value ) {
				customPopup.alert( { "header" : "<mtl:message key="ERR_INVALID_NOTICEDATE"/>" } );
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
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:title key="title"/></td>
				<td class='content1'><mtl:text id="record" key="title" styleClass="length_70"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_BOARDOPTION"/></td>
				<td class='content4'><mtl:select id="record" key="boardOption1" nullValueKey="RBM_BOARD_BOARDOPTION1_T"
						prefixKey="RBM_BOARD_BOARDOPTION1_" codeValues="T,H"/></td>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_REGISTUSERNAME"/></td>
				<td class='content4'><mtl:value id="record" key="registUserName"/></td>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_NOTICESTARTDATE"/></td>
   				<td class='content4'>
 					<mtl:text id="record" key="noticeStartDate" onBlur="JavaScript:Field.formatDate(this);" styleClass="length_date_ymd"/>
					<mtl:ibutton type="calendar" key="noticeStartDate" styleClass="tbtn"/>
				</td>
				<td class='subject'><mtl:message key="FIELD_RBM_BOARD_NOTICEENDDATE"/></td>
				<td class='content4'>
					<mtl:text id="record" key="noticeEndDate" onBlur="JavaScript:Field.formatDate(this);" styleClass="length_date_ymd"/>
					<mtl:ibutton type="calendar" key="noticeEndDate" styleClass="tbtn"/>
				</td>

			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td class='content0' style='padding: 7px;'>
				<mtl:textarea id="record" key="content" styleClass="notice_content length_full"/>
			</td></tr>
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
