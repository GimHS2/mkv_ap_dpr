<%--
	File Name:	dpr_main_notice.jsp
	Version:	2.2.3

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2017/02/28		2.2.3	중요 게시물 상단에 표시되도록 변경
	jbaek		2014/12/14		2.2.2	notice 팝업창 resize가능하도록 변경
	jbaek		2014/06/30		2.2.1	CrossBrowsing
	lsinji		2008/09/26		2.2.0	create
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
	<link rel='stylesheet' href='style/menu_portal.css'/>
	<style type='text/css'><!--
		tr, td { margin: 0 0 0 ; font-family: tahoma, Verdana, Arial, Helvetica, sans-serif; font-size:12px; color:#767676; }
	//--></style>
	<script type='text/javascript'>
		function noticeReq( boardClassCode, boardNumber) {
			if( boardClassCode )
				window.open( "<%= systemConfig.getClassURL() %>/ICSBoard?noticeInd=Y&boardClassCode="+ boardClassCode + "&boardNumber="+ boardNumber
					+ "&mode=info"
					+ "&menu=portal&locale=" + "<%= htmlpage.getLocale() %>"
					, "winNotice"
					, "toolbar=no, status=no, location=no, directories=no, menubar=no, resizable=yes, scrollbars=yes"
				);
			else
				windowSelfOpen( "<%= systemConfig.getClassURL() %>/ICSBoard?mode=list" );
		}

		function noticeListReq( boardClassCode ) {
			if( typeof boardClassCode == "undefined" )
				boardClassCode = "NO";

			var partyId = "<%= sessionMng.getPartyId() %>";
			var organizationCode = "<%= sessionMng.getExtraValue() %>";
			if( organizationCode != "null" && partyId != "" ) boardClassCode += ( "."+ organizationCode );
			else {
					if( partyId != null && partyId != "" ) {
						boardClassCode += ( "."+ partyId );
					} else {
						alert( "<%= msghandler.getMessage("ERR_NEEDED_SELECT_ORGANIZATION") %>" );
					return false;
				}
			}

			parent.noticeListReq( boardClassCode );
		}
	</script>
</head>

<body style='margin: 0px;'>
	<%@ include file="include_pub_list.inc" %>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
	<div class='list_content_data_scroll' style='overflow-y: auto; height: 150px;'>
		<table width='100%' cellpadding='2' cellspacing='1' class='board_index'>
		<tr>
			<th width='20%'><mtl:message key="jsp.dpr_main_notice.FIELD_BOARD_NUMBER"/></th>
			<th width='40%'><mtl:message key="jsp.dpr_main_notice.FIELD_BOARD_TITLE"/></th>
			<th width='20%'><mtl:message key="jsp.dpr_main_notice.FIELD_REGISTER_USER"/></th>
			<th width='20%'><mtl:message key="jsp.dpr_main_notice.FIELD_BOARD_UPDATETIME"/></th>
		</tr>
		<mtl:loop id="notices" loopId="loop" loopIndex="index">
		<tr height='20'>
			<td width='5%'>
			<mtl:ifvalue key="boardType" id="loop" value="N"><img src='images/board/ico_important.png' class='attach'></mtl:ifvalue>
			<mtl:ifvalue key="boardType" id="loop" notValue="N"><mtl:value id="loop" key="boardNumber"/></mtl:ifvalue>
			</td>
			<td width='65%'>
			<a href='JavaScript:noticeReq("<mtl:value id="loop" key="boardClassCode"/>", "<mtl:value id="loop" key="boardNumber"/>")'>
			<mtl:value id="loop" key="title"/>
			<mtl:ifvalue key="attachedFileInd" id="loop" value="Y"><img src='images/board/ico_attach_file.gif' class='attach'></mtl:ifvalue>
			</a>
			</td>
			<td width='20%'><mtl:value id="loop" key="registUserName"/></a></td>
			<td width='10%'><mtl:valuef id="loop" format="${updateDateTime~0~10}"/></td>
		</tr>
		</mtl:loop>
		</table>
		</div>
	</mtl:form>
</body>
</mtl:html>
