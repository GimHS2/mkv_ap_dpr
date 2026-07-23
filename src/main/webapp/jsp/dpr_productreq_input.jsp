<%--
	File Name:	dpr_productreq_input.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.1	필수입력항목 Title 에 mandatory=true 속성 추가
	hankalam	2017/11/30		2.2.0	create
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
	java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
	String orderNumber = null;
	if( recordMap != null ) {
		orderNumber = (String)recordMap.get( "orderNumber" );
	}
	List<Map<String, Object>> items = (List<Map<String, Object>>)pageContext.findAttribute( "items" );
	org.json.simple.JSONArray itemArray = null;
	if( items != null ) {
		itemArray = com.irt.util.Utility.convertListToJson( items );
	}
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_dpr_itemtree.inc" %>

	<style type='text/css'>
		ul, li {margin:0; padding:0}
		a.close {
			text-align: center;
			position: absolute;
			width: 28px;
			height: 28px;
			cursor: pointer;
			border-radius: 15px;
			color: #FFFFFF;
			background-color: #B7EBF8;
		}

		a.close:before {
			content: '\2716';
			font-size: 18px;
			line-height: 28px;
		}

		a.close:hover {
			background-color: #92CDDC;
		}
	</style>

	<script type='text/javascript'>
		var windowWidth = 800;
		function bodyLoad() {
		<% if( "ireg".equals(htmlpage.getMode()) ) { %>
			addItem();
		<% } else if( "imod".equals(htmlpage.getMode()) && recordMap != null && recordMap.get("partyCode") != null ) { %>
			readConditionReq( "SHIP" );
		<% } %>
		}

		function attributePartyCallback() {
			$("#shipPartyCode").val( "<mtl:value id="record" key="shipPartyCode"/>" );
			$("#shipPartyCode").singleSelectmenu( "refresh" );
		}

		function addItem() {
			$("#itemWrapper").append( $("#itemHidden").html() );

			var items = <%= itemArray %>;
			var codeItems = $.map( items, function(item) {
				return {
					label: "(" + item.itemCode + ") " + item.itemName,
					itemCode: item.itemCode,
					itemName: item.itemName
				};
			});

			$("#itemWrapper").last().find( "input[name=itemCode]" ).autocomplete( {
				minLength: 2,
				source: codeItems,
				focus: function( event,ui ) { return false; },
				select: function( event, ui ) {
					$(event.target).val( ui.item.itemCode );
					return false;
				}
			});

			setDatePicker();
			parent.subContentResize( true );
		}

		function removeItem( obj ) {
			$(obj).closest( ".row" ).remove();
			parent.subContentResize( true );
		}

		function setDatePicker() {
			$("#itemWrapper input[type=text].date").datepicker( {
				prevText: "<mtl:message key="jsp.DATEPICKER_PREV_MONTH"/>",
				nextText: "<mtl:message key="jsp.DATEPICKER_NEXT_MONTH"/>",
				monthNames: [ "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" ],
				monthNamesShort: [ "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" ],
				dayNames: [
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SUNDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_MONDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_TUSEDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_WEDNESDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_THURSDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_FRIDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SATURDAY"/>",
				],
				dayNamesMin: [
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_SUNDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_MONDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_TUSEDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_WEDNESDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_THURSDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_FRIDAY"/>",
					"<mtl:message key="jsp.DATEPICKER_DAY_NAME_SHORT_SATURDAY"/>",
				],
				dateFormat: "<mtl:message key="jsp.DATEPICKER_DATE_FORMAT"/>",
				firstDay: 0,
				isRTL: false,
				showMonthAfterYear: true,
				showOtherMonths: true,
				selectOtherMonths: true,
				changeMonth: true,
				changeYear: true,
				yearRange: "c-3:c+3",
				beforeShow: function( input, inst ) {
					$.datepicker._pos = $.datepicker._findPos( input );
					$.datepicker._pos[ 1 ] += input.offsetHeight + 5;
				}
			});
		}
	</script>
