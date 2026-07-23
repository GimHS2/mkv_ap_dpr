<%--
	File Name:	pub_upload_input.jsp
	Version:	2.2.2c

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2018/04/30		2.2.2c	supportFileType 처리
	jbaek		2018/04/30		2.2.2c	encoding detect suggestion 처리
	jbaek		2014/09/26		2.2.2	rbm2 taglib 적용
	stghr12		2008/03/31		2.2.1	PUB_ASCII_FILEFORMAT_ -> PUB_FILEFORMAT_
	stghr12		2007/11/30		2.2.0	"<script language='JavaScript'><!--"를 "<script type='text/javascript'>"로 변경
										systemConfig.getEncoding() -> systemConfig.getEncoding(htmlpage.getLocale())
										resetInput(); -> resetForm( frmMain );
										jsp.pub_upload_input.FIELD_ENCODING -> FIELD_ENCODING
	stghr12		2007/04/30		2.1.1	uploadType 항목 style 수정
	stghr12		2006/12/01		2.1.0	create
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
<%
	java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
	if( recordMap == null ) recordMap = new java.util.HashMap();
	if( recordMap.get("encoding") == null && systemConfig.getEncoding(htmlpage.getLocale()) != null )
		recordMap.put( "encoding", systemConfig.getEncoding(htmlpage.getLocale()) );

	String[] hiddenKeys = (String[])pageContext.findAttribute( "hiddenKeys" );

	String encs = "";
	String supportedLocale = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocale;" + sessionMng.getPartyId(), "en");
	if( supportedLocale != null ) {
		String[] ll = supportedLocale.split(",");
		for( String l : ll ) {
			java.util.Locale locale = new java.util.Locale(l);
			if( locale != null ) {
				String enc = systemConfig.getEncoding(locale);

				if( enc != null ) {
					encs += enc;
					encs += ",";
				}
			}
		}
	}
	encs = encs.replaceAll(",$", "");

	String defaultSupportEncodings = "UTF8,UTF16";
	String supportedEncodingsCsv = encs;
	if( supportedEncodingsCsv == null || supportedEncodingsCsv.length() <= 0 )
		supportedEncodingsCsv = "";
	else
		supportedEncodingsCsv += ",";

	supportedEncodingsCsv += defaultSupportEncodings;

	String supportFileTypesCsv = (String)recordMap.get("supportFileTypesCsv");
	String defaultFileType = (String)recordMap.get("defaultFileType");
	if( supportFileTypesCsv == null || supportFileTypesCsv.length() == 0 )
		supportFileTypesCsv = "CSV,TAB";
	if( defaultFileType == null || defaultFileType.length() == 0 ) {
		if( supportFileTypesCsv.contains("CSV") )
			defaultFileType = "CSV";
		else
			defaultFileType = supportFileTypesCsv.split(",")[0];
	}

%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 600 );
			resetForm( frmMain );
			focusForm( frmMain, frmMain.file );
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" enctype="multipart/form-data" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<input type='hidden' name='mode' value='up'/>
			<mtl:hidden id="record" key="organizationCode" />

			<%
				if( hiddenKeys != null ) {
					for( int k = 0; k < hiddenKeys.length; k++ ) {
						Object value = recordMap.get( hiddenKeys[k] );
						if( value != null )
							out.println( "<input type='hidden' name='"+ hiddenKeys[k] +"' value='"+ HtmlUtility.toScriptString(value.toString()) +"'/>" );
					}
				}
			%>

			<mtl:contentGroup groupId="upload" type="content">
				<div class='info-table'>
				<% if( pageContext.findAttribute("uploadTypeList") != null ) { %>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="uploadType" mandatory="true" descriptionKey="jsp.pub_upload_input.FIELD_UPLOADTYPE"/></div>
							<div class='field'>
								<mtl:select id="record" key="uploadType" mandatory="true" hasBlank="false"
										listId="uploadTypeList" listCodeKey="code" listNameFormat="$H{name}" searchable="false"/>
							</div>
						</div>
					</div>
				<% } %>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="fileType" mandatory="true" descriptionKey="jsp.pub_upload_input.FIELD_FILETYPE"/></div>
							<div class='field'>
								<mtl:radio id="record" key="fileType" mandatory="true" prefixKey="PUB_FILEFORMAT_"
										codeValues="<%=supportFileTypesCsv%>" defaultValue="<%=defaultFileType%>"/>
							</div>
						</div>
					</div>
					<mtl:contains id="record" key="uploadOption">
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="uploadOption" mandatory="true" descriptionKey="jsp.dpr_plantitem_upload.FIELD_OPTION"/></div>
							<div class='field'>
								<mtl:radio id="record" key="uploadOption" mandatory="true" defaultValue="UPD"
										prefixKey="jsp.dpr_plantitem_upload.OPTION_" codeValues="UPD,DEL"/>
							</div>
						</div>
					</div>
					</mtl:contains>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="encoding" mandatory="true" descriptionKey="FIELD_ENCODING"/></div>
							<div class='field'>
								<mtl:select id="record" key="encoding" mandatory="true" hasBlank="false"
										prefixKey="PUB_ENCODING_" codeValues="<%=supportedEncodingsCsv%>" searchable="false"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="file" mandatory="true" descriptionKey="jsp.pub_upload_input.FIELD_FILE"/></div>
							<div class='field'>
								<input type='file' name='file' class='input-field'/>
							</div>
						</div>
					</div>
				</div>

				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="return"/>
						<mtl:button type="save"/>
						<mtl:button type="reset"/>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>
					if( !Field.checkMandatory(frmMain.file) ) return false;

					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
