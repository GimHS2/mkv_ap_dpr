<%--
	File Name:	dpr_order_upload.jsp
	Version:	2.2.3

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/05/30		2.2.3	Warn Message 표시 추가, PackDeal 추가
	hankalam	2017/02/28		2.2.2	PartyCode 속성 받아오도록 수정
	jbaek		2014/11/30		2.2.1	오더 업로드포맷을 xls 형식으로 변경.
	lsinji		2009/06/30		2.2.0	create
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

System.err.println( recordMap );
	String status = (String)recordMap.get( "status" );
	java.util.Collection ex_warns = (java.util.Collection)pageContext.findAttribute( "ex_warns" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<style type='text/css'><!--
		table.warn_content li {
			border-bottom: 1px dotted #BBBBBB;
			margin-bottom: 4px; padding-top: 5px; padding-bottom: 0px;
		}
		table.warn_content td.warn {
			font-size: 12px; font-weight: bold; color: #B90000;
			background: #F0F0F0 url(images/error_bg.gif) fixed no-repeat left bottom;
			min-height: 130px;
		}
		table.warn_content ul {
			vertical-align: middle;
			margin-left: 80px; margin-right: 20px; padding-top: 5px;
		}
	//--></style>
	<script type='text/javascript'>
		function bodyLoad() {
			<% if( "CO".equals(status) && ex_warns == null ) { %>
				frameReload();
			<% } %>
		}

		function frameReload() {
			parent.$(".sub-content-wrap").hide();
			parent.$(".sub-content-wrap").css( "width", "" );
			parent.$(".content-overlay").fadeOut();
			parent.window.main_content.location.reload();
		}

		function uploadClose() {
		<% if( ex_warns != null ) { %>
			frameReload();
		<% } else { %>
			parent.$(".sub-content-wrap").hide();
			parent.$(".sub-content-wrap").css( "width", "" );
			parent.$(".content-overlay").fadeOut();
		<% } %>
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>

	<% if( ex_warns != null ) { %>
		<table class='warn_content' cellspacing='0' cellpadding='0' style='table-layout: fixed;'>
		<tr><td class='warn'><ul>
			<%
				if( ex_warns != null ) {
					for( java.util.Iterator iterator = ex_warns.iterator(); iterator.hasNext(); ) {
						com.irt.data.DataException dataEx = (com.irt.data.DataException)iterator.next();

						out.print( "<li>" );
						if( dataEx.getLineNumber() >= 0 )
							out.print( "[WARN] " + "Line "+ dataEx.getLineNumber() +" : " );
						out.print( HtmlUtility.toHtmlString(dataEx.getMessage()) );
						out.println( "</li>" );
					}
				}
			%>

			<mtl:loop id="warns" loopId="loop" loopIndex="index">
				<li><mtl:value id="loop" key="name"/> -&gt; <mtl:value id="loop" key="message"/></li>
			</mtl:loop>
		</ul></td></tr>
		</table>
		<% } %>

		<mtl:form name="frmMain" enctype="multipart/form-data" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<input type='hidden' name='mode' value='up'/>
			<mtl:hidden id="request" key="uploadType"/>
			<mtl:hidden id="record" key="orderKey"/>
			<mtl:hidden id="record" key="dealCode"/>
			<mtl:hidden id="record" key="partyCode"/>
			<mtl:hidden id="record" key="organizationCode"/>
			<mtl:hidden id="record" key="distributionChannelCode"/>
			<mtl:hidden id="record" key="divisionCode"/>
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
							<div class='field-title'><mtl:title key="uploadType" mandatory="true" descriptionKey="jsp.pub_upload_input.FIELD_UPLOADTYPE"/></div>
							<div class='field'><mtl:valuef id="request" format="${DPR_UPLOAD_@uploadType}"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="headerInd" mandatory="true" descriptionKey="jsp.dpr_upload_input.FIELD_DPR_UPLOAD_HEADER"/></div>
							<div class='field'>
								<mtl:radio id="record" key="headerInd" mandatory="true" prefixKey="PUB_WHETHER_"
										codeValues="Y,N" defaultValue="Y"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="file" mandatory="true" descriptionKey="jsp.pub_upload_input.FIELD_FILE"/></div>
							<div class='field'><input type='file' name='file' class='input-field'/></div>
						</div>
					</div>
				</div>
			</mtl:contentGroup>

			<div class='info-bottom'>
				<div class='table-cell info-button'>
					<mtl:button type="return"/>
					<mtl:button type="save"/>
					<mtl:button type="reset"/>
					<mtl:button type="close_if" onClick="JavaScript: uploadClose();"/>
				</div>
			</div>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>
					if( !Field.checkMandatory(frmMain.file) ) return false;

					toggleLoading( true );
					return true;
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
