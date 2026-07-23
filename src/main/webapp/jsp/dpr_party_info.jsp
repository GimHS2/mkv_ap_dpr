<%--
	File Name:	dpr_party_info.jsp
	Version:	2.2.3

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/03/30		2.2.3	contentGroup groupId를 uniq하게 지정.
	jbaek		2019/07/30		2.2.2	Base Party 숨김.
	jbaek		2014/03/31		2.2.1	CrossBrowsing 적용
	lsinji		2008/09/26		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='utf-8' %>
<%@ page import="com.irt.html.*, java.util.Map, java.util.List" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
response.setHeader( "Cache-Control", "no-cache" );
response.setHeader( "Pragma", "no-cache" );
response.setDateHeader( "Expires", 0 );
%>

<mtl:html errorPage="error.jsp">
<%
	java.util.Map condition = (java.util.Map)pageContext.findAttribute( "condition" );
	java.util.Map record = (java.util.Map)pageContext.findAttribute( "record" );
	int maxRows = com.irt.rbm.RBMSystem.getSystemEnvInt( "DPR", "Party;maxShowCount", 5 );

	if( record != null && record.size() > 0 ) {
		pageContext.setAttribute( "soldParties", (List<Map<String, Object>>)record.get("soldParties"), PageContext.PAGE_SCOPE );
		pageContext.setAttribute( "shipParties", (List<Map<String, Object>>)record.get("shipParties"), PageContext.PAGE_SCOPE );
		pageContext.setAttribute( "payerParties", (List<Map<String, Object>>)record.get("payerParties"), PageContext.PAGE_SCOPE );
		pageContext.setAttribute( "billParties", (List<Map<String, Object>>)record.get("billParties"), PageContext.PAGE_SCOPE );
	}

	boolean[] existingParties = new boolean[4];
	existingParties[0] = (pageContext.findAttribute( "soldParties" ) != null);
	existingParties[1] = (pageContext.findAttribute( "shipParties" ) != null);
	existingParties[2] = (pageContext.findAttribute( "billParties" ) != null);
	existingParties[3] = (pageContext.findAttribute( "payerParties" ) != null);

	int focusTabIndex = 0;
	String displayLinkType = request.getParameter( "displayLinkType" );
	if( existingParties[0] && ("AG".equals(displayLinkType) || "SP".equals(displayLinkType)) )
		focusTabIndex = 0;
	else if( "WE".equals(displayLinkType) || "SH".equals(displayLinkType) )
		focusTabIndex = ( existingParties[0] && existingParties[1] ? 1 : 0 );
	else if( "BE".equals(displayLinkType) || "BP".equals(displayLinkType) ) {
		if( existingParties[0] && existingParties[1] && existingParties[2] )
			focusTabIndex = 2;
		else if( !existingParties[0] && existingParties[1] && existingParties[2] )
			focusTabIndex = 1;
		else if( !existingParties[0] && !existingParties[1] && existingParties[2] )
			focusTabIndex = 0;
	} else if( "RG".equals(displayLinkType) || "PY".equals(displayLinkType) ) {
		if( existingParties[0] && existingParties[1] && existingParties[2] && existingParties[3] )
			focusTabIndex = 3;
		else if( !existingParties[0] && existingParties[1] && existingParties[2] && existingParties[3] )
			focusTabIndex = 2;
		else if( !existingParties[0] && !existingParties[1] && existingParties[2] && existingParties[3] )
			focusTabIndex = 1;
		else if( !existingParties[0] && !existingParties[1] && !existingParties[2] && existingParties[3] )
			focusTabIndex = 0;
	}

