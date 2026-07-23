<%--
	File Name:	dpr_stockquery_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
	hankalam	2020/12/31		2.2.0	create
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
	String queryKey = (String)pageContext.findAttribute( "queryKey" );
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

		function downloadReq( anchorObj ) {
			var checkObj = frmMain.listcheckbox;
			var url = getLocationURL();

			if( checkObj ) {
				var query = CheckBox.getQueryValue( checkObj, name );
				if( !query ) {
					url = replaceQueryValue( url, "queryKey", "<%= queryKey %>" );
					checkObj = null;
				}
			}

			requestDownload( url, checkObj, "simulationKey,organizationCode,distributionChannelCode,soldPartyCode,shipPartyCode,itemCode", anchorObj );
		}

		function downloadTemplateReq() {
			var url = getLocationURL();
			requestDownload( url, null, null, null, "tdown" );
		}

		function settingReq() {
			windowOpen( "<%= htmlpage.getRequestURL() %>?mode=imng&wintype=sub", "sub-content" );
		}

		function uploadReq() {
			windowOpen( "<%= htmlpage.getRequestURL() %>?mode=iup&wintype=sub", "sub-content" );
		}

		function plantMappingListReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlantMapping?mode=list";
			url = replaceQueryValue( url, "url", encodeURIComponent(getLocationURL()) );
			windowSelfOpen( url );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<input type='hidden' name='mode' value='conds'>
			<div id='messagebar'></div>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="soldPartyCode" mandatory="true" descriptionKey="FIELD_DPR_SOLDPARTY_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="soldPartyCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="shipPartyCode" mandatory="true" descriptionKey="FIELD_DPR_SHIPPARTY_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="shipPartyCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="plantCode" mandatory="true" descriptionKey="FIELD_DPR_PLANT_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="plantCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode" mandatory="true" descriptionKey="FIELD_DPR_ITEM_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemCode"/></div>
					</div>
				</div>
			</div>
			<div class='search-bottom'>
				<div class='table-cell search-button'>
					<mtl:button type="reset" styleClass="seccondary-w135"/>
					<mtl:button type="search" styleClass="primary-w135"/>
				</div>
			</div>

			<script type='text/javaScript'>
				function checkSearchCond() {
					var errorCount = 0;
					if( Field.getValue(frmCond.plantCode).trim().length == 0 ) {
						if( !Field.checkMandatory(frmCond.soldPartyCode) ) errorCount++;
						if( !Field.checkMandatory(frmCond.shipPartyCode) ) errorCount++;
					} else {
						toggleFieldErrorMessage( frmCond.soldPartyCode, false );
						toggleFieldErrorMessage( frmCond.shipPartyCode, false );
					}
					if( !Field.checkMandatory(frmCond.itemCode) ) errorCount++;
					if( errorCount > 0 ) return false;

					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function initSearchCond() {

				}
				attachWindowEvent( "load", initSearchCond );
			</script>
		</mtl:contentGroup>
	</mtl:form>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>
			<div class='list-top-content font-bold'>
				<mtl:message key="jsp.dpr_stockquery_list.MSG_AUTORETRY"/> <mtl:valuef id="manageMap" format="${DPR_STOCK_QUERY_AUTORETRY_@autoRetry}"/>
			</div>
			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				listwriter.setCheckboxType( ListWriter.CHECKBOXTYPE_CHECK );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( listwriter.containsData() ) { %>
					<mtl:button type="download"/>
				<% } %>
					<mtl:button type="button" onClick="JavaScript: downloadTemplateReq();" messageKey="jsp.BTN_DOWNLOAD_TEMPLATE"
							icon="images/ico_download.png"/>
					<mtl:button type="upload"/>
				<% if( htmlpage.hasManageAuth() ) { %>
					<mtl:button type="button" onClick="JavaScript: plantMappingListReq();" messageKey="jsp.BTN_PLANT_MAPPING"/>
					<mtl:button type="button" onClick="JavaScript: settingReq();" messageKey="jsp.BTN_SETTING"/>
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
