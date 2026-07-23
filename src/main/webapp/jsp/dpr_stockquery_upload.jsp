<%--
	File Name:	dpr_stockquery_upload.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.0	create
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
	String uploadStatus = (String)pageContext.findAttribute( "uploadStatus" );
	String queryKey = (String)pageContext.findAttribute( "queryKey" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			<% if( com.irt.dpr.StockQuery.STATUS_UPLOAD.equals(uploadStatus) ) { %>
				simulationReq( "<%= queryKey %>" );
			<% } %>
		}


		function simulationReq( queryKey ) {
			var url = "<%= htmlpage.getRequestURL() %>";
			url = replaceQueryValue( url, "mode", "wait" );
			url = replaceQueryValue( url, "queryKey", queryKey );

			<% if( htmlpage.getSystemMenu() != null ) { %>
			url = replaceQueryValue( url, "menu", "<%= htmlpage.getSystemMenu() %>" );
			<% } %>
			url = replaceQueryValue( url, "locale", "<mtl:value id="request" key="locale"/>" );

			if( parent.$("#sub-content").length ) {
				parent.location.href = url;
				windowClose();
			} else {
				windowSelfOpen( url );
			}
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" enctype="multipart/form-data" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<mtl:hidden id="property" key="vtype"/>
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
										codeValues="CSV" defaultValue="CSV"/>
							</div>
						</div>
					</div>
					<div class='row'>
							<div class='cell'>
								<div class='field-title'><mtl:title key="encoding" mandatory="true" descriptionKey="FIELD_ENCODING"/></div>
								<div class='field'>
									<mtl:select id="record" key="encoding" mandatory="true" hasBlank="false" searchable="false"/>
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
