<%--
	File Name:	dpr_plantitem_cond.jsp
	Version:	2.2.0

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2014/02/17		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='euc-kr' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	Object distributionChannels = pageContext.findAttribute( "distributionChannels" );
	Object soldParties = pageContext.findAttribute( "soldParties" );
	Object shipParties = pageContext.findAttribute( "shipParties" );
	Object palnts = pageContext.findAttribute( "plants" );
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

			<% if( shipParties != null ) { %>
				setInnerHTML( parent.document.all.search_shipParty, document.all.search_shipParty.innerHTML );
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
			<mtl:select id="condition" key="distributionChannelCode" nullValueKey="MSG_COND_DISTRIBUTION_CHANNEL" mandatory="true"
					hasBlank="true" listId="distributionChannels" listCodeKey="distributionChannelCode"
					listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}"
					modified="JavaScript:Field.modified(this);JavaScript:readTradePartnersReq(null, this.value);"/>
		</span>

		<span id='search_shipParty'>
			<mtl:select id="condition" key="shipPartyCode" nullValueKey="MSG_COND_SHIPPARTY" hasBlank="true"
					listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="$S{[:linkPartyCode;$S{] :linkPartyName}}"/>
		</span>

	</mtl:form>
</body>
</mtl:html>
