<%--
	File Name:	dpr_packdealitem_input.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/04/30		2.2.1	packdeal tolrate 100허용하여 제한없이 발주하도록 설정.
	jbaek		2019/06/30		2.2.0	create
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
			<mtl:hidden id="property" key="organizationCode"/>
			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="dealCode" descriptionKey="FIELD_DPR_PACKDEAL_CODE" mandatory="true"/></div>
							<div class='field'>
								<mtl:text id="record" key="dealCode" readonly="true"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE" mandatory="true"/></div>
							<div class='field'>
								<mtl:text id="property" key="distributionChannelCode" readonly="true"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="officeCode" descriptionKey="FIELD_DPR_PARTY_SALESOFFICE_CODE" mandatory="true"/></div>
							<div class='field'>
								<mtl:text id="property" key="officeCode" readonly="true"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="groupCode" descriptionKey="FIELD_DPR_PARTY_SALESGROUP_CODE" mandatory="true"/></div>
							<div class='field'>
								<mtl:text id="property" key="groupCode" readonly="true"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="partyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE" mandatory="true"/></div>
							<div class='field'>
								<mtl:text id="property" key="partyCode" readonly="true"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemCode"/></div>
							<div class='field'>
								<mtl:hidden id="record" key="tmp_itemCode"/>
								<mtl:text id="record" key="itemCode"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="packdealDiscountRate"/></div>
							<div class='field'>
								<mtl:text id="record" key="packdealDiscountRate"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="pdMoqDay" descriptionKey="FIELD_DPR_MOQITEM_PACKDEALMOQ_DAY"/></div>
							<div class='field'>
								<mtl:text id="record" key="pdMoqDay"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="pdMoqMonth" descriptionKey="FIELD_DPR_MOQITEM_PACKDEALMOQ_MONTH"/></div>
							<div class='field'>
								<mtl:text id="record" key="pdMoqMonth"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="save"/>
						<mtl:button type="reset"/>
					<% } else if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION ) { %>
						<mtl:button type="modify"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>

					if( !Field.checkNumberRange( frmMain.packdealDiscountRate, false, -1, 101 ) ) return false;
					if( !Field.checkNumberFormat(frmMain.pdMoqDay, true, true) ) return false;
					if( !Field.checkNumberFormat(frmMain.pdMoqMonth, true, true) ) return false;

					var d = new Number(frmMain.pdMoqDay.value);
					var m = new Number(frmMain.pdMoqMonth.value);
					if( ( d > m ) ) {
						customPopup.alert( { "header" : "<mtl:message key="ERR_MOQITEMVALUE_SHOULD_DAY_LT_MONTH" encodeScript="true"/>" } );
						return false;
					}

					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
