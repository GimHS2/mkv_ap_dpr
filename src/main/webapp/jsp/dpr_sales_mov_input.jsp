<%--
	File Name:	dpr_sales_mov_input.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	hankalam	2021/03/31		2.2.2	Ship-to 레벨 추가
	jbaek		2014/07/13		2.2.1	Sold-to Level MOV 기능 개발
	song7981	2013/04/30		2.2.0	create
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
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function bodyLoad() {
			windowResizeTo( 600 );
			resetForm( frmMain );
			focusForm( frmMain );
		}

		function modifyReq() {
			var btype = "<mtl:value id="property" key="btype"/>";
			var url = "<%= systemConfig.getClassURL() %>/DPRSalesMov?mode=imod";
			url = replaceQueryValue( url, "mode", "imod" );
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="record" key="organizationCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="record" key="distributionChannelCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "divisionCode", "<mtl:value id="record" key="divisionCode" encodeScript="true"/>" );

			if( "SPARTY" == btype || "DSPARTY" == btype ) {
				url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="record" key="shipPartyCode" encodeScript="true"/>" );
			} else if( "PARTY" == btype || "DPARTY" == btype) {
				url = replaceQueryValue( url, "officeCode", "<mtl:value id="record" key="officeCode" encodeScript="true"/>" );
				url = replaceQueryValue( url, "partyCode", "<mtl:value id="record" key="partyCode" encodeScript="true"/>" );
			} else {
				url = replaceQueryValue( url, "officeCode", "<mtl:value id="record" key="officeCode" encodeScript="true"/>" );
			}
			url = replaceQueryValue( url, "btype", btype );
			windowSelfOpen( url );
		}

		function registReq() {
			var url = "<%= systemConfig.getClassURL() %>?mode=ireg";
			url = replaceQueryValue( url, "btype", "<mtl:value id="property" key="btype"/>" );
			windowSelfOpen( url );
		}

		function readConditionReq( type ) {
			<mtl:ifvalue id="property" key="btype" valueList="PARTY,DPARTY">
				type = type + ";SOLD";
			</mtl:ifvalue>
			<mtl:ifvalue id="property" key="btype" valueList="SPARTY,DSPARTY">
				type = type + ";SHIP";
			</mtl:ifvalue>

			var url = "<%= systemConfig.getClassURL() %>/DPRSalesMov?mode=stp";
			readPartyAttributeReq( url, type, frmMain );
		}
	</script>
</head>

<body class='sub-content'>
	<div id='content-wrap' class='content-wrap'>
		<div id='messagebar'></div>
		<%@ include file="include_pub_list.inc" %>
		<%@ include file="include_rbm_bodyheader.inc"%>

		<mtl:form name="frmMain" fieldSetId="fieldSet" onSubmit="JavaScript:return checkInput();">
			<%@ include file="include_rbm_form.inc" %>
			<mtl:hidden id="property" key="btype"/>
			<mtl:hidden id="record" key="dangerousInd"/>
			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="organizationCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="organizationCode" mandatory="true" searchable="false"
										listId="organizations" listCodeKey="organizationCode"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO\");"/>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="distributionChannelCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="distributionChannelCode" mandatory="true"  searchable="false"
										listId="distributionChannels" listCodeKey="distributionChannelCode"
										listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SO\");"/>
							</div>
						</div>
					</div>
					<mtl:ifvalue id="property" key="btype" valueList="OFFICE,DOFFICE,PARTY,DPARTY">
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="officeCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="officeCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE"
										listId="offices" listCodeKey="officeCode" listNameFormat="$S{[:officeCode;$S{] :officeName}}"
										modified="readConditionReq();"/>
							</div>
						</div>
					</div>
					</mtl:ifvalue>
					<mtl:ifvalue id="property" key="btype" valueList="PARTY,DPARTY">
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="partyCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="partyCode" mandatory="true"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
										listId="soldParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"/>
							</div>
						</div>
					</div>
					</mtl:ifvalue>
					<mtl:ifvalue id="property" key="btype" valueList="SPARTY,DSPARTY">
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="shipPartyCode"/></div>
							<div class='field'>
								<mtl:select id="record" key="shipPartyCode" mandatory="true"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_SOLDTO"
										listId="shipParties" listCodeKey="linkPartyCode" listNameFormat="[$f{pure(linkPartyCode)}] $H{linkPartyName}"/>
							</div>
						</div>
					</div>
					</mtl:ifvalue>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="minimumValue"/></div>
							<div class='field'>
								<mtl:text id="record" key="minimumValue"/>
							</div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="close_if"/>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="reset"/>
						<mtl:button type="save"/>
					<% } else if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION ) { %>
						<mtl:button type="modify"/>
					<% } %>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>
					return submitInput();
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
