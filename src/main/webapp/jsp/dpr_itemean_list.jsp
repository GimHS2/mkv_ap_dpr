<%--
	File Name:	dpr_itemean_list.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2019/07/30		2.2.1	conditionItemName조건 수정.
	jbaek		2017/06/30		2.2.0	create
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
	List<Map<String, Object>> items = (List<Map<String, Object>>)pageContext.findAttribute( "items" );
	org.json.simple.JSONArray itemArray = null;
	if( items != null ) {
		itemArray = com.irt.util.Utility.convertListToJson( items );
	}
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
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

			var upcItems = $.map( items, function(item) {
				return {
					label: item.itemConsumerEANCode + "|" + item.itemName,
					itemCode: item.itemCode,
					itemName: item.itemName,
					itemConsumerEANCode: item.itemConsumerEANCode
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

			if( $("input[name=itemName]").length ) {
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
			}

			if( $("input[name=itemConsumerEANCode]").length ) {
				$("input[name=itemConsumerEANCode]").autocomplete( {
					minLength: 2,
					source: upcItems,
					focus: function( event,ui ) { return false; },
					select: function( event, ui ) {
						$("input[name=itemConsumerEANCode]").val( ui.item.itemConsumerEANCode );
						return false;
					},
				}).autocomplete("instance")._renderItem=function( ul, item ) {
					var disp ="<div>("+ item.itemCode + ") " + item.itemName+ " (" + item.itemConsumerEANCode + ")" + "</div>"
					return $("<li>").append( disp ).appendTo( ul );
				};
			}
		});

		function downloadReq() {
			var messages = { "detail" : "<mtl:message key="MSG_CONFIRM_DOWNLOAD" encodeScript="true"/>" };
			customPopup.confirm( messages, function(res) {
				if( res ) {
					var url = replaceQueryValue( getLocationURL("url"), "mode", "down" );
					windowOpen( url );
				}
			});
		}
		<% String primaryKeysString = com.irt.util.StringUtil.strJoin(((com.irt.data.cols.ColumnList)request.getAttribute("columnList")).getPrimaryFieldKeys(), ","); %>

		function uploadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRItemEAN?mode=iup&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>" );
			windowOpen( url, "sub-content" );
		}

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "<%=primaryKeysString%>" );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "<%=primaryKeysString%>" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="condition" key="organizationCode"/>")
			windowOpen( url, "sub-content" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'>
							<mtl:title key="organizationCode" descriptionKey="FIELD_DPR_SALESORGANIZATION_CODE" mandatory="true"/>
						</div>
						<div class='field'>
							<mtl:select id="condition" key="organizationCode" mandatory="true" hasBlank="true"
									nullValueKey="jsp.MSG_SELECT_NOT_SELECT" listId="organizations" listCodeKey="organizationCode"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION"
									listNameFormat="$S{[:organizationCode;$S{] :organizationName}}"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemCode" descriptionKey="FIELD_DPR_ITEM_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemCode"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemName" descriptionKey="FIELD_DPR_ITEM_MASTER_NAME"/></div>
						<div class='field'><mtl:text id="condition" key="itemName"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="itemConsumerEANCode" descriptionKey="jsp.FIELD_DPR_UPC_CODE"/></div>
						<div class='field'><mtl:text id="condition" key="itemConsumerEANCode"/></div>
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
					if( !Field.checkMandatory(frmCond.organizationCode) ) return false;

					return submitInput();
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
		<%@ include file="include_dpr_tail.inc" %>
	</mtl:form>
</body>
</mtl:html>
