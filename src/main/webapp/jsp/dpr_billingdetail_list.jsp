<%--
	File Name:	dpr_billingdetail_list.jsp
	Version:	2.0.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	GimHS		2011/04/29		2.0.1	vatNumber  항목 추가
	lsinji		2008/09/26		2.0.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.data.cols.*, com.irt.html.*, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	java.util.Map headerMap = (java.util.Map)pageContext.findAttribute( "header" );
	String status = null;
	if( headerMap != null && headerMap.containsKey("status") )
		status = com.irt.data.Record.extractString( headerMap, "status" );
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<script type='text/javascript'>
		var windowWidth = 1000;
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<%@ include file="include_rbm_form.inc" %>
		<%@ include file="include_pub_list.inc" %>
		<mtl:form name="frmMain" fieldSetId="headerFieldSet" onSubmit="JavaScript: return null;">
			<mtl:contentGroup groupId="billing_header" type="content">
				<div class='info-table table-fixed'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:message key="FIELD_DPR_ORDERBILLING_NUMBER"/></div>
							<div class='field-info'><mtl:value id="header" key="billingNumber"/></div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:message key="FIELD_DPR_ORDERBILLING_BILLINGDATE"/></div>
							<div class='field-info'><mtl:value id="header" key="billingDate"/></div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:message key="FIELD_DPR_ORDERBILLING_VATNUMBER"/></div>
							<div class='field-info'><mtl:value id="header" key="vatNumber"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:message key="FIELD_DPR_ORDERBILLING_INVOICEVALUE"/></div>
							<div class='field-info'><mtl:valuef id="header" format="${invoiceValue#NF.CURRENCY}"/></div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:message key="FIELD_DPR_ORDER_ORDERNUMBER"/></div>
							<div class='field-info'><mtl:value id="header" key="orderNumber"/></div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:message key="FIELD_DPR_ORDER_SOLDPARTYCODE"/></div>
							<div class='field-info'><mtl:valuef id="header" format="$S{[:soldPartyCode;$S{]:soldPartyName}}"/></div>
						</div>
					</div>
				</div>
			</mtl:contentGroup>

			<mtl:contentGroup groupId="list" type="list" styleClass="list_content none-card">
				<div class='list-menu align-right'>
					<mtl:select id="request" key="ftype" prefixKey="jsp.dpr_order_input.MSG_FORMATTYPE_" codeValues="PC,DZ"
							customOption="smallSelectmenuOptions" modified="listLink(this, null, \"sort\");" width="auto" searchable="false"/>
				</div>

				<%
					ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage );
					listWriter.setNumbering( false );
					listWriter.print( out );
				%>

				<div class='info-table table-fixed' style='margin-top: 20px;'>
					<div class='row'>
						<div class='cell field-title'><mtl:message key="FIELD_DPR_ITEMUOM_VOLUME"/></div>
						<div class='cell field-info'><mtl:valuef id="summary" format="$S{volume#NF.FLOAT2;$S{(:volumeUnit;)},N/A}"/></div>
						<div class='cell field-title'><mtl:message key="jsp.dpr_billing_detail.FIELD_NETAMOUNT"/></div>
						<div class='cell field-info'><mtl:valuef id="summary" format="${billingNetAmount#NF.CURRENCY,N/A}"/></div>
					</div>
					<div class='row'>
						<div class='cell field-title'><mtl:message key="FIELD_DPR_ITEMUOM_WEIGHT"/></div>
						<div class='cell field-info'><mtl:valuef id="summary" format="$S{weight#NF.FLOAT2;$S{(:weightUnit;)},N/A}"/></div>
						<div class='cell field-title'><mtl:message key="jsp.dpr_billing_detail.FIELD_VAT"/></div>
						<div class='cell field-info'><mtl:valuef id="summary" format="${billingTax#NF.CURRENCY,N/A}"/></div>
					</div>
					<div class='row'>
						<div class='cell field-title'></div>
						<div class='cell field-info'></div>
						<div class='cell field-title'><mtl:message key="jsp.dpr_billing_detail.FIELD_DAMAGEDDISCOUNT"/></div>
						<div class='cell field-info'><mtl:valuef id="summary" format="${billingDamagedDiscount#NF.CURRENCY}"/></div>
					</div>
					<div class='row'>
						<div class='cell field-title'></div>
						<div class='cell field-info'></div>
						<div class='cell field-title'><mtl:message key="jsp.dpr_billing_detail.FIELD_TOTAL"/></div>
						<div class='cell field-info'><mtl:valuef id="summary" format="${billingValue#NF.CURRENCY}"/></div>
					</div>
				</div>
				<div class='list-function' style='margin-top: 0;'>
					<div class='button'>
						<mtl:button type="return"/>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:form>
	</div>
</body>
</mtl:html>
