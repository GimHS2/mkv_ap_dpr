<%--
	File Name:	dpr_sales_mov_upload.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2014/07/14		2.2.1	Sold-to Level MOV 기능 개발
	song7981	2013/04/30		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Map recordMap = (Map)pageContext.findAttribute( "record" );
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
			<mtl:hidden id="request" key="uploadType"/>
			<mtl:hidden id="property" key="btype"/>
			<input type='hidden' name='mode' value='up'/>

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
							<div class='field-title'>
								<mtl:title key="organizationCode" mandatory="true" descriptionKey="MSG_PUB_SALES_ORGANIZATION"/>
							</div>
							<div class='field'>
								<mtl:select id="record" key="organizationCode" mandatory="true" searchable="false"
										listId="organizations" listCodeKey="organizationCode"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'>
								<mtl:message key="jsp.dpr_upload_input.FIELD_DPR_UPLOAD_HEADER"/>
							</div>
							<div class='field'>
								<mtl:radio id="record" key="headerInd" mandatory="true" prefixKey="PUB_WHETHER_"
										codeValues="Y,N" defaultValue="Y"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'>
								<mtl:title key="fileType" mandatory="true" descriptionKey="jsp.pub_upload_input.FIELD_FILETYPE"/>
							</div>
							<div class='field'>
								<mtl:radio id="record" key="fileType" mandatory="true"
										prefixKey="PUB_FILEFORMAT_" codeValues="CSV,TAB" defaultValue="CSV"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="encoding" mandatory="true" descriptionKey="FIELD_ENCODING"/></div>
							<div class='field'>
								<mtl:select id="record" key="encoding" mandatory="true" prefixKey="PUB_ENCODING_" codeValues="UTF8,UTF16" searchable="false"/>
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
