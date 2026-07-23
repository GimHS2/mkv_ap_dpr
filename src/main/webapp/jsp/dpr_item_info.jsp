<%--
	File Name:	dpr_item_info.jsp
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
<%
	String countryCode = property.getProperty( "countryCode" );
	String itemCode = property.getProperty( "itemCode" );

	String IMG_PARAM = "&countryCode=" + countryCode + "&itemCode=" + itemCode;
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<style type='text/css'>
		div pre {
			font: bold 16px arial;
			text-align: center;
			padding-left: 30px; padding-right: 30px;
		}
	</style>

	<script type='text/javascript' src='script/tabpane.js'></script>
	<script type='text/javascript'>
		var tabPane;

		function bodyLoad() {
			<% if( !"frm".equals(property.get("vtype")) ) { %>
				initTabPaneForm( tabPane = new TabPane(document.all.tabpane_main) );
			<% } %>

			windowResizeTo( 900 );
		}

		function imageResize( img, maxwidth, maxheight ) {
			var vimg = new Image();
			vimg.src = img.src;
			if( vimg.width > maxwidth && vimg.width > vimg.height * maxwidth / maxheight )
				img.width = maxwidth;
			else if( vimg.height > maxheight )
				img.height = maxheight;
		}

		function deleteImage() {
			var url = "<%= systemConfig.getClassURL() %>/DPRItemImage?mode=del";

			var countryCode = "<mtl:value id="property" key="countryCode"/>";
			var itemCode = "<mtl:value id="request" key="itemCode"/>";

			url = replaceQueryValue( url, "countryCode", countryCode );
			url = replaceQueryValue( url, "itemCode", itemCode );

			url = attachDefaultParameter( url );
			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
			if( checkURLLength(url) ) {
				var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DELETE" encodeScript="true"/>" };
				customPopup.confirm( messages, function(res) {
					if( res ) {
						location.replace( url );
					}
				});
			}
		}

		function modifyDescription() {
			var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=upd";
			url = replaceQeuryValue( url, "countryCode", "<mtl:value id="property" key="countryCode"/>" );
			url = replaceQeuryValue( url, "itemCode", "<mtl:value id="request" key="itemCode"/>" );
			url = replaceQeuryValue( url, "description", encodeURIComponent(frmMain.description.value) );
			var partyCode = "<mtl:value id="record" key="partyCode"/>";
			if( partyCode )
				url = replaceQeuryValue( url, "partyCode", partyCode );

			url = attachDefaultParameter( url );

			windowSelfOpen( url );
		}

		function registImage() {
			var url = "<%= systemConfig.getClassURL() %>/DPRItemImage?mode=ireg";

			var countryCode = "<mtl:value id="property" key="countryCode"/>";
			var itemCode = "<mtl:value id="request" key="itemCode"/>";

			url = replaceQueryValue( url, "countryCode", countryCode );
			url = replaceQueryValue( url, "itemCode", itemCode );

			windowOpen( url + "&wintype=sub", "clsMng" );
		}

	</script>
</head>