</head>
<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<%@ include file="include_pub_input.inc" %>
		<%@ include file="include_rbm_bodyheader.inc"%>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<mtl:contentGroup groupId="main" type="content">
				<mtl:hidden id="record" key="productReqKey"/>
				<mtl:hidden id="record" key="orderKey" />
				<div class='info-table'>
					<div class='row'>
						<mtl:ifvalue id="record" key="orderDate" notValue="">
						<div class='cell'>
							<div class='field-title'><mtl:title key="orderDate"/></div>
							<div class='field'>
								<mtl:text id="record" key="orderDate" readonly="true"/>
							</div>
						</div>
						</mtl:ifvalue>
						<mtl:ifvalue id="record" key="orderNumber" notValue="">
						<div class='cell'>
							<div class='field-title'><mtl:title key="orderNumber" descriptionKey="FIELD_DPR_ORDER_ORDERNUMBER"/></div>
							<div class='field'>
								<mtl:text id="record" key="orderNumber" readonly="true"/>
							</div>
						</div>
						</mtl:ifvalue>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="partyCode" descriptionKey="jsp.dpr_order_input.FIELD_SOLD_PARTY"/></div>
							<div class='field'>
							<% if( "info".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="partyCode" format="$S{[:partyCode;]$S{ :partyName}}"/>
								<mtl:hidden id="record" key="partyCode"/>
							<% } else { %>
								<mtl:select id="record" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
										listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"
										modified="readConditionReq(\"SHIP\");"/>
							<% } %>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="shipPartyCode" descriptionKey="jsp.dpr_order_input.FIELD_SHIP_PARTY"/></div>
							<div class='field'>
							<% if( "info".equals(htmlpage.getMode()) ) { %>
								<mtl:valuef id="record" key="shipPartyCode" format="$S{[:shipPartyCode;]$S{ :shipPartyName}}"/>
								<mtl:hidden id="record" key="shipPartyCode"/>
							<% } else { %>
								<mtl:select id="record" key="shipPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SHIPTO"
										listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
							<% } %>
							</div>
						</div>
					</div>
				</div>
				<div class='info-table'>
					<div class='row'>
						<mtl:ifvalue id="record" key="deliveryPlant" notValue="">
						<div class='cell'>
							<div class='field-title'><mtl:title key="deliveryPlant" descriptionKey="FIELD_DPR_PLANT_CODE"/></div>
							<div class='field'>
								<mtl:text id="record" key="deliveryPlant" readonly="true"/>
							</div>
						</div>
						</mtl:ifvalue>
						<mtl:ifvalue id="record" key="officeCode" notValue="">
						<div class='cell'>
							<div class='field-title'><mtl:title key="officeCode" descriptionKey="FIELD_DPR_SALESOFFICE_CODE"/></div>
							<div class='field'>
								<mtl:text id="record" key="officeCode" readonly="true"/>
							</div>
						</div>
						</mtl:ifvalue>
					</div>
				</div>

			<% if( "ireg".equals(htmlpage.getMode()) ) { %>
				<div id='itemWrapper' class='info-table'>
				</div>
			<% } else { %>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="expectedDate" mandatory="true" descriptionKey="FIELD_DPR_PRODUCTREQ_EXPECTED_DATE"/></div>
							<div class='field'>
							<% if( orderNumber != null && orderNumber.length() > 0 ) { %>
								<mtl:text id="record" key="expectedDate" readonly="true" maxlength="10"/>
							<% } else { %>
								<mtl:date id="record" key="expectedDate"/>
							<% } %>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemCode" descriptionKey="FIELD_DPR_ITEM_MASTER_CODE"/></div>
							<div class='field'>
								<mtl:text id="record" key="itemCode" readonly="true"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemName" descriptionKey="FIELD_DPR_ITEM_MASTER_NAME"/></div>
							<div class='field'>
								<mtl:text id="record" key="itemName" readonly="true"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="itemConsumerEAN" descriptionKey="jsp.dpr_orderitem_tree.FIELD_UPC_CODE"/></div>
							<div class='field'>
								<mtl:text id="record" key="itemConsumerEAN" readonly="true"/>
							</div>
						</div>
						<div class='cell'>
							<div class='field-title'><mtl:title key="qty" mandatory="true"/></div>
							<div class='field'>
								<mtl:text id="record" key="qty"/>
							</div>
						</div>
					</div>
				</div>
			<% } %>
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="uom" mandatory="true"/></div>
							<div class='field'>
								<mtl:select id="record" key="uom" hasBlank="false" searchable="false"
										listId="allowUOMList" listCodeKey="allowUOM" listNameFormat="$H{MSG_UOM_@allowUOM}"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="description"/></div>
							<div class='field'>
								<mtl:text id="record" key="description" readonly="false"/>
							</div>
						</div>
					</div>
				</div>
				<div class='list-function' style='margin-top: 0'>
					<div class='button'>
					<% if( "ireg".equals(htmlpage.getMode()) ) { %>
						<mtl:button type="button" onClick="JavaScript: addItem();" messageKey="jsp.BTN_ADD_ITEM"/>
					<% } %>
					</div>
					<div id='list_page' class='page' style='width:auto'>
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
				function checkInput() {
					if( !Field.checkMandatory(frmMain.partyCode) ) return false;
					if( !Field.checkMandatory(frmMain.shipPartyCode) ) return false;
					if( Field.isArray(frmMain.itemCode) ) {
						for(var i = 0; i < frmMain.itemCode.length-1; i++) {
							if( !Field.checkMandatory(frmMain.itemCode[i]) ) return false;
							if( !Field.checkMandatory(frmMain.qty[i]) ) return false;
							if( !Field.checkNumberFormat(frmMain.qty[i], false, true) ) return false;
							if( !Field.checkNumberRange(frmMain.qty[i], false, 1, 9999, 3) ) return false;
							if( !Field.checkMandatory(frmMain.expectedDate[i]) ) return false;
							if( !Field.checkDateFormat(frmMain.expectedDate[i]) ) return false;
							var expectedDate = new Date( frmMain.expectedDate[i].value );
							if( orderDate > expectedDate ) {
								customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_productreq_input.MSG_DATE_ERROR"/>" } );
								frmMain.expectedDate[i].focus();
								return false;
							}
						}
					} else {
						if( !Field.checkMandatory(frmMain.itemCode) ) return false;
						if( !Field.checkNumberFormat(frmMain.qty, false, true) ) return false;
						if( !Field.checkNumberRange(frmMain.qty, false, 1, 9999, 3) ) return false;
						if( !Field.checkDateFormat(frmMain.expectedDate) ) return false;
						var expectedDate = new Date( frmMain.expectedDate.value );
						if( orderDate > expectedDate ) {
							customPopup.alert( { "header" : "<mtl:message key="jsp.dpr_productreq_input.MSG_DATE_ERROR"/>" } );
							return false;
						}
					}

					var orderDate;
					if( typeof frmMain.orderDate == "undefined" ) {
						orderDate = new Date( new Date().toISOString().slice(0,10) );
					} else {
						orderDate = new Date( frmMain.orderDate.value );
					}

					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRProductRequire?mode=rtp";
					readPartyAttributeReq( url, type, frmMain );
				}
			</script>
		</mtl:form>

	<% if( "ireg".equals(htmlpage.getMode()) ) { %>
		<div id='itemHidden' class='info-table' style='display:none;'>
			<div class='row'>
				<div class='cell'>
					<div class='field-title'><mtl:title key="itemCode" mandatory="true" descriptionKey="FIELD_DPR_ITEM_MASTER_CODE"/></div>
					<div class='field'>
						<mtl:text id="record" key="itemCode"/>
					</div>
				</div>
				<div class='cell'>
					<div class='field-title'><mtl:title key="qty" mandatory="true" descriptionKey="FIELD_DPR_PRODUCTREQ_QTY"/></div>
					<div class='field'>
						<mtl:text id="record" key="qty"/>
					</div>
				</div>
				<div class='cell'>
					<div class='field-title'><mtl:title key="expectedDate" mandatory="false" descriptionKey="FIELD_DPR_PRODUCTREQ_EXPECTED_DATE"/></div>
					<div class='field'>
						<mtl:date id="record" key="expectedDate" mandatory="false"/>
					</div>
				</div>
				<div class='cell w30' style='vertical-align: middle'>
					<a onclick='JavaScript: removeItem(this);' class='close'></a>
				</div>
			</div>
		</div>
	<% } %>
	</div>
</body>
</mtl:html>

