<%--
	File Name:	dpr_order_remark_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	lsinji		2008/09/26		2.2.0	create
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
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 800 );

			resetForm( frmMain );
			focusForm( frmMain, frmMain.title );
		}

        function modifyReq() {
            var url = "<%= systemConfig.getClassURL() %>/DPROrderRemark?mode=imod";
			url = replaceQueryValue( url, "orderKey", "<mtl:value id="record" key="orderKey"/>" );

			windowSelfOpen( url );
        }

	</script>
</head>

<body>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
		<%@ include file="include_rbm_form.inc" %>
		<mtl:hidden id="request" key="orderKey"/>
		<mtl:hidden id="record" key="orderNumber"/>
		<mtl:hidden id="record" key="organizationCode"/>
		<mtl:hidden id="record" key="distributionChannelCode"/>
		<mtl:hidden id="record" key="divisionCode"/>
		<mtl:hidden id="record" key="soldPartyCode"/>
		<mtl:hidden id="record" key="shipPartyCode"/>

		<mtl:contentGroup groupId="country" type="content" descriptionKey="jsp.GRP_DPR_COUNTRY_INFO">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_DPR_ORDER_ORDERNUMBER"/></td>
				<td class='content2'><mtl:value id="record" key="orderNumber"/></td>
				<td class='subject'><mtl:message key="MSG_PUB_SALES_ORGANIZATION"/></td>
				<td class='content2'><mtl:valuef id="record" format="$S{[:organizationCode;$S{] :organizationName}}"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_DPR_ORDER_ORDERDATE"/></td>
				<td class='content2'><mtl:value id="record" key="orderDate"/></td>
				<td class='subject'><mtl:message key="FIELD_DPR_ORDER_CONFIRMINDATE"/></td>
				<td class='content2'><mtl:value id="record" key="inDateConfirm"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_DPR_ORDER_SOLDPARTYCODE"/></td>
				<td class='content1'><mtl:valuef id="record" format="$S{[:soldPartyCode;$S{] :soldPartyName}}"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:message key="FIELD_DPR_ORDER_SOLDPARTYCODE"/></td>
				<td class='content1'><mtl:valuef id="record" format="$S{[:shipPartyCode;$S{] :shipPartyName}}"/></td>
			</tr>
			</table>

			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr>
				<td class='subject'><mtl:title key="remark" mandatory="true"/></td>
				<td class='content1'><mtl:textarea id="record" key="remark" rows="2" styleClass="length_full" mandatory="true"/></td>
			</tr>
			</table>
		</mtl:contentGroup>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return" styleClass="btn_page"/>
			<% if( sessionMng.isAuthorized("DPR", "DPROrder.RMK.MNG") ) { %>
				<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
					<mtl:button type="submit" styleClass="btn_page"/>
					<mtl:button type="reset" styleClass="btn_page"/>
				<% } else  { %>
					<mtl:button type="modify" styleClass="btn_page"/>
				<% } %>
			<% } %>
			<mtl:button type="close_if" href="javascript:windowClose(true)" styleClass="btn_page"/>
		</td></tr>
		</table>

		<script type='text/javascript'>
			function checkInput() {
				if( !Field.checkMandatory(frmMain.remark) ) return false;

				return submitInput();
			}

			function resetInput() {
				frmMain.reset();

				resetForm( frmMain );
			}
		</script>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
