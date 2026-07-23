<%--
	File Name:	dpr_plantitem_upload.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2017/02/28		2.2.0	create
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
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" enctype="multipart/form-data" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<input type='hidden' name='mode' value='up'/>
			<input type='hidden' name='organizationCode' value='<%= recordMap.get("organizationCode") %>'/>
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
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="autoRetry"/></div>
							<div class='field'>
								<mtl:select id="record" key="autoRetry" prefixKey="DPR_STOCK_QUERY_AUTORETRY_" codeValues="Y,N" hasBlank="false"/>
							</div>
						</div>
					</div>

			<% if( pageContext.findAttribute("uploadTypeList") != null ) { %>
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject0' width='25%'><mtl:title key="uploadType" mandatory="true"
							descriptionKey="jsp.pub_upload_input.FIELD_UPLOADTYPE"/></td>
					<td class='content0' width='75%'><mtl:select id="record" key="uploadType" mandatory="true"
							listId="uploadTypeList" listCodeKey="code" listNameFormat="$H{name}"/></td>
				</tr>
				</table>
			<% } %>

				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject0' width='25%'><mtl:title key="fileType" mandatory="true"
							descriptionKey="jsp.pub_upload_input.FIELD_FILETYPE"/></td>
					<td class='content0' width='75%'><mtl:radio id="record" key="fileType" mandatory="true" prefixKey="PUB_FILEFORMAT_"
							codeValues="CSV,TAB" defaultValue="CSV"/></td>
				</tr>
				</table>

				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject0' width='25%'><mtl:title key="option" mandatory="true" descriptionKey="jsp.dpr_plantitem_upload.FIELD_OPTION"/></td>
					<td class='content0' width='75%'>
						<mtl:radio id="record" key="uploadType" mandatory="true" defaultValue="UPD"
							prefixKey="jsp.dpr_plantitem_upload.OPTION_" codeValues="UPD,DEL"/>
					</td>
				</tr>
				</table>

				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject0' width='25%'><mtl:title key="encoding" mandatory="true" descriptionKey="FIELD_ENCODING"/></td>
					<td class='content0' width='75%'><mtl:select id="record" key="encoding" mandatory="true" prefixKey="PUB_ENCODING_"
							codeValues="EUC-KR,UTF8,UTF16"/></td>
				</tr>
				</table>

				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject0' width='25%'><mtl:title key="file" mandatory="true" descriptionKey="jsp.pub_upload_input.FIELD_FILE"/></td>
					<td class='content0' width='75%'><input type='file' name='file' onChange='JavaScript:Field.modified(this);'
							class='content_m length_60'/></td>
				</tr>
				</table>
			</mtl:contentGroup>

			<table class='btn_page' cellspacing='0' cellpadding='0'>
			<tr><td>
				<mtl:button type="return" styleClass="btn_page"/>
				<mtl:button type="submit" styleClass="btn_page"/>
				<mtl:button type="reset" styleClass="btn_page"/>
				<mtl:button type="close_if" styleClass="btn_page"/>
			</td></tr>
			</table>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>
					if( !frmMain.file.value ) {
						customPopup.alert( { "header" : "<mtl:message key="jsp.pub_upload_input.MSG_ENTER_FILE"/>" } );
						frmMain.file.focus();
						return false;
					}

					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
