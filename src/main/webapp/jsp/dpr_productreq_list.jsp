<%--
	File Name:	dpr_productreq_list.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.1	검색조건 오류 수정
	hankalam	2017/08/31		2.2.0	create
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
	String type = property.getProperty( "type" );
%>

<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_linkmenu.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 900 );
			resetForm( frmMain );
			focusForm( frmMain, frmMain.title );

			if( typeof frmCond != "undefined" && frmCond.shipPartyCode.value != null && frmCond.shipPartyCode.value != "" ) {
				readDeliveryPlantReq();
			}
		}

		function deleteReq() {
			requestDelete( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "productReqKey,orderDate,partyCode,shipPartyCode,itemCode" );
		}

		function infoReq( productReqKey, orderDate, partyCode, shipPartyCode, itemCode ) {
			var values = productReqKey + ";" + orderDate + ";" + partyCode + ";" + shipPartyCode + ";" + itemCode;
			requestInfo( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "productReqKey,orderDate,partyCode,shipPartyCode,itemCode", values );
		}

		function modifyReq() {
			requestModify( "<%= htmlpage.getRequestURL() %>", frmMain.listcheckbox, "productReqKey,orderDate,partyCode,shipPartyCode,itemCode" );
		}

		function registReq() {
			var url = "<%= htmlpage.getRequestURL() %>?mode=ireg&wintype=sub";
			windowOpen( url, "sub-content" );
		}

		function downloadReq() {
			requestDownload( getLocationURL("url"), frmMain.listcheckbox, "productReqKey,orderDate,partyCode,shipPartyCode,itemCode" );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_pub_list.inc" %>
	<%@ include file="include_rbm_bodyheader.inc" %>
	<mtl:ifvalue key="type" id="property" notValue="sim">
	<%@ include file="include_pub_calendar.inc" %>
	<mtl:form name="frmCond" method="get" onSubmit="JavaScript: return checkSearchCond();">
		<input type='hidden' name='mode' value='list'/>
		<mtl:contentGroup groupId="condition" type="search" descriptionKey="jsp.GRP_SEARCH_CONDITION">
			<%@ include file="include_rbm_listcond.inc" %>
			<div class='search-table'>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="startOrderDate" descriptionKey="jsp.FIELD_DPR_ORDER_STARTDATE" mandatory="true"/></div>
						<div class='field'><mtl:date id="condition" key="startOrderDate"/></div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="endOrderDate" descriptionKey="jsp.FIELD_DPR_ORDER_ENDDATE" mandatory="true"/></div>
						<div class='field'><mtl:date id="condition" key="endOrderDate"/></div>
					</div>
					<div class='cell'></div>
					<div class='cell'></div>
				</div>
				<div class='row'>
					<div class='cell'>
						<div class='field-title'><mtl:title key="partyCode" descriptionKey="FIELD_DPR_SOLDPARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="partyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
									listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"
									modified="readConditionReq(\"SHIP\");"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="shipPartyCode" descriptionKey="FIELD_DPR_SHIPPARTY_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" key="shipPartyCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SHIPTO"
									listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"/>
						</div>
					</div>
					<div class='cell'>
						<div class='field-title'><mtl:title key="deliveryPlant" descriptionKey="FIELD_DPR_PLANT_CODE"/></div>
						<div class='field'>
							<mtl:select id="condition" uniqId="plantCode" key="deliveryPlant" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
									listId="plants" listCodeKey="linkPlantCode" listNameFormat="$S{[:linkPlantCode;$S{] :linkPlantName}}"
									hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_PLANT"/>
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
				$( function() {
					$("input[type=text].date").blur( function() {
						setTimeout( function() {
							if( Field.checkMultiMandatory([frmCond.startOrderDate, frmCond.endOrderDate], false) ) {
								Field.checkDateRange( frmCond.startOrderDate, frmCond.endOrderDate );
							}
						}, 500 );
					});
				});

				function checkSearchCond() {
					if( !Field.checkMultiMandatory([frmCond.startOrderDate, frmCond.endOrderDate], false) ) return false;
					if( !Field.checkDateRange(frmCond.startOrderDate, frmCond.endOrderDate) ) return false;

					disableBlankInput( frmCond, true );
					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRProductRequire?mode=rtp";
					readPartyAttributeReq( url, type );
				}
			</script>
		</mtl:contentGroup>
	</mtl:form>
	</mtl:ifvalue>

	<mtl:form name="frmMain" onSubmit="JavaScript: return false;">
		<mtl:contentGroup groupId="list" type="list">
			<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>

			<%
				ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
				if( sessionMng.isAuthorized("DPR", "DPRProductRequire.DOWN") && !"sim".equals(type) )
					listwriter.setCheckboxTypeAndNumbering( com.irt.custom.ListWriter.CHECKBOXTYPE_CHECK );
				listwriter.print( out );
			%>

			<div class='list-function'>
				<div class='button'>
					<mtl:button type="return"/>
				<% if( htmlpage.hasManageAuth() && !"sim".equals(type) ) { %>
					<mtl:button type="regist"/>
					<% if( listwriter.containsData() ) { %>
						<mtl:button type="modify"/>
						<mtl:button type="delete"/>
					<% } %>
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
