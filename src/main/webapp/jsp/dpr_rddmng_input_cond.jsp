<%--
	File Name:	dpr_rddmng_input_cond.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2019/06/28		2.2.0	create
--%>

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

	Map<String, Object> condition = (Map<String, Object>)pageContext.findAttribute( "condition" );
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

			<% if( distributionChannels != null ) { %>
				setInnerHTML( parent.document.all.search_distribution, document.all.search_distribution.innerHTML );
			<% } %>

			<% if( soldParties != null ) { %>
				if( parent.document.all.partyCode != "undefined" ) {
					setInnerHTML( parent.document.all.search_soldParty, document.all.search_soldParty.innerHTML );
				} else if( parent.document.all.soldPartyCode != "undefined" ) {
					setInnerHTML( parent.document.all.search_soldPartyCode, document.all.search_soldPartyCode.innerHTML );
				}
			<% } %>

			setInnerHTML( parent.document.all.search_shipParty, document.all.search_shipParty.innerHTML );


			<%-- if( shipParties != null ) { %>
				setInnerHTML( parent.document.all.search_shipParty, document.all.search_shipParty.innerHTML );
			<% } --%>

			<% if( offices != null ) { %>
				setInnerHTML( parent.document.all.search_office, document.all.search_office.innerHTML );
			<% } %>

			<% if( groups != null ) { %>
				setInnerHTML( parent.document.all.search_group, document.all.search_group.innerHTML );
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
					nullValueKey="MSG_COND_DISTRIBUTION_CHANNEL" mandatory="false"
					hasBlank="true" listId="distributionChannels" listCodeKey="distributionChannelCode"
					listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}"
					modified="JavaScript:readOfficeGroupReq(null, this.value, null); JavaScript:Field.modified(this);"/>
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
					modified="JavaScript:Field.modified(this);" />
		</span>

		<span id='search_soldParty'>
			<mtl:select id="condition" key="partyCode" hasBlank="true"
					nullValueKey="MSG_COND_SOLDPARTY"
					listId="soldParties" listCodeKey="linkPartyCode"
					listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"
					modified="JavaScript: readTradePartnerReq(\"ship\"); Field.modified(this);"/>
		</span>

		<span id='search_shipParty'>
			<mtl:select id="condition" key="shipPartyCode" hasBlank="true"
					nullValueKey="MSG_COND_SHIPPARTY"
					listId="shipParties" listCodeKey="linkPartyCode"
					listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"
					modified="JavaScript:Field.modified(this);"/>
		</span>

	</mtl:form>
</body>
</mtl:html>
