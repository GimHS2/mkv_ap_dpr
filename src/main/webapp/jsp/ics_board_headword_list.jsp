<%--
	File Name:	ics_board_headword_list.jsp
	Version:	2.2.4

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	song7981	2015/09/30		2.2.4	headwordName에 글자수 제한
	GimHS		2012/12/31		2.2.3	CrossBrowsing 적용
										  -> 표시건수 select 박스가 아래로 내려가는 문제 해결
										  -> 리스트 좌측 상단에 있는 검색 Select 박스 일렬로 안나오는 문제 해결
										  -> style 값 끝에 ";" 추가
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
<head>
	<%@ include file="include_ics_board_header.inc" %>
	<%@ include file="include_ics_ajax.inc" %>

	<script type='text/javascript'>
		var request = null;
		var windowWidth = 1100;

		function deleteReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=del&boardClassCode="+ frmMain.boardClassCode.value;
			requestDelete( url, frmMain.listcheckbox, "headwordNumber" );
		}

		function imageChange( mode ) {
			if( mode ) {
				frmMain.mode.value = mode;
				document.getElementById("modifyContent").style.display = "";
				$("#btn_submit").text( mode == "reg" ? "<mtl:message key="jsp.BTN_REGIST"/>" : "<mtl:message key="jsp.BTN_MODIFY"/>" );
				if( mode == "reg" ) {
					$("input[name=headwordName]").val("");
				}
			} else
				document.getElementById("modifyContent").style.display = 'none';
		}

		function modifyReq() {
			if( request != null && typeof request.readyState != 'undefined' ) {
				customPopup.alert( { "header" : "<mtl:message key="MSG_ICS_REQUEST_ALREADY_USED"/>" } );
				return;
			}
			if( (request = createXMLRequest()) == null ) return;

			var selectedValues = CheckBox.getValues( frmMain.listcheckbox );
			if( frmMain.boardClassCode.value == "" ) {
				customPopup.alert( { "header" : "<mtl:message key="MSG_ICS_BOARD_SELECT_BOARDCLASS" encodeScript="true"/>" } );
				request = null;
			} else if( selectedValues == null || selectedValues.length > 1 ) {
				customPopup.alert( { "header" : "<mtl:message key="MSG_CHOOSE_ONLY_ONE" encodeScript="true"/>" } );
				request = null;
			} else {
				imageChange( "mod" );
				frmMain.headwordCode.value = selectedValues[0];

				var url = "<%= htmlpage.getRequestURL() %>?mode=req";
				url = replaceQueryValue( url, "boardClassCode", frmMain.boardClassCode.value );
				url = replaceQueryValue( url, "headwordCode", selectedValues[0] );

				request.open( "GET", url, true );
				request.onreadystatechange = stateChanged;
				request.send( null );
			}
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=reg";

			if( frmMain.boardClassCode.value == "" ) {
				customPopup.alert( { "header" : "<mtl:message key="MSG_ICS_BOARD_SELECT_BOARDCLASS" encodeScript="true"/>" } );
				return;
			}

			imageChange("reg");
			frmMain.headwordName.select();
			frmMain.headwordName.focus();
		}

		function stateChanged() {
			if ( request.readyState == 4 ) {
				frmMain.headwordName.value = request.responseText;
				frmMain.headwordName.select();
				frmMain.headwordName.focus();

				request = null;
			}
		}

		function submitContent() {
			var url = "<%= htmlpage.getRequestURL() %>?mode="+ frmMain.mode.value;
			url = replaceQueryValue( url, "boardClassCode", frmMain.boardClassCode.value );
			url = replaceQueryValue( url, "headwordCode", frmMain.headwordCode.value );
			url = replaceQueryValue( url, "headwordName", encodeURIComponent(frmMain.headwordName.value) );

			windowSelfOpen( url, getLocationURL() );
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_ics_board_bodyheader.inc" %>
		<h2><%= htmlpage.getTitle() %></h2>
		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return false;">
			<mtl:contentGroup groupId="list" type="list" styleClass="list_content none-card">
				<div class='list-menu'>
					<mtl:select id="request" key="boardClassCode" searchable="false" width="auto"
							className="com.irt.ics.BoardClass" listCodeKey="boardClassCode" listNameFormat="$H{boardClassName}"
							hasBlank="true" nullValueKey="MSG_PUB_SELECT@FIELD_ICS_BOARD_BOARDCLASS"
							modified="JavaScript:listLink(this);"/>
				</div>

				<%
					ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );

					if( sessionMng.isAuthorized("ICS", "ICSBoardHeadword.DEL") )
						listwriter.setCheckboxType( ListWriter.CHECKBOXTYPE_CHECK );
					listwriter.setNumbering( true );
					listwriter.print( out );
				%>

				<div class='list-function'>
					<div class='button'>
					<% if( sessionMng.isAuthorized("ICS", "ICSBoardHeadword.MNG") ) { %>
						<mtl:button type="regist" messageKey="jsp.BTN_WRITE"/>
						<mtl:button type="modify"/>
					<% } %>
					<% if( sessionMng.isAuthorized("ICS", "ICSBoardHeadword.DEL") ) { %>
						<mtl:button type="delete"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
					<div id='list_page' class='page'>
						<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
					</div>
				</div>

				<div id='modifyContent' style='display: none; margin-top: 15px;'>
					<input type='hidden' name='mode'/>
					<input type='hidden' name='headwordCode'/>
					<input type='text' name='headwordName' onKeyDown='JavaScript:callByKeydown(submitContent);' maxlength='15' class='input-field' style='width: 200px; margin-right: 15px;'/>

					<mtl:button type="button" id="btn_submit" onClick="JavaScript:submitContent();" messageKey="jsp.BTN_REGIST"/>
					<mtl:button type="button" onClick="JavaScript:imageChange();" messageKey="jsp.BTN_CANCEL"/>
				</div>
			</mtl:contentGroup>
		</mtl:form>
	</div>
</body>
</mtl:html>
