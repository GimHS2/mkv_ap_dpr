<%--
	File Name:	ics_board_input.jsp
	Version:	2.2.6c(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2018/09/30		2.2.6c	disableLegacyAttachImage: #box_upload_image 때문에 fileAttach의 삭제 버튼이 동작하지 않았었음.
	jbaek		2017/09/30		2.2.6c	FAQ 페이지 적용: content-wrapper 적용. noticeEndDate mandatory 삭제
	jbaek		2016/08/31		2.2.5	fieldSet 추가. element id 추가. tab_write.gif를 message로 대체
	GimHS		2015/05/29		2.2.4	CrossBrowsing 적용: 오타 및 스타일 수정
	jbaek		2014/06/30		2.2.3c	noticeStartDate, noticeEndDate 오른 정렬 css추가, fckeditor사용 안하는 경우 적용.
	GimHS		2012/12/31		2.2.3	CrossBrowsing 적용: 'cursor: hand;' -> 'cursor: pointer;' 스타일 변경
	stghr12		2011/06/30		2.2.2	pageEncoding="euc-kr" 추가
	yjcha		2010/11/30		2.2.1c	noticeStartDate, noticeEndDate 처리 추가
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
	String type = property.getProperty("type");

	boolean disableLegacyAttachImage = true;
%>
<head>
	<%@ include file="include_ics_board_header.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<style type='text/css'>
		#contentboard_tab_write {
			background: #EEF0F9;
			background-image: -webkit-linear-gradient(top, #EEF0F9, #EEF0F9);
			background-image: -moz-linear-gradient(top, #EEF0F9, #EEF0F9);
			background-image: -ms-linear-gradient(top, #EEF0F9, #EEF0F9);
			background-image: -o-linear-gradient(top, #EEF0F9, #EEF0F9);
			background-image: linear-gradient(to bottom, #EEF0F9, #EEF0F9);
			-webkit-border-radius: 3px;
			-moz-border-radius: 3px;
			border-radius: 3px;
			border-color: lightgrey;
			border-style: solid;
			border-bottom-style: none;
			border-width: 1px;
			font-family: Arial;
			color: #000000;
			text-decoration: none;
			margin-left: 10px;
			padding: 2px 10px;
			vertical-align: super;
		}

		textarea.notice_content {
			background-color: #FFFFFF;
			width: 100%; height: 280px;
			table-layout: fixed;
			border: 1px solid #666666;
			padding: 5px;
			overflow: auto;
		}

		<% if( disableLegacyAttachImage ) { %>
		#box_upload_image, #btn_upload_image {
			display: none; visibility: hidden;
		}
		<% } %>
	</style>

	<%@ include file="include_ics_board_editor.inc" %>
	<%@ include file="include_ics_ajax.inc" %>
	<%@ include file="include_ics_async_fileupload.inc" %>
	<script type='text/javascript'>
		Attach.MAX_SIZE = 3;
		Attach.INPUT_ID = "file_item";
		Attach.DISPLAY_ID = "content_files";

		function bodyLoad() {
			if( parent.$("iframe.main-content") ) {
				$(".frame-content").innerHeight( parent.$(".menu-content").contents().find( ".frame-content").innerHeight() );
			}

			if( frmMain.boardTypeCheck ) {
				if( frmMain.boardType.value == 'N' )
					frmMain.boardTypeCheck.checked = true;
			}

			<mtl:loop id="attaches" loopId="loop" loopIndex="loopIndex">
				Attach.attachList.push(
					new Attach( "<mtl:value id="loop" key="attachNumber"/>", "<mtl:value id="loop" key="fileName" encodeScript="true"/>" )
				);
			</mtl:loop>

			Attach.print();
			Attach.appendFile();

			BoardEditor.createEditor( 'content', '100%', '300' );
		}
	</script>
</head>

<body class='content' style='padding-left: 8px; overflow: hidden;'>
<%@ include file="include_pub_menuhtml_bodyheader.inc" %>
	<%@ include file="include_ics_board_bodyheader.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<mtl:form name="frmImage" action="ICSBoardAttach" enctype="multipart/form-data">
		<input type='hidden' name='mode' value='reg'/>
		<input type='hidden' name='wintype' value='inner'/>

		<mtl:hidden id="record" key="boardClassCode"/>
		<mtl:hidden id="record" key="boardNumber"/>
		<mtl:hidden id="record" key="attachManageKey"/>

		<input type='hidden' name='attachNumber'>

		<div id="box_upload_image" style='width: 80px; height: 115px; overflow: hidden; position: absolute; top: 153px; left: 200px; filter: alpha(opacity=0); opacity: 0; z-index: 10;'>
			<input type='file' name='imagefile' title='<mtl:message key='MSG_ICS_BOARD_CK_TOOLBAR_QUICKIMAGEINSERT' encodeScript='true'/>'
					onChange='JavaScript: BoardEditorImage.regist(this);' size='1' style='width: 80px; cursor: pointer;'/>
		</div>
	</mtl:form>

	<mtl:form name="frmMain" enctype="multipart/form-data" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>

		<mtl:hidden id="property" key="type"/>
		<mtl:hidden id="record" key="boardType"/>
		<mtl:hidden id="record" key="boardClassCode"/>
		<mtl:hidden id="record" key="boardNumber"/>
		<mtl:hidden id="record" key="boardOption"/>
		<mtl:hidden id="record" key="attachManageKey"/>
		<mtl:hidden id="record" key="originalBoardNumber"/>
		<input type='hidden' name='attachNumbers'>

		<mtl:contentGroup groupId="input" type="content" styleClass="frame-content" style='padding-right: 5px;'>
			<div style='overflow: auto; height: calc(100% - 42px); padding-right:10px; border-bottom: 2px solid #C6C6C6'>
				<div class='info-table'>
					<div class='row'>
						<div class='cell' style='width: 150px; padding-bottom: 10px;'>
							<div class='field-title'><mtl:message key="FIELD_ICS_BOARD_CATEGORY"/></div>
						</div>
						<div class='cell' style='padding-bottom: 10px;'>
							<div class='field'>
								<mtl:select id="record" key="headwordCode" format="$H{headwordName}" searchable="false" width="auto"
										className="com.irt.ics.BoardHeadword" listCodeKey="headwordCode" listNameFormat="$H{headwordName}"
										condition="<%= \"boardClassCode=\"+ property.getProperty(\"boardClassCode\") %>"
										hasBlank="true" nullValueKey="MSG_PUB_SELECT@FIELD_ICS_BOARD_HEADWORD"/>
								<mtl:ifvalue id="record"  key="boardTypeCheck" notValue="R">
									&nbsp;&nbsp;<mtl:check id="record" key="boardTypeCheck" checkValue="N" defaultValue="C" descriptionKey="jsp.ics_board_input.MSG_IMPORTANT_POST"/>
								</mtl:ifvalue>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell' style='width: 150px; padding-bottom: 10px;'>
							<div class='field-title'><mtl:title key="title" descriptionKey="FIELD_ICS_BOARD_TITLE" mandatory="false"/></div>
						</div>
						<div class='cell' style='padding-bottom: 10px;'>
							<div class='field'><mtl:text id="record" key="title"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell' style='width: 150px; padding-bottom: 10px;'>
							<div class='field-title'><mtl:message key="FIELD_RBM_BOARD_REGISTUSERNAME"/></div>
						</div>
						<div class='cell' style='padding-bottom: 10px;'>
							<div class='field'>
								<mtl:hidden id="record" key="registUserName"/>
								<mtl:hidden id="record" key="registUserUserId"/>
								<mtl:value id="record" key="registUserName"/>
								<mtl:valuef id="record" format="${(:registUserUserId;)}"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell' style='width: 150px; padding-bottom: 10px;'>
							<div class='field-title'><mtl:message key="FIELD_ICS_BOARD_ATTACH"/></div>
						</div>
						<div class='cell' style='width: 120px;'>
							<div style='width: 120px; height: 15px; overflow: hidden; position: relative; top: 8px; filter: alpha(opacity=0); opacity: 0; z-index: 10;'>
								<div id='file_item'></div>
							</div>
							<div id="btn_upload_attach" style='width: 120px; height: 15px; overflow: hidden; position: relative; top: -7px; filter: alpha(opacity=100); opacity: 100; z-index: 0;'>
								<img src='images/board/ico_attach_file.gif' style='cursor: pointer;' align='absmiddle'/>
								<font style='font-size: 13px;'><mtl:message key="ICS_BOARD_ATTACHTYPE_FLE"/></font>
							</div>
						</div>
						<div class='cell'>
							<div id='content_files' style='padding-top: 8px; padding-left: 5px;'><mtl:message key="MSG_ICS_NO_ATTACHFILE"/></div>
						</div>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<textarea name="content"><mtl:value id="record" key="content" encodeHTML="true"/></textarea>
						</div>
					</div>
				</div>
			</div>
			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
					<mtl:button type="save"/>
				</div>
			</div>
		</mtl:contentGroup>

		<script type='text/javascript'>
			function checkInput() {
				if( frmMain.boardTypeCheck ) {
					 if( !frmMain.boardTypeCheck.checked )
						frmMain.boardType.value = "C";
					 else
						frmMain.boardType.value = "N";
				}

				<%= htmlpage.getValidationScript() %>

				var attachArray = new Array;

				var images = BoardEditor.getImageElementFromEditor();
				if( images ) {
					for( var i = 0; i < images.length; i++ ) {
						if( images[i].id.indexOf(frmMain.attachManageKey.value) >= 0 ) {
							var values = images[i].id.split( ";" );
							if( values.length == 2 )
								attachArray.push( values[1] );
						}
					}
				}

				for( var i = 0; i < Attach.attachList.length; i++ ) {
					if( Attach.attachList[i].display && Attach.attachList[i].id != null ) {
						if( Attach.attachList[i].id )
							attachArray.push( Attach.attachList[i].id );
					}
				}
				if( attachArray.length > 0 ) frmMain.attachNumbers.value = attachArray;

				BoardEditor.submitReady();
				return submitInput();
			}
		</script>
	</mtl:form>
</body>
</mtl:html>