<body>
	<%@ include file="include_rbm_bodyheader.inc" %>

	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return false;">

	<% if( "frm".equals(property.get("vtype")) ) { %>
		<div style='border: 5px solid #C5C5C5; margin-top: 50px; padding-bottom: 35px;'>
			<img src='images/noselect.gif' style='margin-top: -50px; margin-left: 20px;'>

			<pre id='msg'><%= property.getProperty("infomsg") %></pre>
		</div>
	<% } else  { %>
		<mtl:contentGroup groupId="main" type="tabpane" styleClass="tabpane">
<%-- 1. Item Base --%>
		<mtl:contentGroup groupId="base" type="tabpage" descriptionKey="jsp.GRP_DPR_ITEM_INFO">
			<mtl:contentGroup groupId="base0" type="fieldset" descriptionKey="jsp.GRP_DPR_ITEM_BASE_INFO">
			<table class='line_content' cellspacing='0' cellpadding='0'>
			<tr><td>
				<table class='line_content' cellspacing='0' cellpadding='0'>
	            <tr>
	                <td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_CODE"/></td>
	                <td class='content1'><mtl:valuef id="record" format="$f{pure(itemCode)}"/></td>
	            </tr>
	            </table>

	            <table class='line_content' cellspacing='0' cellpadding='0'>
	            <tr>
	                <td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_NAME"/></td>
	                <td class='content1'><mtl:value id="record" key="itemName"/></td>
	            </tr>
	            </table>

	            <table class='line_content' cellspacing='0' cellpadding='0'>
	            <tr>
	                <td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_DESCRIPTION"/></td>
	                <td class='content1'><mtl:textarea id="record" key="nvlIntro"/>
					</td>
	            </tr>
	            </table>

	            <table class='line_content' cellspacing='0' cellpadding='0'>
	            <tr>
	                <td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_NEWITEM_INDICATOR"/></td>
	                <td class='content2'><mtl:ynInput id="record" key="newItemInd" readonly="true" defaultValue="N"/></td>
	                <td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_PROMOTION_INDICATOR"/></td>
	                <td class='content2'><mtl:ynInput id="record" key="promotionInd" readonly="true" defaultValue="N"/></td>
	            </tr>
	            </table>

	            <table class='line_content' cellspacing='0' cellpadding='0'>
	            <tr>
	                <td class='subject'><mtl:message key="jsp.dpr_item_info.FILED_DPR_ITEM_PRICE"/></td>
	                <td class='content1'>
	                    <mtl:value id="record" key="price"/>
	                    <mtl:value id="record" key="priceCurrencyName"/>
	                </td>
	            </tr>
	            </table>
			</td>
			<td class='content_image' width='150'>
				<img src='<%= systemConfig.getClassURL() %>/DPRItemImage?mode=img<%=IMG_PARAM%>'
						onLoad='JavaScript:imageResize(this, 150, 150);'/>
			</td>

            </tr>
            </table>
			</mtl:contentGroup>

			<mtl:contentGroup groupId="base1" type="fieldset" descriptionKey="jsp.GRP_DPR_ITEM_HIERARCHY_INFO">
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL1_NAME"/></td>
					<td class='content3'><mtl:valuef id="record" key="productHR1Name"/>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL2_NAME"/></td>
					<td class='content3_c'><mtl:valuef id="record" key="productHR2Name"/>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL3_NAME"/></td>
					<td class='content3'><mtl:valuef id="record" key="productHR3Name"/>
				</tr>
				</table>

				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL4_NAME"/></td>
					<td class='content3'><mtl:valuef id="record" key="productHR4Name"/>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL5_NAME"/></td>
					<td class='content3_c'><mtl:valuef id="record" key="productHR5Name"/>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_ITEM_HIERARCHY_LEVEL6_NAME"/></td>
					<td class='content3'><mtl:valuef id="record" key="productHR6Name"/>
				</tr>
				</table>
			</mtl:contentGroup>

			<mtl:contentGroup groupId="base2" type="fieldset" descriptionKey="jsp.GRP_DPR_ITEM_GROUPING_INFO">
				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_BASEPRODUCT"/></td>
					<td class='content3'><mtl:value id="record" key="baseProductName"/></td>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_MEGABRAND"/></td>
					<td class='content3_c'><mtl:value id="record" key="megaBrandName"/></td>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_BRAND"/></td>
					<td class='content3'><mtl:value id="record" key="brandName"/></td>
				</tr>
				</table>

				<table class='line_content' cellspacing='0' cellpadding='0'>
				<tr>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_VARIANT"/>
					<td class='content3'><mtl:value id="record" key="variantName"/></td>
					<td class='subject'><mtl:message key="jsp.dpr_item_info.FIELD_DPR_PUTUP"/>
					<td class='content3_2'><mtl:value id="record" key="putupName"/></td>
				</tr>
				</table>
			</mtl:contentGroup>
		</mtl:contentGroup>

<%-- 2. item UOM --%>
		<mtl:contentGroup groupId="uom" type="tabpage" descriptionKey="jsp.GRP_DPR_ITEM_UOM">
			<mtl:contentGroup groupId="list" type="list">
				<table class='list_content_top' cellspacing='0' cellpadding='0' style='position: absolute; width: 0px;'>
				<tr><td class='list_content_top' align='left'>
				</td></tr>
				</table>
				<%
					ListWriter listWriter = new com.irt.custom.ListWriter( request, htmlpage, "records_uom" );
					listWriter.setCheckboxTypeAndNumbering( ListWriter.CHECKBOXTYPE_NUMBER );
					listWriter.print( out );
				%>
			</mtl:contentGroup>
		</mtl:contentGroup>
		</mtl:contentGroup>
	<% } %>

		<table class='btn_page' cellspacing='0' cellpadding='0'>
		<tr><td>
			<mtl:button type="return" styleClass="btn_page"/>
			<mtl:contains id="record">
				<% if( sessionMng.isAuthorized("DPR", "DPRItemImage.MNG") ) { %>
					<mtl:button type="registImg" imageSrc="images/btn_regist_image.gif" href="JavaScript:registImage();"
							styleClass="btn_page"/>
					<mtl:button type="deleteImg" imageSrc="images/btn_delete_image.gif" href="JavaScript:deleteImage();"
							styleClass="btn_page"/>
				<% } %>
			</mtl:contains>

			<mtl:button type="close_if" styleClass="btn_page"/>
		</td></tr>
		</table>

		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
