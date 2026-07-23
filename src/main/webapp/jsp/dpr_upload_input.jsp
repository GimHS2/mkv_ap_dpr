<%--
	File Name:	dpr_upload_input.jsp
	Version:	2.2.5

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.5	신규 UI/UX 적용
	hankalam	2020/12/31		2.2.4	upload option 기능 추가
	hankalam	2019/08/31		2.2.3	download(): Selling SKU Upload 시 China Upload 양식 구분
	jbaek		2015/04/30		2.2.2	Product Hierarchy Level 기능: 다운로드시 masterLangCode를 mandatory로 변경.
	jbaek		2014/09/30		2.2.1	Product Hierarchy Level 기능 개발
	guksm		2008/09/26		2.2.0	create
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
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 800 );
			focusForm( frmMain, frmMain.file );
		}

		function downloadReq() {
			var url = "<%= systemConfig.getClassURL () %>/DPRUpload?mode=down";

			var uploadType = frmMain.uploadType.value;
			if( uploadType == "PCD" ) {
				if( !Field.checkMandatory(frmMain.masterLangCode) ) return;
				url = replaceQueryValue( url, "fileType", frmMain.fileType.value );

			}
			url = replaceQueryValue( url, "masterLangCode", frmMain.masterLangCode.value );
			url = replaceQueryValue( url, "countryCode", "<mtl:value id="condition" key="countryCode"/>" );
			url = replaceQueryValue( url, "organizationCode", frmMain.organizationCode.value );
			url = replaceQueryValue( url, "uploadType", frmMain.uploadType.value );
			url = replaceQueryValue( url, "encoding", frmMain.encoding.value );

			windowSelfOpen( url );
		}

		function downloadSSLTemplateReq() {
			window.open( "<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>sample/Selling SKU List Sample.csv" );
		}
	</script>
	<script type="text/javascript">
	<%
		String supportLocale = com.irt.rbm.RBMSystem.getSystemEnv("SYS", "PartySupportLocale;"+ sessionMng.getPartyId(), "en");
		String[] locales = supportLocale.split(",");
		String langSelectCond = "";
		for( String locale : locales ) {
			langSelectCond += "&code=" + locale;
		}

	%>

	</script>
</head>

<body class='content'>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" enctype="multipart/form-data" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>
		<input type='hidden' name='mode' value='up'/>
		<mtl:hidden id="request" key="uploadType"/>
		<mtl:hidden id="record" key="sslType" defaultValue="D"/>
		<%
			if( hiddenKeys != null ) {
				for( int k = 0; k < hiddenKeys.length; k++ ) {
					Object value = recordMap.get( hiddenKeys[k] );
					if( value != null )
						out.println( "<input type='hidden' name='"+ hiddenKeys[k] +"' value='"+ HtmlUtility.toScriptString(value.toString()) +"'/>" );
				}
			}
		%>

		<mtl:contentGroup groupId="upload" type="search">
			<div id='messagebar'></div>
			<div class='info-table' style='max-width: 1300px;'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="uploadType" mandatory="true" descriptionKey="jsp.dpr_upload_input.FIELD_DPR_UPLOAD_TYPE"/></div>
						<div class='field'><mtl:valuef id="request" format="${DPR_UPLOAD_@uploadType}"/></div>
					</div>
					<div class='cell'>
					<mtl:ifvalue id="request" key="uploadType" valueList="SSL,PCD">
						<div class='field-title'><mtl:title key="organization" mandatory="true" descriptionKey="jsp.dpr_upload_input.FIELD_DPR_ORGANIZATION_LIST"/></div>
						<div class='field'>
							<mtl:select id="record" key="organizationCode" searchable="false"
									listId="organizations" listCodeKey="organizationCode" listNameFormat="[$f{pure(organizationCode)}] ${organizationName}"/>
						</div>
					</mtl:ifvalue>
					</div>
					<div class='cell'>
					<mtl:ifvalue id="request" key="uploadType" valueList="PCD">
					<mtl:hidden id="request" key="fileType" defaultValue="XLS"/>
						<div class='field-title'><mtl:title key="masterLangCode" mandatory="true" descriptionKey="jsp.dpr_upload_input.FIELD_DPR_MASTER_LANGUAGECODE"/></div>
						<div class='field'>
							<mtl:select id="request" key="masterLangCode" mandatory="true" className="com.irt.rbm.sys.LanguageCode" condition="<%=langSelectCond%>"
									listNameFormat="[$f{pure(code)}] ${name}" searchable="false"/>
						</div>
					</mtl:ifvalue>
					</div>
				</div>
			<% if( !"PCD".equals(request.getParameter("uploadType")) ) { %>
				<div class='row'>
				<% if( "SSL".equals(request.getParameter("uploadType")) && sessionMng.isAuthorized("DPR", "DPRUpload.OPT") ) { %>
					<div class='cell'>
						<div class='field-title'><mtl:title key="uploadOption" mandatory="true" descriptionKey="jsp.dpr_upload_input.FIELD_DPR_UPLOAD_OPTION"/></div>
						<div class='field'>
							<mtl:radio id="record" key="uploadOption" mandatory="true"
									prefixKey="DPR_UPLOAD_OPTION_" codeValues="ADD,REP" defaultValue="ADD"/>
						</div>
					</div>
				<% } %>
					<div class='cell'>
						<div class='field-title'><mtl:title key="headerInd" mandatory="true" descriptionKey="jsp.dpr_upload_input.FIELD_DPR_UPLOAD_HEADER"/></div>
						<div class='field'>
							<mtl:radio id="record" key="headerInd" mandatory="true" prefixKey="PUB_WHETHER_"
									codeValues="Y,N" defaultValue="Y"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="fileType" mandatory="true" descriptionKey="jsp.pub_upload_input.FIELD_FILETYPE"/></div>
						<div class='field'>
							<mtl:radio id="record" key="fileType" mandatory="true"
									prefixKey="PUB_FILEFORMAT_" codeValues="CSV,TAB" defaultValue="CSV"/>
						</div>
					</div>
				</div>
			<% } %>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="encoding" mandatory="true" descriptionKey="FIELD_ENCODING"/></div>
						<div class='field'>
							<mtl:select id="record" key="encoding" mandatory="true" searchable="false" prefixKey="PUB_ENCODING_" codeValues="UTF8,UTF16"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="encoding" mandatory="true" descriptionKey="jsp.pub_upload_input.FIELD_FILE"/></div>
						<div class='field'>
							<input type='file' name='file' class='input-field'/>
						</div>
					</div>
					<div class='cell'></div>
				</div>
			</div>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
					<mtl:button type="save"/>
					<mtl:button type="reset"/>
					<mtl:button type="close_if"/>
					<mtl:ifvalue id="request" key="uploadType" valueList="PCD">
						<mtl:button type="download"/>
					</mtl:ifvalue>
					<mtl:ifvalue id="request" key="uploadType" valueList="SSL">
						<mtl:button type="download" onClick="JavaScript: downloadSSLTemplateReq();" messageKey="jsp.BTN_DOWNLOAD_TEMPLATE"/>
					</mtl:ifvalue>
				</div>
			</div>
		</mtl:contentGroup>

		<script type='text/javascript'>
			function checkInput() {
				<%= htmlpage.getValidationScript() %>

				if( !frmMain.file.value ) {
					customPopup.alert( { "header" : "<mtl:message key="jsp.pub_upload_input.MSG_ENTER_FILE"/>" } );
					frmMain.file.focus();
					return false;
				}

				frmMain.url.value = encodeURIComponent( getLocationURL() );

				return submitInput();
			}
		</script>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
