<%--
	File Name:	dpr_itemprice_input.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/07/30		2.2.1	item select Ãß°¡.
	song7981	2016/05/20		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.List, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>
<mtl:html errorPage="error.jsp">
<%
	Map<String, Object> record = (Map<String, Object>)request.getAttribute("record");
	String organizationCode = (String)record.get("organizationCode");
	boolean inserting = "reg".equals(htmlpage.getMode()) || "ireg".equals(htmlpage.getMode());
	boolean useDetailCondition = com.irt.dpr.Country.isFeature( organizationCode, "useDetailCondition" );
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
	List<Map<String, Object>> items = (List<Map<String, Object>>)pageContext.findAttribute( "items" );
	org.json.simple.JSONArray itemArray = null;
	if( items != null ) {
		itemArray = com.irt.util.Utility.convertListToJson( items );
	}
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		$(function() {
			var items = <%= itemArray %>;
			var codeItems = $.map( items, function(item) {
				return {
					label: item.itemCode + "|" + item.itemName,
					itemCode: item.itemCode,
					itemName: item.itemName
				};
			});

			if( $("input[name=itemCode]").length ) {
				$("input[name=itemCode]").autocomplete( {
					minLength: 2,
					source: codeItems,
					focus: function( event,ui ) { return false; },
					select: function( event, ui ) {
						$("input[name=itemCode]").val( ui.item.itemCode );
						return false;
					},
				}).autocomplete("instance")._renderItem=function( ul, item ) {
					var disp = "<div>("+ item.itemCode + ") " + item.itemName+ "</div>"
					return $("<li>").append( disp ).appendTo( ul );
				};
			}
		});

		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlantItem?mode=imod";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="record" key="organizationCode"/>")
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="record" key="distributionChannelCode"/>")
			url = replaceQueryValue( url, "officeCode", "<mtl:value id="record" key="officeCode"/>")
			url = replaceQueryValue( url, "groupCode", "<mtl:value id="record" key="groupCode"/>")
			url = replaceQueryValue( url, "partyCode", "<mtl:value id="record" key="partyCode"/>")
			url = replaceQueryValue( url, "itemCode", "<mtl:value id="record" key="itemCode"/>")

			windowSelfOpen( url );
		}
	</script>
</head>
<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_rbm_bodyheader.inc" %>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>

			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
				<% if( useDetailCondition ) { %>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="organizationCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="organizationCode" mandatory="true" listId="organizations" listCodeKey="organizationCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO;SG;SOLD\");"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="distributionChannelCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="distributionChannelCode" mandatory="true" listId="distributionChannels" listCodeKey="distributionChannelCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION"
										listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SO;SG;SOLD\");"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="officeCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
										listId="offices" listCodeKey="officeCode" listNameFormat="$S{[:officeCode;$S{] :officeName}}"
										modified="readConditionReq(\"SG;SOLD\");"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="groupCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="groupCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_GROUP"
										listId="groups" listCodeKey="groupCode" listNameFormat="$S{[:groupCode;$S{] :groupName}}"
										modified="readConditionReq(\"SOLD\");"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="partyCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
										listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
							</div>
						</div>
					</div>
				<% } else { %>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="organizationCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="organizationCode" mandatory="true" listId="organizations" listCodeKey="organizationCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO\");"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="distributionChannelCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="distributionChannelCode" mandatory="true" listId="distributionChannels" listCodeKey="distributionChannelCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION"
										listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SO\");"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="officeCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
										listId="offices" listCodeKey="officeCode" listNameFormat="$S{[:officeCode;$S{] :officeName}}"/>
							</div>
						</div>
					</div>
				<% } %>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemCode"/></div>
							<div class='field'><mtl:text id="record" key="itemCode" readonly="<%= !inserting %>"/></div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemPrice"/></div>
							<div class='field'><mtl:text id="record" key="itemPrice"/></div>
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
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRItemPrice?mode=rtp";
					readPartyAttributeReq( url, type, frmMain );
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
