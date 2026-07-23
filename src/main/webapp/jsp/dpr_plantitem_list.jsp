<%--
	File Name:	dpr_plantitem_list.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/07/30		2.2.2	conditionItemName 조건 수정.
	jbaek		2015/04/07		2.2.1	plantCode 를 PK에 추가
	jbaek		2014/02/17		2.2.0	create
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
	Map<String, Object> condition = (Map<String, Object>)pageContext.findAttribute( "condition" );
	List<Map<String, Object>> distributionChannels = (List<Map<String, Object>>)pageContext.findAttribute( "distributionChannels" );
	List<Map<String, Object>> items = (List<Map<String, Object>>)pageContext.findAttribute( "items" );
	org.json.simple.JSONArray itemArray = null;
	if( items != null ) {
		itemArray = com.irt.util.Utility.convertListToJson( items );
	}
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_rbm_catelink.inc"%>
	<script type='text/javascript'>
		var linkmenu = null;

		$(function() {
			var items = <%= itemArray %>;
			var codeItems = $.map( items, function(item) {
				return {
					label: item.itemCode + "|" + item.itemName,
					itemCode: item.itemCode,
					itemName: item.itemName
				};
			});

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

			$("input[name=itemName]").autocomplete( {
				minLength: 2,
				source: codeItems,
				focus: function( event,ui ) { return false; },
				select: function( event, ui ) {
					$("input[name=itemName]").val( ui.item.itemName );
					return false;
				},
			}).autocomplete("instance")._renderItem=function( ul, item ) {
				var disp = "<div>("+ item.itemCode + ") " + item.itemName+ "</div>"
				return $("<li>").append( disp ).appendTo( ul );
			};
		});

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "plantCode,shipPartyCode,officeCode,organizationCode,distributionChannelCode,itemCode" );
		}

		function downloadReq( anchorObj ) {
			requestDownload( getLocationURL("url"), frmMain.listcheckbox, "plantCode,shipPartyCode,officeCode,organizationCode,distributionChannelCode,itemCode", anchorObj );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "plantCode,shipPartyCode,officeCode,organizationCode,distributionChannelCode,itemCode" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>")

			windowOpen( url, "sub-content" );
		}

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlantItem?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_pub_input.inc" %>

	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:hidden key="ptype" defaultValue="excl" />
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div id='messagebar'></div>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode" descriptionKey="FIELD_DPR_ITEM_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemName" descriptionKey="FIELD_DPR_ITEM_MASTER_NAME"/></div>
						<div class='field'><mtl:text id="condition" key="itemName"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="plantCode" descriptionKey="FIELD_DPR_PLANT_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="plantCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									listId="plants" listCodeKey="linkPlantCode" listNameFormat="$S{[:linkPlantCode;$S{] :linkPlantName}}"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_PLANT"/>
						</div>
					</div>
					<div class='cell'>
					</div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/></div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SHIP;PT\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="distributionChannelCode" descriptionKey="FIELD_DPR_DISTRIBUTIONCHANNEL_CODE" mandatory="true"/>
						</div>
						<div class='field'>
						<%
							String[] condDistributionChannelCodes = (String[])condition.get( "distributionChannelCode" );
							for( Map<String, Object> channelMap : distributionChannels ) {
								String code = (String)channelMap.get( "distributionChannelCode" );
								String name = (String)channelMap.get( "distributionChannelName" );
								out.print( "<input type='checkbox' id='distributionChannelCode_" + code + "'" );
								out.print( " name='distributionChannelCode' class='mandatory' value='" + code + "'");
								out.print( " onclick='JavaScript: readConditionReq(\"SHIP\");'" );
								for( String channelCode : condDistributionChannelCodes ) {
									if( code.equals(channelCode) ) {
										out.print( " checked " );
									}
								}
								out.print( ">" );
								out.print( "<label for='distributionChannelCode_" + code + "'>" );
								out.print( "<span>" + name + "</span>" );
								out.print( "</label>" );
							}
						%>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="shipPartyCode" descriptionKey="jsp.dpr_order_input.FIELD_SHIP_PARTY"/></div>
						<div class='field'>
							<mtl:select id="condition" key="shipPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SHIPTO"
									listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"/>
						</div>
					</div>
					<div class='cell'></div>
				</div>
			</div>
			<mtl:hidden id="request" key="search-fold" defaultValue="N"/>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
 				function checkSearchCond() {
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;
					if( !Field.checkMandatory(frmCond.distributionChannelCode) ) return false;

					disableBlankInput( frmCond, true );
					return submitInput();
				}

 				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRPlantItem?mode=rtp";
					readPartyAttributeReq( url, type );
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				com.irt.custom.ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				listwriter.setUseFlexDataLine( true );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="regist"/>
					<% if( listwriter.containsData() ) { %>
						<mtl:button type="modify"/>
						<mtl:button type="delete"/>
					<% } %>
					<mtl:button type="upload"/>
				<% } %>
				<% if( listwriter.containsData() ) { %>
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
