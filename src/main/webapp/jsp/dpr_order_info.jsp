<%--
	File Name:	dpr_order_info.jsp
	Version:	2.2.12

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/11/30		2.2.12	신규 UI/UX 적용
	hankalam	2021/07/30		2.2.11	Freegoods 가 포함된 발주가 발주 수정이 되는 문제 수정
	jbaek		2020/06/30		2.2.10	Revise Order Feature.
	hankalam	2020/06/30		2.2.10	useDangerousItem 옵션에 따라 orderType 항목 타이틀 표시되도록 변경
	hankalam	2019/07/31		2.2.9	Freegoods 관련 항목 표시
	jbaek		2019/07.30		2.2.8	inDateConfirm 조건에 따라 표시. remarkAuth오류수정
	jbaek		2018/10/30		2.2.7	isChinaCountry() 삭제
	jbaek		2018/04/30		2.2.6	SJJP Order Download 적용
	hankalam	2017/11/30		2.2.5	order list 화면 표시할 때 shortage item 등록 리스트 출력.
	jbaek		2017/02/28		2.2.4	download url 변경.
	jbaek		2014/10/30		2.2.3	Goods Issue Date, Credit Release Date 적용
	jbaek		2013/01/30		2.2.2	PIPO 기능  개발
	jbaek		2012/08/30		2.2.1	rdd Logic 변경: China country는 defaultRDD 칼럼명만 사용
	guksm		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='UTF-8' %>
<%@ page import="com.irt.data.cols.ColumnList, com.irt.html.*, java.util.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );

	String create = request.getParameter("create");
%>
<mtl:html errorPage="error.jsp">
<%
	Map<String, Object> headerMap = (Map<String, Object>)pageContext.findAttribute( "header" );

	String status = null;
	if( headerMap != null )
		status = (String)headerMap.get( "status" );

	String orderFlag = null;
	if( headerMap != null )
		orderFlag = (String)headerMap.get( "orderStatus" );

	boolean remarkAuth = false;
	if( "CD".equals(status) && sessionMng.isAuthorized("DPR", "DPROrder.RMK.MNG") )
		remarkAuth = true;

	String freegoodsOrderWay = (String)headerMap.get( "freegoodsOrderWay" );
	String freegoodsOrderInd = (String)headerMap.get( "freegoodsOrderInd" );
%>

<%
	String organizationCode = null;
	if( headerMap != null ) {
		organizationCode = (String)headerMap.get( "organizationCode" );
	}
%>

