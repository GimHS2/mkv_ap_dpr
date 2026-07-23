<%--
	File Name:	dpr_plantitem_input.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2015/04/07		2.2.2	plantCode 를 PK에 추가
	jbaek		2014/11/30		2.2.1	updateStatus = CO 일 경우 windowClose 되도록 변경.
	jbaek		2014/02/17		2.2.0	create
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
	String updateStatus = "";
	if( recordMap != null )
		updateStatus = (String)recordMap.get( "updateStatus" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<script type='text/javascript'>
		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlantItem?mode=imod";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="record" key="organizationCode"/>")
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="record" key="distributionChannelCode"/>")
			url = replaceQueryValue( url, "officeCode", "<mtl:value id="record" key="officeCode"/>")
			url = replaceQueryValue( url, "plantCode", "<mtl:value id="record" key="plantCode"/>")
			url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="record" key="shipPartyCode"/>")
			url = replaceQueryValue( url, "itemCode", "<mtl:value id="record" key="itemCode"/>")

			windowSelfOpen( url );
		}
	</script>
</head>
<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc"%>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<input type='hidden' name='url' value="<%= htmlpage.getRequestURL() %>" />
			<mtl:hidden key="pttype" defaultValue="excl" />
			<mtl:hidden key="status" defaultValue="99" />

			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="plantCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="plantCode" readonly="false"/>
								<mtl:hidden id="record" key="plantCode_new" />
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="shipPartyCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="shipPartyCode" readonly="false" defaultValue="0"/>
								<mtl:hidden id="record" key="shipPartyCode_new" />
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="officeCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="officeCode" readonly="false" defaultValue="0"/>
								<mtl:hidden id="record" key="officeCode_new" />
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="organizationCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="organizationCode" readonly="false"/>
								<mtl:hidden id="record" key="organizationCode_new" />
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="distributionChannelCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="distributionChannelCode" readonly="false"/>
								<mtl:hidden id="record" key="distributionChannelCode_new" />
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="itemCode" readonly="false"/>
								<mtl:hidden id="record" key="itemCode_new" />
							</div>
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
					<% } else if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION ) { %>
						<mtl:button type="modify"/>
					<% } %>
					</div>
				</div>
			</mtl:contentGroup>


			<script type='text/javascript'>

				function checkInput() {
					if( !checkMandatoryInForm(frmMain) ) return false;

					<%= htmlpage.getValidationScript() %>

					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<% if( "imod".equals(htmlpage.getMode()) ) { %>
							frmMain.plantCode_new.value = frmMain.plantCode.value;
							frmMain.shipPartyCode_new.value = frmMain.shipPartyCode.value;
							frmMain.officeCode_new.value = frmMain.officeCode.value;
							frmMain.organizationCode_new.value = frmMain.organizationCode.value;
							frmMain.distributionChannelCode_new.value = frmMain.distributionChannelCode.value;
							frmMain.itemCode_new.value = frmMain.itemCode.value;

							frmMain.plantCode.value = '<mtl:value key="plantCode" id="record"/>';
							frmMain.shipPartyCode.value = '<mtl:value key="shipPartyCode" id="record"/>';
							frmMain.officeCode.value = '<mtl:value key="officeCode" id="record"/>';
							frmMain.organizationCode.value = '<mtl:value key="organizationCode" id="record"/>';
							frmMain.distributionChannelCode.value = '<mtl:value key="distributionChannelCode" id="record"/>';
							frmMain.itemCode.value = '<mtl:value key="itemCode" id="record"/>';
						<% } %>
						<% if( "ireg".equals(htmlpage.getMode()) ) { %>
							frmMain.plantCode_new.value = frmMain.plantCode.value;
							frmMain.shipPartyCode_new.value = frmMain.shipPartyCode.value;
							frmMain.officeCode_new.value = frmMain.officeCode.value;
							frmMain.organizationCode_new.value = frmMain.organizationCode.value;
							frmMain.distributionChannelCode_new.value = frmMain.distributionChannelCode.value;
							frmMain.itemCode_new.value = frmMain.itemCode.value;
						<% } %>
					<% } %>

					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