%>
<head>
<%@ include file="include_rbm_header.inc" %>
<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
	var windowWidth = 850;

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
		$("#tabs-main").tabs( {
			classes: {
				"ui-tabs": "tabs-main",
				"ui-tabs-nav": "tabs-nav",
				"ui-tabs-tab": "tabs-tab",
			}
		});

		$("#tabs-main").find( ".field-info" ).each( function(index, item) {
			if( $(item).text().trim() === "" ) {
				$(item).text( "–" );
			}
		});

		$("#tabs-main").tabs( "option", "active", "<%= focusTabIndex %>" );
	});

	function bodyLoad() {
		windowResizeTo( 850, 700 );
	}

	function infoPartner() {
		var url = "<%= systemConfig.getClassURL() %>/DPRPartyLink?mode=list";
		url = replaceQueryValue( url, "partyCode", "<mtl:value id="record" key="partyCode"/>" );
		url = replaceQueryValue( url, "organizationCode", "<mtl:value id="record" key="organizationCode"/>" );
		url = replaceQueryValue( url, "divisionCode", "<mtl:value id="record" key="divisionCode"/>" );
		url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="record" key="distributionChannelCode"/>" );

		windowOpen( url+"&wintype=sub", "sub-content" );
	}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_rbm_bodyheader.inc" %>
		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<input type='hidden' name='countryConditiones'>
			<input type='hidden' name='countryManager'>

			<mtl:contentGroup groupId="main" type="tabpane" styleClass="tabpane none-card">
				<div id='tabs-main'>
					<ul>
						<li><a href='#tabs-soldto' class='tabs-title'><mtl:message key="jsp.GRP_DPR_PARTY_SOLD"/></a></li>
						<li><a href='#tabs-shipto' class='tabs-title'><mtl:message key="jsp.GRP_DPR_PARTY_SHIP"/></a></li>
						<li><a href='#tabs-billto' class='tabs-title'><mtl:message key="jsp.GRP_DPR_PARTY_BILL"/></a></li>
						<li><a href='#tabs-payer' class='tabs-title'><mtl:message key="jsp.GRP_DPR_PARTY_PAYER"/></a></li>
					</ul>

					<div id='tabs-soldto'>
						<mtl:loop id="soldParties" loopId="loop" loopIndex="index">
							<mtl:contains id="loop" copyId="_record">
								<%@ include file="include_dpr_party_link_info.inc" %>
								<div class='group-line'></div>
							</mtl:contains>
						</mtl:loop>
					</div>

					<div id='tabs-shipto'>
						<mtl:loop id="shipParties" loopId="loop" loopIndex="index">
							<mtl:contains id="loop" copyId="_record">
								<%@ include file="include_dpr_party_link_info.inc" %>
								<div class='group-line'></div>
							</mtl:contains>
						</mtl:loop>
					</div>

					<div id='tabs-billto'>
						<mtl:loop id="billParties" loopId="loop" loopIndex="index">
							<mtl:contains id="loop" copyId="_record">
								<%@ include file="include_dpr_party_link_info.inc" %>
								<div class='group-line'></div>
							</mtl:contains>
						</mtl:loop>
					</div>

					<div id='tabs-payer'>
						<mtl:loop id="payerParties" loopId="loop" loopIndex="index">
							<mtl:contains id="loop" copyId="_record">
								<%@ include file="include_dpr_party_link_info.inc" %>
								<div class='group-line'></div>
							</mtl:contains>
						</mtl:loop>
					</div>
				</div>
<%--
			<mtl:contains id="soldParties">
			<mtl:contentGroup groupId="sold" type="tabpage" descriptionKey="jsp.GRP_DPR_PARTY_SOLD">
				<mtl:loop id="soldParties" loopId="loop" loopIndex="index">
					<mtl:contentGroup groupId="<%= \"soldParty_\" + index %>" fieldSetId="fieldSet_LNK" type="content">
						<mtl:contains id="loop" copyId="_record">
							<%@ include file="include_dpr_party_link_info.inc" %>
						</mtl:contains>
					</mtl:contentGroup>
				</mtl:loop>
			</mtl:contentGroup>
			</mtl:contains>

			<mtl:contains id="shipParties">
			<mtl:contentGroup groupId="ship" type="tabpage" descriptionKey="jsp.GRP_DPR_PARTY_SHIP">
				<mtl:loop id="shipParties" loopId="loop" loopIndex="index" >
					<mtl:contentGroup groupId="<%= \"shipParty_\" + index %>" fieldSetId="fieldSet_LNK" type="content">
						<mtl:contains id="loop" copyId="_record">
							<%@ include file="include_dpr_party_link_info.inc" %>
						</mtl:contains>
					</mtl:contentGroup>
				</mtl:loop>
			</mtl:contentGroup>
			</mtl:contains>

			<mtl:contains id="billParties">
			<mtl:contentGroup groupId="bill" type="tabpage" descriptionKey="jsp.GRP_DPR_PARTY_BILL">
				<mtl:loop id="billParties" loopId="loop" loopIndex="index">
					<mtl:contentGroup groupId="<%= \"billParty_\" + index %>" fieldSetId="fieldSet_LNK" type="content">
						<mtl:contains id="loop" copyId="_record">
							<%@ include file="include_dpr_party_link_info.inc" %>
						</mtl:contains>
					</mtl:contentGroup>
				</mtl:loop>
			</mtl:contentGroup>
			</mtl:contains>

			<mtl:contains id="payerParties">
			<mtl:contentGroup groupId="payer" type="tabpage" descriptionKey="jsp.GRP_DPR_PARTY_PAYER">
				<mtl:loop id="payerParties" loopId="loop" loopIndex="index">
					<mtl:contentGroup groupId="<%= \"payerParty_\" + index %>" fieldSetId="fieldSet_LNK" type="content">
						<mtl:contains id="loop" copyId="_record">
							<%@ include file="include_dpr_party_link_info.inc" %>
						</mtl:contains>
					</mtl:contentGroup>
				</mtl:loop>
			</mtl:contentGroup>
			</mtl:contains>
--%>
				<div class='list-function'>
					<div class='button'>
						<mtl:button type="return"/>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>
		</mtl:form>
	</div>
</body>
</mtl:html>
