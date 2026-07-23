<%--
	File Name:	ics_helpboard_input.jsp
	Version:	2.2.0c(dpr)

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2017/02/28		2.2.0c	create
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

		#box_upload_image, #btn_upload_image {
			/* display: none; */
		}
	</style>

	<%@ include file="include_ics_board_editor.inc" %>
	<%@ include file="include_ics_ajax.inc" %>
	<%@ include file="include_ics_async_fileupload.inc" %>
	<script type='text/javascript'>
		Attach.MAX_SIZE = 3;
		Attach.INPUT_ID = "file_item";
		Attach.DISPLAY_ID = "content_files";

	</script>
</head>

<body class='content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<mtl:form name="frmImage" action="ICSBoardAttach" enctype="multipart/form-data">
		<input type='hidden' name='mode' value='reg'/>
		<input type='hidden' name='wintype' value='inner'/>

		<mtl:hidden id="record" key="boardClassCode"/>
		<mtl:hidden id="record" key="boardNumber"/>
		<mtl:hidden id="record" key="attachManageKey"/>

		<input type='hidden' name='attachNumber'>

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

		<mtl:contentGroup groupId="input" type="content" styleClass="card">
			<div id='messagebar'></div>
			<div class='table w100p'>
				<div class='row'>
					<div class='cell align-center' style='vertical-align: middle; padding: 0 70px 0 50px;'>
						<img src='images/person_desk.png' style='margin: 0 60px 15px;'>
						<span style='font-size: 16px; line-height: 140%;'><mtl:message key="jsp.ics_helpboard_input.MSG_HELP" encodeHTML="true"/></span>
					</div>
					<div class='cell'>
						<div style='display: inline-block;'>
							<div class='info-table table-fixed'>
								<div class='row'>
									<div class='cell'>
										<div class='field-title'><mtl:title key="headwordCode" descriptionKey="FIELD_ICS_HELP_BOARD_HEADWORD"/></div>
										<div class='field'>
											<mtl:select id="record" key="headwordCode" format="$H{headwordName}"
													className="com.irt.ics.BoardHeadword" listCodeKey="headwordCode" listNameFormat="$H{ICS_HELP_BOARD_HEADWORD_@headwordName}"
													condition="<%= \"boardClassCode=\"+ property.getProperty(\"boardClassCode\") + \"&headwordName=FO&headwordName_type=NEQ\" %>"
													hasBlank="false" searchable="false"/>
										</div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:title key="orderNumber" descriptionKey="FIELD_ICS_HELP_BOARD_ORDERNUMBER"/></div>
										<div class='field'><mtl:text id="record" key="orderNumber"/></div>
									</div>
									<div class='cell'></div>
								</div>
								<div class='row'>
									<div class='cell'>
										<div class='field-title'><mtl:title key="userName" descriptionKey="FIELD_ICS_HELP_BOARD_USERNAME"/></div>
										<div class='field'><mtl:text id="record" key="userName"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:title key="tel" descriptionKey="FIELD_ICS_HELP_BOARD_TEL"/></div>
										<div class='field'><mtl:text id="record" key="tel"/></div>
									</div>
									<div class='cell'>
										<div class='field-title'><mtl:title key="email" descriptionKey="FIELD_ICS_HELP_BOARD_EMAIL"/></div>
										<div class='field'><mtl:text id="record" key="email"/></div>
									</div>
								</div>
							</div>
							<div class='info-table table-fixed'>
								<div class='row'>
									<div class='cell'>
										<div class='field-title'><mtl:title key="content" descriptionKey="FIELD_ICS_HELP_BOARD_CONTENT"/></div>
										<div class='field'>
											<textarea name="content" rows="6"><mtl:value id="record" key="content" encodeHTML="true"/></textarea>
										</div>
									</div>
								</div>
							</div>
							<div class='search-bottom'>
								<div class='table-cell search-button'>
									<mtl:button type="return"/>
									<mtl:button type="submit" messageKey="jsp.BTN_SEND"/>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</mtl:contentGroup>

		<script type='text/javascript'>
			function checkInput() {
				<%= htmlpage.getValidationScript() %>
				if( !Field.checkMandatory(frmMain.content) ) return false;
				return submitInput();
			}
/*
			function emailCheck( elementObj ) {
				var regex = /\w+([-+.]\w+)*@\w+([-.]\w+)*\.[a-zA-Z]{2,5}$/;
				if( !regex.test(Field.getValue(elementObj)) ){
					return Field.alertError( elementObj, "<mtl:message key="jsp.usr_user_find_account_input.MSG_EMAIL_ERROR" encodeScript="true"/>" )
				}
				return true;
			}
*/
		</script>
	</mtl:form>
</body>
</mtl:html>
