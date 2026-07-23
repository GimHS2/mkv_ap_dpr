<%--
	File Name:	dpr_upload_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.1	Upload Option Ãß°¡
	guksm		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );

	boolean isDetailLog = ((java.util.Map)request.getAttribute("record_log") != null ? true : false);
%>

<mtl:html errorPage="error.jsp">
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		function downloadReq() {
			requestOne( "<%= htmlpage.getRequestURL() %>?mode=down", frmMain.listcheckbox, "uploadCode,uploadType", null, true );
		}

		function detailReq( uploadCode, type ) {
			var url = "<%= htmlpage.getRequestURL() %>?mode=list";
			url = replaceQueryValue( url, "uploadCode", uploadCode );
			url = replaceQueryValue( url, "type", type );

			windowSelfOpen( url, getLocationURL() );
		}

		function logListLink( selectObj, name ) {
			var url = getLocationURL();
			if( url == null ) {
				url = attachDefaultParameter( "<%= htmlpage.getRequestURL() %>" );
				url = replaceQueryValue( url, "uploadCode", "<mtl:value id="condition" key="uploadCode"/>" );
				url = replaceQueryValue( url, "type", "<mtl:value id="condition" key="uploadType"/>" );
			}
			url = replaceQueryValue( url, name ? name : selectObj.name, encodeURIComponent(selectObj.value) );
			var args = logListLink.arguments;
			if( args.length > 2 )
				for( var i = 2; i < args.length; i++ )
					url = replaceQueryValue( url, args[i], null );
			url = replaceQueryValue( url, "skip", null );
			url = replaceQueryValue( url, "focus", selectObj.name );

			window.open( url, "_self" );
		}

	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>

	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:containsElse id="record_log" copyId="_record">
	<%@ include file="include_dpr_upload_cond.inc" %>
	</mtl:containsElse>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<%@ include file="include_rbm_form.inc" %>
		<mtl:contains id="record_log" copyId="_record">
			<mtl:contentGroup groupId="info" type="search">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:message key="FIELD_RBM_UPLOADLOG_UPLOADTYPE"/></div>
							<div class='field-info'><mtl:valuef id="_record" format="${DPR_UPLOAD_@uploadType}" /></div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:message key="FIELD_RBM_UPLOADLOG_DATETIME"/></div>
							<div class='field-info'><mtl:value id="_record" key="uploadDateTime"/></div>
						</div>
						<div class='cell'>
						<% if( sessionMng.isAuthorized("DPR", "DPRUpload.OPT") ) { %>
							<div class='field-title'><mtl:message key="FIELD_DPR_UPLOAD_UPLOADOPTION"/></div>
							<div class='field-info'><mtl:valuef id="_record" format="${DPR_UPLOAD_OPTION_@uploadOption}" /></div>
						<% } %>
						</div>
						<div class='cell'></div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:message key="FIELD_RBM_UPLOADLOG_MESSAGE"/></div>
							<div class='field-info'>
							<% if( sessionMng.isAuthorized("DPR", "DPRUpload.OPT") ) { %>
								<mtl:valuef id="_record" format="%{MSG_DPR_UPLOAD_MESSAGE_${status},${rowCount,0},$f{eval(rowCount-errorCount,NF.INTEGER)},${errorCount},${ignoreCount}}"/>
							<% } else { %>
								<mtl:valuef id="_record" format="%{MSG_DPR_UPLOAD_MESSAGE_${status},${rowCount,0},$f{eval(rowCount-errorCount,NF.INTEGER)},${errorCount}}"/>
							<% } %>
							</div>
						</div>
						<div class='cell'>
							<mtl:contains id="_record" key="fileName">
							<div class='field-title'><mtl:message key="FIELD_RBM_UPLOADLOG_UPLOADFILENAME"/></div>
							<div class='field-info'><mtl:value id="_record" key="fileName"/></div>
							</mtl:contains>
						</div>
						<div class='cell'></div>
						<div class='cell'></div>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:contains>

		<mtl:contentGroup groupId="list" type="list">
			<mtl:contains id="record_log" copyId="_record">
				<div class='list-menu'>
				<%
					String statusValues;
					if( "SSL".equals(request.getParameter("type")) && sessionMng.isAuthorized("DPR", "DPRUpload.OPT") ) {
						statusValues = "RD,CO,IG,ER";
					} else {
						statusValues = "RD,CO,ER";
					}
				%>
					<mtl:select id="condition" key="status" prefixKey="DPR_UPLOAD_STATUS_" codeValues="<%= statusValues %>" searchable="false"
							hasBlank="true" nullValueKey="MSG_PUB_SELECT@FIELD_RBM_UPLOADLOG_LINESTATUS" modified="logListLink(this);" width="auto"/>
				</div>
			</mtl:contains>

			<%
				com.irt.custom.ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( listwriter.containsData() && !isDetailLog ) { %>
					<mtl:button type="download"/>
				<% } %>
					<mtl:button type="close_if"/>
				</div>
				<div id='list_page' class='page'>
					<mtl:message key="jsp.MSG_PAGEINDEX_LOADING"/>
				</div>
			</div>
		</mtl:contentGroup>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
