<%--
	File Name:	dpr_itemstatus_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2013/11/30		2.2.0	create
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

	<script type='text/javascript'>
		$.fn.__tabs = $.fn.tabs;
		$.fn.tabs = function (a, b, c, d, e, f) {
			var base = location.href.replace(/#.*$/, '');
			$('ul>li>a[href^="#"]', this).each(function () {
				var href = $(this).attr('href');
				$(this).attr('href', base + href);
			});
			$(this).__tabs(a, b, c, d, e, f);
		};

		$(function() {
			$("#tabs-main").tabs( {
				classes: {
					"ui-tabs": "tabs-main",
					"ui-tabs-nav": "tabs-nav",
					"ui-tabs-tab": "tabs-tab",
				}
			});
		});

		function modifyMaterialStatusReq( materialStatusType ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRItem?mode=imodstatus";

			url = replaceQueryValue( url, "countryCode", "<mtl:value id="record" key="countryCode"/>" );
			url = replaceQueryValue( url, "itemCode", "<mtl:value id="record" key="itemCode"/>" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="record" key="organizationCode"/>" );
			if( materialStatusType == "chain" ) {
				url = replaceQueryValue( url, "distributionChannelCode", $(frmMain.distributionChannelCode2).val() );
			} else {
				url = replaceQueryValue( url, "distributionChannelCode", $(frmMain.distributionChannelCode1).val() );
			}
			url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );

			windowSelfOpen(url);
		}

		function modifyMaterialStatus( materialStatusType ) {
			var url = "<%=systemConfig.getClassURL()%>/DPRItem?mode=modstatus";
			if( materialStatusType == "chain" ) {
				if( !Field.checkMandatory(frmMain.chainStatusFrom) ) return false;

				url = replaceQueryValue(url, "mstype", "chain");
				url = replaceQueryValue(url, "countryCode",
						"<mtl:value id="record" key="countryCode"/>");
				url = replaceQueryValue(url, "itemCode",
						"<mtl:value id="record" key="itemCode"/>");
				url = replaceQueryValue(url, "distributionChannelCode",
						"<mtl:value id="record" key="distributionChannelCode"/>");
				url = replaceQueryValue(url, "chainStatus",
						frmMain.chainStatus.value);
				url = replaceQueryValue(url, "chainStatusFrom",
						frmMain.chainStatusFrom.value);
				windowSelfOpen(url);
			} else {
				if( !Field.checkMandatory(frmMain.salesStatusFrom) ) return;

				url = replaceQueryValue(url, "mstype", "sales");
				url = replaceQueryValue(url, "countryCode",
						"<mtl:value id="record" key="countryCode"/>");
				url = replaceQueryValue(url, "itemCode",
						"<mtl:value id="record" key="itemCode"/>");
				url = replaceQueryValue(url, "distributionChannelCode",
						"<mtl:value id="record" key="distributionChannelCode"/>");
				url = replaceQueryValue(url, "salesStatus",
						frmMain.salesStatus.value);
				url = replaceQueryValue(url, "salesStatusFrom",
						frmMain.salesStatusFrom.value);
				windowSelfOpen(url);
			}
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<mtl:hidden id="record" key="itemCode"/>
			<%@ include file="include_rbm_form.inc" %>
			<%@ include file="include_pub_calendar.inc" %>
			<mtl:contentGroup groupId="main" type="tabpane" styleClass="tabpane none-card">
				<div id='tabs-main'>
					<ul style="margin-bottom: 25px;">
						<li><a href='#tabs-sales' class='tabs-title'><mtl:message key="jsp.dpr_itemstatus_input.FIELD_DPR_ITEM_SALESSTATUS"/></a></li>
						<li><a href='#tabs-dchain' class='tabs-title'><mtl:message key="jsp.dpr_itemstatus_input.FIELD_DPR_ITEM_CHAINSTATUS"/></a></li>
					</ul>
					<div id='tabs-sales'>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
									</div>
									<div class='field'>
										<mtl:valuef id="record" key="organizationCode" format="$S{[:organizationCode;]}"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE" mandatory="true"/></div>
									<div class='field'>
										<mtl:select id="record" key="distributionChannelCode" uniqId="distributionChannelCode1" mandatory="true" searchable="false"
												listId="distributionChannels" listCodeKey="distributionChannelCode"
												listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="modifyMaterialStatusReq( \"sales\" );"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:message key="jsp.dpr_orderitem_tree.FIELD_MATERIAL"/>
									</div>
									<div class='field'>
										<mtl:valuef id="record" format="$f{pure(itemCode)}"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:message key="jsp.dpr_itemstatus_input.FIELD_DPR_ITEM_SALESSTATUS_FROM"/>
									</div>
									<div class='field'>
										<mtl:date id="record" key="salesStatusFrom" mandatory="true"/>
										<mtl:message key="jsp.dpr_itemstatus_input.MSG_MATERIALSTATUS_NULLSTRING"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:message key="jsp.dpr_itemstatus_input.FIELD_DPR_ITEM_SALESSTATUS"/>
									</div>
									<div class='field'>
										<mtl:select id="record" key="salesStatus" listId="materialStatusNames" listCodeKey="statusCode" listNameFormat="[$H{statusCode}] $H{statusDescription}"
												hasBlank="true" nullValueKey="jsp.dpr_itemstatus_input.MSG_SELECT_SALESSTATUS_CODE" searchable="false"/>
									</div>
								</div>
							</div>

						</div>
						<div class='info-bottom'>
							<div class='table-cell info-button'>
								<mtl:button type="close_if"/>
								<mtl:button type="return"/>
								<mtl:button type="button" icon="images/ico_save_white.png" styleClass="primary" onClick="JavaScript:modifyMaterialStatus( \"sales\" );" messageKey="jsp.BTN_SAVE"/>
							</div>
						</div>
					</div>

					<div id='tabs-dchain'>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
									</div>
									<div class='field'>
										<mtl:valuef id="record" key="organizationCode" format="$S{[:organizationCode;]}"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE" mandatory="true"/></div>
									<div class='field'>
										<mtl:select id="record" key="distributionChannelCode" uniqId="distributionChannelCode2" mandatory="true" searchable="false"
												listId="distributionChannels" listCodeKey="distributionChannelCode"
												listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="modifyMaterialStatusReq( \"chain\" );"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:message key="jsp.dpr_orderitem_tree.FIELD_MATERIAL"/>
									</div>
									<div class='field'>
										<mtl:valuef id="record" format="$f{pure(itemCode)}"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:message key="jsp.dpr_itemstatus_input.FIELD_DPR_ITEM_CHAINSTATUS_FROM"/>
									</div>
									<div class='field'>
										<mtl:date id="record" key="chainStatusFrom" mandatory="true"/>
										<mtl:message key="jsp.dpr_itemstatus_input.MSG_MATERIALSTATUS_NULLSTRING"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
										<mtl:message key="jsp.dpr_itemstatus_input.FIELD_DPR_ITEM_CHAINSTATUS"/>
									</div>
									<div class='field'>
										<mtl:select id="record" key="chainStatus" listId="materialStatusNames" listCodeKey="statusCode" listNameFormat="[$H{statusCode}] $H{statusDescription}"
											hasBlank="true" nullValueKey="jsp.dpr_itemstatus_input.MSG_SELECT_CHAINSTATUS_CODE" searchable="false"/>
									</div>
								</div>
							</div>
						</div>
						<div class='info-bottom'>
							<div class='table-cell info-button'>
								<mtl:button type="close_if"/>
								<mtl:button type="return"/>
								<mtl:button type="button" icon="images/ico_save_white.png" styleClass="primary" onClick="JavaScript: modifyMaterialStatus( \"chain\" );" messageKey="jsp.BTN_SAVE"/>
							</div>
						</div>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:form>
	</div>
</body>
</mtl:html>
