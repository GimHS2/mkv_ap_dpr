<%--
	File Name:	dpr_product_req.jsp
	Version:	2.2.1

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2020/12/31		2.2.1	오류 수정
	hankalam	2017/08/31		2.2.0	create
--%>

<%@page import="com.irt.custom.PageConfig"%>
<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*, java.util.Map" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Object distributionChannels = pageContext.findAttribute( "distributionChannels" );
	Object offices = pageContext.findAttribute( "offices" );
	Object groups = pageContext.findAttribute( "groups" );
	Object soldParties = pageContext.findAttribute( "soldParties" );
	Object shipParties = pageContext.findAttribute( "shipParties" );
	String viewType = property.getProperty( "vtype" );
%>

<head>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
	<meta http-equiv='Content-Style-Type' content='text/css'/>
	<meta http-equiv='Content-Script-Type' content='text/javascript'/>
	<title><%= htmlpage.getTitle() %></title>
	<base href='<%= systemConfig.getBaseURL(htmlpage.getLocale()) %>'/>

	<script type='text/javascript' src='script/main.js'></script>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {

			<% if( "list".equals(viewType) ) { %>
				<% if( distributionChannels != null ) { %>
					setInnerHTML( parent.document.all.search_distribution, document.all.search_distribution.innerHTML );
				<% } %>

				<% if( soldParties != null ) { %>
					setInnerHTML( parent.document.all.search_soldParty, document.all.search_soldParty.innerHTML );
				<% } %>

				//setInnerHTML( parent.document.all.search_shipParty, document.all.search_shipParty.innerHTML );


				<% if( shipParties != null ) { %>
					setInnerHTML( parent.document.all.search_shipParty, document.all.search_shipParty.innerHTML );
				<% } %>

				<% if( offices != null ) { %>
					setInnerHTML( parent.document.all.search_office, document.all.search_office.innerHTML );
				<% } %>

				<% if( groups != null ) { %>
					setInnerHTML( parent.document.all.search_group, document.all.search_group.innerHTML );
				<% } %>

				parent.document.all.deliveryPlant.value = document.all.deliveryPlant.value;
			<% } else if( "input".equals(viewType) ) {
				if( soldParties != null ) { %>
					setInnerHTML( parent.document.all.input_soldParty, document.all.input_soldParty.innerHTML );
				<% } %>
				setInnerHTML( parent.document.all.input_allowUOM, document.all.input_allowUOM.innerHTML );
				setInnerHTML( parent.document.all.input_shipParty, document.all.input_shipParty.innerHTML );

			<% } %>
		}

		function setInnerHTML( contentObj, html ) {
			if( contentObj ) contentObj.innerHTML = html;
		}
	</script>
</head>

<body onload="JavaScript:bodyLoad();">
	<mtl:form name="frmCond">
		<%@ include file="include_rbm_listcond.inc" %>

		<span id='search_distribution'>
			<mtl:select id="condition" key="distributionChannelCode"
					nullValueKey="MSG_COND_DISTRIBUTION_CHANNEL" mandatory="true"
					hasBlank="true" listId="distributionChannels" listCodeKey="distributionChannelCode"
					listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}"
					modified="JavaScript:Field.modified(this);JavaScript:readTradePartnersReq(null, this.value);"/>
		</span>

		<span id='search_office'>
			<mtl:select id="condition" key="officeCode"
					nullValueKey="FIELD_DPR_PARTY_SALESOFFICE_CODE" mandatory="false"
					hasBlank="true" listId="offices" listCodeKey="officeCode"
					listNameFormat="$S{[:officeCode;$S{] :officeName}}"
					modified="JavaScript:readOfficeGroupReq(null, null, this.value); JavaScript:Field.modified(this);"/>
		</span>

		<span id='search_group'>
			<mtl:select id="condition" key="groupCode"
					nullValueKey="FIELD_DPR_PARTY_SALESGROUP_CODE" mandatory="false"
					hasBlank="true" listId="groups" listCodeKey="groupCode"
					listNameFormat="$S{[:groupCode;$S{] :groupName}}"
					modified="JavaScript:readTradePartnersReq(null,null); JavaScript:Field.modified(this);" />
		</span>
		<span id='search_soldParty'>
		<% if( sessionMng.isAdminUser() ) {%>
			<mtl:text id="condition" key="partyCode" styleClass="length_15" modified="JavaScript: readTradePartnerReq(\"ship\");" />
		<% } else {%>
			<mtl:select id="condition" key="partyCode" hasBlank="true"
				nullValueKey="MSG_COND_SOLDPARTY"
				listId="soldParties" listCodeKey="linkPartyCode"
				listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"
				modified="JavaScript: readTradePartnerReq(\"ship\"); Field.modified(this);"/>
		<% } %>
		</span>

		<span id='search_shipParty'>
			<mtl:select id="condition" key="shipPartyCode" hasBlank="true"
					nullValueKey="MSG_COND_SHIPPARTY"
					listId="shipParties" listCodeKey="linkPartyCode"
					listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"
					modified="JavaScript: readDeliveryPlantReq(); Field.modified(this);"/>
		</span>

		<span id='input_soldParty'>
			<mtl:select id="record" key="partyCode" hasBlank="true"
				nullValueKey="MSG_COND_SOLDPARTY"
				listId="soldParties" listCodeKey="linkPartyCode"
				listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"
				modified="JavaScript: removeItemNodes(itemWrapper); readTradePartnerReq(\"ship\"); treeReq(); Field.modified(this);"/>
		</span>

		<span id='input_shipParty'>
			<mtl:select id="record" key="shipPartyCode" hasBlank="true"
					nullValueKey="MSG_COND_SHIPPARTY"
					listId="shipParties" listCodeKey="linkPartyCode"
					listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"
					modified="JavaScript: removeItemNodes(itemWrapper); treeReq(); Field.modified(this);"/>
		</span>

		<span id='input_allowUOM'>
			<mtl:select id="record" key="uom" hasBlank="false"
					listId="allowUOMList" listCodeKey="allowUOM"
					listNameFormat="$H{allowUOM}"
					modified="JavaScript: Field.modified(this);"/>
		</span>
		<mtl:text id="condition" key="deliveryPlant" />
	</mtl:form>
</body>
</mtl:html>