<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<%@ include file="include_pub_list.inc" %>
	<script type='text/javascript' src='script/tabpane.js'></script>
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
			var rtype = "<mtl:value id="property" key="rtype" />";
			if( rtype == "reg" ) {
				var url = "<%= systemConfig.getClassURL() %>/DPRProductRequire?mode=list";
				url = attachDefaultParameter( url );
				url = replaceQueryValue( url, "startOrderDate", "<mtl:value id="header" key="orderDate"/>" );
				url = replaceQueryValue( url, "endOrderDate", "<mtl:value id="header" key="orderDate"/>" );
				url = replaceQueryValue( url, "orderKey", "<mtl:value id="request" key="orderKey"/>" );
				url = replaceQueryValue( url, "soldPartyCode", "<mtl:value id="header" key="soldPartyCode"/>" );
				url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="header" key="shipPartyCode"/>" );
				url = replaceQueryValue( url, "type", "sim" );
				url = replaceQueryValue( url, "wintype", "sub" );

				windowOpen( url, "clsMng" );
			}

			$("#tabs-order-main").tabs( {
				classes: {
					"ui-tabs": "tabs-main",
					"ui-tabs-nav": "tabs-nav",
					"ui-tabs-tab": "tabs-tab",
				}
			});

			var cellWidth = 285;
			$("#tabs-order-header").find( ".group-wrap" ).each( function(index, item) {
				var maxCellLength = 0;
				$(item).find( ".row" ).each( function(index, item) {
					var length = $(item).find( ".cell" ).length;
					if( maxCellLength < length ) {
						maxCellLength = length;
					}
				});
				if( maxCellLength > 0 ) {
					$(item).css( "max-width", cellWidth * maxCellLength );
				}
			});

			$("#tabs-order-header").find( ".field-info" ).each( function(index, item) {
				if( $(item).text().trim() === "" ) {
					$(item).text( "–" );
				}
			});

		<% if( request.getParameter("focustab") != null ) { %>
			$("#tabs-order-main").tabs( "option", "active", "<mtl:value id="request" key="focustab"/>" );
		<% } else { %>
			var infoType = "<mtl:value id="request" key="infoType"/>";
			if( infoType == "B" )
				$("#tabs-order-main").tabs( "option", "active", 2 );
			else if( infoType == "M" )
				$("#tabs-order-main").tabs( "option", "active", 3 );
			else if( infoType == "D" )
				$("#tabs-order-main").tabs( "option", "active", 0 );
		<% }%>
		});

		function billingDetailReq( billingNumber, orderNumber ) {
			var url = "<%= systemConfig.getClassURL() %>/DPRBillingDetail?mode=list&rtype=S";
			url = replaceQueryValue( url, "billingNumber", billingNumber );
			url = replaceQueryValue( url, "orderNumber", orderNumber );

			windowOpen( url + "&wintype=sub", "clsMng" );
		}

		function downloadReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder?mode=down";
			url = replaceQueryValue( url, "orderKey", "<mtl:value id="header" key="orderKey"/>" );
			url = replaceQueryValue( url, "orderNumber", "<mtl:value id="header" key="orderNumber"/>" );

			windowOpen( url );
		}

		function formattedPrinting() {
			var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder?mode=dlist";

			url = replaceQueryValue( url, "orderKey", "<mtl:value id="header" key="orderKey"/>" );
			url = replaceQueryValue( url, "orderNumber", "<mtl:value id="header" key="orderNumber"/>" );

			windowOpen( url + "&wintype=sub", "printing" );
		}

		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRPlaceOrder?mode=frm";
			url = replaceQueryValue( url, "orderKey", encodeURIComponent("<mtl:value id="header" key="orderKey"/>") );

			windowSelfOpen( url );
		}

		function modifyRemarkReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPROrderRemark?mode=imod";
			url = replaceQueryValue( url, "orderKey", encodeURIComponent("<mtl:value id="header" key="orderKey"/>") );

			windowOpen( url + "&wintype=sub", "clsMng" );
		}

		function reviseRegistReq() {
			if( String("<%=HtmlUtility.toHtmlString(freegoodsOrderWay)%>") == "1" || String("<%=HtmlUtility.toHtmlString(freegoodsOrderWay)%>") == "2" ) {
				var messages = { "header" : "<mtl:message key="ERR_ORDREV_CANNOT_FREEGOODS" encodeScript="true"/>" };
				customPopup.alert( messages );
				return;
			}
			var partyRevOrdMaxLimit = new Number("<%=com.irt.rbm.RBMSystem.getSystemEnvInt("DPR"
					, sessionMng.getPartyId()+";"+"partyRevOrdMaxLimit"
					, com.irt.dpr.OrderRevise.DEFAULT_MAX_LIMIT_MODIFICATION)%>");
			var parentReviseModCount = new Number("<%=HtmlUtility.toHtmlString(headerMap.get("parentReviseModCount"))%>");
			if( parentReviseModCount >= partyRevOrdMaxLimit ) {
				var messages = { "header" : "<mtl:message key="ERR_ORDREV_CANNOT_MAX_LIMIT" encodeScript="true"/>" };
				customPopup.alert( messages );
				return;
			}
			if( String("<%=HtmlUtility.toHtmlString(headerMap.get("deliveryNumber"))%>") != "" ) {
				var messages = { "header" : "<mtl:message key="ERR_ORDREV_CANNOT_SHIPPED" encodeScript="true"/>" };
				customPopup.alert( messages );
				return;
			}
			var thisReviseStatus = "<mtl:value id="header" key="reviseStatus"/>";
			if( ( thisReviseStatus == "CQ" )
				&& String("<%=headerMap.get("revHbrdLastDate")%>") == "<%=com.irt.data.Date.getInstance()%>" ) {
				var messages = { "header" : "<mtl:message key="ERR_ORDREV_CANNOT_EXCEED_MAX_PER_DAY" encodeScript="true"/>" };
				customPopup.alert( messages );
				return;
			}
			if( ( thisReviseStatus == "CQ" ) ) {
				var messages = { "header" : "<mtl:message key="ERR_ORDREV_CREATION_REQUEST_ALREADY" encodeScript="true"/>" };
				customPopup.alert( messages );
				return;
			}

			var url = "<%= systemConfig.getClassURL() %>/DPROrderRevise";
			url = replaceQueryValue( url, "mode", "ireg" );
			url = replaceQueryValue( url, "origOrderKey", encodeURIComponent("<mtl:value id="header" key="orderKey"/>") );

			windowSelfOpen( url, getLocationURL() );
		}

		function reviseListReq() {
			if( String("<%=HtmlUtility.toHtmlString(freegoodsOrderWay)%>") == "1" || String("<%=HtmlUtility.toHtmlString(freegoodsOrderWay)%>") == "2" ) {
				var messages = { "header" : "<mtl:message key="ERR_ORDREV_CANNOT_FREEGOODS" encodeScript="true"/>" };
				customPopup.alert( messages );
				return;
			}

			var url = "<%= systemConfig.getClassURL() %>/DPROrderRevise";
			url = replaceQueryValue( url, "mode", "list" );
			url = replaceQueryValue( url, "origOrderKey", encodeURIComponent("<mtl:value id="header" key="orderKey"/>") );
			url = replaceQueryValue( url, "revOrderNumber", encodeURIComponent("<mtl:value id="header" key="orderNumber"/>") );
			url = replaceQueryValue( url, "soldPartyCode", encodeURIComponent("<mtl:value id="header" key="soldPartyCode"/>") );

			windowSelfOpen( url, getLocationURL() );
		}

		function orderEnquiry() {
			var url = "<%= systemConfig.getClassURL() %>/DPREnquiryOrder?mode=info";
			url = replaceQueryValue( url, "orderKey", encodeURIComponent("<mtl:value id="header" key="orderKey"/>") );
			url = replaceQueryValue( url, "status", encodeURIComponent("<mtl:value id="header" key="status"/>") );

			var msg = document.getElementById( "msg" );
			if( msg ) msg.innerHTML = "<mtl:message key="jsp.include_rbm_header.MSG_SUBMIT" encodeScript="true"/>";

			windowSelfOpen( url );
		}
	</script>
