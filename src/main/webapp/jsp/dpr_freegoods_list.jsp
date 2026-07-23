<%--
	File Name:	dpr_freegoods_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2019/07/31		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding="euc-kr" %>
<%@ page import="com.irt.html.*, java.util.List, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
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

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "freegoodsKey" );
		}

		function downloadReq( anchorObj ) {
			requestDownload( getLocationURL("url"), frmMain.listcheckbox, "freegoodsKey", anchorObj );
		}

		function infoReq() {
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "freegoodsKey" );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "freegoodsKey" );
		}

		function registReq() {
			windowOpen( "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub", "sub-content" );
		}

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRFreeGoods?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<%@ include file="include_pub_calendar.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="startDate_min" descriptionKey="jsp.FIELD_DPR_STARTDATE"/></div>
						<div class='field'><mtl:date id="condition" key="startDate_min"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="endDate_max" descriptionKey="jsp.FIELD_DPR_ENDDATE"/></div>
						<div class='field'><mtl:date id="condition" key="endDate_max"/></div>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"SO;SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:message key="FIELD_DPR_PARTY_SALESOFFICE_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
									listId="offices" listCodeKey="officeCode" listNameFormat="$S{[:officeCode;$S{] :officeName}}"
									modified="readConditionReq(\"SOLD\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:message key="FIELD_DPR_SOLDPARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode" descriptionKey="FIELD_DPR_ITEM_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemCode"/></div>
					</div>
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
				$( function() {
					$("input[type=text].date").blur( function() {
						setTimeout( function() {
							Field.checkDateRange( frmCond.startDate_min, frmCond.endDate_max );
						}, 500 );
					});
				});

				function checkSearchCond() {
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;
					if( !Field.checkDateRange(frmCond.startDate_min, frmCond.endDate_max) ) return false;

					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRFreeGoods?mode=rtp";
					readPartyAttributeReq( url, type );
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>
	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
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
		<%@ include file="include_dpr_tail.inc"%>
	</mtl:form>
</body>
</mtl:html>
