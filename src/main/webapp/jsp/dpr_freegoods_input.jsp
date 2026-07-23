<%--
	File Name:	dpr_freegoods_input.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2019/07/31		2.2.0	create
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
	Map<String, Object> recordMap = (Map<String, Object>)pageContext.findAttribute( "record" );
	String updateStatus = "";
	if( recordMap != null )
		updateStatus = (String)recordMap.get( "updateStatus" );

	boolean inserting = "reg".equals(htmlpage.getMode()) || "ireg".equals(htmlpage.getMode());

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
			var url = "<%= systemConfig.getClassURL() %>?mode=imod";
			url = replaceQueryValue( url, "mode", "imod" );
			url = replaceQueryValue( url, "freegoodsKey", "<mtl:value id="record" key="freegoodsKey" encodeScript="true"/>" );

			windowSelfOpen( url );
		}

		function registReq() {
			var url = "<%= systemConfig.getClassURL() %>?mode=ireg";
			windowSelfOpen( url );
		}

		function selectDPRPartyReq( naming ) {
			var query = "";

			if( frmMain.officeCode && frmMain.officeCode.value && frmMain.officeCode.value != "0" ) {
				query += ( "&officeCode="+ frmMain.officeCode.value );
			}
			if( frmMain.groupCode && frmMain.groupCode.value && frmMain.groupCode.value != "0" ) {
				query += ( "&groupCode="+ frmMain.groupCode.value );
			}
			if( naming ) {
				query += ( "&partyCode="+ frmMain.partyCode.value );
				return _selectDPRParty( "party", "D", ""+query, null, null, naming );
			} else {
				_selectDPRParty( "party", "D", ""+query, null, null, naming );
			}
		}

		function _selectDPRItem( slname, namecls, attr, value, winname , naming) {
			var url = classURL +"DPRItem?slname="+ slname;
			url += ( namecls ? "&namecls="+ namecls : "" );
			url += ( attr ? "&attr="+ attr : "" );
			url += "&organizationCode="+ frmMain.organizationCode.value;
			url += "&oitmHierIndex=1";

			if( frmMain.partyCode && frmMain.partyCode.value && frmMain.partyCode.value != "0" ) {
				url += ( "&btype=ord" );
				url += ( "&partyCode="+ frmMain.partyCode.value );
			} else {
				url += ( "&btype=itm" );
			}

			if( value && naming )
				url += "&code=" + value;

			if( naming ) {
				url += "&itemCode="+ frmMain.itemCode.value;
				windowOpen( url +"&mode=name", winname ? winname : "clsName" );
			} else
				windowOpen( url +"&mode=sel&wintype=sub", winname ? winname : "clsSel" );

			return false;

		}

		function selectDPRItemReq( naming ) {
			if( naming ) {
				return _selectDPRItem( "item", "D", "", null, null, naming );
			} else {
				_selectDPRItem( "item", "D", "", null, null, naming );
			}
		}

	</script>
</head>
<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_rbm_bodyheader.inc"%>
		<%@ include file="include_pub_calendar.inc" %>
		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<input type='hidden' name='url' value="<%= htmlpage.getRequestURL() %>" />
			<mtl:hidden id="record" key="freegoodsKey" />
			<mtl:hidden id="record" key="organizationCode" />

			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="startDate" descriptionKey="jsp.FIELD_DPR_STARTDATE"/></div>
							<div class='field'>
								<mtl:date id="record" key="startDate"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="endDate" descriptionKey="jsp.FIELD_DPR_ENDDATE"/></div>
							<div class='field'>
								<mtl:date id="record" key="endDate"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="officeCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
										listId="offices" listCodeKey="officeCode" listNameFormat="$S{[:officeCode;$S{] :officeName}}"
										modified="readConditionReq(\"SOLD\");"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="partyCode" mandatory="false"/></div>
							<div class='field'>
								<mtl:select id="record" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" mandatory="false"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
										listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemCode"/></div>
							<div class='field'>
								<mtl:text id="record" key="itemCode"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="quota"/></div>
							<div class='field'>
								<mtl:text id="record" key="quota"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="orderableRatio"/></div>
							<div class='field'>
								<mtl:text id="record" key="orderableRatio"/>
							</div>
						</div>
					</div>

					<% if( "mod".equals(htmlpage.getMode()) || "imod".equals(htmlpage.getMode()) ) { %>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="usedQty"/></div>
							<div class='field'>
								<mtl:text id="record" key="usedQty"/>
							</div>
						</div>
					</div>
					<% } %>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="close_if"/>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="reset"/>
						<mtl:button type="save"/>
					<% } %>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				$( function() {
					$("input[type=text].date").blur( function() {
						setTimeout( function() {
							if( Field.checkMultiMandatory([frmMain.startDate, frmMain.endDate], false) ) {
								Field.checkDateRange( frmMain.startDate, frmMain.endDate );
							}
						}, 500 );
					});
				});

				function checkInput() {
					<%= htmlpage.getValidationScript() %>
					if( !Field.checkMandatory(frmMain.organizationCode) ) return false;
					if( !Field.checkMultiMandatory([frmMain.startDate, frmMain.endDate], false) ) return false;
					if( !Field.checkDateRange(frmMain.startDate, frmMain.endDate) ) return false;

				<% if( "imod".equals(htmlpage.getMode()) ) { %>
					var quota = frmMain.quota.value;
					if( !Field.checkNumberRange(frmMain.usedQty, false, -1, Number(quota) + 1) ) {
						return false;
					}
				<% } %>

					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRFreeGoods?mode=rtp";
					readPartyAttributeReq( url, type, frmMain );
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