</head>

<body class='content'>
	<%@ include file="include_rbm_bodyheader.inc" %>

	<div class="content">
	<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript: return false;">
		<%@ include file="include_rbm_form.inc" %>

		<% if( headerMap != null && "Y".equals(headerMap.get("pipoItemExist")) ) { %>
			<pre id="msg" style="color: #FFC000;"><mtl:message key="jsp.MSG_PIPO_EXIST"/></pre>
		<% } %>

		<mtl:contentGroup groupId="main" type="tabpane" styleClass="tabpane">
			<div id='messagebar'></div>
			<mtl:ifvalue id="request" key="create" value="Y">
				<h2><%= HtmlUtility.toHtmlString( htmlpage.getSubTitle() ) %></h2>
			</mtl:ifvalue>
			<div class='list-function upper'>
				<div class='button'>
					<% if( !"Y".equals(create) ) { %>
						<mtl:button type="return"/>
					<% } %>
					<% if( sessionMng.isAuthorized("DPR", "DPROrder.MNG")
							&& ( headerMap.get("orderNumber") == null )
							&& ("WK".equals(status) || "ER".equals(status) || "SD".equals(status)) ) { %>
						<mtl:button type="modify"/>
					<% } else if( "CD".equals(status) ) { %>
						<mtl:button type="button" onClick="JavaScript: orderEnquiry();"
							icon="images/ico_search.png" messageKey="jsp.BTN_ORDER_STATUS_SYNC"/>
						<mtl:button type="download"/>
					<% } %>
				<% if( com.irt.dpr.Country.isFeature(htmlpage.getProperty().getProperty("savedOrgCd"), "useRevOrd")
						&& !"Y".equals(freegoodsOrderInd) ) { %>
					<mtl:button type="modify" onClick="JavaScript:reviseRegistReq();"/>
					<mtl:button type="list" onClick="JavaScript:reviseListReq();" icon="images/ico_editrecord.png" messageKey="jsp.BTN_ORDER_MODIFY_LIST"/>
				<% } %>

					<mtl:button type="close_if"/>
				</div>
			</div>

			<div id='tabs-order-main'>
				<ul>
					<li><a href="#tabs-order-header" class='tabs-title order-header'><mtl:message key="jsp.dpr_order_info.GRP_HEADER"/></a></li>
					<li><a href="#tabs-order-detail" class='tabs-title order-detail'><mtl:message key="jsp.dpr_order_info.GRP_DETAIL"/></a></li>
					<li><a href="#tabs-order-billing" class='tabs-title order-billing'><mtl:message key="jsp.dpr_order_info.GRP_BILLING"/></a></li>
				<% if( com.irt.dpr.Country.isFeature(organizationCode, "useMemo") ) { %>
					<li><a href="#tabs-order-memo" class='tabs-title order-memo'><mtl:message key="jsp.dpr_order_info.GRP_MEMO"/></a></li>
				<% } %>
				</ul>
				<div id="tabs-order-header">
					<div class='group-wrap'>
						<h3><mtl:message key="jsp.dpr_order_info.GRP_BASE"/></h3>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'>
									<% if( com.irt.dpr.Country.isFeature(organizationCode, "useDangerousItem") ) { %>
										<mtl:title key="orderType" descriptionKey="jsp.MSG_ORDER_CATEGORY" mandatory="false"/>
									<% } else { %>
										<mtl:title key="orderType" mandatory="false"/>
									<% } %>
									</div>
									<div class='field-info'><mtl:valuef id="header" format="${DPR_ORDER_ORDERTYPE_@orderType}"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="status" mandatory="false"/></div>
									<div class='field-info'><mtl:valuef id="header" format="${DPR_ORDER_STATUS_@status}"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="orderDate" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="orderDate"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="orderStatus" mandatory="false"/></div>
									<div class='field-info'>
										<mtl:valuef id="header"
												format="$f{decode(officeCode,3220,$f{decode2(${DPR_ORDER_ORDERSTATUS_@orderStatus},orderStatus,PC,<font color='red'>,</font>,,)},3260,$f{decode2(${DPR_ORDER_ORDERSTATUS_@orderStatus},orderStatus,PC,<font color='red'>,</font>,,)},3270,$f{decode2(${DPR_ORDER_ORDERSTATUS_@orderStatus},orderStatus,PC,<font color='red'>,</font>,,)},3250,$f{decode2(${DPR_ORDER_ORDERSTATUS_@orderStatus},orderStatus,PC,<font color='white'>,</font>,,)},${DPR_ORDER_ORDERSTATUS_@orderStatus,–})}"/>
									</div>
								</div>
							</div>
							<div class='row'>
							<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePredefinedRDD") ) { %>
								<div class='cell'>
									<div class='field-title'><mtl:title key="inDateDefault" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="inDateDefault"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="inDateSimulation" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="inDateSimulation"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="inDate" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="inDate"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="deliveryDate" descriptionKey="FIELD_DPR_ORDER_INFO_DELIVERYDATE" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="deliveryDate"/></div>
								</div>
							<% } else { %>
								<div class='cell'>
									<div class='field-title'><mtl:title key="inDate" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="inDate"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="inDateDefault" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="inDateDefault"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="inDateSimulation" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="inDateSimulation"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="inDateConfirm" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="inDateConfirm"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="deliveryDate" descriptionKey="FIELD_DPR_ORDER_INFO_DELIVERYDATE" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="deliveryDate"/></div>
								</div>
							<% } %>
							</div>
							<div class='row'>
								<div class='cell' style='white-space: nowrap;'>
									<div class='field-title'><mtl:title key="partyCode" mandatory="false"/></div>
									<div class='field-info'><mtl:valuef id="header" format="[$f{pure(partyCode)}] ${partyName}"/></div>
								</div>
								<div class='cell'></div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="organizationCode" mandatory="false"/></div>
									<div class='field-info'><mtl:valuef id="header" format="[$f{pure(organizationCode)}] ${organizationName}"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="distributionChannelCode" mandatory="false"/></div>
									<div class='field-info'><mtl:valuef id="header" format="[$f{pure(distributionChannelCode)}] ${distributionChannelName}"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='cell' style='white-space: nowrap;'>
									<div class='field-title'><mtl:title key="soldPartyCode" mandatory="false"/></div>
									<div class='field-info'><mtl:valuef id="header" format="[$f{pure(soldPartyCode)}] ${soldPartyName}"/></div>
								</div>
								<div class='cell'></div>
								<div class='cell' style='white-space: nowrap;'>
									<div class='field-title'><mtl:title key="shipPartyCode" mandatory="false"/></div>
									<div class='field-info'><mtl:valuef id="header" format="[$f{pure(shipPartyCode)}] ${shipPartyName}"/></div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="simulationKey" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="simulationKey"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="orderNumber" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="orderNumber"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="deliveryNumber" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="deliveryNumber"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="goodsIssueDate" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="goodsIssueDate"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="creditReleaseDate" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="creditReleaseDate"/></div>
								</div>
							</div>
							<div class='row'>
							<mtl:ifvalue id="header" key="parentOrderNumber" notValue="">
								<div class='cell'>
									<div class='field-title'><mtl:title key="parentOrderNumber" descriptionKey="FIELD_DPR_ORDER_PARENT_ORDERNUMBER" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="parentOrderNumber"/></div>
								</div>
							</mtl:ifvalue>
							<mtl:ifvalue id="header" key="childOrderNumber" notValue="">
								<div class='cell'>
									<div class='field-title'><mtl:title key="childOrderNumber" descriptionKey="FIELD_DPR_ORDER_CHILD_ORDERNUMBER" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="childOrderNumber"/></div>
								</div>
							</mtl:ifvalue>
							<% if( com.irt.dpr.Country.isFeature(organizationCode, "usePackDeal") ) { %>
								<div class='cell'>
									<div class='field-title'><mtl:title key="dealCode" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="dealCode"/></div>
								</div>
							<% } %>
							</div>
						</div>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="remark" descriptionKey="FIELD_DPR_ORDER_REMARK" mandatory="false"/></div>
									<div class='field-info'><mtl:value id="header" key="remark"/></div>
								</div>
							</div>
						</div>
					</div>
					<div class='group-line'></div>

					<div class='group-wrap'>
						<h3><mtl:message key="jsp.dpr_order_info.GRP_VALUE"/></h3>
						<div class='info-table table-fixed'>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="infoOrderVolume" descriptionKey="jsp.dpr_order_info.FIELD_DPR_ORDER_VOLUME"/></div>
									<div class='field-info'><mtl:valuef id="header" format="$S{infoOrderVolume#NF.FLOAT2;$S{(:infoOrderVolumeUnit;)}}"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="infoOrderWeight" descriptionKey="jsp.dpr_order_info.FIELD_DPR_ORDER_WEIGHT"/></div>
									<div class='field-info'><mtl:valuef id="header" format="$S{infoOrderWeight#NF.FLOAT2;$S{(:infoOrderWeightUnit;)}}"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="orderValue" descriptionKey="jsp.dpr_order_info.FIELD_DPR_ORDER_ORDERNETAMOUNT"/></div>
									<div class='field-info'><mtl:valuef id="header" format="$S{orderValue#NF.CURRENCY}"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="confirmedOrderValue" descriptionKey="jsp.dpr_order_info.FIELD_DPR_ORDER_CONFIRM_ORDERNETAMOUNT"/></div>
									<div class='field-info'>
										<mtl:valuef id="header"
												format="$f{decode(isCurrInt,Y,${confirmedOrderValue#NF.INTEGER,N/A},${confirmedOrderValue#NF.CURRENCY,N/A})}"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="orderTax" descriptionKey="jsp.dpr_order_info.FIELD_DPR_ORDER_ORDERTAX"/></div>
									<div class='field-info'><mtl:valuef id="header" format="$S{orderTax#NF.CURRENCY}"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="confirmedOrderTax" descriptionKey="jsp.dpr_order_info.FIELD_DPR_ORDER_CONFIRM_ORDERTAX"/></div>
									<div class='field-info'>
										<mtl:valuef id="header"
												format="$f{decode(isCurrInt,Y,${confirmedOrderTax#NF.INTEGER,N/A},${confirmedOrderTax#NF.CURRENCY,N/A})}"/>
									</div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="orderDiscount" descriptionKey="jsp.dpr_order_info.FIELD_DPR_ORDER_DAMAGEDDISCOUNT"/></div>
									<div class='field-info'><mtl:valuef id="header" format="$S{orderDiscount#NF.CURRENCY}"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="confirmedOrderDiscount" descriptionKey="jsp.dpr_order_info.FIELD_DPR_ORDER_CONFIRM_DAMAGEDDISCOUNT"/></div>
									<div class='field-info'>
										<mtl:valuef id="header"
												format="$f{decode(isCurrInt,Y,${confirmedOrderDiscount#NF.INTEGER,N/A},${confirmedOrderDiscount#NF.CURRENCY,N/A})}"/>
									</div>
								</div>
							</div>
							<div class='row'>
								<div class='cell'>
									<div class='field-title'><mtl:title key="orderTotal" descriptionKey="jsp.dpr_order_info.FIELD_DPR_ORDER_ORDERVALUE"/></div>
									<div class='field-info'><mtl:valuef id="header" format="$S{orderTotal#NF.CURRENCY}"/></div>
								</div>
								<div class='cell'>
									<div class='field-title'><mtl:title key="confirmedOrderTotal" descriptionKey="jsp.dpr_order_info.FIELD_DPR_ORDER_CONFIRM_ORDERVALUE"/></div>
									<div class='field-info'>
										<mtl:valuef id="header"
												format="$f{decode(isCurrInt,Y,${confirmedOrderTotal#NF.INTEGER,N/A},${confirmedOrderTotal#NF.CURRENCY,N/A})}"/>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>

				<div id="tabs-order-detail">
					<mtl:ifvalue id="header" key="childOrderNumber" notValue="">
						<h3><mtl:message key="jsp.dpr_order_info.GRP_ORDER_DETAIL_LIST"/></h3>
					</mtl:ifvalue>
					<mtl:ifvalue id="header" key="childOrderNumber" value="">
						<div style='height: 30px; display: block;'></div>
					</mtl:ifvalue>

					<mtl:contentGroup groupId="detail" type="list" styleClass="list_content none-card">
						<div class='list-menu'>
							<mtl:select id="request" key="ftype" prefixKey="jsp.dpr_order_input.MSG_FORMATTYPE_"
									codeValues="PC,DZ" modified="tabLink(this, 1, null, \"sort\");" width="auto" searchable="false"/>
						</div>
						<%
							com.irt.custom.ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage );
							listwriter.setRecords( (java.util.List)pageContext.findAttribute("details") );
							listwriter.setScrollHeight( 360 );
							listwriter.setNumbering( false );
							listwriter.setSortable( false );
							listwriter.print( out );
						%>
					</mtl:contentGroup>

				<% if( com.irt.dpr.Country.isFeature(organizationCode, "useFreegoods") ) { %>
					<mtl:ifvalue id="header" key="childOrderNumber" notValue="">
					<h3><mtl:message key="jsp.dpr_order_info.GRP_FREEGODDS_LIST"/></h3>
					</mtl:ifvalue>
					<mtl:contentGroup groupId="freegoods" type="list" styleClass="list_content none-card">
						<mtl:ifvalue id="header" key="childOrderNumber" notValue="">
							<span style='color:red;'><mtl:message key="jsp.dpr_order_info.MSG_FREEGOODS_ORDER_DETAIL"/></span>
						</mtl:ifvalue>
						<%
							String freegoodsOrderNumber = (String)headerMap.get( "childOrderNumber" );
							if( freegoodsOrderNumber != null ) {
								ColumnList fgColumnList = (ColumnList)pageContext.findAttribute( "fgColumnList" );
								com.irt.custom.ListWriter fgListwriter = new com.irt.custom.ListWriter( request, htmlpage, fgColumnList );
								fgListwriter.setRecords( (java.util.List)pageContext.findAttribute("fgDetails") );
								fgListwriter.setScrollHeight( 300 );
								fgListwriter.setNumbering( false );
								fgListwriter.setSortable( false );
								fgListwriter.print( out );
							}
						%>
					</mtl:contentGroup>
				<% } %>
				</div>

				<div id="tabs-order-billing">
					<mtl:contentGroup groupId="billing" type="list" styleClass="list_content none-card" style='padding-top: 30px;'>
						<%
							ColumnList billingColumnList = (ColumnList)pageContext.findAttribute( "billingColumnList" );
							com.irt.custom.ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage, billingColumnList );
							listwriter.setRecords( (java.util.List)pageContext.findAttribute("billings") );
							listwriter.setScrollHeight( 300 );
							listwriter.setSortable( false );
							listwriter.print( out );
						%>
					</mtl:contentGroup>
				</div>
			<% if( com.irt.dpr.Country.isFeature(organizationCode, "useMemo") ) { %>
				<div id="tabs-order-memo">
					<mtl:contentGroup groupId="memo" type="list" styleClass="list_content none-card" style='padding-top: 30px;'>
						<%
							ColumnList memosColumnList = (ColumnList)pageContext.findAttribute( "memosColumnList" );
							com.irt.custom.ListWriter listwriter = new com.irt.custom.ListWriter( request, htmlpage, memosColumnList );
							listwriter.setRecords( (java.util.List)pageContext.findAttribute("memos") );
							listwriter.setScrollHeight( 300 );
							listwriter.setSortable( false );
							listwriter.print( out );
						%>
					</mtl:contentGroup>
				</div>
			<% } %>
			</div>
		</mtl:contentGroup>
	</mtl:form>
	</div>
	<%@ include file="include_dpr_tail.inc" %>
</body>
</mtl:html>
