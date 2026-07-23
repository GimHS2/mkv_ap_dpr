<%--
	File Name:	dpr_itemean_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2017/06/30		2.2.0	create
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
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
</head>
<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_pub_calendar.inc" %>
		<%@ include file="include_rbm_bodyheader.inc" %>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>

			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="organizationCode"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="organizationCode" format="$S{[:organizationCode;]$S{ :organizationName}}"/>
								<mtl:hidden id="record" key="organizationCode"></mtl:hidden>
								<mtl:hidden id="record" key="organizationName"></mtl:hidden>
							<% } else { %>
								<mtl:select id="record" key="organizationCode" mandatory="true" hasBlank="true"
										nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemCode"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:text id="record" key="itemCode" readonly="true"/>
							<% } else { %>
								<mtl:text id="record" key="itemCode" readonly="false"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="mapEffectFrom"/></div>
							<div class='field'>
							<% if( "imod".equals(htmlpage.getMode()) ) { %>
								<mtl:hidden id="record" key="mapEffectFrom" name="mapEffectFrom"/>
								<mtl:text id="record" key="mapEffectFrom" name="mapEffectFrom_newpk" readonly="false"/>
							<% } else { %>
								<mtl:text id="record" key="mapEffectFrom"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="mapEanCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="mapEanCode" readonly="false"/>
							</div>
						</div>
					</div>
					<div class='info-bottom'>
						<div class='table-cell info-button'>
							<mtl:button type="close_if"/>
							<mtl:button type="return"/>
						<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
							<mtl:button type="reset"/>
							<mtl:button type="save"/>
						<% } %>
						</div>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>
					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>

