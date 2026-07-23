<%--
	File Name:	dpr_rddmng_input.jsp
	Version:	2.2.2

	Description:

	Note:

	Modified	(YYYY/MM/DD)	Ver		Content
	dudwls3720	2024/09/23		2.2.2	Rdd 설정 수정 시 디폴트 값(1,2) 표시 안되도록 변경
	hankalam	2021/11/30		2.2.1	신규 UI/UX 적용
	hankalam	2019/06/28		2.2.0	create
--%>

<%@ page contentType="text/html; charset=utf-8" pageEncoding='UTF-8' %>
<%@ page import="com.irt.html.*" %>
<%@ taglib uri="/mtltaglib" prefix="mtl" %>
<%
	response.setHeader( "Cache-Control", "no-cache" );
	response.setHeader( "Pragma", "no-cache" );
	response.setDateHeader( "Expires", 0 );
%>
<mtl:html errorPage="error.jsp">
<%
	java.util.Map recordMap = (java.util.Map)pageContext.findAttribute( "record" );
	String updateStatus = "";
	if( recordMap != null )
		updateStatus = (String)recordMap.get( "updateStatus" );

	boolean inserting = "reg".equals(htmlpage.getMode()) || "ireg".equals(htmlpage.getMode());
%>
<head>
	<%@ include file="include_rbm_header.inc" %>
	<%@ include file="include_pub_input.inc" %>
	<script type='text/javascript'>
		function modifyReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRRddMng?mode=imod";
			url = replaceQueryValue( url, "organizationCode", "<mtl:value id="record" key="organizationCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "distributionChannelCode", "<mtl:value id="record" key="distributionChannelCode" encodeScript="true"/>" );
			url = replaceQueryValue( url, "vtype", "<mtl:value id="property" key="vtype"/>" );

			<mtl:ifvalue id="property" key="vtype" value="TRG">
				url = replaceQueryValue( url, "trgKey", "<mtl:value id="record" key="trgKey" encodeScript="true"/>" );
				url = replaceQueryValue( url, "officeCode", "<mtl:value id="record" key="officeCode" encodeScript="true"/>" );
			</mtl:ifvalue>

			<mtl:ifvalue id="property" key="vtype" value="IND">
				url = replaceQueryValue( url, "partyCode", "<mtl:value id="record" key="partyCode" encodeScript="true"/>" );
				url = replaceQueryValue( url, "shipPartyCode", "<mtl:value id="record" key="shipPartyCode" encodeScript="true"/>" );
			</mtl:ifvalue>

			windowSelfOpen( url );
		}

		function registReq() {
			var url = "<%= systemConfig.getClassURL() %>/DPRRddMng?mode=ireg";
			url = replaceQueryValue( url, "vtype", "<mtl:value id="property" key="vtype"/>" );
			windowSelfOpen( url );
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
			<input type='hidden' name='url' value="<%= htmlpage.getRequestURL() %>" />
			<mtl:hidden key="status" defaultValue="99" />
			<mtl:hidden id="property" key="vtype" />
			<mtl:contains id="record">
				<mtl:hidden id="record" key="trgKey" />
				<mtl:hidden id="record" name="beforeDayOfWeek" key="dayOfWeek" />
			</mtl:contains>
			<mtl:contentGroup groupId="main" type="content">
				<div class='info-table'>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="organizationCode"/></div>
							<div class='field'>
							<% if( inserting ) { %>
								<mtl:select id="record" key="organizationCode" mandatory="true" listId="organizations" listCodeKey="organizationCode"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_ORGANIZATION" format="$S{[:organizationCode;$S{] :organizationName}}"
										listNameFormat="$S{[:organizationCode;$S{] :organizationName}}" modified="readConditionReq(\"DC;SO\");"/>
							<% } else { %>
								<mtl:valuef id="record" key="organizationCode" format="$S{[:organizationCode;]$S{ :organizationName}}"/>
								<mtl:hidden id="record" key="organizationCode"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="distributionChannelCode" mandatory="false"/></div>
							<div class='field'>
							<% if( inserting ) { %>
								<mtl:select id="record" key="distributionChannelCode" listId="distributionChannels" listCodeKey="distributionChannelCode"
										hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT" searchable="false" mandatory="false"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_DISTRIBUTION" format="$S{[:distributionChannelCode;$S{] :distributionChannelName}}"
										listNameFormat="$S{[:distributionChannelCode;$S{] :distributionChannelName}}" modified="readConditionReq(\"SO\");"/>
							<% } else { %>
								<mtl:valuef id="record" key="distributionChannelCode" format="$S{[:distributionChannelCode;]$S{ :distributionChannelName}}"/>
								<mtl:hidden id="record" key="distributionChannelCode"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="officeCode" mandatory="false"/></div>
							<div class='field'>
							<% if( inserting ) { %>
								<mtl:select id="record" key="officeCode" hasBlank="true" nullValueKey="jsp.MSG_SELECT_NOT_SELECT"
										hasPlaceholder="true" placeholder="jsp.MSG_SELECT_PLACEHOLDER_OFFICE" mandatory="false"
										listId="offices" listCodeKey="code" listNameFormat="$S{[:officeCode;$S{] :officeName}}"
										format="$S{[:officeCode;]$S{ :officeName}}"/>
							<% } else { %>
								<mtl:valuef id="record" key="officeCode" format="$S{[:officeCode;]$S{ :officeName}}"/>
								<mtl:hidden id="record" key="officeCode"/>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="dayOfWeek"/></div>
							<div class='field'>
							<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
								<mtl:hidden id="record" key="dayOfWeek" />
								<mtl:check id="record" key="dayOfWeek1" descriptionKey="DPR_RDD_MNG_DAYOFWEEK_SHORT_1" checkValue="1" />
								<mtl:check id="record" key="dayOfWeek2" descriptionKey="DPR_RDD_MNG_DAYOFWEEK_SHORT_2" checkValue="2" />
								<mtl:check id="record" key="dayOfWeek3" descriptionKey="DPR_RDD_MNG_DAYOFWEEK_SHORT_3" checkValue="3" />
								<mtl:check id="record" key="dayOfWeek4" descriptionKey="DPR_RDD_MNG_DAYOFWEEK_SHORT_4" checkValue="4" />
								<mtl:check id="record" key="dayOfWeek5" descriptionKey="DPR_RDD_MNG_DAYOFWEEK_SHORT_5" checkValue="5" />
								<mtl:check id="record" key="dayOfWeek6" descriptionKey="DPR_RDD_MNG_DAYOFWEEK_SHORT_6" checkValue="6" />
								<mtl:check id="record" key="dayOfWeek7" descriptionKey="DPR_RDD_MNG_DAYOFWEEK_SHORT_7" checkValue="7" />
							<% } else {
								String dayOfWeeks = (String)recordMap.get( "dayOfWeek" );
								String value = "";
								if( dayOfWeeks != null && dayOfWeeks.length() > 0 ) {
									for( String dayOfWeek : dayOfWeeks.split(",") ) {
										value += "," + htmlpage.getMessageHandler().getMessage( "DPR_RDD_MNG_DAYOFWEEK_SHORT_" + dayOfWeek );
									}
								}
								if( value.indexOf(",") > -1 ) {
									value = value.substring( 1 );
								}
							%>
								<%= value %>
							<% } %>
							</div>
						</div>
					</div>
					<div class='row'>
						<div class='cell'>
							<div class='field-title'><mtl:title key="allowDays"/></div>
							<div class='field'>
							<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
								<mtl:hidden id="record" key="allowDays" />
								<% if( recordMap.get("allowDays") != null ) { %>
									<mtl:check id="record" key="allowDays1" descriptionKey="DPR_RDD_MNG_ALLOWDAYS_1" checkValue="1" />
									<mtl:check id="record" key="allowDays2" descriptionKey="DPR_RDD_MNG_ALLOWDAYS_2" checkValue="2" />
								<% } else { %>
									<mtl:check id="record" key="allowDays1" descriptionKey="DPR_RDD_MNG_ALLOWDAYS_1" checkValue="1" defaultValue="1" />
									<mtl:check id="record" key="allowDays2" descriptionKey="DPR_RDD_MNG_ALLOWDAYS_2" checkValue="2" defaultValue="2" />
								<% } %>
								<mtl:check id="record" key="allowDays3" descriptionKey="DPR_RDD_MNG_ALLOWDAYS_3" checkValue="3" />
								<mtl:check id="record" key="allowDays4" descriptionKey="DPR_RDD_MNG_ALLOWDAYS_4" checkValue="4" />
								<mtl:check id="record" key="allowDays5" descriptionKey="DPR_RDD_MNG_ALLOWDAYS_5" checkValue="5" />
								<mtl:check id="record" key="allowDays6" descriptionKey="DPR_RDD_MNG_ALLOWDAYS_6" checkValue="6" />
								<mtl:check id="record" key="allowDays7" descriptionKey="DPR_RDD_MNG_ALLOWDAYS_7" checkValue="7" />
							<% } else { %>
								<mtl:value id="record" key="allowDays" />
							<% } %>
							</div>
						</div>
					</div>
				</div>
				<div class='info-bottom'>
					<div class='table-cell info-button'>
						<mtl:button type="return"/>
					<% if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INPUT ) { %>
						<mtl:button type="save"/>
						<mtl:button type="reset"/>
					<% } else if( htmlpage.getInputStatus() == HtmlPage.INPUTSTATUS_INFORMATION ) { %>
						<mtl:button type="modify"/>
					<% } %>
						<mtl:button type="close_if"/>
					</div>
				</div>
			</mtl:contentGroup>

			<script type='text/javascript'>
				function checkInput() {
					<%= htmlpage.getValidationScript() %>
					<mtl:ifvalue id="property" key="vtype" value="TRG">
						var dayOfWeeks = ( frmMain.dayOfWeek1.checked ? "1," : "" ) + ( frmMain.dayOfWeek2.checked ? "2," : "" )
								+ ( frmMain.dayOfWeek3.checked ? "3," : "" ) + ( frmMain.dayOfWeek4.checked ? "4," : "" )
								+ ( frmMain.dayOfWeek5.checked ? "5," : "" ) + ( frmMain.dayOfWeek6.checked ? "6," : "" ) + ( frmMain.dayOfWeek7.checked ? "7," : "" );
						if( dayOfWeeks.length > 0 ) {
							dayOfWeeks = dayOfWeeks.substring( 0, dayOfWeeks.length - 1 );
						}
						frmMain.dayOfWeek.value = dayOfWeeks;

						var allowDays = ( frmMain.allowDays1.checked ? "1," : "" ) + ( frmMain.allowDays2.checked ? "2," : "" )
								+ ( frmMain.allowDays3.checked ? "3," : "" ) + ( frmMain.allowDays4.checked ? "4," : "" )
								+ ( frmMain.allowDays5.checked ? "5," : "" ) + ( frmMain.allowDays6.checked ? "6," : "" ) + ( frmMain.allowDays7.checked ? "7," : "" );
						if( allowDays.length > 0 ) {
							allowDays = allowDays.substring( 0, allowDays.length - 1 );
						}
						frmMain.allowDays.value = allowDays;
					</mtl:ifvalue>

					if( !Field.checkMandatory(frmMain.dayOfWeek) ) return;
					if( !Field.checkMandatory(frmMain.allowDays) ) return;

					return submitInput();
				}

				function readConditionReq( type ) {
					var url = "<%= systemConfig.getClassURL() %>/DPRRddMng?mode=rtp";
					readPartyAttributeReq( url, type, frmMain );
				}
			</script>
		</mtl:form>
	</div>
</body>
</mtl:html>
